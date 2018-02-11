package com.aurora.puremanager.permission;

import java.util.List;

import com.aurora.puremanager.R;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.SystemProperties;

public class PermissionAdapter extends BaseExpandableListAdapter {
    private int mPosition = -1;
    private List<ItemInfo> mAppLists;
    private Context mContext;

    public PermissionAdapter(Context context) {
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        final int position = groupPosition;
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.softmanager_child_listview, parent,
                    false);
        }

        final Button button1 = (Button) convertView.findViewById(R.id.bt1);
        button1.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mAppLists.get(position).getStatus() != PermissionsInfo.STATUS_PERMIT) {
                    setPermission(PermissionsInfo.STATUS_PERMIT, position, mAppLists);
                    mAppLists.get(position).setStauts(PermissionsInfo.STATUS_PERMIT);
                    notifyDataSetChanged();
                    updateTrust(groupPosition);
                }
            }
        });

        final Button button2 = (Button) convertView.findViewById(R.id.bt2);
        if (PermissionSettingAdapter.PARTIAL_TRUST.equalsIgnoreCase(SystemProperties
                .get("persist.sys.permission.level"))
                || PermissionSettingAdapter.ALL_TRUST.equalsIgnoreCase(SystemProperties
                        .get("persist.sys.permission.level"))) {
            button2.setText(R.string.text_ask);
        } else if (PermissionSettingAdapter.NONE_TRUST.equalsIgnoreCase(SystemProperties
                .get("persist.sys.permission.level"))) {
            button2.setText(R.string.text_ask);
        }

        button2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mAppLists.get(position).getStatus() != PermissionsInfo.STATUS_REQUEST) {
                    setPermission(PermissionsInfo.STATUS_REQUEST, position, mAppLists);
                    mAppLists.get(position).setStauts(PermissionsInfo.STATUS_REQUEST);
                    notifyDataSetChanged();
                    updateTrust(groupPosition);
                }
            }
        });

        final Button button3 = (Button) convertView.findViewById(R.id.bt3);
        button3.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mAppLists.get(position).getStatus() != PermissionsInfo.STATUS_PROHIBITION) {
                    setPermission(PermissionsInfo.STATUS_PROHIBITION, position, mAppLists);
                    mAppLists.get(position).setStauts(PermissionsInfo.STATUS_PROHIBITION);
                    notifyDataSetChanged();
                    updateTrust(groupPosition);
                }
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return mAppLists.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return mAppLists.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ItemInfo info = null;
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.softmanager_permission_adapter_layout, null);
            viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.mStatus = (TextView) convertView.findViewById(R.id.summary);
            viewHolder.mIndicator = (ImageView) convertView.findViewById(R.id.indicator_arrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        info = mAppLists.get(groupPosition);

        if (info != null) {
            viewHolder.mIndicator.setImageResource(R.drawable.open_holo);
            viewHolder.mIndicator.setVisibility(View.VISIBLE);
            viewHolder.mIcon.setImageDrawable(HelperUtils_per.loadIcon(mContext,
                    HelperUtils_per.getApplicationInfo(mContext, info.getPackageName())));
            viewHolder.mTitle.setText(info.getName());
            viewHolder.mStatus.setTag(groupPosition);

            if (info.getStatus() == 1) {
                viewHolder.mStatus.setText(R.string.text_allow);
            } else if (info.getStatus() == 0) {
                viewHolder.mStatus.setText(R.string.text_ask);
            } else {
                viewHolder.mStatus.setText(R.string.text_forbidden);
            }
            viewHolder.mStatus.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return false;
    }

    private void setPermission(int status, int position, final List<ItemInfo> items) {
        if (items.size() > position) {
            final ItemInfo info = items.get(position);
            PermissionsInfo.getInstance().setPermission(mContext, info.mApplicationInfo.packageName,
                    mPosition, status, info);
        }
    }

    public void setPosition(int position) {
        this.mPosition = position;
        if ("".equalsIgnoreCase(SystemProperties.get("persist.sys.permission.level"))) {
            SystemProperties.set("persist.sys.permission.level",
                    SystemProperties.get("ro.gn.permission.trust.level", "0"));
        }
    }

    public void changeItems(List<ItemInfo> appItems) {
        mAppLists = appItems;
    }

    private void updateTrust(int position) {
        queryTrustDB(position);
        if (PermissionsInfo.mTrust == 1) {
            PermissionsInfo.mTrust = 0;
            PermissionsInfo.getInstance().updateTrust(mContext,
                    mAppLists.get(position).mApplicationInfo.packageName, PermissionsInfo.mTrust);
        }
    }

    private void queryTrustDB(int position) {
        String[] projection = new String[] {"packagename", "status"};
        Cursor cursor = mContext.getContentResolver().query(Uri.parse(PermissionsInfo.TRUST_URI), projection,
                "packagename = ?", new String[] {mAppLists.get(position).mApplicationInfo.packageName}, null);
        while (cursor.moveToNext()) {
            int state = cursor.getInt(1);
            PermissionsInfo.mTrust = state;
        }
        cursor.close();
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}