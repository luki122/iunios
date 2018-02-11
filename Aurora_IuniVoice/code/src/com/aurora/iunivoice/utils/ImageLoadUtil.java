package com.aurora.iunivoice.utils;

import com.aurora.iunivoice.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ImageLoadUtil {

	public static DisplayImageOptions circleUserIconOptions = new DisplayImageOptions.Builder()
	.showImageOnLoading(R.drawable.default_user_icon)
	.showImageForEmptyUri(R.drawable.default_user_icon)
	.showImageOnFail(R.drawable.default_user_icon)
	.displayer(new RoundedBitmapDisplayer(100))			// 设置较大值，直接为圆型图片输出
	.cacheInMemory(false).cacheOnDisk(false).build();
	
	
	public static DisplayImageOptions circleAngleUserIconOptions = new DisplayImageOptions.Builder() //圆角头像
	.showImageOnLoading(R.drawable.default_user_icon)
	.showImageForEmptyUri(R.drawable.default_user_icon)
	.showImageOnFail(R.drawable.default_user_icon)
	.displayer(new RoundedBitmapDisplayer(20))			// 设置较大值，直接为圆型图片输出
	.cacheInMemory(true).cacheOnDisk(true).build();
	
}
