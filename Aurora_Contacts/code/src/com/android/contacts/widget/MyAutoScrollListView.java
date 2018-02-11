package com.android.contacts.widget;

import com.android.contacts.util.DensityUtil;
import com.android.contacts.R;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.AbsListView;

public class MyAutoScrollListView extends AutoScrollListView{

	private static final String TAG = "liyang-MyAutoScrollListView";
	private View header;// 顶部布局文件；
	private int headerHeight;// 顶部布局文件的高度；
	int firstVisibleItem;// 当前第一个可见的item的位置；
	int scrollState;// listview 当前滚动状态；
	boolean isRemark;// 标记，当前是在listview最顶端摁下的；
	int startY;// 摁下时的Y值；

	int state;// 当前的状态；
	final int NONE = 0;// 正常状态；
	final int PULL = 1;// 提示下拉状态；
	final int RELEASE = 2;// 提示释放状态；
	final int REFRESHING = 3;// 刷新状态；
	private Context mContext;
	public MyAutoScrollListView(Context context) {
		super(context);
		initView(context);
		// TODO Auto-generated constructor stub
	}
	
	public MyAutoScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		
	}

	public MyAutoScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}
	
	/**
	 * 通知父布局，占用的宽，高；
	 */
	private void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int height;
		int tempHeight = p.height;
		if (tempHeight > 0) {
			height = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(width, height);
	}

	/**
	 * 初始化界面，添加顶部布局文件到 listview
	 */
	private void initView(Context context) {
		mContext=context;
		LayoutInflater inflater = LayoutInflater.from(context);
		header = inflater.inflate(R.layout.contactlist_header_view, null);
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		Log.i(TAG, "headerHeight = " + headerHeight);

		this.addHeaderView(header);
		maxTopPadding=mContext.getResources().getDimensionPixelOffset(R.dimen.contact_list_hide_topview_height);
		speed=mContext.getResources().getDimensionPixelOffset(R.dimen.contact_list_hide_topview_speed);
		preScroll=mContext.getResources().getDimensionPixelOffset(R.dimen.contact_list_hide_topview_prescroll_height);
		sleepTime=1;
		devide=2;
		currentPadding=-headerHeight;
		topPadding(currentPadding); 
	}

	/**
	 * 设置header布局 上边距；
	 */
	private void topPadding(int topPadding) {
		Log.d(TAG,"topPadding():"+topPadding);
		header.setPadding(header.getPaddingLeft(), topPadding,
				header.getPaddingRight(), header.getPaddingBottom());
		header.invalidate();
	}

	/**
	 * 对屏幕触摸的监控，
	 * 先判断当前是否是在顶端。如果是在最顶端，记录下你开始滑动的Y值
	 * 然后在滑动过程中（监听到的是ACTION_MOVE)，不断地判断当前滑动的范围是否到达应该刷新的程度。
	 * (根据当前的Y-之前的startY的值 与我们的控件的高度之间关系来判断）
	 * 然后在监听到手指松开时，根据当前的状态（我们在onmove（）中计算的），做相应的操作。
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstVisibleItem == 0) {
				isRemark = true;
				startY = (int) ev.getY();
			}
			break;

		case MotionEvent.ACTION_MOVE:
			onMove(ev);
			break;
		case MotionEvent.ACTION_UP:

			if (state == PULL) {
				new HideHeaderTask().execute();

			}
			break;
		}
		return super.onTouchEvent(ev);
	}


	/** 
	 * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。 
	 *  
	 */  
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {  

		@Override  
		protected Integer doInBackground(Void... params) { 
			//			int myMaxTopPadding=maxTopPadding;
			while(currentPadding>-headerHeight+mContext.getResources().getDisplayMetrics().density*30){
				Log.d(TAG, "speed:"+speed+" currentPadding:"+currentPadding);
				currentPadding-=speed;
				Log.d(TAG,"while-currentPadding:"+currentPadding);

				publishProgress(currentPadding); 				
				sleep(sleepTime);
				speed=(int) (speed-20*mContext.getResources().getDisplayMetrics().density>0?speed-20*mContext.getResources().getDisplayMetrics().density:10);
			}
			
			while(currentPadding>-headerHeight){				
				currentPadding=(int) ((currentPadding-10*mContext.getResources().getDisplayMetrics().density>-headerHeight)?
						currentPadding-10*mContext.getResources().getDisplayMetrics().density:-headerHeight);
				Log.d(TAG, "speed1:"+speed+" currentPadding1:"+currentPadding);
				publishProgress(currentPadding); 				
				sleep(sleepTime);
			}

			return currentPadding;
		}  

		@Override  
		protected void onProgressUpdate(Integer... myMaxTopPadding) {  

			topPadding(myMaxTopPadding[0]);

		}  

		@Override  
		protected void onPostExecute(Integer topMargin) {
			topPadding(-headerHeight);
			state=NONE;
			currentPadding=-headerHeight;
			speed=mContext.getResources().getDimensionPixelOffset(R.dimen.contact_list_hide_topview_speed);
		}  
	}  



	private void sleep(int time) {  
		try {  
			Thread.sleep(time);  
		} catch (InterruptedException e) {  
			e.printStackTrace();  
		}  
	}  

	/**
	 * 判断移动过程操作：
	 * 如果不是顶端，不需要做任何的操作
	 * 否则就获取当前的Y值，与开始的Y值做比较。
	 * @param ev
	 */
	int maxTopPadding,speed,sleepTime,devide,preScroll,currentPadding;
	private void onMove(MotionEvent ev) {
		if (!isRemark) {
			return;
		}
		int tempY = (int) ev.getY();
		int space = tempY - startY;		



		switch (state) {
		case NONE:
			currentPadding=-headerHeight;
			if (space > preScroll) {   
				state = PULL;  //正在下拉

			}
			break;
		case PULL:
			//			int topPadding = (space - headerHeight-preScroll)/devide;
			int topPadding=-headerHeight+(space-preScroll)/devide;
			currentPadding=topPadding<=maxTopPadding?topPadding:maxTopPadding;
			Log.d(TAG,"pull topPadding:"+currentPadding);
			topPadding(currentPadding);
			//如果大于一定高度，并且滚动状态是正在滚动时，就到了松开可以刷新的状态
			break;
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		this.firstVisibleItem = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		this.scrollState = scrollState;
	}

}
