package com.aurora.voiceassistant.view;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.ManagedClientConnection;
import org.json.JSONObject;

import com.aurora.voiceassistant.R;
import com.aurora.voiceassistant.model.*;
import com.aurora.voiceassistant.model.ContactsItem.NumberType;
import com.aurora.voiceassistant.model.RspJsonReminderAlarm.ALARM_TYPE;
import com.aurora.voiceassistant.updateinterface.LayoutUpdateListener;
import com.aurora.voiceassistant.view.MainActivity.OFFLINE_TYPE;
import com.google.zxing.client.result.ISBNParsedResult;

import android.R.integer;
import android.R.layout;
import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.nfc.cardemulation.OffHostApduService;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.DropBoxManager.Entry;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.Settings.System;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import aurora.widget.AuroraEditText;
import aurora.widget.AuroraSwitch;

import com.android.calendarcommon2.EventRecurrence;

@SuppressLint("UseSparseArrays")
//public class ContentViewConstructor {
public class ContentViewConstructor implements LayoutUpdateListener {
	private Context context;
	private Handler mHandler;
	private LayoutInflater mLayoutInflater;
	
	private final int xmlnbDispStep = 5;
	private int xmlnbPreLoadMaxIndex = xmlnbDispStep;
	private int xmlnbScoreType = 0;
	
	Map<RspXmlPic, ArrayList<RspXmlPicItem>> rspXmlPictureItemMap = new HashMap<RspXmlPic, ArrayList<RspXmlPicItem>>();
	Map<RspXmlPic, Integer> rspXmlPicturePathMap = new HashMap<RspXmlPic, Integer>();
	private HashMap<String,Integer> xmlWeatherResMap = new HashMap<String, Integer>();
	private HashMap<String,String> xmlNearbySourceType = new HashMap<String, String>();
	private final String PIC_TEMP_PATH = "vstemp";
	
	private HashMap<Integer, Integer> weatherResMap = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> tabLayoutIdMap = new HashMap<Integer, Integer>();
//	private HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
	
	private MainActivity mainActivity;
	
	private int loadpageProgress = 0;
	private boolean loadPageFinished = false;
	private boolean loadPageError = false;
	private Runnable mWaitForPageLoadFinished;
	private Runnable mSendMessageRunnable;
	
	private int pathIndex = 0;
	
	//Offline start
	private ArrayList<ViewGroup> mLayoutArrayList = new ArrayList<ViewGroup>();
	private int messageViewIndex = 0;
	private HashMap<String, String> mLayoutIdHashMap = new HashMap<String, String>();
	private ArrayList<ContactsItem> mContactsItemList = null;
	private ArrayList<AppsItem> mAppsItemList = null;
	//Offline end
	
	//M:shigq fix bug #13340 start
	private AlertDialog mGeneralAlertDialog = null;
	//M:shigq fix bug #13340 end
	
	ContentViewConstructor(Context context, Handler handler) {
		this.context = context;
		this.mHandler = handler;
		mLayoutInflater = LayoutInflater.from(context);
		xmlWeatherInit();
		xmlNearbyInit();
		
		weatherResInit();
		tabLayoutIdInit();
	}
	
	private void xmlNearbyInit() {
		xmlNearbySourceType.put("10001", "美食");
		xmlNearbySourceType.put("10005", "休闲娱乐");
		xmlNearbySourceType.put("10002", "购物");
		xmlNearbySourceType.put("10008", "运动健身");
		xmlNearbySourceType.put("10004", "丽人");
		xmlNearbySourceType.put("10009", "结婚");
		xmlNearbySourceType.put("10010", "亲子");
		xmlNearbySourceType.put("10006", "酒店");
		xmlNearbySourceType.put("10003", "生活服务");
		xmlNearbySourceType.put("20001", "公厕");
	}
	
	private void xmlWeatherInit() {
		String WIND_BAOXUE = "暴雪";
		String WIND_DAXUE = "大雪";
		String WIND_DAXUE_BAOXUE = "大雪转暴雪";
		String WIND_XIAOXUE = "小雪";
		String WIND_XIAOXUE_ZHONGXUE = "小雪转中雪";
		String WIND_YUJIAXUE ="雨夹雪";
		String WIND_ZHENXUE ="阵雪";
		String WIND_ZHONGXUE= "中雪";
		String WIND_ZHONGXUE_DAXUE= "中雪转大雪";
		
		String WIND_BAOYU = "暴雨";
		String WIND_BAOYU_DABAOYU = "暴雨转大暴雨";
		String WIND_DABAOYU = "大暴雨";
		String WIND_DAYU = "大雨";
		String WIND_DAYU_BAOYU = "大雨转暴雨";
		String WIND_TEDABAOYU = "特大暴雨";
		String WIND_XIAOYU = "小雨";
		String WIND_XIAOYU_ZHONGYU = "小雨转中雨";
		String WIND_ZHENGYU = "阵雨";
		String WIND_ZHONGYU = "中雨";
		String WIND_ZHONGYU_DAYU ="中雨转大雨" ;
		String WIND_DABAOYU_TEDA ="大暴雨转特大暴雨" ;
		String WIND_ZHONGYU_DONGYU ="冻雨" ;
		
		String WIND_LEIZHENYU = "雷阵雨";
		String WIND_LEIZHENYU_BINGBAO = "雷阵雨伴有冰雹";
		String WIND_QING = "晴";
		String WIND_QING_DUOYUN = "晴转多云";
		String WIND_YANGSHA ="扬沙";
		String WIND_DUOYUN="多云";
		String WIND_DUOYUN_YIN="多云转阴";
		String WIND_DUOYUN_QING="多云转晴";
		String WIND_YIN ="阴";
		String WIND_YIN_DUOYUN ="阴转多云";
		String WIND_FUCHEN ="浮尘";
		String WIND_QIANGSHACHENBAO ="强沙尘暴";
		String WIND_SHACHENBAO ="沙尘暴";
		String WIND_WU ="雾";
		String WIND_MAI ="霾";
		
		xmlWeatherResMap.put(WIND_BAOXUE, R.drawable.vs_weather_baoxue_alarm);
		xmlWeatherResMap.put(WIND_DAXUE, R.drawable.vs_weather_daxue_alarm);
		xmlWeatherResMap.put(WIND_DAXUE_BAOXUE, R.drawable.vs_weather_daxue_baoxue_alarm);
		xmlWeatherResMap.put(WIND_XIAOXUE, R.drawable.vs_weather_xiaoxue_alarm);
		xmlWeatherResMap.put(WIND_XIAOXUE_ZHONGXUE, R.drawable.vs_weather_xiaoxun_zhongxue_alarm);
		xmlWeatherResMap.put(WIND_YUJIAXUE, R.drawable.vs_weather_yujiaxue_alarm);
		xmlWeatherResMap.put(WIND_ZHONGXUE, R.drawable.vs_weather_zhongxue_alarm);
		xmlWeatherResMap.put(WIND_ZHENXUE, R.drawable.vs_weather_zhenxue_alarm);
		xmlWeatherResMap.put(WIND_ZHONGXUE, R.drawable.vs_weather_zhongxue_alarm);
		xmlWeatherResMap.put(WIND_ZHONGXUE_DAXUE, R.drawable.vs_weather_zhongxue_daxue_alarm);
		xmlWeatherResMap.put(WIND_BAOYU, R.drawable.vs_weather_baoyu_alarm);
		xmlWeatherResMap.put(WIND_BAOYU_DABAOYU, R.drawable.vs_weather_baoyu_dabaoyu_alarm);
		xmlWeatherResMap.put(WIND_DABAOYU, R.drawable.vs_weather_dabaoyu_alarm);
		xmlWeatherResMap.put(WIND_DAYU, R.drawable.vs_weather_dayu_alarm);
		xmlWeatherResMap.put(WIND_DAYU_BAOYU, R.drawable.vs_weather_dayu_baoyu_alarm);
		xmlWeatherResMap.put(WIND_TEDABAOYU, R.drawable.vs_weather_tedabaoyu_alarm);
		xmlWeatherResMap.put(WIND_XIAOYU, R.drawable.vs_weather_xiaoyu_alarm);
		xmlWeatherResMap.put(WIND_XIAOYU_ZHONGYU, R.drawable.vs_weather_xiaoyu_zhongyu_alarm);
		xmlWeatherResMap.put(WIND_ZHENGYU, R.drawable.vs_weather_zhenyu_alarm);
		xmlWeatherResMap.put(WIND_ZHONGYU, R.drawable.vs_weather_zhongyu_alarm);
		xmlWeatherResMap.put(WIND_ZHONGYU_DAYU, R.drawable.vs_weather_zhongyu_dayu_alarm);
		xmlWeatherResMap.put(WIND_DABAOYU_TEDA, R.drawable.vs_weather_dabaoyu_teda_alarm);
		xmlWeatherResMap.put(WIND_LEIZHENYU, R.drawable.vs_weather_leizhenyu_alarm);
		xmlWeatherResMap.put(WIND_LEIZHENYU_BINGBAO, R.drawable.vs_weather_leizhenyu_bingbao_alarm);
		xmlWeatherResMap.put(WIND_ZHONGYU_DONGYU, R.drawable.vs_weather_dongyu_alarm);
		xmlWeatherResMap.put(WIND_QING, R.drawable.vs_weather_qing_alarm);
		xmlWeatherResMap.put(WIND_YANGSHA, R.drawable.vs_weather_yangsha_alarm);
		xmlWeatherResMap.put(WIND_DUOYUN, R.drawable.vs_weather_duoyun_alarm);
		xmlWeatherResMap.put(WIND_YIN, R.drawable.vs_weather_yin_alarm);
		xmlWeatherResMap.put(WIND_FUCHEN, R.drawable.vs_weather_fuchen_alarm);
		xmlWeatherResMap.put(WIND_QIANGSHACHENBAO, R.drawable.vs_weather_qiangshachenbao_alarm);
		xmlWeatherResMap.put(WIND_SHACHENBAO, R.drawable.vs_weather_shachenbao_alarm);
		xmlWeatherResMap.put(WIND_WU, R.drawable.vs_weather_wu_alarm);
		xmlWeatherResMap.put(WIND_MAI, R.drawable.vs_weather_mai_alarm);
		xmlWeatherResMap.put(WIND_DUOYUN_YIN, R.drawable.vs_weather_yin_alarm);
		xmlWeatherResMap.put(WIND_DUOYUN_QING, R.drawable.vs_weather_qing_alarm);
		xmlWeatherResMap.put(WIND_QING_DUOYUN, R.drawable.vs_weather_duoyun_alarm);
		xmlWeatherResMap.put(WIND_YIN_DUOYUN, R.drawable.vs_weather_duoyun_alarm);
	}
	
	private void weatherResInit() {
		weatherResMap.put(0, R.id.vr_1_day_img);
		weatherResMap.put(1, R.id.tem1);
		weatherResMap.put(2, R.id.date1);
		weatherResMap.put(3, R.id.week1);
		weatherResMap.put(4, R.id.vr_2_day_img);
		weatherResMap.put(5, R.id.tem2);
		weatherResMap.put(6, R.id.date2);
		weatherResMap.put(7, R.id.week2);
		weatherResMap.put(8, R.id.vr_3_day_img);
		weatherResMap.put(9, R.id.tem3);
		weatherResMap.put(10, R.id.date3);
		weatherResMap.put(11, R.id.week3);
		weatherResMap.put(12, R.id.vr_4_day_img);
		weatherResMap.put(13, R.id.tem4);
		weatherResMap.put(14, R.id.date4);
		weatherResMap.put(15, R.id.week4);
		weatherResMap.put(16, R.id.vr_5_day_img);
		weatherResMap.put(17, R.id.tem5);
		weatherResMap.put(18, R.id.date5);
		weatherResMap.put(19, R.id.week5);
		weatherResMap.put(20, R.id.vr_6_day_img);
		weatherResMap.put(21, R.id.tem6);
		weatherResMap.put(22, R.id.date6);
		weatherResMap.put(23, R.id.week6);
	}

	private void tabLayoutIdInit() {
		tabLayoutIdMap.put(0, R.id.tab1_content);
		tabLayoutIdMap.put(1, R.id.l_tab1);
		tabLayoutIdMap.put(2, R.id.l_img_tab1);
		tabLayoutIdMap.put(3, R.id.tab1);
		tabLayoutIdMap.put(4, R.id.text1);
		tabLayoutIdMap.put(5, R.id.tab2_content);
		tabLayoutIdMap.put(6, R.id.l_tab2);
		tabLayoutIdMap.put(7, R.id.l_img_tab2);
		tabLayoutIdMap.put(8, R.id.tab2);
		tabLayoutIdMap.put(9, R.id.text2);
		tabLayoutIdMap.put(10, R.id.tab3_content);
		tabLayoutIdMap.put(11, R.id.l_tab3);
		tabLayoutIdMap.put(12, R.id.l_img_tab3);
		tabLayoutIdMap.put(13, R.id.tab3);
		tabLayoutIdMap.put(14, R.id.text3);
		tabLayoutIdMap.put(15, R.id.tab4_content);
		tabLayoutIdMap.put(16, R.id.l_tab4);
		tabLayoutIdMap.put(17, R.id.l_img_tab4);
		tabLayoutIdMap.put(18, R.id.tab4);
		tabLayoutIdMap.put(19, R.id.text4);
		tabLayoutIdMap.put(20, R.id.tab5_content);
		tabLayoutIdMap.put(21, R.id.l_tab5);
		tabLayoutIdMap.put(22, R.id.l_img_tab5);
		tabLayoutIdMap.put(23, R.id.tab5);
		tabLayoutIdMap.put(24, R.id.text5);
	}
	
	/**
	 * to get the view for result by weather searching
	 * @param rsp
	 * @return
	 */
	public View getWeatherResultView(Response rsp) {
		Log.e("DEBUG", "getWeatherResultView");
		int index = 0;
		Response.Item rspItem;
      
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		setListItemHiddenData(rsp,convertView,R.drawable.vs_nearby_icon_tianqi);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取xmlpic所需要的信息
			rspItem = resultIter.next();
			
			switch(rspItem.getType()) {
			case CFG.VIEW_TYPE_WEBVIEW:
				boolean isFocus = (0 == index) ? true:false;
				if(!addWebView(rspItem,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
					continue;
				}
				break;
			case CFG.VIEW_TYPE_XML_TIANQI:
				RspXmlTq rspXmltq = rspItem.getRspxmltq();
				String address = rspXmltq.getAddress();
				if(null == address) continue;
				
				int size = rspXmltq.getListSize();
				if(size <= 0) continue;
				size = (size>7)? 7:size;
				
		        View view = mLayoutInflater.inflate(R.layout.vs_weather_content_layout, null);
		       
		        LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_weather,R.drawable.vs_icon_weather_sl,
		        									R.string.vs_tianqi,true,rsp.tabDataArray, null, null);
		   		contentTabLayout.addView(view);
		   		
		   		for (int i = 0; i < size; i++) {
		   			if (i == 0) {
		   				ImageView image = (ImageView)convertView.findViewById(R.id.vr_weather_today_img);
	        			TextView addr = (TextView) convertView.findViewById(R.id.location_text);
	        			TextView temp = (TextView) convertView.findViewById(R.id.today_tem);
	        			TextView date = (TextView) convertView.findViewById(R.id.today_date_text);
	        			TextView week = (TextView) convertView.findViewById(R.id.today_of_week_text);
	        			
	        			xmltqSetData(rspXmltq,i,addr,image,temp,null,date,week);
		   			} else {
		   				ImageView image = (ImageView)convertView.findViewById(weatherResMap.get((i-1)*4));
	        			TextView hTemp = (TextView) convertView.findViewById(weatherResMap.get((i-1)*4 + 1));
	        			TextView date = (TextView) convertView.findViewById(weatherResMap.get((i-1)*4 + 2));
	        			TextView week = (TextView) convertView.findViewById(weatherResMap.get((i-1)*4 + 3));
	        			
	        			xmltqSetData(rspXmltq,i,null,image,hTemp,null,date,week);
		   			}
		   		}
				break;
			}
			index++;
		}
		return convertView;
	}

	
	private void xmltqSetData(RspXmlTq rspXmltq,int index,TextView addr,ImageView image,TextView hTemp,TextView lTemp,TextView date,TextView week) {
		
		RspXmlTq.Item item = rspXmltq.getList().get(index);
		String dateStr = item.getDate();
		dateStr = dateStr.substring(dateStr.indexOf("-")+1);
		
		if(0 == index) {
			addr.setText(rspXmltq.getAddress());
			hTemp.setText(item.getHigh()+" /"+item.getLow()+"℃");
			date.setText(dateStr);
			week.setText("("+item.getWeek()+")");
			image.setImageResource(xmltqGetImgId(item.getDescription()));
		} else {
			//image
			image.setImageResource(xmltqGetImgId(item.getDescription()));			
			//lTemp
//			lTemp.setText(item.getLow()+"°");
			hTemp.setText(item.getHigh()+" /"+item.getLow()+"℃");
			//date
			date.setText(dateStr);
			//week
			week.setText(item.getWeek());
		}
	}
	
	private int xmltqGetImgId(String description) {
		int imgId = R.drawable.vs_weather_duoyun_alarm;
		try {
			imgId = xmlWeatherResMap.get(description);
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		return imgId;
	}
	
	/**
	 * to get the view for result by nearby searching
	 * @param rsp
	 * @return
	 */
	public View getNearbyResultView(Response rsp) {
		Log.e("DEBUG", "getNearbyResultView");
		int index = 0;
		Response.Item rspItem;	
		
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		final View conView = convertView;
		setListItemHiddenData(rsp,convertView,R.drawable.vs_pickup_vr_nearby_icon);
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		
//		webviewHashMap.clear();
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		while(resultIter.hasNext()) {
			//提取xmlpic所需要的信息
			rspItem = resultIter.next();
			
			switch(rspItem.getType()) {
				case CFG.VIEW_TYPE_WEBVIEW:
					boolean isFocus = (0 == index) ? true:false;
					if(!addWebView(rspItem,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
						continue;
					}
					break;
				case CFG.VIEW_TYPE_XML_NB:
					final RspXmlNb rspXmlnb = rspItem.getRspxmlnb();
					int size = rspXmlnb.getListSize();
					if(size <= 0) continue;
										
			        final View view = mLayoutInflater.inflate(R.layout.vs_nearby_shop_layout, null);
			        LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_nearby,R.drawable.vs_icon_nearby_sl,
			        									R.string.vs_zhoubian,true,rsp.tabDataArray, null, null);
			        contentTabLayout.addView(view);
	        		
//	        		final LinearLayout orderSel=(LinearLayout)convertView.findViewById(R.id.nearby_shop_order_select_linearlayout);
	        		
	        		Spinner mSpinner = (Spinner) view.findViewById(R.id.vs_spinner);
	        		String[] item = mainActivity.getResources().getStringArray(R.array.vs_near_shop_sort);
	        		
	        		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(context, R.layout.vs_simple_spinner_item, item) {
	        			@Override  
	                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
	        				convertView = mLayoutInflater.inflate(R.layout.vs_spinner_drop_down_item, null);
	                        TextView label = (TextView) convertView.findViewById(R.id.textview);
	                        label.setText(getItem(position));
	                          
	                        return convertView;
	        			}
	        		};
	        		
	        		mSpinner.setAdapter(mAdapter);
	        		mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
	        			public void onItemSelected(AdapterView<?> parent, View mview, int position, long id) {   
	                        // TODO Auto-generated method stub   
	                          Log.d("DEBUG", "setOnItemSelectedListener------position = "+position);
	                          switch (position) {
								case 0:
									rspXmlnb.sortByScore();
//									orderSel.setVisibility(View.GONE);
//									scoreTextView.setText(R.string.vs_score);
									xmlnbPreLoadMaxIndex = xmlnbDispStep;
									xmlnbRemoveAllItem(conView,rspXmlnb,(LinearLayout)view,0,xmlnbPreLoadMaxIndex-1);
									break;
								case 1:
									rspXmlnb.sortByDistance();
//									orderSel.setVisibility(View.GONE);
//									scoreTextView.setText(R.string.vs_distancemin);
									xmlnbPreLoadMaxIndex = xmlnbDispStep;
									xmlnbRemoveAllItem(conView,rspXmlnb,(LinearLayout)view,0,xmlnbPreLoadMaxIndex-1);					
									break;
								case 2:
									rspXmlnb.sortByPrice();
//									orderSel.setVisibility(View.GONE);
//									scoreTextView.setText(R.string.vs_pricemin);
									xmlnbPreLoadMaxIndex = xmlnbDispStep;
									xmlnbRemoveAllItem(conView,rspXmlnb,(LinearLayout)view,0,xmlnbPreLoadMaxIndex-1);
									break;
	
								default:
									break;
							}
	                    }   
	                    public void onNothingSelected(AdapterView<?> parent) {   
	                        // TODO Auto-generated method stub   
	                    }  
					});
	        		
	        		TextView typeTextView = (TextView)convertView.findViewById(R.id.nearby_shop_type_textview);//	        		
	        		typeTextView.setText(xmlNearbySourceType.get(rspXmlnb.getSourceType()));

	        		xmlnbScoreType = Integer.parseInt(rspXmlnb.getRankType());
	        		
	        		switch(xmlnbScoreType) {
		        		case RspXmlNb.RANK_TYPE_PRICE:
//		        			scoreTextView.setText(R.string.vs_pricemin);
		        			break;
		        		case RspXmlNb.RANK_TYPE_DISTA:
//		        			scoreTextView.setText(R.string.vs_distancemin);
		        			break;
		        		case RspXmlNb.RANK_TYPE_STAR:
//		        			scoreTextView.setText(R.string.vs_star);
		        			break;
		        		case RspXmlNb.RANK_TYPE_SCORE:
//		        			scoreTextView.setText(R.string.vs_score);
		        			break;
	        		}
	        		
	        		xmlnbInsertItem(rspXmlnb,(LinearLayout)view,0,xmlnbPreLoadMaxIndex-1);
	        		
	        		LinearLayout dispMoreLyaout = (LinearLayout)contentTabLayout.findViewById(R.id.nearby_shop_search_more_linearlayout);
	        		dispMoreLyaout.setOnClickListener(new OnClickListener() {
	       				@Override
	       				public void onClick(View v) {
	       					int oldIndex = xmlnbPreLoadMaxIndex;
	       					if(xmlnbPreLoadMaxIndex>rspXmlnb.getListSize()) {
	       						return ;
	       					}
	       					
	       					xmlnbPreLoadMaxIndex += xmlnbDispStep;
	       					if(xmlnbPreLoadMaxIndex>rspXmlnb.getListSize()) {
	       						xmlnbPreLoadMaxIndex = rspXmlnb.getListSize();
	       					}
	       					
	       					xmlnbInsertItem(rspXmlnb,(LinearLayout)view,oldIndex,xmlnbPreLoadMaxIndex-1);
	       				}
	        		});
					break;
			}
			index++;
		}
			
		return convertView;
	}
	
	private void xmlnbRemoveAllItem(View convertView,RspXmlNb rspXmlnb,LinearLayout layout,int sIndex,int eIndex) {
		LinearLayout itemcontentLayout = (LinearLayout)layout.findViewById(R.id.nearby_shop_item);
		itemcontentLayout.removeAllViews();
		xmlnbInsertItem(rspXmlnb,layout,0,eIndex);
	}
	
	private void xmlnbInsertItem(RspXmlNb rspXmlnb,LinearLayout layout,int sIndex,int eIndex) {
		int index = 0;
		Iterator<RspXmlNb.Item> iter = rspXmlnb.getList().iterator();
		while(iter.hasNext()) {
			RspXmlNb.Item item = iter.next();
			
			if(index < sIndex) {
				index++;
				continue;
			}
			
			index++;
			
			//add item
			final View itemView = mLayoutInflater.inflate(R.layout.vs_nearby_shop_item_layout, null);
			LinearLayout itemcontentLayout = (LinearLayout)layout.findViewById(R.id.nearby_shop_item);
			itemcontentLayout.addView(itemView);
			
			LinearLayout itemLayout = (LinearLayout)itemView.findViewById(R.id.nearby_shop_item_layout);
			itemLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LinearLayout itemExpLayout = (LinearLayout)v.findViewById(R.id.nearby_shop_exp_item);
					int visible = itemExpLayout.getVisibility();
					
					visible =(visible == View.VISIBLE) ? View.GONE : View.VISIBLE;
					itemExpLayout.setVisibility(visible);
				}
			});
			
			TextView shopName = (TextView)itemView.findViewById(R.id.shop_name_textview);
			RatingBar star = (RatingBar)itemView.findViewById(R.id.shop_rating_ratingbar);
			TextView average = (TextView)itemView.findViewById(R.id.shop_average_peopensity_textview);
			TextView distance = (TextView)itemView.findViewById(R.id.shop_distance_textview);
			TextView shopTags = (TextView)itemView.findViewById(R.id.shop_tags_textview);
			
			float starLeve = (float) ((float)(Integer.parseInt(item.getStar())/10.0));
			//String ave = "人均: ￥" +item.getAvgPrice();
			String ave = null;
			if(item.getAvgPrice()!=null&&!"暂无".equals(item.getAvgPrice())){
				ave = "人均: ￥" +item.getAvgPrice();
			} else {
				ave = "人均: 暂无";
			}
			String dist = item.getDistance();
			
			int offset = dist.indexOf(".");
			if(offset>0) {
				dist = dist.substring(0, offset-1);
			}
			shopName.setText(item.getShopName());
			star.setRating(starLeve);
			average.setText(ave);
			if("NULL".equals(dist)) {
				distance.setText("0m");
			} else {
				distance.setText(dist+"m");
			}
		
			shopTags.setText(item.getCategory());
			
			//exp
			TextView location = (TextView)itemView.findViewById(R.id.shop_location_textview);
			//TextView end = (TextView)itemView.findViewById(R.id.shop_line_end_textview);
			//TextView start = (TextView)itemView.findViewById(R.id.shop_line_start_textview);
			TextView tel = (TextView)itemView.findViewById(R.id.shop_telephone_textview);
			TextView recomment = (TextView)itemView.findViewById(R.id.shop_recomment_dish_first_textview);
			//TextView telsave = (TextView)itemView.findViewById(R.id.shop_telephone_save_textview);
			//TextView forwardmsg = (TextView)itemView.findViewById(R.id.shop_forward_message_textview);
			//TextView more = (TextView)itemView.findViewById(R.id.shop_more_info_textview);
			
			location.setText(item.getAddress());
			
			if("NULL".equals(item.getDishTags())) {
				LinearLayout recommentLayout = (LinearLayout) itemView.findViewById(R.id.shop_recomment_dish_first_line_linearlayout);
				recommentLayout.setVisibility(View.GONE);
			} else {
				recomment.setText(item.getDishTags());
			}
			
			String telStr = item.getPhoneNo();
			if("NULL".equals(telStr)) {
				tel.setText(context.getResources().getString(R.string.vs_notel));
				tel.setClickable(false);
			} else {
				tel.setText(telStr);
				
				RelativeLayout telLayout = (RelativeLayout) itemView.findViewById(R.id.shop_telephone_relativelayout);
				telLayout.setOnClickListener(new onClickListener(telStr) {
					@Override
					public void onClick(View v) {
						String telNumber = (String)data;
						Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + telNumber));
						context.startActivity(intent);
					}
				});
			}
			
			RelativeLayout locatLayout = (RelativeLayout) itemView.findViewById(R.id.shop_location_relativelayout);
			locatLayout.setOnClickListener(new onClickListener(item.getCoordUrl()) {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,WebviewActivity.class);
					intent.putExtra("url", (String)data);
					context.startActivity(intent);
				}
			});
			
			RelativeLayout endLayout = (RelativeLayout) itemView.findViewById(R.id.shop_line_end_relativelayout);
			endLayout.setOnClickListener(new onClickListener(item.getGotoUrl()) {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(context,WebviewActivity.class);
							intent.putExtra("url", (String)data);
							context.startActivity(intent);
						}
					});
			
			RelativeLayout startLayout = (RelativeLayout) itemView.findViewById(R.id.shop_line_start_relativelayout);
			startLayout.setOnClickListener(new onClickListener(item.getLeftUrl()) {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,WebviewActivity.class);
					intent.putExtra("url", (String)data);
					context.startActivity(intent);
					
				}
			});
			
			
			LinearLayout telsaveLayout = (LinearLayout) itemView.findViewById(R.id.shop_telephone_save_linearlayout);
			telsaveLayout.setOnClickListener(new onClickListener(item.getShopName()+"-"+item.getPhoneNo()) {
				@Override
				public void onClick(View v) {
					String str = (String)data;
					int index = str.indexOf("-");
					String telNumber = str.substring(index+1);
					String shopName = str.substring(0,index);
					/*//查看 
					 * 查看联系人界面
					 * 联系人名称 ：店名
					 * 号码：店铺电话
					 */
					Intent intent = new Intent(Intent.ACTION_INSERT, android.provider.ContactsContract.Contacts.CONTENT_URI);
					intent.putExtra(Insert.NAME, shopName);//名字显示在名字框
						
					if(!"NULL".equals(telNumber)) {
						intent.putExtra(Insert.PHONE,telNumber.trim());//号码显示在号码框
					}
					
					context.startActivity(intent);
				}
			});
			
			String contentSms= null;
			String name =  item.getShopName();
			String address =  item.getAddress();
			String phone =  item.getPhoneNo();
			
			if(null != name && 0 != name.length()) {
				contentSms = item.getShopName()+" ";
			}
			if(null != address && 0 != address.length()) {
				contentSms += context.getResources().getString(R.string.vs_address)+item.getAddress()+" ";
			}
			if(!"NULL".equals(phone)) {
				contentSms += context.getResources().getString(R.string.vs_phone)+item.getPhoneNo();
			}
			LinearLayout forwardmsgLayout = (LinearLayout) itemView.findViewById(R.id.shop_forward_message_linearlayout);
			forwardmsgLayout.setOnClickListener(new onClickListener(contentSms) {
				@Override
				public void onClick(View v) {
					
					/*//转发  
					 * 发短信界面
					 * 短信内容:店名+地址+电话
					 * 
					 */
					 Uri smsToUri = Uri.parse("smsto:");  
					 Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);  
					 intent.putExtra("sms_body", (String)data);  
					 context.startActivity(intent);
				}
			});
			LinearLayout moreLayout = (LinearLayout) itemView.findViewById(R.id.shop_more_info_linearlayout);
			moreLayout.setOnClickListener(new onClickListener(item.getShopUrl()) {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,WebviewActivity.class);
					intent.putExtra("url",(String)data);
					context.startActivity(intent);
				}
			});
			
			if(index > eIndex) return ;
		}
	}
	
	/**
	 * to get the view for result by picture searching
	 * @param rsp
	 * @return
	 */
	public View getPictureResultView(Response rsp) {
		Log.e("DEBUG", "getPictureResultView");
		int index = 0;
		String description = null;
		Response.Item item;
		
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		
		setListItemHiddenData(rsp,convertView,R.drawable.vs_pickup_vr_nearby_icon_pic);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext() && index<5) {
			//提取xmlpic所需要的信息
			item = resultIter.next();
			
			switch(item.getType()) {
				case CFG.VIEW_TYPE_WEBVIEW:
					boolean isFocus = (0 == index) ? true:false;
					
					if(!addWebView(item,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
						continue;
					}
					break;
				case CFG.VIEW_TYPE_XML_PIC:
					final RspXmlPic rspXmlpic = item.getRspxmlpic();
					description = rspXmlpic.getDescription();
					if(null == description) continue;
					
					if(rspXmlpic.getListSize() <= 0) continue;
					
					ArrayList<RspXmlPicItem> node = rspXmlPictureItemMap.get(rspXmlpic);
					
					ArrayList<RspXmlPicItem> value = new ArrayList<RspXmlPicItem>();
					if(null == node) {
//						ArrayList<RspXmlPicItem> value = new ArrayList<RspXmlPicItem>();
						for(int j = 0; j < rspXmlpic.getListSize(); j++) {
							value.add(rspXmlpic.getList().get(j));
						}
				
						rspXmlPictureItemMap.put(rspXmlpic, value);
						
						pathIndex++;
						rspXmlPicturePathMap.put(rspXmlpic, pathIndex);
					}
					
					View view = mLayoutInflater.inflate(R.layout.vs_xml_pic_layout, null);
			        LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_image,R.drawable.vs_icon_image_sl,
			        									R.string.vs_tupian,true,rsp.tabDataArray, null, null);
			        contentTabLayout.addView(view);
			        
			        String tempPathString = "/temp"+rspXmlPicturePathMap.get(rspXmlpic)+"/";				
	        		PicGridAdapter adapter = new PicGridAdapter(context, rspXmlPictureItemMap.get(rspXmlpic), PIC_TEMP_PATH + tempPathString);
	        		
//	        		View convertView2 = mLayoutInflater.inflate(R.layout.vs_image_layout, null);
	        		for(int j = 0; j < rspXmlpic.getListSize(); j++) {
//	        			ImageView image = (ImageView)convertView2.findViewById(R.id.image);
	        			ImageView image = new ImageView(context);
	        			new ImageDownloadTask().execute(rspXmlPictureItemMap.get(rspXmlpic).get(j).getImagelink(), 
	        					image, j, rspXmlPictureItemMap.get(rspXmlpic), adapter, tempPathString);
					}
	        		
	        		GridView gridview = (GridView)convertView.findViewById(R.id.xml_pic_gridview);
//			        gridview.setAdapter(adapter);
			        gridview.setOnItemClickListener(new GridItemClickListener(rspXmlpic));//, rspXmlPictureItemMap));
			        gridview.setAdapter(adapter);
			        
			        TextView text = (TextView)convertView.findViewById(R.id.xml_pic_text);
			        text.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(context,WebviewActivity.class);
							intent.putExtra("url", rspXmlpic.getMorelink());
							context.startActivity(intent);
						}
					});
					break;
			}
			index++;
		}
		
		//holder.tabHost.setCurrentTab(0);
		return convertView;
	}
	
	/**
	 * to get the view for result by train message searching
	 * @param rsp
	 * @return
	 */
	public View getTrainMessageResultView(Response rsp) {
		Log.e("DEBUG", "getTrainMessageResultView");
		int index = 0;
	    Response.Item rspItem;
			
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		setListItemHiddenData(rsp,convertView,R.drawable.vs_nearby_icon_lc);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取xmlpic所需要的信息
			rspItem = resultIter.next();
			switch(rspItem.getType()) {
				case CFG.VIEW_TYPE_WEBVIEW:
					boolean isFocus = (0 == index) ? true:false;
					if(!addWebView(rspItem,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
						continue;
					}
					break;
				case CFG.VIEW_TYPE_XML_CX_MSG_LC:
					
					RspXmlCxMsgLc rspXmlGoMsgLc = rspItem.getRspxmlcxmsglc();
					int size = rspXmlGoMsgLc.getListSize();
					if(size <= 0) continue;
					
			        View view = mLayoutInflater.inflate(R.layout.vs_train_trainnumber_layout, null);
			        LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_longdistance_car,
			        									R.drawable.vs_icon_longdistance_car_sl, R.string.vs_lc,true,rsp.tabDataArray, null, null);
			   		contentTabLayout.addView(view);
			   		
	        		final RspXmlCxMsgLc.Item item = rspXmlGoMsgLc.getList().get(0);
	        		
	        		RelativeLayout reLayout = (RelativeLayout)convertView.findViewById(R.id.vr_train_trainnumber_layout);
	        		reLayout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(context,WebviewActivity.class);
							intent.putExtra("url", item.getUrl());
							context.startActivity(intent);
						}
					});
	        		
	        		TextView number = (TextView)convertView.findViewById(R.id.train_number_textview);
	        		TextView name = (TextView)convertView.findViewById(R.id.train_name_textview);
	        		TextView sStation = (TextView)convertView.findViewById(R.id.train_departure_station_textview);
	        		TextView eStation = (TextView)convertView.findViewById(R.id.train_arrival_station_textview);
	        		TextView sTime = (TextView)convertView.findViewById(R.id.train_departure_time_textview);
	        		TextView eTime = (TextView)convertView.findViewById(R.id.train_arrival_time_textview);
	        		TextView fPrice = (TextView)convertView.findViewById(R.id.train_first_price_textview);
	        		TextView ePrice = (TextView)convertView.findViewById(R.id.train_second_price_textview);
	        		TextView spendTime = (TextView)convertView.findViewById(R.id.train_spend_time_textview);
	        		
	        		number.setText(item.getNumber());
	        		name.setText(item.getName());
	        		sStation.setText(item.getsStation());
	        		eStation.setText(item.geteStation());
	        		sTime.setText(item.getsTime());
	        		eTime.setText(item.geteTime());
	        		fPrice.setText(item.getfPrice());
	        		ePrice.setText(item.getsPrice());
	        		spendTime.setText(item.getSpendTime());
					break;
			}
			index++;
		}
		return convertView;
	}
	
	/**
	 * to get the view for result by train result searching
	 * @param rsp
	 * @return
	 */
	public View getTrainResultView(Response rsp) {
		Log.e("DEBUG", "getTrainResultView");
		int index = 0;
		Response.Item rspItem;
      
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		setListItemHiddenData(rsp,convertView,R.drawable.vs_nearby_icon_lc);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取xmlpic所需要的信息
			rspItem = resultIter.next();
			switch(rspItem.getType()) {
				case CFG.VIEW_TYPE_WEBVIEW:
					boolean isFocus = (0 == index) ? true:false;
					if(!addWebView(rspItem,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
						continue;
					}
					break;
				case CFG.VIEW_TYPE_XML_CX_LC:
					final RspXmlCxLc rspXmlCxLc = rspItem.getRspxmlcxlc();
					
			        View view = mLayoutInflater.inflate(R.layout.vs_train_stationtostation_header_layout, null);
			        LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_train,R.drawable.vs_icon_train_sl,
			        									R.string.vs_lece,true,rsp.tabDataArray, null, null);
			   		contentTabLayout.addView(view);
			   		
	        		int size = rspXmlCxLc.getListSize();
	        		
	        		TextView start = (TextView)convertView.findViewById(R.id.departure_station_textview);
	        		TextView end = (TextView)convertView.findViewById(R.id.arrival_station_textview);
	        		start.setText(rspXmlCxLc.getTrip_from());
	        		end.setText(rspXmlCxLc.getTrip_to());
	        		
	        		if(0 == size) {
	        			TextView noinfo = (TextView)convertView.findViewById(R.id.no_long_bus_textview);
	        			noinfo.setVisibility(View.VISIBLE);
	        		} else {
	        			RspXmlCxLc.Item item = null;
	        			LinearLayout itemLayout = (LinearLayout)convertView.findViewById(R.id.cxlc_item);
	        			
	        			Iterator<RspXmlCxLc.Item> iter = rspXmlCxLc.getList().iterator();
	        			while(iter.hasNext()) {
	        				item = iter.next();
	        				final String booking = item.getBooking();
	        				final String linkurl = item.getLinkurl();
	        				
	        				View itemView = mLayoutInflater.inflate(R.layout.vs_train_stationtostation_item_layout, null);
	        				itemLayout.addView(itemView);
	        				
	        				LinearLayout itemSubLayout = (LinearLayout)itemView.findViewById(R.id.vr_train_item_linearlayout);
	        				itemSubLayout.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									if(null == booking || 0 == booking.length()) return ;
									Intent intent = new Intent(context,WebviewActivity.class);
									intent.putExtra("url", linkurl);
									context.startActivity(intent);
								}
							});
	        				
	        				
	        				TextView number = (TextView)itemView.findViewById(R.id.train_number_textview);
	        				TextView depTime = (TextView)itemView.findViewById(R.id.train_departure_time_textview);
	        				TextView arrivalTime = (TextView)itemView.findViewById(R.id.train_arrival_time_textview);
	        				
	        				number.setText(item.getName());
	        				depTime.setText(item.getsTime());
	        				arrivalTime.setText(item.geteTime());
	        				
	        				TextView depStation = (TextView)itemView.findViewById(R.id.train_departure_station_textview);
	        				TextView arraStaiton = (TextView)itemView.findViewById(R.id.train_arrival_station_textview);
	        				TextView spendTime = (TextView)itemView.findViewById(R.id.train_spend_time_textview);
	        				
	        				depStation.setText(item.getsStation());
	        				arraStaiton.setText(item.geteStation());
	        				spendTime.setText(item.getSpendTime());
	        				
	        				TextView fPrice = (TextView)itemView.findViewById(R.id.train_first_price_textview);
	        				TextView sPrice = (TextView)itemView.findViewById(R.id.train_second_price_textview);
	        				LinearLayout orderLayout = (LinearLayout)itemView.findViewById(R.id.train_order_linearlayout);
	        				orderLayout.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									if(null == booking || 0 == booking.length()) return ;
									Intent intent = new Intent(context,WebviewActivity.class);
									intent.putExtra("url", booking);
									context.startActivity(intent);
								}
							});
	        				
	        				fPrice.setText(item.getfPrice());
	        				sPrice.setText(item.getsPrice());
	        			}
	        			
	        			RelativeLayout moreLayout = (RelativeLayout)convertView.findViewById(R.id.vr_train_search_more_view);
	        			moreLayout.setVisibility(View.VISIBLE);
	        			
	        			TextView moreText = (TextView)convertView.findViewById(R.id.vr_train_search_more_info_textview);
	        			moreText.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(context,WebviewActivity.class);
								intent.putExtra("url", rspXmlCxLc.getAllurl());
								context.startActivity(intent);
							}
						});
	        		}
					break;
			}
			index++;
		}
		return convertView;
	}
	
	/**
	 * to get the view for result by bus result searching
	 * @param rsp
	 * @return
	 */
	public View getBusResultView(Response rsp) {
		Log.e("DEBUG", "getBusResultView");
		int index = 0;
		Response.Item rspItem;
      
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		setListItemHiddenData(rsp,convertView,R.drawable.vs_nearby_icon_lc);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取xmlpic所需要的信息
			rspItem = resultIter.next();
			switch(rspItem.getType()) {
				case CFG.VIEW_TYPE_WEBVIEW:
					boolean isFocus = (0 == index) ? true:false;
					if(!addWebView(rspItem,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
						continue;
					}
					break;
				case CFG.VIEW_TYPE_XML_CX_KC:
					
					RspXmlCxKc.Item item = null;
					
					final RspXmlCxKc rspXmlCxKc = rspItem.getRspxmlcxkc();
			        View view = mLayoutInflater.inflate(R.layout.vs_long_bus_stationtostation_layout, null);
			        LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_longdistance_car,R.drawable.vs_icon_longdistance_car_sl,
			        											R.string.vs_kece,true,rsp.tabDataArray, null, null);
			   		contentTabLayout.addView(view);
	        		
	        		int size = rspXmlCxKc.getListSize();
	        		
	        		TextView start = (TextView)convertView.findViewById(R.id.long_bus_departure_station_textview);
	        		TextView end = (TextView)convertView.findViewById(R.id.long_bus_arrival_station_textview);
	        		start.setText(rspXmlCxKc.getTrip_from());
	        		end.setText(rspXmlCxKc.getTrip_tod());
	        		
	        		if(0 == size) {
	        			TextView noinfo = (TextView)convertView.findViewById(R.id.no_long_bus_textview);
	        			noinfo.setVisibility(View.VISIBLE);
	        		} else {
	        			LinearLayout busInfo_layout = (LinearLayout)convertView.findViewById(R.id.vr_long_bus_list_linearlayout);
	        			busInfo_layout.setVisibility(View.VISIBLE);
	        			
	        			LinearLayout item_layout = (LinearLayout)convertView.findViewById(R.id.long_bus_list_linearlayout);
	        			
	        			Iterator<RspXmlCxKc.Item> iter = rspXmlCxKc.getList().iterator();
	        			if(iter.hasNext()) iter.next();
	        			
	        			while(iter.hasNext()) {
	        				View itemView = mLayoutInflater.inflate(R.layout.vs_long_bus_list_item_layout, null);
	        				item_layout.addView(itemView);
	        				
	        				TextView col0 = (TextView)itemView.findViewById(R.id.list_item_station_textview);
		        			TextView col1 = (TextView)itemView.findViewById(R.id.list_item_departure_time_textview);
		        			TextView col2 = (TextView)itemView.findViewById(R.id.list_item_ticket_price_textview);
	        				
	        				item = iter.next();

	        				col0.setText(item.getCol0());
	        				col1.setText(item.getCol1());
	        				col2.setText(item.getCol2());
	        			}
	        			
	        			LinearLayout more_layout = (LinearLayout)convertView.findViewById(R.id.long_bus_search_more_linearlayout);
	        			more_layout.setVisibility(View.VISIBLE);
	        			
	        			LinearLayout textlayout = (LinearLayout)convertView.findViewById(R.id.long_bus_search_more_linearlayout);
	        			textlayout.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(context,WebviewActivity.class);
								intent.putExtra("url", rspXmlCxKc.getLinkurl());
								context.startActivity(intent);
							}
						});
	        		}
					break;
			}
			index++;
		}
		return convertView;
	}
	
	/**
	 * to get the view for result by flight result searching
	 * @param rsp
	 * @return
	 */
	public View getFlightResultView(Response rsp) {
		Log.e("DEBUG", "getFlightResultView");
		int index = 0;
		Response.Item rspItem;
      
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		setListItemHiddenData(rsp,convertView,R.drawable.vs_nearby_icon_hb);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取xmlpic所需要的信息
			rspItem = resultIter.next();
			switch(rspItem.getType()) {
				case CFG.VIEW_TYPE_WEBVIEW:
					boolean isFocus = (0 == index) ? true:false;
					if(!addWebView(rspItem,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
						continue;
					}
					break;
				case CFG.VIEW_TYPE_XML_CX_HB:
				   	//出行-航班
					RspXmlCxHb rspXmlcxhb = rspItem.getRspxmlcxhb();
			   		WebView webview = new WebView(context);
			   		webview.getSettings().setJavaScriptEnabled(true);
					//Add storage for web cache
			   		webview.getSettings().setDomStorageEnabled(true);
			   		
					webview.setId(index);
			   		webview.loadUrl(rspXmlcxhb.getLinkUrl());
			   		
			   	    LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_plane,R.drawable.vs_icon_plane_sl,
			   	    									R.string.vs_hangban,true,rsp.tabDataArray, null, null);
			   		contentTabLayout.addView(webview);
				   		

			}
			index++;
		}
		return convertView;
	}
	
	/**
	 * to get the view for result by baike searching
	 * @param rsp
	 * @return
	 */
	public View getBaikeResultView(Response rsp) {
		Log.e("DEBUG", "getBaikeResultView");
		int index = 0;
        Response.Item rspItem;
      
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		setListItemHiddenData(rsp,convertView,R.drawable.pickup_vr_nearby_icon_public);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取xmlpic所需要的信息
			rspItem = resultIter.next();
			switch(rspItem.getType()) {
				case CFG.VIEW_TYPE_WEBVIEW:
					boolean isFocus = (0 == index) ? true:false;
					if(!addWebView(rspItem,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
						continue;
					}
					break;
				case CFG.VIEW_TYPE_XML_BAIKE:
					 
					final RspXmlBk rspXmlbk = rspItem.getRspxmlbk();
					 
					View view = mLayoutInflater.inflate(R.layout.vs_knowledge_mapping_star_feature_item_layout, null);
			        LinearLayout contentTabLayout = setTabData(convertView,index,R.drawable.vs_icon_encyclopedia,R.drawable.vs_icon_encyclopedia_sl,
			        											R.string.vs_baike,true,rsp.tabDataArray, null, null);
			   		contentTabLayout.addView(view);
			   		
	        		
	        		TextView title = (TextView)convertView.findViewById(R.id.star_feature_title_textview);
	        		TextView content = (TextView)convertView.findViewById(R.id.star_feature_content_textview);
	        		LinearLayout moreLayout = (LinearLayout)convertView.findViewById(R.id.star_feature_search_more_linearlayout);
	        		
	        		title.setText(rspXmlbk.getKey());
	        		content.setText(rspXmlbk.getContent());
	        		moreLayout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(context,WebviewActivity.class);
							intent.putExtra("url", rspXmlbk.getMoreUrl());
							context.startActivity(intent);
						}
					});
	        		break;
			}
			index++;
		}
		
		return convertView;
	}
	
	/**
	 * to get the view for result by web result searching
	 * @param rsp
	 * @return
	 */
	public View getWebviewResultView(Response rsp) {
		Log.e("DEBUG", "getWebviewResultView");
		int index = 0;
        Response.Item result;
        boolean isFocus = false;
		
		View convertView =  mLayoutInflater.inflate(R.layout.vs_list_item_tab_layout,null);
		setListItemHiddenData(rsp,convertView,R.drawable.pickup_vr_nearby_icon_public);
		
		HashMap<Integer,String> webviewHashMap = new HashMap<Integer, String>();
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取webview所需要的信息
			result = resultIter.next();
			isFocus = (0 == index) ? true:false;
			if(!addWebView(result,index,convertView,isFocus,rsp.tabDataArray, webviewHashMap)) {
				continue;
			}
			index++;
		}
		
		return convertView;
	}
	
	/**
	 * to get the view for result by sogoumapurl result searching
	 * @param rsp
	 * @return
	 */
	public View getSoGouMapUrlResultView(Response rsp) {
		int index = 0;
        Response.Item result;
        boolean isFocus = false;
		View convertView =  mLayoutInflater.inflate(R.layout.vs_sogoumapurl_layout,null);
//		sogoumapurl_layout
//		TextView mTextView = (TextView)convertView.findViewById(R.id.sogoumapurl_text);
		RelativeLayout mLayout = (RelativeLayout)convertView.findViewById(R.id.sogoumapurl_layout);
		
		String url = null;
		
		Iterator<Response.Item>resultIter = rsp.getList().iterator();
		while(resultIter.hasNext()) {
			//提取webview所需要的信息
			/*result = resultIter.next();
			isFocus = (0 == index) ? true:false;
			if(!addWebView(result,index,convertView,isFocus,rsp.tabDataArray)) {
				continue;
			}*/
			/*result = resultIter.next();
			isFocus = (0 == index) ? true:false;
			Log.d("DEBUG","getSoGouMapUrlResultView while----------");
			index++;*/
			
			
			result = resultIter.next();
//			isFocus = (0 == index) ? true:false;
			
			RspWebview rspWebview;
			String description = null;
		 
			rspWebview = result.getRspwebview();
			
			if(CFG.VIEW_TYPE_SOGOUMAP_URL != result.getType()) return null;
			
			description = rspWebview.getDescription();
			if(null == description) return null;
			
			url = rspWebview.getUrl();
			
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
            
            index++;
		}
		
		
		/*String url = null;
		RspWebview rspWebview;
		String description = null;
	 
		rspWebview = result.getRspwebview();
		
		if(CFG.VIEW_TYPE_WEBVIEW != result.getType()) return false;
		
		description = rspWebview.getDescription();
		if(null == description) return false;
		
		url = rspWebview.getUrl();*/
		
		final String urlString = url;
		
//		mTextView.setOnClickListener(new onClickListener() {
		mLayout.setOnClickListener(new onClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("DEBUG", "onclick=======================");
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
	            context.startActivity(intent);
			}
		});
		return convertView;
	}
	
	
	private void setListItemHiddenData(Response rsp,View convertView,int icon) {
		LinearLayout tabLayout = (LinearLayout) convertView.findViewById(R.id.tab_content);
		
		RelativeLayout hiddenLayout = (RelativeLayout) convertView.findViewById(R.id.layout_list_item_hidden);
		ImageView img = (ImageView) hiddenLayout.findViewById(R.id.vr_nearby_pickup_icon);
		TextView text = (TextView) hiddenLayout.findViewById(R.id.nearby_pickup_place);
		img.setBackgroundResource(icon);
		String searchContent = rsp.getSearchContent();
		if(null != searchContent && 0 != searchContent.length()) {
			text.setText(searchContent);
		}
		
		/*if(false == rsp.getVisible()) {
			RelativeLayout hiddenLayout = (RelativeLayout) convertView.findViewById(R.id.layout_list_item_hidden);
			hiddenLayout.setVisibility(View.VISIBLE);
			
//			LinearLayout tabLayout = (LinearLayout) convertView.findViewById(R.id.tab_content);
			tabLayout.setVisibility(View.GONE);
			Log.d("DEBUG", "-2-2-2-2-2-2-2-2-2-2-2-2-2-2-2");
			hiddenLayout.setOnClickListener(new onClickListener(tabLayout) {
				@Override
				public void onClick(View v) {
					Log.d("DEBUG", "-1-1-1-1-1-1-1-1-1-1-1-1-1-1-1");
					LinearLayout tabLayout = (LinearLayout)data;
					tabLayout.setVisibility(View.VISIBLE);
					v.setVisibility(View.GONE);
//					tabLayout.setVisibility(View.VISIBLE);
					tabLayout.requestLayout();
				}
			});
			
			ImageView img = (ImageView) hiddenLayout.findViewById(R.id.vr_nearby_pickup_icon);
			TextView text = (TextView) hiddenLayout.findViewById(R.id.nearby_pickup_place);
			img.setBackgroundResource(icon);
			String searchContent = rsp.getSearchContent();
			if(null != searchContent && 0 != searchContent.length()) {
				text.setText(searchContent);
			}
		}*/
		
		/*tabHiddenData data = new tabHiddenData();
		data.convertView = convertView;
		data.icon = icon;
		data.rsp = rsp;

		final tabHiddenData hiddenDatafinal = (tabHiddenData)data;
		final RelativeLayout hiddenLayoutfinal = (RelativeLayout) hiddenDatafinal.convertView.findViewById(R.id.layout_list_item_hidden);
		hiddenLayoutfinal.setOnClickListener(new onClickListener(tabLayout) {
			@Override
			public void onClick(View v) {
				Log.d("DEBUG", "0000000000000000000000000");
				LinearLayout tabLayout = (LinearLayout)data;
				tabLayout.setVisibility(View.VISIBLE);
				v.setVisibility(View.GONE);
				tabLayout.requestLayout();
			}
		});
		LinearLayout radioGroupLayout = (LinearLayout) convertView.findViewById(R.id.radioGroup);
		LinearLayout tabhostLayout = (LinearLayout) convertView.findViewById(R.id.tabhost_layout);*/
		
		/*radioGroupLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "11111111111111111111111111");
				if (hiddenLayoutfinal.getVisibility() == View.GONE) {
					hiddenLayoutfinal.setVisibility(View.VISIBLE);
					
					LinearLayout tabLayout = (LinearLayout) hiddenDatafinal.convertView.findViewById(R.id.tab_content);
					tabLayout.setVisibility(View.GONE);
					
					ImageView img = (ImageView) hiddenLayoutfinal.findViewById(R.id.vr_nearby_pickup_icon);
					TextView text = (TextView) hiddenLayoutfinal.findViewById(R.id.nearby_pickup_place);
					img.setBackgroundResource(hiddenDatafinal.icon);
					String searchContent = hiddenDatafinal.rsp.getSearchContent();
					if(null != searchContent && 0 != searchContent.length()) {
						text.setText(searchContent);
					}
					hiddenDatafinal.rsp.setVisible(false);
				}
				Message mMessage = Message.obtain();
				mMessage.what = 2;
				mMessage.obj = new TtsRes();
				
				
				Log.d("DEBUG", "the handle = "+handler+" and the mMessage = "+mMessage);
				handler.sendMessage(mMessage);
			}
		});*/
				
		//shigq
		/*tabhostLayout.setOnClickListener(new onClickListener(data) 
		{
			@Override
			public void onClick(View v) 
			{
				tabHiddenData hiddenData = (tabHiddenData)data;
				RelativeLayout hiddenLayout = (RelativeLayout) hiddenData.convertView.findViewById(R.id.layout_list_item_hidden);
				if(View.GONE == hiddenLayout.getVisibility())
				{
					//do nothing
					//change the click event to radioLayout to hide the tabhostLayout
				} else {
					hiddenLayout.setVisibility(View.GONE);
					LinearLayout tabLayout = (LinearLayout) hiddenData.convertView.findViewById(R.id.tab_content);
					tabLayout.setVisibility(View.VISIBLE);
					hiddenData.rsp.setVisible(true);
				}
			}
		});*/
		
	}
	
	private boolean addWebView(Response.Item result, int index, View convertView, boolean focus, 
								ArrayList<TabData> tabData, HashMap<Integer,String> webviewHashMap) {
		Log.e("DEBUG", "----------------------------addWebView");
		String url = null;
		RspWebview rspWebview;
		String description = null;
	 
		rspWebview = result.getRspwebview();
		
		if(CFG.VIEW_TYPE_WEBVIEW != result.getType()) return false;
		
		description = rspWebview.getDescription();
		if(null == description) return false;
		
		url = rspWebview.getUrl();
		Log.e("DEBUG", "----------------------------addWebView---------description = "+description);
		
		//设置tab的各项数据
	   	if(description.contains(convertView.getResources().getString(R.string.vs_wendai))) {
	   		//问答
	   		//在layout中动态插入webview控件//不能放在判断以外	   		
	   		Log.e("DEBUG", "----------------------------addWebView----------wenda");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_wenda, 
	   				R.drawable.vs_icon_wenda_sl, R.string.vs_wendai, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_sousuo))) {
	   		//搜索-保底
	   		Log.e("DEBUG", "----------------------------addWebView----------search");
		    LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_search, 
		    		R.drawable.vs_icon_search_sl, R.string.vs_sousuo, focus, tabData, url, webviewHashMap);
		    
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_tieba))) {
	   		//贴吧
	   		Log.e("DEBUG", "----------------------------addWebView----------tieba");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_tieba, 
	   				R.drawable.vs_icon_tieba_sl, R.string.vs_tieba, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_shipin))) {
	   		//视频
	   		Log.e("DEBUG", "----------------------------addWebView----------vedio");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_video, 
	   				R.drawable.vs_icon_video_sl, R.string.vs_shipin, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_wenku))) {
	   		//文库
	   		Log.e("DEBUG", "----------------------------addWebView----------wenku");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_wenku, 
	   				R.drawable.vs_icon_wenku_sl, R.string.vs_wenku, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_ditu))) {
			//地图
	   		Log.e("DEBUG", "----------------------------addWebView----------map");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_map, 
	   				R.drawable.vs_icon_map_sl, R.string.vs_ditu, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_travel))) {
			//旅游
	   		Log.e("DEBUG", "----------------------------addWebView----------travel");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_travel, 
	   				R.drawable.vs_icon_travel_sl, R.string.vs_travel, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_tianqi))) {
			//天气
	   		Log.e("DEBUG", "----------------------------addWebView----------weather");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_weather, 
	   				R.drawable.vs_icon_weather_sl, R.string.vs_tianqi, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_lc))) {
			//列车
	   		Log.e("DEBUG", "----------------------------addWebView----------train");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_train, 
	   				R.drawable.vs_icon_train_sl, R.string.vs_lc, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_news))) {
			//新闻
	   		Log.e("DEBUG", "----------------------------addWebView----------news");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_news, 
	   				R.drawable.vs_icon_news_sl, R.string.vs_news, focus, tabData, url, webviewHashMap);
	   		
	   	} else if(description.contains(convertView.getResources().getString(R.string.vs_tupian))) {
			//图片
	   		Log.e("DEBUG", "----------------------------addWebView----------picture");
	   		LinearLayout contentTabLayout = setTabData(convertView, index, R.drawable.vs_icon_image, 
	   				R.drawable.vs_icon_image_sl, R.string.vs_tupian, focus, tabData, url, webviewHashMap);
	   	} else {
	   		//if the tab has not been set, make the next tab visible
	   		if (index == 0) {
	   			return false;
	   		}
	   	}
	   	
		return true;
	}
	
	private LinearLayout setTabData(View convertView, int index, int nImgId, int fImgId, int strId, boolean focus, 
										ArrayList<TabData> tabData, final String url, final HashMap<Integer,String> webviewHashMap) {
		Log.e("DEBUG", "----------------setTabData");
    	int contentId = 0;
		int tabLayoutId = 0;
		int tabImgLayoutId = 0;
		int radioBtnId = 0;
		int textId = 0;
		
		class Parmas {
			public View view;
			public ArrayList<TabData> tabData;
			public LinearLayout mContentTabLayout;
		}
		
		contentId = tabLayoutIdMap.get(index*5);
		tabLayoutId = tabLayoutIdMap.get(index*5 + 1);
		tabImgLayoutId = tabLayoutIdMap.get(index*5 + 2);
		radioBtnId = tabLayoutIdMap.get(index*5 + 3);
		textId = tabLayoutIdMap.get(index*5 + 4);
		
		LinearLayout contentTabLayout = (LinearLayout)convertView.findViewById(contentId);
		LinearLayout tabLayout = (LinearLayout)convertView.findViewById(tabLayoutId);
		LinearLayout tabImgLayout = (LinearLayout)convertView.findViewById(tabImgLayoutId);
		tabLayout.setVisibility(View.VISIBLE);
		if(true == focus) {
			contentTabLayout.setVisibility(View.VISIBLE);
			tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball_b);
		} else {
			tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball);
		}
		
		if (url != null) {
			webviewHashMap.put(tabLayoutId, url);
		}
		
		//if the reulst is web, it should load this web and add to the first tab
		if (contentTabLayout.getVisibility() == View.VISIBLE && url != null) {
			Log.e("DEBUG", "==========web type and will start to load page");
			addWebViewForWebResult(contentTabLayout, url, tabLayoutId, webviewHashMap);
		}
		
		ImageView img = (ImageView)convertView.findViewById(radioBtnId);
		TextView text = (TextView)convertView.findViewById(textId);
		
		TabData data = new TabData();

		data.content_id = contentId;
		data.tab_id = tabLayoutId;
		data.btn_id = radioBtnId;
		data.tabimg_id = tabImgLayoutId;
		
		data.f_img = fImgId;
		data.n_img = nImgId;
		
		//tabData.add(data);
		if(tabData.size()-1 <= index) {
			tabData.add(data);
		} else {
			tabData.set(index, data);
		}
		
		text.setText(strId);
		img.setBackgroundResource((!focus)?nImgId:fImgId);
		
		Parmas parmas = new  Parmas();
		parmas.view = convertView;
		parmas.tabData = tabData;
        parmas.mContentTabLayout = contentTabLayout;
		tabLayout.setOnClickListener(new onClickListener(parmas) {
			@Override
			public void onClick(View v) {
				Parmas parmas  = (Parmas)data;
				LinearLayout mContentTabLayout = parmas.mContentTabLayout;
				Iterator<TabData> iter = parmas.tabData.iterator();
				while(iter.hasNext()) {
					TabData data = iter.next();
					
					LinearLayout layout = (LinearLayout)parmas.view.findViewById(data.tab_id);
					if(v == layout) {
						LinearLayout tabImgLayout = (LinearLayout)parmas.view.findViewById(data.tabimg_id);
						ImageView img = (ImageView)parmas.view.findViewById(data.btn_id);
						tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball_b);
						img.setBackgroundResource(data.f_img);  
						//LinearLayout contentTabLayout = (LinearLayout)parmas.view.findViewById(data.content_id);
						mContentTabLayout = (LinearLayout)parmas.view.findViewById(data.content_id);
						mContentTabLayout.setVisibility(View.VISIBLE);
						
						addWebViewForWebResult(mContentTabLayout, url, data.tab_id, webviewHashMap);
						
					} else {
						
						LinearLayout tabImgLayout = (LinearLayout)parmas.view.findViewById(data.tabimg_id);
						ImageView img = (ImageView)parmas.view.findViewById(data.btn_id);
						tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball);
						img.setBackgroundResource(data.n_img);
						
						//LinearLayout contentTabLayout = (LinearLayout)parmas.view.findViewById(data.content_id);
						
						mContentTabLayout = (LinearLayout)parmas.view.findViewById(data.content_id);
						if (data.tab_id != R.id.l_tab1 && !loadPageFinished) {
							/*if () {
								WebView tempView = (WebView)mContentTabLayout.getChildAt(0);
								if (tempView != null) {
									Log.d("linp", "will stop loading web page on tab changed!!!!!!!!!!!!!!!");
									tempView.stopLoading();
								}
								mContentTabLayout.removeAllViews();
							}*/
							mContentTabLayout.removeAllViews();
						}
						mContentTabLayout.setVisibility(View.GONE);
						
					}
				}
			}
		});
//		Log.d("linp", "##########finish contentTabLayout "+contentTabLayout.getId());
        return contentTabLayout;
    }
	
	public void addWebViewForWebResult(LinearLayout mContentTabLayout, String url, int tabId, HashMap<Integer,String> webviewHashMap) {
		if (url != null && mContentTabLayout.getChildCount() == 0) {
			//to add webview when switch to next tab if needed
			ImageView imageView = new ImageView(context);
			imageView.setBackgroundResource(R.drawable.vs_webview_loading);
			
			mContentTabLayout.removeAllViews();
	   		mContentTabLayout.addView(imageView);
	   		
			Log.e("DEBUG", "==========to start loading new page!");
			if(mWaitForPageLoadFinished != null) {
				mHandler.removeCallbacks(mWaitForPageLoadFinished);
//				Log.e("linp", "#########will remove mWaitForPageLoadFinished call back!");
			}
			if(mSendMessageRunnable != null) {
				mHandler.removeCallbacks(mSendMessageRunnable);
//				Log.e("linp", "==========will remove mSendMessageRunnable call back!");
				if (mHandler.getMessageName(Message.obtain(mHandler, 2)) != null) {
					mHandler.removeMessages(2);
				}
			}
			
			mainActivity.resetFillViewHeight();
			Message msg = Message.obtain();
			msg.what = 2;							// MSG_TYPE_UPDATE_FILLVIEW = 2;
			mHandler.sendMessage(msg);
			
			
			WebView webview = new WebView(context);
			webview.getSettings().setJavaScriptEnabled(true);
//	   		webview.setId(index);
			webview.requestFocus();
	   		webview.loadUrl(webviewHashMap.get(tabId));
	   		webview.setWebChromeClient(new mWebChromeClient());
	   		webview.setWebViewClient(new webViewClient(mContentTabLayout));
	   		/*mContentTabLayout.removeAllViews();
	   		mContentTabLayout.addView(imageView);*/
		} else {
			Log.e("DEBUG", "has child and need to refresh FillView");
			
			//if tab contained a child view, it just need to send message to refresh FillView in MainActivity
			mainActivity.resetFillViewHeight();
			Message msg = Message.obtain();
			msg.what = 2;						// MSG_TYPE_UPDATE_FILLVIEW = 2;
			mHandler.sendMessage(msg);
		}
	}
	
	private class mWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (loadpageProgress == 100) {
				mHandler.postDelayed(mSendMessageRunnable, 200);
				view.setWebChromeClient(null);
			}
		}
		
	}
	
	private class webViewClient extends WebViewClient {
		private LinearLayout layout;
		
		webViewClient(LinearLayout parentLayout) {
			layout = parentLayout;
			mWaitForPageLoadFinished = new waitForPageLoadFinished(layout);
			mSendMessageRunnable = new sendMessageRunnable(layout); 
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//To open new page by browser but not current webview
//			view.loadUrl(url) ;
			Log.e("DEBUG", "---------------------------url=="+url);
			if(url != null && url.startsWith("tel:")){
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	            context.startActivity(intent);
			}else{
				view.loadUrl(url) ;
			}
			return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.e("DEBUG", "---------------------------onPageStarted");
//			layout.setBackgroundResource(R.drawable.vs_webview_loading);
			loadPageFinished = false;
			loadPageError = false;
			loadpageProgress = 0;
//			mHandler.postDelayed(mWaitForPageLoadFinished, 6000);
			mHandler.postDelayed(mWaitForPageLoadFinished, 10000);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			Log.e("DEBUG", "----------------------------------onReceivedError");
			Log.i("DEBUG","errorCode=="+errorCode+"     description=="+description+"     failingUrl=="+failingUrl);
			loadPageError = true;
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			// TODO Auto-generated method stub
			Log.e("DEBUG", "----------------------------------onReceivedSslError");
			super.onReceivedSslError(view, handler, error);
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {
			// TODO Auto-generated method stub\
			Log.e("DEBUG", "----------------------------------onReceivedHttpAuthRequest");
			super.onReceivedHttpAuthRequest(view, handler, host, realm);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.e("DEBUG", "---------------------------onPageFinished");
			
			if (loadPageError) {
				Log.e("DEBUG", "---------------------------onPageFinished------with loading error");
				updateWebPageForReceiveError(layout);
			} else {
				if(mWaitForPageLoadFinished != null) {
					mHandler.removeCallbacks(mWaitForPageLoadFinished);
					Log.e("DEBUG", "---------------------------onPageFinished------OK");
				}
				updateWebAdding(layout, view);
			}
			
			if (mSendMessageRunnable != null) {
				mHandler.removeCallbacks(mSendMessageRunnable);
			}
			mHandler.postDelayed(mSendMessageRunnable, 200);

			loadPageFinished = true;
			
			view.setWebViewClient(null);
			view.setWebChromeClient(null);
		}
	}
	
	class waitForPageLoadFinished implements Runnable{
        LinearLayout mLayout;
		waitForPageLoadFinished(LinearLayout layout){
			mLayout = layout;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!loadPageFinished) {
				//To be defined
				if (loadPageError) {
					Log.e("DEBUG", "---------------------------loadPageError");
					updateWebPageForReceiveError(mLayout);
				} else {
					Log.e("DEBUG", "---------------------------time out and is not finished");
					updateWebPageForReceiveTimeout(mLayout);
				}
				/*Button freshButton = new Button(context);
				freshButton.setWidth(1014);
				freshButton.setHeight(286);
				freshButton.setText("Fresh Page");
				freshButton.setTextSize(30);
				freshButton.setId(2);
				mLayout.removeAllViews();
				mLayout.addView(freshButton);
				freshButton.setOnClickListener(new onClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Log.e("DEBUG", "---------------------------onClick");
					}
				});*/
			} else {
				Log.e("DEBUG", "---------------------------time out and is finished");
				/*mLayout.requestFocus();
				mLayout.requestLayout();*/
			}
		}
		
	}
	
	class sendMessageRunnable implements Runnable{
		LinearLayout mLayout;
		sendMessageRunnable(LinearLayout layout){
			mLayout = layout;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub

			mLayout.requestLayout();
			
			Message msg = Message.obtain();
			msg.what = 2;						//MSG_TYPE_UPDATE_FILLVIEW = 2
			mHandler.sendMessage(msg);
			
			if (mWaitForPageLoadFinished != null) {
				mHandler.removeCallbacks(mWaitForPageLoadFinished);
			}
		}
		
	}
	
	Runnable toUpdateFillView = new Runnable() {
		@Override
		public void run() {
			Message msg = Message.obtain();
			msg.what = 2;						//MSG_TYPE_UPDATE_FILLVIEW = 2
			mHandler.sendMessage(msg);
		}
	};
	
	class GridItemClickListener implements OnItemClickListener {
		private RspXmlPic rspxmlpic = null;
		
		GridItemClickListener(RspXmlPic rspxmlpic) {
			this.rspxmlpic = rspxmlpic;
		}
		
		public void onItemClick(AdapterView<?> parent, View view, int position, long rowid) {
	       	Intent intent = new Intent(context,ViewimageActivity.class);
	       	
	       	String tempPathString = "/temp"+rspXmlPicturePathMap.get(rspxmlpic)+"/";
	       	intent.putParcelableArrayListExtra("list",rspXmlPictureItemMap.get(rspxmlpic));
	       	intent.putExtra("index", position);
	       	intent.putExtra("path", PIC_TEMP_PATH + tempPathString);
	       	context.startActivity(intent);
		}
	 }
	 
	 abstract class onClickListener implements OnClickListener {
		 public Object data;
		 onClickListener(Object data) {
			 this.data = data;
		 }
		public onClickListener() {
			// TODO Auto-generated constructor stub
		}
	 }
	
	private class tabHiddenData {
		public Response rsp;
		public View convertView;
		public int icon;
	}
	
	public void getMainActivity(MainActivity m) {
		mainActivity = m;
	}
	
	public boolean getLoadPageState() {
		return loadPageFinished;
	}
	
	public void updateWebAdding(LinearLayout layout, WebView view) {
		mainActivity.resetFillViewHeight();
		layout.removeAllViews();
		layout.addView(view);
		
		layout.requestLayout();
		layout.invalidate();
		
		layout.setBackgroundResource(R.drawable.vs_question_structure_item_bg);
	}
	
	public void updateWebPageForReceiveError(LinearLayout layout) {
		mainActivity.resetFillViewHeight();
		
		ImageView imageView = new ImageView(context);
		imageView.setBackgroundResource(R.drawable.vs_web_receive_error_big);
		
		layout.removeAllViews();
		layout.addView(imageView);

		layout.requestLayout();
		layout.invalidate();
	}
	
	public void updateWebPageForReceiveTimeout(LinearLayout layout) {
		mainActivity.resetFillViewHeight();
		
		ImageView imageView = new ImageView(context);
		imageView.setBackgroundResource(R.drawable.vs_web_receive_timeout_big);
		
		layout.removeAllViews();
		layout.addView(imageView);

		layout.requestLayout();
		layout.invalidate();
	}

	private class ImageDownloadTask extends AsyncTask<Object, Object, Bitmap> {
     	private ImageView imgView = null;
     	private String url = null;
     	private int index;
     	private ArrayList<RspXmlPicItem> list;
     	private PicGridAdapter picAdapter;
     	private String pathString;

     	@Override
     	protected Bitmap doInBackground(Object... params) {
     		Bitmap bmp = null;
     		url = (String)params[0];
     		imgView = (ImageView) params[1];
     		index = (Integer) params[2];
     		list = (ArrayList<RspXmlPicItem>)params[3];
     		picAdapter = (PicGridAdapter)params[4];
     		pathString = (String)params[5];
     		Log.e("DEBUG", "PicGridAdapter---------------ImageDownloadTask----------doInBackground --------the pathString = "+pathString);
     		
         	try {
     			bmp = BitmapFactory.decodeStream(new URL(url).openStream());
     		} catch (Exception e) {
     			e.printStackTrace();
     		}
     		return bmp;
     	}

     	protected void onPostExecute(Bitmap result) {
//     		Log.e("DEBUG", "PicGridAdapter---------------ImageDownloadTask----------onPostExecute");
     		imgView.setImageBitmap(result);
            list.get(index).setBitmap(result);
//            saveImage(result);
            picAdapter.notifyDataSetChanged();
     	}
     	
     	private void saveImage(Bitmap result) {
	     	if(null == result) return ;
	     	
	 		String   name   =   "vs"+String.valueOf(index)+".png";
	 		
	 		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
	 		if(!sdCardExist) {
//	 			Log.e("DEBUG", "PicGridAdapter---------------ImageDownloadTask----------onPostExecute-----saveImage:sdcard exist!!!!");
	 		}
	 		
			File sdcardDir=Environment.getExternalStorageDirectory();
			String path = sdcardDir.toString()+"/"+PIC_TEMP_PATH+"/" + pathString + "/";
			
			Tools tools = new Tools();
			if(!tools.saveImage(result, path, name)) {
//				Log.e("DEBUG", "PicGridAdapter---------------ImageDownloadTask----------onPostExecute-----saveImage fail!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
     	}
     }
	
	public HashMap<Integer, Integer> getTabLayoutIdMap() {
		return tabLayoutIdMap;
	}
	
	//Offline start
	public View getCallingOrMessageOrContactsLayout(final OFFLINE_TYPE type) {
		LinearLayout offlineResultLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_calling_message_contacts_layout, null);
		
		final LinearLayout callingLayout = (LinearLayout) offlineResultLayout.findViewById(R.id.vs_offline_calling_contact_layout);
		final LinearLayout messageLayout = (LinearLayout) offlineResultLayout.findViewById(R.id.vs_offline_sending_message_layout);
		final LinearLayout contactsLayout = (LinearLayout) offlineResultLayout.findViewById(R.id.vs_offline_contacts_result_layout);
		LinearLayout airplaneSwitchLayout = (LinearLayout) offlineResultLayout.findViewById(R.id.vs_offline_air_plane_switch_layout);
		AuroraSwitch airplaneSwitch = (AuroraSwitch) offlineResultLayout.findViewById(R.id.vs_offline_airplane_mode_switch);
		
		final LinearLayout contactsMoreLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_search_contacts_more, null);
		final ImageView moreNormal = (ImageView) contactsMoreLayout.findViewById(R.id.vs_offline_contacts_more_normal);
		final ImageView morePressed = (ImageView) contactsMoreLayout.findViewById(R.id.vs_offline_contacts_more_pressed);
		final TextView moreTextNormal = (TextView) contactsMoreLayout.findViewById(R.id.vs_offline_contacts_more_text_normal);
		final TextView moreTextPressed = (TextView) contactsMoreLayout.findViewById(R.id.vs_offline_contacts_more_text_pressed);
		
		if (mainActivity.isAirPlaneModeOn(context)) {
			airplaneSwitchLayout.setVisibility(View.VISIBLE);
			airplaneSwitch.setChecked(true);
		}
		airplaneSwitch.setOnCheckedChangeListener(mCheckedChangeListener);
		
		View convertView = null;
		
		mContactsItemList = mainActivity.getContactsItemList();
		if (mContactsItemList == null) return null;
		
		final int size = mContactsItemList.size();
		
		callingLayout.removeAllViews();
		messageLayout.removeAllViews();
		contactsLayout.removeAllViews();
		
		//the length of contacts list to show is 15
		for (int i = 0; i < (size > 15? 15 : size); i++) {
			ContactsItem mContactsItem = mContactsItemList.get(i);
			Log.d("DEBUG", "mContactsItem.getContactName = "+mContactsItem.getContactName());
			setResultLayoutFromContactsInfo(mContactsItem, callingLayout, messageLayout, contactsLayout, size, type);
		}
		
		if (size > 15) {
			contactsLayout.addView(contactsMoreLayout);
			contactsMoreLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (moreNormal.getVisibility() == View.VISIBLE) {
						contactsLayout.removeView(contactsMoreLayout);
						for (int i = 15; i < size; i++) {
							ContactsItem mContactsItem = mContactsItemList.get(i);
							setResultLayoutFromContactsInfo(mContactsItem, callingLayout, messageLayout, contactsLayout, size, type);
						}
						
						moreNormal.setVisibility(View.GONE);
						moreTextNormal.setVisibility(View.GONE);
						
						morePressed.setVisibility(View.VISIBLE);
						moreTextPressed.setVisibility(View.VISIBLE);
						
						contactsLayout.addView(contactsMoreLayout);
					} else {
						contactsLayout.removeView(contactsMoreLayout);
						for (int i = size - 1; i >= 15; i--) {
							contactsLayout.removeViewAt(i);
						}
						
						moreNormal.setVisibility(View.VISIBLE);
						moreTextNormal.setVisibility(View.VISIBLE);
						
						morePressed.setVisibility(View.GONE);
						moreTextPressed.setVisibility(View.GONE);
						
						contactsLayout.addView(contactsMoreLayout);
					}
					
				}
			});
		}

		return offlineResultLayout;
	}
	
	OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			// TODO Auto-generated method stub
			mainActivity.setAirplaneMode(context, isChecked);
		}
	};
	
	public View setContactItemView(final String name, final String number, final LinearLayout numberLayout, Drawable photo, final OFFLINE_TYPE type) {
		final LinearLayout contactItemLayout =  (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_contacts_item, null);
		TextView nameTextView = (TextView) contactItemLayout.findViewById(R.id.vs_offline_contact_name);
		final TextView numberTextView = (TextView) contactItemLayout.findViewById(R.id.vs_offline_contact_number);
		final ImageView arrow = (ImageView) contactItemLayout.findViewById(R.id.arrow_offline);
//		ImageView photoImageView = (ImageView) contactItemLayout.findViewById(R.id.vs_offline_contact_image);
		
		contactItemLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "contactItemLayout-------getchildcount = "+contactItemLayout.getChildCount());
				if (numberLayout != null) {
					if (numberLayout.getVisibility() == View.VISIBLE) {
						numberLayout.setVisibility(View.GONE);
						numberTextView.setVisibility(View.VISIBLE);
						
						if (arrow.getVisibility() == View.VISIBLE) {
							arrow.setBackgroundResource(R.drawable.vs_help_arrows_normal);
						}
						
					} else {
						numberLayout.setVisibility(View.VISIBLE);
						numberTextView.setVisibility(View.GONE);
						
						if (arrow.getVisibility() == View.VISIBLE) {
							arrow.setBackgroundResource(R.drawable.vs_help_arrows_pressed);
						}
						
					}
					
				} else {
					
					if (mainActivity.isAirPlaneModeOn(context)) {
						return;
						
					} else if (mainActivity.getInsertSimCount() <= 0 && mainActivity.getSIMCardState() != 5) {//== 1) {
						mainActivity.setOffLineType(OFFLINE_TYPE.TIPS);
						
						TtsRes  ttsRes = new TtsRes();
		     			ttsRes.flag = true;
		     			ttsRes.text = mainActivity.getResources().getString(R.string.vs_offline_answer_message_no_sim);
		     			
						Message msg = Message.obtain();
						msg.what = MainActivity.MSG_TYPE_TTS;
						msg.obj = ttsRes;
						mHandler.sendMessage(msg);
						
						return;
					}
					
					LinearLayout contactLayout = (LinearLayout) contactItemLayout.getParent();
					if (contactLayout == null) return;
					
					showCallingOrMessageView(name, number, contactLayout, type);
					updateContactListView(contactLayout, contactItemLayout);
					
				}
				
			}
		});
		
		if (name != null) {
			nameTextView.setText(name);
		}
		/*if (number != null) {
			numberTextView.setText(number);
		}*/
		if (numberLayout != null) {
			numberTextView.setText(R.string.vs_offline_contact_item_manynumbers);
			numberTextView.setTextColor(mainActivity.getResources().getColor(R.color.vs_half_white));
			
			arrow.setVisibility(View.VISIBLE);
			
			ImageView devider = new ImageView(context);
			devider.setBackgroundResource(R.drawable.vs_help_devider);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			if (contactItemLayout.getChildCount() >= 1) {
				contactItemLayout.addView(devider);
			}
			contactItemLayout.addView(numberLayout);
			
		} else {
			String tempNumber = number;
			tempNumber = mainActivity.getFormattedNumber(tempNumber);
			numberTextView.setText(tempNumber);
		}
		/*if (photo != null) {
			photoImageView.setBackground(photo);
		}*/
		
		return contactItemLayout;
		
	}
	
	public View setNumberItemView(final String name, String numbertype, final String number, final OFFLINE_TYPE type) {
		final LinearLayout phoneNumberLayout =  (LinearLayout)mLayoutInflater.inflate(R.layout.vs_offline_contacts_number_layout, null);
		
		TextView numberType = (TextView) phoneNumberLayout.findViewById(R.id.vs_offline_contact_number_type);
		TextView numberString = (TextView) phoneNumberLayout.findViewById(R.id.vs_offline_contact_number_number);
		LinearLayout numberContentLayout = (LinearLayout) phoneNumberLayout.findViewById(R.id.vs_offline_contact_number_content_layout);
		
		if (numbertype != null) {
			numberType.setText(numbertype);
		}
		
		if (number != null) {
			String tempNumber = number;
			tempNumber = mainActivity.getFormattedNumber(tempNumber);
			numberString.setText(tempNumber);
		}
				
		numberContentLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "phoneNumberItemView--------to call");
				
				/*if (mainActivity.isAirPlaneModeOn(context) || mainActivity.getInsertSimCount() <= 0 && mainActivity.getSIMCardState() == 1) {
					return;
				}*/
				
				if (mainActivity.isAirPlaneModeOn(context)) {
					return;
					
				} else if (mainActivity.getInsertSimCount() <= 0 && mainActivity.getSIMCardState() == 1) {
					mainActivity.setOffLineType(OFFLINE_TYPE.TIPS);
					
					TtsRes  ttsRes = new TtsRes();
	     			ttsRes.flag = true;
	     			ttsRes.text = mainActivity.getResources().getString(R.string.vs_offline_answer_message_no_sim);
	     			
					Message msg = Message.obtain();
					msg.what = MainActivity.MSG_TYPE_TTS;
					msg.obj = ttsRes;
					mHandler.sendMessage(msg);
					
					return;
				}
				
				LinearLayout numberLayout = (LinearLayout) phoneNumberLayout.getParent();
				if (numberLayout == null) return;
				LinearLayout contactItemLayout = (LinearLayout) numberLayout.getParent();
				if (contactItemLayout == null) return;
				LinearLayout contactLayout = (LinearLayout) contactItemLayout.getParent();
				if (contactLayout == null) return;
				
				showCallingOrMessageView(name, number, contactLayout, type);
				updateContactListView(contactLayout, contactItemLayout);
				
				/*if (contactLayout.getChildCount() > 1) {
					contactLayout.removeAllViews();
					contactLayout.addView(contactItemLayout);
				}*/
								
			}
		});
		
		return phoneNumberLayout;
	}
	
	public void showCallingOrMessageView(String name, String number, LinearLayout contactLayout, OFFLINE_TYPE type) {
		LinearLayout callingMessageContactsLayout = (LinearLayout) contactLayout.getParent();
		if (callingMessageContactsLayout == null) return;
		
		if (type == OFFLINE_TYPE.CALLING) {
			if (mainActivity.getInsertSimCount() == 1) {
				mainActivity.callContactBySimCard(number, mainActivity.getLastCallSlotId(number));
				return;
			}
			
			LinearLayout callingLayout = (LinearLayout) callingMessageContactsLayout.findViewById(R.id.vs_offline_calling_contact_layout);
			if (callingLayout != null) {
				contactLayout.setVisibility(View.GONE);
				
				callingLayout.removeAllViews();
				callingLayout.setVisibility(View.VISIBLE);
				View callingView = setCallingView(name, number, null);
				callingLayout.addView(callingView);
			}
			
		} else if (type == OFFLINE_TYPE.MESSAGE) {
			LinearLayout messageLayout = (LinearLayout) callingMessageContactsLayout.findViewById(R.id.vs_offline_sending_message_layout);
			if (messageLayout != null) {
				contactLayout.setVisibility(View.GONE);
				
				LinearLayout sendingView = (LinearLayout)messageLayout.getChildAt(0);
				int oldIndex = 0;
				boolean flag = true;
				if (sendingView == null) {
					oldIndex = messageViewIndex;
					flag = false;
				} else {
					oldIndex = sendingView.getId();
				}
				Log.d("DEBUG", "showCallingOrMessageView---------oldIndex = "+oldIndex);

				messageLayout.removeAllViews();
				messageLayout.setVisibility(View.VISIBLE);
				View sendingMessageView = setSendingMessageView(name, number, null, flag, oldIndex);
				messageLayout.addView(sendingMessageView);

			}
		}
	}
	
	public void updateContactListView(LinearLayout contactLayout, LinearLayout contactItemLayout) {
		if (contactLayout.getChildCount() > 1) {
			contactLayout.removeAllViews();
			contactLayout.addView(contactItemLayout);
		}
	}
    
	public View setCallingView(final String name, final String number, Drawable photo) {
		
		Log.d("pgm","ContantViewConstructor.java              setCallingView()                   name== "+name+"        number=="+number);
		
		final LinearLayout callingView = (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_contacts_calling, null);
		TextView callContact = (TextView) callingView.findViewById(R.id.vs_offline_contact_calling_name);
		TextView callNumber = (TextView) callingView.findViewById(R.id.vs_offline_contact_calling_number);
		TextView callTipsBy = (TextView) callingView.findViewById(R.id.vs_offline_contact_calling_tips_by);
		TextView callTipsSim = (TextView) callingView.findViewById(R.id.vs_offline_contact_calling_tips_sim);
		
//		ImageView callContactPhoto = (ImageView) callingView.findViewById(R.id.vs_offline_contact_calling_image);
		/*ImageView callSim1 = (ImageView) callingView.findViewById(R.id.vs_offline_contact_calling_sim1);
		ImageView callCancel = (ImageView) callingView.findViewById(R.id.vs_offline_contact_calling_cancel);
		ImageView callSim2 = (ImageView) callingView.findViewById(R.id.vs_offline_contact_calling_sim2);*/
		LinearLayout callSim1 = (LinearLayout) callingView.findViewById(R.id.vs_offline_contact_calling_sim1);
		LinearLayout callCancel = (LinearLayout) callingView.findViewById(R.id.vs_offline_contact_calling_cancel);
		LinearLayout callSim2 = (LinearLayout) callingView.findViewById(R.id.vs_offline_contact_calling_sim2);
		
		int length = (int) mainActivity.getResources().getInteger(R.integer.vs_offline_calling_progressbar_length);
		
		if (!mainActivity.isAirPlaneModeOn(context) && mainActivity.getInsertSimCount() > 0 && mainActivity.getSIMCardState() != 1) {
			mainActivity.setTimerForCalling(callingView, length, number);
			Log.d("DEBUG", "start calling timer ------------------------------= ");
		}
        
		if (mainActivity.getInsertSimCount() == 2) {
			highLightLastCallSimIcon(callSim1, callSim2, number, callTipsSim);
			
			callSim2.setVisibility(View.VISIBLE);
			
			callTipsBy.setVisibility(View.VISIBLE);
			callTipsSim.setVisibility(View.VISIBLE);
		} else {
			ImageView callSim1ImageView = (ImageView) callSim1.getChildAt(0);
			callSim1ImageView.setImageResource(R.drawable.vs_offline_call_single_sim);
			
			callSim2.setVisibility(View.GONE);
			
			callTipsBy.setVisibility(View.GONE);
			callTipsSim.setVisibility(View.GONE);
		}
		Log.d("pgm","mainActivity.getInsertSimCount() == "+mainActivity.getInsertSimCount());
		
		if (name != null) {
			callContact.setText(name);
		}
		if (number != null) {
			String tempNumber = number;
			tempNumber = mainActivity.getFormattedNumber(tempNumber);
			callNumber.setText(tempNumber);
		}
		/*if (photo != null) {
			callContactPhoto.setBackground(photo);
		}
		*/
		
		callSim1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mainActivity.callContactBySimCard(number, 0);
			}
		});
		callCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub			
				mainActivity.cancelCallingTimer();
				mainActivity.resetFillViewHeight();
				hideCallingView(callingView);
				
				mainActivity.setOffLineType(OFFLINE_TYPE.TIPS);
				
				String callCancelHead = mainActivity.getResources().getString(R.string.vs_offline_answer_call_cancel);
				String callCancelTail = mainActivity.getResources().getString(R.string.vs_offline_answer_call_tail);
				StringBuilder answer = new StringBuilder();
				answer.append(callCancelHead).append(name).append(callCancelTail);
				
				TtsRes  ttsRes = new TtsRes();
     			ttsRes.flag = true;
     			ttsRes.text = (answer.toString());
     			
				Message msg = Message.obtain();
				msg.what = MainActivity.MSG_TYPE_TTS;
				msg.obj = ttsRes;
				mHandler.sendMessage(msg);
				
			}
		});
		callSim2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mainActivity.callContactBySimCard(number, 1);
			}
		});
		
		return callingView;
	}
	
	public void highLightLastCallSimIcon(LinearLayout simLayout1, LinearLayout simLayout2, String number, TextView text) {
		int lastSimId = mainActivity.getLastCallSlotId(number);
		/*if (lastSimId == -1) {
			lastSimId = mainActivity.getDefaultSim();
		}*/
		
		switch (lastSimId) {
		case 0:
			ImageView sim1Icon = (ImageView) simLayout1.getChildAt(0);
			sim1Icon.setImageResource(R.drawable.vs_offline_call_sim1_highlight);
			text.setText(mainActivity.getResources().getString(R.string.vs_offline_calling_tips_sim1));
			break;
		case 1:
			ImageView sim2Icon = (ImageView) simLayout2.getChildAt(0);
			sim2Icon.setImageResource(R.drawable.vs_offline_call_sim2_highlight);
			text.setText(mainActivity.getResources().getString(R.string.vs_offline_calling_tips_sim2));
			break;
		}
	}
	
	public void hideCallingView(LinearLayout callingView) {
		LinearLayout callingLayout = (LinearLayout) callingView.getParent();
		if (callingLayout == null) return;
		LinearLayout callingMessageContactsLayout = (LinearLayout) callingLayout.getParent();
		if (callingMessageContactsLayout != null) {
			LinearLayout contactLayout = (LinearLayout) callingMessageContactsLayout.findViewById(R.id.vs_offline_contacts_result_layout);
			if (contactLayout != null) {
				callingLayout.setVisibility(View.GONE);
				contactLayout.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public View setSendingMessageView(String name, final String number, Drawable photo, boolean update, int index) {
		final LinearLayout sendingMessageView = (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_contacts_sending_message, null);
//		TextView sendTo = (TextView) sendingMessageView.findViewById(R.id.vs_offline_sending_message_to);
		TextView sendContact = (TextView) sendingMessageView.findViewById(R.id.vs_offline_sending_contact);
//		TextView sendCancel = (TextView) sendingMessageView.findViewById(R.id.vs_offline_sending_cancel);
		final LinearLayout sendCancel = (LinearLayout) sendingMessageView.findViewById(R.id.vs_offline_sending_cancel_layout);
		final AuroraEditText messageEdit = (AuroraEditText) sendingMessageView.findViewById(R.id.vs_offline_sending_message_content_edit);
		final TextView messageContent = (TextView) sendingMessageView.findViewById(R.id.vs_offline_sending_message_content_text);
		final LinearLayout sendControlLayout = (LinearLayout) sendingMessageView.findViewById(R.id.vs_offline_sending_message_control_layout);
		final ImageView resultIcon = (ImageView) sendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result_icon);
		final TextView sendResultText = (TextView) sendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result);
		final ProgressBar sendProgressBar = (ProgressBar) sendingMessageView.findViewById(R.id.vs_offline_sending_message_progressbar);
		
//		ImageView voiceInput = (ImageView) sendingMessageView.findViewById(R.id.vs_offline_message_voice_input);
//		ImageView sendBySim1 = (ImageView) sendingMessageView.findViewById(R.id.vs_offline_message_sending_sim1);
//		ImageView sendBySim2= (ImageView) sendingMessageView.findViewById(R.id.vs_offline_message_sending_sim2);
		LinearLayout voiceInput= (LinearLayout) sendingMessageView.findViewById(R.id.vs_offline_message_voice_input);
		final LinearLayout sendBySim1= (LinearLayout) sendingMessageView.findViewById(R.id.vs_offline_message_sending_sim1);
		final LinearLayout sendBySim2= (LinearLayout) sendingMessageView.findViewById(R.id.vs_offline_message_sending_sim2);
		final ImageView voiceInputButton = (ImageView) voiceInput.getChildAt(0);
		final ImageView sendBySim1Image = (ImageView) sendBySim1.getChildAt(0);
		final ImageView sendBySim2Image = (ImageView) sendBySim2.getChildAt(0);
		
		final ImageView simCardIcon = (ImageView) sendingMessageView.findViewById(R.id.vs_offline_sim_card_icon);
		
		final LinearLayout messageEditLayout= (LinearLayout) sendingMessageView.findViewById(R.id.vs_offline_sending_message_edit_layout);
		final RelativeLayout sendResultLayout= (RelativeLayout) sendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result_layout);
		
		if (name != null) {
			sendContact.setText(name);
		}
		/*if (number != null) {
			numberTextView.setText(number);
		}*/
		/*if (photo != null) {
			photoImageView.setBackground(photo);
		}*/
		
		if (mainActivity.isNetworkConnected(context)) {
			voiceInputButton.setAlpha(1.0f);
		}
		
		if (update) {
			sendingMessageView.setId(index);
		} else {
			sendingMessageView.setId(messageViewIndex);
			registLayoutUpdateListener(sendingMessageView);
			messageViewIndex++;
		}
						
		if (mainActivity.getInsertSimCount() == 2) {
			sendBySim2.setVisibility(View.VISIBLE);
			
			int icon1 = mainActivity.getSimIcon(context, 0);
			int icon2 = mainActivity.getSimIcon(context, 1);
			sendBySim1Image.setImageResource(mainActivity.setSendIconImage(icon1));
			sendBySim2Image.setImageResource(mainActivity.setSendIconImage(icon2));
			
		} else {
			sendBySim1Image.setImageResource(R.drawable.vs_offline_message_send_single_sim);
			sendBySim2.setVisibility(View.GONE);
		}
				
		messageEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (arg0.length() == 0) {
					sendBySim1Image.setAlpha(0.5f);
					sendBySim2Image.setAlpha(0.5f);
				} else {
					sendBySim1Image.setAlpha(1f);
					sendBySim2Image.setAlpha(1f);
				}
			}
		});
				
		voiceInput.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!mainActivity.isNetworkConnected(context)) return;
				
				mainActivity.setVoiceContentForOfflineMessage(null, messageEdit);
				mainActivity.sendBroadcastToShowVoiceInputTips();
			}
		});
		
		sendBySim1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (messageEdit.getText().length() == 0) return;
				
				sendCancel.setVisibility(View.GONE);
				
				InputMethodManager im = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
				im.hideSoftInputFromWindow(sendBySim1.getWindowToken(), 0);
				
				messageEditLayout.setVisibility(View.GONE);
				sendResultLayout.setVisibility(View.VISIBLE);
				messageContent.setText(messageEdit.getText().toString());
				
				int simCardId = mainActivity.getSimIdBySlot(context, 0);
				
				mainActivity.sendMessageByAurora(sendingMessageView, number, messageEdit.getText().toString(), simCardId, simCardIcon);
				
				sendControlLayout.setVisibility(View.GONE);
			}
		});
		sendBySim2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (messageEdit.getText().length() == 0) return;
				
				sendCancel.setVisibility(View.GONE);
				
				InputMethodManager im = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
				im.hideSoftInputFromWindow(sendBySim1.getWindowToken(), 0);
				
				messageEditLayout.setVisibility(View.GONE);
				sendResultLayout.setVisibility(View.VISIBLE);
				messageContent.setText(messageEdit.getText().toString());
				
				int simCardId = mainActivity.getSimIdBySlot(context, 1);
				
				mainActivity.sendMessageByAurora(sendingMessageView, number, messageEdit.getText().toString(), simCardId, simCardIcon);
				
				sendControlLayout.setVisibility(View.GONE);
			}
		});

		sendCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mainActivity.resetFillViewHeight();
				
				LinearLayout messageLayout = (LinearLayout) sendingMessageView.getParent();
				if (messageLayout == null) return;
				LinearLayout messageResultLayout = (LinearLayout) messageLayout.getParent();
				
				if (messageResultLayout != null) {
					LinearLayout contactLayout = (LinearLayout) messageResultLayout.findViewById(R.id.vs_offline_contacts_result_layout);
					if (contactLayout != null) {
						messageLayout.setVisibility(View.GONE);
//						contactLayout.setVisibility(View.VISIBLE);
					}
				}
				
				mainActivity.setOffLineType(OFFLINE_TYPE.TIPS);
								
				TtsRes ttsRes = new TtsRes();
     			ttsRes.flag = true;
     			ttsRes.text = mainActivity.getResources().getString(R.string.vs_offline_answer_message_cancel);
     			
				Message msg = Message.obtain();
				msg.what = MainActivity.MSG_TYPE_TTS;
				msg.obj = ttsRes;
				mHandler.sendMessage(msg);
				
			}
		});
		
		resultIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (sendResultText.getText().equals(mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed))) {
					mGeneralAlertDialog = new AlertDialog.Builder(context)
					.setTitle(mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed_tips))
					.setMessage(mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed_tips_content))
					.setPositiveButton(mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed_resend_yes),
											new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							int id = sendingMessageView.getId();
							if (mLayoutIdHashMap == null) return;
							
							String resendUri = null;
							resendUri = mLayoutIdHashMap.get(String.valueOf(id));
							Log.d("DEBUG", "resendUri-------------= "+resendUri);
							if (resendUri == null) return;
														
//							mainActivity.sendMessageByAurora(sendingMessageView, number, messageEdit.getText().toString(), 0, simCardIcon, resendUri);
							mainActivity.queryMessageDataForResending(sendingMessageView, number, messageEdit.getText().toString(), "0", 
																			simCardIcon, resendUri, sendProgressBar, resultIcon);
							
							sendProgressBar.setVisibility(View.VISIBLE);
		                    resultIcon.setVisibility(View.GONE);
						}
					})
					.setNegativeButton(mainActivity.getResources().getString(R.string.vs_offline_sending_cancel),
											new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							//do nothing
						}
					}).show();
				}
			}
		});
		
		return sendingMessageView;
	}
	
	public View setSearchContactsItemView(final String name, final String number, final LinearLayout numberLayout,
													Drawable photo, final OFFLINE_TYPE type, int size, final String contactId) {
		final LinearLayout searchContactsItemLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_search_contacts_item, null);
		LinearLayout contactsNumberLayout = (LinearLayout) searchContactsItemLayout.findViewById(R.id.vs_offline_search_contact_item_number_layout);
		final LinearLayout contactsControlLayout = (LinearLayout) searchContactsItemLayout.findViewById(R.id.vs_offline_search_contact_control_layout);
				
		TextView nameTextView = (TextView) searchContactsItemLayout.findViewById(R.id.vs_offline_contact_name);
		final TextView numberTextView = (TextView) searchContactsItemLayout.findViewById(R.id.vs_offline_contact_number);
//		final ImageView arrow = (ImageView) searchContactsItemLayout.findViewById(R.id.arrow_offline);
//		ImageView photoImageView = (ImageView) contactItemLayout.findViewById(R.id.vs_offline_contact_image);
		TextView editContact = (TextView) searchContactsItemLayout.findViewById(R.id.vs_offline_search_contact_edit);
		TextView forwardContact = (TextView) searchContactsItemLayout.findViewById(R.id.vs_offline_search_contact_forward);
		
		searchContactsItemLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "searchContactsItemLayout-------getchildcount = "+searchContactsItemLayout.getChildCount());
				if (numberLayout != null) {
					if (numberLayout.getVisibility() == View.VISIBLE) {
						/*numberLayout.setVisibility(View.GONE);
						numberTextView.setVisibility(View.VISIBLE);*/
						
						/*if (arrow.getVisibility() == View.VISIBLE) {
							arrow.setBackgroundResource(R.drawable.vs_help_arrows_normal);
						}*/
						
					} else {
						numberLayout.setVisibility(View.VISIBLE);
						numberTextView.setVisibility(View.GONE);
						
						contactsControlLayout.setVisibility(View.VISIBLE);
						/*if (arrow.getVisibility() == View.VISIBLE) {
							arrow.setBackgroundResource(R.drawable.vs_help_arrows_pressed);
						}*/
						LinearLayout contactLayout = (LinearLayout) searchContactsItemLayout.getParent();
						if (contactLayout == null) return;

						updateContactListView(contactLayout, searchContactsItemLayout);
					}
					
				}
			}
		});
		
		if (name != null) {
			nameTextView.setText(name);
		}

		if (numberLayout != null) {
			if (numberLayout.getVisibility() == View.GONE && numberLayout.getChildCount() > 1) {
				numberTextView.setText(R.string.vs_offline_contact_item_manynumbers);
				numberTextView.setTextColor(mainActivity.getResources().getColor(R.color.vs_half_white));
				
				contactsControlLayout.setVisibility(View.GONE);
			} else {
				String tempNumber = number;
				tempNumber = mainActivity.getFormattedNumber(tempNumber);
				numberTextView.setText(tempNumber);
//				numberTextView.setVisibility(View.GONE);
				
				if (size > 1) {
					contactsControlLayout.setVisibility(View.GONE);
				} else {
					numberTextView.setVisibility(View.GONE);
				}
			}
//			arrow.setVisibility(View.VISIBLE);
			
//			searchContactsItemLayout.addView(numberLayout);
			contactsNumberLayout.addView(numberLayout);
		}
				
		editContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mainActivity.editContactsByContactId(contactId);
			}
		});
		forwardContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String nameTitle = mainActivity.getResources().getString(R.string.vs_offline_contact_name_title);
				StringBuilder message = new StringBuilder(nameTitle);
				message.append(name);
				
				for (int i = 0; i < numberLayout.getChildCount(); i++) {
					LinearLayout searchContactsNumberLayout =  (LinearLayout) numberLayout.getChildAt(i);
					if (searchContactsItemLayout == null) break;
					
					TextView numberType = (TextView) searchContactsNumberLayout.findViewById(R.id.vs_offline_contact_number_type);
					TextView numberString = (TextView) searchContactsNumberLayout.findViewById(R.id.vs_offline_contact_number_number);
					
					message.append("\n" + numberType.getText().toString() + ":");
					message.append(numberString.getText().toString());
				}
				
				mainActivity.sendMessageBySystem("", 0, message);

			}
		});
		
		return searchContactsItemLayout;
		
	}
	
	public View setSearchContactsNumberItemView(final String name, String numbertype, final String number, OFFLINE_TYPE type) {
		final LinearLayout searchContactsNumberLayout =  (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_search_contacts_number_layout, null);
		
		TextView numberType = (TextView) searchContactsNumberLayout.findViewById(R.id.vs_offline_contact_number_type);
		TextView numberString = (TextView) searchContactsNumberLayout.findViewById(R.id.vs_offline_contact_number_number);
		LinearLayout numberNumberLayout = (LinearLayout) searchContactsNumberLayout.findViewById(R.id.vs_offline_search_contacts_number_number_layout);
		ImageView sendMessage = (ImageView) searchContactsNumberLayout.findViewById(R.id.vs_offline_contact_send_message);
		
		if (numbertype != null) {
			numberType.setText(numbertype);
		}
		
		if (number != null) {
			String tempNumber = number;
			tempNumber = mainActivity.getFormattedNumber(tempNumber);
			numberString.setText(tempNumber);
//			numberString.setText(number);
		}
				
		numberNumberLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mainActivity.getInsertSimCount() == 2) {
					LinearLayout contactNumberLayout = (LinearLayout) searchContactsNumberLayout.getParent();
					if (contactNumberLayout == null) return;
					LinearLayout numberLayout = (LinearLayout) contactNumberLayout.getParent();
					if (numberLayout == null) return;
					LinearLayout contactItemLayout = (LinearLayout) numberLayout.getParent();
					if (contactItemLayout == null) return;
					LinearLayout contactLayout = (LinearLayout) contactItemLayout.getParent();
					if (contactLayout == null) return;
					
					showCallingOrMessageView(name, number, contactLayout, OFFLINE_TYPE.CALLING);
					
				} else {
					mainActivity.callContactBySimCard(number, mainActivity.getLastCallSlotId(number));
				}
				
			}
		});
		sendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout contactNumberLayout = (LinearLayout) searchContactsNumberLayout.getParent();
				if (contactNumberLayout == null) return;
				LinearLayout numberLayout = (LinearLayout) contactNumberLayout.getParent();
				if (numberLayout == null) return;
				LinearLayout contactItemLayout = (LinearLayout) numberLayout.getParent();
				if (contactItemLayout == null) return;
				LinearLayout contactLayout = (LinearLayout) contactItemLayout.getParent();
				if (contactLayout == null) return;
				
				showCallingOrMessageView(name, number, contactLayout, OFFLINE_TYPE.MESSAGE);
			}
		});
		
		return searchContactsNumberLayout;
	}
	
	public void setResultLayoutFromContactsInfo(ContactsItem mContactsItem, LinearLayout callingLayout, 
												LinearLayout messageLayout, LinearLayout contactLayout, int size, OFFLINE_TYPE type) {
		
		LinearLayout numberLayout = new LinearLayout(context);
		numberLayout.setOrientation(LinearLayout.VERTICAL);
		
		String contactId = mContactsItem.getContactId();
		String nameString = mContactsItem.getContactName();
		String typeString = null;
		String numberString = null;
		Drawable photoDrawable = null;
		
		LinkedHashMap<NumberType, String> mNumberMap = mContactsItem.getContactNumberMap();
		if (mNumberMap == null) return;
		
		int mNumberCount = mNumberMap.size();
		Iterator mIterator = mNumberMap.keySet().iterator();
		if (mIterator == null) return;
		
		while (mIterator.hasNext()) {
			NumberType mNumberType = (NumberType) mIterator.next();
			numberString = mNumberMap.get(mNumberType);
			Log.d("DEBUG", "setResultLayoutFromContactsInfo------------- mNumberType = "+mNumberType+"numberString = "+numberString);
			switch (mNumberType) {
				case NUMBER_HOME:
					typeString = mainActivity.getResources().getString(R.string.vs_offline_contact_number_type_home);
					break;
				case NUMBER_MOBLIE:
					typeString = mainActivity.getResources().getString(R.string.vs_offline_contact_number_type_mobile);		
					break;
				case NUMBER_WORK:
					typeString = mainActivity.getResources().getString(R.string.vs_offline_contact_number_type_work);
					break;
				case NUMBER_WORKFAX:
					typeString = mainActivity.getResources().getString(R.string.vs_offline_contact_number_type_workfax);
					break;
				case NUMBER_OTHER:
					typeString = mainActivity.getResources().getString(R.string.vs_offline_contact_number_type_other);
					break;
				default:
					typeString = mainActivity.getResources().getString(R.string.vs_offline_contact_number_type_mobile);
					break;
			}
			
			if (type == OFFLINE_TYPE.CONTACTS) {
				View numberView = null;
				numberView = setSearchContactsNumberItemView(nameString, typeString, numberString, type);
				numberLayout.addView(numberView);
				if (size > 1) {
					numberLayout.setVisibility(View.GONE);
				}
			
			} else {
				if (mNumberCount > 1) {
					View numberView = null;
					numberView = setNumberItemView(nameString, typeString, numberString, type);
					numberLayout.addView(numberView);
					numberLayout.setVisibility(View.GONE);
				} else {
					numberLayout = null;
				}
			}
		}
		Log.d("DEBUG", "ContentViewConstructor---------setResultLayoutFromContactsInfo------------- type = "+type);
	
		/* Add one item to contactLayout
		* There is only contactLayout at OFFLINE_TYPE.CONTACTS mode
		*/
		View tempView = null;
		if (type == OFFLINE_TYPE.CONTACTS) {
			tempView = setSearchContactsItemView(nameString, numberString, numberLayout, photoDrawable, type, size, contactId);
			contactLayout.setVisibility(View.VISIBLE);
			
		} else {
			if (mNumberCount == 1 && size == 1) {
				View callOrMessageView = null;
				if (type == OFFLINE_TYPE.CALLING) {
					callOrMessageView = setCallingView(nameString, numberString, photoDrawable);
					callingLayout.addView(callOrMessageView);
					callingLayout.setVisibility(View.VISIBLE);
					
				} else if (type == OFFLINE_TYPE.MESSAGE) {
					callOrMessageView = setSendingMessageView(nameString, numberString, photoDrawable, false, 0);
					messageLayout.addView(callOrMessageView);
					messageLayout.setVisibility(View.VISIBLE);
				}
				
				if (mainActivity.isAirPlaneModeOn(context) || mainActivity.getInsertSimCount() <= 0 || mainActivity.getSIMCardState() == 1) {
					if (callingLayout.getVisibility() == View.VISIBLE) {
						callingLayout.setVisibility(View.GONE);
					}
					if (messageLayout.getVisibility() == View.VISIBLE) {
						messageLayout.setVisibility(View.GONE);
					}
					contactLayout.setVisibility(View.VISIBLE);
				}
				
			} else {
				contactLayout.setVisibility(View.VISIBLE);
			}
			
			tempView = setContactItemView(nameString, numberString, numberLayout, photoDrawable, type);
			
		}
		contactLayout.addView(tempView);
	
	}
	
	public View getApplicationResultView(String contentString) {
		LinearLayout mAppsLayout =  (LinearLayout) mLayoutInflater.inflate(R.layout.vs_offline_applications_layout, null);
		
		CustomAppsContainer mAppsContainer = (CustomAppsContainer) mAppsLayout.findViewById(R.id.vs_offline_apps_container_layout);
		
		mAppsItemList = mainActivity.getAppsItemsList();
		if (mAppsItemList == null) return null;
		
		int mAppSize = mAppsItemList.size();
		Log.i("DEBUG", "getApplicationResultView---------mAppsItemList.size() = "+mAppSize);
		if (mAppSize <= 0) return null;
		
		for (int i = 0; i < mAppSize; i++) {
			AppsItem mAppsItem = mAppsItemList.get(i);
			
			final ShadowTextView mShadowTextView = new ShadowTextView(mainActivity);
			mShadowTextView.applyFromShortcutInfo(mAppsItem.getAppDrawable(), mAppsItem.getAppName());
			final Intent appIntent = mAppsItem.getAppIntent();
			mShadowTextView.setTag(appIntent);
			
			if (mAppsItem.getAppName().equals(mainActivity.getResources().getString(R.string.vs_app_name))) {
				continue;
			}
			mAppsContainer.addView(mShadowTextView);

			mShadowTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					mainActivity.startActivity(appIntent);
				}
			});
			
			if (mAppSize == 1) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mainActivity.startActivity(appIntent);
					}
				});
			}
		}
		
		return mAppsLayout;
	}
	
	@Override
	public void registLayoutUpdateListener(ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		if(viewGroup !=null){
			mLayoutArrayList.add(viewGroup);
		}
	}

	@Override
	public void unRegistLayoutUpdateListener(ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		if(viewGroup != null){
			if(mLayoutArrayList.contains(viewGroup)){
				mLayoutArrayList.remove(viewGroup);
			}
		}
	}
	
	@Override
	public void unRegistAllLayout() {
		// TODO Auto-generated method stub
		if (mLayoutArrayList != null) {
			mLayoutArrayList.clear();
		}
	}

	@Override
	public void updateVoiceButtonAlpha(boolean flag) {
		// TODO Auto-generated method stub
		for(ViewGroup mViewGroup :mLayoutArrayList){
			LinearLayout voiceInput= (LinearLayout) mViewGroup.findViewById(R.id.vs_offline_message_voice_input);
			if (voiceInput == null) return;
			
			ImageView voiceInputButton = (ImageView) voiceInput.getChildAt(0);
			if(voiceInputButton != null && voiceInputButton instanceof ImageView){
				if(flag){
					voiceInputButton.setAlpha(1.0f);
				}else{
					voiceInputButton.setAlpha(0.5f);
				}
			}
		}
	}
	
	@Override
	public void addLayoutIdMap(String id, String uriString) {
		// TODO Auto-generated method stub
		mLayoutIdHashMap.put(id, uriString);
	}

	@Override
	public void clearLayoutIdMap() {
		// TODO Auto-generated method stub
		if (mLayoutIdHashMap != null) {
			mLayoutIdHashMap.clear();
		}
	}

	@Override
	public void updateMessageSentState(String id, String uriString, int errorCode) {
		// TODO Auto-generated method stub
		if (mLayoutArrayList.size() == 0) return;		//if the size of list is 0, it means the broadcast received has been out of time
		
		int layoutId = -1;
		if (id != null && uriString != null) {
			addLayoutIdMap(id, uriString);
			layoutId = Integer.valueOf(id);
			
		} else if (id == null && uriString != null) {
			if (mLayoutIdHashMap == null) return;
			
			Iterator mIterator = mLayoutIdHashMap.keySet().iterator();
			if (mIterator == null) return;
			
			while (mIterator.hasNext()) {
				String uri = mIterator.next().toString();
				if (uri.equals(uriString)) {
					String idString = mLayoutIdHashMap.get(uri);
					layoutId = Integer.valueOf(idString);
					break;
				} else {
					return;
				}
			}
		}
		
		if (layoutId == -1) return;
		
		Log.d("DEBUG", "id = "+layoutId);
		LinearLayout mSendingMessageView = (LinearLayout) mLayoutArrayList.get(layoutId);
		if (mSendingMessageView == null) return;
		
		final ImageView resultIcon = (ImageView) mSendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result_icon);
		final TextView resultText = (TextView) mSendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result);
		final ProgressBar sendProgressBar = (ProgressBar) mSendingMessageView.findViewById(R.id.vs_offline_sending_message_progressbar);
		
		final String sending = mainActivity.getResources().getString(R.string.vs_offline_sending_message_sending);
		final String sendingFailed = mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed);
		Log.d("DEBUG", "updateMessageSentState-------------errorCode = "+errorCode);
		switch(errorCode) {
            case Activity.RESULT_OK:
                Log.d("DEBUG", "updateMessageSentState-------------Activity.RESULT_OK");
                //send operation is ok
                resultText.setText(mainActivity.getSystemTime());
                sendProgressBar.setVisibility(View.GONE);
                resultIcon.setBackgroundResource(R.drawable.vs_offline_message_send_success);
                resultIcon.setVisibility(View.VISIBLE);
                		
                break;
            case 0:
            	Log.d("DEBUG", "updateMessageSentState-------------errorCode == 0");
            	if (mainActivity.isAirPlaneModeOn(context) || mainActivity.getInsertSimCount() <= 0 || mainActivity.getSIMCardState() == 1) {
	                sendProgressBar.setVisibility(View.GONE);
	                resultIcon.setBackgroundResource(R.drawable.vs_offline_message_send_failed);
	                resultIcon.setVisibility(View.VISIBLE);
	                
	                resultText.setText(mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed));
				} else {
					resultText.setText(mainActivity.getSystemTime());
	                sendProgressBar.setVisibility(View.GONE);
	                resultIcon.setBackgroundResource(R.drawable.vs_offline_message_send_success);
	                resultIcon.setVisibility(View.VISIBLE);
				}
            	
            	break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                Log.i("DEBUG", "sendMessageDerectly-------------RESULT_ERROR_GENERIC_FAILURE");

            case SmsManager.RESULT_ERROR_NO_SERVICE:
//                Log.i("DEBUG", "sendMessageDerectly-------------RESULT_ERROR_NO_SERVICE");

            case SmsManager.RESULT_ERROR_NULL_PDU:
//                Log.i("DEBUG", "sendMessageDerectly-------------RESULT_ERROR_NULL_PDU");

            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Log.d("DEBUG", "updateMessageSentState-------------RESULT_ERROR_RADIO_OFF--------send failed");
                /*sendProgressBar.setVisibility(View.GONE);
                resultIcon.setBackgroundResource(R.drawable.vs_offline_message_send_failed);
                resultIcon.setVisibility(View.VISIBLE);
                
                resultText.setText(mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed));
                
                break;*/
            default:
            	Log.d("DEBUG", "updateMessageSentState-------------send failed-----errorCode = "+errorCode);
            	sendProgressBar.setVisibility(View.GONE);
                resultIcon.setBackgroundResource(R.drawable.vs_offline_message_send_failed);
                resultIcon.setVisibility(View.VISIBLE);
                
                resultText.setText(mainActivity.getResources().getString(R.string.vs_offline_sending_message_failed));
                
            	break;
            
		}
	}	
	//Offline end
	
	public View getJSONDataResultView(Response response) {
		Response.Item rspItem;
		View view = null;
	    
		Iterator<Response.Item> resultIter = response.getList().iterator();
		while(resultIter.hasNext()) {
			rspItem = resultIter.next();
			RspJson rspJson = rspItem.getRspjson();
			if (rspJson == null) return null;
			
			JSONObject jsonObjectData = rspJson.getJSONObjectData();
			
			String description = jsonObjectData.optString("description");
			Log.d("DEBUG", "the description = "+description);
			if (description == null) return null;
			
			if (description.equals(CFG.DESCRIPTION_TYPE_REMIND_ALARM)) {
				String cmdType = jsonObjectData.optString("cmd");
				if (cmdType == null) {
					cmdType = "add";
				}
				
				RspJsonReminderAlarm mReminderAlarm = new RspJsonReminderAlarm(context, jsonObjectData);
				ArrayList<Event> mEventList = mainActivity.getEventArrayList();
//				mEventList.clear();
				
				if (cmdType.equals("view")) {
					/*Time mTime = new Time();
					mTime.year = mReminderAlarm.getReminderDateYear();
					mTime.month = mReminderAlarm.getReminderDateMonth();
					mTime.monthDay = mReminderAlarm.getReminderDateDay();
					mTime.normalize(true);
					Long timeLong = mTime.toMillis(true);
					
					int time = mTime.getJulianDay(timeLong, mTime.gmtoff);
					int duration = mReminderAlarm.getReminderDaysDuration();
					
//					CalendarOperation.loadEvents(context, time, duration, 0);
					
					Event.loadEvents(context, mEventList, time, duration);
					
					for (int i = 0; i < mEventList.size(); i++) {
						Event mEventItem = mEventList.get(i);
						Log.d("DEBUG", "mEventItem.title = "+mEventItem.title);
						Log.d("DEBUG", "mEventItem.description = "+mEventItem.description);
						Log.d("DEBUG", "mEventItem.getStartMillis() = "+mEventItem.getStartMillis());
						Log.d("DEBUG", "mEventItem.startMillis = "+mEventItem.startMillis);
						Log.d("DEBUG", "mEventItem.startTime = "+mEventItem.startTime);
						Log.d("DEBUG", "mEventItem.startDay = "+mEventItem.startDay);
						Log.d("DEBUG", "mEventItem.dtstart = "+mEventItem.dtstart);
						
						String stringTime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(mEventItem.getStartMillis());
						Log.d("DEBUG", "the stringTime = "+stringTime);
						String stringTime1 = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(mEventItem.startTime);
						Log.d("DEBUG", "the stringTime1 = "+stringTime1);
						String stringTime2 = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(mEventItem.dtstart);
						Log.d("DEBUG", "the stringTime2 = "+stringTime2);
						
					}*/
					
//						Time mTime = new Time();
					/*Calendar beginTime = Calendar.getInstance();
					beginTime.set(beginTime.get(Calendar.YEAR),
							beginTime.get(Calendar.MONTH),
							beginTime.get(Calendar.DAY_OF_MONTH),
							beginTime.get(Calendar.HOUR_OF_DAY),
							beginTime.get(Calendar.MINUTE));*/
					
//						mTime.year = (int)2015;
//						mTime.month = (int)1;
//						mTime.monthDay = (int)9;
//						mTime.normalize(true);
//						Long timeLong = mTime.toMillis(true);
//						
//						String stringTime = new SimpleDateFormat("yyyy/MM/dd hh:mm").format(new Date(timeLong));
//						Log.d("DEBUG", "getjulianday string time = "+stringTime);
//						
//						int time = mTime.getJulianDay(timeLong, mTime.gmtoff);
//						Log.d("DEBUG", "getjulianday = "+time);
//						
//						CalendarOperation.loadEvents(context, time, 1, 0);
					
					/*ContentResolver cr = mainActivity.getContentResolver();
	                Cursor cur = cr.query(Events.CONTENT_URI, new String[] {
	                        Events._ID, Events.TITLE, Events.DESCRIPTION,
	                        Events.DTSTART, Events.DTEND },
	                 Events._ID + "=" + myEventsId null, null, null); // 注释中的条件是是查询特定ID的events
	                Log.d("DEBUG", "the count  = "+cur.getCount());
	                while (cur.moveToNext()) {
	                    Long tempEventsId = cur.getLong(0);
	                    String tempEventsTitle = cur.getString(1);
	                    String tempEventsDecription = cur.getString(2);
	                    String tempEventsStartTime = cur.getString(3);
	                    String tempEventsEndTime = cur.getString(4);
	                    
	                    Log.d("DEBUG", "tempEventsTitle = "+tempEventsTitle);
	                    Log.d("DEBUG", "tempEventsDecription = "+tempEventsDecription);
	                    Log.d("DEBUG", "tempEventsStartTime = "+tempEventsStartTime);
	                    Log.d("DEBUG", "tempEventsEndTime = "+tempEventsEndTime);
	                    
	                    String beginTime = new SimpleDateFormat("yyyy/MM/dd hh:mm").format(new Date(Long.parseLong(tempEventsStartTime)));
	                    Log.d("DEBUG", "the beginTime = "+beginTime);
	                    String endTime = new SimpleDateFormat("yyyy/MM/dd hh:mm").format(new Date(Long.parseLong(tempEventsEndTime)));
	                    Log.d("DEBUG", "the beginTime = "+endTime);
	                    
	                }
	                cur.close();*/
				}
				
				view = setReminderAlarmView(mReminderAlarm, cmdType, mEventList, response);
				
				
				
			}
		}
		return view;
	}
	
	@SuppressLint("InlinedApi")
	public void scheduleCalendarEvent(String content, Integer year, Integer month, Integer day, String hour, 
														String minute, ALARM_TYPE alarmType, String rule) {
		CalendarHelper mCalendarHelper = new CalendarHelper(context);
		mCalendarHelper.ProgramName			= content;
		mCalendarHelper.EVENT_TITLE			= content;
		mCalendarHelper.DST_YEAR 			= year;
		mCalendarHelper.DST_MONTH 			= month > 0? month : 0;	//the value of month is 0~11
		mCalendarHelper.DST_DAY_OF_MONTH	= day;
		mCalendarHelper.DST_HOUR_OF_DAY		= Integer.valueOf(hour);
		mCalendarHelper.DST_MINUTES			= Integer.valueOf(minute);
		mCalendarHelper.EVENT_HAS_ALARM		= 1;
		if (alarmType == ALARM_TYPE.ONCE) {
			mCalendarHelper.RRULE			= null;
		} else {
			mCalendarHelper.RRULE			= rule;
			mCalendarHelper.DURATION		= "P1800S";
		}
		
		
		mCalendarHelper.scheduleNewEvent();
		
		AlarmHelper mAlarmHelper	= new AlarmHelper(context);
		mAlarmHelper.Hour			= hour;
		mAlarmHelper.Minutes		= minute;
		mAlarmHelper.Enable			= "true";
		mAlarmHelper.Message		= content;
//		mAlarmHelper.DaysofWeek	= "3";
		
		mAlarmHelper.scheduleNewEvent();
	}
	
	public View setReminderAlarmView(final RspJsonReminderAlarm mReminderAlarm, String cmdType, ArrayList<Event> eventList, Response response) {
		LinearLayout mReminderLayout =  (LinearLayout) mLayoutInflater.inflate(R.layout.vs_reminder_alarm_layout, null);
		
		final LinearLayout mReminderAddLayout = (LinearLayout) mReminderLayout.findViewById(R.id.vs_reminder_alarm_add_layout);
		LinearLayout mReminderTitleLayout = (LinearLayout) mReminderLayout.findViewById(R.id.vs_reminder_alarm_add_title_layout);
		final LinearLayout mReminderControlLayout = (LinearLayout) mReminderLayout.findViewById(R.id.vs_reminder_alarm_control_layout);
		
		TextView mReminderContent = (TextView) mReminderLayout.findViewById(R.id.vs_reminder_alarm_add_content);
		TextView mReminderDate = (TextView) mReminderLayout.findViewById(R.id.vs_reminder_alarm_add_date);
		TextView mReminderTime = (TextView) mReminderLayout.findViewById(R.id.vs_reminder_alarm_add_time);
		TextView mReminderSet = (TextView) mReminderLayout.findViewById(R.id.vs_reminder_alarm_set);
		TextView mReminderCancel = (TextView) mReminderLayout.findViewById(R.id.vs_reminder_alarm_cancel);
		
		final RelativeLayout mReminderCheckLayout = (RelativeLayout) mReminderLayout.findViewById(R.id.vs_reminder_alarm_check_layout);
		LinearLayout mReminderQueryLayout = (LinearLayout) mReminderLayout.findViewById(R.id.vs_reminder_alarm_query_layout);
		
		if (cmdType.equals("add")) {
			mReminderCheckLayout.setVisibility(View.GONE);
			mReminderQueryLayout.setVisibility(View.GONE);
			
			mReminderContent.setText(mReminderAlarm.getReminderContent());
			mReminderDate.setText(mReminderAlarm.getReminderDateMonth() + 1 + "月" + mReminderAlarm.getReminderDateDay() + "日");
			mReminderTime.setText(mReminderAlarm.getReminderTimeHour() + ":" + mReminderAlarm.getReminderTimeMinute());
			
			String ruleString = mReminderAlarm.getAlarmRule();
			
			EventRecurrence eventRecurrence = new EventRecurrence();
			
			switch (mReminderAlarm.getAlarmType()) {
				case ONCE:
//					eventRecurrence.freq = EventRecurrence.YEARLY;
					break;
					
				case EVERY_DAY:
					eventRecurrence.freq = EventRecurrence.DAILY;
					break;
					
				case EVERY_WEEK:
					eventRecurrence.freq = EventRecurrence.WEEKLY;
					eventRecurrence.wkst = EventRecurrence.calendarDay2Day(3 + 1);
					break;
					
				case EVERY_MONTH:
					eventRecurrence.freq = EventRecurrence.MONTHLY;
					break;
					
				case EVERY_YEAR:
					eventRecurrence.freq = EventRecurrence.YEARLY;
					break;
	
				default:
					break;
			}
			
//			eventRecurrence.freq = EventRecurrence.YEARLY;
//			eventRecurrence.wkst = EventRecurrence.calendarDay2Day(3 + 1);
			final String rule = eventRecurrence.toString();
			Log.d("DEBUG", "add calendar rule = "+rule);
			
			Calendar time = Calendar.getInstance();
	        time.set(mReminderAlarm.getReminderDateYear(), 
					  mReminderAlarm.getReminderDateMonth(), 
					  mReminderAlarm.getReminderDateDay(), 
					  Integer.valueOf(mReminderAlarm.getReminderTimeHour()), 
					  Integer.valueOf(mReminderAlarm.getReminderTimeMinute()));
	        final long startMillis = time.getTimeInMillis();
			
			mReminderTitleLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Log.d("DEBUG", "getReminderAlarmView-----------mReminderTitleLayout onClick");
					Intent intent = new Intent(Intent.ACTION_MAIN);//Uri.parse("com.android.calendar.AllInOneActivity");
					ComponentName cn = new ComponentName("com.android.calendar", "com.android.calendar.AllInOneActivity");
					intent.setComponent(cn);
					mainActivity.startActivity(intent);
				}
			});
			
			mReminderCheckLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Log.d("DEBUG", "getReminderAlarmView-----------mReminderCheckLayout onClick");
										
					Intent intent = new Intent(Intent.ACTION_MAIN);//Uri.parse("com.android.calendar.AllInOneActivity");
					ComponentName cn = new ComponentName("com.android.calendar", "com.android.calendar.AllInOneActivity");
					intent.setComponent(cn);
					intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
					mainActivity.startActivity(intent);
				}
			});
			
			mReminderSet.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Log.d("DEBUG", "getReminderAlarmView-----------mReminderSet onClick");
					
					scheduleCalendarEvent(mReminderAlarm.getReminderContent(), 
										  mReminderAlarm.getReminderDateYear(), 
										  mReminderAlarm.getReminderDateMonth(), 
										  mReminderAlarm.getReminderDateDay(), 
										  mReminderAlarm.getReminderTimeHour(), 
										  mReminderAlarm.getReminderTimeMinute(), 
										  mReminderAlarm.getAlarmType(), rule);
					
					mReminderControlLayout.setVisibility(View.GONE);
					mReminderCheckLayout.setVisibility(View.VISIBLE);
				}
			});
			
			mReminderCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Log.d("DEBUG", "getReminderAlarmView-----------mReminderCancel onClick");
					mReminderAddLayout.setVisibility(View.GONE);
					
					mainActivity.setOffLineType(OFFLINE_TYPE.TIPS);
					mainActivity.resetFillViewHeight();
					
					TtsRes ttsRes = new TtsRes();
	     			ttsRes.flag = true;
	     			ttsRes.text = mainActivity.getResources().getString(R.string.vs_reminder_alarm_cancel);
	     			
					Message msg = Message.obtain();
					msg.what = MainActivity.MSG_TYPE_TTS;
					msg.obj = ttsRes;
					mHandler.sendMessage(msg);
				}
			});
			
		} else {
			mReminderAddLayout.setVisibility(View.GONE);
			mReminderCheckLayout.setVisibility(View.GONE);
			
			
			TextView queryDate = (TextView) mReminderQueryLayout.findViewById(R.id.vs_reminder_alarm_query_date);
			
			if ("1".equals(mReminderAlarm.getDateType())) {
				String requeString = response.getRequestString();
				int index = requeString.indexOf("周");
				if (index == -1) {
					index = requeString.indexOf("月");
				}
				String tempString = requeString.substring(index - 1, index + 1);
				queryDate.setText(tempString);
				
			} else {
				queryDate.setText(mReminderAlarm.getReminderDateMonth() + 1 + "月" + mReminderAlarm.getReminderDateDay() + "日");	//month will add 1 and show
			}
			
			int size = eventList.size();
			Log.d("DEBUG", "eventList.size() = "+size);
			
			if (size <= 0) {
				mReminderQueryLayout.setVisibility(View.GONE);
				
			} else {
				mReminderQueryLayout.setVisibility(View.VISIBLE);
			}
			
			for (int i = 0; i < size; i++) {
				Event eventItem = eventList.get(i);
				
				LinearLayout reminderItem = (LinearLayout) mLayoutInflater.inflate(R.layout.vs_reminder_alarm_item_layout, null);
				TextView reminderTitle = (TextView) reminderItem.findViewById(R.id.vs_reminder_alarm_item_content);
				TextView reminderTime = (TextView) reminderItem.findViewById(R.id.vs_reminder_alarm_item_time);
				TextView reminderType = (TextView) reminderItem.findViewById(R.id.vs_reminder_alarm_item_type);
				
				reminderTitle.setText(eventItem.title);
				
				String beginTime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(eventItem.getStartMillis());                
                int index = beginTime.indexOf(":");
                if (index != -1) {
					beginTime = beginTime.substring(index - 2, index + 3);
					Log.d("DEBUG", "the final beginTime = "+beginTime);
				}
				reminderTime.setText(beginTime);
				
				String repeatString = "一次性";
				Log.d("DEBUG", "eventItem.isRepeating = "+eventItem.isRepeating);
				if (eventItem.isRepeating) {
					EventRecurrence eventRecurrence = new EventRecurrence();
					eventRecurrence.parse(eventItem.rRule);
					Time date = new Time(eventItem.timeZone);
					date.set(eventItem.getStartMillis());
					if (eventItem.allDay) {
						date.timezone = Time.TIMEZONE_UTC;
					}
					eventRecurrence.setStartDate(date);
					
					Log.d("DEBUG", "-----------eventRecurrence.freq = "+eventRecurrence.freq);
					
					String tempDayOfWeek = null;
					if (mReminderAlarm.getReminderDate() != null) {
						try {
							String tempDate = new SimpleDateFormat("yyyyMMdd").format(eventItem.getStartMillis());
							Date mDate = new SimpleDateFormat("yyyyMMdd").parse(tempDate);
							Calendar tempCalendar = Calendar.getInstance();
							tempCalendar.setTime(mDate);
							
							switch (tempCalendar.get(Calendar.DAY_OF_WEEK)) {
							case 1:
								tempDayOfWeek = "日";
								break;
							case 2:
								tempDayOfWeek = "一"	;
								break;
							case 3:
								tempDayOfWeek = "二";
								break;
							case 4:
								tempDayOfWeek = "三";
								break;
							case 5:
								tempDayOfWeek = "四";
								break;
							case 6:
								tempDayOfWeek = "五";
								break;
							case 7:
								tempDayOfWeek = "六";
								break;
							}
						} catch (Exception e) {
							// TODO: handle exception
							Log.d("DEBUG", "repeatString-----------Exception e = "+e);
						}
						
					}
					
					switch (eventRecurrence.freq) {
					case EventRecurrence.DAILY:				//EventRecurrence.DAILY = 4
						Log.d("DEBUG", "-----------EventRecurrence.DAILY");
						repeatString = "每天";
						break;
					case EventRecurrence.WEEKLY:			//EventRecurrence.WEEKLY = 5
						Log.d("DEBUG", "-----------EventRecurrence.WEEKLY");
						repeatString = tempDayOfWeek == null? "每周" : ("每周" + tempDayOfWeek);
						break;
					case EventRecurrence.MONTHLY:			//EventRecurrence.MONTHLY = 6
						Log.d("DEBUG", "-----------EventRecurrence.MONTHLY");
						repeatString = "每月";
						break;
					case EventRecurrence.YEARLY:			//EventRecurrence.YEARLY = 7
						Log.d("DEBUG", "-----------EventRecurrence.YEARLY");
						repeatString = "每年";
						break;
					}
				}

				reminderType.setText(repeatString);
				
				mReminderQueryLayout.addView(reminderItem);
			}
			
		}
		
		return mReminderLayout;
	}
	
	//M:shigq fix bug #13340 start
	public void dismissGeneralDialog() {
		if (mGeneralAlertDialog != null) {
			mGeneralAlertDialog.dismiss();
		}
	}
	//M:shigq fix bug #13340 end

}
