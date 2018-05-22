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
	
	//�ϴ��ļ�
	@RequestMapping(value="/fileIndex",method=RequestMethod.GET)
	public String uploadIndex(HttpServletRequest req) {
		return "fileUploadTest/uploadForm2OSS.jsp";		
	}
	
	
	
	//�ϴ��ļ��߼�
	@RequestMapping(value="/fileUpload2",method=RequestMethod.POST)
	@ResponseBody
	public String fileupload(HttpServletRequest req,
			@RequestParam("filePath") String filepath,
			@RequestParam("description") String desc,
			@RequestParam("file") MultipartFile file
			) throws Exception {

		int errorCode;
		//�ж��û��Ƿ��ѵ�¼
		HttpSession session = req.getSession();
		if(session.getAttribute("username")==null) {
			errorCode=-1;
			return "{\"error_code\":"+errorCode+"}";	
		}
	
		//�ж��ļ��Ƿ�Ϊ��
		if(file.isEmpty()) {
			errorCode=-2;
			return "{\"error_code\":"+errorCode+"-2}";
		}
		String filename=file.getOriginalFilename();
		//�ж��ļ��Ƿ����
		String uploader="pslin";
		if( FileManageUtil.isFileExist(filename,filepath,uploader)) {//�ļ�����
			errorCode=-3;	
		}else { //�ļ������ڣ����ϴ��ļ�
			long fileLength = FileManageUtil.saveFile2Local(file,filepath); //���ļ����浽����
			FileBufferManageUtil.saveBuffer2Database(filename,filepath,fileLength,uploader); //�������ļ���¼���������ݿ���
			FileManageUtil.uoloadFile2OSS(file,filepath,fileLength,desc);
			errorCode=0;
		}	
		return "{\"error_code\":"+errorCode+"}";		
	}
	
	
	
	//�ϴ��ļ��߼�
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
	
	//�ϴ��ļ��߼�
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
	
	
	//�����ļ�
	@RequestMapping(value="/updateFileIndex",method=RequestMethod.GET)
	public String uploadFileIndex(HttpServletRequest req) {
		return "fileUploadTest/updateForm2OSS";		
	}
	
	
	//�����ļ�
	@RequestMapping(value="/updateFile")
	@ResponseBody
	public String updateFile(HttpServletRequest req,
			@RequestParam("description") String desc,
			MultipartFile file,
			@RequestParam("filePath") String filePath
			) throws Exception {
		
		//�ж��û��Ƿ��ѵ�¼
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
	
	//ɾ���ļ�
	@RequestMapping(value="/deleteFile")
	@ResponseBody
	public String deleteFile(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename) {
		
		//�ж��û��Ƿ��ѵ�¼
		HttpSession session = req.getSession();
		if(session.getAttribute("username")==null) {
			return "{\"status\":\"you are not sign in. \"}";	
		}
		
		String result="";
		String uploader ="pslin";
		int state = FileManageUtil.deleteFile(filePath, filename, uploader);
		
		if(state==-1) {  //�ļ�������
			result="file is not exist";
		}
		else if(state ==0) {  //ɾ���ļ�ʧ��
			result="error";
		}
		else {  //ɾ���ļ��ɹ�
			result="success";
		}
		
		return "{\"status\":\""+result+"\"}";
	}
	
	
	//�����ļ�����ѯ�ļ�����������·������ѯ�ļ�
	@RequestMapping(value="/searchFile")
	@ResponseBody
	public String searchFiles(
			HttpServletRequest req,
			String word) {
		
		//�ж��û��Ƿ��ѵ�¼
		HttpSession session = req.getSession();
		if(session.getAttribute("username")==null) {
			return "{\"status\":\"you are not sign in. \"}";	
		}
		
		String result="";
		String uploader = "pslin";
		//����uploader�������У�Ȼ���ٸ���·�����ļ����ٽ��в���
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
