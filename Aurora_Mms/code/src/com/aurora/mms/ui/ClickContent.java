package com.aurora.mms.ui;
//Aurora xuyong 2013-09-20 created for aurora's new feature
// Aurora xuyong 2013-10-14 added for aurora's new feature start 
import java.util.ArrayList;
// Aurora xuyong 2013-10-14 added for aurora's new feature end

import android.net.Uri;

public class ClickContent {
    // Aurora xuyong 2013-10-14 modified for aurora's new feature start 
    private String mValue;
    private String mContact;
    private boolean mExistContactDB;
    private int mIndicatePhoneOrSim;
    private Uri mContactUri;
    private ArrayList<String> mLists;
    // Aurora xuyong 2013-10-14 modified for aurora's new feature end
    
    public void setValue(String value) {
        mValue = value;
    }
    // Aurora xuyong 2013-10-14 added for aurora's new feature start 
    public void setValues(ArrayList<String> lists) {
        mLists = lists;
    }
    // Aurora xuyong 2013-10-14 added for aurora's new feature end
    public void setContact(String contact) {
        mContact = contact;
    }
    
    public void setExistContactDB(boolean existContactDB) {
        mExistContactDB = existContactDB;
    }
    
    public void setIndicatePhoneOrSim(int indicatePhoneOrSim) {
        mIndicatePhoneOrSim = indicatePhoneOrSim;
    }
    
    public void setContactUri(Uri contactUri) {
        mContactUri = contactUri;
    }
    
    public String getValue() {
        return mValue;
    }
    // Aurora xuyong 2013-10-14 added for aurora's new feature start 
    public ArrayList<String> getValues() {
        return mLists;
    }
    // Aurora xuyong 2013-10-14 added for aurora's new feature end
    public String getContact() {
        return mContact;
    }
    
    public boolean getExistContactDB() {
        return mExistContactDB;
    }
    
    public int getIndicatePhoneOrSim() {
        return mIndicatePhoneOrSim;
    }
    
    public Uri getContactUri() {
        return mContactUri;
    }

}
