package com.aurora.apihook.app;

import com.android.internal.R;
import android.widget.RemoteViews;
import android.content.Context;
import android.graphics.Bitmap;
import android.app.Notification.BigPictureStyle;
import android.app.Notification.Builder;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

/**
 * Hook Notification applyStandardTemplate/
 * 
 * @author: Rock.Tong
 * @date: 2015-01-19
 */
public class NotificationBigPictureStyleHook{
	public void after_makeBigContentView(MethodHookParam param) {
		Bitmap mPicture = (Bitmap) ClassHelper.getObjectField(param.thisObject, "mPicture");
		Builder mBuilder = (Builder) ClassHelper.getObjectField(param.thisObject, "mBuilder");
        CharSequence mSubText = (CharSequence) ClassHelper.getObjectField(mBuilder, "mSubText");
        CharSequence mContentText = (CharSequence) ClassHelper.getObjectField(param.thisObject, "mContentText");
		int res = com.aurora.R.layout.notification_template_big_picture;
		RemoteViews contentView = (RemoteViews)ClassHelper.callMethod(param.thisObject,"getStandardView",res);
        contentView.setImageViewBitmap(com.aurora.R.id.big_picture, mPicture);
        //up to 5.0
//        applyTopPadding(contentView);
        ClassHelper.callMethod(param.thisObject,"applyTopPadding",contentView);
        boolean twoTextLines = mSubText != null && mContentText != null;
//        mBuilder.addProfileBadge(contentView,
//                twoTextLines ? com.aurora.R.id.profile_badge_line2 : com.aurora.R.id.profile_badge_line3);
        //up to 5.0
        param.setResult(contentView);
	}
}