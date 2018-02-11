package com.android.gallery3d.xcloudalbum.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.xcloudalbum.fragment.CloudMainFragment;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader.ImageProcessingCallback;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.android.gallery3d.R;

public class CloudMainAdapter extends BaseAdapter {
	private List<CommonFileInfo> fileInfos = new ArrayList<CommonFileInfo>();
	private CloudMainFragment fragment;
	private static Bitmap icon_cache,icon_noWifi;
	private static Bitmap add_icon;
	private LayoutInflater mInflater;
	private ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap;
	private ImageLoader imageLoader;
	private static final String TAG = "CloudMainAdapter";
	private int albumNameLength = 350;

	public CloudMainAdapter(List<CommonFileInfo> fileInfos,
			CloudMainFragment fragment) {
		super();
		if (fileInfos != null) {
			this.fileInfos = fileInfos;
		}
		this.fragment = fragment;
		mInflater = LayoutInflater.from(fragment.getCloudActivity());
		imageLoader = ImageLoader.getInstance(fragment.getCloudActivity()
				.getApplicationContext());

		if (icon_cache == null) {
			icon_cache = BitmapFactory.decodeResource(fragment.getResources(),
					//wenyongzhe 2015.23
					R.drawable.aurora_album_no_photo);
		}
		if (add_icon == null) {
			add_icon = BitmapFactory.decodeResource(fragment.getResources(),
					//wenyongzhe 2015.23
					R.drawable.aurora_add_new_album_bg);
		}
		if(icon_noWifi==null){//aurora_nowifi
			icon_noWifi = BitmapFactory.decodeResource(fragment.getResources(),
					R.drawable.aurora_nowifi);
		}
		albumNameLength = (int) fragment.getResources().getDimension(
				R.dimen.aurora_album_name_length);
	}

	public void setFileInfos(List<CommonFileInfo> fileInfos) {
		if (fileInfos == null) {
			this.fileInfos.clear();
		} else {
			this.fileInfos = fileInfos;
		}
	}

	public void setConcurrentHashMap(
			ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap) {
		this.concurrentHashMap = concurrentHashMap;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		CloudMainViewCache viewCache = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.aurora_cloud_album_browse,
					null);
			viewCache = new CloudMainViewCache(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (CloudMainViewCache) convertView.getTag();
		}

		if (position == 0) {
			viewCache.getRelativeLayoutTitle().setVisibility(View.GONE);
			viewCache.getCreateAlbumText().setVisibility(View.VISIBLE);
		} else {
			viewCache.getRelativeLayoutTitle().setVisibility(View.VISIBLE);
			viewCache.getCreateAlbumText().setVisibility(View.GONE);
		}

		// add header view
		if (position == 0 || position == 1) {
			viewCache.getHeadView().setVisibility(View.VISIBLE);
		} else {
			viewCache.getHeadView().setVisibility(View.GONE);
		}

		// set left right padding
		if (position % 2 != 0) {
			LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			Resources res = fragment.getResources();
			lParams.rightMargin = res
					.getDimensionPixelSize(R.dimen.aurora_album_margin);
			lParams.leftMargin = res
					.getDimensionPixelSize(R.dimen.aurora_album_margin_2);
			viewCache.getLayout().setLayoutParams(lParams);
		} else {
			LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			Resources res = fragment.getResources();
			lParams.leftMargin = res
					.getDimensionPixelSize(R.dimen.aurora_album_margin);
			lParams.rightMargin = res
					.getDimensionPixelSize(R.dimen.aurora_album_margin_2);
			viewCache.getLayout().setLayoutParams(lParams);
		}

		final CommonFileInfo fileInfo = (CommonFileInfo) getItem(position);
		if (fileInfo != null) {
			if (position == 0) {
				viewCache.getAlbumImage().setImageBitmap(add_icon);
				viewCache.getAlbumImage().setScaleType(ScaleType.FIT_XY);//wenyongzhe 2015.11.17
				viewCache.getAlbumAmount().setText("");
			} else {
				viewCache.getAlbumImage().setScaleType(ScaleType.CENTER_CROP);//wenyongzhe 2015.11.17
				//wenyongzhe 2015.10.9
				if(!NetworkUtil.checkNetwork(fragment.getCloudActivity())){
					viewCache.getAlbumImage().setImageBitmap(icon_noWifi);
				}else {
					viewCache.getAlbumImage().setImageBitmap(icon_cache);
				}
				viewCache.getAlbumAmount().setText("0");
			}

			//wenyongzhe 2015.9.24 modify start
//			if (!TextUtils.isEmpty(fileInfo.path)
//					&& (fileInfo.path.endsWith(fragment
//							.getString(R.string.aurora_system_screenshot)) || fileInfo.path
//							.endsWith(fragment.getString(R.string.aurora_album)))) {
//				viewCache.getAlbumSubtitle().setText(
//						fragment.getString(R.string.aurora_system_album));
//			} else {
				viewCache.getAlbumSubtitle().setText("");
//			}
			//wenyongzhe 2015.9.24 modify end
				
			if (concurrentHashMap != null && position != 0) {
				List<CommonFileInfo> temp = concurrentHashMap.get(fileInfo);
				if (temp != null) {
					int size = temp.size();
					viewCache.getAlbumAmount().setText(size + "");
					if (size > 0) {
						final ImageView imageView = viewCache.getAlbumImage();
						final CommonFileInfo tempInfo = temp.get(0);
						imageView.setTag(tempInfo.blockList);
						imageLoader.displayImage(tempInfo.path,
								tempInfo.blockList,
								new ImageProcessingCallback() {

									@Override
									public void onImageProcessing(
											WeakReference<Bitmap> weak,
											String tag) {
										if (((String) imageView.getTag())
												.equals(tag)) {//
											Bitmap bitmap = weak.get();
											if (bitmap == null
													|| bitmap.isRecycled()) {
												if(!Utils.isConnect(fragment.getCloudActivity())){
													imageView.setImageBitmap(icon_noWifi);
												}
												return;
											}
											if(position==0){
												imageView.setImageBitmap(add_icon);
												return;
											}
											imageView.setImageBitmap(bitmap);
										}
									}
								});
					}
				}
			}

			String name = Utils.getPathNameFromPath(fileInfo.path);
			CharSequence foldName = Utils.getEllipsizeEnd(name,
					viewCache.getAlbumTitle(), albumNameLength);
			viewCache.getAlbumTitle().setText(foldName);
			// LogUtil.d(TAG,
			// "Operation::" + fragment.isOperationFile() + " select::"
			// + fragment.getSelectPosition() + " position::"
			// + position + " anim::" + fragment.isItemPicAnim());

			//wenyongzhe
//			if (fragment.isOperationFile() && (position != 0 && position != 1 && position != 2)) {
			if (fragment.isOperationFile() && (position != 0 && position != 1)) {
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
				} else {
					viewCache.getCheckboxOk().setVisibility(View.GONE);
					viewCache.getCheckboxNo().setVisibility(View.VISIBLE);
					if (fragment.getSelectPosition() == position
							&& fragment.isItemPicAnim()) {
						fragment.imageViewAnim(viewCache.getCheckboxNo(), false);
					} else {
						viewCache.getCheckboxNo().setImageResource(
								R.drawable.aurora_check00);
					}
				}
			} else {
				viewCache.getCheckboxOk().setVisibility(View.GONE);
				viewCache.getCheckboxNo().setVisibility(View.GONE);
			}

		}

		return convertView;
	}

}
