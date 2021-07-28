package com.watts.phaseII.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.context.Context;
import com.ibm.pim.docstore.DocstoreManager;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;

public class SendPIMCompleteHelper 
{
	public void setCollabEntryAttribs(CollaborationItem collabItem,Context pimCtx,Logger wattsLogger)
	{
		wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-----------------------SendPIMCompleteHelper.setCollabEntryAttribs() method START-----"); 
		try 
		{
			
			
			String itemPk = collabItem.getPrimaryKey();
			Item catalogItem = pimCtx.getCatalogManager().getCatalog(WattsConstants.MAIN_CATALOG).getItemByPrimaryKey(itemPk);
			
			//Set PIMComplete status to true for the collab item
			//collabItem.setAttributeValue(WattsConstants.PIM_COMPLETE_PATH, "Yes");
			//
			
			if(null != catalogItem)
			{
				catalogItem.setAttributeValue(WattsConstants.PIM_COMPLETE_PATH, "Yes");
				try 
				{
					//collabItem.getCollaborationArea().getProcessingOptions().setAllProcessingOptions(false);
					ExtendedValidationErrors itemErrs =  catalogItem.save();
					if(null != itemErrs)
					{
						wattsLogger.logInfo("**:SEND_PIMCOMPLETE_QAD:*********ERROR------while saving Catalog Item...************");
						wattsLogger.logInfo("**:SEND_PIMCOMPLETE_QAD:*************************************************");
						List<ValidationError> objValidationError = itemErrs.getErrors();
						if (null != objValidationError)
						{
							for (Iterator iterator2 = objValidationError.iterator(); iterator2.hasNext();) {
								ValidationError validationError = (ValidationError) iterator2.next();
								wattsLogger.logInfo(":SEND_PIMCOMPLETE_QAD:Error :: " + validationError.getMessage());
							}
						}
					}else
					{
						wattsLogger.logInfo("*:SEND_PIMCOMPLETE_QAD:**********SUCCESS------Catalog Item Saved************");
					}
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-Exception while saving collab item::-"+e.getMessage());
				}
				
			}
			
			
			try 
			{
				//collabItem.getCollaborationArea().getProcessingOptions().setAllProcessingOptions(false);
				ExtendedValidationErrors errs =  collabItem.save();
				if(null != errs)
				{
					wattsLogger.logInfo("**:SEND_PIMCOMPLETE_QAD:*********ERROR------while saving Collab Item...************");
					wattsLogger.logInfo("**:SEND_PIMCOMPLETE_QAD:*************************************************");
					List<ValidationError> objValidationError = errs.getErrors();
					if (null != objValidationError)
					{
						for (Iterator iterator2 = objValidationError.iterator(); iterator2.hasNext();) {
							ValidationError validationError = (ValidationError) iterator2.next();
							wattsLogger.logInfo(":SEND_PIMCOMPLETE_QAD:Error :: " + validationError.getMessage());
						}
					}
				}else
				{
					wattsLogger.logInfo("*:SEND_PIMCOMPLETE_QAD:**********SUCCESS------Collab Item Saved************");
				}
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-Exception while saving collab item::-"+e.getMessage());
			}
			
		} catch (Exception e) 
		{
			// TODO: handle exception
			wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:--Exception Occurred::--"+e.getMessage());
		}
		wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-----------------------SendPIMCompleteHelper.setCollabEntryAttribs() method END-----");
		
	}
	
	
	
	public void generateOutFeedForERP(PIMCollection<CollaborationItem> collabItemList,Context pimCtx,Logger wattsLogger)
	{
		wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-----------------------SendPIMCompleteHelper.generateOutFeedForERP() method START-----");
		String companyCode = pimCtx.getCurrentUser().getCompany().getName();
		companyCode = "watts";
		StringBuffer sb = new StringBuffer();
		Date dateObj = new Date();
		String strDateValue = new SimpleDateFormat("yyyyMMdd_HHmm_ss_SSS").format(dateObj).toString();
		String date = new SimpleDateFormat("yyMMdd").format(dateObj).toString();
		String fileName = WattsConstants.TO_ERP_DOCSTORE_FILE_PATH+"MDMPIMtoQAD_"+companyCode+"_"+strDateValue+".txt";
		wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:---Outbound Feed File Name---::"+fileName);
		wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:---Start Writing StringBuffer for the Feed File as Below:::");
		sb.append("Part Number|PIMComplete");
		wattsLogger.logInfo("Part Number|PIMComplete");
		sb.append("\r\n");
		try 
		{
			//Write data into the StringBuffer
			for (CollaborationItem collabItem : collabItemList) 
			{
				String time = new SimpleDateFormat("HHmm").format(dateObj).toString();
				String pim_ID = collabItem.getPrimaryKey();
				String partNumber = (null !=collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH)?collabItem.getAttributeValue(WattsConstants.PART_NUMBER_PATH).toString():"") ;
				String systemOfRecord = (null !=collabItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH)?collabItem.getAttributeValue(WattsConstants.SYSTEM_OF_RECORD_PATH).toString():"") ;
				String pimCompleteStatus = (null !=collabItem.getAttributeValue(WattsConstants.PIM_COMPLETE_PATH)?collabItem.getAttributeValue(WattsConstants.PIM_COMPLETE_PATH).toString():"Yes") ;
				if("No".equalsIgnoreCase(pimCompleteStatus))
				{
					pimCompleteStatus = "Yes";
				}
				sb.append(partNumber+"|"+pimCompleteStatus);
				sb.append("\r\n");
				wattsLogger.logInfo(partNumber+"|"+pimCompleteStatus);
				
			}
			wattsLogger.logInfo("End writing String Buffer...........");
			//Generate Feed File in docstore location
			new CommonHelper().saveFileToDocstore(pimCtx, fileName, sb, wattsLogger);
			//
			
		} catch(Exception e)
		{
			wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:--Exception Occurred::--"+e.getMessage());
		}
			
		wattsLogger.logInfo("-:SEND_PIMCOMPLETE_QAD:-----------------------SendPIMCompleteHelper.generateOutFeedForERP() method END-----");
	}
	
	
	
	
	
	

}
