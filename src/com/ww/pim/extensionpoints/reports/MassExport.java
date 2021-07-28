//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.extensionpoints.reports.MassExport.class"

package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;

public class MassExport implements ReportGenerateFunction
{
	//PIM objects
	private Context ctx = PIMContextFactory.getCurrentContext();
	private Catalog objProductCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
	private Spec objSpec = objProductCatalog.getSpec();
	
	//Properties file
	private static final String PROPERTY_FILE_NAME = System.getProperty(Constant.DOLLAR_TOP)+"/properties/MassExport.properties";
	public Properties properties = null;
	
	
	
	//Log file
	private String logFileName = System.getProperty(Constant.DOLLAR_TOP)+"/logs/test_log.txt";
	private Logger objLogger = null;
	
	
	//Output Excel file
	private XSSFWorkbook wb = new XSSFWorkbook();
	private String outputFileName = System.getProperty(Constant.DOLLAR_TOP)+"/Output Files/Mass_Export"+new Date().getTime()+".xlsx";
	private FileOutputStream outputStream = null;
	
	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) 
	{
		try
		{
			
			
			//Creating log file
			File logFile = new File(logFileName);
			objLogger = ctx.getLogger(Constant.IMPORT_LOGGER_NAME);
			
			//Reading property file that contains changed attribute names
			File file = new File(PROPERTY_FILE_NAME);
			objLogger.logDebug("Property file name is : "+ file.getName());
			FileInputStream fileInput = new FileInputStream(file);
			properties = new Properties();
			properties.load(fileInput);
			
			objLogger.logDebug("Size of properties file :"+properties.size());
			fileInput.close();
			
			
			
			//Creating list of attributes for Mass Export
			List <String> attribList = new ArrayList <String> ();
			for(int i = 1; i <= properties.size(); i++)
			{
				objLogger.logDebug(i+"th property is : "+properties.getProperty(String.valueOf(i)));
				attribList.add(properties.getProperty(String.valueOf(i)));
			}
			//Creating output excel file stream
			outputStream = new FileOutputStream(outputFileName);
			XSSFSheet sheet = wb.createSheet("Sheet1");
			int rowNum = 0;
			int columnNum = 0;
			Row row = sheet.createRow(rowNum);
			//Creating header
			for(String attribute : attribList)
			{
				Cell cell = row.createCell(columnNum++);
				objLogger.logDebug("attribute  : "+attribute);
				String arr [] = attribute.split("/");
				
				if(arr.length==2)
				{
					objLogger.logDebug("lenght-----2");
					cell.setCellValue(arr[1]);
				}
				else if(arr.length==3)
				{
					objLogger.logDebug("lenght-----3");
					cell.setCellValue(arr[2]);
				}
				else if(arr.length==4)
				{
					objLogger.logDebug("lenght-----4");
					cell.setCellValue("EA : "+arr[3]);
				}
				else if(arr.length==5)
				{
					objLogger.logDebug("lenght-----5");
					cell.setCellValue("MASTER : "+arr[4]);
					cell = row.createCell(columnNum++);
					cell.setCellValue("INNER : "+arr[4]);
					cell = row.createCell(columnNum++);
					cell.setCellValue("PALLET : "+arr[4]);
				} 
			}
			
			String specName = objSpec.getName();
			PIMCollection<Item> objItemList = null;
			objItemList = objProductCatalog.getItems();
			objLogger.logDebug("******Printing item list in product catalog********");
			for(Item objItem : objItemList)
			{
				rowNum++;
				columnNum = 0;
				row = sheet.createRow(rowNum);
				objLogger.logDebug(objItem.getAttributeValue(specName+"/PIM_ID").toString());
				
				
				for(int i = 1 ; i<properties.size() ; i++)
				{
					
					String attribValue = "";
					String attribPath = properties.getProperty(String.valueOf(i));
					String arr [] = attribPath.split("/");
					if(arr.length <= 2 || arr[2].equals("Each Level"))
					{
						Cell cell = row.createCell(columnNum++);
						if(null!=objItem.getAttributeValue(attribPath))
						{
							attribValue = objItem.getAttributeValue(attribPath).toString();
						}
						objLogger.logDebug(i+"---"+attribValue);
						cell.setCellValue(attribValue);
					}
					else if(arr[1].equals("Associations"))
					{
						String associatedParts = "";
						Cell cell = row.createCell(columnNum++);
						AttributeInstance attribInst = objItem.getAttributeInstance(arr[0]+"/"+arr[1]);
						List<AttributeInstance> associationAtrribInstList = (List<AttributeInstance>) attribInst.getChildren();
						if(!(associationAtrribInstList.size()>0))
						{
							objLogger.logDebug("Has no occurence for Associations new");
							//columnNum++;
							continue;
						}
						objLogger.logDebug("Has occurence for Associations");
						int count = 0;
						int count2 = 0;
						for(AttributeInstance associationLeafAttributeInstance : associationAtrribInstList)
						{
							attribPath = associationLeafAttributeInstance.getPath()+"/AssociatedParts";
							objLogger.logDebug("Final attribPath for Association is : "+attribPath);
							AttributeInstance attrInst = objItem.getAttributeInstance(attribPath);
							
							List<? extends AttributeInstance> attrAssociatedPartsInstList = attrInst.getChildren();
							for(AttributeInstance associatedPartsInst : attrAssociatedPartsInstList)
							{
								String associatePIM_ID = "";
								Item linkedItem = null;
								String pathAssociatedPartsInst = associatedPartsInst.getPath();
								objLogger.logDebug("Path for associatedPartsInst is : "+pathAssociatedPartsInst);
								
								if(null!=associatedPartsInst.getValue())
								{
									associatePIM_ID = associatedPartsInst.getValue().toString();
									linkedItem = (Item)associatedPartsInst.getValue();
									objLogger.logDebug(++count2+"th attribValue for association is : "+associatedPartsInst.getValue());
								}
								
								if(null!=linkedItem)
								{
									if(null!=linkedItem)
									{
										count++;
										attribValue = linkedItem.getAttributeValue("Product Catalog Global Spec/Part Number").toString();
										objLogger.logDebug(count+"th Associated Patr number is : "+attribValue);
									}
									if(!(associatedParts.equals("")))
									{
										associatedParts = associatedParts+","+attribValue;
									}
									else
									{
										associatedParts = attribValue;
									}
									
								}
								
								
								
							}
							if(!(associatedParts.equals("")))
							{
								associatedParts = associatedParts+","+attribValue;
							}
							else
							{
								associatedParts = attribValue;
							}
						}
						
						objLogger.logDebug(i+"---"+associatedParts);
						cell.setCellValue(associatedParts);
					}
					else if(arr[2].equals("Paths"))
					{
						int columnNumOld = columnNum;
						
						AttributeInstance attribInst = objItem.getAttributeInstance(arr[0]+"/"+arr[1]+"/"+arr[2]);
						List<? extends AttributeInstance> pathAtrribInstList = (List<? extends AttributeInstance>) attribInst.getChildren();
						if(!(pathAtrribInstList.size()>0))
						{
							objLogger.logDebug("Has no occurence for Paths");
							columnNum=columnNum+3;
							continue;
						}
						objLogger.logDebug("Has occurence for Paths");
						AttributeInstance pathAttribInst = pathAtrribInstList.get(0);
						objLogger.logDebug("Path for pathAttribInst is : "+pathAttribInst.getPath());
						List<? extends AttributeInstance> higherPkgLvlAtrribInstList = (List<? extends AttributeInstance>) pathAttribInst.getChildren();
						objLogger.logDebug("Path is @ : "+higherPkgLvlAtrribInstList.get(0).getPath());
						List<? extends AttributeInstance> higherPkgLvlAtrribInstList2 = (List<? extends AttributeInstance>) higherPkgLvlAtrribInstList.get(0).getChildren();
						objLogger.logDebug("Number of packaging level is : "+higherPkgLvlAtrribInstList2.size());
						for(AttributeInstance higherPkgLvlAttribInst : higherPkgLvlAtrribInstList2)
						{
							
							String pathUOM = higherPkgLvlAttribInst.getPath()+"/Unit of Measure";
							String valueUOM = "";
							if(null!=objItem.getAttributeValue(pathUOM))
							{
								valueUOM = objItem.getAttributeValue(pathUOM).toString();
							}
							attribPath = higherPkgLvlAttribInst.getPath()+"/"+arr[4];
							
							objLogger.logDebug("attribPath is: "+attribPath);
							if(null!=objItem.getAttributeValue(attribPath))
							{
								attribValue = objItem.getAttributeValue(attribPath).toString();
							}
							objLogger.logDebug(i+"---"+attribValue);
							if(valueUOM.equals("MASTER"))
							{
								columnNum = columnNumOld;
								Cell cell = row.createCell(columnNum);
								cell.setCellValue(attribValue);
							}
							else if(valueUOM.equals("INNER"))
							{
								columnNum = columnNumOld+1;
								Cell cell = row.createCell(columnNum);
								cell.setCellValue(attribValue);
							}
							else if(valueUOM.equals("PALLET"))
							{
								columnNum = columnNumOld+2;
								Cell cell = row.createCell(columnNum);
								cell.setCellValue(attribValue);
							}
						}
						columnNum = columnNumOld+3;
					}
					
				}
				break;
			}
			objLogger.logDebug("******END OF item list********");
			objLogger.logDebug("");
			objLogger.logDebug("");
			objLogger.logDebug("");
			objLogger.logDebug("");
			objLogger.logDebug("******Printing list of global attribute********");
			
			objLogger.logDebug("******END OF Attribute list********");
		}
		catch(Exception e)
		{
			objLogger.logDebug("Exception caught.."+e);
		}
		finally
		{
			try 
			{
				wb.write(outputStream);
				outputStream.close();
				//objLogger.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}

}
