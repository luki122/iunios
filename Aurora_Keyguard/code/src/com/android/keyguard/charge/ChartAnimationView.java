package com.android.keyguard.charge;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChartAnimationView extends FrameLayout {
    private ProgressView progressWaveImg;
    private RelativeLayout progressLayout;
    private TextView progressText;
    private TextView progressPercentText;
    private ArcView arcView;
    private Handler handler;

    /**
     * 是否需要显示弧形动画
     */
    private boolean needShowArcViewAni = false;

    public ChartAnimationView(Context context) {
        this(context, null);
    }

    public ChartAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        needShowArcViewAni = false;
        handler = new Handler();
    }

    private void startAnim() {
        if (progressWaveImg == null || arcView == null || progressText == null || this.VISIBLE != View.VISIBLE) {
            return;
        }
        Animation animation1 = AnimationUtils.loadAnimation(getContext(), R.anim.chart_ani_alpha_in);
        animation1.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                progressWaveImg.setVisibility(View.VISIBLE);
            }
        });
        progressWaveImg.startAnimation(animation1);

        Animation animation2 = AnimationUtils.loadAnimation(getContext(), R.anim.chart_ani_push_bottom_in);
        animation2.setInterpolator(new OvershootInterpolator(1.5F));
        animation2.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                needShowArcViewAni = true;
                arcView.startAnimPublic();
                progressWaveImg.startAnimPublic();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
//                progressText.setVisibility(View.VISIBLE);
//                progressPercentText.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.VISIBLE);
            }

        });
//        progressText.startAnimation(animation2);
        progressLayout.startAnimation(animation2);
    }

    /**
     * 电量变化(必须放在UI线程)
     * 
     * @param progress
     *            现有电量占满电量的百分比
     */
    public void powerChanges(int progress) {
        if (progressWaveImg == null || progressText == null || arcView == null) {
            return;
        }
        if (progress < 0) {
            progress = 0;
        } else if (progress >= 100) {
            progress = 100;
        }
        progressWaveImg.setProgress(progress);
//        progressText.setText("" + progress + "%");
        progressText.setText("" + progress);
        progressPercentText.setText("%");
        if (progress < 100) {
            arcView.setVisibility(View.VISIBLE);
        }
        startAnim();
//        if (needShowArcViewAni) {
//            if (progress == 100) {
//                arcView.endAnim();
//                arcView.setVisibility(View.GONE);
//            } else {
//                arcView.setVisibility(View.VISIBLE);
//                arcView.startAnimPublic();
//            }
//        }
        if (progress == 100) {
            arcView.endAnim();
            arcView.setVisibility(View.GONE);
        }
    }

    public void onCreate(final int progress) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressWaveImg = ( ProgressView ) findViewById(R.id.progressWaveImg);
                progressWaveImg.setVisibility(View.INVISIBLE);
                progressWaveImg.setProgress(progress);
                progressText = ( TextView ) findViewById(R.id.progressText);
                arcView = ( ArcView ) findViewById(R.id.arcView);
//                progressText.setText("" + progress + "%");
                progressText.setText("" + progress);
                progressPercentText.setText("%");
                startAnim();
            }
        }, 500);

    }

    public void onRestart() {
        if (handler == null || arcView == null || this.VISIBLE != View.VISIBLE) {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                needShowArcViewAni = true;
                arcView.startAnimPublic();
            }
        }, 300);
    }

    public void onStop() {
        if (arcView == null) {
            return;
        }
        needShowArcViewAni = false;
        arcView.endAnimImmediately();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("zhangxin", "chartView=onFinishInflate");
        progressWaveImg = ( ProgressView ) findViewById(R.id.progressWaveImg);
        progressWaveImg.setVisibility(View.INVISIBLE);
        progressText = ( TextView ) findViewById(R.id.progressText);
        progressPercentText = ( TextView ) findViewById(R.id.progressPercentText);
        progressLayout = ( RelativeLayout ) findViewById(R.id.progressLayout);
        arcView = ( ArcView ) findViewById(R.id.arcView);
//        startAnim();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("zhangxin", "chartView=onAttachedToWindow");
//        startAnim();
//        onRestart();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("zhangxin", "chartView=onDetachedFromWindow");
//        onStop();
    }

//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        if(!changed){
//            return ;
//        }
//        super.onLayout(changed, left, top, right, bottom);
//    }

}
