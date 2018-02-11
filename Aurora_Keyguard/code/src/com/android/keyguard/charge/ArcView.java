package com.android.keyguard.charge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

public class ArcView extends ImageView {
    private Animation anim = null;
    private double pointCanMoveDis = 0.5 * Math.PI / 4;
    private int curAnimTimes = 0;
    private DisplayMetrics dm;
    private int scale;
    private float radius;
    private PointF fatherViewCenter;
    private List<PointData> firstPointList;
    private List<PointData> secondPointList;
    private List<PointData> thirdPointList;
    private List<PointData> fourthPointList;
    private boolean isDuringAnim;
    private boolean isDuringStopAnim;

    public ArcView(Context context) {
        super(context);
        initViews();
    }

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    private void initViews() {
        dm = getContext().getResources().getDisplayMetrics();
        scale = dm.densityDpi / 160;
        radius = ( float ) (73 * scale);
        fatherViewCenter = new PointF(( float ) (180 / 2) * scale, ( float ) (180 / 2) * scale);
        isDuringAnim = false;
        isDuringStopAnim = false;
        initFirstArc();
        initSecondArc();
        initThirdArc();
        initFourthArc();
    }

    private void initFirstArc() {
        firstPointList = new ArrayList<PointData>();
        for (int i = 0; i < 14; i++) {
            PointF centerCircle = new PointF(( float ) (fatherViewCenter.x + 12.3 * scale),
                    ( float ) (fatherViewCenter.y + 18.6 * scale));
            PointData pointData = new PointData(dm, centerCircle, radius, 139.0 / 180 * Math.PI,// 116.0/180*Math.PI,
                    -23.0 / 180 * Math.PI);
            firstPointList.add(pointData);
        }
    }

    private void initSecondArc() {
        secondPointList = new ArrayList<PointData>();
        for (int i = 0; i < 14; i++) {
            PointF centerCircle = new PointF(( float ) (fatherViewCenter.x + 18.6 * scale),
                    ( float ) (fatherViewCenter.y - 12.3 * scale));
            PointData pointData = new PointData(dm, centerCircle, radius, 46.0 / 180 * Math.PI,// 23.0/180*Math.PI,
                    -116.0 / 180 * Math.PI);
            secondPointList.add(pointData);
        }
    }

    private void initThirdArc() {
        thirdPointList = new ArrayList<PointData>();
        for (int i = 0; i < 14; i++) {
            PointF centerCircle = new PointF(( float ) (fatherViewCenter.x - 12.3 * scale),
                    ( float ) (fatherViewCenter.y - 18.6 * scale));
            PointData pointData = new PointData(dm, centerCircle, radius, -23.0 / 180 * Math.PI,// -46.0/180*Math.PI,
                    -203.0 / 180 * Math.PI);
            thirdPointList.add(pointData);
        }
    }

    private void initFourthArc() {
        fourthPointList = new ArrayList<PointData>();
        for (int i = 0; i < 14; i++) {
            PointF centerCircle = new PointF(( float ) (fatherViewCenter.x - 18.6 * scale),
                    ( float ) (fatherViewCenter.y + 12.3 * scale));
            PointData pointData = new PointData(dm, centerCircle, radius, -134.0 / 180 * Math.PI,// -157.0/180*Math.PI,
                    -293.0 / 180 * Math.PI);
            fourthPointList.add(pointData);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPointList(canvas, firstPointList);
        drawPointList(canvas, secondPointList);
        drawPointList(canvas, thirdPointList);
        drawPointList(canvas, fourthPointList);
        super.onDraw(canvas);
    }

    private void drawPointList(Canvas canvas, List<PointData> pointList) {
        if (pointList != null) {
            PointData pointData = null;
            for (int i = 0; i < pointList.size(); i++) {
                pointData = pointList.get(i);
                pointData.onDraw(canvas);
            }
        }
    }

    public void startAnimPublic() {
        if (isDuringAnim || isDuringStopAnim) {
            return;
        }
        Log.i("huangbin", "startAnimPublic isDuringAnim=" + isDuringAnim + ",isDuringStopAnim="
                + isDuringStopAnim);
        reset();
        isDuringAnim = true;
        startAnim();
    }

    private void startAnim() {
        if (anim == null) {
            anim = new TweensAnimation(new TweensAnimCallBack() {
                public void callBack(float interpolatedTime, Transformation t) {
                    updateAllPointData(interpolatedTime);
                    postInvalidate();

                    if (isDuringStopAnim) {
                        boolean isDuringAnimForFirst = isPointListDuringAnim(firstPointList);
                        boolean isDuringAnimForSecond = isPointListDuringAnim(secondPointList);
                        boolean isDuringAnimForThird = isPointListDuringAnim(thirdPointList);
                        boolean isDuringAnimForFourth = isPointListDuringAnim(fourthPointList);
                        if (!isDuringAnimForFirst && !isDuringAnimForSecond && !isDuringAnimForThird
                                && !isDuringAnimForFourth) {
                            reset();
                            return;
                        }
                    }

                    if (interpolatedTime == 1) {
                        animationEnd();
                    }
                }
            });
            anim.setDuration(getAnimationDuration());
            anim.setInterpolator(new LinearInterpolator());
        }
        startAnimation(anim);
    }

    /**
     * 缓存收起动画，例如充电达到百分之百
     */
    public void endAnim() {
        if (!isDuringStopAnim) {
            changePointMoveSpeedBeginTimeSlowToFast = 0;
        }
        isDuringStopAnim = true;

    }

    /**
     * 立刻停止动画，比如屏幕黑屏
     */
    public void endAnimImmediately() {
        reset();
    }

    private void reset() {
        clearAnimation();
        resetPointList(firstPointList);
        resetPointList(secondPointList);
        resetPointList(thirdPointList);
        resetPointList(fourthPointList);
        PointData.speedScale = MIN_SPEED_SCALE;
        curAnimTimes = 0;
        isDuringStopAnim = false;
        isDuringAnim = false;
    }

    private void updateAllPointData(float interpolatedTime) {
        if (isDuringStopAnim) {
            changePointMoveSpeedOfSlowToFast();
        } else {
            if (curAnimTimes == 0) {
                if (interpolatedTime == 0) {
                    changePointMoveSpeedBeginTimeFastToSlow = 0;
                }
                changePointMoveSpeedOfFastToSlow();
            }
        }

        updateOneArc(firstPointList, interpolatedTime);
        updateOneArc(secondPointList, interpolatedTime);
        updateOneArc(thirdPointList, interpolatedTime);
        updateOneArc(fourthPointList, interpolatedTime);
    }

    private void updateOneArc(List<PointData> pointList, float interpolatedTime) {
        if (pointList != null) {
            for (int i = 0; i < pointList.size(); i++) {
                PointData pointData = pointList.get(i);
                if (i == 0 && curAnimTimes == 0) {
                    pointData.setIsMove(true);
                } else if (!isDuringStopAnim && !pointData.getIsMove()) {
                    PointData lastPointData;
                    if (i == 0) {
                        lastPointData = pointList.get(pointList.size() - 1);
                    } else {
                        lastPointData = pointList.get(i - 1);
                    }
                    if (Math.abs(pointData.getCurRadian() - lastPointData.getCurRadian()) >= pointCanMoveDis) {
                        pointData.setIsMove(true);
                    }
                }
                pointData.updateCurRadian((curAnimTimes + interpolatedTime) * getAnimationDuration());
            }

            Collections.sort(pointList, new Comparator<PointData>() {
                public int compare(PointData s1, PointData s2) {
                    double s1Move = Math.abs(s1.getCurRadian() - s1.getBeginRadian());
                    double s2Move = Math.abs(s2.getCurRadian() - s2.getBeginRadian());
                    if (s1Move > s2Move) {
                        return -1;
                    } else if (s1Move == s2Move) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    /**
     * 动画一轮执行完毕后调用的函数
     */
    private void animationEnd() {
        curAnimTimes++;
        startAnim();
    }

    private boolean isPointListDuringAnim(List<PointData> pointList) {
        if (pointList != null) {
            for (int i = 0; i < pointList.size(); i++) {
                PointData pointData = pointList.get(i);
                if (pointData.getIsMove()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void resetPointList(List<PointData> pointList) {
        if (pointList != null) {
            for (int i = 0; i < pointList.size(); i++) {
                PointData pointData = pointList.get(i);
                pointData.reset();
            }
        }
    }

    /**
     * 得到动画执行的时间
     * 
     * @return
     */
    public int getAnimationDuration() {
        return 3000;
    }

    /******************* 调整point运行的速度 **********************/
    private final float MAX_SPEED_SCALE = 4.0f;
    private final float MIN_SPEED_SCALE = 1.0f;
    private final float MAX_SPEED_DURING_TIME = 450.0f;// 单位毫秒
    private final float CHANGE_SPEED_DURING_TIME = 750.0f;// 单位毫秒
    private long changePointMoveSpeedBeginTimeFastToSlow = 0;
    private long changePointMoveSpeedBeginTimeSlowToFast = 0;

    private void changePointMoveSpeedOfFastToSlow() {
        long currentTimeMillis = System.currentTimeMillis();
        if (changePointMoveSpeedBeginTimeFastToSlow == 0) {
            changePointMoveSpeedBeginTimeFastToSlow = currentTimeMillis;
            PointData.speedScale = MIN_SPEED_SCALE;
        }
        if (currentTimeMillis - changePointMoveSpeedBeginTimeFastToSlow < MAX_SPEED_DURING_TIME) {
            PointData.speedScale = MAX_SPEED_SCALE;
        } else if (currentTimeMillis - changePointMoveSpeedBeginTimeFastToSlow < MAX_SPEED_DURING_TIME
                + CHANGE_SPEED_DURING_TIME) {
            PointData.speedScale = MAX_SPEED_SCALE
                    - (currentTimeMillis - changePointMoveSpeedBeginTimeFastToSlow - MAX_SPEED_DURING_TIME)
                    * (MAX_SPEED_SCALE - MIN_SPEED_SCALE) / CHANGE_SPEED_DURING_TIME;
        } else {
            PointData.speedScale = MIN_SPEED_SCALE;
        }
        checkPointMoveSpeedScale();
    }

    private void changePointMoveSpeedOfSlowToFast() {
        long currentTimeMillis = System.currentTimeMillis();
        if (changePointMoveSpeedBeginTimeSlowToFast == 0) {
            changePointMoveSpeedBeginTimeSlowToFast = currentTimeMillis;
            PointData.speedScale = MIN_SPEED_SCALE;
        }

        if (currentTimeMillis - changePointMoveSpeedBeginTimeSlowToFast < CHANGE_SPEED_DURING_TIME) {
            PointData.speedScale = MIN_SPEED_SCALE
                    + (currentTimeMillis - changePointMoveSpeedBeginTimeSlowToFast)
                    * (MAX_SPEED_SCALE - MIN_SPEED_SCALE) / CHANGE_SPEED_DURING_TIME;
        } else {
            PointData.speedScale = MAX_SPEED_SCALE;
        }
        checkPointMoveSpeedScale();
    }

    private void checkPointMoveSpeedScale() {
        if (PointData.speedScale < MIN_SPEED_SCALE) {
            PointData.speedScale = MIN_SPEED_SCALE;
        } else if (PointData.speedScale > MAX_SPEED_SCALE) {
            PointData.speedScale = MAX_SPEED_SCALE;
        }
    }
}
