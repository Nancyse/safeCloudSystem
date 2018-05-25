package com.nancyse.controller.NewServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.aliyun.oss.HttpMethod;
import com.nancyse.controller.GenericServer.DataModel.DefaultFile;
import com.nancyse.controller.GenericServer.DataModel.User;
import com.nancyse.controller.NewServer.Const.FilePath;
import com.nancyse.controller.NewServer.Const.OSSConfig;
import com.nancyse.controller.NewServer.Const.PageData;
import com.nancyse.controller.NewServer.Util.DirManageUtil;
import com.nancyse.controller.NewServer.Util.FileBufferManageUtil;
import com.nancyse.controller.NewServer.Util.FileManageUtil;
import com.nancyse.controller.NewServer.Util.OSSManageUtil;
import com.nancyse.controller.NewServer.Util.UserManageUtil;

@CrossOrigin(origins="*")
@Controller
@RequestMapping(value="/server")
public class UploadController {
	
	//首页
	@RequestMapping(value="/index")
	public String index(HttpServletRequest req) {
		HttpSession session = req.getSession();
		System.out.println("username:"+session.getAttribute("username"));
		System.out.println("userType:"+session.getAttribute("userType"));
		System.out.println("userSpace:"+session.getAttribute("userSpace"));
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录
			return "safeCloudSystem/login.jsp";
		}
		return "safeCloudSystem/index.jsp";
	}
	
	//文件上传入口
	@RequestMapping(value="/uploadfile")
	public String uploadfile(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录
			return "safeCloudSystem/login.jsp";
		}
		return "safeCloudSystem/uploadfile.jsp";
	}
	
	//上传文件密文
	@RequestMapping(value="/uploadStr",method=RequestMethod.POST)
	@ResponseBody
	public String uploadEncryptStr(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,
			@RequestParam("fileData") String fileData,
			@RequestParam("length") long length ) throws IOException{
		
		int errorCode;
		String  result = "";
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录
			errorCode = -1;
			result="{\"error_code\":"+errorCode+"}";
			return result;
		}
		HttpSession session = req.getSession();
		String uploader = (String)session.getAttribute("username");
		String userSpace = (String)session.getAttribute("userSpace");		
		String key = userSpace+filePath+filename;
		int userType = (Integer) session.getAttribute("userType");
		String bucketName = "lps-test";		
		
		//文件是否存在
		if( FileManageUtil.isFileExist(filename, filePath, uploader)) { //文件已存在
			errorCode = -2;
			result="{\"error_code\":"+errorCode+"}";
			return result;
		}
		
		//将加密字符串保存到本地
		FileManageUtil.saveStr2Local(filename, filePath, fileData);
		//新增缓存记录
		FileBufferManageUtil.saveBuffer2Database(filename, filePath, length, uploader);
		//将文件传送至OSS
		OSSConfig ossConfig;		
		errorCode = -3;
		try {
			ossConfig = new OSSConfig(FilePath.CONFIGFILE);
			OSSManageUtil.uploadString(ossConfig, bucketName, key, fileData); //上传密文到OSS	
			errorCode = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		result="{\"error_code\":"+errorCode+"}";
		return result;
	}
	
	//上传文件信息
	@CrossOrigin(origins="http://localhost")
	@RequestMapping(value="/uploadFileData",method=RequestMethod.POST)	
	@ResponseBody
	public String uploadFileData(HttpServletRequest req,
			@RequestParam(value="description",required=false) String desc,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,
			@RequestParam("fileKey") String fileKey,
			@RequestParam("fileHash") String fileHash,
			@RequestParam("length") long length) throws IOException{		
		
		int errorCode;
		String  result = "";
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录
			errorCode = -1;
			result="{\"error_code\":"+errorCode+"}";
			return result;
		}	
		HttpSession session = req.getSession();		
		String uploader = (String)session.getAttribute("username");
		
		//判断文件是否存在
		if( FileManageUtil.isFileExist(filename, filePath, uploader)) { //文件已存在
			errorCode = 1;
			result="{\"error_code\":"+errorCode+"}";
			return result;
		}
				
		//获取文件类型		
		String fileType = FileManageUtil.getFileType(filename);
		//将文件信息保存至文件表中
		FileManageUtil.saveFileData(fileHash, fileKey, filePath, filename, fileType, length, uploader, desc);
		errorCode = 0;
		result="{\"error_code\":"+errorCode+"}";
		return result;
	}
	
	
	//更新文件密文	
	@RequestMapping(value="/updateStr",method=RequestMethod.POST)
	@ResponseBody
	public String updateEncryptStr(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,
			@RequestParam("fileData") String fileData,
			@RequestParam("length") long length,
			@RequestParam(value="uploader") String uploader) throws IOException{
		
		System.out.println("更新文件密文");
		int errorCode=0;
		
		if( UserManageUtil.isSignIn(req)<0) { //用户未登录
			errorCode = -1;
		}
		
		HttpSession session = req.getSession();
		String username = (String)session.getAttribute("username");  //当前登录的用户
		String userSpace = (String) session.getAttribute("userSpace");
		String key = userSpace+filePath+filename;
		int userType = (Integer) session.getAttribute("userType"); //当前用户类型
		String bucketName = "lps-test";		
		 
		if( userType == 2 || uploader == username) {
		//if (true) {
			//文件密文保存到本地文件
			FileManageUtil.saveStr2Local(filename, filePath, fileData);			
			//更新缓存数据库
			FileBufferManageUtil.updateFileData(filePath, filename, uploader, length);
			//更新OSS上的文件
			OSSConfig ossConfig;		
			errorCode = -3;
			try {
				ossConfig = new OSSConfig(FilePath.CONFIGFILE);
				OSSManageUtil.uploadString(ossConfig, bucketName, key, fileData); //上传密文到OSS	
				errorCode = 0;
				System.out.println("上传成功");
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		else {
			errorCode = -2;
		}		
		
		
		String result="{\"error_code\":"+errorCode+"}";
		return result;
	}
	
	
	//更新文件元数据
	@RequestMapping(value="/updateFileData",method=RequestMethod.POST)
	@ResponseBody
	public String updateFile(HttpServletRequest req,
			@RequestParam(value="description",required=false) String desc,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,
			@RequestParam("fileKey") String fileKey,
			@RequestParam("fileHash") String fileHash,
			@RequestParam("length") long length,
			@RequestParam("uploader") String uploader) throws IOException{
		
		System.out.println("更新元数据");
		int errorCode=0;
		if( UserManageUtil.isSignIn(req)<0) { //用户未登录
			errorCode = -1;
		}
		HttpSession session = req.getSession();
		String username = (String)session.getAttribute("username");  //当前登录的用户
		int type = (Integer) session.getAttribute("userType");  //当前用户类型
		if( type == 2 || uploader == username) { //当前用户是管理员  或者 当前用户是原文件的上传者
				
			String fileType = FileManageUtil.getFileType(filename);  //获取文件类型	
			//更新文件信息
			FileManageUtil.updateFileData(filePath, fileHash, fileKey, filename, length, fileType, uploader, desc);
			System.out.println("更新元数据成功");
		}
		else {
			errorCode = -2;
		}
		String res="{\"error_code\":"+errorCode+"}";
		return res;
	}

	
	//删除文件密文
	@RequestMapping(value="/deleteFile",method=RequestMethod.POST)
	@ResponseBody
	public String deleteFile(HttpServletRequest req,			
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,			
			@RequestParam("uploader") String uploader) {
		
		System.out.println("开始删除密文文件");
		int errorCode = 0;		
		if( UserManageUtil.isSignIn(req)<0) { //用户未登录
			errorCode = -1;
			return "{\"error_code\":"+errorCode+"}";
		}
		
		HttpSession session = req.getSession();
		String username = (String)session.getAttribute("username");
		String userSpace = (String ) session.getAttribute("userSpace");
		int userType = (Integer) session.getAttribute("userType");
		System.out.println("username:"+username);
		System.out.println("uploader:"+uploader);
		
		if(userType==2 || username.equals(uploader) ) {
			System.out.println("用户开始删除密文文件");
			if( FileManageUtil.isFileExist(filename, filePath, uploader) ) { //如果文件存在
				System.out.print("文件存在");
				//删除本地文件
				File file = new File(FilePath.LOCALDIR+userSpace+filePath+filename);				
				file.delete();
				//删除缓存记录
				FileBufferManageUtil.deleteFileData(filename, filePath, uploader);
				//删除文件记录
				FileManageUtil.deleteFileData(filename, filePath, uploader);
				//删除OSS上的文件
				String bucketName="lps-test";
				String key = userSpace+filePath+filename;
				OSSConfig ossConfig;
				try {
					ossConfig = new OSSConfig(FilePath.CONFIGFILE);					
					OSSManageUtil.deleteFile(ossConfig, bucketName, key);
				} catch (IOException e) {
					e.printStackTrace();
					
				}
				errorCode=0;
				System.out.print("删除成功");
				
			}
			else {
				errorCode = -2;
				System.out.print("文件不存在");
			}
		}
				
		return "{\"error_code\":"+errorCode+"}";
		
	}
	
	
	//个人信息
	@RequestMapping(value="/persondetail")
	public String persondetail(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录			
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/persondetail.jsp";
	}
	
	
	//所有文件信息
	@RequestMapping(value="/filedetail")
	public ModelAndView getFiledetail(HttpServletRequest req,
			@RequestParam(value="page",defaultValue="1")String page) {
		ModelAndView mav = new ModelAndView();
		
		int startRow=0,pageSize=10, pageTimes=1;
		startRow = (Integer.parseInt(page)-1)*pageSize;	
		
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录	
			mav.setViewName("safeCloudSystem/login.jsp");
			return mav;
		}	
		
		HttpSession session = req.getSession();
		int userType = (Integer)session.getAttribute("userType");
		String uploader = (String) session.getAttribute("username");
		
		//获取文件信息
		List<Map> list = new ArrayList<Map>();
		List<DefaultFile> fileList=null;
		
		if(userType == 2) { //当前用户为管理员
			List<DefaultFile> fl = FileManageUtil.getAllFiles();
			pageTimes = fl.size()/pageSize+1;
			fileList = FileManageUtil.getAllFilesByPage(startRow, pageSize);
		}
		else { //当前用户为普通用户
			List<DefaultFile> fl = FileManageUtil.getUserAllFiles(uploader);
			pageTimes = fl.size()/pageSize+1;
			fileList = FileManageUtil.getUserFilesByPage(uploader, startRow, pageSize);
		}
			
		for( DefaultFile f : fileList) {			
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("file_name",f.getFile_name() );			
			model.put("file_type", f.getFile_type());
			model.put("file_size", f.getFile_size());
			model.put("file_dir",f.getFile_dir());
			model.put("file_hash", f.getFile_hash());
			model.put("file_uploader", f.getFile_uploader());
			model.put("file_desc", f.getFile_desc());
			model.put("upload_time", f.getUpload_time());
			list.add(model);			
		}		
		String viewName="safeCloudSystem/filedetail.jsp";  //真实用
		//String viewName="safeCloudSystem/testForEach.jsp";//测试用
		String modelName = "fileList";	
		mav.addObject(modelName, list);
		mav.addObject("currentPage", Integer.parseInt(page));		
		mav.addObject("pageTimes", pageTimes);
		mav.setViewName(viewName);
		
		return mav;
	}
	
	//查找文件
	@RequestMapping(value="/searchFile",method=RequestMethod.POST)
	public ModelAndView searchFile(HttpServletRequest req,
			@RequestParam(value="minNum",defaultValue="0") int minNum,
			@RequestParam(value="maxNum",defaultValue="0") int maxNum,
			@RequestParam(value="dirKeyword",defaultValue="#") String dirKeyword,
			@RequestParam(value="fileType",defaultValue="#") int fileTypeIndex,
			@RequestParam(value="keywords",defaultValue="#") String keyword,
			@RequestParam(value="page",defaultValue="1")String page) {
		
		
		ModelAndView mav = new ModelAndView();		
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录	
			mav.setViewName("safeCloudSystem/login.jsp");
			return mav;
		}			
		
		HttpSession session = req.getSession();
		int userType = (Integer)session.getAttribute("userType");
		String uploader = (String) session.getAttribute("username");
		
		int startRow=0,pageSize=10, pageTimes=1;
		startRow = (Integer.parseInt(page)-1)*pageSize;	
		String fileType="txt";
		//获取文件信息
		List<Map> list = new ArrayList<Map>();
		List<DefaultFile> fileList=new ArrayList<DefaultFile>();
		
		if(userType == 2) { //当前用户为管理员
			List<DefaultFile> fl = FileManageUtil.getAllFiles();
			
			pageTimes = fl.size()/pageSize+1;
			fileList = FileManageUtil.getAllFilesByPage(startRow, pageSize);
		}
		else { //当前用户为普通用户
			if(fileTypeIndex==1) {
				fileType="txt";
			}
			else if(fileTypeIndex==2){
				fileType="pdf";				
			}
			List<DefaultFile> fl = FileManageUtil.findFileWithWord(minNum, maxNum, fileType, dirKeyword, uploader, keyword);
					
			pageTimes = fl.size()/pageSize+1;
			for( int i=startRow; i<fl.size() && i< startRow+pageSize;i++) {
				fileList.add(fl.get(i));
			}			
		}
			
		for( DefaultFile f : fileList) {			
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("file_name",f.getFile_name() );			
			model.put("file_type", f.getFile_type());
			model.put("file_size", f.getFile_size());
			model.put("file_dir",f.getFile_dir());
			model.put("file_hash", f.getFile_hash());
			model.put("file_uploader", f.getFile_uploader());
			model.put("file_desc", f.getFile_desc());
			model.put("upload_time", f.getUpload_time());
			System.out.println("file_size:"+f.getFile_size());
			list.add(model);			
		}		
		String viewName="safeCloudSystem/filedetail.jsp";  //真实用
		//String viewName="safeCloudSystem/testForEach.jsp";//测试用
		String modelName = "fileList";	
		mav.addObject(modelName, list);
		mav.addObject("currentPage", Integer.parseInt(page));		
		mav.addObject("pageTimes", pageTimes);
		mav.setViewName(viewName);
		
		int currentPage = Integer.parseInt(page);
		
		mav.setViewName(viewName);
		System.out.println("查找成功");
		return mav;
	}
	
	//下载文件
	@RequestMapping(value="/downloadfile",method=RequestMethod.POST)
	@ResponseBody
	public String downloadFile(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename) throws IOException {

		int errorCode;
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录	
			errorCode = -1;
			return "{\"error_code\":"+errorCode+"}";
		}	
		
		HttpSession session = req.getSession();
		String uploader=(String)session.getAttribute("username");
		String userSpace = (String) session.getAttribute("userSpace");
		String fileData="";
		
		//判断文件是否在本地
		if( !FileBufferManageUtil.hasFile(filePath, filename, uploader) ) {
			String bucketName = "lps-test";
			String key = userSpace+filePath+filename;
			OSSConfig ossConfig;
			try {
				ossConfig = new OSSConfig(FilePath.CONFIGFILE);
				fileData = OSSManageUtil.downloadStr(ossConfig, bucketName, key);
				//增加缓存记录
				long fileLength=fileData.length();
				FileBufferManageUtil.saveBuffer2Database(filename,filePath,fileLength,uploader);				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		else {
			String path = FilePath.LOCALDIR+filePath+filename;
			File file = new File(path);
			if( file.isFile() && file.exists()) {
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file));;
				BufferedReader bufferedReader = new BufferedReader(reader);
				String lineText = null;
				while ( (lineText = bufferedReader.readLine())!=null) {
					fileData += lineText;
				}
				reader.close();
			}
			else {
				return "系统找不到文件";
			}
		}		
		return fileData;
	}
	
	
	/*
	 * 获取文件的哈希值和密钥
	 */
	@CrossOrigin(origins="http://localhost")
	@RequestMapping(value="/getFilehashAndKey",method=RequestMethod.POST)
	@ResponseBody
	public String getFilehashAndKey(HttpServletRequest req,
			@RequestParam("filename") String filename,
			@RequestParam("filePath") String filePath){
		
		System.out.println("获取文件摘要和密钥");
		int errorCode;
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录	
			errorCode = -1;
			return "{\"error_code\":"+errorCode+"}";
		}	
		
		HttpSession session = req.getSession();		
		String uploader=(String)session.getAttribute("username");

		Map map = FileManageUtil.getFileHashAndKey(filename, filePath, uploader); //获取文件哈希值和密钥
		
		return "{\"fileHash\":\""+map.get("fileHash")+"\",\"fileKey\":\""+map.get("fileKey")+"\"}";		
	}
	
	
	//常见问题
	@RequestMapping(value="/commonfaq")
	public String commonfaq(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录				
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/commonfaq.jsp";
	}
	
	//需改密码
	@RequestMapping(value="/changePassword")
	public String changePassword(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录				
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/changePassword.jsp";
	}
	
	//后台管理系统-文件管理
	@RequestMapping(value="/sys-filemanage")
	public String sysFileManage(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录				
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/sys-filemanage.jsp";
	}
	
	//后台管理系统-目录管理
	@RequestMapping(value="/sys-dirsmanage")
	public ModelAndView sysDirManage(HttpServletRequest req,
			@RequestParam(value="page",defaultValue="1")String page) {
		ModelAndView mav = new ModelAndView();
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录
			String viewName="safeCloudSystem/login.jsp";
			mav.setViewName(viewName);
			return mav;
		}	
		return DirManageUtil.getAllDirs(req, page);				
		
	}
	
	//后台管理系统-用户管理
	@RequestMapping(value="/sys-usermanage")
	public ModelAndView sysUserManage(HttpServletRequest req,
			@RequestParam(value="page",defaultValue="1")String page ){
		
		ModelAndView mav = new ModelAndView();
		int startRow=0, pageSize=PageData.PAGESIZE ;
		long pageTimes=1;
		startRow = (Integer.parseInt(page)-1)*pageSize;
		
		if(UserManageUtil.isSignIn(req) == -1) {  //用户未登录				
			String viewName="safeCloudSystem/login.jsp";
			mav.setViewName(viewName);
			return mav;
		}	
		
		HttpSession session = req.getSession();
		int userType = (Integer)session.getAttribute("userType");
		String username = (String) session.getAttribute("username");//当前登陆用户
		
		//获取文件信息
		List<Map> list = new ArrayList<Map>();
		List<User> userList = null;
		if(userType == 2) { //当前用户为管理员
			List<User> ul = UserManageUtil.getAllUserData();
			pageTimes = ul.size()/pageSize+1;
			
			userList = UserManageUtil.getAllUsersByPage(startRow, pageSize);
			for(User user: userList) {				
				Map<String,Object> model = new HashMap<String,Object>();
				model.put("user_name",user.getUser_name() );
				if(user.getUser_type()==1)
					model.put("user_type", "用户");
				else
					model.put("user_type", "管理员");		
				model.put("user_email",user.getUser_email() );
				model.put("user_space",user.getUser_space());								
				list.add(model);			
			}	
			String viewName="safeCloudSystem/sys-usermanage.jsp";  //真实用
			String modelName = "userList";	
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
	
	//测试
	@RequestMapping(value="/createNewUser")
	public String createNewUser() {
		return "safeCloudSystem/createNewUser.jsp";
	}
	
	//添加用户
	@RequestMapping(value="/NewUser",method=RequestMethod.POST)
	public String NewUser(HttpServletRequest req,
			@RequestParam("username") String username,
			@RequestParam("type") int type,
			@RequestParam("email")String email) {
		
		UserManageUtil.createNewUser(username, type, email);
		
		return "safeCloudSystem/sys-usermanage.jsp";
	}
	
	//测试
	@RequestMapping(value="/testJS3")
	public String testJS3() {
		return "safeCloudSystem/testJS3.jsp";
	}
		
	
	@RequestMapping(value="/testFileSaver")
	public String downloadFileIndex(HttpServletRequest req){		
		String decryptData="";		
		return "safeCloudSystem/testFileSaver.jsp";		
	}
	
	@RequestMapping(value="/downloadfile2",method=RequestMethod.POST)
	@ResponseBody
	public String downloadFile2(HttpServletRequest req,
			@RequestParam("filename") String desc,
			@RequestParam("filePath") String filePath){		
		String decryptData="BaDC0p+3yaQrkHEiK4cQ+g==";		
		return decryptData;		
	}	
	
	
}
