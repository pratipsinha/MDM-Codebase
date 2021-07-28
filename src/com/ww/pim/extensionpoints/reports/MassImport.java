//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.extensionpoints.reports.MassImport.class"

package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.spec.Spec;
import com.ww.pim.common.utils.ConnectionManager;
import com.ww.pim.common.utils.Constant;

public class MassImport implements ReportGenerateFunction
{
	private Context ctx = PIMContextFactory.getCurrentContext();
	private static final String EXCEL_FILE_NAME = System.getProperty(Constant.DOLLAR_TOP)+"/feedfiles/Mass Update Import/MassImport.xlsx";
	private FileInputStream excelFileStream = null;
	public Catalog objCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
	public Item objItem = null;
	public Spec objSpec = ctx.getSpecManager().getSpec("Product Catalog Global Spec");
	public String specName = objSpec.getName();
	private String logFileName = System.getProperty(Constant.DOLLAR_TOP)+"/logs/mass_import_test_log.txt";
	private PrintWriter objLogger = null;
	private Map<String,String> headerMap = null;
	private Connection con=null;
	private Statement stmt=null;
	
	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) 
	{
		try 
		{
			File logFile = new File(logFileName);
			objLogger = new PrintWriter(new FileWriter(logFile, true));
			objLogger.println("****************Mass Import report started*****************");
			//Reading Excel file
			File excelFile = new File(EXCEL_FILE_NAME);
			excelFileStream = new FileInputStream(excelFile);
			objLogger.println("Excel file name is : "+ excelFile.getName());
            Workbook workbook = new XSSFWorkbook(excelFileStream);
            Sheet dataSheet = workbook.getSheet("Sheet1");
            
            //Retrieving Header
            Row row = dataSheet.getRow(0);
            Cell cell = null;
            objLogger.println("*****Retrieving Header*****");
            parseHeader(row);
            
            //Retrieving connection object
            if (null == con)
			{
				con = ConnectionManager.getConnection();
			}
            
            if(null!=con)
            {
            	objLogger.println("Mass Import connection obtained : [OK]");
            	stmt = con.createStatement();
            	int rowCount = dataSheet.getPhysicalNumberOfRows();
            	objLogger.println("Row count in import file is : "+rowCount);
            	for(int rowNum = 1 ; rowNum < rowCount;rowNum++)
	            {
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
	            			double var = Double.valueOf(PIM_id);
	            			int var2 = (int) var;
	            			PIM_id = String.valueOf(var2);
	            			objLogger.println("PIM ID is : "+PIM_id);
	            		}
	            		else if(headerName.equals("Part Number"))
	            		{
	            			objLogger.println("Column number is : "+j);
	            			cell = row.getCell(j);
	            			partNo = validateCellValue(cell,rowNum);
	            			double var = Double.valueOf(partNo);
	            			int var2 = (int) var;
	            			partNo = String.valueOf(var2);
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
            			objLogger.println("PIM_ID is blank for row "+rowNum);
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
	            	
	            	for(int i = 0 ; i<headerSize;i++)
		            {
	            		String attribVal = "";
		            	String attribHeaderName = headerMap.get(String.valueOf(i));
		            	if(null!=attribHeaderName)
		            	{
		            		cell = row.getCell(i);
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
		            			String query = "INSERT INTO PIMDBUSR.MASS_IMPORT_TAB (PIM_ID,PART_NUMBER,SYSTEM_OF_RECORD,ATTRIBUTE,STATUS) VALUES('" +
										PIM_id+"','"+partNo+"','"+sysOfRecord+"','"+attribHeaderName+"|"+attribVal+"','N')" ;
		            			objLogger.println("Query string is : "+query);
		            			stmt.addBatch(query);
		            		}
		            	}
		            }
	            }
            	objLogger.println("Storing to database");
            	stmt.executeBatch();
				con.commit();
				
				String query = "SELECT DISTINCT PIM_ID FROM PIMDBUSR.MASS_IMPORT_TAB "
						+ "WHERE STATUS = 'N'";
				ResultSet rs = stmt.executeQuery(query);
				List<String> itemIDlist = new ArrayList<String> ();
				while(rs.next())
				{
					String pim_id = rs.getString("PIM_ID");
					objLogger.println("PIM_ID from database is : "+pim_id);
					itemIDlist.add(pim_id);
				}
				
				for(String pim_id : itemIDlist)
				{
					stmt = con.createStatement();
					if(null!=pim_id)
					{
						objItem = objCatalog.getItemByPrimaryKey(pim_id);
						if(null!=objItem)
						{
							HashMap<String,String> packPathMap = getPackagingLevelPath(objItem,pim_id,specName);
							String queryAttrib = "SELECT ATTRIBUTE FROM PIMDBUSR.MASS_IMPORT_TAB "
									+ "WHERE STATUS = 'N'AND PIM_ID = '"+pim_id+"'";
							ResultSet rsAttrib = stmt.executeQuery(queryAttrib);
							objLogger.println("********Retrieving attribute value from table***********For PIM_ID : "+pim_id);
							while(rsAttrib.next())
							{
								String attribName = "";
								String attribValue = "";
								String attribute = rsAttrib.getString("ATTRIBUTE");
								String arr [] = attribute.split("\\|");
								objLogger.println("arr.length : "+arr.length);
								if(arr.length==1)
								{
									attribName = arr[0];
								}
								else if(arr.length==2)
								{
									attribName = arr[0];
									attribValue = arr[1];
								}
								else
								{
									objLogger.println("Contains multiple | for "+attribute);
									continue;
								}
								String updateQuery = "UPDATE PIMDBUSR.MASS_IMPORT_TAB SET STATUS = 'Y'"
										+ " WHERE PIM_ID = '"+pim_id+"' AND ATTRIBUTE = '"+attribute+"' AND STATUS = 'N'";
								objLogger.println(updateQuery);
								stmt.addBatch(updateQuery);
								objLogger.println("attribute from database is : "+attribName+ " Value is : "+attribValue);
								
								//To save item
								String path = specName;
								if(attribName.contains(":"))
								{
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
											objLogger.println("Item# "+pim_id+" has no MASTER type Packaging level data");
											continue;
										}
										path = packPathMap.get("MASTER_PATH");
									}
									else if(packagingType.equals("INNER"))
									{
										objLogger.println("Packaging level Data : INNER");
										if(packPathMap.get("HAS_INNER").equals("N"))
										{
											objLogger.println("Item# "+pim_id+" has no INNER type Packaging level data");
											continue;
										}
										path = packPathMap.get("INNER_PATH");
									}
									else if(packagingType.equals("PALLET"))
									{
										objLogger.println("Packaging level Data : PALLET");
										if(packPathMap.get("HAS_PALLET").equals("N"))
										{
											objLogger.println("Item# "+pim_id+" has no PALLET type Packaging level data");
											continue;
										}
										path = packPathMap.get("PALLET_PATH");
									}
									
									objItem.setAttributeValue(path+"/"+attribName, attribValue);
								}
								else if(attribName.equals("Type"))
								{
									path = path + "/Associations";
									AttributeInstance attrInst = objItem.getAttributeInstance(path);
									List<? extends AttributeInstance> attrAssociatedPartsInstList = attrInst.getChildren();
									for(AttributeInstance associatedPartsInst : attrAssociatedPartsInstList)
									{
										path = associatedPartsInst.getPath();
										objLogger.println("Path for association-->Type is : "+path);
									}
									//objItem.setAttributeValue(path+"/"+attribName, attribValue);
								}
								else if(attribName.equals("AssociatedParts"))
								{
									List <String> associatedPartList = new ArrayList<String> ();
									int associatedPartsCount = 1;
									if(attribValue.contains(","))
									{
										String assocParts [] = attribValue.split(",");
										for(String s : assocParts)
										{
											objLogger.println(associatedPartsCount+++"th associated part num : "+s);
											associatedPartList.add(s);
										}
									}
									else
									{
										objLogger.println(associatedPartsCount+++"th associated part num : "+attribValue);
										associatedPartList.add(attribValue);
									}
									
								}
								else
								{
									objItem.setAttributeValue(path+"/"+attribName, attribValue);
								}
								
								
							}
							
							ExtendedValidationErrors errors = objItem.save();
							if(null==errors)
			            	{
			            		objLogger.println(" Item is saved....PIM_ID : "+pim_id);
			            		stmt.executeBatch();
			            	}
			            	else
			            	{
			            		
			            		List<ValidationError> errorList = errors.getErrors();
			            		int errorCount = 0;
			            		for(ValidationError error : errorList)
			            		{
			            			objLogger.println("Error"+errorCount+" while saving item: "+pim_id+"---> Error is : "+error.getMessage());
			            		}
			            	}
						}
						else
						{
							objLogger.println("Item not found for PIM_ID "+pim_id);
						}
						
					}
					
					
					
				}
				
				con.commit();
				stmt.close();
            }
            else
            {
            	objLogger.println("Connection can't be created");
            }
            
            
            
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
				headerMap.put(String.valueOf(i), headerVal);
			}
		}
		catch(Exception e)
		{
			objLogger.println("Error inside parseHeader method....");
			e.printStackTrace(objLogger);
		}
		
		
	}
	
	private HashMap<String,String> getPackagingLevelPath(Item objItem,String pimID,String specName)
	{
		HashMap<String,String> packLvlPath = new HashMap<String, String> ();
		String palletPath = "";
		String innerPath = "";
		String masterPath = "";
		String isPallet = "N";
		String isInner = "N";
		String isMaster = "N";
		List<AttributeInstance> packagingLevelAttributeList = null;
		
		List<AttributeInstance> pathAttributeList = (List<AttributeInstance>) objItem.getAttributeInstance(specName+"/Packaging Hierarchy/Paths").getChildren();
		if(pathAttributeList.size() > 0)
		{
			AttributeInstance pathAttribInst = pathAttributeList.get(0);
			objLogger.println("Path 1 is : "+pathAttribInst.getPath());
			packagingLevelAttributeList = (List<AttributeInstance>) objItem.getAttributeInstance(pathAttribInst.getPath()+"/Higher Packaging Levels").getChildren();
			
			if(null!= packagingLevelAttributeList && packagingLevelAttributeList.size()>0)
			{
				objLogger.println("Path 2 is : "+packagingLevelAttributeList.get(0).getPath());
				for(AttributeInstance packagingLevelAttribInst : packagingLevelAttributeList)
    			{
    				objLogger.println("Retrieving path for different Packaging level attribute ");
    				String packagingType = objItem.getAttributeValue(packagingLevelAttribInst.getPath()+"/Unit of Measure").toString();
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

}
