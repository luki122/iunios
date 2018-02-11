package com.aurora.thememanager.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraCheckBox;

import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.activity.DownloadedThemeActivity;
import com.aurora.thememanager.activity.LocalThemeActivity;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.ToastUtils;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;

public class ThemeLocalAdapter extends AbsThemeAdapter {

	private HashMap<Integer, Boolean> mChoice = new HashMap<Integer, Boolean>();
	
	private boolean mSelecteMode = false;
	
	private int mAppliedThemeId = -2;
	
	public ThemeLocalAdapter(Context context,int themeId) {
		super(context);
		// TODO Auto-generated constructor stub
		mAppliedThemeId = themeId;
	}
	

	
	@Override
	public View getView(final int position, View itemView, ViewGroup paramViewGroup) {
		// TODO Auto-generated method stub
		Holder holder;
		if(itemView == null){
			itemView = mInflater.inflate(R.layout.theme_grid_item, null);
			holder = new Holder();
			holder.image = (NetworkImageView) itemView.findViewById(R.id.theme_grid_img);
			holder.title = (TextView) itemView.findViewById(R.id.theme_grid_text);
			holder.state = (ImageView) itemView.findViewById(R.id.downloaded_theme_state);
			holder.mChecked = (AuroraCheckBox)itemView.findViewById(R.id.downloaded_theme_selected_box);
			holder.mask = itemView.findViewById(R.id.theme_item_mask);
			holder.checkMengban = itemView.findViewById(R.id.theme_item_check_mengban);
			itemView.setTag(holder);
		}else{
			holder = (Holder) itemView.getTag();
		}
		final Theme theme = mDatas.get(position);
		if (theme != null) {
			   holder.image.setDefaultImageResId(R.drawable.item_default_bg_normal);
			   if(theme.themeId == mAppliedThemeId){
				   holder.state.setVisibility(View.VISIBLE);
			   }else{
				   holder.state.setVisibility(View.GONE);
			   }
			   holder.mask.setVisibility((theme.themeId == mAppliedThemeId)?View.VISIBLE:View.GONE);
			   holder.image.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mSelecteMode){
						
						Boolean select = mChoice.get(position);
						if(select == null){
							select = false;
						}
						if(position == 0){
							mChoice.put(position, false);
							ToastUtils.showToast(mContext, R.string.tip_delelte_system_theme);
						}else{
							if(isAppliedTheme(theme.themeId)){
								mChoice.put(position, false);
								ToastUtils.showToast(mContext, R.string.tip_delelte_applied_theme);
							}else{
								mChoice.put(position, !select);
							}
							
						}
						
						((DownloadedThemeActivity)mContext).updateSelectedItems();
						notifyDataSetChanged();
						
					}else{
						Intent intent = new Intent();
						intent.setAction(Action.ACTION_APPLY_THEME);
						intent.putExtra(ThemeConfig.KEY_FOR_APPLY_THEME, theme);
						intent.putExtra(ThemeConfig.KEY_FOR_APPLY_FROM_LOACAL, true);
						mContext.startActivity(intent);
					}
				}
			});
			   holder.image.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					if(!mSelecteMode){
						if(position == 0){
							mChoice.put(position, false);
						}else{
							if(isAppliedTheme(theme.themeId)){
								mChoice.put(position, false);
							}else{
								mChoice.put(position, true);
							}
						}
						
						enterSelectMode(true);
						((DownloadedThemeActivity)mContext).showEditView();
						((DownloadedThemeActivity)mContext).updateSelectedItems();
					}
					return true;
				}
			});
			   
			   holder.image.setDefaultImageResId(R.drawable.item_default_bg_normal);
			   if(theme.themeId == ThemeConfig.THEME_DEFAULT_ID){
				   holder.image.setImageUrl(ImageLoaderImpl.RES_ASSETS+theme.preview, getImageLoader());
			   }else{
				   holder.image.setImageUrl(ImageLoaderImpl.RES_SDCARD+theme.preview, getImageLoader());
			   }
				
				holder.title.setText(theme.name);
				Boolean selected = mChoice.get(position);
				if(selected == null){
					selected = false;
				}
				holder.mChecked.setChecked(selected);
				holder.checkMengban.setVisibility(selected ? View.VISIBLE : View.GONE);
				if(mSelecteMode){
					holder.mChecked.setVisibility(View.VISIBLE);
				}else{
					holder.mChecked.setVisibility(View.GONE);
					holder.checkMengban.setVisibility(View.GONE);
				}
			}
		return itemView;
	}
	
	class Holder{
		NetworkImageView image;
		TextView title;
		ImageView state;
		AuroraCheckBox mChecked;
		View mask;
		View checkMengban;
		
	}

	private boolean isAppliedTheme(int themeId){
		ThemeManager tm = ((DownloadedThemeActivity)mContext).getThemeManager();
		int appliedId = tm.getAppliedThemeId(mContext);
		return themeId == appliedId;
	}
	
	public void enterSelectMode(boolean select){
		mSelecteMode = select;
		notifyDataSetChanged();
	}

	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}


	@Override
	public synchronized void  addData(Theme data) {
		// TODO Auto-generated method stub
		super.addData(data);
	}



	public void exitSelectMode() {
		// TODO Auto-generated method stub
		mChoice.clear();
		mSelecteMode = false;
		notifyDataSetChanged();
	}



	public void selectAll(boolean selectAll) {
		// TODO Auto-generated method stub
		selectAllOrNot(selectAll);
	}
	


	private void selectAllOrNot(boolean select){
		int size = getCount();
		for(int i = 0;i< size;i++){
			mChoice.put(i, select);
		}
		notifyDataSetChanged();
	}



	public ArrayList<Theme> getSelectedTheme() {
		// TODO Auto-generated method stub
		int size = mChoice.size();
		Set<Integer> keys = mChoice.keySet();
		if(keys == null || keys.size() < 1){
			return null;
		}
		ArrayList<Theme> themes = new ArrayList<Theme>();
		Iterator< Integer> in = keys.iterator();
		while(in.hasNext()){
			Integer position = in.next();
			Boolean selected = mChoice.get(position);
			if(selected){
				Theme theme = getItem(position);
				themes.add(theme);
			}
		}
		return themes;
	}
	
	public void updateItems(){
		if(mDatas.size() > 0){
			for(Theme theme:mDatas){
				String themeFileName = theme.fileDir+File.separatorChar+theme.fileName;
				if(theme.themeId == ThemeConfig.THEME_DEFAULT_ID){
					continue;
				}
				File themeFile = new File(themeFileName);
				if(themeFileName.contains("null")){
					continue;
				}
				if(!themeFile.exists()){
					mDatas.remove(theme);
				}
			}
			notifyDataSetChanged();
		}
	}
	

}