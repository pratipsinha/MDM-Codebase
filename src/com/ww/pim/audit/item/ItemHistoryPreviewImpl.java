package com.ww.pim.audit.item;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationHistoryEvent;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationObject;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.extensionpoints.CategoryPreviewFunctionArguments;
import com.ibm.pim.extensionpoints.CollaborationCategoryPreviewFunctionArguments;
import com.ibm.pim.extensionpoints.CollaborationItemPreviewFunctionArguments;
import com.ibm.pim.extensionpoints.EntryPreviewFunction;
import com.ibm.pim.extensionpoints.ItemPreviewFunctionArguments;
import com.ibm.pim.history.HistoryManager;
import com.ibm.pim.lookuptable.LookupTable;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.ActionType;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.common.utils.PIMUtils;

public class ItemHistoryPreviewImpl implements EntryPreviewFunction
{

	@SuppressWarnings("unused")
	public void entryPreview(ItemPreviewFunctionArguments inArgs) 
	{
		PIMCollection<Item> items = inArgs.getItems();
		int itemID = -1;
		int containerId = -1;
		Item item = null;
		if(items != null && items.size() > 0 && items.iterator().hasNext()) {
			item = items.iterator().next();
			itemID = item.getId();
		}
		Context ctx = PIMUtils.getCurrentContext();
		Logger objLogger =  ctx.getLogger(Constant.LOGGER_NAME);
		String currentUserName = ctx.getCurrentUser().getUserName();
		//String pimUserID = PIMUtils.getUserIdByName(currentUserName);
		//String userIDStringValConcatPIMUserID= "<user_id>"+pimUserID;

		objLogger.logDebug("ItemHistoryPreviewImpl For Catalog Item Started for user..1111!!!!"+currentUserName);

		List<String> primaryKey = new ArrayList<String>();
		String PIM_ID = item.getAttributeValue(PIMUtils.PIM_ID_ATTRIBUTE).toString();
		System.out.println("PIM_ID for item " + PIM_ID+ " : "+ item.getPrimaryKey());
		if(item != null){
			primaryKey.add(item.getPrimaryKey());
		}
		HistoryManager historyMgr = ctx.getHistoryManager();
		List<String> historyList = historyMgr.searchFullHistoryForEntry(primaryKey, PIMUtils.getHistoryStartDate(), PIMUtils.getHistoryEndDate());
		//objLogger.logDebug("From History Manager CollabHistory::"+historyList);

		//List<String> historyList = historyMgr.searchHistory(new int[] {itemID},Item.OBJECT_TYPE, PIMUtils.getHistoryStartDate(), PIMUtils.getHistoryEndDate());
		//System.out.println("From History Manager ItemHistory::"+historyList);
		//List<String> collabhistoryList = historyMgr.searchHistory(new int[] {itemID},CollaborationItem.OBJECT_TYPE, PIMUtils.getHistoryStartDate(), PIMUtils.getHistoryEndDate());
		//System.out.println("From History Manager CollabHistory::"+collabhistoryList);
		/*Catalog associatedCatalog = item.getCatalog();
		Collection<ItemCollaborationArea> collabAreas = associatedCatalog.getCollaborationAreas();
		if(collabAreas != null && collabAreas.size() > 0 && collabAreas.iterator().hasNext()) {
			CollaborationArea collabArea = collabAreas.iterator().next();
			containerId = collabArea.getId();
			System.out.println("containerId:" + containerId);
			List<String> containerhistoryList = historyMgr.searchHistory(containerId,Item.OBJECT_TYPE, null,PIMUtils.getHistoryStartDate(), PIMUtils.getHistoryEndDate());
			System.out.println("From History Manager containerhistoryList::"+containerhistoryList);
			List<String> containerhistoryList1 = historyMgr.searchHistory(containerId,CollaborationItem.OBJECT_TYPE, null,PIMUtils.getHistoryStartDate(), PIMUtils.getHistoryEndDate());
			System.out.println("From History Manager containerhistoryList for collabItem::"+containerhistoryList1);
		}*/

		PrintWriter out = inArgs.getOutput();
		if(item == null) {
			objLogger.logDebug("Item is null");
			printNoHistoryMessage(out, PIMUtils.NEW_ITEM_NO_ITEM_HISTORY);
			return;
		}
		//if(historyList == null || historyList.size() == 0 || !historyList.toString().contains(userIDStringValConcatPIMUserID)) {
		if(historyList == null || historyList.size() == 0 ) {
			objLogger.logDebug("History is null.....");
			printNoHistoryMessage(out, PIMUtils.NEW_ITEM_NO_ITEM_HISTORY);
			return;
		}
		//To display PIM_ID if UniqueProductQualifier is blank
		String UPQ = (String) item.getAttributeValue(PIMUtils.UNIQUE_PRODUCT_QUALIFIER_ATTRIBUTE);
		if(StringUtils.isNotBlank(UPQ)){
			out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_ITEM_HISTORY, new String[]{item.getAttributeValue(PIMUtils.UNIQUE_PRODUCT_QUALIFIER_ATTRIBUTE).toString()})));
		}
		else{
			out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_ITEM_HISTORY, new String[]{item.getAttributeValue(PIMUtils.PIM_ID_ATTRIBUTE).toString()})));
		}

		try {
			List<String> collabHistoryContent = new ArrayList<String>();
			for (int i=historyList.size()-1;i>=0;i--) {
				String historyContent = historyList.get(i);
				String partNumber = (String) item.getAttributeValue(PIMUtils.PART_NUMBER_ATTRIBUTE);
				String systemOfRecord = (String) item.getAttributeValue(PIMUtils.SYSRECORD_ATTRIBUTE);
				if(StringUtils.isBlank(partNumber)){
					partNumber = PIMUtils.NULL_ATTRIBUTE_VALUE;
				}
				if(StringUtils.isBlank(systemOfRecord)){
					systemOfRecord = PIMUtils.NULL_ATTRIBUTE_VALUE;
				}
				Document document = PIMUtils.constructXML(historyContent);
				Element root = document.getDocumentElement();
				//TODO Can declare loglevel, eventype attributes as enums and can call separate methods to handle different combinations - LATER
				NodeList nodeListBasicInfo = root.getElementsByTagName("basic_event_info");
				Element elebasicInfoAttrs = (Element)nodeListBasicInfo.item(0);
				String logLevel = elebasicInfoAttrs.getElementsByTagName("log_level").item(0).getTextContent();
				String timestamp = elebasicInfoAttrs.getElementsByTagName("timestamp").item(0).getTextContent();
				String objectType = elebasicInfoAttrs.getElementsByTagName("object_type").item(0).getTextContent();

				if(objectType.equalsIgnoreCase("COLLABORATION_ITEM")){
					collabHistoryContent.add(historyContent);
				}
				String userID = elebasicInfoAttrs.getElementsByTagName("user_id").item(0).getTextContent();
				String userName = PIMUtils.getUserNameById(userID);
				objLogger.logDebug("username is----"+userName+" and user ID--"+userID);
				//	if(eventTypeAttribute.equals("UPDATE") && logLevel.equals("DELTA")) {
				if( objectType.equalsIgnoreCase("ITEM")){
					if(logLevel.equals("DELTA")) {
						//objLogger.logDebug("processCurrentUser for catalog item---"+processCurrentUser(currentUserName, userName));
						//if(processCurrentUser(currentUserName, userName)) { // Phase 2 Changes for restricting user specific changes to be visible to given user
							NodeList nodeListAttributes = root.getElementsByTagName("attributes");
							Element eleAttributes = (Element) nodeListAttributes.item(0);
							NodeList nodeListDelts = eleAttributes.getElementsByTagName("delta");
							Element eleDelta = (Element) nodeListDelts.item(0);
							NodeList nodeListSpec = eleDelta.getElementsByTagName("spec_driven");
							Element specDrivenElement = (Element) nodeListSpec.item(0);
							if(specDrivenElement != null) {
								//This element contains only changes specific to attributes at Spec level. 
								//If an item is saved without any changes then version change happens to the item but spec_driven element will not be present.
								//If item is recategorized but not attribute level changes are done then again spec_driven element will not be present.
								handleSpecDrivenElements(specDrivenElement, out, timestamp, userName, item);
							}
							NodeList mappingNodes = root.getElementsByTagName("mappings");
							Element mappingNode = (Element) mappingNodes.item(0);
							if(mappingNode != null) {
								handleMappingChanges(mappingNode, out, timestamp, userName, partNumber, systemOfRecord);	
							}
						//}
					}
				}
			}
			out.println("</table>");
			//To display collabHistory in Catalog
			System.out.println("collabHistoryContent:" + collabHistoryContent);

			//if(collabHistoryContent == null || collabHistoryContent.size() == 0 || !collabHistoryContent.toString().contains(userIDStringValConcatPIMUserID)){
				if(collabHistoryContent == null || collabHistoryContent.size() == 0 ){
				System.out.println("Collab History for item in catalog is null");
				printNoHistoryMessage(out, PIMUtils.NEW_ITEM_NO_ITEM_HISTORY_IN_CATALOG);
				return;
			}
			if(StringUtils.isNotBlank(UPQ)){
				out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_COLLABITEM_HISTORY_IN_CATALOG, new String[]{item.getAttributeValue(PIMUtils.UNIQUE_PRODUCT_QUALIFIER_ATTRIBUTE).toString()})));
			}
			else{
				out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_COLLABITEM_HISTORY_IN_CATALOG, new String[]{item.getAttributeValue(PIMUtils.PIM_ID_ATTRIBUTE).toString()})));
			}
			for(int j = 0; j < collabHistoryContent.size(); j++){

				String historyContent = collabHistoryContent.get(j);
				String partNumber = (String) item.getAttributeValue(PIMUtils.PART_NUMBER_ATTRIBUTE);
				String systemOfRecord = (String) item.getAttributeValue(PIMUtils.SYSRECORD_ATTRIBUTE);
				if(StringUtils.isBlank(partNumber)){
					partNumber = PIMUtils.NULL_ATTRIBUTE_VALUE;
				}
				if(StringUtils.isBlank(systemOfRecord)){
					systemOfRecord = PIMUtils.NULL_ATTRIBUTE_VALUE;
				}
				Document document = PIMUtils.constructXML(historyContent);
				Element root = document.getDocumentElement();
				//TODO Can declare loglevel, eventype attributes as enums and can call separate methods to handle different combinations - LATER
				NodeList nodeListBasicInfo = root.getElementsByTagName("basic_event_info");
				Element elebasicInfoAttrs = (Element)nodeListBasicInfo.item(0);
				String logLevel = elebasicInfoAttrs.getElementsByTagName("log_level").item(0).getTextContent();
				String timestamp = elebasicInfoAttrs.getElementsByTagName("timestamp").item(0).getTextContent();
				String objectType = elebasicInfoAttrs.getElementsByTagName("object_type").item(0).getTextContent();
				String userID = elebasicInfoAttrs.getElementsByTagName("user_id").item(0).getTextContent();
				String userName = PIMUtils.getUserNameById(userID);

				if(logLevel.equals("DELTA")) {
					//if(processCurrentUser(currentUserName, userName)) { // Phase 2 Changes for restricting user specific changes to be visible to given user
						NodeList nodeListAttributes = root.getElementsByTagName("attributes");
						Element eleAttributes = (Element) nodeListAttributes.item(0);
						NodeList nodeListDelts = eleAttributes.getElementsByTagName("delta");
						Element eleDelta = (Element) nodeListDelts.item(0);
						NodeList nodeListSpec = eleDelta.getElementsByTagName("spec_driven");
						Element specDrivenElement = (Element) nodeListSpec.item(0);
						if(specDrivenElement != null) {
							//This element contains only changes specific to attributes at Spec level. 
							//If an item is saved without any changes then version change happens to the item but spec_driven element will not be present.
							//If item is recategorized but not attribute level changes are done then again spec_driven element will not be present.
							handleSpecDrivenElements(specDrivenElement, out, timestamp, userName, item);
						}
						NodeList mappingNodes = root.getElementsByTagName("mappings");
						Element mappingNode = (Element) mappingNodes.item(0);
						if(mappingNode != null) {
							handleMappingChanges(mappingNode, out, timestamp, userName, partNumber, systemOfRecord);	
						}
					//}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			out.println("Issue while retrieving history data. Please try again.");
		}
		finally {
			out.println("</table>");
			if(ctx != null) {
				ctx.cleanUp();	
			}

		}
	}

	public void entryPreview(CollaborationItemPreviewFunctionArguments inArgs) {
		Context ctx = PIMUtils.getCurrentContext();
		String currentUser = ctx.getCurrentUser().getUserName();
		Logger objLogger =  ctx.getLogger(Constant.LOGGER_NAME);
		objLogger.logDebug("ItemHistoryPreviewImpl For Collaboration Item Started..!!!"+currentUser);
		PIMCollection<CollaborationItem> items = inArgs.getCollaborationItems();
		PrintWriter out = inArgs.getOutput();
		//BufferedWriter out = new BufferedWriter(pWriter);
		CollaborationItem item = null;
		if(items != null && items.size() > 0 && items.iterator().hasNext())	{
			item = items.iterator().next();
		}
		CollaborationStep collabStep = inArgs.getCollaborationStep();
		CollaborationObject collabObject = collabStep.getCollaborationObject(item.getId());
		boolean headingsReady = false;
		boolean historyAvailable = false;
		List<CollaborationHistoryEvent> collabHistory = collabObject.getCollaborationHistory(PIMUtils.getHistoryStartDate(), PIMUtils.getHistoryEndDate());
		//objLogger.logDebug("CollabHistory from getCollabHistory::: "+collabHistory);
		//Context ctx = PIMUtils.getCurrentContext();
		//HistoryManager historyMgr = ctx.getHistoryManager();
		//List<String> historyList = historyMgr.searchHistory(new int[] {item.getId()},CollaborationItem.OBJECT_TYPE, PIMUtils.getHistoryStartDate(), PIMUtils.getHistoryEndDate());
		//System.out.println("From History Manager CollabHistory::"+historyList);
/*		TreeSet<CollaborationItemHistoryObject> timelineSortedSet = new TreeSet<CollaborationItemHistoryObject>();
		for(int index=0;index<collabHistory.size();index++) {
			CollaborationHistoryEvent collabHistoryEvnt = collabHistory.get(index);
			timelineSortedSet.add(new CollaborationItemHistoryObject(item.getId(), collabHistoryEvnt));
		}
		objLogger.logDebug("timelineSortedSet: "+timelineSortedSet);
		Iterator<CollaborationItemHistoryObject> iterator = timelineSortedSet.iterator();*/
		//while(iterator.hasNext()) {
		for(CollaborationHistoryEvent collabHistoryEvnt : collabHistory) {
			//CollaborationHistoryEvent collabHistoryEvnt = ((CollaborationItemHistoryObject) iterator.next()).getCollabHistoryEvnt();
			String username = collabHistoryEvnt.getUser().getUserName();
			String appendBoldStart = "";
			String appendBoldEnd = "";
			//objLogger.logDebug("username--"+username+" process current user for collaboration item...--"+processCurrentUser(currentUser,username));
			//if(processCurrentUser(currentUser,username)) // phase 2 this will be filtered based on user login.
		//	{
				String diff = collabHistoryEvnt.getDifferences();
				//objLogger.logDebug("CollabHistory Differences::: "+diff);
				if(diff != null) {
					Document root = PIMUtils.constructXML(diff);
					if(root == null) {
						continue;
					}
					NodeList diffNodesList = root.getElementsByTagName("Node");
					//objLogger.logDebug("diffNodesList: " + diffNodesList);
					if(diffNodesList.getLength() > 0) {
						if(!headingsReady) {
							historyAvailable = true;
							//System.out.println("CollabHistory item::: "+item);
							//To display PIM_ID if UniqueProductQualifier is blank
							String UPQ = (String) item.getAttributeValue(PIMUtils.UNIQUE_PRODUCT_QUALIFIER_ATTRIBUTE);
							if(StringUtils.isNotBlank(UPQ)){
								out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_COLLABITEM_HISTORY, new String[]{item.getAttributeValue(PIMUtils.UNIQUE_PRODUCT_QUALIFIER_ATTRIBUTE).toString()})));
							}
							else{
								out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_COLLABITEM_HISTORY, new String[]{item.getAttributeValue(PIMUtils.PIM_ID_ATTRIBUTE).toString()})));
							}
							headingsReady = true;
						}
						for(int i=0; i<diffNodesList.getLength(); i++) {
							Element node = (Element) diffNodesList.item(i);
							Date date = collabHistoryEvnt.getDate();
							int size = node.getElementsByTagName("Path").item(0).getTextContent().length();
							int lastIndex = node.getElementsByTagName("Path").item(0).getTextContent().lastIndexOf(PIMUtils.DELIMITER);
							String attribName = node.getElementsByTagName("Path").item(0).getTextContent().substring(lastIndex + 1, size);
							System.out.println("AttribName check for Hidden:" + attribName);
							boolean isHidden = PIMUtils.isHidden(attribName);
							String lkpTableName = PIMUtils.isLookUpTableAttrib(attribName);
							String oldValue = null;
							String newValue = null;
							if(StringUtils.isBlank(lkpTableName)){
								lkpTableName = PIMUtils.isCollabItemLookUpTableAttribFromSecSpec(attribName,item);
								System.out.println("lkpTableName from secSpec is.." + lkpTableName);
							}
							if(StringUtils.isNotBlank(lkpTableName)){
								LookupTable lkpTable = PIMUtils.getLookupTable(lkpTableName);
								System.out.println("LkpTable:" + lkpTable);
								if(lkpTable != null){
									oldValue = node.getElementsByTagName("OldValue").item(0).getTextContent();
									if(StringUtils.isNotBlank(oldValue)){
										String lkpTableOldValue = (String) PIMUtils.getLookupTableValue(lkpTable,oldValue);
										System.out.println("LkpTableOldvalue:" + lkpTableOldValue);
										if(StringUtils.isNotBlank(lkpTableOldValue)){
											if(lkpTableOldValue.equalsIgnoreCase("Not set")){
												oldValue = "";
											}
											else{
												oldValue = lkpTableOldValue;
											}
										}
										else{
											oldValue = node.getElementsByTagName("OldValue").item(0).getTextContent();
										}
									}
									newValue = node.getElementsByTagName("NewValue").item(0).getTextContent();
									if(StringUtils.isNotBlank(newValue)){
										String lkpTableNewValue = (String) PIMUtils.getLookupTableValue(lkpTable,newValue);
										System.out.println("LkpTableNewvalue:" + lkpTableNewValue);
										if(StringUtils.isNotBlank(lkpTableNewValue)){
											if(lkpTableNewValue.equalsIgnoreCase("Not set")){
												newValue = "";
											}
											else{
												newValue = lkpTableNewValue;
											}
										}
										else{
											newValue = node.getElementsByTagName("NewValue").item(0).getTextContent();
										}
									}
								}
							}
							else{
								oldValue = node.getElementsByTagName("OldValue").item(0).getTextContent();
								newValue = node.getElementsByTagName("NewValue").item(0).getTextContent();
							}
							if(!isHidden){
								out.println("<tr>");
								if(!username.equals("Admin")) {
									 appendBoldStart = "<b>";
									 appendBoldEnd = "</b>";
								}
								out.println("<td>"+appendBoldStart+PIMUtils.getDate(date)+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+PIMUtils.getTime(date)+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+item.getAttributeValue(PIMUtils.PART_NUMBER_ATTRIBUTE)+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+item.getAttributeValue(PIMUtils.SYSRECORD_ATTRIBUTE)+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+collabHistoryEvnt.getCollaborationStep().getName()+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+username+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+node.getElementsByTagName("Path").item(0).getTextContent()+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+oldValue+appendBoldEnd+"</td>");
								out.println("<td>"+appendBoldStart+newValue+appendBoldEnd+"</td>");
								out.println("<td></td>");
								out.println("</tr>");
							}
						}			
					}
				}
			//}
		}

		handleMappingChanges(out, item, historyAvailable, headingsReady);
	}

	public void entryPreview(CategoryPreviewFunctionArguments inArgs) {	
	}

	public void entryPreview(CollaborationCategoryPreviewFunctionArguments inArgs) {	
	}

	private static void handleMappingChanges(Element mappingNode, PrintWriter out, String timestamp, String userName, String partNumber, String systemOfRecord) {
		NodeList nodeListRemoved = mappingNode.getElementsByTagName("removed");
		Element eleRemoved = (Element) nodeListRemoved.item(0);
		if(eleRemoved != null) {
			StringBuffer sb = new StringBuffer();
			NodeList removedCategoriesNL = eleRemoved.getElementsByTagName("name");
			if(removedCategoriesNL.getLength() > 0) {
				sb.append(removedCategoriesNL.item(0).getTextContent());
			}
			for(int i=1;i<removedCategoriesNL.getLength();i++) {
				sb.append(",").append(removedCategoriesNL.item(i).getTextContent());
			}
			out.println(printMappingChanges(timestamp, partNumber, systemOfRecord, userName, sb.toString(), ActionType.REMOVED));
		}
		NodeList nodeListAdded = mappingNode.getElementsByTagName("added");
		Element eleAdded = (Element) nodeListAdded.item(0);
		if(eleAdded != null) {
			StringBuffer sb = new StringBuffer();
			NodeList addedCategoriesNL = eleAdded.getElementsByTagName("name");	
			if(addedCategoriesNL.getLength() > 0) {
				sb.append(addedCategoriesNL.item(0).getTextContent());
			}
			for(int i=1;i<addedCategoriesNL.getLength();i++) {
				sb.append(",").append(addedCategoriesNL.item(i).getTextContent());
			}
			out.println(printMappingChanges(timestamp, partNumber, systemOfRecord, userName, sb.toString(), ActionType.ADDED));
		}
	}

	private static String printMappingChanges(String timestamp, String partNumber, String systemOfRecord, String userName, String changedCategories, ActionType type ) {
		StringBuffer buffer = new StringBuffer();
		String appendBoldStart = "";
		String appendBoldEnd = "";
		buffer.append("<tr>");
		if(!userName.equals("Admin"))
		{
			 appendBoldStart = "<b>";
			 appendBoldEnd = "</b>";
		}
		buffer.append("<td>"+appendBoldStart+PIMUtils.getDateFromTimeStamp(timestamp)+appendBoldEnd+"</td>");
		buffer.append("<td>"+appendBoldStart+PIMUtils.getTimeFromTimeStamp(timestamp)+appendBoldEnd+"</td>");
		buffer.append("<td>"+appendBoldStart+partNumber+appendBoldEnd+"</td>");
		buffer.append("<td>"+appendBoldStart+systemOfRecord+appendBoldEnd+"</td>");
		buffer.append("<td></td>");
		buffer.append("<td>"+appendBoldStart+userName+appendBoldEnd+"</td>");
		buffer.append("<td>"+appendBoldStart+"Category Mapping Change"+appendBoldEnd+"</td>");
		buffer.append("<td></td>");
		buffer.append("<td>"+appendBoldStart+changedCategories+appendBoldEnd+"</td>");
		buffer.append("<td>"+appendBoldStart+type.name()+appendBoldEnd+"</td>");
		buffer.append("</tr>");
		return buffer.toString();
	}

	private static String prepareHeaders(String pageTitle) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<h1>"+pageTitle+"</h1>");
		buffer.append("<hr>").append("<br>").append("<br>");
		buffer.append("<table border=1 cellPadding=0 cellSpacing=0>");
		buffer.append("<tr>");
		buffer.append("<td><b>Date</b></td>");
		buffer.append("<td><b>Time</b></td>");
		buffer.append("<td><b>Part Number</b></td>");
		buffer.append("<td><b>System Of Record</b></td>");
		buffer.append("<td><b>Workflow Step</b></td>");
		buffer.append("<td><b>User</b></td>");
		buffer.append("<td><b>Attribute</b></td>");
		buffer.append("<td><b>Old Value</b></td>");
		buffer.append("<td><b>New Value</b></td>");
		buffer.append("<td><b>Action</b></td>");
		buffer.append("</tr>");
		return buffer.toString();
	}

	private void handleSpecDrivenElements(Element specDrivenElement, PrintWriter out, String timestamp, String userName, Item item) {
		NodeList nodeListModified = specDrivenElement.getElementsByTagName("modified");
		Element eleModified = (Element) nodeListModified.item(0);
		NodeList nodeListAdded = specDrivenElement.getElementsByTagName("added");
		Element eleAdded = (Element) nodeListAdded.item(0);
		NodeList nodeListRemoved = specDrivenElement.getElementsByTagName("removed");
		Element eleRemoved = (Element) nodeListRemoved.item(0);
		if(eleAdded != null) {
			handleAttributeChanges(eleAdded, out, timestamp, userName, item, ActionType.ADDED);	
		}
		if(eleModified != null) {
			handleAttributeChanges(eleModified, out, timestamp, userName, item, ActionType.MODIFIED);	
		}
		if(eleRemoved != null) {
			System.out.println("Inside removed node...");
			handleAttributeChanges(eleRemoved, out, timestamp, userName, item, ActionType.REMOVED);	
		}		
	}

	private void handleAttributeChanges(Element eleRemoved, PrintWriter out, String timestamp, String userName, Item item, ActionType actionType) {
		NodeList nodeListRemovedAttrs = eleRemoved.getElementsByTagName("attribute");
		String appendBoldStart="";;
		String appendBoldEnd="";
		if(!userName.equals("Admin"))
		{
			 appendBoldStart = "<b>";
			 appendBoldEnd = "</b>";
		}
		if(nodeListRemovedAttrs.getLength()>0) {
			for (int temp = 0; temp < nodeListRemovedAttrs.getLength(); temp++) {
				Node nNode = nodeListRemovedAttrs.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElementAttr = (Element) nNode;
					String sPath = eElementAttr.getElementsByTagName("name").item(0).getTextContent();
					sPath = sPath.substring(1);
					int size = sPath.length();
					int lastIndex = sPath.lastIndexOf(PIMUtils.DELIMITER);
					String attribName = sPath.substring(lastIndex + 1, size);
					boolean isHidden = PIMUtils.isHidden(attribName);
					String lkpTableName = PIMUtils.isLookUpTableAttrib(attribName);
					System.out.println("LkpTableName:" + lkpTableName);
					String lkpTableOldValue = null;
					String lkpTableNewValue = null;
					if(StringUtils.isBlank(lkpTableName)){
						lkpTableName = PIMUtils.isCatalogItemLookUpTableAttribFromSecSpec(attribName,item);
						System.out.println("lkpTableName from secSpec is.." + lkpTableName);
					}
					if(!isHidden){
						out.println("<tr>");
						out.println("<td>"+appendBoldStart+PIMUtils.getDateFromTimeStamp(timestamp)+appendBoldEnd+"</td>");
						out.println("<td>"+appendBoldStart+PIMUtils.getTimeFromTimeStamp(timestamp)+appendBoldEnd+"</td>");
						out.println("<td>"+appendBoldStart+item.getAttributeValue(PIMUtils.PART_NUMBER_ATTRIBUTE)+appendBoldEnd+"</td>");
						out.println("<td>"+appendBoldStart+item.getAttributeValue(PIMUtils.SYSRECORD_ATTRIBUTE)+appendBoldEnd+"</td>");
						out.println("<td></td>");
						out.println("<td>"+appendBoldStart+userName+appendBoldEnd+"</td>");
						out.println("<td>"+appendBoldStart+sPath+appendBoldEnd+"</td>");
						String oldValue = "";
						String newValue = "";
						if(actionType == ActionType.MODIFIED) {

							if(StringUtils.isNotBlank(lkpTableName)){
								LookupTable lkpTable = PIMUtils.getLookupTable(lkpTableName);
								System.out.println("LkpTable:" + lkpTable);
								if(lkpTable != null){
									Element oldLkpValue = (Element)eElementAttr.getElementsByTagName("old_value").item(0);
									oldValue = (oldLkpValue != null) ? oldLkpValue.getTextContent() : "";
									lkpTableOldValue = (String) PIMUtils.getLookupTableValue(lkpTable,oldValue);
									System.out.println("lkpTableOldValue:" + lkpTableOldValue);
									Element newLkpValue = (Element)eElementAttr.getElementsByTagName("new_value").item(0);
									newValue = (newLkpValue != null) ? newLkpValue.getTextContent() : "";
									lkpTableNewValue = (String) PIMUtils.getLookupTableValue(lkpTable,newValue);
									if(StringUtils.isNotBlank(lkpTableNewValue)){
										if(lkpTableNewValue.equalsIgnoreCase("Not set")){
											lkpTableNewValue = "";
										}
										newValue = lkpTableNewValue;
									}
									if(StringUtils.isNotBlank(lkpTableOldValue)){
										if(lkpTableOldValue.equalsIgnoreCase("Not set")){
											lkpTableOldValue = "";
										}
										out.println("<td>"+appendBoldStart+lkpTableOldValue+appendBoldEnd+"</td>");
									}
									else{
										out.println("<td>"+appendBoldStart+oldValue+appendBoldEnd+"</td>");
									}
								}
							}
							else{
								Element eleOldValue = (Element)eElementAttr.getElementsByTagName("old_value").item(0);
								oldValue = (eleOldValue != null) ? eleOldValue.getTextContent() : "";
								Element eleNewValue = (Element)eElementAttr.getElementsByTagName("new_value").item(0);
								newValue = (eleNewValue != null) ? eleNewValue.getTextContent() : "";
								out.println("<td>"+appendBoldStart+oldValue+appendBoldEnd+"</td>");
							}
						}
						else if((actionType == ActionType.ADDED) || (actionType == ActionType.REMOVED)){

							if(StringUtils.isNotBlank(lkpTableName)){
								LookupTable lkpTable = PIMUtils.getLookupTable(lkpTableName);
								System.out.println("LkpTable:" + lkpTable);
								if(lkpTable != null){

									Element newLkpValue = (Element)eElementAttr.getElementsByTagName("value").item(0);
									newValue = (newLkpValue != null) ? newLkpValue.getTextContent() : "";
									lkpTableNewValue = (String) PIMUtils.getLookupTableValue(lkpTable,newValue);
									if(StringUtils.isNotBlank(lkpTableNewValue)){
										if(lkpTableNewValue.equalsIgnoreCase("Not set")){
											lkpTableNewValue = "";
										}
										newValue = lkpTableNewValue;
									}
								}
								out.println("<td>&nbsp;&nbsp;</td>");
							}
							else{
								newValue = eElementAttr.getElementsByTagName("value").item(0).getTextContent();
								out.println("<td>&nbsp;&nbsp;</td>");
							}
						}
						out.println("<td>"+appendBoldStart+newValue+appendBoldEnd+"</td>");
						out.println("<td>"+appendBoldStart+actionType.name()+appendBoldEnd+"</td>");
						out.println("</tr>");
					}
				}
			}
		}
	}

	private static void printNoHistoryMessage(PrintWriter out, String title) {
		out.println("<h1>"+title+"</h1>");		
	}

	private static void handleMappingChanges(PrintWriter out, CollaborationItem item, boolean historyAvailable, boolean headingsReady) {
		Context ctx = PIMUtils.getCurrentContext();
		HistoryManager historyMgr = ctx.getHistoryManager();
		String loggedUser = ctx.getCurrentUser().getUserName();
		List<String> historyList = historyMgr.searchHistory(new int[] {item.getId()} ,CollaborationItem.OBJECT_TYPE, null, null);
		String UPQ = (String) item.getAttributeValue(PIMUtils.UNIQUE_PRODUCT_QUALIFIER_ATTRIBUTE);
		String systemOfRecord = (String) item.getAttributeValue(PIMUtils.SYSRECORD_ATTRIBUTE);
		String partNumber = (String) item.getAttributeValue(PIMUtils.PART_NUMBER_ATTRIBUTE);
		if(StringUtils.isBlank(UPQ)){
			UPQ = PIMUtils.NULL_ATTRIBUTE_VALUE;
		}
		if(StringUtils.isBlank(systemOfRecord)){
			systemOfRecord = PIMUtils.NULL_ATTRIBUTE_VALUE;
		}
		if(historyList != null && historyList.size() > 0) {
			for (int index=historyList.size()-1; index>=0; index--) {
				Document document = PIMUtils.constructXML(historyList.get(index));
				Element root = document.getDocumentElement();
				NodeList nodeListBasicInfo = root.getElementsByTagName("basic_event_info");
				Element elebasicInfoAttrs = (Element)nodeListBasicInfo.item(0);
				String logLevel = elebasicInfoAttrs.getElementsByTagName("log_level").item(0).getTextContent();
				String timestamp = elebasicInfoAttrs.getElementsByTagName("timestamp").item(0).getTextContent();
				String userID = elebasicInfoAttrs.getElementsByTagName("user_id").item(0).getTextContent();
				String userName = PIMUtils.getUserNameById(userID);
				//if(eventTypeAttribute.equals("UPDATE") && logLevel.equals("DELTA")) {
				if(logLevel.equals("DELTA")) {
					if(processCurrentUser(loggedUser,userName)) {  // phase 2 this will be printed based on user login
						NodeList mappingNodes = root.getElementsByTagName("mappings");
						Element mappingNode = (Element) mappingNodes.item(0);
						if(mappingNode == null) {
							continue;
						}
						if(!headingsReady) {
							historyAvailable = true;
							if(StringUtils.isNotBlank(UPQ)){
								out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_COLLABITEM_HISTORY,UPQ)));
							}
							else{
								out.println(prepareHeaders(MessageFormat.format(PIMUtils.HEADER_COLLABITEM_HISTORY,new String[]{item.getAttributeValue(PIMUtils.PIM_ID_ATTRIBUTE).toString()})));
							}
							headingsReady = true;
						}
						handleMappingChanges(mappingNode, out, timestamp, userName, partNumber, systemOfRecord);
					}
				} /////
			}
		}
		if(!historyAvailable) {
			printNoHistoryMessage(out, PIMUtils.COLLAB_ITEM_NO_ITEM_HISTORY);
		}
	}

	// Added as part of Phase 2

	private static boolean processCurrentUser(String currentUser, String username)
	{
		if(currentUser.equals("Admin"))
		{
			return true;
		}
		if(currentUser.equals(username) && !currentUser.equals("Admin"))
		{
			return true;
		}
		return false;
	}

}