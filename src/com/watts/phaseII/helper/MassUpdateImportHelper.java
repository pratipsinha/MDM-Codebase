package com.watts.phaseII.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeCollection;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeDefinition.Type;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationArea;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationObject;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.common.exceptions.PIMAlreadyInTransactionException;
import com.ibm.pim.common.exceptions.PIMSearchException;
import com.ibm.pim.context.Context;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.organization.Performer;
import com.ibm.pim.organization.Role;
import com.ibm.pim.organization.User;
import com.ibm.pim.search.SearchQuery;
import com.ibm.pim.search.SearchResultSet;
import com.ibm.pim.spec.SecondarySpec;
import com.ibm.pim.utils.Logger;
import com.ibm.pim.workflow.WorkflowStep;
import com.watts.phaseII.constants.WattsConstants;
import com.ww.pim.email.SendEmailNew;

public class MassUpdateImportHelper 
{

	HashMap<String,String[]> audit_Job_Columns;
	String emailMessage ;
	
	
	public MassUpdateImportHelper()
	{
		this.audit_Job_Columns = new HashMap<String,String[]>();
		this.emailMessage = "";
	}
	
	public void doExecuteMassUpdateImport(Context pimCtx, Logger wattsLogger,Catalog serviceCatalog, Document document, String docStoreFileName) 
	{
		doReadExcelDocAndUpdateItems(pimCtx,wattsLogger,serviceCatalog,document);
	}
	
	private void doReadExcelDocAndUpdateItems(Context pimCtx, Logger wattsLogger,Catalog serviceCatalog, Document document) 
	{
		
		wattsLogger.logInfo("MASS UPDATE IMPORT: MassUpdateImportHelper.doReadExcelDocAndUpdateItems()    START------");
		
		String fromAddress = "MDM_Notifications@wattswater.com";
		String mailSubject = "Workflow Mass Update Import Report";
		String mailBody = "Hi,\n\n\n The Workflow Mass Update Import Schedule has been completed.\n Please find attached the activity logs. \n\n\nRegards,\nPIMTeam.";
		
		StringBuffer email_log_buffer = new StringBuffer("----------------Workflow:Rich Search Import Summary--------------------------------"); 
		email_log_buffer.append("\r\n");
		
		audit_InitializeAuditErrorReportConfig();
		
		CollaborationArea collabAreaObj= pimCtx.getCollaborationAreaManager().getCollaborationArea(WattsConstants.MAIN_COLLAB_AREA);
		ItemCollaborationArea itemCollabAreaObj = (ItemCollaborationArea) collabAreaObj;
		
		HashMap<String,Object> errorReportConfig = audit_CreateAuditErrorReportConfig(pimCtx, WattsConstants.MASS_IMPORT_NAME, WattsConstants.MASS_IMPORT_JOB_TYPE);
		audit_WriteHeaderInAuditErrorReport(errorReportConfig, wattsLogger);
		
		HashMap<Integer,String> headerMap = new LinkedHashMap<Integer,String>();
		HashMap<String,String> eachRowDataMap = null;
		HashMap<String,String> eachRowAttributeErrorMap = null;
		
		HashMap<String,String> columnPathToBeUpdated = null;
		
		String strDocPath = null;
		InputStream oInputStream = null;
		XSSFRow oXSSFRow = null;;
		XSSFCell oXSSFCell = null;
		Iterator<Cell> iteCell = null;
		XSSFWorkbook wb = null;
		User currentUser = pimCtx.getCurrentUser();
		String currentUserName = currentUser.getName();
		PIMCollection<Role> currentUserRoles = currentUser.getRoles();
		
		String userEmailID = (null != currentUser.getEmail())?currentUser.getEmail():"mdrouson.sarwar@wattswater.com";
		email_log_buffer.append("userEmailID" + userEmailID).append("\r\n");
		
		String formatted_date = new SimpleDateFormat(WattsConstants.EMAIL_FILE_DATE_FORMAT).format(new Date()).toString();
		
		String email_attachFile_Name = "ExcelMassUpdateImportEmail_" + formatted_date + ".txt";
		String email_attach_File_Path =  "/logs/Mass Update Import/" + currentUserName + "/"+email_attachFile_Name;
		
		
		
		String emailfileName = "Workflow_Mass__Import_Logs_" + formatted_date + ".txt";
		String emailFilePath =  "/opt/IBM/MDMCSv115/Output Files/Workflow Import/"+emailfileName;
		
		boolean allowAssoc = false;
		boolean isPMRole = false;
		boolean isMarPDS = false;
		boolean isENG = false;
		CollaborationStep tempCollabStepObj = null;
		
		Hierarchy templateHierarchyObj = pimCtx.getHierarchyManager().getHierarchy(WattsConstants.TEMPLATES_HIERARCHY_NAME);
		
		if("Admin".equalsIgnoreCase(currentUserName))
		{
			isPMRole = true;
			tempCollabStepObj = itemCollabAreaObj.getStep(WattsConstants.PM_STEP_NAME);
			allowAssoc = true;
		}
		
		int noOfRoles = 0;
		for (Role role : currentUserRoles) 
		{
			if("PM".equalsIgnoreCase(role.getName()))
			{
				isPMRole = true;
				tempCollabStepObj = itemCollabAreaObj.getStep(WattsConstants.PM_STEP_NAME);
				noOfRoles++;
			}
			if("MAR-PDS".equalsIgnoreCase(role.getName()))
			{
				isMarPDS = true;
				tempCollabStepObj = itemCollabAreaObj.getStep(WattsConstants.MARPDS_STEP_NAME);
				noOfRoles++;
			}
			if("ENG-RD".equalsIgnoreCase(role.getName()) || "ENG-Sus".equalsIgnoreCase(role.getName()))
			{
				isENG = true;
				tempCollabStepObj = itemCollabAreaObj.getStep(WattsConstants.ENGG_STEP_NAME);
				noOfRoles++;
			}
			if("PM".equalsIgnoreCase(role.getName()) || ("" + currentUser.getCompany().getName() + "_admin").equalsIgnoreCase(role.getName()))
			{
				allowAssoc = true;
			}
		}
		
		String stepPathNames ;
		CollaborationStep collabStepObj;
		CollaborationStep collabStepTemp;
		
		int rowNum = 0;
		String lastColumnHeader = "";
		
		emailMessage = "Hi Team,\n\n\n Following is Mass Update Report :- \n\n" ;
		
		try
		{
			
			strDocPath = document.getRealPath();
			oInputStream = new FileInputStream(new File(strDocPath));
			wb = new XSSFWorkbook(oInputStream);
			XSSFSheet sheet = wb.getSheetAt(0);
			
			serviceCatalog.startBatchProcessing();
			//For Each Row
			for (Row row : sheet) 
			{
				wattsLogger.logInfo("MASS UPDATE IMPORT: Reading excel row number:: "+rowNum);
				email_log_buffer.append(" Processing row # " + rowNum + " : ").append("\r\n");
				eachRowDataMap = new LinkedHashMap<String, String>();
				eachRowAttributeErrorMap = new LinkedHashMap<String,String>();
				columnPathToBeUpdated = null;
				stepPathNames = "";
				collabStepObj = null;
				collabStepTemp= null;
				oXSSFRow = (XSSFRow)row;
				int cellNum = 0;
				int cellColumnIndex = 0;
				iteCell = oXSSFRow.cellIterator();
				
				
				while(iteCell.hasNext())
				{
					String cellValue = "";
					
					oXSSFCell = (XSSFCell) iteCell.next();
					if(null == oXSSFCell)
					{
						cellValue = "";
					}else if(Cell.CELL_TYPE_BOOLEAN == oXSSFCell.getCellType())
					{
						cellValue = String.valueOf(oXSSFCell.getBooleanCellValue());
						
					}else if(Cell.CELL_TYPE_NUMERIC == oXSSFCell.getCellType())
					{
						cellValue = String.valueOf(oXSSFCell.getNumericCellValue());
						
					}else if(Cell.CELL_TYPE_STRING == oXSSFCell.getCellType())
					{
						cellValue = "" + ((null != oXSSFCell.getStringCellValue())?oXSSFCell.getStringCellValue():"");
					}else if(Cell.CELL_TYPE_BLANK == oXSSFCell.getCellType())
					{
						cellValue = "";
					}
					cellColumnIndex = oXSSFCell.getColumnIndex();
					if(rowNum == 0)
					{
						//Header row
						String tempCellVal =cellValue;
						int startIndex = cellValue.indexOf(":");
						int lastIndex = cellValue.lastIndexOf(":");
						if(startIndex == lastIndex && tempCellVal.contains("REQUIRED"))
						{
							tempCellVal = cellValue.substring(0,startIndex);
						}else if(startIndex != lastIndex && tempCellVal.contains("REQUIRED"))
						{
							tempCellVal = cellValue.substring(0,lastIndex);
						}
						cellValue = tempCellVal;
						headerMap.put(cellNum, cellValue);
						
					}else
					{
						if(rowNum == 1)
						{
							lastColumnHeader = headerMap.get(headerMap.size()-1); 
						}
						String column_Name = headerMap.get(cellColumnIndex);
						String column_Val = cellValue;
						
						boolean columnValuePasses = checkSpecialCharacterForColumn(column_Name,column_Val);
						
						if(columnValuePasses)
						{	
							eachRowDataMap.put(column_Name, column_Val);
						}else
						{
							eachRowAttributeErrorMap.put(column_Name, column_Val);
						}
							
					}
					cellNum++;
				}
				
				//For testing iterate the maps data and error
				
				
				/*if(headerMap.size()>0 && rowNum == 0)
				{
					wattsLogger.logInfo("HEADER MAP size:::--"+headerMap.size());
					for(Map.Entry<Integer, String> entry0:headerMap.entrySet())
					{
						wattsLogger.logInfo(entry0.getKey()+":"+entry0.getValue());
					}
				}
			
				
				if(eachRowDataMap.size()>0)
				{
					wattsLogger.logInfo("DATA MAP size:::---"+eachRowDataMap.size());
					for(Map.Entry<String, String> entry1:eachRowDataMap.entrySet())
					{
						wattsLogger.logInfo(entry1.getKey()+":"+entry1.getValue());
					}
				}*/
				
				/*if(eachRowAttributeErrorMap.size()>0)
				{
					wattsLogger.logInfo("ERROR MAP:::--");
					for(Map.Entry<String, String> entry2:eachRowAttributeErrorMap.entrySet())
					{
						wattsLogger.logInfo(entry2.getKey()+":"+entry2.getValue());
					}
				}*/
				if(rowNum !=0)
				{
					
					//Data Row
					boolean isParrallel = false;
					boolean statusFlag = true;
					boolean bCheckedOut = false;
					String checkoutDetails = null;
					
					CollaborationItem collabItem = null;
					Item catalogItem = null;
					
					String excelPK = null;
					
					String itemPk = null;
					String systemOfRecord = null;
					String partNumber = null;
					String UPQ = null;
					
					excelPK = eachRowDataMap.get("PIM_ID");
					wattsLogger.logInfo("------1--------------"+excelPK);
					//Check if item is in collabArea
					if(null != excelPK)
					{
						if (excelPK.contains("."))
						{
							excelPK = excelPK.substring(0, excelPK.indexOf("."));
						}
						collabItem = itemCollabAreaObj.getCheckedOutItem(excelPK);
						
						if(null == collabItem)
						{
							try {
								
								catalogItem = serviceCatalog.getItemByPrimaryKey(excelPK);
							} catch (Exception e) 
							{
								catalogItem = null;
								wattsLogger.logInfo("------Exception getting catalog Item--------------"+e.getMessage());
								e.printStackTrace();
							}
							collabStepObj = tempCollabStepObj;
							if(null != collabStepObj && null != catalogItem)
							{
								wattsLogger.logInfo("---------Catalog Item---");
								wattsLogger.logInfo("---------Check Out To ["+collabStepObj.getName()+"]---");
							
								itemCollabAreaObj.checkoutAndWaitForStatus(catalogItem,collabStepObj);
								bCheckedOut = true;
								checkoutDetails = "StepName: "+collabStepObj.getName(); 
								collabItem =itemCollabAreaObj.getCheckedOutItem(excelPK);
								
							}else
							{
								wattsLogger.logInfo("-----Catalog Item is also null---");
							}
						}
						if(null != collabItem)
						{
							itemPk = collabItem.getPrimaryKey();
							systemOfRecord =  (null != collabItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH)?collabItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH).toString():"");
							partNumber = (null != collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"");
							UPQ = (null != collabItem.getAttributeValue(WattsConstants.UNIQUE_QUALI_PATH)?collabItem.getAttributeValue(WattsConstants.UNIQUE_QUALI_PATH).toString():"");
							if (!"".equals(UPQ)){
								emailMessage = emailMessage + "\nFOR ITEM WITH \n\t PIM : "+ itemPk+ "\n" + "UniqueProductQualifier: "+UPQ; 
							}
							else 
							{
								emailMessage = emailMessage + "\nFOR ITEM WITH \n\t PIM : "+ itemPk;
							}
							email_log_buffer.append("\t Provides data for [ PART_NUMBER: " + partNumber+"|"+"SYSTEM OF RECORD: "+systemOfRecord+" ]").append("\r\n");
							
							
							List<CollaborationStep> stepList = collabItem.getSteps();
							if(null != stepList)
							{
								if(stepList.size() == 1)
								{
									collabStepObj = stepList.get(0);
									stepPathNames = collabStepObj.getName();
								}else
								{
									
									if("Admin".equalsIgnoreCase(currentUserName))
									{
										if("Catalog".equalsIgnoreCase(lastColumnHeader))
										{
											lastColumnHeader = WattsConstants.PM_STEP_NAME;
										}
										collabStepObj = collabAreaObj.getStep(lastColumnHeader);
										stepPathNames = lastColumnHeader;
										email_log_buffer.append("  Step path is " + stepPathNames).append("\r\n");
										
									}else
									{
										for (CollaborationStep collabStep : stepList) 
										{
											stepPathNames = collabStep.getName()+"," + stepPathNames;
											Collection<Performer> stepPerformers = collabStep.getWorkflowStep().getPerformers();
											for (Role roleObj : currentUserRoles) 
											{
												if(stepPerformers.contains(roleObj))
												{
													if(noOfRoles >1)
													{
														collabStepObj = collabAreaObj.getStep(lastColumnHeader);
													}else
													{
														collabStepObj = collabStep;
													}
													email_log_buffer.append("  Step path is " + collabStepObj.getName()).append("\r\n");
													break;
												}
											}
										}
									}
									email_log_buffer.append("\n \t\t Item is in more than one step.\n").append("\r\n");
									isParrallel = true;
									
								}
								
							}
							
							//Collab Item,Step Object is retrieved.
							// Now to check if item is reserved or not and if so of the same role or not.
							boolean isReserved = false;
							boolean isReservedBySameRole = false;
							HashMap<Integer,String> rUserName = new HashMap<Integer, String>();
							HashMap<Integer, String> rName = new HashMap<Integer, String>();
							List<String> sameUser = new ArrayList<String>();
							
							isReserved = collabItem.isReserved();
							
							if(isReserved)
							{
								if(stepList.size() == 1)
								{
									collabStepTemp = stepList.get(0);
									rUserName.put(0,  collabStepTemp.getUserWhoReserved(collabItem).getUserName());
									if("Admin".equalsIgnoreCase(rUserName.get(0)) && !rUserName.get(0).equalsIgnoreCase(currentUserName))
									{
										isReservedBySameRole = true;
										sameUser.add(rUserName.get(0));
									}
									
								}else
								{
									int stepCounter = 0;
									int counter = 0;
									for (CollaborationStep collabStep : stepList) 
									{
										if (WorkflowStep.Type.AUTOMATED_STEP != collabStep.getWorkflowStep().getType() && WorkflowStep.Type.MERGE_STEP != collabStep.getWorkflowStep().getType())
										{
											
											String tempUserName = null;
											if(null != collabStep.getReservedObjects())
											{
												boolean itemReservedInthisStep = false;
												for (CollaborationObject collabObj : collabStep.getReservedObjects()) 
												{
													String collabObjPK = null;
													try
													{
														collabObjPK = ((CollaborationItem)collabObj).getPrimaryKey();
													}catch(Exception e)
													{
														collabObjPK = null;
														wattsLogger.logInfo("--:MASS UPDATE IMPORT:Calculating IsReservedBySameRole--:"+e.getMessage());
													}
													if(null != collabObjPK && itemPk.equals(collabObjPK))
													{
														itemReservedInthisStep = true;
														break;
													}
												}
												if(itemReservedInthisStep)
												{
													
													if(null != collabStep.getUserWhoReserved(collabItem))
													{
														
														tempUserName = collabStep.getUserWhoReserved(collabItem).getUserName();
														
													}
												}
											}
											if(null != tempUserName)
											{
												
												rName.put(stepCounter, tempUserName);
												rUserName.put(counter, rName.get(stepCounter));
												if("Admin".equalsIgnoreCase(rName.get(stepCounter)) && !currentUserName.equalsIgnoreCase(rName.get(stepCounter) ) && collabStep.getName().equalsIgnoreCase(collabStepObj.getName()))
												{
													isReservedBySameRole = true;
													sameUser.add("Admin");
												}
												PIMCollection<Role> roles = pimCtx.getOrganizationManager().getUser(rName.get(stepCounter)).getRoles();
												if(null != roles && !isReservedBySameRole)
												{
													for (Role role : roles) 
													{
														if(currentUserRoles.contains(role) && !rUserName.get(counter).equalsIgnoreCase(currentUserName))
														{
															isReservedBySameRole = true;
															sameUser.add(rUserName.get(counter));
															break;
														}
													}
												}
												
												counter++;
											}
										}
										stepCounter++;
									}
									email_log_buffer.append("Item is in Parallel step.\n").append("\r\n");
								}
							}
							
							
							wattsLogger.logInfo("isReserved:"+isReserved);
							wattsLogger.logInfo("isParrallel:"+isParrallel);
							wattsLogger.logInfo("isReservedBySameRole:"+isReservedBySameRole);
							
							
							
							//Now the main part comes to update item
							
							if((!isReserved) || (isUserSame(isReservedBySameRole, rUserName, currentUserName)) || (isParrallel && !isReservedBySameRole))
							{
								
								wattsLogger.logInfo("--Within the Update Condition--");
								boolean isItemReservedInCurrStep = collabItem.isReserved(collabStepObj);
								if(!isItemReservedInCurrStep)
								{
									String userComment = currentUserName+" : Reserving item for Mass Update Import";
									collabStepObj.reserve(collabItem, userComment, currentUser);
								}
								
								columnPathToBeUpdated = checkUserPriviledge(pimCtx,wattsLogger,collabItem,collabStepObj,currentUserName,stepPathNames,eachRowDataMap,partNumber,systemOfRecord,errorReportConfig,allowAssoc);
								
								if(null != columnPathToBeUpdated && columnPathToBeUpdated.size() >0)
								{
									wattsLogger.logInfo("Updated eachRowDataMap size ::"+eachRowDataMap.size());
									wattsLogger.logInfo("columnPathToBeUpdated size ::"+columnPathToBeUpdated.size());
									/*for (Map.Entry<String, String> entry : eachRowDataMap.entrySet()) 
									{
										wattsLogger.logInfo(entry.getKey()+":"+columnPathToBeUpdated.get(entry.getKey())+":"+entry.getValue());
									}*/
									email_log_buffer.append("Item has been determined to be updated").append("\r\n");
									if(bCheckedOut)
									{
										email_log_buffer.append("has been checked out - details : [" + checkoutDetails + "]").append("\r\n");
									}else
									{
										email_log_buffer.append("Item was already checked out").append("\r\n");
									}
									
									wattsLogger.logInfo("---Before Updating Item::["+itemPk+"]");
									updateItem(pimCtx,wattsLogger,collabItem,eachRowDataMap,columnPathToBeUpdated,currentUserName,stepPathNames,partNumber,systemOfRecord,errorReportConfig,templateHierarchyObj,serviceCatalog);
									wattsLogger.logInfo("---After Updating Item::["+itemPk+"]");
									
									//Now save the collab Item
									wattsLogger.logInfo("---Before Saving Item::["+itemPk+"]");
									saveCollabItemWithLogs(pimCtx,wattsLogger,collabItem,currentUserName,partNumber,systemOfRecord,stepPathNames,errorReportConfig);
									wattsLogger.logInfo("---After Saving Item::");
									
									
								}else
								{
									wattsLogger.logInfo("The input columns are not editable in this step");
									email_log_buffer.append("Requested Column can not be updated as they are not editable in step or user do not have privilege to edit it ").append("\r\n");
								}
								
								
								
							}else
							{
								wattsLogger.logInfo("-------------22222--Item is reserved by more than one user of same role or Admin--------");
								String reserveUser = "\r\n";
								for(int i=0; i<sameUser.size(); i++)
								{
									reserveUser = reserveUser +"Update Failed :- Part number is reserved by  " + sameUser.get(i)+"\r\n";
									email_log_buffer.append("Update Failed :- Part number is reserved by  " + sameUser.get(i)).append("\r\n");
								}
								HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
								
								toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
								toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
								toPrint.put("SystemOfRecord" ,systemOfRecord);
								toPrint.put("Part Number" ,partNumber);
								toPrint.put("Workflow Step", stepPathNames);
								toPrint.put("User Name", currentUserName);
								toPrint.put("Message", "Item is reserved by more than one user of same role or Admin");
								audit_WriteInAuditErrorReport(errorReportConfig, toPrint,wattsLogger);
								emailMessage = emailMessage + reserveUser ;
								statusFlag = false;
							}
							
							
						}else
						{
							email_log_buffer.append("\t\t  ERROR: could not find product to update.").append("\r\n");
							HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
							
							toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
							toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
							toPrint.put("SystemOfRecord" ,systemOfRecord);
							toPrint.put("Part Number" ,partNumber);
							toPrint.put("Workflow Step", stepPathNames);
							toPrint.put("User Name", currentUserName);
							toPrint.put("Message", "Item not found in catalog or workflow");
							
							statusFlag = false;
							
							audit_WriteInAuditErrorReport(errorReportConfig,toPrint,wattsLogger);
						}
					}else
					{
						wattsLogger.logInfo("Item PK is null or blank in the excel row ");
					}
					
					if(eachRowAttributeErrorMap.size()>0)
					{
						wattsLogger.logInfo("ERROR MAP:::--");
						String special_character_error_message = "Data can not  be saved as string contains special characters (@, ?, !, %, *, \', \", °, ™)";
						for(Map.Entry<String, String> entry2:eachRowAttributeErrorMap.entrySet())
						{
							String attr_Name = entry2.getKey();
							String attr_Val = entry2.getValue();
							
							email_log_buffer.append("\n\tAttributeName : " + attr_Name +"\n\tAttribute Value : "+ attr_Val + "\n\tError : "+ special_character_error_message + "\n").append("\r\n");
							emailMessage = emailMessage + "\n\t Update Failed :- \n\tAttributeName : " + attr_Name +"\n\tAttribute Value : "+ attr_Val + "\n\tError : "+special_character_error_message + "\n";
							
							HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
							
							toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
							toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
							toPrint.put("SystemOfRecord" ,systemOfRecord);
							toPrint.put("Part Number" ,partNumber);
							toPrint.put("Workflow Step", stepPathNames);
							toPrint.put("User Name", currentUserName);
							toPrint.put("Message", special_character_error_message);
							toPrint.put("Attribute",attr_Name);
							toPrint.put("Attribute Value", attr_Val);
							
							audit_WriteInAuditErrorReport(errorReportConfig,toPrint,wattsLogger);	
						}
					}
					
					if(statusFlag)
					{
						emailMessage = emailMessage + " \n Update Success \n" ;
						
						HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
						
						toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
						toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
						toPrint.put("SystemOfRecord" ,systemOfRecord);
						toPrint.put("Part Number" ,partNumber);
						toPrint.put("Workflow Step", stepPathNames);
						toPrint.put("User Name", currentUserName);
						toPrint.put("Message", "Part number processed successfully");
						
						audit_WriteInAuditErrorReport(errorReportConfig,toPrint,wattsLogger);
					}
					
				}
				
				rowNum++;
			}
			
			try
            {
                List<ExtendedValidationErrors> errs=serviceCatalog.flushBatch();
                // Process the failed items as identified from errs.
                // Perform any additional work for succeeded items.
                pimCtx.commit(); 
            }
            catch(Exception e)
            {
            	pimCtx.rollback();
                if (isCriticalException(e) )
                    throwRuntime(e);
                // Process all items in the batch as failed
            }
            finally
            {
                try
                {
                    pimCtx.startTransaction();
                }
                	catch(PIMAlreadyInTransactionException paie)
                {
                    // unexpected exception. re-throw
                    throwRuntime(paie);
                }
            }
            serviceCatalog.stopBatchProcessing();
            
		}catch (Exception e) 
		{
			e.printStackTrace();
			wattsLogger.logInfo("--:MASS UPDATE IMPORT:HELPER:doReadExcelDoc--Exception--:"+e.getMessage());
		}finally
		{
			//wattsLogger.logInfo("----------E-mail Message---::"+emailMessage);
			audit_SaveAuditErrorReport(pimCtx,wattsLogger,errorReportConfig);
			
			new CommonHelper().saveFileToDocstore(pimCtx, email_attach_File_Path, email_log_buffer, wattsLogger);
			/*String filePath = pimCtx.getDocstoreManager().getDocument(email_attach_File_Path).getRealPath();
			 
			wattsLogger.logInfo("---filePath:--"+filePath);
			wattsLogger.logInfo("---email_attachFile_Name:--"+email_attachFile_Name);
			emailMessage = emailMessage +"\n\n Thanks";
			if(!"".equals(userEmailID))
			{
				wattsLogger.logInfo("userEmailID:"+userEmailID);
				wattsLogger.logInfo("filePath:"+filePath);
				wattsLogger.logInfo("email_attachFile_Name:"+email_attachFile_Name);
				sendMailToRelevantUsers(userEmailID,filePath,email_attachFile_Name);
				
			}else
			{
				userEmailID = "mdrouson.sarwar@wattswater.com";//Sinha.Kumar@wattswater.com";
				wattsLogger.logInfo("userEmailID:"+userEmailID);
				wattsLogger.logInfo("filePath:"+filePath);
				wattsLogger.logInfo("email_attachFile_Name:"+email_attachFile_Name);
				sendMailToRelevantUsers(userEmailID,filePath,email_attachFile_Name);
				
			}*/
			new CommonHelper().saveToRealPath(pimCtx, emailFilePath, (StringBuffer) errorReportConfig.get("WRITER"), wattsLogger);
			
			new SendEmailNew().sendEmailToUser(userEmailID, fromAddress, mailBody, mailSubject, emailFilePath, emailfileName);
			
			
			
		}
		wattsLogger.logInfo("MASS UPDATE IMPORT: MassUpdateImportHelper.doReadExcelDocAndUpdateItems()    END------");
		
	}
	

	private void sendMailToRelevantUsers(String userEmailID, String filePath,String email_attachFile_Name) 
	{
		String fromAddress = "pim@watts.com";
		String mailSubject = "QAD Parts Maintenance :Mass Update Import!!";
		String mailBody = emailMessage;
		new SendEmailNew().sendEmailToUser(userEmailID, fromAddress, mailBody, mailSubject, filePath, email_attachFile_Name);
	}

	private void audit_SaveAuditErrorReport(Context pimCtx, Logger wattsLogger, HashMap<String, Object> errorReportConfig) 
	{
		String filePath = (String) errorReportConfig.get("FILENAME");
		StringBuffer sb = (StringBuffer) errorReportConfig.get("WRITER");
		//wattsLogger.logInfo("---Records written in String Buffer--"+sb.toString());
		new CommonHelper().saveFileToDocstore(pimCtx, filePath, sb, wattsLogger);
	}

	private void saveCollabItemWithLogs(Context pimCtx, Logger wattsLogger,CollaborationItem collabItem, String currentUserName,String partNumber, String systemOfRecord, String stepPathNames,HashMap<String, Object> errorReportConfig) 
	{
		ExtendedValidationErrors errs = null;
		try
		{
			collabItem.getCollaborationArea().getProcessingOptions().setPostSaveScriptProcessing(false);
			collabItem.getCollaborationArea().getProcessingOptions().setPostScriptProcessing(false);
			errs = collabItem.save();
			if(null != errs)
			{
				List<ValidationError> errorList=errs.getErrors();
				wattsLogger.logInfo("Errors during save....");
				for (ValidationError validationError : errorList) 
				{
					wattsLogger.logInfo(validationError.getMessage());
					wattsLogger.logInfo("Attribute:"+validationError.getAttributeInstance().getPath());
					wattsLogger.logInfo("Attribute Value Before Save:"+validationError.getAttributeInstance().getValue());
					
					HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
					
					toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
					toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
					toPrint.put("SystemOfRecord" ,systemOfRecord);
					toPrint.put("Part Number" ,partNumber);
					toPrint.put("Workflow Step", stepPathNames);
					toPrint.put("User Name", currentUserName);
					toPrint.put("Message", validationError.getMessage());
					toPrint.put("Attribute",validationError.getAttributeInstance().getPath());
					toPrint.put("Attribute Value", (null != validationError.getAttributeInstance().getValue())? validationError.getAttributeInstance().getValue().toString():"null");
					
					audit_WriteInAuditErrorReport(errorReportConfig, toPrint,wattsLogger);
					
					
				}
			}
			
		}catch (Exception e) 
		{
			wattsLogger.logInfo("Exception during save:"+e.getMessage());
			e.printStackTrace();
		}
		
	}

	private void updateItem(Context pimCtx, Logger wattsLogger,	CollaborationItem collabItem,HashMap<String, String> eachRowDataMap,HashMap<String, String> columnPathToBeUpdated,String currentUserName, String stepPathNames, String partNumber,String systemOfRecord, HashMap<String, Object> errorReportConfig, Hierarchy templateHierarchyObj, Catalog serviceCatalog) 
	{
		
		String strColumnName = null;
		String strOldValue = null;
		String strNewValue = null;
		
		List<String> arrAssociationTypes = new LinkedList<String>();
		List<List<String>> arrTypeAssociatedParts = new LinkedList<List<String>>();
		
		for (Map.Entry<String, String> entry: eachRowDataMap.entrySet()) 
		{
			strColumnName = entry.getKey();
			strNewValue = entry.getValue();
			
			
			String sNewValueWithoutDotZero = (null !=strNewValue)?strNewValue:null;
			
			wattsLogger.logInfo("Within Loop ["+strColumnName+"] with Value ["+sNewValueWithoutDotZero+"] ");
			if("PIM_ID".equalsIgnoreCase(strColumnName) || "Part Number".equalsIgnoreCase(strColumnName))
			{
				if(null != strNewValue && strNewValue.contains("."))
				{
					sNewValueWithoutDotZero = strNewValue.substring(0, strNewValue.indexOf("."));
				}
				//sNewValueWithoutDotZero = (null !=strNewValue)?strNewValue.replace(".0", ""):null;
			}
			
			if("Template".equalsIgnoreCase(strColumnName))
			{
				if(null != sNewValueWithoutDotZero && !"".equals(sNewValueWithoutDotZero))
				{
					strOldValue = getCategoryString(collabItem, null, WattsConstants.TEMPLATES_HIERARCHY_NAME, templateHierarchyObj);
					if(!sNewValueWithoutDotZero.equalsIgnoreCase(strOldValue))
					{
						
						String newTemplatePath = sNewValueWithoutDotZero.replaceAll(" *> *", "/");
						wattsLogger.logInfo("Before Updating ["+strColumnName+"] with Value ["+newTemplatePath+"] ");
						moveToCategory(pimCtx,wattsLogger,collabItem,null,WattsConstants.TEMPLATES_HIERARCHY_NAME,templateHierarchyObj,newTemplatePath);
					}
				}
			}else if("Type".equalsIgnoreCase(strColumnName) || "AssociatedParts".equalsIgnoreCase(strColumnName) || "PIM_ID".equalsIgnoreCase(strColumnName) || "Cloning Setup".equalsIgnoreCase(strColumnName) || "Part Number".equalsIgnoreCase(strColumnName))
			{
				
			}else if("FEBCO Valve to Accessory".equalsIgnoreCase(strColumnName) || "Kit".equalsIgnoreCase(strColumnName) || 
					"Kit Components".equalsIgnoreCase(strColumnName) || "Standard Retail to Private Label".equalsIgnoreCase(strColumnName) || 
					"System to Filters".equalsIgnoreCase(strColumnName) || "Watts Regulator to FEBCO".equalsIgnoreCase(strColumnName) || 
					"Wholesale To Retail".equalsIgnoreCase(strColumnName) || "Valve to Accessory".equalsIgnoreCase(strColumnName))
			{
				if(null != sNewValueWithoutDotZero && !"".equals(sNewValueWithoutDotZero))
				{
					wattsLogger.logInfo("Before Updating ["+strColumnName+"] with Value ["+sNewValueWithoutDotZero+"] ");
					List<String> arrAssociatedParts = new ArrayList<String>();
					
					String[] uPQArr =  sNewValueWithoutDotZero.split(",");//Unique Product Qualifier Array
					for (String uniqueProductQual : uPQArr) 
					{
						if(null != uniqueProductQual && !"".equals(uniqueProductQual))
						{
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
								String itemPk = getProductCatalogItemPK(pimCtx,wattsLogger,associatedPartNum,associatedSysOfRec);
								if(null != itemPk)
								{
									arrAssociatedParts.add(itemPk);
									
								}else
								{
									HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
									
									toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
									toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
									toPrint.put("SystemOfRecord" ,systemOfRecord);
									toPrint.put("Part Number" ,partNumber);
									toPrint.put("Workflow Step", stepPathNames);
									toPrint.put("User Name", currentUserName);
									toPrint.put("Message", "WARNING: Could not find part [" + uniqueProductQual + "] to set up association of type [" + strColumnName + "]");
									toPrint.put("Attribute",strColumnName);
									toPrint.put("Attribute Value", uniqueProductQual);
									
									audit_WriteInAuditErrorReport(errorReportConfig, toPrint,wattsLogger);
									
								}
								
							}else
							{
								HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
								
								toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
								toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
								toPrint.put("SystemOfRecord" ,systemOfRecord);
								toPrint.put("Part Number" ,partNumber);
								toPrint.put("Workflow Step", stepPathNames);
								toPrint.put("User Name", currentUserName);
								toPrint.put("Message", "WARNING: Invalid value given for "+strColumnName + " : " +uniqueProductQual);
								toPrint.put("Attribute",strColumnName);
								toPrint.put("Attribute Value", uniqueProductQual);
								
								audit_WriteInAuditErrorReport(errorReportConfig, toPrint,wattsLogger);
								
							}
						}
					}
					if ( arrAssociatedParts.size() > 0 )
					{
						arrAssociationTypes.add(strColumnName);
						arrTypeAssociatedParts.add(arrAssociatedParts);
					}	
					
				}
				
			}else
			{
				if(null != sNewValueWithoutDotZero && !"".equals(sNewValueWithoutDotZero))
				{
					if("__BLANK__".equalsIgnoreCase(sNewValueWithoutDotZero))
					{
						sNewValueWithoutDotZero = null;
					}
					
					String tempColumnName = strColumnName;
					String level = "";
					
					if(strColumnName.contains(":"))
					{
						int startIndex = strColumnName.indexOf(":");
						int lastIndex = strColumnName.lastIndexOf(":");
						
						if(startIndex == lastIndex)
						{
							level = strColumnName.substring(0, startIndex);
							tempColumnName = strColumnName.substring(startIndex+1, strColumnName.length());
						}
					}
					
					if("EA".equalsIgnoreCase(level) || "".equals(level))
					{
						wattsLogger.logInfo("Before Updating ["+strColumnName+"] with Value ["+sNewValueWithoutDotZero+"] ");
						try 
						{
							updateSingleOccurringAttributes(strColumnName,sNewValueWithoutDotZero,collabItem,columnPathToBeUpdated,wattsLogger);
						} catch (Exception e) 
						{
							HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
							
							toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
							toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
							toPrint.put("SystemOfRecord" ,systemOfRecord);
							toPrint.put("Part Number" ,partNumber);
							toPrint.put("Workflow Step", stepPathNames);
							toPrint.put("User Name", currentUserName);
							toPrint.put("Message", "EXCEPTION OCCURRED: "+e.getMessage());
							toPrint.put("Attribute",strColumnName);
							toPrint.put("Attribute Value", sNewValueWithoutDotZero);
							
							audit_WriteInAuditErrorReport(errorReportConfig, toPrint,wattsLogger);
							wattsLogger.logInfo("--:MASS UPDATE IMPORT:HELPER:-Exception--:"+e.getMessage());
							
						}
					}else
					{
						wattsLogger.logInfo("Before Updating ["+strColumnName+"] with Value ["+sNewValueWithoutDotZero+"] ");
						try 
						{
							updateMultioccurringAttributes(strColumnName,tempColumnName,sNewValueWithoutDotZero,level,collabItem,columnPathToBeUpdated,wattsLogger);
						} catch (Exception e) 
						{	
							HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
						
							toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
							toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
							toPrint.put("SystemOfRecord" ,systemOfRecord);
							toPrint.put("Part Number" ,partNumber);
							toPrint.put("Workflow Step", stepPathNames);
							toPrint.put("User Name", currentUserName);
							toPrint.put("Message", "EXCEPTION OCCURRED: "+e.getMessage());
							toPrint.put("Attribute",strColumnName);
							toPrint.put("Attribute Value", sNewValueWithoutDotZero);
						
							audit_WriteInAuditErrorReport(errorReportConfig, toPrint,wattsLogger);
							wattsLogger.logInfo("--:MASS UPDATE IMPORT:HELPER:-Exception--:"+e.getMessage());
							
						}
					}
				}
					
			}
			
			
		}
		
		if(arrAssociationTypes.size() >0)
		{
			wattsLogger.logInfo("Before Updating association");
			updateAssociations(pimCtx,wattsLogger,collabItem,arrAssociationTypes,arrTypeAssociatedParts,errorReportConfig,partNumber,systemOfRecord,currentUserName,stepPathNames,serviceCatalog);
			wattsLogger.logInfo("After Updating association");
		}
		
	}
	
	private void updateMultioccurringAttributes(String strColumnName,String tempColumnName, String strNewValue, String level,CollaborationItem collabItem,HashMap<String, String> columnPathToBeUpdated, Logger wattsLogger) 
	{
		String resolvedAttrValue = strNewValue;
		String attrPath = columnPathToBeUpdated.get(strColumnName);
		if(null != attrPath)
		{
			if(null != strNewValue && !"".equals(strNewValue))
			{
				AttributeInstance attrInstancePaths = collabItem.getAttributeInstance(WattsConstants.HIGHER_PACKAGING_PATHS);
				List<? extends AttributeInstance> childInstList = attrInstancePaths.getChildren();
				if(null !=childInstList && childInstList.size() > 0)
				{
					AttributeInstance attrInstHPackLevel = collabItem.getAttributeInstance(WattsConstants.HIGHER_PACKAGING_PATHS + "#0/Higher Packaging Levels");
					List<? extends AttributeInstance> hPackchildInstList =attrInstHPackLevel.getChildren();
					if(null !=hPackchildInstList && hPackchildInstList.size() > 0)
					{
						int noOfPacks = hPackchildInstList.size();
						for(int i = 0; i< noOfPacks ; i++)
						{
							String attr_Path_Prefix = WattsConstants.HIGHER_PACKAGING_PATHS + "#0/Higher Packaging Levels#"+i+"/";
							String packIdentifier = (null != collabItem.getAttributeValue(attr_Path_Prefix+"Unit of Measure"))?collabItem.getAttributeValue(attr_Path_Prefix+"Unit of Measure").toString():"";
							if(level.equalsIgnoreCase(packIdentifier))
							{
								String attr_Path_Temp = attr_Path_Prefix+tempColumnName;
								wattsLogger.logInfo("attr_Path_Temp :["+attr_Path_Temp+"]");
								AttributeInstance attrInstance = collabItem.getAttributeInstance(attr_Path_Temp);
								AttributeDefinition attrDef = attrInstance.getAttributeDefinition();
								AttributeDefinition.Type attrDefType = attrDef.getType();
								resolvedAttrValue = getResolvedValue(attrDefType,strNewValue);
								wattsLogger.logInfo("Multioccuring resolved Value::"+resolvedAttrValue);
								
								collabItem.setAttributeValue(attr_Path_Temp, resolvedAttrValue);
								
							}
						}
						
					}
					
				}
			}
		}
		
	}

	private void updateSingleOccurringAttributes(String strColumnName, String strNewValue,CollaborationItem collabItem,HashMap<String, String> columnPathToBeUpdated, Logger wattsLogger) 
	{
		String resolvedAttrValue = strNewValue;
		String attrPath = columnPathToBeUpdated.get(strColumnName);
		if(null != attrPath)
		{
			AttributeInstance attrInstance = collabItem.getAttributeInstance(attrPath);
			AttributeDefinition attrDef = attrInstance.getAttributeDefinition();
			AttributeDefinition.Type attrDefType = attrDef.getType();
			resolvedAttrValue = getResolvedValue(attrDefType,strNewValue);
			wattsLogger.logInfo("Single resolved Value::"+resolvedAttrValue);
			collabItem.setAttributeValue(attrPath, resolvedAttrValue);
			
			
		}
		
	}

	private String getResolvedValue(Type attrDefType,String strNewValue) 
	{
		String resolvedValue = strNewValue;
		if(AttributeDefinition.Type.FLAG == attrDefType)
		{
			if(null != strNewValue)
			{
				if("Y".equalsIgnoreCase(strNewValue) || "1".equalsIgnoreCase(strNewValue) || "TRUE".equalsIgnoreCase(strNewValue) || "YES".equalsIgnoreCase(strNewValue))
				{
					resolvedValue = "true";
				}else
				{
					resolvedValue = "false";
				}
			}
		}else if(AttributeDefinition.Type.DATE == attrDefType)
		{
			resolvedValue = null;
			try 
			{
				Date date = null;
				String defaultFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
				String db2Format = "yyyy-MM-dd-HH.mm.ss";
				String oracleFormat = "dd-MMM-yyyy HH:mm:ss";
				String otherFormatV10 = "yyyy-MM-dd";
				String otherFormatV11 = "M/d/y";
				
				if(null != strNewValue)
				{
					date = new SimpleDateFormat(otherFormatV11).parse(strNewValue);
					resolvedValue = (null != date)? new SimpleDateFormat(otherFormatV11).format(date):null;
					if(null == date)
					{
						date = new SimpleDateFormat(otherFormatV10).parse(strNewValue);
						resolvedValue = (null != date)? new SimpleDateFormat(otherFormatV10).format(date):null;
						
					}
					if(null == date)
					{
						date = new SimpleDateFormat(db2Format).parse(strNewValue);
						resolvedValue = (null != date)? new SimpleDateFormat(db2Format).format(date):null;
						
					}
					if(null == date)
					{
						date = new SimpleDateFormat(oracleFormat).parse(strNewValue);
						resolvedValue = (null != date)? new SimpleDateFormat(oracleFormat).format(date):null;
						
					}
					if(null == date)
					{
						date = new SimpleDateFormat(defaultFormat).parse(strNewValue);
						resolvedValue = (null != date)? new SimpleDateFormat(defaultFormat).format(date):null;
						
					}
					
				}
			} catch (ParseException e) 
			{
				e.printStackTrace();
			}
		     
			
		}else if(AttributeDefinition.Type.INTEGER == attrDefType)
		{
			if(null != strNewValue && strNewValue.contains("."))
			{
				int index = strNewValue.indexOf(".");
				resolvedValue = strNewValue.substring(0, index);
			}
			
		}else if(AttributeDefinition.Type.SEQUENCE == attrDefType)
		{
			//Not sure of the java api to get a sequence number[seqName = "Product Catalog_CTG_attribuitePath" ]
			//Script apis:  var seq = getSequenceByName(seqName);var nxtVal = getSequenceNextValue(seq);
		}
		
		
		return resolvedValue;
	}

	private void updateAssociations(Context pimCtx, Logger wattsLogger,CollaborationItem collabItem, List<String> arrAssociationTypes,List<List<String>> arrTypeAssociatedParts,HashMap<String, Object> errorReportConfig, String partNumber,String systemOfRecord, String currentUserName, String stepPathNames, Catalog serviceCatalog) 
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
				collabItem.getAttributeInstance(eachAssocPathPrefix+"AssociatedParts#"+j).setValue(serviceCatalog.getItemByPrimaryKey(itemPk));
				
			}
		}
		
		
	}

	private String getProductCatalogItemPK(Context pimCtx, Logger wattsLogger,String associatedPartNum, String associatedSysOfRec) 
	{

		String itemPk = null;
		String queryStr = "select item.pk from catalog('"+WattsConstants.MAIN_CATALOG+"') where item['"+WattsConstants.PART_NUMBER_PATH+"'] = '"+associatedPartNum+"' and item['"+WattsConstants.SYSTEM_OF_RECORD_PATH+"'] = '"+associatedSysOfRec+"'";
		SearchQuery queryObj = pimCtx.createSearchQuery(queryStr);
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

	private void moveToCategory(Context pimCtx, Logger wattsLogger,CollaborationItem collabItem, Item catalogItem,String templatesHierarchyName, Hierarchy templateHierarchyObj,String newTemplatePath) 
	{
		String delim = "/";
		
		try 
		{
			if(null != newTemplatePath && !"".equalsIgnoreCase(newTemplatePath))
			{
				Category oCat = null;
				Collection<String> fullNamePaths = new ArrayList<String>();
				fullNamePaths.add(newTemplatePath);
				Collection<Category> categoryList = templateHierarchyObj.getCategoriesByPaths(fullNamePaths, delim);
				if(null != categoryList)
				{
					for (Category category : categoryList) 
					{
						oCat = category;
					}
				}
				
				if(null != oCat)
				{
					//Unmap Old Category
					Collection<Category> oldCategories = null;
					if(null != collabItem)
					{
						oldCategories = collabItem.getCategories();
					}else if(null != catalogItem)
					{
						oldCategories = catalogItem.getCategories(templateHierarchyObj);
					}
					if(null != oldCategories)
					{
						for (Category category : oldCategories) 
						{
							if(null != catalogItem)
							{
								catalogItem.removeFromCategory(category);
							}else if(null != collabItem)
							{
								if(category.getHierarchy().equals(templateHierarchyObj))
								{
									collabItem.removeFromCategory(category);
								}
							}
						}
					}
					
					//Map to new cat
					
					if(null != catalogItem)
					{
						catalogItem.mapToCategory(oCat);
					}
					if(null != collabItem)
					{
						collabItem.mapToCategory(oCat);
					}
					
					
				}
				
			}
		} catch (Exception e) 
		{
			
			wattsLogger.logInfo("Move to Category Exception : "+e.getMessage());
			e.printStackTrace();
		}
		
	}

	private String getCategoryString(CollaborationItem collabItem,Item catalogItem,String hierarchyName,Hierarchy hierarchyObj)
	{
		String retStr = null;
		Collection<Category> catList = null;
		if(null != collabItem)
		{
			catList = collabItem.getCategories();
		}else if(null != catalogItem)
		{
			catList =  catalogItem.getCategories(hierarchyObj);
		}
		for (Category category : catList) 
		{
			if(null != catalogItem)
			{
				 retStr = (String) ((ArrayList)category.getFullPaths(" > ", false)).get(0);
			}else
			{
				if(category.getHierarchy().equals(hierarchyObj))
				{
					retStr = (String) ((ArrayList)category.getFullPaths(" > ", false)).get(0);
				}
			}
			
		}
		return retStr;
	}

	private HashMap<String, String> checkUserPriviledge(Context pimCtx,Logger wattsLogger, CollaborationItem collabItem,CollaborationStep collabStepObj, String currentUserName,String stepPathNames, HashMap<String, String> eachRowDataMap, String partNumber, String systemOfRecord, HashMap<String, Object> errorReportConfig, boolean allowAssoc) 
	{
		
		wattsLogger.logInfo("----Start method checkUserPriviledge() -----");
		Hierarchy templateHierarchyObj = pimCtx.getHierarchyManager().getHierarchy(WattsConstants.TEMPLATES_HIERARCHY_NAME);
		HashMap<String,String> columnPathToBeUpdated = new HashMap<String,String>();
		List<String> keysToRemove = new ArrayList<String>();
		String mailMessage = null;
		
		Collection<AttributeCollection> editableAttrCols = collabStepObj.getWorkflowStep().getEditableAttributeCollections();
		Collection<AttributeCollection> requiredAttrCols = collabStepObj.getWorkflowStep().getRequiredAttributeCollections();
		
		
		for (Map.Entry<String, String> entry : eachRowDataMap.entrySet()) 
		{
			String attr_Name = entry.getKey();
			String attr_Val = entry.getValue();
			String attr_Path = "";
			//wattsLogger.logInfo("Checking for Attribute :["+attr_Name+"]");
			if(attr_Name.contains(WattsConstants.SECONDARY_SPEC_NAME_POSTFIX) && null != attr_Val && !"".equals(attr_Val))
			{
				int startDelimIndex = 0;
				startDelimIndex = attr_Name.indexOf("/");
				if(startDelimIndex > 0)
				{
					String template_Spec_Name = attr_Name.substring(0, startDelimIndex);
					Collection<Category> categoryList = collabItem.getCategories();
					boolean isCategoryMapped = false;
					
					if(null != categoryList && categoryList.size() >0)
					{
						for (Category catObj : categoryList) 
						{
							Collection<SecondarySpec>  secSpecsList = catObj.getSecondarySpecs();
							if(null != secSpecsList)
							{
								for (SecondarySpec secSpecObj : secSpecsList) 
								{
									String specName = secSpecObj.getName();
									if(template_Spec_Name.equalsIgnoreCase(specName))
									{
										isCategoryMapped = true;
										break;
									}
								}
							}
							if(isCategoryMapped)
							{
								break;
							}
						}
						
					}
					wattsLogger.logInfo("----isCategoryMapped---::"+isCategoryMapped);
					
					if(!isCategoryMapped && null != categoryList && categoryList.size() ==0)
					{
						
						SecondarySpec secSpec =  ((null != pimCtx.getSpecManager().getSpec(template_Spec_Name))?(SecondarySpec)pimCtx.getSpecManager().getSpec(template_Spec_Name):null);
						if(null != secSpec)
						{
							PIMCollection<Category> catObjList = templateHierarchyObj.getCategoriesByItemSecondarySpec(secSpec);
							if(null != catObjList)
							{
								for (Category category : catObjList) 
								{
									try 
									{
										wattsLogger.logInfo("---Before mapping to category--"+category.getPrimaryKey());
										collabItem.mapToCategory(category);
										collabItem.getCollaborationArea().getProcessingOptions().setPostSaveScriptProcessing(false);
										collabItem.getCollaborationArea().getProcessingOptions().setPostScriptProcessing(false);
										collabItem.save();
										wattsLogger.logInfo("---After mapping to category--"+category.getPrimaryKey());
										
									} catch (Exception e) 
									{
										
										wattsLogger.logInfo("Exception Mapping item to Category :: "+e.getMessage());
										e.printStackTrace();
									}
									
								}
							}
						}
					}else if(isCategoryMapped)
					{
						emailMessage = emailMessage + "\n \t Update Allowed - As Item is already mapped to this template hierarchy \n";
					}else
					{
						emailMessage = emailMessage + "\n \t Update not allowed - Item is already mapped to another template hierarchy \n";
					}
					
					
					
				}
				
			}
			int startIndex = 0;
			int lastIndex = 0;
			String level = "";
			String tempAttrName = attr_Name;
			boolean isSecSpecAttr = false;
			String sec_Spec_Name = "";
			
			if(attr_Name.contains(":"))
			{
				startIndex = attr_Name.indexOf(":");
				lastIndex = attr_Name.lastIndexOf(":");
				
				if(startIndex == lastIndex)
				{
					level = attr_Name.substring(0, startIndex);
					tempAttrName = attr_Name.substring(startIndex+1, attr_Name.length());
					
				}
				
			}else if(attr_Name.contains(WattsConstants.SECONDARY_SPEC_NAME_POSTFIX))
			{
				int lastIndx = attr_Name.lastIndexOf("/");
				int firstIndx = attr_Name.indexOf("/");
				tempAttrName = attr_Name.substring(lastIndx+1);
				isSecSpecAttr = true;
				sec_Spec_Name = attr_Name.substring(0, firstIndx);
			}
			
			
			boolean flag = true; 
			if ("Template".equalsIgnoreCase(tempAttrName))
			{
				flag = false;
				attr_Path = "PATH_NOT_REQUIRED";
				
			}else if(allowAssoc)
			{
				if("FEBCO Valve to Accessory".equalsIgnoreCase(tempAttrName) || "Kit".equalsIgnoreCase(tempAttrName) || 
						"Kit Components".equalsIgnoreCase(tempAttrName) || "Standard Retail to Private Label".equalsIgnoreCase(tempAttrName) || 
						"System to Filters".equalsIgnoreCase(tempAttrName) || "Watts Regulator to FEBCO".equalsIgnoreCase(tempAttrName) || 
						"Wholesale To Retail".equalsIgnoreCase(tempAttrName) || "Valve to Accessory".equalsIgnoreCase(tempAttrName))
				{
					flag = false;
					attr_Path = "PATH_NOT_REQUIRED";
				}
			}
			for (AttributeCollection attributeCollection : editableAttrCols) 
			{
				Collection<AttributeDefinition> attrDefs = attributeCollection.getAttributes();
				for (AttributeDefinition attributeDef : attrDefs) 
				{
					int lastIndx = 0;
					String tempPath = attributeDef.getPath();
					lastIndx = tempPath.lastIndexOf("/");
					String tempAttr_Path = tempPath.substring(lastIndx+1);
					
					if(tempAttr_Path.equalsIgnoreCase(tempAttrName))
					{
						if("".equals(level))
						{
							if(!tempPath.toUpperCase().contains("EACH LEVEL") && !tempPath.toUpperCase().contains("PATHS"))
							{
								if(isSecSpecAttr)
								{
									if(tempPath.toUpperCase().contains(sec_Spec_Name.toUpperCase()))
									{
										flag = false;
										attr_Path = tempPath;
										break;
									}
									
								}else
								{
									flag = false;
									attr_Path = tempPath;
									break;
								}
								
							}
						}else
						{
							if("EA".equalsIgnoreCase(level))
							{
								if(tempPath.toUpperCase().contains("EACH LEVEL"))
								{
									flag = false;
									attr_Path = tempPath;
									break;
								}
							}else
							{
								if(tempPath.toUpperCase().contains("PATHS"))
								{
									flag = false;
									attr_Path = tempPath;
									break;
								}
							}
						}
					}
					
							
				}
			}
			
			for (AttributeCollection attributeCollection : requiredAttrCols) 
			{
				Collection<AttributeDefinition> attrDefs = attributeCollection.getAttributes();
				for (AttributeDefinition attributeDef : attrDefs) 
				{
					int lastIndx = 0;
					String tempPath = attributeDef.getPath();
					lastIndx = tempPath.lastIndexOf("/");
					String tempAttr_Path = tempPath.substring(lastIndx+1);
					
					if(tempAttr_Path.equalsIgnoreCase(tempAttrName))
					{
						if("".equals(level))
						{
							if(!tempPath.toUpperCase().contains("EACH LEVEL") && !tempPath.toUpperCase().contains("PATHS"))
							{
								if(isSecSpecAttr)
								{
									if(tempPath.toUpperCase().contains(sec_Spec_Name.toUpperCase()))
									{
										flag = false;
										attr_Path = tempPath;
										break;
									}
									
								}else
								{
									flag = false;
									attr_Path = tempPath;
									break;
								}
								
							}
						}else
						{
							if("EA".equalsIgnoreCase(level))
							{
								if(tempPath.toUpperCase().contains("EACH LEVEL"))
								{
									flag = false;
									attr_Path = tempPath;
									break;
								}
							}else
							{
								if(tempPath.toUpperCase().contains("PATHS"))
								{
									flag = false;
									attr_Path = tempPath;
									break;
								}
							}
						}
					}
					
				}
			}
			
			if(!flag)
			{
				if(null != attr_Val && !"".equals(attr_Val))
				{
					columnPathToBeUpdated.put(attr_Name, attr_Path);
				}else
				{
					keysToRemove.add(attr_Name);
				}
				
			}else
			{
				
				if(!attr_Name.contains("PIM_ID") && !attr_Name.contains("Part Number") && !attr_Name.contains("SystemOfRecord") && null != attr_Val && !"".equals(attr_Val))
				{
					HashMap<String,String> toPrint = new LinkedHashMap<String,String>();
					
					toPrint.put("Date", new SimpleDateFormat((String) errorReportConfig.get("DATE_FORMATTER")).format(new Date()).toString());
					toPrint.put("Time", new SimpleDateFormat((String) errorReportConfig.get("TIME_FORMATTER")).format(new Date()).toString());
					toPrint.put("SystemOfRecord" ,systemOfRecord);
					toPrint.put("Part Number" ,partNumber);
					toPrint.put("Workflow Step", stepPathNames);
					toPrint.put("User Name", currentUserName);
					toPrint.put("Message", "Attribute is not in editable attribute collection for this step");
					toPrint.put("Attribute",attr_Name);
					toPrint.put("Attribute Value", attr_Val);
					
					audit_WriteInAuditErrorReport(errorReportConfig, toPrint,wattsLogger);
					String errMessage = "\nUpdate not allowed .Attribute "+attr_Name+" is not in editable attribute collection for this step" ;
					if (null!=mailMessage)
					{
						mailMessage = mailMessage + errMessage ; 
					}
					else {
						mailMessage = errMessage ;			
	
					}
					
				}
				keysToRemove.add(attr_Name);
				
			}
			
			
		}
		if(null != mailMessage)
		{
			emailMessage = emailMessage +"\n\tUpdate Failed :-\t "+ mailMessage;
		}
		
		if(null !=keysToRemove && keysToRemove.size() > 0)
		{
			for (String key : keysToRemove) 
			{
				eachRowDataMap.remove(key);
			}
		}
		wattsLogger.logInfo("----END method checkUserPriviledge() -----");
		return columnPathToBeUpdated;
	}

	private boolean isUserSame(boolean isReserved, HashMap<Integer,String> rUser,String cUserName)
	{
		 
		for(int i=0; i < rUser.size(); i++)
		{
			if(!cUserName.equalsIgnoreCase(rUser.get(i)))
			{
				return false;
			}
		}
		return true;
	}

	private boolean checkSpecialCharacterForColumn(String column_Name,String column_Val) 
	{
		String regex = ".*[@?!%*\'\"°™].*";
		if(column_Name.contains("AdditionalTradeItemDescription_en-US") || column_Name.contains("Description-Marketing") 
		|| column_Name.contains("DescriptionShort_en-US") || column_Name.contains("Feature1_en-US")
		|| column_Name.contains("Feature10_en-US") || column_Name.contains("Feature2_en-US")
		|| column_Name.contains("Feature3_en-US") || column_Name.contains("Feature4_en-US")
		|| column_Name.contains("Feature5_en-US") || column_Name.contains("Feature6_en-US")
		|| column_Name.contains("Feature7_en-US") || column_Name.contains("Feature8_en-US")
		|| column_Name.contains("Feature9_en-US") || column_Name.contains("KeyWord1_en-US")
		|| column_Name.contains("ModelNumber") || column_Name.contains("Series"))
		{
			Pattern pat = Pattern.compile(regex);
			Matcher m = pat.matcher(column_Val);
			return (!m.matches());
			
		}
		return true;
	}

	public HashMap<String,Object> audit_CreateAuditErrorReportConfig(Context pimCtx,String jobName,String jobType)
	{
		HashMap<String,Object> errorReportConfig = new HashMap<String, Object>();
		String reportFileDestination = getAuditErrorReportDestination(pimCtx, jobName, jobType, null);
		String reportFileName = jobName;
		String formattedDate = new SimpleDateFormat(WattsConstants.AUDIT_ERRORREPORT_FILENAME_SUFFIX_DEFAULT_FORMATTER).format(new Date()).toString();
		String reportPath = reportFileDestination + reportFileName + "_" +formattedDate + "_" +pimCtx.getCurrentUser().getCompany().getName() + WattsConstants.AUDIT_ERRORREPORT_FILE_DEFAULT_EXTENSION;
		StringBuffer sb = new StringBuffer();
		errorReportConfig.put("JOBNAME", jobName);
		errorReportConfig.put("WRITER", sb);
		errorReportConfig.put("FILENAME", reportPath);
		errorReportConfig.put("COLUMN_DELIMITER", WattsConstants.AUDIT_ERRORREPORT_COLUMN_DEFAULT_DELIMITER);
		errorReportConfig.put("DATE_FORMATTER", WattsConstants.AUDIT_ERRORREPORT_DATE_DEFAULT_FORMATTER);
		errorReportConfig.put("TIME_FORMATTER", WattsConstants.AUDIT_ERRORREPORT_TIME_DEFAULT_FORMATTER);
		
		return errorReportConfig;
	}
	
	public void audit_InitializeAuditErrorReportConfig()
	{
		
		HashMap<String,String> audit_Jobs_Error_Report_Columns = new LinkedHashMap<String, String>();
		audit_Jobs_Error_Report_Columns.put(WattsConstants.MASS_IMPORT_NAME, WattsConstants.MASS_IMPORT_AUDIT_COLUMNS);
		
		for(Map.Entry<String, String> entry:audit_Jobs_Error_Report_Columns.entrySet())
		{
			String[] headers = entry.getValue().split(",");
			audit_Job_Columns.put(entry.getKey(), headers);
		}
		
	}
	
	public void audit_WriteHeaderInAuditErrorReport(HashMap<String,Object> errorReportConfig, Logger wattsLogger)
	{
		StringBuffer sb = null;
		sb = (StringBuffer) errorReportConfig.get("WRITER");
		if(null != errorReportConfig && errorReportConfig.size()>0 )
		{
			String recordToWrite = "";
			String jobName = (String) errorReportConfig.get("JOBNAME");
			String[] columns = audit_Job_Columns.get(jobName);
			recordToWrite = recordToWrite + columns[0];
			String strToBeWritten = null;
			for(int i = 1; i <columns.length ; i++)
			{
				strToBeWritten = columns[i];
				recordToWrite = recordToWrite +errorReportConfig.get("COLUMN_DELIMITER")+strToBeWritten;
			}
			
			sb.append(recordToWrite).append("\r\n");
		}
		
	}
	
	public void audit_WriteInAuditErrorReport(HashMap<String,Object> errorReportConfig,HashMap<String,String> keyValuePairs, Logger wattsLogger)
	{
		StringBuffer sb = null;
		sb = (StringBuffer) errorReportConfig.get("WRITER");
		if(null != errorReportConfig && null != keyValuePairs && errorReportConfig.size()>0 && keyValuePairs.size()>0)
		{
			String recordToWrite = "";
			String jobName = (String) errorReportConfig.get("JOBNAME");
			String[] columns = audit_Job_Columns.get(jobName);
			recordToWrite = recordToWrite + keyValuePairs.get(columns[0]);
			String strToBeWritten = null;
			for(int i = 1; i <columns.length ; i++)
			{
				strToBeWritten = "";
				if(i < keyValuePairs.size())
				{
					strToBeWritten = (null !=keyValuePairs.get(columns[i]))?keyValuePairs.get(columns[i]):"";
				}
				recordToWrite = recordToWrite +errorReportConfig.get("COLUMN_DELIMITER")+strToBeWritten;
			}
			sb.append(recordToWrite).append("\r\n");
		}
		
	}
	
	public String getAuditErrorReportDestination(Context pimCtx ,String jobName,String jobType,String fileDestination) 
	{
		if(null != fileDestination)
		{
			return (fileDestination.endsWith("/")?fileDestination:fileDestination+"/");
		}
		
		return (jobType+"/"+pimCtx.getCurrentUser().getUserName()+"/"+jobName+"/");
	}
	
	
	private void throwRuntime(Exception e) 
    {
        throw new RuntimeException(e);
    }
	
	private boolean isCriticalException(Exception e)
    {
        Throwable rootCause = e;
        Throwable cause = (e == null) ? null : e.getCause();
        while (cause != null) 
        {
            rootCause = cause;
            cause = cause.getCause();
        }
        return (rootCause instanceof SQLException);
    }
	

}
