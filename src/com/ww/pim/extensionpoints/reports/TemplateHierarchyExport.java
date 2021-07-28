//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.extensionpoints.reports.TemplateHierarchyExportV2.class"

package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.spec.SecondarySpec;
import com.ww.pim.common.utils.Constant;

public class TemplateHierarchyExport implements ReportGenerateFunction
{
	private Context ctx = PIMContextFactory.getCurrentContext();
	private PrintWriter objLogger = null;
	private Hierarchy objHierarchy = ctx.getHierarchyManager().getHierarchy("Templates Hierarchy");
	private Catalog objProductCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
	private XSSFWorkbook wb = new XSSFWorkbook();
	private String outputFileName = System.getProperty(Constant.DOLLAR_TOP)+"/Output Files/Mass_Export/Template_Attribute_Export/Template_Export"+new Date().toString()+".xlsx";
	private String logFileName = System.getProperty(Constant.DOLLAR_TOP)+"/logs/Template_Export_log.txt";
	private FileOutputStream outputStream = null;
	
	
	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) 
	{
		try
		{
			File logFile = new File(logFileName);
			outputStream = new FileOutputStream(outputFileName);
			try 
			{
				objLogger = new PrintWriter(new FileWriter(logFile, true));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}  

			objLogger.println("*******TemplateHierarchyExport Started**********");
			PIMCollection<Category> level1CategoryList = objHierarchy.getCategoriesAtLevel(1);
			for(Category level1Category : level1CategoryList)
			{
				
				if(level1Category.hasChildren())
				{
					String parentCategoryName = level1Category.getName();
					objLogger.println("");
					objLogger.println("");
					objLogger.println("");
					objLogger.println("");
					objLogger.println("");
					objLogger.println("");
					objLogger.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					objLogger.println("****************Parent category is : "+parentCategoryName+"***************");
					objLogger.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					objLogger.println("");
					objLogger.println("");
					objLogger.println("");
					
					//HSSFSheet sheet = wb.createSheet(parentCategoryName);
					XSSFSheet sheet = wb.createSheet(parentCategoryName);
					int rowNum = 0;
					int columnNum = 3;
					int lastColNum = columnNum;
					Row row = sheet.createRow(rowNum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Part Number");
					cell = row.createCell(1);
					cell.setCellValue("System of Record");
					cell = row.createCell(2);
					cell.setCellValue("Hierarchy");
					Collection<Category> level2CategoryList = level1Category.getChildren();
					for(Category level2Category : level2CategoryList)
					{
						
						if(!level2Category.hasChildren())
						{
							String leafCategoryName = level2Category.getName();
							objLogger.println("");
							objLogger.println("");
							objLogger.println("");
							objLogger.println("*************************************************************************");
							objLogger.println("Fetching items for category "+leafCategoryName);
							objLogger.println("*************************************************************************");
							
							
							
							PIMCollection <Item> itemList = level2Category.getItems(objProductCatalog);
							Collection<SecondarySpec> objSecSpecList = level2Category.getItemSecondarySpecs();
							List <String> attributeList = new ArrayList<String>();
							if(itemList.size()> 0)
							{
								for(SecondarySpec objSecSpec : objSecSpecList)
								{
									List<AttributeDefinition> attrDefList = objSecSpec.getAttributeDefinitions();
									for(AttributeDefinition attrDef : attrDefList)
									{
										//objLogger.println(attrDef.getName());
										cell = row.createCell(columnNum++);
										cell.setCellValue(attrDef.getName());
										attributeList.add(objSecSpec.getName()+"/"+attrDef.getName());
									}
								}
								
								int itemCount = 0;
								for(Item item : itemList)
								{
									columnNum = 0;
									Row row2 = sheet.createRow(rowNum++);
									String partNumber = "";
									String systemOfRecord = "";
									
									
									objLogger.println("");
									objLogger.println("");
									objLogger.println("");
									objLogger.println("--------------------------------------------------------------------------------------------");
									if(null!=item.getAttributeValue("Product Catalog Global Spec/Part Number"))
									{
										partNumber = item.getAttributeValue("Product Catalog Global Spec/Part Number").toString();
									}
									objLogger.println("Part number of Item"+String.valueOf(itemCount++)+" is : "+partNumber);
									if(null!=item.getAttributeValue("Product Catalog Global Spec/SystemOfRecord"))
									{
										systemOfRecord = item.getAttributeValue("Product Catalog Global Spec/SystemOfRecord").toString();
									}
									objLogger.println("System of record value for  Item"+String.valueOf(itemCount-1)+" is : "+systemOfRecord);
									objLogger.println("--------------------------------------------------------------------------------------------");
									if(null!=partNumber)
									{
										row2.createCell(columnNum++).setCellValue(partNumber);
									}
									else
									{
										columnNum++;
									}
									
									if(null!=systemOfRecord)
									{
										row2.createCell(columnNum++).setCellValue(systemOfRecord);
									}
									else
									{
										columnNum++;
									}
									row2.createCell(columnNum++).setCellValue(parentCategoryName+"/"+leafCategoryName);
									columnNum = lastColNum;
									for(String attributePath : attributeList)
									{
										String attrbValue = "";
										if(null!=item.getAttributeValue(attributePath))
										{
											attrbValue = item.getAttributeValue(attributePath).toString();
										}
										objLogger.println(attributePath.split("/")[1]+" attribute value is : "+attrbValue);
										if(null!=attrbValue)
										{
											row2.createCell(columnNum++).setCellValue(attrbValue);
										}
										else
										{
											columnNum++;
										}
										
									}
								}
							}
							
							lastColNum = columnNum;
						}
					}
				}
			}
			
			objLogger.println("*******TemplateHierarchyExport Ended**********");
		}
		catch(Exception e)
		{
			objLogger.println("*******Error found while running the report**********");
			e.printStackTrace(objLogger);
		}
		
		
		
		
		finally
		{
			try 
			{
				wb.write(outputStream);
				outputStream.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			objLogger.close();
		}
	}

}
