package com.ww.pim.common.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.ibm.ccd.common.db.ThinPoolDBManager;

public enum DBConnectionManager {

	INSTANCE;

	private final ThinPoolDBManager tdb;
	
	private Connection connection;

	private DBConnectionManager() {
		tdb = new ThinPoolDBManager();
		connection = tdb.getConnection();
		
	}

	public static DBConnectionManager getInstance() {
		return INSTANCE;
	}

	public Connection getConnection() {
		return connection;
	}
	
	public Connection createConnection() {
		return (connection = tdb.getConnection());
	}
	

	public void closeConnection(Connection c) throws SQLException {
		if (null != c) 
		{
			tdb.closeConnection(c);
		}
	}
}