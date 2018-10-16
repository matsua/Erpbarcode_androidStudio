package com.ktds.erpbarcode.management.model;

public class RemodelBarcodeInfo {
	private String matnr;     // 자재코드
	private String maktx;     // 자재명
	private String upgdoc;    // 지시서번호

	public String getProductCode() {
		return matnr;
	}
	public void setProductCode(String matnr) {
		this.matnr = matnr;
	}
	public String getProductName() {
		return maktx;
	}
	public void setProductName(String maktx) {
		this.maktx = maktx;
	}
	public String getUpgdoc() {
		return upgdoc;
	}
	public void setUpgdoc(String upgdoc) {
		this.upgdoc = upgdoc;
	}
}
