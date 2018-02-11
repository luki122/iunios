/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.ui;

import com.android.internal.widget.LockPatternUtils;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessagingPreferenceActivity;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;
//import android.drm.DrmHelper;

public class SecuritySettingActivity extends AuroraPreferenceActivity{
    

    private static final int MIN_PASSWORD_LENGTH = 4;
    
    public static final String PASSWOERD_CLEAR         = "pref_key_mms_password_clear";
    public static final String PASSWOERD_CHANGE        = "pref_key_mms_password_change";
    
    private AuroraPreference mPasswordClear;
    private AuroraPreference mPasswordChange;
    
    protected void onCreate(Bundle icicle) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(icicle);
        
        addPreferencesFromResource(R.xml.gn_securitsetting);
        
        mPasswordClear = (AuroraPreference) findPreference(PASSWOERD_CLEAR);
        
        mPasswordChange = (AuroraPreference) findPreference(PASSWOERD_CHANGE);
        //gionee gaoj 2012-5-29 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            ActionBar actionBar = getActionBar();
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            actionBar.setDisplayShowHomeEnabled(false);
            //gionee gaoj added for CR00725602 20121201 start
            actionBar.setDisplayHomeAsUpEnabled(true);
            //gionee gaoj added for CR00725602 20121201 end
        }
        //gionee gaoj 2012-5-29 added for CR00555790 end
    }

    //gionee gaoj added for CR00725602 20121201 start
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    //gionee gaoj added for CR00725602 20121201 end

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        // TODO Auto-generated method stub
        if (preference == mPasswordChange) {
            inputencryption();
        } else if (preference == mPasswordClear) {
            updateAll(this);
//            Conversation.savepsw(getApplicationContext(), DrmHelper.getIMEI(getApplicationContext()));
            Conversation.setFirstEncryption(true);
            //Gionee <zhouyj> <2013-05-06> add for CR00803793 begin
            Conversation.savepsw(this, "cr");
            //Gionee <zhouyj> <2013-05-06> add for CR00803793 end
            Toast.makeText(this, R.string.pref_summary_mms_password_clear_succeed, Toast.LENGTH_SHORT).show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }   
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case ConvFragment.UPDATE_PASSWORD_REQUEST:
                if (data != null && data.getAction().equals("succeed")) {
                    Toast.makeText(this, R.string.pref_summary_mms_password_change_succeed, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void updateAll(Context context) {
        long threadid = -1;
        Uri uri = Uri.parse("content://mms-sms/encryption/" + threadid);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put("encryption", 0);
        resolver.update(uri, values, null, null);
    }
    
    private void inputencryption() {
        DevicePolicyManager DPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        int quality = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC;
        int minQuality = DPM.getPasswordQuality(null);
        if (quality < minQuality) {
            quality = minQuality;
        }
        if (quality >= DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
            int minLength = DPM.getPasswordMinimumLength(null);
            if (minLength < MIN_PASSWORD_LENGTH) {
                minLength = MIN_PASSWORD_LENGTH;
            }
            final int maxLength = DPM.getPasswordMaximumLength(quality);
            Intent intent = new Intent(this, MsgChooseLockPassword.class);
            intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
            intent.putExtra(MsgChooseLockPassword.PASSWORD_MIN_KEY, minLength);
            intent.putExtra(MsgChooseLockPassword.PASSWORD_MAX_KEY, maxLength);
            startActivityForResult(intent, ConvFragment.UPDATE_PASSWORD_REQUEST);
        }
    }
}
