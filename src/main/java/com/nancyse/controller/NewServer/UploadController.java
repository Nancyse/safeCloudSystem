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
	
	//��ҳ
	@RequestMapping(value="/index")
	public String index(HttpServletRequest req) {
		HttpSession session = req.getSession();
		System.out.println("username:"+session.getAttribute("username"));
		System.out.println("userType:"+session.getAttribute("userType"));
		System.out.println("userSpace:"+session.getAttribute("userSpace"));
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼
			return "safeCloudSystem/login.jsp";
		}
		return "safeCloudSystem/index.jsp";
	}
	
	//�ļ��ϴ����
	@RequestMapping(value="/uploadfile")
	public String uploadfile(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼
			return "safeCloudSystem/login.jsp";
		}
		return "safeCloudSystem/uploadfile.jsp";
	}
	
	//�ϴ��ļ�����
	@RequestMapping(value="/uploadStr",method=RequestMethod.POST)
	@ResponseBody
	public String uploadEncryptStr(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,
			@RequestParam("fileData") String fileData,
			@RequestParam("length") long length ) throws IOException{
		
		int errorCode;
		String  result = "";
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼
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
		
		//�ļ��Ƿ����
		if( FileManageUtil.isFileExist(filename, filePath, uploader)) { //�ļ��Ѵ���
			errorCode = -2;
			result="{\"error_code\":"+errorCode+"}";
			return result;
		}
		
		//�������ַ������浽����
		FileManageUtil.saveStr2Local(filename, filePath, fileData);
		//���������¼
		FileBufferManageUtil.saveBuffer2Database(filename, filePath, length, uploader);
		//���ļ�������OSS
		OSSConfig ossConfig;		
		errorCode = -3;
		try {
			ossConfig = new OSSConfig(FilePath.CONFIGFILE);
			OSSManageUtil.uploadString(ossConfig, bucketName, key, fileData); //�ϴ����ĵ�OSS	
			errorCode = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		result="{\"error_code\":"+errorCode+"}";
		return result;
	}
	
	//�ϴ��ļ���Ϣ
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
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼
			errorCode = -1;
			result="{\"error_code\":"+errorCode+"}";
			return result;
		}	
		HttpSession session = req.getSession();		
		String uploader = (String)session.getAttribute("username");
		
		//�ж��ļ��Ƿ����
		if( FileManageUtil.isFileExist(filename, filePath, uploader)) { //�ļ��Ѵ���
			errorCode = 1;
			result="{\"error_code\":"+errorCode+"}";
			return result;
		}
				
		//��ȡ�ļ�����		
		String fileType = FileManageUtil.getFileType(filename);
		//���ļ���Ϣ�������ļ�����
		FileManageUtil.saveFileData(fileHash, fileKey, filePath, filename, fileType, length, uploader, desc);
		errorCode = 0;
		result="{\"error_code\":"+errorCode+"}";
		return result;
	}
	
	
	//�����ļ�����	
	@RequestMapping(value="/updateStr",method=RequestMethod.POST)
	@ResponseBody
	public String updateEncryptStr(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,
			@RequestParam("fileData") String fileData,
			@RequestParam("length") long length,
			@RequestParam(value="uploader") String uploader) throws IOException{
		
		System.out.println("�����ļ�����");
		int errorCode=0;
		
		if( UserManageUtil.isSignIn(req)<0) { //�û�δ��¼
			errorCode = -1;
		}
		
		HttpSession session = req.getSession();
		String username = (String)session.getAttribute("username");  //��ǰ��¼���û�
		String userSpace = (String) session.getAttribute("userSpace");
		String key = userSpace+filePath+filename;
		int userType = (Integer) session.getAttribute("userType"); //��ǰ�û�����
		String bucketName = "lps-test";		
		 
		if( userType == 2 || uploader == username) {
		//if (true) {
			//�ļ����ı��浽�����ļ�
			FileManageUtil.saveStr2Local(filename, filePath, fileData);			
			//���»������ݿ�
			FileBufferManageUtil.updateFileData(filePath, filename, uploader, length);
			//����OSS�ϵ��ļ�
			OSSConfig ossConfig;		
			errorCode = -3;
			try {
				ossConfig = new OSSConfig(FilePath.CONFIGFILE);
				OSSManageUtil.uploadString(ossConfig, bucketName, key, fileData); //�ϴ����ĵ�OSS	
				errorCode = 0;
				System.out.println("�ϴ��ɹ�");
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
	
	
	//�����ļ�Ԫ����
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
		
		System.out.println("����Ԫ����");
		int errorCode=0;
		if( UserManageUtil.isSignIn(req)<0) { //�û�δ��¼
			errorCode = -1;
		}
		HttpSession session = req.getSession();
		String username = (String)session.getAttribute("username");  //��ǰ��¼���û�
		int type = (Integer) session.getAttribute("userType");  //��ǰ�û�����
		if( type == 2 || uploader == username) { //��ǰ�û��ǹ���Ա  ���� ��ǰ�û���ԭ�ļ����ϴ���
				
			String fileType = FileManageUtil.getFileType(filename);  //��ȡ�ļ�����	
			//�����ļ���Ϣ
			FileManageUtil.updateFileData(filePath, fileHash, fileKey, filename, length, fileType, uploader, desc);
			System.out.println("����Ԫ���ݳɹ�");
		}
		else {
			errorCode = -2;
		}
		String res="{\"error_code\":"+errorCode+"}";
		return res;
	}

	
	//ɾ���ļ�����
	@RequestMapping(value="/deleteFile",method=RequestMethod.POST)
	@ResponseBody
	public String deleteFile(HttpServletRequest req,			
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,			
			@RequestParam("uploader") String uploader) {
		
		System.out.println("��ʼɾ�������ļ�");
		int errorCode = 0;		
		if( UserManageUtil.isSignIn(req)<0) { //�û�δ��¼
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
			System.out.println("�û���ʼɾ�������ļ�");
			if( FileManageUtil.isFileExist(filename, filePath, uploader) ) { //����ļ�����
				System.out.print("�ļ�����");
				//ɾ�������ļ�
				File file = new File(FilePath.LOCALDIR+userSpace+filePath+filename);				
				file.delete();
				//ɾ�������¼
				FileBufferManageUtil.deleteFileData(filename, filePath, uploader);
				//ɾ���ļ���¼
				FileManageUtil.deleteFileData(filename, filePath, uploader);
				//ɾ��OSS�ϵ��ļ�
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
				System.out.print("ɾ���ɹ�");
				
			}
			else {
				errorCode = -2;
				System.out.print("�ļ�������");
			}
		}
				
		return "{\"error_code\":"+errorCode+"}";
		
	}
	
	
	//������Ϣ
	@RequestMapping(value="/persondetail")
	public String persondetail(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼			
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/persondetail.jsp";
	}
	
	
	//�����ļ���Ϣ
	@RequestMapping(value="/filedetail")
	public ModelAndView getFiledetail(HttpServletRequest req,
			@RequestParam(value="page",defaultValue="1")String page) {
		ModelAndView mav = new ModelAndView();
		
		int startRow=0,pageSize=10, pageTimes=1;
		startRow = (Integer.parseInt(page)-1)*pageSize;	
		
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼	
			mav.setViewName("safeCloudSystem/login.jsp");
			return mav;
		}	
		
		HttpSession session = req.getSession();
		int userType = (Integer)session.getAttribute("userType");
		String uploader = (String) session.getAttribute("username");
		
		//��ȡ�ļ���Ϣ
		List<Map> list = new ArrayList<Map>();
		List<DefaultFile> fileList=null;
		
		if(userType == 2) { //��ǰ�û�Ϊ����Ա
			List<DefaultFile> fl = FileManageUtil.getAllFiles();
			pageTimes = fl.size()/pageSize+1;
			fileList = FileManageUtil.getAllFilesByPage(startRow, pageSize);
		}
		else { //��ǰ�û�Ϊ��ͨ�û�
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
		String viewName="safeCloudSystem/filedetail.jsp";  //��ʵ��
		//String viewName="safeCloudSystem/testForEach.jsp";//������
		String modelName = "fileList";	
		mav.addObject(modelName, list);
		mav.addObject("currentPage", Integer.parseInt(page));		
		mav.addObject("pageTimes", pageTimes);
		mav.setViewName(viewName);
		
		return mav;
	}
	
	//�����ļ�
	@RequestMapping(value="/searchFile",method=RequestMethod.POST)
	public ModelAndView searchFile(HttpServletRequest req,
			@RequestParam(value="minNum",defaultValue="0") int minNum,
			@RequestParam(value="maxNum",defaultValue="0") int maxNum,
			@RequestParam(value="dirKeyword",defaultValue="#") String dirKeyword,
			@RequestParam(value="fileType",defaultValue="#") int fileTypeIndex,
			@RequestParam(value="keywords",defaultValue="#") String keyword,
			@RequestParam(value="page",defaultValue="1")String page) {
		
		
		ModelAndView mav = new ModelAndView();		
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼	
			mav.setViewName("safeCloudSystem/login.jsp");
			return mav;
		}			
		
		HttpSession session = req.getSession();
		int userType = (Integer)session.getAttribute("userType");
		String uploader = (String) session.getAttribute("username");
		
		int startRow=0,pageSize=10, pageTimes=1;
		startRow = (Integer.parseInt(page)-1)*pageSize;	
		String fileType="txt";
		//��ȡ�ļ���Ϣ
		List<Map> list = new ArrayList<Map>();
		List<DefaultFile> fileList=new ArrayList<DefaultFile>();
		
		if(userType == 2) { //��ǰ�û�Ϊ����Ա
			List<DefaultFile> fl = FileManageUtil.getAllFiles();
			
			pageTimes = fl.size()/pageSize+1;
			fileList = FileManageUtil.getAllFilesByPage(startRow, pageSize);
		}
		else { //��ǰ�û�Ϊ��ͨ�û�
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
		String viewName="safeCloudSystem/filedetail.jsp";  //��ʵ��
		//String viewName="safeCloudSystem/testForEach.jsp";//������
		String modelName = "fileList";	
		mav.addObject(modelName, list);
		mav.addObject("currentPage", Integer.parseInt(page));		
		mav.addObject("pageTimes", pageTimes);
		mav.setViewName(viewName);
		
		int currentPage = Integer.parseInt(page);
		
		mav.setViewName(viewName);
		System.out.println("���ҳɹ�");
		return mav;
	}
	
	//�����ļ�
	@RequestMapping(value="/downloadfile",method=RequestMethod.POST)
	@ResponseBody
	public String downloadFile(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename) throws IOException {

		int errorCode;
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼	
			errorCode = -1;
			return "{\"error_code\":"+errorCode+"}";
		}	
		
		HttpSession session = req.getSession();
		String uploader=(String)session.getAttribute("username");
		String userSpace = (String) session.getAttribute("userSpace");
		String fileData="";
		
		//�ж��ļ��Ƿ��ڱ���
		if( !FileBufferManageUtil.hasFile(filePath, filename, uploader) ) {
			String bucketName = "lps-test";
			String key = userSpace+filePath+filename;
			OSSConfig ossConfig;
			try {
				ossConfig = new OSSConfig(FilePath.CONFIGFILE);
				fileData = OSSManageUtil.downloadStr(ossConfig, bucketName, key);
				//���ӻ����¼
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
				return "ϵͳ�Ҳ����ļ�";
			}
		}		
		return fileData;
	}
	
	
	/*
	 * ��ȡ�ļ��Ĺ�ϣֵ����Կ
	 */
	@CrossOrigin(origins="http://localhost")
	@RequestMapping(value="/getFilehashAndKey",method=RequestMethod.POST)
	@ResponseBody
	public String getFilehashAndKey(HttpServletRequest req,
			@RequestParam("filename") String filename,
			@RequestParam("filePath") String filePath){
		
		System.out.println("��ȡ�ļ�ժҪ����Կ");
		int errorCode;
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼	
			errorCode = -1;
			return "{\"error_code\":"+errorCode+"}";
		}	
		
		HttpSession session = req.getSession();		
		String uploader=(String)session.getAttribute("username");

		Map map = FileManageUtil.getFileHashAndKey(filename, filePath, uploader); //��ȡ�ļ���ϣֵ����Կ
		
		return "{\"fileHash\":\""+map.get("fileHash")+"\",\"fileKey\":\""+map.get("fileKey")+"\"}";		
	}
	
	
	//��������
	@RequestMapping(value="/commonfaq")
	public String commonfaq(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼				
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/commonfaq.jsp";
	}
	
	//�������
	@RequestMapping(value="/changePassword")
	public String changePassword(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼				
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/changePassword.jsp";
	}
	
	//��̨����ϵͳ-�ļ�����
	@RequestMapping(value="/sys-filemanage")
	public String sysFileManage(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼				
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/sys-filemanage.jsp";
	}
	
	//��̨����ϵͳ-Ŀ¼����
	@RequestMapping(value="/sys-dirsmanage")
	public ModelAndView sysDirManage(HttpServletRequest req,
			@RequestParam(value="page",defaultValue="1")String page) {
		ModelAndView mav = new ModelAndView();
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼
			String viewName="safeCloudSystem/login.jsp";
			mav.setViewName(viewName);
			return mav;
		}	
		return DirManageUtil.getAllDirs(req, page);				
		
	}
	
	//��̨����ϵͳ-�û�����
	@RequestMapping(value="/sys-usermanage")
	public ModelAndView sysUserManage(HttpServletRequest req,
			@RequestParam(value="page",defaultValue="1")String page ){
		
		ModelAndView mav = new ModelAndView();
		int startRow=0, pageSize=PageData.PAGESIZE ;
		long pageTimes=1;
		startRow = (Integer.parseInt(page)-1)*pageSize;
		
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼				
			String viewName="safeCloudSystem/login.jsp";
			mav.setViewName(viewName);
			return mav;
		}	
		
		HttpSession session = req.getSession();
		int userType = (Integer)session.getAttribute("userType");
		String username = (String) session.getAttribute("username");//��ǰ��½�û�
		
		//��ȡ�ļ���Ϣ
		List<Map> list = new ArrayList<Map>();
		List<User> userList = null;
		if(userType == 2) { //��ǰ�û�Ϊ����Ա
			List<User> ul = UserManageUtil.getAllUserData();
			pageTimes = ul.size()/pageSize+1;
			
			userList = UserManageUtil.getAllUsersByPage(startRow, pageSize);
			for(User user: userList) {				
				Map<String,Object> model = new HashMap<String,Object>();
				model.put("user_name",user.getUser_name() );
				if(user.getUser_type()==1)
					model.put("user_type", "�û�");
				else
					model.put("user_type", "����Ա");		
				model.put("user_email",user.getUser_email() );
				model.put("user_space",user.getUser_space());								
				list.add(model);			
			}	
			String viewName="safeCloudSystem/sys-usermanage.jsp";  //��ʵ��
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
	
	//����
	@RequestMapping(value="/createNewUser")
	public String createNewUser() {
		return "safeCloudSystem/createNewUser.jsp";
	}
	
	//����û�
	@RequestMapping(value="/NewUser",method=RequestMethod.POST)
	public String NewUser(HttpServletRequest req,
			@RequestParam("username") String username,
			@RequestParam("type") int type,
			@RequestParam("email")String email) {
		
		UserManageUtil.createNewUser(username, type, email);
		
		return "safeCloudSystem/sys-usermanage.jsp";
	}
	
	//����
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
