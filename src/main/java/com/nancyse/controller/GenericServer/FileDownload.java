package com.nancyse.controller.GenericServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping(value="/gs")
@Controller
public class FileDownload {
	
	
	@RequestMapping(value="/filedownload")
	public ResponseEntity<byte[]> fileDownload(
			HttpServletRequest request,
			@RequestParam("filename") String filename
			) throws IOException, Exception {
		//��ý�����Կ
		String fileKey = getDecryptKey(request,filename);
		FileEncryptUtil.logger.info("decrypt key:"+fileKey);
		
		//�ҵ����ص��ļ�
		//String path=request.getServletContext().getRealPath("/encryptFiles/");
		String path=FilePath.DOWNLOADFILEPATH;
		File rawfile=new File(path+File.separator+filename);
		
		//�����ļ�
		File decrypfile = new File("decryfile.txt");  //����һ����ʱ�ļ�,����������ļ�
		decrypfile.deleteOnExit();  //�Ƴ��������ͬʱ��ɾ���ļ�
		File file=FileEncryptUtil.decryptFile2(rawfile,decrypfile,fileKey);		
		FileEncryptUtil.logger.info(decrypfile.getAbsolutePath());
		//���ļ�����ȷ�Խ���У��
		
		
		//�����ļ����ͻ���
		HttpHeaders headers= new HttpHeaders();
		//������ʾ���ļ������������������������
		String downloadFileName = new String(filename.getBytes("UTF-8"),"iso-8859-1");
		//֪ͨ�������attachment(���ط�ʽ)��ͼƬ
		headers.setContentDispositionFormData("attachment", downloadFileName);
		//application/octet-stream:�����������ݣ�������ļ����أ�
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(decrypfile),headers,HttpStatus.CREATED);	
	}
	
	
	/*
	 * ��ȡ������Կ
	 */
	public String getDecryptKey(
			HttpServletRequest request,
			String filename) throws IOException {
		String decryptKey="";
		//String path=request.getServletContext().getRealPath("/fileblocks/");
		//File rawfile=new File(path+File.separator+filename);
		String path=FilePath.FILEBLOCKPATH;
		File rawfile=new File(path+filename);
		System.out.println(rawfile.getAbsolutePath());
		String filedata=getFileAsString(rawfile);
		decryptKey=getFileKey(filedata);
		
		return decryptKey;		
	}
	
	/*
	 * ��ȡ�����ļ�����Կ
	 */
	public String getFileKey(String filedata) {
		String fileKey="";
		String urlName = FileEncryptUtil.hostName+"/safeCloudSystem/AS2/decryptFileBlock?block="+filedata;

		try {
			URLConnection conn = new URL(urlName).openConnection();
			//����ͨ�õ���������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			//����ʵ������
			conn.connect();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String strReader="";
			while( (strReader=br.readLine())!=null) {
				fileKey+=strReader;
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileKey;		
	}
	
	/*
	 *��ȡ�ļ�����
	 */
	private String getFileAsString(File file) throws IOException {
		String result="";
		FileReader fileInput = new FileReader(file);
		int hasCode=0;
		char[] cbuf = new char[32];
		while((hasCode=fileInput.read(cbuf))>0) {
			result+=new String(cbuf,0,hasCode);
		}
		return result;
	}

}
