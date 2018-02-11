package com.aurora.widget;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.filemanager.fragment.PictureCategoryFragment;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;

/**
 * 图片分类适配器
 * 
 * @author jiangxh
 * @CreateTime 2014年4月28日 下午6:50:51
 * @Description com.aurora.widget AuroraPicTwoAdapter.java
 */
public class AuroraPicTwoAdapter extends BaseAdapter {

	private Map<String, List<FileInfo>> categoryPiclistMap;
	private List<FileInfo> categoryFiles;
	private Map<Integer, String> picFilePath;
	private LayoutInflater mInflater;
	private Context mContext;
	public ImageLoader mImageLoader;
	private static final String TAG = "AuroraPicTwoAdapter";
	private static Bitmap cache;
	private static Bitmap icon_cache;//
	private static Bitmap priCache;
	private List<String> selects;
	private int picNameLength = 350;
	// add by Jxh 2014-8-12 begin
	private PictureCategoryFragment fragment;
	// add by Jxh 2014-8-12 end

	private FileExplorerTabActivity activity;

	/**
	 * @return the selects
	 */
	public List<String> getSelects() {
		return selects;
	}

	/**
	 * @param selects
	 *            the selects to set
	 */
	public void setSelects(List<String> selects) {
		this.selects = selects;
	}

	/**
	 * @param categoryPiclistMap
	 *            the categoryPiclistMap to set
	 */
	public void setCategoryPiclistMap(
			Map<String, List<FileInfo>> categoryPiclistMap) {
		this.categoryPiclistMap = categoryPiclistMap;
	}

	/**
	 * @param picFilePath
	 *            the picFilePath to set
	 */
	public void setPicFilePath(Map<Integer, String> picFilePath) {
		this.picFilePath = picFilePath;
	}

	public AuroraPicTwoAdapter(Map<String, List<FileInfo>> categoryPiclistMap,
			Map<Integer, String> picFilePath, Context mContext,
			PictureCategoryFragment fragment) {
		super();
		this.categoryPiclistMap = categoryPiclistMap;
		this.picFilePath = picFilePath;
		this.mContext = mContext;
		this.mInflater = LayoutInflater.from(mContext);
		this.mImageLoader = ImageLoader.getInstance(mContext);
		if (cache == null) {
			cache = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.file_icon_picture_cache);
		}
		if (icon_cache == null) {
			icon_cache = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.privacy_img_icon);
		}
		if(priCache==null){
			priCache = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.pri_pic);
		}
		picNameLength = (int) mContext.getResources().getDimension(
				R.dimen.pic_name_length);
		// add by Jxh 2014-8-12 begin
		this.fragment = fragment;
		// add by Jxh 2014-8-12 end
		activity = (FileExplorerTabActivity) mContext;
	}

	@Override
	public int getCount() {
		return picFilePath.size();
	}

	@Override
	public Object getItem(int position) {
		return picFilePath.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private FileInfo fileInfo;
	
	private boolean selectAnim;

	@Override
	public View getView(int position, View rowView, ViewGroup parent) {
		// LogUtil.log(TAG, "getView");
		AuroraPicViewCache viewCache;
		if (rowView == null) {
			rowView = mInflater.inflate(R.layout.aurora_file_pic_brower_item,
					null);
			viewCache = new AuroraPicViewCache(rowView);
			rowView.setTag(viewCache);
		} else {
			viewCache = (AuroraPicViewCache) rowView.getTag();
		}
		// 空隙调整,add header view
		if (position == 0 || position == 1) {
			viewCache.getHeaderView().setVisibility(View.VISIBLE);
		} else {
			viewCache.getHeaderView().setVisibility(View.GONE);
		}

		// 滑动条位置调整,set left right padding
		if (position % 2 != 0) {
			LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			Resources res = mContext.getResources();
			lParams.rightMargin = res
					.getDimensionPixelSize(R.dimen.aurora_file_manager_pic_list_margin);
			lParams.leftMargin = res
					.getDimensionPixelSize(R.dimen.aurora_file_manager_pic_list_margin_2);
			viewCache.getLayout().setLayoutParams(lParams);
		} else {
			LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			Resources res = mContext.getResources();
			lParams.leftMargin = res
					.getDimensionPixelSize(R.dimen.aurora_file_manager_pic_list_margin);
			lParams.rightMargin = res
					.getDimensionPixelSize(R.dimen.aurora_file_manager_pic_list_margin_2);
			viewCache.getLayout().setLayoutParams(lParams);
		}

		if (categoryPiclistMap != null) {
			String fileName = picFilePath.get(position);
			// LogUtil.elog(TAG, fileName);
			if (fileName != null) {
				categoryFiles = categoryPiclistMap.get(fileName);
				if (categoryFiles != null) {
					if (categoryFiles.size() > 0) {
						fileInfo = categoryFiles.get(0);
					} else {// 隐私图框显示
						fileInfo = new FileInfo();
					}
					CharSequence foldName = Util.getEllipsize(fileName,
							viewCache.getPicFoldName(), picNameLength);
					// 解决英文字母过长 只显示一个点的问题
					if (activity.isPrivacy()
							&& fileName.equals(FileExplorerTabActivity
									.getPrivacyMedioPath(FileCategory.Picture))) {
						foldName = mContext.getString(R.string.p_picture);
					}

					viewCache.getPicFoldName().setText(foldName);
					String url = fileInfo.filePath;
					viewCache.getPicCount().setText(categoryFiles.size() + "");
					final ImageView imageView = viewCache.getFirstImage();
					// LogUtil.log(TAG, url);
					imageView.setImageBitmap(cache);
					if (!TextUtils.isEmpty(url)) {
						imageView.setTag(url);
						mImageLoader.displayImage(url,
								new ImageProcessingCallback() {

									@Override
									public void onImageProcessing(
											WeakReference<Bitmap> weak,
											String tag) {
										try {
											if (tag.equals(imageView.getTag())) {
												Bitmap bitmap = weak.get();
												if (bitmap == null) {
													return;
												}
												bitmap = Util.iamgeRotation(
														bitmap,
														fileInfo.orientation);
												imageView
														.setImageBitmap(bitmap);
											}
										} catch (Exception e) {
											Log.e(TAG, e.getMessage());
											e.printStackTrace();
										}
									}
								});
					}
					ImageView simageView = viewCache.getSecondImage();
					if (activity.isPrivacy()
							&& fileName.equals(FileExplorerTabActivity
									.getPrivacyMedioPath(FileCategory.Picture))) {
						simageView.setImageBitmap(icon_cache);
						simageView.setVisibility(View.VISIBLE);
						if(TextUtils.isEmpty(url)){
							imageView.setImageBitmap(priCache);
						}
					} else {
						simageView.setVisibility(View.GONE);
					}
				}
				// modify by JXH 2014-8-12 begin
				if (fragment.isOperationFile()) {
					// modify by JXH 2014-8-12 end
					if (selects.contains(fileName)) {
						viewCache.getPicCheckboxYes().setVisibility(
								View.VISIBLE);
						if((position==fragment.selectPosition)&&fragment.isItemPicAnim()){
							activity.imageViewAnim(viewCache.getPicCheckboxYes(), true);
						}else {
							viewCache.getPicCheckboxYes().setImageResource(R.drawable.aurora_check14);
						}
						viewCache.getPicCheckboxNo().setVisibility(View.GONE);
					} else {
						if (activity.isPrivacy()
								&& fileName
										.equals(FileExplorerTabActivity
												.getPrivacyMedioPath(FileCategory.Picture))) {
							viewCache.getPicCheckboxNo().setVisibility(
									View.GONE);
							viewCache.getPicCheckboxYes().setVisibility(
									View.GONE);

						} else {
							viewCache.getPicCheckboxYes().setVisibility(
									View.GONE);
							viewCache.getPicCheckboxNo().setVisibility(
									View.VISIBLE);
							if((position==fragment.selectPosition)&&fragment.isItemPicAnim()){
								activity.imageViewAnim(viewCache.getPicCheckboxNo(), false);
							}else {
								viewCache.getPicCheckboxNo().setImageResource(R.drawable.aurora_check00);
							}
							
						}
					}
				} else {
					viewCache.getPicCheckboxNo().setVisibility(View.GONE);
					viewCache.getPicCheckboxYes().setVisibility(View.GONE);
				}
			}
		}

		return rowView;
	}
	
	

}