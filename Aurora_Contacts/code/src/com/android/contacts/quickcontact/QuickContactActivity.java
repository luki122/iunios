/*
 * Copyright (C) 2009 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.contacts.quickcontact;

import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import android.app.ActivityManager;
import android.accounts.Account;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Trace;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Identity;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.DisplayNameSources;
import android.provider.ContactsContract.DataUsageFeedback;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.QuickContact;
import android.provider.ContactsContract.RawContacts;
import android.support.v7.graphics.Palette;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.android.contacts.AuroraContactsActivity;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.common.GroupMetaData;
import com.android.contacts.NfcHandler;
import com.android.contacts.R;
import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ClipboardUtils;
import com.android.contacts.common.Collapser;
import com.android.contacts.common.ContactsUtils;
// Aurora xuyong 2016-01-13 added for aurora 2.0 new feature start
import com.android.contacts.common.util.BitmapUtil;
// Aurora xuyong 2016-01-13 added for aurora 2.0 new feature end
import com.android.contacts.editor.SelectAccountDialogFragment;
import com.android.contacts.common.interactions.TouchPointManager;
import com.android.contacts.common.lettertiles.LetterTileDrawable;
import com.android.contacts.common.list.ShortcutIntentBuilder;
import com.android.contacts.common.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.ContactLoader;
import com.android.contacts.common.model.RawContact;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountType.*;
//import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.common.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.common.model.account.BaseAccountType.EventActionInflater;
import com.android.contacts.common.model.dataitem.DataItem;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.model.dataitem.EmailDataItem;
import com.android.contacts.common.model.dataitem.EventDataItem;
import com.android.contacts.common.model.dataitem.ImDataItem;
import com.android.contacts.common.model.dataitem.NicknameDataItem;
import com.android.contacts.common.model.dataitem.NoteDataItem;
import com.android.contacts.common.model.dataitem.OrganizationDataItem;
import com.android.contacts.common.model.dataitem.PhoneDataItem;
import com.android.contacts.common.model.dataitem.RelationDataItem;
import com.android.contacts.common.model.dataitem.SipAddressDataItem;
import com.android.contacts.common.model.dataitem.StructuredNameDataItem;
import com.android.contacts.common.model.dataitem.StructuredPostalDataItem;
import com.android.contacts.common.model.dataitem.WebsiteDataItem;
import com.android.contacts.common.model.dataitem.GroupMembershipDataItem;
import com.android.contacts.common.util.DateUtils;
import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.common.util.MaterialColorMapUtils.MaterialPalette;
import com.android.contacts.common.util.ViewUtil;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.detail.ContactDisplayUtils;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.interactions.CalendarInteractionsLoader;
import com.android.contacts.interactions.CallLogInteractionsLoader;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.interactions.ContactInteraction;
import com.android.contacts.interactions.SmsInteractionsLoader;
import com.android.contacts.interactions.CallLogInteraction;
import com.android.contacts.quickcontact.ExpandingEntryCardView.Entry;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryContextMenuInfo;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryTag;
import com.android.contacts.quickcontact.ExpandingEntryCardView.ExpandingEntryCardViewListener;
import com.android.contacts.util.Blur;
import com.android.contacts.util.ImageViewDrawableSetter;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SchedulingUtils;
import com.android.contacts.util.StructuredPostalUtils;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.widget.MultiShrinkScroller;
import com.android.contacts.widget.MultiShrinkScroller.MultiShrinkScrollerListener;
import com.android.contacts.widget.QuickContactImageView;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.NullCipher;

import android.os.Build;

import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
import com.android.contacts.activities.SystemUtils;

import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.widget.ImageView;

import com.android.contacts.common.model.account.AccountType.EditField;
import com.android.contacts.common.util.CommonDateUtils;

import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.contacts.util.NumberAreaUtil;

import aurora.app.AuroraAlertDialog;

import com.android.contacts.*;
import com.privacymanage.service.AuroraPrivacyUtils;

import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import gionee.provider.GnTelephony.SIMInfo;

import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.android.contacts.activities.ContactsLog;

import android.telephony.PhoneNumberUtils;
import android.view.KeyEvent;
import android.widget.AdapterView.OnItemClickListener;
import gionee.telephony.AuroraTelephoneManager;

import com.android.contacts.activities.ContactEditorActivity;

import android.provider.ContactsContract.RawContactsEntity;

import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import android.content.ClipboardManager;
import android.content.ClipData;
/**
 * Mostly translucent {@link Activity} that shows QuickContact dialog. It loads
 * data asynchronously, and then shows a popup with details centered around
 * {@link Intent#getSourceBounds()}.
 */
public class QuickContactActivity extends AuroraContactsActivity {

    /**
     * QuickContacts immediately takes up the full screen. All possible information is shown.
     * This value for {@link android.provider.ContactsContract.QuickContact#EXTRA_MODE}
     * should only be used by the Contacts app.
     */
    public static final int MODE_FULLY_EXPANDED = 4;

    private static final String TAG = "QuickContact";

    private static final String KEY_THEME_COLOR = "theme_color";

    private static final int ANIMATION_STATUS_BAR_COLOR_CHANGE_DURATION = 150;
    private static final int REQUEST_CODE_CONTACT_EDITOR_ACTIVITY = 1;
//    private static final int DEFAULT_SCRIM_ALPHA = 0xC8;
    private static final int DEFAULT_SCRIM_ALPHA = 0xFF;
    private static final int SCRIM_COLOR = Color.argb(DEFAULT_SCRIM_ALPHA, 0, 0, 0);
    private static final int REQUEST_CODE_CONTACT_SELECTION_ACTIVITY = 2;
    private static final String MIMETYPE_SMS = "vnd.android-dir/mms-sms";

    /** This is the Intent action to install a shortcut in the launcher. */
    private static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";

    @SuppressWarnings("deprecation")
    private static final String LEGACY_AUTHORITY = android.provider.Contacts.AUTHORITY;

    private static final String MIMETYPE_GPLUS_PROFILE =
            "vnd.android.cursor.item/vnd.googleplus.profile";
    private static final String INTENT_DATA_GPLUS_PROFILE_ADD_TO_CIRCLE = "Add to circle";
    private static final String MIMETYPE_HANGOUTS =
            "vnd.android.cursor.item/vnd.googleplus.profile.comm";
    private static final String INTENT_DATA_HANGOUTS_VIDEO = "Start video call";
    private static final String CALL_ORIGIN_QUICK_CONTACTS_ACTIVITY =
            "com.android.contacts.quickcontact.QuickContactActivity";

    /**
     * The URI used to load the the Contact. Once the contact is loaded, use Contact#getLookupUri()
     * instead of referencing this URI.
     */
    private Uri mLookupUri;
    private String[] mExcludeMimes;
    private int mExtraMode;
    private int mStatusBarColor;
    private boolean mHasAlreadyBeenOpened;
    private boolean mOnlyOnePhoneNumber;
    private boolean mOnlyOneEmail;

    private View mTransparentView;
    private QuickContactImageView mPhotoView, mBlurPhotoView;
    private View mPhotoCover;
    private ImageView mSmallPhotoView;
    private TextView mLargeTextView;
    private PhoneExpandingEntryCardView mContactCard;
    private ExpandingEntryCardView mNoContactDetailsCard;
    private CallLogExpandingEntryCardView mRecentCard;
    private AboutExpandingEntryCardView mAboutCard;
    private MultiShrinkScroller mScroller;
    private SelectAccountDialogFragmentListener mSelectAccountFragmentListener;
    private AsyncTask<Void, Void, Cp2DataCardModel> mEntriesAndActionsTask;
    private AsyncTask<Void, Void, Void> mRecentDataTask;
    /**
     * The last copy of Cp2DataCardModel that was passed to {@link #populateContactAndAboutCard}.
     */
    private Cp2DataCardModel mCachedCp2DataCardModel;
    /**
     *  This scrim's opacity is controlled in two different ways. 1) Before the initial entrance
     *  animation finishes, the opacity is animated by a value animator. This is designed to
     *  distract the user from the length of the initial loading time. 2) After the initial
     *  entrance animation, the opacity is directly related to scroll position.
     */
    private ColorDrawable mWindowScrim;
    private boolean mIsEntranceAnimationFinished;
    private boolean mIsExitAnimationFinished = true;
    private MaterialColorMapUtils mMaterialColorMapUtils;
    private boolean mIsExitAnimationInProgress;
    private boolean mHasComputedThemeColor;

    /**
     * Used to stop the ExpandingEntry cards from adjusting between an entry click and the intent
     * being launched.
     */
    private boolean mHasIntentLaunched;
    private boolean mHasDialNumber;

    private Contact mContactData;
    private ContactLoader mContactLoader;
    private PorterDuffColorFilter mColorFilter;

    private ImageViewDrawableSetter mPhotoSetter;
    
    

    /**
     * {@link #LEADING_MIMETYPES} is used to sort MIME-types.
     *
     * <p>The MIME-types in {@link #LEADING_MIMETYPES} appear in the front of the dialog,
     * in the order specified here.</p>
     */
    private static final List<String> LEADING_MIMETYPES = Lists.newArrayList(
            Phone.CONTENT_ITEM_TYPE, SipAddress.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE);

    private static final List<String> SORTED_ABOUT_CARD_MIMETYPES = Lists.newArrayList(
            Nickname.CONTENT_ITEM_TYPE,
            // Phonetic name is inserted after nickname if it is available.
            // No mimetype for phonetic name exists.
            Website.CONTENT_ITEM_TYPE,
            Organization.CONTENT_ITEM_TYPE,
            Event.CONTENT_ITEM_TYPE,
            Relation.CONTENT_ITEM_TYPE,
            Im.CONTENT_ITEM_TYPE,
            GroupMembership.CONTENT_ITEM_TYPE,
            Identity.CONTENT_ITEM_TYPE,
            Note.CONTENT_ITEM_TYPE);

    private static final BidiFormatter sBidiFormatter = BidiFormatter.getInstance();

    /** Id for the background contact loader */
    private static final int LOADER_CONTACT_ID = 0;

    private static final String KEY_LOADER_EXTRA_PHONES =
            QuickContactActivity.class.getCanonicalName() + ".KEY_LOADER_EXTRA_PHONES";

    /** Id for the background Sms Loader */
    // Aurora xuyong 2016-01-05 deleted for bug #18275 start
    /*private static final int LOADER_SMS_ID = 1;
    private static final int MAX_SMS_RETRIEVE = 3;*/
    // Aurora xuyong 2016-01-05 deleted for bug #18275 end

    /** Id for the back Calendar Loader */
    private static final int LOADER_CALENDAR_ID = 2;
    private static final String KEY_LOADER_EXTRA_EMAILS =
            QuickContactActivity.class.getCanonicalName() + ".KEY_LOADER_EXTRA_EMAILS";
    private static final int MAX_PAST_CALENDAR_RETRIEVE = 3;
    private static final int MAX_FUTURE_CALENDAR_RETRIEVE = 3;
    private static final long PAST_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR =
            1L * 24L * 60L * 60L * 1000L /* 1 day */;
    private static final long FUTURE_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR =
            7L * 24L * 60L * 60L * 1000L /* 7 days */;

    /** Id for the background Call Log Loader */
    private static final int LOADER_CALL_LOG_ID = 3;
    // Aurora xuyong 2016-01-13 modified fora aurora 2.0 new feature start
    private static final int MAX_CALL_LOG_RETRIEVE = 4;
    // Aurora xuyong 2016-01-13 modified fora aurora 2.0 new feature end
    private static final int MIN_NUM_CONTACT_ENTRIES_SHOWN = 3;
    private static final int MIN_NUM_COLLAPSED_RECENT_ENTRIES_SHOWN = 3;
    private static final int CARD_ENTRY_ID_EDIT_CONTACT = -2;


    private static final int[] mRecentLoaderIds = new int[]{
        // Aurora xuyong 2016-01-05 deleted for bug #18275 start
        /*LOADER_SMS_ID,*/
        // Aurora xuyong 2016-01-05 deleted for bug #18275 end
//        LOADER_CALENDAR_ID,
        LOADER_CALL_LOG_ID};
    private Map<Integer, List<ContactInteraction>> mRecentLoaderResults = new HashMap<>();

    private static final String FRAGMENT_TAG_SELECT_ACCOUNT = "select_account_fragment";

    final OnClickListener mEntryClickHandler = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.w(TAG, "mEntryClickHandler onClick ");
            final Object entryTagObject = v.getTag();
            if (entryTagObject == null || !(entryTagObject instanceof EntryTag)) {
                Log.w(TAG, "EntryTag was not used correctly");
                return;
            }
            final EntryTag entryTag = (EntryTag) entryTagObject;
            final Intent intent = entryTag.getIntent();
            final int dataId = entryTag.getId();
            
  		  Log.d(TAG, "dataId = " + dataId);
            if("PLAY_RECORD_ACTION".equals(intent.getAction())) {
            	try {
            		playRecord(mPhoneRecords.get(dataId));
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	return;
            }

            if (dataId == CARD_ENTRY_ID_EDIT_CONTACT) {
                editContact();
                return;
            }            

            // Default to USAGE_TYPE_CALL. Usage is summed among all types for sorting each data id
            // so the exact usage type is not necessary in all cases
            String usageType = DataUsageFeedback.USAGE_TYPE_CALL;

            final Uri intentUri = intent.getData();
            if ((intentUri != null && intentUri.getScheme() != null &&
                    intentUri.getScheme().equals(ContactsUtils.SCHEME_SMSTO)) ||
                    (intent.getType() != null && intent.getType().equals(MIMETYPE_SMS))) {
                usageType = DataUsageFeedback.USAGE_TYPE_SHORT_TEXT;
            }

            // Data IDs start at 1 so anything less is invalid
//            if (dataId > 0) {
//                final Uri dataUsageUri = DataUsageFeedback.FEEDBACK_URI.buildUpon()
//                        .appendPath(String.valueOf(dataId))
//                        .appendQueryParameter(DataUsageFeedback.USAGE_TYPE, usageType)
//                        .build();
//                final boolean successful = getContentResolver().update(
//                        dataUsageUri, new ContentValues(), null, null) > 0;
//                if (!successful) {
//                    Log.w(TAG, "DataUsageFeedback increment failed");
//                }
//            } else {
//                Log.w(TAG, "Invalid Data ID");
//            }

            // Pass the touch point through the intent for use in the InCallUI
            if (Intent.ACTION_CALL.equals(intent.getAction())) {
                if (TouchPointManager.getInstance().hasValidPoint()) {
                    Bundle extras = new Bundle();
                    extras.putParcelable(TouchPointManager.TOUCH_POINT,
                            TouchPointManager.getInstance().getPoint());
                    intent.putExtra(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras);
                }
            }

           

            // Force the window dim amount to the scrim value for app transition animations
            // The scrim may be removed before the window transitions to the new activity, which
            // can cause a flicker in the status and navigation bar. Set dim alone does not work
            // well because the request is passed through IPC which makes it slow to animate.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getWindow().setDimAmount(mWindowScrim.getAlpha() / DEFAULT_SCRIM_ALPHA);

            mHasIntentLaunched = true;
            
            //aurora modify by liguangyu 
            if (Intent.ACTION_CALL.equals(intent.getAction())) {   
//            	showCallDialog(intent);
            	// Aurora xuyong 2016-01-22 modified for bug #18556 start
            	//showCallPopupWindow(intent);
                Intent dial = IntentFactory.newDialNumberIntent(PhoneNumberUtils.getNumberFromIntent(intent, QuickContactActivity.this));
                dial.putExtra("contactUri", mContactData.getUri());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dial);      
                // Aurora xuyong 2016-01-22 modified for bug #18556 end
            } else {
            	startActivity(intent);
            }
    	  	mHasDialNumber = true;
        }
    };

    final ExpandingEntryCardViewListener mExpandingEntryCardViewListener
            = new ExpandingEntryCardViewListener() {
        @Override
        public void onCollapse(int heightDelta) {
            mScroller.prepareForShrinkingScrollChild(heightDelta);
        }

        @Override
        public void onExpand(int heightDelta) {
            mScroller.prepareForExpandingScrollChild();
        }
    };

    private interface ContextMenuIds {
        static final int COPY_TEXT = 0;
        static final int CLEAR_DEFAULT = 1;
        static final int SET_DEFAULT = 2;
    }

    private final OnCreateContextMenuListener mEntryContextMenuListener =
            new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (menuInfo == null) {
                return;
            }
            final EntryContextMenuInfo info = (EntryContextMenuInfo) menuInfo;
            menu.setHeaderTitle(info.getCopyText());
            menu.add(ContextMenu.NONE, ContextMenuIds.COPY_TEXT,
                    ContextMenu.NONE, getString(R.string.copy_text));

            // Don't allow setting or clearing of defaults for non-editable contacts
            if (!isContactEditable()) {
                return;
            }

            final String selectedMimeType = info.getMimeType();

            // Defaults to true will only enable the detail to be copied to the clipboard.
            boolean onlyOneOfMimeType = true;

            // Only allow primary support for Phone and Email content types
            if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
                onlyOneOfMimeType = mOnlyOnePhoneNumber;
            } else if (Email.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
                onlyOneOfMimeType = mOnlyOneEmail;
            }

            // Checking for previously set default
            if (info.isSuperPrimary()) {
                menu.add(ContextMenu.NONE, ContextMenuIds.CLEAR_DEFAULT,
                        ContextMenu.NONE, getString(R.string.clear_default));
            } else if (!onlyOneOfMimeType) {
                menu.add(ContextMenu.NONE, ContextMenuIds.SET_DEFAULT,
                        ContextMenu.NONE, getString(R.string.set_default));
            }
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        EntryContextMenuInfo menuInfo;
        try {
            menuInfo = (EntryContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case ContextMenuIds.COPY_TEXT:
                ClipboardUtils.copyText(this, menuInfo.getCopyLabel(), menuInfo.getCopyText(),
                        true);
                return true;
            case ContextMenuIds.SET_DEFAULT:
                final Intent setIntent = ContactSaveService.createSetSuperPrimaryIntent(this,
                        menuInfo.getId());
                this.startService(setIntent);
                return true;
            case ContextMenuIds.CLEAR_DEFAULT:
                final Intent clearIntent = ContactSaveService.createClearPrimaryIntent(this,
                        menuInfo.getId());
                this.startService(clearIntent);
                return true;
            default:
                throw new IllegalArgumentException("Unknown menu option " + item.getItemId());
        }
    }

    /**
     * Headless fragment used to handle account selection callbacks invoked from
     * {@link DirectoryContactUtil}.
     */
    public static class SelectAccountDialogFragmentListener extends Fragment
            implements SelectAccountDialogFragment.Listener {

        private QuickContactActivity mQuickContactActivity;

        public SelectAccountDialogFragmentListener() {}

        @Override
        public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
            DirectoryContactUtil.createCopy(mQuickContactActivity.mContactData.getContentValues(),
                    account, mQuickContactActivity);
        }

        @Override
        public void onAccountSelectorCancelled() {}

        /**
         * Set the parent activity. Since rotation can cause this fragment to be used across
         * more than one activity instance, we need to explicitly set this value instead
         * of making this class non-static.
         */
        public void setQuickContactActivity(QuickContactActivity quickContactActivity) {
            mQuickContactActivity = quickContactActivity;
        }
    }

    final MultiShrinkScrollerListener mMultiShrinkScrollerListener
            = new MultiShrinkScrollerListener() {
        @Override
        public void onScrolledOffBottom() {
            finish();
        }

        @Override
        public void onEnterFullscreen() {
            updateStatusBarColor();
        }

        @Override
        public void onExitFullscreen() {
            updateStatusBarColor();
        }

        @Override
        public void onStartScrollOffBottom() {
            mIsExitAnimationInProgress = true;
        }

        @Override
        public void onEntranceAnimationDone() {
            mTransparentView.setEnabled(true);
            mIsEntranceAnimationFinished = true;
            //aurora add
            startInteractionLoaders(mCachedCp2DataCardModel);    
//            BitmapDrawable bd;
//     	   if(mPhotoView.getDrawable() instanceof LetterTileDrawable) {
//         	   if (!mContactData.isDisplayNameFromOrganization()) {
//                    // Aurora xuyong 2016-01-13 modified for aurora 2.0 new feature start
//                    /*bd = (BitmapDrawable) getResources().getDrawable(
//                            R.drawable.person_white_540dp);*/
//                    // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature start
//                    bd = (BitmapDrawable) getResources().getDrawable(
//                            R.drawable.svg_dial_default_photo1);
//                    // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature end
//                    // Aurora xuyong 2016-01-13 modified for aurora 2.0 new feature end
//                } else {
//                    bd = (BitmapDrawable) getResources().getDrawable(
//                            R.drawable.generic_business_white_540dp);
//                }
//     	   } else {
//     		   bd = (BitmapDrawable) mPhotoView.getDrawable();
//     	   }
//            Bitmap bitmap = bd.getBitmap();
//            Bitmap newbitmap = Blur.fastblur(QuickContactActivity.this,bitmap, 12);
//            mBlurPhotoView.setImageBitmap(newbitmap);
//            mBlurPhotoView.setVisibility(View.VISIBLE);

        }

        @Override
        public void onTransparentViewHeightChange(float ratio) {
            if (mIsEntranceAnimationFinished && mIsExitAnimationFinished) {
                mWindowScrim.setAlpha((int) (0xFF * ratio));
            }
        }
        
        @Override
        public void onStartEnterDimAnimator(int duration) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(mContext,
          		  android.R.interpolator.ease_cubic);
	        ObjectAnimator o = ObjectAnimator.ofInt(mWindowScrim, "alpha", 0, (int)(255*0.3f));
	        o.setDuration(duration);
	        o.setInterpolator(interpolator);
		    o.start();
        }
        
        @Override
        public void onStartExitDimAnimator(int duration) {
        	mIsExitAnimationFinished = false;
            final Interpolator interpolator = AnimationUtils.loadInterpolator(mContext,
          		  android.R.interpolator.ease_cubic_hide);
	        ObjectAnimator o = ObjectAnimator.ofInt(mWindowScrim, "alpha", mWindowScrim.getAlpha(), 0);
	        o.setDuration(duration);
	        o.setInterpolator(interpolator);
		    o.start();
        }
        
    };


    /**
     * Data items are compared to the same mimetype based off of three qualities:
     * 1. Super primary
     * 2. Primary
     * 3. Times used
     */
    private final Comparator<DataItem> mWithinMimeTypeDataItemComparator =
            new Comparator<DataItem>() {
        @Override
        public int compare(DataItem lhs, DataItem rhs) {
            if (!lhs.getMimeType().equals(rhs.getMimeType())) {
                Log.wtf(TAG, "Comparing DataItems with different mimetypes lhs.getMimeType(): " +
                        lhs.getMimeType() + " rhs.getMimeType(): " + rhs.getMimeType());
                return 0;
            }

            if (lhs.isSuperPrimary()) {
                return -1;
            } else if (rhs.isSuperPrimary()) {
                return 1;
            } else if (lhs.isPrimary() && !rhs.isPrimary()) {
                return -1;
            } else if (!lhs.isPrimary() && rhs.isPrimary()) {
                return 1;
            } else {
                final int lhsTimesUsed =
                        lhs.getTimesUsed() == null ? 0 : lhs.getTimesUsed();
                final int rhsTimesUsed =
                        rhs.getTimesUsed() == null ? 0 : rhs.getTimesUsed();

                return rhsTimesUsed - lhsTimesUsed;
            }
        }
    };

    /**
     * Sorts among different mimetypes based off:
     * 1. Times used
     * 2. Last time used
     * 3. Statically defined
     */
    private final Comparator<List<DataItem>> mAmongstMimeTypeDataItemComparator =
            new Comparator<List<DataItem>> () {
        @Override
        public int compare(List<DataItem> lhsList, List<DataItem> rhsList) {
            DataItem lhs = lhsList.get(0);
            DataItem rhs = rhsList.get(0);
            final int lhsTimesUsed = lhs.getTimesUsed() == null ? 0 : lhs.getTimesUsed();
            final int rhsTimesUsed = rhs.getTimesUsed() == null ? 0 : rhs.getTimesUsed();
            final int timesUsedDifference = rhsTimesUsed - lhsTimesUsed;
            if (timesUsedDifference != 0) {
                return timesUsedDifference;
            }

            final long lhsLastTimeUsed =
                    lhs.getLastTimeUsed() == null ? 0 : lhs.getLastTimeUsed();
            final long rhsLastTimeUsed =
                    rhs.getLastTimeUsed() == null ? 0 : rhs.getLastTimeUsed();
            final long lastTimeUsedDifference = rhsLastTimeUsed - lhsLastTimeUsed;
            if (lastTimeUsedDifference > 0) {
                return 1;
            } else if (lastTimeUsedDifference < 0) {
                return -1;
            }

            // Times used and last time used are the same. Resort to statically defined.
            final String lhsMimeType = lhs.getMimeType();
            final String rhsMimeType = rhs.getMimeType();
            for (String mimeType : LEADING_MIMETYPES) {
                if (lhsMimeType.equals(mimeType)) {
                    return -1;
                } else if (rhsMimeType.equals(mimeType)) {
                    return 1;
                }
            }
            return 0;
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            TouchPointManager.getInstance().setPoint((int) ev.getRawX(), (int) ev.getRawY());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	ContactsApplication.sendSimContactBroad();
        Trace.beginSection("onCreate()");
        Log.d(TAG,"oncreate");
        super.onCreate(savedInstanceState);

		if(Build.VERSION.SDK_INT >= 21) {
		       getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		SystemUtils.setStatusBarBackgroundTransparent(this);

        processIntent(getIntent());

        // Show QuickContact in front of soft input
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        setContentView(R.layout.quickcontact_activity);

        mMaterialColorMapUtils = new MaterialColorMapUtils(getResources());

        mScroller = (MultiShrinkScroller) findViewById(R.id.multiscroller);
        mLargeTextView = (TextView) findViewById(R.id.large_title);
        mLargeTextView.setVisibility(View.INVISIBLE);

        mContactCard = (PhoneExpandingEntryCardView) findViewById(R.id.communication_card);
        mNoContactDetailsCard = (ExpandingEntryCardView) findViewById(R.id.no_contact_data_card);
        mRecentCard = (CallLogExpandingEntryCardView) findViewById(R.id.recent_card);
        mRecentCard.setCallLogMenuButtonListener(mCallLogMenuButtonListener);
        // Aurora xuyong 2016-01-13 added fora aurora 2.0 new feature start
        mRecentCard.setExpandButtonOnClickListener(mCallLogMenuButtonListener);
        // Aurora xuyong 2016-01-13 added fora aurora 2.0 new feature end
        mAboutCard = (AboutExpandingEntryCardView) findViewById(R.id.about_card);

        mNoContactDetailsCard.setOnClickListener(mEntryClickHandler);
        mContactCard.setOnClickListener(mEntryClickHandler);
        mContactCard.setExpandButtonText(getResources().getString(R.string.expanding_entry_card_view_see_all));
//        mContactCard.setOnCreateContextMenuListener(mEntryContextMenuListener);
        mContactCard.setOnLongClickListener(mOnLongClickListener);

        mRecentCard.setOnClickListener(mEntryClickHandler);
        mRecentCard.setTitle(getResources().getString(R.string.recent_card_title));

        mAboutCard.setOnClickListener(mEntryClickHandler);
//        mAboutCard.setOnCreateContextMenuListener(mEntryContextMenuListener);

        mPhotoView = (QuickContactImageView) findViewById(R.id.photo);
        mPhotoCover =  findViewById(R.id.photo_cover);        
//        mBlurPhotoView = (QuickContactImageView) findViewById(R.id.blur_photo);
        mSmallPhotoView = (ImageView) findViewById(R.id.small_photo);
        mTransparentView = findViewById(R.id.transparent_view);
        mTransparentView.setEnabled(false);
        if (mScroller != null) {
        	mTransparentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	if (null != mScroller) {
                        mTransparentView.setEnabled(false);
                		mScroller.scrollOffBottomAsActivity();
                	}
                }
            });
        }

        // Allow a shadow to be shown under the toolbar.
//        ViewUtil.addRectangularOutlineProvider(findViewById(R.id.toolbar_parent), getResources());

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//    	if(Build.VERSION.SDK_INT >= 21) {
////    		setActionBar(toolbar);
//    	} else {
    		setSupportActionBar(toolbar);
//    	}
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Put a TextView with a known resource id into the ActionBar. This allows us to easily
        // find the correct TextView location & size later.
        toolbar.addView(getLayoutInflater().inflate(R.layout.quickcontact_title_placeholder, null));

        mHasAlreadyBeenOpened = savedInstanceState != null;
        mIsEntranceAnimationFinished = mHasAlreadyBeenOpened;
        mWindowScrim = new ColorDrawable(SCRIM_COLOR);
        mIsNoAnim = getIntent().getBooleanExtra("noanim", false);
        if(!mIsNoAnim) {
        	mWindowScrim.setAlpha(0);
        } else {
            mTransparentView.setEnabled(true);
        	mIsEntranceAnimationFinished = true;
            final float alphaRatio = mScroller.getStartingTransparentHeightRatio();
            final int desiredAlpha = (int) (0xFF * alphaRatio);
            mWindowScrim.setAlpha(desiredAlpha);
        }
        getWindow().setBackgroundDrawable(mWindowScrim);

        mScroller.initialize(mMultiShrinkScrollerListener, mExtraMode == MODE_FULLY_EXPANDED);
        // mScroller needs to perform asynchronous measurements after initalize(), therefore
        // we can't mark this as GONE.
  
        
        mScroller.setVisibility(View.INVISIBLE);

        setHeaderNameText(R.string.missing_name);

        mSelectAccountFragmentListener= (SelectAccountDialogFragmentListener) getFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG_SELECT_ACCOUNT);
        if (mSelectAccountFragmentListener == null) {
        	Log.d(TAG, "mSelectAccountFragmentListener");
            mSelectAccountFragmentListener = new SelectAccountDialogFragmentListener();
            getFragmentManager().beginTransaction().add(0, mSelectAccountFragmentListener,
                    FRAGMENT_TAG_SELECT_ACCOUNT).commit();
            mSelectAccountFragmentListener.setRetainInstance(true);
        }
        mSelectAccountFragmentListener.setQuickContactActivity(this);
        

//        SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ true,
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!mHasAlreadyBeenOpened && !mIsNoAnim) {
//                            // The initial scrim opacity must match the scrim opacity that would be
//                            // achieved by scrolling to the starting position.
//                            final float alphaRatio = mExtraMode == MODE_FULLY_EXPANDED ?
//                                    1 : mScroller.getStartingTransparentHeightRatio();
//                            final int duration = getResources().getInteger(
//                                    android.R.integer.config_shortAnimTime);
//                            final int desiredAlpha = (int) (0xFF * alphaRatio);
////                            ObjectAnimator o = ObjectAnimator.ofInt(mWindowScrim, "alpha", 0,
////                                    desiredAlpha).setDuration(duration);
////
////                            o.start();
//                            mWindowScrim.setAlpha(desiredAlpha);
//                        }
//                    }
//                });

        if (savedInstanceState != null) {
            final int color = savedInstanceState.getInt(KEY_THEME_COLOR, 0);
            SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ false,
                    new Runnable() {
                        @Override
                        public void run() {
                            // Need to wait for the pre draw before setting the initial scroll
                            // value. Prior to pre draw all scroll values are invalid.
                            if (mHasAlreadyBeenOpened) {
                                mScroller.setVisibility(View.VISIBLE);
                                mScroller.setScroll(mScroller.getScrollNeededToBeFullScreen());
                            }
                            // Need to wait for pre draw for setting the theme color. Setting the
                            // header tint before the MultiShrinkScroller has been measured will
                            // cause incorrect tinting calculations.
//                            if (color != 0) {
//                                setThemeColor(mMaterialColorMapUtils
//                                        .calculatePrimaryAndSecondaryColor(color));
//                            }
                        }
                    });
        }

        Trace.endSection();
        
        mContext = QuickContactActivity.this;
        mPhotoSetter=new ImageViewDrawableSetter(mContext);
        Bundle extras = getIntent().getExtras();
        if (ContactsApplication.sIsAuroraPrivacySupport && null != extras) {
        	mIsPrivacyContact = extras.getBoolean("is_privacy_contact");
        	mCurrentPrivacyAccountId = AuroraPrivacyUtils.mCurrentAccountId;
            if (mIsPrivacyContact) {
                ContactsApplication.mPrivacyActivityList.add(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONTACT_EDITOR_ACTIVITY &&
                resultCode == ContactDeletionInteraction.RESULT_CODE_DELETED) {
            // The contact that we were showing has been deleted.
            finish();
        } else if (requestCode == REQUEST_CODE_CONTACT_SELECTION_ACTIVITY &&
                resultCode != RESULT_CANCELED) {
            processIntent(data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
    	Log.d(TAG,"onNewIntent");
        super.onNewIntent(intent);
        mHasAlreadyBeenOpened = true;
        mIsEntranceAnimationFinished = true;
        mHasComputedThemeColor = false;
        processIntent(intent);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //aurora delete by lgy ,crash for 4.4 
//        if (mColorFilter != null) {
//            savedInstanceState.putInt(KEY_THEME_COLOR, mColorFilter.getColor());
//        }
    }

    private void processIntent(Intent intent) {
    	Log.d(TAG,"processIntent");
        if (intent == null) {
            finish();
            return;
        }
        Uri lookupUri = intent.getData();
        simOrPhoneUri = lookupUri;
        
        mRawContactId = com.android.contacts.ContactsUtils.queryForRawContactId(getContentResolver(), Long.parseLong(lookupUri.getLastPathSegment()));
      	mLastestNumber = getLastContactNumber();

        // Check to see whether it comes from the old version.
        if (lookupUri != null && LEGACY_AUTHORITY.equals(lookupUri.getAuthority())) {
            final long rawContactId = ContentUris.parseId(lookupUri);
            lookupUri = RawContacts.getContactLookupUri(getContentResolver(),
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
        }
        mExtraMode = getIntent().getIntExtra(QuickContact.EXTRA_MODE,
                QuickContact.MODE_LARGE);
        final Uri oldLookupUri = mLookupUri;

        mLookupUri = Preconditions.checkNotNull(lookupUri, "missing lookupUri");
        mExcludeMimes = intent.getStringArrayExtra(QuickContact.EXTRA_EXCLUDE_MIMES);
        if (oldLookupUri == null) {
            mContactLoader = (ContactLoader) getLoaderManager().initLoader(
                    LOADER_CONTACT_ID, null, mLoaderContactCallbacks);
        } else if (oldLookupUri != mLookupUri) {
            // After copying a directory contact, the contact URI changes. Therefore,
            // we need to restart the loader and reload the new contact.
            for (int interactionLoaderId : mRecentLoaderIds) {
                getLoaderManager().destroyLoader(interactionLoaderId);
            }
            mContactLoader = (ContactLoader) getLoaderManager().restartLoader(
                    LOADER_CONTACT_ID, null, mLoaderContactCallbacks);
        }
//aurora delete
//        NfcHandler.register(this, mLookupUri);
    }

    private void runEntranceAnimation() {
        if (mHasAlreadyBeenOpened || mIsNoAnim) {
            mHasAlreadyBeenOpened = true;            
            //auroraadd
            final float alphaRatio = mScroller.getStartingTransparentHeightRatio();
            final int desiredAlpha = (int) (0xFF * alphaRatio);
            mWindowScrim.setAlpha(desiredAlpha);
            mTransparentView.setEnabled(true);
            return;
        }
        mHasAlreadyBeenOpened = true;
        mScroller.scrollUpForEntranceAnimation(mExtraMode != MODE_FULLY_EXPANDED);
    }

    /** Assign this string to the view if it is not empty. */
    private void setHeaderNameText(int resId) {
        if (mScroller != null) {
            mScroller.setTitle(getText(resId) == null ? null : getText(resId).toString());
        }
        getSupportActionBar().setTitle(getText(resId));
    }

    /** Assign this string to the view if it is not empty. */
    private void setHeaderNameText(String value) {
        if (!TextUtils.isEmpty(value)) {
            if (mScroller != null) {
                mScroller.setTitle(value);
            }
            getSupportActionBar().setTitle(value);
        }
    }

    /**
     * Check if the given MIME-type appears in the list of excluded MIME-types
     * that the most-recent caller requested.
     */
    private boolean isMimeExcluded(String mimeType) {
        if (mExcludeMimes == null) return false;
        for (String excludedMime : mExcludeMimes) {
            if (TextUtils.equals(excludedMime, mimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle the result from the ContactLoader
     */
    private void bindContactData(final Contact data) {
        Trace.beginSection("bindContactData");
        mContactData = data;
        
        int[] simInfo = getSimContactInfo();
        mIndicate = simInfo[0] ;
        mSimIndex = simInfo[1] ;
        invalidateOptionsMenu();

        Trace.endSection();
        Trace.beginSection("Set display photo & name");
        
        mPhotoView.setContactId(Integer.parseInt(String.valueOf(mContactData.getId())));
        mPhotoView.setIsBusiness(mContactData.isDisplayNameFromOrganization());    
        mPhotoSetter.setupContactPhoto(data, mPhotoView);        
        if(mPhotoSetter.mIsUseCover) {
        	mPhotoCover.setVisibility(View.VISIBLE);
        } else {
        	mPhotoCover.setVisibility(View.GONE);
        }
//        mBlurPhotoView.setVisibility(View.GONE);
//        mSmallPhotoView.setImageBitmap(ContactDetailDisplayUtils.toRoundBitmap(bitmap));
        extractAndApplyTintFromPhotoViewAsynchronously();
        analyzeWhitenessOfPhotoAsynchronously();
        mName = ContactDisplayUtils.getDisplayName(this, data).toString();
        setHeaderNameText(mName);

        Trace.endSection();

        mEntriesAndActionsTask = new AsyncTask<Void, Void, Cp2DataCardModel>() {

            @Override
            protected Cp2DataCardModel doInBackground(
                    Void... params) {
                return generateDataModelFromContact(data);
            }

            @Override
            protected void onPostExecute(Cp2DataCardModel cardDataModel) {
                super.onPostExecute(cardDataModel);
                // Check that original AsyncTask parameters are still valid and the activity
                // is still running before binding to UI. A new intent could invalidate
                // the results, for example.
                if (data == mContactData && !isCancelled()) {
                    bindDataToCards(cardDataModel);
                    showActivity();
                }
            }
        };
        mEntriesAndActionsTask.execute();
    }

    private void bindDataToCards(Cp2DataCardModel cp2DataCardModel) {
//        startInteractionLoaders(cp2DataCardModel);
        populateContactAndAboutCard(cp2DataCardModel);
    }

    private void startInteractionLoaders(Cp2DataCardModel cp2DataCardModel) {
    	Log.d(TAG,"startInteractionLoaders");
        final Map<String, List<DataItem>> dataItemsMap = cp2DataCardModel.dataItemsMap;
        final List<DataItem> phoneDataItems = dataItemsMap.get(Phone.CONTENT_ITEM_TYPE);
//        if (phoneDataItems != null && phoneDataItems.size() == 1) {
//            mOnlyOnePhoneNumber = true;
//        } 
        String[] phoneNumbers = null;
        if (phoneDataItems != null) {
            phoneNumbers = new String[phoneDataItems.size()];
            for (int i = 0; i < phoneDataItems.size(); ++i) {
                phoneNumbers[i] = ((PhoneDataItem) phoneDataItems.get(i)).getNumber();
            }
        }
        final Bundle phonesExtraBundle = new Bundle();
        phonesExtraBundle.putStringArray(KEY_LOADER_EXTRA_PHONES, phoneNumbers);
        // Aurora xuyong 2016-01-05 deleted for bug #18275 start
        /*Trace.beginSection("start sms loader");
        getLoaderManager().initLoader(
                LOADER_SMS_ID,
                phonesExtraBundle,
                mLoaderInteractionsCallbacks);
        Trace.endSection();*/
        // Aurora xuyong 2016-01-05 deleted for bug #18275 end
        Log.d(TAG,"start call log loader");
        getLoaderManager().initLoader(
                LOADER_CALL_LOG_ID,
                phonesExtraBundle,
                mLoaderInteractionsCallbacks);
        Trace.endSection();


//        Trace.beginSection("start calendar loader");
//        final List<DataItem> emailDataItems = dataItemsMap.get(Email.CONTENT_ITEM_TYPE);
//        if (emailDataItems != null && emailDataItems.size() == 1) {
//            mOnlyOneEmail = true;
//        }
//        String[] emailAddresses = null;
//        if (emailDataItems != null) {
//            emailAddresses = new String[emailDataItems.size()];
//            for (int i = 0; i < emailDataItems.size(); ++i) {
//                emailAddresses[i] = ((EmailDataItem) emailDataItems.get(i)).getAddress();
//            }
//        }
//        final Bundle emailsExtraBundle = new Bundle();
//        emailsExtraBundle.putStringArray(KEY_LOADER_EXTRA_EMAILS, emailAddresses);
//        getLoaderManager().initLoader(
//                LOADER_CALENDAR_ID,
//                emailsExtraBundle,
//                mLoaderInteractionsCallbacks);
        Trace.endSection();
    }

    private void showActivity() {
    	   Log.e(TAG, "showActivity");   
        if (mScroller != null) {
            mScroller.setVisibility(View.VISIBLE);
            SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ false,
                    new Runnable() {
                        @Override
                        public void run() {
                            runEntranceAnimation();
                        }
                    });
        }
    }

    private List<List<Entry>> buildAboutCardEntries(Map<String, List<DataItem>> dataItemsMap) {
        final List<List<Entry>> aboutCardEntries = new ArrayList<>();
        for (String mimetype : SORTED_ABOUT_CARD_MIMETYPES) {
            final List<DataItem> mimeTypeItems = dataItemsMap.get(mimetype);
            Log.e(TAG, "buildAboutCardEntries: + mimeTypeItems = " + mimeTypeItems + " , mimetype = " + mimetype);            
            if (mimeTypeItems == null) {            	
                continue;
            }
            // Set aboutCardTitleOut = null, since SORTED_ABOUT_CARD_MIMETYPES doesn't contain
            // the name mimetype.
            final List<Entry> aboutEntries = dataItemsToEntries(mimeTypeItems,
                    /* aboutCardTitleOut = */ null);
            if (aboutEntries.size() > 0) {
                aboutCardEntries.add(aboutEntries);
            }
        }
        //aurora add by liguangyu start
        final List<Entry> ringtoneEntries = getRingtoneEntries();
        if (ringtoneEntries.size() > 0) {
            aboutCardEntries.add(ringtoneEntries);
        }
        //aurora add by liguangyu end
        return aboutCardEntries;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // If returning from a launched activity, repopulate the contact and about card
        if (mHasIntentLaunched) {
         	mLastestNumber = getLastContactNumber();
            mHasIntentLaunched = false;
            populateContactAndAboutCard(mCachedCp2DataCardModel);
        }
        
        Log.d(TAG,"onResume:"+mHasDialNumber);
        if(mHasDialNumber){
        	mHasDialNumber=false;
        	 for (int interactionLoaderId : mRecentLoaderIds) {
                 getLoaderManager().destroyLoader(interactionLoaderId);
             }
             mContactLoader = (ContactLoader) getLoaderManager().restartLoader(
                     LOADER_CONTACT_ID, null, mLoaderContactCallbacks);
             
             if(mCachedCp2DataCardModel!=null){
            	 startInteractionLoaders(mCachedCp2DataCardModel);
             }
        }
        SystemUtils.setStatusBarBackgroundTransparent(this);
    }

    private void populateContactAndAboutCard(Cp2DataCardModel cp2DataCardModel) {
        mCachedCp2DataCardModel = cp2DataCardModel;
        if (mHasIntentLaunched || cp2DataCardModel == null) {
            return;
        }
        Trace.beginSection("bind contact card");        

        final List<List<Entry>> contactCardEntries = cp2DataCardModel.contactCardEntries;
        final List<List<Entry>> aboutCardEntries = cp2DataCardModel.aboutCardEntries;
        final String customAboutCardName = cp2DataCardModel.customAboutCardName;

        if (contactCardEntries.size() > 0) {
            mContactCard.initialize(contactCardEntries,
                    /* numInitialVisibleEntries = */ MIN_NUM_CONTACT_ENTRIES_SHOWN,
                    /* isExpanded = */ mContactCard.isExpanded(),
                    /* isAlwaysExpanded = */ true,
                    mExpandingEntryCardViewListener,
                    mScroller);
            mContactCard.setVisibility(View.VISIBLE);
        } else {
            mContactCard.setVisibility(View.GONE);
        }
        Trace.endSection();

        Trace.beginSection("bind about card");
        // Phonetic name is not a data item, so the entry needs to be created separately
        final String phoneticName = mContactData.getPhoneticName();
        if (!TextUtils.isEmpty(phoneticName)) {
            Entry phoneticEntry = new Entry(/* viewId = */ -1,
                    /* icon = */ null,
                    getResources().getString(R.string.name_phonetic),
                    phoneticName,
                    /* text = */ null,
                    /* primaryContentDescription = */ null,
                    /* intent = */ null,
                    /* alternateIcon = */ null,
                    /* alternateIntent = */ null,
                    /* alternateContentDescription = */ null,
                    /* shouldApplyColor = */ false,
                    /* isEditable = */ false,
                    /* EntryContextMenuInfo = */ new EntryContextMenuInfo(phoneticName,
                            getResources().getString(R.string.name_phonetic),
                            /* mimeType = */ null, /* id = */ -1, /* isPrimary = */ false),
                    /* thirdIcon = */ null,
                    /* thirdIntent = */ null,
                    /* thirdContentDescription = */ null,
                    /* iconResourceId = */ 0);
            List<Entry> phoneticList = new ArrayList<>();
            phoneticList.add(phoneticEntry);
            // Phonetic name comes after nickname. Check to see if the first entry type is nickname
            if (aboutCardEntries.size() > 0 && aboutCardEntries.get(0).get(0).getHeader().equals(
                    getResources().getString(R.string.header_nickname_entry))) {
                aboutCardEntries.add(1, phoneticList);
            } else {
                aboutCardEntries.add(0, phoneticList);
            }
        }

        if (!TextUtils.isEmpty(customAboutCardName)) {
            mAboutCard.setTitle(customAboutCardName);
        }

        if (aboutCardEntries.size() > 0) {
            mAboutCard.initialize(aboutCardEntries,
                    /* numInitialVisibleEntries = */ 1,
                    /* isExpanded = */ true,
                    /* isAlwaysExpanded = */ true,
                    mExpandingEntryCardViewListener,
                    mScroller);
        }

        if (contactCardEntries.size() == 0 && aboutCardEntries.size() == 0) {
            initializeNoContactDetailCard();
        } else {
            mNoContactDetailsCard.setVisibility(View.GONE);
        }

        // If the Recent card is already initialized (all recent data is loaded), show the About
        // card if it has entries. Otherwise About card visibility will be set in bindRecentData()
//        if (isAllRecentDataLoaded() && aboutCardEntries.size() > 0) {
        if (aboutCardEntries.size() > 0) {
            mAboutCard.setVisibility(View.VISIBLE);
        }
        Trace.endSection();
    }

    /**
     * Create a card that shows "Add email" and "Add phone number" entries in grey.
     */
    private void initializeNoContactDetailCard() {
        final Drawable phoneIcon = getResources().getDrawable(
                R.drawable.ic_phone_24dp).mutate();
        final Entry phonePromptEntry = new Entry(CARD_ENTRY_ID_EDIT_CONTACT,
                phoneIcon, getString(R.string.quickcontact_add_phone_number),
                /* subHeader = */ null, /* text = */ null, /* primaryContentDescription = */ null,
                getEditContactIntent(),
                /* alternateIcon = */ null, /* alternateIntent = */ null,
                /* alternateContentDescription = */ null, /* shouldApplyColor = */ false,
                /* isEditable = */ false, /* EntryContextMenuInfo = */ null,
                /* thirdIcon = */ null, /* thirdIntent = */ null,
                /* thirdContentDescription = */ null, R.drawable.ic_phone_24dp);

        final Drawable emailIcon = getResources().getDrawable(
                R.drawable.ic_email_24dp).mutate();
        final Entry emailPromptEntry = new Entry(CARD_ENTRY_ID_EDIT_CONTACT,
                emailIcon, getString(R.string.quickcontact_add_email), /* subHeader = */ null,
                /* text = */ null, /* primaryContentDescription = */ null,
                getEditContactIntent(), /* alternateIcon = */ null,
                /* alternateIntent = */ null, /* alternateContentDescription = */ null,
                /* shouldApplyColor = */ false, /* isEditable = */ false,
                /* EntryContextMenuInfo = */ null, /* thirdIcon = */ null,
                /* thirdIntent = */ null, /* thirdContentDescription = */ null,
                R.drawable.ic_email_24dp);

        final List<List<Entry>> promptEntries = new ArrayList<>();
        promptEntries.add(new ArrayList<Entry>(1));
        promptEntries.add(new ArrayList<Entry>(1));
        promptEntries.get(0).add(phonePromptEntry);
        promptEntries.get(1).add(emailPromptEntry);

        final int subHeaderTextColor = getResources().getColor(
                R.color.quickcontact_entry_sub_header_text_color);
        final PorterDuffColorFilter greyColorFilter =
                new PorterDuffColorFilter(subHeaderTextColor, PorterDuff.Mode.SRC_ATOP);
        mNoContactDetailsCard.initialize(promptEntries, 2, /* isExpanded = */ true,
                /* isAlwaysExpanded = */ true, mExpandingEntryCardViewListener, mScroller);
        mNoContactDetailsCard.setVisibility(View.VISIBLE);
        mNoContactDetailsCard.setEntryHeaderColor(subHeaderTextColor);
        mNoContactDetailsCard.setColorAndFilter(subHeaderTextColor, greyColorFilter);
    }

    /**
     * Builds the {@link DataItem}s Map out of the Contact.
     * @param data The contact to build the data from.
     * @return A pair containing a list of data items sorted within mimetype and sorted
     *  amongst mimetype. The map goes from mimetype string to the sorted list of data items within
     *  mimetype
     */
    private Cp2DataCardModel generateDataModelFromContact(
            Contact data) {
        Trace.beginSection("Build data items map");

        final Map<String, List<DataItem>> dataItemsMap = new HashMap<>();

        final ResolveCache cache = ResolveCache.getInstance(this);
        mGroupDataKind = null;

        for (RawContact rawContact : data.getRawContacts()) {
            for (DataItem dataItem : rawContact.getDataItems()) {
                dataItem.setRawContactId(rawContact.getId());

                final String mimeType = dataItem.getMimeType();
                Log.i(TAG, "lgy mimeType = " + mimeType);
                if (mimeType == null) continue;

                final AccountType accountType = rawContact.getAccountType(this);
                final DataKind dataKind;
                //aurora add by liguangyu start
                if(GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    Log.i(TAG, "lgy GroupMembership = ");
                    if(mGroupDataKind != null) {
                    	getDataKindGroupMembership((GroupMembershipDataItem)dataItem); 
                    	continue;
                    } else {
                   	 dataKind = getDataKindGroupMembership((GroupMembershipDataItem)dataItem); 
                    }
                } else if(Event.CONTENT_ITEM_TYPE.equals(mimeType)) {
                	dataKind = getDataKindEvent((EventDataItem)dataItem); 
                } else {
                	dataKind = AccountTypeManager.getInstance(this)
                            .getKindOrFallback(accountType, mimeType);
                }
                 //aurora add by liguangyu end
                Log.i(TAG, "lgy dataKind = " + dataKind);
                if (dataKind == null) continue;

                dataItem.setDataKind(dataKind);

                final boolean hasData = !TextUtils.isEmpty(dataItem.buildDataString(this,
                        dataKind));

                Log.i(TAG, "lgy hasData = " + hasData);
                if (isMimeExcluded(mimeType) || !hasData) continue;

                List<DataItem> dataItemListByType = dataItemsMap.get(mimeType);
                if (dataItemListByType == null) {
                    dataItemListByType = new ArrayList<>();
                    dataItemsMap.put(mimeType, dataItemListByType);
                }         
                Log.i(TAG, "lgy dataItemListByType add ");
                dataItemListByType.add(dataItem);
            }
        }
        Trace.endSection();

        Trace.beginSection("sort within mimetypes");
        /*
         * Sorting is a multi part step. The end result is to a have a sorted list of the most
         * used data items, one per mimetype. Then, within each mimetype, the list of data items
         * for that type is also sorted, based off of {super primary, primary, times used} in that
         * order.
         */
        final List<List<DataItem>> dataItemsList = new ArrayList<>();
        for (List<DataItem> mimeTypeDataItems : dataItemsMap.values()) {
            // Remove duplicate data items
            Collapser.collapseList(mimeTypeDataItems, this);
            // Sort within mimetype
            Collections.sort(mimeTypeDataItems, mWithinMimeTypeDataItemComparator);
            // Add to the list of data item lists
            dataItemsList.add(mimeTypeDataItems);
        }
        Trace.endSection();

        Trace.beginSection("sort amongst mimetypes");
        // Sort amongst mimetypes to bubble up the top data items for the contact card
        Collections.sort(dataItemsList, mAmongstMimeTypeDataItemComparator);
        Trace.endSection();

        Trace.beginSection("cp2 data items to entries");

        final List<List<Entry>> contactCardEntries = new ArrayList<>();
        final List<List<Entry>> aboutCardEntries = buildAboutCardEntries(dataItemsMap);
        final MutableString aboutCardName = new MutableString();
        
        final List<DataItem> phoneDataItems = dataItemsMap.get(Phone.CONTENT_ITEM_TYPE);
        if (phoneDataItems != null && phoneDataItems.size() == 1) {
            mOnlyOnePhoneNumber = true;
        } 

        for (int i = 0; i < dataItemsList.size(); ++i) {
            final List<DataItem> dataItemsByMimeType = dataItemsList.get(i);
            final DataItem topDataItem = dataItemsByMimeType.get(0);
            if (SORTED_ABOUT_CARD_MIMETYPES.contains(topDataItem.getMimeType())) {
                // About card mimetypes are built in buildAboutCardEntries, skip here
                continue;
            } else {
                List<Entry> contactEntries = dataItemsToEntries(dataItemsList.get(i),
                        aboutCardName);
                if (contactEntries.size() > 0) {
                    contactCardEntries.add(contactEntries);
                }
            }
        }

        Trace.endSection();

        final Cp2DataCardModel dataModel = new Cp2DataCardModel();
        dataModel.customAboutCardName = aboutCardName.value;
        dataModel.aboutCardEntries = aboutCardEntries;
        dataModel.contactCardEntries = contactCardEntries;
        dataModel.dataItemsMap = dataItemsMap;
        return dataModel;
    }

    /**
     * Class used to hold the About card and Contact cards' data model that gets generated
     * on a background thread. All data is from CP2.
     */
    private static class Cp2DataCardModel {
        /**
         * A map between a mimetype string and the corresponding list of data items. The data items
         * are in sorted order using mWithinMimeTypeDataItemComparator.
         */
        public Map<String, List<DataItem>> dataItemsMap;
        public List<List<Entry>> aboutCardEntries;
        public List<List<Entry>> contactCardEntries;
        public String customAboutCardName;
    }

    private static class MutableString {
        public String value;
    }

    /**
     * Converts a {@link DataItem} into an {@link ExpandingEntryCardView.Entry} for display.
     * If the {@link ExpandingEntryCardView.Entry} has no visual elements, null is returned.
     *
     * This runs on a background thread. This is set as static to avoid accidentally adding
     * additional dependencies on unsafe things (like the Activity).
     *
     * @param dataItem The {@link DataItem} to convert.
     * @return The {@link ExpandingEntryCardView.Entry}, or null if no visual elements are present.
     */
    private Entry dataItemToEntry(DataItem dataItem,
            Context context, Contact contactData,
            final MutableString aboutCardName) {
        Drawable icon = null;
        String header = null;
        String subHeader = null;
        Drawable subHeaderIcon = null;
        String text = null;
        Drawable textIcon = null;
        StringBuilder primaryContentDescription = new StringBuilder();
        Intent intent = null;
        boolean shouldApplyColor = false;
        Drawable alternateIcon = null;
        Intent alternateIntent = null;
        StringBuilder alternateContentDescription = new StringBuilder();
        final boolean isEditable = false;
        EntryContextMenuInfo entryContextMenuInfo = null;
        Drawable thirdIcon = null;
        Intent thirdIntent = null;
        String thirdContentDescription = null;
        int iconResourceId = 0;

        context = context.getApplicationContext();
        final Resources res = context.getResources();
        DataKind kind = dataItem.getDataKind();

        if (dataItem instanceof ImDataItem) {
            final ImDataItem im = (ImDataItem) dataItem;
            intent = ContactsUtils.buildImIntent(context, im).first;
            final boolean isEmail = im.isCreatedFromEmail();
            final int protocol;
            if (!im.isProtocolValid()) {
                protocol = Im.PROTOCOL_CUSTOM;
            } else {
                protocol = isEmail ? Im.PROTOCOL_GOOGLE_TALK : im.getProtocol();
            }
            if (protocol == Im.PROTOCOL_CUSTOM) {
                // If the protocol is custom, display the "IM" entry header as well to distinguish
                // this entry from other ones
                header = res.getString(R.string.header_im_entry);
                subHeader = Im.getProtocolLabel(res, protocol,
                        im.getCustomProtocol()).toString();
                text = im.getData();
            } else {
                header = Im.getProtocolLabel(res, protocol,
                        im.getCustomProtocol()).toString();
                subHeader = im.getData();
            }
            entryContextMenuInfo = new EntryContextMenuInfo(im.getData(), header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
        } else if (dataItem instanceof OrganizationDataItem) {
            final OrganizationDataItem organization = (OrganizationDataItem) dataItem;
            header = res.getString(R.string.header_organization_entry);
            //aurora
//            subHeader = organization.getCompany();
            text = organization.getCompany();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
//            text = organization.getTitle();
        } else if (dataItem instanceof NicknameDataItem) {
            final NicknameDataItem nickname = (NicknameDataItem) dataItem;
            // Build nickname entries
            final boolean isNameRawContact =
                (contactData.getNameRawContactId() == dataItem.getRawContactId());

            final boolean duplicatesTitle =
                isNameRawContact
                && contactData.getDisplayNameSource() == DisplayNameSources.NICKNAME;

            if (!duplicatesTitle) {
                header = res.getString(R.string.header_nickname_entry);
                subHeader = nickname.getName();
                entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                        dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
            }
        } else if (dataItem instanceof NoteDataItem) {
            final NoteDataItem note = (NoteDataItem) dataItem;
            header = res.getString(R.string.header_note_entry);
            //aurora
//            subHeader = note.getNote();
            text = note.getNote();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
        } else if (dataItem instanceof WebsiteDataItem) {
            final WebsiteDataItem website = (WebsiteDataItem) dataItem;
            header = res.getString(R.string.header_website_entry);
            subHeader = website.getUrl();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
            try {
                final WebAddress webAddress = new WebAddress(website.buildDataString(context,
                        kind));
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webAddress.toString()));
            } catch (final ParseException e) {
                Log.e(TAG, "Couldn't parse website: " + website.buildDataString(context, kind));
            }
        } else if (dataItem instanceof EventDataItem) {
            final EventDataItem event = (EventDataItem) dataItem;
            final String dataString = event.buildDataString(context, kind);
            final Calendar cal = DateUtils.parseDate(dataString, false);
            if (cal != null) {
                final Date nextAnniversary =
                        DateUtils.getNextAnnualDate(cal);
                final Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, nextAnniversary.getTime());
                intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            }
//            header = event.getLabel();
            if (event.hasKindTypeColumn(kind)) {
            	header = Event.getTypeLabel(res, event.getKindTypeColumn(kind),
                        event.getLabel()).toString();
            }
            text = DateUtils.formatDate(context, dataString);
            entryContextMenuInfo = new EntryContextMenuInfo(text, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
        } else if (dataItem instanceof RelationDataItem) {
            final RelationDataItem relation = (RelationDataItem) dataItem;
            final String dataString = relation.buildDataString(context, kind);
            if (!TextUtils.isEmpty(dataString)) {
                intent = new Intent(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY, dataString);
                intent.setType(Contacts.CONTENT_TYPE);
            }
            header = res.getString(R.string.header_relation_entry);
            subHeader = relation.getName();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
            if (relation.hasKindTypeColumn(kind)) {
                text = Relation.getTypeLabel(res,
                        relation.getKindTypeColumn(kind),
                        relation.getLabel()).toString();
            }
        } else if (dataItem instanceof PhoneDataItem) {
            final PhoneDataItem phone = (PhoneDataItem) dataItem;
            if (!TextUtils.isEmpty(phone.getNumber())) {
                primaryContentDescription.append(res.getString(R.string.call_other)).append(" ");
                header = sBidiFormatter.unicodeWrap(phone.buildDataString(context, kind),
                        TextDirectionHeuristics.LTR);
                entryContextMenuInfo = new EntryContextMenuInfo(header,
                        res.getString(R.string.phoneLabelsGroup), dataItem.getMimeType(),
                        dataItem.getId(), dataItem.isSuperPrimary());
                if (phone.hasKindTypeColumn(kind)) {
                    text = Phone.getTypeLabel(res, phone.getKindTypeColumn(kind),
                            phone.getLabel()).toString();                    
//                    String area = NumberAreaUtil.getInstance(context).getNumAreaFromLocal(context, header, false);   
                    String area =  YuloreUtils.getInstance(context).getAreaLocal(header);
                    if(!TextUtils.isEmpty(area) &&  !area.equalsIgnoreCase("null")) {
                    	text += "  " + area;
                        primaryContentDescription.append(text).append(" ");
                    }
                }
                primaryContentDescription.append(header);
                icon = res.getDrawable(R.drawable.ic_phone_24dp);
                iconResourceId = R.drawable.ic_phone_24dp;
                if (PhoneCapabilityTester.isPhone(context)) {
                    intent = CallUtil.getCallIntent(phone.getNumber());
                }
                alternateIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts(ContactsUtils.SCHEME_SMSTO, phone.getNumber(), null));

                alternateIcon = res.getDrawable(R.drawable.ic_message_24dp);
                alternateContentDescription.append(res.getString(R.string.sms_custom, header));
                
                if(!mOnlyOnePhoneNumber) { 
                    Log.d(TAG, "!mOnlyOnePhoneNumber header = " + header);
                    String tempNumber = header.replaceAll(" ", "");
                	if(tempNumber.equalsIgnoreCase(mLastestNumber)) {
                		header = header + "  " + res.getString(R.string.aurora_recent_call, header);
                	}
                }

                // Add video call button if supported
//                if (CallUtil.isVideoEnabled(context)) {
//                    thirdIcon = res.getDrawable(R.drawable.ic_videocam);
//                    thirdIntent = CallUtil.getVideoCallIntent(phone.getNumber(),
//                            CALL_ORIGIN_QUICK_CONTACTS_ACTIVITY);
//                    thirdContentDescription =
//                            res.getString(R.string.description_video_call);
//                }
            }
        } else if (dataItem instanceof EmailDataItem) {
            final EmailDataItem email = (EmailDataItem) dataItem;
            final String address = email.getData();
            if (!TextUtils.isEmpty(address)) {
                primaryContentDescription.append(res.getString(R.string.email_other)).append(" ");
                final Uri mailUri = Uri.fromParts(ContactsUtils.SCHEME_MAILTO, address, null);
                intent = new Intent(Intent.ACTION_SENDTO, mailUri);
                header = email.getAddress();
                entryContextMenuInfo = new EntryContextMenuInfo(header,
                        res.getString(R.string.emailLabelsGroup), dataItem.getMimeType(),
                        dataItem.getId(), dataItem.isSuperPrimary());
                if (email.hasKindTypeColumn(kind)) {
                    text = Email.getTypeLabel(res, email.getKindTypeColumn(kind),
                            email.getLabel()).toString();
                    primaryContentDescription.append(text).append(" ");
                }
                primaryContentDescription.append(header);
                icon = res.getDrawable(R.drawable.ic_email_24dp);
                iconResourceId = R.drawable.ic_email_24dp;
                shouldApplyColor = true;
            }
        } else if (dataItem instanceof StructuredPostalDataItem) {
            StructuredPostalDataItem postal = (StructuredPostalDataItem) dataItem;
            final String postalAddress = postal.getFormattedAddress();
            if (!TextUtils.isEmpty(postalAddress)) {
                primaryContentDescription.append(res.getString(R.string.map_other)).append(" ");
                intent = StructuredPostalUtils.getViewPostalAddressIntent(postalAddress);
                header = postal.getFormattedAddress();
                entryContextMenuInfo = new EntryContextMenuInfo(header,
                        res.getString(R.string.postalLabelsGroup), dataItem.getMimeType(),
                        dataItem.getId(), dataItem.isSuperPrimary());
                if (postal.hasKindTypeColumn(kind)) {
                    text = StructuredPostal.getTypeLabel(res,
                            postal.getKindTypeColumn(kind), postal.getLabel()).toString();
                    primaryContentDescription.append(text).append(" ");
                }
                primaryContentDescription.append(header);
//                alternateIntent =
//                        StructuredPostalUtils.getViewPostalAddressDirectionsIntent(postalAddress);
//                alternateIcon = res.getDrawable(R.drawable.ic_directions_24dp);
//                alternateContentDescription.append(res.getString(
//                        R.string.content_description_directions)).append(" ").append(header);
                icon = res.getDrawable(R.drawable.ic_place_24dp);
                iconResourceId = R.drawable.ic_place_24dp;
                shouldApplyColor = true;
            }
        } else if (dataItem instanceof SipAddressDataItem) {
            if (PhoneCapabilityTester.isSipPhone(context)) {
                final SipAddressDataItem sip = (SipAddressDataItem) dataItem;
                final String address = sip.getSipAddress();
                if (!TextUtils.isEmpty(address)) {
                    primaryContentDescription.append(res.getString(R.string.call_other)).append(
                            " ");
                    final Uri callUri = Uri.fromParts(PhoneAccount.SCHEME_SIP, address, null);
                    intent = CallUtil.getCallIntent(callUri);
                    header = address;
                    entryContextMenuInfo = new EntryContextMenuInfo(header,
                            res.getString(R.string.phoneLabelsGroup), dataItem.getMimeType(),
                            dataItem.getId(), dataItem.isSuperPrimary());
                    if (sip.hasKindTypeColumn(kind)) {
                        text = SipAddress.getTypeLabel(res,
                                sip.getKindTypeColumn(kind), sip.getLabel()).toString();
                        primaryContentDescription.append(text).append(" ");
                    }
                    primaryContentDescription.append(header);
                    icon = res.getDrawable(R.drawable.ic_dialer_sip_black_24dp);
                    iconResourceId = R.drawable.ic_dialer_sip_black_24dp;
                    shouldApplyColor = true;
                }
            }
        } else if (dataItem instanceof StructuredNameDataItem) {
            final String givenName = ((StructuredNameDataItem) dataItem).getGivenName();
//            if (!TextUtils.isEmpty(givenName)) {
//                aboutCardName.value = res.getString(R.string.about_card_title) +
//                        " " + givenName;
//            } else {
                aboutCardName.value = res.getString(R.string.about_card_title);
//            }
        } else {
            // Custom DataItem
            header = dataItem.buildDataStringForDisplay(context, kind);
            text = kind.typeColumn;
            intent = new Intent(Intent.ACTION_VIEW);
            final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, dataItem.getId());
            intent.setDataAndType(uri, dataItem.getMimeType());

            if (intent != null) {
                final String mimetype = intent.getType();

                // Attempt to use known icons for known 3p types. Otherwise default to ResolveCache
                switch (mimetype) {
                    case MIMETYPE_GPLUS_PROFILE:
                        if (INTENT_DATA_GPLUS_PROFILE_ADD_TO_CIRCLE.equals(
                                intent.getDataString())) {
                            icon = res.getDrawable(
                                    R.drawable.ic_add_to_circles_black_24);
                            iconResourceId = R.drawable.ic_add_to_circles_black_24;
                        } else {
                            icon = res.getDrawable(R.drawable.ic_google_plus_24dp);
                            iconResourceId = R.drawable.ic_google_plus_24dp;
                        }
                        break;
                    case MIMETYPE_HANGOUTS:
                        if (INTENT_DATA_HANGOUTS_VIDEO.equals(intent.getDataString())) {
                            icon = res.getDrawable(R.drawable.ic_hangout_video_24dp);
                            iconResourceId = R.drawable.ic_hangout_video_24dp;
                        } else {
                            icon = res.getDrawable(R.drawable.ic_hangout_24dp);
                            iconResourceId = R.drawable.ic_hangout_24dp;
                        }
                        break;
                    default:
                        entryContextMenuInfo = new EntryContextMenuInfo(header, mimetype,
                                dataItem.getMimeType(), dataItem.getId(),
                                dataItem.isSuperPrimary());
                        icon = ResolveCache.getInstance(context).getIcon(
                                dataItem.getMimeType(), intent);
                        // Call mutate to create a new Drawable.ConstantState for color filtering
                        if (icon != null) {
                            icon.mutate();
                        }
                        shouldApplyColor = false;
                }
            }
        }

        if (intent != null) {
            // Do not set the intent is there are no resolves
            if (!PhoneCapabilityTester.isIntentRegistered(context, intent)) {
                intent = null;
            }
        }

        if (alternateIntent != null) {
            // Do not set the alternate intent is there are no resolves
            if (!PhoneCapabilityTester.isIntentRegistered(context, alternateIntent)) {
                alternateIntent = null;
            } else if (TextUtils.isEmpty(alternateContentDescription)) {
                // Attempt to use package manager to find a suitable content description if needed
                alternateContentDescription.append(getIntentResolveLabel(alternateIntent, context));
            }
        }

        // If the Entry has no visual elements, return null
        if (icon == null && TextUtils.isEmpty(header) && TextUtils.isEmpty(subHeader) &&
                subHeaderIcon == null && TextUtils.isEmpty(text) && textIcon == null) {
            return null;
        }

        // Ignore dataIds from the Me profile.
        final int dataId = dataItem.getId() > Integer.MAX_VALUE ?
                -1 : (int) dataItem.getId();

        return new Entry(dataId, icon, header, subHeader, subHeaderIcon, text, textIcon,
                primaryContentDescription.toString(), intent, alternateIcon, alternateIntent,
                alternateContentDescription.toString(), shouldApplyColor, isEditable,
                entryContextMenuInfo, thirdIcon, thirdIntent, thirdContentDescription,
                iconResourceId);
    }

    private List<Entry> dataItemsToEntries(List<DataItem> dataItems,
            MutableString aboutCardTitleOut) {
        final List<Entry> entries = new ArrayList<>();
                                        
        for (DataItem dataItem : dataItems) {
            Log.e(TAG, "dataItem=" + dataItem);        
            final Entry entry = dataItemToEntry(dataItem, this, mContactData, aboutCardTitleOut);
            Log.e(TAG, "entry =" + entry);        
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private static String getIntentResolveLabel(Intent intent, Context context) {
        final List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        // Pick first match, otherwise best found
        ResolveInfo bestResolve = null;
        final int size = matches.size();
        if (size == 1) {
            bestResolve = matches.get(0);
        } else if (size > 1) {
            bestResolve = ResolveCache.getInstance(context).getBestResolve(intent, matches);
        }

        if (bestResolve == null) {
            return null;
        }

        return String.valueOf(bestResolve.loadLabel(context.getPackageManager()));
    }

    /**
     * Asynchronously extract the most vibrant color from the PhotoView. Once extracted,
     * apply this tint to {@link MultiShrinkScroller}. This operation takes about 20-30ms
     * on a Nexus 5.
     */
    private void extractAndApplyTintFromPhotoViewAsynchronously() {
        if (mScroller == null) {
            return;
        }
        final Drawable imageViewDrawable = mPhotoView.getDrawable();
        new AsyncTask<Void, Void, MaterialPalette>() {
            @Override
            protected MaterialPalette doInBackground(Void... params) {

//                if (imageViewDrawable instanceof BitmapDrawable
//                        && mContactData.getThumbnailPhotoBinaryData() != null
//                        && mContactData.getThumbnailPhotoBinaryData().length > 0) {
//                    // Perform the color analysis on the thumbnail instead of the full sized
//                    // image, so that our results will be as similar as possible to the Bugle
//                    // app.
//                    final Bitmap bitmap = BitmapFactory.decodeByteArray(
//                            mContactData.getThumbnailPhotoBinaryData(), 0,
//                            mContactData.getThumbnailPhotoBinaryData().length);
//                    try {
//                        final int primaryColor = colorFromBitmap(bitmap);
//                        if (primaryColor != 0) {
//                            return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(
//                                    primaryColor);
//                        }
//                    } finally {
//                        bitmap.recycle();
//                    }
//                }
//                if (imageViewDrawable instanceof LetterTileDrawable) {
//                    final int primaryColor = ((LetterTileDrawable) imageViewDrawable).getColor();
//                    return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(primaryColor);
//                }
//                return MaterialColorMapUtils.getDefaultPrimaryAndSecondaryColors(getResources());
            	
            	final int primaryColor = 0x0EBC7D;       
//              	final int primaryColor = 0x0FD28C;       
                  return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(
                          primaryColor);
              
            }

            @Override
            protected void onPostExecute(MaterialPalette palette) {
                super.onPostExecute(palette);
                if (mHasComputedThemeColor) {
                    // If we had previously computed a theme color from the contact photo,
                    // then do not update the theme color. Changing the theme color several
                    // seconds after QC has started, as a result of an updated/upgraded photo,
                    // is a jarring experience. On the other hand, changing the theme color after
                    // a rotation or onNewIntent() is perfectly fine.
                    return;
                }
                // Check that the Photo has not changed. If it has changed, the new tint
                // color needs to be extracted
                if (imageViewDrawable == mPhotoView.getDrawable()) {
                    mHasComputedThemeColor = true;
//                    setThemeColor(palette);
                }
            }
        }.execute();
    }

    /**
     * Examine how many white pixels are in the bitmap in order to determine whether or not
     * we need gradient overlays on top of the image.
     */
    private void analyzeWhitenessOfPhotoAsynchronously() {
//        final Drawable imageViewDrawable = mPhotoView.getDrawable();
//        new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                if (imageViewDrawable instanceof BitmapDrawable) {
//                    final Bitmap bitmap = ((BitmapDrawable) imageViewDrawable).getBitmap();
//                    return WhitenessUtils.isBitmapWhiteAtTopOrBottom(bitmap);
//                }
//                return !(imageViewDrawable instanceof LetterTileDrawable);
//            }
//
//            @Override
//            protected void onPostExecute(Boolean isWhite) {
//                super.onPostExecute(isWhite);
//                mScroller.setUseGradient(isWhite);
//            }
//        }.execute();
    }

    private void setThemeColor(MaterialPalette palette) {
        // If the color is invalid, use the predefined default
    	//aurora lgy change
        final int primaryColor = palette.mPrimaryColor;
        
        mScroller.setHeaderTintColor(primaryColor);
        mStatusBarColor = palette.mSecondaryColor;
        updateStatusBarColor();

        mColorFilter =
                new PorterDuffColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP);
        mContactCard.setColorAndFilter(primaryColor, mColorFilter);
        mRecentCard.setColorAndFilter(primaryColor, mColorFilter);
        mAboutCard.setColorAndFilter(primaryColor, mColorFilter);
    }

    private void updateStatusBarColor() {
//        if (mScroller == null) {
//            return;
//        }
//        final int desiredStatusBarColor;
//        // Only use a custom status bar color if QuickContacts touches the top of the viewport.
//        if (mScroller.getScrollNeededToBeFullScreen() <= 0) {
//            desiredStatusBarColor = mStatusBarColor;
//        } else {
//            desiredStatusBarColor = Color.TRANSPARENT;
//        }
//        // Animate to the new color.
//        final ObjectAnimator animation = ObjectAnimator.ofInt(getWindow(), "statusBarColor",
//                getWindow().getStatusBarColor(), desiredStatusBarColor);
//        animation.setDuration(ANIMATION_STATUS_BAR_COLOR_CHANGE_DURATION);
//        animation.setEvaluator(new ArgbEvaluator());
//        animation.start();
	      if (mScroller.getScrollNeededToBeFullScreen() <= 0) {
	      		com.aurora.utils.SystemUtils.switchStatusBarColorMode(com.aurora.utils.SystemUtils.STATUS_BAR_MODE_WHITE, this);
	      } else {
	      		com.aurora.utils.SystemUtils.switchStatusBarColorMode(com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
	      }
    }

    private int colorFromBitmap(Bitmap bitmap) {
        // Author of Palette recommends using 24 colors when analyzing profile photos.
        final int NUMBER_OF_PALETTE_COLORS = 24;
        final Palette palette = Palette.generate(bitmap, NUMBER_OF_PALETTE_COLORS);
        if (palette != null && palette.getVibrantSwatch() != null) {
            return palette.getVibrantSwatch().getRgb();
        }
        return 0;
    }

    private List<List<PhoneCallRecord>> mPhoneRecords = new ArrayList<>();
    private List<Entry> contactInteractionsToEntries(List<ContactInteraction> interactions) {
        final List<Entry> entries = new ArrayList<>();
        int recordIndex = 0;
        for (ContactInteraction interaction : interactions) {
        	mPhoneRecords.add(recordIndex, ((CallLogInteraction)interaction).getPhoneRecords());
        	boolean isMissedCall = false;
        	if(interaction instanceof CallLogInteraction) {
        		isMissedCall = ((CallLogInteraction)interaction).isMissedCall();
        	}
            entries.add(new Entry(/* id = */recordIndex,
                    interaction.getIcon(this),
                    interaction.getViewHeader(this),
                    interaction.getViewBody(this),
                    interaction.getBodyIcon(this),
                    interaction.getViewFooter(this),
                    interaction.getFooterIcon(this),
                    interaction.getContentDescription(this),
                    ((CallLogInteraction)interaction).getRecordIntent(),
                    /* alternateIcon = */ getResources().getDrawable(R.drawable.gn_play_call_record_light),
                    /* alternateIntent = */ ((CallLogInteraction)interaction).getRecordIntent(),
                    /* alternateContentDescription = */ null,
                    /* shouldApplyColor = */ isMissedCall ? true : false,
                    /* isEditable = */ false,
                    /* EntryContextMenuInfo = */ null,
                    /* thirdIcon = */ null,
                    /* thirdIntent = */ null,
                    /* thirdContentDescription = */ null,
                    interaction.getIconResourceId()));
            recordIndex ++;
        }
        return entries;
    }

    private final LoaderCallbacks<Contact> mLoaderContactCallbacks =
            new LoaderCallbacks<Contact>() {
        @Override
        public void onLoaderReset(Loader<Contact> loader) {
            mContactData = null;
        }

        @Override
        public void onLoadFinished(Loader<Contact> loader, Contact data) {
            Trace.beginSection("onLoadFinished()1");

            if (isFinishing()) {
                return;
            }
            if (data.isError()) {
                // This shouldn't ever happen, so throw an exception. The {@link ContactLoader}
                // should log the actual exception.
                throw new IllegalStateException("Failed to load contact", data.getException());
            }
            if (data.isNotFound()) {
                if (mHasAlreadyBeenOpened) {
                    finish();
                } else {
                    Log.i(TAG, "No contact found: " + ((ContactLoader)loader).getLookupUri());
                    Toast.makeText(QuickContactActivity.this, R.string.invalidContactMessage,
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

            Log.d(TAG,"onLoadFinish()2");
            bindContactData(data);

            Trace.endSection();
        }

        @Override
        public Loader<Contact> onCreateLoader(int id, Bundle args) {
            if (mLookupUri == null) {
                Log.wtf(TAG, "Lookup uri wasn't initialized. Loader was started too early");
            }
            // Load all contact data. We need loadGroupMetaData=true to determine whether the
            // contact is invisible. If it is, we need to display an "Add to Contacts" MenuItem.
            return new ContactLoader(getApplicationContext(), mLookupUri,
                    true /*loadGroupMetaData*/, false /*loadInvitableAccountTypes*/,
                    true /*postViewNotification*/, true /*computeFormattedPhoneNumber*/);
        }
    };

    @Override
    public void onBackPressed() {
        if (mScroller != null) {
            if (!mIsExitAnimationInProgress) {
                mScroller.scrollOffBottomAsActivity();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();

        // override transitions to skip the standard window animations
        overridePendingTransition(0, 0);
    }

    private final LoaderCallbacks<List<ContactInteraction>> mLoaderInteractionsCallbacks =
            new LoaderCallbacks<List<ContactInteraction>>() {

        @Override
        public Loader<List<ContactInteraction>> onCreateLoader(int id, Bundle args) {
            Log.v(TAG, "onCreateLoader");
            Loader<List<ContactInteraction>> loader = null;
            switch (id) {
                // Aurora xuyong 2016-01-05 deleted for bug #18275 start
                /*case LOADER_SMS_ID:
                    Log.v(TAG, "LOADER_SMS_ID");
                    loader = new SmsInteractionsLoader(
                            QuickContactActivity.this,
                            args.getStringArray(KEY_LOADER_EXTRA_PHONES),
                            MAX_SMS_RETRIEVE);
                    break;*/
                // Aurora xuyong 2016-01-05 deleted for bug #18275 end
//                case LOADER_CALENDAR_ID:
//                    Log.v(TAG, "LOADER_CALENDAR_ID");
//                    final String[] emailsArray = args.getStringArray(KEY_LOADER_EXTRA_EMAILS);
//                    List<String> emailsList = null;
//                    if (emailsArray != null) {
//                        emailsList = Arrays.asList(args.getStringArray(KEY_LOADER_EXTRA_EMAILS));
//                    }
//                    loader = new CalendarInteractionsLoader(
//                            QuickContactActivity.this,
//                            emailsList,
//                            MAX_FUTURE_CALENDAR_RETRIEVE,
//                            MAX_PAST_CALENDAR_RETRIEVE,
//                            FUTURE_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR,
//                            PAST_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR);
//                    break;
                case LOADER_CALL_LOG_ID:
                    Log.v(TAG, "LOADER_CALL_LOG_ID");
                    loader = new CallLogInteractionsLoader(
                            QuickContactActivity.this,
                            args.getStringArray(KEY_LOADER_EXTRA_PHONES),
                            MAX_CALL_LOG_RETRIEVE, mIsPrivacyContact);
            }
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<ContactInteraction>> loader,
                List<ContactInteraction> data) {
            mRecentLoaderResults.put(loader.getId(), data);
            Log.d(TAG, "isAllRecentDataLoaded():"+isAllRecentDataLoaded());
            if (isAllRecentDataLoaded()) {
            	Log.d(TAG,"bindRecentData:"+mRecentLoaderResults.size()+" data:"+data);
                bindRecentData();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<ContactInteraction>> loader) {
        	Log.d(TAG,"onLoaderReset");
            mRecentLoaderResults.remove(loader.getId());
        }
    };

    private boolean isAllRecentDataLoaded() {
    	Log.d(TAG," mRecentLoaderResults.size():"+ mRecentLoaderResults.size());
        return mRecentLoaderResults.size() == mRecentLoaderIds.length;
    }

    private void bindRecentData() {
        final List<ContactInteraction> allInteractions = new ArrayList<>();
        final List<List<Entry>> interactionsWrapper = new ArrayList<>();

        mRecentDataTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Trace.beginSection("sort recent loader results");

//                Log.d(TAG,"mRecentLoaderResults.values():"+mRecentLoaderResults.values());
                if(mRecentLoaderResults.values()==null) return null;
                for (List<ContactInteraction> loaderInteractions : mRecentLoaderResults.values()) {
                	Log.d(TAG,"loaderInteractions.size():"+loaderInteractions.size());
                    if(loaderInteractions.size()>0){
                	allInteractions.addAll(loaderInteractions);
                    }
                }

                // Sort the interactions by most recentfrom_import
                Collections.sort(allInteractions, new Comparator<ContactInteraction>() {
                    @Override
                    public int compare(ContactInteraction a, ContactInteraction b) {
                        return a.getInteractionDate() >= b.getInteractionDate() ? -1 : 1;
                    }
                });

                Trace.endSection();
                Trace.beginSection("contactInteractionsToEntries");

                // Wrap each interaction in its own list so that an icon is displayed for each entry
                for (Entry contactInteraction : contactInteractionsToEntries(allInteractions)) {
                    List<Entry> entryListWrapper = new ArrayList<>(1);
                    entryListWrapper.add(contactInteraction);
                    interactionsWrapper.add(entryListWrapper);
                }

                Trace.endSection();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Trace.beginSection("initialize recents card");

                if (allInteractions.size() > 0) {
                    mRecentCard.initialize(interactionsWrapper,
                    /* numInitialVisibleEntries = */ MIN_NUM_COLLAPSED_RECENT_ENTRIES_SHOWN,
                    /* isExpanded = */ mRecentCard.isExpanded(), /* isAlwaysExpanded = */ false,
                            mExpandingEntryCardViewListener, mScroller);
                    mRecentCard.setVisibility(View.VISIBLE);
                }else{
                	mRecentCard.setVisibility(View.GONE);
                }

                Trace.endSection();

                // About card is initialized along with the contact card, but since it appears after
                // the recent card in the UI, we hold off until making it visible until the recent
                // card is also ready to avoid stuttering.
                if (mAboutCard.shouldShow()) {
                    mAboutCard.setVisibility(View.VISIBLE);
                } else {
                    mAboutCard.setVisibility(View.GONE);
                }
                mRecentDataTask = null;
            }
        };
        mRecentDataTask.execute();
    }
       

    @Override
    protected void onStop() {
        super.onStop();

        if (mEntriesAndActionsTask != null) {
            // Once the activity is stopped, we will no longer want to bind mEntriesAndActionsTask's
            // results on the UI thread. In some circumstances Activities are killed without
            // onStop() being called. This is not a problem, because in these circumstances
            // the entire process will be killed.
            mEntriesAndActionsTask.cancel(/* mayInterruptIfRunning = */ false);
        }
        if (mRecentDataTask != null) {
            mRecentDataTask.cancel(/* mayInterruptIfRunning = */ false);
        }
        //SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK, QuickContactActivity.this);
    }

    /**
     * Returns true if it is possible to edit the current contact.
     */
    private boolean isContactEditable() {
        return mContactData != null && !mContactData.isDirectoryEntry();
    }

    /**
     * Returns true if it is possible to share the current contact.
     */
    private boolean isContactShareable() {
	    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE); 		
	    if(am.isInLockTaskMode()){
	    	return false;
	    }
    	String mode = Settings.System.getString(getContentResolver(), "aurora_power_mode");
    	if(mode != null && mode.equalsIgnoreCase("super")) {
    		return false;
    	}    			
        return mContactData != null && !mContactData.isDirectoryEntry();
    }

    private Intent getEditContactIntent() {
        final Intent intent = new Intent(Intent.ACTION_EDIT, mContactData.getLookupUri());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(
                ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
        //aurora add
        intent.putExtra("mSimId", mIndicate);
        intent.putExtra("is_privacy_contact", mIsPrivacyContact);
        return intent;
    }

    private void editContact() {
        mHasIntentLaunched = true;
        mContactLoader.cacheResult();
        startActivityForResult(getEditContactIntent(), REQUEST_CODE_CONTACT_EDITOR_ACTIVITY);
    }

    private void toggleStar(MenuItem starredMenuItem) {
        // Make sure there is a contact
        if (mContactData != null) {
            // Read the current starred value from the UI instead of using the last
            // loaded state. This allows rapid tapping without writing the same
            // value several times
            final boolean isStarred = starredMenuItem.isChecked();

            // To improve responsiveness, swap out the picture (and tag) in the UI already
            ContactDisplayUtils.configureStarredMenuItem(starredMenuItem,
                    mContactData.isDirectoryEntry(), mContactData.isUserProfile(),
                    !isStarred);

            // Now perform the real save
            final Intent intent = ContactSaveService.createSetStarredIntent(
                    QuickContactActivity.this, mContactData.getLookupUri(), !isStarred);
            startService(intent);

            final CharSequence accessibilityText = !isStarred
                    ? getResources().getText(R.string.description_action_menu_add_star)
                    : getResources().getText(R.string.description_action_menu_remove_star);
            // Accessibility actions need to have an associated view. We can't access the MenuItem's
            // underlying view, so put this accessibility action on the root view.
            mScroller.announceForAccessibility(accessibilityText);
        }
    }

    /**
     * Calls into the contacts provider to get a pre-authorized version of the given URI.
     */
    private Uri getPreAuthorizedUri(Uri uri) {
        final Bundle uriBundle = new Bundle();
        uriBundle.putParcelable(ContactsContract.Authorization.KEY_URI_TO_AUTHORIZE, uri);
        final Bundle authResponse = getContentResolver().call(
                ContactsContract.AUTHORITY_URI,
                ContactsContract.Authorization.AUTHORIZATION_METHOD,
                null,
                uriBundle);
        if (authResponse != null) {
            return (Uri) authResponse.getParcelable(
                    ContactsContract.Authorization.KEY_AUTHORIZED_URI);
        } else {
            return uri;
        }
    }

    private void shareContact() {
        final String lookupKey = mContactData.getLookupKey();
        Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);
        if (mContactData.isUserProfile()) {
            // User is sharing the profile.  We don't want to force the receiver to have
            // the highly-privileged READ_PROFILE permission, so we need to request a
            // pre-authorized URI from the provider.
            shareUri = getPreAuthorizedUri(shareUri);
        }

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
        
        intent.putExtra("contactId", String.valueOf(mContactData.getId()));
        intent.putExtra("LOOKUPURIS", String.valueOf(mLookupUri));
        
        
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);

        // Launch chooser to share contact via
        final CharSequence chooseTitle = getText(R.string.share_via);
        final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

        try {
            mHasIntentLaunched = true;
            this.startActivity(chooseIntent);
        } catch (final ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a launcher shortcut with the current contact.
     */
    private void createLauncherShortcutWithContact() {
        final ShortcutIntentBuilder builder = new ShortcutIntentBuilder(this,
                new OnShortcutIntentCreatedListener() {

                    @Override
                    public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
                        // Broadcast the shortcutIntent to the launcher to create a
                        // shortcut to this contact
                        shortcutIntent.setAction(ACTION_INSTALL_SHORTCUT);
                        QuickContactActivity.this.sendBroadcast(shortcutIntent);

                        // Send a toast to give feedback to the user that a shortcut to this
                        // contact was added to the launcher.
                        Toast.makeText(QuickContactActivity.this,
                                R.string.createContactShortcutSuccessful,
                                Toast.LENGTH_SHORT).show();
                    }

                });
        builder.createContactShortcutIntent(mContactData.getLookupUri());
    }

    private boolean isShortcutCreatable() {
        final Intent createShortcutIntent = new Intent();
        createShortcutIntent.setAction(ACTION_INSTALL_SHORTCUT);
        final List<ResolveInfo> receivers = getPackageManager()
                .queryBroadcastReceivers(createShortcutIntent, 0);
        return receivers != null && receivers.size() > 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quickcontact, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mContactData != null) {
            final MenuItem starredMenuItem = menu.findItem(R.id.menu_star);
            ContactDisplayUtils.configureStarredMenuItem(starredMenuItem,
                    mContactData.isDirectoryEntry(), mContactData.isUserProfile(),
                    mContactData.getStarred());
			if(mIndicate >= 0) {
				starredMenuItem.setVisible(false);
			}

            // Configure edit MenuItem
            final MenuItem editMenuItem = menu.findItem(R.id.menu_edit);
            editMenuItem.setVisible(true);
            if (DirectoryContactUtil.isDirectoryContact(mContactData) || InvisibleContactUtil
                    .isInvisibleAndAddable(mContactData, this)) {
                editMenuItem.setIcon(R.drawable.ic_person_add_tinted_24dp);
                editMenuItem.setTitle(R.string.menu_add_contact);
            } else if (isContactEditable()) {
                editMenuItem.setIcon(R.drawable.ic_create_24dp);
                editMenuItem.setTitle(R.string.edit_contact);
            } else {
                editMenuItem.setVisible(false);
            }

            final MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(isContactShareable());

            final MenuItem shortcutMenuItem = menu.findItem(R.id.menu_create_contact_shortcut);
            shortcutMenuItem.setVisible(isShortcutCreatable());
            shortcutMenuItem.setVisible(false);
            
   		  final MenuItem removeBlackMenuItem = menu.findItem(R.id.menu_remove_black);
		  final MenuItem addBlackMenuItem = menu.findItem(R.id.menu_add_black);
		  
		  checkForBlack();
		  
		  if (mIsShowRejectFlag) {
				removeBlackMenuItem.setVisible(true);
				if(mIsAllRejectFlag) {
					addBlackMenuItem.setVisible(false);
				} else {
					addBlackMenuItem.setVisible(true);
				}
    		} else {
    			addBlackMenuItem.setVisible(true);
    			removeBlackMenuItem.setVisible(false);
    		}
            
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_star:
                toggleStar(item);
                return true;
            case R.id.menu_edit:
                if (DirectoryContactUtil.isDirectoryContact(mContactData)) {
                	Log.i(TAG, "lgy menu_edit isDirectoryContact " );
                    // This action is used to launch the contact selector, with the option of
                    // creating a new contact. Creating a new contact is an INSERT, while selecting
                    // an exisiting one is an edit. The fields in the edit screen will be
                    // prepopulated with data.

                    final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);

                    // Only pre-fill the name field if the provided display name is an organization
                    // name or better (e.g. structured name, nickname)
                    if (mContactData.getDisplayNameSource() >= DisplayNameSources.ORGANIZATION) {
                        intent.putExtra(Intents.Insert.NAME, mContactData.getDisplayName());
                    }
                    ArrayList<ContentValues> values = mContactData.getContentValues();
                    // Last time used and times used are aggregated values from the usage stat
                    // table. They need to be removed from data values so the SQL table can insert
                    // properly
                    for (ContentValues value : values) {
                        value.remove(Data.LAST_TIME_USED);
                        value.remove(Data.TIMES_USED);
                    }
                    intent.putExtra(Intents.Insert.DATA, values);

                    // If the contact can only export to the same account, add it to the intent.
                    // Otherwise the ContactEditorFragment will show a dialog for selecting an
                    // account.
                    if (mContactData.getDirectoryExportSupport() ==
                            Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY) {
                        intent.putExtra(Intents.Insert.ACCOUNT,
                                new Account(mContactData.getDirectoryAccountName(),
                                        mContactData.getDirectoryAccountType()));
                        intent.putExtra(Intents.Insert.DATA_SET,
                                mContactData.getRawContacts().get(0).getDataSet());
                    }

                    // Add this flag to disable the delete menu option on directory contact joins
                    // with local contacts. The delete option is ambiguous when joining contacts.
                    intent.putExtra(ContactEditorFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION,
                            true);

                    startActivityForResult(intent, REQUEST_CODE_CONTACT_SELECTION_ACTIVITY);
                } else if (InvisibleContactUtil.isInvisibleAndAddable(mContactData, this)) {
                    InvisibleContactUtil.addToDefaultGroup(mContactData, this);
                } else if (isContactEditable()) {
                    editContact();
                }
                return true;
            case R.id.menu_share:
                shareContact();
            	/*   if (mContactData == null) {
            		   return true;
            	   }
                   final String lookupKey = mContactData.getLookupKey();
                   Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);
                   final Intent intent = new Intent(Intent.ACTION_SEND);
                   // aurora ukiliu 2013-10-10 modify for aurora-design begin
                   intent.setClass(QuickContactActivity.this, com.android.contacts.ShareContactViaSMS.class);
                   intent.setType(Contacts.CONTENT_VCARD_TYPE);
                   intent.putExtra("contactId", String.valueOf(mContactData.getId()));
                   intent.putExtra("LOOKUPURIS", String.valueOf(mLookupUri));
                   // aurora ukiliu 2013-10-10 modify for aurora-design end
                   intent.putExtra(Intent.EXTRA_STREAM, shareUri);
                   try {
                       startActivity(intent);
                   } catch (ActivityNotFoundException ex) {
                   } */
                return true; 
            case R.id.menu_create_contact_shortcut:
                createLauncherShortcutWithContact();
                return true;
            case android.R.id.home:           
                onBackPressed();
                return true;
            case R.id.menu_contact_delete: {
            	auroraDeleteContact();
            	return true;
            }
            case R.id.menu_add_black:
            	showAddBlackDialog(); 
//            	showAddBlackPopupWindow(QuickContactActivity.this.findViewById(R.id.multiscroller));
                return true;
            case R.id.menu_remove_black:	
            	showRemoveBlackDialog();
//            	showRemoveBlackPopupWindow(QuickContactActivity.this.findViewById(R.id.multiscroller));
                return true;
              case R.id.menu_contact_qrcode: 
            	showQrcodeImage();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }  
    
 

    private List<Entry> getRingtoneEntries() {
    	 final List<Entry> entries = new ArrayList<>();     
         final Entry entry = getRingtoneEntry(this);
         if (entry != null) {
             entries.add(entry);
         }
         return entries;
    }
    
    private Entry getRingtoneEntry(Context context) {
        String header = null;
        String subHeader = null;
        context = context.getApplicationContext();
        final Resources res = context.getResources();
    
        header = res.getString(R.string.gn_ringtone_label);
        String ringtoneTitle = com.android.contacts.ContactLoader.gnGetRingtoneTile(this, mContactData.getCustomRingtone());    
        if (null == ringtoneTitle) {
    		ringtoneTitle = getString(R.string.gnDefaultLabel);
    	}
        subHeader =   ringtoneTitle;
        

    	if (null == ringtoneTitle || ringtoneTitle.equals(getString(R.string.gnDefaultLabel))) { 
    		return null;
    	}

        // Ignore dataIds from the Me profile.
        final int dataId = -1;

        return new Entry(-1, null, header, null, null, subHeader, null,
                "", null, null, null,
                "", false, false,
                null, null, null, "",
                0);
    }
    
    private static final int MAX_LINES_FOR_GROUP = 10;
    private DataKind mGroupDataKind = null;
    private DataKind getDataKindGroupMembership(GroupMembershipDataItem dataItem) {
        DataKind kind = new DataKind(GroupMembership.CONTENT_ITEM_TYPE,
                R.string.groupsLabel, 999, true);

        kind.typeOverallMax = 1;
        kind.actionBody =  new SimpleInflater(R.string.groupsLabel);
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(GroupMembership.GROUP_ROW_ID, -1, -1));

        kind.maxLinesForDisplay = MAX_LINES_FOR_GROUP;
       

        Long groupId = dataItem.getGroupRowId();
        if (groupId != null) {
        	 String name = getGroupMembershipName(mContactData.getGroupMetaData(), groupId);
        	 if(mGroupDataKind == null) {
        		 kind.typeColumn = name; 
        	 } else {
        		 mGroupDataKind.typeColumn += " "+ name;
        	 }
        }

        if(mGroupDataKind == null) {
        	mGroupDataKind = kind;
        } 
        return mGroupDataKind;
    }
    
    private DataKind getDataKindEvent(EventDataItem dataItem) {
    	 DataKind kind = new DataKind(Event.CONTENT_ITEM_TYPE,
                 R.string.eventLabelsGroup, 150, true);
     kind.actionHeader = new EventActionInflater();
     kind.actionBody = new SimpleInflater(Event.START_DATE);

     kind.typeColumn = Event.TYPE;
     kind.typeList = Lists.newArrayList();
     kind.dateFormatWithoutYear = CommonDateUtils.NO_YEAR_DATE_FORMAT;
     kind.dateFormatWithYear = CommonDateUtils.FULL_DATE_FORMAT;
     kind.typeList.add(buildEventType(Event.TYPE_BIRTHDAY, true).setSpecificMax(1));
     kind.typeList.add(buildEventType(Event.TYPE_ANNIVERSARY, false));
     kind.typeList.add(buildEventType(Event.TYPE_OTHER, false));
     kind.typeList.add(buildEventType(Event.TYPE_CUSTOM, false).setSecondary(true)
             .setCustomColumn(Event.LABEL));

     kind.defaultValues = new ContentValues();
     kind.defaultValues.put(Event.TYPE, Event.TYPE_BIRTHDAY);

     kind.fieldList = Lists.newArrayList();
     kind.fieldList.add(new EditField(Event.DATA, R.string.eventLabelsGroup, FLAGS_EVENT));

     return kind;
    }
    protected static final int FLAGS_EVENT = EditorInfo.TYPE_CLASS_TEXT;
    
    private static EditType buildEventType(int type, boolean yearOptional) {
        return new EventEditType(type, Event.getTypeResource(type)).setYearOptional(yearOptional);
    }
    
    
    
    /**
     * Maps group ID to the corresponding group name, collapses all synonymous groups.
     * Ignores default groups (e.g. My Contacts) and favorites groups.
     */
    private String getGroupMembershipName( List<GroupMetaData> groupMetaData, long groupId) {
        if (groupMetaData == null) {
            return "";
        }

        for (GroupMetaData group : groupMetaData) {
            if (group.getGroupId() == groupId) {
                if (!group.isDefaultGroup() && !group.isFavorites()) {
                    return group.getTitle();
                }
            }
        }
        return "";
    }
    
    private void auroraDeleteContact() {

    	if (null == mContactData || null == mLookupUri) {
    		return;
    	}
    	
    	Uri contactUri = mContactData.getUri();
    	
        if (mIndicate < 0) {
        	// aurora ukiliu 2013-11-16 modify for BUG #626 begin 
//        	ContactDeletionInteraction.start(mContext, contactUri, true);
        	final CharSequence name = mContactData.getDisplayName();
        	String message = ContactsApplication.getInstance().getResources().getString(R.string.aurora_delete_one_contact_message, name);
        	String title = ContactsApplication.getInstance().getResources().getString(R.string.delete);
        	final long rawContactId = mContactData.getNameRawContactId();
        	final long contactId = mContactData.getId();
        	
        	View dialogView = null;
        	AuroraCheckBox black_remove = null;
        	if (mIsPrivacyContact) {
        		title = ContactsApplication.getInstance().getResources().getString(R.string.gn_remove);
        		
        		dialogView = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
        		TextView messageView = (TextView)dialogView.findViewById(R.id.textView1);
        		messageView.setText(mContext.getString(R.string.aurora_remove_one_privacy_contact_message, name));
        		black_remove = (AuroraCheckBox)dialogView.findViewById(R.id.check_box);
        		black_remove.setText(mContext.getString(R.string.aurora_remove_privacy_contact_check_box_text));
        		black_remove.setChecked(false);
        	} 
        	
        	final AuroraCheckBox checked = black_remove;
        	
        	AuroraAlertDialog mDialog = new AuroraAlertDialog.Builder(mContext,
					AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
					.setTitle(title)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(R.string.delete,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialog,
										int whichButton) {
									if (mIsPrivacyContact) {
			            				if (checked.isChecked()) {
			                    			mContext.getContentResolver().delete(
			                                        RawContacts.CONTENT_URI, 
			                                        RawContacts._ID + "=?" + " and deleted=0 and is_privacy=" + mCurrentPrivacyAccountId, 
			                                        new String[] {String.valueOf(rawContactId)});
			            				} else {
			            					ContentValues values = new ContentValues();
			                        		values.put("is_privacy", 0);
			                        		
			                    			mContext.getContentResolver().update(RawContacts.CONTENT_URI, values, 
			                                		RawContacts.CONTACT_ID + "=" + contactId, null);
			                                mContext.getContentResolver().update(Data.CONTENT_URI, values, 
			                                        Data.CONTACT_ID + "=" + contactId, null);
			                                values.clear();
			            				}
			            				
			            				finish();
			            				
			            				return;
			            			}
									
									CharSequence toastChar = ContactsApplication.getInstance().
				                            getResources().getString(R.string.aurora_delete_one_contact_toast, 
				                            		name);
									
				            		if (doDeleteContact(rawContactId)) {
				            			toastChar = ContactsApplication.getInstance().
					                            getResources().getString(R.string.aurora_delete_one_contact_toast, 
					                            		name);
				            			Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
				                                toastChar, Toast.LENGTH_SHORT).show();
                                ContactsApplication.getInstance().sendSyncBroad();
				            			finish();
				            		} else {
				            			toastChar = ContactsApplication.getInstance().
					                            getResources().getString(R.string.notifier_fail_delete_title);
				            			Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
				                                toastChar, Toast.LENGTH_SHORT).show();
				            		}					            		
								}
							}).create();
        	
        	if (mIsPrivacyContact) {
        		mDialog.setView(dialogView);
        	} else {
        		mDialog.setTitle(message);
        		mDialog.setMessage(ContactsApplication.getInstance().getResources().getString(R.string.single_contact_delete_summary));
        	}
        	
            Window window = mDialog.getWindow();  
            window.setWindowAnimations(R.style.aurora_dialog_anim); 
        	mDialog.show();
        	// aurora ukiliu 2013-11-16 modify for BUG #626 end
        } else {
        	Log.d(TAG, "indicate >=0");			

			String message = ContactsApplication.getInstance().getResources().getString(R.string.aurora_delete_one_contact_message, mContactData.getDisplayName());
			String title = ContactsApplication.getInstance().getResources().getString(R.string.delete);
			AuroraAlertDialog mDialog = new AuroraAlertDialog.Builder(mContext,
					AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
			.setTitle(title)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(R.string.delete,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int whichButton) {
					doDeleteContactForSimContact(mRawContactId);
				}
			}).create();

			if (mIsPrivacyContact) {
//				mDialog.setView(dialogView);
			} else {
				mDialog.setTitle(message);
				mDialog.setMessage(ContactsApplication.getInstance().getResources().getString(R.string.single_contact_delete_summary));
			}

			Window window = mDialog.getWindow();  
			window.setWindowAnimations(R.style.aurora_dialog_anim); 
			mDialog.show();

			if(true) return;
			
			
        	
			if (GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
				Toast.makeText(
						mContext,
						R.string.aurora_sim_not_ready,
						Toast.LENGTH_SHORT).show();
				return;
			}
			
      
            Uri simUri;
            if (Build.VERSION.SDK_INT < 21) {
            	  simUri = SubContactsUtils.getUri(SIMInfo.getSlotById(mContext, mIndicate));
			} else {
				 // Aurora xuyong 2015-07-13 modified for bug #14161 start
			      simUri = SimCardUtils.getSimContactsUri((int)mIndicate,
	                        		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mContext, mIndicate)));
	                // Aurora xuyong 2015-07-13 modified for bug #14161 end
			}
            
            
            // qc begin
            if (GNContactsUtils.isOnlyQcContactsSupport() && ContactsApplication.isMultiSimEnabled && contactUri != null) {
                int slotId = GNContactsUtils.querySlotIdByLookupUri(getApplicationContext(), contactUri);
                simUri = GNContactsUtils.getQcSimUri(slotId);
            }
            // qc end
            Log.v(TAG, "name="+mContactData.getDisplayName()+"PhoneticName="+mContactData.getPhoneticName()
            		+"number="+getPhoneNumber());
            String name = mContactData.getDisplayName();              
            if (Build.VERSION.SDK_INT < 21) {
                if(name.equalsIgnoreCase(getPhoneNumber()))
                	name="";
                String s="槌�";
            	name=name.replace(' ', s.toCharArray()[0]);//aurora add zhouxiaobing 20140305 for delete space
                simUri=SimCardUtils.getSimContactsUri(SIMInfo.getSlotById(mContext,mIndicate), false);
//                ContactDeletionInteraction.start(mContext, contactUri, true, simUri, ("index = " + simIndex));   
                ContactsLog.log("detail contact delete index = " + mSimIndex);
                ContactDeletionInteraction.start(QuickContactActivity.this, contactUri, true, simUri, ("tag = " + name+" AND "+"number="+getPhoneNumber()));//aurora change zhouxiaobing 20131227   
            } else {
            	// Aurora xuyong 2015-07-13 modified for bug #14161 start
                simUri = SimCardUtils.getSimContactsUri((int)mIndicate,
                		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mContext, mIndicate)));
                // Aurora xuyong 2015-07-13 modified for bug #14161 end 
                ContactsLog.log("detail contact delete index = " + mSimIndex);
                ContactDeletionInteraction.start(QuickContactActivity.this, contactUri, true, simUri, ("index = " + mSimIndex), name);
            }
            

        }
    
    }
    
    private long mIndicate = -1;
    private int mSimIndex = -1;
    private Context mContext;
    private boolean  mIsPrivacyContact = false;
    private long mCurrentPrivacyAccountId = 0;
    private boolean mIsShowRejectFlag, mIsAllRejectFlag;
    private final int MUTI_CHOICE_DIALOG = 1;  
    
 // aurora ukiliu 2013-11-16 add for BUG #626 begin 
    private boolean doDeleteContact(long rawContactId) {
    	
    	int resultcode = getContentResolver().delete(
                RawContacts.CONTENT_URI, 
                RawContacts._ID + " = " + rawContactId + " and deleted = 0", 
                null);
    	if (resultcode > 0) {
    		return true;
    	}
    	return false;
    }
    // aurora ukiliu 2013-11-16 add for BUG #626 end
    
  //liyang add begin
  	private boolean doDeleteContactForSimContact(long rawContactId) {
  		Log.d(TAG, "rawContactId = " + rawContactId + "  indicate = " + mIndicate);

  		int result = 0;
  		int deleteCount=0;

  		if (mIndicate >= 0) {
  			// stop
  			if ((FeatureOption.MTK_GEMINI_SUPPORT && !SimCardUtils.isSimStateReady(SIMInfoWrapper.getDefault().getSlotIdBySimId((int)mIndicate)))
  					|| (!FeatureOption.MTK_GEMINI_SUPPORT && !SimCardUtils
  							.isSimStateReady(0))||GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
  				return false;
  			}

  			String number="";
  			String name="";
  			final String[] projection = new String[] {
  					Contacts._ID, 
  					Contacts.Data.MIMETYPE, 
  					Contacts.Data.DATA1,
  			};
  			Cursor dataCursor = mContext.getContentResolver().query(Data.CONTENT_URI, 
  					projection, Data.RAW_CONTACT_ID + " =? ", 
  					new String[] { String.valueOf(rawContactId) }, null);
  			if (dataCursor != null && dataCursor.moveToFirst()) {
  				do {
  					if(dataCursor.getString(1).equals(Phone.CONTENT_ITEM_TYPE)) {
  						number=dataCursor.getString(2);
  					} else if (dataCursor.getString(1).equals(StructuredName.CONTENT_ITEM_TYPE)) {
  						name=dataCursor.getString(2);
  					}
  				} while (dataCursor.moveToNext());

  				dataCursor.close();
  				dataCursor=null;
  				Log.v(TAG, "tag = " + name + "  number = " + number + "  uri = " + SubContactsUtils.getUri(0));

  				try {
  					if (Build.VERSION.SDK_INT < 21) {
  						Log.d(TAG, "uri0:"+SimCardUtils.getSimContactsUri(SIMInfo.getSlotById(mContext,mIndicate), false));
  						String s="鳚";
  						name=name.replace(' ', s.toCharArray()[0]);//aurora add zhouxiaobing 20140305 for delete space
  						result = mContext.getContentResolver().delete(/*SubContactsUtils.getUri(0)*/SimCardUtils.getSimContactsUri(SIMInfo.getSlotById(mContext,mIndicate), false), ("tag=" + name+" AND "+"number="+number), null);
  					} else {
  						Log.d(TAG, "uri:"+SimCardUtils.getSimContactsUri((int)mIndicate,
  								SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mContext, mIndicate))));
  						// Aurora xuyong 2015-07-13 modified for bug #14161 start
  						result = mContext.getContentResolver().delete(SimCardUtils.getSimContactsUri((int)mIndicate,
  								SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mContext, mIndicate))), ("tag=" + name+" AND "+"number="+number), null);
  						// Aurora xuyong 2015-07-13 modified for bug #14161 end					

  					}    
  					deleteCount = getContentResolver().delete(
  							RawContacts.CONTENT_URI, 
  							RawContacts._ID + " = " + rawContactId + " and deleted = 0", 
  							null);
  				} catch (Exception e) {
  					e.printStackTrace();
  				}
  			} else {
  				if(dataCursor != null) {
  					dataCursor.close();
  				}
  			}
  		}


  		Log.d(TAG,"result:"+result+" deleteCount:"+deleteCount);
  		if (deleteCount > 0) {
  			return true;
  		}
  		return false;
  	}

  	//liyang add end
    
    public int[]  getSimContactInfo() {		
    	int[] result= new int[2];
		result[0] = -1;
		result[1] = -1;
				long contact_id = mContactData.getNameRawContactId();	
				Cursor cursor2=mContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{RawContacts.INDICATE_PHONE_SIM,  RawContacts.INDEX_IN_SIM},
						android.provider.ContactsContract.RawContacts._ID+" = " +contact_id+ " AND "+ "deleted"+" < 1" + " and is_privacy > -1", null, null);
//				Log.v(TAG, "cursor2 count="+cursor2.getCount()+"contact_id="+contact_id);
				if(cursor2!=null) {
					if(cursor2.moveToFirst()) {
						int indicate = cursor2.getInt(0);
						int simIndex = cursor2.getInt(0);
						result[0] = indicate;
						result[1] = simIndex;
						Log.v(TAG, "indicate="+indicate + " simIndex = " + simIndex);
					}
					cursor2.close();
					cursor2 = null;
				}
			
		return result;
		
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	   if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyContact) {
               ContactsApplication.mPrivacyActivityList.remove(this);
           }
		     if(mBlackDialog != null) {
		    	 mBlackDialog.dismiss();
		    	 mBlackDialog = null;
		     }
		     
		     if(mCallDialog != null) {
		    	 mCallDialog.dismiss();
		    	 mCallDialog = null;
		     }
		     
		     if(myDialog != null) {
	        	  myDialog.dismiss();
	        	  myDialog = null;
	          }
    }
    
  //aurora add zhouxiaobing 20131227 start
    public String getPhoneNumber()
    {
        String mimeType=Phone.CONTENT_ITEM_TYPE;
           
        final Map<String, List<DataItem>> dataItemsMap = mCachedCp2DataCardModel.dataItemsMap;
        final List<DataItem> phoneDataItems = dataItemsMap.get(Phone.CONTENT_ITEM_TYPE);    
        if (phoneDataItems != null) {     
                return ((PhoneDataItem) phoneDataItems.get(0)).getNumber();
        }
        return "";
        
    }
//aurora add zhouxiaobing 20131227 end    
    
    
    private final OnClickListener mCallLogMenuButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	mHasDialNumber=true;
        	Log.i("CallLogExpandingEntryCardView" , "mCallLogMenuButtonListener onClick rawcontactId = " + mContactData.getNameRawContactId());
        	Intent intent= IntentFactory.newShowFullCallDetailIntentV2(QuickContactActivity.this, mContactData.getNameRawContactId());
        	intent.putExtra("is_privacy_contact", mIsPrivacyContact);
        	mContext.startActivity(intent);
        }
    };
    
    
    private List<String> mPhoneNumbers = new ArrayList<String>() ;
    private List<String> 		mBlackNumber = new ArrayList<String>();
    private List<String> 		mAddDialogNumbers = new ArrayList<String>();

    
    private void checkForBlack() {
    	if(mContactData == null || mCachedCp2DataCardModel == null) {
    		return; 
    	}
    	mPhoneNumbers.clear();
    	mBlackNumber.clear();
    	 final Map<String, List<DataItem>> dataItemsMap = mCachedCp2DataCardModel.dataItemsMap;
         final List<DataItem> phoneDataItems = dataItemsMap.get(Phone.CONTENT_ITEM_TYPE);          
         if (phoneDataItems != null) {
             for (int i = 0; i < phoneDataItems.size(); ++i) {    
            	 mPhoneNumbers.add(((PhoneDataItem) phoneDataItems.get(i)).getNumber());
             }
             int count = 0;
             for(String number:mPhoneNumbers) {
            	 if(checkForBlackNumber(number)) {    
            		 mBlackNumber.add(number);
            		 count++;
            	 }
             }
             if(count == mPhoneNumbers.size()) {
            	 mIsAllRejectFlag = true;
            	 mIsShowRejectFlag = true;
             } else if(count >0) {
            	 mIsShowRejectFlag = true;
            	 mIsAllRejectFlag = false;
             } else {
            	 mIsShowRejectFlag = false;
            	 mIsAllRejectFlag = false;
             }
             mAddDialogNumbers = new ArrayList<String>(mPhoneNumbers);     
             mAddDialogNumbers.removeAll(mBlackNumber);
             if(mAddDialogNumbers.size() > 0) {
             	mSelectAddNumber = mAddDialogNumbers.get(0);
             }         
             if(mBlackNumber.size() > 0) {
            	 mSelectRemoveNumber = mBlackNumber.get(0);
             }
             
             
             
         }
      
    }
    
    private boolean checkForBlackNumber(String number) {
		if (!ContactsApplication.sIsAuroraRejectSupport) {
		    return false;
		}
		boolean result = false;
		Cursor cursor = mContext.getContentResolver().query(
				Uri.parse("content://com.android.contacts/black"), new String[]{"reject"},
				GNContactsUtils.getPhoneNumberEqualString(number) + " and isblack=1", null, null);
		if (cursor != null) {
		    if (cursor.moveToFirst()) {
		    	int reject = cursor.getInt(0);
		        if (reject > 0) {
		        	result = true;
		        }
		    }
		    cursor.close();
		    cursor = null;
		} 
		
		return result;
	}
    
    
    private String mName = "";
    private void addToBlack(String number) {
//	    Intent intent = new Intent();
//	    intent.setClassName("com.aurora.reject", "com.aurora.reject.AuroraManuallyAddActivity");
//	    try {
//	    	Bundle bundle = new Bundle();
//            bundle.putString("add_name", getSupportActionBar().getTitle().toString());
//            bundle.putString("add_number", number);
//            intent.putExtras(bundle);
//            intent.putExtra("add", true);
//	    	mContext.startActivity(intent);
//	    } catch (ActivityNotFoundException e) {
//	        e.printStackTrace();
//	    }    	    
    	com.android.contacts.ContactsUtils.addblack(this, number, mName);
	}
	
	private void removeFromBlack(final String number) {
//		View view = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
//		final AuroraCheckBox checkBox = (AuroraCheckBox)view.findViewById(R.id.check_box);
//		checkBox.setChecked(true);
//
//		AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
//				QuickContactActivity.this)
//				.setTitle(mContext.getResources().getString(R.string.black_remove))
//				.setView(view)
//				.setPositiveButton(android.R.string.ok,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int whichButton) {
//								boolean recoveryLogs = checkBox.isChecked();
//								int isblack = 0;
//								if (!recoveryLogs) {
//									isblack = -1;
//								}
//								
//								ContentValues values = new ContentValues();
//								values.put("isblack", isblack);
//								values.put("number", number);
//								values.put("reject", 0);
//				            	mContext.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), values, GNContactsUtils.getPhoneNumberEqualString(number), null);
//				                values.clear();
//							}
//						})
//				.setNegativeButton(android.R.string.cancel,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int whichButton) {
//								dialog.dismiss();
//							}
//						}).show();	
		
		ContentValues values = new ContentValues();
		values.put("isblack", 0);
		values.put("number", number);
		values.put("reject", 0);
    	mContext.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), values, GNContactsUtils.getPhoneNumberEqualString(number), null);
        values.clear();
	
	}
	
	private String mSelectAddNumber, mSelectRemoveNumber;
	AuroraAlertDialog mBlackDialog = null;  
 
    protected void showAddBlackDialog() {  
		     if(mBlackDialog != null) {
		    	 mBlackDialog.dismiss();
		    	 mBlackDialog = null;
		     }
		     
//		     if(mAddDialogNumbers.size() ==1) {
//		    	 addToBlack(mSelectAddNumber);
//		    	 return;
//		     }
    	
	    
        final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
			        .setTitle(R.string.add_contact_to_black_title)
			        .setMessage(R.string.add_black_message)        
			        .setNegativeButton(android.R.string.cancel, null)
			        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
		               		     for(String number: mAddDialogNumbers) {
		        		    		 addToBlack(number);		    	 
		        		         }
                        }
                    });                    
        mBlackDialog = builder.create();   
        Window window = mBlackDialog.getWindow();  
        window.setWindowAnimations(R.style.aurora_dialog_anim); 
        mBlackDialog.show();
		     
     
//		     AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);  
//		        builder.setTitle(R.string.add_contact_to_black_title);
//
//                DialogInterface.OnClickListener singleListener =   
//                    new DialogInterface.OnClickListener() {  
//                          
//                        @Override  
//                        public void onClick(DialogInterface dialogInterface,   
//                                int which) {  
//                        	mSelectAddNumber = mAddDialogNumbers.get(which) ;  
//                        }  
//                    };           
//                    String[] adapter = new String[mAddDialogNumbers.size()];
//                    mAddDialogNumbers.toArray(adapter);
//                builder.setSingleChoiceItems(adapter, 0, singleListener);  
//                DialogInterface.OnClickListener btnListener =   
//                    new DialogInterface.OnClickListener() {  
//                        @Override  
//                        public void onClick(DialogInterface dialogInterface, int which) {
//                        	addToBlack(mSelectAddNumber);
//                        }  
//                    };  
//                builder.setPositiveButton(android.R.string.ok, btnListener);  
//                mBlackDialog = builder.create();   
//                mBlackDialog.show();
    }  
    
    
    
    protected void showRemoveBlackDialog() {  
	     if(mBlackDialog != null) {
	    	 mBlackDialog.dismiss();
	    	 mBlackDialog = null;
	     }
	     
	     for(String number: mBlackNumber) {
	    	 removeFromBlack(number);
	     }
	     return;
	     
//	     if(mBlackNumber.size() ==1) {
//	    	 removeFromBlack(mSelectRemoveNumber);
//	    	 return;
//	     }
//
//	     AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);  
//           builder.setTitle(R.string.remove_contact_from_black_title);
//
//           DialogInterface.OnClickListener singleListener =   
//               new DialogInterface.OnClickListener() {  
//                     
//                   @Override  
//                   public void onClick(DialogInterface dialogInterface,   
//                           int which) {  
//                	   mSelectRemoveNumber = mBlackNumber.get(which) ;  
//                   }  
//               };           
//               String[] adapter = new String[mBlackNumber.size()];
//               mBlackNumber.toArray(adapter);
//           builder.setSingleChoiceItems(adapter, 0, singleListener);  
//           DialogInterface.OnClickListener btnListener =   
//               new DialogInterface.OnClickListener() {  
//                   @Override  
//                   public void onClick(DialogInterface dialogInterface, int which) {
//                   	removeFromBlack(mSelectRemoveNumber);
//                   }  
//               };  
//           builder.setPositiveButton(android.R.string.ok, btnListener);  
//           mBlackDialog = builder.create();   
//           mBlackDialog.show();
}  
    
    public boolean dispatchKeyEvent(KeyEvent event) {
  		Log.v(TAG, "dispatchKeyEvent dispatchKeyEvent  "); 
        if(event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
     	   Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      	   if(toolbar.isOverflowMenuShowing()) {
      		   toolbar.hideOverflowMenu();
      	   } else {
      		   toolbar.showOverflowMenu();
      	   }
     	   return true;
     	   
       }
    return super.dispatchKeyEvent(event);
}
    
    public class MenuAdapter extends ArrayAdapter<String> {
        private int mResourceId;
  

        public MenuAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            this.mResourceId = textViewResourceId;
      
        }
        
        public MenuAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
            this.mResourceId = textViewResourceId;
      
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {            
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(mResourceId, null);
            TextView text = (TextView) view.findViewById(R.id.popup_menu_item);
            text.setText(getItem(position));     
            return view;
        }
    }
    
   private void showAddBlackPopupWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.aurora_popup_menu, null);
        
        AuroraListView menulist = (AuroraListView)contentView.findViewById(R.id.menu_list);
        
        menulist.setAdapter(new MenuAdapter(QuickContactActivity.this,R.layout.aurora_popup_menu_item, mAddDialogNumbers));
        
        final PopupWindow popupWindow = new PopupWindow(contentView,
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        
        menulist.setOnItemClickListener(new OnItemClickListener() {
        	@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		mSelectAddNumber = mAddDialogNumbers.get(position) ;  
        	 	addToBlack(mSelectAddNumber);
        	 	popupWindow.dismiss();
        	}
		});

        popupWindow.setTouchable(true);
        
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
      
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        // 设置好参数之后再show
        popupWindow.showAtLocation (view, Gravity.BOTTOM, 0, 0 );

    }
   
   private void showRemoveBlackPopupWindow(View view) {

       // 一个自定义的布局，作为显示的内容
       View contentView = LayoutInflater.from(mContext).inflate(
               R.layout.aurora_popup_menu, null);
       
       AuroraListView menulist = (AuroraListView)contentView.findViewById(R.id.menu_list);
       
       menulist.setAdapter(new MenuAdapter(QuickContactActivity.this,R.layout.aurora_popup_menu_item, mBlackNumber));
       
       final PopupWindow popupWindow = new PopupWindow(contentView,
               LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
       
       menulist.setOnItemClickListener(new OnItemClickListener() {
       	@Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       		mSelectRemoveNumber = mBlackNumber.get(position) ;  
       		removeFromBlack(mSelectRemoveNumber);
       	 	popupWindow.dismiss();
       	}
		});

       popupWindow.setTouchable(true);
       
       popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
     
       // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
       // 我觉得这里是API的一个bug
       popupWindow.setBackgroundDrawable(new BitmapDrawable());

       // 设置好参数之后再show
       popupWindow.showAtLocation (view, Gravity.BOTTOM, 0, 0 );

   }
   
   
	private  AuroraAlertDialog mCallDialog = null;  
	private int mSelectSlot = 0;
	
   private void showCallDialog(Intent intent) {  
   	
    	final String number = PhoneNumberUtils.getNumberFromIntent(intent, this);
	     if(mCallDialog != null) {
	    	 mCallDialog.dismiss();
	    	 mCallDialog = null;
	     }
	     
  	if(ContactsApplication.isMultiSimEnabled) {
  		boolean showDouble = com.android.contacts.ContactsUtils.isShowDoubleButton();
			if (showDouble && SubContactsUtils.simStateReady(0) && SubContactsUtils.simStateReady(1)) {
				  int lastCallSlotId = com.android.contacts.ContactsUtils.getLastCallSlotId(mContext, number);
			    String recentCall = getString(R.string.aurora_recent_call);
			    String menuSlot0 = getString(R.string.aurora_slot_0) +  getString(R.string.gn_dial_desciption);
			    String menuSlot1 = getString(R.string.aurora_slot_1) +  getString(R.string.gn_dial_desciption);
			    if (lastCallSlotId == 0) {
			        menuSlot0 = menuSlot0 + recentCall;
			    } else if (lastCallSlotId == 1) {
			        menuSlot1 = menuSlot1 + recentCall;
			    }
			    AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);  
		        builder.setTitle(mContext.getString(R.string.menu_callNumber, number));

	           DialogInterface.OnClickListener singleListener =   
	               new DialogInterface.OnClickListener() {  
	                     
	                   @Override  
	                   public void onClick(DialogInterface dialogInterface,   
	                           int which) {  
	                	      mSelectSlot = which ;  
	                   }  
	               };           
	            
	           builder.setSingleChoiceItems(new String[]{menuSlot0, menuSlot1}, 0, singleListener);  
	           DialogInterface.OnClickListener btnListener =   
	               new DialogInterface.OnClickListener() {  
	                   @Override  
	                   public void onClick(DialogInterface dialogInterface, int which) {
	                		Intent intent = AuroraTelephoneManager.getCallNumberIntent(number, mSelectSlot);
							intent.putExtra("contactUri", mContactData.getUri());
							startActivity(intent);
	                   }  
	               };  
	           builder.setPositiveButton(android.R.string.ok, btnListener);  
	           mCallDialog = builder.create();   
	           mCallDialog.show();
	           return;
			}		

  	}
	startActivity(intent);
	 
}  
   
   private void showCallPopupWindow(Intent intent) {
	  	if(ContactsApplication.isMultiSimEnabled) {
	  		   boolean showDouble = com.android.contacts.ContactsUtils.isShowDoubleButton();
				if (showDouble && SubContactsUtils.simStateReady(0) && SubContactsUtils.simStateReady(1)) {
					final String number = PhoneNumberUtils.getNumberFromIntent(intent, this);
					  // 一个自定义的布局，作为显示的内容
				       View contentView = LayoutInflater.from(mContext).inflate(
				               R.layout.aurora_popup_menu, null);
				       
				       AuroraListView menulist = (AuroraListView)contentView.findViewById(R.id.menu_list);
				 
				       int lastCallSlotId = com.android.contacts.ContactsUtils.getLastCallSlotId(mContext, number);
				       String recentCall = getString(R.string.aurora_recent_call);
					    String menuSlot0 = getString(R.string.aurora_slot_0) +  getString(R.string.gn_dial_desciption);
					    String menuSlot1 = getString(R.string.aurora_slot_1) +  getString(R.string.gn_dial_desciption);
					    if (lastCallSlotId == 0) {
					        menuSlot0 = menuSlot0 + recentCall;
					    } else if (lastCallSlotId == 1) {
					        menuSlot1 = menuSlot1 + recentCall;
					    }
				       
				       menulist.setAdapter(new MenuAdapter(QuickContactActivity.this,R.layout.aurora_popup_menu_item, new String[]{menuSlot0, menuSlot1}));
				       
				       final PopupWindow popupWindow = new PopupWindow(contentView,
				               LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
				       
				       menulist.setOnItemClickListener(new OnItemClickListener() {
				       	@Override
				           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				       		Intent intent = AuroraTelephoneManager.getCallNumberIntent(number, position);
							intent.putExtra("contactUri", mContactData.getUri());
							mHasDialNumber=true;
							startActivity(intent);
				       	 	popupWindow.dismiss();
				       	}
						});

				       popupWindow.setTouchable(true);
				       
				       popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
				     
				       // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
				       // 我觉得这里是API的一个bug
				       popupWindow.setBackgroundDrawable(new BitmapDrawable());

				       // 设置好参数之后再show
				       popupWindow.showAtLocation (QuickContactActivity.this.findViewById(R.id.multiscroller), Gravity.BOTTOM, 0, 0 );
				       return;
				}
	  	}
	  	mHasDialNumber=true;
	    startActivity(intent);
   }
    
   
   private int mPhoneCount;
   private long mRawContactId = 0;
   private String mLastestNumber;
   
   private String getLastContactNumber() {
	    	String srotOrder = Calls.DATE + " DESC LIMIT 1";	    	
	    	String result  = null;
	        Log.d(TAG, "getLastContactNumber mRawContactId = " + mRawContactId);
	    	String selection =" raw_contact_id =" + mRawContactId;	    	
	    	Cursor c = getContentResolver().query(Calls.CONTENT_URI, new String[]{Calls.NUMBER}, 
	    			selection, null, srotOrder);    	    	
	    	if (c != null) {	    	
	    		if(c.moveToFirst()) {
		    		result = c.getString(0);
	    		}
		    	c.close();
		    	c = null;
	    	}	    	
	        Log.d(TAG, "getLastContactNumber number = " + result);
	    	return result;	    
   }
   
   private boolean mIsNoAnim = false;
   
	private void playRecord(final List<PhoneCallRecord> records) {
	    Log.d(TAG, "playRecord ");
    	if (null == records) {
    		return;
    	}
    	
    	final int size = records.size();
    	
    	if (size == 1) {
    		playRecord(records.get(0));
    		return;
    	}
    	
    	CharSequence[] items = new CharSequence[size];
    	for (int i = 0; i < items.length; i++) {
    		items[i] = new File(records.get(i).getPath()).getName().substring(0, 13) + ".3gpp";
    	}
    	AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
	    	.setTitle(R.string.gn_phone_call_record_tile).setTitleDividerVisible(true)
	    	.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					playRecord(records.get(which));
				}
			}).create();
        Window window = dialog.getWindow();  
        window.setWindowAnimations(R.style.aurora_dialog_anim); 
    	dialog.show();
    }
	
    private void playRecord(PhoneCallRecord record) {
        Log.d(TAG, "playRecord single");
    	if (null == record) {
    		return;
    	}
    	
    	Uri data = Uri.fromFile(new File(record.getPath()));  
        Intent intent = new Intent(Intent.ACTION_VIEW);  
        intent.setClassName("com.android.music", "com.android.music.AudioPreview");
        intent.setDataAndType(data, record.getMimeType());  
        startActivity(intent);
    }
   
	  private void showQrcodeImage() {
		String qrcode = getQrcodeString();
		Log.d(TAG, "showQrcodeImage = " + qrcode);
		  Map<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
	      hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
	      //图像数据转换，使用了矩阵转换
	      try {
	          BitMatrix bitMatrix = new QRCodeWriter().encode(qrcode, BarcodeFormat.QR_CODE, 400,400, hints);
	          int[] pixels = new int[400 * 400];
	          //下面这里按照二维码的算法，逐个生成二维码的图片，
	          //两个for循环是图片横列扫描的结果
	          for (int y = 0; y < 400; y++)
	          {
	              for (int x = 0; x < 400; x++)
	              {
	                  if (bitMatrix.get(x, y))
	                  {
	                      pixels[y * 400 + x] = 0xff000000;
	                  }
	                  else
	                  {
	                      pixels[y * 400 + x] = 0xffffffff;
	                  }
	              }
	          }
	          //生成二维码图片的格式，使用ARGB_8888
	          Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
	          bitmap.setPixels(pixels, 0, 400, 0, 0, 400, 400);
	          
	          
	//          Toast toast = Toast.makeText(mContext, "带图片的Toast", Toast.LENGTH_LONG); 
	//          toast.setGravity(Gravity.CENTER, 0, 0);    
	//          LinearLayout toastView = (LinearLayout) toast.getView();    
	//          ImageView imageCodeProject = new ImageView(mContext);  
	//          imageCodeProject.setImageBitmap(bitmap);   
	//          toastView.addView(imageCodeProject, 0);    	          
	//          toast.show();
	          
	          if(myDialog != null) {
	        	  myDialog.dismiss();
	        	  myDialog = null;
	          }
	          myDialog = new AlertDialog.Builder(this).create();
//	          myDialog.setContentView(R.layout.aurora_qrcode_dialog);
	          myDialog.show();  
	          myDialog.getWindow().setContentView(R.layout.aurora_qrcode_dialog);  

	          myDialog.getWindow().setGravity(Gravity.CENTER);
	          ImageView v = (ImageView)myDialog.getWindow().findViewById(R.id.qrcode_image); 
	          v.setImageBitmap(bitmap);
//	          myDialog.show();
	          
	      } catch(Exception e) {
	    	  e.printStackTrace();
	      }
	      
	}
	
	private AlertDialog myDialog = null; 
	private Uri simOrPhoneUri;
	
	private String getQrcodeString() {
	//	 VCardComposer composer = new VCardComposer(this, VCardConfig.getVCardTypeFromString(getResources().getString(R.string.config_export_vcard_type)), true);
		VCardComposer composer = new VCardComposer(this, VCardConfig.VCARD_TYPE_V30_GENERIC | VCardConfig.FLAG_REFRAIN_IMAGE_EXPORT, true);    	
		    final Uri contentUriForRawContactsEntity = RawContactsEntity.CONTENT_URI.buildUpon()
	                .appendQueryParameter(RawContactsEntity.FOR_EXPORT_ONLY, "1")
	                .build();
		   if (!composer.init(simOrPhoneUri, null,
	               null, null, null, contentUriForRawContactsEntity)) {
			   final String errorReason = composer.getErrorReason();
			   final String translatedErrorReason =
	                   translateComposerError(errorReason);
			   return "";
		   }
		   
		   if (!composer.isAfterLast()) {
			   return composer.createOneEntry();
		   }
		   
		   return "";
	
	}
	
	private String translateComposerError(String errorMessage) {
	    final Resources resources = getResources();
	    if (VCardComposer.FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO.equals(errorMessage)) {
	        return resources.getString(R.string.composer_failed_to_get_database_infomation);
	    } else if (VCardComposer.FAILURE_REASON_NO_ENTRY.equals(errorMessage)) {
	        return resources.getString(R.string.composer_has_no_exportable_contact);
	    } else if (VCardComposer.FAILURE_REASON_NOT_INITIALIZED.equals(errorMessage)) {
	        return resources.getString(R.string.composer_not_initialized);
	    } else {
	        return errorMessage;
	    }
	}
	
	   private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
		        final TextView header = (TextView) v.findViewById(R.id.header);
		        String number = header.getText().toString();
	    		AuroraAlertDialog dialog = (AuroraAlertDialog) createLongClickDialog(number);
	            Window window = dialog.getWindow();  
	            window.setWindowAnimations(R.style.aurora_dialog_anim); 
	    		dialog.show();
	    		return false;
			}        	
       };
	   
		private AuroraAlertDialog createLongClickDialog(final String number) {
			ArrayList<String> itemList = new ArrayList<String>();		        
	        itemList.add(getResources().getString(R.string.copy_text));
	        itemList.add(getResources().getString(R.string.gn_edit_number_before_call));        
	        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);        
			AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this.mContext)
	        .setTitle(number)
	        .setItems(items, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
			        switch (which) {
			            case 0:
			            	gnCopyToClipboard(number);
			                break;
			            case 1:
			            	startActivity(com.android.contacts.ContactsUtils.getEditNumberBeforeCallIntent(number));
			                break;
			            default:
			                break;
			        }
				}
			})
			.setTitleDividerVisible(true)
	        .setCancelIcon(true)
	        .create();
			
	        Window window = dialog.getWindow();  
	        window.setWindowAnimations(R.style.aurora_dialog_anim); 
	        return dialog;
		}
		
		 private void gnCopyToClipboard(String number) {
		        if (TextUtils.isEmpty(number)) return;	        
				ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setPrimaryClip(ClipData.newPlainText(null, number));
		        String toastText = getString(R.string.toast_text_copied);
		        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
		    }
}
