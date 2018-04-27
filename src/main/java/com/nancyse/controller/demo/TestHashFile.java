package com.nancyse.controller.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class TestHashFile {
	public static String getSHA256StrJava(String str)
	{
		MessageDigest messageDigest;
		String encodeStr="";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			encodeStr=byte2Hex(messageDigest.digest());
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return encodeStr;
	}
	
	/*
	 * 将byte转为16进制
	 * @param bytes
	 * @return
	 */
	private static String byte2Hex(byte[] bytes)
	{
		StringBuffer stringBuffer = new StringBuffer();
		String temp=null;
		for( int i=0;i<bytes.length;i++)
		{
			temp=Integer.toHexString(bytes[i]&0xFF);
			if(temp.length()==1)  //当转换后为个位数时，自动在该数字后面添加0
			{
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}
	
	//获取文件数据
	public static String getFileData(InputStream in) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		InputStreamReader isr = new InputStreamReader(in);
		int ch=0;
		while((ch=isr.read())!=-1)
		{
			sb.append(ch);
		}
		return sb.toString();
		//
	}
	
	public static void main(String[] args) throws IOException
	{
		File file=new File("src/org/nancyse/fundation/graduationProject/TestHashFile.java");
		FileInputStream fis = new FileInputStream(file);
		String fileData = getFileData(fis);
		String hash=getSHA256StrJava(fileData);
		System.out.println(hash);	
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		System.out.println(num);
		String key=hash+hash.substring(0,num);
		System.out.println(key);
	}
	

}
