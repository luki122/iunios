package com.mediatek.contacts.dialpad;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.EditText;
import aurora.widget.AuroraListView;

import com.android.contacts.ContactsApplication;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.model.AccountType;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.NumberAreaUtil;
import com.mediatek.contacts.dialpad.IDialerSearchController.AuroraDialerSearchResultColumns;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.provider.ContactsContract.Contacts;

//import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraEditText;

public class AuroraDialerSearchController extends AsyncQueryHandler implements TextWatcher, IDialerSearchController, AuroraDialerSearchAdapter.OnDialCompleteListener {

    private static final String TAG = "AuroraDialerSearchController";
    private static final int QUERY_TOKEN_INCREMENT = 11;
    private static final int QUERY_TOKEN_INIT = 12;

     
    private Activity mActivity;

    protected AuroraEditText mDigits;
    protected AuroraListView mListView;
     
    private AuroraDialerSearchAdapter mAdapter;

     
    private int mDialerSearchCursorCount;
    protected boolean noMoreResult;

     
    private boolean mFormatting;

    protected Uri mSelectedContactUri;

     
    private ContactsPreferences mContactsPrefs;
     
    private int mDisplayOrder;
     
    private int mSortOrder;

     
    private OnDialerSearchListener mOnDialerSearchResult;

    private String mCurSearchContent;

    public AuroraDialerSearchController (Activity act, AuroraListView listView) {
        super(act.getContentResolver());
        mActivity = act;
        mListView = listView;
        mAdapter = new AuroraDialerSearchAdapter(act);
        mAdapter.setDialCompleteListener(this);
        mListView.setAdapter(mAdapter);

        mContactsPrefs = new ContactsPreferences(act);
        mContactsPrefs.registerChangeListener(new ContactsPreferences.ChangeListener() {
            public void onChange() {
                log("contacts display or sort order changed");
            }
        });
        
        mDelayHandler = new DelayHandler();
    }

    public void setDialerSearchTextWatcher(AuroraEditText digits) {
        mDigits = digits;
        mDigits.addTextChangedListener(this);
    }

    public void setOnDialerSearchListener(OnDialerSearchListener dialerSearchListener) {
        mOnDialerSearchResult = dialerSearchListener;
    }

    public void onResume() {
    	log("onResume");
    	init();
    	
        if(mDigits.getText().length() == 0) {
            startQuery(null);
        } else {
            startQuery(mDigits.getText().toString());
        }
    }
    
    public void onPause() {
        log("onPause");
    }
    
    public void onStop() {
        log("onStop");
        if(mDigits.getText().length() > 0) {
            mDigits.setText(null);
        }
    }

    public void onDestroy() {
        if(mContactsPrefs != null)
            mContactsPrefs.unregisterChangeListener();
        
        if(mAdapter != null) {
        	mAdapter.changeCursor(null);
        }
    }

    public void startQuery(String searchContent) {
    	//Gionee <huangzy> <2013-03-27> modify for CR00786343 begin
    	/*mCurSearchContent = searchContent;
    	
    	if (TextUtils.isEmpty(searchContent)) {
    		clearResult();
            return;
    	}
    	
    	String formatedSearchContent = null;
    	if (ContactsApplication.sIsGnQwertDialpadSupport) {
    		formatedSearchContent = DialerSearchUtils.tripHyphen(searchContent);	
    	} else {
    		formatedSearchContent = DialerSearchUtils.tripNonDigit(searchContent);
    	}
    	
		startQuery(QUERY_TOKEN_INCREMENT, searchContent, 
                Uri.parse("content://com.android.contacts/gn_dialer_search/" +
                		formatedSearchContent),
                	null, null, null, null);*/
    	
    	if (TextUtils.isEmpty(searchContent)) {
    		mCurSearchContent = null;
    		clearResult();
            return;
    	}
    	
    	{
	    	String formatedSearchContent = null;
	    
	    		formatedSearchContent = DialerSearchUtils.tripNonDigit(searchContent);
	    	
	    	
//	    	if (formatedSearchContent.equals(mCurSearchContent)) {
//	    		//return;
//	    	} else {
	    		mCurSearchContent = formatedSearchContent;
//	    	}
    	}
    	
    	String selection = null;
    	if (isCustomMode()) {
    	    selection = "in_visible_group=1";
    	} else {
    	    selection = "( vds_account_name='" + AccountType.ACCOUNT_NAME_LOCAL_PHONE + 
                    "' AND vds_account_type='" + AccountType.ACCOUNT_TYPE_LOCAL_PHONE + "')";
    	}
    	
    	//todo ligy
    	if (ContactsApplication.sIsAuroraPrivacySupport) {
    		long currentPrivacyId = AuroraPrivacyUtils.mCurrentAccountId;
    		if (currentPrivacyId > 0) {
    			if(TextUtils.isEmpty(selection)) {
    				selection = "is_privacy IN (0, " +  currentPrivacyId + ")";
    			} else {
    				selection += " and is_privacy IN (0, " +  currentPrivacyId + ")";
    			}
    		} else {
    			if(TextUtils.isEmpty(selection)) {
        			selection = "is_privacy = 0";
    			} else {
    				selection += " and is_privacy = 0";
    			}

    		}
    	}
    	
    	String key = mCurSearchContent;
    	startQuery(QUERY_TOKEN_INCREMENT, key, 
                Uri.parse("content://com.android.contacts/dialer_search/" +
                		key), null, selection, null, null);
    	//Gionee <huangzy> <2013-03-27> modify for CR00786343 end
    }
    
    private boolean isCustomMode() {
        SharedPreferences mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(mActivity);
        int filterInt = mPrefs.getInt("filter.type", -1);
        if (filterInt == -3) {
            return true;
        }
        
        return false;
    }
    
    public void init() {
    
    	
//    	startQuery(QUERY_TOKEN_INIT, null, 
//                Uri.parse("content://com.android.contacts/gn_dialer_search_init"),
//                	null, null, null, null);
        mDisplayOrder = mContactsPrefs.getDisplayOrder();
        mSortOrder = mContactsPrefs.getSortOrder();
//        startQuery(QUERY_TOKEN_INIT, null, 
//                Uri.parse("content://com.android.contacts/dialer_search_init"), 
//                null, null, null, null);
    	
    }
    
    public void clearResult() {
    	onQueryComplete(QUERY_TOKEN_INCREMENT, null, null);
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }
    
    private boolean isActivityFinishing() {
    	return mActivity == null || mActivity.isFinishing();
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
    	log("+onQueryComplete");
    	if (token == QUERY_TOKEN_INCREMENT) {
    		mDialerSearchCursorCount = ((null == cursor) ? 0 : cursor.getCount());
    		
    		if(mOnDialerSearchResult != null) {
                mOnDialerSearchResult.onGnDialerSearchCompleted(mDialerSearchCursorCount);
            }
    		
    		if (null != mCurSearchContent && mCurSearchContent.equals(cookie.toString())) {
    		    mAdapter.changeCursor(cursor);
                return;
    		} else { 
    			if (null != cursor) {
    				cursor.close();
    			}
    			mAdapter.changeCursor(null);
    			mListView.setAdapter(mAdapter);
    		}
    	} 
        log("-onQueryComplete");
    }

    public void afterTextChanged(Editable arg0) {
    	if (null == arg0 || 0 == arg0.length()) {
    		startQuery(null);
    		return;
    	}
    	
    	String oriText = arg0.toString();
    	if(oriText.equals(mCurSearchContent)) {
            return;
        }

        if (!mFormatting) {
            mFormatting = true;
            startQuery(oriText);
        }
        mFormatting = false;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        /*int selIdex = Selection.getSelectionStart(s);*/
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(mAdapter != null)
            mAdapter.onScrollStateChanged(view, scrollState);
    }
    
    public void updateDialerSearch() {
    	init();
    	
        if (null != mDigits) {
        	startQuery(mDigits.getText().toString());
        }
    }
    
    protected class GnWorkerHandler extends Handler {
    	private static final int EVENT_ARG_QUERY = 1;
        
        //Gionee:huangzy 20120904 add for CR00614805 start
        private final String DIALER_SEARCH_URI_STRING = "content://com.android.contacts/dialer_search/";
        private final int DIALER_SEARCH_URI_STRING_LEN = DIALER_SEARCH_URI_STRING.length();
        //Gionee:huangzy 20120904 add for CR00614805 end
        
        public GnWorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final ContentResolver resolver = mActivity.getContentResolver();
            if (resolver == null) return;

            WorkerArgs args = (WorkerArgs) msg.obj;

            int token = msg.what;
            int event = msg.arg1;

            switch (event) {
                case EVENT_ARG_QUERY:
                    Cursor cursor;
                    try {                    	                        
                        cursor = resolver.query(args.uri, args.projection,
                                args.selection, args.selectionArgs,
                                args.orderBy);
                                      
                    	//aurora add liguangyu 20131208 start  
                    	mHasContact = false;
                    	mContactUri = null;
                        if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                        	while(!cursor.isAfterLast()) {
		                        final String number = cursor.getString(AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
		                        if(number.equalsIgnoreCase(mCurSearchContent)) {
		                    		mHasContact = true;
			                        final String lookup = cursor.getString(AuroraDialerSearchResultColumns.CONTACT_NAME_LOOKUP_INDEX);
			                        final int contactId = cursor.getInt(AuroraDialerSearchResultColumns.CONTACT_ID_INDEX);
			                        mContactUri = Contacts.getLookupUri(contactId, lookup);
			                        break;
		                        }
		                        cursor.moveToNext();
                        	}
                        	cursor.moveToFirst();
                        }                      
                    	//aurora add liguangyu 20131208 end                        

                        // Calling getCount() causes the cursor window to be filled,
                        // which will make the first access on the main thread a lot faster.
                        if (cursor != null) {
                            cursor.getCount();
                        }
                                    	
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_QUERY", e);
                        cursor = null;
                    }

                    args.result = cursor;                    
                    break;
                    
            }

            // passing the original token value back to the caller
            // on top of the event values in arg1.
            Message reply = args.handler.obtainMessage(token);
            reply.obj = args;
            reply.arg1 = msg.arg1;

            reply.sendToTarget();
        }
    }
   
    @Override
    protected Handler createHandler(Looper looper) {
    	return new GnWorkerHandler(looper);
    }

	@Override
	public void setOnDialerSearchResult(OnDialerSearchResult dialerSearchResult) {
		
	}
	
	private final class DelayHandler extends Handler {
		private static final int DELAY_INDICATOR = 19871113;
		
		@Override
		public void handleMessage(Message msg) {
			Cursor cursor = null;
			if (null != msg.obj && msg.obj instanceof Cursor) {
				cursor = (Cursor)(msg.obj);
				msg.obj = null;
			}
			
			if (!hasMessages(DELAY_INDICATOR) && null != mCurSearchContent &&
					mCurSearchContent.length() == msg.arg1) {
				mAdapter.changeCursor(cursor);
			} else if (null != cursor) {
				cursor.close();
			}
		}
	}
	
	private DelayHandler mDelayHandler;
	
    //aurora add liguangyu 20131208 start
	private boolean mHasContact;
	private Uri mContactUri;
	public Uri getContactUri() {
		return mAdapter.getContactUri();
	}
	public boolean hasContact() {
		return mHasContact;
	}
	
	public String getDialNumber() {
		return mAdapter.getDialNumber();
	}
    //aurora add liguangyu 20131208 end
	
	public void resetLastClickResult(){
		mAdapter.resetLastClickResult();
	}
	
	//aurora add liguangyu 20131224 begin	
		
		public void onDialComplete(){
	    	if(mDigits != null && mDigits.length() > 0) {
	    		mDigits.getText().clear();
	    		mDigits.setCursorVisible(false);
	    	}
		}
		
		//aurora add liguangyu 20140829 for start
		public void onHotLineFill(String hotNumber){
	    	if(mDigits != null) {
	    	   	Editable digits = mDigits.getText();
                digits.replace(0, digits.length(), hotNumber);             
                mDigits.setCursorVisible(true);
                mDigits.requestFocus();
                afterTextChanged(digits);
	    	}
		}
		//aurora add liguangyu 20140829 for end
		
		 public interface OnDialerSearchResult {
		        public void onDialerSearchResult(DialerSearchResult dialerSearchResult);
		    }
		
		   public class DialerSearchResult {
		        private int count;
		        public int getCount() {
		        	return count;
		        }
		    }
}
