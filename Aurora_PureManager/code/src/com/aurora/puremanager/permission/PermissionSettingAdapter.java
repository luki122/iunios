package com.aurora.puremanager.permission;

import java.util.List;

import com.aurora.puremanager.R;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.os.SystemProperties;
import android.widget.TextView;

public class PermissionSettingAdapter extends BaseAdapter {

    public static final String ALL_TRUST = "1";
    public static final String PARTIAL_TRUST = "0";
    public static final String NONE_TRUST = "-1";
    public List<ItemInfo> mData;
    protected LayoutInflater mInflater;
    private Context mContext;

    public PermissionSettingAdapter(Context context, List<ItemInfo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return this.mData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return this.mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.permission_setting_listview_item, parent, false);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mTitle_add = (TextView) convertView.findViewById(R.id.title_add);
            view.mSummary = (TextView) convertView.findViewById(R.id.summary);
            view.mRadioButton = (RadioButton) convertView.findViewById(R.id.radiobutton);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        if (mData.size() > 0) {
            view.mTitle.setText(mData.get(position).getName());
            if (mData.get(position).getSummary() != null) {
                view.mSummary.setText(Html.fromHtml(mData.get(position).getSummary()));
                view.mSummary.setVisibility(View.VISIBLE);
            } else {
                view.mSummary.setVisibility(View.GONE);
            }
        }

        if (position == 1) {
            view.mTitle_add.setText(mContext.getResources().getString(R.string.recommend_use));
            view.mTitle_add.setVisibility(View.VISIBLE);
        } else {
            view.mTitle_add.setVisibility(View.GONE);
        }

        if ("".equalsIgnoreCase(SystemProperties.get("persist.sys.permission.level"))) {
            SystemProperties.set("persist.sys.permission.level",
                    SystemProperties.get("ro.gn.permission.trust.level", "0"));
        }
        if (PARTIAL_TRUST.equalsIgnoreCase(SystemProperties.get("persist.sys.permission.level"))) {
            if (position == 0 || position == 2) {
                view.mRadioButton.setChecked(false);
            } else {
                view.mRadioButton.setChecked(true);
            }
        } else if (ALL_TRUST.equalsIgnoreCase(SystemProperties.get("persist.sys.permission.level"))) {
            if (position == 1 || position == 2) {
                view.mRadioButton.setChecked(false);
            } else {
                view.mRadioButton.setChecked(true);
            }
        } else {
            if (position == 0 || position == 1) {
                view.mRadioButton.setChecked(false);
            } else {
                view.mRadioButton.setChecked(true);
            }
        }

        return convertView;
    }

}