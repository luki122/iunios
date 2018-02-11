package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;
import com.android.contacts.R;
import com.android.contacts.dialpad.AnimUtils;

import android.app.LocalActivityManager;

public class CopyOfMainFragment extends Fragment{

	private static final String TAG = "liyang-CopyOfMainFragment";
	private Context context;
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated");
		initAuroraTabWidget(savedInstanceState,fragmentView);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		// TODO Auto-generated method stub
		fragmentView = inflater.inflate(R.layout.main_layout, container, false);	
		return fragmentView;
	}

	public static void setPagerCanScroll(boolean flag){
		if(pager!=null) pager.setCanScroll(flag);
	}

	private LocalActivityManager manager = null;
	public static AuroraViewPager pager = null;
	public int currIndex = 0;// 当前页卡编号
	private AuroraTabWidget auroraTabWidget;
	public View scrollIconView,callLogView,peopleView;

	private void initAuroraTabWidget(Bundle savedInstanceState, View fragmentView) {

		auroraTabWidget=(AuroraTabWidget)fragmentView.findViewById(R.id.auroratabwidget);
		auroraTabWidget.setOnPageChangeListener(new MyOnPageChangeListener());

		pager = auroraTabWidget.getViewPager();

		manager = new LocalActivityManager((Activity)context , true);
		manager.dispatchCreate(savedInstanceState);


		final ArrayList<View> list = new ArrayList<View>();
		Intent intent = new Intent(context, AuroraCallLogActivity.class);
		if(callLogView==null){
			callLogView=getView("A", intent);
		}		
		list.add(callLogView);

		Intent intent2 = new Intent(context, AuroraPeopleActivity.class);
		if(peopleView==null){
			peopleView=getView("B", intent2);
		}
		list.add(peopleView);

		pager.setAdapter(new MyPagerAdapter(list));		

		scrollIconView=	auroraTabWidget.getmScrollIconLinearLayout();
		
//		scrollIconView.setFocusable(true);
//		scrollIconView.setFocusableInTouchMode(true);
//		scrollIconView.requestFocus();
//		scrollIconView.requestFocusFromTouch();

		setPagerCanScroll(true);

		if(AuroraDialActivityV3.switch_to_contacts_page){
			pager.setCurrentItem(1);
		}
	}



	/**
	 * 页卡切换监听
	 */

	public class MyOnPageChangeListener implements AuroraViewPager.OnPageChangeListener {

		//		int one = bmpW;// 页卡1 -> 页卡2 偏移量


		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:		
				if(currIndex==1){

					if(AuroraDialActivityV3.callLogHandler!=null){
						Message msg=AuroraDialActivityV3.callLogHandler.obtainMessage();
						msg.what=AuroraDialActivityV3.SWITCH_TO_PAGE0;
						AuroraDialActivityV3.callLogHandler.sendMessageDelayed(msg, 450);
					}

				}
				break;
			case 1:
				if(currIndex==0){

					if(AuroraDialActivityV3.contactsHandler!=null){
						Message msg=AuroraDialActivityV3.contactsHandler.obtainMessage();
						msg.what=AuroraDialActivityV3.SWITCH_TO_PAGE1;
						AuroraDialActivityV3.contactsHandler.sendMessageDelayed(msg, 450);
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




	/**
	 * 通过activity获取视图
	 * @param id
	 * @param intent
	 * @return
	 */
	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

	/**
	 * Pager适配器
	 */
	public class MyPagerAdapter extends aurora.view.PagerAdapter{
		List<View> list =  new ArrayList<View>();
		public MyPagerAdapter(ArrayList<View> list) {
			this.list = list;
		}

		@Override
		public void destroyItem(ViewGroup container, int position,
				Object object) {
			AuroraViewPager pViewPager = ((AuroraViewPager) container);
			pViewPager.removeView(list.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			AuroraViewPager pViewPager = ((AuroraViewPager) arg0);
			pViewPager.addView(list.get(arg1));
			return list.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
}
