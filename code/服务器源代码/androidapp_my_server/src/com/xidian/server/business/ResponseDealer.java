package com.xidian.server.business;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.json.JSONObject;

import com.xidian.database.MariaDB;
import com.xidian.tools.TimeManager;

public class ResponseDealer implements Runnable{
	private PreparedStatement ps;
	private ResultSet rs;
	private BufferedWriter out;
	@SuppressWarnings("unused")
	private String msgId,classs,day,time;
	private String MID,UID;
	private MariaDB db;
	private JSONObject data,msgIn;
	private Map<String,Socket> onlineMap;
	public ResponseDealer(Map<String,Socket> onlineMap,Socket socket,JSONObject msgIn,MariaDB db,String MID){
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		};
		this.db=db;
		this.MID=MID;
		this.msgIn=msgIn;
		this.msgId=msgIn.getString("MsgId");
		this.UID=msgIn.getString("UID");
		this.data=msgIn.getJSONObject("Data");
		this.onlineMap=onlineMap;
	}
	public void run(){
		classs=data.getString("Class");
		day=data.getString("Day");
		time=data.getString("Time");
		responseDealer();
	}
	private boolean responseDealer(){
		if(classs.equals(MsgRes.AllowFile)){
			
		}
		else if(classs.equals(MsgRes.DenieFile)){
			
		}
		else if(classs.equals(MsgRes.FileAccess)){
			
		}
		else if(classs.equals(MsgRes.FileFailed)){
			
		}
		else if(classs.equals(MsgRes.AcceptFriend)){
			String target=data.getJSONObject("Value").getString("Target");
			String isAccept=data.getJSONObject("Value").getString("IsAccept");
			if(isAccept.equals("true"))addFriends();
			Socket targetSo=null;
			synchronized (onlineMap) {
				targetSo=onlineMap.get("Target");
			}
			if(targetSo!=null){
				try{
					BufferedWriter targetOut=new BufferedWriter(new OutputStreamWriter(targetSo.getOutputStream(),"UTF-8"));
					targetOut.write(msgIn.toString()+"\n");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}else{//doesn't online
				synchronized (db) {
					try{
						ps=db.getConnection().prepareStatement("insert into messagepool (id,msg) values(?,?)");
						ps.setString(1,target);
						ps.setString(2,msgIn.toString());
						ps.executeUpdate();
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
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
	private boolean addFriends(){
		try{
			String TargetID=data.getString("Value");
			synchronized(db){	
				rs=db.query("select * from friends where idb='"+TargetID+"' and ida='"+UID+"'"
						+ " or ida='"+TargetID+"' and idb='"+UID+"'");
			}
			if(rs.next()){
				//Action Faile! Already Exsist
				sendPushMsg(MsgPush.Fail);
				System.out.println("Error:Addfriends by Id fail-Already Exsist");
				return false;
			}
			else{
				//Action Success!
				sendPushMsg(MsgPush.Success);
				synchronized (db) {
					//Insert friends List
					ps=db.getConnection().prepareStatement("insert into friends (ida,idb,meet_time) values(?,?,?)");
					ps.setString(1,UID);
					ps.setString(2,TargetID);
					ps.setString(3,day);
					ps.executeUpdate();
				}
				return true;
			}
		}catch(SQLException e1){
			//Action Failed Caused by database error.
			sendPushMsg(MsgPush.Fail);
			e1.printStackTrace();
		}catch(Exception e2){
			sendPushMsg(MsgPush.Fail);
			e2.printStackTrace();
		}
		return false;
	}
}
