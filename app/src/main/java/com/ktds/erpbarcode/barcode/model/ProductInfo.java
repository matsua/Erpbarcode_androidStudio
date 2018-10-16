package com.ktds.erpbarcode.barcode.model;

import java.io.Serializable;

public class ProductInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int _id;               //                일련번호    (MATERIALSEQ)
	private String productCode;    // matnr          자재코드
	private String productName;    // maktx          자재명
	private String devType;        // zmatgb         품명구분
	private String bismt;          //                기존 자재 코드
	private String eqshape;        //                장비타입
	private String partTypeCode;   // comptype       부품종류
	private String zzoldbarcdind;  //                구바코드구분지시자
	private String zzoldbarmatl;   //                구분류바코드
	private String zznewbarcdind;  //                신바코드구분지시자
	private String zznewbarmatl;   //                신분류바코드
	private String zemaft;         //                제조사코드
	private String zemaft_name;    //                제조사코드명칭
	private String zefamatnr;      //                제조사자재
	private String extwg;          //                소싱그룹
	private String status;         //                상태
	private String eai_cdate;      //                전송요청일자
	private String zzmatn;         //                대체자재코드
	private String mtart;          //                자재유형
	private String barcd;          //                바코드 대상 여부
	private String itemClassificationName;  // 자재정보 서버에서 조회시만 사용.
	private AssetClassInfo assetClassInfo;

	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
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
	public String getBismt() {
		return bismt;
	}
	public void setBismt(String bismt) {
		this.bismt = bismt;
	}
	public String getEqshape() {
		return eqshape;
	}
	public void setEqshape(String eqshape) {
		this.eqshape = eqshape;
	}
	public String getPartTypeCode() {
		return partTypeCode;
	}
	public void setPartTypeCode(String partTypeCode) {
		this.partTypeCode = partTypeCode;
	}
	public String getZzoldbarcdind() {
		return zzoldbarcdind;
	}
	public void setZzoldbarcdind(String zzoldbarcdind) {
		this.zzoldbarcdind = zzoldbarcdind;
	}
	public String getZzoldbarmatl() {
		return zzoldbarmatl;
	}
	public void setZzoldbarmatl(String zzoldbarmatl) {
		this.zzoldbarmatl = zzoldbarmatl;
	}
	public String getZznewbarcdind() {
		return zznewbarcdind;
	}
	public void setZznewbarcdind(String zznewbarcdind) {
		this.zznewbarcdind = zznewbarcdind;
	}
	public String getZznewbarmatl() {
		return zznewbarmatl;
	}
	public void setZznewbarmatl(String zznewbarmatl) {
		this.zznewbarmatl = zznewbarmatl;
	}
	public String getZemaft() {
		return zemaft;
	}
	public void setZemaft(String zemaft) {
		this.zemaft = zemaft;
	}
	public String getZemaft_name() {
		return zemaft_name;
	}
	public void setZemaft_name(String zemaft_name) {
		this.zemaft_name = zemaft_name;
	}
	public String getZefamatnr() {
		return zefamatnr;
	}
	public void setZefamatnr(String zefamatnr) {
		this.zefamatnr = zefamatnr;
	}
	public String getExtwg() {
		return extwg;
	}
	public void setExtwg(String extwg) {
		this.extwg = extwg;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getEai_cdate() {
		return eai_cdate;
	}
	public void setEai_cdate(String eai_cdate) {
		this.eai_cdate = eai_cdate;
	}
	public String getZzmatn() {
		return zzmatn;
	}
	public void setZzmatn(String zzmatn) {
		this.zzmatn = zzmatn;
	}
	public String getMtart() {
		return mtart;
	}
	public void setMtart(String mtart) {
		this.mtart = mtart;
	}
	public String getBarcd() {
		return barcd;
	}
	public void setBarcd(String barcd) {
		this.barcd = barcd;
	}
	public String getItemClassificationName() {
		return itemClassificationName;
	}
	public void setItemClassificationName(String itemClassificationName) {
		this.itemClassificationName = itemClassificationName;
	}
	public AssetClassInfo getAssetClassInfo() {
		return assetClassInfo;
	}
	public void setAssetClassInfo(AssetClassInfo assetClassInfo) {
		this.assetClassInfo = assetClassInfo;
	}
}
