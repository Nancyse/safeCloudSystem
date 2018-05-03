package com.nancyse.controller.GenericServer.DataModel;

public class User {
	public String user_name;
	public String user_pwd;
	public Integer user_type=0;
	public User() {
		super();
	}
	public User(String username,String pwd, Integer type) {
		this.user_name=username;
		this.user_pwd=pwd;
		this.user_type=type;		
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
	
	
}
