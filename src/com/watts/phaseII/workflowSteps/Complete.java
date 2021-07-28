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
import com.watts.phaseII.helper.CommonHelper;
import com.watts.phaseII.helper.CompleteStepHelper;

public class Complete implements WorkflowStepFunction 
{

	public Complete() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void in(WorkflowStepFunctionArguments arg0) 
	{
		// TODO Auto-generated method stub
		
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		CompleteStepHelper competeHelper = null;
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();
			
			if(null != pimCtx)
			{
				
				wattsLogger = pimCtx.getLogger("com.ibm.ccd.wpc_user_scripting.Watts_WorflowFunctions");
				wattsLogger.logInfo("----Logger Object Retrieved successfully :COMPLETE:Workflow_IN-----");
				
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
				
				
				wattsLogger.logInfo("-:COMPLETE:----------------Print the Step Objects-------------------");
				//wattsLogger.logInfo("-:COMPLETE:-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:COMPLETE:-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:COMPLETE:-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:COMPLETE:---------------------------------------------------------");
				
				
				competeHelper = new CompleteStepHelper();
				
				String exit_val = "DONE";
				
				// Setting exit values
				for (CollaborationItem collabItem : collabItemList) 
				{
					String partNumber = null;
					partNumber = (null !=collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"") ;
					wattsLogger.logInfo("-:COMPLETE:--------------ITEM PRIMARY_KEY------------::"+collabItem.getPrimaryKey());
					wattsLogger.logInfo("-:COMPLETE:--------------ITEM PART_NUMBER------------::"+partNumber);
					competeHelper.setEntryAttributesForStep(collabItem, pimCtx, wattsLogger);
					colConfig.setExitValue(collabItem, wrkflowStepObj.createExitValue(exit_val));
					wattsLogger.logInfo(partNumber +" GDS Status : ");
					wattsLogger.logInfo(collabItem.getAttributeValue("Product Catalog Global Spec/Packaging Hierarchy/Each Level/GDS Status"));
				}
				wattsLogger.logInfo("-:COMPLETE:---------------------------------------------------------");
				
			}else
			{
				System.out.println("-:COMPLETE:--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:COMPLETE:----Exception---"+e.getMessage());
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
