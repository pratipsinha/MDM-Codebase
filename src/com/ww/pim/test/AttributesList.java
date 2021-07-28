package com.ww.pim.test;

import java.util.List;

import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;

public class AttributesList implements ReportGenerateFunction {

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		// TODO Auto-generated method stub
		
		Context ctx = PIMContextFactory.getCurrentContext();
		Logger objLogger = ctx.getLogger(Constant.LOGGER_NAME);
		Spec objSpec = ctx.getSpecManager().getSpec("Product Catalog Global Spec");
	List<AttributeDefinition> attrDefList=objSpec.getAttributeDefinitions();
	for(AttributeDefinition attrDef : attrDefList)
	{
		//if(attrDef.getType().equal)
		//attrDef.getProperty(AttributeDefin)

	}
		
	}

}
