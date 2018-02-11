package com.aurora.email.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.lang.Integer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.common.Rfc822Validator;
import com.android.emailcommon.mail.Message;
import com.android.mail.R;
import com.android.mail.compose.ComposeActivity;
import com.android.mail.providers.Account;
import com.android.mail.providers.Address;
import com.android.mail.utils.HardwareLayerEnabler;
import com.aurora.email.AuroraComposeActivity;
import com.aurora.email.adapters.AuroraComposeMatchAdapter;
import com.aurora.email.widget.AuroraEditTextStateView.AuroraListener;

import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraEditText;
import gionee.provider.GnContactsContract;

import com.android.email.provider.AuroraAutoCompleteDBHelper;

public class AuroraComposeAddressView extends LinearLayout implements
		AuroraListener, OnClickListener {

	AuroraInnerScrollView mInnerScrollView;
	private Context mContext;
	AuroraEditTextStateView mAuroraEditTextStateView;
	AuroraInnerListView mListView;
	private OnFocusChangeListener mFocusChangedListener;
	private TextView mTitleTextView;
	private ImageView mAddButton;
	private String currentAddress;
	private Rfc822Validator mValidator;
	private Rfc822Token[] mReplyRfc822Token;
	private int mRows = 0;
	private View oldView;
	AuroraEditText mEditText;
	private Rfc822Token mCurrentRfc822Token;
	private TextView mSenderTextView;
	private TextView mSenderNumber;
	private View mSenderparent;
	private int mAddressHeight;
	private int mMaxHeight, mMaxWidth;
	private FocusControl mFocusControl;
	private static boolean visiable;
	private final String pfix = "<";
	private final String bfix = ">,";
	private ArrayList<Rfc822Token> mAddressList;
	private HashMap<Rfc822Token,Integer> mAddressHashMap;
	private float mAddWidth;
	private boolean mNeedMaxHeight = false;
	private float mWidth = 0;
	private char mSplit = 'þ';
	private String mContactUrifix = "?directory=0&remove_duplicate_entries=true&address_book_index_extras=true  &&  selection: null";
	private String mContactUri = "content://" + GnContactsContract.AUTHORITY
			+ "/data/emails/filter/";
	private ArrayList<String> mContactList;
	private ContactEmailQueryHandler mQueryHandler;
	private String[] mProjection = new String[] { Email._ID, Email.DATA,
			Email.DISPLAY_NAME_PRIMARY };
	private String[] mEmailProjection = new String[] {
			com.android.emailcommon.provider.EmailContent.MessageColumns.ID,
			com.android.emailcommon.provider.EmailContent.MessageColumns.TO_LIST,
			com.android.emailcommon.provider.EmailContent.MessageColumns.DISPLAY_NAME };
	private AuroraComposeMatchAdapter mMatchAdapter;
	private ArrayList<String> mAddEmaiList;
	private AuroraAutoCompleteDBHelper mDbHelper;
	private String mSenderName;
	private String mSenderAddress;

	public AuroraComposeAddressView(Context context) {
		super(context);
		mContext = context;
		initViews();
	}

	public AuroraComposeAddressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initViews();

	}

	public interface FocusChangedListener {
		void OnFocusChangeListener();
	}

	@SuppressLint("NewApi")
	public AuroraComposeAddressView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initViews();
	}

	private void initViews() {
		LayoutInflater.from(mContext).inflate(R.layout.aurora_compose_address,
				this);
		mInnerScrollView = (AuroraInnerScrollView) findViewById(R.id.myScroll);
		mAuroraEditTextStateView = (AuroraEditTextStateView) findViewById(R.id.aurora_edit);
		mAuroraEditTextStateView.setAuroraListener(this);
		mListView = (AuroraInnerListView) findViewById(R.id.contactList);
		mListView.auroraEnableSelector(false);
		mListView.setSelector(R.drawable.aurora_innerlistview_selector);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterview, View view,
					int i, long l) {
				// TODO Auto-generated method stub
				TextView textView = (TextView) view
						.findViewById(R.id.contact_address);
				String tmpAdrress = textView.getText().toString();
				int index = tmpAdrress.lastIndexOf("(");
				int end = tmpAdrress.lastIndexOf(")");
				if (index >= 0 && end >= 0) {
					String address = tmpAdrress.substring(index + 1, end).trim();
					for(int k=0;k<mAddressList.size();k++){
						if(mAddressList.get(k).getAddress().equals(address)){
							return;
						}
					}
					getRfc822Token(tmpAdrress.substring(0, index - 1).trim(),
							address);
					dealwithAddress(-1);
					mEditText.getText().clear();
				}
			}

		});
		mEditText = (AuroraEditText) findViewById(R.id.autocomplete_textview);
		mEditText.setCursorVisible(false);
		mEditText.setOnClickListener(this);
		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence charsequence, int i, int j,
					int k) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence charsequence, int i,
					int j, int k) {
				// TODO Auto-generated method stub
				mEditText.setCursorVisible(true);
				if (oldView != null) {
					oldView.setSelected(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				// TODO Auto-generated method stub
				String text = editable.toString();
				if (text.equals("")) {
					matchNewlyContactEmails();
				} else if (text.equals(" ") || text.equals(";")
						|| text.equals(",")) {
					mEditText.getText().clear();
				} else if (text.endsWith(";") || text.endsWith(",")) {
					dealwithAddress(1);
				} else if (!TextUtils.isEmpty(text)) {
					String search = text.trim();
					if (search.indexOf("@") >= 0) {
						matchContactEmails(search, 0);
					} else {
						matchContactEmails(search, 1);
					}
				} else {
					matchNewlyContactEmails();
				}
			}
		});
		mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1 && visiable) {
					mEditText.setCursorVisible(true);
					mSenderTextView.setText("");   //cjs add
					matchNewlyContactEmails();
				} else if (!arg1) {
					if (!TextUtils.isEmpty(mEditText.getText().toString())) {
						dealwithAddress(0);
					}
					resetView();
				}
				visiable = true;
				if (mFocusChangedListener != null) {
					mFocusChangedListener.onFocusChange(
							AuroraComposeAddressView.this, arg1);
				}
			}
		});
		mSenderTextView = (TextView) findViewById(R.id.aurora_sendertext);
		mSenderTextView.setOnClickListener(this);
		mSenderNumber = (TextView) findViewById(R.id.aurora_sendernum);
		mSenderparent = findViewById(R.id.aurora_senderparent);
		mSenderparent.setOnClickListener(this);
		mAddButton = (ImageView) findViewById(R.id.aurora_btn_add);
		mAddButton.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.aurora_adress_title);
		mAddressHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.aurora_address_editheight);
		mMaxHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.aurora_address_maxheight);
		mMaxWidth = mContext.getResources().getDimensionPixelSize(
				R.dimen.aurora_address_maxwidth);
		mAddWidth = mContext.getResources().getDimensionPixelSize(
				R.dimen.aurora_detail_add_width);
		visiable = false;
		mAddressList = new ArrayList<Rfc822Token>();
		mContactList = new ArrayList<String>();
		mQueryHandler = new ContactEmailQueryHandler(
				mContext.getContentResolver());
		mAddEmaiList = new ArrayList<String>();
		mAddressHashMap = new HashMap<Rfc822Token,Integer>();
	}

	public void setParentScroll(ScrollView scrollView) {
		mInnerScrollView.initView(scrollView);
		mListView.initView(scrollView);
	}

	public void setCurrentAccount(Account account) {
		mDbHelper = AuroraAutoCompleteDBHelper.getInstance(mContext);
		mDbHelper.setCurrentTableName(account.getEmailAddress()
				.replace("@", "").replace(".", ""));
	}

	public void setFocusChangedListener(OnFocusChangeListener changedListener) {
		mFocusChangedListener = changedListener;
	}

	public void requestFocusFromChild() {
		showAuroraEditTextStateView();
	}

	public View getCurrentFocus() {
		return mEditText;
	}

	public void setTitle(String title) {
		mTitleTextView.setText(title);
	}

	public Rfc822Token[] getRfc822Tokens() {
		Rfc822Token[] tokens = new Rfc822Token[mAddressList.size()];
		return mAddressList.toArray(tokens);
	}

	public void setRfc822Tokens(Rfc822Token[] tokens) {
		visiable = true;
		mReplyRfc822Token = tokens;
		dealwithAddress(tokens);
		resetView();
	}

	public void setValidator(Rfc822Validator validator) {
		mValidator = validator;
	}

	public CharSequence getText() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mAddressList.size(); i++) {
			sb.append(pfix).append(mAddressList.get(i).getAddress())
					.append(bfix);
		}
		return sb;
	}

	public void setText(CharSequence text) {
		mEditText.setText(text);
	}

	public long length() {
		return mEditText.length();
	}

	@Override
	public void changeHeight(final int index, boolean flag) {
		// TODO Auto-generated method stub
	}

	private void dealwithAddress(int code) {
		String text = mEditText.getText().toString();
		if (code == 0) {
			if (!TextUtils.isEmpty(mSenderAddress)&&mSenderAddress.equals(text)) {
				getRfc822Token(mSenderName, text);
			} else {
				getRfc822Token("", text);
			}
		} else if (code == 1) {
			text = text.substring(0, text.length() - 1);
			if (!TextUtils.isEmpty(mSenderAddress)&&mSenderAddress.equals(text)) {
				getRfc822Token(mSenderName, text);
			} else {
				getRfc822Token("", text);
			}
		}
		if(null==mAddressHashMap.get(mCurrentRfc822Token)){
			mAddressHashMap.put(mCurrentRfc822Token,1);
		}else{
			mEditText.getText().clear();
			return;
		}
		mEditText.getText().clear();
		View view;
		if (mValidator != null
				&& mValidator.isValid(mCurrentRfc822Token.getAddress())) {
			view = View.inflate(mContext, R.layout.aurora_compose_address_item,
					null);
		} else {
			view = View.inflate(mContext,
					R.layout.aurora_compose_recipient_address_error_item, null);
		}
		TextView textView = (TextView) view
				.findViewById(R.id.addressitem_button);
		String name = mCurrentRfc822Token.getName().trim();
		if (!TextUtils.isEmpty(name)) {
			textView.setText(name);
		} else if (!TextUtils.isEmpty(mCurrentRfc822Token.getAddress())) {
			textView.setText(mCurrentRfc822Token.getAddress());
		}
		mAddressList.add(mCurrentRfc822Token);
		textView.setOnClickListener(this);
		mAuroraEditTextStateView.addView(view,
				mAuroraEditTextStateView.getChildCount() - 1);
		mFocusControl.moveFocus(AuroraComposeActivity.SENDVIEW_ENABLE_TRUE);
		measureScrollViewHeightForRemove();
	}

	public void dealwithAddress(Rfc822Token[] tokens) {
		int rows = 0;
		float viewWidth = 0;
		float width = 0;
		final float rowWidth = (float) mMaxWidth;
		for (int i = 0; i < tokens.length; ++i) {
			if(null==mAddressHashMap.get(tokens[i])){
				mAddressHashMap.put(mCurrentRfc822Token,1);
			}else{
				mEditText.getText().clear();
				return;
			}
			View view = View.inflate(mContext,
					R.layout.aurora_compose_address_item, null);
			TextView textView = (TextView) view
					.findViewById(R.id.addressitem_button);
			if(null!=tokens[i].getName()&&!TextUtils.isEmpty(tokens[i].getName().trim())){
				tokens[i].setName(Address.decodeAddressName(tokens[i].getName()).trim());
			}else{
				tokens[i].setName("");
			}
			mAddressList.add(tokens[i]);
			String email = tokens[i].getAddress();
			String name = tokens[i].getName();
			if (!TextUtils.isEmpty(name)){
				textView.setText(name);
			} else if (!TextUtils.isEmpty(email)) {
				textView.setText(email);
			}
			mFocusControl.moveFocus(AuroraComposeActivity.SENDVIEW_ENABLE_TRUE);
			textView.setOnClickListener(this);
			mAuroraEditTextStateView.addView(view,
					mAuroraEditTextStateView.getChildCount() - 1);

			if (rows <= 2) {
				viewWidth = mAddWidth
						+ textView.getPaint().measureText(
								textView.getText().toString());
				if (width + viewWidth > rowWidth) {
					width = viewWidth;
					++rows;
				} else {
					width += viewWidth;
				}
			}
		}
		measureScrollViewHeightForRemove();
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int id = view.getId();
		switch (id) {
		case R.id.addressitem_button:
			if (view.isSelected()) {
				view.setSelected(false);
				mEditText.setCursorVisible(true);
			} else {
				if (oldView == null) {
					oldView = view;
				} else {
					oldView.setSelected(false);
				}
				view.setSelected(true);
				AuroraComposeActivity.showInputMethod(mContext, mEditText);
				if (mFocusChangedListener != null) {
					mFocusChangedListener.onFocusChange(
							AuroraComposeAddressView.this, true);
				}
				oldView = view;
				mEditText.setCursorVisible(false);
			}
			break;
		case R.id.autocomplete_textview:
			mEditText.requestFocus();
			mEditText.setCursorVisible(true);
			matchNewlyContactEmails();
			if (mFocusChangedListener != null) {
				mFocusChangedListener.onFocusChange(
						AuroraComposeAddressView.this, true);
			}
			if (oldView != null)
				oldView.setSelected(false);
			break;
		case R.id.aurora_btn_add:
			Intent intent = new Intent("com.aurora.action.email.select.contact");
			ArrayList<String> mEmailList = new ArrayList<String>();
			mAddEmaiList.clear();
			for (int i = 0; i < mAddressList.size(); i++) {
				String tmpAddressString = mAddressList.get(i).getAddress();
				mEmailList.add(tmpAddressString); // 已经填写的邮箱地址
				mAddEmaiList.add(mAddressList.get(i).getName() + mSplit
						+ mAddressList.get(i).getAddress());
			}
			intent.putStringArrayListExtra("emails", mEmailList);
			((Activity) mContext).startActivityForResult(intent, getId());
			break;
		case R.id.aurora_senderparent:
		case R.id.aurora_sendertext:
			showAuroraEditTextStateView();
			break;
		default:
			break;
		}
	}

	private int getSelectedPosition() {
		for (int i = mAuroraEditTextStateView.getChildCount() - 2; i >= 0; i--) {
			if (mAuroraEditTextStateView.getChildAt(i)
					.findViewById(R.id.addressitem_button).isSelected()) {
				return i;
			}
		}
		return -1;
	}

	private void setLastSelected() {
		if (mAuroraEditTextStateView.getChildCount() >= 2) {
			View view = mAuroraEditTextStateView.getChildAt(
					mAuroraEditTextStateView.getChildCount() - 2).findViewById(
					R.id.addressitem_button);
			if (oldView == null) {
				oldView = view;
			} else {
				oldView.setSelected(false);
			}
			view.setSelected(true);
			oldView = view;
			mEditText.setCursorVisible(false);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			int code = event.getKeyCode();
			switch (code) {
			case KeyEvent.KEYCODE_ENTER:
				if (!TextUtils.isEmpty(mEditText.getText().toString())) {
					dealwithAddress(0);
				} else {
					if (oldView != null)
						oldView.setSelected(false);
					mFocusControl.moveFocus(R.id.aurora_et_title);
				}
				break;
			case KeyEvent.KEYCODE_DEL:
				getRfc822Token("", mEditText.getText().toString());
				if (!TextUtils.isEmpty(mEditText.getText().toString())) {
					return super.dispatchKeyEvent(event);
				} else {
					mEditText.setCursorVisible(true);
					int selectPosition = getSelectedPosition();
					if (selectPosition >= 0) {
						mAuroraEditTextStateView.removeViewAt(selectPosition);
						measureScrollViewHeightForRemove();
						mAddressHashMap.remove(mAddressList.get(selectPosition));
						mAddressList.remove(selectPosition);
						mFocusControl
								.moveFocus(AuroraComposeActivity.SENDVIEW_ENABLE_FALSE);
					} else {
						setLastSelected();
					}
				}
				break;
			default:
				return super.dispatchKeyEvent(event);
			}
		} else {
			return super.dispatchKeyEvent(event);
		}

		return true;
	}

	public void setFocusControl(FocusControl control) {
		mFocusControl = control;
	}

	private void resetView() {
		mListView.setVisibility(View.GONE);
		if (mAddressList.size() == 0) {
			return;
		}
		String textString = getCcText();
		mSenderTextView.setText(textString);
		if (mSenderTextView.getPaint().measureText(textString) > mMaxWidth) {
			String number = mContext.getResources().getString(
					R.string.aurora_num_sender, mAddressList.size());
			mSenderNumber.setText(number);
		} else {
			mSenderNumber.setText("");
		}
		mAuroraEditTextStateView.setVisibility(View.GONE);
		ViewGroup.LayoutParams params = mInnerScrollView.getLayoutParams();
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		mInnerScrollView.setLayoutParams(params);
	}

	public String getCcText() {
		if (mAddressList.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < mAddressList.size() - 1; i++) {
			String name = mAddressList.get(i).getName();
			if (!TextUtils.isEmpty(name)) {
				sb.append(name).append(",");
			} else {
				sb.append(mAddressList.get(i).getAddress()).append(",");
			}
		}
		String lastName = mAddressList.get(mAddressList.size() - 1).getName();
		if (!TextUtils.isEmpty(lastName)) {
			sb.append(lastName);
		} else {
			sb.append(mAddressList.get(mAddressList.size() - 1).getAddress());
		}
		return sb.toString();
	}

	public int getAdrressCount() {
		return mAddressList.size();
	}

	public void getRfc822Token(String name, String address) {
		mCurrentRfc822Token = new Rfc822Token(name.trim(), address, "");
	}

	private void showAuroraEditTextStateView() {
		ViewGroup.LayoutParams params = mInnerScrollView.getLayoutParams();
		measureScrollViewHeightForRemove();
		mAuroraEditTextStateView.setVisibility(View.VISIBLE);
		mEditText.requestFocus();
		AuroraComposeActivity.showInputMethod(mContext, mEditText);
	}

	private void measureScrollViewHeight(TextView textView) {
		float viewWidth = 0;
		final float rowWidth = (float) mMaxWidth;
		if (mRows <= 2) {
			viewWidth = mAddWidth
					+ textView.getPaint().measureText(
							textView.getText().toString());
			if (mWidth + viewWidth > rowWidth) {
				if (viewWidth > rowWidth && mWidth > 0) {
					mRows++;
				}
				mWidth = 0;
				mRows++;
			} else {
				mWidth += viewWidth;
			}
		}
		ViewGroup.LayoutParams params = mInnerScrollView.getLayoutParams();
		if (mRows >= 2) {
			params.height = mMaxHeight;
		} else {
			params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		}
		mInnerScrollView.setLayoutParams(params);
	}

	private void measureScrollViewHeightForRemove() {
		mRows = 0;
		mWidth = 0;
		for (int i = 0; i <= mAuroraEditTextStateView.getChildCount() - 2; i++) {
			measureScrollViewHeight((TextView) mAuroraEditTextStateView
					.getChildAt(i).findViewById(R.id.addressitem_button));
			if (mRows > 2) {
				break;
			}
		}
	}

	public void addContact(ArrayList<String> list) {
		if (list == null)
			return;
		list.removeAll(mAddEmaiList);
		for (int i = 0; i < list.size(); i++) {
			int index = list.get(i).indexOf(mSplit);
			if (index >= 0) {
				getRfc822Token(list.get(i).substring(0, index), list.get(i)
						.substring(index + 1, list.get(i).length()));
				dealwithAddress(-1);
			}
		}
		ViewGroup.LayoutParams params = mInnerScrollView.getLayoutParams();
		measureScrollViewHeightForRemove();
		mAuroraEditTextStateView.setVisibility(View.VISIBLE);
		mEditText.requestFocus();
		mEditText.setCursorVisible(true);
		if (mFocusChangedListener != null) {
			mFocusChangedListener.onFocusChange(AuroraComposeAddressView.this,
					true);
		}
	}

	public void matchContactEmails(String filter, int type) {
		mQueryHandler.setType(type);
		mQueryHandler.doQuery(Uri.parse(mContactUri + filter + mContactUrifix),
				mProjection, null, null, null);
	}

	public void getContactEmails() {

	}

	class ContactEmailQueryHandler extends AsyncQueryHandler {
		private int mType;

		public void setType(int type) {
			mType = type;
		}

		public ContactEmailQueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}

		public void doQuery(Uri uri, String[] projection, String selection,
				String[] selectionArgs, String orderBy) {
			startQuery(0, null, uri, projection, selection, selectionArgs,
					orderBy);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			Cursor cursor2 = mDbHelper.getAdrress(mEditText.getText()
					.toString(), mType);
			Cursor mergeCursor = new MergeCursor(
					new Cursor[] { cursor, cursor2 });
			if (mergeCursor.getCount() > 0) {
				mergeCursor.moveToFirst();
				mSenderName = mergeCursor.getString(2);
				mSenderAddress = mergeCursor.getString(1);
			}
			if (mListView.getVisibility() == View.GONE) {
				mListView.setVisibility(View.VISIBLE);
			}
			if (mMatchAdapter == null) {
				mMatchAdapter = new AuroraComposeMatchAdapter(mContext,
						R.layout.aurora_contact_item, mergeCursor,
						new String[] {}, new int[] {});
				mMatchAdapter.setKey(mEditText.getText().toString());
				mMatchAdapter.setFlag(false);
				mListView.setAdapter(mMatchAdapter);
			} else {
				mMatchAdapter.setKey(mEditText.getText().toString());
				mMatchAdapter.setFlag(false);
				mMatchAdapter.changeCursor(mergeCursor);
			}
			setListViewHeightBasedOnChildren(mListView);
			super.onQueryComplete(token, cookie, cursor);
		}

	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		int count = listAdapter.getCount();
		if (count > 3) {
			count = 3;
		}
		for (int i = 0; i < count; i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (count));
		listView.setLayoutParams(params);
	}

	public void matchNewlyContactEmails() {
		if (mDbHelper == null) {
			return;
		}
		Cursor cursor = mDbHelper.getNewlyAdrress(3);
		if (mListView.getVisibility() == View.GONE) {
			mListView.setVisibility(View.VISIBLE);
		}
		if (mMatchAdapter == null) {
			mMatchAdapter = new AuroraComposeMatchAdapter(mContext,
					R.layout.aurora_contact_item, cursor, new String[] {},
					new int[] {});
			mMatchAdapter.setFlag(true);
			mMatchAdapter.setKey("");
			mListView.setAdapter(mMatchAdapter);
		} else {
			mMatchAdapter.setFlag(true);
			mMatchAdapter.setKey("");
			mMatchAdapter.changeCursor(cursor);
		}
		setListViewHeightBasedOnChildren(mListView);
	}
}
