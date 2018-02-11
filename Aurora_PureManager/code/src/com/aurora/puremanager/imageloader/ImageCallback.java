package com.aurora.puremanager.imageloader;

import android.graphics.drawable.Drawable;

public interface ImageCallback {
	public void imageLoaded(Drawable imageDrawable, Object viewTag);
}
