package com.aurora.change.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aurora.thememanager.R;
import com.aurora.change.data.WallpaperValue;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.imagecache.ImageWorker;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;

public class WallpaperPreviewAdapter extends PagerAdapter {

    private static final String TAG = "WallpaperPreviewAdapter";
    private static CommonLog log = LogFactory.createLog(TAG);
    private ViewPager mViewPager;
    private Context mContext;
    private List<PictureInfo> mPictureInfos;
    private List<View> mViews;
    private List<String> mExtraList;
    private List<String> mExtraListForCut;
    private LayoutInflater mInflater;
    private String mWallpaperType;
//    private ImageLoader mImageLoader;
    
    private ImageResizer mImageResizer;

    public WallpaperPreviewAdapter(Context context, List<PictureInfo> pictureInfos, String type) {
        mContext = context;
        mWallpaperType = type;
        mPictureInfos = pictureInfos;
//        mImageLoader = new ImageLoader(context);
//        mImageLoader.registerCallback(mLoaderCallback);
//        mInflater = LayoutInflater.from(context);
        mViews = initLayout(pictureInfos);
    }

    public WallpaperPreviewAdapter(Context context, String type, List<String> extraList, List<String> extraListForCut) {
    	mContext = context;
    	mExtraList = extraList;
    	mExtraListForCut = extraListForCut;
        mWallpaperType = type;
        mViews = initViews(extraList, extraListForCut);
    }
    
    @Override
    public int getCount() {
        if (mViews != null) {
            return mViews.size();
        }
        return 0;
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ViewPager viewPager = ((ViewPager) container);
        viewPager.addView(mViews.get(position));
        refreshData(position);
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager) container;
        ImageView mImageView = (ImageView) mViews.get(position).findViewById(R.id.wallpaper_preview_item_background);
        if (mImageView != null) {
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
        viewPager.removeView(mViews.get(position));
    }

//    @Override
//    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        log.d("data=setPrimaryItem");
//        refreshData(position);
//    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    public List<View> initLayout(List<PictureInfo> infos) {
        int size = infos.size();
//        log.d("initLayout:size=" + size);
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < size; i++) {
            View view = View.inflate(mContext, R.layout.wallpaper_preview_item, null);
//            if (Consts.WALLPAPER_DESKTOP_TYPE.equals(mWallpaperType)) {
//                ((ImageView) view.findViewById(R.id.wallpaper_preview_item_foreground))
//                        .setImageResource(R.drawable.manager_wallpaper);
//            }
            views.add(view);
        }
        return views;
    }
    
    public List<View> initViews(List<String> list, List<String> listForCut) {
        int size = Consts.LOCAL_WALLPAPERS.length;
        if (list != null) {
			size += list.size();
			size += listForCut.size();
		}
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < size; i++) {
            View view = View.inflate(mContext, R.layout.wallpaper_preview_item, null);
//            if (Consts.WALLPAPER_DESKTOP_TYPE.equals(mWallpaperType)) {
//                ((ImageView) view.findViewById(R.id.wallpaper_preview_item_foreground)).setVisibility(View.GONE);
//                        /*.setImageResource(R.drawable.manager_wallpaper);*/
//            }
            views.add(view);
        }
        return views;
    }

    public void refreshData(int position) {
//        log.d("refresData");
        View view = mViews.get(position);
            ImageView iv = (ImageView) view.findViewById(R.id.wallpaper_preview_item_background);           
            if (Consts.WALLPAPER_DESKTOP_TYPE.equals(mWallpaperType)) {
            	/*Log.d("liugj", "------null------"+(mPicInfos.get(position).get() == null));
            	BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), mPicInfos.get(position).get());
				iv.setImageDrawable(bd);*/
            	int len = Consts.LOCAL_WALLPAPERS.length;
            	if (position < len) {
            		mImageResizer.loadImage(Consts.DEFAULT_SYSTEM_DESKTOP_WALLPAPER_PATH + Consts.LOCAL_WALLPAPERS[position], iv);
				}else if(position - len < mExtraListForCut.size()) {
					mImageResizer.loadImage(mExtraListForCut.get(position - len), iv);
				} else {
					//mImageResizer.loadImage(getSDPath() + mExtraList.get(position - len), iv);
					mImageResizer.loadImage(mExtraList.get(position - len - mExtraListForCut.size()), iv);
				}
			}else {
				//mImageLoader.DisplayImage(mPictureInfos.get(position).getBigIcon() + "", iv, isBusy);
				mImageResizer.loadImage(mPictureInfos.get(position).getBigIcon(), iv);
			}
//        }
    }
    
    private String getSDPath(){ 
        File sdDir = null; 
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
        if(sdCardExist)
        {                               
          sdDir = Environment.getExternalStorageDirectory();//获取跟目录 
        }
        if (sdDir != null) {
        	return sdDir.toString();
		}
        return null;
    }
    
    class ViewHolder {
        ImageView mBackground;
        ImageView mForeground;
    }

    public void clearData(){
//        mImageLoader.clearCache();
        if (mViews != null) {
            mViews.clear();
            mViews = null;
        }
        if (mPictureInfos != null) {
            mPictureInfos.clear();
            mPictureInfos = null;
        }
        if (mImageResizer != null) {
            mImageResizer.clearCache();
            //mImageResizer.clearMemoryCache();
            mImageResizer.closeCache();
        }

    }
    
    public void onPause(){
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(true);
            mImageResizer.flushCache();
        }
    }
    
    public void onResume(){
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(false);
            notifyDataSetChanged();
        }
    }
    
    public void setImageResizer(ImageResizer imageResizer){
        mImageResizer = imageResizer;
    }
}
