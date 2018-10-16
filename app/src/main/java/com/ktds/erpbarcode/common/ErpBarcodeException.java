package com.ktds.erpbarcode.common;

import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;


public class ErpBarcodeException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private int errCode;
	private String errMessage;
	private int sound;

	public ErpBarcodeException() {
		super();
		this.errCode = -1;
		this.errMessage = "KT ERP Barcode System Unknown";
		this.sound = BarcodeSoundPlay.SOUND_NOT_PLAY;
	}
  
	public ErpBarcodeException(int errCode, String errMessage) {
		super(errMessage);
		this.errCode = errCode;
		this.errMessage = errMessage;
		this.sound = BarcodeSoundPlay.SOUND_NOT_PLAY;
	}
	
	public ErpBarcodeException(int errCode, String errMessage, int sound) {
		super(errMessage);
		this.errCode = errCode;
		this.errMessage = errMessage;
		this.sound = sound;
	}

	public int getErrCode() {
		return errCode;
	}

	public String getErrMessage() {
		return errMessage;
	}

	public int getSound() {
		return sound;
	}
}
