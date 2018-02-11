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
import com.android.contacts.datepicker.DatePicker;
import com.android.contacts.model.AccountType.EditField;
import com.android.contacts.model.AccountType.EventEditType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.util.DateUtils;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraDatePicker;
import aurora.app.AuroraDatePickerDialog;

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Editor that allows editing Events using a {@link DatePickerDialog}
 */
public class EventFieldEditorView extends LabeledEditorView {
    /**
     * Exchange requires 8:00 for birthdays
     */
    private final static int DEFAULT_HOUR = 8;

    /**
     * Default string to show when there is no date selected yet.
     */
    private String mNoDateString;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;

    private AuroraButton mDateView;

    public EventFieldEditorView(Context context) {
        super(context);
    }

    public EventFieldEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EventFieldEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Resources resources = mContext.getResources();
        mPrimaryTextColor = resources.getColor(R.color.primary_text_color);
        mSecondaryTextColor = resources.getColor(R.color.secondary_text_color);
        mNoDateString = mContext.getString(R.string.event_edit_field_hint_text);

        //aurora ukiliu 2014-02-18 add for aurora ui begin
        findViewById(R.id.type_vertical_divider).setVisibility(GONE);
        //aurora ukiliu 2014-02-18 add for aurora ui end
        mDateView = (AuroraButton) findViewById(R.id.date_view);

        // gionee xuhz 20121123 add for GIUI2.0 start
//        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            mDateView.setTextAppearance(getContext(), R.style.editor_field_text_style);
//        }
        // gionee xuhz 20121123 add for GIUI2.0 end
//        mDateView.setOnClickListener(new OnClickListener() {
        mDateView.setClickable(false);
        findViewById(R.id.gn_edit_date_picker_pan).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	// aurora wangth 20140813 modify for 7603 begin
//                showDialog(R.id.dialog_event_date_picker);
            	try {
            		createDatePickerDialog().show();
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	// aurora wangth 20140813 modify for 7603 end
            }
        });
    }

    @Override
    protected void requestFocusForFirstEditField() {
        mDateView.requestFocus();
        createDatePickerDialog().show();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mDateView.setEnabled(!isReadOnly() && enabled);
    }

    @Override
    public void setValues(DataKind kind, ValuesDelta entry, EntityDelta state, boolean readOnly,
            ViewIdGenerator vig) {
        if (kind.fieldList.size() != 2) throw new IllegalStateException("kind must have 2 field");
        super.setValues(kind, entry, state, readOnly, vig);

        mDateView.setEnabled(isEnabled() && !readOnly);
        // aurora ukiliu 2013-10-22 add for BUG #123 begin
        getLabel().setVisibility(VISIBLE);
        // aurora ukiliu 2013-10-22 add for BUG #123 end
        rebuildDateView();
    }

    private void rebuildDateView() {
        final EditField editField = getKind().fieldList.get(0);
        final String column = editField.column;
        String data = DateUtils.formatDate(getContext(), getEntry().getAsString(column));
        if (TextUtils.isEmpty(data)) {            
            if (!ContactsApplication.sIsGnContactsSupport) {
            	mDateView.setText(mNoDateString);
            	mDateView.setTextColor(mSecondaryTextColor);
            }
            setDeleteButtonVisible(false);
        } else {
            mDateView.setText(data);
            if (!ContactsApplication.sIsGnContactsSupport) {
            	mDateView.setTextColor(mPrimaryTextColor);
            }
            setDeleteButtonVisible(true);
        }
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(mDateView.getText());
    }

    @Override
    public Dialog createDialog(Bundle bundle) {
        if (bundle == null) throw new IllegalArgumentException("bundle must not be null");
        int dialogId = bundle.getInt(DIALOG_ID_KEY);
        switch (dialogId) {
            case R.id.dialog_event_date_picker:
                return createDatePickerDialog();
            default:
                return super.createDialog(bundle);
        }
    }

    @Override
    protected EventEditType getType() {
        return (EventEditType) super.getType();
    }

    @Override
    protected void onLabelRebuilt() {
        // if we changed to a type that requires a year, ensure that it is actually set
        final String column = getKind().fieldList.get(0).column;
        final String oldValue = getEntry().getAsString(column);
        final DataKind kind = getKind();

        final Calendar calendar = Calendar.getInstance(DateUtils.UTC_TIMEZONE, Locale.US);
        final int defaultYear = calendar.get(Calendar.YEAR);

        // Check whether the year is optional
        final boolean isYearOptional = getType().isYearOptional();

        if (!isYearOptional && !TextUtils.isEmpty(oldValue)) {
        	if(kind.dateFormatWithoutYear == null) {
        		return;
        	}
            final ParsePosition position = new ParsePosition(0);
            final Date date2 = kind.dateFormatWithoutYear.parse(oldValue, position);

            // Don't understand the date, lets not change it
            if (date2 == null) return;

            // This value is missing the year. Add it now
            calendar.setTime(date2);
            calendar.set(defaultYear, calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH), DEFAULT_HOUR, 0, 0);

            // gionee xuhz 20121123 add for GIUI2.0 start
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            	mDateView.setText(kind.dateFormatWithYear.format(calendar.getTime()));
            }
            // gionee xuhz 20121123 add for GIUI2.0 end
            onFieldChanged(column, kind.dateFormatWithYear.format(calendar.getTime()));
            rebuildDateView();
        }
    }

    /**
     * Prepare dialog for entering a date
     */
    private Dialog createDatePickerDialog() {
        final String column = getKind().fieldList.get(0).column;
        final String oldValue = getEntry().getAsString(column);
        final DataKind kind = getKind();

        final Calendar calendar = Calendar.getInstance(DateUtils.UTC_TIMEZONE, Locale.US);
        final int defaultYear = calendar.get(Calendar.YEAR);

        // Check whether the year is optional
        final boolean isYearOptional = getType().isYearOptional();

        final int oldYear, oldMonth, oldDay;
        if (TextUtils.isEmpty(oldValue)) {
            // Default to January first, 30 years ago
            oldYear = defaultYear;
            oldMonth = 0;
            oldDay = 1;
        } else {
            final ParsePosition position = new ParsePosition(0);
            // Try parsing with year
            Date date1 = kind.dateFormatWithYear.parse(oldValue, position);
            if (date1 == null) {
                // If that format does not fit, try guessing the right format
                date1 = DateUtils.parseDate(oldValue);
            }
            if (date1 != null) {
                calendar.setTime(date1);
                oldYear = calendar.get(Calendar.YEAR);
                oldMonth = calendar.get(Calendar.MONTH);
                oldDay = calendar.get(Calendar.DAY_OF_MONTH);
            } else {
                final Date date2 = kind.dateFormatWithoutYear.parse(oldValue, position);
                // Don't understand the date, lets not change it
                if (date2 == null) return null;
                calendar.setTime(date2);
                oldYear = isYearOptional ? 0 : defaultYear;
                oldMonth = calendar.get(Calendar.MONTH);
                oldDay = calendar.get(Calendar.DAY_OF_MONTH);
            }
        }
        
        // aurora ukiliu 2013-10-10 modify aurora-ui begin
        final aurora.app.AuroraDatePickerDialog.OnDateSetListener callBack = new aurora.app.AuroraDatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(AuroraDatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (year == 0 && !isYearOptional) throw new IllegalStateException();
                final Calendar outCalendar =
                        Calendar.getInstance(DateUtils.UTC_TIMEZONE, Locale.US);
                // If no year specified, set it to 1900. The format string will ignore that year
                // For formats other than Exchange, the time of the day is ignored
                outCalendar.clear();
                outCalendar.set(year == 0 ? 1900 : year, monthOfYear, dayOfMonth,
                        DEFAULT_HOUR, 0, 0);

                final String resultString;
                if (year == 0) {
                    resultString = kind.dateFormatWithoutYear.format(outCalendar.getTime());
                } else {
                    resultString = kind.dateFormatWithYear.format(outCalendar.getTime());
                }
                // gionee xuhz 20121123 add for GIUI2.0 start
                if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                    mDateView.setText(resultString);
                }
                // gionee xuhz 20121123 add for GIUI2.0 end
                onFieldChanged(column, resultString);
                rebuildDateView();
            }
        };
        // gionee xuhz 20120528 add for support gn theme start
        AuroraDatePickerDialog resultDialog = null;
        if (ContactsApplication.auroraContactsSupport) {
        	int theme = -1;
        	//Gionee <huangzy> <2013-05-13> modify for CR00808068 begin
        	theme = AuroraAlertDialog.THEME_AMIGO_FULLSCREEN;
        	//Gionee <huangzy> <2013-05-13> modify for CR00808068 end
        	
//            if (ContactsApplication.sIsGnTransparentTheme) {
//            	resultDialog = new DatePickerDialog(getContext(), theme,
//            			callBack, oldYear, oldMonth, oldDay, isYearOptional);
//            	resultDialog = new AuroraDatePickerDialog(getContext(), callBack,
//                         oldYear, oldMonth, oldDay);
//            } 
//            else if (ContactsApplication.sIsGnDarkTheme) {
//            	resultDialog = new DatePickerDialog(getContext(), theme,
//                        callBack, oldYear, oldMonth, oldDay, isYearOptional);
//            } 
//            else if (ContactsApplication.sIsGnLightTheme) {
//            	resultDialog = new DatePickerDialog(getContext(), theme,
//            			callBack, oldYear, oldMonth, oldDay, isYearOptional);
            	resultDialog = new AuroraDatePickerDialog(getContext(), callBack,
            			oldYear, oldMonth, oldDay);
//            }
        } else {
//            resultDialog = new DatePickerDialog(getContext(), callBack,
//                    oldYear, oldMonth, oldDay, isYearOptional);
            resultDialog = new AuroraDatePickerDialog(getContext(), callBack,
        			oldYear, oldMonth, oldDay);
        }
        // aurora ukiliu 2013-10-10 modify aurora-ui end
        // gionee xuhz 20120528 add for support gn theme end
        return resultDialog;
    }

    /**
     * @return Default hour which should be used for birthday field.
     */
    public static int getDefaultHourForBirthday() {
        return DEFAULT_HOUR;
    }

    @Override
    public void clearAllFields() {
        // Update UI
        if (!ContactsApplication.sIsGnContactsSupport) {
        	mDateView.setText(mNoDateString);
        	mDateView.setTextColor(mSecondaryTextColor);
        }

        // Update state
        // gionee xuhz 20121123 add for GIUI2.0 start
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            mDateView.setText("");
            setDeleteButtonVisible(false);
        }
        // gionee xuhz 20121123 add for GIUI2.0 end
        
        final String column = getKind().fieldList.get(0).column;
        onFieldChanged(column, "");
    }
}
