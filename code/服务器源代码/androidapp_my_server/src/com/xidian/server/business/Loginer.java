package com.xidian.server.business;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import org.json.JSONObject;

import com.xidian.database.MariaDB;
import com.xidian.tools.TimeManager;

public class Loginer {
	
	@SuppressWarnings("unused")
	private PreparedStatement ps;
	private ResultSet rs;
	private BufferedWriter out;
	@SuppressWarnings("unused")
	private String msgId;
	private String msgLog_Id;
	private String msgLog_Pw;
	@SuppressWarnings("unused")
	private String msgLog_Longitude,msgLog_Latitude,msgLog_Address,msgLog_Adcode;
	private String MID;
	private String nickName=null;
	
	public boolean loginer(Socket socket,JSONObject msgIn,MariaDB db,String MID){
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.MID=MID;
		JSONObject data=msgIn.getJSONObject("Data");
		msgId=msgIn.getString("MsgId");
		msgLog_Id=data.getString("Account");
		msgLog_Pw=data.getString("Password");
		msgLog_Longitude=data.getString("Longitude");
		msgLog_Latitude=data.getString("Latitude");
		msgLog_Address=data.getString("Address");
		msgLog_Adcode=data.getString("Adcode");
		System.out.println("Log:Recevie Login Quest:"+msgLog_Id+":"+msgLog_Pw);
		try{
			if(!data.getString("Valicode").equals("empty")){
				//vali code wrong
				sendPushMsg(MsgPush.ValicodeWrong);
				return false;
			}
			else{
				synchronized (db) {
					rs=db.query("select id from user where id='"+msgLog_Id+"' and password='"+msgLog_Pw+"'");
					if(rs.next()){
						//Login Access!
						/*synchronized (db) {
							//Insert online List
							ps=db.getConnection().prepareStatement("insert into online (id,longitude,latitude,address,adcode) values(?,?,?,?,?)");
							ps.setString(1,msgLog_Id);
							ps.setString(2,msgLog_Longitude);
							ps.setString(3,msgLog_Latitude);
							ps.setString(4,msgLog_Address);
							ps.setInt(5,Integer.parseInt(msgLog_Adcode));
							ps.executeUpdate();
						}*/
						synchronized (db) {
							rs=db.query("select nick_name from profile where id='"+msgLog_Id+"'");
							if(rs.next()){
								nickName=rs.getString(1);
							}else nickName=msgLog_Id;
						}
						sendPushMsg(MsgPush.LoginSuccess);
						System.out.println("Login:Success - "+msgLog_Id);
						return true;
					}
					else{
						//Login Failed!Already online
						sendPushMsg(MsgPush.LoginFailed);
						return false;
					}
				}
			}
		}catch(SQLException e1){
			//Login Failed Caused by database error.
			sendPushMsg(MsgPush.LoginFailed);
			e1.printStackTrace();
		}catch(Exception e2){
			//Other Failed
			sendPushMsg(MsgPush.LoginFailed);
			e2.printStackTrace();
		}
		System.out.println("Login:Fail - "+msgLog_Id);
		return false;
	}
	
	private void sendPushMsg(String msgToSend){
		try{
			JSONObject msgOut=new JSONObject();
			msgOut.put("MsgType",MsgType.SERVER_PUSH);
			msgOut.put("MsgId",MID);
			msgOut.put("UID",PackageConstants.SERVER_ID);
				JSONObject data=new JSONObject();
				data.put("Class",msgToSend);
				if(nickName!=null){
					data.put("UserID", msgLog_Id);
					data.put("Password", msgLog_Pw);
					data.put("NickName", nickName);
				}
				data.put("Day",TimeManager.getSqlDate());
				data.put("Time",TimeManager.getTime());
			msgOut.put("Data", data);
			
			out.write(msgOut.toString()+"\n");
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
