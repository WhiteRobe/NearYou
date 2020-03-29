package com.xidian.server.business;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import com.xidian.database.MariaDB;
import com.xidian.tools.TimeManager;

public class ProfileAlter implements Runnable{
	private PreparedStatement ps;
	private BufferedWriter out;
	@SuppressWarnings("unused")
	private String msgId;
	private String nick_name,country,email;
	private String province,phone;
	private String note,city,real_name;
	private String address,birth;
	private String hobby,adcode,job_privacy;
	private String masters,loc_privacy,education,indi_privacy;
	private String learning,school,organization,sex,first_lt;
	private String MID,UID;
	private JSONObject data;
	private MariaDB db;
	
	public ProfileAlter(Socket socket,JSONObject msgIn,MariaDB db,String MID){
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		};
		this.data=msgIn.getJSONObject("Data");
		this.db=db;
		this.UID=msgIn.getString("UID");
		this.MID=MID;
	}
	public void run(){
		getData();
		alter();
	}
	public boolean alter(){
		try{
			synchronized(db) {
	            //Insert profie table
	            ps=db.getConnection().prepareStatement("update profile set "
	            		+ "nick_name=?,note=?,hobby=?,"
	            		+ "masters=?,learning=?,country=?,"
	            		+ "province=?,city=?,address=?,"
	            		+ "adcode=?,loc_privacy=?,school=?,"
	            		+ "organization=?,education=?,job_privacy=?,"
	            		+ "real_name=?,birth=?,phone=?,"
	            		+ "email=?,sex=?,first_lt=?,indi_privacy=?"
	            		+ " where id=?");
	            ps.setString(1,nick_name);
	            ps.setString(2,note);
	            ps.setString(3,hobby);
	            ps.setString(4,masters);
	            ps.setString(5,learning);
	            ps.setString(6,country);
	            ps.setString(7,province);
	            ps.setString(8,city);
	            ps.setString(9,address);
	            ps.setInt(10,Integer.parseInt(adcode));
	            ps.setString(11,loc_privacy);
	            ps.setString(12,school);
	            ps.setString(13,organization);
	            ps.setString(14,education);
	            ps.setString(15,job_privacy);
	            ps.setString(16,real_name);
	            ps.setString(17,birth);
	            ps.setString(18,phone);
	            ps.setString(19,email);
	            ps.setString(20,sex);
	            ps.setString(21,first_lt);
	            ps.setString(22,indi_privacy);
	            ps.setString(23,UID);
	            ps.executeUpdate();
	            //Try todo make them atom - Dai
	        }
			//Alter Access!
			if(data.getString("UpdataAvatar").equals("true")){
				try{
					synchronized(db) {
						//Insert profie table
						//String type=data.getString("Type");//Abondon
						String type="";
						String filename=UID+"_avatar"+type;//such as 958_avatar
						String URL=PackageConstants.rootURL+"avatar/"+filename;
						
						//Wirte Picture
						ServerSocket fileServerSocket=new ServerSocket(PackageConstants.FILE_SERVER_PORT);
						Socket fileSocket=fileServerSocket.accept();
						InputStream in = fileSocket.getInputStream();  
						FileOutputStream fw=new FileOutputStream(new File(URL));
						byte[] buf=new byte[1024];
						int len=0;
						while((len=in.read(buf))!=-1){
							fw.write(buf,0,len);
						}
						fw.flush();fw.close();
						fileSocket.close();fileServerSocket.close();
						
						//if success,update db-profile
						ps=db.getConnection().prepareStatement("update profile set avatar=? where id=?");
						ps.setString(1, filename);
						ps.setString(2, UID);
						ps.executeUpdate();
					}
				}catch (Exception e) {
					sendPushMsg(MsgPush.AvatarLost);
					return false;
				}
			}
			//Alter Avatar Access!
			sendPushMsg(MsgPush.UpdataSuccess);
			return true;
		}catch(SQLException e1){
			//Alter Failed Caused by database error.
			sendPushMsg(MsgPush.UpdataFailed);
			e1.printStackTrace();
		}catch(Exception e2){
			sendPushMsg(MsgPush.UpdataFailed);
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
		nick_name=data.getString("NickName");
		note=data.getString("Note");
		hobby=data.getString("Hobby");
		masters=data.getString("Master");
		learning=data.getString("Learning");
		country=data.getString("Country");
		province=data.getString("Province");
		city=data.getString("City");
		address=data.getString("Address");
		adcode=data.getString("Adcode");
		loc_privacy=data.getString("LocPrivacy");
		school=data.getString("School");
		organization=data.getString("Organization");
		education=data.getString("Education");
		job_privacy=data.getString("JobPrivacy");
		real_name=data.getString("RealName");
		birth=data.getString("Birth");
		phone=data.getString("Phone");
		email=data.getString("Email");
		sex=data.getString("Sex");
		indi_privacy=data.getString("IndiPrivacy");
		first_lt=data.getString("FirstLt");
	}
}
