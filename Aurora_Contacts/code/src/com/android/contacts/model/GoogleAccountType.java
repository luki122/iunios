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

package com.android.contacts.model;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.model.AccountType.DefinitionException;
import com.android.contacts.model.AccountType.EditType;
import com.android.contacts.model.BaseAccountType.Weight;
import com.android.contacts.util.DateUtils;
import com.google.android.collect.Lists;

import android.content.ContentValues;
import android.content.Context;
import android.net.sip.SipManager;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.Event;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Relation;
import android.util.Log;

import java.util.List;

public class GoogleAccountType extends BaseAccountType {
    private static final String TAG = "GoogleAccountType";

    public static final String ACCOUNT_TYPE = "com.google";

    private static final List<String> mExtensionPackages =
            Lists.newArrayList("com.google.android.apps.plus");

    public GoogleAccountType(Context context, String resPackageName) {
        this.accountType = ACCOUNT_TYPE;
        this.resPackageName = null;
        this.summaryResPackageName = resPackageName;

        try {
            addDataKindStructuredName(context);
            addDataKindDisplayName(context);
            addDataKindPhoneticName(context);
            addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            addDataKindStructuredPostal(context);
            addDataKindIm(context);
            addDataKindOrganization(context);
            addDataKindPhoto(context);
            addDataKindNote(context);
            addDataKindWebsite(context);
            if (SipManager.isVoipSupported(context)) {
                addDataKindSipAddress(context);
            }
            addDataKindGroupMembership(context);
            addDataKindRelation(context);
            addDataKindEvent(context);

            mIsInitialized = true;
        } catch (DefinitionException e) {
            Log.e(TAG, "Problem building account type", e);
        }
    }

    @Override
    public List<String> getExtensionPackageNames() {
        return mExtensionPackages;
    }

    @Override
    protected DataKind addDataKindPhone(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindPhone(context);

        kind.typeColumn = Phone.TYPE;
        kind.typeList = Lists.newArrayList();
        // aurora <wangth> <2014-1-9> modify for aurora begin
        /*
        kind.typeList.add(buildPhoneType(Phone.TYPE_MOBILE));
        kind.typeList.add(buildPhoneType(Phone.TYPE_WORK));
        kind.typeList.add(buildPhoneType(Phone.TYPE_HOME));
        kind.typeList.add(buildPhoneType(Phone.TYPE_MAIN));
        kind.typeList.add(buildPhoneType(Phone.TYPE_FAX_WORK).setSecondary(true));
        kind.typeList.add(buildPhoneType(Phone.TYPE_FAX_HOME).setSecondary(true));
        kind.typeList.add(buildPhoneType(Phone.TYPE_PAGER).setSecondary(true));
        kind.typeList.add(buildPhoneType(Phone.TYPE_OTHER));
        kind.typeList.add(buildPhoneType(Phone.TYPE_CUSTOM).setSecondary(true)
                .setCustomColumn(Phone.LABEL));
        */
        kind.typeList.add(new EditType(Phone.TYPE_MOBILE, R.string.telType_Moblie));
        kind.typeList.add(new EditType(Phone.TYPE_WORK, R.string.telType_Work));
        kind.typeList.add(new EditType(Phone.TYPE_HOME, R.string.telType_Home));
        kind.typeList.add(new EditType(Phone.TYPE_MAIN, R.string.telType_Main));
        kind.typeList.add(new EditType(Phone.TYPE_FAX_WORK, R.string.telType_Fax));
        kind.typeList.add(new EditType(Phone.TYPE_FAX_HOME, R.string.telType_FaxHome).setSecondary(true));
        kind.typeList.add(new EditType(Phone.TYPE_PAGER, R.string.telType_Pager).setSecondary(true));
        kind.typeList.add(new EditType(Phone.TYPE_OTHER, R.string.othertype));
        kind.typeList.add(new EditType(Phone.TYPE_CUSTOM, R.string.telType_custom).setSecondary(true)
                .setCustomColumn(Phone.LABEL));
        // aurora <wangth> <2014-1-9> modify for aurora end

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));

        return kind;
    }

    @Override
    protected DataKind addDataKindEmail(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindEmail(context);

        kind.typeColumn = Email.TYPE;
        kind.typeList = Lists.newArrayList();
        // aurora <wangth> <2014-1-9> modify for aurora begin
        /*
        kind.typeList.add(buildEmailType(Email.TYPE_HOME));
        kind.typeList.add(buildEmailType(Email.TYPE_WORK));
        kind.typeList.add(buildEmailType(Email.TYPE_OTHER));
        kind.typeList.add(buildEmailType(Email.TYPE_CUSTOM).setSecondary(true).setCustomColumn(
                Email.LABEL));
        */
        kind.typeList.add(new EditType(Email.TYPE_HOME, R.string.emailType_Private));
        kind.typeList.add(new EditType(Email.TYPE_WORK, R.string.emailType_Work));
        kind.typeList.add(new EditType(Email.TYPE_CUSTOM, R.string.telType_custom).setSecondary(true).setCustomColumn(
                Email.LABEL));
        // aurora <wangth> <2014-1-9> modify for aurora end

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));

        return kind;
    }

    private DataKind addDataKindRelation(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(Relation.CONTENT_ITEM_TYPE,
        		R.string.relationLabelsGroup, Weight.RELATIONSHIP,
        		true, getTextFieldsEditorViewRes()));
        kind.actionHeader = new RelationActionInflater();
        kind.actionBody = new SimpleInflater(Relation.NAME);

        kind.typeColumn = Relation.TYPE;
        kind.typeList = Lists.newArrayList();
        // aurora <wangth> <2014-1-9> modify for aurora begin
        /*
        kind.typeList.add(buildRelationType(Relation.TYPE_ASSISTANT));
        kind.typeList.add(buildRelationType(Relation.TYPE_BROTHER));
        kind.typeList.add(buildRelationType(Relation.TYPE_CHILD));
        kind.typeList.add(buildRelationType(Relation.TYPE_DOMESTIC_PARTNER));
        kind.typeList.add(buildRelationType(Relation.TYPE_FATHER));
        kind.typeList.add(buildRelationType(Relation.TYPE_FRIEND));
        kind.typeList.add(buildRelationType(Relation.TYPE_MANAGER));
        kind.typeList.add(buildRelationType(Relation.TYPE_MOTHER));
        kind.typeList.add(buildRelationType(Relation.TYPE_PARENT));
        kind.typeList.add(buildRelationType(Relation.TYPE_PARTNER));
        kind.typeList.add(buildRelationType(Relation.TYPE_REFERRED_BY));
        kind.typeList.add(buildRelationType(Relation.TYPE_RELATIVE));
        kind.typeList.add(buildRelationType(Relation.TYPE_SISTER));
        kind.typeList.add(buildRelationType(Relation.TYPE_SPOUSE));
        kind.typeList.add(buildRelationType(Relation.TYPE_CUSTOM).setSecondary(true)
                .setCustomColumn(Relation.LABEL));
        */
        kind.typeList.add(new EditType(Relation.TYPE_ASSISTANT, R.string.relation_assitant));
        kind.typeList.add(new EditType(Relation.TYPE_BROTHER, R.string.relation_brother));
        kind.typeList.add(new EditType(Relation.TYPE_CHILD, R.string.relation_child));
        kind.typeList.add(new EditType(Relation.TYPE_DOMESTIC_PARTNER, R.string.relation_dom_partner));
        kind.typeList.add(new EditType(Relation.TYPE_FATHER, R.string.relation_father));
        kind.typeList.add(new EditType(Relation.TYPE_FRIEND, R.string.relation_friend));
        kind.typeList.add(new EditType(Relation.TYPE_MANAGER, R.string.relation_manager));
        kind.typeList.add(new EditType(Relation.TYPE_MOTHER, R.string.relation_mother));
        kind.typeList.add(new EditType(Relation.TYPE_PARENT, R.string.relation_parent));
        kind.typeList.add(new EditType(Relation.TYPE_PARTNER, R.string.relation_partner));
        kind.typeList.add(new EditType(Relation.TYPE_REFERRED_BY, R.string.relation_referred));
        kind.typeList.add(new EditType(Relation.TYPE_RELATIVE, R.string.relation_relative));
        kind.typeList.add(new EditType(Relation.TYPE_SISTER, R.string.relation_sistert));
        kind.typeList.add(new EditType(Relation.TYPE_SPOUSE, R.string.relation_spouse));
        kind.typeList.add(new EditType(Relation.TYPE_CUSTOM, R.string.telType_custom).setSecondary(true)
                .setCustomColumn(Relation.LABEL));
        // aurora <wangth> <2014-1-9> modify for aurora end

        kind.defaultValues = new ContentValues();
        kind.defaultValues.put(Relation.TYPE, Relation.TYPE_SPOUSE);

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Relation.DATA, R.string.relationLabelsGroup,
                FLAGS_RELATION));
        
        kind.hintRes = R.string.gn_relationLabelsGroup_hint;

        return kind;
    }

    private DataKind addDataKindEvent(Context context) throws DefinitionException {
    	int resId = ContactsApplication.sIsGnContactsSupport ? R.layout.gn_event_field_editor_view :
    		R.layout.event_field_editor_view;
        DataKind kind = addKind(new DataKind(Event.CONTENT_ITEM_TYPE,
                    R.string.eventLabelsGroup, 150, true, resId));
        kind.actionHeader = new EventActionInflater();
        kind.actionBody = new SimpleInflater(Event.START_DATE);

        kind.typeColumn = Event.TYPE;
        kind.typeList = Lists.newArrayList();
        kind.dateFormatWithoutYear = DateUtils.NO_YEAR_DATE_FORMAT;
        kind.dateFormatWithYear = DateUtils.FULL_DATE_FORMAT;
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

    @Override
    public boolean isGroupMembershipEditable() {
        return true;
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }
}
