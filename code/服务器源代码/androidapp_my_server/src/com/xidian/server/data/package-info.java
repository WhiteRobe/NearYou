/**
 * Copyright (C), 2017-2018, Xidian University, Xian, China
 * 
 * @author Dai
 * @date 2017-4-23
 */
package com.xidian.server.data;

class PackageInfo{  
    public void common(){  
        System.out.println("Cteate by Dai on 2017-4-23");  
    }  
} 

class PackageConstants{
	public static final int SERVER_PORT=12345;
	public static final int FILE_SERVER_PORT=12344;
	public static final String SERVER_ID="ROOT";
	public static final String CLIENT_ID="CLIENT";
	public static final int fileDelay=60000;
	public static final int beatSecond=45;
	public static String keystorePassword = "server";
	
	//Mypc
	public static final String rootURL="D:/Tomcat8.0/webapps/Myserver/";
	public static String keystorePath = "D:/eclipse/jdk8/bin/server.ks";
	public static String trustKeystorePath = "D:/eclipse/jdk8/bin/tclient.jks";
	/*
	//Ali
	public static final String rootURL="/usr/apache-tomcat-8.0.43/webapps/Myserver/";
	public static String keystorePath = "/user/ftpme/write/server.ks";
	public static String trustKeystorePath = "/user/ftpme/write/tclient.jks";*/
	
	
}
class MsgType{
	//Message Type
	public static final String BEAT="0";
	public static final String LOGIN="1";
    public static final String REGISTER="2";
    public static final String CLIENT_RES="3";
    public static final String CLIENT_REQ="4";
    public static final String SERVER_RES="5";
    public static final String SERVER_REQ="6";
    public static final String CLIENT_PUSH="7";
    public static final String SERVER_PUSH="8";
    public static final String EMPTY="9";
    public static final String ALTER_PROFILE="10";
    public static final String MESSAGE="11";
    public static final String CLIENT_POST_MOMENTS="12";
    public static final String CLIENT_POST_COMMENT="13";
}
class MsgPush{
	//Message Push Content
	public static final String Success="1";
	public static final String Fail="2";
	public static final String LoginSuccess="3";
	public static final String LoginFailed="4";
	public static final String RegisterSuccess="5";
	public static final String RegisterFailed="6";
	public static final String UpdataSuccess="7";
	public static final String UpdataFailed="8";
	public static final String ClientOffline="9";
	public static final String VersionUpdata="10";
	public static final String FaceChecked="11";
	public static final String FileAccess="12";
	public static final String FileFailed="13";
	public static final String P2PAccess="14";
	public static final String P2PFailed="15";
	public static final String UpdataGeo="16";
	public static final String ACK="17";
	public static final String ValicodeWrong="18";
	public static final String AvatarLost="19";
	public static final String PicLost="20";
	public static final String MomentLikedOrUnlike="21";
	public static final String ResetPWSuccess="22";
	public static final String ResetPWFailed="23";
}
class MsgReq{
	//Message Request Content
	public static final String GetMomentsFriends="1";
	public static final String GetMomentsNearby="2";
	public static final String GetMomentsComment="3";
	public static final String AddFriends="4";
	public static final String RemoveFriends="5";
	public static final String SearchFriendById="6";
	public static final String SearchFriendNearby="7";
	public static final String SearchFriendByNickname="8";
	public static final String AddBlackList="9";
	public static final String RemoveBlackList="10";
	public static final String CheckProfile="11";
	public static final String SendFile="12";
	public static final String ResetPassword="13";
	public static final String GetMomentsById="14";
}
class MsgRes{
	//Message Response Content
	public static final String AllowFile="1";
	public static final String DenieFile="2";
	public static final String FileAccess="3";
	public static final String FileFailed="4";
	public static final String AcceptFriend="5";
	public static final String ProfileResponse="6";
	public static final String MomentResponse="7";
	public static final String CommentResponse="8";
}

class MsgName{
    public static final String[] MsgType=new String[]{
            "BEAT","LOGIN","REGISTER","CLIENT_RES","CLIENT_REQ",
            "SERVER_RES","SERVER_REQ","CLIENT_PUSH","SERVER_PUSH","EMPTY",
            "ALTER_PROFILE","MESSAGE","CLIENT_POST_MOMENTS","CLIENT_POST_COMMENT"
    };
    public static final String[] MsgPush=new String[]{
            "zero","Success","Fail","LoginSuccess","LoginFailed",
            "RegisterSuccess","RegisterFailed","UpdataSuccess","UpdataFailed","ClientOffline",
            "VersionUpdata","FaceChecked","FileAccess","FileFailed","P2PAccess",
            "P2PFailed","UpdataGeo","ACK","ValicodeWrong","AvatarLost",
            "PicLost","MomentLikedOrUnlike","ResetPWSuccess","ResetPWFailed"
    };
    public static final String[] MsgReq=new String[]{
            "zero","GetMomentsFriends","GetMomentsNearby","GetMomentsComment","AddFriends",
            "RemoveFriends","SearchFriendById","SearchFriendNearby","SearchFriendByNickname","AddBlackList",
            "RemoveBlackList","CheckProfile","SendFile","ResetPassword","GetMomentsById"
    };
    public static final String[] MsgRes=new String[]{
            "zero","AllowFile","DenieFile","FileAccess","FileFailed","AcceptFriend","ProfileResponse","MomentResponse","CommentResponse"
    };
}