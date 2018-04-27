package com.nancyse.controller.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TestFilePath {
	public static String path="E:/files/fileblocks/test.txt";
	public static void main(String[] args) throws FileNotFoundException {
		File file=new File(path);
		System.out.println(file.getAbsolutePath());
		FileReader fileInput = new FileReader(file);
		String result="";
		int hasCode=0;
		char[] cbuf = new char[32];
		try {
			while((hasCode=fileInput.read(cbuf))>0) {
				result+=new String(cbuf,0,hasCode);
			}
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
