package com.nancyse.controller.GenericServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping(value="/gs")
@Controller
public class FileUpload {
	
	@RequestMapping(value="/fileupload.index",method=RequestMethod.GET)
	public String index() {
		return "fileUploadTest/uploadForm";
	}
	
	
	@RequestMapping(value="/upload")
	@ResponseBody
	public String uploadFile(
			HttpServletRequest req,
			@RequestParam("description") String desc,
			@RequestParam("file") MultipartFile file) throws IOException {
		
		if(file.isEmpty()) {
			return "文件为空！";
		}
		byte[] rawfileData = file.getBytes();
		
		//计算文件的hash值
		String fileHash=FileEncryptUtil.getSHA256HashCode(rawfileData);  
		//System.out.println("fileHash:"+fileHash);
		
		//配置请求参数
		Map<String,String> params=new HashMap<String,String>();
		params.put("filename", file.getOriginalFilename());
		params.put("creator", "nancyse");
		params.put("fileHashCode", fileHash);
		
		//发送请求获得加密密钥和文件控制块
		String scretFileData = this.getFileKeyAndFileBlock(params);
		ObjectMapper objMapper = new ObjectMapper();
		
		
		FileData fd = objMapper.readValue(scretFileData, FileData.class);
		String fileKey = fd.getEncryptKey();
		String fileblock = fd.getFileblock();
		
		//System.out.println("EncryptKey: "+fileKey);
		//System.out.println("Fileblock: "+ fileblock);
		FileEncryptUtil.logger.info("EncryptKey: "+fileKey);
		FileEncryptUtil.logger.info("Fileblock: "+ fileblock);
		
		//加密文件		
		//String path = req.getServletContext().getRealPath("/encryptFiles/");
		String path=FilePath.UPLOADFILEPATH;
		//String path = "E:/files/";
		//System.out.println("encryptfilepath: "+path);
		FileEncryptUtil.logger.info("encryptfilepath: "+path);
		
		String filename = file.getOriginalFilename();
		File filepath = new File(path,filename);
		if( !filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		
		FileEncryptUtil.encryptFile(rawfileData, encryptfile, fileKey);
		
		//保存文件控制块
		saveBlock(req,fileblock,filename);
				
		return "文件上传成功！";
	}
	
	
	//保存文件控制块
	private void saveBlock(HttpServletRequest request,String block,String filename) {
		//String path=request.getServletContext().getRealPath("/fileblocks/");
		String path=FilePath.FILEBLOCKPATH;
		//System.out.println("fileblock path:"+path);
		FileEncryptUtil.logger.info("fileblock path:"+path);
		
		//上传文件
		File filepath=new File(path,filename);
		//判断路径是存在，如果不存在就创建一个
		if(!filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		StringReader sr = new StringReader(block);
		try {
			FileWriter fw = new FileWriter(filepath);
			fw.write(block);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			
		}
		
	}
	
	
	//获取文件密钥和文件控制块
	private String getFileKeyAndFileBlock(Map<String,String> map) {
		String result="";
		String urltemp = FileEncryptUtil.hostName+"/safeCloudSystem/AS2/createFileKeyAndFileBlock";
		String url=buildUrl(urltemp,map);  //组合url
		try {
			URLConnection conn = new URL(url).openConnection();
			//设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//建立实际连接
			conn.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			String strRead="";
			while( (strRead= in.readLine())!=null) {
				result+=strRead;
			}
			return result;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	//组合url地址
	private String buildUrl(String url, Map<String,String> params) {
		String finalUrl=url+"?";
		int index=1;
		for(String key: params.keySet()) {
			finalUrl+=key+"="+params.get(key);
			if( index < params.size()) {
				finalUrl+="&";
				index+=1;
			}
		}
		return finalUrl;
	}
	
	

}
