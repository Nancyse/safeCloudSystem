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
		//读取mybatis-config.xml文件
		InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
		
		//初始化mybatis,创建SQLSessionFactory类的实例
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		//创建session实例
		SqlSession session=sqlSessionFactory.openSession();
		//创建User对象
		User user =  new User("admin","男",25);
		//插入数据
		/*
		session.insert("com.nancyse.controller.demo.testMybatis.UserMapper.save",user);
		user = new User("Nancyse","女",18);
		session.insert("com.nancyse.controller.demo.testMybatis.UserMapper.save2",user);
		session.update("com.nancyse.controller.demo.testMybatis.UserMapper.update");
		session.delete("com.nancyse.controller.demo.testMybatis.UserMapper.del");
		*/
		List<User> list=session.selectList("com.nancyse.controller.demo.testMybatis.UserMapper.select");
		for( User u:list) {
			System.out.println("name: "+u.getName());
		}
		//提交事务
		session.commit();
		//关闭session
		session.close();
	}

}
