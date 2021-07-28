package com.watts.phaseII.helper;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.docstore.DocstoreManager;
import com.ibm.pim.docstore.Document;
import com.ibm.pim.utils.Logger;
import com.ww.pim.email.SendEmailNew;

public class CommonHelper 
{
	public Context getPIMContext()
	{
		Context pimCtx = PIMContextFactory.getCurrentContext();
		
		if(null != pimCtx)
		{
			return pimCtx;
		}
		return null;
		
	}
	
	public void saveFileToDocstore(Context pimCtx,String filePath,StringBuffer sb,Logger wattsLogger){		
		wattsLogger.logInfo("*******************Begin saveFileToDocstore()*********************");
		DocstoreManager manager = pimCtx.getDocstoreManager();
		Document document = manager.createAndPersistDocument(filePath);
		document.setContent(sb.toString());
		/*logger.logInfo("*******************START clearing the buffer*********************");
		//clearing the buffer.
		sb.delete(0, sb.length());
		logger.logInfo("*******************END clearing the buffer*********************");*/
		wattsLogger.logInfo("*******************End saveFileToDocstore()*********************");
	}
	
	public void saveToRealPath(Context pimCtx, String email_attach_File_Path,StringBuffer sb, Logger wattsLogger) 
	{
		wattsLogger.logInfo("*******************Begin saveToRealPath()*********************");
		Writer writer = null;

		try 
		{
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(email_attach_File_Path), "utf-8"));
		    writer.write(sb.toString());
		    
		} catch (IOException ex) 
		{
		    // Report
			wattsLogger.logInfo("*******Exception while writing to file*** :: ["+ex.getMessage()+"]");
		} finally 
		{
		   try 
		   {
			   writer.close();
		   } catch (Exception ex) 
		   {/*ignore*/}
		}
		wattsLogger.logInfo("*******************End saveToRealPath()*********************");
	
		
	}

	public void updateCustomDataBaseTable(Context pimCtx, Logger wattsLogger,String tableNameWithSchema, Map<String, String> valueMap) 
	{
		// TODO Auto-generated method stub
		wattsLogger.logInfo("---updateCustomDataBaseTable  START----");
		String insertQueryStr ="";
		Connection conn = null;
		Statement statement = null;
		String queryPart1 = "";
		String queryPart2 = "";
		int mapSize = 0; 
		
		if(null != valueMap && valueMap.size() > 0)
		{
			mapSize = valueMap.size();
			int count = 1;
			for(Map.Entry<String, String> entry:valueMap.entrySet())
			{
				if(count == mapSize)
				{
					queryPart1 = queryPart1 + entry.getKey();
					queryPart2 = queryPart2 + "'"+entry.getValue()+"'";
				}else
				{
					queryPart1 = queryPart1 + entry.getKey()+",";
					queryPart2 = queryPart2 + "'"+entry.getValue()+"',";
				}
				count++;
			}
			insertQueryStr = "INSERT INTO "+tableNameWithSchema+"("+queryPart1+") VALUES("+queryPart2+")";
			wattsLogger.logInfo("Query::["+insertQueryStr+"]");
			try
			{
				conn = new DBXMLReader().getConnectionFromDBXML(pimCtx, wattsLogger);
				if(null != conn)
				{
					statement = conn.createStatement();
					if(statement.executeUpdate(insertQueryStr) > 0)
					{
						conn.commit();
					}
				}
				
			}catch(Exception e){
				wattsLogger.logInfo("Error occurred while inserting the data: ["+e.getMessage()+"]");
			}finally
			{
				if(null != statement)
				{
					try {
						statement.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(null != conn)
				{
					try {
						conn.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
				
				
		}
		
		wattsLogger.logInfo("---updateCustomDataBaseTable  END----");
	}
	
	public void sendMailForTemplateLoad(Context pimCtx, Logger wattsLogger) 
	{
		wattsLogger.logInfo("----sendMailForTemplateLoad START-----");
		Connection conn = null;
		ResultSet rsltSet = null;
		Statement selectStmnt = null;
		Statement updateStmnt = null;
		String queryStr = "";
		String updateStr = "";
		try
		{
			queryStr = "SELECT TO_ADDRESS,FROM_ADDRESS,MAIL_BODY,MAIN_SUBJECT,DOCSTORE_FILE_PATH,FILE_NAME from PIMDBUSR.WPT_EMAIL_TEMPLATE_LOAD WHERE MAIL_STATUS = 'N'";
			String toAddress = null;
			String fromAddress = null;
			String mailBody = null;
			String mailSubject = null;
			String filePath = null;
			String fileName = null;
			
			conn = new DBXMLReader().getConnectionFromDBXML(pimCtx, wattsLogger);
			if(null != conn)
			{
				selectStmnt = conn.createStatement();
				rsltSet = selectStmnt.executeQuery(queryStr);
				while(rsltSet.next())
				{
					toAddress = rsltSet.getString("TO_ADDRESS");
					fromAddress = rsltSet.getString("FROM_ADDRESS");
					mailBody = rsltSet.getString("MAIL_BODY");
					mailSubject = rsltSet.getString("MAIN_SUBJECT");
					filePath = rsltSet.getString("DOCSTORE_FILE_PATH");
					fileName = rsltSet.getString("FILE_NAME");
					
					
					wattsLogger.logInfo("-TO_ADDRESS :: ["+toAddress+"]");
					wattsLogger.logInfo("-FROM_ADDRESS :: ["+fromAddress+"]");
					wattsLogger.logInfo("-MAIL_BODY :: ["+mailBody+"]");
					wattsLogger.logInfo("-MAIN_SUBJECT :: ["+mailSubject+"]");
					wattsLogger.logInfo("-FILE_PATH :: ["+filePath+"]");
					wattsLogger.logInfo("-FILE_NAME :: ["+fileName+"]");
					
					
					//Send Mail 
					new SendEmailNew().sendEmailToUser(toAddress, fromAddress, mailBody, mailSubject, filePath, fileName);
					wattsLogger.logInfo("-Mail is sent");
					
					//UpdateDatabase Table
					
					updateStr = "UPDATE PIMDBUSR.WPT_EMAIL_TEMPLATE_LOAD SET MAIL_STATUS = 'Y' WHERE FILE_NAME = '"+fileName+"'";
					
					updateStmnt = conn.createStatement();
					if(updateStmnt.executeUpdate(updateStr) > 0 )
					{
						conn.commit();
					}
					
					try
					{
						updateStmnt.close();
					}catch(SQLException e)
					{
						e.printStackTrace();
					}
				}
				
			}
			
			
		}catch(Exception e)
		{
			wattsLogger.logInfo("Error occurred in sendMailForTemplateLoad: ["+e.getMessage()+"]");
		}finally
		{
			if(null != selectStmnt)
			{
				try {
					selectStmnt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(null != conn)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		
		wattsLogger.logInfo("----sendMailForTemplateLoad END-----");
	}

	
	
	
	
	


}
