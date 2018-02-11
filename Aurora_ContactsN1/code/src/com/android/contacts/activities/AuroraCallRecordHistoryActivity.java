package com.android.contacts.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import aurora.widget.AuroraCheckBox;
import aurora.app.AuroraProgressDialog;
import aurora.app.AuroraAlertDialog;
import com.aurora.android.contacts.AuroraStorageManager;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.R;
import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
import com.android.contacts.list.AuroraCallRecordHistoryAdapter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.AuroraCallRecordHistoryAdapter.AuroraCallRecord;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.AuroraPrivacyUtils;

public class AuroraCallRecordHistoryActivity extends AuroraActivity implements OnItemClickListener {
	
	private static final String TAG = "AuroraCallRecordHistoryActivity";
	private static AuroraActionBar mActionBar;
	private static String mSelectAllStr;
    private static String mUnSelectAllStr;
    private Context mContext;
    
    private static AuroraCallRecordHistoryAdapter mAdapter;
    private ArrayList<AuroraCallRecord> mRecords = new ArrayList<AuroraCallRecord>();
    private AuroraListView mListView;
    private static TextView mEmptyView;
    private static ProgressBar mProgress;
    private AuroraContactsProgressDialog mSaveProgressDialog = null;
    private static final int START = 0;
    private static final int END = 1;
    private static final int REFRESH = 2;
    private static final int WAIT_CURSOR_START = 3;
    private static final int WAIT_CURSOR_END = 4;
    private static final long WAIT_CURSOR_DELAY_TIME = 500;
    private static boolean isFinished = false;
    
    private boolean mIsDeleteMode = false;
    private boolean mIsNeedContextMenu = true;
    
    private static boolean mIsAdding = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAuroraContentView(R.layout.aurora_call_record_history, AuroraActionBar.Type.Normal);
		mEmptyView = (TextView)findViewById(R.id.calllog_empty);
		mProgress = (ProgressBar)findViewById(R.id.progress_loading);
		mContext = AuroraCallRecordHistoryActivity.this;
		mSelectAllStr = mContext.getResources().getString(R.string.select_all);
        mUnSelectAllStr = mContext.getResources().getString(R.string.unselect_all);
        
        initListView();
	    initActionBar();
	    
	    if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.add(this);
        }
	    
	    buildData();
	}
	
	private void initListView() {
		mListView = (AuroraListView)findViewById(android.R.id.list);
	    mListView.setItemsCanFocus(true);
	    mListView.setOnItemClickListener(this);
	    mListView.auroraEnableSelector(true);
	    mListView.setFastScrollEnabled(false);
	    mListView.setFastScrollAlwaysVisible(false);
	    mListView.setOnCreateContextMenuListener(this);
	    mListView.auroraSetNeedSlideDelete(true);
	    
	    mListView.auroraSetDeleteItemListener(new AuroraListView.AuroraDeleteItemListener() {
	    	
	    	@Override
            public void auroraDeleteItem(View v,int position) {
	        	String historyPath = mRecords.get(position).getPath();
	        	Log.d(TAG, "position = " + position + "  path = " + historyPath);
	        	try {
	        		File file = new File(historyPath);
		        	if (file.exists()) {
		        		file.delete();
		        		mRecords.remove(position);
		        		mAdapter.setCheckBoxEnable(false);
		        		mAdapter.setNeedAnim(false);
		        		refresh();
		        		mAdapter.notifyDataSetChanged();
		        	}
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
	        	
	        	mHandler.sendEmptyMessage(REFRESH);
	    	}
	    });
	    
	    mListView.auroraSetAuroraBackOnClickListener(
                new AuroraListView.AuroraBackOnClickListener() {
                	
                	@Override
                    public void auroraOnClick(final int position) {
                		AuroraAlertDialog dialog = null;
                		if (null == dialog) {
                			dialog = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                            .setTitle(R.string.call_record_delete_title)
                            .setMessage(R.string.call_record_delete_message)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                        	mListView.auroraDeleteSelectedItemAnim();
                                        }
                                    }).create();
                		}
                		
                		dialog.show();
                	}
                	
                	@Override
                    public void auroraPrepareDraged(int position) {
                		if (mListView != null && mListView.getChildAt(position) != null) {
                            ImageView iv = (ImageView)mListView.getChildAt(position).findViewById(R.id.detail);
                            if (iv != null) {
                                iv.setVisibility(View.INVISIBLE);
                            }
                        }
                	}
                	
                	@Override
                    public void auroraDragedSuccess(int position) {
                		if (mListView != null && mListView.getChildAt(position) != null) {
                            ImageView iv = (ImageView)mListView.getChildAt(position).findViewById(R.id.detail);
                            if (iv != null) {
                                iv.setVisibility(View.INVISIBLE);
                            }
                        }
                	}
                	
                	@Override
                    public void auroraDragedUnSuccess(int position) {
                		if (mListView != null && mListView.getChildAt(position) != null) {
                            ImageView iv = (ImageView)mListView.getChildAt(position).findViewById(R.id.detail);
                            if (iv != null) {
                                iv.setVisibility(View.VISIBLE);
                            }
                        }
                	}
                });
	}
	
	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.aurora_record_history);
		setAuroraMenuCallBack(auroraMenuCallBack);
		mActionBar.initActionBottomBarMenu(R.menu.aurora_delete, 1);
        showLeftRight();
	}
	
	private void initActionBar(boolean flag) {
		mActionBar.setShowBottomBarMenu(flag);
		mActionBar.showActionBarDashBoard();
	}
	
	private void showLeftRight() {
        if (mActionBar.getSelectLeftButton() != null ) {
            mActionBar.getSelectLeftButton().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    
                	if (mIsDeleteMode) {
                		mIsDeleteMode = false;
                		changeToNormalMode(true);
                	}
                }
            });
        }
        
        if (mActionBar.getSelectRightButton() != null ) {
            mActionBar.getSelectRightButton().setOnClickListener(new OnClickListener() {

                @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub

                            String selectStr = ((TextView) (mActionBar.getSelectRightButton())).getText().toString();
                            if (selectStr.equals(mSelectAllStr)) {
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
                                selectAll(true);
                            } else if (selectStr.equals(mUnSelectAllStr)) {
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
                                selectAll(false);
                            }
                        }
            });
        }
    }
	
	private void buildData() {
		isFinished = false;
		mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                WAIT_CURSOR_DELAY_TIME);
		
		String path = AuroraStorageManager.getInstance(mContext).getInternalStoragePath();
    	if (path == null) {
    		return;
    	}
    	
    	String historyPath = path + "/" + mContext.getString(R.string.aurora_call_record_history_path);
    	
    	ArrayList<AuroraCallRecord> records = new ArrayList<AuroraCallRecord>();
    	parseRecording(records, historyPath, false);
    	
    	if (ContactsApplication.sIsAuroraPrivacySupport && AuroraPrivacyUtils.mCurrentAccountId > 0) {
    		historyPath = AuroraPrivacyUtils.mCurrentAccountHomePath
                    + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);
    		historyPath = ContactsUtils.replaceBlank(historyPath);
    		parseRecording(records, historyPath, true);
    	}
    	
    	mAdapter = new AuroraCallRecordHistoryAdapter(mContext, mRecords);
    	mListView.setAdapter(mAdapter);
    	isFinished = true;
		mHandler.sendEmptyMessage(WAIT_CURSOR_END);
	}
	
	private void parseRecording(ArrayList<AuroraCallRecord> records, String path, boolean isPrivacyPath) {
		try {
			synchronized(this) {
				File file = new File(path);
	    		if (file.isDirectory()) {
	    			String[] filesArr = file.list();
	    			File[] files = file.listFiles();
	    			String origName = null;
	    			if (filesArr != null) {
	    				int fileLen = filesArr.length;
	    				
	    				if (fileLen > 0) {
	    					for (int i = fileLen - 1; i >= 0; i--) {
	    						AuroraCallRecord record = new AuroraCallRecord();
	            				String name = filesArr[i];
	            				origName = name;
	            				String startTime = "";
	            				String duration = "";
	            				Log.i(TAG, " name =" + name);
	            				String postfix = ".3gpp";
	            				if(!TextUtils.isEmpty(name) && name.endsWith(".amr")) {
	            					 postfix = ".amr";
	            				}
	            				
	            				if (isPrivacyPath && !name.contains(postfix)) {
	            					boolean change = ContactsUtils.auroraChangeFile(files[i].getPath());
	            					Log.i(TAG, "files[i].getPath():" + files[i].getPath() + "  change:" + change);
	            					if (!change) {
	            						continue;
	            					} else {
	            						name = new String(Base64.decode(name, Base64.URL_SAFE), "UTF-8");
	            						try {
	            							boolean rename = files[i].renameTo(new File(path, name));
	            							Log.i(TAG, "rename:" + rename + "  path:" + path + "  name:" + name);
	            						} catch (Exception ex) {
	            							ex.printStackTrace();
	            						}
	            					}
	            				}
	            				
	            				if (name != null) {
	            					if (name.length() > 20) {
	            						startTime = name.substring(0, 13);
	                					if (!TextUtils.isEmpty(startTime)) {
	                						long endTime = 0;
	                						long durationTime = 0;
	                						try {
	                							int durEnd = (name.substring(15, name.length())).indexOf("_");
	                							durEnd += 15;
	                							duration = name.substring(14, durEnd);
	                							if (!TextUtils.isEmpty(duration)) {
	                								durationTime = Long.valueOf(duration);
	                    							endTime = Long.valueOf(startTime) + durationTime;
	                    							String number=null;
	                    							number = name.substring(durEnd + 1,  name.indexOf(postfix));
	                    							if (number != null) {
	                    								Cursor nameCursor = mContext.getContentResolver().query(  
	            		            			                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	            		            			                new String [] {ContactsContract.PhoneLookup.DISPLAY_NAME,},
	            		            			                ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + number + "'"
	            		            			                + " and is_privacy>-1",
	            		            			                null,
	            		            			                null);
	            		            					if (nameCursor != null) {
	            		            						if (nameCursor.moveToFirst()) {
	            		            							number = nameCursor.getString(0);
	            		            						}
	            		            						
	            		            						nameCursor.close();
	            		            					}
	                    							} else {
	                    								number = name;
	                    							}
	                    							
	                        						record.setEndTime(endTime);
	                        						record.setDruation(durationTime);
	                        						record.setName(number);
	                							}
	                						} catch (Exception e) {
	                							e.printStackTrace();
	                						}
	                					}
	                				}
	            				}
	            				
	            				if (record.getName() == null) {
	            					File fi = new File(path + "/" + name);
	    							if (fi.exists()) {
	    								record.setEndTime(fi.lastModified());
	    							}
	    							
	            					String nameSub = name.substring(0, name.indexOf(postfix));
	    							if (nameSub == null) {
	    								nameSub = name;
	    							}
	    							record.setName(name);
	            				}
	            				
	            				record.setMimeType("audio/amr");
	            				record.setPath(path + "/" + name);
	            				record.setFileName(name);
	            				records.add(record);
								Log.d(TAG, "name:" + name + "  startTime:"
										+ startTime + " duration:" + duration
										+ "  records.size:" + records.size()
										+ "  path:" + path + "/" + name);
	            				
	            				mRecords = records;
	            				try {
	            					Collections.sort(mRecords, new DisplayComparator());
	            					Collections.reverse(mRecords);
	            				} catch (Exception e) {
	            					e.printStackTrace();
	            				}
	                    	}
	    				}
	    			}
	    		}
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	// sort
	public static class DisplayComparator implements Comparator<AuroraCallRecord> {
        private final Collator mCollator = Collator.getInstance();

        public DisplayComparator() {
        }

        private String getDisplay(AuroraCallRecord record) {
            long label = record.getEndTime();
            return String.valueOf(label);
        }

		@Override
		public int compare(AuroraCallRecord lhs, AuroraCallRecord rhs) {
			return mCollator.compare(getDisplay(lhs), getDisplay(rhs));
		}
    }
	
	private void refresh() {
		mAdapter.setRecords(mRecords);
	}
	
	private void selectAll(boolean checked) {
		for (int position = 0;
                position < mAdapter.getCount(); ++position) {
            if (checked) {
            	mAdapter.setCheckedItem(String.valueOf(position), mRecords.get(position));
            } else {
            	mAdapter.clearCheckedItem();
            }
            
            int realPos = position - mListView.getFirstVisiblePosition();
            if (realPos >= 0) {
                View view = mListView.getChildAt(realPos);
                if (view != null) {
                    final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                    if (null != checkBox) {
                        checkBox.auroraSetChecked(checked, true);
                    }
                }
            }
        }
		
		setBottomMenuEnable(checked);
		updateSelectedItemsView();
	}

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.menu_delete: {
				deleteCallRecord();
				break;
			}

			default:
				break;
			}
		}
	};
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
		
		if (!mIsNeedContextMenu) {
            return;
        }
		
		super.onCreateContextMenu(menu, v, menuInfo);
		
		View targetView = ((AdapterContextMenuInfo)menuInfo).targetView;
        RelativeLayout mainUi = (RelativeLayout)targetView.findViewById(com.aurora.R.id.aurora_listview_front);
        if (null != mainUi) {
            AdapterView.AdapterContextMenuInfo info;
            try {
                info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            } catch (ClassCastException e) {
                Log.e(TAG, "bad menuInfo", e);
                return;
            }
            
            Log.d(TAG, "info.id = " + info.id + "   info.po = " + info.position);
            int pos = info.position;
            mAdapter.setCheckedItem(String.valueOf(pos), mRecords.get(pos));
        }
		
		mAdapter.setCheckBoxEnable(true);
		mAdapter.setNeedAnim(true);
		mAdapter.notifyDataSetChanged();
		mIsNeedContextMenu = false;
		mIsDeleteMode = true;
		mListView.auroraSetNeedSlideDelete(false);
		mListView.auroraEnableSelector(false);
		initActionBar(true);
		setBottomMenuEnable(true);
		updateSelectedItemsView();
	}
	
	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter == null) {
			mEmptyView.setVisibility(View.VISIBLE);
			return;
		}
		
		if (!mIsDeleteMode) {
	    	String historyPath = mRecords.get(position).getPath();
			playRecord(historyPath);
			return;
		}
		
		 final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
	        if (null != checkBox) {
	            boolean checked = checkBox.isChecked();
	            checkBox.auroraSetChecked(!checked, true);
	            
	            if (!checked) {
	            	mAdapter.setCheckedItem(String.valueOf(position), mRecords.get(position));
	            } else {
	            	mAdapter.removeCheckedItem(String.valueOf(position));
	            }
	            
	            updateSelectedItemsView();
	        }
	}
	
	private void updateSelectedItemsView() {
		if (mAdapter == null) {
			finish();
			return;
		}
		
		int checkedCount = mAdapter.getCheckedItem().size();
		int totleItems = mAdapter.getCount();
		
		try {
            if (checkedCount >= totleItems) {
                ((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
            } else {
                ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (checkedCount > 0) {
            setBottomMenuEnable(true);
        } else {
            setBottomMenuEnable(false);
        }
	}
	
	private void changeToNormalMode(boolean flag) {
		initActionBar(false);
		
		mListView.auroraSetNeedSlideDelete(true);
		mListView.auroraEnableSelector(true);
		
		try {
            mAdapter.clearCheckedItem();
            mAdapter.setNeedAnim(true);
            mAdapter.setCheckBoxEnable(false);
            mIsDeleteMode = false;
            mIsNeedContextMenu = true;
            if (!flag) {
            	refresh();
            }
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@Override
    public void onResume() {
		super.onResume();
		
		try {
        	mListView.auroraOnResume();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
	
	@Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        
        try {
        	mListView.auroraOnPause();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

	@Override
    protected void onDestroy() {
		super.onDestroy();
		  if (ContactsApplication.sIsAuroraPrivacySupport) {
	        	ContactsApplication.mPrivacyActivityList.remove(this);
	        }
	}

	private void playRecord(String path) {
    	Uri data = Uri.fromFile(new File(path));  
        Intent intent = new Intent(Intent.ACTION_VIEW);  
        intent.setDataAndType(data, "audio/amr");  
        mContext.startActivity(intent);
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO move to the fragment
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
        	
        	if (mActionBar != null && 
                    (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
                return true;
            }
        	
        	
        	
        	if (mIsDeleteMode) {
        		try {
                    Thread.sleep(300);
                    changeToNormalMode(true);
                    mListView.auroraSetNeedSlideDelete(true);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
        	}
        	
        	break;
        }
        }
        
        return super.onKeyDown(keyCode, event);
	}
	
	private static void setBottomMenuEnable(boolean flag) {
        AuroraMenu auroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        auroraMenu.setBottomMenuItemEnable(1, flag);
    }
	
	private static class AuroraContactsProgressDialog extends AuroraProgressDialog {
        public AuroraContactsProgressDialog(Context context, int style) {
            super(context);
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (mIsAdding) {
                    return true;
                }
                break;
            }
            }
            
            return super.onKeyDown(keyCode, event);
        }
    };
    
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            
            case WAIT_CURSOR_START: {
                if (!isFinished) {
                    mProgress.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                }
                break;
            }
            
            case WAIT_CURSOR_END: {
            	isFinished = true;
				mProgress.setVisibility(View.GONE);
				setBottomMenuEnable(false);
				
				if (mRecords.size() <= 0) {
					mEmptyView.setVisibility(View.VISIBLE);
				} else {
					mEmptyView.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
				}
				
                break;
            }
            
            case REFRESH: {
            	isFinished = true;
				
				if (mRecords.size() <= 0) {
					mEmptyView.setVisibility(View.VISIBLE);
				} else {
					mEmptyView.setVisibility(View.GONE);
				}
				
                break;
            }
            
            case START: {
            	mIsAdding = true;
            	
                if (!isFinishing()) {
                    if (null == mSaveProgressDialog) {
                        mSaveProgressDialog = new AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
                    }
//                    mSaveProgressDialog.setTitle(R.string.aurora_save_title);
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
            	mIsAdding = false;
                if (!isFinishing() 
                        && null != mSaveProgressDialog && mSaveProgressDialog.isShowing()) {
                    try {
                        mSaveProgressDialog.dismiss();
                        mSaveProgressDialog = null;
                        mAdapter.setCheckBoxEnable(false);
                        changeToNormalMode(false);
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            }
            
            super.handleMessage(msg);
        }
    
    };
    
    private void deleteCallRecord() {
    	int selectedCount = mAdapter.getCheckedItem().size();
        if (0 >= selectedCount) {
            return;
        }
        
        AuroraAlertDialog dialog = null;
		if (null == dialog) {
			dialog = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
            .setTitle(R.string.call_record_delete_title)
            .setMessage(R.string.call_record_delete_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                        	mHandler.sendEmptyMessage(START);
                        	
                        	new deleteThread(mAdapter.getCheckedItem()).start();
                        }
                    }).create();
		}
		
		dialog.show();
    }
    
    private class deleteThread extends Thread {
        
    	private HashMap<String, AuroraCallRecord> deleteItems = new HashMap<String, AuroraCallRecord>();
    	
        public deleteThread(HashMap<String, AuroraCallRecord> items) {
        	deleteItems = items;
        }
        
        @Override
        public void run() {
        	if (deleteItems == null || deleteItems.size() <= 0) {
        		return;
        	}
        	
        	AuroraCallRecord acr = null;
        	String path = null;
        	Set<String> pisitions = deleteItems.keySet();
        	Object[] pos = pisitions.toArray();
        	try {
        		for (int i = 0; i < deleteItems.size(); i++) {
            		acr = deleteItems.get(pos[i]);
            		path = acr.getPath();
            		File file = new File(path);
    	        	if (file.exists()) {
    	        		boolean move = file.delete();
    	        		mRecords.remove(acr);
    	        	}
            	}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	
            mHandler.sendEmptyMessage(END);
            mHandler.sendEmptyMessage(REFRESH);
        }
    }
}
