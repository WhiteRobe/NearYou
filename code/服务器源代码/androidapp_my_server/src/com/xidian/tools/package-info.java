/**
 * Copyright (C), 2017-2018, Xidian University, Xian, China
 *
 * @author Dai
 * @date 2017-4-23
 */
package com.xidian.tools;
class PackageInfo{  
    public void common(){  
        System.out.println("Cteate by Dai on 2017-4-23");  
    }  
} 

class PackageConstants{
	public static final String ESCAPE_STRING="}(]";
	public static final String ESCAPE_STRING_TRANS_VALUE="mY3}(#]sElFvAluE";
    public static final int ESC_STRING_LENGTH=ESCAPE_STRING.length();
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