package com.android.settings;

import java.io.File;




import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.widget.HeaderViewListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;

import java.lang.String;

public class AuroraScreenTimeoutPickerActivity extends AuroraActivity implements AdapterView.OnItemClickListener{
	private ListView mScreenTimeoutList; 
	private AuroraActionBar auroraActionBar;
	private  String[] mScreenTimeoutEntries;
    private  String[] mScreenTimeoutValues;
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 60000;
	 
    private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
			public void onAuroraActionBarBackItemClicked(int itemId) {
				switch (itemId) {
				case -1:
					// setMenuEnable(true);					
					finish();
					break;
				default:
					break;
				}
			}
		};

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			int value = Integer.parseInt(mScreenTimeoutValues[position]);
			try {
	            Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, value);
	            
	        } catch (NumberFormatException e) {	            
	            e.printStackTrace();
	        }
			finish();
		}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.aurora_screen_timeout_listview_layout,AuroraActionBar.Type.Normal);
		mScreenTimeoutEntries = getResources().getStringArray(R.array.aurora_screen_timeout_entries);
        mScreenTimeoutValues = getResources().getStringArray(R.array.aurora_screen_timeout_values);
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(getResources().getString(R.string.screen_timeout));  // title
		auroraActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);		
		
		ArrayAdapter adapter =new ArrayAdapter(this,R.layout.aurora_screen_timeout_listitem,mScreenTimeoutEntries); 
		 
        mScreenTimeoutList = (ListView) findViewById(R.id.ListView_screen_timeout);  
        
        mScreenTimeoutList.setAdapter(adapter);  
        mScreenTimeoutList.setOnItemClickListener(this);  
        mScreenTimeoutList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//.CHOICE_MODE_SINGLE);  
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mScreenTimeoutList.setItemChecked(getInitItemPosition(), true);
	}
	
	private int getInitItemPosition(){
		final int currentTimeout = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
		for(int i =0; i< mScreenTimeoutValues.length; i++){
			if(currentTimeout == Integer.parseInt(mScreenTimeoutValues[i])){
				return i;
			}
		}
		return -1;
	}
	
	 @Override
	 public void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	    }
	
	    @Override
	    protected void onStop() {
	        super.onStop();
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();  
	    }
	    
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub	    	
	    	
	    	switch (keyCode) {
        	case KeyEvent.KEYCODE_BACK:
        		finish();
        		return true;
        	
        	default:
    			return super.onKeyDown(keyCode, event);
	    	}
	    }
	    
}
