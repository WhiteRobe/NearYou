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

public class MomentPoster implements Runnable{
	private PreparedStatement ps;
	private BufferedWriter out;
	@SuppressWarnings("unused")
	private String msgId;
	private String text;
	private String location;
	private String adcode;
	private String privacy;
	private String day,time;
	@SuppressWarnings("unused")
	private String upLoadPicture,type,picNum;
	private String MID,UID;
	private JSONObject msgIn,data;
	private MariaDB db;
	
	public MomentPoster(Socket socket,JSONObject msgIn,MariaDB db,String MID){
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		};
		this.db=db;
		this.MID=MID;
		this.msgIn=msgIn;
		msgId=msgIn.getString("MsgId");
		UID=msgIn.getString("UID");
	}
	
	public void run(){
		data=msgIn.getJSONObject("Data");
		getData();
		momentPoster();
	}
	private boolean momentPoster(){
		try{
			synchronized(db) {
	            //Insert moments table
	            ps=db.getConnection().prepareStatement("insert into moments ("
	            		+ "id,text,picnum,location,adcode,time,date,privacy"
	            		+ ") values(?,?,?,?,?,?,?,?)");
	            ps.setString(1,UID);
	            ps.setString(2,text);
	            ps.setString(3,picNum);
	            ps.setString(4,location);
	            ps.setString(5,adcode);
	            ps.setString(6,time);
	            ps.setString(7,day);
	            ps.setString(8,privacy);
	            ps.executeUpdate();
	            //Try todo make them atom - Dai
	        }
			//sendPushMsg(MsgPush.Success);
			int getPicNum=Integer.parseInt(picNum);
			if(getPicNum>0){
				type=data.getString("Type");
				String firstFileName=day+TimeManager.getTimeSys()+UID+type;//such sa 2015-11-11211515958.jpg
				int successNum=0;
				try{
					synchronized(db) {
						//Insert pic-info into moments table
						ServerSocket fileServerSocket=new ServerSocket(PackageConstants.FILE_SERVER_PORT);
						fileServerSocket.setSoTimeout(PackageConstants.fileDelay);
						for(int num=1;num<=getPicNum;num++){
							Socket fileSocket=fileServerSocket.accept();
							InputStream in = fileSocket.getInputStream();
							String filename=num+firstFileName;//such sa 12015-11-11211515958.jpg
							String URL=PackageConstants.rootURL+"momentpic/"+filename;
							//Wirte Picture - Moment
							FileOutputStream fw=new FileOutputStream(new File(URL));
							byte[] buf=new byte[1024];
							int len=0;
							while((len=in.read(buf))!=-1){
								fw.write(buf,0,len);
							}
							fw.flush();fw.close();
							fileSocket.close();
							successNum++;
						}		
						fileServerSocket.close();
					}
				}catch (Exception e) {
					e.printStackTrace();
					sendPushMsg(MsgPush.PicLost);
					return false;
				}
				ps=db.getConnection().prepareStatement("update moments set pic=?,picNum=? where id=? and date=? and time=?");
				ps.setString(1,"1"+firstFileName);
				ps.setString(2,""+successNum);
				ps.setString(3,UID);
				ps.setString(4,day);
				ps.setString(5,time);
				ps.executeUpdate();
			}
			sendPushMsg(MsgPush.Success);
			return true;
		}catch(SQLException e1){
			//MomentsPoster Failed Caused by database error.
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
		text=data.getString("Text");
		picNum=data.getString("PicNum");
		location=data.getString("Location");
		adcode=data.getString("Adcode");
		privacy=data.getString("Privacy");
		day=data.getString("Day");
		time=data.getString("Time");
	}
}
