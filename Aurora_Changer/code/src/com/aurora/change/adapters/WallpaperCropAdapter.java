package com.aurora.change.adapters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aurora.change.R;
import com.aurora.change.activities.WallpaperCropActivity;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.imagecache.ImageWorker;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.view.CropImageView;
//import com.aurora.change.view.CropImageView.ActionBarCallBack;
//import com.aurora.filemanager.FileInfo;

//Aurora liugj 2014-07-17 modified for文管提供图片选择接口
public class WallpaperCropAdapter extends PagerAdapter {

    private static final String TAG = "WallpaperCropAdapter";
    private static CommonLog log = LogFactory.createLog(TAG);
    private Context mContext;
    //private List<FileInfo> mFileInfos;
    private List<String> mImageList;
    private List<View> mViews;
    //private ViewPager mPager;
    private ImageResizer mImageResizer;

    public WallpaperCropAdapter(Context context, List<String> imageList) {
        mContext = context;
        if (imageList == null) {
			mImageList = new ArrayList<String>();
		}else {
			mImageList = imageList;
		}
        initLayout();
    }

    @Override
    public int getCount() {
        if (null != mImageList) {
            return mImageList.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewPager viewPager = ((ViewPager) container);
        viewPager.addView(mViews.get(position));
        refreshData(position);
        return mViews.get(position);
    }

//    @Override
//    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        log.d("data=setPrimaryItem");
//    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager) container;
        CropImageView iv = (CropImageView) mViews.get(position).findViewById(R.id.wallpaper_crop_item);
        if (iv != null) {
            ImageWorker.cancelWork(iv);
            iv.setImageDrawable(null);
        }
        viewPager.removeView(mViews.get(position));
    }

    private void initLayout() {
        int size = mImageList.size();
        mViews = new ArrayList<View>(size);
        for (int i = 0; i < size; i++) {
            View view = View.inflate(mContext, R.layout.wallpaper_crop_item, null);
            CropImageView iv = (CropImageView) view.findViewById(R.id.wallpaper_crop_item);
//            iv.setDrawable(mContext.getResources().getDrawable(R.drawable.preview_loading));
            //iv.setActionBarCallBack(mActionBarCallBack);
//            iv.setBackgroundColor(Color.BLACK);
            mViews.add(view);
        }
    }

    /*public void setViewPager(ViewPager viewPager) {
        mPager = viewPager;
    }*/

    public void setImageResizer(ImageResizer imageResizer) {
        mImageResizer = imageResizer;
    }

    /*ActionBarCallBack mActionBarCallBack = new ActionBarCallBack() {

        @Override
        public void toggleActionBarVisibility() {
            //toggleActionBarVisibilitys();
//            toggleActionBar();
        }
    };*/

    /*private void toggleActionBarVisibilitys() {
        final int vis = mPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            Log.d("count", "toggleActionBarVisibility false");
        } else {
            Log.d("count", "toggleActionBarVisibility true");
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }*/

    public void refreshData(int position) {
        if (mViews == null || mViews.size() <= 0) {
            return;
        }
        View view = mViews.get(position);
        CropImageView iv = (CropImageView) view.findViewById(R.id.wallpaper_crop_item);
//        log.d("cropImage:refreshData=" + position + ",path=" + mImageList.get(position));
        mImageResizer.loadImage(mImageList.get(position), iv, position);
        /*final BitmapFactory.Options options = new BitmapFactory.Options();
        InputStream is = null;
        try {
            is = mContext.getContentResolver().openInputStream(Uri.fromFile(new File(String.valueOf(mImageList.get(position)))));
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(String.valueOf(mFileInfos.get(position).filePath), options);
            options.inPreferredConfig = Bitmap.Config.RGB_565; 
//            options.inJustDecodeBounds = false;
            Bitmap bitmap =  BitmapFactory.decodeStream(is, null, options);
            iv.setImageDrawable(new BitmapDrawable(bitmap));
        } catch (FileNotFoundException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public List<View> getItemViews(){
        return mViews;
    }
    
    public void onResume() {
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(false);
            notifyDataSetChanged();
        }
        
        //M:shigq fix bug15327 start
        //It may be no safe, if this problem happens again, please check here
        refreshData(((WallpaperCropActivity) mContext).getCurrentItem());
        //M:shigq fix bug15327 end
    }

    public void onPause() {
    	Log.d("Wallpaper_DEBUG", "WallpaperCropAdapter-----------------onPause-------------------");
    	//M:shigq fix bug15327 start
    	//It may be no safe, if this problem happens again, please check here
        /*if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(true);
            mImageResizer.flushCache();
        }*/
    	//M:shigq fix bug15327 end
    }

    public void clearData() {
        if (mViews != null) {
            mViews.clear();
            mViews = null;
        }
        if (mImageList != null) {
        	mImageList.clear();
        	mImageList = null;
        }
        if (mImageResizer != null) {
            // mImageResizer.clearCache();
            // mImageResizer.clearMemoryCache();
            mImageResizer.closeCache();
        }
    }
}
