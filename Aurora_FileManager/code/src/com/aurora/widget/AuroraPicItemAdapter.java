package com.aurora.widget;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.aurora.lazyloader.ImageLoader;
import com.aurora.lazyloader.ImageLoader.ImageProcessingCallback;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.Util;
import com.aurora.filemanager.fragment.PictureCategoryFragment;
import com.aurora.filemanager.fragment.PictureFragment;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;

/**
 * 图片分类适配器
 * 
 * @author jiangxh
 * @CreateTime 2014年4月28日 下午6:50:51
 * @Description com.aurora.widget AuroraPicTwoAdapter.java
 */
public class AuroraPicItemAdapter extends BaseAdapter {

	private List<FileInfo> categoryFiles;
	private LayoutInflater mInflater;
	private Context mContext;
	public ImageLoader mImageLoader;
	private static final String TAG = "AuroraPicItemAdapter";
	private static Bitmap cache;
	private List<FileInfo> selects;
	// add by Jxh 2014-8-12 begin
	private PictureFragment fragment;
	// add by Jxh 2014-8-12 end
	private FileExplorerTabActivity activity;

	/**
	 * @return the selects
	 */
	public List<FileInfo> getSelects() {
		return selects;
	}

	/**
	 * @param selects
	 *            the selects to set
	 */
	public void setSelects(List<FileInfo> selects) {
		this.selects = selects;
	}
	public void setFileInfos(List<FileInfo> infos){
		this.categoryFiles = infos;
	}
	
	public AuroraPicItemAdapter(List<FileInfo> categoryFiles, Context mContext,
			PictureFragment fragment) {
		super();
		this.mContext = mContext;
		this.mInflater = LayoutInflater.from(mContext);
		this.mImageLoader = ImageLoader.getInstance(mContext);
		this.categoryFiles = categoryFiles;
		if (cache == null) {
			cache = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.file_icon_picture_cache);
		}
		// add by Jxh 2014-8-12 begin
		this.fragment = fragment;
		// add by Jxh 2014-8-12 end
		this.activity = (FileExplorerTabActivity) mContext;
	}

	@Override
	public int getCount() {
		return categoryFiles.size();
	}

	@Override
	public Object getItem(int position) {
		return categoryFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View rowView, ViewGroup parent) {
//		LogUtil.elog(TAG, "getView");
		AuroraPicItemViewCache viewCache;
		if (rowView == null) {
			rowView = mInflater.inflate(R.layout.aurora_pic_brower_item, null);
			viewCache = new AuroraPicItemViewCache(rowView);
			rowView.setTag(viewCache);
		} else {
			viewCache = (AuroraPicItemViewCache) rowView.getTag();
		}

		if (categoryFiles != null) {
			try {
				final FileInfo fileInfo = categoryFiles.get(position);
				if (fileInfo != null) {
					String url = fileInfo.filePath;
					final ImageView imageView = viewCache.getFirstImage();
					// LogUtil.log(TAG, url);
					imageView.setTag(url);
					imageView.setImageBitmap(cache);
					mImageLoader.displayImage(url, new ImageProcessingCallback() {

						@Override
						public void onImageProcessing(WeakReference<Bitmap> weak,
								String tag) {
							try {
								if (tag.equals(imageView.getTag())) {
									Bitmap bitmap = weak.get();
									bitmap = Util.iamgeRotation(bitmap,
											fileInfo.orientation);
									imageView.setImageBitmap(bitmap);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
				// modify by Jxh 2014-8-12 begin
				if (fragment.isOperationFile()) {
					// modify by Jxh 2014-8-12 end
					if (selects.contains(fileInfo)) {
						viewCache.getPicCheckboxNo().setVisibility(View.GONE);
						viewCache.getPicCheckboxYes().setVisibility(View.VISIBLE);
						if(fragment.isItemPicAnim()&&(fragment.getSelectPosition()==position)){
							activity.imageViewAnim(viewCache.getPicCheckboxYes(), true);
						}else {
							viewCache.getPicCheckboxYes().setImageResource(R.drawable.aurora_check14);
						}
					} else {
						viewCache.getPicCheckboxYes().setVisibility(View.GONE);
						viewCache.getPicCheckboxNo().setVisibility(View.VISIBLE);
						if(fragment.isItemPicAnim()&&(fragment.getSelectPosition()==position)){
							activity.imageViewAnim(viewCache.getPicCheckboxNo(), false);
						}else {
							viewCache.getPicCheckboxNo().setImageResource(R.drawable.aurora_check00);
						}
					}
				} else {
					viewCache.getPicCheckboxNo().setVisibility(View.GONE);
					viewCache.getPicCheckboxYes().setVisibility(View.GONE);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return rowView;
	}
}