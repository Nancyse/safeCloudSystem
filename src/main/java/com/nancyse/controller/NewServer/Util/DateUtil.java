package com.nancyse.controller.NewServer.Util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
		//��ȡ��ǰʱ��
		public static String  getCurrentTimeAsString() {
			Date currentTime = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
			String dateString = formatter.format(currentTime);
			return dateString;
		}
		
		//��ȡ��ǰʱ��
		public static Date  getCurrentTimeAsDate() {
			Date currentTime = new Date();
			return currentTime;
		}
		
		//ʱ���ַ���ת��ΪDate
		public static Date strToDate(String strDate) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
			ParsePosition pos = new ParsePosition(0);
			Date strtodate = formatter.parse(strDate,pos);
			return strtodate;
		}
		
		
		//�Ƚ�����������������
		public static long date1ToDate2(Date date1, Date date2) {
			//�����������죬����Ժ��뵽���ת����ʽ
			long days = (date1.getTime()-date2.getTime())/(1000*60*60*24);
			
			return days<0 ? -1*days :days;
		}
		
		
}
