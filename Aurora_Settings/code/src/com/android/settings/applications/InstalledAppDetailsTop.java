package com.android.settings.applications;

import android.content.Intent;
import aurora.preference.AuroraPreferenceActivity;
//Gionee fangbin 20120619 added for CR00622030 start
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.GnSettingsUtils;
//Gionee fangbin 20120619 added for CR00622030 end

public class InstalledAppDetailsTop extends AuroraPreferenceActivity {
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, InstalledAppDetails.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }
    
    // Gionee fangbin 20120619 added for CR00622030 start
    @Override
    protected void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120719 modified for CR00651589 end
        super.onCreate(icicle);
        
        //Gionee:zhang_xin 20121215 add for CR00746521 start
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
        /*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        getAuroraActionBar().setTitle(R.string.application_info_label);
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        //Gionee:zhang_xin 20121215 add for CR00746521 end
    }
    // Gionee fangbin 20120619 added for CR00622030 end
}
