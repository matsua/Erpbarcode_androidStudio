package com.ktds.erpbarcode.management.model;

import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;

public class ArgumentConfirmInfo {
	private String deviceId;
	private String locCd;
	private String docNo;
	private String daori;
	private String wbsNo;
	private String hostYn;
	private String usgYn;
	private DeviceBarcodeInfo deviceBarcodeInfo;

	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getLocCd() {
		return locCd;
	}
	public void setLocCd(String locCd) {
		this.locCd = locCd;
	}
	public String getDocNo() {
		return docNo;
	}
	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}
	public String getDaori() {
		return daori;
	}
	public void setDaori(String daori) {
		this.daori = daori;
	}
	public String getWbsNo() {
		return wbsNo;
	}
	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}
	public String getHostYn() {
		return hostYn;
	}
	public void setHostYn(String hostYn) {
		this.hostYn = hostYn;
	}
	public String getUsgYn() {
		return usgYn;
	}
	public void setUsgYn(String usgYn) {
		this.usgYn = usgYn;
	}
	public DeviceBarcodeInfo getDeviceBarcodeInfo() {
		return deviceBarcodeInfo;
	}
	public void setDeviceBarcodeInfo(DeviceBarcodeInfo deviceBarcodeInfo) {
		this.deviceBarcodeInfo = deviceBarcodeInfo;
	}
}
