package com.aurora.thememanager.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.themeloader.PictureLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public abstract class ThemeAdapter extends BaseAdapter implements ImageLoadingListener{

	protected int mItemLayoutId;
	protected Context mContext;
	protected LayoutInflater mInflater;
	
	
	protected List<Theme> mDatas = new ArrayList<Theme>();
	
	public ThemeAdapter(Context context){
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	@Override
	public Theme getItem(int index) {
		// TODO Auto-generated method stub
		return mDatas.get(index);
	}

	@Override
	public long getItemId(int itemId) {
		// TODO Auto-generated method stub
		return itemId;
	}

	public void setItemLayoutId(int layoutId){
		mItemLayoutId = layoutId;
	}
	
	public  void addData(Theme data){
		
		if(data == null){
			return;
		}
		synchronized (mDatas) {
			if(mDatas.size() == 0 || !mDatas.contains(data)){
				mDatas.add(data);
				updateData();
			}
		}
		
		
	}
	
	public  void deleteData(Theme data){
		if(data == null){
			return;
		}
		synchronized (mDatas) {
			if(mDatas.contains(data)){
				mDatas.remove(data);
				updateData();
			}
		}
		
	}
	
	protected void loadImage(String uri,ImageView imageview){
		PictureLoader.displayImage(uri, imageview, this);
	}
	
	public abstract void updateData();
	
	
	
	

}
