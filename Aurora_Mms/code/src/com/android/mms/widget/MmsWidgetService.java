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
import android.appwidget.AppWidgetManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Handler;
//import android.provider.Telephony.Threads;
import gionee.provider.GnTelephony.Threads;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ComposeMessageActivity;
import com.aurora.mms.ui.AuroraConvListActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.ConversationListItem;
import com.android.mms.ui.FolderViewList;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.SmileyParser;
import com.privacymanage.service.AuroraPrivacyUtils;

import java.util.Collection;
import java.util.Map;
import com.android.mms.MmsApp;
import com.gionee.mms.ui.ConvFragment;
//gionee <gaoj> <2013-06-29> modify for CR00826240 begin
import android.provider.Telephony.Sms.Conversations;
//gionee <gaoj> <2013-06-29> modify for CR00826240 end

public class MmsWidgetService extends RemoteViewsService {
    private static final String TAG = "MmsWidgetService";

    public static final String ACTION_FOLDER_MODE = "com android.mms.widget.ACTION_FOLDER_MODE";
    public static final String ACTION_CONVERSATION_MODE = "com android.mms.widget.ACTION_CONVERSATION_MODE";
    public static final String EXTRA_KEY_FOLDER_TYPE = "folder_type";
    public static final String EXTRA_KEY_CONVSATION_TYPE = "conversation_type";
    public static final String EXTRA_KEY_THREAD_ID = "thread_id";

    public static final int FOLDER_HAS_UNREAD = 1;
    public static final int FOLDER_CB_OR_PUSH = 2;
    public static final int FOLDER_HAS_DRAFT  = 3;
    public static final int FOLDER_HAS_ERROR  = 4;
    public static final int FOLDER_NORMAL     = 5;

    public static final int MORE_MESSAGES     = 600;
    /**
     * Lock to avoid race condition between widgets.
     */
    private static final Object sWidgetLock = new Object();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
            Log.v(TAG, "onGetViewFactory intent: " + intent);
        }
        return new MmsFactory(getApplicationContext(), intent);
    }

    /**
     * Remote Views Factory for Mms Widget.
     */
    private static class MmsFactory
            implements RemoteViewsService.RemoteViewsFactory, Contact.UpdateListener {
        private static final int MAX_CONVERSATIONS_COUNT = 20;
        private final Context mContext;
        private final int mAppWidgetId;
        private boolean mShouldShowViewMore;
        private Cursor mConversationCursor;
        private int mUnreadConvCount;
        private AppWidgetManager mAppWidgetManager = null;

        // Static colors
        private static int SUBJECT_TEXT_COLOR_READ;
        private static int SUBJECT_TEXT_COLOR_UNREAD;
        private static int SENDERS_TEXT_COLOR_READ;
        private static int SENDERS_TEXT_COLOR_UNREAD;

        private Handler mHandler = new Handler();

        //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
        private Cursor mNewConvCursor;
        //gionee <gaoj> <2013-06-29> modify for CR00826240 end

        public MmsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            mAppWidgetManager = AppWidgetManager.getInstance(context);
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "MmsFactory intent: " + intent + "widget id: " + mAppWidgetId);
            }
            // Initialize colors
            Resources res = context.getResources();
            SENDERS_TEXT_COLOR_READ = res.getColor(R.color.widget_sender_text_color_read);
            SENDERS_TEXT_COLOR_UNREAD = res.getColor(R.color.widget_sender_text_color_unread);
            SUBJECT_TEXT_COLOR_READ = res.getColor(R.color.widget_subject_text_color_read);
            SUBJECT_TEXT_COLOR_UNREAD = res.getColor(R.color.widget_subject_text_color_unread);
        }

        @Override
        public void onCreate() {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "onCreate");
            }
            Contact.addListener(this);
        }

        @Override
        public void onDestroy() {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "onDestroy");
            }
            mAppWidgetManager = null;
            synchronized (sWidgetLock) {
                if (mConversationCursor != null && !mConversationCursor.isClosed()) {
                    mConversationCursor.close();
                    mConversationCursor = null;
                }
                Contact.removeListener(this);
            }
        }

        @Override
        public void onDataSetChanged() {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "onDataSetChanged");
            }
            synchronized (sWidgetLock) {
                if (mConversationCursor != null) {
                    mConversationCursor.close();
                    mConversationCursor = null;
                }
                //gionee gaoj 2013-3-15 added for CR00784388 start
                if (MmsApp.mIsSafeModeSupport) {
                    //nothing
                    //Gionee <zhouyj> <2013-05-28> add for CR00819617 begin
                    mUnreadConvCount = 0;
                    //Gionee <zhouyj> <2013-05-28> add for CR00819617 end
                } 
                //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
                else if (MmsApp.mGnLockScrnSupport) {
                    mNewConvCursor = queryOneConversations();
                }
                //gionee <gaoj> <2013-06-29> modify for CR00826240 end
                else {
                    //gionee gaoj 2013-3-15 added for CR00784388 end
                //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
                /*mConversationCursor = queryAllConversations();*/
                    if (MmsApp.mGnHideEncryption) {
                        mConversationCursor = startQuery();
                    } else {
                        mConversationCursor = queryAllConversations();
                    }
                //Gionee <gaoj> <2013-05-21> added for CR00817770 end
                mUnreadConvCount = queryUnreadCount();
                //gionee gaoj 2013-3-15 added for CR00784388 start
                }
                //gionee gaoj 2013-3-15 added for CR00784388 end
                onLoadComplete();
                mHandler.removeCallbacks(mUpdateFromContactRunnable);
                mHandler.postDelayed(mUpdateFromContactRunnable, 60000);
            }
        }

        //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
        private Cursor queryOneConversations() {
            return mContext.getContentResolver().query(
                    Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION,
                    "(message_count!=0 and read = 0)", null, Conversations.DEFAULT_SORT_ORDER + " LIMIT 1");
        }
        //gionee <gaoj> <2013-06-29> modify for CR00826240 end

        //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
        private Cursor startQuery() {
            if (ConvFragment.ReadPopTag(mContext, ConvFragment.HIDEENCRYPTION)) {
                return queryNoEncryptions();
            } else {
                return queryAllConversations();
            }
        }

        private Cursor queryNoEncryptions() {
            return mContext.getContentResolver().query(
                    Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION,
                    "(message_count!=0 and encryption = 0)", null, null);
        }
        //Gionee <gaoj> <2013-05-21> added for CR00817770 end

        private Cursor queryAllConversations() {
            //gionee gaoj 2013-3-21 modified for CR00787048 start
            return mContext.getContentResolver().query(
                    Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION,
                    getConvSelection(), null, null);
            //gionee gaoj 2013-3-21 modified for CR00787048 end
        }

        private int queryUnreadCount() {
            Cursor cursor = null;
            int unreadCount = 0;
            try {
                cursor = mContext.getContentResolver().query(
                    Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION,
                    Threads.READ + "=0", null, null);
                if (cursor != null) {
                    unreadCount = cursor.getCount();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return unreadCount;
        }

        /**
         * Returns the number of items should be shown in the widget list.  This method also updates
         * the boolean that indicates whether the "show more" item should be shown.
         * @return the number of items to be displayed in the list.
         */
        @Override
        public int getCount() {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "getCount");
            }
            synchronized (sWidgetLock) {
                if (mConversationCursor == null) {
                    return 0;
                }
                final int count = getConversationCount();
                mShouldShowViewMore = mConversationCursor.getCount() > 0;
                //Gionee <zhouyj> <2013-06-08> add for CR00820099 begin
                onLoadComplete();
                //Gionee <zhouyj> <2013-06-08> add for CR00820099 end
                //return count + (mShouldShowViewMore ? 1 : 0);
                return count + 1;
            }
        }

        /**
         * Returns the number of conversations that should be shown in the widget.  This method
         * doesn't update the boolean that indicates that the "show more" item should be included
         * in the list.
         * @return
         */
        private int getConversationCount() {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "getConversationCount");
            }
            synchronized (sWidgetLock) {
                return Math.min(mConversationCursor.getCount(), MAX_CONVERSATIONS_COUNT);
            }
        }

        /*
         * Add color to a given text
         */
        private SpannableStringBuilder addColor(CharSequence text, int color) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            if (color != 0) {
                builder.setSpan(new ForegroundColorSpan(color), 0, text.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return builder;
        }

        private RemoteViews getEmptyConversationsView(int position) {
            RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_empty);
            view.setViewVisibility(R.id.no_conv_tip, View.VISIBLE);
            return view;
        }

        /**
         * @return the {@link RemoteViews} for a specific position in the list.
         */
        @Override
        public RemoteViews getViewAt(int position) {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "getViewAt position: " + position);
            }
            synchronized (sWidgetLock) {
                // "View more conversations" view.
                if (mConversationCursor == null || mConversationCursor.getCount() <= 0) {
                    return getEmptyConversationsView(position);
                }
                if (mConversationCursor == null
                        || (mShouldShowViewMore && position >= getConversationCount())) {
                    return getViewMoreConversationsView();
                }

                if (!mConversationCursor.moveToPosition(position)) {
                    // If we ever fail to move to a position, return the "View More conversations"
                    // view.
                    Log.w(TAG, "Failed to move to position: " + position);
                    return getViewMoreConversationsView();
                }
                // Aurora xuyong 2016-03-08 added for bug #18675 start
                Conversation conv = Conversation.from(mContext, mConversationCursor, false, true);
                // Aurora xuyong 2016-03-08 added for bug #18675 end
                // Inflate and fill out the remote view
                RemoteViews remoteViews = new RemoteViews(
                        mContext.getPackageName(), R.layout.widget_conversation);

                if (conv.getPrivacy() > 0) {
                    remoteViews.setViewVisibility(R.id.lock_privacy, View.VISIBLE);
                } else {
                    remoteViews.setViewVisibility(R.id.lock_privacy, View.GONE);
                }

                if (conv.hasUnreadMessages()) {
                    //remoteViews.setViewVisibility(R.id.widget_unread_background, View.VISIBLE);
                    //remoteViews.setViewVisibility(R.id.widget_read_background, View.GONE);
                } else {
                    //remoteViews.setViewVisibility(R.id.widget_unread_background, View.GONE);
                    //remoteViews.setViewVisibility(R.id.widget_read_background, View.VISIBLE);
                }
                //Gionee <zhouyj> <2013-05-07> remove for CR00808717 begin
                boolean hasAttachment = conv.hasAttachment();
                remoteViews.setViewVisibility(R.id.attachment, hasAttachment ? View.VISIBLE :
                    View.GONE);
                //Gionee <zhouyj> <2013-05-07> remove for CR00808717 end

                /// M: check mute status     conv.isMute()
                //remoteViews.setViewVisibility(R.id.mute, false ? View.VISIBLE : View.GONE);

                // Date M: use another method
                //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
                if (MmsApp.mGnPerfList) {
                    remoteViews.setTextViewText(R.id.date, MessageUtils.formatAuroraTimeStampString(mContext, conv.getDate(), true));
                } else {
                remoteViews.setTextViewText(R.id.date, MessageUtils.formatAuroraTimeStampString(mContext, conv.getDate(), true));
                }
                //Gionee <gaoj> <2013-05-13> added for CR00811367 end

                // From
                int color = conv.hasUnreadMessages() ? SENDERS_TEXT_COLOR_UNREAD :
                        SENDERS_TEXT_COLOR_READ;
                String unreadTip = null;
                if (conv.getUnreadMessageCount() > 0) {
                    unreadTip = " (" + conv.getUnreadMessageCount() + ") ";
                } else {
                    unreadTip = "";
                }
                SpannableStringBuilder from = addColor(conv.getRecipients().formatNames(", ") + unreadTip,
                        color);

                //** M: do not show draft flag
                /*if (conv.hasDraft()) {
                    from.append(mContext.getResources().getString(R.string.draft_separator));
                    int before = from.length();
                    from.append(mContext.getResources().getString(R.string.has_draft));
                    from.setSpan(new TextAppearanceSpan(mContext,
                            android.R.style.TextAppearance_Small, color), before,
                            from.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    from.setSpan(new ForegroundColorSpan(
                            mContext.getResources().getColor(R.drawable.text_color_red)),
                            before, from.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }*/

                // Unread messages are shown in bold
                if (conv.hasUnreadMessages()) {
                    from.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.aurora_conv_unread_color)), 0, from.length(),
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }

                /// M: if this is a guide thread
                if (conv.getType() == Threads.IP_MESSAGE_GUIDE_THREAD) {
                    String title = mContext.getString(R.string.str_ipmsg_service_title);
                    remoteViews.setTextViewText(R.id.from, title);
                } else {
                    remoteViews.setTextViewText(R.id.from, from);
                }

                /*if (conv.getUnreadMessageCount() > 0) {
                    remoteViews.setTextViewText(R.id.from, addColor(conv.getRecipients().formatNames(", ") + " (" + conv.getUnreadMessageCount() + ") ",
                            mContext.getResources().getColor(R.color.aurora_conv_unread_color)));
                } else {
                    remoteViews.setTextViewText(R.id.from, conv.getRecipients().formatNames(", "));
                }*/

                // Subject
                // TODO: the SmileyParser inserts image spans but they don't seem to make it
                // into the remote view.
                SmileyParser parser = SmileyParser.getInstance();
                if (conv.getEncryption()) {
                    //remoteViews.setViewVisibility(R.id.widget_encryptionimage,View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.subject,View.GONE);
                } else {
                    //remoteViews.setViewVisibility(R.id.widget_encryptionimage,View.GONE);
                    remoteViews.setViewVisibility(R.id.subject,View.VISIBLE);
                remoteViews.setTextViewText(R.id.subject,
                        addColor(parser.addSmileySpans(conv.getSnippet()),
                                conv.hasUnreadMessages() ? SUBJECT_TEXT_COLOR_UNREAD :
                                    SUBJECT_TEXT_COLOR_READ));
                }

                if (conv.hasError()) {
                    if (!conv.hasDraft()) {
                        remoteViews.setTextViewText(R.id.subject, rebuildErrorConvSnippet(conv.getSnippet()));
                    }
                }
                if(conv.hasDraft()) {
                    remoteViews.setTextViewText(R.id.subject, rebuildDraftConvSnippet(conv.getSnippet()));
                }

                // On click intent.
//                Intent clickIntent = new Intent(Intent.ACTION_VIEW);
//                clickIntent.setType("vnd.android-dir/mms-sms");
//                clickIntent.putExtra("thread_id", conv.getThreadId());
                boolean dirMode;
                Intent clickIntent = null;
                dirMode = MmsConfig.getMmsDirMode();
                if (true && dirMode) { //MmsConfig.getFolderModeEnabled()
                    clickIntent = new Intent(ACTION_FOLDER_MODE);
                    if (conv.hasUnreadMessages()) {
                        clickIntent.putExtra(EXTRA_KEY_FOLDER_TYPE, FOLDER_HAS_UNREAD);
                        clickIntent.putExtra(EXTRA_KEY_THREAD_ID, conv.getThreadId());
                    } else if (conv.hasDraft()) {
                        clickIntent.putExtra(EXTRA_KEY_FOLDER_TYPE, FOLDER_HAS_DRAFT);
                    } else if (conv.hasError()) {
                        clickIntent.putExtra(EXTRA_KEY_FOLDER_TYPE, FOLDER_HAS_ERROR);
                    } else if (Threads.WAPPUSH_THREAD == conv.getType() ||
                            Threads.CELL_BROADCAST_THREAD == conv.getType()) {
                        clickIntent.putExtra(EXTRA_KEY_FOLDER_TYPE, FOLDER_CB_OR_PUSH);
                    } else {
                        clickIntent.putExtra(EXTRA_KEY_FOLDER_TYPE, FOLDER_NORMAL);
                        clickIntent.putExtra(EXTRA_KEY_THREAD_ID, conv.getThreadId());
                    }
                } else {
                    clickIntent = new Intent(ACTION_CONVERSATION_MODE);
                    clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    clickIntent.putExtra(EXTRA_KEY_CONVSATION_TYPE, conv.getType());
                    clickIntent.putExtra(EXTRA_KEY_THREAD_ID, conv.getThreadId());
                    clickIntent.putExtra("quick_query", true);
                }
                remoteViews.setOnClickFillInIntent(R.id.widget_conversation, clickIntent);

                return remoteViews;
            }
        }

        /**
         * @return the "View more conversations" view. When the user taps this item, they're
         * taken to the messaging app's conversation list.
         */
/*        private RemoteViews getViewMoreConversationsView() {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "getViewMoreConversationsView");
            }
            RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_loading);
            view.setTextViewText(
                    R.id.loading_text, mContext.getText(R.string.view_more_conversations));
            Intent clickIntent = null;
            if (false && MmsConfig.getMmsDirMode()) {//MmsConfig.getFolderModeEnabled()
                clickIntent = new Intent(ACTION_FOLDER_MODE);
                clickIntent.putExtra(EXTRA_KEY_FOLDER_TYPE, MORE_MESSAGES);
            } else {
                clickIntent = new Intent(ACTION_CONVERSATION_MODE);
                clickIntent.putExtra(EXTRA_KEY_CONVSATION_TYPE, MORE_MESSAGES);
            }
            view.setOnClickFillInIntent(R.id.widget_loading, clickIntent);
//            view.setOnClickFillInIntent(R.id.widget_loading,
//                    new Intent(mContext, ConversationList.class));
            return view;
        }*/

        private RemoteViews getViewMoreConversationsView() {
            RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_loading);
            if (mConversationCursor.getCount() >= MAX_CONVERSATIONS_COUNT) {
                view.setTextViewText(
                        R.id.loading_text, mContext.getText(R.string.view_more_conversations));
               /* PendingIntent pendingIntent =
                        PendingIntent.getActivity(mContext, 0, new Intent(mContext,
                                        AuroraConvListActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT);*/
                Intent clickIntent = new Intent(ACTION_CONVERSATION_MODE);
                clickIntent.putExtra(EXTRA_KEY_CONVSATION_TYPE, MORE_MESSAGES);
                view.setOnClickFillInIntent(R.id.widget_loading, clickIntent);
            } else {
                view.setTextViewText(
                        R.id.loading_text, mContext.getText(R.string.aurora_widget_nomore_tip));
            }
            return view;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_loading);
            view.setTextViewText(
                    R.id.loading_text, mContext.getText(R.string.loading_conversations));
            return null;
        }

        @Override
        public int getViewTypeCount() {
            // Aurora xuyong 2016-03-16 modified for bug #21358 start
            // We should change this count depend on our logic.
            return 3;
            // Aurora xuyong 2016-03-16 modified for bug #21358 end
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void onLoadComplete() {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "onLoadComplete");
            }
            //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
            RemoteViews remoteViews = null;
            Conversation conv = null;
            /*if (MmsApp.mGnLockScrnSupport) {
                if (mNewConvCursor != null && mNewConvCursor.moveToFirst()) {
                    conv = Conversation.from(mContext, mNewConvCursor);
                }
                if (mNewConvCursor != null) {
                    mNewConvCursor.close();
                    mNewConvCursor = null;
                }
                if (conv == null) {
                    return;
                }
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.gn_infozone_widget);
                remoteViews.setViewVisibility(R.id.gn_widget_from, View.VISIBLE);
                remoteViews.setTextViewText(R.id.gn_widget_from, conv.getRecipients().formatNames(", "));
                remoteViews.setTextViewText(R.id.gn_widget_subject, conv.getSnippet());
                if (conv.getEncryption()) {
                    remoteViews.setViewVisibility(R.id.gn_widget_subject,View.GONE);
                } else {
                    remoteViews.setViewVisibility(R.id.gn_widget_subject,View.VISIBLE);
                }
//                final Intent composeIntent = new Intent(context, MmsWidgetProxyActivity.class);
//                composeIntent.setAction(MmsWidgetService.ACTION_CONVERSATION_MODE);
//                composeIntent.putExtra("thread_id", conv.getThreadId());
//                PendingIntent clickIntent = PendingIntent.getActivity(
//                        context, 0, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                remoteViews.setOnClickPendingIntent(R.id.gn_widget, clickIntent);
            } else {*/
                //gionee <gaoj> <2013-06-29> modify for CR00826240 end
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget);

            //gionee <gaoj> <2013-07-01> add for CR00826240 begin
            remoteViews.setViewVisibility(R.id.conversation_list, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.aurora_edit_widget_bt, View.VISIBLE);
            /*final Intent composeIntent = new Intent(mContext, ComposeMessageActivity.class);
            PendingIntent clickIntent = PendingIntent.getActivity(
                    mContext, 0, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.aurora_edit_widget_bt, clickIntent);
            remoteViews.setViewVisibility(R.id.aurora_edit_widget_bt, View.VISIBLE);*/

            //remoteViews.setViewVisibility(R.id.widget_header, View.VISIBLE);
            //gionee <gaoj> <2013-07-01> add for CR00826240 end
            
            /*remoteViews.setViewVisibility(R.id.widget_unread_count, mUnreadConvCount > 0 ?
                    View.VISIBLE : View.GONE);
            if (mUnreadConvCount > 0) {
                remoteViews.setTextViewText(
                        R.id.widget_unread_count, Integer.toString(mUnreadConvCount));
            }*/
            //gionee <gaoj> <2013-06-29> modify for CR00826240 begin
            //}
            //gionee <gaoj> <2013-06-29> modify for CR00826240 end
            if (mAppWidgetManager != null) {
                mAppWidgetManager.partiallyUpdateAppWidget(mAppWidgetId, remoteViews);
            }
        }

        private Runnable mUpdateFromContactRunnable = new Runnable() {
            public void run() {
                if (mAppWidgetManager != null) {
                    Log.v(TAG, "mUpdateFromContactRunnable is called ");
                    mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.conversation_list);
                }
            }
        };


        public void onUpdate(Contact updated) {
            if (Log.isLoggable(LogTag.WIDGET, Log.VERBOSE)) {
                Log.v(TAG, "onUpdate from Contact: " + updated);
            }
            mHandler.removeCallbacks(mUpdateFromContactRunnable);
            mHandler.postDelayed(mUpdateFromContactRunnable, 1000);
        }

        private String getConvSelection() {
            String selection = null;
            if (MmsApp.mIsDraftOpen) {
                selection = "(message_count != 0)";
            }
            String snippetSelection = "NOT ((snippet is null) AND (snippet_cs not null AND snippet_cs = 0) AND (has_attachment = 0) AND (message_count = 0))";
            if (MmsApp.sHasRejectFeature) {
                snippetSelection = "(" +snippetSelection + ") AND (NOT date IS NULL)";
            }
            if (MmsApp.sHasPrivacyFeature) {
                long curAccountId = AuroraPrivacyUtils.getCurrentAccountId();
                if (curAccountId > 0) {
                    snippetSelection = "(" +snippetSelection + ") AND (is_privacy IN (0, " + curAccountId +"))";
                }
            }
            String newSelection;
            if (selection == null) {
                newSelection = snippetSelection;
            } else {
                newSelection = selection + " AND (" + snippetSelection + ")";
            }
            return newSelection;
        }

        private SpannableStringBuilder rebuildErrorConvSnippet(String snipped) {
            int color = mContext.getResources().getColor(R.color.aurora_conv_error_color);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
            String errorIndicate = mContext.getResources().getString(R.string.aurora_conv_error_indi);
            SpannableStringBuilder buffer = new SpannableStringBuilder();
            buffer.append(errorIndicate + snipped);
            buffer.setSpan(colorSpan, 0, errorIndicate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return buffer;
        }

        private SpannableStringBuilder rebuildDraftConvSnippet(String snipped) {
            int color = mContext.getResources().getColor(R.color.aurora_conv_draft_color);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
            String draftIndicate = mContext.getResources().getString(R.string.aurora_conv_draft_indi);
            SpannableStringBuilder buffer = new SpannableStringBuilder();
            buffer.append(draftIndicate + snipped);
            buffer.setSpan(colorSpan, 0, draftIndicate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return buffer;
        }

    }

    /// M: fix bug ALPS00448814, update conversation in the MmsWidget @{
    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().registerContentObserver(DRAFT_URI, true, mConversationObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mConversationObserver);
    }

    private static final Uri DRAFT_URI = Uri.parse("content://mms-sms/");

    private final ContentObserver mConversationObserver =
      new ContentObserver(new Handler()) {
          public void onChange(boolean selfUpdate) {
              MmsWidgetProvider.notifyDatasetChanged(getApplicationContext());
          }
    };
    /// @}
}
