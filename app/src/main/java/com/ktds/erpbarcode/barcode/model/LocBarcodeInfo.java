package com.ktds.erpbarcode.barcode.model;

import java.io.Serializable;

public class LocBarcodeInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String locCd;
	private String locName;
	private String deviceId;
	private String roomTypeCode;
	private String roomTypeName;
	private String operationSystemCode;
	private String orgId;                 // 부서코드(운용)   (ZKOSTL)
	private String orgName;               // 부서명(운용)    (ZKTEXT)
	private String locationCode;
	private String locationFullName;
	private String locationName;
	private String locationName2;
	private String locationShortName;
	private String locationTypeCode;
	private String locationTypeName;
	private double latitude;              // 위도
	private double longitude;             // 경도
	private double diffTitude;            // 거리(Km)

	// sesang 20190910 대표위치 정보 추가
	private String repLocCd;
	private String repLocNm;
	// end sesang

	public LocBarcodeInfo() {
		clear();
	}
	
	public void clear() {
		setLocCd("");
		setLocName("");
		setRepLocCd("");
		setRepLocNm("");
		setDeviceId("");
		setRoomTypeCode("");
		setRoomTypeName("");
		setOperationSystemCode("");
		setOrgId("");
		setOrgName("");
		setLocationCode("");
		setLocationFullName("");
		setLocationName("");
		setLocationName2("");
		setLocationShortName("");
		setLocationTypeCode("");
		setLocationTypeName("");
		setLatitude(0);
		setLongitude(0);
		setDiffTitude(0);
	}

	public String getLocCd() {
		return locCd;
	}

	public void setLocCd(String locCd) {
		this.locCd = locCd;
	}

	public String getLocName() {
		return locName;
	}

	public void setLocName(String locName) {
		this.locName = locName;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getRoomTypeCode() {
		return roomTypeCode;
	}

	public void setRoomTypeCode(String roomTypeCode) {
		this.roomTypeCode = roomTypeCode;
	}

	public String getRoomTypeName() {
		return roomTypeName;
	}

	public void setRoomTypeName(String roomTypeName) {
		this.roomTypeName = roomTypeName;
	}

	public String getOperationSystemCode() {
		return operationSystemCode;
	}

	public void setOperationSystemCode(String operationSystemCode) {
		this.operationSystemCode = operationSystemCode;
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

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getLocationFullName() {
		return locationFullName;
	}

	public void setLocationFullName(String locationFullName) {
		this.locationFullName = locationFullName;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationName2() {
		return locationName2;
	}

	public void setLocationName2(String locationName2) {
		this.locationName2 = locationName2;
	}

	public String getLocationShortName() {
		return locationShortName;
	}

	public void setLocationShortName(String locationShortName) {
		this.locationShortName = locationShortName;
	}

	public String getLocationTypeCode() {
		return locationTypeCode;
	}

	public void setLocationTypeCode(String locationTypeCode) {
		this.locationTypeCode = locationTypeCode;
	}

	public String getLocationTypeName() {
		return locationTypeName;
	}

	public void setLocationTypeName(String locationTypeName) {
		this.locationTypeName = locationTypeName;
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

	public double getDiffTitude() {
		return diffTitude;
	}

	public void setDiffTitude(double diffTitude) {
		this.diffTitude = diffTitude;
	}

	public String getRepLocCd() {
		return repLocCd;
	}

	public void setRepLocCd(String repLocCd) {
		this.repLocCd = repLocCd;
	}

	public String getRepLocNm() {
		return repLocNm;
	}

	public void setRepLocNm(String repLocNm) {
		this.repLocNm = repLocNm;
	}
}
