package com.aurora.change.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.change.R;
import com.aurora.change.cache.ImageLoader;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.LogFactory;
import com.aurora.filemanager.FileInfo;

public class WallpaperSourceSelectAdapter extends BaseAdapter {
    private static final String TAG = "WallpaperSourceSelectAdapter";
    private static CommonLog log = LogFactory.createLog(TAG);
    private ImageResizer mImageResizer;

    private boolean mBusy = false;

    public void setFlagBusy(boolean busy) {
        this.mBusy = busy;
    }

//    private ImageLoader mImageLoader;
    private Context mContext;
    private List<FileInfo> fileInfos;
    private FileInfo fileInfo;
    private List<FileInfo> mChooseFiles;
    private boolean isChoice[];
    private int count;

    public WallpaperSourceSelectAdapter(Context context, List<FileInfo> fileInfos) {
        this.mContext = context;
        this.fileInfos = fileInfos;
//        mImageLoader = new ImageLoader(context);
        mChooseFiles = new ArrayList<FileInfo>();
        if (getCount() != 0) {
            initCheckBox();
        }
        count = 0;
    }

    public void initCheckBox() {
        isChoice = new boolean[getCount()];
        for (int i = 0; i < getCount(); i++) {
            isChoice[i] = false;
        }
    }

    public void checkBoxState(int position) {
        isChoice[position] = isChoice[position] == true ? false : true;
        if (isChoice[position]) {
            count++;
            mChooseFiles.add(fileInfos.get(position));
        } else {
            count--;
            mChooseFiles.remove(fileInfos.get(position));
        }
    }

    public boolean getCheckState(int position) {
        return isChoice[position];
    }

//    public ImageLoader getImageLoader() {
//        return mImageLoader;
//    }

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

    public int getChooseCount() {
        return count;
    }

    public List<FileInfo> getChooseFiles() {
        return mChooseFiles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        View view = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.wallpaper_source_select_item, null);
            viewHolder.wallpaperImage = ( ImageView ) view.findViewById(R.id.wallpaperImage);
            viewHolder.picCheckbox = ( ImageView ) view.findViewById(R.id.picCheckbox);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = ( ViewHolder ) view.getTag();
        }

//        if (isChoice[position]) {
//            viewHolder.picCheckbox.setImageResource(R.drawable.source_select_checkbox_choosed);
//        } else {
//            viewHolder.picCheckbox.setImageResource(R.drawable.source_select_checkbox_normal);
//        }
        viewHolder.picCheckbox.setActivated(isChoice[position]);

//        String url = "";
//        fileInfo = fileInfos.get(position);
//        url = String.valueOf(fileInfo.filePath);

//        Bitmap bitmap = mImageLoader.getmeMemoryCache().getBitmapFromCache(url);
//        if (bitmap != null) {
//            viewHolder.wallpaperImage.setImageBitmap(bitmap);
//            return convertView;
//        }
//
//        viewHolder.wallpaperImage.setImageResource(R.drawable.loading);
//
//        if (!mBusy) {
//            mImageLoader.DisplayImage(url, viewHolder.wallpaperImage, false);
//        } else {
//            mImageLoader.DisplayImage(url, viewHolder.wallpaperImage, true);
//        }
        mImageResizer.loadImage(fileInfos.get(position).filePath, viewHolder.wallpaperImage);
//        viewHolder.wallpaperImage.setImageResource(R.drawable.default_lockpaper)
        return view;
    }

    static class ViewHolder {
        ImageView wallpaperImage;
        ImageView picCheckbox;
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
