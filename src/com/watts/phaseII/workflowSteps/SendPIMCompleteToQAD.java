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
import com.watts.phaseII.helper.SendPIMCompleteHelper;

public class SendPIMCompleteToQAD implements WorkflowStepFunction 
{

	public SendPIMCompleteToQAD() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void in(WorkflowStepFunctionArguments arg0) 
	{
		// TODO Auto-generated method stub
		
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		SendPIMCompleteHelper pimCompleteHelpObj = null;
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();
			
			if(null != pimCtx)
			{
				//ProductManagementStepHelper helperObj = new ProductManagementStepHelper();
				wattsLogger = pimCtx.getLogger("com.ibm.ccd.wpc_user_scripting.Watts_WorflowFunctions");
				wattsLogger.logInfo("----Logger Object Retrieved successfully :SEND_PIMCOMPLETE_QAD:Workflow_IN-----");
				
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
				
				
				wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:----------------Print the Step Objects-------------------");
				//wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:---------------------------------------------------------");
				
				String exit_val = "DONE";
				pimCompleteHelpObj = new SendPIMCompleteHelper();
				
				// Setting exit values
				for (CollaborationItem collabItem : collabItemList) 
				{
					String partNumber = null;
					partNumber = (null !=collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"") ;
					wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:--------------ITEM PRIMARY_KEY------------::"+collabItem.getPrimaryKey());
					wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:--------------ITEM PART_NUMBER------------::"+partNumber);
					pimCompleteHelpObj.setCollabEntryAttribs(collabItem, pimCtx, wattsLogger);
					colConfig.setExitValue(collabItem, wrkflowStepObj.createExitValue(exit_val));
					
				}
				//Generate outbound file having PIMComplete status as "Yes" to be send to ERP
				if(null != collabItemList && collabItemList.size() >0)
				{
					pimCompleteHelpObj.generateOutFeedForERP(collabItemList, pimCtx, wattsLogger);
				}
				//
				wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:---------------------------------------------------------");
				
			}else
			{
				System.out.println("-:SEND_PIMCOMPLETE_QAD:--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:----Exception---"+e.getMessage());
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
