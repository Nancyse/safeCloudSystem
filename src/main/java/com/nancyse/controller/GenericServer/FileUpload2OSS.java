package com.nancyse.controller.GenericServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

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

import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.FileBuffer;

@Controller
@RequestMapping(value="/gs")
public class FileUpload2OSS {
	
	//�ϴ��ļ�
	@RequestMapping(value="/fileIndex",method=RequestMethod.GET)
	public String uploadIndex(HttpServletRequest req) {
		return "fileUploadTest/uploadForm2OSS";		
	}
	
	//�ϴ��ļ��߼�
	@RequestMapping(value="/fileUpload",method=RequestMethod.POST)
	@ResponseBody
	public String fileupload(HttpServletRequest req,
			@RequestParam("description") String desc,
			@RequestParam("file") MultipartFile file,
			@RequestParam("filePath") String filepath) throws Exception {
		
		String result="";
		//�ж��ļ��Ƿ�Ϊ��
		if(file.isEmpty()) {
			result="You could not choose a file .";
			return "{\"satuc\":\""+result+"\"}";
		}
		String filename=file.getOriginalFilename();
		//�ж��ļ��Ƿ����
		String uploader="pslin";
		if( FileManager.isFileExist(filename,filepath,uploader)) {//�ļ�����
			result="The file is exist,do you want to update it?";
			
		}else { //�ļ������ڣ����ϴ��ļ�
			long fileLength = FileManager.saveFile2Local(file,filepath); //���ļ����浽����
			FileBufferManager.saveBuffer2Database(filename,filepath,fileLength,uploader); //�������ļ���¼���������ݿ���
			FileManager.uoloadFile2OSS(file,filepath,fileLength);
			result="success";
		}	
		return "{\"status\":\""+result+"\"}";		
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
		
		String result="success";
		String uploader = "pslin";
		String filename = file.getOriginalFilename();
		FileManager.updateFile(file, filePath, filename,uploader);
		return "{\"status\":\""+result+"\"}";	
	}
	
	//ɾ���ļ�
	@RequestMapping(value="/deleteFile")
	@ResponseBody
	public String deleteFile(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename) {
		
		String result="";
		String uploader ="pslin";
		int state = FileManager.deleteFile(filePath, filename, uploader);
		
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
		String result="";
		String uploader = "pslin";
		//����uploader�������У�Ȼ���ٸ���·�����ļ����ٽ��в���
		List<DefaultFile> resList = FileManager.findFile(uploader, word);
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
