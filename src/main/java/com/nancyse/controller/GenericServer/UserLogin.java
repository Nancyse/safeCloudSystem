package com.nancyse.controller.GenericServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value="/gs")
@Controller
public class UserLogin {
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public String login(HttpServletRequest request,HttpSession httpSession) {
		String username=request.getParameter("username");
		String pwd = request.getParameter("password");
		String shapwd = Util.getSHA256HashCode(pwd.getBytes());
		int result = getLoginMessage(username,shapwd,httpSession);
		if(result==-1) {
			request.setAttribute("username", "用户不存在");
			return "signIn";
		}else if( result==-2) {
			request.setAttribute("pwd", "密码错误");
			return "signIn";
		}
		return "redirect:/home.html";
	}
	
	public int getLoginMessage(String username, String pwd,HttpSession httpSession) {
		return -1;
	}

}
