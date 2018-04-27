package com.nancyse.controller.demo;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJson {
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String carJson= "{\"author\":\"nancy\",\"name\":\"nancyse\",\"id\":\"5\"}";
		Book book = objectMapper.readValue(carJson, Book.class);
		System.out.println(book.getAuthor());
		System.out.println(book.getName());
		System.out.println(book.getId());
	}
	

}
