package com.ww.pim.common.utils;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.ibm.ccd.api.lookuptable.LookupTableEntryImpl;
import com.ibm.ccd.api.lookuptable.LookupTableImpl;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeDefinitionProperty;
import com.ibm.ccd.api.common.PIMObject;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.HierarchyManager;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.lookuptable.LookupTable;
import com.ibm.pim.lookuptable.LookupTableManager;
import com.ibm.pim.spec.SecondarySpec;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;

public class PIMUtils {

	private static final String GET_USERNAME_BY_ID = "SELECT SCU_USERNAME FROM SCU WHERE SCU_USER_ID=?";
	
	// newly Added line for Phase 2
	private static final String GET_USERID_BY_NAME = "SELECT SCU_USER_ID FROM SCU WHERE SCU_USERNAME=?";

	public static final String PRODUCT_CATALOG_GLOBAL_SPEC = "Product Catalog Global Spec";
	
	public static final String TEMPLATES_HIERARCHY = "Templates Hierarchy";

	//public static final String PRODUCT_CATALOG_GLOBAL_SPEC = "BankingProducts_Catalog";

	public static final String PART_NUMBER_ATTRIBUTE = PRODUCT_CATALOG_GLOBAL_SPEC+"/"+"Part Number";

	public static final String UNIQUE_PRODUCT_QUALIFIER_ATTRIBUTE = PRODUCT_CATALOG_GLOBAL_SPEC+"/"+"UniqueProductQualifier";

	public static final String SYSRECORD_ATTRIBUTE = PRODUCT_CATALOG_GLOBAL_SPEC+"/"+"SystemOfRecord";

	//Added to display PIM_ID if Part Number not available
	public static final String PIM_ID_ATTRIBUTE = PRODUCT_CATALOG_GLOBAL_SPEC+"/"+"PIM_ID";

	//To display Null if value not available
	public static final String NULL_ATTRIBUTE_VALUE = "null";

	public static final String DATEPARSER_FOR_AUDIT_HISTORY = "yyyy-MM-dd-HH.mm.ss";

	public static final String DATEFORMAT_FOR_AUDIT_HISTORY = "yyyy-MM-dd";

	public static final String TIMEFORMAT_FOR_AUDIT_HISTORY = "HH:mm:ss";

	//As per business requirements history for so far this year plus previous 3 years need to be maintained. 
	//TODO Keeping this as a constant for the timebeing but shall be kept as a Lookup table value.
	public static final int HISTORY_RETENTION_PERIOD_IN_YEARS = 4;

	public static final String NEW_ITEM_NO_ITEM_HISTORY = "No Audit History Available for the Item";
	
	public static final String NEW_ITEM_NO_ITEM_HISTORY_IN_CATALOG = "No Audit History Available for the Item related to Collaboration Area Changes";

	public static final String COLLAB_ITEM_NO_ITEM_HISTORY = "No Audit History Available for the Collaboration Item";

	public static final String NO_ITEM_HISTORY = "No Audit History Available for the Item - {0}";

	public static final String NO_COLLABITEM_HISTORY = "No Audit History Available for the Collaboration Item - {0}";

	public static final String HEADER_COLLABITEM_HISTORY = "Audit History for the Collaboration Item - {0}";
	
	public static final String HEADER_COLLABITEM_HISTORY_IN_CATALOG = "Audit History for the Item related to Collaboration Area Changes - {0}";

	public static final String HEADER_ITEM_HISTORY = "Audit History for the Item - {0}";

	//TODO: To be changed to retrieve dynamically
	public static final String LOOKUP_TABLE_NAME = "BuyerPlanner - USQAD";

	public static final String YES_VALUE = "yes";

	public static final String DELIMITER = "/";
	private static Context ctx = PIMContextFactory.getCurrentContext();
	private static Logger objLogger =  ctx.getLogger("WattsCustomLog");

	public static String getDateFromTimeStamp(String timeStamp){
		DateFormat dateParser = new SimpleDateFormat(PIMUtils.DATEPARSER_FOR_AUDIT_HISTORY);
		Date date;
		try {
			date = dateParser.parse(timeStamp);
			DateFormat dateFormat = new SimpleDateFormat(PIMUtils.DATEFORMAT_FOR_AUDIT_HISTORY);
			return dateFormat.format(date).toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timeStamp;
	}

	public static String getTimeFromTimeStamp(String timeStamp) {
		DateFormat dateParser = new SimpleDateFormat(PIMUtils.DATEPARSER_FOR_AUDIT_HISTORY);
		Date date;
		try {
			date = dateParser.parse(timeStamp);
			DateFormat dateFormat = new SimpleDateFormat(PIMUtils.TIMEFORMAT_FOR_AUDIT_HISTORY);
			return dateFormat.format(date).toString();		
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timeStamp;
	}

	public static String getDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(PIMUtils.DATEFORMAT_FOR_AUDIT_HISTORY);
		return dateFormat.format(date).toString();
	}

	public static String getTime(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(PIMUtils.TIMEFORMAT_FOR_AUDIT_HISTORY);
		return dateFormat.format(date).toString();
	}

	/**
	 * Creates a Document object from the given String XML content
	 * @param content The string which need to be converted into a XML Document and returned.
	 * @return Document object of the given string content
	 */
	public static Document constructXML(String content) {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(content)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Utility method that provides the username for the given id from the SCU table.
	 * If any exception occurs, then id is returned as a safe bet.
	 * @param id Id of the user whose name should be provided in return
	 * @return Name of the user
	 */
	public static String getUserNameById(String id) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Connection  c = DBConnectionManager.getInstance().getConnection(); 
			ps = c.prepareStatement(GET_USERNAME_BY_ID);
			ps.setInt(1,Integer.parseInt(id));
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println("Inside Catch for connection exception...");
			String res = getNewConnection(ex.getMessage(),id);
			System.out.println("Result in catch:" + res);
			return res;
		}
		finally {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Result in method:" + id);
		return id;
	}
	// newly Added method for Phase 2 to retrieve user id based on username
	public static String getUserIdByName(String name) {
		objLogger.logDebug("inside getUserIdByName..");
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Connection  c = DBConnectionManager.getInstance().getConnection(); 
			ps = c.prepareStatement(GET_USERID_BY_NAME);
			ps.setString(1,name);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);

			}

		} catch (SQLException ex) {
			ex.printStackTrace();
			objLogger.logDebug("Inside Catch for connection exception...");
			String res = getNewConnectionForUserID(ex.getMessage(),name);
			objLogger.logDebug("Result in catch:" + res);
			return res;
		}
		finally {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		objLogger.logDebug("Result in method:" + name);
		return name;
	}
	public static String getNewConnection(String message,String id){

		ResultSet rs = null;
		PreparedStatement ps = null;
		String result = null;
		try{
			if(message.contains("Connection is closed")){
				System.out.println("Inside error message of connection exception...");

				Connection  c = DBConnectionManager.getInstance().createConnection();
				System.out.println("Inside createConnection...");
				ps = c.prepareStatement(GET_USERNAME_BY_ID);
				ps.setInt(1,Integer.parseInt(id));
				rs = ps.executeQuery();
				if(rs.next()) {
					result = rs.getString(1);
					System.out.println("Result:" + result);
				}
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		finally {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	// newly Added method for Phase 2
	
	
	public static String getNewConnectionForUserID(String message,String id){
		

		ResultSet rs = null;
		PreparedStatement ps = null;
		String result = null;
		try{
			if(message.contains("Connection is closed")){
				objLogger.logDebug("Inside error message of connection exception...");

				Connection  c = DBConnectionManager.getInstance().createConnection();
				ps = c.prepareStatement(GET_USERID_BY_NAME);
				ps.setString(1,id);
				rs = ps.executeQuery();
				if(rs.next()) {
					result = rs.getString(1);
					objLogger.logDebug("Result:" + result);
				}
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		finally {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * This method provides the start date for all history queries.
	 * Currently, as per the business requirement start date is calculated as current year so far plus three previous years.
	 * @return start date object for history queries.
	 */
	public static Date getHistoryStartDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, (c.get(Calendar.YEAR)-HISTORY_RETENTION_PERIOD_IN_YEARS));
		return c.getTime();
	}

	/**
	 * This method provides the end date for all history queries.
	 * Now, it returns the current date and time.
	 * @return Current Date object.
	 */
	public static Date getHistoryEndDate() {
		return new Date();
	}
	/**
	 * To get the lookup table from lookup table name
	 */
	public static LookupTable getLookupTable(String lkpTableName) {
		Context ctx = PIMUtils.getCurrentContext();
		LookupTableManager lkpTableMgr = ctx.getLookupTableManager();
		LookupTable lkpTable = lkpTableMgr.getLookupTable(lkpTableName);
		return lkpTable;
	}
	/**
	 * To get the lookup table value from lookup table key
	 */
	public static Object getLookupTableValue(LookupTable lkpTable, String entryId){
		List<Object> value = new ArrayList<Object>();
		List<Object> tempValue = new ArrayList<Object>();
		try{
			if(entryId != null && entryId != "" && entryId != " "){
				System.out.println("Entering entry if as it is not null");
				int id = Integer.parseInt(entryId); 
				System.out.println("Parsed integer:" + id);
				PIMObject pim = (PIMObject) ((LookupTableImpl)lkpTable).getByID(id);
				System.out.println("Pimobject:" + pim);
				if(pim != null){
					String key = ((LookupTableEntryImpl) pim).getKey();
					System.out.println("LkpKey:" + key);
					value = lkpTable.getLookupEntryValues(key);
					System.out.println("LkpValue:" + value);
					
					if(value != null && value.size() > 0){
						System.out.println("Value list is in size greater than zero");
						if(value.get(0).toString().equals("") || value.get(0).toString().equals(" ")){
							tempValue.add(key);
							//return tempValue.get(0).toString();
						}
					}
					//return value.get(0).toString();
					return key;
				}
				else{
					System.out.println("PimObject is null...");
					return "Not set";
				}
			}
			else{
				System.out.println("Entry id is empty");
				return "Not set";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("entryId is not integer..");
		}
		return "Not set";
	}
	/**
	 * To get the hidden attributes from spec
	 */
	public static List<String> getHiddenAttribsFromSpec(){
		Context ctx = PIMUtils.getCurrentContext();
		Spec spec = ctx.getSpecManager().getSpec(PRODUCT_CATALOG_GLOBAL_SPEC);
		List<String> hiddenAttribs = new ArrayList<String>();
		List<AttributeDefinition> ls = spec.getAttributeDefinitions();
		for(AttributeDefinition ad : ls) {
			if(StringUtils.isNotBlank(ad.getProperty(AttributeDefinitionProperty.Name.HIDDEN).getValue()) 
					&& ad.getProperty(AttributeDefinitionProperty.Name.HIDDEN).getValue().equalsIgnoreCase(YES_VALUE)){
				//System.out.println(ad.getName()+":: HIDDEN:: "+ad.getProperty(AttributeDefinitionProperty.Name.HIDDEN).getValue());
				hiddenAttribs.add(ad.getName());
			}
		}
		return hiddenAttribs;
	}
	/**
	 * To get the lookup table attributes from spec
	 */
	public static Map<String,String> getLkpTableAttribsFromSpec(){
		Context ctx = PIMUtils.getCurrentContext();
		Spec spec = ctx.getSpecManager().getSpec(PRODUCT_CATALOG_GLOBAL_SPEC);
		List<AttributeDefinition> ls = spec.getAttributeDefinitions();
		Map<String,String> lkpTableAttribsMap = new HashMap<String,String>();
		for(AttributeDefinition ad : ls) {
			if(StringUtils.isNotBlank(ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue())){
				//System.out.println(ad.getName()+":: LKP_TABLE::Value "+ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue());
				lkpTableAttribsMap.put(ad.getName(),ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue());
			}
		}
		return lkpTableAttribsMap;
	}
	/**
	 * To get the lookup table attributes from secondary spec
	 */
	public static Map<String,String> getLkpTableAttribsFromSecondarySpecForCollabItem(CollaborationItem item){
		Context ctx = PIMUtils.getCurrentContext();
		Map<String,String> lkpTableAttribsMap = new HashMap<String,String>();
		//HierarchyManager hierarchyManager = ctx.getHierarchyManager();
		//Hierarchy hierarchy = hierarchyManager.getHierarchy(TEMPLATES_HIERARCHY);
		Collection<Category> categories = item.getCategories();
		for(Category category : categories){
			Collection<SecondarySpec> categoryspecs = category.getItemSecondarySpecs();
			for(SecondarySpec secSpec:categoryspecs){
				String secSpecName = secSpec.getName();
				Spec spec = ctx.getSpecManager().getSpec(secSpecName);
				List<AttributeDefinition> ls = spec.getAttributeDefinitions();
				
				for(AttributeDefinition ad : ls) {
					if(StringUtils.isNotBlank(ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue())){
						//System.out.println(ad.getName()+":: LKP_TABLE::Value "+ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue());
						lkpTableAttribsMap.put(ad.getName(),ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue());
					}
				}
			}
		}
		System.out.println("lkpTableAttribsMap is.."+lkpTableAttribsMap);
		return lkpTableAttribsMap;
	}
	/**
	 * To get the lookup table attributes from secondary spec
	 */
	public static Map<String,String> getLkpTableAttribsFromSecondarySpecForCatalogItem(Item item){
		Context ctx = PIMUtils.getCurrentContext();
		Map<String,String> lkpTableAttribsMap = new HashMap<String,String>();
		//HierarchyManager hierarchyManager = ctx.getHierarchyManager();
		//Hierarchy hierarchy = hierarchyManager.getHierarchy(TEMPLATES_HIERARCHY);
		Collection<Category> categories = item.getCategories();
		for(Category category : categories){
			Collection<SecondarySpec> categoryspecs = category.getItemSecondarySpecs();
			for(SecondarySpec secSpec:categoryspecs){
				String secSpecName = secSpec.getName();
				Spec spec = ctx.getSpecManager().getSpec(secSpecName);
				List<AttributeDefinition> ls = spec.getAttributeDefinitions();
				
				for(AttributeDefinition ad : ls) {
					if(StringUtils.isNotBlank(ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue())){
						//System.out.println(ad.getName()+":: LKP_TABLE::Value "+ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue());
						lkpTableAttribsMap.put(ad.getName(),ad.getProperty(AttributeDefinitionProperty.Name.LOOKUP_TABLE).getValue());
					}
				}
			}
		}
		System.out.println("lkpTableAttribsMap is.."+lkpTableAttribsMap);
		return lkpTableAttribsMap;
	}
	/**
	 * To check whether attribute is hidden
	 * @param attribName
	 * @return
	 */
	public static boolean isHidden(String attribName){
		List<String> hiddenAttribs = getHiddenAttribsFromSpec();
		boolean isHiddenAttrib = false;
		for(String attrib : hiddenAttribs){
			if(attrib.equalsIgnoreCase(attribName)){
				isHiddenAttrib = true;
				break;
			}
		}
		return isHiddenAttrib;
	}
	/**
	 * To check whether attribute is lookup table attribute
	 */
	@SuppressWarnings("rawtypes")
	public static String isLookUpTableAttrib(String attribName){
		Map<String,String> lkpTableAttribs = getLkpTableAttribsFromSpec();
		String lkpTableName = null;
		Iterator iterator = lkpTableAttribs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry lkpTableAttrib = (Map.Entry)iterator.next();
			//System.out.println(lkpTableAttrib.getKey() + " = " + lkpTableAttrib.getValue());
			if(lkpTableAttrib.getKey().equals(attribName)){
				lkpTableName = (String) lkpTableAttrib.getValue();
				break;
			}
		}
		return lkpTableName;
	}
	/**
	 * To check whether attribute is lookup table attribute in secondary spec
	 */
	@SuppressWarnings("rawtypes")
	public static String isCollabItemLookUpTableAttribFromSecSpec(String attribName,CollaborationItem item){
		Map<String,String> lkpTableAttribs = getLkpTableAttribsFromSecondarySpecForCollabItem(item);
		String lkpTableName = null;
		Iterator iterator = lkpTableAttribs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry lkpTableAttrib = (Map.Entry)iterator.next();
			//System.out.println(lkpTableAttrib.getKey() + " = " + lkpTableAttrib.getValue());
			if(lkpTableAttrib.getKey().equals(attribName)){
				lkpTableName = (String) lkpTableAttrib.getValue();
				break;
			}
		}
		return lkpTableName;
	}
	/**
	 * To check whether attribute is lookup table attribute in secondary spec
	 */
	@SuppressWarnings("rawtypes")
	public static String isCatalogItemLookUpTableAttribFromSecSpec(String attribName,Item item){
		Map<String,String> lkpTableAttribs = getLkpTableAttribsFromSecondarySpecForCatalogItem(item);
		String lkpTableName = null;
		Iterator iterator = lkpTableAttribs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry lkpTableAttrib = (Map.Entry)iterator.next();
			//System.out.println(lkpTableAttrib.getKey() + " = " + lkpTableAttrib.getValue());
			if(lkpTableAttrib.getKey().equals(attribName)){
				lkpTableName = (String) lkpTableAttrib.getValue();
				break;
			}
		}
		return lkpTableName;
	}
	/**
	 * To get the current context
	 */
	public static Context getCurrentContext(){

		Context ctx = PIMContextFactory.getCurrentContext();
		//TODO: to be commented out during deployment
		/*Context ctx = null;
		try {
			ctx = PIMContextFactory.getContext("Admin","trinitron","W2DEV");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("exception is caught...");
			e.printStackTrace();
			if (ctx != null) {
				ctx.cleanUp();
			}
		}*/
		return ctx;
	}
}