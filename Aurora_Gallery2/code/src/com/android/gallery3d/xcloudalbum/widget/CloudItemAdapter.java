package com.android.gallery3d.xcloudalbum.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader.ImageProcessingCallback;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.gallery3d.R;

public class CloudItemAdapter extends BaseAdapter {
	private static final String TAG="CloudItemAdapter";

	private List<CommonFileInfo> fileInfos = new ArrayList<CommonFileInfo>();
	private static Bitmap icon_cache,icon_noWifi;
	private CloudItemFragment fragment;
	private LayoutInflater mInflater;
	private ImageLoader imageLoader;

	public CloudItemAdapter(List<CommonFileInfo> fileInfos,
			CloudItemFragment fragment) {
		super();
		this.fileInfos = fileInfos;
		this.fragment = fragment;
		mInflater = LayoutInflater.from(fragment.getCloudActivity());
		imageLoader = ImageLoader.getInstance(fragment.getCloudActivity()
				.getApplicationContext());
		if (icon_cache == null) {
			icon_cache = BitmapFactory.decodeResource(fragment.getResources(),
					R.drawable.empty_photo);
		}
		if(icon_noWifi==null){//aurora_nowifi
			icon_noWifi = BitmapFactory.decodeResource(fragment.getResources(),
					R.drawable.aurora_nowifi);
		}
	}

	public void setFileInfos(List<CommonFileInfo> fileInfos) {
		if (fileInfos == null) {
			this.fileInfos.clear();
		} else {
			this.fileInfos = fileInfos;
		}
	}

	@Override
	public int getCount() {
		return fileInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return fileInfos.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CloudItemViewCache viewCache = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.aurora_cloud_item_browse,
					null);
			viewCache = new CloudItemViewCache(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (CloudItemViewCache) convertView.getTag();
		}
		final ImageView imageView = viewCache.getAlbumImage();
		
		if(!NetworkUtil.checkNetwork(fragment.getCloudActivity())){//wenyongzhe 2015.10.26
			imageView.setImageBitmap(icon_noWifi);
		}else {
			imageView.setImageBitmap(icon_cache);
		}
		CommonFileInfo fileInfo = (CommonFileInfo) getItem(position);
		if (fileInfo != null && !TextUtils.isEmpty(fileInfo.path)) {
			imageView.setTag(fileInfo.blockList + "");
			imageLoader.displayImage(fileInfo.path, fileInfo.blockList,
					new ImageProcessingCallback() {

						@Override
						public void onImageProcessing(
								WeakReference<Bitmap> weak, String tag) {
							if (((String) imageView.getTag()).equals(tag)) {//
								Bitmap weakBitmap = weak.get();
								if (weakBitmap == null || weakBitmap.isRecycled()) {
									if(!Utils.isConnect(fragment.getCloudActivity())){
										imageView.setImageBitmap(icon_noWifi);
									}
									return;
								}
								imageView.setImageBitmap(weakBitmap);
							}
						}
					});
		}
		
		final Drawable d;//wenyongzhe 2016.3.3
		if (fragment.isOperationFile()) {
			if (fragment.getSelectImages().contains(fileInfo)) {
				viewCache.getCheckboxOk().setVisibility(View.VISIBLE);
				if (fragment.getSelectPosition() == position
						&& fragment.isItemPicAnim()) {
					fragment.imageViewAnim(viewCache.getCheckboxOk(), true);
				} else {
					viewCache.getCheckboxOk().setImageResource(
							R.drawable.aurora_check14);
				}
				viewCache.getCheckboxNo().setVisibility(View.GONE);
				d = new ColorDrawable(Color.parseColor("#4c000000"));//wenyongzhe 2016.3.3
			} else {
				viewCache.getCheckboxOk().setVisibility(View.GONE);
				viewCache.getCheckboxNo().setVisibility(View.GONE);
				d=null;//wenyongzhe 2016.3.3
				/**disable CheckboxNo  wenyongzhe 2016.2.29
				if (fragment.getSelectPosition() == position
						&& fragment.isItemPicAnim()) {
					fragment.imageViewAnim(viewCache.getCheckboxNo(), false);
				} else {
					viewCache.getCheckboxNo().setImageResource(
							R.drawable.aurora_check00);
				}
				 */
			}
		} else {
			viewCache.getCheckboxOk().setVisibility(View.GONE);
			viewCache.getCheckboxNo().setVisibility(View.GONE);
			d=null;//wenyongzhe 2016.3.3
		}
		viewCache.getFrameLayout().setForeground(d);//wenyongzhe 2016.3.3
//		LogUtil.d(TAG, "Operation::" + fragment.isOperationFile() + " select::"
//				+ fragment.getSelectPosition() + " position::" + position
//				+ " anim::" + fragment.isItemPicAnim()
//				+ " fragment.getSelectImages()::"
//				+ fragment.getSelectImages().size() + " fragment::" + fragment);
		return convertView;
	}
}
