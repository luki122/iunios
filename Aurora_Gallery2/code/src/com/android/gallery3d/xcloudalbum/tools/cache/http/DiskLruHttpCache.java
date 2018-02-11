package com.android.gallery3d.xcloudalbum.tools.cache.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public class DiskLruHttpCache {
	private static final String TAG = "DiskLruHttpCache";
	private static final boolean DEBUG = true;

	private static final String CACHE_FILENAME_PREFIX = "cache_";
	private static final int MAX_REMOVALS = 4;
	private static final int INITIAL_CAPACITY = 32;
	private static final float LOAD_FACTOR = 0.75f;

	private final File mCacheDir;
	private int cacheSize = 0;
	private int cacheByteSize = 0;
	private final int maxCacheItemSize = 8192; // 8192 item default
	private long maxCacheByteSize = 1024 * 1024 * 16; // 16MB default

	private final Map<String, String> mLinkedHashMap = Collections
			.synchronizedMap(new LinkedHashMap<String, String>(
					INITIAL_CAPACITY, LOAD_FACTOR, true));

	/**
	 * A filename filter to use to identify the cache filenames which have
	 * CACHE_FILENAME_PREFIX prepended.
	 */
	private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.startsWith(CACHE_FILENAME_PREFIX);
		}
	};

	/**
	 * Used to fetch an instance of DiskLruHttpCache.
	 * 
	 * @param context
	 * @param cacheDir
	 * @param maxByteSize
	 * @return
	 */
	public static DiskLruHttpCache openCache(File cacheDir, long maxByteSize) {
		if (cacheDir == null) {
			return null;
		}
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				Log.e(TAG, "ERROR: Cannot create dir " + cacheDir.toString()
						+ "!!!");
			}
		}

		if (cacheDir.isDirectory() && cacheDir.canWrite()
				&& getUsableSpace(cacheDir) > maxByteSize) {
			LogUtil.d(TAG, "cacheDir :" + cacheDir.toString());
			return new DiskLruHttpCache(cacheDir, maxByteSize);
		}

		return null;
	}

	/**
	 * Constructor that should not be called directly, instead use
	 * {@link DiskLruHttpCache#openCache(android.content.Context, java.io.File, long)}
	 * which runs some extra checks before creating a DiskLruHttpCache instance.
	 * 
	 * @param cacheDir
	 * @param maxByteSize
	 */
	private DiskLruHttpCache(File cacheDir, long maxByteSize) {
		mCacheDir = cacheDir;
		maxCacheByteSize = maxByteSize;
	}

	/**
	 * Add a bitmap to the disk cache.
	 * 
	 * @param key
	 *            A unique identifier for the bitmap.
	 * @param data
	 *            The bitmap to store.
	 */
	public void put(String key, List<CommonFileInfo> data) {
		synchronized (mLinkedHashMap) {
			if (mLinkedHashMap.get(key) == null) {
				try {
					final String file = createFilePath(mCacheDir, key);
					if (wirteListToFile(data, file)) {
						put(key, file);
						flushCache();
					}
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void put(String key, String file) {
		mLinkedHashMap.put(key, file);
		cacheSize = mLinkedHashMap.size();
		cacheByteSize += new File(file).length();
	}

	
	public void removeCache(String key) {
		synchronized (mLinkedHashMap) {
			try {
				LogUtil.d(TAG, "removeCache  key::"+key);
				final String filePath = createFilePath(mCacheDir, key);
				File file = new File(filePath);
				mLinkedHashMap.remove(key);
				cacheSize = mLinkedHashMap.size();
				cacheByteSize -= file.length();
				flushCache();
				file.delete();
				LogUtil.d(TAG, "removeCache "+get(key)+" key::"+key);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Flush the cache, removing oldest entries if the total size is over the
	 * specified cache size. Note that this isn't keeping track of stale files
	 * in the cache directory that aren't in the HashMap. If the images and keys
	 * in the disk cache change often then they probably won't ever be removed.
	 */
	private void flushCache() {
		Entry<String, String> eldestEntry;
		File eldestFile;
		long eldestFileSize;
		int count = 0;

		while (count < MAX_REMOVALS
				&& (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheByteSize)) {
			eldestEntry = mLinkedHashMap.entrySet().iterator().next();
			eldestFile = new File(eldestEntry.getValue());
			eldestFileSize = eldestFile.length();
			mLinkedHashMap.remove(eldestEntry.getKey());
			eldestFile.delete();
			cacheSize = mLinkedHashMap.size();
			cacheByteSize -= eldestFileSize;
			count++;
			if (DEBUG) {
				Log.d(TAG, "flushCache - Removed cache file, " + eldestFile
						+ ", " + eldestFileSize);
			}
		}
	}

	public List<CommonFileInfo> get(String key) {
		synchronized (mLinkedHashMap) {
			final String file = mLinkedHashMap.get(key);
			if (file != null) {
				if (DEBUG) {
//					LogUtil.d(TAG, "Disk cache hit");
				}
				return getList(new File(file));
			} else {
				final String existingFile = createFilePath(mCacheDir, key);
				if (new File(existingFile).exists()) {
					put(key, existingFile);
					if (DEBUG) {
//						LogUtil.d(TAG, "Disk cache hit (existing file)");
					}
					return getList(new File(existingFile));
				}
			}
			return null;
		}
	}

	private List<CommonFileInfo> getList(File file) {

		BufferedReader in = null;
		try {
			List<CommonFileInfo> list= null;
			in = new BufferedReader(new FileReader(file));
			String readString = "";
			String currentLine;
			while ((currentLine = in.readLine()) != null) {
				readString += currentLine;
			}
			// Log.d("JXH",
			// "readString+++++++++++++++++++++++++++++++++++++++++::"+readString);
			JSONArray array = new JSONArray(readString);
			for (int i = 0; i < array.length(); i++) {
				if(list==null){
					list = new ArrayList<CommonFileInfo>();
				}
				JSONObject jsonObject = array.getJSONObject(i);
				CommonFileInfo fileInfo = new CommonFileInfo();
				fileInfo.blockList = jsonObject.getString("blockList");
				fileInfo.cTime = jsonObject.getLong("cTime");
				fileInfo.isDir = jsonObject.getBoolean("isDir");
				fileInfo.fsId = jsonObject.getLong("fsId");
				fileInfo.hasSubFolder = jsonObject.getBoolean("hasSubFolder");
				fileInfo.mTime = jsonObject.getLong("mTime");
				fileInfo.path = jsonObject.getString("path");
				fileInfo.size = jsonObject.getLong("size");
				list.add(fileInfo);
			}
			// Log.d("JXH",
			// "getList+++++++++++++++++++++++++++++++++++++++++::"+list.size());
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Checks if a specific key exist in the cache.
	 * 
	 * @param key
	 *            The unique identifier for the bitmap
	 * @return true if found, false otherwise
	 */
	public boolean containsKey(String key) {
		// See if the key is in our HashMap
		if (mLinkedHashMap.containsKey(key)) {
			return true;
		}

		// Now check if there's an actual file that exists based on the key
		final String existingFile = createFilePath(mCacheDir, key);
		if (new File(existingFile).exists()) {
			// File found, add it to the HashMap for future use
			put(key, existingFile);
			return true;
		}
		return false;
	}

	/**
	 * Removes all disk cache entries from this instance cache dir
	 */
	public void clearCache() {
		DiskLruHttpCache.clearCache(mCacheDir);
	}

	/**
	 * Removes all disk cache entries from the application cache directory in
	 * the uniqueName sub-directory.
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique cache directory name to append to the app cache
	 *            directory
	 */
	public static void clearCache(Context context, String uniqueName) {
		File cacheDir = getDiskCacheDir(context, uniqueName);
		clearCache(cacheDir);
	}

	/**
	 * Removes all disk cache entries from the given directory. This should not
	 * be called directly, call
	 * {@link DiskLruHttpCache#clearCache(android.content.Context, String)} or
	 * {@link DiskLruHttpCache#clearCache()} instead.
	 * 
	 * @param cacheDir
	 *            The directory to remove the cache files from
	 */
	private static void clearCache(File cacheDir) {
		final File[] files = cacheDir.listFiles(cacheFileFilter);
		if (files == null) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {

		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
				|| !isExternalStorageRemovable() ? getExternalCacheDir(context)
				.getPath() : context.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * Creates a constant cache file path given a target cache directory and an
	 * image key.
	 * 
	 * @param cacheDir
	 * @param key
	 * @return
	 */
	public static String createFilePath(File cacheDir, String key) {
		try {
			// Use URLEncoder to ensure we have a valid filename, a tad hacky
			// but it will do for
			// this example
			return cacheDir.getAbsolutePath() + File.separator
					+ CACHE_FILENAME_PREFIX
					+ URLEncoder.encode(key.replace("*", ""), "UTF-8");// URLEncoder.encode(key.replace("*",
																		// ""),
																		// "UTF-8")
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a constant cache file path using the current cache directory and
	 * an image key.
	 * 
	 * @param key
	 * @return
	 */
	public String createFilePath(String key) {
		return createFilePath(mCacheDir, key);
	}

	private boolean wirteListToFile(List<CommonFileInfo> infos, String file)
			throws IOException {
		if (infos == null) {
			return false;
		}
		JSONArray jsonArray = new JSONArray();
		for (CommonFileInfo info : infos) {
			JSONObject object = new JSONObject();
			try {
				object.put("blockList", info.blockList);
				object.put("cTime", info.cTime);
				object.put("isDir", info.isDir);
				object.put("fsId", info.fsId);
				object.put("hasSubFolder", info.hasSubFolder);
				object.put("mTime", info.mTime);
				object.put("path", info.path);
				object.put("size", info.size);
				jsonArray.put(object);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file), IO_BUFFER_SIZE);
			out.write(jsonArray.toString());
			// Log.d("JXH",
			// "jsonArray------------------------::"+jsonArray.toString());
			return true;
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private static final int IO_BUFFER_SIZE = 8 * 1024;

	private static long getUsableSpace(File path) {
		/*
		 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		 * return path.getUsableSpace(); }
		 */
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	private static boolean isExternalStorageRemovable() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	private static boolean hasExternalCacheDir() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static File getExternalCacheDir(Context context) {
		if (hasExternalCacheDir()) {
			return context.getExternalCacheDir();
		}
		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName()
				+ "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath()
				+ cacheDir);
	}
}
