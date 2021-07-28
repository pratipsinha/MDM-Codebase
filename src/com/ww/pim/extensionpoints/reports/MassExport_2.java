//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.extensionpoints.reports.MassExportSingleFile.class"

package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.search.SearchQuery;
import com.ibm.pim.search.SearchResultSet;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.common.utils.UtilityMethods;

public class MassExport_2 implements ReportGenerateFunction
{
	private Context ctx = PIMContextFactory.getCurrentContext();
	private Logger objLogger = ctx.getLogger(Constant.REPORT_LOGGER_NAME);
	private UtilityMethods utilityMethods = new UtilityMethods();
	
	
	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) 
	{
		objLogger.logDebug("Report MassExport_2 started...");
		String PROPERTY_FILE_NAME = System.getProperty(Constant.DOLLAR_TOP)+"/properties/MassExport.properties";
		Properties properties = null;
		XSSFWorkbook wb = new XSSFWorkbook();
		String outputFileName = System.getProperty(Constant.DOLLAR_TOP)+"/Output Files/Mass_Export/Core_Attribute_Export/Mass_Export_2_";
		FileOutputStream outputStream = null;
		try
		{
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			timeStamp = timeStamp.replace(".", "_");
			
			outputFileName = outputFileName + timeStamp;
			outputFileName = outputFileName+".xlsx";
			File file = new File(PROPERTY_FILE_NAME);
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
			outputStream = new FileOutputStream(outputFileName,false);
		
			Map<String,Integer> associationTypesMap = new HashMap<String,Integer>();
			XSSFSheet sheet = wb.createSheet("Sheet1");
			
			int rowNum = 0;
			int columnNum = 0;
			Row row = sheet.createRow(rowNum);
			int numberOfTypes=0;
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
					String[] associationTypes = utilityMethods.getPossibleAssociationTypes(ctx,objLogger);
					if (null != associationTypes && associationTypes.length>0){
						for (int i=0;i<associationTypes.length ; i++){
							objLogger.logInfo(associationTypes[i]);
						}
						numberOfTypes = associationTypes.length;
						for (int i=0;i<associationTypes.length ; i++){
							if (i==0){
								
								cell.setCellValue(associationTypes[i]);
								associationTypesMap.put(associationTypes[i], columnNum -1);
							}
							else{
								cell = row.createCell(columnNum++);
								cell.setCellValue(associationTypes[i]);
								associationTypesMap.put(associationTypes[i], columnNum-1);
							}
						}
					}
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
			
			objLogger.logDebug("******Printing item list in product catalog********");
			String query = null;
			SearchQuery srchQuery = null;
			SearchResultSet resultSet = null;
			Item objItem= null;
			//query = "select item from catalog('Product Catalog') where item['Product Catalog Global Spec/PIM_ID'] in (1,2,3,4,5,6,7,8,9,10,11,12,13,14,130,144,165,172,179,188,229,234,235,239,240,245,251,252,265,271,272,313,349,401,465,482,483,484,485,486,487,488,489,517,521,522,556,557,558,581,584,591,592,607,608,621,628,639,640,771,774,777,805,807,822,823,825,826,828,829,889,915,916,917,918,919,920,921,922,926,944,952,956,969,975,1007,1059,1124,1298,1299,1300,1301,1302,1303,1304,1306,1310,1311,1325,1326,1327,1328,1329,1330,1331,1332,1333,1334,1335,1336,1338,1357,1358,1359,1360,1361,1362,1363,1364,1365,1366,1367,1369,1371,1373,1375,1377,1379,1383,1385,1387,1389,1401,1402,1406,1409,1411,1422,1423,1424,1425,1426,1427,1429,1431,1433,1435,1437,1439,1441,1443,1445,1447,1449,1451,1453,1455,1457,1459,1461,1473,1474,1480,1482,1484,1495,1500,1504,1506,1508,1510,1511,1512,1513,1514,1515,1516,1517,1518,1521,1522,1523,1524,1526,1527,1529,1530,1531,1532,1533,1534,1535,1536,1537,1538,1539,1540,1541,1542,1543,1544,1545,1546,1547,1548,1549,1569,1570,1573,1585,1586,1587,1588,1589,1593,1597,1601,1602,1617,2595,2602,2603,2605,2608,2609,2611,2613,2614,2615,2616,2619,2621,2622,2623,2625,2626,2627,2631,2632,2633,2634,2654,2656,2700,2705,2707,2708,2709,2714,2716,2764,2765,2767,2774,2775,2776,2780,2781,2784,2785,3283,3284,3285,3286,3290,3291,3292,3294,3295,3296,3298,3299,3300,3301,3303,3304,3305,3306,3308,3309,3310,3311,3313,3314,3315,3316,3625,3673,3694,3695,3727,3734,3973,4032,4090,4091,4092,4117)";
			//query = "select item from catalog('Product Catalog') where item['Product Catalog Global Spec/PIM_ID']  >="+1+" AND item['Product Catalog Global Spec/PIM_ID']  <="+21109;
			query = "select item from catalog('Product Catalog') where item['Product Catalog Global Spec/PIM_ID']  >="+21110+" AND item['Product Catalog Global Spec/PIM_ID']  <="+60039;
			srchQuery = ctx.createSearchQuery(query);
			resultSet = srchQuery.execute();
			objLogger.logDebug("Query is---"+query);
			srchQuery = ctx.createSearchQuery(query);
			resultSet = srchQuery.execute();
			while(resultSet.next()) {
				objItem = resultSet.getItem(1);
				rowNum++;
				columnNum = 0;
				row = sheet.createRow(rowNum);
				for(int i = 1 ; i<=properties.size() ; i++)
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
						cell.setCellValue(attribValue);
					}
					else if(arr[1].equals("Associations"))
					{
						columnNum = columnNum + numberOfTypes;
						AttributeInstance attribInst = objItem.getAttributeInstance(arr[0]+"/"+arr[1]);
						List<? extends AttributeInstance> associationAtrribInstList = (List<? extends AttributeInstance>) attribInst.getChildren();
						if(associationAtrribInstList.size()==0)
						{
							continue;
						}
						String associatedParts = "";
						for(AttributeInstance associationLeafAttributeInstance : associationAtrribInstList)
						{
							String attribPathType = associationLeafAttributeInstance.getPath()+"/Type";
							String typeValue = (String) objItem.getAttributeValue(attribPathType);
							//objLogger.logInfo(typeValue);
							if(null==typeValue)
							{
								typeValue = "";
							}
							int typeColumnIndex = associationTypesMap.get(typeValue);
							attribPath = associationLeafAttributeInstance.getPath()+"/AssociatedParts";
							AttributeInstance attrInst = objItem.getAttributeInstance(attribPath);
							List<? extends AttributeInstance> attrAssociatedPartsInstList = attrInst.getChildren();
							for(AttributeInstance associatedPartsInst : attrAssociatedPartsInstList)
							{
								Item linkedItem = null;
								//String pathAssociatedPartsInst = associatedPartsInst.getPath();
								//objLogger.logDebug("Path for associatedPartsInst is : "+pathAssociatedPartsInst);
								
								if(null!=associatedPartsInst.getValue())
								{
									//associatePIM_ID = associatedPartsInst.getValue().toString();
									linkedItem = (Item)associatedPartsInst.getValue();
									//objLogger.logDebug(++count2+"th attribValue for association is : "+associatedPartsInst.getValue());
								}
								
								if(null!=linkedItem)
								{
									attribValue = linkedItem.getAttributeValue("Product Catalog Global Spec/Part Number").toString();
									
										//objLogger.logDebug(count+"th Associated Patr number is : "+attribValue);
									
									
									if(associatedParts.equals("") && !attribValue.equals(""))
									{
										associatedParts = attribValue+ " "+linkedItem.getAttributeValue("Product Catalog Global Spec/SystemOfRecord").toString();
										
									}
									else if(!associatedParts.equals("") && !attribValue.equals(""))
									{
										associatedParts = associatedParts+";"+attribValue+ " "+linkedItem.getAttributeValue("Product Catalog Global Spec/SystemOfRecord").toString();
									}
									
								}
							}
							Cell cell = row.createCell(typeColumnIndex);
							//objLogger.logInfo("associatedParts"+associatedParts);
							cell.setCellValue(associatedParts);
						}
					}
					else if(arr[2].equals("Paths"))
					{
						int columnNumOld = columnNum;
						
						AttributeInstance attribInst = objItem.getAttributeInstance(arr[0]+"/"+arr[1]+"/"+arr[2]);
						List<? extends AttributeInstance> pathAtrribInstList = (List<? extends AttributeInstance>) attribInst.getChildren();
						if(!(pathAtrribInstList.size()>0))
						{
							//objLogger.logDebug("Has no occurence for Paths");
							columnNum=columnNum+3;
							continue;
						}
						//objLogger.logDebug("Has occurence for Paths");
						AttributeInstance pathAttribInst = pathAtrribInstList.get(0);
						//objLogger.logDebug("Path for pathAttribInst is : "+pathAttribInst.getPath());
						List<? extends AttributeInstance> higherPkgLvlAtrribInstList = (List<? extends AttributeInstance>) pathAttribInst.getChildren();
						//objLogger.logDebug("Path is @ : "+higherPkgLvlAtrribInstList.get(0).getPath());
						List<? extends AttributeInstance> higherPkgLvlAtrribInstList2 = (List<? extends AttributeInstance>) higherPkgLvlAtrribInstList.get(0).getChildren();
						//objLogger.logDebug("Number of packaging level is : "+higherPkgLvlAtrribInstList2.size());
						for(AttributeInstance higherPkgLvlAttribInst : higherPkgLvlAtrribInstList2)
						{
							
							String pathUOM = higherPkgLvlAttribInst.getPath()+"/Unit of Measure";
							String valueUOM = "";
							if(null!=objItem.getAttributeValue(pathUOM))
							{
								valueUOM = objItem.getAttributeValue(pathUOM).toString();
							}
							attribPath = higherPkgLvlAttribInst.getPath()+"/"+arr[4];
							
							//objLogger.logDebug("attribPath is: "+attribPath);
							if(null!=objItem.getAttributeValue(attribPath))
							{
								attribValue = objItem.getAttributeValue(attribPath).toString();
							}
							//objLogger.logDebug(i+"---"+attribValue);
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
			objLogger.logDebug(e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				objLogger.logDebug("Start : Writing to excel");
				wb.write(outputStream);
				//wb2.write(outputStream2);
				objLogger.logDebug("Complete : Writing to excel");
				
				outputStream.close();
				//outputStream2.close();
				//objLogger.close();
			} 
			catch (Exception e) 
			{
				objLogger.logDebug(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
