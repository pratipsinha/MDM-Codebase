package com.ww.pim.workflow;


import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.CollaborationStepTransitionConfiguration;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.WorkflowStepFunction;
import com.ibm.pim.extensionpoints.WorkflowStepFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ibm.pim.workflow.WorkflowStep;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.common.utils.WorkflowUtilsMethods;

public class AssociateGDSN implements WorkflowStepFunction{

	@Override
	public void in(WorkflowStepFunctionArguments arg0) {
		Context ctx = null;
		Logger logger = null;
		CollaborationStep collabStep = null;
		CollaborationStepTransitionConfiguration collabTranConfig = null;
		PIMCollection<CollaborationItem> collabItemList = null;
		WorkflowStep workflowStep = null;
		String collabStepName = "";
		Catalog prodCatalog = null;
		WorkflowUtilsMethods utilityMethods = new WorkflowUtilsMethods();
		try{
			System.out.println("AssociateGDSN Step In method called : [OK]");
			if (null == ctx){
				ctx = PIMContextFactory.getCurrentContext();
			}
			if (null != ctx){
				logger = ctx.getLogger(Constant.WORKFLOW_LOGGER_NAME);
				logger.logInfo("AssociateGDSN Step Context Obtained : [OK]");
				collabStep = arg0.getCollaborationStep();
				collabStepName = collabStep.getName();
				workflowStep = collabStep.getWorkflowStep();
				collabTranConfig = arg0.getTransitionConfiguration();
				collabItemList = arg0.getItems();
				prodCatalog = ctx.getCatalogManager().getCatalog(Constant.PRODUCT_CATALOG);
				if (null != collabItemList && collabItemList.size() > 0){
					logger.logInfo("Items Available in Associate with Customer via GDSN step : [OK]");
					for (CollaborationItem collabItem : collabItemList){
						String itmPrimaryKey = collabItem.getPrimaryKey();
						String partNumber = (String) collabItem.getAttributeValue(Constant.PRODUCT_CATALOG_GLOBAL_SPEC+"/"+ Constant.CONST_PART_NO);
						String exitValue = "NOT_REQUIRED";
						if (utilityMethods.isGDSNCandidate(collabItem, logger)){
							logger.logInfo("Collab Item "+ itmPrimaryKey + " is a GDSN Candidate : [OK]");
							
							Item ctgItem = null;
							logger.logInfo("itmPrimaryKey "+itmPrimaryKey);
							ctgItem = prodCatalog.getItemByPrimaryKey(itmPrimaryKey);
							if (null == ctgItem){
								logger.logInfo("Item "+itmPrimaryKey +" is not available in Product Catalog : [OK]");
								exitValue = "DONE";
							}else{
								logger.logInfo("Item "+itmPrimaryKey +" is available in Product Catalog. Now to check mapping Change : [OK]");
								boolean HAVE_GDSN_TRADING_PARTNER_MAPPINGS_CHANGED = false;
								HAVE_GDSN_TRADING_PARTNER_MAPPINGS_CHANGED = utilityMethods.haveGDSNTradingPartnerMappingsChanged(collabItem, ctgItem, partNumber, logger);
								
								if (HAVE_GDSN_TRADING_PARTNER_MAPPINGS_CHANGED){
									logger.logInfo("Part Number : "+partNumber+ " HAVE_GDSN_TRADING_PARTNER_MAPPINGS_CHANGED : "+HAVE_GDSN_TRADING_PARTNER_MAPPINGS_CHANGED);
									exitValue = "DONE";
								}else{
									logger.logInfo("Part Number : "+partNumber+ " GDSN attribute Change Code Start : [OK]");
									 if(utilityMethods.isGDSNAttribChanged(collabItem,ctgItem,ctx,partNumber,logger)){
										 logger.logInfo("Part Number : "+partNumber+ " GDSN Attribute changed so Resync Required : [OK]");
										 exitValue = "DONE";
									 }
									logger.logInfo("Part Number : "+partNumber+ " GDSN attribute Change Code End : [OK]");
								}
							}
							
						}else{
							logger.logInfo("Collab Item "+ itmPrimaryKey + " is not a GDSN Candidate : [OK]");
							exitValue = "NOT_REQUIRED";
						}
						collabTranConfig.setExitValue(collabItem, workflowStep.createExitValue(exitValue));
						logger.logInfo("Items Available in Associate with Customer via GDSN step processing Complete for Item : "+itmPrimaryKey+ " Exit value : "+exitValue);
					}
				}else{
					logger.logInfo("CollabItem list is either null or empty : [NOT OK]");
				}
			}
			
		}catch (Exception exp){
			logger.logInfo(exp.getMessage());
			exp.printStackTrace();
		}
		
	}

	@Override
	public void out(WorkflowStepFunctionArguments arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void timeout(WorkflowStepFunctionArguments arg0) {
		// TODO Auto-generated method stub
		
	}

}
