package com.ww.pim.test;

import java.io.BufferedReader;
import java.util.List;

import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;
import com.ibm.pim.spec.Spec;

public class AttributePathFinder implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		BufferedReader br = arg0.getInput();
		Context ctx = null;
		try{
			String line;
			ctx = PIMContextFactory.getCurrentContext();
			Spec spec = ctx.getSpecManager().getSpec("Product Catalog Global Spec");
			List<AttributeDefinition> attrs = spec.getAttributeDefinitions();
			
			while((line = br.readLine()) != null){
				for (AttributeDefinition attrDef : attrs){
					if (attrDef.getPath().contains(line)){
						arg0.getOutput().println(attrDef.getPath());
					}
				}
			}
		}catch(Exception exp){
			exp.printStackTrace(arg0.getOutput());
		}
		
	}

}
