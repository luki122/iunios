//Gionee <jianghuan> <2013-12-28> add for CR00975553 begin
package com.aurora.puremanager.traffic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.aurora.puremanager.R;

public class NotificationController {

    private static final int NOTIFICATION_REMOVEVIEWS_ID = R.string.app_name + 2;
    private static final int NOTIFICATION_WARNING_ID = R.string.app_name + 1;
    private static final int NOTIFICATION_BOOTED_ID = R.string.app_name;
    private static final int WARNING_ID = 1;
    private static final int BOOTED_ID = 2;
    private static final int REMOVEVIEWS_ID = 3;
    private Notification mNotification;
    private NotificationManager sNotificationManager;
    private static NotificationController sNotificationController;
    private Context mContext;
    private String mTitle;
    private String mContent;
    private int mTickerText;
    private int mImageId;
    private boolean mSoundId = true;
    private Class<?> mClass;
    private int mActivatedSimIndex;
    private String mCycle;

    public static NotificationController getDefault(Context context) {
        if (sNotificationController == null) {
            sNotificationController = new NotificationController(context);
        }
        return sNotificationController;
    }

    private NotificationController(Context context) {
        mContext = context;
        sNotificationManager = (NotificationManager) mContext
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
    }

    // Gionee <jianghuan> <2014-03-08> add for CR01111459 begin
    public void setCycleInfo(String cycle) {
        mCycle = cycle;
    }

    // Gionee <jianghuan> <2014-03-08> add for CR01111459 end
    public void setTitle(int title) {
        mTitle = mContext.getString(title);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setContent(int content) {
        mContent = mContext.getString(content);
    }

    public void setContent(String content) {
        mContent = content;
    }

    public void setTickerText(int tickerText) {
        mTickerText = tickerText;
    }

    public void setSmallIcon(int imageId) {
        mImageId = imageId;
    }

    public void setClass(Class<?> cls) {
        mClass = cls;
    }

    public void setSimIndex(int simindex) {
        mActivatedSimIndex = simindex;
    }

    public void setSoundId(boolean soundId) {
        mSoundId = soundId;
    }

    public int getBootedId() {
        return BOOTED_ID;
    }

    public int getWarningId() {
        return WARNING_ID;
    }

    int mViewId;

    public int getRemoveViewsId() {
        return REMOVEVIEWS_ID;
    }

    public void setViewId(int id) {
        mViewId = id;
    }

    public void definedRemoteViews(int layoutId, int flag) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), layoutId);
        PendingIntent pendingIntent = getIntent();
        remoteViews.setOnClickPendingIntent(mViewId, pendingIntent);

        mNotification = new Notification();// new
                                           // Notification(0,"",System.currentTimeMillis());
        mNotification.icon = mImageId;
        mNotification.contentView = remoteViews;

        mNotification.flags |= Notification.FLAG_NO_CLEAR;
        mNotification.contentIntent = pendingIntent;

        showNotification(flag, mNotification);
    }

    public void setViewText(int viewId, String str) {
        mNotification.contentView.setTextViewText(viewId, str);
    }

    public void setTextColor(int viewId, int color) {
        mNotification.contentView.setTextColor(viewId, color);
    }

    public void setViewVisiblity(int viewId, boolean visible) {
        if (visible) {
            mNotification.contentView.setViewVisibility(viewId, View.VISIBLE);
        } else {
            mNotification.contentView.setViewVisibility(viewId, View.GONE);
        }
    }

    public void setViewsVibilities(int[] viewIds, boolean visible) {
        for (int id : viewIds) {
            setViewVisiblity(id, visible);
        }
    }

    public void show(int flag) {
        PendingIntent contentIntent = getIntent();

        Notification.Builder builder = new Notification.Builder(mContext).setContentTitle(mTitle)
                .setContentText(mContent).setContentIntent(contentIntent).setSmallIcon(mImageId)
                .setWhen(System.currentTimeMillis()).setTicker(mContext.getString(mTickerText))
                .setColor(0x4fc6bb)
                .setAutoCancel(true);

        if (mSoundId) {
            builder.setDefaults(Notification.DEFAULT_SOUND);
        }

        Notification notification = builder.getNotification();

        showNotification(flag, notification);
    }

    private PendingIntent getIntent() {
        Intent mIntent = new Intent(mContext, mClass);
        mIntent.putExtra(TrafficPreference.getSimValue(), mActivatedSimIndex);
        // Gionee <jianghuan> <2014-03-08> add for CR01111459 begin
        mIntent.putExtra(TrafficPreference.getTrafficCycle(), mCycle);
        // Gionee <jianghuan> <2014-03-08> add for CR01111459 end
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mContentIntent = PendingIntent.getActivity(mContext, 0, mIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mContentIntent;
    }

    private void showNotification(int flag, Notification mNotification) {
        if (WARNING_ID == flag) {
            sNotificationManager.notify(NOTIFICATION_WARNING_ID, mNotification);
        } else if (BOOTED_ID == flag) {
            sNotificationManager.notify(NOTIFICATION_BOOTED_ID, mNotification);
        } else if (REMOVEVIEWS_ID == flag) {
            sNotificationManager.notify(NOTIFICATION_REMOVEVIEWS_ID, mNotification);
        }
    }

    public void notifyAction() {
        sNotificationManager.notify(NOTIFICATION_REMOVEVIEWS_ID, mNotification);
    }

    public void cancelNotification(/* int flag */) {
        // if(WARNING_ID == flag){
        sNotificationManager.cancel(NOTIFICATION_WARNING_ID);
        // }else if(BOOTED_ID == flag){
        sNotificationManager.cancel(NOTIFICATION_BOOTED_ID);
        // }
    }

    public void cancelNotification(int flag) {
        sNotificationManager.cancel(NOTIFICATION_REMOVEVIEWS_ID);
    }

    // Gionee <liuyb> <2014-3-3> add for CR01078882 begin
    public void release() {

    }
    // Gionee <liuyb> <2014-3-3> add for CR01078882 end
}
// Gionee <jianghuan> <2013-12-28> add for CR00975553 end