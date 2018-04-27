package com.nancyse.controller.demo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestServletPath {
	
	@RequestMapping(value="/testServletPath")
	public void testServletPath(HttpServletRequest req) {
		String path=req.getServletContext().getRealPath("/encryptFiles/");
		System.out.println(path);
		
	}

}
