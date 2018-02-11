/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.contacts;

import com.android.internal.telephony.ITelephony;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.mediatek.contacts.util.TelephonyUtils;

import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraEditText;

/**
 * Helper class to listen for some magic character sequences
 * that are handled specially by the dialer.
 *
 * Note the Phone app also handles these sequences too (in a couple of
 * relativly obscure places in the UI), so there's a separate version of
 * this class under apps/Phone.
 *
 * TODO: there's lots of duplicated code between this class and the
 * corresponding class under apps/Phone.  Let's figure out a way to
 * unify these two classes (in the framework? in a common shared library?)
 */
public class SpecialCharSequenceMgr {
    private static final String TAG = "SpecialCharSequenceMgr";
    private static final String MMI_IMEI_DISPLAY = "*#06#";

    /** This class is never instantiated. */
    private SpecialCharSequenceMgr() {
    }

    public static boolean handleChars(Context context, String input, AuroraEditText textField) {
        return handleChars(context, input, false, textField);
    }

    public static boolean handleChars(Context context, String input) {
        return handleChars(context, input, false, null);
    }

    public static boolean handleChars(Context context, String input, boolean useSystemWindow,
            AuroraEditText textField) {

    	//Gionee:huangzy 20121107 modify for CR00682029 start
        //get rid of the separators so that the string gets parsed correctly
        /*String dialString = PhoneNumberUtils.stripSeparators(input);*/
        if (TextUtils.isEmpty(input) || !input.contains("#")) {
        	return false;
        }
        String dialString = PhoneNumberUtils.stripSeparators(input).trim();        
        //Gionee:huangzy 20121107 modify for CR00682029 end

        if (handleIMEIDisplay(context, dialString, useSystemWindow)
                || handlePinEntry(context, dialString)
                || handleAdnEntry(context, dialString, textField)
                || handleSecretCode(context, dialString)) {
            return true;
        }

        return false;
    }

    /**
     * Handles secret codes to launch arbitrary activities in the form of *#*#<code>#*#*.
     * If a secret code is encountered an Intent is started with the android_secret_code://<code>
     * URI.
     *
     * @param context the context to use
     * @param input the text to check for a secret code in
     * @return true if a secret code was encountered
     */
    public static boolean handleSecretCode(Context context, String input) {
        // Secret codes are in the form *#*#<code>#*#*
        int len = input.length();
        
        // aurora privacy enter
        if (ContactsApplication.sIsAuroraPrivacySupport 
        		&& len > 5 && input.startsWith("#") && input.endsWith("#")) {
            Intent intent = new Intent("com.aurora.privacymanage.ENTER");
            intent.putExtra("password", input.substring(1, len - 1));
            intent.putExtra("time", System.currentTimeMillis());
            context.sendBroadcast(intent);
            return true;
        }
        
        if (len > 8 && input.startsWith("*#*#") && input.endsWith("#*#*")) {
            Intent intent = new Intent("android.provider.Telephony.SECRET_CODE",
                    Uri.parse("android_secret_code://" + input.substring(4, len - 4)));
            context.sendBroadcast(intent);
            return true;
        }

        return false;
    }

    /**
     * Handle ADN requests by filling in the SIM contact number into the requested
     * EditText.
     *
     * This code works alongside the Asynchronous query handler {@link QueryHandler}
     * and query cancel handler implemented in {@link SimContactQueryCookie}.
     */
    public static boolean handleAdnEntry(Context context, String input, AuroraEditText textField) { 
    	return false;
    	}

    public static boolean handlePinEntry(Context context, String input) {
        if ((input.startsWith("**04") || input.startsWith("**05")) && input.endsWith("#")) {
            try {
                return ITelephony.Stub.asInterface(ServiceManager.getService("phone"))
                        .handlePinMmi(input);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to handlePinMmi due to remote exception");
                return false;
            }
        }
        return false;
    }

    public static boolean handleIMEIDisplay(Context context, String input, boolean useSystemWindow) {
        if (input.equals(MMI_IMEI_DISPLAY)) {
            int phoneType = ((TelephonyManager)context.getSystemService(
                    Context.TELEPHONY_SERVICE)).getCurrentPhoneType();

            if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                showIMEIPanel(context, useSystemWindow);
                return true;
            } else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                showMEIDPanel(context, useSystemWindow);
                return true;
            }
        }

        return false;
    }

    // TODO: Combine showIMEIPanel() and showMEIDPanel() into a single
    // generic "showDeviceIdPanel()" method, like in the apps/Phone
    // version of SpecialCharSequenceMgr.java.  (This will require moving
    // the phone app's TelephonyCapabilities.getDeviceIdLabel() method
    // into the telephony framework, though.)

    public static void showIMEIPanel(Context context, boolean useSystemWindow) {
        String imeiStr = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();

        /**
         * add by mediatek .inc
         * description : set the imeiStr to 'Invalid'
         * when it's empty
         */
        if(TextUtils.isEmpty(imeiStr)) {
            imeiStr = context.getResources().getString(R.string.imei_invalid);
        }
        /**
         * add by mediatek .inc end
         */

        AuroraAlertDialog alert = new AuroraAlertDialog.Builder(context)
                .setTitle(R.string.imei)
                .setMessage(imeiStr)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();
    }

    public static void showMEIDPanel(Context context, boolean useSystemWindow) {
        String meidStr = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();

        AuroraAlertDialog alert = new AuroraAlertDialog.Builder(context)
                .setTitle(R.string.meid)
                .setMessage(meidStr)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();
    }

    /*******
     * This code is used to handle SIM Contact queries
     *******/
    private static final String ADN_PHONE_NUMBER_COLUMN_NAME = "number";
    private static final String ADN_NAME_COLUMN_NAME = "name";
    private static final String ADN_INDEX_COLUMN_NAME = "index";	
    private static final int ADN_QUERY_TOKEN = -1;

    /**
     * Cookie object that contains everything we need to communicate to the
     * handler's onQuery Complete, as well as what we need in order to cancel
     * the query (if requested).
     *
     * Note, access to the textField field is going to be synchronized, because
     * the user can request a cancel at any time through the UI.
     */
    private static class SimContactQueryCookie implements DialogInterface.OnCancelListener{
        public AuroraProgressDialog progressDialog;
        public int contactNum;

        // Used to identify the query request.
        private int mToken;
        private QueryHandler mHandler;

        // The text field we're going to update
        private AuroraEditText textField;
        public String text;

        public SimContactQueryCookie(int number, QueryHandler handler, int token) {
            contactNum = number;
            mHandler = handler;
            mToken = token;
        }

        /**
         * Synchronized getter for the EditText.
         */
        public synchronized AuroraEditText getTextField() {
            return textField;
        }

        /**
         * Synchronized setter for the EditText.
         */
        public synchronized void setTextField(AuroraEditText text) {
            textField = text;
        }

        /**
         * Cancel the ADN query by stopping the operation and signaling
         * the cookie that a cancel request is made.
         */
        public synchronized void onCancel(DialogInterface dialog) {
            // close the progress dialog
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            // setting the textfield to null ensures that the UI does NOT get
            // updated.
            textField = null;

            // Cancel the operation if possible.
            mHandler.cancelOperation(mToken);
        }
    }

    /**
     * Asynchronous query handler that services requests to look up ADNs
     *
     * Queries originate from {@link handleAdnEntry}.
     */
    private static class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * Override basic onQueryComplete to fill in the textfield when
         * we're handed the ADN cursor.
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            SimContactQueryCookie sc = (SimContactQueryCookie) cookie;

            // close the progress dialog.
            sc.progressDialog.dismiss();
            if(fdnRequest()) return;

            // get the EditText to update or see if the request was cancelled.
            AuroraEditText text = sc.getTextField();
            String name = null;
            String number = null;

            if (c != null && text != null) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    if (c.getInt(c.getColumnIndexOrThrow(ADN_INDEX_COLUMN_NAME)) == sc.contactNum) {
                        name = c.getString(c.getColumnIndexOrThrow(ADN_NAME_COLUMN_NAME));
                        number = c.getString(c.getColumnIndexOrThrow(ADN_PHONE_NUMBER_COLUMN_NAME));
                        break;
                    }
                }
                c.close();
            }

            // if the textview is valid, and the cursor is valid and postionable
            // on the Nth number, then we update the text field and display a
            // toast indicating the caller name.
                final Context context = sc.progressDialog.getContext();
                final int len = number != null ? number.length() : 0;
                Log.d("onQueryComplete","number " + number + "sc.text" + sc.text);
                if (sc.text.equals(number)) {
                    Toast .makeText(context, context.getString(R.string.non_phone_caption) + "\n" + number, Toast.LENGTH_LONG).show();
                } else if ((len > 1) && (len < 5) && (number.endsWith("#"))) {
                    Toast.makeText(context, context.getString(R.string.non_phone_caption) + "\n" + number, Toast.LENGTH_LONG).show();
                } else if(number != null){
                    // fill the text in.
                    text.setText(number);
                    text.setSelection(text.getText().length());

                    // display the name as a toast
                    name = context.getString(R.string.menu_callNumber, name);
                    Toast.makeText(context, name, Toast.LENGTH_SHORT)
                        .show();
                }
            
        }
    }

	static boolean fdnRequest() {

		boolean bRet = false;

		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (null == iTel) {
			Log.e(TAG, "fdnRequest iTel is null");
			return false;
		}

//		try {
//			bRet = AuroraITelephony.isFDNEnabled(iTel);
//		} catch (Exception e) {
//			Log.e(TAG, e.toString());
//			e.printStackTrace();
//		}

		Log.d(TAG, "fdnRequest fdn enable is " + bRet);
		return bRet;
	}	
}
