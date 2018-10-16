/*
 * Copyright (C) 2010 ZXing authors
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
 * A Derivative Work, changed by Manatee Works.
 *
 */

package com.manateeworks.cameraDemo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ktds.erpbarcode.R;
import com.manateeworks.BarcodeScanner;

final class DecodeHandler extends Handler
{

    private final ActivityCapture activity;

    DecodeHandler(ActivityCapture activity)
    {
        this.activity = activity;
    }

    @Override
  public void handleMessage(Message message) {
    switch (message.what) {
        case R.id.decode:
        //Log.d(TAG, "Got decode message");
            decode((byte[]) message.obj, message.arg1, message.arg2);
            break;
        case R.id.quit:
            Looper.myLooper().quit();
            break;
        }
    }
    
   
    /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
     * 
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
     */
  private void decode(byte[] data, int width, int height) {

	  //Check for barcode inside buffer
        byte[] res = BarcodeScanner.MWBscanGrayscaleImage(data, width,height);
       
        if (res != null)
        {
            String s = "";

            for (int i = 0; i < res.length; i++)
                s = s + (char) res[i];

            Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, res);
            message.sendToTarget();
        }
        else
        {
            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }

}
