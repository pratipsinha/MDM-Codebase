package com.watts.phaseII.imports;

import java.io.PrintWriter;

import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.context.Context;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ImportFunction;
import com.ibm.pim.extensionpoints.ImportFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;
import com.watts.phaseII.helper.CommonHelper;
import com.watts.phaseII.helper.MassImportTemplateHelper;

public class MassUpdateImportTemplateAttributes implements ImportFunction 
{

	
	@Override
	public void doImport(ImportFunctionArguments arg0) 
	{
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		Catalog serviceCatalog = null;
		String docStoreFileName = null;
		Document document = null;
		MassImportTemplateHelper massImpHelperObj = null;
		PrintWriter out = null;
		try 
		{
			out = arg0.getOutput();
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();

			if(null != pimCtx)
			{
				
				wattsLogger = pimCtx.getLogger(WattsConstants.LOGGER_NAME);
				wattsLogger.logInfo("----Logger Object Retrieved successfully :TEMPLATE MASS IMPORT:IMPORT JOB-----");
				serviceCatalog = arg0.getCatalog();
				if(null != serviceCatalog)
				{
					document = arg0.getDocstoreDataDoc();
					if(null != document && null != document.getContent())
					{
						docStoreFileName = document.getName();
						wattsLogger.logInfo("----TEMPLATE MASS IMPORT:docStoreFileName ---::"+docStoreFileName);
						if(docStoreFileName.endsWith(".xlsx"))
						{
							massImpHelperObj = new MassImportTemplateHelper();
							massImpHelperObj.executeMassImportForTemplates(pimCtx,wattsLogger,serviceCatalog,document,docStoreFileName,out);
							
						}else
						{
							wattsLogger.logInfo("----TEMPLATE MASS IMPORT:The Input document must be an excel file ending in .xlsx-----");
						}
						
					}else
					{
						wattsLogger.logInfo("----TEMPLATE MASS IMPORT:No Input Document is provided or Empty file-----");
						
					}
				}else
				{
					wattsLogger.logInfo("----TEMPLATE MASS IMPORT:Catalog is NULL-----");
				}
				
				wattsLogger.logInfo("----TEMPLATE MASS IMPORT:End-----");
			}else
			{
				System.out.println("-:TEMPLATE MASS IMPORT:--------The Context is not retrieved properly-------------");
				
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:TEMPLATE MASS IMPORT:----Exception---"+e.getMessage());
			}
		}

	
		
	}

}
