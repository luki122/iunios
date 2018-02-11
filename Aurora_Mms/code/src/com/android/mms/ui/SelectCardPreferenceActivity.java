package com.android.mms.ui;

import static android.content.res.Configuration.KEYBOARDHIDDEN_NO;

import java.util.List;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.AdvancedEditorPreference.GetSimInfo;

import android.R.color;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceManager;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import aurora.widget.AuroraEditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnTelephony.SIMInfo;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import com.android.mms.ui.MessagingPreferenceActivity;

public class SelectCardPreferenceActivity extends AuroraPreferenceActivity implements GetSimInfo{
    private static final String TAG = "Mms/SelectCardPreferenceActivity";
    
    private AdvancedEditorPreference mSim1;
    private AdvancedEditorPreference mSim2;
    private AdvancedEditorPreference mSim3;
    private AdvancedEditorPreference mSim4;
    
    private String mSim1Number;
    private String mSim2Number;
    private String mSim3Number;
    private String mSim4Number;
    
    private int simCount;
    
    private int currentSim = -1;
    private GnTelephonyManagerEx mTelephonyManager;
    private AuroraEditText mNumberText;
    private AuroraAlertDialog mNumberTextDialog;
    private List<SIMInfo> listSimInfo;
    String intentPreference;
    private static Handler mSMSHandler = new Handler();
    private final int MAX_EDITABLE_LENGTH = 20;
    public String SUB_TITLE_NAME = "sub_title_name";
    private AuroraAlertDialog mSaveLocDialog;//for sms save location
    private SharedPreferences spref;
    protected void onCreate(Bundle icicle) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(icicle);
        listSimInfo = SIMInfo.getInsertedSIMList(this);
        simCount = listSimInfo.size();
        spref = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.multicardeditorpreference);    
        Intent intent = getIntent();
        intentPreference = intent.getStringExtra("preference");
        changeMultiCardKeyToSimRelated(intentPreference);
        //gionee gaoj 2012-5-29 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
             // Aurora liugj 2013-09-13 modified for aurora's new feature start
            ActionBar actionBar = getActionBar();
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            actionBar.setDisplayShowHomeEnabled(false);
            //gionee gaoj added for CR00725602 20121201 start
            actionBar.setDisplayHomeAsUpEnabled(true);
            //gionee gaoj added for CR00725602 20121201 end
        }
        //gionee gaoj 2012-5-29 added for CR00555790 end
    }

    //gionee gaoj added for CR00725602 20121201 start
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    //gionee gaoj added for CR00725602 20121201 end

    private void changeMultiCardKeyToSimRelated(String preference) {

        mSim1 = (AdvancedEditorPreference) findPreference("pref_key_sim1");
        mSim1.init(this, 0);
        mSim2 = (AdvancedEditorPreference) findPreference("pref_key_sim2");
        mSim2.init(this, 1);
        mSim3 = (AdvancedEditorPreference) findPreference("pref_key_sim3");
        mSim3.init(this, 2);
        mSim4 = (AdvancedEditorPreference) findPreference("pref_key_sim4");
        mSim4.init(this, 3);
        //get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        if (simCount == 1) {
            getPreferenceScreen().removePreference(mSim2);
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);           
        } else if (simCount == 2) {
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);   
        } else if (simCount == 3) {
            getPreferenceScreen().removePreference(mSim4);
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
            AuroraPreference preference) {
        int currentId = 0;
        if (preference == mSim1) {
            currentId = 0;
        } else if (preference == mSim2) {
            currentId = 1;
        } else if (preference == mSim3) {
            currentId = 2;
        } else if (preference == mSim4) {
            currentId = 3;
        }
        if (intentPreference.equals(MessagingPreferenceActivity.SMS_MANAGE_SIM_MESSAGES)) {
            startManageSimMessages(currentId);
        } else if (intentPreference.equals(MessagingPreferenceActivity.CELL_BROADCAST)) {
            startCellBroadcast(currentId);
        }  else if (intentPreference.equals(MessagingPreferenceActivity.SMS_SERVICE_CENTER)) {
            //mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            currentSim = listSimInfo.get(currentId).mSlot;
            setServiceCenter(currentSim);
        } else if(intentPreference.equals(MessagingPreferenceActivity.SMS_SAVE_LOCATION)){
            setSaveLocation(currentId);
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private void setSaveLocation(long id){
        Log.d(TAG, "currentSlot is: " + id);
           //the key value for each saveLocation
           final String [] saveLocation = getResources().getStringArray(R.array.pref_sms_save_location_values);
           //the diplayname for each saveLocation
        final String [] saveLocationDisp = getResources().getStringArray(R.array.pref_sms_save_location_choices);
           if(saveLocation == null || saveLocationDisp == null){
               Log.d(TAG, "setSaveLocation is null");
               return;
           }
           final String saveLocationKey = Long.toString(id) + "_" + MessagingPreferenceActivity.SMS_SAVE_LOCATION;
           int pos = getSelectedPosition(saveLocationKey, saveLocation);
           mSaveLocDialog = new AuroraAlertDialog.Builder(this)
        .setTitle(R.string.sms_save_location)
        .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
        .setSingleChoiceItems(saveLocationDisp, pos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "setSaveLocation whichButton = "+whichButton);
                SharedPreferences.Editor editor = spref.edit();
                editor.putString(saveLocationKey, saveLocation[whichButton]);
                editor.commit();
                //Gionee <guoyx> <2013-06-26> modify for CR00828231 begin
                if (mSaveLocDialog != null && mSaveLocDialog.isShowing()) {
                    mSaveLocDialog.dismiss();
                }
                //Gionee <guoyx> <2013-06-26> modify for CR00828231 end
                mSaveLocDialog = null;
            }
        })
        .show();
    }
    
    //get the position which is selected before
    private int getSelectedPosition(String inputmodeKey, String [] modes){
        String res = spref.getString(inputmodeKey, "Phone");
        Log.d(TAG, "getSelectedPosition found the res = "+res);
        for(int i = 0; i < modes.length; i++){
            if(res.equals(modes[i])){
                Log.d(TAG, "getSelectedPosition found the position = "+i);
                return i;
            }
        }
        Log.d(TAG, "getSelectedPosition not found the position");

        return 0; 
    }
    
    public void setServiceCenter(int id){
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.gn_add_quick_text_dialog, null);
        mNumberText = (AuroraEditText) textEntryView.findViewById(R.id.add_quick_text_content);
        mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
        mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
        //mNumberText.setKeyListener(new DigitsKeyListener(false, true));
        mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
        mNumberText.computeScroll();
        Log.d(TAG, "currentSlot is: " + id);
        String scNumber = getServiceCenter(id);
        Log.d(TAG, "getScNumber is: " + scNumber);
        mNumberText.setText(scNumber);
        //gionee gaoj added for CR00725602 20121201 start
        mNumberTextDialog = new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
        //gionee gaoj added for CR00725602 20121201 end
        //Gionee <Gaoj><2013-05-06> delete for CR00808328 begin
        /*.setIcon(android.R.drawable.ic_dialog_info)*/
        //Gionee <Gaoj><2013-05-06> delete for CR00808328 end
        .setTitle(R.string.sms_service_center)
        .setView(textEntryView)
        .setPositiveButton(R.string.OK, new PositiveButtonListener())
        .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
        .show();
    }

    public void startManageSimMessages(int id){
        if(listSimInfo == null || id >= listSimInfo.size()){
            Log.e(TAG, "startManageSimMessages listSimInfo is null ");
            return;
        }
        Intent it = new Intent();
         int slotId = listSimInfo.get(id).mSlot;
         Log.d(TAG, "currentSlot is: " + slotId);
         Log.d(TAG, "currentSlot name is: " + listSimInfo.get(0).mDisplayName);
         it.setClass(this, ManageSimMessages.class);
         it.putExtra("SlotId", slotId);
         startActivity(it);
    }
    private void startCellBroadcast(int num){
        if (listSimInfo == null || num >= listSimInfo.size()){
            Log.e(TAG, "startCellBroadcast listSimInfo is null ");
            return;
        }
        Intent it = new Intent();
         int slotId = listSimInfo.get(num).mSlot;
         Log.i(TAG, "currentSlot is: " + slotId);
         Log.i(TAG, "currentSlot name is: " + listSimInfo.get(num).mDisplayName);
         // gionee zhouyj 2012-11-26 modify for CR00735999 start 
         if (MmsApp.mIsSupportPlatform_4_1) {
             it.setClassName("com.android.phone", "com.mediatek.settings.CellBroadcastActivity");
         } else {
             it.setClassName("com.android.phone", "com.android.phone.CellBroadcastActivity");
         }
         // gionee zhouyj 2012-11-26 modify for CR00735999 end 
         it.setAction(Intent.ACTION_MAIN);
         it.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId);
         it.putExtra(SUB_TITLE_NAME, SIMInfo.getSIMInfoBySlot(this, slotId).mDisplayName);
         startActivity(it);
    }
    
    public String getSimName(int id) {
        return listSimInfo.get(id).mDisplayName;
    } 
    
    public String getSimNumber(int id) {
        return listSimInfo.get(id).mNumber;
    }
    
    public int getSimColor(int id) {
        return listSimInfo.get(id).mSimBackgroundRes;
    }
    
    public int getNumberFormat(int id) {
        return listSimInfo.get(id).mDispalyNumberFormat;
    }
    
    public int getSimStatus(int id) {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        //int slotId = SIMInfo.getSlotById(this,listSimInfo.get(id).mSimId);
        int slotId = listSimInfo.get(id).mSlot;
        if (slotId != -1) {
            return mTelephonyManager.getSimIndicatorStateGemini(slotId);
        }
        return -1;
    }
    
    public boolean is3G(int id)    {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        //int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
        int slotId = listSimInfo.get(id).mSlot;
        Log.d(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
        if (slotId == MessageUtils.get3GCapabilitySIM()) {
            return true;
        }
        return false;
    }
    
    private String getServiceCenter(int id) {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        // gionee zhouyj 2013-03-29 modify for CR00790884 start
        if (MmsApp.mQcMultiSimEnabled) {
            //Gionee guoyx 20130302 modified for CR00772805 begin
            String sc = mTelephonyManager.getScAddress(id);
            if (sc != null && !"".equals(sc)) {
                Log.d(TAG, "getServiceCenter is:" + sc + " before substring.");
                int index = sc.lastIndexOf("\"");
                return sc.substring(1, index);
            } else {
                Log.e(TAG, "getServiceCenter is: fail !");
                return "";
            }
            //Gionee guoyx 20130302 modified for CR00772805 end
        } else {
            return mTelephonyManager.getScAddress(id);
        }
        // gionee zhouyj 2013-03-29 modify for CR00790884 end
    }
    
    private boolean setServiceCenter(String SCnumber, int id) {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        Log.d(TAG, "setScAddress is: " + SCnumber);
        return mTelephonyManager.setScAddress(SCnumber, id);
    }
    
    private void tostScOK() {
        Toast.makeText(this, R.string.set_service_center_OK, 0);
    }
    
    private void tostScFail() {
        Toast.makeText(this, R.string.set_service_center_fail, 0);
    }
    
    private class PositiveButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            mTelephonyManager = GnTelephonyManagerEx.getDefault();
            String scNumber = mNumberText.getText().toString();
            Log.d(TAG, "setScNumber is: " + scNumber);
            Log.d(TAG, "currentSim is: " + currentSim);
            //setServiceCenter(scNumber, currentSim);
            new Thread(new Runnable() {
                public void run() {
                    mTelephonyManager.setScAddress(mNumberText.getText().toString(), currentSim);
                }
            }).start();
        }
    }
    
    private class NegativeButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // cancel
            dialog.dismiss();
        }
    }
}
