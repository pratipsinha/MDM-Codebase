package com.watts.phaseII.helper;

import org.projectx.mdm.businessrules.BusinessRuleManager.BusinessRuleImplementation;

import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.context.Context;
import com.ibm.pim.utils.Logger;
import com.watts.phaseII.constants.WattsConstants;

public class AssociateGDSNHelper 
{
	public void setCollabEntryAttributesForStep(CollaborationItem collabItem,Context pimCtx,Logger wattsLogger)
	{
		wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:-----------------------AssociateGDSNHelper.setEntryAttributesForStep() method START-----"); 
		try 
		{
			
			//Set Entry_Processed Variable to true for the collab item
			collabItem.setAttributeValue(WattsConstants.ENTRY_PROCESSED_PATH, "true");
			//
			
		} catch (Exception e) 
		{
			// TODO: handle exception
			wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:--Exception Occurred::--"+e.getMessage());
		}
		wattsLogger.logInfo("-:ASSOCAITE_CUSTOMER_GDSN:-----------------------AssociateGDSNHelper.setEntryAttributesForStep() method END-----");
		
	}
}
