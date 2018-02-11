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

package com.android.settings.inputmethod;

import com.android.settings.R;
import com.android.settings.Settings.KeyboardLayoutPickerActivity;
import com.android.settings.Settings.SpellCheckersSettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.VoiceInputOutputSettings;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.os.Handler;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.text.Collator;
import java.util.Comparator;
import com.android.settings.Utils;

import android.util.Log;
import android.os.SystemProperties;
import android.os.Build;

public class InputMethodAndLanguageSettings extends SettingsPreferenceFragment
        implements AuroraPreference.OnPreferenceChangeListener, InputManager.InputDeviceListener,
        KeyboardLayoutDialogFragment.OnSetupKeyboardLayoutsListener {

    private static final String KEY_PHONE_LANGUAGE = "phone_language";
    private static final String KEY_CURRENT_INPUT_METHOD = "current_input_method";
    private static final String KEY_INPUT_METHOD_SELECTOR = "input_method_selector";
    private static final String KEY_USER_DICTIONARY_SETTINGS = "key_user_dictionary_settings";
    // false: on ICS or later
    private static final boolean SHOW_INPUT_METHOD_SWITCHER_SETTINGS = false;

    private static final String[] sSystemSettingNames = {
        System.TEXT_AUTO_REPLACE, System.TEXT_AUTO_CAPS, System.TEXT_AUTO_PUNCTUATE,
    };

    private static final String[] sHardKeyboardKeys = {
        "auto_replace", "auto_caps", "auto_punctuate",
    };

    private int mDefaultInputMethodSelectorVisibility = 0;
    private AuroraListPreference mShowInputMethodSelectorPref;
    private AuroraPreferenceCategory mKeyboardSettingsCategory;
    private AuroraPreferenceCategory mHardKeyboardCategory;
    private AuroraPreferenceCategory mGameControllerCategory;
    private AuroraPreference mLanguagePref;
    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList =
            new ArrayList<InputMethodPreference>();
    private final ArrayList<AuroraPreferenceScreen> mHardKeyboardPreferenceList =
            new ArrayList<AuroraPreferenceScreen>();
    private InputManager mIm;
    private InputMethodManager mImm;
    private List<InputMethodInfo> mImis;
    private boolean mIsOnlyImeSettings;
    private Handler mHandler;
    @SuppressWarnings("unused")
    private SettingsObserver mSettingsObserver;
    private Intent mIntentWaitingForResult;
	private boolean mIsIuniInputMethod = false;

    // Aurora <likai> add begin
    private AuroraPreferenceScreen mUserDictionaryPref;
    // Aurora <likai> add end

	class InputMethodInformation 
	{
		String label;
		String value;
	}

	private final static Comparator<InputMethodInformation> sDisplayNameComparator
            = new Comparator<InputMethodInformation>() {
         public final int
         compare(InputMethodInformation a, InputMethodInformation b) {
             return collator.compare(Utils.getSpell(a.label), Utils.getSpell(b.label));
         }
 
         private final Collator collator = Collator.getInstance();
     };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

//        String mDeviceName = SystemProperties.get("ro.product.name");
        String buildModel = Build.MODEL;
//        Log.i("wolfu" , "mDeviceName = "+mDeviceName);
        Log.i("wolfu" , "buildModel = "+buildModel);
//        if(mDeviceName.contains("IUNI"))
        /*
        if(buildModel.contains("U3"))
        addPreferencesFromResource(R.xml.language_settings);
        else 
        addPreferencesFromResource(R.xml.input_settings);
            */
        addPreferencesFromResource(R.xml.language_settings);

        try {
            mDefaultInputMethodSelectorVisibility = Integer.valueOf(
                    getString(R.string.input_method_selector_visibility_default_value));
        } catch (NumberFormatException e) {
        }
        // del temp qy 2014 06 10 begin
               // qy 2014 06 28 add begin
        //wolfu open for u2,u3 20140726
        
        if (getActivity().getAssets().getLocales().length == 1 /*|| ! buildModel.contains("U3")*/) {
//        if (getActivity().getAssets().getLocales().length == 1 || ! mDeviceName.contains("IUNI")) {
            // No "Select language" pref if there's only one system locale available.
            getPreferenceScreen().removePreference(findPreference(KEY_PHONE_LANGUAGE));
        } else 
        
        {
           
            mLanguagePref = findPreference(KEY_PHONE_LANGUAGE);
        }
     // del temp qy 2014 06 10 end
        if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
            mShowInputMethodSelectorPref = (AuroraListPreference)findPreference(
                    KEY_INPUT_METHOD_SELECTOR);
            mShowInputMethodSelectorPref.setOnPreferenceChangeListener(this);
            // TODO: Update current input method name on summary
            updateInputMethodSelectorSummary(loadInputMethodSelectorVisibility());
        }

        new VoiceInputOutputSettings(this).onCreate();

        // Get references to dynamically constructed categories.
        mHardKeyboardCategory = (AuroraPreferenceCategory)findPreference("hard_keyboard");
        mKeyboardSettingsCategory = (AuroraPreferenceCategory)findPreference(
                "keyboard_settings_category");
        mGameControllerCategory = (AuroraPreferenceCategory)findPreference(
                "game_controller_settings_category");

        // Filter out irrelevant features if invoked from IME settings button.
        mIsOnlyImeSettings = Settings.ACTION_INPUT_METHOD_SETTINGS.equals(
                getActivity().getIntent().getAction());
        getActivity().getIntent().setAction(null);
        if (mIsOnlyImeSettings) {
            getPreferenceScreen().removeAll();
            getPreferenceScreen().addPreference(mHardKeyboardCategory);
            if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
                getPreferenceScreen().addPreference(mShowInputMethodSelectorPref);
            }
            getPreferenceScreen().addPreference(mKeyboardSettingsCategory);
        }

        // Build IME preference category.
        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //mImis = mImm.getInputMethodList();
		mImis = mImm.getInputMethodList();

        mKeyboardSettingsCategory.removeAll();
        if (!mIsOnlyImeSettings) {
            final AuroraPreferenceScreen currentIme = new AuroraPreferenceScreen(getActivity(), null);
            currentIme.setKey(KEY_CURRENT_INPUT_METHOD);
            currentIme.setTitle(getResources().getString(R.string.current_input_method));
			mKeyboardSettingsCategory.setHeight(AuroraPreferenceCategory.CATEGORY_ZERO_HEIGHT);
            mKeyboardSettingsCategory.addPreference(currentIme);
        }

		PackageManager pm = getPackageManager();
		final List<InputMethodInformation> mInputMethodInfoList = new ArrayList<InputMethodInformation>();

		String systemDefaultInputMethod = "com.sohu.inputmethod.sogouoem/.SogouIME";
        mInputMethodPreferenceList.clear();
        final int N = (mImis == null ? 0 : mImis.size());
		for (int i = 0; i < N; i++) {
			InputMethodInformation inputMethodInformation = new InputMethodInformation();
            inputMethodInformation.label = mImis.get(i).loadLabel(pm).toString();
			inputMethodInformation.value = mImis.get(i).getId();
			mInputMethodInfoList.add(inputMethodInformation);
        }
		Collections.sort(mInputMethodInfoList, sDisplayNameComparator);
		int[] index = new int[N];
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < N; j++) {
				if(mInputMethodInfoList.get(i).label.equals(mImis.get(j).loadLabel(pm).toString())) {
					index[i] = j;
				}
			}
		}

        for (int i = 0; i < N; ++i) {
            final InputMethodInfo imi = mImis.get(index[i]);
            final InputMethodPreference pref = getInputMethodPreference(imi, N);
            mInputMethodPreferenceList.add(pref);
        }
		
		for(int i = 0; i < N; i++) {
			if(systemDefaultInputMethod.equals(mImis.get(i).getId())) {
				mIsIuniInputMethod = true;
			}
		}		

		if(mIsIuniInputMethod) {
			for(int i = 0; i < N; i ++) {
				if(systemDefaultInputMethod.equals(mImis.get(index[i]).getId())) {
		        	mInputMethodPreferenceList.set(0, getInputMethodPreference(mImis.get(index[i]), N));
				}
			}
			if(N > 1) {
				int count = 1;
				for(int i = 0; i < N; i++) {
					if(!systemDefaultInputMethod.equals(mImis.get(index[i]).getId())) {
		        	mInputMethodPreferenceList.set(count, getInputMethodPreference(mImis.get(index[i]), N));
					count++;
					}
				}
			}		
		}

        if (!mInputMethodPreferenceList.isEmpty()) {
			
            //Collections.sort(mInputMethodPreferenceList);
            for (int i = 0; i < N; ++i) {
                mKeyboardSettingsCategory.addPreference(mInputMethodPreferenceList.get(i));
            }
        }

        // Build hard keyboard and game controller preference categories.
        mIm = (InputManager)getActivity().getSystemService(Context.INPUT_SERVICE);
        updateInputDevices();

        // Spell Checker
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(getActivity(), SpellCheckersSettingsActivity.class);
        final SpellCheckersPreference scp = ((SpellCheckersPreference)findPreference(
                "spellcheckers_settings"));
        if (scp != null) {
            scp.setFragmentIntent(this, intent);
        }

        mHandler = new Handler();
        mSettingsObserver = new SettingsObserver(mHandler, getActivity());

        // Aurora <likai> add begin
        if (findPreference("spellcheckers_settings") != null) {
            getPreferenceScreen().removePreference(findPreference("spellcheckers_settings"));
        }
        //getPreferenceScreen().removePreference(findPreference("spellcheckers_settings"));
        mUserDictionaryPref = (AuroraPreferenceScreen) findPreference(KEY_USER_DICTIONARY_SETTINGS);
        if (mUserDictionaryPref != null) {
            getPreferenceScreen().removePreference(mUserDictionaryPref);
        }
        if (mHardKeyboardCategory != null) {
            getPreferenceScreen().removePreference(mHardKeyboardCategory);
        }
        if (findPreference("voice_category") != null) {
            getPreferenceScreen().removePreference(findPreference("voice_category"));
        }
        if (findPreference("pointer_settings_category") != null) {
            getPreferenceScreen().removePreference(findPreference("pointer_settings_category"));
        }
        if (mGameControllerCategory != null) {
            getPreferenceScreen().removePreference(mGameControllerCategory);
        }
        // Aurora <likai> add end
    }

    private void updateInputMethodSelectorSummary(int value) {
        String[] inputMethodSelectorTitles = getResources().getStringArray(
                R.array.input_method_selector_titles);
        if (inputMethodSelectorTitles.length > value) {
            mShowInputMethodSelectorPref.setSummary(inputMethodSelectorTitles[value]);
            mShowInputMethodSelectorPref.setValue(String.valueOf(value));
        }
    }

    private void updateUserDictionaryPreference(AuroraPreference userDictionaryPreference) {
        final Activity activity = getActivity();
        final TreeSet<String> localeList = UserDictionaryList.getUserDictionaryLocalesSet(activity);
        if (null == localeList) {
            // The locale list is null if and only if the user dictionary service is
            // not present or disabled. In this case we need to remove the preference.
            getPreferenceScreen().removePreference(userDictionaryPreference);
        } else if (localeList.size() <= 1) {
            final Intent intent =
                    new Intent(UserDictionaryList.USER_DICTIONARY_SETTINGS_INTENT_ACTION);
            userDictionaryPreference.setTitle(R.string.user_dict_single_settings_title);
            userDictionaryPreference.setIntent(intent);
            userDictionaryPreference.setFragment(
                    com.android.settings.UserDictionarySettings.class.getName());
            // If the size of localeList is 0, we don't set the locale parameter in the
            // extras. This will be interpreted by the UserDictionarySettings class as
            // meaning "the current locale".
            // Note that with the current code for UserDictionaryList#getUserDictionaryLocalesSet()
            // the locale list always has at least one element, since it always includes the current
            // locale explicitly. @see UserDictionaryList.getUserDictionaryLocalesSet().
            if (localeList.size() == 1) {
                final String locale = (String)localeList.toArray()[0];
                userDictionaryPreference.getExtras().putString("locale", locale);
            }
        } else {
            userDictionaryPreference.setTitle(R.string.user_dict_multiple_settings_title);
            userDictionaryPreference.setFragment(UserDictionaryList.class.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mSettingsObserver.resume();
        mIm.registerInputDeviceListener(this, null);

        if (!mIsOnlyImeSettings) {
            if (mLanguagePref != null) {
                Configuration conf = getResources().getConfiguration();
                String language = conf.locale.getLanguage();
                String localeString;
                // TODO: This is not an accurate way to display the locale, as it is
                // just working around the fact that we support limited dialects
                // and want to pretend that the language is valid for all locales.
                // We need a way to support languages that aren't tied to a particular
                // locale instead of hiding the locale qualifier.
                if (hasOnlyOneLanguageInstance(language,
                        Resources.getSystem().getAssets().getLocales())) {
                    localeString = conf.locale.getDisplayLanguage(conf.locale);
                } else {
                    localeString = conf.locale.getDisplayName(conf.locale);
                }
                if (localeString.length() > 1) {
                    localeString = Character.toUpperCase(localeString.charAt(0))
                            + localeString.substring(1);
                    // Aurora <likai> modify begin
                    //mLanguagePref.setSummary(localeString);
                    mLanguagePref.auroraSetArrowText(localeString);
                    // Aurora <likai> modify end
                }
            }

            // Aurora <likai> modify begin
            //updateUserDictionaryPreference(findPreference(KEY_USER_DICTIONARY_SETTINGS));
            updateUserDictionaryPreference(mUserDictionaryPref);
            // Aurora <likai> modify end
            if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
                mShowInputMethodSelectorPref.setOnPreferenceChangeListener(this);
            }
        }

        // Hard keyboard
        if (!mHardKeyboardPreferenceList.isEmpty()) {
            for (int i = 0; i < sHardKeyboardKeys.length; ++i) {
            	AuroraSwitchPreference chkPref = (AuroraSwitchPreference)
                        mHardKeyboardCategory.findPreference(sHardKeyboardKeys[i]);
            	chkPref.setOnPreferenceChangeListener(this);
                chkPref.setChecked(
                        System.getInt(getContentResolver(), sSystemSettingNames[i], 1) > 0);
            }
        }

        updateInputDevices();

        // IME
        InputMethodAndSubtypeUtil.loadInputMethodSubtypeList(
                this, getContentResolver(), mImis, null);
        updateActiveInputMethodsSummary();
    }

    @Override
    public void onPause() {
        super.onPause();

        mIm.unregisterInputDeviceListener(this);
        mSettingsObserver.pause();

        if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
            mShowInputMethodSelectorPref.setOnPreferenceChangeListener(null);
        }
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(
                this, getContentResolver(), mImis, !mHardKeyboardPreferenceList.isEmpty());
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        updateInputDevices();
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        updateInputDevices();
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        updateInputDevices();
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        // Input Method stuff
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (preference instanceof AuroraPreferenceScreen) {
            if (preference.getFragment() != null) {
                // Fragment will be handled correctly by the super class.
            } else if (KEY_CURRENT_INPUT_METHOD.equals(preference.getKey())) {
                // Aurora <likai> modify begin
                
                /*final InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showInputMethodPicker();*/
                
                Intent intent = new Intent(getActivity(), InputMethodPickerActivity.class);
                startActivityForResult(intent, 13);
            	// Aurora <likai> modify end
            }
        } else if (preference instanceof AuroraCheckBoxPreference) {
            final AuroraCheckBoxPreference chkPref = (AuroraCheckBoxPreference) preference;
//            if (!mHardKeyboardPreferenceList.isEmpty()) {
//                for (int i = 0; i < sHardKeyboardKeys.length; ++i) {
//                    if (chkPref == mHardKeyboardCategory.findPreference(sHardKeyboardKeys[i])) {
//                        System.putInt(getContentResolver(), sSystemSettingNames[i],
//                                chkPref.isChecked() ? 1 : 0);
//                        return true;
//                    }
//                }
//            }
            if (chkPref == mGameControllerCategory.findPreference("vibrate_input_devices")) {
                System.putInt(getContentResolver(), Settings.System.VIBRATE_INPUT_DEVICES,
                        chkPref.isChecked() ? 1 : 0);
                return true;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean hasOnlyOneLanguageInstance(String languageCode, String[] locales) {
        int count = 0;
        for (String localeCode : locales) {
            if (localeCode.length() > 2
                    && localeCode.startsWith(languageCode)) {
                count++;
                if (count > 1) {
                    return false;
                }
            }
        }
        return count == 1;
    }

    private void saveInputMethodSelectorVisibility(String value) {
        try {
            int intValue = Integer.valueOf(value);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.INPUT_METHOD_SELECTOR_VISIBILITY, intValue);
            updateInputMethodSelectorSummary(intValue);
        } catch(NumberFormatException e) {
        }
    }

    private int loadInputMethodSelectorVisibility() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.INPUT_METHOD_SELECTOR_VISIBILITY,
                mDefaultInputMethodSelectorVisibility);
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object value) {
    	boolean checked = ((Boolean)value).booleanValue();
        if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
            if (preference == mShowInputMethodSelectorPref) {
                if (value instanceof String) {
                    saveInputMethodSelectorVisibility((String)value);
                }
            }
        } else if(sHardKeyboardKeys[0].equals(preference.getKey())) {
        	System.putInt(getContentResolver(), sSystemSettingNames[0], checked ? 1 : 0);
            return true;
        } else if(sHardKeyboardKeys[1].equals(preference.getKey())) {
        	System.putInt(getContentResolver(), sSystemSettingNames[1], checked ? 1 : 0);
            return true;
        } else if(sHardKeyboardKeys[2].equals(preference.getKey())) {
        	System.putInt(getContentResolver(), sSystemSettingNames[2], checked ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateActiveInputMethodsSummary() {
        for (AuroraPreference pref : mInputMethodPreferenceList) {
            if (pref instanceof InputMethodPreference) {
                ((InputMethodPreference)pref).updateSummary();
            }
        }
        updateCurrentImeName();
    }

    private void updateCurrentImeName() {
        final Context context = getActivity();
        if (context == null || mImm == null) return;
        final AuroraPreference curPref = getPreferenceScreen().findPreference(KEY_CURRENT_INPUT_METHOD);
        if (curPref != null) {
            final CharSequence curIme = InputMethodAndSubtypeUtil.getCurrentInputMethodName(
                    context, getContentResolver(), mImm, mImis, getPackageManager());
            if (!TextUtils.isEmpty(curIme)) {
                synchronized(this) {
                    // Aurora <likai> modify begin
                    //curPref.setSummary(curIme);
                	curPref.auroraSetArrowText(curIme.toString());
                    // Aurora <likai> modify end
                }
            }
        }
    }

    private InputMethodPreference getInputMethodPreference(InputMethodInfo imi, int imiSize) {
        final PackageManager pm = getPackageManager();
        final CharSequence label = imi.loadLabel(pm);
        // IME settings
        final Intent intent;
        final String settingsActivity = imi.getSettingsActivity();
        if (!TextUtils.isEmpty(settingsActivity)) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(imi.getPackageName(), settingsActivity);
        } else {
            intent = null;
        }

        // Add a check box for enabling/disabling IME
        InputMethodPreference pref = new InputMethodPreference(this, intent, mImm, imi, imiSize);
        pref.setKey(imi.getId());
        pref.setTitle(label);
        return pref;
    }

    private void updateInputDevices() {
        updateHardKeyboards();
        updateGameControllers();
    }

    private void updateHardKeyboards() {
        mHardKeyboardPreferenceList.clear();
        if (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY) {
            final int[] devices = InputDevice.getDeviceIds();
            for (int i = 0; i < devices.length; i++) {
                InputDevice device = InputDevice.getDevice(devices[i]);
                if (device != null
                        && !device.isVirtual()
                        && device.isFullKeyboard()) {
                    final String inputDeviceDescriptor = device.getDescriptor();
                    final String keyboardLayoutDescriptor =
                            mIm.getCurrentKeyboardLayoutForInputDevice(inputDeviceDescriptor);
                    final KeyboardLayout keyboardLayout = keyboardLayoutDescriptor != null ?
                            mIm.getKeyboardLayout(keyboardLayoutDescriptor) : null;

                    final AuroraPreferenceScreen pref = new AuroraPreferenceScreen(getActivity(), null);
                    pref.setTitle(device.getName());
                    if (keyboardLayout != null) {
                        pref.setSummary(keyboardLayout.toString());
                    } else {
                        pref.setSummary(R.string.keyboard_layout_default_label);
                    }
                    pref.setOnPreferenceClickListener(new AuroraPreference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(AuroraPreference preference) {
                            showKeyboardLayoutDialog(inputDeviceDescriptor);
                            return true;
                        }
                    });
                    mHardKeyboardPreferenceList.add(pref);
                }
            }
        }

        if (!mHardKeyboardPreferenceList.isEmpty()) {
            for (int i = mHardKeyboardCategory.getPreferenceCount(); i-- > 0; ) {
                final AuroraPreference pref = mHardKeyboardCategory.getPreference(i);
                if (pref.getOrder() < 1000) {
                    mHardKeyboardCategory.removePreference(pref);
                }
            }

            Collections.sort(mHardKeyboardPreferenceList);
            final int count = mHardKeyboardPreferenceList.size();
            for (int i = 0; i < count; i++) {
                final AuroraPreference pref = mHardKeyboardPreferenceList.get(i);
                pref.setOrder(i);
                mHardKeyboardCategory.addPreference(pref);
            }

            getPreferenceScreen().addPreference(mHardKeyboardCategory);
        } else {
            getPreferenceScreen().removePreference(mHardKeyboardCategory);
        }
    }

    private void showKeyboardLayoutDialog(String inputDeviceDescriptor) {
        KeyboardLayoutDialogFragment fragment =
                new KeyboardLayoutDialogFragment(inputDeviceDescriptor);
        fragment.setTargetFragment(this, 0);
        fragment.show(getActivity().getFragmentManager(), "keyboardLayout");
    }

    @Override
    public void onSetupKeyboardLayouts(String inputDeviceDescriptor) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(getActivity(), KeyboardLayoutPickerActivity.class);
        intent.putExtra(KeyboardLayoutPickerFragment.EXTRA_INPUT_DEVICE_DESCRIPTOR,
                inputDeviceDescriptor);
        mIntentWaitingForResult = intent;
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mIntentWaitingForResult != null) {
            String inputDeviceDescriptor = mIntentWaitingForResult.getStringExtra(
                    KeyboardLayoutPickerFragment.EXTRA_INPUT_DEVICE_DESCRIPTOR);
            mIntentWaitingForResult = null;
            showKeyboardLayoutDialog(inputDeviceDescriptor);
        }
    }

    private void updateGameControllers() {
        if (haveInputDeviceWithVibrator()) {
            getPreferenceScreen().addPreference(mGameControllerCategory);

            AuroraCheckBoxPreference chkPref = (AuroraCheckBoxPreference)
                    mGameControllerCategory.findPreference("vibrate_input_devices");
            chkPref.setChecked(System.getInt(getContentResolver(),
                    Settings.System.VIBRATE_INPUT_DEVICES, 1) > 0);
        } else {
            getPreferenceScreen().removePreference(mGameControllerCategory);
        }
    }

    private boolean haveInputDeviceWithVibrator() {
        final int[] devices = InputDevice.getDeviceIds();
        for (int i = 0; i < devices.length; i++) {
            InputDevice device = InputDevice.getDevice(devices[i]);
            if (device != null && !device.isVirtual() && device.getVibrator().hasVibrator()) {
                return true;
            }
        }
        return false;
    }

    private class SettingsObserver extends ContentObserver {
        private Context mContext;

        public SettingsObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        @Override public void onChange(boolean selfChange) {
            updateCurrentImeName();
        }

        public void resume() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD), false, this);
            cr.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE), false, this);
        }

        public void pause() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
}
