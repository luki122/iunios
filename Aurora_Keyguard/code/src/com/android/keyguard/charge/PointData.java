package com.android.keyguard.charge;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.DisplayMetrics;

public class PointData {
    // 外部属性，记录该点所在圆的属性
    private PointF centerCircle;
    private float radius;
    private double curRadian;
    private double middleRadian;
    private double beginRadian;
    private double endRadian;

    // PointView 的内部属性
    private int POINT_MAX_SIZE;
    private int POINT_MIN_SIZE;
    private float pointCurSize;

    private final int MAX_ALPHA = 100;
    private final int MIN_ALPHA = 0;
    private int alpha;
    private Paint paint;
    private boolean isMove;
    private double lastUpdateRadianTime = 0;
    private double STANDARD_SPEED;
    public static float speedScale = 1;
    private double speed;

    public PointData(DisplayMetrics dm, PointF centerCircle, float radius, double beginRadian,
            double endRadian) {
        POINT_MAX_SIZE = dm.densityDpi / 160 * 8;
        POINT_MIN_SIZE = dm.densityDpi / 160 * 2;
        STANDARD_SPEED = 0.9 * (endRadian - beginRadian) / 3000;

        this.centerCircle = centerCircle;
        this.radius = radius;
        this.beginRadian = beginRadian;
        this.endRadian = endRadian;
        this.middleRadian = (beginRadian + endRadian) * 0.5;
        this.curRadian = beginRadian;
        isMove = false;
        lastUpdateRadianTime = 0;
        updateAlpha();
        updateSize();
        updateSpeed();
    }

    public void reset() {
        this.curRadian = beginRadian;
        isMove = false;
        lastUpdateRadianTime = 0;
        updateAlpha();
        updateSize();
        updateSpeed();
    }

    /**
     * 跟新当前角度
     * 
     * @param updateRadianTime
     */
    public void updateCurRadian(float updateRadianTime) {
        if (!isMove) {
            curRadian = beginRadian;
        } else {
            setCurRadian(curRadian + speed * (updateRadianTime - lastUpdateRadianTime));
        }
        lastUpdateRadianTime = updateRadianTime;
    }

    private void setCurRadian(double tmpCurRadian) {
        if (endRadian > beginRadian) {
            if (tmpCurRadian >= endRadian) {
                this.curRadian = beginRadian;
                isMove = false;
            } else {
                this.curRadian = tmpCurRadian;
            }
        } else {
            if (tmpCurRadian <= endRadian) {
                this.curRadian = beginRadian;
                isMove = false;
            } else {
                this.curRadian = tmpCurRadian;
            }
        }

        updateAlpha();
        updateSize();
        updateSpeed();
    }

    private void updateAlpha() {
        alpha = MAX_ALPHA
                - Math.abs(( int ) ((MAX_ALPHA - MIN_ALPHA) * (curRadian - middleRadian) / (endRadian - middleRadian)));
    }

    /**
     * 更新point的直径
     */
    private void updateSize() {
        int tmpSize = Math
                .abs(( int ) ((POINT_MAX_SIZE - POINT_MIN_SIZE) * (curRadian - beginRadian) / (endRadian - beginRadian)));
        pointCurSize = POINT_MAX_SIZE - tmpSize;
    }

    private void updateSpeed() {
        speed = STANDARD_SPEED * (speedScale * 0.4 + speedScale * 0.6 * Math.abs(curRadian - middleRadian));
    }

    public void onDraw(Canvas canvas) {
        if (isMove) {
            if (paint == null) {
                paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.parseColor("#00fefe"));
            }
            paint.setAlpha(alpha);
            canvas.drawCircle(( float ) getCenterX(), ( float ) getCenterY(), pointCurSize / 2.0f, paint);
        }
    }

    private double getCenterX() {
        return centerCircle.x + radius * Math.sin(curRadian);
    }

    private double getCenterY() {
        return centerCircle.y - radius * Math.cos(curRadian);
    }

    public double getCurRadian() {
        return this.curRadian;
    }

    public double getBeginRadian() {
        return this.beginRadian;
    }

    public double getEndRadian() {
        return this.endRadian;
    }

    public void setIsMove(boolean isMove) {
        this.isMove = isMove;
    }

    public boolean getIsMove() {
        return this.isMove;
    }
}
