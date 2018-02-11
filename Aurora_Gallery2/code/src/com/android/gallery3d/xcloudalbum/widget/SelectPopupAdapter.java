package com.android.gallery3d.xcloudalbum.widget;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.android.gallery3d.app.Log;
import com.android.gallery3d.xcloudalbum.CloudActivity;
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

public class SelectPopupAdapter extends BaseAdapter {

	private static Bitmap icon_cache;
	private static Bitmap add_icon;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private List<CommonFileInfo> fileInfos;
	private CloudActivity cloudActivity;
	//private AuroraActivity cloudActivity;
	private ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap;
	private int albumNameLength=350;
	private static final String TAG ="SelectPopupAdapter";

	public SelectPopupAdapter(Context context, List<CommonFileInfo> objects) {
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
		cloudActivity = (CloudActivity) context;
		albumNameLength = (int) context.getResources().getDimension(
				R.dimen.aurora_album_name_length);
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
		LogUtil.d(TAG, "getView fileInfo::"+fileInfo);
		if (fileInfo != null) {
			TextView name = viewCache.getAlbumName();
			final ImageView icon = viewCache.getAlbumImage();
			icon.setImageBitmap(icon_cache);
			
			//wenyongzhe 2015.9.16  start
//			if (position == 0) {
//				name.setText(R.string.aurora_create_album);
//				icon.setImageBitmap(add_icon);
//			} else {
			//wenyongzhe 2015.9.16  end
			
				String nameString = Utils.getPathNameFromPath(fileInfo.path);
				CharSequence foldName = Utils.getEllipsizeEnd(nameString,
						name, albumNameLength);
				name.setText(nameString);
				
				concurrentHashMap = cloudActivity.getConcurrentHashMap();
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
									if (bitmap == null
											|| bitmap.isRecycled()) {
										return;
									}
									LogUtil.d(TAG, "setImageBitmap::"+weak.get());
									icon.setImageBitmap(weak.get());
								}
							}
						});
					}
				}else {
					LogUtil.d(TAG, "concurrentHashMap is null");
				}
//			}//wenyongzhe
		}

		return convertView;
	}
}
