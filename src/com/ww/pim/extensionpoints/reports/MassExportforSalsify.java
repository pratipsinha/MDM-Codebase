//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.extensionpoints.reports.MassExportforSalsify.class"
package com.ww.pim.extensionpoints.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.pim.attribute.AttributeCollection;
import com.ibm.pim.attribute.AttributeCollectionManager;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ww.pim.common.utils.Constant;

public class MassExportforSalsify implements ReportGenerateFunction{

	private Context ctx = PIMContextFactory.getCurrentContext();
	private PrintWriter objLogger = null;
	private Catalog objProductCatalog = ctx.getCatalogManager().getCatalog("Product Catalog");
	private XSSFWorkbook wb = new XSSFWorkbook();
	private String outputFileName = System.getProperty(Constant.DOLLAR_TOP)+"/Output Files/Mass_Export/Template_Attribute_Export/MassExport_Salsify"+new Date().toString()+".xlsx";
	private String logFileName = System.getProperty(Constant.DOLLAR_TOP)+"/logs/Mass_Export_Salsifylog.txt";
	private FileOutputStream outputStream = null;
	AttributeCollectionManager acm = ctx.getAttributeCollectionManager();

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		
		try{
			File logFile = new File(logFileName);
			outputStream = new FileOutputStream(outputFileName);
			try 
			{
				objLogger = new PrintWriter(new FileWriter(logFile, true));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}  

			objLogger.println("*******Mass Export Salsify Report Started**********");
			AttributeCollection coreAttrs = acm.getAttributeCollection("Core Attribute Group");
			Collection<AttributeDefinition> colAd = coreAttrs.getAttributes();
			for (AttributeDefinition ad : colAd){
				objLogger.println(ad.getName() + "    "+ad.getPath());
			}
		}
		catch(Exception e)
		{
			objLogger.println("*******Error found while running the report**********");
			e.printStackTrace(objLogger);
		}
		
		
		
		
		finally
		{
			try 
			{
				wb.write(outputStream);
				outputStream.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			objLogger.close();
		}
	}

}
