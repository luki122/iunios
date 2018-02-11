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

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.EntityModifier;
import com.mediatek.contacts.util.Objects;
import com.android.contacts.model.AccountType.EditField;

import android.app.Activity;
import android.content.Context;
import android.content.Entity;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import gionee.provider.GnContactsContract.CommonDataKinds.Organization;
import gionee.provider.GnContactsContract.RawContacts;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import aurora.widget.AuroraListView;
import android.widget.ListView;
import android.widget.TextView;
import android.text.InputFilter;

import java.util.ArrayList;

import aurora.widget.AuroraEditText;
/**
 * An editor for group membership.  Displays the current group membership list and
 * brings up a dialog to change it.
 */
public class AuroraOrganizationEditorView extends LinearLayout implements Editor {

	private static final String TAG = "AuroraOrganizationEditorView";
    private DataKind mKind;
    private ValuesDelta mEntry;
    private EntityDelta mState;
    private boolean mReadOnly;
    
    private EditorListener mListener;
    private boolean mCompanyWasEmpty = true;
    private boolean mPositionWasEmpty = true;
    
    private TextView mTitle;
    private String mTitleString;
    private TextView mCompanyTextView;
    private TextView mPositionTextView;
    private AuroraEditText[] mFieldEditTexts;
    private AuroraEditText fieldCompanyView;
    private AuroraEditText fieldPositionView;
    
    private ImageView comDelBtn;
    private ImageView posDelBtn;

    private String mCompanyString;
    private String mPositionString;

    private ViewIdGenerator mViewIdGenerator;

    private LayoutInflater mInflater;
    
    Context mContext = getContext();
    Resources resources = mContext.getResources();

    public AuroraOrganizationEditorView(Context context) {
        super(context);
    }

    public AuroraOrganizationEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mTitle = (TextView) findViewById(R.id.kind_title);

        mCompanyTextView = (TextView) findViewById(R.id.company_title);
        mPositionTextView = (TextView) findViewById(R.id.position_title);

        fieldCompanyView = (AuroraEditText)findViewById(R.id.company_name);
        fieldPositionView = (AuroraEditText)findViewById(R.id.position_name);

        comDelBtn = (ImageView)findViewById(R.id.delete_company_button);

        comDelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCompanyTextView != null) {
					fieldCompanyView.getText().clear();
				}
				comDelBtn.setVisibility(GONE);
				
			}
        	
        });
        posDelBtn = (ImageView)findViewById(R.id.delete_position_button);
        
        posDelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (fieldPositionView != null) {
					fieldPositionView.getText().clear();
				}
				posDelBtn.setVisibility(GONE);
				
			}
        	
        });
        
        mFieldEditTexts = new AuroraEditText[2];
        mFieldEditTexts[0] = fieldCompanyView;
        mFieldEditTexts[1] = fieldPositionView;
        
    }

    protected void clearPosField() {
    	if (fieldCompanyView != null) {
    		fieldCompanyView.setText("");
    	}		
	}

	protected void clearComField() {
		if (fieldCompanyView != null) {
    		fieldCompanyView.setText("");
    	}
	}

	@Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

    }

    public void setState(DataKind kind, EntityDelta state, boolean readOnly, ViewIdGenerator vig) {
        mKind = kind;
        mState = state;
        mReadOnly = readOnly;
        mViewIdGenerator = vig;
        
        mTitleString = resources.getString(kind.titleRes);
        mTitle.setText(mTitleString);

        for (int index = 0; index < 2; index++) {
            final EditField field = kind.fieldList.get(index);
            mFieldEditTexts[index].setId(vig.getId(state, kind, null, index));
            int inputType = field.inputType;
            mFieldEditTexts[index].setInputType(inputType);
            mFieldEditTexts[index].setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mFieldEditTexts[index].setBackgroundDrawable(null);
            mFieldEditTexts[index].setFilters(new InputFilter[]{new InputFilter.LengthFilter(FIELD_VIEW_MAX)});
         }
        rebuildFromState();

    }
    
    /**
     * Build editors for all current {@link #mState} rows.
     */
    public void rebuildFromState() {
        // Check if we are displaying anything here
        boolean hasEntries = mState.hasMimeEntries(mKind.mimeType);
        if (hasEntries) {
            for (ValuesDelta entry : mState.getMimeEntries(mKind.mimeType)) {
                // Skip entries that aren't visible
                if (!entry.isVisible()) continue;
//                if (isEmptyNoop(entry)) continue;
                setVisibility(View.VISIBLE);
//                createEditorView(entry);
                setValues(mKind, entry, mState, mReadOnly, mViewIdGenerator);
            }
        } else {
        	setVisibility(View.GONE);
        }
    }

	public void setTitleVisible(boolean visible) {
        findViewById(R.id.kind_title_layout).setVisibility(/*visible ? View.VISIBLE :*/ View.GONE);
    }
    
    public String getTitle() {
        return mTitleString;
    }
    
    public void addItem() {
    	ValuesDelta values = null;
    	if (mKind.typeOverallMax == 1) {

            // If we already have an item, just make it visible
            ArrayList<ValuesDelta> entries = mState.getMimeEntries(mKind.mimeType);
            if (entries != null && entries.size() > 0) {
                values = entries.get(0);
            }
        }
        // Insert a new child, create its view and set its focus
        if (values == null) {
            values = EntityModifier.insertChild(mState, mKind);
            mEntry = values;    
        }
        if (mEntry != null) {
        	setValues(mKind, mEntry, mState, mReadOnly, mViewIdGenerator);
        }
    	setVisibility(View.VISIBLE);
    }
    
    public DataKind getKind() {
        return mKind;
    }

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void setValues(DataKind kind, ValuesDelta entry, EntityDelta state,
			boolean readOnly, ViewIdGenerator vig) {
		mKind = kind;
        mEntry = entry;
        mState = state;
        mReadOnly = readOnly;
        mViewIdGenerator = vig;
        setId(vig.getId(state, kind, entry, ViewIdGenerator.NO_VIEW_INDEX));

		for (int i = 0; i < 2; i++) {
			final EditField field = mKind.fieldList.get(i);
			final String column = field.column;
			final String value = entry.getAsString(column);

			mFieldEditTexts[i].setText(value);
			if (value != null && !value.isEmpty()) {
				if (column == "data1") {
					comDelBtn.setVisibility(View.VISIBLE);
				} else if (column == "data4") {
					posDelBtn.setVisibility(View.VISIBLE);
				}
			}

			mFieldEditTexts[i].addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					// Trigger event for newly changed value
					onFieldChanged(column, s.toString());

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}
			});
		}		
	}

	@Override
	public void setDeletable(boolean deletable) {
		//no need
	}

	@Override
	public void setEditorListener(EditorListener listener) {
		mListener = listener;
		
	}

	@Override
	public void onFieldChanged(String column, String value) {
		if (!isFieldChanged(column, value)) {
            return;
        }
		
        // Field changes are saved directly
        mEntry.put(column, value);
        if (mListener != null) {
            mListener.onRequest(EditorListener.FIELD_CHANGED);
        }

        if (value != null && !value.isEmpty()) {
        	if (column == "data1") {
        		comDelBtn.setVisibility(View.VISIBLE);
        	} else if (column == "data4") {
        		posDelBtn.setVisibility(View.VISIBLE);
        	}
        } else {
        	if (column == "data1") {
        		comDelBtn.setVisibility(View.GONE);
        	} else if (column == "data4") {
        		posDelBtn.setVisibility(View.GONE);
        	}
        }
        
	}

	@Override
	public void deleteEditor() {
		//no need		
	}

	@Override
	public void clearAllFields() {
		//no need
	}
	
	protected boolean isFieldChanged(String column, String value) {
        final String dbValue = mEntry.getAsString(column);
        // nullable fields (e.g. Middle Name) are usually represented as empty columns,
        // so lets treat null and empty space equivalently here
        final String dbValueNoNull = dbValue == null ? "" : dbValue;
        final String valueNoNull = value == null ? "" : value;
        return !TextUtils.equals(dbValueNoNull, valueNoNull);
    }
	
	private static final int FIELD_VIEW_MAX = 40;

}
