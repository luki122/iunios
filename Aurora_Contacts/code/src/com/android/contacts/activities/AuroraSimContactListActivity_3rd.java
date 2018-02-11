package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.ContactsActivity;
import com.android.contacts.list.AuroraSimContactListAdapter_3rd;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.IntentFactory;
import com.gionee.android.contacts.SimContactsService;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simcontact.StartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService2;

import aurora.widget.AuroraListView;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraActivity;

/**
 * aurora:wangth 20140826 add
 */

public class AuroraSimContactListActivity_3rd extends AuroraActivity {
    private static final String TAG = "AuroraSimContactListActivity_3rd";
    
    private static AuroraActionBar mActionBar;
    private Context mContext;
    
    private AuroraSimContactListAdapter_3rd mAdapter;
    private ArrayList<String> mNamesArray = new ArrayList<String>();
    private ArrayList<String> mPhoneNumberArray = new ArrayList<String>();
    
    private AuroraListView mListView;
    private View mLoadingContainer;
    private TextView mLoadingContact;
    private ProgressBar mProgress;
    private TextView mNoContactsEmptyView;
    
    private static String SIM_CONTACTS_LOADED_ACTION = "aurora.sim.contacts.loaded";
    private BroadcastReceiver mReceiver = new SimContactsLoadedBroadcastReceiver();
    
    private boolean isFinished = false;
    private static final int WAIT_CURSOR_START = 1230;
    private static final long WAIT_CURSOR_DELAY_TIME = 500;
    private static int QUERY_TIMEOUT = 120;

    private ContactsUtils.AuroraContactsProgressDialog mSaveProgressDialog = null;
    private static final int START = 0;
    private static final int END = 1;
    private static final int ERROR = 2;
    
    private int mSlot = 0;
    
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            case WAIT_CURSOR_START: {
            	Log.i(TAG, "start WAIT_CURSOR_START !isFinished : "
                        + !isFinished);
                if (!isFinished) {
                    mLoadingContainer.setVisibility(View.VISIBLE);
                    mLoadingContact.setVisibility(View.VISIBLE);
                    mProgress.setVisibility(View.VISIBLE);
                } else {
                    mLoadingContainer.setVisibility(View.GONE);
                    mLoadingContact.setVisibility(View.GONE);
                    mProgress.setVisibility(View.GONE);
                    
                    if (mNamesArray != null && mNamesArray.size() < 1) {
                    	mNoContactsEmptyView.setVisibility(View.VISIBLE);
                    } else {
                    	mNoContactsEmptyView.setVisibility(View.GONE);
                    	mAdapter = new AuroraSimContactListAdapter_3rd(mContext, mNamesArray, mPhoneNumberArray);
                        mListView.setAdapter(mAdapter);
                    }
                }
                
            	break;
            }
            
            case START: {
                if (!isFinishing()) {
                    if (null == mSaveProgressDialog) {
                        mSaveProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
                    }
                    mSaveProgressDialog.setTitle(R.string.aurora_save_group_dialog_title);
                    mSaveProgressDialog.setIndeterminate(false);
                    mSaveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    try {
                        mSaveProgressDialog.show();
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            
            case END: {
                if (!isFinishing() 
                        && null != mSaveProgressDialog && mSaveProgressDialog.isShowing()) {
                    try {
                        mSaveProgressDialog.dismiss();
                        mSaveProgressDialog = null;
                        finish();
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            
            case ERROR: {
            	ContactsUtils.toastManager(mContext, R.string.aurora_sim_not_ready);
            	finish();
            	break;
            }
            
            }
            
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedState);

        mContext = AuroraSimContactListActivity_3rd.this;
        setAuroraMenuCallBack(auroraMenuCallBack);
        
        setAuroraContentView(R.layout.aurora_sim_contact_list_content_3rd,
                AuroraActionBar.Type.Normal);
        mActionBar = getAuroraActionBar();
    	mActionBar.setTitle(R.string.aurora_menu_sim_contacts);
    	
    	IntentFilter loadIntentFilter =
                new IntentFilter(SIM_CONTACTS_LOADED_ACTION);
            this.registerReceiver(mReceiver, loadIntentFilter);
            
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	mSlot = extras.getInt("slot");
        	
        	if (FeatureOption.MTK_GEMINI_SUPPORT) {
        		if (mSlot == 0) {
        			mActionBar.setTitle(R.string.aurora_menu_sim1_contacts);
        		} else if (mSlot == 1) {
        			mActionBar.setTitle(R.string.aurora_menu_sim2_contacts);
        		}
        	}
        }
            
    	initView();
    	startQuery();
    }
    
    private void initView() {
    	mNoContactsEmptyView = (TextView)findViewById(
                R.id.no_contacts);
    	
    	mLoadingContainer = findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.GONE);
        mLoadingContact = (TextView)findViewById(
                R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar)findViewById(
                R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);
        
        isFinished = false;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                WAIT_CURSOR_DELAY_TIME);
        
        mListView = (AuroraListView)findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
    }
    
    private void startQuery() {
    	new QueryThread(mSlot).start();
    }
    
    private class QueryThread extends Thread {
    	private int slot = 0;
    	
    	public QueryThread(int s) {
    		slot = s;
    	}
    	
    	@Override
        public void run() {
    		mContext.getMainLooper().prepare();
    		if (!SimCardUtils.isSimStateReady(slot)) {
	    		ContactsUtils.toastManager(mContext, R.string.aurora_sim_not_ready);
	    		finish();
	    		return;
	    	}
    		
    		if (GNContactsUtils.isOnlyQcContactsSupport()) {
    			sendQcSimState(slot);
    		} else {
    			sendMtkSimState(slot);
    		}
			
			int i = 0;
			try {
				while (true) {
					if (isFinished) {
						break;
					}
					
					if (i >= QUERY_TIMEOUT) {
						mHandler.sendEmptyMessage(ERROR);
						break;
					}
					
					Thread.sleep(1000);
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mContext.getMainLooper().loop();
    	}
    }
    
	private void sendQcSimState(int slot) {
		Bundle args = new Bundle();
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			args.putInt(GNContactsUtils.SUB, slot);
		}
		args.putInt(SimContactsService.OPERATION, GNContactsUtils.OP_SIM);
		args.putInt(SimContactsService.SIM_STATE, GNContactsUtils.SIM_STATE_READY);
		Intent intent = new Intent(mContext, SimContactsService.class).putExtras(args);
		mContext.stopService(intent);
		mContext.startService(intent);
	}
	
	private void sendMtkSimState(int slot) {
		Intent intent = null;
        if (slot == 0) {
            intent = new Intent(mContext, StartSIMService.class);
        } else if (slot == 1) {
            intent = new Intent(mContext, StartSIMService2.class);
        }
        
        intent.putExtra(AbstractStartSIMService.SERVICE_SLOT_KEY, slot);
        intent.putExtra(AbstractStartSIMService.SERVICE_WORK_TYPE, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        mContext.startService(intent);
	}
	
	private class SimContactsLoadedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SIM_CONTACTS_LOADED_ACTION)) {
            	isFinished = true;
            	mNamesArray = intent.getStringArrayListExtra("names");
            	mPhoneNumberArray = intent.getStringArrayListExtra("phones");
            	Log.d(TAG, "SimContactsLoadedBroadcastReceiver loading completed. mNamesArray " + mNamesArray);
            	
            	mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                        WAIT_CURSOR_DELAY_TIME);
            }
        }
    } 
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        
        unregisterReceiver(mReceiver);
    }
    
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.menu_add: {
                
                break;
            }
            }
        }
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            finish();
            break;
        }

        default: {
        }
        }

        return super.onKeyDown(keyCode, event);
    }
}
