package com.manateeworks;

import android.graphics.Rect;

public class BarcodeScanner
{
  public static final int FOUND_128 = 11;
  public static final int FOUND_39 = 2;
  public static final int FOUND_DM = 1;
  public static final int FOUND_EAN_13 = 7;
  public static final int FOUND_EAN_8 = 8;
  public static final int FOUND_NONE = 0;
  public static final int FOUND_PDF = 12;
  public static final int FOUND_QR = 13;
  public static final int FOUND_RSS_14 = 3;
  public static final int FOUND_RSS_14_STACK = 4;
  public static final int FOUND_RSS_EXP = 6;
  public static final int FOUND_RSS_LIM = 5;
  public static final int FOUND_UPC_A = 9;
  public static final int FOUND_UPC_E = 10;
  public static final int MWB_CFG_CODE128_VIN_MODE = 1;
  public static final int MWB_CFG_CODE39_REQUIRE_CHECKSUM = 2;
  public static final int MWB_CFG_CODE39_VIN_MODE = 1;
  public static final int MWB_CFG_DM_VIN_MODE = 1;
  public static final int MWB_CFG_GLOBAL_HORIZONTAL_SHARPENING = 1;
  public static final int MWB_CFG_GLOBAL_ROTATE90 = 4;
  public static final int MWB_CFG_GLOBAL_SHARPENING = 3;
  public static final int MWB_CFG_GLOBAL_VERTICAL_SHARPENING = 2;
  public static final int MWB_CODE_MASK_128 = 32;
  public static final int MWB_CODE_MASK_39 = 8;
  public static final int MWB_CODE_MASK_ALL = -1;
  public static final int MWB_CODE_MASK_DM = 2;
  public static final int MWB_CODE_MASK_EANUPC = 16;
  public static final int MWB_CODE_MASK_NONE = 0;
  public static final int MWB_CODE_MASK_PDF = 64;
  public static final int MWB_CODE_MASK_QR = 1;
  public static final int MWB_CODE_MASK_RSS = 4;
  public static final int MWB_RT_BAD_PARAM = -3;
  public static final int MWB_RT_FAIL = -1;
  public static final int MWB_RT_NOT_SUPPORTED = -2;
  public static final int MWB_RT_OK = 0;
  public static final int MWB_SCANDIRECTION_AUTODETECT = 8;
  public static final int MWB_SCANDIRECTION_HORIZONTAL = 1;
  public static final int MWB_SCANDIRECTION_OMNI = 4;
  public static final int MWB_SCANDIRECTION_VERTICAL = 2;
  public static final int MWB_SUBC_MASK_EANUPC_EAN_13 = 1;
  public static final int MWB_SUBC_MASK_EANUPC_EAN_8 = 2;
  public static final int MWB_SUBC_MASK_EANUPC_UPC_A = 4;
  public static final int MWB_SUBC_MASK_EANUPC_UPC_E = 8;
  public static final int MWB_SUBC_MASK_RSS_14 = 1;
  public static final int MWB_SUBC_MASK_RSS_EXP = 8;
  public static final int MWB_SUBC_MASK_RSS_LIM = 4;

  static
  {
    System.loadLibrary("BarcodeScannerLib");
  }

  public static native int MWBcleanupLib();

  public static native int MWBgetLastType();

  public static native int MWBgetLibVersion();

  public static native int MWBgetSupportedCodes();

  public static native int MWBregisterCode(int paramInt, String paramString1, String paramString2);

  public static native byte[] MWBscanGrayscaleImage(byte[] paramArrayOfByte, int paramInt1, int paramInt2);

  public static native int MWBsetActiveCodes(int paramInt);

  public static native int MWBsetActiveSubcodes(int paramInt1, int paramInt2);

  public static native int MWBsetDirection(int paramInt);

  public static native int MWBsetFlags(int paramInt1, int paramInt2);

  public static native int MWBsetLevel(int paramInt);

  public static native int MWBsetScanningRect(int paramInt, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4);

  public static int MWBsetScanningRect(int paramInt, Rect paramRect)
  {
    return MWBsetScanningRect(paramInt, paramRect.left, paramRect.top, paramRect.width() + paramRect.left, paramRect.height() + paramRect.top);
  }

  public static native int MWBvalidateVIN(byte[] paramArrayOfByte);
}

/* Location:           /home/hoo/Temp/
 * Qualified Name:     com.manateeworks.BarcodeScanner
 * JD-Core Version:    0.6.0
 */