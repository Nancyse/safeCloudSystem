package com.nancyse.controller.NewServer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/bs")
public class UploadController {
	
	@RequestMapping(value="/index")
	public String index() {
		return "safeCloudSystem/index.jsp";
	}
	
	@RequestMapping(value="/uploadfile")
	public String uploadfile() {
		return "safeCloudSystem/uploadfile.jsp";
	}
	
	@RequestMapping(value="/persondetail")
	public String persondetail() {
		return "safeCloudSystem/persondetail.jsp";
	}
	
	@RequestMapping(value="/login2")
	public String login() {
		return "safeCloudSystem/login.jsp";
	}
	
	@RequestMapping(value="/filedetail")
	public String filedetail() {
		return "safeCloudSystem/filedetail.jsp";
	}
	
	@RequestMapping(value="/commonfaq")
	public String commonfaq() {
		return "safeCloudSystem/commonfaq.jsp";
	}
	

}
