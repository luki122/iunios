package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;
import com.android.contacts.R;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.list.AuroraDefaultContactBrowseListFragment;


import android.app.LocalActivityManager;

public class MainFragment extends Fragment{

	private static final String TAG = "liyang-MainFragment";
	private Context context;
	private ArrayList<Fragment> fragmentList;  
	private AuroraCallLogFragmentV2 mCallLogFragment;
	private AuroraDefaultContactBrowseListFragment mContactBrowseListFragment;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		context=(Context)activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	private View fragmentView = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if(fragmentView==null) fragmentView = inflater.inflate(R.layout.main_layout, container, false);
		initAuroraTabWidget(savedInstanceState,fragmentView);

		return fragmentView;
	}

	public static void setPagerCanScroll(boolean flag){
		if(pager!=null) pager.setCanScroll(flag);
	}

//	private LocalActivityManager manager = null;
	public static AuroraViewPager pager = null;
	public int currIndex = 0;// 当前页卡编号
	private AuroraTabWidget auroraTabWidget;
	public View scrollIconView;

	private void initAuroraTabWidget(Bundle savedInstanceState, View fragmentView) {

		auroraTabWidget=(AuroraTabWidget)fragmentView.findViewById(R.id.auroratabwidget);
		auroraTabWidget.setOnPageChangeListener(new MyOnPageChangeListener());

		pager = auroraTabWidget.getViewPager();
		pager.setId(R.id.tab_view_pager);


		//		manager = new LocalActivityManager((Activity)context , true);
		//		manager.dispatchCreate(savedInstanceState);
		//
		//		final ArrayList<View> list = new ArrayList<View>();
		//		Intent intent = new Intent(context, AuroraCallLogActivity.class);
		//		list.add(getView("A", intent));
		//		Intent intent2 = new Intent(context, AuroraPeopleActivity.class);
		//		list.add(getView("B", intent2));


		scrollIconView=auroraTabWidget.getmScrollIconLinearLayout();
		setPagerCanScroll(true);


		if(fragmentList==null) fragmentList = new ArrayList<Fragment>();         

		if(mCallLogFragment==null){
			mCallLogFragment = new AuroraCallLogFragmentV2();
			Log.d(TAG, "mCallLogFragment:"+mCallLogFragment);
		}
		if(mContactBrowseListFragment==null){
			mContactBrowseListFragment =new AuroraDefaultContactBrowseListFragment();
			Log.d(TAG, "mContactBrowseListFragment:"+mContactBrowseListFragment);
		}
		fragmentList.add(mCallLogFragment);  
		fragmentList.add(mContactBrowseListFragment);  
		

		//给ViewPager设置适配器  
		pager.setAdapter(new MyFragmentPagerAdapter(getFragmentManager(),fragmentList));  
		if(AuroraDialActivity.switch_to_contacts_page){
			pager.setCurrentItem(1);
		}
//		pager.setOnPageChangeListener(new MyOnPageChangeListener());//页面变化时的监听器 
	}

	/**
	 * 页卡切换监听
	 */

	class MyOnPageChangeListener implements AuroraViewPager.OnPageChangeListener {

		//		int one = bmpW;// 页卡1 -> 页卡2 偏移量


		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:		
				if(currIndex==1){

					if(AuroraDialActivity.callLogHandler!=null){
						Message msg=AuroraDialActivity.callLogHandler.obtainMessage();
						msg.what=AuroraDialActivity.SWITCH_TO_PAGE0;
						AuroraDialActivity.callLogHandler.sendMessageDelayed(msg, 450);
					}

				}
				break;
			case 1:
				if(currIndex==0){

					//					mHandler.postDelayed(new Runnable() {
					//						public void run() {   
					//							Log.d(TAG, "onreceive111");
					//							Intent intent=new Intent(SWITCH_TO_PAGE1);
					//							sendBroadcast(intent);
					//						}
					//					}, 450); 

					if(AuroraDialActivity.contactsHandler!=null){
						Message msg=AuroraDialActivity.contactsHandler.obtainMessage();
						msg.what=AuroraDialActivity.SWITCH_TO_PAGE1;
						AuroraDialActivity.contactsHandler.sendMessageDelayed(msg, 450);
					}

				}
				break;

			}

			currIndex=arg0;

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}




//	/**
//	 * 通过activity获取视图
//	 * @param id
//	 * @param intent
//	 * @return
//	 */
//	private View getView(String id, Intent intent) {
//		return manager.startActivity(id, intent).getDecorView();
//	}

	/**
	 * Pager适配器
	 */
	class MyFragmentPagerAdapter extends aurora.view.FragmentPagerAdapter{  
		private ArrayList<Fragment> list;  
	    public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<Fragment> list) {
			super(fm);		
			this.list=list;
		}
	    
	    @Override
		public int getCount() {
	    	Log.d(TAG, "list size:"+list.size());
			return list.size();
		}
		
		@Override
		public Fragment getItem(int arg0) {
			Log.d(TAG, "arg0:"+arg0+" list.get(arg0):"+list.get(arg0));
			return list.get(arg0);
		}
	}  

}
