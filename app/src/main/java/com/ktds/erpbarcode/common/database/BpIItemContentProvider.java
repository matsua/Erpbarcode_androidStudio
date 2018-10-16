package com.ktds.erpbarcode.common.database;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class BpIItemContentProvider extends ContentProvider {
	
	private static final String TAG = "BpIItemContentProvider";

	private ErpBarcodeDatabaseHelper database;

	// Used for the UriMacher
	private static final int BPIITEMS = 10;
	private static final int BPIITEMS_FILTER_MATNR = 11;
	private static final int BPIITEMS_FILTER_MATNR_BISMT = 12;
	private static final int BPIITEM_ID = 20;

	private static final String AUTHORITY = "com.ktds.erpbarcode.contentprovider";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/bpiitems";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/bpiitem";
	

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, "bpiitems", BPIITEMS);
		sURIMatcher.addURI(AUTHORITY, "bpiitems/#", BPIITEM_ID);
		sURIMatcher.addURI(AUTHORITY, "bpiitems/matnr/*", BPIITEMS_FILTER_MATNR);
		sURIMatcher.addURI(AUTHORITY, "bpiitems/matnr/*/bismt/*", BPIITEMS_FILTER_MATNR_BISMT);
	}

	@Override
	public boolean onCreate() {
		database = new ErpBarcodeDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(BpIItemTable.TABLE_BP_I_ITEM);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case BPIITEMS:
			break;
		case BPIITEM_ID:
			queryBuilder.appendWhere(BpIItemTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;
		case BPIITEMS_FILTER_MATNR:
			queryBuilder.appendWhere(BpIItemTable.COLUMN_PRODUCT_CODE + " like " + uri.getLastPathSegment());
			break;
		case BPIITEMS_FILTER_MATNR_BISMT:
			List<String> segments =  uri.getPathSegments();
			for (String str : segments) {
				Log.i(TAG, "segments==>"+str);
			}
			queryBuilder.appendWhere(BpIItemTable.COLUMN_PRODUCT_CODE + " like " + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		
		int uriType = sURIMatcher.match(uri);
		long id = 0;
		switch (uriType) {
		case BPIITEMS:
			id = sqlDB.insert(BpIItemTable.TABLE_BP_I_ITEM, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse("bpiitems/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case BPIITEMS:
			rowsDeleted = sqlDB.delete(BpIItemTable.TABLE_BP_I_ITEM, selection, selectionArgs);
			break;
		case BPIITEM_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(BpIItemTable.TABLE_BP_I_ITEM,
						BpIItemTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(BpIItemTable.TABLE_BP_I_ITEM,
						BpIItemTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case BPIITEMS:
			rowsUpdated = sqlDB.update(BpIItemTable.TABLE_BP_I_ITEM, values, selection,
					selectionArgs);
			break;
		case BPIITEM_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(BpIItemTable.TABLE_BP_I_ITEM, values,
						BpIItemTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(BpIItemTable.TABLE_BP_I_ITEM, values,
						BpIItemTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { BpIItemTable.COLUMN_ID,
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

		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
