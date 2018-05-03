package com.nancyse.controller.demo.testMybatis;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisTest {
	public static void main(String[] args) throws Exception {
		//��ȡmybatis-config.xml�ļ�
		InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
		
		//��ʼ��mybatis,����SQLSessionFactory���ʵ��
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		//����sessionʵ��
		SqlSession session=sqlSessionFactory.openSession();
		//����User����
		User user =  new User("admin","��",25);
		//��������
		/*
		session.insert("com.nancyse.controller.demo.testMybatis.UserMapper.save",user);
		user = new User("Nancyse","Ů",18);
		session.insert("com.nancyse.controller.demo.testMybatis.UserMapper.save2",user);
		session.update("com.nancyse.controller.demo.testMybatis.UserMapper.update");
		session.delete("com.nancyse.controller.demo.testMybatis.UserMapper.del");
		*/
		List<User> list=session.selectList("com.nancyse.controller.demo.testMybatis.UserMapper.select");
		for( User u:list) {
			System.out.println("name: "+u.getName());
		}
		//�ύ����
		session.commit();
		//�ر�session
		session.close();
	}

}
