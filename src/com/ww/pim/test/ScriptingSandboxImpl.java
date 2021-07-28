package com.ww.pim.test;

import java.io.PrintWriter;
import java.util.Iterator;

import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;

public class ScriptingSandboxImpl implements ScriptingSandboxFunction {

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		// TODO Auto-generated method stub

		Context ctx = PIMContextFactory.getCurrentContext();
		PrintWriter pw = arg0.getOutput();
		pw.write("ScriptingSandboxImpl started..");try {
			Catalog objCtg = ctx.getCatalogManager().getCatalog("Product Catalog");
			Item objItem= objCtg.getItemByPrimaryKey("14");
			if(null != objItem)
			{
				Iterator<? extends AttributeInstance> iterInst =objItem.getAttributeInstance("Product Catalog Global Spec/Associations").getChildren().iterator();
				while(iterInst.hasNext())
				{
					AttributeInstance attribIns = iterInst.next();
					pw.write("\n attribIns--"+attribIns.getPath());
					String typePath = objItem.getAttributeInstance(attribIns.getPath()+"/Type").getPath();
					String associatedParts = objItem.getAttributeInstance(attribIns.getPath()+"/AssociatedParts").getPath();
					pw.write("\n typePath--"+typePath+"  associatedParts_---"+associatedParts);
					if(null != objItem.getAttributeInstance(attribIns.getPath()+"/AssociatedParts"))
					{
						pw.write("\n"+objItem.getAttributeInstance(attribIns.getPath()+"/AssociatedParts#1").getValue());
					}
				}
			}

		}

		catch(Exception ex)
		{
			pw.write("\n exception caught.."+ex);
		}
	}
}
