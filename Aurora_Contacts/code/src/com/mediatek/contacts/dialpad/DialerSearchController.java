package com.mediatek.contacts.dialpad;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.BaseColumns;
import gionee.provider.GnContactsContract.DialerSearch;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.EditText;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ContactsApplication;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.GnHotLinesUtil.T9SearchRetColumns;
import com.mediatek.contacts.CallOptionHandler;

import aurora.widget.AuroraEditText;

public class DialerSearchController extends AsyncQueryHandler implements TextWatcher, IDialerSearchController {

    private static final String TAG = "DialerSearchController";

    public static final int DIALER_SEARCH_MODE_ALL = 0;
    public static final int DIALER_SEARCH_MODE_NUMBER = 1;

    private static final int DS_MSG_CONTACTS_DELETE_CHANGED = 1000;
    private static final int DS_MSG_DELAY_TIME = 1000;

    private static final int QUERY_TOKEN_INIT = 30;
    private static final int QUERY_TOKEN_NULL = 40;
    private static final int QUERY_TOKEN_INCREMENT = 50;
    private static final int QUERY_TOKEN_SIMPLE = 60;

    public static final String[] DIALER_SEARCH_PROJECTION = {
        DialerSearch.NAME_LOOKUP_ID,
        DialerSearch.CONTACT_ID ,
        DialerSearch.CALL_DATE,
        DialerSearch.CALL_LOG_ID,
        DialerSearch.CALL_TYPE,
        DialerSearch.SIM_ID,
        DialerSearch.INDICATE_PHONE_SIM,
        DialerSearch.CONTACT_STARRED,
        DialerSearch.PHOTO_ID,
        DialerSearch.SEARCH_PHONE_TYPE,
        DialerSearch.NAME, 
        DialerSearch.SEARCH_PHONE_NUMBER,
        DialerSearch.CONTACT_NAME_LOOKUP,
        DialerSearch.MATCHED_DATA_OFFSETS,
        DialerSearch.MATCHED_NAME_OFFSETS
   };

    protected Activity mActivity;

    protected AuroraEditText mDigits;
    protected AuroraListView mListView;
    protected DialerSearchAdapter mAdapter;

    protected Queue<Integer> mSearchNumCntQ = new LinkedList<Integer>();
    protected int mPrevQueryDigCnt;
    protected int mDialerSearchCursorCount;
    protected boolean mQueryComplete;
    protected int noResultDigCnt;
    protected boolean noMoreResult;

    protected boolean mFormatting;
    protected int searchMode; 
    protected boolean mSearchNumberOnly;
    protected boolean mChangeInMiddle;
    protected String mDigitString;

    protected Uri mSelectedContactUri;

    protected ContactsPreferences mContactsPrefs;
    protected int mDisplayOrder;
    protected int mSortOrder;

    protected OnDialerSearchResult mOnDialerSearchResult;
    CallLogContentObserver mCallLogContentObserver;
    private boolean mIsForeground = false;

    private boolean mDataChanged = false;
    
    private static int delCount=0;

    private String mPreviousText;

    public DialerSearchController (Activity act, AuroraListView listView, CallOptionHandler callOptionHandler) {
        super(act.getContentResolver());
        mActivity = act;
        mListView = listView;
        mAdapter = new DialerSearchAdapter(act, callOptionHandler);
        mListView.setAdapter(mAdapter);

        mCallLogContentObserver = new CallLogContentObserver();

        mContactsPrefs = new ContactsPreferences(act);
        mContactsPrefs.registerChangeListener(new ContactsPreferences.ChangeListener() {
            public void onChange() {
                log("contacts display or sort order changed");
            }
        });
    }

    public DialerSearchController (Activity act, AuroraListView listView) {
        this(act, listView, null);
    }

    public void setDialerSearchTextWatcher(AuroraEditText digits) {
        mDigits = digits;
        mDigits.addTextChangedListener(this);

        mActivity.getContentResolver().registerContentObserver(
                Uri.parse("content://com.android.contacts.dialer_search/callLog/"), true,
                mCallLogContentObserver);
    }

    public void setOnDialerSearchResult(OnDialerSearchResult dialerSearchResult) {
        mOnDialerSearchResult = dialerSearchResult;
    }

    DialerSearchResult obtainDialerSearchResult(int count) {
        DialerSearchResult dialerSearchResult = new DialerSearchResult();
        dialerSearchResult.count = count;
        return dialerSearchResult;
    }

    public void onResume() {    	
        log("onResume");
        if(mAdapter != null)
            mAdapter.onResume();
        
        // Gionee:huangzy 20120527 add for CR00573913 start
        if (ContactsApplication.sIsGnContactsSupport) {
        	if (null != mDigits && mDigits.getText().length() <= 0) {
        		mAdapter.changeCursor(null);
        	}
        }
        // Gionee:huangzy 20120527 add for CR00573913 end

        if(mDigits.getText().length() == 0) {
            log("DialerSearchController onResume startQuery");
            startQuery(null, DIALER_SEARCH_MODE_ALL);
        }
        mIsForeground = true;
    }
    
    public void onPause() {
        log("onPause");
        mIsForeground = false;
    }
    
    public void onStop() {
        log("onStop");
        if(mDigits.getText().length() > 0) {
            log("DialerSearchController onStop");
            mDigits.setText(null);
        }
    }

    public void onDestroy() {
        if(mCallLogContentObserver != null)
            mActivity.getContentResolver().unregisterContentObserver(mCallLogContentObserver);

        if(mContactsPrefs != null)
            mContactsPrefs.unregisterChangeListener();
    }

    public void startQuery(String searchContent, int mode) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnStartQuery(searchContent, mode);
    		return;
    	}
        if (ContactsApplication.sDialerSearchSupport) {
            log("startQuery searchContent: "+searchContent+" mode: "+mode);
            searchContent = DialerSearchUtils.tripHyphen(searchContent);
            noMoreResult = (noResultDigCnt > 0 && mDigits.getText().length() > noResultDigCnt) ? true : false;
            log("noResultDigCnt: " + noResultDigCnt + " || mDigits.getText(): " + mDigits.getText());
            mQueryComplete = false;
            if (searchContent == null) {
                mDisplayOrder = mContactsPrefs.getDisplayOrder();
                mSortOrder = mContactsPrefs.getSortOrder();
                startQuery(QUERY_TOKEN_INIT, null, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/init#" + mDisplayOrder+"#"+mSortOrder), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(0));
            } else if (searchContent.equals("NULL_INPUT")) {
                startQuery(QUERY_TOKEN_NULL, null, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/null_input"), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(0));
            } else if (mode == DIALER_SEARCH_MODE_ALL) {
                if (!noMoreResult) {
                    startQuery(QUERY_TOKEN_INCREMENT, null, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/"+searchContent), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                    mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
                }
            } else if (mode == DIALER_SEARCH_MODE_NUMBER) {
                // won't check noMoreResult for search number mode, since if edit in middle will invoke no search result!
                startQuery(QUERY_TOKEN_SIMPLE, null, 
                    Uri.parse("content://com.android.contacts/dialer_search_number/filter/"+searchContent), 
                    DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
            }
        }
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
    	if (ContactsApplication.sDialerSearchSupport) {
    		gnOnQueryComplete(token, cookie, cursor);
    		return;
    	}
    	
        Integer cnt = mSearchNumCntQ.poll();
        if (cnt != null) {
            mPrevQueryDigCnt = cnt.intValue();
        }
        log("+onQueryComplete");
        
        //if (activity != null && !activity.isFinishing())
        {
            final DialerSearchAdapter dialerSearchAdapter = mAdapter;
            // Whenever we get a suggestions cursor, we need to immediately kick off
            // another query for the complete list of contacts
            if (cursor!= null) {
                mDialerSearchCursorCount = cursor.getCount();
                log("cursor count: "+mDialerSearchCursorCount);
                String tempStr = mDigits.getText().toString();

                if (tempStr != null && mDialerSearchCursorCount > 0) {
                    mQueryComplete = true;
                    noResultDigCnt = 0;
                    // notify UI to update view only if the search digit count is equal to current input search digits in text view
                    // since user may input/delete quickly, the list view will be update continuously and take a lot of time 
                    if (DialerSearchUtils.tripHyphen(tempStr).length() == mPrevQueryDigCnt) {
                        // Don't need to close cursor every time after query complete.
                        if(mOnDialerSearchResult != null) {
                            mOnDialerSearchResult.onDialerSearchResult(obtainDialerSearchResult(mDialerSearchCursorCount));
                        }
                        dialerSearchAdapter.setResultCursor(cursor);
                        dialerSearchAdapter.changeCursor(cursor);
                    } else {
                        cursor.close();
                    }
                } else {
                    if(mOnDialerSearchResult != null) {
                        mOnDialerSearchResult.onDialerSearchResult(obtainDialerSearchResult(mDialerSearchCursorCount));
                    }
                    noResultDigCnt = mDigits.getText().length();
                    cursor.close();
                    dialerSearchAdapter.setResultCursor(null);
                }
            }
        }/* else {
            if (cursor != null)
                cursor.close();
        }*/
        log("-onQueryComplete");
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {//80794
        log("result is "+result);
        if (result < 0) {
            Toast.makeText(mActivity,
                    R.string.delete_error, Toast.LENGTH_SHORT).show();
        } else {
            if (mSelectedContactUri != null) {
                log("Before delete db");
                int deleteCount = mActivity.getContentResolver().delete(mSelectedContactUri, null, null);
                log("onDeleteComplete startQuery");
                if (deleteCount > 0) {
                    delCount = deleteCount;
                    mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
                }
            }
        }

    }

    public void setSearchNumberOnly() {
        mSearchNumberOnly = true;
    }

    public void afterTextChanged(Editable arg0) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnAfterTextChanged(arg0);
    	}
        Log.e(TAG, "afterTextChanged, mPreviousText =  " + mPreviousText + " arg0 = " + arg0.toString());
        if(mPreviousText != null && mPreviousText.equals(arg0.toString())) {
            log("bail out...");
            return;
        }

        mPreviousText = arg0.toString();

        if (!mFormatting) {
            Log.e(TAG, "formatting");
            mFormatting = true;
            mDigitString = mDigits.getText().toString();

            mDigitString = DialerSearchUtils.tripNonDigit(mDigitString);
            if (arg0.length() > 0) {
                String digits = arg0.toString();
                startQuery(digits, searchMode);
            } else if (arg0.length() == 0) {
                mSearchNumberOnly = false;
                if (mDataChanged) {
                    startQuery(null, DIALER_SEARCH_MODE_ALL);
                    mDataChanged = false;
                } else {
                    startQuery("NULL_INPUT", DIALER_SEARCH_MODE_ALL);
                }
            }
        }
        mFormatting = false;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        int selIdex = Selection.getSelectionStart(s);
        if (selIdex < s.length()) {
            mChangeInMiddle = true;
        }
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String digis = s.toString();
        if (!mFormatting && digis.length() > 0) {
            if (ContactsApplication.sDialerSearchSupport) {
                if (mSearchNumberOnly || (start==0 && before==0 && count > 1) || mChangeInMiddle) {
                    // parse action should also set flag
                    setSearchNumberOnly();
                    searchMode = DIALER_SEARCH_MODE_NUMBER;
                } else {
                    searchMode = DIALER_SEARCH_MODE_ALL;
                }
            }
        }
        mChangeInMiddle = false;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(mAdapter != null)
            mAdapter.onScrollStateChanged(view, scrollState);
    }

    private final Handler mDBHandlerForDelContacts = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DS_MSG_CONTACTS_DELETE_CHANGED: {
                    if (delCount > 0) {
                        if (mDigits != null) {
                            mDigits.getText().clear();
                            log("mPhoneStateListener startQuery");
                            startQuery(null, DIALER_SEARCH_MODE_ALL);
                        }
                        delCount = 0;
                    }
                    return;
                }
            }
        }
    };

    public class DialerSearchResult {
        private int count;
        public int getCount() {
        	return count;
        }
    }

    private class CallLogContentObserver extends ContentObserver {
        public CallLogContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            log("call log observer onChange length: "+ mDigits.length());
            if (mIsForeground) {
                if (mDigits != null) {
                    if(mDigits.length() > 0) {
                        // clear the text will trigger startQuery
                        mDataChanged = true;
                    } else {
                        startQuery(null, DIALER_SEARCH_MODE_ALL);
                    }
                }
            } else {
                mDataChanged = true;
            }
        }
    }
    
    
    private class ContactContentObserver extends ContentObserver {

        public ContactContentObserver() {
            super(new Handler());
        }
        
        @Override
        public void onChange(boolean selfChange) {
            log("ContactContentObserver: ");
            if (!mIsForeground) {
                mDataChanged = true;
            }
        }
    }

    public void updateDialerSearch() {
        if (mDigits != null) {
            if (mDigits.length() > 0) {
                // clear the text will trigger startQuery
                mDataChanged = true;
            } else {
                startQuery(null, DIALER_SEARCH_MODE_ALL);
            }
        }
    }

    public interface OnDialerSearchResult {
        public void onDialerSearchResult(DialerSearchResult dialerSearchResult);
    }
    
    // ********************above is MTK************************
    
    private final String COOKIE_QUERY_INIT = "INIT";
    private final String COOKIE_QUERY_NULL_INPUT = "NULL_INPUT";
    
    public void gnStartQuery(String searchContent, int mode) {
        if (ContactsApplication.sDialerSearchSupport) {
            log("startQuery searchContent: "+searchContent+" mode: "+mode);
            searchContent = DialerSearchUtils.tripHyphen(searchContent);
            noMoreResult = (noResultDigCnt > 0 && mDigits.getText().length() > noResultDigCnt) ? true : false;
            log("noResultDigCnt: " + noResultDigCnt + " || mDigits.getText(): " + mDigits.getText());
            mQueryComplete = false;
            if (searchContent == null) {
                mDisplayOrder = mContactsPrefs.getDisplayOrder();
                mSortOrder = mContactsPrefs.getSortOrder();
                startQuery(QUERY_TOKEN_INIT, COOKIE_QUERY_INIT, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/init#" + mDisplayOrder+"#"+mSortOrder), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(0));
            } else if (searchContent.equals("NULL_INPUT")) {
                startQuery(QUERY_TOKEN_NULL, COOKIE_QUERY_NULL_INPUT, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/null_input"), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(0));
            } else if (mode == DIALER_SEARCH_MODE_ALL) {
                if (!noMoreResult) {
                    startQuery(QUERY_TOKEN_INCREMENT, null, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/"+searchContent), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                    mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
                }
            } else if (mode == DIALER_SEARCH_MODE_NUMBER) {
                // won't check noMoreResult for search number mode, since if edit in middle will invoke no search result!
                startQuery(QUERY_TOKEN_SIMPLE, null, 
                    Uri.parse("content://com.android.contacts/dialer_search_number/filter/"+searchContent), 
                    DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
            }
        }
    }
    
    protected void gnOnQueryComplete(int token, Object cookie, Cursor cursor) {
        Integer cnt = mSearchNumCntQ.poll();
        if (cnt != null) {
            mPrevQueryDigCnt = cnt.intValue();
        }
        
        mDialerSearchCursorCount = null == cursor ? 0 : cursor.getCount();
        log("cursor count: "+mDialerSearchCursorCount);
        if(mOnDialerSearchResult != null) {
            mOnDialerSearchResult.onDialerSearchResult(obtainDialerSearchResult(mDialerSearchCursorCount));
        }
        
        if(COOKIE_QUERY_INIT.equals(cookie)) {
        	if (null != cursor) {
        		cursor.close();
        	}
        	return;
        }
        
        if (COOKIE_QUERY_NULL_INPUT.equals(cookie)) {
        	mAdapter.setResultCursor(null);
        	if (null != cursor) {
        		cursor.close();
        	}        	
        	return;
        }
        
        if (mActivity != null && !mActivity.isFinishing())
        {
            final DialerSearchAdapter dialerSearchAdapter = mAdapter;
            // Whenever we get a suggestions cursor, we need to immediately kick off
            // another query for the complete list of contacts
            if (cursor!= null) {
                
                String tempStr = mDigits.getText().toString();
                if (tempStr != null && mDialerSearchCursorCount > 0) {
                    mQueryComplete = true;
                    noResultDigCnt = 0;
                    // notify UI to update view only if the search digit count is equal to current input search digits in text view
                    // since user may input/delete quickly, the list view will be update continuously and take a lot of time 
                    if (DialerSearchUtils.tripHyphen(tempStr).length() == mPrevQueryDigCnt) {
                        // Don't need to close cursor every time after query complete.

                        dialerSearchAdapter.setResultCursor(cursor);
                        dialerSearchAdapter.changeCursor(cursor);
                    } else {
                        cursor.close();
                    }
                } else {                    
                    noResultDigCnt = mDigits.getText().length();
                    cursor.close();
                    dialerSearchAdapter.setResultCursor(null);
                }
            }
        } else {
            if (cursor != null)
                cursor.close();
        }
        log("-onQueryComplete");
    }
    
    public void gnAfterTextChanged(Editable arg0) {
        Log.e(TAG, "afterTextChanged, mPreviousText =  " + mPreviousText + " arg0 = " + arg0.toString());
        if(mPreviousText != null && mPreviousText.equals(arg0.toString())) {
            log("bail out...");
            return;
        }

        mPreviousText = arg0.toString();

        if (!mFormatting) {
            Log.e(TAG, "formatting");
            mFormatting = true;
            mDigitString = mDigits.getText().toString();

            mDigitString = DialerSearchUtils.tripNonDigit(mDigitString);
            if (arg0.length() > 0) {
                String digits = arg0.toString();
                startQuery(digits, searchMode);
            } else if (arg0.length() == 0) {
                mSearchNumberOnly = false;
                if (mDataChanged) {
                    startQuery(null, DIALER_SEARCH_MODE_ALL);
                    mDataChanged = false;
                } else {
                    startQuery("NULL_INPUT", DIALER_SEARCH_MODE_ALL);
                }
            }
        }
        mFormatting = false;
    }
    
    //Gionee:huangzy 20120823 add for CR00614805 start
    private static final String[] RESULT_COLUMN_NAMES = {
	    "_id",//0
	    "vds_contact_id",//1
	    "vds_call_date",//2
	    "vds_call_log_id",//3
	    "vds_call_type",//4
	    "vds_geocoded_location",//5
	    "vds_sim_id",//6
	    "vds_vtcall",//7
	    "vds_indicate_phone_sim",//8
	    "vds_starred",//9
	    "vds_photo_id",//10
	    "vds_phone_type",//11
	    "vds_name",//12
	    "vds_phone_number",//13
	    "vds_lookup",//14
	    "matched_data_offsets",//15
	    "matched_name_offsets",//16
	    "times_contacted",//17
	    "name_jianpintonumber",//18
	    "name_pinyin",//19
	    "matched_offset",//20
	    "hanzi_pinyin",//21
	    "pinyin_highlight_offset"//22
    };
    
    private Cursor compineHotLinesRet(Cursor src, Cursor hotLines) {
    	if (null == hotLines) {
    		return src;
    	} else if (hotLines.getCount() <= 0 || !hotLines.moveToFirst()) {
    		hotLines.close();
    		return src;
    	}
    	
    	MatrixCursor retCursor = new MatrixCursor(RESULT_COLUMN_NAMES);
    	int len = RESULT_COLUMN_NAMES.length;
    	Object[] raw = new Object[len];
    	if (null != src) {
    		if (src.moveToFirst() && src.getCount() > 0) {
    			do {
        			for (int i = 0; i < len; i++) {
        				raw[i] = src.getString(i);
        			}
        			retCursor.addRow(raw);
        		} while(src.moveToNext());	
    		}
    		
    		src.close();
    	}
    	
    	raw = new Object[len];
    	if (null != hotLines) {
    		if (hotLines.moveToFirst()  && hotLines.getCount() > 0) {
    			int id = 0;
    			//Gionee:huangzy 20121011 modify for CR00710695 start
        		do {
        			raw[1] = (--id);
        			raw[12] = hotLines.getString(T9SearchRetColumns.NAME_INDEX);    			
        			raw[13] = hotLines.getString(T9SearchRetColumns.NUMBER_INDEX);
        			String dataHighLight = hotLines.getString(T9SearchRetColumns.DATA_HIGH_LIGHT_INDEX);
        			raw[15] = (null == dataHighLight) ? null :
        				new String(new char[]{dataHighLight.charAt(0), (char)(dataHighLight.charAt(1) - 1)});
        			raw[21] = hotLines.getString(T9SearchRetColumns.NAME_PINYIN_INDEX);
        			raw[22] = hotLines.getString(T9SearchRetColumns.PINYIN_HIGH_LIGHT_INDEX);
        			raw[10] = hotLines.getString(T9SearchRetColumns.PHOTO_NAME_INDEX);
        			retCursor.addRow(raw);
        			
        		} while(hotLines.moveToNext());
        		//Gionee:huangzy 20121011 modify for CR00710695 end
    		}
    		hotLines.close();
    	}
    	return retCursor;
    }
    
    protected class GnWorkerHandler extends Handler {
    	private static final int EVENT_ARG_QUERY = 1;
        private static final int EVENT_ARG_INSERT = 2;
        private static final int EVENT_ARG_UPDATE = 3;
        private static final int EVENT_ARG_DELETE = 4;
        
        //Gionee:huangzy 20120904 add for CR00614805 start
        private final String DIALER_SEARCH_URI_STRING = "content://com.android.contacts/dialer_search/filter/";
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
                        
                        if (ContactsApplication.sIsHotLinesSupport) {
                            //Gionee:huangzy 20120904 modify for CR00614805 start
                        	String uriStr = args.uri.toString();
                        	int uriStrLen = uriStr.length(); 
                        	if (uriStrLen > DIALER_SEARCH_URI_STRING_LEN && 
                        			uriStr.startsWith(DIALER_SEARCH_URI_STRING)) {
                            	String t9SearchKey = uriStr.substring(DIALER_SEARCH_URI_STRING_LEN, uriStrLen);
                        		char first = t9SearchKey.charAt(0);
                        		if (first >= '0' && first <= '9') {
                        			Cursor hotlinesCursor = GnHotLinesUtil.tgSearch(mActivity, t9SearchKey);
                        			
                        			// cursor close in {@link #compineHotLinesRet(Cursor, Cursor)} 
                        			Cursor compined = compineHotLinesRet(cursor, hotlinesCursor);
                                    cursor = compined;
                                    compined = null;
                        		}
                            }
                            //Gionee:huangzy 20120904 modify for CR00614805 end
                        }

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

                case EVENT_ARG_INSERT:
                    args.result = resolver.insert(args.uri, args.values);
                    break;

                case EVENT_ARG_UPDATE:
                    args.result = resolver.update(args.uri, args.values, args.selection,
                            args.selectionArgs);
                    break;

                case EVENT_ARG_DELETE:
                    args.result = resolver.delete(args.uri, args.selection, args.selectionArgs);
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
    	if (ContactsApplication.sIsHotLinesSupport) {
    		return new GnWorkerHandler(looper);
    	} else {
    		return super.createHandler(looper);
    	}
    }
    //Gionee:huangzy 20120823 add for CR00614805 end

	//Gionee:huangzy 20121011 add for CR00710695 start
    @Override
	public void setOnDialerSearchListener(
			OnDialerSearchListener DialerSearchListener) {
	}
	//Gionee:huangzy 20121011 add for CR00710695 end
}
