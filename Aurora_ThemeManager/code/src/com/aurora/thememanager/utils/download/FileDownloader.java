package com.aurora.thememanager.utils.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import com.aurora.change.data.DbControl;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.fragments.HttpCallBack;
import com.aurora.thememanager.fragments.JsonHttpListener;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themehelper.LockScreenApplyHelper;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


public class FileDownloader  implements Runnable {
	
	public static final String TAG = "FileDownloader";
	
	/**
	 * 普通下载类型
	 */
	public static final int TYPE_NORMAL = 0;	
	/**
	 *  自动更新类型
	 */
	public static final int TYPE_AUTO_UPDATE = 1;

	/**
	 * 默认
	 */
	public static final int STATUS_DEFAULT = 0; 
	/**
	 * 等待
	 */
	public static final int STATUS_WAIT = STATUS_DEFAULT + 1; 
    /**
     * 连接
     */
	public static final int STATUS_CONNECTING = STATUS_WAIT + 1; 
	/**
	 * 正在下载
	 */
	public static final int STATUS_DOWNLOADING = STATUS_CONNECTING + 1; 
	/**
	 * 暂停
	 */
	public static final int STATUS_PAUSE = STATUS_DOWNLOADING + 1; 
	/**
	 * 暂停需要继续
	 */
	public static final int STATUS_PAUSE_NEED_CONTINUE = STATUS_PAUSE + 1; 
	/**
	 * 暂无网络
	 */
	public static final int STATUS_NO_NETWORK = STATUS_PAUSE_NEED_CONTINUE + 1;
	/**
	 * 连接超时
	 */
	public static final int STATUS_CONNECT_TIMEOUT = STATUS_NO_NETWORK + 1; 
	/**
	 * 重试,连接超时
	 */
	public static final int STATUS_CONNECT_RETRY = STATUS_CONNECT_TIMEOUT + 1; 
	/**
	 * 下载失败
	 */
	public static final int STATUS_FAIL = STATUS_CONNECT_RETRY + 1; 
	/**
	 * 安装应用
	 */
	public static final int STATUS_APPLY_WAIT = STATUS_FAIL + 1; 
	/**
	 * 正在应用
	 */
	public static final int STATUS_APPLING = STATUS_APPLY_WAIT + 1; 
	/**
	 * 应用失败
	 */
	public static final int STATUS_APPLY_FAIL = STATUS_APPLING + 1;
	/**
	 * 应用完成
	 */
	public static final int STATUS_APPLIED = STATUS_APPLY_FAIL + 1; 
	
	/**
	 * 应用完成
	 */
	public static final int STATUS_UPDATE = STATUS_APPLIED + 1; 
	
	
	private int mType = TYPE_NORMAL;
	/**
	 * 下载数据
	 */
	private DownloadData mDownloadData; 
	/**
	 * 下载路径
	 */
	private String mDownloadUrl; 
	/**
	 * 已下载文件长度
	 */
	private long mDownloadSize = 0;
	/**
	 * 原始文件长度
	 */
	private long mFileSize = 0; 
	/**
	 * 文件目录
	 */
	private String mFileSaveDirStr; 
	
	/**
	 * 文件名
	 */
	private String mFileName;
	/**
	 * 本地保存文件
	 */
	private File mSaveFile; 
	/**
	 * 任务创建时间
	 */
	private long mCreateTime = 0; 
	
	private DatabaseController mDbController;
	/**
	 * 下载状态监听器
	 */
	private DownloadCallback mStatusListener;	
	/**
	 * 当前状态
	 */
	private int mStatus = STATUS_DEFAULT; 

	/**
	 * 取消标识
	 */
	private boolean mCancleFlag = false;
	/**
	 * 是否已经连接重试
	 */
	private boolean mRetred = false; 
	private boolean mNeedRetry = false;
	/**
	 * 是否在准备阶段点下的暂停
	 */
	private boolean mPreparePause = false; 
	
	private long mSpeed = 0;
	private boolean mGetSpeedThreadRun = false;
	
	private Context mContext;
	
	/**
	 * 构建文件下载器
	 * 
	 * @param downloadData 下载信息
	 * @param fileSaveDir 文件保存目录
	 */
	public FileDownloader(Context context,DownloadData downloadData,
			File fileSaveDir, DownloadCallback listener, int type) {
		mContext = context;
		downloadData.fileDir = fileSaveDir.getPath();
		init(downloadData, fileSaveDir, listener, type);
	}
	
	@Override
	public void run() {
		startDownload();
	}
	
	/**
	 * 构建FileDownloader信息
	 * 
	 * @param downloadData
	 * @param fileSaveDir
	 * @param listener
	 */
	private void init(DownloadData downloadData,
			File fileSaveDir, DownloadCallback listener, int type) {
		
		this.mDownloadData = downloadData;
		this.mStatusListener = listener;
		mDownloadUrl =downloadData.downloadPath;
		Log.d(TAG, "Url:"+mDownloadUrl+"  name:"+downloadData.downloadId);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdirs();
		}
		this.mFileSaveDirStr = fileSaveDir.getPath();
		this.mType = type;
		if (type == TYPE_NORMAL) {
			Theme theme = (Theme) mDownloadData;
			int themeType = theme.type;
			initDatabaseController (themeType);
		} else {
			this.mDbController = AutoUpdateService.getDatabaseController();
		}
		
		if (mDbController != null) {
			// 存在记录
			if (mDbController.exists(downloadData.downloadId)) {
				DownloadData dataFromDb = mDbController.getDownloadData(downloadData.downloadId);
				// 同一文件下载地址不一致情况，或者版本号不一样的情况，删除数据库记录及文件
				if (!downloadData.downloadPath.equals(dataFromDb.downloadPath)
						|| dataFromDb.versionCode != downloadData.versionCode) {
					mDbController.delete(downloadData.downloadId);
					File f = new File(dataFromDb.fileDir, dataFromDb.fileName);
					f.delete();
				}
			}
			
			if (mDbController.exists(downloadData.downloadId)) {
				mFileName = mDbController.getFileName(downloadData.downloadId);
				if (!TextUtils.isEmpty(mFileName)) {
					mSaveFile = new File(this.mFileSaveDirStr, mFileName); 	// 保存文件
				}
				
				// 任务已存在，但是文件被删除
				if (mSaveFile == null || !mSaveFile.exists()) {
					mDbController.delete(downloadData.downloadId);
					createNewRecord();
				} else {
					mDownloadSize = mDbController.getDownloadSize(downloadData.downloadId);
					mFileSize = mDbController.getFileSize(downloadData.downloadId);
					mFileName = mDbController.getFileName(downloadData.downloadId);
					mStatus = mDbController.getStatus(downloadData.downloadId);
					mCreateTime = mDbController.getCreateTime(downloadData.downloadId); // 获取创建时间
				}
			} else {
				createNewRecord();
			}
		}
	}
	
	private void initDatabaseController(int themeType){
		switch (themeType) {
		case Theme.TYPE_RINGTONG:
			this.mDbController = RingtongDownloadService.getDownloadController();
			break;
		case Theme.TYPE_THEME_PKG:
			this.mDbController = DownloadService.getDownloadController();
		break;
		case Theme.TYPE_TIME_WALLPAPER:
			this.mDbController = TimeWallpaperDownloadService.getDownloadController();
			break;
		case Theme.TYPE_WALLPAPER:
			this.mDbController = WallpaperDownloadService.getDownloadController();
			break;
		default:
			this.mDbController = DownloadService.getDownloadController();
			break;
		}
	}
	
	/**
	 * 增加新记录
	 * 
	 */
	private void createNewRecord() {
		Log.i(TAG, "createNewRecord()");
		mCreateTime = System.currentTimeMillis();
		mFileName = getFileName();
		mSaveFile = null;
		mDbController.insert(mDownloadData, mCreateTime, mStatus, mFileSize);
		mDbController.updateFileDirAndName(mDownloadData.downloadId, mFileSaveDirStr, mFileName);
	}
	
	/**
	 * 开始下载
	 * 
	 */
	private void startDownload() {
		
		mSpeed = 0;
		mNeedRetry = false;
		Log.d(TAG, "mCancleFlag:"+mCancleFlag);
		if (mCancleFlag)
			return;
		Log.d(TAG, "mStatus:"+mStatus+"  mDownloadUrl:"+mDownloadUrl);
		if (mStatus == STATUS_PAUSE || mStatus == STATUS_NO_NETWORK)
			return;
		
		// 没有获取过文件大小
		if (mFileSize <= 0) {
			connectToGetFileSize();
		}
		
		// 如果获取回来的文件大小小于等于0，则表示失败
		if (mFileSize <= 0) {
			if (NetWorkStatusUtils.hasNetwork(mContext)) {
				Log.d(TAG, "mFileSize FileDownloader 0000:");
				setStatus(STATUS_FAIL);
			} else {
				setStatus(STATUS_NO_NETWORK);
			}
			mDbController.updateStatus(mDownloadData.downloadId, mStatus);
			
			return;
		}
		// 如果是在准备阶段点下的暂停, 则不进行下载, 直接return
		if (mPreparePause) {
			mPreparePause = false;
			setStatus(STATUS_PAUSE);
			return;
		}
		
		if (mSaveFile == null) {
			mSaveFile = new File(mFileSaveDirStr, mFileName);
		}
        if (!mSaveFile.exists()) {
			try {
				mSaveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		setStatus(STATUS_CONNECTING);
		
		RandomAccessFile accessFile = null;
		URL url = null;
		HttpURLConnection http = null;
		InputStream inputStream = null;
		try {
	
			url = new URL(mDownloadUrl);
			http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(8 * 1000);
//			http.setDoOutput(true);
			http.setRequestMethod("GET");
			http.setRequestProperty("Range", "bytes=" + mDownloadSize + "-" + (mFileSize-1));
			http.connect();
			int code = http.getResponseCode();
			
			accessFile = new RandomAccessFile(mSaveFile, "rw");
			accessFile.seek(mDownloadSize);
			Log.d(TAG, "mFileSize FileDownloader Status:"+code);
			// 注：加入断点下载返回码为206
			if (code == HttpStatus.SC_OK || code == HttpStatus.SC_PARTIAL_CONTENT){
				inputStream = http.getInputStream();
				byte[] buffer = new byte[4096];
				int offset = 0;
				// 用于计算下载速度
				mGetSpeedThreadRun = true;
				new GetSpeedThread().start();
				
				while ((offset = inputStream.read(buffer)) != -1) {
					if (mCancleFlag) {
						break;
					}
					
					if (mStatus == FileDownloader.STATUS_PAUSE
							|| mStatus == FileDownloader.STATUS_NO_NETWORK
							|| mStatus == FileDownloader.STATUS_CONNECT_TIMEOUT) {
						break;
					}
					
					if (mStatus != FileDownloader.STATUS_DOWNLOADING) {
						setStatus(FileDownloader.STATUS_DOWNLOADING);
					}
					
					if (mDownloadSize >= mFileSize) {
						break;
					}
					
					accessFile.write(buffer, 0, offset);
					mDownloadSize += offset;
					
					setRetred(false);
				}
			} else {
				if (NetWorkStatusUtils.hasNetwork(mContext)) {
					Log.d(TAG, "mFileSize FileDownloader 2222:");
					setStatus(STATUS_FAIL);
				} else {
					setStatus(STATUS_NO_NETWORK);
				}
			}
		} catch (ClientProtocolException e) {
			Log.d(TAG, "mFileSize FileDownloader Status:"+e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, "mFileSize FileDownloader Status222222:"+e);
			if (e instanceof ConnectTimeoutException) {
				setStatus(STATUS_CONNECT_TIMEOUT);
				if (!mRetred) {
					mRetred = true;
					setStatus(STATUS_CONNECT_RETRY);
					mNeedRetry = true;
				} else {
					setRetred(false);
					Log.d(TAG, "mFileSize FileDownloader 3333:");
					setStatus(STATUS_FAIL);
				}
			} else {
				if (NetWorkStatusUtils.hasNetwork(mContext)) {
					if (!mRetred) {
						mRetred = true;
						setStatus(STATUS_CONNECT_RETRY);
						mNeedRetry = true;
					} else {
						setRetred(false);
						Log.d(TAG, "mFileSize FileDownloader 4444:");
						setStatus(STATUS_FAIL);
					}
				} else {
					setStatus(STATUS_NO_NETWORK);
				}
			}
			e.printStackTrace();
		} finally {
			mGetSpeedThreadRun = false;
			http.disconnect();
			
			try {
				if (accessFile != null) {
					accessFile.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				Log.d(TAG, "mFileSize FileDownloader Status333333:"+e);
				e.printStackTrace();
			}
		}
		
		if (mNeedRetry) {
			startDownload();
			return;
		}
		
		// 如果已经下载完成, 删除正在下载数据库中数据
		if (mDownloadSize >= mFileSize) {
            File finishFile = new File(mFileSaveDirStr, mFileName);
            finishFile.renameTo(new File(mFileSaveDirStr,
                    mFileName.substring(0, mFileName.lastIndexOf("."))));
            mDbController.updateFileName(mDownloadData.downloadId,
                    mFileName.substring(0, mFileName.lastIndexOf(".")));
            downloadFinished();
			postDownloadedToServer();
			mDbController.updateFileFinishTime(mDownloadData.downloadId, System.currentTimeMillis());
		}
		
		mDbController.updateStatus(mDownloadData.downloadId, mStatus);
		mDbController.updateDownloadSize(mDownloadData.downloadId, mDownloadSize);
		
		if (mStatusListener != null) {
			mStatusListener.onDownload(mDownloadData.downloadId, mStatus, mDownloadSize, mFileSize);
		}
	}
	
	private void downloadFinished(){
		Theme theme = (Theme) mDownloadData;
		int themeType = theme.type;
		if(themeType == Theme.TYPE_TIME_WALLPAPER){
			 unzipTimeWallpaper((Theme)mDownloadData);
		}
		setStatus(STATUS_APPLY_WAIT);
	}
	
	
	
	/**
	 * 下载完成后上传数据到服务器
	 */
	private void postDownloadedToServer(){
		ThemeInternetHelper post = new ThemeInternetHelper(mContext);
		post.clearRequest();
		Theme theme = (Theme) mDownloadData;
		int themeType = theme.type;
		int type = -1;
		switch (themeType) {
		case Theme.TYPE_RINGTONG:
			 type = ThemeConfig.HttpConfig.DOWNLOADED_POST_TYPE_RINGTONG;
			break;
		case Theme.TYPE_THEME_PKG:
			 type = ThemeConfig.HttpConfig.DOWNLOADED_POST_TYPE_THEME;
		break;
		case Theme.TYPE_TIME_WALLPAPER:
			 type = ThemeConfig.HttpConfig.DOWNLOADED_POST_TYPE_TIME_WALLPAPER;
			break;
		case Theme.TYPE_WALLPAPER:
			 type = ThemeConfig.HttpConfig.DOWNLOADED_POST_TYPE_WALLPAPER;
			break;

		default:
			break;
		}
		
		if(type != -1){
			JSONObject params = HttpUtils.createDownloadedPostData(mContext, type, mDownloadData.downloadId ^ themeType);
			post.request(ThemeConfig.HttpConfig.DOWNLOADED_POST_URL, new JsonHttpListener(new HttpCallBack<JSONObject>() {
				
				@Override
				public void onUsedCache() {
					
				}
				
				@Override
				public void onSuccess(JSONObject response) {
					if(response != null){
						Log.d("post", ""+response.toString());
					}
				}
				
				@Override
				public void onRetry() {
					
				}
				
				@Override
				public void onProgressChange(long fileSize, long downloadedSize) {
					
				}
				
				@Override
				public void onPreExecute() {
					
				}
				
				@Override
				public void onNetworking() {
					
				}
				
				@Override
				public void onFinish() {
					
				}
				
				@Override
				public void onError(InternetError error) {
					
				}
				
				@Override
				public void onCancel() {
					
				}
			}), params,false);
			post.startRequest();
		}
		
	}
	
	/**
	 * 连接并获取到文件大小
	 * 
	 * @throws IOException
	 */
	private long connectToGetFileSize() {
		setStatus(STATUS_CONNECTING);
		
		URL url = null;
		HttpURLConnection http = null;
		
		try {
			url = new URL(mDownloadUrl);
			http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(8 * 1000);
			http.setRequestMethod("GET");
			http.connect();
			int code = http.getResponseCode();
			Log.d(TAG, "get FileSize:"+code);
			if (code == HttpStatus.SC_OK ) {
				
				mFileSize = http.getContentLength();
				//FileLog.i(TAG, "getFileSize: " + fileSize);
				
				mDbController.updateFileSize(mDownloadData.downloadId, mFileSize);
			}
		} catch (ClientProtocolException e) {
			Log.d(TAG, "0:"+e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, "1:"+e);
			e.printStackTrace();
		} finally {
			if(http != null){
				http.disconnect();
			}
		}
		
		return mFileSize;
	}
	
	/**
	 * 获取文件名
	 * 
	 */
	private String getFileName() {

		Theme theme = (Theme)mDownloadData;
		int point = mDownloadUrl.lastIndexOf("/");
		String suffixString = mDownloadUrl.substring(point+1, mDownloadUrl.length());
		int type = theme.type;
		String typeSubStr = "";
			switch (type) {
			case Theme.TYPE_THEME_PKG:
			case Theme.TYPE_TIME_WALLPAPER:
				typeSubStr = ".zip";
				suffixString.replace(".zip", "");
				break;
			default:
				break;
			}
		StringBuilder fileName = new StringBuilder();
		fileName.append(theme.name);
		fileName.append(theme.themeId+"");
		fileName.append(theme.type+"");
		fileName.append(mCreateTime+"");
		fileName.append(suffixString);
		fileName.append(".tmp");
		
		return fileName.toString();
	}
	
	/**
	 * 设置下载状态
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.mStatus = status;
		if(mDbController == null){
			Theme theme = (Theme) mDownloadData;
			int themeType = theme.type;
			initDatabaseController (themeType);
		}
		mDbController.updateStatus(mDownloadData.downloadId, status);
	}

	/**
	 * 获取下载任务的状态
	 * 
	 * @return
	 */
	public int getStatus() {
		return mStatus;
	}
	
	/**
	 * 设置是否已经连接重试
	 * 
	 * @param retred
	 */
	public void setRetred(boolean retred) {
		this.mRetred = retred;
	}
	
	/**
	 * 获取下载大小
	 * 
	 * @return
	 */
	public long getDownloadSize() {
		return mDownloadSize;
	}

	/**
	 * 获取文件大小
	 * 
	 * @return
	 */
	public long getFileSize() {
		return mFileSize;
	}
	
	/**
	 * 获取DownloadData
	 * 
	 * @return
	 */
	public DownloadData getDownloadData() {
		return mDownloadData;
	}
	
	/**
	 * 获取任务创建时间
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return mCreateTime;
	}
	
	/**
	 * 获取每秒速度（单位：字节）
	 * 
	 * @return
	 */
	public long getSpeed() {
		return mSpeed;
	}
	

	/**
	 * 下载文件
	 * 
	 */
	public void downloadFile() {
		setStatus(STATUS_WAIT);
		
		ThreadPoolExecutor threadPool = DownloadManager.getThreadPoolExecutor();
		threadPool.execute(this);
	}
	
	/**
	 * 暂停下载
	 * 
	 */
	public void pause() {
		Log.i(TAG, "call pause");
        if (mStatus == STATUS_CONNECTING || mStatus == STATUS_DEFAULT) {
			mPreparePause = true;
		} else {
			mPreparePause = false;
		}
		
		mStatus = STATUS_PAUSE;
		mDbController.updateStatus(mDownloadData.downloadId, mStatus);
		
		ThreadPoolExecutor threadPool = DownloadManager.getThreadPoolExecutor();
		threadPool.remove(this);
	}

    /**
     * 关机停止下载
     */
    public void shutdownPause() {
        mDbController.updateStatus(mDownloadData.downloadId, STATUS_PAUSE);
        mDbController.updateDownloadSize(mDownloadData.downloadId, mDownloadSize);

        if (mStatus == STATUS_CONNECTING || mStatus == STATUS_DEFAULT) {
            mPreparePause = true;
        } else {
            mPreparePause = false;
        }
        mStatus = STATUS_PAUSE;
    }

	/**
	 * 取消任务下载, 并把文件删除
	 * 
	 */
	public void cancel() {
		
		mStatus = STATUS_PAUSE;
		mCancleFlag = true;
		
		ThreadPoolExecutor threadPool = DownloadManager.getThreadPoolExecutor();
		threadPool.remove(this);
		mDbController.delete(mDownloadData.downloadId);
		new Thread() {
			@Override
			public void run() {
				FileUtils.deleteFile(mSaveFile);
			}
		}.start();
		if (mType == TYPE_NORMAL) {
			Theme theme = (Theme) mDownloadData;
			int type = theme.type;
			switch (type) {
			case Theme.TYPE_RINGTONG:
				RingtongDownloadService.getDownloaders(mContext).remove(mDownloadData.downloadId);
				break;
			case Theme.TYPE_THEME_PKG:
				DownloadService.getDownloaders(mContext).remove(mDownloadData.downloadId);
			break;
			case Theme.TYPE_TIME_WALLPAPER:
				TimeWallpaperDownloadService.getDownloaders(mContext).remove(mDownloadData.downloadId);
				break;
			case Theme.TYPE_WALLPAPER:
				WallpaperDownloadService.getDownloaders(mContext).remove(mDownloadData.downloadId);
				break;
			default:
				DownloadService.getDownloaders(mContext).remove(mDownloadData.downloadId);
				break;
			}
			
		} else {
			AutoUpdateService.getDownloaders().remove(mDownloadData.downloadId);
		}
	}
	
	
	public boolean unzipTimeWallpaper(Theme theme){

		DbControl dbc = new DbControl(mContext);
		dbc.openDb(mContext);
		PictureGroupInfo picGroup = dbc.queryGroupByDownloadId(theme.downloadId);
		if(picGroup != null){
			
			String groupName = picGroup.getDisplay_name();
			if(!TextUtils.isEmpty(groupName)){
				LockScreenApplyHelper.applyTimeWallpaperExists(mContext, groupName);
				return true;
			}
		}
		Log.d("path", "fileName:"+theme.fileName);
		
		ArrayList<InputStream> images = new ArrayList<InputStream>();
		String fileName = File.separatorChar+mFileName.substring(0,mFileName.lastIndexOf("."));
		theme.fileName = mFileName.substring(0,mFileName.lastIndexOf("."));
	    LockScreenApplyHelper helper = new LockScreenApplyHelper(mContext,theme);
				
		File themePkgFile = new File(mFileSaveDirStr+fileName);
		if(!themePkgFile.exists()){
			return false;
		}
		
		try {
			TimeWallpaperZip zip = new TimeWallpaperZip(themePkgFile);
			if(zip.getSize() <= 0){
				return false;
			}
			
			List<ZipEntry> entries = zip.getEntries();
			
			if(entries.size() > 0){
				for(ZipEntry entry:entries){
					InputStream input = zip.getInputStream(entry);
					if(input != null){
						images.add(input);
					}
				}
			}else{
				return false;
			}
			
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return helper.applyTimeWallpaper(images);
	}
	
	
	/**
	* @ClassName: GetSpeedThread
	* @Description: 获取及时速度
	*
	 */
	private class GetSpeedThread extends Thread {
		@Override
		public void run() {
			mSpeed = 0;
			while (mGetSpeedThreadRun) {
				long lastSize = mDownloadSize;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mSpeed = mDownloadSize - lastSize;
			}
			Log.i(TAG, "GetSpeedThread end");
		}
	}
	
	
	class TimeWallpaperZip extends ZipFile{

		ArrayList<ZipEntry> cache =   new ArrayList<ZipEntry>();
		
		public TimeWallpaperZip(File file) throws ZipException, IOException {
			super(file);
			cache.clear();
			// TODO Auto-generated constructor stub
			try {
				ZipFile zipFile = new ZipFile(file);
				Enumeration<ZipEntry> zipEnumeration = (Enumeration<ZipEntry>) zipFile.entries();
				ZipEntry entry = null;
				while (zipEnumeration.hasMoreElements()) {
					entry = zipEnumeration.nextElement();
					if (!entry.isDirectory()) {
						cache.add(entry);
					}
				}

			} catch (Exception ex) {
				Log.d(TAG, "open theme zip file exception-->" + ex);
			}
		}
		
		public List<ZipEntry> getEntries(){
	
			return cache;
		}
		
		public int getSize(){
			return cache.size();
		}
		
		
	}
	
	
}
