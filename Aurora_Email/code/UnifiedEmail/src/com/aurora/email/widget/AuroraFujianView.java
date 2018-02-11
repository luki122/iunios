package com.aurora.email.widget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aurora.email.AuroraComposeActivity;
import com.aurora.email.FujianInfo;
import com.aurora.email.adapters.AuroraFujianListAdapter;
import com.aurora.email.adapters.AuroraFujianListAdapter.OnfujianDeleteListener;
import com.android.mail.R;
import com.android.mail.providers.Account;
import com.android.mail.providers.Attachment;
import com.android.mail.providers.UIProvider.AttachmentType;
import com.android.mail.ui.AttachmentTile.AttachmentPreview;
import com.android.mail.utils.AttachmentUtils;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.MyLog;

public class AuroraFujianView extends LinearLayout implements
		View.OnClickListener {

	private static final String TAG = "AuroraComposeActivity";
	private View fujianLayout;
	private TextView fujianInfo;
	private AuroraFujianListAdapter mAuroraFujianListAdapter;
	private LinearLayoutForListView mListView;
	private Context mContext;
	private Account mAccount;
	private ImageView mshowButton;
	private boolean isMoreSize=false;
	private FocusControl mFocusControl;
	
	public AuroraFujianView(Context context) {
		super(context);
		init(context);
	}

	public AuroraFujianView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@SuppressLint("NewApi")
	public AuroraFujianView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.aurora_fujian_layout,
				this);
		fujianLayout = findViewById(R.id.id_fujian_layout);
		fujianInfo = (TextView) findViewById(R.id.id_fujian_info);
		mListView = (LinearLayoutForListView) findViewById(R.id.id_test_list);
		mshowButton=(ImageView)findViewById(R.id.id_show);
		//modify by JXH 2014-12-29 begin
//		mshowButton.setOnClickListener(this);
		fujianLayout.setOnClickListener(this);
		//modify by JXH 2014-12-29 end

		mAuroraFujianListAdapter = new AuroraFujianListAdapter(context);
		mAuroraFujianListAdapter
				.setOnfujianDeleteListener(mOnfujianDeleteListener);
		mListView.setAdapter(mAuroraFujianListAdapter);
	}

	private AuroraFujianListAdapter.OnfujianDeleteListener mOnfujianDeleteListener = new OnfujianDeleteListener() {

		@Override
		public void OnfujianDelete(int postion) {
			if (mAuroraFujianListAdapter == null) {
				return;
			}

			// fujianInfo.setText(count + "个附件");
			updateFujianSize();
		}
	};

	private void showFujian() {
		if (mListView != null) {
			mListView.setVisibility(View.VISIBLE);
			mshowButton.setImageResource(R.drawable.aurora_attachment_more_up_normal);
		}
	}

	public boolean hideFujian() {
		if (mListView != null) {
			if (mListView.getVisibility() == View.VISIBLE) {
				mListView.setVisibility(View.GONE);
				mshowButton.setImageResource(R.drawable.aurora_attachment_more);
				return true;
			}
		}
		return false;
	}

	/*
	 * public void addFujian(FujianInfo info) { if (mAuroraFujianListAdapter !=
	 * null) { mAuroraFujianListAdapter.addFujian(info); } }
	 */

	public int getFujianCount() {
		if (mAuroraFujianListAdapter != null) {
			return mAuroraFujianListAdapter.getCount();
		}
		return 0;
	}

	public void notifyDataSetChanged() {
		if (mAuroraFujianListAdapter != null) {
			mAuroraFujianListAdapter.notifyDataSetChanged();
		}
	}

	public void showFujianLayout() {
		fujianLayout.setVisibility(View.VISIBLE);
		fujianInfo.setText(mAuroraFujianListAdapter.getCount() + "个附件");
	}

	public LinearLayoutForListView getFujianListView() {

		return mListView;

	}

	private Activity getActivity() {
		return (Activity) mContext;
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
/*		case R.id.id_show:
			hideInputMethod(mContext, getActivity().getCurrentFocus());
			if (getFujianCount() > 0) {
				//getActivity().findViewById(R.id.id_theme_info).requestFocus();
				//mshowButton.requestFocusFromTouch();
				showFujian();
			}
			break;*///del by JXH 2014-12-29 for new requirement
		case R.id.id_fujian_layout:
			hideInputMethod(mContext, getActivity().getCurrentFocus());
			if (getFujianCount() > 0) {
				//getActivity().findViewById(R.id.id_theme_info).requestFocus();
				//mshowButton.requestFocusFromTouch();
				showFujian();
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 隐藏键盘
	 * 
	 * @param context
	 * @param v
	 * @return
	 */
	public static Boolean hideInputMethod(Context context, View v) {
		if (v == null) {
			return false;
		}
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		return false;
	}

	public Attachment generateLocalAttachment(Uri contentUri) {
		//MyLog.d(TAG, "generateLocalAttachment:" + contentUri);
		final ContentResolver contentResolver = getContext()
				.getContentResolver();
		String contentType = contentResolver.getType(contentUri);
		//MyLog.d(TAG, "contentType:" + contentType);
		if (contentUri == null || TextUtils.isEmpty(contentUri.getPath())) {
			return null;
		}
		if (contentType == null)
			contentType = "";
		final Attachment attachment = new Attachment();
		attachment.uri = null; // URI will be assigned by the provider upon
								// send/save
		attachment.setName(null);
		attachment.size = 0;
		attachment.contentUri = contentUri;
		attachment.thumbnailUri = contentUri;
		Cursor metadataCursor = null;
		try {
			metadataCursor = contentResolver.query(contentUri, new String[] {
					OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE }, null,
					null, null);
			if (metadataCursor != null) {
				try {
					if (metadataCursor.moveToNext()) {
						String nameString = metadataCursor.getString(0);
						int size = metadataCursor.getInt(1);
				/*		MyLog.d(TAG, "nameString:" + nameString + " size:"
								+ size);*/
						attachment.setName(nameString);
						attachment.size=size;
					}
				} finally {
					metadataCursor.close();
				}
			}
		} catch (SQLiteException ex) {

		}
		if (attachment.getName() == null) {
			attachment.setName(contentUri.getLastPathSegment());
		}
		if (attachment.size == 0) {
			// if the attachment is not a content:// for example, a file:// URI
			attachment.size = getSizeFromFile(contentUri, contentResolver);
		}

		attachment.setContentType(contentType);
		return attachment;
	}

	public long addAttachment(Account account, Uri uri) {
	//	MyLog.d(TAG, "addAttachment:"+uri);
		Attachment attachment = generateLocalAttachment(uri);
		return addAttachment(account, attachment);
	}

	public long addAttachment(Account account, Attachment attachment) {
		if (attachment == null || mAuroraFujianListAdapter == null) {
			return 0;
		}
		
		if(attachment.type==AttachmentType.INLINE_CURRENT_MESSAGE||attachment.flags==Attachment.FLAG_DUMMY_ATTACHMENT){
			return 0;
		}
		
		boolean result = false;

		mAccount = account;
		// Error getting the size or the size was too big.
		if (mAuroraFujianListAdapter != null) {
			result = mAuroraFujianListAdapter.addFujian(attachment);
			showFujian();
		}
		if (result) {
			updateFujianSize();
		}
		
		return attachment.size;
	}

	public ArrayList<Attachment> getAttachments() {
		if (mAuroraFujianListAdapter != null) {
			return mAuroraFujianListAdapter.getAttachments();
		}
		return null;
	}

	public ArrayList<AttachmentPreview> getAttachmentPreviews() {

		if (mAuroraFujianListAdapter != null) {
			return mAuroraFujianListAdapter.getAttachmentPreviews();
		}
		return null;
	}

	public void setAttachmentPreviews(ArrayList<AttachmentPreview> previews) {
		if (mAuroraFujianListAdapter != null) {
			mAuroraFujianListAdapter.setAttachmentPreviews(previews);
		}
	}

	private void updateFujianSize() {

		if (mAccount == null || mAuroraFujianListAdapter == null) {
			return;
		}

		final int maxSize = mAccount.settings.getMaxAttachmentSize();
//		MyLog.d(TAG, "maxSize:" + maxSize);
		if (mAuroraFujianListAdapter.getTotalAttachmentsSize() > maxSize) {
			// 提示超过最大限制附件
			isMoreSize = true;
			//modfiy by JXH 2015-1-8 begin
			String txt = mContext.getString(R.string.file_overflow);
			int count = mAuroraFujianListAdapter.getCount();
			if(count==1){
				txt = mContext.getString(R.string.file_overflow_single);
				txt = String.format(txt, AttachmentUtils.convertToHumanReadableSize(getActivity(), maxSize));
			}else if(count>1){
				txt = String.format(txt,count, AttachmentUtils.convertToHumanReadableSize(getActivity(), maxSize));
			}
			fujianInfo.setText(txt);
//			fujianInfo.setText("!" + mAuroraFujianListAdapter.getCount()
//					+ "个附件,已超上限" + AttachmentUtils.convertToHumanReadableSize(getActivity(), maxSize));
			//modfiy by JXH 2015-1-8 end
			fujianInfo.setTextColor(Color.RED);
		} else {
			isMoreSize=false;
			//modfiy by JXH 2015-1-8 begin
			String txt =mContext.getString(R.string.file_normal);
			int count = mAuroraFujianListAdapter.getCount();
			if(count==1){
				txt = mContext.getString(R.string.file_normal_single);
			}else if(count>1){
				txt = String.format(txt, mAuroraFujianListAdapter.getCount());
			}
			fujianInfo.setText(txt);
//			fujianInfo.setText(mAuroraFujianListAdapter.getCount() + "个附件");
			//modfiy by JXH 2015-1-8 end
//			fujianInfo.setTextColor(Color.parseColor("#019c73"));
			//Aurora <shihao> for N2 
			fujianInfo.setTextColor(getResources().getColor(R.color.aurora_attchament_downloaded_textColor));
		}

		int count = mAuroraFujianListAdapter.getCount();
		if (count == 0) {
			fujianLayout.setVisibility(View.GONE);
			hideFujian();
		} else {
			fujianLayout.setVisibility(View.VISIBLE);
		}
		if(mFocusControl!=null){
			mFocusControl.moveFocus(AuroraComposeActivity.ATTACHMENT_MORE_SIZE);
		}
	}

	public boolean isMoreSize(){
		return isMoreSize;
	}
	
	public void clearCache() {
		if (mAuroraFujianListAdapter != null) {
			mAuroraFujianListAdapter.clearCache();
		}
	}

	private static int getSizeFromFile(Uri uri, ContentResolver contentResolver) {
		int size = -1;
		ParcelFileDescriptor file = null;
		try {
			file = contentResolver.openFileDescriptor(uri, "r");
			size = (int) file.getStatSize();
		} catch (FileNotFoundException e) {
			LogUtils.w(TAG, e, "Error opening file to obtain size.");
		} finally {
			try {
				if (file != null) {
					file.close();
				}
			} catch (IOException e) {
				LogUtils.w(TAG, "Error closing file opened to obtain size.");
			}
		}
		// We only want to return a non-negative value.
		// (ParcelFileDescriptor#getStatSize() will
		// return -1 if the fd is not a file
		return Math.max(size, 0);
	}
	
	public void setFocusControl(FocusControl control) {
		mFocusControl = control;
	}
}
