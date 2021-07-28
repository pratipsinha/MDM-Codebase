package com.ww.pim.extensionpoints.reports;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.Constant;

public class SystemOfRecordsUpdateReport implements ReportGenerateFunction {
	
	private Context ctx = null;
	private Logger objLogger = null;
	private Catalog productCatalog = null;
	BufferedReader buffReader = null;

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		// TODO Auto-generated method stub
		
		ctx = PIMContextFactory.getCurrentContext();
		productCatalog =ctx.getCatalogManager().getCatalog(Constant.PRODUCT_CATALOG);
		objLogger = ctx.getLogger(Constant.LOGGER_NAME);
		objLogger.logDebug("SystemOfRecordsUpdateReport started. ");
		String line;
		int counter=0;
		try {
			buffReader = new BufferedReader(new FileReader("/opt/IBM/MDMCSv115/SysOfRecUpdate/1.txt"));
			while((line =buffReader.readLine()) != null) // Iterating through each record of feed file
			{
				counter++;
				objLogger.logDebug("line---"+line);
				String dataArray[]= {};
				Item objItem = null;
				dataArray=line.split("\\|",-1);
				objLogger.logDebug("dataArray[0]==="+dataArray[0]+" dataArray[1]"+dataArray[1]);
				objItem = productCatalog.getItemByPrimaryKey(dataArray[0]);
				if(objItem != null)
				{
					objItem.setAttributeValue("Product Catalog Global Spec/SystemOfRecord",dataArray[1]);
					objItem.save();
				}
				objLogger.logDebug("Item "+counter+" processed.");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			objLogger.logDebug("SystemOfRecordsUpdateReport ended...");
		}
		
	}

}
