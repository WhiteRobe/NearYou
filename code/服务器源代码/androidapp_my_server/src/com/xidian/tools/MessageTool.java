/**
 * Copyright (C), 2017-2018, Xidian University, Xian, China
 * 
 * @auther dai
 * @date 2017-4-23
 */
package com.xidian.tools;

import java.util.Iterator;
import java.util.Set;

public class MessageTool {
	/**
	 * get File Type
	 * @param fileName
	 * @return exp: .jpg
	 */
	public static String getType(String fileName){
		return fileName.substring(fileName.indexOf("."));
	}
	public static String mkMsg(Set<String> set){
		String msg="";
		Iterator<String> it=set.iterator();
		while(it.hasNext()){
			msg+=transMsg(it.next());
			msg+=PackageConstants.ESCAPE_STRING;
		}
		return msg+"\n";
	}
	/**
	 * Make Message with a String[].
	 * 
	 * @param String[]
	 * @return msg
	 */
	public static String mkMsg(String[] string){
		String msg="";
		int lenth=string.length;
		for(int i=0;i<lenth;i++){
			msg+=transMsg(string[i]);
			msg+=PackageConstants.ESCAPE_STRING;
		}
		return msg+"\n";
	}
	/**
	 * Pack message.
	 * 
	 * @param STRING
	 * @return transedMsg
	 */
	public static String transMsg(String STRING){
		return STRING.replace(PackageConstants.ESCAPE_STRING,PackageConstants.ESCAPE_STRING_TRANS_VALUE);
	}
	/**
	 * Unpack message.
	 * 
	 * @param STRING
	 * @return unTransedMsg
	 */
	public static String unTransMsg(String STRING){
		return STRING.replace(PackageConstants.ESCAPE_STRING_TRANS_VALUE,PackageConstants.ESCAPE_STRING);
	}
	/**
	 * Is the string contains PackageConstants.ESCAPE_STRING ?
	 * 
	 * @param STRING
	 * @return boolean
	 */
	public boolean checkEscape(String STRING){
		return STRING.contains(PackageConstants.ESCAPE_STRING);
	}
	/**
	 * Is the string contains PackageConstants.ESCAPE_STRING_TRANS_VALUE ?
	 * 
	 * @param STRING
	 * @return boolean
	 */
	public boolean checkEscapeTrans(String STRING){
		return STRING.contains(PackageConstants.ESCAPE_STRING_TRANS_VALUE);
	}
	/**
	 * Extract parts of msg_string from origin string.
	 * 
	 * @param msgString
	 * @param msgPart
	 * @return str
	 */
	public static String extractMsg(String msgString,int msgPart){
		String str="";
		int start=-PackageConstants.ESC_STRING_LENGTH,end=msgString.indexOf(PackageConstants.ESCAPE_STRING);
		for(int i=0;i<msgPart;i++){
			start=msgString.indexOf(PackageConstants.ESCAPE_STRING,start+PackageConstants.ESC_STRING_LENGTH);
			end=msgString.indexOf(PackageConstants.ESCAPE_STRING,end+PackageConstants.ESC_STRING_LENGTH);
		}
		if(end==-1)end=msgString.length();
		//System.out.println(start+PackageConstants.ESC_STRING_LENGTH+":"+end);//Test
		str=msgString.substring(start+PackageConstants.ESC_STRING_LENGTH,end);
		return unTransMsg(str);
	}

	public static String getMsgTypeName(String num){
		int index=Integer.parseInt(num);
		if(index>=MsgName.MsgType.length)return null;
		else return MsgName.MsgType[index];
	}
	public static String getMsgPushName(String num){
		int index=Integer.parseInt(num);
		if(index>=MsgName.MsgPush.length)return null;
		else return MsgName.MsgPush[index];
	}
	public static String getMsgReqName(String num){
		int index=Integer.parseInt(num);
		if(index>=MsgName.MsgReq.length)return null;
		else return MsgName.MsgReq[index];
	}
	public static String getMsgResName(String num){
		int index=Integer.parseInt(num);
		if(index>=MsgName.MsgRes.length)return null;
		else return MsgName.MsgRes[index];
	}
}
