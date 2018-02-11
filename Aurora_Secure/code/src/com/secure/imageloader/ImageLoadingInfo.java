package com.secure.imageloader;

import java.util.concurrent.locks.ReentrantLock;
import android.os.Handler;
import android.view.View;

final class ImageLoadingInfo {
	Object imgUrl;
	Object memoryCacheKey;
	Object viewTag;
	ReentrantLock loadFromUriLock;
	ImageCallback imageCallback;
	Handler handler;
	View view;
	
	public ImageLoadingInfo(
			View view,
			Object viewTag,
			Object imgUrl,
			Object memoryCacheKey,		
			ImageCallback imageCallback,
			Handler handler,
			ReentrantLock loadFromUriLock
			) {
		this.imgUrl = imgUrl;
		this.viewTag = viewTag;
		this.loadFromUriLock = loadFromUriLock;
		this.memoryCacheKey = memoryCacheKey;
		this.imageCallback = imageCallback;
		this.handler = handler;
		this.view = view;
	}
}
