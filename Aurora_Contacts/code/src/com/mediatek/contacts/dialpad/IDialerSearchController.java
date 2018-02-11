package com.mediatek.contacts.dialpad;

import com.mediatek.contacts.dialpad.DialerSearchController.OnDialerSearchResult;

import android.provider.BaseColumns;
import android.widget.AbsListView;
import android.widget.EditText;
import aurora.widget.AuroraEditText;

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
    public interface GnDialerSearchResultColumns {
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
		
//		String PHOTO_URI="photo_uri";
		
		String CALL_ID="call_id";
		String CALLS_COUNT="calls_count";
		String CALL_IDS="call_id";
		String VOICEMAILURI="voicemailuri";
		String PHOTO="photo";
		String AREA="area";
		
		public String[] COLUMN_NAMES = {
			_ID,
			CONTACT_ID,
			LOOKUP_KEY,
			INDICATE_PHONE_SIM,
			INDEX_IN_SIM,
			PHOTO_ID,
			NAME,
			PHONE_NUMBER,
			DATA_HIGH_LIGHT,
			NAME_PINYIN,
			PINYIN_HIGH_LIGHT,
			IS_PRIVACY,
//			PHOTO_URI,
			CALL_ID,
			CALLS_COUNT,
			CALL_IDS,
			VOICEMAILURI,
			PHOTO,
			AREA,
			
	    };
		
        public int CONTACT_ID_INDEX            = 1;
        public int LOOKUP_KEY_INDEX            = 2;
        public int INDICATE_PHONE_SIM_INDEX    = 3;
        public int INDEX_IN_SIM_INDEX          = 4;        
        public int PHOTO_ID_INDEX              = 5;
        public int NAME_INDEX                  = 6;
        public int PHONE_NUMBER_INDEX          = 7;
        public int DATA_HIGHLIGHT_INDEX        = 8;
        public int PINYIN_INDEX                = 9;
        public int PINYIN_HIGHLIGHT_INDEX      = 10;
        public int IS_PRIVACY_INDEX            = 11;
//        public int PHOTO_URI_INDEX            = 12;
        public int CALL_ID_INDEX            = 12;
        public int CALLS_COUNT_INDEX            = 13;
        public int CALL_IDS_INDEX            = 14;
        public int VOICEMAILURI_INDEX            = 15;
        public int PHOTO_INDEX            = 16;
        public int AREA_INDEX            = 17;
	}
}
