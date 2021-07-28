package com.ww.pim.extensionpoints.reports;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.ConnectionManager;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.common.utils.UtilityMethods;
public class PriceProcessor implements ReportGenerateFunction{

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		System.out.println("PriceProcesser Called");
		Context pimCtx = null;
		Logger logger = null;
		String inputFileCount = "";
		//int fileCount = 0;
		String propertiesFilePath = "";
		Connection con=null;
		Statement stmt=null;
		//Catalog prodCatalog = null;
		//SearchQuery srchQuery = null;
		//SearchResultSet resultSet = null;
		
		//Spec objSpec = null;
		try {
			Map inputHm = arg0.getInputs();
			if (null != inputHm) {
				propertiesFilePath = System.getProperty("TOP") + Constant.COMMON_PROPERTIES;
				FileInputStream inStream = new FileInputStream(propertiesFilePath);
				Properties commonProperties = new Properties();
				commonProperties.load(inStream);
				pimCtx = PIMContextFactory.getCurrentContext();
				if (null != pimCtx) {
					logger = pimCtx.getLogger(Constant.LOGGER_NAME);
					//prodCatalog = pimCtx.getCatalogManager().getCatalog("Product Catalog");
					//objSpec = pimCtx.getSpecManager().getSpec("Product Catalog Global Spec");
					if (null != logger) {
						logger.logInfo("PriceProcessor Pim Context Obtained : [OK]");
						inputFileCount =   inputHm.get(Constant.file_count).toString() ;
						logger.logInfo("PriceProcessor inputFileCount : "+inputFileCount +" : [OK]");
						UtilityMethods utilityMethods = new UtilityMethods();
						logger.logInfo("arg0");
						File[] files = utilityMethods.listAllPriceFiles(commonProperties.getProperty("INBOUND_FILE_LOC"),logger);
						
						//List<PriceRecordPojo> priceRowList=null;
						if (null != files)
						{
							if (files.length ==0)
							{
								logger.logInfo("PriceProcessor There is no Price file to process : [OK]");
							}
							else
							{
								Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
								for (int i=0;i<files.length;i++){
									logger.logInfo("File name : "+files[i].getName());
								}
								if (null == con)
								{
									con = ConnectionManager.getConnection();
								}
								if (null != con)
								{
									logger.logInfo("PriceProcessor connection obtained : [OK]");
									stmt = con.createStatement();
									for (int i=0;i<files.length;i++)
									{
										logger.logInfo("File name : "+files[i].getName());
										utilityMethods.readPriceFileContent(files[i], "\\|",stmt,commonProperties.getProperty("INBOUND_FILE_ARCHIVE_LOC"), logger);
									}
									stmt.executeBatch();
									con.commit();
									stmt.close();
								}else
								{
									logger.logInfo("PriceProcessor connection not obtained : [NOT OK]");
								}
							}
						}
						else
						{
							logger.logInfo("PriceProcessor input file list is empty : [OK]");
						}
									/*if (null != priceRowList)
									{
										
										
										for (PriceRecordPojo priceRow : priceRowList)
										{
											logger.logInfo("PriceProcessor price row obtained : [OK]");
											
										*/	
											
											//Saving item in Product catalog
											/*String specName = objSpec.getName();
											String partNumber = priceRow.getPartNumber();
											String systemOfRec = priceRow.getSystemOfRecord();
											PIMCollection <Item> objCatalogItemList = prodCatalog.getItemsWithAttributeValue(specName+"/Part Number", partNumber);
											for(Item objItem : objCatalogItemList)
											{
												String sysOfRec = objItem.getAttributeValue(specName+"/SystemOfRecord").toString();
												if(sysOfRec.equals(systemOfRec))
												{
													objItem.setAttributeValue(specName+"/ListPriceNew-USADollars", priceRow.getListPriceNew_USADollars());
													objItem.setAttributeValue(specName+"/ListPricePCTChange-USADollars", priceRow.getListPricePCTChange_USADollars());
													objItem.setAttributeValue(specName+"/NewListPriceEffectiveDate", priceRow.getNewListPriceEffectiveDate());
													objItem.setAttributeValue(specName+"/ListPrice-USADollars", priceRow.getListPrice_USADollars());
													ExtendedValidationErrors errors = objItem.save();
									            	if(null==errors)
									            	{
									            		logger.logDebug(partNumber +" : Catalog item is saved....");
									            	}
									            	else
									            	{
									            		
									            		List<ValidationError> errorList = errors.getErrors();
									            		int errorCount = 0;
									            		for(ValidationError error : errorList)
									            		{
									            			logger.logDebug("Error"+errorCount+" while saving catalog item: "+error.getMessage());
									            		}
									            	}
													break;
												}
											}*/
											
											/*//Saving item for collab area
											logger.logDebug("Retrieving colab item");
											String querySelect = "select item from collaboration_area('QAD Parts Maintenance Staging Area') where "
													+ "item['"+specName+"/UniqueProductQualifier'] = '"+priceRow.getPartNumber()+" "+priceRow.getSystemOfRecord()+"'";
											logger.logDebug(querySelect);
											srchQuery = pimCtx.createSearchQuery(querySelect);
											resultSet = srchQuery.execute();
											logger.logDebug("Retrieved...");
											Item objItem = null;
											while(resultSet.next())
											{
												logger.logDebug("Saving collab item..");
												objItem = resultSet.getItem(0);
											}
											
											if(null!=objItem)
											{
												logger.logDebug("Item is : "+objItem.getPrimaryKey());
												objItem.setAttributeValue(specName+"/ListPriceNew-USADollars", priceRow.getListPriceNew_USADollars());
												objItem.setAttributeValue(specName+"/ListPricePCTChange-USADollars", priceRow.getListPricePCTChange_USADollars());
												objItem.setAttributeValue(specName+"/NewListPriceEffectiveDate", priceRow.getNewListPriceEffectiveDate());
												objItem.setAttributeValue(specName+"/ListPrice-USADollars", priceRow.getListPrice_USADollars());
												ExtendedValidationErrors errors = objItem.save();
								            	if(null==errors)
								            	{
								            		logger.logDebug(partNumber +" : Collab area item is saved....");
								            	}
								            	else
								            	{
								            		
								            		List<ValidationError> errorList = errors.getErrors();
								            		int errorCount = 0;
								            		for(ValidationError error : errorList)
								            		{
								            			logger.logDebug("Error"+errorCount+" while saving Collab item: "+error.getMessage());
								            		}
								            	}
											}
											else
											{
												logger.logDebug("Item not found...");
											}
											*/
										
										
					}
					else 
					{
						System.out.println("PriceProcessor Logger Not Found : [NOT OK]");
					}
				}
				else 
				{
					System.out.println("PriceProcessor PIM Context Not Found : [NOT OK]");
				}
			}
			else 
			{
				System.out.println("PriceProcessor input Parameters Null : [NOT OK]");
			}
		}
		catch(Exception exception) 
		{
			logger.logInfo("Exception Occurred in PriceProcessor.reportGenerate Method : [NOT OK]");
			logger.logDebug(exception.getMessage());
		}
		finally
		{
			try
			{
				if (null != con)
				{
					con.close();
					logger.logInfo("Connection Closed in PriceProcessor.reportGenerate Method : [OK]");
				}
			}
			catch(Exception exp)
			{
				logger.logInfo("Exception Occurred in PriceProcessor.reportGenerate Method while closing connection: [NOT OK]");
				exp.printStackTrace();
			}
		}
	}
}