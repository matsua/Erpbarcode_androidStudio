package com.ktds.erpbarcode.infosearch.model;

public class OrgCodeInfo {
	private String orgCode;
	private String orgName;
	private String parentOrgCode;
	private int level;
	private String costCenter;
	private boolean isSearched = false;
	
	private boolean isChecked;
	private String userId;
	private String userName;

	public String getOrgCode() {
		return orgCode;
	}
	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getParentOrgCode() {
		return parentOrgCode;
	}
	public void setParentOrgCode(String parentOrgCode) {
		this.parentOrgCode = parentOrgCode;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getCostCenter() {
		return costCenter;
	}
	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}
	public boolean isSearched() {
		return isSearched;
	}
	public void setSearched(boolean isSearched) {
		this.isSearched = isSearched;
	}
	
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getUserNm() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
