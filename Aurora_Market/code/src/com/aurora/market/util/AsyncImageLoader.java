package com.aurora.market.util;

/**
 * 图片加载工具类
 * 
 * @author 张伟
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.aurora.market.R;
import com.aurora.market.activity.module.SpecialActivity;

public class AsyncImageLoader {
	private static final String TAG = "AsyncImageLoader";
	private HashMap<String, SoftReference<Bitmap>> imageCache;
	private Context m_context;

	public AsyncImageLoader(Context context) {
		this.m_context = context;
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	public void setController() {
		// m_context= null;
	}

	public Bitmap loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {

		if (imageCache.containsKey(imageUrl)) {

			SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
			Bitmap drawable = softReference.get();

			if (drawable != null) {

				return drawable;

			}
		}

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded(((Bitmap) message.obj), imageUrl);

			}
		};

		new Thread() {

			@Override
			public void run() {

				Bitmap drawable = loadImageFromUrl(imageUrl);

				if(null != drawable)
				{
				/*	Bitmap tmp2 = BitmapUtil.scaleBitmap(
							drawable,
							BitmapUtil.getScreenWidth((SpecialActivity) m_context),
							m_context.getResources().getDimensionPixelOffset(
									R.dimen.special_item_image_height));
	
					Bitmap bit = BitmapUtil.cropBitmap(
							tmp2,
							tmp2.getWidth(),
							m_context.getResources().getDimensionPixelOffset(
									R.dimen.special_item_tip_text_height), true);
	
					Bitmap tmp1 = Blur.fastblur(m_context, bit, 20);
	
					Bitmap bit1 = BitmapUtil.combineBitmap1(tmp2, tmp1);*/

	 Bitmap bit = BitmapUtil.cropBitmap(
							drawable,
							drawable.getWidth(),
							drawable.getHeight()*m_context.getResources().getDimensionPixelOffset(
									R.dimen.special_item_tip_text_height)/ m_context.getResources().getDimensionPixelOffset(
									R.dimen.special_item_image_height), true);
	
					//Bitmap tmp1 = Blur.fastblur(m_context, bit, 10);
	
					Bitmap bit1 = BitmapUtil.combineBitmap1(drawable, bit);

					imageCache.put(imageUrl, new SoftReference<Bitmap>(bit1));
					Message message = handler.obtainMessage(0, bit1);
					handler.sendMessage(message);
				}
				else
				{
					imageCache.put(imageUrl, new SoftReference<Bitmap>(drawable));
					Message message = handler.obtainMessage(0, drawable);
					handler.sendMessage(message);
				}
			}

		}.start();

		return null;
	}

	public static Bitmap loadImageFromUrl(String url) {
		URL m;
		InputStream i = null;
		try {
			m = new URL(url);
			i = (InputStream) m.getContent();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Drawable d = Drawable.createFromStream(i, "src");
		BitmapDrawable bd = (BitmapDrawable) d;
		if(null != bd)
			return bd.getBitmap();
		else 
			return null;
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageDrawable, String imageUrl);
	}

}