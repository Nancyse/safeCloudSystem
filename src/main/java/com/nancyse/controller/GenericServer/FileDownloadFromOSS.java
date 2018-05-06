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
		
		//�ж��ļ��Ƿ����
		
		//�ӱ�������
		return downloadFileFromLocal(filename,filePath);
		
		//��OSS����
		
	}
	
	private Boolean isFileExist(String filePath, String filename,String uploader) {
		return true;
	}
	
	//�ӱ���������
	private ResponseEntity<byte[]> downloadFileFromLocal(String filename, String filePath) throws IOException{
		String path=FilePath.LOCALDIR+filePath+filename;
		File file = new File(path);
		//�����ļ����ͻ���
		HttpHeaders headers= new HttpHeaders();
		//������ʾ���ļ������������������������
		String downloadFileName = new String(filename.getBytes("UTF-8"),"iso-8859-1");
		//֪ͨ�������attachment(���ط�ʽ)��ͼƬ
		headers.setContentDispositionFormData("attachment", downloadFileName);
		//application/octet-stream:�����������ݣ�������ļ����أ�
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers,HttpStatus.CREATED);			
	}
	
	//��OSS����

}
