/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import gionee.provider.GnContactsContract.CommonDataKinds.Event;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * Custom view for an entire section of data as segmented by
 * {@link DataKind} around a {@link Data#MIMETYPE}. This view shows a
 * section header and a trigger for adding new {@link Data} rows.
 */
public class KindSectionView extends LinearLayout implements EditorListener {
    private static final String TAG = "KindSectionView";

    private TextView mTitle;
    private ViewGroup mEditors;
    private View mAddFieldFooter;
    private String mTitleString;

    private DataKind mKind;
    private EntityDelta mState;
    private boolean mReadOnly;

    private ViewIdGenerator mViewIdGenerator;

    private LayoutInflater mInflater;

    public KindSectionView(Context context) {
        this(context, null);
    }

    public KindSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mEditors != null) {
            int childCount = mEditors.getChildCount();
            for (int i = 0; i < childCount; i++) {
                mEditors.getChildAt(i).setEnabled(enabled);
            }
        }
        // aurora <ukiliu> <2013-9-17> remove for auroro ui begin
        /*
        if (enabled && !mReadOnly) {
            mAddFieldFooter.setVisibility(View.VISIBLE);
        } else {
            mAddFieldFooter.setVisibility(View.GONE);
        }
        */
		// aurora <ukiliu> <2013-9-17> remove for auroro ui end
    }

    public boolean isReadOnly() {
        return mReadOnly;
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mTitle = (TextView) findViewById(R.id.kind_title);
        mEditors = (ViewGroup) findViewById(R.id.kind_editors);
		// aurora <ukiliu> <2013-9-17> remove for auroro ui begin
        /*
        mAddFieldFooter = findViewById(R.id.add_field_footer);
        mAddFieldFooter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup click listener to add an empty field when the footer is clicked.
                mAddFieldFooter.setVisibility(View.GONE);
                addItem();
            }
        });
        */
		// aurora <ukiliu> <2013-9-17> remove for auroro ui end
    }

    @Override
    public void onDeleteRequested(Editor editor) {
        // If there is only 1 editor in the section, then don't allow the user to delete it.
        // Just clear the fields in the editor.
        if (getEditorCount() == 1) {
            editor.clearAllFields();
        } else {
            // Otherwise it's okay to delete this {@link Editor}
            editor.deleteEditor();
        }
        updateAddFooterVisible();
    }

    @Override
    public void onRequest(int request) {
        // If a field has become empty or non-empty, then check if another row
        // can be added dynamically.
        if (request == FIELD_TURNED_EMPTY || request == FIELD_TURNED_NON_EMPTY) {
            updateAddFooterVisible();
        }
		Log.v(TAG,"onRequest1 request="+request);
		//aurora change zhouxiaobing 20140716  start	
        if(request == FIELD_TURNED_EMPTY)
        {
           Log.v(TAG,"onRequest2 request="+request);
              if(editToActivity!=null)
              {  
                if(isEmpty())
                 {
                   
				   editToActivity.onRequest(FIELD_TURNED_EMPTY);
                 }
				else
				   editToActivity.onRequest(FIELD_TURNED_NON_EMPTY);

			  }
		}
		else if(request==FIELD_TURNED_NON_EMPTY)
		{
		DataKind kind = getKind();
		Log.v(TAG,"onRequest3 request="+request+"kind="+kind.mimeType);
		
             if(editToActivity!=null)
             {
             Log.v(TAG,"onRequest4 request="+request);
               editToActivity.onRequest(request);
			 }
		}
		//aurora change zhouxiaobing 20140716  end	


    }

    public void setState(DataKind kind, EntityDelta state, boolean readOnly, ViewIdGenerator vig) {
        mKind = kind;
        mState = state;
        mReadOnly = readOnly;
        mViewIdGenerator = vig;

        setId(mViewIdGenerator.getId(state, kind, null, ViewIdGenerator.NO_VIEW_INDEX));

        // TODO: handle resources from remote packages
        mTitleString = (kind.titleRes == -1 || kind.titleRes == 0)
                ? ""
                : getResources().getString(kind.titleRes);
        mTitle.setText(mTitleString);
        
        /*if (ContactsApplication.sIsGnContactsSupport) {
            mTitle.setTextColor(ResConstant.sHeaderTextColor);
        }*/
        
        rebuildFromState();
        updateAddFooterVisible();
        updateSectionVisible();
    }

    public String getTitle() {
        return mTitleString;
    }

    public void setTitleVisible(boolean visible) {
		// aurora <ukiliu> <2013-9-17> modify for auroro ui begin
        findViewById(R.id.kind_title_layout).setVisibility(/*visible ? View.VISIBLE :*/ View.GONE);
		// aurora <ukiliu> <2013-9-17> modify for auroro ui end
    }

    /**
     * Build editors for all current {@link #mState} rows.
     */
    public void rebuildFromState() {
        // Remove any existing editors
        mEditors.removeAllViews();

        // Check if we are displaying anything here
        boolean hasEntries = mState.hasMimeEntries(mKind.mimeType);

        if (hasEntries) {
            for (ValuesDelta entry : mState.getMimeEntries(mKind.mimeType)) {
                // Skip entries that aren't visible
                //if (!entry.beforeExists() && !ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mKind.mimeType)) continue;
                if (!entry.isVisible()) continue;
                if (isEmptyNoop(entry)) continue;
                if(!entry.beforeExists() && !entry.isInsert() && !ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mKind.mimeType)) continue;
//                if (!entry.beforeExists() && !ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mKind.mimeType)) continue;

                createEditorView(entry);
            }
        }
    }


    /**
     * Creates an EditorView for the given entry. This function must be used while constructing
     * the views corresponding to the the object-model. The resulting EditorView is also added
     * to the end of mEditors
     */
    private View createEditorView(ValuesDelta entry) {
        final View view;
        try {
            view = mInflater.inflate(mKind.editorLayoutResourceId, mEditors, false);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot allocate editor with layout resource ID " +
                    mKind.editorLayoutResourceId + " for MIME type " + mKind.mimeType +
                    " with error " + e.toString());
        }

        view.setEnabled(isEnabled());

        if (view instanceof Editor) {
            Editor editor = (Editor) view;
            editor.setDeletable(true);
            editor.setValues(mKind, entry, mState, mReadOnly, mViewIdGenerator);
            editor.setEditorListener(this);
        }
        // aurora ukiliu 2013-10-16 modify for aurora ui begin
        if (ContactsApplication.auroraContactsSupport) {
        	int count = mEditors.getChildCount();
        	if (count > 0) {
        		mEditors.getChildAt(count - 1).setBackgroundResource(
        				R.drawable.gn_horizontal_divider);  
        	}
        }
        // aurora ukiliu 2013-10-16 modify for aurora ui end
        
        mEditors.addView(view);
        
        return view;
    }

    /**
     * Tests whether the given item has no changes (so it exists in the database) but is empty
     */
    private boolean isEmptyNoop(ValuesDelta item) {
        if (!item.isNoop()) return false;
        final int fieldCount = mKind.fieldList.size();
        for (int i = 0; i < fieldCount; i++) {
            final String column = mKind.fieldList.get(i).column;
            final String value = item.getAsString(column);
            if (!TextUtils.isEmpty(value)) return false;
        }
        return true;
    }

    private void updateSectionVisible() {
        setVisibility(getEditorCount() != 0 ? VISIBLE : GONE);
    }

    protected void updateAddFooterVisible() {
        if (!mReadOnly && (mKind.typeOverallMax != 1)) {
            // First determine whether there are any existing empty editors.
            updateEmptyEditors();
            // aurora <ukiliu> <2013-9-17> modify for auroro ui begin
            // If there are no existing empty editors and it's possible to add
            // another field, then make the "add footer" field visible.
            if (!noShow() && !hasEmptyEditor() && EntityModifier.canInsert(mState, mKind)) {
//                mAddFieldFooter.setVisibility(View.VISIBLE);
                addItem();
                return;
            }
        }
        //mAddFieldFooter.setVisibility(View.GONE);
		// aurora <ukiliu> <2013-9-17> modify for auroro ui end
    }
    // aurora <ukiliu> <2013-9-17> add for auroro ui begin
    boolean noShow() {
    	
    	return getEditorCount() == 0;
    }
	// aurora <ukiliu> <2013-9-17> add for auroro ui end
    /**
     * Updates the editors being displayed to the user removing extra empty
     * {@link Editor}s, so there is only max 1 empty {@link Editor} view at a time.
     */
    private void updateEmptyEditors() {
        List<View> emptyEditors = getEmptyEditors();
		// aurora <ukiliu> <2013-10-17> add for aurora ui begin
        int count = getEditorCount();

        // If there is more than 1 empty editor, then remove it from the list of editors.
        if (emptyEditors.size() > 1) {
            for (View emptyEditorView : emptyEditors) {
                // If no child {@link View}s are being focused on within
                // this {@link View}, then remove this empty editor.
                if (emptyEditorView.findFocus() == null) {
                    if (null == mEditors) {
                        return;
                    }
                    
                    mEditors.removeView(emptyEditorView);
                }
            }
        }
        // aurora <ukiliu> <2013-10-17> modify for aurora ui end
    }

    /**
     * Returns a list of empty editor views in this section.
     */
    private List<View> getEmptyEditors() {
        List<View> emptyEditorViews = new ArrayList<View>();
        for (int i = 0; i < mEditors.getChildCount(); i++) {
            View view = mEditors.getChildAt(i);
            if (((Editor) view).isEmpty()) {
                emptyEditorViews.add(view);
            }
        }
        return emptyEditorViews;
    }

    /**
     * Returns true if one of the editors has all of its fields empty, or false
     * otherwise.
     */
    private boolean hasEmptyEditor() {
        return getEmptyEditors().size() > 0;
    }

    /**
     * Returns true if all editors are empty.
     */
    public boolean isEmpty() {
        for (int i = 0; i < mEditors.getChildCount(); i++) {
            View view = mEditors.getChildAt(i);
            if (!((Editor) view).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public void addItem() {
    	addItem(false);
    }
    
    public void addItemRequestFocus() {
    	addItem(true);
    }

    public void addItem(boolean focus) {
        ValuesDelta values = null;
        // If this is a list, we can freely add. If not, only allow adding the first.
        if (mKind.typeOverallMax == 1) {
            if (getEditorCount() == 1) {
                return;
            }

            // If we already have an item, just make it visible
            ArrayList<ValuesDelta> entries = mState.getMimeEntries(mKind.mimeType);
            if (entries != null && entries.size() > 0) {
                values = entries.get(0);
            }
        }

        // Insert a new child, create its view and set its focus
        if (values == null) {
            values = EntityModifier.insertChild(mState, mKind);
        }

        final View newField = createEditorView(values);
		// aurora <ukiliu> <2013-9-17> remove for auroro ui begin
        if (Event.CONTENT_ITEM_TYPE.equals(mKind.mimeType) && getEditorCount() == 1) {
            post(new Runnable() {
        		
	            @Override
	            public void run() {
	            	if (newField instanceof EventFieldEditorView) {
	            		((EventFieldEditorView) newField).requestFocusForFirstEditField();
	                }
	            }
	        });
        } else if (focus) {
	        post(new Runnable() {
	
	            @Override
	            public void run() {
	                newField.requestFocus();
	                InputMethodManager imm = (InputMethodManager)newField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	                	
	            }
	        });
        }

        // Hide the "add field" footer because there is now a blank field.
//        mAddFieldFooter.setVisibility(View.GONE);
		// aurora <ukiliu> <2013-9-17> remove for auroro ui end

        // Ensure we are visible
        updateSectionVisible();
    }

    public int getEditorCount() {
        return mEditors.getChildCount();
    }

    public DataKind getKind() {
        return mKind;
    }

//aurora change zhouxiaobing 20140716  start	
   public interface EditListernerToActivity{
      public void onRequest(int request);
  }
  public EditListernerToActivity editToActivity;
  public void setOnEditListernerToActivity(EditListernerToActivity e)
  {
      editToActivity=e;
  }
//aurora change zhouxiaobing 20140716  end		
}
