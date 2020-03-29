package com.xidian.server.business;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import com.xidian.database.MariaDB;
import com.xidian.tools.TimeManager;

public class CommentPoster implements Runnable{
	private PreparedStatement ps;
	private BufferedWriter out;
	@SuppressWarnings("unused")
	private String msgId;
	private String text;
	private String location;
	private String adcode;
	private String target;
	private String day,time;
	private String privacy;
	private String MID,UID;
	private JSONObject msgIn,data;
	private MariaDB db;
	public CommentPoster(Socket socket,JSONObject msgIn,MariaDB db,String MID){
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		};
		this.db=db;
		this.MID=MID;
		this.msgId=msgIn.getString("MsgId");
		this.UID=msgIn.getString("UID");
	}
	
	public void run(){
		data=msgIn.getJSONObject("Data");
		getData();
		commentPoster();
	}
	private boolean commentPoster(){
		try{
			synchronized(db) {
	            //Insert comments table
	            ps=db.getConnection().prepareStatement("insert into comments ("
	            		+ "moments_no,id,text,location,time,date,privacy"
	            		+ ") values(?,?,?,?,?,?,?)");
	            ps.setString(1,target);
	            ps.setString(2,UID);
	            ps.setString(3,text);
	            ps.setString(4,location);
	            ps.setString(5,adcode);
	            ps.setString(6,time);
	            ps.setString(7,day);
	            ps.setString(8,privacy);
	            ps.executeQuery();
	            //comments +1
	            ps=db.getConnection().prepareStatement("update moments set comments=comments+1 where moments_no=?");
	            ps.setString(1,target);
	            ps.executeUpdate();
	            //Try todo make them atom - Dai
	        }
			sendPushMsg(MsgPush.Success);
			return true;
		}catch(SQLException e1){
			//CommentPoster Failed Caused by database error.
			sendPushMsg(MsgPush.Fail);
			e1.printStackTrace();
		}catch(Exception e2){
			sendPushMsg(MsgPush.Fail);
			e2.printStackTrace();
		}
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
				data.put("Day",TimeManager.getSqlDate());
				data.put("Time",TimeManager.getTime());
			msgOut.put("Data", data);
			out.write(msgOut.toString()+"\n");
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void getData(){
		target=data.getString("Target");
		text=data.getString("Text");
		location=data.getString("Location");
		adcode=data.getString("Adcode");
		privacy=data.getString("Privacy");
		day=data.getString("Day");
		time=data.getString("Time");
	}
}
