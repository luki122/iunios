package com.mediatek.contacts.dialpad;

import com.mediatek.contacts.dialpad.AuroraDialerSearchController.OnDialerSearchResult;

import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.widget.AbsListView;
import android.widget.EditText;
import aurora.widget.AuroraEditText;
import android.provider.ContactsContract.DialerSearch;

public interface IDialerSearchController {
	public interface OnDialerSearchListener {
        public void onGnDialerSearchCompleted(int count);
    }
	
	public void onResume();
	public void onPause();
	public void onStop();
	public void onDestroy();
	public void onScrollStateChanged(AbsListView view, int scrollState);
	public void updateDialerSearch();
	public void setDialerSearchTextWatcher(AuroraEditText digits);
	public void setOnDialerSearchListener(OnDialerSearchListener DialerSearchListener);
	public void setOnDialerSearchResult(OnDialerSearchResult dialerSearchResult);
	
	
    //Copy from GnDialerSearchHelper.java
    public interface AuroraDialerSearchResultColumns {
		String _ID = BaseColumns._ID;
		String CONTACT_ID = "contact_id";
		String LOOKUP_KEY = "lookup_key";
		
		String INDICATE_PHONE_SIM = "indicate_phone_sim";
		String INDEX_IN_SIM = "index_in_sim";
		String PHOTO_ID = "photo_id";
		String NAME = "name";
		
		String PHONE_NUMBER = "phone_number";
		String DATA_HIGH_LIGHT = "data_highlight_offset";
		
		String NAME_PINYIN = "name_pinyin";
		String PINYIN_HIGH_LIGHT = "pinyin_highlight_offset";
		
		String IS_PRIVACY = "is_privacy";
		
		
		public String[] COLUMNS = new String[] {
	                DialerSearch.NAME_LOOKUP_ID,
	                DialerSearch.CONTACT_ID,
	                "data_id",
	                DialerSearch.CALL_DATE,
	                DialerSearch.CALL_LOG_ID,
	                DialerSearch.CALL_TYPE,
	                DialerSearch.CALL_GEOCODED_LOCATION,
	                DialerSearch.PHONE_ACCOUNT_ID,
	                DialerSearch.PHONE_ACCOUNT_COMPONENT_NAME,
	                DialerSearch.NUMBER_PRESENTATION,
	                DialerSearch.INDICATE_PHONE_SIM,
	                DialerSearch.CONTACT_STARRED,
	                DialerSearch.PHOTO_ID,
	                DialerSearch.SEARCH_PHONE_TYPE,
	                DialerSearch.SEARCH_PHONE_LABEL,
	                DialerSearch.NAME,
	                DialerSearch.SEARCH_PHONE_NUMBER,
	                DialerSearch.CONTACT_NAME_LOOKUP,
	                DialerSearch.IS_SDN_CONTACT,
	                DialerSearch.MATCHED_DATA_OFFSET,
	                DialerSearch.MATCHED_NAME_OFFSET
	        };
		
//        public int CONTACT_ID_INDEX            = 1;
//        public int LOOKUP_KEY_INDEX            = 2;
//        public int INDICATE_PHONE_SIM_INDEX    = 3;
//        public int INDEX_IN_SIM_INDEX          = 4;        
//        public int PHOTO_ID_INDEX              = 5;
//        public int NAME_INDEX                  = 6;
//        public int PHONE_NUMBER_INDEX          = 7;
//        public int DATA_HIGHLIGHT_INDEX        = 8;
//        public int PINYIN_INDEX                = 9;
//        public int PINYIN_HIGHLIGHT_INDEX      = 10;
//        public int IS_PRIVACY_INDEX            = 11;
        
        
        public final int NAME_LOOKUP_ID_INDEX        = 0;
        public final int CONTACT_ID_INDEX            = 1;
        public final int DATA_ID_INDEX               = 2;
        public final int CALL_LOG_DATE_INDEX         = 3;
        public final int CALL_LOG_ID_INDEX           = 4;
        public final int CALL_TYPE_INDEX             = 5;
        public final int CALL_GEOCODED_LOCATION_INDEX = 6;
        public final int PHONE_ACCOUNT_ID_INDEX                = 7;
        public final int PHONE_ACCOUNT_COMPONENT_NAME_INDEX     = 8;
        public final int PRESENTATION_INDEX          = 9;
        public final int INDICATE_PHONE_SIM_INDEX    = 10;
        public final int CONTACT_STARRED_INDEX       = 11;
        public final int PHOTO_ID_INDEX              = 12;
        public final int SEARCH_PHONE_TYPE_INDEX     = 13;
        public final int SEARCH_PHONE_LABEL_INDEX    = 14;
        public final int NAME_INDEX                  = 15;
        public final int SEARCH_PHONE_NUMBER_INDEX   = 16;
        public final int CONTACT_NAME_LOOKUP_INDEX   = 17;
        public final int IS_SDN_CONTACT              = 18;
        public final int DS_MATCHED_DATA_OFFSETS     = 19;
        public final int DS_MATCHED_NAME_OFFSETS     = 20;
        public final int IS_PRIVACY_INDEX            = 21;
        
	}
}
