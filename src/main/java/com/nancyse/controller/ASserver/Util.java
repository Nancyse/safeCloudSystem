package com.nancyse.controller.ASserver;

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
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
	public static Logger logger = LogManager.getLogger("ACS");
	
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
	 * 对文件进行AES加密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static File encryptFile(byte[] sourceFile,File encrypfile,String sKey) {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			inputStream=new ByteArrayInputStream(sourceFile);
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
	 * 对文件进行AES加密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static OutputStream encryptFile(byte[] sourceFile,OutputStream encrypfile,String sKey) {
		InputStream inputStream=null;
		OutputStream outputStream=encrypfile;
		try {
			inputStream=new ByteArrayInputStream(sourceFile);
			//outputStream=new FileOutputStream(encrypfile);
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
	
	public static File decryptFile2(File sourceFile, File decryptFile,String sKey) {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			Cipher cipher=initAESCipher(sKey,Cipher.DECRYPT_MODE);
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
	public static ByteArrayOutputStream decryptFile(File sourceFile, File decryptFile,String sKey) throws IOException {
		InputStream inputStream=null;
		ByteArrayOutputStream outputStream=null;
		//ByteArrayOutputStream outputStream=null;
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
	public static Cipher initAESCipher(String sKey,int cipherMode) {
		KeyGenerator keyGenerator=null;
		Cipher cipher=null;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(sKey.getBytes());
			//keyGenerator.init(128,new SecureRandom(sKey.getBytes()));
			keyGenerator.init(128,secureRandom);
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
	
	//outputStream转String
	public static String parse_String(OutputStream out) throws IOException
	{
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		swapStream = (ByteArrayOutputStream) out;
		ByteArrayInputStream bais = new ByteArrayInputStream(swapStream.toByteArray());
		InputStreamReader isr = new InputStreamReader(bais,"UTF-8");
		int ch;
		StringBuffer sb = new StringBuffer();
		while((ch=isr.read())!=-1)
		{
			sb.append(ch);
		}
		return sb.toString();
	}
	
	
	//将输出字节流转换成string并输出
	public static String outputStream2String(OutputStream out) {
		String result="";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos = (ByteArrayOutputStream) out;
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		byte[] buff = new byte[1024];
		int hasCode=0;
		try {
			//将加密后的二进制数据转换成字符串
			while( (hasCode=bais.read(buff))!=-1) {
				String tmp = new String(buff,0,hasCode,"UTF-8");
				result+=tmp;
			}
			//用base64编码
			//result = Base64Encode(result);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return result;
	}

	
	//用Base64编码
	public static String Base64Encode(String str) throws UnsupportedEncodingException {
		String result="";
		Base64.Encoder encoder = Base64.getEncoder();
		result = encoder.encodeToString(str.getBytes("UTF-8"));
		return result;
	}
	
	
	//用Base64解码
	public static String Base64Decode(String str) {
		String result="";
		Base64.Decoder decoder= Base64.getDecoder();
		try {
			result = new String(decoder.decode(str),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	//文件解密
	public static OutputStream decryptFile(byte[] sourceFile, OutputStream out,String sKey) {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			Cipher cipher=initAESCipher(sKey,Cipher.DECRYPT_MODE);
			inputStream =new ByteArrayInputStream(sourceFile);
			outputStream=out;
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream,cipher);
			byte[] buffer=new byte[1024];
			int r;
			while((r=inputStream.read(buffer))>=0) {
				cipherOutputStream.write(buffer,0,r);
			}
			cipherOutputStream.close();
			return outputStream;
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
				
		return outputStream;
	
	}
	
	
	
	//文件解密
	public static File decryptFile(String sourceFile, File decryptFile,String sKey) {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			Cipher cipher=initAESCipher(sKey,Cipher.DECRYPT_MODE);
			//inputStream=new FileInputStream(sourceFile);
			inputStream =new ByteArrayInputStream(sourceFile.getBytes("UTF-8"));
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
	 * 将字节数组转换成字符串
	 */
	public static String byte2String(OutputStream out) {
		String result="";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos=(ByteArrayOutputStream) out;
		byte[] buff=baos.toByteArray();
		for( int i=0;i<buff.length;i++) {
			result+=String.valueOf(buff[i]);
			result+=" ";
		}
		
		return result;		
	}
	
	
	/*
	 * 将字符串转换为字节数组
	 */
	public static byte[] string2Bytes(String str) {
		if( str.isEmpty()) {
			return null;
		}
		String[] strs=str.split(" ");
		ByteArrayOutputStream outBuff = new ByteArrayOutputStream();
		for( int i=0;i<strs.length;i++) {
			byte b = Byte.parseByte(strs[i]);
			outBuff.write(b);
		}
		return outBuff.toByteArray();
	}
	
	public static void main(String[] args) {
		//String str="1 -11 23 -52 -54 125 10 73 3 -55 -6 -108 -58 14 -74 -66 -94 19 -93 -37 -77 -21 -35 87 -124 36 50 -36 98 56 70 50 96 -94 -29 -22 -47 85 105 -75 -5 30 119 -82 -97 98 27 25 80 21 66 -21 -113 -48 57 -104 15 26 -7 -97 12 63 8 41 -94 -128 47 73 72 59 -77 -89 73 91 63 -25 59 29 -21 116 -70 56 -125 48 71 42 -57 64 5 -32 -61 16 -100 -96 -107 95 -91 55 -13 11 34 20 10 -84 -4 112 -19 107 -123 -57 103 -22 93 44 -96 111 -124 4 -19 -35 -122 -45 -123 118 99 67 23 44 -49 107 -39 88 -84 42 -23 124 -52 -114 100 -100 126 107 -45 -48 -77 -126 -106 113 -116 104 -88 -38 67 110 -99 75 -47 -45 -53 28 -56 98 -67 100 -11 12 79 81 26 -127 -88 26 -110 35 123 -73 47 -108 0 81 -52 -126 -105 -117 100 -114 3 -94 -94 14 -48 6 -99 -109 46 -73 -91 -111 -41 105 -85 88 -75 58 88 79 122 -29 ";
		String str="107 -8 -52 -40 -61 23 -25 -122 22 -86 -46 20 -48 91 15 -39 104 40 80 113 88 18 -65 113 -5 -95 -64 94 -2 -48 95 37 66 -3 42 83 -101 -116 106 64 55 4 -3 43 -77 46 84 118 120 33 -116 8 -20 -47 -78 -47 80 -2 -78 -57 -61 90 -111 -67 95 58 -27 -34 -46 86 -14 -89 25 -18 -24 68 36 -55 124 38 -69 10 -66 84 -72 -42 -5 -78 -13 -37 92 -117 -29 0 -83 -113 19 124 -74 84 80 -19 -13 -76 -34 -73 40 49 -121 -68 -26 66 -118 40 -57 -34 4 73 26 48 18 30 23 77 -110 108 76 -11 -106 0 12 113 -117 53 -102 -100 -93 -11 51 102 -69 -50 4 -29 -64 85 16 -114 -50 27 100 -87 122 83 41 -121 -14 -114 3 62 -6 94 -113 6 110 -105 -121 -62 64 73 -47 7 50 61 -84 -36 -67 8 20 -2 55 111 -9 127 20 121 -75 79 121 -41 69 58 ";
		String str2="107 -8 -52 -40 -61 23 -25 -122 22 -86 -46 20 -48 91 15 -39 104 40 80 113 88 18 -65 113 -5 -95 -64 94 -2 -48 95 37 66 -3 42 83 -101 -116 106 64 55 4 -3 43 -77 46 84 118 120 33 -116 8 -20 -47 -78 -47 80 -2 -78 -57 -61 90 -111 -67 95 58 -27 -34 -46 86 -14 -89 25 -18 -24 68 36 -55 124 38 -69 10 -66 84 -72 -42 -5 -78 -13 -37 92 -117 -29 0 -83 -113 19 124 -74 84 80 -19 -13 -76 -34 -73 40 49 -121 -68 -26 66 -118 40 -57 -34 4 73 26 48 18 30 23 77 -110 108 76 -11 -106 0 12 113 -117 53 -102 -100 -93 -11 51 102 -69 -50 4 -29 -64 85 16 -114 -50 27 100 -87 122 83 41 -121 -14 -114 3 62 -6 94 -113 6 110 -105 -121 -62 64 73 -47 7 50 61 -84 -36 -67 8 20 -2 55 111 -9 127 20 121 -75 79 121 -41 69 58 ";
		String str3="107 -8 -52 -40 -61 23 -25 -122 22 -86 -46 20 -48 91 15 -39 104 40 80 113 88 18 -65 113 -5 -95 -64 94 -2 -48 95 37 66 -3 42 83 -101 -116 106 64 55 4 -3 43 -77 46 84 118 120 33 -116 8 -20 -47 -78 -47 80 -2 -78 -57 -61 90 -111 -67 95 58 -27 -34 -46 86 -14 -89 25 -18 -24 68 36 -55 124 38 -69 10 -66 84 -72 -42 -5 -78 -13 -37 92 -117 -29 0 -83 -113 19 124 -74 84 80 -19 -13 -76 -34 -73 40 49 -121 -68 -26 66 -118 40 -57 -34 4 73 26 48 18 30 23 77 -110 108 76 -11 -106 0 12 113 -117 53 -102 -100 -93 -11 51 102 -69 -50 4 -29 -64 85 16 -114 -50 27 100 -87 122 83 41 -121 -14 -114 3 62 -6 94 -113 6 110 -105 -121 -62 64 73 -47 7 50 61 -84 -36 -67 8 20 -2 55 111 -9 127 20 121 -75 79 121 -41 69 58 ";
		String str4="107 -8 -52 -40 -61 23 -25 -122 22 -86 -46 20 -48 91 15 -39 104 40 80 113 88 18 -65 113 -5 -95 -64 94 -2 -48 95 37 66 -3 42 83 -101 -116 106 64 55 4 -3 43 -77 46 84 118 120 33 -116 8 -20 -47 -78 -47 80 -2 -78 -57 -61 90 -111 -67 95 58 -27 -34 -46 86 -14 -89 25 -18 -24 68 36 -55 124 38 -69 10 -66 84 -72 -42 -5 -78 -13 -37 92 -117 -29 0 -83 -113 19 124 -74 84 80 -19 -13 -76 -34 -73 40 49 -121 -68 -26 66 -118 40 -57 -34 4 73 26 48 18 30 23 77 -110 108 76 -11 -106 0 12 113 -117 53 -102 -100 -93 -11 51 102 -69 -50 4 -29 -64 85 16 -114 -50 27 100 -87 122 83 41 -121 -14 -114 3 62 -6 94 -113 6 110 -105 -121 -62 64 73 -47 7 50 61 -84 -36 -67 8 20 -2 55 111 -9 127 20 121 -75 79 121 -41 69 58 ";
		String[] strs=str2.split(" ");
		System.out.println(str);
		
		ByteArrayOutputStream outBuff = new ByteArrayOutputStream();
		for( int i=0;i<strs.length;i++) {
			byte b = Byte.parseByte(strs[i]);
			outBuff.write(b);
		}
		byte[] buff=outBuff.toByteArray();
		for( int i=0;i<buff.length;i++) {
			System.out.print(buff[i]+" ");
		}
		System.out.println();
		
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		String ASKEY="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
		OutputStream out = decryptFile(buff,baos,ASKEY);
		System.out.println(outputStream2String(out));		
		
	}
}
