package com.android.auroramusic.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.xiami.music.model.RadioInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.auroramusic.util.LogUtil;
import com.android.music.R;

public class AuroraRadioAdapter extends BaseAdapter {

	private static final String TAG = "AuroraRadioAdapter";
	private List<RadioInfo> mDatas;
	private Context mContext;
	private DisplayImageOptions disoptions;
	private int mPlayingPosition = -1;
	private int[] mXinzuoIds = new int[] { R.drawable.aurora_aries,
			R.drawable.aurora_taurus, R.drawable.aurora_gemini,
			R.drawable.aurora_cancer, R.drawable.aurora_leo,
			R.drawable.aurora_virgo, R.drawable.aurora_libra,
			R.drawable.aurora_scorpio, R.drawable.aurora_sagittarius,
			R.drawable.aurora_capricorn, R.drawable.aurora_aquarius,
			R.drawable.aurora_pisces, };
	private int mType = 0;

	public AuroraRadioAdapter(Context context, List<RadioInfo> list, int type) {
		mDatas = list;
		mContext = context;
		mType = type;
		initImageCacheParams();
	}

	public void setPlayingPosition(int pos) {
		mPlayingPosition = pos;
	}

	public int getCurrentPlayPosition() {
		return mPlayingPosition;
	}

	@Override
	public int getCount() {

		return mDatas.size();
	}

	@Override
	public Object getItem(int arg0) {

		return mDatas.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {

		HoldView holdView;
		if (arg1 == null) {
			holdView = new HoldView();
			arg1 = LayoutInflater.from(mContext).inflate(
					R.layout.radio_list_item, null);
			holdView.icon = (ImageView) arg1.findViewById(R.id.id_icon);
			holdView.name = (TextView) arg1.findViewById(R.id.id_name);
			holdView.playview = (ImageView) arg1
					.findViewById(R.id.id_song_selected);
			arg1.findViewById(R.id.id_cover).setVisibility(View.VISIBLE);
			arg1.findViewById(R.id.id_icon).setVisibility(View.VISIBLE);
			arg1.setTag(holdView);
		} else {
			holdView = (HoldView) arg1.getTag();
		}
		RadioInfo info = mDatas.get(arg0);
		if (info != null) {
			holdView.name.setText(info.getName());
			if (mPlayingPosition == arg0) {
				holdView.playview.setVisibility(View.VISIBLE);
			} else {
				holdView.playview.setVisibility(View.GONE);
			}
			if (mType == 0) {
				holdView.icon.setImageResource(mXinzuoIds[arg0]);
			} else {
				if (mType == 3 && arg0 == 5) {
					holdView.icon.setImageResource(R.drawable.aurora_soothing_icon);
				} else if (mType == 3 && arg0 == 6) {
					holdView.icon.setImageResource(R.drawable.aurora_quiet_icon);
				} else if (mType == 3 && arg0 == 7) {
					holdView.icon.setImageResource(R.drawable.aurora_lonely);
				} else {
					ImageLoader.getInstance().displayImage(info.getImageUrl(),
							holdView.icon, disoptions);
				}
			}
		}

		return arg1;
	}

	class HoldView {
		ImageView icon;
		TextView name;
		ImageView playview;
	}

	private void initImageCacheParams() {
		disoptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_music_icon2)
				.showImageForEmptyUri(R.drawable.default_music_icon2)
				.showImageOnFail(R.drawable.default_music_icon2)
				.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
				.displayer(new SimpleBitmapDisplayer()).build();
	}
}
