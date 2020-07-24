package com.onlineinteract.workflow.utility;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {

	public static String toJson(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			System.out.println("There was a problem converting object to json String: " + e.getOriginalMessage());
		}
		return json;
	}

	public static <T> T fromJson(String json, Class<T> c) {
		ObjectMapper mapper = new ObjectMapper();
		T readValue = null;
		try {
			readValue = mapper.readValue(json, c);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readValue;
	}
}
