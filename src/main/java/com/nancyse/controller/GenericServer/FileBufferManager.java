package com.nancyse.controller.GenericServer;

import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.FileBuffer;

public class FileBufferManager {
	
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
	
	
	//�������ļ���¼���浽���ݿ���
	public static void saveBuffer2Database(String filename, String filepath,long fileLength,String uploader){
		//�ж��Ƿ��л���ռ�
		if(!isCanBufferFile(fileLength)) {
			System.out.println("�ռ䲻����");	
			//ɾ��������������ʵ��ļ�
			
		}
		//�������ļ����������ݿ���
		FileBuffer fb = new FileBuffer();
		fb.setBufferfile_name(filename);
		fb.setBufferfile_dir(filepath);
		fb.setUpdate_time(DateUtil.getCurrentTimeAsDate());
		fb.setBufferfile_size(fileLength);
		fb.setBufferfile_uploader(uploader);
		String id="FileBufferMapper.save";
		SqlSession sqlSession = sqlSessionFactory.openSession();
		sqlSession.insert(statementId+id,fb);
		sqlSession.commit();
		sqlSession.close();	
	}
	
	
	//�ж��Ƿ��л���ռ�
	private static Boolean isCanBufferFile(long filelen) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		//sqlSession.selectList(statementId+"FileBufferMapper.countAll");
		long currentSpace=sqlSession.selectOne(statementId+"FileBufferMapper.countSpace");
		sqlSession.close();
		long totalSpace = 1024*1024*1024*30;
		//long totalSpace=628;
		if( totalSpace>(currentSpace+filelen) ) {
			return true;
		}
		else
			return false;		
	}
	
	
	//�����ļ�
	public static void updateFileData(String filePath,String filename, String uploader, long fileLength) {
		FileBuffer fb =  new FileBuffer();
		fb.setBufferfile_dir(filePath);
		fb.setBufferfile_name(filename);
		fb.setBufferfile_size(fileLength);
		fb.setBufferfile_uploader(uploader);
		fb.setUpdate_time(DateUtil.getCurrentTimeAsDate());	
		SqlSession sqlSession = sqlSessionFactory.openSession();
		sqlSession.update(statementId+"FileBufferMapper.updateFile", fb);
		sqlSession.commit();
		sqlSession.close();			
	}
	
	//ɾ���ļ�
	public static void deleteFileData(String filename,String filePath,String uploader) {
		FileBuffer fb = new FileBuffer();
		fb.setBufferfile_dir(filePath);
		fb.setBufferfile_name(filename);
		fb.setBufferfile_uploader(uploader);
		SqlSession sqlSession = sqlSessionFactory.openSession();
		sqlSession.delete(statementId+"FileBufferMapper.deleteFile", fb);
		sqlSession.commit();
		sqlSession.close();		
	}
	
}
