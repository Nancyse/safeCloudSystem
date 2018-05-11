package com.nancyse.controller.NewServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.FileBuffer;

@Controller
@RequestMapping(value="/gs")
public class FileDownloadFromOSS {
	
	private final static String statementId="com.nancyse.controller.GenericServer.DataModel.";
	private static OSSConfig ossConfig=null;
	private static SqlSessionFactory sqlSessionFactory=null;
	static {
		try {
			ossConfig =new OSSConfig(FilePath.CONFIGFILE);
			InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
		    sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		}
		catch( Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 下载文件
	 */
	@RequestMapping(value="/downfile",method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> downloadFile(HttpServletRequest req, 
			@RequestParam("filename") String filename, 
			@RequestParam("filePath") String filePath) throws Exception {
		String result="";
		
		//判断文件没有在本地缓存
		String uploader = "pslin";
		if( ! this.isFileExistInBuffer(filePath, filename, uploader) ) {
			this.getFileFromOSS(filePath, filename, uploader);
		}
		//从本地下载
		return downloadFileFromLocal(filename,filePath);		
	}
	
	
	//从OSS下载文件并且解密
	private void getFileFromOSS(String filePath,String filename,String uploader) throws Exception {
		//从OSS下载
		String bucketName = "lps-test";
		String key = filePath+filename;
		this.downloadFileFromOSS(bucketName, key);
		//解密文件
		File srcFile = new File(FilePath.TMPDIR+key);  
		File decrypfile = new File(FilePath.LOCALDIR+key);
		Map<String,String> dataMap=getFileKeyAndFileHash(filePath,filename,uploader);
		String fileKey=dataMap.get("fileKey");
		String fileHash=dataMap.get("fileHash");
		System.out.println("fileKey: "+fileKey);
		File file=FileEncryptUtil.decryptFile2(srcFile,decrypfile,fileKey);
		srcFile.delete();
		//增加缓存记录
		long fileLength=decrypfile.length();
		FileBufferManager.saveBuffer2Database(filename,filePath,fileLength,uploader);
	}
	
	
	//判断文件是否已缓存
	private Boolean isFileExistInBuffer(String filePath,String filename, String uploader) {
		//判断文件在缓存中是否存在
		SqlSession sqlSession = sqlSessionFactory.openSession();
		FileBuffer fb = new FileBuffer();
		fb.setBufferfile_name(filename);
		fb.setBufferfile_dir(filePath);
		fb.setBufferfile_uploader(uploader);
		fb = sqlSession.selectOne(statementId+"FileBufferMapper.selectOne");
		sqlSession.close();
		return fb==null ? false:true;
	}
	
	
	//获取文件加密密钥和文件哈希值
	private Map getFileKeyAndFileHash(String filePath,String filename,String uploader) {
		String result="";
		SqlSession sqlSession=sqlSessionFactory.openSession();
		DefaultFile df = new DefaultFile();
		df.setFile_dir(filePath);
		df.setFile_name(filename);
		df.setFile_uploader(uploader);
		DefaultFile resDF = sqlSession.selectOne(statementId+"DefaultFileMapper.selectOne", df);
		//System.out.println("resDF: "+resDF==null);
		sqlSession.close();
		Map<String,String> map = new HashMap<String,String>();
		map.put("fileKey", resDF.getFile_key());
		map.put("fileHash", resDF.getFile_hash());
		return map;
	}
	
	
	
	//从本地下载
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
	private void downloadFileFromOSS(String bucketName, String key) {
		OSSManageUtil.downloadFile2Local(ossConfig, bucketName, key);
	}

}
