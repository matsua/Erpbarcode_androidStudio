package com.ktds.erpbarcode.common;

import org.json.JSONObject;

public class ErpBarEventMessage {
	public static final int MESSAGE_TYPE_YES = 1;
	public static final int MESSAGE_TYPE_NO = 2;
	
	private int messageType;
	private String message;
	private JSONObject jsonMessage;
	
	public ErpBarEventMessage(int messageType, String message) {
		this.messageType = messageType;
		this.message = message;
	}
	
	public ErpBarEventMessage(int messageType, JSONObject jsonMessage) {
		this.messageType = messageType;
		this.jsonMessage = jsonMessage;
	}

	public int getMessageType() {
		return messageType;
	}
	public String getMessage() {
		return message;
	}
	public JSONObject getJsonMessage() {
		return jsonMessage;
	}
	
}
