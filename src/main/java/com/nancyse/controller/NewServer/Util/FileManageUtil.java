package com.nancyse.controller.NewServer.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.FileBuffer;
import com.nancyse.controller.NewServer.Const.FilePath;
import com.nancyse.controller.NewServer.Const.OSSConfig;

public class FileManageUtil {
	
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
	
		
	//删除文件
	public  static  int deleteFile(String filePath, String filename,String uploader) {
		//文件不存在
		if( !isFileExist(filename, filePath, uploader) ) {
			return -1;
		}
		//删除本地文件
		File file = new File(FilePath.LOCALDIR+filePath+filename);
		file.delete();
		//删除缓存记录
		FileBufferManageUtil.deleteFileData(filename, filePath, uploader);
		//删除文件记录
		FileManageUtil.deleteFileData(filename, filePath, uploader);
		//删除OSS上的文件
		String bucketName="lps-test";
		OSSConfig ossConfig;
		try {
			ossConfig = new OSSConfig(FilePath.CONFIGFILE);
			OSSManageUtil.deleteFile(ossConfig, bucketName, filePath+filename);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		return 1;	
	}
	
	
	
	//更新文件
	public static void updateFile(MultipartFile file,String filePath,String filename,String uploader,String desc) throws IllegalStateException, IOException, Exception {
		/*
		//删除本地文件 
		File tmp = new File(FilePath.LOCALDIR+filePath+filename);
		tmp.delete();
		*/
		//删除本地文件，同时新文件保存在本地数据库中
		long fileLength = saveFile2Local(file,filePath);
		//生成文件摘要
		String fileHash=FileEncryptUtil.getSHA256HashCode(file.getBytes());
		//生成文件加密密钥
		String fileKey = createFileKey(fileHash);
		//更新文件数据库
		String fileType="txt";
		FileManageUtil.updateFileData(filePath, fileHash, fileKey, filename, fileLength, fileType, uploader,desc);
		//更新缓存文件数据库
		FileBufferManageUtil.updateFileData(filePath, filename, uploader, fileLength);
		//加密文件
		String path=FilePath.TMPDIR;
		byte[] rawfileData=file.getBytes();
		File filepath = new File(path,filename);
		if( !filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		FileEncryptUtil.encryptFile(rawfileData, encryptfile, fileKey);
		//更新OSS上的文件
		OSSConfig ossConfig= new OSSConfig(FilePath.CONFIGFILE);
		String bucketName = "lps-test";
		String key=filePath+filename;
		OSSManageUtil.uploadLocalFile(ossConfig, bucketName, key, encryptfile);
		encryptfile.delete();
	}
	
	
	//上传文件到OSS
	public static void uoloadFile2OSS(MultipartFile file,String filePath,long fileLength,String desc) throws IOException, Exception {
		//生成文件摘要
		String fileHash=FileEncryptUtil.getSHA256HashCode(file.getBytes());
		System.out.println("文件摘要："+fileHash);
		//生成文件加密密钥
		String fileKey = createFileKey(fileHash);
		//保存文件摘要和文件加密密钥
		String filename = file.getOriginalFilename();
		String uploader="pslin";
		long fileSize = fileLength;
		int index = filename.indexOf('.');
		String fileType="null";
		if(index>=0)
			fileType=filename.substring(filename.indexOf('.')+1);
		saveFileData(fileHash,fileKey,filePath,filename,fileType,fileSize,uploader,desc);
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
		OSSConfig ossConfig= new OSSConfig(FilePath.CONFIGFILE);
		String bucketName = "lps-test";
		String key=filePath+filename;
		OSSManageUtil.uploadLocalFile(ossConfig, bucketName, key, encryptfile);
		encryptfile.delete();
	}
	
	
	/*
	 * 将文件保存到本地
	 * @return 文件大小
	 */
	public static long saveFile2Local(MultipartFile file,String filepath) throws IllegalStateException, IOException {
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
	
	/*
	 * 将字符串保存到本地文件
	 * 返回字符串的长度
	 */
	public static long saveStr2Local(String filename,String filePath,String data) throws IOException {
		//创建本地路径
		String path = FilePath.LOCALDIR+filePath;
		File newFile=new File(path,filename);
		//如果路径不存在，则创建
		if(!newFile.getParentFile().exists()) {
			newFile.getParentFile().mkdirs();			
		}
		//将字符串保存到文件中
		FileWriter fw = new FileWriter(newFile);
		fw.write(data);
		fw.close();
		return data.length();		
	} 
	
	//保存文件信息到数据库中
	public static void saveFileData(String fileHash,String fileKey,String filedir,String filename,String fileType,long fileSize,String uploader,String desc) {
		DefaultFile df = new DefaultFile();
		df.setFile_hash(fileHash);
		df.setFile_key(fileKey);
		df.setFile_name(filename);
		df.setFile_uploader(uploader);
		df.setFile_dir(filedir);
		df.setFile_size(fileSize);
		df.setFile_type(fileType);
		df.setFile_desc(desc);
		df.setUpload_time(DateUtil.getCurrentTimeAsDate());
		SqlSession sqlSession = sqlSessionFactory.openSession();
		String id=statementId+"DefaultFileMapper.save";
		sqlSession.insert(id, df);
		sqlSession.commit();
		sqlSession.close();
	}
	
	//更新数据库文件信息
	public static void updateFileData(String file_dir,String file_hash,String file_key,
			String file_name, long file_size, String file_type,String file_uploader,
			String file_desc) {
		DefaultFile df = new DefaultFile();
		df.setFile_dir(file_dir);
		df.setFile_hash(file_hash);
		df.setFile_key(file_key);
		df.setFile_key(file_key);
		df.setFile_name(file_name);
		df.setFile_size(file_size);
		df.setFile_type(file_type);
		df.setFile_uploader(file_uploader);
		df.setFile_desc(file_desc);
		df.setUpload_time(DateUtil.getCurrentTimeAsDate());
		SqlSession sqlSession  = sqlSessionFactory.openSession();
		sqlSession.update(statementId+"DefaultFileMapper.updateFile", df);
		sqlSession.commit();
		sqlSession.close();
	}
		
	//删除单个数据库文件
	public static void deleteFileData(String filename,String filePath,String uploader) {
		DefaultFile df = new DefaultFile();
		df.setFile_dir(filePath);
		df.setFile_name(filename);
		df.setFile_uploader(uploader);
		SqlSession sqlSession = sqlSessionFactory.openSession();
		sqlSession.delete(statementId+"DefaultFileMapper.deleteFile", df);
		sqlSession.commit();
		sqlSession.close();		
	}
	
	//返回所有的文件
	public static List<DefaultFile> getAllFiles() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		List<DefaultFile> dfList = sqlSession.selectList(statementId+"DefaultFileMapper.getAllFile");
		sqlSession.close();
		return dfList;		
	}
	
	//返回当前用户所有的文件
	public static List<DefaultFile> getUserAllFiles(String uploader) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		DefaultFile df = new DefaultFile();
		df.setFile_uploader(uploader);
		List<DefaultFile> dfList = sqlSession.selectList(statementId+"DefaultFileMapper.getUserAllFile",df);
		sqlSession.close();
		return dfList;		
	}
	
	//查找文件
	public static List findFile(String uploader,String word) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		List<DefaultFile> dfList = sqlSession.selectList(statementId+"DefaultFileMapper.findFile", uploader);
		List<DefaultFile> resList=new ArrayList<DefaultFile>();
		for(DefaultFile df:dfList) {
			String filePath = df.getFile_dir()+df.getFile_name();
			if( filePath.indexOf(word)>=0) {
				resList.add(df);
			}
		}
		sqlSession.close();
		return resList;
	}
	
	//查找文件hash和key
	public static Map getFileHashAndKey(String filename, String filePath, String uploader) {
		Map<String,String> map = new HashMap<String,String>();
		SqlSession sqlSession = sqlSessionFactory.openSession();
		DefaultFile df = new DefaultFile();
		df.setFile_name(filename);
		df.setFile_dir(filePath);
		df.setFile_uploader(uploader);
		
		DefaultFile result = sqlSession.selectOne(statementId+"DefaultFileMapper.getFileHashAndKey", df);
		map.put("fileHash", result.getFile_hash());
		map.put("fileKey", result.getFile_key());
		
		sqlSession.close();
		return map;
		
	}
	
	//获取文件类型
	public static String getFileType(String filename) {
		int index = filename.indexOf('.');
		String fileType="null";
		if(index>=0)
			fileType=filename.substring(filename.indexOf('.')+1);
		return fileType;
	}
	
	//生成文件加密密钥
	public static  String createFileKey(String fileHash) {
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
	
	
	//判断文件是否存在
	public static Boolean isFileExist(String filename, String filepath,String uploader) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		DefaultFile df = new DefaultFile();
		df.setFile_name(filename);
		df.setFile_dir(filepath);
		df.setFile_uploader(uploader);
		DefaultFile result = sqlSession.selectOne(statementId+"DefaultFileMapper.selectOne", df);
		return result==null?false:true;
	}	

	
}
