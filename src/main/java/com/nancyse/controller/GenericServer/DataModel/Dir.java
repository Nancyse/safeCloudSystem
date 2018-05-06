package com.nancyse.controller.GenericServer.DataModel;

public class Dir {
	private String dir_name;
	private int dir_size;
	
	public Dir() {}
	public Dir(String name, int size) {
		this.dir_name=name;
		this.dir_size=size;
	}
	public String getDir_name() {
		return dir_name;
	}
	public void setDir_name(String dir_name) {
		this.dir_name = dir_name;
	}
	public int getDir_size() {
		return dir_size;
	}
	public void setDir_size(int dir_size) {
		this.dir_size = dir_size;
	}
	
}
