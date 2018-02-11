package com.android.auroramusic.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.music.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xiami.sdk.entities.OnlineAlbum;
import com.xiami.sdk.entities.OnlineCollect;

public class AuroraNetPlaylistAdapter extends BaseAdapter {
	private List mList;
	private Activity mContext;
	private int mIconWidth;
	private int mIconHeight;
	private static final int PAGE_SIZE = 0;
	private static volatile boolean isScrolling = false;
	private String IMAGE_CACHE_DIR = "NetAlbum";
	private DisplayImageOptions mOptions;
	private int mType = -1;

	static class ViewHolder {
		TextView line1;// 专辑名
		TextView line2;// 多少歌
	    TextView line3;// 发行时间
		ImageView play_ic;// 播放按钮
		ImageView icon;
	}

	public AuroraNetPlaylistAdapter(Activity context, List list) {
		mContext = context;
		mList = list;
		mIconWidth = (int) mContext.getResources().getDimension(
				R.dimen.aurora_album_item_height);
		mIconHeight = (int) mContext.getResources().getDimension(
				R.dimen.aurora_album_item_height);
		initImageCacheParams();
	}

	private void initImageCacheParams() {
		mOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true)
				.showImageOnLoading(R.drawable.aurora_online_recommend_default)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.displayer(new SimpleBitmapDisplayer()).build();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return (mList == null) ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder vh;
		final String pic;
		if (convertView == null) {
			if(mType==1||mType==2){
				convertView = convertView = LayoutInflater.from(mContext).inflate(R.layout.aurora_search_album, null);
			}else{
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.aurora_nettrack_listitem, null);
			}
			vh = new ViewHolder();
			vh.line1 = (TextView) convertView.findViewById(R.id.album_name);
			vh.line2 = (TextView) convertView.findViewById(R.id.album_numtrack);
			vh.line3 = (TextView) convertView.findViewById(R.id.album_release_date);
			vh.play_ic = (ImageView) convertView.findViewById(R.id.play_now);
			vh.icon = (ImageView) convertView.findViewById(R.id.album_art);
			convertView.setTag(vh);
		} else
			vh = (ViewHolder) convertView.getTag();

		if (mList.get(position) instanceof OnlineCollect) {
			final OnlineCollect item = (OnlineCollect) mList.get(position);
			vh.line2.setText(mContext.getResources().getString(
					R.string.number_track2, item.getSongCount()));
			if (TextUtils.isEmpty(item.getCollectName()))
				vh.line1.setText(mContext.getResources().getString(
						R.string.unknown_album_name));
			else
				vh.line1.setText(item.getCollectName());
			/*
			 * if (!TextUtils.isEmpty(item.getDescription()))
			 * vh.line3.setText(item.getDescription().trim()); else {
			 * vh.line3.setText(R.string.unknown); }
			 */
			pic = item.getImageUrl(220);
		} else {
			final OnlineAlbum item = (OnlineAlbum) mList.get(position);
			vh.line2.setText(mContext.getResources().getString(
					R.string.number_track2, item.getSongCount()));
			if (TextUtils.isEmpty(item.getAlbumName()))
				vh.line1.setText(mContext.getResources().getString(
						R.string.unknown_album_name));
			else
				vh.line1.setText(item.getAlbumName());
			if (mType == 1 && vh.line3!=null) {
				if (item.getPublishTime() > 0) {
					Date date = new Date(item.getPublishTime() * 1000);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String time = sdf.format(date);
					vh.line3.setText(mContext.getResources().getString(
							R.string.release_date_of_album, time));
				} else {
					vh.line3.setText(R.string.unknown);
				}
			}else if(mType == 2 && vh.line3!=null){
					if (item.getArtistName() != null) {
						vh.line3.setText(item.getArtistName());
					} else {
						vh.line3.setText(R.string.unknown);
					}
			}
			pic = item.getImageUrl(220);
		}
		if (!TextUtils.isEmpty(pic) && !pic.equals(vh.icon.getTag())) {
			// mImageFetcher.loadImageAndSize(pic, vh.icon, 100, mIconWidth,
			// mIconHeight);
			ImageLoader.getInstance().displayImage(pic, vh.icon, mOptions,
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String imageUri, View view) {
							// TODO Auto-generated method stub
							vh.icon.setTag(pic);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onLoadingCancelled(String imageUri,
								View view) {
							// TODO Auto-generated method stub

						}
					});
		}
		return convertView;
	}

	public void setScrolling(boolean flag) {
		isScrolling = flag;
	}

	public void setType(int type) {
		mType = type;
	}
}