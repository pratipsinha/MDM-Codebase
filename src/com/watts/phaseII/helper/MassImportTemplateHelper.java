package com.watts.phaseII.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeDefinition.Type;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationArea;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.common.exceptions.PIMAlreadyInTransactionException;
import com.ibm.pim.context.Context;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.organization.Role;
import com.ibm.pim.organization.User;
import com.ibm.pim.search.SearchQuery;
import com.ibm.pim.search.SearchResultSet;
import com.ibm.pim.spec.SecondarySpec;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;
import com.ww.pim.email.SendEmailNew;

public class MassImportTemplateHelper 
{
	StringBuffer sb = null;
	public MassImportTemplateHelper()
	{
		sb = new StringBuffer("");
		
	}
	
	public void executeMassImportForTemplates(Context pimCtx,Logger wattsLogger, Catalog serviceCatalog, Document document,String docStoreFileName, PrintWriter out) 
	{
		readDocAndUpdateItems(pimCtx,wattsLogger,serviceCatalog,document,out);
	}

	private void readDocAndUpdateItems(Context pimCtx, Logger wattsLogger,Catalog serviceCatalog, Document document, PrintWriter out) 
	{
		
		wattsLogger.logInfo("TEMPLATE MASS IMPORT: MassImportTemplateHelper.readDocAndUpdateItems()    START------");
		
		//String tableNameWithSchema = "PIMDBUSR.WPT_EMAIL_TEMPLATE_LOAD";
		//Map<String,String> valueMap = new HashMap<String, String>();
		String fromAddress = "MDM_Notifications@wattswater.com";
		String mailSubject = "Template Mass Update Report";
		String mailBody = "Hi,\n\n\n The Template Mass Load Schedule has been completed.\n Please find attached the activity logs. \n\n\nRegards,\nPIMTeam.";
		sb.append("PartNumber|SystemOfRecord|Message");
		sb.append("\r\n");
		CollaborationArea collabAreaObj= pimCtx.getCollaborationAreaManager().getCollaborationArea(WattsConstants.MAIN_COLLAB_AREA);
		ItemCollaborationArea itemCollabAreaObj = (ItemCollaborationArea) collabAreaObj;
		
		HashMap<Integer,String> headerMap = new LinkedHashMap<Integer,String>();
		HashMap<String,String> eachRowDataMap = null;
			
		
		
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
		
		String formatted_date = new SimpleDateFormat(WattsConstants.EMAIL_FILE_DATE_FORMAT).format(new Date()).toString();
		
		String email_attachFile_Name = "Template_Mass_Update_Import_Logs_" + formatted_date + ".txt";
		String email_attach_File_Path =  "/opt/IBM/MDMCSv115/Output Files/Template Mass Update Import/"+email_attachFile_Name;
		
		
		boolean allowUpdate = false;
		
		if("Admin".equalsIgnoreCase(currentUserName))
		{
			allowUpdate = true;
			//userEmailID = "mdrouson.sarwar@wattswater.com";
		}
		
		for (Role role : currentUserRoles) 
		{
			if("PM".equalsIgnoreCase(role.getName()) || ("" + currentUser.getCompany().getName() + "_admin").equalsIgnoreCase(role.getName()))
			{
				allowUpdate = true;
			}
		}
		int rowNum = 0;
		
		try
		{
			if(allowUpdate)
			{
				strDocPath = document.getRealPath();
				oInputStream = new FileInputStream(new File(strDocPath));
				wb = new XSSFWorkbook(oInputStream);
				XSSFSheet sheet = wb.getSheetAt(0);
				
				serviceCatalog.startBatchProcessing();
				//For Each Row
				for (Row row : sheet) 
				{
					wattsLogger.logInfo("TEMPLATE MASS IMPORT: Reading excel row number:: "+rowNum);
					eachRowDataMap = new LinkedHashMap<String, String>();
					
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
							headerMap.put(cellNum, cellValue);
							
						}else
						{
							String column_Name = headerMap.get(cellColumnIndex);
							String column_Val = cellValue;
							
							//null or blank value check
							if(null != column_Val && !"".equals(column_Val))
							{
								eachRowDataMap.put(column_Name, column_Val);
							}
						}
						cellNum++;
					}
					
					if(rowNum !=0)
					{
						
						CollaborationItem collabItem = null;
						Item catalogItem = null;
						
						String partNumber = null;
						String systemOfRecord = null;
						
						String itemPk = null;
						
						partNumber = eachRowDataMap.get("Part Number");
						systemOfRecord = eachRowDataMap.get("SystemOfRecord");
						wattsLogger.logInfo("------ID from Excel-------"+partNumber+" "+systemOfRecord);
						//Check if item is in collabArea
						
						if(null != partNumber && null != systemOfRecord)
						{
							wattsLogger.logInfo("------1----------");
							
							collabItem = getCollabItemFromQuery(pimCtx,wattsLogger,partNumber,systemOfRecord,itemCollabAreaObj);
							
							wattsLogger.logInfo("------2----------");
							
							catalogItem = getCatalogItemFromQuery(pimCtx,wattsLogger,partNumber,systemOfRecord,serviceCatalog);
							
							boolean presentInCatalog = false;
							boolean presentInWorkflow = false;
							if(null != collabItem)
							{
								presentInWorkflow = true;
								wattsLogger.logInfo("WORKFLOW ITEM");
								itemPk = collabItem.getPrimaryKey();
								
								String templateSpecName = "";
								if(null != eachRowDataMap.get("Template") && !"".equals(eachRowDataMap.get("Template")))
								{
									String templateStr = eachRowDataMap.get("Template");
									if(templateStr.contains("."))
									{
										templateSpecName = templateStr.substring(templateStr.indexOf(".")+1).trim() + " Specific Attributes";
										if(templateSpecName.contains(","))
										{
											templateSpecName = templateSpecName.replace(",", "");
										}
									}
									
									if(!"".equals(templateSpecName))
									{
										wattsLogger.logInfo("WORKFLOW ITEM: Template specified is :: ["+templateStr+":"+templateSpecName+"]");
										checkTemplateMapping(pimCtx,wattsLogger,collabItem,eachRowDataMap,templateSpecName);
											
										if(null != eachRowDataMap && eachRowDataMap.size() >0)
										{
											wattsLogger.logInfo("WORKFLOW ITEM:Updated eachRowDataMap size ::"+eachRowDataMap.size());
											/*for (Map.Entry<String, String> entry : eachRowDataMap.entrySet()) 
											{
												wattsLogger.logInfo(entry.getKey()+":"+columnPathToBeUpdated.get(entry.getKey())+":"+entry.getValue());
											}*/
											wattsLogger.logInfo("---WORKFLOW ITEM:Before Updating Item::["+itemPk+"]");
											updateCollabItem(pimCtx,wattsLogger,collabItem,eachRowDataMap,templateSpecName);
											wattsLogger.logInfo("---WORKFLOW ITEM:After Updating Item::["+itemPk+"]");
												
											//Now save the collab Item
											wattsLogger.logInfo("---WORKFLOW ITEM:Before Saving Item::["+itemPk+"]");
											saveCollabItemWithLogs(pimCtx,wattsLogger,collabItem,partNumber,systemOfRecord);
											wattsLogger.logInfo("---WORKFLOW ITEM:After Saving Item::");
											
										}else
										{
											wattsLogger.logInfo("WORKFLOW ITEM:No Attributes found to be updated");
											sb.append(partNumber+"|"+systemOfRecord+"|"+"No Attributes found to be updated");
											sb.append("\r\n");
										}
									}else
									{
										wattsLogger.logInfo("TEMPLATE MASS IMPORT: Template Column Value is not in correct format :"+templateStr);
										sb.append(partNumber+"|"+systemOfRecord+"|"+"Template Column Value is not in correct format :"+templateStr);
										sb.append("\r\n");
									}
								}else
								{
									wattsLogger.logInfo("TEMPLATE MASS IMPORT: Template is not specified in the excel row");
									sb.append(partNumber+"|"+systemOfRecord+"|"+"Template is not specified in the excel row");
									sb.append("\r\n");
								}
							}
							if (null != catalogItem)
							{
								presentInCatalog = true;
								wattsLogger.logInfo("CATALOG ITEM");
								itemPk = catalogItem.getPrimaryKey();
								
								String templateSpecName = "";
								if(null != eachRowDataMap.get("Template") && !"".equals(eachRowDataMap.get("Template")))
								{
									String templateStr = eachRowDataMap.get("Template");
									if(templateStr.contains("."))
									{
										templateSpecName = templateStr.substring(templateStr.indexOf(".")+1).trim() + " Specific Attributes";
										
										if(templateSpecName.contains(","))
										{
											templateSpecName = templateSpecName.replace(",", "");
										}
									}
									
									if(!"".equals(templateSpecName))
									{
										wattsLogger.logInfo("CATALOG ITEM: Template specified is :: ["+templateStr+":"+templateSpecName+"]");
										checkTemplateMappingForCatalogItem(pimCtx,wattsLogger,catalogItem,eachRowDataMap,templateSpecName);
											
										if(null != eachRowDataMap && eachRowDataMap.size() >0)
										{
											wattsLogger.logInfo("CATALOG ITEM:Updated eachRowDataMap size ::"+eachRowDataMap.size());
											/*for (Map.Entry<String, String> entry : eachRowDataMap.entrySet()) 
											{
												wattsLogger.logInfo(entry.getKey()+":"+columnPathToBeUpdated.get(entry.getKey())+":"+entry.getValue());
											}*/
											wattsLogger.logInfo("---CATALOG ITEM:Before Updating Item::["+itemPk+"]");
											updateCatalogItem(pimCtx,wattsLogger,catalogItem,eachRowDataMap,templateSpecName);
											wattsLogger.logInfo("---CATALOG ITEM:After Updating Item::["+itemPk+"]");
												
											//Now save the Catalog Item
											wattsLogger.logInfo("---CATALOG ITEM:Before Saving Item::["+itemPk+"]");
											saveCatalogItemWithLogs(pimCtx,wattsLogger,catalogItem,partNumber,systemOfRecord);
											wattsLogger.logInfo("---CATALOG ITEM:After Saving Item::");
										}else
										{
											wattsLogger.logInfo("CATALOG ITEM:No Attributes found to be updated");
											sb.append(partNumber+"|"+systemOfRecord+"|"+"No Attributes found to be updated");
											sb.append("\r\n");
										}
									}else
									{
										wattsLogger.logInfo("TEMPLATE MASS IMPORT: Template Column is not in correct format :"+templateStr);
										sb.append(partNumber+"|"+systemOfRecord+"|"+"Template Column Value is not in correct format :"+templateStr);
										sb.append("\r\n");
									}
								}else
								{
									wattsLogger.logInfo("TEMPLATE MASS IMPORT: Template is not specified in the excel row");
									sb.append(partNumber+"|"+systemOfRecord+"|"+"Template is not specified in the excel row");
									sb.append("\r\n");
								}
							}
							
							if(!presentInCatalog && !presentInWorkflow)
							{
								wattsLogger.logInfo("ERROR: could not find product to update in Catalog or Workflow.");
								sb.append(partNumber+"|"+systemOfRecord+"|"+"Could not find Part to update in Catalog or Workflow");
								sb.append("\r\n");
								
							}
						}else
						{
							wattsLogger.logInfo("Part Number or System Of Record is null or blank in the excel row ");
							sb.append("|"+"|"+"Part Number or System Of Record is null or blank in the excel row");
							sb.append("\r\n");
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
	            	out.println("Exception: ["+e.getMessage()+"]");
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
			}else
			{
				out.println("The User does not have neccessary priviledges to run this Import");
			}
		}catch (Exception e) 
		{
			out.println("Exception: ["+e.getMessage()+"]");
			e.printStackTrace();
			wattsLogger.logInfo("--:TEMPLATE MASS IMPORT:HELPER:doReadExcelDoc--Exception--:"+e.getMessage());
		}finally
		{
			new CommonHelper().saveToRealPath(pimCtx, email_attach_File_Path, sb, wattsLogger);
			
			/*if(null != valueMap)
			{
				valueMap.put("TO_ADDRESS",userEmailID);
				valueMap.put("FROM_ADDRESS",fromAddress);
				valueMap.put("MAIL_BODY",mailBody);
				valueMap.put("MAIN_SUBJECT",mailSubject);
				valueMap.put("DOCSTORE_FILE_PATH",email_attach_File_Path);
				valueMap.put("FILE_NAME",email_attachFile_Name);
				valueMap.put("MAIL_STATUS","N");
				
			}*/
			
			new SendEmailNew().sendEmailToUser(userEmailID, fromAddress, mailBody, mailSubject, email_attach_File_Path, email_attachFile_Name);
			
			//new CommonHelper().updateCustomDataBaseTable(pimCtx,wattsLogger,tableNameWithSchema,valueMap);
			
			
		}
		wattsLogger.logInfo("TEMPLATE MASS IMPORT: MassImportTemplateHelper.readDocAndUpdateItems()    END------");
		
	}
	
	private Item getCatalogItemFromQuery(Context pimCtx, Logger wattsLogger,String partNumber, String systemOfRecord, Catalog serviceCatalog) 
	{
		Item resultObj = null;
		String queryStr = null;
		SearchQuery queryObj = null;
		SearchResultSet resultSet = null;
		
		try
		{
			queryStr = "select item from catalog('"+serviceCatalog.getName()+"') where item['"+WattsConstants.UNIQUE_QUALI_PATH+"'] = '"+partNumber+" "+systemOfRecord+"'" ;
			queryObj = pimCtx.createSearchQuery(queryStr);
			resultSet = queryObj.execute();
			
			if(resultSet.next())
			{
				resultObj = resultSet.getItem(1);
			}
			
		}catch(Exception e)
		{
			sb.append(partNumber+"|"+systemOfRecord+"|"+e.getMessage());
			sb.append("\r\n");
			wattsLogger.logInfo("getCatalogItemFromQuery(): Exception :: ["+e.getMessage()+"]");
			return null;
		}
		
		return resultObj;
	}

	private CollaborationItem getCollabItemFromQuery(Context pimCtx,Logger wattsLogger, String partNumber, String systemOfRecord,ItemCollaborationArea itemCollabAreaObj) 
	{
		Item itemObj = null;
		CollaborationItem resultObj = null;
		String queryStr = null;
		SearchQuery queryObj = null;
		SearchResultSet resultSet = null;
		String itemPk = null;
		
		try
		{
			queryStr = "select item from collaboration_area('"+itemCollabAreaObj.getName()+"') where item['"+WattsConstants.UNIQUE_QUALI_PATH+"'] = '"+partNumber+" "+systemOfRecord+"'" ;
			queryObj = pimCtx.createSearchQuery(queryStr);
			resultSet = queryObj.execute();
			
			if(resultSet.next())
			{
				itemObj = resultSet.getItem(1);
				try
				{
					itemPk = itemObj.getPrimaryKey();
					resultObj = itemCollabAreaObj.getCheckedOutItem(itemPk);
				}catch(Exception e)
				{
					sb.append(partNumber+"|"+systemOfRecord+"|"+e.getMessage());
					sb.append("\r\n");
					return null;
				}
				
			}
			
		}catch(Exception e)
		{
			sb.append(partNumber+"|"+systemOfRecord+"|"+e.getMessage());
			sb.append("\r\n");
			wattsLogger.logInfo("getCollabItemFromQuery(): Exception :: ["+e.getMessage()+"]");
			return null;
		}
		
		return resultObj;
	}

	private void updateCatalogItem(Context pimCtx, Logger wattsLogger,Item catalogItem, HashMap<String, String> eachRowDataMap, String templateSpecName) 
	{
		String strColumnName = null;
		String strNewValue = null;
		
		for (Map.Entry<String, String> entry: eachRowDataMap.entrySet()) 
		{
			strColumnName = entry.getKey();
			strNewValue = entry.getValue();
			
			if(!"Template".equalsIgnoreCase(strColumnName) && !"Part Number".equalsIgnoreCase(strColumnName) && !"SystemOfRecord".equalsIgnoreCase(strColumnName))
			{
				if(null != strNewValue && !"".equals(strNewValue))
				{
					if("__BLANK__".equalsIgnoreCase(strNewValue))
					{
						strNewValue = null;
					}
					wattsLogger.logInfo("Before Updating ["+templateSpecName+"/"+strColumnName+"] with Value ["+strNewValue+"] ");
					String resolvedAttrValue = strNewValue;
					if(null != strColumnName)
					{
						try
						{
								AttributeInstance attrInstance = catalogItem.getAttributeInstance(templateSpecName+"/"+strColumnName);
								AttributeDefinition attrDef = attrInstance.getAttributeDefinition();
								AttributeDefinition.Type attrDefType = attrDef.getType();
								resolvedAttrValue = getResolvedValue(attrDefType,strNewValue);
								wattsLogger.logInfo("Single resolved Value:: ["+resolvedAttrValue+"]");
								catalogItem.setAttributeValue(templateSpecName+"/"+strColumnName, resolvedAttrValue);
						}catch(Exception e)
						{
							wattsLogger.logInfo("updateCatalogItem() exception :: ["+e.getMessage()+"]");
						}
					}
					
				}
					
			}
			
		}
	
	
	}

	private void updateCollabItem(Context pimCtx, Logger wattsLogger,CollaborationItem collabItem, HashMap<String, String> eachRowDataMap, String templateSpecName) 
	{
		String strColumnName = null;
		String strNewValue = null;
		
		for (Map.Entry<String, String> entry: eachRowDataMap.entrySet()) 
		{
			strColumnName = entry.getKey();
			strNewValue = entry.getValue();
			
			if(!"Template".equalsIgnoreCase(strColumnName) && !"Part Number".equalsIgnoreCase(strColumnName) && !"SystemOfRecord".equalsIgnoreCase(strColumnName))
			{
				if(null != strNewValue && !"".equals(strNewValue))
				{
					if("__BLANK__".equalsIgnoreCase(strNewValue))
					{
						strNewValue = null;
					}
					wattsLogger.logInfo("Before Updating ["+templateSpecName+"/"+strColumnName+"] with Value ["+strNewValue+"] ");
					String resolvedAttrValue = strNewValue;
					if(null != strColumnName)
					{
						try
						{
							AttributeInstance attrInstance = collabItem.getAttributeInstance(templateSpecName+"/"+strColumnName);
							AttributeDefinition attrDef = attrInstance.getAttributeDefinition();
							AttributeDefinition.Type attrDefType = attrDef.getType();
							resolvedAttrValue = getResolvedValue(attrDefType,strNewValue);
							wattsLogger.logInfo("Single resolved Value:: ["+resolvedAttrValue+"]");
							collabItem.setAttributeValue(templateSpecName+"/"+strColumnName, resolvedAttrValue);
							
						}catch(Exception e)
						{
							wattsLogger.logInfo("updateCollabItem() exception :: ["+e.getMessage()+"]");
						}
					}
					
				}
					
			}
			
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

	private void saveCatalogItemWithLogs(Context pimCtx, Logger wattsLogger,Item catalogItem, String partNumber, String systemOfRecord) 
	{
		ExtendedValidationErrors errs = null;
		try
		{
			catalogItem.getCatalog().getProcessingOptions().setPostSaveScriptProcessing(false);
			catalogItem.getCatalog().getProcessingOptions().setPostScriptProcessing(false);
			catalogItem.getCatalog().getProcessingOptions().setCollaborationAreaLocksValidationProcessing(false);
			errs = catalogItem.save();
			if(null != errs)
			{
				List<ValidationError> errorList=errs.getErrors();
				wattsLogger.logInfo("Errors during save of Catalog Item....");
				for (ValidationError validationError : errorList) 
				{
					sb.append(partNumber+"|"+systemOfRecord+"|"+"validationError.getMessage() for attribute :["+validationError.getAttributeInstance().getPath()+"]");
					sb.append("\r\n");
					wattsLogger.logInfo(validationError.getMessage());
					wattsLogger.logInfo("Attribute: ["+validationError.getAttributeInstance().getPath()+"]");
					wattsLogger.logInfo("Attribute Value Before Save: ["+validationError.getAttributeInstance().getValue()+"]");
				}
			}else
			{
				sb.append(partNumber+"|"+systemOfRecord+"|"+"Part saved in catalog successfully.");
				sb.append("\r\n");
			}
			
		}catch (Exception e) 
		{
			sb.append(partNumber+"|"+systemOfRecord+"|"+"Exception during save: ["+e.getMessage()+"]");
			sb.append("\r\n");
			wattsLogger.logInfo("Exception during save: ["+e.getMessage()+"]");
			e.printStackTrace();
		}
		
	}

	private void saveCollabItemWithLogs(Context pimCtx, Logger wattsLogger,	CollaborationItem collabItem, String partNumber, String systemOfRecord) 
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
				wattsLogger.logInfo("Errors during save of collabItem....");
				for (ValidationError validationError : errorList) 
				{
					sb.append(partNumber+"|"+systemOfRecord+"|"+validationError.getMessage()+"for attribute :["+validationError.getAttributeInstance().getPath()+"]");
					sb.append("\r\n");
					wattsLogger.logInfo(validationError.getMessage());
					wattsLogger.logInfo("Attribute: ["+validationError.getAttributeInstance().getPath()+"]");
					wattsLogger.logInfo("Attribute Value Before Save: ["+validationError.getAttributeInstance().getValue()+"]");
				}
			}else
			{
				sb.append(partNumber+"|"+systemOfRecord+"|"+"Part saved in workflow successfully.");
				sb.append("\r\n");
			}
			
		}catch (Exception e) 
		{
			sb.append(partNumber+"|"+systemOfRecord+"|"+"Exception during saving item: ["+e.getMessage()+"]");
			sb.append("\r\n");
			wattsLogger.logInfo("Exception during save: ["+e.getMessage()+"]");
			e.printStackTrace();
		}
		
	}
	
	private void checkTemplateMappingForCatalogItem(Context pimCtx,	Logger wattsLogger, Item catalogItem,HashMap<String, String> eachRowDataMap, String templateSpecName) 
	{
		List<String> keysToRemove = new ArrayList<String>();
		Hierarchy templateHierarchyObj = pimCtx.getHierarchyManager().getHierarchy(WattsConstants.TEMPLATES_HIERARCHY_NAME);
		
		for (Map.Entry<String, String> entry : eachRowDataMap.entrySet()) 
		{
			String attr_Name = entry.getKey();
			String attr_Val = entry.getValue();
			
			//wattsLogger.logInfo("Checking for Attribute :["+attr_Name+"]");
			
			if(!"Template".equalsIgnoreCase(attr_Name) && !"Part Number".equalsIgnoreCase(attr_Name) && !"SystemOfRecord".equalsIgnoreCase(attr_Name))
			{
				
				if(null != attr_Val && !"".equals(attr_Val))
				{
					Collection<Category> categoryList = catalogItem.getCategories();
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
									if(templateSpecName.equalsIgnoreCase(specName))
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
						
						SecondarySpec secSpec =  ((null != pimCtx.getSpecManager().getSpec(templateSpecName))?(SecondarySpec)pimCtx.getSpecManager().getSpec(templateSpecName):null);
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
										catalogItem.mapToCategory(category);
										catalogItem.getCatalog().getProcessingOptions().setPostSaveScriptProcessing(false);
										catalogItem.getCatalog().getProcessingOptions().setPostScriptProcessing(false);
										catalogItem.save();
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
						wattsLogger.logInfo("Update Allowed - As Item is already mapped to this template hierarchy");
					}else
					{
						wattsLogger.logInfo("Update not allowed - Item is already mapped to another template hierarchy");
					}
					
				}
			}		
		}
			
	}

	

	private void checkTemplateMapping(Context pimCtx,Logger wattsLogger, CollaborationItem collabItem,HashMap<String, String> eachRowDataMap, String templateSpecName) 
	{
		
		Hierarchy templateHierarchyObj = pimCtx.getHierarchyManager().getHierarchy(WattsConstants.TEMPLATES_HIERARCHY_NAME);
		
		for (Map.Entry<String, String> entry : eachRowDataMap.entrySet()) 
		{
			String attr_Name = entry.getKey();
			String attr_Val = entry.getValue();
			
			//wattsLogger.logInfo("Checking for Attribute :["+attr_Name+"]");
			if(!"Template".equalsIgnoreCase(attr_Name) && !"Part Number".equalsIgnoreCase(attr_Name) && !"SystemOfRecord".equalsIgnoreCase(attr_Name))
			{
			
				if(null != attr_Val && !"".equals(attr_Val))
				{
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
									if(templateSpecName.equalsIgnoreCase(specName))
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
						
						SecondarySpec secSpec =  ((null != pimCtx.getSpecManager().getSpec(templateSpecName))?(SecondarySpec)pimCtx.getSpecManager().getSpec(templateSpecName):null);
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
						wattsLogger.logInfo("Update Allowed - As Item is already mapped to this template hierarchy");
					}else
					{
						wattsLogger.logInfo("Update not allowed - Item is already mapped to another template hierarchy");
					}
					
				}
				
			}
		}
		
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
