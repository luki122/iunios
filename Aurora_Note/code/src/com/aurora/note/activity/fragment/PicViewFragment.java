package com.aurora.note.activity.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.aurora.note.R;
import com.aurora.note.activity.picbrowser.PicturePreviewActivity;
import com.aurora.note.activity.picbrowser.PictureViewActivity;
import com.aurora.note.util.ToastUtil;
import com.aurora.note.wxapi.WXEntryActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public class PicViewFragment extends Fragment {

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
        if (activity instanceof PictureViewActivity) {
            ((PictureViewActivity)activity).controlToolViews();
        } else if (activity instanceof PicturePreviewActivity) {
            ((PicturePreviewActivity)activity).controlToolViews();
        } else if (activity instanceof WXEntryActivity) {
            ((WXEntryActivity)activity).controlToolViews();
        }
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
        if (activity instanceof PictureViewActivity) {
            return ((PictureViewActivity)activity).imageLoader;
        } else if (activity instanceof PicturePreviewActivity) {
            return ((PicturePreviewActivity)activity).imageLoader;
        } else if (activity instanceof WXEntryActivity) {
        	return ((WXEntryActivity)activity).imageLoader;
        }

        return null;
    }

    private DisplayImageOptions getImageOptions() {
        FragmentActivity activity = getActivity();
        if (activity instanceof PictureViewActivity) {
            return ((PictureViewActivity)activity).options;
        } else if (activity instanceof PicturePreviewActivity) {
            return ((PicturePreviewActivity)activity).options;
        } else if (activity instanceof WXEntryActivity) {
            return ((WXEntryActivity)activity).options;
        }

        return null;
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
				new SimpleImageLoadingListener() {
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