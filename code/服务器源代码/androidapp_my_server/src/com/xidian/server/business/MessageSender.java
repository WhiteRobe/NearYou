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
import java.util.Map;

import org.json.JSONObject;

import com.xidian.database.MariaDB;
import com.xidian.tools.TimeManager;

public class MessageSender implements Runnable{
	private Socket socketT;
	private JSONObject msgIn,data;
	private BufferedWriter out;
	private String MID;
	private MariaDB db;
	private PreparedStatement ps;
	private String target;
	
	public MessageSender(Map<String,Socket> onlineMap,Socket socketO,JSONObject msgIn,String MID,MariaDB db) {
		this.data=msgIn.getJSONObject("Data");
		synchronized (onlineMap) {
			this.socketT=onlineMap.get(data.getString("Target"));
		}
		this.msgIn=msgIn;
		this.MID=MID;
		this.db=db;
		try {
			this.out=new BufferedWriter(new OutputStreamWriter(socketO.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		};
	}
	
	public void run(){
		try {
			String day=data.getString("Day");
			String time=data.getString("Value");//value = TimeManager.getSqltimSys()
			target=data.getString("Target");
			try{
				if(!data.getString("Class").equals("TXT")){
					//get file
					String type=data.getString("Type");
					String filename=day+time+msgIn.getString("UID")+"_file"+type;//such as 2017-11-11212112958_file.mp4
					String URL=PackageConstants.rootURL+"file/"+filename;
					//Wirte file
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
				};
			}catch (Exception e123) {
				e123.printStackTrace();
				sendPushMsg(MsgPush.FileFailed);
			}
			if(socketT==null){
				//target offline - Send to MessagePool
				synchronized (db) {
					try{
						ps=db.getConnection().prepareStatement("insert into messagepool (id,msg) values(?,?)");
						ps.setString(1,target);
						ps.setString(2,msgIn.toString());
						ps.executeUpdate();
						System.out.println("Taget offline , Message Saved !");
					}catch (Exception e) {
						e.printStackTrace();
						System.out.println("Taget offline , Message Saved ! - Fail");
					}
				}
			}else{
				//resendmsg
				BufferedWriter outTarget =new BufferedWriter(new OutputStreamWriter(socketT.getOutputStream(),"UTF-8"));
				outTarget.write(msgIn.toString()+"\n");outTarget.flush();//ACK TRUE
			}
			sendPushMsg(MsgPush.Success);
		} catch (IOException e) {
			//e.printStackTrace();
			try{
				ps=db.getConnection().prepareStatement("insert into messagepool (id,msg) values(?,?)");
				ps.setString(1,target);
				ps.setString(2,msgIn.toString());
				ps.executeUpdate();
				System.out.println("Taget offline , Message Saved ! - With 2nd Try");
			}catch (Exception e1) {
				e1.printStackTrace();
				System.out.println("Taget offline , Message Saved ! - With 2nd Try - Fail");
			}
			sendPushMsg(MsgPush.Fail);
		}
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
}
