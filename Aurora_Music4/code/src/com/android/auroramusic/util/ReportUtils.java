package com.android.auroramusic.util;

import com.android.auroramusic.report.TotalCount;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

/**
 * 
 * @author chenhl
 *
 *	start				应用启动一次，统计一次
	create_playlist		新建一个歌单，统计一次
	online_music		点击在线音乐一次，统计一次
	online_search		输入在线音乐搜索栏一次，统计一次
	download			点击下载按钮一次，统计一次
	btn_playlist		进入播放器界面，点击歌单一次，统计一次
	btn_album			进入播放器界面，点击专辑一次，统计一次
	btn_artist			进入播放器界面，点击歌手一次，统计一次
	btn_donwload		进入播放器界面，点击下载一次，统计一次
	btn_sound			进入播放器界面，点击音效一次，统计一次
	btn_share_friend	进入播放器界面，分享到朋友圈一次，统计一次
	btn_share_twitter	进入播放器界面，分享到微博一次，统计一次
	share_playlist		在线歌单，点击分享歌单一次，统计一次
	collect_playlist	在线歌单，点击收藏歌单一次，统计一次
 */

public class ReportUtils {

	private static final String TAG = "ReportUtils";
	private static ReportUtils mInstance;
	private ContentResolver mContentResolver;

	private static final String CONTENT_URI = "content://com.iuni.reporter/module/";
	private static final String REPOTR_MODULE = "140";
	private static Context mContext;
	//统计项
	private static final String REPORT_START = "001";
	private static final String REPORT_CREATE_PL = "002";
	private static final String REPORT_OL_MUSIC = "003";
	private static final String REPORT_OL_SEARCH = "004";
	private static final String REPORT_DOWNLOAD = "005";
	private static final String REPORT_BTN_PL = "006";
	private static final String REPORT_BTN_ALBUM = "007";
	private static final String REPORT_BTN_ARTISIT = "008";
	private static final String REPORT_BTN_DOWNLOAD = "009";
	private static final String REPORT_BTN_SOUND = "010";
	private static final String REPORT_BTN_SHARE_FR = "011";
	private static final String REPORT_BTN_SHARE_TW = "012";
	private static final String REPORT_SHARE_PL = "013";
	private static final String REPORT_COLLECT_PL = "014";

	public static final int TAG_START = 100;
	public static final int TAG_CREATE_PL = 101;
	public static final int TAG_OL_MUSIC = 102;
	public static final int TAG_OL_SEARCH = 103;
	public static final int TAG_DOWNLOAD = 104;
	public static final int TAG_BTN_PL = 105;
	public static final int TAG_BTN_ALBUM = 106;
	public static final int TAG_BTN_ARTISIT = 107;
	public static final int TAG_BTN_DOWNLOAD = 108;
	public static final int TAG_BTN_SOUND = 109;
	public static final int TAG_BTN_SHARE_FR = 110;
	public static final int TAG_BTN_SHARE_TW = 111;
	public static final int TAG_SHARE_PL = 112;
	public static final int TAG_COLLECT_PL = 113;

	public ReportUtils(Context context) {
		mContentResolver = context.getContentResolver();
	}

	public static ReportUtils getInstance(Context context) {

		if (mInstance == null) {
			mInstance = new ReportUtils(context);
		}
		mContext = context;
		return mInstance;
	}

	public void reportMessage(int tag) {
		reportMessage(tag, 1);
	}
	
	public void reportMessage(int tag, int size) {
		/*final int myTag = tag;
		final int value = size;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				reportRequest(myTag,value);
			}
		}).start();*/
		reportRequest(tag,size);
	}
	
	private void reportRequest(int tag, int size) {
		String itemTag = null;
		//ContentValues values = new ContentValues();
		//values.put("module_key", REPOTR_MODULE);
		switch (tag) {
		case TAG_START:
			itemTag = REPORT_START;
			break;
		case TAG_CREATE_PL:
			itemTag = REPORT_CREATE_PL;
			break;
		case TAG_OL_MUSIC:
			itemTag = REPORT_OL_MUSIC;
			break;
		case TAG_OL_SEARCH:
			itemTag = REPORT_OL_SEARCH;
			break;
		case TAG_DOWNLOAD:
			itemTag = REPORT_DOWNLOAD;
			break;
		case TAG_BTN_PL:
			itemTag = REPORT_BTN_PL;
			break;
		case TAG_BTN_ALBUM:
			itemTag = REPORT_BTN_ALBUM;
			break;
		case TAG_BTN_ARTISIT:
			itemTag = REPORT_BTN_ARTISIT;
			break;
		case TAG_BTN_DOWNLOAD:
			itemTag = REPORT_BTN_DOWNLOAD;
			break;
		case TAG_BTN_SOUND:
			itemTag = REPORT_BTN_SOUND;
			break;
		case TAG_BTN_SHARE_FR:
			itemTag = REPORT_BTN_SHARE_FR;
			break;
		case TAG_BTN_SHARE_TW:
			itemTag = REPORT_BTN_SHARE_TW;
			break;
		case TAG_SHARE_PL:
			itemTag = REPORT_SHARE_PL;
			break;
		case TAG_COLLECT_PL:
			itemTag = REPORT_COLLECT_PL;
			break;
		}
		/*values.put("item_tag", itemTag);
		values.put("value", size);
		try {
			int id = mContentResolver.update(Uri.parse(CONTENT_URI), values,
					null, null);
			LogUtil.d(TAG, "report success:" + id);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.d(TAG, "report fail!!!");
		}*/
		TotalCount totalCount= new TotalCount(mContext, REPOTR_MODULE, itemTag, size);
		totalCount.CountData();
	}
}
