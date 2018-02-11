package com.android.contacts.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.contacts.ContactsActivity;
import com.android.contacts.list.AuroraPrivacyContactListAdapter;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.IntentFactory;
import com.mediatek.contacts.list.AuroraContactListMultiChoiceActivity;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.AuroraPrivacyUtils;

import aurora.widget.AuroraListView;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreferenceManager;

/**
 * aurora:wangth 20140923 add
 */

public class AuroraPrivacyContactListActivity extends AuroraActivity
        implements OnItemClickListener {
	
    private static final String TAG = "AuroraPrivacyListActivity";
    
    private static AuroraActionBar mActionBar;
    private static final int AURORA_ADD_PRIVACY_CONTACT = 1;
    private Context mContext;
    
    private AuroraListView mListView;
    private View mLoadingContainer;
    private TextView mLoadingContact;
    private ProgressBar mProgress;
    private TextView mNoContactsEmptyView;
    
    private QueryHandler mQueryHandler;
    private AuroraPrivacyContactListAdapter mAdapter;
    
    private boolean isFinished = false;
    private static final int WAIT_CURSOR_START = 1230;
    private static final long WAIT_CURSOR_DELAY_TIME = 500;
    private static int QUERY_TIMEOUT = 120;

    private ContactsUtils.AuroraContactsProgressDialog mSaveProgressDialog = null;
    private static final int START = 0;
    private static final int END = 1;
    private static final int ERROR = 2;
    private static final int REFRESH = 3;
    
    private AuroraAlertDialog mConDialog;
    private SharedPreferences mPrefs;
    private static final String GOTO_PRIVACY_MANAGER_SETTING = "has.goto.privacy.manager.setting";
    
    private boolean mDeleteContactsFromDB = false;
    
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
            
            case REFRESH: {
            	isFinished = true;
				
				if (mAdapter.getCount() <= 0) {
					mNoContactsEmptyView.setVisibility(View.VISIBLE);
				} else {
					mNoContactsEmptyView.setVisibility(View.GONE);
				}
				
                break;
            }
            
            }
            
            super.handleMessage(msg);
        }
    };
    
    private void checkSetting() {
    	if (mConDialog == null) {
    		mConDialog = new AuroraAlertDialog.Builder(mContext,
                    AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.aurora_privacy_contacts_tixing_title)
                    .setMessage(R.string.aurora_privacy_contacts_tixing_message)
                    .setNegativeButton(R.string.aurora_privacy_contacts_tixing_cancle, null)
                    .setPositiveButton(R.string.aurora_privacy_contacts_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    final Intent intent = new Intent();
                                    intent.setClassName("com.aurora.privacymanage", 
                                    		"com.privacymanage.activity.PrivacyRingMsgActivity");
                                    try {
                    					startActivity(intent);
                    				} catch (ActivityNotFoundException a) {
                    				    a.printStackTrace();
                    				}
                                }
                            }).create();
    	}

    	try {
    		mConDialog.show();
    	} catch (Exception e) {
    		
    	}
    }
    
    @Override
    protected void onCreate(Bundle savedState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedState);
        
        mContext = AuroraPrivacyContactListActivity.this;
        setAuroraContentView(R.layout.aurora_sim_contact_list_content_3rd,
                AuroraActionBar.Type.Normal);
        mActionBar = getAuroraActionBar();
    	mActionBar.setTitle(R.string.aurora_privacy_contacts);
    	mActionBar.addItem(AuroraActionBarItem.Type.Add, AURORA_ADD_PRIVACY_CONTACT);
    	mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
    	
    	mAdapter = new AuroraPrivacyContactListAdapter(mContext);
    	mQueryHandler = new QueryHandler(this);
    	
    	initView();
    	
    	if (ContactsApplication.sIsAuroraPrivacySupport) {
    		ContactsApplication.mPrivacyActivityList.add(this);
    	}
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        startQuery();
    }
    
    private void initView() {
    	mNoContactsEmptyView = (TextView)findViewById(
                R.id.no_contacts);
    	mNoContactsEmptyView.setText(mContext.getString(R.string.aurora_no_privacy_contacts));
    	
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
//        mListView.setOnCreateContextMenuListener(this);
	    mListView.auroraSetNeedSlideDelete(true);
	    mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        mListView.auroraSetDeleteItemListener(new AuroraListView.AuroraDeleteItemListener() {
        	
        	@Override
            public void auroraDeleteItem(View v,int position) {
        		if (mAdapter == null || mContext == null) {
        			return;
        		}
        		
        		int contactId = mAdapter.getContactID(position);
        		if (mDeleteContactsFromDB) {
        			int rawContactId = mAdapter.getRawContactID(position);
        			mContext.getContentResolver().delete(
                            RawContacts.CONTENT_URI, 
                            RawContacts._ID + "=?" + " and deleted=0 and is_privacy=" + AuroraPrivacyUtils.mCurrentAccountId, 
                            new String[] {String.valueOf(rawContactId)});
        		} else {
        			ContentValues values = new ContentValues();
            		values.put("is_privacy", 0);
            		
        			mContext.getContentResolver().update(RawContacts.CONTENT_URI, values, 
                    		RawContacts.CONTACT_ID + "=" + contactId, null);
                    mContext.getContentResolver().update(Data.CONTENT_URI, values, 
                            Data.CONTACT_ID + "=" + contactId, null);
                    values.clear();
        		}
        		
        		startQuery();
        	}
        });
        mListView.auroraSetAuroraBackOnClickListener(
                new AuroraListView.AuroraBackOnClickListener() {
                	
                	@Override
                    public void auroraOnClick(final int position) {
                		if (mAdapter == null || mContext == null) {
                			return;
                		}
                		
                		final View dialogView = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
                		final TextView messageView = (TextView)dialogView.findViewById(R.id.textView1);
                		final AuroraCheckBox black_remove=(AuroraCheckBox)dialogView.findViewById(R.id.check_box);
                		
                		messageView.setText(mContext.getString(R.string.aurora_remove_one_privacy_contact_message, mAdapter.getName(position)));
                		black_remove.setText(mContext.getString(R.string.aurora_remove_privacy_contact_check_box_text));
                		black_remove.setChecked(false);
                		
                		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(AuroraPrivacyContactListActivity.this)
                		.setTitle(mContext.getString(R.string.aurora_remove_privacy_contact_title))
                		.setView(dialogView)
                		.setPositiveButton(android.R.string.ok,
                				new DialogInterface.OnClickListener() {
	                        @Override
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                        	mDeleteContactsFromDB = black_remove.isChecked();
	                        	Log.i(TAG, "mDeleteContactsFromDB = " + mDeleteContactsFromDB);
	                        	mListView.auroraDeleteSelectedItemAnim();
	                        }
                		})
                		.setNegativeButton(android.R.string.cancel,   
		                		new DialogInterface.OnClickListener() {
		                    @Override
		                    public void onClick(DialogInterface dialog, int whichButton) {
		                    	dialog.dismiss();
		                    }
		                }).show();
                		dialog.setCanceledOnTouchOutside(false);
                	}
                	
                	@Override
                    public void auroraPrepareDraged(int position) {
                		
                	}
                	
                	@Override
                    public void auroraDragedSuccess(int position) {
                		
                	}
                	
                	@Override
                    public void auroraDragedUnSuccess(int position) {
                		
                	}
                });
    }
    
    private void startQuery() {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				int i = 0;
				while(true) {
					if (AuroraPrivacyUtils.mIsServiceConnected) {
						break;
					}
					
					try {
						Thread.sleep(10);
						i++;
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (i > 15) {
						break;
					}
				}
				
				Uri uri = Contacts.CONTENT_URI;
		    	Log.i(TAG, "current privacy account id = " + AuroraPrivacyUtils.mCurrentAccountId 
		    			+ "  AuroraPrivacyUtils.mIsServiceConnected = " + AuroraPrivacyUtils.mIsServiceConnected);
		    	
		    	String selection = "is_privacy=" + AuroraPrivacyUtils.mCurrentAccountId;
		    	mQueryHandler.startQuery(1, null, uri, AuroraPrivacyContactListAdapter.CONTACT_PROJECTION_PRIMARY, selection, null, Contacts.SORT_KEY_PRIMARY);
			}
		}).start();
    }
    
    private final class QueryHandler extends AsyncQueryHandler {
		private final WeakReference<AuroraPrivacyContactListActivity> mActivity;

		/**
		 * Simple handler that wraps background calls to catch
		 * {@link SQLiteException}, such as when the disk is full.
		 */
		protected class CatchingWorkerHandler extends
				AsyncQueryHandler.WorkerHandler {
			public CatchingWorkerHandler(Looper looper) {
				super(looper);
			}

			@Override
			public void handleMessage(Message msg) {
				try {
					// Perform same query while catching any exceptions
					super.handleMessage(msg);
				} catch (SQLiteDiskIOException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteFullException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteDatabaseCorruptException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				}
			}
		}

		@Override
		protected Handler createHandler(Looper looper) {
			// Provide our special handler that catches exceptions
			return new CatchingWorkerHandler(looper);
		}

		public QueryHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<AuroraPrivacyContactListActivity>(
					(AuroraPrivacyContactListActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			final AuroraPrivacyContactListActivity activity = mActivity.get();
			if (activity != null && mContext != null) {
				final AuroraPrivacyContactListAdapter adapter = activity.mAdapter;
				adapter.changeCursor(cursor);
				
				isFinished = true;
				mHandler.sendEmptyMessage(WAIT_CURSOR_START);
				
				if (cursor == null) {
					Log.e(TAG, "onQueryCompleted - cursor is null");
					mNoContactsEmptyView.setVisibility(View.VISIBLE);
					return;
				}
                
				if (cursor.getCount() == 0) {
					mNoContactsEmptyView.setVisibility(View.VISIBLE);
				} else {
					mNoContactsEmptyView.setVisibility(View.GONE);
				}
				
//				if (AuroraPrivacyUtils.mPrivacyContactsNum != cursor.getCount()) {
					AuroraPrivacyUtils.mPrivacyContactsNum = cursor.getCount();
					AuroraPrivacyUtils.setPrivacyNum(mContext,
							"com.android.contacts.activities.AuroraPrivacyContactListActivity", 
							AuroraPrivacyUtils.mPrivacyContactsNum, 
							AuroraPrivacyUtils.mCurrentAccountId);
//				}
				
                Log.i(TAG, "onQueryCompleted - Count:" + cursor.getCount());
                if (cursor.getCount() > 0) {
                	mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
                	boolean hasGotoSetting = mPrefs.getBoolean(GOTO_PRIVACY_MANAGER_SETTING, false);
                	if (!hasGotoSetting) {
                		mPrefs.edit().putBoolean(GOTO_PRIVACY_MANAGER_SETTING, true).apply();
                    	checkSetting();
                	}
                }
			} else {
				cursor.close();
			}
		}
	}
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mAdapter.changeCursor(null);
        if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.remove(this);
        	AuroraPrivacyUtils.unbindService(this);
        }
    }
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case AURORA_ADD_PRIVACY_CONTACT:
                addMenu(0, mContext.getResources().getString(R.string.aurora_new_privacy_contacts),
						new OnMenuItemClickLisener() {
							public void onItemClick(View menu) {
								Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
								intent.putExtra("is_privacy_contact", true);
								intent.addCategory(IntentFactory.GN_CATEGORY);
								startActivity(intent);
							}
						});
				addMenu(1, mContext.getResources().getString(R.string.aurora_add_privacy_contacts_from_exists),
						new OnMenuItemClickLisener() {
							public void onItemClick(View menu) {
								Intent intent = new Intent(AuroraPrivacyContactListActivity.this, AuroraContactListMultiChoiceActivity.class);
								intent.putExtra("aurora_add_privacy_contacts", true);
								startActivity(intent);
							}
						});
                showCustomMenu();
                break;
            default:
                break;
            }
        }
    };
    
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
        	try {
                boolean deleteIsShow = mListView.auroraIsRubbishOut();
                if (deleteIsShow) {
                	mListView.auroraSetRubbishBack();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
            finish();
            break;
        }
        
        default: {
        }
        }

        return super.onKeyDown(keyCode, event);
    }
    
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
		Uri uri = mAdapter.getContactUri(arg2);
		Intent intent = IntentFactory.newViewContactIntent(uri);
		intent.putExtra("is_privacy_contact", true);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
