package com.nancyse.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nancyse.controller.demo.Util;

@RequestMapping(value="/AS")
@Controller
public class ASServer {
	private final String ASKEY="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1"; 
	/*
	 * 接受请求，生成文件控制块
	 */
	@RequestMapping(value="/getkey",method=RequestMethod.GET)
	@ResponseBody
	public String getKey(HttpServletRequest request) throws IOException {
		System.out.println("/AS/getkey");
		//Map<String, String> params =request.getParameterMap();
		String filename =request.getParameter("filename");
		String creator=request.getParameter("creator");
		String fileHashCode = request.getParameter("fileHashCode");
		System.out.println("filename: "+filename);
		System.out.println("hashcode: "+fileHashCode);
		//生成加密密钥
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		//生成文件控制块
		String fileKey=fileHashCode+fileHashCode.substring(0,num);
		System.out.println(fileKey);
		String fileBlock = "filename:"+filename
				+"creator:"+creator
				+"fileHashCode:"+fileHashCode
				+"key:"+fileKey;
		//对文件控制块进行加密
		String path=request.getServletContext().getRealPath("/fileblocks/");
		File filepath=new File(path,filename);
		//判断路径是存在，如果不存在就创建一个
		if(!filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		OutputStream out = new ByteArrayOutputStream();
		out =  encryptFile(fileBlock, out, ASKEY);
		String fileblock = this.parse_String(out);		

		String result="";
		result+= "{\"encryptKey\":\""+fileKey+"\",\"fileblock\":\""+fileblock+"\"}";
		return result;
	}
	
	/*
	 * 对文件进行AES加密
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return File
	 */
	public static OutputStream encryptFile(String sourceFile,OutputStream out,String sKey) {
		InputStream inputStream=null;
		OutputStream outputStream=null;
		try {
			inputStream=new ByteArrayInputStream(sourceFile.getBytes());
			//outputStream=new FileOutputStream(encrypfile);
			outputStream = out;
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
		
		return out;
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
	
	//outputStream转String
		public String parse_String(OutputStream out) throws IOException
		{
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			swapStream = (ByteArrayOutputStream) out;
			ByteArrayInputStream bais = new ByteArrayInputStream(swapStream.toByteArray());
			InputStreamReader isr = new InputStreamReader(bais);
			int ch;
			StringBuffer sb = new StringBuffer();
			while((ch=isr.read())!=-1)
			{
				sb.append(ch);
			}
			return sb.toString();
		}


}
