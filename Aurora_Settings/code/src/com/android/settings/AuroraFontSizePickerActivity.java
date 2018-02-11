package com.android.settings;

import java.io.File;







import android.app.ActivityManagerNative;
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
import android.os.RemoteException;
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
import android.content.res.Configuration;
import android.widget.HeaderViewListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;

import java.lang.String;
import java.util.Timer;
import java.util.TimerTask;

public class AuroraFontSizePickerActivity extends AuroraActivity implements AdapterView.OnItemClickListener{
	

	private ListView mFontSizeList; 
	private AuroraActionBar auroraActionBar;
	private  String[] mFontSizeEntries;
    private  String[] mFontSizeEntriesValues;
    private final Configuration mCurConfig = new Configuration();
	 
	 
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
			
			writeFontSizePreference(mFontSizeEntriesValues[position]);
			Timer t = new Timer();
			TimerTask task = new TimerTask(){  
				      public void run() {  
				    	 
				    	  finish();
				   }  
				 }; 
			t.schedule(task,200);
			
		}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.aurora_screen_timeout_listview_layout,AuroraActionBar.Type.Normal);
		
		mFontSizeEntries = getResources().getStringArray(R.array.entries_font_size);
        mFontSizeEntriesValues = getResources().getStringArray(R.array.entryvalues_font_size);
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(getResources().getString(R.string.title_font_size));  // title
		auroraActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);		
		
		ArrayAdapter adapter =new ArrayAdapter(this,R.layout.aurora_screen_timeout_listitem,mFontSizeEntries); 
		 
        mFontSizeList = (ListView) findViewById(R.id.ListView_screen_timeout);  
        
        mFontSizeList.setAdapter(adapter);  
        mFontSizeList.setOnItemClickListener(this);  
        mFontSizeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//.CHOICE_MODE_SINGLE);  
 
       
		
	}
	
	public void writeFontSizePreference(String value) {
        try {
            mCurConfig.fontScale = Float.parseFloat(value);
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            
        }
    }

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mFontSizeList.setItemChecked(getInitItemPosition(), true);
	}
	
	private int getInitItemPosition(){
		try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
           
        }
		float val = mCurConfig.fontScale;
		float lastVal = Float.parseFloat(mFontSizeEntriesValues[0]);
        for (int i=1; i<mFontSizeEntriesValues.length; i++) {
            float thisVal = Float.parseFloat(mFontSizeEntriesValues[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return mFontSizeEntriesValues.length-1;
	}
		
	
    
	 @Override
	 public void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
//	        outState.putInt(SAVE_CLICKED_POS, mClickedPos);
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
