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
 */

package com.google.zxing.decoding;

import com.aurora.voiceassistant.account.TotalCount;
import com.aurora.voiceassistant.model.CFG;
import com.aurora.voiceassistant.view.BarCodeMainActivity;
import com.aurora.voiceassistant.view.ViewfinderResultPointCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.camera.CameraManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.aurora.voiceassistant.*;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class BarCodeMainActivityHandler extends Handler {

	private static final String TAG = BarCodeMainActivityHandler.class
			.getSimpleName();

	private final BarCodeMainActivity activity;
	private final DecodeThread decodeThread;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public BarCodeMainActivityHandler(BarCodeMainActivity activity, Vector<BarcodeFormat> decodeFormats, String characterSet) {
		this.activity = activity;
		decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
				new ViewfinderResultPointCallback(activity.getViewfinderView()));
		decodeThread.start();
		state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.auto_focus:
			// Log.d(TAG, "Got auto-focus message");
			// When one auto focus pass finishes, start another. This is the
			// closest thing to
			// continuous AF. It does seem to hunt a bit, but I'm not sure what
			// else to do.
			if (state == State.PREVIEW) {
				Log.d("ZXingDemo", "BarCodeMainActivityHandler-------------------auto_focus");
				CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
			}
			break;
		case R.id.restart_preview:
			Log.d(TAG, "Got restart preview message");
			Log.d("ZXingDemo", "BarCodeMainActivityHandler-------------------restart_preview");
			restartPreviewAndDecode();
			break;
		case R.id.decode_succeeded:
			Log.d(TAG, "Got decode succeeded message");
			Log.d("ZXingDemo", "BarCodeMainActivityHandler-------------------decode_succeeded");
			
			//account start
			TotalCount mTotalCount = null;
			switch (message.arg1) {
			case 1:
				mTotalCount = new TotalCount(activity, CFG.ACCOUNT_MODULE_ID, CFG.ACCOUNT_ACTION_BARCODE_CAMERA, 1);
				break;
			case 2:
				mTotalCount = new TotalCount(activity, CFG.ACCOUNT_MODULE_ID, CFG.ACCOUNT_ACTION_BARCODE_PHOTO, 1);
				break;
				
			}
			if (mTotalCount != null) {
				mTotalCount.CountData();
			}
			//account end
			
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
			activity.handleDecode((Result) message.obj, barcode);
			break;
		case R.id.decode_failed:
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			Log.d("ZXingDemo", "BarCodeMainActivityHandler-------------------decode_failed");
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
			// activity.autoFocusAgain();
			break;
		case R.id.return_scan_result:
			Log.d(TAG, "Got return scan result message");
			// activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
			// activity.finish();

			Log.d("ZXingDemo", "BarCodeMainActivityHandle-------------------return_scan_result");
			
//			activity.changeActionBarAs(false);
//			activity.showResultLayout(true);
			// activity.finish();
			// activity.setActivityResult();

			break;
		case R.id.launch_product_query:
			/*
			 * Log.d(TAG, "Got product query message"); Log.d("ZXingDemo",
			 * "BarCodeMainActivity-------------------launch_product_query");
			 * String url = (String) message.obj; Intent intent = new
			 * Intent(Intent.ACTION_VIEW, Uri.parse(url));
			 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			 * activity.startActivity(intent);
			 */
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();
		try {
			decodeThread.join();
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

	// private void restartPreviewAndDecode() {
	public void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			Log.d("ZXingDemo", "BarCodeMainActivityHandler-----------restartPreviewAndDecode");
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
			CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
			activity.drawViewfinder();
		}
	}

}
