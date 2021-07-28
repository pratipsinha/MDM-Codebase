package com.ww.pim.email;

import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;

public class PMNotificationReport implements ReportGenerateFunction{

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		GenerateNotification generateNotification = new GenerateNotification();
		generateNotification.sendPMNotification();
		
	}

}
