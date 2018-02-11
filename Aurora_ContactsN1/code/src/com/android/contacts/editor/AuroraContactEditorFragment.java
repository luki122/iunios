/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts.editor;

import com.android.contacts.ContactLoader;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.ContactsLog;
import com.android.contacts.activities.JoinContactActivity;
import com.android.contacts.editor.AggregationSuggestionEngine.Suggestion;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.model.AuroraAccountTypeManager;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.EntityDeltaList;
import com.android.contacts.model.EntityModifier;
import com.android.contacts.model.GoogleAccountType;
import com.android.contacts.util.AccountsListAdapter;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.IntentFactory;

import android.accounts.Account;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.BaseTypes;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.DisplayPhoto;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.KeyCharacterMap.KeyData;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.android.contacts.model.DataKind;

import android.provider.ContactsContract;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.activities.EditSimContactActivity;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.OperatorUtils;
import com.privacymanage.data.AidlAccountData;

import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.widget.EditText;

//Gionee:QC--MTK two to one

import com.android.contacts.ContactsUtils;

import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import aurora.preference.AuroraPreferenceManager;

import com.google.android.collect.Lists;

import aurora.widget.AuroraMenu;
import aurora.widget.AuroraTextView;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
/*
 * Change Feature by Mediatek End.
 */
public class AuroraContactEditorFragment extends Fragment implements
        SplitContactConfirmationDialogFragment.Listener,
        AggregationSuggestionEngine.Listener, AggregationSuggestionView.Listener,
        RawContactReadOnlyEditorView.Listener {

    private static final String TAG = AuroraContactEditorFragment.class.getSimpleName();

    private static final int LOADER_DATA = 1;
    private static final int LOADER_GROUPS = 2;
    private static final int LOADER_RINGTONE = 3;

    private static final String KEY_URI = "uri";
    private static final String KEY_ACTION = "action";
    private static final String KEY_EDIT_STATE = "state";
    private static final String KEY_RAW_CONTACT_ID_REQUESTING_PHOTO = "photorequester";
    private static final String KEY_VIEW_ID_GENERATOR = "viewidgenerator";
    private static final String KEY_CURRENT_PHOTO_FILE = "currentphotofile";
    private static final String KEY_CONTACT_ID_FOR_JOIN = "contactidforjoin";
    private static final String KEY_CONTACT_WRITABLE_FOR_JOIN = "contactwritableforjoin";
    private static final String KEY_SHOW_JOIN_SUGGESTIONS = "showJoinSuggestions";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NEW_LOCAL_PROFILE = "newLocalProfile";
    private static final String KEY_IS_USER_PROFILE = "isUserProfile";

    public static final String SAVE_MODE_EXTRA_KEY = "saveMode";
    
    //Gionee:wangth 20120423 add for CR00577037 begin
    private final int MAX_NAME_SIZE = 40;
    //Gionee:wangth 20120423 add for CR00577037 end
    
    // Gionee:wangth 20120625 add for CR00627802 begin
    private boolean mHasOldPhone = false;
    // Gionee:wangth 20120625 add for CR00627802 end
    
    private boolean mIsRingChanged = false;
    private String exRingUri = null;
    
    private boolean isRingChange() {
    	return this.mIsRingChanged;
	}

    /**
     * An intent extra that forces the editor to add the edited contact
     * to the default group (e.g. "My Contacts").
     */
    public static final String INTENT_EXTRA_ADD_TO_DEFAULT_DIRECTORY = "addToDefaultDirectory";

    public static final String INTENT_EXTRA_NEW_LOCAL_PROFILE = "newLocalProfile";
    
    private RelativeLayout mSaveToView;
    private AuroraTextView mSaveToTextView;
    private static List<AccountWithDataSet> mEditorAccounts;
    private boolean mContactsListFilterIsNotCustom = false;
    
    public static boolean mIsPrivacyContact = false;

    /**
     * Modes that specify what the AsyncTask has to perform after saving
     */
    // TODO: Move this into a common utils class or the save service because the contact and
    // group editors need to use this interface definition
    public interface SaveMode {
        /**
         * Close the editor after saving
         */
        public static final int CLOSE = 0;

        /**
         * Reload the data so that the user can continue editing
         */
        public static final int RELOAD = 1;

        /**
         * Split the contact after saving
         */
        public static final int SPLIT = 2;

        /**
         * Join another contact after saving
         */
        public static final int JOIN = 3;

        /**
         * Navigate to Contacts Home activity after saving.
         */
        public static final int HOME = 4;
    }

    private interface Status {
        /**
         * The loader is fetching data
         */
        public static final int LOADING = 0;

        /**
         * Not currently busy. We are waiting for the user to enter data
         */
        public static final int EDITING = 1;

        /**
         * The data is currently being saved. This is used to prevent more
         * auto-saves (they shouldn't overlap)
         */
        public static final int SAVING = 2;

        /**
         * Prevents any more saves. This is used if in the following cases:
         * - After Save/Close
         * - After Revert
         * - After the user has accepted an edit suggestion
         */
        public static final int CLOSING = 3;

        /**
         * Prevents saving while running a child activity.
         */
        public static final int SUB_ACTIVITY = 4;
    }

    private static final int REQUEST_CODE_JOIN = 0;
    private static final int REQUEST_CODE_CAMERA_WITH_DATA = 1;
    private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 2;
    private static final int REQUEST_CODE_ACCOUNTS_CHANGED = 3;
    private static final int REQUEST_CODE_PICK_RINGTONE = 13;
    private static final int REQUEST_CODE_PHOTO_CROP = 14;
    
    // gionee xuhz 20120812 add for GIUI4.3 start
    private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA_OR_RESID = 5;
    // gionee xuhz 20120812 add for GIUI4.3 end

    private Bitmap mPhoto = null;
    private long mRawContactIdRequestingPhoto = -1;
    private long mRawContactIdRequestingPhotoAfterLoad = -1;

    private final EntityDeltaComparator mComparator = new EntityDeltaComparator();

    public static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");

    private Cursor mGroupMetaData;
    
    //Gionee:huangzy 20130316 add for CR00774040 start
    private boolean mIsGoingPickingRing = false;
    //Gionee:huangzy 20130316 add for CR00774040 end

    private File mCurrentPhotoFile;

    // Height/width (in pixels) to request for the photo - queried from the provider.
    private int mPhotoPickSize;

    private Context mContext;
    private String mAction;
    private Uri mLookupUri;
    private Bundle mIntentExtras;
    private Listener mListener;

    private long mContactIdForJoin;
    private boolean mContactWritableForJoin;

    private ContactEditorUtils mEditorUtils;

    private LinearLayout mContent;
    private EntityDeltaList mState;

    private ViewIdGenerator mViewIdGenerator;

    private int mStatus;

    private AggregationSuggestionEngine mAggregationSuggestionEngine;
    private long mAggregationSuggestionsRawContactId;
    private View mAggregationSuggestionView;

    private ListPopupWindow mAggregationSuggestionPopup;
    
    // Gionee <xuhz> <2013-07-24> add for CR00839225 begin
    private AuroraAlertDialog mPhotoActionDialog;
    // Gionee <xuhz> <2013-07-24> add for CR00839225 end
    
    // aurora wangth 20140528 add for #5179 begin
    private AccountWithDataSet mCurrentAccount = null;
    // aurora wangth 20140528 add for #5179 end

	//aurora change zhouxiaobing 20140716  start	
    private KindSectionView ksview;
	//aurora change zhouxiaobing 20140716  end
    
    private static final class AggregationSuggestionAdapter extends BaseAdapter {
        private final Activity mActivity;
        private final boolean mSetNewContact;
        private final AggregationSuggestionView.Listener mListener;
        private final List<Suggestion> mSuggestions;

        public AggregationSuggestionAdapter(Activity activity, boolean setNewContact,
                AggregationSuggestionView.Listener listener, List<Suggestion> suggestions) {
            mActivity = activity;
            mSetNewContact = setNewContact;
            mListener = listener;
            mSuggestions = suggestions;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Suggestion suggestion = (Suggestion) getItem(position);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            // gionee xuhz 20120516 modify start
            AggregationSuggestionView suggestionView;
            
                suggestionView = (AggregationSuggestionView) inflater.inflate(
                        R.layout.aggregation_suggestions_item, null);
            

            suggestionView.setNewContact(mSetNewContact);
            suggestionView.setListener(mListener);
            suggestionView.bindSuggestion(suggestion);
            return suggestionView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mSuggestions.get(position);
        }

        @Override
        public int getCount() {
            return mSuggestions.size();
        }
    }

    private OnItemClickListener mAggregationSuggestionItemClickListener =
            new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AggregationSuggestionView suggestionView = (AggregationSuggestionView) view;
            suggestionView.handleItemClickEvent();
            mAggregationSuggestionPopup.dismiss();
            mAggregationSuggestionPopup = null;
        }
    };

    private boolean mAutoAddToDefaultGroup;

    private boolean mEnabled = true;
    private boolean mRequestFocus;
    private boolean mNewLocalProfile = false;
    private boolean mIsUserProfile = false;

    public AuroraContactEditorFragment() {
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            if (mContent != null) {
                int count = mContent.getChildCount();
                for (int i = 0; i < count; i++) {
                    mContent.getChildAt(i).setEnabled(enabled);
                }
            }
            setAggregationSuggestionViewEnabled(enabled);
            final Activity activity = getActivity();
            if (activity != null) activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        querySetting();
        mEditorUtils = ContactEditorUtils.getInstance(mContext);
        loadPhotoPickSize();
        mEditorAccounts = getAccounts();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAggregationSuggestionEngine != null) {
            mAggregationSuggestionEngine.quit();
        }

        // If anything was left unsaved, save it now but keep the editor open.
        if (!getActivity().isChangingConfigurations() && mStatus == Status.EDITING) {
            // The following lines are provided and maintained by Mediatek inc.
            Log.i(TAG, "[onStop] and the mIsSaveToSim is " + mIsSaveToSim+" | isSimType() : "+isSimType());
            if (mIsSaveToSim||isSimType()) {
                mIsSaveToSim = false;
                    return;
            }
            // The following lines are provided and maintained by Mediatek inc.
         
                if (mState != null) {
                    String accountType = mState.get(0).getValues().getAsString(RawContacts.ACCOUNT_TYPE);
                    String accountName = mState.get(0).getValues().getAsString(RawContacts.ACCOUNT_NAME);
                    if (EntityModifier.hasChanges(mState, AccountTypeManager.getInstance(mContext))
                        && !isContactEditorWithName() 
                        && !isContactEditorWithNumber()
                        && !mIsGoingPickingRing) {
//                        Toast.makeText(mContext, R.string.gn_contactSavedNoNameError_Toast, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            
            // Gionee zhangxx 2012-05-25 add for CR00597254 end
            

            if (!mIsGoingPickingRing && 
            		!(ContactsUtils.isTopActivity(getActivity().getClass().getName()))) {
            	
                // Gionee <xuhz> <2013-07-24> add for CR00839225 begin
        		if (mPhotoActionDialog != null && mPhotoActionDialog.isShowing()) {
        			mPhotoActionDialog.dismiss();
        		}
                // Gionee <xuhz> <2013-07-24> add for CR00839225 end
            	
//            	save(SaveMode.RELOAD); // remove by wangth 20140807
            }
            //Gionee <huangzy> <2013-05-13> modify for CR00797633 end
            
            //Gionee:huangzy 20130316 modify for CR00774040 end
            
            getLoaderManager().destroyLoader(LOADER_RINGTONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        final View view = inflater.inflate(R.layout.contact_editor_fragment, container, false);

        mContent = (LinearLayout) view.findViewById(R.id.editors);

//        setHasOptionsMenu(true);

        // If we are in an orientation change, we already have mState (it was loaded by onCreate)
        if (mState != null) {
            Log.i(TAG,"[onCreateView] mState : "+mState);
            Log.i(TAG,"[onCreateView] mSaveModeForSim : "+mSaveModeForSim+" | mSlotId : "+mSlotId+" | mSimId : "+mSimId);
            bindEditors();
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Handle initial actions only when existing state missing
        final boolean hasIncomingState = savedInstanceState != null;

        if (!hasIncomingState) {
            if (Intent.ACTION_EDIT.equals(mAction)) {
                getLoaderManager().initLoader(LOADER_DATA, null, mDataLoaderListener);
            } else if (Intent.ACTION_INSERT.equals(mAction)) {
                Account account = mIntentExtras == null ? null :
                        (Account) mIntentExtras.getParcelable(Intents.Insert.ACCOUNT);
                String dataSet = mIntentExtras == null ? null :
                        mIntentExtras.getString(Intents.Insert.DATA_SET);
                
                if (account != null) {
                    // Account specified in Intent
                    createContact(new AccountWithDataSet(account.name, account.type, dataSet));
                } else {
                    // No Account specified. Let the user choose
                    // Load Accounts async so that we can present them
                    selectAccountAndCreateContact();
                }
            } else if (ContactEditorActivity.ACTION_SAVE_COMPLETED.equals(mAction)) {
                // do nothing
            } else throw new IllegalArgumentException("Unknown Action String " + mAction +
                    ". Only support " + Intent.ACTION_EDIT + " or " + Intent.ACTION_INSERT);
        }
    }

    @Override
    public void onStart() {
        getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupLoaderListener);
        
        super.onStart();
    }

    public void load(String action, Uri lookupUri, Bundle intentExtras) {
        mAction = action;
        mLookupUri = lookupUri;
        mIntentExtras = intentExtras;
        mAutoAddToDefaultGroup = mIntentExtras != null
                && mIntentExtras.containsKey(INTENT_EXTRA_ADD_TO_DEFAULT_DIRECTORY);
        mNewLocalProfile = mIntentExtras != null
                && mIntentExtras.getBoolean(INTENT_EXTRA_NEW_LOCAL_PROFILE);
    }

    public void setListener(Listener value) {
        mListener = value;
    }

    @Override
    public void onCreate(Bundle savedState) {
        if (savedState != null) {
            // Restore mUri before calling super.onCreate so that onInitializeLoaders
            // would already have a uri and an action to work with
            mLookupUri = savedState.getParcelable(KEY_URI);
            mAction = savedState.getString(KEY_ACTION);
        }

        super.onCreate(savedState);

        if (savedState == null) {
            // If savedState is non-null, onRestoreInstanceState() will restore the generator.
            mViewIdGenerator = new ViewIdGenerator();
        } else {
            // Read state from savedState. No loading involved here
            mState = savedState.<EntityDeltaList> getParcelable(KEY_EDIT_STATE);
            mRawContactIdRequestingPhoto = savedState.getLong(
                    KEY_RAW_CONTACT_ID_REQUESTING_PHOTO);
            mViewIdGenerator = savedState.getParcelable(KEY_VIEW_ID_GENERATOR);
            String fileName = savedState.getString(KEY_CURRENT_PHOTO_FILE);
            if (fileName != null) {
                mCurrentPhotoFile = new File(fileName);
            }
            mContactIdForJoin = savedState.getLong(KEY_CONTACT_ID_FOR_JOIN);
            mContactWritableForJoin = savedState.getBoolean(KEY_CONTACT_WRITABLE_FOR_JOIN);
            mAggregationSuggestionsRawContactId = savedState.getLong(KEY_SHOW_JOIN_SUGGESTIONS);
            mEnabled = savedState.getBoolean(KEY_ENABLED);
            mStatus = savedState.getInt(KEY_STATUS);
            mNewLocalProfile = savedState.getBoolean(KEY_NEW_LOCAL_PROFILE);
            mIsUserProfile = savedState.getBoolean(KEY_IS_USER_PROFILE);
        }
        
        if (getActivity().getIntent() != null) {
            mSimId = getActivity().getIntent().getLongExtra("mSimId", -1);
            
            if (ContactsApplication.sIsAuroraPrivacySupport) {
            	mIsPrivacyContact = getActivity().getIntent().getBooleanExtra("is_privacy_contact", false);
            }
        }
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    public void setData(ContactLoader.Result data) {
        // If we have already loaded data, we do not want to change it here to not confuse the user
        if (mState != null) {
            Log.v(TAG, "Ignoring background change. This will have to be rebased later");
            return;
        }

        // See if this edit operation needs to be redirected to a custom editor
        ArrayList<Entity> entities = data.getEntities();
        if (entities.size() == 1) {
            Entity entity = entities.get(0);
            ContentValues entityValues = entity.getEntityValues();
            String type = entityValues.getAsString(RawContacts.ACCOUNT_TYPE);
            String dataSet = entityValues.getAsString(RawContacts.DATA_SET);
            AccountType accountType = AccountTypeManager.getInstance(mContext).getAccountType(
                    type, dataSet);
            if (accountType.getEditContactActivityClassName() != null &&
                    !accountType.areContactsWritable()) {
                if (mListener != null) {
                    String name = entityValues.getAsString(RawContacts.ACCOUNT_NAME);
                    long rawContactId = entityValues.getAsLong(RawContacts.Entity._ID);
                    mListener.onCustomEditContactActivityRequested(
                            new AccountWithDataSet(name, type, dataSet),
                            ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                            mIntentExtras, true);
                }
                return;
            }
        }

        bindEditorsForExistingContact(data);
    }

    @Override
    public void onExternalEditorRequest(AccountWithDataSet account, Uri uri) {
        mListener.onCustomEditContactActivityRequested(account, uri, null, false);
    }

    private void bindEditorsForExistingContact(ContactLoader.Result data) {
        setEnabled(true);
        Log.i(TAG, "call bindEditorsforExistingContact");

        /*
         * New Feature by Mediatek Begin. Original Android's code: CR
         * ID:ALPS00101852 Descriptions: insert data to SIM/USIM.
         */
        mSaveModeForSim = 2;
		mOldState = EntityDeltaList.fromIterator(data.getEntities().iterator());
        Log.i(TAG,"the mOldState = "+mOldState);
        
        
        mIndicatePhoneOrSimContact = data.getIndicate();
        mSlotId = data.getSlot();
        Log.i(TAG,"[bindEditorsForExistingContact]the indicate is = "+mIndicatePhoneOrSimContact);
        Log.i(TAG,"[bindEditorsForExistingContact]the mSlotId is = "+mSlotId);
        mState = EntityDeltaList.fromIterator(data.getEntities().iterator());
        

        exRingUri = data.getCustomRingtone();
        Log.e(TAG,"exRingUri:"+exRingUri);
        /*
         * Change Feature by Mediatek End.
         */
        setIntentExtras(mIntentExtras);
        mIntentExtras = null;

        // For user profile, change the contacts query URI
        //No User Profile
        mIsUserProfile = false;
        EntityDelta insert2=mState.get(0);
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        final String accountType = insert2.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
        final String dataSet = insert2.getValues().getAsString(RawContacts.DATA_SET);
        final AccountType newAccountType = accountTypes.getAccountType(accountType, dataSet);
        boolean localProfileExists = false;
        EntityModifier.ensureKindExists(insert2, newAccountType, Phone.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert2, newAccountType, Note.CONTENT_ITEM_TYPE);

        //aurora add zhouxiaobing 20140331 start
        EntityModifier.ensureKindExists(insert2, newAccountType, Email.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert2, newAccountType, StructuredPostal.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert2, newAccountType, Event.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert2, newAccountType, Organization.CONTENT_ITEM_TYPE);
      //aurora add zhouxiaobing 20140331 end
        if (mIsUserProfile) {
            for (EntityDelta state : mState) {
                // For profile contacts, we need a different query URI
                state.setProfileQueryUri();
                // Try to find a local profile contact
                if (state.getValues().getAsString(RawContacts.ACCOUNT_TYPE) == null) {
                    localProfileExists = true;
                }
            }
            // Editor should always present a local profile for editing
            if (!localProfileExists) {
                final ContentValues values = new ContentValues();
                values.putNull(RawContacts.ACCOUNT_NAME);
                values.putNull(RawContacts.ACCOUNT_TYPE);
                values.putNull(RawContacts.DATA_SET);
                EntityDelta insert = new EntityDelta(ValuesDelta.fromAfter(values));
                insert.setProfileQueryUri();
                mState.add(insert);
            }
        }
        mRequestFocus = true;

        bindEditors();
    }

    /**
     * Merges extras from the intent.
     */
    public void setIntentExtras(Bundle extras) {
        if (extras == null || extras.size() == 0) {
            return;
        }
        
        log("extras : " + extras.toString());

        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        for (EntityDelta state : mState) {
            final String accountType = state.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
            final String dataSet = state.getValues().getAsString(RawContacts.DATA_SET);
            final AccountType type = accountTypes.getAccountType(accountType, dataSet);
            if (type.areContactsWritable()) {
                // Apply extras to the first writable raw contact only
                EntityModifier.parseExtras(mContext, type, state, extras);
                /*
                 * Bug Fix by Mediatek Begin.
                 *   Original Android's code:
                 *     xxx
                 *   CR ID: ALPS00230431
                 *   Descriptions: 
                 */
                if(EntityModifier.mIsSimType){
                    EntityModifier.mIsSimType = false;
                    Toast.makeText(getActivity(), R.string.add_SIP_to_sim,
                            Toast.LENGTH_LONG).show();
                    getActivity().finish();
                } else if (EntityModifier.mHasSip){
                    EntityModifier.mHasSip = false;
                    Toast.makeText(getActivity(), R.string.already_has_SIP,
                            Toast.LENGTH_LONG).show();
                }
                /*
                 * Bug Fix by Mediatek End.
                 */
                break;
            }
        }
    }

    private void selectAccountAndCreateContact() {
        // If this is a local profile, then skip the logic about showing the accounts changed
        // activity and create a phone-local contact.
        
        List<AccountWithDataSet> accounts = mEditorAccounts;
        final AccountWithDataSet defaultAccount = accounts.get(0);
        if (defaultAccount == null) {
            createContact(null);
        } else {
            createContact(defaultAccount);
        }
    }

    /**
     * Create a contact by automatically selecting the first account. If there's no available
     * account, a device-local contact should be created.
     */
    private void createContact() {
        final List<AccountWithDataSet> accounts =
                AccountTypeManager.getInstance(mContext).getAccounts(true);
        // No Accounts available. Create a phone-local contact.
        if (accounts.isEmpty()) {
            createContact(null);
            return;
        }

        // We have an account switcher in "create-account" screen, so don't need to ask a user to
        // select an account here.
        createContact(accounts.get(0));
    }

    /**
     * Shows account creation screen associated with a given account.
     *
     * @param account may be null to signal a device-local contact should be created.
     */
    private void createContact(AccountWithDataSet account) {
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        final AccountType accountType =
                accountTypes.getAccountType(account != null ? account.type : null,
                        account != null ? account.dataSet : null);

        if (accountType.getCreateContactActivityClassName() != null) {
            if (mListener != null) {
                mListener.onCustomCreateContactActivityRequested(account, mIntentExtras);
            }
        } else {
            bindEditorsForNewContact(account, accountType);
        }
    }

    /**
     * Removes a current editor ({@link #mState}) and rebinds new editor for a new account.
     * Some of old data are reused with new restriction enforced by the new account.
     *
     * @param oldState Old data being edited.
     * @param oldAccount Old account associated with oldState.
     * @param newAccount New account to be used.
     */
    private void rebindEditorsForNewContact(
            EntityDelta oldState, AccountWithDataSet oldAccount, AccountWithDataSet newAccount) {
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        AccountType oldAccountType = accountTypes.getAccountType(
                oldAccount.type, oldAccount.dataSet);
        AccountType newAccountType = accountTypes.getAccountType(
                newAccount.type, newAccount.dataSet);

        if (newAccountType.getCreateContactActivityClassName() != null) {
            Log.w(TAG, "external activity called in rebind situation");
            if (mListener != null) {
                mListener.onCustomCreateContactActivityRequested(newAccount, mIntentExtras);
            }
        } else {
            mState = null;
            bindEditorsForNewContact(newAccount, newAccountType, oldState, oldAccountType);
        }
    }

    private void bindEditorsForNewContact(AccountWithDataSet account,
            final AccountType accountType) {
        bindEditorsForNewContact(account, accountType, null, null);
    }

    private void bindEditorsForNewContact(AccountWithDataSet newAccount,
            final AccountType newAccountType, EntityDelta oldState, AccountType oldAccountType) {
        mStatus = Status.EDITING;
        Log.i(TAG, "call bindEditorsForNewContact  newAccount = " + newAccount
                + "=== newAccountType = " + newAccountType + "===oldState = " + oldState
                + "== oldAccountType = " + oldAccountType);
   
            String email = null;
            if (mIntentExtras != null) {
                email = mIntentExtras.getString("email");
            }
            
            if (mSlotId >= 0 && mSimId > 0 && !mIsUsimMode() && email != null) {
                Toast.makeText(getActivity(), R.string.gn_email_2g_invalid, Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        
      
        /*
         * New Feature by Mediatek Begin. Original Android's code: CR
         * ID:ALPS00101852 Descriptions: insert data to SIM/USIM.
         */
        mSaveModeForSim = 1;

        /*
         * Change Feature by Mediatek End. 
         */

        final ContentValues values = new ContentValues();
        if (newAccount != null) {
            values.put(RawContacts.ACCOUNT_NAME, newAccount.name);
            values.put(RawContacts.ACCOUNT_TYPE, newAccount.type);
            values.put(RawContacts.DATA_SET, newAccount.dataSet);
        } else {
            values.putNull(RawContacts.ACCOUNT_NAME);
            values.putNull(RawContacts.ACCOUNT_TYPE);
            values.putNull(RawContacts.DATA_SET);
        }

        EntityDelta insert = new EntityDelta(ValuesDelta.fromAfter(values));
        Log.i("liumxxx","oldState+"+oldState+"insert::mIntentExtras::"+insert+"::"+mIntentExtras);
        if (oldState == null) {
            // Parse any values from incoming intent
            EntityModifier.parseExtras(mContext, newAccountType, insert, mIntentExtras);
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     
             *   CR ID: ALPS00230412
             *   Descriptions: add toast when insert sip to sim/usim and finish activity
             */
            if(EntityModifier.mIsSimType){
                EntityModifier.mIsSimType = false;
                Toast.makeText(getActivity(), R.string.add_SIP_to_sim,
                        Toast.LENGTH_LONG).show();
                getActivity().finish();
            } else if (EntityModifier.mHasSip){
                EntityModifier.mHasSip = false;
                Toast.makeText(getActivity(), R.string.already_has_SIP,
                        Toast.LENGTH_LONG).show();
            }
            /*
             * Bug Fix by Mediatek End.
             */
        } else {
            EntityModifier.migrateStateForNewContact(mContext, oldState, insert,
                    oldAccountType, newAccountType);
        }

        // Ensure we have some default fields (if the account type does not support a field,
        // ensureKind will not add it, so it is safe to add e.g. Event)
        EntityModifier.ensureKindExists(insert, newAccountType, Phone.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert, newAccountType, Note.CONTENT_ITEM_TYPE);

        //aurora add zhouxiaobing 20140331 start
        EntityModifier.ensureKindExists(insert, newAccountType, Email.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert, newAccountType, StructuredPostal.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert, newAccountType, Event.CONTENT_ITEM_TYPE);
        EntityModifier.ensureKindExists(insert, newAccountType, Organization.CONTENT_ITEM_TYPE);
      //aurora add zhouxiaobing 20140331 end
        // Set the correct URI for saving the contact as a profile
        if (mNewLocalProfile) {
            insert.setProfileQueryUri();
        }

        if (mState == null) {
            // Create state if none exists yet
            mState = EntityDeltaList.fromSingle(insert);
        } else {
            // Add contact onto end of existing state
            mState.add(insert);
        }

        mRequestFocus = true;

        bindEditors();
        // Gionee:wangth 20120625 add for CR00627802 begin
        mHasOldPhone = isContactEditorWithNumber();
        // Gionee:wangth 20120625 add for CR00627802 end
    }

    private void bindEditors() {
        // Sort the editors
        Collections.sort(mState, mComparator);

        // Remove any existing editors and rebuild any visible
        mContent.removeAllViews();

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        int numRawContacts = mState.size();
        // Gionee:wangth 20130323 add for CR00772304 begin
        if (GNContactsUtils.isOnlyQcContactsSupport() && mLookupUri != null) {
            mSlotId = GNContactsUtils.querySlotIdByLookupUri(mContext, mLookupUri);
        }
        // Gionee:wangth 20130323 add for CR00772304 end
        for (int i = 0; i < numRawContacts; i++) {
            // TODO ensure proper ordering of entities in the list
        	
            final EntityDelta entity = mState.get(i);
            final ValuesDelta values = entity.getValues();
            if (!values.isVisible()) continue;
            final String accountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
            final String dataSet = values.getAsString(RawContacts.DATA_SET);
            final AccountType type = accountTypes.getAccountType(accountType, dataSet);
            final long rawContactId = values.getAsLong(RawContacts._ID);
            
            EntityModifier.ensureKindExists(entity, type, Phone.CONTENT_ITEM_TYPE);

            final BaseRawContactEditorView editor;
            if (!type.areContactsWritable()) {
                editor = (BaseRawContactEditorView) inflater.inflate(
                        R.layout.raw_contact_readonly_editor_view, mContent, false);
                ((RawContactReadOnlyEditorView) editor).setListener(this);
            } else if (!TextUtils.isEmpty(accountType)) {
                Log.i(TAG, "call R.layout.raw_contact_editor_view  accountType = " + accountType);
                
                Log.i(TAG, "external account");
                //Aurora-start
                if (accountType.equals(AccountType.ACCOUNT_TYPE_SIM)
                        || accountType.equals(AccountType.ACCOUNT_TYPE_USIM)) {
                	editor = (RawContactEditorView) inflater.inflate(
                            R.layout.aurora_raw_sim_contact_editor_view, mContent, false);
                } else {
                	editor = (RawContactEditorView) inflater.inflate(
                            R.layout.aurora_raw_contact_editor_view, mContent, false);	
                }
 
            } else {
                editor = (RawContactEditorView) inflater.inflate(
                        R.layout.aurora_raw_contact_editor_view, mContent, false);
            }
            
            disableAccountSwitcher(editor);
            
            Log.i(TAG, "[bindEditor] mState = " + mState);
            /*
             * Change Feature by Mediatek End.
             */
            editor.setEnabled(mEnabled);

            mContent.addView(editor);

            editor.setState(entity, type, mViewIdGenerator, isEditingUserProfile());

            if(!TextUtils.isEmpty(accountType)){
                if (!accountType.equals(mUsimAccountType) && !accountType.equals(mSimAccountType)) {
                    editor.getPhotoEditor().setEditorListener(
                            new PhotoEditorListener(editor, type.areContactsWritable()));
                }
            } else {
                editor.getPhotoEditor().setEditorListener(
                        new PhotoEditorListener(editor, type.areContactsWritable()));
            }
            /*
             * Change Feature by Mediatek End.
             */
            if (editor instanceof RawContactEditorView) {
                final RawContactEditorView rawContactEditor = (RawContactEditorView) editor;
                EditorListener listener = new EditorListener() {

                    @Override
                    public void onRequest(int request) {
                        if (request == EditorListener.FIELD_CHANGED && !isEditingUserProfile()) {
                            acquireAggregationSuggestions(rawContactEditor);
                        }
					//aurora change zhouxiaobing 20140716  start	
						if(request==EditorListener.FIELD_TURNED_NON_EMPTY)
						  ((ContactEditorActivity)getActivity()).getAuroraActionBar().getOkButton().setEnabled(true);
						else if(request==EditorListener.FIELD_TURNED_EMPTY)
						{
                           if(ksview.isEmpty())
						   	   ((ContactEditorActivity)getActivity()).getAuroraActionBar().getOkButton().setEnabled(false);
						}
					//aurora change zhouxiaobing 20140716  end		
                    }

                    @Override
                    public void onDeleteRequested(Editor removedEditor) {
                    }
                };
             
                final TextFieldsEditorView nameEditor = rawContactEditor.getNameEditor();
                if (mRequestFocus) {
                    nameEditor.requestFocus();
                    mRequestFocus = false;
                }
                nameEditor.setEditorListener(listener);

              //aurora change zhouxiaobing 20140716  start	
				ViewGroup mFields2 = (ViewGroup)rawContactEditor.findViewById(R.id.sect_fields);
				for (int ij = 0; ij < mFields2.getChildCount(); ij++) {
						View child = mFields2.getChildAt(ij);
						if (child instanceof KindSectionView)
						{
						   
						   ksview=(KindSectionView)child;
						   DataKind kind = ksview.getKind();
						   if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(kind.mimeType))
						   	{
						      break;
						   	}
						   else
						   	{
                              ksview=null;
						    }
						}
					}
				Log.v(TAG,"bindEditors ksview="+ksview);
				ksview.setOnEditListernerToActivity(new KindSectionView.EditListernerToActivity(){
                     @Override
                     public void onRequest(int request)
                     {
                          Log.v(TAG,"onRequest request="+request);
                          if(request==EditorListener.FIELD_TURNED_NON_EMPTY)
                          {
                             ((ContactEditorActivity)getActivity()).getAuroraActionBar().getOkButton().setEnabled(true);
						  }
						  else if(request==EditorListener.FIELD_TURNED_EMPTY)
						  {

                             if(nameEditor.isEmpty())
							 	((ContactEditorActivity)getActivity()).getAuroraActionBar().getOkButton().setEnabled(false);
						  }
					 }
				});
			  //aurora change zhouxiaobing 20140716	end 

                final TextFieldsEditorView phoneticNameEditor =
                        rawContactEditor.getPhoneticNameEditor();
                if (null != phoneticNameEditor) {
                	phoneticNameEditor.setEditorListener(listener);	
                }
                
                rawContactEditor.setAutoAddToDefaultGroup(mAutoAddToDefaultGroup);

                if (rawContactId == mAggregationSuggestionsRawContactId) {
                    acquireAggregationSuggestions(rawContactEditor);
                }
                
                if (rawContactEditor.getOrganizationEditor() != null) {
                	rawContactEditor.getOrganizationEditor().setEditorListener(new OrganizationEditorListener(rawContactEditor));
                }


                    // Gionee jialf 20130408 modified for CR00793565 start
                	GnRingtoneEditorView rev = rawContactEditor.getGnRingtoneEditorView();
                	if (null != rev) {
                	    if (isEditingUserProfile()) {
                            rev.setVisibility(View.GONE);
                        } else {
                            rev.setVisibility(View.VISIBLE);
                    		rev.setState(entity);
                    		rev.setOnPickRingtoneListener(new GnRingtoneEditorView.onPickRingtoneListener() {
    							@Override
    							public void onPickClick(TextView view, EntityDelta state, String ringtoneUri) {
    									//Gionee:huangzy 20130316 add for CR00774040 start
    									mIsGoingPickingRing = true;
    								    //Gionee:huangzy 20130316 add for CR00774040 end
    									
    									try {
    									    startActivityForResult(
                                                    IntentFactory.newPickRingtoneIntent(getActivity(), ringtoneUri),
                                                    REQUEST_CODE_PICK_RINGTONE);
    									} catch (ActivityNotFoundException a) {
    									    a.printStackTrace();
    									}
    								}
                            });
                        }
                    }
                	// Gionee jialf 20130408 modified for CR00793565 end
                
                
            }
            
            mSaveToView = (RelativeLayout) editor.findViewById(R.id.aurora_save_to);
            mSaveToTextView = (AuroraTextView) editor.findViewById(R.id.aurora_save_to_name);
            Log.e(TAG, "mContactsListFilterIsNotCustom  = " + mContactsListFilterIsNotCustom);
            if (mContactsListFilterIsNotCustom) {
                String title = getString(R.string.aurora_save_to_local);
                if (mSaveToTextView != null) {
                    mSaveToTextView.setText(title);
                    
                    mSaveToView.setClickable(false);
                    mSaveToView.setEnabled(false);
                    ImageView saveToImg = (ImageView) editor.findViewById(R.id.aurora_save_to_img);
                    saveToImg.setBackgroundResource(R.drawable.aurora_entrance_group_img_enable);
                }
            } else {
                AccountWithDataSet account = mEditorAccounts.get(0);
                // aurora wangth 20140528 add for #5179 begin
                if (mCurrentAccount != null) {
                    account = mCurrentAccount;
                }
                // aurora wangth 20140528 add for #5179 end
                
                String title = null;
                if (account.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                    title = getString(R.string.aurora_save_to_local);
                } else if (account.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                        || account.type.equals(AccountType.ACCOUNT_TYPE_USIM)) {
                    title = getString(R.string.aurora_save_to_sim);
                    mSimId = 1;
                    mSlotId = 0;
                    
					if (FeatureOption.MTK_GEMINI_SUPPORT && account.name != null) {
						String lastAN = account.name.substring(
								account.name.length() - 1,
								account.name.length());
						if (lastAN != null && lastAN.equals("0")) {
							mSlotId = 0;
							title = getString(R.string.aurora_slot_0);
						} else if (lastAN != null && lastAN.equals("1")) {
							mSlotId = 1;
							title = getString(R.string.aurora_slot_1);
						} else {
							title = getString(R.string.aurora_save_to_sim);
							mSlotId = -1;
						}

						if (mSlotId >= 0) {
							mSimId = ContactsUtils.getSubIdbySlot(mSlotId);
						}
					}
                } else {
                    title = account.name;
                }
                
                if (null != mSaveToTextView) {
                    mSaveToTextView.setText(title);
                }
                
                if (mEditorAccounts.size() > 1 && null != mSaveToTextView) {
                    mSaveToView.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            
                            InputMethodManager inputMethodManager = (InputMethodManager) mContext
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(
                                    editor.getWindowToken(), 0);
                            showAccountsAndChangeEditor();
                        }
                    });
                } else if (mEditorAccounts.size() == 1 && mSaveToView != null) {
                    mSaveToView.setClickable(false);
                    mSaveToView.setEnabled(false);
                    ImageView saveToImg = (ImageView) editor.findViewById(R.id.aurora_save_to_img);
                    saveToImg.setBackgroundResource(R.drawable.aurora_entrance_group_img_enable);
                }
            }
            
            String action = getActivity().getIntent().getAction();
            if (action != null && action.equals(Intent.ACTION_EDIT)) {
            	int slotid =  ContactsUtils.getSlotBySubId((int)mSimId);
                String account_name = values.getAsString(RawContacts.ACCOUNT_NAME);
                String account_type = values.getAsString(RawContacts.ACCOUNT_TYPE);
                if (TextUtils.isEmpty(account_name)) {
                    values.put(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
                }
                
                if (TextUtils.isEmpty(account_type)) {
                    values.put(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
                }
                
                final AccountWithDataSet currentAccount = new AccountWithDataSet(
                        values.getAsString(RawContacts.ACCOUNT_NAME),
                        values.getAsString(RawContacts.ACCOUNT_TYPE),
                        values.getAsString(RawContacts.DATA_SET));
                String title = null;
                if (currentAccount.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                                || currentAccount.type.equals(AccountType.ACCOUNT_TYPE_USIM)) {
                   if(slotid==0)//aurora add zhouxiaobing 20140514 for dual sim
                   {
                	   title=getString(R.string.aurora_slot_0);
                   }
                   else if(slotid==1)
                   {
                	   title=getString(R.string.aurora_slot_1);
                   }
                   else
                   {
                    title = getString(R.string.aurora_save_to_sim);     
                   }
                } else if (currentAccount.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                    title = getString(R.string.aurora_save_to_local);
                } else {
                    title = currentAccount.name;
                }
                
                if (mSaveToTextView != null) {
                	ContactsLog.log("edit text title="+title);
                    mSaveToTextView.setText(title);
                    mSaveToView.setClickable(false);
                    mSaveToView.setEnabled(false);
                    ImageView saveToImg = (ImageView) editor.findViewById(R.id.aurora_save_to_img);
                    saveToImg.setBackgroundResource(R.drawable.aurora_entrance_group_img_enable);
                }
            }
        }

        mRequestFocus = false;

        bindGroupMetaData();

        // Show editor now that we've loaded state
        mContent.setVisibility(View.VISIBLE);
    }
    //aurora add zhouxiaobing 20140228 start
    AuroraAlertDialog adialog;
    private int mPreIndex = 0;
    String account_types[];
    //aurora add zhouxiaobing 20140228 end
    private void showAccountsAndChangeEditor() {
        final AuroraAccountTypeManager am = new AuroraAccountTypeManager();
        List<AccountWithDataSet> accounts = am.getAccounts(mContext);
        Log.d(TAG, "accounts.size() = " + accounts.size());
        Log.d(TAG, "showAccountsAndChangeEditor  accounts.size() = " + accounts.size());
        
        SharedPreferences mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        int filterInt = mPrefs.getInt("filter.type", -1);
        int turnOnCount = 0;
        ArrayList<AccountWithDataSet> menuItemsAccount = Lists.newArrayList();
        ArrayList<Boolean> menuItemsAccountTurnOn = Lists.newArrayList();

        for (int i = accounts.size(); i > 0; i--) {
            final AccountWithDataSet account = accounts.get(i - 1);
            
            if (account == null || 
                    (account != null && (!account.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)
                            && !account.type.equals(AccountType.ACCOUNT_TYPE_SIM) 
                            && !account.type.equals(AccountType.ACCOUNT_TYPE_USIM)
                            && !account.type.equals("com.google")))) {
                continue;
            }
            
            String key = account.name + "þ" + account.type + "þ" + account.dataSet;
            boolean accountIsTurnOn = mPrefs.getBoolean(key, false);
            if (account.name.equals("Phone") && account.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                accountIsTurnOn = mPrefs.getBoolean("local_phone_filter_trun_on", false);
            }
            
            if (mSettingsAccount != null && !mSettingsAccount.contains(key)) {
                accountIsTurnOn = false;
            }
            
            Log.d(TAG, "account = " + account + "     accountIsTurnOn = " + accountIsTurnOn);
            
            menuItemsAccount.add(account);
            menuItemsAccountTurnOn.add(accountIsTurnOn);
            if (accountIsTurnOn) {
                turnOnCount++;
            }
        }
        
        if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyContact) {
        	turnOnCount = 1;
        	menuItemsAccount.clear();
        	menuItemsAccount.add(new AccountWithDataSet("Phone", AccountType.ACCOUNT_TYPE_LOCAL_PHONE, null));
        }
        
        Log.d(TAG, "turnOnCount = " + turnOnCount);
        //aurora add zhouxiaobing 20140228 start
        if(turnOnCount==0)
        	account_types=new String[menuItemsAccount.size()];
        else
        	account_types=new String[turnOnCount];
        ValuesDelta values2 = mState.get(0).getValues();
        final AccountWithDataSet currentAccount2 = new AccountWithDataSet(
                values2.getAsString(RawContacts.ACCOUNT_NAME),
                values2.getAsString(RawContacts.ACCOUNT_TYPE),
                values2.getAsString(RawContacts.DATA_SET));
        int hilite_index=0;
        final ArrayList<AccountWithDataSet> dialogAccounts = Lists.newArrayList();
        //aurora add zhouxiaobing 20140228 end
        if (turnOnCount == 0) {
            String menuTitle = null;
            account_types=new String[menuItemsAccount.size()];
            int j = 0;
            for (int i = menuItemsAccount.size() - 1; i >= 0; i--) {
                final AccountWithDataSet account = menuItemsAccount.get(i);
                
                if (account.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                    menuTitle = getString(R.string.aurora_save_to_local);
                } else if (account.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                        || account.type.equals(AccountType.ACCOUNT_TYPE_USIM)) {
                    menuTitle = getString(R.string.aurora_save_to_sim);
                    mSimId = 1;
                    
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {

                        mSimId = ContactsUtils.getSubIdbySlot(mSlotId);
                        
                        if(account instanceof AccountWithDataSetEx){
                            int slotId = ((AccountWithDataSetEx) account).getSlotId();
                            if (slotId == 0) {
                                menuTitle = getString(R.string.aurora_slot_0);
                            } else if (slotId == 1) {
                                menuTitle = getString(R.string.aurora_slot_1);
                            } else {
                                menuTitle = getString(R.string.aurora_save_to_sim);
                            }
                        }
                    }    
                } else {
                    menuTitle = account.name;
                }
                account_types[j]=menuTitle;
                dialogAccounts.add(account);
                if(currentAccount2.name.equals(account.name) && currentAccount2.type.equals(account.type))
                	hilite_index=j;
                
                j++;
/*                final String title = menuTitle;
                ((ContactEditorActivity)getActivity()).addMenu(AuroraMenu.FIRST + i, menuTitle, new OnMenuItemClickLisener() {
                    
                            public void onItemClick(View menu) {
                                ValuesDelta values = mState.get(0).getValues();
                                final AccountWithDataSet currentAccount = new AccountWithDataSet(
                                        values.getAsString(RawContacts.ACCOUNT_NAME),
                                        values.getAsString(RawContacts.ACCOUNT_TYPE),
                                        values.getAsString(RawContacts.DATA_SET));
                                rebindEditorsForNewContact(mState.get(0), currentAccount, account);
                                if (mSaveToTextView != null) {
                                    mSaveToTextView.setText(title);
                                }
                            }
                        });*/
            }
        } else if (turnOnCount == 1) {
            
        } else {
            String menuTitle = null;
            int j = 0;
            for (int i = menuItemsAccountTurnOn.size() - 1; i >= 0; i--) {
                boolean turnOn = menuItemsAccountTurnOn.get(i);
                if (turnOn) {
                    final AccountWithDataSet account = menuItemsAccount.get(i);

                    if (account.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                        menuTitle = getString(R.string.aurora_save_to_local);
                    } else if (account.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                            || account.type.equals(AccountType.ACCOUNT_TYPE_USIM)) {
                        menuTitle = getString(R.string.aurora_save_to_sim);
                        mSimId = 1;
                        
                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
                            mSimId = ContactsUtils.getSubIdbySlot(mSlotId);
                            
                            if(account instanceof AccountWithDataSetEx){
                                int slotId = ((AccountWithDataSetEx) account).getSlotId();
                                if (slotId == 0) {
                                    menuTitle = getString(R.string.aurora_slot_0);
                                } else if (slotId == 1) {
                                    menuTitle = getString(R.string.aurora_slot_1);
                                } else {
                                    menuTitle = getString(R.string.aurora_save_to_sim);
                                }
                            }
                        }
                        
                    } else {
                        menuTitle = account.name;
                    }
                    account_types[j]=menuTitle;
                    dialogAccounts.add(account);
                    if(currentAccount2.name.equals(account.name) && currentAccount2.type.equals(account.type))
                    	hilite_index=j;
/*                    final String title = menuTitle;
                    Log.d(TAG, "account = " + account + "    menuTitle = " + menuTitle);
                    ((ContactEditorActivity)getActivity()).addMenu(AuroraMenu.FIRST + j, menuTitle, new OnMenuItemClickLisener() {
                                public void onItemClick(View menu) {
                                    ValuesDelta values = mState.get(0).getValues();
                                    final AccountWithDataSet currentAccount = new AccountWithDataSet(
                                            values.getAsString(RawContacts.ACCOUNT_NAME),
                                            values.getAsString(RawContacts.ACCOUNT_TYPE),
                                            values.getAsString(RawContacts.DATA_SET));
                                    rebindEditorsForNewContact(mState.get(0), currentAccount, account);
                                    if (mSaveToTextView != null) {
                                        mSaveToTextView.setText(title);
                                    }
                                }});*/
                    j++;
                }
            }
        }
        
 //       ((ContactEditorActivity)getActivity()).showCustomMenu();
        //aurora add zhouxiaobing 20140228 start
		if (turnOnCount != 1) {
			Log.d(TAG, "hilite_index = " + hilite_index);
			if (mPreIndex == hilite_index && null != adialog) {
				adialog.show();
				return;
			}

			mPreIndex = hilite_index;
			adialog = new AuroraAlertDialog.Builder(mContext)
					.setTitle(R.string.aurora_save_to)
					.setSingleChoiceItems(account_types, hilite_index,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									ValuesDelta values = mState.get(0)
											.getValues();
									final AccountWithDataSet currentAccount = new AccountWithDataSet(
											values.getAsString(RawContacts.ACCOUNT_NAME),
											values.getAsString(RawContacts.ACCOUNT_TYPE),
											values.getAsString(RawContacts.DATA_SET));

									// aurora wangth 20140526 add for #5061 begin
									// aurora wangth 20140528 add for #5179 begin
									mCurrentAccount = dialogAccounts.get(which);
									if (mCurrentAccount.type.equals(AccountType.ACCOUNT_TYPE_SIM)
											|| mCurrentAccount.type.equals(AccountType.ACCOUNT_TYPE_USIM)) {
										mIsSaveToSim = true;
									}
									// aurora wangth 20140528 add for #5179 end
									
									if (mIsSaveToSim) {
								//aurora add zhouxiaobing 20140728 for bug 6929	start
									if (FeatureOption.MTK_GEMINI_SUPPORT) {
										if(which>=account_types.length)
										{
                                          Toast.makeText(
														mContext,
														R.string.aurora_sim_not_ready,
														Toast.LENGTH_SHORT).show();
												getActivity().finish();
												return;
										}
							   //aurora add zhouxiaobing 20140728 for bug 6929	end			
										if (account_types[which].equals("SIM1")) {
											mSlotId = 0;
										} else if (account_types[which]
												.equals("SIM2")) {
											mSlotId = 1;
										}

										if (mSlotId > -1) {
											if (!SimCardUtils.isSimStateReady(mSlotId)||GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
												Toast.makeText(
														mContext,
														R.string.aurora_sim_not_ready,
														Toast.LENGTH_SHORT).show();
												getActivity().finish();
												return;
											}
										}
									} else {
										if (!SimCardUtils.isSimStateReady(0)||GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
											Toast.makeText(
													mContext,
													R.string.aurora_sim_not_ready,
													Toast.LENGTH_SHORT).show();
											getActivity().finish();
											return;
										}
									}
									}
									// aurora wangth 20140526 add for #5061 end

									rebindEditorsForNewContact(mState.get(0),
											currentAccount,
											dialogAccounts.get(which));
									if (mSaveToTextView != null) {
										mSaveToTextView.setText(account_types[which]);
									}
									adialog.dismiss();

								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							}).setTitleDividerVisible(true).create();
			adialog.show();
		}
        //aurora add zhouxiaobing 20140228 end
    }
    
    private List<AccountWithDataSet> getAccounts() {
        final AuroraAccountTypeManager am = new AuroraAccountTypeManager();
        List<AccountWithDataSet> accounts = am.getAccounts(mContext);
        List<AccountWithDataSet> resultAccounts = accounts;
        Log.e(TAG, "getAccounts  accounts.size() = " + accounts.size());
        
        if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyContact) {
        	resultAccounts.add(new AccountWithDataSet(AccountType.ACCOUNT_NAME_LOCAL_PHONE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE, null));
        	return resultAccounts;
        }

        SharedPreferences mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        int filterInt = mPrefs.getInt("filter.type", -1);
        if (filterInt == -1) {
            mContactsListFilterIsNotCustom = true;
        }
        int turnOnCount = 0;
        ArrayList<AccountWithDataSet> menuItemsAccount = Lists.newArrayList();
        ArrayList<Boolean> menuItemsAccountTurnOn = Lists.newArrayList();

        for (int i = 0; i < accounts.size(); i++) {
            final AccountWithDataSet account = accounts.get(i);

            if (account == null
                    || (account != null 
                            && (!account.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)
                            && !account.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                            && !account.type.equals(AccountType.ACCOUNT_TYPE_USIM) 
                            && !account.type.equals("com.google")))) {
                continue;
            }

            String key = account.name + "þ" + account.type + "þ"
                    + account.dataSet;
            boolean accountIsTurnOn = mPrefs.getBoolean(key, false);
            if (account.name.equals("Phone") && account.type.equals("Local Phone Account")) {
                accountIsTurnOn = mPrefs.getBoolean("local_phone_filter_trun_on", false);
            }
            
            if (mSettingsAccount != null && !mSettingsAccount.contains(key)) {
                accountIsTurnOn = false;
            }
            Log.d(TAG, "account = " + account + "     accountIsTurnOn = "
                    + accountIsTurnOn);

            menuItemsAccount.add(account);
            menuItemsAccountTurnOn.add(accountIsTurnOn);
            if (accountIsTurnOn) {
                turnOnCount++;
            }
        }
        
        Log.d(TAG, "turnOnCount = " + turnOnCount);
        resultAccounts.clear();
        if (turnOnCount == 0) {
            for (int i = 0; i < menuItemsAccount.size(); i++) {
                final AccountWithDataSet account = menuItemsAccount.get(i);
                resultAccounts.add(account);
            }
        } else if (turnOnCount == 1) {
            for (int i = 0; i < menuItemsAccountTurnOn.size(); i++) {
                boolean turnOn = menuItemsAccountTurnOn.get(i);
                if (turnOn) {
                    final AccountWithDataSet account = menuItemsAccount.get(i);
                    resultAccounts.add(account);
                    break;
                }
            }
        } else {
            for (int i = 0; i < menuItemsAccountTurnOn.size(); i++) {
                boolean turnOn = menuItemsAccountTurnOn.get(i);
                if (turnOn) {
                    final AccountWithDataSet account = menuItemsAccount.get(i);
                    resultAccounts.add(account);
                }
            }
        }
        
        return resultAccounts;
    }
    
    private Set<String> mSettingsAccount = new HashSet<String>();
    private void querySetting() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    Settings.CONTENT_URI,
                    new String[] {Settings.ACCOUNT_NAME, Settings.ACCOUNT_TYPE, Settings.DATA_SET, Settings.UNGROUPED_VISIBLE}, 
                    null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        mSettingsAccount.add(cursor.getString(0) + "þ" + cursor.getString(1) + "þ" + cursor.getString(2));
                    } while (cursor.moveToNext());
                }
                Log.e(TAG, "mSettingsAccount = " + mSettingsAccount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void bindGroupMetaData() {
        if (mGroupMetaData == null) {
            return;
        }

        int editorCount = mContent.getChildCount();
        for (int i = 0; i < editorCount; i++) {
            BaseRawContactEditorView editor = (BaseRawContactEditorView) mContent.getChildAt(i);
            editor.setGroupMetaData(mGroupMetaData);
        }
    }

    private void saveDefaultAccountIfNecessary() {
        // Verify that this is a newly created contact, that the contact is composed of only
        // 1 raw contact, and that the contact is not a user profile.
        if (!Intent.ACTION_INSERT.equals(mAction) && mState.size() == 1 &&
                !isEditingUserProfile()) {
            return;
        }

        // Find the associated account for this contact (retrieve it here because there are
        // multiple paths to creating a contact and this ensures we always have the correct
        // account).
        final EntityDelta entity = mState.get(0);
        final ValuesDelta values = entity.getValues();
        String name = values.getAsString(RawContacts.ACCOUNT_NAME);
        String type = values.getAsString(RawContacts.ACCOUNT_TYPE);
        String dataSet = values.getAsString(RawContacts.DATA_SET);

        AccountWithDataSet account = (name == null || type == null) ? null :
                new AccountWithDataSet(name, type, dataSet);
        mEditorUtils.saveDefaultAndAllAccounts(account);
    }

    private void addAccountSwitcher(
            final EntityDelta currentState, BaseRawContactEditorView editor) {
        ValuesDelta values = currentState.getValues();
        final AccountWithDataSet currentAccount = new AccountWithDataSet(
                values.getAsString(RawContacts.ACCOUNT_NAME),
                values.getAsString(RawContacts.ACCOUNT_TYPE),
                values.getAsString(RawContacts.DATA_SET));
        final View accountView = editor.findViewById(R.id.account);
        final View anchorView = editor.findViewById(R.id.account_container);
        accountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListPopupWindow popup = new ListPopupWindow(mContext, null);
                final AccountsListAdapter adapter =
                        new AccountsListAdapter(mContext,
                        AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, currentAccount);
                popup.setWidth(anchorView.getWidth());
                popup.setAnchorView(anchorView);
                popup.setAdapter(adapter);
                popup.setModal(true);
                popup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
                popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
                        popup.dismiss();
                        AccountWithDataSet newAccount = adapter.getItem(position);
                        //Gionee:huangzy 20120627 remove for CR00627573 start
                        /*newAccount = gnEnsureAccount(newAccount);*/
                        //Gionee:huangzy 20120627 remove for CR00627573 end
                        if (!newAccount.equals(currentAccount)) {
                            rebindEditorsForNewContact(currentState, currentAccount, newAccount);
                        }
                    }
                });
                popup.show();
            }
        });
    }

    private void disableAccountSwitcher(BaseRawContactEditorView editor) {
        // Remove the pressed state from the account header because the user cannot switch accounts
        // on an existing contact
//        final View accountView = editor.findViewById(R.id.account);
//        accountView.setBackgroundDrawable(null);
//        accountView.setEnabled(false);
    }

    private boolean doSplitContactAction() {
        if (!hasValidState()) return false;

        final SplitContactConfirmationDialogFragment dialog =
                new SplitContactConfirmationDialogFragment();
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), SplitContactConfirmationDialogFragment.TAG);
        return true;
    }

    private boolean doJoinContactAction() {
        if (!hasValidState()) {
            return false;
        }

        // If we just started creating a new contact and haven't added any data, it's too
        // early to do a join
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        if (mState.size() == 1 && mState.get(0).isContactInsert()
                && !EntityModifier.hasChanges(mState, accountTypes)) {
            Toast.makeText(getActivity(), R.string.toast_join_with_empty_contact,
                            Toast.LENGTH_LONG).show();
            return true;
        }

        return save(SaveMode.JOIN);
    }

    private void loadPhotoPickSize() {
        Cursor c = mContext.getContentResolver().query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
                new String[]{DisplayPhoto.DISPLAY_MAX_DIM}, null, null, null);
        //Gionee <huangzy><2013-05-16> modify for CR00812739 begin
        /*try {
            c.moveToFirst();
            mPhotoPickSize = c.getInt(0);
        } finally {
            c.close();
        }*/
        if (null != c && c.moveToFirst()) {
//        	try {
            	mPhotoPickSize = c.getInt(0);
            	Log.i("liumxxx","mPhotoPickSize::"+mPhotoPickSize);
//            } finally {
//            	c.close();
//            }	
        }
        if(c != null) {
        	c.close();
        }
        //Gionee <huangzy><2013-05-16> modify for CR00812739 end
    }

    /**
     * Constructs an intent for picking a photo from Gallery, cropping it and returning the bitmap.
     */
    public Intent getPhotoPickIntent() {
        Intent intent = new Intent("com.aurora.filemanager.SINGLE_GET_CONTENT");
        intent.setType("image/*");
        
        return intent;
    }

    /**
     * Check if our internal {@link #mState} is valid, usually checked before
     * performing user actions.
     */
    private boolean hasValidState() {
        return mState != null && mState.size() > 0;
    }

    /**
     * Create a file name for the icon photo using current time.
     */
    public static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    /**
     * Constructs an intent for capturing a photo and storing it in a temporary file.
     */
    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * Sends a newly acquired photo to Gallery for cropping
     */
    protected void doCropPhoto(File f) {
        try {
            // Add the image to the media store
            MediaScannerConnection.scanFile(
                    mContext,
                    new String[] { f.getAbsolutePath() },
                    new String[] { null },
                    null);

            // Launch gallery to crop the photo
            final Intent intent = getCropImageIntent(Uri.fromFile(f));
            mStatus = Status.SUB_ACTIVITY;
            startActivityForResult(intent, REQUEST_CODE_PHOTO_CROP);
        } catch (Exception e) {
            Log.e(TAG, "Cannot crop image", e);
            Toast.makeText(mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Constructs an intent for image cropping.
     */
    public Intent getCropImageIntent(Uri photoUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", mPhotoPickSize);
        intent.putExtra("outputY", mPhotoPickSize);
        // The following lines are provided and maintained by Mediatek inc.
        intent.putExtra(KEY_SCALE_UP_IF_NEEDED, true);
        // The following lines are provided and maintained by Mediatek inc.
        intent.putExtra("return-data", true);
        
        // Gionee:huangzy 20120530 add for CR00608714 start
        if (ContactsApplication.sIsGnZoomClipSupport) {
            intent.setAction("com.android.camera.action.GNCROP");
            intent.putExtra("gn_crop", true);
        }
        // Gionee:huangzy 20120530 add for CR00608714 end
        
        return intent;
    }

    /**
     * Saves or creates the contact based on the mode, and if successful
     * finishes the activity.
     */
    public boolean save(int saveMode) {
        if (!hasValidState() || mStatus != Status.EDITING) {
            Log.i(TAG,"[save] !hasValidState() : "+(!hasValidState())+" | mStatus != Status.EDITING : "+(mStatus != Status.EDITING));
            return false;
        }
        Log.i(TAG,"[save] saveMode : "+saveMode);
        // If we are about to close the editor - there is no need to refresh the data
        if (saveMode == SaveMode.CLOSE || saveMode == SaveMode.SPLIT) {
            getLoaderManager().destroyLoader(LOADER_DATA);
        }

        mStatus = Status.SAVING;

        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        if (!EntityModifier.hasChanges(mState, accountTypes)) {
            onSaveCompleted(false, saveMode, mLookupUri != null, mLookupUri);
            return true;
        }

        setEnabled(false);

        // Store account as default account, only if this is a new contact
        saveDefaultAccountIfNecessary();

        // Save contact
        Intent intent = ContactSaveService.createSaveContactIntent(getActivity(), mState,
                SAVE_MODE_EXTRA_KEY, saveMode, isEditingUserProfile(),
                getActivity().getClass(), ContactEditorActivity.ACTION_SAVE_COMPLETED);
        if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyContact) {
        	intent.putExtra("is_privacy_contact", true);
        }
        getActivity().startService(intent);
        
        return true;
    }

    public static class CancelEditDialogFragment extends DialogFragment {

        public static void show(AuroraContactEditorFragment fragment) {
            CancelEditDialogFragment dialog = new CancelEditDialogFragment();
            dialog.setTargetFragment(fragment, 0);
            dialog.show(fragment.getFragmentManager(), "cancelEditor");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.cancel_confirmation_dialog_title)
                    .setMessage(R.string.cancel_confirmation_dialog_message)
                    .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((AuroraContactEditorFragment)getTargetFragment()).doRevertAction();
                            }
                        }
                    )
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            return dialog;
        }
    }

    public boolean revert() {
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        if (mState == null || (!EntityModifier.hasChanges(mState, accountTypes) && !isRingChange())) {
            doRevertAction();
        } else {
            CancelEditDialogFragment.show(this);
        }
        return true;
    }

    private void doRevertAction() {
        // When this Fragment is closed we don't want it to auto-save
        mStatus = Status.CLOSING;
        if (mListener != null) mListener.onReverted();
    }

    public void doSaveAction() {
        /*
         * New Feature by Mediatek Begin. Original Android's code:
         * save(SaveMode.CLOSE);} CR ID: ALPS00101852 Descriptions: crete
         * sim/usim contact
         */
        boolean storageState = false;//LowStorageHandle.GetCurrentFlag(); //check storage state
        Log.i(TAG, " stroageState = " + storageState);
        if (!storageState) {
            if (mState != null) {
                String accountType = mState.get(0).getValues()
                        .getAsString(RawContacts.ACCOUNT_TYPE);
                

                
                    // gionee xuhz 20120505 modify for CR00585478 start
                    if (!isEditingUserProfile() && Intent.ACTION_INSERT.equals(mAction)) {
                        String accountName = mState.get(0).getValues()
                        .getAsString(RawContacts.ACCOUNT_NAME);
                        //Gionee:huangzy 20120627 remove for CR00627573 start
                        /*String dataset = mState.get(0).getValues().getAsString(KEY_DATASET);
                        gnSaveAccountToPreferences(accountType, accountName, dataset);*/
                        //Gionee:huangzy 20120627 remove for CR00627573 end
                    }
                    // gionee xuhz 20120505 modify for CR00585478 end
                
                
                Log.w(TAG, "the accountType is " + accountType + " the mState is = " + mState);
                if (!TextUtils.isEmpty(accountType)) {
                    if (accountType.equals(mSimAccountType) || accountType.equals(mUsimAccountType)) {
                        saveToSimCard(mState, SaveMode.CLOSE);
                    } else {
                        Log.i(TAG, "[doSaveAction]");
                        
                        if (!isContactEditorWithName()) {
                            Toast.makeText(mContext, R.string.gn_contactSavedNoNameError_Toast, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        //Gionee:wangth 20120423 add for CR00577037 begin
                  
                            // Gionee:wangth 20120618 modify for CR00625559 begin
                            if (contactNameCount() > MAX_NAME_SIZE) {
                                Toast.makeText(mContext, R.string.name_too_long, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Gionee:wangth 20120618 modify for CR00625559 end
                            // Gionee zhangxx 2012-05-23 add for CR00605159 begin
                            else if ((isRingChange() || (EntityModifier.hasChanges(mState, AccountTypeManager.getInstance(mContext))))
                                && !isContactEditorWithName() 
                                && !isContactEditorWithNumber()) {
                                Toast.makeText(mContext, R.string.gn_contactSavedNoNameError_Toast, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Gionee zhangxx 2012-05-23 add for CR00605159 end
                            // Gionee:wangth 20120625 add for CR00627802 begin
                            else if (mHasOldPhone && !isContactEditorWithName() 
                                    && !isContactEditorWithNumber()) {
                                Toast.makeText(mContext, R.string.gn_contactSavedNoNameError_Toast, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Gionee:wangth 20120625 add for CR00627802 end
                        
                        //Gionee:wangth 20120423 add for CR00577037 end
                        save(SaveMode.CLOSE);
                    }
                } else {
                    Log.i(TAG, "[doSaveAction]");
                    save(SaveMode.CLOSE);
                }
            } else {
                Log.i(TAG, "[doSaveAction]");
                save(SaveMode.CLOSE);
            }
        } else {
            /*when storage is full toast it and finish activity*/
            if(mSaveModeForSim ==1){
                Toast.makeText(mContext, R.string.phone_storage_full_create, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                Toast.makeText(mContext, R.string.phone_storage_full_edit, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }
	/*
	 * Change Feature by Mediatek End.
	 */
    public void onJoinCompleted(Uri uri) {
        onSaveCompleted(false, SaveMode.RELOAD, uri != null, uri);
    }

    public void onSaveCompleted(boolean hadChanges, int saveMode, boolean saveSucceeded,
            Uri contactLookupUri) {
        Log.d(TAG, "onSaveCompleted(" + saveMode + ", contactLookupUri : " + contactLookupUri+" | hadChanges : "+hadChanges+" | saveSucceeded : "+saveSucceeded );
        if (hadChanges) {
            if (saveSucceeded) {
                // aurora <wangth> <2013-11-27> remove for aurora begin
//                if (saveMode != SaveMode.JOIN) {
//                    Toast.makeText(mContext, R.string.contactSavedToast, Toast.LENGTH_SHORT).show();
//                }
                // aurora <wangth> <2013-11-27> remove for aurora end
            } else {
                Toast.makeText(mContext, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
            }
        }
        switch (saveMode) {
            case SaveMode.CLOSE:
            case SaveMode.HOME:
                final Intent resultIntent;
                if (saveSucceeded && contactLookupUri != null) {
                    final String requestAuthority =
                            mLookupUri == null ? null : mLookupUri.getAuthority();

                    final String legacyAuthority = "contacts";

                    //Gionee:huangzy 20130401 remove for CR00792013 start
                    /*resultIntent = new Intent();
                    resultIntent.setAction(Intent.ACTION_VIEW);*/
                    //Gionee:huangzy 20130401 remove for CR00792013 end
                    if (legacyAuthority.equals(requestAuthority)) {
                        // Build legacy Uri when requested by caller
                        final long contactId = ContentUris.parseId(Contacts.lookupContact(
                                mContext.getContentResolver(), contactLookupUri));
                        final Uri legacyContentUri = Uri.parse("content://contacts/people");
                        final Uri legacyUri = ContentUris.withAppendedId(
                                legacyContentUri, contactId);
                        //Gionee:huangzy 20130401 modify for CR00792013 start
                        /*resultIntent.setData(legacyUri);*/                        
                        resultIntent = IntentFactory.newViewContactIntent(legacyUri);
                        if (mIsPrivacyContact) {
                        	resultIntent.putExtra("is_privacy_contact", true);
                        }
                        //Gionee:huangzy 20130401 modify for CR00792013 end
                    } else {
                        //Gionee:huangzy 20130401 modify for CR00792013 start
                        // Otherwise pass back a lookup-style Uri
                        /*resultIntent.setData(contactLookupUri);*/
                        resultIntent = IntentFactory.newViewContactIntent(contactLookupUri);
                        if (mIsPrivacyContact) {
                        	resultIntent.putExtra("is_privacy_contact", true);
                        }
                        //Gionee:huangzy 20130401 modify for CR00792013 end
                    }
                    
                                      
                    if (!mIsUserProfile) {
                    	handleRingtonePicked(contactLookupUri);
                    }
                } else {
                    resultIntent = null;
                }
                // It is already saved, so prevent that it is saved again
                mStatus = Status.CLOSING;
                if (mListener != null) mListener.onSaveFinished(resultIntent);
                break;

            case SaveMode.RELOAD:
            case SaveMode.JOIN:
                if (saveSucceeded && contactLookupUri != null) {
                    // If it was a JOIN, we are now ready to bring up the join activity.
                    if (saveMode == SaveMode.JOIN) {
                        showJoinAggregateActivity(contactLookupUri);
                    }

                    // If this was in INSERT, we are changing into an EDIT now.
                    // If it already was an EDIT, we are changing to the new Uri now
                    mState = null;
                    load(Intent.ACTION_EDIT, contactLookupUri, null);
                    mStatus = Status.LOADING;
                    getLoaderManager().restartLoader(LOADER_DATA, null, mDataLoaderListener);
                }
                /*
                 * Change Feature by Mediatek Begin.
                 *   Original Android¡¯s code:
                 *     
                 *   CR ID: ALPS00113564
                 *   Descriptions: ¡­
                 */
                else if (contactLookupUri == null && hadChanges == false && saveSucceeded == false) {
                    Log.i(TAG, "[onSaveCompleted] saveMode is Reload and finish activity noew");
                    mStatus = Status.EDITING;
                    //getActivity().finish();
                }
                /*
                 * Change Feature by Mediatek End.
                 */
                // gionee xuhz 20120518 add for CR00598966 start
                else if (saveSucceeded && contactLookupUri == null) {
                    mStatus = Status.CLOSING;
                    getActivity().finish();
                }
                // gionee xuhz 20120518 add for CR00598966 end
                break;

            case SaveMode.SPLIT:
                mStatus = Status.CLOSING;
                if (mListener != null) {
                    mListener.onContactSplit(contactLookupUri);
                } else {
                    Log.d(TAG, "No listener registered, can not call onSplitFinished");
                }
                break;
        }
    }

    /**
     * Shows a list of aggregates that can be joined into the currently viewed aggregate.
     *
     * @param contactLookupUri the fresh URI for the currently edited contact (after saving it)
     */
    private void showJoinAggregateActivity(Uri contactLookupUri) {
        if (contactLookupUri == null || !isAdded()) {
            return;
        }

        mContactIdForJoin = ContentUris.parseId(contactLookupUri);
        mContactWritableForJoin = isContactWritable();
        final Intent intent = new Intent(JoinContactActivity.JOIN_CONTACT);
        intent.putExtra(JoinContactActivity.EXTRA_TARGET_CONTACT_ID, mContactIdForJoin);
        startActivityForResult(intent, REQUEST_CODE_JOIN);
    }

    /**
     * Performs aggregation with the contact selected by the user from suggestions or A-Z list.
     */
    private void joinAggregate(final long contactId) {
        Intent intent = ContactSaveService.createJoinContactsIntent(mContext, mContactIdForJoin,
                contactId, mContactWritableForJoin,
                ContactEditorActivity.class, ContactEditorActivity.ACTION_JOIN_COMPLETED);
        mContext.startService(intent);
    }

    /**
     * Returns true if there is at least one writable raw contact in the current contact.
     */
    private boolean isContactWritable() {
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        int size = mState.size();
        for (int i = 0; i < size; i++) {
            ValuesDelta values = mState.get(i).getValues();
            final String accountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
            final String dataSet = values.getAsString(RawContacts.DATA_SET);
            final AccountType type = accountTypes.getAccountType(accountType, dataSet);
            if (type.areContactsWritable()) {
                return true;
            }
        }
        return false;
    }

    private boolean isEditingUserProfile() {
        return mNewLocalProfile || mIsUserProfile;
    }

    public static interface Listener {
        /**
         * Contact was not found, so somehow close this fragment. This is raised after a contact
         * is removed via Menu/Delete (unless it was a new contact)
         */
        void onContactNotFound();

        /**
         * Contact was split, so we can close now.
         * @param newLookupUri The lookup uri of the new contact that should be shown to the user.
         * The editor tries best to chose the most natural contact here.
         */
        void onContactSplit(Uri newLookupUri);

        /**
         * User has tapped Revert, close the fragment now.
         */
        void onReverted();

        /**
         * Contact was saved and the Fragment can now be closed safely.
         */
        void onSaveFinished(Intent resultIntent);

        /**
         * User switched to editing a different contact (a suggestion from the
         * aggregation engine).
         */
        void onEditOtherContactRequested(
                Uri contactLookupUri, ArrayList<ContentValues> contentValues);

        /**
         * Contact is being created for an external account that provides its own
         * new contact activity.
         */
        void onCustomCreateContactActivityRequested(AccountWithDataSet account,
                Bundle intentExtras);

        /**
         * The edited raw contact belongs to an external account that provides
         * its own edit activity.
         *
         * @param redirect indicates that the current editor should be closed
         *            before the custom editor is shown.
         */
        void onCustomEditContactActivityRequested(AccountWithDataSet account, Uri rawContactUri,
                Bundle intentExtras, boolean redirect);
    }

    private class EntityDeltaComparator implements Comparator<EntityDelta> {
        /**
         * Compare EntityDeltas for sorting the stack of editors.
         */
        @Override
        public int compare(EntityDelta one, EntityDelta two) {
            // Check direct equality
            if (one.equals(two)) {
                return 0;
            }

            final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
            String accountType1 = one.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
            String dataSet1 = one.getValues().getAsString(RawContacts.DATA_SET);
            final AccountType type1 = accountTypes.getAccountType(accountType1, dataSet1);
            String accountType2 = two.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
            String dataSet2 = two.getValues().getAsString(RawContacts.DATA_SET);
            final AccountType type2 = accountTypes.getAccountType(accountType2, dataSet2);
            
            // The following lines are provided and maintained by Mediatek Inc.
            Log.i(TAG,"[compare] type1 : "+type1);
            Log.i(TAG,"[compare] type2 : "+type2);
            
            
            if (type2 == null && type1 == null) {
                Log.i(TAG,"type2 and type1 also null");
                return 1;
            } else if (type2 == null || type1 == null){
                Log.i(TAG,"type2 or type1 is null");
                return -1;
            }
            // The previous lines are provided and maintained by Mediatek Inc.
            
            // Check read-only
            if (!type1.areContactsWritable() && type2.areContactsWritable()) {
                return 1;
            } else if (type1.areContactsWritable() && !type2.areContactsWritable()) {
                return -1;
            }

            // Check account type
            boolean skipAccountTypeCheck = false;
            boolean isGoogleAccount1 = type1 instanceof GoogleAccountType;
            boolean isGoogleAccount2 = type2 instanceof GoogleAccountType;
            if (isGoogleAccount1 && !isGoogleAccount2) {
                return -1;
            } else if (!isGoogleAccount1 && isGoogleAccount2) {
                return 1;
            } else if (isGoogleAccount1 && isGoogleAccount2){
                skipAccountTypeCheck = true;
            }

            int value;
            if (!skipAccountTypeCheck) {
                
                /*
                 * Bug Fix by Mediatek Begin.
                 *   Original Android's code:
                 *     if (type1.accountType == null ) {
                    Log.i(TAG,"type.accountType is null");
                    return 1;
                }
                 *   CR ID: ALPS00243673
                 *   Descriptions: consider type2's accountType is null
                 */
                if (type1.accountType == null || type2.accountType == null) {
                    Log.i(TAG,"type.accountType is null");
                    return 1;
                }
                /*
                 * Bug Fix by Mediatek End.
                 */
                
                Log.i(TAG,"ype1.accountType : "+type1.accountType +" | type2.accountType : "+type2.accountType);
                
                
                value = type1.accountType.compareTo(type2.accountType);
                if (value != 0) {
                    return value;
                } else {
                    // Fall back to data set.
                    if (type1.dataSet != null) {
                        value = type1.dataSet.compareTo(type2.dataSet);
                        if (value != 0) {
                            return value;
                        }
                    } else if (type2.dataSet != null) {
                        return 1;
                    }
                }
            }

            // Check account name
            ValuesDelta oneValues = one.getValues();
            String oneAccount = oneValues.getAsString(RawContacts.ACCOUNT_NAME);
            if (oneAccount == null) oneAccount = "";
            ValuesDelta twoValues = two.getValues();
            String twoAccount = twoValues.getAsString(RawContacts.ACCOUNT_NAME);
            if (twoAccount == null) twoAccount = "";
            value = oneAccount.compareTo(twoAccount);
            if (value != 0) {
                return value;
            }

            // Both are in the same account, fall back to contact ID
            Long oneId = oneValues.getAsLong(RawContacts._ID);
            Long twoId = twoValues.getAsLong(RawContacts._ID);
            if (oneId == null) {
                return -1;
            } else if (twoId == null) {
                return 1;
            }

            return (int)(oneId - twoId);
        }
    }

    /**
     * Returns the contact ID for the currently edited contact or 0 if the contact is new.
     */
    protected long getContactId() {
        if (mState != null) {
            for (EntityDelta rawContact : mState) {
                Long contactId = rawContact.getValues().getAsLong(RawContacts.CONTACT_ID);
                if (contactId != null) {
                    return contactId;
                }
            }
        }
        return 0;
    }

    /**
     * Triggers an asynchronous search for aggregation suggestions.
     */
    public void acquireAggregationSuggestions(RawContactEditorView rawContactEditor) {
        long rawContactId = rawContactEditor.getRawContactId();
        if (mAggregationSuggestionsRawContactId != rawContactId
                && mAggregationSuggestionView != null) {
            mAggregationSuggestionView.setVisibility(View.GONE);
            mAggregationSuggestionView = null;
            mAggregationSuggestionEngine.reset();
        }

        mAggregationSuggestionsRawContactId = rawContactId;

        if (mAggregationSuggestionEngine == null) {
            mAggregationSuggestionEngine = new AggregationSuggestionEngine(getActivity());
            mAggregationSuggestionEngine.setListener(this);
            mAggregationSuggestionEngine.start();
        }

        mAggregationSuggestionEngine.setContactId(getContactId());

        LabeledEditorView nameEditor = rawContactEditor.getNameEditor();
        mAggregationSuggestionEngine.onNameChange(nameEditor.getValues());
    }

    @Override
    public void onAggregationSuggestionChange() {
        if (!isAdded() || mState == null || mStatus != Status.EDITING) {
            return;
        }
        /* 
         * New Feature by Mediatek Begin.
         * Original Android's code:
         * 
         * 
         * Descriptions: Remove join function when edit name in SIM/USIM mode
         */
        if(isSimType()){
            return;
        }
        /*
         * Change Feature by Mediatek End.
         */
        if (mAggregationSuggestionPopup != null && mAggregationSuggestionPopup.isShowing()) {
            mAggregationSuggestionPopup.dismiss();
        }

        if (mAggregationSuggestionEngine.getSuggestedContactCount() == 0) {
            return;
        }

        final RawContactEditorView rawContactView =
                (RawContactEditorView)getRawContactEditorView(mAggregationSuggestionsRawContactId);
        if (rawContactView == null) {
            return; // Raw contact deleted?
        }
        final View anchorView = rawContactView.findViewById(R.id.anchor_view);
        mAggregationSuggestionPopup = new ListPopupWindow(mContext, null);
        mAggregationSuggestionPopup.setAnchorView(anchorView);
        mAggregationSuggestionPopup.setWidth(anchorView.getWidth());
        mAggregationSuggestionPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        mAggregationSuggestionPopup.setModal(true);
        mAggregationSuggestionPopup.setAdapter(
                new AggregationSuggestionAdapter(getActivity(),
                        mState.size() == 1 && mState.get(0).isContactInsert(),
                        this, mAggregationSuggestionEngine.getSuggestions()));
        mAggregationSuggestionPopup.setOnItemClickListener(mAggregationSuggestionItemClickListener);
        
        //Gionee:huangzy 20130326 modify for CR00789279 start
        /*mAggregationSuggestionPopup.show();*/
        if (isAdded() && null != getActivity() && !getActivity().isFinishing()) {
        	mAggregationSuggestionPopup.show();
        }
        //Gionee:huangzy 20130326 modify for CR00789279 end
    }

    @Override
    public void onJoinAction(long contactId, List<Long> rawContactIdList) {
        long rawContactIds[] = new long[rawContactIdList.size()];
        for (int i = 0; i < rawContactIds.length; i++) {
            rawContactIds[i] = rawContactIdList.get(i);
        }
        JoinSuggestedContactDialogFragment dialog =
                new JoinSuggestedContactDialogFragment();
        Bundle args = new Bundle();
        args.putLongArray("rawContactIds", rawContactIds);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        try {
            dialog.show(getFragmentManager(), "join");
        } catch (Exception ex) {
            // No problem - the activity is no longer available to display the dialog
        }
    }

    public static class JoinSuggestedContactDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AuroraAlertDialog.Builder(getActivity())
                    .setTitle(R.string.aggregation_suggestion_join_dialog_title)
                    .setMessage(R.string.aggregation_suggestion_join_dialog_message)
                    .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                AuroraContactEditorFragment targetFragment =
                                        (AuroraContactEditorFragment) getTargetFragment();
                                long rawContactIds[] =
                                        getArguments().getLongArray("rawContactIds");
                                targetFragment.doJoinSuggestedContact(rawContactIds);
                            }
                        }
                    )
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        }
    }

    /**
     * Joins the suggested contact (specified by the id's of constituent raw
     * contacts), save all changes, and stay in the editor.
     */
    protected void doJoinSuggestedContact(long[] rawContactIds) {
        if (!hasValidState() || mStatus != Status.EDITING) {
            return;
        }

        mState.setJoinWithRawContacts(rawContactIds);
        save(SaveMode.RELOAD);
    }

    @Override
    public void onEditAction(Uri contactLookupUri) {
        SuggestionEditConfirmationDialogFragment dialog =
                new SuggestionEditConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("contactUri", contactLookupUri);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "edit");
    }

    public static class SuggestionEditConfirmationDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AuroraAlertDialog.Builder(getActivity())
                    .setTitle(R.string.aggregation_suggestion_edit_dialog_title)
                    .setMessage(R.string.aggregation_suggestion_edit_dialog_message)
                    .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                AuroraContactEditorFragment targetFragment =
                                        (AuroraContactEditorFragment) getTargetFragment();
                                Uri contactUri =
                                        getArguments().getParcelable("contactUri");
                                targetFragment.doEditSuggestedContact(contactUri);
                            }
                        }
                    )
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        }
    }

    /**
     * Abandons the currently edited contact and switches to editing the suggested
     * one, transferring all the data there
     */
    protected void doEditSuggestedContact(Uri contactUri) {
        if (mListener != null) {
            // make sure we don't save this contact when closing down
            mStatus = Status.CLOSING;
            mListener.onEditOtherContactRequested(
                    contactUri, mState.get(0).getContentValues());
        }
    }

    public void setAggregationSuggestionViewEnabled(boolean enabled) {
        if (mAggregationSuggestionView == null) {
            return;
        }

//        LinearLayout itemList = (LinearLayout) mAggregationSuggestionView.findViewById(
//                R.id.aggregation_suggestions);
//        int count = itemList.getChildCount();
//        for (int i = 0; i < count; i++) {
//            itemList.getChildAt(i).setEnabled(enabled);
//        }
    }

    /**
     * Computes bounds of the supplied view relative to its ascendant.
     */
    private Rect getRelativeBounds(View ascendant, View view) {
        Rect rect = new Rect();
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());

        View parent = (View) view.getParent();
        while (parent != ascendant) {
            rect.offset(parent.getLeft(), parent.getTop());
            parent = (View) parent.getParent();
        }
        return rect;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_URI, mLookupUri);
        outState.putString(KEY_ACTION, mAction);

        if (hasValidState()) {
            // Store entities with modifications
            outState.putParcelable(KEY_EDIT_STATE, mState);
        }

        outState.putLong(KEY_RAW_CONTACT_ID_REQUESTING_PHOTO, mRawContactIdRequestingPhoto);
        outState.putParcelable(KEY_VIEW_ID_GENERATOR, mViewIdGenerator);
        if (mCurrentPhotoFile != null) {
            outState.putString(KEY_CURRENT_PHOTO_FILE, mCurrentPhotoFile.toString());
        }
        outState.putLong(KEY_CONTACT_ID_FOR_JOIN, mContactIdForJoin);
        outState.putBoolean(KEY_CONTACT_WRITABLE_FOR_JOIN, mContactWritableForJoin);
        outState.putLong(KEY_SHOW_JOIN_SUGGESTIONS, mAggregationSuggestionsRawContactId);
        outState.putBoolean(KEY_ENABLED, mEnabled);
        outState.putBoolean(KEY_NEW_LOCAL_PROFILE, mNewLocalProfile);
        outState.putBoolean(KEY_IS_USER_PROFILE, mIsUserProfile);
        outState.putInt(KEY_STATUS, mStatus);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mStatus == Status.SUB_ACTIVITY) {
            mStatus = Status.EDITING;
        }

        switch (requestCode) {
            // gionee xuhz 20120812 add for GIUI4.3 start
            case REQUEST_CODE_PHOTO_PICKED_WITH_DATA_OR_RESID: {
                // Ignore failed requests
                if (resultCode != Activity.RESULT_OK) return;
                
                {
                    // As we are coming back to this view, the editor will be reloaded automatically,
                    // which will cause the photo that is set here to disappear. To prevent this,
                    // we remember to set a flag which is interpreted after loading.
                    // This photo is set here already to reduce flickering.
                    if (ContactsApplication.sIsGnZoomClipSupport) {
                        String photoPath = data.getStringExtra("gn_data");
                        if (null != photoPath) {
                            File photoFile = new File(photoPath);
                            try {
                                Log.v("xuhz", "xuhz onActivityResult gn_data");
                                FileInputStream ffs = new FileInputStream(photoFile);
                                mPhoto = BitmapFactory.decodeStream(ffs);
                                ffs.close();
                                setPhoto(mRawContactIdRequestingPhoto, mPhoto);
                                mRawContactIdRequestingPhotoAfterLoad = mRawContactIdRequestingPhoto;
                                mRawContactIdRequestingPhoto = -1;
                                photoFile.delete();
                                break;
                            } catch (Exception e) {
                            }
                            return;
                        }
                    }
                    
                    mPhoto = data.getParcelableExtra("data");
                    setPhoto(mRawContactIdRequestingPhoto, mPhoto);
                    mRawContactIdRequestingPhotoAfterLoad = mRawContactIdRequestingPhoto;
                    mRawContactIdRequestingPhoto = -1;
                    
                    mIsGoingPickingRing = false;
                    
                    break;
                }
            }
            // gionee xuhz 20120812 add for GIUI4.3 end
        
            case REQUEST_CODE_PHOTO_PICKED_WITH_DATA: {
            	if (resultCode != Activity.RESULT_OK)
                    return;
                
                if (data != null) {
                	Uri uri = data.getData();
                    Log.d(TAG, "uri = " + uri);
                    if (uri != null) {
                    	Intent intent = new Intent().setDataAndType(uri, "image/*");
                        intent.setAction("com.android.camera.action.GNCROP");
                        startActivityForResult(intent, REQUEST_CODE_PHOTO_CROP);
                    }
                }
                
                break;
            }
            case REQUEST_CODE_CAMERA_WITH_DATA: {
                // Ignore failed requests
                if (resultCode != Activity.RESULT_OK) return;
                doCropPhoto(mCurrentPhotoFile);
                break;
            }
            case REQUEST_CODE_JOIN: {
                // Ignore failed requests
                if (resultCode != Activity.RESULT_OK) return;
                if (data != null) {
                    final long contactId = ContentUris.parseId(data.getData());
                    joinAggregate(contactId);
                }
                break;
            }
            case REQUEST_CODE_ACCOUNTS_CHANGED: {
                // Bail if the account selector was not successful.
                if (resultCode != Activity.RESULT_OK) {
                    mListener.onReverted();
                    return;
                }
                
                // If there's an account specified, use it.
                if (data != null) {
                	/*
                     * New Feature by Mediatek Begin. Original Android's code: CR
                     * ID: ALPS00101852 Descriptions: crete sim/usim contact
                     */
                    mSlotId = data.getIntExtra("mSlotId", -1);
                    mSimId = data.getLongExtra("mSimId", -1);
                    mNewSimType = data.getBooleanExtra("mIsSimType", false);
                    Log.i(TAG, "mslotid,msimid = " + mSlotId + "    " + mSimId);

                    //Gionee:huangzy 20120628 add for CR00624720 start
                    if (!mNewSimType) {
                        mSlotId = -1;
                    }                    
                    //Gionee:huangzy 20120628 add for CR00624720 end
                    
                    /*
                     * Change Feature by Mediatek End.
                     */
                    AccountWithDataSet account = data.getParcelableExtra(Intents.Insert.ACCOUNT);
                    if (account != null) {
                        createContact(account);
                        return;
                    }
                }
                // If there isn't an account specified, then this is likely a phone-local
                // contact, so we should continue setting up the editor by automatically selecting
                // the most appropriate account.
                createContact();
                break;
            }
                // The following lines are provided and maintained by Mediatek
                // Inc.
            case REQUEST_CODE_SAVE_TO_SIM: {
                if (resultCode == Activity.RESULT_CANCELED) {
                    mStatus = Status.EDITING;
                    Log.i(TAG, "[REQUEST_CODE_SAVE_TO_SIM] data is = " + data);
                    if (data != null) {
                        boolean mQuitEdit = data.getBooleanExtra("mQuitEdit", false);
                        Log.i(TAG,"[REQUEST_CODE_SAVE_TO_SIM] mQuitEdit : "+mQuitEdit);
                        if(mQuitEdit){
                            getActivity().finish();
                            break;
                        }
                        ArrayList<EntityDelta> simData = new ArrayList<EntityDelta>();
                        simData = data.getParcelableArrayListExtra("simData1");
                        mState = (EntityDeltaList) simData;
                        mRequestFocus = true;
                        mAggregationSuggestionsRawContactId = 0;
                        mEnabled = true;
                        String s=mSaveToTextView.getText().toString();//aurora add zhouxiaobing 20140226
                        bindEditors();
                        mSaveToTextView.setText(s);
                        mIsSaveToSim = true;
                        Log.i(TAG,"[REQUEST_CODE_SAVE_TO_SIM] bindEditors");
                        return;
                    }
                } else if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                getActivity().finish();
                break;
            }
                // The following lines are provided and maintained by Mediatek
                // Inc.
            
            case REQUEST_CODE_PICK_RINGTONE: {
            	//Gionee:huangzy 20130316 add for CR00774040 start
            	mIsGoingPickingRing = false;
            	//Gionee:huangzy 20130316 add for CR00774040 end
            	if (resultCode == Activity.RESULT_OK) {
            		final Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            		
            		// Gionee <xuhz> <2013-07-23> modify CR00838513 begin
            		/*if (null != pickedUri && null != mState &&
            				-1 != mPickingRingtoneStateIndex && mPickingRingtoneStateIndex < mState.size()) {
            			EntityDelta ed = mState.get(mPickingRingtoneStateIndex);
            			if (null != ed) {
            				ed.getValues().put(Contacts.CUSTOM_RINGTONE, pickedUri.toString());	
            			}
            			bindEditors();
            		}*/
				if (null != pickedUri && null != mState) {
					for (EntityDelta ed : mState) {
						if (null != ed) {
							ed.getValues().put(Contacts.CUSTOM_RINGTONE,
									pickedUri.toString());
						}
						if (!pickedUri.toString().equals(exRingUri)) {
							mIsRingChanged = true;
							Log.i(TAG, "mIsRingChanged:" + mIsRingChanged);
						}
						bindEditors();
					}
				}
            		// Gionee <xuhz> <2013-07-23> modify CR00838513 end
            	} /*else {
            		mPickingRingtoneStateIndex = -1;
            	}*/
            	
            	break;	
            }
            
            case REQUEST_CODE_PHOTO_CROP: {
            	if (data == null) {
            		return;
            	}
            	
            	if (ContactsApplication.sIsGnZoomClipSupport) {
                    String photoPath = data.getStringExtra("gn_data");                    
                    if (null != photoPath) {
                        File photoFile = new File(photoPath);
                        try {
                        	Log.d(TAG, "xuhz onActivityResult gn_data");
                            FileInputStream ffs = new FileInputStream(photoFile);
                            mPhoto = BitmapFactory.decodeStream(ffs);
                            ffs.close();
                            setPhoto(mRawContactIdRequestingPhoto, mPhoto);
                            mRawContactIdRequestingPhotoAfterLoad = mRawContactIdRequestingPhoto;
                            mRawContactIdRequestingPhoto = -1;
                            photoFile.delete();
                            break;
                        } catch (Exception e) {
                        }
                        return;
                    }
                }
                
                mPhoto = data.getParcelableExtra("data");
                setPhoto(mRawContactIdRequestingPhoto, mPhoto);
                mRawContactIdRequestingPhotoAfterLoad = mRawContactIdRequestingPhoto;
                mRawContactIdRequestingPhoto = -1;
                
                mIsGoingPickingRing = false;
                
            	break;
            }
        }
    }

    /**
     * Sets the photo stored in mPhoto and writes it to the RawContact with the given id
     */
    private void setPhoto(long rawContact, Bitmap photo) {
        BaseRawContactEditorView requestingEditor = getRawContactEditorView(rawContact);
        if (requestingEditor != null) {
        	Log.v("xuhz", "xuhz setPhoto requestingEditor != null");
            requestingEditor.setPhotoBitmap(photo);
        } else {
        	Log.v("xuhz", "xuhz setPhoto requestingEditor == null");
            Log.w(TAG, "The contact that requested the photo is no longer present.");
        }
    }

    /**
     * Finds raw contact editor view for the given rawContactId.
     */
    public BaseRawContactEditorView getRawContactEditorView(long rawContactId) {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View childView = mContent.getChildAt(i);
            if (childView instanceof BaseRawContactEditorView) {
                final BaseRawContactEditorView editor = (BaseRawContactEditorView) childView;
                if (editor.getRawContactId() == rawContactId) {
                    return editor;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if there is currently more than one photo on screen.
     */
    private boolean hasMoreThanOnePhoto() {
        int count = mContent.getChildCount();
        int countWithPicture = 0;
        for (int i = 0; i < count; i++) {
            final View childView = mContent.getChildAt(i);
            if (childView instanceof BaseRawContactEditorView) {
                final BaseRawContactEditorView editor = (BaseRawContactEditorView) childView;
                if (editor.hasSetPhoto()) {
                    countWithPicture++;
                    if (countWithPicture > 1) return true;
                }
            }
        }

        return false;
    }

    /**
     * The listener for the data loader
     */
    private final LoaderManager.LoaderCallbacks<ContactLoader.Result> mDataLoaderListener =
            new LoaderCallbacks<ContactLoader.Result>() {
    	private long mLoaderStartTime;
    	
        @Override
        public Loader<ContactLoader.Result> onCreateLoader(int id, Bundle args) {
            mLoaderStartTime = SystemClock.elapsedRealtime();
            return new ContactLoader(mContext, mLookupUri, mIsPrivacyContact);
        }

        @Override
        public void onLoadFinished(Loader<ContactLoader.Result> loader, ContactLoader.Result data) {
            final long loaderCurrentTime = SystemClock.elapsedRealtime();
            Log.v(TAG, "Time needed for loading: " + (loaderCurrentTime-mLoaderStartTime));
            if (!data.isLoaded()) {
                // Item has been deleted
                Log.i(TAG, "No contact found. Closing activity");
                if (mListener != null) mListener.onContactNotFound();
                return;
            }

            mStatus = Status.EDITING;
            mLookupUri = data.getLookupUri();
            final long setDataStartTime = SystemClock.elapsedRealtime();
            setData(data);
            final long setDataEndTime = SystemClock.elapsedRealtime();

            // If we are coming back from the photo trimmer, this will be set.
            if (mRawContactIdRequestingPhotoAfterLoad != -1) {
                setPhoto(mRawContactIdRequestingPhotoAfterLoad, mPhoto);
                mRawContactIdRequestingPhotoAfterLoad = -1;
                mPhoto = null;
            }
            Log.v(TAG, "Time needed for setting UI: " + (setDataEndTime-setDataStartTime));
        }

        @Override
        public void onLoaderReset(Loader<ContactLoader.Result> loader) {
        }
    };

    /**
     * The listener for the group meta data loader for all groups.
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return new GroupMetaDataLoader(mContext, Groups.CONTENT_URI);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        	mGroupMetaData = data;
            bindGroupMetaData();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    

    @Override
    public void onSplitContactConfirmed() {
        if (mState == null) {
            // This may happen when this Fragment is recreated by the system during users
            // confirming the split action (and thus this method is called just before onCreate()),
            // for example.
            Log.e(TAG, "mState became null during the user's confirming split action. " +
                    "Cannot perform the save action.");
            return;
        }

        mState.markRawContactsForSplitting();
        save(SaveMode.SPLIT);
    }
    
    private final class OrganizationEditorListener implements EditorListener {

    	private final BaseRawContactEditorView mEditor;
    	
    	private OrganizationEditorListener(BaseRawContactEditorView editor) {
            mEditor = editor;
        }
    	
		@Override
		public void onDeleteRequested(Editor editor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRequest(int request) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    

    private final class PhotoEditorListener
            implements EditorListener, PhotoActionPopup.Listener {
        private final BaseRawContactEditorView mEditor;
        private final boolean mAccountWritable;

        private PhotoEditorListener(BaseRawContactEditorView editor, boolean accountWritable) {
            mEditor = editor;
            mAccountWritable = accountWritable;
        }

        @Override
        public void onRequest(int request) {
            if (!hasValidState()) return;

            if (request == EditorListener.REQUEST_PICK_PHOTO) {
                // Determine mode
                final int mode;
                if (mAccountWritable) {
                    if (mEditor.hasSetPhoto()) {
                        if (hasMoreThanOnePhoto()) {
                            mode = PhotoActionPopup.MODE_PHOTO_ALLOW_PRIMARY;
                        } else {
                            mode = PhotoActionPopup.MODE_PHOTO_DISALLOW_PRIMARY;
                        }
                    } else {
                        mode = PhotoActionPopup.MODE_NO_PHOTO;
                    }
                } else {
                    if (mEditor.hasSetPhoto() && hasMoreThanOnePhoto()) {
                        mode = PhotoActionPopup.MODE_READ_ONLY_ALLOW_PRIMARY;
                    } else {
                        // Read-only and either no photo or the only photo ==> no options
                        return;
                    }
                }
                
               
                    // Gionee <xuhz> <2013-07-24> modify for CR00839225 begin
            		/*PhotoActionPopup.createDialogMenu(mContext, this, mode)
        			.show();*/
                	mPhotoActionDialog = PhotoActionPopup.createDialogMenu(mContext, this, mode);
                	mPhotoActionDialog.show();
                    // Gionee <xuhz> <2013-07-24> modify for CR00839225 end
            		return;
            	
//                  PhotoActionPopup.createPopupMenu(mContext, mEditor.getPhotoEditor(), this, mode)
//                        .show();
            }
        }

        @Override
        public void onDeleteRequested(Editor removedEditor) {
            // The picture cannot be deleted, it can only be removed, which is handled by
            // onRemovePictureChosen()
        }

        /**
         * User has chosen to set the selected photo as the (super) primary photo
         */
        @Override
        public void onUseAsPrimaryChosen() {
            // Set the IsSuperPrimary for each editor
            int count = mContent.getChildCount();
            for (int i = 0; i < count; i++) {
                final View childView = mContent.getChildAt(i);
                if (childView instanceof BaseRawContactEditorView) {
                    final BaseRawContactEditorView editor = (BaseRawContactEditorView) childView;
                    final PhotoEditorView photoEditor = editor.getPhotoEditor();
                    photoEditor.setSuperPrimary(editor == mEditor);
                }
            }
        }

        /**
         * User has chosen to remove a picture
         */
        @Override
        public void onRemovePictureChosen() {
            mEditor.setPhotoBitmap(null);
        }

        /**
         * Launches Camera to take a picture and store it in a file.
         */
        @Override
        public void onTakePhotoChosen() {
        	mIsGoingPickingRing = true;
        	
            mRawContactIdRequestingPhoto = mEditor.getRawContactId();
            try {
            	// Launch camera to take photo for selected contact
            	
                //Gionee:huangzy 20120809 modify for CR00671961 start
            	
            		//aurora sdcard working
            		String availableDir = null;//ContactsUtils.getAvailableSdcard(mContext, 1024*300);
            		File photoDir = (null != availableDir ? new File(availableDir + "/DCIM/Camera") : PHOTO_DIR);
            		photoDir.mkdirs();
            		Log.i("James", "photoDir = " + photoDir.toString());
            		mCurrentPhotoFile = new File(photoDir, getPhotoFileName());
            
                //Gionee:huangzy 20120809 modify for CR00671961 end
                
                final Intent intent = getTakePickIntent(mCurrentPhotoFile);

                mStatus = Status.SUB_ACTIVITY;
                startActivityForResult(intent, REQUEST_CODE_CAMERA_WITH_DATA);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, R.string.photoPickerNotFoundText,
                        Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Launches Gallery to pick a photo.
         */
        @Override
        public void onPickFromGalleryChosen() {
        	mIsGoingPickingRing = true;
        	
            mRawContactIdRequestingPhoto = mEditor.getRawContactId();
            try {
                // Launch picker to choose photo for selected contact
                final Intent intent = getPhotoPickIntent();
                mStatus = Status.SUB_ACTIVITY;
                startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, R.string.photoPickerNotFoundText,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
/* 
 * New Feature by Mediatek Begin.
 * Original Android's code:
 * 
 * CR ID: ALPS00101852
 * Descriptions: crete sim/usim contact
 */
public boolean saveToSimCard(EntityDeltaList mState, int saveMode) {
        Intent intent = new Intent(mContext, EditSimContactActivity.class);
        intent.putParcelableArrayListExtra("simData", mState);
        intent.putParcelableArrayListExtra("simOldData", mOldState);
        Log.i(TAG,"********* the old state : "+mOldState);
        // slotid
        
        if (!hasValidState() || mStatus != Status.EDITING) {
            return false;
        }
        
        mStatus = Status.SAVING;
        
        setEnabled(false);
        if (mSaveModeForSim == 1) {
            intent.putExtra("slotId", mSlotId);
        }else if (mSaveModeForSim == 2){
            mSlotId = ContactsUtils.getSlotBySubId(mIndicatePhoneOrSimContact);
            intent.putExtra("slotId", mSlotId);
        }
        Log.i(TAG,"[saveToSimCard] mSaveModeForSim : "+mSaveModeForSim);
        intent.putExtra(RawContacts.INDICATE_PHONE_SIM, mSimId);
        intent.putExtra("simSaveMode", mSaveModeForSim);
        intent.setData(mLookupUri);
        
        Log.i(TAG, "THE mState is = " + mState);
        Log.i(TAG, "THE mLookupUri is = " + mLookupUri);
        int i =0;
        if (mGroupMetaData != null) {
            int groupNum = mGroupMetaData.getPosition();
            String accountType = mState.get(0).getValues().getAsString(RawContacts.ACCOUNT_TYPE);
            if (accountType.equals(mUsimAccountType) && groupNum > 0) {
                String groupName[] = new String[groupNum];
                long groupId [] = new long [groupNum];
                mGroupMetaData.moveToPosition(-1);
                while (mGroupMetaData.moveToNext()) {
                    Log.i(TAG, "THE ACCOUNT_NAME is = "
                            + mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_NAME));
                    Log.i(TAG, "THE DATA_SET is = "
                            + mGroupMetaData.getString(GroupMetaDataLoader.DATA_SET));
                    Log.i(TAG, "THE GROUP_ID is = "
                            + mGroupMetaData.getLong(GroupMetaDataLoader.GROUP_ID));
                    Log.i(TAG, "THE TITLE is = "
                            + mGroupMetaData.getString(GroupMetaDataLoader.TITLE));
                    groupName[i] = mGroupMetaData.getString(GroupMetaDataLoader.TITLE);
                    groupId[i] = mGroupMetaData.getLong(GroupMetaDataLoader.GROUP_ID);
                    i++;
                    Log.i(TAG, "[saveToSimCard] I : " + i);
                }
                intent.putExtra("groupName", groupName);
                intent.putExtra("groupNum", groupNum);
                intent.putExtra("groupId", groupId);
                Log.i(TAG, "[saveToSimCard] groupNum : " + groupNum);

            }
        }
        
        startActivityForResult(intent,REQUEST_CODE_SAVE_TO_SIM);
        mIsSaveToSim = true;
        // for(int i = 0;i<mState.get(0).getEntryCount(false);i++){
        // final ValuesDelta values =
        // mState.get(0).getContentValues().get(i);
        //        
        // String dataSet = values.getAsString(RawContacts.DATA_SET);
        // Log.w(TAG,"THE mSaveModeForSim(1 for new contact, 2 for existing contact) is = "+mSaveModeForSim);
        // Log.w(TAG,"THE dataSet is = "+dataSet);
        // Log.w(TAG,"THE values is = "+values);
        // }
        return true;
    }
        
    private boolean isSimType() {
    	return false;
    }
    
    private String mSimAccountType =  AccountType.ACCOUNT_TYPE_SIM;
    private String mUsimAccountType =  AccountType.ACCOUNT_TYPE_USIM;
    private String mLocalPhoneAccountType =  AccountType.ACCOUNT_TYPE_LOCAL_PHONE;
    private int mIndicatePhoneOrSimContact;
    private static final int REQUEST_CODE_SAVE_TO_SIM = 4;
    private boolean mIsSaveToSim = false;
    private static int mSaveModeForSim;
    private static int mSlotId = -1;
    private static long mSimId = -1;
    private static EntityDeltaList mOldState;
    private boolean mIsSimType = false;
    private boolean mNewSimType = false;
    public static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
/*
 * Change Feature by Mediatek End.
 */
    
    public void log(String msg) {
    	Log.i("James", msg);
    }
    
    // Gionee zhangxx 2012-05-30 add for CR00605159 begin
    private boolean isContactEditorWithName() {
        boolean hasName = false;
        ArrayList<ValuesDelta> names = mState.get(0).getMimeEntries(StructuredName.CONTENT_ITEM_TYPE);
        
        if (names == null) {
            return hasName;
        }
        
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i).getAsString(StructuredName.DISPLAY_NAME);
            if (name != null && name.length() > 0) {
                hasName = true;
                // Gionee:wangth 20120607 add for CR00575920 begin
                if (name.trim().isEmpty()) {
                    hasName = false;
                }
                // Gionee:wangth 20120607 add for CR00575920 end
                break;
            }

            // Gionee:wangth 20120605 add for CR00616198 begin
            name = names.get(i).getAsString(StructuredName.GIVEN_NAME);
            if (name != null && name.length() > 0) {
                hasName = true;
                // Gionee:wangth 20120607 add for CR00575920 begin
                if (name.trim().isEmpty()) {
                    hasName = false;
                }
                // Gionee:wangth 20120607 add for CR00575920 end
            }
            // Gionee:wangth 20120605 add for CR00616198 end
            
            // Gionee:wangth 20120618 add for CR00625658 begin
            name = names.get(i).getAsString(StructuredName.FAMILY_NAME);
            if (name != null && name.length() > 0) {
                hasName = true;
                if (name.trim().isEmpty()) {
                    hasName = false;
                }
            }
            // Gionee:wangth 20120618 add for CR00625658 end
        }

        return hasName;
    }
    
    private boolean isContactEditorWithNumber() {
        boolean hasNubmer = false;
        ArrayList<ValuesDelta> phones = mState.get(0).getMimeEntries(Phone.CONTENT_ITEM_TYPE);
        // gionee xuhz 20120527 add for CR00608825 start
        if (phones == null) {
            return hasNubmer;
        }
        // gionee xuhz 20120527 add for CR00608825 end
        for (int i = 0; i < phones.size(); i++) {
            // gionee xuhz 20120713 add for CR00640604 start
            ValuesDelta values = phones.get(i);
            if (values.isDelete()) {
                continue;
            }
            // gionee xuhz 20120713 add for CR00640604 end
            
            String number = phones.get(i).getAsString(Phone.NUMBER);
            if (number != null) {
            	number = number.trim();
            	if (!TextUtils.isEmpty(number)) {
            		return hasNubmer;
            	}
            }
        }
        return hasNubmer;
    }
    // Gionee zhangxx 2012-05-30 add for CR00605159 end
    
    // Gionee:wangth 20120604 add for CR00608106 begin
    public static boolean mIsUsimMode() {
        return SimCardUtils.isSimUsimType((int)mSimId);
    }
    // Gionee:wangth 20120604 add for CR00608106 end
    
    // Gionee:wangth 20120618 add for CR00625559 begin
    private int contactNameCount() {
        if (null == mState || mState.size() < 1) {
            return 0;
        }
        int nameCount = 0;
        ArrayList<ValuesDelta> names = mState.get(0).getMimeEntries(StructuredName.CONTENT_ITEM_TYPE);
        String accountType = mState.get(0).getValues().getAsString(RawContacts.ACCOUNT_TYPE);
        
        if (names == null) {
            return nameCount;
        }
        
        for (int i = 0; i < names.size(); i++) {
            String displayName = names.get(i).getAsString(StructuredName.DISPLAY_NAME);
            if (displayName != null && displayName.length() > 0
                    && accountType != null
                    && accountType.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                nameCount = displayName.length();
                Log.d(TAG, "displayName = " + displayName + " displayName.count = " + nameCount);
            } else {
                String name = names.get(i).getAsString(StructuredName.PREFIX);
                if (name != null && name.length() > 0) {
                    nameCount = name.length();
                }
                
                name = names.get(i).getAsString(StructuredName.GIVEN_NAME);
                if (name != null && name.length() > 0) {
                    nameCount += name.length();
                }
                
                name = names.get(i).getAsString(StructuredName.FAMILY_NAME);
                if (name != null && name.length() > 0) {
                    nameCount += name.length();
                }
                
                name = names.get(i).getAsString(StructuredName.MIDDLE_NAME);
                if (name != null && name.length() > 0) {
                    nameCount += name.length();
                }
                
                name = names.get(i).getAsString(StructuredName.SUFFIX);
                if (name != null && name.length() > 0) {
                    nameCount += name.length();
                }
            }
        }

        Log.d(TAG, "nameCount = " + nameCount);
        return nameCount;
    }
    // Gionee:wangth 20120618 add for CR00625559 end
    
    //gionee yeweiqun 2012.10.10 modify for CR00710328 begin
    public static int getSlotId(){ 
		return mSlotId;
	}
    //gionee yeweiqun 2012.10.10 modify for CR00710328 end
    
    private void handleRingtonePicked(Uri lookupUri) {    	
    	//Gionee <huangzy> <2013-04-26> modify for CR00779351 begin
    	if (null == lookupUri) {
    		return;
    	}
    	
    	//Gionee <huangzy> <2013-07-08> modify for CR00833285 begin
    	/*final long contactId = ContentUris.parseId(Contacts.lookupContact(
                mContext.getContentResolver(), lookupUri));    	
    	EntityDelta ed = mState.getByRawContactId(contactId);*/
    	if (null == mState || mState.size() < 0) {
    		return;
    	}
    	EntityDelta ed = mState.get(0);
    	//Gionee <huangzy> <2013-07-08> modify for CR00833285 end
    	if (null != ed) {
    		// gionee xuhz 20130309 modify for CR00779157 start
    		Uri pickedUri = null;
    		String ringtoneString = ed.getValues().getAsString(Contacts.CUSTOM_RINGTONE);
    		if (!TextUtils.isEmpty(ringtoneString)) {
    			pickedUri = Uri.parse(ringtoneString);
    		}
    		// gionee xuhz 20130309 modify for CR00779157 end
        	String customRingtone = null;
            if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
                customRingtone = null;
            } else {
                customRingtone = pickedUri.toString();
            }
            
            Intent intent = ContactSaveService.createSetRingtone(
            		mContext, lookupUri, customRingtone);
            mContext.startService(intent);
    	}
    	//Gionee <huangzy> <2013-04-26> modify for CR00779351 end
    }
}
