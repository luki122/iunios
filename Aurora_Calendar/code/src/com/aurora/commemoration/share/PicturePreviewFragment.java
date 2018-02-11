package com.aurora.commemoration.share;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.calendar.R;
import com.android.calendar.wxapi.WXEntryActivity;
import com.aurora.calendar.util.ToastUtil;
import com.gionee.calendar.view.Log;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PicturePreviewFragment extends Fragment {

    private String mImageUrl;
    private ImageView mImageView;
    private ProgressBar progressBar;
    private PhotoViewAttacher mAttacher;
    private ImageView defaultImage;
    private boolean mIsCurrent = false; // true表示当前用户点击查看的图片

    public static PicturePreviewFragment newInstance(String imageUrl) {
        final PicturePreviewFragment f = new PicturePreviewFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        f.setArguments(args);

        return f;
    }

    public static PicturePreviewFragment newInstance(String imageUrl, boolean isCurrent) {
        final PicturePreviewFragment f = new PicturePreviewFragment();

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

    private void disPic() {
        if (mImageUrl == null) return;

        ImageLoader loader = getImageLoader();
        DisplayImageOptions options = getImageOptions();
        if (loader == null || options == null) {
            return;
        }

        if (mImageUrl.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
            mImageView.setImageURI(Uri.parse(mImageUrl));
        } else {
            loader.displayImage(mImageUrl, mImageView, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
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
                    if (mAttacher != null) {
                        mAttacher.update();
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    // TODO Auto-generated method stub

                }

            });

        }
    }

    public void updatePic(int url) {
        mImageView.setImageResource(url);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout fl = (RelativeLayout) inflater.inflate(R.layout.picture_preview_item, null);
        mImageView = (ImageView) fl.findViewById(R.id.pic_view_image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlToolViews();
            }
        });

        progressBar = (ProgressBar) fl.findViewById(R.id.loading);
        defaultImage = (ImageView) fl.findViewById(R.id.default_image);

        return fl;
    }

    private void controlToolViews() {
        Log.e("JOY: onClick controlToolViews");
        FragmentActivity activity = getActivity();
        ((WXEntryActivity) activity).controlTopViews(WXEntryActivity.NONE_DOWN_FLAG);
    }

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
        return ((WXEntryActivity) activity).imageLoader;
    }

    private DisplayImageOptions getImageOptions() {
        FragmentActivity activity = getActivity();
        return ((WXEntryActivity) activity).options;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disPic();
    }

}