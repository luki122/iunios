/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.ui;

import java.lang.ref.WeakReference;

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationListItem;
import com.android.mms.ui.ConversationListItemData;
import com.android.mms.util.DraftCache;

import android.R.color;
import aurora.app.AuroraAlertDialog;
import android.app.ListFragment;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
// gionee zhouyj 2012-05-19 add for CR00601094 start
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import com.android.mms.MmsApp;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.widget.ImageButton;
import android.widget.TextView;
// gionee zhouyj 2012-05-19 add for CR00601094 end
// gionee zhouyj 2012-05-29 add for CR00601178 start
import aurora.widget.AuroraButton;

import com.gionee.mms.online.LogUtils;
import com.gionee.mms.ui.CustomMenu.DropDownMenu;
import com.gionee.mms.ui.TabActivity.ViewPagerVisibilityListener;

import android.widget.PopupMenu;
import com.android.mms.ui.ConversationListAdapter;
// gionee zhouyj 2012-05-29 add for CR00601178 end
// gionee zhouyj 2012-08-22 add for CR00678634 start
import com.android.mms.util.GnActionModeHandler;
import com.android.mms.util.GnSelectionManager;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
//gionee zhouyj 2012-08-22 add for CR00678634 end
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
// gionee lwzh add for CR00714645 20121018 begin 
import android.widget.Toast;
// gionee lwzh add for CR00714645 20121018 end

//gionee gaoj 2013-2-19 adde for CR00771935 start
import android.widget.ImageView;
//gionee gaoj 2013-2-19 adde for CR00771935 end

//gionee lwzh modify for CR00774362 20130227 

public class DraftFragment extends ListFragment implements ViewPagerVisibilityListener, DraftCache.OnDraftChangedListener{
        //gionee zhouyj 2012-08-22 add for CR00678634 start
//        , OnItemLongClickListener {
        // gionee zhouyj 2012-08-22 add for CR00678634 end

    private DraftListAdapter mDraftsAdapter;
    private QueryHandler mQueryHandler;
    private LinearLayout mEmptyView;
    private TextView mEmptyTextView;
    private static final int QUERY_TOKEN = 42;
    public static final int DELETE_DRATF_TOKEN = 43;

    //Gionee <zhouyj> <2013-05-03> modify for CR00807509 begin
    //DraftFragment   ContextMenu id from 50 to 69
    private static final int MENU_DELETE            = 50;
    private static final int MENU_VIEW              = 51;
    private static final int MENU_DELETE_ALL_DRATFS = 52;
    //Gionee <zhouyj> <2013-05-03> modify for CR00807509 end
    // gionee zhouyj 2012-04-28 added for CR00585947 start
    private boolean mIsResumed = false;
    // gionee zhouyj 2012-04-28 added for CR00585947 end
    
    // gionee zhouyj 2012-08-22 add for CR00678634 start
    private  static GnActionModeHandler<Long> mActionModeHandler = null;
    private Map<Integer, Long> mThreadsMap = new HashMap<Integer, Long>();
    // gionee zhouyj 2012-08-22 add for CR00678634 end

    // gionee lwzh add for CR00714645 20121018 begin 
    private boolean mQueryingAfterContentChanged = false;
    // gionee lwzh add for CR00714645 20121018 end
    // gionee zhouyj 2012-10-26 add for CR00718476 start 
    private static boolean sInMultiMode = false;
    // gionee zhouyj 2012-10-26 add for CR00718476 end 

    //gionee gaoj 2012-10-12 added for CR00711168 start
    private boolean mSafeMode = false;
    //gionee gaoj 2012-10-12 added for CR00711168 end
    // gionee zhouyj 2012-11-19 add for CR00729484 start 
    private static boolean sResumeNeedQuery = true;
    // gionee zhouyj 2012-11-19 add for CR00729484 end 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mQueryHandler = new QueryHandler(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View fragmentView = inflater.inflate(R.layout.gn_favorites_list, container, false);
        mEmptyView = (LinearLayout) fragmentView.findViewById(R.id.gn_draft_empty);
        mEmptyTextView = (TextView) fragmentView.findViewById(R.id.gn_draft);
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);

        DraftCache.getInstance().addOnDraftChangedListener(this);
        //gionee gaoj 2012-9-24 added for CR00696263 start
//        startQuery();
        //gionee gaoj 2012-9-24 added for CR00696263 end
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        //gionee gaoj 2012-10-12 added for CR00711168 start
        if (MmsApp.mIsSafeModeSupport) {
            mSafeMode = true;
//            startQuery();
        } else {
            if (mSafeMode) {
//                startQuery();
            }
        }
        //gionee gaoj 2012-10-12 added for CR00711168 end
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        // gionee zhouyj 2012-04-28 added for CR00585947 start
        mIsResumed = true;
        // gionee zhouyj 2012-04-28 added for CR00585947 end

        //gionee gaoj 2012-9-24 added for CR00696263 start
        
        //gionee gaoj 2012-9-24 added for CR00696263 end
        // gionee zhouyj 2012-11-19 add for CR00729484 start 
//        if (sResumeNeedQuery) {
//            startQuery();
//        }
        // gionee zhouyj 2012-11-19 add for CR00729484 end 
        super.onResume();
    }
    
    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible == false) {
            // gionee zhouyj 2013-04-02 add for CR00792152 start
            if (mActionModeHandler != null && mActionModeHandler.inSelectionMode()) {
                mActionModeHandler.leaveSelectionMode();
            }
            // gionee zhouyj 2013-04-02 add for CR00792152 end
            return;
        }
        
        if (mDraftsAdapter == null) {
            mDraftsAdapter = new DraftListAdapter(getActivity(), null);
            setListAdapter(mDraftsAdapter);
            ListView listview = getListView();
            listview.setRecyclerListener(mDraftsAdapter);
            // gionee zhouyj 2012-05-19 add for CR00601094 start
            listview.setOnCreateContextMenuListener(mConvListOnCreateContextMenuListener);
            // gionee zhouyj 2012-08-22 modify for CR00678634 start
            listview.setFastScrollEnabled(true);
            // gionee zhouyj 2012-08-22 modify for CR00678634 end
            
            if (MmsApp.mDarkStyle) {
                mEmptyTextView.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
            }
        }
        mDraftsAdapter.notifyDataSetChanged();
        
        startQuery();
    }
    
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
        menu.removeItem(R.id.gn_action_delete_all);
        menu.removeItem(R.id.gn_action_cancel_all_favorite);
        //gionee gaoj added for CR00725602 20121201 start
        if (mDraftsAdapter == null || mDraftsAdapter.isEmpty()) {
            // gionee zhouyj 2013-03-25 modify for CR00789172 start
            MenuItem multiItem = menu.findItem(R.id.gn_action_batch_operation);
            if (multiItem != null) {
                multiItem.setEnabled(false);
            }
            MenuItem delAllItem = menu.findItem(R.id.gn_action_delete_all_draft);
            if (delAllItem != null) {
                delAllItem.setEnabled(false);
            }
            // gionee zhouyj 2013-03-25 modify for CR00789172 end
        } else {
//            MenuItem batchItem = menu.findItem(R.id.gn_action_batch_operation);
//            batchItem.setTitle(R.string.gn_action_batch_delete);
        }
        menu.removeItem(R.id.gn_action_encryption);
        //gionee gaoj added for CR00725602 20121201 end
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        // gionee zhouyj 2012-05-24 modify for CR00608433 start
        switch(item.getItemId()) {
        case R.id.gn_action_delete_all_draft:
            confirmDeleteDraftDialog(new DeleteDraftListener(-1, mQueryHandler, getActivity()), true);
            break;
        //gionee gaoj added for CR00725602 20121201 start
        case R.id.gn_action_batch_operation:
            // gionee lwzh add for CR00714645 20121018 begin 
            if (DraftCache.getInstance().getSavingDraft() || mQueryingAfterContentChanged) {
                Toast.makeText(getActivity(), R.string.gn_mms_updating, Toast.LENGTH_SHORT).show();
                break;
            }
            // gionee lwzh add for CR00714645 20121018 end
            if(null == mActionModeHandler) {
                initDraftThreadsMap();
                initActionModeHandler();
                mActionModeHandler.enterSelectionMode(false, null);
            }
            break;
        //gionee gaoj added for CR00725602 20121201 end
        }
        return super.onOptionsItemSelected(item);
        // gionee zhouyj 2012-05-24 modify for CR00608433 end 
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        DraftCache.getInstance().removeOnDraftChangedListener(this);
        // gionee zhouyj 2012-11-30 modify for CR00737017 start 
        if (mDraftsAdapter != null) {
            mDraftsAdapter.changeCursor(null);
        }
        // gionee zhouyj 2012-11-30 modify for CR00737017 end 
        super.onDestroy();
    }

    @Override
    public void onDraftChanged(long threadId, boolean hasDraft) {
        // TODO Auto-generated method stub
       if (mThreadsMap.containsKey(threadId)) {
            //gionee gaoj 2012-6-6 added for CR00614549 start
            if (isResumed()) {
                startQuery();
            }
            //gionee gaoj 2012-6-6 added for CR00614549 end
        }
    }

    private class DraftListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
        private final static String TAG = "FavoritesAdapter";
        private final LayoutInflater mInflater;
        // gionee zhouyj 2012-08-22 add for CR00678634 start
        private GnSelectionManager<Long> mSelectionManager;
        // gionee zhouyj 2012-08-22 add for CR00678634 end
        // gionee zhouyj 2012-10-12 add for CR00711214 start 
        private boolean mIsShowCheckBox = false;
        // gionee zhouyj 2012-10-12 add for CR00711214 end 
        public DraftListAdapter(Context context, Cursor c) {
            super(context, c, false /** auto-requery */
            );
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = mInflater.inflate(R.layout.gn_conversation_list_item, parent, false);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (!(view instanceof ConversationListItem)) {
                Log.e(TAG, "Unexpected bound view: " + view);
                return;
            }

            ConversationListItem headerView = (ConversationListItem) view;
            Conversation conv = Conversation.from(context, cursor);
            ConversationListItemData ch = new ConversationListItemData(context, conv);

            // gionee lwzh add for CR00706055 20121003 begin
            ch.setGnDraft(true);
            // gionee lwzh add for CR00706055 20121003 end
            // gionee zhouyj 2012-10-12 add for CR00711214 start 
            headerView.setCheckBoxVisibility(mIsShowCheckBox);
            // gionee zhouyj 2012-10-12 add for CR00711214 end 
            // gionee zhouyj 2012-08-22 modify for CR00678634 start
            headerView.bind(context, ch, conv, mSelectionManager, cursor.getPosition());
            // gionee zhouyj 2012-08-22 mofidy for CR00678634 end
        }

        //gionee gaoj 2012-9-20 added for CR00699291 start
        private Runnable mQueryRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(mIsResumed || !mQueryingAfterContentChanged) {
                    startQuery();
                    mQueryingAfterContentChanged = true;
                    // gionee zhouyj 2012-12-05 add for CR00738571 start 
                    if (mActionModeHandler != null && mActionModeHandler.inSelectionMode()) {
                        mActionModeHandler.leaveSelectionMode();
                    }
                    // gionee zhouyj 2012-12-05 add for CR00738571 end 
                }
            }
        };
        //gionee gaoj 2012-9-20 added for CR00699291 end

        @Override
        protected void onContentChanged() {
            mQueryHandler.removeCallbacks(mQueryRunnable);

            // gionee lwzh add for CR00714645 20121018 begin 
            mQueryHandler.postDelayed(mQueryRunnable, 200);       
            // gionee lwzh add for CR00714645 20121018 end
        }
        @Override
        public void onMovedToScrapHeap(View view) {
            // TODO Auto-generated method stub
            ConversationListItem headerView = (ConversationListItem)view;
            headerView.unbind();
        }
        // gionee zhouyj 2012-08-22 add for CR00678634 start
        public void setSelectionManager(GnSelectionManager<Long> selectionManager) {
            mSelectionManager = selectionManager;
        }
        // gionee zhouyj 2012-08-22 add for CR00678634 end
        
        // gionee zhouyj 2012-10-12 add for CR00711214 start 
        public void showCheckBox(boolean show) {
            mIsShowCheckBox = show;
        }
        // gionee zhouyj 2012-10-12 add for CR00711214 end 
    }

    void startQuery() {
        Conversation.startQueryForAllDraft(mQueryHandler, QUERY_TOKEN);
    }

    private class QueryHandler extends AsyncQueryHandler {
        protected final WeakReference<TabActivity> mActivity;
        Context mContext;
        private int mCount = 0;


        public QueryHandler(Context context) {
            super(context.getContentResolver());
            mActivity = new WeakReference<TabActivity>((TabActivity) context);
            mContext = context;
        }

        @Override
        protected void onDeleteComplete(int arg0, Object arg1, int arg2) {
            DraftFragment.this.startQuery();
            super.onDeleteComplete(arg0, arg1, arg2);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final TabActivity activity = mActivity.get();

            // gionee lwzh add for CR00714645 20121018 begin 
            mQueryingAfterContentChanged = false;
            // gionee lwzh add for CR00714645 20121018 end
            // gionee zhouyj 2013-01-09 add for CR00762279 start 
            if(mActionModeHandler != null) {
                mActionModeHandler.leaveSelectionMode();
            }
            // gionee zhouyj 2013-01-09 add for CR00762279 end 
            //gionee gaoj 2012-10-12 added for CR00711168 start
            if (MmsApp.mIsSafeModeSupport) {
                mEmptyView.setVisibility(View.VISIBLE);
                // gionee zhouyj 2012-11-27 modify CR00736134 start 
                if (isVisible()) {
                    getListView().setVisibility(View.GONE);
                }
                // gionee zhouyj 2013-03-25 add for CR00789172 start
                mDraftsAdapter.changeCursor(null);
                // gionee zhouyj 2013-03-25 add for CR00789172 end
                // gionee zhouyj 2012-11-27 modify CR00736134 end 
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return;
            }
            //gionee gaoj 2012-10-12 added for CR00711168 end
            if (null != cursor) {
                mCount = cursor.getCount();
            }

            if (cursor != null && activity != null && !activity.isFinishing()) {
                mDraftsAdapter.changeCursor(cursor);
                if (cursor.getCount() != 0) {
                    mEmptyView.setVisibility(View.GONE);
                    // gionee zhouyj 2012-11-27 modify CR00736134 start 
                    if (isVisible()) {
                        getListView().setVisibility(View.VISIBLE);
                    }
                    // gionee zhouyj 2012-11-27 modify CR00736134 end 
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                //gionee gaoj 2012-5-25 added for CR00421454 start
                TabActivity.sTabActivity.invalidateOptionsMenu();
                //gionee gaoj 2012-5-25 added for CR00421454 end
                // gionee zhouyj 2012-08-22 add for CR00678634 start

                // gionee zhouyj 2012-08-22 add for CR00678634 end
            } else {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        public int getCursorCount() {
            return mCount;
        }

    }

    private final OnCreateContextMenuListener mConvListOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

            //gionee gaoj added for CR00725602 20121201 start
            if (null != mActionModeHandler) {
                return;
            }
            //gionee gaoj added for CR00725602 20121201 end
            // gionee lwzh add for CR00714645 20121018 begin 
            if (DraftCache.getInstance().getSavingDraft() || mQueryingAfterContentChanged) {
                Toast.makeText(getActivity(), R.string.gn_mms_updating, Toast.LENGTH_SHORT).show();
                return;
            }
            // gionee lwzh add for CR00714645 20121018 end
            Cursor cursor = mDraftsAdapter.getCursor();
            if (cursor == null || cursor.getPosition() < 0) {
                return;
            }

            menu.setHeaderTitle(R.string.tab_draft);

//            menu.add(0, MENU_VIEW, 0, R.string.edit);
            menu.add(0, MENU_DELETE, 0, R.string.delete_dratf);
        }
    };

    //gionee gaoj added for CR00725602 20121201 start
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mDraftsAdapter.getCursor();

        int menuId = item.getItemId();
        Conversation conv = Conversation.from(getActivity(), cursor);
        long tid = conv.getThreadId();

        switch (menuId) {
            case MENU_DELETE:
                confirmDeleteDraftDialog(new DeleteDraftListener(tid, mQueryHandler, getActivity()), false);
                break;
            case MENU_VIEW:

                startActivity(ComposeMessageActivity.createIntent(getActivity(), tid));
                break;
        }

        return super.onContextItemSelected(item);
    }
    //gionee gaoj added for CR00725602 20121201 end

    public void confirmDeleteDraftDialog(final DeleteDraftListener listener, boolean deleteAll) {
       
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
        builder.setTitle(R.string.confirm_dialog_title)//.setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(deleteAll ? getString(R.string.confirm_delete_all_dratfs) : getString(R.string.confirm_delete_dratf))
                .setPositiveButton(R.string.OK, listener)
                .setNegativeButton(R.string.no, null).show();
        
    }

    public class DeleteDraftListener implements OnClickListener {
        private final long mThreadId;
        private final AsyncQueryHandler mHandler;
        private final Context mContext;

        public DeleteDraftListener(long threadId, AsyncQueryHandler handler, Context context) {
            mThreadId = threadId;
            mHandler = handler;
            mContext = context;
        }

        public void onClick(DialogInterface dialog, final int whichButton) {

            if (mThreadId == -1) {

                Cursor cur = mDraftsAdapter.getCursor();
                if (cur != null) {
                    cur.moveToPosition(-1);

                    while (cur.moveToNext()) {
                        long threadId = cur.getLong(0);

                        DraftCache.getInstance().setDraftState(threadId, false);
                        Conversation.startDelete(mHandler, DELETE_DRATF_TOKEN,
                                true, threadId);
                    }
                }

                mDraftsAdapter.changeCursor(null);
                mDraftsAdapter.notifyDataSetChanged();
            } else {
                Conversation.startDelete(mHandler, DELETE_DRATF_TOKEN, false,
                        mThreadId);
                DraftCache.getInstance().setDraftState(mThreadId, false);
            }

            dialog.dismiss();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        // gionee lwzh add for CR00714645 20121018 begin 
        if (DraftCache.getInstance().getSavingDraft() || mQueryingAfterContentChanged) {
            Toast.makeText(getActivity(), R.string.gn_mms_updating, Toast.LENGTH_SHORT).show();
            return;
        }
        // gionee lwzh add for CR00714645 20121018 end
    
        // gionee zhouyj 2012-08-22 modify for CR00678634 start
        if(null != mActionModeHandler && mActionModeHandler.inSelectionMode()) {
            mActionModeHandler.getSelectionManger().toggle(id);
        } else {
            // gionee zhouyj 2012-11-09 add for CR00725755 start 
            registerReceiver();
            // gionee zhouyj 2012-11-09 add for CR00725755 end 
            startActivity(ComposeMessageActivity.createIntent(getActivity(), id).putExtra("from_draft_box", true));
            // gionee zhouyj 2012-12-26 add for CR00753821 start 
            mQueryingAfterContentChanged = true;
            // gionee zhouyj 2012-12-26 add for CR00753821 end 
        }
        // gionee zhouyj 2012-08-22 mofidy for CR00678634 start
    }
    
    //gionee gaoj remove for CR00725602 20121201 start
    //gionee gaoj remove for CR00725602 20121201 end

    @Override
    public void onPause() {
    // TODO Auto-generated method stub
        super.onPause();
        //gionee gaoj 2012-9-24 added for CR00696263 start
        mIsResumed = false;
        //gionee gaoj 2012-9-24 added for CR00696263 end
        // gionee zhouyj 2013-04-01 add for CR00792152 start
        if (mActionModeHandler != null && mActionModeHandler.inSelectionMode()) {
            mActionModeHandler.leaveSelectionMode();
        }
        // gionee zhouyj 2013-04-01 add for CR00792152 end
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Contact.clear();
    }

    private void deleteSeleteDrafts() {
        //gionee gaoj 2013-3-15 modified for CR00784733 start
        Iterator it = null;
        if (mActionModeHandler != null) {
            it = mActionModeHandler.getSelected().iterator();
            long threadId;
            while(it.hasNext()) {
                threadId = (Long) it.next();
                DraftCache.getInstance().setDraftState(threadId, false);
                Conversation.startDelete(mQueryHandler, DELETE_DRATF_TOKEN,
                        true, threadId);
            }
        }
        //gionee gaoj 2013-3-15 modified for CR00784733 end
    }
    // gionee zhouyj 2012-05-19 add for CR00601094 end
    
    // gionee zhouyj 2012-08-22 add for CR00678634 start
    protected void initActionModeHandler() {
        mActionModeHandler = new GnActionModeHandler<Long>(getActivity(), null, R.menu.gn_conversation_multi_select_menu) {

            private MenuItem mDeleteItem = null;
            @Override
            public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                // gionee zhouyj 2012-09-04 modify for CR00686905 start
                int orientation = getActivity().getResources().getConfiguration().orientation;
                if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
                // gionee zhouyj 2012-09-04 modify for CR00686905 end
                // gionee zhouyj 2012-10-12 modify for CR00711214 start 
                mDraftsAdapter.showCheckBox(true);
                // gionee zhouyj 2012-10-12 modify for CR00711214 end 
                // gionee zhouyj 2012-10-26 add for CR00718476 start 
                sInMultiMode = true;
                // gionee zhouyj 2012-10-26 add for CR00718476 end 
                mDraftsAdapter.notifyDataSetChanged();
                super.enterSelectionMode(autoLeave, itemPressing);
            }
            
            @Override
            public Set getDataSet() {
                // TODO Auto-generated method stub
                Set<Long> dataSet = new HashSet<Long>(mThreadsMap.size());
                for(int i = 0; i < mThreadsMap.size(); i++)
                    dataSet.add(mThreadsMap.get(i));
                return dataSet;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                super.onPrepareActionMode(mode, menu);
                mDeleteItem = menu.findItem(R.id.delete);
                if (mActionModeHandler.getSelected().isEmpty()) {
                    mDeleteItem.setEnabled(false);
                    /*if (MmsApp.mDarkStyle) {
                        mDeleteItem.setIcon(R.drawable.gn_com_delete_unuse_dark_bg);
                    }*/
                } else {
                    mDeleteItem.setEnabled(true);
                    /*if (MmsApp.mDarkStyle) {
                        mDeleteItem.setIcon(R.drawable.gn_com_delete_dark_bg);
                    }*/
                }
                return true;
            };

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()) {
                case R.id.delete:
                    new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.confirm_dialog_title)
                    .setMessage(R.string.gn_delete_drafts_message)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(true)
                    .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            deleteSeleteDrafts();
                            leaveSelectionMode();
                        }
                    })  
                    .setNegativeButton(R.string.no, null)
                    .show();
                    break;
                default:
                    break;
                }
                return true;
            }
            
            @Override
            public void updateUi() {
                // TODO Auto-generated method stub
                if (null != mDeleteItem && null != getSelected()) {
                    /*if (MmsApp.mDarkStyle) {
                        mDeleteItem.setIcon(getSelected().isEmpty() ? R.drawable.gn_com_delete_unuse_dark_bg : R.drawable.gn_com_delete_dark_bg);
                    }*/
                    mDeleteItem.setEnabled(!getSelected().isEmpty());
                }
                mDraftsAdapter.notifyDataSetChanged();
            }
            
            @Override
            public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                // TODO Auto-generated method stub
                mDraftsAdapter.setSelectionManager(selectionManager);
            }
            
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // gionee zhouyj 2012-10-12 modify for CR00711214 start 
                mDraftsAdapter.showCheckBox(false);
                // gionee zhouyj 2012-10-12 modify for CR00711214 end 
                mDraftsAdapter.notifyDataSetChanged();
                // gionee zhouyj 2012-10-26 modify for CR00718476 start 
                if (!ConvFragment.isMultiMode()) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                sInMultiMode = false;
                // gionee zhouyj 2012-10-26 modify for CR00718476 end 
                mActionModeHandler = null;
                mThreadsMap.clear();
            }
        };
    }
    // gionee zhouyj 2012-08-22 add for CR00678634 end

    private void initDraftThreadsMap() {
        // TODO Auto-generated method stub
        Cursor cursor = mDraftsAdapter.getCursor();
        cursor.moveToPosition(-1);
        int i = 0;
        while(cursor.moveToNext()) {
            if (mThreadsMap.get(i) == null) {
                mThreadsMap.put(i++, cursor.getLong(0));
            }
        }
    }
    
    // gionee zhouyj 2012-10-26 add for CR00718476 start 
    public static boolean isMultiMode() {
        return sInMultiMode;
    }
    // gionee zhouyj 2012-10-26 add for CR00718476 end 
    
    // gionee zhouyj 2012-11-09 add for CR00725755 start 
    private QueryDraftBroadcast mQueryDraftBroadcast = new QueryDraftBroadcast();
    
    private void registerReceiver() {
        if (getActivity() != null) {
            // gionee zhouyj 2013-01-04 modify for CR00753821 start 
            IntentFilter filter = new IntentFilter();
            filter.addAction("query_draft");
            filter.addAction("reset_flag");
            getActivity().registerReceiver(mQueryDraftBroadcast, filter);
            // gionee zhouyj 2013-01-04 modify for CR00753821 end 
        }
    }
    
    private void unRegisterReceiver() {
        if (getActivity() != null) {
            getActivity().unregisterReceiver(mQueryDraftBroadcast);
        }
    }
    
    private class QueryDraftBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // gionee zhouyj 2013-01-04 modify for CR00753821 end 
            if (intent != null && "query_draft".equals(intent.getAction())) {
                unRegisterReceiver();
                startQuery();
            } else if (intent != null && "reset_flag".equals(intent.getAction())) {
                mQueryingAfterContentChanged = false;
            }
            // gionee zhouyj 2013-01-04 modify for CR00753821 end 
        }
    }
    // gionee zhouyj 2012-11-09 add for CR00725755 end 
    
    // gionee zhouyj 2012-11-19 add for CR00729484 start 
    public static void resumeNeedQuery(boolean need) {
        sResumeNeedQuery = need;
    }
    // gionee zhouyj 2012-11-19 add for CR00729484 end 
}
