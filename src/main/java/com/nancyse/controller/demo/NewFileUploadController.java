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
	 * �ļ��ϴ������
	 */
	@RequestMapping(value="/file/uploadForm",method=RequestMethod.GET)
	public String index(HttpServletRequest request){
		System.out.println("you are successful send request");
		return "fileUploadTest/uploadForm";
	}
	
	/*
	 * �ϴ��ļ�
	 */
	@RequestMapping(value="/file/upload",method=RequestMethod.POST)
	public String upload(HttpServletRequest request,
			@RequestParam("description") String description,
			@RequestParam("file") MultipartFile file) throws Exception{
		System.out.println(description);
		//����ϴ����ļ���Ϊ��
		if(!file.isEmpty()) {
			String path=request.getServletContext().getRealPath("/encryptedfiles/");
			//�ϴ��ļ���
			String filename=file.getOriginalFilename();
			File filepath=new File(path,filename);
			//�ж�·���Ǵ��ڣ���������ھʹ���һ��
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
			//���ϴ��ļ����浽һ��Ŀ���ļ�����
			File encryptfile = new File(path+File.separator+filename);
			Util.encryptFile(fileData, encryptfile, key);
			return "forward:createFileBlock";
		}
		else 
			return "error";
	}

	/*
	 * �ض�����֤������
	 */
	@RequestMapping("file/createFileBlock")
	public void forward(HttpServletRequest request){
		System.out.println("�ļ��ϴ��ɹ���");
		String filename=(String) request.getAttribute("filename");
		String creator=(String)request.getAttribute("creator");
		String fileHashCode = (String)request.getAttribute("fileHashCode");
		//���ɼ�����Կ
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		//�����ļ����ƿ�
		String key=fileHashCode+fileHashCode.substring(0,num);
		System.out.println(key);
		String fileBlock = "filename:"+filename
				+"creator:"+creator
				+"fileHashCode:"+fileHashCode
				+"key:"+key;
		//���ļ����ƿ���м���
		String path=request.getServletContext().getRealPath("/fileblocks/");
		File filepath=new File(path,filename);
		//�ж�·���Ǵ��ڣ���������ھʹ���һ��
		if(!filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		File file = Util.encryptFile(fileBlock, encryptfile, key);
		//System.out.println(file.getAbsolutePath());
		
	}
	
	
	/*
	 * �ļ�����
	 */
	@RequestMapping(value="/file/download")
	public ResponseEntity<byte[]> fileDownload(HttpServletRequest request, 
			@RequestParam("filename") String filename,
			Model model)throws Exception{
		
		//�ҵ����ص��ļ�
		String path=request.getServletContext().getRealPath("/encryptedfiles/");
		File rawfile=new File(path+File.separator+filename);
		//���ļ����н���
		File decrypfile = new File("decryfile.txt");  //����һ����ʱ�ļ�,����������ļ�
		decrypfile.deleteOnExit();  //�Ƴ��������ͬʱ��ɾ���ļ�
		String key="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
		File file=Util.decryptFile2(rawfile,decrypfile,key);		
		System.out.println(decrypfile.getAbsolutePath());

		HttpHeaders headers= new HttpHeaders();
		//������ʾ���ļ������������������������
		String downloadFileName = new String(filename.getBytes("UTF-8"),"iso-8859-1");
		//֪ͨ�������attachment(���ط�ʽ)��ͼƬ
		headers.setContentDispositionFormData("attachment", downloadFileName);
		//application/octet-stream:�����������ݣ�������ļ����أ�
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		//201 HTTPStatus.Created
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers,HttpStatus.CREATED);	
	}
	

	//��InputStreamת����String
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
	
	//��InputStreamת����byte[]
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
		System.out.println("�ض���ɹ���");
		
	}
	
	
	/*
	 * �ϴ��ļ�
	 */
	@RequestMapping(value="/file/uploadDemo",method=RequestMethod.POST)
	public void uploadDemo(HttpServletRequest request,
			@RequestParam("description") String description,
			@RequestParam("file") MultipartFile file) throws Exception{
		System.out.println(description);
		//����ϴ����ļ���Ϊ��
		if(!file.isEmpty()) {
			String path=request.getServletContext().getRealPath("/encryptedfiles/");
			//�ϴ��ļ���
			String filename=file.getOriginalFilename();
			File filepath=new File(path,filename);
			//�ж�·���Ǵ��ڣ���������ھʹ���һ��
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
			System.out.println("��������ɹ�");
			String key="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
			//���ϴ��ļ����浽һ��Ŀ���ļ�����
			File encryptfile = new File(path+File.separator+filename);
			Util.encryptFile(fileData, encryptfile, key);
			System.out.println(encryptfile.getAbsolutePath());
			
		}
		
	}
	public void getKeyByGet(Map<String,String> params) {
		try {
			System.out.println("����getKeyByGet");
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
			//����ͨ�õ���������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//����ʵ������
			conn.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * �������������ļ����ƿ�
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
		//���ɼ�����Կ
		Random rand = new Random();
		int num = rand.nextInt(16);
		if(num<0)
		{
			num = -num;
		}
		//�����ļ����ƿ�
		String key=fileHashCode+fileHashCode.substring(0,num);
		System.out.println(key);
		String fileBlock = "filename:"+filename
				+"creator:"+creator
				+"fileHashCode:"+fileHashCode
				+"key:"+key;
		//���ļ����ƿ���м���
		String path=request.getServletContext().getRealPath("/fileblocks/");
		File filepath=new File(path,filename);
		//�ж�·���Ǵ��ڣ���������ھʹ���һ��
		if(!filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		File encryptfile = new File(path+File.separator+filename);
		File file = Util.encryptFile(fileBlock, encryptfile, key);
		System.out.println(file.getAbsolutePath());
	}

}













