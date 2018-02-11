package com.android.gallery3d.local.widget;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.local.tools.ImageLoader;
import com.android.gallery3d.local.tools.ImageLoader.ImageProcessingCallback;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;

public class GalleryAdapter extends GalleryBaseAdapter {
	private Map<String, List<MediaFileInfo>> mediaFilesMap;
	private Map<Integer, String> foldersMap;
	private static final String TAG = "GalleryAdapter";
	private GalleryLocalActivity activity;
	private int textNameLength;

	public GalleryAdapter(ConcurrentHashMap<String, List<MediaFileInfo>> mediaFilesMap, ConcurrentHashMap<Integer, String> foldersMap, Context context) {
		super(context);
		this.activity = (GalleryLocalActivity) context;
		this.mediaFilesMap = mediaFilesMap;
		this.foldersMap = foldersMap;
		textNameLength = (int) context.getResources().getDimension(R.dimen.aurora_album_name_textview_length);
	}

	public GalleryAdapter(Context mContext) {
		this(null, null, mContext);
	}

	/*
	 * public ConcurrentHashMap<String, List<MediaFileInfo>> getMediaFilesMap() { return mediaFilesMap; }
	 */
	public void setMediaFilesMap(ConcurrentHashMap<String, List<MediaFileInfo>> mediaFilesMap) {
		if (this.mediaFilesMap == null) {
			this.mediaFilesMap = new ConcurrentHashMap<String, List<MediaFileInfo>>();
		}
		this.mediaFilesMap.clear();
		this.mediaFilesMap.putAll(mediaFilesMap);
	}

	/*
	 * public ConcurrentHashMap<Integer, String> getFoldersMap() { return foldersMap; }
	 */
	public void setFoldersMap(ConcurrentHashMap<Integer, String> foldersMap) {
		if (this.foldersMap == null) {
			this.foldersMap = new ConcurrentHashMap<Integer, String>();
		}
		this.foldersMap.clear();
		this.foldersMap.putAll(foldersMap);
	}

	@Override
	public int getCount() {
		if (foldersMap == null) {
			return 0;
		}
		return foldersMap.size();
	}

	@Override
	public Object getItem(int position) {
		if (foldersMap == null) {
			return null;
		}
		return foldersMap.get(position);
	}

	/**
	 * 获取文件夹下多媒体文件内容
	 * @param position
	 * @return
	 */
	public List<MediaFileInfo> getItemMediaFileInfos(int position) {
		Object object = getItem(position);
		if (object != null && mediaFilesMap != null) {
			return mediaFilesMap.get(object);
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GalleryAlbumItemView viewCache;
		if (convertView == null) {
			convertView = (View) inflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout main = (RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front);
			inflater.inflate(R.layout.gallery_local_item, main);
			RelativeLayout layout = (RelativeLayout) convertView.findViewById(com.aurora.R.id.control_padding);
			layout.setPadding(0, 0, 0, 0);
			viewCache = new GalleryAlbumItemView(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (GalleryAlbumItemView) convertView.getTag();
		}
		if (mediaFilesMap != null && foldersMap != null) {
			String albumName = foldersMap.get(position);
			if (!TextUtils.isEmpty(albumName)) {
				List<MediaFileInfo> albums = mediaFilesMap.get(albumName);
				String systemAlbumName = getSystemAblumName(mContext, albumName);
				TextView textView = viewCache.getLocalAlbumsName();
				if (TextUtils.isEmpty(systemAlbumName)) {
					int pos = albumName.lastIndexOf('/');
					systemAlbumName = (String) GalleryLocalUtils.getEllipsizeEnd(albumName.substring(pos + 1), textView, textNameLength);
				}
				textView.setText(systemAlbumName);
				int size = albums.size();
				viewCache.getLocalAlbumsNum().setText((albums == null ? "0" : size) + "");
				// viewCache.initImageView();
				if (albums != null) {
					for (int i = 0; i < 4; i++) {
						ImageView imageView = getImageView(albums, i, viewCache);
						imageView.setImageBitmap(null);
						if (imageView != null) {
							MediaFileInfo mediaFileInfo = i < size?albums.get(i):new MediaFileInfo();
							loadImage(mediaFileInfo, i, imageView);
						}
					}
				}
				if (isOperation()) {
					if (activity.getFoldersPath().contains(albumName)) {
						viewCache.getCheckboxOk().setVisibility(View.VISIBLE);
						/*
						 * if (activity.getSelectPosition() == position && isItemPicAnim()) { setItemPicAnim(false);
						 * imageViewAnim(viewCache.getCheckboxOk(), true); } else {
						 */
						viewCache.getCheckboxOk().setImageResource(R.drawable.aurora_check14);
						/* } */
						viewCache.getCheckboxNo().setVisibility(View.GONE);
					} else {
						viewCache.getCheckboxOk().setVisibility(View.GONE);
						viewCache.getCheckboxNo().setVisibility(View.GONE);
						// if (activity.getSelectPosition() == position && isItemPicAnim()) {
						// imageViewAnim(viewCache.getCheckboxNo(), false);
						// setItemPicAnim(false);
						// } else {
						viewCache.getCheckboxNo().setImageResource(R.drawable.aurora_check00);
						// }
					}
				} else {
					viewCache.getCheckboxOk().setVisibility(View.GONE);
					viewCache.getCheckboxNo().setVisibility(View.GONE);
				}
			}
		}
		return convertView;
	}

	public synchronized String getSystemAblumName(Context mContext, String albumName) {
		String systemAlbumName = null;
		systemName: {
			if (albumName.equals(GalleryLocalActivity.cameraPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_camera);
				break systemName;
			}
			if (albumName.equals(GalleryLocalActivity.screenShotsPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_screenshots);
				break systemName;
			}
			if (albumName.equals(GalleryLocalActivity.videoPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_video);
				break systemName;
			}
			if (albumName.equals(GalleryLocalActivity.collectionPath)) {
				systemAlbumName = mContext.getString(R.string.album_name_collection);
				break systemName;
			}
			if (albumName.equals(GalleryLocalActivity.cloudPath)) {
				systemAlbumName = mContext.getString(R.string.cloud_download);
				break systemName;
			}
		}
		return systemAlbumName;
	}

	private ImageView getImageView(List<MediaFileInfo> albums, int position, GalleryAlbumItemView viewCache) {
		/*
		 * if (position < 0 || position > albums.size() || position > 3) { return null; }
		 */
		ImageView imageView = null;
		imageBack: {
			if (position == 0) {
				imageView = viewCache.getFirstImg();
				break imageBack;
			} else if (position == 1) {
				imageView = viewCache.getSecondImg();
				break imageBack;
			} else if (position == 2) {
				imageView = viewCache.getThirdImg();
				break imageBack;
			} else if (position == 3) {
				imageView = viewCache.getFourthImg();
				break imageBack;
			}
		}
		return imageView;
	}

	private void loadImage(final MediaFileInfo mediaFileInfo, final int index, final ImageView imageView) {
		if (mediaFileInfo != null) {
			imageView.setTag(mediaFileInfo.filePath);
			imageLoader.displayImage(mediaFileInfo.filePath, new ImageProcessingCallback() {
				@Override
				public void onImageProcessing(WeakReference<Bitmap> weak, String tag) {
					try {
						if (tag!=null&&tag.equals(imageView.getTag())) {
							Bitmap bitmap = weak.get();
							if (bitmap == null) {
								imageView.setImageBitmap(null);
								return;
							}
							bitmap = GalleryUtils.iamgeRotation(bitmap, mediaFileInfo.orientation);
							imageView.setImageBitmap(bitmap);
						}else {
							imageView.setImageBitmap(null);
						}
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				}
			}, mediaFileInfo.isImage);
		}
	}
}
