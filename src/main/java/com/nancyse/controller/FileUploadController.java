package com.nancyse.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nancyse.controller.demo.Util;

@RequestMapping(value="/file")
@Controller
public class FileUploadController {
	
	/*
	 * 文件上传的入口
	 */
	@RequestMapping(value="/uploadForm",method=RequestMethod.GET)
	public String index(HttpServletRequest request){
		System.out.println("you are successful send request");
		return "fileUploadTest/uploadForm";
	}
	
	/*
	 * 上传文件
	 */
	@RequestMapping(value="/upload",method=RequestMethod.POST)
	public void upload(HttpServletRequest request,
			@RequestParam("description") String description,
			@RequestParam("file") MultipartFile file) throws Exception{
		
		//如果上传的文件不为空
		if(!file.isEmpty()) {
			String path=request.getServletContext().getRealPath("/encryptedfiles/");
			//上传文件名
			String filename=file.getOriginalFilename();
			File filepath=new File(path,filename);
			//判断路径是存在，如果不存在就创建一个
			if(!filepath.getParentFile().exists()) {
				filepath.getParentFile().mkdirs();
			}
			File tmp = new File(path+File.separator+"tmp");
			file.transferTo(tmp);
			tmp.deleteOnExit();
			FileInputStream fis = new FileInputStream(tmp);
			//将文件的内容读取为二进制数据
			byte[] fileData = getFileDataAsBytes(fis);
			String hash=Util.getSHA256StrJava(fileData);
			System.out.println(hash);
			
			Map<String,String> params=new HashMap<String,String>();
			params.put("filename", filename);
			params.put("creator", "nancyse");
			params.put("fileHashCode", hash);
			
			//读取加密密钥和文件控制块
			String result = getEncryptKey(params);
			System.out.println("AS:"+result);
			ObjectMapper objMapper = new ObjectMapper();
			FileData fd = objMapper.readValue(result, FileData.class);
			String key = fd.getEncryptKey();
			String fileblock = fd.getFileblock();
			System.out.println("fileblock: "+fileblock);
			
			//String key="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
			//将上传文件保存到一个目标文件当中
			File encryptfile = new File(path+File.separator+filename);
			Util.encryptFile(fileData, encryptfile, key);
			System.out.println(encryptfile.getAbsolutePath());
			
			
		}
	}

	//将InputStream转换成byte[]
	public static byte[] getFileDataAsBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream sb = new ByteArrayOutputStream();
		//InputStreamReader isr = new InputStreamReader(in);
		int ch=0;
		while((ch=in.read())!=-1)
		{
			sb.write(ch);
		}
		return sb.toByteArray();
	}
	
	//获取加密密钥
	public String getEncryptKey(Map<String,String> params) {
		String result="";
		try {
			System.out.println("进入getKeyByGet");
			String urlName = "http://localhost:8080/safeCloudSystem/AS/getkey?";
			String parameters="";
			int index=1;
			for(String key: params.keySet()) {
				parameters+=key+"="+params.get(key);
				if( index < params.size()) {
					parameters+="&";
					index+=1;
				}
			}
			urlName +=parameters;
			System.out.println(urlName);
			URL url =new  URL(urlName);
			URLConnection conn = url.openConnection();
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
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
		
	}

	
	
	
	/*
	 * 文件下载
	 */
	@RequestMapping(value="/download")
	public ResponseEntity<byte[]> fileDownload(HttpServletRequest request, 
			@RequestParam("filename") String filename,
			Model model)throws Exception{
		
		//找到下载的文件
			String path=request.getServletContext().getRealPath("/encryptedfiles/");
			File rawfile=new File(path+File.separator+filename);
			//对文件进行解密
			File decrypfile = new File("decryfile.txt");  //创建一个临时文件,保存解码后的文件
			decrypfile.deleteOnExit();  //推出虚拟机的同时，删除文件
			String key="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
			File file=Util.decryptFile2(rawfile,decrypfile,key);		
			System.out.println(decrypfile.getAbsolutePath());

			HttpHeaders headers= new HttpHeaders();
			//下载显示的文件名，解决中文名称乱码问题
			String downloadFileName = new String(filename.getBytes("UTF-8"),"iso-8859-1");
			//通知浏览器以attachment(下载方式)打开图片
			headers.setContentDispositionFormData("attachment", downloadFileName);
			//application/octet-stream:二进制流数据（最常见的文件下载）
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			//201 HTTPStatus.Created
			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers,HttpStatus.CREATED);	
	}
	
	//将输入流转换程输出流
	public ByteArrayOutputStream parse(InputStream in) throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while( (ch=in.read())!=-1)  //每次只读一个字符
		{
			out.write(ch);
		}
		return out;
	}
	

	//将InputStream转换成String
	public static String getFileData(InputStream in) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		InputStreamReader isr = new InputStreamReader(in);
		int ch=0;
		while((ch=isr.read())!=-1)
		{
			sb.append(ch);
		}
		return sb.toString();
	}

	
	
	

}













