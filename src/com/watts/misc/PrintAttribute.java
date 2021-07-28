package com.watts.misc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;

public class PrintAttribute implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		PrintWriter out = arg0.getOutput();
		BufferedReader br = arg0.getInput();
		Context ctx= PIMContextFactory.getCurrentContext();
		try{
			out.println("Method Called : [OK]");
			Catalog productCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
		    Item itemObj = productCatalog.getItemByPrimaryKey("5");
		    ItemCollaborationArea itmArea = (ItemCollaborationArea)ctx.getCollaborationAreaManager().getCollaborationArea("QAD Parts Maintenance Staging Area");
		    CollaborationItem cItem = itmArea.getCheckedOutItem("5");
		    String AttrPath = br.readLine();
		    if (null != itemObj){
		    	out.println(itemObj.getAttributeValue(AttrPath));
		    }
		    if (null != cItem){
		    	out.println(cItem.getAttributeValue(AttrPath));
		    }
		 
		}catch(Exception e){
			e.printStackTrace(out);
		}
		
	}

}
