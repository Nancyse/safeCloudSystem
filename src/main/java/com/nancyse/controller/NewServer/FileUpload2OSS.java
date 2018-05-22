package com.nancyse.controller.NewServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.FileBuffer;
import com.nancyse.controller.NewServer.Util.FileBufferManageUtil;
import com.nancyse.controller.NewServer.Util.FileManageUtil;

@Controller
@RequestMapping(value="/bs")
public class FileUpload2OSS {
	
	//上传文件
	@RequestMapping(value="/fileIndex",method=RequestMethod.GET)
	public String uploadIndex(HttpServletRequest req) {
		return "fileUploadTest/uploadForm2OSS.jsp";		
	}
	
	
	
	//上传文件逻辑
	@RequestMapping(value="/fileUpload2",method=RequestMethod.POST)
	@ResponseBody
	public String fileupload(HttpServletRequest req,
			@RequestParam("filePath") String filepath,
			@RequestParam("description") String desc,
			@RequestParam("file") MultipartFile file
			) throws Exception {

		int errorCode;
		//判断用户是否已登录
		HttpSession session = req.getSession();
		if(session.getAttribute("username")==null) {
			errorCode=-1;
			return "{\"error_code\":"+errorCode+"}";	
		}
	
		//判断文件是否为空
		if(file.isEmpty()) {
			errorCode=-2;
			return "{\"error_code\":"+errorCode+"-2}";
		}
		String filename=file.getOriginalFilename();
		//判断文件是否存在
		String uploader="pslin";
		if( FileManageUtil.isFileExist(filename,filepath,uploader)) {//文件存在
			errorCode=-3;	
		}else { //文件不存在，则上传文件
			long fileLength = FileManageUtil.saveFile2Local(file,filepath); //将文件保存到本地
			FileBufferManageUtil.saveBuffer2Database(filename,filepath,fileLength,uploader); //将缓存文件记录保存在数据库中
			FileManageUtil.uoloadFile2OSS(file,filepath,fileLength,desc);
			errorCode=0;
		}	
		return "{\"error_code\":"+errorCode+"}";		
	}
	
	
	
	//上传文件逻辑
	@RequestMapping(value="/fileUpload",method=RequestMethod.POST)
	@ResponseBody
	public String fileupload2(HttpServletRequest req,
			@RequestParam("filePath") String filepath,
			@RequestParam("description") String desc,
			@RequestParam("filename") String filename,
			@RequestParam("fileData") String fileData
			) throws Exception {
		
		System.out.println("fileData:"+fileData);
		return fileData;		
	}
	
	//上传文件逻辑
	@RequestMapping(value="/uploadHashAndKey",method=RequestMethod.POST)
	@ResponseBody
	public String uploadHashAndKey(HttpServletRequest req,			
			@RequestParam("fileHash") String fileHash,
			@RequestParam("fileKey") String fileKey,
			@RequestParam("filename") String filename
			) throws Exception {
		
		System.out.println("fileHash:"+fileHash);
		System.out.println("fileKey:"+fileKey);
		return fileHash;		
	}
	
	
	//更新文件
	@RequestMapping(value="/updateFileIndex",method=RequestMethod.GET)
	public String uploadFileIndex(HttpServletRequest req) {
		return "fileUploadTest/updateForm2OSS";		
	}
	
	
	//更新文件
	@RequestMapping(value="/updateFile")
	@ResponseBody
	public String updateFile(HttpServletRequest req,
			@RequestParam("description") String desc,
			MultipartFile file,
			@RequestParam("filePath") String filePath
			) throws Exception {
		
		//判断用户是否已登录
		HttpSession session = req.getSession();
		if(session.getAttribute("username")==null) {
			return "{\"status\":\"you are not sign in. \"}";	
		}
		
		String result="success";
		String uploader = "pslin";
		String filename = file.getOriginalFilename();
		FileManageUtil.updateFile(file, filePath, filename,uploader,desc);
		return "{\"status\":\""+result+"\"}";	
	}
	
	//删除文件
	@RequestMapping(value="/deleteFile")
	@ResponseBody
	public String deleteFile(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename) {
		
		//判断用户是否已登录
		HttpSession session = req.getSession();
		if(session.getAttribute("username")==null) {
			return "{\"status\":\"you are not sign in. \"}";	
		}
		
		String result="";
		String uploader ="pslin";
		int state = FileManageUtil.deleteFile(filePath, filename, uploader);
		
		if(state==-1) {  //文件不存在
			result="file is not exist";
		}
		else if(state ==0) {  //删除文件失败
			result="error";
		}
		else {  //删除文件成功
			result="success";
		}
		
		return "{\"status\":\""+result+"\"}";
	}
	
	
	//根据文件名查询文件，包括根据路径名查询文件
	@RequestMapping(value="/searchFile")
	@ResponseBody
	public String searchFiles(
			HttpServletRequest req,
			String word) {
		
		//判断用户是否已登录
		HttpSession session = req.getSession();
		if(session.getAttribute("username")==null) {
			return "{\"status\":\"you are not sign in. \"}";	
		}
		
		String result="";
		String uploader = "pslin";
		//根据uploader查找所有，然后再根据路径和文件名再进行查找
		List<DefaultFile> resList = FileManageUtil.findFile(uploader, word);
		if(resList.isEmpty()) {
			result="null";
			return "{\"status\":\""+result+"\"}";
		}
		result="[";
		for(DefaultFile df : resList) {
			result+="\""+df.getFile_dir()+df.getFile_name()+"\",";
		}
		result = result.substring(0, result.length()-1);
		result+="]";
		return "{\"status\":\""+result+"\"}";
	}
	
	 
}
