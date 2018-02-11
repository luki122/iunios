/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.ui;

import java.util.ArrayList;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.gionee.mms.ui.AddReceiptorTab.ContactsPicker;
import com.gionee.mms.ui.ContactsCacheSingleton.ContactListItemCache;

import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.DataUsageFeedback;
import android.provider.ContactsContract.ProviderStatus;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import com.android.mms.util.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraButton;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
//Gionee <zhouyj> <2013-04-17> add for CR00798863 start
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.gionee.mms.alphbetindex.AlphbetIndexView;
import com.gionee.mms.alphbetindex.ContactsSectionIndexer;
import com.gionee.mms.alphbetindex.HanziToPinyin;
import com.gionee.mms.ui.AddReceiptorTab.ViewPagerVisibilityListener;
//Gionee <zhouyj> <2013-04-17> add for CR00798863 end

//Gionee <zhouyj> <2013-04-20> add for CR00798863 start
// gionee lwzh add for CR00705997 20121002 begin  --- add OnTouchListener
public class ContactFragment extends Fragment implements TextWatcher,
TextView.OnEditorActionListener, ContactsPicker,OnTouchListener, ViewPagerVisibilityListener {
// gionee lwzh add for CR00705997 20121002 end
//Gionee <zhouyj> <2013-04-20> add for CR00798863 end

    private Activity mActivity;

    private static final String TAG = "ContactFragment";
    private static final String LIST_STATE_KEY = "liststate";

    private LinearLayout mTopView;
    private LinearLayout mEmptyView;
    private SearchEditText mSearchEdit;
    private Parcelable mListState = null;
    private AuroraListView mList;
    boolean mJustCreated = false;
    private Drawable mIconSearchDefault;
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
    private boolean mIsNeedShowFirstCharPrompt = false;
    private TextView mFirstCharPromptTextView;
    private AlphbetIndexView mAlphbetIndexView;
    private ArrayList<String> mListFirstNameChar = new ArrayList<String>();
    private ArrayList<String> mListKeys = new ArrayList<String>();
    private ArrayList<Integer> mListCounts = new ArrayList<Integer>();
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
    //Gionee <zhouyj> <2013-04-24> add for CR00801550 start
    private boolean mNeedUpdate = true;
    //Gionee <zhouyj> <2013-04-24> add for CR00801550 end
    
    private static final int MENU_SELECT_CANCEL = 0;
    private static final int MENU_SELECT_DONE   = 1;
    private static final int MENU_SELECT_ALL    = 2;

    final static class PinnedHeaderCache {
        public TextView mTitleView;
        public ColorStateList mTextColor;
        public Drawable mBackground;
    }

    private int mPinnedHeaderBackgroundColor;
    private ContactsListArrayAdapter mAdapter;
    private ArrayList<ContactsCacheSingleton.ContactListItemCache> mContactsList = new ArrayList<ContactsCacheSingleton.ContactListItemCache>();

    private class ContactsListArrayAdapter extends
            ArrayAdapter<ContactsCacheSingleton.ContactListItemCache> implements /*SectionIndexer,*/
            OnScrollListener, PinnedHeaderListView.PinnedHeaderAdapter {

        private boolean mDisplayPhotos = true;
        protected SectionIndexer mIndexer;
        private ContactPhotoLoader mPhotoLoader;
        
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
        private String mLastBindName = null;
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
        
        public ContactPhotoLoader getPhotoLoader() {
            return mPhotoLoader;
        }

        private void bindSectionHeader(View itemView, int position, boolean displaySectionHeaders) {
            final ContactListItemView view = (ContactListItemView) itemView;
            if (!displaySectionHeaders) {
                view.setSectionHeader(null);
                view.setDividerVisible(true);
            } else {
                /*final int section = getSectionForPosition(position);
                Log.d(TAG, "[bindSectionHeader] section = " + section + ", position = " + position);
                if (getPositionForSection(section) == position) {
                    String title = (String) mIndexer.getSections()[section];
                    view.setSectionHeader(title);
                    Log.d(TAG, "[bindSectionHeader] position = " + position + "indexer title = "
                            + title);
                } else {
                    view.setDividerVisible(false);
                    view.setSectionHeader(null);
                }

                // move the divider for the last item in a section
                if (getPositionForSection(section + 1) - 1 == position) {
                    view.setDividerVisible(false);
                } else {
                    view.setDividerVisible(true);
                }*/
            }
        }

        public void bindView(View itemView, Context context, int position) {
            final ContactListItemView view = (ContactListItemView) itemView;
            final ContactsCacheSingleton.ContactListItemCache cache = getItem(position);

            Log.i(TAG, "----------------Multi is check " + cache.mIsChecked);

            view.getCheckBox().setChecked(cache.mIsChecked);
            // Aurora xuyong 2014-07-19 modified for sougou start
            String area = MessageUtils.getNumAreaFromAora(context, cache.mNumber);
            // Aurora xuyong 2014-07-19 modified for sougou end

            if (cache.mNames.isEmpty()) {
                view.getNameTextView().setText(cache.mNumber);
                if (area == null || "".equals(area)) {
                    view.getDataView().setVisibility(View.GONE);
                } else {
                    view.getDataView().setText(area);
                }
                //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
                mLastBindName = cache.mNumber;
                //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
            } else {
                view.getNameTextView().setText(cache.getNameString());
                if (area == null || "".equals(area)) {
                    view.getDataView().setText(cache.mNumber);
                } else {
                    view.getDataView().setText(cache.mNumber + "  " + area);
                }
                //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
                mLastBindName = cache.getNameString();
                //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
            }
            //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
            if (MmsApp.mGnAlphbetIndexSupport) {
                view.setInContactFragment(true);
            }
            //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
        }

        public void checkBoxClicked(int position, View convertView) {
            final ContactListItemView view = (ContactListItemView) convertView;

            view.getCheckBox().setChecked(!view.getCheckBox().isChecked());
            getItem(position).mIsChecked = view.getCheckBox().isChecked();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                final ContactListItemView view = new ContactListItemView(getContext(), null);
                v = (View) view;
            } else {
                v = convertView;
            }
            bindView(v, getContext(), position);
            bindSectionHeader(v, position, true);
            return v;
        }

        public ContactsListArrayAdapter(Context context) {
            super(context, 0);
            mPhotoLoader = new ContactPhotoLoader(context, -1);
        }

        @Override
        public int getPinnedHeaderState(int position) {
            if (mIndexer == null || getCount() == 0) {
                return PINNED_HEADER_GONE;
            }
            if (position < 0) {
                return PINNED_HEADER_GONE;
            }
            // The header should get pushed up if the top item shown
            // is the last item in a section for a particular letter.
            /*int section = getSectionForPosition(position);
            int nextSectionPosition = getPositionForSection(section + 1);
            if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
                return PINNED_HEADER_PUSHED_UP;
            }*/

            return PINNED_HEADER_VISIBLE;
        }

        @Override
        public void configurePinnedHeader(View header, int position, int alpha) {
            PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();
            if (cache == null) {
                cache = new PinnedHeaderCache();
                cache.mTitleView = (TextView) header.findViewById(R.id.header_text);
                cache.mTextColor = cache.mTitleView.getTextColors();
                cache.mBackground = header.getBackground();
                header.setTag(cache);
            }
            /*int section = getSectionForPosition(position);
            String title = (String) mIndexer.getSections()[section];
            Log.d(TAG, "[configurePinnedHeader] indexer title = " + title);
            cache.mTitleView.setText(title);*/

            if (alpha == 255) {
                // Opaque: use the default background, and the original text
                // color
                header.setBackgroundDrawable(cache.mBackground);
                cache.mTitleView.setTextColor(cache.mTextColor);
            } else {
                // Faded: use a solid color approximation of the background, and
                // a translucent text color
                header.setBackgroundColor(Color.rgb(Color.red(mPinnedHeaderBackgroundColor) * alpha
                        / 255, Color.green(mPinnedHeaderBackgroundColor) * alpha / 255,
                        Color.blue(mPinnedHeaderBackgroundColor) * alpha / 255));
                int textColor = cache.mTextColor.getDefaultColor();
                cache.mTitleView.setTextColor(Color.argb(alpha, Color.red(textColor),
                        Color.green(textColor), Color.blue(textColor)));
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            Log.d(TAG, "[onScroll] function called");
            if (view instanceof PinnedHeaderListView) {
                ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                mPhotoLoader.pause();
            } else if (this.mDisplayPhotos) {
                mPhotoLoader.resume();
            }
        }

        //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
        /*@Override
        public int getPositionForSection(int section) {
            if (mIndexer == null) {
                return -1;
            }
            return mIndexer.getPositionForSection(section);
        }

        @Override
        public int getSectionForPosition(int position) {
            if (mIndexer == null) {
                return -1;
            }
            return mIndexer.getSectionForPosition(position);
        }

        @Override
        public Object[] getSections() {
            if (mIndexer == null) {
                
                return new String[] {
                    " "
                };
            } else {
                return mIndexer.getSections();
            }
        }*/
        
        public void setIndexer(SectionIndexer indexer) {
            mIndexer = indexer;
        }
        
        public void setLastBindName(String name) {
            mLastBindName = name;
        }

        public String getLastBindName() {
            return mLastBindName;
        }

        public String getLastBindNameFristChar() {
            if (null == mLastBindName || mLastBindName.length() <= 1) {
                return mLastBindName;
            }
            return mLastBindName.substring(0, 1);
        }
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 end

        private boolean mDisplaySectionHeaders = true;

        public boolean getDisplaySectionHeadersEnabled() {
            return mDisplaySectionHeaders;
        }

        public void setAllItemCheckState(boolean isCheck) {
            // mark all menu item
            // In this situation, may need to do some special work
            for (int index = 0; index < getCount(); index++) {
                ContactsCacheSingleton.ContactListItemCache contact = getItem(index);
                contact.mIsChecked = isCheck;
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private AdapterView.OnItemClickListener mContactListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick function called");
            mAdapter.checkBoxClicked(position, view);
            updateSelectDoneText();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
        mJustCreated = true;
        //Gionee <zhouyj> <2013-04-24> add for CR00801550 start
        mNeedUpdate = true;
        //Gionee <zhouyj> <2013-04-24> add for CR00801550 end
        mActivity = AddReceiptorTab.mCurrent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View fragmentView = inflater.inflate(R.layout.gn_multi_contact_select_list_layout, container, false);
        //mIconSearchDefault = getResources().getDrawable(R.drawable.gn_search_icon);
        mTopView = (LinearLayout) fragmentView.findViewById(R.id.top_view);
        mSearchEdit = (SearchEditText) fragmentView.findViewById(R.id.search_text);
        mSearchEdit.setCompoundDrawablesWithIntrinsicBounds(mIconSearchDefault, null, null, null);

        if (mSearchEdit != null) {
            mSearchEdit.addTextChangedListener(this);
            mSearchEdit.setOnEditorActionListener(this);
        }
        mList = (AuroraListView) fragmentView.findViewById(R.id.list);
        mEmptyView = (LinearLayout) fragmentView.findViewById(R.id.gn_contacts_empty);
        /*TextView textView = (TextView) fragmentView.findViewById(R.id.gn_ic_contact_empty_text);
        if (MmsApp.mDarkStyle) {
            textView.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
        }*/

        // gionee lwzh add for CR00705997 20121002 begin
        mList.setOnTouchListener(this);
        // gionee lwzh add for CR00705997 20121002 end
        
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
        mAlphbetIndexView = (AlphbetIndexView) fragmentView.findViewById(R.id.alphbet_indexer);
        mFirstCharPromptTextView = (TextView) fragmentView.findViewById(R.id.first_char_prompt_tv);
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
        return fragmentView;
    }

    private MenuItem mMenuSelectAllItem;
    private MenuItem mMenuOkItem;
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        // select all
        mMenuSelectAllItem = menu.findItem(R.id.gn_add_select_all);
        mMenuSelectAllItem.setEnabled(true);
        if(mEmptyView.getVisibility() == View.VISIBLE || mList.getCount() == 0) {
            mMenuSelectAllItem.setTitle(R.string.select_all);
            mMenuSelectAllItem.setEnabled(false);
        } else if(isAllChecked()) {
            mMenuSelectAllItem.setTitle(R.string.unselect_all);
        } else {
            mMenuSelectAllItem.setTitle(R.string.select_all);
        }
        
        // ok
        mMenuOkItem = menu.findItem(R.id.gn_add_select_done);
        int checkedCount = ContactsCacheSingleton.getInstance().getCheckedCount();
        if(checkedCount == 0) {
            mMenuOkItem.setTitle(R.string.gn_confirm);
            mMenuOkItem.setEnabled(false);
        } else {
            mMenuOkItem.setTitle(getString(R.string.gn_confirm) + "(" + checkedCount + ")");
            mMenuOkItem.setEnabled(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.gn_add_select_all:
            mAdapter.setAllItemCheckState(item.getTitle().toString().equals(getString(R.string.select_all)));
            updateSelectDoneText();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        mList.setOnCreateContextMenuListener(this);
        mList.setOnScrollListener(mAdapter);
        //Gionee <zhouyj> <2013-04-25> modify for CR00796299 start
        if (MmsApp.mGnAlphbetIndexSupport) {
            mAdapter = new ContactsListArrayAdapterForAlphbetIndex(getActivity());
        } else {
            mAdapter = new ContactsListArrayAdapter(getActivity());
        }
        //Gionee <zhouyj> <2013-04-25> modify for CR00796299 start
        // We manually save/restore the listview state
        mList.setSaveEnabled(false);
        mList.setOnItemClickListener(mContactListClickListener);

        if (mList instanceof PinnedHeaderListView && mAdapter.getDisplaySectionHeadersEnabled()) {
            Log.i(TAG, "Pinnedheader AuroraListView support");

            mPinnedHeaderBackgroundColor = getResources()
                    .getColor(R.color.pinned_header_background);
            PinnedHeaderListView pinnedHeaderList = (PinnedHeaderListView) mList;
            View pinnedHeader = getActivity().getLayoutInflater().inflate(R.layout.gn_list_section, mList, false);
            pinnedHeaderList.setPinnedHeaderView(pinnedHeader);
        }
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
        updateData();
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 end
    }
    
    //Gionee <zhouyj> <2013-04-24> add for CR00801550 start
    private void updateData() {
        if (ContactsCacheSingleton.getInstance().getPhoneBookContactCount() != 0) {
            mNeedUpdate = false;
            mTopView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);

            if (mListState != null) {
                mList.onRestoreInstanceState(mListState);
                mListState = null;
            }
            // gionee zhouyj 2012-04-19 add for CR00573912 start
            mContactsList.clear();
            // gionee zhouyj 2012-04-19 add for CR00573912 end
            mAdapter.clear();
            for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getContactList()) {

                    mAdapter.add(cache);

                    // make a copy
                    mContactsList.add(cache);
            }
            mList.setAdapter(mAdapter);
            //updateSelectDoneText();
            //Gionee <zhouyj> <2013-04-20> modify for CR00798863 start
            if (MmsApp.mGnAlphbetIndexSupport) {
                mAlphbetIndexView.setVisibility(View.VISIBLE);
                //Gionee <zhouyj> <2013-05-13> add for CR00807616 begin
                new Thread(mInitAlphbetIndexViewRunnable).start();
                //Gionee <zhouyj> <2013-05-13> add for CR00807616 end
            }
            //Gionee <zhouyj> <2013-04-20> modify for CR00798863 end
        } else {
            mTopView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
            //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
            mAlphbetIndexView.setVisibility(View.GONE);
            //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
            mNeedUpdate = true;
        }
    }
    //Gionee <zhouyj> <2013-04-24> add for CR00801550 end
    
    //Gionee <zhouyj> <2013-04-25> add for CR00796299 start
    /**
     * ContactsListArrayAdapterForAlphbetIndex for AlphbetIndexView
     */
    private class ContactsListArrayAdapterForAlphbetIndex extends ContactsListArrayAdapter implements SectionIndexer{

        public ContactsListArrayAdapterForAlphbetIndex(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        public int getPositionForSection(int section) {
            if (mIndexer == null) {
                return -1;
            }
            return mIndexer.getPositionForSection(section);
        }

        @Override
        public int getSectionForPosition(int position) {
            if (mIndexer == null) {
                return -1;
            }
            return mIndexer.getSectionForPosition(position);
        }

        @Override
        public Object[] getSections() {
            if (mIndexer == null) {
                return new String[] {" "};
            } else {
                return mIndexer.getSections();
            }
        }
        
    }
    //Gionee <zhouyj> <2013-04-25> add for CR00796299 end
    
    public void updateAdapter() {
        //Gionee <zhouyj> <2013-05-13> add for CR00807616 begin
        if (mTopView != null && mEmptyView != null && mList != null) {
            updateData();
        }
        //Gionee <zhouyj> <2013-05-13> add for CR00807616 end
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
        if (mAdapter != null && mAdapter.isEmpty()) {
            for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getContactList()) {
                mAdapter.add(cache);
                // make a copy
                mContactsList.add(cache);
            }
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getCount() > 0) {
                mEmptyView.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
            }
        }
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 end
        //Gionee <zhouyj> <2013-05-13> add for CR00807616 begin
        if (mActivity != null) {
            mActivity.invalidateOptionsMenu();
        }
        //Gionee <zhouyj> <2013-05-13> add for CR00807616 end
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        hideSoftKeyboard();
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("MMS", "ContactFragment...........onResume");
        registerProviderStatusObserver();
        if (mJustCreated == true) {
            mJustCreated = false;
        } else {
            mAdapter.notifyDataSetChanged();
        }
        updateSelectDoneText();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mSearchEdit != null) {
            hideSoftKeyboard();
        }
        unregisterProviderStatusObserver();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //Gionee <zhouyj> <2013-06-04> modify for CR00822542 begin
        if (mAdapter != null && mAdapter.getPhotoLoader() != null) {
            mAdapter.getPhotoLoader().stop();
        }
        //Gionee <zhouyj> <2013-06-04> modify for CR00822542 end
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
        mNeedUpdate = false;
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 end
    }

    @Override
    public void onSaveInstanceState(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(icicle);
        // Save list state in the bundle so we can restore it after the
        // QueryHandler has run
        if (mList != null) {
            icicle.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
        }
    }

    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void registerProviderStatusObserver() {
        getActivity().getContentResolver().registerContentObserver(ProviderStatus.CONTENT_URI, false,
                mProviderStatusObserver);
    }

    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void unregisterProviderStatusObserver() {
        getActivity().getContentResolver().unregisterContentObserver(mProviderStatusObserver);
    }

    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // checkProviderState(true);
        }
    };

    protected void onSearchTextChanged() {
        mAdapter.clear();
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
        if (MmsApp.mGnAlphbetIndexSupport) {
            mListFirstNameChar.clear();
            mListKeys.clear();
            mListCounts.clear();
        }
        String name = null;
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
        if (TextUtils.isEmpty(getTextFilter())) {
            for (ContactsCacheSingleton.ContactListItemCache cache : mContactsList) {
                mAdapter.add(cache);
                //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
                if (MmsApp.mGnAlphbetIndexSupport) {
                    name = cache.getNameString();
                    if (TextUtils.isEmpty(name) || name.length() < 1) {
                        name = "#";
                    } else {
                        char ch = name.charAt(0);
                        if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '\u4e00' && ch <= '\u9fa5')) {
                            name = HanziToPinyin.getInstance().HanziToPinyinString(name).substring(0, 1);
                        } else {
                            name = "#";
                        }
                    }
                    mListFirstNameChar.add(name);
                }
                //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
            }
        } else {
            Cursor filterCursor = runQueryOnBackgroundThread(getTextFilter());
            if (filterCursor != null && filterCursor.getCount() > 0) {
                filterCursor.moveToPosition(-1);
                while (filterCursor.moveToNext()) {
                    //Gionee <zhouyj> <2013-07-29> modify for CR00843669 begin
                    String number = filterCursor.getString(filterCursor
                            .getColumnIndex(Phone.NUMBER));
                    if (TextUtils.isEmpty(number)) {
                        continue;
                    }
                    number = number.replaceAll(" ", "").replaceAll("-", "");
                    //Gionee <zhouyj> <2013-07-29> modify for CR00843669 end
                    ContactsCacheSingleton.ContactListItemCache cache = ContactsCacheSingleton
                            .getInstance().getContactItem(number);
                    if (cache != null) {
                        boolean resultInList = false;
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            ContactsCacheSingleton.ContactListItemCache temp = mAdapter.getItem(i);
                            if (cache == temp) {
                                resultInList = true;
                            }
                        }
                        if (resultInList == false) {
                            mAdapter.add(cache);
                            //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
                            if (MmsApp.mGnAlphbetIndexSupport) {
                                name = cache.getNameString();
                                if (TextUtils.isEmpty(name) || name.length() < 1) {
                                    name = "#";
                                } else {
                                    char ch = name.charAt(0);
                                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '\u4e00' && ch <= '\u9fa5')) {
                                        name = HanziToPinyin.getInstance().HanziToPinyinString(name).substring(0, 1);
                                    } else {
                                        name = "#";
                                    }
                                }
                                mListFirstNameChar.add(name);
                            }
                            //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
                        }
                    }
                }
            }
            if (filterCursor != null) {
                filterCursor.close();
            }
        }
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
        if (MmsApp.mGnAlphbetIndexSupport) {
            mListFirstNameChar.add("#");
            spit(mListFirstNameChar);
            mAdapter.setIndexer(new ContactsSectionIndexer(mListKeys.toArray(), mListCounts.toArray()));
            mAlphbetIndexView.setList(mList, new ScrollListener());
        }
        //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Event handler for search UI.
     */
    public void afterTextChanged(Editable s) {
        onSearchTextChanged();
        // gionee zhouyj 2012-10-11 add for CR00710504 start 
        getActivity().invalidateOptionsMenu();
        // gionee zhouyj 2012-10-11 add for CR00710504 end 
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO Auto-generated method stub
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideSoftKeyboard();
            if (TextUtils.isEmpty(getTextFilter())) {
                getActivity().finish();
            }
            return true;
        }
        return false;
    }

    public void updateSelectDoneText() {
        mAdapter.notifyDataSetChanged();
        // gionee zhouyj 2013-01-23 modify for CR00765961 start 
        if (mActivity != null) {
            mActivity.invalidateOptionsMenu();
        }
        // gionee zhouyj 2013-01-23 modify for CR00765961 end 
    }
    
    private boolean isAllChecked() {
        boolean isAllChecked = true;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            ContactsCacheSingleton.ContactListItemCache cache = mAdapter.getItem(i);
            if (cache.mIsChecked == false) {
                isAllChecked = false;
                break;
            }
        }
        return isAllChecked;
    }

    private String getTextFilter() {
        return mSearchEdit.getText().toString();
    }

    private boolean usefulAsDigits(CharSequence cons) {
        int len = cons.length();
        for (int i = 0; i < len; i++) {
            char c = cons.charAt(i);

            if ((c >= '0') && (c <= '9')) {
                continue;
            }
            if ((c == ' ') || (c == '-') || (c == '(') || (c == ')') || (c == '.') || (c == '+')
                    || (c == '#') || (c == '*')) {
                continue;
            }
            if ((c >= 'A') && (c <= 'Z')) {
                continue;
            }
            if ((c >= 'a') && (c <= 'z')) {
                continue;
            }
            return false;
        }

        return true;
    }

    private static final String[] PROJECTION_PHONE = {
            Phone._ID, // 0
            Phone.CONTACT_ID, // 1
            Phone.TYPE, // 2
            Phone.NUMBER, // 3
            Phone.LABEL, // 4
            Phone.DISPLAY_NAME, // 5
    };

    private static final String SORT_ORDER = Contacts.TIMES_CONTACTED + " DESC,"
            + Contacts.DISPLAY_NAME + "," + Phone.TYPE;

    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String phone = "";
        String cons = null;
        if (constraint != null) {
            cons = constraint.toString();
            if (usefulAsDigits(cons)) {
                phone = PhoneNumberUtils.convertKeypadLettersToDigits(cons);
                if (phone.equals(cons)) {
                    phone = "";
                } else {
                    phone = phone.trim();
                }
            }
        }
        //gionee gaoj 2012-8-11 added for CR00663879 start
        Uri uri = Phone.CONTENT_FILTER_URI.buildUpon()
            .appendPath(cons)
            .appendQueryParameter(DataUsageFeedback.USAGE_TYPE,
                DataUsageFeedback.USAGE_TYPE_SHORT_TEXT)
            .build();
        Cursor phoneCursor = getActivity().getContentResolver().query(uri,
            PROJECTION_PHONE,
            null, //selection,
            null,
            Contacts.SORT_KEY_PRIMARY);
        /*Uri baseUri = Uri.parse("content://com.android.contacts/data/phone_email/filter");
        Uri uri = Uri.withAppendedPath(baseUri, Uri.encode(cons));

        String selection = String.format("%s=%s OR %s=%s OR %s=%s OR %s=%s OR %s=%s OR %s=%s OR %s=%s",
                Phone.TYPE,
                Phone.TYPE_HOME,
                Phone.TYPE,
                Phone.TYPE_MOBILE,
                Phone.TYPE,
                Phone.TYPE_WORK,
                Phone.TYPE,
                Phone.TYPE_OTHER,
                Phone.TYPE,
                Phone.TYPE_CUSTOM,
                Phone.TYPE,
                Phone.TYPE_MMS,
                Phone.TYPE,
                Phone.TYPE_FAX_WORK
                );
        Cursor phoneCursor = getActivity().getContentResolver().query(uri, PROJECTION_PHONE, selection, // selection,
                null, SORT_ORDER);*/
        //gionee gaoj 2012-8-11 added for CR00663879 end

        return phoneCursor;
    }
    
    public void markAll() {
        mAdapter.setAllItemCheckState(true);
        updateSelectDoneText();
    }

    @Override
    public void updateButton() {
        updateSelectDoneText();
    }

    
    // gionee lwzh add for CR00705997 20121002 begin
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mList) {
            hideSoftKeyboard();
        }
        return false;
    }
    // gionee lwzh add for CR00705997 20121002 end
    
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
    public void spit(ArrayList<String> list) {
        synchronized (this) {
            mListKeys.clear();
            mListCounts.clear();
            if (list == null || list.size() < 1) {
                return ;
            }
            int count = 0;
            String prev = "";
            String next = "";
            for (int i = 1; i < list.size(); i++) {
                prev = list.get(i - 1);
                next = list.get(i);
                count++;
                if (!prev.equals(next)) {
                    mListKeys.add(prev);
                    mListCounts.add(Integer.valueOf(count));
                    count = 0;
                }
            }
        }
    }
    
    private void showFirstCharPrompt(boolean show) {
        if (null == mFirstCharPromptTextView) {
            return;
        }

        if (show && mIsNeedShowFirstCharPrompt) {
            String firstChar = mAdapter.getLastBindNameFristChar();
            if (null != mAlphbetIndexView && !TextUtils.isEmpty(firstChar)) {
                if (!firstChar.equals(mFirstCharPromptTextView.getText()
                        .toString())) {
                    mFirstCharPromptTextView.setText(firstChar);
                }
                if (mFirstCharPromptTextView.getVisibility() != View.VISIBLE) {
                    mFirstCharPromptTextView.setVisibility(View.VISIBLE);
                }
                return;
            }
        }

        if (mFirstCharPromptTextView.getVisibility() == View.VISIBLE) {
            mFirstCharPromptTextView.setVisibility(View.GONE);
        }
    }
    
    private class ScrollListener implements AuroraListView.OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            showFirstCharPrompt(true);
            if (null != mAlphbetIndexView) {
                mAlphbetIndexView.invalidateShowingLetterIndex();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                showFirstCharPrompt(false);
                mIsNeedShowFirstCharPrompt = false;
            } else {
                mIsNeedShowFirstCharPrompt = true;
            }
        }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        // TODO Auto-generated method stub
        //Gionee <zhouyj> <2013-05-13> modify for CR00807616 begin
        if (visible && mNeedUpdate){
            updateData();
        //Gionee <zhouyj> <2013-06-27> add for CR00830950 begin
        } else if (!visible) {
            hideSoftKeyboard();
        //Gionee <zhouyj> <2013-06-27> add for CR00830950 end
        }
        //Gionee <zhouyj> <2013-05-13> modify for CR00807616 end
    }
    
    //Gionee <zhouyj> <2013-05-13> add for CR00807616 begin
    private Runnable mInitAlphbetIndexViewRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mListFirstNameChar.clear();
            String name = null;
            char ch;
            for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getContactList()) {
                name = cache.getNameString();
                if (TextUtils.isEmpty(name) || name.length() < 1) {
                    name = "#";
                } else {
                    ch = name.charAt(0);
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '\u4e00' && ch <= '\u9fa5')) {
                        name = HanziToPinyin.getInstance().HanziToPinyinString(name).substring(0, 1);
                    } else {
                        name = "#";
                    }
                }
                mListFirstNameChar.add(name);
            }
            mListFirstNameChar.add("#");
            spit(mListFirstNameChar);
            mHandler.sendEmptyMessage(0);
        }
    };
    
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mAlphbetIndexView.setVisibility(View.VISIBLE);
            mAdapter.setIndexer(new ContactsSectionIndexer(mListKeys.toArray(), mListCounts.toArray()));
            mAlphbetIndexView.setList(mList, new ScrollListener());
        };
    };
    //Gionee <zhouyj> <2013-05-13> add for CR00807616 end
}
