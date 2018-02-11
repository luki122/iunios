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
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.model.AccountType.EditField;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.util.NameConverter;
import com.android.contacts.util.PhoneNumberFormatter;

import android.content.Context;
import android.content.Entity;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
// aurora <ukiliu> <2013-9-17> add for auroro ui begin
import android.widget.TextView;
// aurora <ukiliu> <2013-9-17> add for auroro ui end

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// The following lines are provided and maintained by Mediatek Inc.
import android.text.InputFilter;
import android.util.Log;
// The previous lines are provided and maintained by Mediatek Inc.

import aurora.widget.AuroraEditText;

/**
 * Simple editor that handles labels and any {@link EditField} defined for the
 * entry. Uses {@link ValuesDelta} to read any existing {@link Entity} values,
 * and to correctly write any changes values.
 */
public class TextFieldsEditorView extends LabeledEditorView {
    private AuroraEditText[] mFieldEditTexts = null;
    private ViewGroup mFields = null;
    private View mExpansionViewContainer;
    private ImageView mExpansionView;
    private boolean mHideOptional = true;
    private boolean mHasShortAndLongForms;
    private int mMinFieldHeight;
    private int mMinEditTextHeight;

    public TextFieldsEditorView(Context context) {
        super(context);
    }

    public TextFieldsEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextFieldsEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mMinFieldHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.editor_min_line_item_height);
        mMinEditTextHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.gn_editor_min_edit_text_height);
        mFields = (ViewGroup) findViewById(R.id.editors);
        mExpansionView = (ImageView) findViewById(R.id.expansion_view);
        mExpansionViewContainer = findViewById(R.id.expansion_view_container);
        mExpansionViewContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save focus
                final View focusedChild = getFocusedChild();
                final int focusedViewId = focusedChild == null ? -1 : focusedChild.getId();

                // Reconfigure GUI
                mHideOptional = !mHideOptional;
                onOptionalFieldVisibilityChange();
                rebuildValues();

                // Restore focus
                View newFocusView = findViewById(focusedViewId);
                if (newFocusView == null || newFocusView.getVisibility() == GONE) {
                    // find first visible child
                    newFocusView = TextFieldsEditorView.this;
                }
                newFocusView.requestFocus();
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (mFieldEditTexts != null) {
            for (int index = 0; index < mFieldEditTexts.length; index++) {
                mFieldEditTexts[index].setEnabled(!isReadOnly() && enabled);
            }
        }
        mExpansionView.setEnabled(!isReadOnly() && enabled);
    }

    /**
     * Creates or removes the type/label button. Doesn't do anything if already correctly configured
     */
    private void setupExpansionView(boolean shouldExist, boolean collapsed) {
        if (shouldExist) {
            mExpansionViewContainer.setVisibility(View.VISIBLE);
            // gionee xuhz 20120530 add for gn theme start
            if (ContactsApplication.sIsGnDarkStyle) {
                mExpansionView.setImageResource(collapsed
                        ? R.drawable.ic_menu_expander_minimized_holo_dark
                        : R.drawable.ic_menu_expander_maximized_holo_dark);
            } else {
                mExpansionView.setImageResource(collapsed
                        ? R.drawable.ic_menu_expander_minimized_holo_light
                        : R.drawable.ic_menu_expander_maximized_holo_light);
            }
            // gionee xuhz 20120530 add for gn theme end

        } else {
            mExpansionViewContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void requestFocusForFirstEditField() {
        if (mFieldEditTexts != null && mFieldEditTexts.length != 0) {
            AuroraEditText firstField = null;
            boolean anyFieldHasFocus = false;
            for (AuroraEditText editText : mFieldEditTexts) {
                if (firstField == null && editText.getVisibility() == View.VISIBLE) {
                    firstField = editText;
                }
                if (editText.hasFocus()) {
                    anyFieldHasFocus = true;
                    break;
                }
            }
            if (!anyFieldHasFocus && firstField != null) {
                firstField.requestFocus();
            }
        }
    }

    @Override
    public void setValues(DataKind kind, ValuesDelta entry, EntityDelta state, boolean readOnly,
            ViewIdGenerator vig) {
        super.setValues(kind, entry, state, readOnly, vig);
        // Remove edit texts that we currently have
        if (mFieldEditTexts != null) {
            for (AuroraEditText fieldEditText : mFieldEditTexts) {
                mFields.removeView(fieldEditText);
            }
        }
        boolean hidePossible = false;

        int fieldCount = kind.fieldList.size();
        mFieldEditTexts = new AuroraEditText[fieldCount];
        for (int index = 0; index < fieldCount; index++) {
            final EditField field = kind.fieldList.get(index);
            final AuroraEditText fieldView = new AuroraEditText(mContext);
        
            fieldView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            // Set either a minimum line requirement or a minimum height (because {@link TextView}
            // only takes one or the other at a single time).
          /*  if (field.minLines != 0) {
                fieldView.setMinLines(field.minLines);
            } else {
            }*/  
          //aurora change zhouxiaobing 20140310 for single line editor end      
			// aurora <ukiliu> <2013-9-17> modify for auroro ui begin
            fieldView.setTextAppearance(getContext(), R.style.editor_field_text_style);
            
            // gionee xuhz 20121208 add for GIUI2.0 start
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                switch (field.titleRes) {
                case R.string.full_name:
                	// aurora ukiliu 2013-10-16 add for aurora ui begin
                	fieldView.setIsNeedDeleteAll(true);
                	// aurora ukiliu 2013-10-16 add for aurora ui end
                case R.string.name_given:
                case R.string.name_family:
                case R.string.name_prefix:
                case R.string.name_middle:
                case R.string.name_suffix:
                case R.string.name_phonetic_given:
                case R.string.name_phonetic_middle:
                case R.string.name_phonetic_family:
                case R.string.name_phonetic:
                    //TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.editTextColor});
                    //ColorStateList color = typedArray.getColorStateList(0);
                    //fieldView.setTextColor(color);
                    //break;
                default:
                	break;
                }
            }
			// aurora <ukiliu> <2013-9-17> modify for auroro ui end
            // gionee xuhz 20121208 add for GIUI2.0 end

            fieldView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            mFieldEditTexts[index] = fieldView;
            fieldView.setId(vig.getId(state, kind, entry, index));
            if (field.titleRes > 0) {
                fieldView.setHint(field.titleRes);
            }
            int inputType = field.inputType;
            fieldView.setInputType(inputType);
            if (inputType == InputType.TYPE_CLASS_PHONE) {
                PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(mContext, fieldView);
            }

            // Show the "next" button in IME to navigate between text fields
            // TODO: Still need to properly navigate to/from sections without text fields,
            // See Bug: 5713510
            fieldView.setImeOptions(EditorInfo.IME_ACTION_NEXT);

            // Read current value from state
            final String column = field.column;
            final String value = entry.getAsString(column);
            
            
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     xxx
             *   CR ID: ALPS00244669
             *   Descriptions: 
             */
            Log.i(TAG,"setValues setFilter");
            fieldView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(FIELD_VIEW_MAX)});
            /*
             * Bug Fix by Mediatek End.
             */
            
            
            fieldView.setText(value);

            // Show the delete button if we have a non-null value
            setDeleteButtonVisible(value != null);

            // aurora ukiliu 2013-11-15 modify for aurora ui begin
            final int titleRes = field.titleRes;
            // Prepare listener for writing changes
            fieldView.addTextChangedListener(new TextWatcher() {
            	
//            	private int cou = 0;
//                int selectionEnd = 0;
//                int mMaxLenth = FIELD_VIEW_MAX;

                @Override
                public void afterTextChanged(Editable s) {
//                	if (cou > mMaxLenth) {
//                        selectionEnd = fieldView.getSelectionEnd();
//                        s.delete(mMaxLenth, selectionEnd);
//                        fieldView.setText(s.toString());
//                	}
                    onFieldChanged(column, s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                	int cusor = fieldView.getSelectionStart();
//                	cou = before + count;
//                	cusor = fieldView.getSelectionStart();
                	Log.i("liumxxx", "cusor::"+cusor);
                    String editable = fieldView.getText().toString();
                    String str = editable;
                    if (!editable.equals(str)) {
                    	fieldView.setText(str);
                    	try {
                    		fieldView.setSelection(cusor - 1);
                    	} catch (Exception e) {
                    		Log.i(TAG, "liumxxx:" + e.toString());
                    	}
                    }
//                    cou = fieldView.length();
                }
            });
            // aurora ukiliu 2013-11-15 modify for aurora ui end

            fieldView.setEnabled(isEnabled() && !readOnly);

            if (field.shortForm) {
                hidePossible = true;
                mHasShortAndLongForms = true;
                fieldView.setVisibility(mHideOptional ? View.VISIBLE : View.GONE);
            } else if (field.longForm) {
                hidePossible = true;
                mHasShortAndLongForms = true;
                fieldView.setVisibility(mHideOptional ? View.GONE : View.VISIBLE);
            } else {
                // Hide field when empty and optional value
                final boolean couldHide = (!ContactsUtils.isGraphic(value) && field.optional);
                final boolean willHide = (mHideOptional && couldHide);
                fieldView.setVisibility(willHide ? View.GONE : View.VISIBLE);
                hidePossible = hidePossible || couldHide;
            }
            
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
				// aurora <ukiliu> <2013-9-17> modify for auroro ui begin
            	//if (kind.earseEditBg) {
            		fieldView.setBackgroundDrawable(null);	
            	//}
				// aurora <ukiliu> <2013-9-17> modify for auroro ui end
            	if (kind.hintRes > 0) {
            		fieldView.setHint(kind.hintRes);
            	}
            }
            fieldView.setSingleLine(true);//aurora change zhouxiaobing 20140310 for single line editor  
            mFields.addView(fieldView);
        }
        // When hiding fields, place expandable
        setupExpansionView(hidePossible, mHideOptional);
        mExpansionView.setEnabled(!readOnly && isEnabled());
        
        getLabel().setPromptId(kind.titleRes);

    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < mFields.getChildCount(); i++) {
            AuroraEditText editText = (AuroraEditText) mFields.getChildAt(i);
            if (!TextUtils.isEmpty(editText.getText())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the editor is currently configured to show optional fields.
     */
    public boolean areOptionalFieldsVisible() {
        return !mHideOptional;
    }

    public boolean hasShortAndLongForms() {
        return mHasShortAndLongForms;
    }

    /**
     * Populates the bound rectangle with the bounds of the last editor field inside this view.
     */
    public void acquireEditorBounds(Rect bounds) {
        if (mFieldEditTexts != null) {
            for (int i = mFieldEditTexts.length; --i >= 0;) {
                AuroraEditText editText = mFieldEditTexts[i];
                if (editText.getVisibility() == View.VISIBLE) {
                    bounds.set(editText.getLeft(), editText.getTop(), editText.getRight(),
                            editText.getBottom());
                    return;
                }
            }
        }
    }

    /**
     * Saves the visibility of the child EditTexts, and mHideOptional.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mHideOptional = mHideOptional;

        final int numChildren = mFieldEditTexts == null ? 0 : mFieldEditTexts.length;
        ss.mVisibilities = new int[numChildren];
        for (int i = 0; i < numChildren; i++) {
            ss.mVisibilities[i] = mFieldEditTexts[i].getVisibility();
        }

        return ss;
    }

    /**
     * Restores the visibility of the child EditTexts, and mHideOptional.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        // The following lines are provided and maintained by Mediatek inc.
        mHideOptional = ss.mHideOptional;
        if (mFieldEditTexts != null) {
            int numChildren = Math.min(mFieldEditTexts.length, ss.mVisibilities.length);
            for (int i = 0; i < numChildren; i++) {
                mFieldEditTexts[i].setVisibility(ss.mVisibilities[i]);
            }
        }
        // The following lines are provided and maintained by Mediatek inc.
        
    }

    private static class SavedState extends BaseSavedState {
        public boolean mHideOptional;
        public int[] mVisibilities;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mVisibilities = new int[in.readInt()];
            in.readIntArray(mVisibilities);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mVisibilities.length);
            out.writeIntArray(mVisibilities);
        }

        @SuppressWarnings({"unused", "hiding" })
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public void clearAllFields() {
        if (mFieldEditTexts != null) {
            for (AuroraEditText fieldEditText : mFieldEditTexts) {
                // Update UI (which will trigger a state change through the {@link TextWatcher})
                fieldEditText.setText("");
            }
        }
        
        //Gionee:huangzy 20120731 add for CR00617624 start        
        setDeleteButtonVisible(false);
        //Gionee:huangzy 20120731 add for CR00617624 end
    }
    
    
    private static final int FIELD_VIEW_MAX = 40;
    private static final String TAG = "TextFieldsEditorView";
    
}
