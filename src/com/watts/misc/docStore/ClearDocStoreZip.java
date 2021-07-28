package com.watts.misc.docStore;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.docstore.Directory;
import com.ibm.pim.docstore.DocstoreManager;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunction;
import com.ibm.pim.extensionpoints.ScriptingSandboxFunctionArguments;

public class ClearDocStoreZip implements ScriptingSandboxFunction{

	@Override
	public void scriptingSandbox(ScriptingSandboxFunctionArguments arg0) {
		PrintWriter out = arg0.getOutput();
		ArrayList<String> baseDirs = new ArrayList<String>();
		baseDirs.add("/IBM_MDMPIM_DocStore_Maintenance/Archives");
		Context ctx = PIMContextFactory.getCurrentContext();
		DocstoreManager dm = null; 
		try{
			dm = ctx.getDocstoreManager();
			for (String baseDirPath : baseDirs){
				out.println("Attempting to delete SubDir Contents of : "+baseDirPath);
				Directory directory1 = dm.getDirectory(baseDirPath);
				Collection<Directory> subDirs = directory1.getSubDirectories();
				for(Directory subDir :  subDirs){
					String line1 = subDir.getPath();
					String inter = line1.split("/")[3];
					out.println(baseDirPath+"/"+inter);
					Directory directory = dm.getDirectory(baseDirPath+"/"+inter);
					out.println(directory);
					PIMCollection<Document> docs = directory.getDocuments();
					for (Document doc : docs){
						out.println(doc.getName());
						doc.delete();
					}
				}
				out.println("All Docs Deleted of "+baseDirPath);
			}
		}catch(Exception e){
			e.printStackTrace(out);
		}
		
	}

}
