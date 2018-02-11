package com.aurora.apihook.app;

import java.util.ArrayList;

import com.android.internal.R;
import android.widget.RemoteViews;
import android.content.Context;
import android.app.Notification.Builder;
import android.util.TypedValue;
import android.view.View;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

/**
 * Hook Notification applyStandardTemplate/
 * 
 * @author: Rock.Tong
 * @date: 2015-01-19
 */
public class NotificationInboxStyleHook{
	public void after_makeBigContentView(MethodHookParam param) {
		Builder mBuilder = (Builder) ClassHelper.getObjectField(param.thisObject, "mBuilder");
		Context mContext = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
        CharSequence mContentText = (CharSequence) ClassHelper.getObjectField(mBuilder, "mContentText");
		ArrayList<CharSequence> mTexts = (ArrayList<CharSequence>)ClassHelper.getObjectField(param.thisObject, "mTexts");
		int res = com.aurora.R.layout.notification_template_inbox;
     // Remove the content text so line3 disappears unless you have a summary
	    //update to 5.0
		// Nasty
	    CharSequence oldBuilderContentText = mContentText;
	    //update to 5.0
        mBuilder.setContentText(null);
        RemoteViews contentView = (RemoteViews)ClassHelper.callMethod(param.thisObject,"getStandardView",res);
	    //update to 5.0
        mBuilder.setContentText(oldBuilderContentText);
	    //update to 5.0
        contentView.setViewVisibility(com.aurora.R.id.text2, View.GONE);

        int[] rowIds = {com.aurora.R.id.inbox_text0, com.aurora.R.id.inbox_text1, com.aurora.R.id.inbox_text2, com.aurora.R.id.inbox_text3,
                com.aurora.R.id.inbox_text4, com.aurora.R.id.inbox_text5, com.aurora.R.id.inbox_text6};

        // Make sure all rows are gone in case we reuse a view.
        for (int rowId : rowIds) {
            contentView.setViewVisibility(rowId, View.GONE);
        }

        //update to 5.0
        final boolean largeText =
        		mContext.getResources().getConfiguration().fontScale > 1f;
        final float subTextSize = mContext.getResources().getDimensionPixelSize(
                R.dimen.notification_subtext_size);
        //update to 5.0
        int i=0;
        while (i < mTexts.size() && i < rowIds.length) {
            CharSequence str = mTexts.get(i);
            if (str != null && !str.equals("")) {
                contentView.setViewVisibility(rowIds[i], View.VISIBLE);
                contentView.setTextViewText(rowIds[i], str);
                //update to 5.0
                if (largeText) {
                    contentView.setTextViewTextSize(rowIds[i], TypedValue.COMPLEX_UNIT_PX,
                            subTextSize);
                }
                //update to 5.0
            }
            i++;
        }
        contentView.setViewVisibility(com.aurora.R.id.inbox_end_pad,
                mTexts.size() > 0 ? View.VISIBLE : View.GONE);
        contentView.setViewVisibility(com.aurora.R.id.inbox_more,
                mTexts.size() > rowIds.length ? View.VISIBLE : View.GONE);
        //update to 5.0
//        applyTopPadding(contentView);
        ClassHelper.callMethod(param.thisObject,"applyTopPadding",contentView);

//        mBuilder.shrinkLine3Text(contentView);
        ClassHelper.callMethod(mBuilder,"shrinkLine3Text",contentView);

//        mBuilder.addProfileBadge(contentView, R.id.profile_badge_large_template);
//        ClassHelper.callMethod(mBuilder,"addProfileBadge",contentView, com.auroraR.id.profile_badge_large_template);
        //update to 5.0
        param.setResult(contentView);
	}
}