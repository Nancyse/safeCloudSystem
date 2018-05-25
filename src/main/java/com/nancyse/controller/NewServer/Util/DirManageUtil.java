package com.nancyse.controller.NewServer.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.User;
import com.nancyse.controller.NewServer.Const.PageData;


public class DirManageUtil {
	
	
	//获取所有用户文件路径信息
	
	public static ModelAndView getAllDirs(	HttpServletRequest req,String page) {
		
		ModelAndView mav = new ModelAndView();
		
		int startRow=0, pageSize=PageData.PAGESIZE ;
		long pageTimes=1;
		startRow = (Integer.parseInt(page)-1)*pageSize;	
		
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录	
			mav.setViewName("safeCloudSystem/login.jsp");
			return mav;
		}	
		
		HttpSession session = req.getSession();
		int userType = (Integer)session.getAttribute("userType");
		String username = (String) session.getAttribute("username");//当前登陆用户
		
		//获取文件信息
		List<Map> list = new ArrayList<Map>();
		List<DefaultFile> fileList=null;
		List<User> userList = null;
		if(userType == 2) { //当前用户为管理员
			//获取所有文件信息
			List<DefaultFile> fl = FileManageUtil.getAllFiles();
			pageTimes = fl.size()/pageSize+1;
			fileList = FileManageUtil.getAllFilesByPage(startRow, pageSize);
			for(DefaultFile f: fileList) {
				User user = UserManageUtil.getOneUser(f.getFile_uploader());
				Map<String,Object> model = new HashMap<String,Object>();
				model.put("user_name",user.getUser_name() );
				if(user.getUser_type()==1)
					model.put("user_type", "用户");
				else
					model.put("user_type", "管理员");				
				model.put("file_dir",f.getFile_dir());				
				list.add(model);			
			}	
			String viewName="safeCloudSystem/sys-dirsmanage.jsp";  //真实用
			//String viewName="safeCloudSystem/testForEach.jsp";//测试用
			String modelName = "dirsList";	
			mav.addObject(modelName, list);
			mav.addObject("currentPage", Integer.parseInt(page));		
			mav.addObject("pageTimes", pageTimes);
			mav.setViewName(viewName);
		}else {
			String viewName = "safeCloudSystem/error.jsp";
			mav.setViewName(viewName);
		}
		
		return mav;
		
	}
	

}
