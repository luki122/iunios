package com.android.gallery3d.local.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.android.gallery3d.local.GalleryItemActivity;
import com.android.gallery3d.local.tools.ImageLoader;
import com.android.gallery3d.local.tools.ImageLoader.ImageProcessingCallback;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.gallery3d.R;

public class GalleryItemAdapter extends GalleryBaseAdapter {

	private List<MediaFileInfo> mediaFileInfos = new ArrayList<MediaFileInfo>();
	private static final String TAG = "GalleryItemAdapter";
	private GalleryItemActivity activity;

	public GalleryItemAdapter(List<MediaFileInfo> mediaFileInfos, Context mContext) {
		super(mContext);
		this.mediaFileInfos = mediaFileInfos;
		this.activity = (GalleryItemActivity) mContext;
	}

	public interface SelectionListener {
		void onDragSelectionChanged();
	}

	private SelectionListener mSelectionListener;

	public void setSelectionListener(SelectionListener mSelectionListener) {
		this.mSelectionListener = mSelectionListener;
	}

	private int mLastCount = -1;

	private void fireSelectionListener() {
		if (mLastCount == activity.getSelectFileInfo().size())
			return;
		mLastCount = activity.getSelectFileInfo().size();
		if (mSelectionListener != null)
			mSelectionListener.onDragSelectionChanged();
	}

	@Override
	public int getCount() {
		if (mediaFileInfos == null) {
			return 0;
		}
		return mediaFileInfos.size();
	}

	@Override
	public Object getItem(int position) {
		if (mediaFileInfos == null) {
			return null;
		}
		return mediaFileInfos.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		GalleryItemView viewCache;
		if (convertView == null) {
			convertView = (View) inflater.inflate(R.layout.aurora_local_item_browse, null);
			viewCache = new GalleryItemView(convertView);
			convertView.setTag(viewCache);
			// LogUtil.d(TAG, "-2---getView----convertView:"+convertView);
		} else {
			viewCache = (GalleryItemView) convertView.getTag();
			// LogUtil.d(TAG, "--1--getView----convertView:"+convertView);
		}
		viewCache.getCheckboxOk().setTag(position);
		if (mediaFileInfos != null&&mediaFileInfos.size()>position&&position>=0) {
			final MediaFileInfo mediaFileInfo = mediaFileInfos.get(position);
			if (mediaFileInfo != null && !TextUtils.isEmpty(mediaFileInfo.filePath)) {
				final String filePath = mediaFileInfo.filePath;
				// LogUtil.d(TAG, "----filePath:"+filePath);
				final ImageView imageView = viewCache.getAlbumImage();
				imageView.setImageBitmap(null);
				imageView.setTag(filePath);
				imageLoader.displayImage(filePath, new ImageProcessingCallback() {

					@Override
					public void onImageProcessing(WeakReference<Bitmap> weak, String tag) {
						try {
							if (tag.equals(imageView.getTag())) {
								Bitmap bitmap = weak.get();
								if (bitmap == null) {
									return;
								}
								bitmap = GalleryUtils.iamgeRotation(bitmap, mediaFileInfo.orientation);
								imageView.setImageBitmap(bitmap);
							}
						} catch (Exception e) {
							Log.e(TAG, e.getMessage());
							e.printStackTrace();
						}
					}
				}, mediaFileInfo.isImage);

			}

			ImageView collection = viewCache.getCollectionImage();
			collection.setVisibility(mediaFileInfo.favorite ? View.VISIBLE : View.GONE);
			ImageView isVideo = viewCache.getIsvideoImage();
			isVideo.setVisibility(mediaFileInfo.isImage ? View.GONE : View.VISIBLE);
			final Drawable d;
			if (isOperation()) {
				if (activity.getSelectFileInfo().contains(mediaFileInfo)) {
					viewCache.getCheckboxOk().setVisibility(View.VISIBLE);
					// if (activity.getSelectPosition() == position &&
					// isItemPicAnim()) {
					// setItemPicAnim(false);
					// imageViewAnim(viewCache.getCheckboxOk(), true);
					// } else {
					viewCache.getCheckboxOk().setImageResource(R.drawable.aurora_check14);
					// }
					viewCache.getCheckboxNo().setVisibility(View.GONE);
					d = new ColorDrawable(Color.parseColor("#4c000000"));
				} else {
					viewCache.getCheckboxOk().setVisibility(View.GONE);
					viewCache.getCheckboxNo().setVisibility(View.GONE);
					// if (activity.getSelectPosition() == position &&
					// isItemPicAnim()) {
					// setItemPicAnim(false);
					// imageViewAnim(viewCache.getCheckboxNo(), false);
					// } else {
					viewCache.getCheckboxNo().setImageResource(R.drawable.aurora_check00);
					// }
					d = null;
				}
			} else {
				viewCache.getCheckboxOk().setVisibility(View.GONE);
				viewCache.getCheckboxNo().setVisibility(View.GONE);
				d = null;
			}
			viewCache.getFrameLayout().setForeground(d);
		}
		return convertView;
	}

	public void setSelected(int position, boolean mUnDragSelect) {
		LogUtil.d(TAG, " ----position:" + position + "--" + mUnDragSelect);
		MediaFileInfo mediaFileInfo = mediaFileInfos.get(position);
		List<MediaFileInfo> fileInfos = activity.getSelectFileInfo();
		if (mUnDragSelect) {
			if (!fileInfos.contains(mediaFileInfo)) {
				fileInfos.add(mediaFileInfo);
			}
		} else {
			fileInfos.remove(mediaFileInfo);
		}
		notifyDataSetChanged();
		fireSelectionListener();
	}

	public boolean isSelect(int postion) {
		if(postion<0||mediaFileInfos.size()<=postion){
			return false;
		}
		MediaFileInfo mediaFileInfo = mediaFileInfos.get(postion);
		List<MediaFileInfo> fileInfos = activity.getSelectFileInfo();
		if (fileInfos.contains(mediaFileInfo)) {
			return true;
		} else {
			return false;
		}
	}

	public void setOpposite() {
		opposite = false;
	}

	boolean opposite = false;
	boolean isToTurn = false;

	public void selectRange(int from, int to, int min, int max, boolean mUnDragSelect) {
		LogUtil.d(TAG, "---from:" + from + " to:" + to + " min:" + min + " max:" + max+" opposite:"+opposite);
		if (from < 0 || to < 0) {
			return;
		}
		if (from > to) {
			if (max >= from && to >= min) {
				for (int i = to; i <= from; i++) {
					setSelected(i, opposite);
				}
			}
		} else if (from < to) {

			if (max > to) {// 反向
				if (from <= min) {
					for (int i = from; i <= to; i++) {
						setSelected(i, true);
					}
					return;
				}
				for (int i = from; i <= to; i++) {
					setSelected(i, false);
				}
				isToTurn = true;
			} else if (max == to) {
				for (int i = from; i <= to; i++) {
					setSelected(i, isToTurn ? false : mUnDragSelect);// mUnDragSelect
				}
				isToTurn = false;
			}

		} else {//--from:1 to:1 min:0 max:19 opposite:false
			if (max > to) {
				if (min > to) {
					setSelected(from, mUnDragSelect);
				} else if (min < to) {
					if (opposite) {
						setSelected(from - 1, false);
						opposite = false;
					}
					setSelected(from, false);
				} else {
					if (!opposite) {
						setSelected(from, false);
					} else {
						setSelected(from, true);
					}
				}
			} else if (max == to) {
				setSelected(from, mUnDragSelect);
			} else {
				opposite = true;
				setSelected(from, mUnDragSelect);
			}

		}

	}

}
