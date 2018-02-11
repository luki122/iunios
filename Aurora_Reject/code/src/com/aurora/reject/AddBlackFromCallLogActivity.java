package com.aurora.reject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract;
import aurora.widget.AuroraCheckBox;
import aurora.app.AuroraProgressDialog;

import com.aurora.reject.R;
import com.aurora.reject.adapter.AddBlackFromCallLogAdapter;
import com.aurora.reject.util.TotalCount;
import com.aurora.reject.util.YuloreUtil;

public class AddBlackFromCallLogActivity extends AuroraActivity implements OnItemClickListener {
	
	private static final String TAG = "AddBlackFromCallLogActivity";
	private static AuroraActionBar mActionBar;
	private static String mSelectAllStr;
    private static String mUnSelectAllStr;
    private static Context mContext;
    
    private static final int QUERY_TOKEN = 1;
    
    private QueryHandler mQueryHandler;
    private static AddBlackFromCallLogAdapter mAdapter;
    private AuroraListView mListView;
    private static TextView mEmptyView;
    private String mBlackNumbers = null;
    
    public static HashMap<String, Integer> mCheckedItem = new HashMap<String, Integer>();
    private static boolean mIsAdding = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAuroraContentView(R.layout.add_black_from_call_log_frag, AuroraActionBar.Type.Dashboard);
		mEmptyView = (TextView)findViewById(R.id.calllog_empty);
		mContext = AddBlackFromCallLogActivity.this;
		mSelectAllStr = mContext.getResources().getString(R.string.select_all);
        mUnSelectAllStr = mContext.getResources().getString(R.string.deselect_all);
        
		initActionBar();
		
		mAdapter = new AddBlackFromCallLogAdapter(mContext);
	    mListView = (AuroraListView)findViewById(android.R.id.list);
	    mListView.setItemsCanFocus(false);
	    mListView.setOnItemClickListener(this);
	    mListView.setAdapter(mAdapter);
	    mListView.auroraEnableSelector(false);
	    mListView.setFastScrollEnabled(false);
	    mListView.setFastScrollAlwaysVisible(false);
	    
	    Bundle extras = getIntent().getExtras();
	    if (null != extras) {
	    	mBlackNumbers = extras.getString("blacknumbers");
	    }
		
	    mQueryHandler = new QueryHandler(this);
	}
	
	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		mActionBar.initActionBottomBarMenu(R.menu.aurora_add, 1);
        mActionBar.getOkButton().setText(mSelectAllStr);
        mActionBar.getCancelButton().setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        mActionBar.getOkButton().setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String okButtonStr = mActionBar.getOkButton()
                                .getText().toString();
                        if (okButtonStr.equals(mSelectAllStr)) {
                            mActionBar.getOkButton().setText(mUnSelectAllStr);
                            updateAllSelect(true);
                        } else if (okButtonStr.equals(mUnSelectAllStr)) {
                            mActionBar.getOkButton().setText(mSelectAllStr);
                            updateAllSelect(false);
                        }
                    }
                });
	}

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.menu_add: {
				addContacts();
				break;
			}

			default:
				break;
			}
		}
	};

	@Override
    protected void onResume() {
	    super.onResume();
	    
	    startQuery();
	}
	
	@Override
    protected void onDestroy() {
		mCheckedItem.clear();
		
		super.onDestroy();
		if(mAdapter != null) {
			mAdapter.changeCursor(null);
		}
	}

	private void startQuery() {
		String selection = null;
		if (mBlackNumbers != null) {
			selection = "number not in(" + mBlackNumbers + ")";
		}
		mQueryHandler.startQuery(QUERY_TOKEN, null, 
				Uri.parse("content://call_log/gncallsjoindataview"), 
				AddBlackFromCallLogAdapter.CALL_LOG_PROJECTION,
				selection, null, "_id DESC");
	}
	
	private static final class QueryHandler extends AsyncQueryHandler {
		private final WeakReference<AddBlackFromCallLogActivity> mActivity;

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
			mActivity = new WeakReference<AddBlackFromCallLogActivity>(
					(AddBlackFromCallLogActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			final AddBlackFromCallLogActivity activity = mActivity.get();
			if (activity != null && !activity.isFinishing()) {
				final AddBlackFromCallLogAdapter callsAdapter = activity.mAdapter;
				callsAdapter.clearCheckedItem();
				callsAdapter.changeCursor(cursor);
				
				if (cursor == null) {
					Log.e(TAG, "onQueryCompleted - cursor is null");
					mEmptyView.setVisibility(View.VISIBLE);
					return;
				}
                
				mCheckedItem.clear();
				setBottomMenuEnable(false);
				if (cursor.getCount() == 0) {
					mEmptyView.setVisibility(View.VISIBLE);
					mActionBar.getOkButton().setVisibility(View.GONE);
					   try {
				            mActionBar.setShowBottomBarMenu(false);
				            mActionBar.showActionBottomeBarMenu();
				            return ;
				        } catch(Exception e) {
				            e.printStackTrace();
				        }
				} else {
					mEmptyView.setVisibility(View.GONE);
					mActionBar.getOkButton().setVisibility(View.VISIBLE);
					
					try {
						 mActionBar.setShowBottomBarMenu(true);
						mActionBar.showActionBottomeBarMenu();
						updateActionBar();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
                Log.i(TAG, "onQueryCompleted - Count:" + cursor.getCount());
//                cursor.close();
			} else {
				if(cursor != null) {
					cursor.close();
				}
			}
		}
	}
	
	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter == null) {
			mEmptyView.setVisibility(View.VISIBLE);
			return;
		}
		
		final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(R.id.check_box);
		if (null != checkBox) {
            boolean checked = checkBox.isChecked();
            checkBox.auroraSetChecked(!checked, true);
            String name = mAdapter.getName(position);
            String number = mAdapter.getNumber(position);
            if (number == null) {
            	return;
            }
            Log.i("qiaohu", "checked="+checked);
            if (!checked) {
            	mCheckedItem.put(number, position);
            	mAdapter.setCheckedItem(number);
            } else {
            	mCheckedItem.remove(number);
            	mAdapter.removeCheckedItem(number);
            }
            
            updateActionBar();
        }
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO move to the fragment
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
        	finish();
        	return true;
        }
        }
        
        return super.onKeyDown(keyCode, event);
	}
	
	private void updateAllSelect(boolean flag) {
	    if (flag) {
	        for (int i = 0; i < mAdapter.getCount(); i++) {
	        	String name = mAdapter.getName(i);
	            String number = mAdapter.getNumber(i);
	            if (number == null) {
	            	return;
	            }
	            
	            mCheckedItem.put(number, i);
	            mAdapter.setCheckedItem(number);
	            
	            View view = mListView.getChildAt(i);
	            if (view != null) {
                    final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(R.id.check_box);
                    if (null != checkBox) {
                        checkBox.auroraSetChecked(flag, true);
                    }
                }
	        }
	    } else {
	    	for (int i = 0; i < mAdapter.getCount(); i++) {
	    		View view = mListView.getChildAt(i);
	            if (view != null) {
                    final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(R.id.check_box);
                    if (null != checkBox) {
                        checkBox.auroraSetChecked(flag, true);
                    }
                }
	        }
	    	
	    	mCheckedItem.clear();
	    	mAdapter.clearCheckedItem();
	    }
	    
	    updateActionBar();
	}
	
	private static void updateActionBar() {
		int allItem = mAdapter.getCount();
		int checked = mCheckedItem.size();
		
		if (checked >= allItem) {
			mActionBar.getOkButton().setText(mUnSelectAllStr);
        } else {
        	mActionBar.getOkButton().setText(mSelectAllStr);
        }
		mActionBar.getMiddleTextView().setText(mContext.getResources().getString(R.string.actionbar_count_tip, checked));
		Log.i("qiaohu", "checkedCount="+checked);
		if (checked > 0) {
            setBottomMenuEnable(true);
        } else {
            setBottomMenuEnable(false);
        }
	}
	
	private static void setBottomMenuEnable(boolean flag) {
        AuroraMenu auroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        auroraMenu.setBottomMenuItemEnable(0, flag);
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
    
    private AuroraContactsProgressDialog mSaveProgressDialog = null;
    private static final int START = 0;
    private static final int END = 1;
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            case START: {
            	mIsAdding = true;
            	
                if (!isFinishing()) {
                    if (null == mSaveProgressDialog) {
                        mSaveProgressDialog = new AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
                    }
                    mSaveProgressDialog.setTitle(R.string.aurora_save_title);
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
                        finish();
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            }
            
            super.handleMessage(msg);
        }
    
    };
    
    private void addContacts() {
    	int selectedCount = mCheckedItem.size();
        if (0 >= selectedCount) {
            return;
        }
        
        if (selectedCount > 100) {
        	Toast.makeText(mContext, R.string.aurora_select_more_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        new TotalCount(mContext, "180", "addbl", selectedCount).CountData();
        Set<String> numbers = mCheckedItem.keySet();
        ArrayList<String> numbersForBlack = new ArrayList<String>();
        ArrayList<String> namesForBlack = new ArrayList<String>();
        ArrayList<String> markForBlack = new ArrayList<String>();
        for (String number : numbers) {
        	numbersForBlack.add(number);
        	int position = mCheckedItem.get(number);
        	Cursor cursor = (Cursor) mAdapter.getItem(position);
	        if (cursor != null) {
	        	namesForBlack.add(cursor.getString(2));
	        	markForBlack.add(cursor.getString(4));
	        	Log.d(TAG, "number = " + number + "   name = " + cursor.getString(2) + "  mark = " + cursor.getString(4));
	        }
        }
        
        if (mIsAdding) {
        	return;
        }
        
        new AddBlackNameThread(namesForBlack, numbersForBlack, markForBlack).start();
    }
    
    private class AddBlackNameThread extends Thread {
        ArrayList<String> nameForBlackName = new ArrayList<String>();
        ArrayList<String> numberForBlackName = new ArrayList<String>();
        ArrayList<String> markForBlackName = new ArrayList<String>();
        ArrayList<String> numberAdded = new ArrayList<String>();
        
        public AddBlackNameThread(ArrayList<String> nameList, ArrayList<String> numberList, ArrayList<String> markList) {
            this.nameForBlackName = nameList;
            this.numberForBlackName = numberList;
            this.markForBlackName = markList;
        }
        
        @Override
        public void run() {
            if (numberForBlackName == null || numberForBlackName.size() < 1) {
                return;
            }
            
            mHandler.sendEmptyMessage(START);
            ContentValues values = new ContentValues();
            
            for (int i = 0; i < numberForBlackName.size(); i++) {
            	String number = numberForBlackName.get(i);
            	if (numberAdded.contains(number)) {
            		continue;
            	}
            	
            	values.put("isblack", 1);
            	values.put("black_name", nameForBlackName.get(i));
            	values.put("number", number);
            	values.put("reject", 3);
            	String mark = YuloreUtil.getUserMark(mContext, number);
            	int userMark = -1;
            	Log.d(TAG, "number = " + number + "  mark =  " + mark);
            	if (mark == null) {
//            		mark = YuloreUtil.getMarkContent(number);
            		userMark = YuloreUtil.getMarkNumber(mContext, number);
            	}
            	if (null != mark) {
            		Log.i("qiaohu", "mark="+mark);
            		values.put("lable", mark);
            		values.put("user_mark", userMark);
            	}
            	
            	mContext.getContentResolver().insert(Uri.withAppendedPath(GnContactsContract.AUTHORITY_URI, "black"), values);
                values.clear();
                numberAdded.add(number);
                try {
                	sleep(200);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
            
            mHandler.sendEmptyMessage(END);
            mCheckedItem.clear();
        }
    }
}
