//***************************************************************
// File name      : Ferguson_Net_Change_Export_Impl
// File Type      : Java
// ---------------------------------------------------------------------------------------------------------------------
// Title : This code will extract all the mapped items of Ferguson Categories of Customer Catalogs Hierarchy and will write pipe separated attribute 
// Value in for set of Ferguson defined attributes in a text file.
// For Each category a new text file will be generated at the docstore location and this file will have pipe separated values of all the mapped items.
// This file will also have STATUS as ADDED, DELETED, UPDATED, UNCHANGED for each item in a given file.
// ADDED = new item has been mapped to a given Category with respect to last run.
// DELETED =  item has been removed from a given Category with respect to last run.
//UPDATED = Item attributes (Ferguson attributes) data have been updated with respect to last run
//UNCHANGED = No change in data for a given item with respect to last run.

// Generated File Format : 
//Part Number|SystemOfRecord|DESCRIPTION|ItemStatus|ProductLine|ProductManager|ProductManager-Non-USQAD|Drawing|ModelNumber|TEMPLATE|Inner Pack Width|Obsolete/stock available|Country of Origin|MISC 2|Product Description|Product Family Name|ORMD Y/N|Inner Pack Length|Number=Mfg Product Number|MISC 3|Substitution item|Case Weight|Each Width|Kit Y/N|Case Quantity|Shelf Life Y/N|Manufacturer's Published Suggested List Price_corresponds to unit of measure|Each Weight|Minimum Order Quantity|Each Length|Lead Law Compliant Y, N , N/A|Qty per base unit of measure|Case Height|Inner Pack Weight|Price/ invoice Unit of Measure|Hazmat Y/N|Inner Pack Quantity|UPC/GTIN|Each Height|MSDS Y/N|Lead Law Third Party Certified|CEC Listed Y, N, N/A?|Case Width|Serial Number Required|Can This Item Be Used In Non-Potable Applications?  Yes or No|Inner Pack Height|Direct replacement item|Case Length|Product Returnable Y/N|Mfg/DivName|Case GTIN Number|Obsolete Date / Date Production Ceased|Base Model Number|Product Discount Category/Marketing Group|Inner Pack GTIN Number|Compliant Product Substitute
//0958102|US-QAD|1 In Stainless Steel Manifold Kit, 2 Circuits, Compression Fittings|AC|K701|BREAULMA|||MAN,M2,FLOW, 1 IN||12.25||DE|1 In Stainless Steel Manifold Kit, 2 Circuits, Compression Fittings|1 In Stainless Steel Manifold Kit, 2 Circuits, Compression Fittings||N|13.5||||50.0|12.25|N|10|Y|317.5|5.0|1|13.5|Not In Scope|1|33.6|10.0|EACH CONSUMER UNIT|NO_MSDS_AND_NOT_REGULATED_BY_DOT_(CFR49)|2|098268453560|3.5|N||Does Not Apply|12.25|N||7.0||13.5|Call for Authorization|Watts Radiant|20098268453564||MAN,M2,FLOW, 1 IN|211|10098268453567|
// Authors : Pankaj.Singh3@cognizant.com
// Company : Cognizant
// Creation Date : 1/2/2018
// Description : 
// Assumptions : File generated docstore location will not change and data format will also be used from Phase 1
// Usage :
// Audit :
// : First version 1/2/2018
// ---------------------------------------------------------------------------------------------------------------------
// $Header: /cvs/repo/
//**********************************************************************************************************************
// todo/notes/caveats: <Running Notes>

package com.ww.pim.extensionpoints.reports;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.common.ValidationErrors;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.lookuptable.LookupTable;
import com.ibm.pim.lookuptable.LookupTableEntry;
import com.ibm.pim.search.SearchQuery;
import com.ibm.pim.search.SearchResultSet;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;

public class Ferguson_Net_Change_Export_Impl implements ReportGenerateFunction {

	private Context ctx = null;
	private Logger objLogger = null;
	private List<String> productAttributes = null;
	private List<String> fergusonAttributes = null;
	private Hierarchy custCatalogHierarchy = null;
	private Catalog productCatalog = null;
	private LookupTable netExportLookupTble = null;
	private List<String> currentItemList = null;

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		// TODO Auto-generated method stub

		try
		{
			ctx = PIMContextFactory.getCurrentContext();
			objLogger = ctx.getLogger(Constant.IMPORT_LOGGER_NAME);
			objLogger.logDebug("Ferguson Net Change Export Report Started..");
			productAttributes = new ArrayList<String>();
			fergusonAttributes = new ArrayList<String>();
			addCommonProductAttributesinList();
			custCatalogHierarchy = ctx.getHierarchyManager().getHierarchy(Constant.CUST_CAT_HIERARCHY);
			productCatalog = ctx.getCatalogManager().getCatalog(Constant.PRODUCT_CATALOG);
			netExportLookupTble = ctx.getLookupTableManager().getLookupTable("WPT_Net_Export_LKP");
			String customerCtlgCat = "";
			String custCatList[]= {};
			Category objCustCategory = null;
			PIMCollection<Item> itemList = null;
			Date objDate = null;
			DateFormat dateFormat = null;
			Document exportDoc = null;
			String headerValue = "";

			// Calling populateAttributesDataInList method to populate Ferguson and product Attributes in two different Lists

			if(populateAttributesDataInList())
			{
				Map customerCategoryList = arg0.getInputs(); // Fetch Customer Catalogs categories ("|" separated) from report input parameter
				customerCtlgCat = (String)customerCategoryList.get(Constant.CUST_CTG_HIER);
				objLogger.logDebug("customerCatalogs Category---"+customerCtlgCat);
				custCatList = customerCtlgCat.split("\\|");
				for(String custCategory : custCatList)
				{
					objLogger.logDebug("customerCatalog Category---"+custCategory);
					if(null != custCatalogHierarchy.getCategoryByPrimaryKey(custCategory))
					{
						objDate = new Date();
						String deletedItemsInfo = "";
						dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
						// This document object will store all the mapped items attribute values for a given Ferguson Category.
						exportDoc = ctx.getDocstoreManager().createAndPersistDocument(Constant.FERG_EXPORT_PATH+custCategory+"_Net Change_"+dateFormat.format(objDate)+".txt");
						headerValue = createHeaderInExportFile();
						objLogger.logDebug("headerValue--"+headerValue);
						objCustCategory = custCatalogHierarchy.getCategoryByPrimaryKey(custCategory); // Creating a category object by reading it from input report parameter.
						itemList = objCustCategory.getItems(productCatalog);  // Fetching all the items of relevant Ferguson Category
						String dataListForItem = "";
						currentItemList = new ArrayList<String>();

						for(Item objItem : itemList) // Now iterating through all the mapped items
						{
							dataListForItem = dataListForItem+getAttributesDataListForItem(objItem,custCategory);
							objLogger.logDebug("Data List For Item---"+objItem.getPrimaryKey()+" is:"+dataListForItem);
							objLogger.logDebug("a");
							if(null != objItem.getAttributeValue("Product Catalog Global Spec/Part Number"))
							{
								// Adding all the items in a list present in a category for current run to compare with last run..
								currentItemList.add((String)objItem.getAttributeValue("Product Catalog Global Spec/Part Number"));
							}
						}
						objLogger.logDebug("Total item size--"+currentItemList.size());
						// Calling writeDeletedItemsInExportFile() method to check the list of items that were deleted (un-mapped) with respect to last run.
						deletedItemsInfo = writeDeletedItemsInExportFile(currentItemList, custCategory);
						exportDoc.setContent(headerValue+dataListForItem+deletedItemsInfo); // Writing all relevant mapped items attribute data along with status in docstore file.

					}
					else
					{
						objLogger.logDebug("customerCatalog Category "+custCategory+" not found.");
					}
				}

			}

		}
		catch(Exception ex)
		{
			objLogger.logDebug("Exception occurred in report class.."+ex);
		}
		finally
		{
			if(null != ctx)
			{
				ctx.commit();
				ctx.cleanUp();
				productAttributes.clear();
				fergusonAttributes.clear();
				objLogger.logDebug("Ferguson Net Change Export Report Ended..");
			}
		}

	}

	/**
	 * Description: This method is used to fetch an item attributes data  that is deleted from a given category with respect to last run
	 * Parameters item list, Category String
	 * @return boolean value.
	 */


	private String writeDeletedItemsInExportFile(List<String> itemList, String custCategory) {

		objLogger.logDebug("Within writeDeletedItemsInExportFile method..");
		StringBuffer deletedValues = new StringBuffer();
		//deletedValues.append("\n");
		Item lkpItem = null;
		String attributesData = "";
		String lkpItemID="";
		try
		{
			String query = null;
			SearchQuery srchQuery = null;
			SearchResultSet resultSet = null;
			// This query will fetch all the items for a given category.
			query = "select item from catalog('WPT_Net_Export_LKP') where item['Net_Export_Lookup_Spec/Category_ID']='"+custCategory+"'";
			objLogger.logDebug("Query is---"+query);
			srchQuery = ctx.createSearchQuery(query);
			resultSet = srchQuery.execute();
			while(resultSet.next()) // If single item is retrieved from result set then item object will be formed.
			{
				lkpItem = resultSet.getItem(1);
				lkpItemID = (String)lkpItem.getAttributeValue("Net_Export_Lookup_Spec/Item_ID");
				objLogger.logDebug("Within writeDeletedItemsInExportFile method..."+lkpItemID);
				if(!itemList.contains(lkpItemID)) // If item ID for a given category is present in Lookup_Table but not in current export run, then that item has been unmapped and needs to be captured in export file
				{
					objLogger.logDebug("Within writeDeletedItemsInExportFile method..This item "+lkpItemID+" has been deleted from category "+custCategory+" so it will be deleted from lkp table and written in export file");
					attributesData = (String)lkpItem.getAttributeValue("Net_Export_Lookup_Spec/Attributes_Data")+"DELETED"; // Fetching the item attributes data from Lookup_Table with STATUS = DELETED
					deletedValues.append(attributesData);
					lkpItem.delete(); // Deleting given entry from lookup table since it has been deleted (un-mapped) in  a current run.
				}
			}
			objLogger.logDebug("Within writeDeletedItemsInExportFile method.."+deletedValues.toString());
			return deletedValues.toString();
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within writeDeletedItemsInExportFile method...exception caught..");
			return "";
		}


	}

	/**
	 * Description: This method is used to populate Ferguson Attributes and mapped Product Attributes in a list
	 * Data will be used from properties file present at the docstore location.
	 * @return boolean value.
	 */

	@SuppressWarnings("unchecked")
	private boolean populateAttributesDataInList()
	{
		objLogger.logDebug("Within populateAttributesData method..");
		String fergusonAttribute = "";
		String productAttribute = "";
		try
		{
			String propertyFile = System.getProperty(Constant.DOLLAR_TOP)+Constant.FERGUSON_ATTR_PROPERTY_NEW_FILE_PATH; // Reading property file from Server location.
			objLogger.logInfo("In populateAttributesDataInList.."+propertyFile);
			BufferedReader br = new BufferedReader(new FileReader(propertyFile));
			if(null != br){
				objLogger.logInfo("In populateAttributesDataInList  :"+br);
				String line = "";
				while((line = br.readLine()) != null ){
					String[] entry = line.split("=");
					fergusonAttributes.add(entry[1].trim());
					productAttributes.add(entry[0].trim());
				}
			}
			objLogger.logInfo("fergusonAttributes size---"+fergusonAttributes.size() + "  productAttributes size---" + productAttributes.size());
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within populateAttributesData method...Exception occurred.."+ex);
			return false;
		}
		return true;
	}

	/**
	 * Description: method is used to create pipe separated header that will be written in the text file generated at the docstore location
	 * with the data present in the feed file
	 * @return pipe separated Header
	 */

	private String createHeaderInExportFile()
	{
		objLogger.logDebug("Within createHeaderInExportFile method...");
		StringBuffer sb = new StringBuffer();
		String header = "";
		try
		{
			for(String fergusonAttribute : fergusonAttributes) // Iterating through Ferguson Attributes to create a pipe separated headers in text file
			{
				sb.append(fergusonAttribute);
				//objLogger.logDebug("Within createHeaderInExportFile method...fergusonAttribute"+fergusonAttribute);
				sb.append(Constant.PIPE);
			}
			objLogger.logDebug("data--"+sb.toString());
			header = sb.toString()+"CHANGE STATUS";
			objLogger.logDebug("Within createHeaderInExportFile method..."+header);
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within createHeaderInExportFile method...exception caught.."+ex);
		}
		return header;
	}

	/**
	 * Description: This method will fetch all the PIM defined attributes for a given item (mapped to Ferguson Category) and will store the data 
	 * in a pipe separated format.
	 * Attributes like PackageWeight, PackageDepth, PackageHeight are applicable for Each, Inner as well as Case level attributes but their spec paths are different.
	 * @param Item object
	 * @return Array of data
	 */

	private String getAttributesDataListForItem(Item objItem, String catName)
	{
		objLogger.logDebug("Within getAttributesDataListForItem method...");
		StringBuffer valueList = new StringBuffer();
		valueList.append("\n");
		String productAttributeValue = "";
		String attributeName = "";
		String changeStatus = "";
		try
		{
			for(String productAttribute : productAttributes)  // Iterate through each PIM defined attribute present in the productAttributes list to fetch value from a given item
			{
				objLogger.logDebug("Within getAttributesDataListForItem method...productAttribute--"+productAttribute);
				productAttributeValue = "";
				if(productAttribute.startsWith(Constant.INNER_ID) || productAttribute.startsWith(Constant.MASTER_ID) || productAttribute.startsWith(Constant.PALLET_ID)) // If MDM attribute is prefixed with INNER or MASTER(case)(like INNER:PackageWidth, MASTER:PackageWeight etc) then appropriate spec path of given attribute has to be fetched to get its value
				{
					if(objItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size() >0 && objItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size() >0) // Since INNER level and MASTER level information are stored under different spec path, and they might/might not be present, so checking its instances
					{
						Iterator<? extends AttributeInstance> higherpackLevelsList = objItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
						while(higherpackLevelsList.hasNext())
						{
							AttributeInstance objInstance = higherpackLevelsList.next();
							String higherPackLevelPath = objInstance.getPath();
							if(null != objItem.getAttributeValue(higherPackLevelPath+"/"+Constant.UOM_PATH))
							{
								String packIdentifier = (String)objItem.getAttributeValue(higherPackLevelPath+"/"+Constant.UOM_PATH); // This will determine if path is INNER defined or CASE (MASTER) defined.
								if(packIdentifier.equalsIgnoreCase(productAttribute.split("\\:")[0]))
								{
									attributeName = productAttribute.split("\\:")[1];
									if(null !=objItem.getAttributeValue(higherPackLevelPath+"/"+attributeName) ) {
										productAttributeValue = (objItem.getAttributeValue(higherPackLevelPath+"/"+attributeName)).toString();
									}
									//objLogger.logDebug("Within getAttributesDataListForItem method..fergusonAttributeValue for inner/master Level."+productAttributeValue+" for attribute--"+higherPackLevelPath);
									break;
								}

							}
						}
					}
				}
				else if(!productAttribute.contains("Product Catalog Global Spec") && !productAttribute.equals(Constant.FERGUSON_EMPTY_ATTRS)) // This check is for common attributes that are referred both by Ferguson as well as Product Attributes
				{
					
					if (productAttribute.equals("TEMPLATE")){
						objLogger.logInfo("Templete Attribute encountered");
						Collection<Category> templateCatList = objItem.getCategories(ctx.getHierarchyManager().getHierarchy("Templates Hierarchy"));
						if (null != templateCatList && templateCatList.size() == 1){
							Category category = (Category) templateCatList.iterator().next();
							String catPath = category.getFullPaths(false).toArray()[0].toString();
							productAttributeValue = catPath.replace("/", " > ");
							
						}
					}
					else if(null != objItem.getAttributeValue("Product Catalog Global Spec/"+productAttribute)) {
						productAttribute = "Product Catalog Global Spec/"+productAttribute;
						productAttributeValue = (objItem.getAttributeValue(productAttribute)).toString();
						objLogger.logDebug("Within getAttributesDataListForItem method..for product Attribute "+productAttribute+" value is--"+productAttributeValue);
					}
				}
				else // If PIM attribute is not prefixed with EA/INNER/MASTER (like Part Number, SystemOfRecord etc) then value is fetched through normal iteration.
				{
					if (!productAttribute.equals(Constant.FERGUSON_EMPTY_ATTRS)){
						if(null != objItem.getAttributeValue(productAttribute)) {
							productAttributeValue = (objItem.getAttributeValue(productAttribute)).toString();
							//objLogger.logDebug("Within getAttributesDataListForItem method..for product Attribute "+productAttribute+" value is--"+productAttributeValue);
						}
					}
				}

				valueList.append(productAttributeValue);
				valueList.append(Constant.PIPE);
			}
			objLogger.logDebug("Within getAttributesDataListForItem method...."+valueList);

			// Calling getStatusChangeInfo() method to compare current exported data of a given item with that of a last run export.
			changeStatus = getStatusChangeInfo((String)objItem.getAttributeValue("Product Catalog Global Spec/Part Number"),catName,valueList.toString());
			objLogger.logInfo("changeStatus------------------------->"+changeStatus);
			return valueList.append(changeStatus).toString();
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within getAttributesDataListForItem method...exception caught.."+ex);
			return "";
		}
	}

	/**
	 * Description: This method is used to get Status Change for a given Item i.e., ADDED, UPDATED, UNCHANGED
	 * Parameters are current category and item
	 * @return String value.
	 */

	private String getStatusChangeInfo(String itemID, String catName, String currentValue) {

		objLogger.logDebug("Within getStatusChangeInfo method.."+itemID);
		String status = "";
		LookupTableEntry lkpEntry = null;
		Item lkpItem = null;
		try
		{
			String query = null;
			SearchQuery srchQuery = null;
			SearchResultSet resultSet = null;
			ValidationErrors errorList = null;
			String lookupItemAttribValue = "";

			//  This query will return list of entries for a given category and item
			query = "select item from catalog('WPT_Net_Export_LKP') where item['Net_Export_Lookup_Spec/Category_ID']='"+catName+"' and item['Net_Export_Lookup_Spec/Item_ID']='"+itemID+"'";
			objLogger.logDebug("Query is---"+query);
			srchQuery = ctx.createSearchQuery(query);
			resultSet = srchQuery.execute();
			while(resultSet.next()) // If single item is retrieved from result set then item object will be formed.
			{
				lkpItem = resultSet.getItem(1);
			}
			if(lkpItem == null) // If there is no entry in WPT_Net_Export_LKP then a new entry will be created and STATUS will be ADDED in export file
			{
				objLogger.logDebug("Within getStatusChangeInfo method...catalog item not found..so creating in catalog.."+catName+"  "+itemID+"  "+currentValue);
				lkpEntry = netExportLookupTble.createEntry(); 
				lkpEntry.setAttributeValue("Net_Export_Lookup_Spec/Category_ID", catName);
				lkpEntry.setAttributeValue("Net_Export_Lookup_Spec/Item_ID", itemID);
				lkpEntry.setAttributeValue("Net_Export_Lookup_Spec/Attributes_Data", currentValue);
				errorList = lkpEntry.save();
				objLogger.logDebug("Within getStatusChangeInfo method lkp saved...."+errorList+" and id is--"+lkpEntry.getKey());
				if(null  == errorList)
				{
					objLogger.logDebug("Entry saved successfully..");
					status = "ADDED";
				}

			}
			else // If there is an entry for a given category and item then exported value present in lookup_table will be verified with current exported value
			{
				objLogger.logDebug("Within getStatusChangeInfo method..lkp item found..");
				lkpEntry = netExportLookupTble.getLookupTableEntry(lkpItem.getPrimaryKey());
				lookupItemAttribValue = (String)lkpEntry.getAttributeValue("Net_Export_Lookup_Spec/Attributes_Data");
				//objLogger.logInfo("lookupItemAttribValue-------------->"+lookupItemAttribValue);
				//objLogger.logInfo("currentValue-->"+currentValue);
				if(lookupItemAttribValue.equals(currentValue)) // If current value is same as lookup_table value then STATUS = UNCHANGED
					
				{
					objLogger.logDebug("Within getStatusChangeInfo method..string value same so status = UNCHANGED");
					status = "UNCHANGED";
				}
				else  // If current value is different as lookup_table value then STATUS = UPDATED
				{
					lkpEntry.setAttributeValue("Net_Export_Lookup_Spec/Attributes_Data",currentValue); // Updating the entry with latest Attributes_Data in lookup table.
					errorList = lkpEntry.save();
					objLogger.logInfo("--------------------->");
					if(errorList == null)
					{
						objLogger.logDebug("Within getStatusChangeInfo method..string value changed so status = UPDATED");
						status = "UPDATED";
					}

				}
			}
			return status;

		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within getItemObject method..exception occurred.."+ex);
			return "";
		}
	}

	/**
	 * Description: this method will add all the common attributes of an item in list and consequently this list will be added to Ferguson as well as Customer Attributes
	 * with the data present in the feed file
	 * @param Part Number, SystemOfRecord
	 * @return void
	 */

	private void addCommonProductAttributesinList()
	{
		List<String> commonAttributesList = new ArrayList<String>();
		commonAttributesList.add(Constant.CONST_PART_NO);
		commonAttributesList.add(Constant.CONST_SYS_RECORD);
		commonAttributesList.add(Constant.CONST_DESC);
		commonAttributesList.add(Constant.CONST_ITEM_STATUS);
		commonAttributesList.add(Constant.CONST_PRDLINE);
		commonAttributesList.add(Constant.CONST_PRDMGR);
		commonAttributesList.add(Constant.CONST_PRDMGR_NONUSQAD);
		//commonAttributesList.add(Constant.CONST_DRAWING);
		commonAttributesList.add(Constant.CONST_MODELNO);
		commonAttributesList.add("TEMPLATE");
		fergusonAttributes.addAll(commonAttributesList);  // Adding all the common attributes in Ferguson Attributes list
		productAttributes.addAll(commonAttributesList);	 //  Adding all the common attributes in PIM mapped Attributes list

	}
}
