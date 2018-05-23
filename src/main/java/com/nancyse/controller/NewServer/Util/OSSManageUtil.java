package com.nancyse.controller.NewServer.Util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.SimplifiedObjectMeta;
import com.nancyse.controller.NewServer.Const.FilePath;
import com.nancyse.controller.NewServer.Const.OSSConfig;


/*
 * OSS�ļ������OSSManageUtil������
 */
public class OSSManageUtil {
	
	
	/*
	 * �ϴ������ļ�
	 */
	public static void uploadLocalFile(OSSConfig ossConfig, String bucketName, String key, File file) {
		String endpoint=ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		//����OSSClientʵ��
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		//�ϴ��ļ�
		ossClient.putObject(bucketName, key, file);
		ossClient.shutdown();
	}
	
	/*�ϴ����ĵ�OSS��*/
	public static void uploadString(OSSConfig ossConfig,String bucketName, String key,String str) {
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		//�ϴ��ַ���
		ossClient.putObject(bucketName, key,new ByteArrayInputStream(str.getBytes()));
		ossClient.shutdown();
	}
	
	/*��OSS����ʽ��������*/
	public static String downloadStr(OSSConfig ossConfig,String bucketName,String key) throws IOException {
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		OSSObject ossObject = ossClient.getObject(bucketName, key);
		//��Object����
		BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
		String result = "";
		while(true) {
			String line = reader.readLine();
			if( line == null) break;
			result += line;
		}
		return result;
				
		
	}
	
	/*
	 * ��OSS�����ļ�������
	 */
	public static void downloadFile2Local(OSSConfig ossConfig, String bucketName, String key) {
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		String filePath=FilePath.TMPDIR+key;
		ossClient.getObject(new GetObjectRequest(bucketName,key),new File(filePath));
		ossClient.shutdown();		
	}
	
	
	/*
	 * ɾ���ļ�
	 */
	public static void deleteFile(OSSConfig ossConfig,String bucketName, String key) throws IOException{
		//���������ļ�
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		
		OSSClient ossClient= new OSSClient(endpoint,accessKeyId,accessKeySecret);
		ossClient.deleteObject(bucketName, key);
		ossClient.shutdown();
	}
	
	
	
	
	
	/*
	 * �ж��ļ�Object�Ƿ����
	 */
	public static Boolean isObjectExist(OSSConfig ossConfig,String bucketName,String key) {
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		
		//����OSSClientʵ��
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		//Object�Ƿ����
		Boolean found= ossClient.doesObjectExist(bucketName, key);
		ossClient.shutdown();
		return found;
	}
	
	
	//��ȡ�ļ�Ԫ��Ϣ
	public static void getObjectMeta(OSSConfig ossConfig, String bucketName, String key) throws ParseException {
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		//����OSSClientʵ��
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		//��ȡ�ļ��Ĳ���Ԫ��Ϣ
		SimplifiedObjectMeta objectMeta = ossClient.getSimplifiedObjectMeta(bucketName, key);
		System.out.println("��ȡ�ļ�����Ԫ��Ϣ��");
		System.out.println(objectMeta.getSize());
		System.out.println(objectMeta.getETag());
		System.out.println(objectMeta.getLastModified());
		//��ȡ�ļ���ȫ��Ԫ��Ϣ
		ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, key);
		System.out.println("��ȡ�ļ�ȫ����Ϣ��");
		System.out.println(metadata.getContentType());
		System.out.println(metadata.getLastModified());
		//System.out.println(metadata.getExpirationTime());
		//�ر�client
		ossClient.shutdown();
	}
	
	
	/*
	 * �ϴ�OSS�������ļ�
	 * @param multipartFile spring �ϴ����ļ�
	 * remotePath @param oss����������Ŀ¼
	 * @throws Exception �趨�ļ�@return String
	 */
	public static String uploadFile(InputStream fileContent,String remotePath,String fileName) throws IOException {
		//���������
		//fileName="lps-test_"+new Date().getTime()+fileName.substring(fileName.lastIndexOf("."));
		//���������ļ�����ʼ��OSSClient
		OSSConfig ossConfig = new OSSConfig(FilePath.CONFIGFILE);
		OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(),ossConfig.getAccessKeyId(),ossConfig.getAccessKeySecret());
		//�������Ŀ¼
		//String remoteFilePath = remotePath.substring(0, remotePath.length()).replaceAll("\\\\","/")+"/";
		String remoteFilePath = remotePath;
		//�����ϴ�Object��Metadata
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(fileContent.available());;
		objectMetadata.setContentEncoding("utf-8");
		objectMetadata.setCacheControl("no-cache");
		objectMetadata.setHeader("pragma", "no-cache");
		objectMetadata.setContentType(contentType(fileName.substring(fileName.lastIndexOf("."))));
		objectMetadata.setContentDisposition("inline;filename="+fileName);
		//�ϴ��ļ�
		ossClient.putObject(ossConfig.getBucketName(), remoteFilePath+fileName, fileContent,objectMetadata);
		//�ر�OSSClient
		ossClient.shutdown();
		//�ر�io��
		fileContent.close();
		ossClient.shutdown();
		return ossConfig.getAccessUrl()+"/"+remoteFilePath+fileName;
	}
	
	
	/*
	 * ���ص�����
	 */
	public static void downloadFile(OSSConfig ossConfig, String key, String filename) throws IOException {
		//��ʼ��OSSClient
		OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(),ossConfig.getAccessKeyId(),ossConfig.getAccessKeySecret());
		OSSObject object = ossClient.getObject(ossConfig.getBucketName(),key);
		
		//��ȡObjectMeta
		ObjectMetadata meta = object.getObjectMetadata();
		//��ȡObject��������,���ص�����
		InputStream objectContent = object.getObjectContent();
		ObjectMetadata objectData = ossClient.getObject(new GetObjectRequest(ossConfig.getBucketName(),key), new File(filename));
		//�ر�������
		objectContent.close();
		ossClient.shutdown();
		
	}
	
	
	
	
	
	/*
	 * description: �ж�OSS�����ļ��ϴ�ʱ�ļ���contentType
	 * @param FilenameExtension �ļ���׺
	 * @return String 
	 */
	public static String contentType(String FilenameExtension) {
		if(FilenameExtension.equals(".BMP") || FilenameExtension.equals(".bmp")) {
			return "image/bmp";
		}
		if(FilenameExtension.equals(".GIF") || FilenameExtension.equals(".gif")) {
			return "image/gif";
		}
		if(FilenameExtension.equals(".JPEG") || FilenameExtension.equals(".jpeg") || FilenameExtension.equals(".jpg")) {
			return "image/jpeg";
		}
		if (FilenameExtension.equals(".HTML") || FilenameExtension.equals(".html")) {  
            return "text/html";  
        }  
        if (FilenameExtension.equals(".TXT") || FilenameExtension.equals(".txt")) {  
            return "text/plain";  
        }  
        if (FilenameExtension.equals(".VSD") || FilenameExtension.equals(".vsd")) {  
            return "application/vnd.visio";  
        }  
        if (FilenameExtension.equals(".PPTX") || FilenameExtension.equals(".pptx") || FilenameExtension.equals(".PPT")  
                || FilenameExtension.equals(".ppt")) {  
            return "application/vnd.ms-powerpoint";  
        }  
        if (FilenameExtension.equals(".DOCX") || FilenameExtension.equals(".docx") || FilenameExtension.equals(".DOC")  
                || FilenameExtension.equals(".doc")) {  
            return "application/msword";  
        }  
        if (FilenameExtension.equals(".XML") || FilenameExtension.equals(".xml")) {  
            return "text/xml";  
        }  
        if (FilenameExtension.equals(".apk") || FilenameExtension.equals(".APK")) {  
            return "application/octet-stream";  
        }  
        if (FilenameExtension.equals(".HTML") || FilenameExtension.equals(".html") 
        		||FilenameExtension.equals(".HTM") || FilenameExtension.equals(".htm")) {  
            return "text/html";  
        }
        return "application/octet-stream";  
	}

}
