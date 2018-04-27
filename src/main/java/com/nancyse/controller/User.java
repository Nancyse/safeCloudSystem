package com.nancyse.controller;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

public class User implements Serializable{
	private String username;
	//对应上传的file,类型为MultipartFile,上传文件会自动绑定到image属性当中
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
