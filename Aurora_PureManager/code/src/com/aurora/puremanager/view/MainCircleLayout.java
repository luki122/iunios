package com.aurora.puremanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.animation.MainCircleAnimation;

/**
 * 主界面中的圆圈view
 * 
 * @author chengrq
 */
public class MainCircleLayout extends FrameLayout {
	private MainCircleView mainCircleView;
	private TextView appsNumOfK;
	private TextView appsNumOfH;
	private TextView appsNumOfT;
	private RiseNumberTextView appsNumOfA;

	private int curAppsNum = -1;
	private String curAppsNumOfKStr = null;
	private String curAppsNumOfHStr = null;
	private String curAppsNumOfTStr = null;
	private String curAppsNumOfAStr = null;

	private int userAppNum;
	private int sysAppNum;

	public MainCircleLayout(final Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void startAnimation(int userAppNum, int sysAppNum) {
		this.userAppNum = userAppNum;
		this.sysAppNum = sysAppNum;
		getMainCircleView().init(userAppNum, sysAppNum);

		Animation anim = new MainCircleAnimation(new AnimationCallBack() {
			public void callBack(float interpolatedTime, Transformation t) {
				getMainCircleView().updateAnimView(interpolatedTime);
				updateViewOfAppsNum(interpolatedTime);

			}
		});
		anim.setDuration(getAnimationDuration());
		startAnimation(anim);
	}

	private void updateViewOfAppsNum(float interpolatedTime) {
		int tmpAppsNum = (int) ((userAppNum + sysAppNum) * interpolatedTime);
		if (tmpAppsNum == curAppsNum) {
			return;
		}
		curAppsNum = tmpAppsNum;
		String tmpAppsNumOfKStr = "" + curAppsNum / 1000;
		String tmpAppsNumOfHStr = "" + curAppsNum % 1000 / 100;
		String tmpAppsNumOfTStr = "" + curAppsNum % 100 / 10;
		String tmpAppsNumOfAStr = "" + curAppsNum % 10;
		boolean kShow = false, hShow = false, tShow = false;
		if (curAppsNum / 1000 > 0) {
			kShow = hShow = tShow = true;
		} else if (curAppsNum % 1000 / 100 > 0) {
			hShow = tShow = true;
		} else if (curAppsNum % 100 / 10 > 0) {
			tShow = true;
		}

		/*if (kShow) {
			if (!tmpAppsNumOfKStr.equals(curAppsNumOfKStr)) {
				curAppsNumOfKStr = tmpAppsNumOfKStr;
				getAppsNumOfK().setText(curAppsNumOfKStr);
			}
			getAppsNumOfK().setVisibility(View.VISIBLE);
		} else {
			getAppsNumOfK().setVisibility(View.GONE);
		}

		if (hShow) {
			if (!tmpAppsNumOfHStr.equals(curAppsNumOfHStr)) {
				curAppsNumOfHStr = tmpAppsNumOfHStr;
				getAppsNumOfH().setText(curAppsNumOfHStr);
			}
			getAppsNumOfH().setVisibility(View.VISIBLE);
		} else {
			getAppsNumOfH().setVisibility(View.GONE);
		}

		if (tShow) {
			if (!tmpAppsNumOfTStr.equals(curAppsNumOfTStr)) {
				curAppsNumOfTStr = tmpAppsNumOfTStr;
				getAppsNumOfT().setText(curAppsNumOfTStr);
			}
			getAppsNumOfT().setVisibility(View.VISIBLE);
		} else {
			getAppsNumOfT().setVisibility(View.GONE);
		}*/

		if (!tmpAppsNumOfAStr.equals(curAppsNumOfAStr)) {
			curAppsNumOfAStr = tmpAppsNumOfAStr;
			getAppsNumOfA().setVisibility(View.VISIBLE);
//			getAppsNumOfA().setText(curAppsNumOfAStr);
			getAppsNumOfA().withNumber(Integer.valueOf(curAppsNumOfAStr).intValue());
		}
	}

	public void updateViewWhenAppNumChange(int userAppNum, int sysAppNum) {
		this.userAppNum = userAppNum;
		this.sysAppNum = sysAppNum;

		getMainCircleView().updateViewWhenAppNumChange(userAppNum, sysAppNum);
		updateViewOfAppsNum(1);
	}

	/**
	 * 得到动画执行的时间
	 * 
	 * @return
	 */
	public int getAnimationDuration() {
		return 1000;
	}

	private MainCircleView getMainCircleView() {
		if (mainCircleView == null) {
//			mainCircleView = (MainCircleView) findViewById(R.id.mainCircleView);
		}
		return mainCircleView;
	}

	/*private TextView getAppsNumOfK() {
		if (appsNumOfK == null) {
			appsNumOfK = (TextView) findViewById(R.id.appsNumOfK);
		}
		return appsNumOfK;
	}

	private TextView getAppsNumOfH() {
		if (appsNumOfH == null) {
			appsNumOfH = (TextView) findViewById(R.id.appsNumOfH);
		}
		return appsNumOfH;
	}

	private TextView getAppsNumOfT() {
		if (appsNumOfT == null) {
			appsNumOfT = (TextView) findViewById(R.id.appsNumOfT);
		}
		return appsNumOfT;
	}*/

	private RiseNumberTextView getAppsNumOfA() {
		if (appsNumOfA == null) {
			appsNumOfA = (RiseNumberTextView) findViewById(R.id.appsNumOfA);
		}
		return appsNumOfA;
	}

	public interface AnimationCallBack {
		public void callBack(float interpolatedTime, Transformation t);
	}
}
