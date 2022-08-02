package com.ims.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class IMSDatabase {

	private final String URL = "jdbc:sqlite:resources/imsdb.db";
	private Connection conn;
	
	public IMSDatabase() {
		connect();
	}

	/**
	 * Connect to database
	 */
	public void connect() {
		try {
			conn = DriverManager.getConnection(URL);
			System.out.println("Connection to Database has been established.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Close connection to database
	 */
	public void close() {
		try {
			if (conn != null) {
				conn.close();
				System.out.println("Connection to Database has been closed.");
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Return instance of the database connection
	 * @return
	 */
	public Connection getConnection() {
		return conn;
	}

}
