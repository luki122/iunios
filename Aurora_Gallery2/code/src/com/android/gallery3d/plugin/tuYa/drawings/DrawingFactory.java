package com.android.gallery3d.plugin.tuYa.drawings;

import android.graphics.Paint;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.plugin.tuYa.app.PaintManager;


public class DrawingFactory {

    public static Drawing createDrawing(int id) {
        Paint paint = PaintManager.getInstance().getDrawingPaint(id);
        switch (id) {
            case R.id.tuya_id_pathline:
                return new PathLine(paint);
            case R.id.tuya_id_circle:
                return new Circle(paint);
            case R.id.tuya_id_rectangle:
                return new Rectangle(paint);
            case R.id.tuya_id_arrow:
                return new Arrow(paint);
            case R.id.tuya_id_mosaic:
                return new Mosaic(paint);
            default:
                Utils.assertTrue(false);
        }
        return null;
    }
}
