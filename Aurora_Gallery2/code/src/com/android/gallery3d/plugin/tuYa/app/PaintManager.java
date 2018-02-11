package com.android.gallery3d.plugin.tuYa.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;

public class PaintManager {

    private static final String PAINT_CONFIG_FILE = "paint_config_file";

    private static final String PATHLINE_PAINT_SIZE_KEY = "pathline_paint_size";
    private static final String PATHLINE_PAINT_COLOR_KEY = "pathline_paint_color";

    private static final String CIRCLE_PAINT_SIZE_KEY = "circle_paint_size";
    private static final String CIRCLE_PAINT_COLOR_KEY = "circle_paint_color";

    private static final String RECTANGLE_PAINT_SIZE_KEY = "rectangle_paint_size";
    private static final String RECTANGLE_PAINT_COLOR_KEY = "rectangle_paint_color";

    private static final String ARROW_PAINT_SIZE_KEY = "arrow_paint_size";
    private static final String ARROW_PAINT_COLOR_KEY = "arrow_paint_color";

    private static final String MOSAIC_PAINT_SIZE_KEY = "mosaic_paint_size";
    private static final String MOSAIC_PAINT_BLUR_KEY = "mosaic_paint_blur";

    private static PaintManager mPaintManager;
    private int mPathLinePaintSize;
    private int mPathLinePaintColor;
    private int mCirclePaintSize;
    private int mCirclePaintColor;
    private int mArrowPaintSize;
    private int mArrowPaintColor;
    private int mRectanglePaintSize;
    private int mRectanglePaintColor;
    private int mMosaicPaintSize;
    private int mMosaicPaintBlur;

    private int mPaintSize1;
    private int mPaintSize2;
    private int mPaintSize3;
    private int mPaintRed;
    private int mPaintGreen;
    private int mPaintYellow;
    private int mPaintBlue;
    private int mPaintWhile;

    private PaintManager() {
    }

    public static PaintManager getInstance() {
        if (mPaintManager == null) {
            mPaintManager = new PaintManager();
        }
        return mPaintManager;
    }

    public void init(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PAINT_CONFIG_FILE, Context.MODE_PRIVATE);
        Resources rs = context.getResources();
        mPathLinePaintSize = preferences.getInt(PATHLINE_PAINT_SIZE_KEY,
                rs.getDimensionPixelSize(R.dimen.tu_ya_default_pathline_paint_size));
        mPathLinePaintColor = preferences.getInt(PATHLINE_PAINT_COLOR_KEY,
                rs.getColor(R.color.tu_ya_default_pathline_paint_color));

        mCirclePaintSize = preferences.getInt(CIRCLE_PAINT_SIZE_KEY,
                rs.getDimensionPixelSize(R.dimen.tu_ya_default_circle_paint_size));
        mCirclePaintColor = preferences.getInt(CIRCLE_PAINT_COLOR_KEY,
                rs.getColor(R.color.tu_ya_default_circle_paint_color));

        mArrowPaintSize = preferences.getInt(ARROW_PAINT_SIZE_KEY,
                rs.getDimensionPixelSize(R.dimen.tu_ya_default_arrow_paint_size));
        mArrowPaintColor = preferences.getInt(ARROW_PAINT_COLOR_KEY,
                rs.getColor(R.color.tu_ya_default_arrow_paint_color));

        mRectanglePaintSize = preferences.getInt(RECTANGLE_PAINT_SIZE_KEY,
                rs.getDimensionPixelSize(R.dimen.tu_ya_default_rectangle_paint_size));
        mRectanglePaintColor = preferences.getInt(RECTANGLE_PAINT_COLOR_KEY,
                rs.getColor(R.color.tu_ya_default_rectangle_paint_color));

        mMosaicPaintSize = preferences.getInt(MOSAIC_PAINT_SIZE_KEY,
                rs.getDimensionPixelSize(R.dimen.tu_ya_default_mosaic_paint_size));
        mMosaicPaintBlur = preferences.getInt(MOSAIC_PAINT_BLUR_KEY, TuYaUtils.MOSAIC_BLUR_DEFAULT_VALUE);

        initAllPaintSize(rs);
        initAllPaintColor(rs);
    }

    private void initAllPaintSize(Resources rs) {
        mPaintSize1 = rs.getDimensionPixelSize(R.dimen.tu_ya_dimen_paint_size_1);
        mPaintSize2 = rs.getDimensionPixelSize(R.dimen.tu_ya_dimen_paint_size_2);
        mPaintSize3 = rs.getDimensionPixelSize(R.dimen.tu_ya_dimen_paint_size_3);
    }

    private void initAllPaintColor(Resources rs) {
        mPaintRed = rs.getColor(R.color.tu_ya_col_paint_color_red);
        mPaintYellow = rs.getColor(R.color.tu_ya_col_paint_color_yellow);
        mPaintGreen = rs.getColor(R.color.tu_ya_col_paint_color_green);
        mPaintBlue = rs.getColor(R.color.tu_ya_col_paint_color_blue);
        mPaintWhile = rs.getColor(R.color.tu_ya_col_paint_color_while);
    }

    public int getMosaicPaintBlur() {
        return mMosaicPaintBlur;
    }

    public void updatePathLinePaint(Context context, int drawingId, int size, int color) {
        Utils.assertTrue(drawingId == R.id.tuya_id_pathline);
        boolean change = false;
        if (mPathLinePaintSize != size) {
            mPathLinePaintSize = size;
            change = true;
        }
        if (mPathLinePaintColor != color) {
            mPathLinePaintColor = color;
            change = true;
        }

        if (change) {
            updatePathLinePaintConfig(context, size, color);
        }
    }

    private void updatePathLinePaintConfig(Context context, int size, int color) {
        SharedPreferences preferences = context.getSharedPreferences(PAINT_CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PATHLINE_PAINT_SIZE_KEY, size);
        editor.putInt(PATHLINE_PAINT_COLOR_KEY, color);
        editor.commit();
    }

    public void updateCirclePaint(Context context, int drawingId, int size, int color) {
        Utils.assertTrue(drawingId == R.id.tuya_id_circle);
        boolean change = false;
        if (mCirclePaintSize != size) {
            mCirclePaintSize = size;
            change = true;
        }
        if (mCirclePaintColor != color) {
            mCirclePaintColor = color;
            change = true;
        }

        if (change) {
            updateCirclePaintConfig(context, size, color);
        }
    }

    private void updateCirclePaintConfig(Context context, int size, int color) {
        SharedPreferences preferences = context.getSharedPreferences(PAINT_CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CIRCLE_PAINT_SIZE_KEY, size);
        editor.putInt(CIRCLE_PAINT_COLOR_KEY, color);
        editor.commit();
    }

    public void updateRectanglePaint(Context context, int drawingId, int size, int color) {
        Utils.assertTrue(drawingId == R.id.tuya_id_rectangle);
        boolean change = false;
        if (mRectanglePaintSize != size) {
            mRectanglePaintSize = size;
            change = true;
        }
        if (mRectanglePaintColor != color) {
            mRectanglePaintColor = color;
            change = true;
        }

        if (change) {
            updateRectanglePaintConfig(context, size, color);
        }
    }

    private void updateRectanglePaintConfig(Context context, int size, int color) {
        SharedPreferences preferences = context.getSharedPreferences(PAINT_CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(RECTANGLE_PAINT_SIZE_KEY, size);
        editor.putInt(RECTANGLE_PAINT_COLOR_KEY, color);
        editor.commit();
    }

    public void updateArrowPaint(Context context, int drawingId, int size, int color) {
        Utils.assertTrue(drawingId == R.id.tuya_id_arrow);
        boolean change = false;
        if (mArrowPaintSize != size) {
            mArrowPaintSize = size;
            change = true;
        }
        if (mArrowPaintColor != color) {
            mArrowPaintColor = color;
            change = true;
        }

        if (change) {
            updateArrowPaintConfig(context, size, color);
        }
    }

    private void updateArrowPaintConfig(Context context, int size, int color) {
        SharedPreferences preferences = context.getSharedPreferences(PAINT_CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(ARROW_PAINT_SIZE_KEY, size);
        editor.putInt(ARROW_PAINT_COLOR_KEY, color);
        editor.commit();
    }

    public void updateMosaicPaint(Context context, int drawingId, int size, int blur) {
        blur = blur + TuYaUtils.MOSAIC_MIN_GRID_SIZE;
        Utils.assertTrue(drawingId == R.id.tuya_id_mosaic);
        boolean change = false;
        if (mMosaicPaintSize != size) {
            mMosaicPaintSize = size;
            change = true;
        }
        if (mMosaicPaintBlur != blur) {
            mMosaicPaintBlur = blur;
            change = true;
        }

        if (change) {
            updateMosaicPaintConfig(context, size, blur);
        }
    }

    private void updateMosaicPaintConfig(Context context, int size, int blur) {
        SharedPreferences preferences = context.getSharedPreferences(PAINT_CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(MOSAIC_PAINT_SIZE_KEY, size);
        editor.putInt(MOSAIC_PAINT_BLUR_KEY, blur);
        editor.commit();
    }

    public Paint getDrawingPaint(int drawingId) {
        switch (drawingId) {
            case R.id.tuya_id_pathline:
                return createPaint(mPathLinePaintSize, mPathLinePaintColor);
            case R.id.tuya_id_circle:
                return createPaint(mCirclePaintSize, mCirclePaintColor);
            case R.id.tuya_id_rectangle:
                return createPaint(mRectanglePaintSize, mRectanglePaintColor);
            case R.id.tuya_id_arrow:
                return createPaint(mArrowPaintSize, mArrowPaintColor);
            case R.id.tuya_id_mosaic:
                return createMosaicPaint(mMosaicPaintSize);
            default:
                Utils.assertTrue(false);
        }
        return null;
    }

    public int getPaintColorViewId(int paintSize) {
        if (paintSize == mPaintRed) {
            return R.id.tuya_id_paint_color_red;
        }
        if (paintSize == mPaintYellow) {
            return R.id.tuya_id_paint_color_yellow;
        }
        if (paintSize == mPaintGreen) {
            return R.id.tuya_id_paint_color_green;
        }

        if (paintSize == mPaintBlue) {
            return R.id.tuya_id_paint_color_blue;
        }

        if (paintSize == mPaintWhile) {
            return R.id.tuya_id_paint_color_while;
        }
        Utils.assertTrue(false);
        return -1;
    }

    public int getPaintSizeViewId(int paintColor) {
        if (paintColor == mPaintSize1) {
            return R.id.tuya_id_paint_size_1;
        }
        if (paintColor == mPaintSize2) {
            return R.id.tuya_id_paint_size_2;
        }
        if (paintColor == mPaintSize3) {
            return R.id.tuya_id_paint_size_3;
        }
        Utils.assertTrue(false);
        return -1;
    }

    public int getViewPaintSize(int viewId) {

        if (viewId == R.id.tuya_id_paint_size_1) {
            return mPaintSize1;
        }
        if (viewId == R.id.tuya_id_paint_size_2) {
            return mPaintSize2;
        }
        if (viewId == R.id.tuya_id_paint_size_3) {
            return mPaintSize3;
        }
        Utils.assertTrue(false);
        return -1;
    }

    public int getViewPaintColor(int viewId) {
        if (viewId == R.id.tuya_id_paint_color_red) {
            return mPaintRed;
        }
        if (viewId == R.id.tuya_id_paint_color_yellow) {
            return mPaintYellow;
        }
        if (viewId == R.id.tuya_id_paint_color_green) {
            return mPaintGreen;
        }

        if (viewId == R.id.tuya_id_paint_color_blue) {
            return mPaintBlue;
        }

        if (viewId == R.id.tuya_id_paint_color_while) {
            return mPaintWhile;
        }
        Utils.assertTrue(false);
        return -1;
    }

    private Paint createPaint(int size, int color) {
        Paint brush = new Paint();
        brush.setAntiAlias(true);
        brush.setDither(true);
        brush.setColor(color);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeCap(Paint.Cap.ROUND);
        brush.setStrokeWidth(size / 3.0f);
        return brush;
    }

    private Paint createMosaicPaint(int size) {
        Paint mosaicBrush = new Paint();
        mosaicBrush.setFlags(Paint.ANTI_ALIAS_FLAG);
        mosaicBrush.setStyle(Paint.Style.STROKE);
        mosaicBrush.setAntiAlias(true);
        mosaicBrush.setStrokeJoin(Paint.Join.ROUND);
        mosaicBrush.setStrokeCap(Paint.Cap.ROUND);
        mosaicBrush.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mosaicBrush.setStrokeWidth(size);
        return mosaicBrush;
    }

    public int getCurDrawingPaintSize(int drawingId) {
        switch (drawingId) {
            case R.id.tuya_id_pathline:
                return mPathLinePaintSize;
            case R.id.tuya_id_circle:
                return mCirclePaintSize;
            case R.id.tuya_id_rectangle:
                return mRectanglePaintSize;
            case R.id.tuya_id_arrow:
                return mArrowPaintSize;
            case R.id.tuya_id_mosaic:
                return mMosaicPaintSize;
            default:
                Utils.assertTrue(false);
                break;
        }
        return -1;
    }

    public int getCurDrawingPaintColor(int drawingId) {
        switch (drawingId) {
            case R.id.tuya_id_pathline:
                return mPathLinePaintColor;
            case R.id.tuya_id_circle:
                return mCirclePaintColor;
            case R.id.tuya_id_rectangle:
                return mRectanglePaintColor;
            case R.id.tuya_id_arrow:
                return mArrowPaintColor;
            default:
                Utils.assertTrue(false);
                break;
        }
        return -1;
    }

}
