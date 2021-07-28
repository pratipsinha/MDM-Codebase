package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.util.Map;

import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;
import com.ww.pim.common.utils.UtilityMethods;


public class SendMailReport implements ReportGenerateFunction {

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) 
	{
		Logger logger = null;
		UtilityMethods helperObj = null;
		File[] files = null;
		try 
		{
			helperObj = new UtilityMethods();
			Context pimCtx = PIMContextFactory.getCurrentContext();
			Map inputMap = arg0.getInputs();
			String environment = inputMap.get("environment").toString();
			boolean status = false;
			if(null != pimCtx)
			{
				logger = pimCtx.getLogger(Constant.REPORT_LOGGER_NAME);
				logger.logInfo("----Logger Found :SEND EMAIL TO USERS:REPORT JOB-----");
				files = helperObj.listAllQADFiles(inputMap.get("dirpath").toString(),logger);
				if (null != files && files.length > 0){
					//for(File eachfile : files)
					//logger.logInfo(eachfile.getName());
					status = helperObj.sendEmailToUser(files,environment,logger);
				}
				if (status){
					logger.logInfo("Email Status : Mail Sent");
					helperObj.deleteAllFiles(inputMap.get("dirpath").toString());
					logger.logInfo("All files Deleted");
				}
				
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			if(null != logger)
			{
				logger.logInfo("-:SEND EMAIL TO USERS REPORT:----Exception---"+e.getMessage());
			}
		}

	}

}
