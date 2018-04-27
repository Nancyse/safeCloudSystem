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
	 * �ļ��ϴ������
	 */
	@RequestMapping(value="/uploadForm",method=RequestMethod.GET)
	public String index(HttpServletRequest request){
		System.out.println("you are successful send request");
		return "fileUploadTest/uploadForm";
	}
	
	/*
	 * �ϴ��ļ�
	 */
	@RequestMapping(value="/upload",method=RequestMethod.POST)
	public void upload(HttpServletRequest request,
			@RequestParam("description") String description,
			@RequestParam("file") MultipartFile file) throws Exception{
		
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
			//���ļ������ݶ�ȡΪ����������
			byte[] fileData = getFileDataAsBytes(fis);
			String hash=Util.getSHA256StrJava(fileData);
			System.out.println(hash);
			
			Map<String,String> params=new HashMap<String,String>();
			params.put("filename", filename);
			params.put("creator", "nancyse");
			params.put("fileHashCode", hash);
			
			//��ȡ������Կ���ļ����ƿ�
			String result = getEncryptKey(params);
			System.out.println("AS:"+result);
			ObjectMapper objMapper = new ObjectMapper();
			FileData fd = objMapper.readValue(result, FileData.class);
			String key = fd.getEncryptKey();
			String fileblock = fd.getFileblock();
			System.out.println("fileblock: "+fileblock);
			
			//String key="40f192f85aee2f1736c288218569f2d6d5e0fbb41fff6b316f6a33046872ec5f40f1";
			//���ϴ��ļ����浽һ��Ŀ���ļ�����
			File encryptfile = new File(path+File.separator+filename);
			Util.encryptFile(fileData, encryptfile, key);
			System.out.println(encryptfile.getAbsolutePath());
			
			
		}
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
	
	//��ȡ������Կ
	public String getEncryptKey(Map<String,String> params) {
		String result="";
		try {
			System.out.println("����getKeyByGet");
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
			//����ͨ�õ���������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//����ʵ������
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
	 * �ļ�����
	 */
	@RequestMapping(value="/download")
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
	
	//��������ת���������
	public ByteArrayOutputStream parse(InputStream in) throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while( (ch=in.read())!=-1)  //ÿ��ֻ��һ���ַ�
		{
			out.write(ch);
		}
		return out;
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

	
	
	

}













