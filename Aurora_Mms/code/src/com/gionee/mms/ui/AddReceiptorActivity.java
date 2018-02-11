package com.gionee.mms.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.gionee.mms.ui.ContactsCacheSingleton.ContactListItemCache;

import android.app.ActionBar;
import aurora.app.AuroraActivity;
import android.app.Dialog;
import aurora.app.AuroraProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.ProviderStatus;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import com.android.mms.util.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import aurora.widget.AuroraExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.SectionIndexer;
import aurora.widget.AuroraSpinner;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import aurora.widget.AuroraExpandableListView.OnChildClickListener;

public class AddReceiptorActivity extends AuroraActivity implements TextWatcher,
TextView.OnEditorActionListener, View.OnClickListener{

    private static final int MODE_ADD_CONTACT = 1;
    private static final int MODE_INSERT_CONTACT = 2;
    int mMode = MODE_ADD_CONTACT;
    private static final String TAG = "AddReceiptorActivity";
    static AuroraActivity mCurrent = null;
    private boolean mUseOldCache = false;

    private static final int DIALOG_ADD = 1;
    AuroraProgressDialog dialog;

    private LinearLayout mEmptyView;
    private TextView mEmptyTextView;
    private TextView mActionBarText;
    private ImageButton mSelAllBtn;

    private int mRecentPosition = 0;
    private int mContactPosition = 1;
    private int mGroupPosition = 2;
    //not use star, so set position -1
    private int mStarPosition = -1;

    //contact   //recent
    private AuroraListView mList;
    private ContactsListArrayAdapter mAdapter;
    private ContactsListArrayAdapter mRecentAdapter;
    private ArrayList<ContactsCacheSingleton.ContactListItemCache> mContactsList = new ArrayList<ContactsCacheSingleton.ContactListItemCache>();
    private Drawable mIconSearchDefault;
    private LinearLayout mTopView;
    private SearchEditText mSearchEdit;
    final static class PinnedHeaderCache {
        public TextView mTitleView;
        public ColorStateList mTextColor;
        public Drawable mBackground;
    }
    private int mPinnedHeaderBackgroundColor;

    //group start
    private AuroraExpandableListView mExpandList;
    private ContactGroupExpandableListAdapter mExpandAdapter;
    private QueryHandler mGroupQueryHandler;
    private QueryHandler mStartQueryHandler;

    public static interface DataContactsQuery {
        final String GROUP_MEMBER_SHIP = Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                + "'";
        final String CONTACTS_SORDORDER = "sort_key";
        final String[] CONTACTS_PROJECTION = new String[] {
                Data._ID, Data.CONTACT_ID, Data.RAW_CONTACT_ID, Data.DISPLAY_NAME, Data.PHOTO_ID
        };

        final int DATA_ID = 0;
        final int DATA_CONTACT_ID = 1;
        final int DATA_RAW_CONTACT_ID = 2;
        final int DATA_DISPLAY_NAME = 3;
        final int DATA_PHOTO_ID = 4;
    }
    private static final String INDICATE_PHONE_SIM = "indicate_phone_or_sim_contact";//Contacts.INDICATE_PHONE_SIM,
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            Contacts._ID, // 0
            Contacts.DISPLAY_NAME_PRIMARY, // 1
            Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
            Contacts.SORT_KEY_PRIMARY, // 3
            Contacts.STARRED, // 4
            Contacts.TIMES_CONTACTED, // 5
            Contacts.CONTACT_PRESENCE, // 6
            Contacts.PHOTO_ID, // 7
            Contacts.LOOKUP_KEY, // 8
            Contacts.PHONETIC_NAME, // 9
            Contacts.HAS_PHONE_NUMBER, // 10
            INDICATE_PHONE_SIM,//Contacts.INDICATE_PHONE_SIM, // 11
    };

    static final int SUMMARY_ID_COLUMN_INDEX = 0;
    static final int SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX = 1;
    static final int SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX = 2;
    static final int SUMMARY_SORT_KEY_PRIMARY_COLUMN_INDEX = 3;
    static final int SUMMARY_STARRED_COLUMN_INDEX = 4;
    static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 5;
    static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 6;
    static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 7;
    static final int SUMMARY_LOOKUP_KEY_COLUMN_INDEX = 8;
    static final int SUMMARY_PHONETIC_NAME_COLUMN_INDEX = 9;
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 10;
    static final int SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX = 11;
    static final int SUMMARY_SNIPPET_DATA1_COLUMN_INDEX = 12;
    static final int SUMMARY_SNIPPET_DATA4_COLUMN_INDEX = 13;

    private Parcelable mListState = null;
    private static final String LIST_STATE_KEY = "liststate";

    protected int mPos;
    protected String mSelection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        //gionee gaoj 2012-3-31 added for CR00601661 start
        if (MmsApp.mTransparent) {
            setTheme(R.style.TabActivityTheme);
        }
        //gionee gaoj 2012-3-31 added for CR00601661 end
        mCurrent = this;
        setContentView(R.layout.gn_add_receiptor_activity);

        //contact recent and calllog
        mList = (AuroraListView) findViewById(R.id.list);

        //grop star
        mExpandList  = (AuroraExpandableListView) findViewById(R.id.group_expandable_list);
        mGroupQueryHandler = new QueryHandler(this, mGroupPosition);
        mStartQueryHandler = new QueryHandler(this, mStarPosition);

        mEmptyView = (LinearLayout) findViewById(R.id.gn_empty);
        mEmptyTextView = (TextView) findViewById(R.id.gn_empty_text);

        setupActionBar();

        // default mode
        mMode = MODE_ADD_CONTACT;
        Bundle bundle = getIntent().getExtras();

        // mode indicate that activity started for inserting contact info as
        // text message
        if (bundle != null && bundle.getBoolean("InsertContact")) {
            mMode = MODE_INSERT_CONTACT;
        }

        if (mMode == MODE_INSERT_CONTACT) {
            setTitle(getResources().getString(R.string.insert_contacts));
        } else if (mMode == MODE_ADD_CONTACT) {
            setTitle(getResources().getString(R.string.title_add_receipents));
        }

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }

        ArrayList<String> contactNumbers = null;
        if (bundle != null) {
            contactNumbers = bundle.getStringArrayList("ContactNumbers");
        }

        if (bundle != null && bundle.getBoolean("useoldcache") == true) {
            mUseOldCache = true;
        }

        if (mUseOldCache == true && ContactsCacheSingleton.getInstance().isInited()) {
            Log.i(TAG, " useoldcache");
            ContactsCacheSingleton.getInstance().setCheckedNumber(contactNumbers);
        } else {
            ContactsCacheSingleton.getInstance().init(contactNumbers, getApplicationContext());
            ContactsCacheSingleton.getInstance().starQueryNumbers();
        }

        ContactsCacheSingleton.getInstance().setOnQueryCompleteListener(
                new ContactsCacheSingleton.onQueryCompleteListener() {

                    @Override
                    public void onQueryComplete(ArrayList<ContactListItemCache> result) {

                        initContactAdapter();
                        initRecentandCallLogAdapter();
                        initGroupandStarAdapter();
                        setSpanner();
                    }
                });
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        hideSoftKeyboard();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        registerProviderStatusObserver();

        AuroraSpinner restoreSpinner = (AuroraSpinner)findViewById(R.id.gn_receiptor_spinner);
        restoreSpinner.setSelection(getSpinnerPosition());
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mSearchEdit != null) {
            hideSoftKeyboard();
        }

        unregisterProviderStatusObserver();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (dialog != null) {
            removeDialog(DIALOG_ADD);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mAdapter.getPhotoLoader().stop();
        if (!mUseOldCache && mCurrent == this) {
                Log.i(TAG, " addReci destoryInstance");
                ContactsCacheSingleton.destoryInstance();
        }
    }

    private void setupActionBar() {
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.gn_receiptor_actionbar, null);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        ImageButton quit = (ImageButton) v.findViewById(R.id.gn_back_button);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mActionBarText = (TextView) v.findViewById(R.id.gn_attach_count);
        mSelAllBtn = (ImageButton) v.findViewById(R.id.gn_selallbtn);
        mSelAllBtn.setOnClickListener(mSelAllListener);
        actionBar.setCustomView(v);
    }

    private void setSpanner() {
        AuroraSpinner spinner = (AuroraSpinner) findViewById(R.id.gn_receiptor_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gn_receiptors,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        OnItemSelectedListener spinnerListene=  new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v,
                    int position, long row) {
                mPos = position;
                mSelection = parent.getItemAtPosition(position).toString();
                if (mPos == mRecentPosition) {

                    mExpandList.setVisibility(View.GONE);
                    if (mRecentAdapter.getCount() > 0 || ContactsCacheSingleton.getInstance().getCalllogContactCount() != 0) {
                        mEmptyView.setVisibility(View.GONE);
                        mTopView.setVisibility(View.GONE);

                        if (mListState != null) {
                            mList.onRestoreInstanceState(mListState);
                            mListState = null;
                        }

                        mList.setVisibility(View.VISIBLE);
                        mList.setAdapter(mRecentAdapter);
                        mList.setOnItemClickListener(mRecentListClickListener);
                        updateSelectDoneText(mRecentAdapter);
                    } else {
                        mList.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                        mEmptyTextView.setText(R.string.gn_no_recent_contacts);
                    }

                } else if (mPos == mContactPosition) {

                    mExpandList.setVisibility(View.GONE);
                    if (ContactsCacheSingleton.getInstance().getPhoneBookContactCount() != 0) {
                        mEmptyView.setVisibility(View.GONE);
                        mList.setVisibility(View.VISIBLE);
                        mTopView.setVisibility(View.VISIBLE);

                        if (mListState != null) {
                            mList.onRestoreInstanceState(mListState);
                            mListState = null;
                        }

                        mList.setSaveEnabled(false);
                        mList.setAdapter(mAdapter);
                        mList.setOnScrollListener(mAdapter);
                        mList.setOnItemClickListener(mContactListClickListener);
                        updateSelectDoneText(mAdapter);
                    } else {
                        mList.setVisibility(View.GONE);
                        mTopView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                        mEmptyTextView.setText(R.string.gn_no_contacts);
                    }
                } else if (mPos == mGroupPosition) {

                    mList.setVisibility(View.GONE);
                    mTopView.setVisibility(View.GONE);

                    //mExpandList.setGroupIndicator(getResources().getDrawable(R.drawable.gn_expander_ic_folder));

                    mExpandList.setSaveEnabled(false);

                    mExpandList.setOnChildClickListener(new OnChildClickListener() {
                        @Override
                        public boolean onChildClick(AuroraExpandableListView arg0, View arg1, int arg2, int arg3,
                                long arg4) {
                            mExpandAdapter.childCheckBoxClicked(arg2, arg3, arg1);

                            updateGroupSelect();
                            return true;
                        }
                    });

                    mExpandAdapter.setGroupCheckBoxClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            CheckBox chk = (CheckBox) arg0;

                            int groupPos = (Integer) chk.getTag();

                            mExpandAdapter.groupCheckBoxClicked(groupPos, chk.isChecked());

                            updateGroupSelect();

                            if (mExpandAdapter.getAllChildCount() == 0) {
                                return;
                            }
                        }
                    });
                    mGroupQueryHandler.startQuery(0, null, null, null, null, null, null);
                } else if (mPos == mStarPosition) {

                    mList.setVisibility(View.GONE);
                    mTopView.setVisibility(View.GONE);

                    //mExpandList.setGroupIndicator(getResources().getDrawable(R.drawable.gn_expander_ic_folder));

                    mExpandList.setSaveEnabled(false);

                    mExpandList.setOnChildClickListener(new OnChildClickListener() {
                        @Override
                        public boolean onChildClick(AuroraExpandableListView arg0, View arg1, int arg2, int arg3,
                                long arg4) {
                            mExpandAdapter.childCheckBoxClicked(arg2, arg3, arg1);

                            updateGroupSelect();
                            return true;
                        }
                    });

                    mExpandAdapter.setGroupCheckBoxClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            CheckBox chk = (CheckBox) arg0;

                            int groupPos = (Integer) chk.getTag();

                            mExpandAdapter.groupCheckBoxClicked(groupPos, chk.isChecked());

                            updateGroupSelect();

                            if (mExpandAdapter.getAllChildCount() == 0) {
                                return;
                            }
                        }
                    });
                    mStartQueryHandler.startQuery(0, null, null, null, null, null, null);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
        spinner.setOnItemSelectedListener(spinnerListene);
    }

    public int getSpinnerPosition() {
        return this.mPos;
    }

    public void setSpinnerPosition(int pos) {
        this.mPos = pos;
    }

    public String getSpinnerSelection() {
        return this.mSelection;
    }

    public void setSpinnerSelection(String selection) {
        this.mSelection = selection;
    }
    
    private ContactsListArrayAdapter getReceiptorAdapter() {
        ContactsListArrayAdapter adapter = null;
        if (mPos == mContactPosition) {
            adapter = mAdapter;
        } else if (mPos == mRecentPosition) {
            adapter = mRecentAdapter;
        }
        return adapter;
    }

    private void initContactAdapter() {

       // mIconSearchDefault = getResources().getDrawable(R.drawable.gn_search_icon);
        mTopView = (LinearLayout) findViewById(R.id.top_view);

        mSearchEdit = (SearchEditText) findViewById(R.id.search_text);

        mSearchEdit.setCompoundDrawablesWithIntrinsicBounds(mIconSearchDefault, null, null, null);
        if (mSearchEdit != null) {
            mSearchEdit.addTextChangedListener(this);
            mSearchEdit.setOnEditorActionListener(this);
            mSearchEdit.setOnClickListener(this);
        }

        mAdapter = new ContactsListArrayAdapter(this);

        mContactsList.clear();
        for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getContactList()) {

                mAdapter.add(cache);

                // make a copy
                mContactsList.add(cache);
        }
    }

    private void initRecentandCallLogAdapter() {

        mRecentAdapter = new ContactsListArrayAdapter(AddReceiptorActivity.this);

        for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getRecentCallLogList()) {
            mRecentAdapter.add(cache);
        }

    }

    private void initGroupandStarAdapter() {

        mExpandAdapter = new ContactGroupExpandableListAdapter(this);
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO Auto-generated method stub
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideSoftKeyboard();
            if (TextUtils.isEmpty(getTextFilter())) {
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub

        onSearchTextChanged();
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
            int arg3) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        
    }

    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
    }

    private String getTextFilter() {
        return mSearchEdit.getText().toString();
    }

    //contact  recent and calllog
    private final class ContactsListArrayAdapter extends
            ArrayAdapter<ContactsCacheSingleton.ContactListItemCache> implements SectionIndexer,
            OnScrollListener, PinnedHeaderListView.PinnedHeaderAdapter {

        private boolean mDisplayPhotos = true;
        private SectionIndexer mIndexer;

        private ContactPhotoLoader mPhotoLoader;

        public ContactPhotoLoader getPhotoLoader() {
            return mPhotoLoader;
        }

        private void bindSectionHeader(View itemView, int position, boolean displaySectionHeaders) {
            final ContactListItemView view = (ContactListItemView) itemView;
            if (!displaySectionHeaders) {
                view.setSectionHeader(null);
                view.setDividerVisible(true);
            } else {
                final int section = getSectionForPosition(position);
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
                }
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
            } else {
                view.getNameTextView().setText(cache.getNameString());
                if (area == null || "".equals(area)) {
                    view.getDataView().setText(cache.mNumber);
                } else {
                    view.getDataView().setText(cache.mNumber + "  " + area);
                }
            }
        }

        public void checkBoxClicked(int position, View convertView) {
            final ContactListItemView view = (ContactListItemView) convertView;

            view.getCheckBox().setChecked(!view.getCheckBox().isChecked());
            getItem(position).mIsChecked = view.getCheckBox().isChecked();
        }


        public int getCheckedCount() {
            int checkedCount = 0;
            for (int i = 0; i < getCount(); i++) {
                if (getItem(i).mIsChecked) {
                    checkedCount++;
                }
            }
            return checkedCount;
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
            int section = getSectionForPosition(position);
            int nextSectionPosition = getPositionForSection(section + 1);
            if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
                return PINNED_HEADER_PUSHED_UP;
            }

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

            int section = getSectionForPosition(position);

            String title = (String) mIndexer.getSections()[section];
            Log.d(TAG, "[configurePinnedHeader] indexer title = " + title);
            cache.mTitleView.setText(title);

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
                return new String[] {
                    " "
                };
            } else {
                return mIndexer.getSections();
            }
        }

        private boolean mDisplaySectionHeaders = true;

        public boolean getDisplaySectionHeadersEnabled() {
            return mDisplaySectionHeaders;
        }

        public void setAllItemCheckState(boolean isCheck, ContactsListArrayAdapter adapter) {
            // mark all menu item
            // In this situation, may need to do some special work
            for (int index = 0; index < getCount(); index++) {
                ContactsCacheSingleton.ContactListItemCache contact = getItem(index);
                contact.mIsChecked = isCheck;
            }

            adapter.notifyDataSetChanged();
        }
    }

    //group and star  atapter
    public static class ContactGroupExpandableListAdapter extends BaseExpandableListAdapter {
        private Context mContext;

        public class ContactGroupData {
            String mName = null;
            ArrayList<ContactsCacheSingleton.ContactListItemCache> mChildren = null;
        }

        private ArrayList<ContactGroupData> mGroups = new ArrayList<ContactGroupData>();

        public void addOneGroup(String groupName,
                ArrayList<ContactsCacheSingleton.ContactListItemCache> children) {
            ContactGroupData oneGroup = new ContactGroupData();
            oneGroup.mName = groupName;
            oneGroup.mChildren = children;
            mAllChildCount += children.size();
            mGroups.add(oneGroup);
        }

        public void removeAllGroup() {
            mAllChildCount = 0;
            mGroups.clear();
        }

        private OnClickListener mCheckBoxClickListener;

        public void setGroupCheckBoxClickListener(View.OnClickListener listener) {
            mCheckBoxClickListener = listener;
        }

        public ContactGroupExpandableListAdapter(Context context) {
            mContext = context;
        }

        public Object getChild(int groupPosition, int childPosition) {
            return mGroups.get(groupPosition).mChildren.get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition * 1000 + childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return mGroups.get(groupPosition).mChildren.size();
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            Log.i(TAG, "groupPosition = " + groupPosition);

            ContactListItemView view;
            if (convertView == null) {
                view = new ContactListItemView(mContext, null);
            } else {
                view = (ContactListItemView) convertView;
            }

            ContactsCacheSingleton.ContactListItemCache cache = mGroups.get(groupPosition).mChildren
                    .get(childPosition);

            view.getCheckBox().setChecked(cache.mIsChecked);
            view.getNameTextView().setText(cache.getNameString());
            view.getNameTextView().setPadding(10, 0, 0, 0);
            view.getDataView().setPadding(10, 0, 0, 0);
            // Aurora xuyong 2014-07-19 modified for sougou start
            String area = MessageUtils.getNumAreaFromAora(mContext, cache.mNumber);
            // Aurora xuyong 2014-07-19 modified for sougou end
            if (area == null || "".equals(area)) {
                view.getDataView().setText(cache.mNumber);
            } else {
                view.getDataView().setText(cache.mNumber + "  " + area);
            }

            return view;
        }

        public Object getGroup(int groupPosition) {
            return mGroups.get(groupPosition);
        }

        public int getGroupCount() {
            return mGroups.size();
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {

            View retView = convertView;
            if (convertView == null) {
                retView = LinearLayout.inflate(mContext, R.layout.gn_group_list_group_item, null);
            }

            TextView title = (TextView) retView.findViewById(R.id.groupName);
            CheckBox checkBox = (CheckBox) retView.findViewById(R.id.group_item_check_box);
            checkBox.setTag(groupPosition);

            checkBox.setChecked(false);
            for (ContactsCacheSingleton.ContactListItemCache child : mGroups.get(groupPosition).mChildren) {
                if (child.mIsChecked == true) {
                    checkBox.setChecked(true);
                }
            }

            if (mCheckBoxClickListener != null) {
                checkBox.setOnClickListener(mCheckBoxClickListener);
            }

            title.setText(mGroups.get(groupPosition).mName + 
                    " [" + mGroups.get(groupPosition).mChildren.size() + "]");
            title.setEllipsize(TruncateAt.MIDDLE);
            Log.i(TAG, "getGroupView" + " " + mGroups.get(groupPosition).mName);
            return retView;
        }

        @Override
        public long getGroupId(int arg0) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

        public void groupCheckBoxClicked(int groupPosition, boolean checked) {
            for (int i = 0; i < mGroups.get(groupPosition).mChildren.size(); i++) {
                ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupPosition).mChildren
                        .get(i);

                item.mIsChecked = checked;
            }
        }

        public void childCheckBoxClicked(int groupPosition, int childPosition, View convertView) {
            final ContactListItemView view = (ContactListItemView) convertView;
            ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupPosition).mChildren
                    .get(childPosition);

            view.getCheckBox().setChecked(!view.getCheckBox().isChecked());
            item.mIsChecked = view.getCheckBox().isChecked();

        }

        public int getCheckedCount() {
            int retValue = 0;
            for (int groupIndex = 0; groupIndex < mGroups.size(); groupIndex++) {
                for (int i = 0; i < mGroups.get(groupIndex).mChildren.size(); i++) {
                    ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupIndex).mChildren
                            .get(i);

                    if (item.mIsChecked == true) {
                        retValue++;
                    }
                }
            }

            return retValue;
        }

        int mAllChildCount = 0;

        public int getAllChildCount() {
            return mAllChildCount;
        }

        public void setAllItemCheckState(boolean isCheck) {

            for (int groupIndex = 0; groupIndex < mGroups.size(); groupIndex++) {
                for (int i = 0; i < mGroups.get(groupIndex).mChildren.size(); i++) {
                    ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupIndex).mChildren
                            .get(i);

                    item.mIsChecked = isCheck;
                }
            }
            notifyDataSetChanged();
        }

    }

    private class QueryHandler extends AsyncQueryCallbackHandler {
        protected final WeakReference<AddReceiptorActivity> mActivity;
        Context mContext;
        int mPositon;

        public QueryHandler(Context context, int position) {
            super(context.getContentResolver());
            mActivity = new WeakReference<AddReceiptorActivity>((AddReceiptorActivity) context);
            mContext = context;
            mPositon = position;
        }

        @Override
        public void execQuery() {
            mExpandAdapter.removeAllGroup();
            int allChildSize = 0;
            if (mPositon == mStarPosition) {
                Cursor favoriteContactsCursor = getContentResolver().query(
                        Contacts.CONTENT_URI, CONTACTS_SUMMARY_PROJECTION, Contacts.STARRED + " = 1 ", null, Contacts.DISPLAY_NAME_PRIMARY + " ASC");

                ContactsCacheSingleton.ContactListItemCache favoriteItem = new ContactsCacheSingleton.ContactListItemCache();

                if (favoriteContactsCursor != null) {
                    favoriteContactsCursor.moveToPosition(-1);
                    ArrayList<ContactsCacheSingleton.ContactListItemCache> children = new ArrayList<ContactsCacheSingleton.ContactListItemCache>();
                    while (favoriteContactsCursor.moveToNext()) {
                        long contactId = favoriteContactsCursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                        ContactsCacheSingleton.getInstance().getContactsAndNumber(contactId, children);
                    }

                    mExpandAdapter.addOneGroup(getString(R.string.tab_favorites), children);
                    favoriteContactsCursor.close();
                    allChildSize += children.size();
                }
            } else if (mPositon == mGroupPosition) {
                Cursor contactGroupsCursor = getContentResolver().query(Groups.CONTENT_SUMMARY_URI,
                        DataGroupsQuery.GROUP_PROJECTION, DataGroupsQuery.GROUP_SELECTION, null,
                        DataGroupsQuery.GROUP_SORDORDER);

                if (null != contactGroupsCursor) {
                    contactGroupsCursor.moveToPosition(-1);

                    while (contactGroupsCursor.moveToNext()) {
                        Cursor contactCursor = getGroupContacts(mContext.getContentResolver(),
                                contactGroupsCursor.getInt(DataGroupsQuery.GROUPS_ID));

                        if (contactCursor != null) {
                            contactCursor.moveToPosition(-1);
                            ArrayList<ContactsCacheSingleton.ContactListItemCache> children = new ArrayList<ContactsCacheSingleton.ContactListItemCache>();
                            while (contactCursor.moveToNext()) {
                                long contactId = contactCursor.getLong(contactCursor
                                        .getColumnIndex(Data.CONTACT_ID));
                                ContactsCacheSingleton.getInstance().getContactsAndNumber(contactId,
                                        children);
                            }

                            String groupName = getGroupsName(mContext,
                                    contactGroupsCursor.getString(DataGroupsQuery.GROUPS_TITLE));
                            mExpandAdapter.addOneGroup(groupName, children);

                            contactCursor.close();

                            allChildSize += children.size();
                        }
                    }
                }
                contactGroupsCursor.close();
            }
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Object result) {
            mExpandList.setAdapter(mExpandAdapter);
            mExpandList.setVisibility(View.VISIBLE);

            mTopView.setVisibility(View.GONE);
            mList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private interface DataGroupsQuery {
        public static final String GROUP_SELECTION = Groups.DELETED + "=0";
        public static final String GROUP_SORDORDER = Groups.SYSTEM_ID + " DESC, " + Groups.TITLE;

        public static final String[] GROUP_PROJECTION = new String[] {
                Groups._ID, Groups.TITLE
        };

        public static final int GROUPS_ID = 0;
        public static final int GROUPS_TITLE = 1;
    }

    public static String getGroupsName(Context context, String name) {
        if (name == null) {
            return null;
        }
        Resources res = context.getResources();
        if ("Co-worker".equals(name)) {
            return res.getString(R.string.groups_coworker);
        }

        if ("Family".equals(name)) {
            return res.getString(R.string.groups_family);
        }
        if ("Friends".equals(name)) {
            return res.getString(R.string.groups_friends);
        }
        if ("Schoolmate".equals(name)) {
            return res.getString(R.string.groups_schoolmate);
        }
        if ("VIP".equals(name)) {
            return res.getString(R.string.groups_vip);
        }

        if ("All Contacts".equals(name)) {
            return res.getString(R.string.showAllGroups);
        }
        if ("SIM".equals(name)) {
            return res.getString(R.string.sim);
        }
        return name;
    }

    public static Cursor getGroupContacts(ContentResolver contentresolver, int nGroupId) {
        String strSelection = Data.DATA1 + "=?" + " AND " + Data.MIMETYPE + "='"
                + GroupMembership.CONTENT_ITEM_TYPE + "'";

        return contentresolver.query(Data.CONTENT_URI, DataContactsQuery.CONTACTS_PROJECTION,
                strSelection, new String[] {
                    String.valueOf(nGroupId)
                }, Data.DISPLAY_NAME + " ASC");
    }

    protected void onSearchTextChanged() {
        mAdapter.clear();

        if (TextUtils.isEmpty(getTextFilter())) {
            for (ContactsCacheSingleton.ContactListItemCache cache : mContactsList) {
                mAdapter.add(cache);
            }
        } else {
            Cursor filterCursor = runQueryOnBackgroundThread(getTextFilter());

            if (filterCursor != null && filterCursor.getCount() > 0) {
                filterCursor.moveToPosition(-1);

                while (filterCursor.moveToNext()) {
                    String number = filterCursor.getString(filterCursor
                            .getColumnIndex(Phone.NUMBER)).replace(" ", "");
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
                        }
                    }
                }
            }
            if (filterCursor != null && filterCursor.getCount() == 0) {
                mSelAllBtn.setEnabled(false);
                //gionee gaoj 2012-5-25 added for CR00608302 start
            } else {
                mSelAllBtn.setEnabled(true);
            }
            //gionee gaoj 2012-5-25 added for CR00608302 end
            if (filterCursor != null) {
                filterCursor.close();
            }
        }

        mAdapter.notifyDataSetChanged();
    }

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

        Uri baseUri = Uri.parse("content://com.android.contacts/data/phone_email/filter");
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
        Cursor phoneCursor = getContentResolver().query(uri, PROJECTION_PHONE, selection, // selection,
                null, SORT_ORDER);

        return phoneCursor;
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

    private AdapterView.OnItemClickListener mContactListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick function called");
            mAdapter.checkBoxClicked(position, view);
            updateSelectDoneText(mAdapter);
        }
    };
    
    private AdapterView.OnItemClickListener mRecentListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick function called");
            mRecentAdapter.checkBoxClicked(position, view);
            updateSelectDoneText(mRecentAdapter);
        }
    };
    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void registerProviderStatusObserver() {
        getContentResolver().registerContentObserver(ProviderStatus.CONTENT_URI, false,
                mProviderStatusObserver);
    }

    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void unregisterProviderStatusObserver() {
        getContentResolver().unregisterContentObserver(mProviderStatusObserver);
    }

    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // checkProviderState(true);
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);

        // Save list state in the bundle so we can restore it after the
        // QueryHandler has run
        if (mList != null) {
            icicle.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
        super.onRestoreInstanceState(icicle);

        // Retrieve list state. This will be applied after the QueryHandler has
        // run
        mListState = icicle.getParcelable(LIST_STATE_KEY);
    }

    private View.OnClickListener mSelAllListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ContactsListArrayAdapter adapter = getReceiptorAdapter();
            if (adapter != null) {
                if (adapter.getCheckedCount() < adapter.getCount()) {
                    adapter.setAllItemCheckState(true, adapter);
                } else {
                    adapter.setAllItemCheckState(false, adapter);
                }
                updateSelectDoneText(adapter);
            } else if (mExpandAdapter != null) {
                if (mExpandAdapter.getCheckedCount() < mExpandAdapter.getAllChildCount()) {
                    mExpandAdapter.setAllItemCheckState(true);
                } else {
                    mExpandAdapter.setAllItemCheckState(false);
                }
                updateGroupSelect();
            }
        }
    };

    void updateSelectDoneText(ContactsListArrayAdapter adapter) {
        int checkedCount = ContactsCacheSingleton.getInstance().getCheckedCount();

        mActionBarText.setText(getResources().getString(R.string.gn_select_conversation_more, checkedCount));
        if (mEmptyView.getVisibility() != 0) {
            mSelAllBtn.setEnabled(true);

            /*if(adapter.getCheckedCount() == adapter.getCount()) {
                mSelAllBtn.setImageResource(R.drawable.ic_menu_clear_select);
            } else {
                mSelAllBtn.setImageResource(R.drawable.ic_menu_select_all);
            }*/
        } else {
            mSelAllBtn.setEnabled(false);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void updateGroupSelect() {
        int checkedCount = ContactsCacheSingleton.getInstance().getCheckedCount();

        mActionBarText.setText(getResources().getString(R.string.gn_select_conversation_more, checkedCount));

        if (mEmptyView.getVisibility() != 0 && mExpandAdapter.mAllChildCount != 0) {
            mSelAllBtn.setEnabled(true);

            /*if (mExpandAdapter.getCheckedCount() == mExpandAdapter.getAllChildCount()) {
                mSelAllBtn.setImageResource(R.drawable.ic_menu_clear_select);
            } else {
                mSelAllBtn.setImageResource(R.drawable.ic_menu_select_all);
            }*/
        } else {
            mSelAllBtn.setEnabled(false);
        }
        mExpandAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.gn_reciptor_picker_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case R.id.gn_select_done:
            showDialog(DIALOG_ADD);
            // Get numbers from HashMap
            ArrayList<String> contactInfoList = ContactsCacheSingleton.getInstance()
                    .getCheckedNumbers();

            final Intent intent = new Intent();
            // The following string "ContactNumbers" or "ContactInfo" must
            // match in the received activity
            if (mMode == MODE_ADD_CONTACT) {
                intent.putExtra("ContactNumbers", contactInfoList);
            } else if (mMode == MODE_INSERT_CONTACT) {
                intent.putExtra("ContactInfo", contactInfoList);
            }

            // current activity has no parent,setResult
            if (getParent() == null) {
                setResult(RESULT_OK, intent);
            } else {
                // actually, this activity is the child activity of
                // tabActivity,
                // which start our activity by calling startActivity()
                // so we need to set result to the parent and return.
                getParent().setResult(RESULT_OK, intent);
            }

            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id){
        case DIALOG_ADD: {
            if (dialog != null && dialog.getContext()!= this){
                removeDialog(DIALOG_ADD);
            }
            dialog = new AuroraProgressDialog(this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.refreshing));
            return dialog;
        }
    }
    return null;
    }
}
