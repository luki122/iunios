package com.mediatek.contacts.widget;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.sip.SipManager;
import android.provider.Settings;
import gionee.provider.GnTelephony.SIMInfo;
import android.text.TextUtils;

import com.android.contacts.R;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.util.ContactsSettingsUtils;
import com.mediatek.contacts.widget.SimPickerAdapter.ItemHolder;

public class SimPickerDialog {

    public static AuroraAlertDialog createSingleChoice(Context context, String title, int choiceItem, DialogInterface.OnClickListener listener) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
        List<ItemHolder> items = createItemHolder(context, context.getString(R.string.call_log_filter_all_resources), true, true, null);
        SimPickerAdapter simAdapter = new SimPickerAdapter(context, items, ContactsFeatureConstants.DEFAULT_SIM_NOT_SET);
        simAdapter.setSingleChoice(true);
        simAdapter.setSingleChoiceIndex(choiceItem);
        builder.setSingleChoiceItems(simAdapter, -1, listener).setTitle(title).setTitleDividerVisible(true);
        return builder.create();
    }

    public static AuroraAlertDialog create(Context context, String title, DialogInterface.OnClickListener listener) {
        return create(context, title, ContactsSettingsUtils.DEFAULT_SIM_NOT_SET, true, listener);
    }

    public static AuroraAlertDialog create(Context context, String title, boolean internet, DialogInterface.OnClickListener listener) {
        return create(context, title, ContactsSettingsUtils.DEFAULT_SIM_NOT_SET, internet, listener);
    }

    public static AuroraAlertDialog create(Context context, String title, long suggestedSimId, DialogInterface.OnClickListener listener) {
        return create(context, title, suggestedSimId, true, listener);
    }

    public static AuroraAlertDialog create(Context context, String title, long suggestedSimId, boolean internet, DialogInterface.OnClickListener listener) {
        return create(context, title, suggestedSimId, createItemHolder(context, internet), listener);
    }

    protected static AuroraAlertDialog create(Context context, String title, long suggestedSimId, List<ItemHolder> items, DialogInterface.OnClickListener listener) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
        SimPickerAdapter simAdapter = new SimPickerAdapter(context, items, suggestedSimId);
        builder.setSingleChoiceItems(simAdapter, -1, listener)
               .setTitle(title).setTitleDividerVisible(true);
        return builder.create();
    }

    protected static List<ItemHolder> createItemHolder(Context context, boolean internet) {
        return createItemHolder(context, null, internet, null);
    }

    protected static List<ItemHolder> createItemHolder(Context context, String phone, boolean internet, ArrayList<Account> accounts) {
    	return createItemHolder(context, phone, internet, false, accounts);
    }

    protected static List<ItemHolder> createItemHolder(Context context, String phone, boolean internet, boolean forceInternet, ArrayList<Account> accounts) {
        List<SIMInfo> simInfos = SIMInfo.getInsertedSIMList(context);
        ArrayList<ItemHolder> itemHolders = new ArrayList<ItemHolder>();
        ItemHolder temp = null;

        if(!TextUtils.isEmpty(phone)) {
            temp = new ItemHolder(phone, SimPickerAdapter.ITEM_TYPE_TEXT);
            itemHolders.add(temp);
        }
        
        for(SIMInfo simInfo : simInfos) {
            temp = new ItemHolder(simInfo, SimPickerAdapter.ITEM_TYPE_SIM);
            itemHolders.add(temp);
        }

        int enabled = Settings.System.getInt(context.getContentResolver(), ContactsFeatureConstants.ENABLE_INTERNET_CALL, 0);
        if (SipManager.isVoipSupported(context)) {
			//GIONEE:liuying 2012-7-27 modify for CR00658251begin
			final boolean gnNgmflag = android.os.SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM");
            if (!gnNgmflag && (forceInternet || (internet && enabled == 1) )) {
			//GIONEE:liuying 2012-7-27 modify for CR00658251 end
                temp = new ItemHolder(context.getResources().getText(R.string.label_sip_address),
                        SimPickerAdapter.ITEM_TYPE_INTERNET);
                itemHolders.add(temp);
            }
        }

        if(accounts != null) {
            for(Account account : accounts) {
                temp = new ItemHolder(account, SimPickerAdapter.ITEM_TYPE_ACCOUNT);
                itemHolders.add(temp);
            }
        }

        return itemHolders;
    }

}
