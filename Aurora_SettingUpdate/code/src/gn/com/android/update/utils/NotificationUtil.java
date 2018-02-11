package gn.com.android.update.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";

    public static void clearNotification(Context context, int notificationId) {
        NotificationManager notifyManager = getNotificationManager(context);
        if (null != notifyManager) {
            notifyManager.cancel(notificationId);
        } else {
            LogUtils.loge(TAG, "clearNotification() mNotifyManager is null");
        }
    }

    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void showNotication(Context context, Notification notification, int notificationId) {
        NotificationManager notifyManager = getNotificationManager(context);
        if (null != notifyManager) {
            notifyManager.notify(notificationId, notification);
        } else {
            LogUtils.loge(TAG, "clearNotification() mNotifyManager is null");
        }
    }
}
