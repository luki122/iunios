package com.aurora.puremanager.permission;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

/**
 * @author: Gionee-lihq
 * @see: 2013-1-10 Change List:
 */
public abstract class BaseActivity extends Activity implements PackageChangedCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
//	    setTheme(R.style.GnSettingsLightTheme);
        super.onCreate(savedInstanceState);
//        setActionBar();
    }
    
    @Override
    protected void onStart(){
        super.onStart();
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    protected void setActionBar() {
        ActionBar bar = getActionBar();
        bar.setTitle(getTitle());
        bar.setDisplayShowTitleEnabled(true); 
        bar.setDisplayHomeAsUpEnabled(true);  
        bar.setDisplayShowHomeEnabled(true); 

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
       // unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    protected boolean getHasBackOption() {
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return  super.onKeyDown(keyCode, event);
    }

}
