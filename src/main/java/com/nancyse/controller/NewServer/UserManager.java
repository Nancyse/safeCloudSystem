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
@RequestMapping(value="/gs")
public class UserManager {
	
	private static SqlSession sqlSession=null;
	private final static String statementId="com.nancyse.controller.GenericServer.DataModel.UserMapper";
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
	 * 注册用户
	 * @param 用户名
	 */
	@RequestMapping(value="/login",method=RequestMethod.GET)
	@ResponseBody
	public String login(HttpServletRequest req,String username,int type) throws Exception {
		
		//判断用户名是否已注册
		if( isLogIn(username)) {
			return "{\"status\":\"User has been registered\"}";
		}
		//生成默认密码
		String pwd = "pwd-"+username;
		//生成加密密码
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		System.out.println(encryptPwd);	
		//密码保存到数据库中		
		User user = new User(username,encryptPwd,type);
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.insert(statementId+".save", user);
		sqlSession.commit();
		sqlSession.close();
		String result="{\"status\":\"success\"}";
		return result;
	}
	
	
	//判断用户名是否已被注册过
	private Boolean isLogIn(String username) {
		sqlSession = sqlSessionFactory.openSession();
		User user=sqlSession.selectOne(statementId+".selectOne", username);
		if(user!=null)
			return true;
		return false;
	}
	
	/*
	 * 判断用户是否已登录
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
	 * 用户登录
	 */
	@RequestMapping(value="/signIn",method=RequestMethod.GET)
	@ResponseBody
	public String signIn(HttpServletRequest req,
			@RequestParam("username")String username,
			@RequestParam("pwd") String pwd) {
		
		//判断用户是否已登录
		if( isSignIn(req) ) {
			return "{\"status\":\"User has been logged in.\"}";
		}
		String result="fail";
		//验证用户的账号和密码
		if( isUserPass(req,username, pwd) ) {
			result="success";
		}
		String res="{\"status\":\""+result+"\"}";
		return res;
	}
	
	
	//验证用户账号和密码
	private Boolean isUserPass(HttpServletRequest req,String username, String pwd) {
		
		if(username=="" || pwd=="") {
			return false;
		}	
		String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
		sqlSession = sqlSessionFactory.openSession();
		User user = sqlSession.selectOne(statementId+".selectOne",username);
		Boolean result=false;
		if( user!=null && encryptPwd.equals(user.getUser_pwd()) ) {
			HttpSession session = req.getSession();
			if(session.isNew()) {
				session.setAttribute("username", username);
				session.setAttribute("userType", user.user_type);
			}
			result=true;
		}
		return result;
	}
	
	
	/*
	 *注销用户 
	 */
	@RequestMapping(value="/logout")
	@ResponseBody
	public String logout(HttpServletRequest req) {
		
		String result="";
		//判断用户是否已登录
		HttpSession session = req.getSession();
		if( session.isNew() ) {
			result="{\"status\":\"You have not sign in.\"}";
		}
		//注销用户
		session.invalidate();
		result="{\"status\":\"Now you are sign out.\"}";
		return result;
	}
	
	
	/*
	 * 用户修改密码
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
			sqlSession.update(statementId+".updatePwd",user);
			sqlSession.commit();
			sqlSession.close();
			result = "success";
		}
		String res="\"status\":\""+result+"\"";
		return res;
	}
	
	
	/*
	 * 管理员修改密码
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
		sqlSession.update(statementId+".updatePwd", user);
		sqlSession.commit();
		sqlSession.close();
		result="\"status\":\"Success.\"";
		return result;		
	}
	
	
	/*
	 * 查看所有用户信息
	 */
	@RequestMapping(value="/getAllUser")
	@ResponseBody
	public String getAllUser(HttpServletRequest req) {
		String result="";
		if(!isSignIn(req)) {
			result="\"status\":\"You have not sign in.\"";
			return result;
		}
		//判断是否是管理员
		HttpSession session = req.getSession();
		int type=(Integer)session.getAttribute("userType");
		
		if(type!=2) {
			result="\"status\":\"You are not manager.\"";
			return result;
		}
		//从数据库中获取所有用户信息
		sqlSession = sqlSessionFactory.openSession();
		List<User> userList = sqlSession.selectList(statementId+".selectAll");
		ObjectMapper mapper = new ObjectMapper(); 
		try {
			result = mapper.writeValueAsString(userList);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
