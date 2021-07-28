package com.ww.pim.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.ibm.pim.attribute.AttributeChanges;
import com.ibm.pim.attribute.AttributeCollection;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.common.Entry;
import com.ibm.pim.context.Context;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.utils.Logger;

public class WorkflowUtilsMethods {
	
	
	
	public  boolean isGDSNCandidate(CollaborationItem collabItem,Logger logger){
		boolean ret = false;
		try {
			Collection<Category> catList = collabItem.getCategories();
			String CUSTOMER_CATALOG_GDSN_PREFIX = "GDSN - ";
			String pattern = "(?i)^"+ CUSTOMER_CATALOG_GDSN_PREFIX + ".*$";
			if (null != catList && catList.size() > 0){
				logger.logInfo(" Collaboration Item : "+collabItem.getPrimaryKey() + " Has got Categories : [OK]");
				for (Category category : catList){
					if (category.getHierarchy().getName().equals("Customer Catalogs")){
						logger.logInfo(" Collaboration Item : "+collabItem.getPrimaryKey() + " mapped to Customer Catelog Categories :"+category.getName()+" : [OK]");
						if (category.getName().matches(pattern)){
							logger.logInfo(" Collaboration Item : "+collabItem.getPrimaryKey() + " GDSN Pattern Found : "+category.getName()+" : [OK]");
							ret = true;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return ret;
	}
	
	public ArrayList<String> getGDSNCategoryList(CollaborationItem collabItem, Item itemObj,Logger logger){
		ArrayList<String> gdsnCatPKs = new ArrayList<String>();
		String CUSTOMER_CATALOG_GDSN_PREFIX = "GDSN - ";
		String pattern = "(?i)^"+ CUSTOMER_CATALOG_GDSN_PREFIX + ".*$";
		if (null != collabItem){
			Collection<Category> collabCategories = collabItem.getCategories();
			if (null != collabCategories){
				for (Category category : collabCategories){
					if (category.getHierarchy().getName().equals("Customer Catalogs")){
						logger.logInfo(" Collaboration Item : "+collabItem.getPrimaryKey() + " mapped to Customer Catelog Categories :"+category.getName()+" : [OK]");
						if (category.getName().matches(pattern)){
							logger.logInfo(" Collaboration Item : "+collabItem.getPrimaryKey() + " GDSN Pattern Found : "+category.getName()+" : [OK]");
							gdsnCatPKs.add(category.getPrimaryKey());
						}
					}
				}
			}
		}else{
			Collection<Category> itemCategories = itemObj.getCategories();
			if (null != itemCategories){
				for (Category category : itemCategories){
					if (category.getHierarchy().getName().equals("Customer Catalogs")){
						logger.logInfo(" Catalog Item : "+itemObj.getPrimaryKey() + " mapped to Customer Catelog Categories :"+category.getName()+" : [OK]");
						if (category.getName().matches(pattern)){
							logger.logInfo(" Catalog Item : "+itemObj.getPrimaryKey() + " GDSN Pattern Found : "+category.getName()+" : [OK]");
							gdsnCatPKs.add(category.getPrimaryKey());
						}
					}
				}
			}
		}
		return gdsnCatPKs;
	}
	
	public  boolean haveGDSNTradingPartnerMappingsChanged(CollaborationItem collabItem, Item itemObj,String partNumber, Logger logger){
		
		boolean ret = false;
		try {
			ArrayList<String> collabCatList = this.getGDSNCategoryList(collabItem, null, logger);
			ArrayList<String> itemCatList = this.getGDSNCategoryList(null, itemObj, logger);
			int sizeBefore = (null == itemCatList ? 0 : itemCatList.size());
			int sizeAfter = (null == collabCatList ? 0 : collabCatList.size());
			if (sizeBefore != sizeAfter){
				logger.logInfo("Part Number  : "+partNumber+ " GDSN Catalog Mapping changed");
				ret = true;
			}else{
				logger.logInfo("Part Number  : "+partNumber+ " GDSN Catalog Mapping Size not changed");
				if(!new HashSet<String>(collabCatList).equals(new HashSet<String>(itemCatList))){
					ret = true;
				}
				logger.logInfo("Part Number  : "+partNumber+ " Return Value "+ret);
			}
		} catch (Exception e) {
			logger.logInfo("Exception in haveGDSNTradingPartnerMappingsChanged method : [ NOT OK]");
			logger.logInfo(e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	
	public boolean isGDSNAttribChanged(CollaborationItem collabItem ,Item itemObj,Context ctx, String partNumber, Logger logger){
		boolean gdsnAttribChanged = false;
		List<AttributeInstance> modifiedAttribNames = new ArrayList<AttributeInstance>();
		List<? extends AttributeInstance> modifiedAttribNamesOld = null;
		List<? extends AttributeInstance> addedAttribNames = null;
		List<? extends AttributeInstance> deletedAttribNames = null;
		ArrayList<String> changedAttrList = new ArrayList<String>();
		AttributeChanges entry_changed_data = null;
		ArrayList<String> gdsnAttrPaths = new ArrayList<String>();
		
		try {
			logger.logInfo("Part Number : "+partNumber+ " getGDSNAttribNames() method called : [OK]");
			Entry oldItemObj = collabItem.getOriginalEntry();
			if (null != oldItemObj){
				logger.logInfo("Part Number : "+partNumber+ " Old Item Obj is not null : [OK]");
				entry_changed_data = oldItemObj.getAttributeChangesComparedTo(itemObj);
			}
			if (null != entry_changed_data){
				
				logger.logInfo("Part Number : "+partNumber+ " Entry Changed data is not null : [OK]");
				modifiedAttribNamesOld = entry_changed_data.getModifiedAttributesWithNewData();
				addedAttribNames = entry_changed_data.getDeletedAttributes();
				deletedAttribNames = entry_changed_data.getNewlyAddedAttributes();
				if (null != modifiedAttribNamesOld){
					modifiedAttribNames.addAll(modifiedAttribNamesOld);
				}
				if(null != addedAttribNames){
					modifiedAttribNames.addAll(addedAttribNames);
				}
				if(null != deletedAttribNames){
					modifiedAttribNames.addAll(deletedAttribNames);
				}
				AttributeCollection gdsnAttrCollection = ctx.getAttributeCollectionManager().getAttributeCollection("GDSN Attributes");
				if (null != gdsnAttrCollection){
					logger.logInfo("Part Number : "+partNumber+ " GDSN Attribute Collection Obtained : [OK]");
					Collection<AttributeDefinition> gdsnAttrs = gdsnAttrCollection.getAttributes();
					if (null != gdsnAttrs){
						logger.logInfo("Part Number : "+partNumber+" GDSN Attributes are not null : [OK]");
						for(AttributeDefinition attrDef : gdsnAttrs){
							gdsnAttrPaths.add(attrDef.getPath());
						}
					}
				}
				
				logger.logInfo("=====Modified Attributes=======");
				for (int i =0; i<modifiedAttribNames.size() ; i++){
					String attrPath = modifiedAttribNames.get(i).getPath();
					String modifiedPath = "";
					if (null != attrPath || !attrPath.equals("")){
						// This reqular expression can replace #<digit><digit>  while #[0-9] can replace #<digit>
						modifiedPath = attrPath.replaceAll("#[0-9]*", "").substring(1);
						
					}
					changedAttrList.add(modifiedPath);
					logger.logInfo(modifiedPath);
				}
				boolean breakAll = false;
				if (null != gdsnAttrPaths && gdsnAttrPaths.size()> 0 && null != changedAttrList && changedAttrList.size() > 0){
					logger.logInfo("inside..");
					for (int changed_attribute_counter =0; changed_attribute_counter < changedAttrList.size(); changed_attribute_counter++){
						for (String gdsnAttributePath : gdsnAttrPaths) {
							if (gdsnAttributePath.equals(changedAttrList.get(changed_attribute_counter))){
								logger.logInfo("GDSN Attribute Value Changed : "+gdsnAttributePath);
								gdsnAttribChanged = true;
								breakAll = true;
								break;
							}
						}
						if(breakAll){
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.logInfo("Part Number : "+partNumber+ " Exception occurrec in getGDSNAttribNames "+e.getMessage());
			e.printStackTrace();
		}
		return gdsnAttribChanged;
	}
}
