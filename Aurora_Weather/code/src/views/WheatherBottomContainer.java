package views;

import java.util.ArrayList;
import java.util.List;

import views.TemperatureView.Data;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aurora.utils.DensityUtil;
import com.aurora.weatherforecast.R;

@SuppressLint("NewApi")
public class WheatherBottomContainer extends LinearLayout {
	private Context context;
    private int[] showImgaeId={R.drawable.sun,R.drawable.sunup,R.drawable.sunrise,R.drawable.yin,R.drawable.rain,R.drawable.sun};
    private long[] duration={300,300,500,300,300,500};
    private int width;
    private LayoutInflater inflater;
	@SuppressLint("NewApi")
	public WheatherBottomContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initView();
		// TODO Auto-generated constructor stub
	}

	public WheatherBottomContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	public WheatherBottomContainer(Context context) {
		super(context);
		this.context = context;
		initView();
	}

	private List<Data> dataList = new ArrayList<TemperatureView.Data>();

	public void setDateList(List<Data> dataList) {
		this.dataList.clear();
		this.dataList.addAll(dataList);
		if(getChildCount()==0)
		{
		   fillBottomView();
		}
		setShowLocation();
	}

	private void initView() {
		inflater=LayoutInflater.from(context);
	}

	public void startAnimIn() {
		addListener(ANIM_IN);
		animatorInSets[0].start();
	}

	public void startAnimOut() {
		addListener(ANIM_OUT);
		animatorOutSets[0].start();
	}

	private int getAnimatorIndex(ValueAnimator animator, ObjectAnimator[] sets) {
		for (int i = 0; i < sets.length; i++) {
			if (sets[i] == animator) {
				return i;
			}
		}
		return -1;
	}

	private int getMaxChildHeight(){
		if(getChildCount()==0)
			return -1;
		int height=getViewWidthAndHeight(getChildAt(0))[1];
		for(int i=1;i<getChildCount();i++)
		{
			if(height<getViewWidthAndHeight(getChildAt(i))[1])
			{
				height=getViewWidthAndHeight(getChildAt(i))[1];
			}
		}
		return height;
	}
	
	
	private AnimatorListener auroraAnimListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			int index = getAnimatorIndex((ValueAnimator) animation,
					animatorInSets) == -1 ? getAnimatorIndex(
					(ValueAnimator) animation, animatorOutSets)
					: getAnimatorIndex((ValueAnimator) animation,
							animatorInSets);
			getChildAt(index).setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			int index = getAnimatorIndex((ValueAnimator) animation,
					animatorOutSets);
			if (index != -1) {
				getChildAt(index).setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub

		}
	};

	private AnimatorUpdateListener auroraAnimInUpdateListener = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			int index = getAnimatorIndex(animation, animatorInSets);
			if (index != -1) {
				if (index < animatorInSets.length - 1) {
					animation.removeUpdateListener(this);
					animatorInSets[index + 1].start();
				}
			}
		}

	};

	private AnimatorUpdateListener auroraAnimOutUpdateListener = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			int index = getAnimatorIndex(animation, animatorOutSets);
			if (index != -1) {
				if (index < animatorOutSets.length - 1) {
					animation.removeUpdateListener(this);
					animatorOutSets[index + 1].start();
				}
			}
		}

	};

	private ObjectAnimator[] animatorInSets, animatorOutSets;

	private final int ANIM_IN = 1, ANIM_OUT = 2;

	@SuppressLint("NewApi")
	private void addListener(int type) {
		switch (type) {
		case ANIM_IN:
			for (int i = 0; i < animatorInSets.length; i++) {
				animatorInSets[i].addUpdateListener(auroraAnimInUpdateListener);
				animatorInSets[i].addListener(auroraAnimListener);
			}
			break;
		case ANIM_OUT:
			for (int i = 0; i < animatorOutSets.length; i++) {
				animatorOutSets[i]
						.addUpdateListener(auroraAnimOutUpdateListener);
				animatorOutSets[i].addListener(auroraAnimListener);
			}
			break;
		default:
			break;
		}
	}

	
	private void setShowLocation(){
		int childCount=getChildCount();
		for(int i=0;i<childCount;i++)
		{
			View child=getChildAt(i);
			((ImageView)child.findViewById(R.id.weather_bottom_icon)).setImageResource(showImgaeId[i]);
            ((TextView)child.findViewById(R.id.weather_bottom_text)).setText("17:00");
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            int[] wwh=getViewWidthAndHeight(child);
			if (i == 0) {
				lp.leftMargin = (int) (dataList.get(i+1).px
						- dataList.get(i).px - wwh[0]/2);
			} else {
				lp.leftMargin = (int) (dataList.get(i+1).px
						- dataList.get(i).px - wwh[0]);
			}
			lp.gravity=Gravity.CENTER_VERTICAL;
			child.setLayoutParams(lp);
		}
	}
	
	
	
	@SuppressLint("NewApi")
	private void fillBottomView() {
		float y=getPaddingTop();
		float delY=y+DensityUtil.dip2px(context, 20);
		animatorInSets = new ObjectAnimator[dataList.size() - 2];
		animatorOutSets = new ObjectAnimator[dataList.size() - 2];
		for (int i = 1; i < dataList.size() - 1; i++) {
			View child=inflater.inflate(R.layout.weather_bottom_child, null);
			child.setVisibility(View.INVISIBLE);
			PropertyValuesHolder p1In = PropertyValuesHolder.ofFloat("y",delY,
					y);
			PropertyValuesHolder p2In = PropertyValuesHolder.ofFloat("alpha",
					0, 1);
			animatorInSets[i - 1] = ObjectAnimator.ofPropertyValuesHolder(
					child, p1In, p2In).setDuration(duration[i-1]);
			PropertyValuesHolder p1Out = PropertyValuesHolder.ofFloat("y", y,
					delY);
			PropertyValuesHolder p2Out = PropertyValuesHolder.ofFloat("alpha",
					1, 0);
			animatorOutSets[i - 1] = ObjectAnimator.ofPropertyValuesHolder(
					child, p1Out, p2Out).setDuration(duration[i-1]);
			addView(child);
		}
	}
	
	public int[] getViewWidthAndHeight(View view){
		int[] wh=new int[2];
		int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		view.measure(width,height);
		wh[0]=view.getMeasuredWidth();
		wh[1]=view.getMeasuredHeight();
		return wh;
	}
}
