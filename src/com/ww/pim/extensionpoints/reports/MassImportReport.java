//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.extensionpoints.reports.MassImportReport.class"

package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.organization.User;
import com.ibm.pim.spec.Spec;
import com.ww.pim.common.utils.Constant;

public class MassImportReport implements ReportGenerateFunction
{
	private Context ctx = PIMContextFactory.getCurrentContext();
	private static final String FEED_FILE_PATH = System.getProperty(Constant.DOLLAR_TOP)+"/feedfiles/Mass Update Import/Test";
	private static final String BACKUP_PATH = System.getProperty(Constant.DOLLAR_TOP)+"/feedfiles/Mass Update Import/BackUp";
	private FileInputStream excelFileStream = null;
	public Catalog objCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
	public Spec objSpec = ctx.getSpecManager().getSpec("Product Catalog Global Spec");
	public String specName = objSpec.getName();
	private String logFileName = System.getProperty(Constant.DOLLAR_TOP)+"/logs/mass_import_test_log.txt";
	private PrintWriter objLogger = null;
	private Map<String,String> headerMap = null;
	boolean pimIDFlag;
	boolean partNoFlag;
	boolean sysOfRecFlag;
	
	//Final item object
	Item itemObj = null;
	
	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) 
	{
		try 
		{
			User user = ctx.getCurrentUser();
			
			
			
			
			File logFile = new File(logFileName);
			objLogger = new PrintWriter(new FileWriter(logFile, true));
			objLogger.println("****************Mass Import report started*****************");
			if(!user.getName().equals("Admin"))
			{
				objLogger.println("Only Admin can import...");
				return;
			}
			else
			{
				objLogger.println("Current user is Admin : Importing..");
			}
			//Reading Excel file
        	File folder = new File(FEED_FILE_PATH);
        	File[] listOfFiles = folder.listFiles();
        	List<String> listOfFileName = new ArrayList<String>();
        	for(File ipFile : listOfFiles)
        	{
        		String fileName = ipFile.getPath();
        		listOfFileName.add(fileName);
        	}
        	if(listOfFileName.size()>0)
        	{
        		for(String fileName : listOfFileName)
        		{
        			if(!fileName.endsWith(".xlsx"))
        			{
        				objLogger.println("Invalid Format for file "+fileName);
                    	fileMove(fileName,BACKUP_PATH);
                    	continue;
        			}
        			//Flag for checking header format
        			pimIDFlag = false;
            		partNoFlag = false;
            		sysOfRecFlag = false;
            		
            		//Processing feed file
        			File excelFile = new File(fileName);
        			excelFileStream = new FileInputStream(excelFile);
        			objLogger.println("Excel file name is : "+ excelFile.getName());
                    Workbook workbook = new XSSFWorkbook(excelFileStream);
                    Sheet dataSheet = workbook.getSheet("Sheet1");
                    
                    //Retrieving Header
                    Row row = dataSheet.getRow(0);
                    Cell cell = null;
                    objLogger.println("*****Retrieving Header *****");
                    objLogger.println("File is : "+fileName);
                    parseHeader(row);
                    
                    if(pimIDFlag==false || sysOfRecFlag==false || partNoFlag == false)
                    {
                    	objLogger.println("Wrong Header Format for file "+fileName);
                    	fileMove(fileName,BACKUP_PATH);
                    	continue;
                    }
                	
                	int rowCount = dataSheet.getPhysicalNumberOfRows();
                	objLogger.println("Row count in import file is : "+rowCount);
                	for(int rowNum = 1 ; rowNum < rowCount;rowNum++)
    	            {
                		String typeVal = null;
            			String assocPartsVal = null;
            			
            			//Attribute Instances for Association
            			AttributeInstance associationAttribInst = null;
            			AttributeInstance associatedPartsAttribInst = null;
            			AttributeInstance associationChildAttribInst = null;
            			AttributeInstance associatedPartsChildAttribInst = null;
            			
            			//NoPartPresentFlag
            			boolean noPartPresentFlag = false;
            			
                		objLogger.println("Retrieving data for rowNum : "+rowNum);
    	            	row = dataSheet.getRow(rowNum);
    	            	String PIM_id = "";
    	            	String partNo = "";
    	            	String sysOfRecord = "";
    	            	
    	            	
    	            	int headerSize = headerMap.size();
    	            	objLogger.println("Size of the header is : "+headerSize);
    	            	for(int j = 0; j<headerSize;j++)
    	            	{
    	            		String headerName = headerMap.get(String.valueOf(j));
    	            		if(headerName.equals("PIM_ID"))
    	            		{
    	            			objLogger.println("Column number is : "+j);
    	            			cell = row.getCell(j);
    	            			PIM_id = validateCellValue(cell,rowNum);
    	            			if(isNumeric(PIM_id))
    	            			{
    	            				double var = Double.valueOf(PIM_id);
        	            			int var2 = (int) var;
        	            			PIM_id = String.valueOf(var2);
    	            			}
    	            			objLogger.println("PIM ID is : "+PIM_id);
    	            		}
    	            		else if(headerName.equals("Part Number"))
    	            		{
    	            			objLogger.println("Column number is : "+j);
    	            			cell = row.getCell(j);
    	            			partNo = validateCellValue(cell,rowNum);
    	            			if(isNumeric(partNo))
    	            			{
    	            				double var = Double.valueOf(partNo);
        	            			int var2 = (int) var;
        	            			partNo = String.valueOf(var2);
    	            			}
    	            			objLogger.println("Part Number is : "+partNo);
    	            		}
    	            		else if(headerName.equals("SystemOfRecord"))
    	            		{
    	            			objLogger.println("Column number is : "+j);
    	            			cell = row.getCell(j);
    	            			sysOfRecord = validateCellValue(cell,rowNum);
    	            			objLogger.println("System of Record is : "+sysOfRecord);
    	            		}
    	            		if(!PIM_id.equals("")&&!partNo.equals("")&&!sysOfRecord.equals(""))
    	            		{
    	            			objLogger.println("Before exiting header loop :: Column number is : "+j);
    	            			break;
    	            		}
    	            		
    	            	}
    	            	if(PIM_id.equals(""))
                		{
                			objLogger.println("PIM_id is blank for row "+rowNum);
                			continue;
                		}
    	            	else if(partNo.equals(""))
    	            	{
    	            		objLogger.println("Part Number is blank for row "+rowNum);
    	            		continue;
    	            	}
    	            	else if(sysOfRecord.equals(""))
    	            	{
    	            		objLogger.println("System Of Record is blank for row "+rowNum);
    	            		continue;
    	            	}
    	            	
    	            	//Retrieving item
    	            	if(!PIM_id.equals(""))
    	            	{
    	            		itemObj = objCatalog.getItemByPrimaryKey(PIM_id);
    	            	}
    	            	
    	            	
    	            	
    	            	if(null!=itemObj)
    	            	{
    	            		for(int i = 0 ; i<headerSize;i++)
        		            {
        	            		String attribVal = "";
        		            	String attribHeaderName = headerMap.get(String.valueOf(i));
        		            	if(null!=attribHeaderName)
        		            	{
        		            		cell = row.getCell(i);
        		            		
        		            		//PIM_id,Part_No and System of record can't be changed through Import
        		            		if(attribHeaderName.equals("PIM_ID"))
        		            		{
        		            			continue;
        		            		}
        		            		else if(attribHeaderName.equals("Part Number"))
        		            		{
        		            			continue;
        		            		}
        		            		else if(attribHeaderName.equals("SystemOfRecord"))
        		            		{
        		            			continue;
        		            		}
        		            		else
        		            		{
        		            			attribVal = validateCellValue(cell,rowNum);
        		            			
        		            			
        		            			
        		            			//Saving Item
        		            			HashMap<String,String> packPathMap = getPackagingLevelPath(itemObj,PIM_id,specName);

    									String attribName = attribHeaderName;
    									String attribValue = attribVal;
    									
    									
    									String path = specName;
    									if(attribName.contains(":"))
    									{
    										objLogger.println("Contains : ");
    										String attrib [] = attribName.split(":");
    										String packagingType = attrib[0].trim();
    										attribName = attrib[1].trim();
    										
    										
    										if(packagingType.equals("EA"))
    										{
    											objLogger.println("Packaging level Data : EA");
    											path = path + "/Packaging Hierarchy/Each Level";
    										}
    										else if(packagingType.equals("MASTER"))
    										{
    											objLogger.println("Packaging level Data : MASTER");
    											if(packPathMap.get("HAS_MASTER").equals("N"))
    											{
    												objLogger.println("Item# "+PIM_id+" has no MASTER type Packaging level data");
    												continue;
    											}
    											path = packPathMap.get("MASTER_PATH");
    										}
    										else if(packagingType.equals("INNER"))
    										{
    											objLogger.println("Packaging level Data : INNER");
    											if(packPathMap.get("HAS_INNER").equals("N"))
    											{
    												objLogger.println("Item# "+PIM_id+" has no INNER type Packaging level data");
    												continue;
    											}
    											path = packPathMap.get("INNER_PATH");
    										}
    										else if(packagingType.equals("PALLET"))
    										{
    											objLogger.println("Packaging level Data : PALLET");
    											if(packPathMap.get("HAS_PALLET").equals("N"))
    											{
    												objLogger.println("Item# "+PIM_id+" has no PALLET type Packaging level data");
    												continue;
    											}
    											path = packPathMap.get("PALLET_PATH");
    										}
    										
    										itemObj.setAttributeValue(path+"/"+attribName, attribValue);
    									}
    									else if(attribName.equals("Type") || attribName.equals("AssociatedParts"))
    									{
    										if(attribName.equals("Type"))
    										{
    											objLogger.println("Inside Type");
    											
    											if(assocPartsVal!=null && (assocPartsVal.equals("")||noPartPresentFlag==false))
    											{
    												objLogger.println("Type can be set as Associated parts field is Blank...Skipping");
    												continue;
    											}
    											
    											typeVal = attribValue;
    											//When association attribute has not been populated..Hence populating association attributeInstance
    											if(null==associationAttribInst)
    											{
    												path = path + "/Associations";
    												associationAttribInst = itemObj.getAttributeInstance(path);
    												
    												objLogger.println("Path for Association is : "+associationAttribInst.getPath());
    												List<? extends AttributeInstance> associationAttribInstList = associationAttribInst.getChildren();
            										if(associationAttribInstList.size()>0)
            										{
            											for(AttributeInstance associationInst : associationAttribInstList)
            											{
            												associationChildAttribInst = associationInst;
            												objLogger.println("Path for association-->Type is : "+associationChildAttribInst.getPath().toString());
            												break;
            											}
            										}
    											}
    											if(attribValue.equals(""))
        										{
        											objLogger.println("Type cell in excel is also blank...Hence Deleting occurrance");
        											Iterator <? extends AttributeInstance>  attrbItr = associationAttribInst.getChildren().iterator();
        											while(attrbItr.hasNext())
        											{
        												attrbItr.next().removeOccurrence();
        											}
        											continue;
        										}
    											if(null==associationChildAttribInst)
    											{
    												associationChildAttribInst = associationAttribInst.addOccurrence();
    											}
        										path = associationChildAttribInst.getPath();
        										objLogger.println("Path for associationChildAttribInst is : "+associationChildAttribInst.getPath());
        										itemObj.setAttributeValue(path+"/"+attribName, attribValue);
        									}
    										else if(attribName.equals("AssociatedParts"))
    										{
    											

        										objLogger.println("Inside associated parts");
        										
        										if(typeVal!=null && typeVal.equals(""))
    											{
    												objLogger.println("No part can be set as type is Blank...Skipping");
    												continue;
    											}
        										
        										assocPartsVal = attribValue;
        										//When association attribute has not been populated..Hence populating association attributeInstance
        										if(null==associationAttribInst)
    											{
        											objLogger.println("associationAttribInst is null...creating");
    												path = path + "/Associations";
    												associationAttribInst = itemObj.getAttributeInstance(path);
    												objLogger.println("Path for Association is : "+associationAttribInst.getPath());
    												objLogger.println("itemObj.getAttributeInstance(path).getChildren().size()"+itemObj.getAttributeInstance(path).getChildren().size());
    												List<? extends AttributeInstance> associationAttribInstList = associationAttribInst.getChildren();
            										if(associationAttribInstList.size()>0)
            										{
            											for(AttributeInstance associationInst : associationAttribInstList)
            											{
            												associationChildAttribInst = associationInst;
            												objLogger.println("Path for association Child-->Associated Parts is : "+associationChildAttribInst.getPath().toString());
            												break;
            											}
            										}
    											}
    											
        										if(associationChildAttribInst == null)
        										{
        											objLogger.println("associationChildAttribInst is null ..creating");
        											associationChildAttribInst = associationAttribInst.addOccurrence();
        										}
        										//objLogger.println("associationChildAttribInst.getOccurrenceIndex()"+associationChildAttribInst.getOccurrenceIndex());
        										if(null!=associationChildAttribInst)
    											{
    												associatedPartsAttribInst = itemObj.getAttributeInstance(associationChildAttribInst.getPath()+"/AssociatedParts");
    												objLogger.println("associatedPartsAttribInst is : "+associatedPartsAttribInst.getPath());
													
    												List<? extends AttributeInstance> assocPartList = associatedPartsAttribInst.getChildren();
    												if(assocPartList.size()>0)
    												{
    													for(AttributeInstance assocPart : assocPartList)
    													{
    														assocPart.removeOccurrence();
    													}
    												}
													
    											}
        										
        										if(attribValue.equals(""))
        										{
        											Iterator <? extends AttributeInstance>  attrbItr = associationAttribInst.getChildren().iterator();
        											while(attrbItr.hasNext())
        											{
        												attrbItr.next().removeOccurrence();
        											}
        											objLogger.println("All the associated part instances are removed..and excel doesn't contain any parts..Hence Skipping");
        											continue;
        										}
        										else
        										{
        											if(attribValue.contains(";"))
        											{
        												String assocParts [] = attribValue.split(";");
        												for(int i1 = 0; i1<assocParts.length;i1++)
        												{
        													Item associateditemObj = null;
        													objLogger.println(i1+"th associated part num : "+assocParts[i1]);
        													PIMCollection<Item> itemList = objCatalog.getItemsWithAttributeValue(specName+"/Part Number", assocParts[i1]);
        													for(Item item : itemList)
        													{
        														if(item.getAttributeValue(specName+"/SystemOfRecord").toString().equals("US-QAD"))
        														{
        															noPartPresentFlag = true;
        															associateditemObj = item;
        															break;
        														}
        													}
        													if(null!=associateditemObj)
        													{
        														objLogger.println("Associated item is retrieved for "+assocParts[i1]);
        														associatedPartsChildAttribInst = associatedPartsAttribInst.addOccurrence();
        														associatedPartsChildAttribInst.setValue(associateditemObj);
        														objLogger.println(i1+"th path is : "+associatedPartsChildAttribInst.getPath());
        													}
        													else
        													{
        														objLogger.println("No item present for "+assocParts[i1]);
        													}
        												}
        											}
        											else
        											{
        												if(isNumeric(attribValue))
        												{
        													double var = Double.valueOf(attribValue);
        							            			int var2 = (int) var;
        							            			attribValue = String.valueOf(var2);
        												}
        												objLogger.println("1st(Only) associated part's PIM ID : "+attribValue);
        												Item associateditemObj = null;
    													PIMCollection<Item> itemList = objCatalog.getItemsWithAttributeValue(specName+"/Part Number", attribValue);
    													for(Item item : itemList)
    													{
    														if(item.getAttributeValue(specName+"/SystemOfRecord").toString().equals("US-QAD"))
    														{
    															noPartPresentFlag = true;
    															associateditemObj = item;
    															break;
    														}
    													}
        												if(null!=associateditemObj)
        												{
        													associatedPartsChildAttribInst = associatedPartsAttribInst.addOccurrence();
    														associatedPartsChildAttribInst.setValue(associateditemObj);
        													objLogger.println("Test value of assoc path for single existing part "+associatedPartsChildAttribInst.getPath());
        												}
        												else
        												{
        													objLogger.println("No item present for "+attribValue);
        												}
    												}
        											if(noPartPresentFlag==false)
        											{
        												Iterator <? extends AttributeInstance>  attrbItr = associationAttribInst.getChildren().iterator();
            											while(attrbItr.hasNext())
            											{
            												attrbItr.next().removeOccurrence();
            											}
            											objLogger.println("Part numbers in excel is not found...Hence deleting association instance");
            											continue;
        											}
        										}
        									}
    									}
    									else
    									{
    										objLogger.println("Default block");
    										objLogger.println("Attribute is : "+attribName+"; value is :"+attribValue);
    										itemObj.setAttributeValue(path+"/"+attribName, attribValue);
    									}
    									
        							}
        		            	}
        		            }
    	            		
    	            		//Saving item
    	            		ExtendedValidationErrors errors = itemObj.save();
							if(null==errors)
			            	{
			            		objLogger.println(" Item is saved....PIM_id : "+PIM_id);
			            		
							}
			            	else
			            	{	
			            		List<ValidationError> errorList = errors.getErrors();
			            		int errorCount = 0;
			            		for(ValidationError error : errorList)
			            		{
			            			objLogger.println("Error"+errorCount+" while saving item: "+PIM_id+"---> Error is : "+error.getMessage()+"--->>"+error.getAttributeInstance().getPath());
			            		}
			            	}
    	            	}
    	            }
                	fileMove(fileName,BACKUP_PATH);
        		}
        	}
        	else
        	{
        		objLogger.println("No feedfile found for Mass import");
        	}
        	objLogger.println("All the feedfiles has been retrieved....");
        } 
		catch (Exception e) 
		{
			e.printStackTrace(objLogger);
		} 
		
		finally
		{
			try 
			{
				objLogger.close();
				excelFileStream.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	
	private String validateCellValue(Cell cell, int rowNumber) throws Exception 
    {
          try
          {
               if(null!=cell)
               {
            	   //objLogger.println("Cell type is :" +cell.getCellType()); 
               }
                    
               else
               {
                    objLogger.println("Cell is blank...");
                    return "";
               }
                    
               switch(cell.getCellType())
               {
               case Cell.CELL_TYPE_NUMERIC:
               
               double value = cell.getNumericCellValue();
               //value = Math.round(value);
               if(DateUtil.isCellDateFormatted(cell)) 
               {
                    if(DateUtil.isValidExcelDate(value))
                    {
                          Date date = DateUtil.getJavaDate(value);
                          SimpleDateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy"); 
                          //objLogger.println("Cell type Date...value is :"+dateFormat.format(date));
                          return dateFormat.format(date);                                       
                    }
                    else 
                    {
                          throw new Exception("Invalid Date value found at row number " +
                                    rowNumber +" and column number "+cell.getColumnIndex());}
               } 
               else  
               {
                    //objLogger.println("Cell type Numeric...value is :"+String.valueOf(value));
                    return String.valueOf(value) ;
               }
               
               
               case Cell.CELL_TYPE_STRING:
                    //objLogger.println("Cell type String...value is :"+cell.getStringCellValue());
                    return cell.getStringCellValue();  
                           
               case Cell.CELL_TYPE_BLANK:
                    //objLogger.println("Cell type Blank...value is :");
                   return "";  
               default:
                    return ""; 
               
               }
          }
          catch(Exception ex)
          {
               return "";
          }
          
    }
	
	private void parseHeader(Row row)
	{
		try
		{
			objLogger.println("Inside parseHeader method.........");
			headerMap = new HashMap<String,String>();
			Cell cell = null;
			int maxCellNo = row.getPhysicalNumberOfCells();
			for(int i = 0; i < maxCellNo; i++)
			{
				cell = row.getCell(i);
				String headerVal = validateCellValue(cell,i);
				//objLogger.println(i+"th Header is :"+headerVal);
				headerMap.put(String.valueOf(i), headerVal);
				if(headerVal.equals("PIM_ID"))
				{
					objLogger.println("Inside parseHeader method....PIM_ID");
					pimIDFlag = true;
				}
				else if(headerVal.equals("SystemOfRecord"))
				{
					objLogger.println("Inside parseHeader method....SystemOfRecord");
					sysOfRecFlag = true;
				}
				else if(headerVal.equals("Part Number"))
				{
					objLogger.println("Inside parseHeader method....Part Number");
					partNoFlag = true;
				}
					
			}
		}
		catch(Exception e)
		{
			objLogger.println("Error inside parseHeader method....");
			e.printStackTrace(objLogger);
		}
		
		
	}
	
	private HashMap<String,String> getPackagingLevelPath(Item itemObj,String pimID,String specName)
	{
		HashMap<String,String> packLvlPath = new HashMap<String, String> ();
		String palletPath = "";
		String innerPath = "";
		String masterPath = "";
		String isPallet = "N";
		String isInner = "N";
		String isMaster = "N";
		List<AttributeInstance> packagingLevelAttributeList = null;
		
		List<AttributeInstance> pathAttributeList = (List<AttributeInstance>) itemObj.getAttributeInstance(specName+"/Packaging Hierarchy/Paths").getChildren();
		if(pathAttributeList.size() > 0)
		{
			AttributeInstance pathAttribInst = pathAttributeList.get(0);
			objLogger.println("Path 1 is : "+pathAttribInst.getPath());
			packagingLevelAttributeList = (List<AttributeInstance>) itemObj.getAttributeInstance(pathAttribInst.getPath()+"/Higher Packaging Levels").getChildren();
			
			if(null!= packagingLevelAttributeList && packagingLevelAttributeList.size()>0)
			{
				objLogger.println("Path 2 is : "+packagingLevelAttributeList.get(0).getPath());
				for(AttributeInstance packagingLevelAttribInst : packagingLevelAttributeList)
    			{
    				objLogger.println("Retrieving path for different Packaging level attribute ");
    				String packagingType = itemObj.getAttributeValue(packagingLevelAttribInst.getPath()+"/Unit of Measure").toString();
    				if(packagingType.equals("PALLET"))
    				{
    					objLogger.println("Item "+pimID+" has PALLET Packaging type");
    					isPallet = "Y";
    					palletPath = packagingLevelAttribInst.getPath();
    					objLogger.println("Path of PALLET : "+palletPath);
    				}
    				else if(packagingType.equals("INNER"))
    				{
    					objLogger.println("Item "+pimID+" has INNER Packaging type");
    					isInner = "Y";
    					innerPath = packagingLevelAttribInst.getPath();
    					objLogger.println("Path of INNER : "+innerPath);
    				}
    				else if(packagingType.equals("MASTER"))
    				{
    					objLogger.println("Item "+pimID+" has MASTER Packaging type");
    					isMaster = "Y";
    					masterPath = packagingLevelAttribInst.getPath();
    					objLogger.println("Path of MASTER : "+masterPath);
    				}
    				else
    				{
    					objLogger.println("Item has wrong Packaging Type :"+packagingType);
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
	public void fileMove(String sourceFilePath,String backUPFolderPath)
	{
		try
		{
			objLogger.println("Inside fileMove method");
            File sourceFile = new File(sourceFilePath);
            objLogger.println("sourceFile Path--"+sourceFile);
            File archiveDirFile = new File(backUPFolderPath);
            if (!archiveDirFile.exists()) 
            {
              archiveDirFile.mkdir();
            }
            String archiveFileName = backUPFolderPath + "/" + sourceFile.getName(); 
            objLogger.println("Archive file path--"+archiveFileName);
            File destFile =new File(archiveFileName);

            FileInputStream inStream = new FileInputStream(sourceFile);
            FileOutputStream outStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0)
            {
               outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.flush();
            outStream.close();
            boolean isDelete = sourceFile.delete();
	        if(isDelete)
	        {
	          objLogger.println("file successfully moved...");
	        }
	        else
	        {
	          objLogger.println("file falied to move...");
	        }

        }
		catch(Exception e)
		{
            objLogger.println("Within fileMove method() :Exception is "+e.getMessage());
		}

	}
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}


	


	
}
