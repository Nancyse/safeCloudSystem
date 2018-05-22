package com.nancyse.controller.NewServer.Util;

import java.io.InputStream;
import java.util.List;

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
	
	//�������û�
	public static int createNewUser( String username,int type,String email) {
		//�ж��û����Ƿ���ע��
		if( UserManageUtil.isLogIn(email)) {
			return 1;
		}
		//�����û���
		int num = UserManageUtil.countSameNameNum(username);
		if(num>0) {
			num+=1;
			username=username+num;
		}
		//����Ĭ������
		String pwd = "pwd-"+username;
		//���ɼ�������
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		//���ɸ��˴洢�ռ�
		String userspace = username+"/";
		//�����û�����Ϣ���浽���ݿ���	
		User user = new User(username,encryptPwd,type,email,userspace);
		UserManageUtil.saveNewUserData(user);	
		return 0;
	}
	
	//�ж��û����Ƿ��ѱ�ע���
	public static Boolean isLogIn(String email) {
		sqlSession = sqlSessionFactory.openSession();
		int num=sqlSession.selectOne(statementId+"UserMapper.searchSameEmail", email);
		if(num>0)
			return true;
		return false;
	}
	
	//����ͬ�����û���
	public static int countSameNameNum(String username) {
		sqlSession = sqlSessionFactory.openSession();
		User user = new User();
		user.setUser_name(username);
		Integer num=sqlSession.selectOne(statementId+"UserMapper.selectNames", user);
		sqlSession.close();		
		return num;
	}
	
	//�洢���û���Ϣ
	public static void saveNewUserData(User user) {
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.insert(statementId+"UserMapper.save", user);
		sqlSession.commit();
		sqlSession.close();		
	}
	
	/*
	 * �ж��û��Ƿ��ѵ�¼
	 * @return  -1:δ��¼  1:��ͨ�û���½  2:����Ա��¼
	 */
	public static int isSignIn(HttpServletRequest req) {
		HttpSession session = req.getSession();
		if( session.isNew() || session.getAttribute("username")==null) {
			return -1;
		}			
		else 
			return (Integer)session.getAttribute("userType");		
	}
	
	//��֤�û����
	public static Boolean isUserPass(HttpServletRequest req,String username, String pwd) {		
		if(username=="" || pwd=="") {
			return false;
		}	
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		sqlSession = sqlSessionFactory.openSession();
		User user = sqlSession.selectOne(statementId+"UserMapper.selectOne",username);
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
		
		
	//�����û�����
	public static void updateUserPwd(User user) {
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.update(statementId+"UserMapper.updatePwd",user);
		sqlSession.commit();
		sqlSession.close();		
	}
	
	//��ȡ�����û���Ϣ
	public static List getAllUserData() {
		sqlSession = sqlSessionFactory.openSession();
		List<User> userList = sqlSession.selectList(statementId+"UserMapper.selectAll");
		return userList;		
	}
}
