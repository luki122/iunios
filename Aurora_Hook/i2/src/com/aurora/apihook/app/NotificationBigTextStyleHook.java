package com.aurora.apihook.app;

import com.android.internal.R;
import android.view.View;
import android.widget.RemoteViews;
import android.content.Context;
import android.graphics.Bitmap;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import android.util.Log;
/**
 * Hook Notification applyStandardTemplate/
 * 
 * @author: Rock.Tong
 * @date: 2015-01-19
 */
public class NotificationBigTextStyleHook{
	
	public void  after_makeBigContentView(MethodHookParam param) {
		CharSequence mBigText = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mBigText");
		Builder mBuilder = (Builder) ClassHelper.getObjectField(param.thisObject, "mBuilder");
        CharSequence mContentText = (CharSequence) ClassHelper.getObjectField(mBuilder, "mContentText");
        CharSequence mSubText = (CharSequence) ClassHelper.getObjectField(mBuilder, "mSubText");
        Log.d("Notification", "mBigText = " + mBigText);
//		Builder mBuilder = bt.mBuilder;
		
		// Remove the content text so line3 only shows if you have a summary
        //up to 5.0
    	// Nasty
        CharSequence oldBuilderContentText = mContentText;
//        final boolean hadThreeLines = (mContentText != null && mSubText != null);
        mBuilder.setContentText(null);
        //up to 5.0
//        RemoteViews contentView = getStandardView(com.aurora.R.layout.notification_template_big_text);
        int res = com.aurora.R.layout.notification_template_big_text;
        Log.d("Notification", "LAYOUTID--" + res);
        
        RemoteViews contentView = (RemoteViews)ClassHelper.callMethod(param.thisObject, "getStandardView",com.aurora.R.layout.notification_template_big_text);
//        Log.d("Notification", "hadThreeLines--" + hadThreeLines);
        //up to 5.0
        mBuilder.setContentText (oldBuilderContentText);

//        if (hadThreeLines) {
//            // vertical centering
//            contentView.setViewPadding(com.aurora.R.id.line1, 0, 0, 0, 0);
//        }
        //up to 5.0
        Log.d("Notification", "contentView == null " + (contentView == null));
        Log.d("Notification", "mContentText" + (mContentText));
        
        contentView.setTextViewText(com.aurora.R.id.big_text, mBigText);
        contentView.setViewVisibility(com.aurora.R.id.big_text, View.VISIBLE);
      //up to 5.0
//        contentView.setInt(R.id.big_text, "setMaxLines", calculateMaxLines());
        int  calculateMaxLines = (int)ClassHelper.callMethod(param.thisObject, "calculateMaxLines");
        contentView.setInt(R.id.big_text, "setMaxLines", calculateMaxLines);
        //up to 5.0
        contentView.setViewVisibility(com.aurora.R.id.text2, View.GONE);
        //up to 5.0
//        applyTopPadding(contentView);

//        mBuilder.shrinkLine3Text(contentView);
        ClassHelper.callMethod(mBuilder, "shrinkLine3Text", contentView);

//        mBuilder.addProfileBadge(contentView, R.id.profile_badge_large_template);
//        ClassHelper.callMethod(mBuilder, "addProfileBadge", contentView, com.aurora.R.id.profile_badge_large_template);
        //up to 5.0
        param.setResult(contentView);
	}
}