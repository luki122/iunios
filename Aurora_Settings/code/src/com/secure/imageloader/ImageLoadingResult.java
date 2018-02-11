package com.secure.imageloader;

import android.graphics.drawable.Drawable;

final class ImageLoadingResult {
	Object viewTag;
	Drawable drawable;
	ImageCallback imageCallback;

	public ImageLoadingResult(Object viewTag,ImageCallback imageCallback,Drawable drawable) {
		this.viewTag = viewTag;
		this.imageCallback = imageCallback;
		this.drawable = drawable;
	}
}
