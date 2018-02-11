package com.android.auroramusic.util;

import com.android.auroramusic.ui.album.AlbumDetailActivity;
import com.android.auroramusic.ui.album.AlbumListActivity;

import android.app.Activity;
import android.content.Intent;

public class IntentFactory {

	public IntentFactory() {
		// TODO Auto-generated constructor stub
	}

	public static Intent newAlbumListIntent(Activity mActivity,
			String artistid, String artistname, int artistofalbums,
			int artistofsongs) {
		Intent intent;
		if (artistofalbums <= 3) {
			intent = new Intent(mActivity, AlbumDetailActivity.class);
			intent.putExtra(Globals.KEY_ARTIST_ID, artistid);
			intent.putExtra(Globals.KEY_ARTIST_NAME, artistname);
			intent.putExtra("artistofalbum", artistofalbums);
			intent.putExtra("artistoftrack", artistofsongs);
		} else {
			intent = new Intent(mActivity, AlbumListActivity.class);
			intent.putExtra(Globals.KEY_ARTIST_ID, artistid);
			intent.putExtra(Globals.KEY_ARTIST_NAME, artistname);
			intent.putExtra("artistofalbum", artistofalbums);
			intent.putExtra("artistoftrack", artistofsongs);
		}
		return intent;
	}
	
/*	//1.新碟上架 ； 2.某歌手专辑网上搜索结果
	public static Intent newAlbumListOnlineIntent(Activity mActivity) {
		Intent intent;
		intent = new Intent(mActivity, AlbumListOnlineActivity.class);
		intent.putExtra(Globals.KEY_ONLINE_ALBUM_TYPE, "1");
		return intent;
	}
	
	public static Intent artistAlbumListOnlineIntent(Activity mActivity, String mAlbumId, String artistname) {
		Intent intent;
		intent = new Intent(mActivity, AlbumListOnlineActivity.class);
		intent.putExtra(Globals.KEY_ONLINE_ALBUM_TYPE, "2");
		intent.putExtra(Globals.KEY_ALBUM_ID, mAlbumId);
		intent.putExtra(Globals.KEY_ARTIST_NAME, artistname);
		return intent;
	}*/

}
