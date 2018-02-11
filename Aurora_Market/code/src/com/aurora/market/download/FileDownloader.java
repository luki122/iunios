package com.aurora.market.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import android.text.TextUtils;
import android.util.Log;

import com.aurora.market.db.AppDownloadDao;
import com.aurora.market.model.DownloadData;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.service.AutoUpdateService;
import com.aurora.market.util.FileLog;
import com.aurora.market.util.SystemUtils;

public class FileDownloader implements Runnable {
	
	public static final String TAG = "FileDownloader";
	
	public static final int TYPE_NORMAL = 0;	// 普通下载类型
	public static final int TYPE_AUTO_UPDATE = 1;	// 自动更新类型

	public static final int STATUS_DEFAULT = 0; // 默认
	public static final int STATUS_WAIT = STATUS_DEFAULT + 1; // 等待
	public static final int STATUS_CONNECTING = STATUS_WAIT + 1; // 连接
	public static final int STATUS_DOWNLOADING = STATUS_CONNECTING + 1; // 正在下载
	public static final int STATUS_PAUSE = STATUS_DOWNLOADING + 1; // 暂停
	public static final int STATUS_PAUSE_NEED_CONTINUE = STATUS_PAUSE + 1; // 暂停需要继续
	public static final int STATUS_NO_NETWORK = STATUS_PAUSE_NEED_CONTINUE + 1; // 暂无网络
	public static final int STATUS_CONNECT_TIMEOUT = STATUS_NO_NETWORK + 1; // 连接超时
	public static final int STATUS_CONNECT_RETRY = STATUS_CONNECT_TIMEOUT + 1; // 重试
	public static final int STATUS_FAIL = STATUS_CONNECT_RETRY + 1; // 下载失败
	public static final int STATUS_INSTALL_WAIT = STATUS_FAIL + 1; // 安装等待
	public static final int STATUS_INSTALLING = STATUS_INSTALL_WAIT + 1; // 正在安装
	public static final int STATUS_INSTALLFAILED = STATUS_INSTALLING + 1; // 安装失败
	public static final int STATUS_INSTALLED = STATUS_INSTALLFAILED + 1; // 安装完成
	
//	private int time_interval = 800;
	
	private int type = TYPE_NORMAL;
	
	private DownloadData downloadData; // 下载数据
	private String downloadUrl; // 下载路径
	private long downloadSize = 0; // 已下载文件长度
	private long fileSize = 0; // 原始文件长度
	private String fileSaveDirStr; // 文件目录
	private String fileName; // 文件名
	private File saveFile; // 本地保存文件
	private long createTime = 0; // 任务创建时间
	
	private AppDownloadDao dao;
	
	private DownloadStatusListener listener;	// 下载状态监听器
	
	private int status = STATUS_DEFAULT; // 当前状态

	private boolean cancleFlag = false; // 取消标识
	private boolean retred = false; // 是否已经连接重试
	private boolean needRetry = false;
	private boolean preparePause = false; // 是否在准备阶段点下的暂停
	
	private long speed = 0;
	private boolean getSpeedThreadRun = false;
	
	/**
	 * 构建文件下载器
	 * 
	 * @param downloadData 下载信息
	 * @param fileSaveDir 文件保存目录
	 */
	public FileDownloader(DownloadData downloadData,
			File fileSaveDir, DownloadStatusListener listener, int type) {
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
			File fileSaveDir, DownloadStatusListener listener, int type) {
		Log.i(TAG, "FileDownloader init: id->" + downloadData.getApkId() 
				+ " name->" + downloadData.getApkName()
				+ " packageName->" + downloadData.getPackageName());
		
		this.downloadData = downloadData;
		this.listener = listener;
		downloadUrl =downloadData.getApkDownloadPath();//"http://apps.wandoujia.com/redirect?signature=0e6c488&url=http%3A%2F%2Fapk.wandoujia.com%2Fc%2Fbc%2Fee195f1c791d45b304c0e9887a250bcc.apk&pn=com.wandoujia.roshan&md5=ee195f1c791d45b304c0e9887a250bcc&apkid=11431984&vc=100&size=5831467&pos=t/search/list//%E9%94%81%E5%B1%8F/1/normal&tokenId=iuni&appType=APP";// //downloadData.getApkDownloadPath();
		String down_type= "";
		if (type == TYPE_NORMAL)
		{
			down_type = "download";
		}
		else
		{
			down_type = "update";
		}
		
		String wandoujia_data = "&download_type="+down_type+"&phone_imei="+SystemUtils.getWandoujia_Imei()
				+"&mac_address="+SystemUtils.getWandoujia_MacAddress()+"&phone_model="+SystemUtils.getModelNumber();
		
		if(!downloadUrl.contains("iunios"))
			downloadUrl += wandoujia_data;
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdirs();
		}
		this.fileSaveDirStr = fileSaveDir.getPath();
		this.type = type;
		if (type == TYPE_NORMAL) {
			this.dao = AppDownloadService.getAppDownloadDao();
		} else {
			this.dao = AutoUpdateService.getAutoUpdateDao();
		}
		
		if (dao != null) {
			// 存在记录
			if (dao.isExist(downloadData.getApkId())) {
				DownloadData dataFromDb = dao.getDownloadData(downloadData.getApkId());
				// 同一APK下载地址不一致情况，或者版本号不一样的情况，删除数据库记录及文件
				if (!dataFromDb.getApkDownloadPath()
						.equals(downloadData.getApkDownloadPath())
						|| dataFromDb.getVersionCode() != downloadData.getVersionCode()) {
					dao.delete(downloadData.getApkId());
					File f = new File(dataFromDb.getFileDir(), dataFromDb.getFileName());
					f.delete();
				}
			}
			
			if (dao.isExist(downloadData.getApkId())) {
				fileName = dao.getFileName(downloadData.getApkId());
				if (!TextUtils.isEmpty(fileName)) {
					saveFile = new File(this.fileSaveDirStr, fileName); 	// APK保存文件
				}
				
				// 任务已存在，但是文件被删除
				if (saveFile == null || !saveFile.exists()) {
					dao.delete(downloadData.getApkId());
					createNewRecord();
				} else {
					downloadSize = dao.getDownloadSize(downloadData.getApkId());
					fileSize = dao.getFileSize(downloadData.getApkId());
					fileName = dao.getFileName(downloadData.getApkId());
					status = dao.getStatus(downloadData.getApkId());
					createTime = dao.getCreateTime(downloadData.getApkId()); // 获取创建时间
				}
			} else {
				createNewRecord();
			}
		}
	}
	
	/**
	 * 增加新记录
	 * 
	 */
	private void createNewRecord() {
		Log.i(TAG, "createNewRecord()");
		createTime = System.currentTimeMillis();
		fileName = getFileName();
		saveFile = null;
		dao.insert(downloadData, createTime, status, fileSize);
		dao.updateFileDirAndName(downloadData.getApkId(), fileSaveDirStr, fileName);
	}
	
	/**
	 * 开始下载
	 * 
	 */
	private void startDownload() {
		Log.i(TAG, downloadData.getApkName() + "->startDownload()");
		
		speed = 0;
		needRetry = false;
		
		if (cancleFlag)
			return;
		
		if (status == STATUS_PAUSE || status == STATUS_NO_NETWORK)
			return;
		
		// 没有获取过文件大小
		//FileLog.i(TAG, downloadData.getApkName() + "->startDownload()");
		if (fileSize <= 0) {
			connectToGetFileSize();
		}
		
		// 如果获取回来的文件大小小于等于0，则表示失败
		if (fileSize <= 0) {
			if (SystemUtils.hasNetwork()) {
				setStatus(STATUS_FAIL);
			} else {
				setStatus(STATUS_NO_NETWORK);
			}
			dao.updateStatus(downloadData.getApkId(), status);
			
			return;
		}
		//FileLog.i(TAG, "start download1");
		// 如果是在准备阶段点下的暂停, 则不进行下载, 直接return
		if (preparePause) {
			preparePause = false;
			setStatus(STATUS_PAUSE);
			return;
		}
		
		if (saveFile == null) {
			saveFile = new File(fileSaveDirStr, fileName);
		}
        if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				FileLog.e(TAG, e.toString());
				e.printStackTrace();
			}
		}
		
		setStatus(STATUS_CONNECTING);
		
		RandomAccessFile accessFile = null;
		URL url = null;
		HttpURLConnection http = null;
		InputStream inputStream = null;
		//FileLog.i(TAG, "start download2");
		try {
	
			url = new URL(downloadUrl);
			Log.i(TAG, "zhangwei the downloadUrl= " + downloadUrl);
			http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(8 * 1000);
			http.setDoOutput(true);
			http.setRequestMethod("POST");
			http.setRequestProperty("Range", "bytes=" + downloadSize + "-" + (fileSize-1));
			http.connect();
			int code = http.getResponseCode();
			
			accessFile = new RandomAccessFile(saveFile, "rw");
			accessFile.seek(downloadSize);
			
			// 注：加入断点下载返回码为206
			if (code == HttpStatus.SC_OK || code == HttpStatus.SC_PARTIAL_CONTENT)
					/*&& oldHost.equals(targetHost.getHostName())*/ {
				inputStream = http.getInputStream();
				byte[] buffer = new byte[4096];
				int offset = 0;
				//FileLog.i(TAG, "start download3");
				// 用于计算下载速度
				getSpeedThreadRun = true;
				new GetSpeedThread().start();
				
				while ((offset = inputStream.read(buffer)) != -1) {
					if (cancleFlag) {
						break;
					}
					
					if (status == FileDownloader.STATUS_PAUSE
							|| status == FileDownloader.STATUS_NO_NETWORK
							|| status == FileDownloader.STATUS_CONNECT_TIMEOUT) {
						break;
					}
					
					if (status != FileDownloader.STATUS_DOWNLOADING) {
						setStatus(FileDownloader.STATUS_DOWNLOADING);
					}
					
					if (downloadSize >= fileSize) {
						break;
					}
					
					accessFile.write(buffer, 0, offset);
					downloadSize += offset;
					
					setRetred(false);
					//FileLog.i(TAG, "downloadSize: " + downloadSize);
//					Log.i(TAG, "downloadSize: " + downloadSize);
				}
			} else {
				//FileLog.i(TAG, "start download4");
				if (SystemUtils.hasNetwork()) {
					setStatus(STATUS_FAIL);
					//FileLog.i(TAG, "start download5");
				} else {
					setStatus(STATUS_NO_NETWORK);
					//FileLog.i(TAG, "start download6");
				}
			}
		} catch (ClientProtocolException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			if (e instanceof ConnectTimeoutException) {
				setStatus(STATUS_CONNECT_TIMEOUT);
				if (!retred) {
					retred = true;
					setStatus(STATUS_CONNECT_RETRY);
					needRetry = true;
				} else {
					setRetred(false);
					setStatus(STATUS_FAIL);
				}
			} else {
//				if (e instanceof ConnectException || 
//						e instanceof SocketTimeoutException || 
//						e instanceof UnknownHostException) {
//				}
				if (SystemUtils.hasNetwork()) {
					if (!retred) {
						retred = true;
						setStatus(STATUS_CONNECT_RETRY);
						needRetry = true;
					} else {
						setRetred(false);
						setStatus(STATUS_FAIL);
					}
				} else {
					setStatus(STATUS_NO_NETWORK);
				}
			}
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} finally {
			getSpeedThreadRun = false;
			//FileLog.i(TAG, "start download7");
			http.disconnect();
			
			try {
				if (accessFile != null) {
					accessFile.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				FileLog.e(TAG, e.toString());
				e.printStackTrace();
			}
		}
		
		if (needRetry) {
			//FileLog.i(TAG, "start download8");
			startDownload();
			return;
		}
		
		// 如果已经下载完成, 删除正在下载数据库中数据
		if (downloadSize >= fileSize) {
            File finishFile = new File(fileSaveDirStr, fileName);
            finishFile.renameTo(new File(fileSaveDirStr,
                    fileName.substring(0, fileName.lastIndexOf("."))));
            dao.updateFileName(downloadData.getApkId(),
                    fileName.substring(0, fileName.lastIndexOf(".")));

			setStatus(STATUS_INSTALL_WAIT);
			dao.updateFileFinishTime(downloadData.getApkId(), System.currentTimeMillis());
		}
		
		dao.updateStatus(downloadData.getApkId(), status);
		dao.updateDownloadSize(downloadData.getApkId(), downloadSize);
		
		if (listener != null) {
			listener.onDownload(downloadData.getApkId(), status, downloadSize, fileSize);
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
			url = new URL(downloadUrl);
			http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(8 * 1000);
			http.setRequestMethod("POST");
			http.connect();
			int code = http.getResponseCode();
			
			if (code == HttpStatus.SC_OK 
					/*&& oldHost.equals(targetHost.getHostName())*/) {
				
				fileSize = http.getContentLength();
				//FileLog.i(TAG, "getFileSize: " + fileSize);
				
				dao.updateFileSize(downloadData.getApkId(), fileSize);
			}
		} catch (ClientProtocolException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} finally {
			http.disconnect();
		}
		
		return fileSize;
	}
	
	/**
	 * 获取文件名
	 * 
	 */
	private String getFileName() {
		/*String filename = downloadUrl.substring(this.downloadUrl
				.lastIndexOf('/') + 1);
		if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
			filename = downloadData.getApkName() + "_" + downloadData.getVersionName() + ".apk";
		}*/
		
		/*
		String filename = "";
		int start = downloadUrl.indexOf("md5=");
		if (start != -1) {
			int end = downloadUrl.indexOf("&", start);
			filename = downloadUrl.substring(start + 4, end) + ".apk";
		} else {
			filename = downloadData.getApkName() + "_"
					+ downloadData.getVersionName() + ".apk";
		}
		*/
		
		String filename = downloadData.getApkName() + "_" 
				+ SystemUtils.getTimeString(createTime) + ".apk" + ".tmp";
		
		return filename;
	}
	
	/**
	 * 设置下载状态
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
		dao.updateStatus(downloadData.getApkId(), status);
	}

	/**
	 * 获取下载任务的状态
	 * 
	 * @return
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * 设置是否已经连接重试
	 * 
	 * @param retred
	 */
	public void setRetred(boolean retred) {
		this.retred = retred;
	}
	
	/**
	 * 获取下载大小
	 * 
	 * @return
	 */
	public long getDownloadSize() {
		return downloadSize;
	}

	/**
	 * 获取文件大小
	 * 
	 * @return
	 */
	public long getFileSize() {
		return fileSize;
	}
	
	/**
	 * 获取DownloadData
	 * 
	 * @return
	 */
	public DownloadData getDownloadData() {
		return downloadData;
	}
	
	/**
	 * 获取任务创建时间
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return createTime;
	}
	
	/**
	 * 获取每秒速度（单位：字节）
	 * 
	 * @return
	 */
	public long getSpeed() {
		return speed;
	}
	
	//============对外控制方法开始=============//

	/**
	 * 下载文件
	 * 
	 */
	public void downloadFile() {
		setStatus(STATUS_WAIT);
		
		ThreadPoolExecutor threadPool = FileDownloadManage.getThreadPoolExecutor();
		threadPool.execute(this);
	}
	
	/**
	 * 暂停下载
	 * 
	 */
	public void pause() {
		Log.i(TAG, "call pause");
        if (status == STATUS_CONNECTING || status == STATUS_DEFAULT) {
			preparePause = true;
		} else {
			preparePause = false;
		}
		
		status = STATUS_PAUSE;
		dao.updateStatus(downloadData.getApkId(), status);
		
		ThreadPoolExecutor threadPool = FileDownloadManage.getThreadPoolExecutor();
		threadPool.remove(this);
	}

    /**
     * 关机停止下载
     */
    public void shutdownPause() {
        dao.updateStatus(downloadData.getApkId(), STATUS_PAUSE);
        dao.updateDownloadSize(downloadData.getApkId(), downloadSize);

        if (status == STATUS_CONNECTING || status == STATUS_DEFAULT) {
            preparePause = true;
        } else {
            preparePause = false;
        }
        status = STATUS_PAUSE;
    }

	/**
	 * 取消任务下载, 并把文件删除
	 * 
	 */
	public void cancel() {
		Log.i(TAG, "cancel " + downloadData.getApkName() + " type " + type);
		
		status = STATUS_PAUSE;
		cancleFlag = true;
		
		ThreadPoolExecutor threadPool = FileDownloadManage.getThreadPoolExecutor();
		threadPool.remove(this);
		dao.delete(downloadData.getApkId());
		new Thread() {
			@Override
			public void run() {
				FileUtil.deleteFile(saveFile);
			}
		}.start();
		if (type == TYPE_NORMAL) {
			AppDownloadService.getDownloaders().remove(downloadData.getApkId());
		} else {
			AutoUpdateService.getDownloaders().remove(downloadData.getApkId());
		}
	}
	
	//============对外控制方法结束=============//
	
	/**
	* @ClassName: GetSpeedThread
	* @Description: 获取及时速度
	* @author Linxiaobin
	* @date 2014-6-6 上午11:33:53
	*
	 */
	private class GetSpeedThread extends Thread {
		@Override
		public void run() {
			speed = 0;
			while (getSpeedThreadRun) {
				long lastSize = downloadSize;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					FileLog.e(TAG, e.toString());
					e.printStackTrace();
				}
				speed = downloadSize - lastSize;
			}
			Log.i(TAG, "GetSpeedThread end");
		}
	}

}
