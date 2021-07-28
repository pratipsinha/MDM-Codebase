//***************************************************************
// File name      : QAD_PIM_Parts_Creation_And_Updation
// File Type      : Java
// ---------------------------------------------------------------------------------------------------------------------
// Title : This code will create/update catalog/collaboration Item in Product Catalog/Product Maintenance Collaboration Area. This code will also create/update cloned item in catalog/collaboration area based on request feed file from QAD application.
// PIM will receive a  pipe (|) separated feed file for QAD owned attributes from QAD application at specified accessible location and will parse those file to create/update item in PIM.

//  File Format : 
//Part Number|Pack_level_change|Is_cloned|ClonedFromPartNumber|BuyAmericanAct|ItemStatus|LeadFree|ListPrice-USADollars|ReplaceByItem|StockStatus|TargetAudience|Brand|CountryOfOrigin-Primary|CountryOfOrigin-Secondary|Description1|Description2|Description-PriceGroup|Description-ProductLine|HTSNumber|LeadFreeItemNumber|LegacyPartNumber|ListPriceNew-USADollars|ListPricePCTChange-USADollars|NSFSafetyListing|OrderingLeadTime|OrderingLeadTimeUnit|PriceGroup|ProductLine|ProductManager|ReplaceByItemAvailabilityDate|ReplaceByNotificationDate|FinishedGood|FreightClass|EANUCCCode|EA:NetContent|EA:NetContentUOM|EA:PackageDepth|EA:PackageGTIN|EA:PackageHeight|EA:PackagePiecesPerLayer|EA:PackagePiecesPerPallet|EA:PackageWeight|EA:PackageWidth|EA:Volume|EA:Unit of Measure|INNER:NetContent|INNER:NetContentUOM|INNER:PackageDepth|INNER:PackageGTIN|INNER:PackageHeight|INNER:PackagePiecesPerLayer|INNER:PackagePiecesPerPallet|INNER:PackageWeight|INNER:PackageWidth|INNER:PackageQtyPer|INNER:Volume|MASTER:NetContent|MASTER:NetContentUOM|MASTER:PackageDepth|MASTER:PackageGTIN|MASTER:PackageHeight|MASTER:PackagePiecesPerLayer|MASTER:PackagePiecesPerPallet|MASTER:PackageWeight|MASTER:PackageWidth|MASTER:PackageQtyPer|MASTER:Volume|MASTER:Unit of Measure|PALLET:NetContent|PALLET:NetContentUOM|PALLET:PackageDepth|PALLET:PackageGTIN|PALLET:PackageHeight|PALLET:PackagePiecesPerLayer|PALLET:PackagePiecesPerPallet|PALLET:PackageWeight|PALLET:PackageWidth|PALLET:PackageQtyPer|PALLET:Volume|PALLET:Unit of Measure|RequireSOMultipleOf|ADACompliant|WeightNet
//1111|No|No||NO|AC||||||||||||||||||||||5010|||||||1234||2||||||||||||||||||||2671||174|||||||||MASTER|||||||||||8757|PALLET|||2
// Authors : Pankaj.Singh3@cognizant.com
// Company : Cognizant
// Date : 2/14/2018
// Description : 
// Assumptions : For one job run only one feed file will be processed.
// Usage :
// Audit :
// : First version 2/14/2018
// ---------------------------------------------------------------------------------------------------------------------
// $Header: /cvs/repo/
//**********************************************************************************************************************
// todo/notes/caveats: <Running Notes>

package com.ww.pim.extensionpoints.reports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import com.ibm.ccd.element.common.ProcessingOptions;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationArea;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.search.SearchQuery;
import com.ibm.pim.search.SearchResultSet;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.email.GenerateNotification;

public class QAD_PIM_Parts_Creation_And_Updation implements ReportGenerateFunction {

	private Context ctx = null;
	private Logger objLogger = null;
	private Catalog productCatalog = null;
	private Hierarchy templateHierarchy = null;
	private ItemCollaborationArea itemColArea = null;
	private LinkedHashMap<String, String> qadpimAttributesMap = null;
	private List<String> clonedExceptionList = null;
	private List<CollaborationItem> clonedItmList = null;
	//private LinkedHashMap<AttributeInstance, Object> originalPartData = null;

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		// TODO Auto-generated method stub
		System.out.println("Parts creation report called");
		ctx = PIMContextFactory.getCurrentContext();
		//System.out.println("Parts creation report called1");
		productCatalog =ctx.getCatalogManager().getCatalog(Constant.PRODUCT_CATALOG);
		//System.out.println("Parts creation report called2");
		templateHierarchy=ctx.getHierarchyManager().getHierarchy("Templates Hierarchy");
		//System.out.println("Parts creation report called3");
		itemColArea = (ItemCollaborationArea)ctx.getCollaborationAreaManager().getCollaborationArea(Constant.QADPARTSMAINTCOLAREANAME);
		//System.out.println("Parts creation report called4");
		objLogger = ctx.getLogger(Constant.LOGGER_NAME);
		//System.out.println("Parts creation report called5");
		objLogger.logDebug("Parts creation report called");
		BufferedReader buffReader = null;
		String filePath = "";
		String line = "";
		String errorFileName = "";
		File objFile = null;
		File mailFile = null;
		FileWriter fileWriter = null;
		BufferedWriter bwWriter = null;
		objLogger.logDebug("QAD_PIM_Parts_Creation_And_Updation Report Started..........");
		Map<Integer, String> namePathMap = new HashMap<Integer, String>();
		clonedItmList = new ArrayList<CollaborationItem>();
		// populating Cloning Exception List
		populateCloneExceptionsList();
		objLogger.logDebug("populateCloneExceptionsList method ended.."+clonedExceptionList.size());
		try
		{
			if(populateAttributesDataInList())
			{
				objLogger.logDebug("Property file populated...");
				File[] files = new File(Constant.QAD_PIM_IMPORT_FEED_FILE_PATH).listFiles();
				Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
				for(File file : files) // processing for feed file starts.
				{
					if( !file.isDirectory()) {
						try {
							if(file.getName().startsWith("mdmpimex"))
							{
								filePath = file.getPath();
								int count=0;
								int commitCounter = 0;
								errorFileName  = file.getName().replace(".txt", "_Error");
								objLogger.logDebug("Path is--"+filePath);
								//	if(!"".equals(filePath)) // Only if file to process exists in server.
								//{
								objFile = new File(Constant.QAD_PIM_IMPORT_ERROR_FILE_PATH+"/"+errorFileName+".txt");
								mailFile = new File(Constant.QAD_PIM_IMPORT_MAIL_DIR_PATH+"/"+errorFileName+".txt");
								buffReader = new BufferedReader(new FileReader(filePath));
								BufferedReader inputReader = new BufferedReader(new FileReader(filePath));
								while((line =inputReader.readLine()) != null) // Iterating through each record of feed file
								{
									objLogger.logDebug("Verification line--"+line);
								}
								fileWriter = new FileWriter(objFile,true);
								bwWriter = new BufferedWriter(fileWriter);
								while((line =buffReader.readLine()) != null) // Iterating through each record of feed file
								{
									objLogger.logDebug("line--"+line);
									String dataArray[]= {};
									String partNumber = "";
									String pack_level_changes = "";
									String clone_status = "";
									String fromClonedPartNo = "";
									boolean occurencesDeleted = false;
									Item ctgItem = null;
									CollaborationItem newCollabItem = null;
									CollaborationItem existingCollabItem = null;
									boolean newItemCreation = false;
									dataArray=line.split("\\|",-1);
									objLogger.logDebug("dataArray--"+dataArray.length);
									if(dataArray.length != 73)
									{
										objLogger.logDebug("Since file columns does not match pre-defined template hence processing for the file will not be done.");
										break;
									}
									Item fromPartNumberItem = null;
									ExtendedValidationErrors evErrorList = null;
									boolean cloneNotCreated = false;

									if(count == 0) // This is used for mapping feed file header name with property file and each column index will be mapped with PIM attribute Path.
									{
										int index = 0;
										for(String colname : dataArray)
										{
											String path = qadpimAttributesMap.get(colname);  // This will retrieve attribute path.
											namePathMap.put(index, path); // key = column_index value = PIM attribute path. For e.g., 0=Product Catalog Global Spec/Part Number 5=Product Catalog Global Spec/ItemStatus
											index++;
										}
									}
									else if(count>0) // Processing Records 
									{
										partNumber = dataArray[0].trim();
										pack_level_changes = dataArray[1].trim();
										clone_status = dataArray[2].trim();
										fromClonedPartNo = dataArray[3].trim();
										objLogger.logDebug("partNumber--"+partNumber+" pack_level_changes---"+pack_level_changes+" clone_status---"+clone_status+" fromClonedPartNo--"+fromClonedPartNo);
										Map<String, String> itemMap= getItemObjectMap(partNumber); // key = ctgItem or collab item, value = PrimaryKey of catalog item
										if(!itemMap.get("ctgItem").equals("null")) {
											ctgItem = productCatalog.getItemByPrimaryKey(itemMap.get("ctgItem")); // Catalog Item object created if valid primary key is returned
										}
										if(!itemMap.get("colItem").equals("null")) {
											existingCollabItem = itemColArea.getCheckedOutItem(itemMap.get("colItem"));  // Collaboration Item object created if valid primary key is returned
										}
										objLogger.logDebug("ctgItem--"+ctgItem+" existingCollItem--"+existingCollabItem);

										if(null == ctgItem && null  == existingCollabItem) // If both catalog item and collaboration item are null then a new collaboration item will be created.
										{
											objLogger.logDebug("Both catalog and coll item are null..so creating collaboration item..");
											newCollabItem = itemColArea.createCollaborationItem();
											newItemCreation=true;
											newCollabItem.setAttributeValue(namePathMap.get(0),partNumber);
											newCollabItem.setAttributeValue("Product Catalog Global Spec/SystemOfRecord", "US-QAD");
										}
										if(!newItemCreation && pack_level_changes.equalsIgnoreCase("yes")) // This logic will be executed if feed file has pack_level_changes = Yes and item should be an existing one
										{
											objLogger.logDebug("pack level change is yes....");
											occurencesDeleted = deleteExistingPackagingOccurrences(ctgItem,existingCollabItem); // All existing packaging hierarchy paths will be deleted to create a new one from feed file.
											objLogger.logDebug("occurencesDeleted--!!!!!"+occurencesDeleted);
										}

										if(newItemCreation && clone_status.equalsIgnoreCase("yes")) // This logic will be executed if clone_status is Yes (and item does not exist in PIM) in QAD generated feed file.
										{
											objLogger.logDebug("clone_status--"+clone_status+" and collab item--"+newCollabItem);
											Map<String, String> sourcePartNumberMap = getItemObjectMap(fromClonedPartNo); // This will retrieve a original catalog item primary key from where global attributes data are to be copied to cloned item
											objLogger.logDebug("sourcePartNumberMap.getctgItem--"+sourcePartNumberMap.get("ctgItem"));
											if(!sourcePartNumberMap.get("ctgItem").equals("null")) {  
												fromPartNumberItem = productCatalog.getItemByPrimaryKey(sourcePartNumberMap.get("ctgItem")); // creating catalog item object of original item
											}
											if(fromPartNumberItem != null) {  // If original item exists in product Catalog then cloned item will  be created.
												newCollabItem.setAttributeValue(namePathMap.get(3),fromClonedPartNo);
												copyOriginalPartCoreAndTemplateAttributesToClonedItem(newCollabItem,fromPartNumberItem); // Copying global attributes from Original item to Cloned Item
											}
											else
											{
												//newCollabItem = null; // Setting newly created collab item to null if fromClonedPartNumber is not found.
												//cloneNotCreated = true;
												objLogger.logDebug("Set cloneNotCreated to true and new collabItem to null..");
											}
										}

										// Going to set remaining attributes
										for(int i=4;i<72;i++) // From 5th column onwards all attributes will be set with data in an item.
										{
											String attribPath = namePathMap.get(i);
											if(ctgItem != null) // This will update attribute values in existing catalog item
											{
												objLogger.logDebug("Entering existing catalog item for setting attributes.."+attribPath+" ->"+dataArray[i].trim());
												if(attribPath.contains("INNER") || attribPath.contains("MASTER") || attribPath.contains("PALLET"))
												{
													if(occurencesDeleted) // This logic will be executed if pack_level_changes =yes where all packaging attributes will be updated with QAD feed file
													{
														setPackagingAttributes(attribPath,ctgItem,null,dataArray[i].trim(),clone_status);
													}
												}
												else
												{
													objLogger.logDebug("Going to set  attribute.."+attribPath+" for existing catalog item");
													setAttribValue(ctgItem,null,attribPath,dataArray[i].trim());
													//ctgItem.setAttributeValue(namePathMap.get(i), dataArray[i]); 
												}
											}
											if(existingCollabItem != null) // This will update attribute values in existing collaboration item
											{
												objLogger.logDebug("Entering existing collaboration item.."+attribPath+" ->"+dataArray[i].trim());
												if(attribPath.contains("INNER") || attribPath.contains("MASTER") || attribPath.contains("PALLET"))
												{
													if(occurencesDeleted) // This logic will be executed if pack_level_changes =yes where all packaging attributes will be updated with QAD feed file
													{
														setPackagingAttributes(attribPath,null,existingCollabItem,dataArray[i].trim(),clone_status);
													}
												}
												else
												{
													objLogger.logDebug("Going to set  attribute.."+attribPath+" for existing collaboration item");
													setAttribValue(null,existingCollabItem,attribPath,dataArray[i].trim());
													//existingCollabItem.setAttributeValue(namePathMap.get(i), dataArray[i].trim());
												}
											}
											if(newCollabItem != null) // This will set attribute values in newly created collaboration item
											{
												objLogger.logDebug("Entering new collaboration item.."+attribPath+" ->"+dataArray[i].trim());
												if(attribPath.contains("INNER") || attribPath.contains("MASTER") || attribPath.contains("PALLET"))
												{

													setPackagingAttributes(attribPath,null,newCollabItem,dataArray[i].trim(),clone_status);
												}
												else
												{
													objLogger.logDebug("Going to set  attribute.."+attribPath+" for new collaboration item");
													setAttribValue(null,newCollabItem,attribPath, dataArray[i].trim());
													//newCollabItem.setAttributeValue(namePathMap.get(i), dataArray[i].trim());
												}
											}
											if(newCollabItem == null && existingCollabItem == null &&  ctgItem == null)
											{
												break;
											}
										}

										if(newItemCreation && null != newCollabItem) // For new item creation collaboration item will be saved.
										{
											objLogger.logDebug("Going to create collaboration item.");
											Date currentDate = new Date();
											newCollabItem.setAttributeValue("Product Catalog Global Spec/WorkflowDate", currentDate);
											if(null != newCollabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode")) // Going to set EA:PackageGTIN by prefixing 000 with UPCCode
											{
												String eaPkgGTIN = "00"+newCollabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode").toString();
												newCollabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN", eaPkgGTIN);
												//	objLogger.logDebug("Adding to col Area..");
												//	boolean isItemAdded = itemColArea.addIntoCollaborationArea(newCollabItem);
												//objLogger.logDebug("Added To Col Area.."+isItemAdded);								

											}
											// Going to append AdditionalTradeItemDescription_en-US, Description1 and Description-Marketing with String value "Edit before save" for cloned item
											if(clone_status.equalsIgnoreCase("yes"))
											{
												//newCollabItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US", "EBS!");
												//	newCollabItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing", "EBS!");
												/*if(null != newCollabItem.getAttributeValue("Product Catalog Global Spec/Description1"))
												{
													String desc1 = (String)newCollabItem.getAttributeValue("Product Catalog Global Spec/Description1");
													String newDescVal = desc1+"-Edit before saving!" ;
													newCollabItem.setAttributeValue("Product Catalog Global Spec/Description1",newDescVal);
												}
												else
												{
													newCollabItem.setAttributeValue("Product Catalog Global Spec/Description1","-Edit before saving!");
												}*/
												Random random = new Random(System.nanoTime() % 100000);

												int randomInt = random.nextInt(10000000);
												
												if(null != newCollabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US"))
												{
													String tradeitemDesc = (String)newCollabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US");
													//String newtradeitemDesc = tradeitemDesc+"-Edit before saving!";
													String newtradeitemDesc = tradeitemDesc+"-Edit before saving("+randomInt+")!";
													newCollabItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US",newtradeitemDesc);
												}
												else
												{
													//newCollabItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US","-Edit before saving!");
													newCollabItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US","-Edit before saving("+randomInt+")!");
												}
												if(null != newCollabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing"))
												{
													String mktDesc = (String)newCollabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing");
													//String newmktDesc = mktDesc+"-Edit before saving!";
													String newmktDesc = mktDesc+"-Edit before saving("+randomInt+")!";
													newCollabItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing",newmktDesc);
												}
												else
												{
													//newCollabItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing","-Edit before saving!");
													newCollabItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing","-Edit before saving("+randomInt+")!");
												}
												// Going to append AdditionalTradeItemDescription_en-US, Description1 and Description-Marketing with String value "Edit before save" for cloned item Ends
											}
											if (clone_status.equalsIgnoreCase("yes")){
												clonedItmList.add(newCollabItem);
												/*try{
													objLogger.logDebug("Before processing clone record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
													
													itemColArea.getProcessingOptions().setMinMaxLengthProcessing(false);
													if(newCollabItem.validate() == null) 
													{
														objLogger.logDebug("Validation Passed clone record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
														evErrorList = newCollabItem.save();
														if(null != newCollabItem.getAttributeValue("Product Catalog Global Spec/ProductLine"))
														{
															objLogger.logDebug("Collaboration item successfully created and saved. Now adding the record in table..");
															String productLine = newCollabItem.getAttributeValue("Product Catalog Global Spec/ProductLine").toString();
															new GenerateNotification().insertRecordMailNotification(partNumber, "US-QAD", productLine);
															objLogger.logDebug("new collaboration item successfuly added in Mail_Notification table..");
														}
													}else
													{
														evErrorList =newCollabItem.validate();
													}
													objLogger.logDebug("After processing clone record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
												}catch(Exception exp){
													objLogger.logDebug("Exception is processing clone records "+exp.getMessage());
												}finally{
													
													itemColArea.getProcessingOptions().resetProcessingOptions();
													Thread.sleep(5000);
													objLogger.logDebug("After sleeping");
												}*/
											}else {
												
												try{
													objLogger.logDebug("Before processing normal record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
													
													if(newCollabItem.validate() == null) 
													{
														objLogger.logDebug("Validation Passed normal record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
														
														
														//itemColArea.getProcessingOptions().setMinMaxLengthProcessing(true);
														//com.ibm.pim.common.ProcessingOptions ps = itemColArea.getProcessingOptions();
														//ps.
														objLogger.logDebug("After enableing");
														//itemColArea.getProcessingOptions();
														//objLogger.logDebug(itemColArea.getProcessingOptions();
														evErrorList = newCollabItem.save();
														if(null != newCollabItem.getAttributeValue("Product Catalog Global Spec/ProductLine"))
														{
															objLogger.logDebug("Collaboration item successfully created and saved. Now adding the record in table..");
															String productLine = newCollabItem.getAttributeValue("Product Catalog Global Spec/ProductLine").toString();
															new GenerateNotification().insertRecordMailNotification(partNumber, "US-QAD", productLine);
															objLogger.logDebug("new collaboration item successfuly added in Mail_Notification table..");
														}
													}else
													{
														evErrorList =newCollabItem.validate();
													}
													objLogger.logDebug("After processing normal record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
												}catch(Exception exp){
													objLogger.logDebug("Exception is processing normal records "+exp.getMessage());
												}
											}
											
											
										}
										if(null != ctgItem) //  update in existing catalog item will be saved.
										{
											if(null != ctgItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode")) {
												String eaPkgGTIN = "00"+ctgItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode").toString();
												ctgItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN", eaPkgGTIN);
											}
											if(ctgItem.validate() == null) {
												objLogger.logDebug("Going to save existing catalog item..");
												productCatalog.getProcessingOptions().setCollaborationAreaLocksValidationProcessing(false);
												objLogger.logDebug("Going to save existing catalog item..");
												evErrorList = ctgItem.save();
												objLogger.logDebug("Saved Existing catalog item.."+evErrorList);
											}
											else
											{
												evErrorList =ctgItem.validate();
											}
										}
										if(null != existingCollabItem) //  update in existing collaboration item will be saved.
										{
											if(null != existingCollabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode")) {
												String eaPkgGTIN = "00"+existingCollabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode").toString();
												existingCollabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN", eaPkgGTIN);
											}
											if(existingCollabItem.validate() == null) {
												objLogger.logDebug("Going to save existing collaboration item..");
												//itemColArea.getProcessingOptions().setAllProcessingOptions(false);
												evErrorList = existingCollabItem.save();
												objLogger.logDebug("Saved existingCollItem.."+evErrorList);
											}
											else
											{
												evErrorList =existingCollabItem.validate();
											}
										}
										if(evErrorList != null && evErrorList.getErrors().size() > 0)
										{
											if(!objFile.exists()){
												objFile.createNewFile();
												objLogger.logDebug("New file created for part number.."+partNumber);
											}
											else
											{
												objLogger.logDebug("Existing  file appended for part number.."+partNumber);
											}
											List<ValidationError> esList = evErrorList.getErrors();
											for(Iterator<ValidationError> oItr =esList.iterator();oItr.hasNext();){
												ValidationError verror = oItr.next();
												objLogger.logDebug("Error message is --"+verror.getMessage()+" "+verror.getAttributeInstance().getPath());
												bwWriter.write(partNumber+" has Validation Error for attribute "+verror.getAttributeInstance().getAttributeDefinition().getName()+" which is "+verror.getMessage());
												bwWriter.newLine();								
											}

										}
										if(cloneNotCreated)
										{
											bwWriter.write("Cloned Item "+partNumber+" can not be created as its fromPartNumber "+fromClonedPartNo+" does not exist in Product Catalog.");
											bwWriter.newLine();	
										}

										// Write in Error file in case item save is not successful
									}

									count++;
									commitCounter++;
									if(commitCounter>=50){
										objLogger.logDebug("commitCounter --> "+commitCounter);
										ctx.commit();
										commitCounter=0;
									}
								} // record iteration ends here
								//ctx.commit();
								Iterator<CollaborationItem> iter = clonedItmList.iterator();
								int cloneCommitCounter = 0;
								while(iter.hasNext()){
									ExtendedValidationErrors evErrorList = null;
									CollaborationItem newCollabItem = iter.next();
									try{
									objLogger.logDebug("Before processing clone record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
									
									itemColArea.getProcessingOptions().setMinMaxLengthProcessing(false);
									String partNumber  = newCollabItem.getAttributeValue("Product Catalog Global Spec/Part Number").toString();
									if(newCollabItem.validate() == null) 
									{
										objLogger.logDebug("Validation Passed clone record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
										evErrorList = newCollabItem.save();
										if(null != newCollabItem.getAttributeValue("Product Catalog Global Spec/ProductLine"))
										{
											objLogger.logDebug("Collaboration item successfully created and saved. Now adding the record in table..");
											String productLine = newCollabItem.getAttributeValue("Product Catalog Global Spec/ProductLine").toString();
											new GenerateNotification().insertRecordMailNotification(partNumber, "US-QAD", productLine);
											objLogger.logDebug("new collaboration item successfuly added in Mail_Notification table..");
										}
									}else
									{
										evErrorList =newCollabItem.validate();
									}
									if(evErrorList != null && evErrorList.getErrors().size() > 0)
									{
										if(!objFile.exists()){
											objFile.createNewFile();
											objLogger.logDebug("New file created for part number.."+partNumber);
										}
										else
										{
											objLogger.logDebug("Existing  file appended for part number.."+partNumber);
										}
										List<ValidationError> esList = evErrorList.getErrors();
										for(Iterator<ValidationError> oItr =esList.iterator();oItr.hasNext();){
											ValidationError verror = oItr.next();
											objLogger.logDebug("Error message is --"+verror.getMessage()+" "+verror.getAttributeInstance().getPath());
											bwWriter.write(partNumber+" has Validation Error for attribute "+verror.getAttributeInstance().getAttributeDefinition().getName()+" which is "+verror.getMessage());
											bwWriter.newLine();								
										}

									}
									objLogger.logDebug("After processing clone record "+newCollabItem.getAttributeValue(namePathMap.get(0)));
									}catch(Exception exp){
										objLogger.logDebug("Exception is processing clone records "+exp.getMessage());
									}finally{
										
										itemColArea.getProcessingOptions().resetProcessingOptions();
									}
									if(cloneCommitCounter>=50){
										objLogger.logDebug(" cloneCommitCounter cloneCommitCounter --> "+cloneCommitCounter);
										ctx.commit();
										cloneCommitCounter=0;
									}
								}
								/*try {
									if(bwWriter != null)
									{
										try {
											bwWriter.flush();
											objLogger.logInfo("Flushing bwWriter.");
										} catch (IOException e) {
											objLogger.logInfo("Exception occurred while flushing bwWriter..."+e);
										}
									}
									if(fileWriter != null)
									{
										try {
											fileWriter.flush();
											fileWriter.close();
											bwWriter.close();
											Files.copy(objFile.toPath(),mailFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
											if(objFile.length() == 0)
											{
												objLogger.logInfo("Going to delete file since it is empty..");
												objFile.delete();
												objLogger.logInfo("Empty file deleted..");
											}
										} catch (IOException e) {
											objLogger.logInfo("Exception occurred while Flushing and closing fileWriter. and closing bwWriter..."+e);
										}
									}

								} catch (Exception e) {
									objLogger.logInfo("Exception is:"+e);
								} */
							} // Only if filename starts with "mdmpimex" it will be processed.
							else
							{
								objLogger.logInfo("File name doesn't start with mdmpimex..so ignoring..");
							}
						}
						catch(Exception ex)
						{
							objLogger.logInfo("Exception caught for file--"+ex);
						}
						finally {
							if(file.getName().startsWith("mdmpimex"))
							{
								archiveFile(filePath,Constant.QAD_PIM_IMPORT_FEED_FILE_ARCHIVEPATH); 
							}
							else
							{
								objLogger.logInfo("File name doesn't start with mdmpimex..so not archiving..");
							}
							
							//Added By Pratip 22nd Apr 2021
							try {
								if(bwWriter != null)
								{
									try {
										bwWriter.flush();
										objLogger.logInfo("Flushing bwWriter.");
									} catch (IOException e) {
										objLogger.logInfo("Exception occurred while flushing bwWriter..."+e);
									}
								}
								if(fileWriter != null)
								{
									try {
										fileWriter.flush();
										fileWriter.close();
										bwWriter.close();
										Files.copy(objFile.toPath(),mailFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
										if(objFile.length() == 0)
										{
											objLogger.logInfo("Going to delete file since it is empty..");
											objFile.delete();
											objLogger.logInfo("Empty file deleted..");
										}
										if(mailFile.length() == 0)
										{
											objLogger.logInfo("Going to delete mail file since it is empty..");
											mailFile.delete();
											objLogger.logInfo("Empty file deleted..");
										}
									} catch (IOException e) {
										objLogger.logInfo("Exception occurred while Flushing and closing fileWriter. and closing bwWriter..."+e);
									}
								}

							} catch (Exception e) {
								objLogger.logInfo("Exception is:"+e);
							}
							// End Added by Pratip 22nd Apr 2021
						}

					} // If file is processed logic ends here
				} // file iteration ends here
			} // populateAttributesDataInList ends here
		} // try block ends here.
		catch(Exception ex)
		{
			objLogger.logDebug("Within main catch block exception caught.."+ex);
		}
		finally {
			objLogger.logDebug("QAD_PIM_Parts_Creation_And_Updation Report Ended....");

			/*try {
				if(bwWriter != null)
				{
					try {
						bwWriter.flush();
						objLogger.logInfo("Flushing bwWriter.");
					} catch (IOException e) {
						objLogger.logInfo("Exception occurred while flushing bwWriter..."+e);
					}
				}
				if(fileWriter != null)
				{
					try {
						fileWriter.flush();
						fileWriter.close();
						bwWriter.close();
						if(objFile.length() == 0)
						{
							objLogger.logInfo("Going to delete file since it is empty..");
							objFile.delete();
							objLogger.logInfo("Empty file deleted..");
						}
					} catch (IOException e) {
						objLogger.logInfo("Exception occurred while Flushing and closing fileWriter. and closing bwWriter..."+e);
					}
				}

			} catch (Exception e) {
				objLogger.logInfo("Exception is:"+e);
			}*/
		}

	}

	/**
	 * Description: This method is used to set QAD owned packaging attributes (INNER:Package, PALLET:PackageDepth, MASTER:Volume) in catalog/collaboration item.
	 * parameters attribute path, catalog item, collaboration item, attribute value
	 * @return void.
	 */

	private void setPackagingAttributes(String attrPath, Item objItem, CollaborationItem colItem, String data, String cloneStatus) {
		// TODO Auto-generated method stub
		objLogger.logDebug("Within setPackagingAttributes method..");
		try {
			if(objItem == null) { // If update has come for collaboration item then catalog item logic will not be executed.
				objLogger.logDebug("Within setPackagingAttributes method.going to create packaging for collaboration Item..");
				int occSize = colItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size();
				String attrName = attrPath.split("\\.")[1];
				String packlevelIdentifier = attrPath.split("\\.")[0];
				objLogger.logDebug("AttributeName----"+attrName+" and packlevelIdentifier---"+packlevelIdentifier+" and occSize--"+occSize);
				boolean uomFound = false;
				if(occSize == 0 )  // If Packaging Hierarchy occurrence is zero, a new occurrence will be added and values will be set. This will happen for 1st Packaging relevant attribute encounter
				{
					if(!("").equals(data)) {
						colItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).addOccurrence();
						AttributeInstance packLevelAttrinstance = colItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).addOccurrence();  // New occurrence added
						String path = packLevelAttrinstance.getPath()+"/"+attrName;
						colItem.setAttributeValue( packLevelAttrinstance.getPath()+"/"+Constant.UOM_PATH, packlevelIdentifier);
						setAttribValue(null, colItem, path, data);
						//colItem.setAttributeValue(path, data);
					}
				}
				else // This will happen for 2nd attribute encounter when at least one occurrence is added for 1st attribute encounter
				{
					objLogger.logDebug("In else..");
					Iterator<? extends AttributeInstance> higherpackLevelsList = colItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
					while(higherpackLevelsList.hasNext()) // This will check if Unit of Measure already exists in Item for which value needs to be processed
					{
						AttributeInstance childInst = higherpackLevelsList.next();
						String higherPackLevelPath = childInst.getPath();
						objLogger.logDebug("higherPackLevelPath--"+higherPackLevelPath);
						if(null != colItem.getAttributeValue(higherPackLevelPath+"/"+Constant.UOM_PATH) && colItem.getAttributeValue(higherPackLevelPath+"/"+Constant.UOM_PATH).equals(packlevelIdentifier))
						{
							if(cloneStatus.equalsIgnoreCase("yes") && attrName.equals("Unit of Measure") && ("").equals(data))
							{
								objLogger.logDebug("Clone status is yes and attribute name is Unit of measure..");
							}
							else
							{
								objLogger.logDebug("Going to set value for higher packaging attributes..");
								setAttribValue(null, colItem, higherPackLevelPath+"/"+attrName, data);
								//colItem.setAttributeValue(higherPackLevelPath+"/"+attrName, data);
							}
							uomFound =true;
							break;
						}

					}
					if(!uomFound && colItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).canAddOccurrence())  // If uom encountered while processing is not already present in item then a new occurrence will be added for new unit of measure
					{
						if(!("").equals(data)) {
							objLogger.logDebug("uom not found..so going to add occurrence..");
							AttributeInstance attrInstance = colItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).addOccurrence();
							String path = attrInstance.getPath()+"/"+attrName;
							objLogger.logDebug("path--"+path+"-----"+attrInstance.getPath()+"/"+Constant.UOM_PATH);
							colItem.setAttributeValue( attrInstance.getPath()+"/"+Constant.UOM_PATH, packlevelIdentifier);
							if(cloneStatus.equalsIgnoreCase("yes") && attrName.equals("Unit of Measure"))
							{

							}
							else
							{
								setAttribValue(null, colItem,path, data);
								//colItem.setAttributeValue(path, data);
							}
						}
					}
				}
			}
			else   // If update has come for catalog item then collaboration item logic will not be executed.
			{
				objLogger.logDebug("Within setPackagingAttributes method.going to create packaging for catalog Item.."+data);
				int occSize = objItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size();
				String attrName = attrPath.split("\\.")[1];
				String packlevelIdentifier = attrPath.split("\\.")[0];
				objLogger.logDebug("AttributeName----"+attrName+" and packlevelIdentifier---"+packlevelIdentifier+" and occSize--"+occSize);
				boolean uomFound = false;
				if(occSize == 0) // If Packaging Hierarchy occurrence is zero, a new occurrence will be added and values will be set. This will happen for 1st Packaging relevant attribute encounter
				{
					if(!("").equals(data))
					{
						objItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).addOccurrence();
						AttributeInstance packLevelAttrinstance = objItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).addOccurrence(); 
						String path = packLevelAttrinstance.getPath()+"/"+attrName;
						objItem.setAttributeValue( packLevelAttrinstance.getPath()+"/"+Constant.UOM_PATH, packlevelIdentifier);
						setAttribValue(objItem, null,path, data);
						//objItem.setAttributeValue(path, data);
					}
				}
				else // This will happen for 2nd packaging attribute encounter when at least one occurrence is added for 1st attribute encounter
				{
					objLogger.logDebug("In else..");
					Iterator<? extends AttributeInstance> higherpackLevelsList = objItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
					while(higherpackLevelsList.hasNext()) // This will check if Unit of Measure already exists in Item for which value needs to be processed
					{
						AttributeInstance childInst = higherpackLevelsList.next();
						String higherPackLevelPath = childInst.getPath();
						objLogger.logDebug("higherPackLevelPath--"+higherPackLevelPath);
						if(null != objItem.getAttributeValue(higherPackLevelPath+"/"+Constant.UOM_PATH) && objItem.getAttributeValue(higherPackLevelPath+"/"+Constant.UOM_PATH).equals(packlevelIdentifier))
						{
							setAttribValue(objItem, null,higherPackLevelPath+"/"+attrName, data);
							//objItem.setAttributeValue(higherPackLevelPath+"/"+attrName, data);
							uomFound =true;
							break;
						}

					}
					if(!uomFound && objItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).canAddOccurrence()) // If uom encountered while processing is not already present in item then a new occurrence will be added for new unit of measure
					{
						objLogger.logDebug("uom not found..so going to add occurrence..");
						AttributeInstance attrInstance = objItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).addOccurrence();
						String path = attrInstance.getPath()+"/"+attrName;
						objLogger.logDebug("path--"+path+"-----"+attrInstance.getPath()+"/"+Constant.UOM_PATH);
						objItem.setAttributeValue( attrInstance.getPath()+"/"+Constant.UOM_PATH, packlevelIdentifier);
						setAttribValue(objItem, null,path, data);
						//objItem.setAttributeValue(path, data);
					}
				}
			}

		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within setPackagingAttributes method exception caught.."+ex);
		}
	}

	/**
	 * Description: This method is used to copy global defined attributes (with some exceptions) from original Item to cloned Item
	 * parameters cloned item, original item
	 * @return void.
	 */

	private void copyOriginalPartCoreAndTemplateAttributesToClonedItem(CollaborationItem clonedItem, Item originalItem) {
		objLogger.logDebug("Within copyOriginalPartCoreAttributesToClonedItem method.."+originalItem.getPrimaryKey());
		try {
			AttributeDefinition attrDef = null;
			String attributePath = "";
			Object attributeValue = null;
			if(originalItem.getCategories(templateHierarchy).size() > 0) // Mapping original item mapped template categories to cloned item 
			{
				Collection<Category> catList = originalItem.getCategories(templateHierarchy);
				for(Category cat : catList)
				{
					try
					{
						clonedItem.mapToCategory(cat);
					}
					catch(UnsupportedOperationException uoe)
					{
						objLogger.logDebug("Exception occurred while mapping to category.."+uoe);
					}
				}
			}
			Collection<Spec> itemSpecList = clonedItem.getSpecs();
			objLogger.logDebug("Within copyOriginalPartCoreAttributesToClonedItem method..."+itemSpecList.size());

			//Spec primarySpec = productCatalog.getSpec(); // Since only global attributes will be copied, so taking primary spec into consideration
			LinkedHashMap<AttributeInstance, Object> originalPartData = new LinkedHashMap<AttributeInstance, Object>();
			for(Spec objSpec : itemSpecList) {
				objLogger.logDebug("Within copyOriginalPartCoreAttributesToClonedItem method...processing spec--"+objSpec.getName());
				List<AttributeDefinition> attribDefList = objSpec.getAttributeDefinitions();
				for(Iterator<AttributeDefinition> oItr = attribDefList.iterator();oItr.hasNext();) // Iterating through each attribute of Product Catalog Global Spec
				{
					attrDef = oItr.next();
					if(!clonedExceptionList.contains(attrDef.getName())) // Original Item values for few pre-defined set of items (Cloning Exception Attributes) will not be copied to Cloned Item
					{
						//objLogger.logDebug("Going to process attribute.."+attrDef.getName());
						getValuesForOriginalItem(attrDef,originalItem, originalPartData);
					}
				}
			}

			for(Map.Entry<AttributeInstance, Object> PropertyData  :originalPartData.entrySet()){ // All original Items global attributes path and data are iterated to copy them in Cloned item.
				attributePath = PropertyData.getKey().getPath();
				attributeValue = PropertyData.getValue();
				clonedItem.setAttributeValue(attributePath, attributeValue);

			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within copyOriginalPartCoreAttributesToClonedItem exception caught.."+ex);
		}


	}

	/**
	 * Description: This method is get all global attributes path and corresponding data from Original Item and are put in map. 
	 * parameters AttributeDefinition object, Original Item object, map object
	 * @return void.
	 */


	private void getValuesForOriginalItem(AttributeDefinition attrDef, Item originalItem, LinkedHashMap<AttributeInstance, Object> originalPartData) 
	{
		objLogger.logDebug("Within getValuesForOriginalItem method..");
		try
		{
			AttributeInstance attrInst = originalItem.getAttributeInstance(attrDef.getPath());
			objLogger.logDebug("Attribute Path is "+attrDef.getPath());

			if(attrDef.isGrouping()){ // If encountered attribute is a grouping then this logic will be executed
				objLogger.logDebug("Grouping");
				int occurrence = attrDef.getMaxOccurrence();

				for(int i = 0; i<occurrence;i++){

					AttributeInstance parentInst = originalItem.getAttributeInstance(attrDef.getPath()+"#"+i);

					if(parentInst != null){

						Iterator<? extends AttributeInstance> oInst = parentInst.getChildren().iterator();
						objLogger.logDebug(" Active Grouping Path is "+oInst);
						while(oInst.hasNext())
						{
							AttributeInstance childInst = oInst.next();
							objLogger.logDebug("Child Attribute Instance Path is  "+childInst.getPath());

							if(childInst!=null){
								if(childInst.getAttributeDefinition().isGrouping()) // If encountered attribute is a grouping then method will be called recursively till child attribute is retrieved.
								{
									objLogger.logDebug(" Child Attribute is Group Again");
									getValuesForOriginalItem(childInst.getAttributeDefinition(),originalItem, originalPartData);

								}
								else if(childInst.getValue() != null)  // If child attribute value is present then attribute path and corresponding data will be set in map
								{
									Object childVal = childInst.getValue();
									objLogger.logDebug("Putting path and value for grouping--"+childInst.getPath()+" and value--"+childVal);
									originalPartData.put(childInst, childVal);
									objLogger.logDebug(" Child Attribute  Value "+childVal);
								}
							}
						}
					}
				}
			}
			else if(attrInst!=null && attrInst.isMultiOccurrence())  // If encountered attribute is a multi-occurring then this logic will be executed
			{
				objLogger.logDebug("MultiOccurrence");
				Iterator<? extends AttributeInstance> oInst =attrInst.getChildren().iterator();

				while(oInst.hasNext()){

					AttributeInstance childInst = oInst.next();
					objLogger.logDebug(" Path is "+childInst.getPath());
					if(childInst.getValue() != null && !originalPartData.containsKey(childInst)) // If child attribute value is present and it is not added in map then attribute path and corresponding data will be set in map
					{
						Object attrVal =childInst.getValue();
						objLogger.logDebug("Putting path and value for multi occurence--"+childInst.getPath()+" and value--"+attrVal);
						originalPartData.put(childInst, attrVal);
						objLogger.logDebug("Child Attribute  Value "+attrVal);
					}
				}
			}
			else{ // If encountered attribute is neither grouping nor multi occurring, then this loop will be executed
				objLogger.logDebug("String");
				if(attrInst != null){
					objLogger.logDebug("Within String Path is "+attrInst.getPath()+" and attrInst.getValue() is--"+attrInst.getValue());
					if(attrInst.getValue() != null && !originalPartData.containsKey(attrInst))
					{
						Object attrVal =attrInst.getValue();
						objLogger.logDebug("Putting path and value for string--"+attrInst.getPath()+" and value--"+attrVal);
						originalPartData.put(attrInst, attrVal);
						objLogger.logDebug("Attribute  Value "+attrVal);
					}
				}
			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within getValuesForOriginalItem method exception caught.."+ex);
		}
	}


	/**
	 * Description: This method is used to delete all the existing occurrences of  Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels. This will be done when pack_level_change indicator is Yes in QAD generated feed file
	 * parameters Catalog item, Collaboration Item
	 * @return boolean.
	 */

	private boolean deleteExistingPackagingOccurrences(Item partItem, CollaborationItem colItem) {
		// TODO Auto-generated method stub
		objLogger.logDebug("Within deleteExistingPackagingOccurrences method.....");
		boolean occDeleted = false;
		try
		{
			if(null != partItem && partItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size() >0 && partItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size() >0) // If there are existing occurrence in Higher Packaging levels catalog item then all occurrences will be removed 
			{
				Iterator<? extends AttributeInstance> higherpackLevelsList = partItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().iterator();
				while(higherpackLevelsList.hasNext()) // iterating through each packaging hierarchy levels
				{
					AttributeInstance objInstance = higherpackLevelsList.next();
					objInstance.removeOccurrence();


				}
				occDeleted=true;
			}
			else if(null != partItem && partItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size() ==0) // If there is no occurrence of Packaging levels of catalog item then only boolean will be set to true.
			{
				occDeleted=true;
			}
			if(null != colItem && colItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size() >0 && colItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size() >0) // If there are existing occurrence in Higher Packaging levels collaboration item then all occurrences will be removed
			{
				Iterator<? extends AttributeInstance> higherpackLevelsList = colItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().iterator();
				while(higherpackLevelsList.hasNext())
				{
					AttributeInstance objInstance = higherpackLevelsList.next();
					objInstance.removeOccurrence();


				}
				occDeleted=true;
			}
			else if(null != colItem && colItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size() ==0) // If there is no occurrence of Packaging levels of collaboration item then only boolean will be set to true.
			{
				occDeleted=true;
			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within deleteExistingPackagingOccurrences method....."+ex);
			return false;
		}
		return occDeleted;

	}

	/**
	 * Description: This method is used to get catalog item object based on wql.
	 * parameters Part Number
	 * @return map of catalogitem  PK and collaborationitem PK.
	 */

	private Map<String,String> getItemObjectMap(String partNumber) {

		objLogger.logDebug("Within getItemObjectMap method..");

		Item objItem = null;
		String query = null;
		Item colItem = null;
		boolean collabItemFound = false;
		boolean ctgItemFound = false;
		CollaborationItem collabItem = null;
		Map<String, String> itemMap = new HashMap<String, String>();
		SearchQuery srchQuery = null;
		SearchResultSet resultSet = null;
		String itemPrimaryKey = "";
		String collabItemPrimKey = "";
		try
		{
			query = "select item from catalog('Product Catalog') where item['Product Catalog Global Spec/Part Number']='"+partNumber+"' and item['Product Catalog Global Spec/SystemOfRecord']='US-QAD'";
			objLogger.logDebug("Catalog Query is---"+query);
			srchQuery = ctx.createSearchQuery(query);
			resultSet = srchQuery.execute();
			objLogger.logDebug("-------------1-------------");
			while(resultSet.next()) // If single item is retrieved from result set then item object will be formed.
			{
				objLogger.logDebug("-------------2-------------");
				objItem = resultSet.getItem(1);
				objLogger.logDebug("-------------3-------------");
				ctgItemFound =true;
				itemPrimaryKey = objItem.getPrimaryKey();
				objLogger.logDebug("-------------4-------------"+itemPrimaryKey);
			}
			if(objItem != null)
			{
				collabItem = itemColArea.getCheckedOutItem(objItem.getPrimaryKey()); // if catalog item is checked out in collaboration area then collaboration item object will be formed.
				objLogger.logDebug("-------------5-------------");
				if(collabItem != null) {
					collabItemFound =true;
					collabItemPrimKey = collabItem.getPrimaryKey();
				}
			}
			else // if item is not present in catalog item, but is present in collaboration item then wql will be executed.
			{
				colItem = getCollaborationItem(partNumber);
				if(null != colItem) {
					collabItemFound = true;
					collabItemPrimKey = colItem.getPrimaryKey();
				}
			}

			objLogger.logDebug("colItem--"+colItem+" objectItem --"+objItem+" collabItem"+collabItem);
			if(ctgItemFound)
			{
				itemMap.put("ctgItem",itemPrimaryKey); // If catalog item is present then key=ctgItem value=PIM_ID
			}
			else
			{
				itemMap.put("ctgItem","null"); // If catalog item is not present then key=ctgItem value=null
			}
			if(collabItemFound) // If collaboration item is present then key=colItem value=PIM_ID
			{
				itemMap.put("colItem",collabItemPrimKey);
			}
			else // If catalog item is not present then key=colItem value=null
			{
				itemMap.put("colItem","null");
			}
			objLogger.logDebug("Item map size--"+itemMap.size());
			return itemMap;

		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within getItemObjectMap method...excepton caught.."+ex);
		}
		return null;
	}


	/**
	 * Description: This method is used to get collaboration item object based on wql.
	 * parameters Part Number
	 * @return collaboration item object.
	 */

	private Item getCollaborationItem(String partNumber) {

		objLogger.logDebug("Within getEntryFromColArea method..");
		Item objItem = null;
		boolean itemFound = false;
		CollaborationArea objColArea = null;
		List<CollaborationStep> stepList = null;
		try
		{
			String query = null;
			SearchQuery srchQuery = null;
			SearchResultSet resultSet = null;
			objColArea = ctx.getCollaborationAreaManager().getCollaborationArea(Constant.QADPARTSMAINTCOLAREANAME);
			stepList = objColArea.getSteps();
			for(CollaborationStep stepName : stepList) // Iterating through steps of Collaboration Area to form wql.
			{
				// This query will retrieve item object from Collaboration Area based on Part Number and SystemOfRecord

				query = "select item from collaboration_area('"+Constant.QADPARTSMAINTCOLAREANAME+"') where item['Product Catalog Global Spec/Part Number']='"+partNumber+"' and item['Product Catalog Global Spec/SystemOfRecord']='US-QAD'and item.step.path='"+stepName.getName()+"'";
				objLogger.logDebug("Query for colArea is---"+query);
				srchQuery = ctx.createSearchQuery(query);
				resultSet = srchQuery.execute();
				while(resultSet.next())
				{
					objItem = resultSet.getItem(1);
					itemFound=true;
				}
				if(itemFound) // If item is present in any step then breaking from the for loop to avoid further step iteration.
				{
					objLogger.logDebug("collab Item found..");
					break;
				}
			}
			//}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within getEntryFromColArea method..exception occurred..."+ex);
			return null;
		}
		return objItem;


	}

	/**
	 * Description: This method is used to populate Attributes Name and Attributes Path in a map. Attribute Name will be then compared with feed file headers and Path will be retrieved.
	 * Data will be used from properties file present at the server location.
	 * @return boolean value.
	 */

	private boolean populateAttributesDataInList()
	{
		objLogger.logDebug("Within populateAttributesData method..");
		String pimAttributeName = "";
		String pimAttributePath= "";
		try
		{
			File propertyFile = new File(System.getProperty(Constant.DOLLAR_TOP)+Constant.QAD_PIM_IMPORT_ATTRIBUTES_FILE_PATH); // Reading property file from Server location.
			objLogger.logInfo("In populateAttributesDataInList.."+propertyFile);

			//if(null != fileInputStream){
			BufferedReader br = new BufferedReader(new FileReader(propertyFile));
			qadpimAttributesMap = new LinkedHashMap<String, String>();
			String line= null;
			while((line = br.readLine())!= null) { // here line represents "Part Number=Product Catalog Global Spec/Part Number"
				pimAttributePath = line.split("=")[1];
				pimAttributeName =  line.split("=")[0];
				qadpimAttributesMap.put(pimAttributeName, pimAttributePath);
			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within populateAttributesData method...Exception occurred.."+ex);
			return false;
		}
		return true;
	}

	/**
	 * Description: This method is used to populate PIM Attributes Transferable exceptions in a list. This list will be used when attributes data are copied from original item to clone item.
	 * Data will be used from properties file present at the server location.
	 * @return void.
	 */

	private void populateCloneExceptionsList()
	{
		clonedExceptionList = new ArrayList<String>();
		clonedExceptionList.add("PIM_ID");
		clonedExceptionList.add("Part Number");
		clonedExceptionList.add("GDS Status");
		clonedExceptionList.add("AvailabilityEndDateTime");
		clonedExceptionList.add("AvailabilityStartDateTime");
		clonedExceptionList.add("Type");
		clonedExceptionList.add("Associations");
		clonedExceptionList.add("AssociatedParts");
		clonedExceptionList.add("ModelNumber");
		clonedExceptionList.add("CancellationDate");
		clonedExceptionList.add("ColorPhotoFileName_en-US");
		clonedExceptionList.add("ColorPhotoURL_en-US");
		clonedExceptionList.add("DirectReplacementItem-Validated");
		clonedExceptionList.add("DiscontinuationDate");
		clonedExceptionList.add("FirstNotificationToFergusonNoLongerAvailable");
		clonedExceptionList.add("FirstNotificationToWolseleyNoLongerAvailable");
		clonedExceptionList.add("FunctionallyEquivalentItem");
		clonedExceptionList.add("InstructionSheetFileName");
		clonedExceptionList.add("InstructionSheetURL");
		clonedExceptionList.add("LF-Agency_CertURL");
		clonedExceptionList.add("LF-NSF-ANSI-14_CertFileName");
		clonedExceptionList.add("LF-NSF-ANSI-14_CertURL");
		clonedExceptionList.add("LF-NSF-ANSI-372_CertFileName");
		clonedExceptionList.add("LF-NSF-ANSI-372_CertURL");
		clonedExceptionList.add("LF-NSF-ANSI-61-G_CertFileName");
		clonedExceptionList.add("LF-NSF-ANSI-61-G_CertURL");
		clonedExceptionList.add("LF-NSF-LowLeadCertProgram_CertFileName");
		clonedExceptionList.add("LF-NSF-LowLeadCertProgram_CertURL");
		clonedExceptionList.add("LF-WQA-NSFANSI-EquivalentStandard_CertFileName");
		clonedExceptionList.add("LF-WQA-NSFANSI-EquivalentStandard_CertURL");
		clonedExceptionList.add("ListPrice-CanadianDollars");
		clonedExceptionList.add("ListPriceNew-USADollars");
		clonedExceptionList.add("ListPricePCTChange-USADollars");
		clonedExceptionList.add("NewListPriceEffectiveDate");
		clonedExceptionList.add("OkForNonPotableWaterApplications");
		clonedExceptionList.add("PackagingException");
		clonedExceptionList.add("PIMComplete");
		clonedExceptionList.add("PrivateLabel_CustomerName");
		clonedExceptionList.add("RestrictDate");
		clonedExceptionList.add("ThirdPartyCertified");
		clonedExceptionList.add("WQACertified");
		clonedExceptionList.add("EANUCCCode");
		clonedExceptionList.add("ItemStatus");
		clonedExceptionList.add("LeadFree");
		clonedExceptionList.add("ListPrice-USADollars");
		clonedExceptionList.add("NSFSafetyListing");
		clonedExceptionList.add("PackageGTIN");
		clonedExceptionList.add("UniqueProductQualifier");
		clonedExceptionList.add("BuyAmericanAct");
		clonedExceptionList.add("HTSNumber");
		clonedExceptionList.add("TradingPartnerGLN-AcmePaper1Sync");
		clonedExceptionList.add("TradingPartnerGLN-CoburnSupply1Sync");
		clonedExceptionList.add("TradingPartnerGLN-EdwardDon1Sync");
		clonedExceptionList.add("TradingPartnerGLN-EMCO1Sync");
		clonedExceptionList.add("TradingPartnerGLN-GS1DatakwaliteitChecker1Sync");
		clonedExceptionList.add("TradingPartnerGLN-HDSupply1Sync");
		clonedExceptionList.add("TradingPartnerGLN-HomeDepot1Sync");
		clonedExceptionList.add("TradingPartnerGLN-Lowes1Sync");
		clonedExceptionList.add("TradingPartnerGLN-LowesCanada");
		clonedExceptionList.add("TradingPartnerGLN-OrchardSupply1Sync");
		clonedExceptionList.add("TradingPartnerGLN-TrueValue1Sync");
		clonedExceptionList.add("TradingPartnerGLN-ReinhartFoodserviceL.L.C.1Sync");
		clonedExceptionList.add("TradingPartnerGLN-UCCnet1Sync");
		clonedExceptionList.add("TradingPartnerGLN-WinWholesale1Sync");
		clonedExceptionList.add("ModelNumber_Unique");
		clonedExceptionList.add("AdditionalTradeItemDescription_en-US_Unique");
		clonedExceptionList.add("Description-Marketing_Unique");
		clonedExceptionList.add("EANUCCCode_Unique");
		clonedExceptionList.add("EA_PackageGTIN_Unique");
		clonedExceptionList.add("Inner_PackageGTIN_Unique");
		clonedExceptionList.add("Inner01_PackageGTIN_Unique");
		clonedExceptionList.add("Inner02_PackageGTIN_Unique");
		clonedExceptionList.add("Pallet_PackageGTIN_Unique");
		clonedExceptionList.add("Master_PackageGTIN_Unique");
		clonedExceptionList.add("Master01_PackageGTIN_Unique");
		clonedExceptionList.add("Master02_PackageGTIN_Unique");
		
		clonedExceptionList.add("CAProp65-InScope");
		clonedExceptionList.add("CAProp65-OutOfScopeReason");
		clonedExceptionList.add("CAProp65-CancerRiskSubstance");
		clonedExceptionList.add("CAProp65-DevelopRiskSubstance");
		clonedExceptionList.add("CAProp65-LastReviewDate");
		clonedExceptionList.add("CAProp65-ArtworkFileName");
		clonedExceptionList.add("CAProp65-ArtworkURL");
	}

	/**
	 * Description: This method is used to find the type of attribute and set the value for given catalog/collaboration item accordingly..
	 * @return void.
	 */

	private void setAttribValue(Item ctgItem,CollaborationItem collabItem, String attrPath, String value)
	{
		objLogger.logDebug("Within setAttributeValue method.."+attrPath+" ======"+value);
		AttributeInstance attrInstance = null;
		boolean isValidAttribute = true;
		try
		{
			if(ctgItem == null) // If item for which value to be set is collaboration item
			{
				try {
					attrInstance = collabItem.getAttributeInstance(attrPath);
				}
				catch(Exception ex)
				{
					objLogger.logDebug("Within setAttributeValue method...attribute  not found in spec for collaboration item.."+ex);
					isValidAttribute = false;
				}
				if(isValidAttribute) {
					String attrType = attrInstance.getAttributeDefinition().getType().toString();
					if(attrType.equals("STRING_ENUMERATION") ||attrType.equals("LOOKUP_TABLE") ) { // IF ATTRIBUTE TYPE IS STRING ENUMERATION OR LOOKUP TABLE THEN VALUE IN FEED FILE SHOULD MATCH POSSIBLE VALUES IN PIM
						objLogger.logDebug("attrInstance.getPossibleValues() for collaboration item--"+attrInstance.getPossibleValues());
						if(attrInstance.getPossibleValues().contains(value))
						{
							collabItem.setAttributeValue(attrPath, value);
						}
						else
						{
							objLogger.logDebug("Within setAttributeValue method...does not contain possible value..hence not setting..");
							collabItem.setAttributeValue(attrPath, value);
						}
					}
					else if(attrType.equals("INTEGER") ) 
					{
						if(value.contains(".")) // IF ATTRIBUTE TYPE IS INTEGER BUT DATA IN FEED FILE IS NUMBER THEN DECIMAL DIGIT WILL BE CHECKED AND CONVERTED TO INTEGER
						{
							String newVal = value.substring(0, value.indexOf("."));
							objLogger.logDebug("Within setAttributeValue method...newVal.."+newVal);
							collabItem.setAttributeValue(attrPath, newVal);
						}
						else
						{
							collabItem.setAttributeValue(attrPath, value);
						}
					}
					else    // IF ATTRIBUTE TYPE IS NUMBER OR STRING THEN FEED FILE VALUE WILL BE SET
					{
						collabItem.setAttributeValue(attrPath, value);
					}
					objLogger.logDebug("Within setAttributeValue method..attributeType for collabItem is---"+attrType);
				}
			}
			else // If item for which value to be set is catalog item
			{
				try {
					attrInstance = ctgItem.getAttributeInstance(attrPath);
				}
				catch(Exception ex)
				{
					objLogger.logDebug("Within setAttributeValue method...attribute not found in spec  for catalog item.."+ex);
					isValidAttribute = false;
				}
				if(isValidAttribute) 
				{
					String attrType = attrInstance.getAttributeDefinition().getType().toString();
					if(attrType.equals("STRING_ENUMERATION") ||attrType.equals("LOOKUP_TABLE") ) { // IF ATTRIBUTE TYPE IS STRING ENUMERATION OR LOOKUP TABLE THEN VALUE IN FEED FILE SHOULD MATCH POSSIBLE VALUES IN PIM
						objLogger.logDebug("attrInstance.getPossibleValues() for catalog item--"+attrInstance.getPossibleValues());
						if(attrInstance.getPossibleValues().contains(value))
						{
							ctgItem.setAttributeValue(attrPath, value);
						}
						else
						{
							objLogger.logDebug("Within setAttributeValue method...does not contain possible value..hence not setting..");
							ctgItem.setAttributeValue(attrPath, value);
						}
					}
					else if(attrType.equals("INTEGER") ) 
					{
						if(value.contains(".")) // IF ATTRIBUTE TYPE IS INTEGER BUT DATA IN FEED FILE IS NUMBER THEN DECIMAL DIGIT WILL BE CHECKED AND CONVERTED TO INTEGER
						{
							String newVal = value.substring(0, value.indexOf("."));
							objLogger.logDebug("Within setAttributeValue method...newVa123l.."+newVal);
							ctgItem.setAttributeValue(attrPath, newVal);
						}
						else
						{
							ctgItem.setAttributeValue(attrPath, value);
						}
					}
					else    // IF ATTRIBUTE TYPE IS NUMBER OR STRING THEN VALUE WILL BE SET
					{
						ctgItem.setAttributeValue(attrPath, value);
					}
					objLogger.logDebug("Within setAttributeValue method..attributeType  for catalog item11 is---"+attrType);
				}
			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within setAttributeValue method...exception caught.."+ex);
		}
	}

	/**
	 * Description: This method is used to transfer feed file from Inbound location to Archived location
	 * parameters inbound file full path, archived path
	 * @return void.
	 */

	private void archiveFile(String sourceFileName, String archivedirPath)
	{
		objLogger.logDebug("Within ArchiveFile method() : Inbox Location   :"+sourceFileName);
		objLogger.logDebug("Within ArchiveFile method() : Archive Location :"+archivedirPath);
		InputStream inStream   = null;
		OutputStream outStream = null;


		try{
			File sourceFile =new File(sourceFileName);
			File archiveDirFile = new File(archivedirPath);
			if (!archiveDirFile.exists()) {
				archiveDirFile.mkdir();
			}
			String archiveFileName = archivedirPath + sourceFile.getName(); 
			File destFile =new File(archiveFileName);

			inStream = new FileInputStream(sourceFile);
			outStream = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0){
				outStream.write(buffer, 0, length);
			}

			inStream.close();
			outStream.flush();
			outStream.close();
			boolean isDelete = sourceFile.delete();
			if(isDelete)
			{
				objLogger.logDebug("file successfully moved...");
			}
			else
			{
				objLogger.logDebug("file failed to move...");
			}

		}catch(Exception e){

			objLogger.logDebug("Within ArchiveFile method() :Exception is "+e.getMessage());

		}
	}

}
