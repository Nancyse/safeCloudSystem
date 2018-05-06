package com.nancyse.controller.GenericServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.SimplifiedObjectMeta;

/*
 * OSS文件管理的OSSManageUtil工具类
 */
public class OSSManageUtil {
	
	/*
	 * 判断文件Object是否存在
	 */
	public static Boolean isObjectExist(OSSConfig ossConfig,String bucketName,String key) {
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		
		//创建OSSClient实例
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		//Object是否存在
		Boolean found= ossClient.doesObjectExist(bucketName, key);
		ossClient.shutdown();
		return found;
	}
	
	
	//获取文件元信息
	public static void getObjectMeta(OSSConfig ossConfig, String bucketName, String key) throws ParseException {
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		//创建OSSClient实例
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		//获取文件的部分元信息
		SimplifiedObjectMeta objectMeta = ossClient.getSimplifiedObjectMeta(bucketName, key);
		System.out.println("获取文件部分元信息：");
		System.out.println(objectMeta.getSize());
		System.out.println(objectMeta.getETag());
		System.out.println(objectMeta.getLastModified());
		//获取文件的全部元信息
		ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, key);
		System.out.println("获取文件全部信息：");
		System.out.println(metadata.getContentType());
		System.out.println(metadata.getLastModified());
		//System.out.println(metadata.getExpirationTime());
		//关闭client
		ossClient.shutdown();
	}
	
	/*
	 * 上传本地文件
	 */
	public static void uploadLocalFile(OSSConfig ossConfig, String bucketName, String key, File file) {
		String endpoint=ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		//创建OSSClient实例
		OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
		//上传文件
		ossClient.putObject(bucketName, key, file);
		ossClient.shutdown();
	}
	
	/*
	 * 上传OSS服务器文件
	 * @param multipartFile spring 上传的文件
	 * remotePath @param oss服务器二级目录
	 * @throws Exception 设定文件@return String
	 */
	public static String uploadFile(InputStream fileContent,String remotePath,String fileName) throws IOException {
		//随机名处理
		//fileName="lps-test_"+new Date().getTime()+fileName.substring(fileName.lastIndexOf("."));
		//加载配置文件，初始化OSSClient
		OSSConfig ossConfig = new OSSConfig("com/nancyse/controller/demo/testOSS/config.properties");
		OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(),ossConfig.getAccessKeyId(),ossConfig.getAccessKeySecret());
		//定义二级目录
		//String remoteFilePath = remotePath.substring(0, remotePath.length()).replaceAll("\\\\","/")+"/";
		String remoteFilePath = remotePath;
		//创建上传Object的Metadata
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(fileContent.available());;
		objectMetadata.setContentEncoding("utf-8");
		objectMetadata.setCacheControl("no-cache");
		objectMetadata.setHeader("pragma", "no-cache");
		objectMetadata.setContentType(contentType(fileName.substring(fileName.lastIndexOf("."))));
		objectMetadata.setContentDisposition("inline;filename="+fileName);
		//上传文件
		ossClient.putObject(ossConfig.getBucketName(), remoteFilePath+fileName, fileContent,objectMetadata);
		//关闭OSSClient
		ossClient.shutdown();
		//关闭io流
		fileContent.close();
		ossClient.shutdown();
		return ossConfig.getAccessUrl()+"/"+remoteFilePath+fileName;
	}
	
	
	/*
	 * 下载到本地
	 */
	public static void downloadFile(OSSConfig ossConfig, String key, String filename) throws IOException {
		//初始化OSSClient
		OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(),ossConfig.getAccessKeyId(),ossConfig.getAccessKeySecret());
		OSSObject object = ossClient.getObject(ossConfig.getBucketName(),key);
		
		//获取ObjectMeta
		ObjectMetadata meta = object.getObjectMetadata();
		//获取Object的输入流,下载到本地
		InputStream objectContent = object.getObjectContent();
		ObjectMetadata objectData = ossClient.getObject(new GetObjectRequest(ossConfig.getBucketName(),key), new File(filename));
		//关闭数据流
		objectContent.close();
		ossClient.shutdown();
		
	}
	
	
	/*
	 * 删除文件
	 * 根据key删除OSS服务器上的文件 @Title: deleteFile @Description: @param @param 
     * ossConfigure @param @param filePath 设定文件 @return void 返回类型 @throws 
     * @throws IOException  
	 */
	public static void deleteFile(OSSConfig ossConfig,String bucketName, String key) throws IOException{
		//加载配置文件
		String endpoint = ossConfig.getEndpoint();
		String accessKeyId = ossConfig.getAccessKeyId();
		String accessKeySecret = ossConfig.getAccessKeySecret();
		
		OSSClient ossClient= new OSSClient(endpoint,accessKeyId,accessKeySecret);
		ossClient.deleteObject(bucketName, key);
	}
	
	
	
	/*
	 * description: 判断OSS服务文件上传时文件的contentType
	 * @param FilenameExtension 文件后缀
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
