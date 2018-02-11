package com.android.phone;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.RemoteException;

import com.android.internal.telephony.IIccPhoneBook;

import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.IccProvider;

import gionee.telephony.AuroraTelephoneManager;

public class AuroraIccProvider extends IccProvider{
    private static final String TAG = "AuroraIccProvider";
    private static final boolean DBG = true;

    static final int EF_ADN = 0x6F3A;
    static final int EF_FDN = 0x6F3B;
    static final int EF_SDN = 0x6F49;
    
    private static final String[] ADDRESS_BOOK_COLUMN_NAMES = new String[] {
        "name",
        "number",
        "emails",
        "_id"
    };

    private static final int ADN = 1;
    private static final int FDN = 2;
    private static final int SDN = 3;
    
    private static final int ADN2 = 11;//aurora add zhouxiaobing 20140421 for dualsim
    private static final int FDN2 = 21;
    private static final int SDN2 = 31;
    private int slot_id;

    private static final String STR_TAG = "tag";
    private static final String STR_NUMBER = "number";
    private static final String STR_EMAILS = "emails";
    private static final String STR_PIN2 = "pin2";

    private static final UriMatcher URL_MATCHER =
                            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URL_MATCHER.addURI("auroraicc", "auroraadn", ADN);
        URL_MATCHER.addURI("auroraicc", "aurorafdn", FDN);
        URL_MATCHER.addURI("auroraicc", "aurorasdn", SDN);
        URL_MATCHER.addURI("auroraicc", "auroraadn2", ADN2);
        URL_MATCHER.addURI("auroraicc", "aurorafdn2", FDN2);
        URL_MATCHER.addURI("auroraicc", "aurorasdn2", SDN2);
    }


    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection,
            String[] selectionArgs, String sort) {
/*        switch (URL_MATCHER.match(url)) {
            case ADN:
                return loadFromEf(IccConstants.EF_ADN);

            case FDN:
                return loadFromEf(IccConstants.EF_FDN);

            case SDN:
                return loadFromEf(IccConstants.EF_SDN);

            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }*/
    	return null;
    }

    @Override
    public String getType(Uri url) {
        switch (URL_MATCHER.match(url)) {
            case ADN:
            case FDN:
            case SDN:
            case ADN2:
            case FDN2:
            case SDN2:
                return "vnd.android.cursor.dir/sim-contact";

            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        Uri resultUri;
        int efType;
        String pin2 = null;

        if (DBG) auroralog("insert");

        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN:
                efType = EF_ADN;
                slot_id=0;
                break;
            case ADN2:
            	efType = EF_ADN;
                slot_id=1;
                break;
            case FDN:
                efType = EF_FDN;
                slot_id=0;
                pin2 = initialValues.getAsString("pin2");                
                break;
            case FDN2:
                efType = EF_FDN;
                slot_id=1;
                pin2 = initialValues.getAsString("pin2");                
                break;
            default:
                throw new UnsupportedOperationException(
                        "Cannot insert into URL: " + url);
        }

        String tag = initialValues.getAsString("tag");
        String number = initialValues.getAsString("number");
        // TODO(): Read email instead of sending null.
        boolean success = auroraaddIccRecordToEf(efType, tag, number, null, pin2);
        auroralog("insert efType="+efType+" tag="+tag+" number="+number+" ");
        if (!success) {
            return null;
        }

        StringBuilder buf = new StringBuilder("content://icc/");
        switch (match) {
            case ADN:
                buf.append("adn/");
                break;

            case FDN:
                buf.append("fdn/");
                break;
        }

        // TODO: we need to find out the rowId for the newly added record
        buf.append(0);

        resultUri = Uri.parse(buf.toString());

        /*
        // notify interested parties that an insertion happened
        getContext().getContentResolver().notifyInsert(
                resultUri, rowID, null);
        */

        return resultUri;
    }

    private String auroranormalizeValue(String inVal) {
/*        int len = inVal.length();
        if(len==0)
        	return inVal;
        String retVal = inVal;

        if (inVal.charAt(0) == '\'' && inVal.charAt(len-1) == '\'') {
            retVal = inVal.substring(1, len-1);
        }
*/      
    	String s="鳚";
    	inVal=inVal.replace(s.toCharArray()[0], ' '); //aurora add zhouxiaobing 20140305 for delete space
    	auroralog("auroranormalizeValue inVal="+inVal);
        return inVal;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        int efType;

        if (DBG) auroralog("delete");

        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN:
                efType = EF_ADN;
                slot_id=0;
                break;
            case ADN2:
                efType = EF_ADN;
                slot_id=1;
                break;     

            case FDN:
                efType = EF_FDN;
                slot_id=0;
                break;
            case FDN2:
                efType = EF_FDN;
                slot_id=1;
                break;     

            default:
                throw new UnsupportedOperationException(
                        "Cannot insert into URL: " + url);
        }

        // parse where clause
        String tag = null;
        String number = null;
        String[] emails = null;
        String pin2 = null;

        String[] tokens = where.split("AND");
        int n = tokens.length;

        while (--n >= 0) {
            String param = tokens[n];
            if (DBG) auroralog("parsing '" + param + "'");

            String[] pair = param.split("=", 2);
            if (DBG) auroralog("pair[0]=" + pair[0] + "pair[1]="+pair[1]);
            String key = pair[0].trim();
            String val = pair[1].trim();

            if (STR_TAG.equals(key)) {
                tag = auroranormalizeValue(val);
                if (DBG) auroralog("delete:tag=" + tag);
            } else if (STR_NUMBER.equals(key)) {
                number = auroranormalizeValue(val);
            } else if (STR_EMAILS.equals(key)) {
                //TODO(): Email is null.
                emails = null;
            } else if (STR_PIN2.equals(key)) {
                pin2 = auroranormalizeValue(val);
            }
        }

//        if (TextUtils.isEmpty(number)) {
//            return 0;
//        }

        if (efType == EF_FDN && TextUtils.isEmpty(pin2)) {
            return 0;
        }

        boolean success = deleteIccRecordFromEf(efType, tag, number, emails, pin2);
        auroralog("delete efType="+efType+" tag="+tag+" number="+number+" success="+success);
        if (!success) {
            return 0;
        }

        return 1;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int efType;
        String pin2 = null;

        if (DBG) auroralog("update");

        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN:
                efType = EF_ADN;
                slot_id=0;
                break;
            case ADN2:
                efType = EF_ADN;
                slot_id=1;
                break;

            case FDN:
                efType = EF_FDN;
                slot_id=0;
                pin2 = values.getAsString("pin2");
                break;
            case FDN2:
                efType = EF_FDN;
                slot_id=1;
                pin2 = values.getAsString("pin2");
                break;     

            default:
                throw new UnsupportedOperationException(
                        "Cannot insert into URL: " + url);
        }

        String tag = values.getAsString("tag");
        String number = values.getAsString("number");
        String[] emails = null;
        String newTag = values.getAsString("newTag");
        String newNumber = values.getAsString("newNumber");
        String[] newEmails = null;
        // TODO(): Update for email.
        boolean success = auroraupdateIccRecordInEf(efType, tag, number,
                newTag, newNumber, pin2);
        auroralog(" update efType="+efType+" tag="+tag+" number="+number+" success="+success); 
        if (!success) {
            return 0;
        }

        return 1;
    }

    private MatrixCursor loadFromEf(int efType) {
/*        if (DBG) auroralog("loadFromEf: efType=" + efType);

        List<AdnRecord> adnRecords = null;
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(
                    ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEf(efType);
            }
        } catch (RemoteException ex) {
            // ignore it
        } catch (SecurityException ex) {
            if (DBG) auroralog(ex.toString());
        }

        if (adnRecords != null) {
            // Load the results
            final int N = adnRecords.size();
            final MatrixCursor cursor = new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES, N);
            if (DBG) auroralog("adnRecords.size=" + N);
            for (int i = 0; i < N ; i++) {
                loadRecord(adnRecords.get(i), cursor, i);
            }
            return cursor;
        } else {
            // No results to load
            Rlog.w(TAG, "Cannot load ADN records");
            return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
        }*/
    	return null;
    }

    private boolean
    auroraaddIccRecordToEf(int efType, String name, String number, String[] emails, String pin2) {
        if (DBG) auroralog("addIccRecordToEf: efType=" + efType + ", name=" + name +
                ", number=" + number + ", emails=" + emails);
        boolean success = false;

        // TODO: do we need to call getAdnRecordsInEf() before calling
        // updateAdnRecordsInEfBySearch()? In any case, we will leave
        // the UI level logic to fill that prereq if necessary. But
        // hopefully, we can remove this requirement.
      //aurora add zhouxiaobing  for xiaomi td 20140606 start    
        try{
        	Class<?> sp=Class.forName("com.android.internal.telephony.MiuiAdnRecord");
        	Class sps[]=new Class[]{String.class,String.class,String[].class,String.class};
        	Constructor con=sp.getConstructor(sps);
        	Object os1[]=new Object[]{name,number,null,""};
        	Object os2[]=new Object[]{"","",null,""};
        	IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(
                    ServiceManager.getService("simphonebook"));
        	Object ooo=ServiceManager.getService("simphonebook");
        	
        	Class<?> sp2=Class.forName("com.android.internal.telephony.MiuiIccPhoneBookInterfaceManagerProxy");
        	Class<?> sp3=Class.forName("com.android.internal.telephony.AdnRecord");
        	Method method3=sp2.getMethod("updateUsimPhoneBookRecordsBySearch", int.class,sp3,sp3);
        	Method method=sp2.getMethod("updateAdnRecordsInEfBySearch", int.class,String.class,String.class,String.class,String.class,String.class);
        	
        	Class<?> sp4=Class.forName("com.android.internal.telephony.MiuiIccProvider");
        	Method method2=sp4.getDeclaredMethod("getIccPhoneBookService");
        	method2.setAccessible(true);
        	Object ob=method2.invoke(sp4.newInstance());
        	//success=((Boolean)(method.invoke((com.android.internal.telephony.IccPhoneBookInterfaceManager)ob, efType,"","",name,number,""))).booleanValue();
            success=((Boolean)(method3.invoke(ob,efType,con.newInstance(os2),con.newInstance(os1)))).booleanValue();
        	return success;
        }catch(Exception e){auroralog("addIccRecordToEf: e= " + e);
        }
      //aurora add zhouxiaobing  for xiaomi td 20140606 end    
        success=AuroraTelephoneManager.auroraaddIccRecordToEf(efType,name,number,emails,pin2,slot_id); 
        if (DBG) auroralog("addIccRecordToEf: " + success);
        return success;
    }

    private boolean
    auroraupdateIccRecordInEf(int efType, String oldName, String oldNumber,
            String newName, String newNumber, String pin2) {
        if (DBG) auroralog("updateIccRecordInEf: efType=" + efType +
                ", oldname=" + oldName + ", oldnumber=" + oldNumber +
                ", newname=" + newName + ", newnumber=" + newNumber);
        boolean success = false;
       //aurora add zhouxiaobing  for xiaomi td 20140606 start
        try{
        	Class<?> sp=Class.forName("com.android.internal.telephony.MiuiAdnRecord");
        	Class sps[]=new Class[]{String.class,String.class,String[].class,String.class};
        	Constructor con=sp.getConstructor(sps);
        	Object os1[]=new Object[]{newName,newNumber,null,""};
        	Object os2[]=new Object[]{oldName,oldNumber,null,""};
        	IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(
                    ServiceManager.getService("simphonebook"));
        	Object ooo=ServiceManager.getService("simphonebook");
        	
        	Class<?> sp2=Class.forName("com.android.internal.telephony.MiuiIccPhoneBookInterfaceManagerProxy");
        	Class<?> sp3=Class.forName("com.android.internal.telephony.AdnRecord");
        	Method method3=sp2.getMethod("updateUsimPhoneBookRecordsBySearch", int.class,sp3,sp3);
        	Method method=sp2.getMethod("updateAdnRecordsInEfBySearch", int.class,String.class,String.class,String.class,String.class,String.class);
        	
        	Class<?> sp4=Class.forName("com.android.internal.telephony.MiuiIccProvider");
        	Method method2=sp4.getDeclaredMethod("getIccPhoneBookService");
        	method2.setAccessible(true);
        	Object ob=method2.invoke(sp4.newInstance());
        	//success=((Boolean)(method.invoke((com.android.internal.telephony.IccPhoneBookInterfaceManager)ob, efType,"","",name,number,""))).booleanValue();
            success=((Boolean)(method3.invoke(ob,efType,con.newInstance(os2),con.newInstance(os1)))).booleanValue();
        	return success;
        }catch(Exception e){auroralog("addIccRecordToEf: e= " + e);
        }
      //aurora add zhouxiaobing  for xiaomi td 20140606 end
        success=AuroraTelephoneManager.auroraupdateIccRecordInEf(efType,oldName,oldNumber,newName,newNumber,pin2,slot_id);
        if (DBG) auroralog("updateIccRecordInEf: " + success);
        return success;
    }


    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails,
            String pin2) {
        if (DBG) auroralog("deleteIccRecordFromEf: efType=" + efType +
                ", name=" + name + ", number=" + number + ", emails=" + emails + ", pin2=" + pin2);
        boolean success = false;
      //aurora add zhouxiaobing  for xiaomi td 20140606 start   
        try{
        	Class<?> sp=Class.forName("com.android.internal.telephony.MiuiAdnRecord");
        	Class sps[]=new Class[]{String.class,String.class,String[].class,String.class};
        	Constructor con=sp.getConstructor(sps);
        	Object os1[]=new Object[]{"","",null,""};
        	Object os2[]=new Object[]{name,number,null,""};
        	IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(
                    ServiceManager.getService("simphonebook"));
        	Object ooo=ServiceManager.getService("simphonebook");
        	
        	Class<?> sp2=Class.forName("com.android.internal.telephony.MiuiIccPhoneBookInterfaceManagerProxy");
        	Class<?> sp3=Class.forName("com.android.internal.telephony.AdnRecord");
        	Method method3=sp2.getMethod("updateUsimPhoneBookRecordsBySearch", int.class,sp3,sp3);
        	Method method=sp2.getMethod("updateAdnRecordsInEfBySearch", int.class,String.class,String.class,String.class,String.class,String.class);
        	
        	Class<?> sp4=Class.forName("com.android.internal.telephony.MiuiIccProvider");
        	Method method2=sp4.getDeclaredMethod("getIccPhoneBookService");
        	method2.setAccessible(true);
        	Object ob=method2.invoke(sp4.newInstance());
        	//success=((Boolean)(method.invoke((com.android.internal.telephony.IccPhoneBookInterfaceManager)ob, efType,"","",name,number,""))).booleanValue();
            success=((Boolean)(method3.invoke(ob,efType,con.newInstance(os2),con.newInstance(os1)))).booleanValue();
        	return success;
        }catch(Exception e){auroralog("addIccRecordToEf: e= " + e);
        }
      //aurora add zhouxiaobing  for xiaomi td 20140606 end  
        success=AuroraTelephoneManager.deleteIccRecordFromEf(efType,name,number,emails,pin2,slot_id);
        if (DBG) auroralog("deleteIccRecordFromEf: " + success);
        return success;
    }

    /**
     * Loads an AdnRecord into a MatrixCursor. Must be called with mLock held.
     *
     * @param record the ADN record to load from
     * @param cursor the cursor to receive the results
     */
/*    private void loadRecord(AdnRecord record, MatrixCursor cursor, int id) {
        if (!record.isEmpty()) {
            Object[] contact = new Object[4];
            String alphaTag = record.getAlphaTag();
            String number = record.getNumber();

            if (DBG) auroralog("loadRecord: " + alphaTag + ", " + number + ",");
            contact[0] = alphaTag;
            contact[1] = number;

            String[] emails = record.getEmails();
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                for (String email: emails) {
                    if (DBG) auroralog("Adding email:" + email);
                    emailString.append(email);
                    emailString.append(",");
                }
                contact[2] = emailString.toString();
            }
            contact[3] = id;
            cursor.addRow(contact);
        }
    }
*/
    private void auroralog(String msg) {
        //Rlog.d(TAG, "[IccProvider] " + msg);
    	Log.v(TAG, msg);
    }

}

