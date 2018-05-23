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
		System.out.print("��ʼɾ�������ļ�");
		int errorCode = 0;		
		if( UserManageUtil.isSignIn(req)<0) { //�û�δ��¼
			errorCode = -1;
			return "{\"error_code\":"+errorCode+"}";
		}
		
		HttpSession session = req.getSession();
		String username = (String)session.getAttribute("username");
		String userSpace = (String ) session.getAttribute("userSpace");
		int userType = (Integer) session.getAttribute("userType");
		if(userType==2 || username == uploader) {
			if( FileManageUtil.isFileExist(filename, filePath, uploader) ) { //����ļ�����
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
	
	
	//�ļ���Ϣ
	@RequestMapping(value="/filedetail")
	public ModelAndView getFiledetail(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView();
		
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
			fileList = FileManageUtil.getAllFiles();
		}
		else { //��ǰ�û�Ϊ��ͨ�û�
			fileList = FileManageUtil.getUserAllFiles(uploader);
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
		mav.setViewName(viewName);
		return mav;
	}
	
	//�����ļ�
	@RequestMapping(value="/searchFile",method=RequestMethod.POST)
	public ModelAndView searchFile(HttpServletRequest req,
			@RequestParam(value="minNum",required=false) int minNum,
			@RequestParam(value="maxNum",required=false) int maxNum,
			@RequestParam(value="dirKeyword",required=false) String dirKeyword,
			@RequestParam(value="fileType",required=false) String fileType,
			@RequestParam(value="keywords",required=false) String keyword) {
		
		ModelAndView mav = new ModelAndView();
		
		String viewName = "safeCloudSystem/filedetail.jsp";
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
	
	//�������
	@RequestMapping(value="/sys-filemanage")
	public String sysFileManage(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼				
			return "safeCloudSystem/login.jsp";
		}	
		return "safeCloudSystem/sys-filemanage.jsp";
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
