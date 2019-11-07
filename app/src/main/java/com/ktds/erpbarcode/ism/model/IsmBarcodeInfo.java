package com.ktds.erpbarcode.ism.model;

public class IsmBarcodeInfo {
	private boolean isChecked;
	private String newBarcode;              // 시뉴바코드       (newBarcode)
	private String itemCode;                // 시뉴바코드       (itemCode)
	private String itemName;                // 시뉴바코드       (itemName)
	private String itemCategoryCode;
	private String itemCategoryName;
	private String partKindCode;
	private String partKindName;	
	private String itemLargeClassificationCode;
	private String itemMiddleClassificationCode;
	private String itemSmallClassificationCode;
	private String itemDetailClassificationCode;
	private String injuryBarcode;
	private String publicationWhyCode;
	private String publicationWhyName;
	private String supplierCode;
	private String deptCode;
	private String operationDeptCode;
	private String makerCode;
	private String makerSerial;
	private String makerNational;
	private String obtainDay;
	private String generationRequestSeq;	//채번요청일련번호
	private String facCd;                   // 설비바코드       (FEQUNR)
	private String facStatus;               // 설비상태         (ZPSTATU)
	private String facStatusName;           // 설비상태명       (ZPSTATU)
	private String productCode;             // 자재코드         (SUBMT)
	private String productName;             // 자재명          (MAKTX)
	private String devType;                 // 품목구분        (ZPGUBUN)
	private String devTypeName;             // 품목구분명       (ZPGUBUN)
	private String partTypeCode;            // 품목구분코드     (ZPPART)
	private String partType;                // 부품종류       	U, S, R - getPartTypeName(ZPPART, ZPGUBUN)
	private String partTypeName;            // 부품종류명       Unit, Shelf, Rack - getPartTypeName(ZPPART, ZPGUBUN)
	private String locCd;                   // 위치바코드       (ZEQUIPLP)
	private String locName;                 // 위치명          (PLOCNAME)
	private String assetClass;              // 자산분류        (ZEQART1 / ZEQART2 / ZEQART3 / ZEQART4)
	private String assetClassName;          // 자산분류(대/중/소/세)  (ZATEXT02 / ZATEXT03 / ZATEXT04)
	private String manufacturerName;        // 제조사명        (ZCODENAME / HERST)
	private String manufacturerSN;          // 제조사S/N       (SERGE)
	private String deviceId;                // 장치바코드       (ZEQUIPGC)
	private String useStopYn;               // 사용중지여부
	private String replaceProductCode;      // 대체자재코드
	private String existingProductCode ;    // 기존요청상태
	private String costCenter;              // 코스트센터       (ZKOSTL)
	private String makerItemYn;				// 제조사 물자 여부
	private String facilityCategory;
	
	private String transactionDate;				//처리일
	private String transactionTime; 			//처리시간
	private String transactionStatusName; 		//진행상태 
	private String transactionStatusCode; 		//진행상태 코드
	private String itemClassificationName; 		//자재분류(대/중/소/세) 
	private String transactionUserName;			//처리자 
	private String transactionUserId;			//처리자id
	private String orgName;						//운용조직
	private String generationRequestDetailSeq; 	//채번요청상세일련번호
	private String oldBarcodeYN; 				//구바코드여부
	private String conditions;					//장치 - 표준서비스코드상태 (20: 종료진행, 30: 종료)
	
	/*
	 * 위치바코드 추가 내역 
	 */
	
	private String locLevel;		//레벨
	private String silYuhung;		//유형 
	private String silType;			//실유형 
	private String silName;			//실명칭 
	private String silStatus;		//상태 
	private String silGubun;		//실층구분 
	private String silAddress;		//주소 
	private String silBulGb;		//건물군여부 
	private String silBuType;		//건물유형 
	private String silKTBulType;	//KT건물유형 
	private String silKuksa;		//관리국사 
	private String silKuksaName;	//관리국사명 
	private String comment;			//설명 
	private int position;			//position

	// sesang 20190910 대표위치 정보 추가
	private String repLocCd;                 // 대표주소코드
	private String repLocNm;                 // 대표위치명
	private String locationCode;                 // 대표위치명
	private String locationName;                 // 대표위치명
	private String nmsDeviceId;                 // 대표위치명
	private String deviceName;                 // 대표위치명
	private String wbsCode;
	
	// end sesang

	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	public String getNewBarcode() {
		return newBarcode;
	}
	public void setNewBarcode(String newBarcode) {
		this.newBarcode = newBarcode;
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
	
	public String getItemCategoryCode() {
		return itemCategoryCode;
	}
	public void setItemCategoryCode(String itemCategoryCode) {
		this.itemCategoryCode = itemCategoryCode;
	}
	public String getItemCategoryName() {
		return itemCategoryName;
	}
	public void setItemCategoryName(String itemCategoryName) {
		this.itemCategoryName = itemCategoryName;
	}
	public String getPartKindCode() {
		return partKindCode;
	}
	public void setPartKindCode(String partKindCode) {
		this.partKindCode = partKindCode;
	}
	public String getPartKindName() {
		return partKindName;
	}
	public void setPartKindName(String partKindName) {
		this.partKindName = partKindName;
	}
	public String getItemLargeClassificationCode() {
		return itemLargeClassificationCode;
	}
	public void setItemLargeClassificationCode(String itemLargeClassificationCode) {
		this.itemLargeClassificationCode = itemLargeClassificationCode;
	}
	public String getItemMiddleClassificationCode() {
		return itemMiddleClassificationCode;
	}
	public void setItemMiddleClassificationCode(String itemMiddleClassificationCode) {
		this.itemMiddleClassificationCode = itemMiddleClassificationCode;
	}
	public String getItemSmallClassificationCode() {
		return itemSmallClassificationCode;
	}
	public void setItemSmallClassificationCode(String itemSmallClassificationCode) {
		this.itemSmallClassificationCode = itemSmallClassificationCode;
	}
	public String getItemDetailClassificationCode() {
		return itemDetailClassificationCode;
	}
	public void setItemDetailClassificationCode(String itemDetailClassificationCode) {
		this.itemDetailClassificationCode = itemDetailClassificationCode;
	}
	public String getInjuryBarcode() {
		return injuryBarcode;
	}
	public void setInjuryBarcode(String injuryBarcode) {
		this.injuryBarcode = injuryBarcode;
	}
	public String getPublicationWhyCode() {
		return publicationWhyCode;
	}
	public void setPublicationWhyCode(String publicationWhyCode) {
		this.publicationWhyCode = publicationWhyCode;
	}
	public String getPublicationWhyName() {
		return publicationWhyName;
	}
	public void setPublicationWhyName(String publicationWhyName) {
		this.publicationWhyName = publicationWhyName;
	}
	public String getSupplierCode() {
		return supplierCode;
	}
	public void setSupplierCode(String supplierCode) {
		this.supplierCode = supplierCode;
	}
	public String getDeptCode() {
		return deptCode;
	}
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	public String getOperationDeptCode() {
		return operationDeptCode;
	}
	public void setOperationDeptCode(String operationDeptCode) {
		this.operationDeptCode = operationDeptCode;
	}
	public String getMakerCode() {
		return makerCode;
	}
	public void setMakerCode(String makerCode) {
		this.makerCode = makerCode;
	}
	public String getMakerSerial() {
		return makerSerial;
	}
	public void setMakerSerial(String makerSerial) {
		this.makerSerial = makerSerial;
	}
	public String getMakerNational() {
		return makerNational;
	}
	public void setMakerNational(String makerNational) {
		this.makerNational = makerNational;
	}
	public String getObtainDay() {
		return obtainDay;
	}
	public void setObtainDay(String obtainDay) {
		this.obtainDay = obtainDay;
	}
	public String getGenerationRequestSeq() {
		return generationRequestSeq;
	}
	public void setGenerationRequestSeq(String generationRequestSeq) {
		this.generationRequestSeq = generationRequestSeq;
	}
	public String getFacCd() {
		return facCd;
	}
	public void setFacCd(String facCd) {
		this.facCd = facCd;
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
	public String getAssetClass() {
		return assetClass;
	}
	public void setAssetClass(String assetClass) {
		this.assetClass = assetClass;
	}
	public String getAssetClassName() {
		return assetClassName;
	}
	public void setAssetClassName(String assetClassName) {
		this.assetClassName = assetClassName;
	}
	public String getManufacturerName() {
		return manufacturerName;
	}
	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}
	public String getManufacturerSN() {
		return manufacturerSN;
	}
	public void setManufacturerSN(String manufacturerSN) {
		this.manufacturerSN = manufacturerSN;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getUseStopYn() {
		return useStopYn;
	}
	public void setUseStopYn(String useStopYn) {
		this.useStopYn = useStopYn;
	}
	public String getReplaceProductCode() {
		return replaceProductCode;
	}
	public void setReplaceProductCode(String replaceProductCode) {
		this.replaceProductCode = replaceProductCode;
	}
	public String getExistingProductCode() {
		return existingProductCode;
	}
	public void setExistingProductCode(String existingProductCode) {
		this.existingProductCode = existingProductCode;
	}
	public String getCostCenter() {
		return costCenter;
	}
	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}
	public String getMakerItemYn() {
		return makerItemYn;
	}
	public void setMakerItemYn(String makerItemYn) {
		this.makerItemYn = makerItemYn;
	}
	
	public String getFacilityCategory() {
		return facilityCategory;
	}
	
	public void setFacilityCategory(String facilityCategory) {
		this.facilityCategory = facilityCategory;
	}
	public String getPartTypeName() {
		return partTypeName;
	}
	public void setPartTypeName(String partTypeName) {
		this.partTypeName = partTypeName;
	}
	
	public String getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}
	public String getTransactionTime() {
		return transactionTime;
	}
	public void setTransactionTime(String transactionTime) {
		this.transactionTime = transactionTime;
	}
	public String getTransactionStatusName() {
		return transactionStatusName;
	}
	public void setTransactionStatusName(String transactionStatusName) {
		this.transactionStatusName = transactionStatusName;
	}
	public String getItemClassificationName() {
		return itemClassificationName;
	}
	public void setItemClassificationName(String itemClassificationName) {
		this.itemClassificationName = itemClassificationName;
	}
	public String getTransactionUserName() {
		return transactionUserName;
	}
	public void setTransactionUserName(String transactionUserName) {
		this.transactionUserName = transactionUserName;
	}
	public String getTransactionUserId() {
		return transactionUserId;
	}
	public void setTransactionUserId(String transactionUserId) {
		this.transactionUserId = transactionUserId;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getGenerationRequestDetailSeq() {
		return generationRequestDetailSeq;
	}
	public void setGenerationRequestDetailSeq(String generationRequestDetailSeq) {
		this.generationRequestDetailSeq = generationRequestDetailSeq;
	}
	public String getTransactionStatusCode() {
		return transactionStatusCode;
	}
	public void setTransactionStatusCode(String transactionStatusCode) {
		this.transactionStatusCode = transactionStatusCode;
	}
	public String getOldBarcodeYN(){
		return oldBarcodeYN;
	}
	public void setOldBarcodeYN(String oldBarcodeYN){
		this.oldBarcodeYN = oldBarcodeYN;
	}
	
	public String geLocLevel(){
		return locLevel;
	}
	public void setLocLevel(String locLevel){
		this.locLevel = locLevel;
	}
	public String getSilYuhung(){
		return silYuhung;
	}
	public void setSilYuhung(String silYuhung){
		this.silYuhung = silYuhung;
	}
	public String getSilType(){
		return silType;
	}
	public void setSilType(String silType){
		this.silType = silType;
	}
	public String getSilName(){
		return silName;
	}
	public void setSilName(String silName){
		this.silName = silName;
	}
	public String getSilStatus(){
		return silStatus;
	}
	public void setSilStatus(String silStatus){
		this.silStatus = silStatus;
	}
	public String getSilGubun(){
		return silGubun;
	}
	public void setSilGubun(String silGubun){
		this.silGubun = silGubun;
	}
	public String getSilAddress(){
		return silAddress;
	}
	public void setSilAddress(String silAddress){
		this.silAddress = silAddress;
	}
	public String getSilBulGb(){
		return silBulGb;
	}
	public void setSilBulGb(String silBulGb){
		this.silBulGb = silBulGb;
	}
	public String getSilBuType(){
		return silBuType;
	}
	public void setSilBuType(String silBuType){
		this.silBuType = silBuType;
	}
	public String getSilKTBulType(){
		return silKTBulType;
	}
	public void setSilKTBulType(String silKTBulType){
		this.silKTBulType = silKTBulType;
	}
	public String getSilKuksa(){
		return silKuksa;
	}
	public void setSilKuksa(String silKuksa){
		this.silKuksa = silKuksa;
	}
	public String getSilKuksaName(){
		return silKuksaName;
	}
	public void setSilKuksaName(String silKuksaName){
		this.silKuksaName = silKuksaName;
	}
	public String getComment(){
		return comment;
	}
	public void setComment(String comment){
		this.comment = comment;
	}
	public int getPosition(){
		return position;
	}
	public void setPosition(int position){
		this.position = position;
	}
	public String getConditions(){
		return conditions;
	}
	public void setConditions(String conditions){
		this.conditions = conditions;
	}

	// sesang 20190910 대표위치 정보 추가
	public String getLocationCode() { return locationCode;}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getLocationName() { return locationName; }
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getNmsDeviceId() { return nmsDeviceId; }
	public void setNmsDeviceId(String nmsDeviceId) {
		this.nmsDeviceId = nmsDeviceId;
	}
	public String getDeviceName() { return deviceName; }
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
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
	public String getWbsCode() {
		return wbsCode;
	}
	public void setWbsCode(String wbsCode) {
		this.wbsCode = wbsCode;
	}
	// end sesang

}
