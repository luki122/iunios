package com.android.auroramusic.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.auroramusic.model.Playlist;
import com.android.auroramusic.util.DialogUtil;
import com.android.music.MusicUtils;
import com.android.music.R;

public class AuroraAddSongAdapter extends BaseAdapter{
	private ArrayList<Playlist> mList;
	private Context mContext;
	class ViewHold{
		ImageView playlist_icon;
		TextView playlist_title;
	}
	public AuroraAddSongAdapter(Context context,ArrayList<Playlist> list){
		mList = list;
		mContext = context;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Playlist getItem(int position) {
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
		ViewHold vh;
		if(convertView == null){
			vh = new ViewHold();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.aurora_addtoplaylist_layout, null);
			vh.playlist_icon = (ImageView)convertView.findViewById(R.id.aurora_addsong_icon);
			vh.playlist_title = (TextView)convertView.findViewById(R.id.aurora_addsong_title);
			convertView.setTag(vh);
		}else
			vh = (ViewHold)convertView.getTag();
		if(mList.get(position).mPlaylistIcon<=0)
			vh.playlist_icon.setImageResource(R.drawable.create_song_list_default_icon_normal);
		else
			vh.playlist_icon.setImageResource(mList.get(position).mPlaylistIcon);
		if(position==mList.size()-1){
			convertView.setBackgroundResource(R.drawable.aurora_corners_bg);
		}else{
			convertView.setBackgroundResource(R.drawable.aurora_playlist_item_clicked);
		}
		vh.playlist_title.setText(mList.get(position).mPlaylistName);
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				long pid = getItem(position).mPlaylistId;
				if (pid == -1) {
					DialogUtil.initCreatePlayList(mContext);
					DialogUtil.dismissDialog();
				} else {
					MusicUtils.addToPlaylist(mContext, DialogUtil.mSongList, pid,
							getItem(position).mPlaylistName);
					DialogUtil.dismissDialog();
					DialogUtil.notifyListener();
				}
			}
		});
		return convertView;
	}

}
