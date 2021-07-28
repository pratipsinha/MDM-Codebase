package com.ww.pim.extensionpoints.containers.catalogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.ibm.pim.attribute.AttributeChanges;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.common.ValidationError.Type;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.CategoryPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.CollaborationCategoryPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.CollaborationItemPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.ItemPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.PrePostProcessingFunction;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.common.utils.UtilityMethods;

public class ProductCatalogPostProcessing_Back implements PrePostProcessingFunction{
	UtilityMethods utilityMethods = null;
	@Override
	public void prePostProcessing(ItemPrePostProcessingFunctionArguments arg0) {
		System.out.println("CATALOG : Product Catalog Post processing called ......: [OK]");
		Context ctx = PIMContextFactory.getCurrentContext();
		//Catalog productCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
		Logger logger = ctx.getLogger(Constant.WORKFLOW_LOGGER_NAME);
		logger.logInfo("CATALOG : Product Catalog Post processing called : [OK]");
		Item catalogItem = arg0.getItem();
		catalogItem.getCatalog().getProcessingOptions().setCollaborationAreaLocksValidationProcessing(false);
		//Item catalogItem = (Item) catalogItem1.getOriginalEntry();
		AttributeChanges attributeChanges = null; 
		logger.logInfo("CATALOG :-----1--------");
		String itmPrimaryKey=null;
		String partNumber = null;
		String sysOfRec = null;
		List<? extends AttributeInstance> modifiedAttributesWithnewDataList = null;
		
		if (null != catalogItem){
			utilityMethods = new UtilityMethods();
			try{
				logger.logInfo("CATALOG : -----2--------");
				itmPrimaryKey = catalogItem.getPrimaryKey();
			partNumber = (null != catalogItem.getAttributeValue("Product Catalog Global Spec/Part Number"))? catalogItem.getAttributeValue("Product Catalog Global Spec/Part Number").toString():null;
			sysOfRec = (null != catalogItem.getAttributeValue("Product Catalog Global Spec/SystemOfRecord"))? catalogItem.getAttributeValue("Product Catalog Global Spec/SystemOfRecord").toString():null;
			logger.logInfo("CATALOG : Part Number "+partNumber);
			logger.logInfo("CATALOG : System of Record "+sysOfRec);
			
			// Rule 62.1 Start 
			logger.logDebug("CATALOG : start // Rule 62..");
			//productCatalog.getProcessingOptions().setCollaborationAreaLocksValidationProcessing(false);

			// Setting AlternateItemIdentificationId with ModelNumber

			if(null !=  catalogItem.getAttributeValue("Product Catalog Global Spec/ModelNumber"))
			{
				String modelNo = (String)catalogItem.getAttributeValue("Product Catalog Global Spec/ModelNumber");
				catalogItem.setAttributeValue("Product Catalog Global Spec/AlternateItemIdentificationId",modelNo);
			}
			
			 // Rule 90.1 Start 
			/*logger.logDebug("start // Rule 90..");

			// Going to set SpecificationSheetUsage = 'DOCUMENT' If it is null and SpecificationSheetFileName is not null

			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetUsage")  && null != catalogItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetFileName"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/SpecificationSheetUsage","DOCUMENT") ;
			}*/
			 // Rule 90.1 End 
			
			// Rule 90.2 Start 
			// Going to set SpecificationSheetFileType = 'PDF' If it is null and SpecificationSheetFileName is not null
			/*logger.logDebug("start // Rule 90.2..");
			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetFileType")  && null != catalogItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetFileName"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/SpecificationSheetFileType","PDF") ;
			}*/
			 // Rule 90.2 End
			
			
			 // Rule 62.1 End 
			
			// Rule 80.1 Start 
			/*logger.logDebug("start // Rule 80.1.");

			// Setting  InstructionSheetUsage and InstructionSheetFileType based on certain business criterias

			if(null !=  catalogItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileName") && null == catalogItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetUsage"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetUsage","DOCUMENT");
			}
			 // Rule 80.1 End 
			
			 // Rule 80.3 Start 
			logger.logDebug("start // Rule 80.3..");
			if(null  ==  catalogItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileName") )
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetUsage",null);
				catalogItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetFileType",null);
			}
			 // Rule 80.3 End 
			
			 // Rule 80.2 Start 
			logger.logDebug("start // Rule 80.2..");
			if(null !=  catalogItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileName") && null == catalogItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileType"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetFileType","PDF");
			}
			 // Rule 80.2 End 
			*/
			// Rule 113.1 Start 
			logger.logDebug("start // Rule 113..");
			// Going to populate Terms attribute as '2%10TH PROX/N30' if it is empty

			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/Terms"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/Terms","2%10TH PROX/N30");
			}
			 // Rule 113.1 End 
			
			// Rule 84.1 Start 
			logger.logDebug("start // Rule 84..");

			// Going to set LF-NSF-ANSI-14_CertFileName = null If LF-NSF-ANSI-14_CertURL = null

			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-14_CertURL"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-14_CertFileName",null) ;
			}
			 // Rule 84.1 End
			
			 // Rule 84.2 Start 
			// Going to set LF-NSF-ANSI-372_CertFileName = null If LF-NSF-ANSI-372_CertURL= null
			logger.logDebug("start // Rule 84.2..");
			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-372_CertURL"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-372_CertFileName",null) ;
			}
			 // Rule 84.2 End 
			
			 // Rule 84.3 Start 
			// Going to set LF-NSF-ANSI-61-G_CertFileName = null If LF-NSF-ANSI-61-G_CertURL= null
			logger.logDebug("start // Rule 84.3..");
			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-61-G_CertURL"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-61-G_CertFileName",null) ;
			}
			 // Rule 84.3 End 
			
			 // Rule 84.4 Start 
			// Going to set LF-NSF-LowLeadCertProgram_CertFileName = null If LF-NSF-LowLeadCertProgram_CertURL= null
			logger.logDebug("start // Rule 84.4..");
			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-LowLeadCertProgram_CertURL"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-LowLeadCertProgram_CertFileName",null) ;
			}
			 // Rule 84.4 End 
			
			 // Rule 84.5 Start 
			logger.logDebug("start // Rule 84.5..");
			// Going to set LF-WQA-NSFANSI-EquivalentStandard_CertFileName = null If LF-WQA-NSFANSI-EquivalentStandard_CertURL= null

			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/LF-WQA-NSFANSI-EquivalentStandard_CertURL"))
			{
				catalogItem.setAttributeValue("Product Catalog Global Spec/LF-WQA-NSFANSI-EquivalentStandard_CertFileName",null) ;
			}
			 // Rule 84.5 End 
			
			// Rule 99.1 Start 
			logger.logDebug("start // Rule 99..");

			// Going to populate UniqueProductQualifier  by concatenating Part Number and SystemOfRecord

			if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/UniqueProductQualifier") && null != partNumber &&  null != sysOfRec )
			{
				String uniqueProdQualifier = partNumber+" "+sysOfRec;
				catalogItem.setAttributeValue("Product Catalog Global Spec/UniqueProductQualifier",uniqueProdQualifier);
			}
			 // Rule 99.1 End 
			// Going to set Warrenty Attributes 
			logger.logDebug("start // Rule 143.1..");
			if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/WarrantyName") )
			{
				String warrantyName = catalogItem.getAttributeValue("Product Catalog Global Spec/WarrantyName").toString();
				String[] lookupValues = utilityMethods.getLkpValuesForKey(ctx,"WPT_Warranty_Attrib_Lkp",warrantyName,logger);
				catalogItem.setAttributeValue("Product Catalog Global Spec/WarrantyPeriod",lookupValues[0]) ;
				catalogItem.setAttributeValue("Product Catalog Global Spec/WarrantyBlurb",lookupValues[1]) ;
				catalogItem.setAttributeValue("Product Catalog Global Spec/WarrantyLink",lookupValues[2]) ;
			}
			 // Rule 143.1 End 
			 // Rule 95.1 Start 
			logger.logDebug("CATALOG :start // Rule 95..");

			// Going to fetch  and set ClassificationCategoryCode-GPC  from ClassificationCategoryName-GPC

			if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/ClassificationCategoryName-GPC") )
			{
				logger.logInfo("CATALOG :Testjj");
				String classificationCategoryName = catalogItem.getAttributeValue("Product Catalog Global Spec/ClassificationCategoryName-GPC").toString() ;
				logger.logInfo(classificationCategoryName);
				String classificationCatCode = utilityMethods.getLkpValueForKey(ctx, "gpcBrickName - GDSN Table", classificationCategoryName, logger);
				logger.logInfo(classificationCatCode);
				if(null != classificationCatCode)
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/ClassificationCategoryCode-GPC",classificationCatCode);
				}
			}
			 // Rule 95.1 End 
			
			// Rule 85.1 Start 
			logger.logDebug("CATALOG : start // Rule 85..");

			// Going to set DataPoolItemStatus based on ItemStatus value

			if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/ItemStatus"))
			{
				String itemStatusVal = catalogItem.getAttributeValue("Product Catalog Global Spec/ItemStatus").toString() ;
				if(itemStatusVal.equals("AC") || itemStatusVal.equals("HOLD")  || itemStatusVal.equals("INXS")   || itemStatusVal.equals("PO SELL") )
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/DataPoolItemStatus","ACTIVE") ;
				}
				else if(itemStatusVal.equals("RESTRICT") || itemStatusVal.equals("NFS")  || itemStatusVal.equals("CR") )
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/DataPoolItemStatus","OBSOLETE") ;
				}
				else if(itemStatusVal.equals("PP") || itemStatusVal.equals("PILOT")  || itemStatusVal.equals("PM")  || itemStatusVal.equals("PHANTOM"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/DataPoolItemStatus","NO SYNC") ;
				}
			}
			 // Rule 85.1 End
			
			// Rule 138.1 Start 
			logger.logDebug("start // Rule 138..");

			// Going to populate EANUCC-ManufacturerPortion attribute if EANUCCCode is not empty
			if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode"))
			{
				String eanUCCCode = (String) catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode");
				if(eanUCCCode.length() > 5)
				{
					String eanUCCManufacturerPortion = "";
					if (eanUCCCode.startsWith("8402130")){
						eanUCCManufacturerPortion = eanUCCCode.substring(0, 7);
					}else{
						eanUCCManufacturerPortion = eanUCCCode.substring(0, 6);
					}
					catalogItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ManufacturerPortion", eanUCCManufacturerPortion);
				}

			}
			 // Rule 138.1 End 
			
			 // Rule 86.1 Start 
			logger.logDebug("start // Rule 86..");

			// IF length (EANUCCCode) > 5 then  item['EANUCC-ProductPortion'] = item['EANUCCCode'].substring ( item['EANUCCCode'].length() - 6 ); 

			if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode"))
			{
				String eanUCCCode = (String)catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode");
				String trimmedValue="";
				if(eanUCCCode.startsWith("8402130")){
					trimmedValue = eanUCCCode.substring(eanUCCCode.length()-5);
				}else{
					trimmedValue = eanUCCCode.substring(eanUCCCode.length()-6);
				}
				catalogItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ProductPortion",trimmedValue);
			}
			 // Rule 86.1 End 
			
			// Rule 136.1 and 135.1 Start 
			
			logger.logDebug("start // Rule 135 & // Rule 136..");
			if(null !=catalogItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US")  )
			{
				String shortDesc_enUS = (String)catalogItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US") ;
				catalogItem.setAttributeValue("Product Catalog Global Spec/GtinName_en-US", shortDesc_enUS);
				catalogItem.setAttributeValue("Product Catalog Global Spec/FunctionalName_en-US", shortDesc_enUS);
			}
			 // Rule 136.1 and 135.1 End 
			
			// Rule 100.1 Start 
			logger.logDebug("start // Rule 100..");

			// Going to populate LongDescription_en-US  by concatenating BrandName and DescriptionShort_en-US

			String brandName = (null != catalogItem.getAttributeValue("Product Catalog Global Spec/BrandName"))?catalogItem.getAttributeValue("Product Catalog Global Spec/BrandName").toString():"";
			//String brandName = collabItem.getAttributeValue("Product Catalog Global Spec/BrandName").toString();
			String shortDesc = (null != catalogItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US"))?catalogItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US").toString():"";
			//String shortDesc = (String)collabItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US");
			String longDesc = brandName+" "+shortDesc;
			catalogItem.setAttributeValue("Product Catalog Global Spec/LongDescription_en-US",longDesc.trim());
			
			/*if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/BrandName") &&  null != catalogItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US") )
			{
				String brandName = catalogItem.getAttributeValue("Product Catalog Global Spec/BrandName").toString();
				String shortDesc = (String)catalogItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US");
				String longDesc = brandName+" "+shortDesc;
				catalogItem.setAttributeValue("Product Catalog Global Spec/LongDescription_en-US",longDesc);
				
			}*/
			 // Rule 100.1 End 
			
			 logger.logDebug("start // Rule 139..");
				if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription", "Call for Authorization");
				}
				  // Rule 139.1 End 
				
				 // Rule 139.2 Start 
				logger.logDebug("start // Rule 139.2..");
				if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription")){
					catalogItem.setAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyCode",utilityMethods.getLkpValueForKey(ctx,"ReturnGoodsPolicy - GDSN Table",
							catalogItem.getAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription").toString(),logger));
				}
			     // Rule 139.2 End 
			    
				// Rule 134.1 Start 
				logger.logDebug("start // Rule 134..");
				// Going to populate TradeItemMarketingMessage_en-US attribute with value as Description-Marketing attribute if it is empty
				if(null !=catalogItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") )
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/TradeItemMarketingMessage_en-US", catalogItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") );
				}
				 // Rule 134.1 End 
				
				 // Rule 4.3 Start 
				logger.logDebug("start // Rule 4.3..");
				if (null == catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique")){
					if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode") && null != sysOfRec){
						logger.logInfo("Item : "+itmPrimaryKey +" EANUCCCode is Not Null. Calculating EANUCCCode_Unique");
						catalogItem.setAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique",catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode") + " " + sysOfRec);
					}
				}
				 // Rule 4.3 End 
				
				 // Rule 4.4 Start 
				logger.logDebug("start // Rule 4.4..");
				if (null == catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode")){
					logger.logInfo("Item : "+itmPrimaryKey +" EANUCCCode is Null. Setting EANUCCCode_Unique,EA:PackageGTIN,EA_PackageGTIN_Unique to Null");
					if( null != catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique"))
						catalogItem.setAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique", null);
					if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique"))
						catalogItem.setAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique", null);
					if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN"))
						catalogItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN", null);
					if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCC-ProductPortion"))
						catalogItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ProductPortion",null);
					if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCC-ManufacturerPortion"))
						catalogItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ManufacturerPortion",null);
				}
				 // Rule 4.4 End 
				
				 // Rule 4.12 Start 
				logger.logDebug("start // Rule 4.12..");
				if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US") && null != sysOfRec){
					logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" AdditionalTradeItemDescription_en-US is Not Null. Calculating AdditionalTradeItemDescription_en-US_Unique");
					catalogItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US_Unique",catalogItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US") + " " + sysOfRec);
				}
				 // Rule 4.12 End 
				
				 // Rule 4.13 Start 
				logger.logDebug("start // Rule 4.13..");
				if (null == catalogItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US")){
					logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" AdditionalTradeItemDescription_en-US is Null. Setting AdditionalTradeItemDescription_en-US_Unique to Null");
					catalogItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US_Unique",null);
				}
				 // Rule 4.13 End 
				
				// Rule 4.1 Start 
				logger.logDebug("start // Rule 4.1..");
				if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/ModelNumber") && null != sysOfRec){
					logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" ModelNumber is Not Null. Calculating ModelNumber_Unique");
					catalogItem.setAttributeValue("Product Catalog Global Spec/ModelNumber_Unique",catalogItem.getAttributeValue("Product Catalog Global Spec/ModelNumber") + " " + sysOfRec);
				}
				 // Rule 4.1 End 
				
				 // Rule 4.2 Start 
				logger.logDebug("start // Rule 4.2..");
				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/ModelNumber_Unique")){
					if ( catalogItem.getAttributeValue("Product Catalog Global Spec/ModelNumber") ==  null ) {
						logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" ModelNumber is Null. Setting ModelNumber_Unique to Null");
						catalogItem.setAttributeValue("Product Catalog Global Spec/ModelNumber_Unique",null);
					 }
				}
				 // Rule 4.2 End
				
				// Rule 4.14 Start 
				logger.logDebug("start // Rule 4.14..");
				if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") && null != sysOfRec){
					logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" Description-Marketing is Not Null. Calculating Description-Marketing_Unique");
					catalogItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing_Unique",catalogItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") + " " + sysOfRec);
				}
				 // Rule 4.14 End 
				
				 // Rule 4.15 Start 
				logger.logDebug("start // Rule 4.15..");
				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing_Unique")){
					if (null == catalogItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing")){
						logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" Description-Marketing is Null. Setting Description-Marketing_Unique to Null");
						catalogItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing_Unique",null);
					}
				}
				 // Rule 4.15 End 
				
				
				
				 // Rule 4.5 Start 
				logger.logDebug("start // Rule 4.5..");
				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique")){
					if (null == catalogItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN")){
						logger.logInfo("Item : "+itmPrimaryKey +" EA:PackageGTIN is Null. Calculating EA_PackageGTIN_Unique");
						catalogItem.setAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique", null);
					}
				}
				 // Rule 4.5 End
				
				// Rule 101.1 Start 
				logger.logDebug("start // Rule 101	..");
	
				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode"))
				{
					String upcCode = (String)catalogItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode");
					String ea_PkgGTIN_Unique = "00"+upcCode+" "+sysOfRec;
					catalogItem.setAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique",ea_PkgGTIN_Unique);
					catalogItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN","00"+upcCode);
				}
				 // Rule 101.1 End 
				
				// Rule 98.1 Start 
				logger.logDebug("start // Rule 98.1..");
				if (null != catalogItem && null != catalogItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingTypeDescription")){
					catalogItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", catalogItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingTypeDescription").toString(), logger));
				}
				 // Rule 98.1 End 
				
				if (catalogItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size()>0 && catalogItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size()> 0){
					logger.logInfo("Item : "+itmPrimaryKey + " Higher Packaging Exists");
					Iterator<? extends AttributeInstance> packLvlIter = catalogItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
					while(packLvlIter.hasNext()){
						AttributeInstance chldIns = packLvlIter.next();
						String higherPackLvlPath = chldIns.getPath();
						logger.logInfo("Item : "+itmPrimaryKey + "Path "+higherPackLvlPath);
						if (null != catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "INNER".equalsIgnoreCase(catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
							// Rule 4.6 Start 
							logger.logDebug("start // Rule 4.6..");
							if (null != catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
								logger.logInfo("Item : "+itmPrimaryKey+" Inner Package GTIN is Not empty. Calculating  Inner_PackageGTIN_Unique value ");
								catalogItem.setAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique", catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
							}
							 // Rule 4.6 End 
							
							 // Rule 4.7 Start 
							logger.logDebug("start // Rule 4.7..");
							if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique")){
								if (null == catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
									logger.logInfo("Item : "+itmPrimaryKey+" Inner Package GTIN is empty. Setting  Inner_PackageGTIN_Unique to Null");
									catalogItem.setAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique", null);
								}
							}
							 // Rule 4.7 End
							
							// Rule 98.3 Start 
							logger.logDebug("start // Rule 98.3..");
							if (null != catalogItem && null != catalogItem.getAttributeValue(higherPackLvlPath + "/PackagingTypeDescription")){
								catalogItem.setAttributeValue(higherPackLvlPath + "/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", catalogItem.getAttributeValue(higherPackLvlPath+ "/PackagingTypeDescription").toString(), logger));
							}
							 // Rule 98.3 End 
							
						}
						
						if (null != catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "MASTER".equalsIgnoreCase(catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
							// Rule 4.8 Start 
							logger.logDebug("start // Rule 4.8..");
							if (null != catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
								logger.logInfo("Item : "+itmPrimaryKey+" MASTER-02 Package GTIN is Not empty. Calculating  Master_PackageGTIN_Unique value ");
								catalogItem.setAttributeValue("Product Catalog Global Spec/Master_PackageGTIN_Unique", catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
							}
							 // Rule 4.8 End 
							
							 // Rule 4.9 Start 
							logger.logDebug("start // Rule 4.9..");
							if (null == catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
								logger.logInfo("Item : "+itmPrimaryKey+" MASTER-02 Package GTIN is empty. Setting  Master_PackageGTIN_Unique to Null");
								catalogItem.setAttributeValue("Product Catalog Global Spec/Master_PackageGTIN_Unique", null);
							}
							 // Rule 4.9 End
							
							// Rule 98.3 Start 
							logger.logDebug("start // Rule 98.3..");
							if (null != catalogItem && null != catalogItem.getAttributeValue(higherPackLvlPath + "/PackagingTypeDescription")){
								catalogItem.setAttributeValue(higherPackLvlPath + "/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", catalogItem.getAttributeValue(higherPackLvlPath+ "/PackagingTypeDescription").toString(), logger));
							}
							 // Rule 98.3 End 
						}
						
						if (null != catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "PALLET".equalsIgnoreCase(catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
							 // Rule 4.10 Start 
							logger.logDebug("start // Rule 4.10..");
							if (null != catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
								logger.logInfo("Item : "+itmPrimaryKey+" PALLET Package GTIN is Not empty. Calculating  Pallet_PackageGTIN_Unique value ");
								catalogItem.setAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique", catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
							}
							 // Rule 4.10 End 
							
							 // Rule 4.11 Start 
							logger.logDebug("start // Rule 4.11..");
							if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique")){
								if (null == catalogItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
									logger.logInfo("Item : "+itmPrimaryKey+" PALLET Package GTIN is empty. Setting  Pallet_PackageGTIN_Unique to Null");
									catalogItem.setAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique", null);
								}
							}
							 // Rule 4.11 End
							
							// Rule 98.3 Start 
							logger.logDebug("start // Rule 98.3..");
							if (null != catalogItem && null != catalogItem.getAttributeValue(higherPackLvlPath + "/PackagingTypeDescription")){
								catalogItem.setAttributeValue(higherPackLvlPath + "/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", catalogItem.getAttributeValue(higherPackLvlPath+ "/PackagingTypeDescription").toString(), logger));
							}
							 // Rule 98.3 End 
						}
					}
				}
				
				 // Rule 14.1 Start 
				logger.logDebug("start // Rule 14.1..");
				if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US")){
					catalogItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", catalogItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US"));
				}else{
					if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/Description1")
							&& null != catalogItem.getAttributeValue("Product Catalog Global Spec/Description2")){
						catalogItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", new StringBuilder(catalogItem.getAttributeValue("Product Catalog Global Spec/Description1").toString()).append(" ").append(catalogItem.getAttributeValue("Product Catalog Global Spec/Description2").toString()));
					}else if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/Description1")
									&& null == catalogItem.getAttributeValue("Product Catalog Global Spec/Description2")){
						catalogItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", catalogItem.getAttributeValue("Product Catalog Global Spec/Description1").toString());
					}else if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/Description2")
									&& null == catalogItem.getAttributeValue("Product Catalog Global Spec/Description1")){
						catalogItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", catalogItem.getAttributeValue("Product Catalog Global Spec/Description2").toString());
					} 
				}
				 // Rule 14.1 End
				
				 // Rule 89.1 Start 
				// IF ItemStatus= 'RESTRICT' then RestrictDate='Set Current Time'
				logger.logInfo("--------------------Test Start------------------");
				attributeChanges =  catalogItem.getAttributeChangesSinceLastSave();
				logger.logInfo("--------------------Test Start1------------------");
				if (null != attributeChanges){
					logger.logInfo("--------------------Test Start21------------------");
					modifiedAttributesWithnewDataList=new ArrayList<AttributeInstance>();
					List<String> mustEditAttrLst = new ArrayList<String>();
					mustEditAttrLst.add("Product Catalog Global Spec/ItemStatus");
					modifiedAttributesWithnewDataList = attributeChanges.getModifiedAttributesWithNewData();
					List<? extends AttributeInstance> modifiedAttributesWitholdDataList = attributeChanges.getModifiedAttributesWithOldData();
					if (null != modifiedAttributesWithnewDataList && !modifiedAttributesWithnewDataList.isEmpty()){
						logger.logInfo("ITEM Item : "+itmPrimaryKey + " has got Modified attributessss");
						AttributeInstance AttrInsOldVal = null;
						AttributeInstance AttrInsNewVal = null;
						for (int i=0; i< modifiedAttributesWithnewDataList.size();i++){
							logger.logInfo("ITEM Inside 1");
							String attrPath = modifiedAttributesWithnewDataList.get(i).getPath().substring(1);
							logger.logInfo(attrPath);
							if (mustEditAttrLst.contains(attrPath)){
								logger.logInfo("ITEM Match111");
								AttrInsNewVal = modifiedAttributesWithnewDataList.get(i);
							}
						}
						for (int i=0; i< modifiedAttributesWitholdDataList.size();i++){
							logger.logInfo("ITEM Inside 2");
							String attrPath = modifiedAttributesWitholdDataList.get(i).getPath().substring(1);
							
							logger.logInfo(attrPath);
							if (mustEditAttrLst.contains(attrPath)){
								logger.logInfo("ITEM Match222");
								AttrInsOldVal = modifiedAttributesWitholdDataList.get(i);
							}
						}
						logger.logInfo("ITEM AttrInsNewVal.getValue().toString()");
						//logger.logInfo("ITEM AttrInsNewVal.getValue().toString()" + AttrInsNewVal.getValue().toString());
						//logger.logInfo("ITEM AttrInsOldVal.getValue().toString()"+AttrInsOldVal.getValue().toString());
						if (null != AttrInsNewVal && (null != AttrInsNewVal.getValue() && AttrInsNewVal.getValue().toString().equalsIgnoreCase("RESTRICT"))){
							logger.logInfo("ITEM Item : "+itmPrimaryKey + " has ItemStatus Changed to Restrict");
							boolean setRestrictDate = false;
							if (null == AttrInsOldVal ){
								setRestrictDate = true;
							}else if (null == AttrInsOldVal.getValue()){
								setRestrictDate = true;
							}else if (null != AttrInsOldVal.getValue() && !AttrInsOldVal.getValue().toString().equals("RESTRICT")){
								setRestrictDate = true;
							}
							logger.logInfo("ITEM Item : "+itmPrimaryKey + " setRestrictDate : "+ setRestrictDate);
							if (setRestrictDate){
								java.text.DateFormat f = new java.text.SimpleDateFormat ( "MM/dd/yyyy" );
								java.util.Calendar c = java.util.Calendar.getInstance();
								String dateVal = f.format ( c.getTime() );
								logger.logInfo("ITEM Item : "+itmPrimaryKey + " setting RestrictDate : " + dateVal);
								catalogItem.setAttributeValue("Product Catalog Global Spec/RestrictDate",dateVal);
								
							}
						}
					}
				}
				
				 // Rule 89.1 End 
				
				 // Rule 79.1 Start 
				logger.logDebug("Catalog : start // Rule 79.1.");

				// Setting  ImportClassificationValue and ImportClassificationType based on certain business criterias  HTSNumber

				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/HTSNumber"))
				{
					String htsNumber = (String)catalogItem.getAttributeValue("Product Catalog Global Spec/HTSNumber");
					String importClassficationVal = htsNumber.replaceAll("\\.", "");
					catalogItem.setAttributeValue("Product Catalog Global Spec/ImportClassificationValue",importClassficationVal);
				}
				logger.logInfo("Catalog : end // Rule 79.1.");
				 // Rule 79.1 End 
				// Rule 79.2 Start 
				logger.logDebug("Catalog : start // Rule 79.2..");
				if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/ImportClassificationType") && null !=catalogItem.getAttributeValue("Product Catalog Global Spec/ImportClassificationValue") )
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/ImportClassificationType","HTS");
				}
				logger.logDebug("Catalog : end // Rule 79.2..");
				 // Rule 79.2 End 
				
				// Rule 140.1 Start 
				logger.logDebug("Catalog : start // Rule 140.1..");
				if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsBarcodeSymbologyDerivable"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsBarcodeSymbologyDerivable","false");
				}
				logger.logDebug("Catalog : end // Rule 140.1..");
				 // Rule 140.1 End 
				
				// Rule 137.1 Start 
				logger.logDebug("start // Rule 137..");
				// Going to populate ObsoleteStockAvailable-Ferguson attribute with value 'No' if ItemStatus='RESTRICT'

				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/ItemStatus") &&  catalogItem.getAttributeValue("Product Catalog Global Spec/ItemStatus").toString().equals("RESTRICT"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/ObsoleteStockAvailable-Ferguson", "No");
				}
				 // Rule 137.1 End
				
				 // Rule 122.1 Start 
				logger.logDebug("start // Rule 122..");
				// Going to populate ListPriceUnitOfMeasure attribute as EACH CONSUMER UNIT if it is empty
				if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/ListPriceUnitOfMeasure"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/ListPriceUnitOfMeasure","EACH CONSUMER UNIT");
				}
				 // Rule 122.1 End 
				
				// Rule 114.1 Start 
				logger.logDebug("start // Rule 114..");
				// Going to populate AlternateItemIdentificationAgency attribute as 'MNO' if it is empty
				if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/AlternateItemIdentificationAgency"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/AlternateItemIdentificationAgency","MNO");
				}
				 // Rule 114.1 End
				
				// Rule 77.4 Start 
				/*logger.logDebug("start // Rule 77.4..");
				if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoURL_en-US"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1Usage",null);
				}*/
				 // Rule 77.4 End 
				
				
				// Rule 77.3 Start 
				/*logger.logDebug("start // Rule 77.3..");
				if(null == catalogItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoFileName_en-US"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileName_en-US",null);
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileType",null);
				}*/
				 // Rule 77.3 End 
				
				// Rule 77.2 Start 
				logger.logDebug("start // Rule 77.2..");
				
				catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileName_en-US",catalogItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoFileName_en-US"));
					
				 // Rule 77.2 End 
				
				// Rule 77.1 Start 
				logger.logDebug("start // Rule 77.1..");
				
				catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileURL_en-US",catalogItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoURL_en-US"));
					
				// Rule 77.1 End 
				
				// Rule 144 Start 
				logger.logDebug("start // Rule 144..");
				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/CaliforniaEnergyCommissionListed") 
						&& catalogItem.getAttributeValue("Product Catalog Global Spec/CaliforniaEnergyCommissionListed").equals("Compliant") 
						&& (null == catalogItem.getAttributeValue("Product Catalog Global Spec/CaliforniaEnergyCommissionCertifyingAgency")))
				{
					arg0.addValidationError(catalogItem.getAttributeInstance("Product Catalog Global Spec/CaliforniaEnergyCommissionCertifyingAgency"), Type.VALIDATION_RULE, "If CaliforniaEnergyCommissionListed is set to Compliant, CaliforniaEnergyCommissionCertifyingAgency has to be populated.");
				}
				// Rule 144 End
				// Rule 77.6 Start 
				//logger.logDebug("start // Rule 77.6..");
				/*if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/Image1FileURL_en-US"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1Usage","PRODUCT_IMAGE");
				}
				else
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1Usage",null);
				}*/
				 // Rule 77.6 End 
				
				/*logger.logDebug("start // Rule 77.5.");
				if (null != catalogItem.getAttributeValue("Product Catalog Global Spec/Image1FileName_en-US") && 
						catalogItem.getAttributeValue("Product Catalog Global Spec/Image1FileName_en-US").toString().length() > 2){
					
					String raw_1_file_type = catalogItem.getAttributeValue("Product Catalog Global Spec/Image1FileName_en-US").toString().substring ( catalogItem.getAttributeValue("Product Catalog Global Spec/Image1FileName_en-US").toString().length() - 3 );
				    if ( null != raw_1_file_type && !raw_1_file_type.equals("") && raw_1_file_type == "jpg" )
				    {
				        catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileType","JPEG");
				    }
				    else
				    {
				        catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileType",raw_1_file_type);
				    }
				}else{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileType",null);
				}

				logger.logDebug("End // Rule 77.5.");*/
				
				 // Rule 78.1 Start 
				/*logger.logDebug("start // Rule 78..");

				// Setting Image1EffectiveStartDate, Image1FileURL_en-US based on value change of ColorPhotoURL_en-US

				java.text.DateFormat f = new java.text.SimpleDateFormat ( "MM/dd/yyyy" );
				java.util.Calendar c = java.util.Calendar.getInstance();
				String current_date = f.format ( c.getTime() );
				boolean isColorPhotoAttribValChanged = utilityMethods.hasAttributeChangedCatalog(catalogItem, "ColorPhotoURL_en-US",logger);

				if(null != catalogItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoURL_en-US") && isColorPhotoAttribValChanged)
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1EffectiveStartDate",current_date) ;
				}
				else if(null  == catalogItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoURL_en-US") )
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1FileURL_en-US",null) ;
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1EffectiveStartDate",null) ;
				}
				else if(null ==  catalogItem.getAttributeValue("Product Catalog Global Spec/Image1FileURL_en-US"))
				{
					catalogItem.setAttributeValue("Product Catalog Global Spec/Image1EffectiveStartDate",null) ;
				}*/
				 // Rule 78.1 End 
				logger.logInfo("CATALOG : Post processing Completed");
			}catch(Exception e){
				logger.logInfo("CATALOG :  Exception occurred");
				logger.logInfo(e);
				logger.logInfo(e.getMessage());
				e.printStackTrace();
			}
		}
		logger.logInfo("CATALOG Product catalog Post Processing Complete");
		
	}

	@Override
	public void prePostProcessing(
			CategoryPrePostProcessingFunctionArguments arg0) {
		
		
	}

	@Override
	public void prePostProcessing(
			CollaborationItemPrePostProcessingFunctionArguments arg0) {
		System.out.println("Product Catalog Post processing called ......: [OK]");
		Context ctx = PIMContextFactory.getCurrentContext();
		Logger logger = ctx.getLogger(Constant.WORKFLOW_LOGGER_NAME);
		logger.logInfo("Product Catalog Post processing called : [OK]");
		CollaborationItem collabItem = arg0.getCollaborationItem();
		logger.logInfo("-----Collab1--------");
		String itmPrimaryKey=null;
		AttributeChanges attributeChanges = null; 
		CollaborationStep collabStep = null;
		String partNumber = null;
		String sysOfRec = null;
		String stepName = null;
		List<? extends AttributeInstance> modifiedAttributesWithnewDataList = null;
		
		if (null != collabItem){
			logger.logInfo("-----Collab 2--------");
			itmPrimaryKey = collabItem.getPrimaryKey();
			logger.logInfo("-----Collab 3--------"+itmPrimaryKey);
			if (null == collabStep){
				collabStep = arg0.getCollaborationStep();
				logger.logInfo("Collab 4");
				logger.logInfo("Collab 4"+collabStep);
				if (null != collabStep)
				stepName = collabStep.getName();
			}
			partNumber = (null != collabItem.getAttributeValue("Product Catalog Global Spec/Part Number"))? collabItem.getAttributeValue("Product Catalog Global Spec/Part Number").toString():null;
			sysOfRec = (null != collabItem.getAttributeValue("Product Catalog Global Spec/SystemOfRecord"))? collabItem.getAttributeValue("Product Catalog Global Spec/SystemOfRecord").toString():null;
			//sysOfRec = collabItem.getAttributeValue("Product Catalog Global Spec/SystemOfRecord").toString();
		}
		
		try{
			utilityMethods = new UtilityMethods();
			if (null != collabStep){
					
				logger.logInfo("Post Processing for Product Catalog Have been started for Item "+ collabItem.getPrimaryKey() +" in QAD Part Workflow at "+collabStep.getName() +" Step");
				
				
				//PM Step Rules Start
				if (null != collabStep && (collabStep.getName().equalsIgnoreCase("Product Management Maintenance"))){
					
					 
					
					 
					
					if (null == collabItem.getAttributeValue("Product Catalog Global Spec/OrderingLeadTime")){
				    	String stockType = (String)collabItem.getAttributeValue("Product Catalog Global Spec/StockStatus");
				    	if (null != stockType){
				    		if (stockType.equalsIgnoreCase("STK") || stockType.equalsIgnoreCase("ATO") || stockType.equalsIgnoreCase("RST") || stockType.equalsIgnoreCase("KIT")){
				    			collabItem.setAttributeValue("Product Catalog Global Spec/OrderingLeadTime","5");
				    		}else if (stockType.equalsIgnoreCase("MTO")){
				    			collabItem.setAttributeValue("Product Catalog Global Spec/OrderingLeadTime","7");
				    		}
				    	}
				    }
					
					 
					
					 // Rule 88.1 Start 
					logger.logDebug("start // Rule 88..");
	
					List<String> product_certification_attributes = new ArrayList<String>();
					product_certification_attributes.add("Tubing and Hoses Specific Attributes/IAPMOCertified");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-ASSE");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-ASSE-Horizontal");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-ASSE-Other");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-ASSE-VerticalDown");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-ASSE-VerticalUP");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-FM");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-IAPMO-cUPC");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-NSF-372");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-NSF-61G");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-CSA");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-USC");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-USC-Horizontal");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-USC-Other");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-USC-VerticalDown");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-USC-VerticalUP");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Certification-WaterMark");
					product_certification_attributes.add("Backflow Preventers Specific Attributes/Conformance-ANSI-AWWA");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/CACertified");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Anti-Scale Systems Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/CACertified");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Cartridge and Inline Filters Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/CACertified");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Media Filtration Systems Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Resin and Media Specific Attributes/CACertified");
					product_certification_attributes.add("Resin and Media Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Resin and Media Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Resin and Media Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Resin and Media Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Resin and Media Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Resin and Media Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Resin and Media Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Resin and Media Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/CACertified");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Drinking Water Systems Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Food Service Specific Attributes/CACertified");
					product_certification_attributes.add("Food Service Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Food Service Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Food Service Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Food Service Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Food Service Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Food Service Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Food Service Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Food Service Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Housings Specific Attributes/CACertified");
					product_certification_attributes.add("Housings Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Housings Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Housings Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Housings Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Housings Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Housings Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Housings Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Housings Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/CACertified");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Industrial Filtration Housings Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/CACertified");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Reverse Osmosis Systems Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Ultraviolet Specific Attributes/CACertified");
					product_certification_attributes.add("Ultraviolet Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Ultraviolet Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Ultraviolet Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Ultraviolet Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Ultraviolet Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Ultraviolet Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Ultraviolet Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Ultraviolet Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/CACertified");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Ultraviolet - Combo Ports Specific Attributes/NSF-ANSIStandard-61");
					product_certification_attributes.add("Water Softeners Specific Attributes/CACertified");
					product_certification_attributes.add("Water Softeners Specific Attributes/CityofLACertified");
					product_certification_attributes.add("Water Softeners Specific Attributes/ISO9001-2008Certified");
					product_certification_attributes.add("Water Softeners Specific Attributes/NSF-ANSIStandard-372");
					product_certification_attributes.add("Water Softeners Specific Attributes/NSF-ANSIStandard-42");
					product_certification_attributes.add("Water Softeners Specific Attributes/NSF-ANSIStandard-44");
					product_certification_attributes.add("Water Softeners Specific Attributes/NSF-ANSIStandard-53");
					product_certification_attributes.add("Water Softeners Specific Attributes/NSF-ANSIStandard-58");
					product_certification_attributes.add("Water Softeners Specific Attributes/NSF-ANSIStandard-61");
	
					for (String attributePath : product_certification_attributes)
					{
						AttributeInstance attrInst;
						try
						{
							attrInst = collabItem.getAttributeInstance(attributePath);
						}
						catch(Exception ex)
						{
							attrInst = null;
						}
						if(null != attrInst)
						{
							collabItem.setAttributeValue(attributePath, "No");
						}
					}
					 // Rule 88.1 End 
					
				}
				
				
				//PM Step Rules End
				
				//Manual Resolution or PM Start
				if (null != collabStep && (collabStep.getName().equalsIgnoreCase("Product Management Maintenance")) || (collabStep.getName().equalsIgnoreCase("Manual Resolution"))){
					
					 // Rule 12.1 Start 
					logger.logDebug("start // Rule 12.1..");
					if (null != collabItem.getAttributeValue("Product Catalog Global Spec/LeadFree") && collabItem.getAttributeValue("Product Catalog Global Spec/LeadFree").toString().equalsIgnoreCase("NO")){
						if (null == collabItem.getAttributeValue("Product Catalog Global Spec/OkForNonPotableWaterApplications")){
							arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/OkForNonPotableWaterApplications"), Type.VALIDATION_RULE, "OkForNonPotableWaterApplications should be populated if LeadFree is NO");
						}
					}
					 // Rule 12.1 End 
					
					 // Rule 7.1 Start 
					logger.logDebug("start // Rule 7.1..");
					List<String> mustEditAttrLst = new ArrayList<String>();
					mustEditAttrLst.add("Product Catalog Global Spec/Description-Marketing");
					mustEditAttrLst.add("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US");
					for (String attrPath: mustEditAttrLst){
						if (null != collabItem.getAttributeValue(attrPath)){
							//if (collabItem.getAttributeValue(attrPath).toString().endsWith("-Edit before saving!")){
							if (collabItem.getAttributeValue(attrPath).toString().contains("-Edit before saving")){
								arg0.addValidationError(collabItem.getAttributeInstance(attrPath), Type.VALIDATION_RULE, "Must Be edited before Save");
							}
						}
					}
					 // Rule 7.1 End 
					
					 // Rule 13.1 Start 
					logger.logDebug("start // Rule 13.1..");
					if (null != collabItem.getAttributeValue("Product Catalog Global Spec/TargetAudience") 
							&& "NGA".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/TargetAudience").toString())){
						if (null == collabItem.getAttributeValue("Product Catalog Global Spec/PrivateLabel_CustomerName")){
							logger.logInfo("Item : "+itmPrimaryKey + " PrivateLabel_CustomerName is Null");
							arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/PrivateLabel_CustomerName"), Type.VALIDATION_RULE, "PrivateLabel_CustomerName was empty even though TargetAudience was NGA");
						}
					}
					 // Rule 13.1 End 
					
					/* // Rule 14.1 Start 
					logger.logDebug("start // Rule 14.1..");
					if (null != collabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US")){
						collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", collabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US"));
					}else{
						if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Description1")
								&& null != collabItem.getAttributeValue("Product Catalog Global Spec/Description2")){
									collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", new StringBuilder(collabItem.getAttributeValue("Product Catalog Global Spec/Description1").toString()).append(" ").append(collabItem.getAttributeValue("Product Catalog Global Spec/Description2").toString()));
						}else if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Description1")
										&& null == collabItem.getAttributeValue("Product Catalog Global Spec/Description2")){
									collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", collabItem.getAttributeValue("Product Catalog Global Spec/Description1").toString());
						}else if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Description2")
										&& null == collabItem.getAttributeValue("Product Catalog Global Spec/Description1")){
									collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", collabItem.getAttributeValue("Product Catalog Global Spec/Description2").toString());
						} 
					}
					 // Rule 14.1 End 
*/					
					 // Rule 15 Start 
					logger.logDebug("start // Rule 15..");
					List<String> special_character_validation_attributes = new ArrayList<String>();
					List<String> make_upper_case_attributes = new ArrayList<String>();
					List<String> make_lower_case_attributes = new ArrayList<String>();
					List<String> make_title_case_attributes = new ArrayList<String>();
					special_character_validation_attributes.add("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Description-Marketing");
					special_character_validation_attributes.add("Product Catalog Global Spec/DescriptionShort_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature1_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature2_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature3_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature4_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature5_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature6_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature7_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature8_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/Feature9_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/KeyWord1_en-US");
					special_character_validation_attributes.add("Product Catalog Global Spec/ModelNumber");
				    special_character_validation_attributes.add("Product Catalog Global Spec/Series");
					 
				    Iterator iterSpl = special_character_validation_attributes.iterator();
				    while(iterSpl.hasNext()){
				    	String attrPath = (String) iterSpl.next();
				    	if (!StringUtils.isEmpty((null != collabItem.getAttributeValue(attrPath))? collabItem.getAttributeValue(attrPath).toString() : "")){
				    		logger.logInfo("15 .1    -----");
				    		if(utilityMethods.specialChanracterCheck(collabItem.getAttributeValue(attrPath).toString())){
				    			arg0.addValidationError(collabItem.getAttributeInstance(attrPath), Type.VALIDATION_RULE, "Do not allow data to be saved if string contains special characters (@, ?, !, %, *, \", °, ™)");
				    		}
				    	}
				    }
				    logger.logInfo("15 .2    -----");
				    make_upper_case_attributes.add("Product Catalog Global Spec/FunctionallyEquivalentItem"); 	
				   // make_upper_case_attributes.add("Product Catalog Global Spec/ModelNumber");
				    make_upper_case_attributes.add("Product Catalog Global Spec/Part Number");
				   // make_upper_case_attributes.add("Product Catalog Global Spec/Series");
				    make_upper_case_attributes.add("Product Catalog Global Spec/CountryMarketingLimitedTo");
				    
				    for (String attrPath : make_upper_case_attributes){
				    	if (!StringUtils.isEmpty((null != collabItem.getAttributeValue(attrPath))? collabItem.getAttributeValue(attrPath).toString() : "")){
				    		collabItem.setAttributeValue(attrPath, StringUtils.upperCase(collabItem.getAttributeValue(attrPath).toString()));
				    	}
				    }
				    make_lower_case_attributes.add("Product Catalog Global Spec/KeyWord1_en-US");
				    for (String attrPath : make_lower_case_attributes){
				    	if (!StringUtils.isEmpty((null != collabItem.getAttributeValue(attrPath))? collabItem.getAttributeValue(attrPath).toString() : "")){
				    		collabItem.setAttributeValue(attrPath, StringUtils.lowerCase(collabItem.getAttributeValue(attrPath).toString()));
				    	}
				    }
				    
				    //make_title_case_attributes.add("Product Catalog Global Spec/DescriptionShort_en-US");
				    make_title_case_attributes.add("Product Catalog Global Spec/PrivateLabel_CustomerName");
				    for (String attrPath : make_title_case_attributes){
				    	if (!StringUtils.isEmpty((null != collabItem.getAttributeValue(attrPath))? collabItem.getAttributeValue(attrPath).toString() : "")){
				    		collabItem.setAttributeValue(attrPath, WordUtils.capitalizeFully(collabItem.getAttributeValue(attrPath).toString()));
				    	}
				    }
				     // Rule 15 End 
				    
				     // Rule 81.1 Start 
				    logger.logDebug("start // Rule 81.1..");
				    if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder","false");
				    }
				     // Rule 81.1 End 
				    
				     // Rule 81.3 Start 
				    logger.logDebug("start // Rule 81.3..");
				    if (null != collabItem
				    		&& null == collabItem.getAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnitName")
				    		&& (null != collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder") && "true".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString()))
				    		&& null != collabItem.getAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTime")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnitName", "Days");
				    }
				     // Rule 81.3 End 
				    
				     // Rule 82.1 Start 
				    logger.logDebug("start // Rule 82..");
	
					// Setting  SecurityTagType and SecurityTagLocation required if set to true
				    
				    
					if(null  == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsSecurityTagPresent"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsSecurityTagPresent","false");
					}
					else{
						 // Rule 82.2 Start 
						String securityTagPresent = collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsSecurityTagPresent").toString();
						if(securityTagPresent.equals("true"))
						{
							if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagType"))
							{
								AttributeInstance SecurityTagTypeInstance = collabItem.getAttributeInstance("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagType");
								arg0.addValidationError(SecurityTagTypeInstance, Type.VALIDATION_RULE, "EA:SecurityTagType was empty even though EA:IsSecurityTagPresent was true.");
							}
							if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagLocation"))
							{
								AttributeInstance SecurityTagLocInstance = collabItem.getAttributeInstance("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagLocation");
								arg0.addValidationError(SecurityTagLocInstance, Type.VALIDATION_RULE, "EA:SecurityTagLocation was empty even though EA:IsSecurityTagPresent was true.");
							}
						}
						else
						{
							collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagLocation",null);
							collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SecurityTagType",null);
						}
					}
					 // Rule 82.2 End 
				    
				    
				     
				    
					 
					
					 // Rule 132.1 Start 
					logger.logDebug("start // Rule 132..");
					// Going to populate IsTradeItemMarkedAsRecyclable attribute as false if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemMarkedAsRecyclable"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemMarkedAsRecyclable","false");
					}
					 // Rule 132.1 End 
					
					 // Rule 131.1 Start 
					logger.logDebug("start // Rule 131..");
					// Going to populate IsSecurityTagPresent attribute as false if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsSecurityTagPresent"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsSecurityTagPresent","false");
					}
					 // Rule 131.1 End 
					
					 // Rule 129.1 Start 
					logger.logDebug("start // Rule 129..");
					// Going to populate IsNetContentDeclarationIndicated attribute as false if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsNetContentDeclarationIndicated"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsNetContentDeclarationIndicated","false");
					}
					 // Rule 129.1 End 
					
					 // Rule 127.1 Start 
				/*logger.logDebug("start // Rule 127..");
				// Going to populate IsItemAvailableForDirectToConsumerDelivery attribute as false if it is empty
				if(null == collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForDirectToConsumerDelivery"))
				{
					collabItem.setAttributeValue("Product Catalog Global Spec/IsItemAvailableForDirectToConsumerDelivery","false");
				}*/
					 // Rule 127.1 End 
					
					 // Rule 125.1 Start 
					logger.logDebug("start // Rule 125..");
					// Going to populate IsWoodAComponentOfThisItem attribute as false if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/IsWoodAComponentOfThisItem"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/IsWoodAComponentOfThisItem","false");
					}
					 // Rule 125.1 End 
					
					 // Rule 120.1 Start 
					logger.logDebug("start // Rule 120..");
					// Going to populate SDSRequired attribute as N if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/SDSRequired"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/SDSRequired","N");
					}
					 // Rule 120.1 End 
					
					 // Rule 123.1 Start 
					logger.logDebug("start // Rule 123..");
					// Going to populate ShelfLife attribute as N if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/ShelfLife"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/ShelfLife","N");
					}
					 // Rule 123.1 End 
					
					 // Rule 116.1 Start 
					logger.logDebug("start // Rule 116..");
					// Going to populate IsPackagingMarkedReturnable attribute as 'false' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsPackagingMarkedReturnable"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsPackagingMarkedReturnable","false");
					}
					 // Rule 116.1 End 
					
					 // Rule 104.1 Start 
					logger.logDebug("start // Rule 104..");
	
					// Going to populate IsNonSoldTradeItemReturnable attribute as 'false' if it is empty
	
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/IsNonSoldTradeItemReturnable"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/IsNonSoldTradeItemReturnable","false");
					}
					 // Rule 104.1 End 
					
					 // Rule 105.1 Start 
					logger.logDebug("start // Rule 105..");
	
					// Going to populate OrderingLeadTimeUnit attribute as 'DA' if it is empty
	
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/OrderingLeadTimeUnit"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/OrderingLeadTimeUnit","DA");
					}
					 // Rule 105.1 End 
					
					 // Rule 106.1 Start 
					logger.logDebug("start // Rule 106..");
	
					// Going to populate IsTradeItemRecalled = 'false'  if it is empty
	
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/IsTradeItemRecalled"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/IsTradeItemRecalled","false");
					}
					 // Rule 106.1 End 
					
					
					 
					
					 // Rule 94.6 Start 
					// Going to set SpecialOrderQuantityLeadTimeUnit = LOOKUPTABLE Value : SpecialOrderQuantityLeadTimeUnitName KEY
					logger.logDebug("start // Rule 94.6..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnitName") )
					{
						String spclOrderQtyLeadTimeUnitName = collabItem.getAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnitName").toString();
						String spclOrderQtyLeadTimeUnit = utilityMethods.getLkpValueForKey(ctx,"UOM - GDSN Table",spclOrderQtyLeadTimeUnitName,logger);
						collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnit",spclOrderQtyLeadTimeUnit) ;
					}
					 // Rule 94.6 End 
					
					 // Rule 94.7 Start 
					// Going to set OrderableUnitIndicator = LOOKUPTABLE Value : OrderingUnitOfMeasureName KEY
					logger.logDebug("start // Rule 94.7..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/OrderingUnitOfMeasureName") )
					{
						String orderOfUnitName = collabItem.getAttributeValue("Product Catalog Global Spec/OrderingUnitOfMeasureName").toString();
						String orderUnitIndicator = utilityMethods.getLkpValueForKey(ctx,"UOM - GDSN Table",orderOfUnitName,logger);
						collabItem.setAttributeValue("Product Catalog Global Spec/OrderableUnitIndicator",orderUnitIndicator) ;
					}
					 // Rule 94.7 End 
					
					// Rule 143.1 Start 
					// Going to set Warrenty Attributes 
					logger.logDebug("start // Rule 143.1..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/WarrantyName") )
					{
						String warrantyName = collabItem.getAttributeValue("Product Catalog Global Spec/WarrantyName").toString();
						String[] lookupValues = utilityMethods.getLkpValuesForKey(ctx,"WPT_Warranty_Attrib_Lkp",warrantyName,logger);
						collabItem.setAttributeValue("Product Catalog Global Spec/WarrantyPeriod",lookupValues[0]) ;
						collabItem.setAttributeValue("Product Catalog Global Spec/WarrantyBlurb",lookupValues[1]) ;
						collabItem.setAttributeValue("Product Catalog Global Spec/WarrantyLink",lookupValues[2]) ;
					}
					 // Rule 143.1 End 
				}
				//Manual Resolution or PM End
				
				
				
				
				
				
				
				
				
				//MAR PDS step start
				if(null != collabStep && (collabStep.getName().equalsIgnoreCase("Marcom Product Data Services Maintenance"))){
					
					 // Rule 77.4 Start 
					/*logger.logDebug("start // Rule 77.4..");
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoURL_en-US"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Image1Usage",null);
					}*/
					 // Rule 77.4 End 
					
					 // Rule 77.6 Start 
					/*logger.logDebug("start // Rule 77.6..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Image1FileURL_en-US"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Image1Usage","PRODUCT_IMAGE");
					}
					else
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Image1Usage",null);
					}*/
					 // Rule 77.6 End 
					
					 // Rule 78.1 Start 
					/*logger.logDebug("start // Rule 78..");
	
					// Setting Image1EffectiveStartDate, Image1FileURL_en-US based on value change of ColorPhotoURL_en-US
	
					java.text.DateFormat f = new java.text.SimpleDateFormat ( "MM/dd/yyyy" );
					java.util.Calendar c = java.util.Calendar.getInstance();
					String current_date = f.format ( c.getTime() );
					boolean isColorPhotoAttribValChanged = utilityMethods.hasAttributeChanged(collabItem, "ColorPhotoURL_en-US",logger);
	
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoURL_en-US") && isColorPhotoAttribValChanged)
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Image1EffectiveStartDate",current_date) ;
					}
					else if(null  == collabItem.getAttributeValue("Product Catalog Global Spec/ColorPhotoURL_en-US") )
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Image1FileURL_en-US",null) ;
						collabItem.setAttributeValue("Product Catalog Global Spec/Image1EffectiveStartDate",null) ;
					}
					else if(null ==  collabItem.getAttributeValue("Product Catalog Global Spec/Image1FileURL_en-US"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Image1EffectiveStartDate",null) ;
					}*/
					 // Rule 78.1 End 
					
					
					
					
					 /*// Rule 80.1 Start 
					logger.logDebug("start // Rule 80..");
	
					// Setting  InstructionSheetUsage and InstructionSheetFileType based on certain business criterias
	
					if(null !=  collabItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileName") && null == collabItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetUsage"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetUsage","DOCUMENT");
					}
					 // Rule 80.1 End 
					
					 // Rule 80.3 Start 
					logger.logDebug("start // Rule 80.3..");
					if(null  ==  collabItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileName") )
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetUsage",null);
						collabItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetFileType",null);
					}
					 // Rule 80.3 End 
					*/
					 
					
					 // Rule 92.1 Start 
					logger.logDebug("start // Rule 92..");
	
					// Going to set TargetMarketCountryCode = LOOKUPTABLE Value : TargetMarketCountryCodeName KEY
	
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/TargetMarketCountryCodeName") )
					{
						String targetMarketCtryName = collabItem.getAttributeValue("Product Catalog Global Spec/TargetMarketCountryCodeName").toString();
						String targetMarketCtryCode = utilityMethods.getLkpValueForKey(ctx,"CountryOfOrigin-Primary",targetMarketCtryName,logger);
						collabItem.setAttributeValue("Product Catalog Global Spec/TargetMarketCountryCode",targetMarketCtryCode) ;
					}
					 // Rule 92.1 End 
					
					 // Rule 93.1 Start 
					logger.logDebug("start // Rule 93..");
	
					// Going to set TargetMarket2 = LOOKUPTABLE Value : TargetMarket2Name KEY
	
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/TargetMarket2Name") )
					{
						String targetMarket2Name = collabItem.getAttributeValue("Product Catalog Global Spec/TargetMarket2Name").toString();
						String targetMarket2 = utilityMethods.getLkpValueForKey(ctx,"CountryOfOrigin-Primary",targetMarket2Name,logger);
						collabItem.setAttributeValue("Product Catalog Global Spec/TargetMarket2",targetMarket2) ;
					}
					 // Rule 93.1 End 
					
					 // Rule 94.2 Start 
					logger.logDebug("start // Rule 94.2..");
					// Going to set SellingUOMName = 'Each' if it is empty
	
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/SellingUOMName") )
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/SellingUOMName","Each") ;
					}
					 // Rule 94.2 End 
					
					 // Rule 94.5 Start 
					// Going to set SellingUOM = LOOKUPTABLE Value : SellingUOMName KEY
					logger.logDebug("start // Rule 94.5..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/SellingUOMName") )
					{
						String sellingUOMName = collabItem.getAttributeValue("Product Catalog Global Spec/SellingUOMName").toString();
						String sellingUOM = utilityMethods.getLkpValueForKey(ctx,"UOM - GDSN Table",sellingUOMName,logger);
						collabItem.setAttributeValue("Product Catalog Global Spec/SellingUOM",sellingUOM) ;
					}
					 // Rule 94.5 End 
					
					 // Rule 117.1 Start 
					logger.logDebug("start // Rule 117..");
					// Going to populate informationProviderGLN attribute as '0042805000007' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/informationProviderGLN"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/informationProviderGLN","0042805000007");
					}
					 // Rule 117.1 End 
					
					 // Rule 118.1 Start 
					logger.logDebug("start // Rule 118..");
					// Going to populate SellingUOM attribute as EA if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/SellingUOM"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/SellingUOM","EA");
					}
					 // Rule 118.1 End 
					
					 // Rule 124.1 Start 
					logger.logDebug("start // Rule 124..");
					// Going to populate TargetMarketCountryCodeName attribute as UNITED STATES if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/TargetMarketCountryCodeName"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/TargetMarketCountryCodeName","UNITED STATES");
					}
					 // Rule 124.1 End 
					
					 // Rule 115.1 Start 
					logger.logDebug("start // Rule 115..");
					// Going to populate IsTradeItemAVariableUnit attribute as 'false' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAVariableUnit"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAVariableUnit","false");
					}
					 // Rule 115.1 End 
					
					 // Rule 114.1 Start 
					logger.logDebug("start // Rule 114..");
					// Going to populate AlternateItemIdentificationAgency attribute as 'MNO' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/AlternateItemIdentificationAgency"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/AlternateItemIdentificationAgency","MNO");
					}
					 // Rule 114.1 End 
					
					 // Rule 109.1 Start 
					logger.logDebug("start // Rule 109..");
					// Going to populate Kit-Ferguson attribute as 'N' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Kit-Ferguson"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Kit-Ferguson","N");
					}
					 // Rule 109.1 End 
					
					 // Rule 110.1 Start 
					logger.logDebug("start // Rule 110..");
					// Going to populate IsTradeItemInformationPrivate attribute as 'true' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/IsTradeItemInformationPrivate"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/IsTradeItemInformationPrivate","true");
					}
					 // Rule 110.1 End 
					
					 // Rule 107.1 Start 
					logger.logDebug("start // Rule 107..");
	
					// Going to populate BrandOwnerGLN = '0042805000007'  if it is empty
	
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/BrandOwnerGLN"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/BrandOwnerGLN","0042805000007");
					}
					 // Rule 107.1 End 
					
					// Rule 140.1 Start 
					logger.logDebug("Workflow : start // Rule 140.1..");
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsBarcodeSymbologyDerivable"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsBarcodeSymbologyDerivable","false");
					}
					logger.logDebug("Workflow : end // Rule 140.1..");
					 // Rule 140.1 End 
					logger.logDebug("start // Rule 107.1..");
					boolean IsTradeItemADespatchUnit = false;
					if (collabItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size()>0 && collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size()> 0){
						logger.logInfo("Item : "+itmPrimaryKey + " Higher Packaging Exists");
						Iterator<? extends AttributeInstance> packLvlIter = collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
						
						while(packLvlIter.hasNext()){
							AttributeInstance chldIns = packLvlIter.next();
							String higherPackLvlPath = chldIns.getPath();
							logger.logInfo("Item : "+itmPrimaryKey + "Path "+higherPackLvlPath);
							if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "INNER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
								
								if(null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.BAR_CODE_TYPE)){
									logger.logInfo("Item : "+itmPrimaryKey+" Inner Bar Code Type is empty. Setting  Bar Code Type ");
									collabItem.setAttributeValue(higherPackLvlPath+"/"+Constant.BAR_CODE_TYPE, "ITF_14");
								}
								
								 // Rule 83.1 Start 
								logger.logDebug("start // Rule 83.1..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemADespatchUnit")){
									collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemADespatchUnit", "true");
									
								}
								 // Rule 83.1 End 
								
								 // Rule 83.4 Start 
								logger.logDebug("start // Rule 83.4..");
								if(collabItem != null && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit")){
									collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit", false);
									IsTradeItemADespatchUnit = true;
								}
								 // Rule 83.4 End 
								
								 // Rule 76.2 Start 
								logger.logDebug("start // Rule 76.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Volume")){
							    	collabItem.setAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Volume","Cubic inch");
							    }
								 // Rule 76.2 End 
								
								 // Rule 74.2 Start 
								logger.logDebug("start // Rule 74.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Weight")){
							    	collabItem.setAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Weight","Pound");
							    }
								 // Rule 74.2 End 
								
								 // Rule 73.2 Start 
								logger.logDebug("start // Rule 73.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/UnitOfMeasureImperial-Dimension")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/UnitOfMeasureImperial-Dimension","Inch");
							    }
								 // Rule 73.2 End 
								
								 // Rule 69.2 Start 
								logger.logDebug("start // Rule 69.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/ProductTypeCode")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/ProductTypeCode","PK");
							    }
								 // Rule 69.2 End 
								
								 // Rule 68.2 Start 
								logger.logDebug("start // Rule 68.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAConsumerUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAConsumerUnit","false");
							    }
								 // Rule 68.2 End 
								
								 // Rule 67.2 Start 
								logger.logDebug("start // Rule 67.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAnOrderableUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAnOrderableUnit","false");
							    }
								 // Rule 67.2 End 
								
								 // Rule 65.2 Start 
								logger.logDebug("start // Rule 65.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemABaseUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemABaseUnit","false");
							    }
								 // Rule 65.2 End 
								
								 // Rule 64.2 Start 
								logger.logDebug("start // Rule 64.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAnInvoiceUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAnInvoiceUnit","false");
							    }
								 // Rule 64.2 End 
								
							}
							if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "MASTER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
								
								if(null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.BAR_CODE_TYPE)){
									logger.logInfo("Item : "+itmPrimaryKey+" Master Bar Code Type is empty. Setting  Bar Code Type ");
									collabItem.setAttributeValue(higherPackLvlPath+"/"+Constant.BAR_CODE_TYPE, "ITF_14");
								}
								
								 // Rule 83.2 Start 
								logger.logDebug("start // Rule 83.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemADespatchUnit")){
									collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemADespatchUnit", "true");
									
								}
								 // Rule 83.2 End 
								
								 // Rule 83.4 Start 
								logger.logDebug("start // Rule 83.4..");
								if(collabItem != null && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit")){
									collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit", false);
									IsTradeItemADespatchUnit = true;
								}
								 // Rule 83.4 End 
								
								 // Rule 76.3 Start 
								logger.logDebug("start // Rule 76.2..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Volume")){
							    	collabItem.setAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Volume","Cubic inch");
							    }
								 // Rule 76.3 End 
								
								 // Rule 74.3 Start 
								logger.logDebug("start // Rule 74.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Weight")){
							    	collabItem.setAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Weight","Pound");
							    }
								 // Rule 74.3 End 
								
								 // Rule 73.3 Start 
								logger.logDebug("start // Rule 73.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/UnitOfMeasureImperial-Dimension")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/UnitOfMeasureImperial-Dimension","Inch");
							    }
								 // Rule 73.3 End 
								
								 // Rule 69.3 Start 
								logger.logDebug("start // Rule 69.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/ProductTypeCode")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/ProductTypeCode","CA");
							    }
								 // Rule 69.3 End 
								
								 // Rule 68.3 Start 
								logger.logDebug("start // Rule 68.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAConsumerUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAConsumerUnit","false");
							    }
								 // Rule 68.3 End 
								
								 // Rule 67.3 Start 
								logger.logDebug("start // Rule 67.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAnOrderableUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAnOrderableUnit","false");
							    }
								 // Rule 67.3 End 
								
								 // Rule 65.3 Start 
								logger.logDebug("start // Rule 65.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemABaseUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemABaseUnit","false");
							    }
								
								 // Rule 65.3 End 
								
								 // Rule 64.3 Start 
								logger.logDebug("start // Rule 64.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAnInvoiceUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAnInvoiceUnit","false");
							    }
								 // Rule 64.3 End 
							}
							if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "PALLET".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
								
								if(null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.BAR_CODE_TYPE)){
									logger.logInfo("Item : "+itmPrimaryKey+" Pallet Bar Code Type is empty. Setting  Bar Code Type ");
									collabItem.setAttributeValue(higherPackLvlPath+"/"+Constant.BAR_CODE_TYPE, "ITF_14");
								}
								
								 // Rule 83.3 Start 
								logger.logDebug("start // Rule 83.3..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemADespatchUnit")){
									collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemADespatchUnit", "true");
									
								}
								 // Rule 83.3 End 
								
								 // Rule 83.4 Start 
								logger.logDebug("start // Rule 83.4..");
								if(collabItem != null && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit")){
									collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit", false);
									IsTradeItemADespatchUnit = true;
								}
								 // Rule 83.4 End 
								
								 // Rule 76.4 Start 
								logger.logDebug("start // Rule 76.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Volume")){
							    	collabItem.setAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Volume","Cubic inch");
							    }
								 // Rule 76.4 End 
								
								 // Rule 74.4 Start 
								logger.logDebug("start // Rule 74.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Weight")){
							    	collabItem.setAttributeValue(higherPackLvlPath + "/UnitOfMeasureImperial-Weight","Pound");
							    }
								 // Rule 74.4 End 
								
								 // Rule 73.4 Start 
								logger.logDebug("start // Rule 73.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/UnitOfMeasureImperial-Dimension")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/UnitOfMeasureImperial-Dimension","Inch");
							    }
								 // Rule 73.4 End 
								
								 // Rule 69.4 Start 
								logger.logDebug("start // Rule 69.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/ProductTypeCode")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/ProductTypeCode","PL");
							    }
								 // Rule 69.4 End 
								
								 // Rule 68.4 Start 
								logger.logDebug("start // Rule 68.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAConsumerUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAConsumerUnit","false");
							    }
								 // Rule 68.4 End 
								
								 // Rule 67.4 Start 
								logger.logDebug("start // Rule 67.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAnOrderableUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAnOrderableUnit","false");
							    }
								 // Rule 67.4 End 
								
								 // Rule 65.4 Start 
								logger.logDebug("start // Rule 65.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemABaseUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemABaseUnit","false");
							    }
								 // Rule 65.4 End 
								
								 // Rule 64.4 Start 
								logger.logDebug("start // Rule 64.4..");
								if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/IsTradeItemAnInvoiceUnit")){
							    	collabItem.setAttributeValue(higherPackLvlPath+"/IsTradeItemAnInvoiceUnit","false");
							    }
								 // Rule 64.4 End 
							}
						}
					}
					
					if(collabItem != null && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit") && !IsTradeItemADespatchUnit){
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemADespatchUnit", true);
					}
					
					 // Rule 76.1 Start 
					logger.logDebug("start // Rule 76.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/UnitOfMeasureImperial-Volume")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/UnitOfMeasureImperial-Volume","Cubic inch");
				    }
					 // Rule 76.1 End 
					
					 // Rule 75.1 Start 
					logger.logDebug("start // Rule 75.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingMaterialCompositionQuantityUnit")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingMaterialCompositionQuantityUnit","Each");
				    }
					 // Rule 75.1 End 
					
					 // Rule 74.1 Start 
					logger.logDebug("start // Rule 74.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/UnitOfMeasureImperial-Weight")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/UnitOfMeasureImperial-Weight","Pound");
				    }
					 // Rule 74.1 End 
					
					 // Rule 73.1 Start 
					logger.logDebug("start // Rule 73.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/UnitOfMeasureImperial-Dimension")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/UnitOfMeasureImperial-Dimension","Inch");
				    }
					 // Rule 73.1 End 
					
					 // Rule 69.1 Start 
					logger.logDebug("start // Rule 69.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/ProductTypeCode")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/ProductTypeCode","EA");
				    }
					 // Rule 69.1 End 
					
					 // Rule 68.1 Start 
					logger.logDebug("start // Rule 68.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAConsumerUnit")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAConsumerUnit","true");
				    }
					 // Rule 68.1 End 
					
					 // Rule 67.1 Start 
					logger.logDebug("start // Rule 67.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAnOrderableUnit")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAnOrderableUnit","true");
				    }
					 // Rule 67.1 End 
					
					 // Rule 65.1 Start 
					logger.logDebug("start // Rule 65.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemABaseUnit")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemABaseUnit","true");
				    }
					 // Rule 65.1 End 
					
					 // Rule 64.1 Start 
					logger.logDebug("start // Rule 64.1..");
					if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAnInvoiceUnit")){
				    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsTradeItemAnInvoiceUnit","true");
				    }
					 // Rule 64.1 End 
					
					 
					
					 // Rule 137.1 Start 
					logger.logDebug("start // Rule 137..");
					// Going to populate ObsoleteStockAvailable-Ferguson attribute with value 'No' if ItemStatus='RESTRICT'
	
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/ItemStatus") &&  collabItem.getAttributeValue("Product Catalog Global Spec/ItemStatus").toString().equals("RESTRICT"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/ObsoleteStockAvailable-Ferguson", "No");
					}
					 // Rule 137.1 End 
					
					 // Rule 130.1 Start 
					logger.logDebug("start // Rule 130..");
					// Going to populate IsPackagingMarkedWithIngredients attribute as false if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsPackagingMarkedWithIngredients"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/IsPackagingMarkedWithIngredients","false");
					}
					 // Rule 130.1 End 
					
					 // Rule 103.1 Start 
					logger.logDebug("start // Rule 103..");
					// Going to populate PackagingMaterialCompositionQuantity attribute as '1' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingMaterialCompositionQuantity"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingMaterialCompositionQuantity",1);
					}
					 // Rule 103.1 End 
					
					
					
				}
				////MAR PDS step END
				
				
				
				
				
				
				
				// Engg Maintenance Step Start
				if(null != collabStep && (collabStep.getName().equalsIgnoreCase("Engineering Maintenance"))){
					
					
					 // Rule 91.1 Start 
					logger.logDebug("start // Rule 91..");
	
					// If STEP = Engineering Maintenance and LeadFree is = 'YES' and ThirdPartyCertified is null then ERROR = 'ThirdPartyCertified should be populated if LeadFree is YES'
	
					if(null !=  collabItem.getAttributeValue("Product Catalog Global Spec/LeadFree") && collabItem.getAttributeValue("Product Catalog Global Spec/LeadFree").toString().equals("YES") && null == collabItem.getAttributeValue("Product Catalog Global Spec/ThirdPartyCertified"))
	
					{
						AttributeInstance thirdPartyCertifiedInstance = collabItem.getAttributeInstance("Product Catalog Global Spec/ThirdPartyCertified");
						arg0.addValidationError(thirdPartyCertifiedInstance, Type.VALIDATION_RULE, "ThirdPartyCertified should be populated if LeadFree is YES.");
					}
					 // Rule 91.1 End 
					
					 // Rule 98.1 Start 
					logger.logDebug("start // Rule 98.1..");
					if (null != collabItem && null != collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingTypeDescription")){
						collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackagingTypeDescription").toString(), logger));
					}
					 // Rule 98.1 End 
					
					
					if (collabItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size()>0 && collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size()> 0){
						logger.logInfo("Item : "+itmPrimaryKey + " Higher Packaging Exists");
						Iterator<? extends AttributeInstance> packLvlIter = collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
						while(packLvlIter.hasNext()){
							AttributeInstance chldIns = packLvlIter.next();
							String higherPackLvlPath = chldIns.getPath();
							logger.logInfo("Item : "+itmPrimaryKey + "Path "+higherPackLvlPath);
							if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "INNER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
								
								// // Rule 98.2 Start 
								logger.logDebug("start // Rule 98.2..");
								if (null != collabItem && null != collabItem.getAttributeValue(higherPackLvlPath + "/PackagingTypeDescription")){
									collabItem.setAttributeValue(higherPackLvlPath + "/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", collabItem.getAttributeValue(higherPackLvlPath+ "/PackagingTypeDescription").toString(), logger));
								}
								// // Rule 98.2 End 
							}
							if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "MASTER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
								
								 // Rule 98.3 Start 
								logger.logDebug("start // Rule 98.3..");
								if (null != collabItem && null != collabItem.getAttributeValue(higherPackLvlPath + "/PackagingTypeDescription")){
									collabItem.setAttributeValue(higherPackLvlPath + "/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", collabItem.getAttributeValue(higherPackLvlPath+ "/PackagingTypeDescription").toString(), logger));
								}
								 // Rule 98.3 End 
							}
							if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "PALLET".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
								
								 // Rule 98.4 Start 
								logger.logDebug("start // Rule 98.4..");
								if (null != collabItem && null != collabItem.getAttributeValue(higherPackLvlPath + "/PackagingTypeDescription")){
									collabItem.setAttributeValue(higherPackLvlPath + "/PackagingTypeCode",utilityMethods.getLkpValueForKey(ctx, "PackagingTypeDescription - GDSN Table", collabItem.getAttributeValue(higherPackLvlPath+ "/PackagingTypeDescription").toString(), logger));
								}
								 // Rule 98.4 End 
								
							}
						}
					}
					
					
					
					 // Rule 133.1 Start 
					logger.logDebug("start // Rule 133..");
					// Going to populate ADACompliant attribute with value ='N' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ADACompliant"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/ADACompliant","N");
					}
					 // Rule 133.1 End 
					
					 // Rule 128.1 Start 
					logger.logDebug("start // Rule 128..");
					// Going to populate IsItemSubjectToUSPatent attribute as false if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/IsItemSubjectToUSPatent"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/IsItemSubjectToUSPatent","false");
					}
					 // Rule 128.1 End 
					
					 // Rule 121.1 Start 
					logger.logDebug("start // Rule 121..");
					// Going to populate WQACertified attribute as No if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/WQACertified"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/WQACertified","No");
					}
					 // Rule 121.1 End 
					
					 // Rule 111.1 Start 
					logger.logDebug("start // Rule 111..");
					// Going to populate ORMD-Ferguson attribute as 'N' if it is empty
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ORMD-Ferguson"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/ORMD-Ferguson","N");
					}
					 // Rule 111.1 End 
					
					// Rule 142.1 Start 
					logger.logDebug("start // Rule 142.1..");
					if(null == collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-InScope"))
					{
						collabItem.setAttributeValue("Product Catalog Global Spec/CAProp65-InScope","Yes");
					}
					// Rule 142.1 End
					
					// Rule 142.2 Start 
					logger.logDebug("start // Rule 142.2..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-InScope") 
							&& collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-InScope").equals("No") 
							&& (null == collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-OutOfScopeReason")))
					{
						arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/CAProp65-OutOfScopeReason"), Type.VALIDATION_RULE, "CAProp65-InScope value is No, please fill CAProp65-OutOfScopeReason");
					}
					// Rule 142.2 End
					
					// Rule 142.3 Start 
					logger.logDebug("start // Rule 142.3..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-InScope") 
							&& collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-InScope").equals("Yes") 
							&& null == collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-CancerRiskSubstance")
								&&	 null == collabItem.getAttributeValue("Product Catalog Global Spec/CAProp65-DevelopRiskSubstance"))
					{
						arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/CAProp65-CancerRiskSubstance"), Type.VALIDATION_RULE, "CAProp65-InScope value is Yes, either CAProp65-CancerRiskSubstance or CAProp65-DevelopRiskSubstance is required");
						arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/CAProp65-DevelopRiskSubstance"), Type.VALIDATION_RULE, "CAProp65-InScope value is Yes, either CAProp65-CancerRiskSubstance or CAProp65-DevelopRiskSubstance is required");
					}
					// Rule 142.3 End
					
					// Rule 144 Start 
					logger.logDebug("start // Rule 144..");
					if(null != collabItem.getAttributeValue("Product Catalog Global Spec/CaliforniaEnergyCommissionListed") 
							&& collabItem.getAttributeValue("Product Catalog Global Spec/CaliforniaEnergyCommissionListed").equals("Compliant") 
							&& (null == collabItem.getAttributeValue("Product Catalog Global Spec/CaliforniaEnergyCommissionCertifyingAgency")))
					{
						arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/CaliforniaEnergyCommissionCertifyingAgency"), Type.VALIDATION_RULE, "If CaliforniaEnergyCommissionListed is set to Compliant, CaliforniaEnergyCommissionCertifyingAgency has to be populated.");
					}
					// Rule 144 End
				}
				
				// End Engg Step
				
				
				
				
				 // Rule 81.2 Start 
				logger.logDebug("start // Rule 81.2..");
				if (null != collabStep && (collabStep.getName().equalsIgnoreCase("Product Management Maintenance")
						|| collabStep.getName().equalsIgnoreCase("Complete")
						|| collabStep.getName().equalsIgnoreCase("Manual Resolution"))){
					
					if (null != collabItem && null != collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
							&& "true".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString())){
						if(null == collabItem.getAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTime")){
							arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/SpecialOrderQuantityLeadTime"), Type.VALIDATION_RULE, "SpecialOrderQuantityLeadTime was empty even though IsItemAvailableForSpecialOrder was true");
						}
						if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMinimum")){
							arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMinimum"), Type.VALIDATION_RULE, "packaging_level : EACH SpecialOrderQuantityMinimum was empty even though IsItemAvailableForSpecialOrder was true");
						}
						if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMultiple")){
							arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMultiple"), Type.VALIDATION_RULE, "packaging_level : EACH SpecialOrderQuantityMultiple was empty even though IsItemAvailableForSpecialOrder was true");
						}
						
						
					}else if(null != collabItem && (null == collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
							|| "false".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString()))){
						collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTime", null);
						collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnit", null);
						if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMinimum")){
							collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMinimum", null);
						}
						if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMultiple")){
							collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/SpecialOrderQuantityMultiple", null);
						}
					}
				}
				 // Rule 81.2 End 
				
				 // Rule 70.1 Start 
				logger.logDebug("start // Rule 70..");
	
				// Validating Templates Hierarchy Mapping based on given steps
	
				if(stepName.equals("Combine")  || stepName.equals("Product Management Maintenance")  || stepName.equals("Marcom Product Data Services Maintenance") || stepName.equals("Product Maintenance") )
				{
					Collection<Category> catList = collabItem.getCategories();
					int templatesCatCount = 0;
					AttributeInstance itemIDInstance = collabItem.getAttributeInstance("Product Catalog Global Spec/Part Number");
	
					for(Category objCat : catList)
					{
						if(objCat.getHierarchy().getName().equals("Templates Hierarchy"))
						{
							templatesCatCount++;
							if(templatesCatCount > 1)
							{
								arg0.addValidationError(itemIDInstance, Type.VALIDATION_RULE, "A product can not be mapped to more than one template.");
								break;
							}
						}
					}
					//if(stepName.equals("Product Maintenance")  && templatesCatCount == 0)
					if((stepName.equals("Product Management Maintenance") || stepName.equals("Marcom Product Data Services Maintenance") ) && templatesCatCount == 0)
					{
						arg0.addValidationError(itemIDInstance, Type.VALIDATION_RULE, "A product has to be mapped to a template.");
					}
				}
				logger.logDebug("Rule 70.1 End ");
				 // Rule 70.1 End 
				logger.logInfo("---------->>>>>Before new Code");
				if (null != collabStep && collabStep.getName().equalsIgnoreCase("Send PIM Complete To QAD"))
				{
				    logger.logInfo("Inside null != collabStep && collabStep.getName().equalsIgnoreCase(Send PIM Complete To QAD)");	
					collabItem.setAttributeValue("Product Catalog Global Spec/PIMComplete", "Yes");
				    logger.logDebug("Setting PIM Complete Rule");
				}
				
				if (null != collabStep && collabStep.getName().equalsIgnoreCase("Product Maintenance") )
				{
					logger.logInfo("Inside null != collabStep && collabStep.getName().equalsIgnoreCase(Send PIM Complete To QAD)");	
					collabItem.setAttributeValue("Product Catalog Global Spec/Is_Checked_To_First_Step", true);
					logger.logDebug("Setting Is_Checked_To_First_Step Rule");
				}
				
				if (null != collabStep && collabStep.getName().equalsIgnoreCase("Complete") )
				{
					logger.logInfo("Inside null != collabStep && collabStep.getName().equalsIgnoreCase(Send PIM Complete To QAD)");	
					collabItem.setAttributeValue("Product Catalog Global Spec/Is_Checked_To_First_Step", false);
				    logger.logDebug("Setting Is_Checked_To_First_Step for Complete Rule");
				}
				
				
				logger.logDebug("Post Processing Completed Successfully ");
			}
			//Start
			if (null == partNumber){
				logger.logInfo("Part Number is blank. Hence Created either by Add or Clone Button");
				arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/Part Number"), Type.VALIDATION_RULE, "Product Creation by Add or Clone button is not allowed.");
			}
			
			//System Rules Start
			
			
			 // Rule 62.1 Start 
			logger.logDebug("start // Rule 62..");

			// Setting AlternateItemIdentificationId with ModelNumber

			if(null !=  collabItem.getAttributeValue("Product Catalog Global Spec/ModelNumber"))
			{
				String modelNo = (String)collabItem.getAttributeValue("Product Catalog Global Spec/ModelNumber");
				collabItem.setAttributeValue("Product Catalog Global Spec/AlternateItemIdentificationId",modelNo);
			}
			 // Rule 62.1 End 
			
			 // Rule 80.2 Start 
			/*logger.logDebug("start // Rule 80.2..");
			if(null !=  collabItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileName") && null == collabItem.getAttributeValue("Product Catalog Global Spec/InstructionSheetFileType"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/InstructionSheetFileType","PDF");
			}*/
			 // Rule 80.2 End 
			
			 // Rule 84.1 Start 
			logger.logDebug("start // Rule 84..");

			// Going to set LF-NSF-ANSI-14_CertFileName = null If LF-NSF-ANSI-14_CertURL = null

			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-14_CertURL"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-14_CertFileName",null) ;
			}
			 // Rule 84.1 End 
			
			 // Rule 84.2 Start 
			// Going to set LF-NSF-ANSI-372_CertFileName = null If LF-NSF-ANSI-372_CertURL= null
			logger.logDebug("start // Rule 84.2..");
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-372_CertURL"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-372_CertFileName",null) ;
			}
			 // Rule 84.2 End 
			
			 // Rule 84.3 Start 
			// Going to set LF-NSF-ANSI-61-G_CertFileName = null If LF-NSF-ANSI-61-G_CertURL= null
			logger.logDebug("start // Rule 84.3..");
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-61-G_CertURL"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-ANSI-61-G_CertFileName",null) ;
			}
			 // Rule 84.3 End 
			
			 // Rule 84.4 Start 
			// Going to set LF-NSF-LowLeadCertProgram_CertFileName = null If LF-NSF-LowLeadCertProgram_CertURL= null
			logger.logDebug("start // Rule 84.4..");
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/LF-NSF-LowLeadCertProgram_CertURL"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/LF-NSF-LowLeadCertProgram_CertFileName",null) ;
			}
			 // Rule 84.4 End 
			
			 // Rule 84.5 Start 
			logger.logDebug("start // Rule 84.5..");
			// Going to set LF-WQA-NSFANSI-EquivalentStandard_CertFileName = null If LF-WQA-NSFANSI-EquivalentStandard_CertURL= null

			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/LF-WQA-NSFANSI-EquivalentStandard_CertURL"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/LF-WQA-NSFANSI-EquivalentStandard_CertFileName",null) ;
			}
			 // Rule 84.5 End 
			
			
			 
			
			 // Rule 89.1 Start 
			// IF ItemStatus= 'RESTRICT' then RestrictDate='Set Current Time'
			logger.logInfo("--------------------Test Start------------------");
			attributeChanges =  collabItem.getAttributeChangesSinceLastSave();
			logger.logInfo("--------------------Test Start1------------------");
			if (null != attributeChanges){
				logger.logInfo("--------------------Test Start2------------------");
				modifiedAttributesWithnewDataList=new ArrayList<AttributeInstance>();
				List<String> mustEditAttrLst = new ArrayList<String>();
				mustEditAttrLst.add("Product Catalog Global Spec/ItemStatus");
				modifiedAttributesWithnewDataList = attributeChanges.getModifiedAttributesWithNewData();
				logger.logInfo("--------------------Test Start3------------------");
				List<? extends AttributeInstance> modifiedAttributesWitholdDataList = attributeChanges.getModifiedAttributesWithOldData();
				logger.logInfo("--------------------Test Start4------------------");
				if (!modifiedAttributesWithnewDataList.isEmpty()){
					logger.logInfo("Item : "+itmPrimaryKey + " has got Modified attributessss");
					AttributeInstance AttrInsOldVal = null;
					AttributeInstance AttrInsNewVal = null;
					for (int i=0; i< modifiedAttributesWithnewDataList.size();i++){
						logger.logInfo("Inside 1");
						String attrPath = modifiedAttributesWithnewDataList.get(i).getPath().substring(1);
						logger.logInfo(attrPath);
						if (mustEditAttrLst.contains(attrPath)){
							logger.logInfo("Match111");
							AttrInsNewVal = modifiedAttributesWithnewDataList.get(i);
						}
					}
					for (int i=0; i< modifiedAttributesWitholdDataList.size();i++){
						logger.logInfo("Inside 2");
						String attrPath = modifiedAttributesWitholdDataList.get(i).getPath().substring(1);
						
						logger.logInfo(attrPath);
						if (mustEditAttrLst.contains(attrPath)){
							logger.logInfo("Match222");
							AttrInsOldVal = modifiedAttributesWitholdDataList.get(i);
						}
					}
					logger.logInfo("AttrInsNewVal.getValue().toString()");
					//logger.logInfo("AttrInsNewVal.getValue().toString()" + AttrInsNewVal.getValue().toString());
					//logger.logInfo("AttrInsOldVal.getValue().toString()"+AttrInsOldVal.getValue().toString());
					if (null != AttrInsNewVal && (null != AttrInsNewVal.getValue() && AttrInsNewVal.getValue().toString().equalsIgnoreCase("RESTRICT"))){
						logger.logInfo("Item : "+itmPrimaryKey + " has ItemStatus Changed to Restrict");
						boolean setRestrictDate = false;
						if (null == AttrInsOldVal ){
							setRestrictDate = true;
						}else if (null == AttrInsOldVal.getValue()){
							setRestrictDate = true;
						}else if (null != AttrInsOldVal.getValue() && !AttrInsOldVal.getValue().toString().equals("RESTRICT")){
							setRestrictDate = true;
						}
						logger.logInfo("Item : "+itmPrimaryKey + " setRestrictDate : "+ setRestrictDate);
						if (setRestrictDate){
							java.text.DateFormat f = new java.text.SimpleDateFormat ( "MM/dd/yyyy" );
							java.util.Calendar c = java.util.Calendar.getInstance();
							String dateVal = f.format ( c.getTime() );
							logger.logInfo("Item : "+itmPrimaryKey + " setting RestrictDate : " + dateVal);
							collabItem.setAttributeValue("Product Catalog Global Spec/RestrictDate",dateVal);
							
						}
					}
				}
			}
			
			 // Rule 89.1 End 
			
			 // Rule 90.1 Start 
			/*logger.logDebug("start // Rule 90..");

			// Going to set SpecificationSheetUsage = 'DOCUMENT' If it is null and SpecificationSheetFileName is not null

			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetUsage")  && null != collabItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetFileName"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/SpecificationSheetUsage","DOCUMENT") ;
			}
			 // Rule 90.1 End 
			
			 // Rule 90.2 Start 
			// Going to set SpecificationSheetFileType = 'PDF' If it is null and SpecificationSheetFileName is not null
			logger.logDebug("start // Rule 90.2..");
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetFileType")  && null != collabItem.getAttributeValue("Product Catalog Global Spec/SpecificationSheetFileName"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/SpecificationSheetFileType","PDF") ;
			}*/
			 // Rule 90.2 End 
			
			 // Rule 99.1 Start 
			logger.logDebug("start // Rule 99..");

			// Going to populate UniqueProductQualifier  by concatenating Part Number and SystemOfRecord

			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/UniqueProductQualifier") && null != partNumber &&  null != sysOfRec )
			{
				String uniqueProdQualifier = partNumber+" "+sysOfRec;
				collabItem.setAttributeValue("Product Catalog Global Spec/UniqueProductQualifier",uniqueProdQualifier);
			}
			 // Rule 99.1 End 
			
			 
			
			 
			
			
			 // Rule 4.16 Start 
			logger.logDebug("start // Rule 4.16..");
			if (null != collabItem.getAttributeValue("Product Catalog Global Spec/Part Number") && null != collabItem.getAttributeValue("Product Catalog Global Spec/PIM_ID")){
				if (collabItem.getAttributeValue("Product Catalog Global Spec/Part Number").toString().equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/PIM_ID").toString())){
					logger.logInfo("Item : "+itmPrimaryKey +"Part Number and PIM ID is same");
					arg0.addValidationError(collabItem.getAttributeInstance("Product Catalog Global Spec/Part Number"), Type.VALIDATION_RULE, "Part Number can not be equal to PIM ID");
				}
			}
			 // Rule 4.16 End 
			
			if (null == collabItem.getAttributeValue("Product Catalog Global Spec/PIMComplete")){
		    	collabItem.setAttributeValue("Product Catalog Global Spec/PIMComplete", "No");
		    }
			
			if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/BarCodeType")){
		    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/BarCodeType","UPC_A");
		    }
			
			 // Rule 66.1 Start 
			logger.logDebug("start // Rule 66.1..");
			if (null != collabItem && null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/EANUCCType")){
		    	collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/EANUCCType","GTIN_12");
		    }
			 // Rule 66.1 End 
			
			 // Rule 126.1 Start 
			logger.logDebug("start // Rule 126..");
			// Going to populate HasBatchNumber attribute as false if it is empty
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/HasBatchNumber"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/HasBatchNumber","false");
			}
			 // Rule 126.1 End 
			
			 // Rule 122.1 Start 
			logger.logDebug("start // Rule 122..");
			// Going to populate ListPriceUnitOfMeasure attribute as EACH CONSUMER UNIT if it is empty
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ListPriceUnitOfMeasure"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/ListPriceUnitOfMeasure","EACH CONSUMER UNIT");
			}
			 // Rule 122.1 End 
			
			 // Rule 113.1 Start 
			logger.logDebug("start // Rule 113..");
			// Going to populate Terms attribute as '2%10TH PROX/N30' if it is empty

			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/Terms"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/Terms","2%10TH PROX/N30");
			}
			 // Rule 113.1 End 
			
			 // Rule 122.1 Start 
			logger.logDebug("start // Rule 112..");
			// Going to populate ManufacturerGLN attribute as '0042805000007'' if it is empty

			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ManufacturerGLN"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/ManufacturerGLN","0042805000007");
			}
			 // Rule 122.1 End 
			
			 // Rule 108.1 Start 
			logger.logDebug("start // Rule 108..");
			// Going to populate ManufacturerName = 'Watts'  if it is empty
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ManufacturerName"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/ManufacturerName","Watts");
			}
			 // Rule 108.1 End 
			
			
			if (collabItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size()>0 && collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size()> 0){
				logger.logInfo("Item : "+itmPrimaryKey + " Higher Packaging Exists");
				Iterator<? extends AttributeInstance> packLvlIter = collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
				while(packLvlIter.hasNext()){
					AttributeInstance chldIns = packLvlIter.next();
					String higherPackLvlPath = chldIns.getPath();
					logger.logInfo("Item : "+itmPrimaryKey + "Path "+higherPackLvlPath);
					if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "INNER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
						
						 /*// Rule 4.6 Start 
						logger.logDebug("start // Rule 4.6..");
						if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
							logger.logInfo("Item : "+itmPrimaryKey+" Inner Package GTIN is Not empty. Calculating  Inner_PackageGTIN_Unique value ");
							collabItem.setAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique", collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
						}
						 // Rule 4.6 End 
						
						 // Rule 4.7 Start 
						logger.logDebug("start // Rule 4.7..");
						if (null != collabItem.getAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique")){
							if (null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
								logger.logInfo("Item : "+itmPrimaryKey+" Inner Package GTIN is empty. Setting  Inner_PackageGTIN_Unique to Null");
								collabItem.setAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique", null);
							}
						}
						 // Rule 4.7 End 
*/							
						 // Rule 75.2 Start 
						logger.logDebug("start // Rule 75.2..");
						if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/PackagingMaterialCompositionQuantityUnit")){
					    	collabItem.setAttributeValue(higherPackLvlPath + "/PackagingMaterialCompositionQuantityUnit","Each");
					    }
						 // Rule 75.2 End 
						
						 // Rule 66.2 Start 
						logger.logDebug("start // Rule 66.2..");
						if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/EANUCCType")){
					    	collabItem.setAttributeValue(higherPackLvlPath+"/EANUCCType","GTIN_14");
					    }
						 // Rule 66.2 End 
						
						
						
						 // Rule 81.2 Start 
						logger.logDebug("start // Rule 81.2..");
						if (null != collabStep && (collabStep.getName().equalsIgnoreCase("Product Management Maintenance")
								|| collabStep.getName().equalsIgnoreCase("Complete")
								|| collabStep.getName().equalsIgnoreCase("Manual Resolution"))){
							
							if (null != collabItem && null != collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
									&& "true".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString())){
								
								if(null == collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum")){
									arg0.addValidationError(collabItem.getAttributeInstance(higherPackLvlPath + "/SpecialOrderQuantityMinimum"), Type.VALIDATION_RULE, "packaging_level : INNER SpecialOrderQuantityMinimum was empty even though IsItemAvailableForSpecialOrder was true");
								}
								if(null == collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple")){
									arg0.addValidationError(collabItem.getAttributeInstance(higherPackLvlPath + "/SpecialOrderQuantityMultiple"), Type.VALIDATION_RULE, "packaging_level : INNER SpecialOrderQuantityMultiple was empty even though IsItemAvailableForSpecialOrder was true");
								}
								
								
							}else if(null != collabItem && (null == collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
									|| "false".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString()))){
								collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTime", null);
								collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnit", null);
								if(null != collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum")){
									collabItem.setAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum", null);
								}
								if(null != collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple")){
									collabItem.setAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple", null);
								}
							}
						}
						 // Rule 81.2 End 
						
						
						
					}
					if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "MASTER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
						
						/* // Rule 4.8 Start 
						logger.logDebug("start // Rule 4.8..");
						if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
							logger.logInfo("Item : "+itmPrimaryKey+" MASTER-02 Package GTIN is Not empty. Calculating  Master_PackageGTIN_Unique value ");
							collabItem.setAttributeValue("Product Catalog Global Spec/Master_PackageGTIN_Unique", collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
						}
						 // Rule 4.8 End 
						
						 // Rule 4.9 Start 
						logger.logDebug("start // Rule 4.9..");
						if (null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
							logger.logInfo("Item : "+itmPrimaryKey+" MASTER-02 Package GTIN is empty. Setting  Master_PackageGTIN_Unique to Null");
							collabItem.setAttributeValue("Product Catalog Global Spec/Master_PackageGTIN_Unique", null);
						}
						 // Rule 4.9 End 
*/							
						 // Rule 75.3 Start 
						logger.logDebug("start // Rule 75.3..");
						if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/PackagingMaterialCompositionQuantityUnit")){
					    	collabItem.setAttributeValue(higherPackLvlPath + "/PackagingMaterialCompositionQuantityUnit","Each");
					    }
						 // Rule 75.3 End 
						
						 // Rule 66.3 Start 
						logger.logDebug("start // Rule 66.3..");
						if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/EANUCCType")){
					    	collabItem.setAttributeValue(higherPackLvlPath+"/EANUCCType","GTIN_14");
					    }
						 // Rule 66.3 End 
						
						 // Rule 81.2 Start 
						logger.logDebug("start // Rule 81.2..");
						if (null != collabStep && (collabStep.getName().equalsIgnoreCase("Product Management Maintenance")
								|| collabStep.getName().equalsIgnoreCase("Complete")
								|| collabStep.getName().equalsIgnoreCase("Manual Resolution"))){
							
							if (null != collabItem && null != collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
									&& "true".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString())){
								
								if(null == collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum")){
									arg0.addValidationError(collabItem.getAttributeInstance(higherPackLvlPath + "/SpecialOrderQuantityMinimum"), Type.VALIDATION_RULE, "packaging_level : MASTER SpecialOrderQuantityMinimum was empty even though IsItemAvailableForSpecialOrder was true");
								}
								if(null == collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple")){
									arg0.addValidationError(collabItem.getAttributeInstance(higherPackLvlPath + "/SpecialOrderQuantityMultiple"), Type.VALIDATION_RULE, "packaging_level : MASTER SpecialOrderQuantityMultiple was empty even though IsItemAvailableForSpecialOrder was true");
								}
								
								
							}else if(null != collabItem && (null == collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
									|| "false".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString()))){
								collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTime", null);
								collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnit", null);
								if(null != collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum")){
									collabItem.setAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum", null);
								}
								if(null != collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple")){
									collabItem.setAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple", null);
								}
							}
						}
						 // Rule 81.2 End 
						
						
					}
					if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "PALLET".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
						
						 /*// Rule 4.10 Start 
						logger.logDebug("start // Rule 4.10..");
						if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
							logger.logInfo("Item : "+itmPrimaryKey+" PALLET Package GTIN is Not empty. Calculating  Pallet_PackageGTIN_Unique value ");
							collabItem.setAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique", collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
						}
						 // Rule 4.10 End 
						
						 // Rule 4.11 Start 
						logger.logDebug("start // Rule 4.11..");
						if (null != collabItem.getAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique")){
							if (null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
								logger.logInfo("Item : "+itmPrimaryKey+" PALLET Package GTIN is empty. Setting  Pallet_PackageGTIN_Unique to Null");
								collabItem.setAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique", null);
							}
						}
						 // Rule 4.11 End 
*/							
						 // Rule 75.4 Start 
						logger.logDebug("start // Rule 75.4..");
						if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath + "/PackagingMaterialCompositionQuantityUnit")){
					    	collabItem.setAttributeValue(higherPackLvlPath + "/PackagingMaterialCompositionQuantityUnit","Each");
					    }
						 // Rule 75.4 End 
						
						 // Rule 66.4 Start 
						logger.logDebug("start // Rule 66.4..");
						if (null != collabItem && null == collabItem.getAttributeValue(higherPackLvlPath+"/EANUCCType")){
					    	collabItem.setAttributeValue(higherPackLvlPath+"/EANUCCType","GTIN_14");
					    }
						 // Rule 66.4 End 
						
						 // Rule 81.2 Start 
						logger.logDebug("start // Rule 81.2..");
						if (null != collabStep && (collabStep.getName().equalsIgnoreCase("Product Management Maintenance")
								|| collabStep.getName().equalsIgnoreCase("Complete")
								|| collabStep.getName().equalsIgnoreCase("Manual Resolution"))){
							
							if (null != collabItem && null != collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
									&& "true".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString())){
								
								if(null == collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum")){
									arg0.addValidationError(collabItem.getAttributeInstance(higherPackLvlPath + "/SpecialOrderQuantityMinimum"), Type.VALIDATION_RULE, "packaging_level : PALLET SpecialOrderQuantityMinimum was empty even though IsItemAvailableForSpecialOrder was true");
								}
								if(null == collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple")){
									arg0.addValidationError(collabItem.getAttributeInstance(higherPackLvlPath + "/SpecialOrderQuantityMultiple"), Type.VALIDATION_RULE, "packaging_level : PALLET SpecialOrderQuantityMultiple was empty even though IsItemAvailableForSpecialOrder was true");
								}
								
								
							}else if(null != collabItem && (null == collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder")
									|| "false".equalsIgnoreCase(collabItem.getAttributeValue("Product Catalog Global Spec/IsItemAvailableForSpecialOrder").toString()))){
								collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTime", null);
								collabItem.setAttributeValue("Product Catalog Global Spec/SpecialOrderQuantityLeadTimeUnit", null);
								if(null != collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum")){
									collabItem.setAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMinimum", null);
								}
								if(null != collabItem.getAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple")){
									collabItem.setAttributeValue(higherPackLvlPath + "/SpecialOrderQuantityMultiple", null);
								}
							}
						}
						 // Rule 81.2 End 
						
					}
				}
			}
			// Rule 95.1 Start 
			logger.logDebug("start // Rule 95..");

			// Going to fetch  and set ClassificationCategoryCode-GPC  from ClassificationCategoryName-GPC

			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/ClassificationCategoryName-GPC") )
			{
				String classificationCategoryName = collabItem.getAttributeValue("Product Catalog Global Spec/ClassificationCategoryName-GPC").toString() ;
				String classificationCatCode = utilityMethods.getLkpValueForKey(ctx, "gpcBrickName - GDSN Table", classificationCategoryName, logger);
				if(null != classificationCatCode)
				{
					collabItem.setAttributeValue("Product Catalog Global Spec/ClassificationCategoryCode-GPC",classificationCatCode);
				}
			}
			 // Rule 95.1 End 
			
			 // Rule 85.1 Start 
			logger.logDebug("start // Rule 85..");

			// Going to set DataPoolItemStatus based on ItemStatus value

			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/ItemStatus"))
			{
				String itemStatusVal = collabItem.getAttributeValue("Product Catalog Global Spec/ItemStatus").toString() ;
				if(itemStatusVal.equals("AC") || itemStatusVal.equals("HOLD")  || itemStatusVal.equals("INXS")   || itemStatusVal.equals("PO SELL") )
				{
					collabItem.setAttributeValue("Product Catalog Global Spec/DataPoolItemStatus","ACTIVE") ;
				}
				else if(itemStatusVal.equals("RESTRICT") || itemStatusVal.equals("NFS")  || itemStatusVal.equals("CR") )
				{
					collabItem.setAttributeValue("Product Catalog Global Spec/DataPoolItemStatus","OBSOLETE") ;
				}
				else if(itemStatusVal.equals("PP") || itemStatusVal.equals("PILOT")  || itemStatusVal.equals("PM")  || itemStatusVal.equals("PHANTOM"))
				{
					collabItem.setAttributeValue("Product Catalog Global Spec/DataPoolItemStatus","NO SYNC") ;
				}
			}
			 // Rule 85.1 End
			
			 // Rule 86.1 Start 
			logger.logDebug("start // Rule 86..");

			// IF length (EANUCCCode) > 5 then  item['EANUCC-ProductPortion'] = item['EANUCCCode'].substring ( item['EANUCCCode'].length() - 6 ); 

			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode"))
			{
				String eanUCCCode = (String)collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode");
				String trimmedValue="";
				if(eanUCCCode.startsWith("8402130")){
					trimmedValue = eanUCCCode.substring(eanUCCCode.length()-5);
				}else{
					trimmedValue = eanUCCCode.substring(eanUCCCode.length()-6);
				}
				collabItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ProductPortion",trimmedValue);
			}
			 // Rule 86.1 End
			
			// Rule 138.1 Start 
			logger.logDebug("start // Rule 138..");

			// Going to populate EANUCC-ManufacturerPortion attribute if EANUCCCode is not empty
			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode"))
			{
				String eanUCCCode = (String) collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode");
				if(eanUCCCode.length() > 5)
				{
					String eanUCCManufacturerPortion = "";
					if (eanUCCCode.startsWith("8402130")){
						eanUCCManufacturerPortion = eanUCCCode.substring(0, 7);
					}else{
						eanUCCManufacturerPortion = eanUCCCode.substring(0, 6);
					}
					collabItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ManufacturerPortion", eanUCCManufacturerPortion);
				}

			}
			 // Rule 138.1 End 
			
			// Rule 136.1 and 135.1 Start 
			
			logger.logDebug("start // Rule 135 & // Rule 136..");
			if(null !=collabItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US")  )
			{
				String shortDesc_enUS = (String)collabItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US") ;
				collabItem.setAttributeValue("Product Catalog Global Spec/GtinName_en-US", shortDesc_enUS);
				collabItem.setAttributeValue("Product Catalog Global Spec/FunctionalName_en-US", shortDesc_enUS);
			}
			 // Rule 136.1 and 135.1 End 
			
			 // Rule 100.1 Start 
			logger.logDebug("start // Rule 100..");

			// Going to populate LongDescription_en-US  by concatenating BrandName and DescriptionShort_en-US

			//if(null != collabItem.getAttributeValue("Product Catalog Global Spec/BrandName") &&  null != collabItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US") )
			//{
				String brandName = (null != collabItem.getAttributeValue("Product Catalog Global Spec/BrandName"))?collabItem.getAttributeValue("Product Catalog Global Spec/BrandName").toString():"";
				//String brandName = collabItem.getAttributeValue("Product Catalog Global Spec/BrandName").toString();
				String shortDesc = (null != collabItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US"))?collabItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US").toString():"";
				//String shortDesc = (String)collabItem.getAttributeValue("Product Catalog Global Spec/DescriptionShort_en-US");
				String longDesc = brandName+" "+shortDesc;
				collabItem.setAttributeValue("Product Catalog Global Spec/LongDescription_en-US",longDesc.trim());
			//}
			 // Rule 100.1 End 
			
			// Rule 139.1 Start 
		    logger.logDebug("start // Rule 139..");
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription"))
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription", "Call for Authorization");
			}
			  // Rule 139.1 End 
			
			 // Rule 139.2 Start 
			logger.logDebug("start // Rule 139.2..");
			if (null != collabItem.getAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription")){
				collabItem.setAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyCode",utilityMethods.getLkpValueForKey(ctx,"ReturnGoodsPolicy - GDSN Table",
				collabItem.getAttributeValue("Product Catalog Global Spec/ReturnGoodsPolicyDescription").toString(),logger));
			}
		     // Rule 139.2 End 
			
			// Rule 134.1 Start 
			logger.logDebug("start // Rule 134..");
			// Going to populate TradeItemMarketingMessage_en-US attribute with value as Description-Marketing attribute if it is empty
			if(null !=collabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") )
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/TradeItemMarketingMessage_en-US", collabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") );
			}
			 // Rule 134.1 End 
			
			 // Rule 4.3 Start 
			logger.logDebug("start // Rule 4.3..");
			if (null == collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique")){
				if (null != collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode") && null != sysOfRec){
					logger.logInfo("Item : "+itmPrimaryKey +" EANUCCCode is Not Null. Calculating EANUCCCode_Unique");
					collabItem.setAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique",collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode") + " " + sysOfRec);
				}
			}
			 // Rule 4.3 End 
			
			 // Rule 4.4 Start 
			logger.logDebug("start // Rule 4.4..");
			if (null == collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode")){
				logger.logInfo("Collab Item : "+itmPrimaryKey +" EANUCCCode is Null. Setting EANUCCCode_Unique,EA:PackageGTIN,EA_PackageGTIN_Unique to Null");
				if( null != collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique"))
					collabItem.setAttributeValue("Product Catalog Global Spec/EANUCCCode_Unique", null);
				if(null != collabItem.getAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique"))
					collabItem.setAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique", null);
				if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN"))
					collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN", null);
				if (null != collabItem.getAttributeValue("Product Catalog Global Spec/EANUCC-ProductPortion")){
					logger.logInfo("Setting Product Catalog Global Spec/EANUCC-ProductPortion to Null");
					collabItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ProductPortion",null);
				}
				if (null != collabItem.getAttributeValue("Product Catalog Global Spec/EANUCC-ManufacturerPortion")){
					logger.logInfo("Setting Product Catalog Global Spec/EANUCC-ManufacturerPortion to Null");
					collabItem.setAttributeValue("Product Catalog Global Spec/EANUCC-ManufacturerPortion",null);
				}
			
				
			}
			 // Rule 4.4 End
			
			// Rule 4.1 Start 
			logger.logDebug("start // Rule 4.1..");
			if (null != collabItem.getAttributeValue("Product Catalog Global Spec/ModelNumber") && null != sysOfRec){
				logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" ModelNumber is Not Null. Calculating ModelNumber_Unique");
				collabItem.setAttributeValue("Product Catalog Global Spec/ModelNumber_Unique",collabItem.getAttributeValue("Product Catalog Global Spec/ModelNumber") + " " + sysOfRec);
			}
			 // Rule 4.1 End 
			
			 // Rule 4.2 Start 
			logger.logDebug("start // Rule 4.2..");
			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/ModelNumber_Unique")){
				if ( collabItem.getAttributeValue("Product Catalog Global Spec/ModelNumber") ==  null ) {
					logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" ModelNumber is Null. Setting ModelNumber_Unique to Null");
					collabItem.setAttributeValue("Product Catalog Global Spec/ModelNumber_Unique",null);
				 }
			}
			 // Rule 4.2 End 
			
			 // Rule 4.12 Start 
			logger.logDebug("start // Rule 4.12..");
			if (null != collabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US") && null != sysOfRec){
				logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" AdditionalTradeItemDescription_en-US is Not Null. Calculating AdditionalTradeItemDescription_en-US_Unique");
				collabItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US_Unique",collabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US") + " " + sysOfRec);
			}
			 // Rule 4.12 End 
			
			 // Rule 4.13 Start 
			logger.logDebug("start // Rule 4.13..");
			if (null == collabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US")){
				logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" AdditionalTradeItemDescription_en-US is Null. Setting AdditionalTradeItemDescription_en-US_Unique to Null");
				collabItem.setAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US_Unique",null);
			}
			 // Rule 4.13 End 
			
			// Rule 4.14 Start 
			logger.logDebug("start // Rule 4.14..");
			if (null != collabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") && null != sysOfRec){
				logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" Description-Marketing is Not Null. Calculating Description-Marketing_Unique");
				collabItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing_Unique",collabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing") + " " + sysOfRec);
			}
			 // Rule 4.14 End 
			
			 // Rule 4.15 Start 
			logger.logDebug("start // Rule 4.15..");
			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing_Unique")){
				if (null == collabItem.getAttributeValue("Product Catalog Global Spec/Description-Marketing")){
					logger.logInfo("Step : Product Management Maintenance. Item : "+itmPrimaryKey +" Description-Marketing is Null. Setting Description-Marketing_Unique to Null");
					collabItem.setAttributeValue("Product Catalog Global Spec/Description-Marketing_Unique",null);
				}
			}
			 // Rule 4.15 End 
			
			// Rule 4.5 Start 
			logger.logDebug("start // Rule 4.5..");
			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique")){
				if (null == collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN")){
					logger.logInfo("Item : "+itmPrimaryKey +" EA:PackageGTIN is Null. Calculating EA_PackageGTIN_Unique");
					collabItem.setAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique", null);
				}
			}
			 // Rule 4.5 End 
			
			 // Rule 101.1 Start 
			logger.logDebug("start // Rule 101	..");

			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode"))
			{
				String upcCode = (String)collabItem.getAttributeValue("Product Catalog Global Spec/EANUCCCode");
				String ea_PkgGTIN_Unique = "00"+upcCode+" "+sysOfRec;
				collabItem.setAttributeValue("Product Catalog Global Spec/EA_PackageGTIN_Unique",ea_PkgGTIN_Unique);
				collabItem.setAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/PackageGTIN","00"+upcCode);
			}
			 // Rule 101.1 End 
			
			if (collabItem.getAttributeInstance(Constant.PART_PATH_IN_SPEC).getChildren().size()>0 && collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().size()> 0){
				logger.logInfo("Item : "+itmPrimaryKey + " Higher Packaging Exists");
				Iterator<? extends AttributeInstance> packLvlIter = collabItem.getAttributeInstance(Constant.PACK_LEVEL_PATH_IN_SPEC).getChildren().iterator();
				while(packLvlIter.hasNext()){
					AttributeInstance chldIns = packLvlIter.next();
					String higherPackLvlPath = chldIns.getPath();
					logger.logInfo("Item : "+itmPrimaryKey + "Path "+higherPackLvlPath);
					if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "INNER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
						// Rule 4.6 Start 
						logger.logDebug("start // Rule 4.6..");
						if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
							logger.logInfo("Item : "+itmPrimaryKey+" Inner Package GTIN is Not empty. Calculating  Inner_PackageGTIN_Unique value ");
							collabItem.setAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique", collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
						}
						 // Rule 4.6 End 
						
						 // Rule 4.7 Start 
						logger.logDebug("start // Rule 4.7..");
						if (null != collabItem.getAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique")){
							if (null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
								logger.logInfo("Item : "+itmPrimaryKey+" Inner Package GTIN is empty. Setting  Inner_PackageGTIN_Unique to Null");
								collabItem.setAttributeValue("Product Catalog Global Spec/Inner_PackageGTIN_Unique", null);
							}
						}
						 // Rule 4.7 End
					}
					
					if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "MASTER".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
						// Rule 4.8 Start 
						logger.logDebug("start // Rule 4.8..");
						if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
							logger.logInfo("Item : "+itmPrimaryKey+" MASTER-02 Package GTIN is Not empty. Calculating  Master_PackageGTIN_Unique value ");
							collabItem.setAttributeValue("Product Catalog Global Spec/Master_PackageGTIN_Unique", collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
						}
						 // Rule 4.8 End 
						
						 // Rule 4.9 Start 
						logger.logDebug("start // Rule 4.9..");
						if (null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
							logger.logInfo("Item : "+itmPrimaryKey+" MASTER-02 Package GTIN is empty. Setting  Master_PackageGTIN_Unique to Null");
							collabItem.setAttributeValue("Product Catalog Global Spec/Master_PackageGTIN_Unique", null);
						}
						 // Rule 4.9 End
					}
					
					if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH) && "PALLET".equalsIgnoreCase(collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.UOM_PATH).toString())){
						 // Rule 4.10 Start 
						logger.logDebug("start // Rule 4.10..");
						if (null != collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) && null != sysOfRec){
							logger.logInfo("Item : "+itmPrimaryKey+" PALLET Package GTIN is Not empty. Calculating  Pallet_PackageGTIN_Unique value ");
							collabItem.setAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique", collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN) + " " + sysOfRec);
						}
						 // Rule 4.10 End 
						
						 // Rule 4.11 Start 
						logger.logDebug("start // Rule 4.11..");
						if (null != collabItem.getAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique")){
							if (null == collabItem.getAttributeValue(higherPackLvlPath+"/"+Constant.PACKAGE_GTIN)){
								logger.logInfo("Item : "+itmPrimaryKey+" PALLET Package GTIN is empty. Setting  Pallet_PackageGTIN_Unique to Null");
								collabItem.setAttributeValue("Product Catalog Global Spec/Pallet_PackageGTIN_Unique", null);
							}
						}
						 // Rule 4.11 End
					}
				}
			}
			
			// Rule 14.1 Start 
			logger.logDebug("start // Rule 14.1..");
			if (null != collabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US")){
				collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", collabItem.getAttributeValue("Product Catalog Global Spec/AdditionalTradeItemDescription_en-US"));
			}else{
				if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Description1")
						&& null != collabItem.getAttributeValue("Product Catalog Global Spec/Description2")){
					collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", new StringBuilder(collabItem.getAttributeValue("Product Catalog Global Spec/Description1").toString()).append(" ").append(collabItem.getAttributeValue("Product Catalog Global Spec/Description2").toString()));
				}else if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Description1")
								&& null == collabItem.getAttributeValue("Product Catalog Global Spec/Description2")){
					collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", collabItem.getAttributeValue("Product Catalog Global Spec/Description1").toString());
				}else if(null != collabItem.getAttributeValue("Product Catalog Global Spec/Description2")
								&& null == collabItem.getAttributeValue("Product Catalog Global Spec/Description1")){
					collabItem.setAttributeValue("Product Catalog Global Spec/DESCRIPTION", collabItem.getAttributeValue("Product Catalog Global Spec/Description2").toString());
				} 
			}
			 // Rule 14.1 End	
			
			 // Rule 79.1 Start 
			logger.logDebug("start // Rule 79..");

			// Setting  ImportClassificationValue and ImportClassificationType based on certain business criterias  HTSNumber

			if(null != collabItem.getAttributeValue("Product Catalog Global Spec/HTSNumber"))
			{
				String htsNumber = (String)collabItem.getAttributeValue("Product Catalog Global Spec/HTSNumber");
				String importClassficationVal = htsNumber.replaceAll("\\.", "");
				collabItem.setAttributeValue("Product Catalog Global Spec/ImportClassificationValue",importClassficationVal);
			}
			 // Rule 79.1 End 
			
			 // Rule 79.2 Start 
			logger.logDebug("start // Rule 79.2..");
			if(null == collabItem.getAttributeValue("Product Catalog Global Spec/ImportClassificationType") && null !=collabItem.getAttributeValue("Product Catalog Global Spec/ImportClassificationValue") )
			{
				collabItem.setAttributeValue("Product Catalog Global Spec/ImportClassificationType","HTS");
			}
			
			 // Rule 79.2 End 
			//System Rules End
			//End
			logger.logInfo("COLLABORATION AREA POST Processing Complete");
		}catch(Exception e){
			logger.logInfo("Exception occurred");
			logger.logInfo(e);
			logger.logInfo(e.getMessage());
			e.printStackTrace();
		}finally
		{
			if(null != ctx)
			{
				//ctx.commit();
				//ctx.cleanUp();
			}
		}
		
	}

	@Override
	public void prePostProcessing(
			CollaborationCategoryPrePostProcessingFunctionArguments arg0) {
		
		
	}

}
