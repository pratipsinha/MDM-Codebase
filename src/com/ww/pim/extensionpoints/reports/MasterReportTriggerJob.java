package com.ww.pim.extensionpoints.reports;

import java.util.Map;

import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.job.JobManager;
import com.ibm.pim.job.Report;
import com.ibm.pim.job.Schedule;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;

//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.extensionpoints.reports.MasterReportTriggerJob.class"
//Mass_Export_1,Mass_Export_2,Mass_Export_2

public class MasterReportTriggerJob implements ReportGenerateFunction {

	Context ctx = PIMContextFactory.getCurrentContext();
	Logger oLog = ctx.getLogger(Constant.REPORT_LOGGER_NAME);

	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		// TODO Auto-generated method stub

		oLog.logInfo("Within MasterReportTriggerJob report");
		try
		{
			Map<String, String> inputList = arg0.getInputs();
			String reportNames = inputList.get("report_names");
			String[] reportNameList = {};
			if(!"".equals(reportNames)){

				JobManager objManager = ctx.getJobManager();
				Schedule scheduledReport = null;
				Report objReport = null;
				if(reportNames.contains(","))
				{
					reportNameList =  reportNames.split("\\,");
				}
				else
				{
					reportNameList=new String[] {reportNames};
				}

				for(String reportName : reportNameList)
				{
					objReport = objManager.getReport(reportName);
					if(null != objReport)
					{
						scheduledReport = objManager.createSchedule(objReport);
						oLog.logInfo("child report "+reportName+" started....");
					}
					else
					{
						oLog.logInfo("Report "+reportName+" does not exist...");
					}
				}
			}
			else
			{
				oLog.logInfo("Please provide value in input parameter..");
			}
		}
		catch(Exception ex)
		{
			oLog.logInfo("Within MasterReportTriggerJob report..exception occurred.."+ex);
		}



	}

}
