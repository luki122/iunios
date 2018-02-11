package com.aurora.change.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import com.aurora.thememanager.R;
import com.aurora.change.data.NextDayDbControl;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.utils.CommonUtil;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.ImageLoaderHelper;
import com.aurora.change.utils.NextDayLoadAndDisplayTask;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.thememanager.ThemeManagerApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class NextDayPreViewAdapter extends PagerAdapter{
	private String DEBUG_TAG = "Wallpaper_DEBUG";
	private Context mContext;
	private Handler mHandler;
	private ViewPager mViewPager;
	private ArrayList<View> mViews;
	private ArrayList<String> mPaths;
	private ArrayList<NextDayPictureInfo> mPictureList;
	private ImageLoaderHelper mImageLoaderHelper;
	private DisplayImageOptions mOptions;
	private ProgressBar mProgressBar;
		
	public NextDayPreViewAdapter(Context context, ArrayList<NextDayPictureInfo> list, ProgressBar progressBar) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mImageLoaderHelper = ((ThemeManagerApplication) mContext.getApplicationContext()).getImageLoaderHelper();
		mOptions = ImageLoaderHelper.setupDisplayImageOption();
		mPictureList = list;
		mViews = new ArrayList<View>();
		mProgressBar = progressBar;
		initiateViews();
	}
	
	public NextDayPreViewAdapter(Context context, Handler handler, ArrayList<NextDayPictureInfo> list, ProgressBar progressBar) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mHandler = handler;
		mImageLoaderHelper = ((ThemeManagerApplication) mContext.getApplicationContext()).getImageLoaderHelper();
		mOptions = ImageLoaderHelper.setupDisplayImageOption();
		mPictureList = list;
		mViews = new ArrayList<View>();
		mProgressBar = progressBar;
		initiateViews();
	}
	
	public ArrayList<View> getViews() {
		return mViews;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mViews == null) return 0;
		return mViews.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
//		return false;
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "----------------destroyItem----position = "+position);
		ViewPager viewPager = (ViewPager) container;
		viewPager.removeView(mViews.get(position));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
//		NextDayPictureInfo pictureInfo = mPictureList.get(Consts.NEXTDAY_PICTURE_SIZE - 1 - position);
		NextDayPictureInfo pictureInfo = mPictureList.get(mPictureList.size() - 1 - position);
		String pictureUrl = null;
		if (pictureInfo.getPictureComment() == null) {
			NextDayDbControl mNextDayDbControl = new NextDayDbControl(mContext);
			NextDayPictureInfo pictureInfoDb = mNextDayDbControl.queryPictureInfoByName(pictureInfo.getPictureTime());
			if (pictureInfoDb != null && pictureInfoDb.getPictureName() != null) {
				pictureInfo.setPictureName(pictureInfoDb.getPictureName());
				pictureInfo.setPictureDimension(pictureInfoDb.getPictureDimension());
//				pictureInfo.setPictureThumnailUrl(pictureInfoDb.getPictureThumnailUrl());
//				pictureInfo.setPictureOriginalUrl(pictureInfoDb.getPictureOriginalUrl());
				pictureUrl = pictureInfoDb.getPictureOriginalUrl();
				pictureInfo.setPictureCommentCity(pictureInfoDb.getPictureCommentCity());
				pictureInfo.setPictureComment(pictureInfoDb.getPictureComment());
				pictureInfo.setPictureTimeColor(pictureInfoDb.getPictureTimeColor());
				pictureInfo.setPictureStatusColor(pictureInfoDb.getPictureStatusColor());
			}
			mNextDayDbControl.close();
		}
		
		String initPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator + pictureInfo.getPictureTime() + ".jpg";
    	String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
    	String previewPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + "_comment" + ".jpg";
    	
		File mFile;
		String operation = Consts.NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD;
    	
		if (pictureInfo.getPictureOriginalUrl() != null && pictureInfo.getPictureComment() != null) {
			operation = Consts.NEXTDAY_PICTURE_LOADTYPE_NONE;
			
		} else if (pictureInfo.getPictureOriginalUrl() != null && pictureInfo.getPictureComment() == null) {
			operation = Consts.NEXTDAY_PICTURE_LOADTYPE_INFO;
			
		} else if (pictureInfo.getPictureOriginalUrl() == null && pictureInfo.getPictureComment() != null) {
			pictureInfo.setPictureThumnailUrl(pictureUrl);
			pictureInfo.setPictureOriginalUrl(pictureUrl);
			operation = Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE;
		}
    	
    	boolean isMobileData = ((ThemeManagerApplication) mContext.getApplicationContext()).getIsMobileData();
    	if (CommonUtil.NetWorkType.MOBILE_ONLY == CommonUtil.getNetWorkType(mContext) && !isMobileData) {
			/*if (!isMobileData) {
				operation = Consts.NEXTDAY_PICTURE_LOADTYPE_NONE;
			}*/
    		mViews.get(position).setBackgroundResource(R.drawable.nextday_no_network);
    		
		} else {
	    	if (!Consts.NEXTDAY_PICTURE_LOADTYPE_NONE.equals(operation)) {
				mProgressBar.setVisibility(View.VISIBLE);
			}
			
			Log.d("Wallpaper_DEBUG", "the operation = "+operation);
			String width = String.valueOf(((ThemeManagerApplication) mContext.getApplicationContext()).getDisplayWidth());
	    	String height = String.valueOf(((ThemeManagerApplication) mContext.getApplicationContext()).getDisplayHeight());
			NextDayLoadAndDisplayTask loadAndDisplayTask = new NextDayLoadAndDisplayTask(mHandler);
			loadAndDisplayTask.execute(mContext, pictureInfo, width + "*" + height, operation, (ImageView) mViews.get(position), mOptions, mProgressBar);
		}
		
		ViewPager viewPager = ((ViewPager) container);
        viewPager.addView(mViews.get(position));
        return mViews.get(position);
		
	}
	
	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return super.getItemPosition(object);
//		return POSITION_NONE;
	}

	public void initiateViews() {
		for (int i = 0; i < mPictureList.size(); i++) {
			ImageView tempImageView = new ImageView(mContext);
			if (CommonUtil.NetWorkType.NO_NET == CommonUtil.getNetWorkType(mContext)) {
				tempImageView.setBackgroundResource(R.drawable.nextday_no_network);
			}
			mViews.add(tempImageView);
		}
	}

}
