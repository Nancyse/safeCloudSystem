package com.nancyse.controller.GenericServer.DataModel;

public class DefaultFile {
	private String file_name;
	private String file_type;
	private long file_size;
	private String file_dir;
	private String file_hash;
	private String file_key;
	private String file_uploader;
	
	public DefaultFile() {
		
	}
	public DefaultFile(String name,String type,long size,String dir, String hash,String key,String loader) {
		this.file_name=name;
		this.file_type=type;
		this.file_size=size;
		this.file_dir = dir;
		this.file_hash=hash;
		this.file_key=key;
		this.file_uploader=loader;
	}
	public String getFile_dir() {
		return file_dir;
	}
	public void setFile_dir(String file_dir) {
		this.file_dir = file_dir;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public String getFile_type() {
		return file_type;
	}
	public void setFile_type(String file_type) {
		this.file_type = file_type;
	}
	public long getFile_size() {
		return file_size;
	}
	public void setFile_size(long file_size) {
		this.file_size = file_size;
	}
	public String getFile_hash() {
		return file_hash;
	}
	public void setFile_hash(String file_hash) {
		this.file_hash = file_hash;
	}
	public String getFile_key() {
		return file_key;
	}
	public void setFile_key(String file_key) {
		this.file_key = file_key;
	}
	public String getFile_uploader() {
		return file_uploader;
	}
	public void setFile_uploader(String file_uploader) {
		this.file_uploader = file_uploader;
	}
	
}
