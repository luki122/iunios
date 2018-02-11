package com.aurora.mms.util;
// Aurora xuyong 2014-07-02 added for reject feature start
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
// Aurora xuyong 2014-10-23 added for privacy feature start
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// Aurora xuyong 2014-10-23 added for privacy feature end
// Aurora xuyong 2014-07-02 added for reject feature end
import android.app.Activity;
// Aurora xuyong 2015-09-08 added for bug #15971 start
import android.content.ContentUris;
// Aurora xuyong 2015-09-08 added for bug #15971 end
// Aurora xuyong 2014-07-02 added for reject feature start
import android.content.ContentValues;
// Aurora xuyong 2014-09-01 added for bug #7751 start
import android.content.Intent;
// Aurora xuyong 2014-09-01 added for bug #7751 end
// Aurora xuyong 2014-07-02 added for reject feature end
import android.content.Context;
// Aurora xuyong 2014-07-02 added for reject feature start
import android.content.SharedPreferences;
// Aurora xuyong 2015-09-08 added for bug #15971 start
import android.content.pm.ResolveInfo;
import android.database.Cursor;
// Aurora xuyong 2015-09-08 added for bug #15971 end
import android.database.sqlite.SQLiteException;
// Aurora xuyong 2014-10-23 added for privacy feature start
import android.database.sqlite.SqliteWrapper;
// Aurora xuyong 2014-10-23 added for privacy feature end
// Aurora xuyong 2014-07-02 added for reject feature end
import android.graphics.Rect;
// Aurora xuyong 2014-04-29 added for aurora's new feature start
// Aurora xuyong 2014-07-02 added for reject feature start
// Aurora xuyong 2014-07-10 added for reject feature start
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
// Aurora xuyong 2014-07-10 added for reject feature end
import android.net.Uri;
// Aurora xuyong 2014-07-02 added for reject feature end
import android.os.Build;
// Aurora xuyong 2015-09-08 added for bug #15971 start
import android.os.Environment;
// Aurora xuyong 2015-09-08 added for bug #15971 end
// Aurora xuyong 2015-03-30 added for yulore feature start
import android.os.RemoteException;
// Aurora xuyong 2015-03-30 added for yulore feature end
// Aurora xuyong 2014-09-01 added for bug #7751 start
// Aurora xuyong 2014-10-23 added for privacy feature start
import android.provider.ContactsContract.Data;
// Aurora xuyong 2015-09-08 added for bug #15971 start
import android.provider.MediaStore;
// Aurora xuyong 2015-09-08 added for bug #15971 end
// Aurora xuyong 2014-10-23 added for privacy feature end
import android.provider.Settings;
// Aurora xuyong 2014-09-01 added for bug #7751 end
// Aurora xuyong 2014-07-02 added for reject feature start
import android.util.Log;
// Aurora xuyong 2014-07-02 added for reject feature end
// Aurora xuyong 2014-04-29 added for aurora's new feature end
import android.view.inputmethod.InputMethodManager;
// Aurora xuyong 2014-07-02 added for reject feature start
// Aurora xuyong 2014-07-07 added for reject feature start
// Aurora xuyong 2014-07-07 added for reject feature start
// Aurora xuyong 2014-09-03 added for whitelist feature start
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
// Aurora xuyong 2014-09-03 added for whitelist feature end
import com.android.mms.util.PhoneNumberUtils;
// Aurora xuyong 2014-07-07 added for reject feature end
// Aurora xuyong 2014-10-23 added for privacy feature start
import gionee.provider.GnTelephony.Mms;
// Aurora xuyong 2014-10-23 added for privacy feature end
import com.aurora.android.mms.pdu.GenericPdu;
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduPart;
import com.aurora.android.mms.pdu.EncodedStringValue;
// Aurora xuyong 2014-07-07 added for reject feature end
import com.aurora.mms.transaction.AuroraRejectRubbishOrBlackMsgReceiver;
// Aurora xuyong 2015-03-30 deleted for yulore feature start
//import com.sogou.hmt.sdk.manager.HMTNumber;
//import com.sogou.hmt.sdk.manager.HmtSdkManager;
// Aurora xuyong 2015-03-30 deleted for yulore feature end
// Aurora xuyong 2014-07-02 added for reject feature end
// Aurora xuyong 2015-03-30 added for yulore feature start
import com.yulore.framework.YuloreHelper;
import com.yulore.sdk.smartsms.bean.SmsCheckResult;
import com.yulore.superyellowpage.modelbean.RecognitionTelephone;
import com.yulore.superyellowpage.modelbean.TelephoneFlag;
// Aurora xuyong 2015-03-30 added for yulore feature end
public class Utils {
    // Aurora xuyong 2014-07-02 added for reject feature start
    public final static Uri BLACK_URI = Uri.parse("content://com.android.contacts/black");
   // Aurora xuyong 2014-07-15 added for reject feature start
    private static final Uri MARK_URI = Uri.parse("content://com.android.contacts/mark");
   // Aurora xuyong 2014-07-15 added for reject feature end
   // Aurora xuyong 2014-09-02 added for whitelist feature start
    public final static Uri WHITE_URI = Uri.parse("content://white-list/recipient");
   // Aurora xuyong 2014-09-02 added for whitelist feature end
    private final static String[] BLACK_PROJECTION = new String[]{
        "number"
    };
   // Aurora xuyong 2014-09-02 added for whitelist feature start
    private final static String[] WHITE_PROJECTION = new String[]{
        "number"
    };
   // Aurora xuyong 2014-09-02 added for whitelist feature end
    private static final int NUMBER_COLUMN_INDEX = 0;
    // Aurora xuyong 2014-07-02 added for reject feature end
    // Aurora xuyong 2014-10-24 added for privacy feature start
    private static ArrayList<Activity> mNeedRestList = new ArrayList<Activity>();
    // Aurora xuyong 2015-03-30 added for yulore feature start
    private final static String YULORE_APIKEY = "jfvLhfephClV9jkRuscG9nmpDL9SiOc7";
    private final static String YULORE_SECRET = "0hOdge6xrsi9fcpXRKplnmpub3xXpDdzaeML6HjUCMuf" +
                          "LDiwyEtRJuLocasVkoiJ5thxzwDdvan7lsi1zajntau9i5rpoVlrg5jvVC1aksvi4TlL" +
                          "fWyxdlrQB7pnMcNzvD06GHCrrMrju0hzqvYlrvzoFDBh3SoxGjKcd";
    // Aurora xuyong 2015-03-30 added for yulore feature end
    public static void clearAllInstance() {
        if (mNeedRestList != null) {
            for (Activity instance : mNeedRestList) {
                if (instance != null && !instance.isFinishing()) {
                    instance.finish();
                }
            }
             mNeedRestList.clear();
        }
    }
    
    public static void addInstance(Activity instance) {
        if (mNeedRestList != null) {
            mNeedRestList.add(instance);
        }
    }
    
    public static void removeInstance(Activity instance) {
        if (mNeedRestList != null) {
            mNeedRestList.remove(instance);
        }
    }
    // Aurora xuyong 2014-10-24 added for privacy feature end
    public static int getStatusHeight(Activity activity){
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = activity.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }
    
    // Aurora liugj 2013-10-19 added for aurora's new feature start
    public static void hideInputMethod(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getWindow() != null
                && activity.getWindow().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getWindow()
                    .getCurrentFocus().getWindowToken(), 0);
        }
    }
    // Aurora liugj 2013-10-19 added for aurora's new feature end
    // Aurora xuyong 2014-04-29 added for aurora's new feature start
    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= 0x8; //Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= 0x9; //Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= 0xb; //Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= 0xc; //Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= 0x10; //Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= 0x13; //Build.VERSION_CODES.KITKAT;
    }
    // Aurora xuyong 2014-04-29 added for aurora's new feature end
    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= 0x15; //Build.VERSION_CODES.LOLLIPOP;
    }
    // Aurora xuyong 2014-07-02 added for reject feature start
    public static boolean isInit(){
        if (!MmsApp.sNotCNFeature) {
            // Aurora xuyong 2015-03-30 modified for yulore feature start
            return sHasBinded;
            // Aurora xuyong 2015-03-30 modified for yulore feature end
        } else {
            return true;
        }
    }
    // Aurora xuyong 2015-03-30 added for yulore feature start
    private static YuloreHelper sYuloreHelperInstance;
    private static boolean sHasBinded = false;
    // Aurora xuyong 2015-03-30 added for yulore feature end
    public static void init(Context context){
        if (!MmsApp.sNotCNFeature) {
            // Aurora xuyong 2015-03-30 modified for yulore feature start
            sYuloreHelperInstance = new YuloreHelper(context);
            sHasBinded = sYuloreHelperInstance.bindService(YULORE_APIKEY, YULORE_SECRET);
            // Aurora xuyong 2015-03-30 modified for yulore feature end
           // Aurora xuyong 2014-07-08 added for reject feature start
            try {
                Thread.currentThread().sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
           // Aurora xuyong 2014-07-08 added for reject feature end
        }
       // Aurora xuyong 2014-07-08 added for reject feature end
    }
    
    // Aurora xuyong 2014-07-19 added for sougou start
    public static final int RIGION_NAME_INDEX   = 1;
    public static final int CITY_NAME_INDEX     = 2;
    public static final int DISTRICT_NAME_INDEX = 3;
    public static String getAreaName(Context context, String number, int index) {
        if (!MmsApp.sNotCNFeature) {
            if (!isInit()) {
                init(context);
            }
            // Aurora xuyong 2015-03-30 modified for yulore feature start
            RecognitionTelephone rt = null;
            try {
                // Aurora xuyong 2015-04-16 modified for yulore feature start
                rt = sYuloreHelperInstance.queryNumberInfo(YULORE_APIKEY, YULORE_SECRET, number, 1, true, false, 1);
                Log.e("Mms/Weather", rt.toString());
                // Aurora xuyong 2015-04-16 modified for yulore feature end
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (rt != null) {
                    String result = "";
                    switch (index) {
                        case RIGION_NAME_INDEX:
                            result = rt.getLocation();
                            break;
                        case CITY_NAME_INDEX:
                            result = rt.getCityName();
                            break;
                        case DISTRICT_NAME_INDEX:
                            result = rt.getDistrictName();
                            break;
                    }
                    return result;
                } else {
                    return null;
                }
            }
            // Aurora xuyong 2015-03-30 modified for yulore feature end
        } else {
            return null;
        }
    }

    public static boolean isNotificationMsg(Context context, String address,String smsBody) {
        if (!MmsApp.sNotCNFeature) {
                 if (!isInit()) {
                     init(context);
                 }
                 int result = 2;
                 try {
                     result = sYuloreHelperInstance.querySmsType(YULORE_APIKEY, YULORE_SECRET, address, smsBody);
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 } finally {
                     if (result == 1) {
                         return true;
                     } else {
                         return false;
                     }
                 }
                 // Aurora xuyong 2015-03-30 modified for yulore feature end
                 //return HmtSdkManager.getInstance().isSpam(address, smsBody);
                 // Aurora xuyong 2014-09-15 modified for sogou jar replace end
             } else {
                 return false;
             }
    }
    
    // Aurora xuyong 2014-07-19 added for sougou end
    // Aurora xuyong 2014-07-07 modified for reject feature start
   // Aurora xuyong 2014-09-15 modified for sogou jar replace start
    public static boolean isSpam(Context context, String address, String smsBody){
        if (!MmsApp.sNotCNFeature) {
       // Aurora xuyong 2014-09-15 modified for sogou jar replace end
            if (!isInit()) {
                init(context);
            }
       // Aurora xuyong 2014-07-07 modified for reject feature end
            // Aurora xuyong 2014-09-15 modified for sogou jar replace start
            // Aurora xuyong 2015-03-30 modified for yulore feature start
            SmsCheckResult result = null;
            try {
                result = sYuloreHelperInstance.check(YULORE_APIKEY, YULORE_SECRET, address, smsBody);
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                if (result != null && result.getSuggestion() != 2) {
                    return false;
                } else {
                    return true;
                }
            }
            // Aurora xuyong 2015-03-30 modified for yulore feature end
            //return HmtSdkManager.getInstance().isSpam(address, smsBody);
            // Aurora xuyong 2014-09-15 modified for sogou jar replace end
        } else {
            return false;
        }
    }
    
    public static boolean insertUserMark(Context context, String number,String lable) {
        if (number == null) {
            return false;
        }
        
        ContentValues cv = new ContentValues();
        cv.put("lable", lable);
        cv.put("number", number);
        // Aurora xuyong 2014-07-19 modified for +86 start
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MARK_URI, null, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
           // Aurora xuyong 2014-07-19 modified for +86 end
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    if (lable == null) {
                    // Aurora xuyong 2014-07-19 modified for +86 start
                        int count = 0;
                        try {
                            count = context.getContentResolver().delete(MARK_URI, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
                        } catch (SQLiteException e) {
                            e.printStackTrace();
                        } finally {
                        // Aurora xuyong 2014-07-19 modified for +86 end
                            if (count > 0) {
                                cursor.close();
                                  return true;
                              }
                        }
                    } else {
                    // Aurora xuyong 2014-07-19 modified for +86 start
                        int count = 0;
                        try {
                            count = context.getContentResolver().update(MARK_URI, cv, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
                        } catch (SQLiteException e) {
                            e.printStackTrace();
                        } finally {
                        // Aurora xuyong 2014-07-19 modified for +86 end
                              if (count > 0) {
                                  cursor.close();
                                  return true;
                              }
                        }
                    }
                }
            }
            
            Uri uri = context.getContentResolver().insert(MARK_URI, cv);
            if (uri != null) {
                return true;
            }
            
            return false;
        }
    }

    
    public static boolean deleteUserMark(Context context, String number) {
       // Aurora xuyong 2014-07-19 modified for +86 start
       // Aurora xuyong 2014-08-01 added for aurora's new feature start
        int count = 0;
        try {
            count = context.getContentResolver().delete(MARK_URI, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
           // Aurora xuyong 2014-08-01 added for aurora's new feature start
           // Aurora xuyong 2014-07-19 modified for +86 end
            if (count > 0) {
                return true;
            }
            return false;
        }
    }
    
    private static String getUserMark(Context context, String number){
        String result = null;
       // Aurora xuyong 2014-07-15 modified for reject feature start
       // Aurora xuyong 2014-07-19 modified for +86 start
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MARK_URI, null, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
           // Aurora xuyong 2014-07-19 modified for +86 end
           // Aurora xuyong 2014-07-15 modified for reject feature end
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        result = cursor.getString(1);
                    }
                } finally {
                    cursor.close();
                }
            }
            
            return result;
        }

    }
    // Aurora xuyong 2014-07-15 added for reject feature start
    public static int getMarkNumber(Context context, String number) {
        if (!MmsApp.sNotCNFeature) {
            // Aurora xuyong 2015-03-30 modified for yulore feature start
            int num = 0;
            RecognitionTelephone rt = null;
            try {
                rt = sYuloreHelperInstance.queryNumberInfo(YULORE_APIKEY, YULORE_SECRET, number, 1, true, true, 1);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            TelephoneFlag flag = rt.getFlag();
            if (flag != null) {
                num = flag.getNum();
            }
            return num;
            // Aurora xuyong 2015-03-30 modified for yulore feature end
        } else {
            return -1;
        }
    }
   // Aurora xuyong 2014-07-15 added for reject feature end
   // Aurora xuyong 2014-07-10 modified for reject feature start
    private static String getMarkContent(Context context, String number){
        if (!MmsApp.sNotCNFeature) {
           // Aurora xuyong 2014-07-16 added for reject feature start
            if (!isInit()) {
                init(context);
            }
           // Aurora xuyong 2014-07-16 added for reject feature end
            // Aurora xuyong 2015-03-30 modified for yulore feature start
            String mark = null;
            RecognitionTelephone rt = null;
            try {
                rt= sYuloreHelperInstance.queryNumberInfo(YULORE_APIKEY, YULORE_SECRET, number, 1, true, true, 1);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mark = rt.getName();
            if(mark != null){
                return mark;
            }else{
                TelephoneFlag flag= rt.getFlag();
                if(flag != null){
                    mark = flag.getType();
                }
            }
            return mark;
            // Aurora xuyong 2015-03-30 modified for yulore feature end
        } else {
            return null;
        }
    }
    // Aurora xuyong 2014-07-10 modified for reject feature end
   // Aurora xuyong 2014-07-15 modified for reject feature start
    public static String getLable(Context context, String number, ContentValues values) {
   // Aurora xuyong 2014-07-15 modified for reject feature end
        String s = getUserMark(context, number);
        if (s != null) {
          // Aurora xuyong 2014-07-15 modified for reject feature start
            values.put("user_mark", -1);
          // Aurora xuyong 2014-07-15 modified for reject feature end
            return s;
        } else {
          // Aurora xuyong 2014-07-10 modified for reject feature start
            s = getMarkContent(context, number);
          // Aurora xuyong 2014-07-15 added for reject feature start
            int markCount = getMarkNumber(context, number);
            if (s != null && markCount >= 0) {
                values.put("user_mark", markCount);
            }
          // Aurora xuyong 2014-07-15 added for reject feature end
          // Aurora xuyong 2014-07-10 modified for reject feature end
        }
        return s;
        
    }
     // Aurora xuyong 2014-08-08 modified for bug #7389 start
    public static boolean needRejectBlackMsg(Context context) {
    // Aurora xuyong 2014-08-08 modified for bug #7389 end
      // Aurora xuyong 2014-08-14 modified for bug #7665 start
        //SharedPreferences sf = context.getSharedPreferences(
        //        AuroraRejectRubbishOrBlackMsgReceiver.SHARED_TAG, Context.MODE_PRIVATE);
        // we should always reject the message which was sent from a recipient who has been in the black list
        return true;//sf.getBoolean(AuroraRejectRubbishOrBlackMsgReceiver.BLACK_REJECT_KEY, true);
      // Aurora xuyong 2014-08-14 modified for bug #7665 end
    }
    // Aurora xuyong 2014-08-08 modified for bug #7389 start
    public static boolean needRejectRubbishMsg(Context context) {
    // Aurora xuyong 2014-08-08 modified for bug #7389 end
        SharedPreferences sf = context.getSharedPreferences(
                AuroraRejectRubbishOrBlackMsgReceiver.SHARED_TAG, Context.MODE_PRIVATE);
        return sf.getBoolean(AuroraRejectRubbishOrBlackMsgReceiver.RUBBISH_REJECT_KEY, true);
    }
    
    public static boolean isInBlackList(Context context, String number) {
        // Aurora xuyong 2014-11-21 added for bug #9950 start
        if (getFristPrivacyId(context, number) > 0) {
            // As privacy has a higher priority, so if a number has been checked 
            // to be a privacy recipient, we needn't reject it any more, so we return 
            // true here.
            return false;
        }
        // Aurora xuyong 2014-11-21 added for bug #9950 end
       // Aurora xuyong 2014-07-19 modified for +86 start
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(BLACK_URI, BLACK_PROJECTION, "isblack = ? AND (reject = ? OR reject = ?) AND PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", new String[]{"1", "2", "3"}, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                return true;
           // Aurora xuyong 2014-07-19 modified for +86 end
            }
            return false;
        }
    }

    // Aurora yudingmin 2014-10-20 added for whitelist feature start
    private static final String[] prefixWhiteList = new String[]{
        "10086", "10000", "10010"
    };
    
    private static boolean isInPrefixWhiteList(String number){
       for(int i = 0; i < prefixWhiteList.length; i++){
           if(number.startsWith(prefixWhiteList[i])){
               return true;
           }
       }
       return false;
    }
    // Aurora yudingmin 2014-10-20 added for whitelist feature end
    
    // Aurora xuyong 2014-09-02 added for whitelist feature start
    public static boolean isInWhiteList(Context context, String number) {
        // Aurora xuyong 2014-07-19 modified for +86 start
      // Aurora xuyong 2014-09-03 added for whitelist feature start
       // Aurora xuyong 2014-10-23 added for privacy feature start
       if (getFristPrivacyId(context, number) > 0) {
           // As privacy has a higher priority, so if a number has been checked 
           // to be a privacy recipient, we needn't reject it any more, so we return 
           // true here.
           return true;
       }
       // Aurora xuyong 2014-10-23 added for privacy feature end
       Contact contact = Contact.get(number, true);
       if (contact.existsInDatabase()) {
           return true;
       }
       // Aurora yudingmin 2014-10-20 added for whitelist feature start
       if (isInPrefixWhiteList(number)) {
           return true;
       }
       // Aurora yudingmin 2014-10-20 added for whitelist feature end
      // Aurora xuyong 2014-09-03 added for whitelist feature end
         Cursor cursor = null;
         try {
             cursor = context.getContentResolver().query(WHITE_URI, WHITE_PROJECTION, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
         } catch (SQLiteException e) {
             e.printStackTrace();
         } finally {
             if (cursor != null && cursor.getCount() > 0) {
                 cursor.close();
                 return true;
            // Aurora xuyong 2014-07-19 modified for +86 end
             }
             return false;
         }
     }
    // Aurora xuyong 2014-09-02 added for whitelist feature end
    public static boolean showRemoveBlack(Context context, String number) {
      // Aurora xuyong 2014-07-19 modified for +86 start
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(BLACK_URI, BLACK_PROJECTION, "(isblack = 1 AND reject > 0) AND PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
        } catch (SQLiteException e) { 
            e.printStackTrace();
        } finally {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                return true;
          // Aurora xuyong 2014-07-19 modified for +86 end
            }
            return false;
        }
    }
    
    public static boolean showAddBlack(Context context, String number) {
       // Aurora xuyong 2014-07-19 modified for +86 start
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(BLACK_URI, BLACK_PROJECTION, "(isblack = 1 AND reject > 0) AND PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                return false;
           // Aurora xuyong 2014-07-19 modified for +86 end
            }
            return true;
        }
    }
    
    public static void notRejectMore(Context context, String number) {
        ContentValues values = new ContentValues();
        values.put("isblack", 0);
       // Aurora xuyong 2014-07-19 modified for +86 start
        try {
            context.getContentResolver().update(BLACK_URI, values, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
       // Aurora xuyong 2014-07-19 modified for +86 end
    }
  // Aurora xuyong 2014-07-02 added for reject feature end
    // Aurora xuyong 2014-07-07 added for reject feature start
     // Aurora xuyong 2014-11-08 modified for reject new feature start
     public static boolean needRejectBlackMms(GenericPdu pdu, Context context) {
     // Aurora xuyong 2014-11-08 modified for reject new feature end
         if (!Utils.isInit()) {
             Utils.init(context);
         }
         if (needRejectBlackMsg(context)) {
             return Utils.isInBlackList(context, pdu.getFrom().getString());
         }
         return false;
     }
     // Aurora xuyong 2014-09-02 added for whitelist feature start
     private static boolean checkWhiteMsg(GenericPdu pdu, Context context) {
         if (!Utils.isInit()) {
             Utils.init(context);
         }
         if (Utils.needRejectRubbishMsg(context)) {
             return Utils.isInWhiteList(context, pdu.getFrom().getString());
         } else {
             return false;
         }
     }
     // Aurora xuyong 2014-09-02 added for whitelist feature end
     // Aurora xuyong 2014-11-08 modified for reject new feature start
     public static boolean needRejectRubMms(GenericPdu pdu, Context context) {
     // Aurora xuyong 2014-11-08 modified for reject new feature end
       // Aurora xuyong 2014-09-02 added for whitelist feature start
         if (checkWhiteMsg(pdu, context)) {
             return false;
         }
       // Aurora xuyong 2014-09-02 added for whitelist feature end
         try {
             if (!Utils.isInit()) {
                 Utils.init(context);    
             }
             boolean result = false;
          // Aurora xuyong 2014-09-15 added for sogou jar replace start
             String address = pdu.getFrom().getString();
          // Aurora xuyong 2014-09-15 added for sogou jar replace end
             if (Utils.needRejectRubbishMsg(context)) {
                 PduBody body = ((MultimediaMessagePdu)pdu).getBody();
                 int partsNum = body.getPartsNum();
                 for (int i = 0; i < partsNum; i++) {
                     PduPart part = body.getPart(i); 
                     String contentType = new String(part.getContentType(),"gb2312"); 
                     if (contentType.contains("text")) {
                         String content= new EncodedStringValue(part.getData()).getString();
                    // Aurora xuyong 2014-07-07 modified for reject feature start
                   // Aurora xuyong 2014-09-15 modified for sogou jar replace start
                         result |= Utils.isSpam(context, address, content);
                   // Aurora xuyong 2014-09-15 modified for sogou jar replace end
                    // Aurora xuyong 2014-07-07 modified for reject feature end
                     }
                 }
                 return result;
             }
         } catch (Exception e) {
             return false;
         }
         return false;
     }
   // Aurora xuyong 2014-07-07 added for reject feature end
   // Aurora xuyong 2014-07-19 added for bug #6646 start
   public static final String[] NAME_NUMBER_PRO = {"black_name", "number"};
   public static String getRejectNameByNumber(Context context, String number) {
      // Aurora xuyong 2014-07-19 modified for +86 start
       Cursor cursor = null;
       try {
           cursor = context.getContentResolver().query(BLACK_URI, NAME_NUMBER_PRO, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
       } catch (SQLiteException e) {
           e.printStackTrace();
       } finally {
          // Aurora xuyong 2014-07-19 modified for +86 end
           if (cursor != null && cursor.moveToFirst()) {
               String black_name = cursor.getString(0);
               cursor.close();
               if (black_name == null || black_name.isEmpty()) {
                   return number;
               } else {
                   return black_name;
               }
           }
           return null;
       }
   }
   // Aurora xuyong 2014-07-19 added for bug #6646 end
   // Aurora xuyong 2014-09-01 added for bug #7751 start
   public static void kitKatDefaultMsgCheck(Context context) {
       final String systemSmsName = context.getPackageName();
       if (!isDefaultSmsApk(context)) {
              Intent intent = new Intent("android.provider.Telephony.ACTION_CHANGE_DEFAULT");  
           intent.putExtra("package", systemSmsName);  
           context.startActivity(intent);
       }
   }
   
   public static boolean isDefaultSmsApk(Context context) {
       final String systemSmsName = context.getPackageName();
           String defaultApplication = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");
           return systemSmsName.equals(defaultApplication);
   }
   // Aurora xuyong 2014-09-01 added for bug #7751 end
   // Aurora xuyong 2014-10-23 added for privacy feature start
   // Aurora xuyong 2014-11-10 modified for bug #9720 start
   public static long getFristPrivacyId(Context context, String number) {
   // Aurora xuyong 2014-11-10 modified for bug #9720 end
       try {
           String selection = "mimetype_id = 5 and PHONE_NUMBERS_EQUAL(data1, " + number + ", 0)" + " and is_privacy > 0";
           Cursor c = context.getContentResolver().query(
               Data.CONTENT_URI, 
               new String[]{"is_privacy"}, 
               selection , 
               null, 
               "is_privacy desc");
           if (c != null && c.moveToFirst()) {
               // Aurora xuyong 2014-11-10 modified for bug #9720 start
               return c.getLong(0);
               // Aurora xuyong 2014-11-10 modified for bug #9720 end
           }
       } catch (SQLiteException e) {
           e.printStackTrace();
       }
       return 0;
   }
   
   public static long getOrCreateThreadId(
           Context context, Set<String> recipients, long privacy) {
       Uri.Builder uriBuilder = Uri.parse("content://mms-sms/threadID").buildUpon();

       for (String recipient : recipients) {
           if (Mms.isEmailAddress(recipient)) {
               recipient = android.provider.Telephony.Mms.extractAddrSpec(recipient);
           }

           uriBuilder.appendQueryParameter("recipient", recipient);
           uriBuilder.appendQueryParameter("is_privacy", "" + privacy);
       }

       Uri uri = uriBuilder.build();
       //if (DEBUG) Rlog.v(TAG, "getOrCreateThreadId uri: " + uri);

       Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
               uri, new String[] { "_id" }, null, null, null);
       if (cursor != null) {
           try {
               if (cursor.moveToFirst()) {
                   return cursor.getLong(0);
               } else {
                   Log.e("Telephony", "getOrCreateThreadId returned no rows!");
               }
           } finally {
               cursor.close();
           }
       }

       Log.e("Telephony", "getOrCreateThreadId failed with uri " + uri.toString());
       throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
   }
   
   private final Uri sPrivacyUri = Uri.parse("content://mms-sms/privacy-account");
   public void setCurrentThreadId(Context context, long currentThreadId) {
       ContentValues values = new ContentValues();
          // syn update the value of the column
          values.put("cur_opt_thread_id", currentThreadId);
          context.getContentResolver().update(sPrivacyUri, values, null, null);
   }
   // Aurora xuyong 2014-10-23 added for privacy feature end
   
   public static String getUsefulCode(Context context, String body) {
       if (body == null) {
           return null;
       }
       Pattern pattern = Pattern.compile("[0-9]{4,}");
       Matcher match = pattern.matcher(body);
       
       String keyWord = null;
       String[] keyWords = context.getResources().getStringArray(R.array.aurora_ipop_keys);
       for(String key : keyWords) {
           if(body.indexOf(key) > -1) {
               keyWord = key;
               break;
           }
       }
       if (keyWord == null) {
           return null;
       }
       List<Integer> keyList = new ArrayList<Integer>();
       // Aurora xuyong 2015-05-15 added for aurora's new feature start
       List<Integer> newkeyList = new ArrayList<Integer>();
       // Aurora xuyong 2015-05-15 added for aurora's new feature end
       int index = 0;
       int length = body.length() - 1;
       // Get all keywords in the message body.
       while (index >= 0 && index <= length) {
           index = body.indexOf(keyWord, index);
           if (index >= 0 && index <= length) {
               keyList.add(index);
               index++;
           }
       }
       if (keyList.size() <= 0) {
           return null;
       // Aurora xuyong 2015-05-15 added for aurora's new feature start
       } else {
    	   String[] noNeddKeyWords = context.getResources().getStringArray(R.array.aurora_ipop_noneed_keys);
    	   for (String item : noNeddKeyWords) {
    		   int itemIndex = body.indexOf(item);
    		   for (Integer value : keyList) {
    			   if (!(itemIndex < value && value < itemIndex + item.length())) {
    				   newkeyList.add(value);
    			   }
    		   }
    	   }
       // Aurora xuyong 2015-05-15 added for aurora's new feature end
       }
       Stack<String> matches = new Stack<String>();
       int offSet = 0;
       // Find the correct code by the closest distance.
       while (match.find()) {
           // Aurora xuyong 2015-05-15 modified for aurora's new feature start
           for (Integer indexItem : newkeyList) {
           // Aurora xuyong 2015-05-15 modified for aurora's new feature end
               int keyIndex = indexItem.intValue();
               int offsetHead = Math.abs(match.start() - keyIndex);
               int offsetNail = Math.abs(match.end() - keyIndex);
               int tag = 0;
               int ind = offsetHead <= offsetNail ? offsetHead : offsetNail;
               if (offSet > 0 && offSet < ind) {
                   continue;
               } else {
                   offSet = ind;
                   // Aurora xuyong 2015-02-02 modified for aurora's new feature start
                   String findResult = body.substring(match.start(), match.end());
                   // Find results' length filter
                   if (findResult != null && findResult.length() <= 8) {
                       matches.push(findResult);
                   }
                   // Aurora xuyong 2015-02-02 modified for aurora's new feature end
               }
           }
       }
       if (matches.size() > 0) {
           return matches.pop();
       } else {
           return null;
       }
   }
   // Aurora xuyong 2015-09-08 added for bug #15971 start
   public static String getImageAbsolutePath(Context context, Uri imageUri) {
	   if (hasKitKat() && isDocumentUri(context, imageUri)) {  
	        if (isExternalStorageDocument(imageUri)) {  
	            String docId = getDocumentId(imageUri);  
	            String[] split = docId.split(":");  
	            String type = split[0];  
	            if ("primary".equalsIgnoreCase(type)) {  
	                return Environment.getExternalStorageDirectory() + "/" + split[1];  
	            }  
	        } else if (isDownloadsDocument(imageUri)) {  
	            String id = getDocumentId(imageUri);  
	            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));  
	            return getDataColumn(context, contentUri, null, null);  
	        } else if (isMediaDocument(imageUri)) {  
	            String docId = getDocumentId(imageUri);  
	            String[] split = docId.split(":");  
	            String type = split[0];  
	            Uri contentUri = null;  
	            if ("image".equals(type)) {  
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("video".equals(type)) {  
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("audio".equals(type)) {  
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;  
	            }  
	            String selection = MediaStore.Images.Media._ID + "=?";  
	            String[] selectionArgs = new String[] { split[1] };  
	            return getDataColumn(context, contentUri, selection, selectionArgs);  
	        }  
	  } // MediaStore (and general)  
	    else if ("content".equalsIgnoreCase(imageUri.getScheme())) {  
	        // Return the remote address  
	        if (isGooglePhotosUri(imageUri))  
	            return imageUri.getLastPathSegment();  
	        return getDataColumn(context, imageUri, null, null);  
	    }  
	    // File  
	    else if ("file".equalsIgnoreCase(imageUri.getScheme())) {  
	        return imageUri.getPath();  
	    }  
	    return null;
	  
   }
   
   public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {  
	    Cursor cursor = null;  
	    String column = MediaStore.Images.Media.DATA;  
	    String[] projection = { column };  
	    try {  
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);  
	        if (cursor != null && cursor.moveToFirst()) {  
	            int index = cursor.getColumnIndexOrThrow(column);  
	            return cursor.getString(index);  
	        }  
	    } finally {  
	        if (cursor != null)  
	            cursor.close();  
	    }  
	    return null;  
	}
   
   public static boolean isExternalStorageDocument(Uri uri) {  
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());  
   } 
   
   public static boolean isDownloadsDocument(Uri uri) {  
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());  
   }
   
   public static boolean isGooglePhotosUri(Uri uri) {  
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());  
   } 
   
   public static boolean isMediaDocument(Uri uri) {  
	    return "com.android.providers.media.documents".equals(uri.getAuthority());  
   }
   
   public static boolean isDocumentUri(Context context, Uri uri) {
       final List<String> paths = uri.getPathSegments();
       if (paths.size() == 2 && "document".equals(paths.get(0))) {
           return isDocumentsProvider(context, uri.getAuthority());
       }
       if (paths.size() == 4 && "tree".equals(paths.get(0))
               && "document".equals(paths.get(2))) {
           return isDocumentsProvider(context, uri.getAuthority());
       }
       return false;
   }

   private static boolean isDocumentsProvider(Context context, String authority) {
       final Intent intent = new Intent("android.content.action.DOCUMENTS_PROVIDER");
       final List<ResolveInfo> infos = context.getPackageManager()
               .queryIntentContentProviders(intent, 0);
       /// M: PackageManager.queryIntentContentProviders() may return null,
       ///    So add this to avoid NPE
       if (null == infos) {
           return false;
       }
       for (ResolveInfo info : infos) {
           if (authority.equals(info.providerInfo.authority)) {
               return true;
           }
       }
       return false;
   }
   
   public static String getDocumentId(Uri documentUri) {
       final List<String> paths = documentUri.getPathSegments();
       if (paths.size() >= 2 && "document".equals(paths.get(0))) {
           return paths.get(1);
       }
       if (paths.size() >= 4 && "tree".equals(paths.get(0))
               && "document".equals(paths.get(2))) {
           return paths.get(3);
       }
       throw new IllegalArgumentException("Invalid URI: " + documentUri);
   }
   // Aurora xuyong 2015-09-08 added for bug #15971 end
}
