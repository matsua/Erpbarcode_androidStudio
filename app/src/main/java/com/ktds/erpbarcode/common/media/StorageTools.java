package com.ktds.erpbarcode.common.media;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class StorageTools {

	private static final String TAG = "StorageTools";
	
	private Activity mActivity;
	
	public StorageTools(Activity activity) {
		this.mActivity = activity;
	}
	
	public File getAlbumDirectory() {
		File storageDirectory = null;
		final String DIRECTORY_NAME = "erpbarcode";
		
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				storageDirectory = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
			} else {
				storageDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DIRECTORY_NAME);
			}
			
			if (storageDirectory != null) {
				if (! storageDirectory.mkdirs()) {
					if (! storageDirectory.exists()){
						Log.d(TAG, "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.d(TAG, "External storage is not mounted READ/WRITE.");
			return null;
		}

		return storageDirectory;
	}
	
	public Uri getImageFile() {
		File storageDirectory = getAlbumDirectory();
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile = new File(storageDirectory.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");

		return Uri.fromFile(mediaFile);
	}
	
	public String getMediaStoreImageUri(Uri imageUri) {
		String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor =  mActivity.getContentResolver().query(imageUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filepath = cursor.getString(column_index);
        if (cursor != null) cursor.close();

        return filepath;
	}
	
	public void galleryAddPicture(String imagePath) {
		File f = new File(imagePath);
	    Uri contentUri = Uri.fromFile(f);
		
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
	    mediaScanIntent.setData(contentUri);
	    mActivity.sendBroadcast(mediaScanIntent);
	}
	
	/**
	 * 로컬 이미지 파일 싸이즈를 BitmapFactory.decodeFile를 이용하여 줄인다.
	 * @param filePath
	 * @param limitPixel
	 * @return image file to resized bitmap
	 */
	public static Bitmap createImaegBitmap(String filePath, int limitPixel) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inTempStorage = new byte[16 * 1024];
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, limitPixel);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }
	
	/**
	 * 이미지 싸이지 비율을 계산한다.
	 * 
	 * @param width
	 * @param height
	 * @param limitPixel
	 * @return image size ratio
	 */
	public static int calculateInSampleSize(final int width, final int height, final int limitPixel) {
	    int inSampleSize = 1;

	    if (width > height) {
	    	inSampleSize = Math.round((float) width / (float) limitPixel);
	    } else {
	    	inSampleSize = Math.round((float) height / (float) limitPixel);
	    }
	    return inSampleSize;
	}
	
	/**
	 * 원본 이미지의 가로/세로 방향을 구한다. 
	 * 
	 * @param imagePath
	 * @return angle 90, 180, 270
	 */
	public static int getCameraOrientation(String imagePath) {
	    int rotate = 0;
	    int orientation = 0;

		try {
			ExifInterface exif = new ExifInterface(imagePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException e) {
			e.printStackTrace();
		}

        switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_270:
            rotate = 270;
            break;
        case ExifInterface.ORIENTATION_ROTATE_180:
            rotate = 180;
            break;
        case ExifInterface.ORIENTATION_ROTATE_90:
            rotate = 90;
            break;
        }

        Log.i("RotateImage", "Exif orientation: " + orientation);
        Log.i("RotateImage", "Rotate value: " + rotate);

	    return rotate;
	}
	
	/**
	 * 사용자 정의 각도로 Bitmap이미지를 변환한다.
	 * 
	 * @param bitmap
	 * @param degrees
	 * @return reRotated bitmap
	 */
	public static Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) {
		if ( degrees != 0 && bitmap != null ) {
			Matrix matrix = new Matrix();
			matrix.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2 );
			try {
				Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				if (bitmap != b2) {
					bitmap.recycle();
					bitmap = b2;
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}
}
