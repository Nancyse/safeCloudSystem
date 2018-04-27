package com.nancyse.controller.demo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CreateFileBlock {
	static String fileName="第一个文件";
	static String creater = "nancyse";
	static String hash="0Xhdhhfhfbff";
	static String key="fhoahfoiahfai";
	static int userId=123;
	static String permission="w";
	
	/*
	 * 对文件进行AES加密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static File encryptFile(String sourceFile,File encrypfile,String sKey) {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			inputStream=new ByteArrayInputStream(sourceFile.getBytes());
			outputStream=new FileOutputStream(encrypfile);
			Cipher cipher=initAESCipher(sKey,Cipher.ENCRYPT_MODE);
			//以加密流写入文件
			CipherInputStream cipherInputStream=new CipherInputStream(inputStream,cipher);
			byte[] cache=new byte[1024];
			int mRead=0;
			while((mRead=cipherInputStream.read(cache))!=-1) {
				outputStream.write(cache, 0, mRead);
				outputStream.flush();
			}
			cipherInputStream.close();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			try {
				inputStream.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		return encrypfile;
	}
	
	/*
	 * 对文件进行AES解密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static File decryptFile(File sourceFile, File decryptFile,String sKey) throws IOException {
		InputStream inputStream=null;
		//OutputStream outputStream=null;
		ByteArrayOutputStream outputStream=null;
		File tmp=File.createTempFile("tmp", null);  //创建一个临时文件
		tmp.deleteOnExit();
		try {
			Cipher cipher=initAESCipher(sKey,Cipher.DECRYPT_MODE);
			inputStream=new FileInputStream(sourceFile);
			outputStream = new ByteArrayOutputStream();			
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream,cipher);
			byte[] buffer=new byte[1024];
			int r;
			while((r=inputStream.read(buffer))>=0) {
				cipherOutputStream.write(buffer,0,r);
			}
			cipherOutputStream.close();
			
			//将outputStream里的内容转换成string
			ByteArrayInputStream swapStream =  new ByteArrayInputStream(outputStream.toByteArray());
			BufferedReader reader = new BufferedReader(new InputStreamReader(swapStream));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while( (line=reader.readLine())!=null)
			{
				sb.append(line+"\n");
			}
			System.out.println(sb);
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			try {
				inputStream.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			try {
				outputStream.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
				
		return decryptFile;
	}
	public static Cipher initAESCipher(String sKey,int cipherMode) {
		KeyGenerator keyGenerator=null;
		Cipher cipher=null;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128,new SecureRandom(sKey.getBytes()));
			SecretKey secretKey=keyGenerator.generateKey();
			byte[] codeFormat = secretKey.getEncoded();
			SecretKeySpec key=new SecretKeySpec(codeFormat,"AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(cipherMode, key);
		}catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cipher;
	}
	
	public static void createBlock() throws IOException {
		//创建一个空白文档
		//File tmp = File.createTempFile("tmp", null);
		StringWriter tmpOut = new StringWriter();
		tmpOut.write("Filename:"+fileName+"\n");
		tmpOut.write("Creator:"+creater+"\n");
		tmpOut.write("Hashcode:"+hash+"\n");
		tmpOut.write("Key:"+key+"\n");
		tmpOut.write("PermissionAccessTable:\n");
		tmpOut.write(userId+" "+permission);
	}
	
	
	public static void main(String[] args) throws IOException {
		String cKey="70b1d1e07fd39200e96738d88138c15e98b07c224143be80f5d249b6fc9bca3e70b1d1e07f";
		StringWriter tmpOut = new StringWriter();
		tmpOut.write("Filename:"+fileName+"\n");
		tmpOut.write("Creator:"+creater+"\n");
		tmpOut.write("Hashcode:"+hash+"\n");
		tmpOut.write("Key:"+key+"\n");
		tmpOut.write("PermissionAccessTable:\n");
		tmpOut.write(userId+" "+permission);
		
		File encrypfile=new File("encryfile.txt");
		File decrypfile = new File("decryfile.txt");
		
		encryptFile(tmpOut.toString(),encrypfile,cKey);
		decryptFile(encrypfile,decrypfile,cKey);
	}

}
