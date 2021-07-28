package com.ww.pim.extensionpoints.containers.hierarchy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.CategoryPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.CollaborationCategoryPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.CollaborationItemPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.ItemPrePostProcessingFunctionArguments;
import com.ibm.pim.extensionpoints.PrePostProcessingFunction;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;

public class FergusonNetChangeExportHierarchyUpdate implements PrePostProcessingFunction{

	@Override
	public void prePostProcessing(ItemPrePostProcessingFunctionArguments arg0) {
		
		
	}

	@Override
	public void prePostProcessing(
			CategoryPrePostProcessingFunctionArguments arg0) {
		System.out.println("FergusonNetChangeExportHierarchyUpdate class Method Called : [OK]");
		Context ctx = null;
		Logger logger = null;
		Hierarchy exportConfig = null;
		LinkedHashMap<String,String> mappingDtls = new LinkedHashMap<String,String>();
		FileWriter fileWriter = null;
		try{
			if (null == ctx){
				System.out.println("FergusonNetChangeExportHierarchyUpdate Context is Null. Assigning....");
				ctx = PIMContextFactory.getCurrentContext();
				logger = ctx.getLogger(Constant.IMPORT_LOGGER_NAME);
			}
			logger.logInfo("FergusonNetChangeExportHierarchyUpdate logger Obtained : [OK]");
			exportConfig = ctx.getHierarchyManager().getHierarchy(Constant.IMPORT_EXPORT_CONFIG);
			logger.logInfo(exportConfig.getName());
			Category fergusonCategory = exportConfig.getCategoryByPath(Constant.FERGUSON_CATEGORY_NAME,"/");
			logger.logInfo(fergusonCategory.getName());
			AttributeInstance attrMapping = fergusonCategory.getAttributeInstance(Constant.IMEXPT_ATTR_MAPPING);
			logger.logInfo("2222");
			fileWriter = new FileWriter(new File(System.getProperty("TOP")+Constant.FERGUSON_ATTR_PROPERTY_NEW_FILE_PATH));
			if (null != attrMapping && attrMapping.getChildren().size() > 0){
				List<? extends AttributeInstance> attrMappingLst = attrMapping.getChildren();
				logger.logInfo("FergusonNetChangeExportHierarchyUpdate attrMapping Size : "+attrMappingLst.size());
				Spec productCatalogGlobalSpec = ctx.getSpecManager().getSpec(Constant.PRODUCT_CATALOG_GLOBAL_SPEC);
				List<AttributeDefinition> attrDefs = productCatalogGlobalSpec.getAttributeDefinitions();
				for (AttributeInstance occurrance : attrMappingLst){
					String customerAttr = (String)fergusonCategory.getAttributeValue(occurrance.getPath()+Constant.CUSTOMER_ATTRIBUTE);
					String productAttr = (String) fergusonCategory.getAttributeValue(occurrance.getPath()+Constant.PRODUCT_ATTRIBUTE);
					logger.logInfo(customerAttr+ " : " +productAttr);
					if (productAttr.equals("__NULL_") || productAttr.startsWith("INNER:") || productAttr.startsWith("MASTER:") || productAttr.startsWith("PALLET:") || productAttr.equals("TEMPLATE")){
						mappingDtls.put(customerAttr, productAttr);
					}else {
						if (productAttr.startsWith("EA:")){
							List<AttributeDefinition> eachAttrDefs = productCatalogGlobalSpec.
																							getAttributeDefinition(Constant.PRODUCT_CATALOG_GLOBAL_SPEC+"/Packaging Hierarchy/Each Level")
																							.getChildren();
							for (AttributeDefinition ad : eachAttrDefs){
								if (ad.getPath().endsWith(productAttr.substring(3))){
									mappingDtls.put(customerAttr, ad.getPath());
								}
							}
						}else{
							for (AttributeDefinition attrDef : attrDefs){
								if (!attrDef.isGrouping()){
									if (attrDef.getPath().endsWith(productAttr)){
										mappingDtls.put(customerAttr, attrDef.getPath());
									}
								}
							}
						}
					}
				}
				if(mappingDtls.size() > 0){
					for(Map.Entry<String,String> entry : mappingDtls.entrySet()){
						fileWriter.write(entry.getValue()+"="+entry.getKey() + "\n");
					}
					
				}
			}
			
			logger.logInfo("FergusonNetChangeExportHierarchyUpdate try block processing complete : [OK]");
		}catch(Exception e){
			logger.logInfo("Exception occurred in FergusonNetChangeExportHierarchyUpdate method : [NOT OK]");
			logger.logInfo(e.getMessage());
			e.printStackTrace();
			
		}finally{
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void prePostProcessing(
			CollaborationItemPrePostProcessingFunctionArguments arg0) {
		
		
	}

	@Override
	public void prePostProcessing(
			CollaborationCategoryPrePostProcessingFunctionArguments arg0) {
		
		
	}

}
