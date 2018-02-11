/**
 * Vulcan created this file in 2015年3月16日 下午6:01:45 .
 */
package com.android.phase1.cinema;

import com.android.browser.BaseUi;
import com.android.phase1.cinema.CinemaMan.CinemaListener;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/**
 * Vulcan created WebviewStatusMachine in 2015年3月16日 .
 * 
 */
public class WebviewStatusMachine{
	
	//definition of running status of webview
	public static final int STATUS_NULL = -1;
	public static final int STATUS_INIT = 0;
	public static final int STATUS_BROWSE = 1;
	public static final int STATUS_END = 2;
	public static final int STATUS_REPLAY = 3;
	
	//direction of moving of finger
	public static final int DIRECTION_UP = -1;
	public static final int DIRECTION_NULL = 0;
	public static final int DIRECTION_DOWN = 1;
	
	private int mStatus = STATUS_INIT;
	private int mTargetStatus = STATUS_NULL;
	private boolean mEventSwitch = true;
	private final FingerTracker mFingerTracker = new FingerTracker();
	private final CinemaMan mCinemaMan;
	
	public int mSlideTimesFromBottom = 0;
	//private boolean mIsFromBottom = false;
	

	/**
	 * 
	 */
	public WebviewStatusMachine(BaseUi baseUi, View webview, View titlebar, View toolbar) {
		mBaseUi = baseUi;
		mEventSwitch = true;
		mStatus = STATUS_INIT;
		mCinemaMan = new CinemaMan(baseUi.getActivity(), webview, titlebar,toolbar);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 上午10:12:32 .
	 * @return
	 * if this method returns true, touch events will be transfered to webview;
	 * or they will not be transfered.
	 */
	public boolean getWebviewEventSwitch(MotionEvent e) {
		return mEventSwitch;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 上午10:22:22 .
	 * @param b
	 */
	public void setWebviewEventSwitch(boolean b) {
		if(b) {
			mEventSwitch = b;
		}
		else {
			mEventSwitch = true;
		}
		
	}
	
	
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午3:33:27 .
	 * @param e
	 * @param direction
	 * @param distance
	 * @param velocity
	 */
	protected void processEventOnInitStatus(MotionEvent e, int direction, float distance, float velocity) {
		
		if(MotionEvent.ACTION_MOVE == e.getAction()) {
			processMovingEventOnInitStatus(e, direction,distance,velocity);
		}
		else if(MotionEvent.ACTION_UP == e.getAction()) {
			processUpEventOnInitStatus(e);
		}
	}
	

	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 上午10:24:14 .
	 * @param e
	 * @param direction Moving direction of finger
	 * @param distance
	 * @param velocity
	 */
	protected void processEventOnBrowseStatus(MotionEvent e, int direction, float distance, float velocity) {
		if(MotionEvent.ACTION_MOVE == e.getAction()) {
			processMovingEventOnBrowseStatus(e, direction,distance,velocity);
		}
		else if(MotionEvent.ACTION_UP == e.getAction()) {
			processUpEventOnBrowseStatus(e, direction);
		}
	}
	
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午3:36:19 .
	 */
	private void processUpEventOnInitStatus(final MotionEvent e) {
		
		Log.d("vsm",
				String.format("processUpEventOnInitStatus: event = ACTION_UP, mTargetStatus=%d", mTargetStatus));
		
		if(STATUS_INIT == mStatus
			&& STATUS_NULL != mTargetStatus) {
			setWebviewEventSwitch(false);
			startCinema(CinemaMan.CINEMA_TYPE_INIT_BROWSE,
					new CinemaListener() {

						@Override
						public void onCinemaEnd(Cinema cinema) {
							Log.d("vsm2",
									String.format("onCinemaEnd: cinema=%s", cinema));
							setWebviewEventSwitch(true);
							changeStatus(e, mTargetStatus);
							changeTargetStatus(e,STATUS_NULL);
							mSlideTimesFromBottom = 0;
							Log.d("vtwice",
									String.format("mSlideTimesFromBottom: reset"));
						}
				
			});
		}
	}


	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午3:33:34 .
	 * @param direction
	 * @param distance
	 * @param velocity
	 */
	private void processMovingEventOnInitStatus(final MotionEvent e, int direction, float distance, float velocity) {
		
		final int cinemaTp = CinemaMan.CINEMA_TYPE_INIT_BROWSE;
		final float progress = distance/FingerTracker.DISTANCE_FAREST;
		Log.d("vsm2",
				String.format("processMovingEventOnInitStatus: distance = %f, direction = %d, velocity=%f",
						distance, direction, velocity));
		
		if(STATUS_NULL == mTargetStatus
			&& direction == DIRECTION_UP) {
			//switch status
			setCinemaProgress(CinemaMan.CINEMA_TYPE_WEBVIEW,1f);
			setCinemaProgress(cinemaTp,-progress);
			//setCinemaProgress(cinemaTp,0);
			setWebviewEventSwitch(false);
			changeTargetStatus(e,STATUS_BROWSE);
		}
		else if(STATUS_NULL != mTargetStatus
				&& velocity < FingerTracker.VELOCITY_LIMIT){
			//the status is between STATUS_INIT & STATUS_BROWSE
			setCinemaProgress(cinemaTp, -progress);
			//setCinemaProgress(cinemaTp,Math.abs(progress));
			if(cinemaProgressIsFull(cinemaTp)) {
				setWebviewEventSwitch(true);
				changeStatus(e, mTargetStatus);
				changeTargetStatus(e,STATUS_NULL);
				mSlideTimesFromBottom = 0;
				Log.d("vtwice",
						String.format("mSlideTimesFromBottom: reset"));
			}
			
			if(cinemaProgressIsZero(cinemaTp)) {
				setWebviewEventSwitch(true);
				changeStatus(e, STATUS_INIT);
				changeTargetStatus(e,STATUS_NULL);
			}
		}
		else if(STATUS_NULL != mTargetStatus
				&& velocity >= FingerTracker.VELOCITY_LIMIT) {
			setWebviewEventSwitch(false);
			startCinema(cinemaTp,new CinemaListener() {

				@Override
				public void onCinemaEnd(Cinema cinema) {
					setWebviewEventSwitch(true);
					changeStatus(e, mTargetStatus);
					changeTargetStatus(e,STATUS_NULL);
					mSlideTimesFromBottom = 0;
					Log.d("vtwice",
							String.format("mSlideTimesFromBottom: reset"));
				}
			});
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 上午10:21:22 .
	 */
	private void processMovingEventOnBrowseStatus(final MotionEvent e, int direction, float distance, float velocity) {
		
		Log.d("vsm2",
				String.format("processMovingEventOnBrowseStatus: distance = %f, direction = %d, velocity=%f",
						distance, direction, velocity));
		
		final int cinemaTp1 = CinemaMan.CINEMA_TYPE_BROWSE_END;
		final int cinemaTp2 = CinemaMan.CINEMA_TYPE_BROWSE_REPLAY;
		final int cinemaTp3 = CinemaMan.CINEMA_TYPE_BROWSE_INIT;
		final float progress = distance/FingerTracker.DISTANCE_FAREST;

		if(STATUS_NULL == mTargetStatus) {
			if(direction == DIRECTION_UP) {
				if(mSlideTimesFromBottom == 0) {
					//do nothing
				}
				else if(mSlideTimesFromBottom == 1) {
					//switch status
					setWebviewEventSwitch(false);
					changeTargetStatus(e,STATUS_END);
					//setCinemaProgress(cinemaTp1,Math.abs(progress));
					setCinemaProgress(cinemaTp1,0);
					return;
				}
				else {
					//do nothing
				}
			}
			else if(direction == DIRECTION_DOWN){
				
				if(isWebViewAtTop()) {
					//shortcut: from browse status to init status
					//going to switch status
					//setCinemaProgress(cinemaTp3,0);
					setCinemaProgress(cinemaTp3,progress);
					setWebviewEventSwitch(false);
					changeTargetStatus(e,STATUS_INIT);
				}
				else {
					//normal path: from browse status to replay status
					//going to switch status
					//setCinemaProgress(cinemaTp2,Math.abs(progress));
					setCinemaProgress(cinemaTp2,0);
					setWebviewEventSwitch(false);
					changeTargetStatus(e,STATUS_REPLAY);
				}

			}

		}
		else if(STATUS_END == mTargetStatus){
			//1.the status is between STATUS_BROWSE & STATUS_END
			//2.check if it is low speed.
			//3.check if cinema is ended.
			//4.check if cinema is started.
			if(!isSpeedHighEnough(velocity)) {
				setCinemaProgress(cinemaTp1,-progress);
				if(cinemaProgressIsFull(cinemaTp1)) {
					setWebviewEventSwitch(true);
					changeStatus(e, mTargetStatus);
					changeTargetStatus(e,STATUS_NULL);
				}
				
				if(cinemaProgressIsZero(cinemaTp1)) {
					setWebviewEventSwitch(true);
					changeStatus(e, STATUS_BROWSE);
					changeTargetStatus(e,STATUS_NULL);
				}
			}
			else {
				CinemaListener cl = new CinemaListener() {
					
					@Override
					public void onCinemaEnd(Cinema cinema) {
						Log.d("vsm2",
								String.format("onCinemaEnd: cinema=%s", cinema));
						setWebviewEventSwitch(true);
						changeStatus(e, mTargetStatus);
						changeTargetStatus(e,STATUS_NULL);
					}
				};
				if(direction == DIRECTION_UP) {
					setWebviewEventSwitch(false);
					startCinema(CinemaMan.CINEMA_TYPE_BROWSE_END,cl);
				}
				else if(direction == DIRECTION_DOWN) {
					setWebviewEventSwitch(false);
					startCinema(CinemaMan.CINEMA_TYPE_BROWSE_END,cl);
				}
			}

		}
		else if(STATUS_REPLAY == mTargetStatus) {
			//1.the status is between STATUS_BROWSE & STATUS_END
			//2.check if it is low speed.
			
			if(!isSpeedHighEnough(velocity)) {
				setCinemaProgress(cinemaTp2,progress);
				if(cinemaProgressIsFull(cinemaTp2)) {
					setWebviewEventSwitch(true);
					changeStatus(e, mTargetStatus);
					changeTargetStatus(e,STATUS_NULL);
				}
				
				if(cinemaProgressIsZero(cinemaTp2)) {
					setWebviewEventSwitch(true);
					changeStatus(e, STATUS_BROWSE);
					changeTargetStatus(e,STATUS_NULL);
				}
			}
			else {
				if(direction == DIRECTION_UP) {
				}
				else if(direction == DIRECTION_DOWN) {
				}
			}
		}
		else if(STATUS_INIT == mTargetStatus) {
			//1.the status is between STATUS_BROWSE & STATUS_INIT
			
			if(!isSpeedHighEnough(velocity)) {
				setCinemaProgress(cinemaTp3,progress);
				if(cinemaProgressIsFull(cinemaTp3)) {
					setWebviewEventSwitch(true);
					changeStatus(e, mTargetStatus);
					changeTargetStatus(e,STATUS_NULL);
				}
				
				if(cinemaProgressIsZero(cinemaTp3)) {
					setWebviewEventSwitch(true);
					changeStatus(e, STATUS_BROWSE);
					changeTargetStatus(e,STATUS_NULL);
				}
			}
			else {
				if(direction == DIRECTION_UP) {
				}
				else if(direction == DIRECTION_DOWN) {
				}
			}
		}

		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 上午10:21:43 .
	 */
	private void processUpEventOnBrowseStatus(final MotionEvent e, int direction) {
		Log.d("vsm2",
				String.format("processUpEventOnBrowseStatus: event = ACTION_UP"));

		if(direction == DIRECTION_DOWN) {
			Log.d("vtwice",
					String.format("mSlideTimesFromBottom: reset"));
			mSlideTimesFromBottom = 0;
		}

		boolean isAtBottom = false;
		isAtBottom = isWebViewAtBottom();
		Log.d("vtwice",
				String.format("mSlideTimesFromBottom: isAtBottom = %b", isAtBottom));
		if(isAtBottom && direction == DIRECTION_UP) {
			mSlideTimesFromBottom ++;
			Log.d("vtwice",
					String.format("mSlideTimesFromBottom: grow up to %d", mSlideTimesFromBottom));
			if(mSlideTimesFromBottom >= 3) {
				mSlideTimesFromBottom = 3;
			}
		}
		
		if(STATUS_END == mTargetStatus) {
			final int targetStatus = mTargetStatus;
			setWebviewEventSwitch(false);
			startCinema(CinemaMan.CINEMA_TYPE_BROWSE_END,new CinemaListener() {
				
				@Override
				public void onCinemaEnd(Cinema cinema) {
					setWebviewEventSwitch(true);
					changeStatus(e, targetStatus);
					changeTargetStatus(e,STATUS_NULL);
					mSlideTimesFromBottom = 0;
					Log.d("vtwice",
							String.format("mSlideTimesFromBottom: reset"));
				}
			});
		}
		else if(STATUS_REPLAY == mTargetStatus) {
			final int targetStatus = mTargetStatus;
			setWebviewEventSwitch(false);
			startCinema(CinemaMan.CINEMA_TYPE_BROWSE_REPLAY,new CinemaListener() {
				
				@Override
				public void onCinemaEnd(Cinema cinema) {
					Log.d("vsm2","onCinemaEnd: cinema = " + cinema);
					setWebviewEventSwitch(true);
					changeStatus(e, targetStatus);
					changeTargetStatus(e,STATUS_NULL);
					mSlideTimesFromBottom = 0;
					Log.d("vtwice",
							String.format("mSlideTimesFromBottom: reset"));
				}
			});
		}
		else if(STATUS_INIT == mTargetStatus) {
			final int targetStatus = mTargetStatus;
			setWebviewEventSwitch(false);
			startCinema(CinemaMan.CINEMA_TYPE_BROWSE_INIT,new CinemaListener() {
				
				@Override
				public void onCinemaEnd(Cinema cinema) {
					Log.d("vsm2","onCinemaEnd: cinema = " + cinema);
					setCinemaProgress(CinemaMan.CINEMA_TYPE_WEBVIEW, 0f);
					setWebviewEventSwitch(true);
					changeStatus(e, targetStatus);
					changeTargetStatus(e,STATUS_NULL);
					mSlideTimesFromBottom = 0;
					Log.d("vtwice",
							String.format("mSlideTimesFromBottom: reset"));
				}
			});
		}
	}
	

	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 下午6:33:34 .
	 * @param direction
	 * @param distance
	 * @param velocity
	 */
	private void processMovingEventOnEndStatus(final MotionEvent e, int direction, float distance, float velocity) {
		final int cinemaTp = CinemaMan.CINEMA_TYPE_END_REPLAY;
		final float progress = distance/FingerTracker.DISTANCE_FAREST;
		Log.d("vsm",
				String.format("processMovingEventOnEndStatus: distance = %f, direction = %d, velocity=%f",
						distance, direction, velocity));
		
		if(STATUS_NULL == mTargetStatus
			&& direction == DIRECTION_DOWN) {
			//switch status
			//setCinemaProgress(cinemaTp,Math.abs(progress));
			setCinemaProgress(cinemaTp,0);
			setWebviewEventSwitch(false);
			changeTargetStatus(e,STATUS_REPLAY);
		}
		else if(STATUS_NULL != mTargetStatus
				&& velocity < FingerTracker.VELOCITY_LIMIT){
			//the status is between STATUS_END & STATUS_REPLAY
			setCinemaProgress(cinemaTp, progress);
			if(cinemaProgressIsFull(cinemaTp)) {
				setWebviewEventSwitch(true);
				changeStatus(e, mTargetStatus);
				changeTargetStatus(e,STATUS_NULL);
			}
			
			if(cinemaProgressIsZero(cinemaTp)) {
				setWebviewEventSwitch(true);
				changeStatus(e, STATUS_END);
				changeTargetStatus(e,STATUS_NULL);
			}
		}
		else if(STATUS_NULL != mTargetStatus
				&& velocity >= FingerTracker.VELOCITY_LIMIT) {
			//mCinemaMan.startCinema(cinemaTp,this);
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 下午6:34:01 .
	 * @param direction
	 * @param distance
	 * @param velocity
	 */
	private void processUpEventOnEndStatus(final MotionEvent e) {
		Log.d("vsm2",
				String.format("processUpEventOnEndStatus: event = ACTION_UP, mTargetStatus=%d", mTargetStatus));
		
		if(STATUS_END == mStatus
			&& STATUS_NULL != mTargetStatus) {
			setWebviewEventSwitch(false);
			startCinema(CinemaMan.CINEMA_TYPE_END_REPLAY,
					new CinemaListener() {

						@Override
						public void onCinemaEnd(Cinema cinema) {
							Log.d("vsm2",
									String.format("onCinemaEnd: cinema=%s", cinema));
							setWebviewEventSwitch(true);
							changeStatus(e, mTargetStatus);
							changeTargetStatus(e,STATUS_NULL);
							mSlideTimesFromBottom = 0;
							Log.d("vtwice",
									String.format("mSlideTimesFromBottom: reset"));
						}
				
			});
		}
		return;
	}
	
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午2:03:28 .
	 */
	public void processEventOnEndStatus(MotionEvent e, int direction, float distance, float velocity) {
		if(MotionEvent.ACTION_MOVE == e.getAction()) {
			processMovingEventOnEndStatus(e, direction,distance,velocity);
		}
		else if(MotionEvent.ACTION_UP == e.getAction()) {
			processUpEventOnEndStatus(e);
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月20日 下午2:37:45 .
	 * @param direction
	 * @param distance
	 * @param velocityY
	 */
	private void processMovingEventOnReplayStatus(final MotionEvent e, int direction,
			float distance, float velocity) {
		final WebView webView = getActiveWebView();
		if(webView == null) {
			throw new RuntimeException("Active webview is null");
		}
		
		Log.d("vsm2",
				String.format("processMovingEventOnReplayStatus: distance = %f, direction = %d, velocity=%f",
						distance, direction, velocity));
		
		final int cinemaTp1 = CinemaMan.CINEMA_TYPE_REPLAY_INIT;
		final int cinemaTp2 = CinemaMan.CINEMA_TYPE_REPLAY_BROWSE;
		final float progress = distance/FingerTracker.DISTANCE_FAREST;
		
		
		if(STATUS_NULL == mTargetStatus) {
			if(direction == DIRECTION_DOWN) {
				boolean isAtTop = isWebViewAtTop();
				if(isAtTop) {
					//1. web is at the top of the webview
					//2. finger is moving down
					//3. so we switch status
					//setCinemaProgress(cinemaTp1,Math.abs(progress));
					setCinemaProgress(cinemaTp1,0);
					setWebviewEventSwitch(false);
					changeTargetStatus(e,STATUS_INIT);	
					
					Log.d("vsm2",
							String.format("processMovingEventOnReplayStatus:switching status, distance = %f",
									distance));
					
				}
			}
			else if(direction == DIRECTION_UP){
				//1. finger is moving up
				//2. so we switch status
				//setCinemaProgress(cinemaTp2,Math.abs(progress));
				setCinemaProgress(cinemaTp2,0);
				setWebviewEventSwitch(false);
				changeTargetStatus(e,STATUS_BROWSE);
			}

		}
		else if(STATUS_INIT == mTargetStatus){
			//1.the status is between STATUS_REPLAY & STATUS_INIT
			//2.check if it is low speed.
			//3.check if cinema is ended.
			//4.check if cinema is started.
			if(!isSpeedHighEnough(velocity)) {
				setCinemaProgress(cinemaTp1,progress);
				if(cinemaProgressIsFull(cinemaTp1)) {
					setWebviewEventSwitch(true);
					changeStatus(e, STATUS_INIT);
					changeTargetStatus(e,STATUS_NULL);
					setCinemaProgress(CinemaMan.CINEMA_TYPE_WEBVIEW, 0f);
				}
				
				if(cinemaProgressIsZero(cinemaTp1)) {
					setWebviewEventSwitch(true);
					changeStatus(e, STATUS_REPLAY);
					changeTargetStatus(e,STATUS_NULL);
				}
			}
			else {
				//do nothing
			}

		}
		else if(STATUS_BROWSE == mTargetStatus) {
			//1.the status is between STATUS_REPLAY & STATUS_BROWSE
			//2.check if it is low speed.
			
			if(!isSpeedHighEnough(velocity)) {
				setCinemaProgress(cinemaTp2, -progress);
				if(cinemaProgressIsFull(cinemaTp2)) {
					setWebviewEventSwitch(true);
					changeStatus(e, mTargetStatus);
					changeTargetStatus(e,STATUS_NULL);
				}
				
				if(cinemaProgressIsZero(cinemaTp2)) {
					setWebviewEventSwitch(true);
					changeStatus(e, STATUS_REPLAY);
					changeTargetStatus(e,STATUS_NULL);
				}
			}
			else {
				//do nothing
			}
		}

		return;
	}
	

	/**
	 * 
	 * Vulcan created this method in 2015年3月20日 下午2:37:41 .
	 */
	private void processUpEventOnReplayStatus(final MotionEvent e, int direction) {
		
		int tpRepBrow = CinemaMan.CINEMA_TYPE_REPLAY_BROWSE;
		int tpRepInit = CinemaMan.CINEMA_TYPE_REPLAY_INIT;
		
		Log.d("vsm2",
				String.format("processUpEventOnReplayStatus: event = ACTION_UP"));

		if(direction == DIRECTION_DOWN) {
			Log.d("vtwice",
					String.format("mSlideTimesFromBottom: reset"));
			mSlideTimesFromBottom = 0;
		}
		
		if(STATUS_NULL != mTargetStatus) {
			if(direction == DIRECTION_UP) {
				startCinema(tpRepBrow,new CinemaListener() {
					
					@Override
					public void onCinemaEnd(Cinema cinema) {
						Log.d("vsm2","onCinemaEnd: cinema = " + cinema);
						setWebviewEventSwitch(true);
						changeStatus(e, STATUS_BROWSE);
						changeTargetStatus(e,STATUS_NULL);
					}
				});
			}
			else if(direction == DIRECTION_DOWN){
				startCinema(tpRepInit,new CinemaListener() {
					@Override
					public void onCinemaEnd(Cinema cinema) {
						setCinemaProgress(CinemaMan.CINEMA_TYPE_WEBVIEW, 0f);
						setWebviewEventSwitch(true);
						changeStatus(e, STATUS_INIT);
						changeTargetStatus(e,STATUS_NULL);
					}
				});
			}
		}

	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午2:03:38 .
	 * @param velocityY 
	 * @param distance 
	 * @param movingDirection 
	 * @param event 
	 */
	public void processEventOnReplayStatus(MotionEvent e, int direction, float distance, float velocityY) {
		if(MotionEvent.ACTION_MOVE == e.getAction()) {
			processMovingEventOnReplayStatus(e, direction,distance,velocityY);
		}
		else if(MotionEvent.ACTION_UP == e.getAction()) {
			processUpEventOnReplayStatus(e, direction);
		}
		return;
	}


	/**
	 * 
	 * Vulcan created this method in 2015年3月20日 上午9:34:10 .
	 * @param distance
	 * @return
	 */
	public int directionOfDistance(float distance) {
		int movingDirection = DIRECTION_NULL;
		int rst = Float.compare(distance, 0f);
		if(rst > 0) {
			movingDirection = DIRECTION_DOWN;
		}
		else if(rst == 0){
			movingDirection = DIRECTION_NULL;
		}
		else {
			movingDirection = DIRECTION_UP;
		}
		return movingDirection;
	}
	
	/**
	 * when users are going to ask titlebar to be shown, this method will be called.
	 * Vulcan created this method in 2015年3月31日 上午10:59:15 .
	 */
	public void onAskTitleBar() {
		if(mStatus == STATUS_BROWSE && mTargetStatus == STATUS_NULL
				&& !isAnyPlaying()) {
			stateTransitionBrowse2Init();
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月26日 上午10:22:31 .
	 */
	private void stateTransitionBrowse2Init() {
		startCinema(CinemaMan.CINEMA_TYPE_BROWSE_INIT,
				new CinemaListener() {
					@Override
					public void onCinemaEnd(Cinema cinema) {
						Log.d("vsm2",
								String.format("stateTransitionInit2Browse: cinema=%s", cinema));
						setWebviewEventSwitch(true);
						changeStatus(null, STATUS_INIT);
						changeTargetStatus(null,STATUS_NULL);
						mSlideTimesFromBottom = 0;
						Log.d("vtwice",
								String.format("mSlideTimesFromBottom: reset"));
					}
			
		});
		return;
	}

	/**
	 * Vulcan created this method in 2015年3月23日 下午3:45:45 .
	 * @param l
	 * @param t
	 * @param oldl
	 * @param oldt
	 */
	public void onScrollChanged(int l, int t, int oldl, int oldt) {
		if(isWebViewAtBottom() && mSlideTimesFromBottom == 0) {
			mSlideTimesFromBottom ++;
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 上午10:29:21 .
	 * @param v
	 * @param event
	 */
	public void onTouchEvent(View v, MotionEvent event) {
		//ignore all events if any cinema is playing.
		if(isAnyPlaying()) {
			return;
		}
		
		if(getActiveWebView() == null) {
			return;
		}
		
		int movingDirection = DIRECTION_NULL;
		float velocityY = 0f;
		float distance = 0f;
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mFingerTracker.startTracking(event);
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			distance = mFingerTracker.getDistance(event.getRawY());
			movingDirection = directionOfDistance(distance);
			velocityY = mFingerTracker.computeCurrentYVelocity();
			mFingerTracker.stopTracking();
			break;

		case MotionEvent.ACTION_MOVE:
			mFingerTracker.addMovement(event);
			distance = mFingerTracker.getDistance(event.getRawY());
			movingDirection = directionOfDistance(distance);
			velocityY = mFingerTracker.computeCurrentYVelocity();
			break;
		}
		
		Log.d("vsm2",
				String.format("onTouchEvent: start=%f,now=%f,distance=%f",
						mFingerTracker.getStartY(), event.getRawY(), distance));
		if(STATUS_INIT == mStatus) {
			processEventOnInitStatus(event,movingDirection, distance, velocityY);
		}
		else if(STATUS_BROWSE == mStatus) {
			processEventOnBrowseStatus(event,movingDirection, distance, velocityY);
		}
		else if(STATUS_END == mStatus) {
			processEventOnEndStatus(event,movingDirection, distance, velocityY);
		}
		else if(STATUS_REPLAY == mStatus) {
			processEventOnReplayStatus(event,movingDirection, distance, velocityY);
		}
		else {
			//throw new RuntimeException("Invalid status of mStatus");
		}
		
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 下午5:24:29 .
	 * @param newStatus
	 */
	public void changeStatus(MotionEvent e, int newStatus) {
		int oldStatus = mStatus;
		
		Log.d("vsm2", String.format("changeStatus: %d->%d", mStatus,newStatus));
		mStatus = newStatus;
		
		if(oldStatus != newStatus) {
			onStatusChanged(e, oldStatus, newStatus);
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月23日 下午5:36:21 .
	 * @param oldStatus
	 * @param newStatus
	 */
	protected void onStatusChanged(MotionEvent e, int oldStatus, int newStatus) {
		//mFingerTracker.updateY(e.getRawY());
		//Log.d("vsm2", String.format("onStatusChanged: updateY: %f", e.getRawY()));
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月24日 上午9:22:36 .
	 * @param e
	 * @param oldTargetStatus
	 * @param newTargetStatus
	 */
	protected void onTargetStatusChanged(MotionEvent e, int oldTargetStatus, int newTargetStatus) {
		
		if((oldTargetStatus == STATUS_NULL
			|| newTargetStatus == STATUS_NULL)
			&& (e != null)) {
			Log.d("vsm2", String.format("onTargetStatusChanged: updateY: %f", e.getRawY()));
			mFingerTracker.updateY(e.getRawY());
		}

	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 下午5:27:08 .
	 * @param newTargetStatus
	 */
	public void changeTargetStatus(MotionEvent e, int newTargetStatus) {
		int oldTargetStatus = mTargetStatus;
		Log.d("vsm2", String.format("changeTargetStatus: %d->%d", mTargetStatus,newTargetStatus));
		mTargetStatus = newTargetStatus;
		
		if(oldTargetStatus != newTargetStatus) {
			onTargetStatusChanged(e, oldTargetStatus, newTargetStatus);
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 上午10:32:31 .
	 * @param cinemaTp
	 * @param progress
	 */
	@SuppressWarnings("unused")
	private void changeCinemaProgress(int cinemaTp, float progress) {
		Log.d("vsm2",
				 String.format("changeCinemaProgress: cinemaTp=%d,progress=%f",cinemaTp,progress));
		mCinemaMan.changeCinemaProgress(cinemaTp,progress);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 上午10:33:26 .
	 * @param cinemaTp
	 * @return
	 */
	private boolean cinemaProgressIsFull(int cinemaTp) {

		boolean rst = mCinemaMan.isProgressFull(cinemaTp);
		
		Log.d("vsm2",
				 String.format("cinemaProgressIsFull: cinemaTp=%d,rst=%b",cinemaTp,rst));
		
		return rst;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 上午10:37:26 .
	 * @param cinemaTp
	 * @return
	 */
	private boolean cinemaProgressIsZero(int cinemaTp) {
		boolean rst = mCinemaMan.isProgressZero(cinemaTp);
		
		Log.d("vsm2",
				 String.format("cinemaProgressIsZero: cinemaTp=%d,rst=%b",cinemaTp,rst));
		
		return rst;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 上午10:34:21 .
	 * @param cinemaTp
	 * @param progress
	 */
	public void setCinemaProgress(int cinemaTp, float progress) {
		Log.d("vsm2",
				 String.format("setCinemaProgress: cinemaTp=%d,progress=%f",cinemaTp,progress));
		mCinemaMan.setCinemaProgress(cinemaTp,progress);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月23日 下午4:28:13 .
	 * @param cinemaTp
	 */
	@SuppressWarnings("unused")
	private float getCinemaProgress(int cinemaTp) {
		return mCinemaMan.getCinemaProgress(cinemaTp);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 上午11:31:37 .
	 * @param cinemaTp
	 * @param cinemaListener
	 */
	public void startCinema(int cinemaTp, CinemaListener cinemaListener) {
		Log.d("vsm2",
				 String.format("startCinema: cinemaTp=%d,cinemaListener=%s",cinemaTp,cinemaListener));
		mCinemaMan.startCinema(cinemaTp, cinemaListener);
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 上午11:21:39 .
	 * @param velocity
	 * @return
	 */
	private boolean isSpeedHighEnough(float velocity) {
		return velocity >= FingerTracker.VELOCITY_LIMIT;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月19日 下午5:47:09 .
	 * @return
	 */
	private boolean isAnyPlaying() {
		boolean b = mCinemaMan.isAnyPlaying();
		return b;
	}
	

	/**
	 * 
	 * Vulcan created this method in 2015年3月20日 下午2:43:53 .
	 * @param webView
	 * @return
	 */
	private boolean isWebViewAtTop() {
		if(getActiveWebView() == null) {
			throw new RuntimeException("Active WebView is null");
		}
		return mCinemaMan.webviewAtTop(getActiveWebView());
	}
	

	/**
	 * 
	 * Vulcan created this method in 2015年3月20日 下午2:43:30 .
	 * @param webView
	 * @return
	 */
	private boolean isWebViewAtBottom() {
		if(getActiveWebView() == null) {
			throw new RuntimeException("Active WebView is null");
		}
		return mCinemaMan.webviewAtBottom(getActiveWebView());
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月20日 下午1:55:22 .
	 * @return
	 */
	private WebView getActiveWebView() {
		if(mBaseUi != null) {
			if(mBaseUi.getActiveTab() != null) {
				return mBaseUi.getActiveTab().getWebView();
			}
		}
		return null;
	}
	
	private BaseUi mBaseUi = null;;

}
