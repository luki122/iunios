package com.android.mail.ui;

import com.android.mail.R;
import android.view.View;
import android.content.Context;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import com.android.mail.providers.Conversation;


public class AuroraWholeConversationListItem {
	
	private Context mContext;
	private View mRoot;
	
	private TextView mSenderTextView;
	private TextView mUnreadCountTextView;
	private TextView mSubjectTextView;
	private TextView mContentTextView;
	
	private Conversation mConversation;
	
	private boolean mIsChecked = false;
	
	public AuroraWholeConversationListItem(Context context, View root) {
		mContext = context;
		mRoot = root;
		mSenderTextView = (TextView)root.findViewById(R.id.aurora_conv_list_item_sender);
		mUnreadCountTextView = (TextView)root.findViewById(R.id.aurora_conv_list_item_unread_count);
		mSubjectTextView = (TextView)root.findViewById(R.id.aurora_conv_list_item_subject);
		mContentTextView = (TextView)root.findViewById(R.id.aurora_conv_list_item_snippet);
	}
	
	public void setSender(String sender) {
		mSenderTextView.setText(sender);
	}
//	
//	public void setUnreadCount(String unreadCount) {
//		mUnreadCountTextView.setText(unreadCount);
//	}
	
	public void setUnreadCount(int unreadCount) {
		if(unreadCount != 0) {
			mUnreadCountTextView.setText(String.format("%d", unreadCount));
		} else {
			mUnreadCountTextView.setText("");
		}
	}
	
	public void setSubject(String subject) {
		mSubjectTextView.setText(subject);
	}
	
	public void setSnippet(String snippet) {
		mContentTextView.setText(snippet);
	}
	
	public void setHasAttachmentSign(boolean has) {
		if(has) {
			Drawable drawableRight = mContext.getResources().getDrawable(R.drawable.ic_attachment_holo_light);
			mSubjectTextView.setCompoundDrawables (null, null, drawableRight, null);
		} else {
			mSubjectTextView.setCompoundDrawables (null, null, null, null);
		}
	}
	
	public void setConversation(Conversation conv) {
		mConversation = conv;
	}
	
	public Conversation getConversation() {
		return mConversation;
	}
	
	public void setIsChecked(boolean checked) {
		mIsChecked = checked;
	}
	
	public boolean getIsChecked() {
		return mIsChecked;
	}
}