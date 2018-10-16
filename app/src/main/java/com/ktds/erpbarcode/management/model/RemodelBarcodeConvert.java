package com.ktds.erpbarcode.management.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.ktds.erpbarcode.GlobalData;

public class RemodelBarcodeConvert {
	//private static final String TAG = "RemodelBarcodeConvert";
	
	public static RemodelBarcodeInfo jsonToRemodelBarcodeInfo(JSONObject jsonobj) throws JSONException {

		String matnr = "";
		String maktx = "";
		String UPGDOC = "";
		
		if (GlobalData.getInstance().getJobGubun().equals("개조개량의뢰")) {
			matnr = jsonobj.getString("MATNR");          // 자재코드
			maktx = jsonobj.getString("MAKTX");          // 자재명
			UPGDOC = jsonobj.getString("UPGDOC");        // 지시서번호
		} else if (GlobalData.getInstance().getJobGubun().equals("개조개량완료")) {
			matnr = jsonobj.getString("EQUNR");          // 개조개량바코드번호
			UPGDOC = jsonobj.getString("UPGEQUNR");      // 개조개량신규바코드번호
		}

		RemodelBarcodeInfo remodelBarcodeInfo = new RemodelBarcodeInfo();
		remodelBarcodeInfo.setProductCode( matnr );
		remodelBarcodeInfo.setProductName( maktx );
		remodelBarcodeInfo.setUpgdoc( UPGDOC );

		return remodelBarcodeInfo;
	}
}
