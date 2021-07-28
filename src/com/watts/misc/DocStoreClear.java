package com.watts.misc;

import java.io.PrintWriter;
import java.util.Collection;

import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.docstore.Directory;
import com.ibm.pim.docstore.DocstoreManager;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;

public class DocStoreClear implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		PrintWriter out = arg0.getOutput();
		Context ctx = null;
		try{
			ctx = PIMContextFactory.getCurrentContext();
			DocstoreManager dm = ctx.getDocstoreManager();
			String line = arg0.getInput().readLine();
			Directory directory1 = dm.getDirectory("/Exports");
			Collection<Directory> subDirs = directory1.getSubDirectories();
			for(Directory subDir :  subDirs){
				String line1 = subDir.getPath();
				String inter = line1.split("/")[2];
				Directory directory = dm.getDirectory("/Exports/"+inter+"/MDM_PIM Export to USQAD");
				out.println(directory);
				PIMCollection<Document> docs = directory.getDocuments();
				for (Document doc : docs){
					out.println(doc.getName());
					doc.delete();
				}
			}
			/**/
		}catch(Exception exp){
			out.println("Exception");
			exp.printStackTrace(out);
		}
		
	}
	

}
