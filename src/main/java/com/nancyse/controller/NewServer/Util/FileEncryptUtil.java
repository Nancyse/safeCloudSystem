package com.nancyse.controller.NewServer.Util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class FileEncryptUtil {
	public static Logger logger = LogManager.getLogger("ACS");
	public static String hostName = "http://localhost:8080";
	public static String ivParameter="zhncpNPMFzEjjPKY";
	/*
	 * 将字符串进行hash计算
	 * @param String
	 * @return
	 */
	public static String getSHA256HashCode(byte[] str)
	{
		MessageDigest messageDigest;
		String encodeStr="";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str);
			encodeStr=byte2Hex(messageDigest.digest());  //将二进制转换程16进制
		}
		catch(NoSuchAlgorithmException e)
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

	/*
	 * 对文件进行AES加密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static File encryptFile(String sourceFile,File encrypfile,String sKey) throws InvalidAlgorithmParameterException {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			inputStream=new ByteArrayInputStream(sourceFile.getBytes());
			outputStream=new FileOutputStream(encrypfile);
			Cipher cipher=initAESCipher(sKey,Cipher.ENCRYPT_MODE,ivParameter);
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
	 * 对文件进行AES加密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static File encryptFile(byte[] sourceFile,File encrypfile,String sKey) throws InvalidAlgorithmParameterException {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			inputStream=new ByteArrayInputStream(sourceFile);
			outputStream=new FileOutputStream(encrypfile);
			Cipher cipher=initAESCipher(sKey,Cipher.ENCRYPT_MODE,ivParameter);
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
	 * 对文件进行AES加密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static OutputStream encryptFile(byte[] sourceFile,OutputStream encrypfile,String sKey) throws InvalidAlgorithmParameterException {
		InputStream inputStream=null;
		OutputStream outputStream=encrypfile;
		try {
			inputStream=new ByteArrayInputStream(sourceFile);
			//outputStream=new FileOutputStream(encrypfile);
			Cipher cipher=initAESCipher(sKey,Cipher.ENCRYPT_MODE,ivParameter);
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
	
	public static File decryptFile2(File sourceFile, File decryptFile,String sKey) throws InvalidAlgorithmParameterException {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			Cipher cipher=initAESCipher(sKey,Cipher.DECRYPT_MODE,ivParameter);
			inputStream=new FileInputStream(sourceFile);
			outputStream=new FileOutputStream(decryptFile);
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream,cipher);
			byte[] buffer=new byte[1024];
			int r;
			while((r=inputStream.read(buffer))>=0) {
				cipherOutputStream.write(buffer,0,r);
			}
			cipherOutputStream.close();
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
	
	/*
	 * 对文件进行AES解密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return String
	 */
	public static ByteArrayOutputStream decryptFile(File sourceFile, File decryptFile,String sKey) throws IOException, InvalidAlgorithmParameterException {
		InputStream inputStream=null;
		ByteArrayOutputStream outputStream=null;
		//ByteArrayOutputStream outputStream=null;
		File tmp=File.createTempFile("tmp", null);  //创建一个临时文件
		tmp.deleteOnExit();
		try {
			Cipher cipher=initAESCipher(sKey,Cipher.DECRYPT_MODE,ivParameter);
			inputStream=new FileInputStream(sourceFile);
			outputStream = new ByteArrayOutputStream();			
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream,cipher);
			byte[] buffer=new byte[1024];
			int r;
			while((r=inputStream.read(buffer))>=0) {
				cipherOutputStream.write(buffer,0,r);
			}
			cipherOutputStream.close();
			
			/*
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
			return swapStream;
			*/
			return outputStream;
			
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(inputStream!=null)
					inputStream.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			try {
				if(outputStream!=null)
					outputStream.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
				
		return null;
	}
	public static Cipher initAESCipher(String sKey,int cipherMode,String ivParameter) throws InvalidAlgorithmParameterException {
		KeyGenerator keyGenerator=null;
		Cipher cipher=null;
		try {
			//构造密钥生成器，指定为AES算法，不区分大小写
			keyGenerator = KeyGenerator.getInstance("AES");
			//keyGenerator.init(128,new SecureRandom(sKey.getBytes()));
			//初始化密钥生成器
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(sKey.getBytes());
			keyGenerator.init(128,secureRandom);
			//产生原始对称密钥
			SecretKey secretKey=keyGenerator.generateKey();
			//获得原始对称密钥的字节数组
			byte[] codeFormat = secretKey.getEncoded();
			//根据字节数组生成AES密钥
			SecretKeySpec key=new SecretKeySpec(codeFormat,"AES");
			//使用CBC模式，需要一个向量iv，可增加加密算法的强度
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			//指定算法AES生成密码器
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			//初始化密码器，第一个参数为加密/解密操作，第二个参数是使用的key
			cipher.init(cipherMode, key,iv);
		}catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return cipher;
	}
}
