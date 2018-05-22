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
	 * ���ַ�������hash����
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
			encodeStr=byte2Hex(messageDigest.digest());  //��������ת����16����
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return encodeStr;
	}
	
	/*
	 * ��byteתΪ16����
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
			if(temp.length()==1)  //��ת����Ϊ��λ��ʱ���Զ��ڸ����ֺ������0
			{
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}

	/*
	 * ���ļ�����AES����
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
			//�Լ�����д���ļ�
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
	 * ���ļ�����AES����
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
			//�Լ�����д���ļ�
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
	 * ���ļ�����AES����
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
			//�Լ�����д���ļ�
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
	 * ���ļ�����AES����
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return String
	 */
	public static ByteArrayOutputStream decryptFile(File sourceFile, File decryptFile,String sKey) throws IOException, InvalidAlgorithmParameterException {
		InputStream inputStream=null;
		ByteArrayOutputStream outputStream=null;
		//ByteArrayOutputStream outputStream=null;
		File tmp=File.createTempFile("tmp", null);  //����һ����ʱ�ļ�
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
			//��outputStream�������ת����string
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
			//������Կ��������ָ��ΪAES�㷨�������ִ�Сд
			keyGenerator = KeyGenerator.getInstance("AES");
			//keyGenerator.init(128,new SecureRandom(sKey.getBytes()));
			//��ʼ����Կ������
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(sKey.getBytes());
			keyGenerator.init(128,secureRandom);
			//����ԭʼ�Գ���Կ
			SecretKey secretKey=keyGenerator.generateKey();
			//���ԭʼ�Գ���Կ���ֽ�����
			byte[] codeFormat = secretKey.getEncoded();
			//�����ֽ���������AES��Կ
			SecretKeySpec key=new SecretKeySpec(codeFormat,"AES");
			//ʹ��CBCģʽ����Ҫһ������iv�������Ӽ����㷨��ǿ��
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			//ָ���㷨AES����������
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			//��ʼ������������һ������Ϊ����/���ܲ������ڶ���������ʹ�õ�key
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
