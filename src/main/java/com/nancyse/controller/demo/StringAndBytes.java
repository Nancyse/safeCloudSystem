package com.nancyse.controller.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import com.nancyse.controller.ASserver.Util;

public class StringAndBytes {
	
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
			Cipher cipher=Util.initAESCipher(sKey,Cipher.ENCRYPT_MODE);
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
	
	
	//文件解密
		public static OutputStream decryptFile(byte[] sourceFile, OutputStream out,String sKey) {
			InputStream inputStream=null;
			OutputStream outputStream=null;
			try {
				Cipher cipher=Util.initAESCipher(sKey,Cipher.DECRYPT_MODE);
				//inputStream=new FileInputStream(sourceFile);
				//inputStream =new ByteArrayInputStream(sourceFile.getBytes("UTF-8"));
				inputStream =new ByteArrayInputStream(sourceFile);
				//outputStream=new FileOutputStream(decryptFile);
				outputStream=out;
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
					
			return out;
		
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
		
	public static String byte2String(OutputStream out) {
		String result="";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos=(ByteArrayOutputStream) out;
		byte[] buff=baos.toByteArray();
		System.out.println("bytes length: "+buff.length);
		for( int i=0;i<buff.length;i++) {
			result+=String.valueOf(buff[i]);
			result+=" ";
		}
		
		return result;		
	}
		
	public static void main(String[] args) throws IOException {
		/*
		String str="hello";
		byte[] strbytes = str.getBytes("UTF-8");
		String res = new String(strbytes);
		System.out.println(res);
		for( int i=0; i<strbytes.length;i++)
			System.out.print(strbytes[i]);
		System.out.println();
		
		byte[] bytes= {104,101,108,108,111};
		res = new String(bytes);
		System.out.println(res);
		strbytes = res.getBytes("UTF-8");
		for( int i=0; i<strbytes.length;i++)
			System.out.print(strbytes[i]);
		*/
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer= {104,101,108,108,111};
		String str="name:linpengshan\nage:12";
		baos.write(str.getBytes());
		String result = outputStream2String(baos);
		System.out.println(result);
		//加密文件
		OutputStream out = new ByteArrayOutputStream();
		String sKey="jfhfhffhfhjsjsns";
		out = encryptFile(str.getBytes(),out,sKey);
		//String encryptResult = outputStream2String(out);
		String encryptResult=byte2String(out);
		
		System.out.println("encryptResult:\n"+encryptResult);
		
		//解密文件：
		String[] strs=encryptResult.split(" ");
		ByteArrayOutputStream outBuff = new ByteArrayOutputStream();
		System.out.println("bytes length:"+strs.length);
		for( int i=0;i<strs.length;i++) {
			byte b = Byte.parseByte(strs[i]);
			outBuff.write(b);
		}
		/*
		byte[] buf;
		buf=out.toByteArray();
		System.out.println("\nout byte:\n");
		for( int i=0;i<buf.length;i++) {
			System.out.print(buf[i]+" ");
		}*/
		
		ByteArrayOutputStream outTmp =new ByteArrayOutputStream();
		//out = decryptFile(out.toByteArray(),outTmp,sKey);
		out = decryptFile(outBuff.toByteArray(),outTmp,sKey);
		String decryptResult = outputStream2String(out);
		System.out.println("\ndecryptResult:\n"+decryptResult);		
		
		
	}
	
	

}
