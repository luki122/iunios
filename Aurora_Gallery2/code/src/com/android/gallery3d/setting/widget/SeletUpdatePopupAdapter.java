package com.android.gallery3d.setting.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraSwitch;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.ImageLoader;
import com.android.gallery3d.local.tools.MediaFileOperationUtil;
import com.android.gallery3d.local.tools.ImageLoader.ImageProcessingCallback;
import com.android.gallery3d.local.widget.AlbumsPopViewCache;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.setting.SettingsActivity;
import com.android.gallery3d.setting.tools.SettingLocalUtils;
import com.android.gallery3d.setting.tools.SettingMediaFileOperationUtil;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;

public class SeletUpdatePopupAdapter extends BaseAdapter {

	private static Bitmap icon_cache;
	private static Bitmap add_icon;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private List<MediaFileInfo> fileInfos;
	private int albumNameLength = 350;
	private static final String TAG = "AlbumsSelectPopupAdapter";
	private Context mContext;
	protected SettingMediaFileOperationUtil operationUtil;
	

	public SeletUpdatePopupAdapter(Context context, List<MediaFileInfo> objects) {
		inflater = LayoutInflater.from(context);
		this.mContext = context;
		if (icon_cache == null) {
			icon_cache = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_photo);
		}
		if (add_icon == null) {
			add_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud_create_album_icon_normal);
		}
		imageLoader = new ImageLoader(context);
		this.fileInfos = objects;
		albumNameLength = (int) context.getResources().getDimension(R.dimen.aurora_album_name_length);
		
		canClickCheckBox = false;
		operationUtil =SettingMediaFileOperationUtil.getMediaFileOperationUtil(mContext);
		new LoadXmlFileTask().execute();
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
		SettingPopViewCache viewCache = null;
		if (convertView == null) {
			convertView = inflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout main = (RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front);
			inflater.inflate(R.layout.aurora_setting_pop_item, main);
			RelativeLayout layout =  (RelativeLayout) convertView.findViewById(com.aurora.R.id.control_padding);
			layout.setPadding(0, 0, 0, 0);
			viewCache = new SettingPopViewCache(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (SettingPopViewCache) convertView.getTag();
		}
		final MediaFileInfo fileInfo = (MediaFileInfo) getItem(position);
		if (fileInfo != null) {
			TextView name = viewCache.getAlbumName();
			final ImageView icon = viewCache.getAlbumImage();
			TextView albumNameNum = viewCache.getAlbumNameNum();
			AuroraSwitch mAuroraSwitch = viewCache.getmAuroraSwitch();
			if(  allAutoUploadPaths.contains( fileInfo.firstPhotoPath )){
				mAuroraSwitch.setChecked(true);
				mAuroraSwitch.setSelected(true);
			}else{
				mAuroraSwitch.setChecked(false);
				mAuroraSwitch.setSelected(false);
			}
			mAuroraSwitch.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!canClickCheckBox){
						return;
					}
					String path = fileInfo.firstPhotoPath;
					if (allAutoUploadPaths.contains(path)) {
						allAutoUploadPaths.remove(path);
					} else {
						allAutoUploadPaths.add(path);
					}
					//wenyongzhe 2016.2.23
					if(operationUtil == null){
						operationUtil =SettingMediaFileOperationUtil.getMediaFileOperationUtil(mContext);
					}
					operationUtil.shieldOperation(allAutoUploadPaths);
				}
			});
			
			icon.setImageBitmap(icon_cache);
		
			String systemAlbumName = getSystemAblumName(mContext, fileInfo.fileName);//wenyongzhe
			CharSequence foldName = Utils.getEllipsizeEnd(systemAlbumName, name, albumNameLength);
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
						if (((String) icon.getTag()).equals(tag)) {
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

		return convertView;
	}
	
	//protected List<String> foldersPath = Collections.synchronizedList(new ArrayList<String>());
	protected List<String> allAutoUploadPaths = new ArrayList<String>();
	private boolean canClickCheckBox = false;
	public class LoadXmlFileTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			List<String> noShowPaths = SettingLocalUtils.doParseXml(mContext);
			allAutoUploadPaths.clear();
			allAutoUploadPaths.addAll(noShowPaths);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			canClickCheckBox = true;
			notifyDataSetChanged();
		}
	}
	
	protected void dismiss(){
		if(operationUtil == null){
			operationUtil =SettingMediaFileOperationUtil.getMediaFileOperationUtil(mContext);
		}
		operationUtil.shieldOperation(allAutoUploadPaths);
	}
	
//wenyongzhe2016.1.29
	public static final String cameraPath =  "Camera";
	public static final String  screenShotsPath = "Screenshots";
	public static final String cloudPath =  "cloud";
	public static final String videoPath = "videoPath";
	public static final String collectionPath = "collectionPath";
	public synchronized String getSystemAblumName(Context mContext, String albumName) {
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
			systemAlbumName = albumName;
		}
		return systemAlbumName;
	}
}
