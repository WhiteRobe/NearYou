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

public class RequestDealer implements Runnable{
	private PreparedStatement ps;
	private ResultSet rs;
	private BufferedWriter out;
	@SuppressWarnings("unused")
	private String msgId,classs,day,time;
	private String MID,UID;
	private MariaDB db;
	private JSONObject data,msgIn;
	private Map<String,Socket> onlineMap;
	
	public RequestDealer(Map<String,Socket> onlineMap,Socket socket,JSONObject msgIn,MariaDB db,String MID){
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
		requestDealer();
	}
	private boolean requestDealer(){
		if(classs.equals(MsgReq.GetMomentsFriends)){
			System.out.println("GetMomentsFriends not work now");
		}
		else if(classs.equals(MsgReq.GetMomentsNearby)){
			sendMomentsNearby();
		}
		else if(classs.equals(MsgReq.GetMomentsComment)){
			sendComments();
		}
		else if(classs.equals(MsgReq.GetMomentsById)){
			sendMomentsByid();
		}
		else if(classs.equals(MsgReq.AddFriends)){
			//addFriends();//abortd
			String target=data.getJSONObject("Value").getString("Target");
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
		else if(classs.equals(MsgReq.RemoveFriends)){
			removeFriends();
		}
		else if(classs.equals(MsgReq.SearchFriendById)){
			searchById();
		}
		else if(classs.equals(MsgReq.SearchFriendNearby)){
			searchNearBy();
		}
		else if(classs.equals(MsgReq.SearchFriendByNickname)){
			searchNearByNickname();
		}
		else if(classs.equals(MsgReq.AddBlackList)){
			System.out.println("AddBlackList not work now");
		}
		else if(classs.equals(MsgReq.RemoveBlackList)){
			System.out.println("RemoveBlackList not work now");
		}
		else if(classs.equals(MsgReq.CheckProfile)){
			sendProfileByID();
		}
		else if(classs.equals(MsgReq.SendFile)){
			//remain to deal 2017.6.4
		}
		else if(classs.equals(MsgReq.ResetPassword)){
			resetPassword();
		}
		else{
			System.out.println("Error:Unkown request");
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
	private boolean sendProfileByID(){
		try{
			synchronized(db){
				rs=db.query("select * from profile where id='"+UID+"'");
				if(rs.next()){
					//Action Access!
					JSONObject msgOut=new JSONObject();
					msgOut.put("MsgType", MsgType.SERVER_RES);
					msgOut.put("MsgId", MID);
					msgOut.put("UID", PackageConstants.SERVER_ID);
						JSONObject data=new JSONObject();
						data.put("Class",MsgRes.ProfileResponse);
							JSONObject value=new JSONObject();
							value.put("Id", rs.getString(1));
							value.put("NickName", rs.getString(2));
							value.put("Avatar", rs.getString(3));//Remain to deal use http
							//String filename=rs.getString(3);
							//String avatarURL=PackageConstants.rootURL+"avatar/"+filename;
							value.put("Note", rs.getString(4));
							value.put("RegTime", rs.getString(5));
							value.put("Hobby", rs.getString(6));
							value.put("Masters", rs.getString(7));
							value.put("Learning", rs.getString(8));
							value.put("Country", rs.getString(9));
							value.put("Province", rs.getString(10));
							value.put("City", rs.getString(11));
							value.put("Address", rs.getString(12));
							value.put("Adcode", rs.getString(13));
							value.put("LocPrivacy", rs.getString(14));
							value.put("School", rs.getString(15));
							value.put("Organization", rs.getString(16));
							value.put("Education", rs.getString(17));
							value.put("JobPrivacy", rs.getString(18));
							value.put("RealName", rs.getString(19));
							value.put("Birth", rs.getString(20));
							value.put("Phone", rs.getString(21));
							value.put("Email", rs.getString(22));
							value.put("Sex", rs.getString(23));
							value.put("IndiPrivacy", rs.getString(24));
							value.put("FirstLt", rs.getString(25));
						data.put("Value", value);
						data.put("Day", TimeManager.getSqlDate());
						data.put("Time",TimeManager.getTime());
					msgOut.put("Data", data);
					out.write(msgOut+"\n");out.flush();
					//sendPushMsg(MsgPush.Success);
					return true;
				}
				else{
					//Action Failed!
					System.out.println("Error:Profile send");
					sendPushMsg(MsgPush.Fail);
					return false;
				}
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
	private boolean searchNearBy(){
		try{
			int i=0,j=0;
			boolean hasSend=false;
			JSONObject value0=data.getJSONObject("Value");
			int begin=value0.getInt("Begin");
			int Uad=value0.getInt("Adcode");
			synchronized(db){
				//V2- profile->online
				rs=db.query("select * from profile where adcode-"+Uad+"<20 or "+Uad+"-adcode>-20");
				while(rs.next()){
					//Action Access!
					if(i++ < begin)continue;
					if(j++ >= 10)break;//sum data to send
					JSONObject msgOut=new JSONObject();
					msgOut.put("MsgType", MsgType.SERVER_RES);
					msgOut.put("MsgId", MID);
					msgOut.put("UID", PackageConstants.SERVER_ID);
						JSONObject data=new JSONObject();
						data.put("Class",MsgRes.ProfileResponse);
							JSONObject value=new JSONObject();
							value.put("Id", rs.getString(1));
							value.put("NickName", rs.getString(2));
							value.put("Avatar", rs.getString(3));//Remain to deal use http
							//String picURL=PackageConstants.rootURL+"avatar/"+rs.getString(3);
							value.put("Note", rs.getString(4));
							value.put("RegTime", rs.getString(5));
							value.put("Hobby", rs.getString(6));
							value.put("Masters", rs.getString(7));
							value.put("Learning", rs.getString(8));
							value.put("Country", rs.getString(9));
							value.put("Province", rs.getString(10));
							value.put("City", rs.getString(11));
							value.put("Address", rs.getString(12));
							value.put("Adcode", rs.getString(13));
							value.put("LocPrivacy", rs.getString(14));
							value.put("School", rs.getString(15));
							value.put("Organization", rs.getString(16));
							value.put("Education", rs.getString(17));
							value.put("JobPrivacy", rs.getString(18));
							value.put("RealName", rs.getString(19));
							value.put("Birth", rs.getString(20));
							value.put("Phone", rs.getString(21));
							value.put("Email", rs.getString(22));
							value.put("Sex", rs.getString(23));
							value.put("IndiPrivacy", rs.getString(24));
							value.put("FirstLt", rs.getString(25));
						data.put("Value", value);
						data.put("Day", TimeManager.getSqlDate());
						data.put("Time",TimeManager.getTime());
					msgOut.put("Data", data);
					out.write(msgOut+"\n");out.flush();
					System.out.println("Find People nearby:\n"+msgOut);
					Thread.sleep(500);
					hasSend=true;
				}
			}
			if(hasSend){
				sendPushMsg(MsgPush.Success);
				return true;
			}
			else{
				//Action Faile! Not Exist
				sendPushMsg(MsgPush.Fail);
				System.out.println("Error:Addfriends by Id fail-Not Exist");
				return false;
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
	private boolean searchNearByNickname(){
		try{
			int i=0,j=0;
			boolean hasSend=false;
			JSONObject value0=data.getJSONObject("Value");
			int begin=value0.getInt("Begin");
			String TargetID=value0.getString("TargetId");
			synchronized(db){
				rs=db.query("select * from profile where nick_name='"+TargetID+"'");
				while(rs.next()){
					//Action Access!
					if(i++ < begin)continue;
					if(j++ >= 10)break;//sum data to send
					JSONObject msgOut=new JSONObject();
					msgOut.put("MsgType", MsgType.SERVER_RES);
					msgOut.put("MsgId", MID);
					msgOut.put("UID", PackageConstants.SERVER_ID);
						JSONObject data=new JSONObject();
						data.put("Class",MsgRes.ProfileResponse);
							JSONObject value=new JSONObject();
							value.put("Id", rs.getString(1));
							value.put("NickName", rs.getString(2));
							value.put("Avatar", rs.getString(3));//Remain to deal
							//String picURL=PackageConstants.rootURL+"avatar/"+rs.getString(3);
							value.put("Note", rs.getString(4));
							value.put("RegTime", rs.getString(5));
							value.put("Hobby", rs.getString(6));
							value.put("Masters", rs.getString(7));
							value.put("Learning", rs.getString(8));
							value.put("Country", rs.getString(9));
							value.put("Province", rs.getString(10));
							value.put("City", rs.getString(11));
							value.put("Address", rs.getString(12));
							value.put("Adcode", rs.getString(13));
							value.put("LocPrivacy", rs.getString(14));
							value.put("School", rs.getString(15));
							value.put("Organization", rs.getString(16));
							value.put("Education", rs.getString(17));
							value.put("JobPrivacy", rs.getString(18));
							value.put("RealName", rs.getString(19));
							value.put("Birth", rs.getString(20));
							value.put("Phone", rs.getString(21));
							value.put("Email", rs.getString(22));
							value.put("Sex", rs.getString(23));
							value.put("IndiPrivacy", rs.getString(24));
							value.put("FirstLt", rs.getString(25));
						data.put("Value", value);
						data.put("Day", TimeManager.getSqlDate());
						data.put("Time",TimeManager.getTime());
					msgOut.put("Data", data);
					out.write(msgOut+"\n");out.flush();
					//sendPushMsg(MsgPush.Success);
					System.out.println("Find People nickname:\n"+msgOut);
					Thread.sleep(500);
					hasSend=true;
				}
			}
			if(!hasSend){
				//Action Failed!
				System.out.println("Error:Search by Id fail or no rusult");
				sendPushMsg(MsgPush.Fail);
				return false;
			}
			else if(hasSend){
				sendPushMsg(MsgPush.Success);
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
	private boolean searchById(){
		try{
			synchronized(db){
				String TargetID=data.getJSONObject("Value").getString("TargetId");
				rs=db.query("select * from profile where id='"+TargetID+"'");
				if(rs.next()){
					//Action Access!
					JSONObject msgOut=new JSONObject();
					msgOut.put("MsgType", MsgType.SERVER_RES);
					msgOut.put("MsgId", MID);
					msgOut.put("UID", PackageConstants.SERVER_ID);
						JSONObject data=new JSONObject();
						data.put("Class",MsgRes.ProfileResponse);
							JSONObject value=new JSONObject();
							value.put("Id", rs.getString(1));
							value.put("NickName", rs.getString(2));
							value.put("Avatar", rs.getString(3));//Remain to deal
							//String picURL=PackageConstants.rootURL+"avatar/"+rs.getString(3);
							value.put("Note", rs.getString(4));
							value.put("RegTime", rs.getString(5));
							value.put("Hobby", rs.getString(6));
							value.put("Masters", rs.getString(7));
							value.put("Learning", rs.getString(8));
							value.put("Country", rs.getString(9));
							value.put("Province", rs.getString(10));
							value.put("City", rs.getString(11));
							value.put("Address", rs.getString(12));
							value.put("Adcode", rs.getString(13));
							value.put("LocPrivacy", rs.getString(14));
							value.put("School", rs.getString(15));
							value.put("Organization", rs.getString(16));
							value.put("Education", rs.getString(17));
							value.put("JobPrivacy", rs.getString(18));
							value.put("RealName", rs.getString(19));
							value.put("Birth", rs.getString(20));
							value.put("Phone", rs.getString(21));
							value.put("Email", rs.getString(22));
							value.put("Sex", rs.getString(23));
							value.put("IndiPrivacy", rs.getString(24));
							value.put("FirstLt", rs.getString(25));
						data.put("Value", value);
						data.put("Day", TimeManager.getSqlDate());
						data.put("Time",TimeManager.getTime());
					msgOut.put("Data", data);
					out.write(msgOut+"\n");out.flush();
					//sendPushMsg(MsgPush.Success);
					System.out.println("Find People:\n"+msgOut);
					//Send Picture	
					/*try{
						ServerSocket fileServerSocket=new ServerSocket(PackageConstants.FILE_SERVER_PORT);
						fileServerSocket.setSoTimeout(10000);
						Socket fileSocket=fileServerSocket.accept();
						FileInputStream fr=new FileInputStream(new File(picURL));
						OutputStream out=fileSocket.getOutputStream();
						byte[] buf=new byte[1024];
						int len=0;
						while((len=fr.read(buf))!=-1){
							out.write(buf,0,len);
						}out.close();
						fr.close();fileServerSocket.close();fileServerSocket.close();
					}catch(Exception e){
						System.out.println("Error:Moments send avatar picture:Time out or Network not work");
						sendPushMsg(MsgPush.PicLost);
						e.printStackTrace();
						return false;
					}
					return true;*/
				}
				else{
					//Action Failed!
					System.out.println("Error:Search by Id fail");
					sendPushMsg(MsgPush.Fail);
					return false;
				}
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
	private boolean removeFriends(){
		try{
			String TargetID=data.getString("Value");
			synchronized(db){	
				rs=db.query("select * from friends where idb='"+TargetID+"' and ida='"+UID+"'"
						+ " or ida='"+TargetID+"' and idb='"+UID+"'");
			}
			if(rs.next()){
				//Action Success!
				synchronized (db) {
					//Insert friends List
					db.exec("delete from friends where(ida='"+UID+"' and idb='"+TargetID+"' or idb='"+UID+"' and ida='"+TargetID+"')");
				}
				sendPushMsg(MsgPush.Success);
				return true;
			}
			else{
				//Action Faile! Not Exsist
				sendPushMsg(MsgPush.Fail);
				System.out.println("Error:removefriends by Id fail-Not Exsist");
				return false;
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
	private boolean sendComments(){
		try{
			boolean hasSend=false;
			String momentsNo=data.getString("Value");
			synchronized(db){	
				rs=db.query("select * from comments where moments_no='"+momentsNo+"'");
				while(rs.next()){
					//Action Success!
					//sendPushMsg(MsgPush.Success);
					JSONObject msgOut=new JSONObject();
					msgOut.put("MsgType", MsgType.SERVER_RES);
					msgOut.put("MsgId", MID);
					msgOut.put("UID", PackageConstants.SERVER_ID);
						JSONObject data=new JSONObject();
						data.put("Class",MsgRes.CommentResponse);
							JSONObject value=new JSONObject();
							value.put("CommentsNo", rs.getString(1));
							value.put("MomentsNo", rs.getString(2));
							value.put("Id", rs.getString(3));
							value.put("Text", rs.getString(4));
							value.put("Location", rs.getString(5));
							value.put("Time", rs.getString(6));
							value.put("Date", rs.getString(7));
							value.put("Privacy", rs.getString(8));
							String nickName=rs.getString(3);
							ResultSet rs2=db.getNewStatement().executeQuery("select nick_name from profile where id='"+nickName+"'");
							if(rs2.next()){
								nickName=rs2.getString(1);
							}
							value.put("NickName",nickName);
						data.put("Value", value);
						data.put("Day", TimeManager.getSqlDate());
						data.put("Time",TimeManager.getTime());
					msgOut.put("Data", data);
					
					out.write(msgOut+"\n");out.flush();
					hasSend=true;
				}
			}
			if(hasSend){
				sendPushMsg(MsgPush.Success);
				return true;
			}
			else{
				//Action Faile! Not Exist
				sendPushMsg(MsgPush.Fail);
				System.out.println("Error:SendComments by null fail-Not Exist");
				return false;
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
	private boolean sendMomentsNearby(){
		int i=0,j=0;
		boolean hasSend=false;
		JSONObject value0=data.getJSONObject("Value");
		int begin=value0.getInt("Begin");
		int Uad=value0.getInt("Adcode");
		try{
			synchronized(db){	
				rs=db.query("select * from moments where adcode-"+Uad+"<20 or "+Uad+"-adcode>-20");
			}
			while(rs.next()){
				//Action Success!
				if(i++ < begin)continue;
				if(j++ >= 10)break;//sum data to send
				JSONObject msgOut=new JSONObject();
				msgOut.put("MsgType", MsgType.SERVER_RES);
				msgOut.put("MsgId", MID);
				msgOut.put("UID", PackageConstants.SERVER_ID);
					JSONObject data=new JSONObject();
					data.put("Class",MsgRes.MomentResponse);
						JSONObject value=new JSONObject();
						value.put("MomentsNo", rs.getString(1));
						value.put("Id", rs.getString(2));
						value.put("Text", rs.getString(3));
						value.put("PicNum", rs.getString(4));
						value.put("Liked", rs.getString(5));
						value.put("Comments", rs.getString(6));
						value.put("IsLike", rs.getString(7));
						value.put("Pic", rs.getString(8));//Remain to deal
						value.put("Location", rs.getString(9));
						value.put("Adcode", rs.getString(10));
						value.put("Time", rs.getString(11));
						value.put("Date", rs.getString(12));
						value.put("Privacy", rs.getString(13));
						String nickName=rs.getString(2);
						ResultSet rs2=db.getNewStatement().executeQuery("select nick_name from profile where id='"+nickName+"'");
						if(rs2.next()){
							nickName=rs2.getString(1);
						}
						value.put("NickName",nickName);
					data.put("Value", value);
					data.put("Day", TimeManager.getSqlDate());
					data.put("Time",TimeManager.getTime());
				msgOut.put("Data", data);
				out.write(msgOut+"\n");out.flush();
				hasSend=true;
				//sendPushMsg(MsgPush.Success);
				Thread.sleep(500);
			}
			if(hasSend){
				sendPushMsg(MsgPush.Success);
				return true;
			}
			else{
				//Action Faile! Not Exist
				sendPushMsg(MsgPush.Fail);
				System.out.println("Error:Send Moments by near fail-Not Exist");
				return false;
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
	private boolean sendMomentsByid(){
		int j=0;
		boolean hasSend=false;
		JSONObject value0=data.getJSONObject("Value");
		String targetId=value0.getString("TargetId");
		try{
			synchronized(db){	
				rs=db.query("select * from moments where id='"+targetId+"' order by date and time DESC");
				while(rs.next()){
					//Action Success!
					if(j++ >= 5)break;//sum data to send
					JSONObject msgOut=new JSONObject();
					msgOut.put("MsgType", MsgType.SERVER_RES);
					msgOut.put("MsgId", MID);
					msgOut.put("UID", PackageConstants.SERVER_ID);
						JSONObject data=new JSONObject();
						data.put("Class",MsgRes.MomentResponse);
							JSONObject value=new JSONObject();
							value.put("MomentsNo", rs.getString(1));
							value.put("Id", rs.getString(2));
							value.put("Text", rs.getString(3));
							value.put("PicNum", rs.getString(4));
							value.put("Liked", rs.getString(5));
							value.put("Comments", rs.getString(6));
							value.put("IsLike", rs.getString(7));
							value.put("Pic", rs.getString(8));//Remain to deal
							//String filename=rs.getString(8);
							//String picURL=PackageConstants.rootURL+"momentpic/"+filename;
							value.put("Location", rs.getString(9));
							value.put("Adcode", rs.getString(10));
							value.put("Time", rs.getString(11));
							value.put("Date", rs.getString(12));
							value.put("Privacy", rs.getString(13));
							String nickName=rs.getString(2);
							ResultSet rs2=db.getNewStatement().executeQuery("select nick_name from profile where id='"+nickName+"'");
							if(rs2.next()){
								nickName=rs2.getString(1);
							}
							value.put("NickName",nickName);
						data.put("Value", value);
						data.put("Day", TimeManager.getSqlDate());
						data.put("Time",TimeManager.getTime());
					msgOut.put("Data", data);
					out.write(msgOut+"\n");out.flush();
					hasSend=true;
					//sendPushMsg(MsgPush.Success);
					Thread.sleep(500);
					//Send Picture	
					/*if(filename.equals("null"))continue;
					try{
						ServerSocket fileServerSocket=new ServerSocket(PackageConstants.FILE_SERVER_PORT);
						fileServerSocket.setSoTimeout(10000);
						Socket fileSocket=fileServerSocket.accept();
						FileInputStream fr=new FileInputStream(new File(picURL));
						OutputStream out=fileSocket.getOutputStream();
						byte[] buf=new byte[1024];
						int len=0;
						while((len=fr.read(buf))!=-1){
							out.write(buf,0,len);
						}out.close();
						fr.close();fileServerSocket.close();fileServerSocket.close();
					}catch(Exception e){
						System.out.println("Error:Moments send picture:Time out or Network not work");
						sendPushMsg(MsgPush.PicLost);
						e.printStackTrace();
						break;
					}*/
				}
			}
			if(hasSend){
				sendPushMsg(MsgPush.Success);
				return true;
			}
			else{
				//Action Faile! Not Exist
				sendPushMsg(MsgPush.Fail);
				System.out.println("Error:Send Moments by id fail-Not Exist");
				return false;
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
	private boolean resetPassword(){
		JSONObject value=data.getJSONObject("Value");
		String id=value.getString("Account");
		String opw=value.getString("OldPassword");
		String npw=value.getString("NewPassword");
		try{
			synchronized(db){	
				rs=db.query("select * from user where id='"+id+"' and password='"+opw+"'");
			}
			if(rs.next()){
				synchronized(db) {
	            //update user table
					ps=db.getConnection().prepareStatement("update user set password=? where id=? and password=?");
					ps.setString(1,npw);
					ps.setString(2,id);
					ps.setString(3,opw);
					ps.executeUpdate();
					System.out.println("Log:"+id+"has reset his password to "+npw);
					sendPushMsg(MsgPush.ResetPWSuccess);
					return true;
				}
			}
			else{
				sendPushMsg(MsgPush.ResetPWFailed);
			}
		}catch(Exception e){
			sendPushMsg(MsgPush.ResetPWFailed);
			e.printStackTrace();
		}
		return false;
	}
}
