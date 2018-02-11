package com.aurora.email.widget;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Rfc822Token;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mail.R;
import com.android.mail.browse.EmailAddressSpan;
import com.android.mail.providers.Account;
import com.android.mail.providers.Address;
import com.aurora.email.widget.AuroraEditTextStateView.AuroraListener;

import java.util.ArrayList;

public class AuroraDetailAddressView extends LinearLayout implements
		AuroraListener, OnClickListener {

	AuroraInnerScrollView mInnerScrollView;
	private Context mContext;
	AuroraEditTextStateView mAuroraEditTextStateView;
	private TextView mTitleTextView;
	private TextView mSenderTextView;
	private TextView mSenderNumber;
	private View mSenderparent;
	private int mMaxHeight,mMaxWidth;
	private ArrayList<Rfc822Token> mAddressList;
	private boolean mNeedMaxHeight = false;
	private float mAddWidth;
	private float mLeftWidth;
	private int mRows = -1;
	public AuroraDetailAddressView(Context context) {
		super(context);
		mContext = context;
		initViews();
	}

	public AuroraDetailAddressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initViews();

	}

	public AuroraDetailAddressView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initViews();
	}

	private void initViews() {
		Resources res = mContext.getResources();
		LayoutInflater.from(mContext).inflate(R.layout.aurora_detail_address, this);
		mInnerScrollView = (AuroraInnerScrollView) findViewById(R.id.myScroll);
		mAuroraEditTextStateView = (AuroraEditTextStateView) findViewById(R.id.aurora_edit);
		mAuroraEditTextStateView.setAuroraListener(this);
		mSenderTextView = (TextView) findViewById(R.id.aurora_sendertext);
		mSenderNumber = (TextView) findViewById(R.id.aurora_sendernum);
		mSenderparent = findViewById(R.id.aurora_sendertext);
		mSenderparent.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.aurora_adress_title);
		mMaxHeight = res.getDimensionPixelSize(R.dimen.aurora_address_maxheight);
		mAddressList = new ArrayList<Rfc822Token>();
		mLeftWidth = res.getDimensionPixelSize(R.dimen.aurora_detail_paddingleft)
				+ res.getDimensionPixelSize(R.dimen.aurora_detail_titlewidth);
		mAddWidth = res.getDimensionPixelSize(R.dimen.aurora_detail_text_leftpadding)
				+ res.getDimensionPixelSize(R.dimen.aurora_detail_text_rightpadding)
				+ res.getDimensionPixelSize(R.dimen.aurora_detail_text_margin);
	}

	public void setTitle(String title) {
		mTitleTextView.setText(title);
	}

	@Override
	public void changeHeight(final int index,boolean flag) {
		if(mRows == index) return;
		mRows = index;
		post(new Runnable(){
			public void run(){
				updateScrollView();
			}
		});
	}

    private static void createLinkTextView(TextView textView, String emailAddress, Account account) {
		if(null == account) return;
		final Spannable spannable = (Spannable) textView.getText();
        URLSpan span = new EmailAddressSpan(account, emailAddress);
		final int start = 0;
		final int end = textView.getText().length();
        spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
	
	

	public void dealwithAddress(Rfc822Token[] tokens, Account account, int w) {
		float rowWidth = w - mLeftWidth;
		if(w <= 0 || rowWidth <= 0){
			Log.e("AuroraDetailAddressView","dealwithAddress err :" + w + " lw:" + mLeftWidth);
			rowWidth = mContext.getResources().getDimensionPixelSize(R.dimen.aurora_detail_contentwidth);
		}

		if(mAddressList.size() > 0){
			mAddressList.clear();
			mAuroraEditTextStateView.removeAllViews();
		}
		
		float viewWidth = 0;
		float width = 0;
		mRows = 0;
        for (int i = 0; i < tokens.length; ++i) {
	 		View view = View.inflate(mContext, R.layout.aurora_detail_address_item, null);
			TextView textView = (TextView) view.findViewById(R.id.addressitem_button);
			String email = tokens[i].getAddress();
			String name = tokens[i].getName();
			if(null != name) name = name.trim();
			if(!TextUtils.isEmpty(name)){
				textView.setText(Address.decodeAddressName(name));
			}else if(!TextUtils.isEmpty(email)){
				textView.setText(email);
			}
			textView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
				@Override
				public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
					return false;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
					return false;
				}

				@Override
				public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode actionMode) {
				}
			});
			createLinkTextView(textView, email, account);
			mAddressList.add(tokens[i]);
			mAuroraEditTextStateView.addView(view);

			if(mRows <= 1){
				viewWidth = mAddWidth + textView.getPaint().measureText(textView.getText().toString());
				if(width + viewWidth > rowWidth){
					width = viewWidth;
					++mRows;
				}else{
					width += viewWidth;
				}
			}
		}


		resetView();

	}
	
	private void updateScrollView(){
		ViewGroup.LayoutParams params = mInnerScrollView.getLayoutParams();
		if(mRows > 1){
			params.height = mMaxHeight;
		}else{
			params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		}
		mInnerScrollView.setLayoutParams(params);

	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int id = view.getId();
		switch (id) {
		case R.id.aurora_sendertext:
			mSenderparent.setVisibility(View.GONE);
			updateScrollView();
			mAuroraEditTextStateView.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	public String getDisplayText(){
		int lastIndex = mAddressList.size() - 1;
		if(lastIndex < 0){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i < lastIndex;i++){
			String name = mAddressList.get(i).getName();
			if(null != name) name = name.trim();
			if(!TextUtils.isEmpty(name)){
				sb.append(Address.decodeAddressName(name)).append(",");
			}
			else{
				sb.append(mAddressList.get(i).getAddress()).append(",");
			}
		}
		if(!TextUtils.isEmpty(mAddressList.get(lastIndex).getName())){
			sb.append(Address.decodeAddressName(mAddressList.get(lastIndex).getName()));
		}
		else{
			sb.append(mAddressList.get(lastIndex).getAddress());
		}

		sb.append(mContext.getResources().getString(R.string.aurora_num_sender, mAddressList.size()));
		return sb.toString();
	}

	private void resetView(){		
		if(mAddressList.size()==0){
			return;
		}
		
		if(mRows > 0){
			mSenderTextView.setText(getDisplayText());
			mAuroraEditTextStateView.setVisibility(View.GONE);
			mSenderparent.setVisibility(View.VISIBLE);
		}
	}

}
