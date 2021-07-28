package com.watts.phaseII.imports;

import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.context.Context;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.extensionpoints.ImportFunction;
import com.ibm.pim.extensionpoints.ImportFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;
import com.watts.phaseII.helper.AttrDataTypeImportHelper;
import com.watts.phaseII.helper.CommonHelper;

public class AttributeDataTypeChangeImport implements ImportFunction 
{

	
	@Override
	public void doImport(ImportFunctionArguments arg0) 
	{
		Logger wattsLogger = null;
		CommonHelper helperObj = null;
		Catalog serviceCatalog = null;
		String docStoreFileName = null;
		Document document = null;
		AttrDataTypeImportHelper attrImportHelper = null;
		
		try 
		{
			helperObj = new CommonHelper();
			Context pimCtx = helperObj.getPIMContext();

			if(null != pimCtx)
			{
				wattsLogger = pimCtx.getLogger(WattsConstants.LOGGER_NAME);
				wattsLogger.logInfo("----Logger Object Retrieved successfully :Data Type Change Import:IMPORT JOB-----");
				serviceCatalog = arg0.getCatalog();
				if(null != serviceCatalog)
				{
					document = arg0.getDocstoreDataDoc();
					if(null != document && null != document.getContent())
					{
						docStoreFileName = document.getName();
						wattsLogger.logInfo("----Data Type Change Import:docStoreFileName ---::"+docStoreFileName);
						if(docStoreFileName.endsWith(".xlsx"))
						{
							attrImportHelper = new AttrDataTypeImportHelper();
							attrImportHelper.doExecuteImport(pimCtx,wattsLogger,serviceCatalog,document,docStoreFileName);
							
						}else
						{
							wattsLogger.logInfo("----Data Type Change Import:The Input document must be an excel file ending in .xlsx-----");
						}
						
					}else
					{
						wattsLogger.logInfo("----Data Type Change Import:No Input Document is provided or Empty file-----");
						
					}
				}else
				{
					wattsLogger.logInfo("----Data Type Change Import:Catalog is NULL-----");
				}
				
				wattsLogger.logInfo("----Data Type Change Import:End-----");
			}else
			{
				System.out.println("-:Data Type Change Import:--------The Context is not retrieved properly-------------");
				
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			if(null != wattsLogger)
			{
				wattsLogger.logInfo("-:Data Type Change Import:----Exception---"+e.getMessage());
			}
		}

	}

}
