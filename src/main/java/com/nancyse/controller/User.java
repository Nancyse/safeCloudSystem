package com.nancyse.controller;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

public class User implements Serializable{
	private String username;
	//��Ӧ�ϴ���file,����ΪMultipartFile,�ϴ��ļ����Զ��󶨵�image���Ե���
	private MultipartFile image;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public MultipartFile getImage() {
		return image;
	}
	public void setImage(MultipartFile image) {
		this.image = image;
	}
	
	

}
