package com.aurora.account.download;

import android.content.Context;
import android.text.TextUtils;

import com.aurora.account.bean.DownloadDataResult;
import com.aurora.account.bean.accessoryInfo;
import com.aurora.account.bean.accessoryObj;
import com.aurora.account.bean.syncDataItemObject;
import com.aurora.account.bean.syncDataObject;
import com.aurora.account.contentprovider.AccountsAdapter;
import com.aurora.account.db.ExtraFileDownloadDao;
import com.aurora.account.service.ExtraFileUpService;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.FileLog;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

public class FileDownloader implements Runnable {
	
	public static final String TAG = "FileDownloader";
	
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
	public static final int STATUS_FINISH = STATUS_FAIL + 1; // 下载完成
	
	private Context m_context;
	private String m_packageName;
	private String m_uri;
	private accessoryInfo m_accessoryInfo;
	private long downloadSize = 0; // 已下载文件长度
	private long fileSize = 0; // 原始文件长度
	private long createTime = 0; // 任务创建时间
	
	private ExtraFileDownloadDao dao;
	
	private DownloadStatusListener listener;	// 下载状态监听器
	
	private int status = STATUS_DEFAULT; // 当前状态

	private boolean cancleFlag = false; // 取消标识
	private boolean retred = false; // 是否已经连接重试
	private boolean needRetry = false;
	private boolean preparePause = false; // 是否在准备阶段点下的暂停
	
	private long speed = 0;
	private boolean getSpeedThreadRun = false;
	
	private CountDownLatch countdown;
	
	private DownloadDataResult result;
	
	/**
	 * 构建文件下载器
	 * 
	 * @param downloadData 下载信息
	 * @param fileSaveDir 文件保存目录
	 */
	public FileDownloader(String packageName,String uri, accessoryInfo accinfo,
			DownloadStatusListener listener, Context context, CountDownLatch countdown) {
		init(packageName, uri, accinfo, listener, context, countdown);
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
	private void init(String packageName,String uri, accessoryInfo accinfo,
			DownloadStatusListener listener, Context context, CountDownLatch countdown) {
		Log.i(TAG, "FileDownloader init: syncId->" + accinfo.getSyncid()
				+ " syncId->" + accinfo.getSyncid());
		
		this.m_context = context;
		this.m_packageName = packageName;
		this.m_uri = uri;
		this.m_accessoryInfo = accinfo;
//		m_accessoryInfo.setType("providerFile");
		this.listener = listener;
		this.countdown = countdown;
		this.dao = ExtraFileUpService.getExtraFileDownloadDao();
		
		if (dao != null) {
			// 存在记录
			if (!TextUtils.isEmpty(accinfo.getSyncid()) && dao.isExist(accinfo.getSyncid())) {
				result = dao.getDownloadDataResult(accinfo.getSyncid());
				
				m_accessoryInfo.setNew_path(result.getPath());
				udpateSyncData(result.getPath(), accinfo.getSyncid());
			} else {
				
				createTime = System.currentTimeMillis();
				
				AccountsAdapter db = new AccountsAdapter(m_context, m_packageName, m_uri);
				String newPath = db.getNewPath(packageName, accinfo.getSyncid(), accinfo.getPath());
				m_accessoryInfo.setNew_path(newPath);
				udpateSyncData(newPath, accinfo.getSyncid());
				
				result = new DownloadDataResult();
				result.setPath(newPath);
				result.setSyncId(accinfo.getSyncid());
				result.setPackage_name(packageName);
				result.setStatus(getStatus());
				result.setCreate_time(createTime);
				result.setFinish_time(0);
				result.setFile_size(0);
				result.setDownloadSize(0);
				
				createNewRecord();
			}
		}
		
	}
	
	private void udpateSyncData(String newPath, String syncId) {
		syncDataObject dataObj = ExtraFileUpService.getModule_obj();
		List<syncDataItemObject> itemObjects = dataObj.getSycndata();
		
		accessoryObj accessoryObj = null;
		ArrayList<accessoryInfo> infoes = null;
		
		for (int i = 0; i < itemObjects.size(); i++) {
			accessoryObj = itemObjects.get(i).getAccOjb();
			if (accessoryObj != null) {
				infoes = accessoryObj.getAccessory();
				if (infoes != null) {
					for (int j = 0; j < infoes.size(); j++) {
						if (infoes.get(j).getSyncid() != null &&
								infoes.get(j).getSyncid().equals(syncId)) {
							infoes.get(j).setNew_path(newPath);
						}
					}
				}
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
		dao.insert(result);
	}
	
	/**
	 * 开始下载
	 * 
	 */
	private void startDownload() {
		Log.i(TAG, m_accessoryInfo.getSyncid() + "->startDownload()");
		FileLog.i(TAG, m_accessoryInfo.getSyncid() + "->startDownload()");
		speed = 0;
		needRetry = false;
		
		if (cancleFlag)
			return;
		
		if (status == STATUS_PAUSE || status == STATUS_NO_NETWORK
				|| status == STATUS_FINISH)
			return;
		
		// 没有获取过文件大小
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
			dao.updateStatus(m_accessoryInfo.getSyncid(), status);
			
			return;
		}
		
		// 如果是在准备阶段点下的暂停, 则不进行下载, 直接return
		if (preparePause) {
			preparePause = false;
			setStatus(STATUS_PAUSE);
			return;
		}
		
		setStatus(STATUS_CONNECTING);
		
//		RandomAccessFile accessFile = null;
		HttpURLConnection http = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		AccountsAdapter db = new AccountsAdapter(m_context, m_packageName, m_uri);
		outputStream = db.getOutputStreamByAccessory(m_packageName, m_accessoryInfo);
		
		if (outputStream == null) {
			
			Log.e(TAG, "syncId: " + m_accessoryInfo.getSyncid() + " outputStream is NULL");
			FileLog.e(TAG, "syncId: " + m_accessoryInfo.getSyncid() + " outputStream is NULL");
			setStatus(STATUS_FAIL);
			
		} else {
			
			try {
				
				http = getHttpURLConnection(true);
				http.connect();
				int code = http.getResponseCode();
				
//				accessFile = new RandomAccessFile(saveFile, "rw");
//				accessFile.seek(downloadSize);
				
				// 注：加入断点下载返回码为206
				if (code == HttpStatus.SC_OK || code == HttpStatus.SC_PARTIAL_CONTENT)
						/*&& oldHost.equals(targetHost.getHostName())*/ {
					
					inputStream = http.getInputStream();
					byte[] buffer = new byte[4096];
					int offset = 0;
					
					// 用于计算下载速度
//					getSpeedThreadRun = true;
//					new GetSpeedThread().start();
					
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
							setRetred(false);
						}
						
						if (downloadSize >= fileSize) {
							break;
						}
						
						outputStream.write(buffer, 0, offset);
//						accessFile.write(buffer, 0, offset);
						downloadSize += offset;
						
//						Log.i(TAG, "downloadSize: " + downloadSize);
					}
				} else {
					if (SystemUtils.hasNetwork()) {
						setStatus(STATUS_FAIL);
					} else {
						setStatus(STATUS_NO_NETWORK);
					}
				}
			} catch (ClientProtocolException e) {
				FileLog.e(TAG, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				if (e instanceof ConnectTimeoutException || e instanceof ConnectException || 
						e instanceof SocketTimeoutException || 
						e instanceof UnknownHostException) {
					setStatus(STATUS_CONNECT_TIMEOUT);
					if (!retred) {
						retred = true;
						setStatus(STATUS_CONNECT_RETRY);
						needRetry = true;
					} else {
						setRetred(false);
						setStatus(STATUS_FAIL);
						FileLog.e(TAG, "the failed syncid="+m_accessoryInfo.getSyncid()+" the failed type ="+m_accessoryInfo.getType());
						//ExtraFileUpService.PauseDownloadAcc();
						ExtraFileUpService.pauseOperation(m_context,0);
					}
				} else {
//					if (e instanceof ConnectException || 
//							e instanceof SocketTimeoutException || 
//							e instanceof UnknownHostException) {
//					}
					if (SystemUtils.hasNetwork()) {
						if (!retred) {
							retred = true;
							setStatus(STATUS_CONNECT_RETRY);
							needRetry = true;
						} else {
							setRetred(false);
							setStatus(STATUS_FAIL);
							FileLog.e(TAG, "the failed syncid="+m_accessoryInfo.getSyncid()+" the failed type ="+m_accessoryInfo.getType());
							ExtraFileUpService.PauseDownloadAcc();
						}
					} else {
						setStatus(STATUS_NO_NETWORK);
					}
				}
				FileLog.e(TAG, e.toString());
				e.printStackTrace();
			} finally {
				getSpeedThreadRun = false;
				
				if (http != null) {
					http.disconnect();
				}
				
				try {
//					if (accessFile != null) {
//						accessFile.close();
//					}
					if (outputStream != null) {
						outputStream.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (IOException e) {
					FileLog.e(TAG, e.toString());
					e.printStackTrace();
				}
			}
			
		}
		
		if (needRetry) {
			startDownload();
			return;
		}
		
		// 如果已经下载完成, 删除正在下载数据库中数据
		if (downloadSize >= fileSize) {
			setStatus(STATUS_FINISH);
			dao.updateFileFinishTime(m_accessoryInfo.getSyncid(), System.currentTimeMillis());
		}
		
		dao.updateStatus(m_accessoryInfo.getSyncid(), status);
		dao.updateDownloadSize(m_accessoryInfo.getSyncid(), downloadSize);
		
		if (listener != null) {
			listener.onDownload(m_accessoryInfo.getSyncid(), status, downloadSize, fileSize);
		}
		
		countdown.countDown();
		
		Log.i(TAG, "downloaded: " + fileSize + ", fileSize: " + fileSize);
		Log.i(TAG, m_accessoryInfo.getSyncid() + "->startDownload() end");
		
		FileLog.i(TAG, "downloaded: " + fileSize + ", fileSize: " + fileSize);
		FileLog.i(TAG, m_accessoryInfo.getSyncid() + "->startDownload() end");
	}
	
	private HttpURLConnection getHttpURLConnection(boolean hasRange) throws IOException {
		URL url = null;
		HttpURLConnection http = null;
		
		url = new URL(getDownloadUrl());
		http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setConnectTimeout(10000);
        if (hasRange) {
        	http.setRequestProperty("Range", "bytes=" + downloadSize + "-" + fileSize);
        }
        http.setRequestProperty("connection", "keep-alive");
        http.setRequestProperty("Charset", "UTF-8");
        http.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");

		StringBuilder entityBuilder = new StringBuilder("");
		
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		AccountPreferencesUtil pref = AccountPreferencesUtil.getInstance(m_context);
		map.put("userId", pref.getUserID());
		map.put("userKey", pref.getUserKey());
//		map.put("userId", "12345678");
//		map.put("userKey", "7ab6db578e7ef80f00f1433f39bdf174");
//		map.put("part", "sms");
//		map.put("recId", "100001");

		mapper.writeValue(str, map);
		
		entityBuilder.append("parmJson=").append(str.toString());

		OutputStream outStream = http.getOutputStream();
		outStream.write(entityBuilder.toString().getBytes());
		outStream.flush();
		
		return http;
	}
	
	/**
	 * 连接并获取到文件大小
	 * 
	 * @throws IOException
	 */
	private long connectToGetFileSize() {
		setStatus(STATUS_CONNECTING);
		
		HttpURLConnection http = null;
		
		try {
			http = getHttpURLConnection(false);
			
			int code = http.getResponseCode();
			
			if (code == HttpStatus.SC_OK 
					/*&& oldHost.equals(targetHost.getHostName())*/) {
				
				fileSize = http.getContentLength();
				Log.i(TAG, "getFileSize: " + fileSize);
				
				dao.updateFileSize(m_accessoryInfo.getSyncid(), fileSize);
			}
		} catch (ClientProtocolException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (http != null) {
				http.disconnect();
			}
		}
		
		return fileSize;
	}
	
	private String getDownloadUrl() {
//		return "http://dev.ucloud.iunios.com/sync?module=attachment&action=download&attachId=100010";
//		return "http://dev.ucloud.iunios.com/account?module=attachment&action=download&attachId=100010";
//		return "http://dev.ucloud.iunios.com/sync?module=attachment&action=download&attachId=" + result.getSyncId();
		return m_accessoryInfo.getAccessoryurl();
	}
	
	/**
	 * 设置下载状态
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
		dao.updateStatus(m_accessoryInfo.getSyncid(), status);
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
	
	public accessoryInfo getAccessoryInfo() {
		return m_accessoryInfo;
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
		Log.i(TAG, m_accessoryInfo.getSyncid() + "->pause()");
		
		if (status == STATUS_CONNECTING || status == STATUS_DEFAULT) {
			preparePause = true;
		} else {
			preparePause = false;
		}
		
		status = STATUS_PAUSE;
		dao.updateStatus(m_accessoryInfo.getSyncid(), status);
		
		ThreadPoolExecutor threadPool = FileDownloadManage.getThreadPoolExecutor();
		threadPool.remove(this);
	}
	
	/**
	 * 取消任务下载, 并把文件删除
	 * 
	 */
	public void cancel() {
		status = STATUS_PAUSE;
		cancleFlag = true;
		
		ThreadPoolExecutor threadPool = FileDownloadManage.getThreadPoolExecutor();
		threadPool.remove(this);
		dao.delete(m_accessoryInfo.getSyncid());
//		new Thread() {
//			@Override
//			public void run() {
//				FileUtil.deleteFile(saveFile);
//			}
//		}.start();
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
