package com.android.gallery3d.plugin.tuYa.app;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.android.gallery3d.plugin.tuYa.drawings.Drawing;
import com.android.gallery3d.plugin.tuYa.drawings.Mosaic;

import java.util.ArrayList;
import java.util.Iterator;


public class DrawingManager {
    private ArrayList<Drawing> mDrawings;
    private ArrayList<Drawing> mReDoDrawings;
    private int mShowW;
    private int mShowH;
    private DrawingChangeListener mListener;

    public interface DrawingChangeListener {
        public void onDrawingSizeChange(int drawingSize, int redoDrawingSize);
    }

    public DrawingManager() {
        mDrawings = new ArrayList<>();
        mReDoDrawings = new ArrayList<>();
    }

    public void setDrawingChangeListener(DrawingChangeListener listener) {
        mListener = listener;
    }

    private void notifyDrawingChange() {
        if (mListener != null) {
            mListener.onDrawingSizeChange(mDrawings.size(), mReDoDrawings.size());
        }
    }

    public void undo(Canvas canvas) {
        int size = mDrawings.size();
        mReDoDrawings.add(mDrawings.remove(size - 1));
        applayAllDrawing(canvas);
        notifyDrawingChange();
    }

    public void redo(Canvas canvas) {
        int size = mReDoDrawings.size();
        if (size < 1) {
            return;
        }
        mDrawings.add(mReDoDrawings.remove(size - 1));
        applayAllDrawing(canvas);
        notifyDrawingChange();
    }

    private void applayAllDrawing(Canvas canvas) {
        Iterator<Drawing> iter = mDrawings.iterator();
        Drawing temp;
        while (iter.hasNext()) {
            temp = iter.next();
            temp.apply(canvas);
        }
    }

    public void setShowSize(int w, int h) {
        mShowW = w;
        mShowH = h;
    }

    public void push(Drawing drawing) {
        synchronized (mDrawings) {
            mDrawings.add(drawing);
            mReDoDrawings.clear();
            notifyDrawingChange();
        }
    }

    public boolean hasModifications() {
        return mDrawings.size() > 0;
    }

    public Bitmap apply(Bitmap bitmap) {
        float scale = Math.min(((float) bitmap.getWidth()) / mShowW, ((float) bitmap.getHeight()) / mShowH);
        if (hasStateClass(Mosaic.class)) {
            Bitmap tuYaBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(tuYaBitmap);
            Paint paint = new Paint(Paint.DITHER_FLAG);
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            applyDrawings(scale, canvas);

            Canvas c = new Canvas(bitmap);
            TuYaUtils.drawMosaic(c, bitmap, bitmap.getWidth(), bitmap.getHeight(),
                    (int) Math.ceil(PaintManager.getInstance().getMosaicPaintBlur() * scale));
            c.drawBitmap(tuYaBitmap, 0, 0, paint);

            tuYaBitmap.recycle();

        } else {
            Canvas canvas = new Canvas(bitmap);
            applyDrawings(scale, canvas);
        }
        return bitmap;
    }

    private void applyDrawings(float scale, Canvas canvas) {
        canvas.save();
        canvas.scale(scale, scale);
        synchronized (mDrawings) {
            for (Drawing drawing : mDrawings) {
                drawing.apply(canvas);
            }
        }
        canvas.restore();
    }

    public void clear() {
        synchronized (mDrawings) {
            mDrawings.clear();
            mReDoDrawings.clear();
            notifyDrawingChange();
        }
    }

    private boolean hasStateClass(Class<? extends Drawing> klass) {
        synchronized (mDrawings) {
            for (Drawing drawing : mDrawings) {
                if (klass.isInstance(drawing)) {
                    return true;
                }
            }
        }
        return false;
    }
}
