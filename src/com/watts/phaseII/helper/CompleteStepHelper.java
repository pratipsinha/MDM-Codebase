package com.watts.phaseII.helper;


import java.util.Iterator;
import java.util.List;

import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.common.ExtendedValidationErrors;
import com.ibm.pim.common.ValidationError;
import com.ibm.pim.context.Context;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;


public class CompleteStepHelper 
{
	public void setEntryAttributesForStep(CollaborationItem collabItem,Context pimCtx,Logger wattsLogger)
	{
		wattsLogger.logInfo("-:COMPLETE:-----------------------CompleteStepHelper.setItemStepActors() method START-----"); 
		try 
		{
			String itemPk = collabItem.getPrimaryKey();
			Item catalogItem = pimCtx.getCatalogManager().getCatalog(WattsConstants.MAIN_CATALOG).getItemByPrimaryKey(itemPk);
			
			
			try 
			{
				//Set user role variable to blank once the item moves to complete state.
				//collabItem.setAttributeValue(WattsConstants.USER_ROLE_PATH, null);
				if(null != catalogItem)
				{
					catalogItem.setAttributeValue(WattsConstants.USER_ROLE_PATH, "");
					catalogItem.setAttributeValue(WattsConstants.ENTRY_PROCESSED_PATH, "false");
					catalogItem.setAttributeValue(WattsConstants.CHECKOUT_FLAG_PATH, false);
				}
				//
				
				//Set Entry_Processed Variable to false for the collab item
				//collabItem.setAttributeValue(WattsConstants.ENTRY_PROCESSED_PATH, "false");
				//
			} catch (Exception e1) 
			{
				// TODO Auto-generated catch block
				wattsLogger.logInfo("-:COMPLETE:------Exception while setting Attribute Value :: "+e1.getMessage());
			}
			
			
			//Set  to false at the end
			//collabItem.setAttributeValue(WattsConstants.CHECKOUT_FLAG_PATH, false);
			
			ExtendedValidationErrors collabItemErrs = null;
			ExtendedValidationErrors itemErrors = null;
			//Save both the catalog and collaboration item.
			try
			{
				//collabItem.getCollaborationArea().getProcessingOptions().setAllProcessingOptions(false);
				collabItemErrs = collabItem.save();
			}catch(Exception e)
			{
				wattsLogger.logInfo("-:COMPLETE:-Exception while saving collab item::-"+e.getMessage());
			}
			
			try
			{
				if(null != catalogItem)
				{
					//catalogItem.getCatalog().getProcessingOptions().setAllProcessingOptions(false);
					itemErrors = catalogItem.save();
				}
			}catch(Exception e)
			{
				wattsLogger.logInfo("-:COMPLETE:-Exception while saving Catalog item::-"+e.getMessage());
			}
			//
			
			if(null != collabItemErrs)
			{
				wattsLogger.logInfo("**:COMPLETE:*********ERROR------while saving Collab Item...************");
				wattsLogger.logInfo("**:COMPLETE:*************************************************");
				List<ValidationError> objValidationError = collabItemErrs.getErrors();
				if (null != objValidationError)
				{
					for (Iterator iterator2 = objValidationError.iterator(); iterator2.hasNext();) {
						ValidationError validationError = (ValidationError) iterator2.next();
						wattsLogger.logInfo(":COMPLETE:Error :: " + validationError.getMessage());
					}
				}
			}else
			{
				wattsLogger.logInfo("*:COMPLETE:**********SUCCESS------Collab Item Saved************");
			}
				
			if(null != itemErrors)
			{
				wattsLogger.logInfo("*:COMPLETE:**********ERROR------while saving Catalog Item...************");
				wattsLogger.logInfo("*:COMPLETE:**************************************************");
				List<ValidationError> objValidationError = itemErrors.getErrors();
				if (null != objValidationError)
				{
					for (Iterator iterator2 = objValidationError.iterator(); iterator2.hasNext();) {
						ValidationError validationError = (ValidationError) iterator2.next();
						wattsLogger.logInfo(":COMPLETE:Error :: " + validationError.getMessage());
					}
				}
			}else
			{
				wattsLogger.logInfo("*:COMPLETE:**********SUCCESS------Catalog Item Saved--************");
			}
			
			
		} catch (Exception e) 
		{
			// TODO: handle exception
			wattsLogger.logInfo("-:COMPLETE:--Exception Occurred::--"+e.getMessage());
		}
		wattsLogger.logInfo("-:COMPLETE:-----------------------CompleteStepHelper.setItemStepActors() method END-----");
		
	}

}
