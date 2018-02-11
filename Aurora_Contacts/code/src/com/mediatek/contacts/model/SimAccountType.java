
package com.mediatek.contacts.model;

import com.android.contacts.R;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.BaseAccountType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.AccountType.EditField;
import com.android.contacts.model.BaseAccountType.EventActionInflater;
import com.android.contacts.model.BaseAccountType.SimpleInflater;
import com.android.contacts.util.DateUtils;
import com.google.android.collect.Lists;
// Gionee liuyanbo 2012-07-31 modified for CR00657509 start
import com.mediatek.contacts.util.TelephonyUtils;
// Gionee liuyanbo 2012-07-31 modified for CR00657509 end

import android.content.ContentValues;
import android.content.Context;
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

public class SimAccountType extends BaseAccountType {

    public static final String TAG = "SimAccountType";
    public static final String ACCOUNT_TYPE = AccountType.ACCOUNT_TYPE_SIM;

    public SimAccountType(Context context, String resPackageName) {
        this.accountType = ACCOUNT_TYPE;
        this.resPackageName = null;
        this.summaryResPackageName = resPackageName;
        // Gionee liuyanbo 2012-07-31 modified for CR00657509 start
        if (TelephonyUtils.isCDMAPhone()) {
            this.titleRes = R.string.account_type_uim_label;
        } else {
            this.titleRes = R.string.account_type_sim_label;
        }
        // Gionee liuyanbo 2012-07-31 modified for CR00657509 end
        this.iconRes = R.drawable.ic_contact_account_sim;

        try {
            addDataKindStructuredName(context);
            addDataKindDisplayName(context);
            addDataKindPhone(context);
            // addDataKindEmail(context);
            // addDataKindUsimPhone(context);
            addDataKindPhoto(context);
            // addDataKindGroupMembership(context);
        } catch (DefinitionException e) {
            Log.e(TAG, "Problem building account type", e);
        }

    }

    @Override
    protected DataKind addDataKindStructuredName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                R.string.nameLabelsGroup, -1, true, R.layout.structured_name_editor_view));
        kind.actionHeader = new SimpleInflater(R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater(Nickname.NAME);
        
        // aurora <wangth> <2014-1-21> add for aurora begin
        kind.hintRes = R.string.aurora_edit_name_hint;
        // aurora <wangth> <2014-1-21> add for aurora end

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.DISPLAY_NAME, R.string.full_name,
                FLAGS_PERSON_NAME));
        kind.earseEditBg = false;
        return kind;
    }

    @Override
    protected DataKind addDataKindDisplayName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME,
                R.string.nameLabelsGroup, -1, true, R.layout.text_fields_editor_view));
        kind.typeOverallMax = 1;
        
        // aurora <wangth> <2014-1-21> add for aurora begin
        kind.hintRes = R.string.aurora_edit_name_hint;
        // aurora <wangth> <2014-1-21> add for aurora end

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.DISPLAY_NAME, R.string.full_name,
                FLAGS_PERSON_NAME));
        kind.earseEditBg = false;
        return kind;
    }

    @Override
    protected DataKind addDataKindPhone(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindPhone(context);
        Log.w("SimAccountType", "addDataKindPhone");
        kind.typeColumn = Phone.TYPE;
        kind.typeList = Lists.newArrayList();
        // aurora <wangth> <2014-1-2> remove for aurora begin
        //kind.typeList.add(buildPhoneType(Phone.TYPE_MOBILE).setSpecificMax(2));
        // aurora <wangth> <2014-1-2> remove for aurora end
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));

        return kind;
    }

    protected DataKind addDataKindUsimPhone(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindPhone(context);
        kind.typeColumn = Phone.TYPE;
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildPhoneType(Phone.TYPE_HOME).setSpecificMax(2));

        // kind.typeList.add(buildPhoneType(Phone.TYPE_MOBILE).setSpecificMax(1));
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));

        return kind;
    }

    @Override
    protected DataKind addDataKindPhoto(Context context)  throws DefinitionException {
        final DataKind kind = super.addDataKindPhoto(context);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Photo.PHOTO, -1, -1));

        return kind;
    }

    @Override
    protected DataKind addDataKindEmail(Context context)  throws DefinitionException {
        final DataKind kind = super.addDataKindEmail(context);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));

        return kind;
    }
//google 4.03 delete this function
//    @Override
//    public int getHeaderColor(Context context) {
//        return 0xffd5ba96;
//    }
//
//    @Override
//    public int getSideBarColor(Context context) {
//        return 0xffb58e59;
//    }

    @Override
    public boolean isGroupMembershipEditable() {
        return true;
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }
}
