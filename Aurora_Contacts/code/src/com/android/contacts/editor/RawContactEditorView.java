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

package com.android.contacts.editor;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountType.EditType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.EntityModifier;
import com.mediatek.contacts.util.Objects;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Organization;
import gionee.provider.GnContactsContract.CommonDataKinds.Photo;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import aurora.widget.AuroraButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

// The following lines are provided and maintained by Mediatek Inc.
// Description: for SIM name display
import android.util.Log;
import java.util.List;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.util.OperatorUtils;
// The previous lines are provided and maintained by Mediatek Inc.

//Gionee:wangth 20120414 modify for CR00572641 begin
import com.android.contacts.ContactsUtils;
//Gionee:wangth 20120414 modify for CR00572641 end
//gionee yeweiqun 2012.10.10 modify for CR00710328 begin
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import android.os.RemoteException;
import com.android.internal.telephony.IIccPhoneBook;
//Gionee <wangth><2013-05-03> modify for CR00805658 begin
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
//Gionee <wangth><2013-05-03> modify for CR00805658 end
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
//gionee yeweiqun 2012.10.10 modify for CR00710328 end

/**
 * Custom view that provides all the editor interaction for a specific
 * {@link Contacts} represented through an {@link EntityDelta}. Callers can
 * reuse this view and quickly rebuild its contents through
 * {@link #setState(EntityDelta, AccountType, ViewIdGenerator)}.
 * <p>
 * Internal updates are performed against {@link ValuesDelta} so that the
 * source {@link Entity} can be swapped out. Any state-based changes, such as
 * adding {@link Data} rows or changing {@link EditType}, are performed through
 * {@link EntityModifier} to ensure that {@link AccountType} are enforced.
 */
public class RawContactEditorView extends BaseRawContactEditorView {
    private LayoutInflater mInflater;

    private StructuredNameEditorView mName;
    private PhoneticNameEditorView mPhoneticName;
    private AuroraGroupMembershipView mGroupMembershipView;
    private GnRingtoneEditorView mGnRingtoneEditorView;

    private ViewGroup mFields;

    private ImageView mAccountIcon;
    private TextView mAccountTypeTextView;
    private TextView mAccountNameTextView;

    private AuroraButton mAddFieldButton;

    private long mRawContactId = -1;
    private boolean mAutoAddToDefaultGroup = true;
    private Cursor mGroupMetaData;
    private DataKind mGroupMembershipKind;
    private EntityDelta mState;

    private boolean mPhoneticNameAdded;

    // The following lines are provided and maintained by Mediatek Inc.
    // Description: for SIM name display
    private List<AccountWithDataSet> mAccounts = null;
    // The previous lines are provided and maintained by Mediatek Inc.

    public RawContactEditorView(Context context) {
        super(context);
        // The following lines are provided and maintained by Mediatek Inc.
        // Description: for SIM name display
        initMembers(context);
        // The previous lines are provided and maintained by Mediatek Inc.
    }

    public RawContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // The following lines are provided and maintained by Mediatek Inc.
        // Description: for SIM name display
        initMembers(context);
        // The previous lines are provided and maintained by Mediatek Inc.
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        View view = getPhotoEditor();
        if (view != null) {
            view.setEnabled(enabled);
        }

        if (mName != null) {
            mName.setEnabled(enabled);
        }

        if (mPhoneticName != null) {
            mPhoneticName.setEnabled(enabled);
        }

        if (mFields != null) {
            int count = mFields.getChildCount();
            for (int i = 0; i < count; i++) {
                mFields.getChildAt(i).setEnabled(enabled);
            }
        }

        if (mGroupMembershipView != null) {
            mGroupMembershipView.setEnabled(enabled);
        }
        
        if (null != mGnRingtoneEditorView) {
        	mGnRingtoneEditorView.setEnabled(enabled);
        }
        /*
         * New Feature by Mediatek Begin. 
         * Original Android's code:
         * mAddFieldButton.setEnabled(enabled);
         * Descriptions: crete sim/usim contact
         */
        if (mAddFieldButton != null) {
            mAddFieldButton.setEnabled(enabled);
        } 
        /*
         * Change Feature by Mediatek End.
         */
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mName = (StructuredNameEditorView)findViewById(R.id.edit_name);
        mName.setDeletable(false);

        mPhoneticName = (PhoneticNameEditorView)findViewById(R.id.edit_phonetic_name);
        if (null != mPhoneticName) {
        	mPhoneticName.setDeletable(true);
        }

        mFields = (ViewGroup)findViewById(R.id.sect_fields);

        mAccountIcon = (ImageView) findViewById(R.id.account_icon);
        mAccountTypeTextView = (TextView) findViewById(R.id.account_type);
        mAccountNameTextView = (TextView) findViewById(R.id.account_name);

        mAddFieldButton = (AuroraButton) findViewById(R.id.button_add_field);
        /*
         * New Feature by Mediatek Begin. 
         * Original Android's code:
         * mAddFieldButton.setOnClickListener(new OnClickListener() {
         * @Override public void onClick(View v) {
         * showAddInformationPopupWindow(); } }); 
         * Descriptions: crete sim/usim contact
         */
        if (mAddFieldButton != null) {
            mAddFieldButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
				// aurora <ukiliu> <2013-9-17> modify for auroro ui begin
//                    showAddInformationPopupWindow();
                	showAddInformationDialog();
				// aurora <ukiliu> <2013-9-17> modify for auroro ui end
                }
            });
        } 
        /*
         * Change Feature by Mediatek End.
         */
    }

    /**
     * Set the internal state for this view, given a current
     * {@link EntityDelta} state and the {@link AccountType} that
     * apply to that state.
     */
    @Override
    public void setState(EntityDelta state, AccountType type, ViewIdGenerator vig,
            boolean isProfile) {

        mState = state;

        // Remove any existing sections
        mFields.removeAllViews();
        Log.d(TAG, "[setState]start mFields.size(): " + mFields.getChildCount());

        // Bail if invalid state or account type
        if (state == null || type == null) return;

        setId(vig.getId(state, null, null, ViewIdGenerator.NO_VIEW_INDEX));

        // Make sure we have a StructuredName
        EntityModifier.ensureKindExists(state, type, StructuredName.CONTENT_ITEM_TYPE);

        ValuesDelta values = state.getValues();
        mRawContactId = values.getAsLong(RawContacts._ID);

        // The following lines are provided and maintained by Mediatek Inc.
        // Description: for SIM name display
        String accountDisplayName = null;
        String accountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        Log.d(TAG, "[setState]got mRawContactId " + mRawContactId + " from AccountName: " + accountName + " isProfile: " + isProfile);
        // The previous lines are provided and maintained by Mediatek Inc.

        // Fill in the account info
        if (isProfile) {
            // The following lines are provided and maintained by Mediatek Inc.
            // Description: for SIM name display, and accountName defined above
            // Keep previous code here.
            // String accountName = values.getAsString(RawContacts.ACCOUNT_NAME);
            // The previous lines are provided and maintained by Mediatek inc.
            if (TextUtils.isEmpty(accountName)) {
                mAccountNameTextView.setVisibility(View.GONE);
                mAccountTypeTextView.setText(R.string.local_profile_title);
            } else {
                CharSequence accountType = type.getDisplayLabel(mContext);
                mAccountTypeTextView.setText(mContext.getString(R.string.external_profile_title,
                        accountType));
                mAccountNameTextView.setText(accountName);
            }
        } else {
            // The following lines are provided and maintained by Mediatek Inc.
            // Description: for SIM name display, and accountName defined above
            // Keep previous code here.
            // String accountName = values.getAsString(RawContacts.ACCOUNT_NAME);
            // The previous lines are provided and maintained by Mediatek inc.
            CharSequence accountType = type.getDisplayLabel(mContext);

			if (TextUtils.isEmpty(accountType)
					|| TextUtils.isEmpty(type.accountType)
					|| AccountType.ACCOUNT_TYPE_LOCAL_PHONE
							.equals(type.accountType)) {
				accountType = mContext.getString(R.string.gn_account_phone);
			}

			// Gionee <xuhz> <2013-08-09> modify for CR00850854 begin
			if (!TextUtils.isEmpty(accountName)
					&& !TextUtils.isEmpty(type.accountType)
					&& AccountType.ACCOUNT_TYPE_LOCAL_PHONE
							.endsWith(type.accountType)) {
				// Gionee <xuhz> <2013-08-09> modify for CR00850854 end
				accountName = null;
			}
            
            
            if (!TextUtils.isEmpty(accountName)) {
            	
                mAccountNameTextView.setVisibility(View.VISIBLE);
                //Gionee:wangth 20120414 modify for CR00572641 begin
                /*
                mAccountNameTextView.setText(
                        mContext.getString(R.string.from_account_format, accountName));
                */
                if (ContactsUtils.mIsGnContactsSupport 
                        && AccountType.ACCOUNT_TYPE_LOCAL_PHONE.equals(type.accountType)) {
                    String accountText = mContext.getString(R.string.gn_phone_text);
                    mAccountNameTextView.setText(accountText);
                } else {
                    mAccountNameTextView.setText(
                            mContext.getString(R.string.from_account_format, accountName));
                }
                //Gionee:wangth 20120414 modify for CR00572641 end
                
                // gionee xuhz 20121211 add for GIUI2.0 CR00738457 start
                if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                    mAccountTypeTextView.setVisibility(View.GONE);
                    mAccountNameTextView.setText(
                            mContext.getString(R.string.from_account_format, accountName));
                }
                // gionee xuhz 20121211 add for GIUI2.0 CR00738457 end
            } else {
                // Hide this view so the other text view will be centered vertically
                mAccountNameTextView.setVisibility(View.GONE);
                // gionee xuhz 20121211 add for GIUI2.0 CR00738457 start
                if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                    mAccountTypeTextView.setText(
                            mContext.getString(R.string.account_type_format, accountType));
                }
                // gionee xuhz 20121211 add for GIUI2.0 CR00738457 end
            }
            // gionee xuhz 20121211 modify for GIUI2.0 CR00738457 start
            if (!ContactsApplication.sIsGnGGKJ_V2_0Support) {
                mAccountTypeTextView.setText(
                        mContext.getString(R.string.account_type_format, accountType));
            }
            // gionee xuhz 20121211 modify for GIUI2.0 CR00738457 end
        }
        Log.d(TAG, "[setState]got mRawContactId " + mRawContactId + " from AccountName: " + accountName);
        Log.i(TAG,"[Rawcontacteditorview] setstate");
        /*
         * Change feature by Mediatek Begin.
         *   Original Android's code:
         *     mAccountIcon.setImageDrawable(type.getDisplayIcon(mContext));
         *   CR ID: ALPS00233786
         *   Descriptions: cu feature change photo by slot id 
         */
        // Gionee <xuhz> <2013-08-16> modify for CR00858149 begin
        //old:if (OperatorUtils.getOptrProperties().equals("OP02") && isSimUsimAccountType(type.accountType)) {
        if (OperatorUtils.getActualOptrProperties().equals("OP02") && isSimUsimAccountType(type.accountType)) {
        // Gionee <xuhz> <2013-08-16> modify for CR00858149 end
            Log.i("checkphoto","RawContactEditorview slotIdForCU : "+slotIdForCU);
            mAccountIcon.setImageDrawable(type.getDisplayIconBySlotId(mContext, slotIdForCU));
            slotIdForCU = -1;
        }
        //Gionee lihuafang 2012-06-25 add for CR00628322 begin
        else if (ContactsUtils.mIsGnShowDigitalSlotSupport||ContactsUtils.mIsGnShowSlotSupport) {
            mAccountIcon.setImageDrawable(type.getDisplayIconBySlotId(mContext, slotIdForCU));
            slotIdForCU = -1;
        }
        //Gionee lihuafang 2012-06-25 add for CR00628322 end
        else {
            mAccountIcon.setImageDrawable(type.getDisplayIcon(mContext));
        }
        /*
         * Change Feature by Mediatek End.
         */
        // Show photo editor when supported
        EntityModifier.ensureKindExists(state, type, Photo.CONTENT_ITEM_TYPE);
        setHasPhotoEditor((type.getKindForMimetype(Photo.CONTENT_ITEM_TYPE) != null));
        getPhotoEditor().setEnabled(isEnabled());
        mName.setEnabled(isEnabled());

        if (null != mPhoneticName) {
        	mPhoneticName.setEnabled(false);
        }

        // Show and hide the appropriate views
        mFields.setVisibility(View.VISIBLE);
        mName.setVisibility(View.VISIBLE);
        
        if (null != mPhoneticName) {
        	mPhoneticName.setVisibility(View.GONE);
        }

        mGroupMembershipKind = type.getKindForMimetype(GroupMembership.CONTENT_ITEM_TYPE);
        if (mGroupMembershipKind != null) {
//        	int resId = ContactsApplication.sIsGnGGKJ_V2_0Support ? 
//        			R.layout.aurora_groupmembership_editor : R.layout.item_group_membership;
            mGroupMembershipView = (AuroraGroupMembershipView)mInflater.inflate(
            		R.layout.aurora_editor_groupmembership, mFields, false);
            mGroupMembershipView.setKind(mGroupMembershipKind);
            mGroupMembershipView.setEnabled(isEnabled());
        }
        
        if (!isSimUsimAccountType(type.accountType)) {
        	mGnRingtoneEditorView = (GnRingtoneEditorView)mInflater.inflate(
            		R.layout.aurora_editor_ringtone, mFields, false);
        	mGnRingtoneEditorView.setEnabled(isEnabled());
        }
		
		//gionee yeweiqun 2012.10.10 modify for CR00710328 begin
		/*Added 1 start*/
		boolean hasEmailFile = true;
		boolean hasAnrFile = true;
		/*Added 1 end*/
		//gionee yeweiqun 2012.10.10 modify for CR00710328 end
		Log.d(TAG, "[setState]middle mFields.size(): " + mFields.getChildCount());
        // Create editor sections for each possible data kind
        for (DataKind kind : type.getSortedDataKinds()) {
            // Skip kind of not editable
            if (!kind.editable) continue;

            final String mimeType = kind.mimeType;
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for structured name
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                mName.setValues(
                        type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME),
                        primary, state, false, vig);
                if (null != mPhoneticName) {
                	mPhoneticName.setValues(
                            type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME),
                            primary, state, false, vig);	
                }
            } else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for photos
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                getPhotoEditor().setValues(kind, primary, state, false, vig);
            } else if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                if (mGroupMembershipView != null) {
                    mGroupMembershipView.setState(state);
                }
            } else if (!ContactsApplication.sIsGnGGKJ_V2_0Support &&
            		Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Create the organization section
                // gionee xuhz 20120604 xuhz modify for CR00614796 start
                final KindSectionView section;
                if (ContactsApplication.sIsGnContactsSupport) {
                    section = (KindSectionView) mInflater.inflate(
                            R.layout.gn_item_kind_section, mFields, false);
                    ImageView addImage = (ImageView)section.findViewById(R.id.add_image);
                    if (ContactsApplication.sIsGnDarkStyle) {
                        addImage.setImageResource(R.drawable.gn_edit_add_field_image_light);
                    } else {
                        addImage.setImageResource(R.drawable.gn_edit_add_field_image_dark);
                    }
                } else {
                    section = (KindSectionView) mInflater.inflate(
                            R.layout.item_kind_section, mFields, false);
                }
                // gionee xuhz 20120604 xuhz modify for CR00614796 end
                
                section.setTitleVisible(false);
                section.setEnabled(isEnabled());
                section.setState(kind, state, false, vig);

                // If there is organization info for the contact already, display it
                if (!section.isEmpty()) {
                    mFields.addView(section);
                } else {
                    // Otherwise provide the user with an "add organization" button that shows the
                    // EditText fields only when clicked
                	
                    // gionee xuhz 20120604 xuhz modify for CR00614796 start
                    final View organizationView;
                    if (ContactsApplication.sIsGnContactsSupport) {
                        organizationView = mInflater.inflate(
                                R.layout.gn_organization_editor_view_switcher, mFields, false);
                        ImageView addOrganizationImage = (ImageView)organizationView.findViewById(R.id.add_organization_image);
                        if (ContactsApplication.sIsGnDarkStyle) {
                            addOrganizationImage.setImageResource(R.drawable.gn_edit_add_field_image_light);
                        } else {
                            addOrganizationImage.setImageResource(R.drawable.gn_edit_add_field_image_dark);
                        }
                    } else {
                        organizationView = mInflater.inflate(
                                R.layout.organization_editor_view_switcher, mFields, false);
                    }
                    // gionee xuhz 20120604 xuhz modify for CR00614796 end
                    
                    final View addOrganizationButton = organizationView.findViewById(
                            R.id.add_organization_button);
                    final ViewGroup organizationSectionViewContainer =
                            (ViewGroup) organizationView.findViewById(R.id.container);

                    organizationSectionViewContainer.addView(section);

                    // Setup the click listener for the "add organization" button
                    addOrganizationButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Once the user expands the organization field, the user cannot
                            // collapse them again.
                            addOrganizationButton.setVisibility(View.GONE);
                            organizationSectionViewContainer.setVisibility(View.VISIBLE);
                            organizationSectionViewContainer.requestFocus();
                        }
                    });

                    mFields.addView(organizationView);
                }
            } else {
            	
                if (kind.fieldList == null) continue;
                final KindSectionView section;
                if (ContactsApplication.auroraContactsSupport) {
                    section = (KindSectionView)mInflater.inflate(R.layout.aurora_item_kind_section, mFields, false);
                } else {
                    section = (KindSectionView)mInflater.inflate(R.layout.item_kind_section, mFields, false);
                }
                
                section.setEnabled(isEnabled());
                section.setState(kind, state, false, vig);
                mFields.addView(section);
            }
        }
        
        if (mGroupMembershipView != null && !AuroraContactEditorFragment.mIsPrivacyContact) { // aurora wangth 20140929 modify
            mFields.addView(mGroupMembershipView);
        }
        
        if (null != mGnRingtoneEditorView) {
        	mFields.addView(mGnRingtoneEditorView);        	
        }

        /*
         * New Feature by Mediatek Begin. 
         * Original Android's code:
         * updatePhoneticNameVisibility(); 
         * CR ID: ALPS00101852 
         * Descriptions: new SIM & USIM UI when create new account.
         */
        Log.i(TAG,"type : "+type.accountType);
        
        Log.d(TAG, "[setState]end mFields.size(): " + mFields.getChildCount());
        
        if (type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME) != null) {
            updatePhoneticNameVisibility();
        } 
        else if (type.accountType.equals(mUsimAccountType) || type.accountType.equals(mSimAccountType)) {
           
                Log.i(TAG,"***************");
                simTypeSetState(mFields);
               
            
        }
        /*
         * Change Feature by Mediatek End.
         */
        addToDefaultGroupIfNeeded();
        /*
         * New Feature by Mediatek Begin. 
         * Original Android's code:
         * mAddFieldButton.setEnabled(enabled);
         * Descriptions: crete sim/usim contact
         */
        final int sectionCount = getSectionViewsWithoutFields().size();
        
        if (mAddFieldButton != null) {
            mAddFieldButton.setVisibility(sectionCount > 0 ? View.VISIBLE : View.GONE);
            mAddFieldButton.setEnabled(isEnabled());
        }
        /*
         * Change Feature by Mediatek End.
         */
    }

	@Override
    public void setGroupMetaData(Cursor groupMetaData) {
        mGroupMetaData = groupMetaData;
        addToDefaultGroupIfNeeded();
        if (mGroupMembershipView != null) {
            mGroupMembershipView.setGroupMetaData(groupMetaData);
        }
    }

    public void setAutoAddToDefaultGroup(boolean flag) {
        this.mAutoAddToDefaultGroup = flag;
    }

    /**
     * If automatic addition to the default group was requested (see
     * {@link #setAutoAddToDefaultGroup}, checks if the raw contact is in any
     * group and if it is not adds it to the default group (in case of Google
     * contacts that's "My Contacts").
     */
    private void addToDefaultGroupIfNeeded() {
        if (!mAutoAddToDefaultGroup || mGroupMetaData == null || mGroupMetaData.isClosed()
                || mState == null) {
            return;
        }

        boolean hasGroupMembership = false;
        ArrayList<ValuesDelta> entries = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
        if (entries != null) {
            for (ValuesDelta values : entries) {
                Long id = values.getAsLong(GroupMembership.GROUP_ROW_ID);
                if (id != null && id.longValue() != 0) {
                    hasGroupMembership = true;
                    break;
                }
            }
        }

        if (!hasGroupMembership) {
            long defaultGroupId = getDefaultGroupId();
            if (defaultGroupId != -1) {
                ValuesDelta entry = EntityModifier.insertChild(mState, mGroupMembershipKind);
                entry.put(GroupMembership.GROUP_ROW_ID, defaultGroupId);
            }
        }
    }

    /**
     * Returns the default group (e.g. "My Contacts") for the current raw contact's
     * account.  Returns -1 if there is no such group.
     */
    private long getDefaultGroupId() {
        String accountType = mState.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
        String accountName = mState.getValues().getAsString(RawContacts.ACCOUNT_NAME);
        String accountDataSet = mState.getValues().getAsString(RawContacts.DATA_SET);
        mGroupMetaData.moveToPosition(-1);
        while (mGroupMetaData.moveToNext()) {
            String name = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_NAME);
            String type = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
            String dataSet = mGroupMetaData.getString(GroupMetaDataLoader.DATA_SET);
            if (name.equals(accountName) && type.equals(accountType)
                    && Objects.equal(dataSet, accountDataSet)) {
                long groupId = mGroupMetaData.getLong(GroupMetaDataLoader.GROUP_ID);
                if (!mGroupMetaData.isNull(GroupMetaDataLoader.AUTO_ADD)
                            && mGroupMetaData.getInt(GroupMetaDataLoader.AUTO_ADD) != 0) {
                    return groupId;
                }
            }
        }
        return -1;
    }

    public TextFieldsEditorView getNameEditor() {
        return mName;
    }

    public TextFieldsEditorView getPhoneticNameEditor() {
        return mPhoneticName;
    }

    private void updatePhoneticNameVisibility() {
    	if (null == mPhoneticName) {
    		return;
    	}
    	
        boolean showByDefault =
                getContext().getResources().getBoolean(R.bool.config_editor_include_phonetic_name);

        if (showByDefault || mPhoneticName.hasData() || mPhoneticNameAdded) {
            mPhoneticName.setVisibility(View.VISIBLE);
        } else {
            mPhoneticName.setVisibility(View.GONE);
        }
    }

    @Override
    public long getRawContactId() {
        return mRawContactId;
    }
    
    /**
     * Return a list of KindSectionViews that have no fields yet...
     * these are candidates to have fields added in
     * {@link #showAddInformationPopupWindow()}
     */
    private ArrayList<KindSectionView> getSectionViewsWithoutFields() {
        final ArrayList<KindSectionView> fields =
                new ArrayList<KindSectionView>(mFields.getChildCount());
        for (int i = 0; i < mFields.getChildCount(); i++) {
            View child = mFields.getChildAt(i);
            if (child instanceof KindSectionView) {
                final KindSectionView sectionView = (KindSectionView) child;
                // If the section is already visible (has 1 or more editors), then don't offer the
                // option to add this type of field in the popup menu
                if (sectionView.getEditorCount() > 0) {
                    continue;
                }
                DataKind kind = sectionView.getKind();
                // not a list and already exists? ignore
                if ((kind.typeOverallMax == 1) && sectionView.getEditorCount() != 0) {
                    continue;
                }
                if (DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME.equals(kind.mimeType)) {
                    continue;
                }

                if (DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(kind.mimeType)) {
//                        && mPhoneticName.getVisibility() == View.VISIBLE) {/*delete mPhoneticName setting*/
                    continue;
                }

                fields.add(sectionView);
            }
        }
        return fields;
    }
    
    private void showAddInformationPopupWindow() {
        final ArrayList<KindSectionView> fields = getSectionViewsWithoutFields();
        final PopupMenu popupMenu = new PopupMenu(getContext(), mAddFieldButton);
        final Menu menu = popupMenu.getMenu();
        for (int i = 0; i < fields.size(); i++) {
            menu.add(Menu.NONE, i, Menu.NONE, fields.get(i).getTitle());
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final KindSectionView view = fields.get(item.getItemId());
                if (DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(view.getKind().mimeType)) {
                    mPhoneticNameAdded = true;
                    updatePhoneticNameVisibility();
                } else {
                    view.addItemRequestFocus();
                }

                // If this was the last section without an entry, we just added one, and therefore
                // there's no reason to show the button.
                if (fields.size() == 1 && mAddFieldButton != null) {
                    mAddFieldButton.setVisibility(View.GONE);
                }

                return true;
            }
        });

        popupMenu.show();
    }
    
	private void showAddInformationDialog() {
		final ArrayList<KindSectionView> fields = getSectionViewsWithoutFields();

		ArrayList<String> titles = new ArrayList<String>();
		for (int i = 0; i < fields.size(); i++) {
			titles.add(fields.get(i).getTitle());
		}

		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(getContext())
				.setItems(titles.toArray(new String[] {}),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								final KindSectionView view = fields.get(which);
								if (DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME
										.equals(view.getKind().mimeType)) {
									mPhoneticNameAdded = true;
									updatePhoneticNameVisibility();
								} else {
									view.addItemRequestFocus();
								}

								// If this was the last section without an
								// entry, we just added one, and therefore
								// there's no reason to show the button.
								if (fields.size() == 1
										&& mAddFieldButton != null) {
									mAddFieldButton.setVisibility(View.GONE);
								}
							}
						}).create();

		dialog.show();
	}

    // The following lines are provided and maintained by Mediatek Inc.
    // Description: for SIM name display
    private void initMembers(Context context) {
        mAccounts = AccountTypeManager.getInstance(context).getAccounts(true);
    }

    private boolean isSimUsimAccountType(String accountType) {
        boolean bRet = false;
        if (AccountType.ACCOUNT_TYPE_SIM.equals(accountType)  
                || AccountType.ACCOUNT_TYPE_USIM.equals(accountType)) {
            bRet = true;
        }
        return bRet;
    }
    // The previous lines are provided and maintained by Mediatek inc.
	/*
    * New Feature by Mediatek Begin.
    *   Original Android¡¯s code:
    *     
    *   CR ID: 
    *   Descriptions: ¡­
    */
    private String mUsimAccountType =  AccountType.ACCOUNT_TYPE_USIM;
    private String mSimAccountType = AccountType.ACCOUNT_TYPE_SIM;
    private static final String TAG = "RawContactEditorView";
    private static int slotIdForCU =-1;
    private void simTypeSetState(ViewGroup fields){
        Log.i(TAG,"************** INT I = "+fields.getChildCount());
        for (int i = 0; i < fields.getChildCount(); i++) {
            View child = fields.getChildAt(i);
            if (child instanceof KindSectionView) {
                final KindSectionView sectionView = (KindSectionView) child;
                // If the section is already visible (has 1 or more editors), then don't offer the
                // option to add this type of field in the popup menu
                if (sectionView.getEditorCount() > 0) {
                    continue;
                }
                DataKind kind = sectionView.getKind();
                // not a list and already exists? ignore
                if ((kind.typeOverallMax == 1) && sectionView.getEditorCount() != 0) {
                    continue;
                }
                if (DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME.equals(kind.mimeType)) {
                    continue;
                }

                if (DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(kind.mimeType)
                        && null != mPhoneticName && mPhoneticName.getVisibility() == View.VISIBLE) {
                    continue;
                }
                Log.i(TAG,"the child :" +child);
                ((KindSectionView) child).addItem();
            }
        }
        
        
    }
   /*
    * New Feature by Mediatek End.
    */
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	super.onLayout(changed, l, t, r, b);
    	
    	/*if (ContactsApplication.sIsGnContactsSupport) {
    		Rect rect = new Rect();
    		View view = findViewById(R.id.editors);
    		if (null != view) {
    			view.getGlobalVisibleRect(rect); 
            	view = findViewById(R.id.add_organization_button);
            	if (null != view) {
            		view.setPadding(rect.left + 2, 0, 0, 0);
            	}
    		}
        }*/
    }
    
    //gionee yeweiqun 2012.10.10 modify for CR00710328 begin
    public static final String SIMPHONEBOOK_SERVICE = "simphonebook";
	public static final String SIMPHONEBOOK2_SERVICE = "simphonebook2";
	public static final int GEMINI_SLOT1 = 0;
	public static final int GEMINI_SLOT2 = 1;

	public static IIccPhoneBook getIIccPhoneBook(int slotId) {
		String serviceName;
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			serviceName = (slotId == GEMINI_SLOT2) ? SIMPHONEBOOK2_SERVICE : SIMPHONEBOOK_SERVICE;
		} else {
			serviceName = SIMPHONEBOOK_SERVICE;
		}
		final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService(serviceName));
		return iIccPhb;
	}

	public static boolean hasEmailFile(int slotId) throws RemoteException{
		/*final IIccPhoneBook ilccPhb = getIIccPhoneBook(slotId);
		return ilccPhb.hasEmailFile();*/
		return false;
	}

	public static boolean hasAnrFile(int slotId) throws RemoteException{
		/*final IIccPhoneBook ilccPhb = getIIccPhoneBook(slotId);
		return ilccPhb.hasAnrFile();*/
		return false;
	}

	public static boolean hasGroupFile(int slotId) throws RemoteException{
		/*final IIccPhoneBook ilccPhb = getIIccPhoneBook(slotId);
		return ilccPhb.hasGroupFile();*/
		return false;
	}
    //gionee yeweiqun 2012.10.10 modify for CR00710328 end

	public GnRingtoneEditorView getGnRingtoneEditorView() {
		return mGnRingtoneEditorView;
	}
}
