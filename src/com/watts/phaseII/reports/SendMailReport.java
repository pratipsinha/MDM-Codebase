package com.watts.phaseII.reports;

import com.ibm.pim.context.Context;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;
import com.watts.phaseII.helper.CommonHelper;

public class SendMailReport implements ReportGenerateFunction {

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) 
	{
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();

			if(null != pimCtx)
			{
				wattsLogger = pimCtx.getLogger(WattsConstants.LOGGER_NAME);
				wattsLogger.logInfo("----Logger Found :SEND EMAIL TO USERS:REPORT JOB-----");
				helperObj.sendMailForTemplateLoad(pimCtx, wattsLogger);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:SEND EMAIL TO USERS REPORT:----Exception---"+e.getMessage());
			}
		}

	}

}
