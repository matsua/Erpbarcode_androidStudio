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

package com.manateeworks.cameraDemo.camera;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.view.SurfaceHolder;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 */
public final class CameraManager
{

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 160;
    private static final int MAX_FRAME_WIDTH = 2048;
    private static final int MAX_FRAME_HEIGHT = 480;

    private static CameraManager cameraManager;

    static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
    static {
      int sdkInt;
      try {
        sdkInt = Integer.parseInt(Build.VERSION.SDK);
      } catch (NumberFormatException nfe) {
        // Just to be safe
        sdkInt = 10000;
      }
      SDK_INT = sdkInt;
    }

    private final Context context;
    private final CameraConfigurationManager configManager;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private final boolean useOneShotPreviewCallback;
    /**
     * Preview frames are delivered here, which we pass on to the registered
     * handler. Make sure to clear the handler so it will only receive one
     * message.
     */
    private final PreviewCallback previewCallback;
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which
     * requested them.
     */
    private final AutoFocusCallback autoFocusCallback;

    /**
     * Initializes this static object with the Context of the calling Activity.
     * 
     * @param context
     *            The Activity which wants to use the camera.
     */
    public static void init(Context context)
    {
        if (cameraManager == null)
        {
            cameraManager = new CameraManager(context);
        }
    }

    /**
     * Gets the CameraManager singleton instance.
     * 
     * @return A reference to the CameraManager singleton.
     */
    public static CameraManager get()
    {
        return cameraManager;
    }

    private CameraManager(Context context)
    {

        this.context = context;
        this.configManager = new CameraConfigurationManager(context);

        // Camera.setOneShotPreviewCallback() has a race condition in Cupcake,
        // so we use the older
        // Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later,
        // we need to use
        // the more efficient one shot callback, as the older one can swamp the
        // system and cause it
        // to run out of memory. We can't use SDK_INT because it was introduced
        // in the Donut SDK.
        useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.CUPCAKE;

        previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);
        autoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     * 
     * @param holder
     *            The surface object which the camera will draw preview frames
     *            into.
     * @throws IOException
     *             Indicates the camera driver failed to open.
     */
    public void openDriver(SurfaceHolder holder) throws IOException
    {
        if (camera == null)
        {
            camera = Camera.open();
            if (camera == null)
            {
                throw new IOException();
            }
            camera.setPreviewDisplay(holder);

            if (!initialized)
            {
                initialized = true;
                configManager.initFromCameraParameters(camera);
            }
            configManager.setDesiredCameraParameters(camera);
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver()
    {
        if (camera != null)
        {
            camera.release();
            camera = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public void startPreview()
    {
        if (camera != null && !previewing)
        {
            camera.startPreview();
            previewing = true;
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public void stopPreview()
    {
        if (camera != null && previewing)
        {
            if (!useOneShotPreviewCallback)
            {
                camera.setPreviewCallback(null);
            }
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data
     * will arrive as byte[] in the message.obj field, with width and height
     * encoded as message.arg1 and message.arg2, respectively.
     * 
     * @param handler
     *            The handler to send the message to.
     * @param message
     *            The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message)
    {
        if (camera != null && previewing)
        {
            previewCallback.setHandler(handler, message);
            if (useOneShotPreviewCallback)
            {
                camera.setOneShotPreviewCallback(previewCallback);
            }
            else
            {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     * 
     * @param handler
     *            The Handler to notify when the autofocus completes.
     * @param message
     *            The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message)
    {
        if (camera != null && previewing)
        {
            autoFocusCallback.setHandler(handler, message);
            // Log.d(TAG, "Requesting auto-focus callback");
            camera.autoFocus(autoFocusCallback);
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user
     * where to place the barcode. This target helps with alignment as well as
     * forces the user to hold the device far enough away to ensure the image
     * will be in focus.
     * 
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect()
    {
        Point screenResolution = configManager.getScreenResolution();
        if (framingRect == null)
        {
            if (camera == null)
            {
                return null;
            }
            int width = (int) (screenResolution.x * 10/10);
            if (width < MIN_FRAME_WIDTH)
            {
                width = MIN_FRAME_WIDTH;
            }
            else
                if (width > MAX_FRAME_WIDTH)
                {
                    width = MAX_FRAME_WIDTH;
                }
            int height = screenResolution.y * 3 / 4;
            if (height < MIN_FRAME_HEIGHT)
            {
                height = MIN_FRAME_HEIGHT;
            }
            else
                if (height > MAX_FRAME_HEIGHT)
                {
                    height = MAX_FRAME_HEIGHT;
                }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        }
        return framingRect;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview
     * frame, not UI / screen.
     */
    public Rect getFramingRectInPreview()
    {
        if (framingRectInPreview == null)
        {
            Rect rect = new Rect(getFramingRect());
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     * 
     * @param data
     *            A preview frame.
     * @param width
     *            The width of the image.
     * @param height
     *            The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */

    public byte[] buildLuminanceSource(byte[] data, int width, int height)
    {
        Rect rect = getFramingRect();
        int w = rect.right - rect.left + 1;
        int h = rect.bottom - rect.top + 1;
        int i = width * rect.top;
        int j = 0;
        byte[] image = new byte[w * h];
        for (int y = rect.top; y <= rect.bottom; y++)
        {
            for (int x = rect.left; x <= rect.right; x++)
            {
                image[j++] = data[i + x];
            }
            i += width;
        }
        return image;
    }

    public Bitmap renderCroppedGreyscaleBitmap(byte[] data, int width, int height)
    {
        int[] pixels = new int[width * height];
        byte[] yuv = data;
        int row = 0;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int grey = yuv[row + x] & 0xff;
                pixels[row + x] = 0xFF000000 | (grey * 0x00010101);
            }
            row += width;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public int getFrameWidth()
    {
        Rect rect = getFramingRect();
        return rect.right - rect.left + 1;
    }

    public int getFrameHeight()
    {
        Rect rect = getFramingRect();
        return rect.bottom - rect.top + 1;
    }

}
