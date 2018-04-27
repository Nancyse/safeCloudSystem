package com.nancyse.controller.ASserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



@RequestMapping(value="/AS2")
@Controller
public class ASServer2 {
	
	private  static final String ASKEY="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
	
	@RequestMapping(value="/createFileKeyAndFileBlock",method=RequestMethod.GET)
	@ResponseBody
	public String createFileKeyAndFileBlock(
			HttpServletRequest request,
			HttpServletResponse res) throws IOException {
		System.out.println("����/AS2/createFileKeyAndFileBlock");
		String result="";
		String filename =request.getParameter("filename");
		String creator=request.getParameter("creator");
		String fileHashCode = request.getParameter("fileHashCode");
		System.out.println("filename: "+filename);
		System.out.println("hashcode: "+fileHashCode);
		
		//�����ļ�������Կ
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		//String fileKey=fileHashCode+fileHashCode.substring(0,num);
		String fileKey=fileHashCode;
		//System.out.println(fileKey);
		Util.logger.info("filekey: "+fileKey);
		
		//�����ļ����ƿ�
		String fileBlock = "filename:"+filename
				+"\ncreator:"+creator
				+"\nfileHashCode:"+fileHashCode
				+"\nkey:"+fileKey;
		
		//System.out.println("raw fileblock:\n");
		//System.out.println(fileBlock);
		Util.logger.info("raw fileblock:\n");
		Util.logger.info(fileBlock);
		
		//�����ļ����ƿ�
		OutputStream outputStream = new ByteArrayOutputStream();
		outputStream =Util.encryptFile(fileBlock.getBytes("UTF-8"), outputStream, ASKEY);  //���ļ����ƿ���м���
		String fileblock=Util.byte2String(outputStream);  //�����ܺ���ֽ���תΪ�ַ���
		
		Util.logger.info("before base64Encode: "+fileblock);
		//��base64����
		fileblock = Util.Base64Encode(fileblock);  //��base64����
		
		//���ɷ���json����
		result+= "{\"encryptKey\":\""+fileKey+"\",\"fileblock\":\""+fileblock+"\"}";
		return result;
	}
	
	/*
	 * �ļ�����
	 */
	@RequestMapping(value="/decryptFileBlock",method=RequestMethod.GET)
	@ResponseBody
	public String decryptBlock(HttpServletRequest request,
			@RequestParam("block") String block) {
		
		Util.logger.info("block: "+block);
		
		//��block��base64����
		block = Util.Base64Decode(block);
		Util.logger.info("after base64 block: "+block);
		
		byte[] buf=Util.string2Bytes(block);  //�������ļ���ԭΪ���ܺ���ֽ�����

		
		OutputStream out = new ByteArrayOutputStream();
		out = Util.decryptFile(buf, out, ASKEY);
		String fileblock=Util.outputStream2String(out);
		
		//System.out.println("\ndecrypt fileblock:\n");
		//System.out.println(fileblock);
		Util.logger.info("\ndecrypt fileblock:"+fileblock);
		
		String fileKey = getFileKey(fileblock);
		
		Util.logger.info("trace: decrypt fileKey:"+fileKey);
		return fileKey;
		
	}
	
	
	/*
	 * ��ȡ������Կ
	 */
	public String getFileKey(String fileblock) {
		String fileKey="";
		String[] strs = fileblock.split("\n");
		fileKey = strs[3].split(":")[1];
		Util.logger.info("begin: decrypt fileKey:"+fileKey);
		return fileKey;
	}
	
	

}
