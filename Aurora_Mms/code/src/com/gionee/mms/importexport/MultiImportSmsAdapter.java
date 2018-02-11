/*
 *
 * Copyright (C) 2011 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.importexport;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.mms.R;
import java.io.File;

import com.aurora.featureoption.FeatureOption;

public class MultiImportSmsAdapter extends BaseAdapter {
    private ArrayList<String> mData = new ArrayList<String>();
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private ArrayList<String> mSelectedPath = new ArrayList<String>();
    private ImportSmsAdapterCallBack mCallBack = null;
    private final String MNT = "/mnt/";

    public MultiImportSmsAdapter(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public ArrayList<String> getmData() {
        return mData;
    }

    public void setmData(ArrayList<String> mData) {
        this.mData = mData;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    public int getSelectedNumber() {
        return mSelectedPath.size();
    }

    public boolean isAllSelected() {
        if (mSelectedPath.size() > 0 && mSelectedPath.size() == mData.size()) {
            return true;
        } else {
            return false;
        }
    }

    public void setSelectedAll() {
        mSelectedPath.clear();
        for (int i=0;i<mData.size();i++) {
            mSelectedPath.add(mData.get(i));
        }
        callBack(mSelectedPath);
    }

    public void cancelSelectedAll() {
        mSelectedPath.clear();
        callBack(mSelectedPath);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        HolderView mHolderView = null;
        if (null == convertView) {
            mHolderView = new HolderView();
            convertView = mInflater.inflate(R.layout.gn_import_export_item, null);
            mHolderView.mTextView = (TextView) convertView.findViewById(R.id.content);
            mHolderView.mFilePath = (TextView) convertView.findViewById(R.id.file_path);
            mHolderView.mCheckBox = (CheckBox) convertView.findViewById(R.id.selBtn);
            convertView.setTag(mHolderView);
        } else {
            mHolderView = (HolderView) convertView.getTag();
        }
        mHolderView.mTextView.setText(getFileName(mData.get(position)));
        mHolderView.mFilePath.setText(getFileAbsolutePath(mData.get(position)));
        mHolderView.mCheckBox.setClickable(false);
        // gionee zhouyj 2012-08-08 modify for CR00667854 start 
        String absolutePath = descriptionToPath(mHolderView.mFilePath.getText().toString()) + "/" + mHolderView.mTextView.getText().toString();
        if (mSelectedPath.contains(absolutePath)) {
        // gionee zhouyj 2012-08-08 modify for CR00667854 end 
            mHolderView.mCheckBox.setChecked(true);
        } else {
            mHolderView.mCheckBox.setChecked(false);
        }
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                String virtualPath = descriptionToPath(((TextView) view.findViewById(R.id.file_path)).getText().toString());
                String path = virtualPath + "/" + ((TextView) view.findViewById(R.id.content)).getText().toString();
                if (((CheckBox) view.findViewById(R.id.selBtn)).isChecked()) {
                    ((CheckBox) view.findViewById(R.id.selBtn)).setChecked(false);
                    mSelectedPath.remove(path);
                } else {
                    ((CheckBox) view.findViewById(R.id.selBtn)).setChecked(true);
                    if (!mSelectedPath.contains(path)) {
                        mSelectedPath.add(path);
                    }
                }
                callBack(mSelectedPath);
            }
        });
        return convertView;
    }

    class HolderView {
        public TextView mTextView;
        public TextView mFilePath;
        public CheckBox mCheckBox;
    }

    public interface ImportSmsAdapterCallBack {
        public void onImportSmsAdapterCallBack(ArrayList<String> selectedPaths);
    }

    public void setImportSmsAdapterCallBack(ImportSmsAdapterCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    private void callBack(ArrayList<String> selectedPaths) {
        if (null != mCallBack) {
            mCallBack.onImportSmsAdapterCallBack(selectedPaths);
        }
    }
    
    // gionee zhouyj 2012-05-28 add for CR00607938 start
    private String getFileName(String absolutePath) {
        String name = "";
        int index = absolutePath.lastIndexOf("/");
        if(index != -1) {
            name = absolutePath.substring(index+1, absolutePath.length());
        }
        return name;
    }
    
    private String getFileAbsolutePath(String absolutePath) {
        String path = "";
        int index = absolutePath.lastIndexOf("/");
        if (index != -1) {
            path = absolutePath.substring(0, index);
        }
        return pathToDescription(path);
    }
    
    private String pathToDescription(String path) {
        if(ImportExportSmsActivity.getSdcardCount() < 2) {
            if(path.startsWith(ImportExportSmsActivity.mSDCardPath)) {
                path = path.replaceFirst(ImportExportSmsActivity.mSDCardPath, mContext.getString(R.string.gn_chooser_internal_sdcard));
            }
        } else {
            if(FeatureOption.MTK_2SDCARD_SWAP) {
                if(path.startsWith(ImportExportSmsActivity.mSDCard2Path)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCard2Path, mContext.getString(R.string.gn_chooser_internal_sdcard));
                } else if(path.startsWith(ImportExportSmsActivity.mSDCardPath)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCardPath, mContext.getString(R.string.gn_chooser_external_sdcard));
                }
            } else {
                if(path.startsWith(ImportExportSmsActivity.mSDCard2Path)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCard2Path, mContext.getString(R.string.gn_chooser_external_sdcard));
                } else if(path.startsWith(ImportExportSmsActivity.mSDCardPath)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCardPath, mContext.getString(R.string.gn_chooser_internal_sdcard));
                }
            }
        }
        return path;
    }
    
    private String descriptionToPath (String description) {
        if(ImportExportSmsActivity.getSdcardCount() < 2) {
            if(description.contains(mContext.getString(R.string.gn_chooser_internal_sdcard))) {
                description = description.replaceFirst(mContext.getString(R.string.gn_chooser_internal_sdcard), ImportExportSmsActivity.mSDCardPath);
            }
        } else {
            if(FeatureOption.MTK_2SDCARD_SWAP) {
                if(description.contains(mContext.getString(R.string.gn_chooser_internal_sdcard))) {
                    description = description.replaceFirst(mContext.getString(R.string.gn_chooser_internal_sdcard), ImportExportSmsActivity.mSDCard2Path);
                } else if(description.contains(mContext.getString(R.string.gn_chooser_external_sdcard))) {
                    description = description.replaceFirst(mContext.getString(R.string.gn_chooser_external_sdcard), ImportExportSmsActivity.mSDCardPath);
                }
            } else {
                if(description.contains(mContext.getString(R.string.gn_chooser_internal_sdcard))) {
                    description = description.replaceFirst(mContext.getString(R.string.gn_chooser_internal_sdcard), ImportExportSmsActivity.mSDCardPath);
                } else if(description.contains(mContext.getString(R.string.gn_chooser_external_sdcard))) {
                    description = description.replaceFirst(mContext.getString(R.string.gn_chooser_external_sdcard), ImportExportSmsActivity.mSDCard2Path);
                }
            }
        }
        return description;
    }
    // gionee zhouyj 2012-05-28 add for CR00607938 end
}
