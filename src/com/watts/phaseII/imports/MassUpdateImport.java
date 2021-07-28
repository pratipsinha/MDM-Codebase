package com.watts.phaseII.imports;

import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.context.Context;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ImportFunction;
import com.ibm.pim.extensionpoints.ImportFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;
import com.watts.phaseII.helper.CommonHelper;
import com.watts.phaseII.helper.MassUpdateImportHelper;

public class MassUpdateImport implements ImportFunction 
{
	@Override
	public void doImport(ImportFunctionArguments arg0) 
	{
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		Catalog serviceCatalog = null;
		String docStoreFileName = null;
		Document document = null;
		MassUpdateImportHelper massImpHelperObj = null;
		
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();

			if(null != pimCtx)
			{
				wattsLogger = pimCtx.getLogger(WattsConstants.LOGGER_NAME);
				wattsLogger.logInfo("----Logger Object Retrieved successfully :MASS UPDATE IMPORT:IMPORT JOB-----");
				serviceCatalog = arg0.getCatalog();
				if(null != serviceCatalog)
				{
					document = arg0.getDocstoreDataDoc();
					if(null != document && null != document.getContent())
					{
						docStoreFileName = document.getName();
						wattsLogger.logInfo("----MASS UPDATE IMPORT:docStoreFileName ---::"+docStoreFileName);
						if(docStoreFileName.endsWith(".xlsx"))
						{
							massImpHelperObj = new MassUpdateImportHelper();
							massImpHelperObj.doExecuteMassUpdateImport(pimCtx,wattsLogger,serviceCatalog,document,docStoreFileName);
							
						}else
						{
							wattsLogger.logInfo("----MASS UPDATE IMPORT:The Input document must be an excel file ending in .xlsx-----");
						}
						
					}else
					{
						wattsLogger.logInfo("----MASS UPDATE IMPORT:No Input Document is provided or Empty file-----");
						
					}
				}else
				{
					wattsLogger.logInfo("----MASS UPDATE IMPORT:Catalog is NULL-----");
				}
				
				wattsLogger.logInfo("----MASS UPDATE IMPORT:End-----");
			}else
			{
				System.out.println("-:MASS UPDATE IMPORT:--------The Context is not retrieved properly-------------");
				
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:MASS UPDATE IMPORT:----Exception---"+e.getMessage());
			}
		}

	
		
	}
	
	

}
