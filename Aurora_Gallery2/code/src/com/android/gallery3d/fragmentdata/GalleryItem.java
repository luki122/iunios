package com.android.gallery3d.fragmentdata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GalleryItem implements IGristItem {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'.'MM'.'dd");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy'.'MM");
    private final static int ORDERBYDATE_STRING = 0;
    private final static int ORDERBYMONTH_STRING = 1;

    private Context mContext;
    private Long mMediaStoreId;
    private String mBitmapPath;
    private SoftReference<Bitmap> mBitmapReference;
    private Date mTakenAt;
    private int mType;
    private long compare_time;
    private int mTmg_Type;//1:pic;2:video
    private String mUri;
    private String mPath;
    private int mRotation;

    private static final int TYPE_CONTENT_PROVIDER = 0;
    private static final int TYPE_FILE             = 1;

    public GalleryItem(long mediaStoreId, int type, String uri, Long taken, String path, int rotation) {
    	mTmg_Type 	  = type;
    	mType         = TYPE_CONTENT_PROVIDER;
        mMediaStoreId = mediaStoreId;
        mUri 		  = uri;
        if (taken != null) {
        	try {
        		mTakenAt      = new Date(taken);
                compare_time  = Long.valueOf(taken);
			} catch (Exception e) {
			}
        	
		} else {
			mTakenAt      = new Date(0);
	        compare_time  = Long.valueOf(0);
		}
        
        mPath		  = path;
        mRotation	  = rotation;
    }
    
    public GalleryItem(long mediaStoreId, int type, String uri, String taken, String path, int rotation) {
    	mTmg_Type 	  = type;
    	mType         = TYPE_CONTENT_PROVIDER;
        mMediaStoreId = mediaStoreId;
        mUri 		  = uri;
        if (taken != null) {
        	try {
        		mTakenAt      = new Date(Long.valueOf(taken));
                compare_time  = Long.valueOf(taken);
			} catch (Exception e) {
			}
        	
		} else {
			mTakenAt      = new Date(Long.valueOf(0));
	        compare_time  = 0;
		}
        
        mPath		  = path;
        mRotation	  = rotation;
    }
    
    public GalleryItem(Context context, int type, String uri, String taken, String com_time) {
    	mTmg_Type 	  = type;
    	mType         = TYPE_CONTENT_PROVIDER;
        mContext      = context;
        //mMediaStoreId = mediaStoreId;
        mUri 		  = uri;
        mTakenAt      = new Date(Long.valueOf(taken));
        compare_time = Long.parseLong(com_time);
    }

    public GalleryItem(Context context, String path, String taken) {
        mType       = TYPE_FILE;
        mContext    = context;
        mBitmapPath = path;
        mTakenAt    = new Date(Long.valueOf(taken));
    }

    public Date getTakenAt() {
        return mTakenAt;
    }

    @Override
    public long getId() {
        return mMediaStoreId;
    }

    @Override
    public String getTitle(int type) {
    	if (type == ORDERBYDATE_STRING) {
    		return sdf.format(getTakenAt());
		}else {
			return sdf2.format(getTakenAt());
		}
    }

	@Override
	public long getTime() {
		return compare_time;
	}

	@Override
	public int getType() {
		return mTmg_Type;
	}

	@Override
	public String getUri() {
		return mUri;
	}

	@Override
	public String getFilePath() {
		return mPath;
	}

	@Override
	public int getRotation() {
		return mRotation;
	}
}

