package com.nancyse.controller.demo.testOSS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Controller;


public class testOSSController {
	
	//�ϴ��ļ�
	public static void testUploadObject2OSS() throws IOException {
		//�ϴ��ļ�
		String fileName="E:/testOSS.txt";
		File file = new File(fileName);
		String diskName = "lps-test/";
		String newFileName = "testOSS.txt";
		InputStream fileContent = new FileInputStream(file);
		OSSManageUtil.uploadFile(fileContent, diskName, newFileName);
	}
	
	//�����ļ�
	public static void testDownloadObject() throws IOException{
		String filename="E:/downloadfile.txt";
		String key="lps-test/testOSS.txt";
		OSSConfig ossConfig= new OSSConfig("com/nancyse/controller/demo/testOSS/config.properties");
		OSSManageUtil.downloadFile(ossConfig, key, filename);
	}
	
	
	//�ж�Object�Ƿ����
	public static void testIsObjectExist() throws IOException {
		OSSConfig ossConfig= new OSSConfig("com/nancyse/controller/demo/testOSS/config.properties");
		String bucketName = "lps-test";
		String key="test.txt";
		//String key="lps-test/testOSS.txt";
		//String key="lps-test//testOSS.txt";
		Boolean found = OSSManageUtil.isObjectExist(ossConfig, bucketName, key);
		System.out.print(found);
	}
	
	
	//��ȡ�ļ�Ԫ��Ϣ
	public static void testGetObjectMeta() throws Exception {
		OSSConfig ossConfig= new OSSConfig("com/nancyse/controller/demo/testOSS/config.properties");
		String bucketName = "lps-test";
		String key="test.txt";
		OSSManageUtil.getObjectMeta(ossConfig, bucketName, key);
	}

	//ɾ���ļ�
	public static void testDeleteFile() throws IOException {
		OSSConfig ossConfig = new OSSConfig("com/nancyse/controller/demo/testOSS/config.properties");
		String bucketName = "lps-test";
		String key="test.txt";
		OSSManageUtil.deleteFile(ossConfig, bucketName, key);
	}
	
	public static void main(String[] args) throws Exception {
		//testUploadObject2OSS();
		//testDownloadObject();
		//testIsObjectExist();
		//testGetObjectMeta();
		testDeleteFile();
	}

}
