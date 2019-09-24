/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * A Derivative Work, changed by Manatee Works, Inc.
 *
 */

package com.manateeworks.cameraDemo;

/**
 * This class provides the constants to use when sending an Intent to Barcode
 * Scanner. These strings are effectively API and cannot be changed.
 */
public final class Intents
{
    private Intents()
    {
    }

    public static final class Scan
    {
        /**
         * Send this intent to open the Barcodes app in scanning mode, find a
         * barcode, and return the results.
         */
        public static final String ACTION = "com.manateeworks.mobiscan39.SCAN";

        /**
         * By default, sending Scan.ACTION will decode all barcodes that we
         * understand. However it may be useful to limit scanning to certain
         * formats. Use Intent.putExtra(MODE, value) with one of the values
         * below ({@link #PRODUCT_MODE}, {@link #ONE_D_MODE},
         * {@link #QR_CODE_MODE}). Optional.
         * 
         * Setting this is effectively shorthnad for setting explicit formats
         * with {@link #SCAN_FORMATS}. It is overridden by that setting.
         */
        public static final String MODE = "SCAN_MODE";

        /**
         * If a barcode is found, Barcodes returns RESULT_OK to
         * onActivityResult() of the app which requested the scan via
         * startSubActivity(). The barcodes contents can be retrieved with
         * intent.getStringExtra(RESULT). If the user presses Back, the result
         * code will be RESULT_CANCELED.
         */
        public static final String RESULT = "SCAN_RESULT";

        /**
         * Call intent.getStringExtra(RESULT_FORMAT) to determine which barcode
         * format was found. See Contents.Format for possible values.
         */
        public static final String RESULT_FORMAT = "SCAN_RESULT_FORMAT";

        private Scan()
        {
        }
    }
}
