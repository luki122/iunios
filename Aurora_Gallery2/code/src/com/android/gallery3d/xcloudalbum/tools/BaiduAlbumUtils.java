package com.android.gallery3d.xcloudalbum.tools;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.R.layout;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.gallery3d.app.AlbumPage.state;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.inter.IBaiduTaskListener;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.android.gallery3d.xcloudalbum.inter.IOperationComplete;
import com.android.gallery3d.xcloudalbum.tools.MediaFile.MediaFileType;
import com.android.gallery3d.xcloudalbum.tools.cache.http.HttpCacheManager;
import com.android.gallery3d.xcloudalbum.tools.cache.image.FileCache;
import com.android.gallery3d.xcloudalbum.tools.cache.image.LruMemoryCache;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader.ImageProcessingCallback;
import com.android.gallery3d.xcloudalbum.uploaddownload.FakeTaskManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudTaskListenerManager;
import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.account.AuthInfo;
import com.baidu.xcloud.account.IAuthExpireListener;
import com.baidu.xcloud.pluginAlbum.AbstractAlbumListener;
import com.baidu.xcloud.pluginAlbum.AccountProxy;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.IAlbumListener;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.AccountProxy.IAccountInfoListener;
import com.baidu.xcloud.pluginAlbum.AlbumClientProxy;
import com.baidu.xcloud.pluginAlbum.bean.AsyncTaskBaseBean;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.DiffResponse;
import com.baidu.xcloud.pluginAlbum.bean.DifferEntryInfo;
import com.baidu.xcloud.pluginAlbum.bean.ErrorCode;
import com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.MetaResponse;
import com.baidu.xcloud.pluginAlbum.bean.QuotaResponse;
import com.baidu.xcloud.pluginAlbum.bean.SimpleResponse;
import com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse;
import com.android.gallery3d.R;

import java.util.HashMap;

import android.content.ContentValues;
import android.provider.MediaStore.Images;
import android.content.Intent;

import com.android.gallery3d.util.NetworkUtil;

import android.provider.MediaStore.Images.Media;
import android.database.Cursor;

/**
 * 百度工具类
 * 
 * @author JiangXh
 * 
 */
public class BaiduAlbumUtils {
	public static final String ACTION_IMAGE_DOWNLOADED = "aurora_img_downloaded";// paul
																					// add

	private LruMemoryCache lruMemoryCache;
	private FileCache fileCache;
	private HttpCacheManager httpCacheManager;

	public HttpCacheManager getHttpCacheManager() {
		return httpCacheManager;
	}

	public void setLruMemoryCache(LruMemoryCache lruMemoryCache) {
		this.lruMemoryCache = lruMemoryCache;
	}

	public void setFileCache(FileCache fileCache) {
		this.fileCache = fileCache;
	}

	private IOperationComplete operationComplete;

	public void setOperationComplete(IOperationComplete operationComplete) {
		this.operationComplete = operationComplete;
	}

	private static final String TAG = "BaiduAlbumUtils";
	private AccountInfo mAccount;
	private String mThirdAccessToken;
	private Context mContext;
	private AlbumClientProxy mAlbumClient;
	private SharedPreferences mPref;

	public AlbumClientProxy getmAlbumClient() {
		return mAlbumClient;
	}

	private String currentPath;

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

	private IBaiduinterface baiduinterface;

	public void setBaiduinterface(IBaiduinterface baiduinterface) {
		this.baiduinterface = baiduinterface;
	}

	private IBaiduTaskListener baiduTaskListener;

	public void setBaiduTaskListener(IBaiduTaskListener baiduTaskListener) {
		this.baiduTaskListener = baiduTaskListener;
	}

	private static BaiduAlbumUtils baiduAlbumUtils;

	public static BaiduAlbumUtils getInstance(Context context) {
		if (baiduAlbumUtils == null) {
			baiduAlbumUtils = new BaiduAlbumUtils(
					context.getApplicationContext());
		}
		return baiduAlbumUtils;
	}

	private void accountProxyInit(Context context) {
		AccountProxy.getInstance().init(context, AlbumConfig.APPID,
				AlbumConfig.APIKEY, mThirdAccessToken,
				AlbumConfig.BAIDU_PERMISSIONS);
	}

	public BaiduAlbumUtils(Context mContext) {
		super();
		this.mContext = mContext;
		httpCacheManager = HttpCacheManager.getInstance(mContext);
		accountProxyInit(mContext);
	}

	/**
	 * 登录
	 * 
	 * @param forceLogin
	 */
	public void loginBaidu(String token, boolean forceLogin) {
		mThirdAccessToken = token;
		if (AlbumConfig.IUNI_TEST) {
			mThirdAccessToken = AlbumConfig.IUNI_TEST_TOKEN;
		}
		AccountProxy proxy = AccountProxy.getInstance();
		proxy.setThirdToken(mThirdAccessToken);
		proxy.getAccountInfo(loginListener, forceLogin,
				AuthInfo.AUTHTYPE_NEWEXPLICITBIND);
	}

	/**
	 * 登出
	 */
		

	public void loginOutBaidu() {
		Log.d(TAG,"loginOutBaidu");
		//paul modfiy
		final AccountProxy proxy = AccountProxy.getInstance();
		proxy.logout(new IAuthExpireListener.Stub() {

			@Override
			public void onResult(boolean result) throws RemoteException {
				if (result) {
					mAccount = null;
					//wenyongzhe 2015.12.2 auto_update_bug start
					PrefUtil.setBoolean(mContext, "ACCOUNT_BIND", false);
					//wenyongzhe 2015.12.2 auto_update_bug end
					proxy.logOutClearCache();
					// LogUtil.d(TAG, "loginOutBaidu");
				}
				Log.d(TAG,"onResult : " + result);
			}

			@Override
			public void onException(String errorMsg) throws RemoteException {
				Log.e(TAG, "loginOutBaidu errorMsg::" + errorMsg);
			}
		});
	}

	/**
	 * 第一次请求相册列表或则请求每个相册下到图片
	 * 
	 * @param path
	 * @param isDirPath
	 * @param info
	 */
	public void getFileListFromBaidu(final String path,
			final boolean isDirPath, final CommonFileInfo info) {
		if (TextUtils.isEmpty(path) || mAccount == null) {
			Log.e(TAG, "getFileListFromBaidu path or mAccount  is null::"
					+ path + " mAccount::" + mAccount);
			return;
		}
		if (isDirPath && info == null) {
			setCurrentPath(path);
			setup();
		}
		if (info != null) {
			List<CommonFileInfo> list = httpCacheManager
					.getFromCache(info.path);
			if (list != null) {
				if (baiduinterface != null) {
					baiduinterface.baiduPhotoList(list, isDirPath, info);
					return;
				}
			}
		}

		if (mAlbumClient == null) {
			Log.e(TAG, "mAlbumClient is null");
			return;
		}
		mAlbumClient.getPhotoList(mAccount, path, "time", "desc",
				new AbstractAlbumListener() {
					@Override
					public void onGetFileList(ListInfoResponse listInfoResponse) {
						String taskId = listInfoResponse.getTaskId();
						int statusType = listInfoResponse.getStatusType();
						int errorCode = listInfoResponse.getErrorCode();
						String errorMsg = listInfoResponse.getMessage();
						if (null != listInfoResponse) {
							if (ErrorCode.No_Error == errorCode) {
								if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
									List<CommonFileInfo> list = listInfoResponse.list;
									List<CommonFileInfo> temps = new ArrayList<CommonFileInfo>(
											list);
									for (CommonFileInfo commonFileInfo : temps) {
										// LogUtil.d(TAG,
										// "commonFileInfo::"+commonFileInfo.path);
										if (!commonFileInfo.isDir && isDirPath) {
											list.remove(commonFileInfo);
										} else if (!isDirPath
												&& commonFileInfo.isDir) {
											list.remove(commonFileInfo);
										} else if (!isDirPath) {
											MediaFileType fileType = MediaFile
													.getFileType(commonFileInfo.path);
											if (fileType != null
													&& !MediaFile
															.isImageFileType(fileType.fileType)) {
												list.remove(commonFileInfo);
											} else if (fileType == null) {
												list.remove(commonFileInfo);
											}

										}
									}

									if (list != null) {
										if (info != null) {
											httpCacheManager.saveCache(
													info.path, list);
										} else {
											httpCacheManager.removeCache(path);
											httpCacheManager.saveCache(path,
													list);
										}
									}
									if (baiduinterface != null) {
										Log.i("SQF_LOG", "!!!!!!!!!! getFileListFromBaidu ---- ");
										baiduinterface.baiduPhotoList(list,
												isDirPath, info);
										return;
									}

								}
							} else {
								Log.e(TAG, "getFileList failed. errorCode:"
										+ errorCode + " errorMsg:" + errorMsg);
								
								//wenyongzhe 2015.10.19 login
								onErrorException(errorCode);
							}
						}
						if (baiduinterface != null) {
							baiduinterface
									.baiduPhotoList(null, isDirPath, info);
						}

					}
				});
	}

	//wenyongzhe 2015.10.19 login
	private void onErrorException(int errorCode){
		switch(errorCode){
		case ErrorCode.Error_Invalid_Access_Token: /** 用户账号不正确或者已经失效，需要重新登录 */
		case ErrorCode.Error_Access_Token_Expired: /** 用户账号已过期，需要刷新一下 */
			loginBaidu(PrefUtil.getString(mContext,AlbumPage.PREF_KEY_IUNI_ACCOUNT_TOKEN, ""), true);
		break;
		}
	}
	
	private List<CommonFileInfo> cacheList = null;

	/**
	 * 
	 * @param path
	 * @param isDirPath
	 * @param info
	 */
	public void getFileListDiffFromBaidu(final String path,
			final boolean isDirPath, final CommonFileInfo info) {
		if (mAccount == null || mAlbumClient == null) {
			Log.e(TAG,
					"getFileListDiffFromBaidu mAccount or mAlbumClient is null");
			return;
		}

		if (info == null) {
			cacheList = httpCacheManager.getFromCache(path);
			if (cacheList != null) {
				if (baiduinterface != null) {
					baiduinterface.baiduPhotoList(cacheList, isDirPath, info);
				}
			}
		}
		String cursor = Utils.getListTag(mContext);
		mAlbumClient.diffWithCursor(mAccount, cursor,
				new AbstractAlbumListener() {
					@Override
					public void onDiffWithCursor(DiffResponse diffResponse) {

						String taskId = diffResponse.getTaskId();
						int statusType = diffResponse.getStatusType();
						int errorCode = diffResponse.getErrorCode();
						String errorMsg = diffResponse.getMessage();
						if (null != diffResponse) {
							if (ErrorCode.No_Error == errorCode) {
								if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
									if (!TextUtils.isEmpty(Utils
											.getListTag(mContext))) {
										// 增量列表内容(更新列表)
										List<DifferEntryInfo> entries = diffResponse.entries;
										Utils.setListTag(mContext,
												diffResponse.cursor);
										if ((entries == null || entries
												.isEmpty())
												&& cacheList != null) {
											return;
										}
										if (entries != null) {
											for (int i = 0; i < entries.size(); i++) {
												DifferEntryInfo info = entries
														.get(i);
												if (info.commonFileInfo.isDir
														&& info.isDeleted) {
													if (httpCacheManager != null) {
														httpCacheManager
																.removeCache(info.commonFileInfo.path);
													}
												} else {
													if (httpCacheManager != null) {
														httpCacheManager
																.removeCache(Utils
																		.getPathFromFilepath(info.commonFileInfo.path));
													}
													if (lruMemoryCache != null) {
														lruMemoryCache
																.removeBitmapFromMemCache(info.commonFileInfo.blockList);
													}
												}
											}
										}
									}

									getFileListFromBaidu(path, isDirPath, info);
									Utils.setListTag(mContext,
											diffResponse.cursor);
									return;
								}
							} else {
								Log.e(TAG, "Diff files failed. errorCode:"
										+ errorCode + " errorMsg:" + errorMsg);
								if (!Utils.isConnect(mContext)) {
									List<CommonFileInfo> list = httpCacheManager
											.getFromCache(path);
									// LogUtil.d(TAG, "list::" + list);
									if (list != null) {
										if (baiduinterface != null) {
											baiduinterface.baiduPhotoList(list,
													isDirPath, info);
											return;
										}
									}

								}
								//wenyongzhe 2015.10.19 login
								onErrorException(errorCode);
							}
						}
						getFileListFromBaidu(path, isDirPath, info);
					}
				});
	}

	public void getThumbnailsFromBaidu(final String url, final String md5,
			final ImageProcessingCallback callback) {
		if (mAccount == null || mAlbumClient == null || TextUtils.isEmpty(url)
				|| TextUtils.isEmpty(md5)) {
			Log.e(TAG,
					"getThumbnailsFromBaidu mAccount or mAlbumClient or url or md5 is null");
			LogUtil.d(TAG, "getThumbnailsFromBaidu::" + url);
			return;
		}
		mAlbumClient.thumbnail(mAccount, url, 100, 300, 300,
				new AbstractAlbumListener() {
					@Override
					public void onThumbnail(ThumbnailResponse thumbnailResponse) {
						String taskId = thumbnailResponse.getTaskId();
						int statusType = thumbnailResponse.getStatusType();
						int errorCode = thumbnailResponse.getErrorCode();
						String errorMsg = thumbnailResponse.getMessage();
						if (null != thumbnailResponse) {
							if (ErrorCode.No_Error == errorCode) {
								if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
									if (null != thumbnailResponse.bitmap) {
										callback.onImageProcessing(
												new WeakReference<Bitmap>(
														thumbnailResponse.bitmap),
												md5);
										if (lruMemoryCache == null
												|| fileCache == null) {
											Log.e(TAG,
													"lruMemoryCache  or fileCache is null");
										}
										if (thumbnailResponse.bitmap != null) {
											lruMemoryCache
													.addBitmapToMemoryCache(
															md5,
															thumbnailResponse.bitmap);
											fileCache.saveBitmapByLru(md5,
													thumbnailResponse.bitmap);
										}
										thumbnailResponse.bitmap = null;

									}

								}
							} else {
								Log.e(TAG, "Thumbnail failed. errorCode:"
										+ errorCode + " errorMsg:" + errorMsg
										+ " url:" + url);
								
								//wenyongzhe 2015.10.19 login
								onErrorException(errorCode);
							}
						}
					}

				});
	}

	public void renameFromBaidu(final CommonFileInfo folde, String newName) {
		if (mAccount == null || folde == null || TextUtils.isEmpty(newName)) {
			Log.e(TAG, "renameFromBaidu folde or newName or mAccount is null");
			return;
		}

		List<FileFromToInfo> list = new ArrayList<FileFromToInfo>();
		FileFromToInfo info = new FileFromToInfo();
		info.setSource(folde.path);
		info.setTarget(newName);
		list.add(info);
		// LogUtil.d(TAG, "folde.path::" + folde.path + " name::" + newName);
		mAlbumClient.renamePhotos(mAccount, list, new AbstractAlbumListener() {
			@Override
			public void onRenameFiles(FileFromToResponse fileFromToResponse) {
				super.onRenameFiles(fileFromToResponse);
				String taskId = fileFromToResponse.getTaskId();
				int statusType = fileFromToResponse.getStatusType();
				int errorCode = fileFromToResponse.getErrorCode();
				String errorMsg = fileFromToResponse.getMessage();

				if (null != fileFromToResponse) {
					if (ErrorCode.No_Error == errorCode) {
						if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
							getFileListFromBaidu(AlbumConfig.REMOTEPATH, true,
									null);
							if (operationComplete != null) {
								operationComplete.renameComplete(true);
							}
							return;
						}
					} else {
						Log.e(TAG, "Rename files failed. errorCode:"
								+ errorCode + " errorMsg:" + errorMsg);
						//wenyongzhe 2015.10.19 login
						onErrorException(errorCode);
					}
				}
				if (operationComplete != null) {
					operationComplete.renameComplete(false);
				}
			}
		});
	}

	public void createAlbumFromBaidu(String name) {
		if (mAccount == null || mAlbumClient == null || TextUtils.isEmpty(name)) {
			Log.e(TAG,
					"createAlbumFromBaidu mAccount or mAlbumClient or name is null");
			return;
		}
		String path = AlbumConfig.REMOTEPATH + File.separator + name;
		mAlbumClient.makePhotoDir(mAccount, path, new AbstractAlbumListener() {
			@Override
			public void onMakeDir(FileInfoResponse fileInfoResponse) {

				String taskId = fileInfoResponse.getTaskId();
				int statusType = fileInfoResponse.getStatusType();
				int errorCode = fileInfoResponse.getErrorCode();
				String errorMsg = fileInfoResponse.getMessage();
				// LogUtil.d(TAG, "onMakeDir:" + statusType + " taskID:" +
				// taskId);
				if (null != fileInfoResponse) {
					if (ErrorCode.No_Error == errorCode) {
						if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
							getFileListFromBaidu(AlbumConfig.REMOTEPATH, true,
									null);
							if (operationComplete != null) {
								operationComplete.createAlbumComplete(true);
								return;
							}
						}
					} else {
						Log.e(TAG, "MakeDir failed. errorCode:" + errorCode
								+ " errorMsg:" + errorMsg);
						//wenyongzhe 2015.10.19 login
						onErrorException(errorCode);
					}
				}
				if (operationComplete != null) {
					operationComplete.createAlbumComplete(false);
				}

			}
		});
	}

	public void deletePhotoOrAlbum(List<CommonFileInfo> fileInfos,
			final boolean isDir) {
		if (mAccount == null || mAlbumClient == null || fileInfos == null
				|| fileInfos.size() == 0) {
			Log.e(TAG,
					"deletePhotoOrAlbum mAccount or mAlbumClient or fileInfos is null");
			return;
		}
		List<String> strings = new ArrayList<String>();
		for (CommonFileInfo info : fileInfos) {
			strings.add(info.path);
			// LogUtil.d(TAG, "info.path::" + info.path);
		}
		mAlbumClient.deletePhotos(mAccount, strings,
				new AbstractAlbumListener() {
					@Override
					public void onDeleteFiles(SimpleResponse simplefiedResponse) {
						String taskId = simplefiedResponse.getTaskId();
						int statusType = simplefiedResponse.getStatusType();
						int errorCode = simplefiedResponse.getErrorCode();
						String errorMsg = simplefiedResponse.getMessage();
						if (null != simplefiedResponse) {
							if (ErrorCode.No_Error == errorCode) {
								if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
									if (isDir) {
										getFileListFromBaidu(
												AlbumConfig.REMOTEPATH, isDir,
												null);
									}
									if (operationComplete != null) {
										operationComplete.delComplete(true);
										return;
									}
								}
							} else {
								Log.e(TAG, "Delete files failed. errorCode:"
										+ errorCode + " errorMsg:" + errorMsg);
								//wenyongzhe 2015.10.19 login
								onErrorException(errorCode);
							}

						}
						if (operationComplete != null) {
							operationComplete.delComplete(false);
						}
					}
				});
	}

	public void moveOrCopyPhotoFromBaidu(List<CommonFileInfo> fileInfos,
			String path, boolean isMove) {
		if (mAccount == null || mAlbumClient == null || fileInfos == null
				|| fileInfos.size() == 0 || TextUtils.isEmpty(path)) {
			Log.e(TAG,
					"moveOrCopyPhotoFromBaidu mAccount or mAlbumClient or fileInfos or path is null");
			return;
		}
		List<FileFromToInfo> list = new ArrayList<FileFromToInfo>();
		for (CommonFileInfo info : fileInfos) {
			FileFromToInfo toInfo = new FileFromToInfo(info.path, path
					+ File.separator + Utils.getNameFromFilepath(info.path));
			list.add(toInfo);
		}
		// LogUtil.d(TAG, "move list::" + list.size());
		if (isMove) {
			mAlbumClient.movePhotos(mAccount, list, albumListener);
		} else {
			mAlbumClient.copyPhotos(mAccount, list, albumListener);
		}
	}

	public void moveOrCopyPhotoFromBaidu(List<CommonFileInfo> fileInfos,
			CommonFileInfo tInfo, boolean isMove) {
		if (tInfo == null) {
			Log.e(TAG, "moveOrCopyPhotoFromBaidu tInfo is null");
		}
		moveOrCopyPhotoFromBaidu(fileInfos, tInfo.path, isMove);

	}

	private AbstractAlbumListener albumListener = new AbstractAlbumListener() {
		public void onMoveFiles(FileFromToResponse fileFromToResponse) {
			String taskId = fileFromToResponse.getTaskId();
			int statusType = fileFromToResponse.getStatusType();
			int errorCode = fileFromToResponse.getErrorCode();
			String errorMsg = fileFromToResponse.getMessage();
			if (null != fileFromToResponse) {
				if (ErrorCode.No_Error == errorCode) {
					if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {

						List<FileFromToInfo> list = fileFromToResponse.list;
						int length = 0;
						if (list != null) {
							length = list.size();

						}
						if (operationComplete != null) {
							operationComplete.moveOrCopyComplete(true,
									list.size() > 0, errorCode);
							return;
						}

					}
				} else {
					Log.e(TAG, "Move files failed. errorCode:" + errorCode
							+ " errorMsg:" + errorMsg);
					//wenyongzhe 2015.10.19 login
					onErrorException(errorCode);
				}
				if (operationComplete != null) {
					operationComplete
							.moveOrCopyComplete(false, true, errorCode);
					return;
				}
			}
		}

		public void onCopyFiles(FileFromToResponse fileFromToResponse) {
			String taskId = fileFromToResponse.getTaskId();
			int statusType = fileFromToResponse.getStatusType();
			int errorCode = fileFromToResponse.getErrorCode();
			String errorMsg = fileFromToResponse.getMessage();
			if (null != fileFromToResponse) {
				if (ErrorCode.No_Error == errorCode) {
					if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
						List<FileFromToInfo> list = fileFromToResponse.list;
						int length = 0;
						if (list != null) {
							length = list.size();
						}
						if (operationComplete != null) {
							operationComplete.moveOrCopyComplete(true, false,
									errorCode);
							return;
						}
					}
				} else {
					Log.e(TAG, "Copy files failed. errorCode:" + errorCode
							+ " errorMsg:" + errorMsg);
					//wenyongzhe 2015.10.19 login
					onErrorException(errorCode);
					// errorCode:31061 errorMsg:file already exists
				}
				if (operationComplete != null) {
					operationComplete.moveOrCopyComplete(false, false,
							errorCode);
				}
			}
		};
	};

	/**
	 * {@link CloudItemFragment}
	 */
	private List<CommonFileInfo> albumInfos;

	public List<CommonFileInfo> getAlbumInfos() {
		return albumInfos;
	}

	public void setAlbumInfos(List<CommonFileInfo> albumInfos) {
		this.albumInfos = albumInfos;
		initHashMap();// paul add
	}

	/*
	 * public List<CommonFileInfo> getDownload(int position, int offset, boolean
	 * noPosition) { List<CommonFileInfo> fileInfos = new
	 * ArrayList<CommonFileInfo>( getAlbumInfos()); int start = position -
	 * offset; if (start < 0) { start = 0; } int end = position + offset; if
	 * (end - getAlbumInfos().size() > 0) { end = getAlbumInfos().size(); }
	 * 
	 * LogUtil.d(TAG, "start::" + start + " end::" + end + " offset::" +
	 * offset); List<CommonFileInfo> temps = fileInfos.subList(start, end); if
	 * (start == end) { temps.add(getAlbumInfos().get(position)); return temps;
	 * } if (noPosition) { temps.remove(fileInfos.get(position)); } return
	 * temps; }
	 */

	/*
	 * public void downloadFromBaidu(int position, int offset, boolean
	 * noPosition) { downloadFromBaidu(getDownload(position, offset,
	 * noPosition)); }
	 */

	// Aurora <paul> <2015-4-23> add start

	private class DlCheckInfo {
		public int index = -1;
		public boolean downloaded = false;
		public boolean started = false;
	}

	private HashMap<String, DlCheckInfo> mDownloadInfo = new HashMap<String, DlCheckInfo>();
	private ArrayList<ContentValues> mSavingList = new ArrayList<ContentValues>();
	private ArrayList<String> mName = new ArrayList<String>();
	private static final int DOWNLOAD_CACHE_NUM = 3;
	private String mViewingPath = null;
	private int mCurrentIndex = -1;

	private void initHashMap() {
		mDownloadInfo.clear();
		mName.clear();

		if (null == albumInfos) {
			return;
		}

		int len = albumInfos.size();
		for (int i = 0; i < len; ++i) {
			DlCheckInfo info = new DlCheckInfo();
			info.index = i;
			info.started = false;
			info.downloaded = false;
			String name = Utils.getNameFromFilepath(albumInfos.get(i).path);
			mDownloadInfo.put(name, info);
			mName.add(i, name);
		}

		checkNoMedia();
	}

	public String CACHEPATH = null;

	private String getCacheDir() {
		if (null == CACHEPATH) {
			File file = mContext.getExternalCacheDir();
			/*
			 * if(null == file){ file =
			 * Environment.getExternalStorageDirectory(); }
			 * 
			 * if(null == file){ file = mContext.getCacheDir(); }
			 */
			if (null != file) {
				CACHEPATH = file.getAbsolutePath() + "/CloudDownload/";
			}
		}
		return CACHEPATH;
	}

	private void resetHashMap() {

		if (null == albumInfos) {
			return;
		}

		int len = albumInfos.size();
		for (int i = 0; i < len; ++i) {
			String name = mName.get(i);
			if (null == name) {
				name = Utils.getNameFromFilepath(albumInfos.get(i).path);
				mName.add(i, name);
			}
			DlCheckInfo info = mDownloadInfo.get(name);
			if (null == info) {
				info = new DlCheckInfo();
				info.index = i;
				info.started = false;
				info.downloaded = false;
				mDownloadInfo.put(name, info);
			} else {
				info.index = i;
				info.started = false;
				info.downloaded = false;
			}

		}
	}

	public ArrayList<Integer> genIndexs(int curPos, int offset) {

		ArrayList<Integer> index = new ArrayList<Integer>();
		int size = albumInfos.size();
		index.add(curPos);
		for (int i = 1; i <= offset; ++i) {
			if (curPos + i < size) {
				index.add(curPos + i);
			}
			if (curPos - i >= 0) {
				index.add(curPos - i);
			}
		}
		return index;
	}

	private void invokeCallback(final String name) {
		// AsyncQueryHandler
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (baiduTaskListener != null) {
					FileTaskStatusBean bean = new FileTaskStatusBean();
					bean.setFileName(name);
					bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_DONE);
					// bean.setType(1);
					baiduTaskListener.baiduDownloadTaskStatus(bean);
				}
			}
		});
	}

	private boolean hasStarted(String name) {
		return ((DlCheckInfo) mDownloadInfo.get(name)).started;
	}

	private void setStarted(String name) {
		((DlCheckInfo) mDownloadInfo.get(name)).started = true;
	}

	public void setDownloaded(String name) {
		if (null == name)
			return;
		DlCheckInfo info = ((DlCheckInfo) mDownloadInfo.get(name));
		// add by JXH 2015-5-20 begin java.lang.NullPointerException
		if (info == null) {
			return;
		}
		// add by JXH 2015-5-20 end
		if (null != info)
			info.downloaded = true;

		if (mCurrentIndex == info.index) {
			Intent intent = new Intent(BaiduAlbumUtils.ACTION_IMAGE_DOWNLOADED);
			mContext.sendBroadcast(intent);
		}
	}

	public void onDownloadError(String name) {
		DlCheckInfo info = mDownloadInfo.get(name);
		if (null != info) {
			info.started = false;
			toDownload(info.index);
		}
	}

	public boolean isImgDownloaded(String name) {
		/*
		 * if (index < 0 || index >= albumInfos.size()) return false; String
		 * name = Utils.getNameFromFilepath(albumInfos.get(index).path);
		 */
		name = Utils.getNameFromFilepath(name);
		DlCheckInfo info = mDownloadInfo.get(name);
		boolean ret = ((null == info) ? false : info.downloaded);
		return ret;
	}

	private void toDownload(int curPos) {
		if (!NetworkUtil.checkNetwork(mContext))//wenyongzhe 2015.10.26
			return;

		List<FileUpDownloadInfo> downloadInfos = new ArrayList<FileUpDownloadInfo>();
		String path = albumInfos.get(curPos).path;
		if (!TextUtils.isEmpty(path)) {
			String subStr[] = path.split("/");
			if (subStr.length <= 0) {
				return;
			}
			String name = subStr[subStr.length - 1];
			String p = getCacheDir() + subStr[subStr.length - 2] + "/" + name;
			if ((new File(p)).exists()) {
				if (!hasStarted(name)) {
					setStarted(name);
					invokeCallback(name);
				}
			} else {
				try {
					setStarted(name);
					saveToDb(name, mContext,
							((DlCheckInfo) mDownloadInfo.get(name)).index, p);
					FileUpDownloadInfo downloadInfo = new FileUpDownloadInfo(
							path, p, FileUpDownloadInfo.TYPE_DOWNLOAD);
					downloadInfos.add(downloadInfo);
					mAlbumClient.downloadPhotos(mAccount, downloadInfos,
							downloadFileTaskListener);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void downloadSingleImg(int curPos, boolean increase) {
		if (mAccount == null || mAlbumClient == null) {
			Log.e(TAG,
					"downloadSingleImg mAccount or mAlbumClient or fileInfos is null");
			return;
		}
		mCurrentIndex = curPos;

		if (increase) {
			curPos += DOWNLOAD_CACHE_NUM;
		} else {
			curPos -= DOWNLOAD_CACHE_NUM;
		}

		if (curPos >= albumInfos.size() || curPos < 0) {
			Log.e(TAG, "downloadSingleImg err! albumInfos.size() : "
					+ albumInfos.size() + " curPos:" + curPos);
			return;
		}
		toDownload(curPos);
	}

	public String getViewingPath() {
		return mViewingPath;
	}

	private void checkNoMedia() {
		String cache = getCacheDir();
		if (null == cache)
			return;

		File noMedia = new File(cache, ".nomedia");
		if (!noMedia.exists()) {
			try {
				noMedia.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "Can't create the nomedia");
			}
		}
	}

	public void downloadFromBaidu(int curPos) {
		if (mAccount == null || mAlbumClient == null) {
			Log.e(TAG,
					"downloadFromBaidu mAccount or mAlbumClient or fileInfos is null");
			return;
		}
		resetHashMap();
		ArrayList<Integer> index = genIndexs(curPos, DOWNLOAD_CACHE_NUM);
		List<FileUpDownloadInfo> downloadInfos = new ArrayList<FileUpDownloadInfo>();
		List<FileUpDownloadInfo> firstTask = new ArrayList<FileUpDownloadInfo>();
		boolean first = true;
		for (Integer i : index) {
			String path = albumInfos.get(i).path;
			if (!TextUtils.isEmpty(path)) {
				String subStr[] = path.split("/");
				if (subStr.length <= 0) {
					return;
				}
				String name = subStr[subStr.length - 1];
				String p = getCacheDir() + subStr[subStr.length - 2] + "/"
						+ name;
				if (first) {
					mViewingPath = p;
				}
				if ((new File(p)).exists()) {
					boolean started = hasStarted(name);
					if (first) {
						if (!started) {
							setStarted(name);
							intentToSave(
									name,
									((DlCheckInfo) mDownloadInfo.get(name)).index,
									p);
						}
						invokeCallback(name);
					} else if (!started) {
						setStarted(name);
						intentToSave(name,
								((DlCheckInfo) mDownloadInfo.get(name)).index,
								p);
						invokeCallback(name);
					}
				} else {
					try {
						setStarted(name);
						intentToSave(name,
								((DlCheckInfo) mDownloadInfo.get(name)).index,
								p);

						FileUpDownloadInfo downloadInfo = new FileUpDownloadInfo(
								path, p, FileUpDownloadInfo.TYPE_DOWNLOAD);
						if (!first) {
							downloadInfos.add(downloadInfo);
						} else {
							firstTask.add(downloadInfo);
							mAlbumClient.downloadPhotos(mAccount, firstTask,
									downloadFileTaskListener);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				first = false;
			}
		}
		if (downloadInfos.size() > 0) {
			mAlbumClient.downloadPhotos(mAccount, downloadInfos,
					downloadFileTaskListener);
		}
		flush();
	}

	public void flush() {
		if (!mSavingList.isEmpty()) {
			ContentValues[] valuesArray = new ContentValues[mSavingList.size()];
			valuesArray = mSavingList.toArray(valuesArray);
			int num = 0;
			try {
				num = mContext.getContentResolver().bulkInsert(
						Images.Media.EXTERNAL_CONTENT_URI, valuesArray);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mSavingList.clear();
		}
	}

	private boolean checkDbItem(String path) {
		Cursor cursor = null;
		try {
			cursor = Media.query(mContext.getContentResolver(),
					Media.getContentUri("external"),
					new String[] { Media._ID }, "_data=(?)",
					new String[] { path }, null);
			if (null != cursor && cursor.moveToNext()) {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "query exception!");
		} finally {
			if (null != cursor) {
				cursor.close();
				cursor = null;
			}
		}
		return false;

	}

	public boolean intentToSave(String name, long index, String path) {
		if (TextUtils.isEmpty(name)) {
			return false;
		}

		if (checkDbItem(path)) {
			return false;
		}

		long time = System.currentTimeMillis();
		final ContentValues values = new ContentValues();

		values.put(Images.Media.TITLE, name);
		values.put(Images.Media.DISPLAY_NAME, name);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.DATE_TAKEN, Long.MAX_VALUE - index);
		values.put(Images.Media.DATE_MODIFIED, time);
		values.put(Images.Media.DATE_ADDED, time);
		values.put(Images.Media.ORIENTATION, 0);
		values.put(Images.Media.DATA, path);
		// values.put(Images.Media.SIZE, file.length());
		// if(width != 0) values.put(Images.Media.WIDTH, width);
		// if(height != 0) values.put(Images.Media.HEIGHT, height);
		values.put(Images.Media.MINI_THUMB_MAGIC, 0);

		mSavingList.add(values);
		return true;
	}

	public void clearAfterDelete(Context context) {
		String where = "_data like'" + getCacheDir() + "%'";
		context.getContentResolver().delete(Images.Media.EXTERNAL_CONTENT_URI,
				where, null);
	}

	public boolean saveToDb(String name, Context context, long index,
			String path) {

		if (TextUtils.isEmpty(name)) {
			return false;
		}
		long time = System.currentTimeMillis();
		final ContentValues values = new ContentValues();

		values.put(Images.Media.TITLE, name);
		values.put(Images.Media.DISPLAY_NAME, name);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.DATE_TAKEN, Long.MAX_VALUE - index);
		values.put(Images.Media.DATE_MODIFIED, time);
		values.put(Images.Media.DATE_ADDED, time);
		values.put(Images.Media.ORIENTATION, 0);
		values.put(Images.Media.DATA, path);
		// values.put(Images.Media.SIZE, file.length());

		// if(width != 0) values.put(Images.Media.WIDTH, width);
		// if(height != 0) values.put(Images.Media.HEIGHT, height);

		values.put(Images.Media.MINI_THUMB_MAGIC, 0);
		try {
			context.getContentResolver().insert(
					Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	// Aurora <paul> <2015-4-23> add end
	public void downloadFromBaidu(List<CommonFileInfo> fileInfos) {
		if (mAccount == null || mAlbumClient == null || fileInfos == null
				|| fileInfos.size() == 0) {
			Log.e(TAG,
					"downloadFromBaidu mAccount or mAlbumClient or fileInfos is null");
			return;
		}
		List<FileUpDownloadInfo> downloadInfos = new ArrayList<FileUpDownloadInfo>();
		for (CommonFileInfo info : fileInfos) {
			String path = info.path;
			if (!TextUtils.isEmpty(path)) {
				try {
					FileUpDownloadInfo downloadInfo = new FileUpDownloadInfo(
							path, AlbumConfig.DOWNLOADPATH
									+ Utils.getNameFromFilepath(path),
							FileUpDownloadInfo.TYPE_DOWNLOAD,
							FileUpDownloadInfo.OVER_WRITE);
					downloadInfos.add(downloadInfo);

					/*
					 * onGetTaskStatus bean :Bundle[EMPTY_PARCEL]message:
					 * source:
					 * /storage/emulated/0/DCIM/CloudIMG_20150508_170815_1.jpg
					 * target:/apps/iuni云/hh/IMG_20150508_170815_1.jpg
					 * fileName:CloudIMG_20150508_170815_1.jpg fileTaskId:78
					 * totalSize:2428271 currentSize:704512 type:2 errorCode:-1
					 * statusType:3 statusTaskCode:101
					 */
					FileTaskStatusBean bean = new FileTaskStatusBean();
					String fileName = Utils.getNameFromFilepath(path);
					bean.setSource(AlbumConfig.DOWNLOADPATH + fileName);
					bean.setTarget(info.path);
					bean.setType(FileTaskStatusBean.TYPE_TASK_DOWNLOAD);
					bean.setFileName(fileName);
					bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_CREATE);
					FakeTaskManager.getInstance().addDownloadFakeBeans(bean);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		XCloudTaskListenerManager manager = XCloudTaskListenerManager
				.getInstance(mContext);
		manager.registerListener(fileTaskListener);
		DownloadTaskListManager downloadTaskListManager = ((GalleryAppImpl) mContext)
				.getDownloadTaskListManager();
		downloadTaskListManager.addDownloadTask(downloadInfos);
		if (downloadTaskListManager.getDownloadTaskSize() == 1) {
			mAlbumClient.downloadPhotos(mAccount, downloadInfos,
					manager.getAlbumTaskListener());
		} else {
			downloadFromBaidu();
		}
		// mAlbumClient.downloadPhotos(mAccount, downloadInfos,
		// fileTaskListener);
	}

	public void downloadFromBaidu() {
		DownloadTaskListManager downloadTaskListManager = ((GalleryAppImpl) mContext)
				.getDownloadTaskListManager();
		downloadTaskListManager.setBaiduAlbumUtils(this);
		XCloudTaskListenerManager manager = XCloudTaskListenerManager
				.getInstance(mContext);
		if (downloadTaskListManager.getDownloadTaskSize() > 0) {
			mAlbumClient.downloadPhotos(mAccount,
					downloadTaskListManager.getFileDownlaodIndexOne(),
					manager.getAlbumTaskListener());
		}
	}

	/**
	 * 任务相关监听器,下载查看
	 */
	final IAlbumTaskListener downloadFileTaskListener = new IAlbumTaskListener() {

		@Override
		public void onGetTaskStatus(FileTaskStatusBean bean) {

			if (baiduTaskListener != null) {
				baiduTaskListener.baiduDownloadTaskStatus(bean);
			}

		}

		@Override
		public long progressInterval() {
			return 2000;
		}

		@Override
		public void onGetTaskListFinished(
				List<FileTaskStatusBean> fileTaskStatusBeanList) {

		}
	};
	/**
	 * 任务相关监听器(上传,下载统称为任务.)
	 */
	final IAlbumTaskListener fileTaskListener = new IAlbumTaskListener() {

		@Override
		public void onGetTaskStatus(FileTaskStatusBean bean) {

			if (baiduTaskListener != null && bean != null) {
				if (bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
					baiduTaskListener.baiduUploadTaskStatus(bean);
				} else {
					baiduTaskListener.baiduTaskStatus(bean);
				}
			}

		}

		@Override
		public long progressInterval() {
			return 200;
		}

		@Override
		public void onGetTaskListFinished(
				List<FileTaskStatusBean> fileTaskStatusBeanList) {

		}

	};

	final AccountProxy.IAccountInfoListener loginListener = new AccountProxy.IAccountInfoListener() {
		public void onException(final String errorMsg) {
			Log.e(TAG, "onException:::" + errorMsg);
			if (baiduinterface != null
					&& !errorMsg.equals("Fail to do task expantion")) {
				baiduinterface.loginComplete(false);
			}

		}

		public void onComplete(AccountInfo account) {
			mAccount = account;
			//wenyongzhe 2015.12.2 auto_update_bug start
			if(mAccount!=null){
				PrefUtil.setBoolean(mContext, "ACCOUNT_BIND", true);
			}
			//wenyongzhe 2015.12.2 auto_update_bug end
			setup();
			if (baiduinterface != null) {
				baiduinterface.loginComplete(true);
				String cursor = Utils.getListTag(mContext);
				// LogUtil.d(TAG, "cursor::" + cursor);
				if (TextUtils.isEmpty(cursor)) {
					createAlbumFromBaidu(mContext
							.getString(R.string.aurora_album));
					//wenyongzhe 2015.11.18 disable screenshot start
					//createAlbumFromBaidu(mContext
					//		.getString(R.string.aurora_system_screenshot));
					//wenyongzhe 2015.11.18 disable screenshot end
				}
			}
			return;
		}

		public void onCancel() {
			LogUtil.d(TAG, "onCancel:::");
			if (baiduinterface != null) {
				baiduinterface.loginComplete(false);
			}
		}
	};

	/**
	 * 设置(设置服务端打开日志,设置下载/上传线程数等)
	 */
	private void setup() {
		if (mAccount == null) {
			Log.e(TAG, " no login");
			return;
		}
		if (TextUtils.isEmpty(mAccount.getAccessToken())) {
			Log.e(TAG, "no login ");
			return;
		}

		if (TextUtils.isEmpty(mAccount.getUid())) {
			Log.e(TAG, "baidu uid is null");
			return;
		}

		mAlbumClient = AlbumClientProxy.getInstance(mHandler);

		Bundle config = new Bundle();
		config.putInt(AlbumClientProxy.UPLOAD_THREAD_COUNT,
				AlbumClientProxy.DEFAULT_THREAD_COUNT);
		config.putInt(AlbumClientProxy.DOWNLOAD_THREAD_COUNT,
				AlbumClientProxy.DEFAULT_THREAD_COUNT);
		config.putBoolean(AlbumClientProxy.LOG_SWITCH, true);
		mAlbumClient.setup(mContext, mAccount, config);
	}

	/**
	 * 获取登录信息，已经登录过
	 * 
	 * @return
	 */
	public AccountInfo getAccountInfo() {
		return mAccount;
	}

	public Handler mHandler = new Handler() {

	};

	public void uploadToAlbum(ArrayList<FileUpDownloadInfo> infos) {
		if (mAccount == null) {
			Log.e("SQF_LOG", "mAccount == null: " + (mAccount == null));
			return;
		}
		XCloudTaskListenerManager.getInstance(mContext).registerListener(
				fileTaskListener);
		mAlbumClient.uploadPhotos(mAccount, infos, XCloudTaskListenerManager
				.getInstance(mContext).getAlbumTaskListener());
	}

	public void getPhotoTaskList() {
		// mAccountInfo = mXCloudManager.getAccountInfo();
		if (mAccount == null) {
			Log.e("SQF_LOG", "mAccount == null: " + (mAccount == null));
			return;
		}
		//Log.e("SQF_LOG", "BaiduAlbumUtils.getPhotoTaskList ---------");
		mAlbumClient.getPhotoTaskList(mAccount, XCloudTaskListenerManager
				.getInstance(mContext).getAlbumTaskListener());
	}

	public void processPhotoTaskList(String type, String fileType) {
		if (mAccount == null) {
			Log.e("SQF_LOG", "mAccount == null: " + (mAccount == null));
			return;
		}
		/*
		 * type : pause, resume, remove
		 * fileType : upload, download, all
		 */
		Log.e("SQF_LOG", "processPhotoTaskList type: " + type + " fileType:" + fileType);
		mAlbumClient.processPhotoTaskList(mAccount, type, fileType, XCloudTaskListenerManager.getInstance(mContext).getAlbumTaskListener());
	}
}
