/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
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
import static com.android.providers.downloads.Constants.XTAG;
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
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.LongSparseLongArray;
import android.provider.Downloads;//add by JXH

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mediatek.downloadmanager.ext.Extensions;
import com.mediatek.downloadmanager.ext.IDownloadProviderFeatureExt;
import com.mediatek.xlog.Xlog;

import java.lang.ref.SoftReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

/**
 * Update {@link NotificationManager} to reflect current {@link DownloadInfo}
 * states. Collapses similar downloads into a single notification, and builds
 * {@link PendingIntent} that launch towards {@link DownloadReceiver}.
 */
public class DownloadNotifier {

    private static final int TYPE_ACTIVE = 1;
    private static final int TYPE_WAITING = 2;
    private static final int TYPE_COMPLETE = 3;

	private final Context mContext;
	private final NotificationManager mNotifManager;
	public static boolean isCancleCompleteNofi = false;

    /// M: add to fix 452723. @{
    private static HashMap <Long, Intent> sPendingIntents;
    /// @}

    /// M: add to fix 1918727. @{
    private static HashSet <Long> sUpdateDoneItems;
    /// @}

    private static IDownloadProviderFeatureExt sDownloadProviderFeatureExt;

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

    public DownloadNotifier(Context context) {
        mContext = context;
        mNotifManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        /// M: add to fix 452723. @{
        sPendingIntents = new HashMap<Long, Intent>();
        /// @}
        /// M: add to fix 1918727. @{
        sUpdateDoneItems = new HashSet<Long>();
        /// @}
    }

    public void cancelAll() {
        mNotifManager.cancelAll();
    }
    
    private List<Long> notifId = new ArrayList<Long>();
	private SoftReference<List<Long>> softReferenceId;

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
    
    private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Collection<DownloadInfo> downloads = (Collection<DownloadInfo>) msg.obj;
			updateWithLocked(downloads);
		}
    	
    };

    /**
     * Update {@link NotificationManager} to reflect the given set of
     * {@link DownloadInfo}, adding, collapsing, and removing as needed.
     */
    public void updateWith(Collection<DownloadInfo> downloads) {
        synchronized (mActiveNotifs) {
        	Message msg = handler.obtainMessage(200, downloads);
        	handler.removeMessages(200);
        	handler.sendMessageDelayed(msg, 100);
           
        }
    }

    private void updateWithLocked(Collection<DownloadInfo> downloads) {
        final Resources res = mContext.getResources();
        
        if (softReferenceId != null) {
			notifId = softReferenceId.get();
		}
		if (notifId == null) {
			notifId = new ArrayList<Long>();
		}

		// Cluster downloads together
		final Multimap<String, DownloadInfo> clustered = ArrayListMultimap
				.create();
		for (DownloadInfo info : downloads) {
			// modify by JXH 2015-5-6 begin
			if (info.mIsVisibleInDownloadUi) {
				final String tag = buildNotificationTag(info);
				if (isCancleCompleteNofi && isCompleteAndVisible(info)) {
					hideNotification(mContext, info.mId);
					mNotifManager.cancel(tag, 0);
				} else {
					if (tag != null && !info.mDeleted&& !notifId.contains(info.mId)) {
						clustered.put(tag, info);
					}
				}

			}
			// modify by JXH 2015-5-6 end
		}

		// Build notification for each cluster
		Log.d(Constants.TAG, "------------clustered:"+clustered.size());
		for (String tag : clustered.keySet()) {
			final int type = getNotificationTagType(tag);
			final Collection<DownloadInfo> cluster = clustered.get(tag);
			// add by JXH begin
			final DownloadInfo infoRun = cluster.iterator().next();
			// add by JXH end
			final Notification.Builder builder = new Notification.Builder(
					mContext);
			builder.setColor(res
					.getColor(com.android.internal.R.color.system_notification_accent_color));

			// Use time when cluster was first shown to avoid shuffling
			final long firstShown;
			if (mActiveNotifs.containsKey(tag)) {
				firstShown = mActiveNotifs.get(tag);
			} else {
				firstShown = System.currentTimeMillis();
				mActiveNotifs.put(tag, firstShown);
			}
			builder.setWhen(firstShown);

			// Show relevant icon
			if (type == TYPE_ACTIVE) {
				// modify by JXH begin
				if (isRunningNotify(infoRun)) {
					builder.setSmallIcon(android.R.drawable.stat_sys_download);
				} else {
					builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
				}
				// modify by JXH end
			} else if (type == TYPE_WAITING) {
				builder.setSmallIcon(android.R.drawable.stat_sys_warning);
			} else if (type == TYPE_COMPLETE) {
				builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
			}

            // Build action intents
            if (type == TYPE_ACTIVE || type == TYPE_WAITING) {
                // build a synthetic uri for intent identification purposes
                final Uri uri = new Uri.Builder().scheme("active-dl").appendPath(tag).build();
                final Intent intent = new Intent(Constants.ACTION_LIST,
                        uri, mContext, DownloadReceiver.class);
                intent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS,
                        getDownloadIds(cluster));
                builder.setContentIntent(PendingIntent.getBroadcast(mContext,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
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

                final Intent intent = new Intent(action, uri, mContext, DownloadReceiver.class);
                intent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS,
                        getDownloadIds(cluster));
                builder.setContentIntent(PendingIntent.getBroadcast(mContext,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

                /// M: add to fix 452723. @{
                if (!sPendingIntents.containsKey(info.mId)) {
                     sPendingIntents.put(info.mId, intent);
                     Xlog.i(Constants.TAG, "sPendingIntents.put(), id: " + info.mId);
                }
                /// @}

					final Intent hideIntent = new Intent(Constants.ACTION_HIDE,
					        uri, mContext, DownloadReceiver.class);
					builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, hideIntent, 0));
				try {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
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
                    percentText =
                            NumberFormat.getPercentInstance().format((double) current / total);

                    if (speed > 0) {
                        final long remainingMillis = ((total - current) * 1000) / speed;
                        remainingText = res.getString(R.string.download_remaining,
                                DateUtils.formatDuration(remainingMillis));
                    }

                    final int percent = (int) ((current * 100) / total);
                    builder.setProgress(100, percent, false);
                } else {
                    builder.setProgress(100, 0, true);
                }
            }

            // Build titles and description
            final Notification notif;
            if (cluster.size() == 1) {
                final DownloadInfo info = cluster.iterator().next();

				builder.setContentTitle(getDownloadTitle(res, info));

				if (type == TYPE_ACTIVE) {
					if (!TextUtils.isEmpty(info.mDescription)) {
						builder.setContentText(info.mDescription);
					} else {
						builder.setContentText(remainingText);
					}
					// add by JXH begin
					if (!isRunningNotify(info)) {
						builder.setContentTitle(res.getQuantityString(
								R.plurals.notif_summary_waiting,
								cluster.size(), cluster.size()));
						builder.setContentText(res
								.getString(R.string.download_paused));

						builder.setAutoCancel(false);
						builder.setOngoing(true);
						
					}
					// add by JXH end
					builder.setContentInfo(percentText);

                } else if (type == TYPE_WAITING) {
                    builder.setContentText(
                            res.getString(R.string.notification_need_wifi_for_size));

                } else if (type == TYPE_COMPLETE) {
                    if (Downloads.Impl.isStatusError(info.mStatus)) {
                        builder.setContentText(res.getText(R.string.notification_download_failed));
                    } else if (Downloads.Impl.isStatusSuccess(info.mStatus)) {
                        /// M: Operator Feature get the notificationtext for completing download. @{
                        String caption = null;
                        if (info.mPackage != null && info.mMimeType != null) {
                            sDownloadProviderFeatureExt = Extensions.getDefault(mContext);
                            caption = sDownloadProviderFeatureExt.getNotificationText(info.mPackage, info.mMimeType, info.mFileName);
                        }

                        if (caption == null) {
                            builder.setContentText(
                                    res.getText(R.string.notification_download_complete));
                        } else {
                            builder.setContentText(caption);
                        }
                        /// @}
                    }
                }

                notif = builder.build();

			} else {
				final Notification.InboxStyle inboxStyle = new Notification.InboxStyle(
						builder);
				int active = 0, paused = 0;// add by JXH
				for (DownloadInfo info : cluster) {
					inboxStyle.addLine(getDownloadTitle(res, info));
					// add by JXH begin
					if (info.mStatus == STATUS_RUNNING && info.mControl != 1) {
						active++;
					} else if (info.mStatus == Downloads.Impl.STATUS_PAUSED_BY_APP
							|| info.mControl == 1) {
						paused++;
					}
					// add by JXH begin end
				}
				if (type == TYPE_ACTIVE) {
					// add by JXH begin
					if (Downloads.Impl.isStatusError(infoRun.mStatus)) {
						builder.setContentText(res
								.getText(R.string.notification_download_failed));
					}

					String contentTitle = null, contentText = null;
					if (active > 0 && paused > 0) {
						contentTitle = res.getString(R.string.pause_content, active,paused) ;
						builder.setAutoCancel(false);
						builder.setOngoing(true);
						builder.setContentInfo(percentText);
						inboxStyle.setSummaryText(remainingText);
					} else if (active > 0 && paused == 0) {
						contentTitle = res.getQuantityString(
								R.plurals.notif_summary_active, active, active);
						builder.setContentInfo(percentText);
						inboxStyle.setSummaryText(remainingText);
					} else if (active == 0 && paused > 0) {
						contentTitle = res
								.getQuantityString(
										R.plurals.notif_summary_waiting,
										paused, paused);
						builder.setAutoCancel(false);
						builder.setOngoing(true);
						contentText = res.getString(R.string.download_paused);
					}

					if (!TextUtils.isEmpty(contentTitle)) {
						builder.setContentTitle(contentTitle);
					}
					if (TextUtils.isEmpty(contentText)) {
						builder.setContentText(remainingText);
					} else {
						builder.setContentText(contentText);
					}
					
					if (Constants.LOGV) {
						Log.d(XTAG, "contentTitle::" + contentTitle
								+ " contentText:" + contentText
								+ " remainingText:" + remainingText
								+ " percentText:" + percentText);
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
			// / M: add to fix 1918727. @{
			if (type == TYPE_COMPLETE) {
				DownloadInfo info = cluster.iterator().next();
				if (sUpdateDoneItems.contains(info.mId)) {
					Xlog.i(Constants.TAG,
							"sUpdateDoneItems contains,will continue, id: "
									+ info.mId);
					continue;
				}
			}
			// / @}
			Log.d(Constants.TAG, "--mNotifManager.notify--tag:"+tag+" mActiveNotifs:"+mActiveNotifs.size());
			mNotifManager.notify(tag, 0, notif);
			// / M: add to fix 1918727. @{
			if (type == TYPE_COMPLETE) {
				DownloadInfo info = cluster.iterator().next();
				sUpdateDoneItems.add(info.mId);
			}
			// / @}
		}

        // Remove stale tags that weren't renewed
        final Iterator<String> it = mActiveNotifs.keySet().iterator();
        while (it.hasNext()) {
            final String tag = it.next();
            if (!clustered.containsKey(tag)) {
                mNotifManager.cancel(tag, 0);
                it.remove();
            }
        }
    }

    private static CharSequence getDownloadTitle(Resources res, DownloadInfo info) {
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
                final long delta = SystemClock.elapsedRealtime() - mDownloadTouch.get(id);
                Log.d(TAG, "Download " + id + " speed " + mDownloadSpeed.valueAt(i) + "bps, "
                        + delta + "ms ago");
            }
        }
    }

    /**
     * Build tag used for collapsing several {@link DownloadInfo} into a single
     * {@link Notification}.
     */
    private static String buildNotificationTag(DownloadInfo info) {
        /// M : add to fix 1437582. @{
        if (info.mDeleted) {
            return null;
        }
        /// @}
        if (info.mStatus == Downloads.Impl.STATUS_QUEUED_FOR_WIFI) {
            return TYPE_WAITING + ":" + info.mPackage;
        } else if (isActiveAndVisible(info)) {
            return TYPE_ACTIVE + ":" + info.mPackage;
        } else if (isCompleteAndVisible(info)) {
            // Complete downloads always have unique notifs
            return TYPE_COMPLETE + ":" + info.mId;
        } else {
            return null;
        }
    }

    /**
     * Return the cluster type of the given tag, as created by
     * {@link #buildNotificationTag(DownloadInfo)}.
     */
    private static int getNotificationTagType(String tag) {
        return Integer.parseInt(tag.substring(0, tag.indexOf(':')));
    }

	private static boolean isActiveAndVisible(DownloadInfo download) {
		return (download.mStatus == STATUS_RUNNING || download.mStatus == Downloads.Impl.STATUS_PAUSED_BY_APP)
				&& (download.mVisibility == VISIBILITY_VISIBLE || download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	}

	private static boolean isCompleteAndVisible(DownloadInfo download) {
		return Downloads.Impl.isStatusCompleted(download.mStatus)
				&& (download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED || download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
	}

	// / M: add to fix 452723. @{
	HashMap getPendingIntentsMap() {
		return sPendingIntents;
	}

	Context getNotificationContext() {
		return mContext;
	}

	// / @}
	// / M: add to fix 1918727. @{
	HashSet getUpdateDoneItems() {
		return sUpdateDoneItems;
	}

	// / @}

	// add by JXH 2015-5-6 begin
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

	private boolean isRunningNotify(DownloadInfo info) {
		return info.mStatus == Downloads.Impl.STATUS_RUNNING
				&& info.mControl == Downloads.Impl.CONTROL_RUN;
	}
	// add by JXH 2015-5-6 end
}
