package com.android.auroramusic.adapter;


import java.util.ArrayList;

import com.android.auroramusic.util.AuroraIListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.LogUtil;
import com.android.music.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class AuroraPlayerListViewAdapter<T extends AuroraIListItem> extends BaseAdapter{
	
	private static final String TAG = "AuroraPlayerListViewAdapter";
	private ArrayList<T> mList = null;
	private LayoutInflater  mInflater;
	
	private static String mArtistName;
	private static String mAlbumName;
	private int mPlayingPosition = -1;
	private int mOldPosition = -1;
	private Context mContext;
	
	
	public AuroraPlayerListViewAdapter(Context context, ArrayList<T> list) {
		AuroraMusicUtil.justTest();
		if (list == null || context == null) {
			LogUtil.e(TAG, " ---- AuroraPlayerListViewAdapter create list fail");
			return;
		}
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		mList = list;
		mArtistName = context.getString(R.string.unknown);
		mAlbumName = context.getString(R.string.unknown_album_name);
	}
	
	public void setPlayingPosition(int position) {
		mOldPosition = mPlayingPosition;
		mPlayingPosition = position;
	}
	
	public int getCurrentPlayPosition() {
		return mPlayingPosition;
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		if (mList == null) {
			return null;
		}
		
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (mList == null) {
			return null;
		}
		
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.aurora_player_listitem, null);
			holder = new ViewHolder();
			//holder.songImage = (ImageView)convertView.findViewById(R.id.listitem_img);
			holder.playImg = (ImageView)convertView.findViewById(R.id.listitem_playing);
			holder.songName = (TextView)convertView.findViewById(R.id.listitem_title);
			holder.songAlbum = (TextView)convertView.findViewById(R.id.listitem_album);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		//Log.i(TAG, "zll ---- getView mPlayingPosition:"+mPlayingPosition+",position:"+position);
		if (mPlayingPosition == position) {
			holder.playImg.setVisibility(View.VISIBLE);
		} else {
			holder.playImg.setVisibility(View.INVISIBLE);
		}
		
		T iteminfo = mList.get(position);
		if (iteminfo != null) {
			try {
				holder.songName.setText(iteminfo.getTitle());
				StringBuffer tBuffer = new StringBuffer();
				
				String artiststr = iteminfo.getArtistName();
				if (MediaStore.UNKNOWN_STRING.equals(artiststr) || artiststr == null) {
					artiststr = mArtistName;
                }
				
				String albumstr = iteminfo.getAlbumName();
				albumstr = AuroraMusicUtil.doAlbumName(iteminfo.getFilePath(), albumstr);
				if (MediaStore.UNKNOWN_STRING.equals(albumstr) || albumstr == null) {
					albumstr = mAlbumName;
                }
				tBuffer.append(artiststr);
				tBuffer.append("·");
				tBuffer.append(albumstr);
				holder.songAlbum.setText(tBuffer.toString());
			} catch (Exception e) {
				Log.i(TAG, "zll ---- getView set iteminfo fail");
			}
		}
		
		if(!iteminfo.isAvailable()){
			holder.songName.setTextColor(Color.parseColor("#b3b3b3"));
			holder.songAlbum.setTextColor(Color.parseColor("#b3b3b3"));
			
		}else {
			holder.songName.setTextColor(Color.parseColor("#ffffff"));
			holder.songAlbum.setTextColor(Color.parseColor("#ffffff"));
		}
		
		return convertView;
	}

	public static class ViewHolder{
		//ImageView 	songImage;
		ImageView	playImg;
		TextView	songName;
		TextView 	songAlbum;
	}
}
