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
import com.ww.pim.email.GenerateNotification;

public class ProductMaintenanceManagement implements WorkflowStepFunction 
{

	public ProductMaintenanceManagement() {
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
				wattsLogger.logInfo("----Logger Object Retrieved successfully :PM MAINTENANCE :Workflow_IN-----");
				
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
				
				
				wattsLogger.logInfo("-:PM MAINTENANCE :----------------Print the Step Objects-------------------");
				//wattsLogger.logInfo("-:PM MAINTENANCE :-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:PM MAINTENANCE :-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:PM MAINTENANCE :-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:PM MAINTENANCE :---------------------------------------------------------");
				
				String exit_val = "DONE";
				
				// Setting exit values
				for (CollaborationItem collaborationItem : collabItemList) 
				{
					wattsLogger.logInfo("-:PM MAINTENANCE :--------------ITEM PRIMARY_KEY------------::"+collaborationItem.getPrimaryKey());
					colConfig.setExitValue(collaborationItem, wrkflowStepObj.createExitValue(exit_val));
					
				}
				wattsLogger.logInfo("-:PM MAINTENANCE :---------------------------------------------------------");
				
				
			}else
			{
				System.out.println("-:PM MAINTENANCE :--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:PM MAINTENANCE :----Exception---"+e.getMessage());
			}
		
		}

	}

	@Override
	public void out(WorkflowStepFunctionArguments arg0) 
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
				
				wattsLogger = pimCtx.getLogger("com.ibm.ccd.wpc_user_scripting.Watts_WorflowFunctions");
				wattsLogger.logInfo("----Logger Object Retrieved successfully :PM MAINTENANCE :Workflow_OUT-----");
				
				
				PIMCollection<CollaborationItem> collabItemList = arg0.getItems();
				String collabAreaName = arg0.getCollaborationStep().getCollaborationArea().getName();
				String workflowStepName =arg0.getCollaborationStep().getWorkflowStep().getName();
				
				
				wattsLogger.logInfo("-:PM MAINTENANCE:workflow_out :----------------Print the Step Objects-------------------");
				//wattsLogger.logInfo("-:PM MAINTENANCE :-::Collaboration Step Name :: "+collabStepName);
				wattsLogger.logInfo("-:PM MAINTENANCE :workflow_out :-::Collaboration Area Name :: "+collabAreaName);
				wattsLogger.logInfo("-:PM MAINTENANCE :workflow_out :-::Worklow Step Name       :: "+workflowStepName);
				wattsLogger.logInfo("-:PM MAINTENANCE:workflow_out  :---------------------------------------------------------");
				
				
				// Populate external table in PM Maintenance Step with email receipient list to notify ENG
				for (CollaborationItem collabItem : collabItemList) 
				{
					wattsLogger.logInfo("-:PM MAINTENANCE:workflow_out  :--------------ITEM PRIMARY_KEY------------::"+collabItem.getPrimaryKey());
					String partNumber = (null !=collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"") ;
					String sysOfRec = (null !=collabItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH)?collabItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH).toString():"") ;
					String emailRec = (null !=collabItem.getAttributeValue(WattsConstants.EMAIL_RECEIPIENTS_PATH)?collabItem.getAttributeValue(WattsConstants.EMAIL_RECEIPIENTS_PATH).toString():"") ;
					if(!"".equals(emailRec))
					{
						new GenerateNotification().insertRecordMailNotificationEngineering(partNumber, sysOfRec, emailRec);
					}
					
				}
				wattsLogger.logInfo("-:PM MAINTENANCE :workflow_out :---------------------------------------------------------");
				
				
			}else
			{
				System.out.println("-:PM MAINTENANCE:workflow_out  :--------The Context is not retrieved properly-------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:PM MAINTENANCE:workflow_out  :----Exception---"+e.getMessage());
			}
		
		}
		

	}

	@Override
	public void timeout(WorkflowStepFunctionArguments arg0) {
		// TODO Auto-generated method stub

	}

}
