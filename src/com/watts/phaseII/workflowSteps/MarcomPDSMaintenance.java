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
import com.watts.phaseII.helper.CommonHelper;

public class MarcomPDSMaintenance implements WorkflowStepFunction {

	public MarcomPDSMaintenance() {
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
				wattsLogger.logInfo("----Logger Object Retrieved successfully :MAR PDS MAINTENANCE :Workflow_IN-----");
				
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
				
				
				wattsLogger.logInfo("-:MAR PDS MAINTENANCE :----------------Print the Step Objects-------------------");
				//wattsLogger.logInfo("-:MAR PDS MAINTENANCE :-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:MAR PDS MAINTENANCE :-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:MAR PDS MAINTENANCE :-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:MAR PDS MAINTENANCE :---------------------------------------------------------");
				
				String exit_val = "DONE";
				
				// Setting exit values
				for (CollaborationItem collaborationItem : collabItemList) 
				{
					wattsLogger.logInfo("-:MAR PDS MAINTENANCE :--------------ITEM PRIMARY_KEY------------::"+collaborationItem.getPrimaryKey());
					colConfig.setExitValue(collaborationItem, wrkflowStepObj.createExitValue(exit_val));
					
				}
				wattsLogger.logInfo("-:MAR PDS MAINTENANCE :---------------------------------------------------------");
				
			}else
			{
				System.out.println("-:MAR PDS MAINTENANCE :--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:MAR PDS MAINTENANCE :----Exception---"+e.getMessage());
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
