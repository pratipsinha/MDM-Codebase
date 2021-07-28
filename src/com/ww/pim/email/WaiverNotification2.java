//script_execution_mode=java_api="japi:///javaapiclass:com.ww.pim.email.WaiverNotification.class"
package com.ww.pim.email;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.ReportGenerateFunction;
import com.ibm.pim.extensionpoints.ReportGenerateFunctionArguments;
import com.ibm.pim.utils.Logger;
import com.ww.pim.common.utils.ConnectionManager;
import com.ww.pim.common.utils.Constant;

public class WaiverNotification2 implements ReportGenerateFunction {

	private Connection con;
	private HashMap<String, String> prduct_manager_email;
	private HashMap<String, String> group2_email;
	private Context context;
	private Logger objLogger;

	@Override
	public void reportGenerate(ReportGenerateFunctionArguments arg0) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Set<CollaborationItem> productManagerNotificationItem = new HashSet<CollaborationItem>();
		
		Set<CollaborationItem> group2NotificationItem = new HashSet<CollaborationItem>();
		
		try 
		{
			context = PIMContextFactory.getCurrentContext();
			objLogger = context.getLogger(Constant.REPORT_LOGGER_NAME);
			objLogger.logDebug("WaiverNotification Report started..");
			Map launchDateMap = arg0.getInputs();
			String launchDt =  (String)launchDateMap.get("LaunchDate");
			Date launchDate = sdf.parse(launchDt);
			objLogger.logDebug("LaunchDate--"+launchDate);
			Set<String> uniquePL = new HashSet<String>();
			Map<String, List<String>> plMailContentMap = new HashMap<String, List<String>>();
			populateMap();
			ItemCollaborationArea  itmCollabArea= (ItemCollaborationArea)context.getCollaborationAreaManager().getCollaborationArea(Constant.QADPARTSMAINTCOLAREANAME);

			PIMCollection<CollaborationItem> coll = itmCollabArea.getCheckedOutItems();
			for(CollaborationItem collaborationItem : coll){		
				String itemStatus = "";
				String pimComplete = "";
				Date workflowDate = null;
				//objLogger.logDebug("ColItem--"+collaborationItem.getPrimaryKey());
				if(null !=collaborationItem.getAttributeValue("Product Catalog Global Spec/ItemStatus") && null != collaborationItem.getAttributeValue("Product Catalog Global Spec/WorkflowDate") && null != collaborationItem.getAttributeValue("Product Catalog Global Spec/ProductLine"))
				{
					itemStatus = collaborationItem.getAttributeValue("Product Catalog Global Spec/ItemStatus").toString();
					if(null != collaborationItem.getAttributeValue("Product Catalog Global Spec/PIMComplete")) {
						pimComplete =collaborationItem.getAttributeValue("Product Catalog Global Spec/PIMComplete").toString();
					}
					else
					{
						pimComplete = "No";
					}
					workflowDate = (Date) collaborationItem.getAttributeValue("Product Catalog Global Spec/WorkflowDate");
					String pl = collaborationItem.getAttributeValue("Product Catalog Global Spec/ProductLine").toString();
					if(itemStatus.equals("AC") && pimComplete.equals("No")) {
						if(null != workflowDate) {
							long diffFromLauchDate = Math.abs(workflowDate.getTime() - launchDate.getTime());
							objLogger.logDebug("diffFromLauchDate--"+diffFromLauchDate);
							if(diffFromLauchDate > 0) { //workflow insertion date after launch date
								long diffFromCurrentDateInMill = Math.abs(System.currentTimeMillis() - workflowDate.getTime());
								long diffFromCurrentDateInDays = TimeUnit.DAYS.convert(diffFromCurrentDateInMill, TimeUnit.MILLISECONDS);
								objLogger.logDebug("diffFromCurrentDateInMill--"+diffFromCurrentDateInMill+" diffFromCurrentDateInDays--"+diffFromCurrentDateInDays);

								if(diffFromCurrentDateInDays%7 == 0) {
									objLogger.logDebug("Item to be processed---"+collaborationItem.getPrimaryKey()+"itemStatus--"+itemStatus+" pimComplete---"+pimComplete+" workflowDate----"+workflowDate+" ProductLine--"+pl);
									productManagerNotificationItem.add(collaborationItem);
									uniquePL.add(pl);
									objLogger.logDebug("ProductLine "+pl+" Added..");
								}
								if(diffFromCurrentDateInDays%14 == 0) {
									objLogger.logDebug("Item to be processed---"+collaborationItem.getPrimaryKey()+"itemStatus--"+itemStatus+" pimComplete---"+pimComplete+" workflowDate----"+workflowDate+" ProductLine--"+pl);
									group2NotificationItem.add(collaborationItem);

								}
							}
						}
					}
				}
			}

			for(String productLine : uniquePL) {
				List<String> mailBodyList = new ArrayList<String>();

				for(CollaborationItem pmItemTemp : productManagerNotificationItem) {
					objLogger.logDebug("pmItemTemp---"+pmItemTemp);
					String currentPLTemp = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/ProductLine");
					objLogger.logDebug("currentPLTemp--"+currentPLTemp);
					String mailBody = "";
					if(currentPLTemp.equals(productLine)) {
						String partNo = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/Part Number");
						String sysRecord = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/SystemOfRecord");
						String desc = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/Description1");
						mailBody = mailBody + "<tr><td>" +partNo+ "</td><td>" + currentPLTemp + "</td><td>" + sysRecord + "</td><td>" + desc + "</td></tr>";
						if(!plMailContentMap.containsKey(productLine) )
						{
							if(!mailBody.equals("") ){
								mailBodyList.add(mailBody);
								plMailContentMap.put(currentPLTemp, mailBodyList);
							}
						}
						else
						{
							if(!mailBody.equals("") ) {
								mailBodyList = plMailContentMap.get(currentPLTemp);
								mailBodyList.add(mailBody);
								plMailContentMap.put(currentPLTemp, mailBodyList);
							}
						}

						objLogger.logDebug("mailBody--"+mailBody);
					}
				}
			}
			objLogger.logDebug("plMailContentMap--"+plMailContentMap.size());
			for (Map.Entry<String,List<String>> entry : plMailContentMap.entrySet()) {
				objLogger.logDebug("entry.getKey() --"+entry.getKey() );
				String mailContent = "<table><tr><td><b>Part Number</b></td><td><b>Product Line</b></td><td><b>System of Record</b></td><td><b>Description</b></td></tr>";
				for(String mailBody :entry.getValue() )
				{
					objLogger.logDebug("mailBody--"+mailBody);
					mailContent = mailContent+mailBody;
				}
				mailContent = mailContent+"</table>";
				SendEmail smail = new SendEmail();
				objLogger.logDebug("mailContent---"+mailContent);
				if(null != prduct_manager_email.get(entry.getKey())) {
					smail.sendMailCommon(prduct_manager_email.get(entry.getKey()), "pim@watts.com", mailContent, "Waiver Notification - Parts available in Workflow");
				}
			}
			
			// group 2 email functionality
			Map<String, List<String>> plGr2MailContentMap = new HashMap<String, List<String>>();
			String table_initializer="<table><tr><td><b>Part Number</b></td><td><b>Product Line</b></td><td><b>System of Record</b></td><td><b>Description</b></td></tr>";
			for(CollaborationItem pmItemTemp : group2NotificationItem) {
				objLogger.logDebug("pmItemTemp---"+pmItemTemp);
				
				
				String currentPLTemp = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/ProductLine");
				String partNo = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/Part Number");
				String sysRecord = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/SystemOfRecord");
				String desc = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/Description1");
				String group2_email_addr = group2_email.get(currentPLTemp.trim());
				if(null != group2_email_addr) {
					String [] group2emailaddresses = group2_email_addr.split(";");
					for (String email : group2emailaddresses){
						System.out.println(email);
						if(plGr2MailContentMap.containsKey(email)) {
							ArrayList partsInfo = (ArrayList) plMailContentMap.get(email);
							partsInfo.add(partNo+"|"+sysRecord+"|"+currentPLTemp+"|"+desc);
							plGr2MailContentMap.put(email, partsInfo);
						}
						else {
							ArrayList<String> partsInfoAlt = new ArrayList<String>();
							partsInfoAlt.add(partNo+"|"+sysRecord+"|"+currentPLTemp+"|"+desc);
							plGr2MailContentMap.put(email, partsInfoAlt);
						}
					}
				}
				
			}
			
			for (String key : plGr2MailContentMap.keySet()) {
				ArrayList<String> partsInfoMail = (ArrayList<String>) plGr2MailContentMap.get(key);
				String gr2_mail_body="";
				 for (String str : partsInfoMail) { 		      
			           System.out.println(str); 
			           String [] strArr = str.split("\\|");
			           gr2_mail_body = gr2_mail_body + "<tr><td>"+strArr[0]+"</td><td>"+strArr[1]+"</td><td>"+strArr[2]+"</td><td>"+strArr[3]+"</td></tr>";
			      }
				 if(!gr2_mail_body.equals("")) {
					 gr2_mail_body = table_initializer + gr2_mail_body + "</table>";
					 SendEmail smail = new SendEmail();
					// smail.sendMailCommon(key, "pim@watts.com", gr2_mail_body, "Waiver - Parts available in Workflow (Test Email for UAT)");
					 smail.sendMailCommon(key, "pim@watts.com", gr2_mail_body, "Waiver - Parts available in Workflow (Email form PROD)");
				 }
			}
			

			// end of group 2 email
			
		} catch (Exception e) 
		{
			e.printStackTrace();
			objLogger.logDebug("Exception caught in main method.."+e);
		}
		finally
		{
			objLogger.logDebug("WaiverNotification Report ended..");
		}
	}

	private void populateMap() {
		prduct_manager_email = new HashMap<String,String>();
		group2_email = new HashMap<String,String>();
		String sql = "select productline,email,email_group2 from pimdbuser.productline_manager";

		try {
			con = ConnectionManager.getConnection();		
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(null != rs) {
				while(rs.next()) {
					String product_line  = rs.getString(1);
					String email = rs.getString(2);
					String email_group2 = rs.getString(3);
					System.out.println(product_line+":"+email);
					prduct_manager_email.put(product_line, email);
					group2_email.put(product_line, email_group2);
				}

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			objLogger.logDebug("Exception caught in populateMap method.."+e);
		}
	}
	
}


