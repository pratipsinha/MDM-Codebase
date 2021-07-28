package com.watts.phaseII.workflowSteps;

import com.ibm.pim.collaboration.CollaborationArea;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.CollaborationStepTransitionConfiguration;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.extensionpoints.WorkflowStepFunction;
import com.ibm.pim.extensionpoints.WorkflowStepFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ibm.pim.workflow.WorkflowStep;
import com.watts.phaseII.constants.WattsConstants;
import com.watts.phaseII.helper.AssociateGDSNHelper;
import com.watts.phaseII.helper.CommonHelper;

public class AssociateCustomerGDSN implements WorkflowStepFunction 
{

	public AssociateCustomerGDSN() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void in(WorkflowStepFunctionArguments arg0) 
	{
		// TODO Auto-generated method stub
		
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		AssociateGDSNHelper assocGDSNHelperOb = null;
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();
			
			if(null != pimCtx)
			{
				//ProductManagementStepHelper helperObj = new ProductManagementStepHelper();
				wattsLogger = pimCtx.getLogger("com.ibm.ccd.wpc_user_scripting.Watts_WorflowFunctions");
				wattsLogger.logInfo("----Logger Object Retrieved successfully :ASSOCAITE_CUSTOMER_GDSN:Workflow_IN-----");
				
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
				
				
				wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:----------------Print the Step Objects-------------------");
				//wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:---------------------------------------------------------");
				
				assocGDSNHelperOb = new AssociateGDSNHelper();
				
				String exit_val = "NOT_REQUIRED";
				
				if(true)
				{
					exit_val = "DONE";
				}
				
				// Setting exit values
				for (CollaborationItem collabItem : collabItemList) 
				{
					String partNumber = null;
					partNumber = (null !=collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"") ;
					wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:--------------ITEM PRIMARY_KEY------------::"+collabItem.getPrimaryKey());
					wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:--------------ITEM PART_NUMBER------------::"+partNumber);
					assocGDSNHelperOb.setCollabEntryAttributesForStep(collabItem, pimCtx, wattsLogger);
					colConfig.setExitValue(collabItem, wrkflowStepObj.createExitValue(exit_val));
					
				}
				wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:---------------------------------------------------------");
				
			}else
			{
				System.out.println("-:ASSOCAITE_CUSTOMER_GDSN:--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:----Exception---"+e.getMessage());
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
