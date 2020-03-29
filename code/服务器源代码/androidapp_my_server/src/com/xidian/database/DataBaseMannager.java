package com.xidian.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBaseMannager {
	
	@SuppressWarnings("unused")
	private static PreparedStatement ps;
	@SuppressWarnings("unused")
	private static ResultSet rs;
	
	private static Statement sta;
	private static Connection con;
	private static MariaDB mariadb = null;
	
	public DataBaseMannager(){
	}
	public MariaDB getMariaDB() throws Exception{
		if(mariadb != null){
			return mariadb;
		}
		else {
			mariadb=new MariaDB();
			if(isConnected()){
				return mariadb;
			}
			else {
				throw new Exception();
			}
		}
	}
	private boolean ConnectDriver(){
		try{
			Class.forName(DBValue.DRIVER_MYSQL);
			con=DriverManager.getConnection(DBValue.URL,DBValue.AdminName,DBValue.AdminPW);
			sta=con.createStatement();
			mariadb.setConnection(con);
			mariadb.setStatement(sta);
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private boolean isConnected(){
		return ConnectDriver();
	}
}
