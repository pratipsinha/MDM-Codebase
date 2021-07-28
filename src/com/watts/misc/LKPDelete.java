package com.watts.misc;

import java.io.PrintWriter;

import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;
import com.ibm.pim.lookuptable.LookupTable;

public class LKPDelete implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		PrintWriter pw = arg0.getOutput();
		Context ctx= PIMContextFactory.getCurrentContext();
		try{
			String LkpName = (String)arg0.getInput().readLine();
			LookupTable lkpTable = ctx.getLookupTableManager().getLookupTable(LkpName);
			lkpTable.deleteAsynchronous();
			ctx.commit();
			pw.println("table deleted");
		}catch(Exception exp){
			exp.printStackTrace(pw);
		}
		
	}

}
