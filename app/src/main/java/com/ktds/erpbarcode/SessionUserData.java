package com.ktds.erpbarcode;

import com.ktds.erpbarcode.common.http.HttpAddressConfig;

public class SessionUserData {
	private static SessionUserData instance;
	
	private boolean authenticated;
	private String userId;
	private String userPasswd;
	private String userName;
	private String telNo;
	private String userCellPhoneNo;
	private String orgId;               // 코스트센터
	private String orgName;
	private String orgCode;
	private String orgTypeCode;         // 조직타입
	private String companyCode;         // 사업자번호
	private String sessionId;
	private String jobEqunr;
	private String confirmationYn;
	private String confirmationAminYn;
	private String passwdUpdateYn;
	private double latitude;            // device 위도.
	private double longitude;           // device 경도.
	private boolean isOffline;
	private String newAppUri;
	private int newAppVersionCode;
	private String accessServer;
	private String accessServerName;
	private String centerName;         // 유지보수 센터 이름
	private String centerId;           // 유지보수 센터 아이디
	private String summaryOrgName;     // 조직 풀 네임

	public static synchronized SessionUserData getInstance() {
		if (instance==null) {
			instance=new SessionUserData();
			instance.setAuthenticated(false);
			instance.setUserId("");
			instance.setUserPasswd("");
			instance.setUserName("");
			instance.setTelNo("");	// PDA번호 -> 사용자의 전화번호로 나중에 셋팅
			instance.setUserCellPhoneNo("");
			instance.setOrgId("");
			instance.setOrgName("");
			instance.setOrgCode("");
			instance.setOrgTypeCode("");
			instance.setCompanyCode("");
			instance.setSessionId("");
			instance.setJobEqunr("");
			instance.setConfirmationYn("");
			instance.setConfirmationAdminYn("");
			instance.setPasswdUpdateYn("");
			instance.setLatitude(0L);
			instance.setLongitude(0L);
			instance.setOffline(false);
			instance.setNewAppUri("");
			instance.setNewAppVersionCode(0);
			instance.setAccessServer("");
		}
		return instance;
	}


	public boolean isAuthenticated() {
		return authenticated;
	}


	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getUserPasswd() {
		return userPasswd;
	}


	public void setUserPasswd(String userPasswd) {
		this.userPasswd = userPasswd;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getTelNo() {
		return telNo;
	}


	public void setTelNo(String telNo) {
		this.telNo = telNo;
	}


	public String getUserCellPhoneNo() {
		return userCellPhoneNo;
	}


	public void setUserCellPhoneNo(String userCellPhoneNo) {
		this.userCellPhoneNo = userCellPhoneNo;
	}


	public String getOrgId() {
		return orgId;
	}


	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}


	public String getOrgName() {
		return orgName;
	}


	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}


	public String getOrgCode() {
		return orgCode;
	}


	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}


	public String getOrgTypeCode() {
		return orgTypeCode;
	}


	public void setOrgTypeCode(String orgTypeCode) {
		this.orgTypeCode = orgTypeCode;
	}


	public String getCompanyCode() {
		return companyCode;
	}


	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}


	public String getSessionId() {
		return sessionId;
	}


	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	public String getJobEqunr() {
		return jobEqunr;
	}


	public void setJobEqunr(String jobEqunr) {
		this.jobEqunr = jobEqunr;
	}


	public String getConfirmationYn() {
		return confirmationYn;
	}

	public void setConfirmationYn(String confirmationYn) {
		this.confirmationYn = confirmationYn;
	}
	public String getConfirmationAdminYn() {
		return confirmationAminYn;
	}
	public void setConfirmationAdminYn(String confirmationAminYn) {
		this.confirmationAminYn = confirmationAminYn;
	}
	public String getPasswdUpdateYn(){
		return passwdUpdateYn;
	}
	
	public void setPasswdUpdateYn(String passwdUpdateYn) {
		this.passwdUpdateYn = passwdUpdateYn;
	}


	public double getLatitude() {
		return latitude;
	}


	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public double getLongitude() {
		return longitude;
	}


	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


	public boolean isOffline() {
		return isOffline;
	}


	public void setOffline(boolean isOffline) {
		this.isOffline = isOffline;
	}


	public String getNewAppUri() {
		return newAppUri;
	}


	public void setNewAppUri(String newAppUri) {
		this.newAppUri = newAppUri;
	}


	public int getNewAppVersionCode() {
		return newAppVersionCode;
	}


	public void setNewAppVersionCode(int newAppVersionCode) {
		this.newAppVersionCode = newAppVersionCode;
	}


	public String getAccessServer() {
		return accessServer;
	}


	public void setAccessServer(String accessServer) {
		this.accessServer = accessServer;
		if (accessServer.equals(HttpAddressConfig.APP_SERVER)) {
			this.accessServerName = "운영";
		} else {
			this.accessServerName = "QA";
		}
	}

	public String getAccessServerName() {
		return accessServerName;
	}


	public String getCenterName() {
		return centerName;
	}


	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}


	public String getCenterId() {
		return centerId;
	}

	public void setCenterId(String centerId) {
		this.centerId = centerId;
	}
	
	public String getSummaryOrgName() {
		return summaryOrgName;
	}


	public void setSummaryOrgName(String summaryOrgName) {
		this.summaryOrgName = summaryOrgName;
	}
}
