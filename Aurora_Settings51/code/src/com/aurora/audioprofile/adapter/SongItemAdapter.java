package com.aurora.audioprofile.adapter;

import com.aurora.audioprofile.entity.Song;

import java.util.List;

import com.aurora.audioprofile.AuroraRingPickerActivity;
import com.android.settings.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.TextView;

public class SongItemAdapter extends BaseAdapter {
	private Context context;
	private List<Song> data;
	private int mClickedPos;
	
	
	public SongItemAdapter(Context context,List<Song> data, int mClickedPos){
		this.context=context;
		this.data=data;
		this.mClickedPos = mClickedPos;
	}
	
	
	
	public int getCount() {
		return data.size();
	}

	public void deleteItem(int position){
		data.remove(position);
		notifyDataSetChanged();
	}
	
	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}


	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder=null;
		if(convertView==null){
			viewHolder=new ViewHolder();
//			convertView=LayoutInflater.from(context).inflate(R.layout.aurora_song_listitem, null);
			if(AuroraRingPickerActivity.mIsFullScreen){
				convertView=LayoutInflater.from(context).inflate(R.layout.aurora_other_song_listitem_alarm, null);
			}else{
				convertView=LayoutInflater.from(context).inflate(R.layout.aurora_other_song_listitem, null);
			}
			
			viewHolder.tv_song_name =(TextView)convertView.findViewById(R.id.text1);		
			((CheckedTextView)viewHolder.tv_song_name).setChecked(mClickedPos == position);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		
		viewHolder.tv_song_name.setText(data.get(position).getDisplayName());
		
		
		return convertView;
	}
	
	private String changeDisplayNameToDisplay(String str){		
		
		if(!(str.charAt(0)>='A'&&str.charAt(0)<='Z')&&!(str.charAt(0)>='a'&&str.charAt(0)<='z')){
			if(str.length() > 16){
	    		str = str.substring(0,6)+"..."+str.subSequence(str.length()-8, str.length());
	    	}
		
		}else{
			if(str.length() > 23){
	    		str = str.substring(0,12)+"..."+str.subSequence(str.length()-8, str.length());
	    	}
		}
		
		return str;
	}
 
	public class ViewHolder{
		public TextView tv_song_name;
		
	}
	
	
}
