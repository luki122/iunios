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

import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
import com.gionee.mms.data.RecommendManagerDbAdater;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.CursorTreeAdapter;
import aurora.widget.AuroraExpandableListView;
import android.widget.TextView;
import aurora.widget.AuroraExpandableListView.ExpandableListContextMenuInfo;

public class RecommendFragment extends Fragment implements OnCreateContextMenuListener, AuroraExpandableListView.OnChildClickListener{

    private AuroraExpandableListView mListView;
    private RecommendManagerDbAdater mRecommendManagerDbAdater;
    private MyCursrTreeAdapter myCursorTreeAdapter;

    private final static int MENU_FORWARD = 0;
    private final static int MENU_COPY = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mRecommendManagerDbAdater = new RecommendManagerDbAdater(getActivity());
        mRecommendManagerDbAdater.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View fragmentView = inflater.inflate(R.layout.gn_recommend_list, container, false);

        mListView = (AuroraExpandableListView) fragmentView.findViewById(R.id.recommend_expandable_list);

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);


        initMyAdapter();

        //mListView.setGroupIndicator(getResources().getDrawable(R.drawable.gn_expander_ic_folder));
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnChildClickListener(this);
    }

    public void initMyAdapter() {
        Cursor groupCursor = mRecommendManagerDbAdater.getAllGroups();
        getActivity().startManagingCursor(groupCursor);

        // set my adapter
        myCursorTreeAdapter = new MyCursrTreeAdapter(groupCursor, getActivity(), true);

        mListView.setAdapter(myCursorTreeAdapter);
    }

    public class MyCursrTreeAdapter extends CursorTreeAdapter {

        public MyCursrTreeAdapter(Cursor cursor, Context context, boolean autoRequery) {
            super(cursor, context, autoRequery);
        }

        @Override
        protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {

            TextView groupNameView = (TextView) view.findViewById(R.id.groupName);
            String groupName = cursor.getString(cursor
                    .getColumnIndex(RecommendManagerDbAdater.RecommendGroup.mColumName));

            groupNameView.setText(groupName);

            TextView groupCount = (TextView) view.findViewById(R.id.groupCount);
            int count = mRecommendManagerDbAdater.getCountItemsByGroupName(groupName);
            groupCount.setText("[" + count + "]");
        }

        @Override
        protected View newGroupView(Context context, Cursor cursor, boolean isExpanded,
                ViewGroup parent) {

            LayoutInflater inflate = LayoutInflater.from(getActivity());
            View view = inflate.inflate(R.layout.gn_recommend_list_group_item, null);

            bindGroupView(view, context, cursor, isExpanded);

            return view;
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {

            int columIndex = groupCursor
                    .getColumnIndex(RecommendManagerDbAdater.RecommendGroup.mColumId);
            long groupId = groupCursor.getLong(columIndex);

            Cursor childCursor = mRecommendManagerDbAdater.getItemByGroupId(groupId);
            getActivity().startManagingCursor(childCursor);

            return childCursor;
        }

        @Override
        protected View newChildView(Context context, Cursor cursor, boolean isLastChild,
                ViewGroup parent) {
            LayoutInflater inflate = LayoutInflater.from(getActivity());
            View view = inflate.inflate(R.layout.gn_recommend_list_item, null);

            bindChildView(view, context, cursor, isLastChild);

            return view;
        }

        @Override
        protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {

            TextView bodyView = (TextView) view.findViewById(R.id.recommend_body);
            String body = cursor.getString(cursor
                    .getColumnIndex(RecommendManagerDbAdater.RecommendItem.mColumText));
            bodyView.setText(body);
        }
    }

    @Override
    public boolean onChildClick(AuroraExpandableListView arg0, View arg1, int arg2,
            int arg3, long arg4) {
        // TODO Auto-generated method stub
        arg1.showContextMenu();
        myCursorTreeAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        int groupPos = 0;
        int childPos = 0;
        int type = AuroraExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == AuroraExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = AuroraExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = AuroraExpandableListView.getPackedPositionChild(info.packedPosition);
            menu.setHeaderTitle(getMsgBody(groupPos, childPos));
            menu.add(0, MENU_FORWARD, 0, this.getString(R.string.menu_forward));
            menu.add(0, MENU_COPY, 0, this.getString(R.string.gn_copy_text));
        }
    }

    private String getMsgBody(int groupPosition, int childPosition) {
        String msgBody = null;
        Cursor recommendCursor = myCursorTreeAdapter.getChild(groupPosition, childPosition);
        msgBody = recommendCursor.getString(recommendCursor
                .getColumnIndex(RecommendManagerDbAdater.RecommendItem.mColumText));
        return msgBody;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int groupPos = 0;
        int childPos = 0;

        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

        int type = AuroraExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == AuroraExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = AuroraExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = AuroraExpandableListView.getPackedPositionChild(info.packedPosition);
        }

        String msgBody = getMsgBody(groupPos, childPos);

        switch (item.getItemId()) {
            case MENU_FORWARD:
                Intent intent = new Intent(getActivity(), ComposeMessageActivity.class);
                intent.putExtra("sms_body", msgBody);
                startActivity(intent);
                break;

            case MENU_COPY:
                ClipboardManager clip = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clip.setText(msgBody);
                break;
        }

        myCursorTreeAdapter.notifyDataSetChanged();
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        myCursorTreeAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mRecommendManagerDbAdater != null) {
            mRecommendManagerDbAdater.close();
            mRecommendManagerDbAdater = null;
        }
    }
}
