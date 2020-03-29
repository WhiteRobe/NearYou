package com.xidian.server.main;

import com.xidian.database.MariaDB;
import com.xidian.server.data.JsonMsgSystem;
import com.xidian.tools.TimeManager;

public class ServerMain {
	public static MariaDB db;
	
	public static void main(String args[]){
		System.out.println("Date: " + TimeManager.getSqlDate());
		System.out.println("Date: " + TimeManager.getTime());
		new Thread(new JsonMsgSystem()).start();
	}
}