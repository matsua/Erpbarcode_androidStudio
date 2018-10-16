package com.ktds.erpbarcode.management.model;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.util.Log;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class SendHttpController {
	private static final String TAG = "SendHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public SendHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}

	public OutputParameter sendToServer(String serverPath, JSONArray paramList, JSONArray subParamList) throws ErpBarcodeException {
		choicePath(serverPath);
		
		InputParameter input = new InputParameter();
		input.setParamList(paramList);
		input.setSubParamList(subParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		return outputParameter;
	}
	
	public OutputParameter sendToServer(String serverPath, JSONArray paramList) throws ErpBarcodeException {
		choicePath(serverPath);
		
		InputParameter input = new InputParameter();
		if(serverPath.equals("Post_Ism_PrintStatusMod")){
			input.setParamList(paramList);
		}else{
			input.setNobodyParamList(paramList);
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		return outputParameter;
	}

	public void imageFileWrite(String barcode, String imagefilePath) throws ErpBarcodeException {
		Log.i(TAG, "imageFileWrite  Start...");
		choicePath(HttpAddressConfig.PATH_POST_OOSFILEUPLOAD);
		Log.i(TAG, "imageFileWrite  HttpAddressConfig.PATH_POST_OOSFULEUPLOAD");
		
		MultipartEntity params = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	    try {
	    	FileBody imagefile = new FileBody(new File(imagefilePath));
	    	
	    	params.addPart("file",  imagefile);
			params.addPart("prcid", new StringBody("0410") );
			params.addPart("barcode", new StringBody(barcode) );
		} catch (UnsupportedEncodingException e1) {
			throw new ErpBarcodeException(-1, "파라메터 변환중 오류가 발생했습니다. ");
		} catch (Exception e) {
			throw new ErpBarcodeException(-1, "이미지 파일 변환중 오류가 발생했습니다. ");
		}
		
		
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpclient.getParams().setParameter("http.protocol.expect-continue", false);
		httpclient.getParams().setParameter("http.connection.timeout", 10000);
		httpclient.getParams().setParameter("http.socket.timeout", 15000);
		Log.i(TAG, "imageFileWrite  CloseableHttpClient create instance");
		//String sessionId = "";
		//SessionUserData sessionUserData = SessionUserData.getInstance();
		//if (sessionUserData.isAuthenticated()) {
		//	sessionId = SessionUserData.getInstance().getSessionId();
		//}
		//Log.i(TAG, "imageFileWrite  sessionId==>"+sessionId);
		
		//CookieStore cookieStore = new BasicCookieStore();
		//HttpContext httpContext = new BasicHttpContext();
		//httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		//BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", sessionId);
		//cookie.setPath("/");
		//cookieStore.addCookie(cookie);
		//Log.i(TAG, "imageFileWrite  cookieStore init");
		
		ResponseHandler<byte[]> handler = new ResponseHandler<byte[]>() {
	        public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
	        	StatusLine statusLine = response.getStatusLine();
	    		if (statusLine.getStatusCode() == 200) {
	    			HttpEntity entity = response.getEntity();
		            if (entity != null) {
		                return EntityUtils.toByteArray(entity);
		            } else {
		                return null;
		            }	    			
	    		} else {
	    			throw new IOException("서버로 요청중 오류(IOException)가 발생했습니다. ");
	    		}
	        }
	    };
		
	    String jsonString = "";
		try {
			String url = mHttpAddress.getNotSecurityUrlAddress();
			Log.i(TAG, "imageFileWrite  url==>"+url);
		    HttpPost httppost = new HttpPost(url);
		    httppost.setEntity(params);
	

			byte[] response = httpclient.execute(httppost, handler);
			if (response==null) {
				throw new ErpBarcodeException(-1, "파일서버로 요청한 자료가 없습니다. ");
			}
			
			jsonString = EncodingUtils.getString(response, 0, response.length, "utf-8");
			Log.i(TAG, "imageFileWrite  jsonString==>"+jsonString);

		} catch (ClientProtocolException e) {
			Log.i(TAG, "imageFileWrite  ClientProtocolException==>"+e.getMessage());
			throw new ErpBarcodeException(-1, "서버로 요청중 오류(ClientProtocolException)가 발생했습니다. ");
		} catch (IOException e) {
			Log.i(TAG, "imageFileWrite  IOException==>"+e.getMessage());
			throw new ErpBarcodeException(-1, "서버로 요청중 오류(IOException)가 발생했습니다. ");
		}
	}
}
