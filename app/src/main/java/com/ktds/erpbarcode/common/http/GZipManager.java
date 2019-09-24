package com.ktds.erpbarcode.common.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.encryption.Base64Encoder;

public class GZipManager {
	private static final String TAG = "GZipManager";
	
	private final String mMessage;
    
	public GZipManager(String message) throws ErpBarcodeException {
		if (message.isEmpty()) {
			Log.i(TAG, "인스턴스 생성중 오류가 발생했습니다.");
			throw new ErpBarcodeException(-1, "인스턴스 생성중 오류가 발생했습니다. ");
		}
		mMessage = message;
	}
	
	public String compress() throws ErpBarcodeException {
		try {
	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
	        GZIPOutputStream gout = new GZIPOutputStream(bout);
	        gout.write(mMessage.getBytes());
	        gout.close();
	        byte[] buffer = bout.toByteArray();
	        String base64String = Base64Encoder.encode(buffer);
	        bout.close();
	        return base64String;
		} catch (Exception e) {
			Log.i(TAG, "GZIP 압축중 오류가 발생했습니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "GZIP 압축중 오류가 발생했습니다. ");
		}
	}
	
    public String deCompress() throws ErpBarcodeException {

    	System.gc();
    	Runtime.getRuntime().gc();
    	String gzipString;
		try {
			byte[] compressed = Base64Encoder.decode(mMessage);
    	
	    	ByteArrayInputStream bin = new ByteArrayInputStream(compressed);
	    	GZIPInputStream gzipInputStream = new GZIPInputStream(bin);
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	
	    	byte[] buffer = new byte[8192];
	    	
	    	int length;
	    	while ((length = gzipInputStream.read(buffer))>0) {
	    		baos.write(buffer, 0, length);
	    	}
	    	gzipInputStream.close();
	    	bin.close();
	    	baos.close();
	    	
	    	compressed = null;
	    	buffer = null;
	    		    	
	    	byte[] byteArray = baos.toByteArray();
	
	        gzipString = new String(byteArray, "UTF-8");
		} catch (Exception e) {
			Log.i(TAG, "GZIP 압축 해제중 오류가 발생했습니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "GZIP 압축 해제중 오류가 발생했습니다. ");
		}
		
    	System.gc();
    	Runtime.getRuntime().gc();
        
        return gzipString;
    }
}
