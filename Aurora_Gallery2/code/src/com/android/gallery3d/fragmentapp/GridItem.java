package com.android.gallery3d.fragmentapp;


import com.android.gallery3d.fragmentutil.ImageResizer;
import com.android.gallery3d.fragmentutil.RecyclingImageView;
import com.android.gallery3d.R;
import android.content.Context;
import android.renderscript.Sampler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.gallery3d.fragmentdata.GalleryItem;
import com.android.gallery3d.fragmentdata.IGristItem;



public class GridItem <T extends IGristItem>  extends RelativeLayout implements Checkable {

    private Context mContext;
    private boolean mChecked;
    private RecyclingImageView mImgView = null;
    private ImageView mSecletView = null;
    private ImageView mVideoView = null;
    private static int m_ClunmNums = 0;
    private static boolean m_bVisible = false;

    public GridItem(Context context, ImageResizer mResizer, T item) {
    	this(context, null, 0);
    	SetRecyclingImageView(mResizer, item.getUri(), item.getType());
    }
    
    public GridItem(Context context) {
        this(context, null, 0);
    }

    public GridItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.grid_item, this);
        mImgView = (RecyclingImageView) findViewById(R.id.img_view);
        mSecletView = (ImageView) findViewById(R.id.select);
        mVideoView = (ImageView)findViewById(R.id.video_view);
    }

	@Override
    public void setChecked(boolean checked) {
        // TODO Auto-generated method stub
        mChecked = checked;
        //Log.i("dasd", "zll ---- setChecked checked:"+checked);
        //mSecletView.setVisibility(checked ? View.VISIBLE : View.GONE);
        if (mSecletView == null) {
			return;
		}
        
        if (checked) {
        	mSecletView.setImageResource(R.drawable.check);
		}else {
			mSecletView.setImageResource(R.drawable.uncheck);
		}
        
        mSecletView.setVisibility(m_bVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isChecked() {
        // TODO Auto-generated method stub
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
    
    public static void setCheckImageVisible(boolean flag) {
    	m_bVisible = flag;
		return;
	}

    public void setImgResId(int resId) {
        if (mImgView != null) {
            mImgView.setBackgroundResource(resId);
        }
    }
    
    public static void SetCloumsNum(int nums) {
    	m_ClunmNums = nums;
	}

    public void SetRecyclingImageView(ImageResizer mResizer, Object data, int defStyle) {
    	//<!-- Iuni <lory><2013-12-11> modify start-->
    	//Log.i("zll", "zll ---- m_ClunmNums:"+m_ClunmNums);
    	if (defStyle == 2) {//video
        	if (m_ClunmNums == 6) {
        		mVideoView.setImageResource(R.drawable.ic_gallery_play_six);
    		} else if (m_ClunmNums == 2) {
    			mVideoView.setImageResource(R.drawable.ic_gallery_play_two);
    		} else {
    			mVideoView.setImageResource(R.drawable.ic_gallery_play);
			}
        	mVideoView.setVisibility(View.VISIBLE);
		}
    	else {
    		mVideoView.setVisibility(View.GONE);
		}

    	//<!-- Iuni <lory><2013-12-11> modify end-->
    	
    	if ((mImgView != null) && (mResizer != null)) {
    		mResizer.loadImage(data.toString(), mImgView);
		}
		return;
	}
    
    public void SetRecyclingImageViewByObject(ImageResizer mResizer, Object data, int defStyle) {
    	//Log.i("zll", "zll ---- m_ClunmNums:"+m_ClunmNums);
    	if (defStyle == 2) {//video
        	if (m_ClunmNums == 6) {
        		mVideoView.setImageResource(R.drawable.ic_gallery_play_six);
    		} else if (m_ClunmNums == 2) {
    			mVideoView.setImageResource(R.drawable.ic_gallery_play_two);
    		} else {
    			mVideoView.setImageResource(R.drawable.ic_gallery_play);
			}
        	mVideoView.setVisibility(View.VISIBLE);
		}
    	else {
    		mVideoView.setVisibility(View.GONE);
		}
    	
    	if ((mImgView != null) && (mResizer != null)) {
    		mResizer.loadImage(data, mImgView);
		}
		return;
	}
    
    public void SetRecyclingImageViewByUri(ImageResizer mResizer, Object data, int defStyle) {
    	if (defStyle == 2) {//video
    		//Log.i("zll", "zll ---- m_ClunmNums:"+m_ClunmNums);
    		if (m_ClunmNums == 6) {
        		mVideoView.setImageResource(R.drawable.ic_gallery_play_six);
    		} else if (m_ClunmNums == 2) {
    			mVideoView.setImageResource(R.drawable.ic_gallery_play_two);
    		}
        	mVideoView.setVisibility(View.VISIBLE);
		}
    	else {
    		mVideoView.setVisibility(View.GONE);
		}
    	
    	if ((mImgView != null) && (mResizer != null)) {
    		mResizer.loadImage(data.toString(), mImgView);
		}
		return;
	}
}
