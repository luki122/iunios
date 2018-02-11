
package aurora.lib.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import com.aurora.lib.R;

public class AuroraTimePicker extends FrameLayout {

    private static final boolean DEFAULT_ENABLED_STATE = true;

    private static final int HOURS_IN_HALF_DAY = 12;

    /**
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(AuroraTimePicker view, int hourOfDay, int minute) {
        }
    };

    // state
    private boolean mIs24HourView;

    private boolean mIsAm;

    // ui components
    private final AuroraNumberPicker mHourSpinner;

    private final AuroraNumberPicker mMinuteSpinner;

    private final AuroraNumberPicker mAmPmSpinner;

    private final EditText mHourSpinnerInput;

    private final EditText mMinuteSpinnerInput;

    private final EditText mAmPmSpinnerInput;

    private final TextView mDivider = null;

    // Note that the legacy implementation of the TimePicker is
    // using a button for toggling between AM/PM while the new
    // version uses a NumberPicker spinner. Therefore the code
    // accommodates these two cases to be backwards compatible.
    private final Button mAmPmButton;

    private final String[] mAmPmStrings;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;
    private boolean mHideApPm;

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;

    private Context mContext;
    
    
    private LinearLayout.LayoutParams mLayoutParams;

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(AuroraTimePicker view, int hourOfDay, int minute);
    }

    public AuroraTimePicker(Context context) {
        this(context, null);
    }

    public AuroraTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.auroratimePickerStyle);
    }

    public AuroraTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        // process style attributes
        TypedArray attributesArray = context.obtainStyledAttributes(
                attrs, com.aurora.lib.R.styleable.AuroraTimePicker, defStyle, 0);
        int layoutResourceId = attributesArray.getResourceId(
                com.aurora.lib.R.styleable.AuroraTimePicker_aurorainternalLayout,
                R.layout.aurora_time_picker);
        mHideApPm = attributesArray.getBoolean(
                com.aurora.lib.R.styleable.AuroraTimePicker_hideAmPm, false);
        mIs24HourView = attributesArray.getBoolean(
                com.aurora.lib.R.styleable.AuroraTimePicker_is24Hour, true);
        attributesArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResourceId, this, true);

        // hour
        mHourSpinner = (AuroraNumberPicker) findViewById(com.aurora.lib.R.id.aurora_hour);
        mHourSpinner.setLabel(getResources().getString(
                com.aurora.lib.R.string.aurora_time_picker_hour_lebel));
        mHourSpinner.setSelectionSrc(getResources().getDrawable(
                com.aurora.lib.R.drawable.aurora_numberpicker_selector));
        mHourSpinner.setOnValueChangedListener(new AuroraNumberPicker.OnValueChangeListener() {
            public void onValueChange(AuroraNumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                if (!is24HourView()) {
                    if ((oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY)
                            || (oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1)) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                }
                onTimeChanged();
            }
        });
        mHourSpinnerInput = (EditText) mHourSpinner
                .findViewById(com.aurora.lib.R.id.aurora_numberpicker_input);
        mHourSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // divider (only for the new widget style)
        /*
         * mDivider = (TextView)
         * findViewById(com.aurora.lib.R.id.aurora_divider); if (mDivider
         * != null) { mDivider.setText(R.string.time_picker_separator); }
         */

        // minute
        mMinuteSpinner = (AuroraNumberPicker) findViewById(com.aurora.lib.R.id.aurora_minute);
        mMinuteSpinner.setMinValue(0);
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setLabel(getResources().getString(
                com.aurora.lib.R.string.aurora_time_picker_m_label));
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setSelectionSrc(getResources().getDrawable(
                com.aurora.lib.R.drawable.aurora_numberpicker_selector));
        mMinuteSpinner.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
        mMinuteSpinner.setOnValueChangedListener(new AuroraNumberPicker.OnValueChangeListener() {
            public void onValueChange(AuroraNumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                int minValue = mMinuteSpinner.getMinValue();
                int maxValue = mMinuteSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mHourSpinner.getValue() + 1;
                    if (!is24HourView() && newHour == HOURS_IN_HALF_DAY) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mHourSpinner.getValue() - 1;
                    if (!is24HourView() && newHour == HOURS_IN_HALF_DAY - 1) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setValue(newHour);
                }
                onTimeChanged();
            }
        });

        mMinuteSpinnerInput = (EditText) mMinuteSpinner
                .findViewById(com.aurora.lib.R.id.aurora_numberpicker_input);
        mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        /* Get the localized am/pm strings and use them in the spinner */
        mAmPmStrings = new DateFormatSymbols().getAmPmStrings();

        // am/pm
        View amPmView = findViewById(com.aurora.lib.R.id.aurora_amPm);
        if (amPmView instanceof Button) {
            mAmPmSpinner = null;
            mAmPmSpinnerInput = null;
            mAmPmButton = (Button) amPmView;
            mAmPmButton.setOnClickListener(new OnClickListener() {
                public void onClick(View button) {
                    button.requestFocus();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged();
                }
            });
        } else {
            mAmPmButton = null;
            mAmPmSpinner = (AuroraNumberPicker) amPmView;
            mAmPmSpinner.setMinValue(0);
            mAmPmSpinner.setMaxValue(1);
            mAmPmSpinner.setDisplayedValues(mAmPmStrings);
            mAmPmSpinner.setSelectionSrc(getResources().getDrawable(
                    com.aurora.lib.R.drawable.aurora_numberpicker_selector));
            mAmPmSpinner.setOnValueChangedListener(new AuroraNumberPicker.OnValueChangeListener() {
                public void onValueChange(AuroraNumberPicker picker, int oldVal, int newVal) {
                    updateInputState();
                    picker.requestFocus();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged();
                }
            });
            mAmPmSpinnerInput = (EditText) mAmPmSpinner
                    .findViewById(com.aurora.lib.R.id.aurora_numberpicker_input);
            mAmPmSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
            
            mLayoutParams = (android.widget.LinearLayout.LayoutParams) mMinuteSpinner.getLayoutParams();
        }

        // update controls to initial state
        updateHourControl();
        updateAmPmControl();

        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);

        // set to current time
        setCurrentHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));

        setIs24HourView(mIs24HourView);
        if (!isEnabled()) {
            setEnabled(false);
        }

        // set the content descriptions
        setContentDescriptions();

        // If not explicitly specified this view is important for accessibility.
        /*
         * if (getImportantForAccessibility() ==
         * IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
         * setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES); }
         */
    }

    public void setWidgetGapWidth(float width){
        setGapWidth(width);
    }
    
    private void setGapWidth(float width){
        if(mLayoutParams == null){
            return;
        }
        
        mLayoutParams.leftMargin = (int)width;
        
        if(mMinuteSpinner != null){
            mMinuteSpinner.setLayoutParams(mLayoutParams);
        }
        
        if(mAmPmSpinner != null){
            mLayoutParams = (android.widget.LinearLayout.LayoutParams) mAmPmSpinner.getLayoutParams();
        }
        
        if(mLayoutParams != null){
            mLayoutParams.leftMargin = (int)width;
            mAmPmSpinner.setLayoutParams(mLayoutParams);
        }
    }
    
    public void setWidgetWidth(float width){
        mLayoutParams = (android.widget.LinearLayout.LayoutParams) mHourSpinner.getLayoutParams();
        if(mLayoutParams != null){
            mLayoutParams.width = (int)width;
            mHourSpinner.setLayoutParams(mLayoutParams);
        }
        
        mLayoutParams = (android.widget.LinearLayout.LayoutParams) mMinuteSpinner.getLayoutParams();
        if(mLayoutParams != null){
            mLayoutParams.width = (int)width;
            mMinuteSpinner.setLayoutParams(mLayoutParams);
        }
    }
    public void setTextSize(float size) {
        if (mHourSpinner != null) {
            mHourSpinner.setTextSize(size);
        }
        if (mMinuteSpinner != null) {
            mMinuteSpinner.setTextSize(size);
        }
        
        if(mAmPmSpinner != null){
            mAmPmSpinner.setTextSize(size);
        }

    }
    
    

    public void setOtherItemTextSize(float size) {
        if (mHourSpinner != null) {
            mHourSpinner.setOtherItemTextSize(size, size);
        }
        if (mMinuteSpinner != null) {
            mMinuteSpinner.setOtherItemTextSize(size, size);
        }
        if(mAmPmSpinner != null){
            mAmPmSpinner.setOtherItemTextSize(size,size);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mMinuteSpinner.setEnabled(enabled);
        if (mDivider != null) {
            mDivider.setEnabled(enabled);
        }
        mHourSpinner.setEnabled(enabled);
        if (mAmPmSpinner != null) {
            mAmPmSpinner.setEnabled(enabled);
        } else {
            mAmPmButton.setEnabled(enabled);
        }
        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setTextColor(int colorOne, int colorSelected, int colorTwo) {
        if (mAmPmSpinner != null) {
            mAmPmSpinner.setTextColor(colorOne, colorSelected, colorTwo);
        }

        if (mMinuteSpinner != null) {
            mMinuteSpinner.setTextColor(colorOne, colorSelected, colorTwo);
        }

        if (mHourSpinner != null) {
            mHourSpinner.setTextColor(colorOne, colorSelected, colorTwo);
        }

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    public void hideAmPm(boolean hide) {
        mHideApPm = hide;
    }

    /**
     * Sets the current locale.
     * 
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        mTempCalendar = Calendar.getInstance(locale);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;

        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            mHour = hour;
            mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        @SuppressWarnings({
                "unused", "hiding"
        })
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentHour(), getCurrentMinute());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * 
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        int currentHour = mHourSpinner.getValue();
        if (is24HourView()) {
            
            return currentHour;
        } else if (mIsAm) {
            return currentHour % HOURS_IN_HALF_DAY;
        } else {
            return (currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
        }
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        // why was Integer used in the first place?
        if (currentHour == null || currentHour == getCurrentHour()) {
            return;
        }
        if (!is24HourView()) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour >= HOURS_IN_HALF_DAY) {
                mIsAm = false;
                if (currentHour > HOURS_IN_HALF_DAY) {
                    currentHour = currentHour - HOURS_IN_HALF_DAY;
                }
            } else {
                mIsAm = true;
                if (currentHour == 0) {
                    currentHour = HOURS_IN_HALF_DAY;
                }
            }
            updateAmPmControl();
        }
        mHourSpinner.setValue(currentHour);
        onTimeChanged();
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     * 
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView == is24HourView) {
            return;
        }
        mIs24HourView = is24HourView;
        // cache the current hour since spinner range changes
        int currentHour = getCurrentHour();
        updateHourControl();
        // set value after spinner range is updated
        setCurrentHour(currentHour);
        updateAmPmControl();
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mMinuteSpinner.getValue();
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == getCurrentMinute()) {
            return;
        }
        mMinuteSpinner.setValue(currentMinute);
        onTimeChanged();
    }

    @Override
    public int getBaseline() {
        return mHourSpinner.getBaseline();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

        int flags = DateUtils.FORMAT_SHOW_TIME;
        if (mIs24HourView) {
            flags |= DateUtils.FORMAT_24HOUR;
        } else {
            flags |= DateUtils.FORMAT_12HOUR;
        }
        mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
        mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
        String selectedDateUtterance = DateUtils.formatDateTime(mContext,
                mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AuroraTimePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AuroraTimePicker.class.getName());
    }

    private void updateHourControl() {
        if (is24HourView()) {
            mHourSpinner.setMinValue(0);
            mHourSpinner.setMaxValue(23);
            mHourSpinner.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
        } else {
            mHourSpinner.setMinValue(1);
            mHourSpinner.setMaxValue(12);
            mHourSpinner.setFormatter(null);
        }
    }

    private void updateAmPmControl() {
        if (is24HourView()) {
            if (mAmPmSpinner != null) {
                mAmPmSpinner.setVisibility(View.GONE);
            } else {
                mAmPmButton.setVisibility(View.GONE);
            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 begin
            if (mMinuteSpinner != null) {
                // mMinuteSpinner.setBackgroundDrawable(getResources().getDrawable(R.drawable.aurora_numberpicker_right));
                mMinuteSpinner.setSelectionSrc(getResources().getDrawable(
                        com.aurora.lib.R.drawable.aurora_numberpicker_selector));
            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 end
        } else {
            int index = mIsAm ? Calendar.AM : Calendar.PM;
            if (mAmPmSpinner != null && !mHideApPm) {
                mAmPmSpinner.setValue(index);
                mAmPmSpinner.setVisibility(View.VISIBLE);
            } else {
                if (!mHideApPm) {
                    mAmPmButton.setText(mAmPmStrings[index]);
                    mAmPmButton.setVisibility(View.VISIBLE);
                }

            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 begin
            if (mMinuteSpinner != null) {
                // mMinuteSpinner.setBackgroundDrawable(getResources().getDrawable(R.drawable.aurora_numberpicker_center));
                mMinuteSpinner.setSelectionSrc(getResources().getDrawable(
                        com.aurora.lib.R.drawable.aurora_numberpicker_selector));
            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 end
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());
        }
    }

    private void setContentDescriptions() {
        // Minute
        /*
         * trySetContentDescription(mMinuteSpinner,
         * com.aurora.lib.R.id.aurora_increment,
         * R.string.time_picker_increment_minute_button);
         * trySetContentDescription(mMinuteSpinner,
         * com.aurora.lib.R.id.aurora_decrement,
         * R.string.time_picker_decrement_minute_button); // Hour
         * trySetContentDescription(mHourSpinner,
         * com.aurora.lib.R.id.aurora_increment,
         * R.string.time_picker_increment_hour_button);
         * trySetContentDescription(mHourSpinner,
         * com.aurora.lib.R.id.aurora_decrement,
         * R.string.time_picker_decrement_hour_button); // AM/PM if
         * (mAmPmSpinner != null) { trySetContentDescription(mAmPmSpinner,
         * com.aurora.lib.R.id.aurora_increment,
         * R.string.time_picker_increment_set_pm_button);
         * trySetContentDescription(mAmPmSpinner,
         * com.aurora.lib.R.id.aurora_decrement,
         * R.string.time_picker_decrement_set_am_button); }
         */
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(mContext.getString(contDescResId));
        }
    }

    private void updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        InputMethodManager inputMethodManager = (InputMethodManager) (mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE));/*
                                                                  * InputMethodManager
                                                                  * .
                                                                  * peekInstance
                                                                  * ();
                                                                  */
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourSpinnerInput)) {
                mHourSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteSpinnerInput)) {
                mMinuteSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mAmPmSpinnerInput)) {
                mAmPmSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }
}
