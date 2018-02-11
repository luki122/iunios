package com.mediatek.contacts.model;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.model.BaseAccountType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.AccountType.EditField;
import com.android.contacts.model.BaseAccountType.EventActionInflater;
import com.android.contacts.model.BaseAccountType.SimpleInflater;
import com.android.contacts.util.DateUtils;
import com.android.contacts.model.AccountType;
import com.google.android.collect.Lists;
import com.mediatek.contacts.util.OperatorUtils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.sip.SipManager;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.Event;
import gionee.provider.GnContactsContract.CommonDataKinds.Im;
import gionee.provider.GnContactsContract.CommonDataKinds.Nickname;
import gionee.provider.GnContactsContract.CommonDataKinds.Note;
import gionee.provider.GnContactsContract.CommonDataKinds.Organization;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Photo;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredPostal;
import gionee.provider.GnContactsContract.CommonDataKinds.Website;
import android.util.Log;

import java.util.Locale;

public class LocalPhoneAccountType extends BaseAccountType {
    public static final String TAG = "LocalPhoneAccountType";
    
	public static final String ACCOUNT_TYPE = AccountType.ACCOUNT_TYPE_LOCAL_PHONE;

	public LocalPhoneAccountType(Context context, String resPackageName) {
	    this.accountType = ACCOUNT_TYPE;
        this.resPackageName = null;
        this.summaryResPackageName = resPackageName;
        if (ContactsApplication.sIsGnContactsSupport) {
        	this.titleRes = R.string.gn_account_phone;
        } else {
        	this.titleRes = R.string.account_phone;
        }
        this.iconRes = ResConstant.getIconRes(ResConstant.IconTpye.LocalPhone);
        
        try {
            boolean bl = OperatorUtils.getOptrProperties().equals("OP01");
            Log.i("yongjian","bl : "+bl);
            
            if (OperatorUtils.getOptrProperties().equals("OP01")) {
                addDataKindStructuredNameForCMCC(context);
                addDataKindDisplayNameForCMCC(context);
            } else {
                addDataKindStructuredName(context);
                addDataKindDisplayName(context);
            }
            
            addDataKindPhoneticName(context);
            // Aurora <ukiliu> <11 Sep, 2013> modify for new need of Contacts begin
            //addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            addDataKindStructuredPostal(context);
            //addDataKindIm(context);
            addDataKindOrganization(context);
            addDataKindPhoto(context);
            addDataKindNote(context);
            addDataKindEvent(context);
            //addDataKindWebsite(context);
            // Aurora <ukiliu> <11 Sep, 2013> modify for new need of Contacts end
            addDataKindGroupMembership(context);
            
//            if (SipManager.isVoipSupported(context)) {
//                addDataKindSipAddress(context);
//            }
        } catch (DefinitionException e) {
            Log.e(TAG, "Problem building account type", e);
        }
    }

    @Override
    protected DataKind addDataKindStructuredPostal(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindStructuredPostal(context);

        final boolean useJapaneseOrder = Locale.JAPANESE.getLanguage().equals(
                Locale.getDefault().getLanguage());
        kind.typeColumn = StructuredPostal.TYPE;
        kind.typeList = Lists.newArrayList();
        // aurora ukiliu 2013-10-22 modify for BUG #118&#113 begin
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_WORK));
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_HOME));
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_OTHER));
        // aurora ukiliu 2013-10-22 modify for BUG #118&#113 end

        kind.fieldList = Lists.newArrayList();
        // aurora ukiliu  2013-10-21 add for aurora ui begin
        if (ContactsApplication.auroraContactsSupport) {
        	kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.aurora_address_hint,
                     FLAGS_POSTAL));
        // aurora ukiliu  2013-10-21 add for aurora ui end
        } else if (useJapaneseOrder) {
            kind.fieldList.add(new EditField(StructuredPostal.COUNTRY, R.string.postal_country,
                    FLAGS_POSTAL).setOptional(true));
            kind.fieldList.add(new EditField(StructuredPostal.POSTCODE, R.string.postal_postcode,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.REGION, R.string.postal_region,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.CITY, R.string.postal_city,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.NEIGHBORHOOD, R.string.postal_neighborhood,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.POBOX, R.string.postal_pobox,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.postal_street,
                    FLAGS_POSTAL));
        } else {
            kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.postal_street,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.POBOX, R.string.postal_pobox,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.NEIGHBORHOOD, R.string.postal_neighborhood,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.CITY, R.string.postal_city,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.REGION, R.string.postal_region,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.POSTCODE, R.string.postal_postcode,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.COUNTRY, R.string.postal_country,
                    FLAGS_POSTAL).setOptional(true));
        }

        return kind;
    }
    
    
    protected DataKind addDataKindStructuredNameForCMCC(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                R.string.nameLabelsGroup, -1, true, R.layout.structured_name_editor_view));
        kind.actionHeader = new SimpleInflater(R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater(Nickname.NAME);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.PREFIX, R.string.name_prefix,
                FLAGS_PERSON_NAME).setOptional(true));
        kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME,
                R.string.name_family, FLAGS_PERSON_NAME));
        kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME,
                R.string.name_middle, FLAGS_PERSON_NAME));
        kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME,
                R.string.name_given, FLAGS_PERSON_NAME));
        kind.fieldList.add(new EditField(StructuredName.SUFFIX,
                R.string.name_suffix, FLAGS_PERSON_NAME));

        kind.fieldList.add(new EditField(StructuredName.PHONETIC_FAMILY_NAME,
                R.string.name_phonetic_family, FLAGS_PHONETIC));
        kind.fieldList.add(new EditField(StructuredName.PHONETIC_GIVEN_NAME,
                R.string.name_phonetic_given, FLAGS_PHONETIC));

        return kind;
    }

    protected DataKind addDataKindDisplayNameForCMCC(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME,
                R.string.nameLabelsGroup, -1, true, R.layout.text_fields_editor_view));

        boolean displayOrderPrimary =
                context.getResources().getBoolean(R.bool.config_editor_field_order_primary);
        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.PREFIX, R.string.name_prefix,
                FLAGS_PERSON_NAME).setOptional(true));
        if (!displayOrderPrimary) {
            kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME,
                    R.string.name_family, FLAGS_PERSON_NAME));
            kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME,
                    R.string.name_middle, FLAGS_PERSON_NAME).setOptional(true));
            kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME,
                    R.string.name_given, FLAGS_PERSON_NAME));
        } else {
            kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME,
                    R.string.name_given, FLAGS_PERSON_NAME));
            kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME,
                    R.string.name_middle, FLAGS_PERSON_NAME).setOptional(true));
            kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME,
                    R.string.name_family, FLAGS_PERSON_NAME));
        }
        kind.fieldList.add(new EditField(StructuredName.SUFFIX,
                R.string.name_suffix, FLAGS_PERSON_NAME).setOptional(true));

        return kind;
    }

    // Aurora <ukiliu> <11 Sep, 2013> add for new need of Contacts begin
    protected DataKind addDataKindEvent(Context context) throws DefinitionException {
    	int layoutRes = ContactsApplication.sIsGnContactsSupport ? 
    			R.layout.gn_event_field_editor_view : R.layout.event_field_editor_view;
    	
        DataKind kind = addKind(
                new DataKind(Event.CONTENT_ITEM_TYPE, R.string.eventLabelsGroup, 150, true,
                		layoutRes));
        kind.actionHeader = new EventActionInflater();
        kind.actionBody = new SimpleInflater(Event.START_DATE);

//        kind.typeOverallMax = 1;

        kind.typeColumn = Event.TYPE;
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildEventType(Event.TYPE_BIRTHDAY, false).setSpecificMax(1));
//        kind.typeList.add(buildEventType(Event.TYPE_BIRTHDAY, false));
        kind.typeList.add(buildEventType(Event.TYPE_ANNIVERSARY, false));

        kind.dateFormatWithYear = DateUtils.DATE_AND_TIME_FORMAT;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Event.DATA, R.string.aurora_editor_event_birthday, FLAGS_EVENT));
        kind.fieldList.add(new EditField(Event.DATA, R.string.aurora_editor_event_day, FLAGS_EVENT));

        return kind;
    }
    // Aurora <ukiliu> <11 Sep, 2013> add for new need of Contacts end
    

    // google 4.03 delete this function
    // @Override
    // public int getHeaderColor(Context context) {
    // return 0xffd5ba96;
    // }
    //
    // @Override
    // public int getSideBarColor(Context context) {
    // return 0xffb58e59;
    // }

    @Override
    public boolean isGroupMembershipEditable() {
        return true;
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }
    
    @Override
    public Drawable getDisplayIcon(Context context) {
    	return context.getResources().getDrawable(
    			ResConstant.getIconRes(ResConstant.IconTpye.LocalPhone));
    }
}
