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
 */

package com.google.zxing.decoding;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.aurora.voiceassistant.view.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.camera.CameraManager;
import com.google.zxing.camera.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.aurora.voiceassistant.view.BarCodeMainActivity;
import com.aurora.voiceassistant.view.BitmapLuminanceSource;
import com.aurora.voiceassistant.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.util.Hashtable;

final class DecodeHandler extends Handler {

	private static final String TAG = DecodeHandler.class.getSimpleName();

	private final BarCodeMainActivity activity;
	private final MultiFormatReader multiFormatReader;

	/*
	 * private boolean notFoundFlag = false; private AlertDialog notFoundDialog
	 * = null;
	 */

	DecodeHandler(BarCodeMainActivity activity, Hashtable<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.decode:
			// Log.d(TAG, "Got decode message");
			// Log.d("ZXingDemo",
			// "message.obj = "+message.obj+" message.arg1 = "+message.arg1+" message.arg2 = "+message.arg2);
			decode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case R.id.quit:
			Looper.myLooper().quit();
			break;
		}
	}

	/**
	 * Decode the data within the viewfinder rectangle, and time how long it
	 * took. For efficiency, reuse the same reader objects from one decode to
	 * the next.
	 * 
	 * @param data
	 *            The YUV preview frame.
	 * @param width
	 *            The width of the preview frame.
	 * @param height
	 *            The height of the preview frame.
	 */
	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		Result rawResult = null;
		// Log.d("ZXingDemo",
		// "DecodeHandler-----decode----data11111111111 = "+data+" width = "+width+" height = "+height);
		if (activity.getResultLayoutVisibility() == View.VISIBLE || activity.getNotFoundFlag()) {
			return;
		}

		/****** add for potrait start *****/
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}
		int tmp = width; // Here we are swapping, that's the difference to #11
		width = height;
		height = tmp;
		/****** add for potrait end *****/

		boolean flag = false;

		// PlanarYUVLuminanceSource source =
		// CameraManager.get().buildLuminanceSource(data, width, height);
		PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
		if (source != null) {
			BinaryBitmap bitmap = null;
			if (activity.getTransferedBitmap() != null) {
				Log.d("ZXingDemo", "choosed photo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1 ");
				// bitmap = new BinaryBitmap(new HybridBinarizer(new
				// BitmapLuminanceSource(activity.getTransferedBitmap())));

				bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(activity.getTransferedBitmap())));

    		
				flag = true;
			} else {
				bitmap = new BinaryBitmap(new HybridBinarizer(source));
			}
    	
			// BinaryBitmap bitmap = new BinaryBitmap(new
			// HybridBinarizer(source));
			// Log.d("ZXingDemo", "the source = "+source);
			// Log.d("ZXingDemo",
			// "the bitmap.width = "+bitmap.getWidth()+" and the bitmap.height = "+bitmap.getHeight());

			try {
				rawResult = multiFormatReader.decodeWithState(bitmap);
				Log.d("ZXingDemo", "DecodeHandler---------- decodeWithState(bitmap)------------------------------ = ");
			} catch (ReaderException re) {
				// continue
			} finally {
				// Log.d("ZXingDemo",
				// "DecodeHandler---------- decodeWithState(bitmap)----------------------------finally() = ");
				multiFormatReader.reset();
			}
			// }
		}

		Log.d("ZXingDemo", "DecodeHandler----------- the rawResult = "+ rawResult);
		if (rawResult != null) {
			long end = System.currentTimeMillis();
			Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n"+ rawResult.toString());
			Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
			Bundle bundle = new Bundle();
			bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
			message.setData(bundle);
			// Log.d(TAG, "Sending decode succeeded message...");
			
			//account start
			message.arg1 = flag == false ? 1 : 2;
			//account end
			
			message.sendToTarget();

			activity.releaseChoosedBitmap();
		} else {
			if (flag) {
				activity.showNotFoundDialog();
			}
			activity.releaseChoosedBitmap();

			Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
			message.sendToTarget();
		}
	}
	
}
