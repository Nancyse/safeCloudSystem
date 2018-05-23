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
	 * ע���û�
	 * @param �û���
	 */
	@RequestMapping(value="/register",method=RequestMethod.POST)
	@ResponseBody
	public String register(HttpServletRequest req,
			@RequestParam("username") String username,
			@RequestParam("type") int type,
			@RequestParam("email") String email) throws Exception {
		
		int error_code;   
		int state = UserManageUtil.isSignIn(req); //�û���¼״̬
		if( state == -1) { //�û�δ��¼����ת��¼����	
			error_code = -1; 			
		}
		else if(state == 1) {//�û�����ͨ�û���û��Ȩ�޴����û�
			error_code = -2;
		}
		else {
			error_code = UserManageUtil.createNewUser(username, type, email);		
		}		
		String result="{\"error_code\":"+error_code+"}";
		
		return result;
	}
	
	/*
	 * �û���¼
	 */
	@RequestMapping(value="/signIn",method=RequestMethod.POST)
	public String signIn(HttpServletRequest req,
			@RequestParam("Username")String username,
			@RequestParam("Password") String pwd) {
		
		int errorCode;
		
		if( UserManageUtil.isSignIn(req) == 0 ) { //�û�δ��¼
			errorCode = -1;
		}
		else {
			//��֤�û����
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
	 *ע���û� 
	 */
	@RequestMapping(value="/logout")
	public String logout(HttpServletRequest req) {
		if( UserManageUtil.isSignIn(req)>0) {  //���û��ѵ�¼
			HttpSession session = req.getSession();			
			session.invalidate();//ע���û�			
		}		
		//String result="{\"error_code\":0}";
		return "safeCloudSystem/login.jsp";
	}
	
	
	/*
	 * �û��޸�����
	 */
	@RequestMapping(value="/updatePasswordByUser",method=RequestMethod.POST)
	@ResponseBody
	public String updatePasswordByUser(
			HttpServletRequest req,
			@RequestParam("username") String username, 
			@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword) {
		
		int error_code;
		if ( UserManageUtil.isSignIn(req) == 0 ) {  //�û�δ��¼
			error_code = -1;			
		}else {
			if( UserManageUtil.isUserPass(req,username,oldPassword)) {  //�˺�������֤ͨ��
				String newPwd = FileEncryptUtil.getSHA256HashCode(newPassword.getBytes());
				User user = new User();
				user.setUser_name(username);
				user.setUser_pwd(newPwd);
				UserManageUtil.updateUserPwd(user);  //�����û�����			
				error_code = 0;
			}
			else {  //�û��˺��������
				error_code = -2;
			}			
		}		
		
		String res="\"error_code\":"+error_code+"";
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
		
		int errorCode ;
		int state = UserManageUtil.isSignIn(req);
		if( state ==0 ) { //�û�δ��¼
			errorCode = -1;
		}
		else if(state ==1 ) { //��ǰ�û�Ϊ��ͨ�û��������޸Ĺ���Ա����
			errorCode = -2;
		}
		else {
			if( ! UserManageUtil.isLogIn(username)) {//��ǰ�û���������
				errorCode = -3;
			}
			else if ( pwd == null || pwd == "") {  //����Ϊ��
				errorCode = -4;				
			}
			else {  //�޸�����
				String encryptPwd = FileEncryptUtil.getSHA256HashCode(pwd.getBytes());
				User user=new User();
				user.setUser_name(username);
				user.setUser_pwd(encryptPwd);
				UserManageUtil.updateUserPwd(user);  //�����û�����
				errorCode = 0;
			}
		}
		
		String res="{\"error_code\":"+errorCode+"}";
		return res;		
	}
	
	
	/*
	 * �鿴�����û���Ϣ
	 */
	@RequestMapping(value="/getAllUser")
	@ResponseBody
	public String getAllUser(HttpServletRequest req) {
		
		int errorCode;
		String result="";
		int state = UserManageUtil.isSignIn(req);  //��ǰ�û���״̬
		if( state ==-1) {//�û�δ��¼
			errorCode = -1;			
		}
		else if(state == 1 ){ //��ǰ����ͨ�û�,��Ȩ�޲鿴
			errorCode = -2;			
		}
		else {
			errorCode = 0;
			//�����ݿ��л�ȡ�����û���Ϣ
			try {			
				List<User> userList = UserManageUtil.getAllUserData();
				ObjectMapper mapper = new ObjectMapper();
				result = mapper.writeValueAsString(userList);  //��listת��Ϊjson���ݸ�ʽ
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
				
		String res="{\"error_code\":"+errorCode+"},\"userList\":"+result+"";
		return res;	
	}	
	
}
