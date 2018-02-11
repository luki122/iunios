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

package com.android.mms.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.ui.BootActivity;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
//Gionee <zhouyj> <2013-05-31> add for CR00820099 begin
import android.database.Cursor;
import android.view.View;
import com.android.mms.data.Conversation;
import gionee.provider.GnTelephony.Threads;
//Gionee <zhouyj> <2013-05-31> add for CR00820099 end

//gionee <gaoj> <2013-06-19> add for CR00826240 begin
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.aurora.mms.ui.AuroraConvListActivity;

import android.provider.Telephony.Sms.Conversations;
//gionee <gaoj> <2013-06-19> add for CR00826240 end

public class MmsWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_NOTIFY_DATASET_CHANGED =
            "com.android.mms.intent.action.ACTION_NOTIFY_DATASET_CHANGED";

    public static final String ACTION_COMPOSE_NEW_MESSAGE =
            "com.android.mms.widget.ACTION_COMPOSE_NEW_MESSAGE";

    private static final String TAG = "MmsWidgetProvider";

    /**
     * Update all widgets in the list
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int i = 0; i < appWidgetIds.length; ++i) {
            updateWidget(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
            Log.v(TAG, "onReceive intent: " + intent);
        }
        String action = intent.getAction();

        // The base class AppWidgetProvider's onReceive handles the normal widget intents. Here
        // we're looking for an intent sent by the messaging app when it knows a message has
        // been sent or received (or a conversation has been read) and is telling the widget it
        // needs to update.
        if (ACTION_NOTIFY_DATASET_CHANGED.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    MmsWidgetProvider.class));
            if (appWidgetIds.length > 0) {
                /// M: fix bug ALPS00389743, all launcher mms widget should update, not frist @{
                //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
                /*appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
                        R.id.conversation_list);*/
                if (MmsApp.mGnLockScrnSupport) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
                            R.id.gn_widget);
                } else {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
                            R.id.conversation_list);
                }
                //gionee <gaoj> <2013-06-29> modify for CR00826240 end
                /// @}
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    /**
     * Update the widget appWidgetId
     */
    private static void updateWidget(Context context, int appWidgetId) {
        if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
            Log.v(TAG, "updateWidget appWidgetId: " + appWidgetId);
        }
        //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
        RemoteViews remoteViews = null;

        // Launch an intent to avoid ANRs
        final Intent intent = new Intent(context, MmsWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        if (MmsApp.mGnLockScrnSupport) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.gn_infozone_widget);
            remoteViews.setRemoteAdapter(appWidgetId, R.id.gn_infozone_mms_list, intent);
        } else {
            //gionee <gaoj> <2013-06-29> modify for CR00826240 end
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            
            //gionee <gaoj> <2013-07-01> add for CR00826240 begin
            remoteViews.setViewVisibility(R.id.conversation_list, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.widget_header, View.VISIBLE);
            //gionee <gaoj> <2013-07-01> add for CR00826240 end
            
        PendingIntent clickIntent;

        remoteViews.setRemoteAdapter(appWidgetId, R.id.conversation_list, intent);

        remoteViews.setTextViewText(R.id.widget_label, context.getString(R.string.app_label));

          // Aurora liugj 2013-12-12 modified for App start optimize start 
        // Open Mms's app conversation list when click on header
        final Intent convIntent = new Intent(context, AuroraConvListActivity.class);
          // Aurora liugj 2013-12-12 modified for App start optimize end
        clickIntent = PendingIntent.getActivity(
                context, 0, convIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_header, clickIntent);

        // On click intent for Compose
//        final Intent composeIntent = new Intent(context, ComposeMessageActivity.class);
//        composeIntent.setAction(Intent.ACTION_SENDTO);
//        clickIntent = PendingIntent.getActivity(
//                context, 0, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.widget_compose, clickIntent);

        // On click intent for Conversation
//        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
//        taskStackBuilder.addParentStack(ComposeMessageActivity.class);
//        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
//        msgIntent.setType("vnd.android-dir/mms-sms");
//        taskStackBuilder.addNextIntent(msgIntent);
//        remoteViews.setPendingIntentTemplate(R.id.conversation_list,
//                taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));

        // On click intent for Compose
//        final Intent composeIntent = new Intent(context, MmsWidgetProxy.class);
//        composeIntent.setAction(ACTION_COMPOSE_NEW_MESSAGE);
//        clickIntent = PendingIntent.getBroadcast(
//                context, 0, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.widget_compose, clickIntent);

        final Intent composeIntent = new Intent(context, MmsWidgetProxyActivity.class);
        composeIntent.setAction(ACTION_COMPOSE_NEW_MESSAGE);
        clickIntent = PendingIntent.getActivity(
                context, 0, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_compose, clickIntent);

        // On click intent for Conversation
//        Intent ic = new Intent(context, MmsWidgetProxy.class);
//        remoteViews.setPendingIntentTemplate(R.id.conversation_list,
//                PendingIntent.getBroadcast(context, 0, ic, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent ic = new Intent(context, MmsWidgetProxyActivity.class);
        remoteViews.setPendingIntentTemplate(R.id.conversation_list,
                PendingIntent.getActivity(context, 0, ic, PendingIntent.FLAG_UPDATE_CURRENT));

        //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
        }
        //gionee <gaoj> <2013-06-29> modify for CR00826240 end
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews);
    }

    /*
     * notifyDatasetChanged call when the conversation list changes so the mms widget will
     * update and reflect the changes
     */
    public static void notifyDatasetChanged(Context context) {
        if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
            Log.v(TAG, "notifyDatasetChanged");
        }
        final Intent intent = new Intent(ACTION_NOTIFY_DATASET_CHANGED);
        context.sendBroadcast(intent);
    }

}
