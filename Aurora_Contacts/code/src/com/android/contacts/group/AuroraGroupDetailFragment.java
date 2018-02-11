/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.contacts.group;

import java.util.List;

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.activities.AuroraGroupDetailActivity;
import com.android.contacts.activities.AuroraGroupEditorActivity;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.list.AuroraGroupDetailAdapter;

import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import aurora.widget.AuroraSearchView.OnCloseButtonClickListener;
import aurora.app.AuroraAlertDialog;

import com.android.contacts.list.AuroraSimContactListAdapter;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.model.AccountType;
import com.mediatek.contacts.list.AuroraContactListMultiChoiceActivity;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.widget.AbsListIndexer;
import com.android.contacts.widget.AlphbetIndexView;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import aurora.widget.AuroraListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.os.Handler;
import android.os.Message;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraActivity;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import aurora.widget.AuroraCheckBox;

/**
 * aurora: wangth 20130912 add
 */
public class AuroraGroupDetailFragment extends
ContactEntryListFragment<ContactListAdapter> {

	private static final String TAG = "liyang-AuroraGroupDetailFragment";
	private Context mContext;

	private Uri mGroupUri;
	private long mGroupId;
	public String mGroupRingtineName;
	private String mAccountTypeString;

	private View mLoadingContainer;
	private TextView mLoadingContact;
	private ProgressBar mProgress;

	public static boolean isFinished = false;
	private static final int WAIT_CURSOR_START = 1230;
	private static final long WAIT_CURSOR_DELAY_TIME = 500;
	private final int REQUEST_PICK_RINGTONE = 0;

	private ContactListFilter mFilter;
	private String mFilterExInfo;

	//    private RelativeLayout mGotoSearchLayout;
	private AbsListIndexer mAlphbetIndexView;
	private AuroraSearchView mSearchView;
	private View mNoContactsEmptyView;
	public View addGroupMemberInEmptyView;

	public boolean mIsAuroraSearchMode = false;
	private boolean mIsRemoveMemberMode = false;
	private boolean mIsNeedContextMenu = true;

	public static int mItemCount = 0;

	public boolean mSearchViewHasFocus = false;

//	public View add_group_member;

	public AuroraGroupDetailFragment() {
		setPhotoLoaderEnabled(true);
		setSectionHeaderDisplayEnabled(true);
	}

	public void quitSeach(){
		Log.d(TAG,"quitSearch");
		mIsAuroraSearchMode = false;
		super.setPartionStatus();
		mSearchView.clearText();
		svQueryTextListener.onQueryTextChange("");
		((AuroraActivity)getActivity()).hideSearchviewLayout();	

		//		if(mGotoSearchLayout!=null && !mGotoSearchLayout.isShown()){
		//			getListView().addHeaderView(mGotoSearchLayout);
		//
		//		}
		getListView().auroraSetHeaderViewYOffset(0);
		//		mAdapter.setSearchMode(mIsAuroraSearchMode);
		mAlphbetIndexView.setVisibility(View.VISIBLE);
		getAdapter().setSearchMode(mIsAuroraSearchMode);
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
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		setPhotoLoaderEnabled(true);
		Intent in = getActivity().getIntent();
		mGroupUri = in.getData();
		if (mGroupUri != null) {
			mGroupId = Long.parseLong(mGroupUri.getLastPathSegment());
		}
		Bundle extras = in.getExtras();
		if (null != extras) {
			mFilter = extras
					.getParcelable(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER);
			mFilterExInfo = extras
					.getString(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO);
		}

		svQueryTextListener=new SvQueryTextListener();
		searchViewBackButton=(((AuroraActivity)getActivity()).getAuroraActionBar()).getAuroraActionbarSearchViewBackButton();	
		searchViewBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				quitSeach();

			}
		});
	}

	@Override
	protected View inflateView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.aurora_group_detail_list_content, null);
	}

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

//		add_group_member = (View) LayoutInflater.from(mContext).inflate(
//				com.aurora.R.layout.aurora_slid_listview, null);
//		RelativeLayout mainUi = (RelativeLayout) add_group_member
//				.findViewById(com.aurora.R.id.aurora_listview_front);
//		mainUi.addView(inflater.inflate(R.layout.add_group_member_header, null), 0, new LayoutParams(LayoutParams.MATCH_PARENT,
//				LayoutParams.WRAP_CONTENT));
//		View paddingView=add_group_member.findViewById(com.aurora.R.id.control_padding);
//		paddingView.setPadding(0, 0, 0, 0);
		
//		getListView().addHeaderView(add_group_member, null, false);
		//        mGotoSearchLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_goto_search_mode, null);
		//        getListView().addHeaderView(mGotoSearchLayout);

		getListView().setFastScrollEnabled(false);
		getListView().setFastScrollAlwaysVisible(false);
		getListView().setOnCreateContextMenuListener(this);
		getListView().auroraSetNeedSlideDelete(false);
		setSlideDelete(false);
		/*getListView().auroraSetAuroraBackOnClickListener(
				new AuroraListView.AuroraBackOnClickListener() {

					@Override
					public void auroraOnClick(final int position) {
						final int pos = getRightPosition(position);
						String message = getActivity().getString(R.string.aurora_remove_group_member_message, 
								((AuroraGroupDetailActivity)getActivity()).getGroupName(), getAdapter().getContactDisplayName(pos));
						AuroraAlertDialog deleteConDialog = new AuroraAlertDialog.Builder(mContext,
								AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
						.setTitle(R.string.gn_remove)
						.setMessage(message)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								int rawContactId = getAdapter().getRawcontactId(pos);
								mContext.getContentResolver().delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=? AND " +
										Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
												new String[] { String.valueOf(rawContactId),
										GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(mGroupId)});
								Toast.makeText(mContext, 
										mContext.getResources().getString(R.string.aurora_remove_group_one_toast, 
												getAdapter().getContactDisplayName(pos)), Toast.LENGTH_SHORT).show();

								//                                                getListView().auroraDeleteSelectedItemAnim(); // remove temp
								getListView().auroraSetRubbishBackNoAnim();
							}
						}).create();

						deleteConDialog.show();
					}

					@Override
					public void auroraPrepareDraged(int position) {
						if (mAlphbetIndexView != null && mContext != null) {
							Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.aurora_contact_prompt_exit);
							mAlphbetIndexView.startAnimation(animation);
							mAlphbetIndexView.setVisibility(View.GONE);
						}
					}

					@Override
					public void auroraDragedSuccess(int position) {
						if (mAlphbetIndexView != null) {
							mAlphbetIndexView.setVisibility(View.GONE);
						}
					}

					@Override
					public void auroraDragedUnSuccess(int position) {
						if (mAlphbetIndexView != null && mContext != null) {
							try {
								Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.aurora_contact_prompt_enter);
								mAlphbetIndexView.startAnimation(animation);
								mAlphbetIndexView.setVisibility(View.VISIBLE);
								mAlphbetIndexView.invalidate();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});*/

		mNoContactsEmptyView = getView().findViewById(
				R.id.no_contacts);
//		addGroupMemberInEmptyView=getView().findViewById(
//				R.id.add_group_member_in_emptyview);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		if (getListView() != null) {
			try {
				getListView().auroraOnPause();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected ContactListAdapter createListAdapter() {
		AuroraGroupDetailAdapter adapter = new AuroraGroupDetailAdapter(getContext());
		adapter.setFilter(ContactListFilter
				.createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS));
		adapter.setSectionHeaderDisplayEnabled(true);
		return adapter;
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
		getListView().auroraOnResume();
		super.onResume();

		bindToAlphdetIndexer();

		if (mGroupId == 0) {
			getListView().auroraSetNeedSlideDelete(false);
		}
	}

	private void showEmptyView(boolean flag) {
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
		if (null != mAlphbetIndexView) {
			mAlphbetIndexView.setList(getListView(), this);
		}
	}

	@Override
	protected void onItemClick(int position, long id) {
		if (getRemoveMemberMode()) {
			return;
		}

		Uri selectUri = getAdapter().getContactUri(position);
		Intent intent = new Intent(Intent.ACTION_VIEW, selectUri);
		intent.putExtra(ContactDetailActivity.INTENT_KEY_FINISH_ACTIVITY_ON_UP_SELECTED, true);
		//        intent.addCategory(IntentFactory.GN_CATEGORY);
		startActivity(intent);
	}

	public void setRemoveMemberMode(boolean flag) {
		mIsRemoveMemberMode = flag;
	}

	public boolean getRemoveMemberMode() {
		return mIsRemoveMemberMode;
	}

	public void changeToNormalMode(boolean flag) {
		if (getActivity() == null) {
			return;
		}

		((AuroraActivity)getActivity()).setMenuEnable(true);
		if (!flag) {
			initActionBar(false);
		}

		//        getListView().auroraSetNeedSlideDelete(true);
		getListView().auroraEnableSelector(true);
//		getListView().addHeaderView(add_group_member, null, false);

		if (!flag) {
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					try {
						getAdapter().getCheckedItem().clear();
						getAdapter().setCheckBoxEnable(false);
						getAdapter().setNeedAnim(true);
						setRemoveMemberMode(false);
						mIsNeedContextMenu = true;
						getAdapter().notifyDataSetChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 330);
		} else {
			getAdapter().getCheckedItem().clear();
			getAdapter().setCheckBoxEnable(false);
			getAdapter().setNeedAnim(true);
			setRemoveMemberMode(false);
			mIsNeedContextMenu = true;
			getAdapter().notifyDataSetChanged();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG,"onCreateContextMenu");
		if (((AuroraGroupDetailActivity)(getActivity())).isSearchviewLayoutShow()) {
			return;
		}

		if (!mIsNeedContextMenu || null == mGroupUri) {
			return;
		}

		super.onCreateContextMenu(menu, v, menuInfo);
		initActionBar(true);
		setRemoveMemberMode(true);
		View targetView = ((AdapterContextMenuInfo)menuInfo).targetView;
		RelativeLayout mainUi = (RelativeLayout)targetView.findViewById(com.aurora.R.id.aurora_listview_front);
		if (null != mainUi && mainUi.getChildAt(0) instanceof ContactListItemView) {
			AdapterView.AdapterContextMenuInfo info;
			try {
				info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			} catch (ClassCastException e) {
				Log.e(TAG, "bad menuInfo", e);
				return;
			}

			Log.d(TAG, "info.id = " + info.id + "   info.po = " + info.position);
			final int pos = getRightPosition(info.position);
			getAdapter().setCheckedItem(Long.valueOf(getAdapter().getContactID(pos)), "");
		}

		mIsNeedContextMenu = false;
		getAdapter().setCheckBoxEnable(true);
		getAdapter().setNeedAnim(true);
		
		//        super.reloadData();
		getListView().auroraSetNeedSlideDelete(false);
		getListView().auroraEnableSelector(false);

		getAdapter().notifyDataSetChanged();

		
		((AuroraActivity)getActivity()).setMenuEnable(false);
		((AuroraGroupDetailActivity)getActivity()).updateSelectedItemsView(mItemCount);
	}

	private TextView middleTextView;
	private void initActionBar(boolean flag) {
		AuroraActionBar actionBar;
		actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
		actionBar.setShowBottomBarMenu(flag);
		//        actionBar.showActionBarMenu();
		actionBar.showActionBarDashBoard();

		if(middleTextView==null){
			middleTextView=actionBar.getMiddleTextView();
		}

//		getListView().removeHeaderView(add_group_member);
		if(middleTextView!=null){
			middleTextView.setText(mContext.getString(R.string.selected_total_num, 1));
		}
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.onItemClick(parent, view, position, id);
		Log.d(TAG, "onItemClick with adapterView: position = " + position + "  id = " + id);

		if (!getRemoveMemberMode()) {
			return;
		}

		Log.d(TAG, "onItemClick1");

		final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		if (null != checkBox) {
			boolean checked = checkBox.isChecked();
			Log.d(TAG,"checked:"+checked);
			checkBox.auroraSetChecked(!checked, true);

			int realPosition = getRightPosition(position);
			if (!checked) {
				getAdapter().setCheckedItem(Long.valueOf(getAdapter().getContactID(realPosition)), "");
			} else {
				getAdapter().getCheckedItem().remove(Long.valueOf(getAdapter().getContactID(realPosition)));
			}

			if(middleTextView!=null){
				middleTextView.setText(mContext.getString(R.string.selected_total_num, getAdapter().getSelectedCount()));
			}

			if (null != getActivity()) {
				((AuroraGroupDetailActivity)(getActivity())).updateSelectedItemsView(mItemCount);
			}
		}
	}

	public void onSelectAll(boolean check) {
		updateListCheckBoxeState(check);

		if(middleTextView!=null){
			middleTextView.setText(mContext.getString(R.string.selected_total_num, getAdapter().getSelectedCount()));
		}

	}

	private void updateListCheckBoxeState(boolean checked) {
		final int headerCount = getListView().getHeaderViewsCount();
		final int count = getAdapter().getCount() + headerCount;
		int contactId = -1;

		for (int position = headerCount; position < count; ++position) {
			int adapterPos = position - headerCount;
			contactId = getAdapter().getContactID(adapterPos);
			Log.d(TAG, "adapterPos = " + adapterPos + "  contactId = " + contactId);
			if (checked) {
				getAdapter().setCheckedItem(Long.valueOf(contactId), "");
			} else {
				getAdapter().getCheckedItem().clear();
			}

			int realPos = position - getListView().getFirstVisiblePosition();
			View view = getListView().getChildAt(realPos);
			if (null != view) {
				final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
				if (null != checkBox) {
					checkBox.auroraSetChecked(checked, true);
				}
			}
		}

		((AuroraGroupDetailActivity)getActivity()).setBottomMenuEnable(checked);
	}

	private int getRightPosition(int position) {
//		if(mIsRemoveMemberMode){
//			return position - 2;
//		}else{
//			return position - 1;
//		}
		return position;
	}
	public ImageButton searchViewBackButton;
	private SvQueryTextListener svQueryTextListener;
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

		mItemCount = getAdapter().getCount();
		((AuroraGroupDetailActivity)getActivity()).updateSelectedItemsView(mItemCount);

		//        if (mItemCount > 0) {
			//            mGotoSearchLayout.setOnClickListener(new View.OnClickListener() {
				//
				//                @Override
		//                public void onClick(View v) {
		//                	Log.d(TAG, "search");
		//                	
		//                	if(mGotoSearchLayout!=null && mGotoSearchLayout.isShown()){
		//						getListView().removeHeaderView(mGotoSearchLayout);
		//						mIsAuroraSearchMode=true;
		//					}
		//                	
		//                	
		//                    // TODO Auto-generated method stub
		//                    ((AuroraActivity) getActivity()).showSearchviewLayout();
		//                    
		//                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);  
		//					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		//					
		//					mSearchView =((AuroraActivity)getActivity()).getAuroraActionBar().getAuroraActionbarSearchView();
		//					mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		//					mSearchView.setMaxLength(30);
		//					initButtomBar(false);
		//                    
		//                    ((AuroraGroupDetailActivity)getActivity()).setRightBtnTv(getAdapter().getCheckedItem().size());
		//                    mSearchView.setOnQueryTextListener(svQueryTextListener);
		//                    mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {
		//                        @Override
		//                        public void onFocusChange(View view, boolean hasFocus) {
		//                            mSearchViewHasFocus = hasFocus;
		//                        }
		//                    });
		//                    setSearchView(mSearchView);
		//                    getListView().auroraSetNeedSlideDelete(false);
		//                }
		//                
		//            });
		//            
		//            if (!getRemoveMemberMode()) {
		//                getListView().auroraEnableSelector(true);
		//            }
		//        }
	}

	private void setQueryTextToFragment(String query) {
		setQueryString(query, true);
		setVisibleScrollbarEnabled(!mIsAuroraSearchMode);
	}

	private final class SvQueryTextListener implements OnQueryTextListener {
		@Override
		public boolean onQueryTextChange(String queryString) {

			Log.d(TAG,"queryString:"+queryString);
			if (queryString.length() > 0) {
				setQueryTextToFragment(queryString);
				mIsAuroraSearchMode = true;
				mAlphbetIndexView.setVisibility(View.GONE);

				//                if (mGotoSearchLayout != null) {
				//                    mGotoSearchLayout.setVisibility(View.GONE);
				//                }

				getListView().auroraSetHeaderViewYOffset(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));

				if (checkIsNeedQueryFromDialer(queryString)) {
					auroraNoMatchView(mIsAuroraSearchMode, null);
				}
			} else {
				setQueryTextToFragment("");
				//                mIsAuroraSearchMode = false;
				//                mAlphbetIndexView.setVisibility(View.VISIBLE);
				//                
				////                if (mGotoSearchLayout != null) {
				////                    mGotoSearchLayout.setVisibility(View.VISIBLE);
				////                }
				//                
				//                getListView().auroraSetHeaderViewYOffset(0);
				//                
				//                auroraNoMatchView(false, null);
			}

			getAdapter().setSearchMode(mIsAuroraSearchMode);
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return true;
		}
	}
}
