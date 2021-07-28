package com.ww.pim.common.utils;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConnectionManager {
	private static Connection con;
	public static Connection getConnection(){
		String propertiesFilePath = "";
		try{
			propertiesFilePath = System.getProperty("TOP") + Constant.COMMON_PROPERTIES;
			FileInputStream inStream = new FileInputStream(propertiesFilePath);
			Properties commonProperties = new Properties();
			commonProperties.load(inStream);
			Class.forName(commonProperties.getProperty("db_class_name"));
			con=DriverManager.getConnection(commonProperties.getProperty("db_url"),commonProperties.getProperty("db_userName"),commonProperties.getProperty("db_password_plain"));
		}catch(Exception exp){
			System.out.println("Exception in getting Connection "+exp);
		}
		return con;
	}
}
