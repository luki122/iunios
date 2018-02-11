/**  
 *   @author root (modify by hujw)
 */
package com.android.aurora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.GnIContentProvider;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaFile;
import android.media.MediaInserter;
import android.media.GnMediaInserter;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
//import libcore.io.ErrnoException;
import libcore.io.Libcore;
//import android.system.ErrnoException;

public class AuroraMediaScanner {
	private final static String TAG = "AuroraMediaScanner";

	// 加载动态库，初始化扫描器JNI环境
	static {
		System.loadLibrary("aurorascanner_jni");
		auroraNativeInit();
	}

	// 多媒体数据库Files表操作字段
	private static final String[] FILES_PRESCAN_PROJECTION = new String[] {
			Files.FileColumns._ID, // 0
			Files.FileColumns.DATA, // 1
			Files.FileColumns.FORMAT, // 2
			Files.FileColumns.DATE_MODIFIED, // 3
	};

	// 多媒体数据库Files表操作字段
	private static final String[] ID_PROJECTION = new String[] { Files.FileColumns._ID, };

	private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
	private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
	private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
	private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;

	private static final String[] PLAYLIST_MEMBERS_PROJECTION = new String[] { Audio.Playlists.Members.PLAYLIST_ID, // 0
	};

	private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
	private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
	private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;

	private static final String RINGTONES_DIR = "/ringtones/";
	private static final String NOTIFICATIONS_DIR = "/notifications/";
	private static final String ALARMS_DIR = "/alarms/";
	private static final String MUSIC_DIR = "/music/";
	private static final String PODCAST_DIR = "/podcasts/";

	private static final String[] ID3_GENRES = {
			// ID3v1 Genres
			"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk",
			"Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other",
			"Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial",
			"Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
			"Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk",
			"Fusion", "Trance", "Classical", "Instrumental", "Acid", "House",
			"Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass",
			"Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
			"Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
			"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance",
			"Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
			"Christian Rap", "Pop/Funk", "Jungle", "Native American",
			"Cabaret",
			"New Wave",
			"Psychadelic",
			"Rave",
			"Showtunes",
			"Trailer",
			"Lo-Fi",
			"Tribal",
			"Acid Punk",
			"Acid Jazz",
			"Polka",
			"Retro",
			"Musical",
			"Rock & Roll",
			"Hard Rock",
			// The following genres are Winamp extensions
			"Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion",
			"Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
			"Gothic Rock", "Progressive Rock", "Psychedelic Rock",
			"Symphonic Rock", "Slow Rock", "Big Band", "Chorus",
			"Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
			"Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass",
			"Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango",
			"Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
			"Freestyle", "Duet", "Punk Rock",
			"Drum Solo",
			"A capella",
			"Euro-House",
			"Dance Hall",
			// The following ones seem to be fairly widely supported as well
			"Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie",
			"Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta",
			"Heavy Metal", "Black Metal", "Crossover",
			"Contemporary Christian", "Christian Rock", "Merengue", "Salsa",
			"Thrash Metal", "Anime", "JPop", "Synthpop",
	// 148 and up don't seem to have been defined yet.
	};

	private int mOriginalCount;
	/** Whether the database had any entries in it before the scan started */
	private boolean mWasEmptyPriorToScan = false;
	/** Whether the scanner has set a default sound for the ringer ringtone. */
	private boolean mDefaultRingtoneSet;
	/**
	 * Whether the scanner has set a default sound for the notification
	 * ringtone.
	 */
	private boolean mDefaultNotificationSet;
	/** Whether the scanner has set a default sound for the alarm ringtone. */
	private boolean mDefaultAlarmSet;
	/** The filename for the default sound for the ringer ringtone. */
	private String mDefaultRingtoneFilename;
	/** The filename for the default sound for the notification ringtone. */
	private String mDefaultNotificationFilename;
	/** The filename for the default sound for the alarm ringtone. */
	private String mDefaultAlarmAlertFilename;
	/**
	 * The prefix for system properties that define the default sound for
	 * ringtones. Concatenate the name of the setting from Settings to get the
	 * full system property.
	 */
	private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";

	private static final boolean ENABLE_BULK_INSERTS = true;

	private DrmManagerClient mDrmManagerClient = null;

	private int mNativeContext;
	private Context mContext;
	private String mPackageName;
	private final String mExternalStoragePath;
	private final boolean mExternalIsEmulated;
	private IContentProvider mMediaProvider;
	private ArrayList<FileEntry> mPlayLists;
	private Uri mAudioUri;
	private Uri mVideoUri;
	private Uri mImagesUri;
	private Uri mThumbsUri;
	private Uri mPlaylistsUri;
	private Uri mFilesUri;
	private Uri mFilesUriNoNotify;
	private boolean mProcessPlaylists, mProcessGenres;
	private int mMtpObjectHandle;
	private MediaInserter mMediaInserter;
	private final BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
	private boolean mCaseInsensitivePaths;

	private static class PlaylistEntry {
		String path;
		long bestmatchid;
		int bestmatchlevel;
	}

	private ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList<PlaylistEntry>();

	// 类型定义
	private static class FileEntry {
		long mRowId;
		String mPath;
		long mLastModified;
		int mFormat;
		boolean mLastModifiedChanged;

		FileEntry(long rowId, String path, long lastModified, int format) {
			mRowId = rowId;
			mPath = path;
			mLastModified = lastModified;
			mFormat = format;
			mLastModifiedChanged = false;
		}

		public String toAllString() {
			return "FileEntry [mRowId=" + mRowId + ", mPath=" + mPath
					+ ", mLastModified=" + mLastModified + ", mFormat="
					+ mFormat + ", mLastModifiedChanged="
					+ mLastModifiedChanged + "]";
		}

		@Override
		public String toString() {
			return mPath + " mRowId: " + mRowId;
		}

	}

	public AuroraMediaScanner(Context c) {
		auroraNativeSetup(); // 设置JNI层扫描器
		mContext = c;
		mPackageName = c.getPackageName();
		mBitmapOptions.inSampleSize = 1;
		mBitmapOptions.inJustDecodeBounds = true;
		mExternalStoragePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		mExternalIsEmulated = Environment.isExternalStorageEmulated();
	}

	private final MyAuroraMediaScannerClient mClient = new MyAuroraMediaScannerClient();

	/**
	 * 扫描指定目录
	 * 
	 * @param directories
	 */
	public void auroraScannerDirectories(String[] directories) {
		try {
			long start = System.currentTimeMillis();
			auroraInitUri("external");
			auroraPrescan(null, true);
			long auroraPrescan = System.currentTimeMillis();

			if (ENABLE_BULK_INSERTS) {
				mMediaInserter = GnMediaInserter.GenerateMediaInserter(
						mMediaProvider, mPackageName, 500);
			}
//			AuroraLog.dLog(TAG, "dir len:" + directories.length);
			for (int i = 0; i < directories.length; i++) {
//				AuroraLog.dLog(TAG, "dir path:" + directories[i]);
				auroraProgressDirectory(directories[i], mClient); // 扫描路径入口
			}

			if (ENABLE_BULK_INSERTS) {
				mMediaInserter.flushAll();
				mMediaInserter = null;
			}

			long scan = System.currentTimeMillis();

			auroraPostScan(directories);

			long end = System.currentTimeMillis();

			if (false) {
				AuroraLog.dLog(TAG, "auroraPrescan time: "
						+ (auroraPrescan - start) + "ms\n");
				AuroraLog.dLog(TAG, "scan time: " + (scan - auroraPrescan)
						+ "ms\n");
				AuroraLog.dLog(TAG, "auroraPostScan time: " + (end - scan)
						+ "ms\n");
				AuroraLog.dLog(TAG, "total time:" + (end - start) + "ms\n");
			}
		} catch (SQLException e) {
			// this might happen if the SD card is removed while the media
			// scanner is running
			Log.e(TAG, "SQLException in AuroraMediaScanner.scan()", e);
		} catch (UnsupportedOperationException e) {
			// this might happen if the SD card is removed while the media
			// scanner is running
			Log.e(TAG,
					"UnsupportedOperationException in AuroraMediaScanner.scan()",
					e);
		} catch (RemoteException e) {
			Log.e(TAG, "RemoteException in AuroraMediaScanner.scan()", e);
		}

	}

	/**
	 * 扫描单个文件
	 * 
	 * @param path
	 *            ：文件的绝对路径
	 * @param volumeName
	 *            ：外部卷
	 * @param mimeType
	 *            ：文件类型
	 * @return ：文件的uri
	 */
	public Uri auroraScanSingleFile(String path, String volumeName,
			String mimeType) {
		try {
//			AuroraLog.dLog(TAG, "auroraScanSingleFile");
			auroraInitUri(volumeName);

			auroraPrescan(path, true);

			File file = new File(path);

			if (!file.exists()) {
				return null;
			}

			long lastModifiedSeconds = file.lastModified() / 1000;

			return mClient.auroradoScanFile(path, mimeType,
					lastModifiedSeconds, file.length(), false, true,
					AuroraMediaScanner.isNoMediaPath(path));

		} catch (RemoteException e) {
			Log.e(TAG,
					"RemoteException in AuroraMediaScanner.auroraScanFile()", e);
			return null;
		}
	}

	private boolean isDrmEnabled() {
		String prop = SystemProperties.get("drm.service.enabled");
		return prop != null && prop.equals("true");
	}

	private class MyAuroraMediaScannerClient implements
			AuroraMediaScannerClient {

		private String mArtist;
		private String mAlbumArtist; // use this if mArtist is missing
		private String mAlbum;
		private String mTitle;
		private String mComposer;
		private String mGenre;
		private String mMimeType;
		private int mFileType;
		private int mTrack;
		private int mYear;
		private int mDuration;
		private String mPath;
		private long mLastModified;
		private long mFileSize;
		private String mWriter;
		private int mCompilation;
		private boolean mIsDrm;
		private boolean mNoMedia; // flag to suppress file from appearing in
									// media tables
		private int mWidth;
		private int mHeight;

		public FileEntry beginFile(String path, String mimeType,
				long lastModified, long fileSize, boolean isDirectory,
				boolean noMedia) {
			mMimeType = mimeType;
			mFileType = 0;
			mFileSize = fileSize;
			mIsDrm = false;

			if (!isDirectory) {
				if (!noMedia && isNoMediaFile(path)) {
					noMedia = true;
				}
				mNoMedia = noMedia;

				// try mimeType first, if it is specified
				if (mimeType != null) {
					mFileType = MediaFile.getFileTypeForMimeType(mimeType);
				}

				// if mimeType was not specified, compute file type based on
				// file extension.
				if (mFileType == 0) {
					MediaFile.MediaFileType mediaFileType = MediaFile
							.getFileType(path);
					if (mediaFileType != null) {
						mFileType = mediaFileType.fileType;
						if (mMimeType == null) {
							mMimeType = mediaFileType.mimeType;
						}
					}
					// AuroraLog.dLog(TAG, "extension:[mMimeType: " + mMimeType
					// + "] [mFileType: " + mFileType + "]");
				}

				if (isDrmEnabled() && MediaFile.isDrmFileType(mFileType)) {
					mFileType = getFileTypeFromDrm(path);
					// AuroraLog.dLog(TAG, "getFileTypeFromDrm: mFileType:" +
					// mFileType);
				}
			}

			FileEntry entry = makeEntryFor(path);
			// add some slack to avoid a rounding error
			long delta = (entry != null) ? (lastModified - entry.mLastModified)
					: 0;
			boolean wasModified = delta > 1 || delta < -1;

			if (entry == null || wasModified) {
				if (wasModified) {
					entry.mLastModified = lastModified;
				} else {
					entry = new FileEntry(0, path, lastModified,
							(isDirectory ? MtpConstants.FORMAT_ASSOCIATION : 0));
				}
				entry.mLastModifiedChanged = true;
			}

			if (mProcessPlaylists && MediaFile.isPlayListFileType(mFileType)) {
				mPlayLists.add(entry);
				// we don't process playlists in the main scan, so return null
				return null;
			}

			// clear all the metadata
			mArtist = null;
			mAlbumArtist = null;
			mAlbum = null;
			mTitle = null;
			mComposer = null;
			mGenre = null;
			mTrack = 0;
			mYear = 0;
			mDuration = 0;
			mPath = path;
			mLastModified = lastModified;
			mWriter = null;
			mCompilation = 0;
			mWidth = 0;
			mHeight = 0;

			return entry;
		}

		@Override
		public void auroraScanFile(String path, long lastModified,
				long fileSize, boolean isDirectory, boolean noMedia) {

			auroradoScanFile(path, null, lastModified, fileSize, isDirectory,
					false, noMedia);
		}

		/**
		 * 扫描指定文件的多媒体信息
		 * 
		 * @param path
		 * @param mimeType
		 * @param lastModified
		 * @param fileSize
		 * @param isDirectory
		 * @param scanAlways
		 * @param noMedia
		 * @return
		 */
		public Uri auroradoScanFile(String path, String mimeType,
				long lastModified, long fileSize, boolean isDirectory,
				boolean scanAlways, boolean noMedia) {
			Uri result = null;

			try {
				// AuroraLog.dLog(TAG, "auroradoScanFile path:" + path);
				FileEntry entry = beginFile(path, mimeType, lastModified,
						fileSize, isDirectory, noMedia);
//				Log.d("JXH", "entry:" + entry.toAllString());
				// if this file was just inserted via mtp, set the rowid to zero
				// (even though it already exists in the database), to trigger
				// the correct code path for updating its entry
				if (mMtpObjectHandle != 0) {
					entry.mRowId = 0;
				}

				// rescan for metadata if file was modified since last scan
				if (entry != null && (entry.mLastModifiedChanged || scanAlways)) {

					if (noMedia) {
						result = endFile(entry, false, false, false, false,
								false);
					} else {
						String lowpath = path.toLowerCase();
						boolean ringtones = (lowpath.indexOf(RINGTONES_DIR) > 0);
						boolean notifications = (lowpath
								.indexOf(NOTIFICATIONS_DIR) > 0);
						boolean alarms = (lowpath.indexOf(ALARMS_DIR) > 0);
						boolean podcasts = (lowpath.indexOf(PODCAST_DIR) > 0);
						boolean music = (lowpath.indexOf(MUSIC_DIR) > 0)
								|| (!ringtones && !notifications && !alarms && !podcasts);

						boolean isaudio = MediaFile.isAudioFileType(mFileType);
						boolean isvideo = MediaFile.isVideoFileType(mFileType);
						boolean isimage = MediaFile.isImageFileType(mFileType);

						if (isaudio || isvideo || isimage) {
							if (mExternalIsEmulated
									&& path.startsWith(mExternalStoragePath)) {

								String directPath = Environment
										.getMediaStorageDirectory()
										+ path.substring(mExternalStoragePath
												.length());
								File f = new File(directPath);
								if (f.exists()) {
									path = directPath;
								}
							}
						}

						// 只有视频跟音频文件才获取它的元数据
						if (isaudio || isvideo) {

							// 真正扫描文件
							// AuroraLog.vLog(TAG, " start auroraProgressFile");
							auroraProgressFile(path, mimeType, this);
						}

						if (isimage) {
							// AuroraLog.vLog(TAG, " start processImageFile");
							processImageFile(path);
						}

						// 多媒体文件数据写入到数据库
						result = endFile(entry, ringtones, notifications,
								alarms, music, podcasts);
						// AuroraLog.vLog(TAG, "endFile");
					}
				}
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException in AuroraMediaScanner.scanFile()",
						e);
			}

			return result;
		}

		private int parseSubstring(String s, int start, int defaultValue) {
			int length = s.length();
			if (start == length)
				return defaultValue;

			char ch = s.charAt(start++);
			// return defaultValue if we have no integer at all
			if (ch < '0' || ch > '9')
				return defaultValue;

			int result = ch - '0';
			while (start < length) {
				ch = s.charAt(start++);
				if (ch < '0' || ch > '9')
					return result;
				result = result * 10 + (ch - '0');
			}

			return result;
		}

		/**
		 * 判断字符是否是中文
		 * 
		 * @param c
		 *            字符
		 * @return 是否是中文
		 */
		public boolean isChinese(char c) {
			Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
			if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
					|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
					|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
					|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
					|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
				return true;
			}
			return false;
		}

		/**
		 * 判断字符串是否是乱码
		 * 
		 * @param strName
		 *            字符串
		 * @return 是否是乱码
		 */
		public boolean isMessyCode(String strName) {
			Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
			Matcher m = p.matcher(strName);
			String after = m.replaceAll("");
			String temp = after.replaceAll("\\p{P}", "");
			char[] ch = temp.trim().toCharArray();
			float chLength = ch.length;
			float count = 0;
			for (int i = 0; i < ch.length; i++) {
				char c = ch[i];
				if (!Character.isLetterOrDigit(c)) {
					if (!isChinese(c)) {
						count = count + 1;
					}
				}
			}
			float result = count / chLength;
			if (result > 0.4) {
				return true;
			} else {
				return false;
			}

		}

		public void auroraHandleStringTag(String name, String value) {
			// AuroraLog.dLog(TAG, "name==" + name + " value==" + value);
			if (isMessyCode(value)) {
				value = "Unknown";
				// AuroraLog.dLog(TAG, "name==" + name + " set value==" +
				// value);
			}
			if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
				// Don't trim() here, to preserve the special \001 character
				// used to force sorting. The media provider will trim() before
				// inserting the title in to the database.
				mTitle = value;
			} else if (name.equalsIgnoreCase("artist")
					|| name.startsWith("artist;")) {
				mArtist = value.trim();

			} else if (name.equalsIgnoreCase("albumartist")
					|| name.startsWith("albumartist;")
					|| name.equalsIgnoreCase("band")
					|| name.startsWith("band;")) {
				mAlbumArtist = value.trim();
			} else if (name.equalsIgnoreCase("album")
					|| name.startsWith("album;")) {
				mAlbum = value.trim();
			} else if (name.equalsIgnoreCase("composer")
					|| name.startsWith("composer;")) {
				mComposer = value.trim();
			} else if (mProcessGenres
					&& (name.equalsIgnoreCase("genre") || name
							.startsWith("genre;"))) {
				mGenre = getGenreName(value);
			} else if (name.equalsIgnoreCase("year")
					|| name.startsWith("year;")) {
				mYear = parseSubstring(value, 0, 0);
			} else if (name.equalsIgnoreCase("tracknumber")
					|| name.startsWith("tracknumber;")) {
				// track number might be of the form "2/12"
				// we just read the number before the slash
				int num = parseSubstring(value, 0, 0);
				mTrack = (mTrack / 1000) * 1000 + num;
			} else if (name.equalsIgnoreCase("discnumber")
					|| name.equals("set") || name.startsWith("set;")) {
				// set number might be of the form "1/3"
				// we just read the number before the slash
				int num = parseSubstring(value, 0, 0);
				mTrack = (num * 1000) + (mTrack % 1000);
			} else if (name.equalsIgnoreCase("duration")) {
				mDuration = parseSubstring(value, 0, 0);
			} else if (name.equalsIgnoreCase("writer")
					|| name.startsWith("writer;")) {
				mWriter = value.trim();
			} else if (name.equalsIgnoreCase(Audio.Media.COMPILATION)) {
				mCompilation = parseSubstring(value, 0, 0);
			} else if (name.equalsIgnoreCase("isdrm")) {
				mIsDrm = (parseSubstring(value, 0, 0) == 1);
			} else if (name.equalsIgnoreCase("width")) {
				mWidth = parseSubstring(value, 0, 0);
			} else if (name.equalsIgnoreCase("height")) {
				mHeight = parseSubstring(value, 0, 0);
			} else {

			}
			// AuroraLog.vLog(TAG, "handleStringTag >>name: " + name + " value:"
			// + value);
		}

		private boolean convertGenreCode(String input, String expected) {
			String output = getGenreName(input);
			if (output.equals(expected)) {
				return true;
			} else {
				AuroraLog.dLog(TAG, "'" + input + "' -> '" + output
						+ "', expected '" + expected + "'");
				return false;
			}
		}

		private void testGenreNameConverter() {
			convertGenreCode("2", "Country");
			convertGenreCode("(2)", "Country");
			convertGenreCode("(2", "(2");
			convertGenreCode("2 Foo", "Country");
			convertGenreCode("(2) Foo", "Country");
			convertGenreCode("(2 Foo", "(2 Foo");
			convertGenreCode("2Foo", "2Foo");
			convertGenreCode("(2)Foo", "Country");
			convertGenreCode("200 Foo", "Foo");
			convertGenreCode("(200) Foo", "Foo");
			convertGenreCode("200Foo", "200Foo");
			convertGenreCode("(200)Foo", "Foo");
			convertGenreCode("200)Foo", "200)Foo");
			convertGenreCode("200) Foo", "200) Foo");
		}

		public String getGenreName(String genreTagValue) {

			if (genreTagValue == null) {
				return null;
			}
			final int length = genreTagValue.length();

			if (length > 0) {
				boolean parenthesized = false;
				StringBuffer number = new StringBuffer();
				int i = 0;
				for (; i < length; ++i) {
					char c = genreTagValue.charAt(i);
					if (i == 0 && c == '(') {
						parenthesized = true;
					} else if (Character.isDigit(c)) {
						number.append(c);
					} else {
						break;
					}
				}
				char charAfterNumber = i < length ? genreTagValue.charAt(i)
						: ' ';
				if ((parenthesized && charAfterNumber == ')') || !parenthesized
						&& Character.isWhitespace(charAfterNumber)) {
					try {
						short genreIndex = Short.parseShort(number.toString());
						if (genreIndex >= 0) {
							if (genreIndex < ID3_GENRES.length
									&& ID3_GENRES[genreIndex] != null) {
								return ID3_GENRES[genreIndex];
							} else if (genreIndex == 0xFF) {
								return null;
							} else if (genreIndex < 0xFF && (i + 1) < length) {
								// genre is valid but unknown,
								// if there is a string after the value we take
								// it
								if (parenthesized && charAfterNumber == ')') {
									i++;
								}
								String ret = genreTagValue.substring(i).trim();
								if (ret.length() != 0) {
									return ret;
								}
							} else {
								// else return the number, without parentheses
								return number.toString();
							}
						}
					} catch (NumberFormatException e) {
					}
				}
			}

			return genreTagValue;
		}

		private void processImageFile(String path) {
			try {
				mBitmapOptions.outWidth = 0;
				mBitmapOptions.outHeight = 0;
				BitmapFactory.decodeFile(path, mBitmapOptions);
				mWidth = mBitmapOptions.outWidth;
				mHeight = mBitmapOptions.outHeight;
			} catch (Throwable th) {
				// ignore;
			}
		}

		public void auroraSetMimeType(String mimeType) {
			// AuroraLog.dLog(TAG,"++mimeType:" + mimeType);
			if ("audio/mp4".equals(mMimeType) && mimeType.startsWith("video")) {
				// for feature parity with Donut, we force m4a files to keep the
				// audio/mp4 mimetype, even if they are really
				// "enhanced podcasts"
				// with a video track
				return;
			}
			// Aurora <hujianwei> <2014-04-23> added for media file mimetype
			// check begin
			if (auroraMimeTypeCheck(mimeType)) {
				return;
			}
			// Aurora <hujianwei> <2014-04-23> added for media file mimetype
			// check end

			mMimeType = mimeType;
			mFileType = MediaFile.getFileTypeForMimeType(mimeType);
		}

		/**
		 * Formats the data into a values array suitable for use with the Media
		 * Content Provider.
		 * 
		 * @return a map of values
		 */
		private ContentValues toValues() {
			ContentValues map = new ContentValues();

			map.put(MediaStore.MediaColumns.DATA, mPath);
			map.put(MediaStore.MediaColumns.TITLE, mTitle);
			map.put(MediaStore.MediaColumns.DATE_MODIFIED, mLastModified);
			map.put(MediaStore.MediaColumns.SIZE, mFileSize);
			map.put(MediaStore.MediaColumns.MIME_TYPE, mMimeType);
			map.put(MediaStore.MediaColumns.IS_DRM, mIsDrm);

			String resolution = null;
			if (mWidth > 0 && mHeight > 0) {
				map.put(MediaStore.MediaColumns.WIDTH, mWidth);
				map.put(MediaStore.MediaColumns.HEIGHT, mHeight);
				resolution = mWidth + "x" + mHeight;
			}

			if (!mNoMedia) {
				if (MediaFile.isVideoFileType(mFileType)) {
					map.put(Video.Media.ARTIST,
							(mArtist != null && mArtist.length() > 0 ? mArtist
									: MediaStore.UNKNOWN_STRING));
					map.put(Video.Media.ALBUM,
							(mAlbum != null && mAlbum.length() > 0 ? mAlbum
									: MediaStore.UNKNOWN_STRING));
					map.put(Video.Media.DURATION, mDuration);
					if (resolution != null) {
						map.put(Video.Media.RESOLUTION, resolution);
					}
				} else if (MediaFile.isImageFileType(mFileType)) {
					// FIXME - add DESCRIPTION
				} else if (MediaFile.isAudioFileType(mFileType)) {
					map.put(Audio.Media.ARTIST, (mArtist != null && mArtist
							.length() > 0) ? mArtist
							: MediaStore.UNKNOWN_STRING);
					map.put(Audio.Media.ALBUM_ARTIST,
							(mAlbumArtist != null && mAlbumArtist.length() > 0) ? mAlbumArtist
									: null);
					map.put(Audio.Media.ALBUM, (mAlbum != null && mAlbum
							.length() > 0) ? mAlbum : MediaStore.UNKNOWN_STRING);
					map.put(Audio.Media.COMPOSER, mComposer);
					map.put("genre", mGenre);
					if (mYear != 0) {
						map.put(Audio.Media.YEAR, mYear);
					}
					map.put(Audio.Media.TRACK, mTrack);
					map.put(Audio.Media.DURATION, mDuration);
					map.put(Audio.Media.COMPILATION, mCompilation);
				}
			}
			return map;
		}

		private Uri endFile(FileEntry entry, boolean ringtones,
				boolean notifications, boolean alarms, boolean music,
				boolean podcasts) throws RemoteException {

			if (mArtist == null || mArtist.length() == 0) {
				mArtist = mAlbumArtist;
			}

			ContentValues values = toValues();
			String title = values.getAsString(MediaStore.MediaColumns.TITLE);
			if (title == null || TextUtils.isEmpty(title.trim())) {
				title = MediaFile.getFileTitle(values
						.getAsString(MediaStore.MediaColumns.DATA));
				values.put(MediaStore.MediaColumns.TITLE, title);
			}
			String album = values.getAsString(Audio.Media.ALBUM);
			if (MediaStore.UNKNOWN_STRING.equals(album)) {
				album = values.getAsString(MediaStore.MediaColumns.DATA);
				// extract last path segment before file name
				int lastSlash = album.lastIndexOf('/');
				if (lastSlash >= 0) {
					int previousSlash = 0;
					while (true) {
						int idx = album.indexOf('/', previousSlash + 1);
						if (idx < 0 || idx >= lastSlash) {
							break;
						}
						previousSlash = idx;
					}
					if (previousSlash != 0) {
						album = album.substring(previousSlash + 1, lastSlash);
						values.put(Audio.Media.ALBUM, album);
					}
				}
			}
			long rowId = entry.mRowId;
			if (MediaFile.isAudioFileType(mFileType)
					&& (rowId == 0 || mMtpObjectHandle != 0)) {
				// Only set these for new entries. For existing entries, they
				// may have been modified later, and we want to keep the current
				// values so that custom ringtones still show up in the ringtone
				// picker.
				values.put(Audio.Media.IS_RINGTONE, ringtones);
				values.put(Audio.Media.IS_NOTIFICATION, notifications);
				values.put(Audio.Media.IS_ALARM, alarms);
				values.put(Audio.Media.IS_MUSIC, music);
				values.put(Audio.Media.IS_PODCAST, podcasts);
			} else if (mFileType == MediaFile.FILE_TYPE_JPEG && !mNoMedia) {
				ExifInterface exif = null;
				try {
					exif = new ExifInterface(entry.mPath);
				} catch (IOException ex) {
					// exif is null
				}
				if (exif != null) {
					float[] latlng = new float[2];
					if (exif.getLatLong(latlng)) {
						values.put(Images.Media.LATITUDE, latlng[0]);
						values.put(Images.Media.LONGITUDE, latlng[1]);
					}

					long time = exif.getGpsDateTime();
					if (time != -1) {
						values.put(Images.Media.DATE_TAKEN, time);
					} else {
						// If no time zone information is available, we should
						// consider using
						// EXIF local time as taken time if the difference
						// between file time
						// and EXIF local time is not less than 1 Day, otherwise
						// MediaProvider
						// will use file time as taken time.
						time = exif.getDateTime();
						if (time != -1
								&& Math.abs(mLastModified * 1000 - time) >= 86400000) {
							values.put(Images.Media.DATE_TAKEN, time);
						}
					}

					int orientation = exif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION, -1);
					if (orientation != -1) {
						// We only recognize a subset of orientation tag values.
						int degree;
						switch (orientation) {
						case ExifInterface.ORIENTATION_ROTATE_90:
							degree = 90;
							break;
						case ExifInterface.ORIENTATION_ROTATE_180:
							degree = 180;
							break;
						case ExifInterface.ORIENTATION_ROTATE_270:
							degree = 270;
							break;
						default:
							degree = 0;
							break;
						}
						values.put(Images.Media.ORIENTATION, degree);
					}
				}
			}
			Uri tableUri = mFilesUri;
			MediaInserter inserter = mMediaInserter;
			if (!mNoMedia) {
				if (MediaFile.isVideoFileType(mFileType)) {
					tableUri = mVideoUri;
				} else if (MediaFile.isImageFileType(mFileType)) {
					tableUri = mImagesUri;
				} else if (MediaFile.isAudioFileType(mFileType)) {
					tableUri = mAudioUri;
				}
			}
			Uri result = null;
			boolean needToSetSettings = false;
			if (rowId == 0) {
				if (mMtpObjectHandle != 0) {
					values.put("media_scanner_new_object_id", mMtpObjectHandle);
				}
				if (tableUri == mFilesUri) {
					int format = entry.mFormat;
					if (format == 0) {
						format = MediaFile
								.getFormatCode(entry.mPath, mMimeType);
					}
					values.put(Files.FileColumns.FORMAT, format);
				}
				// Setting a flag in order not to use bulk insert for the file
				// related with
				// notifications, ringtones, and alarms, because the rowId of
				// the inserted file is
				// needed.

				if (mWasEmptyPriorToScan) {
					if (notifications && !mDefaultNotificationSet) {
						if (TextUtils.isEmpty(mDefaultNotificationFilename)
								|| doesPathHaveFilename(entry.mPath,
										mDefaultNotificationFilename)) {
							needToSetSettings = true;
						}
					} else if (ringtones && !mDefaultRingtoneSet) {
						if (TextUtils.isEmpty(mDefaultRingtoneFilename)
								|| doesPathHaveFilename(entry.mPath,
										mDefaultRingtoneFilename)) {
							needToSetSettings = true;
						}
					} else if (alarms && !mDefaultAlarmSet) {
						if (TextUtils.isEmpty(mDefaultAlarmAlertFilename)
								|| doesPathHaveFilename(entry.mPath,
										mDefaultAlarmAlertFilename)) {
							needToSetSettings = true;
						}
					}
				}

				// New file, insert it.
				// Directories need to be inserted before the files they
				// contain, so they
				// get priority when bulk inserting.
				// If the rowId of the inserted file is needed, it gets inserted
				// immediately,
				// bypassing the bulk inserter.

				// AuroraLog.vLog(TAG, "insertFile Progress");

				if (inserter == null || needToSetSettings) {
					if (inserter != null) {
						inserter.flushAll();
					}
					// modify aurora_jianweihu 2014-05-12
					// result = mMediaProvider.insert(mPackageName, tableUri,
					// values);
					result = GnIContentProvider.insert(mMediaProvider,
							mPackageName, tableUri, values);
					// modify aurora_jianweihu 2014-05-12
				} else if (entry.mFormat == MtpConstants.FORMAT_ASSOCIATION) {
					inserter.insertwithPriority(tableUri, values);
				} else {
					inserter.insert(tableUri, values);
				}

				if (result != null) {
					rowId = ContentUris.parseId(result);
					entry.mRowId = rowId;
				}
			} else {
				// updated file
				result = ContentUris.withAppendedId(tableUri, rowId);
				// path should never change, and we want to avoid replacing
				// mixed cased paths
				// with squashed lower case paths
				values.remove(MediaStore.MediaColumns.DATA);
				// AuroraLog.dLog(TAG, "updateFile Progress");
				int mediaType = 0;
				if (!AuroraMediaScanner.isNoMediaPath(entry.mPath)) {
					int fileType = MediaFile.getFileTypeForMimeType(mMimeType);

					if (MediaFile.isAudioFileType(fileType)) {
						mediaType = FileColumns.MEDIA_TYPE_AUDIO; // 2
					} else if (MediaFile.isVideoFileType(fileType)) {
						mediaType = FileColumns.MEDIA_TYPE_VIDEO; // 3
					} else if (MediaFile.isImageFileType(fileType)) {
						mediaType = FileColumns.MEDIA_TYPE_IMAGE;
					} else if (MediaFile.isPlayListFileType(fileType)) {
						mediaType = FileColumns.MEDIA_TYPE_PLAYLIST;
					}

					values.put(FileColumns.MEDIA_TYPE, mediaType);
				}
				// modify aurora_jianweihu 2014-05-12
				// mMediaProvider.update(mPackageName, result, values, null,
				// null);

				GnIContentProvider.update(mMediaProvider, mPackageName, result,
						values, null, null);
				// modify aurora_jianweihu 2014-05-12
			}

			if (needToSetSettings) {
				if (notifications) {
					setSettingIfNotSet(Settings.System.NOTIFICATION_SOUND,
							tableUri, rowId);
					mDefaultNotificationSet = true;
				} else if (ringtones) {
					setSettingIfNotSet(Settings.System.RINGTONE, tableUri,
							rowId);
					mDefaultRingtoneSet = true;
				} else if (alarms) {
					setSettingIfNotSet(Settings.System.ALARM_ALERT, tableUri,
							rowId);
					mDefaultAlarmSet = true;
				}
			}

			return result;
		}

		private boolean doesPathHaveFilename(String path, String filename) {
			int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
			int filenameLength = filename.length();
			return path.regionMatches(pathFilenameStart, filename, 0,
					filenameLength)
					&& pathFilenameStart + filenameLength == path.length();
		}

		private void setSettingIfNotSet(String settingName, Uri uri, long rowId) {

			String existingSettingValue = Settings.System.getString(
					mContext.getContentResolver(), settingName);

			if (TextUtils.isEmpty(existingSettingValue)) {
				// Set the setting to the given URI
				Settings.System.putString(mContext.getContentResolver(),
						settingName, ContentUris.withAppendedId(uri, rowId)
								.toString());
			}
		}

		private int getFileTypeFromDrm(String path) {
			if (!isDrmEnabled()) {
				return 0;
			}

			int resultFileType = 0;

			if (mDrmManagerClient == null) {
				mDrmManagerClient = new DrmManagerClient(mContext);
			}

			if (mDrmManagerClient.canHandle(path, null)) {
				mIsDrm = true;
				String drmMimetype = mDrmManagerClient
						.getOriginalMimeType(path);
				if (drmMimetype != null) {
					mMimeType = drmMimetype;
					resultFileType = MediaFile
							.getFileTypeForMimeType(drmMimetype);
				}
			}
			return resultFileType;
		}

		/**
		 * @author :Aurora <hujianwei>
		 * @param :mimetype
		 * @Description :if file is the special mimetype file return true else
		 *              return false
		 * @time :<2014-04-23>
		 */
		private boolean auroraMimeTypeCheck(String mimeType) {

			// the .mpg format file
			if ("video/mp2p".equals(mMimeType) && mimeType.startsWith("audio")) {
				return true;
			}

			// the .wmv format file
			if ("video/x-ms-wmv".equals(mMimeType)
					&& mimeType.startsWith("audio")) {
				return true;
			}

			return false;

		}

	}; // end of anonymous MediaScannerClient instance

	/**
	 * 扫描预处理
	 * 
	 * @param filePath
	 * @param prescanFiles
	 * @throws RemoteException
	 */
	private void auroraPrescan(String filePath, boolean prescanFiles)
			throws RemoteException {
		Cursor c = null;
		String where = null;
		String[] selectionArgs = null;

		if (mPlayLists == null) {
			mPlayLists = new ArrayList<FileEntry>();
		} else {
			mPlayLists.clear();
		}
		if (filePath != null) {
			// query for only one file
			where = MediaStore.Files.FileColumns._ID + ">?" + " AND "
					+ Files.FileColumns.DATA + "=?";
			selectionArgs = new String[] { "", filePath };
		} else {
			where = MediaStore.Files.FileColumns._ID + ">?";
			selectionArgs = new String[] { "" };
		}

		// Tell the provider to not delete the file.
		// If the file is truly gone the delete is unnecessary, and we want to
		// avoid
		// accidentally deleting files that are really there (this may happen if
		// the
		// filesystem is mounted and unmounted while the scanner is running).
		Uri.Builder builder = mFilesUri.buildUpon();
		builder.appendQueryParameter(MediaStore.PARAM_DELETE_DATA, "false");
		MediaBulkDeleter deleter = new MediaBulkDeleter(mMediaProvider,
				mPackageName, builder.build());

		// Build the list of files from the content provider
		try {
			if (prescanFiles) {
				// First read existing files from the files table.
				// Because we'll be deleting entries for missing files as we go,
				// we need to query the database in small batches, to avoid
				// problems
				// with CursorWindow positioning.
				long lastId = Long.MIN_VALUE;
				Uri limitUri = mFilesUri.buildUpon()
						.appendQueryParameter("limit", "1000").build();
				mWasEmptyPriorToScan = true;

				while (true) {
					selectionArgs[0] = "" + lastId;
					if (c != null) {
						c.close();
						c = null;
					}

					// modify aurora_jianweihu 2014-05-12
					// c = mMediaProvider.query(mPackageName, limitUri,
					// FILES_PRESCAN_PROJECTION,
					// where, selectionArgs, MediaStore.Files.FileColumns._ID,
					// null);

					c = GnIContentProvider.query(mMediaProvider, mPackageName,
							limitUri, FILES_PRESCAN_PROJECTION, where,
							selectionArgs, MediaStore.Files.FileColumns._ID,
							null);

					// modify aurora_jianweihu 2014-05-12
					if (c == null) {
						break;
					}

					int num = c.getCount();

					if (num == 0) {
						break;
					}
					mWasEmptyPriorToScan = false;
					while (c.moveToNext()) {
						long rowId = c.getLong(FILES_PRESCAN_ID_COLUMN_INDEX);
						String path = c
								.getString(FILES_PRESCAN_PATH_COLUMN_INDEX);
						int format = c
								.getInt(FILES_PRESCAN_FORMAT_COLUMN_INDEX);
						long lastModified = c
								.getLong(FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX);
						lastId = rowId;

						// Only consider entries with absolute path names.
						// This allows storing URIs in the database without the
						// media scanner removing them.
						if (path != null && path.startsWith("/")) {
							boolean exists = false;
							File file = new File(path);
							exists = file.exists();
							/*try {
								exists = Libcore.os.access(path,
										libcore.io.OsConstants.F_OK);
							} catch (ErrnoException e1) {
							}*/

							if (!exists
									&& !MtpConstants.isAbstractObject(format)) {
								// do not delete missing playlists, since they
								// may have been
								// modified by the user.
								// The user can delete them in the media player
								// instead.
								// instead, clear the path and lastModified
								// fields in the row
								MediaFile.MediaFileType mediaFileType = MediaFile
										.getFileType(path);
								int fileType = (mediaFileType == null ? 0
										: mediaFileType.fileType);

								if (!MediaFile.isPlayListFileType(fileType)) {
									// add by aurora.jiangmx
									// AuroraLog.vLog(TAG,
									// "aurora.jiangmx scan delete path: "
									// + path + " with rowId: "
									// + rowId + " ??????");

									deleter.delete(rowId);
									if (path.toLowerCase(Locale.US).endsWith(
											"/.nomedia")) {
										deleter.flush();
										String parent = new File(path)
												.getParent();
										// mMediaProvider.call(mPackageName,
										// MediaStore.UNHIDE_CALL,
										// parent, null);
										// modify aurora_jianweihu 2014-05-12
										GnIContentProvider.call(mMediaProvider,
												mPackageName,
												MediaStore.UNHIDE_CALL, parent,
												null);
										// modify aurora_jianweihu 2014-05-12
									}
								}
							}
						}
					}
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
			deleter.flush();
		}

		// compute original size of images
		mOriginalCount = 0;
		// modify aurora_jianweihu 2014-05-12
		// c = mMediaProvider.query(mPackageName, mImagesUri, ID_PROJECTION,
		// null, null, null, null);

		c = GnIContentProvider.query(mMediaProvider, mPackageName, mImagesUri,
				ID_PROJECTION, null, null, null, null);
		// modify aurora_jianweihu 2014-05-12
		if (c != null) {
			mOriginalCount = c.getCount();
			c.close();
		}
	}

	/**
	 * 判断是否不是多媒体文件
	 * 
	 * @param path
	 * @return true不是多媒体文件 false是多媒体文件
	 */
	private static boolean isNoMediaFile(String path) {

		File file = new File(path);
		if (file.isDirectory())
			return false;

		// special case certain file names
		// I use regionMatches() instead of substring() below
		// to avoid memory allocation
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
			// ignore those ._* files created by MacOS
			if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
				return true;
			}

			// ignore album art files created by Windows Media Player:
			// Folder.jpg, AlbumArtSmall.jpg, AlbumArt_{...}_Large.jpg
			// and AlbumArt_{...}_Small.jpg
			if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
				if (path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10)
						|| path.regionMatches(true, lastSlash + 1, "AlbumArt.",
								0, 9)) {
					return true;
				}
				int length = path.length() - lastSlash - 1;
				if ((length == 17 && path.regionMatches(true, lastSlash + 1,
						"AlbumArtSmall", 0, 13))
						|| (length == 10 && path.regionMatches(true,
								lastSlash + 1, "Folder", 0, 6))) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isNoMediaPath(String path) {
		if (path == null)
			return false;

		// “/.”包含这两个字符的路径不是多媒体文件
		if (path.indexOf("/.") >= 0)
			return true;

		// 查询任何父级目录中是否包含.nomedia文件，跳过root根目录。
		int offset = 1;
		while (offset >= 0) {
			int slashIndex = path.indexOf('/', offset);
			if (slashIndex > offset) {
				slashIndex++;
				File file = new File(path.substring(0, slashIndex) + ".nomedia");
				if (file.exists()) {
					// 在父级及以上的目录中包含.nomedia的不是多媒体文件
					return true;
				}
			}
			offset = slashIndex;
		}
		return isNoMediaFile(path);
	}

	FileEntry makeEntryFor(String path) {
		String where;
		String[] selectionArgs;

		Cursor c = null;
		try {
			where = Files.FileColumns.DATA + "=?";
			selectionArgs = new String[] { path };
			// modify aurora_jianweihu 2014-05-12
			/* c = mMediaProvider.query(mPackageName, mFilesUriNoNotify,
			 FILES_PRESCAN_PROJECTION,
			 where, selectionArgs, null, null);*/
			c = GnIContentProvider.query(mMediaProvider, mPackageName,
					mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, where,
					selectionArgs, null, null);
			// modify aurora_jianweihu 2014-05-12

			if (c.moveToFirst()) {
				long rowId = c.getLong(FILES_PRESCAN_ID_COLUMN_INDEX);
				int format = c.getInt(FILES_PRESCAN_FORMAT_COLUMN_INDEX);
				long lastModified = c
						.getLong(FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX);
				return new FileEntry(rowId, path, lastModified, format);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	static class MediaBulkDeleter {
		StringBuilder whereClause = new StringBuilder();
		ArrayList<String> whereArgs = new ArrayList<String>(100);
		final IContentProvider mProvider;
		final String mPackageName;
		final Uri mBaseUri;

		public MediaBulkDeleter(IContentProvider provider, String packageName,
				Uri baseUri) {
			mProvider = provider;
			mPackageName = packageName;
			mBaseUri = baseUri;
		}

		public void delete(long id) throws RemoteException {
			if (whereClause.length() != 0) {
				whereClause.append(",");
			}
			whereClause.append("?");
			whereArgs.add("" + id);
			if (whereArgs.size() > 100) {
				flush();
			}
		}

		public void flush() throws RemoteException {
			int size = whereArgs.size();
			if (size > 0) {
				String[] foo = new String[size];
				foo = whereArgs.toArray(foo);
				// modify by aurora_jianweihu 2014-05-13
				/*
				 * int numrows = mProvider.delete(mPackageName, mBaseUri,
				 * MediaStore.MediaColumns._ID + " IN (" +
				 * whereClause.toString() + ")", foo);
				 */
				int numrows = GnIContentProvider.delete(mProvider,
						mPackageName, mBaseUri, MediaStore.MediaColumns._ID
								+ " IN (" + whereClause.toString() + ")", foo);

				// modify by aurora_jianweihu 2014-05-13

				whereClause.setLength(0);
				whereArgs.clear();
			}
		}
	}

	private void auroraPostScan(String[] directories) throws RemoteException {

		// 更新播放列表这样才知道有什么多媒体文件存储在设备上
		if (mProcessPlaylists) {

			processPlayLists();
		}

		if (mOriginalCount == 0
				&& mImagesUri.equals(Images.Media.getContentUri("external")))

			// 生成缩略图
			pruneDeadThumbnailFiles();

		// 解除引用，GC回收
		mPlayLists = null;
		mMediaProvider = null;
	}

	private void auroraInitUri(String volumeName) {

		// 获取一个mMediaProvider实例
		mMediaProvider = mContext.getContentResolver().acquireProvider("media");

		mAudioUri = Audio.Media.getContentUri(volumeName);
		mVideoUri = Video.Media.getContentUri(volumeName);
		mImagesUri = Images.Media.getContentUri(volumeName);
		mThumbsUri = Images.Thumbnails.getContentUri(volumeName);
		mFilesUri = Files.getContentUri(volumeName);
		mFilesUriNoNotify = mFilesUri.buildUpon()
				.appendQueryParameter("nonotify", "1").build();

		// 只有在外部存储卷上才支持播放列表
		if (!volumeName.equals("internal")) {

			mProcessPlaylists = true;
			mProcessGenres = true;
			mPlaylistsUri = Playlists.getContentUri(volumeName);
			mCaseInsensitivePaths = true;
		}
	}

	private void processPlayLists() throws RemoteException {
		Iterator<FileEntry> iterator = mPlayLists.iterator();
		Cursor fileList = null;
		try {
			// 查询在Files表中的多媒体文件
			// modify aurora_jianweihu 2014-05-12
			// fileList = mMediaProvider.query(mPackageName, mFilesUri,
			// FILES_PRESCAN_PROJECTION, "media_type=2", null, null, null);

			fileList = GnIContentProvider.query(mMediaProvider, mPackageName,
					mFilesUri, FILES_PRESCAN_PROJECTION, "media_type=2", null,
					null, null);
			// modify aurora_jianweihu 2014-05-12
			while (iterator.hasNext()) {

				FileEntry entry = iterator.next();

				// 只处理新增的或者是在最后浏览之后被修改过的播放列表文件
				if (entry.mLastModifiedChanged) {
					processPlayList(entry, fileList);
				}
			}
		} catch (RemoteException e1) {
		} finally {
			if (fileList != null) {
				fileList.close();
			}
		}
	}

	private void pruneDeadThumbnailFiles() {
		HashSet<String> existingFiles = new HashSet<String>();
		String directory = "/sdcard/DCIM/.thumbnails";
		String[] files = (new File(directory)).list();
		if (files == null)
			files = new String[0];

		for (int i = 0; i < files.length; i++) {
			String fullPathString = directory + "/" + files[i];
			existingFiles.add(fullPathString);
		}

		try {
			// modify by aurora_jianweihu 2014-05-12
			/*
			 * Cursor c = mMediaProvider.query( mPackageName, mThumbsUri, new
			 * String [] { "_data" }, null, null, null, null);
			 */

			Cursor c = GnIContentProvider.query(mMediaProvider, mPackageName,
					mThumbsUri, new String[] { "_data" }, null, null, null,
					null);
			// modify aurora_jianweihu 2014-05-12
//			AuroraLog.vLog(TAG, "pruneDeadThumbnailFiles... " + c);
			if (c != null && c.moveToFirst()) {
				do {
					String fullPathString = c.getString(0);
					existingFiles.remove(fullPathString);
				} while (c.moveToNext());
			}

			for (String fileToDelete : existingFiles) {
				if (false)
					AuroraLog.vLog(TAG, "fileToDelete is " + fileToDelete);
				try {
					(new File(fileToDelete)).delete();
				} catch (SecurityException ex) {
				}
			}

//			AuroraLog.vLog(TAG, "/pruneDeadThumbnailFiles... " + c);
			if (c != null) {
				c.close();
			}
		} catch (RemoteException e) {
			// We will soon be killed...
		}
	}

	private void processPlayList(FileEntry entry, Cursor fileList)
			throws RemoteException {

		String path = entry.mPath;
		ContentValues values = new ContentValues();
		int lastSlash = path.lastIndexOf('/');

		// 路径不包含‘/’抛出路径错误
		if (lastSlash < 0)
			throw new IllegalArgumentException("bad path " + path);

		Uri uri, membersUri;
		long rowId = entry.mRowId;

		// 获取播放列表的名字
		String name = values.getAsString(MediaStore.Audio.Playlists.NAME);
		if (name == null) {
			name = values.getAsString(MediaStore.MediaColumns.TITLE);
			if (name == null) {
				// 获取文件名
				int lastDot = path.lastIndexOf('.');
				name = (lastDot < 0 ? path.substring(lastSlash + 1) : path
						.substring(lastSlash + 1, lastDot));
			}
		}

		values.put(MediaStore.Audio.Playlists.NAME, name);
		values.put(MediaStore.Audio.Playlists.DATE_MODIFIED,
				entry.mLastModified);

		if (rowId == 0) {
			values.put(MediaStore.Audio.Playlists.DATA, path);
			// modify aurora_jianweihu 2014-05-12
			// uri = mMediaProvider.insert(mPackageName, mPlaylistsUri, values);
			uri = GnIContentProvider.insert(mMediaProvider, mPackageName,
					mPlaylistsUri, values);
			// modify aurora_jianweihu 2014-05-12
			rowId = ContentUris.parseId(uri);
			membersUri = Uri.withAppendedPath(uri,
					Playlists.Members.CONTENT_DIRECTORY);
		} else {
			uri = ContentUris.withAppendedId(mPlaylistsUri, rowId);
			// modify aurora_jianweihu 2014-05-12
			// mMediaProvider.update(mPackageName, uri, values, null, null);

			GnIContentProvider.update(mMediaProvider, mPackageName, uri,
					values, null, null);
			// modify aurora_jianweihu 2014-05-12
			// delete members of existing playlist
			membersUri = Uri.withAppendedPath(uri,
					Playlists.Members.CONTENT_DIRECTORY);
			// modify aurora_jianweihu 2014-05-12
			// mMediaProvider.delete(mPackageName, membersUri, null, null);

			GnIContentProvider.delete(mMediaProvider, mPackageName, membersUri,
					null, null);
			// modify aurora_jianweihu 2014-05-12
		}

		String playListDirectory = path.substring(0, lastSlash + 1);
		MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
		int fileType = (mediaFileType == null ? 0 : mediaFileType.fileType);

		if (fileType == MediaFile.FILE_TYPE_M3U) {
			processM3uPlayList(path, playListDirectory, membersUri, values,
					fileList);
		} else if (fileType == MediaFile.FILE_TYPE_PLS) {
			processPlsPlayList(path, playListDirectory, membersUri, values,
					fileList);
		} else if (fileType == MediaFile.FILE_TYPE_WPL) {
			processWplPlayList(path, playListDirectory, membersUri, values,
					fileList);
		}
	}

	private void processM3uPlayList(String path, String playListDirectory,
			Uri uri, ContentValues values, Cursor fileList) {
		BufferedReader reader = null;
		try {
			File f = new File(path);
			if (f.exists()) {
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)), 8192);
				String line = reader.readLine();
				mPlaylistEntries.clear();
				while (line != null) {
					// ignore comment lines, which begin with '#'
					if (line.length() > 0 && line.charAt(0) != '#') {
						cachePlaylistEntry(line, playListDirectory);
					}
					line = reader.readLine();
				}

				processCachedPlaylist(fileList, values, uri);
			}
		} catch (IOException e) {
			Log.e(TAG,
					"IOException in AuroraMediaScanner.processM3uPlayList()", e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				Log.e(TAG,
						"IOException in AuroraMediaScanner.processM3uPlayList()",
						e);
			}
		}
	}

	private void processPlsPlayList(String path, String playListDirectory,
			Uri uri, ContentValues values, Cursor fileList) {
		BufferedReader reader = null;
		try {
			File f = new File(path);
			if (f.exists()) {
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)), 8192);
				String line = reader.readLine();
				mPlaylistEntries.clear();
				while (line != null) {
					// ignore comment lines, which begin with '#'
					if (line.startsWith("File")) {
						int equals = line.indexOf('=');
						if (equals > 0) {
							cachePlaylistEntry(line.substring(equals + 1),
									playListDirectory);
						}
					}
					line = reader.readLine();
				}

				processCachedPlaylist(fileList, values, uri);
			}
		} catch (IOException e) {
			Log.e(TAG,
					"IOException in AuroraMediaScanner.processPlsPlayList()", e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				Log.e(TAG,
						"IOException in AuroraMediaScanner.processPlsPlayList()",
						e);
			}
		}
	}

	class WplHandler implements ElementListener {

		final ContentHandler handler;
		String playListDirectory;

		public WplHandler(String playListDirectory, Uri uri, Cursor fileList) {
			this.playListDirectory = playListDirectory;

			RootElement root = new RootElement("smil");
			Element body = root.getChild("body");
			Element seq = body.getChild("seq");
			Element media = seq.getChild("media");
			media.setElementListener(this);

			this.handler = root.getContentHandler();
		}

		@Override
		public void start(Attributes attributes) {
			String path = attributes.getValue("", "src");
			if (path != null) {
				cachePlaylistEntry(path, playListDirectory);
			}
		}

		@Override
		public void end() {
		}

		ContentHandler getContentHandler() {
			return handler;
		}
	}

	private void processWplPlayList(String path, String playListDirectory,
			Uri uri, ContentValues values, Cursor fileList) {
		FileInputStream fis = null;
		try {
			File f = new File(path);
			if (f.exists()) {
				fis = new FileInputStream(f);

				mPlaylistEntries.clear();
				Xml.parse(fis, Xml.findEncodingByName("UTF-8"), new WplHandler(
						playListDirectory, uri, fileList).getContentHandler());

				processCachedPlaylist(fileList, values, uri);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				Log.e(TAG,
						"IOException in AuroraMediaScanner.processWplPlayList()",
						e);
			}
		}
	}

	private void cachePlaylistEntry(String line, String playListDirectory) {
		PlaylistEntry entry = new PlaylistEntry();
		// watch for trailing whitespace
		int entryLength = line.length();
		while (entryLength > 0
				&& Character.isWhitespace(line.charAt(entryLength - 1)))
			entryLength--;
		// path should be longer than 3 characters.
		// avoid index out of bounds errors below by returning here.
		if (entryLength < 3)
			return;
		if (entryLength < line.length())
			line = line.substring(0, entryLength);

		// does entry appear to be an absolute path?
		// look for Unix or DOS absolute paths
		char ch1 = line.charAt(0);
		boolean fullPath = (ch1 == '/' || (Character.isLetter(ch1)
				&& line.charAt(1) == ':' && line.charAt(2) == '\\'));
		// if we have a relative path, combine entry with playListDirectory
		if (!fullPath)
			line = playListDirectory + line;
		entry.path = line;
		// FIXME - should we look for "../" within the path?

		mPlaylistEntries.add(entry);
	}

	// returns the number of matching file/directory names, starting from the
	// right
	private int matchPaths(String path1, String path2) {
		int result = 0;
		int end1 = path1.length();
		int end2 = path2.length();

		while (end1 > 0 && end2 > 0) {
			int slash1 = path1.lastIndexOf('/', end1 - 1);
			int slash2 = path2.lastIndexOf('/', end2 - 1);
			int backSlash1 = path1.lastIndexOf('\\', end1 - 1);
			int backSlash2 = path2.lastIndexOf('\\', end2 - 1);
			int start1 = (slash1 > backSlash1 ? slash1 : backSlash1);
			int start2 = (slash2 > backSlash2 ? slash2 : backSlash2);
			if (start1 < 0)
				start1 = 0;
			else
				start1++;
			if (start2 < 0)
				start2 = 0;
			else
				start2++;
			int length = end1 - start1;
			if (end2 - start2 != length)
				break;
			if (path1.regionMatches(true, start1, path2, start2, length)) {
				result++;
				end1 = start1 - 1;
				end2 = start2 - 1;
			} else
				break;
		}

		return result;
	}

	private boolean matchEntries(long rowId, String data) {

		int len = mPlaylistEntries.size();
		boolean done = true;
		for (int i = 0; i < len; i++) {
			PlaylistEntry entry = mPlaylistEntries.get(i);
			if (entry.bestmatchlevel == Integer.MAX_VALUE) {
				continue; // this entry has been matched already
			}
			done = false;
			if (data.equalsIgnoreCase(entry.path)) {
				entry.bestmatchid = rowId;
				entry.bestmatchlevel = Integer.MAX_VALUE;
				continue; // no need for path matching
			}

			int matchLength = matchPaths(data, entry.path);
			if (matchLength > entry.bestmatchlevel) {
				entry.bestmatchid = rowId;
				entry.bestmatchlevel = matchLength;
			}
		}
		return done;
	}

	private void processCachedPlaylist(Cursor fileList, ContentValues values,
			Uri playlistUri) {
		fileList.moveToPosition(-1);
		while (fileList.moveToNext()) {
			long rowId = fileList.getLong(FILES_PRESCAN_ID_COLUMN_INDEX);
			String data = fileList.getString(FILES_PRESCAN_PATH_COLUMN_INDEX);
			if (matchEntries(rowId, data)) {
				break;
			}
		}

	}

	// JNI方法
	private native void auroraProgressDirectory(String path,
			AuroraMediaScannerClient client);

	private native void auroraProgressFile(String path, String mimeType,
			AuroraMediaScannerClient client);

	public native void auroraSetLocale(String locale);

	private static native final void auroraNativeInit();

	private native final void auroraNativeSetup();

	private native final void auroraNativeFinalize();

	public void release() {
		auroraNativeFinalize();
	}

	@Override
	protected void finalize() {
		mContext.getContentResolver().releaseProvider(mMediaProvider);
		auroraNativeFinalize();
	}
}
