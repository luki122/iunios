package com.android.keyguard.charge;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.android.keyguard.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

public abstract class MaskedImage extends ImageView {
    private static final Xfermode MASK_XFERMODE;
    private final int LOW_PROGRESS = 10;
    private final int HIGHT_PROGRESS = 15;
    private Bitmap mask;
    private Paint paint;
    private Paint alphaPaintForGreen;
    private HashMap<String, SoftReference<Bitmap>> imageCache;
    private WaveData frontWaveData, backWaveData;
    private int frontWavesImgWidth, backWavesImgWidth;
    private int frontWaveMoveMaxDistance, backWaveMoveMaxDistance;
    private boolean isUseFirstGroupImg;// 是否使用第一组图片
    private Animation waveAnim = null;
    private int lastProgress = -1;
    private float frontWaveTopMargin;
    private float backWaveTopMargin;
    private int scale = 1;
    private boolean isStartAnim = false;

    private DisplayMetrics dm;

    static {
        PorterDuff.Mode localMode = PorterDuff.Mode.DST_IN;
        MASK_XFERMODE = new PorterDuffXfermode(localMode);
    }

    public MaskedImage(Context paramContext) {
        super(paramContext);
        initView();
    }

    public MaskedImage(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        initView();
    }

    public MaskedImage(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        initView();
    }

    private int getMyWidth() {
        int width = getWidth();
        if (width == 0) {
            width = ( int ) (60 * scale);
        }
        return width;
    }

    private int getMyHeight() {
        int height = getHeight();
        if (height == 0) {
            height = ( int ) (60 * scale);
        }
        return height;
    }

    private void initView() {
        imageCache = new HashMap<String, SoftReference<Bitmap>>();

        dm = getContext().getResources().getDisplayMetrics();
        scale = dm.densityDpi / 160;
        Bitmap front_waves_img = loadBitmap(R.drawable.charge_front_waves_img_1, "charge_front_waves_img_1");
        backWavesImgWidth = frontWavesImgWidth = front_waves_img.getWidth();

        frontWaveMoveMaxDistance = Math.abs(frontWavesImgWidth - getMyWidth());
        backWaveMoveMaxDistance = Math.abs(backWavesImgWidth - getMyWidth());

        frontWaveData = new WaveData(WaveData.WAVE_WHERE_OF_FRONT);
        backWaveData = new WaveData(WaveData.WAVE_WHERE_OF_BACK);

        frontWaveData.initOrReset(WaveData.MOVE_TO_RIGHT, frontWaveMoveMaxDistance, getMyWidth()
                - frontWavesImgWidth);

        backWaveData.initOrReset(WaveData.MOVE_TO_LEFT, backWaveMoveMaxDistance, 0);

        alphaPaintForGreen = new Paint();
        isUseFirstGroupImg = true;
        isStartAnim = false;
    }

    public abstract Bitmap createMask();

    protected void onDraw(Canvas paramCanvas) {
//		if(!isStartAnim){
//			return ;
//		}
        try {
            if (this.paint == null) {
                this.paint = new Paint();
                /**
                 * 如果该项设置为true，则图像在动画进行中会滤掉对Bitmap图像的优化操作， 加快显示 速度，本设置项依赖于dither和xfermode的设置
                 */
                this.paint.setFilterBitmap(false);
                this.paint.setXfermode(MASK_XFERMODE);
            }

            float f1 = getWidth();
            float f2 = getHeight();

            int i = paramCanvas.saveLayer(0.0F, 0.0F, f1, f2, null, Canvas.ALL_SAVE_FLAG);
            if (lastProgress <= LOW_PROGRESS) {
                if (isUseFirstGroupImg) {
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_front_waves_red_img_1, "charge_front_waves_red_img_1"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, null);
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_back_waves_red_img_1, "charge_back_waves_red_img_1"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, null);
                } else {
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_front_waves_red_img_2, "charge_front_waves_red_img_2"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, null);
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_back_waves_red_img_2, "charge_back_waves_red_img_2"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, null);
                }
            } else if (LOW_PROGRESS < lastProgress && lastProgress < HIGHT_PROGRESS) {
                int tmpAlpha = ( int ) (100.0 * (lastProgress - LOW_PROGRESS) / (HIGHT_PROGRESS - LOW_PROGRESS));
                alphaPaintForGreen.setAlpha(tmpAlpha);
                if (isUseFirstGroupImg) {
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_back_waves_red_img_1, "charge_back_waves_red_img_1"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, null);
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_front_waves_red_img_1, "charge_front_waves_red_img_1"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, null);
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_back_waves_img_1, "charge_back_waves_img_1"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, alphaPaintForGreen);
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_front_waves_img_1, "charge_front_waves_img_1"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, alphaPaintForGreen);
                } else {
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_back_waves_red_img_2, "charge_back_waves_red_img_2"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, null);
                    paramCanvas.drawBitmap(
                            loadBitmap(R.drawable.charge_front_waves_red_img_2, "charge_front_waves_red_img_2"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, null);
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_back_waves_img_2, "charge_back_waves_img_2"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, alphaPaintForGreen);
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_front_waves_img_2, "charge_front_waves_img_2"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, alphaPaintForGreen);
                }
            } else if (lastProgress >= HIGHT_PROGRESS) {
                if (isUseFirstGroupImg) {
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_front_waves_img_1, "charge_front_waves_img_1"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, null);
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_back_waves_img_1, "charge_back_waves_img_1"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, null);
                } else {
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_front_waves_img_2, "charge_front_waves_img_2"),
                            frontWaveData.getCurLeftMargin(), frontWaveTopMargin, null);
                    paramCanvas.drawBitmap(loadBitmap(R.drawable.charge_back_waves_img_2, "charge_back_waves_img_2"),
                            backWaveData.getCurLeftMargin(), backWaveTopMargin, null);
                }
            }

            if ((this.mask == null) || (this.mask.isRecycled())) {
                Bitmap localBitmap1 = createMask();
                this.mask = localBitmap1;
            }

            paramCanvas.drawBitmap(mask, 0.0F, 0.0F, paint);
            paramCanvas.restoreToCount(i);
            return;
        } catch (Exception localException) {
            StringBuilder localStringBuilder = new StringBuilder()
                    .append("Attempting to draw with recycled bitmap. View ID = ");
            System.out.println("localStringBuilder==" + localStringBuilder);
        }
    }

    public void startAnimPublic() {
        startWaveAnim();
    }

    private void startWaveAnim() {
        isStartAnim = true;
        if (waveAnim == null) {
            waveAnim = new TweensAnimation(new TweensAnimCallBack() {
                public void callBack(float interpolatedTime, Transformation t) {
                    if (interpolatedTime == 1) {
                        animationEnd();
                    } else {
                        frontWaveData.computeCurLeftMargin(interpolatedTime);
                        backWaveData.computeCurLeftMargin(interpolatedTime);
                    }
                    postInvalidate();
                }
            });
            waveAnim.setDuration(getAnimationDuration());
            waveAnim.setInterpolator(new LinearInterpolator());
        }
        clearAnimation();
        startAnimation(waveAnim);
    }

    /**
     * 动画一轮执行完毕后调用的函数
     */
    private void animationEnd() {
        frontWaveData.initOrReset(WaveData.MOVE_TO_RIGHT, frontWaveMoveMaxDistance, getMyWidth()
                - frontWavesImgWidth);

        backWaveData.initOrReset(WaveData.MOVE_TO_LEFT, backWaveMoveMaxDistance, 0);

        if (isUseFirstGroupImg) {
            isUseFirstGroupImg = false;
        } else {
            isUseFirstGroupImg = true;
        }

        startWaveAnim();
    }

    /**
     * 得到动画执行的时间
     * 
     * @return
     */
    public int getAnimationDuration() {
        return 2100;
    }

    /**
     * 设置进度
     * 
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress < 0 || progress > 100 || lastProgress == progress) {
            return;
        }
        this.lastProgress = progress;
        float tmpTopMargin = getMyHeight() - 1.0f * getMyHeight() * progress / 100;
        frontWaveTopMargin = tmpTopMargin - 3 * scale;
        backWaveTopMargin = ( int ) (frontWaveTopMargin - 1.5 * scale);
        postInvalidate();
    }

    private Bitmap loadBitmap(int resId, String key) {
        SoftReference<Bitmap> softReference = imageCache.get(key);
        if (softReference != null) {
            Bitmap drawable = softReference.get();
            if (drawable != null && !drawable.isRecycled()) {
                return drawable;
            }
        }

        Bitmap bitmap = (( BitmapDrawable ) getResources().getDrawable(resId)).getBitmap();
        imageCache.put(key, new SoftReference<Bitmap>(bitmap));
        return bitmap;
    }
}
