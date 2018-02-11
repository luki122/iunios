package com.aurora.note.ui;

import com.aurora.note.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
// import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class PLA_PullListview extends MultiColumnListView implements PLA_AbsListView.OnScrollListener {

	private final static String TAG = "PullToRefreshListView";

	// 下拉刷新标志
	private final static int PULL_To_REFRESH = 0;
	// 松开刷新标志
	private final static int RELEASE_To_REFRESH = 1;
	// 正在刷新标志
	private final static int REFRESHING = 2;
	// 刷新完成标志
	private final static int DONE = 3;

	private LayoutInflater inflater;

	private LinearLayout headView;
	// private TextView tipsTextview;
	// private TextView lastUpdatedTextView;
	// private ImageView arrowImageView;
	// private ProgressBar progressBar;
	// 用来设置箭头图标动画效果
	// private RotateAnimation animation;
	// private RotateAnimation reverseAnimation;

	private Scroller mScroller;

	private boolean headViewIsShow = false;

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;

	private int headContentWidth;
	private int headContentHeight;
	private int headContentOriginalTopPadding;
	private int headContentTopPadding;

	private int startX;
	private int startY;
	private int firstItemIndex;
	private int currentScrollState;

	private int state;

	private boolean isBack;
	private boolean isFirstPull = true;
	public OnRefreshListener refreshListener;

	public PLA_PullListview(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PLA_PullListview(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// 设置滑动效果
		/*animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(100);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(100);
		reverseAnimation.setFillAfter(true);*/

		mScroller = new Scroller(context, new DecelerateInterpolator());

		inflater = LayoutInflater.from(context);
		headView = (LinearLayout) inflater.inflate(R.layout.note_main_search_view, null);

		/*
		 * arrowImageView = (ImageView)
		 * headView.findViewById(R.id.head_arrowImageView);
		 * arrowImageView.setMinimumWidth(50);
		 * arrowImageView.setMinimumHeight(50); progressBar = (ProgressBar)
		 * headView.findViewById(R.id.head_progressBar); tipsTextview =
		 * (TextView) headView.findViewById(R.id.head_tipsTextView);
		 * lastUpdatedTextView = (TextView)
		 * headView.findViewById(R.id.head_lastUpdatedTextView);
		 */

		headContentOriginalTopPadding = headView.getPaddingTop();

		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();
		Log.i(TAG, "刷新init-TopPad：" + headContentOriginalTopPadding);
		headView.setPadding(headView.getPaddingLeft(), -1 * headContentHeight,
				headView.getPaddingRight(), headView.getPaddingBottom());
		headView.invalidate();

		addHeaderView(headView);
		setOnScrollListener(this);
	}

	public void setCanMoveHeadView(boolean canMoveHeadView) {
		isFirstPull = canMoveHeadView;
	}

	public void showHeadView() {
		if (headView != null) {
			headView.setPadding(headView.getPaddingLeft(), headContentOriginalTopPadding,
					headView.getPaddingRight(), headView.getPaddingBottom());
			headView.invalidate();
			headViewIsShow = true;
		}
	}

	public void hideHeadView() {
		if (headView != null) {
			headView.setPadding(headView.getPaddingLeft(), -1 * headContentHeight,
					headView.getPaddingRight(), headView.getPaddingBottom());
			headView.invalidate();
			headViewIsShow = false;
		}
	}

	private void handleVerticalScroll(int scrollY) {
		if (state == REFRESHING || !isRecored) {
			return;
		}

		int topPadding = headContentTopPadding + scrollY;
		if (topPadding >= headContentOriginalTopPadding) {
			topPadding = headContentOriginalTopPadding;
			headViewIsShow = true;
			isRecored = false;
		} else if (topPadding <= -1 * headContentHeight) {
			topPadding = -1 * headContentHeight;
			headViewIsShow = false;
			isRecored = false;
		}

		headView.setPadding(
				headView.getPaddingLeft(), 
				topPadding, 
				headView.getPaddingRight(), 
				headView.getPaddingBottom());
		headView.invalidate();
	}

	public void rHeadView() {
		if (headView != null) removeHeaderView(headView);
	}

	public void aHeadView() {
		if (headView != null) addHeaderView(headView);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			headView.setPadding(
					headView.getPaddingLeft(), 
					mScroller.getCurrY(), 
					headView.getPaddingRight(), 
					headView.getPaddingBottom());
			// headView.invalidate();
			postInvalidate();
		}
		super.computeScroll();
	}

	public void onScroll(PLA_AbsListView view, int firstVisiableItem,
			int visibleItemCount, int totalItemCount) {
		firstItemIndex = firstVisiableItem;
		// Log.i("pull", "the firstItemIndex=" + firstItemIndex);
	}

	public void onScrollStateChanged(PLA_AbsListView view, int scrollState) {
		currentScrollState = scrollState;
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstItemIndex == 0 && !isRecored) {
				startX = (int) event.getX();
				startY = (int) event.getY();
				headContentTopPadding = headView.getPaddingTop();
				isRecored = true;
				// Log.i(TAG, "当前-按下高度-ACTION_DOWN-Y："+startY);

			}
			break;

		case MotionEvent.ACTION_CANCEL:// 失去焦点&取消动作
		case MotionEvent.ACTION_UP:
			/*if (state != REFRESHING) {
				if (state == DONE) {
					// Log.i(TAG,"当前-抬起-ACTION_UP：DONE什么都不做");
				} else if (state == PULL_To_REFRESH) {
					state = DONE;
					changeHeaderViewByState();
					// Log.i(TAG,
					// "当前-抬起-ACTION_UP：PULL_To_REFRESH-->DONE-由下拉刷新状态到刷新完成状态");
				} else if (state == RELEASE_To_REFRESH) {

					state = REFRESHING;
					changeHeaderViewByState();
					onRefresh();
					// Log.i(TAG,
					// "当前-抬起-ACTION_UP：RELEASE_To_REFRESH-->REFRESHING-由松开刷新状态，到刷新完成状态");
				}
			}*/

			if (isRecored) {
				int headviewTopPadding = headView.getPaddingTop();
				if (headviewTopPadding != headContentOriginalTopPadding && 
						headviewTopPadding != headContentHeight * -1) {

					int upLine = - headContentHeight / 4;
					int downLine = -2 * headContentHeight / 3;

					mScroller.forceFinished(true);
					smoothScrollToPosition(0);
					if (headviewTopPadding < headContentTopPadding) {
						//mScroller.startScroll(0, headviewTopPadding, 0, 
						//		- headContentHeight - headviewTopPadding, 300);
						if (headviewTopPadding >= upLine) {
							mScroller.startScroll(0, headviewTopPadding, 0, 
									headContentOriginalTopPadding - headviewTopPadding, 300);
						} else {
							mScroller.startScroll(0, headviewTopPadding, 0, 
									- headContentHeight - headviewTopPadding, 300);
						}
					} else if (headviewTopPadding > headContentTopPadding) {
						//mScroller.startScroll(0, headviewTopPadding, 0, 
						//		headContentOriginalTopPadding - headviewTopPadding, 300);
						if (headviewTopPadding <= downLine) {
							mScroller.startScroll(0, headviewTopPadding, 0, 
									- headContentHeight - headviewTopPadding, 300);
						} else {
							mScroller.startScroll(0, headviewTopPadding, 0, 
									headContentOriginalTopPadding - headviewTopPadding, 300);
						}
					}
				}
			}

			state = DONE;
			isRecored = false;
			isBack = false;

			break;

		case MotionEvent.ACTION_MOVE:
			int tempX = (int) event.getX();
			int tempY = (int) event.getY();
			if(!isFirstPull)
			{
				break;
			}
			// Log.i(TAG, "当前-滑动-ACTION_MOVE Y："+tempY);
			if (!isRecored && firstItemIndex == 0) {
				// Log.i(TAG, "当前-滑动-记录拖拽时的位置 Y："+tempY);
				isRecored = true;
				startY = tempY;
				startX = tempX;
				headContentTopPadding = headView.getPaddingTop();
			}

			if (Math.abs(tempX - startX) * 1.7 >= Math.abs(tempY - startY)) break;

			handleVerticalScroll(tempY - startY);

			/*if (state != REFRESHING && isRecored) {
				// 可以松开刷新了
				if (state == RELEASE_To_REFRESH) {
					// 往上推，推到屏幕足够掩盖head的程度，但还没有全部掩盖

					if ((tempY - startY < headContentHeight + 20)
							&& (tempY - startY) > 0) {
						state = PULL_To_REFRESH;
						changeHeaderViewByState();
						// Log.i(TAG,
						// "当前-滑动-ACTION_MOVE：RELEASE_To_REFRESH--》PULL_To_REFRESH-由松开刷新状态转变到下拉刷新状态");
					}
					// 一下子推到顶
					else if (tempY - startY <= 0) {
						state = DONE;
						changeHeaderViewByState();
						// Log.i(TAG,
						// "当前-滑动-ACTION_MOVE：RELEASE_To_REFRESH--》DONE-由松开刷新状态转变到done状态");
					}
					// 往下拉，或者还没有上推到屏幕顶部掩盖head
					else {
						// 不用进行特别的操作，只用更新paddingTop的值就行了
					}
				}
				// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
				else if (state == PULL_To_REFRESH) {
					// 下拉到可以进入RELEASE_TO_REFRESH的状态
					if (tempY - startY >= headContentHeight + 20) {
						state = RELEASE_To_REFRESH;
						isBack = true;
						changeHeaderViewByState();
						// Log.i(TAG,
						// "当前-滑动-PULL_To_REFRESH--》RELEASE_To_REFRESH-由done或者下拉刷新状态转变到松开刷新");
					}
					// 上推到顶了
					else if (tempY - startY <= 0) {
						state = DONE;
						changeHeaderViewByState();
						// Log.i(TAG,
						// "当前-滑动-PULL_To_REFRESH--》DONE-由Done或者下拉刷新状态转变到
						    headContentOriginalTopPaddingdone状态");
					}
				}
				// done状态下
				else if (state == DONE) {
					if (tempY - startY > 0) {
						state = PULL_To_REFRESH;
						changeHeaderViewByState();
						// Log.i(TAG,
						// "当前-滑动-DONE--》PULL_To_REFRESH-由done状态转变到下拉刷新状态");
					}
				}

				if (tempY - startY > 185) {
					tempY = startY + 185;
				}
				
				// 更新headView的size
				if (state == PULL_To_REFRESH) {
					int topPadding = (int) ((-1 * headContentHeight + (tempY - startY)));
					Log.i(TAG, "刷新PULL_To_REFRESH-TopPad："
							+ headContentOriginalTopPadding);
					Log.i(TAG, "刷新PULL_To_REFRESH-topPadding："
							+ topPadding);
					headView.setPadding(headView.getPaddingLeft(),
					 topPadding, headView.getPaddingRight(),
					headView.getPaddingBottom());
					headView.invalidate();
					// Log.i(TAG, "当前-下拉刷新PULL_To_REFRESH-TopPad："+topPadding);
				}

				// 更新headView的paddingTop
				if (state == RELEASE_To_REFRESH) {
					int topPadding = (int) ((tempY - startY - headContentHeight));
					Log.i(TAG, "刷新RELEASE_To_REFRESH-TopPad："
							+ headContentOriginalTopPadding);
					headView.setPadding(headView.getPaddingLeft(), topPadding,
							headView.getPaddingRight(),
							headView.getPaddingBottom());
					headView.invalidate();
					// Log.i(TAG,
					// "当前-释放刷新RELEASE_To_REFRESH-TopPad："+topPadding);
				}
			}*/
			break;
		}
		return super.onTouchEvent(event);
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:

			/*
			 * arrowImageView.setVisibility(View.VISIBLE);
			 * progressBar.setVisibility(View.GONE);
			 * tipsTextview.setVisibility(View.VISIBLE);
			 * lastUpdatedTextView.setVisibility(View.VISIBLE);
			 * 
			 * arrowImageView.clearAnimation();
			 * arrowImageView.startAnimation(animation);
			 * 
			 * tipsTextview.setText(R.string.pull_to_refresh_release_label);
			 */

			break;
		case PULL_To_REFRESH:

			/*
			 * progressBar.setVisibility(View.GONE);
			 * tipsTextview.setVisibility(View.VISIBLE);
			 * lastUpdatedTextView.setVisibility(View.VISIBLE);
			 * arrowImageView.clearAnimation();
			 * arrowImageView.setVisibility(View.VISIBLE); if (isBack) { isBack
			 * = false; arrowImageView.clearAnimation();
			 * arrowImageView.startAnimation(reverseAnimation); }
			 * tipsTextview.setText(R.string.pull_to_refresh_pull_label);
			 */

			break;

		case REFRESHING:
			Log.i(TAG, "刷新REFRESHING-TopPad：" + headContentOriginalTopPadding);
			if(isFirstPull)
			{
				headView.setPadding(headView.getPaddingLeft(),
						headContentOriginalTopPadding, headView.getPaddingRight(),
						headView.getPaddingBottom());
				headView.invalidate();
			}

			/*
			 * progressBar.setVisibility(View.VISIBLE);
			 * arrowImageView.clearAnimation();
			 * arrowImageView.setVisibility(View.GONE);
			 * tipsTextview.setText(R.string.loading);
			 * lastUpdatedTextView.setVisibility(View.GONE);
			 */

			break;
		case DONE:
			// Log.i("joey","完成DONE-TopPad："+(-1 * headContentHeight));
			// headView.setPadding(headView.getPaddingLeft(), -1 *
			// headContentHeight, headView.getPaddingRight(),
			// headView.getPaddingBottom());
			// headView.invalidate();

			/*
			 * progressBar.setVisibility(View.GONE);
			 * arrowImageView.clearAnimation(); // 此处更换图标
			 * arrowImageView.setImageResource
			 * (R.drawable.pull_to_refresh_arrow);
			 * 
			 * tipsTextview.setText(R.string.pull_to_refresh_pull_label);
			 * lastUpdatedTextView.setVisibility(View.VISIBLE);
			 */

			// Log.i("joey", "当前状态，done");
			break;
		}
	}

	public View getHeadView() {
		return headView;
	}

	// 点击刷新
	public void clickRefresh() {
		setSelection(0);
		state = REFRESHING;
		changeHeaderViewByState();
		onRefresh();
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete(String update) {
		// lastUpdatedTextView.setText(update);
		onRefreshComplete();
	}

	public void onRefreshComplete() {
		state = DONE;
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			// isFirstPull = false;
			refreshListener.onRefresh();
		}
	}

	// 计算headView的width及height值
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setTipsTextViewColor(int color) {
		// tipsTextview.setTextColor(color);
		// lastUpdatedTextView.setTextColor(color);
	}
}
