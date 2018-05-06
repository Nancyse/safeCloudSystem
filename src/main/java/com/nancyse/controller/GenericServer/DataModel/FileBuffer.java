package com.nancyse.controller.GenericServer.DataModel;

import java.util.Date;

public class FileBuffer {
	private String bufferfile_name;
	private String bufferfile_dir;
	private String uploader_name;
	private long bufferfile_size;
	private Date update_time;
	
	public FileBuffer() {}
	public FileBuffer(String name,String dir,String uploader,int size,Date time) {
		this.bufferfile_name=name;
		this.bufferfile_dir=dir;
		this.bufferfile_size=size;
		this.uploader_name=uploader;
		this.update_time=time;
	}
	public String getBufferfile_name() {
		return bufferfile_name;
	}
	public void setBufferfile_name(String bufferfile_name) {
		this.bufferfile_name = bufferfile_name;
	}
	public String getBufferfile_dir() {
		return bufferfile_dir;
	}
	public void setBufferfile_dir(String bufferfile_dir) {
		this.bufferfile_dir = bufferfile_dir;
	}
	public String getBufferfile_uploader() {
		return uploader_name;
	}
	public void setBufferfile_uploader(String bufferfile_uploader) {
		this.uploader_name = bufferfile_uploader;
	}
	public long getBufferfile_size() {
		return bufferfile_size;
	}
	public void setBufferfile_size(long bufferfile_size) {
		this.bufferfile_size = bufferfile_size;
	}
	public Date getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(Date update_time) {
		this.update_time = update_time;
	}
	
}
