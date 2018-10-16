package com.ktds.erpbarcode.barcode.model;

import java.io.Serializable;

import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;

public class BarcodeListInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private boolean checked;
	private String jobGubun;              // 업무구분
	private String partTypeCode;          // 부품종류코드
	private String partType;              // 파트타입     (ZPPART, ZPGUBUN) L:loc, D:device, R:rack, S:shelf, U:unit, E:Equipment
	private String partTypeName;          // 부품종류명
	private String barcode;               // 바코드      (EQUNR)
	private String barcodeName;           // 바코드명     (EQKTX)
	private String uFacCd;                // 상위바코드
	private String uFacCdName;            // 상위바코드명  (HEQKTX)
	private String level;				  // 설비레벨 
	private String serverUFacCd;          // 서버상위바코드
	private String facStatus;             // 설비상태코드  (ZPSTATU)
	private String facStatusName;         // 설비상태명
	private String facSergeNum;           // 설비S/N
	private String locCd;                 // 위치바코드    (ZEQUIPLP)      ** 만약 partType = "L"일때 locCd == barcode
	private String locName;               // 위치바코드명
	private String devType;               // 품목구분코드   (ZPGUBUN)
	private String devTypeName;           // 품목구분코드명
	private String scanValue;             // 스캔값(0.기본, 1.스캔, 2.스캔추가)
	private String crudType;              // CRUD타입 (db.DB search, add.형상에 추가)
	private String wbsNo;                 // WBS번호
	private String deviceId;              // 장치ID       (ZEQUIPGC)      ** 만약 partType = "D"일때 장치ID == barcode
	private String itemCode;              // 장치(아이템코드)
	private String itemName;              // 장치(아이템명)
	private String deviceStatusCode;      // 장치(장치상태코드)
	private String deviceStatusName;      // 장치(장치상태명)
	private String operationSystemCode;   // 장치(무선장치ID)
	private String itemNo;
	private String sloss;
	private String transNo;
	private String checkValue;            // 현장점검에서만 사용함.
	private String checkOrgYn;            // 운용조직여부
	private String orgId;                 // 부서코드(운용)   (ZKOSTL)
	private String orgName;               // 부서명(운용)    (ZKTEXT)
	private String productCode;           // 자재코드       (SUBMT)
	private String productName;           // 자재명         (MAKTX)
	private String hequi;
	private String oldBarcode;            // 수리/개조(구바코드)
	private String failureCode;           // 수리/개조(고장코드)
	private String failureName;           // 수리/개조(고장명)
	private String partnerCode;           // 수리/개조(협력사코드)
	private String partnerName;           // 수리/개조(협력사명)
	private String failureRegNo;          // 수리/개조(고장등록번호)
	private String repairRequestNo;       // 수리/개조(수리의뢰번호)
	private String upgdoc;				  // 개조개량의뢰(지시서번호)
	private String zkequi;				  // 설비처리구분
	private String zanln1;				  // 자산상태
	private String ansdt;				  // 취득일
	private String zsetup;				  // 공사비 
	private String urcodn;
	private String mncodn;
	private String rreason;
	private String comment;
	private IsmBarcodeInfo ismBarcodeInfo; // 인스토어 마킹
	private String o_data_c;
	private String skostl;                 // 접수(팀간) 에서 SelectOrgCodeActivity에서 사용. 
	private String skostlTxt;              // 접수(팀간) 에서 SelectOrgCodeActivity에서 사용. 
	private String cboPhotoUri;            // 고장등록시 설비 이미지
	private String host_yn;
	private String usg_yn;
	private String hequnr;					//상위바코
	private String gwlen_o;					//보증종료일 

	public BarcodeListInfo() { clear(); }
	
	private void clear() {
		this.checked = false;
		this.jobGubun = "";
		this.partTypeCode = "";
		this.partType = "";
		this.partTypeName = "";
		this.barcode = "";
		this.barcodeName = "";
		this.uFacCd = "";
		this.uFacCdName = "";
		this.level = "";
		this.facStatus = "";
		this.facStatusName = "";
		this.facSergeNum="";
		this.locCd = "";
		this.locName = "";
		this.devType = "";
		this.devTypeName = "";
		this.scanValue = "";
		this.crudType = "";
		this.wbsNo = "";
		this.deviceId = "";
		this.itemCode = "";
		this.itemName = "";
		this.deviceStatusCode = "";
		this.deviceStatusName = "";
		this.operationSystemCode = "";
		this.itemNo = "";
		this.sloss = "";
		this.transNo = "";
		this.checkValue = "";
		this.checkOrgYn = "";
		this.orgId = "";
		this.orgName = "";
		this.productCode = "";
		this.productName = "";
		this.hequi = "";
		this.oldBarcode = "";
		this.failureCode = "";
		this.failureName = "";
		this.partnerCode = "";
		this.partnerName = "";
		this.failureRegNo = "";
		this.repairRequestNo = "";
		this.upgdoc= "";
		this.zkequi = "";
		this.zanln1 = "";
		this.urcodn = "";
		this.mncodn = "";
		this.comment = "";
		this.skostl = "";
		this.skostlTxt = "";
		this.cboPhotoUri = "";
		this.ansdt = "";
		this.zsetup = "";
		this.host_yn = "";
		this.usg_yn = "";
		this.hequnr = "";
		this.gwlen_o = "";
	}
	
	// 위치바코드 입력시만 사용.
	public BarcodeListInfo(String jobGubun, String partType, String locCd, String locName) {
		clear();
		this.jobGubun = jobGubun;
		this.partType = partType;
		this.partTypeName = "위치";
		this.barcode = locCd;
		this.barcodeName = locName;
		this.locCd = locCd;
	}		
	// 장치바코드 입력시만 사용.
	public BarcodeListInfo(String jobGubun, String partType, 
			String deviceId, String deviceName, String uFacCd, String itemCode, String itemName, String deviceStatusCode, String deviceStatusName, String locCd, String wbsNo, String operationSystemCode) {
		clear();
		this.jobGubun = jobGubun;
		this.partType = partType;
		this.partTypeName = "장치";
		this.barcode = deviceId;
		this.barcodeName = deviceName;
		this.uFacCd = uFacCd;
		this.serverUFacCd = uFacCd;
		this.deviceId = deviceId;
		this.itemCode = itemCode;
		this.itemName = itemName;
		this.deviceStatusCode = deviceStatusCode;
		this.deviceStatusName = deviceStatusName;
		this.locCd = locCd;
		this.wbsNo = wbsNo;
		this.operationSystemCode = operationSystemCode;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getJobGubun() {
		return jobGubun;
	}

	public void setJobGubun(String jobGubun) {
		this.jobGubun = jobGubun;
	}

	public String getPartTypeCode() {
		return partTypeCode;
	}

	public void setPartTypeCode(String partTypeCode) {
		this.partTypeCode = partTypeCode;
	}

	public String getPartType() {
		return partType;
	}

	public void setPartType(String partType) {
		this.partType = partType;
	}

	public String getPartTypeName() {
		return partTypeName;
	}

	public void setPartTypeName(String partTypeName) {
		this.partTypeName = partTypeName;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getBarcodeName() {
		return barcodeName;
	}

	public void setBarcodeName(String barcodeName) {
		this.barcodeName = barcodeName;
	}

	public String getuFacCd() {
		return uFacCd;
	}

	public void setuFacCd(String uFacCd) {
		this.uFacCd = uFacCd;
	}

	public String getuFacCdName() {
		return uFacCdName;
	}
	
	public void setuFacCdName(String uFacCdName) {
		this.uFacCdName = uFacCdName;
	}
	
	public String getLevel() {
		return level;
	}
	
	public void setLevel(String level) {
		this.level = level;
	}
	
	public String getFacSergeNum() {
		return facSergeNum;
	}
	
	public void setFacSergeNum(String facSergeNum){
		this.facSergeNum = facSergeNum;
	}

	public String getServerUFacCd() {
		return serverUFacCd;
	}

	public void setServerUFacCd(String serverUFacCd) {
		this.serverUFacCd = serverUFacCd;
	}

	public String getFacStatus() {
		return facStatus;
	}

	public void setFacStatus(String facStatus) {
		this.facStatus = facStatus;
	}

	public String getFacStatusName() {
		return facStatusName;
	}

	public void setFacStatusName(String facStatusName) {
		this.facStatusName = facStatusName;
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

	public String getDevType() {
		return devType;
	}

	public void setDevType(String devType) {
		this.devType = devType;
	}

	public String getDevTypeName() {
		return devTypeName;
	}

	public void setDevTypeName(String devTypeName) {
		this.devTypeName = devTypeName;
	}

	public String getScanValue() {
		return scanValue;
	}

	public void setScanValue(String scanValue) {
		this.scanValue = scanValue;
	}

	public String getWbsNo() {
		return wbsNo;
	}

	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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

	public String getOperationSystemCode() {
		return operationSystemCode;
	}

	public void setOperationSystemCode(String operationSystemCode) {
		this.operationSystemCode = operationSystemCode;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public String getSloss() {
		return sloss;
	}

	public void setSloss(String sloss) {
		this.sloss = sloss;
	}

	public String getTransNo() {
		return transNo;
	}

	public void setTransNo(String transNo) {
		this.transNo = transNo;
	}

	public String getCheckValue() {
		return checkValue;
	}

	public void setCheckValue(String checkValue) {
		this.checkValue = checkValue;
	}

	public String getCheckOrgYn() {
		return checkOrgYn;
	}

	public void setCheckOrgYn(String checkOrgYn) {
		this.checkOrgYn = checkOrgYn;
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

	public String getHequi() {
		return hequi;
	}

	public void setHequi(String hequi) {
		this.hequi = hequi;
	}

	public String getOldBarcode() {
		return oldBarcode;
	}

	public void setOldBarcode(String oldBarcode) {
		this.oldBarcode = oldBarcode;
	}

	public String getFailureCode() {
		return failureCode;
	}

	public void setFailureCode(String failureCode) {
		this.failureCode = failureCode;
	}

	public String getFailureName() {
		return failureName;
	}

	public void setFailureName(String failureName) {
		this.failureName = failureName;
	}

	public String getPartnerCode() {
		return partnerCode;
	}

	public void setPartnerCode(String partnerCode) {
		this.partnerCode = partnerCode;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public String getFailureRegNo() {
		return failureRegNo;
	}

	public void setFailureRegNo(String failureRegNo) {
		this.failureRegNo = failureRegNo;
	}

	public String getRepairRequestNo() {
		return repairRequestNo;
	}

	public void setRepairRequestNo(String repairRequestNo) {
		this.repairRequestNo = repairRequestNo;
	}

	public String getUpgdoc() {
		return upgdoc;
	}

	public void setUpgdoc(String upgdoc) {
		this.upgdoc = upgdoc;
	}

	public String getZkequi() {
		return zkequi;
	}

	public void setZkequi(String zkequi) {
		this.zkequi = zkequi;
	}

	public String getZanln1() {
		return zanln1;
	}

	public void setZanln1(String zanln1) {
		this.zanln1 = zanln1;
	}
	
	public String getAnsdt() {
		return ansdt;
	}

	public void setAnsdt(String ansdt) {
		this.ansdt = ansdt;
	}
	
	public String getZsetup() {
		return zsetup;
	}

	public void setZsetup(String zsetup) {
		this.zsetup = zsetup;
	}

	public String getUrcodn() {
		return urcodn;
	}

	public void setUrcodn(String urcodn) {
		this.urcodn = urcodn;
	}

	public String getMncodn() {
		return mncodn;
	}

	public void setMncodn(String mncodn) {
		this.mncodn = mncodn;
	}

	public String getRreason() {
		return rreason;
	}

	public void setRreason(String rreason) {
		this.rreason = rreason;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public IsmBarcodeInfo getIsmBarcodeInfo() {
		return ismBarcodeInfo;
	}

	public void setIsmBarcodeInfo(IsmBarcodeInfo ismBarcodeInfo) {
		this.ismBarcodeInfo = ismBarcodeInfo;
	}

	public String getO_data_c() {
		return o_data_c;
	}

	public void setO_data_c(String o_data_c) {
		this.o_data_c = o_data_c;
	}

	public String getSkostl() {
		return skostl;
	}

	public void setSkostl(String skostl) {
		this.skostl = skostl;
	}

	public String getSkostlTxt() {
		return skostlTxt;
	}

	public void setSkostlTxt(String skostlTxt) {
		this.skostlTxt = skostlTxt;
	}

	public String getCrudType() {
		return crudType;
	}

	public void setCrudType(String crudType) {
		this.crudType = crudType;
	}

	public String getCboPhotoUri() {
		return cboPhotoUri;
	}

	public void setCboPhotoUri(String cboPhotoUri) {
		this.cboPhotoUri = cboPhotoUri;
	}
	
	public String getHostYn() {
		return host_yn;
	}

	public void setHostYn(String host_yn) {
		this.host_yn = host_yn;
	}
	public String getUsgYn() {
		return usg_yn;
	}

	public void setUsgYn(String usg_yn) {
		this.usg_yn = usg_yn;
	}
	
	public void setHequnr(String hequnr){
		this.hequnr = hequnr;
	}
	
	public String getHequnr(){
		return hequnr;
	}
	
	public void setGwlen_o(String gwlen_o){
		this.gwlen_o = gwlen_o;
	}
	
	public String getGwlen_o(){
		return gwlen_o;
	}
}
