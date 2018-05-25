package com.nancyse.controller.NewServer.Util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.nancyse.controller.GenericServer.FileEncryptUtil;
import com.nancyse.controller.GenericServer.DataModel.User;

public class UserManageUtil {
	
	private static SqlSession sqlSession=null;
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
	
	//创建新用户
	public static int createNewUser( String username,int type,String email) {
		//判断用户名是否已注册
		if( UserManageUtil.isLogIn(email)) {
			System.out.println("用户邮箱已注册");
			return 1;
		}
		//生成用户名
		int num = UserManageUtil.countSameNameNum(username);
		if(num>0) {
			num+=1;
			username=username+num;
		}
		//生成默认密码
		String pwd = "pwd-"+username;
		//生成加密密码
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		//生成个人存储空间
		String userspace = username+"/";
		//将新用户的信息保存到数据库中	
		User user = new User(username,encryptPwd,type,email,userspace);
		UserManageUtil.saveNewUserData(user);	
		System.out.println("新用户添加成功");
		return 0;
	}
	
	//判断用户名是否已被注册过
	public static Boolean isLogIn(String email) {
		sqlSession = sqlSessionFactory.openSession();
		int num=sqlSession.selectOne(statementId+"UserMapper.searchSameEmail", email);
		if(num>0)
			return true;
		return false;
	}
	
	//计算同名的用户名
	public static int countSameNameNum(String username) {
		sqlSession = sqlSessionFactory.openSession();
		User user = new User();
		user.setUser_name(username);
		Integer num=sqlSession.selectOne(statementId+"UserMapper.selectNames", user);
		sqlSession.close();		
		return num;
	}
	
	//存储新用户信息
	public static void saveNewUserData(User user) {
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.insert(statementId+"UserMapper.save", user);
		sqlSession.commit();
		sqlSession.close();		
	}
	
	/*
	 * 判断用户是否已登录
	 * @return  -1:未登录  1:普通用户登陆  2:管理员登录
	 */
	public static int isSignIn(HttpServletRequest req) {
		HttpSession session = req.getSession();
		if( session.isNew() || session.getAttribute("username")==null) {
			return -1;
		}			
		else 
			return (Integer)session.getAttribute("userType");		
	}
	
	//验证用户身份
	public static Boolean isUserPass(HttpServletRequest req,String username, String pwd) {		
		if(username=="" || pwd=="") {
			return false;
		}	
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		
		User user = getOneUser(username);
		Boolean result=false;
		if( user!=null && encryptPwd.equals(user.getUser_pwd()) ) {
			HttpSession session = req.getSession();
			if(session.isNew() || session.getAttribute("username")==null) {
				session.setAttribute("username", username);
				session.setAttribute("userType", user.getUser_type());
				session.setAttribute("userSpace", user.getUser_space());
			}
			result=true;
		}
		return result;
	}
	
	//获取单个用户信息
	public static User getOneUser(String username) {
		sqlSession = sqlSessionFactory.openSession();
		User user = sqlSession.selectOne(statementId+"UserMapper.selectOne",username);
		sqlSession.close();	
		return user;		
	}
		
	//更新用户密码
	public static void updateUserPwd(User user) {
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.update(statementId+"UserMapper.updatePwd",user);
		sqlSession.commit();
		sqlSession.close();		
	}
	
	//获取所有用户信息
	public static List getAllUserData() {
		sqlSession = sqlSessionFactory.openSession();
		List<User> userList = sqlSession.selectList(statementId+"UserMapper.selectAll");
		return userList;		
	}
	
	
	//获取所有用户信息
	public static List<User> getAllUsersByPage(int startRow,int pageSize) {
		Map<String,Object> map = new HashMap<String,Object>();		
		map.put("startRow", startRow);
		map.put("pageSize", pageSize);
		sqlSession = sqlSessionFactory.openSession();
		List<User> userList = sqlSession.selectList(statementId+"UserMapper.getAllUsersByPage",map);
		sqlSession.close();
		return userList;		
	}
	
	
}
