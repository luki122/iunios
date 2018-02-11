package com.aurora.email.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.android.mail.ui.AttachmentBitmapHolder;
import com.android.mail.ui.AttachmentTile.AttachmentPreview;
import com.android.mail.ui.AttachmentTile.AttachmentPreviewCache;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.MyLog;
import com.aurora.email.AuroraThumLoadTask;
import com.aurora.email.FujianInfo;
import com.aurora.email.MimeTypeUtil;
import com.android.mail.R;
import com.android.mail.providers.Attachment;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.android.ex.photo.util.ImageUtils;

public class AuroraFujianListAdapter extends BaseAdapter implements
		AttachmentPreviewCache {

	private static final String TAG = "AuroraComposeActivity";
	private Context mContext;
	private ArrayList<Attachment> datas = new ArrayList<Attachment>();
	private OnfujianDeleteListener mOnfujianDeleteListener;
	private final HashMap<String, AttachmentPreview> mAttachmentPreviews;
	
	
	public AuroraFujianListAdapter(Context context) {
		this.mContext = context;
		mAttachmentPreviews = Maps.newHashMap();
	}

	public boolean addFujian(Attachment info) {
		if (!datas.contains(info)) {
			datas.add(info);
			return true;
		}
	//	MyLog.d(TAG, "datas contain attachement!");
		return false;
	}

	@Override
	public int getCount() {

		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {

		return datas.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
/*		MyLog.d("AuroraComposeActivity", "getView.......:" + arg0 + " arg1:"
				+ arg1);*/
		HoldView holdView;
		final int postion = arg0;
		if (arg1 == null) {
			holdView = new HoldView();
			arg1 = LayoutInflater.from(mContext).inflate(
					R.layout.aurora_fujian_list_item, null);
			holdView.inconView = (ImageView) arg1.findViewById(R.id.id_icon);
			holdView.name = (TextView) arg1.findViewById(R.id.id_name);
			holdView.size = (TextView) arg1.findViewById(R.id.id_size);
			holdView.deleteView = (ImageView) arg1.findViewById(R.id.id_delete);

			arg1.setTag(holdView);
		} else {
			holdView = (HoldView) arg1.getTag();
		}
		Attachment info = datas.get(arg0);
		if (info != null) {
			holdView.name.setText(info.getName());
			holdView.size.setText("("+FujianInfo.FormetFileSize(info.size)+")");
		}
		holdView.deleteView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				MyLog.d(TAG, "deleteView:" + postion);
				datas.remove(postion);
				notifyDataSetChanged();
				if (mOnfujianDeleteListener != null) {
					mOnfujianDeleteListener.OnfujianDelete(postion);
				}
			}
		});

		holdView.render(info, this);
		return arg1;
	}

	class HoldView implements AttachmentBitmapHolder {
		ImageView inconView;
		TextView name;
		TextView size;
		ImageView deleteView;

		AuroraThumLoadTask mAuroraThumLoadTask;
		Attachment mAttachment;
		AttachmentPreviewCache mAttachmentPreviewCache;
		boolean mDefaultThumbnailSet;
		private static final float skinnyThresholdRatio = 0.5f;

		public HoldView() {
			mDefaultThumbnailSet = true;
		}

		public void render(Attachment attachment,
				AttachmentPreviewCache attachmentPreviewCache) {
			final Attachment prevAttachment = mAttachment;
			MyLog.d(TAG, "render attachment:" + attachment.getName());
			mAttachment = attachment;
			mAttachmentPreviewCache = attachmentPreviewCache;
			if ((attachment.flags & Attachment.FLAG_DUMMY_ATTACHMENT) != 0) {
				// 显示加载更多
				return ;
			}

			// 判断缓存是否存在
			Bitmap cachedPreview = mAttachmentPreviewCache.get(mAttachment);
			if (cachedPreview != null) {
				// inconView.setScaleType(ScaleType.CENTER);
				inconView.setImageBitmap(cachedPreview);
			} else {
				AuroraThumLoadTask.setupThumbnailPreview(mAuroraThumLoadTask,
						this, attachment, prevAttachment,mContext);
			}
		}

		@Override
		public int getThumbnailWidth() {
			// return inconView.getMeasuredWidth();
	//		MyLog.d(TAG, "getThumbnailWidth:" + mContext.getResources().getDimensionPixelSize(R.dimen.aurora_img_size));
			return mContext.getResources().getDimensionPixelSize(R.dimen.aurora_img_size);
		}

		@Override
		public int getThumbnailHeight() {

			// return inconView.getHeight();
			return mContext.getResources().getDimensionPixelSize(R.dimen.aurora_img_size);
		}

		@Override
		public void setThumbnail(Bitmap result) {
			if (result == null) {
				return;
			}
			// inconView.setScaleType(ScaleType.CENTER);
			inconView.setImageBitmap(result);
			mAttachmentPreviewCache.set(mAttachment, result);
			mDefaultThumbnailSet = false;
		}

		@Override
		public void setThumbnailToDefault() {
			String contentType=mAttachment.getContentType();
		//	MyLog.d(TAG, "name:"+mAttachment.getName()+" getContentType():"+contentType);
			if(contentType==null||TextUtils.isEmpty(contentType)){
				return;
			}
			if(ImageUtils.isImageMimeType(contentType)){
				inconView.setImageResource(R.drawable.aurora_file_icon_picture_light);
			}else if(MimeTypeUtil.isApkMimeType(contentType)){
				inconView.setImageResource(R.drawable.aurora_gn_search_file_icon_apk_light);
			}else{
				MimeTypeUtil.showDefualtbyExtension(mAttachment.getName(),inconView);
			}
			mDefaultThumbnailSet = true;
		}

		@Override
		public ContentResolver getResolver() {

			return mContext.getContentResolver();
		}

		@Override
		public boolean bitmapSetToDefault() {

			return mDefaultThumbnailSet;
		}

		@Override
		public void thumbnailLoadFailed() {
			setThumbnailToDefault();
		}
	}

	public void setOnfujianDeleteListener(OnfujianDeleteListener l) {
		mOnfujianDeleteListener = l;
	}

	public interface OnfujianDeleteListener {
		public void OnfujianDelete(int postion);
	}

	/**
	 * Get the total size of all attachments currently in this view.
	 */
	public long getTotalAttachmentsSize() {
		long totalSize = 0;
		for (Attachment attachment : datas) {
			totalSize += attachment.size;
		}
		return totalSize;
	}

	public ArrayList<Attachment> getAttachments() {
		return datas;
	}

	@Override
	public void set(Attachment attachment, Bitmap preview) {

		final String attachmentIdentifier = attachment.getIdentifierUri()
				.toString();
	//	MyLog.d(TAG, "set attachmentIdentifier:" + attachmentIdentifier);
		if (attachmentIdentifier != null) {
			mAttachmentPreviews.put(attachmentIdentifier,
					new AttachmentPreview(attachment, preview));
		}
	}

	@Override
	public Bitmap get(Attachment attachment) {
		final String attachmentIdentifier = attachment.getIdentifierUri()
				.toString();
//		MyLog.d(TAG, "get attachmentIdentifier:" + attachmentIdentifier);
		if (attachmentIdentifier != null) {
			final AttachmentPreview attachmentPreview = mAttachmentPreviews
					.get(attachmentIdentifier);
			if (attachmentPreview != null) {
				return attachmentPreview.preview;
			}
		}
		return null;
	}

	/**
	 * 清理缓存
	 */
	public void clearCache() {
		Iterator<AttachmentPreview> iterator = mAttachmentPreviews.values()
				.iterator();
		while (iterator.hasNext()) {
			Bitmap preview = iterator.next().preview;
			if (preview != null) {
				preview.recycle();
				preview = null;
			}
		}
		mAttachmentPreviews.clear();
	}

	public ArrayList<AttachmentPreview> getAttachmentPreviews() {
		return Lists.newArrayList(mAttachmentPreviews.values());
	}

	public void setAttachmentPreviews(ArrayList<AttachmentPreview> previews) {
		if (previews != null) {
			for (AttachmentPreview preview : previews) {
				mAttachmentPreviews.put(preview.attachmentIdentifier, preview);
			}
		}
	}
	
}
