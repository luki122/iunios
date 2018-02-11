package com.aurora.reminder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;

public class ReminderUtils {

	public static final Uri CONTENT_URI = Uri.parse("content://com.aurora.reminder");
	public static final String TABLE_NAME = "reminder_data";

	public static final String ID = "_id";
	public static final String TITLE = "title";
	public static final String ACTION = "action";
	public static final String PACKAGE = "package";
	public static final String LEVEL = "level";
	public static final String VISIBLE = "visible";

	public static final String PACKAGE_OWN = "com.aurora.reminder";
	public static final String PACKAGE_PHONE = "com.android.phone";
	public static final String PACKAGE_WEATHER = "com.aurora.weatherforecast";
	public static final String PACKAGE_CALENDAR = "com.android.calendar";

	public static Bitmap zoomBitmap(Bitmap bitmap, float scale) {
		Bitmap newBitmap = null;
		if (bitmap != null) {
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			newBitmap = Bitmap.createBitmap(bitmap, 0, 0, 
					bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		}
        return newBitmap;
    }

	public static Bitmap createBitmap(int width, int height, int color, int radius) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawRect(new Rect(0, 0, width, height), paint);

		return roundCorners(bitmap, radius);
	}

	public static Bitmap roundCorners(Bitmap source, float radius) {
        int width = source.getWidth();
        int height = source.getHeight();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(android.graphics.Color.WHITE);

        Bitmap clipped = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(clipped);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
        // 遮住下圆角
        // canvas.drawRect(new Rect(0, height/2, width, height), paint);

        paint.setXfermode(new PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);

        source.recycle();

        return clipped;
    }

}
