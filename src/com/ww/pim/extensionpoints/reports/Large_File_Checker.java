package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.LinkedHashMap;
import java.util.Map;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.common.utils.Large_File_Checker_Filter;
import com.ww.pim.email.SendEmail;

public class Large_File_Checker implements ReportGenerateFunction{
	private Context ctx = null;
	private Logger objLogger = null;
	int recordLimit =0;
	private LinkedHashMap<String, String> fileList= new LinkedHashMap<String, String>();
	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		try{
		ctx = PIMContextFactory.getCurrentContext();
		objLogger = ctx.getLogger(Constant.LOGGER_NAME);
		objLogger.logDebug("Large_File_Checker Invoked");
		
		Map inputMap = arg0.getInputs();
		
		if (null != inputMap){
			String recordValue = (String) inputMap.get("recordCount");
			objLogger.logDebug("recordLimit : " + recordValue);
			recordLimit =Integer.parseInt(recordValue);
		}
		objLogger.logDebug("recordLimit : " + recordLimit);
		listFiles();
		for (Map.Entry<String,String> entry : fileList.entrySet())  
			objLogger.logDebug("File name = " + entry.getKey() + 
                             ", Row Count = " + entry.getValue()); 
		if (fileList.size()> 0)
		sendLargeFileNotification();
		objLogger.logDebug("Large_File_Checker ended");
		}catch(Exception e){
			objLogger.logDebug(e.getMessage());
			objLogger.logDebug(e);
		}
	}

	private void listFiles() throws Exception{
		File dir = new File("/rpt1/WattsPrd/mdmpim/prod/outbound/archive/");
		objLogger.logDebug("Large_File_Checker Dir Path4 : "+dir.getAbsolutePath());
		Large_File_Checker_Filter filter = new Large_File_Checker_Filter();
		File[] files = dir.listFiles(filter);
		for (File txtfile : files) {
			objLogger.logDebug(txtfile.getName());
			int numRecords = countLines(txtfile);
			if (numRecords >   recordLimit){
				fileList.put(txtfile.getName(),Integer.toString(numRecords));
			}
		}
	}
	
	public int countLines(File file) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("wc", "-l", file.getAbsolutePath());
        Process process = builder.start();
        InputStream in = process.getInputStream();
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
        String line = reader.readLine();
        if (line != null) {
            return Integer.parseInt(line.trim().split(" ")[0]);
        } else {
            return -1;
        }
    }
	
	public void sendLargeFileNotification() {
		String current_pl = "";
		String mail_body = "";
		String table_initializer = "<table><tr><td>File Name</td><td>Number of Rows</td></tr>";
		try{
			for (Map.Entry<String,String> entry : fileList.entrySet()){  
				mail_body = mail_body + "<tr><td>"+entry.getKey()+"</td><td>"+entry.getValue()+"</td></tr>";
				}
			if(!mail_body.equals("")) {
				mail_body = table_initializer + mail_body + "</table>";
				SendEmail smail = new SendEmail();
				smail.sendMailCommon("Sinha.Kumar@wattswater.com", "pim@watts.com", mail_body, "Notification - Large Files Received from QAD");
			}
			
		}catch(Exception e){
			objLogger.logDebug(e.getMessage());
			e.printStackTrace();
		}
	}
}
