package com.aurora.thememanager.adapter;

import java.util.ArrayList;

import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.adapter.TimeWallpaperAdapter.ViewHolder;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.DensityUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.widget.NetworkRoundedImageView;
import com.aurora.thememanager.widget.ProgressBtn;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class WallpaperAdapter extends AbsThemeAdapter {

	public WallpaperAdapter(Context context) {
		super(context);
		mDatas.clear();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		Theme theme = mDatas.get(position);
		
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.wallpaper_list_item, null);
			holder = new ViewHolder();
			holder.preview1 = (NetworkImageView)convertView.findViewById(R.id.theme_item_preview1);
			holder.preview2 = (NetworkImageView)convertView.findViewById(R.id.theme_item_preview2);
			holder.preview3 = (NetworkImageView)convertView.findViewById(R.id.theme_item_preview3);
			convertView.setTag(holder);
			if(position == 0) {
				convertView.setPadding(convertView.getPaddingLeft(), DensityUtils.dip2px(mContext, 8), convertView.getPaddingRight(), convertView.getPaddingBottom());
			}
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		updateItemLayout(holder, position);
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size() > 0 ? ( mDatas.size() + 2 ) / 3 : 0;
	}
	
	/**
	 * 更新主题
	 * @param holder
	 * @param theme
	 */
	private void updateItemLayout(ViewHolder holder, int position){
		
		if(mDatas.size() > 0 && ( mDatas.size() + 2 ) / 3 > position){
			
			Theme theme;
			if (mDatas.size() > position * 3) {
				theme = mDatas.get(position * 3);
				theme.downloadId = theme.themeId;
				theme.fileName = theme.name;
				holder.preview1.setVisibility(View.VISIBLE);
				holder.theme1 = theme;
				holder.viewPagerPosition1 = position * 3;
				holder.preview1.setDefaultImageResId(R.drawable.item_default_bg_normal);
				holder.preview1.setImageUrl(theme.previews[0], getImageLoader());
				holder.setOnClickListener(holder.preview1);
			} else {
				holder.preview1.setVisibility(View.GONE);
			}
			if (mDatas.size() > position * 3 + 1) {
				theme = mDatas.get(position * 3 + 1);
				theme.downloadId = theme.themeId;
				theme.fileName = theme.name;
				holder.theme2 = theme;
				holder.viewPagerPosition2 = position * 3 + 1;
				holder.preview2.setVisibility(View.VISIBLE);
				holder.preview2.setDefaultImageResId(R.drawable.item_default_bg_normal);
				holder.preview2.setImageUrl(theme.previews[0], getImageLoader());
				holder.setOnClickListener(holder.preview2);
			} else {
				holder.preview2.setVisibility(View.GONE);
			}
			
			if (mDatas.size() > position * 3 + 2) {
				theme = mDatas.get(position * 3 + 2);
				theme.downloadId = theme.themeId;
				theme.fileName = theme.name;
				holder.theme3 = theme;
				holder.viewPagerPosition3 = position * 3 + 2;
				holder.preview3.setVisibility(View.VISIBLE);
				holder.preview3.setDefaultImageResId(R.drawable.item_default_bg_normal);
				holder.preview3.setImageUrl(theme.previews[0], getImageLoader());
				holder.setOnClickListener(holder.preview3);
			} else {
				holder.preview3.setVisibility(View.GONE);
			}
		}
		
	}

	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}
	
	class ViewHolder implements OnClickListener{
		NetworkImageView preview1;
		NetworkImageView preview2;
		NetworkImageView preview3;
		Theme theme1;
		Theme theme2;
		Theme theme3;
		int viewPagerPosition1;
		int viewPagerPosition2;
		int viewPagerPosition3;
		
		void setOnClickListener(View itemView){
			itemView.setOnClickListener(this);
			
		}
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			int id = view.getId();
			Intent intent = new Intent(Action.ACTION_PREVIEW_WALLPAPER);
			intent.putExtra("wallpaper_preview_list_theme", (ArrayList<Theme>)mDatas);
			switch (id) {
			case R.id.theme_item_preview1:
				//intent.putExtra(Action.KEY_SHOW_WALL_PAPER_PREVIEW, theme1);
				intent.putExtra("wallpaper_preview_position", viewPagerPosition1);
				mContext.startActivity(intent);
				break;
			case R.id.theme_item_preview2:
				//intent.putExtra(Action.KEY_SHOW_WALL_PAPER_PREVIEW, theme2);
				intent.putExtra("wallpaper_preview_position", viewPagerPosition2);
				mContext.startActivity(intent);
				break;
			case R.id.theme_item_preview3:
				//intent.putExtra(Action.KEY_SHOW_WALL_PAPER_PREVIEW, theme3);
				intent.putExtra("wallpaper_preview_position", viewPagerPosition3);
				mContext.startActivity(intent);
				break;

			default:
				break;
			}
		}
    }

}
