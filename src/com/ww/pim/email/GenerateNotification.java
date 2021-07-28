package com.ww.pim.email;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.ww.pim.common.utils.ConnectionManager;

public class GenerateNotification {
	Connection con;
	HashMap<String, String> prduct_manager_email;
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		GenerateNotification generateNotification = new GenerateNotification();
		//generateNotification.populateMap();
		generateNotification.sendPMNotification();

	}*/
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
	
	public void sendPMNotification() {
		String sql = "select PartNumber,SystemOfRecord,ProductLine,status from PIMDBUSER.Mail_Notification where status='N' order by ProductLine";
		String current_pl = "";
		String mail_body = "";
		String table_initializer = "<table><tr><td>Part Number</td><td>System Of Record</td><td>Product Line</td></tr>";
		try {
			if(null == prduct_manager_email) {
				populateMap();
			}
				
			con = ConnectionManager.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(null != rs) {
				while(rs.next()) {
					String part_number = rs.getString(1);
					String sys_of_record = rs.getString(2);
					String product_line = rs.getString(3);
					System.out.println(part_number+":"+sys_of_record+":"+product_line);
					if(current_pl.equalsIgnoreCase(product_line)) {
						mail_body = mail_body + "<tr><td>"+part_number+"</td><td>"+sys_of_record+"</td><td>"+product_line+"</td></tr>";
					}
					else {
						if(!mail_body.equals("")) {
							mail_body = mail_body + "</table>";
							String product_manager_email = prduct_manager_email.get(current_pl.trim());
							if(null != product_manager_email) {
								SendEmail smail = new SendEmail();
								smail.sendMailCommon(product_manager_email, "pim@watts.com", mail_body, "Notification - Parts available in Workflow");
							}
						}
						current_pl = product_line;
						mail_body =table_initializer +"<tr><td>"+part_number+"</td><td>"+sys_of_record+"</td><td>"+product_line+"</td></tr>";
					}
				}
				if(!mail_body.equals("")) {
					mail_body = mail_body + "</table>";
					String product_manager_email = prduct_manager_email.get(current_pl.trim());
					if(null != product_manager_email) {
						SendEmail smail = new SendEmail();
						smail.sendMailCommon(product_manager_email, "pim@watts.com", mail_body, "Notification - Parts available in Workflow");
					}
				}
				updateRecordMailNotificationStatus();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	public void sendEngineeringNotification() {
		String sql = "select PartNumber,SystemOfRecord,Email,status from PIMDBUSER.Mail_Notification_Engineering where status='N' order by Email";
		String current_pl = "";
		String mail_body = "";
		String table_initializer = "<table><tr><td>Part Number</td><td>System Of Record</td></tr>";
		try {
			
			con = ConnectionManager.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(null != rs) {
				while(rs.next()) {
					String part_number = rs.getString(1);
					String sys_of_record = rs.getString(2);
					String email = rs.getString(3);
					System.out.println(part_number+":"+sys_of_record+":"+email);
					if(current_pl.equalsIgnoreCase(email)) {
						mail_body = mail_body + "<tr><td>"+part_number+"</td><td>"+sys_of_record+"</td></tr>";
					}
					else {
						if(!mail_body.equals("")) {
							mail_body = mail_body + "</table>";
							//String product_manager_email = prduct_manager_email.get(current_pl.trim());
							if(null != current_pl) {
								SendEmail smail = new SendEmail();
								smail.sendMailCommon(current_pl, "pim@watts.com", mail_body, "Notification - Parts available in Workflow");
							}
						}
						current_pl = email;
						mail_body =table_initializer +"<tr><td>"+part_number+"</td><td>"+sys_of_record+"</td></tr>";
					}
				}
				if(!mail_body.equals("")) {
					mail_body = mail_body + "</table>";
					//String product_manager_email = current_pl;
					if(null != current_pl) {
						SendEmail smail = new SendEmail();
						smail.sendMailCommon(current_pl, "pim@watts.com", mail_body, "Notification - Parts available in Workflow");
					}
				}
				updateRecordMailNotificationStatus();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	private void updateRecordMailNotificationStatus() {
		String sql = "UPDATE PIMDBUSER.Mail_Notification SET status='Y' where status='N'";
		try {
			
			con = ConnectionManager.getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	private void updateRecordEngineeringMailNotificationStatus() {
		String sql = "UPDATE PIMDBUSER.Mail_Notification_Engineering SET status='Y' where status='N'";
		try {
			con = ConnectionManager.getConnection();		
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}	
	public void insertRecordMailNotification(String part_no, String sys_record, String product_line) {
		String sql = "insert into PIMDBUSER.Mail_Notification(PartNumber,SystemOfRecord,ProductLine,status) values(?,?,?,'N')";
		try {
			con = ConnectionManager.getConnection();		
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, part_no);
			stmt.setString(2, sys_record);
			stmt.setString(3, product_line);
			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	public void insertRecordMailNotificationEngineering(String part_no, String sys_record, String email) {
		String sql = "insert into PIMDBUSER.Mail_Notification_Engineering(PartNumber,SystemOfRecord,Email,status) values(?,?,?,'N')";
		try {
			con = ConnectionManager.getConnection();		
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, part_no);
			stmt.setString(2, sys_record);
			stmt.setString(3, email);
			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
