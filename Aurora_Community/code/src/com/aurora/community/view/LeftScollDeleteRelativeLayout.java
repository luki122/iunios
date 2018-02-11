package com.aurora.community.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aurora.community.R;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.DensityUtil;
import com.tencent.mm.sdk.platformtools.Log;

public class LeftScollDeleteRelativeLayout extends RelativeLayout {

	private final int[] rubbishIcons = {R.drawable.aurora_rubbish_anim_000,R.drawable.aurora_rubbish_anim_001,
			                               R.drawable.aurora_rubbish_anim_002,R.drawable.aurora_rubbish_anim_003,
			                               R.drawable.aurora_rubbish_anim_004,R.drawable.aurora_rubbish_anim_005,
			                               R.drawable.aurora_rubbish_anim_006,R.drawable.aurora_rubbish_anim_007,
			                               R.drawable.aurora_rubbish_anim_008,R.drawable.aurora_rubbish_anim_009,
			                               R.drawable.aurora_rubbish_anim_010,R.drawable.aurora_rubbish_anim_011,
			                               R.drawable.aurora_rubbish_anim_012,R.drawable.aurora_rubbish_anim_013,
			                               R.drawable.aurora_rubbish_anim_014,R.drawable.aurora_rubbish_anim_015};
	
	private int RUBBISH_WIDTH;
	private ImageView iv_rubbish;
	private View childView;
	private RelativeLayout rl_frontView_container;
	
	private GestureDetector gestureDetector;
	
	private int position;
	
	private ILeftScrollListener leftScrollListener;
	
	private static final int START_RUBBISH_STATE =0, SHOW_RUBBISH_STATE = 1,DELETE_RUBBISH_STATE = 2,HIDE_RUBBISH_STATE = 3;
	
	
	public LeftScollDeleteRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setupViews();
	}

	public LeftScollDeleteRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		setupViews();
	}

	public LeftScollDeleteRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setupViews();
	}

	private LinearLayout ll_rubbish;
	private float averageX;
	private static final int FRONT_VIEW_ID = 0x4515;
	private void setupViews(){
		RUBBISH_WIDTH = DensityUtil.dip2px(getContext(), 72);
		averageX = RUBBISH_WIDTH/(float)(rubbishIcons.length-1);
		gestureDetector = new GestureDetector(gestureListener);
		RelativeLayout.LayoutParams lp = new LayoutParams(RUBBISH_WIDTH, RelativeLayout.LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		ll_rubbish = new LinearLayout(getContext());
		ll_rubbish.setGravity(Gravity.CENTER);
		ll_rubbish.setBackgroundColor(Color.parseColor("#ffff4444"));
		rl_frontView_container = new RelativeLayout(getContext());
		rl_frontView_container.setId(FRONT_VIEW_ID);
		iv_rubbish = new ImageView(getContext());
		ll_rubbish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(totalDistanceX > 0)
				{
				  setRubbishState(DELETE_RUBBISH_STATE);
			      hide();
				}
			}
		});
		iv_rubbish.setImageResource(rubbishIcons[0]);
		ll_rubbish.addView(iv_rubbish, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		lp.addRule(ALIGN_TOP, FRONT_VIEW_ID);
		lp.addRule(ALIGN_BOTTOM, FRONT_VIEW_ID);
		addView(ll_rubbish, lp);
		addView(rl_frontView_container,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
	}
	
	private float totalDistanceX = 0;
	private boolean isShow = false;
	
	public boolean isRubbishShow(){
		return totalDistanceX != 0 || isShow;
	}
	
	public void hide(){
		if(totalDistanceX > 0)
		{
			childView.layout(0, 0, getWidth(), getHeight());
			totalDistanceX = 0;
			setRubbishState(HIDE_RUBBISH_STATE);
		}
	}
	private void setRubbishIcon(){
		
		int index = (int) (totalDistanceX / averageX);
		iv_rubbish.setImageResource(rubbishIcons[index]);
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private OnGestureListener gestureListener = new OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			if(Math.abs(distanceX) < Math.abs(distanceY))
			{
				return false;
			}
			totalDistanceX +=distanceX;
			if(distanceX > 0)
			{
				if(totalDistanceX > RUBBISH_WIDTH)
				{
					totalDistanceX = RUBBISH_WIDTH;
				}
			}else{
				if(totalDistanceX < 0)
				{
					totalDistanceX = 0;
				}
			}
			isShow = true;
			childView.layout((int)-totalDistanceX, 0, (int)(getWidth()-totalDistanceX), getHeight());
			setRubbishIcon();
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	private void handleActionUp(){
		if(totalDistanceX >= RUBBISH_WIDTH/2)
		{
			childView.layout((int)-RUBBISH_WIDTH, 0, (int)(getWidth()-RUBBISH_WIDTH), getHeight());
			totalDistanceX = RUBBISH_WIDTH;
			setRubbishState(SHOW_RUBBISH_STATE);
		}else{
			childView.layout(0, 0, getWidth(), getHeight());
			totalDistanceX = 0;
		}
		setRubbishIcon();
	}
	
	private final String TAG = "jadon1";
	
	public void addFrontView(View child){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rl_frontView_container.addView(child, lp);
		childView = child;
	}
	
	public void setPosition(int position){
		this.position = position;
	}
	
	public void setILeftScrollListener(ILeftScrollListener leftScrollListener){
		this.leftScrollListener = leftScrollListener;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			isShow = false;
			setRubbishState(START_RUBBISH_STATE);
			break;
		case MotionEvent.ACTION_UP:
			handleActionUp();
			break;
		}
		return super.onTouchEvent(event);
	}
	
	private void setRubbishState(int state){
		
		if(leftScrollListener != null)
		{
			switch (state) {
			case START_RUBBISH_STATE:
				leftScrollListener.startRubbish(position);
				break;
			case SHOW_RUBBISH_STATE:
				leftScrollListener.showRubbish(position);
				break;
			case DELETE_RUBBISH_STATE:
				leftScrollListener.deleteRubbish(position);
				break;
			case HIDE_RUBBISH_STATE:
				leftScrollListener.hideRubbish(position);
				break;
			default:
				break;
			}
			
			
		}
	}
	
	public static interface ILeftScrollListener{
		void deleteRubbish(int position);
		void showRubbish(int position);
		void hideRubbish(int position);
		void startRubbish(int position);
	}
	
}
