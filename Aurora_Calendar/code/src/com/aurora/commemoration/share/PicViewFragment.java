package com.aurora.commemoration.share;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.calendar.R;
import com.aurora.calendar.util.FileLog;
import com.aurora.calendar.util.Globals;
import com.aurora.calendar.util.Log;
import com.aurora.calendar.util.ToastUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public class PicViewFragment extends Fragment {
	private static final String TAG ="PicViewFragment";
	private String mImageUrl;
	private ImageView mImageView;
	private ProgressBar progressBar;
	private PhotoViewAttacher mAttacher;
	private ImageView defaultImage;
	private boolean mIsCurrent = false; // true表示当前用户点击查看的图片

	public static PicViewFragment newInstance(String imageUrl) {
		final PicViewFragment f = new PicViewFragment();

		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		f.setArguments(args);

		return f;
	}

	public static PicViewFragment newInstance(String imageUrl, boolean isCurrent) {
        final PicViewFragment f = new PicViewFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        f.setArguments(args);
        f.mIsCurrent = isCurrent;

        return f;
    }
    @SuppressLint("SimpleDateFormat")
    private String getPictureFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IUNINote'_yyyyMMdd_HHmmss");
        return dateFormat.format(date);
    }
	public String savePic()
	{
		int width = mImageView.getWidth();
		int height = mImageView.getHeight();

		BitmapDrawable bd = (BitmapDrawable) mImageView.getDrawable();
		Bitmap bitmap = bd.getBitmap();
		
		Bitmap img1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mAttacher.getDisplayMatrix(), true);// 得到放大图片
		
		width = img1.getWidth();
		height = img1.getHeight();
		
		
		Display d = getActivity().getWindowManager().getDefaultDisplay();
	
		if(img1.getWidth()>d.getWidth())
		{
			width = d.getWidth();
		}
		else
		{
			width = img1.getWidth();
		}
		if(img1.getHeight()>d.getHeight())
		{
			height = d.getHeight();
		}
		else
		{
			height = img1.getHeight();
		}
		
		Bitmap img2 = Bitmap.createBitmap(img1, -(int)mAttacher.getDisplayRect().left>0?-(int)mAttacher.getDisplayRect().left:0, -(int)mAttacher.getDisplayRect().top>0?-(int)mAttacher.getDisplayRect().top:0, width, height);// 得到放大图片
		
		width = img2.getWidth();
		height = img2.getHeight();
		boolean bl = false;
	    if (!Globals.PIC_DIR.exists()) {
	    	bl = Globals.PIC_DIR.mkdirs();
        }
	    final boolean usePngFormat = true;
        String extName = usePngFormat ? ".png": ".jpg";
        String fname = Globals.PIC_DIR.getAbsolutePath() + "/" + getPictureFileName() + extName;
        File bitmapFile = new File(fname);
        if (img2 != null) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(bitmapFile);
                if (usePngFormat) {
                	img2.compress(Bitmap.CompressFormat.PNG, 100, out);
                } else {
                	img2.compress(Bitmap.CompressFormat.JPEG, 80, out);
                }
//                testImageFile(bitmap, Globals.PIC_DIR.getAbsolutePath() + "/");
                return fname;
            } catch (Exception e) {
               
                FileLog.e(TAG, e.getMessage());
            } catch (Throwable t) {

                FileLog.e(TAG, t.getMessage());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        FileLog.e(TAG, e.getMessage());
                    }
                }
          
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        } else {
            Log.i(TAG, "doScreenShot bitmap is NULL!");
        }
		return fname;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		RelativeLayout fl = (RelativeLayout) inflater.inflate(R.layout.picture_view_item, null);
		mImageView = (ImageView) fl.findViewById(R.id.pic_view_image);
		mAttacher = new PhotoViewAttacher(mImageView);
		
		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				controlToolViews();
			}
		});

		progressBar = (ProgressBar) fl.findViewById(R.id.loading);
		defaultImage = (ImageView) fl.findViewById(R.id.default_image);
		// setDefaultImageSize(fl);

		return fl;
	}

	private void controlToolViews() {
	    FragmentActivity activity = getActivity();
       
        ((PictureViewActivity)activity).controlToolViews();
     
	}

	/**
	 * author lwx192606 2014-2-25 设置默认图的位置和大小
	 * 
	 * @param parentView
	 *            return void
	 */
	/*private ImageView setDefaultImageSize(RelativeLayout parentView) {
	    final int screenWidth = parentView.getResources().getDisplayMetrics().widthPixels;
		ImageView originalDefaultView = (ImageView) parentView.findViewById(R.id.default_image);
		RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(screenWidth * 1 / 2, screenWidth * 1 / 2);
		rl.addRule(RelativeLayout.CENTER_IN_PARENT);
		originalDefaultView.setLayoutParams(rl);
		return originalDefaultView;
	}*/

    @Override
    public void onDestroyView() {
        if (mAttacher != null) {
            mAttacher.cleanup();
            mAttacher = null;
        }
        super.onDestroyView();
    }

    private ImageLoader getImageLoader() {
        FragmentActivity activity = getActivity();
      
        return ((PictureViewActivity)activity).imageLoader;
    }

    private DisplayImageOptions getImageOptions() {
        FragmentActivity activity = getActivity();
        
        return ((PictureViewActivity)activity).options;
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mImageUrl == null) return;

		ImageLoader loader = getImageLoader();
		DisplayImageOptions options = getImageOptions();
		if (loader == null || options == null) {
		    return;
		}

		loader.displayImage(mImageUrl, mImageView, options,
				 new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
//						defaultImage.setVisibility(View.VISIBLE);
						progressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						int msgId = -1;
						switch (failReason.getType()) {
						case IO_ERROR:
							msgId = R.string.load_picture_error_io;
							break;
						case DECODING_ERROR:
						    msgId = R.string.load_picture_error_decoding;
							break;
						case NETWORK_DENIED:
						    msgId = R.string.load_picture_error_network;
							break;
						case OUT_OF_MEMORY:
						    msgId = R.string.load_picture_error_out_of_memory;
							break;
						case UNKNOWN:
						    msgId = R.string.load_picture_error_unknow;
							break;
						}
						if (msgId != -1 && mIsCurrent) {
						    ToastUtil.shortToast(msgId);
						}
						defaultImage.setVisibility(View.VISIBLE);
						defaultImage.setImageResource(R.drawable.forum_loading_failed);
						progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						defaultImage.setVisibility(View.GONE);
						progressBar.setVisibility(View.GONE);
//						rotateBitmapIfNeeded(imageUri, loadedImage);
						if (mAttacher != null) {
						    mAttacher.update();
						}
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						// TODO Auto-generated method stub
						
					}

//					private void rotateBitmapIfNeeded(String imageUri, Bitmap loadedImage) {
//					    int degree = BitmapUtil.readPictureDegree(imageUri);
//					    if (degree > 0) {
//					        loadedImage = BitmapUtil.rotateBitmap(loadedImage, degree);
//					        mImageView.setImageBitmap(loadedImage);
//					    }
//					}
				});
	}

}