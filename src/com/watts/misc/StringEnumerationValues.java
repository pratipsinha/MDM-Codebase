package com.watts.misc;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Set;

import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeDefinition.Type;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;
import com.ibm.pim.spec.Spec;


public class StringEnumerationValues implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		Context ctx = null;
		BufferedReader br = arg0.getInput();
		PrintWriter pw = arg0.getOutput();
		
		try{
			ctx = PIMContextFactory.getCurrentContext();
			
			String line ="";
			Item itemObj = ctx.getCatalogManager().getCatalog("Product Catalog").getItemByPrimaryKey("4032");
			while ((line = br.readLine()) != null){
				pw.println("==============================================================");
				pw.println("********************"+line+"*******************");
				AttributeInstance attrIns = itemObj.getAttributeInstance(line);
				Set<String> possibleValues = attrIns.getPossibleValues();
				
				for (String value : possibleValues){
					pw.println(value);
				}
			}
			
			pw.println("Processing Completed");
			Spec spec = ctx.getSpecManager().getSpec("Product Catalog Global Spec");
			pw.println("Processing Again Started");
			AttributeDefinition ad = spec.getAttributeDefinition("Product Catalog Global Spec/Associations/Type");
			Type[] as = ad.getType().values();
			pw.println("1");
			/*if (null != ad){
				pw.println(ad.getPath());
				ad.get
			}*/
		}catch(Exception exp){
			//pw.println("Exception"+exp.get);
			exp.printStackTrace(pw);
		}
		
	}

}
