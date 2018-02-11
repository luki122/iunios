package com.aurora.apihook.app;

import com.android.internal.R;
import android.view.View;
import android.widget.RemoteViews;
import android.content.Context;
import android.graphics.Bitmap;
import java.text.NumberFormat;
import java.util.ArrayList;
import android.app.Notification.Action;
import android.app.Notification;
import android.os.Bundle;
import android.app.Notification.Builder;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.os.SystemClock;
import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

/**
 * Hook Notification applyStandardTemplate/
 * 
 * @author: Rock.Tong
 * @date: 2015-01-19
 */
public class NotificationHook{
	
	public void after_applyStandardTemplate(MethodHookParam param) {
		int resId = (Integer) param.args[0];
		boolean fitIn1U = (Boolean) param.args[1];
		Context mContext = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
        Bitmap mLargeIcon = (Bitmap) ClassHelper.getObjectField(param.thisObject, "mLargeIcon");
        int mPriority = (Integer) ClassHelper.getObjectField(param.thisObject, "mPriority");
        int mSmallIcon = (Integer) ClassHelper.getObjectField(param.thisObject, "mSmallIcon");
        CharSequence mContentTitle = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mContentTitle");
        CharSequence mContentText = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mContentText");
        CharSequence mContentInfo = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mContentInfo");
        int mNumber = (Integer) ClassHelper.getObjectField(param.thisObject, "mNumber");
        CharSequence mSubText = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mSubText");
        long mWhen = (Long) ClassHelper.getObjectField(param.thisObject, "mWhen");
        int mProgressMax = (Integer) ClassHelper.getObjectField(param.thisObject, "mProgressMax");
        boolean mShowWhen = (Boolean) ClassHelper.getObjectField(param.thisObject, "mShowWhen");
        boolean mUseChronometer = (Boolean) ClassHelper.getObjectField(param.thisObject, "mUseChronometer");
        boolean mProgressIndeterminate = (Boolean) ClassHelper.getObjectField(param.thisObject, "mProgressIndeterminate");
        int mProgress = (Integer) ClassHelper.getObjectField(param.thisObject, "mProgress");
		RemoteViews contentView = new RemoteViews(mContext.getPackageName(),
				resId);
		boolean showLine3 = false;
		boolean showLine2 = false;
		int smallIconImageViewId = com.aurora.R.id.icon;
		if (mLargeIcon != null) {
			contentView.setImageViewBitmap(com.aurora.R.id.icon, mLargeIcon);
			smallIconImageViewId = com.aurora.R.id.right_icon;
		}
		if (mPriority < Notification.PRIORITY_LOW) {
			// contentView.setInt(R.id.icon,
			// "setBackgroundResource",
			// R.drawable.notification_template_icon_low_bg);
			// contentView.setInt(com.aurora.R.id.status_bar_latest_event_content,
			// "setBackgroundResource", android.R.drawable.notification_bg_low);
		}
		if (mSmallIcon != 0) {
			contentView.setImageViewResource(smallIconImageViewId, mSmallIcon);
			contentView.setViewVisibility(smallIconImageViewId, View.VISIBLE);
		} else {
			contentView.setViewVisibility(smallIconImageViewId, View.GONE);
		}
		if (mContentTitle != null) {
			contentView.setTextViewText(com.aurora.R.id.title, mContentTitle);
		}
		if (mContentText != null) {
			contentView.setTextViewText(com.aurora.R.id.text, mContentText);
			showLine3 = true;
		}
		if (mContentInfo != null) {
			contentView.setTextViewText(com.aurora.R.id.info, mContentInfo);
			contentView.setViewVisibility(com.aurora.R.id.info, View.VISIBLE);
			showLine3 = true;
		} else if (mNumber > 0) {
			final int tooBig = mContext.getResources().getInteger(
					android.R.integer.status_bar_notification_info_maxnum);
			if (mNumber > tooBig) {
				contentView
						.setTextViewText(
								com.aurora.R.id.info,
								mContext.getResources()
										.getString(
												android.R.string.status_bar_notification_info_overflow));
			} else {
				NumberFormat f = NumberFormat.getIntegerInstance();
				contentView.setTextViewText(com.aurora.R.id.info,
						f.format(mNumber));
			}
			contentView.setViewVisibility(com.aurora.R.id.info, View.VISIBLE);
			showLine3 = true;
		} else {
			contentView.setViewVisibility(com.aurora.R.id.info, View.GONE);
		}

		// Need to show three lines?
		if (mSubText != null) {
			contentView.setTextViewText(com.aurora.R.id.text, mSubText);
			if (mContentText != null) {
				contentView
						.setTextViewText(com.aurora.R.id.text2, mContentText);
				contentView.setViewVisibility(com.aurora.R.id.text2,
						View.VISIBLE);
				showLine2 = true;
			} else {
				contentView.setViewVisibility(com.aurora.R.id.text2, View.GONE);
			}
		} else {
			contentView.setViewVisibility(com.aurora.R.id.text2, View.GONE);
			if (mProgressMax != 0 || mProgressIndeterminate) {
				contentView.setProgressBar(com.aurora.R.id.progress,
						mProgressMax, mProgress, mProgressIndeterminate);
				contentView.setViewVisibility(com.aurora.R.id.progress,
						View.VISIBLE);
				showLine2 = true;
			} else {
				contentView.setViewVisibility(com.aurora.R.id.progress,
						View.GONE);
			}
		}
		if (showLine2) {
			if (fitIn1U) {
				// need to shrink all the type to make sure everything fits
				final Resources res = mContext.getResources();
				final float subTextSize = res
						.getDimensionPixelSize(R.dimen.notification_subtext_size);
				contentView.setTextViewTextSize(com.aurora.R.id.text,
						TypedValue.COMPLEX_UNIT_PX, subTextSize);
			}
			// vertical centering
			contentView.setViewPadding(com.aurora.R.id.line1, 0, 0, 0, 0);
		}

		if (mWhen != 0 && mShowWhen) {
			if (mUseChronometer) {
				contentView.setViewVisibility(com.aurora.R.id.chronometer,
						View.VISIBLE);
				contentView.setLong(
						com.aurora.R.id.chronometer,
						"setBase",
						mWhen
								+ (SystemClock.elapsedRealtime() - System
										.currentTimeMillis()));
				contentView.setBoolean(com.aurora.R.id.chronometer,
						"setStarted", true);
			} else {
				contentView.setViewVisibility(com.aurora.R.id.time,
						View.VISIBLE);
				contentView.setLong(com.aurora.R.id.time, "setTime", mWhen);
			}
		} else {
			contentView.setViewVisibility(com.aurora.R.id.time, View.GONE);
		}

		contentView.setViewVisibility(com.aurora.R.id.line3,
				showLine3 ? View.VISIBLE : View.GONE);
		contentView.setViewVisibility(com.aurora.R.id.overflow_divider,
				showLine3 ? View.VISIBLE : View.GONE);
		param.setResult(contentView);
	}
	
	

	public void after_applyStandardTemplateWithActions(MethodHookParam param) {
		int layoutId = (Integer)param.args[0];
		int MAX_ACTION_BUTTONS = (Integer) ClassHelper.getObjectField(param.thisObject, "MAX_ACTION_BUTTONS");
		ArrayList<Action> mActions = (ArrayList<Action>) ClassHelper.getObjectField(param.thisObject, "mActions");
		
//		RemoteViews big = applyStandardTemplate(layoutId, false);
		RemoteViews big = (RemoteViews)ClassHelper.callMethod(param.thisObject,"applyStandardTemplate",layoutId, false);
        int N = mActions.size();
        if (N > 0) {
            // Log.d("Notification", "has actions: " + mContentText);
            big.setViewVisibility(com.aurora.R.id.actions, View.VISIBLE);
            big.setViewVisibility(com.aurora.R.id.action_divider, View.VISIBLE);
            if (N>MAX_ACTION_BUTTONS) N=MAX_ACTION_BUTTONS;
            big.removeAllViews(com.aurora.R.id.actions);
            for (int i=0; i<N; i++) {
//                final RemoteViews button = generateActionButton(mActions.get(i));
            	final RemoteViews button = (RemoteViews)ClassHelper.callMethod(param.thisObject, "generateActionButton", mActions.get(i));
                big.addView(com.aurora.R.id.actions, button);
            }
        }
        param.setResult(big);
	}

	public void after_makeContentView(MethodHookParam param) {
		Log.d("0121","after_makeContentView");
		RemoteViews mContentView = (RemoteViews) ClassHelper.getObjectField(param.thisObject, "mContentView");
		Log.d("0121","mContentView != null --- " + (mContentView != null) );
		if (mContentView != null) {
			param.setResult(mContentView);
        } else {
        	int res = com.aurora.R.layout.notification_template_base;
        	param.setResult(ClassHelper.callMethod(param.thisObject,"applyStandardTemplate",res, true)); // no more special large_icon flavor
        }
	}
	
	

	public void after_makeTickerView(MethodHookParam param) {
		RemoteViews mTickerView = (RemoteViews) ClassHelper.getObjectField(param.thisObject, "mTickerView");
		RemoteViews mContentView = (RemoteViews) ClassHelper.getObjectField(param.thisObject, "mContentView");
		Bitmap mLargeIcon = (Bitmap) ClassHelper.getObjectField(param.thisObject, "mLargeIcon");;
		if (mTickerView != null) {
			param.setResult(mTickerView);
        } else {
            if (mContentView == null) {
//                return applyStandardTemplate(mLargeIcon == null
//                        ? com.aurora.R.layout.status_bar_latest_event_ticker
//                        : com.aurora.R.layout.status_bar_latest_event_ticker_large_icon, true);
            	param.setResult(ClassHelper.callMethod(param.thisObject,"applyStandardTemplate",mLargeIcon == null
                      ? com.aurora.R.layout.status_bar_latest_event_ticker
                      : com.aurora.R.layout.status_bar_latest_event_ticker_large_icon, true));
            } else {
            	param.setResult(null);
            }
        }
	}

	public void after_makeBigContentView(MethodHookParam param) {
		ArrayList<Action> mActions = (ArrayList<Action>) ClassHelper.getObjectField(param.thisObject, "mActions");
		if (mActions.size() == 0) param.setResult(null);

//        return applyStandardTemplateWithActions(com.aurora.R.layout.notification_template_big_base);
		int res = com.aurora.R.layout.notification_template_big_base;
		param.setResult(ClassHelper.callMethod(param.thisObject,"applyStandardTemplateWithActions",res));
	}
	

	public void after_generateActionButton(MethodHookParam param) {
		Action action = (Action)param.args[0];
		Context mContext = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
		final boolean tombstone = (action.actionIntent == null);
        RemoteViews button = new RemoteViews(mContext.getPackageName(),
                tombstone ? com.aurora.R.layout.notification_action_tombstone
                          : com.aurora.R.layout.notification_action);
        button.setTextViewCompoundDrawablesRelative(com.aurora.R.id.action0, action.icon, 0, 0, 0);
        button.setTextViewText(com.aurora.R.id.action0, action.title);
        if (!tombstone) {
            button.setOnClickPendingIntent(com.aurora.R.id.action0, action.actionIntent);
        }
        button.setContentDescription(com.aurora.R.id.action0, action.title);
        param.setResult(button);
	}
	
	public void after_getStandardView(MethodHookParam param) {
		int layoutId = (Integer)param.args[0];
		CharSequence mBigContentTitle = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mBigContentTitle");
        CharSequence mSummaryText = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mSummaryText");
        boolean mSummaryTextSet = (Boolean) ClassHelper.getObjectField(param.thisObject, "mSummaryTextSet");
        Builder mBuilder = (Builder) ClassHelper.getObjectField(param.thisObject, "mBuilder");
        CharSequence mSubText = (CharSequence) ClassHelper.getObjectField(mBuilder, "mSubText");
        ClassHelper.callMethod(param.thisObject,"checkBuilder");
        Log.d("Notification", "mBigContentTitle != null --- " + (mBigContentTitle != null));
        if (mBigContentTitle != null) {
            mBuilder.setContentTitle(mBigContentTitle);
        }

//        RemoteViews contentView = applyStandardTemplateWithActions(layoutId);
        Log.d("Notification", "layoutId = " + layoutId);
        RemoteViews contentView = (RemoteViews)ClassHelper.callMethod(mBuilder, "applyStandardTemplateWithActions", layoutId);
        Log.d("Notification", "mBigContentTitle != null && mBigContentTitle.equals('')---" + (mBigContentTitle != null && mBigContentTitle.equals("")));
        if (mBigContentTitle != null && mBigContentTitle.equals("")) {
            contentView.setViewVisibility(com.aurora.R.id.line1, View.GONE);
        } else {
            contentView.setViewVisibility(com.aurora.R.id.line1, View.VISIBLE);
        }

        // The last line defaults to the subtext, but can be replaced by mSummaryText
        final CharSequence overflowText =
                mSummaryTextSet ? mSummaryText
                                : mSubText;
        Log.d("Notification", "overflowText = " + overflowText);
        if (overflowText != null) {
            contentView.setTextViewText(com.aurora.R.id.text, overflowText);
            contentView.setViewVisibility(com.aurora.R.id.overflow_divider, View.VISIBLE);
            contentView.setViewVisibility(com.aurora.R.id.line3, View.VISIBLE);
        } else {
        	Log.d("Notification", "com.aurora.R.id.overflow_divider-----com.aurora.R.id.line3-----GONE");
            contentView.setViewVisibility(com.aurora.R.id.overflow_divider, View.GONE);
            contentView.setViewVisibility(com.aurora.R.id.line3, View.GONE);
        }

        param.setResult(contentView);
	}

}