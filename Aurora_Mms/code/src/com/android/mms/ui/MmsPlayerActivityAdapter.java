/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MmsPlayerActivity.OnDataSetChangedListener;
import com.android.mms.util.SmileyParser;

import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.text.ClipboardManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ImageView; // import com.gionee.widget.GnImageView;
import com.aurora.featureoption.FeatureOption;
import android.os.SystemProperties;

//gionee wangym 2012-11-22 add for CR00735223 start
import com.android.mms.MmsApp;
import android.graphics.Color;
import android.webkit.MimeTypeMap;
import android.view.View.OnClickListener;
//gionee wangym 2012-11-22 add for CR00735223 end
/**
 * The back-end data adapter for MmsPlayerActivity.
 */
public class MmsPlayerActivityAdapter extends BaseAdapter {
    private static final String TAG = "MmsPlayerActivityAdapter";
    private static final boolean LOCAL_LOGV = false;

    private final LayoutInflater mFactory;
    private ArrayList<MmsPlayerActivityItemData> mListItem;
    private int mAllCount;
    private Context mContext;
    private float textSize = 18;
    private HashMap<Integer, View> mListItemViewCache = new HashMap<Integer, View>();
    private SmileyParser parser = SmileyParser.getInstance();
    
    public void setTextSize(float size) {
        textSize = size;
    }

    public MmsPlayerActivityAdapter(Context context, ArrayList<MmsPlayerActivityItemData> listItem) {
        mFactory = LayoutInflater.from(context);
        mListItem = listItem;
        mAllCount = mListItem.size();
        mContext = context;
    }

    public void onMovedToScrapHeap(View view) {
    }

    public ArrayList<MmsPlayerActivityItemData> getListItem() {
        return mListItem;
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mAllCount;
    }

    @Override
    public MmsPlayerActivityItemData getItem(int arg0) {
        return mListItem.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        Log.i(TAG, "getView, for position " + arg0 + ", view " + arg1);
        View itemView = mListItemViewCache.get(arg0); 
        if (itemView != null) {
            String text = mListItem.get(arg0).getText();
            if (text != null) {
                TextView mText = (TextView) itemView.findViewById(R.id.text);
                if (mText.getTextSize() != textSize && mText.getVisibility() != View.GONE) {
                    mText.setTextSize(textSize);
                }
            }
            Log.i(TAG, "getView(): from cache.");
            return itemView;
        }
        Log.i(TAG, "getView(): from new one.");

        itemView = mFactory.inflate(R.layout.mms_player_activity_item, null);

        TextView mPageIndex = (TextView) itemView.findViewById(R.id.page_index);
        ImageView mImage = (ImageView) itemView.findViewById(R.id.image);
        ImageView mVideo = (ImageView) itemView.findViewById(R.id.video);
        View mAudio = (View) itemView.findViewById(R.id.audio);
        TextView mAudioName = (TextView) itemView.findViewById(R.id.audio_name);
        ImageView mAudioIcon = (ImageView) itemView.findViewById(R.id.audio_icon);
        TextView mText = (TextView) itemView.findViewById(R.id.text);

        final MmsPlayerActivityItemData item = mListItem.get(arg0);

        // show page index
        String index = mContext.getResources().getString(R.string.page, arg0 + 1);
        mPageIndex.setText(index);

        //gionee wangym 2012-11-22 add for CR00735223 start
        if (MmsApp.mLightTheme) {
            mPageIndex.setTextColor(Color.BLACK);
        } else if (MmsApp.mDarkStyle) {
            mPageIndex.setTextColor(Color.GRAY);
        }
        //gionee wangym 2012-11-22 add for CR00735223 end
        // show image
        Uri imageUri = item.getImageUri();
        if (imageUri != null) {
            Log.i(TAG, "set image: "+ imageUri);
            mImage.setPadding(0, 1, 0, 0);
            mImage.setImageURI(imageUri);
            mImage.setVisibility(View.VISIBLE);
        } else {
            mImage.setVisibility(View.GONE);
        }
        
        // show video thumbnail
        Bitmap t = item.getVideoThumbnail();
        if (t != null) {
            mVideo.setImageBitmap(t);
            mVideo.setVisibility(View.VISIBLE);
        } else {
            mVideo.setVisibility(View.GONE);
        }

        String audioName = item.getAudioName();
        if (audioName != null) {
            Log.i(TAG, "show audio name:" + audioName);
            mAudioName.setText(audioName);
            mAudioName.setTextSize(18);
            mAudioIcon.setVisibility(View.VISIBLE);
            mAudio.setVisibility(View.VISIBLE);
            mAudioName.setVisibility(View.VISIBLE);
            
        } else {
            mAudioIcon.setVisibility(View.GONE);
            mAudioName.setVisibility(View.GONE);
            mAudio.setVisibility(View.GONE);
        }
        
        String text = item.getText();
        if (text != null) {
            mText.setText(parser.addSmileySpans(text));
            mText.setTextSize(textSize);
            mText.setVisibility(View.VISIBLE);
            //MTK_OP01_PROTECT_START
            /*if (MmsApp.isTelecomOperator()) {
                mText.setEnableShowUrlDialog(true);
            }*/
            //MTK_OP01_PROTECT_END 

            //gionee wangym 2012-11-22 add for CR00735223 start
            if (MmsApp.mLightTheme) {
                mText.setTextColor(Color.BLACK);
            } else if (MmsApp.mDarkStyle) {
                mText.setTextColor(Color.GRAY);
            }
            //gionee wangym 2012-11-22 add for CR00735223 end
        } else {
            mText.setVisibility(View.GONE);
        }
        mListItemViewCache.put(arg0, itemView);
        return itemView;
    }

    public void clearAllCache() {
        if (mListItemViewCache.size() > 0) {
            View itemView = null;
            for (Integer key : mListItemViewCache.keySet()) {
                itemView = mListItemViewCache.get(key);
                ImageView mImage = (ImageView) itemView.findViewById(R.id.image);
                mImage.setImageURI(null);
            }
            mListItemViewCache.clear();
        }
    }
}
