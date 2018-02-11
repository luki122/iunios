package com.android.auroramusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.music.R;

public class AuroraSoundControlAdapter extends BaseAdapter{

	private Context mContext;
	private String[] mImgTextStrings = new String[]{
			"Default","Pop","Live","Rock","Bass","Jazz","Electronic","Classical"
	};
	
	private int[] soundNames= new int[]{
			R.string.aurora_defualt,R.string.aurora_pop,R.string.aurora_live,R.string.aurora_rock,
			R.string.aurora_bass,R.string.aurora_jazz,R.string.aurora_electri,R.string.aurora_classical			
	};
	
	private int[] uncheckedImgs = new int[]{
			R.drawable.aurora_sound_default,R.drawable.aurora_sound_pop,R.drawable.aurora_sound_live,R.drawable.aurora_sound_rock,
			R.drawable.aurora_sound_bass,R.drawable.aurora_sound_jazz,R.drawable.aurora_sound_electri,R.drawable.aurora_sound_classical
	};
	
	private int[] checkedImgs = new int[]{
			R.drawable.aurora_default_on,R.drawable.aurora_pop_on,R.drawable.aurora_live_on,R.drawable.aurora_rock_on,
			R.drawable.aurora_bass_on,R.drawable.aurora_jazz_on,R.drawable.aurora_electri_on,R.drawable.aurora_classical_on
	};
	
	private int checkedPosition;
	private boolean isControlEnable=false;
	
	public AuroraSoundControlAdapter(Context context){
		mContext = context;
		checkedPosition=0;
		isControlEnable =true;
	}
	
	public void setSelectControl(int pos){
		checkedPosition = pos;
		notifyDataSetChanged();
	}
	
	public void updateControl(int pos, boolean is) {
		checkedPosition = pos;
		isControlEnable=is;
		notifyDataSetChanged();
		return;
	}
	
	public void setSoundControlEnable(boolean is){
		isControlEnable=is;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {

		return mImgTextStrings.length;
	}

	@Override
	public Object getItem(int arg0) {

		return mImgTextStrings[arg0];
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		
		HoldView holdView;
		if(arg1==null){
			holdView = new HoldView();
			arg1 = LayoutInflater.from(mContext).inflate(R.layout.aurora_sound_control_item, null);
			holdView.checksoud = (ImageView)arg1.findViewById(R.id.aurora_select_sound);
			holdView.imgTextView = (TextView)arg1.findViewById(R.id.img_text);
			holdView.soundName = (TextView)arg1.findViewById(R.id.aurora_sound_name);
			arg1.setTag(holdView);
		}else{
			holdView = (HoldView)arg1.getTag();
		}
		if(checkedPosition==arg0&&isControlEnable){
			holdView.checksoud.setBackgroundResource(checkedImgs[arg0]);
		}else{
			holdView.checksoud.setBackgroundResource(uncheckedImgs[arg0]);
		}
		
		if(isControlEnable){
			arg1.setClickable(false);
			holdView.checksoud.setEnabled(true);
			holdView.soundName.setEnabled(true);
			arg1.setAlpha(1f);
		}else{
			arg1.setClickable(true);
			holdView.checksoud.setEnabled(false);
			holdView.soundName.setEnabled(false);
			arg1.setAlpha(0.4f);
		}
		
		holdView.imgTextView.setText(mImgTextStrings[arg0]);
		holdView.soundName.setText(mContext.getString(soundNames[arg0]));
		return arg1;
	}
	
	class HoldView{
		ImageView checksoud;
		TextView soundName;
		TextView imgTextView;
	}
}
