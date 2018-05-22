package com.nancyse.controller.NewServer.Util;

import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.FileBuffer;

public class FileBufferManageUtil {
	
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
	
	
	//将缓存文件记录保存到数据库中
	public static void saveBuffer2Database(String filename, String filepath,long fileLength,String uploader){
		//判断是否还有缓存空间
		if(!isCanBufferFile(fileLength)) {
			System.out.println("空间不足了");	
			//删除最近不常被访问的文件
			
		}
		//将缓存文件保存在数据库中
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
	
	
	//判断是否有缓存空间
	private static Boolean isCanBufferFile(long filelen) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		long currentSpace = 0;
		Object result = sqlSession.selectOne(statementId+"FileBufferMapper.countSpace");
		if(result != null) {
			currentSpace = (Long)result;
		}
		System.out.println("当前的空间大小："+(currentSpace+filelen));
		sqlSession.close();
		long totalSpace = 1024*1024*1024*30;
		System.out.println("总空间大小："+totalSpace);
		//long totalSpace=628;
		if( totalSpace>(currentSpace+filelen) ) {
			return true;
		}
		else
			return false;		
	}
	
	//判断文件是否已缓存
	public static Boolean hasFile(String filePath,String filename, String uploader) {
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
	
	//更新文件
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
	
	//删除文件
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
