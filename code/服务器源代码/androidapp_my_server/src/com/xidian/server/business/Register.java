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

public class Register{
	private PreparedStatement ps;
	private ResultSet rs;
	private BufferedWriter out;
	@SuppressWarnings("unused")
	private String msgId;
	private String msgReg_Id;
	private String msgReg_Pw;
	@SuppressWarnings("unused")
	private String msgReg_Longitude,msgReg_Latitude,msgReg_Address;
	private String msgReg_Adcode;
	private String MID;
	private String nickName=null;
	
	public boolean register(Socket socket,JSONObject msgIn,MariaDB db,String MID){
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		};
		this.MID=MID;
		JSONObject data=msgIn.getJSONObject("Data");
		msgId=msgIn.getString("MsgId");
		msgReg_Id=data.getString("Account");
		msgReg_Pw=data.getString("Password");
		msgReg_Longitude=data.getString("Longitude");
		msgReg_Latitude=data.getString("Latitude");
		msgReg_Address=data.getString("Address");
		msgReg_Adcode=data.getString("Adcode");
		System.out.println("Log:Recevie Register Quest:"+msgReg_Id+":"+msgReg_Pw);
		try{
			if(!data.getString("Valicode").equals("empty")){
				//vali code wrong
				sendPushMsg(MsgPush.ValicodeWrong);
				return false;
			}
			else{
				rs=db.query("select id from user where id='"+msgReg_Id+"'");
				
				if(rs.next()){
					//Register Failed! User Account Exist
					sendPushMsg(MsgPush.RegisterFailed);
					System.out.println("Id Already used!");
					return false;
				}
				else{
					synchronized(db) {
						//Insert user table
			            ps=db.getConnection().prepareStatement("insert into user (id,password) values(?,?)");
			            ps.setString(1, msgReg_Id);
			            ps.setString(2, msgReg_Pw);
			            ps.executeUpdate();
			            //Insert profie table
			            ps=db.getConnection().prepareStatement("insert into profile (id,reg_time,adcode) values(?,?,?)");
			            ps.setString(1, msgReg_Id);
			            ps.setString(2, TimeManager.getSqlDate());
			            ps.setString(3, msgReg_Adcode);
			            ps.executeUpdate();
			            //set nick_name = id (default)
			            ps=db.getConnection().prepareStatement("update profile set nick_name=? where id=?");
			            ps.setString(1, msgReg_Id);
			            ps.setString(2, msgReg_Id);
			            ps.executeUpdate();
			            //Insert online table
			            /*ps=db.getConnection().prepareStatement("insert into online (id,longitude,latitude,address,adcode) values(?,?,?,?,?)");
			            ps.setString(1,msgReg_Id);
			            ps.setString(2,msgReg_Longitude);
			            ps.setString(3,msgReg_Latitude);
			            ps.setString(4,msgReg_Address);
			            ps.setInt(5,Integer.parseInt(msgReg_Adcode));
			            ps.executeUpdate();*/
			            //Try todo make them atom - Dai
			        }
					//Register Access!
					synchronized (db) {
						rs=db.query("select nick_name from profile where id='"+msgReg_Id+"'");
						if(rs.next()){
							nickName=rs.getString(1);
						}else nickName=msgReg_Id;
					}
					sendPushMsg(MsgPush.RegisterSuccess);
					return true;
				}
			}
		}catch(SQLException e1){
			//Register Failed Caused by database error.
			sendPushMsg(MsgPush.RegisterFailed);
			e1.printStackTrace();
		}catch(Exception e2){
			sendPushMsg(MsgPush.RegisterFailed);
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
				if(nickName!=null){
					data.put("UserID", msgReg_Id);
					data.put("Password", msgReg_Pw);
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
