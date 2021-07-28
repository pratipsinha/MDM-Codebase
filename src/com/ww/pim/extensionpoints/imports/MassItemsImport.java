package com.ww.pim.extensionpoints.imports;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.common.exceptions.PIMSearchException;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ImportFunction;
import com.ibm.pim.extensionpoints.ImportFunctionArguments;
import com.ibm.pim.organization.User;
import com.ibm.pim.search.SearchQuery;
import com.ibm.pim.search.SearchResultSet;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;
import com.watts.phaseII.helper.CommonHelper;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.email.SendEmailNew;

public class MassItemsImport implements ImportFunction {

	private Context ctx = PIMContextFactory.getCurrentContext();
	private String username = ctx.getCurrentUser().getUserName();
	private FileInputStream excelFileStream = null;
	public Catalog productCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
	//public ItemCollaborationArea collabArea = (ItemCollaborationArea) ctx.getCollaborationAreaManager().getCollaborationArea("Item Maintenance Staging Area");
	public ItemCollaborationArea collabArea = (ItemCollaborationArea) ctx.getCollaborationAreaManager().getCollaborationArea("QAD Parts Maintenance Staging Area");
	public Item objItem = null;
	public Spec objSpec = ctx.getSpecManager().getSpec("Product Catalog Global Spec");
	public String specName = objSpec.getName();
	private Logger objLogger = null;
	private Map<String, String> headerMap = null;
	boolean pimIDFlag;
	boolean partNoFlag;
	boolean sysOfRecFlag;
	

	@SuppressWarnings("unused")
	@Override
	public void doImport(ImportFunctionArguments arg0) 
	{
		// TODO Auto-generated method stub
		
		//RS: Added for Email Notification
		String fromAddress = "MDM_Notifications@wattswater.com";
		String mailSubject = "WPT_Admin_Mass_Items_Import Report";
		String mailBody = "Hi,\n\n\n The Admin Mass Load Schedule has been completed.\n Please find attached the activity logs. \n\n\nRegards,\nPIMTeam.";
		StringBuffer sb = new StringBuffer("");
		//RS:END
		
		User currentUser = ctx.getCurrentUser();
		String currentUserName = currentUser.getName();
		
		//RS: Added for Email Notification
		String userEmailID = (null != currentUser.getEmail())?currentUser.getEmail():"mdrouson.sarwar@wattswater.com";
		
		Random rand = new Random();
		int randomNum = rand.nextInt(99)+1;
		
		
		String formatted_date = new SimpleDateFormat("yyyyMMdd_HHmm_ss").format(new Date()).toString();
		
		String email_attachFile_Name = "WPT_Admin_Mass_Items_Import_Logs_" + formatted_date + randomNum+".txt";
		String email_attach_File_Path =  "/opt/IBM/MDMCSv115/Output Files/Admin Mass Import/"+email_attachFile_Name;
		
		sb.append("PIM_ID|Message");
		sb.append("\r\n");
		//RS:End
		
		if (username.equals("Admin")) 
		{
			objLogger = ctx.getLogger(Constant.IMPORT_LOGGER_NAME);
			objLogger.logDebug("****************Mass Import report started*****************");
			try 
			{
				// Reading Excel file
				String feedFilePath = arg0.getDocstoreDataDoc().getPath();
				String filePath = feedFilePath.substring(11);
				objLogger.logDebug("filePath--" + filePath);
				String serverFilePath = System.getProperty(Constant.DOLLAR_TOP) + "/feedfiles" + filePath;
				objLogger.logDebug("serverFilePath--" + serverFilePath);
				File fileName = new File(serverFilePath);

				if (!fileName.getName().endsWith(".xlsx")) 
				{
					objLogger.logDebug("Invalid Format for file " + fileName);
					return;
				}
				// Flag for checking header format
				pimIDFlag = false;
				partNoFlag = false;
				sysOfRecFlag = false;

				// Processing feed file
				excelFileStream = new FileInputStream(serverFilePath);
				objLogger.logDebug("Excel file name is : " + fileName.getName());
				Workbook workbook = new XSSFWorkbook(excelFileStream);
				Sheet dataSheet = workbook.getSheetAt(0);

				// Retrieving Header
				Row row = dataSheet.getRow(0);
				Cell cell = null;
				objLogger.logDebug("*****Retrieving Header *****");
				objLogger.logDebug("File is : " + fileName);
				parseHeader(row);

				if (pimIDFlag == false || sysOfRecFlag == false || partNoFlag == false) 
				{
					objLogger.logDebug("Wrong Header Format for file " + fileName);
					return;
				}

				int rowCount = dataSheet.getPhysicalNumberOfRows();
				objLogger.logDebug("Row count in import file is : " + rowCount);

				// Setting processing option to false
				//productCatalog.getProcessingOptions().setAllProcessingOptions(false);
				productCatalog.getProcessingOptions().setCollaborationAreaLocksValidationProcessing(false);

				for (int rowNum = 1; rowNum < rowCount; rowNum++) 
				{
					objLogger.logDebug("Retrieving data for rowNum : " + rowNum);
					row = dataSheet.getRow(rowNum);
					String PIM_id = "";
					String partNo = "";
					String sysOfRecord = "";
					
					int headerSize = headerMap.size();
					objLogger.logDebug("Size of the header is : " + headerSize);
					for (int j = 0; j < headerSize; j++) 
					{
						String headerName = headerMap.get(String.valueOf(j));
						if (headerName.equals("PIM_ID")) 
						{
							objLogger.logDebug("Column number is : " + j);
							cell = row.getCell(j);
							PIM_id = validateCellValue(cell, rowNum);
							if (isNumeric(PIM_id)) 
							{
								double var = Double.valueOf(PIM_id);
								int var2 = (int) var;
								PIM_id = String.valueOf(var2);
							}
							objLogger.logDebug("PIM ID is : " + PIM_id);
						} else if (headerName.equals("Part Number")) 
						{
							objLogger.logDebug("Column number is : " + j);
							cell = row.getCell(j);
							partNo = validateCellValue(cell, rowNum);
							if (isNumeric(partNo)) 
							{
								double var = Double.valueOf(partNo);
								int var2 = (int) var;
								partNo = String.valueOf(var2);
							}
							objLogger.logDebug("Part Number is : " + partNo);
						} else if (headerName.equals("SystemOfRecord")) 
						{
							objLogger.logDebug("Column number is : " + j);
							cell = row.getCell(j);
							sysOfRecord = validateCellValue(cell, rowNum);
							objLogger.logDebug("System of Record is : " + sysOfRecord);
						}else if (headerName.equals("PIM_ID")) 
							{
								objLogger.logDebug("Column number is : " + j);
								cell = row.getCell(j);
								PIM_id = validateCellValue(cell, rowNum);
								if (isNumeric(PIM_id)) 
								{
									double var = Double.valueOf(PIM_id);
									int var2 = (int) var;
									PIM_id = String.valueOf(var2);
								}
								objLogger.logDebug("PIM ID is : " + PIM_id);
							}
						if (!PIM_id.equals("") && !partNo.equals("") && !sysOfRecord.equals("")) 
						{
							objLogger.logDebug("Before exiting header loop :: Column number is : " + j);
							break;
						}

					}
					if (PIM_id.equals("")) 
					{
						objLogger.logDebug("PIM_ID is blank for row " + rowNum);
						continue;
					} else if (partNo.equals("")) 
					{
						objLogger.logDebug("Part Number is blank for row " + rowNum);
						continue;
					} else if (sysOfRecord.equals("")) 
					{
						objLogger.logDebug("System Of Record is blank for row " + rowNum);
						continue;
					}
					objLogger.logDebug("PIM_ID : "+PIM_id);
					objItem = productCatalog.getItemByPrimaryKey(PIM_id);
					objLogger.logDebug("Obj item found");
					CollaborationItem collabItem = null;

					if (null != objItem) 
					{
						
						//RS: Added for Association Updation
						List<String> arrAssociationTypes = new LinkedList<String>();
						List<List<String>> arrTypeAssociatedParts = new LinkedList<List<String>>();
						//RS:END
						
						if (objItem.isCheckedOut()) 
						{
							collabItem = collabArea.getCheckedOutItem(PIM_id);
						}

						for (int i = 0; i < headerSize; i++) 
						{
							String path = specName;
							String attribVal = "";
							String attribHeaderName = headerMap.get(String.valueOf(i));
							if (null != attribHeaderName) 
							{
								cell = row.getCell(i);
								if (attribHeaderName.equals("PIM_ID")) {
									continue;
								} else if (attribHeaderName.equals("Part Number")) {
									continue;
								} else if (attribHeaderName.equals("SystemOfRecord")) {
									continue;
								} else 
								{
									attribVal = validateCellValue(cell, rowNum);
									attribVal = attribVal.trim();
									if(attribVal.equals(""))
									{
										objLogger.logDebug("Blank cell..Hence skipping the attribute");
										continue;
									}
									String typeVal = null;
									String assocPartsVal = null;

									// Attribute Instances for Association
									//AttributeInstance associationAttribInst = null;
									//AttributeInstance associatedPartsAttribInst = null;
									//AttributeInstance associationChildAttribInst = null;
									//AttributeInstance associatedPartsChildAttribInst = null;

									// NoPartPresentFlag
									boolean noPartPresentFlag = false;

									HashMap<String, String> packPathMap = getPackagingLevelPath(objItem, PIM_id,
											specName);

									// Saving item

									if (attribHeaderName.contains(":")) 
									{
										objLogger.logDebug("Contains : ");
										String attrib[] = attribHeaderName.split(":");
										String packagingType = attrib[0].trim();
										attribHeaderName = attrib[1].trim();

										if (packagingType.equals("EA")) 
										{
											objLogger.logDebug("Packaging level Data : EA");
											path = path + "/Packaging Hierarchy/Each Level";
										} else if (packagingType.equals("MASTER")) 
										{
											objLogger.logDebug("Packaging level Data : MASTER");
											if (packPathMap.get("HAS_MASTER").equals("N")) 
											{
												objLogger.logDebug(
														"Item# " + PIM_id + " has no MASTER type Packaging level data");
												continue;
											}
											path = packPathMap.get("MASTER_PATH");
										} else if (packagingType.equals("INNER")) 
										{
											objLogger.logDebug("Packaging level Data : INNER");
											if (packPathMap.get("HAS_INNER").equals("N")) 
											{
												objLogger.logDebug(
														"Item# " + PIM_id + " has no INNER type Packaging level data");
												continue;
											}
											path = packPathMap.get("INNER_PATH");
										} else if (packagingType.equals("PALLET")) 
										{
											objLogger.logDebug("Packaging level Data : PALLET");
											if (packPathMap.get("HAS_PALLET").equals("N")) 
											{
												objLogger.logDebug(
														"Item# " + PIM_id + " has no PALLET type Packaging level data");
												continue;
											}
											path = packPathMap.get("PALLET_PATH");
										}
										if (attribVal.equals("__BLANK__")) 
										{
											attribVal = "";
										}
										objItem.setAttributeValue(path + "/" + attribHeaderName, attribVal);
										if (collabItem != null) 
										{
											collabItem.setAttributeValue(path + "/" + attribHeaderName, attribVal);
										}
									} else if (attribHeaderName.equals("Type")
											|| attribHeaderName.equals("AssociatedParts")) 
									{
										continue;
									} else if("Product to Component".equalsIgnoreCase(attribHeaderName) || "Kit".equalsIgnoreCase(attribHeaderName) || 
											"Kit Components".equalsIgnoreCase(attribHeaderName) || "Standard Retail to Private Label".equalsIgnoreCase(attribHeaderName) || 
											"System to Filters".equalsIgnoreCase(attribHeaderName) || "Watts Regulator to FEBCO".equalsIgnoreCase(attribHeaderName) || 
											"Wholesale To Retail".equalsIgnoreCase(attribHeaderName) || "Valve to Accessory".equalsIgnoreCase(attribHeaderName))
									
									{
										 
										//RS:Added for Association Update
										if (attribVal.equals("__BLANK__"))
										{
											removeAllAssociations(objLogger,objItem); 
											if(null != collabItem)
											{
												removeAllAssociations(objLogger,collabItem);
											}
											
										}else
										{
											
											if(attribVal.contains(";"))
											{
												sb.append(PIM_id+"|"+"WARNING:Associations should be seperated by comma not semicolon. Please correct and import again.");
											}else
											{
												objLogger.logInfo("Before Updating ["+attribHeaderName+"] with Value ["+attribVal+"] ");
												List<String> arrAssociatedParts = new ArrayList<String>();
												
												//String[] uPQArr =  attribVal.split(";");//Unique Product Qualifier Array
												String[] uPQArr =  attribVal.split(",");
												//objLogger.logInfo("TT01");
												for (String uniqueProductQual : uPQArr) 
												{
													//objLogger.logInfo("TT02 " +uniqueProductQual);
													if(null != uniqueProductQual && !"".equals(uniqueProductQual))
													{
														//objLogger.logInfo("TT03");
														String associatedPartNum = null;
														String associatedSysOfRec = null;
														boolean associationFound = false;
														for(String sysRec:WattsConstants.sysOfRecordList)
														{
															if(uniqueProductQual.toUpperCase().contains(sysRec.toUpperCase()))
															{
																int index =  uniqueProductQual.toUpperCase().indexOf(sysRec.toUpperCase());
																associatedPartNum = uniqueProductQual.substring(0, index-1).trim();
																associatedSysOfRec = sysRec.trim();
																associationFound = true;
																break;
															}
														}
														if(null != associatedPartNum && !"".equals(associatedPartNum) && associationFound)
														{
															//objLogger.logInfo("TT04");
															String itemPk = getProductCatalogItemPK(ctx,objLogger,associatedPartNum,associatedSysOfRec);
															//objLogger.logInfo("TT05");
															if(null != itemPk)
															{
																//objLogger.logInfo("Item Found"+associatedPartNum);
																//objLogger.logInfo("item "+itemPk);
																arrAssociatedParts.add(itemPk);
																
															}else
															{
																sb.append(PIM_id+"|"+"WARNING: Could not find part [" + uniqueProductQual + "] to set up association of type [" + attribHeaderName + "]");
															}
															
														}else
														{
															sb.append(PIM_id+"|"+"WARNING: Invalid value given for "+attribHeaderName + " : " +uniqueProductQual);
														}
													}
												}
												if ( arrAssociatedParts.size() > 0 )
												{
													arrAssociationTypes.add(attribHeaderName);
													arrTypeAssociatedParts.add(arrAssociatedParts);
												}
											}
											
											
										}
										
										//RS:END
										
									}else
									{
										try
										{
											objLogger.logDebug("Attribute path is in try block :"+path+ "/" + attribHeaderName);
											objItem.getAttributeInstance(path+ "/" + attribHeaderName);
											
										}
										catch(Exception e)
										{
											objLogger.logDebug("Not found...hence skipping");
											continue;
										}
										objLogger.logDebug("Default block");

										if (attribVal.equals("__BLANK__")) {
											attribVal = "";
										}

										objItem.setAttributeValue(path + "/" + attribHeaderName, attribVal);

										if (collabItem != null) 
										{
											collabItem.setAttributeValue(path + "/" + attribHeaderName, attribVal);
										}
									}
									
								}
							}
						}
						
						if(arrAssociationTypes.size() >0)
						{
							objLogger.logInfo("Before Updating association for Catalog Item");
							updateAssociations(ctx,objLogger,objItem,arrAssociationTypes,arrTypeAssociatedParts,partNo,sysOfRecord,currentUserName,productCatalog);
							objLogger.logInfo("After Updating association for Catalog Item");
							
							if (null != collabItem) 
							{
								objLogger.logInfo("Before Updating association for CollabItem");
								updateAssociations(ctx,objLogger,collabItem,arrAssociationTypes,arrTypeAssociatedParts,partNo,sysOfRecord,currentUserName,productCatalog);
								objLogger.logInfo("After Updating association for CollabItem");
							}
						}
						
						
						
						
						// Saving item
						ExtendedValidationErrors errors = objItem.save();
						if (null != collabItem) {
							collabItem.save();
						}

						if (null == errors) {
							objLogger.logDebug(" Item is saved....PIM_id : " + PIM_id);
							sb.append(PIM_id+"|"+"Item is saved successfully");
							sb.append("\r\n");

						} else {
							List<ValidationError> errorList = errors.getErrors();
							int errorCount = 0;
							for (ValidationError error : errorList) 
							{
								objLogger.logDebug("Error" + errorCount++ + " while saving item: " + PIM_id
										+ "---> Error is : " + error.getMessage() + "--->>"
										+ error.getAttributeInstance().getPath());
								sb.append(PIM_id+"|"+"Error" + errorCount++ + " while saving item: " + PIM_id
										+ "---> Error is : " + error.getMessage() + "--->>"
										+ error.getAttributeInstance().getPath());
								sb.append("\r\n");
							}
						}
					}
				}
			}

			catch (Exception e) {
				objLogger.logDebug("Exception caught.." + e);
			}

			finally 
			{
				//RS: Added for Email Notification
				new CommonHelper().saveToRealPath(ctx, email_attach_File_Path, sb, objLogger);
				new SendEmailNew().sendEmailToUser(userEmailID, fromAddress, mailBody, mailSubject, email_attach_File_Path, email_attachFile_Name);
				//RS:END
			}

		} else {

		}

	}

	private void removeAllAssociations(Logger objLogger,CollaborationItem collabItem) 
	{
		//Step 1:Remove all old associations
		AttributeInstance associationAttrInstanse = collabItem.getAttributeInstance(WattsConstants.PRIMARY_SPEC_NAME + "/Associations" );
		
		if(null != associationAttrInstanse)
		{
			List<? extends AttributeInstance> childrenInstanceList = associationAttrInstanse.getChildren();
			if(null != childrenInstanceList && childrenInstanceList.size() >0)
			{
				for (AttributeInstance attributeInstance : childrenInstanceList) 
				{
					attributeInstance.removeOccurrence();
				}
			}
		}
		
	}

	private void removeAllAssociations(Logger objLogger, Item objItem) 
	{
		//Step 1:Remove all old associations
		objLogger.logInfo("----1----");
		AttributeInstance associationAttrInstanse = objItem.getAttributeInstance(WattsConstants.PRIMARY_SPEC_NAME + "/Associations" );
		objLogger.logInfo("----2----");
		if(null != associationAttrInstanse)
		{
			List<? extends AttributeInstance> childrenInstanceList = associationAttrInstanse.getChildren();
			if(null != childrenInstanceList && childrenInstanceList.size() >0)
			{
				for (AttributeInstance attributeInstance : childrenInstanceList) 
				{
					attributeInstance.removeOccurrence();
				}
			}
		}
		
	}

	private void updateAssociations(Context ctx, Logger objLogger,Item objItem, List<String> arrAssociationTypes,List<List<String>> arrTypeAssociatedParts, String partNo,String sysOfRecord, String currentUserName, Catalog productCatalog) 
	{		
		//Step 1:Remove all old associations
		objLogger.logInfo("----1----");
		AttributeInstance associationAttrInstanse = objItem.getAttributeInstance(WattsConstants.PRIMARY_SPEC_NAME + "/Associations" );
		objLogger.logInfo("----2----");
		if(null != associationAttrInstanse)
		{
			List<? extends AttributeInstance> childrenInstanceList = associationAttrInstanse.getChildren();
			if(null != childrenInstanceList && childrenInstanceList.size() >0)
			{
				for (AttributeInstance attributeInstance : childrenInstanceList) 
				{
					attributeInstance.removeOccurrence();
				}
			}
		}
		
		objLogger.logInfo("----3----");
		//Step 2: Add new Associations
		int noOfAssociations = arrAssociationTypes.size();
		objLogger.logInfo("----4----");
		for(int i = 0; i < noOfAssociations ; i++)
		{
			String eachAssocPathPrefix  = WattsConstants.PRIMARY_SPEC_NAME + "/Associations#" + i+"/"; 
			objLogger.logInfo("----arrAssociationTypes.get(i)----"+arrAssociationTypes.get(i));
			if(null != arrAssociationTypes.get(i))
			{
				objItem.setAttributeValue(eachAssocPathPrefix+"Type", arrAssociationTypes.get(i));
				int noOfPartsPerAssociation = arrTypeAssociatedParts.get(i).size();
				objLogger.logInfo("----noOfPartsPerAssociation----"+noOfPartsPerAssociation);
				for(int j=0 ; j< noOfPartsPerAssociation ; j++)
				{
					if(null != arrTypeAssociatedParts.get(i).get(j))
					{
						String itemPk = arrTypeAssociatedParts.get(i).get(j);
						objLogger.logInfo("----itemPk----"+itemPk);
						Item ascItem = null;
						try
						{
							ascItem = productCatalog.getItemByPrimaryKey(itemPk);
						}catch(Exception xe)
						{
							objLogger.logInfo("---Associated item is null--");
						}
						if(null != ascItem)
						{
							if(j==0)
							{
								objItem.getAttributeInstance(eachAssocPathPrefix+"AssociatedParts#"+j).setValue(ascItem);
							}else
							{
								
								AttributeInstance attribInst = objItem.getAttributeInstance(eachAssocPathPrefix+"AssociatedParts");
								
								attribInst.addOccurrence();
								
								objItem.getAttributeInstance(eachAssocPathPrefix+"AssociatedParts#"+j).setValue(ascItem);
								
							}
						}
						
					}
				}
			}
		}
		objLogger.logInfo("----5----");
		
	}

	private void updateAssociations(Context ctx2, Logger objLogger2,CollaborationItem collabItem, List<String> arrAssociationTypes,List<List<String>> arrTypeAssociatedParts, String partNo,String sysOfRecord, String currentUserName, Catalog productCatalog) 
	{		
		//Step 1:Remove all old associations
		AttributeInstance associationAttrInstanse = collabItem.getAttributeInstance(WattsConstants.PRIMARY_SPEC_NAME + "/Associations" );
		
		if(null != associationAttrInstanse)
		{
			List<? extends AttributeInstance> childrenInstanceList = associationAttrInstanse.getChildren();
			if(null != childrenInstanceList && childrenInstanceList.size() >0)
			{
				for (AttributeInstance attributeInstance : childrenInstanceList) 
				{
					attributeInstance.removeOccurrence();
				}
			}
		}
		
		//Step 2: Add new Associations
		int noOfAssociations = arrAssociationTypes.size();
		for(int i = 0; i < noOfAssociations ; i++)
		{
			String eachAssocPathPrefix  = WattsConstants.PRIMARY_SPEC_NAME + "/Associations#" + i+"/"; 
			collabItem.setAttributeValue(eachAssocPathPrefix+"Type", arrAssociationTypes.get(i));
			int noOfPartsPerAssociation = arrTypeAssociatedParts.get(i).size();
			for(int j=0 ; j< noOfPartsPerAssociation ; j++)
			{
				String itemPk = arrTypeAssociatedParts.get(i).get(j);
				Item ascItem = null;
				try
				{
					ascItem = productCatalog.getItemByPrimaryKey(itemPk);
				}catch(Exception xe)
				{
					objLogger.logInfo("---Associated item is null--");
				}
				if(null != ascItem)
				{
					if(j==0)
					{
						collabItem.getAttributeInstance(eachAssocPathPrefix+"AssociatedParts#"+j).setValue(ascItem);
					}else
					{
						
						AttributeInstance attribInst = collabItem.getAttributeInstance(eachAssocPathPrefix+"AssociatedParts");
						
						attribInst.addOccurrence();
						
						collabItem.getAttributeInstance(eachAssocPathPrefix+"AssociatedParts#"+j).setValue(ascItem);
						
					}
				}
				
				
			}
		}
		
		
	}

	private String getProductCatalogItemPK(Context ctx2, Logger objLogger2,	String associatedPartNum, String associatedSysOfRec) 
	{

		String itemPk = null;
		String queryStr = "select item.pk from catalog('"+WattsConstants.MAIN_CATALOG+"') where item['"+WattsConstants.PART_NUMBER_PATH+"'] = '"+associatedPartNum+"' and item['"+WattsConstants.SYSTEM_OF_RECORD_PATH+"'] = '"+associatedSysOfRec+"'";
		objLogger2.logInfo(queryStr);
		SearchQuery queryObj = ctx2.createSearchQuery(queryStr);
		SearchResultSet rsltSet = queryObj.execute();
		try {
			if(null != rsltSet)
			{
				if(rsltSet.next())
				{
					itemPk = rsltSet.getString(1);
				}
			}
		} catch (PIMSearchException e) 
		{
			e.printStackTrace();
		}
		
		return itemPk;
	}

	private String validateCellValue(Cell cell, int rowNumber) throws Exception {
		try {
			if (null == cell) 
			{
				return "";
			}

			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC:

				double value = cell.getNumericCellValue();
				// value = Math.round(value);
				if (DateUtil.isCellDateFormatted(cell)) {
					if (DateUtil.isValidExcelDate(value)) {
						Date date = DateUtil.getJavaDate(value);
						SimpleDateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy");
						// objLogger.logDebug("Cell type Date...value is
						// :"+dateFormat.format(date));
						return dateFormat.format(date);
					} else {
						throw new Exception("Invalid Date value found at row number " + rowNumber
								+ " and column number " + cell.getColumnIndex());
					}
				} else {
					// objLogger.logDebug("Cell type Numeric...value is
					// :"+String.valueOf(value));
					//return String.valueOf(value);
					return BigDecimal.valueOf(value).toPlainString();
				}

			case Cell.CELL_TYPE_STRING:
				// objLogger.logDebug("Cell type String...value is
				// :"+cell.getStringCellValue());
				return cell.getStringCellValue();

			case Cell.CELL_TYPE_BLANK:
				// objLogger.logDebug("Cell type Blank...value is :");
				return "";
			default:
				return "";

			}
		} catch (Exception ex) {
			return "";
		}

	}

	private void parseHeader(Row row) {
		try {
			objLogger.logDebug("Inside parseHeader method.........");
			headerMap = new HashMap<String, String>();
			Cell cell = null;
			int maxCellNo = row.getPhysicalNumberOfCells();
			for (int i = 0; i < maxCellNo; i++) {
				cell = row.getCell(i);
				String headerVal = validateCellValue(cell, i);
				headerMap.put(String.valueOf(i), headerVal);
				if (headerVal.equals("PIM_ID")) {
					pimIDFlag = true;
				} else if (headerVal.equals("SystemOfRecord")) {
					sysOfRecFlag = true;
				} else if (headerVal.equals("Part Number")) {
					partNoFlag = true;
				}

			}
		} catch (Exception e) {
			objLogger.logDebug("Error inside parseHeader method....");
		}

	}

	@SuppressWarnings("unchecked")
	private HashMap<String, String> getPackagingLevelPath(Item objItem, String pimID, String specName) {
		HashMap<String, String> packLvlPath = new HashMap<String, String>();
		String palletPath = "";
		String innerPath = "";
		String masterPath = "";
		String isPallet = "N";
		String isInner = "N";
		String isMaster = "N";
		List<AttributeInstance> packagingLevelAttributeList = null;

		List<AttributeInstance> pathAttributeList = (List<AttributeInstance>) objItem
				.getAttributeInstance(specName + "/Packaging Hierarchy/Paths").getChildren();
		if (pathAttributeList.size() > 0) {
			AttributeInstance pathAttribInst = pathAttributeList.get(0);
			objLogger.logDebug("Path 1 is : " + pathAttribInst.getPath());
			packagingLevelAttributeList = (List<AttributeInstance>) objItem
					.getAttributeInstance(pathAttribInst.getPath() + "/Higher Packaging Levels").getChildren();

			if (null != packagingLevelAttributeList && packagingLevelAttributeList.size() > 0) {
				objLogger.logDebug("Path 2 is : " + packagingLevelAttributeList.get(0).getPath());
				for (AttributeInstance packagingLevelAttribInst : packagingLevelAttributeList) {
					objLogger.logDebug("Retrieving path for different Packaging level attribute ");
					String packagingType = (null == objItem.getAttributeValue(packagingLevelAttribInst.getPath() + "/Unit of Measure") ? "": objItem.getAttributeValue(packagingLevelAttribInst.getPath() + "/Unit of Measure").toString());
					if (packagingType.equals("PALLET")) {
						objLogger.logDebug("Item " + pimID + " has PALLET Packaging type");
						isPallet = "Y";
						palletPath = packagingLevelAttribInst.getPath();
						objLogger.logDebug("Path of PALLET : " + palletPath);
					} else if (packagingType.equals("INNER")) {
						objLogger.logDebug("Item " + pimID + " has INNER Packaging type");
						isInner = "Y";
						innerPath = packagingLevelAttribInst.getPath();
						objLogger.logDebug("Path of INNER : " + innerPath);
					} else if (packagingType.equals("MASTER")) {
						objLogger.logDebug("Item " + pimID + " has MASTER Packaging type");
						isMaster = "Y";
						masterPath = packagingLevelAttribInst.getPath();
						objLogger.logDebug("Path of MASTER : " + masterPath);
					} else {
						objLogger.logDebug("Item has wrong Packaging Type :" + packagingType);
					}
				}
			}
		}
		packLvlPath.put("MASTER_PATH", masterPath);
		packLvlPath.put("HAS_MASTER", isMaster);
		packLvlPath.put("INNER_PATH", innerPath);
		packLvlPath.put("HAS_INNER", isInner);
		packLvlPath.put("PALLET_PATH", palletPath);
		packLvlPath.put("HAS_PALLET", isPallet);
		return packLvlPath;
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
