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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeCollection;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.attribute.AttributeDefinition.Type;
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
import com.ibm.pim.context.Context;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.organization.Performer;
import com.ibm.pim.organization.Role;
import com.ibm.pim.organization.User;
import com.ibm.pim.spec.SecondarySpec;
import com.ibm.pim.utils.Logger;
import com.ibm.pim.workflow.WorkflowStep;
import com.watts.phaseII.constants.WattsConstants;

public class AttrDataTypeImportHelper 
{

	public void doExecuteImport(Context pimCtx, Logger wattsLogger,Catalog serviceCatalog, Document document, String docStoreFileName) 
	{
		readFileAndUpdateItems(pimCtx,wattsLogger,serviceCatalog,document);
	}
	
	private void readFileAndUpdateItems(Context pimCtx, Logger wattsLogger,Catalog serviceCatalog, Document document) 
	{
		wattsLogger.logInfo("Data Type Change Import: AttrDataTypeImportHelper.readFileAndUpdateItems()    START------");
		
		HashMap<Integer,String> headerMap = new LinkedHashMap<Integer,String>();
		HashMap<String,String> eachRowDataMap = null;
		
		String strDocPath = null;
		InputStream oInputStream = null;
		XSSFRow oXSSFRow = null;;
		XSSFCell oXSSFCell = null;
		Iterator<Cell> iteCell = null;
		XSSFWorkbook wb = null;
		
		int rowNum = 0;
		
		try
		{
			strDocPath = document.getRealPath();
			oInputStream = new FileInputStream(new File(strDocPath));
			wb = new XSSFWorkbook(oInputStream);
			XSSFSheet sheet = wb.getSheetAt(0);
			
			//serviceCatalog.startBatchProcessing();
			//For Each Row
			for (Row row : sheet) 
			{
				wattsLogger.logInfo("Data Type Change Import: Reading excel row number::  ["+rowNum+"]");
				
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
						//Header row
						headerMap.put(cellNum, cellValue);
						
					}else
					{
						String column_Name = headerMap.get(cellColumnIndex);
						String column_Val = cellValue;
						
						boolean columnValuePasses = (null != column_Val && !"".equals(column_Val))? true:false;
						if(columnValuePasses)
						{	
							eachRowDataMap.put(column_Name, column_Val);
						}
							
					}
					cellNum++;
				}
				
				//For testing iterate the maps data and error
				
				if(headerMap.size()>0 && rowNum == 0)
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
				}
				
				
				if(rowNum !=0)
				{
					
					Item catalogItem;
					String excelPK = null;
					String itemPk = null;
					String systemOfRecord = null;
					String partNumber = null;
					String UPQ = null;
					
					excelPK = eachRowDataMap.get("PIM_ID");
					wattsLogger.logInfo("------1--------------"+excelPK);
					//Check if item in Catalog
					if(null != excelPK)
					{
						if (excelPK.contains("."))
						{
							excelPK = excelPK.substring(0, excelPK.indexOf("."));
						}
						catalogItem = serviceCatalog.getItemByPrimaryKey(excelPK);
						
						if(null != catalogItem)
						{
							
							itemPk = catalogItem.getPrimaryKey();
							systemOfRecord =  (null != catalogItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH)?catalogItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH).toString():"");
							partNumber = (null != catalogItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?catalogItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"");
							UPQ = (null != catalogItem.getAttributeValue(WattsConstants.UNIQUE_QUALI_PATH)?catalogItem.getAttributeValue(WattsConstants.UNIQUE_QUALI_PATH).toString():"");
							
							if(true)
							{
								wattsLogger.logInfo("--Within the Update Condition--");
								
								if(eachRowDataMap.size()>0)
								{
									wattsLogger.logInfo("---Before Updating Item::["+itemPk+"]");
									updateItem(pimCtx,wattsLogger,catalogItem,eachRowDataMap);
									wattsLogger.logInfo("---After Updating Item::["+itemPk+"]");
									
									//Now save the collab Item
									wattsLogger.logInfo("---Before Saving Item::["+itemPk+"]");
									savecatalogItem(pimCtx,wattsLogger,catalogItem);
									wattsLogger.logInfo("---After Saving Item::");
								}else
								{
									wattsLogger.logInfo("No Attribute to Update");
								}
								
							}
							
							
						}else
						{
							wattsLogger.logInfo("Could not find item in Catalog ");
						}
					}else
					{
						wattsLogger.logInfo("Item PK is null or blank in the excel row ");
					}
					
				}
				
				rowNum++;
			}
			
			try
            {
                //List<ExtendedValidationErrors> errs=serviceCatalog.flushBatch();
                // Process the failed items as identified from errs.
                // Perform any additional work for succeeded items.
				wattsLogger.logInfo("Before Committing the context");
                //pimCtx.commit(); 
                wattsLogger.logInfo("Context Committed");
            }
            catch(Exception e)
            {
            	pimCtx.rollback();
                if (isCriticalException(e) )
                    throwRuntime(e);
                // Process all items in the batch as failed
                wattsLogger.logInfo("Update Failed. Context Rollback.");
            }
            /*finally
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
            serviceCatalog.stopBatchProcessing();*/
            
		}catch (Exception e) 
		{
			e.printStackTrace();
			wattsLogger.logInfo("--:Data Type Change Import:HELPER:doReadExcelDoc--Exception--:"+e.getMessage());
		}
		wattsLogger.logInfo("Data Type Change Import: AttrDataTypeImportHelper.readFileAndUpdateItems()    END------");
		
	
	}
	
	private void savecatalogItem(Context pimCtx, Logger wattsLogger,Item catalogItem) 
	{
		ExtendedValidationErrors errs = null;
		try
		{
			catalogItem.getCatalog().getProcessingOptions().setPostSaveScriptProcessing(false);
			catalogItem.getCatalog().getProcessingOptions().setPostScriptProcessing(false);
			errs = catalogItem.save();
			if(null != errs)
			{
				List<ValidationError> errorList=errs.getErrors();
				wattsLogger.logInfo("Errors during save....");
				for (ValidationError validationError : errorList) 
				{
					wattsLogger.logInfo(validationError.getMessage());
					wattsLogger.logInfo("Attribute:"+validationError.getAttributeInstance().getPath());
					wattsLogger.logInfo("Attribute Value Before Save:"+validationError.getAttributeInstance().getValue());
					
				}
			}
			
		}catch (Exception e) 
		{
			wattsLogger.logInfo("Exception during save:"+e.getMessage());
			e.printStackTrace();
		}
		
	}

	private void updateItem(Context pimCtx, Logger wattsLogger,Item catalogItem, HashMap<String, String> eachRowDataMap) 
	{

		
		String strColumnName = null;
		String strOldValue = null;
		String strNewValue = null;
		
		for (Map.Entry<String, String> entry: eachRowDataMap.entrySet()) 
		{
			strColumnName = entry.getKey();
			strNewValue = entry.getValue();
			
			String sNewValueWithoutDotZero = (null !=strNewValue)?strNewValue:null;
			
			if("PIM_ID".equalsIgnoreCase(strColumnName) || "Part Number".equalsIgnoreCase(strColumnName))
			{
				if(null != strNewValue && strNewValue.contains("."))
				{
					sNewValueWithoutDotZero = strNewValue.substring(0, strNewValue.indexOf("."));
				}
				
			}else
			{
				if(null != sNewValueWithoutDotZero && !"".equals(sNewValueWithoutDotZero))
				{
					
					String tempColumnName = strColumnName;
					String level = "";
					
					if(strColumnName.contains(":"))
					{
						int startIndex = strColumnName.indexOf(":");
						level = strColumnName.substring(0, startIndex);
						tempColumnName = strColumnName.substring(startIndex+1, strColumnName.length());
						
					}
					
					if("EA".equalsIgnoreCase(level) || "".equals(level))
					{
						wattsLogger.logInfo("Before Updating ["+strColumnName+"] with Value ["+sNewValueWithoutDotZero+"] ");
						updateSingleOccurringAttributes(strColumnName,sNewValueWithoutDotZero,catalogItem,wattsLogger);
					}else
					{
						wattsLogger.logInfo("Before Updating ["+strColumnName+"] with Value ["+sNewValueWithoutDotZero+"] ");
						updateMultioccurringAttributes(strColumnName,tempColumnName,sNewValueWithoutDotZero,catalogItem,level,wattsLogger);
					}
				}
					
			}
			
			
		}
	
	
	}
	private void updateSingleOccurringAttributes(String strColumnName,String strNewValue, Item catalogItem, Logger wattsLogger) 
	{
		String resolvedAttrValue = strNewValue;
		String attrPath = WattsConstants.headerNameToAttrPathMap.get(strColumnName);
		if(null != attrPath)
		{
			AttributeInstance attrInstance = catalogItem.getAttributeInstance(attrPath);
			AttributeDefinition attrDef = attrInstance.getAttributeDefinition();
			AttributeDefinition.Type attrDefType = attrDef.getType();
			resolvedAttrValue = getResolvedValue(attrDefType,strNewValue);
			wattsLogger.logInfo("Single resolved Value::"+resolvedAttrValue);
			catalogItem.setAttributeValue(attrPath, resolvedAttrValue);
			
			
		}
		
	}
	
	private void updateMultioccurringAttributes(String strColumnName,String tempColumnName, String strNewValue,Item catalogItem, String level, Logger wattsLogger) 
	{

		String resolvedAttrValue = strNewValue;
		String attrPath = WattsConstants.headerNameToAttrPathMap.get(strColumnName);
		if(null != attrPath)
		{
			if(null != strNewValue && !"".equals(strNewValue))
			{
				AttributeInstance attrInstancePaths = catalogItem.getAttributeInstance(WattsConstants.HIGHER_PACKAGING_PATHS);
				List<? extends AttributeInstance> childInstList = attrInstancePaths.getChildren();
				if(null !=childInstList && childInstList.size() > 0)
				{
					AttributeInstance attrInstHPackLevel = catalogItem.getAttributeInstance(WattsConstants.HIGHER_PACKAGING_PATHS + "#0/Higher Packaging Levels");
					List<? extends AttributeInstance> hPackchildInstList =attrInstHPackLevel.getChildren();
					if(null !=hPackchildInstList && hPackchildInstList.size() > 0)
					{
						int noOfPacks = hPackchildInstList.size();
						for(int i = 0; i< noOfPacks ; i++)
						{
							String attr_Path_Prefix = WattsConstants.HIGHER_PACKAGING_PATHS + "#0/Higher Packaging Levels#"+i+"/";
							String packIdentifier = (null != catalogItem.getAttributeValue(attr_Path_Prefix+"Unit of Measure"))?catalogItem.getAttributeValue(attr_Path_Prefix+"Unit of Measure").toString():"";
							if(level.equalsIgnoreCase(packIdentifier))
							{
								String attr_Path_Temp = attr_Path_Prefix+tempColumnName;
								wattsLogger.logInfo("attr_Path_Temp :["+attr_Path_Temp+"]");
								AttributeInstance attrInstance = catalogItem.getAttributeInstance(attr_Path_Temp);
								AttributeDefinition attrDef = attrInstance.getAttributeDefinition();
								AttributeDefinition.Type attrDefType = attrDef.getType();
								resolvedAttrValue = getResolvedValue(attrDefType,strNewValue);
								wattsLogger.logInfo("Multioccuring resolved Value::"+resolvedAttrValue);
								
								catalogItem.setAttributeValue(attr_Path_Temp, resolvedAttrValue);
								
							}
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
