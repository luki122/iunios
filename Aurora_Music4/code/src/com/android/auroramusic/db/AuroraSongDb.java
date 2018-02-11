package com.android.auroramusic.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.auroramusic.model.AuroraCollectPlaylist;
import com.android.auroramusic.model.AuroraLoadingListener;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;

import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

public class AuroraSongDb {

	private static final String TAG = "AuroraSongDb";
	private static final int AURORA_MSG_NEED_UPDATE = 1;
	private static final int AURORA_MSG_NONEED_UPDATE = 2;

	private AuroraDbHelper mDbHelper;
	private FavoritesDbThread mDbTask;
	private Context mContext;
	private final Handler mMainHandler;
	private ArrayList<AuroraLoadingListener> mListeners = new ArrayList<AuroraLoadingListener>();
	private AuroraLoadingListener mLoadingListener = null;
	// private static StringBuilder mWhere = new StringBuilder();;
	private static List<Long> mWhere = new ArrayList<Long>();

	public AuroraSongDb(Context context) {
		this.mContext = context;
		this.mDbHelper = new AuroraDbHelper(context);

		mMainHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case AURORA_MSG_NEED_UPDATE:
					for (int i = 0; i < mListeners.size(); i++) {
						mListeners.get(i).onNeedLoading();
					}

					break;

				case AURORA_MSG_NONEED_UPDATE:
					for (int i = 0; i < mListeners.size(); i++) {
						mListeners.get(i).onNotNeedLoading();
					}
					break;

				default:
					break;
				}
			}

		};
	}

	public void resume() {
		mDbTask = new FavoritesDbThread();
		mDbTask.start();
		return;
	}

	public void pause() {

		mMainHandler.removeCallbacksAndMessages(null);
		if (mDbTask != null) {
			mDbTask.terminate();
			mDbTask = null;
		}

		return;
	}

	public void onContentDirty() {
		if (mDbTask != null) {
			mDbTask.onNotify();
		}
		return;
	}

	public void setLoadingListener(AuroraLoadingListener mListener) {
		// mLoadingListener = mListener;
		// Log.i(TAG, "zll ---- setLoadingListener mListener:"+mListener);
		if (!mListeners.contains(mListener)) {
			mListeners.add(mListener);
		}

		return;
	}

	public void removesetLoadingListener(AuroraLoadingListener mListener) {
		// mLoadingListener = mListener;
		// Log.i(TAG, "zll ---- removesetLoadingListener mListener:"+mListener);
		if (mListeners.contains(mListener)) {
			mListeners.remove(mListener);
		}

		return;
	}

	/*
	 * 增加歌曲信息到数据库
	 */
	public long insertDb(AuroraListItem song) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();

		// values.put(DBData.SONG_DISPLAYNAME, song.getDisplayName());
		values.put(AuroraDbData.SONG_FILEPATH, song.getFilePath());
		// values.put(DBData.SONG_LYRICPATH, song.getLyricPath());
		// values.put(DBData.SONG_MIMETYPE, song.getMimeType());
		values.put(AuroraDbData.SONG_NAME, song.getTitle());
		// values.put(DBData.SONG_ALBUMID, song.getAlbum().getId());
		// values.put(DBData.SONG_NETURL, song.getNetUrl());
		// values.put(DBData.SONG_DURATIONTIME, song.getDurationTime());
		// values.put(DBData.SONG_SIZE, song.getSize());
		// values.put(DBData.SONG_ARTISTID, song.getArtist().getId());
		// values.put(DBData.SONG_PLAYERLIST, song.getPlayerList());
		// values.put(DBData.SONG_ISDOWNFINISH, song.isDownFinish());
		values.put(AuroraDbData.SONG_ISLIKE, "0");
		// values.put(DBData.SONG_ISNET, song.isNet());
		values.put(AuroraDbData.SONG_ALBUMNAME, song.getAlbumName());
		values.put(AuroraDbData.SONG_ARTISTNAME, song.getArtistName());
		values.put(AuroraDbData.SONG_AUDIO_ID, song.getSongId());
		// values.put(AuroraDbData.SONG_DATAADDED, song.getSongId());

		long rs = db.insert(AuroraDbData.SONG_TABLENAME, AuroraDbData.SONG_NAME, values);
		db.close();
		return rs;
	}

	private void compareAudioDb(List<Long> where) {
		// Log.i(TAG, "zll ---- compareAudioDb where:"+where);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		Cursor cur = null;
		ArrayList<Long> list = new ArrayList<Long>();
		try {
			cur = db.query(AuroraDbData.FAVORITES_TABLENAME, null, AuroraDbData.FAVORITES_ISNET + "=0", null, null, null, null);
			if (cur != null) {
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					Long id = cur.getLong(1);
					if (!where.contains(id)) {
						list.add(id);
					}
					cur.moveToNext();
				}
			}

			for (int i = 0; i < list.size(); i++) {
				String[] whereArgs = { "" + String.valueOf(list.get(i)) };
				// Log.i(TAG,
				// "zll ---- compareAudioDb i:"+i+",whereArgs:"+whereArgs);
				db.delete(AuroraDbData.FAVORITES_TABLENAME, AuroraDbData.FAVORITES_AUDIO_ID + " = ?", whereArgs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
				cur = null;
			}

			db.close();
		}

		return;
	}

	public String getLocalLrc(String title, String artist) {
		if (title == null || (title != null && title.isEmpty()) || artist == null || (artist != null && artist.isEmpty())) {
			return null;
		}

		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			Cursor cursor = null;
			try {
				cursor = db.query(AuroraDbData.AUDIOINFO_TABLENAME, null, null, null, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						if (cursor.getString(2).equalsIgnoreCase(title) && cursor.getString(3).equalsIgnoreCase(artist)) {
							return cursor.getString(4);
						}
						cursor.moveToNext();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}

				db.close();
			}

			return null;
		}
	}

	private String toLowerCase(String s) {
		if (s == null) {
			return null;
		}

		char[] data = s.toCharArray();
		int dist = 'a' - 'A';
		int end = data.length;
		for (int i = 0; i < end; i++) {
			if (data[i] >= 'A' && data[i] <= 'Z') {
				data[i] += dist;
			}
		}

		return String.valueOf(data);
	}

	public AuroraMusicInfo getAuroraMusicInfo(long id, String title, String artist, int isnet) {
		synchronized (this) {
			if (id < 0 && isnet == 1) {
				return null;
			}

			if (title == null || (title != null && TextUtils.isEmpty(title)) || artist == null || (artist != null && TextUtils.isEmpty(artist))) {
				return null;
			}

			String t1 = toLowerCase(title);
			String t2 = toLowerCase(artist);

			StringBuilder where = new StringBuilder();
			// String[] columns = new String[] { AuroraDbData.AUDIOINFO_SONG_ID,
			// AuroraDbData.AUDIOINFO_SONG_TITLE,
			// AuroraDbData.AUDIOINFO_SONG_ARTIST};
			String where1 = AuroraDbData.AUDIOINFO_SONG_ID + " = '" + String.valueOf(id) + "'";
			String where2 = AuroraDbData.AUDIOINFO_SONG_TITLE + " = '" + t1 + "'";
			String where3 = AuroraDbData.AUDIOINFO_SONG_ARTIST + " = '" + t2 + "'";
			where.append(where1 + " AND " + where2 + " AND " + where3);

			LogUtil.d(TAG, "----- ---- getAuroraMusicInfo 1 where:" + where.toString() + ",isnet:" + isnet + " title:" + title + " artist:" + artist);

			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			Cursor cursor = null;

			try {
				cursor = db.query(AuroraDbData.AUDIOINFO_TABLENAME, null, where.toString(), null, null, null, null);
				if (cursor != null) {
					if (cursor.getCount() > 0 && cursor.moveToFirst()) {
						AuroraMusicInfo item = new AuroraMusicInfo(cursor.getString(4), cursor.getString(5));
						LogUtil.d(TAG, "----------item:" + item.toString());
						return item;
					}
				}
			} catch (Exception e) {
				Log.i(TAG, "zll ---- getAuroraMusicInfo fail ----");
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}

				db.close();
			}
		}

		return null;
	}

	public boolean isHaveLrc(String title, String artist) {
		if (title == null || title.isEmpty() || artist == null || artist.isEmpty()) {
			return false;
		}

		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			Cursor cursor = null;
			try {
				cursor = db.query(AuroraDbData.AUDIOINFO_TABLENAME, null, null, null, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						if (cursor.getString(2).equalsIgnoreCase(title) && cursor.getString(3).equalsIgnoreCase(artist)) {

							return true;
						}
						cursor.moveToNext();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}

				db.close();
			}
		}

		return false;
	}

	// 0:表示local music；1:表示net download music；同AuroraListItem
	public void addToAudioInfo(long id, String title, String artist, String picpath, String lrcpath, int isnet) {
		synchronized (this) {
			if (id < 0 || (picpath == null && lrcpath == null) || ((picpath != null && TextUtils.isEmpty(picpath)) && (lrcpath != null && TextUtils.isEmpty(lrcpath)))) {
				return;
			}

			if (title == null || (title != null && TextUtils.isEmpty(title))) {
				title = "unknow";
			}

			if (artist == null || (artist != null && TextUtils.isEmpty(artist))) {
				artist = "unknow";
			}

			String t1 = toLowerCase(title).replaceAll("'", "");
			String t2 = toLowerCase(artist).replaceAll("'", "");

			// String[] columns = new String[] { AuroraDbData.AUDIOINFO_SONG_ID,
			// AuroraDbData.AUDIOINFO_SONG_TITLE,
			// AuroraDbData.AUDIOINFO_SONG_ARTIST};
			String where1 = AuroraDbData.AUDIOINFO_SONG_ID + " = '" + String.valueOf(id) + "'";
			String where2 = AuroraDbData.AUDIOINFO_SONG_TITLE + " = '" + t1 + "'";
			String where3 = AuroraDbData.AUDIOINFO_SONG_ARTIST + " = '" + t2 + "'";
			StringBuilder where = new StringBuilder();
			where.append(where1 + " AND " + where2 + " AND " + where3);
			// Log.i(TAG,
			// "zll ---- addToAudioInfoPics 1 where:"+where.toString());
			// Log.i(TAG,
			// "zll ---- addToAudioInfoPics 1.2 picpath:"+picpath+",lrcpath:"+lrcpath);
			LogUtil.d(TAG, "---- addToAudioInfoPics  where:" + where.toString() + " title:" + t1 + " artist:" + t2 + " picpath:" + picpath + ",lrcpath:" + lrcpath);
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			Cursor cur = null;

			try {
				cur = db.query(AuroraDbData.AUDIOINFO_TABLENAME, null, where.toString(), null, null, null, null);
				if (cur != null) {
					ContentValues values = new ContentValues();
					if (cur.getCount() <= 0) {
						values.put(AuroraDbData.AUDIOINFO_SONG_ID, id);
						values.put(AuroraDbData.AUDIOINFO_SONG_TITLE, t1);
						values.put(AuroraDbData.AUDIOINFO_SONG_ARTIST, t2);
						if (lrcpath != null) {
							values.put(AuroraDbData.AUDIOINFO_SONG_LRC, lrcpath);
						}
						if (picpath != null) {
							values.put(AuroraDbData.AUDIOINFO_SONG_ALBUMPIC, picpath);
						}
						values.put(AuroraDbData.AUDIOINFO_SONG_ISNET, isnet);
						long index = db.insert(AuroraDbData.AUDIOINFO_TABLENAME, null, values);
						LogUtil.e(TAG, "zll ---- addToAudioInfoPics 2 index:" + index);
					} else {
						if (lrcpath != null) {
							values.put(AuroraDbData.AUDIOINFO_SONG_LRC, lrcpath);
						}
						if (picpath != null) {
							values.put(AuroraDbData.AUDIOINFO_SONG_ALBUMPIC, picpath);
						}
						values.put(AuroraDbData.AUDIOINFO_SONG_ISNET, isnet);
						int index = db.update(AuroraDbData.AUDIOINFO_TABLENAME, values, where.toString(), null);
						LogUtil.e(TAG, "zll ---- addToAudioInfoPics 3 index:" + index);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cur != null) {
					cur.close();
					cur = null;
				}

				db.close();
			}
		}

		return;
	}

	public boolean updateFavoritesEx() {

		if (mContext == null) {
			return false;
		}

		Cursor mAudio = null;
		List<Long> where = new ArrayList<Long>();
		try {
			mAudio = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, null, null, null);
			if (mAudio != null && mAudio.moveToFirst()) {
				do {
					where.add(mAudio.getLong(0));
				} while (mAudio.moveToNext());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mAudio != null) {
				mAudio.close();
				mAudio = null;
			}
		}

		if (where.size() > 0) {
			if (mWhere.size() > 0 && where.equals(mWhere)) {
				return false;
			}

			compareAudioDb(where);
			mWhere = where;
			return true;
		}
		// add by chenhl start
		else {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			try {
				db.delete(AuroraDbData.FAVORITES_TABLENAME, AuroraDbData.FAVORITES_ISNET + "=0", null);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			mWhere.clear();
		}
		// add by chenhl end

		return false;
	}

	public void addToFavoritesEx(AuroraListItem item) {
		if (item == null) {
			return;
		}

		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			AuroraListItem tmpItem = item;
			Cursor cur = null;
			try {
				cur = db.query(AuroraDbData.FAVORITES_TABLENAME, null, null, null, null, null, null);
				if (cur != null) {
					int base = cur.getCount();
					cur.moveToFirst();
					while (!cur.isAfterLast()) {

						if (cur.getLong(1) == tmpItem.getSongId())
							return;
						cur.moveToNext();
					}
					// add by chenhl start 20140530
					if (base > 0) {
						cur.moveToLast();
						base = cur.getInt(2);
					}
					// add by chenhl end
					ContentValues values = new ContentValues();
					values.put(AuroraDbData.FAVORITES_AUDIO_ID, tmpItem.getSongId());
					values.put(AuroraDbData.FAVORITES_PLAY_ORDER, base + 1);
					values.put(AuroraDbData.FAVORITES_TITLE, tmpItem.getTitle());
					values.put(AuroraDbData.FAVORITES_ALBUMNAME, tmpItem.getAlbumName());
					values.put(AuroraDbData.FAVORITES_ARTISTNAME, tmpItem.getArtistName());
					values.put(AuroraDbData.FAVORITES_URI, tmpItem.getFilePath());
					values.put(AuroraDbData.FAVORITES_ISNET, tmpItem.getIsDownLoadType());
					long t = db.insert(AuroraDbData.FAVORITES_TABLENAME, null, values);
					if (t < 0) {
						Log.i(TAG, "zll ----- addToFavoritesEx fail t:" + t + ",id:" + tmpItem.getSongId());
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cur != null) {
					cur.close();
					cur = null;
				}

				db.close();
			}
		}

		return;
	}

	public void removeFromFavoritesEx(long id, int isnet) {
		if (id < 0) {
			return;
		}

		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();

			StringBuilder where = new StringBuilder();
			where.append(AuroraDbData.FAVORITES_AUDIO_ID + " = " + id);
			where.append(" AND " + AuroraDbData.FAVORITES_ISNET + " = " + isnet);
			db.delete(AuroraDbData.FAVORITES_TABLENAME, where.toString(), null);
			db.close();
		}

		return;
	}

	public boolean isFavoriteEx(long id, int isnet) {

		if (id < 0) {
			return false;
		}

		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();

			Cursor cursor = null;
			try {
				cursor = db.query(AuroraDbData.FAVORITES_TABLENAME, null, null, null, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						if (cursor.getLong(1) == id && cursor.getInt(7) == isnet) {
							return true;
						}
						cursor.moveToNext();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;

					db.close();
				}
			}
		}

		return false;
	}

	private class FavoritesDbThread extends Thread {
		private volatile boolean mActive = true;
		private volatile boolean mDirty = false;

		private void updateLoading(boolean loading) {
			mMainHandler.removeMessages(AURORA_MSG_NEED_UPDATE);
			mMainHandler.removeMessages(AURORA_MSG_NONEED_UPDATE);
			mMainHandler.sendEmptyMessage(loading ? AURORA_MSG_NEED_UPDATE : AURORA_MSG_NONEED_UPDATE);
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

			while (mActive) {
				synchronized (this) {
					if (mActive && !mDirty) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							Log.w(TAG, "zll unexpected interrupt: " + this);
						}
						continue;
					}

					mDirty = false;
				}

				boolean flag = updateFavoritesEx();
				updateLoading(flag);
			}
		}

		public synchronized void onNotify() {
			// Log.i(TAG, "zll ---- onNotify xxxx 1");
			mDirty = true;
			notifyAll();
			return;
		}

		public synchronized void terminate() {
			// Log.i(TAG, "zll ---- terminate xxxx 1");
			mActive = false;
			notifyAll();
			return;
		}
	}

	// add by chenhl start 20140530
	public ArrayList<AuroraListItem> querySongIdFromFavorites(List<String> xmlpath) {
		ArrayList<AuroraListItem> list = new ArrayList<AuroraListItem>();
		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			Cursor cursor = null;
			try {
				cursor = db.query(AuroraDbData.FAVORITES_TABLENAME, new String[] { AuroraDbData.FAVORITES_AUDIO_ID, AuroraDbData.FAVORITES_TITLE, AuroraDbData.FAVORITES_ALBUMNAME,
						AuroraDbData.FAVORITES_ARTISTNAME, AuroraDbData.FAVORITES_ISNET, AuroraDbData.FAVORITES_URI }, null, null, null, null, AuroraDbData.FAVORITES_PLAY_ORDER + " desc");
				if (cursor != null && cursor.moveToFirst()) {
					do {
						long id = cursor.getLong(0);
						String title = cursor.getString(1);
						String album = cursor.getString(2);
						String artist = cursor.getString(3);
						int isnet = cursor.getInt(4);
						String uri = cursor.getString(5);
						String imgUri = AuroraMusicUtil.getImgPath(mContext, AuroraMusicUtil.MD5(title + artist + album));
						AuroraListItem item = new AuroraListItem(id, title, uri, album, -1, artist, isnet, imgUri, null, null, -1);
						if (isnet == 0) {
							String dir = uri.substring(0, uri.lastIndexOf("/"));
							if (!xmlpath.contains(dir)) {
								list.add(item);
							}
						} else {
							list.add(item);
						}

					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {// lory add 2014.6.7
				if (cursor != null) {
					cursor.close();
					cursor = null;

					db.close();
				}
			}
		}
		return list;
	}

	public void deleteFavoritesById(long[] list) {

		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			StringBuilder where = new StringBuilder();
			where.append(AuroraDbData.FAVORITES_AUDIO_ID + " IN (");
			for (int i = 0; i < list.length; i++) {
				where.append(list[i]);
				if (i < list.length - 1) {
					where.append(",");
				}
			}
			where.append(")");

			try {
				db.delete(AuroraDbData.FAVORITES_TABLENAME, where.toString(), null);
			} catch (Exception e) {
				e.printStackTrace();
			}

			db.close();// lory add 2014.6.7
		}

	}

	public void insertCollect(AuroraCollectPlaylist playlist) {

		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(AuroraDbData.COLLECT_NAME, playlist.getPlaylistname());
			values.put(AuroraDbData.COLLECT_SONG_SIZE, playlist.getSongSize());
			values.put(AuroraDbData.COLLECT_IMG, playlist.getImgUrl());
			values.put(AuroraDbData.COLLECT_SHOU_INFO, playlist.getInfo());
			values.put(AuroraDbData.COLLECT_PLAYLISTID, playlist.getPlaylistid());
			values.put(AuroraDbData.COLLECT_TYPE, playlist.getType());
			values.put(AuroraDbData.COLLECT_LIST_TYPE, playlist.getListType());

			db.insert(AuroraDbData.COLLECT_TABLENAME, null, values);
			db.close();
		}
	}

	public void deleteCollectById(String playlistid) {
		if (playlistid == null || playlistid.equals("")) {
			return;
		}
		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			try {
				String[] whereArgs = { "" + playlistid };
				db.delete(AuroraDbData.COLLECT_TABLENAME, AuroraDbData.COLLECT_PLAYLISTID + " =?", whereArgs);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();

			}
		}
	}

	public boolean isCollectById(String id) {

		if (id == null || id.equals("")) {
			return false;
		}
		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			Cursor cursor = null;
			try {
				String[] whereArgs = { "" + id };
				cursor = db.query(AuroraDbData.COLLECT_TABLENAME, null, AuroraDbData.COLLECT_PLAYLISTID + " =?", whereArgs, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
				db.close();
			}
		}
		return false;
	}

	public List<AuroraCollectPlaylist> queryCollectInfo() {
		List<AuroraCollectPlaylist> list = new ArrayList<AuroraCollectPlaylist>();
		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			Cursor cursor = null;
			String[] columns = new String[] { AuroraDbData.COLLECT_NAME, AuroraDbData.COLLECT_SONG_SIZE, AuroraDbData.COLLECT_IMG, AuroraDbData.COLLECT_SHOU_INFO, AuroraDbData.COLLECT_PLAYLISTID,
					AuroraDbData.COLLECT_TYPE, AuroraDbData.COLLECT_LIST_TYPE };

			try {
				cursor = db.query(AuroraDbData.COLLECT_TABLENAME, columns, null, null, null, null, AuroraDbData.COLLECT_ID + " desc");

				if (cursor != null && cursor.moveToFirst()) {
					do {
						String name = cursor.getString(0);
						int size = cursor.getInt(1);
						String imgurl = cursor.getString(2);
						String info = cursor.getString(3);
						String id = cursor.getString(4);
						String type = cursor.getString(5);
						int listType = cursor.getInt(6);
						AuroraCollectPlaylist playlist = new AuroraCollectPlaylist(name, id, imgurl, size, info, listType, type);
						// LogUtil.d(TAG, playlist.toString());
						list.add(playlist);
					} while (cursor.moveToNext());
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {

					cursor.close();
					cursor = null;
				}
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}

		return list;
	}

	public boolean insertSearchHistory(String keyword) {
		if (keyword == null || (keyword != null && keyword.isEmpty())) {
			return false;
		}
		if (isSearchHistory(keyword)) {
			return false;
		}
		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(AuroraDbData.SEARCH_HISTORY_KEY, keyword);
			db.insert(AuroraDbData.SEARCH_HISTORY_TABLENAME, null, values);
			db.close();
		}
		return true;
	}

	public List<String> querySearchHistory() {

		List<String> list = new ArrayList<String>();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = null;
		String[] columns = new String[] { AuroraDbData.SEARCH_HISTORY_KEY };
		try {
			cursor = db.query(AuroraDbData.SEARCH_HISTORY_TABLENAME, columns, null, null, null, null, AuroraDbData.SEARCH_HISTORY_ID);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String keyword = cursor.getString(0);
					list.add(keyword);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			db.close();
		}
		return list;
	}

	private boolean isSearchHistory(String keyword) {

		boolean result = false;
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Cursor cursor = null;
		String[] columns = new String[] { AuroraDbData.SEARCH_HISTORY_KEY };
		try {
			cursor = db.query(AuroraDbData.SEARCH_HISTORY_TABLENAME, columns, null, null, null, null, AuroraDbData.SEARCH_HISTORY_ID);
			if (cursor != null && cursor.moveToFirst()) {
				int count = cursor.getCount();
				String str = cursor.getString(0);
				if (count >= 5) {
					db.delete(AuroraDbData.SEARCH_HISTORY_TABLENAME, AuroraDbData.SEARCH_HISTORY_KEY + "=?", new String[] { str });
				}
				do {
					str = cursor.getString(0);
					if (str.equals(keyword)) {
						result = true;
						break;
					}
				} while (cursor.moveToNext());

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			db.close();
		}

		return result;
	}

	public void clearSearchHistory() {
		synchronized (this) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			try {

				db.delete(AuroraDbData.SEARCH_HISTORY_TABLENAME, null, null);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();

			}
		}
	}
	// add by chenhl end
}
