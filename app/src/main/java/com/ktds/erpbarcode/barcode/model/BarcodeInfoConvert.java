package com.ktds.erpbarcode.barcode.model;

import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.common.ErpBarcodeException;

public class BarcodeInfoConvert {
	//private static final String TAG = "BarcodeInfoConvert";
	
	public static List<BarcodeListInfo> jsonArrayStringToBarcodeListInfos(String jsonArrayString) {

		Gson gson = new Gson();
		Type listType = new TypeToken<List<BarcodeListInfo>>(){}.getType();
		List<BarcodeListInfo> barcodeListInfos = gson.fromJson(jsonArrayString, listType);

		return barcodeListInfos;
	}
	
	public static String barcodeInfosToJsonArrayString(List<BarcodeListInfo> barcodeListInfos) {
		
		Gson gson = new Gson();
		String jsonArrayString = gson.toJson(barcodeListInfos);
		
		return jsonArrayString;
	}
	
	/**
	 * JSON형태를 자재정보(List<ProductInfo>)로 변환한다.
	 * 
	 * @param jsonArrayString
	 * @return
	 * @throws ErpBarcodeException
	 */
	public static List<ProductInfo> jsonArrayStringToProductInfos(String jsonArrayString) {

		Gson gson = new Gson();
		Type listType = new TypeToken<List<ProductInfo>>(){}.getType();
		List<ProductInfo> productInfos = gson.fromJson(jsonArrayString, listType);
		
		return productInfos;
	}
	
	/**
	 * 자재정보(List<ProductInfo>)를 JSON로  변환한다.
	 * 
	 * @param productInfos
	 * @return
	 * @throws ErpBarcodeException
	 */
	public static String productInfosToJsonArrayString(List<ProductInfo> productInfos) {
		
		Gson gson = new Gson();
		String jsonArrayString = gson.toJson(productInfos);
		
		return jsonArrayString;
	}
	
	public static BarcodeListInfo productInfoToBarcodeListInfo(ProductInfo productInfo) throws JSONException {

    	String partType = SuportLogic.getPartType(productInfo.getPartTypeCode(), productInfo.getDevType());
    	String partTypeName = SuportLogic.getNodeStringType(partType);
    	String devTypeName = SuportLogic.getDevTypeName(productInfo.getDevType());      // 품목구분코드명

		BarcodeListInfo barcodeInfo = new BarcodeListInfo();
		barcodeInfo.setJobGubun(GlobalData.getInstance().getJobGubun());
		barcodeInfo.setPartTypeCode(productInfo.getPartTypeCode());
		barcodeInfo.setPartType(partType );
		barcodeInfo.setPartTypeName(partTypeName);
		barcodeInfo.setBarcode(productInfo.getProductCode() );
		barcodeInfo.setBarcodeName(productInfo.getProductName() );
		barcodeInfo.setLocCd("");
		barcodeInfo.setuFacCd("");
		barcodeInfo.setuFacCdName("");
		barcodeInfo.setServerUFacCd("");
		barcodeInfo.setFacStatus("");
		barcodeInfo.setFacStatusName("");
		if (GlobalData.getInstance().getJobGubun().contains("현장점검")) {
			barcodeInfo.setFacStatusName("설비마스터없음");
		}
		barcodeInfo.setDevType(productInfo.getDevType() );
		barcodeInfo.setDevTypeName(devTypeName );
		barcodeInfo.setDeviceId("" );
		barcodeInfo.setScanValue("0");
		barcodeInfo.setCheckValue("");
		barcodeInfo.setWbsNo("");
		barcodeInfo.setCheckOrgYn("N");
		barcodeInfo.setOrgId("");
		barcodeInfo.setOrgName("");
		barcodeInfo.setItemNo("");
		barcodeInfo.setSloss("");
		barcodeInfo.setTransNo("");
		barcodeInfo.setProductCode(productInfo.getProductCode());
		barcodeInfo.setProductName(productInfo.getProductName());

		return barcodeInfo;
	}
	
	public static ProductInfo jsonToProductInfo(int _id, JSONObject jsonobj) throws JSONException {
		ProductInfo productInfo = new ProductInfo();

		int MATERIALSEQ = _id;
		String MATNR = jsonobj.getString("MATNR").replace("null", "");
		String MAKTX = jsonobj.getString("MAKTX").replace("null", "");
		String ZMATGB = jsonobj.getString("ZMATGB").replace("null", "");
		String BISMT = jsonobj.getString("BISMT").replace("null", "");
		String EQSHAPE = jsonobj.getString("EQSHAPE").replace("null", "");
		String COMPTYPE = jsonobj.getString("COMPTYPE").replace("null", "");
		String ZZOLDBARCDIND = "";
		String ZZOLDBARMATL = "";
		String ZZNEWBARCDIND = "";
		String ZZNEWBARMATL = "";
		String ZEMAFT = "";
		String ZEMAFT_NAME = jsonobj.getString("ZEMAFT_NAME").replace("null", "");
		String ZEFAMATNR = "";
		String EXTWG = jsonobj.getString("EXTWG").replace("null", "");
		String STATUS = jsonobj.getString("STATUS").replace("null", "");
		String EAI_CDATE = "";
		String ZZMATN = jsonobj.getString("ZZMATN").replace("null", "");
		String MTART = jsonobj.getString("MTART").replace("null", "");
		String BARCD = jsonobj.getString("BARCD").replace("null", "");
		String ITEMCLASSIFICATIONNAME = jsonobj.getString("ITEMCLASSIFICATIONNAME").replace("null", "");
		
		productInfo.set_id(MATERIALSEQ);
		productInfo.setProductCode(MATNR);
		productInfo.setProductName(MAKTX);
		productInfo.setDevType(ZMATGB);
		productInfo.setBismt(BISMT);
		productInfo.setEqshape(EQSHAPE);
		productInfo.setPartTypeCode(COMPTYPE);
		productInfo.setZzoldbarcdind(ZZOLDBARCDIND);
		productInfo.setZzoldbarmatl(ZZOLDBARMATL);
		productInfo.setZznewbarcdind(ZZNEWBARCDIND);
		productInfo.setZznewbarmatl(ZZNEWBARMATL);
		productInfo.setZemaft(ZEMAFT);
		productInfo.setZemaft_name(ZEMAFT_NAME);
		productInfo.setZefamatnr(ZEFAMATNR);
		productInfo.setExtwg(EXTWG);
		productInfo.setStatus(STATUS);
		productInfo.setEai_cdate(EAI_CDATE);
		productInfo.setZzmatn(ZZMATN);
		productInfo.setMtart(MTART);
		productInfo.setBarcd(BARCD);
		productInfo.setItemClassificationName(ITEMCLASSIFICATIONNAME);

		return productInfo;
	}
	
	// SAP 설비 정보 조회
	public static BarcodeListInfo jsonToBarcodeListInfo(JSONObject jsonobj) throws JSONException {

		String level = "";
		if(!jsonobj.isNull("LEVEL")){
			level = jsonobj.getString("LEVEL");						//설비레벨
		}
		
		String barcode = jsonobj.getString("EQUNR");
		String barcodeName = jsonobj.getString("EQKTX");
		String deviceId = jsonobj.getString("ZEQUIPGC");               // 장치바코드
		String uFacCd = jsonobj.getString("HEQUNR");                   // 상위바코드
		String uFacCdName = "";
		if(!jsonobj.isNull("HEQKTX")){
			uFacCdName = jsonobj.getString("HEQKTX");               // 상위바코드명
		}
		String devType = jsonobj.getString("ZPGUBUN");                 // 품목구분코드
		String devTypeName = SuportLogic.getDevTypeName(devType);      // 품목구분코드명
		String facStatus = jsonobj.getString("ZPSTATU");               // 바코드상태코드
		String FACStatusName = jsonobj.getString("ZDESC");           	// 바코드상태명
        String orgId = jsonobj.getString("ZKOSTL");                    // 부서코드(운용)
        String orgName = jsonobj.getString("ZKTEXT");                  // 부서명(운용)
		String locCd = jsonobj.getString("ZEQUIPLP");                  // 위치코드
		String productCode = jsonobj.getString("SUBMT");               // 자재코드
		String productName = jsonobj.getString("MAKTX");               // 자재명
		String hequnr = jsonobj.getString("HEQUNR");
		
        String zkequi = jsonobj.getString("ZKEQUI");        			// 설비처리구분
        String zanln1 = jsonobj.getString("ZANLN1");  		 			// 자산번호
        
		String partTypeCode = jsonobj.getString("ZPPART");             // 부품종류코드
		String partType = SuportLogic.getPartType(partTypeCode, devType);
		String partTypeName = SuportLogic.getNodeStringType(partType);
		
		String wbsNo = jsonobj.getString("ZPS_PNR");           		   // 시설등록 - wbsNO
		String o_data_c = jsonobj.getString("O_DATA_C");           	   // 인계/인수/시설등록 대상 여부 조회
		
		String serge = "";
		if(!jsonobj.isNull("SERGE")){
			serge = jsonobj.getString("SERGE");              // 바코드 sn	//null
		}
		
		String ansdt = "";
		if(!jsonobj.isNull("ANSDT")){
			ansdt = jsonobj.getString("ANSDT");              // 취득일		//null
		}
		
		String zsetup = "";
		if(!jsonobj.isNull("ZSETUP")){
			zsetup = jsonobj.getString("ZSETUP");              // 공사비 	//null
		}
		
		String gwlen_o = "";
		if(!jsonobj.isNull("GWLEN_O")){
			gwlen_o = jsonobj.getString("GWLEN_O");			//보증종료일 //null
		}
		
		// 자료보정
		if (ansdt.equals("0000-00-00")) ansdt = "";
		if (zsetup.equals("")||zsetup.equals("0.00")) zsetup="0";
		
		BarcodeListInfo barcodeInfo = new BarcodeListInfo();
		barcodeInfo.setJobGubun(GlobalData.getInstance().getJobGubun());
		barcodeInfo.setPartTypeCode(partTypeCode);
		barcodeInfo.setPartType(partType);
		barcodeInfo.setPartTypeName(partTypeName);
		barcodeInfo.setBarcode(barcode);
		barcodeInfo.setBarcodeName(barcodeName );
		barcodeInfo.setLocCd(locCd );
		barcodeInfo.setuFacCd( uFacCd );
		barcodeInfo.setuFacCdName(uFacCdName);
		barcodeInfo.setServerUFacCd(uFacCd);
		barcodeInfo.setLevel(level);
		barcodeInfo.setFacStatus( facStatus );
		barcodeInfo.setFacStatusName( FACStatusName );
		barcodeInfo.setDevType( devType );
		barcodeInfo.setDevTypeName( devTypeName );
		barcodeInfo.setDeviceId( deviceId );
		barcodeInfo.setScanValue("0");
		barcodeInfo.setCheckOrgYn("N");
		barcodeInfo.setOrgId(orgId);
		barcodeInfo.setOrgName(orgName);
		barcodeInfo.setProductCode(productCode);
		barcodeInfo.setProductName(productName);
		barcodeInfo.setFacSergeNum(serge);
		barcodeInfo.setHequnr(hequnr);

		barcodeInfo.setZkequi(zkequi);
		barcodeInfo.setZanln1(zanln1);
		
		barcodeInfo.setAnsdt(ansdt);
		barcodeInfo.setZsetup(zsetup);

		barcodeInfo.setWbsNo(wbsNo);
		barcodeInfo.setO_data_c(o_data_c);
		barcodeInfo.setGwlen_o(gwlen_o);
		
		if(!jsonobj.isNull("HOST_YN")){
			String host_yn = jsonobj.getString("HOST_YN");
			String usg_yn = jsonobj.getString("USG_YN");
			barcodeInfo.setHostYn(host_yn);
			barcodeInfo.setUsgYn(usg_yn);
		}
		
		return barcodeInfo;
	}
	
	public static BarcodeListInfo jsonToTransferBarcodeListInfo(JSONObject jsonobj) throws JSONException {

		String barcode = jsonobj.getString("UNITID");
		String barcodeName = jsonobj.getString("EQKTX");
		String uFacCd = jsonobj.getString("SHELFID");                  // 상위바코드
		String uFacCdName = "";                                        // 상위바코드명
		String devType = jsonobj.getString("DEVTYPE");                 // 품목구분코드
		String devTypeName = SuportLogic.getDevTypeName(devType);      // 품목구분코드명
		String facStatus = jsonobj.getString("ZPSTATU");                 // 바코드상태코드
		String facStatusName = SuportLogic.getFACStatusName(facStatus);  // 바코드상태코드명
        String orgId = "";                                             // 부서코드(운용)
        String orgName = "";                                           // 부서명(운용)
		String locCd = jsonobj.getString("LOCCODE");                   // 위치코드
		String wbsNo = jsonobj.getString("POSID");                     // WBS번호
		String deviceId = jsonobj.getString("DEVICEID");               // 장치ID
		String productCode = "";                                       // 자재코드
		String productName = "";                                       // 자재명
		String hequi = jsonobj.getString("HEQUI");                     // ??


		String partTypeCode = jsonobj.getString("PARTTYPE");           // 부품종류코드
		String partType = SuportLogic.getPartType(partTypeCode, devType);
		String partTypeName = SuportLogic.getNodeStringType(partType);
		
		
		BarcodeListInfo barcodeInfo = new BarcodeListInfo();
		barcodeInfo.setJobGubun(GlobalData.getInstance().getJobGubun());
		barcodeInfo.setPartTypeCode(partTypeCode);
		barcodeInfo.setPartType( partType );
		barcodeInfo.setPartTypeName(partTypeName);
		barcodeInfo.setBarcode( barcode );
		barcodeInfo.setBarcodeName( barcodeName );
		barcodeInfo.setLocCd( locCd );
		barcodeInfo.setuFacCd( uFacCd );
		barcodeInfo.setuFacCdName(uFacCdName);
		barcodeInfo.setServerUFacCd(uFacCd);
		barcodeInfo.setFacStatus( facStatus );
		barcodeInfo.setFacStatusName( facStatusName );
		barcodeInfo.setDevType( devType );
		barcodeInfo.setDevTypeName( devTypeName );
		barcodeInfo.setDeviceId( deviceId );
		barcodeInfo.setScanValue("0");
		barcodeInfo.setWbsNo(wbsNo);
		barcodeInfo.setCheckOrgYn("N");
		barcodeInfo.setOrgId(orgId);
		barcodeInfo.setOrgName(orgName);
		barcodeInfo.setProductCode(productCode);
		barcodeInfo.setProductName(productName);
		barcodeInfo.setHequi(hequi);

		return barcodeInfo;
	}
	
	public static BarcodeListInfo jsonToRapairBarcodeListInfo(JSONObject jsonobj) throws JSONException {

		String barcode = jsonobj.getString("BARCODE");            // 설비바코드
		String productCode = jsonobj.getString("SUBMT");          // 자재코드
		String productName = jsonobj.getString("EQKTX");          // 자재명
		
		String devType = jsonobj.getString("ZPGUBUN");            // 품목구분
		String devTypeName = SuportLogic.getDevTypeName(devType);

		String facStatus = jsonobj.getString("ZPSTATU");          // 설비상태코드
		String facStatusName = SuportLogic.getFACStatusName(facStatus);
		String uFacCd = jsonobj.getString("EXBARCODE");           // 상위바코드
		String oldBarcode = "";                                   // 구바코드
		String locCd = jsonobj.getString("ZEQUIPLP");             // 위치바코드
		String locName = jsonobj.getString("ZEQUIPLPT");          // 위치명
		String failureCode = jsonobj.getString("FECOD");          // 고장코드
		String failureName = jsonobj.getString("FECODN");         // 고장명
		String partnerCode = jsonobj.getString("LIFNR");          // 협력사코드
		String partnerName = jsonobj.getString("LIFNRN");         // 협력사명
		String failureRegNo = jsonobj.getString("REGNO");         // 고장등록번호
		String repairRequestNo = jsonobj.getString("QMNUM");      // 수리의뢰번호
		String orgId = jsonobj.getString("ZKOSTL");               // 운영부서코드
		String orgName = jsonobj.getString("ZKTEXT");             // 운영부서명

		String partTypeCode = jsonobj.getString("ZPPART");        // 부품종류
		String partType = SuportLogic.getPartType(partTypeCode, devType);
		String partTypeName = SuportLogic.getNodeStringType(partType);

		
		BarcodeListInfo barcodeInfo = new BarcodeListInfo();
		barcodeInfo.setJobGubun(GlobalData.getInstance().getJobGubun());
		barcodeInfo.setPartTypeCode(partTypeCode);
		barcodeInfo.setPartType( partType );
		barcodeInfo.setPartTypeName(partTypeName);
		barcodeInfo.setBarcode( barcode );
		barcodeInfo.setLocCd( locCd );
		barcodeInfo.setLocName(locName);
		barcodeInfo.setuFacCd( uFacCd );
		barcodeInfo.setServerUFacCd(uFacCd);
		barcodeInfo.setFacStatus( facStatus );
		barcodeInfo.setFacStatusName( facStatusName );
		barcodeInfo.setDevType( devType );
		barcodeInfo.setDevTypeName( devTypeName );
		barcodeInfo.setCheckOrgYn("N");
		barcodeInfo.setOrgId(orgId);
		barcodeInfo.setOrgName(orgName);
		barcodeInfo.setProductCode(productCode);
		barcodeInfo.setProductName(productName);
		barcodeInfo.setOldBarcode(oldBarcode);
		barcodeInfo.setFailureCode(failureCode);
		barcodeInfo.setFailureName(failureName);
		barcodeInfo.setPartnerCode(partnerCode);
		barcodeInfo.setPartnerName(partnerName);
		barcodeInfo.setFailureRegNo(failureRegNo);
		barcodeInfo.setRepairRequestNo(repairRequestNo);

		return barcodeInfo;
	}
	
	public static BarcodeListInfo jsonToRapairReceiptBarcodeListInfo(JSONObject jsonobj) throws JSONException {
        String barcode = jsonobj.getString("BARCODE");            // 설비바코드
        String barcodeName = jsonobj.getString("EQKTX");            // 설비바코드

		String devType = jsonobj.getString("ZPGUBUN");            // 품목구분
		String devTypeName = SuportLogic.getDevTypeName(devType);

		String partTypeCode = jsonobj.getString("ZPPART");        // 부품종류
		String partType = SuportLogic.getPartType(partTypeCode, devType);
		String partTypeName = SuportLogic.getNodeStringType(partType);

        String URCODN = jsonobj.getString("URCODN");			// 원인유형
        String MNCODN = jsonobj.getString("MNCODN");			// 수리유형

        String locCd = jsonobj.getString("ZEQUIPLP");             // 위치바코드
		String locName = jsonobj.getString("ZEQUIPLPT");          // 위치명

		String repairRequestNo = jsonobj.getString("QMNUM");      // 수리의뢰번호
		String failureCode = jsonobj.getString("FECOD");          // 고장코드

		String facStatus = jsonobj.getString("ZPSTATU");          // 설비상태코드
		String facStatusName = SuportLogic.getFACStatusName(facStatus);
		
		String uFacCd = jsonobj.getString("EXBARCODE");          // 기존훼손바코드(바코드 대체시 활용)
		String RREASON = jsonobj.getString("RREASON");           // 대체사유

		BarcodeListInfo barcodeInfo = new BarcodeListInfo();
		barcodeInfo.setJobGubun(GlobalData.getInstance().getJobGubun());
		barcodeInfo.setBarcode( barcode );
		barcodeInfo.setBarcodeName( barcodeName );
		barcodeInfo.setProductName( barcodeName );				// 자재명을 사용한다... 설비명 대신
		barcodeInfo.setPartTypeCode(partTypeCode);
		barcodeInfo.setPartType( partType );
		barcodeInfo.setPartTypeName(partTypeName);
		barcodeInfo.setUrcodn(URCODN);
		barcodeInfo.setMncodn(MNCODN);
		barcodeInfo.setLocCd( locCd );
		barcodeInfo.setLocName(locName);
		barcodeInfo.setDevType( devType );
		barcodeInfo.setDevTypeName( devTypeName );
		barcodeInfo.setCheckOrgYn("N");
		barcodeInfo.setuFacCd(URCODN);
		barcodeInfo.setRepairRequestNo(repairRequestNo);
		barcodeInfo.setFailureCode(failureCode);
		barcodeInfo.setFacStatus( facStatus );
		barcodeInfo.setFacStatusName( facStatusName );
		barcodeInfo.setuFacCd( uFacCd );
		barcodeInfo.setServerUFacCd(uFacCd);
		barcodeInfo.setRreason(RREASON);

		return barcodeInfo;
	}
	
	public static AssetClassInfo jsonToAssetClassInfo(String productCode, JSONObject jsonobj) throws JSONException {
		String assetLargeCode = jsonobj.getString("assetLargeClassificationCode").trim();
		String assetMiddleCode = jsonobj.getString("assetMiddleClassificationCode").trim();
		String assetSmallCode = jsonobj.getString("assetSmallClassificationCode").trim();
		String assetDetailCode = jsonobj.getString("assetDetailClassificationCode").trim();
		String assetLargeName = jsonobj.getString("assetLargeClassificationName").trim();
		String assetMiddleName = jsonobj.getString("assetMiddleClassificationName").trim();
		String assetSmallName = jsonobj.getString("assetSmallClassificationName").trim();
		String assetDetailName = jsonobj.getString("assetDetailClassificationName").trim();
		
		AssetClassInfo assetClassInfo = new AssetClassInfo();
		assetClassInfo.setProductCode(productCode);
		assetClassInfo.setAssetLargeCode(assetLargeCode);
		assetClassInfo.setAssetMiddleCode(assetMiddleCode);
		assetClassInfo.setAssetSmallCode(assetSmallCode);
		assetClassInfo.setAssetDetailCode(assetDetailCode);
		assetClassInfo.setAssetLargeName(assetLargeName);
		assetClassInfo.setAssetMiddleName(assetMiddleName);
		assetClassInfo.setAssetSmallName(assetSmallName);
		assetClassInfo.setAssetDetailName(assetDetailName);

		return assetClassInfo;
	}
}
