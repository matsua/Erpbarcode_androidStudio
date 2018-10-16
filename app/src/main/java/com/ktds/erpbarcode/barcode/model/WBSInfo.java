package com.ktds.erpbarcode.barcode.model;

import java.io.Serializable;

public class WBSInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String contractorName;    // 시공사정보
	private String wbsNo;             // WBS번호
	private String jpjtType;          // 공사유형
	private String wbsHistory;        // WBS내역
	private String wbsStatus;         // WBS상태
	private String wbsStatusName;     // WBS상태명
	private String kostlStatus;       // 코스트센터상태

	public String getContractorName() {
		return contractorName;
	}
	public void setContractorName(String contractorName) {
		this.contractorName = contractorName;
	}
	public String getWbsNo() {
		return wbsNo;
	}
	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}
	public String getJpjtType() {
		return jpjtType;
	}
	public void setJpjtType(String jpjtType) {
		this.jpjtType = jpjtType;
	}
	public String getWbsHistory() {
		return wbsHistory;
	}
	public void setWbsHistory(String wbsHistory) {
		this.wbsHistory = wbsHistory;
	}
	public String getWbsStatus() {
		return wbsStatus;
	}
	public void setWbsStatus(String wbsStatus) {
		this.wbsStatus = wbsStatus;
	}
	public String getWbsStatusName() {
		return wbsStatusName;
	}
	public void setWbsStatusName(String wbsStatusName) {
		this.wbsStatusName = wbsStatusName;
	}
	public String getKostlStatus() {
		return kostlStatus;
	}
	public void setKostlStatus(String kostlStatus) {
		this.kostlStatus = kostlStatus;
	}
}
