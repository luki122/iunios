/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.inputmethod;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import aurora.app.AuroraAlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreferenceActivity;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Comparator;
import java.util.List;

// gionee wangyaohui 2012825 add for CR00681083 begin 
import com.android.settings.GnSettingsUtils;
import android.content.Context;
// gionee wangyaohui 2012825 add for CR00681083 end 

public class InputMethodPreference extends AuroraCheckBoxPreference
        implements Comparator<InputMethodPreference> {
    private static final String TAG = InputMethodPreference.class.getSimpleName();
    private static final float DISABLED_ALPHA = 0.4f;
    private final SettingsPreferenceFragment mFragment;
    private final InputMethodInfo mImi;
    private final InputMethodManager mImm;
    private final Intent mSettingsIntent;
    private final boolean mAlwaysChecked;
    private final boolean mIsSystemIme;

    private AuroraAlertDialog mDialog = null;
    private ImageView mInputMethodSettingsButton;
    private TextView mTitleText;
    private TextView mSummaryText;
    private View mInputMethodPref;
	private ImageView mInputMethodIcon;
    // gionee wangyaohui 2012825 add for CR00681083 begin 
    private View mSeparatorView = null;
    private Context mContext = null;
    // gionee wangyaohui 2012825 add for CR00681083 end 

    private final OnClickListener mPrefOnclickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (!isEnabled()) {
                return;
            }
            if (isChecked()) {
                setChecked(false, true /* save */);
            } else {
                if (mIsSystemIme) {
                    setChecked(true, true /* save */);
                } else {
                    showSecurityWarnDialog(mImi, InputMethodPreference.this);
                }
            }
        }
    };

    public InputMethodPreference(SettingsPreferenceFragment fragment, Intent settingsIntent,
            InputMethodManager imm, InputMethodInfo imi, int imiCount) {
        super(fragment.getActivity(), null, R.style.InputMethodPreferenceStyle);

        // Aurora <likai> modify begin
        //setLayoutResource(R.layout.preference_inputmethod);
        setLayoutResource(R.layout.aurora_preference_inputmethod);
        // Aurora <likai> modify end

        setWidgetLayoutResource(R.layout.preference_inputmethod_widget);
        mFragment = fragment;
        mSettingsIntent = settingsIntent;
        mImm = imm;
        mImi = imi;
        updateSummary();
        mAlwaysChecked = InputMethodAndSubtypeUtil.isAlwaysCheckedIme(
                imi, fragment.getActivity(), imiCount);
        mIsSystemIme = InputMethodAndSubtypeUtil.isSystemIme(imi);
        if (mAlwaysChecked) {
            setEnabled(false);
        }
    	// gionee wangyaohui 2012825 add for CR00681083 begin 
	mContext = fragment.getActivity();
    	// gionee wangyaohui 2012825 add for CR00681083 end 
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        // Aurora <likai> <2013-11-11> modify begin
        //mInputMethodPref = view.findViewById(R.id.inputmethod_pref);
        mInputMethodPref = view;
        // Aurora <likai> <2013-11-11> modify end

        mInputMethodPref.setOnClickListener(mPrefOnclickListener);
        mInputMethodSettingsButton = (ImageView)view.findViewById(R.id.inputmethod_settings);
		mInputMethodIcon = (ImageView)view.findViewById(R.id.inputmethod_icon);
        mTitleText = (TextView)view.findViewById(android.R.id.title);
        mSummaryText = (TextView)view.findViewById(android.R.id.summary);

        // Aurora <likai> delete begin
        /*
    	// gionee wangyaohui 2012825 add for CR00681083 begin 
	 mSeparatorView = view.findViewById(R.id.gn_separator);	
	if (null != mSeparatorView) {
	    if (GnSettingsUtils.getThemeType(mContext.getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
			mSeparatorView.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
		} else {
	       mSeparatorView.setBackgroundResource(android.R.drawable.divider_horizontal_dark);
	       	}
	}
    	// gionee wangyaohui 2012825 add for CR00681083 end 
        */
        // Aurora <likai> delete end

        final boolean hasSubtypes = mImi.getSubtypeCount() > 1;
        final String imiId = mImi.getId();
        if (hasSubtypes) {
            mInputMethodPref.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    final Bundle bundle = new Bundle();
                    bundle.putString(Settings.EXTRA_INPUT_METHOD_ID, imiId);
                    startFragment(mFragment, InputMethodAndSubtypeEnabler.class.getName(),
                            0, bundle);
                    return true;
                }
            });
        }

        if (mSettingsIntent != null) {
            mInputMethodSettingsButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            try {
                                mFragment.startActivity(mSettingsIntent);
                            } catch (ActivityNotFoundException e) {
                                Log.d(TAG, "IME's Settings AuroraActivity Not Found: " + e);
                                final String msg = mFragment.getString(
                                        R.string.failed_to_open_app_settings_toast,
                                        mImi.loadLabel(
                                                mFragment.getActivity().getPackageManager()));
                                Toast.makeText(
                                        mFragment.getActivity(), msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        if (hasSubtypes) {
            final OnLongClickListener listener = new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    final Bundle bundle = new Bundle();
                    bundle.putString(Settings.EXTRA_INPUT_METHOD_ID, imiId);
                    startFragment(mFragment, InputMethodAndSubtypeEnabler.class.getName(),
                            0, bundle);
                    return true;
                }
            };
            mInputMethodSettingsButton.setOnLongClickListener(listener);
        }
        if (mSettingsIntent == null) {
            mInputMethodSettingsButton.setVisibility(View.GONE);
        } else {
            updatePreferenceViews();
        }
		final boolean checked = isChecked();
		if(mIsSystemIme) {
			mInputMethodIcon.setBackgroundResource(R.drawable.aurora_input_method_disable);
		}else {
			if(checked) {
				mInputMethodIcon.setBackgroundResource(R.drawable.aurora_input_method_checked);
			} else {
				mInputMethodIcon.setBackgroundResource(R.drawable.aurora_input_method_uncheck);
			}
		}
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updatePreferenceViews();
    }

    private void updatePreferenceViews() {
        final boolean checked = isChecked();
        if (mInputMethodSettingsButton != null) {
            mInputMethodSettingsButton.setEnabled(checked);
            mInputMethodSettingsButton.setClickable(checked);
            mInputMethodSettingsButton.setFocusable(checked);
            if (!checked) {
                mInputMethodSettingsButton.setAlpha(DISABLED_ALPHA);
            }
        }
        if (mTitleText != null) {
            mTitleText.setEnabled(true);
        }
        if (mSummaryText != null) {
            mSummaryText.setEnabled(checked);
        }
        if (mInputMethodPref != null) {
            mInputMethodPref.setEnabled(true);
            mInputMethodPref.setLongClickable(checked);
            final boolean enabled = isEnabled();
            mInputMethodPref.setOnClickListener(enabled ? mPrefOnclickListener : null);
            if (!enabled) {
                //Gionee <chenml> <2013-08-15> modify for CR00854905 begin
               // mInputMethodPref.setBackgroundColor(0);
                //Gionee <chenml> <2013-08-15> modify for CR00854905 end
            }
        }
    }

    public static boolean startFragment(
            Fragment fragment, String fragmentClass, int requestCode, Bundle extras) {
        if (fragment.getActivity() instanceof AuroraPreferenceActivity) {
            AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity)fragment.getActivity();
            preferenceActivity.startPreferencePanel(fragmentClass, extras, 0, null, fragment,
                    requestCode);
            return true;
        } else {
            Log.w(TAG, "Parent isn't AuroraPreferenceActivity, thus there's no way to launch the "
                    + "given Fragment (name: " + fragmentClass + ", requestCode: " + requestCode
                    + ")");
            return false;
        }
    }

    public String getSummaryString() {
        final StringBuilder builder = new StringBuilder();
        final List<InputMethodSubtype> subtypes = mImm.getEnabledInputMethodSubtypeList(mImi, true);
        for (InputMethodSubtype subtype : subtypes) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            final CharSequence subtypeLabel = subtype.getDisplayName(mFragment.getActivity(),
                    mImi.getPackageName(), mImi.getServiceInfo().applicationInfo);
            builder.append(subtypeLabel);
        }
        return builder.toString();
    }

    public void updateSummary() {
        // Aurora <likai> delete begin
        /*
        final String summary = getSummaryString();
        if (TextUtils.isEmpty(summary)) {
            return;
        }
        setSummary(summary);
        */
        // Aurora <likai> delete end
    }

    /**
     * Sets the checkbox state and optionally saves the settings.
     * @param checked whether to check the box
     * @param save whether to save IME settings
     */
    public void setChecked(boolean checked, boolean save) {
        super.setChecked(checked);
        if (save) {
            saveImeSettings();
        }
        updateSummary();
    }

    @Override
    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    private void showSecurityWarnDialog(InputMethodInfo imi, final InputMethodPreference chkPref) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        //Gionee <chenml> <2013-03-15> modify for CR00785091 begin 
        mDialog = (new AuroraAlertDialog.Builder(mFragment.getActivity()))
                .setTitle(android.R.string.dialog_alert_title)
             //   .setIconAttribute(android.R.attr.alertDialogIcon)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chkPref.setChecked(true, true);
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        mDialog.setMessage(mFragment.getResources().getString(R.string.ime_security_warning,
                imi.getServiceInfo().applicationInfo.loadLabel(
                        mFragment.getActivity().getPackageManager())));
        mDialog.show();
    }
    //Gionee <chenml> <2013-03-15> modify for CR00785091 end 

    @Override
    public int compare(InputMethodPreference arg0, InputMethodPreference arg1) {
        if (arg0.isEnabled() == arg0.isEnabled()) {
            return arg0.mImi.getId().compareTo(arg1.mImi.getId());
        } else {
            // Prefer system IMEs
            return arg0.isEnabled() ? 1 : -1;
        }
    }

    private void saveImeSettings() {
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(
                mFragment, mFragment.getActivity().getContentResolver(), mImm.getInputMethodList(),
                mFragment.getResources().getConfiguration().keyboard
                        == Configuration.KEYBOARD_QWERTY);
    }
}
