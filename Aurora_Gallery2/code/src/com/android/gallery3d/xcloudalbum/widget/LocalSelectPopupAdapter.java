package com.android.gallery3d.xcloudalbum.widget;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader.ImageProcessingCallback;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;

public class LocalSelectPopupAdapter extends BaseAdapter {
	private static final String TAG="LocalSelectPopupAdapter";
	private static Bitmap icon_cache;
	private static Bitmap add_icon;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private List<CommonFileInfo> fileInfos;
	private GalleryAppImpl mActivity;
	private ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap;

	public LocalSelectPopupAdapter(Context context, List<CommonFileInfo> objects) {
		inflater = LayoutInflater.from(context);
		if (icon_cache == null) {
			icon_cache = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.empty_photo);
		}
		if (add_icon == null) {
			add_icon = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.cloud_create_album_icon_normal);
		}
		imageLoader = new ImageLoader(context);
		this.fileInfos = objects;
		mActivity = (GalleryAppImpl) context.getApplicationContext();
	}
	
	//paul add for BUG #15522
	public void setInfos(List<CommonFileInfo> objects){
		this.fileInfos = objects;
	}
	
	@Override
	public int getCount() {
		if(fileInfos == null) return 0;
		//wenyongzhe
//		return fileInfos.size();
		return fileInfos.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		//wenyongzhe start
		if(position == 0) return null;
		return fileInfos.get(position - 1);
//		return fileInfos.get(position);
		//wenyongzhe end
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CloudPopViewCache viewCache = null;
		if (convertView == null) {
			convertView = inflater.inflate(com.aurora.R.layout.aurora_slid_listview,
					null);
			RelativeLayout main = (RelativeLayout) convertView
					.findViewById(com.aurora.R.id.aurora_listview_front);
			inflater.inflate(R.layout.aurora_cloud_fragment_item, main);//wenyongzhe 2015.9.16 fragment
			viewCache = new CloudPopViewCache(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (CloudPopViewCache) convertView.getTag();
		}

		final CommonFileInfo fileInfo = (CommonFileInfo) getItem(position);
		
		//wenyongzhe start
		if (position == 0) {
			TextView name = viewCache.getAlbumName();
			name.setText(R.string.aurora_create_album);
			final ImageView icon = viewCache.getAlbumImage();
			icon.setImageBitmap(icon_cache);
			icon.setImageBitmap(add_icon);
		} 
		if (position != 0 && fileInfo != null) {
//		if ( fileInfo != null) {
		//wenyongzhe end

			TextView name = viewCache.getAlbumName();
			final ImageView icon = viewCache.getAlbumImage();
			icon.setImageBitmap(icon_cache);
			//else {
				name.setText(Utils.getPathNameFromPath(fileInfo.path));
				concurrentHashMap = mActivity.getConcurrentHashMap();
//				LogUtil.d(TAG, "concurrentHashMap size::"+concurrentHashMap.size());
				if(concurrentHashMap!=null){
					List<CommonFileInfo> infos = concurrentHashMap.get(fileInfo);
					if (infos != null && infos.size() > 0) {
						CommonFileInfo temp = infos.get(0);
						icon.setTag(temp.blockList);
						imageLoader.displayImage(temp.path, temp.blockList,
								new ImageProcessingCallback() {
							
							@Override
							public void onImageProcessing(
									WeakReference<Bitmap> weak, String tag) {
								if (((String) icon.getTag()).equals(tag)) {//
									Bitmap bitmap = weak.get();
//									LogUtil.d(TAG, "bitmap::"+bitmap);
									//LogUtil.d("SQF_LOG", "LocalSelectPopupAdapter ----> bitmap::"+bitmap);
									
									if (bitmap == null
											|| bitmap.isRecycled()) {
										return;
									}
									icon.setImageBitmap(weak.get());
								}
							}
						});
					}
				}
			//}
		}

		return convertView;
	}
}
