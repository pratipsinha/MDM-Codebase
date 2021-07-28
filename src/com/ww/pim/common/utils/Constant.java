/**
 * This class contains various constants used in the application 
 * @author Cognizant
 * */


package com.ww.pim.common.utils;

public class Constant {
	
	public static final String CUST_CAT_HIERARCHY = "Customer Catalogs";
	public static final String LOGGER_NAME = "WattsCustomLog";
	public static final String REPORT_LOGGER_NAME = "WattsReportLog";
	public static final String IMPORT_LOGGER_NAME = "WattsImportLog";
	public static final String DOLLAR_TOP = "TOP";
	public static final String MASS_ITEM_UPLOAD_PATH = "/feedfiles/Main Catalog Item Categorization Import/";
	//public static final String FERGUSON_ATTR_PROPERTY_FILE_PATH = "/properties/wattswater/Ferguson_Attr_Mapping.properties";
	public static final String FERGUSON_ATTR_PROPERTY_NEW_FILE_PATH = "/properties/wattswater/Ferguson_Attr_Mapping_new.properties";
	public static final String QAD_PIM_IMPORT_ATTRIBUTES_FILE_PATH = "/properties/wattswater/QAD_Parts_PIM_Creation.properties";
	//public static final String QAD_PIM_IMPORT_FEED_FILE_PATH = "/rpt1/WattsPrd/mdmpim/test/outbound/";
	//public static final String QAD_PIM_IMPORT_ERROR_FILE_PATH = "/rpt1/WattsPrd/mdmpim/test/outbound/error/";
	//public static final String QAD_PIM_IMPORT_FEED_FILE_ARCHIVEPATH = "/rpt1/WattsPrd/mdmpim/test/outbound/archive/";
	//public static final String QAD_PIM_IMPORT_MAIL_DIR_PATH = "/rpt1/WattsPrd/mdmpim/test/outbound/mail/";
	//public static final String QAD_PIM_IMPORT_FEED_FILE_PATH = "/rpt1/WattsPrd/mdmpim/prod/outbound/";
	//public static final String QAD_PIM_IMPORT_ERROR_FILE_PATH = "/rpt1/WattsPrd/mdmpim/prod/outbound/error/";
	//public static final String QAD_PIM_IMPORT_FEED_FILE_ARCHIVEPATH = "/rpt1/WattsPrd/mdmpim/prod/outbound/archive/";
	//public static final String QAD_PIM_IMPORT_MAIL_DIR_PATH = "/rpt1/WattsPrd/mdmpim/prod/outbound/mail/";
	public static final String QAD_PIM_IMPORT_FEED_FILE_PATH = "/rpt1-new/WattsPrd/mdmpim/prod/outbound/";
	public static final String QAD_PIM_IMPORT_ERROR_FILE_PATH = "/rpt1-new/WattsPrd/mdmpim/prod/outbound/error/";
	public static final String QAD_PIM_IMPORT_FEED_FILE_ARCHIVEPATH = "/rpt1-new/WattsPrd/mdmpim/prod/outbound/archive/";
	public static final String QAD_PIM_IMPORT_MAIL_DIR_PATH = "/rpt1-new/WattsPrd/mdmpim/prod/outbound/mail/";
	//public static final String QAD_PIM_IMPORT_FEED_FILE_PATH = "/rpt1-new/WattsPrd/mdmpim/test/outbound/";
	//public static final String QAD_PIM_IMPORT_ERROR_FILE_PATH = "/rpt1-new/WattsPrd/mdmpim/test/outbound/error/";
	//public static final String QAD_PIM_IMPORT_FEED_FILE_ARCHIVEPATH = "/rpt1-new/WattsPrd/mdmpim/test/outbound/archive/";
	//public static final String QAD_PIM_IMPORT_MAIL_DIR_PATH = "/rpt1-new/WattsPrd/mdmpim/test/outbound/mail/";
	public static final String NULL = "null";
	public static final String COMMA = ",";
	public static final String ACTION_ADD = "ADD";
	public static final String ACTION_REMOVE = "REMOVE";
	public static final String COLUMN_PARTNUMBER = "Part Number";
	public static final String COLUMN_SYSRECOD = "SystemOfRecord";
	public static final String COLUMN_ACTION = "ACTION";
	public static final String COLUMN_CUSTCATALOG = "Customer Catalog";
	
	//public static final String QADPARTSMAINTCOLAREANAME="QAD Parts Maintenance Staging Area";
	public static final String QADPARTSMAINTCOLAREANAME="Item Maintenance Staging Area";
	public static final String NONFINISHEDCOLAREANAME="Non-Finished Goods Staging Area";
	public static final String PRODUCT_CATALOG="Product Catalog";
	public static final String NET_EXPORT_LOOKUP_TABLE="Net_Export_Lookup_Table";
	public static final String CUST_CTG_HIER="Customer Catalogs";
	public static final String FERG_EXPORT_PATH = "/Exports/Net Change Export/";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String PIPE = "|";
	public static final String EACH_ID = "EA.";
	public static final String MASTER_ID = "MASTER:";
	public static final String INNER_ID = "INNER:";
	public static final String PALLET_ID = "PALLET:";
	public static final String PATH_EACH_LEVEL="Each Level";
	public static final String PART_PATH_IN_SPEC="Product Catalog Global Spec/Packaging Hierarchy/Paths";
	public static final String PACK_LEVEL_PATH_IN_SPEC="Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels";
	public static final String UOM_PATH = "Unit of Measure";
	public static final String CONST_PART_NO = "Part Number";
	public static final String CONST_SYS_RECORD = "SystemOfRecord";
	public static final String CONST_DESC = "DESCRIPTION";
	public static final String CONST_ITEM_STATUS = "ItemStatus";
	public static final String CONST_PRDLINE= "ProductLine";
	public static final String CONST_PRDMGR= "ProductManager";
	public static final String CONST_PRDMGR_NONUSQAD= "ProductManager-Non-USQAD";
	public static final String CONST_DRAWING= "Drawing";
	public static final String CONST_MODELNO= "ModelNumber";
	//public static final String db_url="jdbc:db2://USNA878:50000/PIMDEVDB";
	//public static final String db_url="jdbc:db2://USNA878:50000/PIMDEVDB";
	//public static final String db_class_name="com.ibm.db2.jcc.DB2Driver";
	//public static final String db_userName="pimold";
	//public static final String db_password_plain="pimold123";
	public static final String DATE_FORMAT_FOR_REPORT_FOLDER = "yyyy-MM-dd";
	public static final String COMMON_PROPERTIES = "/properties/wattswater/common.properties";
	public static final String file_count="File_count";
	public static final String MASS_ITEM_EXPORT_PATH = "/rpt1/WattsPrd/mdmpim/test/ReportFiles/MassItemExport/";
	public static final String DATE_FORMAT_FOR_REPORT_FILES = "yyyy-MM-dd_HH-mm-ss";
	public static final String WORKFLOW_LOGGER_NAME = "WattsWorkflowLog";
	public static final String PRODUCT_CATALOG_GLOBAL_SPEC = "Product Catalog Global Spec";
	public static final String PACKAGE_GTIN = "PackageGTIN";
	public static final String BAR_CODE_TYPE = "BarCodeType";
	public static final String IMPORT_EXPORT_CONFIG = "Import-Export Configuration";
	public static final String IMEXPT_ATTR_MAPPING = "Import-Export Configuration Spec/Attribute Mapping";
	public static final String FERGUSON_CATEGORY_NAME = "Ferguson";
	public static final String CUSTOMER_ATTRIBUTE = "/Customer Attribute";
	public static final String PRODUCT_ATTRIBUTE = "/Product Attribute";
	public static final String FERGUSON_EMPTY_ATTRS = "__NULL_";
}
