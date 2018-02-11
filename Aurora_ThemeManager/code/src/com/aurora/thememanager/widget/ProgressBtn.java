package com.aurora.thememanager.widget;

import android.R.color;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import  com.aurora.thememanager.R;

public class ProgressBtn extends LinearLayout {

	public static final int STATUS_NORMAL = 1;
	public static final int STATUS_WAIT_DOWNLOAD = 2;
	public static final int STATUS_PROGRESSING_DOWNLOAD = 3;
	public static final int STATUS_WAIT_INSTALL = 4;
	public static final int STATUS_PROGRESSING_INSTALLING = 5;
	public static final int STATUS_FOUCE = 6;
	public static final int STATUS_FOUCE_NORMAL = 7;
	public static final int STATUS_PROGRESSING_RETRY = 8;
	public static final int STATUS_INSTALLED = 9;
	public static final int STATUS_UPDATE = 10;
    public static final int STATUS_UPDATE_FROM_DETAIL = 11;
	private int status = STATUS_NORMAL;

	private float startShow = 0.9f;
	private static int progressTime = 500;

	private Button btn;
	private Button progress_btn;
	private ToCircleView tcv_toProgress;
	private RoundProgressView round_progress_view;
	private ImageView iv_progress1;
	private ImageView iv_progress2;
	private AuroraGlassView agv1;
	private AuroraGlassView agv2;
	private ToCircleView tcv_toRect;
	private Button fouceBtn;

	private float textSize;

	private int progress = 0;
	private boolean isRuningStartAnim = false;
	private boolean isRuningEndAnim = false;
	private boolean mShowDownloadText = true;
	private OnClickListener onButtonClickListener = null;
	private OnClickListener onNormalClickListener = null;
	private OnAnimListener onBeginAnimListener;

	public ProgressBtn(Context context) {
		super(context);
		initView();
	}

	public ProgressBtn(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (btn != null) {
			btn.setEnabled(enabled);
		}
		if (fouceBtn != null) {
			fouceBtn.setEnabled(enabled);
		}
		if( progress_btn != null){
		    progress_btn.setEnabled(enabled);
		}
		    
		
	}

	
	
	
	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.view_progressbtn, this);
		btn = (Button) view.findViewById(R.id.btn);
		progress_btn = (Button) view.findViewById(R.id.progress_btn);
		tcv_toProgress = (ToCircleView) view.findViewById(R.id.tcv_toProgress);
		round_progress_view = (RoundProgressView) view
				.findViewById(R.id.round_progress_view);
		iv_progress1 = (ImageView) view.findViewById(R.id.iv_progress1);
		iv_progress2 = (ImageView) view.findViewById(R.id.iv_progress2);
		agv1 = (AuroraGlassView) view.findViewById(R.id.agv1);
		agv2 = (AuroraGlassView) view.findViewById(R.id.agv2);
		tcv_toRect = (ToCircleView) view.findViewById(R.id.tcv_toRect);
		fouceBtn = (Button) view.findViewById(R.id.fouceBtn);

		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

					if (onButtonClickListener != null) {
						onButtonClickListener.onClick(v);
					}

					startBeginAnim();

					if (onNormalClickListener != null) {
						onNormalClickListener.onClick(v);
					}
			}
		});
		
		expandViewTouchDelegate(btn);

		textSize = getResources().getDimension(R.dimen.progressBtnTextSize);
	}
	
	/**
	 * @Title: startBeginAnim
	 * @Description: TODO 进度开始前动画
	 * @param
	 * @return void
	 * @throws
	 */
	public void startBeginAnim() {
		btn.setVisibility(View.GONE);
		round_progress_view.setVisibility(View.GONE);
		int width = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_width);
		int height = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_height);
		tcv_toProgress.setAllViewWidthAndHeight(width, height,
				ToCircleView.TYPE_TO_CIRCLE);
		tcv_toProgress.setVisibility(View.VISIBLE);
		isRuningStartAnim = true;
		tcv_toProgress.startAnim(new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {

				if (interpolatedTime == 1) {
					isRuningStartAnim = false;
					setStatus(STATUS_WAIT_DOWNLOAD);

					if (onBeginAnimListener != null) {
						onBeginAnimListener.onEnd(ProgressBtn.this);
					}

				}
			}
		});
		round_progress_view.setProgress(0);
	}

	
	public void showDownloadButtonWithoutText(){
		btn.setBackgroundResource(R.drawable.btn_download_normal_for_detail);
		btn.setPadding(0, 0, 0, 0);
		ViewGroup.LayoutParams params = btn.getLayoutParams();
		params.width = getResources().getDimensionPixelSize(R.dimen.btn_download_normal_width);
		params.height = getResources().getDimensionPixelSize(R.dimen.btn_download_normal_height);
		btn.setLayoutParams(params);
		mShowDownloadText = false;
		btn.setText("");
		btn.setVisibility(View.VISIBLE);
		tcv_toProgress.setVisibility(View.GONE);
		progress_btn.setVisibility(View.GONE);
		round_progress_view.setVisibility(View.GONE);
		iv_progress1.setVisibility(View.GONE);
		iv_progress2.setVisibility(View.GONE);
		agv1.setVisibility(View.GONE);
		agv2.setVisibility(View.GONE);
		tcv_toRect.setVisibility(View.GONE);
		fouceBtn.setVisibility(View.GONE);
		expandViewTouchDelegate(btn);
	}
	
	
	
	/**
	 * @Title: startEndAnim
	 * @Description: TODO 结束进度后动画，按钮变为focus
	 * @param
	 * @return void
	 * @throws
	 */
	public void startEndAnim() {
		startEndAnim(true);
	}
	
	public void startEndAnim(final boolean fouce) {
		int width = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_width);
		int height = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_height);
		tcv_toRect.setAllViewWidthAndHeight(width, height,
				ToCircleView.TYPE_TO_RECT);
		tcv_toRect.setVisibility(View.VISIBLE);
		progress_btn.setVisibility(View.GONE);
		round_progress_view.setVisibility(View.GONE);
		iv_progress1.clearAnimation();
		iv_progress1.setVisibility(View.GONE);
		iv_progress2.clearAnimation();
		iv_progress2.setVisibility(View.GONE);
		agv1.stop();
		agv1.setVisibility(View.GONE);
		agv2.stop();
		agv2.setVisibility(View.GONE);
		isRuningEndAnim = true;
		tcv_toRect.startAnim(new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {

				if (interpolatedTime >= startShow) {
					float alpha = (interpolatedTime - startShow)
							/ (1 - startShow);
					fouceBtn.setAlpha(alpha);
					fouceBtn.setVisibility(View.VISIBLE);
				}

				if (interpolatedTime == 1) {
					isRuningEndAnim = false;
					if (fouce) {
						setStatus(STATUS_FOUCE);
					} else {
						setStatus(STATUS_FOUCE_NORMAL);
					}
				}
			}
		}, fouce);
	}

	public int getStatus() {
		return status;
	}
	

	public void setStatus(int status) {
		
		if (this.status == status) {
			return;
		}
		
		isRuningStartAnim = false;
		isRuningEndAnim = false;

		iv_progress1.clearAnimation();
		iv_progress2.clearAnimation();
		agv1.stop();
		agv2.stop();
		tcv_toProgress.clearAnimation();
		tcv_toRect.clearAnimation();
		round_progress_view.clearAnimation();
		
		this.status = status;
		switch (status) {
		case STATUS_INSTALLED:
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.VISIBLE);
			fouceBtn.setText(R.string.theme_applyed);
			restoreViewTouchDelegate(lastDelegate);
			break;
		case STATUS_NORMAL:
			btn.setVisibility(View.VISIBLE);
			btn.setTextColor(getContext().getResources().getColor(R.color.theme_list_item_download_btn_text_color));
			btn.setText(R.string.donwloadman_download);
			btn.setBackgroundResource(R.drawable.btn_theme_list_download_btn_bg);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			expandViewTouchDelegate(btn);
			
		//	btn_backup.setAlpha(1);
			//btn_backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, btn.getTextSize());
			break;
			
		case STATUS_UPDATE:
			btn.setVisibility(View.VISIBLE);
			btn.setTextColor(getContext().getResources().getColor(R.color.theme_list_item_download_btn_text_color));
			btn.setBackgroundResource(R.drawable.btn_theme_list_download_btn_bg);
			btn.setText(R.string.status_text_update);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			expandViewTouchDelegate(btn);
			
		//	btn_backup.setAlpha(1);
			//btn_backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, btn.getTextSize());
			break;
			
		case STATUS_UPDATE_FROM_DETAIL:
			btn.setVisibility(View.VISIBLE);
			btn.setTextColor(getContext().getResources().getColor(R.color.theme_main_color));
			btn.setBackgroundColor(Color.TRANSPARENT);
			btn.setText(R.string.status_text_update);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			expandViewTouchDelegate(btn);
			
		//	btn_backup.setAlpha(1);
			//btn_backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, btn.getTextSize());
			break;
			
		case STATUS_WAIT_DOWNLOAD:
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.VISIBLE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			
			restoreViewTouchDelegate(lastDelegate);
			
			agv1.start();
			break;
		case STATUS_PROGRESSING_DOWNLOAD:
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.VISIBLE);
			round_progress_view.setVisibility(View.VISIBLE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			expandViewTouchDelegate(progress_btn);
			
			break;
		case STATUS_PROGRESSING_RETRY:{
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.VISIBLE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			expandViewTouchDelegate(progress_btn);
			
			break;
		}
		case STATUS_WAIT_INSTALL:
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setText(R.string.item_apply);
			fouceBtn.setVisibility(View.VISIBLE);
			restoreViewTouchDelegate(lastDelegate);
			break;
		case STATUS_PROGRESSING_INSTALLING:
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.VISIBLE);
			iv_progress2.setVisibility(View.VISIBLE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			restoreViewTouchDelegate(lastDelegate);
			
			iv_progress1.postInvalidate();
			iv_progress1.startAnimation(createRotateAnimation(false));
			iv_progress2.postInvalidate();
			iv_progress2.startAnimation(createRotateAnimation(true));
			break;
		case STATUS_FOUCE:
			setFouceStyle();
			
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.VISIBLE);
			expandViewTouchDelegate(fouceBtn);
			
			break;
		case STATUS_FOUCE_NORMAL:
			setFouceNormalStyle();
			
			btn.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.VISIBLE);
			expandViewTouchDelegate(fouceBtn);
			
			break;
		}
	}

	public boolean isRuningStartAnim() {
		return isRuningStartAnim;
	}

	public boolean isRuningEndAnim() {
		return isRuningEndAnim;
	}

	public void setOnButtonClickListener(OnClickListener onButtonClickListener) {
		this.onButtonClickListener = onButtonClickListener;
	}

	public void setOnNormalClickListener(OnClickListener onNormalClickListener) {
		this.onNormalClickListener = onNormalClickListener;
	}

	public void setBtnText(String text) {
		btn.setText(text);
		btn.postInvalidate();
	}

	public void setFoucesBtnText(String text) {
		fouceBtn.setText(text);
		fouceBtn.postInvalidate();
	}

	public void setOnFoucsClickListener(OnClickListener onFoucsClickListener) {
		fouceBtn.setOnClickListener(onFoucsClickListener);
	}

	public void setOnProgressClickListener(
			OnClickListener onProgressClickListener) {
		progress_btn.setOnClickListener(onProgressClickListener);
	}

	public void setProgressBackground(int resid) {
		progress_btn.setBackgroundResource(resid);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		if (status != STATUS_PROGRESSING_DOWNLOAD && status != STATUS_PROGRESSING_RETRY) {
			setStatus(STATUS_PROGRESSING_DOWNLOAD);
		}
		round_progress_view.setProgress(progress);
	}

	public void setProgressAnim(int progress) {
		this.progress = progress;
		if (status != STATUS_PROGRESSING_DOWNLOAD) {
			setStatus(STATUS_PROGRESSING_DOWNLOAD);
		}
		round_progress_view.setProgressAnim(progress, progressTime);
	}

	public void setOnBeginAnimListener(OnAnimListener onBeginAnimListener) {
		this.onBeginAnimListener = onBeginAnimListener;
	}
	
	public void setFouceStyle() {
		fouceBtn.setBackgroundResource(R.drawable.button_focus_selector);
		fouceBtn.setTextColor(getResources().getColor(R.color.white));
	}
	
	public void setFouceNormalStyle() {
		fouceBtn.setBackgroundResource(R.drawable.button_default_selector);
		fouceBtn.setTextColor(getResources().getColorStateList(R.color.normal_btn_text_color));
	}

	/**
	 * @Title: createRotateAnimation
	 * @Description: 创建旋转动画
	 * @param @return
	 * @return RotateAnimation
	 * @throws
	 */
	private RotateAnimation createRotateAnimation(boolean reverse) {
		RotateAnimation animation = null;
		if (!reverse) {
			animation = new RotateAnimation(0, 3600, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		} else {
			animation = new RotateAnimation(0, -3600, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		}
		animation.setInterpolator(new LinearInterpolator());
		animation.setFillAfter(true);
		animation.setDuration(10000);
		animation.setStartOffset(0);
		animation.setRepeatCount(1000);
		return animation;
	}
	
	//====================加大按钮点击区域start====================//
	
	private View lastDelegate;
	
	private void expandViewTouchDelegate(final View view) {
		post(new Runnable() {
			@Override
			public void run() {
				Rect bounds = new Rect();
				view.setEnabled(true);
				view.getHitRect(bounds);

		        bounds.top -= 500;
		        bounds.bottom += 500;
		        bounds.left -= 500;
		        bounds.right += 500;

		        TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

		        if (View.class.isInstance(view.getParent())) {
		            ((View) view.getParent()).setTouchDelegate(touchDelegate);
		        }
		        
		        lastDelegate = view;
			}
		});
	}
	
	private void restoreViewTouchDelegate(final View view) {
        post(new Runnable() {
            @Override
            public void run() {
            	if (view != null) {
            		Rect bounds = new Rect();
            		bounds.setEmpty();
            		TouchDelegate touchDelegate = new TouchDelegate(bounds, view);
            		
            		if (View.class.isInstance(view.getParent())) {
            			((View) view.getParent()).setTouchDelegate(touchDelegate);
            		}
            	}
            	
            }
        });
    }
	
	//====================加大按钮点击区域end====================//
	
	public interface OnAnimListener {
		public void onEnd(ProgressBtn view);
	}

}
