package com.pascal.prettymd;

public class JsonObject {
	
	public String type;
	public String text;
	public String options;
	
	public JsonObject(String type, String text) {
		this.type = type;
		this.text = text;
	}
	
	public JsonObject(String type, String text, String options) {
		this.type = type;
		this.text = text;
		this.options = options;
	}

}
