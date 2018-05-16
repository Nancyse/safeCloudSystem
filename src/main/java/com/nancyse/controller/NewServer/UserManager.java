package com.nancyse.controller.NewServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nancyse.controller.GenericServer.FileEncryptUtil;
import com.nancyse.controller.GenericServer.DataModel.User;

@Controller
@RequestMapping(value="/bs")
public class UserManager {
	
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
	
	
	/*
	 * ע���û�
	 * @param �û���
	 */
	@RequestMapping(value="/login",method=RequestMethod.GET)
	@ResponseBody
	public String login(HttpServletRequest req,
			@RequestParam("username") String username,
			@RequestParam("type") int type,
			@RequestParam("email") String email) throws Exception {
		
		//�ж��û����Ƿ���ע��
		if( isLogIn(email)) {
			return "{\"status\":\"User has been registered\"}";
		}
		//�����û���
		int num = getSameNameNum(username);
		if(num>0) {
			num+=1;
			username=username+num;
		}
		//����Ĭ������
		String pwd = "pwd-"+username;
		//���ɼ�������
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		//���ɸ��˴洢�ռ�
		String userspace=username+"/";
		//�����û�����Ϣ���浽���ݿ���	
		User user = new User(username,encryptPwd,type,email,userspace);
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.insert(statementId+"UserMapper.save", user);
		sqlSession.commit();
		sqlSession.close();
		String result="{\"status\":\"success\"}";
		return result;
	}
	
	
	private int getSameNameNum(String username) {
		// TODO Auto-generated method stub
		sqlSession = sqlSessionFactory.openSession();
		User user = new User();
		user.setUser_name(username);
		Integer num=sqlSession.selectOne(statementId+"UserMapper.selectNames", user);
		sqlSession.close();
		
		return num;
	}


	//�ж��û����Ƿ��ѱ�ע���
	private Boolean isLogIn(String email) {
		sqlSession = sqlSessionFactory.openSession();
		int num=sqlSession.selectOne(statementId+"UserMapper.searchSameEmail", email);
		if(num>0)
			return true;
		return false;
	}
	
	/*
	 * �ж��û��Ƿ��ѵ�¼
	 */
	private Boolean isSignIn(HttpServletRequest req) {
		HttpSession session = req.getSession();
		if( session.isNew()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	
	/*
	 * �û���¼
	 */
	@RequestMapping(value="/signIn",method=RequestMethod.GET)
	@ResponseBody
	public String signIn(HttpServletRequest req,
			@RequestParam("username")String username,
			@RequestParam("pwd") String pwd) {
		//�ж��û��Ƿ��ѵ�¼
		if( isSignIn(req) ) {
			return "{\"error_code\":1}";
		}
		int errorCode=-1;
		//��֤�û����˺ź�����
		if( isUserPass(req,username, pwd) ) {
			errorCode=0;
		}
		String res="{\"error_code\":"+errorCode+"}";
		return res;
	}
	
	
	//��֤�û��˺ź�����
	private Boolean isUserPass(HttpServletRequest req,String username, String pwd) {
		
		if(username=="" || pwd=="") {
			return false;
		}	
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		sqlSession = sqlSessionFactory.openSession();
		User user = sqlSession.selectOne(statementId+"UserMapper.selectOne",username);
		Boolean result=false;
		if( user!=null && encryptPwd.equals(user.getUser_pwd()) ) {
			HttpSession session = req.getSession();
			if(session.isNew()) {
				session.setAttribute("username", username);
				session.setAttribute("userType", user.getUser_type());
			}
			result=true;
		}
		return result;
	}
	
	
	/*
	 *ע���û� 
	 */
	@RequestMapping(value="/logout")
	@ResponseBody
	public String logout(HttpServletRequest req) {
		
		String result="";
		//�ж��û��Ƿ��ѵ�¼
		HttpSession session = req.getSession();
		if( session.isNew() ) {
			result="{\"status\":\"You have not sign in.\"}";
		}
		//ע���û�
		session.invalidate();
		result="{\"status\":\"Now you are sign out.\"}";
		return result;
	}
	
	
	/*
	 * �û��޸�����
	 */
	@RequestMapping(value="/updatePasswordByUser",method=RequestMethod.GET)
	@ResponseBody
	public String updatePasswordByUser(
			HttpServletRequest req,
			@RequestParam("username") String username, 
			@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword) {
		
		if(!isSignIn(req)) {
			return "\"status\":\"You have not logged in.\"";
		}
		String result="fail";
		if( isUserPass(req,username,oldPassword)) {
			String newPwd = FileEncryptUtil.getSHA256HashCode(newPassword.getBytes());
			User user = new User();
			user.setUser_name(username);
			user.setUser_pwd(newPwd);
			sqlSession = sqlSessionFactory.openSession();
			sqlSession.update(statementId+"UserMapper.updatePwd",user);
			sqlSession.commit();
			sqlSession.close();
			result = "success";
		}
		String res="\"status\":\""+result+"\"";
		return res;
	}
	
	
	/*
	 * ����Ա�޸�����
	 */
	@RequestMapping(value="/updatePasswordByManager")
	@ResponseBody
	public String updatePasswordByManager(HttpServletRequest req,
			@RequestParam("username") String username,
			@RequestParam("pwd") String pwd) {
		
		String result="";
		if(!isSignIn(req)) {
			result="\"status\":\"You have not sign in.\"";
			return result;
		}
		
		HttpSession session = req.getSession();
		int type=(Integer)session.getAttribute("userType");
		
		if(type!=2) {
			result="\"status\":\"You are not manager.\"";
			return result;
		}
		
		if( !isLogIn(username)) {
			result="\"status\":\"Username is not exist.\"";
			return result;
		}
		
		if( pwd==null || pwd=="") {
			result="\"status\":\"The password is not null.\"";
			return result;
		}
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		User user=new User();
		user.setUser_name(username);
		user.setUser_pwd(encryptPwd);
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.update(statementId+"UserMapper.updatePwd", user);
		sqlSession.commit();
		sqlSession.close();
		result="\"status\":\"Success.\"";
		return result;		
	}
	
	
	/*
	 * �鿴�����û���Ϣ
	 */
	@RequestMapping(value="/getAllUser")
	@ResponseBody
	public String getAllUser(HttpServletRequest req) {
		String result="";
		if(!isSignIn(req)) {
			result="\"status\":\"You have not sign in.\"";
			return result;
		}
		//�ж��Ƿ��ǹ���Ա
		HttpSession session = req.getSession();
		int type=(Integer)session.getAttribute("userType");
		
		if(type!=2) {
			result="\"status\":\"You are not manager.\"";
			return result;
		}
		//�����ݿ��л�ȡ�����û���Ϣ
		sqlSession = sqlSessionFactory.openSession();
		List<User> userList = sqlSession.selectList(statementId+"UserMapper.selectAll");
		ObjectMapper mapper = new ObjectMapper(); 
		try {
			result = mapper.writeValueAsString(userList);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
