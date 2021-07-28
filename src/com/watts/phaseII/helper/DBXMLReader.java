package com.watts.phaseII.helper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.pim.context.Context;
import com.ibm.pim.utils.Logger;

public class DBXMLReader {
	// BCC - This should use jndi to look this up from WAS or at the least from a Properties File!!!!
	private static final String CONFIG_FILE_NAME_FROM_TOP 	= "/etc/default/db.xml";

	private static final String DEFAULT_TOP					= "/opt/IBM/MDMCSv115/";
	
	private static String className 						= "";
	private static String connectionString 					= "";
	private static String user 								= "";
	private static String PW								= "";
	private static boolean readDBXML						= false;
	private static String dollarTOP							= "";
	
	private static void debugLog(String strToWrite,Logger logger) {
		logger.logDebug(strToWrite);
	}
	
	private static void errLog(String strToWrite,Logger logger) {
		logger.logDebug(strToWrite);
	}

	/*
	 * get the first child's value and return "" if it doesn't have a child or the list is null
	 */
	private String getFirstChildsValue(NodeList nl) {
		String retValue = "";
		if (null != nl) {
			if (nl.getLength() > 0) {
				retValue = nl.item(0).getTextContent().trim();
			}
		}
		return retValue;
	}
	
	public Connection getConnectionFromDBXML(Context context,Logger logger) {
		Connection conn = null;
		if (!readDBXML) {
			
			dollarTOP = System.getProperty("TOP");
			if (null == dollarTOP || "".equals(dollarTOP)) {
				dollarTOP = DEFAULT_TOP;				
			}
			String dbConfigFileName 	= dollarTOP + CONFIG_FILE_NAME_FROM_TOP;
			debugLog("$TOP = " + dbConfigFileName,logger);
			
			File xmlFile = new File(dbConfigFileName);
			if (xmlFile.exists()) {
				DocumentBuilderFactory docBldrFactory 	= DocumentBuilderFactory.newInstance();
				DocumentBuilder docBldr;
				try {
					docBldr = docBldrFactory.newDocumentBuilder();
					Document doc = docBldr.parse(xmlFile);

					user 				= getFirstChildsValue(doc.getElementsByTagName("db_userName"));
					PW					= getFirstChildsValue(doc.getElementsByTagName("db_password_plain"));
					connectionString	= getFirstChildsValue(doc.getElementsByTagName("db_url"));
					className 			= getFirstChildsValue(doc.getElementsByTagName("db_class_name"));

					debugLog("DB                  User: " + user,logger);
					debugLog("DB                    PW: " + PW,logger);
					debugLog("DB     Connection String: " + connectionString,logger);
					debugLog("DB            Class Name: " + className,logger);
					try {
						conn= DriverManager.getConnection(connectionString, user, PW);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (ParserConfigurationException e) {
					errLog("Parser ERROR!!!! Can not read the DB Config Information!!!!" + e,logger);
					e.printStackTrace();
				} catch (SAXException e) {
					errLog("SAX ERROR!!!! Can not read the DB Config Information!!!!" + e,logger);
					e.printStackTrace();
				} catch (IOException e) {
					errLog("IO ERROR!!!! Can not read the DB Config Information!!!!" + e,logger);
					e.printStackTrace();
				}
			} else {
				errLog("ERROR!!!! Can not read the DB Config Information!!!!",logger);
			}
			
		}
		return conn;
		// doc object will close on exit of the method		
	}
}
