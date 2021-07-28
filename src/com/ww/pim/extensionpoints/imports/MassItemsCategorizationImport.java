//***************************************************************
// File name      : MassItemsCategorizationImport
// File Type      : Java
// ---------------------------------------------------------------------------------------------------------------------
// Title : This code will be used to map/un-map part item to/from Customer Catalog Categories based on ACTIONS (ADD/REMOVE) 
// values from a csv type feed file. 
// File Format : 
//Part Number,SystemOfRecord,ACTION,Customer Catalog
//D9000122,Canada-QAD,ADD,Wolseley CVR
//D9000123,REMOVE,WinWholesale-IMDM
// Authors : Pankaj.Singh3@cognizant.com
// Company : Cognizant
// Date : 12/22/2017
// Description : 
// Assumptions : File Format currently used will remain unchanged during Phase 2 as well
// Usage :
// Audit :
// : First version 12/22/2017
// ---------------------------------------------------------------------------------------------------------------------
// $Header: /cvs/repo/
//**********************************************************************************************************************
// todo/notes/caveats: <Running Notes>

package com.ww.pim.extensionpoints.imports;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationArea;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ImportFunction;
import com.ibm.pim.extensionpoints.ImportFunctionArguments;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.search.SearchQuery;
import com.ibm.pim.search.SearchResultSet;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;

public class MassItemsCategorizationImport implements ImportFunction {

	private Context ctx = null;
	private Logger objLogger = null;
	private Hierarchy custCtgHierarchy = null;

	@Override
	public void doImport(ImportFunctionArguments arg0) {
		// TODO Auto-generated method stub

		String line = "";
		int counter = 0;
		String header[]= {};
		
		BufferedReader buffReader = null;

		try {
			ctx = PIMContextFactory.getCurrentContext();
			if(null != ctx) 
			{
				objLogger = ctx.getLogger(Constant.IMPORT_LOGGER_NAME);
				custCtgHierarchy = ctx.getHierarchyManager().getHierarchy(Constant.CUST_CAT_HIERARCHY);
				objLogger.logDebug("MassItemsCategorizationImport import started...!!!");
				String feedFilePath = arg0.getDocstoreDataDoc().getPath(); // This is PIM docstore path where feed file will be uploaded
				String filePath = feedFilePath.substring(11);
				objLogger.logDebug("Feedfile path--"+filePath);
				String serverFilePath = System.getProperty(Constant.DOLLAR_TOP)+"/feedfiles"+filePath; // Reading file from Server location which is mapped with PIM docstore.
				objLogger.logDebug("Server File Path is--"+serverFilePath);
				buffReader = new BufferedReader(new FileReader(serverFilePath));

				while((line =buffReader.readLine()) != null) // Iterating through each record of feed file
				{
					Item partItem = null;
					objLogger.logDebug("line is---"+line);
					String data[]= {};
					String partID = "";
					String systemOfRecord = "";
					String action = "";
					String custCatalogCategory = "";
					Category objCategory = null;
					boolean toBeSaved = false;
					ExtendedValidationErrors evErrorObject = null;
					if(counter == 0) {
						header	= line.split(Constant.COMMA);
						if(header.length == 4)
						{
							objLogger.logDebug("All 4 columns present..");
							if(!validateHeader(header)) // If column name is improper then file will not be processed.
							{
								objLogger.logDebug("Wrong header column name..");
								break;
							}
						}
						else   // If file header format is different then job will not be processed
						{
							objLogger.logDebug("Bad File format...");
							break;
						}
					}
					counter++;
					data = line.split(Constant.COMMA);
					if(data.length==4)
					{
						partID = isValueNull(data[0]);
						systemOfRecord = isValueNull(data[1]);
						action = isValueNull(data[2]);
						custCatalogCategory = isValueNull(data[3]);
						objLogger.logDebug(data[0]+" --"+data[1]+" --"+data[2]+" --"+data[3]);

						if((partID.equals(Constant.NULL)) || (systemOfRecord.equals(Constant.NULL)) || (action.equals(Constant.NULL)) || (custCatalogCategory.equals(Constant.NULL))) // If any of the value of the 4 columns is blank then entire record will be skipped
						{
							objLogger.logDebug("data in all 4 columns are mandatory..");
						}
						else if(!(action.equals(Constant.ACTION_ADD) || action.equals(Constant.ACTION_REMOVE))) // If ACTION COMMAND is not "ADD" or "REMOVE" then record will be skipped and no further processing will be required
						{
							objLogger.logDebug("Valid Action should be Present...");
						}
						else if(null == custCtgHierarchy.getCategoryByPrimaryKey(custCatalogCategory)) // If Category present in feed file does not exist in Hierarchy then record will be skipped
						{
							objLogger.logDebug("Valid Customer Catalog Category should be Present...");
						}
						else
						{
							objCategory = custCtgHierarchy.getCategoryByPrimaryKey(custCatalogCategory);
							partItem = getItemObject(partID,systemOfRecord); // Get Item Object from PIM
							objLogger.logDebug("Part item is--"+partItem);

							if(null != partItem)
							{
								if(Constant.ACTION_ADD.equals(action)) // If ACTION = ADD 
								{
									if(partItem.getCategories(custCtgHierarchy).size() > 0 && partItem.getCategories(custCtgHierarchy).contains(objCategory)) // IF Part item is already mapped to category that is present in feed file then record will be skipped
									{
										objLogger.logDebug("Item is already mapped to given category..");
									}
									else // Going to map Part item to Customer Catalog category
									{
										try {
											partItem.mapToCategory(objCategory);
											toBeSaved = true;
											objLogger.logDebug("Item mapped"+objCategory);
										}
										catch(UnsupportedOperationException exc)
										{
											objLogger.logDebug("Exception occurred while try to map item with a category.."+exc);
										}
									}
								}
								else if(partItem.getCategories(custCtgHierarchy).size() > 0) // If ACTION = REMOVE
								{

									if(partItem.getCategories(custCtgHierarchy).contains(objCategory)) // Going to remove Part Item from given Category based on feed file data
									{
										try {
											partItem.removeFromCategory(objCategory);
											toBeSaved = true;
										}
										catch(UnsupportedOperationException exc)
										{
											objLogger.logDebug("Exception occurred while try to map item with a category.."+exc);
										}
									}
									else  // IF Part item is not present in the category for which REMOVE has come then record will be skipped
									{
										objLogger.logDebug("Item is not mapped to given category for dissociation..");
									}
								}

								if(toBeSaved) // If Part item is mapped/unmapped to/from category then save action will be triggered
								{
									evErrorObject = partItem.save();
									//ctx.commit();
									objLogger.logDebug("Item saved11.."+evErrorObject);

									if(null != evErrorObject && evErrorObject.getErrors().size() > 0) // If item save fails then error will be logged.
									{
										String path = evErrorObject.getErrors().iterator().next().getAttributeInstance().getPath();
										objLogger.logDebug("Item save failed while saving attribute==="+path);
									}
								}
							}
							else // If item is not found in PIM
							{
								objLogger.logDebug("Item not found in PIM..");
							}
						}
					}
					else
					{
						objLogger.logDebug("All 4 records are mandatory...");
					}

				}
			}
			else
			{
				// Context object not found.
			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within catch Exception occurred.."+ex);
		}

		finally
		{
			if(null != ctx)
			{
				ctx.commit();
				ctx.cleanUp();
			}
			if(null != buffReader)
			{
				try {
					buffReader.close();
				} catch (IOException e) {
					
					objLogger.logDebug("Exception occurred while closing reader object..");
				}
			}
		}

	}
	
	/**
	 * Description: method is used to validate headers of the import file
	 * with the data present in the feed file
	 * @param Array of column headers
	 * @return boolean value
	 */

	private boolean validateHeader( String[] value)
	{
		objLogger.logDebug("Within validateHeader method..");
		objLogger.logDebug(value[0]+" "+value[1]+" "+value[2]+" "+value[3]);
		try
		{
			if(value[0].equals(Constant.COLUMN_PARTNUMBER) && value[1].equals(Constant.COLUMN_SYSRECOD) && value[2].equals(Constant.COLUMN_ACTION) && value[3].equals(Constant.COLUMN_CUSTCATALOG))
			{
				objLogger.logDebug("Within validateHeader method...correct file format..");
				return true;
			}
			else
			{
				objLogger.logDebug("Within validateHeader method..Wrong format file.");
			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within validateHeader method exception occurred---"+ex);
			return false;
		}
		return false;
	}

	/**
	 * Description: method is used to create item object from Product Catalog
	 * with the data present in the feed file
	 * @param Part Number, SystemOfRecord
	 * @return Item object
	 */
	private Item getItemObject(String partid, String systemID)
	{
		objLogger.logDebug("Within getItemObject method..");
		Item objItem = null;
		String query = null;
		SearchQuery srchQuery = null;
		SearchResultSet resultSet = null;
		try
		{
			// This query will retrieve item object from Product Catalog based on Part Number and SystemOfRecord
			query = "select item from catalog('Product Catalog') where item['Product Catalog Global Spec/Part Number']='"+partid+"' and item['Product Catalog Global Spec/SystemOfRecord']='"+systemID+"'";
			objLogger.logDebug("Query is---"+query);
			srchQuery = ctx.createSearchQuery(query);
			objLogger.logDebug("1");
			resultSet = srchQuery.execute();
			objLogger.logDebug("2");
			while(resultSet.next()) // If single item is retrieved from result set then item object will be formed.
			{
				objLogger.logDebug("3");
				objItem = resultSet.getItem(1);
			}

			if(null == objItem || objItem.isCheckedOut() )// If item is not fetched from Product Catalog or item is checked out in workflow.
			{
				objItem = getEntryFromColArea(partid,systemID);
			}
		}
		catch(Exception ex)
		{
			objLogger.logDebug("Within getItemObject method..exception occurred.."+ex);
			return null;
		}
		return objItem;
	}

	/**
	 * Description: method is used to create item object from any of the 3 relevant Collaboration Areas
	 * with the data present in the feed file
	 * @param Part Number, SystemOfRecord
	 * @return Item object
	 */

	private Item getEntryFromColArea(String partID, String sysID)
	{
		objLogger.logDebug("Within getEntryFromColArea method..");
		Item objItem = null;
		boolean itemFound = false;
		CollaborationArea objColArea = null;
		List<CollaborationStep> stepList = null;
		try
		{
			//List<String> colAreaList = new ArrayList<String>();
			// All the three relevant Collaboration Areas are added in the array list.
			//colAreaList.add(Constant.PRDINTROCOLAREANAME);
			//colAreaList.add(Constant.PRDMAINTCOLAREANAME);
			//colAreaList.add(Constant.NONFINISHEDCOLAREANAME);
			//for(String colAreaName : colAreaList) // Now iterating through each of collaboration areas
			//{
				String query = null;
				SearchQuery srchQuery = null;
				SearchResultSet resultSet = null;
				objColArea = ctx.getCollaborationAreaManager().getCollaborationArea(Constant.QADPARTSMAINTCOLAREANAME);
				stepList = objColArea.getSteps();
				for(CollaborationStep stepName : stepList) // Iterating through steps of Collaboration Area to form wql.
				{
					// This query will retrieve item object from Collaboration Area based on Part Number and SystemOfRecord

					query = "select item from collaboration_area('"+Constant.QADPARTSMAINTCOLAREANAME+"') where item['Product Catalog Global Spec/Part Number']='"+partID+"' and item.step.path='"+stepName.getName()+"' and item['Product Catalog Global Spec/SystemOfRecord']='"+sysID+"'";
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
						objLogger.logDebug("Item found..");
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
	 * Description: this method is used to check the null data present in the feed file
	 * @param feed file column data
	 * @return String
	 */


	private String isValueNull(String data)
	{

		if(data.equals(""))
		{
			return Constant.NULL;
		}
		return data;
	}

}
