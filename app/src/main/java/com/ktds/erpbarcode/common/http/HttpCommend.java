package com.ktds.erpbarcode.common.http;

import android.util.Log;

import com.ktds.erpbarcode.common.ErpBarcodeException;

public class HttpCommend {
	private static final String TAG = "HttpCommend";
	
	private HttpAddressConfig mHttpAddress;
	private InputParameter mInputParameter;
	
	public HttpCommend(HttpAddressConfig httpAddress, InputParameter input) throws ErpBarcodeException {
		if (input==null) {
			Log.i(TAG, "인스턴스 생성중 오류가 발생했습니다.");
			throw new ErpBarcodeException(-1, "인스턴스 생성중 오류가 발생했습니다. ");
		}
		mInputParameter = input;
		mHttpAddress = httpAddress;
	}
	
	public OutputParameter httpSend() throws ErpBarcodeException {
		String url = mHttpAddress.getUrlAddress();
		String postData = mInputParameter.encodeEncryptString();
		Log.i(TAG, "<<<<< http input >>>>>   postData==>"+postData);

		PostHttpManager postHttpManager = new PostHttpManager(url, postData);
		String gzipString = postHttpManager.postSend();
		
		//Log.i(TAG, "<<<<< http result >>>>>   gzipString==>"+gzipString);
		OutputParameter outputParameter = new OutputParameter(gzipString);
		return outputParameter;
	}

}
