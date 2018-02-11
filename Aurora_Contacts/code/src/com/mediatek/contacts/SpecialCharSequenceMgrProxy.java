package com.mediatek.contacts;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import gionee.telephony.GnTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListAdapter;

//import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.SpecialCharSequenceMgr;
import com.android.internal.telephony.ITelephony;

import com.mediatek.contacts.util.ContactsSettingsUtils;
import com.mediatek.contacts.util.TelephonyUtils;
import com.mediatek.contacts.widget.SimPickerDialog;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
//Gionee <wangth><2013-05-03> modify for CR00805658 begin
//import com.mediatek.featureoption.FeatureOption;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
//Gionee <wangth><2013-05-03> modify for CR00805658 end
import com.gionee.internal.telephony.GnITelephony;
import com.android.contacts.ContactsUtils;

//Gionee zjy 20120321 add for CR00555135 start
import android.os.SystemProperties;
import java.text.SimpleDateFormat;
//Gionee zjy 20120321 add for CR00555135 end

// GIONEE licheng May 8, 2012 add for CR00594200 start
import java.util.Date;
import android.os.Build;
// GIONEE licheng May 8, 2012 add for CR00594200 end

//Gionee qiuxd 20120612 add for CR00623811 start
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//Gionee qiuxd 20120612 add for CR00623811 end
//GIONEE huyuke 20120824 add for CR00680819 begin
import java.io.FileNotFoundException;
//GIONEE huyuke 20120824 add for CR00680819 end

// Gionee lihuafang 2012-06-14 add for CR00608224 begin
import android.os.Build;
import java.util.Date;
import android.webkit.WebView;
// Gionee lihuafang 2012-06-14 add for CR00608224 end
//GIONEE:wangfei 2012-08-28 modify for CR00681759 begin
import android.content.DialogInterface.OnCancelListener;
import android.app.Dialog;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
//GIONEE:wangfei 2012-08-28 modify for CR00681759 end

//Gionee liming 2012-06-10 add for CR00623293 begin //tangzepeng 2012-9-21 merge begin
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.view.LayoutInflater;
import java.io.File;
import android.view.View;
//Gionee liming 2012-06-10 add for CR00623293 end //tangzepeng 2012-9-21 merge end

import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraEditText;


public class SpecialCharSequenceMgrProxy {

    private final static String TAG = "SpecialCharSequenceMgrProxy";

    private static final String MMI_IMEI_DISPLAY = "*#06#";

    private static final String ADN_PHONE_NUMBER_COLUMN_NAME = "number";
    private static final String ADN_NAME_COLUMN_NAME = "name";
    private static final String ADN_INDEX_COLUMN_NAME = "index";

    private static final int ADN_QUERY_TOKEN_SIM1 = 0;
    private static final int ADN_QUERY_TOKEN_SIM2 = 1;

    private static final String SIM_CONTACT_URI = "content://icc/adn";
    private static final String USIM_CONTACT_URI = "content://icc/pbr";
	
    // Gionee xiaolin 20120319 add for CR00544355 start 
	private static final String MMI_TEST_ENTRY = "*#8702#";
    // Gionee xiaolin 20120319 add for CR00544355 end

	// GIONEE zjy 20120321 add for  CR00555135 search version start
    private static final String GN_INTERNAL_VERSION = "*#837500#";
    /*private static final String GN_EXTERNAL_VERSION = "*#837501#";
    private static final String GN_GBW_INTERNAL_VERSION = "*#837502#";*/
    // GIONEE: dingwen 20120407 add for CR00564842 Verion number begin
    private static final String GN_OVERSEA_INTERNAL_VERSION = "*#2229#";
    private static final String GN_OVERSEA_EXTERNAL_VERSION = "*#837500#";
    // GIONEE: dingwen 20120407 add for CR00564842 Verion number end

    // Gionee: 20120709 chenrui add for CR00640809 begin
    private static final String GN_BLU_HW_VERSION = "*#999*#";
    // Gionee: 20120709 chenrui add for CR00640809 end
    
    //Gionee:huangzy 20121107 modify for CR00682029 start
    /*// GIONEE: dingwen 20120704 add for CR00586298 begin
    private static final String GN_OVERSEA_FACTORY_VERSION = "*#446633#";
    private static final String GN_NATIVE_FACTORY_VERSION = "*#*#3646633#*#*";
    // GIONEE: dingwen 20120704 add for CR00586298 end*/
    private static final String GN_GIONEE_FACTORY_VERSION = "*#446633#";
    private static final String GN_NATIVE_FACTORY_VERSION = "*#*#3646633#*#*";
    //Gionee:huangzy 20121107 modify for CR00682029 end
	//GIONEE:guoyx 20121106 added for CR00725060 begin
    /**
     * Android Setting new request for 4.5.3 version.
     * Disable the normal developer options, and use "*#*#452789#*#*" 
     * dial chars to entry the normal developer options.
     */
    private static final String GN_SETTING_DEVELOPER_OPTIONS = "*#*#452789#*#*";
    //GIONEE:guoyx 20121106 added for CR00725060 begin
    
    // GIONEE licheng May 8, 2012 add for CR00594200 start
    //private static final String GN_UA = "*#282#";
    // GIONEE licheng May 8, 2012 add for CR00594200 end

    // GIONEE licheng May 9, 2012 add for CR00594199 start
    private static final String GN_UINICOM_VERSION_NUM= "*#123#";
    // GIONEE licheng May 9, 2012 add for CR00594199 end
    
    //Gionee qiuxd 20120612 add for CR00623811 start
    private static final String GN_GTL_TYPE_SEARCH = "*#2523#";    
    //Gionee qiuxd 20120612 add for CR00623811 end
    
    // Gionee lihuafang 2012-06-14 add for CR00608224 begin
    private static final String GN_UA_ALL="*#282#";
    private static final int GN_UA_ALL_DIALOG = 106; 
    // Gionee lihuafang 2012-06-14 add for CR00608224 end

    //GIONEE huyuke 20120824 add for CR00680819 begin
    private static final String GN_GET_DEVICES_STATUS ="*#7552#";
    private static final int GN_GET_DEVICES_STATUS_DIALOG = 107; 
    //GIONEE huyuke 20120824 add for CR00680819 end

    //Gionee <xuhz> <2013-07-18> add for CR00837777 begin
    private static final String GN_EXTERNAL_VERSION ="*#0000#";
    //Gionee <xuhz> <2013-07-18> add for CR00837777 end

    private AuroraAlertDialog mInternalDialog;
    private AuroraAlertDialog mExternalDialog;
    private AuroraAlertDialog mGN_GBW_mInternalDialog;
    private final static int INTER_VER_DIALOG = 100;
    private final static int EXTER_VER_DIALOG = 101;
    private final static int GN_GBW_INTER_VER_DIALOG = 102;
    // GIONEE zjy 20120321 add for  CR00555135 search version end
    
    // GIONEE licheng May 9, 2012 add for CR00594199 start
    private final static int GN_UINICOM_VERSION_NUM_DIALOG = 104;
    // GIONEE licheng May 9, 2012 add for CR00594199 end

    // GIONEE licheng May 8, 2012 add for CR00594200 start
    private final static int GN_UA_DIALOG = 103;
    // GIONEE licheng May 8, 2012 add for CR00594200 end
    
    //Gionee qiuxd 20120612 add for CR00623811 start
    private static final int GN_GTL_TYPE_DIALOG = 105;    
    //Gionee qiuxd 20120612 add for CR00623811 end
    
    private static final int GN_QC_TP_LCD_DIALOG = 110; 
	
	// Gionee: 20121106 guoxiaotian add for CR00725416 begin
    private static final int BLU_HW_INFO_DIALOG = 108;
    private static final int BLU_PCBA_VER_DIALOG = 109;

    private static final String GN_BLU_HW_INFO = "*#888*#";
    private static final String GN_BLU_PCBA_VERSION = "*#999*#";

    private static final boolean gnBLUflag = SystemProperties.get("ro.gn.oversea.custom").equals("SOUTH_AMERICA_BLU");
    // Gionee: 20121106 guoxiaotian add for CR00725416 end
 
    
	// Gionee zjy 20120329 add for CR00560679 start
    private static boolean mSingleIMEI = SystemProperties.get("ro.gn.display.single.imei").equals("yes");
	// Gionee zjy 20120329 add for CR00560679 end 
	
    // GIONEE: dingwen 20120407 add for CR00564842 Verion number begin
    private static boolean mOverseaProduct = SystemProperties.get("ro.gn.oversea.product").equals("yes");
    // GIONEE: dingwen 20120407 add for CR00564842 Verion number end
    
    //GIONEE: lujian 2012.07.30 modify for "fly CR00659144" begin
    private static final boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    //GIONEE: lujian 2012.07.30 modify for "fly CR00659144"   end

    //Gionee qinkai 2012-08-29 added for CR00682029 start
    private static final boolean gnNgmflag = SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM");
    //Gionee qinkai 2012-08-29 added for CR00682029 end
    
    //GIONEE:chenxiong 2012-05-31 add begin for CR00608860 //tangzepeng 2012-9-21 merge begin 
    public static final boolean gnSalestrackflag = SystemProperties.get("ro.gn.oversea.salestrack").equals("yes");
    //GIONEE:chenxiong 2012-05-31 add end for CR00608860
		
    //Gionee liming 2012-06-10 add for CR00623425 begin
    public static final boolean gnSpyflag = SystemProperties.get("ro.gn.oversea.custom").equals("BANGLADESH_SYMPHONY");
    //Gionee liming 2012-06-10 add for CR00623425 end
	
    //Gionee liming 2012-06-10 add for CR00623293 begin
    private static final String MMI_SALESTRACKSETTING_DISPLAY = "*#4672835#";
    private static CheckBox mSalesSwitch;
    private static boolean salesState = false;
    private static AuroraEditText numText;
    private static AuroraEditText timeText;
    private static String TEST_SMS_NUMBER= null;
    private static String TEST_TIME_MIN = null;
    private static String SALES_FILE = "gionee_sales_file.txt";
    private static boolean salesIsSent = false;
    private static final String SALESTRACK_FLAG_FILE = "sales_track_sms_is_sent";
    private static AuroraButton buttonSave;
    private static AuroraButton buttonExit;
    //Gionee liming 2012-06-10 add for CR00623293 end  // tangzepeng 2012-9-21 merge end
    
    private static final String AURORA_INDIA_SAR = "*#07#";  
    
	
    private SpecialCharSequenceMgrProxy() {
    }

    public static boolean handleChars(Context context, String input, AuroraEditText textField) {
        //Gionee:niejn 20121027 modify for CR00722199 begin
        /*
        if(ContactsApplication.sGemini)
            return handleChars(context, input, false, textField);
        else
            return SpecialCharSequenceMgr.handleChars(context, input, false, textField);
        */
        return handleChars(context, input, false, textField);
        //Gionee:niejn 20121027 modify for CR00722199 end
    }

    static boolean handleChars(Context context, String input) {
        if(ContactsApplication.sGemini)
            return handleChars(context, input, false, null);
        else
            return SpecialCharSequenceMgr.handleChars(context, input, false, null);
    }

    static boolean handleChars(Context context, String input, boolean useSystemWindow,
            AuroraEditText textField) {
        Log.d(TAG, "handleChars() dialString:" + input);
    
        //Gionee:huangzy 20121107 modify for CR00682029 start
        if (TextUtils.isEmpty(input) || !input.contains("#")) {
        	return false;
        }
        String dialString = PhoneNumberUtils.stripSeparators(input).trim();
        try{
        // GIONEE zjy 20120321 add for CR00555135 search version start
        // GIONEE: dingwen 20120407 add for CR00564842 Oversea Verion number begin
        if(mOverseaProduct) {
            if (dialString.equals(GN_OVERSEA_INTERNAL_VERSION)) {
			   if(ContactsApplication.isMultiSimEnabled)
			   	{
                  Intent intent = new Intent("gn.com.android.mmitest.internalversion");
		          context.startActivity(intent);
			    }
			    else
			    {
                    showDialog(INTER_VER_DIALOG, context);
			    }
                return true;
            } else if (dialString.equals(GN_OVERSEA_EXTERNAL_VERSION)) {
                showDialog(EXTER_VER_DIALOG, context);
                return true;
            } /*else if (dialString.equals(GN_GBW_INTERNAL_VERSION)) {
                showDialog(GN_GBW_INTER_VER_DIALOG, context);
                return true;
            }*/else if(gnSalestrackflag && gnSpyflag && dialString.equals(MMI_SALESTRACKSETTING_DISPLAY)) {
                if(handleSalesTrackSettingCode(context, dialString)) {
                	return true;
                }
            }
        } else {
            if (dialString.equals(GN_INTERNAL_VERSION)) {
                Intent intent = new Intent("gn.com.android.mmitest.internalversion");
                context.startActivity(intent);
            	
                return true;
            }/* else if (dialString.equals(GN_EXTERNAL_VERSION)) {
                showDialog(EXTER_VER_DIALOG, context);
                return true;
            } else if (dialString.equals(GN_GBW_INTERNAL_VERSION)) {
                showDialog(GN_GBW_INTER_VER_DIALOG, context);
                return true;
            }*/
            //Gionee <xuhz> <2013-07-18> add for CR00837777 begin
            if (dialString.equals(GN_EXTERNAL_VERSION)) {
                String mOptr = SystemProperties.get("ro.operator.optr");
                boolean cmccSupport = false;
                if (null != mOptr && mOptr.equals("OP01")) {
                    cmccSupport = true;
                }
                
                if (cmccSupport) {
                	showDialog(EXTER_VER_DIALOG, context);
                    return true;
                }
            }
            //Gionee <xuhz> <2013-07-18> add for CR00837777 end
        }
		
        //Gionee zhanglina 20121024 add for CR00717937 start

        // GIONEE licheng May 8, 2012 add for CR00594200 start
        /*
        else if (dialString != null
                && dialString.trim().equals(GN_UA)) {
            showDialog(GN_UA_DIALOG, context);
            return true;
        }
        */
        // GIONEE licheng May 8, 2012 add for CR00594200 end

        // gionee xuhz 20130221 remove for CR00772677 start
        // GIONEE licheng May 9, 2012 add for CR00594199 start
        /*if (dialString.equals(GN_UINICOM_VERSION_NUM)) {
            showDialog(GN_UINICOM_VERSION_NUM_DIALOG, context);
            return true;
        }
        // GIONEE licheng May 9, 2012 add for CR00594199 end
        //Gionee qiuxd 20120612 add for CR00623811 start
        else*/
     // gionee xuhz 20130221 remove for CR00772677 end
        if(dialString.equals(GN_GTL_TYPE_SEARCH)){
//            if (GNContactsUtils.isOnlyQcContactsSupport()) {
//                showDialog(GN_QC_TP_LCD_DIALOG, context);
//                return true;
//            }

            showDialog(GN_GTL_TYPE_DIALOG, context);
            return true;
        }    
        //Gionee qiuxd 20120612 add for CR00623811 end
        // Gionee lihuafang 2012-06-14 add for CR00608224 begin
        else if (dialString.equals(GN_UA_ALL)) {
            showDialog(GN_UA_ALL_DIALOG, context);
            return true;
        }
        // Gionee lihuafang 2012-06-14 add for CR00608224 end
        //GIONEE huyuke 20120824 add for CR00680819 begin
        else if (dialString.equals(GN_GET_DEVICES_STATUS)) {
            showDialog(GN_GET_DEVICES_STATUS_DIALOG, context);
            return true;
        }
        //GIONEE huyuke 20120824 add for CR00680819 end
		//Gionee zhanglina 20121024 add for CR00717937 end
        // GIONEE: dingwen 20120407 add for CR00564842 Oversea Verion number end
        // GIONEE zjy 20120321 add for CR00555135 search version end

        //Gionee:huangzy 20121107 modify for CR00682029 start
        // GIONEE: dingwen 20120704 add for CR00586298 begin
        /*if (mOverseaProduct) {
        	if (dialString.equals(GN_OVERSEA_FACTORY_VERSION)) {
        		return SpecialCharSequenceMgr.handleSecretCode(context, GN_NATIVE_FACTORY_VERSION);
        	} else if (dialString.equals(GN_NATIVE_FACTORY_VERSION)) {
        		return false;
        	}
        }
        // GIONEE: dingwen 20120704 add for CR00586298 end*/
        
        if (dialString.equals(GN_GIONEE_FACTORY_VERSION)) {
    		return SpecialCharSequenceMgr.handleSecretCode(context, GN_NATIVE_FACTORY_VERSION);
    	}
        //Gionee:huangzy 20121107 modify for CR00682029 end        
        
		// Gionee: 20121106 guoxiaotian add for CR00725416 begin
        // Gionee: 20120709 chenrui add for CR00640809 begin
        /*if (SystemProperties.get("ro.gn.oversea.custom").equals("SOUTH_AMERICA_BLU")) {
        	if (dialString.equals(GN_BLU_HW_VERSION)) {
                    showDialog(EXTER_VER_DIALOG, context);
        		return true;
        	}
        }*/
        if (gnBLUflag) {
        	if (dialString.equals(GN_BLU_HW_INFO)) {
                showDialog(BLU_HW_INFO_DIALOG, context);
				return true;
        	}
			else if (dialString.equals(GN_BLU_PCBA_VERSION)) {
				showDialog(BLU_PCBA_VER_DIALOG, context);
				return true;
			}			
        }
        // Gionee: 20120709 chenrui add for CR00640809 end
        // Gionee: 20121106 guoxiaotian add for CR00725416 end
        //GIONEE:wangfei 2012-08-28 modify for CR00681759 begin
        if(gnNgmflag){
            if(dialString.equals("*#22558463*#")){
                if(handleWmpCallTimerDisplayCode(context, dialString)){
                    return true;
                }
            } else if(dialString.equals("*#225584630000*#")){
                if(handleWmpCallTimerResteCode(context, dialString)){
                    return true;
                }
            }
        }
        //GIONEE:wangfei 2012-08-28 modify for CR00681759 end

        // Gionee guoyx 20121106 added for CR00725060 start
        if(dialString.equals(GN_SETTING_DEVELOPER_OPTIONS)){
            handleDeveloperOptionsEntry(context);
            return true;
        }
        // Gionee guoyx 20121106 added for CR00725060 end
        // Gionee xiaolin 20120319 modify for CR00544355 start
        if (handleIMEIDisplay(context, dialString, useSystemWindow)
                || handlePinEntry(context, dialString)
                || handleAdnEntry(context, dialString, textField)
                || handleSecretCode(context, dialString)
                // Gionee qinkai 2012-08-29 added for CR00682029 start
                || handleNGMSecretCode(context, dialString)
                // Gionee qinkai 2012-08-29 added for CR00682029 end
                || handleMMITestEntry(context, dialString)
                || handleS4Mmi(context, dialString)) {
            return true;
            // Gionee xiaolin 20120319 modify for CR00544355 end
        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        //Gionee:huangzy 20121107 add modify CR00682029 end
        
        
		if (GNContactsUtils.isIndia()) {
			if (dialString.equals(AURORA_INDIA_SAR)) {
				handleIndiaSAR(context);
				return true;
			}
		}
        
        return false;
    }
	
	// Gionee xiaolin 20120319 add for CR00544355 start  
    static boolean handleMMITestEntry(Context context, String input) {
		if (input.equals(MMI_TEST_ENTRY)) {
			context.startActivity(new Intent("gn.com.android.mmitest.MAIN"));
			return true;
		}	
		return false;
	}
	// Gionee xiaolin 20120319 add for CR00544355 end  
    
    // Gionee guoyx 20121106 added for CR00725060 start  
    static boolean handleDeveloperOptionsEntry(Context context) {
        Intent intent = Intent.makeRestartActivityTask(new ComponentName(
                "com.android.settings",
                "com.android.settings.DevelopmentSettings"));
        intent.setPackage(context.getApplicationInfo().packageName);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
        return true;
    }
    // Gionee guoyx 20120319 add for CR00725060 end 

    //Gionee qinkai 2012-08-29 added for CR00682029 start
    static boolean handleNGMSecretCode(Context context, String input) {
        // Secret codes are in the form *#<code>#
        Log.v("FactoryMode", "handleNGMSecretCode=" + input);
        int len = input.length();
        if (input != null && input.equals("*#84666364*#") && gnNgmflag) {
            Intent intent = new Intent("android.provider.Telephony.SECRET_CODE",
            Uri.parse("android_secret_code://" + input.substring(2, len - 2)));
            context.sendBroadcast(intent);
       	    Log.v("FactoryMode", "handleNGMSecretCode send");
            return true;
        }
        return false;
    }	
    //Gionee qinkai 2012-08-29 added for CR00682029 end
   //Gionee liming 2012-06-10 add for CR00623293 begin //tangzepeng 2012-8-30 merge begin 
    static boolean  handleSalesTrackSettingCode(Context context,String input){
        if(input.equals(MMI_SALESTRACKSETTING_DISPLAY)){
            getFlagFromGioneePartition();
            showGnSalesTrackSetting(context);
            return true;
        }
        return false;
    }
    
    static void getFlagFromGioneePartition(){
        File flagFile = new File("/gionee/"+SALESTRACK_FLAG_FILE);
        if (flagFile != null && flagFile.exists()) {
            salesIsSent = true;
        }else{
            salesIsSent = false;
        }
    }
    
    static void showGnSalesTrackSetting(final Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        View customerLayout = inflater.inflate(R.layout.gn_sales_setting, null);
        
        numText = (AuroraEditText)customerLayout.findViewById(R.id.number);
        timeText = (AuroraEditText)customerLayout.findViewById(R.id.time);
        mSalesSwitch = (CheckBox)customerLayout.findViewById(R.id.sales_switch);
        
        if(salesIsSent){
            mSalesSwitch.setChecked(false);
        }else{
            mSalesSwitch.setChecked(true);
        }
        
        mSalesSwitch
            .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    // TODO Auto-generated method stub
                    if (isChecked) {
                        salesIsSent = false;
                    }else{
                        salesIsSent = true;
                    }
                }
        });
        
        final AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
                                   .setTitle("SalesTrackSetting")
                                   .setView(customerLayout)
                                   .show();
        buttonSave = (AuroraButton) customerLayout.findViewById(R.id.save);
        buttonSave.setOnClickListener(new AuroraButton.OnClickListener(){
            public void onClick(View v){
                String salesNum = numText.getText().toString();
                String salesTime = timeText.getText().toString();
                /*
                if(salesNum.equals("") || salesTime.equals("")){
                    Toast mToast = null;
                    String msg = "Please input number and time !";
                    if (mToast != null){
                        mToast.cancel();
                        toast.setText(msg);
                        mToast.setDuration(Toast.LENGTH_SHORT);
                    }else{
                        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                    }
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                }else{
                */
                Intent intent = new Intent("gn.com.android.salestrack.setting");
                intent.putExtra("salesNum", salesNum);
                intent.putExtra("salesTime", salesTime);
                intent.putExtra("salesIsSent",salesIsSent);         
                context.startService(intent);
                dialog.dismiss();
                /*
                }
                */
           }
        });
        buttonExit = (AuroraButton) customerLayout.findViewById(R.id.exit);
        buttonExit.setOnClickListener(new AuroraButton.OnClickListener(){
            public void onClick(View v){
                dialog.dismiss();
           }
        });
    }
    //Gionee liming 2012-06-10 add for CR00623293 end //tangzepeng 2012-8-31 merge end

	//GIONEE:wangfei 2012-08-28 modify for CR00681759 begin
    static boolean handleWmpCallTimerDisplayCode(Context context, String input) {
        AuroraAlertDialog dialog = null;
        int len = input.length();
        if (input != null && input.equals("*#22558463*#")) {
        String totalcalls;
        String receivedcalls;
        String dialedcalls;
        String calltimers = "";
        int totaltime;
        int totalInTime;
        int totalOutTime;

        try{
            Context NgmWmpContext = context.createPackageContext("gn.com.android.ngmwmp", Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sharedPref = NgmWmpContext.getSharedPreferences("NgmWmp", Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
            totaltime = sharedPref.getInt("totaltime", 0);
            totalInTime = sharedPref.getInt("totalintime", 0);
            totalOutTime = sharedPref.getInt("totalouttime", 0);

             totalcalls = String.format("%02d:%02d:%02d",(int) totaltime / 3600, (int) totaltime / 60 % 60,
                             (int) totaltime % 60);
             receivedcalls = String.format("%02d:%02d:%02d",(int) totalInTime / 3600, (int) totalInTime / 60 % 60,
                             (int) totalInTime % 60);
             dialedcalls = String.format("%02d:%02d:%02d",(int) totalOutTime / 3600, (int) totalOutTime / 60 % 60,
                             (int) totalOutTime % 60);
             
             calltimers += "Total Calls" + "\n" + totalcalls + "\n\n" + "Received Calls" + "\n" + receivedcalls
                             + "\n\n" + "Dialed Calls" + "\n" + dialedcalls + "\n";
                         
             dialog = new AuroraAlertDialog.Builder(context)
                  .setTitle("Calls Timers")
                  .setMessage(calltimers)
                  .setPositiveButton(context.getResources().getString(R.string.close),new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                         dialog = null;
                     }
             }
            ).create();
			dialog.show();

        }catch(PackageManager.NameNotFoundException e){
        }
            return true;
        }

        return false;
    }

    
    static boolean handleWmpCallTimerResteCode(Context context, String input) {
        int len = input.length();
        if (input != null && input.equals("*#225584630000*#")) {
            Intent intent = new Intent("gn.com.android.ngmwmp.action.RESET_CALL_TIME");
            context.startService(intent);
            Toast.makeText(context,"Reset call time", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }
	//GIONEE:wangfei 2012-08-28 modify for CR00681759 end

    static boolean handleIMEIDisplay(Context context, String input, boolean useSystemWindow) {
        if(ContactsApplication.sGemini) {
            if (input.equals(MMI_IMEI_DISPLAY)) {
                int phoneType = ((TelephonyManager)context.getSystemService(
                        Context.TELEPHONY_SERVICE)).getCurrentPhoneType();

                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    showIMEIPanel(context, useSystemWindow);
                    return true;
                } else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    showMEIDPanel(context, useSystemWindow);
                    return true;
                }
            }
            return false;
        } else
            return SpecialCharSequenceMgr.handleIMEIDisplay(context, input, useSystemWindow);
    }

    static boolean handlePinEntry(Context context, String input) {
        if(ContactsApplication.sGemini) {
            final ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if ((input.startsWith("**04") || input.startsWith("**05")) && input.endsWith("#")) {
                try {
                    final String _input = input;
                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //convert the click item to slot id
                            final AuroraAlertDialog alert = (AuroraAlertDialog) dialog;
                            final ListAdapter listAdapter = alert.getListView().getAdapter();
                            final int slot = ((Integer)listAdapter.getItem(which)).intValue();
                            
                            try {
                                Log.d(TAG, "handlePinMmiGemini, slot " + slot);
                                GnITelephony.handlePinMmiGemini(phone, _input, slot);
                            } catch(Exception e) {
                                Log.d(TAG, "exception : "+e.getMessage());
                            }
                            
                            dialog.dismiss();
                        }
                    };

                    final long defaultSim = ContactsSettingsUtils.getDefaultSIMForVoiceCall();

                    if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_NOT_SET ||
                            defaultSim == ContactsSettingsUtils.VOICE_CALL_SIM_SETTING_INTERNET) {
                        return false;
                    }

                    final SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getDefault();
                    int simCount = simInfoWrapper.getInsertedSimCount();

                    if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_SETTING_ALWAYS_ASK && simCount == 2) {
                        AuroraAlertDialog dialog = SimPickerDialog.create(context, context.getResources().getString(R.string.call_pin_dialog_title), onClickListener);
                        dialog.show();
                        return true;
                    } else {
                        // default sim is internet, nothing to do
                        if(defaultSim == ContactsSettingsUtils.VOICE_CALL_SIM_SETTING_INTERNET)
                            return false;

                        // default sim is always ask but sim count < 2
                        if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                            return GnITelephony.handlePinMmiGemini(phone, _input, simInfoWrapper.getInsertedSimInfoList().get(0).mSlot);
                        }

                        final int slot = simInfoWrapper.getSimSlotById((int)defaultSim);
                        return GnITelephony.handlePinMmiGemini(phone, _input, slot);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to handlePinMmi due to remote exception");
                    return false;
                }
            }
            return false;
        } else
            return SpecialCharSequenceMgr.handlePinEntry(context, input);
    }

	private static boolean is7505() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("7505");
	}
    
    static void showIMEIPanel(Context context, boolean useSystemWindow) {
        if (ContactsApplication.sGemini) {
            final TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            //Gionee zjy 20120329 add for CR00560679 start
            /*final CharSequence[] imeiStrs = new CharSequence[2];*/
            CharSequence[] imeiStrs = null;
			if (!mSingleIMEI) {
				imeiStrs = new CharSequence[2];
			} else {
				imeiStrs = new CharSequence[1];
			}
			
            imeiStrs[0] = GnTelephonyManager.getDeviceIdGemini(ContactsFeatureConstants.GEMINI_SIM_1);
            
			if (!mSingleIMEI) {
				imeiStrs[1] = GnTelephonyManager.getDeviceIdGemini(ContactsFeatureConstants.GEMINI_SIM_2);
			}

            if(TextUtils.isEmpty(imeiStrs[0]))
                imeiStrs[0] = context.getResources().getString(R.string.imei_invalid);
            
			if (!mSingleIMEI) {
				if (TextUtils.isEmpty(imeiStrs[1]))
					imeiStrs[1] = context.getResources().getString(R.string.imei_invalid);
			}
			

            AuroraAlertDialog alert = new AuroraAlertDialog.Builder(context).setTitle(R.string.imei).setTitleDividerVisible(true)
                    .setItems(imeiStrs, null).setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false).create();
            alert.show();
        } else
            SpecialCharSequenceMgr.showIMEIPanel(context, useSystemWindow);
    }

    static void showMEIDPanel(Context context, boolean useSystemWindow) {
        if (ContactsApplication.sGemini) {
            final TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            //Gionee zjy 20120329 add for CR00560679 start
            /*final CharSequence[] imeiStrs = new CharSequence[2];*/
            CharSequence[] imeiStrs = null;
			if (!mSingleIMEI) {
				imeiStrs = new CharSequence[2];
			} else {
				imeiStrs = new CharSequence[1];
			}
			
            imeiStrs[0] = GnTelephonyManager.getDeviceIdGemini(ContactsFeatureConstants.GEMINI_SIM_1);
            
            /*imeiStrs[1] = telephonyManager.getDeviceIdGemini(Phone.GEMINI_SIM_2);*/
			if (!mSingleIMEI) {
				imeiStrs[1] = GnTelephonyManager.getDeviceIdGemini(ContactsFeatureConstants.GEMINI_SIM_2);
			}

            if(TextUtils.isEmpty(imeiStrs[0]))
                imeiStrs[0] = context.getResources().getString(R.string.imei_invalid);
            
            
            /*if(TextUtils.isEmpty(imeiStrs[1]))
                imeiStrs[1] = context.getResources().getString(R.string.imei_invalid);*/
			if (!mSingleIMEI) {
				if (TextUtils.isEmpty(imeiStrs[1]))
					imeiStrs[1] = context.getResources().getString(R.string.imei_invalid);
			}
            //Gionee zjy 20120329 add for CR00560679 end
			
			if(is7505()) {
				 imeiStrs[0] = context.getResources().getString(R.string.meid) +  " : " + imeiStrs[0]  ;
				 imeiStrs[1] =  "IMEI : " + imeiStrs[1] ;
			}

            AuroraAlertDialog alert = new AuroraAlertDialog.Builder(context).setTitle(R.string.imei).setTitleDividerVisible(true)
                    .setItems(imeiStrs, null).setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false).create();
            alert.show();
        } else
        SpecialCharSequenceMgr.showMEIDPanel(context, useSystemWindow);
    }

    static boolean handleAdnEntry(Context context, String input, AuroraEditText textField) {
        log("handleAdnEntry, input = " + input);
        if(ContactsApplication.sGemini) {
            KeyguardManager keyguardManager =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager.inKeyguardRestrictedInputMode()) {
                return false;
            }

            int len = input.length();
            if ((len > 1) && (len < 5) && (input.endsWith("#"))) {
                try {
                    // get the ordinal number of the sim contact
                    int index = -1;
                    try {
                        index = Integer.parseInt(input.substring(0, len-1));
                        if(index <= 0)
                            return false;
                    } catch(Exception e) {
                        return false;
                    }
                    

                    // The original code that navigated to a SIM Contacts list view did not
                    // highlight the requested contact correctly, a requirement for PTCRB
                    // certification.  This behaviour is consistent with the UI paradigm
                    // for touch-enabled lists, so it does not make sense to try to work
                    // around it.  Instead we fill in the the requested phone number into
                    // the dialer text field.

                    // create the async query handler
                    QueryHandler handler = new QueryHandler (context.getContentResolver());

                    // create the cookie object
                    // index in SIM
                    SimContactQueryCookie sc = new SimContactQueryCookie(index, handler, 0);

                    // setup the cookie fields
                    sc.contactNum = index;
                    sc.setTextField(textField);
                    if (null != textField) {
                        sc.text = textField.getText().toString();
                    } else {
                        sc.text = null;
                    }
                    log("index = " + index);

                    // create the progress dialog
                    if(null == sc.progressDialog) {
                        sc.progressDialog = new AuroraProgressDialog(context);
                    }
                    sc.progressDialog.setTitle(R.string.simContacts_title);
                    sc.progressDialog.setMessage(context.getText(R.string.simContacts_emptyLoading));
                    sc.progressDialog.setIndeterminate(true);
                    sc.progressDialog.setCancelable(true);
                    sc.progressDialog.setOnCancelListener(sc);
                    sc.progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                    sc.context = context;

                    final long defaultSim = Settings.System.getLong(context.getContentResolver(), ContactsFeatureConstants.VOICE_CALL_SIM_SETTING, -3);

                    if(defaultSim == ContactsSettingsUtils.VOICE_CALL_SIM_SETTING_INTERNET ||
                       defaultSim == ContactsSettingsUtils.DEFAULT_SIM_NOT_SET) {
                        return false;
                    }

                    final SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getDefault();
                    int simCount = simInfoWrapper.getInsertedSimCount();

                    int slot = ADN_QUERY_TOKEN_SIM1;
                    if(defaultSim == ContactsSettingsUtils.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                        if(!TelephonyUtils.isRadioOnInner(0) && !TelephonyUtils.isRadioOnInner(1)) {
                            log("radio power off, bail out");
                            return false;
                        }

                        final boolean sim1Ready = TelephonyUtils.isSimReadyInner(0);
                        final boolean sim2Ready = TelephonyUtils.isSimReadyInner(1);

                        if(!sim1Ready && !sim2Ready) {
                            log("sim not ready, bail out");
                            return false;
                        }

                        sc.doubleQuery = false;

                        if(!sim1Ready && sim2Ready) {
                            slot = ADN_QUERY_TOKEN_SIM2;
                        } else if(sim1Ready && sim2Ready) {
                            sc.doubleQuery = true;
                        }
                        log("sim1Ready = " + sim1Ready + " sim2Ready = " + sim2Ready + " doubleQuery = " + sc.doubleQuery);
                    } else {
                        slot = simInfoWrapper.getSimSlotById((int)defaultSim);

                        if(!TelephonyUtils.isRadioOn((int)defaultSim) || !TelephonyUtils.isSimReady((int)defaultSim)) {
                            log("radio power off or sim not ready, bail out");
                            return false;
                        }
                    }

                    Uri uri = Uri.parse(buildSIMContactQueryUri(slot));
                    log("slot = " + slot + " uri = " + uri);
                    sc.progressDialog.show();
                    handler.startQuery(slot, sc, uri, new String[] { ADN_PHONE_NUMBER_COLUMN_NAME,ADN_INDEX_COLUMN_NAME },
                            null, null, null);
                } catch(Exception e) {
                    Log.d(TAG, e.getMessage());
                }
                return true;
            }
            return false;
        } else
            return SpecialCharSequenceMgr.handleAdnEntry(context, input, textField);
    }

    static boolean handleSecretCode(Context context, String input) {
        // Secret codes are in the form *#*#<code>#*#*
        return SpecialCharSequenceMgr.handleSecretCode(context, input);
    }

    static String buildSIMContactQueryUri(int slot) {
        StringBuilder builder = new StringBuilder();
        if(TelephonyUtils.isUSIMInner(slot)) {
            builder.append(USIM_CONTACT_URI);
        } else
            builder.append(SIM_CONTACT_URI);

        // slot 0/1 ---> adn1/2
        builder.append(slot+1);
        return builder.toString();
    }

    private static class SimContactQueryCookie implements DialogInterface.OnCancelListener{
        public AuroraProgressDialog progressDialog;
        public int contactNum;
        public boolean doubleQuery;

        // Used to identify the query request.
        private int mToken;
        private QueryHandler mHandler;

        // The text field we're going to update
        private AuroraEditText textField;
        public String text;

        public Context context;
        public String[] simNumber = new String[2];
        public String[] simName = new String[2];
        public boolean[] find = new boolean[2];

        public SimContactQueryCookie(int number, QueryHandler handler, int token) {
            contactNum = number;
            mHandler = handler;
            mToken = token;
        }

        /**
         * Synchronized getter for the EditText.
         */
        public synchronized AuroraEditText getTextField() {
            return textField;
        }

        public synchronized QueryHandler getQueryHandler(){
            return mHandler;
        }
        
        /**
         * Synchronized setter for the EditText.
         */
        public synchronized void setTextField(AuroraEditText text) {
            textField = text;
        }

        /**
         * Cancel the ADN query by stopping the operation and signaling
         * the cookie that a cancel request is made.
         */
        public synchronized void onCancel(DialogInterface dialog) {
            // close the progress dialog
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            // setting the textfield to null ensures that the UI does NOT get
            // updated.
            textField = null;

            // Cancel the operation if possible.
            mHandler.cancelOperation(mToken);
        }
    }

    private static class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void showToast(Context context, SimContactQueryCookie sc, String name, String number) {
            final AuroraEditText text = sc.textField;
            int len = number != null ? number.length() : 0;
            
            if (sc.text.equals(number)) {
                Toast .makeText(context, context.getString(R.string.non_phone_caption) + "\n" + number, Toast.LENGTH_LONG).show();
            } else if ((len > 1) && (len < 5) && (number.endsWith("#"))) {
                Toast.makeText(context, context.getString(R.string.non_phone_caption) + "\n" + number, Toast.LENGTH_LONG).show();
            } else {
                // fill the text in.
                text.setText(number);
                text.setSelection(text.getText().length());

                // display the name as a toast
                name = context.getString(R.string.menu_callNumber, name);
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Override basic onQueryComplete to fill in the textfield when
         * we're handed the ADN cursor.
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            log("onQueryComplete token = "+token);
            final SimContactQueryCookie sc = (SimContactQueryCookie) cookie;
            final Context context = sc.progressDialog.getContext();
            AuroraEditText text = sc.getTextField();

            String name = null;
            String number = null;

            if (c != null && text != null) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    if ((token == ADN_QUERY_TOKEN_SIM1)&& fdnRequest(ADN_QUERY_TOKEN_SIM1)){
                        break;
                    }	
					
                    if ((token == ADN_QUERY_TOKEN_SIM2)&& fdnRequest(ADN_QUERY_TOKEN_SIM2)){
                        break;
                    }	
					
                    if (c.getInt(c.getColumnIndexOrThrow(ADN_INDEX_COLUMN_NAME)) == sc.contactNum) {
                        name = c.getString(c.getColumnIndexOrThrow(ADN_NAME_COLUMN_NAME));
                        number = c.getString(c.getColumnIndexOrThrow(ADN_PHONE_NUMBER_COLUMN_NAME));
                        sc.find[token] = true;
                        break;
                    }
                }
                c.close();
            }

            log("sc.find["+token+"] "+sc.find[token]);

            sc.simName[token] = name;
            sc.simNumber[token] = number;
            log("name = " + name + " number = " + number);

            if(!sc.doubleQuery) {
                if (sc.progressDialog != null && sc.progressDialog.isShowing()) {
                    sc.progressDialog.dismiss();
                    sc.progressDialog = null;
                }

                if(sc.find[token]) {
                    showToast(context, sc, name, number);
                } // findFlag
            } else {
                if(token == ADN_QUERY_TOKEN_SIM2) {
                    if (sc.progressDialog != null && sc.progressDialog.isShowing()) {
                        sc.progressDialog.dismiss();
                        sc.progressDialog = null;
                    }

                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                final AuroraAlertDialog alert = (AuroraAlertDialog) dialog;
                                final ListAdapter listAdapter = alert.getListView().getAdapter();
                                final int slot = ((Integer)listAdapter.getItem(which)).intValue();
                                log("onClick slot = " + slot);

                                if(sc.find[slot]) {
                                    showToast(context, sc, sc.simName[slot], sc.simNumber[slot]);
                                }
                                dialog.dismiss();
                            } catch(Exception e) {
                                Log.d(TAG, "exception : "+e.getMessage());
                            }
                        }
                    };

                    AuroraAlertDialog dialog = SimPickerDialog.create(context, context.getString(R.string.call_pin_dialog_title), false, onClickListener);
                    dialog.show();
              } else {
                  QueryHandler handler = sc.getQueryHandler();
                  Uri uri = Uri.parse(buildSIMContactQueryUri(ADN_QUERY_TOKEN_SIM2));
                  handler.startQuery(ADN_QUERY_TOKEN_SIM2, sc, uri, new String[] { ADN_PHONE_NUMBER_COLUMN_NAME,ADN_INDEX_COLUMN_NAME },
                        null, null, null);
              }
           }
        }
    }

	/*
	 * public static boolean fdnRequest(int slot) {
	 * 
	 * Phone phone = PhoneFactory.getDefaultPhone(); if (null == phone) {
	 * Log.e(TAG, "fdnRequest phone is null"); return false; } IccCard iccCard;
	 * if (true == FeatureOption.MTK_GEMINI_SUPPORT) { iccCard = ((GeminiPhone)
	 * phone).getIccCardGemini(slot); } else { iccCard = phone.getIccCard(); }
	 * 
	 * return iccCard.getIccFdnEnabled(); }
	 */
	static boolean fdnRequest(int slot) {

		boolean bRet = false;

		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (null == iTel) {
			Log.e(TAG, "fdnRequest iTel is null");
			return false;
		}

		try {
			if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
				bRet = GnITelephony.isFDNEnabledGemini(iTel, slot);
			} else {
				bRet = GnITelephony.isFDNEnabled(iTel);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}

		Log.d(TAG, "fdnRequest fdn enable is " + bRet);
		return bRet;
	}

    static void log(String msg) {
        Log.d(TAG, msg);
    }
   
    //GIONEE zjy 20120321 add for CR00555135 search version   start
    public static void  showDialog(int id,Context context) {
        AuroraAlertDialog dialog = null;
        
        switch(id) {
            case INTER_VER_DIALOG:{
                String gnznvernumber = SystemProperties.get("ro.gn.gnznvernumber");
                String type = SystemProperties.get("ro.build.type");
                String gnRom = SystemProperties.get("ro.gn.gnromvernumber");
				String gnvernumber = SystemProperties.get("ro.gn.gnvernumber");
				String buildTime = SystemProperties.get("ro.build.date");
				String gnvernumberrel = SystemProperties.get("ro.gn.gnvernumberrel");
				String uct = SystemProperties.get("ro.build.date.utc");
				String baseband = SystemProperties.get("gsm.version.baseband", "Unknown");
	
				ContentResolver cv = context.getContentResolver();
				// GIONEE: luohui 2012-06-19 modify for CR00625161 minute wrong
				// start->
				// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:dd");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				// GIONEE: luohui 2012-06-19 modify for CR00625161 minute wrong
				// end<-
				if (uct != null && !uct.equals("")) {
					buildTime = sdf.format(Long.parseLong(uct) * 1000);
				}
				
				//Gionee <huangzy> <2013-04-24> add for CR00800987 begin
				Resources resources = context.getResources();
				final String gnFtBtResultSuccess = resources.getString(
						R.string.gn_ft_bt_result_success);
				final String gnFtBtResultFail = resources.getString(
						R.string.gn_ft_bt_result_fail);
				final String gnFtBtResultNo = resources.getString(
						R.string.gn_ft_bt_result_no);
				
				String BtResultWCDMA = gnFtBtResultNo;
				String FtResultWCDMA = gnFtBtResultNo;

				String BtResultGSM = gnFtBtResultNo;
				String FtResultGSM = gnFtBtResultNo;
				
		        String BtResultTD = gnFtBtResultNo;
		        String FtResultTD = gnFtBtResultNo;

						//iuni gary.gou 20140514 modify  start
			    String gsmc2btResult = gnFtBtResultNo;
			    String gsmc2ftResult = gnFtBtResultNo;
			    String gsmc2antResult = gnFtBtResultNo;
			    //iuni gary.gou 20140514 modify  end
			    
			    // Gionee zhangxiaowei 20131116 modify for CR00935598 start
			    String tdbtResult = gnFtBtResultNo;
			    String tdftResult = gnFtBtResultNo;
			    // Gionee zhangxiaowei 20131116 modify for CR00935598 end
			    String lbtResult = gnFtBtResultNo;
			    String lftResult = gnFtBtResultNo;

				String ltefddbtResult = gnFtBtResultNo; 
                String ltefddftResult = gnFtBtResultNo; 
		        //Gionee <huangzy> <2013-04-24> add for CR00800987 end
	
				TelephonyManager teleMgr = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);				
				String sn =GnTelephonyManager.getSN();//aurora change zhouxiaobing 20140211
				
				if (sn != null) {
				    Log.d(TAG, "SN is " + sn);
					char[] barcodes = sn.toCharArray();
					// Gionee:wangth 20130301 modify for CR00773823 begin
					if (barcodes != null) {
						if (GNContactsUtils.isOnlyQcContactsSupport()) { // qc
						    //Gionee <wangth><2013-04-17> modify for CR00798034 begin
							if (barcodes.length >=9) {
								if ('P' == barcodes[5])
									BtResultGSM = gnFtBtResultSuccess;
								if ('F' == barcodes[5])
								    BtResultGSM = gnFtBtResultFail;
	
								if ('P' == barcodes[6])
									FtResultGSM = gnFtBtResultSuccess;
								if ('F' == barcodes[6])
									FtResultGSM = gnFtBtResultFail;
								
								if ('P' == barcodes[7])
								    BtResultWCDMA = gnFtBtResultSuccess;
								if ('F' == barcodes[7])
								    BtResultWCDMA = gnFtBtResultFail;
								
								if ('P' == barcodes[8])
								    FtResultWCDMA = gnFtBtResultSuccess;
								if ('F' == barcodes[8])
								    FtResultWCDMA = gnFtBtResultFail;
								//iuni gary.gou 20140514 modify  start
							  if(barcodes.length >=32)
							  	{
									  //iuni gary.gou 20140514 modify  start
									if('P' == barcodes[19]){
									  gsmc2btResult =  gnFtBtResultSuccess;
									}
									if('F' == barcodes[19]){
									  gsmc2btResult =  gnFtBtResultFail;
									}

									if('P' == barcodes[20]){
									  gsmc2ftResult =  gnFtBtResultSuccess;
									}
									if('F' == barcodes[20]){
									  gsmc2ftResult =  gnFtBtResultFail;
									}

									if('P' == barcodes[21]){
									  gsmc2antResult =	gnFtBtResultSuccess;
									}
									if('F' == barcodes[21]){
									  gsmc2antResult =	gnFtBtResultFail;
									}
									//iuni gary.gou 20140514 modify  end


									// Gionee zhangxiaowei 20131116 modify for CR00935598 start
									if('P' == barcodes[26])
									  tdbtResult = gnFtBtResultSuccess;
									if ('F' == barcodes[26])
									  tdbtResult = gnFtBtResultFail;

									if('P' == barcodes[27])
									  tdftResult = gnFtBtResultSuccess;
									if ('F' == barcodes[27])
									  tdftResult = gnFtBtResultFail;

									if('P' == barcodes[28])
									lbtResult = gnFtBtResultSuccess;
									if('F' == barcodes[28])
									lbtResult = gnFtBtResultFail;

									if('P' == barcodes[29])
									lftResult = gnFtBtResultSuccess;
									if('F' == barcodes[29])
									lftResult = gnFtBtResultFail;

									//Begin add by gary.gou 20140707
									if('P' == barcodes[30]){
									ltefddbtResult = gnFtBtResultSuccess;
									}
									if('F' == barcodes[30]){
									ltefddbtResult = gnFtBtResultFail;
									}

									if('P' == barcodes[31]){
									ltefddftResult = gnFtBtResultSuccess;
									}
									if('F' == barcodes[31]){
									ltefddftResult = gnFtBtResultFail;
									}
									//End add by gary.gou 20140707

							  	}
							}
							//Gionee <wangth><2013-04-17> modify for CR00798034 end
						} else { // mtk
							//Gionee <huangzy><2013-04-24> add for CR00800987 begin
							if(barcodes.length >= 47){
				            	if ('P' == barcodes[46])
				            		BtResultTD = gnFtBtResultSuccess;
				            	else if ('F' == barcodes[46])
				            		BtResultTD = gnFtBtResultFail;
				            }				            
				            if(barcodes.length >=48 ) {
				            	if ('P' == barcodes[47])
				            		FtResultTD = gnFtBtResultSuccess;
				            	else if ('F' == barcodes[47])
				            		FtResultTD = gnFtBtResultFail;
				            }
				            
							if(barcodes != null && barcodes.length >= 59){
				            	if ('P' == barcodes[58])
				            		BtResultWCDMA = gnFtBtResultSuccess;
				            	else if ('F' == barcodes[58])
				            		BtResultWCDMA = gnFtBtResultFail;
				            }							
				            if(barcodes != null && barcodes.length >=60 ) {
				            	if ('P' == barcodes[59])
				            		FtResultWCDMA = gnFtBtResultSuccess;
				            	else if ('F' == barcodes[59])
				            		FtResultWCDMA = gnFtBtResultFail;
				            }
				            
				            if (barcodes.length >= 62) {
				                if ('1' == barcodes[60] && '0' == barcodes[61]) {
				                    BtResultGSM = gnFtBtResultSuccess;
				                } else if ('0' == barcodes[60] && '1' == barcodes[61]) {
				                    BtResultGSM = gnFtBtResultFail;
				                }
				            }
				            if (barcodes.length >= 63) {
				                if ('P' == barcodes[62]) {
				                    FtResultGSM = gnFtBtResultSuccess;
				                } else if ('F' == barcodes[62]) {
				                    FtResultGSM = gnFtBtResultFail;
				                }
				            }
				            //Gionee <huangzy><2013-04-24> add for CR00800987 end
						}
					}
					// Gionee:wangth 20130301 modify for CR00773823 end
			}

            if (buildTime == null || buildTime.equals("")) {
                buildTime = resources.getString(R.string.isnull);
            }

            if (gnvernumber == null || gnvernumber.equals("")) {
                gnvernumber = resources.getString(R.string.isnull);
            } 
            //Gionee zjy 20120530 remove for CR00611942 start
            /*else {
                String[] gnvernumbers = gnvernumber.split("_");
                String splitGnvernumbers = "";
                int length = gnvernumbers.length;
                if (gnvernumbers != null && gnvernumbers.length > 0) {
                    for (int i = 0; i < length; i++) {
                        if (i == length - 1) {
                            splitGnvernumbers += gnvernumbers[i];
                        } else if (i == length - 2) {
                            if(!TextUtils.isEmpty(gnvernumberrel)){
                                splitGnvernumbers += gnvernumbers[i].substring(0, 1) + gnvernumberrel + "-";
                            } else {
                            if (gnvernumbers[i].length() > 2) {
                                splitGnvernumbers += gnvernumbers[i].substring(
                                        0, 3) + "-";
                            }
                            }
                        } else {
                            splitGnvernumbers += gnvernumbers[i] + "-";
                        }
                    }
                    gnvernumber = splitGnvernumbers;
                }
            }*/
            //Gionee zjy 20120530 remove for CR00611942 end            
            
            //Gionee <huangzy> <2013-04-24> add for CR00800987 begin
            StringBuilder messageBuilder = new StringBuilder(250);
            String typePart = ("user".equals(type) ? "" : "_" + type);
            String basebandPart = (TextUtils.isEmpty(baseband) ? "" : "\n" + baseband);
            boolean isTdSupport = false;//SystemProperties.getBoolean("gn.mmi.tdscdma", false);
	        
        	messageBuilder
        		.append("[").append(resources.getString(R.string.external_version)).append("]")
        		.append("\n").append(gnznvernumber).append(typePart)
        		.append("\n").append(gnRom).append(basebandPart);
        	
        	if (isTdSupport) {
        		messageBuilder
	        		.append("\n[BT] GSM ").append(BtResultGSM)
	    			.append("; TD-SCDMA ").append(BtResultTD)
	    			.append("\n[FT] GSM ").append(FtResultGSM)
	    			.append("; TD-SCDMA ").append(FtResultTD);
        	} else {
        	  if(ContactsApplication.isMultiSimEnabled)
        	  {
				  messageBuilder
					  .append("\n[BT] GSM ").append(BtResultGSM)
					  .append("; TD-SCDMA ").append(tdbtResult)
					  .append("; WCDMA ").append(BtResultWCDMA)
					  .append("; GSMC2 ").append(gsmc2btResult)
					  .append("; LTETDD ").append(lbtResult)
					  .append("; LTEFDD ").append(ltefddbtResult)
					  .append("\n[FT] GSM ").append(FtResultGSM)
					  .append("; TD-SCDMA ").append(tdftResult)
					  .append("; WCDMA ").append(FtResultWCDMA)
					  .append("; GSMC2 ").append(gsmc2ftResult)
					  .append("; LTETDD ").append(lftResult)
					  .append("; LTEFDD ").append(ltefddftResult);

			  }
			  else
			  {
        		messageBuilder
	    			.append("\n[BT] GSM ").append(BtResultGSM)
	    			.append("; WCDMA ").append(BtResultWCDMA)
	    			.append("\n[FT] GSM ").append(FtResultGSM)
	    			.append("; WCDMA ").append(FtResultWCDMA);
			  }
        	}
			//iuni gary.gou 20140514 modify start
/*		if(GNContactsUtils.isMultiSimEnabled())
			{
				if (SystemProperties.getBoolean("gn.mmi.gsmc2", true)) {
					messageBuilder.append("\nGSMC2 BT:" + gsmc2btResult + "\nGSMC2 FT:" + gsmc2ftResult);
				}
				//iuni gary.gou 20140514 modify end
				
				if (SystemProperties.getBoolean("gn.mmi.tdscdma", true)) {
					//messageBuilder.append("\nTD-SCDMA BT:" + tdbtResult + "\nTD-SCDMA FT:" + tdftResult);
				}
				if (SystemProperties.getBoolean("gn.mmi.ltetdd", true)) {
					 messageBuilder.append( "\nLTETDD BT:" + lbtResult + "\nLTETDD FT:" + lftResult);
				}
				 
				//Begin add by gary.gou 20140707 
				if (SystemProperties.getBoolean("gn.mmi.ltefdd", true)) {
					 messageBuilder.append("\nLTEFDD BT:" + ltefddbtResult + "\nLTEFDD FT:" + ltefddftResult);
				}
				//End add by gary.gou 20140707
			}*/
        	messageBuilder
        		.append("\n[").append(resources.getString(R.string.buildtime)).append("]")
        		.append(buildTime);
	        //Gionee <huangzy> <2013-04-24> add for CR00800987 end
        	
            dialog = new AuroraAlertDialog.Builder(context)
                 .setTitle(R.string.internal_version)
                 .setMessage(messageBuilder.toString())
                 .setPositiveButton(resources.getString(R.string.close),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog = null;
                        }
                 }      
                ).create();
            dialog.show();
                break;
            }
            case EXTER_VER_DIALOG:{
                // GIONEE: dingwen 20120407 add for CR00564842 Oversea Verion number begin
                String extVerNumber = "";
                if(mOverseaProduct) {
                    String gnExtHwVerNo = SystemProperties.get("ro.gn.extHWvernumber");
                    String gnExtSwVerNo =SystemProperties.get("ro.gn.extvernumber");
                    String gnModel =SystemProperties.get("ro.product.model"); 

                    gnExtSwVerNo = gnModel + gnExtSwVerNo.substring(gnExtSwVerNo.indexOf("_"), gnExtSwVerNo.length());
                    gnExtHwVerNo = gnModel + "_" + gnExtHwVerNo;
                    //GIONEE: lujian 2012.07.30 modify for "fly CR00659144" begin
                    if(gnFlyFlag) {
                    	String uct = SystemProperties.get("ro.build.date.utc");
                   	    String gnbuildTime = SystemProperties.get("ro.build.date");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
                        if (uct != null && !uct.equals("")) {
                            gnbuildTime = sdf.format(Long.parseLong(uct) * 1000);
                        }
                    	String gnFlyVerString = SystemProperties.get("ro.gn.fly.version.number");
                    	gnExtSwVerNo= "SW"+ gnFlyVerString + "_FLY_" + gnModel + "_" + gnbuildTime;
                    }
                    //GIONEE: lujian 2012.07.30 modify for "fly CR00659144"   end
                    extVerNumber = "[SW VERSION]\n" + gnExtSwVerNo + "\n[HW VERSION]\n" + gnExtHwVerNo;
                } else {
                    extVerNumber = SystemProperties.get("ro.build.display.id"); 
                }
                // GIONEE: dingwen 20120407 add for CR00564842 Oversea Verion number end

                if (extVerNumber == null || extVerNumber.equals("")) {
                    extVerNumber = context.getResources().getString(R.string.isnull);
                }
                dialog = new AuroraAlertDialog.Builder(context)
                     .setTitle(R.string.external_version)
                     .setMessage(extVerNumber)
                     .setPositiveButton(context.getResources().getString(R.string.close),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog = null;
                        }
                }
               ).create();
                dialog.show();
                break;
            }
			// Gionee: 20121106 guoxiaotian add for CR00725416 begin
            case BLU_HW_INFO_DIALOG:{
				String HwInfo = "";
                String gnExtHwVerNo = SystemProperties.get("ro.gn.extHWvernumber");
                String gnModel =SystemProperties.get("ro.product.model"); 
                
                gnExtHwVerNo = gnModel + "_" + gnExtHwVerNo;
				HwInfo = "[HW VERSION]\n" + gnExtHwVerNo;
                if (HwInfo == null || HwInfo.equals("")) {
                    HwInfo = context.getResources().getString(R.string.isnull);
                }
                dialog = new AuroraAlertDialog.Builder(context)
                    .setTitle("HW Version")
                    .setMessage(HwInfo)
                    .setPositiveButton(context.getResources().getString(R.string.close),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog = null;
                        }
                    }
                    ).create();
                dialog.show();
                break;
            }
            case BLU_PCBA_VER_DIALOG:{
                String PCBAVerNumber = "[PCBA]\nWBWS215BI_0301";

                if (PCBAVerNumber == null || PCBAVerNumber.equals("")) {
                    PCBAVerNumber = context.getResources().getString(R.string.isnull);
                }
                dialog = new AuroraAlertDialog.Builder(context)
                    .setTitle("PCBA Version")
                    .setMessage(PCBAVerNumber)
                    .setPositiveButton(context.getResources().getString(R.string.close),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog = null;
                        }
                    }
                    ).create();
                dialog.show();
                break;
            }
            // Gionee: 20121106 guoxiaotian add for CR00725416 end

            case GN_GBW_INTER_VER_DIALOG:{
                String gnznvernumber = SystemProperties.get("ro.gn.gnznvernumber");
                String type = SystemProperties.get("ro.build.type");
                String gnRom = SystemProperties.get("ro.gn.gnromvernumber");
                if (gnznvernumber == null || gnznvernumber.equals("")) {
                    gnznvernumber = context.getResources().getString(R.string.isnull);
                } 
                if (gnRom == null || gnRom.equals("")) {
                	gnRom = context.getResources().getString(R.string.isnull);
                } 
                if (type == null || type.equals("")) {
                    type = context.getResources().getString(R.string.isnull);
                }
                dialog = new AuroraAlertDialog.Builder(context)
                    .setTitle(R.string.gn_gbw_internal_v)
                    .setMessage(gnznvernumber+"_"+type+"\n"+gnRom)
                    .setPositiveButton(
                            context.getResources().getString(R.string.close),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    dialog.cancel();
                                    dialog = null;
                                }
                }
               ).create();
                dialog.show();
                break;
            }
 
            // GIONEE licheng May 8, 2012 add for CR00594200 start
            case GN_UA_DIALOG: {
                String uaString = "";
                Date date = new Date(Build.TIME);
                String strTime = new SimpleDateFormat("MM.dd.yyyy").format(date);
                String extModel = SystemProperties.get("ro.gn.extmodel", "Phone");
                date = new Date(Build.TIME);
                strTime = new SimpleDateFormat("MM.dd.yyyy").format(date);
                uaString = Build.BRAND + "-" + Build.MODEL + "/" + extModel
                        + " Linux/3.0.13 Android/"
                        + Build.VERSION.RELEASE + " Release/" + strTime
                        + " Browser/AppleWebKit534.30 Profile/MIDP-2.0 Configuration/CLDC-1.1"
                        + " Mobile Safari/534.30" + " Android " + Build.VERSION.RELEASE + ";";
                dialog = new AuroraAlertDialog.Builder(context).setTitle(R.string.user_agent)
                        .setMessage(uaString).setPositiveButton(
                                context.getResources().getString(R.string.close),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        dialog = null;
                                    }
                                }
                        ).create();
                dialog.show();
                break;
            }
            // GIONEE licheng May 8, 2012 add for CR00594200 end

            // GIONEE licheng May 9, 2012 add for CR00594199 start
            case GN_UINICOM_VERSION_NUM_DIALOG: {
                String relString = SystemProperties.get("ro.gn.op_special_vn");
                dialog = new AuroraAlertDialog.Builder(context).setTitle(R.string.unicom_version)
                        .setMessage(relString).setPositiveButton(
                                context.getResources().getString(R.string.close),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        dialog = null;
                                    }
                                }
                        ).create();
                dialog.show();
                break;

            }
            // GIONEE licheng May 9, 2012 add for CR00594199 end
            
            //Gionee qiuxd 20120612 add for CR00623811 start
            case GN_GTL_TYPE_DIALOG :{
                String gtlType = null;
                String lcdType = null, GType = null, TPType = null, cameraType = null, LPType = null;
                boolean isFileExists = false;
                File gtlFilePath = null;
                String mFileName = "/sys/devices/platform/gn_device_check/name";
                String mFileNameOriginal = "/sys/devices/platform/leds-mt65xx/leds/lcd-backlight/lcd_name";
                FileInputStream fileInputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader br = null;
                gtlFilePath = new File(mFileName);
                isFileExists = gtlFilePath.exists();
                try {
                    try {
                        if (isFileExists) {
                            fileInputStream = new FileInputStream(gtlFilePath);
                            inputStreamReader = new InputStreamReader(fileInputStream);
                            br = new BufferedReader(inputStreamReader);
                            String data = null;
                            while ((data = br.readLine()) != null) {
                                // lcdType = data;
                                String[] str = data.split(":");
                                if (data.contains("LCD")) {
                                    lcdType = str[str.length - 1];
                                } else if (data.contains("G-sensor")) {
                                    GType = str[str.length - 1];
                                } else if (data.contains("TP")) {
                                    TPType = str[str.length - 1];
                                } else if (data.contains("Camera")) {
                                	cameraType = str[str.length - 1];
                                } else if (data.contains("L/P-sensor")) {
                                    LPType = str[str.length - 1];
                                }
                            }
                        } else {
                            gtlFilePath = new File(mFileNameOriginal);
                            if(gtlFilePath.exists()){
                                fileInputStream = new FileInputStream(gtlFilePath);
                                inputStreamReader = new InputStreamReader(fileInputStream);
                                br = new BufferedReader(inputStreamReader);
                                String data = null;
                                while((data = br.readLine())!=null)
                                {
                                    lcdType = data;
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (br != null) {
                            br.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                if (lcdType == null || lcdType.equals("")) {
                    lcdType = context.getResources().getString(R.string.isnull);
                }

                if (GType == null || GType.equals("")) {
                    GType = context.getResources().getString(R.string.isnull);
                }

                if (TPType == null || TPType.equals("")) {
                    TPType = context.getResources().getString(R.string.isnull);
                }
                
                String gnType = context.getResources().getString(R.string.gn_type) + ": ";
                String typeTittle = "";
                if(isFileExists){
                    gtlType = "TP" + gnType + TPType + "\n" + "LCD" + gnType + lcdType + "\n"
                    + "G-Sensor" + gnType + GType;
                    
                    if (!TextUtils.isEmpty(LPType)) {
                    	gtlType += ("\nL/PSensor" + gnType + LPType); 
                    }
                    if (!TextUtils.isEmpty(cameraType)) {
                    	gtlType += ("\nCamera:" + cameraType); 
                    }
                    typeTittle = context.getResources().getString(R.string.gn_gtl_type);
                }else{
                    gtlType = lcdType;
                    typeTittle = context.getResources().getString(R.string.gn_lcd_type);
                }
                
                // gionee xuhz 20121217 add for CR00747376 start
                String audioVerNo = SystemProperties.get("persist.gn.audio.param.verno");
                if (!TextUtils.isEmpty(audioVerNo)) {
                    gtlType = gtlType + "\n\n" + "AUDIO Version Number:";
                    gtlType = gtlType + "\n" + audioVerNo;
                }
                // gionee xuhz 20121217 add for CR00747376 end
                
                dialog = new AuroraAlertDialog.Builder(context)
                .setTitle(typeTittle)
                .setMessage(gtlType)
                .setPositiveButton(context.getResources().getString(R.string.close),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog = null;
                            }
                        }).create();
                
                dialog.show();
                
                break;
            }
            //Gionee qiuxd 20120612 add for CR00623811 end
            // Gionee lihuafang 2012-06-14 add for CR00608224 begin

            // GIONEE licheng Jul 10, 2012 modify for CR00642420 start
            case GN_UA_ALL_DIALOG: {
                String extModel = SystemProperties.get("ro.gn.extmodel", "Phone");
                String brand = SystemProperties.get("ro.product.brand", "GiONEE");
                String model = SystemProperties.get("ro.product.model", "Phone");
                
                String uaString = "Mozilla/5.0 (Linux; U; Android " + Build.VERSION.RELEASE + "; zh-cn;"
                        + brand + "-" + model + "/" + extModel 
                        +" Build/IMM76D) AppleWebKit534.30(KHTML,like Gecko)Version/4.0 Mobile Safari/534.30";

                dialog = new AuroraAlertDialog.Builder(context)
                        .setTitle(R.string.user_agent)
                        .setMessage(uaString)
                        .setPositiveButton(context.getResources().getString(R.string.close),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        dialog = null;
                                    }
                                }).create();
                dialog.show();
                break;
            }
            // GIONEE licheng Jul 10, 2012 modify for CR00642420 end

            // Gionee lihuafang 2012-06-14 add for CR00608224 end

            //GIONEE huyuke 20120824 add for CR00680819 begin
            case GN_GET_DEVICES_STATUS_DIALOG: {
                showXMiscStatus(context);
                break;
            }
            //GIONEE huyuke 20120824 add for CR00680819 end
            
        case GN_QC_TP_LCD_DIALOG: {
            String lcdType = null;
            String tpType = null;
            String lcdFileName = "/sys/devices/gn_lcdc_type/gn_lcd_info";
            String tpFileName = "/sys/devices/gn_tp_type/gn_tp_info";
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader br = null;
            
            try {
                try {
                    File lcdFilePath = new File(lcdFileName);
                    if (lcdFilePath.exists()) {
                        fileInputStream = new FileInputStream(lcdFilePath);
                        inputStreamReader = new InputStreamReader(
                                fileInputStream);
                        br = new BufferedReader(inputStreamReader);
                        String data = null;
                        while ((data = br.readLine()) != null) {
                            lcdType = data;
                        }
                    }
                    
                    File tpFilePath = new File(tpFileName);
                    if (tpFilePath.exists()) {
                        fileInputStream = new FileInputStream(tpFilePath);
                        inputStreamReader = new InputStreamReader(
                                fileInputStream);
                        br = new BufferedReader(inputStreamReader);
                        String data = null;
                        while ((data = br.readLine()) != null) {
                            tpType = data;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (lcdType == null || lcdType.equals("")) {
                lcdType = context.getResources().getString(R.string.isnull);
            }
            if (tpType == null || tpType.equals("")) {
                tpType = context.getResources().getString(R.string.isnull);
            }
            
            dialog = new AuroraAlertDialog.Builder(context)
                    .setTitle(R.string.gn_gtl_type)
                    .setMessage(context.getResources().getString(R.string.gn_lcd_type) + ":" + lcdType + "\n" + "TP" +
                            context.getResources().getString(R.string.gn_type) + ":" + tpType)
                    .setPositiveButton(
                            context.getResources().getString(R.string.close),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                    dialog = null;
                                }
                            }).create();
            dialog.show();
            break;
        }
        }

    }
    //GIONEE zjy 20120321 add for CR00555135 search version   end

    //GIONEE huyuke 20120824 add for CR00680819 begin
    private static void showXMiscStatus(Context context) {
        String toDisplay = null;
        String xMiscStatus = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;

        try {
            try {
                File xMiscFile = new File("/sys/devices/virtual/misc/x_misc/x_misc/status");
                if (xMiscFile.exists()) {
                    fileInputStream = new FileInputStream(xMiscFile);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        xMiscStatus = data;
                    }
                } else {
                    toDisplay = context.getResources().getString(R.string.gn_x_misc_status_normal);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
               }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (toDisplay == null) {
            if (xMiscStatus == null) {
                toDisplay = context.getResources().getString(R.string.gn_x_misc_status_unkown);
            } else if (xMiscStatus.equals("0")){
                toDisplay = context.getResources().getString(R.string.gn_x_misc_status_normal);
            } else if (xMiscStatus.equals("1")){
                toDisplay = context.getResources().getString(R.string.gn_x_misc_status_locked);
            } else {
                toDisplay = context.getResources().getString(R.string.gn_x_misc_status_unkown);
            }
        }
        
        toDisplay = context.getResources().getString(R.string.gn_x_misc_status_normal);

        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
                .setTitle(R.string.gn_device_status_dlg_title)
                .setMessage(toDisplay)
                .setPositiveButton(
                        context.getResources().getString(R.string.close),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                dialog.cancel();
                                dialog = null;
                            }
            }
            ).create();
            dialog.show();
    }
    //GIONEE huyuke 20120824 add for CR00680819 end

	private static final String S4_MMI_TEST_ENTRY = "*#0*#";
    static boolean handleS4Mmi(Context context, String input) {
		if (input.equals(S4_MMI_TEST_ENTRY)) {
			Intent i = new Intent("android.intent.action.MAIN");
//			i.setClassName("com.sec.android.app.hwmoduletest", "com.sec.android.app.hwmoduletest.HwModuleTest");
			i.setClassName("com.sec.android.app.parser", "com.sec.android.app.parser.SecretCodeIME");
			context.startActivity(i);
			
			return true;
		}	
		return false;
	}
    
	private static void handleIndiaSAR(Context context) {

		String sarValue = null;
		String prop = SystemProperties.get("ro.gn.extmodel");
		if (prop.equals("P2"))
			sarValue = "1.365 W/kg @ 1g (Head)\n0.731 W/kg @ 1g (Body)";
		else if (prop.equals("P3"))
			sarValue = "0.679 W/kg @ 1g (Head)\n0.667 W/kg @ 1g (Body)";
		else if (prop.equals("E6"))
			sarValue = "0.619 W/kg @ 1g (Head)\n0.834 W/kg @ 1g (Body)";
		else if (prop.equals("E7"))
			sarValue = "1.43 W/kg @ 1g (Head)\n0.889 W/kg @ 1g (Body)";
		else if (prop.equals("E7mini"))
			sarValue = "0.612 W/kg @ 1g (Head)\n0.524 W/kg @ 1g (Body)";
		else if (prop.equals("S5.5"))
			sarValue = "0.38 W/kg @ 1g (Head)\n0.326 W/kg @ 1g (Body)";
		else if (prop.equals("V5"))
			sarValue = "0.45 W/kg @ 1g (Head)\n0.572 W/kg @ 1g (Body)";
		else if (prop.equals("G5"))
			sarValue = "1.100 W/kg @ 1g (Head)\n1.040 W/kg @ 1g (Body)";
		else if (prop.equals("V4S"))
			sarValue = "0.458 W/kg @ 1g (Head)\n0.740 W/kg @ 1g (Body)";
		else
			sarValue = "0.825 W/kg @ 1g (Head)\n0.696 W/kg @ 1g (Body)";

		AuroraAlertDialog alert = new AuroraAlertDialog.Builder(context)
				.setTitle("SAR Information").setMessage(sarValue)
				.setPositiveButton(android.R.string.ok, null)
				.setCancelable(false).show();
	}
}
