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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ibm.pim.collaboration.CollaborationArea;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ww.pim.common.utils.ConnectionManager;

public class WaiverNotification {
	Connection con;
	HashMap<String, String> prduct_manager_email;
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public void createNotification() {
		String userName = "";
		String company = "";
		String password = "";
		CollaborationArea objColArea = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		List<CollaborationItem> productManagerNotificationItem = new ArrayList<CollaborationItem>();
		
		try {
			Context context = PIMContextFactory.getContext(userName, password, company);
			Date lauchDate = sdf.parse("2018-04-27");
			Set<String> uniquePL = new HashSet<String>();
			
			objColArea = context.getCollaborationAreaManager().getCollaborationArea("");
			ItemCollaborationArea itmCollabArea = (ItemCollaborationArea) objColArea;
			
			PIMCollection<CollaborationItem> coll = itmCollabArea.getCheckedOutItems();
			for(CollaborationItem collaborationItem : coll){		
				String itemStatus = "";
				String pimComplete = "";
				Date workflowDate = null;
				itemStatus = (String) collaborationItem.getAttributeValue("Product Catalog Global Spec/ItemStatus");
				pimComplete = (String) collaborationItem.getAttributeValue("Product Catalog Global Spec/PIMComplete");
				workflowDate = (Date) collaborationItem.getAttributeValue("Product Catalog Global Spec/WorkflowDate");
				String pl = (String) collaborationItem.getAttributeValue("Product Catalog Global Spec/ProductLine");
				
				if(itemStatus.equals("AC") && pimComplete.equals("No")) {
					if(null != workflowDate) {
						long diffFromLauchDate = Math.abs(workflowDate.getTime() - lauchDate.getTime());
						if(diffFromLauchDate > 0) { //workflow insertion date after lauch date
							long diffFromCurrentDateInMill = Math.abs(System.currentTimeMillis() - workflowDate.getTime());
						    long diffFromCurrentDateInDays = TimeUnit.DAYS.convert(diffFromCurrentDateInMill, TimeUnit.MILLISECONDS);
						    
						    if(diffFromCurrentDateInDays%7 == 0) {
						    	productManagerNotificationItem.add(collaborationItem);
						    	uniquePL.add(pl);
						    }
						}
					}
				}
			}
			
			for(String productLine : uniquePL) {
				
				for(CollaborationItem pmItemTemp : productManagerNotificationItem) {
					String currentPLTemp = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/ProductLine");
					String mailBody = "";
					if(currentPLTemp.equals(productLine)) {
						String partNo = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/Part Number");
						String sysRecord = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/SystemOfRecord");
						String desc = (String) pmItemTemp.getAttributeValue("Product Catalog Global Spec/Description1");
						mailBody = mailBody + "<tr><td>" +partNo+ "</td><td>" + currentPLTemp + "</td><td>" + sysRecord + "</td><td>" + desc + "</td></tr>";
					}
					
					String product_manager_email = prduct_manager_email.get(currentPLTemp.trim());
					if((!mailBody.equals("")) && (null!=product_manager_email)) {
						mailBody = "<table><tr><td><b>Part Number</b></td><td><b>Product Line</b></td><td><b>System of Record</b></td><td><b>Description</b></td></tr>" + mailBody + "</table>";
						SendEmail smail = new SendEmail();
						smail.sendMailCommon(product_manager_email, "pim@watts.com", mailBody, "Waiver Notification - Parts available in Workflow");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void populateMap() {
		prduct_manager_email = new HashMap<String,String>();
		String sql = "select productline,email from pimdbuser.productline_manager";

		try {
			con = ConnectionManager.getConnection();		
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(null != rs) {
				while(rs.next()) {
					String product_line  = rs.getString(1);
					String email = rs.getString(2);
					System.out.println(product_line+":"+email);
					prduct_manager_email.put(product_line, email);
				}
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
