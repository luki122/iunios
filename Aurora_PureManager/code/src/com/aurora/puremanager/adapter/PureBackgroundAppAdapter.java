package com.aurora.puremanager.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.viewcache.PureBackgroundListItemCache;

import java.util.List;

public class PureBackgroundAppAdapter extends ArrayAdapter<BaseData> implements View.OnClickListener {
    private Activity activity;

    public PureBackgroundAppAdapter(Activity activity, List<BaseData> listData) {
        super(activity, 0, listData);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        PureBackgroundListItemCache holder;
        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.pure_background_app_list_item, parent, false);
            holder = new PureBackgroundListItemCache(convertView);
            convertView.setTag(holder);
        } else {
            holder = (PureBackgroundListItemCache) convertView.getTag();
        }

        if (getCount() <= position) {
            return convertView;
        }

        AppInfo item = (AppInfo) getItem(position);
        holder.getAppName().setText(item.getAppName());
        String subText = "";
        if (item.isStopAutoStart()) {
            subText += "禁止自启,";
        }
        if (item.isStopWakeup()) {
            subText += "禁止唤醒,";
        }
        if (item.isAutoSleep()) {
            subText += "自动休眠";
        }
        if (subText.length() != 0) {
            if (subText.endsWith(",")) {
                subText = subText.substring(0,subText.length()-1);
            }
            holder.getSubText().setText(subText);
        } else {
            holder.getSubText().setText("无");
        }

        String iconViewTag = item.getPackageName() + "@app_icon";
        holder.getAppIcon().setTag(iconViewTag);
        Drawable cachedImage = ImageLoader.getInstance(getContext()).displayImage(
                holder.getAppIcon(),
                item.getPackageName(),
                iconViewTag,
                new ImageCallback() {
                    public void imageLoaded(Drawable imageDrawable, Object viewTag) {
                        if (parent == null ||
                                imageDrawable == null ||
                                viewTag == null) {
                            return;
                        }
                        ImageView imageViewByTag = (ImageView) parent.findViewWithTag(viewTag);
                        if (imageViewByTag != null) {
                            imageViewByTag.setImageDrawable(imageDrawable);
                        }
                    }
                });
        if (cachedImage != null) {
            holder.getAppIcon().setImageDrawable(cachedImage);
        } else {
            holder.getAppIcon().setImageResource(R.drawable.def_app_icon);
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {
    }
}
