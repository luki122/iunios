/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 * lwzh                                                             modify for CR00774362 20130227 
 *
 */
package com.gionee.mms.ui;

import java.util.ArrayList;
import java.util.List;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.gionee.mms.ui.TabActivity.ViewPagerVisibilityListener;

import android.R.raw;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnTelephony.MmsSms;
import android.provider.Telephony.Sms;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.BaseExpandableListAdapter;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.widget.AuroraExpandableListView.ExpandableListContextMenuInfo;

public class FavoritesFragment extends Fragment implements ViewPagerVisibilityListener,OnCreateContextMenuListener, 
AuroraExpandableListView.OnChildClickListener{


    private final static String TAG = "FavoritesFragment";

    //Gionee <zhouyj> <2013-05-03> modify for CR00807509 begin
    //FavoritesFragment   ContextMenu id from 30 to 49
    private final static int MENU_FORWARD             = 30;
    private final static int MENU_EDIT_QUICK_TEXT     = 31;
    private final static int MENU_DELETE_QUICK_TEXT   = 32;
    private final static int MENU_CANCEL_FAVORITE     = 33;
    private final static int MENU_CANCEL_ALL_FAVORITE = 34;
    //Gionee <zhouyj> <2013-05-03> modify for CR00807509 end
    private Context mContext;

    private Resources mResource;

    private AuroraAlertDialog mEditDialog;
    private AuroraAlertDialog mNewDialog;
    private final static int NEW_QUICK_TEXT_DIALOG = 1;

    private final static int EDIT_QUICK_TEXT_DIALOG = 2;

    private final static int DELETE_QUICK_TEXT_DIALOG = 3;
    private BaseExpandableListAdapter mAdapter;

    private AuroraExpandableListView mListView;
    private LayoutInflater mInflater;
    //gionee gaoj 2013-4-1 added for CR00791839 start
    private boolean mVisibile = false;
    //gionee gaoj 2013-4-1 added for CR00791839 end
    
    //Gionee <zhouyj> <2013-07-15> add for CR00830175 begin
    private static final int UPDATE_UI_MESSAGE = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_UI_MESSAGE) {
                mAdapter.notifyDataSetChanged();
                //Gionee <zhouyj> <2013-07-17> add for CR00837363 begin
                if (TabActivity.sTabActivity != null) {
                    TabActivity.sTabActivity.invalidateOptionsMenu();
                }
                //Gionee <zhouyj> <2013-07-17> add for CR00837363 end
            }
        };
    };
    //Gionee <zhouyj> <2013-07-15> add for CR00830175 end

    private List<QuickTextData> mQuickTextList = new ArrayList<QuickTextData>();
    private List<ExpandableListChildData> mFavList = new ArrayList<ExpandableListChildData>();

    class ExpandableListChildData {
        public int msgId;

        public int type;
        
        public String bodyValue;

        public String dateValue;

        public String recipientValue;
    }

    private void queryQuickText() {
        Cursor cursor = getActivity().getContentResolver().query(MmsSms.CONTENT_URI_QUICKTEXT,
                QUICK_TEXT_PROJECTION, null, null, "_id desc");

        mQuickTextList.clear();

        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                QuickTextData qtData = new QuickTextData();
                qtData.qtId = cursor.getInt(0);
                qtData.qtValue = cursor.getString(1);
                mQuickTextList.add(qtData);
            }
            cursor.close();
        }
    }

    private final static String[] QUICK_TEXT_PROJECTION = new String[] {
            "_id", "text"
    };

    private final static String[] FAVORITE_PROJECTION = new String[] {
            "_id", Sms.TYPE, Sms.BODY, Sms.DATE, Sms.ADDRESS
    };

    class QuickTextData {
        public int qtId;

        public String qtValue;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mResource = mContext.getResources();
//        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View fragmentView = inflater.inflate(R.layout.gn_favorite_recommend, container, false);

        mListView = (AuroraExpandableListView) fragmentView.findViewById(R.id.favorite_expandable_list);

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);

//        mListView.setGroupIndicator(mResource.getDrawable(R.drawable.gn_expander_ic_folder));
    }

    private void initData() {
        //Gionee <zhouyj> <2013-07-15> modify for CR00830175 start
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                queryQuickText();
                queryFavoriteListChildData();
                Message msg = mHandler.obtainMessage(UPDATE_UI_MESSAGE);
                mHandler.sendMessage(msg);
            }
        }).start();
        //Gionee <zhouyj> <2013-07-15> modify for CR00830175 end
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    @Override
    public void onVisibilityChanged(boolean visible) {
        //gionee gaoj 2013-4-1 added for CR00791839 start
        mVisibile = visible;
        //gionee gaoj 2013-4-1 added for CR00791839 end
        Log.d(TAG, "onVisibilityChanged  mVisibile = "+mVisibile);
        if (visible == false) {
            return;
        }
        
        if (mAdapter == null) {
            mAdapter = new MyExpandableListAdapter(getActivity());
    
            mListView.setOnChildClickListener(this);
            mListView.setOnCreateContextMenuListener(this);
            //gionee gaoj 2012-7-3 added for CR00633458 start
            // initData();
            mListView.setAdapter(mAdapter);
            //gionee gaoj 2012-7-3 added for CR00633458 end
        }
        
        initData();      
        boolean isExpand = mListView.isGroupExpanded(1);
        if(!isExpand){
            mListView.expandGroup(1);
        }
        //Gionee <zhouyj> <2013-07-24> remove for CR00839590 begin
        //mAdapter.notifyDataSetChanged();
        //Gionee <zhouyj> <2013-07-24> remove for CR00839590 end
    }

    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        private Context mContext;

        private String[] mGroups = {
                getString(R.string.tab_favorites_common_words),
                getString(R.string.tab_favorites_msg_fav)
        };

        public MyExpandableListAdapter(Context context) {
            this.mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case 0:
                    return mQuickTextList.get(childPosition - 1);
                case 1:
                    return mFavList.get(childPosition);
            }
            return null;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            int childrenCount = 0;
            switch (groupPosition) {
                case 0:
                    childrenCount = mQuickTextList.size() + 1;
                    break;
                case 1:
                    childrenCount = mFavList.size();
                    break;
            }
            return childrenCount;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            int childCount = 0;
            if (groupPosition == 0 && childPosition == 0) {
                return mInflater.inflate(R.layout.gn_quick_text_header, parent, false);
            }

            if (convertView != null) {
                convertView = null;
            }

            View retView = convertView;

            if (retView == null) {
                if (groupPosition == 0) {
                    retView = mInflater.inflate(R.layout.gn_quicktext_list_item, parent, false);
                    TextView body = (TextView) retView.findViewById(R.id.quick_text_body);
                    QuickTextData qtData = (QuickTextData) getChild(groupPosition, childPosition);
                    body.setText(qtData.qtValue);
                } else {
                    retView = mInflater.inflate(R.layout.gn_favorites_list_item, parent, false);
                    TextView body = (TextView) retView.findViewById(R.id.favorite_body);
                    TextView date = (TextView) retView.findViewById(R.id.favorite_date);
                    TextView recipient = (TextView) retView.findViewById(R.id.favorite_receipt);

                    ExpandableListChildData favChild = (ExpandableListChildData) getChild(
                            groupPosition, childPosition);
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
                    /*String dateValue = MessageUtils.newformatGNTime(
                            mContext, Long.parseLong(favChild.dateValue));*/
                    String dateValue = null;
                    if (MmsApp.mGnPerfList) {
                        dateValue = MessageUtils.formatTimeStampString(
                                mContext, Long.parseLong(favChild.dateValue));
                    } else {
                        dateValue = MessageUtils.newformatGNTime(
                                mContext, Long.parseLong(favChild.dateValue));
                    }
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 end

                    date.setVisibility(View.VISIBLE);
                    recipient.setVisibility(View.VISIBLE);
                    date.setText(dateValue);
                    String name = getNameOrNumber(favChild.recipientValue,favChild.type);
                    recipient.setText(name);
                    body.setText(favChild.bodyValue);
                }
            }
            return retView;
        }

        public Object getGroup(int groupPosition) {
            return mGroups[groupPosition];
        }

        public int getGroupCount() {
            return mGroups.length;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {

            View retView = convertView;
            if (convertView == null) {
                retView = LinearLayout
                        .inflate(mContext, R.layout.gn_favorite_list_group_item, null);
            }

            TextView title = (TextView) retView.findViewById(R.id.groupName);
            TextView count = (TextView) retView.findViewById(R.id.groupCount);

            title.setText(getGroup(groupPosition).toString());
            if (groupPosition == 0) {
                count.setText("[" + (getChildrenCount(groupPosition) -1 ) + "]");
            }else {
                count.setText("[" + getChildrenCount(groupPosition) + "]");
            }
            return retView;
        }

        @Override
        public long getGroupId(int arg0) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }
    }

    private void queryFavoriteListChildData() {
        //gionee gaoj 2012-10-12 added for CR00711168 start
        if (MmsApp.mIsSafeModeSupport) {
            mFavList.clear();
            return;
        }
        //gionee gaoj 2012-10-12 added for CR00711168 end
        Cursor cursor = getActivity().getContentResolver().query(Sms.CONTENT_URI, FAVORITE_PROJECTION, "star=1",
                null, null);

        mFavList.clear();

        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                ExpandableListChildData favData = new ExpandableListChildData();
                favData.msgId = cursor.getInt(cursor.getColumnIndex("_id"));
                favData.type = cursor.getInt(cursor.getColumnIndex(Sms.TYPE));
                favData.bodyValue = cursor.getString(cursor.getColumnIndex(Sms.BODY));
                favData.dateValue = cursor.getString(cursor.getColumnIndex(Sms.DATE));
                favData.recipientValue = cursor.getString(cursor.getColumnIndex(Sms.ADDRESS));
                mFavList.add(favData);
            }
            cursor.close();
        }
    }

    private String getNameOrNumber(String number, int type) {
        String name = "";
        if (type == 1) {
            Contact contact = Contact.get(number, false);
            boolean existsInDb = contact.existsInDatabase();
            if (existsInDb) {
                name = contact.getNameAndNumber();
            } else {
                name = number;
            }
        } else {
            name = mResource.getString(R.string.favorite_display_name);
        }
        return name;
    }

    @Override
    public boolean onChildClick(AuroraExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        if (groupPosition == 0 && childPosition == 0) {
            createGnDialog(NEW_QUICK_TEXT_DIALOG, 0, "").show();
            if (mNewDialog != null) {
                mNewDialog.getButton(mNewDialog.BUTTON_POSITIVE).setEnabled(false);
              }
        } else {
            v.showContextMenu();
        }
        mAdapter.notifyDataSetChanged();
        return false;
    }

    private Dialog createGnDialog(int id, final int msgId, String msgContent) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.gn_add_quick_text_dialog, null);
//        final TextView quickTextTitle = (TextView) textEntryView
//                .findViewById(R.id.add_quick_text_title);
        final AuroraEditText quickText = (AuroraEditText) textEntryView
                .findViewById(R.id.add_quick_text_content);
        quickText.setText(msgContent);
        quickText.addTextChangedListener(mTextWatcher);

        if (MmsApp.mTransparent) {
            quickText.setTextColor(Color.BLACK);
            //gionee gaoj 2012-5-25 added for CR00607726 start
            //gionee gaoj 2012-5-25 added for CR00607726 end
        }

        switch (id) {
            case EDIT_QUICK_TEXT_DIALOG:
//                quickTextTitle.setText(mResource.getString(R.string.quick_text_header_text_edit));
                mEditDialog = new AuroraAlertDialog.Builder(mContext/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
                        .setTitle(mResource.getString(R.string.quick_text_header_text_edit))
                        .setView(textEntryView)
                        .setPositiveButton(R.string.add_quick_text_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* User clicked OK so do some stuff */
                                        String content = quickText.getText().toString();
                                        // insert into db
                                        if (content != null && !"".equals(content)) {
                                            updateQuickText(content, (long) msgId);
                                        } else {
                                            dialog.dismiss();
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.add_quick_text_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* User clicked cancel so do some stuff */
                                        dialog.dismiss();
                                    }
                                }).create();
                return mEditDialog;
            case NEW_QUICK_TEXT_DIALOG:
                mNewDialog = new AuroraAlertDialog.Builder(mContext/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
                        .setTitle(mResource.getString(R.string.quick_text_header_text))
                        .setView(textEntryView)
                        .setPositiveButton(R.string.add_quick_text_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* User clicked OK so do some stuff */
                                        String content = quickText.getText().toString();
                                        // insert into db
                                        if (content != null && !"".equals(content)) {
                                            insertQuickText(content);
                                        } else {
                                            dialog.dismiss();
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.add_quick_text_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* User clicked cancel so do some stuff */
                                        dialog.dismiss();
                                    }
                                }).create();
                return mNewDialog;
            case DELETE_QUICK_TEXT_DIALOG:
                return new AuroraAlertDialog.Builder(mContext/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
                        .setTitle(mResource.getString(R.string.delete_quick_text_title))
//                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(mResource.getString(R.string.delete_quick_text_confirm))
                        .setPositiveButton(R.string.OK,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        boolean del = mContext.getContentResolver().delete(
                                                MmsSms.CONTENT_URI_QUICKTEXT, "_id=" + msgId, null) > 0;
                                        if (del) {
                                            refreshQtData();
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.add_quick_text_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* User clicked cancel so do some stuff */
                                        dialog.dismiss();
                                    }
                                }).create();
        }
        return null;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
        }

        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (mEditDialog != null) {
                if (s.toString().equals("")) {
                    mEditDialog.getButton(mEditDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    mEditDialog.getButton(mEditDialog.BUTTON_POSITIVE).setEnabled(true);
                  }
              }
            if (mNewDialog != null) {
                if (s.toString().equals("")) {
                    mNewDialog.getButton(mNewDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    mNewDialog.getButton(mNewDialog.BUTTON_POSITIVE).setEnabled(true);
                  }
              }
        }
    };

    private void updateQuickText(String content, final long id) {
        final ContentValues cv = new ContentValues();
        cv.put("text", content);
        mContext.getContentResolver().update(MmsSms.CONTENT_URI_QUICKTEXT, cv, "_id=" + id, null);
        refreshQtData();
    }

    private void refreshQtData() {
        expandGnGroup(0);
    }

    private void expandGnGroup(int groupId) {
        boolean isExpand = mListView.isGroupExpanded(groupId);
        if (isExpand) {
            if (groupId == 0) {
                queryQuickText();
            } else {
                queryFavoriteListChildData();
            }
            mListView.expandGroup(groupId);
        } else {
            // gionee zhouyj 2012-09-17 add for CR00693271 start 
            if(groupId == 1) 
                queryFavoriteListChildData();
            // gionee zhouyj 2012-09-17 add for CR00693271 end 
            mListView.collapseGroup(groupId);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void insertQuickText(String content) {
        final ContentValues cv = new ContentValues();
        cv.put("text", content);
        mContext.getContentResolver().insert(MmsSms.CONTENT_URI_QUICKTEXT, cv);
        refreshQtData();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        mFavList.clear();
        super.onDestroy();
    }

    private String getMsgBody(int groupPosition, int childPosition) {
        String msgBody = null;
        if (groupPosition == 0 && childPosition != 0) {
            QuickTextData qtData = (QuickTextData) mAdapter.getChild(groupPosition, childPosition);
            msgBody = qtData.qtValue;
        } else if (groupPosition != 0) {
            ExpandableListChildData favChild = (ExpandableListChildData) mAdapter.getChild(
                    groupPosition, childPosition);
            msgBody = favChild.bodyValue;
        }
        return msgBody;
    }

    private int getMsgId(int groupPosition, int childPosition) {
        int msgId = 0;
        if (groupPosition == 0 && childPosition != 0) {
            QuickTextData qtData = (QuickTextData) mAdapter.getChild(groupPosition, childPosition);
            msgId = qtData.qtId;
        } else if (groupPosition == 1) {
            ExpandableListChildData favChild = (ExpandableListChildData) mAdapter.getChild(
                    groupPosition, childPosition);
            msgId = favChild.msgId;
        }
        return msgId;
    }

    private void unFavorivteMessage(int msgId) {
        final Uri lockUri = ContentUris.withAppendedId(Sms.CONTENT_URI, (long) msgId);
        final ContentValues values = new ContentValues(1);
        values.put("star", 0);
        mContext.getContentResolver().update(lockUri, values, null, null);
        refreshFavData();
    }
    
    public void refreshFavData(){
        expandGnGroup(1);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        int groupPos = 0;
        int childPos = 0;
        int type = AuroraExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == AuroraExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = AuroraExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = AuroraExpandableListView.getPackedPositionChild(info.packedPosition);
        }
        if (groupPos == 0 && childPos == 0) {
        } else {

            menu.setHeaderTitle(getMsgBody(groupPos, childPos));

            menu.add(0, MENU_FORWARD, 0, mResource.getString(R.string.quick_text_menu_forward));
            if (groupPos == 0) {
                menu.add(0, MENU_EDIT_QUICK_TEXT, 0,
                        mResource.getString(R.string.quick_text_menu_edit));
                menu.add(0, MENU_DELETE_QUICK_TEXT, 0,
                        mResource.getString(R.string.quick_text_menu_delete));
            } else {
                menu.add(0, MENU_CANCEL_FAVORITE, 0, mResource.getString(R.string.cancel_favorite));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int groupPos = 0;
        int childPos = 0;
        // gionee zhouyj 2013-02-25 modify for CR00773190 start
        try {
            ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
            int type = AuroraExpandableListView.getPackedPositionType(info.packedPosition);
            if (type == AuroraExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                groupPos = AuroraExpandableListView.getPackedPositionGroup(info.packedPosition);
                childPos = AuroraExpandableListView.getPackedPositionChild(info.packedPosition);
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.i(TAG, "onContextItemSelected   java.lang.ClassCastException: android.widget.AdapterView$AdapterContextMenuInfo cannot be cast to aurora.widget.AuroraExpandableListView$ExpandableListContextMenuInfo");
            return true;
        }
        // gionee zhouyj 2013-02-25 modify for CR00773190 end
        String msgBody = getMsgBody(groupPos, childPos);
        int msgId = getMsgId(groupPos, childPos);
        switch (item.getItemId()) {
            case MENU_FORWARD:
                Intent intent = new Intent(mContext, ComposeMessageActivity.class);
                intent.putExtra("sms_body", msgBody);
                startActivity(intent);
                break;
            case MENU_CANCEL_FAVORITE:
                unFavorivteMessage(msgId);
                break;
            case MENU_EDIT_QUICK_TEXT:
                createGnDialog(EDIT_QUICK_TEXT_DIALOG, msgId, msgBody).show();
                break;
            case MENU_DELETE_QUICK_TEXT:
                createGnDialog(DELETE_QUICK_TEXT_DIALOG, msgId, "").show();
                break;
        }
        mAdapter.notifyDataSetChanged();
        return super.onContextItemSelected(item);
    }

    private MenuItem mMenuBatchOperationItem;
    private MenuItem mMenuChangeEncryptionItem;
    private MenuItem mMenuCancelAllFavoriteItem;
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        // gionee zhouyj 2012-05-16 modify for CR00601094 start
        //gionee gaoj 2012-8-7 added for CR00671408 start
        if(!MmsApp.sIsExchangeExist) {
            menu.removeItem(R.id.gn_action_exchange);
        }
        if (!MmsApp.isOpenApi()) {
            menu.removeItem(R.id.gn_action_doctoran);
        }
        //gionee gaoj 2012-8-7 added for CR00671408 end

        // check mms import & export
        if(!TabActivity.checkMsgImportExportSms()) {
            menu.removeItem(R.id.gn_action_in_out);
        }
        mMenuCancelAllFavoriteItem = menu.findItem(R.id.gn_action_cancel_all_favorite);
        if (mMenuCancelAllFavoriteItem != null) {
            if(mFavList == null || mFavList.size() == 0) {
                mMenuCancelAllFavoriteItem.setVisible(false);
            } else {
                mMenuCancelAllFavoriteItem.setVisible(true);
            }
        }
        //gionee gaoj added for CR00725602 20121201 start
        mMenuChangeEncryptionItem = menu.findItem(R.id.gn_action_encryption);
        if (mMenuChangeEncryptionItem != null) {
            mMenuChangeEncryptionItem.setVisible(false);
        }
        mMenuBatchOperationItem = menu.findItem(R.id.gn_action_batch_operation);
        if (mMenuBatchOperationItem != null) {
            mMenuBatchOperationItem.setVisible(false);
        }
        //gionee gaoj added for CR00725602 20121201 end
        super.onPrepareOptionsMenu(menu);
    }

    //gionee gaoj 2012-6-25 added for CR00625692 start
    void updateFavList() {
        queryFavoriteListChildData();
        mAdapter.notifyDataSetChanged();
    }
    //gionee gaoj 2012-6-25 added for CR00625692 end
}
