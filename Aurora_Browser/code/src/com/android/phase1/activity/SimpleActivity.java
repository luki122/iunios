package com.android.phase1.activity;

import java.util.List;

import com.android.browser.BaseUi;
import com.android.browser.BrowserSettings;
import com.android.browser.R;
import com.android.browser.UrlUtils;
import com.android.browser.homepages.HomeProvider;
import com.android.phase1.model.ActivityMan;
import com.android.phase1.preference.AuroraPreferenceKeys;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraEditText;

public abstract class SimpleActivity extends AuroraPreferenceActivity implements AuroraPreference.OnPreferenceChangeListener {

    static final String BLANK_URL = "about:blank";
    static final String CURRENT = "current";
    static final String BLANK = "blank";
    static final String DEFAULT = "default";
    static final String MOST_VISITED = "most_visited";
    static final String OTHER = "other";
	
	public SimpleActivity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onPreferenceChange(AuroraPreference pref, Object objValue) {

		boolean b = changePreference(pref,objValue);
        BrowserSettings.executeSetup();

        return b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月30日 下午2:06:05 .
	 * @return
	 */
	public static String getCurrentPage() {
		return mCurrentPage;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午5:44:18 .
	 * @return
	 */
    String getHomepageValue() {
        BrowserSettings settings = BrowserSettings.getInstance();
        String homepage = settings.getHomePage();
        if (TextUtils.isEmpty(homepage) || BLANK_URL.endsWith(homepage)) {
            return BLANK;
        }
        if (HomeProvider.MOST_VISITED.equals(homepage)) {
            return MOST_VISITED;
        }
        String defaultHomepage = BrowserSettings.getFactoryResetHomeUrl(
                this);
        if (TextUtils.equals(defaultHomepage, homepage)) {
            return DEFAULT;
        }
        if (TextUtils.equals(mCurrentPage, homepage)) {
            return CURRENT;
        }
        return OTHER;
    }
	
    /**
     * 
     * Vulcan created this method in 2015年1月24日 下午5:44:36 .
     * @param pref
     */
    void promptForHomepage(final AuroraListPreference pref) {
        final BrowserSettings settings = BrowserSettings.getInstance();
        final AuroraEditText editText = new AuroraEditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_URI);
        editText.setText(settings.getHomePage());
        editText.setSelectAllOnFocus(true);
        editText.setSingleLine(true);
        editText.setImeActionLabel(null, EditorInfo.IME_ACTION_DONE);
        final AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String homepage = editText.getText().toString().trim();
                        homepage = UrlUtils.smartUrlFilter(homepage);
                        settings.setHomePage(homepage);
                        pref.setValue(getHomepageValue());
                        //pref.setSummary(getHomepageSummary());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setTitle(R.string.pref_set_homepage_to)
                .create();
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }
    

	
	/**
	 * 
	 * Vulcan created this method in 2015年1月20日 下午6:00:29 .
	 */
	public boolean changePreference(AuroraPreference pref, Object objValue) {
		
		
        if (pref.getKey().equals(AuroraPreferenceKeys.PREF_HOMEPAGE_PICKER)) {
            BrowserSettings settings = BrowserSettings.getInstance();
            if (CURRENT.equals(objValue)) {
                settings.setHomePage(mCurrentPage);
            }
            if (BLANK.equals(objValue)) {
                settings.setHomePage(BLANK_URL);
            }
            if (DEFAULT.equals(objValue)) {
                settings.setHomePage(BrowserSettings.getFactoryResetHomeUrl(null));
            }
            if (MOST_VISITED.equals(objValue)) {
                settings.setHomePage(HomeProvider.MOST_VISITED);
            }
            if (OTHER.equals(objValue)) {
                promptForHomepage((AuroraListPreference) pref);
                return true;
            }
            //pref.setSummary(getHomepageSummary());
            ((AuroraListPreference)pref).setValue(getHomepageValue());
            return true;
        }
        else if(pref.getKey().equals(AuroraPreferenceKeys.PREF_SAVE_FORMDATA)
        		|| pref.getKey().equals(AuroraPreferenceKeys.PREF_REMEMBER_PASSWORDS)) {
            AuroraSwitchPreference lp = (AuroraSwitchPreference) pref;
            lp.setChecked((Boolean)objValue);
        }
        else if (pref.getKey().equals(AuroraPreferenceKeys.PREF_SEARCH_ENGINE)) {
            AuroraListPreference lp = (AuroraListPreference) pref;
            lp.setValue((String) objValue);
            return true;
        }
        else if(pref.getKey().equals(AuroraPreferenceKeys.PREF_TEXT_SIZE)) {
            AuroraListPreference lp = (AuroraListPreference) pref;
            lp.setValue((String) objValue);
            return true;
        }
        else if(pref.getKey().equals(AuroraPreferenceKeys.PREF_NO_PICTURE_MODE)) {
            AuroraSwitchPreference lp = (AuroraSwitchPreference) pref;
            lp.setChecked((Boolean)objValue);
            Log.d("vpref","lp.setChecked objValue = " + objValue);
        	return true;
        }
        else if(pref.getKey().equals(AuroraPreferenceKeys.PREF_DATA_PRELOAD)) {
            AuroraListPreference lp = (AuroraListPreference) pref;
            lp.setValue((String) objValue);
            return true;
        }
        else if(pref.getKey().equals(AuroraPreferenceKeys.PREF_CLEAR_DATA_INPUT_RECORD)
        	|| pref.getKey().equals(AuroraPreferenceKeys.PREF_CLEAR_DATA_BROWSE_RECORD)
        	|| pref.getKey().equals(AuroraPreferenceKeys.PREF_CLEAR_DATA_PASSWORD)
        	|| pref.getKey().equals(AuroraPreferenceKeys.PREF_CLEAR_DATA_BUFFERED_PAGE)
        	|| pref.getKey().equals(AuroraPreferenceKeys.PREF_CLEAR_DATA_COOKIES)
        	|| pref.getKey().equals(AuroraPreferenceKeys.PREF_CLEAR_DATA_GEO_AUTHORIZATION)) {
            AuroraCheckBoxPreference lp = (AuroraCheckBoxPreference) pref;
            lp.setChecked((Boolean)objValue);
            
            if(!isPrefClearInputRecordChecked()
            	&& !isPrefClearBrowseRecordChecked()
            	&& !isPrefClearPasswordChecked()
            	&& !isPrefClearBufferedPageChecked()
            	&& !isPrefClearCookiesChecked()
            	&& !isPrefClearGeoAuthorizationChecked()) {
            	@SuppressWarnings("deprecation")
            	AuroraPreference prefDialog = (AuroraPreference) getPreferenceManager()
        				.findPreference(
        						AuroraPreferenceKeys.PREF_DIALOG_CLEAR_DATA);
            	prefDialog.setEnabled(false);
            }
            else {
            	@SuppressWarnings("deprecation")
            	AuroraPreference prefDialog = (AuroraPreference) getPreferenceManager()
        				.findPreference(
        						AuroraPreferenceKeys.PREF_DIALOG_CLEAR_DATA);
            	prefDialog.setEnabled(true);
            }
            
            return true;
        }
        //PREF_CLEAR_DATA_BROWSE_RECORD
        return true;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:02:12 .
	 * @return
	 */
	public boolean isPrefClearInputRecordChecked() {
		@SuppressWarnings("deprecation")
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_INPUT_RECORD);
		return prefClearInputRecord.isChecked();
	}

	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:03:25 .
	 * @return
	 */
	public boolean isPrefClearBrowseRecordChecked() {
		@SuppressWarnings("deprecation")
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_BROWSE_RECORD);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:03:40 .
	 * @return
	 */
	public boolean isPrefClearPasswordChecked() {
		@SuppressWarnings("deprecation")
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_PASSWORD);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:04:09 .
	 * @return
	 */
	public boolean isPrefClearBufferedPageChecked() {
		@SuppressWarnings("deprecation")
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_BUFFERED_PAGE);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:04:14 .
	 * @return
	 */
	public boolean isPrefClearCookiesChecked() {
		@SuppressWarnings("deprecation")
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_COOKIES);
		return prefClearInputRecord.isChecked();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月24日 下午2:04:14 .
	 * @return
	 */
	public boolean isPrefClearGeoAuthorizationChecked() {
		@SuppressWarnings("deprecation")
		AuroraCheckBoxPreference prefClearInputRecord = (AuroraCheckBoxPreference) getPreferenceManager()
				.findPreference(
						AuroraPreferenceKeys.PREF_CLEAR_DATA_GEO_AUTHORIZATION);
		return prefClearInputRecord.isChecked();
	}

	
	/**
	 * 
	 * Vulcan created this method in 2015年1月20日 下午2:51:30 .
	 */
	protected void setupPreferences() {
        List<String> keysPreference = getKeysPreference();
        for(String key: keysPreference) {
    		@SuppressWarnings("deprecation")
			AuroraPreference e = getPreferenceScreen().findPreference(key);
    		if(e != null) {
    			e.setOnPreferenceChangeListener(this);
    		}
        }
	}
	


	protected abstract List<String> getKeysPreference();
	protected abstract void restorePreference();
	protected static String mCurrentPage;//The URL of current page. Every time setting page is started, this variable is updated.

	/* (non-Javadoc)
	 * @see aurora.preference.AuroraPreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		ActivityMan.getInstance().addSimpleActivity(this);
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月30日 下午2:35:56 .
	 */
	public static void restorePreferences() {
		List<SimpleActivity> list = ActivityMan.getInstance().getActivities();
		for(SimpleActivity sa: list) {
			sa.restorePreference();
		}
	}
	
	@Override
	protected void onResume() {
		BaseUi.changeStatusBar(this,true);
		super.onResume();
	}

}
