package com.nancyse.controller.GenericServer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/gs")
public class FileDownloadFromOSS {
	
	@RequestMapping(value="/downfile",method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> downloadFile(HttpServletRequest req, 
			String filename, 
			String filePath) throws Exception {
		String result="";
		
		//判断文件是否存在
		
		//从本地下载
		return downloadFileFromLocal(filename,filePath);
		
		//从OSS下载
		
	}
	
	private Boolean isFileExist(String filePath, String filename,String uploader) {
		return true;
	}
	
	//从本地中下载
	private ResponseEntity<byte[]> downloadFileFromLocal(String filename, String filePath) throws IOException{
		String path=FilePath.LOCALDIR+filePath+filename;
		File file = new File(path);
		//传输文件到客户端
		HttpHeaders headers= new HttpHeaders();
		//下载显示的文件名，解决中文名称乱码问题
		String downloadFileName = new String(filename.getBytes("UTF-8"),"iso-8859-1");
		//通知浏览器以attachment(下载方式)打开图片
		headers.setContentDispositionFormData("attachment", downloadFileName);
		//application/octet-stream:二进制流数据（最常见的文件下载）
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers,HttpStatus.CREATED);			
	}
	
	//从OSS下载

}
