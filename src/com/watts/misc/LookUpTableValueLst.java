package com.watts.misc;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;

import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;
import com.ibm.pim.lookuptable.LookupTable;
import com.ibm.pim.lookuptable.LookupTableEntry;

public class LookUpTableValueLst implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		PrintWriter pw = arg0.getOutput();
		Context ctx = null;
		StringBuffer header = new StringBuffer("");
		StringBuffer content = new StringBuffer("");
		Document exportDoc = null;
		try{
			String lookUpTableName = arg0.getInput().readLine();
			ctx = PIMContextFactory.getCurrentContext();
			Date dateObj = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			LookupTable lkp = ctx.getLookupTableManager().getLookupTable(lookUpTableName);
			exportDoc = ctx.getDocstoreManager().createAndPersistDocument("/Exports/LookupTable/"+lookUpTableName+"_"+sdf.format(dateObj)+".txt");
			
			PIMCollection<LookupTableEntry>lkpEntries = lkp.getLookupTableEntries();
			List<AttributeDefinition> listAttrdefs = lkp.getSpec().getAttributeDefinitions();
			boolean firstHeader = true;
			boolean firstContent = true;
			for(AttributeDefinition attrDef: listAttrdefs){
				
					header.append(attrDef.getName());
					header.append("|");
					
				
			}
			for (LookupTableEntry lkpEntry : lkpEntries){
				content.append("\n");
				for(AttributeDefinition attrDef: listAttrdefs){
					
						
						content.append(lkpEntry.getAttributeValue(attrDef.getPath()));
						content.append("|");
					
					
				}
				
			}
			exportDoc.setContent(header.toString()+content.toString());
			pw.println("Processing complete");
		}catch(Exception exp){
			exp.printStackTrace(pw);
		}
		
	}
	
}
