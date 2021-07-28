package com.watts.phaseII.workflowSteps;

import com.ibm.pim.collaboration.CollaborationArea;
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
import com.watts.phaseII.constants.WattsConstants;
import com.watts.phaseII.helper.CommonHelper;
import com.watts.phaseII.helper.CompleteStepHelper;
import com.watts.phaseII.helper.ProductManagementHelper;

public class ProductManagement implements WorkflowStepFunction 
{

	
	
	@Override
	public void in(WorkflowStepFunctionArguments arg0) 
	{
		// TODO Auto-generated method stub
		
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		ProductManagementHelper pmHelper = null;
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();
			
			if(null != pimCtx)
			{
				//ProductManagementStepHelper helperObj = new ProductManagementStepHelper();
				wattsLogger = pimCtx.getLogger("com.ibm.ccd.wpc_user_scripting.Watts_WorflowFunctions");
				wattsLogger.logInfo("----Logger Object Retrieved successfully:PRODUCT MANAGEMENT:Workflow_IN-----");
				
				//Usefull configuration objects for entry redirection with exit values
				CollaborationStepTransitionConfiguration colConfig = arg0.getTransitionConfiguration(); 
				CollaborationStep collabStepObj = arg0.getCollaborationStep();
				/*---*/
				//Get the objects in the workflow step argument--
				
				PIMCollection<CollaborationItem> collabItemList = arg0.getItems();
				
				String collabStepName = collabStepObj.getName();
				CollaborationArea collabArea = collabStepObj.getCollaborationArea();
				String collabAreaName = collabArea.getName();
				WorkflowStep wrkflowStepObj = collabStepObj.getWorkflowStep();
				String workflowStepName = wrkflowStepObj.getName();
				
				
				wattsLogger.logInfo("-:PRODUCT MANAGEMENT:--------------Print the Step Objects---------------------");
				//wattsLogger.logInfo("-:PRODUCT MANAGEMENT:-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:PRODUCT MANAGEMENT:-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:PRODUCT MANAGEMENT:-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:PRODUCT MANAGEMENT:---------------------------------------------------------");
				
				String exit_val = "DONE";
				
				pmHelper = new ProductManagementHelper();
				
				// Setting exit values
				for (CollaborationItem collabItem : collabItemList) 
				{
					String partNumber = null;
					partNumber = (null !=collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"") ;
					wattsLogger.logInfo("-:PRODUCT MANAGEMENT:--------------ITEM PRIMARY_KEY------------::"+collabItem.getPrimaryKey());
					wattsLogger.logInfo("-:PRODUCT MANAGEMENT:--------------ITEM PART_NUMBER------------::"+partNumber);
					pmHelper.setEntryAttributesForStep(collabItem, pimCtx, wattsLogger);
					colConfig.setExitValue(collabItem, wrkflowStepObj.createExitValue(exit_val));
					
				}
				wattsLogger.logInfo("-:PRODUCT MANAGEMENT:---------------------------------------------------------");
				
			}else
			{
				System.out.println("-:PRODUCT MANAGEMENT:--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:PRODUCT MANAGEMENT:----Exception---"+e.getMessage());
			}
		
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
