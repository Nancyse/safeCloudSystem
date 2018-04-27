package com.nancyse.controller.demo;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

//import org.apache.tomcat.util.codec.binary.Base64;

public class EncodeAndDecode {
	public static void main(String[] args) throws UnsupportedEncodingException {
		String str="5ZSxfy5+J+mRmueHsiXnrr5t5ai+57KIB+aRn+iOku+/vTp8EO+/vQZsOO6RuUbvv70bRO+/vS4BFgLnrbDmuJfun53oi4vonqzku5AnJ+ebpu+/vTMu7oeiAjdza++/vR7kvJzlkIE/LEDvv70QbemvoD9q5Lyo5p2hVD41Ye+/vSnov7ZQetCx5Yi9dOefjl3vv70WCu6QiOiTvemzmRltfSHojrlA77+9Gui6uO+/vQR0OgHnkJVN77+9Mei+uk8b6Je66IaxCO+/vQflm7MpHueCsXo0Pua6neWCsUFfdjXnmbzvv70Y77+9DOWMjklBDWTorbdq5b2855eO5bKebyLvv70k5buf7pWP77+9MjDvv70HC+eQvO+/vRXlsonpg7Bd77+9";
		String result = new String(Base64.getDecoder().decode(str),"UTF-8");
		System.out.println(result);
	
	}

}
