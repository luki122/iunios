package com.mediatek.contacts.list;

import java.util.HashMap;
import java.util.List;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.list.AuroraGroupDetailAdapter;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnCloseButtonClickListener;
import aurora.widget.AuroraSearchView.OnQueryTextListener;

import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.AuroraSimContactListFragment.InputMethodBroadCastReceiver;
import com.android.contacts.model.AccountType;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;
import com.android.contacts.widget.AbsListIndexer;
import com.android.contacts.widget.AlphbetIndexView;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import aurora.widget.AuroraListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.os.Handler;
import android.os.Message;
import aurora.app.AuroraActivity;
import android.view.View.OnFocusChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraCheckBox;


public class AuroraContactListMultiChoiceFragment extends 
ContactEntryListFragment<ContactListAdapter> {

	private static final String TAG = "liyang-AuroraContactListMultiChoiceFragment";

	private Context mContext;

	public AuroraGroupDetailAdapter mAdapter;

	private Uri mGroupUri;
	private long mGroupId;
	public String mGroupName;
	public String mGroupRingtineName;
	private String mAccountTypeString;
	private String mDataSet;
	private boolean mIsReadOnly;

	private View mLoadingContainer;
	private TextView mLoadingContact;
	private ProgressBar mProgress;

	public static boolean isFinished = false;
	private static final int WAIT_CURSOR_START = 1230;
	private static final long WAIT_CURSOR_DELAY_TIME = 500;
	private final int REQUEST_PICK_RINGTONE = 0;

	private ContactListFilter mFilter;
	private String mFilterExInfo;

	private RelativeLayout mGotoSearchLayout;
	private AbsListIndexer mAlphbetIndexView;
	private AuroraSearchView mSearchView;
	private View mNoContactsEmptyView;

	public boolean mIsAuroraSearchMode = false;
	private static int mItemCount = 0;
	private static boolean mNeedCreateDialerTempTable = true;
	public boolean mSearchViewHasFocus = false;

	private boolean mIsPrivacyMode = false;
	public void AuroraContactListMultiChoiceFragment() {
		setSectionHeaderDisplayEnabled(true);
	}

	public boolean getMIsAuroraSearchMode(){
		return mIsAuroraSearchMode;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mContext = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPhotoLoaderEnabled(true);
		Bundle extras = getActivity().getIntent().getExtras();
		if (null != extras) {
			mFilter = extras
					.getParcelable(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER);
			mFilterExInfo = extras
					.getString(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO);

			mIsPrivacyMode = extras.getBoolean("aurora_add_privacy_contacts");
		}

		mGroupUri = getActivity().getIntent().getData();
		setSlideDelete(false);

		svQueryTextListener=new SvQueryTextListener();
		searchViewBackButton=(((AuroraActivity)getActivity()).getAuroraActionBar()).getAuroraActionbarSearchViewBackButton();				
		Log.d(TAG,"searchViewBackButton:"+searchViewBackButton);

		searchViewBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				quitSeach();

			}
		});


		if(myBroadCastReceiver==null){
			//生成广播处理   
			myBroadCastReceiver = new InputMethodBroadCastReceiver();   
			IntentFilter intentFilter = new IntentFilter(ACTION_INPUT_METHOD_SHOW); 
			intentFilter.addAction(ACTION_INPUT_METHOD_HIDE);

			//注册广播   
			mContext.registerReceiver(myBroadCastReceiver, intentFilter);  
		}
	}

	@Override
	protected View inflateView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.aurora_group_detail_list_content, null);
	}

	public void quitSeach(){				
		mIsAuroraSearchMode = false;
		super.setPartionStatus();
		mSearchView.clearText();
		svQueryTextListener.onQueryTextChange("");
		((AuroraActivity)getActivity()).hideSearchviewLayout();	

		if(mGotoSearchLayout!=null && !mGotoSearchLayout.isShown()){
			getListView().addHeaderView(mGotoSearchLayout);

		}
		getListView().auroraSetHeaderViewYOffset(0);
		mAdapter.setSearchMode(mIsAuroraSearchMode);
		mAlphbetIndexView.setVisibility(View.VISIBLE);
	}

	public ImageButton searchViewBackButton;
	private SvQueryTextListener svQueryTextListener;

	@Override
	protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
		super.onCreateView(inflater, container);

		mAlphbetIndexView = (AbsListIndexer) getView().findViewById(
				R.id.gn_alphbet_indexer);

		mLoadingContainer = getView().findViewById(R.id.loading_container);
		mLoadingContainer.setVisibility(View.GONE);
		mLoadingContact = (TextView) getView().findViewById(
				R.id.loading_contact);
		mLoadingContact.setVisibility(View.GONE);
		mProgress = (ProgressBar) getView().findViewById(
				R.id.progress_loading_contact);
		mProgress.setVisibility(View.GONE);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
				WAIT_CURSOR_DELAY_TIME);


		if (!mIsPrivacyMode) {
			mGotoSearchLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_goto_search_mode, null);
			mGotoSearchLayout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if(mGotoSearchLayout!=null && mGotoSearchLayout.isShown()){
						getListView().removeHeaderView(mGotoSearchLayout);
						mIsAuroraSearchMode=true;
					}


					((AuroraActivity) getActivity()).showSearchviewLayout();
					InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);  
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

					mSearchView =((AuroraActivity)getActivity()).getAuroraActionBar().getAuroraActionbarSearchView();
					mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
					mSearchView.setMaxLength(30);
					mSearchView.setQueryHint(mContext.getResources().getString(R.string.search_contacts));
					initButtomBar(false);

					((AuroraContactListMultiChoiceActivity)getActivity()).setRightBtnTv(mAdapter.getCheckedItem().size());
					mSearchView.setOnQueryTextListener(svQueryTextListener);

					mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View view, boolean hasFocus) {
							mSearchViewHasFocus = hasFocus;
							if (hasFocus) {
								if (mNeedCreateDialerTempTable) {
									auroraInitQueryDialerABC();
									mNeedCreateDialerTempTable = false;
								}
							}
						}
					});
					setSearchView(mSearchView);
				}
			});
			getListView().addHeaderView(mGotoSearchLayout);
		}

		

		getListView().setFastScrollEnabled(false);
		getListView().setFastScrollAlwaysVisible(false);
		getListView().auroraEnableSelector(false);

		mNoContactsEmptyView = getView().findViewById(
				R.id.no_contacts);

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				initButtomBar(true);
			}
		}, 500);
	}

	private void initButtomBar(boolean flag) {
		try {
			AuroraActionBar actionBar;
			actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
			actionBar.setShowBottomBarMenu(flag);
			actionBar.showActionBottomeBarMenu();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try{
			mContext.unregisterReceiver(myBroadCastReceiver);
			myBroadCastReceiver=null;
		}catch(Exception e){
		}
		if (mAdapter != null) {
			mAdapter = null;
		}
	}

	@Override
	protected ContactListAdapter createListAdapter() {
		mAdapter = new AuroraGroupDetailAdapter(getContext());
		mAdapter.setCheckBoxEnable(true);
		mAdapter.setSectionHeaderDisplayEnabled(true);
		mAdapter.setPrivacyMode(mIsPrivacyMode);
		return mAdapter;
	}

	@Override
	protected void configureAdapter() {
		super.configureAdapter();
		ContactEntryListAdapter adapter = getAdapter();
		adapter.setSectionHeaderDisplayEnabled(true);
		adapter.setFilter(mFilter);
		adapter.setFilterExInfo(mFilterExInfo);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "handleMessage msg = " + msg.what);

			switch (msg.what) {

			case WAIT_CURSOR_START:
				Log.i(TAG, "start WAIT_CURSOR_START !isFinished : "
						+ !isFinished);
				if (!isFinished) {
					mLoadingContainer.setVisibility(View.VISIBLE);
					mLoadingContact.setVisibility(View.VISIBLE);
					mProgress.setVisibility(View.VISIBLE);
				} else {
					mLoadingContainer.setVisibility(View.GONE);
					mLoadingContact.setVisibility(View.GONE);
					mProgress.setVisibility(View.GONE);
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();

		bindToAlphdetIndexer();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private void showEmptyView(boolean flag) {
		Log.d(TAG,"showEmptyView:"+flag);
		if (flag) {
			if (mAlphbetIndexView != null) {
				mAlphbetIndexView.setVisibility(View.GONE);
			}

			if (mNoContactsEmptyView != null && !mIsAuroraSearchMode) {
				mNoContactsEmptyView.setVisibility(View.VISIBLE);
			}
		} else {
			if (mAlphbetIndexView != null && !mIsAuroraSearchMode) {
				mAlphbetIndexView.setVisibility(View.VISIBLE);
			}

			if (mNoContactsEmptyView != null) {
				mNoContactsEmptyView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

		if (null != mAlphbetIndexView) {
			mAlphbetIndexView.invalidateShowingLetterIndex();
		}
	}

	private void bindToAlphdetIndexer() {
		mAlphbetIndexView = (AbsListIndexer) getView().findViewById(
				R.id.gn_alphbet_indexer);
		if (null != mAlphbetIndexView) {
			mAlphbetIndexView.setList(getListView(), this);
		}
	}

	@Override
	protected void onItemClick(int position, long id) {

	}

	private InputMethodBroadCastReceiver myBroadCastReceiver;
	public final String ACTION_INPUT_METHOD_SHOW = "android.intent.action.ACTION_INPUT_METHOD_SHOW";
	public final String ACTION_INPUT_METHOD_HIDE = "android.intent.action.ACTION_INPUT_METHOD_HIDE";
	public class InputMethodBroadCastReceiver extends BroadcastReceiver    
	{     

		@Override  
		public void onReceive(Context context, Intent intent)   
		{   
			String action = intent.getAction();    	

			Log.d(TAG, "onReceive: "+intent.getAction());
			if (action.equals(ACTION_INPUT_METHOD_SHOW)) {
				initButtomBar(false);
			}else if (action.equals(ACTION_INPUT_METHOD_HIDE)) {
				initButtomBar(true);
			}
		}   

	} 


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.onItemClick(parent, view, position, id);
		Log.d(TAG, "onItemClick with adapterView: position = " + position + "  id = " + id);

		RelativeLayout mainUi = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		View v = null;
		if (null != mainUi) {
			v = mainUi.getChildAt(0);
		}

		if (null != v && v instanceof ContactListItemView) {


			AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
			boolean checked =checkBox.isChecked();
			checkBox.auroraSetChecked(!checked, true);


			//            ((ContactListItemView) v).getCheckBox().auroraSetChecked(!checked, true);

			int realPosition = getRightPosition(position);
			if (!checked) {
				getAdapter().setCheckedItem(Long.valueOf(mAdapter.getContactID(realPosition)), "");
			} else {
				getAdapter().getCheckedItem().remove(Long.valueOf(mAdapter.getContactID(realPosition)));
			}

			((AuroraContactListMultiChoiceActivity)getActivity()).updateSelectedItemsView(mItemCount);
		}
	}

	public void onSelectAll(boolean check) {
		updateListCheckBoxeState(check);
	}

	private void updateListCheckBoxeState(boolean checked) {
		if (mAdapter == null) {
			return;
		}

		final int headerCount = getListView().getHeaderViewsCount();
		final int count = mAdapter.getCount() + headerCount;
		int contactId = -1;

		for (int position = headerCount; position < count; ++position) {
			int adapterPos = position - headerCount;
			contactId = mAdapter.getContactID(adapterPos);
			Log.d(TAG, "adapterPos = " + adapterPos + "  rawContactId = " + contactId);

			if (checked) {
				mAdapter.setCheckedItem(Long.valueOf(contactId), "");
			} else {
				mAdapter.getCheckedItem().clear();
			}

			int realPos = position - getListView().getFirstVisiblePosition();
			View view = getListView().getChildAt(realPos);
			if (null != view) {
				RelativeLayout mainUi = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
				View v = null;
				if (null != mainUi) {
					v = mainUi.getChildAt(0);
				}

				if (null != v && v instanceof ContactListItemView) {
					//                    ((ContactListItemView) v).getCheckBox().auroraSetChecked(checked, true);
					((AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox)).auroraSetChecked(checked, true);
				}
			}
		}


		((AuroraContactListMultiChoiceActivity)getActivity()).setBottomMenuEnable(checked);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		super.onLoadFinished(loader, data);

		isFinished = true;
		bindToAlphdetIndexer();
		mHandler.sendEmptyMessage(WAIT_CURSOR_START);

		if (mIsAuroraSearchMode) {
			auroraNoMatchView(true, null);
		}

		if (data != null && data.getCount() <= 0) {
			showEmptyView(true);
		} else {
			showEmptyView(false);
		}

		mItemCount = mAdapter.getCount();
		((AuroraContactListMultiChoiceActivity)getActivity()).updateSelectedItemsView(mItemCount);
	}

	private void setQueryTextToFragment(String query) {
		setSearchMode(mIsAuroraSearchMode);
		setQueryString(query, true);
		setVisibleScrollbarEnabled(!mIsAuroraSearchMode);
	}

	private final class SvQueryTextListener implements OnQueryTextListener {
		@Override
		public boolean onQueryTextChange(String queryString) {

			if (queryString.length() > 0) {
				setQueryTextToFragment(queryString);
				mAlphbetIndexView.setVisibility(View.GONE);
				if (checkIsNeedQueryFromDialer(queryString)) {
					auroraNoMatchView(mIsAuroraSearchMode, null);
				}

				//                if (mGotoSearchLayout != null) {
				//                    mGotoSearchLayout.setVisibility(View.GONE);
				//                }
				getListView().auroraSetHeaderViewYOffset(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));
			} else {
				setQueryTextToFragment("");
				//				mIsAuroraSearchMode = false;
				//				mAlphbetIndexView.setVisibility(View.VISIBLE);
				//				auroraNoMatchView(mIsAuroraSearchMode, null);

				//                if (mGotoSearchLayout != null) {
				//                    mGotoSearchLayout.setVisibility(View.VISIBLE);
				//                }
				//				getListView().auroraSetHeaderViewYOffset(0);
			}

			((AuroraContactListMultiChoiceActivity)getActivity()).updateSelectedItemsView(mItemCount);
			mAdapter.setSearchMode(mIsAuroraSearchMode);
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return true;
		}
	}

	private int getRightPosition(int position) {
		Log.d(TAG, "getRightPosition:"+position+" mIsAuroraSearchMode:"+mIsAuroraSearchMode);
		if (mIsPrivacyMode) {
			return position;
		}

		if(mIsAuroraSearchMode){
			return position;
		}


		return position - 1;
	}
}
