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
 * limitations under the License.
 */

package com.android.contacts.editor;

import com.android.contacts.ContactLoader;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.EntityModifier;
import com.android.contacts.util.IntentFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * An editor for group membership.  Displays the current group membership list and
 * brings up a dialog to change it.
 */
public class GnRingtoneEditorView extends LinearLayout implements OnClickListener{

	private EntityDelta mState;
	private String mRingtoneUri;
	private onPickRingtoneListener mPickRingtoneListener;
	
	private DataKind mKind;
    private TextView mRingtoneTv;
    private String mNoGroupString;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;

    public GnRingtoneEditorView(Context context) {
        super(context);
    }

    public GnRingtoneEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = mContext.getResources();
        mPrimaryTextColor = resources.getColor(R.color.primary_text_color);
        mSecondaryTextColor = resources.getColor(R.color.secondary_text_color);
        mNoGroupString = mContext.getString(R.string.group_edit_field_hint_text);
        
        mRingtoneTv = (TextView)findViewById(R.id.ringtone_list);

        TextView kindTitle = (TextView)findViewById(R.id.kind_title);
        if (null != kindTitle) {
        	/*if (ContactsApplication.sIsGnContactsSupport) {
            	kindTitle.setTextColor(ResConstant.sHeaderTextColor);	
            }*/
            
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            	kindTitle.setText(R.string.gn_others_label);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mRingtoneTv != null) {
            mRingtoneTv.setEnabled(enabled);
            ((View)(mRingtoneTv.getParent())).setOnClickListener(this);
        }
    }
    
    public void setKind(DataKind kind) {
        mKind = kind;
        TextView kindTitle = (TextView) findViewById(R.id.kind_title);
        kindTitle.setText(getResources().getString(kind.titleRes).toUpperCase());
        // aurora <ukiliu> <2013-9-17> add for auroro ui begin
        kindTitle.setVisibility(View.GONE);
		// aurora <ukiliu> <2013-9-17> add for auroro ui end
        /*if (ContactsApplication.sIsGnContactsSupport) {
            kindTitle.setTextColor(ResConstant.sHeaderTextColor);
        }*/
    }
    
    public void setState(EntityDelta state) {
    	mState = state;
    	ValuesDelta values = mState.getValues();
    	mRingtoneUri = values.getAsString(Contacts.CUSTOM_RINGTONE);
        if (null != mRingtoneUri) {
        	new SimpleAsynTask() {
        		String title = null;
				@Override
				protected Integer doInBackground(Integer... params) {
		            //Gionee:huangzy 20130308 modify for CR00772601 start
					/*title = RingtoneManager.getRingtone(getContext(), Uri.parse(mRingtoneUri)).
						getTitle(getContext());*/
					title = ContactLoader.gnGetRingtoneTile(getContext(), mRingtoneUri);
		            //Gionee:huangzy 20130308 modify for CR00772601 end
					return null;
				}
				
				@Override
				protected void onPostExecute(Integer result) {
					mRingtoneTv.setText(title);
				}
			}.execute();
        }
    }

    private void updateView() {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

	@Override
	public void onClick(View v) {
		if (null != mPickRingtoneListener) {
			mPickRingtoneListener.onPickClick(mRingtoneTv, mState, mRingtoneUri);
		}
	}
	
	public interface onPickRingtoneListener {
		public void onPickClick(TextView view, EntityDelta state, String ringtoneUri);
	}
	
	public void setOnPickRingtoneListener(onPickRingtoneListener pickRingtoneListener) {
		mPickRingtoneListener = pickRingtoneListener;
	}
}
