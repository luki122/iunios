package com.android.gallery3d.local.widget;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.local.tools.ImageLoader;
import com.android.gallery3d.local.tools.ImageLoader.ImageProcessingCallback;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;

public class AlbumsSelectPopupAdapter extends BaseAdapter {

	private static Bitmap icon_cache;
	private static Bitmap add_icon,image_bk;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private List<MediaFileInfo> fileInfos;
	private int albumNameLength = 350;
	private static final String TAG = "AlbumsSelectPopupAdapter";
	private Context mContext;

	public AlbumsSelectPopupAdapter(Context context, List<MediaFileInfo> objects) {
		inflater = LayoutInflater.from(context);
		this.mContext = context;
		if (icon_cache == null) {
			icon_cache = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_photo);
		}
		if (add_icon == null) {
			add_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud_create_album_icon_normal);
		}
		if(image_bk==null){
			image_bk = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_bk);
		}
		imageLoader = new ImageLoader(context);
		this.fileInfos = objects;
		albumNameLength = (int) context.getResources().getDimension(R.dimen.aurora_album_name_length);
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
		AlbumsPopViewCache viewCache = null;
		LogUtil.d(TAG, "---------getView:"+position);
		if (convertView == null) {
			convertView = inflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout main = (RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front);
			inflater.inflate(R.layout.aurora_local_pop_item, main);
			RelativeLayout layout =  (RelativeLayout) convertView.findViewById(com.aurora.R.id.control_padding);
			layout.setPadding(0, 0, 0, 0);
			viewCache = new AlbumsPopViewCache(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (AlbumsPopViewCache) convertView.getTag();
		}
		final MediaFileInfo fileInfo = (MediaFileInfo) getItem(position);
		if (fileInfo != null) {
			TextView name = viewCache.getAlbumName();
			final ImageView icon = viewCache.getAlbumImage();
			icon.setTag(position+"position");
			TextView albumNameNum = viewCache.getAlbumNameNum();
			if (position == 0) {
				name.setText(R.string.aurora_create_album);
				if(icon.getTag().equals(position+"position")){
					icon.setImageBitmap(add_icon);
				}
				albumNameNum.setVisibility(View.GONE);
				viewCache.getImageframe().setBackgroundColor(Color.WHITE);
			} else {
				CharSequence foldName = Utils.getEllipsizeEnd(fileInfo.fileName, name, albumNameLength);
				name.setText(foldName);
				String msg = String.format(mContext.getResources().getString(R.string.album_num_string),fileInfo.Count);
				albumNameNum.setVisibility(View.VISIBLE);
				albumNameNum.setText(msg);
				viewCache.getImageframe().setBackgroundResource(R.drawable.image_bk);
				if(!fileInfo.IsDir){
					icon.setTag(fileInfo.filePath);
					imageLoader.displayImage(fileInfo.filePath, new ImageProcessingCallback() {
						
						@Override
						public void onImageProcessing(WeakReference<Bitmap> weak, String tag) {
							if (((String) icon.getTag()).equals(tag)) {//
								Bitmap bitmap = weak.get();
								if (bitmap == null || bitmap.isRecycled()) {
									return;
								}
								icon.setImageBitmap(weak.get());
							}
						}
					}, true);
				}
			}
		}

		return convertView;
	}
}
