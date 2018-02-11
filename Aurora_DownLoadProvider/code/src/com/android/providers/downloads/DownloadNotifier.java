/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.providers.downloads;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION;
import static android.provider.Downloads.Impl.STATUS_RUNNING;
import static com.android.providers.downloads.Constants.TAG;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Downloads;
//import android.util.LongSparseLongArray;
import android.text.TextUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import android.util.SparseLongArray;

import com.android.providers.downloads.util.DateUtils;
import com.android.providers.downloads.util.DownloadConstants;
import com.android.providers.downloads.util.Log;

import android.app.Notification.Builder;
import android.R.fraction;

/**
 * Update {@link NotificationManager} to reflect current {@link DownloadInfo}
 * states. Collapses similar downloads into a single notification, and builds
 * {@link PendingIntent} that launch towards {@link DownloadReceiver}.
 */
public class DownloadNotifier {
	private static final String LOGTAG = "DownloadNotifier";

	private static final int TYPE_ACTIVE = 1;
	private static final int TYPE_WAITING = 2;
	private static final int TYPE_COMPLETE = 3;


	private final Context mContext;
	private final NotificationManager mNotifManager;

	/**
	 * Currently active notifications, mapped from clustering tag to timestamp
	 * when first shown.
	 * 
	 * @see #buildNotificationTag(DownloadInfo)
	 */
	@GuardedBy("mActiveNotifs")
	private final HashMap<String, Long> mActiveNotifs = Maps.newHashMap();

	/**
	 * Current speed of active downloads, mapped from {@link DownloadInfo#mId}
	 * to speed in bytes per second.
	 */
	@GuardedBy("mDownloadSpeed")
	private final LongSparseLongArray mDownloadSpeed = new LongSparseLongArray();

	/**
	 * Last time speed was reproted, mapped from {@link DownloadInfo#mId} to
	 * {@link SystemClock#elapsedRealtime()}.
	 */
	@GuardedBy("mDownloadSpeed")
	private final LongSparseLongArray mDownloadTouch = new LongSparseLongArray();

	HashMap<String, NotificationItem> mNotifications;
	private SparseLongArray mFirstShown = new SparseLongArray();

	private List<Long> notifId = new ArrayList<Long>();
	private SoftReference<List<Long>> softReferenceId;

	private static final class NotificationItem {
		// TODO: refactor to mNotifId and avoid building Uris based on it, since
		// they can overflow
		int mId; // This first db _id for the download for the app
		long mTotalCurrent = 0;
		long mTotalTotal = 0;
		int mTitleCount = 0;
		int mStatus = 0;
		int mControl = -1;
		String mPackageName; // App package name
		String mDescription;
		String[] mTitles = new String[2]; // download titles.
		String mPausedText = null;
		String mTag;

		/*
		 * Add a second download to this notification item.
		 */
		void addItem(String title, long currentBytes, long totalBytes) {
			mTotalCurrent += currentBytes;
			if (totalBytes <= 0 || mTotalTotal == -1) {
				mTotalTotal = -1;
			} else {
				mTotalTotal += totalBytes;
			}
			if (mTitleCount < 2) {
				mTitles[mTitleCount] = title;
			}
			mTitleCount++;
		}
	}

	private static final class BuilderInfo {
		String mRemainingText = null;
		String mPercentText = null;
	}

	public DownloadNotifier(Context context) {
		mContext = context;
		mNotifManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifications = new HashMap<String, NotificationItem>();
	}

	public void cancelAll() {
		mNotifManager.cancelAll();
	}

	/**
	 * Notify the current speed of an active download, used for calculating
	 * estimated remaining time.
	 */
	public void notifyDownloadSpeed(long id, long bytesPerSecond) {
		synchronized (mDownloadSpeed) {
			if (bytesPerSecond != 0) {
				mDownloadSpeed.put(id, bytesPerSecond);
				mDownloadTouch.put(id, SystemClock.elapsedRealtime());
			} else {
				mDownloadSpeed.delete(id);
				mDownloadTouch.delete(id);
			}
		}
	}

	/**
	 * Update {@link NotificationManager} to reflect the given set of
	 * {@link DownloadInfo}, adding, collapsing, and removing as needed.
	 */
	public void updateWith(Collection<DownloadInfo> downloads) {
		synchronized (mActiveNotifs) {
			// modify by JXH 2014-7-9 begin
			// updateActiveNotification(downloads);
			// updateCompletedNotification(downloads);
			updateWithLocked(downloads);
			// modify by JXH 2014-7-9 end
		}
	}

	// add by JXH 2014-7-9 begin
	private void updateWithLocked(Collection<DownloadInfo> downloads) {
		final Resources res = mContext.getResources();

		// Cluster downloads together
		final Multimap<String, DownloadInfo> clustered = ArrayListMultimap
				.create();
		if (softReferenceId != null) {
			notifId = softReferenceId.get();
		}
		if (notifId == null) {
			notifId = new ArrayList<Long>();
		}
		for (DownloadInfo info : downloads) {
			// modify by JXH 2014-7-24 begin
			if (info.mIsVisibleInDownloadUi) {
				// modify by JXH 2014-7-24 end
				if (Constants.LOGV
						&& (isCancleCompleteNofi && isCompleteAndVisible(info))) {
					Log.d(TAG, " info==" + info.mFileName);
				}
				final String tag = buildNotificationTag(info);
				if (isCancleCompleteNofi && isCompleteAndVisible(info)) {
					hideNotification(mContext, info.mId);
					mNotifManager.cancel(tag, 0);
				} else {

					if (tag != null && !info.mDeleted
							&& !notifId.contains(info.mId)) {// &&
						clustered.put(tag, info);
					}
				}

			}
		}

		// Build notification for each cluster
		for (String tag : clustered.keySet()) {
			final int type = getNotificationTagType(tag);
			final Collection<DownloadInfo> cluster = clustered.get(tag);
			final DownloadInfo infoRun = cluster.iterator().next();
			final Notification.Builder builder = new Notification.Builder(
					mContext);

			// Use time when cluster was first shown to avoid shuffling
			final long firstShown;
			if (mActiveNotifs.containsKey(tag)) {
				firstShown = mActiveNotifs.get(tag);
			} else {
				firstShown = System.currentTimeMillis();
				mActiveNotifs.put(tag, firstShown);
			}
			builder.setWhen(firstShown);
			if (Constants.LOGVV) {
				Log.d(TAG, "tag==" + tag + "  type==" + type
						+ " 1==TYPE_ACTIVE 2==TYPE_WAITING 3==TYPE_COMPLETE");
			}
			// Show relevant icon
			if (type == TYPE_ACTIVE) {
				if (isRunningNotify(infoRun)) {
					builder.setSmallIcon(android.R.drawable.stat_sys_download);
				} else {
					builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
				}
			} else if (type == TYPE_WAITING) {
				builder.setSmallIcon(android.R.drawable.stat_sys_warning);
			} else if (type == TYPE_COMPLETE) {
				builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
			}

			// Build action intents
			if (type == TYPE_ACTIVE || type == TYPE_WAITING) {
				// build a synthetic uri for intent identification purposes
				final Uri uri = new Uri.Builder().scheme("active-dl")
						.appendPath(tag).build();
				final Intent intent = new Intent(Constants.ACTION_LIST, uri,
						mContext, DownloadReceiver.class);
				intent.putExtra(
						DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS,
						getDownloadIds(cluster));
				builder.setContentIntent(PendingIntent.getBroadcast(mContext,
						0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
				builder.setOngoing(true);

			} else if (type == TYPE_COMPLETE) {
				final DownloadInfo info = cluster.iterator().next();
//				final Uri urif = Uri.parse(info.mFileName);
				final Uri uri = ContentUris.withAppendedId(
						Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, info.mId);
				builder.setAutoCancel(true);

				final String action;
				if (Downloads.Impl.isStatusError(info.mStatus)) {
					action = Constants.ACTION_LIST;
				} else {
					if (info.mDestination != Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION) {
						action = Constants.ACTION_OPEN;
					} else {
						action = Constants.ACTION_LIST;
					}
				}

				final Intent intent = new Intent(action, uri, mContext,
						DownloadReceiver.class);
				intent.putExtra(
						DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS,
						getDownloadIds(cluster));
				builder.setContentIntent(PendingIntent.getBroadcast(mContext,
						0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

				final Intent hideIntent = new Intent(Constants.ACTION_HIDE,
						uri, mContext, DownloadReceiver.class);
				builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0,
						hideIntent, 0));
				if (Constants.LOGVV) {
					Log.d(TAG, " TYPE_COMPLETE" + info.mId + " uri=="
							+ uri.toString());
				}
				if (softReferenceId != null) {
					notifId = softReferenceId.get();
				}
				if (notifId == null) {
					notifId = new ArrayList<Long>();
				}
				if (!notifId.contains(info.mId)) {
					notifId.add(info.mId);
					softReferenceId = new SoftReference<List<Long>>(notifId);
					notifId = null;
				}
				mActiveNotifs.remove(tag);

			} else {
				Log.e(LOGTAG, "error type==" + type);
			}

			// Calculate and show progress
			String remainingText = null;
			String percentText = null;
			if (type == TYPE_ACTIVE) {
				long current = 0;
				long total = 0;
				long speed = 0;
				synchronized (mDownloadSpeed) {
					for (DownloadInfo info : cluster) {
						if (info.mTotalBytes != -1) {
							current += info.mCurrentBytes;
							total += info.mTotalBytes;
							speed += mDownloadSpeed.get(info.mId);
						}
					}
				}

				if (total > 0) {
					final int percent = (int) ((current * 100) / total);
					percentText = res.getString(R.string.download_percent,
							percent);

					if (speed > 0) {
						final long remainingMillis = ((total - current) * 1000)
								/ speed;
						remainingText = res.getString(
								R.string.download_remaining, DateUtils
										.formatDuration(mContext,
												remainingMillis));
					}

					builder.setProgress(100, percent, false);
				} else {
					builder.setProgress(100, 0, true);
				}
			}

			// Build titles and description
			final Notification notif;
			boolean hasErrorStatus = hasErrorStatus(cluster);
			if (cluster.size() == 1) {
				final DownloadInfo info = cluster.iterator().next();

				builder.setContentTitle(getDownloadTitle(res, info));

				if (type == TYPE_ACTIVE) {
					if (hasErrorStatus) {
						builder.setContentText(res
								.getText(R.string.notification_download_failed));
					} else if (!TextUtils.isEmpty(info.mDescription)) {
						builder.setContentText(info.mDescription);
					} else {
						builder.setContentText(remainingText);
					}
					if (!isRunningNotify(info)) {
						builder.setContentTitle(res.getQuantityString(
								R.plurals.notif_summary_waiting,
								cluster.size(), cluster.size()));
						builder.setContentText(res
								.getString(R.string.download_paused));

						builder.setAutoCancel(false);
						builder.setOngoing(true);
					}
					builder.setContentInfo(percentText);

				} else if (type == TYPE_WAITING) {
					builder.setContentText(res
							.getString(R.string.notification_need_wifi_for_size));

				} else if (type == TYPE_COMPLETE) {
					if (Downloads.Impl.isStatusError(info.mStatus)) {
						builder.setContentText(res
								.getText(R.string.notification_download_failed));
					} else if (Downloads.Impl.isStatusSuccess(info.mStatus)) {
						builder.setContentText(res
								.getText(R.string.notification_download_complete));

					}
					builder.setAutoCancel(true);
				}

				notif = builder.build();

			} else {
				final Notification.InboxStyle inboxStyle = new Notification.InboxStyle(
						builder);

				for (DownloadInfo info : cluster) {
					inboxStyle.addLine(getDownloadTitle(res, info));
				}

				if (type == TYPE_ACTIVE) {
					if (!isRunningNotify(infoRun)) {
						builder.setContentTitle(res.getQuantityString(
								R.plurals.notif_summary_waiting,
								cluster.size(), cluster.size()));
						builder.setContentText(res
								.getString(R.string.download_paused));
						builder.setAutoCancel(false);
						builder.setOngoing(true);
					} else {
						if (Downloads.Impl.isStatusError(infoRun.mStatus)) {
							builder.setContentText(res
									.getText(R.string.notification_download_failed));
						} else {
							builder.setContentTitle(res.getQuantityString(
									R.plurals.notif_summary_active,
									cluster.size(), cluster.size()));
							builder.setContentText(remainingText);
							builder.setContentInfo(percentText);
							inboxStyle.setSummaryText(remainingText);
						}
					}

				} else if (type == TYPE_WAITING) {
					builder.setContentTitle(res.getQuantityString(
							R.plurals.notif_summary_waiting, cluster.size(),
							cluster.size()));
					builder.setContentText(res
							.getString(R.string.notification_need_wifi_for_size));
					inboxStyle
							.setSummaryText(res
									.getString(R.string.notification_need_wifi_for_size));
				}

				notif = inboxStyle.build();
			}
			if (Constants.LOGV) {
				Log.e(TAG, "mNotifManager.notify TAG==" + tag);
			}
			//add by JXH 2015-2-2 only systemUi begin
//			if(type==TYPE_WAITING){
//				notif.icon=android.R.drawable.stat_sys_warning;
//			}else if (type==TYPE_ACTIVE) {
//				notif.icon=android.R.drawable.stat_sys_download;
//			}else if(type==TYPE_COMPLETE) {
//				notif.icon=android.R.drawable.stat_sys_download_done;
//			}
			//add by JXH 2015-2-2 only systemUi end
			mNotifManager.notify(tag, 0, notif);
		}

		// Remove stale tags that weren't renewed
		final Iterator<String> it = mActiveNotifs.keySet().iterator();
		while (it.hasNext()) {
			final String tag = it.next();
			if (!clustered.containsKey(tag)) {
				if (Constants.LOGV) {
					Log.e(TAG, "mNotifManager.cancel TAG==" + tag);
				}
				mNotifManager.cancel(tag, 0);
				it.remove();
			}
		}
	}

	// add by JXH 2014-7-9 end

	private static CharSequence getDownloadTitle(Resources res,
			DownloadInfo info) {
		if (!TextUtils.isEmpty(info.mTitle)) {
			return info.mTitle;
		} else {
			return res.getString(R.string.download_unknown_title);
		}
	}

	private long[] getDownloadIds(Collection<DownloadInfo> infos) {
		final long[] ids = new long[infos.size()];
		int i = 0;
		for (DownloadInfo info : infos) {
			ids[i++] = info.mId;
		}
		return ids;
	}

	public void dumpSpeeds() {
		synchronized (mDownloadSpeed) {
			for (int i = 0; i < mDownloadSpeed.size(); i++) {
				final long id = mDownloadSpeed.keyAt(i);
				final long delta = SystemClock.elapsedRealtime()
						- mDownloadTouch.get(id);
				Log.d(TAG,
						"Download " + id + " speed "
								+ mDownloadSpeed.valueAt(i) + "bps, " + delta
								+ "ms ago");
			}
		}
	}

	/**
	 * Build tag used for collapsing several {@link DownloadInfo} into a single
	 * {@link Notification}.
	 */
	private static String buildNotificationTag(DownloadInfo info) {
		/*
		 * if (info.mControl == Downloads.Impl.CONTROL_PAUSED) { return
		 * TYPE_PAUSE + ":" + info.mPackage; } else
		 */if (info.mStatus == Downloads.Impl.STATUS_QUEUED_FOR_WIFI) {
			return TYPE_WAITING + File.pathSeparator + info.mPackage;
		} else if (isActiveAndVisible(info)) {
			// if (!info.isActivityNetwork(info)) {
			// return TYPE_PAUSE + ":" + info.mPackage;
			// }
			return TYPE_ACTIVE + File.pathSeparator + info.mPackage;
		} else if (isCompleteAndVisible(info)) {
			// Complete downloads always have unique notifs
			return TYPE_COMPLETE + File.pathSeparator + info.mId;
		} else {
			return null;
		}
	}

	/**
	 * Return the cluster type of the given tag, as created by
	 * {@link #buildNotificationTag(DownloadInfo)}.
	 */
	private static int getNotificationTagType(String tag) {
		return Integer.parseInt(tag.substring(0,
				tag.indexOf(File.pathSeparator)));
	}

	private static boolean isActiveAndVisible(DownloadInfo download) {
		return Downloads.Impl.isStatusInformational(download.mStatus)
				&& (download.mVisibility == VISIBILITY_VISIBLE || download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	}

	private static boolean isCompleteAndVisible(DownloadInfo download) {
		return Downloads.Impl.isStatusCompleted(download.mStatus)
				&& (download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED || download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
	}

	private boolean isErrorStatus(int status) {
		boolean isErrorStatus = Downloads.Impl.isStatusError(status)
				|| Downloads.Impl.isStatusClientError(status)
				|| Downloads.Impl.isStatusServerError(status)
				|| status == Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR
				|| status == Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR
				|| status == Downloads.Impl.STATUS_WAITING_FOR_NETWORK;
		return isErrorStatus;
	}


//	private Multimap<String, DownloadInfo> createNotificationItems(
//			Collection<DownloadInfo> downloads) {
//		final Multimap<String, DownloadInfo> clustered = ArrayListMultimap
//				.create();
//		for (DownloadInfo download : downloads) {
//			if (!isActiveAndVisible(download) || download.mDeleted) {
//				continue;
//			}
//			final String tag = buildNotificationTag(download);
//			if (tag != null) {
//				clustered.put(tag, download);
//			}
//			String packageName = download.mPackage;
//			long max = download.mTotalBytes;
//			long progress = download.mCurrentBytes;
//			long id = download.mId;
//			int status = download.mStatus;
//			int control = download.mControl;
//			String title = download.mTitle;
//			if (title == null || title.length() == 0) {
//				title = mContext.getResources().getString(
//						R.string.download_unknown_title);
//			}
//
//			NotificationItem item;
//			if (mNotifications.containsKey(packageName)) {
//				item = mNotifications.get(packageName);
//				if (status == Downloads.Impl.STATUS_RUNNING) {
//					item.mStatus = status;
//				}
//				if (control == Downloads.Impl.CONTROL_RUN) {
//					item.mControl = control;
//				}
//				item.addItem(title, progress, max);
//			} else {
//				item = new NotificationItem();
//				item.mId = (int) id;
//				item.mPackageName = packageName;
//				item.mDescription = download.mDescription;
//				item.mStatus = status;
//				item.mControl = control;
//				item.addItem(title, progress, max);
//				mNotifications.put(packageName, item);
//			}
//			item.mTag = tag;
//			if (download.mStatus == Downloads.Impl.STATUS_QUEUED_FOR_WIFI
//					&& item.mPausedText == null) {
//				item.mPausedText = mContext.getResources().getString(
//						R.string.notification_need_wifi_for_size);
//			}
//			if (download.mStatus == Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR
//					&& item.mPausedText == null) {
//				item.mPausedText = mContext.getResources().getString(
//						R.string.notification_download_failed);
//			}
//		}
//
//		return clustered;
//	}

/*	private int getDownloadIcon(NotificationItem item) {
		int icon = -1;
		if (isRunningNotify(item)) {
			icon = android.R.drawable.stat_sys_download;
		} else {
			icon = android.R.drawable.stat_sys_download_done;
		}
		boolean hasPausedText = (item.mPausedText != null);
		if (hasPausedText) {
			icon = android.R.drawable.stat_sys_warning;
		}
		return icon;
	}*/

	private boolean isRunningNotify(NotificationItem item) {
		return item.mStatus == Downloads.Impl.STATUS_RUNNING
				&& item.mControl == Downloads.Impl.CONTROL_RUN;
	}

	private boolean isRunningNotify(DownloadInfo info) {
		return info.mStatus == Downloads.Impl.STATUS_RUNNING
				&& info.mControl == Downloads.Impl.CONTROL_RUN;
	}

	private long getFirstShown(NotificationItem item) {
		final long firstShown;
		if (mActiveNotifs.containsKey(item.mTag)) {
			firstShown = mActiveNotifs.get(item.mTag);
		} else {
			firstShown = System.currentTimeMillis();
			mActiveNotifs.put(item.mTag, firstShown);
		}
		return firstShown;
	}

	private boolean hasErrorStatus(Collection<DownloadInfo> cluster) {
		boolean hasErrorStatus = false;
		for (DownloadInfo info : cluster) {
			if (isErrorStatus(info.mStatus)) {
				hasErrorStatus = true;
				break;
			}
		}
		return hasErrorStatus;
	}

/*	private void buildActionIntents(Builder builder, int type,
			Collection<DownloadInfo> cluster, NotificationItem item) {
		if (type == TYPE_ACTIVE || type == TYPE_WAITING) {
			// build a synthetic uri for intent identification purposes
			final Uri uri = new Uri.Builder().scheme("active-dl")
					.appendPath(item.mTag).build();
			final Intent intent = new Intent(Constants.ACTION_LIST, uri,
					mContext, DownloadReceiver.class);
			intent.putExtra(
					DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS,
					getDownloadIds(cluster));
			builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT));
			builder.setOngoing(true);

		} else if (type == TYPE_COMPLETE) {
			final DownloadInfo info = cluster.iterator().next();
			final Uri uri = ContentUris.withAppendedId(
					Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, info.mId);
			builder.setAutoCancel(true);

			final String action;
			if (Downloads.Impl.isStatusError(info.mStatus)) {
				action = Constants.ACTION_LIST;
			} else {
				if (info.mDestination != Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION) {
					action = Constants.ACTION_OPEN;
				} else {
					action = Constants.ACTION_LIST;
				}
			}

			final Intent intent = new Intent(action, uri, mContext,
					DownloadReceiver.class);
			intent.putExtra(
					DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS,
					getDownloadIds(cluster));
			builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT));

			final Intent hideIntent = new Intent(Constants.ACTION_HIDE, uri,
					mContext, DownloadReceiver.class);
			builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0,
					hideIntent, 0));
		}
	}*/

/*	private BuilderInfo buildShowProgress(Builder builder, int type,
			Collection<DownloadInfo> cluster, NotificationItem item) {
		final Resources res = mContext.getResources();
		BuilderInfo builderInfo = new BuilderInfo();
		if (type == TYPE_ACTIVE) {
			long current = 0;
			long total = 0;
			long speed = 0;
			synchronized (mDownloadSpeed) {
				for (DownloadInfo info : cluster) {
					if (info.mTotalBytes != -1) {
						current += info.mCurrentBytes;
						total += info.mTotalBytes;
						speed += mDownloadSpeed.get(info.mId);
					}
				}
			}

			if (total > 0) {
				final int percent = (int) ((current * 100) / total);
				builderInfo.mPercentText = res.getString(
						R.string.download_percent, percent);

				if (speed > 0) {
					final long remainingMillis = ((total - current) * 1000)
							/ speed;
					builderInfo.mRemainingText = res
							.getString(R.string.download_remaining, DateUtils
									.formatDuration(mContext, remainingMillis));
				}

				if (isRunningNotify(item)) {
					builder.setProgress(100, percent, false);
				} else {
					builderInfo.mRemainingText = res
							.getString(R.string.notification_download_pausetext);
				}
			} else {
				builder.setProgress(100, 0, true);
			}
		}
		return builderInfo;
	}*/

/*	private Notification getActiveNotification(Builder builder, int type,
			Collection<DownloadInfo> cluster, BuilderInfo builderInfo) {
		final Resources res = mContext.getResources();
		boolean hasErrorStatus = hasErrorStatus(cluster);
		if (cluster.size() == 1) {
			final DownloadInfo info = cluster.iterator().next();

			builder.setContentTitle(getDownloadTitle(res, info));

			if (type == TYPE_ACTIVE) {
				if (hasErrorStatus) {
					builder.setContentText(res
							.getText(R.string.notification_download_failed));
				} else if (!TextUtils.isEmpty(info.mDescription)) {
					builder.setContentText(info.mDescription);
				} else {
					builder.setContentText(builderInfo.mRemainingText);
				}
				builder.setContentInfo(builderInfo.mPercentText);

			} else if (type == TYPE_WAITING) {
				builder.setContentText(res
						.getString(R.string.notification_need_wifi_for_size));

			} else if (type == TYPE_COMPLETE) {
				if (Downloads.Impl.isStatusError(info.mStatus)) {
					builder.setContentText(res
							.getText(R.string.notification_download_failed));
				} else if (Downloads.Impl.isStatusSuccess(info.mStatus)) {
					builder.setContentText(res
							.getText(R.string.notification_download_complete));
				}
			}
			return builder.build();
		} else {
			final Notification.InboxStyle inboxStyle = new Notification.InboxStyle(
					builder);

			for (DownloadInfo info : cluster) {
				inboxStyle.addLine(getDownloadTitle(res, info));
			}

			if (type == TYPE_ACTIVE) {
				if (hasErrorStatus) {
					builder.setContentTitle(res
							.getString(R.string.notification_download_failed));
				} else {
					builder.setContentTitle(res.getQuantityString(
							R.plurals.notif_summary_active, cluster.size(),
							cluster.size()));
				}
				builder.setContentText(builderInfo.mRemainingText);
				builder.setContentInfo(builderInfo.mPercentText);
				inboxStyle.setSummaryText(builderInfo.mRemainingText);

			} else if (type == TYPE_WAITING) {
				builder.setContentTitle(res.getQuantityString(
						R.plurals.notif_summary_waiting, cluster.size(),
						cluster.size()));
				builder.setContentText(res
						.getString(R.string.notification_need_wifi_for_size));
				inboxStyle.setSummaryText(res
						.getString(R.string.notification_need_wifi_for_size));
			}

			return inboxStyle.build();
		}
	}*/


/*	void notificationForCompletedDownload(long id, String title, int status,
			int destination, long lastMod, String packageName, String mimeType,
			String fullFileName) {
		// Add the notifications
		Notification.Builder builder = new Notification.Builder(mContext);
		builder.setSmallIcon(android.R.drawable.stat_sys_download_done,
				(int) id);
		if (title == null || title.length() == 0) {
			title = mContext.getResources().getString(
					R.string.download_unknown_title);
		}
		Uri contentUri = ContentUris.withAppendedId(
				Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
		String caption = null;
		Intent intent;
		if (Downloads.Impl.isStatusError(status)) {
			caption = mContext.getResources().getString(
					R.string.notification_download_failed);
			intent = new Intent(Constants.ACTION_LIST);
		} else {
			if (caption == null) {
				caption = mContext.getResources().getString(
						R.string.notification_download_complete);
			}

			if (destination != Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION) {
				intent = new Intent(Constants.ACTION_OPEN);
			} else {
				intent = new Intent(Constants.ACTION_LIST);
			}
		}
		intent.setClassName("com.android.providers.downloads",
				DownloadReceiver.class.getName());
		intent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS,
				new long[] { id });
		intent.setData(contentUri);

		builder.setWhen(lastMod);
		builder.setContentTitle(title);
		builder.setContentText(caption);
		builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0,
				intent, 0));

		intent = new Intent(Constants.ACTION_HIDE);
		intent.setClassName("com.android.providers.downloads",
				DownloadReceiver.class.getName());
		intent.setData(contentUri);
		builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, intent,
				0));

		mNotifManager.notify((int) id, builder.build());
	}
*/
/*	private static String buildPercentageLabel(Context context,
			long totalBytes, long currentBytes) {
		if (totalBytes <= 0) {
			return null;
		} else {
			final int percent = (int) (100 * currentBytes / totalBytes);
			return context.getString(R.string.download_percent, percent);
		}
	}
*/
	// add by Jxh 2014-8-13 begin

	public static boolean isCancleCompleteNofi = false;

	/**
	 * Mark the given {@link DownloadManager#COLUMN_ID} as being acknowledged by
	 * user so it's not renewed later.
	 */
	private void hideNotification(Context context, long id) {
		final int status;
		final int visibility;

		final Uri uri = ContentUris.withAppendedId(
				Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
		final Cursor cursor = context.getContentResolver().query(uri, null,
				null, null, null);
		try {
			if (cursor.moveToFirst()) {
				status = getInt(cursor, Downloads.Impl.COLUMN_STATUS);
				visibility = getInt(cursor, Downloads.Impl.COLUMN_VISIBILITY);
			} else {
				Log.w(TAG, "Missing details for download " + id);
				return;
			}
		} finally {
			cursor.close();
		}

		if (Downloads.Impl.isStatusCompleted(status)
				&& (visibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED || visibility == VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)) {
			final ContentValues values = new ContentValues();
			values.put(Downloads.Impl.COLUMN_VISIBILITY,
					Downloads.Impl.VISIBILITY_VISIBLE);
			context.getContentResolver().update(uri, values, null, null);
		}
	}

	private static int getInt(Cursor cursor, String col) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(col));
	}
	// add by Jxh 2014-8-13 end
}
