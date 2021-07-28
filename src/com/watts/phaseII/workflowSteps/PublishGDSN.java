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
import com.watts.phaseII.helper.CommonHelper;

public class PublishGDSN implements WorkflowStepFunction 
{

	public PublishGDSN() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void in(WorkflowStepFunctionArguments arg0) 
	{
		// TODO Auto-generated method stub
		
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();
			
			if(null != pimCtx)
			{
				//ProductManagementStepHelper helperObj = new ProductManagementStepHelper();
				wattsLogger = pimCtx.getLogger("com.ibm.ccd.wpc_user_scripting.Watts_WorflowFunctions");
				wattsLogger.logInfo("----Logger Object Retrieved successfully :PUBLISH_TO_GDSN:Workflow_IN-----");
				
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
				
				
				wattsLogger.logInfo("-:PUBLISH_TO_GDSN:----------------Print the Step Objects-------------------");
				//wattsLogger.logInfo("-:PUBLISH_TO_GDSN:-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:PUBLISH_TO_GDSN:-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:PUBLISH_TO_GDSN:-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:PUBLISH_TO_GDSN:---------------------------------------------------------");
				
				String exit_val = "DONE";
				
				// Setting exit values
				for (CollaborationItem collaborationItem : collabItemList) 
				{
					wattsLogger.logInfo("-:PUBLISH_TO_GDSN:--------------ITEM PRIMARY_KEY------------::"+collaborationItem.getPrimaryKey());
					colConfig.setExitValue(collaborationItem, wrkflowStepObj.createExitValue(exit_val));
					
				}
				wattsLogger.logInfo("-:PUBLISH_TO_GDSN:---------------------------------------------------------");
				
			}else
			{
				System.out.println("-:PUBLISH_TO_GDSN:--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:PUBLISH_TO_GDSN:----Exception---"+e.getMessage());
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
