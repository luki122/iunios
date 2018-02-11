package com.android.auroramusic.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class AuroraNetDBConfig {
	public static final String SAVE_PATH = "save_path";
	public static final String SIZE = "_size";

	public static final class DownloadItemColumns implements BaseColumns {
		public static final String _DATA = "_data";
		public static final String URL = "url";
		public static final String SONG_ID = "song_id";
		public static final String URL_DIGEST = "url_md";
		public static final String SINGER_IMG = "singer_img";
		public static final String LYRIC_URL = "lyric_url";
		public static final String TRACK_TITLE = "track_title";
		public static final String ARTIST = "artist";
		public static final String ALBUM = "album";
		public static final String POSTFIX = "postfix";
		public static final String TOTAL_BYTES = "total_bytes";
		public static final String CURRENT_BYTES = "current_bytes";
		public static final String SAVE_PATH = "save_path";
		public static final String SAVE_NAME = "save_name";
		public static final String FILE_NAME = "file_name";
		public static final String ADDED_TIME = "added_time";
		public static final String LAST_MOD = "last_mod";
		public static final String VISIBILITY = "visibility";
		public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
		public static final int VISIBILITY_HIDDEN = 2;
		public static final String USER_ACTION = "control";
		public static final String MEDIA_SCANNED = "scanned";
		public static final String STATUS = "status";
		public static final String BITRATE = "bitrate";

		public static Uri getContentUri() {
			return Uri.parse("content://AuroraMp3/download");
		}
	}
}
