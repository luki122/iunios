package com.gionee.mms.adaptor;

import java.util.ArrayList;
import java.util.Iterator;

import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Plus notification manager.
 * It will receive {
 * NotificationManagerPlus#ACTION_FULL_SCREEN_NOTIFY}
 * and notify user via a dialog.
 *
 */
public class NotificationManagerPlus {
    private static final String TAG = "NotificationManangerPlus";
    private static final boolean LOG = true;
    
    private static final int MSG_SHOW = 1;
    private boolean mSend = true;
    private boolean mListening = false;
    private Parameters mParams;
    private IntentFilter mFilter;
    private ArrayList<DialogPlus> mList = new ArrayList<DialogPlus>();
    
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (LOG) Log.i(TAG, "handleMessage() " + msg);
            switch(msg.what) {
            case MSG_SHOW:
                if (!(msg.obj instanceof Bundle)) return;
                Bundle extra = (Bundle) msg.obj;
                resolve(extra);
                break;
            default:
                break;
            }
        }

    };
    
    private void resolve(Bundle extra) {
        String packageName = extra.getString(NotificationPlus.EXTRA_PACKAGE_NAME);
        int type = extra.getInt(NotificationPlus.EXTRA_TYPE, NotificationPlus.TYPE_UNKNOWN);
        if (LOG) Log.i(TAG, "resolve(" + extra + ") packageName=" + packageName + ", type=" + type);
        if (packageName == null || 
                (type != NotificationPlus.TYPE_NOTIFY && type != NotificationPlus.TYPE_CANCEL)) {
            return;
        }
        boolean hasId = extra.containsKey(NotificationPlus.EXTRA_ID);
        int id = -1;
        if (hasId) {
            id = extra.getInt(NotificationPlus.EXTRA_ID);
        }
        if (type == NotificationPlus.TYPE_CANCEL) {
            if (hasId) {//cancel one
                synchronized(mList) {
                    for(Iterator<DialogPlus> iter = mList.iterator(); iter.hasNext();) {
                        DialogPlus dialog = iter.next();
                        if (equals(dialog, packageName, id)) {
                            iter.remove();//remove it
                            dialog.cancel();//cancel it
                            break;
                        }
                    }
                }
            } else {//cancle all
                for(Iterator<DialogPlus> iter = mList.iterator(); iter.hasNext();) {
                    DialogPlus dialog = iter.next();
                    if (equals(dialog, packageName)) {
                        iter.remove();//remove it
                        dialog.cancel();//cancel it
                        break;
                    }
                }
            }
            return;
        }
        DialogPlus find = null;
        if (!hasId) {
            Log.w(TAG, "resolve(" + extra + ") type=notify, but no id!");
            return; 
        } else {
            for(Iterator<DialogPlus> iter = mList.iterator(); iter.hasNext();) {
                DialogPlus temp = iter.next();
                if (equals(temp, packageName, id)) {
                    find = temp;//find it
                    break;
                }
            }
        }
        DialogPlus newDialog = new DialogPlus(mParams.mContext);
        newDialog.setPackageName(packageName);
        newDialog.setId(id);
        newDialog.setTitle(extra.getString(NotificationPlus.EXTRA_CONTENT_TITLE));
        newDialog.setMessage(extra.getString(NotificationPlus.EXTRA_CONTENT_TEXT));
        if (mParams.mPositiveButtonHandled) {//manager handle it
            newDialog.setButton(Dialog.BUTTON_POSITIVE, mParams.mPositiveButtonText, mParams.mPositiveButtonListener);
        } else if (extra.containsKey(NotificationPlus.EXTRA_BUTTON_NAME_POSITIVE)) {
            final PendingIntent positive = extra.getParcelable(NotificationPlus.EXTRA_BUTTON_INTENT_POSITIVE);
            newDialog.setButton(Dialog.BUTTON_POSITIVE, extra.getString(NotificationPlus.EXTRA_BUTTON_NAME_POSITIVE), buildOnClickListenr(positive));
        }
        if (mParams.mNeutralButtonHandled) {//manager handle it
            newDialog.setButton(Dialog.BUTTON_NEUTRAL, mParams.mNeutralButtonText, mParams.mNeutralButtonListener);
        } else if (extra.containsKey(NotificationPlus.EXTRA_BUTTON_NAME_NEUTRAL)) {
            final PendingIntent neutral = extra.getParcelable(NotificationPlus.EXTRA_BUTTON_INTENT_NEUTRAL);
            newDialog.setButton(Dialog.BUTTON_NEUTRAL, extra.getString(NotificationPlus.EXTRA_BUTTON_NAME_NEUTRAL), buildOnClickListenr(neutral));
        }
        if (mParams.mNegativeButtonHandled) {//manager handle it
            newDialog.setButton(Dialog.BUTTON_NEGATIVE, mParams.mNegativeButtonText, mParams.mNegativeButtonListener);
        } else if (extra.containsKey(NotificationPlus.EXTRA_BUTTON_NAME_NEGATIVE)) {
            final PendingIntent negative = extra.getParcelable(NotificationPlus.EXTRA_BUTTON_INTENT_NEGATIVE);
            newDialog.setButton(Dialog.BUTTON_NEGATIVE, extra.getString(NotificationPlus.EXTRA_BUTTON_NAME_NEGATIVE), buildOnClickListenr(negative));
        }
        if (mParams.mCancelable) {//if manager set cancelable true
            if (mParams.mCancelHandled) {//manager handle it
                newDialog.setOnCancelListener(mParams.mOnCancelListener);
            } else {
                if (extra.containsKey(NotificationPlus.EXTRA_CANCELABLE)) {//user set cancelable
                    mParams.mCancelable = extra.getBoolean(NotificationPlus.EXTRA_CANCELABLE);
                }
                if (mParams.mCancelable) {//user handle it
                    final PendingIntent cancel = extra.getParcelable(NotificationPlus.EXTRA_CANCEL_INTENT);
                    newDialog.setOnCancelListener(buildOnCancelListener(cancel));
                }
            }
        }
        newDialog.setOnDismissListener(mOnDismissListener);
        newDialog.setOnShowListener(mOnShowListener);
        newDialog.show();
        if (find != null) {
            find.dismiss();//not cancel for replacing
        }
        if (LOG) Log.i(TAG, "resolve(" + extra + ") find=" + find);
    }
    
    public boolean equals(DialogPlus cmp, String packageName, int id) {
        boolean result = false;
        if (id == cmp.mId) {
            if (packageName == null && cmp.mPackageName == null) {
                result = true;
            } else if (packageName != null && cmp.mPackageName != null
                    && packageName.equals(cmp.mPackageName)) {
                result = true;
            }
        }
        return result;
    }
    
    public boolean equals(DialogPlus cmp, String packageName) {
        boolean result = false;
        if (packageName == null && cmp.mPackageName == null) {
            result = true;
        } else if (packageName != null && cmp.mPackageName != null
                && packageName.equals(cmp.mPackageName)) {
            result = true;
        }
        return result;
    }
    
    private OnClickListener buildOnClickListenr(final PendingIntent pending) {
        if (pending == null) return null;
        return new OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                if (LOG) Log.i(TAG, "onClick(" + which + ") pending=" + pending);
                try {
                    pending.send();
                } catch (CanceledException e) {
                    e.printStackTrace();
                }
            }
        };
    }
    
    private OnCancelListener buildOnCancelListener(final PendingIntent pending) {
        if (pending == null) return null;
        return new OnCancelListener() {
            
            public void onCancel(DialogInterface dialog) {
                if (LOG) Log.i(TAG, "onCancel() pending=" + pending);
                try {
                    if (pending != null) {
                        pending.send();
                    }
                } catch (CanceledException e) {
                    e.printStackTrace();
                }
            }
        };
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOG) Log.i(TAG, "onReceive(" + intent + ") mSend=" + mSend);
            if (!mSend) return;
            if (NotificationPlus.ACTION_FULL_SCREEN_NOTIFY.equals(intent.getAction())) {
                Message msg = mHandler.obtainMessage(MSG_SHOW);
                msg.obj = intent.getExtras();
                msg.sendToTarget();
            }
        }
    };
    
    private OnDismissListener mOnDismissListener = new OnDismissListener() {
        
        public void onDismiss(DialogInterface dialog) {
            synchronized(mList) {
                boolean remove = mList.remove(dialog);
                if (remove && mParams.mOnDismissListener != null) {
                    mParams.mOnDismissListener.onDismiss(dialog);
                }
                if (remove && mList.size() == 0 && mParams.mOnLastDismissListener != null) {
                    mParams.mOnLastDismissListener.onLastDismiss(dialog);
                }
                if (LOG) Log.i(TAG, "onDismiss() size=" + mList.size() + ", remove=" + remove);
            }
        }
    };
    
    private OnShowListener mOnShowListener = new OnShowListener() {
        
        public void onShow(DialogInterface dialog) {
            synchronized(mList) {
                boolean add = mList.add((DialogPlus) dialog);
                if (add && mList.size() == 1 && mParams.mOnFirstShowListener != null) {
                    mParams.mOnFirstShowListener.onFirstShow(dialog);
                }
                if (add && mParams.mOnShowListener != null) {
                    mParams.mOnShowListener.onShow(dialog);
                }
                if (LOG) Log.i(TAG, "onShow() size=" + mList.size() + ", add=" + add);
            }
        }
    };
    
    private NotificationManagerPlus(Parameters p) {
        if (p.mContext == null) {
            throw new NullPointerException("context not allowed null!");
        }
        mParams = p;
        mFilter = new IntentFilter();
        mFilter.addAction(NotificationPlus.ACTION_FULL_SCREEN_NOTIFY);
        mFilter.addCategory(Intent.CATEGORY_DEFAULT);
    }
    
    /**
     * Resume the notification listening action.
     */
    public void resume() {
        if (LOG) Log.i(TAG, "resume() mSend=" + mSend);
        mSend = true;
    }
    
    /**
     * Puase the notification listening action.
     */
    public void pause() {
        if (LOG) Log.i(TAG, "pause() mSend=" + mSend);
        mSend = false;
        mHandler.removeMessages(MSG_SHOW);
    }
    
    /**
     * Start listen notification event.
     */
    public void startListening() {
        if (LOG) Log.i(TAG, "startListening() mListening=" + mListening);
        if (!mListening) {
            mParams.mContext.registerReceiver(mReceiver, mFilter);
            mListening = true;
        }
    }
    
    /**
     * Stop listen notification event.
     * Should be called if you do not use NotificationManangerPlus.
     */
    public void stopListening() {
        if (LOG) Log.i(TAG, "stopListening() mListening=" + mListening);
        if (mListening) {
            mHandler.removeMessages(MSG_SHOW);
            mParams.mContext.unregisterReceiver(mReceiver);
            mListening = false;
        }
    }
    
    /**
     * Cancel all the plus notifications.
     * It call cancelAll(true).
     */
    public void cancelAll() {
        cancelAll(true);
    }
    
    /**
     * Cancel all the plus notifications.
     * @param ignoreAction if true OnDismissListener and OnLastDismissListener will not be called.
     */
    public void cancelAll(boolean ignoreAction) {
        if (LOG) Log.i(TAG, "cancelAll(" + ignoreAction + ") size=" + mList.size());
        synchronized(mList) {
            for(Dialog dialog : mList) {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
            if (ignoreAction) {
                mList.clear();
            }
        }
    }
    
    /**
     * Dismiss all the plus notifications.
     * It call clearAll(true).
     */
    public void clearAll() {
        clearAll(true);
    }
    
    /**
     * Dismiss all the plus notifications.
     * @param ignoreAction if true OnDismissListener and OnLastDismissListener will not be called.
     */
    public void clearAll(boolean ignoreAction) {
        if (LOG) Log.i(TAG, "clearAll(" + ignoreAction + ") size=" + mList.size());
        synchronized(mList) {
            for(Dialog dialog : mList) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            if (ignoreAction) {
                mList.clear();
            }
        }
    }
    
    /**
     * 
     * @param id An identifier for this notification unique within your
     *        application.
     * @param notification Must not be null.
     */
    public static void notify(int id, NotificationPlus notification) {
        notification.setId(id);
        notification.setType(NotificationPlus.TYPE_NOTIFY);
        notification.send();
    }
    
    /**
     * Cancel a previously shown notification in server. 
     * @param context must not be null.
     * @param id
     */
    public static void cancel(Context context, int id) {
        NotificationPlus notification = new NotificationPlus(context);
        notification.setId(id);
        notification.setType(NotificationPlus.TYPE_CANCEL);
        notification.send();
    }

    /**
     * Cancel all previously shown notifications.
     * @param context must not be null.
     */
    public static void cancelAll(Context context) {
        NotificationPlus notification = new NotificationPlus(context);
        notification.setType(NotificationPlus.TYPE_CANCEL);
        notification.send();
    }
    
    private static class DialogPlus extends AuroraAlertDialog{

        protected DialogPlus(Context context) {
            super(context);
        }

        public int mId;//id for dialog
        public String mPackageName;//package name for dialog
        
        public void setId(int id) {
            mId = id;
        }
        
        public void setPackageName(String packageName) {
            mPackageName = packageName;
        }
        
    }
    
    private static class Parameters {
        /*package*/ Context mContext;
        
        /*package*/ boolean mPositiveButtonHandled;
        /*package*/ CharSequence mPositiveButtonText;
        /*package*/ DialogInterface.OnClickListener mPositiveButtonListener;
        
        /*package*/ boolean mNegativeButtonHandled;
        /*package*/ CharSequence mNegativeButtonText;
        /*package*/ DialogInterface.OnClickListener mNegativeButtonListener;
        
        /*package*/ boolean mNeutralButtonHandled;
        /*package*/ CharSequence mNeutralButtonText;
        /*package*/ DialogInterface.OnClickListener mNeutralButtonListener;
        
        /*package*/ boolean mCancelHandled;
        /*package*/ boolean mCancelable;
        /*package*/ DialogInterface.OnCancelListener mOnCancelListener;

        /*package*/ DialogInterface.OnDismissListener mOnDismissListener;
        /*package*/ DialogInterface.OnShowListener mOnShowListener;
        
        /*package*/ OnLastDismissListener mOnLastDismissListener;
        /*package*/ OnFirstShowListener mOnFirstShowListener;
    }
    
    /**
     * Helper class for building a {@link NotificationManagerPlus}.
     */
    public static class ManagerBuilder {
        private Parameters P = new Parameters();
        /**
         * @param context shouldn't be null.
         */
        public ManagerBuilder(Context context) {
            P.mContext = context;
            P.mCancelable = true;
        }
        
        /**
         * Set a listener to be invoked when the positive button of the plus notification is pressed.
         * Note: this will disable client's Positive PendingIntent
         * @param text The text to display in the positive button
         * @param listener The {@link DialogInterface.OnClickListener} to use.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public ManagerBuilder setPositiveButton(CharSequence text, final OnClickListener listener) {
            P.mPositiveButtonText = text;
            P.mPositiveButtonListener = listener;
            P.mPositiveButtonHandled = true;
            return this;
        }
        
        /**
         * Set a listener to be invoked when the neutral button of the plus notification is pressed.
         * Note: this will disable client's Neutral PendingIntent
         * @param text The text to display in the neutral button
         * @param listener The {@link DialogInterface.OnClickListener} to use.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public ManagerBuilder setNeutralButton(CharSequence text, final OnClickListener listener) {
            P.mNeutralButtonText = text;
            P.mNeutralButtonListener = listener;
            P.mNeutralButtonHandled = true;
            return this;
        }
        
        /**
         * Set a listener to be invoked when the negative button of the plus notification is pressed.
         * Note: this will disable client's Negative PendingIntent
         * @param text The text to display in the negative button
         * @param listener The {@link DialogInterface.OnClickListener} to use.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public ManagerBuilder setNegativeButton(CharSequence text, final OnClickListener listener) {
            P.mNegativeButtonText = text;
            P.mNegativeButtonListener = listener;
            P.mNegativeButtonHandled = true;
            return this;
        }
        
        /**
         * Sets the callback that will be called if the plus notification is canceled.
         * @see #setCancelable(boolean)
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public ManagerBuilder setOnCancelListener(final OnCancelListener listener) {
            P.mOnCancelListener = listener;
            P.mCancelHandled = true;
            return this;
        }
        
        /**
         * Sets whether the plus notification is cancelable or not.  Default is true.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public ManagerBuilder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }
        /**
         * Set a listener to be invoked when the plus notification is dismissed.
         * <br/>Note: will be called if every plus notification is dismissed. 
         * @param listener The {@link DialogInterface.OnDismissListener} to use.
         */
        public ManagerBuilder setOnDismissListener(final OnDismissListener listener) {
            P.mOnDismissListener = listener;
            return this;
        }

        /**
         * Sets a listener to be invoked when the plus notification is shown.
         * <br/>Note: will be called if every plus notification is shown.
         * @param listener The {@link DialogInterface.OnShowListener} to use.
         */
        public ManagerBuilder setOnShowListener(final OnShowListener listener) {
            P.mOnShowListener = listener;
            return this;
        }
        /**
         * Set a listener to be invoked when the plus notification is last dismissed.
         * <br/>Note: maybe not be called every plus notification is dismissed. 
         * @param listener
         */
        public ManagerBuilder setOnLastDismissListener(final OnLastDismissListener listener) {
            P.mOnLastDismissListener = listener;
            return this;
        }

        /**
         * Sets a listener to be invoked when the plus notification is first shown.
         * <br/>Note: maybe not be called every plus notification is shown.
         * @param listener
         */
        public ManagerBuilder setOnFirstShowListener(final OnFirstShowListener listener) {
            P.mOnFirstShowListener = listener;
            return this;
        }
        /**
         * Get the instance of NotificationManangerPlus.
         */
        public NotificationManagerPlus create() {
            return new NotificationManagerPlus(P);
        }
    }
    /**
     * Interface used to allow the creator of a notification to run some code 
     * when the notification is first shown.
     * for example: show A --> show B:
     * A will be called OnFirstShowListener.onShow() and OnShowListener.onShow();
     * B will be called OnShowListener.onShow().
     * show A --> dismiss A --> show B -->dimiss B:
     * A will be called OnFirstShowListener.onShow() and OnShowListener.onShow().
     * B will be called OnFirstShowListener.onShow() and OnShowListener.onShow() too;
     */
    public interface OnFirstShowListener {
        public void onFirstShow(DialogInterface dialog);
    }
    /**
     * Interface used to allow the creator of a notification to run some code 
     * when the notification is last dismissed.
     * for example: show A --> show B --> dismiss B --> dismiss A:
     * A will be called OnLastDismissListener.onDismiss() and OnDismissListener.onDismiss();
     * B will be called OnDismissListener.onDismiss().
     * show A --> dismiss A --> show B -->dimiss B:
     * A will be called OnLastDismissListener.onDismiss() and OnDismissListener.onDismiss().
     * B will be called OnLastDismissListener.onDismiss() and OnDismissListener.onDismiss() too;
     */
    public interface OnLastDismissListener {
        public void onLastDismiss(DialogInterface dialog);
    }
}
