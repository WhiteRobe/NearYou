package com.xidian.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDB {
	@SuppressWarnings("unused")
	private static PreparedStatement ps;
	@SuppressWarnings("unused")
	private static ResultSet rs;
	
	private static Statement sta;
	private static Connection con;
	
	public void setStatement(Statement statement){
		sta=statement;
	}
	public void setConnection(Connection connnection){
		con=connnection;
	}
	
	public Statement getStatement(){
		return sta;
	}
	public Connection getConnection(){
		return con;
	}
	public Statement getNewStatement(){
		try {
			return con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ResultSet query(String string){
		ResultSet result=null;
		try {
			result=sta.executeQuery(string);
		} catch (SQLException e) {
			//System.out.println("Log:02-Prepare Language:[ "+string+" ]Failed!");
			e.printStackTrace();
		}
		//System.out.println("Log:02-Prepare Language:[ "+string+" ]Acessed!");
		return result;
	}
	public boolean exec(String str){
		try {
			sta.execute(str);
		} catch (SQLException e) {
			//System.out.println("Log:03-Prepare Language:[ "+str+" ]Failed!");
			return false;
		}
		//System.out.println("Log:03-Prepare Language:[ "+str+" ]Acessed!");
		return true;
	}
}
