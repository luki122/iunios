package com.aurora.change.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.thememanager.R;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.LogFactory;
import com.aurora.filemanager.FileInfo;

public class DesktopWallpaperSourceSelectAdapter extends BaseAdapter {
    private static final String TAG = "WallpaperSourceSelectAdapter";
    private static CommonLog log = LogFactory.createLog(TAG);
    private ImageResizer mImageResizer;

    private Context mContext;
    private List<FileInfo> fileInfos;

    public DesktopWallpaperSourceSelectAdapter(Context context, List<FileInfo> fileInfos) {
        this.mContext = context;
        this.fileInfos = fileInfos;
    }

    @Override
    public int getCount() {
        return fileInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        View view = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.desktop_source_select_item, null);
            viewHolder.wallpaperImage = ( ImageView ) view.findViewById(R.id.desktop_source_item);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = ( ViewHolder ) view.getTag();
        }

        mImageResizer.loadImage(fileInfos.get(position).filePath, viewHolder.wallpaperImage);
        return view;
    }

    static class ViewHolder {
        ImageView wallpaperImage;
    }

    public void setImageResizer(ImageResizer imageResizer) {
        mImageResizer = imageResizer;
    }

    public void onResume() {
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(false);
            notifyDataSetChanged();
        }
    }

    public void onPause() {
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(true);
            mImageResizer.flushCache();
        }
    }

    public void clearData() {
        if (fileInfos != null) {
            fileInfos.clear();
            fileInfos = null;
        }
        if (mImageResizer != null) {
            // mImageResizer.clearCache();
            // mImageResizer.clearMemoryCache();
            mImageResizer.closeCache();
        }
    }
}
