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
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.NumberAreaUtil;
import com.mediatek.contacts.CallOptionHandler;
import com.mediatek.contacts.calllog.CallLogDateFormatHelper;
import com.mediatek.contacts.dialpad.DialerSearchController.OnDialerSearchResult;
import com.mediatek.contacts.dialpad.IDialerSearchController.GnDialerSearchResultColumns;
import com.privacymanage.service.AuroraPrivacyUtils;

import gionee.provider.GnContactsContract.Contacts;

//import android.provider.CallLog;
//import android.provider.CallLog.Calls;
import gionee.provider.GnCallLog.Calls;
import android.provider.ContactsContract;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraEditText;

public class AuroraDialerSearchController extends AsyncQueryHandler implements TextWatcher, IDialerSearchController, AuroraDialerSearchAdapter.OnDialCompleteListener {

    private static final String TAG = "DialerSearchController";
    private static final int QUERY_TOKEN_INCREMENT = 11;
    private static final int QUERY_TOKEN_INIT = 12;

    protected Activity mActivity;

    protected AuroraEditText mDigits;
    protected AuroraListView mListView;
    protected AuroraDialerSearchAdapter mAdapter;

    protected int mDialerSearchCursorCount;
    protected boolean noMoreResult;

    protected boolean mFormatting;

    protected Uri mSelectedContactUri;

    protected ContactsPreferences mContactsPrefs;
    protected int mDisplayOrder;
    protected int mSortOrder;

    protected OnDialerSearchListener mOnDialerSearchResult;

    private String mCurSearchContent;

    public AuroraDialerSearchController (Activity act, AuroraListView listView, CallOptionHandler callOptionHandler) {
        super(act.getContentResolver());
        mActivity = act;
        mListView = listView;
        mAdapter = new AuroraDialerSearchAdapter(act, callOptionHandler);
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

    public AuroraDialerSearchController (Activity act, AuroraListView listView) {
        this(act, listView, null);
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
	    	if (ContactsApplication.sIsGnQwertDialpadSupport) {
	    		formatedSearchContent = DialerSearchUtils.tripHyphen(searchContent);	
	    	} else {
	    		formatedSearchContent = DialerSearchUtils.tripNonDigit(searchContent);
	    	}
	    	
	    	if (formatedSearchContent.equals(mCurSearchContent)) {
	    		//return;
	    	} else {
	    		mCurSearchContent = formatedSearchContent;
	    	}
    	}
    	
    	String selection = null;
//    	if (isCustomMode()) {
//    	    selection = "vds_in_visible_group=1";
//    	} else {
//    	    selection = "( vds_account_name='" + AccountType.ACCOUNT_NAME_LOCAL_PHONE + 
//                    "' AND vds_account_type='" + AccountType.ACCOUNT_TYPE_LOCAL_PHONE + "')";
//    	}
    	
    	if (ContactsApplication.sIsAuroraPrivacySupport) {
    		long currentPrivacyId = AuroraPrivacyUtils.mCurrentAccountId;
    		if (currentPrivacyId > 0) {
    			selection = "vds_is_privacy IN (0, " +  currentPrivacyId + ")";
    		} else {
    			selection = "vds_is_privacy = 0";
    		}
    	}
    	
    	String key = mCurSearchContent;
    	startQuery(QUERY_TOKEN_INCREMENT, key, 
                Uri.parse("content://com.android.contacts/gn_dialer_search/" +
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
    	if (ContactsApplication.sIsHotLinesSupport) {
    		startQuery(QUERY_TOKEN_INIT, null, 
                    GnHotLinesUtil.INIT_HOT_LINES_URI,
                    	null, null, null, null);	
    	}
    	
    	startQuery(QUERY_TOKEN_INIT, null, 
                Uri.parse("content://com.android.contacts/gn_dialer_search_init"),
                	null, null, null, null);
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
        private final String DIALER_SEARCH_URI_STRING = "content://com.android.contacts/gn_dialer_search/";
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
		                        final String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
		                        if(number.equalsIgnoreCase(mCurSearchContent)) {
		                    		mHasContact = true;
			                        final String lookup = cursor.getString(GnDialerSearchResultColumns.LOOKUP_KEY_INDEX);
			                        final int contactId = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
			                        mContactUri = Contacts.getLookupUri(contactId, lookup);
			                        break;
		                        }
		                        cursor.moveToNext();
                        	}
                        	cursor.moveToFirst();
                        }                      
                    	//aurora add liguangyu 20131208 end
                        
                        if (ContactsApplication.sIsHotLinesSupport) {
                            //Gionee:huangzy 20120904 modify for CR00614805 start
                        	String uriStr = args.uri.toString();
                        	int uriStrLen = uriStr.length(); 
                        	if (uriStrLen > DIALER_SEARCH_URI_STRING_LEN && 
                        			uriStr.startsWith(DIALER_SEARCH_URI_STRING)) {
                            	String t9SearchKey = uriStr.substring(DIALER_SEARCH_URI_STRING_LEN, uriStrLen);
                        		char first = t9SearchKey.charAt(0);
                        		//Gionee:huangzy 20121023 modify for CR00717161 start
                        		/*if (first >= '0' && first <= '9') {*/
                        		
                        		if((cursor == null || cursor.getCount() <= 0) && mCurSearchContent != null && mCurSearchContent.length() > 1) {
                               		//aurora add liguangyu 20131224 begin  
                            	    Cursor calllogCursor = resolver.query(Uri.withAppendedPath(Uri.parse("content://call_log/calls/aurora_search_filter"), Uri.encode(mCurSearchContent)),
                           	    		 new String[]{Calls.NUMBER, "area", Calls.DATE, Calls.DURATION,  Calls.TYPE, Calls.SUBSCRIPTION}, null, null, Calls.DEFAULT_SORT_ORDER); 
                         			Cursor compined2 = compineCalllog(cursor, calllogCursor);
                                    cursor = compined2;
                                    compined2 = null;
                           	    	//aurora add liguangyu 20131224 end
                        		}                
                        		
                        		if (ContactsApplication.sIsGnQwertDialpadSupport || (first >= '0' && first <= '9')) {
                        		//Gionee:huangzy 20121023 modify for CR00717161 end
                        			Cursor hotlinesCursor = GnHotLinesUtil.tgSearch(mActivity, t9SearchKey);
                        			// cursor close in {@link #compineHotLinesRet(Cursor, Cursor)} 
                        			Cursor compined = GnHotLinesUtil.compineHotLinesRet(cursor, hotlinesCursor);
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
	  public Cursor compineCalllog(Cursor src, Cursor calllogLines) {
	    	if (null == calllogLines) {
	    		return src;
	    	} else if (calllogLines.getCount() <= 0 || !calllogLines.moveToFirst()) {
	    		calllogLines.close();
	    		return src;
	    	}
	    	
	    	MatrixCursor retCursor = new MatrixCursor(GnDialerSearchResultColumns.COLUMN_NAMES);
	    	int len = GnDialerSearchResultColumns.COLUMN_NAMES.length;
	    	Object[] raw = new Object[len];
	    	
	    	raw = new Object[len];
	    	
	    	
	    	
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
	    	
	    	
	    	if (null != calllogLines) {
	    		if (calllogLines.moveToFirst()  && calllogLines.getCount() > 0) {
	    			int id = 0;
	        		char[] dataHighLight = new char[2];
	        		dataHighLight[0] = 0;
	        		do {
	        		    String number = calllogLines.getString(0);
	        			String area = calllogLines.getString(1);
	        			long date = calllogLines.getLong(2);
	        			long duration = calllogLines.getLong(3);
	        			int type = calllogLines.getInt(4);
	        			int simId = calllogLines.getInt(5);
	        	        if(TextUtils.isEmpty(area)) {
	        	            area = NumberAreaUtil.getInstance(mActivity).getNumAreaFromAora(mActivity, number, false);
	        	            if(!TextUtils.isEmpty(area)) {
	        	                ContentValues values = new ContentValues();
	                            values.put("area", area);
	        	            	mActivity.getContentResolver().update(Calls.CONTENT_URI, values, "number=?", new String[]{number});
	        	            }
	        	        }
	        			raw[GnDialerSearchResultColumns.CONTACT_ID_INDEX] = 0;  	        	
	        			raw[GnDialerSearchResultColumns.NAME_INDEX] = area;
	        			raw[GnDialerSearchResultColumns.PHONE_NUMBER_INDEX] = number; 			
	        			raw[GnDialerSearchResultColumns.DATA_HIGHLIGHT_INDEX] = getDataHighlight(number, mCurSearchContent);
	        			raw[GnDialerSearchResultColumns.PINYIN_INDEX] = date;
	        			raw[GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX] = duration;
	        			raw[GnDialerSearchResultColumns.PHOTO_ID_INDEX] = type;
	        			raw[GnDialerSearchResultColumns.INDEX_IN_SIM_INDEX] = simId;  
	        			retCursor.addRow(raw);
	        			
	        		} while(calllogLines.moveToNext());    			
	    		}
	    		calllogLines.close();
	    	}
	    	
	    	return retCursor;
	    }
	  
		private String getDataHighlight(String number, String searchKey) {
			if (null != number) {
				//Gionee:huangzy 20121213 modify for CR00741589 start
				/*int index = number.indexOf(searchKey);*/
				int index = indexOf(number, searchKey);
				//Gionee:huangzy 20121213 modify for CR00741589 start
				if (-1 != index) {
					return new String(new char[]{(char)index, (char)(index + searchKey.length())});	
				}
			}
			
			return null;
		}
		
		private final int indexOf(String sourceStr, String targetStr) {
			char[] source = sourceStr.toCharArray();
			int sourceCount = sourceStr.length();
	        char[] target = targetStr.toCharArray();
	        int targetCount = targetStr.length();
	        
			if (0 >= sourceCount) {
				return (targetCount == 0 ? sourceCount : -1);
			}
			
			if (targetCount == 0) {
				return 0;
			}

			char first = target[0];
			int max = sourceCount - targetCount;
			int specialOffset = 0;
			for (int i = 0; i <= max; i++) {
				/* Look for first character. */
				if (source[i] != first) {
					do {
						if (' ' == source[i] || '-' == source[i]) {
							specialOffset++;
						}
					} while (++i <= max && source[i] != first);
				}

				/* Found first character, now look at the rest of v2 */
				if (i <= max) {
					int j = i + 1;
					int end = j + targetCount - 1;
					for (int k = 1; j < end; j++, k++) {
						while ((' ' == source[j] || '-' == source[j]) && j < end) {
							j++;
							end++;
						}
						
						if (source[j] != target[k]) {
							break;
						}
					}

					if (j == end) {
						/* Found whole string. */
						return i - specialOffset;
					}
				}
			}
			return -1;
		}
		//aurora add liguangyu 20131224 end
		
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
		
}
