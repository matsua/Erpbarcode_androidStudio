package com.ktds.erpbarcode.common.database;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ktds.erpbarcode.barcode.model.ProductInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;

public class BpIItemQuery {
	private static final String TAG = "BpIItemQuery";
	

	private SQLiteDatabase database;
	private ErpBarcodeDatabaseHelper dbHelper;
	private String[] allColumns = { 
			BpIItemTable.COLUMN_ID,
			BpIItemTable.COLUMN_PRODUCT_CODE, 
			BpIItemTable.COLUMN_PRODUCT_NAME,
			BpIItemTable.COLUMN_DEVTYPE,
			BpIItemTable.COLUMN_BISMT,
			BpIItemTable.COLUMN_EQSHAPE,
			BpIItemTable.COLUMN_PARTTYPECODE,
			BpIItemTable.COLUMN_ZZOLDBARCDIND,
			BpIItemTable.COLUMN_ZZOLDBARMATL,
			BpIItemTable.COLUMN_ZZNEWBARCDIND,
			BpIItemTable.COLUMN_ZZNEWBARMATL,
			BpIItemTable.COLUMN_ZEMAFT,
			BpIItemTable.COLUMN_ZEMAFT_NAME,
			BpIItemTable.COLUMN_ZEFAMATNR,
			BpIItemTable.COLUMN_EXTWG,
			BpIItemTable.COLUMN_STATUS,
			BpIItemTable.COLUMN_EAI_CDATE,
			BpIItemTable.COLUMN_ZZMATN,
			BpIItemTable.COLUMN_MTART,
			BpIItemTable.COLUMN_BARCD };

	public BpIItemQuery(Context context) {
		dbHelper = new ErpBarcodeDatabaseHelper(context);
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
	    dbHelper.close();
	}
	
	public int totalCount() {
		Cursor tottalcursor = database.query(BpIItemTable.TABLE_BP_I_ITEM,
				allColumns, null, null, null, null, null);
		Log.i(TAG, "getProductInfosByMatnrAndBismt     tottalcursor.getCount()==>"+tottalcursor.getCount());
		int count = tottalcursor.getCount();
		tottalcursor.close();
		return count;
	}
	
    public List<ProductInfo> getProductInfosByBarcode(String barcode) throws ErpBarcodeException {
    	Log.i(TAG, "search Start...");
    	String matnr = "";
    	String bismt = "";
    	int nine_idx = barcode.indexOf("9");
    	Log.i(TAG, "search 1...");
    	if (barcode.length() == 16 && !barcode.startsWith("K")) {
            // 신바코드인데 16자리면서 K9으로 시작하지 않으면 무조건 앞에서 8자리가 자재코드
            // FS16023000000002:처리할 수 없는 설비바코드입니다.
    		matnr = barcode.substring(0,8);
    		
    	// 무선단말/기계장치(길이 18자리) 이면 BISMT 10자리로 조회 한다. 
    	// 서버의 BP_ITEM의 LGCY_ITEM_CD = 단말의 BISMT ex : 001500300102120016
        } else if (barcode.length() == 18) {
            bismt = barcode.substring(0, 10);
            
        // 유선단말(길이 17자리) 이면 MATNR 10자리로 조회 한다. 서버의 BP_ITEM의 LGCY_ITEM_CD = 단말의 BISMT
        } else if (barcode.length() == 17) {
            if (barcode.substring(4, 1).equals("9")) {
            	matnr = "K" + barcode.substring(4,7);
            } else {
                // 다섯번째 숫자가 9가 아니면 무조건 앞에서 8자리가 자재코드
            	matnr = barcode.substring(0, 8);
            }
        } else if (barcode.length() >= 16 && barcode.length() <= 18 && nine_idx > 0) {
            // 장치 바코드 때문에 길이 10자리 이상으로 국한 시킴
        	matnr = "K" + barcode.substring(nine_idx, 7);
        }
    	
    	if (matnr.isEmpty() && bismt.isEmpty()) {
    		throw new ErpBarcodeException(-1, "물풀코드나 기존자재코드를 입력하세요. ");
    	}
    	
    	Log.i(TAG, "search 2...");
    	return getProductInfosByMatnrAndBismt(matnr, bismt);
    }
	
	public List<ProductInfo> getProductInfosByMatnrAndBismt(String matnr, String bismt) {
		List<ProductInfo> productInfos = new ArrayList<ProductInfo>();

		Log.i(TAG, "getProductInfosByMatnrAndBismt     matnr==>"+matnr);
		Log.i(TAG, "getProductInfosByMatnrAndBismt     bismt==>"+bismt);
		
		String selection = "";
		if (!matnr.isEmpty()) {
			selection = BpIItemTable.COLUMN_PRODUCT_CODE + " like '" + matnr + "%' ";
		}
		if (!bismt.isEmpty()) {
			if (selection.isEmpty()) {
				selection += BpIItemTable.COLUMN_BISMT + " like '%" + bismt + "%'";
			} else {
				selection += "and " + BpIItemTable.COLUMN_BISMT + " like '%" + bismt + "%'";
			}
		}
		if (selection.isEmpty()) {
			return null;
		}

		Cursor cursor = database.query(BpIItemTable.TABLE_BP_I_ITEM,
				allColumns, selection, null, null, null, null);

		Log.i(TAG, "getProductInfosByMatnrAndBismt     Count==>"+cursor.getCount());
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ProductInfo productInfo = cursorToProductInfo(cursor);
			productInfos.add(productInfo);
			cursor.moveToNext();
		}
		cursor.close();
		
		return productInfos;
	}
	
	public ProductInfo createProductInfo(ProductInfo productInfo) {
		
		ContentValues values = new ContentValues();
	    values.put(BpIItemTable.COLUMN_ID, Integer.valueOf(productInfo.get_id()));
	    values.put(BpIItemTable.COLUMN_PRODUCT_CODE, productInfo.getProductCode());
	    values.put(BpIItemTable.COLUMN_PRODUCT_NAME, productInfo.getProductName());
	    values.put(BpIItemTable.COLUMN_DEVTYPE, productInfo.getDevType());
	    values.put(BpIItemTable.COLUMN_BISMT, productInfo.getBismt());
	    values.put(BpIItemTable.COLUMN_EQSHAPE, productInfo.getEqshape());
	    values.put(BpIItemTable.COLUMN_PARTTYPECODE, productInfo.getPartTypeCode());
	    values.put(BpIItemTable.COLUMN_ZZOLDBARCDIND, productInfo.getZzoldbarcdind());
	    values.put(BpIItemTable.COLUMN_ZZOLDBARMATL, productInfo.getZzoldbarmatl());
	    values.put(BpIItemTable.COLUMN_ZZNEWBARCDIND, productInfo.getZznewbarcdind());
	    values.put(BpIItemTable.COLUMN_ZZNEWBARMATL, productInfo.getZznewbarmatl());
	    values.put(BpIItemTable.COLUMN_ZEMAFT, productInfo.getZemaft());
	    values.put(BpIItemTable.COLUMN_ZEMAFT_NAME, productInfo.getZemaft_name());
	    values.put(BpIItemTable.COLUMN_ZEFAMATNR, productInfo.getZefamatnr());
	    values.put(BpIItemTable.COLUMN_EXTWG, productInfo.getExtwg());
	    values.put(BpIItemTable.COLUMN_STATUS, productInfo.getStatus());
	    values.put(BpIItemTable.COLUMN_EAI_CDATE, productInfo.getEai_cdate());
	    values.put(BpIItemTable.COLUMN_ZZMATN, productInfo.getZzmatn());
	    values.put(BpIItemTable.COLUMN_MTART, productInfo.getMtart());
	    values.put(BpIItemTable.COLUMN_BARCD, productInfo.getBarcd());
	    
	    long insertId = database.insert(BpIItemTable.TABLE_BP_I_ITEM, null, values);
	    Cursor cursor = database.query(BpIItemTable.TABLE_BP_I_ITEM,
	        allColumns, BpIItemTable.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    ProductInfo newProductInfo = cursorToProductInfo(cursor);
	    cursor.close();
	    return newProductInfo;
	}
	
	public void createBaseProductInfo(ProductInfo productInfo) {
		
		ContentValues values = new ContentValues();
	    values.put(BpIItemTable.COLUMN_ID, Integer.valueOf(productInfo.get_id()));
	    values.put(BpIItemTable.COLUMN_PRODUCT_CODE, productInfo.getProductCode());
	    values.put(BpIItemTable.COLUMN_PRODUCT_NAME, productInfo.getProductName());
	    values.put(BpIItemTable.COLUMN_DEVTYPE, productInfo.getDevType());
	    values.put(BpIItemTable.COLUMN_BISMT, productInfo.getBismt());
	    values.put(BpIItemTable.COLUMN_EQSHAPE, productInfo.getEqshape());
	    values.put(BpIItemTable.COLUMN_PARTTYPECODE, productInfo.getPartTypeCode());
	    values.put(BpIItemTable.COLUMN_ZZOLDBARCDIND, productInfo.getZzoldbarcdind());
	    values.put(BpIItemTable.COLUMN_ZZOLDBARMATL, productInfo.getZzoldbarmatl());
	    values.put(BpIItemTable.COLUMN_ZZNEWBARCDIND, productInfo.getZznewbarcdind());
	    values.put(BpIItemTable.COLUMN_ZZNEWBARMATL, productInfo.getZznewbarmatl());
	    values.put(BpIItemTable.COLUMN_ZEMAFT, productInfo.getZemaft());
	    values.put(BpIItemTable.COLUMN_ZEMAFT_NAME, productInfo.getZemaft_name());
	    values.put(BpIItemTable.COLUMN_ZEFAMATNR, productInfo.getZefamatnr());
	    values.put(BpIItemTable.COLUMN_EXTWG, productInfo.getExtwg());
	    values.put(BpIItemTable.COLUMN_STATUS, productInfo.getStatus());
	    values.put(BpIItemTable.COLUMN_EAI_CDATE, productInfo.getEai_cdate());
	    values.put(BpIItemTable.COLUMN_ZZMATN, productInfo.getZzmatn());
	    values.put(BpIItemTable.COLUMN_MTART, productInfo.getMtart());
	    values.put(BpIItemTable.COLUMN_BARCD, productInfo.getBarcd());

	    database.insert(BpIItemTable.TABLE_BP_I_ITEM, null, values);
	}
	
	public void createBulkProductInfos(JSONArray jsonProductInfos) throws ErpBarcodeException {
		
		database.beginTransaction();
		for (int i=0;i<jsonProductInfos.length();i++) {
			try {
				JSONObject jsonobj = jsonProductInfos.getJSONObject(i);

				ProductInfo productInfo = new ProductInfo();
				productInfo.set_id(Integer.valueOf(jsonobj.getString("MATERIALSEQ").replace("null", "")));
				productInfo.setProductCode(jsonobj.getString("MATNR").replace("null", ""));
				productInfo.setProductName(jsonobj.getString("MAKTX").replace("null", ""));
				productInfo.setDevType(jsonobj.getString("ZMATGB").replace("null", ""));
				productInfo.setBismt(jsonobj.getString("BISMT").replace("null", ""));
				productInfo.setEqshape(jsonobj.getString("EQSHAPE").replace("null", ""));
				productInfo.setPartTypeCode(jsonobj.getString("COMPTYPE").replace("null", ""));
				productInfo.setZzoldbarcdind(jsonobj.getString("ZZOLDBARCDIND").replace("null", ""));
				productInfo.setZzoldbarmatl(jsonobj.getString("ZZOLDBARMATL").replace("null", ""));
				productInfo.setZznewbarcdind(jsonobj.getString("ZZNEWBARCDIND").replace("null", ""));
				productInfo.setZznewbarmatl(jsonobj.getString("ZZNEWBARMATL").replace("null", ""));
				productInfo.setZemaft(jsonobj.getString("ZEMAFT").replace("null", ""));
				productInfo.setZemaft_name(jsonobj.getString("ZEMAFT_NAME").replace("null", ""));
				productInfo.setZefamatnr(jsonobj.getString("ZEFAMATNR").replace("null", ""));
				productInfo.setExtwg(jsonobj.getString("EXTWG").replace("null", ""));
				productInfo.setStatus(jsonobj.getString("STATUS").replace("null", ""));
				productInfo.setEai_cdate(jsonobj.getString("EAI_CDATE").replace("null", ""));
				productInfo.setZzmatn(jsonobj.getString("ZZMATN").replace("null", ""));
				productInfo.setMtart(jsonobj.getString("MTART").replace("null", ""));
				productInfo.setBarcd(jsonobj.getString("BARCD").replace("null", ""));
				
				createBaseProductInfo(productInfo);
			} catch (JSONException e) {
				Log.i(TAG, "자재마스터 업데이트중 JSONException==>"+e.getMessage());
				database.endTransaction();
				throw new ErpBarcodeException(-1, "자재마스터 업데이트중 JSONException==>"+e.getMessage());
			}
		}
		database.setTransactionSuccessful();
		database.endTransaction();
	}
	
	public void deleteProductInfo(int _id) {
		Log.i(TAG, "deleteProductInfo     id==>"+_id);
	    database.delete(BpIItemTable.TABLE_BP_I_ITEM, BpIItemTable.COLUMN_ID + " = " + _id, null);
	}
	
	public void deleteAllProductInfo() {
		Log.i(TAG, "deleteAllProductInfo     Start...");
	    database.delete(BpIItemTable.TABLE_BP_I_ITEM, null, null);
	}
	
	private ProductInfo cursorToProductInfo(Cursor cursor) {
		ProductInfo productInfo = new ProductInfo();
		
		productInfo.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ID)) );
		productInfo.setProductCode(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_PRODUCT_CODE)) );
		productInfo.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_PRODUCT_NAME)) );
		productInfo.setDevType(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_DEVTYPE)) );
		productInfo.setBismt(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_BISMT)) );
		productInfo.setEqshape(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_EQSHAPE)) );
		productInfo.setPartTypeCode(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_PARTTYPECODE)) );
		productInfo.setZzoldbarcdind(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZZOLDBARCDIND)) );
		productInfo.setZzoldbarmatl(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZZOLDBARMATL)) );
		productInfo.setZznewbarcdind(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZZNEWBARCDIND)) );
		productInfo.setZznewbarmatl(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZZNEWBARMATL)) );
		productInfo.setZemaft(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZEMAFT)) );
		productInfo.setZemaft_name(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZEMAFT_NAME)) );
		productInfo.setZefamatnr(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZEFAMATNR)) );
		productInfo.setExtwg(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_EXTWG)) );
		productInfo.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_STATUS)) );
		productInfo.setEai_cdate(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_EAI_CDATE)) );
		productInfo.setZzmatn(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_ZZMATN)) );
		productInfo.setMtart(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_MTART)) );
		productInfo.setBarcd(cursor.getString(cursor.getColumnIndexOrThrow(BpIItemTable.COLUMN_BARCD)) );

		return productInfo;
	}
}
