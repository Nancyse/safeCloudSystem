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
	
	//�ļ��ϴ�
	@RequestMapping(value="/uploadfile")
	public String uploadfile(HttpServletRequest req) {
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼
			return "safeCloudSystem/login.jsp";
		}
		return "safeCloudSystem/uploadfile.jsp";
	}
	
	//�ϴ��ļ��ַ���
	@RequestMapping(value="/uploadStr",method=RequestMethod.POST)
	@ResponseBody
	public String uploadEncryptStr(HttpServletRequest req,
			@RequestParam("filePath") String filePath,
			@RequestParam("filename") String filename,
			@RequestParam("fileData") String fileData,
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
		String userSpace = (String)session.getAttribute("userSpace");		
		String key = userSpace+filePath+filename;
		String bucketName = "lps-test";
		
		//�������ַ������浽����
		FileManageUtil.saveStr2Local(filename, filePath, fileData);
		//���������¼
		FileBufferManageUtil.saveBuffer2Database(filename, filePath, length, uploader);
		//���ļ�������OSS
		OSSConfig ossConfig;		
		errorCode = -2;
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
		//��ȡ�ļ�����
		int index = filename.indexOf('.');
		String fileType="null";
		if(index>=0)
			fileType=filename.substring(filename.indexOf('.')+1);
		//���ļ���Ϣ�������ļ�����
		FileManageUtil.saveFileData(fileHash, fileKey, filePath, filename, fileType, length, uploader, desc);
		errorCode = 0;
		result="{\"error_code\":"+errorCode+"}";
		return result;
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
	public ModelAndView filedetail(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView();
		
		if(UserManageUtil.isSignIn(req) == -1) {  //�û�δ��¼	
			mav.setViewName("safeCloudSystem/login.jsp");
			return mav;
		}	
		
		//��ȡ�ļ���Ϣ
		List<Map> list = new ArrayList<Map>();
		List<DefaultFile> fileList = FileManageUtil.getAllFiles();
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
		String viewName="safeCloudSystem/filedetail.jsp";
		String modelName = "fileList";	
		mav.addObject(modelName, list);
		mav.setViewName(viewName);
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
