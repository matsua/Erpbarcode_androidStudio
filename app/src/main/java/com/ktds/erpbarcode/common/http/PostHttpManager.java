package com.ktds.erpbarcode.common.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.ErpBarcodeException;

public class PostHttpManager {
	
	private static final String TAG = "PostHttpManager";
	
	private HttpURLConnection mHttpConnection;
	private final String mUrl;
	private final String mParameter;
	
	public PostHttpManager(String url, String parameter) throws ErpBarcodeException  {
		Log.i(TAG, "url==>"+url.toString());
		mUrl = url;
		mParameter = parameter;
		ConnectionInitialization();
	}
	
	private void ConnectionInitialization() throws ErpBarcodeException {
		try {
			mHttpConnection = (HttpURLConnection) urlGeneration().openConnection();

	        // 전송모드 설정(일반적인 POST방식)

			mHttpConnection.setDefaultUseCaches(false);
			mHttpConnection.setDoInput(true);
			mHttpConnection.setDoOutput(true);
			mHttpConnection.setRequestMethod("POST");
			mHttpConnection.setConnectTimeout(10000);

			String sessionId = "";
			SessionUserData sessionUserData = SessionUserData.getInstance();
			if (sessionUserData.isAuthenticated()) {
				sessionId = SessionUserData.getInstance().getSessionId();
			}
			
	        // content-type 설정
			mHttpConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			mHttpConnection.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);

		} catch (Exception e) {
			Log.i(TAG, "##wcf##   PostHttpManager  Exception==>"+e.getMessage());
			throw new ErpBarcodeException(-1, "PostHttpManager 인스턴스 생성중 오류가 발생했습니다. ");
		}
	}

	private URL urlGeneration() throws ErpBarcodeException {
		URL url = null;
		try {
			url = new URL(mUrl);
		} catch (MalformedURLException e) {
			throw new ErpBarcodeException(-1, "URL 생성중 오류(MalformedURLException)가 발생했습니다. ");
		}
		return url;
	}
	
	public String postSend() throws ErpBarcodeException {

		String gzipString;
		
		try {
	        // 서버로 전송
	        OutputStreamWriter outStream = new OutputStreamWriter(mHttpConnection.getOutputStream(), "UTF-8");
	        PrintWriter writer = new PrintWriter(outStream);
	        writer.write(mParameter);
	        writer.flush();

	        // 전송 결과값 받기
	        InputStreamReader inputStream = new InputStreamReader(mHttpConnection.getInputStream(), "UTF-8");
			BufferedReader bufferReader = new BufferedReader(inputStream);
	        StringBuilder builder = new StringBuilder();
	        String str;
	        while((str = bufferReader.readLine()) != null){
	            builder.append(str + "\n");
	        }
	        
	        
	        mHttpConnection.disconnect();
	        bufferReader.close();
	        inputStream.close();

	        gzipString = builder.toString();
		} catch (IOException e) {
			Log.i(TAG, "Http 서버로 서비스 요청중 오류(IO)가 발행했습니다. "+e.getMessage());
			throw new ErpBarcodeException(-1, "네트워크 상태를 확인하세요.");
		} catch (Exception e) {
			Log.i(TAG, "Http 서버로 서비스 요청중 오류가 발행했습니다. "+e.getMessage());
			throw new ErpBarcodeException(-1, "Http 서버로 서비스 요청중 오류가 발행했습니다. ");
		}

		return gzipString;
	}

}
