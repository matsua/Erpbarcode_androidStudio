package com.ktds.erpbarcode.survey.model;

public class ProductSurveyInfo {
    private String itemNumber;     // 항번
    private String productCode;    // 자재코드
    private String productName;    // 자재명 
    private int quantity;          // 수량
    private int scanQuantity;      // 스캔수량
    private String batchNumber;    // 배치번호

	public String getItemNumber() {
		return itemNumber;
	}
	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getScanQuantity() {
		return scanQuantity;
	}
	public void setScanQuantity(int scanQuantity) {
		this.scanQuantity = scanQuantity;
	}
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
}
