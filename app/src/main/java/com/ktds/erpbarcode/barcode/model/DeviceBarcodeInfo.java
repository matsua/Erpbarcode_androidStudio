package com.ktds.erpbarcode.barcode.model;

import java.io.Serializable;

public class DeviceBarcodeInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String deviceId;
	private String deviceName;
	private String projectNo;
	private String projectCode;
	private String wbsNo;
	private String operationSystemToken;
	private String operationSystemTokenName;
	private String operationSystemCode;
	private String locationIdToken;
	private String deviceStatusCode;
	private String deviceStatusName;
	private String itemCode;
	private String itemName;
	private String assetClassificationCode;
	private String assetClassificationName;
	private String migrationTypeCode;
	private String migrationTypeName;
	private String argumentDecisionName;
	private String standardServiceCode;
	private String standardServiceName;
	private String locationCode;
	private String locationName;
	// sesang 20190910 대표주소 정보 추가
	private String repLocationCode;
	private String repLocationName;
	// end sesang
	private String locationShortName;
	private String detailAddress;
	private String operationOrgCode;
	private String operatioinOrgName;
	private String ownOrgCode;
	private String ownOrgName;
	private String level1Code;
	private String level1Name;
	private String level2Code;
	private String level2Name;
	private String level3Code;
	private String level3Name;
	private String level4Code;
	private String level4Name;

	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getProjectNo() {
		return projectNo;
	}
	public void setProjectNo(String projectNo) {
		this.projectNo = projectNo;
	}
	public String getProjectCode() {
		return projectCode;
	}
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}
	public String getWbsNo() {
		return wbsNo;
	}
	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}
	public String getOperationSystemToken() {
		return operationSystemToken;
	}
	public void setOperationSystemToken(String operationSystemToken) {
		this.operationSystemToken = operationSystemToken;
	}
	public String getOperationSystemTokenName() {
		return operationSystemTokenName;
	}
	public void setOperationSystemTokenName(String operationSystemTokenName) {
		this.operationSystemTokenName = operationSystemTokenName;
	}
	public String getOperationSystemCode() {
		return operationSystemCode;
	}
	public void setOperationSystemCode(String operationSystemCode) {
		this.operationSystemCode = operationSystemCode;
	}
	public String getLocationIdToken() {
		return locationIdToken;
	}
	public void setLocationIdToken(String locationIdToken) {
		this.locationIdToken = locationIdToken;
	}
	public String getDeviceStatusCode() {
		return deviceStatusCode;
	}
	public void setDeviceStatusCode(String deviceStatusCode) {
		this.deviceStatusCode = deviceStatusCode;
	}
	public String getDeviceStatusName() {
		return deviceStatusName;
	}
	public void setDeviceStatusName(String deviceStatusName) {
		this.deviceStatusName = deviceStatusName;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getAssetClassificationCode() {
		return assetClassificationCode;
	}
	public void setAssetClassificationCode(String assetClassificationCode) {
		this.assetClassificationCode = assetClassificationCode;
	}
	public String getAssetClassificationName() {
		return assetClassificationName;
	}
	public void setAssetClassificationName(String assetClassificationName) {
		this.assetClassificationName = assetClassificationName;
	}
	public String getMigrationTypeCode() {
		return migrationTypeCode;
	}
	public void setMigrationTypeCode(String migrationTypeCode) {
		this.migrationTypeCode = migrationTypeCode;
	}
	public String getMigrationTypeName() {
		return migrationTypeName;
	}
	public void setMigrationTypeName(String migrationTypeName) {
		this.migrationTypeName = migrationTypeName;
	}
	public String getArgumentDecisionName() {
		return argumentDecisionName;
	}
	public void setArgumentDecisionName(String argumentDecisionName) {
		this.argumentDecisionName = argumentDecisionName;
	}
	public String getStandardServiceCode() {
		return standardServiceCode;
	}
	public void setStandardServiceCode(String standardServiceCode) {
		this.standardServiceCode = standardServiceCode;
	}
	public String getStandardServiceName() {
		return standardServiceName;
	}
	public void setStandardServiceName(String standardServiceName) {
		this.standardServiceName = standardServiceName;
	}
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	// sesang 20190910 대표주소 정보 추가
	public String getRepLocationCode() {
		return repLocationCode;
	}
	public void setRepLocationCode(String repLocationCode) {
		this.repLocationCode = repLocationCode;
	}
	public String getRepLocationName() {
		return repLocationName;
	}
	public void setRepLocationName(String repLocationName) {
		this.repLocationName = repLocationName;
	}
	// end sesang
	public String getDetailAddress() {
		return detailAddress;
	}
	public void setDetailAddress(String detailAddress) {
		this.detailAddress = detailAddress;
	}
	public String getOperationOrgCode() {
		return operationOrgCode;
	}
	public void setOperationOrgCode(String operationOrgCode) {
		this.operationOrgCode = operationOrgCode;
	}
	public String getOperatioinOrgName() {
		return operatioinOrgName;
	}
	public void setOperatioinOrgName(String operatioinOrgName) {
		this.operatioinOrgName = operatioinOrgName;
	}
	public String getOwnOrgCode() {
		return ownOrgCode;
	}
	public void setOwnOrgCode(String ownOrgCode) {
		this.ownOrgCode = ownOrgCode;
	}
	public String getOwnOrgName() {
		return ownOrgName;
	}
	public void setOwnOrgName(String ownOrgName) {
		this.ownOrgName = ownOrgName;
	}
	public String getLevel1Code() {
		return level1Code;
	}
	public void setLevel1Code(String level1Code) {
		this.level1Code = level1Code;
	}
	public String getLevel1Name() {
		return level1Name;
	}
	public void setLevel1Name(String level1Name) {
		this.level1Name = level1Name;
	}
	public String getLevel2Code() {
		return level2Code;
	}
	public void setLevel2Code(String level2Code) {
		this.level2Code = level2Code;
	}
	public String getLevel2Name() {
		return level2Name;
	}
	public void setLevel2Name(String level2Name) {
		this.level2Name = level2Name;
	}
	public String getLevel3Code() {
		return level3Code;
	}
	public void setLevel3Code(String level3Code) {
		this.level3Code = level3Code;
	}
	public String getLevel3Name() {
		return level3Name;
	}
	public void setLevel3Name(String level3Name) {
		this.level3Name = level3Name;
	}
	public String getLevel4Code() {
		return level4Code;
	}
	public void setLevel4Code(String level4Code) {
		this.level4Code = level4Code;
	}
	public String getLevel4Name() {
		return level4Name;
	}
	public void setLevel4Name(String level4Name) {
		this.level4Name = level4Name;
	}
	public String getLocationShortName() {
		return locationShortName;
	}
	public void setLocationShortName(String locationShortName) {
		this.locationShortName = locationShortName;
	}

}
