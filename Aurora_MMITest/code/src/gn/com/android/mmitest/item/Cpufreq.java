/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package gn.com.android.mmitest.item;

import gn.com.android.emsvr.*; //import com.mediatek.engineermode.baseband.EMbaseband;
import gn.com.android.mmitest.R;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.app.AlertDialog;
import android.content.DialogInterface;

//import com.mediatek.engineermode.ShellExe;
import java.io.File;
import java.io.IOException;

public class Cpufreq extends Activity implements OnClickListener {

	private final int EVENT_UPDATE = 666;
	private Button mBtnStartTest;
	private Button mBtnStopTest;
	private Button mBtnCurrentTest;
	private TextView mInfo;

	private String TAG = "EM-CPUFREQ";
	private String mInfoStr = "Info: \n";
	private boolean mRun = false;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_UPDATE:				
				mInfo.setText(mInfoStr);	
				mInfo.invalidate();
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.cpufreq_test);

		mBtnStartTest = (Button) findViewById(R.id.CpuFreq_BtnStartTest);
		mBtnStopTest = (Button) findViewById(R.id.CpuFreq_BtnStopTest);
		mBtnCurrentTest = (Button) findViewById(R.id.CpuFreq_BtnCurrentTest);
		mInfo = (TextView) findViewById(R.id.CpuFreq_Info);
		if (mBtnStartTest == null || mBtnStopTest == null
				|| mBtnCurrentTest == null || mInfo == null) {
			Log.e(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mBtnStartTest.setOnClickListener(this);
		mBtnStopTest.setOnClickListener(this);
		mBtnCurrentTest.setOnClickListener(this);
		mBtnStopTest.setEnabled(false);
	}

	@Override
	public void onPause() {
		// EMbaseband.End();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStop() {
		onClick(mBtnStopTest);
		super.onStop();
	}

	private void buildMsg(String text)
	{
		if(text != null)
		{
			mInfoStr += text + "\n";
		}		
	}
	private void errorMsg(String text)
	{
		if(text != null)
		{
			mInfoStr += text + "\n";
		}
		mHandler.sendEmptyMessage(EVENT_UPDATE);
	}
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (arg0.getId() == mBtnStartTest.getId()) {
			
			mRun = true;
			new Thread() {
				@Override
				public void run() {

					for (int i = 0; i < 23 && mRun; i++) // step by step --> 520 ~ 806// MHZ
					{
						mInfoStr = "Info: \n";
						AFMFunctionCallEx A = new AFMFunctionCallEx();
						boolean result = A
								.StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_CPU_FREQ_TEST_START);
						A.WriteParamNo(1);
						A.WriteParamInt(i);

						if (!result) {
							errorMsg("ERROR Pipe");
							return;
						}
						FunctionReturn r;
						do {
							r = A.GetNextResult();
							if (r.returnString == "")
								break;
							buildMsg(r.returnString);

						} while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);
						if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
							// error
							errorMsg("ERROR");
						}
						else
						{
							mHandler.sendEmptyMessage(EVENT_UPDATE);
							//Log.e(TAG, mInfoStr);
						}
						try
						{
							sleep(500);// sleep too short cause TextView display error.
						}
						catch(InterruptedException e)
						{
							
						}
					}
					
				}
			}.start();

			mBtnStartTest.setEnabled(false);
			mBtnStopTest.setEnabled(true);
		} else if (arg0.getId() == mBtnStopTest.getId()) {
			
			AFMFunctionCallEx A = new AFMFunctionCallEx();
			boolean result = A
					.StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_CPU_FREQ_TEST_STOP);
			A.WriteParamNo(0);

			if (!result) {
				errorMsg("ERROR Pipe");				
				return;
			}

			FunctionReturn r;
			do {
				r = A.GetNextResult();
				if (r.returnString == "")
					break;

			} while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);
			if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
				// error
				errorMsg("ERROR");				
			}
			mBtnStartTest.setEnabled(true);
			mBtnStopTest.setEnabled(false);
			mRun = false;

		} else if (arg0.getId() == mBtnCurrentTest.getId()) {
			errorMsg("WARNING: Machine Will Halt.");
			new Thread() {
				@Override
				public void run() {
					try
					{
						sleep(500);//wait for display
					}
					catch(InterruptedException e)
					{
						
					}
					AFMFunctionCallEx A = new AFMFunctionCallEx();
					boolean result = A
							.StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_CPU_FREQ_TEST_CURRENCT);
					A.WriteParamNo(0);

					if (!result) {
						errorMsg("ERROR Pipe");
						return;
					}

					FunctionReturn r;
					do {
						r = A.GetNextResult();
						if (r.returnString == "")
							break;

					} while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);
					if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
						// error
						errorMsg("ERROR");
					}
				}

			}.start();

		}

	}
}