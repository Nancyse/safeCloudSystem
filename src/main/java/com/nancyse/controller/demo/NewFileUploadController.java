package com.nancyse.controller.demo;

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
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@RequestMapping(value="/new")
@Controller
public class NewFileUploadController {
	
	/*
	 * 文件上传的入口
	 */
	@RequestMapping(value="/file/uploadForm",method=RequestMethod.GET)
	public String index(HttpServletRequest request){
		System.out.println("you are successful send request");
		return "fileUploadTest/uploadForm";
	}
	
	/*
	 * 上传文件
	 */
	@RequestMapping(value="/file/upload",method=RequestMethod.POST)
	public String upload(HttpServletRequest request,
			@RequestParam("description") String description,
			@RequestParam("file") MultipartFile file) throws Exception{
		System.out.println(description);
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
			//String fileData = getFileData(fis);
			byte[] fileData = getFileDataAsBytes(fis);
			String hash=Util.getSHA256StrJava(fileData);
			System.out.println(hash);
			
			request.setAttribute("filename",filename);
			request.setAttribute("creator", "nancyse");
			request.setAttribute("fileHashCode", hash);
			String key="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
			//将上传文件保存到一个目标文件当中
			File encryptfile = new File(path+File.separator+filename);
			Util.encryptFile(fileData, encryptfile, key);
			return "forward:createFileBlock";
		}
		else 
			return "error";
	}

	/*
	 * 重定向到验证服务器
	 */
	@RequestMapping("file/createFileBlock")
	public void forward(HttpServletRequest request){
		System.out.println("文件上传成功！");
		String filename=(String) request.getAttribute("filename");
		String creator=(String)request.getAttribute("creator");
		String fileHashCode = (String)request.getAttribute("fileHashCode");
		//生成加密密钥
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		//生成文件控制块
		String key=fileHashCode+fileHashCode.substring(0,num);
		System.out.println(key);
		String fileBlock = "filename:"+filename
				+"creator:"+creator
				+"fileHashCode:"+fileHashCode
				+"key:"+key;
		//对文件控制块进行加密
		String path=request.getServletContext().getRealPath("/fileblocks/");
		File filepath=new File(path,filename);
		//判断路径是存在，如果不存在就创建一个
		if(!filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		File file = Util.encryptFile(fileBlock, encryptfile, key);
		//System.out.println(file.getAbsolutePath());
		
	}
	
	
	/*
	 * 文件下载
	 */
	@RequestMapping(value="/file/download")
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

	
	@RequestMapping(value="/testForward")
	public void testForward(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
		//req.getRequestDispatcher("index.jsp").forward(req, resp);
		System.out.println("testForward");
		resp.sendRedirect("https://www.baidu.com");
		System.out.println("重定向成功！");
		
	}
	
	
	/*
	 * 上传文件
	 */
	@RequestMapping(value="/file/uploadDemo",method=RequestMethod.POST)
	public void uploadDemo(HttpServletRequest request,
			@RequestParam("description") String description,
			@RequestParam("file") MultipartFile file) throws Exception{
		System.out.println(description);
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
			//String fileData = getFileData(fis);
			byte[] fileData = getFileDataAsBytes(fis);
			String hash=Util.getSHA256StrJava(fileData);
			System.out.println(hash);
			
			Map<String,String> params=new HashMap<String,String>();
			params.put("filename", filename);
			params.put("creator", "nancyse");
			params.put("fileHashCode", hash);
			getKeyByGet(params);
			System.out.println("发送请求成功");
			String key="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
			//将上传文件保存到一个目标文件当中
			File encryptfile = new File(path+File.separator+filename);
			Util.encryptFile(fileData, encryptfile, key);
			System.out.println(encryptfile.getAbsolutePath());
			
		}
		
	}
	public void getKeyByGet(Map<String,String> params) {
		try {
			System.out.println("进入getKeyByGet");
			String urlName = "http://localhost:8080/safeCloudSystem/new/file/getkey?";
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
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * 接受请求，生成文件控制块
	 */
	@RequestMapping(value="/file/getkey")
	public void getKey(HttpServletRequest request) {
		System.out.println("/file/getkey");
		//Map<String, String> params =request.getParameterMap();
		String filename =request.getParameter("filename");
		String creator=request.getParameter("creator");
		String fileHashCode = request.getParameter("fileHashCode");
		System.out.println("filename: "+filename);
		System.out.println("hashcode: "+fileHashCode);
		//生成加密密钥
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		//生成文件控制块
		String key=fileHashCode+fileHashCode.substring(0,num);
		System.out.println(key);
		String fileBlock = "filename:"+filename
				+"creator:"+creator
				+"fileHashCode:"+fileHashCode
				+"key:"+key;
		//对文件控制块进行加密
		String path=request.getServletContext().getRealPath("/fileblocks/");
		File filepath=new File(path,filename);
		//判断路径是存在，如果不存在就创建一个
		if(!filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		File file = Util.encryptFile(fileBlock, encryptfile, key);
		System.out.println(file.getAbsolutePath());
	}

}













