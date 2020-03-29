/**
 * Copyright (C), 2017-2018, Xidian University, Xian, China
 * 
 * @auther dai
 * @date 2017-6-3
 */
package com.xidian.tools;

import java.util.Calendar;
import java.util.Date;

public class TimeManager {
	/**
	 * Get time
	 * @return Hh:Mm:Ss ep:14:32:49
     */
	public static String getTime(){
		Calendar c= Calendar.getInstance();
		return c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND);
	}
	/**
	 * Get time
	 * @return Hh:Mm:Ss ep:143249
     */
	public static String getTimeSys(){
		Calendar c= Calendar.getInstance();
		return c.get(Calendar.HOUR_OF_DAY)+""+c.get(Calendar.MINUTE)+""+c.get(Calendar.SECOND);
	}
	/**
	 * Get day
	 * @return String[] Hh-Mm-Ss ep:14 32 49
     */
	public static String[] getTimes(){
		Calendar c= Calendar.getInstance();
		String[] str=new String[3];
		str[0]=c.get(Calendar.HOUR_OF_DAY)+"";
		str[1]=c.get(Calendar.MINUTE)+"";
		str[2]=c.get(Calendar.SECOND)+"";
		return str;
	}
	/**
	 * Get day
	 * @return String[] Year-Month-Year ep:2017 6 3
     */
	public static String[] getDays(){
		Calendar c= Calendar.getInstance();
		String[] str=new String[3];
		str[0]=c.get(Calendar.YEAR)+"";
		str[1]=(c.get(Calendar.MONTH)+1)+"";
		str[2]=c.get(Calendar.DAY_OF_MONTH)+"";
		return str;
	}
	/**
	 * Get Sql-format System time
	 * 
	 * @return sqlDate
	 */
	public static String getSqlDate(){
		return ""+new java.sql.Date(new Date().getTime());
	}
}
