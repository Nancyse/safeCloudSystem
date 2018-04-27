package com.nancyse.controller;

public class FileData {
	String fileblock;
	String encryptKey;
	
	public FileData() {}
	public FileData(String encryptKey,String fileblock) {
		this.encryptKey = encryptKey;
		this.fileblock = fileblock;
	}
	public String getFileblock() {
		return fileblock;
	}
	public void setFileblock(String fileblock) {
		this.fileblock = fileblock;
	}
	public String getEncryptKey() {
		return encryptKey;
	}
	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey;
	}
	

}
