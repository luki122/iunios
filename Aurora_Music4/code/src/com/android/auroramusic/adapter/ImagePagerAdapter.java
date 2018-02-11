/*
 * Copyright 2014 trinea.cn All right reserved. This software is the confidential and proprietary information of
 * trinea.cn ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with trinea.cn.
 */
package com.android.auroramusic.adapter;

import java.util.List;

import com.android.auroramusic.util.DisplayUtil;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.widget.AuroraLinearLayout;
import com.android.music.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.xiami.sdk.entities.Banner;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * ImagePagerAdapter
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2014-2-23
 */
public class ImagePagerAdapter extends RecyclingPagerAdapter {

	private static final String TAG = "ImagePagerAdapter";
    private Context       context;
    private List<Banner> imageIdList;
    private OnBannerClickListener mOnBannerClickListener;
    private int           size;
    private boolean       isInfiniteLoop;
    
    public static int MAXNUM =5;
    private DisplayImageOptions options;
    
    public ImagePagerAdapter(Context context, List<Banner> imageIdList,OnBannerClickListener l) {
        this.context = context;
        this.imageIdList = imageIdList;
        if(imageIdList.size()>MAXNUM){
        	this.size = MAXNUM;
        }else{
        	this.size = imageIdList.size();
        }
        mOnBannerClickListener=l;
        isInfiniteLoop = false;
        initImageCacheParams(context);
    }

    @Override
    public int getCount() {
        // Infinite loop
        return isInfiniteLoop ? Integer.MAX_VALUE : size;
    }

    /**
     * get really position
     * 
     * @param position
     * @return
     */
    private int getPosition(int position) {
        return isInfiniteLoop ? position % size : position;
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.aurora_online_banner_layout, null);
            holder.imageView=(ImageView)view.findViewById(R.id.aurora_img);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        final Banner item = imageIdList.get(getPosition(position));
        ((AuroraLinearLayout)view).setShouldClicked(true);
        view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				LogUtil.d(TAG, "imageview clicked!!");
				if(mOnBannerClickListener!=null){
					mOnBannerClickListener.onBannerClick(item);
				}
			}
		});
  //      LogUtil.d(TAG, "chenhl---------ImagePagerAdapter:"+item.getImageUrl());
/*        ImageManager.render(item.getImageUrl(),holder.imageView,
				1008, 400, 0, true, true, R.drawable.aurora_online_music_defualt);*/
       // mImageFetcher.loadImageAndSize(item.getImageUrl(), holder.imageView, 100, DisplayUtil.dip2px(context, 336), DisplayUtil.dip2px(context, 133));
        ImageLoader.getInstance().displayImage(item.getImageUrl(), holder.imageView, options);
        return view;
    }

    private static class ViewHolder {

        ImageView imageView;
    }

    /**
     * @return the isInfiniteLoop
     */
    public boolean isInfiniteLoop() {
        return isInfiniteLoop;
    }

    /**
     * @param isInfiniteLoop the isInfiniteLoop to set
     */
    public ImagePagerAdapter setInfiniteLoop(boolean isInfiniteLoop) {
        this.isInfiniteLoop = isInfiniteLoop;
        return this;
    }
    
    public interface OnBannerClickListener {
    	public void onBannerClick(Banner item);
    }

	private void initImageCacheParams(Context context) {
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.aurora_online_music_defualt)
		.showImageForEmptyUri(R.drawable.aurora_online_music_defualt)
		.showImageOnFail(R.drawable.aurora_online_music_defualt)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.displayer(new SimpleBitmapDisplayer())
		.build();
	}
}
