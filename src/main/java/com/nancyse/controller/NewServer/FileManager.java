package com.nancyse.controller.NewServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.List;
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

public class FileManager {
	
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
	
	
	//��ѯ�ļ�
	
	//ɾ���ļ�
	public  static  int deleteFile(String filePath, String filename,String uploader) {
		//�ļ�������
		if( !isFileExist(filename, filePath, uploader) ) {
			return -1;
		}
		//ɾ�������ļ�
		File file = new File(FilePath.LOCALDIR+filePath+filename);
		file.delete();
		//ɾ�������¼
		FileBufferManager.deleteFileData(filename, filePath, uploader);
		//ɾ���ļ���¼
		FileManager.deleteFileData(filename, filePath, uploader);
		//ɾ��OSS�ϵ��ļ�
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
	
	
	
	
	
	
	//�����ļ�
	public static void updateFile(MultipartFile file,String filePath,String filename,String uploader) throws IllegalStateException, IOException, Exception {
		/*
		//ɾ�������ļ� 
		File tmp = new File(FilePath.LOCALDIR+filePath+filename);
		tmp.delete();
		*/
		//ɾ�������ļ���ͬʱ���ļ������ڱ������ݿ���
		long fileLength = saveFile2Local(file,filePath);
		//�����ļ�ժҪ
		String fileHash=FileEncryptUtil.getSHA256HashCode(file.getBytes());
		//�����ļ�������Կ
		String fileKey = createFileKey(fileHash);
		//�����ļ����ݿ�
		String fileType="txt";
		FileManager.updateFileData(filePath, fileHash, fileKey, filename, fileLength, fileType, uploader);
		//���»����ļ����ݿ�
		FileBufferManager.updateFileData(filePath, filename, uploader, fileLength);
		//�����ļ�
		String path=FilePath.TMPDIR;
		byte[] rawfileData=file.getBytes();
		File filepath = new File(path,filename);
		if( !filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		FileEncryptUtil.encryptFile(rawfileData, encryptfile, fileKey);
		//����OSS�ϵ��ļ�
		OSSConfig ossConfig= new OSSConfig(FilePath.CONFIGFILE);
		String bucketName = "lps-test";
		String key=filePath+filename;
		OSSManageUtil.uploadLocalFile(ossConfig, bucketName, key, encryptfile);
		encryptfile.delete();
	}
	
	
	//�ϴ��ļ���OSS
	public static void uoloadFile2OSS(MultipartFile file,String filePath,long fileLength) throws IOException, Exception {
		//�����ļ�ժҪ
		String fileHash=FileEncryptUtil.getSHA256HashCode(file.getBytes());
		System.out.println("�ļ�ժҪ��"+fileHash);
		//�����ļ�������Կ
		String fileKey = createFileKey(fileHash);
		//�����ļ�ժҪ���ļ�������Կ
		String filename = file.getOriginalFilename();
		String uploader="pslin";
		long fileSize = fileLength;
		saveFileData(fileHash,fileKey,filePath,filename,fileSize,uploader);
		//�����ļ�
		String path=FilePath.TMPDIR;
		byte[] rawfileData=file.getBytes();
		File filepath = new File(path,filename);
		if( !filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		FileEncryptUtil.encryptFile(rawfileData, encryptfile, fileKey);
		//�ϴ���OSS��
		OSSConfig ossConfig= new OSSConfig(FilePath.CONFIGFILE);
		String bucketName = "lps-test";
		String key=filePath+filename;
		OSSManageUtil.uploadLocalFile(ossConfig, bucketName, key, encryptfile);
		encryptfile.delete();
	}
	
	
	/*
	 * ���ļ����浽����
	 * @return �ļ���С
	 */
	public static long saveFile2Local(MultipartFile file,String filepath) throws IllegalStateException, IOException {
		//��������·��
		String path = FilePath.LOCALDIR+filepath;
		String filename = file.getOriginalFilename();
		File newFilePath=new File(path,filename);
		//���·�������ڣ��򴴽�
		if(!newFilePath.getParentFile().exists()) {
			newFilePath.getParentFile().mkdirs();			
		}
		File newfile = new File(path+filename);
		file.transferTo(newfile);	
		long fileLength=newfile.length(); //��BΪ��λ
		return fileLength;
	}
	
	//�����ļ���Ϣ�����ݿ���
	public static void saveFileData(String fileHash,String fileKey,String filedir,String filename,long fileSize,String uploader) {
		DefaultFile df = new DefaultFile();
		df.setFile_hash(fileHash);
		df.setFile_key(fileKey);
		df.setFile_name(filename);
		df.setFile_uploader(uploader);
		df.setFile_dir(filedir);
		df.setFile_size(fileSize);
		SqlSession sqlSession = sqlSessionFactory.openSession();
		String id=statementId+"DefaultFileMapper.save";
		sqlSession.insert(id, df);
		sqlSession.commit();
		sqlSession.close();
	}
	
	//�������ݿ��ļ���Ϣ
	public static void updateFileData(String file_dir,String file_hash,String file_key,
			String file_name, long file_size, String file_type,String file_uploader) {
		DefaultFile df = new DefaultFile();
		df.setFile_dir(file_dir);
		df.setFile_hash(file_hash);
		df.setFile_key(file_key);
		df.setFile_key(file_key);
		df.setFile_name(file_name);
		df.setFile_size(file_size);
		df.setFile_type(file_type);
		df.setFile_uploader(file_uploader);
		SqlSession sqlSession  = sqlSessionFactory.openSession();
		sqlSession.update(statementId+"DefaultFileMapper.updateFile", df);
		sqlSession.commit();
		sqlSession.close();
	}
		
	//ɾ���������ݿ��ļ�
	private static void deleteFileData(String filename,String filePath,String uploader) {
		DefaultFile df = new DefaultFile();
		df.setFile_dir(filePath);
		df.setFile_name(filename);
		df.setFile_uploader(uploader);
		SqlSession sqlSession = sqlSessionFactory.openSession();
		sqlSession.delete(statementId+"DefaultFileMapper.deleteFile", df);
		sqlSession.commit();
		sqlSession.close();		
	}
	
	
	//�����ļ�
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
	
	
	//�����ļ�������Կ
	public static  String createFileKey(String fileHash) {
		//�����ļ�������Կ
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		String fileKey=fileHash+fileHash.substring(0,num);
		return fileKey;
	}
	
	
	//�ж��ļ��Ƿ����
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
