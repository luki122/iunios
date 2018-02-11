/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.deskclock;

import java.util.Locale;

import com.android.deskclock.Alarms;
import com.android.deskclock.R;
import com.aurora.utils.GnRingtoneUtil;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraRingtonePreference;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;

import aurora.app.AuroraActivity;
import android.content.SharedPreferences;



/**
 * The RingtonePreference does not have a way to get/set the current ringtone so
 * we override onSaveRingtone and onRestoreRingtone to get the same behavior.
 */
public class AlarmPreference extends AuroraPreference {
    private Uri mAlert;
    private boolean mChangeDefault;
    private AsyncTask mRingtoneTask;

    private String mDefaultAlarmAlert;
    private Context mContext;
    private Uri mDefaultUri;

    public AlarmPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //aurora mod by tangjun 2013.12.20 start change AuroraRingtonePreference to AuroraPreference
    /*
    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        setAlert(ringtoneUri);
        if (mChangeDefault) {
            // Update the default alert in the system.
            Settings.System.putString(getContext().getContentResolver(),
                    Settings.System.ALARM_ALERT,
                    ringtoneUri == null ? null : ringtoneUri.toString());
        }
    }

    @Override
    protected Uri onRestoreRingtone() {
        if (RingtoneManager.isDefault(mAlert)) {
            // Gionee baorui 2012-12-21 modify for CR00733082 begin
            if (AlarmPreference.this.getShowDefault()) {
                return mAlert;
            }
            // Gionee baorui 2012-12-21 modify for CR00733082 end
            return RingtoneManager.getActualDefaultRingtoneUri(getContext(),
                    RingtoneManager.TYPE_ALARM);
        }
        return mAlert;
    }
    */
    
    protected void getSaveRingtone(Uri ringtoneUri) {
        setAlert(ringtoneUri);
        if (mChangeDefault) {
            // Update the default alert in the system.
            Settings.System.putString(getContext().getContentResolver(),
                    Settings.System.ALARM_ALERT,
                    ringtoneUri == null ? null : ringtoneUri.toString());
        }
    }

    protected Uri getRestoreRingtone() {
        if (RingtoneManager.isDefault(mAlert)) {

            return RingtoneManager.getActualDefaultRingtoneUri(getContext(),
                    RingtoneManager.TYPE_ALARM);
        }
        return mAlert;
    }
    
    //aurora mod by tangjun 2013.12.20

    public void setAlert(Uri alert) {
        mAlert = alert;
        // Gionee <baorui><2013-06-27> modify for CR00828064 begin
        mContext = getContext();
        mDefaultAlarmAlert = getgetDefaultAlarmAlert(mContext, RingtoneManager.TYPE_ALARM);

        // Gionee <baorui><2013-07-02> modify for CR00832465 begin
        if (isSilent(mAlert, mDefaultAlarmAlert)) {
            return;
        }
        // Gionee <baorui><2013-07-02> modify for CR00832465 end

        mDefaultUri = getDefaultRingtoneUri(mContext, RingtoneManager.TYPE_ALARM);
        // Gionee <baorui><2013-06-27> modify for CR00828064 end

        if (alert != null) {
//            setSummary(R.string.loading_ringtone);  不需要正在在如铃声的提示
        	
            if (mRingtoneTask != null) {
                mRingtoneTask.cancel(true);
            }
            mRingtoneTask = new AsyncTask<Uri, Void, String>() {
                @Override
                protected String doInBackground(Uri... params) {
                    // Gionee <baorui><2013-07-12> modify for CR00835747 begin
                    if (Alarms.mIsGnMtkPoweroffAlarmSupport) {
                        return getMtkAlertTitle(params[0]);
                    } else {
                        return getQcAlertTitle(params[0]);
                    }
                    // Gionee <baorui><2013-07-12> modify for CR00835747 end
                }

                @Override
                protected void onPostExecute(String title) {
					if (!isCancelled()) {
						String mytitle = title;
						int index = mytitle.indexOf(";");
						if (index == -1) {
							setSummary(title);
						} else {
							String language = Locale.getDefault().getLanguage();
							String[] array = mytitle.split(";");
							if (array.length < 2) {
								setSummary(title);
							

							} else {
								if (language.contains("en")) {
									setSummary(array[1]);
								

								} else {
									setSummary(array[0]);
									
								}

							}

						}

					//	setSummary(title);
						mRingtoneTask = null;
					}
                }
            }.execute(alert);
        } else {
            setSummary(R.string.silent_alarm_summary);
        }
    }

    public Uri getAlert() {
        return mAlert;
    }

    public void setChangeDefault() {
        mChangeDefault = true;
    }

    // Gionee <baorui><2013-06-27> modify for CR00828064 begin
    private Uri getDefaultRingtoneUri(Context context, int type) {
        return RingtoneManager.getActualDefaultRingtoneUri(context, type);
    }

    private String getgetDefaultAlarmAlert(Context context, int type) {
        if (null == getDefaultRingtoneUri(context, type)) {
            return null;
        }

        return getDefaultRingtoneUri(context, type).toString();
    }

    private boolean isSilent(Uri uri, String defaultAlert) {
        if (RingtoneManager.isDefault(uri)) {
            if (defaultAlert == null) {
                setSummary(R.string.silent_alarm_summary);
                return true;
            }
        }
        return false;
    }

    private boolean isRingtoneExist(Uri uri, Context context) {
        return GnRingtoneUtil.isRingtoneExist(uri, context.getContentResolver());
    }

    private String UpdateRintone(Context context, Uri uri) {
        String mUriStr = Settings.System.getString(context.getContentResolver(), Settings.System.RINGTONE);
        mUriStr = newRintoneStr(context, uri, mUriStr);

        return mUriStr;
    }

    private String newRintoneStr(Context context, Uri uri, String mUriStr) {
        SharedPreferences prefs = context.getSharedPreferences("SettingsActivity", AuroraActivity.MODE_PRIVATE);

        String mData = prefs.getString("_data", null);
        int mVolumes = prefs.getInt("volumes", 0);

        if (Alarms.isUpdateRintoneUri(mData, uri, context, mVolumes)) {
            mUriStr = Alarms.updateRintoneUri(mData, uri, context, mVolumes).toString();
        }

        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("alert", mUriStr);
        ed.putString("_data", Alarms.getExternalUriData(context, Uri.parse(mUriStr)));
        ed.putInt("volumes", Alarms.getVolumes(context));
        ed.apply();

        return mUriStr;
    }

    private String getMtkAlertTitle(Uri uri) {
        Ringtone r = RingtoneManager.getRingtone(mContext, uri);
        try {
            if (RingtoneManager.isDefault(uri)) {
                if (!isRingtoneExist(mDefaultUri, mContext)) {
                    String mUriStr = UpdateRintone(mContext, mDefaultUri);

                    mAlert = Uri.parse(mUriStr);

                    Settings.System.putString(mContext.getContentResolver(), Settings.System.ALARM_ALERT,
                            mAlert == null ? null : mAlert.toString());

                    r = RingtoneManager.getRingtone(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
                }
            }

            if (!isRingtoneExist(uri, mContext)) {
                String mUriStr = UpdateRintone(mContext, mDefaultUri);

                mAlert = Uri.parse(mUriStr);

                Settings.System.putString(mContext.getContentResolver(), Settings.System.ALARM_ALERT,
                        mAlert == null ? null : mAlert.toString());

                r = RingtoneManager.getRingtone(mContext, mAlert);
            }

        } catch (Exception e) {
            r = RingtoneManager.getRingtone(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
        }

        if (r == null) {
            r = RingtoneManager.getRingtone(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
        }

        if (r != null) {
            return r.getTitle(mContext);
        }

        return null;
        // Gionee <baorui><2013-06-27> modify for CR00828064 end
    }

    private String getQcAlertTitle(Uri uri) {
        // Gionee <baorui><2013-06-27> modify for CR00828064 begin
        String ret = GnRingtoneUtil.gnGetRingtoneTile(mContext, uri);
        try {
            if (RingtoneManager.isDefault(uri)) {
                if (!isRingtoneExist(mDefaultUri, mContext)) {
                    String mUriStr = UpdateRintone(mContext, mDefaultUri);

                    mAlert = Uri.parse(mUriStr);

                    Settings.System.putString(mContext.getContentResolver(), Settings.System.ALARM_ALERT,
                            mAlert == null ? null : mAlert.toString());

                    ret = GnRingtoneUtil.gnGetRingtoneTile(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
                }
            }

            if (!isRingtoneExist(uri, mContext)) {
                String mUriStr = UpdateRintone(mContext, mDefaultUri);

                mAlert = Uri.parse(mUriStr);

                Settings.System.putString(mContext.getContentResolver(), Settings.System.ALARM_ALERT,
                        mAlert == null ? null : mAlert.toString());

                ret = GnRingtoneUtil.gnGetRingtoneTile(mContext, mAlert);
            }

        } catch (Exception e) {
            ret = GnRingtoneUtil.gnGetRingtoneTile(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
        }

        if (ret == null || "".equals(ret)) {
            ret = GnRingtoneUtil.gnGetRingtoneTile(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
        }

        return ret;
        // Gionee <baorui><2013-06-27> modify for CR00828064 end
    }
    // Gionee <baorui><2013-07-12> modify for CR00835747 end
}
