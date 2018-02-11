/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.contacts.detail;


import com.android.contacts.ContactLoader;
import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.util.NotifyingAsyncQueryHandler;
import com.mediatek.contacts.widget.SimPickerDialog;      

import android.app.ActionBar;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Entity.NamedContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import aurora.widget.AuroraButton;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.List;
import gionee.provider.GnTelephony.SIMInfo;  
import android.widget.LinearLayout;
import java.util.ArrayList;
import android.net.Uri;

public class AssociationSimActivity extends ContactsActivity implements
        NotifyingAsyncQueryHandler.AsyncQueryListener, OnClickListener,
        NotifyingAsyncQueryHandler.AsyncUpdateListener {
    
    public static ContactDetailInfo sContactDetailInfo = null;       
    public static final String INTENT_DATA_KEY_SEL_DATA_ID = "sel_data_id";
    public static final String INTENT_DATA_KEY_SEL_SIM_ID = "sel_sim_id";
    private static final String TAG = "AssociationSimActivity";
    
    
    private ActionBar                  mActionBar        = null;
    private ImageView                  mContactPhoto     = null;
    private AuroraListView                   mListView         = null;
    private AuroraButton                     mBtnSave          = null;
    private AuroraButton                     mBtnCancel        = null;
    private NotifyingAsyncQueryHandler mHandler          = null;
    private SimInfoMgr                 mSimInfoMgr       = new SimInfoMgr();
    private NumberInfoMgr              mNumberInfoMgr    = new NumberInfoMgr();
    
    private long                       mInitDataId       = -1;
    private int                        mInitSimId        = -1;
    private int                        mAdapterChildSize = 2;
    private AuroraAlertDialog                mSelectDialog     = null;
    private int                        mSelectDialogType = 0;
    
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
       
        Intent intent = this.getIntent();
        this.mInitDataId = intent.getLongExtra(INTENT_DATA_KEY_SEL_DATA_ID, -1);
        this.mInitSimId = intent.getIntExtra(INTENT_DATA_KEY_SEL_SIM_ID, -1);
        this.setContentView(R.layout.association_sim);
        this.initView();
        
        this.mHandler = new NotifyingAsyncQueryHandler(this, this);
        mHandler.setUpdateListener(this);
    }
        
    public void initView() {         
        this.mActionBar = getActionBar();
        if (this.mActionBar != null) {
            this.mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE, 
                                         ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);                   
            this.mActionBar.setTitle(sContactDetailInfo.mDisplayTitle); 
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
                 this.mActionBar.setSubtitle(sContactDetailInfo.mDisplaySubtitle);
             *   CR ID: ALPS00115576
             */
            if (sContactDetailInfo.mDisplaySubtitle != null && !sContactDetailInfo.mDisplaySubtitle.isEmpty()) {
                this.mActionBar.setSubtitle(sContactDetailInfo.mDisplaySubtitle);
            }
            /*
             * Bug Fix by Mediatek End.
             */
        }
        
        this. mContactPhoto = (ImageView) this.findViewById(R.id.contact_photo);
        if (this.mContactPhoto != null) {
            sContactDetailInfo.setPhoto(this, this.mContactPhoto);                      
        }
        
        this.mListView = (AuroraListView) findViewById(R.id.association_list);
        this.mBtnSave = (AuroraButton) findViewById(R.id.btn_done);
        this.mBtnCancel = (AuroraButton) findViewById(R.id.btn_discard);

        this.mBtnSave.setOnClickListener(this);
        this.mBtnCancel.setOnClickListener(this);                 
    } 
   
    @Override
    protected void onResume() {
        super.onResume();
                       
        this.mNumberInfoMgr.initNumberInfo();  
        this.mSimInfoMgr.initSimInfo(this);
        this.mNumberInfoMgr.setShowingNumberNameByDataId(this.mInitDataId);
        this.mSimInfoMgr.setShowingIndexBySimId(this.mInitSimId);
        
        this.mListView.setAdapter(new ListViewAdapter(this));
        this.mListView.setOnItemClickListener(this.onListViewItemClick);
        
        this.mBtnSave.setEnabled(this.isSimInfoChanged());
    }
    
    /*
     * Bug Fix by Mediatek Begin.
     *   Original Android's code:
     *   
     *   CR ID: ALPS00114099
     */
    @Override
    protected void onDestroy() {
        Intent intent = this.getIntent();
        intent.putExtra(INTENT_DATA_KEY_SEL_DATA_ID, this.mInitDataId);
        intent.putExtra(INTENT_DATA_KEY_SEL_SIM_ID, this.mInitSimId);
        super.onDestroy();
    }
    /*
     * Bug Fix by Mediatek End.
     */
    
    private boolean isSimInfoChanged() {
        if (mNumberInfoMgr.mNumberInfoList.size() > 0 && mSimInfoMgr.mSimInfoList.size() > 0) {
            return mNumberInfoMgr.getShowingNumberSimId() != mSimInfoMgr.getShowingSimId();
        }
        return false;
    }
    
    public void closeSelectDialog() {
        if (mSelectDialog != null && !this.isFinishing()) {
            mSelectDialog.dismiss();
            mSelectDialog = null;
        }
    }
    
    public void saveAssociationSimInfo() {
        ContentValues values = new ContentValues();
        values.put(Data.SIM_ASSOCIATION_ID, mSimInfoMgr.getShowingSimId());
        this.mHandler.startUpdate(0, this, Data.CONTENT_URI, values, Data._ID + "=? ", new String[] { String.valueOf(mNumberInfoMgr.getShowingNumberDataId())});
    }
    
    public void setListViewChildText(View view, String text1, String text2) {         
        TextView txtTitle = (TextView) view.findViewById(R.id.text1);
        txtTitle.setText(text1);
        TextView txtData = (TextView) view.findViewById(R.id.text2);
        txtData.setText(text2);
    }
    
    private void returnToContactDetail(boolean isNewIntent) {
        if (isNewIntent) {
            final Intent intent = new Intent(Intent.ACTION_VIEW, sContactDetailInfo.mLookupUri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }

    private OnItemClickListener onListViewItemClick = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
            if (position < 2) {
                closeSelectDialog();
                if (position == 1 && mSimInfoMgr.mSimInfoList.size() == 0) {
                    Log.i(TAG, "[onListViewItemClick]: mSimInfoList.size() = 0");
                    return;
                }
                mSelectDialogType = position;
                final NumberInfoAdapter adapter;

                final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mSelectDialogType == 0) {
                            /*
                             * Bug Fix by Mediatek Begin. 
                             * Original Android's code: 
                                mNumberInfoMgr.setShowingIndex(which);
                            } 
                             CR ID: ALPS00115837
                            */
                            if (!mNumberInfoMgr.setShowingIndex(which)) {
                                closeSelectDialog();
                                return;
                            }
                            /*
                             * Bug Fix by Mediatek End.
                             */
                            mInitDataId = mNumberInfoMgr.getShowingNumberDataId();
                            mSimInfoMgr.setShowingIndexBySimId(mNumberInfoMgr.getShowingNumberSimId());                                                      
                        } else {
                            mSimInfoMgr.setShowingIndex(which);
                        /*
                         * Bug Fix by Mediatek Begin. 
                         * Original Android's code: 
                            mInitSimId = mSimInfoMgr.getShowingSimId();
                        } 
                         CR ID: ALPS00114099
                        */
                        }
                        mInitSimId = mSimInfoMgr.getShowingSimId();
                        /*
                         * Bug Fix by Mediatek End.
                         */
                        mListView.setAdapter(new ListViewAdapter(AssociationSimActivity.this));   
                        mBtnSave.setEnabled(isSimInfoChanged());
                        closeSelectDialog();
                    }
                };

                if (position == 0) {
                    adapter = new NumberInfoAdapter(AssociationSimActivity.this);
                    AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(AssociationSimActivity.this);
                    builder.setSingleChoiceItems(adapter, -1, onClickListener)
                            .setTitle(sContactDetailInfo.mDisplayTitle)
                            .setIcon(android.R.drawable.ic_menu_more).setTitleDividerVisible(true);
                    mSelectDialog = builder.create();
                } else {
                    /*
                     * Bug Fix by Mediatek Begin. 
                     * Original Android's code: 
                     *   mSelectDialog = SimPickerDialog.create(AssociationSimActivity.this, getResources().getString(R.string.associate_SIM), onClickListener);
                     * CR ID: ALPS00115742
                    */
                    mSelectDialog = SimPickerDialog.create(AssociationSimActivity.this, getResources().getString(R.string.associate_SIM), false, onClickListener);
                    /*
                     * Bug Fix by Mediatek End.
                     */
                }
                mSelectDialog.show();
            }
        }

    };
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:               
                this.returnToContactDetail(true);
                return true;
                
            default:
                break;
        }
        
        return super.onOptionsItemSelected(item);
    }
  
    public void onClick(View v) {        
        switch (v.getId()) {            
            case R.id.btn_done:
                this.saveAssociationSimInfo();
                this.returnToContactDetail(true);
                break;

            case R.id.btn_discard:
                this.returnToContactDetail(false);
                break;
        }
    }    
    
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
    
    }        
    
    private class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        ListViewAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return mAdapterChildSize;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {               
            convertView = mInflater.inflate(R.layout.association_sim_item, null);
            ImageView moreImage = (ImageView) convertView.findViewById(R.id.more_icon);
            moreImage.setImageResource(R.drawable.ic_btn_round_more_normal);
           
            if (position == 0) {
                setListViewChildText(convertView, mNumberInfoMgr.getShowingNumberTypeName(), mNumberInfoMgr.getShowingNumberContent());
            } else {   
                setListViewChildText(convertView, getResources().getString(R.string.associatiated_SIM), mSimInfoMgr.getShowingSimName());
            }                
            convertView.setTag(position);
            
            return convertView;
        }

    }
        
    private class NumberInfoAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        NumberInfoAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return mNumberInfoMgr.mNumberInfoList.size();
        }

        public Object getItem(int position) {
            return mNumberInfoMgr.mNumberInfoList.get(position);
        }

        public long getItemId(int position) {           
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.number_list_item, null);            
            setListViewChildText(convertView, mNumberInfoMgr.getTypeName(position), mNumberInfoMgr.getContent(position));                          
            convertView.setTag(position);           
            return convertView;
        }

    }
    
    public final class NumberInfoMgr {
        public List<NamedContentValues> mNumberInfoList = null;        
        private int mShowingIndex = -1;
        
        public void clear() {
            if (this.mNumberInfoList != null) {
                this.mNumberInfoList.clear();
            }
            this.mShowingIndex = -1;
        }
        
        public boolean initNumberInfo() {  
            this.clear();
            this.mNumberInfoList = new ArrayList<NamedContentValues>();
            for (NamedContentValues values: sContactDetailInfo.mNumberInfoList) {
                this.mNumberInfoList.add(values);
            }          
            return !this.mNumberInfoList.isEmpty();
        }        
          
        public boolean setShowingIndex(int showingIndex) {
            if (this.mShowingIndex != showingIndex) {
                if (showingIndex > -1 && showingIndex < this.mNumberInfoList.size()) {
                    this.mShowingIndex = showingIndex;
                    return true;
                }
            }
            return false;
        }
        
        public String getShowingNumberContent() {
            return getContent(this.mShowingIndex);
        }
        
        public String getShowingNumberTypeName() {
            return getTypeName(this.mShowingIndex);
        }
        
        public String getContent(int index) {
            if (index > -1 && index < this.mNumberInfoList.size()) {
                NamedContentValues subValues = this.mNumberInfoList.get(index);
                return subValues.values.getAsString(Data.DATA1);
            }
            return "";
        }
        
        public String getTypeName(int index) {
            if (index > -1 && index < this.mNumberInfoList.size()) {
                NamedContentValues subValues = this.mNumberInfoList.get(index);            
                int type = Integer.parseInt(subValues.values.getAsString(Data.DATA2));
                return (String) CommonDataKinds.Phone.getTypeLabel(getResources(), type, null);
            }
            return "";
        }
        
        public boolean setShowingNumberNameByDataId(long id) {
            for (int i=0; i<this.mNumberInfoList.size(); i++) {
                NamedContentValues subValues = this.mNumberInfoList.get(i);
                if (id == subValues.values.getAsLong(Data._ID)) {
                    this.mShowingIndex = i;
                    return true;
                }
            }
            return false;
        }
        
        public int getShowingNumberSimId() {
            if (this.mShowingIndex > -1) {  
                NamedContentValues subValues = this.mNumberInfoList.get(this.mShowingIndex); 
                return subValues.values.getAsInteger(Data.SIM_ASSOCIATION_ID);
            }
            return -1;
        }
        
        public long getShowingNumberDataId() {
            if (this.mShowingIndex > -1) {  
                NamedContentValues subValues = this.mNumberInfoList.get(this.mShowingIndex);
                return subValues.values.getAsLong(Data._ID);
            }
            return -1;
        }
    }
           
    public final class SimInfoMgr {
        public List<SIMInfo> mSimInfoList = null;        
        private int mShowingIndex = -1;
        
        public void clear() {
            if (this.mSimInfoList != null) {
                this.mSimInfoList.clear();
            }
            this.mShowingIndex = -1;
        }
        
        public boolean initSimInfo(Context context) { 
            this.clear();
            this.mSimInfoList = ContactDetailActivity.getInsertedSimCardInfoList(AssociationSimActivity.this, false);    
            return !this.mSimInfoList.isEmpty();
        }        
          
        public boolean setShowingIndex(int showingIndex) {
            if (this.mShowingIndex != showingIndex) {                
                this.mShowingIndex = showingIndex;
                return true;               
            }
            return false;
        }
        
        public boolean setShowingIndexBySimId(int simId) {
            if (simId < 0) {
                return this.setShowingIndex(-1);
            }
            for (int i=0; i<this.mSimInfoList.size(); i++) {
                SIMInfo simInfo = this.mSimInfoList.get(i);
                if (simInfo.mSimId == simId) {
                    this.mShowingIndex = i;
                    return true;
                }
            }            
            return false;
        }
        
        public String getShowingSimName() {
            if (this.mShowingIndex > -1) {
                return this.mSimInfoList.get(this.mShowingIndex).mDisplayName;
            }
            return getResources().getString(R.string.unassociated);
        }
        
        public int getShowingSimId() {
            if (this.mShowingIndex > -1) {
                return (int)this.mSimInfoList.get(this.mShowingIndex).mSimId;
            }
            return -1;
        }
    }

    public static final class ContactDetailInfo {
        public String         mDisplayTitle     = "";
        public String         mDisplaySubtitle  = "";
        public Uri            mLookupUri        = null;
        
        public ContactLoader.Result     mContactData    = null;
        public List<NamedContentValues> mNumberInfoList = null;
        
       
        public ContactDetailInfo(String displayTitle, String displaySubtitle, Uri lookupUri, List<NamedContentValues> numberInfoList) {
            mDisplayTitle = displayTitle;
            mDisplaySubtitle = displaySubtitle;
            mLookupUri = lookupUri;
            mNumberInfoList = numberInfoList;            
           
        }
        
        public void setPhoto(Context context, ImageView photoView) {
            if (mContactData != null) {
                ContactDetailDisplayUtils.setPhoto(context, mContactData, photoView);
            } 
        }
        
    }

    public void onUpdateComplete(int token, Object cookie, int result) {
        if (token == 0) {
            this.sendBroadcast(new Intent("com.android.contacts.associate_changed"));
        }
    }
}
