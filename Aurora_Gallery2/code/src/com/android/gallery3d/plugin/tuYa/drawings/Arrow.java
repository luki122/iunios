package com.android.gallery3d.plugin.tuYa.drawings;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class Arrow extends Drawing {

    public Arrow(Paint paint) {
        super(paint);
    }

    private static final double ARROW_H = 20;
    private static final double ARROW_L = 20;

    @Override
    public void apply(Canvas canvas) {
        drawAL(mStartX, mStartY, mEndX, mEndY, canvas);
    }

    /*
     * 
     *                        (x3,y3) 
     *     (x1,y1) ------------->(x2,y2)
     *                        (x4,y4)  
     * 
     */
    private void drawAL(float x1, float y1, float x2, float y2, Canvas canvas) {
        double arrowHeight = ARROW_H;
        double arrowHalfWidth = ARROW_L / 2;
        float x3 = 0;
        float y3 = 0;
        float x4 = 0;
        float y4 = 0;
        double awrad = Math.atan(arrowHalfWidth / arrowHeight);
        double arraow_len = Math.sqrt(arrowHalfWidth * arrowHalfWidth + arrowHeight * arrowHeight);
        double[] arrXY_1 = rotateVec(x2 - x1, y2 - y1, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(x2 - x1, y2 - y1, -awrad, true, arraow_len);
        x3 = (float) (x2 - arrXY_1[0]);
        y3 = (float) (y2 - arrXY_1[1]);
        x4 = (float) (x2 - arrXY_2[0]);
        y4 = (float) (y2 - arrXY_2[1]);

        canvas.drawLine(x1, y1, x2, y2, mPaint);
        Path triangle = new Path();
        triangle.moveTo(x3, y3);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x4, y4);
        canvas.drawPath(triangle, mPaint);

    }

    private double[] rotateVec(float px, float py, double ang, boolean isChLen, double newLen) {
        double mathstr[] = new double[2];
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }

}
