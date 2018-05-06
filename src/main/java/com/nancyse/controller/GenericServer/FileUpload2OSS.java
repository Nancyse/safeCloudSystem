package com.nancyse.controller.GenericServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	private final static String statementId="com.nancyse.controller.GenericServer.DataModel.";
	private static SqlSessionFactory sqlSessionFactory=null;
	static {
		try {
			InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
		    sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	//上传文件
	@RequestMapping(value="/fileIndex",method=RequestMethod.GET)
	public String uploadIndex(HttpServletRequest req) {
		return "fileUploadTest/uploadForm2OSS";		
	}
	
	//上传文件逻辑
	@RequestMapping(value="/fileUpload",method=RequestMethod.POST)
	@ResponseBody
	public String fileupload(HttpServletRequest req,
			@RequestParam("description") String desc,
			@RequestParam("file") MultipartFile file,
			@RequestParam("filePath") String filepath) throws Exception {
		
		String result="";
		//判断文件是否为空
		if(file.isEmpty()) {
			result="You could not choose a file .";
			return "{\"satuc\":\""+result+"\"}";
		}
		String filename=file.getOriginalFilename();
		//判断文件是否存在
		String uploader="pslin";
		if( isFileExist(filename,filepath,uploader)) {//文件存在
			//System.out.println("该文件已存在");
			result="The file is exist,do you want to update it?";
			
		}else { //文件不存在，则上传文件
			long fileLength = saveFile2Local(file,filepath); //将文件保存到本地
			//String uploader="pslin";
			saveBuffer2Database(filename,filepath,fileLength,uploader); //将缓存文件记录保存在数据库中
			saveFile2OSS(file,filepath);
			result="success";
		}	
		return "{\"status\":\""+result+"\"}";		
	}
	
	
	//将文件保存到OSS
	private void saveFile2OSS(MultipartFile file,String filePath) throws IOException {
		//生成文件摘要
		String fileHash=FileEncryptUtil.getSHA256HashCode(file.getBytes());
		System.out.println("文件摘要："+fileHash);
		//生成文件加密密钥
		String fileKey = this.createFileKey(fileHash);
		//保存文件摘要和文件加密密钥
		String filename = file.getOriginalFilename();
		String uploader="pslin";
		System.out.println("filePath: "+filePath);
		saveFileData(fileHash,fileKey,filePath,filename,uploader);
		//加密文件
		String path=FilePath.TMPDIR;
		byte[] rawfileData=file.getBytes();
		File filepath = new File(path,filename);
		if( !filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		FileEncryptUtil.encryptFile(rawfileData, encryptfile, fileKey);
		//上传到OSS上
		OSSConfig ossConfig= new OSSConfig("com/nancyse/controller/GenericServer/config.properties");
		String bucketName = "lps-test";
		String key=filePath+filename;
		OSSManageUtil.uploadLocalFile(ossConfig, bucketName, key, encryptfile);
		encryptfile.delete();
	}
	
	
	//保存文件信息
	private void saveFileData(String fileHash,String fileKey,String filedir,String filename,String uploader) {
		DefaultFile df = new DefaultFile();
		df.setFile_hash(fileHash);
		df.setFile_key(fileKey);
		df.setFile_name(filename);
		df.setFile_uploader(uploader);
		df.setFile_dir(filedir);
		SqlSession sqlSession = sqlSessionFactory.openSession();
		String id=statementId+"DefaultFileMapper.save";
		sqlSession.insert(id, df);
		sqlSession.commit();
		sqlSession.close();
	}
	
	
	//生成文件加密密钥
	private String createFileKey(String fileHash) {
		//生成文件加密密钥
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		String fileKey=fileHash+fileHash.substring(0,num);
		return fileKey;
	}
	
	
	//将缓存文件记录保存到数据库中
	private void saveBuffer2Database(String filename, String filepath,long fileLength,String uploader){
		//判断是否还有缓存空间
		if(!isCanBufferFile(fileLength)) {
			System.out.println("空间不足了");	
			//删除最近不常被访问的文件
			
		}
		//将缓存文件保存在数据库中
		FileBuffer fb = new FileBuffer();
		fb.setBufferfile_name(filename);
		fb.setBufferfile_dir(filepath);
		fb.setUpdate_time(DateUtil.getCurrentTime());
		fb.setBufferfile_size(fileLength);
		fb.setBufferfile_uploader(uploader);
		String id="FileBufferMapper.save";
		SqlSession sqlSession = sqlSessionFactory.openSession();
		sqlSession.insert(statementId+id,fb);
		sqlSession.commit();
		sqlSession.close();	
	}
	
	//判断是否有缓存空间
	private Boolean isCanBufferFile(long filelen) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		//sqlSession.selectList(statementId+"FileBufferMapper.countAll");
		long currentSpace=sqlSession.selectOne(statementId+"FileBufferMapper.countSpace");
		long totalSpace = 1024*1024*1024*30;
		//long totalSpace=628;
		if( totalSpace>(currentSpace+filelen) ) {
			return true;
		}
		else
			return false;
	}
	
	
	//判断文件是否存在
	private Boolean isFileExist(String filename, String filepath,String uploader) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		DefaultFile df = new DefaultFile();
		df.setFile_name(filename);
		df.setFile_dir(filepath);
		df.setFile_uploader(uploader);
		DefaultFile result = sqlSession.selectOne(statementId+"DefaultFileMapper.selectOne", df);
		return result==null?false:true;
	}
	
	
	/*
	 * 保存到本地
	 * @return 文件大小
	 */
	private long saveFile2Local(MultipartFile file,String filepath) throws IllegalStateException, IOException {
		//创建本地路径
		String path = FilePath.LOCALDIR+filepath;
		String filename = file.getOriginalFilename();
		File newFilePath=new File(path,filename);
		//如果路径不存在，则创建
		if(!newFilePath.getParentFile().exists()) {
			newFilePath.getParentFile().mkdirs();			
		}
		File newfile = new File(path+filename);
		file.transferTo(newfile);	
		long fileLength=newfile.length(); //以B为单位
		return fileLength;
	}
	
}
