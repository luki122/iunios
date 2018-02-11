/*
 * Copyright (C) 2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.android.music;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.R.integer;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.net.rtp.RtpStream;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraProgressDialog;

import com.android.auroramusic.db.AuroraMusicInfo;
import com.android.auroramusic.db.AuroraSongDb;
import com.android.auroramusic.downloadex.BitmapUtil;
import com.android.auroramusic.downloadex.DownloadManager;
import com.android.auroramusic.model.AuroraDeleteItem;
import com.android.auroramusic.online.AuroraNetTrackDetailActivity;
import com.android.auroramusic.ui.AuroraTrackBrowserActivity;
import com.android.auroramusic.ui.album.AlbumDetailActivity;
import com.android.auroramusic.util.AuroraAlbum;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.DataConvertUtil;
import com.android.auroramusic.util.DialogUtil;
import com.android.auroramusic.util.FlowTips;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.auroramusic.util.FlowTips.OndialogClickListener;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.HanziToPinyin;
import com.android.auroramusic.util.HanziToPinyin.Token;
import com.android.auroramusic.util.LogUtil;

public class MusicUtils {
	private static final String TAG = "MusicUtils";
	public static final String NEW_LIST_STRING = "create_new_list";// lory add
																	// 2014.5.13
	public static final String ENTER_FROM_NOTIFICATION = "from_notification";// lory
																				// add
																				// 2014.5.15
	public static AuroraSongDb mSongDb = null;// lory add 2014.5.1528
	private static final Uri AURORA_CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// lory
																								// add
																								// 2014.5.1528
	private static DbChangeListener mDbChangeListener;
	private static boolean mbFirstInit = false;
	public static final String AURORA_FITST_ENTNER = "firstplay";

	public interface Defs {
		public final static int OPEN_URL = 0;
		public final static int ADD_TO_PLAYLIST = 1;
		public final static int USE_AS_RINGTONE = 2;
		public final static int PLAYLIST_SELECTED = 3;
		public final static int NEW_PLAYLIST = 4;
		public final static int PLAY_SELECTION = 5;
		public final static int GOTO_START = 6;
		public final static int GOTO_PLAYBACK = 7;
		public final static int PARTY_SHUFFLE = 8;
		public final static int SHUFFLE_ALL = 9;
		public final static int DELETE_ITEM = 10;
		public final static int SCAN_DONE = 11;
		public final static int QUEUE = 12;
		public final static int EFFECTS_PANEL = 13;
		public final static int CHILD_MENU_BASE = 14; // this should be the last
														// item
	}

	// Iuni <lory><2014-05-30> add begin
	public static interface DbChangeListener {
		public void onDbChanged(boolean selfChange);
	}

	public static void registerDbObserver(Context context) {
		// Log.i(TAG, "zll ----- registerDbObserver 1 mbFirstInit:" +
		// mbFirstInit);
		if (context == null || mbFirstInit) {
			return;
		}
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		context.getContentResolver().registerContentObserver(AURORA_CONTENT_URI, true, mAuroraObserver);
		resumeSongDb();
		mbFirstInit = true;
	}

	public static boolean registerDbObserver(Context context, DbChangeListener dListener) {
		// Log.i(TAG, "zll ----- registerDbObserver 2 mbFirstInit:" +
		// mbFirstInit);
		if (context == null || dListener == null) {
			return false;
		}
		if (mbFirstInit) {
			if (mDbChangeListener == null) {
				mDbChangeListener = dListener;
			}
			return false;
		} else {
			mDbChangeListener = dListener;
		}
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		context.getContentResolver().registerContentObserver(AURORA_CONTENT_URI, true, mAuroraObserver);
		resumeSongDb();
		mbFirstInit = true;
		return true;
	}

	public static void unregisterDbObserver(Context context) {
		// Log.i(TAG, "zll ----- unregisterDbObserver mbFirstInit:" +
		// mbFirstInit);
		pauseSongDb();
		context.getContentResolver().unregisterContentObserver(mAuroraObserver);
		mbFirstInit = false;
	}

	private static final ContentObserver mAuroraObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			if (mDbChangeListener != null) {
				mDbChangeListener.onDbChanged(selfChange);
			}
		}
	};

	public static void resumeSongDb() {
		if (mSongDb != null) {
			mSongDb.resume();
		}
	}

	public static void pauseSongDb() {
		if (mSongDb != null) {
			mSongDb.pause();
		}
	}

	// Iuni <lory><2014-05-30> add end
	public static String makeAlbumsLabel(Context context, int numalbums, int numsongs, boolean isUnknown) {
		// There are two formats for the albums/songs information:
		// "N Song(s)" - used for unknown artist/album
		// "N Album(s)" - used for known albums
		StringBuilder songs_albums = new StringBuilder();
		Resources r = context.getResources();
		if (isUnknown) {
			if (numsongs == 1) {
				songs_albums.append(context.getString(R.string.onesong));
			} else {
				String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
				sFormatBuilder.setLength(0);
				sFormatter.format(f, Integer.valueOf(numsongs));
				songs_albums.append(sFormatBuilder);
			}
		} else {
			String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
			sFormatBuilder.setLength(0);
			sFormatter.format(f, Integer.valueOf(numalbums));
			songs_albums.append(sFormatBuilder);
			songs_albums.append(context.getString(R.string.albumsongseparator));
		}
		return songs_albums.toString();
	}

	/**
	 * This is now only used for the query screen
	 */
	public static String makeAlbumsSongsLabel(Context context, int numalbums, int numsongs, boolean isUnknown) {
		// There are several formats for the albums/songs information:
		// "1 Song" - used if there is only 1 song
		// "N Songs" - used for the "unknown artist" item
		// "1 Album"/"N Songs"
		// "N Album"/"M Songs"
		// Depending on locale, these may need to be further subdivided
		StringBuilder songs_albums = new StringBuilder();
		if (numsongs == 1) {
			songs_albums.append(context.getString(R.string.onesong));
		} else {
			Resources r = context.getResources();
			if (!isUnknown) {
				String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
				sFormatBuilder.setLength(0);
				sFormatter.format(f, Integer.valueOf(numalbums));
				songs_albums.append(sFormatBuilder);
				songs_albums.append(context.getString(R.string.albumsongseparator));
			}
			String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
			sFormatBuilder.setLength(0);
			sFormatter.format(f, Integer.valueOf(numsongs));
			songs_albums.append(sFormatBuilder);
		}
		return songs_albums.toString();
	}

	public static IMediaPlaybackService sService = null;
	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

	public static class ServiceToken {
		ContextWrapper mWrappedContext;

		ServiceToken(ContextWrapper context) {
			mWrappedContext = context;
		}
	}

	// 2014.6.6 lory add for lock music screen start
	/**
	 * 锁屏音乐bindToService
	 * @param context
	 * @return
	 */
	public static ServiceToken bindToService(Context context) {
		return bindToService(context, null);
	}

	/**
	 * 锁屏音乐bindToService
	 * @param context
	 * @param callback
	 * @return
	 */
	private static ServiceToken bindToService(Context context, ServiceConnection callback) {
		if (context == null) {
			LogUtil.e("Music", "zll Failed to bind to service 1");
			return null;
		}
		ContextWrapper cw = new ContextWrapper(context);
		cw.startService(new Intent(cw, MediaPlaybackService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (cw.bindService((new Intent()).setClass(cw, MediaPlaybackService.class), sb, 0)) {
			sConnectionMap.put(cw, sb);
			return new ServiceToken(cw);
		}
		LogUtil.e("Music", "zll Failed to bind to service 2");
		return null;
	}

	// 2014.6.6 lory add for lock music screen end
	public static ServiceToken bindToService(Activity context) {
		return bindToService(context, null);
	}

	public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
		Activity realActivity = context.getParent();
		if (realActivity == null) {
			realActivity = context;
		}
		ContextWrapper cw = new ContextWrapper(realActivity);
		cw.startService(new Intent(cw, MediaPlaybackService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (cw.bindService((new Intent()).setClass(cw, MediaPlaybackService.class), sb, 0)) {
			sConnectionMap.put(cw, sb);
			LogUtil.d(TAG, "---- bind to service context:" + context);
			return new ServiceToken(cw);
		}
		LogUtil.e(TAG, "Failed to bind to service");
		return null;
	}

	public static void unbindFromService(ServiceToken token) {
		if (token == null) {
			LogUtil.e(TAG, "-------- Trying to unbind with null token");
			return;
		}
		ContextWrapper cw = token.mWrappedContext;
		ServiceBinder sb = sConnectionMap.remove(cw);
		if (sb == null) {
			LogUtil.e(TAG, "------ Trying to unbind for unknown Context");
			return;
		}
		cw.unbindService(sb);
		LogUtil.e(TAG, "-------- unbindFromService 3");
		if (sConnectionMap.isEmpty()) {
			LogUtil.e(TAG, "-------- sConnectionMap is isEmpty");
			// presumably there is nobody interested in the service at this
			// point,
			// so don't hang on to the ServiceConnection
			sService = null;
		}
	}

	public static void resetStaticService() {
		sConnectionMap.clear();
		sService = null;
		LogUtil.d(TAG, "resetStaticService when service onDestroy!");
	}

	private static class ServiceBinder implements ServiceConnection {
		ServiceConnection mCallback;

		ServiceBinder(ServiceConnection callback) {
			mCallback = callback;
		}

		public void onServiceConnected(ComponentName className, android.os.IBinder service) {
			sService = IMediaPlaybackService.Stub.asInterface(service);
			initAlbumArtCache();
			if (mCallback != null) {
				mCallback.onServiceConnected(className, service);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			if (mCallback != null) {
				mCallback.onServiceDisconnected(className);
			}
			LogUtil.e(TAG, "-------- onServiceDisconnected");
			sService = null;
		}
	}

	public static long getCurrentAlbumId() {
		if (sService != null) {
			try {
				return sService.getAlbumId();
			} catch (RemoteException ex) {
			}
		}
		return -1;
	}

	public static long getCurrentArtistId() {
		if (MusicUtils.sService != null) {
			try {
				return sService.getArtistId();
			} catch (RemoteException ex) {
			}
		}
		return -1;
	}

	public static long getCurrentAudioId() {
		if (MusicUtils.sService != null) {
			try {
				return sService.getAudioId();
			} catch (RemoteException ex) {
			}
		}
		return -1;
	}

	// Iuni <lory><2014-06-14> add begin
	public static int getCurrentPlayMode() {
		int mode = MediaPlaybackService.REPEAT_ALL;
		if (sService != null) {
			try {
				int repeat = sService.getRepeatMode();
				if (repeat == MediaPlaybackService.REPEAT_ALL) {
					int shuffle = sService.getShuffleMode();
					if (shuffle == MediaPlaybackService.SHUFFLE_NORMAL || shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
						mode = MediaPlaybackService.SHUFFLE_NORMAL;
					} else {
						mode = MediaPlaybackService.REPEAT_ALL;
					}
				} else if (repeat == MediaPlaybackService.REPEAT_CURRENT) {
					mode = MediaPlaybackService.REPEAT_CURRENT;
				}
			} catch (Exception e) {
			}
		}
		return mode;
	}

	// Iuni <lory><2014-06-14> add end
	public static int getCurrentShuffleMode() {
		int mode = MediaPlaybackService.SHUFFLE_NONE;
		if (sService != null) {
			try {
				mode = sService.getShuffleMode();
			} catch (RemoteException ex) {
			}
		}
		return mode;
	}

	public static void togglePartyShuffle() {
		if (sService != null) {
			int shuffle = getCurrentShuffleMode();
			try {
				if (shuffle == MediaPlaybackService.SHUFFLE_AUTO || shuffle == MediaPlaybackService.SHUFFLE_NORMAL) {
					sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
				} else {
					// lory del 2014.5.20
					// sService.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
					sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
				}
			} catch (RemoteException ex) {
			}
		}
	}

	public static void setPartyShuffleMenuIcon(Menu menu) {
		MenuItem item = menu.findItem(Defs.PARTY_SHUFFLE);
		if (item != null) {
			int shuffle = MusicUtils.getCurrentShuffleMode();
			if (shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
				item.setIcon(R.drawable.ic_menu_party_shuffle);
				item.setTitle(R.string.party_shuffle_off);
			} else {
				item.setIcon(R.drawable.ic_menu_party_shuffle);
				item.setTitle(R.string.party_shuffle);
			}
		}
	}

	/*
	 * Returns true if a file is currently opened for playback (regardless of whether it's playing or paused).
	 */
	public static boolean isMusicLoaded() {
		if (MusicUtils.sService != null) {
			try {
				return sService.getPath() != null;
			} catch (RemoteException ex) {
			}
		}
		return false;
	}

	private final static long[] sEmptyList = new long[0];

	public static long[] getSongListForCursor(Cursor cursor) {
		if (cursor == null) {
			return sEmptyList;
		}
		int len = cursor.getCount();
		long[] list = new long[len];
		cursor.moveToFirst();
		int colidx = -1;
		try {
			colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
		} catch (IllegalArgumentException ex) {
			colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
		}
		for (int i = 0; i < len; i++) {
			list[i] = cursor.getLong(colidx);
			cursor.moveToNext();
		}
		return list;
	}

	public static long[] getSongListForCursor(Cursor cursor, AuroraDeleteItem item) {
		if (cursor == null) {
			return sEmptyList;
		}
		int len = cursor.getCount();
		long[] list = new long[len];
		String[] pathList = new String[len];
		cursor.moveToFirst();
		int colidx = -1;
		int pathid = -1;
		try {
			colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
		} catch (IllegalArgumentException ex) {
			colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
		}
		pathid = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		for (int i = 0; i < len; i++) {
			list[i] = cursor.getLong(colidx);
			String path = cursor.getString(pathid);
			if (!TextUtils.isEmpty(path)) {
				pathList[i] = path;
			}
			cursor.moveToNext();
		}
		item.setId(list);
		item.setPath(pathList);
		return list;
	}

	// ----add by tangjie 2015/01/08-----//
	public static AuroraDeleteItem getSongListForArtist(Context context, long id, String title) {
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA };
		String where = MediaStore.Audio.Media.ARTIST_ID + "=" + id;
		// modify by tangjie 2014/06/23
		// + " AND "
		// + MediaStore.Audio.Media.IS_MUSIC + "=1";
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.ALBUM_KEY + "," + MediaStore.Audio.Media.TRACK);
		if (cursor != null) {
			AuroraDeleteItem deleteItem = new AuroraDeleteItem();
			getSongListForCursor(cursor, deleteItem);
			cursor.close();
			return deleteItem;
		}
		return null;
	}

	public static long[] getSongListForArtist(Context context, long id) {
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = MediaStore.Audio.Media.ARTIST_ID + "=" + id;
		// modify by tangjie 2014/06/23
		// + " AND "
		// + MediaStore.Audio.Media.IS_MUSIC + "=1";
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.ALBUM_KEY + "," + MediaStore.Audio.Media.TRACK);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	// aurora ukiliu 2014-07-14 add begin
	public static ArrayList<AuroraListItem> getSongsForArtist(Context context, long id, List<String> paths) {
		ArrayList<AuroraListItem> mList = new ArrayList<AuroraListItem>();
		final String[] ccols = DataConvertUtil.trackCols;
		String where = MediaStore.Audio.Media.ARTIST_ID + "=" + id;
		// modify by tangjie 2014/06/23
		// + " AND "
		// + MediaStore.Audio.Media.IS_MUSIC + "=1";
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.ALBUM_KEY + "," + MediaStore.Audio.Media.TRACK);
		if (cursor != null) {
			mList = DataConvertUtil.ConvertToTrack(cursor, paths, context);
			cursor.close();
			return mList;
		}
		return mList;
	}

	// aurora ukiliu 2014-07-14 add end
	public static long[] getSongListForAlbum(Context context, long id) {
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = MediaStore.Audio.Media.ALBUM_ID + "=" + id;
		// modify by tangjie 2014/06/23
		// + " AND "
		// + MediaStore.Audio.Media.IS_MUSIC + "=1";
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.TRACK);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	// aurora ukiliu 2014-05-20 add begin
	public static long[] getSongListForAuroraAlbum(Context context, long albumId, long artistId) {
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = MediaStore.Audio.Media.ALBUM_ID + "=" + albumId + " AND " + MediaStore.Audio.Media.ARTIST_ID
				+ "=" + artistId
		/* + " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1" */;
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.TRACK);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	// aurora ukiliu 2014-05-20 add end
	// Iuni <lory><2014-05-16> add begin
	static final String[] mCursorCols = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION };

	public static void setAuroraPlayList(Context context, long[] list) {
		if (mSortList == null || list == null || list.length == 0) {
			return;
		}
		int len = list.length;
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media._ID + " IN (");
		for (int i = 0; i < len; i++) {
			where.append(list[i]);
			if (i < len - 1) {
				where.append(",");
			}
		}
		where.append(")");
		if (mSortList == null) {
			mSortList = new ArrayList<AuroraListItem>();
		}
		Cursor tCursor = null;
		try {
			tCursor = MusicUtils.query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
					where.toString(), null, null);
			if (tCursor == null) {
				return;
			}
			mSortList.clear();
			if (tCursor.moveToFirst()) {
				do {
					int mId = tCursor.getInt(0);
					String mTitle = tCursor.getString(1);
					String mPath = tCursor.getString(2);
					String mAlbumName = tCursor.getString(3);
					String mArtistName = tCursor.getString(4);
					String mUri = mPath;
					AuroraListItem item = new AuroraListItem(mId, mTitle, mUri, mAlbumName, -1, mArtistName, 0, null,
							null, null, -1);
					mSortList.add(item);
				} while (tCursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tCursor != null) {
				tCursor.close();
			}
		}
		return;
	}

	/*
	 * public static Bitmap getArtworkEx(Context context, long song_id, long album_id) { return getArtworkEx(context,
	 * song_id, album_id, true); }
	 */
	// lory add 2014.6.13 start
	private static boolean mFadeIn = true;
	private static final int AURORA_FADE_IN_TIME = 100;
	private static Bitmap mLoadingBitmap = null;
	private static byte[] mblock = new byte[0];

	public static void setFadInImageDrawable(Context context, ImageView imageView, Drawable srcdrawable,
			Drawable decdrawable) {
		if (decdrawable == null || srcdrawable == null) {
			return;
		}
		synchronized (mblock) {
			if (mLoadingBitmap == null) {
				mLoadingBitmap = getDefaultArtworkEx(context);
			}
			if (mFadeIn) {
				final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
						new ColorDrawable(android.R.color.transparent), decdrawable });
				imageView.setBackgroundDrawable(srcdrawable);
				imageView.setImageDrawable(td);
				td.startTransition(AURORA_FADE_IN_TIME);
			} else {
				imageView.setImageDrawable(decdrawable);
			}
		}
		return;
	}

	// lory add 2014.6.13 end
	private static Bitmap getDefaultArtworkEx(Context context) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		int rid = -1;
		if (AuroraMusicUtil.isIndiaVersion()) {
			rid = R.drawable.default_album_india_bg;
		} else {
			rid = R.drawable.default_album_bg;
		}
		return BitmapFactory.decodeStream(context.getResources().openRawResource(rid), null, opts);// lory
																									// modify
																									// 2014.5.9
																									// albumart_mp_unknown
	}

	public static Bitmap getArtworkEx(Context context, long song_id, long album_id, boolean allowdefault, int w, int h) {
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1, w, h);
				if (bm != null) {
					LogUtil.d(TAG, " -----------getArtworkEx 1");
					return bm;
				}
			}
			if (allowdefault) {
				LogUtil.d(TAG, " -----------getArtworkEx 2");
				return getDefaultArtworkEx(context);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				LogUtil.d(TAG, " -----------getArtworkEx 3");
				return BitmapFactory.decodeStream(in, null, sBitmapOptions);
			} catch (FileNotFoundException ex) {
				// The album art thumbnail does not actually exist. Maybe the
				// user deleted it, or
				// maybe it never existed to begin with.
				Bitmap bm = getArtworkFromFile(context, song_id, album_id, w, h);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							LogUtil.d(TAG, " -----------getArtworkEx 4");
							return getDefaultArtworkEx(context);
						}
					}
				} else if (allowdefault) {
					LogUtil.d(TAG, " -----------getArtworkEx 4");
					bm = getDefaultArtworkEx(context);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}

	/************************** 歌词显示 start *************************************/
	public static final String AURORA_PLAYLIST_FAVORITES = "AuroraFavorites";
	public final static String AURORA_EXTERNAL = "external";

	public static boolean isHaveLrc(Context context, String title, String artist) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		return mSongDb.isHaveLrc(title, artist);
	}

	public static void addToAudioInfoLrc(Context context, long id, String title, String artist, String path) {
		addToAudioInfoLrcEx(context, id, title, artist, null, path, 0);
		return;
	}

	public static void addToAudioInfoLrcEx(Context context, long id, String title, String artist, String picpath,
			String lrcpath, int isnet) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		mSongDb.addToAudioInfo(id, title, artist, picpath, lrcpath, isnet);
		return;
	}

	public static String getLocalLrc(Context context, long id, String title, String artist) {
		return getDblrc(context, id, title, artist, 0);
	}

	public static AuroraMusicInfo getDbMusicInfo(Context context, long id, String title, String artist, int isnet) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		AuroraMusicInfo info = mSongDb.getAuroraMusicInfo(id, title, artist, isnet);
		if (info != null) {
			return info;
		}
		return null;
	}

	public static String getDblrc(Context context, long id, String title, String artist, int isnet) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		AuroraMusicInfo info = mSongDb.getAuroraMusicInfo(id, title, artist, isnet);
		if (info != null && info.getLrcPath() != null) {
			return info.getLrcPath();
		}
		return null;
	}

	public static String getDbImg(Context context, long id, String title, String artist, int isnet) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		AuroraMusicInfo info = mSongDb.getAuroraMusicInfo(id, title, artist, isnet);
		if (info != null && info.getPicPath() != null) {
			return info.getPicPath();
		}
		return null;
	}

	public static void addToAudioInfoPic(Context context) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		return;
	}

	public static String getLocalPic(Context context) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		return null;
	}

	/************************** 歌词显示 end *************************************/
	/**
	 * 判断是否是最喜爱的歌曲
	 * @param context
	 * @param id
	 */
	public static boolean isFavorite(Context context, long id, int isnet) {
		if (mSongDb == null) {
			mSongDb = new AuroraSongDb(context);
		}
		return mSongDb.isFavoriteEx(id, isnet);
	}

	/**
	 * 创建新的数据库播放列表
	 * @param context
	 * @param name
	 * @return
	 */
	public static long createAuroraPlaylist(Context context, String name) {
		if (name != null && name.length() > 0) {
			ContentResolver resolver = context.getContentResolver();
			String[] cols = new String[] { PlaylistsColumns.NAME };
			String whereclause = PlaylistsColumns.NAME + " = '" + name + "'";
			Cursor cur = resolver.query(Audio.Playlists.EXTERNAL_CONTENT_URI, cols, whereclause, null, null);
			if (cur.getCount() <= 0) {
				ContentValues values = new ContentValues(1);
				values.put(PlaylistsColumns.NAME, name);
				Uri uri = resolver.insert(Audio.Playlists.EXTERNAL_CONTENT_URI, values);
				return Long.parseLong(uri.getLastPathSegment());
			}
			return -1;
		}
		return -1;
	}

	/**
	 * 删除最喜爱的歌曲
	 * @param context
	 * @param id
	 */
	public static void removeFromFavorites(Context context, long id, int isnet) {
		if (true) {
			if (mSongDb == null) {
				mSongDb = new AuroraSongDb(context);
			}
			mSongDb.removeFromFavoritesEx(id, isnet);
			return;
		}
		long favorites_id;
		if (id < 0) {
			return;
		} else {
			ContentResolver resolver = context.getContentResolver();
			String favorites_where = PlaylistsColumns.NAME + "='" + AURORA_PLAYLIST_FAVORITES + "'";
			String[] favorites_cols = new String[] { BaseColumns._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = null;
			try {
				cursor = resolver.query(favorites_uri, favorites_cols, favorites_where, null, null);
				if (cursor != null) {
					if (cursor.getCount() <= 0) {
						favorites_id = createAuroraPlaylist(context, AURORA_PLAYLIST_FAVORITES);
					} else {
						cursor.moveToFirst();
						favorites_id = cursor.getLong(0);
						cursor.close();
					}
					Uri uri = Playlists.Members.getContentUri(AURORA_EXTERNAL, favorites_id);
					resolver.delete(uri, Playlists.Members.AUDIO_ID + "=" + id, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		}
	}

	/**
	 * @param context
	 * @param id
	 */
	public static void addToFavorite(Context context, AuroraListItem item) {
		if (true) {
			if (mSongDb == null) {
				mSongDb = new AuroraSongDb(context);
			}
			mSongDb.addToFavoritesEx(item);
			return;
		}
		long favorites_id;
		if (item == null) {
			return;
		} else {
			ContentResolver resolver = context.getContentResolver();
			String favorites_where = PlaylistsColumns.NAME + "='" + AURORA_PLAYLIST_FAVORITES + "'";
			String[] favorites_cols = new String[] { BaseColumns._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = resolver.query(favorites_uri, favorites_cols, favorites_where, null, null);
			if (cursor.getCount() <= 0) {
				favorites_id = createAuroraPlaylist(context, AURORA_PLAYLIST_FAVORITES);
			} else {
				cursor.moveToFirst();
				favorites_id = cursor.getLong(0);
				cursor.close();
			}
			String[] cols = new String[] { Playlists.Members.AUDIO_ID };
			Uri uri = Playlists.Members.getContentUri(AURORA_EXTERNAL, favorites_id);
			Cursor cur = null;
			try {
				cur = resolver.query(uri, cols, null, null, null);
				if (cur != null) {
					int base = cur.getCount();
					cur.moveToFirst();
					while (!cur.isAfterLast()) {
						if (cur.getLong(0) == item.getSongId())
							return;
						cur.moveToNext();
					}
					ContentValues values = new ContentValues();
					values.put(Playlists.Members.AUDIO_ID, item.getSongId());
					values.put(Playlists.Members.PLAY_ORDER, base + 1);
					resolver.insert(uri, values);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cur != null) {
					cur.close();
					cur = null;
				}
			}
		}
	}

	public static void toggleMyFavorite() {
		if (sService == null)
			return;
		try {
			sService.toggleMyFavorite();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// Iuni <lory><2014-05-16> add end
	// aurora ukiliu 2014-05-15 add begin
	public static long[] getSongListForAlbums(Context context, ArrayList<Long> ids) {
		int numAlbum = ids == null ? 0 : ids.size();
		String albumWhere = " (";
		for (int i = 0; i < numAlbum; i++) {
			if (i != 0) {
				albumWhere += " OR ";
			}
			albumWhere += MediaStore.Audio.Media.ALBUM_ID + " = " + ids.get(i);
		}
		albumWhere += ") ";
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = albumWhere /*
								 * + " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1"
								 */;
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.TRACK);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	// aurora ukiliu 2014-05-15 add end
	// aurora ukiliu 2014-05-21 add begin
	public static long[] getSongListForAuroraAlbums(Context context, ArrayList<Long> ids, long artistId) {
		int numAlbum = ids == null ? 0 : ids.size();
		// ukiliu add by BUG #6952 2014-07-28 begin
		if (numAlbum == 0) {
			return sEmptyList;
		}
		// ukiliu add by BUG #6952 2014-07-28 end
		String artistWhere = MediaStore.Audio.Media.ARTIST_ID + "=" + artistId;
		String albumWhere = " (";
		for (int i = 0; i < numAlbum; i++) {
			if (i != 0) {
				albumWhere += " OR ";
			}
			albumWhere += MediaStore.Audio.Media.ALBUM_ID + " = " + ids.get(i);
		}
		albumWhere += ") ";
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA };
		String where = Globals.QUERY_SONG_FILTER + " AND " + artistWhere + " AND " + albumWhere /*
																								 * + " AND " +
																								 * MediaStore . Audio .
																								 * Media . IS_MUSIC +
																								 * "=1"
																								 */;
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.TRACK);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	// aurora ukiliu 2014-05-21 add end
	public static long[] getSongListForPlaylist(Context context, long plid) {
		final String[] ccols = new String[] { MediaStore.Audio.Playlists.Members.AUDIO_ID };
		Cursor cursor = query(context, MediaStore.Audio.Playlists.Members.getContentUri("external", plid), ccols, null,
				null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	/*
	 * public static void playPlaylist(Context context, long plid) { long[] list = getSongListForPlaylist(context,
	 * plid); if (list != null) { playAll(context, list, -1, false); } }
	 */
	public static long[] getAllSongs(Context context) {
		Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media._ID }, null, null, null);
		try {
			if (c == null || c.getCount() == 0) {
				return null;
			}
			int len = c.getCount();
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				c.moveToNext();
				list[i] = c.getLong(0);
			}
			return list;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	/**
	 * Fills out the given submenu with items for "new playlist" and any
	 * existing playlists. When the user selects an item, the application will
	 * receive PLAYLIST_SELECTED with the Uri of the selected playlist,
	 * NEW_PLAYLIST if a new playlist should be created, and QUEUE if the
	 * "current playlist" was selected.
	 * @param context
	 *            The context to use for creating the menu items
	 * @param sub
	 *            The submenu to add the items to.
	 */
	public static void makePlaylistMenu(Context context, SubMenu sub) {
		String[] cols = new String[] { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME };
		ContentResolver resolver = context.getContentResolver();
		if (resolver == null) {
			System.out.println("resolver = null");
		} else {
			String whereclause = MediaStore.Audio.Playlists.NAME + " != ''";
			Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cols, whereclause, null,
					MediaStore.Audio.Playlists.NAME);
			sub.clear();
			sub.add(1, Defs.QUEUE, 0, R.string.queue);
			sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
			if (cur != null && cur.getCount() > 0) {
				// sub.addSeparator(1, 0);
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					Intent intent = new Intent();
					intent.putExtra("playlist", cur.getLong(0));
					// if (cur.getInt(0) == mLastPlaylistSelected) {
					// sub.add(0, MusicBaseActivity.PLAYLIST_SELECTED,
					// cur.getString(1)).setIntent(intent);
					// } else {
					sub.add(1, Defs.PLAYLIST_SELECTED, 0, cur.getString(1)).setIntent(intent);
					// }
					cur.moveToNext();
				}
			}
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static void clearPlaylist(Context context, int plid) {
		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", plid);
		context.getContentResolver().delete(uri, null, null);
		return;
	}

	// add by chenhl start 20140612
	public static void removeTracksFromCurrentPlaylist(Context context, long[] list, int playlistid) {
		if (playlistid != AuroraMusicUtil.getCurrentPlaylist(context)) {
			return;
		}
		String[] cols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID };
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media._ID + " IN (");
		for (int i = 0; i < list.length; i++) {
			try {
				sService.removeTrack(list[i]);
			} catch (RemoteException ex) {
			}
			where.append(list[i]);
			if (i < list.length - 1) {
				where.append(",");
			}
		}
		where.append(")");
		Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, where.toString(), null, null);
		if (c != null) {
			// step 1: remove selected tracks from the current playlist, as well
			// as from the album art cache
			c.moveToFirst();
			while (!c.isAfterLast()) {
				// remove from current playlist
				long artIndex = c.getLong(2);
				synchronized (sArtCache) {
					sArtCache.remove(artIndex);
				}
				c.moveToNext();
			}
		}
	}

	// add by chenhl end
	public static void deleteTracks(Context context, long[] list) {
		String[] cols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID };
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media._ID + " IN (");
		for (int i = 0; i < list.length; i++) {
			where.append(list[i]);
			if (i < list.length - 1) {
				where.append(",");
			}
		}
		where.append(")");
		Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, where.toString(), null, null);
		if (c != null) {
			// step 1: remove selected tracks from the current playlist, as well
			// as from the album art cache
			try {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					// remove from current playlist
					long id = c.getLong(0);
					sService.removeTrack(id);
					// remove from album art cache
					long artIndex = c.getLong(2);
					synchronized (sArtCache) {
						sArtCache.remove(artIndex);
					}
					c.moveToNext();
				}
			} catch (RemoteException ex) {
			}
			// step 2: remove selected tracks from the database
			context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where.toString(), null);
			// step 3: remove files from card
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String name = c.getString(1);
				File f = new File(name);
				try { // File.delete can throw a security exception
					if (!f.delete()) {
						// I'm not sure if we'd ever get here (deletion would
						// have to fail, but no exception thrown)
						LogUtil.e(TAG, "Failed to delete file " + name);
					}
					c.moveToNext();
				} catch (SecurityException ex) {
					c.moveToNext();
				}
			}
			c.close();
		}
		String message = context.getResources().getQuantityString(R.plurals.NNNtracksdeleted, list.length,
				Integer.valueOf(list.length));
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		// We deleted a number of tracks, which could affect any number of
		// things
		// in the media content domain, so update everything.
		context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
	}

	// add by tangjie 2014/06/03
	public static void deleteTracks(Context context, long[] list, DialogUtil.OnDeleteFileListener deleteFileListener) {
		String[] cols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID };
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media._ID + " IN (");
		for (int i = 0; i < list.length; i++) {
			where.append(list[i]);
			if (i < list.length - 1) {
				where.append(",");
			}
		}
		where.append(")");
		Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, where.toString(), null, null);
		if (c != null) {
			// step 1: remove selected tracks from the current playlist, as well
			// as from the album art cache
			try {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					// remove from current playlist
					long id = c.getLong(0);
					sService.removeTrack(id);
					// remove from album art cache
					long artIndex = c.getLong(2);
					synchronized (sArtCache) {
						sArtCache.remove(artIndex);
					}
					c.moveToNext();
				}
			} catch (RemoteException ex) {
			}
			// step 2: remove selected tracks from the database
			context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where.toString(), null);
			// step 3: remove files from card
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String name = c.getString(1);
				File f = new File(name);
				try { // File.delete can throw a security exception
					if (!f.delete()) {
						// I'm not sure if we'd ever get here (deletion would
						// have to fail, but no exception thrown)
						LogUtil.e(TAG, "Failed to delete file " + name);
					} else {
						deleteFileListener.OnDeleteFileSuccess();
					}
					c.moveToNext();
				} catch (SecurityException ex) {
					c.moveToNext();
				}
			}
			c.close();
		}
		// We deleted a number of tracks, which could affect any number of
		// things
		// in the media content domain, so update everything.
		context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
	}
	
	public static void removeMediaTracks(Context context, long[] list,
			DialogUtil.OnRemoveFileListener onRemoveFileListener,int playListId) {
		new RemoveTask(context, list, onRemoveFileListener,playListId).executeOnExecutor(ThreadPoolExecutorUtils
				.getThreadPoolExecutor().getExecutor());
	}

	// add by tangjie 2014/6/17 start
	public static void deleteMediaTracks(Context context, long[] list,
			DialogUtil.OnDeleteFileListener deleteFileListener) {
		new DeleteTask(context, list, deleteFileListener).executeOnExecutor(ThreadPoolExecutorUtils
				.getThreadPoolExecutor().getExecutor());
	}

	public static void deleteMediaTracks(Context context, AuroraDeleteItem item,
			DialogUtil.OnDeleteFileListener deleteFileListener) {
		new DeleteTask(context, item, deleteFileListener).executeOnExecutor(ThreadPoolExecutorUtils
				.getThreadPoolExecutor().getExecutor());
	}

	public static void deleteMediaTracks(Context context, long[] list,
			DialogUtil.OnDeleteFileListener deleteFileListener, ArrayList<AuroraListItem> datalist) {
		new DeleteTask(context, list, deleteFileListener, datalist).executeOnExecutor(ThreadPoolExecutorUtils
				.getThreadPoolExecutor().getExecutor());
	}

	// add by tangjie 2014/6/17 end
	public static void addToCurrentPlaylist(Context context, long[] list) {
		if (sService == null) {
			return;
		}
		try {
			sService.enqueue(list, MediaPlaybackService.LAST);
			String message = context.getResources().getQuantityString(R.plurals.NNNtrackstoplaylist, list.length,
					Integer.valueOf(list.length));
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		} catch (RemoteException ex) {
		}
	}

	private static ContentValues[] sContentValuesCache = null;

	/**
	 * @param ids
	 *            The source array containing all the ids to be added to the
	 *            playlist
	 * @param offset
	 *            Where in the 'ids' array we start reading
	 * @param len
	 *            How many items to copy during this pass
	 * @param base
	 *            The play order offset to use for this pass
	 */
	private static void makeInsertItems(long[] ids, int offset, int len, int base) {
		// adjust 'len' if would extend beyond the end of the source array
		if (offset + len > ids.length) {
			len = ids.length - offset;
		}
		// allocate the ContentValues array, or reallocate if it is the wrong
		// size
		if (sContentValuesCache == null || sContentValuesCache.length != len) {
			sContentValuesCache = new ContentValues[len];
		}
		// fill in the ContentValues array with the right values for this pass
		for (int i = 0; i < len; i++) {
			if (sContentValuesCache[i] == null) {
				sContentValuesCache[i] = new ContentValues();
			}
			sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
			sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
		}
	}

	// add by chenhl start 20140523
	public static void addToPlaylist(Context context, List<String> list, long playlistid) {
		/*
		 * Iterator<String> it=list.iterator(); while(it.hasNext()){ long id=Long.parseLong(it.next()); if
		 * (AuroraMusicUtil.isForPlaylistById(context, id , playlistid)){ it.remove(); } }
		 */
		long[] list1 = new long[list.size()];
		List<String> oldlist = AuroraMusicUtil.getPlayListAudioId(context, playlistid);
		for (int i = 0; i < list.size(); i++) {
			if (!oldlist.contains(list.get(i))) {
				list1[i] = Long.parseLong(list.get(i));
			}
		}
		addToPlaylist(context, list1, playlistid);
	}

	// add by chenhl end
	// add by tangjie start 20140606
	public static void addToPlaylist(Context context, List<String> list, long playlistid, String name) {
		long[] list1 = new long[list.size()];
		List<String> oldlist = AuroraMusicUtil.getPlayListAudioId(context, playlistid);
		for (int i = 0; i < list.size(); i++) {
			if (!oldlist.contains(list.get(i))) {
				list1[i] = Long.parseLong(list.get(i));
			}
		}
		addToPlaylist(context, list1, playlistid, name);
	}

	public static void addToPlaylist(Context context, long[] ids, long playlistid) {
		if (ids == null) {
			// this shouldn't happen (the menuitems shouldn't be visible
			// unless the selected item represents something playable
			LogUtil.e("MusicBase", "ListSelection null");
		} else {
			int size = ids.length;
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.
			// modify by chenhl start 20140522
			String[] cols = new String[] {
					// "count(*)"
					MediaStore.Audio.Playlists.Members.PLAY_ORDER, MediaStore.Audio.Media.TITLE, };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
			// Cursor cur = resolver.query(uri, cols, null, null, null);
			Cursor cur = resolver.query(uri, cols, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
			// cur.moveToFirst();
			// int base = cur.getInt(0);
			int base = cur.getCount();
			if (cur.moveToLast() && base > 0) {
				int order = cur.getInt(0);
				base = order + 1;
				LogUtil.d(TAG, "base:" + base + " last order:" + order);
			}
			cur.close();
			// modify by chenhl end
			int numinserted = 0;
			for (int i = 0; i < size; i += 1000) {
				makeInsertItems(ids, i, 1000, base);
				numinserted += resolver.bulkInsert(uri, sContentValuesCache);
			}
			String message = context.getResources().getQuantityString(R.plurals.NNNtrackstoplaylist, numinserted,
					numinserted);
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			// mLastPlaylistSelected = playlistid;
		}
	}

	public static void addToPlaylist(Context context, long[] ids, long playlistid, String name) {
		if (ids == null) {
			// this shouldn't happen (the menuitems shouldn't be visible
			// unless the selected item represents something playable
			LogUtil.e(TAG, "ListSelection null");
		} else {
			int size = ids.length;
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.
			// modify by chenhl start 20140522
			String[] cols = new String[] {
					// "count(*)"
					MediaStore.Audio.Playlists.Members.PLAY_ORDER, MediaStore.Audio.Media.TITLE, };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
			// Cursor cur = resolver.query(uri, cols, null, null, null);
			Cursor cur = resolver.query(uri, cols, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
			// cur.moveToFirst();
			// int base = cur.getInt(0);
			int base = cur.getCount();
			if (cur.moveToLast() && base > 0) {
				int order = cur.getInt(0);
				base = order + 1;
				LogUtil.d(TAG, "base:" + base + " last order:" + order);
			}
			cur.close();
			// modify by chenhl end
			int numinserted = 0;
			for (int i = 0; i < size; i += 1000) {
				makeInsertItems(ids, i, 1000, base);
				numinserted += resolver.bulkInsert(uri, sContentValuesCache);
			}
			String message;
			if (name != null && !TextUtils.isEmpty(name))
				message = context.getResources().getQuantityString(R.plurals.NNNtrackstoplaylist2, numinserted,
						numinserted, name);
			else
				message = context.getResources().getQuantityString(R.plurals.NNNtrackstoplaylist, numinserted,
						numinserted);
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			// mLastPlaylistSelected = playlistid;
		}
	}

	public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, int limit) {
		try {
			ContentResolver resolver = context.getContentResolver();
			if (resolver == null) {
				return null;
			}
			if (limit > 0) {
				uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
			}
			return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (UnsupportedOperationException ex) {
			return null;
		}
	}

	public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
	}

	public static boolean isMediaScannerScanning(Context context) {
		boolean result = false;
		Cursor cursor = query(context, MediaStore.getMediaScannerUri(),
				new String[] { MediaStore.MEDIA_SCANNER_VOLUME }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() == 1) {
				cursor.moveToFirst();
				result = "external".equals(cursor.getString(0));
			}
			cursor.close();
		}
		return result;
	}

	public static void setSpinnerState(Activity a) {
		if (isMediaScannerScanning(a)) {
			// start the progress spinner
			a.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, Window.PROGRESS_INDETERMINATE_ON);
			a.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		} else {
			// stop the progress spinner
			a.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);
		}
	}

	static protected Uri getContentURIForPath(String path) {
		return Uri.fromFile(new File(path));
	}

	/*
	 * Try to use String.format() as little as possible, because it creates a new Formatter every time you call it,
	 * which is very inefficient. Reusing an existing Formatter more than tripled the speed of makeTimeString(). This
	 * Formatter/StringBuilder are also used by makeAlbumSongsLabel()
	 */
	private static StringBuilder sFormatBuilder = new StringBuilder();
	private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
	private static final Object[] sTimeArgs = new Object[5];

	public static String makeTimeString(Context context, long secs) {
		String durationformat = context.getString(secs < 3600 ? R.string.durationformatshort
				: R.string.durationformatlong);
		/*
		 * Provide multiple arguments so the format can be changed easily by modifying the xml.
		 */
		sFormatBuilder.setLength(0);
		final Object[] timeArgs = sTimeArgs;
		timeArgs[0] = secs / 3600;
		timeArgs[1] = secs / 60;
		timeArgs[2] = (secs / 60) % 60;
		timeArgs[3] = secs;
		timeArgs[4] = secs % 60;
		return sFormatter.format(durationformat, timeArgs).toString();
	}

	// Iuni <lory><2014-05-13> add begin
	private static ArrayList<AuroraListItem> mSortList = new ArrayList<AuroraListItem>();

	public static void setSortListData(ArrayList<AuroraListItem> list) {
		if (mSortList == null) {
			mSortList = new ArrayList<AuroraListItem>();
		}
		if (list == null) {
			return;
		}
		mSortList.clear();
	}

	public static ArrayList<AuroraListItem> getSortListData() {
		return mSortList;
	}

	// Iuni <lory><2014-05-13> add end
	// add by chenhl begin 20140515
	public static void setSortListDataEx(ArrayList<AuroraListItem> list) {
		if (mSortList == null) {
			mSortList = new ArrayList<AuroraListItem>();
		}
		if (list == null) {
			return;
		}
		mSortList.clear();
		mSortList.addAll(list);
	}

	public static void playRadioAll(final Context context, final ArrayList<AuroraListItem> list, final int position,
			final int repeat_mode, boolean isonline) {
		if (isonline) {
			boolean isshow = FlowTips.showPlayFlowTips(context, new OndialogClickListener() {
				@Override
				public void OndialogClick() {
					if (context instanceof MediaPlaybackService) {
						long[] llist = new long[list.size()];
						for (int i = 0; i < list.size(); i++) {
							llist[i] = list.get(i).getSongId();
						}
						try {
							MediaPlaybackService service = (MediaPlaybackService) context;
							service.setListInfo(list);
							service.open(llist, position);
							service.play();
						} catch (Exception e) {
							LogUtil.d(TAG, "chenhl playRadioAll 2 error!");
							e.printStackTrace();
						}
					} else {
						playAll2(context, list, position, repeat_mode);
					}
				}
			});
			if (isshow) {
				return;
			}
		}
		if (context instanceof MediaPlaybackService) {
			long[] llist = new long[list.size()];
			for (int i = 0; i < list.size(); i++) {
				llist[i] = list.get(i).getSongId();
			}
			try {
				MediaPlaybackService service = (MediaPlaybackService) context;
				service.setListInfo(list);
				service.open(llist, position);
				service.play();
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.d(TAG, "chenhl playRadioAll 1 error!");
			}
		} else {
			playAll2(context, list, position, repeat_mode);
		}
	}

	public static void playAll(final Context context, final ArrayList<AuroraListItem> list, final int position,
			final int repeat_mode, boolean isonline) {
		if (isonline) {
			FlowTips.showPlayFlowTips(context, new OndialogClickListener() {
				@Override
				public void OndialogClick() {
					playAll2(context, list, position, repeat_mode);
				}
			});
		}
		playAll(context, list, position, repeat_mode);
	}

	public static void playAll(Context context, ArrayList<AuroraListItem> list, int position, int repeat_mode) {
		Application.setRadio(-1, -1);
		playAll2(context, list, position, repeat_mode);
	}

	private static void playAll2(Context context, ArrayList<AuroraListItem> list, int position, int repeat_mode) {
		if (list.size() == 0) {
			LogUtil.e(TAG, "zll playAll fail, no music---");
			return;
		}
		if (sService == null) {
			LogUtil.e(TAG, "-----sService is null");
			return;
		}
		try {
			while (!list.get(position).isAvailable()) {
				position++;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		long[] llist = new long[list.size()];
		for (int i = 0; i < list.size(); i++) {
			llist[i] = list.get(i).getSongId();
		}
		try {
			sService.setListInfo(list);
		} catch (Exception e) {
			LogUtil.i(TAG, "zll playAll fail, setListInfo fail---");
		}
		playAllEx(context, llist, position, repeat_mode);
		AuroraMusicUtil.setCurrentPlaylist(context, -1);
		return;
	}

	private static void playAllEx(Context context, long[] list, int position, int repeat_mode) {
		AuroraMusicUtil.justTest("playAllEx");
		if (list.length == 0) {
			LogUtil.d(TAG, "---- attempt to play empty song list repeat_mode:" + repeat_mode);
			return;
		}
		if (sService == null) {
			LogUtil.e(TAG, "-----sService is null");
			return;
		}
		try {
			if (repeat_mode >= 4) {
				repeat_mode = 2;
			}
			if (repeat_mode == 1) {// 单曲循环
				sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
				sService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
			} else if (repeat_mode == 2) {// 全部循环
				sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
				sService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
			} else if (repeat_mode == 3) {// 随机播放
				sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
				sService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
			}
			if (position < 0) {
				position = 0;
			}
			LogUtil.d(TAG, "----repeat_mode:" + repeat_mode+"---position:"+position);
			if (repeat_mode == 3) {
				sService.shuffleOpen(list, position);
			} else {
				sService.open(list, position);
			}
			if (sService.isOnlineSong()) {
				sService.play();
			}
			// if (!mFirstInitPref) {
			// setIntPref(context, AURORA_FITST_ENTNER, 1);
			// mFirstInitPref = true;
			// }
		} catch (RemoteException ex) {
			LogUtil.e(TAG, "--playAllEx error", ex);
		}
	}

	public static void clearQueue() {
		try {
			sService.removeTracks(0, Integer.MAX_VALUE);
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	// A really simple BitmapDrawable-like class, that doesn't do
	// scaling, dithering or filtering.
	private static class FastBitmapDrawable extends Drawable {
		private Bitmap mBitmap;

		public FastBitmapDrawable(Bitmap b) {
			mBitmap = b;
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.OPAQUE;
		}

		@Override
		public void setAlpha(int alpha) {}

		@Override
		public void setColorFilter(ColorFilter cf) {}
	}

	private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	private static final HashMap<Long, Drawable> sArtCache = new HashMap<Long, Drawable>();
	private static int sArtCacheId = -1;
	static {
		// for the cache,
		// 565 is faster to decode and display
		// and we don't want to dither here because the image will be scaled
		// down later
		sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptionsCache.inDither = false;
		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptions.inDither = false;
	}

	public static void initAlbumArtCache() {
		try {
			int id = sService.getMediaMountedCount();
			if (id != sArtCacheId) {
				clearAlbumArtCache();
				sArtCacheId = id;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void clearAlbumArtCache() {
		synchronized (sArtCache) {
			sArtCache.clear();
		}
	}

	public static Drawable getCachedArtwork(Context context, long artIndex, BitmapDrawable defaultArtwork) {
		Drawable d = null;
		synchronized (sArtCache) {
			d = sArtCache.get(artIndex);
		}
		if (d == null) {
			d = defaultArtwork;
			final Bitmap icon = defaultArtwork.getBitmap();
			int w = icon.getWidth();
			int h = icon.getHeight();
			Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
			if (b != null) {
				d = new FastBitmapDrawable(b);
				synchronized (sArtCache) {
					// the cache may have changed since we checked
					Drawable value = sArtCache.get(artIndex);
					if (value == null) {
						sArtCache.put(artIndex, d);
					} else {
						d = value;
					}
				}
			}
		}
		return d;
	}

	// Get album art for specified album. This method will not try to
	// fall back to getting artwork directly from the file, nor will
	// it attempt to repair the database.
	private static Bitmap getArtworkQuick(Context context, long album_id, int w, int h) {
		// NOTE: There is in fact a 1 pixel border on the right side in the
		// ImageView
		// used to display this drawable. Take it into account now, so we don't
		// have to
		// scale later.
		w -= 1;
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			ParcelFileDescriptor fd = null;
			try {
				fd = res.openFileDescriptor(uri, "r");
				int sampleSize = 1;
				// Compute the closest power-of-two scale factor
				// and pass that to sBitmapOptionsCache.inSampleSize, which will
				// result in faster decoding and better quality
				sBitmapOptionsCache.inJustDecodeBounds = true;
				BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, sBitmapOptionsCache);
				int nextWidth = sBitmapOptionsCache.outWidth >> 1;
				int nextHeight = sBitmapOptionsCache.outHeight >> 1;
				while (nextWidth > w && nextHeight > h) {
					sampleSize <<= 1;
					nextWidth >>= 1;
					nextHeight >>= 1;
				}
				sBitmapOptionsCache.inSampleSize = sampleSize;
				sBitmapOptionsCache.inJustDecodeBounds = false;
				Bitmap b = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, sBitmapOptionsCache);
				if (b != null) {
					// finally rescale to exactly the size we need
					if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
						Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
						// Bitmap.createScaledBitmap() can return the same
						// bitmap
						if (tmp != b)
							b.recycle();
						b = tmp;
					}
				}
				return b;
			} catch (FileNotFoundException e) {
			} finally {
				try {
					if (fd != null)
						fd.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public static boolean hasArtwork(Context context, long album_id) {
		// NOTE: There is in fact a 1 pixel border on the right side in the
		// ImageView
		// used to display this drawable. Take it into account now, so we don't
		// have to
		// scale later.
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			try {
				res.openFileDescriptor(uri, "r");
			} catch (Exception exception) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get album art for specified album. You should not pass in the album id
	 * for the "unknown" album here (use -1 instead) This method always returns
	 * the default album art icon when no album art is found.
	 */
	/*
	 * public static Bitmap getArtwork(Context context, long song_id, long album_id, int w, int h) { return
	 * getArtwork(context, song_id, album_id, true, w, h); }
	 */
	// lory add
	public static Bitmap getAuroraArtwork(Context context, long song_id, long album_id, int w, int h) {
		return null;
		// return getArtwork(context, song_id, album_id, false, w, h);
	}

	public static Bitmap getSmallArtwork(Context context, long song_id, long album_id, boolean allowdefault,
			String path, int default_img, int w, int h) {
		Bitmap tmp = null;
		try {
			if (path != null && !path.isEmpty()) {
				tmp = BitmapUtil.decodeSampledBitmapFromFileForSmall(path, w, h);
			}
			if (tmp != null) {
				return tmp;
			} else {
				tmp = MusicUtils.getAuroraArtwork(context, song_id, -1, w, h);
			}
			if (tmp != null) {
				return tmp;
			}
			if (default_img == 0) {
				return getArtworkEx(context, song_id, album_id, allowdefault, w, h);
			} 
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	// default_img:0,表示播放器界面，其他直为非播放器界面
	public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefault, String path,
			int default_img, int w, int h) {
		Bitmap tmp = null;
		try {
			if (!TextUtils.isEmpty(path) && new File(path).exists()) {
				tmp = BitmapUtil.decodeSampledBitmapFromFile(path, w, h);
			}
			if (tmp != null) {
				return tmp;
			}
			/*
			 * LogUtil.d(TAG, " -----------getArtwork 2"); tmp = MusicUtils.getAuroraArtwork(context, song_id, -1, w,
			 * h);
			 * 
			 * if (tmp == null) { return tmp; }
			 */
			LogUtil.d(TAG, " -----------getArtwork 4");
			return getArtworkEx(context, song_id, album_id, allowdefault, w, h);
		} catch (Exception e) {
			return null;
		}
	}

	// get album art for specified file
	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid, int w, int h) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException("Must specify an album or a song id");
		}
		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				/*
				 * Cursor cursor = context.getContentResolver().query(uri, null, null, null, null); if (cursor != null)
				 * { LogUtil.d(TAG, "-----------------cursor:" + cursor.getCount()); String[] names =
				 * cursor.getColumnNames(); for (String string : names) { LogUtil.d(TAG,
				 * "-----------------cursor: names:" + string); } } cursor.close();
				 */
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					// bm = BitmapFactory.decodeFileDescriptor(fd);
					LogUtil.d(TAG, "---------getArtworkFromFile fd:" + fd + " fd:" + fd.valid() + " " + fd.toString());
					bm = BitmapUtil.decodeSampledBitmapFromDescriptor(fd, w, h);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					// bm = BitmapFactory.decodeFileDescriptor(fd);
					bm = BitmapUtil.decodeSampledBitmapFromDescriptor(fd, w, h);
				}
			}
		} catch (Exception ex) {
		}
		if (bm != null) {
		}
		return bm;
	}

	private static Bitmap getDefaultArtwork(Context context) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.default_music_icon), null,
				opts);// lory
						// modify
						// 2014.5.9
						// albumart_mp_unknown
	}

	public static int getIntPref(Context context, String name, int def) {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	public static void setIntPref(Context context, String name, int value) {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putInt(name, value);
		SharedPreferencesCompat.apply(ed);
	}

	public static boolean setRingtone(Context context, long id) {
		ContentResolver resolver = context.getContentResolver();
		// Set the flag in the database to mark this as a ringtone
		Uri ringUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
		try {
			ContentValues values = new ContentValues(2);
			values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
			values.put(MediaStore.Audio.Media.IS_ALARM, "1");
			resolver.update(ringUri, values, null, null);
		} catch (UnsupportedOperationException ex) {
			// most likely the card just got unmounted
			LogUtil.e(TAG, "zll couldn't set ringtone flag for id " + id);
			return false;
		}
		String[] cols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE };
		String where = MediaStore.Audio.Media._ID + "=" + id;
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, where, null, null);
		try {
			if (cursor != null && cursor.getCount() == 1) {
				// Set the system setting to make this the current ringtone
				cursor.moveToFirst();
				Settings.System.putString(resolver, Settings.System.RINGTONE, ringUri.toString());
				String message = context.getString(R.string.ringtone_set, cursor.getString(2));
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			} else {
				return false;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return true;
	}

	static int sActiveTabIndex = -1;

	@Deprecated
	public static boolean updateButtonBar(Activity a, int highlight) {
		final TabWidget ll = (TabWidget) a.findViewById(R.id.buttonbar);
		boolean withtabs = false;
		Intent intent = a.getIntent();
		if (intent != null) {
			withtabs = intent.getBooleanExtra("withtabs", false);
		}
		if (highlight == 0 || !withtabs) {
			ll.setVisibility(View.GONE);
			return withtabs;
		} else if (withtabs) {
			ll.setVisibility(View.VISIBLE);
		}
		for (int i = ll.getChildCount() - 1; i >= 0; i--) {
			View v = ll.getChildAt(i);
			boolean isActive = (v.getId() == highlight);
			if (isActive) {
				ll.setCurrentTab(i);
				sActiveTabIndex = i;
			}
			v.setTag(i);
			v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						for (int i = 0; i < ll.getTabCount(); i++) {
							if (ll.getChildTabViewAt(i) == v) {
								ll.setCurrentTab(i);
								processTabClick((Activity) ll.getContext(), v, ll.getChildAt(sActiveTabIndex).getId());
								break;
							}
						}
					}
				}
			});
			v.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					processTabClick((Activity) ll.getContext(), v, ll.getChildAt(sActiveTabIndex).getId());
				}
			});
		}
		return withtabs;
	}
	@Deprecated
	public static void processTabClick(Activity a, View v, int current) {
		int id = v.getId();
		if (id == current) {
			return;
		}
		final TabWidget ll = (TabWidget) a.findViewById(R.id.buttonbar);
		activateTab(a, id);
		if (id != R.id.nowplayingtab) {
			ll.setCurrentTab((Integer) v.getTag());
			setIntPref(a, "activetab", id);
		}
	}

	public static void activateTab(Activity a, int id) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		switch (id) {
		case R.id.artisttab:
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/artistalbum");
			break;
		case R.id.albumtab:
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/album");
			break;
		case R.id.songtab:
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
			break;
		case R.id.playlisttab:
			intent.setDataAndType(Uri.EMPTY, MediaStore.Audio.Playlists.CONTENT_TYPE);
			break;
		case R.id.nowplayingtab:
			/*
			 * lory del 2014.7.26 intent = new Intent(a, MediaPlaybackActivity.class); a.startActivity(intent);
			 */
			return;
			// fall through and return
		default:
			return;
		}
		intent.putExtra("withtabs", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		a.startActivity(intent);
		a.finish();
		// a.overridePendingTransition(0, 0);
	}

	public static String getSpell(String str) {
		StringBuffer buffer = new StringBuffer();
		boolean flag = false;
		if (str != null && !str.equals("")) {
			char[] cc = str.toCharArray();
			for (int i = 0; i < cc.length; i++) {
				ArrayList<Token> mArrayList = HanziToPinyin.getInstance().get(String.valueOf(cc[i]));
				if (mArrayList.size() > 0) {
					String n = mArrayList.get(0).target;
					buffer.append(n);
					if (i == 0 && (n.toUpperCase().charAt(0) > 90 || n.toUpperCase().charAt(0) < 65)) {
						flag = true;
					}
				}
			}
		}
		String spellStr = buffer.toString();
		if (flag) {
			spellStr = String.valueOf((char) 91) + str;
		}
		if ((spellStr != null && TextUtils.isEmpty(spellStr)) || (spellStr != null && spellStr.length() <= 0)) {
			spellStr = "[";
		}
		return spellStr.toUpperCase();
	}

	public static void updateNowPlaying(Activity a) {
		View nowPlayingView = a.findViewById(R.id.nowplaying);
		if (nowPlayingView == null) {
			return;
		}
		try {
			boolean withtabs = false;
			Intent intent = a.getIntent();
			if (intent != null) {
				withtabs = intent.getBooleanExtra("withtabs", false);
			}
			if (true && MusicUtils.sService != null && MusicUtils.sService.getAudioId() != -1) {
				TextView title = (TextView) nowPlayingView.findViewById(R.id.title);
				TextView artist = (TextView) nowPlayingView.findViewById(R.id.artist);
				title.setText(MusicUtils.sService.getTrackName());
				String artistName = MusicUtils.sService.getArtistName();
				if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
					artistName = a.getString(R.string.unknown_artist_name);
				}
				artist.setText(artistName);
				// mNowPlayingView.setOnFocusChangeListener(mFocuser);
				// mNowPlayingView.setOnClickListener(this);
				nowPlayingView.setVisibility(View.VISIBLE);
				nowPlayingView.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						/*
						 * lory del 2014. 7.26 Context c = v.getContext(); c.startActivity(new Intent(c,
						 * MediaPlaybackActivity.class));
						 */
						return;
					}
				});
				return;
			}
		} catch (RemoteException ex) {
		}
		nowPlayingView.setVisibility(View.GONE);
	}

	public static void setBackground(View v, Bitmap bm) {
		if (bm == null) {
			v.setBackgroundResource(0);
			return;
		}
		int vwidth = v.getWidth();
		int vheight = v.getHeight();
		int bwidth = bm.getWidth();
		int bheight = bm.getHeight();
		float scalex = (float) vwidth / bwidth;
		float scaley = (float) vheight / bheight;
		float scale = Math.max(scalex, scaley) * 1.3f;
		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		Bitmap bg = Bitmap.createBitmap(vwidth, vheight, config);
		Canvas c = new Canvas(bg);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		ColorMatrix greymatrix = new ColorMatrix();
		greymatrix.setSaturation(0);
		ColorMatrix darkmatrix = new ColorMatrix();
		darkmatrix.setScale(.3f, .3f, .3f, 1.0f);
		greymatrix.postConcat(darkmatrix);
		ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
		paint.setColorFilter(filter);
		Matrix matrix = new Matrix();
		matrix.setTranslate(-bwidth / 2, -bheight / 2); // move bitmap center to
														// origin
		matrix.postRotate(10);
		matrix.postScale(scale, scale);
		matrix.postTranslate(vwidth / 2, vheight / 2); // Move bitmap center to
														// view center
		c.drawBitmap(bm, matrix, paint);
		v.setBackgroundDrawable(new BitmapDrawable(bg));
	}

	public static int getCardId(Context context) {
		ContentResolver res = context.getContentResolver();
		Cursor c = res.query(Uri.parse("content://media/external/fs_id"), null, null, null, null);
		int id = -1;
		if (c != null) {
			c.moveToFirst();
			id = c.getInt(0);
			c.close();
		}
		return id;
	}

	public static class LogEntry {
		Object item;
		long time;

		LogEntry(Object o) {
			item = o;
			time = System.currentTimeMillis();
		}

		void dump(PrintWriter out) {
			sTime.set(time);
			out.print(sTime.toString() + " : ");
			if (item instanceof Exception) {
				((Exception) item).printStackTrace(out);
			} else {
				out.println(item);
			}
		}
	}

	private static LogEntry[] sMusicLog = new LogEntry[100];
	private static int sLogPtr = 0;
	private static Time sTime = new Time();

	static void debugLog(Object o) {
		sMusicLog[sLogPtr] = new LogEntry(o);
		sLogPtr++;
		if (sLogPtr >= sMusicLog.length) {
			sLogPtr = 0;
		}
	}

	static void debugDump(PrintWriter out) {
		for (int i = 0; i < sMusicLog.length; i++) {
			int idx = (sLogPtr + i);
			if (idx >= sMusicLog.length) {
				idx -= sMusicLog.length;
			}
			LogEntry entry = sMusicLog[idx];
			if (entry != null) {
				entry.dump(out);
			}
		}
	}

	static class RemoveTask extends AsyncTask<String, Integer, String> {
		private AuroraProgressDialog progressDialog;
		private DialogUtil.OnRemoveFileListener mListener;
		private long[] mList;
		private Context mContext;
		private int playListId;

		public RemoveTask(Context context, long[] list, DialogUtil.OnRemoveFileListener listener,int playListId) {
			mContext = context;
			mList = list;
			mListener = listener;
			this.playListId= playListId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new AuroraProgressDialog(mContext);
			progressDialog.setTitle(mContext.getResources().getString(R.string.aurora_remove));
			progressDialog.setMax(mList.length);
			progressDialog.setProgressStyle(AuroraProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					progressDialog.dismiss();
				}
			});
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
			String deleteWhere;
			for (int i = 0; i < mList.length; i++) {
				deleteWhere = MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + mList[i];
				mContext.getContentResolver().delete(uri, deleteWhere, null);
				publishProgress(i);
			}
			MusicUtils.removeTracksFromCurrentPlaylist(mContext, mList, playListId);
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			progressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			AuroraMusicUtil.showDeleteToast(mContext, mList.length, 0);
			mListener.OnRemoveFileSuccess();
		}
	}


	// add by tangjie 2014/6/17 start
	// private static byte[] mDeleteTask = new byte[0];
	static class DeleteTask extends AsyncTask<String, Integer, String> {
		private AuroraProgressDialog progressDialog;
		private DialogUtil.OnDeleteFileListener mListener;
		private long[] mList;
		private Context mContext;
		private boolean cancel;
		private int count;
		private ArrayList<AuroraListItem> mDataList;
		private ArrayList<Long> mDeletedArrayList = new ArrayList<Long>();
		private AuroraDeleteItem mItem;

		public DeleteTask(Context context, long[] list, DialogUtil.OnDeleteFileListener listener) {
			mContext = context;
			mList = list;
			mListener = listener;
			cancel = false;
		}

		public DeleteTask(Context context, AuroraDeleteItem item, DialogUtil.OnDeleteFileListener listener) {
			mContext = context;
			mItem = item;
			mList = mItem.getId();
			mListener = listener;
			cancel = false;
		}

		public DeleteTask(Context context, long[] list, DialogUtil.OnDeleteFileListener listener,
				ArrayList<AuroraListItem> datalist) {
			mContext = context;
			mList = list;
			mListener = listener;
			cancel = false;
			mDataList = datalist;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = new AuroraProgressDialog(mContext);
			progressDialog.setTitle(mContext.getResources().getString(R.string.delete_songfiles));
			progressDialog.setMax(mList.length);
			progressDialog.setProgressStyle(AuroraProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					progressDialog.dismiss();
					cancel = true;
				}
			});
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String[] cols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.ALBUM_ID };
			StringBuilder where = new StringBuilder();
			where.append(MediaStore.Audio.Media._ID + " IN (");
			for (int i = 0; i < mList.length; i++) {
				where.append(mList[i]);
				if (i < mList.length - 1) {
					where.append(",");
				}
			}
			where.append(")");
			Cursor c = query(mContext, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, where.toString(), null, null);
			String deleteWhere;
			if (c != null) {
				// step 1: remove selected tracks from the current playlist, as
				// well
				// as from the album art cache
				try {
					c.moveToFirst();
					while (!c.isAfterLast() && !cancel) {
						// remove from current playlist
						long id = c.getLong(0);
						sService.removeTrack(id);
						// remove from album art cache
						long artIndex = c.getLong(2);
						synchronized (sArtCache) {
							sArtCache.remove(artIndex);
						}
						// step 2: remove selected tracks from the database
						deleteWhere = MediaStore.Audio.Media._ID + " = " + id;
						mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, deleteWhere,
								null);
						String name = c.getString(1);
						// step 3: remove files from card
						File f = new File(name);
						try { // File.delete can throw a security exception
							if (!f.delete()) {
								// I'm not sure if we'd ever get here (deletion
								// would
								// have to fail, but no exception thrown)
								LogUtil.e(TAG, "Failed to delete file " + name);
							}
						} catch (SecurityException ex) {
							c.moveToNext();
						}
						c.moveToNext();
						mDeletedArrayList.add(id);
						publishProgress(c.getPosition());
					}
					count = c.getPosition();
				} catch (RemoteException ex) {
				} finally {// lory add
					if (c != null) {
						c.close();
						c = null;
					}
				}
			}
			// We deleted a number of tracks, which could affect any number of
			// things
			// in the media content domain, so update everything.
			mContext.getContentResolver().notifyChange(Uri.parse("content://media"), null);
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			progressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			String message = mContext.getResources().getQuantityString(R.plurals.NNNtracksdeleted, count, count);
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
			if (mItem != null) {
				for (int i = 0; i < mDeletedArrayList.size(); i++) {
					for (int j = 0; j < mList.length; j++) {
						if (mList[j] == mDeletedArrayList.get(i)) {
							DownloadManager.getInstance(mContext).removeDownloadByPath(mItem.getPath()[j]);
							break;
						}
					}
				}
			}
			if (mDataList != null) {
				for (int i = 0; i < mDeletedArrayList.size(); i++) {
					for (int j = 0; j < mDataList.size(); j++) {
						if (mDataList.get(j).getSongId() == mDeletedArrayList.get(i)) {
							DownloadManager.getInstance(mContext).removeDownloadByPath(mDataList.get(j).getFilePath());
							mDataList.remove(j);
							break;
						}
					}
				}
			}
			mListener.OnDeleteFileSuccess();
		}
	}

	// add by tangjie 2014/6/17 end
	// add by tangjie 2014/9/22 start
	public static AuroraListItem getFirstSongOfAlbum(Context context, long id, String artistId) {
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM };
		String where = MediaStore.Audio.Media.ALBUM_ID + "=" + id + " AND " + MediaStore.Audio.Media.ARTIST_ID + "="
				+ artistId;
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where, null,
				MediaStore.Audio.Media.TRACK);
		AuroraListItem item = null;
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			String imgUri = AuroraMusicUtil.getImgPath(context,
					AuroraMusicUtil.MD5(cursor.getString(1) + cursor.getString(3) + cursor.getString(4)));
			item = new AuroraListItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), "", id, "", 0,
					imgUri, "", "", 1);
		}
		if (cursor != null) {
			cursor.close();
		}
		return item;
	}

	// add end
	// add by tangjie 2014/9/23 start
	public static void goAlbumActivity(Context context, long artistId, String artistName, long albumId,
			String albumName, int type, String albumUrl) {
		Intent intent = new Intent();
		if (type == 1) {
			if (!AuroraMusicUtil.isNetWorkActive(context)) {
				Toast.makeText(context, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
				return;
			}
			intent.setClass(context, AuroraNetTrackDetailActivity.class);
			intent.putExtra("tag", 0);
			intent.putExtra("Id", String.valueOf(albumId));
			intent.putExtra("title", albumName);
			intent.putExtra("imageUrl", albumUrl);
		} else {
			intent.setClass(context, AlbumDetailActivity.class);
			AuroraAlbum album = new AuroraAlbum(albumId, albumName);
			Bundle bl = new Bundle();
			bl.putParcelable(Globals.KEY_ALBUM_ITEM, album);
			intent.putExtra(Globals.KEY_ARTIST_ID, String.valueOf(artistId));
			intent.putExtra(Globals.KEY_ARTIST_NAME, artistName);
			intent.putExtras(bl);
		}
		context.startActivity(intent);
	}
	// add by end
}
