package com.aurora.iunivoice.activity;

import java.util.ArrayList;
import java.util.Collection;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.app.ActionBar.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncContext;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.SystemMsg;
import com.aurora.datauiapi.data.bean.SystemMsgHolder;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.adapter.MineNewsAdapter;
import com.aurora.iunivoice.utils.DensityUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.LoadingPageUtil;
import com.aurora.iunivoice.utils.SystemUtils;

public class MineNewsActivity extends BaseActivity {

	private PullToRefreshListView lv_news;
	private LinearLayout ll_no_news;

	private IuniVoiceManager manager;
	
	private ArrayList<SystemMsg> datas = new ArrayList<SystemMsg>();
	
	private MineNewsAdapter adapter;
	
	private static final int UNREAD_MSG = 0x111445;
	
	private RefreshMode refreshMode = RefreshMode.REFRESH;
	
	private ArrayList<SystemMsg> alreadyNews = new ArrayList<SystemMsg>();
	private ArrayList<SystemMsg> unAlreadyNews = new ArrayList<SystemMsg>();
	private ArrayList<SystemMsg> allNews = new ArrayList<SystemMsg>();
	
	private PopupWindow popupWindow;

	private int curentPage = 1;
       
	private LinearLayout ll_error;
	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mine_news);
		manager = new IuniVoiceManager(this);
		setupViews();
		getNetData();
	}
	
	
	private void createPopwindow(){
		View popView = LayoutInflater.from(this).inflate(R.layout.popwindow_mine_news, null);
		popView.findViewById(R.id.btn_unread).setOnClickListener(popwindowClickListener);
		popView.findViewById(R.id.btn_read).setOnClickListener(popwindowClickListener);
		popView.findViewById(R.id.btn_all).setOnClickListener(popwindowClickListener);
		popupWindow = new PopupWindow(popView, DensityUtil.dip2px(this, 168),  LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.message_pop_bg));
	}
	
	private NewsType currentNewsType = NewsType.All_News;
	
	enum NewsType{
		All_News,Already_News,Not_Read_News;
	}
	
	private OnClickListener popwindowClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			datas.clear();
			switch (arg0.getId()) {
			case R.id.btn_unread:
				currentNewsType = NewsType.Not_Read_News;
				datas.addAll(unAlreadyNews);
				break;
			case R.id.btn_read:
				currentNewsType = NewsType.Already_News;
				datas.addAll(alreadyNews);
				break;
			case R.id.btn_all:
				currentNewsType = NewsType.All_News;
				datas.addAll(allNews);
				break;
			}
			adapter.notifyDataSetChanged();
			checkHasNews();
			popupWindow.dismiss();
		}
	};
	
	public void fillterAllNews() {
		alreadyNews.clear();
		unAlreadyNews.clear();
		for (SystemMsg systemMsg : allNews) {
			if (systemMsg.getStatus().equals("0")) {
				unAlreadyNews.add(systemMsg);
			} else {
				alreadyNews.add(systemMsg);
			}
		}
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		setTitleRes(R.string.mine_news_text);
		lv_news = (PullToRefreshListView) findViewById(R.id.lv_news);
		ll_no_news = (LinearLayout) findViewById(R.id.ll_no_news);
		lv_news.setXListViewListener(listener);
		lv_news.setOnItemClickListener(lvItemClickListener);
		adapter = new MineNewsAdapter(this, datas);
		lv_news.setAdapter(adapter);
		ll_error = (LinearLayout) findViewById(R.id.ll_error);
		addActionBarItem(R.drawable.icon_unread_selector, UNREAD_MSG);
		findViewById(R.id.btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ll_error.setVisibility(View.GONE);
				lv_news.setVisibility(View.VISIBLE);
				refreshMode = RefreshMode.REFRESH;
				getNetData();
			}
		});
		IntentFilter filter = new IntentFilter(CONNECTIVITY_CHANGE_ACTION);
		registerReceiver(reciver, filter);
	}
	
	private  BroadcastReceiver reciver =  new  BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (SystemUtils.hasNetwork()) {
				if(allNews.size() == 0 && refreshMode == RefreshMode.NONE)
				{
					ll_error.setVisibility(View.GONE);
					lv_news.setVisibility(View.VISIBLE);
					refreshMode = RefreshMode.REFRESH;
					getNetData();
				}
			}
		}
	};
	
	private OnItemClickListener lvItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			int position = (int)arg3;
			if((!adapter.isLoadMore(position))&&adapter.isUnRead(position))
			{
				systemMsgDetail(datas.get(position).getId());
				datas.get(position).setStatus("1");
				adapter.notifyDataSetChanged();
				fillterAllNews();
			}
		}
	};

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		// TODO Auto-generated method stub
		super.onActionBarItemClick(view, itemId);
		
		if(itemId == UNREAD_MSG)
		{
			if(popupWindow == null)
			{
				createPopwindow();
			}
			popupWindow.showAsDropDown(getActionBarItem(UNREAD_MSG), -DensityUtil.dip2px(this,100), -DensityUtil.dip2px(this, 30));
		}
	}
	
	enum RefreshMode{
		REFRESH,LOAD_MORE,NONE;
	}
	
	
	private IXListViewListener listener = new IXListViewListener() {

		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			if(refreshMode != RefreshMode.NONE)
				return;
			refreshMode = RefreshMode.REFRESH;
			curentPage = 1;
			getNetData();
		}

		@Override
		public void onLoadMore() {
			if(refreshMode != RefreshMode.NONE)
				return;
			// TODO Auto-generated method stub
			refreshMode = RefreshMode.LOAD_MORE;
			curentPage++;
			getNetData();
		}
	};

	@Override
	protected void networkError() {
		super.networkError();
		haveNoNet();
	}
	
	private void haveNoNet(){
		
		if(allNews.size() == 0)
		{
			ll_error.setVisibility(View.VISIBLE);
			lv_news.setVisibility(View.GONE);
		}
		if(curentPage > 1)
		{
			curentPage--;
		}else if(curentPage == 1)
		{
			curentPage = 1;
		}
		refreshMode = RefreshMode.NONE;
		lv_news.stopRefresh();
		lv_news.stopLoadMore();
	}
	
	@Override
	protected void noNetwork() {
		// TODO Auto-generated method stub
		super.noNetwork();
		haveNoNet();
	}
	
	private void filterNews(ArrayList<SystemMsg> news){
		
		if(refreshMode == RefreshMode.REFRESH)
		{
			alreadyNews.clear();
			unAlreadyNews.clear();
		}
		
		for (SystemMsg systemMsg : news) {
			if(systemMsg.getStatus().equals("0"))
			{
				unAlreadyNews.add(systemMsg);
			}else{
				alreadyNews.add(systemMsg);
			}
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(reciver);
	}
	
	public void systemMsgDetail(String nid){
		manager.systemMsgDetail(new DataResponse<BaseResponseObject>(){
			@Override
			public void run() {
				super.run();
			}
		}, nid);
	}
	
	private static final int TPP = 10;//每页返回10条，接口默认的
	
	private void getNetData(){
		manager.getPushMessage(new DataResponse<SystemMsgHolder>(){
			@Override
			public void run() {
				super.run();
				handleNetData(value);
			}
		},curentPage);
	}
	
	private synchronized void handleNetData(SystemMsgHolder value) {
		if (value != null && value.getData() != null
				&& value.getData().getList() != null) {
			filterNews(value.getData().getList());
			switch (refreshMode) {
			case REFRESH:
				allNews.clear();
				allNews.addAll(value.getData().getList());
				break;
			case LOAD_MORE:
				allNews.addAll(value.getData().getList());
				break;
			default:
				break;
			}
			datas.clear();
			switch (currentNewsType) {
			case All_News:
				datas.addAll(allNews);
				break;
			case Already_News:
				datas.addAll(alreadyNews);
				break;
			case Not_Read_News:
				datas.addAll(unAlreadyNews);
				break;
			default:
				break;
			}
			if (value.getData().getList().size() < TPP) {
				lv_news.setPullLoadEnable(false);
			} else {
				lv_news.setPullLoadEnable(true);
			}
			adapter.notifyDataSetChanged();
			checkHasNews();
		}
		refreshMode = RefreshMode.NONE;
		lv_news.stopRefresh();
		lv_news.stopLoadMore();
	}
	
	
	private void checkHasNews() {
		if (datas.size() == 0) {
			ll_no_news.setVisibility(View.VISIBLE);
		} else {
			ll_no_news.setVisibility(View.GONE);
		}
	}
	
}
