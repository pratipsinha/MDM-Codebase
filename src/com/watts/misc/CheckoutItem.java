package com.watts.misc;

import java.io.PrintWriter;

import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CheckoutStatus;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;


public class CheckoutItem implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		PrintWriter pw = arg0.getOutput();
		Context ctx = null;
		try{
			String pim_id = arg0.getInput().readLine();
			ctx = PIMContextFactory.getCurrentContext();
			Item item = ctx.getCatalogManager().getCatalog("Product Catalog").getItemByPrimaryKey(pim_id);
			ItemCollaborationArea collabArea = (ItemCollaborationArea) ctx.getCollaborationAreaManager().getCollaborationArea("Item Maintenance Staging Area");
			CollaborationStep collabStep = collabArea.getStep("Engineering Maintenance");
			CollaborationStep collabStep1 = collabArea.getStep("Marcom Product Data Services Maintenance");
			CollaborationStep collabStep2 = collabArea.getStep("Product Management Maintenance");
			CheckoutStatus chkStatus = collabArea.checkoutAndWaitForStatus(item, collabStep);
			CheckoutStatus chkStatus1 = collabArea.checkoutAndWaitForStatus(item, collabStep1);
			CheckoutStatus chkStatus2 = collabArea.checkoutAndWaitForStatus(item, collabStep2);
			pw.println("Done");
			pw.println(chkStatus);
			pw.println(chkStatus1);
			pw.println(chkStatus2);
			
		}catch(Exception exp){
			exp.printStackTrace(pw);
		}
		
	}

}
