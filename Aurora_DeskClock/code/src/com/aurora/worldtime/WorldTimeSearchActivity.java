package com.aurora.worldtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraSearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.deskclock.AlarmClock;
import com.android.deskclock.Log;
import com.android.deskclock.R;
import com.aurora.utils.Blur;
import com.aurora.worldtime.LetterSideBar.OnTouchingLetterChangedListener;

//GIONEE: wangfei 2012-09-04 add for CR00686766 begin
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
//GIONEE: wangfei 2012-09-04 add for CR00686766 end

public class WorldTimeSearchActivity extends AuroraActivity implements
		OnItemClickListener,OnTouchListener, OnTouchingLetterChangedListener{

	List<City> mResultList = new ArrayList<City>();
	List<City> mSearchList = new ArrayList<City>();

	private ListView mListView;
	private ImageView mSearchButton;
	private AuroraSearchView mSearchView;
	//private ImageButton forback;
	private TextView mSearchNoCity;

	private Toast mToast = null;

	PinyinUtil mPinyin;
	
	//aurora add by tangjun start 2014.1.4
	private LetterToast letterToast;
	private WorldTimeSearchAdapter mAdapter;
	//aurora add by tangjun end 2014.1.4
 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.world_time_search_white);
		
		//deskClockApplication = (DeskClockApplication) this.getApplication();
		mSearchList = WorldTimeUtils.getmWorldTimeSearchList();
		
		/*
		//aurora add by tangjun 2014.1.4 start
		Collections.sort(mSearchList, new Comparator() {

			@Override
			public int compare(final Object lhs, final Object rhs) {
				City s1 = (City) lhs;
				City s2 = (City) rhs;
				
				return UsefulUtils.getSpell(s1.getName()).compareTo(UsefulUtils.getSpell(s2.getName()));
			}
			
		});
		//aurora add by tangjun 2014.1.4 end
		 */
		
		mPinyin = new PinyinUtil();
		getView();
	}

	/*
	 * refresh activity
	 */
	private void getView() {
		//aurora add by tangjun 2014.1.4 start
		LetterSideBar letterSideBar = (LetterSideBar)findViewById(R.id.letterSideBar);
		letterSideBar.setOnTouchingLetterChangedListener(this);
		
		RelativeLayout searchback = (RelativeLayout)findViewById(R.id.searchback);
		Bitmap bitmap = Blur.getBackgroundPic(this, "Default-Wallpaper.png");
		searchback.setBackgroundDrawable(new BitmapDrawable(bitmap));
		//bitmap.recycle();
		//Blur.showBgBlurView(this, searchback, AlarmClock.lockscreenDefaultPath);
//		if ( AlarmClock.mScreenBitmapMatrix != null ) {
//			searchback.setBackground(new BitmapDrawable(AlarmClock.mScreenBitmapMatrix));
//		} else {
//			searchback.setBackgroundResource(R.drawable.background);
//		}
		//aurora add by tangjun 2014.1.4 end
		
		mListView = (ListView) findViewById(R.id.lv_zone_search);
		mSearchView = (AuroraSearchView) findViewById(R.id.et_search_city);
		mSearchView.setSearchViewBorder(R.drawable.searchviewback);
		//mSearchView.requestFocus();
		mSearchView.clearEditFocus();
		mSearchView.setHintTextColor(getResources().getColor(
												R.color.worldtime_searchview_hintcolor));
		//mSearchView.onActionViewExpanded();
		mSearchNoCity = (TextView) findViewById(R.id.world_time_search_no_city);
		/*
		forback = (ImageButton) findViewById(R.id.world_forBack);
		forback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
				finish();
			}
		});
		*/
		mResultList = mSearchList;
		setListViewAdapter(mSearchList);
		mListView.setOnItemClickListener(this);
        //GIONEE: wangfei 2012-09-04 add for CR00686766 begin
        mListView.setOnTouchListener(this);
        //GIONEE: wangfei 2012-09-04 add for CR00686766 end

		mSearchView
				.setOnQueryTextListener(new AuroraSearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String query) {
						InputMethodManager inputMethodManager = (InputMethodManager) WorldTimeSearchActivity.this
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputMethodManager.hideSoftInputFromWindow(
								mSearchView.getWindowToken(), 0);
						return true;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						int end;
						if(newText == null){
							return false;
						}
						end = newText.length();
						if (end == 0) {
							mResultList = mSearchList;
							setListViewAdapter(mResultList);
						} else {
							String mSearchName = newText;
							Log.e("---------newText = -------------" + newText);
							if (!isHaveChinese(mSearchName)) {
								mResultList = search(mSearchName, mSearchList);
							} else {
								mResultList = search(mSearchName,
										mSearchList);
							}
							setListViewAdapter(mResultList);
						}

						if (mResultList.size() == 0) {
							mSearchNoCity.setVisibility(View.VISIBLE);
						} else {
							mSearchNoCity.setVisibility(View.GONE);
						}
						return false;
					}
				});
	}

	/**
	 * Judge of character is all Chinese
	 * 
	 */
	public static boolean isHaveChinese(String str) {
		Log.e("---------isHaveChinese = -------------" + str);
		for (int i = 0; i < str.length(); i++) {
			char ss = str.charAt(i);
			boolean s = String.valueOf(ss).matches("[\u4e00-\u9fa5]");
			if (!s) {
				Log.e("---------HaveChinese = -------------");
				return true;
			}
		}

		return false;
	}
	private void setListViewAdapter(List<City> list) {

		WorldTimeSearchAdapter adapter = null;

		adapter = new WorldTimeSearchAdapter(list,
				R.layout.world_time_search_list);
		
		mAdapter = adapter;

		mListView.setAdapter(adapter);

	}

	/*
	 * search by letter
	 */
	public List<City> searchByChar(String name, List<City> list) {
		List<City> results = new ArrayList<City>();
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			return results;
		}
		for (int i = 0; i < list.size(); i++) {
			Matcher matcher = pattern.matcher((CharSequence) PinyinUtil.stringArrayToString(PinyinUtil.getHeadByString(list.get(i).getName(), true)));
			if (matcher.find()) {
				results.add(list.get(i));
			}

		}
		return results;
	}

	/*
	 * fuzzy search
	 */
	public List<City> search(String name, List<City> list) {
		List<City> results = new ArrayList<City>();
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			return results;
		}
		for (int i = 0; i < list.size(); i++) {
			Matcher matcher = pattern.matcher((CharSequence) list.get(i)
					.getName());
			if (matcher.find()) {
				results.add(list.get(i));
			}
		}
		return results;
	}

	/*
	 * whether it has repeat date
	 */
	private boolean isRepeatData(String name) {
		int size = 0;
		List<City> idList = WorldTimeUtils.getmWorldTimeShowList();
		if (idList.size() != 0) {
			while (size < idList.size()) {
				if (name.equals(idList.get(size).getName())) {
					break;
				} else {
					size++;
				}
			}

			if (size != idList.size()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	public void showToast(String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		} else {
			// mToast.cancel();
			mToast.setText(msg);

		}
		mToast.show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String name = (String) mResultList.get(arg2).getName();
		String id = (String) mResultList.get(arg2).getId();
		String gmt = (String) mResultList.get(arg2).getGmt();
		if (isRepeatData(name)) {
			showToast(name + getResources().getString(R.string.exists_already));
		} else {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
			Intent intent = new Intent();
			intent.putExtra("name", name);
			intent.putExtra("cityId", id);
			intent.putExtra("gmt", gmt);
			setResult(Constants.RESULT, intent);
			finish();
		}

	}

    //GIONEE: wangfei 2012-09-04 add for CR00686766 begin
	@Override
	public boolean onTouch(View v, MotionEvent event) {
	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
		    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }
        return false;
	}
    //GIONEE: wangfei 2012-09-04 add for CR00686766 end

	class WorldTimeSearchAdapter extends BaseAdapter {
		List<City> showList = new ArrayList<City>();

		private int inflaterResourceId;
		
		private boolean[] isFirstAlpha;

		public WorldTimeSearchAdapter(List<City> searchList,
				int inflaterResourceId) {
			showList = searchList;
			this.inflaterResourceId = inflaterResourceId;
			getFirstAlpha();
		}

		@Override
		public int getCount() {
			return showList.size();
		}

		@Override
		public Object getItem(int position) {
			return showList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(WorldTimeSearchActivity.this)
						.inflate(inflaterResourceId, null);
				holder = new ViewHolder();
				holder.tvTitle = (TextView)convertView.findViewById(R.id.title);
				holder.tvName = (TextView) convertView
						.findViewById(R.id.world_time_search_name);
				holder.tvGmt = (TextView) convertView
						.findViewById(R.id.world_time_search_gmt);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			String cityName = showList.get(position).getName();
			holder.tvName.setText(cityName);
			holder.tvGmt.setText(showList.get(position).getGmt());
			
			if ( isFirstAlpha[position] ) {
				holder.tvTitle.setVisibility(View.VISIBLE);
				holder.tvTitle.setText(UsefulUtils.getSpell(cityName).substring(0, 1));
			} else {
				holder.tvTitle.setVisibility(View.GONE);
			}
			return convertView;
		}

		public final class ViewHolder {
			public TextView tvTitle;
			public TextView tvName;
			public TextView tvGmt;
		}
		
		private void getFirstAlpha() {
			boolean flag[] = new boolean[26];
			isFirstAlpha = new boolean[showList.size()];
			for( int i = 0; i < showList.size(); i++ ) {
				String cityName = showList.get(i).getName();
				//Log.e("--cityName = " + cityName);
				//Log.e("--i = " + i + ", charAt(0) = " + UsefulUtils.getSpell(cityName).charAt(0));
				if ( !flag[(int)(UsefulUtils.getSpell(cityName).charAt(0)) - 65] ) {
					flag[(int)(UsefulUtils.getSpell(cityName).charAt(0)) - 65] = true;
					isFirstAlpha[i] = true;
				}
			}
		}

	}

	@Override
	public void onTouchingLetterChanged(String s, float positionOfY) {
		// TODO Auto-generated method stub
		if(letterToast == null ){
			FrameLayout frameLayout = (FrameLayout)findViewById(R.id.ListFrameLayout);
			letterToast = new LetterToast(frameLayout);
		}
		letterToast.LetterChanged(s, getResources().getInteger(R.integer.lettertoast_ofy), mListView, mAdapter);
		android.util.Log.i("ddd",positionOfY+"" );
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(letterToast != null){
			letterToast.releaseObject();
		}
		
		super.onDestroy();
	}

}
