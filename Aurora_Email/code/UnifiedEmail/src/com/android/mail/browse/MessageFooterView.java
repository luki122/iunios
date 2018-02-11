/*
 * Copyright (C) 2012 Google Inc.
 * Licensed to The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mail.browse;

import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import com.android.emailcommon.provider.EmailContent.AttachmentColumns;
import com.android.email.R;
import com.android.mail.browse.AttachmentLoader.AttachmentCursor;
import com.android.mail.browse.ConversationContainer.DetachListener;
import com.android.mail.browse.ConversationViewAdapter.MessageHeaderItem;
import com.android.mail.browse.MessageAttachmentBar;
import com.android.mail.providers.Attachment;
import com.android.mail.providers.EmlAttachmentProvider;
import com.android.mail.providers.Message;
import com.android.mail.ui.AttachmentTile;
import com.android.mail.ui.AttachmentTile.AttachmentPreview;
import com.android.mail.ui.AttachmentTile.AttachmentPreviewCache;
import com.android.mail.ui.AttachmentTileGrid;
import com.android.mail.utils.AttachmentUtils;
import com.android.mail.utils.LogTag;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.MyLog;
import com.aurora.email.FujianInfo;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import android.view.animation.AnimationUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import com.android.mail.providers.UIProvider.AttachmentState;
import com.android.mail.providers.UIProvider.AttachmentType;

public class MessageFooterView extends LinearLayout implements DetachListener,
		LoaderManager.LoaderCallbacks<Cursor>, AttachmentPreviewCache {

	private MessageHeaderItem mMessageHeaderItem;
	private LoaderManager mLoaderManager;
	private FragmentManager mFragmentManager;
	private AttachmentCursor mAttachmentsCursor;
	private TextView mTitleText;
	private AttachmentTileGrid mAttachmentGrid;
	private LinearLayout mAttachmentBarList;
	//Aurora <shihao> <20150423> for attachment animation add 
	private int barHeight;
	private boolean isFirstTime = true;
	private boolean isAttachListOpen = false;
	private ValueAnimator scaleOpenAnim,scaleCloseAnim;
	private AlphaAnimation alphaOpenAnim,alphaCloseAnim;
	private final static int mDuration = 600;
	private boolean isOnClick = false;
	private boolean isTitleTextChangeColor = false;
	private Animation textInAnim,textOutAnim;
	private int textOldColor = -1;
	private String titleTextResult = null;
	//Aurora <shihao> end

	private final LayoutInflater mInflater;

	private static final String LOG_TAG = LogTag.getLogTag();

	private Uri mAccountUri;

	// add by chenhl start
	private View mAttachHead;

	// add by chenhl end

	public MessageFooterView(Context context) {
		this(context, null);
	}

	public MessageFooterView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mInflater = LayoutInflater.from(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mTitleText = (TextView) findViewById(R.id.attachments_header_text);
		mAttachHead = findViewById(R.id.attachments_header);// add by chenhl
		mAttachmentGrid = (AttachmentTileGrid) findViewById(R.id.attachment_tile_grid);
		mAttachmentBarList = (LinearLayout) findViewById(R.id.attachment_bar_list);
		// modify by JXH 2014-12-29 begin
		// findViewById(R.id.id_attachments_show).setOnClickListener(mOnClickListener);//add
		// by chenhl
		mAttachHead.setOnClickListener(mOnClickListener);
		// modify by JXH 2014-12-29 end
		
		//Aurora <shihao> <20150423> for Attachment animation begin 
	    textInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.aurora_attachment_title_in);
	    textOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.aurora_attachment_title_out);
	    textOutAnim.setAnimationListener(textAnimListener);
		
		mAttachmentBarList.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				if(mAttachmentBarList.getHeight() > 0 && isFirstTime){
					isFirstTime = false;
					barHeight = mAttachmentBarList.getHeight();
					mAttachmentBarList.getLayoutParams().height = 0;
					initAnimation();
				}
			}
		});
		//Aurora <shihao> <20150423> for Attachment animation end
	}

	public void initialize(LoaderManager loaderManager,
			FragmentManager fragmentManager) {
		mLoaderManager = loaderManager;
		mFragmentManager = fragmentManager;
	}

	public void bind(MessageHeaderItem headerItem, Uri accountUri,
			boolean measureOnly) {
		mAccountUri = accountUri;
		// MyLog.d("chenhl", "bind......1111");
		// Resets the footer view. This step is only done if the
		// attachmentsListUri changes so that we don't
		// repeat the work of layout and measure when
		// we're only updating the attachments.
		if (mMessageHeaderItem != null
				&& mMessageHeaderItem.getMessage() != null
				&& mMessageHeaderItem.getMessage().attachmentListUri != null
				&& !mMessageHeaderItem.getMessage().attachmentListUri
						.equals(headerItem.getMessage().attachmentListUri)) {
			mAttachmentGrid.removeAllViewsInLayout();
			mAttachmentBarList.removeAllViewsInLayout();
			// mTitleText.setVisibility(View.GONE);
			mAttachHead.setVisibility(View.GONE);// add by chenhl

			mAttachmentGrid.setVisibility(View.GONE);
			mAttachmentBarList.setVisibility(View.GONE);
		}

		// If this MessageFooterView is being bound to a new attachment, we need
		// to unbind with the
		// old loader
		final Integer oldAttachmentLoaderId = getAttachmentLoaderId();

		mMessageHeaderItem = headerItem;

		final Integer attachmentLoaderId = getAttachmentLoaderId();
		// Destroy the loader if we are attempting to load a different
		// attachment
		if (oldAttachmentLoaderId != null
				&& !Objects.equal(oldAttachmentLoaderId, attachmentLoaderId)) {
			mLoaderManager.destroyLoader(oldAttachmentLoaderId);
		}

		// kick off load of Attachment objects in background thread
		// but don't do any Loader work if we're only measuring
		if (!measureOnly && attachmentLoaderId != null) {
			LogUtils.i(LOG_TAG,
					"binding footer view, calling initLoader for message %d",
					attachmentLoaderId);
			mLoaderManager.initLoader(attachmentLoaderId, Bundle.EMPTY, this);
		}

		// Do an initial render if initLoader didn't already do one
		if (mAttachmentGrid.getChildCount() == 0
				&& mAttachmentBarList.getChildCount() == 0) {
			renderAttachments(false);
		}
		setVisibility(mMessageHeaderItem.isExpanded() ? VISIBLE : GONE);
	}

	private void renderAttachments(boolean loaderResult) {
		final List<Attachment> attachments;
		if (mAttachmentsCursor != null && !mAttachmentsCursor.isClosed()) {
			int i = -1;
			attachments = Lists.newArrayList();
			while (mAttachmentsCursor.moveToPosition(++i)) {
				attachments.add(mAttachmentsCursor.get());
			}
		} else {
			// before the attachment loader results are in, we can still render
			// immediately using
			// the basic info in the message's attachmentsJSON
			attachments = mMessageHeaderItem.getMessage().getAttachments();
		}
		renderAttachments(attachments, loaderResult);
	}

	private void renderAttachments(List<Attachment> attachments,
			boolean loaderResult) {
		if (attachments == null || attachments.isEmpty()) {
			return;
		}

		// filter the attachments into tiled and non-tiled
		final int maxSize = attachments.size();
		// delete by chenhl
		/*
		 * final List<Attachment> tiledAttachments = new
		 * ArrayList<Attachment>(maxSize); final List<Attachment> barAttachments
		 * = new ArrayList<Attachment>(maxSize);
		 * 
		 * for (Attachment attachment : attachments) { if
		 * (AttachmentTile.isTiledAttachment(attachment)) {
		 * tiledAttachments.add(attachment); } else {
		 * barAttachments.add(attachment); } }
		 */
		mMessageHeaderItem.getMessage().attachmentsJson = Attachment
				.toJSONArray(attachments);

		// add by chenhl start
		// mTitleText.setVisibility(View.VISIBLE);
		showAttachmentSize(attachments, loaderResult);

		// add by chenhl end 20141105

		// renderTiledAttachments(tiledAttachments, loaderResult);//delete by
		// chenhl
		renderBarAttachments(attachments, loaderResult); // modify by chenhl
	}

	private void renderTiledAttachments(List<Attachment> tiledAttachments,
			boolean loaderResult) {
		mAttachmentGrid.setVisibility(View.VISIBLE);

		// Setup the tiles.
		mAttachmentGrid.configureGrid(mFragmentManager,
				mMessageHeaderItem.getMessage().attachmentListUri,
				tiledAttachments, loaderResult);
	}

	private AttachmentActionHandler mActionHandler;// paul add

	private void renderBarAttachments(List<Attachment> barAttachments,
			boolean loaderResult) {
		// mAttachmentBarList.setVisibility(View.VISIBLE);//del by chenhl

		for (final Attachment attachment : barAttachments) {
			// paul add
			// MyLog.d("chenhl",attachment.getName() + " attachment.type : " +
			// attachment.type);
			if (attachment.type == AttachmentType.INLINE_CURRENT_MESSAGE) {
				// MyLog.i("chenhl","attachment.state : " + attachment.state);
				if (attachment.state == AttachmentState.PAUSED
						|| attachment.state == AttachmentState.NOT_SAVED) {
					if (null == mActionHandler)
						mActionHandler = new AttachmentActionHandler(
								getContext(), null);
					if (loaderResult) {
						mActionHandler.setAttachment(attachment);
						mActionHandler
								.startDownloadingAttachment(attachment.destination);
					}
				}
				continue;
			}
			// MyLog.d("chenhl", "renderBarAttachments...");
			final Uri id = attachment.getIdentifierUri();
			MessageAttachmentBar barAttachmentView = (MessageAttachmentBar) mAttachmentBarList
					.findViewWithTag(id);

			if (barAttachmentView == null) {
				barAttachmentView = MessageAttachmentBar.inflate(mInflater,
						this);
				barAttachmentView.setTag(id);
				barAttachmentView.initialize(mFragmentManager);
				//Aurora <shihao> <20150423> for Attachment animation begin
				LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//				mAttachmentBarList.addView(barAttachmentView);
				mAttachmentBarList.addView(barAttachmentView,params);
				//Aurora <shihao> <20150423> for Attachment animation end
			}

			barAttachmentView.render(attachment, mAccountUri, loaderResult,
					this);
			// Add by JXH
			openOrReDownloadAttachment(barAttachmentView, attachment);
		}
	}

	/**
	 * @author JXH
	 * @param uri
	 *            content://com.android.email.provider/uiattachment/8
	 * @return content://com.android.email.provider/attachment/8
	 */
	private Uri getAttachmentUri(Uri uri) {
		if (uri == null) {
			return uri;
		}
		return Uri.parse(uri.toString().replace("ui", ""));
	}

	/**
	 * 打开附件，重新下载附件
	 * 
	 * @author JXH
	 * @param barAttachmentView
	 * @param attachment
	 */
	private void openOrReDownloadAttachment(
			final MessageAttachmentBar barAttachmentView, final Attachment attachment) {
		final Uri id = attachment.getIdentifierUri();
		barAttachmentView.setClickable(true);
		barAttachmentView.setLongClickable(true);
		barAttachmentView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (attachment.isSavedToExternal()
						&& attachment.state == AttachmentState.SAVED) {
					Cursor cursor = null;
					// id content://com.android.email.provider/uiattachment/8
					try {
						cursor = getContext().getContentResolver().query(
								getAttachmentUri(id),
								new String[] { AttachmentColumns.CONTENT },
								null, null, null);
						if (cursor != null && cursor.moveToFirst()) {
							String path = cursor.getString(cursor
									.getColumnIndexOrThrow(AttachmentColumns.CONTENT));
//							Log.d("JXH", "path:" + path + "  id:" + id);
							Intent intent = new Intent();
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							intent.setAction(android.content.Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(new File(path)),
									attachment.getContentType());
							getContext().startActivity(intent);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (cursor != null && !cursor.isClosed()) {
							cursor.close();
						}
					}
				}
//				Log.d("JXH", "attachment:" + attachment.debugToString());
			}
		});
		barAttachmentView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (attachment.isSavedToExternal()
						&& attachment.state == AttachmentState.SAVED&&attachment.supportsDownloadAgain()) {
					if (null == mActionHandler)
						mActionHandler = new AttachmentActionHandler(
								getContext(), null);
						mActionHandler.setAttachment(attachment);
						mActionHandler
								.startRedownloadingAttachment(attachment);
				}
				return true;
			}
		});
	}

	private Integer getAttachmentLoaderId() {
		Integer id = null;
		final Message msg = mMessageHeaderItem == null ? null
				: mMessageHeaderItem.getMessage();
		if (msg != null && 0 != msg.hasAttachments
				&& msg.attachmentListUri != null) {// paul modify
			id = msg.attachmentListUri.hashCode();
		}
		return id;
	}

	@Override
	public void onDetachedFromParent() {
		// Do nothing
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new AttachmentLoader(getContext(),
				mMessageHeaderItem.getMessage().attachmentListUri);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAttachmentsCursor = (AttachmentCursor) data;

		if (mAttachmentsCursor == null || mAttachmentsCursor.isClosed()) {
			return;
		}

		renderAttachments(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAttachmentsCursor = null;
	}

	// add by chenhl start
	private final HashMap<String, AttachmentPreview> mAttachmentPreviews = Maps
			.newHashMap();

	private void showAttachmentSize(List<Attachment> attachments,
			boolean loaderResult) {
		if (!loaderResult || mTitleText == null) {
			return;
		}
		textOldColor = mTitleText.getCurrentTextColor();//Aurora <shihao> <20150423> for Attachment animation begin 
		
		final int maxSize = attachments.size();
		int filesize = 0;
		int downloadingCount = 0; // 正在下载文件总数
		int downloadedCount = 0;// 已下载文件总数
		int downloadPercent = 0; // 下载百分比
		int attachmentCount = 0;
		String result;
		for (int i = 0; i < maxSize; i++) {
			Attachment attachment = attachments.get(i);
			if (attachment.type == AttachmentType.INLINE_CURRENT_MESSAGE) {

				continue;
			}
			attachmentCount++;
			filesize += attachment.size;
			// MyLog.d("chenhl",
			// "attachment.size:"+attachment.size+" downloadedSize:"+attachment.downloadedSize+" state:"+attachment.state+" name:"+attachment.getName());
			if (attachment.isDownloading()) {
				// 正在下载
				downloadingCount++;
				if (attachment.downloadedSize > 0) {
					downloadPercent = (int) (((float) attachment.downloadedSize / (float) attachment.size) * 100);
				}
			} else if (attachment.isSavedToExternal()) {
				// 下载完成
				downloadedCount++;
			}
		}
		if (attachmentCount == 0) {
			mAttachHead.setVisibility(View.GONE);// add by chenhl
			return;
		} else {
			mAttachHead.setVisibility(View.VISIBLE);// add by chenhl
		}
		//Aurora <shihao> <20150423> for Attachment animation begin 
//		if (downloadingCount + downloadedCount > 0
//				&& mAttachmentBarList.getVisibility() != View.VISIBLE) {
		if (downloadingCount + downloadedCount > 0
				&& !isAttachListOpen) {  
		//Aurora <shihao> <20150423> for Attachment animation end
			if (downloadingCount > 0) {
				result = getContext().getString(
						R.string.aurora_attachment_donwload_percent,
						(downloadingCount + downloadedCount) + "/"
								+ attachmentCount, downloadPercent + "%");
			} else if (downloadedCount == attachmentCount) {
				result = getContext().getString(
						R.string.aurora_attachment_donwload_info,
						attachmentCount);
			} else {
				result = getContext().getString(
						R.string.aurora_attachment_info,
						(downloadingCount + downloadedCount) + "/"
								+ attachmentCount,
						FujianInfo.FormetFileSize(filesize));
			}
			//Aurora <shihao> <20150423> for Attachment animation begin
			if(isOnClick){
				if(textOldColor != getTextGreenColor())
					isTitleTextChangeColor = true;
				else
					isTitleTextChangeColor = false;
			}else
				setTitleGreenColor();
			//Aurora <shihao> <20150423> for Attachment animation end
		} else {
			result = getContext().getString(R.string.aurora_attachment_info,
					String.valueOf(attachmentCount),
					FujianInfo.FormetFileSize(filesize));
			//Aurora <shihao> <20150423> for Attachment animation begin 
			if(isOnClick){
				if(textOldColor != getTextDefaultColor()) //if textColor has changed
					isTitleTextChangeColor = true;
				else
					isTitleTextChangeColor = false;
			}else
				setTitleDefaultColor();
			//Aurora <shihao> <20150423> for Attachment animation end
		}
		//Aurora <shihao> <20150423> for Attachment animation begin 
		if(isOnClick && isTitleTextChangeColor){
			titleTextResult = result;
			mTitleText.startAnimation(textOutAnim);
			isOnClick = false;
			isTitleTextChangeColor = false;
		}else{
			mTitleText.setText(result);
		}
		//Aurora <shihao> <20150423> for Attachment animation end
//		mTitleText.setText(result);
		// return result;
	}

	private String getAttachmentSize(List<Attachment> attachments) {
		int size = 0;
		for (int i = 0; i < attachments.size(); i++) {
			size += attachments.get(i).size;
		}
		return FujianInfo.FormetFileSize(size);
	}

	@Override
	public void set(Attachment attachment, Bitmap preview) {

		final String attachmentIdentifier = attachment.getIdentifierUri()
				.toString();
		if (attachmentIdentifier != null) {
			mAttachmentPreviews.put(attachmentIdentifier,
					new AttachmentPreview(attachment, preview));
		}
	}

	@Override
	public Bitmap get(Attachment attachment) {

		final String attachmentIdentifier = attachment.getIdentifierUri()
				.toString();
		if (attachmentIdentifier != null) {
			final AttachmentPreview attachmentPreview = mAttachmentPreviews
					.get(attachmentIdentifier);
			if (attachmentPreview != null) {
				return attachmentPreview.preview;
			}
		}
		return null;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			//Aurora <shihao> <20150423> for Attachment animation begin 
			startAttachmentAnimation();
			isOnClick = true;
			/*mAttachmentBarList
					.setVisibility(mAttachmentBarList.getVisibility() == View.VISIBLE ? View.GONE
							: View.VISIBLE);*/
			//Aurora <shihao> <20150423> for Attachment animation end
			renderAttachments(true);
		}
	};
	// add by chenhl end
	
	//Aurora <shihao> <20150423> for animation of Attchment begin
	public void startAttachmentAnimation(){
		if(isAttachmentBarListVisible()){
			isAttachListOpen = false;
			scaleCloseAnim.start();
			mAttachmentBarList.startAnimation(alphaCloseAnim);
		}else{
			isAttachListOpen = true;
			scaleOpenAnim.start();
			mAttachmentBarList.startAnimation(alphaOpenAnim);
		}
	}
	
	public boolean isAttachmentBarListVisible(){
		return mAttachmentBarList.getHeight() > 0;
	}
	
	public void initAnimation(){
		Log.i("shihao","MessageFooterView-->initAnimation barHeight =="+barHeight);
		scaleOpenAnim = ValueAnimator.ofInt(0,barHeight);
		scaleOpenAnim.setDuration(mDuration);
		scaleOpenAnim.setInterpolator(new AccelerateDecelerateInterpolator());
		scaleOpenAnim.addUpdateListener(updateListener);
		
		scaleCloseAnim = ValueAnimator.ofInt(barHeight,0);
		scaleCloseAnim.setDuration(mDuration);
		scaleCloseAnim.setInterpolator(new AccelerateDecelerateInterpolator());
		scaleCloseAnim.addUpdateListener(updateListener);
		
		alphaOpenAnim = new AlphaAnimation(0.0f, 1.0f);
		alphaOpenAnim.setDuration(mDuration+1400);
		alphaOpenAnim.setInterpolator(new DecelerateInterpolator());
		alphaOpenAnim.setFillAfter(true);
		
		alphaCloseAnim = new AlphaAnimation(1.0f, 0.0f);
		alphaCloseAnim.setDuration(mDuration);
		alphaCloseAnim.setInterpolator(new LinearInterpolator());
		alphaCloseAnim.setFillAfter(true);
	}
	
	AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {
		
		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			// TODO Auto-generated method stub
			int height = (Integer) arg0.getAnimatedValue();
			mAttachmentBarList.getLayoutParams().height = height;
			mAttachmentBarList.requestLayout();
		}
	};	
	
	AnimationListener textAnimListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animation arg0) {
			// TODO Auto-generated method stub
			boolean isDefaultColor = getTextDefaultColor() == textOldColor;
			if(isDefaultColor)
				setTitleGreenColor();
			else
				setTitleDefaultColor();
			mTitleText.setText(titleTextResult);
			mTitleText.startAnimation(textInAnim);
		}
	};
	
	public int getTextGreenColor(){
//		return Color.parseColor("#019c73");
		return getResources().getColor(R.color.aurora_attchament_downloaded_textColor);
	}
	public int getTextDefaultColor(){
		return Color.parseColor("#aaaaaa");
	}
	
	public void setTitleGreenColor(){
		mTitleText.setTextColor(getTextGreenColor());
	}
	public void setTitleDefaultColor(){
		mTitleText.setTextColor(getTextDefaultColor());
	}
	//Aurora <shihao> <20150423> for animation of Attchment end
}
