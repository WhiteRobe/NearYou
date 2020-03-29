package com.xidian.server.business;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import com.xidian.database.MariaDB;

public class ClientPushDealer implements Runnable{
	private MariaDB db;
	private JSONObject data;
	@SuppressWarnings("unused")
	private String msgID,UID,classs,day,time;
	private PreparedStatement ps;
	public ClientPushDealer(JSONObject data,String UID,MariaDB Db){
		this.db=Db;
		this.data=data;
		this.UID=UID;
	}
	public void run(){
		clientPushDealer();
	}
	public void clientPushDealer(){
		//day=data.getString("Day");
		//time=data.getString("Time");
		classs=data.getString("Class");
		if(classs.equals(MsgPush.UpdataGeo)&&!UID.endsWith("CLIENT")){
			JSONObject geo=data.getJSONObject("Value");
			String type=geo.getString("Type");
			if(type.equals("1")){
				onUpdateAdcode(geo.getString("GeoData"));
			}
			else if(type.equals("2")){
				onUpdateAddress(geo.getString("GeoData"));
			}
			else if(type.equals("3")){
				onUpdateLngLant(geo.getString("GeoDataX"),geo.getString("GeoDataY"));
			}
		}
		else if(classs.equals(MsgPush.MomentLikedOrUnlike)){
			JSONObject value=data.getJSONObject("Value");
			int momentsNo=Integer.parseInt(value.getString("MomentsNo"));
			String isLike=value.getString("IsLike");
			try {
				synchronized(db) {
					if(isLike.equals("true")){
						//like +1
						ps=db.getConnection().prepareStatement("update moments set liked=liked+1,li_like=1 where moments_no=?");
		        	}
					else{
						//like -1
						ps=db.getConnection().prepareStatement("update moments set liked=liked-1,is_like=0 where moments_no=?");
					}
					ps.setInt(1,momentsNo);
					ps.executeQuery();
				}
			}catch (SQLException e) {
				System.out.println("Error: Liked Action Fail");
				e.printStackTrace();
			}
		}
		
	}
	
	@SuppressWarnings("unused")
	private void onEnterGeoFence(){
		;
	}
	
	private synchronized void onUpdateAdcode(String ad){
		try{
			ps=db.getConnection().prepareStatement("updata online set adcode=? where(id=?);");
			ps.setString(1,ad);
			ps.setString(2,UID);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private synchronized void onUpdateAddress(String address){
		try{
			ps=db.getConnection().prepareStatement("updata online set address=? where(id=?);");
			ps.setString(1,address);
			ps.setString(2,UID);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private synchronized void onUpdateLngLant(String x,String y){
		try{
			ps=db.getConnection().prepareStatement("updata online set longititude=?,latititude=? where(id=?);");
			ps.setString(1,x);
			ps.setString(2,y);
			ps.setString(3,UID);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
