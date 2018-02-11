package com.aurora.commemoration.share;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.calendar.R;
import com.android.calendar.wxapi.WXEntryActivity;
import com.aurora.commemoration.model.RememberDayInfo;
import com.aurora.commemoration.widget.InfoView;
import com.gionee.calendar.view.Log;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RememberDayFragment extends Fragment {

    private String mImageUrl;
    private ImageView mImageView;
    private InfoView content;
    private RememberDayInfo info;

    public RememberDayFragment(String imageUrl, RememberDayInfo info) {
        this.mImageUrl = imageUrl;
        this.info = info;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void disPic() {
        if (mImageUrl == null) return;

        if (mImageUrl.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
            mImageView.setImageURI(Uri.parse(mImageUrl));
        } else {
            String url = mImageUrl.substring(7);
            BitmapFactory ft = new BitmapFactory();
            Bitmap bt = ft.decodeFile(url);
            mImageView.setImageBitmap(bt);
        }
    }

    public void updatePic(int url) {
        mImageView.setImageResource(url);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FrameLayout fl = (FrameLayout) inflater.inflate(R.layout.rememberday_preview_item, null);
        //content = (InfoView) fl.findViewById(R.id.content);
        mImageView = (ImageView) fl.findViewById(R.id.pic_view_image);
        //content.initData(false, info);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlToolViews();
            }
        });

        return fl;
    }

    private void controlToolViews() {
        Log.e("JOY: onClick controlToolViews");
        FragmentActivity activity = getActivity();
        ((WXEntryActivity) activity).controlTopViews(WXEntryActivity.NONE_DOWN_FLAG);
    }

    @Override
    public void onDestroyView() {
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