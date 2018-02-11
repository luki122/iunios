/*
 * aurora add zhouxiaobing 20131220 for import fail caused by some vcf file;
 * */
package com.android.contacts.vcard;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.android.vcard.VCardEntryCommitter;
import com.android.vcard.VCardConfig;
import com.android.vcard.VCardEntry;
import com.mediatek.contacts.util.TelephonyUtils;

public class AuroraVCardEntryCommitter extends VCardEntryCommitter{
	
    private final ContentResolver mContentResolver;
    private long mTimeToCommit;
    private int mCounter;
    private ArrayList<ContentProviderOperation> mOperationList;
    private final ArrayList<Uri> mCreatedUris = new ArrayList<Uri>();
    public AuroraVCardEntryCommitter(ContentResolver resolver) {
    	super(resolver);
    	mContentResolver = resolver;
        
    }
    @Override
    public void onEnd() {
        if (mOperationList != null) {
            mCreatedUris.add(pushIntoContentResolver(mOperationList));
        }

        if (VCardConfig.showPerformanceLog()) {
        }
    }
    @Override
    public void onEntryCreated(final VCardEntry vcardEntry) {
/*        final long start = System.currentTimeMillis();
        mOperationList = vcardEntry.constructInsertOperations(mContentResolver, mOperationList);
        mCounter++;
        if (mCounter >= 80) {
            mCreatedUris.add(pushIntoContentResolver(mOperationList));
            mCounter = 0;
            mOperationList = null;
        }
        mTimeToCommit += System.currentTimeMillis() - start;*/
    	final long start = System.currentTimeMillis();
        mOperationList = vcardEntry.constructInsertOperations(mContentResolver, mOperationList);
        mCounter++;
        if (mOperationList != null && mOperationList.size() >= 80) {
            mCreatedUris.add(pushIntoContentResolver(mOperationList));
            mCounter = 0;
            mOperationList = null;
        }
        mTimeToCommit += System.currentTimeMillis() - start;
    }
    
    private Uri pushIntoContentResolver(ArrayList<ContentProviderOperation> operationList) {
        try {
        	TelephonyUtils.sleepInCall();
            final ContentProviderResult[] results = mContentResolver.applyBatch(
                    ContactsContract.AUTHORITY, operationList);

            // the first result is always the raw_contact. return it's uri so
            // that it can be found later. do null checking for badly behaving
            // ContentResolvers
            return ((results == null || results.length == 0 || results[0] == null)
                            ? null : results[0].uri);
        } catch (RemoteException e) {

            return null;
        } catch (OperationApplicationException e) {

            return null;
        }
    }  
    @Override
    public ArrayList<Uri> getCreatedUris() {
        return mCreatedUris;
    }    

}
