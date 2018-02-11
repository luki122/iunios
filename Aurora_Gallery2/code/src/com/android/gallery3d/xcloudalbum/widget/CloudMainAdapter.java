package com.android.gallery3d.xcloudalbum.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
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

import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.widget.GalleryAlbumItemView;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.util.GalleryUtils;
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
//			fileInfos.remove(0);
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

//		if (position == 0) {
//			viewCache.getRelativeLayoutTitle().setVisibility(View.GONE);
//			viewCache.getCreateAlbumText().setVisibility(View.VISIBLE);
//		} else {
//			viewCache.getRelativeLayoutTitle().setVisibility(View.VISIBLE);
//			viewCache.getCreateAlbumText().setVisibility(View.GONE);
//		}

		// set left right padding
//		if (position % 2 != 0) {
//			LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT);
//			Resources res = fragment.getResources();
//			lParams.rightMargin = res
//					.getDimensionPixelSize(R.dimen.aurora_album_margin);
//			lParams.leftMargin = res
//					.getDimensionPixelSize(R.dimen.aurora_album_margin_2);
//			viewCache.getLayout().setLayoutParams(lParams);
//		} else {
//			LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT);
//			Resources res = fragment.getResources();
//			lParams.leftMargin = res
//					.getDimensionPixelSize(R.dimen.aurora_album_margin);
//			lParams.rightMargin = res
//					.getDimensionPixelSize(R.dimen.aurora_album_margin_2);
//			viewCache.getLayout().setLayoutParams(lParams);
//		}

		final CommonFileInfo fileInfo = (CommonFileInfo) getItem(position);
		if (fileInfo != null) {
//			if (position == 0) {
//				viewCache.getAlbumImage().setImageBitmap(add_icon);
//				viewCache.getAlbumImage().setScaleType(ScaleType.FIT_XY);//wenyongzhe 2015.11.17
//				viewCache.getAlbumAmount().setText("");
//			} else {
//				viewCache.getAlbumImage().setScaleType(ScaleType.CENTER_CROP);//wenyongzhe 2015.11.17
				//wenyongzhe 2015.10.9
//				if(!NetworkUtil.checkNetwork(fragment.getCloudActivity())){
//					viewCache.getAlbumImage().setImageBitmap(icon_noWifi);
//				}else {
//					viewCache.getAlbumImage().setImageBitmap(icon_cache);
//				}
//				viewCache.getAlbumAmount().setText("0");
//			}

			//wenyongzhe 2015.9.24 modify start
//			if (!TextUtils.isEmpty(fileInfo.path)
//					&& (fileInfo.path.endsWith(fragment
//							.getString(R.string.aurora_system_screenshot)) || fileInfo.path
//							.endsWith(fragment.getString(R.string.aurora_album)))) {
//				viewCache.getAlbumSubtitle().setText(
//						fragment.getString(R.string.aurora_system_album));
//			} else {
//				viewCache.getAlbumSubtitle().setText("");
//			}
			//wenyongzhe 2015.9.24 modify end
				
//			if (concurrentHashMap != null && position != 0) {
			if (concurrentHashMap != null ) {
				List<CommonFileInfo> temp = concurrentHashMap.get(fileInfo);
				if (temp != null) {
					int size = temp.size();
					viewCache.getAlbumAmount().setText(size + "");
					clearImageView(viewCache);//wenyongzhe 2016.2.29
					if (size > 0) {
						for (int i = 0; i < size; i++) {
							ImageView imageView= getImageView(temp, i, viewCache);
							if(imageView!=null){
								loadImage(temp, i, imageView);
							}
						}
					}
				}
			}

			String name = Utils.getPathNameFromPath(fileInfo.path);
			CharSequence foldName = Utils.getEllipsizeEnd(name,
					viewCache.getAlbumTitle(), albumNameLength);
			
			//wenyongzhe 2016.2.16
			String systemAlbumName = getSystemAblumName(fragment.getActivity().getApplicationContext(), foldName+"");
			
			viewCache.getAlbumTitle().setText(systemAlbumName);
			// LogUtil.d(TAG,
			// "Operation::" + fragment.isOperationFile() + " select::"
			// + fragment.getSelectPosition() + " position::"
			// + position + " anim::" + fragment.isItemPicAnim());

			//wenyongzhe
//			if (fragment.isOperationFile() && (position != 0 && position != 1 && position != 2)) {
			if (fragment.isOperationFile() ) {//wenyongzhe2016.1.29
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
					viewCache.getCheckboxNo().setVisibility(View.GONE);
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
			}

		}

		return convertView;
	}
	
	//wenyongzhe 2016.2.16
	public static final String cameraPath ="Camera";
	public static final String screenShotsPath = "Screenshots";
	public static final String cloudPath = "cloud";
	public static final String videoPath = "videoPath";
	public static final String collectionPath = "collectionPath";
	public synchronized  static String getSystemAblumName(Context mContext, String albumName) {//wenyongzhe 2016.3.2
		String systemAlbumName = null;
		systemName: {
			if (albumName.equals(cameraPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_camera);
				break systemName;
			}
			if (albumName.equals(screenShotsPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_screenshots);
				break systemName;
			}
			if (albumName.equals(videoPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_video);
				break systemName;
			}
			if (albumName.equals(collectionPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_collection);
				break systemName;
			}
			if (albumName.equals(cloudPath)) {
				systemAlbumName = mContext.getString(R.string.cloud_download);
				break systemName;
			}
		}
		if(systemAlbumName==null){
			return albumName;
		}
		return systemAlbumName;
	}
	
	//wenyongzhe 2016.1.9 new_ui
	private synchronized void loadImage(List<CommonFileInfo> albums, int position, final ImageView imageView) {
//		final ImageView imageView = imageView;
		final CommonFileInfo tempInfo = albums.get(position);
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
//							if(position==0){
//								imageView.setImageBitmap(add_icon);
//								return;
//							}
							imageView.setImageBitmap(bitmap);
						}
					}
				});
	}
	
	//wenyongzhe 2016.1.9 new_ui
	private synchronized ImageView getImageView(List<CommonFileInfo> albums, int position, CloudMainViewCache viewCache){
		if (position < 0 || position > albums.size() || position > 3) {
			return null;
		}
		ImageView imageView=null;
		imageBack: {
			if (position == 0) {
				imageView = viewCache.getCloud_first_img();
				break imageBack;
			} else if (position == 1) {
				imageView = viewCache.getCloud_second_img();
				break imageBack;

			} else if (position == 2) {
				imageView = viewCache.getCloud_third_img();
				break imageBack;

			} else if (position == 3) {
				imageView = viewCache.getCloud_fourth_img();
				break imageBack;
			}
		}
		return imageView;
	}
	
	//wenyongzhe 2016.2.29
	private synchronized void clearImageView(CloudMainViewCache viewCache){
		ImageView imageView=null;
		imageView = viewCache.getCloud_first_img();
		imageView.setImageBitmap(null);
		imageView = viewCache.getCloud_second_img();
		imageView.setImageBitmap(null);
		imageView = viewCache.getCloud_third_img();
		imageView.setImageBitmap(null);
		imageView = viewCache.getCloud_fourth_img();
		imageView.setImageBitmap(null);
	}
				

}
