package com.nancyse.controller.NewServer.Util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
		//获取当前时间
		public static String  getCurrentTimeAsString() {
			Date currentTime = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
			String dateString = formatter.format(currentTime);
			return dateString;
		}
		
		//获取当前时间
		public static Date  getCurrentTimeAsDate() {
			Date currentTime = new Date();
			return currentTime;
		}
		
		//时间字符串转换为Date
		public static Date strToDate(String strDate) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
			ParsePosition pos = new ParsePosition(0);
			Date strtodate = formatter.parse(strDate,pos);
			return strtodate;
		}
		
		
		//比较两个日期相差的天数
		public static long date1ToDate2(Date date1, Date date2) {
			//计算间隔多少天，则除以毫秒到天的转换公式
			long days = (date1.getTime()-date2.getTime())/(1000*60*60*24);
			
			return days<0 ? -1*days :days;
		}
		
		
}
