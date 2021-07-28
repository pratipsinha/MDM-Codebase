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

public class ProductManagementHelper 
{
	public void setEntryAttributesForStep(CollaborationItem collabItem,Context pimCtx,Logger wattsLogger)
	{
		wattsLogger.logInfo("-:PRODUCT MANAGEMENT:-----------------------ProductManagementHelper.setEntryAttributesForStep() method START-----"); 
		try 
		{
			//Set Is_Checked_To_First_Step Variable to true for the collab item
			//collabItem.setAttributeValue(WattsConstants.CHECKOUT_FLAG_PATH, true);
			//
			
			ExtendedValidationErrors collabItemErrs = null;
			try
			{
				//collabItem.getCollaborationArea().getProcessingOptions().setAllProcessingOptions(false);
				collabItemErrs = collabItem.save();
			}catch(Exception e)
			{
				wattsLogger.logInfo("-:PRODUCT MANAGEMENT:-Exception while saving collab item::-"+e.getMessage());
			}
			
			if(null != collabItemErrs)
			{
				wattsLogger.logInfo("**:PRODUCT MANAGEMENT:*********ERROR------while saving Collab Item...************");
				wattsLogger.logInfo("**:PRODUCT MANAGEMENT:*************************************************");
				List<ValidationError> objValidationError = collabItemErrs.getErrors();
				if (null != objValidationError)
				{
					for (Iterator iterator2 = objValidationError.iterator(); iterator2.hasNext();) {
						ValidationError validationError = (ValidationError) iterator2.next();
						wattsLogger.logInfo(":PRODUCT MANAGEMENT:Error :: " + validationError.getMessage());
					}
				}
			}else
			{
				wattsLogger.logInfo("*:PRODUCT MANAGEMENT:**********SUCCESS------Collab Item Saved************");
			}
				
			
			
		} catch (Exception e) 
		{
			// TODO: handle exception
			wattsLogger.logInfo("-:PRODUCT MANAGEMENT:--Exception Occurred::--"+e.getMessage());
		}
		wattsLogger.logInfo("-:PRODUCT MANAGEMENT:-----------------------ProductManagementHelper.setEntryAttributesForStep() method END-----");
		
	}
}
