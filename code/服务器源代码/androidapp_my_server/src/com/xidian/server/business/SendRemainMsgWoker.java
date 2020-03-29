package com.xidian.server.business;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.xidian.database.MariaDB;
import com.xidian.tools.TimeManager;

public class SendRemainMsgWoker implements Runnable{
	private BufferedWriter out;
	private ResultSet rs;
	private String UserId;
	private MariaDB db;
	
	public SendRemainMsgWoker(Socket socket,MariaDB db,String UserId){
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		};
		this.db=db;
		this.UserId=UserId;
	}
	public void run(){
		System.out.println("Deal remain msg to - [ "+UserId+" ]");
		try {
			synchronized (db) {
				rs=db.query("select * from messagepool where id='"+UserId+"'");
				while(rs.next()){
					JSONObject msgOut=new JSONObject(rs.getString(3));
					out.write(msgOut.toString()+"\n");
					out.flush();
					System.out.println(msgOut.toString());
				}
				//sendPushMsg(MsgPush.Success);
				db.exec("delete from messagepool where id='"+UserId+"'");
			}
		} catch (SQLException e) {
			sendPushMsg(MsgPush.Fail);
			e.printStackTrace();
		} catch (IOException e1) {
			sendPushMsg(MsgPush.Fail);
			e1.printStackTrace();
		}
	}
	
	private void sendPushMsg(String msgToSend){
		try{
			JSONObject msgOut=new JSONObject();
			msgOut.put("MsgType",MsgType.SERVER_PUSH);
			msgOut.put("MsgId",0);//MID=0
			msgOut.put("UID",PackageConstants.SERVER_ID);
				JSONObject data=new JSONObject();
				data.put("Class",msgToSend);
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
