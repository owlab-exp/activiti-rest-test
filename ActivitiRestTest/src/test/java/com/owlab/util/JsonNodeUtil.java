package com.owlab.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonNodeUtil {
	
	public static void beautifulPrint(JsonNode jsonNode) {
		if(jsonNode == null) {
			System.out.println("null parameter.");
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object o;
		try {
			o = mapper.treeToValue(jsonNode, Object.class);
		
			System.out.println(mapper.writeValueAsString(o));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
