package com.watts.phaseII.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WattsConstants 
{
	public static final String LOGGER_NAME = "com.ibm.ccd.wpc_user_scripting.Watts_WorflowFunctions";
	
	public static final String MAIN_CATALOG = "Product Catalog";
	
	//public static final String MAIN_COLLAB_AREA = "QAD Parts Maintenance Staging Area";
	public static final String MAIN_COLLAB_AREA = "Item Maintenance Staging Area";
	
	public static final String FIRST_COLLAB_STEP_NAME = "Product Maintenance";
	
	public static final String PM_STEP_NAME = "Product Management Maintenance";
	public static final String MARPDS_STEP_NAME = "Marcom Product Data Services Maintenance";
	public static final String ENGG_STEP_NAME = "Engineering Maintenance";
	
	public static final String MASS_IMPORT_NAME = "Mass Update Import";
	public static final String MASS_IMPORT_JOB_TYPE = "Imports";
	public static final String EMAIL_FILE_DATE_FORMAT = "yyyyMMdd_HHmm_ss";
	
	public static final String MASS_IMPORT_AUDIT_COLUMNS = "Date,Time,SystemOfRecord,Part Number,Workflow Step,User Name,Message,Attribute,Attribute Value";
	
	
	public static final String TO_ERP_DOCSTORE_FILE_PATH = "/to_usqad_test/";
	
	public static final String PRIMARY_SPEC_NAME = "Product Catalog Global Spec";
	
	public static final String EMAIL_RECEIPIENTS_PATH = "Product Catalog Global Spec/Engg Email Receipients";
	
	public static final String USER_ROLE_PATH = "Product Catalog Global Spec/User_Role";
	public static final String ENTRY_PROCESSED_PATH = "Product Catalog Global Spec/Is_Entry_Processed";
	public static final String PART_NUMBER_PATH = "Product Catalog Global Spec/Part Number";
	public static final String PIM_COMPLETE_PATH = "Product Catalog Global Spec/PIMComplete";
	public static final String SYSTEM_OF_RECORD_PATH = "Product Catalog Global Spec/SystemOfRecord";
	
	public static final String UNIQUE_QUALI_PATH = "Product Catalog Global Spec/UniqueProductQualifier";
	
	public static final String HIGHER_PACKAGING_PATHS = "Product Catalog Global Spec/Packaging Hierarchy/Paths";
	
	public static final String CHECKOUT_FLAG_PATH = "Product Catalog Global Spec/Is_Checked_To_First_Step";
	
	//Audit Constants
	public static final String AUDIT_ERRORREPORT_COLUMN_DEFAULT_DELIMITER = "|";

	//Default formatter to be used for Date column in Audit Trail Error Reports
	public static final String AUDIT_ERRORREPORT_DATE_DEFAULT_FORMATTER = "yyyy/MM/dd";

	//Default formatter to be used for Time column in Audit Trail Error Reports
	public static final String AUDIT_ERRORREPORT_TIME_DEFAULT_FORMATTER = "HH:mm:ss:SSS";

	//Default formatter to be used for suffix of Audit Trail Error Report Filenames
	public static final String AUDIT_ERRORREPORT_FILENAME_SUFFIX_DEFAULT_FORMATTER = "yyyy_MM_dd_HH_mm_ss_SSS";

	//Default extension for Audit Trail Error Report Filenames
	public static final String AUDIT_ERRORREPORT_FILE_DEFAULT_EXTENSION = ".txt";
	
	
	public static final String SECONDARY_SPEC_NAME_POSTFIX = " Specific Attributes";
	public static final String TEMPLATES_HIERARCHY_NAME = "Templates Hierarchy";
	
	
	
	
	public static final List<String> sysOfRecordList = Collections.unmodifiableList(Arrays.asList("Canada-QAD","Enterprise","Macola","Real World","SyteLine","US-QAD"));
	
															
	
	public static final Map<String,String> headerNameToAttrPathMap = new HashMap<String,String>();
													
	static
	{
		headerNameToAttrPathMap.put("PIM_ID","Product Catalog Global Spec/PIM_ID");
		headerNameToAttrPathMap.put("SystemOfRecord","Product Catalog Global Spec/SystemOfRecord");
		headerNameToAttrPathMap.put("ItemStatus","Product Catalog Global Spec/ItemStatus");
		headerNameToAttrPathMap.put("ADACompliant","Product Catalog Global Spec/ADACompliant");
		headerNameToAttrPathMap.put("BuyAmericanAct","Product Catalog Global Spec/BuyAmericanAct");
		headerNameToAttrPathMap.put("CaliforniaEnergyCommissionListed","Product Catalog Global Spec/CaliforniaEnergyCommissionListed");
		headerNameToAttrPathMap.put("DataPoolItemStatus","Product Catalog Global Spec/DataPoolItemStatus");
		headerNameToAttrPathMap.put("FinishedGood","Product Catalog Global Spec/FinishedGood");
		headerNameToAttrPathMap.put("HasBatchNumber","Product Catalog Global Spec/HasBatchNumber");
		headerNameToAttrPathMap.put("HazardousMaterialClassification","Product Catalog Global Spec/HazardousMaterialClassification");
		headerNameToAttrPathMap.put("IsItemAvailableForDirectToConsumerDelivery","Product Catalog Global Spec/IsItemAvailableForDirectToConsumerDelivery");
		headerNameToAttrPathMap.put("IsItemAvailableForSpecialOrder","Product Catalog Global Spec/IsItemAvailableForSpecialOrder");
		headerNameToAttrPathMap.put("IsItemSubjectToUSPatent","Product Catalog Global Spec/IsItemSubjectToUSPatent");
		headerNameToAttrPathMap.put("IsNonSoldTradeItemReturnable","Product Catalog Global Spec/IsNonSoldTradeItemReturnable");
		headerNameToAttrPathMap.put("IsTradeItemInformationPrivate","Product Catalog Global Spec/IsTradeItemInformationPrivate");
		headerNameToAttrPathMap.put("IsTradeItemRecalled","Product Catalog Global Spec/IsTradeItemRecalled");
		headerNameToAttrPathMap.put("IsWoodAComponentOfThisItem","Product Catalog Global Spec/IsWoodAComponentOfThisItem");
		headerNameToAttrPathMap.put("Kit-Ferguson","Product Catalog Global Spec/Kit-Ferguson");
		headerNameToAttrPathMap.put("LeadFree","Product Catalog Global Spec/LeadFree");
		headerNameToAttrPathMap.put("NSFSafetyListing","Product Catalog Global Spec/NSFSafetyListing");
		headerNameToAttrPathMap.put("ObsoleteStockAvailable-Ferguson","Product Catalog Global Spec/ObsoleteStockAvailable-Ferguson");
		headerNameToAttrPathMap.put("OkForNonPotableWaterApplications","Product Catalog Global Spec/OkForNonPotableWaterApplications");
		headerNameToAttrPathMap.put("ORMD-Ferguson","Product Catalog Global Spec/ORMD-Ferguson");
		headerNameToAttrPathMap.put("PackagingException","Product Catalog Global Spec/PackagingException");
		headerNameToAttrPathMap.put("PackagingStatus","Product Catalog Global Spec/PackagingStatus");
		headerNameToAttrPathMap.put("PIMComplete","Product Catalog Global Spec/PIMComplete");
		headerNameToAttrPathMap.put("PurMfg","Product Catalog Global Spec/PurMfg");
		headerNameToAttrPathMap.put("SDSRequired","Product Catalog Global Spec/SDSRequired");
		headerNameToAttrPathMap.put("SerializedProduct","Product Catalog Global Spec/SerializedProduct");
		headerNameToAttrPathMap.put("StockStatus","Product Catalog Global Spec/StockStatus");
		headerNameToAttrPathMap.put("TargetAudience","Product Catalog Global Spec/TargetAudience");
		headerNameToAttrPathMap.put("ThirdPartyCertified","Product Catalog Global Spec/ThirdPartyCertified");
		headerNameToAttrPathMap.put("WQACertified","Product Catalog Global Spec/WQACertified");
		headerNameToAttrPathMap.put("PriceListName","Product Catalog Global Spec/PriceListName");
		headerNameToAttrPathMap.put("BrandName","Product Catalog Global Spec/BrandName");
		headerNameToAttrPathMap.put("EnvironmentalIdentifier","Product Catalog Global Spec/EnvironmentalIdentifier");
		headerNameToAttrPathMap.put("EA:IsTradeItemAVariableUnit","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAVariableUnit");
		headerNameToAttrPathMap.put("EA:IsTradeItemMarkedAsRecyclable","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemMarkedAsRecyclable");
		headerNameToAttrPathMap.put("EA:SecurityTagLocation","Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagLocation");
		headerNameToAttrPathMap.put("EA:SecurityTagType","Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagType");
		headerNameToAttrPathMap.put("EA:ShelfLife","Product Catalog Global Spec/Packaging Hierarchy/Each Level/ShelfLife");
		headerNameToAttrPathMap.put("EA:IsBarcodeSymbologyDerivable","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsBarcodeSymbologyDerivable");
		headerNameToAttrPathMap.put("EA:IsNetContentDeclarationIndicated","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsNetContentDeclarationIndicated");
		headerNameToAttrPathMap.put("EA:IsPackagingMarkedReturnable","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsPackagingMarkedReturnable");
		headerNameToAttrPathMap.put("EA:IsPackagingMarkedWithIngredients","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsPackagingMarkedWithIngredients");
		headerNameToAttrPathMap.put("EA:IsSecurityTagPresent","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsSecurityTagPresent");
		headerNameToAttrPathMap.put("EA:ProductTypeCode","Product Catalog Global Spec/Packaging Hierarchy/Each Level/ProductTypeCode");
		headerNameToAttrPathMap.put("INNER:ProductTypeCode","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/ProductTypeCode");
		headerNameToAttrPathMap.put("MASTER:ProductTypeCode","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/ProductTypeCode");
		headerNameToAttrPathMap.put("PALLET:ProductTypeCode","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/ProductTypeCode");
		headerNameToAttrPathMap.put("EA:EANUCCType","Product Catalog Global Spec/Packaging Hierarchy/Each Level/EANUCCType");
		headerNameToAttrPathMap.put("INNER:EANUCCType","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/EANUCCType");
		headerNameToAttrPathMap.put("MASTER:EANUCCType","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/EANUCCType");
		headerNameToAttrPathMap.put("PALLET:EANUCCType","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/EANUCCType");
		headerNameToAttrPathMap.put("EA:IsTradeItemABaseUnit","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemABaseUnit");
		headerNameToAttrPathMap.put("INNER:IsTradeItemABaseUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemABaseUnit");
		headerNameToAttrPathMap.put("MASTER:IsTradeItemABaseUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemABaseUnit");
		headerNameToAttrPathMap.put("PALLET:IsTradeItemABaseUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemABaseUnit");
		headerNameToAttrPathMap.put("EA:IsTradeItemAConsumerUnit","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAConsumerUnit");
		headerNameToAttrPathMap.put("INNER:IsTradeItemAConsumerUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAConsumerUnit");
		headerNameToAttrPathMap.put("MASTER:IsTradeItemAConsumerUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAConsumerUnit");
		headerNameToAttrPathMap.put("PALLET:IsTradeItemAConsumerUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAConsumerUnit");
		headerNameToAttrPathMap.put("EA:IsTradeItemADespatchUnit","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit");
		headerNameToAttrPathMap.put("INNER:IsTradeItemADespatchUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemADespatchUnit");
		headerNameToAttrPathMap.put("MASTER:IsTradeItemADespatchUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemADespatchUnit");
		headerNameToAttrPathMap.put("PALLET:IsTradeItemADespatchUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemADespatchUnit");
		headerNameToAttrPathMap.put("EA:IsTradeItemAnInvoiceUnit","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAnInvoiceUnit");
		headerNameToAttrPathMap.put("INNER:IsTradeItemAnInvoiceUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAnInvoiceUnit");
		headerNameToAttrPathMap.put("MASTER:IsTradeItemAnInvoiceUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAnInvoiceUnit");
		headerNameToAttrPathMap.put("PALLET:IsTradeItemAnInvoiceUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAnInvoiceUnit");
		headerNameToAttrPathMap.put("EA:IsTradeItemAnOrderableUnit","Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAnOrderableUnit");
		headerNameToAttrPathMap.put("INNER:IsTradeItemAnOrderableUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAnOrderableUnit");
		headerNameToAttrPathMap.put("MASTER:IsTradeItemAnOrderableUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAnOrderableUnit");
		headerNameToAttrPathMap.put("PALLET:IsTradeItemAnOrderableUnit","Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels/IsTradeItemAnOrderableUnit");

	}
	
	
}
