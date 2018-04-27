package com.nancyse.controller;

import java.io.File;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ASController {
	/*
	 * 重定向到验证服务器
	 */
	@RequestMapping("as/createFileBlock")
	public void forward(HttpServletRequest request){
		String filename=(String) request.getAttribute("filename");
		String creator=(String)request.getAttribute("creator");
		String fileHashCode = (String)request.getAttribute("fileHashCode");
		//生成加密密钥
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		//生成文件控制块
		String key=fileHashCode+fileHashCode.substring(0,num);
		System.out.println(key);
		String fileBlock = "filename:"+filename
				+"creator:"+creator
				+"fileHashCode:"+fileHashCode
				+"key:"+key;
		//对文件控制块进行加密
		File encryptfile = new File(filename);
		File file = Util.encryptFile(fileBlock, encryptfile, key);
		System.out.print(file.getAbsolutePath());
		//return 
	}
	

}
