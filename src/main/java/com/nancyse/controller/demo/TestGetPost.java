package com.nancyse.controller.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class TestGetPost {
	/*
	 * ��ָ��URL����GET����������
	 * 
	 */
	public static String sendGet(String url, String param) {
		String result="";
		BufferedReader in = null;
		try {
			String urlName=url+"?"+param;
			URL realUrl = new URL(urlName);
			//�򿪺�URL֮�������
			URLConnection conn = realUrl.openConnection();
			//����ͨ�õ���������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//����ʵ������
			conn.connect();
			//��ȡ��Ӧͷ�ֶ�
			Map<String,List<String>> map = conn.getHeaderFields();
			//�������е���Ӧ�ֶ�
			for(String key:map.keySet()) {
				System.out.println(key+"--->"+map.get(key));
			}
			//����BufferedReader����������ȡURL����Ӧ
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while((line=in.readLine())!=null) {
				result += "\n"+line;
			}
			
		}catch(Exception e) {
			System.out.println("����GET��������쳣��"+e);
			e.printStackTrace();
		}finally {
			try {
				if(in!=null)
					in.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static String sendPost(String url,String param) {
		String result=null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			//�򿪺�url������
			URLConnection conn = realUrl.openConnection();
			//����ͨ������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//����POST����������õ�����
			conn.setDoOutput(true);
			conn.setDoOutput(true);
			//��ȡURLConnection�����Ӧ�������
			out = new PrintWriter(conn.getOutputStream());
			//�����������
			out.print(param);
			//flush������Ļ���
			out.flush();
			//����BufferedReader����������ȡURL��Ӧ
			in= new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while( (line=in.readLine())!=null) {
				result+="\n"+line;
			}
			
		}catch(Exception e) {
			System.out.println("����Post��������쳣��"+e);
			e.printStackTrace();
		}
		finally {
			try {
				if(out!=null)
					out.close();
				if(in!=null)
					in.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		return result;
	}
	
	public static void main(String[] args) {
		//����GET����
		String s = TestGetPost.sendGet("http://localhost:8080/safeCloudSystem/new/testForward", null);
		System.out.println(s);
		//����POST����
		String s1 = TestGetPost.sendPost("http://localhost:8080/", "user=��&pass=abc");
		System.out.println(s1);
		
	}

}
