package com.ktds.erpbarcode.common;

import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;


public class ErpBarcodeMessage {
	
	public static final int NORMAL_PROGRESS_MESSAGE_CODE = 99000;      // 정상이며, 메시지만 출력.
	
	public static final String OFFLINE_MESSAGE_CANT_SEND = "'음영지역작업' 중에는\n\r'전송' 하실 수 없습니다.\n\r1. 먼저 '저장' 하신 후\n\r2. 네트워크 접속으로 로그인 하시고\n\r3. '저장' 하신 자료를 불러와서\n\r4. '전송' 하시기 바랍니다.";
	public static final String OFFLINE_MESSAGE = "'음영지역작업' 중입니다.";

	private int messageCode;
	private String message;
	private int sound;

	public ErpBarcodeMessage() {
		super();
		this.messageCode = 0;
		this.message = "";
		this.sound = BarcodeSoundPlay.SOUND_NOT_PLAY;
	}
  
	public ErpBarcodeMessage(int messageCode, String message) {
		this.messageCode = messageCode;
		this.message = message;
		this.sound = BarcodeSoundPlay.SOUND_NOT_PLAY;
	}
	
	public ErpBarcodeMessage(int messageCode, String message, int sound) {
		this.messageCode = messageCode;
		this.message = message;
		this.sound = sound;
	}

	public int getMessageCode() {
		return messageCode;
	}

	public String getMessage() {
		return message;
	}

	public int getSound() {
		return sound;
	}
}
