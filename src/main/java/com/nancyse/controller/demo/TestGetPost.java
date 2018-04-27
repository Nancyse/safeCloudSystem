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
	 * 向指定URL发送GET方法的请求
	 * 
	 */
	public static String sendGet(String url, String param) {
		String result="";
		BufferedReader in = null;
		try {
			String urlName=url+"?"+param;
			URL realUrl = new URL(urlName);
			//打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			//设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//建立实际连接
			conn.connect();
			//获取响应头字段
			Map<String,List<String>> map = conn.getHeaderFields();
			//遍历所有的响应字段
			for(String key:map.keySet()) {
				System.out.println(key+"--->"+map.get(key));
			}
			//定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while((line=in.readLine())!=null) {
				result += "\n"+line;
			}
			
		}catch(Exception e) {
			System.out.println("发送GET请求出现异常！"+e);
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
			//打开和url的链接
			URLConnection conn = realUrl.openConnection();
			//设置通用属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//发送POST请求必须设置的两行
			conn.setDoOutput(true);
			conn.setDoOutput(true);
			//获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			//发送请求参数
			out.print(param);
			//flush输出流的缓冲
			out.flush();
			//定义BufferedReader输入流来读取URL响应
			in= new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while( (line=in.readLine())!=null) {
				result+="\n"+line;
			}
			
		}catch(Exception e) {
			System.out.println("发送Post请求出现异常！"+e);
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
		//发送GET请求
		String s = TestGetPost.sendGet("http://localhost:8080/safeCloudSystem/new/testForward", null);
		System.out.println(s);
		//发送POST请求
		String s1 = TestGetPost.sendPost("http://localhost:8080/", "user=理当&pass=abc");
		System.out.println(s1);
		
	}

}
