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

package com.android.settings.accounts;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.internal.util.CharSequences;
import com.android.settings.R;
import com.google.android.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


// Gionee fangbin 20120619 added for CR00622030 start
import com.android.settings.GnSettingsUtils;
// Gionee fangbin 20120619 added for CR00622030 end

// Gionee xiongjiaxin 2012-8-29 modified for CR00681080 start
import com.android.settings.R.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import aurora.widget.AuroraListView ;
// Gionee xiongjiaxin 2012-8-29 modified for CR00681080 end
import aurora.widget.AuroraActionBar;

/**
 * Activity asking a user to select an account to be set up.
 */
public class ChooseAccountActivity extends AuroraPreferenceActivity {

    private static final String TAG = "ChooseAccountActivity";
    private String[] mAuthorities;
    private AuroraPreferenceGroup mAddAccountGroup;
    private final ArrayList<ProviderEntry> mProviderList = new ArrayList<ProviderEntry>();
    public HashSet<String> mAccountTypesFilter;
    private AuthenticatorDescription[] mAuthDescs;
    private HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = null;
    private Map<String, AuthenticatorDescription> mTypeToAuthDescription
            = new HashMap<String, AuthenticatorDescription>();
    // qy 
    private TextView mEmptyText;
    private AuroraListView mAccountAuroraListView;
    private static class ProviderEntry implements Comparable<ProviderEntry> {
        private final CharSequence name;
        private final String type;
        ProviderEntry(CharSequence providerName, String accountType) {
            name = providerName;
            type = accountType;
        }

        public int compareTo(ProviderEntry another) {
            if (name == null) {
                return -1;
            }
            if (another.name == null) {
                return +1;
            }
            return CharSequences.compareToIgnoreCase(name, another.name);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120719 modified for CR00651589 end
        super.onCreate(icicle);
        Log.i("qy","*******ChooseAccountActivity");
        /*Gionee: huangsf 20121210 add for CR00741405 start*/
        //AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
        /*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
 	*/
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        /*Gionee: huangsf 20121210 add for CR00741405 end*/

        //
        //AURORA-START::delete temporarily for compile::waynelin::2013-9-18 
        //setContentView(R.layout.add_account_screen);
        setAuroraContentView(R.layout.add_account_screen,AuroraActionBar.Type.Normal);
        mEmptyText = (TextView)findViewById(R.id.empty_text);
        mAccountAuroraListView = (AuroraListView)findViewById(android.R.id.list);
        getAuroraActionBar().setTitle(R.string.header_add_an_account);
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
	//AURORA-END::delete temporarily for compile::waynelin::2013-9-18

        addPreferencesFromResource(R.xml.add_account_settings);
        mAuthorities = getIntent().getStringArrayExtra(
                AccountPreferenceBase.AUTHORITIES_FILTER_KEY);
        String[] accountTypesFilter = getIntent().getStringArrayExtra(
                AccountPreferenceBase.ACCOUNT_TYPES_FILTER_KEY);
        if (accountTypesFilter != null) {
            mAccountTypesFilter = new HashSet<String>();
            for (String accountType : accountTypesFilter) {
                mAccountTypesFilter.add(accountType);
            }
        }
        mAddAccountGroup = getPreferenceScreen();
        updateAuthDescriptions();
    }

    /*Gionee: huangsf 20121210 add for CR00741405 start*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    /*Gionee: huangsf 20121210 add for CR00741405 end*/
    
    /**
     * Updates provider icons. Subclasses should call this in onCreate()
     * and update any UI that depends on AuthenticatorDescriptions in onAuthDescriptionsUpdated().
     */
    private void updateAuthDescriptions() {
        mAuthDescs = AccountManager.get(this).getAuthenticatorTypes();
        for (int i = 0; i < mAuthDescs.length; i++) {
            mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
        }
        onAuthDescriptionsUpdated();
    }

    private void onAuthDescriptionsUpdated() {
        // Create list of providers to show on preference screen
        for (int i = 0; i < mAuthDescs.length; i++) {
            String accountType = mAuthDescs[i].type;
            CharSequence providerName = getLabelForType(accountType);

            // Skip preferences for authorities not specified. If no authorities specified,
            // then include them all.
            ArrayList<String> accountAuths = getAuthoritiesForAccountType(accountType);
            boolean addAccountPref = true;
            if (mAuthorities != null && mAuthorities.length > 0 && accountAuths != null) {
                addAccountPref = false;
                for (int k = 0; k < mAuthorities.length; k++) {
                    if (accountAuths.contains(mAuthorities[k])) {
                        addAccountPref = true;
                        break;
                    }
                }
            }
            if (addAccountPref && mAccountTypesFilter != null
                    && !mAccountTypesFilter.contains(accountType)) {
                addAccountPref = false;
            }
            if (addAccountPref) {
                mProviderList.add(new ProviderEntry(providerName, accountType));
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Skipped pref " + providerName + ": has no authority we need");
                }
            }
        }
        // qy modify
        if(mProviderList.size() == 0){
        	mEmptyText.setVisibility(View.VISIBLE);
        	mAccountAuroraListView.setVisibility(View.GONE);
        }
       /* if (mProviderList.size() == 1) {
            // If there's only one provider that matches, just run it.
            finishWithAccountType(mProviderList.get(0).type);
        } */  // end
        else if (mProviderList.size() > 0) {
            Collections.sort(mProviderList);
            mAddAccountGroup.removeAll();
            for (ProviderEntry pref : mProviderList) {
                Drawable drawable = getDrawableForType(pref.type);
                ProviderPreference p =
                        new ProviderPreference(this, pref.type, drawable, pref.name);
                mAddAccountGroup.addPreference(p);
            }
        } else {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                final StringBuilder auths = new StringBuilder();
                for (String a : mAuthorities) {
                    auths.append(a);
                    auths.append(' ');
                }
                Log.v(TAG, "No providers found for authorities: " + auths);
            }
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        if (mAccountTypeToAuthorities == null) {
            mAccountTypeToAuthorities = Maps.newHashMap();
            SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypes();
            for (int i = 0, n = syncAdapters.length; i < n; i++) {
                final SyncAdapterType sa = syncAdapters[i];
                ArrayList<String> authorities = mAccountTypeToAuthorities.get(sa.accountType);
                if (authorities == null) {
                    authorities = new ArrayList<String>();
                    mAccountTypeToAuthorities.put(sa.accountType, authorities);
                }
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.d(TAG, "added authority " + sa.authority + " to accountType "
                            + sa.accountType);
                }
                authorities.add(sa.authority);
            }
        }
        return mAccountTypeToAuthorities.get(type);
    }

    /**
     * Gets an icon associated with a particular account type. If none found, return null.
     * @param accountType the type of account
     * @return a drawable for the icon or null if one cannot be found.
     */
    protected Drawable getDrawableForType(final String accountType) {
    	// Gionee xiongjiaxin 2012-8-29 modified for CR00681080 start
    	Drawable tmpIconDrawable = null;
    	// Gionee xiongjiaxin 2012-8-29 modified for CR00681080 end
        Drawable icon = null;
        if (mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                Context authContext = createPackageContext(desc.packageName, 0);
                // Gionee xiongjiaxin 2012-8-29 modified for CR00681080 start
                //icon = authContext.getResources().getDrawable(desc.iconId);
                tmpIconDrawable = authContext.getResources().getDrawable(desc.iconId);
                int icon_width = (int)(this.getBaseContext().getResources().getDimension(R.dimen.icon_width));
                int icon_height = (int)(this.getBaseContext().getResources().getDimension(R.dimen.icon_height));
                Log.d(TAG, "icon_height = " + icon_height);
                Log.d(TAG, "icon_width = " + icon_width);
                icon = zoomDrawable(tmpIconDrawable, icon_width, icon_height);
                // Gionee xiongjiaxin 2012-8-29 modified for CR00681080 end
            } catch (PackageManager.NameNotFoundException e) {
                // TODO: place holder icon for missing account icons?
                Log.w(TAG, "No icon name for account type " + accountType);
            } catch (Resources.NotFoundException e) {
                // TODO: place holder icon for missing account icons?
                Log.w(TAG, "No icon resource for account type " + accountType);
            }
        }
        return icon;
    }

    // Gionee xiongjiaxin 2012-8-29 modified for CR00681080 start
	Bitmap drawableToBitmap(Drawable drawable)
	{
		int width = drawable.getIntrinsicWidth(); 	
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; 			
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap); 
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); 
		return bitmap;
	}

	Drawable zoomDrawable(Drawable drawable, int w, int h)
    {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable);
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);

		//modify by steve.tang 2014-06-19. new BitmapDrawable(newbmp)not dealing with density. start
		//use BitmapDrawable(Resources res, Bitmap bitmap) instead
		//return new BitmapDrawable(newbmp);
		return new BitmapDrawable(ChooseAccountActivity.this.getResources(), newbmp);
		//Modify by steve.tang 2014-06019 end
    }
	// Gionee xiongjiaxin 2012-8-29 modified for CR00681080 end

    /**
     * Gets the label associated with a particular account type. If none found, return null.
     * @param accountType the type of account
     * @return a CharSequence for the label or null if one cannot be found.
     */
    protected CharSequence getLabelForType(final String accountType) {
        CharSequence label = null;
        if (mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                Context authContext = createPackageContext(desc.packageName, 0);
                label = authContext.getResources().getText(desc.labelId);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "No label name for account type " + accountType);
            } catch (Resources.NotFoundException e) {
                Log.w(TAG, "No label resource for account type " + accountType);
            }
        }
        return label;
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferences, AuroraPreference preference) {
        if (preference instanceof ProviderPreference) {
            ProviderPreference pref = (ProviderPreference) preference;
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Attempting to add account of type " + pref.getAccountType());
            }
            finishWithAccountType(pref.getAccountType());
        }
        return true;
    }

    private void finishWithAccountType(String accountType) {
        Intent intent = new Intent();
        intent.putExtra(AddAccountSettings.EXTRA_SELECTED_ACCOUNT, accountType);
        setResult(RESULT_OK, intent);
        finish();
    }
}
