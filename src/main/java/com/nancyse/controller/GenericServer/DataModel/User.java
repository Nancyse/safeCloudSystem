package com.nancyse.controller.GenericServer.DataModel;

public class User {
	private String user_name;
	private String user_pwd;
	private Integer user_type=0;
	private String user_email;
	private String user_space;
	
	
	public User() {
		super();
	}
	public User(String username,String pwd, Integer type,String email,String space) {
		this.user_name=username;
		this.user_pwd=pwd;
		this.user_type=type;		
		this.user_email=email;
		this.user_space=space;
	}
	
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getUser_pwd() {
		return user_pwd;
	}
	public void setUser_pwd(String user_pwd) {
		this.user_pwd = user_pwd;
	}
	public Integer getUser_type() {
		return user_type;
	}
	public void setUser_type(Integer user_type) {
		this.user_type = user_type;
	}
	public String getUser_space() {
		return user_space;
	}
	public void setUser_space(String user_space) {
		this.user_space = user_space;
	}
	public String getUser_email() {
		return user_email;
	}
	public void setUser_email(String user_email) {
		this.user_email = user_email;
	}
	
}
