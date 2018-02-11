package com.aurora.dbutil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.R.integer;
import android.R.string;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.aurora.config.AuroraConfig;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;
import com.aurora.filemanager.fragment.AuroraMainFragment.SearchSort;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.MimeTypeUtil;
import com.aurora.tools.OperationAction;
import com.aurora.tools.SearchSortCursor;
import com.aurora.tools.Util;
import com.aurora.tools.FileSortHelper.SortMethod;
import com.aurora.tools.OperationAction.Operation;

import android.provider.Downloads;

/**
 * 文件分类
 * @author jiangxh
 * @CreateTime 2014年4月24日 上午11:27:45
 * @Description com.aurora.dbutil FileCategoryHelper.java
 */
public class FileCategoryHelper {
	private static final String TAG = "FileCategoryHelper";
	public static final String volumeName = "external";
	private static final String[] PROJECTION = new String[] { "_id", FileColumns.DATA, FileColumns.DISPLAY_NAME };
	public static final int COLUMN_ID = 0;
	public static final int COLUMN_PATH = 1;
	public static final int COLUMN_TITLE = 2;
	public static final String FILECOLUMNS_ORIENTATION = "orientation";
	private Context context;
	private onFileCategoryInfoChangedLisenter mFileCategoryInfoChangedLisenter = null;

	/**
	 * 数据统计和分类数据接口
	 * @author jiangxh
	 * @CreateTime 2014年4月28日 上午9:25:44
	 * @Description com.aurora.dbutil FileCategoryHelper.java
	 *              onFileCategoryInfoChangedLisenter
	 */
	public interface onFileCategoryInfoChangedLisenter {
		void onFileCategoryInfoChanged(FileCategory fc);

		void onFileListQueryComplete(Cursor cursor);
	}

	public static final int FILE_CATEGORY_NUM_TOKEN = 1000;// 查询文件分类数量
	public static final int FILE_SD_CATEGORY_NUM_TOKEN = 1009;// 查询文件分类数量
	public static final int FILE_CATEGORY_DATAS_TOKEN = 1001;// 查询文件分类数据
	private static final int SEARCH_QUERY_TOKEN = 1002;// 搜索文件
	private static final int SOMETHING_PIC_TOKEN = 1003;
	private BackgroundQueryHandler backgroundQueryHandler;
	private FileCategory mCategory;
	private FileExplorerTabActivity activity;

	/**
	 * 返回当前分类
	 * @return
	 */
	public FileCategory getFileCategory() {
		return mCategory;
	}

	/**
	 * @param mCategory
	 *            the mCategory to set
	 */
	public void setmCategory(FileCategory mCategory) {
		this.mCategory = mCategory;
	}

	/**
	 * 取消分类数据查询任务，如果任务已经开始则无法取消
	 */
	public void cancelQueryEvent(int token) {
		backgroundQueryHandler.cancelOperation(token);
		if (asynQueryData != null && token == FILE_CATEGORY_DATAS_TOKEN) {
			if (asynQueryData.isCancelled()) {
				asynQueryData.cancel(true);
			}
		}
	}

	/**
	 * 文件类型分类
	 */
	public enum FileCategory {
		All, Music, Video, Picture, Doc, Apk, Other, DownLoad
	}

	/**
	 * 存储详情分类
	 */
	public static FileCategory[] sCategories = new FileCategory[] { FileCategory.Music, FileCategory.Video, FileCategory.Picture, FileCategory.Apk,
			FileCategory.Doc, FileCategory.Other };
	private static HashMap<FileCategory, Integer> categoryNames = new HashMap<FileCategory, Integer>();
	static {
		categoryNames.put(FileCategory.All, R.string.category_all);
		categoryNames.put(FileCategory.Music, R.string.category_music);
		categoryNames.put(FileCategory.Video, R.string.category_video);
		categoryNames.put(FileCategory.Picture, R.string.category_picture);
		categoryNames.put(FileCategory.Doc, R.string.category_document);
		categoryNames.put(FileCategory.Apk, R.string.category_apk);
		categoryNames.put(FileCategory.Other, R.string.category_other);
	}

	/**
	 * 通过Category 获取分类名称
	 * @return
	 */
	public int getCurCategoryNameResId() {
		return categoryNames.get(mCategory);
	}

	private static FileCategoryHelper instance;

	public static FileCategoryHelper getInstance(onFileCategoryInfoChangedLisenter mFileCategoryInfoChangedLisenter, Context context) {
		if (instance == null) {
			instance = new FileCategoryHelper(mFileCategoryInfoChangedLisenter, context);
		}
		return instance;
	}

	public static FileCategoryHelper getInstance(Context context) {
		if (instance == null) {
			instance = new FileCategoryHelper(context);
		}
		return instance;
	}

	public FileCategoryHelper(onFileCategoryInfoChangedLisenter mFileCategoryInfoChangedLisenter, Context context) {
		super();
		this.mFileCategoryInfoChangedLisenter = mFileCategoryInfoChangedLisenter;
		this.mCategory = FileCategory.All;
		if (backgroundQueryHandler == null) {
			backgroundQueryHandler = new BackgroundQueryHandler(context.getContentResolver());
		}
		this.context = context;
		activity = (FileExplorerTabActivity) context;
	}

	public FileCategoryHelper(Context context) {
		super();
		this.mCategory = FileCategory.All;
		if (backgroundQueryHandler == null) {
			backgroundQueryHandler = new BackgroundQueryHandler(context.getApplicationContext().getContentResolver());
		}
		this.context = context;
		activity = (FileExplorerTabActivity) context;
	}

	/**
	 * 刷新指定分类数据
	 * @param category
	 */
	public void refreshCategoryInfo(FileCategory category) {
		Uri uri = null;
		List<FileInfo> fileInfos = activity.getStorages();
		switch (category) {
		case Apk:
		case Doc:
			uri = Files.getContentUri(volumeName);
			refreshFileCategory(FileCategory.Doc, uri, 0);
			refreshFileCategory(FileCategory.Apk, uri, 0);
			if (fileInfos.size() >= 2 && FileExplorerTabActivity.getmSDCard2Path() != null) {
				for (FileInfo fileInfo : fileInfos) {
					refreshFileCategory(FileCategory.Doc, uri, fileInfo.filePath, 5);
					refreshFileCategory(FileCategory.Apk, uri, fileInfo.filePath, 5);
				}
			}
			break;
		case Music:
			uri = Audio.Media.getContentUri(volumeName);
			refreshFileCategory(FileCategory.Music, uri, 0);
			if (fileInfos.size() >= 2 && FileExplorerTabActivity.getmSDCard2Path() != null) {
				for (FileInfo fileInfo : fileInfos) {
					refreshFileCategory(FileCategory.Music, uri, fileInfo.filePath, 5);
				}
			}
			break;
		case Picture:
			uri = Images.Media.getContentUri(volumeName);
			refreshFileCategory(FileCategory.Picture, uri, 0);
			if (fileInfos.size() >= 2 && FileExplorerTabActivity.getmSDCard2Path() != null) {
				for (FileInfo fileInfo : fileInfos) {
					refreshFileCategory(FileCategory.Picture, uri, fileInfo.filePath, 5);
				}
			}
			break;
		case Video:
			uri = Video.Media.getContentUri(volumeName);
			refreshFileCategory(FileCategory.Video, uri, 0);
			if (fileInfos.size() >= 2 && FileExplorerTabActivity.getmSDCard2Path() != null) {
				for (FileInfo fileInfo : fileInfos) {
					refreshFileCategory(FileCategory.Video, uri, fileInfo.filePath, 5);
				}
			}
			break;
		case DownLoad:
			uri = getContentUriByCategory(FileCategory.DownLoad);
			refreshFileCategory(FileCategory.DownLoad, uri, 0);
			break;
		default:
			break;
		}
	}

	private static final long dTime = 300;

	/**
	 * 初始查询统计数据
	 */
	public void initFileCategoryInfo() {
		// LogUtil.log(TAG, "initFileCategoryInfo");
		Uri uria = Audio.Media.getContentUri(volumeName);
		Uri uriv = Video.Media.getContentUri(volumeName);
		Uri urii = Images.Media.getContentUri(volumeName);
		Uri urif = Files.getContentUri(volumeName);
		refreshFileCategory(FileCategory.Music, uria, dTime);
		refreshFileCategory(FileCategory.Video, uriv, dTime);
		refreshFileCategory(FileCategory.Picture, urii, dTime);
		refreshFileCategory(FileCategory.Doc, urif, dTime);
		refreshFileCategory(FileCategory.Apk, urif, dTime);
		Uri uri = getContentUriByCategory(FileCategory.DownLoad);
		refreshFileCategory(FileCategory.DownLoad, uri, dTime);
		List<FileInfo> fileInfos = activity.getStorages();
		if (fileInfos.size() >= 2 && FileExplorerTabActivity.getmSDCard2Path() != null) {
			for (FileInfo fileInfo : fileInfos) {
				refreshFileCategory(FileCategory.Music, uria, fileInfo.filePath, dTime);
				refreshFileCategory(FileCategory.Video, uriv, fileInfo.filePath, dTime);
				refreshFileCategory(FileCategory.Picture, urii, fileInfo.filePath, dTime);
				refreshFileCategory(FileCategory.Doc, urif, fileInfo.filePath, dTime);
				refreshFileCategory(FileCategory.Apk, urif, fileInfo.filePath, dTime);
			}
		}
	}

	/**
	 * 记录分类数据
	 * @author jiangxh
	 * @CreateTime 2014年4月24日 下午3:33:59
	 */
	public class CategoryInfo {
		public long count;// 总数
		public long size;

		@Override
		public String toString() {
			return "CategoryInfo [count=" + count + ", size=" + size + "]";
		}
	}

	private ConcurrentHashMap<String, ConcurrentHashMap<FileCategory, CategoryInfo>> concurrentCategoryInfo = new ConcurrentHashMap<String, ConcurrentHashMap<FileCategory, CategoryInfo>>();
	private ConcurrentHashMap<FileCategory, CategoryInfo> tagCategoryInfo;

	public ConcurrentHashMap<FileCategory, CategoryInfo> getCategoryInfos(String tag) {
		for (Entry<String, ConcurrentHashMap<FileCategory, CategoryInfo>> entry : concurrentCategoryInfo.entrySet()) {
			// LogUtil.log(TAG, "concurrentCategoryInfo==" + entry.getKey()
			// + ", " + entry.getValue());
		}
		return concurrentCategoryInfo.get(tag);
	}

	public CategoryInfo getCategoryInfo(FileCategory fc) {
		if (FileExplorerTabActivity.ROOT_PATH == null) {// 储存器不可用
			return null;
		}
		tagCategoryInfo = concurrentCategoryInfo.get(FileExplorerTabActivity.ROOT_PATH);
		if (tagCategoryInfo == null) {
			tagCategoryInfo = new ConcurrentHashMap<FileCategory, CategoryInfo>();
		}
		if (tagCategoryInfo.containsKey(fc)) {
			return tagCategoryInfo.get(fc);
		} else {
			CategoryInfo info = new CategoryInfo();
			tagCategoryInfo.put(fc, info);
			return info;
		}
	}

	/**
	 * 保存存储信息
	 * @param tag
	 *            存储器名词 eg：/storage/sdcard0 或者 sdcard0
	 * @param fc
	 * @param count
	 * @param size
	 */
	private synchronized void setConcurrentCategoryInfo(String tag, FileCategory fc, long count, long size) {
		if (tag == null) {// 储存器不可用
			return;
		}
		tagCategoryInfo = concurrentCategoryInfo.get(tag);
		if (tagCategoryInfo == null) {
			tagCategoryInfo = new ConcurrentHashMap<FileCategoryHelper.FileCategory, FileCategoryHelper.CategoryInfo>();
			CategoryInfo categoryInfo = new CategoryInfo();
			categoryInfo.count = count;
			categoryInfo.size = size;
			tagCategoryInfo.put(fc, categoryInfo);
			concurrentCategoryInfo.put(tag, tagCategoryInfo);
			// LogUtil.log(TAG, "1 put===" + tag + " fc==" + fc + " count=="
			// + count + " size==" + size);
		} else {
			CategoryInfo info = tagCategoryInfo.get(fc);
			if (info == null) {
				info = new CategoryInfo();
				info.count = count;
				info.size = size;
				tagCategoryInfo.put(fc, info);
			} else {
				info.count = count;
				info.size = size;
				tagCategoryInfo.put(fc, info);
			}
			concurrentCategoryInfo.put(tag, tagCategoryInfo);
			// LogUtil.log(TAG, "2 put===" + tag + " fc==" + fc + " count=="
			// + count + " size==" + size);
		}
		// LogUtil.log(TAG, "concurrentCategoryInfo.size()=="
		// + concurrentCategoryInfo.size());
	}

	/**
	 * 刷新分类统计数据(内部存储和外部存储统计分类)
	 * @param fc
	 * @param uri
	 * @param delayMillis
	 */
	private synchronized void refreshFileCategory(final FileCategory fc, final Uri uri, long delayMillis) {
		final String[] columns;
		if (fc == FileCategory.DownLoad) {
			columns = new String[] { Downloads.Impl._ID, Downloads.Impl.COLUMN_STATUS, Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI };
		} else {
			columns = new String[] { "COUNT(*)", "SUM(_size)" };
		}
		// cancelQueryEvent(FILE_CATEGORY_NUM_TOKEN);
		backgroundQueryHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// LogUtil.log(TAG, "查询分类==" + fc.toString());
				if (fc.equals(FileCategory.DownLoad)) {
					backgroundQueryHandler.startQuery(FILE_CATEGORY_NUM_TOKEN, fc, uri, columns, null, null, null);
				} else {
					backgroundQueryHandler.startQuery(FILE_CATEGORY_NUM_TOKEN, fc, uri, columns, buildSelectionByCategory(fc),// +") GROUP BY SUBSTR(_data,0,"
							// + getLength(),
							null, null);
				}
			}
		}, delayMillis);
	}

	private int getLength() {
		int internalLen = 0;
		int externalLen = 0;
		if (FileExplorerTabActivity.getmSDCardPath() != null) {
			internalLen = FileExplorerTabActivity.getmSDCardPath().length();
		}
		if (FileExplorerTabActivity.getmSDCard2Path() != null) {
			externalLen = FileExplorerTabActivity.getmSDCard2Path().length();
		}
		return Math.max(internalLen, externalLen);
	}

	/**
	 * 查询每个存储器数据分类
	 * @param fc
	 * @param uri
	 * @param sdPath
	 * @param delayMillis
	 */
	private void refreshFileCategory(final FileCategory fc, final Uri uri, final String sdPath, long delayMillis) {
		final String[] columns = new String[] { "COUNT(*)", "SUM(_size)" };
		cancelQueryEvent(FILE_CATEGORY_NUM_TOKEN);
		backgroundQueryHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				backgroundQueryHandler.startQuery(FILE_SD_CATEGORY_NUM_TOKEN, fc, sdPath, uri, columns,
						buildSelectionByCategoryFromSdType(fc, sdPath), null, null);
			}
		}, delayMillis);
	}

	private AsynQueryData asynQueryData;
	private WorkerArgs workerArgs;
	/*********************************** Statistics start ****************************************************************************/
	private static final Uri uriStatistics = Uri.parse("content://com.iuni.reporter/module/");
	private static final int insertsStatisticsToken = 10000;
	private static final int updateStatisticsToken = 10001;
	private static final String MODULEKEY = "module_key";
	private static final String ITEMTAG = "item_tag";
	private static final String value = "value";

	public void updateStatistics(ContentValues values) {
		if (backgroundQueryHandler == null || !AuroraConfig.isStatistics) {
			LogUtil.e(TAG, "backgroundQueryHandler is null or isStatistics::" + AuroraConfig.isStatistics);
			return;
		}
		LogUtil.d(TAG, "updateStatistics:::" + values.get(ITEMTAG));
		backgroundQueryHandler.startUpdate(updateStatisticsToken, values.get(ITEMTAG), uriStatistics, values, null, null);
	}

	public ContentValues getStatisticsContentValues(String itemKey) {
		ContentValues values = new ContentValues();
		values.put(MODULEKEY, AuroraConfig.fileManagerKey);
		values.put(ITEMTAG, itemKey);
		values.put(value, 1);
		return values;
	}

	/*********************************** Statistics end ****************************************************************************/
	/**
	 * 查询分类数据
	 * @param fc
	 *            类型
	 * @param {@link SortMethod} 排序(图片和APK 按照时间降序排序)
	 * @return {@link Cursor}
	 */
	public void query(FileCategory fc, SortMethod sort) {
		this.mCategory = fc;
		Uri uri = getContentUriByCategory(fc);
		if (uri == null) {
			return;
		}
		// LogUtil.elog(TAG, "url==" + uri.toString());
		String selection = buildSelectionByCategory(fc);
		// + " ) GROUP BY SUBSTR(_data,0,length(_data) ";//语句有遗漏
		String sortOrder = buildSortOrder(sort);
		// if (fc == FileCategory.Picture || fc == FileCategory.Apk) {
		// sortOrder = buildSortOrder(SortMethod.date);
		// }
		String[] columns;
		if (fc == FileCategory.Video || fc == FileCategory.Music) {
			columns = new String[] { MediaColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED, MediaColumns.DISPLAY_NAME,
					MediaColumns.TITLE };
		} else if (fc == FileCategory.Picture) {
			columns = new String[] { MediaColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED, MediaColumns.TITLE,
					FILECOLUMNS_ORIENTATION };
		} else {
			columns = new String[] { MediaColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED, MediaColumns.TITLE };
		}
		// backgroundQueryHandler.startQuery(FILE_CATEGORY_DATAS_TOKEN, fc, uri,
		// columns, selection, null, sortOrder);
		if (asynQueryData != null && asynQueryData.getStatus() == AsyncTask.Status.RUNNING) {
			asynQueryData.cancel(true);
		}
		// LogUtil.log(TAG, "selection=="+selection);
		workerArgs = new WorkerArgs(uri, columns, selection, null, sortOrder);
		asynQueryData = new AsynQueryData(context.getContentResolver());
		// asynQueryData.execute(workerArgs);
		asynQueryData.executeOnExecutor(activity.getFULL_TASK_EXECUTOR(), workerArgs);
	}

	/**
	 * 通过key值查询数据
	 * @param fc
	 * @param sort
	 * @param dataKey
	 *            {@link FileColumns.data}
	 */
	public void queryByKey(FileCategory fc, SortMethod sort, String dataKey) {
		Uri uri = getContentUriByCategory(fc);
		if (uri == null) {
			return;
		}
		String selection = buildSelectionByCategory(fc, dataKey);
		String sortOrder = buildSortOrder(sort);
		// if (fc == FileCategory.Picture || fc == FileCategory.Apk) {
		// sortOrder = buildSortOrder(SortMethod.date);
		// }
		String[] columns;
		if (fc == FileCategory.Video || fc == FileCategory.Music) {
			columns = new String[] { MediaColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED, MediaColumns.DISPLAY_NAME,
					MediaColumns.TITLE };
		} else if (fc == FileCategory.Picture) {
			columns = new String[] { MediaColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED, MediaColumns.TITLE,
					FILECOLUMNS_ORIENTATION };
		} else {
			columns = new String[] { MediaColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED, MediaColumns.TITLE };
		}
		backgroundQueryHandler.startQuery(SOMETHING_PIC_TOKEN, fc, uri, columns, selection, null, sortOrder);
	}

	/**
	 * 搜索查询
	 * @param key
	 */
	public void searchQuery(String key) {
		Uri uri = Files.getContentUri(volumeName);
		// 搜索重复值条件设置
		String where = "(" + FileColumns.DATA + " LIKE '%" + key + "%'";
		// String where = "(" + FileColumns.TITLE + " LIKE '" + key +
		// "%' or "+FileColumns.DISPLAY_NAME+" LIKE '"+key+" %' ";
		where += " and " + FileColumns.DATA + " not like LOWER('%/android%') and " + FileColumns.DATA + " not like LOWER('%/cache%') and "
				+ FileColumns.DATA + " not like LOWER('%/.%'))";
		List<FileInfo> fileInfos = activity.getStorages();
		int sizs = fileInfos.size();
		if (sizs != 0) {
			where += " and (";
			for (int i = 0; i < sizs - 1; i++) {
				where += " (_data like '" + fileInfos.get(i).filePath + "%') or";
			}
			where += " (_data like '" + fileInfos.get(fileInfos.size() - 1).filePath + "%') ) ";
		}
		// LogUtil.log(TAG, "search==" + where);
		String sortOrder = buildSortOrder(SortMethod.name);
		// backgroundQueryHandler.startQuery(SEARCH_QUERY_TOKEN, null, uri,
		// PROJECTION, where, null, sortOrder);
		if (asynQueryData != null && asynQueryData.getStatus() == AsyncTask.Status.RUNNING) {
			asynQueryData.cancel(true);
		}
		workerArgs = new WorkerArgs(uri, PROJECTION, where, null, sortOrder);
		asynQueryData = new AsynQueryData(context.getContentResolver());
		// asynQueryData.execute(workerArgs);
		asynQueryData.executeOnExecutor(activity.getFULL_TASK_EXECUTOR(), workerArgs);
	}

	/**
	 * 根据类型获取查询URI
	 * @param {@link FileCategory}
	 * @return {@link URL}
	 */
	private Uri getContentUriByCategory(FileCategory cat) {
		Uri uri;
		switch (cat) {
		case Doc:
		case Apk:
			uri = Files.getContentUri(volumeName);
			break;
		case Music:
			uri = Audio.Media.getContentUri(volumeName);
			break;
		case Video:
			uri = Video.Media.getContentUri(volumeName);
			break;
		case Picture:
			uri = Images.Media.getContentUri(volumeName);
			break;
		case DownLoad:
			uri = Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI;
			break;
		default:
			uri = null;
		}
		return uri;
	}

	public static final String DATETAKEN = "datetaken";

	/**
	 * 查询排序
	 * @param sort
	 * @return
	 */
	private String buildSortOrder(SortMethod sort) {
		String sortOrder = null;
		switch (sort) {
		case pinyin:
			// sortOrder = "bucket_display_name" +" COLLATE LOCALIZED";
			// //按字母排序a_z COLLATE LOCALIZED
			break;
		case name:
			sortOrder = FileColumns.TITLE + " asc";
			break;
		case size:
			sortOrder = FileColumns.SIZE + " asc";
			break;
		case date:
			// sortOrder = FileColumns.DATE_MODIFIED + " desc";
			sortOrder = DATETAKEN + " desc , " + FileColumns._ID + " desc ";
			break;
		case modifyDate:
			sortOrder = FileColumns.DATE_MODIFIED + " desc";
			break;
		case type:
			sortOrder = FileColumns.MIME_TYPE + " asc, " + FileColumns.TITLE + " asc";
			break;
		case music:
			sortOrder = FileColumns.DISPLAY_NAME + " asc";
		}
		return sortOrder;
	}

	/**
	 * 组建筛选查询语句
	 * @param fc
	 * @param key
	 * @return
	 */
	private synchronized String buildSelectionByCategory(FileCategory fc, String key) {
		if (key == null || key.equals("")) {
			return buildSelectionByCategory(fc);
		}
		return buildSelectionByCategory(fc) + " and " + FileColumns.DATA + " LIKE '" + key + "%'  and bucket_display_name = '"
				+ Util.getNameFromFilepath(key) + "' ";
	}

	/**
	 * 组建筛选查询语句
	 * @param fc
	 * @param sdPath
	 * @return
	 */
	private String buildSelectionByCategoryFromSdType(FileCategory fc, String sdPath) {
		String selection = "";
		if (sdPath != null && !sdPath.equals("")) {
			selection = FileColumns.DATA + " LIKE '" + sdPath + "%' and ";
		}
		// LogUtil.elog(TAG, "selection=="+selection);
		switch (fc) {
		case Doc:
			selection += "( " + buildDocSelection() + "  )";
			// LogUtil.log(TAG, selection);
			break;
		case Picture:
			selection += " (" + MediaColumns.SIZE + " > " + AuroraConfig.AURORA_PIC_SIZE + " or " + FileColumns.DATA + " LIKE '"
					+ FileExplorerTabActivity.mSDCardPath + "/QQ_Screenshot%' " + " ) and " + FileColumns.DATA
					+ " NOT LIKE '%/aurora/change/lockscreen%' and " + FileColumns.DATA + " NOT LIKE '" + FileExplorerTabActivity.mSDCardPath
					+ "/iuni/wallpaper/save%' and " + FileColumns.DATA + " NOT LIKE '%.pcx' and " + FileColumns.DATA + " NOT LIKE '%.tif' ";
			// modify by Jxh end
			break;
		case Music:
			selection += MediaColumns.SIZE + " > " + AuroraConfig.AURORA_VIDEO_SIZE + " and " + FileColumns.DATA + " NOT LIKE '"
					+ FileExplorerTabActivity.mSDCardPath + File.separator + context.getResources().getString(R.string.phone_audios) + "%' and "
					+ FileColumns.DATA + " NOT LIKE '" + FileExplorerTabActivity.mSDCardPath + "/VoiceWakeUp%' and " + FileColumns.DATA
					+ " NOT LIKE '" + FileExplorerTabActivity.mSDCardPath + "/note/sound%' ";
			break;
		case Video:
			break;
		case Apk:
			selection += FileColumns.DATA + " LIKE '%.apk' ";// FileColumns.MIME_TYPE
			break;
		default:
			selection = null;
		}
		if (fc != FileCategory.DownLoad) {
			String temp = FileColumns.DATA + " NOT LIKE '" + FileExplorerTabActivity.mSDCardPath + File.separator + "Android/%'" + " and "
					+ FileColumns.DATA + " NOT LIKE ('%/.%')";
			if (TextUtils.isEmpty(selection)) {
				selection = temp;
			} else if (fc.equals(FileCategory.Video)) {
				selection += temp;
			} else {
				selection += " and " + temp;
			}
		}
		// LogUtil.elog(TAG, selection);
		return selection;
	}

	/**
	 * 组建筛选查询语句
	 * @param fc
	 * @return
	 */
	private synchronized String buildSelectionByCategory(FileCategory fc) {
		String result = buildSelectionByCategoryFromSdType(fc, null);
		List<FileInfo> fileInfos = activity.getStorages();
		int sizs = fileInfos.size();
		if (sizs == 0) {
			return result;
		}
		result += " and ( ";
		for (int i = 0; i < sizs - 1; i++) {
			result += " (_data like '" + fileInfos.get(i).filePath + "%') or";
		}
		result += " (_data like '" + fileInfos.get(fileInfos.size() - 1).filePath + "%') ";
		result += " )";
		String priHome = Util.getPrivacyHomePath(activity);
		if (!TextUtils.isEmpty(priHome)) {
			result += " and ( _data NOT LIKE '" + priHome + "%' )";
		}
		// LogUtil.d(TAG, "---sql:"+result);
		return result;
	}

	/**
	 * 组建文档查询语句
	 * @return
	 */
	private String buildDocSelection() {
		StringBuilder selection = new StringBuilder();
		Iterator<String> iter = MimeTypeUtil.sDocMimeTypesSet.iterator();
		selection.append("(");
		while (iter.hasNext()) {
			selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next() + "') OR ");
		}
		return selection + "(" + FileColumns.DATA + " LIKE '%.pptx') OR " + "(" + FileColumns.DATA + " LIKE '%.xlsx') OR " + "(" + FileColumns.DATA
				+ " LIKE '%.docx'))";
	}

	/**
	 * 异步查询类
	 * @author jiangxh
	 * @CreateTime 2014年4月24日 下午1:41:03
	 * @Description com.aurora.dbutil FileCategoryHelper.java
	 */
	private class BackgroundQueryHandler extends AsyncQueryHandler {
		public BackgroundQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Object params, Cursor cursor) {
			super.onQueryComplete(token, cookie, params, cursor);
			if (cursor == null) {
				return;
			}
			switch (token) {
			case FILE_CATEGORY_NUM_TOKEN:
				// LogUtil.log(TAG, "FILE_CATEGORY_NUM_TOKEN");
				if (cursor != null && !cursor.isClosed()) {
					if (((FileCategory) cookie == FileCategory.DownLoad)) {
						int count = 0;
						while (cursor.moveToNext()) {
							int isVisible = -1;
							isVisible = cursor.getInt(2);
							if (isVisible == 1) {// COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI,不可见与可见
								count++;
							}
						}
						LogUtil.d(TAG, (FileCategory) cookie + " count:" + count);
						setConcurrentCategoryInfo(FileExplorerTabActivity.ROOT_PATH, (FileCategory) cookie, count, 0);
						cursor.close();
					} else if (cursor.moveToFirst()) {// 全部数据分类
						long totalCount = cursor.getLong(0);
						long totalSize = cursor.getLong(1);
						LogUtil.d(TAG, (FileCategory) cookie + " totalCount:" + totalCount);
						setConcurrentCategoryInfo(FileExplorerTabActivity.ROOT_PATH, (FileCategory) cookie, totalCount, totalSize);
					}
					cursor.close();
				}
				if (mFileCategoryInfoChangedLisenter != null) {
					mFileCategoryInfoChangedLisenter.onFileCategoryInfoChanged((FileCategory) cookie);
				}
				break;
			case FILE_SD_CATEGORY_NUM_TOKEN:
				// LogUtil.log(TAG, "FILE_SD_CATEGORY_NUM_TOKEN");
				if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
					long totalCount = cursor.getLong(0);
					long totalSize = cursor.getLong(1);
					// LogUtil.log(TAG, (FileCategory) cookie + " totalCount=="
					// + totalCount + " totalSize==" + totalSize
					// + " params==" + params.toString());
					setConcurrentCategoryInfo(params.toString(), (FileCategory) cookie, totalCount, totalSize);
					cursor.close();
				}
				break;
			case SEARCH_QUERY_TOKEN:
				if (cursor != null && !cursor.isClosed()) {
					if (mFileCategoryInfoChangedLisenter != null) {
						mFileCategoryInfoChangedLisenter.onFileListQueryComplete(cursor);
					}
				}
				break;
			case SOMETHING_PIC_TOKEN:
				if (cursor != null && !cursor.isClosed()) {
					// LogUtil.d(TAG,
					// "SOMETHING_PIC_TOKEN cursor:"+cursor.getCount());
					if (mFileCategoryInfoChangedLisenter != null) {
						mFileCategoryInfoChangedLisenter.onFileListQueryComplete(cursor);
					}
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 删除数据库数据
	 * @param fc
	 * @param ids
	 */
	public void delete(long[] ids) {
		if (ids == null || ids.length <= 0) {
			return;
		}
		Uri baseUri = Files.getContentUri(volumeName);
		String selection = "_id in (";
		for (int i = 0; i < ids.length; i++) {
			selection += ids[i];
			if (i != ids.length - 1) {
				selection += ",";
			} else {
				selection += ")";
			}
		}
		// LogUtil.log(TAG, "selection==" + selection);
		backgroundQueryHandler.startDelete(0, null, baseUri, selection, null);
	}

	public void delete(List<FileInfo> fileInfos) {
		if (fileInfos == null || fileInfos.size() <= 0) {
			return;
		}
		long[] ids = new long[fileInfos.size()];
		try {
			for (int i = 0; i < fileInfos.size(); i++) {
				long id = fileInfos.get(i).dbId;
				if (id == 0) {
					id = getDbId(fileInfos.get(i).filePath);
				}
				ids[i] = id;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		delete(ids);
	}

	/**
	 * 通过path 获取id
	 * @param path
	 * @return
	 */
	public long getDbId(String path) {
		String volumeName = "external";
		Uri uri = Files.getContentUri(FileCategoryHelper.volumeName);
		String selection = FileColumns.DATA + "=?";
		String[] selectionArgs = new String[] { path };
		String[] columns = new String[] { FileColumns._ID };
		Cursor c = null;
		long id = 0;
		try {
			c = activity.getContentResolver().query(uri, columns, selection, selectionArgs, null);
			if (c == null) {
				return 0;
			}
			if (c.moveToNext()) {
				id = c.getLong(0);
			}
		} catch (AndroidRuntimeException e) {
			Log.e(TAG, "AndroidRuntimeException " + e != null ? e.getMessage() : "");
		} catch (Exception e) {
			Log.e(TAG, "Exception " + e != null ? e.getMessage() : "");
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return id;
	}

	public Cursor matrixCursorFromCursor(Cursor cursor, String mSearchKey, SearchSort task) {
		if (cursor == null)
			return null;
		MatrixCursor newCursor = new MatrixCursor(cursor.getColumnNames());
		int numColumns = cursor.getColumnCount();
		String data[] = new String[numColumns];
		while (cursor.moveToNext()) {
			if (task != null && task.isCancelled()) {
				return null;
			}
			for (int i = 0; i < numColumns; i++) {
				data[i] = cursor.getString(i);
			}
			String fileName = data[COLUMN_PATH].substring(data[COLUMN_PATH].lastIndexOf("/") + 1, data[COLUMN_PATH].length());
			if (fileName.toLowerCase().contains(mSearchKey.toLowerCase())) {
				data[COLUMN_TITLE] = fileName;
				newCursor.addRow(data);
			}
		}
		// 搜索结果在100个以内的，进行结果排序输出
		if (newCursor != null && newCursor.getCount() < 100) {//
			long time = SystemClock.currentThreadTimeMillis();
			SearchSortCursor sortCursor = new SearchSortCursor(newCursor, MediaColumns.DATA, true, mSearchKey, task);
			long time1 = SystemClock.currentThreadTimeMillis();
			// LogUtil.elog(TAG, "time1=time==" + (time1 - time));
			if (task != null && task.isCancelled()) {
				return null;
			}
			return sortCursor;
		}
		return newCursor;
	}

	protected static final class WorkerArgs {
		public Uri uri;
		public String[] projection;
		public String selection;
		public String[] selectionArgs;
		public String orderBy;

		public WorkerArgs(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
			super();
			this.uri = uri;
			this.projection = projection;
			this.selection = selection;
			this.selectionArgs = selectionArgs;
			this.orderBy = orderBy;
		}
	}

	/**
	 * 查询数据使用异步获取，数据量大不会阻塞
	 * @author jiangxh
	 * @CreateTime 2014年5月20日 上午9:08:53
	 * @Description com.aurora.dbutil FileCategoryHelper.java
	 */
	private class AsynQueryData extends AsyncTask<WorkerArgs, Void, Cursor> {
		private WeakReference<ContentResolver> mResolver;

		public AsynQueryData(ContentResolver cr) {
			super();
			this.mResolver = new WeakReference<ContentResolver>(cr);
		}

		@Override
		protected Cursor doInBackground(WorkerArgs... params) {
			WorkerArgs workerArgs = params[0];
			ContentResolver resolver = mResolver.get();
			if (resolver == null) {// 内存紧张 该资源已经被释放
				return null;
			}
			if (isCancelled()) {// 任务被取消
				// LogUtil.log(TAG, "isCancelled()==" + isCancelled()
				// + " 任务被取消11 !!!!! ");
				return null;
			}
			Cursor cursor = null;
			// long time = SystemClock.currentThreadTimeMillis();
			try {
				cursor = resolver.query(workerArgs.uri, workerArgs.projection, workerArgs.selection, workerArgs.selectionArgs, workerArgs.orderBy);
				if (cursor != null) {
					LogUtil.d(TAG, "----cursor.getCount():" + cursor.getCount());
				}
				// long time2 = SystemClock.currentThreadTimeMillis();
				// LogUtil.elog(TAG, "SystemClock==" + (time2 - time));
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.getMessage());
				if (cursor != null) {
					cursor.close();
				}
				cursor = null;
			}
			if (isCancelled()) {// 任务被取消
				if (cursor != null) {
					cursor.close();
				}
				// LogUtil.log(TAG, "isCancelled()==" + isCancelled()
				// + " 任务被取消22 !!!!! ");
				return null;
			}
			// LogUtil.log(TAG, "isCancelled()==" + isCancelled() +
			// " 任务已经完成 ");
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			// LogUtil.log(TAG, "isCancelled()==" + isCancelled() +
			// " result=="
			// + result);
			/*
			 * if (isCancelled()) { return; }
			 */
			if (mFileCategoryInfoChangedLisenter != null) {
				// LogUtil.log(TAG, "onFileListQueryComplete(result)");
				mFileCategoryInfoChangedLisenter.onFileListQueryComplete(result);
			}
		}
	}
}
