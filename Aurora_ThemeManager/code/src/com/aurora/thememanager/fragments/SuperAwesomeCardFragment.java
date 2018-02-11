package com.aurora.thememanager.fragments;

import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.MainActivity;
import com.aurora.thememanager.activity.MainActivity.OnNetworkChangeListener;
import com.aurora.thememanager.parser.JsonParser;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.parser.RingTongPaser;
import com.aurora.thememanager.parser.ThemeFromInternetPaser;
import com.aurora.thememanager.parser.ThemeTimeWallpaperPaser;
import com.aurora.thememanager.parser.WallpaperPaser;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

public  abstract class SuperAwesomeCardFragment extends Fragment implements HttpCallBack{

	private static final String ARG_POSITION = "position";
	
	protected static final int TAB_THEME = 0;
	
	protected static final int TAB_WALLPAPER = 1;
	
	protected static final int TAB_FONTS = 2;
	
	protected static final int TAB_RINGTONG = 3;
	
	private JsonParser mJsonParser;
	
	private static Class<?>[] mFragments = {
			ThemeFragment.class,
			WallPaperFragment.class,
			TimeWallPaperFragment.class,
			RingTongFragment.class
			
	};
	

	protected int position;

	private JsonHttpListener mHttpListener;
	
	private Parser mThemeParser;
	
	public static SuperAwesomeCardFragment newInstance(int position) {
		SuperAwesomeCardFragment f;
		try {
			f = (SuperAwesomeCardFragment) mFragments[position].newInstance();
			Bundle b = new Bundle();
			b.putInt(ARG_POSITION, position);
			f.setArguments(b);
			
			return f;
		} catch (java.lang.InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHttpListener = new JsonHttpListener(this);
		mJsonParser = new ThemeFromInternetPaser();
		mThemeParser = new Parser(new ThemeFromInternetPaser());
		position = getArguments().getInt(ARG_POSITION);
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		FrameLayout fl = new FrameLayout(getActivity());
		fl.setLayoutParams(params);
		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
				.getDisplayMetrics());

		final TextView v = new TextView(getActivity());
		params.setMargins(margin, margin, margin, margin);
		v.setLayoutParams(params);
		v.setLayoutParams(params);
		v.setGravity(Gravity.BOTTOM);
		v.setText("CARD " + (position + 1));
		fl.addView(v);
		return fl;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
	}
	
	/**
	 * 获取网络监听器
	 * @return
	 */
	protected JsonHttpListener getHttpListener(){
		return mHttpListener;
	}
	
	/**
	 * 获取json解析器
	 * @return
	 */
	protected Parser getThemeParser() {
		
		return mThemeParser;
	}
	
	protected Parser getThemeParser(int type) {
		switch (type) {
		case Parser.TYPE_RINGTONG:{
			mJsonParser = new RingTongPaser();
			return new Parser(mJsonParser);
		}
		case Parser.TYPE_THEME_PAKAGE:{
			return mThemeParser;
		}
		case Parser.TYPE_TIME_WALLPAPER:{
			mJsonParser = new ThemeTimeWallpaperPaser();
			return new Parser(mJsonParser);
		}
		case Parser.TYPE_WALLPAPER:{
			mJsonParser = new WallpaperPaser();
			return new Parser(mJsonParser);
		}

		default:
			return mThemeParser;
		}
	}
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	/**
	 * 提供当前Fragment的主色调的Bitmap对象,供Palette解析颜色
	 * 
	 * @return
	 */
//	public static int getBackgroundBitmapPosition(int selectViewPagerItem) {
//		return drawables[selectViewPagerItem];
//	}

}