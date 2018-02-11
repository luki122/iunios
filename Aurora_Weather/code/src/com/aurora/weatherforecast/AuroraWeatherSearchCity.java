package com.aurora.weatherforecast;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.weatherdata.bean.CityItem;
import com.aurora.weatherdata.db.CNCityAdapter;
import com.aurora.weatherdata.util.Globals;
import com.aurora.weatherdata.util.Log;
import com.aurora.weatherdata.util.SystemUtils;

import java.util.ArrayList;
import java.util.List;

import totalcount.AddCountHelp;

import adapters.SearchCityAdapter;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import datas.CityListShowData;
import datas.CityListShowData.INetResponseCode;

public class AuroraWeatherSearchCity extends AuroraActivity {

	public static final int RESULTCODE = 15;
	private final String TAG = "JoyTest";
	private AuroraSearchView searchView;
	private Button btnCancle;
	private ListView lvCities;
	// 无搜索结果提示
	private TextView tvNoSearchCity;
	private List<CityItem> cityList;
	private CNCityAdapter weatherDB;
	private SearchCityAdapter adapter;
	private ProgressDialog requestNetShowDialog;
	private FrameLayout flList;

	private CityListShowData mCityListShowData;
	
	public static boolean isSearchAcitivytShow=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(com.aurora.R.style.Theme_Aurora_Light_NoActionBar_Fullscreen);
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.activity_aurora_weather_search_city,
				AuroraActionBar.Type.Normal, true);
		mCityListShowData=CityListShowData.getInstance(this);
		onInit();
		addINetResponseCode();
		setProgressBar();
		
	}

	private void setProgressBar() {
		requestNetShowDialog = new ProgressDialog(this);
		requestNetShowDialog.setCancelable(false);
		requestNetShowDialog.setCanceledOnTouchOutside(false);
		requestNetShowDialog.setMessage(getString(R.string.request_network_in));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isSearchAcitivytShow=true;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isSearchAcitivytShow=false;
	}
	
	/**
	 * 进行一些初始化操作
	 */
	private void onInit() {
		weatherDB = new CNCityAdapter(this);
		weatherDB.open();
		cityList = new ArrayList<CityItem>();

		getAuroraActionBar().setVisibility(View.GONE);
		searchView = (AuroraSearchView) findViewById(R.id.as_weather_search_city);
		searchView.setOnQueryTextListener(new SearchViewTextListener());
		searchView.getFocus();

		btnCancle = (Button) findViewById(R.id.btn_weather_search_city_cancle);
		btnCancle.setOnClickListener(new CancleBtnClickListener());

		flList = (FrameLayout) findViewById(R.id.fl_weather_search_list);
		lvCities = (ListView) findViewById(R.id.lv_weather_search_city);
		setSearchViewHint(getResources().getString(R.string.tv_search_input));
		adapter = new SearchCityAdapter(this, cityList);
		lvCities.setAdapter(adapter);
		lvCities.setOnItemClickListener(new CityListItemClickListener());
		tvNoSearchCity = (TextView) findViewById(R.id.tv_weather_no_searching_city);
	}

	/**
	 * 设置SearchView的hint提示
	 * 
	 * @param hint
	 */
	@SuppressWarnings("unused")
	private void setSearchViewHint(CharSequence hint) {
		if (searchView != null) {
			searchView.setQueryHint(hint);
		}
		setSearchResultBg(false);
	}

	private String addCityName;
	private boolean isCanClickItem=true;
	private long requestTime;
	private class CityListItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if(!isCanClickItem)
				return;
			isCanClickItem=false;
			hideKeyBoard();
			int cityId = cityList.get(position).getId();
			if (mCityListShowData.checkCityIsAdded(cityId)) {
				int index=mCityListShowData.getCityIndexByCityId(cityId);
				if(index!=-1)
				{
					mCityListShowData.itemClick(index);
				}
				setResult(RESULTCODE);
				finish();
				return;
			}
			if (SystemUtils.isNetworkAvailable(AuroraWeatherSearchCity.this)) {
				addCityName=weatherDB.getCityFromId(cityId).getCityName();
				if(addCityName!=null)
				{
					mCityListShowData.add(addCityName, cityId);
					requestTime=System.currentTimeMillis();
					if (!requestNetShowDialog.isShowing()) {
						requestNetShowDialog.show();
					}
				}
			} else {
				isCanClickItem=true;
				Toast.makeText(AuroraWeatherSearchCity.this,
						R.string.no_network, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class CancleBtnClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			hideKeyBoard();
			onBackPressed();
		}

	}

	/**
	 * 是否显示无结果提示
	 * 
	 * @param show
	 *            true 显示；false 不显示
	 */
	private void showNoSearchCityPrompt(boolean show) {
		if (tvNoSearchCity != null) {
			if (show) {
				if (tvNoSearchCity.getVisibility() == View.GONE) {
					tvNoSearchCity.setVisibility(View.VISIBLE);
				}
			} else {
				if (tvNoSearchCity.getVisibility() == View.VISIBLE) {
					tvNoSearchCity.setVisibility(View.GONE);
				}
			}
		}
	}

	/**
	 * 获取点击的城市名称
	 * 
	 * @param cityname
	 * @return
	 */
	public String getClickingCityName(String cityname, boolean isCity) {
		if (isCity) {
			return cityname.substring(0, cityname.indexOf("-"));
		} else {
			return cityname.substring(cityname.indexOf("-") + 1);
		}
	}

	private void setSearchResultBg(boolean lightBg) {
		if (!lightBg) {
			flList.setBackgroundResource(R.drawable.aurora_activity_search_bg);
		} else {
			flList.setBackgroundResource(0);
			flList.setBackgroundColor(Color.WHITE);
		}
	}

	/**
	 * 输入框内容变化监听器
	 * 
	 * @author leo
	 * 
	 */
	private class SearchViewTextListener implements OnQueryTextListener {

		@Override
		public boolean onQueryTextChange(String content) {
			Log.i(TAG, content);
			String key = content;
			if (key != null && !"".equals(key.trim())) {
				Log.i(TAG, " key:" + key);
				showNoSearchCityPrompt(false);
				//避免用户输入%，匹配出城市
				if(key.contains("%")) {
					key = key.replaceAll("%", "#");
				}
				if (SystemUtils.containCn(key)) {
					weatherDB.getCityListFromHZ(cityList, key);
				} else {
					weatherDB.getCityListFromPY(cityList, key);
				}

				// 如果没有搜索到结果，则显示无结果提示
				if (cityList == null || cityList.size() == 0) {
					showNoSearchCityPrompt(true);
				}
				adapter.notifyDataSetChanged();
				setSearchResultBg(true);
			} else {
				cityList.clear();
				adapter.notifyDataSetChanged();
				showNoSearchCityPrompt(false);
				setSearchResultBg(false);
			}
			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String arg0) {
			Log.i(TAG, arg0);
			return false;
		}

	}

	/**
	 * 关闭键盘
	 */
	private void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		}
		searchView.clearFocus();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ( keyCode == KeyEvent.KEYCODE_BACK ) {
			onBackPressed();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private INetResponseCode mINetResponseCode=new INetResponseCode() {
		@Override
		public void netResponseCode(int netCode,String cityName) {
			Log.i("jadon", "添加返回成功,城市名="+cityName+"     时间="+(System.currentTimeMillis()-requestTime)/1000+ "   netCode="+netCode+"    addCityName="+addCityName);
			isCanClickItem=true;
			requestNetShowDialog.dismiss();
			if(netCode==Globals.HAS_NETWORK)
			{
				if(addCityName!=null&&addCityName.equals(cityName))
				{	
					AddCountHelp.addCount(AddCountHelp.ADD_CITY, AuroraWeatherSearchCity.this);
					mCityListShowData.itemClick(mCityListShowData.getCitySize()-1);
					mCityListShowData.removeINetResponseCode(this);
					setResult(RESULTCODE);
					finish();
				}
			}else{
				mCityListShowData.resetLastAdd();
			}
			
		}
	};
	
	private void addINetResponseCode(){
		mCityListShowData.addINetResponseCode(mINetResponseCode);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (weatherDB != null) {
			weatherDB.close();
			weatherDB = null;
		}
		mCityListShowData.removeINetResponseCode(mINetResponseCode);
	}

}
