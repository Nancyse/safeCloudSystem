package com.nancyse.controller.demo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/json")
public class BookController {
	@RequestMapping(value="/testRequestBody")
	public Object getJson() {
		List<Book>list = new ArrayList<Book>();
		list.add(new Book(1,"mvc","nancyse"));
		list.add(new Book(2,"spring","book"));
		return list;
		
	}
	
	@RequestMapping(value="/login",method=RequestMethod.POST,consumes="application/json")
    public @ResponseBody Book login(@RequestBody Book book){
        System.out.println("name: "+book.getName());
        System.out.println("author: "+book.getAuthor());
        System.out.println("id: "+book.getId());
        return book;

        //return JacksonUtil.toJson(result);//Jacksonתjson
    }
	

}
