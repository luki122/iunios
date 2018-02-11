/*
 * Copyright (C) 2013 Google Inc.
 * Licensed to The Android Open Source Project.
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
package com.android.mail.ui;

import com.google.common.collect.Lists;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import com.android.mail.providers.Account;
import com.android.mail.R;

import java.util.ArrayList;

import com.android.mail.ui.ConversationDividerView;
import com.android.mail.ui.ConversationSearchView.ListController;//paul add

public class ConversationListHelper {
	private static ConversationDividerView conversationDividerView;
	
	private static int setDividerPostion=0;
    public static int getSetDividerPostion() {
		return setDividerPostion;
	}

	public static void setSetDividerPostion(int setDividerPostion) {
		ConversationListHelper.setDividerPostion = setDividerPostion;
		if(conversationDividerView!=null){
			 conversationDividerView.setPosition(getSetDividerPostion());
//			 Log.d("JXH", "conversationDividerView update position:"+getSetDividerPostion());
		}
	}

	
	


	/**
     * Creates a list of newly created special views.
     */
	//pual modify start
    public ArrayList<ConversationSpecialItemView> makeConversationListSpecialViews(
            final Context context, final ControllableActivity activity, final Account account,ListController listController) {
		
		/*
		final ConversationSyncDisabledTipView conversationSyncDisabledTipView =
                (ConversationSyncDisabledTipView) LayoutInflater.from(context)
                        .inflate(R.layout.conversation_sync_disabled_tip_view, null);
        conversationSyncDisabledTipView.bindAccount(account, activity);

        final ConversationsInOutboxTipView conversationsInOutboxTipView =
                (ConversationsInOutboxTipView) LayoutInflater.from(context)
                        .inflate(R.layout.conversation_outbox_tip_view, null);
        conversationsInOutboxTipView.bind(account, activity.getFolderSelector());

        // Conversation photo teaser view
        final ConversationPhotoTeaserView conversationPhotoTeaser =
                (ConversationPhotoTeaserView) LayoutInflater.from(context)
                        .inflate(R.layout.conversation_photo_teaser_view, null);

        // Long press to select tip
        final ConversationLongPressTipView conversationLongPressTipView =
                (ConversationLongPressTipView) LayoutInflater.from(context)
                        .inflate(R.layout.conversation_long_press_to_select_tip_view, null);

        final NestedFolderTeaserView nestedFolderTeaserView =
                (NestedFolderTeaserView) LayoutInflater.from(context)
                        .inflate(R.layout.nested_folder_teaser_view, null);
        nestedFolderTeaserView.bind(account, activity.getFolderSelector());
	*/
    	 final ArrayList<ConversationSpecialItemView> itemViews = Lists.newArrayList();
    	 
    	LayoutInflater inflater = LayoutInflater.from(context);
    	
//    	final PullStatusView pullStatusView = (PullStatusView)inflater.inflate(R.layout.aurora_pull_status_view, null);
//    	pullStatusView.bindAccount(account, activity);

		final ConversationSearchView conversationSearchView = (ConversationSearchView) inflater.inflate(R.layout.aurora_search, null);
		conversationSearchView.bindAccount(account, activity);
		conversationSearchView.setListController(listController);
		
//		// Conversation photo teaser view
//        final ConversationPhotoTeaserView conversationPhotoTeaser =
//                (ConversationPhotoTeaserView) LayoutInflater.from(context)
//                        .inflate(R.layout.conversation_photo_teaser_view, null);
         conversationDividerView = (ConversationDividerView)inflater.inflate(R.layout.conversation_item_divider, null);

        // Order matters.  If a and b are added in order itemViews.add(a), itemViews.add(b),
        // they will appear in conversation list as:
        // b
        // a
        itemViews.add(conversationDividerView);
        conversationDividerView.setPosition(getSetDividerPostion());
//        Log.d("JXH", "conversationDividerView add position:"+getSetDividerPostion());
//        itemViews.add(conversationPhotoTeaser);
		itemViews.add(conversationSearchView);
		
//		itemViews.add(pullStatusView);
		/*
        itemViews.add(conversationPhotoTeaser);
        itemViews.add(conversationLongPressTipView);
        itemViews.add(conversationSyncDisabledTipView);
        itemViews.add(conversationsInOutboxTipView);
        itemViews.add(nestedFolderTeaserView);
        */
		
        return itemViews;
    }
	//pual modify end
}
