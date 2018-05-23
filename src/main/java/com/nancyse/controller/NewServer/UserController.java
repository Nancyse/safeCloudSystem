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
import com.nancyse.controller.NewServer.Util.UserManageUtil;

@Controller
@RequestMapping(value="/server")
public class UserController {
	
	/*
	 * 注册用户
	 * @param 用户名
	 */
	@RequestMapping(value="/register",method=RequestMethod.POST)
	@ResponseBody
	public String register(HttpServletRequest req,
			@RequestParam("username") String username,
			@RequestParam("type") int type,
			@RequestParam("email") String email) throws Exception {
		
		int error_code;   
		int state = UserManageUtil.isSignIn(req); //用户登录状态
		if( state == -1) { //用户未登录，跳转登录界面	
			error_code = -1; 			
		}
		else if(state == 1) {//用户是普通用户，没有权限创建用户
			error_code = -2;
		}
		else {
			error_code = UserManageUtil.createNewUser(username, type, email);		
		}		
		String result="{\"error_code\":"+error_code+"}";
		
		return result;
	}
	
	/*
	 * 用户登录
	 */
	@RequestMapping(value="/signIn",method=RequestMethod.POST)
	public String signIn(HttpServletRequest req,
			@RequestParam("Username")String username,
			@RequestParam("Password") String pwd) {
		
		int errorCode;
		
		if( UserManageUtil.isSignIn(req) == 0 ) { //用户未登录
			errorCode = -1;
		}
		else {
			//验证用户身份
			if( UserManageUtil.isUserPass(req,username, pwd) ) {
				errorCode=0;
			}
			else
				errorCode = -2;
		}
		if(errorCode==0) {
			return "safeCloudSystem/index.jsp";
		}
		else {
			return "safeCloudSystem/login.jsp";
		}
		
	}	
	
	
	/*
	 *注销用户 
	 */
	@RequestMapping(value="/logout")
	public String logout(HttpServletRequest req) {
		if( UserManageUtil.isSignIn(req)>0) {  //若用户已登录
			HttpSession session = req.getSession();			
			session.invalidate();//注销用户			
		}		
		//String result="{\"error_code\":0}";
		return "safeCloudSystem/login.jsp";
	}
	
	
	/*
	 * 用户修改密码
	 */
	@RequestMapping(value="/updatePasswordByUser",method=RequestMethod.POST)
	@ResponseBody
	public String updatePasswordByUser(
			HttpServletRequest req,
			@RequestParam("username") String username, 
			@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword) {
		
		int error_code;
		if ( UserManageUtil.isSignIn(req) == 0 ) {  //用户未登录
			error_code = -1;			
		}else {
			if( UserManageUtil.isUserPass(req,username,oldPassword)) {  //账号密码验证通过
				String newPwd = FileEncryptUtil.getSHA256HashCode(newPassword.getBytes());
				User user = new User();
				user.setUser_name(username);
				user.setUser_pwd(newPwd);
				UserManageUtil.updateUserPwd(user);  //更新用户密码			
				error_code = 0;
			}
			else {  //用户账号密码错误
				error_code = -2;
			}			
		}		
		
		String res="\"error_code\":"+error_code+"";
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
		
		int errorCode ;
		int state = UserManageUtil.isSignIn(req);
		if( state ==0 ) { //用户未登录
			errorCode = -1;
		}
		else if(state ==1 ) { //当前用户为普通用户，不能修改管理员密码
			errorCode = -2;
		}
		else {
			if( ! UserManageUtil.isLogIn(username)) {//当前用户名不存在
				errorCode = -3;
			}
			else if ( pwd == null || pwd == "") {  //密码为空
				errorCode = -4;				
			}
			else {  //修改密码
				String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
				User user=new User();
				user.setUser_name(username);
				user.setUser_pwd(encryptPwd);
				UserManageUtil.updateUserPwd(user);  //更新用户密码
				errorCode = 0;
			}
		}
		
		String res="{\"error_code\":"+errorCode+"}";
		return res;		
	}
	
	
	/*
	 * 查看所有用户信息
	 */
	@RequestMapping(value="/getAllUser")
	@ResponseBody
	public String getAllUser(HttpServletRequest req) {
		
		int errorCode;
		String result="";
		int state = UserManageUtil.isSignIn(req);  //当前用户的状态
		if( state ==-1) {//用户未登录
			errorCode = -1;			
		}
		else if(state == 1 ){ //当前是普通用户,无权限查看
			errorCode = -2;			
		}
		else {
			errorCode = 0;
			//从数据库中获取所有用户信息
			try {			
				List<User> userList = UserManageUtil.getAllUserData();
				ObjectMapper mapper = new ObjectMapper();
				result = mapper.writeValueAsString(userList);  //将list转换为json数据格式
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
				
		String res="{\"error_code\":"+errorCode+"},\"userList\":"+result+"";
		return res;	
	}	
	
}
