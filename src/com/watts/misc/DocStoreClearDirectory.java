package com.watts.misc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.docstore.Directory;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;

public class DocStoreClearDirectory implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		Context ctx= null;
		PrintWriter out = arg0.getOutput();
		BufferedReader br = arg0.getInput();
		try{
			ctx = PIMContextFactory.getCurrentContext();
			String path = br.readLine();
			Directory dr = ctx.getDocstoreManager().getDirectory(path);
			PIMCollection<Document> docs = dr.getDocuments();
			for (Document doc : docs){
				//out.println(doc.getName() +"----"+doc.getPath());
				doc.delete();
			}
			out.println(" All Deleted Done : " + path);
		}catch(Exception e){
			e.printStackTrace(out);
		}
		
	}

}
