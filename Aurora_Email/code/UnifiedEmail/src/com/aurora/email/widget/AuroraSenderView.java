package com.aurora.email.widget;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.mail.R;
import com.android.mail.providers.Account;

import com.aurora.email.adapters.AuroraComposeContactAdapter;

public class AuroraSenderView extends LinearLayout {
	private Context mContext;
	private AuroraInnerListView mNewlyContactListView;
	private AuroraComposeContactAdapter mContactAdapter;
	private ArrayList<String> mContactArrayList;
	private TextView mTextView;
	protected Account mCurrentAccount;
	private Account[] mAccounts;
	private ChangeAccount mChangeListener;
	public interface ChangeAccount{
		void chang(Account account);
	}
	public void setChangAccountListener(ChangeAccount listener){
		mChangeListener = listener;
	}
	public AuroraSenderView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public AuroraSenderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public AuroraSenderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	private void initViews(Context context) {
		mContext = context;
		LayoutInflater.from(mContext).inflate(
				R.layout.aurora_compose_senderview, this);
		mNewlyContactListView = (AuroraInnerListView) findViewById(R.id.contact_listView);
		mNewlyContactListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				String contactString = ((TextView) arg1
						.findViewById(R.id.contact_address)).getText()
						.toString();
				if (!TextUtils.isEmpty(contactString)) {
					mTextView.setText(contactString);
					mChangeListener.chang(mAccounts[arg2]);
					showOrHideContact(false);
				}
			}
		});
		mContactArrayList = new ArrayList<String>();
		mTextView = (TextView) findViewById(R.id.id_send_info);
		mTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mNewlyContactListView.setVisibility(View.VISIBLE);
			}
		});
	}

	public void setParentScroll(ScrollView scrollView) {
		mNewlyContactListView.initView(scrollView);
	}

	private void getContactFromDB() {
		mContactArrayList.clear();
		for (int i = 0; i < mAccounts.length; i++) {
			if (!TextUtils.isEmpty(mAccounts[i].getEmailAddress())) {
				mContactArrayList.add(mAccounts[i].getEmailAddress());
			} else if (!TextUtils.isEmpty(mAccounts[i].getSenderName())) {
				mContactArrayList.add(mAccounts[i].getSenderName());
			}
		}
		if (mContactAdapter == null) {
			mContactAdapter = new AuroraComposeContactAdapter(mContext,
					mContactArrayList);
			mContactAdapter.setShowTag(false);
			setListViewHeight(mNewlyContactListView, mContactAdapter,
					mContactArrayList.size());
			mNewlyContactListView.setAdapter(mContactAdapter);
		} else {
			setListViewHeight(mNewlyContactListView, mContactAdapter,
					mContactArrayList.size());
			mContactAdapter.notifyDataSetChanged();
		}
	}

	private void setListViewHeight(ListView listView, BaseAdapter adapter,
			int count) {
		int totalHeight = 0;
		if (count > 3) {
			count = 3;
		}
		for (int i = 0; i < count; i++) {
			View listItem = adapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * count);
		listView.setLayoutParams(params);
	}

	public void showOrHideContact(boolean flag) {
		if (flag) {
			mNewlyContactListView.setVisibility(View.VISIBLE);
		} else {
			mNewlyContactListView.setVisibility(View.GONE);
		}
	}

	public void setCurrentAccount(Account account) {
		mCurrentAccount = account;
		if (!TextUtils.isEmpty(mCurrentAccount.getEmailAddress())) {
			mTextView.setText(mCurrentAccount.getEmailAddress());
		}
	}
	
	public void setAccounts(Account[] accounts) {
		mAccounts = accounts;
		getContactFromDB();
	}

}
