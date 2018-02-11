package com.gionee.mms.adaptor;

import java.util.Set;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * A class that represents how a persistent notification is to be presented to
 * the user using the {NotificationManagerPlus}.
 * <br/>Note: this is different from Notification, it just been shown if server care it.
 */
public class NotificationPlus {
    private static final String TAG = "NotificationPlus";
    private static final boolean LOG = true;
    /**
     * Broadcast Action: notification has been sent.
     * Apps, who cares this scenario, will show notification dialog automatically.
     */
    /*package*/ static final String ACTION_FULL_SCREEN_NOTIFY = "mediatek.intent.action.FULL_SCRENN_NOTIFY";
    /**
     * Used as a string value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told NotificationManangerPlus the title of dialog.
     */
    /*package*/ static final String EXTRA_CONTENT_TITLE = "mediatek.intent.extra.content.title";
    /**
     * Used as a string value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} the message of dialog.
     */
    /*package*/ static final String EXTRA_CONTENT_TEXT = "mediatek.intent.extra.content.text";
    /**
     * Used as a String value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} how to show positive button's name.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_BUTTON_NAME_POSITIVE = "mediatek.intent.extra.button.name.positive";
    /**
     * Used as a String value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} how to show negative button's name.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_BUTTON_NAME_NEGATIVE = "mediatek.intent.extra.button.name.negative";
    /**
     * Used as a String value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} how to show neutral button's name.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_BUTTON_NAME_NEUTRAL = "mediatek.intent.extra.button.name.neutral";
    /**
     * Used as a PendingIntent value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} what will be done after user click the positive button.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_BUTTON_INTENT_POSITIVE = "mediatek.intent.extra.button.intent.positive";
    /**
     * Used as a PendingIntent value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} what will be done after user click the negative button.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_BUTTON_INTENT_NEGATIVE = "mediatek.intent.extra.button.intent.negative";
    /**
     * Used as a PendingIntent value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} what will be done after user click the neutral button.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_BUTTON_INTENT_NEUTRAL = "mediatek.intent.extra.button.intent.neutral";
    /**
     * Used as a PendingIntent value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} what will be done after user cancel the notification.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_CANCEL_INTENT = "mediatek.intent.extra.cancel.intent";
    /**
     * Used as a boolean value for {@link #ACTION_FULL_SCREEN_NOTIFY}
     * to told {@link NotificationManagerPlus} let user cancel the dialog or not.
     * Note: will not be used if manager diable it.
     */
    /*package*/ static final String EXTRA_CANCELABLE = "mediatek.intent.extra.cancel.enable";
    /*package*/ static final int TYPE_NOTIFY = 1;
    /*package*/ static final int TYPE_CANCEL = 2;
    /*package*/ static final int TYPE_UNKNOWN = -1;
    /*package*/ static final int ID_UNKNOWN = -1;
    /*package*/ static final String EXTRA_TYPE = "mediatek.intent.extra.type";
    /*package*/ static final String EXTRA_ID = "mediatek.intent.extra.id";
    /*package*/ static final String EXTRA_PACKAGE_NAME = "mediatek.intent.extra.package";
    
    /*package*/ Context mContext;
    /*package*/ Intent mIntent;
    /*package*/ NotificationPlus(Context context) {
        mContext = context;
        mIntent = new Intent(ACTION_FULL_SCREEN_NOTIFY);
        mIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mIntent.putExtra(EXTRA_PACKAGE_NAME, mContext.getPackageName());
    }
    
    /*package*/ void setType(int type) {
        mIntent.putExtra(EXTRA_TYPE, type);
    }
    
    /*package*/ void setId(int id) {
        mIntent.putExtra(EXTRA_ID, id);
    }
    
    /*package*/ void send() {
        mContext.sendBroadcast(mIntent);
        if (LOG) Log.i(TAG, "send() " + mIntent);
        if (LOG) {
            Bundle extras = mIntent.getExtras();
            Set<String> keys = extras.keySet();
            for(String key : keys) {
                Log.i(TAG, "send() key=" + key + ", value=" + extras.get(key));
            }
        }
    }
    
    /**
     * Helper class for building plus notification and notifing server.
     *
     */
    public static class Builder {
        private NotificationPlus mNotification;
        
        /**
         * Init the notification builder.
         */
        public Builder(Context context) {
            mNotification = new NotificationPlus(context);
        }
        /**
         * Set the title for plus notification.
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            mNotification.mIntent.putExtra(EXTRA_CONTENT_TITLE, title);
            return this;
        }
        /**
         * Set the message for plus notification.
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            mNotification.mIntent.putExtra(EXTRA_CONTENT_TEXT, message);
            return this;
        }
        /**
         * Set the button text and pending action for positive button.
         * <br/>Note: this will enable positive button function for server,
         * unless server don't disable this function.
         * @param name
         * @param pendingIntent
         * @return
         */
        public Builder setPositiveButton(String name, PendingIntent pendingIntent) {
            mNotification.mIntent.putExtra(EXTRA_BUTTON_NAME_POSITIVE, name);
            mNotification.mIntent.putExtra(EXTRA_BUTTON_INTENT_POSITIVE, pendingIntent);
            return this;
        }
        /**
         * Set the button text and pending action for neutral button.
         * <br/>Note: this will enable neutral button function for server,
         * unless server don't disable this function.
         * @param name
         * @param pendingIntent
         * @return
         */
        public Builder setNeutralButton(String name, PendingIntent pendingIntent) {
            mNotification.mIntent.putExtra(EXTRA_BUTTON_NAME_NEUTRAL, name);
            mNotification.mIntent.putExtra(EXTRA_BUTTON_INTENT_NEUTRAL, pendingIntent);
            return this;
        }
        /**
         * Set the button text and pending action for negative button.
         * <br/>Note: this will enable negative button function for server,
         * unless server don't disable this function.
         * @param name
         * @param pendingIntent
         * @return
         */
        public Builder setNegativeButton(String name, PendingIntent pendingIntent) {
            mNotification.mIntent.putExtra(EXTRA_BUTTON_NAME_NEGATIVE, name);
            mNotification.mIntent.putExtra(EXTRA_BUTTON_INTENT_NEGATIVE, pendingIntent);
            return this;
        }
        
        /**
         * Sets whether the plus notification is cancelable or not.  Default is true.
         * <br/>Note: this will enable cancelable function for server,
         * unless server don't disable this function.
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setCancelable(boolean cancelable) {
            mNotification.mIntent.putExtra(EXTRA_CANCELABLE, cancelable);
            return this;
        }
        
        /**
         * Sets the callback that will be called if the plus notification is canceled.
         * <br/>Note: this will enable cancelable function for server,
         * unless server don't disable this function.
         * @see #setCancelable(boolean)
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnCancelListener(PendingIntent pendingIntent) {
            mNotification.mIntent.putExtra(EXTRA_CANCEL_INTENT, pendingIntent);
            return this;
        }
        
        /**
         * 
         * @return the done notifcation.
         */
        public NotificationPlus create() {
            return mNotification;
        }
    }
    
}
