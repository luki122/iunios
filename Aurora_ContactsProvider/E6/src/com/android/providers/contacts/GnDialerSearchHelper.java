package com.android.providers.contacts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sourceforge.pinyin4j.ChineseToPinyinHelper;

import com.android.providers.contacts.ContactsDatabaseHelper.DataColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.android.providers.contacts.ContactsDatabaseHelper.Views;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.BaseColumns;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

public class GnDialerSearchHelper {
    private static final String TAG = "GnDialerSearchHelper";
	private static final int MAX_NUMBER_INDEX_START = 16;
	public static final String GN_DIALER_SEARCH_TABLE = "gn_dialer_search";
	public static final String GN_DIALER_SEARCH_VIEW = "view_gn_dialer_search";
	
	private static final String TEMP_DIALER_SEARCH_VIEW_TABLE = "gn_dialer_search_temp";
	
	//Gionee:huangzy 20130326 add for CR00786343 start
	private static final boolean SKIP_ONE_NUMBER_SEARCH = true;
	//Gionee:huangzy 20130326 add for CR00786343 end
	
	public class SearchResultCache {
		private static final String SPLITER = ",";
		public final String mSearchKey;
		private StringBuilder mRawContactIds;
		private int mIdCount = 0;
		
		public SearchResultCache(String searchKey) {
			mSearchKey = searchKey;
			mRawContactIds = new StringBuilder();
		}
		
		public void addId(String rawContactId) {
			mRawContactIds.append(rawContactId).append(SPLITER);
			++mIdCount;
		}
		
		public boolean isEmpty() {
			return mIdCount == 0;
		}
		
		public String getRawContactIds() {
			int len = mRawContactIds.length();
			if (len > 0 && mRawContactIds.substring(len - 1, len).equals(SPLITER)) {
				return mRawContactIds.subSequence(0, len - 1).toString();
			}
			return mRawContactIds.toString();
		}
	}

	public interface GnDialerSearchColumns {
		String _ID = BaseColumns._ID;
		String RAW_CONTACT_ID = "raw_contact_id";
		String QUAN_PINYIN = "quan_pinyin";
		String JIAN_PINYIN = "jian_pinyin";
		String QUAN_T9 = "quan_t9";
		String JIAN_T9 = "jian_t9";
		String MATCH_MAP = "match_map_quan";
		String MATCH_MAP_JIAN = "match_map_jian";
		String POLYPHONIC = "polyphonic";
		String SORT_KEY = "sort_key";
		String QUAN_PINYIN_HIGHLIGHT = "quan_pinyin_highlight";
		String MATCH_MAP_HIGHLIGHT = "match_map_quan_highlight";
		
		String[] COLUMN_NAMES = new String[]{
				RAW_CONTACT_ID,
				QUAN_PINYIN,
				JIAN_PINYIN,
				QUAN_T9,
				JIAN_T9,
				MATCH_MAP,
				MATCH_MAP_JIAN,
				POLYPHONIC,
				SORT_KEY,
				QUAN_PINYIN_HIGHLIGHT,
				MATCH_MAP_HIGHLIGHT,
				};
		
		int RCI_INDEX = 0;
		int QUAN_PINYIN_INDEX = 1;
		int JIAN_PINYIN_INDEX = 2;
		int QUAN_T9_INDEX = 3;
		int JIAN_T9_INDEX = 4;
		int MATCH_MAP_INDEX = 5;
		int MATCH_MAP_JIAN_INDEX = 6;
		int POLYPHONIC_INDEX = 7;
		int SORT_KEY_INDEX = 8;
		int QUAN_PINYIN_HIGHLIGHT_INDEX = 9;
		int MATCH_MAP_HIGHLIGHT_INDEX = 10;
	}
	
	public interface GnDialerSearchViewColumns {
		String _ID = BaseColumns._ID;
		String RAW_CONTACT_ID = "vds_raw_contact_id";
		String CONTACT_ID = "vds_contact_id";
		String LOOKUP_KEY = "vds_lookup";
		String NAME = "vds_phone_name";
		String PHONE_NUMBER = "vds_phone_number";
		String PHOTO_ID = "vds_photo_id";
		String INDICATE_PHONE_SIM = "vds_indicate_phone_sim";
		String INDEX_IN_SIM = "vds_index_in_sim";
		String QUAN_PINYIN = "vds_quan_pinyin";
		String JIAN_PINYIN = "vds_jian_pinyin";
		
		String TIMES_CONTACTED = "vds_times_contacted";
		String QUAN_T9 = "vds_quan_t9";
		String JIAN_T9 = "vds_jian_t9";
		String MATCH_MAP = "vds_match_map_quan";
		String MATCH_MAP_JIAN = "vds_match_map_jian";
		String POLYPHONIC = "vds_polyphonic";
		String SORT_KEY = "vds_sort_key";
		String HAS_PHONE_NUMBER = "vds_has_phone_number";
		String MIME_TYPE = "vds_mimetype_id";
		String ACCOUNT_NAME = "vds_account_name";
		String ACCOUNT_TYPE = "vds_account_type";
		String IN_VISIBLE_GROUP = "vds_in_visible_group";
		String QUAN_PINYIN_HIGHLIGHT = "vds_quan_pinyin_highlight";
		String MATCH_MAP_HIGHLIGHT = "vds_match_map_quan_highlight";
		String AUTO_RECORD = "vds_auto_record";
		String IS_PRIVACY = "vds_is_privacy";
		
		String[] COLUMN_NAMES = new String[]{
				 RAW_CONTACT_ID,
				 CONTACT_ID,
				 LOOKUP_KEY,
				 NAME,
				 PHONE_NUMBER,
				 PHOTO_ID,
				 INDICATE_PHONE_SIM,
				 INDEX_IN_SIM,
				 QUAN_PINYIN,
				 JIAN_PINYIN,
				 
				 TIMES_CONTACTED,
				 QUAN_T9,
				 JIAN_T9,
				 MATCH_MAP,
				 MATCH_MAP_JIAN,
				 POLYPHONIC,
				 SORT_KEY,
				 HAS_PHONE_NUMBER,
				 MIME_TYPE,
				 ACCOUNT_NAME,
				 ACCOUNT_TYPE,
				 IN_VISIBLE_GROUP,
				 QUAN_PINYIN_HIGHLIGHT,
				 MATCH_MAP_HIGHLIGHT,
				 AUTO_RECORD,
				 IS_PRIVACY,
			};
		
		int RCI_INDEX = 0;
		int CONTACT_ID_INDEX = 1;
		int LOOKUP_KEY_INDEX = 2;
		int NAME_INDEX = 3;
		int PHONE_NUMBER_INDEX = 4;
		int PHOTO_ID_INDEX = 5;
		int INDICATE_PHONE_SIM_INDEX = 6;
		int INDEX_IN_SIM_INDEX = 7;
		int QUAN_PINYIN_INDEX = 8;
		int JIAN_PINYIN_INDEX = 9;
		
		int TIMES_CONTACTED_INDEX = 10;
		int QUAN_T9_INDEX = 11;
		int JIAN_T9_INDEX = 12;
		int MATCH_MAP_QUAN_INDEX = 13;
		int MATCH_MAP_JIAN_INDEX = 14;
		int POLYPHONIC_INDEX = 15;
		int SORT_KEY_INDEX = 16;
		int HAS_PHONE_NUMBER_INDEX = 17;
		int MIME_TYPE_INDEX = 18;
		int QUAN_PINYIN_HIGHLIGHT_INDEX = 22;
		int MATCH_MAP_HIGHLIGHT_INDEX = 23;
		
		int IS_PRIVACY_INDEX = 25;
	}
	
	private class GnDialerSearchViewRow implements GnDialerSearchViewColumns {
		String mRawContactId;
		String mContactId;
		String mLookupKey;
		String mName;
		String mIndicatePhoneSim;
		String mIndexInSim;
		String mPhotoId;
		String mPhoneNumber;
		private String mDataHighlight;
		int mTimesContacted;
		int mPolyphonic;
		
		String mQuanPinyin;
		String mSortKey;
		String mJianPinyin;
		private String mPinyinHighlight;
		String mQuanT9;
		String mJianT9;
		String mMatchMapQuan;
		String mMatchMapJian;
		int mQuanT9Len;
		int mJianT9Len;
		
		private String[] mQuanPinyinMulti;
		private String[] mJianPinyinMulti;
		private String[] mJianT9Multi;
		private String[] mQuanT9Multi;
		private String[] mMatchMapQuanMulti;
		private String[] mMatchMapJianMulti;
		private String[] mSortKeyMulti;
		private int mMultiIndex;
		
		String mMatchMapQuanHighlight;
		private String[] mMatchMapQuanMultiHighlight;
		String mQuanPinyinHighlight;
		private String[] mQuanPinyinMultiHighlight;
		
		private String mPrivacyId;
		
		public void read(Cursor cursor, String searchKey) {
			mRawContactId = cursor.getString(RCI_INDEX);
			mContactId = cursor.getString(CONTACT_ID_INDEX);
			mLookupKey = cursor.getString(LOOKUP_KEY_INDEX);
			mName = cursor.getString(NAME_INDEX);
			mIndicatePhoneSim = cursor.getString(INDICATE_PHONE_SIM_INDEX);
			mIndexInSim = cursor.getString(INDEX_IN_SIM_INDEX);
			mPhotoId = cursor.getString(PHOTO_ID_INDEX);
			mPhoneNumber = cursor.getString(PHONE_NUMBER_INDEX);
			/*mPhoneNumber = DialerSearchUtils.stripSpecialCharInNumberForDialerSearch(
					cursor.getString(PHONE_NUMBER_INDEX));
			*/
			mDataHighlight = getDataHighlight(mPhoneNumber, searchKey);
			mTimesContacted = cursor.getInt(TIMES_CONTACTED_INDEX);
			mPolyphonic = cursor.getInt(POLYPHONIC_INDEX);
			
			mPrivacyId = String.valueOf(cursor.getLong(IS_PRIVACY_INDEX));
			
			if (0 == mPolyphonic) {
				mQuanPinyin = cursor.getString(QUAN_PINYIN_INDEX);
				
				mQuanPinyinHighlight = cursor.getString(QUAN_PINYIN_HIGHLIGHT_INDEX);
				mMatchMapQuanHighlight = cursor.getString(MATCH_MAP_HIGHLIGHT_INDEX);
				
				mJianPinyin = cursor.getString(JIAN_PINYIN_INDEX);
				mQuanT9 = cursor.getString(QUAN_T9_INDEX);
				mJianT9 = cursor.getString(JIAN_T9_INDEX);
				mMatchMapQuan = cursor.getString(MATCH_MAP_QUAN_INDEX);
				mMatchMapJian = cursor.getString(MATCH_MAP_JIAN_INDEX);
				mJianT9Len = mJianT9.length();
				mQuanT9Len = mQuanT9.length();
				mSortKey = cursor.getString(SORT_KEY_INDEX);
			} else {
				mQuanPinyinMulti = cursor.getString(QUAN_PINYIN_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				
				mQuanPinyinMultiHighlight = cursor.getString(QUAN_PINYIN_HIGHLIGHT_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mMatchMapQuanMultiHighlight = cursor.getString(MATCH_MAP_HIGHLIGHT_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				
				mJianPinyinMulti = cursor.getString(JIAN_PINYIN_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mQuanT9Multi = cursor.getString(QUAN_T9_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mJianT9Multi = cursor.getString(JIAN_T9_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mMatchMapQuanMulti = cursor.getString(MATCH_MAP_QUAN_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mMatchMapJianMulti = cursor.getString(MATCH_MAP_JIAN_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mSortKeyMulti = cursor.getString(SORT_KEY_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				
				pickInMulti(0);
			}
		}
		
		private void pickInMulti(int multiIndex) {
			mQuanPinyin = mQuanPinyinMulti[multiIndex];
			mJianPinyin = mJianPinyinMulti[multiIndex];
			mQuanT9 = mQuanT9Multi[multiIndex];
			mJianT9 = mJianT9Multi[multiIndex];
			mMatchMapQuan = mMatchMapQuanMulti[multiIndex];
			mMatchMapJian = mMatchMapJianMulti[multiIndex];
			mSortKey = mSortKeyMulti[multiIndex];
			mJianT9Len = mJianT9.length();
			mQuanT9Len = mQuanT9.length();			
			mPinyinHighlight = null;
			
			mQuanPinyinHighlight = mQuanPinyinMultiHighlight[multiIndex];
			mMatchMapQuanHighlight = mMatchMapQuanMultiHighlight[multiIndex];
		}
		
		public boolean next() {
			if (mPolyphonic == 0 || mMultiIndex >= mQuanPinyinMulti.length - 1) {
				return false;
			}
			
			++mMultiIndex;
			pickInMulti(mMultiIndex);
			
			return true;
		}
		
		// is [)
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
		
		public void setPinyinHighlight(char[] nameHighlight) {
			mPinyinHighlight = String.valueOf(nameHighlight);
		}
		
		public int getDataMatchIndex() {
			if (null == mDataHighlight) {
				return -1;
			}
			
			return mDataHighlight.charAt(0);
		}
		
		public Object[] getGnDialerSearchResultRow() {
			Object[] objs = new Object[GnDialerSearchResultColumns.COLUMN_NAMES.length];
			
			objs[GnDialerSearchResultColumns.CONTACT_ID_INDEX] = mContactId;
			objs[GnDialerSearchResultColumns.LOOKUP_KEY_INDEX] = mLookupKey;
			objs[GnDialerSearchResultColumns.INDICATE_PHONE_SIM_INDEX] = mIndicatePhoneSim;
			objs[GnDialerSearchResultColumns.INDEX_IN_SIM_INDEX] = mIndexInSim;
			objs[GnDialerSearchResultColumns.PHOTO_ID_INDEX] = mPhotoId;
			objs[GnDialerSearchResultColumns.NAME_INDEX] = mName;
			objs[GnDialerSearchResultColumns.PHONE_NUMBER_INDEX] = mPhoneNumber;
			objs[GnDialerSearchResultColumns.DATA_HIGHLIGHT_INDEX] = mDataHighlight;
			objs[GnDialerSearchResultColumns.PINYIN_INDEX] = mQuanPinyinHighlight;//mQuanPinyin;
			objs[GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX] = mPinyinHighlight;
			
			objs[GnDialerSearchResultColumns.IS_PRIVACY_INDEX] = mPrivacyId;
			
			return objs;
		}
	}
	
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
		
		String QUAN_PINYIN = "quan_pinyin";
		String PINYIN_HIGH_LIGHT = "pinyin_highlight_offset";
		
		String IS_PRIVACY = "is_privacy";
		
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
			QUAN_PINYIN,
			PINYIN_HIGH_LIGHT,
			IS_PRIVACY,
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
	}
	
	private static GnDialerSearchHelper mGnDialerSearchHelper = new GnDialerSearchHelper();
	private Map<String, SearchResultCache> mSearchRetCache = new HashMap<String, SearchResultCache>();
	
	private GnDialerSearchHelper() {
	}
	
	public static GnDialerSearchHelper getInstance() {
		return mGnDialerSearchHelper;
	}
	
	private void clearCache() {
		mSearchRetCache.clear();
	}
	
	private void clearCache(String searchKey) {
		if (TextUtils.isEmpty(searchKey)) {
			mSearchRetCache.clear();
			return;
		}
		
		Iterator<Entry<String, SearchResultCache>> iterator = mSearchRetCache.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, SearchResultCache> entry = iterator.next();
			if (!searchKey.startsWith(entry.getKey())) {
				iterator.remove();
			}
		}
	}
	
	private void writeCache(SearchResultCache searchRetCache) {
		mSearchRetCache.put(searchRetCache.mSearchKey, searchRetCache);
	}
	
	private SearchResultCache getCache(String searchKey) {
		if (!TextUtils.isEmpty(searchKey)) {
			SearchResultCache cache = mSearchRetCache.get(searchKey);
			if (null != cache) {
				return cache;
			}
			
			if (searchKey.length() > 1) {
				return mSearchRetCache.get(searchKey.substring(0, searchKey.length() - 1));
			}
		}
		
		return null;
	}
	
	public void init(SQLiteDatabase db) {
		Log.i("James", "GnDialerSearchHelperV2  init");
		clearCache();
		
		db.execSQL("DROP TABLE IF EXISTS " + TEMP_DIALER_SEARCH_VIEW_TABLE);
		db.execSQL("CREATE TEMP TABLE  " + TEMP_DIALER_SEARCH_VIEW_TABLE 
                + " AS SELECT * FROM " + GN_DIALER_SEARCH_VIEW);
	}
	
	public Cursor query(SQLiteDatabase db, Uri uri, String selection) {
		String searchKey = "";
        String uriStr = uri.toString();
        if (uriStr.indexOf("gn_dialer_search/") > 0) {
        	searchKey = uriStr.substring(uriStr.lastIndexOf("/")+1);
        } else {
        	return null;
        }
        
        boolean isAllDigit = true;
        for (char c : searchKey.toCharArray()) {
        	if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
        		isAllDigit = false;
        		searchKey = searchKey.toLowerCase();
        		break;
        	}
        }
        
        if (isAllDigit) {
        	return queryDigit(db, searchKey, selection);
        } else {
        	return queryAbc(db, searchKey, selection);
        }
	}
	
	public Cursor queryMutil(SQLiteDatabase db, Uri uri, String selection) {
	    String searchKey = "";
        String uriStr = uri.toString();
        if (uriStr.indexOf("aurora_multi_search/") > 0) {
            searchKey = uriStr.substring(uriStr.lastIndexOf("/")+1);
        } else {
            return null;
        }
        
        boolean hasHanzi = ContactsDatabaseHelper.hasHanZi(searchKey);
        boolean isAllDigit = hasHanzi ? false : true;
        
        if (isAllDigit) {
            for (char c : searchKey.toCharArray()) {
                if (9 < c && c < 0) {
                    isAllDigit = false;
                    searchKey = searchKey.toLowerCase();
                    break;
                }
            }
        }
        
        StringBuffer sb = new StringBuffer();
        if (!isAllDigit) {
            for (char c : searchKey.toCharArray()) {
                if (ContactsDatabaseHelper.hasHanZi(String.valueOf(c))) {
                    String str = ChineseToPinyinHelper.translateMulti(String.valueOf(c), true)[0][0];
                    sb.append(str);
                } else {
                    sb.append(c);
                }
            }
            
            if (sb != null) {
                searchKey = sb.toString();
            }
        }
        
        if (isAllDigit) {
            return queryDigit(db, searchKey, selection);
        } else {
            return queryContactId(db, searchKey, selection);
        }
	}
	
	private void addToListSort(List<GnDialerSearchViewRow> list, GnDialerSearchViewRow sdr) {
		if (sdr.mTimesContacted > 0) {
			for (int i = 0, size = list.size() - 1; i <= size; ++i) {
				if (sdr.mTimesContacted > list.get(i).mTimesContacted) {
					list.add(i, sdr);
					return;
				}
			}
		}
		
		list.add(sdr);
	}
	
	protected Cursor queryDigit(SQLiteDatabase db, String searchKey, String selec) {
		final int KEY_LEN = searchKey.length();
//		Long startTime = System.currentTimeMillis();
	    
        Cursor queryCursor = null;
        {
        	String selection = "(" + GnDialerSearchViewColumns.JIAN_T9 + " LIKE '%" + searchKey.charAt(0) + "%' OR " +
        	GnDialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%')";
        	
            if (!searchKey.startsWith(QUERY_ABC_PREFIX)) {
                selection = selection + " AND ("
                        + GnDialerSearchViewColumns.MIME_TYPE + " = 5)";
            }
        	
        	boolean skip = false;
        	if (KEY_LEN > 1) {   		
        		SearchResultCache searchRetCache = getCache(searchKey);
        		skip = (null != searchRetCache && searchRetCache.isEmpty()); 
        		if (!skip) {
        	    	if (null != searchRetCache) {
        	    		selection = selection + " AND (" + 
        	    		GnDialerSearchViewColumns.RAW_CONTACT_ID + 
            			" IN (" + searchRetCache.getRawContactIds() + "))";	
        	    	}
                }
        	}
        	
        	if (!skip) {
        	    if (selec != null) {
        	        selection = selection + " AND " + selec;
        	    }
        		queryCursor = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, GnDialerSearchViewColumns.COLUMN_NAMES, selection,
    		    		null, null, null, GnDialerSearchViewColumns.SORT_KEY);	
        	}
        }
        
        List<GnDialerSearchViewRow> matchedList = null;
       	//Gionee:huangzy 20130326 modify for CR00786343 start
        int onlyNumberMatchedStart = 0;
       	//Gionee:huangzy 20130326 modify for CR00786343 end
        MatrixCursor resultCursor = null;
        if (queryCursor != null && queryCursor.moveToFirst()) {
        	List<GnDialerSearchViewRow> fullMatchedList = new LinkedList<GnDialerSearchViewRow>();
        	List<GnDialerSearchViewRow> firstJianMatchedList = new LinkedList<GnDialerSearchViewRow>();
        	List<GnDialerSearchViewRow> firstPartMatchedList = new LinkedList<GnDialerSearchViewRow>();
        	List<GnDialerSearchViewRow> partMatchedList = new LinkedList<GnDialerSearchViewRow>();        	
        	List<GnDialerSearchViewRow> numberMatchedList = new LinkedList<GnDialerSearchViewRow>();
        	int[] numberMatchedListMark = new int[MAX_NUMBER_INDEX_START];
        	
            do {
                try {
            	GnDialerSearchViewRow sdr = new GnDialerSearchViewRow();
            	sdr.read(queryCursor, searchKey);
            	int index = -1;
            	do {
            		String JIAN = sdr.mJianT9;
            		String QUAN = sdr.mQuanT9;
            		
	            	boolean isJianMatch = false;
	            	if (sdr.mJianT9Len >= KEY_LEN) {
	    				index = JIAN.indexOf(searchKey);
	    				isJianMatch = (-1 != index);
	    			}
	    			
	    			if (isJianMatch) {
	    				char[] pinyinHighlight = Arrays.copyOfRange(sdr.mMatchMapJian.toCharArray(),
								index * 2, (index + searchKey.length())*2);
	    				sdr.setPinyinHighlight(pinyinHighlight);    				
					} else {
	    				index = QUAN.indexOf(searchKey);
	    			}
	    			
	    			char[] matchMapQuan = sdr.mMatchMapQuan.toCharArray();
	    			char[] matchMapQuanHighlight = sdr.mMatchMapQuanHighlight.toCharArray(); // add for highlight
	    			
	    			if (0 == index) {
	    				if (!isJianMatch) {
//	    					sdr.setPinyinHighlight(new char[]{0, (char)(KEY_LEN)}); // modify for wangth highlight 20140227
	    					sdr.setPinyinHighlight(new char[]{matchMapQuanHighlight[0], (char)(matchMapQuanHighlight[0] + KEY_LEN)});
	    				}
	    				
	    				if (KEY_LEN == sdr.mQuanT9Len) {
	    					addToListSort(fullMatchedList, sdr);    					
	    				} else {
	    					if (isJianMatch) {
	    						addToListSort(firstJianMatchedList, sdr);    						
	    					} else {
	    						addToListSort(firstPartMatchedList, sdr);
	    					}
	    				}
	    			} else if (-1 != index) {
	    				boolean isMatch = isJianMatch;
	    				if (!isMatch) {
	    					for (int i = 0, len = matchMapQuan.length; i < len; i+=2) {
	    						if (matchMapQuan[i] == index) {
//	    							char[] matchOffset = new char[]{matchMapQuan[i], (char)(matchMapQuan[i] + KEY_LEN)}; // modify for wangth highlight 20140227
	    						    char[] matchOffset = new char[]{matchMapQuanHighlight[i], (char)(matchMapQuanHighlight[i] + KEY_LEN)};
	    							sdr.setPinyinHighlight(matchOffset);    							
	    	    					isMatch = true;
	    							break;
	    						}
	    					}    					
	    				}
	    				if (!isMatch) {
							index = -1;
						} else {
							addToListSort(partMatchedList, sdr);
						}
	    			}
	    			
	    			if (-1 == index) {
	    				String[] sqlits = new String[matchMapQuan.length/2];
	    				for (int i = 0; i < sqlits.length; ++i) {
	    					sqlits[i] = QUAN.substring(matchMapQuan[i*2], matchMapQuan[i*2 + 1]);
	    				}
	    				
	    				for (int i = 0, srcLen = sdr.mQuanT9Len; i < sqlits.length; ++i) {
	    					String tmpSearchKey = new String(searchKey);
	    					if (tmpSearchKey.charAt(0) != sqlits[i].charAt(0)) {
	    						if (i > 0) {
	    							srcLen -= sqlits[i - 1].length();
	        						if (srcLen < KEY_LEN) {
	        							break;
	        						}	
	    						}
	    						continue;
	    					}
	
	    					char keyFirshChar;
	        				String curSqlit;
	        				int matchIndex = 0;
	        				char[] nameHighlight = new char[(matchMapQuan.length - i)*2];
	    					for (int j = i; j < sqlits.length; ++j) {
	    						curSqlit = sqlits[j];
	    						keyFirshChar = tmpSearchKey.charAt(0);
	    						if (curSqlit.charAt(0) == keyFirshChar) {
//	    							nameHighlight[matchIndex++] = matchMapQuan[j*2];
	    						    nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2]; // modify for wangth highlight 20140227
	    							
	        						if (tmpSearchKey.startsWith(curSqlit)) {
	        							String afterSub = tmpSearchKey.substring(curSqlit.length());
        								if (tmpSearchKey.length() > 1 && afterSub.length() > 0 && 
        										j + 1 < sqlits.length) {
        									char nextSqlitFirst = sqlits[j + 1].charAt(0);
        									if (nextSqlitFirst == tmpSearchKey.charAt(1) &&
        											nextSqlitFirst != afterSub.charAt(0)) {
//        										nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + 1);
        									    nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + 1); // modify for wangth highlight 20140227
        	        							tmpSearchKey = tmpSearchKey.substring(1);
        	        							afterSub = null;
        									}
        								}
        								if (null != afterSub) {
//        									nameHighlight[matchIndex++] = matchMapQuan[j*2+1];
        								    nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2+1]; // modify for wangth highlight 20140227
        									tmpSearchKey = afterSub;
        								}
	        						} else if (curSqlit.startsWith(tmpSearchKey)) {
//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + tmpSearchKey.length());
	        						    nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + tmpSearchKey.length()); // modify for wangth highlight 20140227
	        							index = nameHighlight[0];
	        							break;
	        						} else {
//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + 1);
	        						    nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + 1); // modify for wangth highlight 20140227
	        							tmpSearchKey = tmpSearchKey.substring(1);
	        						}
	        						
	        						if (tmpSearchKey.length() == 0) {
	        							index = nameHighlight[0];
	        							break;
	        						}
	        					} else {
	        						index = -1;
	        						break;
	        					}
	    					}
	    					
	    					if (-1 != index) {
	    						sdr.setPinyinHighlight(nameHighlight);
	    						if (nameHighlight[0] == 0) {
	    							addToListSort(firstPartMatchedList, sdr);
	    						} else {
	    							addToListSort(partMatchedList, sdr);
	    						}
								break;
	    					}
	    				}
	    			}
            	} while (-1 == index && sdr.next());
    			
    			if (-1 == index) {
    				index = sdr.getDataMatchIndex();   //NumberMatched if index > -1
    				if (-1 != index) {
            			if (index >= MAX_NUMBER_INDEX_START) {
            				index = MAX_NUMBER_INDEX_START - 1;
            			}
            			
            			int size = numberMatchedList.size();
            			{
            				if (sdr.mTimesContacted < 1) {
                				int location = numberMatchedListMark[index];
                    			if (location != size) {
                    				numberMatchedList.add(location, sdr);
                    			} else {
                    				numberMatchedList.add(sdr);
                    			}
                			} else {
                				boolean isAdded = false;
                				for (int i = 0; i < size; ++i) {
                					if (sdr.mTimesContacted > numberMatchedList.get(i).mTimesContacted) {
                						numberMatchedList.add(i, sdr);
                						isAdded = true;                						
                						break;
                					}
                				}
                				if (!isAdded) {
                					numberMatchedList.add(sdr);
                				}
                				index = 0;
                			}	
            			}
            			
            			for (int i = index; i < MAX_NUMBER_INDEX_START; ++i) {
            				++numberMatchedListMark[i];
            			}
        			}
    			}
                } catch (Exception e) {
    			    e.printStackTrace();
    			    continue;
    			}
            } while(queryCursor.moveToNext());
            
            matchedList = new LinkedList<GnDialerSearchViewRow>();
            if (fullMatchedList.size() > 0) {
            	matchedList.addAll(fullMatchedList);	
            }
            
            matchedList.addAll(firstJianMatchedList);
            matchedList.addAll(firstPartMatchedList);
            matchedList.addAll(partMatchedList);
        	//Gionee:huangzy 20130326 modify for CR00786343 start
            if (KEY_LEN == 1) {
            	onlyNumberMatchedStart = matchedList.size();
            }
        	//Gionee:huangzy 20130326 modify for CR00786343 end
            matchedList.addAll(numberMatchedList);
        }
        
        if (null != queryCursor) {
    		queryCursor.close();
    		queryCursor = null;
    	}               
        
        if (KEY_LEN > 1) {
    		SearchResultCache searchRetCache = new SearchResultCache(searchKey);
    		if (null != matchedList) {
    			for (GnDialerSearchViewRow sdr : matchedList) {
        			searchRetCache.addId(sdr.mRawContactId);
        		}
    		}
    		writeCache(searchRetCache);
    	}
        
        if (null != matchedList && matchedList.size() > 0) {
        	resultCursor = new MatrixCursor(GnDialerSearchResultColumns.COLUMN_NAMES);
        	//Gionee:huangzy 20130326 modify for CR00786343 start
        	int size = KEY_LEN == 1  && SKIP_ONE_NUMBER_SEARCH ?
        			onlyNumberMatchedStart : matchedList.size();

        	Iterator<GnDialerSearchViewRow> iterator = matchedList.iterator();
        	int count = 0;
        	while(iterator.hasNext() && count < size) {
        		GnDialerSearchViewRow sdr = iterator.next();
        		resultCursor.addRow(sdr.getGnDialerSearchResultRow());
        		count++;
        	}
        	//Gionee:huangzy 20130326 modify for CR00786343 end
			matchedList = null;
		}
        
//        Log.i("James", "queryDialerSearch : " + searchKey + "  use time " + (System.currentTimeMillis() - startTime) + "ms");
        
        return resultCursor;
    }
	
	//************
	public void createGnDialerSearchTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + GN_DIALER_SEARCH_TABLE + ";");
		db.execSQL("CREATE TABLE " + GN_DIALER_SEARCH_TABLE + " ("
				+ GnDialerSearchColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ GnDialerSearchColumns.RAW_CONTACT_ID + " INTEGER REFERENCES raw_contacts(_id) NOT NULL,"
	            + GnDialerSearchColumns.QUAN_PINYIN + " VARCHAR DEFAULT NULL,"
	            + GnDialerSearchColumns.JIAN_PINYIN + " VARCHAR DEFAULT NULL,"
	            + GnDialerSearchColumns.QUAN_T9 + " VARCHAR DEFAULT NULL,"
	            + GnDialerSearchColumns.JIAN_T9 + " VARCHAR DEFAULT NULL,"
	            + GnDialerSearchColumns.MATCH_MAP + " VARCHAR DEFAULT NULL,"
	            + GnDialerSearchColumns.MATCH_MAP_JIAN + " VARCHAR DEFAULT NULL,"
	            + GnDialerSearchColumns.POLYPHONIC + " INTEGER DEFAULT 0,"
	            + GnDialerSearchColumns.SORT_KEY + " VARCHAR DEFAULT NULL,"
	            
	            + GnDialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + " VARCHAR DEFAULT NULL," // add by wangth for highlight
	            + GnDialerSearchColumns.MATCH_MAP_HIGHLIGHT + " VARCHAR DEFAULT NULL"
				+ ");");
		
		db.execSQL("CREATE INDEX gn_dialer_search_raw_contact_id_index ON "
				+ GN_DIALER_SEARCH_TABLE + " ("
				+ GnDialerSearchColumns.RAW_CONTACT_ID + ");");	
	}
	
	public void createGnDialerSearchView(SQLiteDatabase db) {
		db.execSQL("DROP VIEW IF EXISTS " + GN_DIALER_SEARCH_VIEW + ";");
		String GDS_DETAIL_TABLE = "contacts_detail";
		String GDS_NUMBER_TABLE = "number_data";
		String RAW_CONTACT_ID = "raw_contact_id";
		
		String VIEW_SELECT = "SELECT " + 
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.RAW_CONTACT_ID +
		  " AS " + GnDialerSearchViewColumns.RAW_CONTACT_ID + "," +
		GDS_DETAIL_TABLE + "." + RawContacts.CONTACT_ID + 
		  " AS " + GnDialerSearchViewColumns.CONTACT_ID + "," +
		GDS_DETAIL_TABLE + "." + Contacts.LOOKUP_KEY + 
		  " AS " + GnDialerSearchViewColumns.LOOKUP_KEY + "," +
		GDS_DETAIL_TABLE + "." + RawContacts.DISPLAY_NAME_PRIMARY + 
		  " AS " + GnDialerSearchViewColumns.NAME + "," +
		GDS_NUMBER_TABLE + "." + GnDialerSearchViewColumns.PHONE_NUMBER +
		  " AS " + GnDialerSearchViewColumns.PHONE_NUMBER + "," +
		GDS_DETAIL_TABLE + "." + Contacts.PHOTO_ID + 
		  " AS " + GnDialerSearchViewColumns.PHOTO_ID + "," +
		GDS_DETAIL_TABLE + "." + RawContacts.INDICATE_PHONE_SIM + 
		  " AS " + GnDialerSearchViewColumns.INDICATE_PHONE_SIM + "," +
		GDS_DETAIL_TABLE + "." + RawContacts.INDEX_IN_SIM + 
		  " AS " + GnDialerSearchViewColumns.INDEX_IN_SIM + "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.QUAN_PINYIN + 
		  " AS " + GnDialerSearchViewColumns.QUAN_PINYIN + "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.JIAN_PINYIN + 
		  " AS " + GnDialerSearchViewColumns.JIAN_PINYIN + "," +
		  
		GDS_DETAIL_TABLE + "." + Contacts.TIMES_CONTACTED + 
		  " AS " + GnDialerSearchViewColumns.TIMES_CONTACTED + "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.JIAN_T9 + 
		  " AS " + GnDialerSearchViewColumns.JIAN_T9 + "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.QUAN_T9 + 
		  " AS " + GnDialerSearchViewColumns.QUAN_T9 + "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.MATCH_MAP + 
		  " AS " + GnDialerSearchViewColumns.MATCH_MAP + "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.MATCH_MAP_JIAN + 
		  " AS " + GnDialerSearchViewColumns.MATCH_MAP_JIAN + "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.POLYPHONIC + 
		  " AS " + GnDialerSearchViewColumns.POLYPHONIC +  "," +
		GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.SORT_KEY + 
		  " AS " + GnDialerSearchViewColumns.SORT_KEY + "," +
		GDS_DETAIL_TABLE + "." + Contacts.HAS_PHONE_NUMBER + 
          " AS " + GnDialerSearchViewColumns.HAS_PHONE_NUMBER + "," +
        GDS_NUMBER_TABLE + "." + DataColumns.MIMETYPE_ID + 
        " AS " + GnDialerSearchViewColumns.MIME_TYPE + "," +
        GDS_DETAIL_TABLE + "." + RawContacts.ACCOUNT_NAME +
        " AS " + GnDialerSearchViewColumns.ACCOUNT_NAME + "," +
        GDS_DETAIL_TABLE + "." + RawContacts.ACCOUNT_TYPE +
        " AS " + GnDialerSearchViewColumns.ACCOUNT_TYPE + "," +
        GDS_DETAIL_TABLE + "." + Contacts.IN_VISIBLE_GROUP + 
        " AS " + GnDialerSearchViewColumns.IN_VISIBLE_GROUP +
        
        // add for fuhao highlight
        "," + GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + 
        " AS " + GnDialerSearchViewColumns.QUAN_PINYIN_HIGHLIGHT + "," +
        GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.MATCH_MAP_HIGHLIGHT + 
        " AS " + GnDialerSearchViewColumns.MATCH_MAP_HIGHLIGHT +
        
        // add for auto_record
        "," + GDS_NUMBER_TABLE + "." + "auto_record" +
        " AS " + GnDialerSearchViewColumns.AUTO_RECORD +
        
        // add for privacy
        "," + GDS_NUMBER_TABLE + "." + "is_privacy" +
        " AS " + GnDialerSearchViewColumns.IS_PRIVACY +
        
		" FROM " +
		GN_DIALER_SEARCH_TABLE + " LEFT JOIN " +
		" (SELECT " +
		Tables.CONTACTS + "." + Contacts._ID + " AS " + RawContacts.CONTACT_ID + "," +
		Tables.CONTACTS + "." + Contacts.LOOKUP_KEY + " AS " + Contacts.LOOKUP_KEY + "," +
		Tables.CONTACTS + "." + Contacts.PHOTO_ID + " AS " + Contacts.PHOTO_ID + "," +
		Tables.CONTACTS + "." + Contacts.TIMES_CONTACTED + " AS " + Contacts.TIMES_CONTACTED + "," +
		Tables.CONTACTS + "." + Contacts.HAS_PHONE_NUMBER + " AS " + Contacts.HAS_PHONE_NUMBER + "," +
		Tables.RAW_CONTACTS + "." + RawContacts._ID + " AS " + RAW_CONTACT_ID + "," +
		Tables.RAW_CONTACTS + "." + RawContacts.DISPLAY_NAME_PRIMARY  + " AS " + RawContacts.DISPLAY_NAME_PRIMARY + "," + 
		Tables.RAW_CONTACTS + "." + RawContacts.INDICATE_PHONE_SIM + " AS " + RawContacts.INDICATE_PHONE_SIM + "," +
		Tables.RAW_CONTACTS + "." + RawContacts.INDEX_IN_SIM + " AS " + RawContacts.INDEX_IN_SIM + "," + 
		Tables.RAW_CONTACTS + "." + RawContacts.ACCOUNT_NAME + " AS " + RawContacts.ACCOUNT_NAME + "," +
		Tables.RAW_CONTACTS + "." + RawContacts.ACCOUNT_TYPE + " AS " + RawContacts.ACCOUNT_TYPE + "," +
		Views.CONTACTS + "." + Contacts.IN_VISIBLE_GROUP + " AS " + Contacts.IN_VISIBLE_GROUP +
		
		" FROM " +
		Tables.CONTACTS + " LEFT JOIN " + Tables.RAW_CONTACTS + " LEFT JOIN " + Views.CONTACTS +
		" WHERE " +
		//Tables.RAW_CONTACTS + "." + RawContacts._ID + "=" + Tables.CONTACTS + "." + "name_raw_contact_id" + " AND " +
		//Tables.CONTACTS + "." + Contacts.HAS_PHONE_NUMBER + " = 1 ) AS " + GDS_DETAIL_TABLE + 
		Tables.RAW_CONTACTS + "." + RawContacts.CONTACT_ID + "=" + Tables.CONTACTS + "." + Contacts._ID + 
		" AND " + Tables.CONTACTS + "." + Contacts._ID + "=" + Views.CONTACTS + "." + "_id" + 
		" ) AS " + GDS_DETAIL_TABLE + 
		" LEFT JOIN " +
		
		" (SELECT " + RAW_CONTACT_ID + "," + Data.DATA1 + " AS " + GnDialerSearchViewColumns.PHONE_NUMBER +
		 "," + DataColumns.MIMETYPE_ID + " AS " + DataColumns.MIMETYPE_ID +
		 "," + "auto_record" + ",is_privacy" +
		" FROM " +
		Tables.DATA +
		" WHERE " +
		DataColumns.MIMETYPE_ID + " in ('5', '7') ) AS " + GDS_NUMBER_TABLE +
		
		" WHERE " +
		GDS_DETAIL_TABLE + "." + RAW_CONTACT_ID + "=" + GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.RAW_CONTACT_ID + " AND " +
		GDS_NUMBER_TABLE + "." + RAW_CONTACT_ID + "=" + GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.RAW_CONTACT_ID /*+
		
		" ORDER BY " + GN_DIALER_SEARCH_TABLE + "." + GnDialerSearchColumns.SORT_KEY*/;
		
		db.execSQL("CREATE VIEW " + GN_DIALER_SEARCH_VIEW + " AS " + VIEW_SELECT);
	}
	
	private SQLiteStatement mGnDialerSearchNameUpdate;
	public void updateNameForGnDialerSearch(SQLiteDatabase db, long rawContactId,
            String displayNamePrimary) {
		
		if (null == displayNamePrimary) {
    		return;
    	}
		
		if (mGnDialerSearchNameUpdate == null) {
			mGnDialerSearchNameUpdate = db.compileStatement("UPDATE "
					+ GN_DIALER_SEARCH_TABLE + " SET "
					+ GnDialerSearchColumns.QUAN_PINYIN + "=?,"
					+ GnDialerSearchColumns.JIAN_PINYIN + "=?,"
					+ GnDialerSearchColumns.QUAN_T9 + "=?,"
					+ GnDialerSearchColumns.JIAN_T9 + "=?,"
					+ GnDialerSearchColumns.MATCH_MAP + "=?,"
					+ GnDialerSearchColumns.MATCH_MAP_JIAN + "=?,"					
					+ GnDialerSearchColumns.POLYPHONIC + "=?,"
					+ GnDialerSearchColumns.SORT_KEY + "=?,"
					+ GnDialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + "=?,"
					+ GnDialerSearchColumns.MATCH_MAP_HIGHLIGHT + "=?"
					+ " WHERE " + GnDialerSearchColumns.RAW_CONTACT_ID + "=? "
					);
		}
		
		bindToSqliteStatement(mGnDialerSearchNameUpdate, rawContactId, displayNamePrimary);
		mGnDialerSearchNameUpdate.execute();
    }
	
	private SQLiteStatement mGnDialerSearchNewRecordInsert;
	public void insertNameForGnDialerSearch(SQLiteDatabase db, long rawContactId,
			String displayNamePrimary) {
		
		if (null == displayNamePrimary) {
    		return;
    	}
		
    	if (mGnDialerSearchNewRecordInsert == null) {
    	    if (!db.isOpen()) {
                try {
                    db = ContactsProvider2.mGnContactsHelper.getWritableDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                Log.d(TAG, "1:try open  db.isOpen() = " + db.isOpen());
            }
    	    
        	mGnDialerSearchNewRecordInsert = db.compileStatement(
        			"INSERT INTO " + GN_DIALER_SEARCH_TABLE + "(" +
        			GnDialerSearchColumns.QUAN_PINYIN + "," +
        			GnDialerSearchColumns.JIAN_PINYIN + "," +
					GnDialerSearchColumns.QUAN_T9 + "," +
					GnDialerSearchColumns.JIAN_T9 + "," +
					GnDialerSearchColumns.MATCH_MAP + "," +
					GnDialerSearchColumns.MATCH_MAP_JIAN + "," +
					GnDialerSearchColumns.POLYPHONIC + "," +
					GnDialerSearchColumns.SORT_KEY + "," +
					GnDialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + "," +
					GnDialerSearchColumns.MATCH_MAP_HIGHLIGHT + "," +
					GnDialerSearchColumns.RAW_CONTACT_ID +
					")" +
        			" VALUES (?,?,?,?,?,?,?,?,?,?,?)");
    	}
    	
		//Do not insert name now, update it later for both name and alternative name.
    	bindToSqliteStatement(mGnDialerSearchNewRecordInsert, rawContactId, displayNamePrimary);
    	
    	Log.d(TAG, "db.isOpen() = " + db.isOpen());
    	try {
    	    mGnDialerSearchNewRecordInsert.executeInsert();
    	} catch (Exception e) {
    	    e.printStackTrace();
    	    if (!db.isOpen()) {
                try {
                    db = ContactsProvider2.mGnContactsHelper.getWritableDatabase();
                    Log.d(TAG, "2:try open  db.isOpen() = " + db.isOpen());
                    if (db.isOpen()) {
                        mGnDialerSearchNewRecordInsert.executeInsert();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
    	}
    }
	
	public final char POLYPHONIC_SEPARATOR = ChineseToPinyinHelper.POLYPHONIC_SEPARATOR;
	public final String POLYPHONIC_SEPARATOR_STR = ChineseToPinyinHelper.POLYPHONIC_SEPARATOR_STR;
	
	private void bindToSqliteStatement(SQLiteStatement sqliteStatement,
			long rawContactId, String displayNamePrimary) {
		String[][] pinyinArrays = ChineseToPinyinHelper.translateMulti(displayNamePrimary, true);
		// aurora <wangth> <2014-2-20> add for highlight begin
		String[][] pinyinArraysHighlight = ChineseToPinyinHelper.translateMulti(displayNamePrimary, false);
		// aurora <wangth> <2014-2-20> add for highlight end
		
		if (pinyinArrays.length == 1) {
			String[] pinyinArray = pinyinArrays[0];
			String quanPinyin = getQuanPinyin(pinyinArray);
			sqliteStatement.bindString(1, quanPinyin);
			sqliteStatement.bindString(2, getJianPinyin(pinyinArray));
            sqliteStatement.bindString(3, getQuanT9(quanPinyin));
            sqliteStatement.bindString(4, getJianT9(pinyinArray));
            // aurora <wangth> <2014-2-20> modify for highlight begin
            /*
            sqliteStatement.bindString(5, getMatchMapQuan(pinyinArray));
            sqliteStatement.bindString(6, getMatchMapJian(pinyinArray));
            sqliteStatement.bindLong(7, 0);
            sqliteStatement.bindString(8, quanPinyin.toLowerCase());
            */
            sqliteStatement.bindString(5, getMatchMapQuan(pinyinArray));
            sqliteStatement.bindString(6, getMatchMapJianHighlight(pinyinArraysHighlight[0]));
            sqliteStatement.bindLong(7, 0);
            sqliteStatement.bindString(8, quanPinyin.toLowerCase());
            sqliteStatement.bindString(9, getQuanPinyin(pinyinArraysHighlight[0]));
            sqliteStatement.bindString(10, getMatchMapQuanHighlight(pinyinArraysHighlight[0]));
            // aurora <wangth> <2014-2-20> modify for highlight end
            
		} else {
			StringBuilder quanPinyin = new StringBuilder();
			StringBuilder jianPinyin = new StringBuilder();
			StringBuilder quanT9 = new StringBuilder();
			StringBuilder jianT9 = new StringBuilder();
			StringBuilder matchMapQuan = new StringBuilder();
			StringBuilder matchMapJian = new StringBuilder();
			StringBuilder sortKey = new StringBuilder();
			
			StringBuilder quanPinyinHighlight = new StringBuilder();
			StringBuilder matchMapQuanHighlight = new StringBuilder();
			
			for (String[] pinyinArray : pinyinArrays) {
				String pinyin = getQuanPinyin(pinyinArray);

				quanPinyin.append(pinyin).append(POLYPHONIC_SEPARATOR);				
				jianPinyin.append(getJianPinyin(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				quanT9.append(getQuanT9(pinyin)).append(POLYPHONIC_SEPARATOR);
				jianT9.append(getJianT9(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				matchMapQuan.append(getMatchMapQuan(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				// aurora <wangth> <2014-2-20> remove for highlight begin
				/*
				matchMapJian.append(getMatchMapJian(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				*/
				// aurora <wangth> <2014-2-20> remove for highlight end
				sortKey.append(pinyin.toLowerCase()).append(POLYPHONIC_SEPARATOR);
			}
			
			// aurora <wangth> <2014-2-20> add for highlight begin
			for (String[] pinyinArrayHighlight : pinyinArraysHighlight) {
			    String pinyin = getQuanPinyin(pinyinArrayHighlight);
			    quanPinyinHighlight.append(pinyin).append(POLYPHONIC_SEPARATOR);
			    matchMapQuanHighlight.append(getMatchMapQuanHighlight(pinyinArrayHighlight)).append(POLYPHONIC_SEPARATOR);
			    matchMapJian.append(getMatchMapJianHighlight(pinyinArrayHighlight)).append(POLYPHONIC_SEPARATOR);
			}
			// aurora <wangth> <2014-2-20> add for highlight end
			
			quanPinyin.setLength(quanPinyin.length()-1);
			//Gionee:huangzy 20130114 modify for CR00762341 start
			/*jianPinyin.setLength(quanPinyin.length()-1);*/
			jianPinyin.setLength(jianPinyin.length()-1);
			//Gionee:huangzy 20130114 modify for CR00762341 end
			quanT9.setLength(quanT9.length()-1);
			jianT9.setLength(jianT9.length()-1);
			matchMapQuan.setLength(matchMapQuan.length()-1);
			matchMapJian.setLength(matchMapJian.length()-1);
			sortKey.setLength(sortKey.length()-1);
			
			quanPinyinHighlight.setLength(quanPinyinHighlight.length()-1);
			matchMapQuanHighlight.setLength(matchMapQuanHighlight.length()-1);
			
			sqliteStatement.bindString(1, quanPinyin.toString());
			sqliteStatement.bindString(2, jianPinyin.toString());
            sqliteStatement.bindString(3, quanT9.toString());
            sqliteStatement.bindString(4, jianT9.toString());
            sqliteStatement.bindString(5, matchMapQuan.toString());
            sqliteStatement.bindString(6, matchMapJian.toString());
            sqliteStatement.bindLong(7, 1);
            sqliteStatement.bindString(8, sortKey.toString());
            sqliteStatement.bindString(9, quanPinyinHighlight.toString());
            sqliteStatement.bindString(10, matchMapQuanHighlight.toString());
		}
		
        sqliteStatement.bindLong(11, rawContactId);
	}
	
	public void updateOrInsertNameDialerSearch(SQLiteDatabase db, long rawContactId,
			String displayNamePrimary) {
		
		boolean recordExisted = false; 
		Cursor c = db.query(Tables.GN_DIALER_SEARCH, new String[]{BaseColumns._ID},
				GnDialerSearchColumns.RAW_CONTACT_ID + "=" + rawContactId,
				null, null, null, BaseColumns._ID + " LIMIT 1");
		if (null != c) {
			recordExisted = (c.getCount() > 0);
			c.close();				
		}
		
		if (recordExisted) {
			updateNameForGnDialerSearch(db, rawContactId, displayNamePrimary);
		} else {
			insertNameForGnDialerSearch(db, rawContactId, displayNamePrimary);
		}
	}
	
	private SQLiteStatement mDialerSearchDelete;
	public void deleteNameForDialerSearch(SQLiteDatabase db, long rawContactId) {
		
    	if (mDialerSearchDelete == null) {
    		mDialerSearchDelete = db.compileStatement(
        			"DELETE FROM " + GN_DIALER_SEARCH_TABLE + 
        			" WHERE " + GnDialerSearchColumns.RAW_CONTACT_ID + "=?");
    	}
    	mDialerSearchDelete.bindLong(1, rawContactId);
    	mDialerSearchDelete.execute();
    }
	
	//*********************
	private static final char[][] T9_ARRAY = {
    	{'0', '+'},
    	{'1'},
    	{'2', 'a', 'b', 'c', 'A', 'B', 'C'},
    	{'3', 'd', 'e', 'f', 'D', 'E', 'F'},
    	{'4', 'g', 'h', 'i', 'G', 'H', 'I'},
    	{'5', 'j', 'k', 'l', 'J', 'K', 'L'},
    	{'6', 'm', 'n', 'o', 'M', 'N', 'O'},
    	{'7', 'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S'},
    	{'8', 't', 'u', 'v', 'T', 'U', 'V'},
    	{'9', 'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z'},
    };
	
	private static final int[][] RUSSIAN_ARRAY = {
	    {1040,1072}, {1041,1073}, {1042,1074}, {1043,1075},
	    {1044,1076}, {1045,1077}, {1046,1078}, {1047,1079},
	    {1048,1080}, {1049,1081}, {1050,1082}, {1051,1083},
	    {1052,1084}, {1053,1085}, {1054,1086}, {1055,1087},
	    {1056,1088}, {1057,1089}, {1058,1090}, {1059,1091},
	    {1060,1092}, {1061,1093}, {1062,1094}, {1063,1095},
	    {1064,1096}, {1065,1097}, {1066,1098}, {1067,1099},
	    {1068,1100}, {1069,1101}, {1070,1102}, {1071,1103},
	};
	
    private static final HashMap<Character, Character> DIALER_KEY_MAP = new HashMap<Character, Character>();
    static {
    	for (int v = 0; v <= 9; ++v) {
    		char value = (char)(v + '0');
    		for (int j = 0, len = T9_ARRAY[v].length; j < len; ++j) {    			
    			DIALER_KEY_MAP.put(T9_ARRAY[v][j], value);
    		}
    	}
    	
        DIALER_KEY_MAP.put('*', '*');
        DIALER_KEY_MAP.put('+', '+');
        
		if (ContactsProvidersApplication.sIsGnFlySupport) {
			for (int v = 2; v <= 9; ++v) {
				char value = (char)(v + '0');
	    		for (int j = 4*v - 8, len = 4*v - 5; j < len; ++j) {
	    			DIALER_KEY_MAP.put((char)RUSSIAN_ARRAY[j][0], value);
	    			DIALER_KEY_MAP.put((char)RUSSIAN_ARRAY[j][1], value);
	    		}
	    	}
		}
		
		ChineseToPinyinHelper.setLegalCharactSet(DIALER_KEY_MAP.keySet());
    }
	    
	public String getQuanPinyin(String[] pinyinArray) {
		if (null != pinyinArray) {
			StringBuilder sb = new StringBuilder();
	    	for (String str : pinyinArray) {
	    		sb.append(str);
	    	}
	    	return sb.toString();
		}
		return "";
	}
	
	private String getJianPinyin(String[] pinyinArray) {
		StringBuilder sb = new StringBuilder();
    	for (String p : pinyinArray) {
    		sb.append(p.charAt(0));
    	}
    	return sb.toString().toLowerCase();
	}
	
	private String getJianT9(String[] pinyinArray) {
    	StringBuilder sb = new StringBuilder();
    	for (String p : pinyinArray) {
    		Character cValue = DIALER_KEY_MAP.get(p.charAt(0));
    		if (null != cValue) {
    			sb.append(cValue);	
    		}
    	}
    	return sb.toString();
    }
	
    
	private String getQuanT9(String pinyin) {
    	StringBuilder sb = new StringBuilder();
    	char[] charArray = pinyin.toCharArray(); 
    	for (char c : charArray) {
    		Character cValue = DIALER_KEY_MAP.get(c);
    		if (null != cValue) {
    			sb.append(cValue);	
    		}
    	}
    	return sb.toString();
    }
    
	private String getMatchMapQuan(String[] pinyinArray) {
	    
    	StringBuilder sb = new StringBuilder();
    	int index = 0;
    	for (String p : pinyinArray) {
    		sb.append((char)index);
    		index = index + p.length();
    		sb.append((char)index);
    	}
    	return sb.toString();
    }
    
	private String getMatchMapJian(String[] pinyinArray) {
    	StringBuilder sb = new StringBuilder();
    	int index = 0;
    	for (String p : pinyinArray) {
    		sb.append((char)index);
    		sb.append((char)(index+1));
    		index = index + p.length();
    	}
    	return sb.toString();
    }
	
	// aurora <wangth> <2014-2-20> add for highlight begin
    private String getMatchMapQuanHighlight(String[] pinyinArray) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String p : pinyinArray) {
            boolean isFuHao = ContactsDatabaseHelper.firstIsFuHao(p);
            if (isFuHao) {
                index = index + p.length();
                continue;
            }
            
            sb.append((char)index);
            index = index + p.length();
            sb.append((char)index);
        }
        return sb.toString();
    }
    
    private String getMatchMapJianHighlight(String[] pinyinArray) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String p : pinyinArray) {
            boolean isFuHao = ContactsDatabaseHelper.firstIsFuHao(p);
            if (isFuHao) {
                index = index + p.length();
                continue;
            }
            
            sb.append((char)index);
            sb.append((char)(index+1));
            index = index + p.length();
        }
        return sb.toString();
    }
	// aurora <wangth> <2014-2-20> add for highlight end

	private static final String QUERY_ABC_PREFIX = "auroracontactqueryfordialerprefix";
	protected Cursor queryAbc(SQLiteDatabase db, String searchKey, String selec) {
		int KEY_LEN = searchKey.length();
		boolean dialSearchOnly = false;
		boolean single = false;
		if (searchKey.startsWith(QUERY_ABC_PREFIX)) {
		    single = true;
		    searchKey = searchKey.replaceAll(QUERY_ABC_PREFIX, "");
		    KEY_LEN = searchKey.length();
		} else {
		    dialSearchOnly = true;
		}
	    
        Cursor queryCursor = null;
        {
        	String selection = "(" + GnDialerSearchViewColumns.JIAN_PINYIN + " LIKE '%" + searchKey.charAt(0) + "%')";
        	
        	boolean skip = false;
        	if (KEY_LEN > 1) {
        		SearchResultCache searchRetCache = getCache(searchKey);
        		skip = (null != searchRetCache && searchRetCache.isEmpty()); 
        		if (!skip) {
        	    	if (null != searchRetCache) {
        	    		selection = selection + " AND (" + 
        	    		GnDialerSearchViewColumns.RAW_CONTACT_ID + 
            			" IN (" + searchRetCache.getRawContactIds() + "))";	
        	    	}
                }
        	}
        	
        	if (dialSearchOnly) {
        	    selection = selection +  " AND (" + GnDialerSearchViewColumns.MIME_TYPE + 
                " = 5)"; 
        	}
        	
        	if (null != selec && !(selec.replaceAll(" ", "").isEmpty())) {
        	    selection = selection + " AND " + selec;
        	}
        	
        	if (!skip) {
        		queryCursor = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, GnDialerSearchViewColumns.COLUMN_NAMES, selection,
    		    		null, null, null, GnDialerSearchViewColumns.SORT_KEY);	
        	}
        }
        
        List<GnDialerSearchViewRow> matchedList = null;
        MatrixCursor resultCursor = null;
        
        if (queryCursor != null && queryCursor.moveToFirst()) {
        	List<GnDialerSearchViewRow> fullMatchedList = new LinkedList<GnDialerSearchViewRow>();
        	List<GnDialerSearchViewRow> firstJianMatchedList = new LinkedList<GnDialerSearchViewRow>();
        	List<GnDialerSearchViewRow> firstPartMatchedList = new LinkedList<GnDialerSearchViewRow>();
        	List<GnDialerSearchViewRow> partMatchedList = new LinkedList<GnDialerSearchViewRow>();        	
        	
            do {
                try {
            	GnDialerSearchViewRow sdr = new GnDialerSearchViewRow();
            	sdr.read(queryCursor, searchKey);
            	int index = -1;
            	do {
            		String JIAN = sdr.mJianPinyin;
            		String QUAN = sdr.mSortKey;
            		
	            	boolean isJianMatch = false;
	            	if (sdr.mJianT9Len >= KEY_LEN) {
	    				index = JIAN.indexOf(searchKey);
	    				isJianMatch = (-1 != index);
	    			}
	    			
	    			if (isJianMatch) {
	    				char[] pinyinHighlight = Arrays.copyOfRange(sdr.mMatchMapJian.toCharArray(),
								index * 2, (index + searchKey.length())*2);
	    				sdr.setPinyinHighlight(pinyinHighlight);    				
					} else {
	    				index = QUAN.indexOf(searchKey);
	    			}
	    			
	    			char[] matchMapQuan = sdr.mMatchMapQuan.toCharArray();
	    			char[] matchMapQuanHighlight = sdr.mMatchMapQuanHighlight.toCharArray(); // add for highlight
	    			
	    			if (0 == index) {
	    				if (!isJianMatch) {
//	    					sdr.setPinyinHighlight(new char[]{0, (char)(KEY_LEN)}); // modify for wangth highlight 20140227
	    				    sdr.setPinyinHighlight(new char[]{matchMapQuanHighlight[0], (char)(matchMapQuanHighlight[0] + KEY_LEN)});
	    				}
	    				
	    				if (KEY_LEN == sdr.mQuanT9Len) {
	    					addToListSort(fullMatchedList, sdr);    					
	    				} else {
	    					if (isJianMatch) {
	    						addToListSort(firstJianMatchedList, sdr);    						
	    					} else {
	    						addToListSort(firstPartMatchedList, sdr);
	    					}
	    				}
	    			} else if (-1 != index) {
	    				boolean isMatch = isJianMatch;
	    				if (!isMatch) {
	    					for (int i = 0, len = matchMapQuan.length; i < len; i+=2) {
	    						if (matchMapQuan[i] == index) {
//	    							char[] matchOffset = new char[]{matchMapQuan[i], (char)(matchMapQuan[i] + KEY_LEN)}; // modify for wangth highlight 20140227
	    						    char[] matchOffset = new char[]{matchMapQuanHighlight[i], (char)(matchMapQuanHighlight[i] + KEY_LEN)};
	    							sdr.setPinyinHighlight(matchOffset);    							
	    	    					isMatch = true;
	    							break;
	    						}
	    					}
	    				}
	    				if (!isMatch) {
							index = -1;
						} else {
							addToListSort(partMatchedList, sdr);
						}
	    			}
	    			
	    			if (-1 == index) {
	    				String[] sqlits = new String[matchMapQuan.length/2];
	    				for (int i = 0; i < sqlits.length; ++i) {
	    					sqlits[i] = QUAN.substring(matchMapQuan[i*2], matchMapQuan[i*2 + 1]);
	    				}
	    				
	    				for (int i = 0, srcLen = sdr.mQuanT9Len; i < sqlits.length; ++i) {
	    				    if (sqlits[i].isEmpty()) {
	    				        continue;
	    				    }
	    				    
	    					String tmpSearchKey = new String(searchKey);
	    					if (tmpSearchKey.charAt(0) != sqlits[i].charAt(0)) {
	    						if (i > 0) {
	    							srcLen -= sqlits[i - 1].length();
	        						if (srcLen < KEY_LEN) {
	        							break;
	        						}	
	    						}
	    						continue;
	    					}
	
	    					char keyFirshChar;
	        				String curSqlit;
	        				int matchIndex = 0;
	        				char[] nameHighlight = new char[(matchMapQuan.length - i)*2];
	    					for (int j = i; j < sqlits.length; ++j) {
	    						curSqlit = sqlits[j];
	    						keyFirshChar = tmpSearchKey.charAt(0);
	    						if (curSqlit.charAt(0) == keyFirshChar) {
//	    							nameHighlight[matchIndex++] = matchMapQuan[j*2];
	    						    nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2]; // modify for wangth highlight 20140227
	    							
	        						if (tmpSearchKey.startsWith(curSqlit)) {
	        							tmpSearchKey = tmpSearchKey.substring(curSqlit.length());
//	        							nameHighlight[matchIndex++] = matchMapQuan[j*2+1];
	        							nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2+1];
	        						} else if (curSqlit.startsWith(tmpSearchKey)) {
//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + tmpSearchKey.length());
	        						    nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + tmpSearchKey.length()); // modify for wangth highlight 20140227
	        							index = nameHighlight[0];
	        							break;
	        						} else {
//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + 1); // modify for wangth highlight 20140227
	        						    nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + 1);
	        							tmpSearchKey = tmpSearchKey.substring(1);
	        						}
	        						
	        						if (tmpSearchKey.length() == 0) {
	        							index = nameHighlight[0];
	        							break;
	        						}
	        					} else {
	        						index = -1;
	        						break;
	        					}
	    					}
	    					
	    					if (-1 != index) {
	    						sdr.setPinyinHighlight(nameHighlight);
	    						if (nameHighlight[0] == 0) {
	    							addToListSort(firstPartMatchedList, sdr);
	    						} else {
	    							addToListSort(partMatchedList, sdr);
	    						}
								break;
	    					}
	    				}
	    			}
            	} while (-1 == index && sdr.next());
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            } while(queryCursor.moveToNext());
            
            matchedList = new LinkedList<GnDialerSearchViewRow>();
            if (fullMatchedList.size() > 0) {
            	matchedList.addAll(fullMatchedList);	
            }
            
            matchedList.addAll(firstJianMatchedList);
            matchedList.addAll(firstPartMatchedList);
            matchedList.addAll(partMatchedList);
        }
        if (null != queryCursor) {
    		queryCursor.close();
    		queryCursor = null;
    	}               
        
        if (KEY_LEN > 1) {
    		SearchResultCache searchRetCache = new SearchResultCache(searchKey);
    		if (null != matchedList) {
    			for (GnDialerSearchViewRow sdr : matchedList) {
        			searchRetCache.addId(sdr.mRawContactId);
        		}	
    		}
    		writeCache(searchRetCache);
    	}
        
        if (null != matchedList && matchedList.size() > 0) {
        	resultCursor = new MatrixCursor(GnDialerSearchResultColumns.COLUMN_NAMES);
//			for (GnDialerSearchViewRow sdr : matchedList) {
//				resultCursor.addRow(sdr.getGnDialerSearchResultRow());
//    		}
			
			for (int i = 0; i < matchedList.size(); i++) {
			    if (single) {
                    Object contactIdObj = matchedList.get(i).getGnDialerSearchResultRow()[1];
                    boolean merge = false;
                    for (int j = 0; j < i; j++) {
                        Object mergeContactIdObj = matchedList.get(j).getGnDialerSearchResultRow()[1];
                        if (null != contactIdObj && null != mergeContactIdObj 
                                && contactIdObj.equals(mergeContactIdObj)) {
                            merge = true;
                            break;
                        }
                    }
                    
                    if (merge) {
                        continue;
                    }
                }
			    
			    resultCursor.addRow(matchedList.get(i).getGnDialerSearchResultRow());
			}
			
			matchedList = null;
		}
        
        return resultCursor;
    }
	
	//Gionee:huangzy 20121213 add for CR00741589 start
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
	//Gionee:huangzy 20121213 add for CR00741589 end
	
	protected Cursor queryContactId(SQLiteDatabase db, String searchKey, String selec) {
	    if (searchKey == null) {
	        return null;
	    }
	    
	    String selection = "(" + GnDialerSearchViewColumns.QUAN_PINYIN + " LIKE '%" + searchKey + "%') AND "
	            + GnDialerSearchViewColumns.MIME_TYPE + "=5";
	    String project[] = new String[] {
	            GnDialerSearchViewColumns.CONTACT_ID,
	            GnDialerSearchViewColumns.NAME,
	            GnDialerSearchViewColumns.QUAN_PINYIN,
	            GnDialerSearchViewColumns.PHONE_NUMBER,
        };
	    
	    Cursor cursor = db.query(GN_DIALER_SEARCH_VIEW, project, selection,
                null, GnDialerSearchViewColumns.CONTACT_ID, null, GnDialerSearchViewColumns.SORT_KEY);
	    
	    MatrixCursor matCursor = new MatrixCursor(project);
	    ArrayList<Object[]> sortObjList = new ArrayList<Object[]>();
	    String quanpin = null;
	    int index = 0;
	    int preEqualCount = 0;
	    int prePartEqualCount = 0;
	    char indexP;
	    
	    try {
	        if (cursor != null && cursor.moveToFirst()) {
	            do {
	                boolean add = true;
	                quanpin = cursor.getString(2);
	                index = quanpin.indexOf(searchKey);
	                
	                String subStr = quanpin.substring(index + searchKey.length());
	                if (subStr != null && subStr.length() > 0) {
	                    indexP = subStr.charAt(0);
	                    if (indexP > 'a' && indexP < 'z') {
	                        add = false;
	                    }
	                }
	                
	                if (add && index >= 0) {
	                    //Log.e("wangth", "index = " + index + "  quanp = " + quanpin + "  searchKey = " + searchKey);
	                    String subStrPre = quanpin.substring(0, index);
	                    
	                    Object[] obj = new Object[project.length];
	                    for (int jj = 0; jj < project.length; jj++) {
	                        obj[jj] = cursor.getString(jj);
	                    }
	                    
	                    if (searchKey.equals(quanpin)) {
	                        sortObjList.add(preEqualCount, obj);
	                        preEqualCount++;
	                    } else if (index == 0) {
	                        sortObjList.add(preEqualCount + prePartEqualCount, obj);
	                        prePartEqualCount++;
	                    } else {
	                        sortObjList.add(obj);
	                    }
	                }
	            } while (cursor.moveToNext());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (cursor != null) {
	            cursor.close();
	        }
	    }
	    
	    for (Object obj[] : sortObjList) {
            matCursor.addRow(obj);
        }
	    
	    return matCursor;
	}
}
