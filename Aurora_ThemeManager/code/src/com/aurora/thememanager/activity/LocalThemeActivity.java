package com.aurora.thememanager.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.aurora.change.activities.DesktopWallpaperLocalActivity;
import com.aurora.change.activities.WallpaperLocalActivity;
import com.aurora.thememanager.R;
import com.aurora.thememanager.adapter.AbsThemeAdapter;
import com.aurora.thememanager.adapter.ThemeLocalAdapter;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.FileUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themeloader.Loader;
import com.aurora.thememanager.utils.themeloader.ThemeLoadListener;
import com.aurora.thememanager.utils.themeloader.ThemePackageLoader;
public class LocalThemeActivity extends BaseActivity implements OnItemClickListener {
	
	
	private ListView mList;
	
	private String[] mItems;
	private int[] mIcons = {
			R.drawable.ic_local_theme_theme,
			R.drawable.ic_local_theme_wallpaper,
			R.drawable.ic_local_theme_time_wallpaper,
			R.drawable.ic_local_theme_ringtong
	};
	
	private ItemAdapter mAdapter;
	
	private int mItemLayout = R.layout.theme_local_category_item;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mApp.registerActivity(this);
		setAuroraContentView(R.layout.local_theme_activity, AuroraActionBar.Type.Normal);
		mList = (ListView)findViewById(android.R.id.list);
		mItems = getResources().getStringArray(R.array.tab_names);
		mAdapter = new ItemAdapter();
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		getAuroraActionBar().setTitle(R.string.local_theme_title);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		boolean resetAnimation = false;
		switch (position) {
		case 0://theme
			intent.putExtra(Action.KEY_GOTO_LOCAL_TIME_WALLPAPER_LOCATION, true);
			intent.setClass(this, DownloadedThemeActivity.class);
			break;
		case 1://wallpaper
			intent.setClass(this, DesktopWallpaperLocalActivity.class);
			resetAnimation = true;
			break;
		case 2://times wallpaper
			intent.putExtra(Action.KEY_GOTO_LOCAL_TIME_WALLPAPER_LOCATION, true);
			intent.setClass(this, WallpaperLocalActivity.class);
			resetAnimation = true;
			break;
			
		case 3://ringtong
			
			intent.setClass(this, DownloadedRingTongActivity.class);
			break;

		default:
			break;
		}
		
		startActivity(intent);
		if(resetAnimation){
			overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
		}
	}

	
	
	
	
	
	
	class ItemAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mItems.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mItems[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			 if(convertView == null){
				 convertView = LayoutInflater.from(LocalThemeActivity.this).inflate(mItemLayout, null);
			 }
			 TextView title = (TextView) convertView.findViewById(R.id.text);
			 View divider = convertView.findViewById(R.id.divider);
			 if(position == mItems.length - 1){
				 divider.setVisibility(View.GONE);
			 }
			 title.setText(mItems[position]);
			 ImageView icon =  (ImageView) convertView.findViewById(R.id.icon);
			 icon.setImageResource(mIcons[position]);
			return convertView;
		}
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
