package com.pascal.prettymd;

public class JsonObject {
	
	public String text;
	public String options;
	
	public JsonObject(String text) {
		this.text = text;
	}
	
	public JsonObject(String text, String options) {
		this.text = text;
		this.options = options;
	}

}
