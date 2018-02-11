/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup.ux10;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.media.SoundPool;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qualcomm.listen.voicewakeup.R;


public class SoundListAdapter extends BaseAdapter {
	private final static String MYTAG = "iht";
	private Context context;
	private ArrayList<ItemData> soundList;
	private ArrayList<RowViewHolder> rowViewHolderList = new ArrayList<RowViewHolder>();
	//private OnItemActionListener onItemActionListener;
	private OnItemSelectActionListener onItemSelectActionListener;

	public SoundListAdapter(Context context, ArrayList<ItemData> inSoundList,OnItemSelectActionListener s) {
		this.context = context;
		this.soundList = inSoundList;
		this.onItemSelectActionListener = s;
		
		rowViewHolderList.clear();
	}

	@Override
	public int getCount() {
		return soundList.size();
	}

	@Override
	public Object getItem(int position) {
		return soundList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	

		View v = convertView;
		RowViewHolder rowviewHolder;
		
		if(v == null){
			
			//需要返回的item
			v = (View) LayoutInflater.from(context).inflate(com.aurora.R.layout.aurora_slid_listview, null);
			rowviewHolder = new RowViewHolder();
			rowviewHolder.itemView = v;  
			v.setTag(rowviewHolder);

			rowViewHolderList.add(rowviewHolder); //将所有的view装载进去，以遍历
			
			//装进入的内容
			RelativeLayout mainUi = (RelativeLayout) v.findViewById(com.aurora.R.id.aurora_listview_front);
			//myView
			View myView  = LayoutInflater.from(context).inflate(R.layout.sounditem, null); 
			//left
			TextView txtView = (TextView) myView.findViewById(R.id.username); //R.id.username;
			//txtView.setText(soundModel.username);
			rowviewHolder.mytxtView = txtView;
			
			//left-down
			TextView introView = (TextView) myView.findViewById(R.id.intro);// 
			rowviewHolder.myintroView = introView;
			
			//right
			View myswitch = myView.findViewById(R.id.sound_switch);
			rowviewHolder.myswitch = myswitch;
			
			View switch_layout = (LinearLayout) myView.findViewById(R.id.switch_layout);
			rowviewHolder.myswitchlayout = switch_layout;
			switch_layout.setTag(rowviewHolder);
			switch_layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onItemClick(v);
				}
			});
			mainUi.addView(myView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			//显示分割线
			ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider);
	        iv.setVisibility(View.VISIBLE);
		}else{
			rowviewHolder = (RowViewHolder) v.getTag();
		}
		
        rowviewHolder.itemData = soundList.get(position);
        ((TextView)rowviewHolder.mytxtView).setText(rowviewHolder.itemData.aliasname);
        ((RadioButton)rowviewHolder.myswitch).setChecked(rowviewHolder.itemData.checked);
        
        if(rowviewHolder.itemData.checked){
        	rowviewHolder.myintroView.setVisibility(View.VISIBLE);
        	((TextView)rowviewHolder.myintroView).setText(R.string.enabled);
        	rowviewHolder.itemData.order = "0";
        }else{
        	rowviewHolder.myintroView.setVisibility(View.GONE);
        	rowviewHolder.itemData.order = "1";
        }
		return v;
	}
	
	public void onItemClick(View v){
		
		RowViewHolder rHolder = (RowViewHolder) v.getTag();
		boolean isCheck = rHolder.itemData.checked;
		if(!isCheck){
			for(ItemData itemdata : soundList){
				if(itemdata == rHolder.itemData){
					itemdata.checked = true;
					itemdata.order = "0";
				}else{
					itemdata.checked = false;
					itemdata.order = "1";
				}
			}
			
			//刷新界面
			for(RowViewHolder rH : rowViewHolderList){
				
				((RadioButton)rH.myswitch).setChecked(rH.itemData.checked);  
				if(rH.itemData.checked){
					rH.myintroView.setVisibility(View.VISIBLE);
					((TextView)rH.myintroView).setText(R.string.enabling);
					
					//也不能点击
					((View)rH.myswitchlayout).setClickable(false);
					
				}else{
					rH.myintroView.setVisibility(View.GONE); 
					
					//不能点击
					((View)rH.myswitchlayout).setClickable(false);
					//改变字体颜色
					((TextView)rH.mytxtView).setTextColor(Color.parseColor("#50444444"));
				}
			}
			
			//返回监听事件
			onItemSelectActionListener.showSelectedView(v, rHolder.itemData);
			
			//这里刷新以下界面
			//onItemSelectActionListener.freshListView();
			
		}else{
			
			//已经选中，无法
			/*
			for(ItemData itemdata : soundList){
				itemdata.checked = false;
				itemdata.order = "1";
			}
			*/
			
		}

		
		/*
		//刷新界面
		for(RowViewHolder rH : rowViewHolderList){
			
			((RadioButton)rH.myswitch).setChecked(rH.itemData.checked);  
			if(rH.itemData.checked){
				rH.myintroView.setVisibility(View.VISIBLE);
				((TextView)rH.myintroView).setText(R.string.enabling);
				
				//也不能点击
				((View)rH.myswitchlayout).setClickable(false);
				
			}else{
				rH.myintroView.setVisibility(View.GONE); 
				
				//不能点击
				((View)rH.myswitchlayout).setClickable(false);
				//改变字体颜色
				((TextView)rH.mytxtView).setTextColor(Color.parseColor("#50444444"));
			}
		}
		
		//返回监听事件
		onItemSelectActionListener.showSelectedView(v, rHolder.itemData);
		
		//这里刷新以下界面
		//onItemSelectActionListener.freshListView();
		*/
	}
	
	public void refreshViewState(){
		if(rowViewHolderList != null && !rowViewHolderList.isEmpty() ){
			for(RowViewHolder rH : rowViewHolderList){
				((View)rH.myswitchlayout).setClickable(true);
				((TextView)rH.mytxtView).setTextColor(Color.parseColor("#444444"));
				if(rH.itemData.checked){
					((TextView)rH.myintroView).setText(R.string.enabled);
					
					//已经选中的无法选择
					//((View)rH.myswitchlayout).setClickable(false);
				}
			}
		}
	}
	
	public interface OnItemSelectActionListener {
		public void showSelectedView(View view, ItemData item);
	}
	
	public static class RowViewHolder{
		public View itemView;
		public View mytxtView;
		public View myintroView;
		public View myswitchlayout;
		public View myswitch;
		public ItemData itemData;
	}

	public static class ItemData implements Comparable<ItemData> {
		private boolean checked;
		private String keyword;
		private String username;
		private String filename;
		private String aliasname;
		private String order;

		public ItemData(boolean checked, String keyword, String username, String filename, String aliasname, String order) {
			this.checked = checked;
            this.keyword = keyword;
            this.username = username;
            this.filename = filename;
            this.aliasname = aliasname;
            this.order = order;
		}

		public boolean checked() {
			return checked;
		}

        public String keyword() {
            return keyword;
        }

		public String username() {
            return username;
        }
		
		public String filename(){
			return filename;
		}
		
		public String aliasname(){
			return aliasname;
		}
		
		public void setAliasname(String name){
			aliasname = name;
		}
		
		public void setChecked(boolean b) {
			checked = b;
		}
		
		public void setOrder(String ord){
			order = ord;
		}
		
		public String order(){
			return order;
		}

		@Override
		public int compareTo(ItemData another) {
			//return this.username().compareToIgnoreCase(another.username());
			return this.order().compareTo(another.order());
		}
	}
	
	
}
