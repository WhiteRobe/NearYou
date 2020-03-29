package com.xidian.server.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import org.json.JSONObject;

import com.xidian.database.DataBaseMannager;
import com.xidian.database.MariaDB;
import com.xidian.server.business.ClientPushDealer;
import com.xidian.server.business.CommentPoster;
import com.xidian.server.business.Loginer;
import com.xidian.server.business.MessageSender;
import com.xidian.server.business.MomentPoster;
import com.xidian.server.business.ProfileAlter;
import com.xidian.server.business.Register;
import com.xidian.server.business.RequestDealer;
import com.xidian.server.business.ResponseDealer;
import com.xidian.server.business.SendRemainMsgWoker;
import com.xidian.tools.MessageTool;
import com.xidian.tools.TimeManager;

public class JsonMsgSystemThread implements Runnable{
	private static Integer threadNum = new Integer(0);
	private PreparedStatement ps;
	private BufferedReader in = null;
    private BufferedWriter out = null;
    private String TCPip = null;//default ip
    private int TCPport = PackageConstants.SERVER_PORT;//default port
    private SSLSocket socket = null;
    private static MariaDB db = null;
    private int autoIncNum=0;
    private String UID;
    private static Map<String,Socket> onlineClientMap=new HashMap<String,Socket>();
    private String currentUsersID=null;
    private boolean ConnectionOver=false;
    /**
     * Create a new TCP socket with new client.
     * 
     * @param SOCKET
     */
    public JsonMsgSystemThread(SSLSocket clientSocket){
    	socket=clientSocket;
    	//get user ip&port
    	TCPip=clientSocket.getInetAddress().getHostAddress();
    	TCPport=socket.getPort();
    	//connect database
    	if(db==null){
    		try{
    			db=new DataBaseMannager().getMariaDB();
    		}catch(Exception e){
    			System.out.println("Log:01-Database Connecting Failed!\n");
    			return ;
    		}
    		System.out.println("Log:01-Database Connecting Access!\n");
    	}
    	threadNum++;
    }
    public void run(){
		try{
			socket.setKeepAlive(true);
			out =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));//Java UTF-8
			System.out.println("Log: "+TCPip+":"+TCPport+" has connected.-"+TimeManager.getTime());
			new Thread(new beating(ConnectionOver,out)).start();//Beating

			//sendEmptyJson();
			while(!ConnectionOver){
				if(socket.isConnected()&&socket.isClosed()){
					System.out.println("Log:Dead-"+TCPip+":"+TCPport);
					return ;//Is Client offline?
				}
		        
				if(true){
					String readIn=in.readLine();
					System.out.println("\nLog: "+TCPip+":"+TCPport+"-"+currentUsersID+" SendMsg:\n"+readIn);//Test-info-2
					if(readIn!=null){
						msgDispatcher(readIn);//Dispathch message
						//sendACK();
					}else {
						ConnectionOver=true;
						//System.out.println("Log:Empty Read");
					}
					readIn=null;
				}
				if(in.ready())System.out.println("Log:Wating next one");
			}
		}catch(IOException eio){
			eio.printStackTrace();
			System.out.println("Log: "+TCPip+":"+TCPport+" has disonnected-01:IOException. - "+TimeManager.getTime());
		}
		
		
		if(currentUsersID!=null){
			//If An Online User Disconnect whatever how
			clientSetOffline(currentUsersID);
		}
		System.out.println("Log: "+TCPip+":"+TCPport+" has disonnected -02: Exit Thread. - "+TimeManager.getTime());
		synchronized (threadNum) {
			threadNum--;
			System.out.println("Tip:Current still alive thered num = "+threadNum);
		}
		//thread end
	}
    
    private String getMID(){
    	if(autoIncNum>65535){
    		autoIncNum=0;
    	}
    	autoIncNum++;
    	return autoIncNum+"";
    }
    private void clientSetOnline(String id){
    	currentUsersID=id;
    	synchronized (onlineClientMap) {
    		onlineClientMap.put(id,socket);
		}
    	new Thread(new SendRemainMsgWoker(socket,db,currentUsersID)).start();
    	System.out.println("User Status: "+id+" - Online!");
    }
    private void clientSetOffline(String id){
    	//currentUsersID=null;
    	synchronized (onlineClientMap) {
    		onlineClientMap.remove(id,socket);
    		System.out.println("User Status: "+id+" - Offline!");
		}
    	synchronized (db) {
			try {
				ps=(PreparedStatement) db.getConnection().prepareStatement("delete from online where id=?");
				ps.setString(1,id);
				ps.executeUpdate();
				System.out.println(id+"-onlinet list removed");
				in.close();
				socket.close();
			} catch (Exception e) {e.printStackTrace();}
		}
    	ConnectionOver=true;
    	currentUsersID=null;
    }
    //Dispatch message
    private void msgDispatcher(String readIn) throws IOException{
		JSONObject msgIn = new JSONObject(readIn);
		String msgType=msgIn.getString("MsgType");//Message's Type
		System.out.println("==[MsgType : "+MessageTool.getMsgTypeName(msgType)+"]==");
		//UID=msgIn.getString("UID");//User's ID
		if(msgType.equals(MsgType.LOGIN)){
			if(new Loginer().loginer(socket,msgIn,db,getMID())){
				//If Login Access
				UID=msgIn.getJSONObject("Data").getString("Account");
				clientSetOnline(UID);
			}
			else{
				//If Login Failed
				System.out.println("Login Failed");
			}
		}
		else if(msgType.equals(MsgType.REGISTER)){
			if(new Register().register(socket,msgIn,db,getMID())){
				//If Registe Access
				UID=msgIn.getJSONObject("Data").getString("Account");
				clientSetOnline(UID);
			}
			else{
				//If Registe Failed
				System.out.println("Register Failed");
			}
		}
		/*
		if(currentUsersID==null){
			//unlogin
			return false;
		}
		else */if(msgType.equals(MsgType.CLIENT_RES)){
			//Deal Response Message
			new Thread(new ResponseDealer(onlineClientMap,socket,msgIn,db,getMID())).start();
		}
		else if(msgType.equals(MsgType.CLIENT_REQ)){
			//Deal Request Message
			new Thread(new RequestDealer(onlineClientMap,socket,msgIn,db,getMID())).start();
		}
		else if(msgType.equals(MsgType.CLIENT_PUSH)){
			//Deal Push Message
			clientPushDealer(msgIn);
		}
		else if(msgType.equals(MsgType.EMPTY)){
			//sendACK();
		}
		else if(msgType.equals(MsgType.ALTER_PROFILE)){
			//Deal Profile update
			new Thread(new ProfileAlter(socket,msgIn,db,getMID())).start();
		}
		else if(msgType.equals(MsgType.MESSAGE)){
			//Resend Message
			new Thread(new MessageSender(onlineClientMap,socket,msgIn,getMID(),db)).start();
		}
		else if(msgType.equals(MsgType.CLIENT_POST_MOMENTS)){
			//Deal Moment Message
			new Thread(new MomentPoster(socket,msgIn,db,getMID())).start();
		}
		else if(msgType.equals(MsgType.CLIENT_POST_COMMENT)){
			//Deal Comment Message
			new Thread(new CommentPoster(socket,msgIn,db,getMID())).start();
		}
    }
    //Push Message Dealer
    private void clientPushDealer(JSONObject msgIn) throws IOException{
		String UID=msgIn.getString("UID");
		//String msgId=msgIn.getString("MsgId");
		JSONObject data=msgIn.getJSONObject("Data");
		String classs=data.getString("Class");
		if(classs.equals(MsgPush.ClientOffline)){
			clientSetOffline(UID);//offline
		}
		else if(classs.equals(MsgPush.FaceChecked)){
			clientSetOnline(UID);
			JSONObject msgOut=new JSONObject();
			msgOut.put("MsgType",MsgType.SERVER_PUSH);
			msgOut.put("MsgId",getMID());
			msgOut.put("UID",PackageConstants.SERVER_ID);
				JSONObject data2=new JSONObject();
				data2.put("Class",MsgPush.LoginSuccess);
				data2.put("Day",TimeManager.getSqlDate());
				data2.put("Time",TimeManager.getTime());
			msgOut.put("Data", data2);
			out.write(msgOut.toString()+"\n");
			out.flush();
		}
		else {
			new Thread(new ClientPushDealer(data, UID, db )).start();
		}
	}
    //Send ACK
    @SuppressWarnings("unused")
	private void sendACK() throws IOException{
    	JSONObject msgOut = new JSONObject();
		msgOut.put("MsgType",MsgType.SERVER_PUSH);
		msgOut.put("MsgId",getMID());
		msgOut.put("UID", PackageConstants.SERVER_ID);
			JSONObject data=new JSONObject();
			data.put("Class",MsgPush.ACK);
			data.put("Day",TimeManager.getSqlDate());
			data.put("Time",TimeManager.getTime());
		msgOut.put("Data", data);
		out.write(msgOut.toString()+"\n");out.flush();//ACK TRUE
    }
    //Send Empty
    @SuppressWarnings("unused")
	private void sendEmptyJson() throws IOException{
    	JSONObject msgOut = new JSONObject();
		msgOut.put("MsgType",MsgType.EMPTY);
		msgOut.put("MsgId",getMID());
		msgOut.put("UID", PackageConstants.SERVER_ID);
			JSONObject data=new JSONObject();
			data.put("Empty", "Empty");
		msgOut.put("Data",data);
		out.write(msgOut.toString()+"\n");out.flush();//EMPTY TRUE
    }
}

class beating implements Runnable{
	private boolean ConnectionOver=false;
	private BufferedWriter out;
	public beating(boolean ConnectionStatus,BufferedWriter out){
		this.ConnectionOver=ConnectionStatus;
		this.out=out;
	}
	public void run(){
		System.out.println("~=[BEATING Start]=~");
		while (!ConnectionOver){
            try{
                sendBeat();
                Thread.sleep(1000*PackageConstants.beatSecond);
            }catch (Exception e){
            	ConnectionOver=true;
                //e.printStackTrace();
            }
        }
		//System.out.println("BEATING over");
	}
	private void sendBeat() throws IOException{
    	JSONObject msgOut = new JSONObject();
		msgOut.put("MsgType",MsgType.BEAT);
		out.write(msgOut.toString()+"\n");out.flush();//EMPTY TRUE
    }
}