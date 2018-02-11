/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.ListFragment;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AuroraLocalePicker extends ListFragment {
    private static final String TAG = "AuroraLocalePicker";
    private static final boolean DEBUG = false;

    public static interface LocaleSelectionListener {
        // You can add any argument if you really need it...
        public void onLocaleSelected(Locale locale);
    }

    LocaleSelectionListener mListener;  // default to null

    public static class LocaleInfo implements Comparable<LocaleInfo> {
        static final Collator sCollator = Collator.getInstance();

        String label;
        Locale locale;

        public LocaleInfo(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }

        public String getLabel() {
            return label;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            return this.label;
        }

        @Override
        public int compareTo(LocaleInfo another) {
            return sCollator.compare(this.label, another.label);
        }
    }

    /**
     * Constructs an Adapter object containing Locale information. Content is sorted by
     * {@link LocaleInfo#label}.
     */
    public static ArrayAdapter<LocaleInfo> constructAdapter(Context context) {
    	return constructAdapter(context, R.layout.simple_list_item, android.R.id.text1);
    }

    public static ArrayAdapter<LocaleInfo> constructAdapter(Context context,
            final int layoutId, final int fieldId) {
        final Resources resources = context.getResources();
        final String[] locales = Resources.getSystem().getAssets().getLocales();
        final String[] specialLocaleCodes = resources.getStringArray(R.array.special_locale_codes);
        final String[] specialLocaleNames = resources.getStringArray(R.array.special_locale_names);
        Arrays.sort(locales);
        final int origSize = locales.length;
        final LocaleInfo[] preprocess = new LocaleInfo[origSize];
        int finalSize = 0;
        for (int i = 0 ; i < origSize; i++ ) {
            final String s = locales[i];
            final int len = s.length();
            if (len == 5) {
                String language = s.substring(0, 2);
                String country = s.substring(3, 5);
                final Locale l = new Locale(language, country);

                if (finalSize == 0) {
                    if (DEBUG) {
                        Log.v(TAG, "adding initial "+ toTitleCase(l.getDisplayLanguage(l)));
                    }
                    preprocess[finalSize++] =
                            new LocaleInfo(toTitleCase(l.getDisplayLanguage(l)), l);
                } else {
                    // check previous entry:
                    //  same lang and a country -> upgrade to full name and
                    //    insert ours with full name
                    //  diff lang -> insert ours with lang-only name
                    if (preprocess[finalSize-1].locale.getLanguage().equals(
                            language)) {
                        if (DEBUG) {
                            Log.v(TAG, "backing up and fixing "+
                                    preprocess[finalSize-1].label+" to "+
                                    getDisplayName(preprocess[finalSize-1].locale,
                                            specialLocaleCodes, specialLocaleNames));
                        }
                        preprocess[finalSize-1].label = toTitleCase(
                                getDisplayName(preprocess[finalSize-1].locale,
                                        specialLocaleCodes, specialLocaleNames));
                        if (DEBUG) {
                            Log.v(TAG, "  and adding "+ toTitleCase(
                                    getDisplayName(l, specialLocaleCodes, specialLocaleNames)));
                        }
                        preprocess[finalSize++] =
                                new LocaleInfo(toTitleCase(
                                        getDisplayName(
                                                l, specialLocaleCodes, specialLocaleNames)), l);
                    } else {
                        String displayName;
                        if (s.equals("zz_ZZ")) {
                            displayName = "Pseudo...";
                        } else {
                            displayName = toTitleCase(l.getDisplayLanguage(l));
                        }
                        if (DEBUG) {
                            Log.v(TAG, "adding "+displayName);
                        }
                        preprocess[finalSize++] = new LocaleInfo(displayName, l);
                    }
                }
            }
        }
        // Modify begin by aurora.jiangmx
        /*
        final LocaleInfo[] localeInfos = new LocaleInfo[finalSize];
        
        for (int i = 0; i < finalSize; i++) {
        	
               localeInfos[i] = preprocess[i];
        }
        */
        // ----------div----------------
        final String[] lSupportLan = resources.getStringArray(R.array.iuni_support_language);
        final String[] lSupportCoutry = resources.getStringArray(R.array.iuni_support_country);
        List<Integer> lSuports = new ArrayList<Integer>();
        
        for(int i = 0; i < finalSize; i++){
            if( isInSupportLanguages(lSupportLan, preprocess[i].locale.getLanguage()) 
               && isInSupportCountry(lSupportCoutry, preprocess[i].locale.getCountry()) ){
               lSuports.add(i);
            }
        }
        
        final LocaleInfo[] localeInfos = new LocaleInfo[lSuports.size()];
        for(int i = 0; i < lSuports.size(); i++){
        	localeInfos[i] = preprocess[lSuports.get(i)];
        }
        // Modify end
        Arrays.sort(localeInfos);

        Configuration conf =  resources.getConfiguration();
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
        }

        for (int i = 0; i < localeInfos.length; i++) {
            if (localeString != null && localeString.equals(localeInfos[i])) {
                position = i;
                break;
            }
        }

        final LayoutInflater inflater =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ArrayAdapter<LocaleInfo>(context, layoutId, fieldId, localeInfos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                TextView text;
                if (convertView == null) {
                    view = inflater.inflate(layoutId, parent, false);
                    text = (TextView) view.findViewById(fieldId);
                    view.setTag(text);
                } else {
                    view = convertView;
                    text = (TextView) view.getTag();
                }
                LocaleInfo item = getItem(position);
                text.setText(item.toString());
                text.setTextColor(Color.BLACK);
                text.setTextLocale(item.getLocale());

                return view;
            }
        };
    }

    // Add begin by aurora.jiangmx
    private static boolean isInSupportLanguages(String[] pSupportLan, String pLan){
    	for(int i =0; i < pSupportLan.length; i++){
    		if(pSupportLan[i].equals(pLan))
    			return true;
    	}
    	return false;
    }
    
    private static boolean isInSupportCountry(String[] pSupportCountry, String pCountry){
    	for(int i =0; i < pSupportCountry.length; i++){
    		if(pSupportCountry[i].equals(pCountry))
    			return true;
    	}
    	return false;
    }
	// Add end
    
    private static boolean hasOnlyOneLanguageInstance(String languageCode, String[] locales) {
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

    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String getDisplayName(
            Locale l, String[] specialLocaleCodes, String[] specialLocaleNames) {
        String code = l.toString();

        for (int i = 0; i < specialLocaleCodes.length; i++) {
            if (specialLocaleCodes[i].equals(code)) {
                return specialLocaleNames[i];
            }
        }

        return l.getDisplayName(l);
    }

    private static int position;
    
    

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
    	final View view = inflater.inflate(R.layout.aurora_language_picker_layout, container, false);
    	return view;

	}

	@Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ArrayAdapter<LocaleInfo> adapter = constructAdapter(getActivity());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListAdapter(adapter);
        getListView().setItemChecked(position, true);
    }

    public void setLocaleSelectionListener(LocaleSelectionListener listener) {
        mListener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().requestFocus();
    }

    /**
     * Each listener needs to call {@link #updateLocale(Locale)} to actually change the locale.
     *
     * We don't call {@link #updateLocale(Locale)} automatically, as it halt the system for
     * a moment and some callers won't want it.
     */
    Handler  upHandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		  final Locale locale = ((LocaleInfo)getListAdapter().getItem(position)).locale;
    		  mListener.onLocaleSelected(locale);
    	}
    	
    };
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener != null) {
            this.position = position;
//            final Locale locale = ((LocaleInfo)getListAdapter().getItem(position)).locale;
//            mListener.onLocaleSelected(locale);
       
       	 upHandler.sendEmptyMessageDelayed(0, 300);
  
            getListView().setItemChecked(position, true);
        }
    }

    /**
     * Requests the system to update the system locale. Note that the system looks halted
     * for a while during the Locale migration, so the caller need to take care of it.
     */
    public static void updateLocale(Locale locale) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();

            // Will set userSetLocale to indicate this isn't some passing default - the user
            // wants this remembered
            config.setLocale(locale);

            am.updateConfiguration(config);
            // Trigger the dirty bit for the Settings Provider.
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
            // Intentionally left blank
        }
    }
}
