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
		if( isFileExist(filename,filepath,uploader)) {//�ļ�����
			//System.out.println("���ļ��Ѵ���");
			result="The file is exist,do you want to update it?";
			
		}else { //�ļ������ڣ����ϴ��ļ�
			long fileLength = saveFile2Local(file,filepath); //���ļ����浽����
			//String uploader="pslin";
			saveBuffer2Database(filename,filepath,fileLength,uploader); //�������ļ���¼���������ݿ���
			saveFile2OSS(file,filepath);
			result="success";
		}	
		return "{\"status\":\""+result+"\"}";		
	}
	
	
	//���ļ����浽OSS
	private void saveFile2OSS(MultipartFile file,String filePath) throws IOException {
		//�����ļ�ժҪ
		String fileHash=FileEncryptUtil.getSHA256HashCode(file.getBytes());
		System.out.println("�ļ�ժҪ��"+fileHash);
		//�����ļ�������Կ
		String fileKey = this.createFileKey(fileHash);
		//�����ļ�ժҪ���ļ�������Կ
		String filename = file.getOriginalFilename();
		String uploader="pslin";
		System.out.println("filePath: "+filePath);
		saveFileData(fileHash,fileKey,filePath,filename,uploader);
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
		OSSConfig ossConfig= new OSSConfig("com/nancyse/controller/GenericServer/config.properties");
		String bucketName = "lps-test";
		String key=filePath+filename;
		OSSManageUtil.uploadLocalFile(ossConfig, bucketName, key, encryptfile);
		encryptfile.delete();
	}
	
	
	//�����ļ���Ϣ
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
	
	
	//�����ļ�������Կ
	private String createFileKey(String fileHash) {
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
	
	
	//�������ļ���¼���浽���ݿ���
	private void saveBuffer2Database(String filename, String filepath,long fileLength,String uploader){
		//�ж��Ƿ��л���ռ�
		if(!isCanBufferFile(fileLength)) {
			System.out.println("�ռ䲻����");	
			//ɾ��������������ʵ��ļ�
			
		}
		//�������ļ����������ݿ���
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
	
	//�ж��Ƿ��л���ռ�
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
	
	
	//�ж��ļ��Ƿ����
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
	 * ���浽����
	 * @return �ļ���С
	 */
	private long saveFile2Local(MultipartFile file,String filepath) throws IllegalStateException, IOException {
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
	
}
