/**
 * 
 */
package com.aurora.iunivoice.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.account.VerifyCodeLoader;
import com.aurora.iunivoice.activity.account.VerifyCodeLoader.LoadVerifyCodeErrorInfo;
import com.aurora.iunivoice.activity.account.VerifyCodeLoader.OnVerifyCodeLoadDoneListener;
import com.aurora.iunivoice.activity.account.VerifyCodeLoader.VC_EVENT;
import com.aurora.iunivoice.utils.BitmapUtil;
import com.aurora.iunivoice.utils.DensityUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.ToastUtil;

/**
 * 显示验证码的控件
 * 
 * @author JimXia
 * @date 2014年12月2日 上午10:25:23
 */
public class VerifyCodeView extends FrameLayout implements OnVerifyCodeLoadDoneListener {
    private static final String TAG = "VerifyCodeImageView";
    
    /**验证码刷新的URL*/
    private final String mVCCodeUrl = Globals.HTTP_ACCOUNT_REQUEST_URL +
            Globals.MODULE_AUTH + Globals.HTTPS_VCIMG_METHOD;
    
    private VC_EVENT mEvent;
    
    /**是否正在加载图片*/
    private boolean mOnLoading = false;
    
    private ImageView mVcCodeIv;
    private ImageView mProgressIv;
    
    private Paint mBgPaint;
    private final RectF mRoundRectF = new RectF();
    private static final float sRoundRadius = DensityUtil.dip2px(IuniVoiceApp.getInstance(), 5);
    private final Animation mProgressAnimation;
    
    public VerifyCodeView(Context context) {
        this(context, null);
    }

    public VerifyCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifyCodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.view_verify_code, this);
        mVcCodeIv = (ImageView) findViewById(R.id.vc_iv);
        mProgressIv = (ImageView) findViewById(R.id.progress_iv);
        mProgressIv.setVisibility(View.GONE);
        int padding = DensityUtil.dip2px(context, 2);
        setPadding(padding, padding, padding, padding);
        setOnClickListener(new OnClickHandler());
        
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(0xffededed);
        
        mProgressAnimation = BitmapUtil.createRotateAnimation(false);
//        loadVCCode();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mRoundRectF.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(mRoundRectF, sRoundRadius, sRoundRadius, mBgPaint);
        super.dispatchDraw(canvas);
    }

    public void setVCEvent(VC_EVENT event) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null.");
        }
        
        Log.d(TAG, "Jim, url: " + mVCCodeUrl + ", event: " + event.name());
        mEvent = event;
        loadVCCode();
    }
    
    public void refresh() {
        if (mEvent != null && !mOnLoading) {
            loadVCCode();
        }
    }
    
    private class OnClickHandler implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!mOnLoading) {
                loadVCCode();
            } else {
                Log.d(TAG, "Onloading, ignore this request");
            }
        }
    }
    
    private void loadVCCode() {
        new VerifyCodeLoader(mVCCodeUrl, mEvent, this).execute();
    }
    
    @Override
    public void onVerifyCodeLoadBegin() {
        mOnLoading = true;
        if (mProgressIv.getVisibility() != View.VISIBLE) {
            mProgressIv.setVisibility(View.VISIBLE);
            mVcCodeIv.setVisibility(View.GONE);
        }
        mProgressIv.startAnimation(mProgressAnimation);
    }

    @Override
    public void onVerifyCodeLoadDone(Bitmap verifyCode, LoadVerifyCodeErrorInfo error) {
        mOnLoading = false;
        mProgressIv.clearAnimation();
        if (verifyCode != null) {
            mProgressIv.setVisibility(View.GONE);
            mVcCodeIv.setVisibility(View.VISIBLE);
            mVcCodeIv.setImageBitmap(verifyCode);
        } else {
            if (error != null) {
                switch (error.getErrorCode()) {
                    case LoadVerifyCodeErrorInfo.ERROR_CODE_NO_NETWORK:
                        ToastUtil.shortToast(R.string.network_not_available);
                        break;
                    case LoadVerifyCodeErrorInfo.ERROR_CODE_NETWORK_EXCEPTION:
                        ToastUtil.shortToast(R.string.no_connection_prompt);
                        break;
                    case LoadVerifyCodeErrorInfo.ERROR_CODE_OTHER:
                        ToastUtil.shortToast(R.string.load_verify_code_error);
                        break;
                }
            }
        }
    }
}
