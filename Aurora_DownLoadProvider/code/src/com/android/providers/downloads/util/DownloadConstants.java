// Gionee <wangpf> <2013-09-09> modify for CR00894142 begin
package com.android.providers.downloads.util;

public class DownloadConstants {
	public static final int STORAGE_INTERNAL = GnStorage.INTERNAL_STORAGE_ID;
	public static final int STORAGE_SDCARD = GnStorage.SDCARD_STORAGE_ID;

	public static final int PRIORITY_MAX = 10;
	public static final int PRIORITY_MIN = 0;

	public static final class DownloadsColum {
		public static final String COLUMN_PRIORITY_LEVEL = "priority_level";
		public static final String COLUMN_LAST_CHANGE_PRIORITY_TIME = "last_change_priority_time";
		public static final String COLUMN_LAST_RUNNING_TIME = "last_running_time";
		public static final String COLUMN_STORAGE = "storage";
		public static final String COLUMN_ADD_TASK_TIME = "add_task_time";
		// add by jxh 2014-6-4 begin
		public static final String COLUMN_ALLOW_WRITE = "allow_write";
		// add by jxh end

		// add by jxh 2014-10-13 S5 特有 begin
		public static final String COLUMN_RANGE_START = "range_start";
		public static final String COLUMN_RANGE_END = "range_end";
		public static final String COLUMN_RANGE_FIRST_END = " range_first_end";
		// add by jxh 2014-10-13 S5 特有 end
		
		//add by JXH 2015-1-28 Mi note begin
		public static final String COLUMN_FILE_CREATE_TIME ="file_create_time";
		public static final String COLUMN_DOWNLOADING_CURRENT_SPEED ="downloading_current_speed";
		public static final String COLUMN_DOWNLOAD_SURPLUS_TIME ="download_surplus_time";
		public static final String COLUMN_XL_ACCELERATE_SPEED ="xl_accelerate_speed";
		public static final String COLUMN_DOWNLOADED_TIME ="downloaded_time";
		public static final String COLUMN_XL_VIP_STATUS ="xl_vip_status";
		public static final String COLUMN_XL_VIP_CDN_URL ="xl_vip_cdn_url"; 
		public static final String COLUMN_XL_TASK_OPEN_MARK ="xl_task_open_mark";
		public static final String COLUMN_DOWNLOAD_TASK_THUMBNAIL ="download_task_thumbnail"; 
		//add by JXH 2015-1-28 Mi note end

	}
}
// Gionee <wangpf> <2013-09-09> modify for CR00894142 end
