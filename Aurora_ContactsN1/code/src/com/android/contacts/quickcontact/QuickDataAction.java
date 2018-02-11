/*
 * 
 * new add field for action data 
 * 
 */

package com.android.contacts.quickcontact;

import com.android.contacts.model.DataKind;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.util.Log;

public class QuickDataAction extends DataAction {

    /*
     * New Feature by Mediatek Begin.            
     * add for association sim card        
     */
    private int mSimId = 0;
    /*
     * New Feature  by Mediatek End.
    */
    
    public QuickDataAction(Context context, String mimeType, DataKind kind, long dataId,
            Cursor cursor) {
        super(context, mimeType, kind, dataId, cursor);

        final int index = cursor.getColumnIndex(Data.SIM_ASSOCIATION_ID);
        if (index > -1) {
            mSimId = cursor.getInt(index);
        }
    }

    public int getSimId() {
        return mSimId;
    }
}
