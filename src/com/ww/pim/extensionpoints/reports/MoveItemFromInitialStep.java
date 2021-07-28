package com.ww.pim.extensionpoints.reports;

import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationObject;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;



public class MoveItemFromInitialStep implements ReportGenerateFunction {


	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		// TODO Auto-generated method stub

		Context ctx = PIMContextFactory.getCurrentContext();
		Logger objLogger = ctx.getLogger(Constant.LOGGER_NAME);
		objLogger.logDebug("MoveItemFromInitialStep Report started..");
		try {
			//objLogger.logDebug("Within MoveItemFromInitialStep method...");
			ItemCollaborationArea itemColArea = (ItemCollaborationArea)ctx.getCollaborationAreaManager().getCollaborationArea(Constant.QADPARTSMAINTCOLAREANAME);
			CollaborationStep colStep = itemColArea.getStep("Initial");
		//	objLogger.logDebug("ColStep===>"+colStep.getContents().size());

			for(CollaborationObject colObj : colStep.getContents())
			{
				CollaborationItem colItem = (CollaborationItem)colObj;
				//objLogger.logDebug("colItem--"+colItem.getPrimaryKey());
			//	objLogger.logDebug("Moving Item to next step..");
				itemColArea.moveToNextStep(colItem, colStep, "SUCCESS");
			//	objLogger.logDebug("Item moved to next step..");
			}

		}

		catch(Exception ex)
		{
			objLogger.logDebug("Within MoveItemFromInitialStep method..exception occurred.."+ex);
		}
		finally
		{
			objLogger.logDebug("MoveItemFromInitialStep Report ended..");
		}
	}

}
