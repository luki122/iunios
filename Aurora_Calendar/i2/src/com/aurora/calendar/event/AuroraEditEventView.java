package com.aurora.calendar.event;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.AlarmManager;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraDatePickerDialog;
import aurora.app.AuroraWeekPickerDialog;
import aurora.app.AuroraWeekPickerDialog.onWeekSetListener;
import android.app.PendingIntent;
import aurora.app.AuroraDatePickerDialog.OnDateSetListener;
import aurora.app.AuroraProgressDialog;
import android.app.Service;
import aurora.app.AuroraTimePickerDialog;
import aurora.app.AuroraTimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import aurora.widget.AuroraButton;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraNumberPicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ResourceCursorAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import aurora.widget.AuroraSpinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import aurora.widget.AuroraTimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.GNAnimationutils;
import com.gionee.calendar.view.GNDateTimeDialog;
import com.gionee.calendar.view.GNEditEventRelativelayout;
import com.gionee.calendar.view.GNDateTimeDialog.OnDateTimeSetListener;
import com.gionee.calendar.view.GNEditEventRelativelayout.OnResizeListener;
import com.mediatek.calendar.EditEventTimePickerDialogFragment;
import com.mediatek.calendar.LogUtil;
import com.mediatek.calendar.edittext.IEditTextExt;
import com.mediatek.calendar.extension.ExtensionFactory;
import com.mediatek.calendar.extension.IEditEventView;
import com.mediatek.calendar.extension.IEditEventViewExt;
import com.android.calendar.CalendarEventModel;
import com.android.calendar.CalendarEventModel.Attendee;
import com.android.calendar.CalendarEventModel.ReminderEntry;
import com.android.calendar.EmailAddressAdapter;
import com.android.calendar.EventInfoFragment;
import com.android.calendar.EventRecurrenceFormatter;
import com.android.calendar.GeneralPreferences;
import com.android.calendar.R;
import com.android.calendar.RecipientAdapter;
import com.android.calendar.TimezoneAdapter;
import com.android.calendar.TimezoneAdapter.TimezoneRow;
import com.android.calendar.Utils;
import com.android.calendar.event.AuroraEditEventActivity;
import com.android.calendar.event.AuroraEditEventFragment;
import com.android.calendar.event.EditEventActivity;
import com.android.calendar.event.EditEventFragment;
import com.android.calendar.event.EditEventHelper;
import com.android.calendar.event.EditEventHelper.EditDoneRunnable;
import com.android.calendar.event.EventViewUtils;
import com.android.calendarcommon2.EventRecurrence;
import com.android.common.Rfc822InputFilter;
import com.android.common.Rfc822Validator;
import com.android.mtkex.chips.AccountSpecifier;
import com.android.mtkex.chips.BaseRecipientAdapter;
import com.android.mtkex.chips.ChipsUtil;
//import com.android.ex.chips.RecipientEditTextView;
import com.mediatek.calendar.LogUtil;
import com.mediatek.calendar.edittext.IEditTextExt;
import com.mediatek.calendar.extension.ExtensionFactory;
import com.mediatek.calendar.extension.IEditEventView;
import com.mediatek.calendar.extension.IEditEventViewExt;
import com.mediatek.calendar.features.Features;
import com.mediatek.calendar.lunar.LunarDatePickerDialog;
import com.mediatek.calendar.lunar.LunarUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;

import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;

public class AuroraEditEventView implements View.OnClickListener,
		DialogInterface.OnCancelListener, DialogInterface.OnClickListener,
		OnItemSelectedListener, IEditEventView {
	private static final String TAG = "AuroraEditEventView";
	private static final String GOOGLE_SECONDARY_CALENDAR = "calendar.google.com";
	private static final String PERIOD_SPACE = ". ";

	private static final int SOFTWEARINPUTOUT = 1;
	private static final int SOFTWEARINPUTHIDE = 2;

	// Constants used for title autocompletion.
	private static final String[] EVENT_PROJECTION = new String[] { Events._ID,
			Events.TITLE, };
	private static final int EVENT_INDEX_ID = 0;
	private static final int EVENT_INDEX_TITLE = 1;
	private static final String TITLE_WHERE = Events.TITLE + " LIKE ?";
	private static final int MAX_TITLE_SUGGESTIONS = 4;

	private boolean isOpen = true;

	ArrayList<View> mEditOnlyList = new ArrayList<View>();
	ArrayList<View> mEditViewList = new ArrayList<View>();
	ArrayList<View> mViewOnlyList = new ArrayList<View>();
	TextView mLoadingMessage;
	ScrollView mScrollView;
	TextView mStartTimeText;
	TextView mEndTimeText;
	TextView mStartDateText;
	TextView mEndDateText;
	View mStartDataView;
	View mEndDataView;
	ImageView mReminderMe;
	AuroraSpinner mCalendarsSpinner;
	AuroraSpinner mRepeatsSpinner;
	TextView mRruleButton;
	AuroraEditText mTitleTextView;
	AuroraEditText descriptionEditText;
	TextView mDescriptionTextView;
	TextView mTimezoneTextView;
	View mRepeatView;
	LinearLayout editEventView2;
	LinearLayout mCalendarAccountAll;
	TextView mCalendarAccountName;
	MultiAutoCompleteTextView mAttendeesList;
	View mCalendarSelectorGroup;
	View mDescriptionGroup;
	View mAttendeesGroup;
	View mCalendarStaticGroup;
	int toolbarHeight;
	private boolean isClickVoiceButton;
	View mEditEvent;

	private int[] mOriginalPadding = new int[4];
	private int[] mOriginalSpinnerPadding = new int[4];

	private boolean mIsMultipane;
//	public static boolean mIsClickMoreButton;
	private AuroraProgressDialog mLoadingCalendarsDialog;
	private AuroraAlertDialog mNoCalendarsDialog;
	private Activity mActivity;
	private EditDoneRunnable mDone;
	private View mView;
	private CalendarEventModel mModel;
	private Cursor mCalendarsCursor;
	private AccountSpecifier mAddressAdapter;
	private Rfc822Validator mEmailValidator;

	private ArrayList<Integer> mRecurrenceIndexes = new ArrayList<Integer>(0);

	/**
	 * Contents of the "minutes" spinner. This has default values from the XML
	 * file, augmented with any additional values that were already associated
	 * with the event.
	 */
	private ArrayList<Integer> mReminderMinuteValues;
	private ArrayList<String> mReminderMinuteLabels;

	/**
	 * Contents of the "methods" spinner. The "values" list specifies the method
	 * constant (e.g. {@link Reminders#METHOD_ALERT}) associated with the
	 * labels. Any methods that aren't allowed by the Calendar will be removed.
	 */
	private ArrayList<Integer> mReminderMethodValues;
	private ArrayList<String> mReminderMethodLabels;

	private int mDefaultReminderMinutes;

	private boolean mSaveAfterQueryComplete = false;

	private Time mStartTime;
	private Time mEndTime;
	private String mTimezone;
	private int mModification = EditEventHelper.MODIFY_UNINITIALIZED;

	private EventRecurrence mEventRecurrence = new EventRecurrence();

	private ArrayList<LinearLayout> mReminderItems = new ArrayList<LinearLayout>(
			0);
	private ArrayList<ReminderEntry> mUnsupportedReminders = new ArrayList<ReminderEntry>();
	private String mRrule;

	private static StringBuilder mSB = new StringBuilder(50);
	private static Formatter mF = new Formatter(mSB, Locale.getDefault());

	private DialogManager mDialogManager = new DialogManager();

	private Toast mToast = null;

	// /M: keep AuroraDatePickerDialog and AuroraTimePickerDialog when rotate
	// device @{
	private static final String EDIT_EVENT_TIME_DIALOG_FRAG = "edit_event_time_dialog_frag";

	// /@}

	// Fills in the date and time fields
	private void populateWhen() {
		Log.i("jiating", "populateWhen....mStartTime=" + mStartTime.toString()
				+ "mEndTime=" + mEndTime.toString());
		final long startMillis = mStartTime.toMillis(false /* use isDst */);
		final long endMillis = mEndTime.toMillis(false /* use isDst */);
		setDate(mStartDateText, mStartTimeText, startMillis);
		setDate(mEndDateText, mEndTimeText, endMillis);
		mStartDataView.setOnClickListener(this);
		mEndDataView.setOnClickListener(this);
	}

	ArrayList<String> repeatArray;
	ArrayList<Integer> recurrenceIndexes;

	private void populateRepeats() {
		Time time = mStartTime;
		Resources r = mActivity.getResources();
		String repeatString;
		boolean enabled;
		
		if (!TextUtils.isEmpty(mRrule)) {
			repeatString = EventRecurrenceFormatter.getRepeatString(mActivity,
					r, mEventRecurrence);
			if (repeatString == null) {
				repeatString = r.getString(R.string.custom);
				Log.e(TAG, "Can't generate display string for " + mRrule);
				enabled = false;
			} else {
				// TODO Should give option to clear/reset rrule
//				enabled = RecurrencePickerDialog
//						.canHandleRecurrenceRule(mEventRecurrence);
//				if (!enabled) {
					Log.e(TAG, "UI can't handle " + mRrule);
//				}
			}
		} else {
			repeatString = r.getString(R.string.does_not_repeat);
			enabled = true;
		}
		Log.e(TAG, "repeatString---- " + repeatString);
		
		mRruleButton.setText(repeatString);

		String[] days = new String[] {
				DateUtils.getDayOfWeekString(Calendar.SUNDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.MONDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.TUESDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.WEDNESDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.THURSDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.FRIDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.SATURDAY,
						DateUtils.LENGTH_MEDIUM), };
		String[] ordinals = r.getStringArray(R.array.ordinal_labels);

		// Only display "Custom" in the spinner if the device does not support
		// the recurrence functionality of the event. Only display every weekday
		// if the event starts on a weekday.
		boolean isCustomRecurrence = isCustomRecurrence();
		boolean isWeekdayEvent = isWeekdayEvent();

		// ArrayList<String> repeatArray = new ArrayList<String>(0);
		// ArrayList<Integer> recurrenceIndexes = new ArrayList<Integer>(0);

		repeatArray = new ArrayList<String>(0);
		recurrenceIndexes = new ArrayList<Integer>(0);

		repeatArray.add(r.getString(R.string.aurora_does_not_repeat));
		recurrenceIndexes.add(EditEventHelper.DOES_NOT_REPEAT);

		repeatArray.add(r.getString(R.string.daily));
		recurrenceIndexes.add(EditEventHelper.REPEATS_DAILY);

		if (isWeekdayEvent) {
			repeatArray.add(r.getString(R.string.aurora_every_weekday));
			recurrenceIndexes.add(EditEventHelper.REPEATS_EVERY_WEEKDAY);
		}

		String format = r.getString(R.string.aurora_weekly);
		repeatArray.add(String.format(format, time.format("%A")));
		recurrenceIndexes.add(EditEventHelper.REPEATS_WEEKLY_ON_DAY);

		// Calculate whether this is the 1st, 2nd, 3rd, 4th, or last appearance
		// of the given day.
		int dayNumber = (time.monthDay - 1) / 7;
		format = r.getString(R.string.aurora_monthly_on_day_count);
		repeatArray.add(String.format(format, ordinals[dayNumber],
				days[time.weekDay]));
		recurrenceIndexes.add(EditEventHelper.REPEATS_MONTHLY_ON_DAY_COUNT);

		format = r.getString(R.string.monthly_on_day);
		repeatArray.add(String.format(format, time.monthDay));
		recurrenceIndexes.add(EditEventHelper.REPEATS_MONTHLY_ON_DAY);

		long when = time.toMillis(false);
		format = r.getString(R.string.yearly_plain);
		int flags = 0;
		if (DateFormat.is24HourFormat(mActivity)) {
			flags |= DateUtils.FORMAT_24HOUR;
		}
		repeatArray.add(format);
		recurrenceIndexes.add(EditEventHelper.REPEATS_YEARLY);

		if (isCustomRecurrence) {
			repeatArray.add(r.getString(R.string.custom));
			recurrenceIndexes.add(EditEventHelper.REPEATS_CUSTOM);
		}
		mRecurrenceIndexes = recurrenceIndexes;

		int position = recurrenceIndexes
				.indexOf(EditEventHelper.DOES_NOT_REPEAT);
		if (!TextUtils.isEmpty(mModel.mRrule)) {
			if (isCustomRecurrence) {
				position = recurrenceIndexes
						.indexOf(EditEventHelper.REPEATS_CUSTOM);
			} else {
				switch (mEventRecurrence.freq) {
				case EventRecurrence.DAILY:
					position = recurrenceIndexes
							.indexOf(EditEventHelper.REPEATS_DAILY);
					break;
				case EventRecurrence.WEEKLY:
					if (mEventRecurrence.repeatsOnEveryWeekDay()) {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_EVERY_WEEKDAY);
					} else {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_WEEKLY_ON_DAY);
					}
					break;
				case EventRecurrence.MONTHLY:
					if (mEventRecurrence.repeatsMonthlyOnDayCount()) {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_MONTHLY_ON_DAY_COUNT);
					} else {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_MONTHLY_ON_DAY);
					}
					break;
				case EventRecurrence.YEARLY:
					position = recurrenceIndexes
							.indexOf(EditEventHelper.REPEATS_YEARLY);
					break;
				}
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
				R.layout.gn_edit_event_repeat_item, repeatArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mRepeatsSpinner.setAdapter(adapter);
//		 mRepeatsSpinner.setSelection(position);
        setRepeat(position);

		// Don't allow the user to make exceptions recurring events.
		if (mModel.mOriginalSyncId != null) {
			mRepeatsSpinner.setEnabled(false);
		}
	}

	private boolean isCustomRecurrence() {

		if (mEventRecurrence.until != null
				|| (mEventRecurrence.interval != 0 && mEventRecurrence.interval != 1)
				|| mEventRecurrence.count != 0) {
			return true;
		}

		if (mEventRecurrence.freq == 0) {
			return false;
		}

		switch (mEventRecurrence.freq) {
		case EventRecurrence.DAILY:
			return false;
		case EventRecurrence.WEEKLY:
			if (mEventRecurrence.repeatsOnEveryWeekDay() && isWeekdayEvent()) {
				return false;
			} else if (mEventRecurrence.bydayCount == 1) {
				return false;
			}
			break;
		case EventRecurrence.MONTHLY:
			if (mEventRecurrence.repeatsMonthlyOnDayCount()) {
				/* this is a "3rd Tuesday of every month" sort of rule */
				return false;
			} else if (mEventRecurrence.bydayCount == 0
					&& mEventRecurrence.bymonthdayCount == 1
					&& mEventRecurrence.bymonthday[0] > 0) {
				/* this is a "22nd day of every month" sort of rule */
				return false;
			}
			break;
		case EventRecurrence.YEARLY:
			return false;
		}

		return true;
	}

	private boolean isWeekdayEvent() {
		if (mStartTime.weekDay != Time.SUNDAY
				&& mStartTime.weekDay != Time.SATURDAY) {
			return true;
		}
		return false;
	}

	static private class CalendarsAdapter extends ResourceCursorAdapter {
		public CalendarsAdapter(Context context, Cursor c) {
			super(context, R.layout.aurora_calendars_item, c);
			setDropDownViewResource(R.layout.aurora_calendars_dropdown_item);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View colorBar = view.findViewById(R.id.color);
			int colorColumn = cursor
					.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR);
			int nameColumn = cursor
					.getColumnIndexOrThrow(Calendars.CALENDAR_DISPLAY_NAME);
			int ownerColumn = cursor
					.getColumnIndexOrThrow(Calendars.OWNER_ACCOUNT);
			if (colorBar != null) {
				colorBar.setBackgroundColor(Utils
						.getDisplayColorFromColor(cursor.getInt(colorColumn)));
			}

			TextView name = (TextView) view.findViewById(R.id.calendar_name);
			if (name != null) {
				String displayName = cursor.getString(nameColumn);
				name.setText(displayName);

				TextView accountName = (TextView) view
						.findViewById(R.id.account_name);
				if (accountName != null) {
					accountName.setText(cursor.getString(ownerColumn));
					accountName.setVisibility(TextView.VISIBLE);
				}
			}
		}
	}

	/**
	 * Adapter for title auto completion.
	 */
	// private static class TitleAdapter extends ResourceCursorAdapter {
	// private final ContentResolver mContentResolver;
	//
	// public TitleAdapter(Context context) {
	// super(context, android.R.layout.simple_dropdown_item_1line, null, 0);
	// mContentResolver = context.getContentResolver();
	// }
	//
	// @Override
	// public int getCount() {
	// return Math.min(MAX_TITLE_SUGGESTIONS, super.getCount());
	// }
	//
	// private static String getTitleAtCursor(Cursor cursor) {
	// return cursor.getString(EVENT_INDEX_TITLE);
	// }
	//
	// @Override
	// public final String convertToString(Cursor cursor) {
	// return getTitleAtCursor(cursor);
	// }
	//
	// @Override
	// public void bindView(View view, Context context, Cursor cursor) {
	// TextView textView = (TextView) view;
	// textView.setText(getTitleAtCursor(cursor));
	// }
	//
	// @Override
	// public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
	// String filter = constraint == null ? "" : constraint.toString()
	// + "%";
	// if (filter.isEmpty()) {
	// return null;
	// }
	// long startTime = System.currentTimeMillis();
	//
	// // Query all titles prefixed with the constraint. There is no way to
	// // insert
	// // 'DISTINCT' or 'GROUP BY' to get rid of dupes, so use
	// // post-processing to
	// // remove dupes. We will order query results by descending event ID
	// // to show
	// // results that were most recently inputted.
	// Cursor tempCursor = mContentResolver.query(Events.CONTENT_URI,
	// EVENT_PROJECTION, TITLE_WHERE, new String[] { filter },
	// Events._ID + " DESC");
	// if (tempCursor != null) {
	// try {
	// // Post process query results.
	// Cursor c = uniqueTitlesCursor(tempCursor);
	//
	// // Log the processing duration.
	// long duration = System.currentTimeMillis() - startTime;
	// StringBuilder msg = new StringBuilder();
	// msg.append("Autocomplete of ");
	// msg.append(constraint);
	// msg.append(": title query match took ");
	// msg.append(duration);
	// msg.append("ms.");
	// Log.d(TAG, msg.toString());
	// return c;
	// } finally {
	// tempCursor.close();
	// }
	// } else {
	// return null;
	// }
	// }
	//
	// /**
	// * Post-process the query results to return the first
	// * MAX_TITLE_SUGGESTIONS unique titles in alphabetical order.
	// */
	// private Cursor uniqueTitlesCursor(Cursor cursor) {
	// TreeMap<String, String[]> titleToQueryResults = new TreeMap<String,
	// String[]>(
	// String.CASE_INSENSITIVE_ORDER);
	// int numColumns = cursor.getColumnCount();
	// cursor.moveToPosition(-1);
	//
	// // Remove dupes.
	// while ((titleToQueryResults.size() < MAX_TITLE_SUGGESTIONS)
	// && cursor.moveToNext()) {
	// String title = getTitleAtCursor(cursor).trim();
	// String data[] = new String[numColumns];
	// if (!titleToQueryResults.containsKey(title)) {
	// for (int i = 0; i < numColumns; i++) {
	// data[i] = cursor.getString(i);
	// }
	// titleToQueryResults.put(title, data);
	// }
	// }
	//
	// // Copy the sorted results to a new cursor.
	// MatrixCursor newCursor = new MatrixCursor(EVENT_PROJECTION);
	// for (String[] result : titleToQueryResults.values()) {
	// newCursor.addRow(result);
	// }
	// newCursor.moveToFirst();
	// return newCursor;
	// }
	// }

	/**
	 * Does prep steps for saving a calendar event.
	 * 
	 * This triggers a parse of the attendees list and checks if the event is
	 * ready to be saved. An event is ready to be saved so long as a model
	 * exists and has a calendar it can be associated with, either because it's
	 * an existing event or we've finished querying.
	 * 
	 * @return false if there is no model or no calendar had been loaded yet,
	 *         true otherwise.
	 */
	public boolean prepareForSave() {
		if (mModel == null || (mCalendarsCursor == null && mModel.mUri == null)) {
			return false;
		}
		return fillModelFromUI();
	}

	public boolean fillModelFromReadOnlyUi() {
		if (mModel == null || (mCalendarsCursor == null && mModel.mUri == null)) {
			return false;
		}
		mModel.mReminders = EventViewUtils.reminderItemsToReminders(
				mReminderItems, mReminderMinuteValues, mReminderMethodValues);
		mModel.mReminders.addAll(mUnsupportedReminders);
		mModel.normalizeReminders();

		return true;
	}

	// This is called if the user clicks on one of the buttons: "Save",
	// "Discard", or "Delete". This is also called if the user clicks
	// on the "remove reminder" button.
	@Override
	public void onClick(View view) {

//		if (view.getId() == R.id.gn_voice_add_activity_view) {
//			Statistics.onEvent(mActivity,
//					Statistics.EDIT_EVENT_CLICK_VOICE_BUTTON);
//			if (isClickVoiceButton) {
//				Log.i("jiating", "isClickVoiceButton=" + isClickVoiceButton);
//
//			} else {
//
//				Log.i("jiating", "isClickVoiceButton=" + isClickVoiceButton);
//				Intent intent = new Intent(
//						AuroraEditEventFragment.GN_RECOGNIZE_ACTION);
//				intent.putExtra("scene", "schedule_create");
//				intent.putExtra("appid", "com.android.calendar");
//				mActivity.startActivityForResult(intent,
//						AuroraEditEventFragment.GN_RECOGNIZE_ACTION_CODE);
//				setClickVoiceButton(true);
//
//			}
//
//			// Gionee <jiating><2013-07-03> modify for CR00830388 begin
//
//		} else 
		if (view.getId() == R.id.from_row) {
			final long startMillis = mStartTime
					.toMillis(false /* use isDst */);
			Statistics.onEvent(mActivity, Statistics.EDIT_EVENT_START_TIME);
			setEditTime(mActivity, startMillis, 0, mStartDataView);

		} else if (view.getId() == R.id.to_row) {
			final long endMillis = mEndTime.toMillis(false /* use isDst */);
			Statistics.onEvent(mActivity, Statistics.EDIT_EVENT_END_TIME);
			setEditTime(mActivity, endMillis, mStartTime.toMillis(false),
					mEndDataView);
		} else if (view.getId() == R.id.repeats_row) {

		} else {
			// This must be a click on one of the "remove reminder" buttons
			// Gionee jiating 2013-04-08 modify for new reminder view begin
			LinearLayout reminderItem = (LinearLayout) view.getParent();
			LinearLayout parent = (LinearLayout) reminderItem.getParent();
			LinearLayout bigParent = (LinearLayout) parent.getParent();
			bigParent.removeView(parent);
			mReminderItems.remove(parent);
			updateRemindersVisibility(mReminderItems.size());
			EventViewUtils.updateAddReminderButton(mActivity, mView,
					mReminderItems, mModel.mCalendarMaxReminders);
		}
	}

	// /M: do request focus action, if there is no focus on the view. @{
	public void requestFocus() {
		if (!(mTitleTextView.isFocused() || descriptionEditText.isFocused() //|| mAttendeesList.isFocused()
				)) {
			Log.i(TAG, "mTitleTextView no focus, request it.");
			mView.requestFocus(View.FOCUS_FORWARD);
			mTitleTextView.requestFocus();
			if (mTitleTextView.getText() != null) {
				mTitleTextView.setSelection(mTitleTextView.getText().length());
			}
			
			InputMethodManager imm = (InputMethodManager) mActivity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
					InputMethodManager.HIDE_NOT_ALWAYS);
		} else {
			Log.i(TAG, "mTitleTextView have focus, just do nothing.");
			showInputMethod();
		}
	}

	// /@}

	// This is called if the user cancels the "No calendars" dialog.
	// The "No calendars" dialog is shown if there are no syncable calendars.
	@Override
	public void onCancel(DialogInterface dialog) {
		if (dialog == mLoadingCalendarsDialog) {
			mLoadingCalendarsDialog = null;
			mSaveAfterQueryComplete = false;
		} else if (dialog == mNoCalendarsDialog) {
			mDone.setDoneCode(Utils.DONE_REVERT);
			mDone.run();
			return;
		}
	}

	// This is called if the user clicks on a dialog button.
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == mNoCalendarsDialog) {
			mDone.setDoneCode(Utils.DONE_REVERT);
			mDone.run();
			if (which == DialogInterface.BUTTON_POSITIVE) {
				Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
				final String[] array = { "com.android.calendar" };
				nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, array);
				nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(nextIntent);
			}

		}
	}

	// Goes through the UI elements and updates the model as necessary
	private boolean fillModelFromUI() {
		if (mModel == null) {
			return false;
		}
		Log.i("jiating", "fillModelFromUI");
		mModel.mReminders = EventViewUtils.reminderItemsToReminders(
				mReminderItems, mReminderMinuteValues, mReminderMethodValues);
		mModel.mReminders.addAll(mUnsupportedReminders);
		int count = mModel.mReminders.size();
		for (int i = 0; i < count; i++) {

			Log.i("jiating", "fillModelFromUI="
					+ mModel.mReminders.get(i).getMinutes()
					+ "....mReminders.get(i).getMethod()"
					+ mModel.mReminders.get(i).getMethod());
			switch (mModel.mReminders.get(i).getMinutes()) {
			case 0:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_ZERO);
				break;
			case 1:
				Statistics
						.onEvent(mActivity, Statistics.EDIT_EVENT_REMINDE_ONE);
				break;
			case 5:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_FIVE);
				break;
			case 10:
				Statistics
						.onEvent(mActivity, Statistics.EDIT_EVENT_REMINDE_TEN);
				break;
			case 15:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_FIFTEEN);
				break;

			case 20:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_TWETY);
				break;

			case 25:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_TWETY_FIVE);
				break;

			case 30:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_THIRTY);
				break;
			case 45:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_FORTY_FIVE);
				break;
			case 60:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_ONE_HOUR);
				break;

			case 120:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_TWO_HOURS);
				break;
			case 180:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_THREE_HOURS);
				break;
			case 720:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_TWELVE_HOURS);
				break;
			case 1440:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_TWETY_FOUR);
				break;
			case 2880:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_TWO_DAYS);
				break;

			case 10080:
				Statistics.onEvent(mActivity,
						Statistics.EDIT_EVENT_REMINDE_ONE_WEEK);
				break;
			default:
				break;
			}
		}
		mModel.normalizeReminders();
		mModel.mHasAlarm = mReminderItems.size() > 0;
		mModel.mTitle = mTitleTextView.getText().toString();
		mModel.mDescription = descriptionEditText.getText().toString();
		if (TextUtils.isEmpty(mModel.mLocation)) {
			mModel.mLocation = null;
		}
		if (TextUtils.isEmpty(mModel.mDescription)) {
			mModel.mDescription = null;
		} else {
			Statistics.onEvent(mActivity, Statistics.EDIT_EVENT_DESCRIPTION);
		}

//		if (mAttendeesList != null) {
//			mEmailValidator.setRemoveInvalid(true);
//			mAttendeesList.performValidation();
//			mModel.mAttendeesList.clear();
//			// /M:@{
//			String address = mAttendeesList.getText().toString();
//			if (!TextUtils.isEmpty(address)
//					&& isHasInvalidAddress(address, mEmailValidator)) {
//				ToastMsg(R.string.attendees_invalid_tip);
//			}
//			// /@}
//			mModel.addAttendees(mAttendeesList.getText().toString(),
//					mEmailValidator);
//			/**
//			 * M: let mHasAttendeeData keep with the attendee number in
//			 * mAttendeesList. @{
//			 */
//			if (mModel.mAttendeesList.size() > 0) {
//				mModel.mHasAttendeeData = true;
//			}
//			/**@}*/
//			mEmailValidator.setRemoveInvalid(false);
//		}

		// If this was a new event we need to fill in the Calendar information
		if (mModel.mUri == null) {
			mModel.mCalendarId = mCalendarsSpinner.getSelectedItemId();
			int calendarCursorPosition = mCalendarsSpinner
					.getSelectedItemPosition();
			if (mCalendarsCursor.moveToPosition(calendarCursorPosition)) {
				String defaultCalendar = mCalendarsCursor
						.getString(EditEventHelper.CALENDARS_INDEX_OWNER_ACCOUNT);
				Utils.setSharedPreference(mActivity,
						GeneralPreferences.KEY_DEFAULT_CALENDAR,
						defaultCalendar);
				mModel.mOwnerAccount = defaultCalendar;
				mModel.mOrganizer = defaultCalendar;
				mModel.mCalendarId = mCalendarsCursor
						.getLong(EditEventHelper.CALENDARS_INDEX_ID);
			}
		}

		if (mModel.mAllDay) {
			// /M: @{
			LogUtil.v(TAG, "all-day event, mTimezone set to UTC");
			// /@}
			// Reset start and end time, increment the monthDay by 1, and set
			// the timezone to UTC, as required for all-day events.
			mTimezone = Time.TIMEZONE_UTC;
			mStartTime.hour = 0;
			mStartTime.minute = 0;
			mStartTime.second = 0;
			mStartTime.timezone = mTimezone;
			mModel.mStart = mStartTime.normalize(true);

			mEndTime.hour = 0;
			mEndTime.minute = 0;
			mEndTime.second = 0;
			mEndTime.timezone = mTimezone;
			// When a user see the event duration as "X - Y" (e.g. Oct. 28 -
			// Oct. 29), end time
			// should be Y + 1 (Oct.30).
			final long normalizedEndTimeMillis = mEndTime.normalize(true)
					+ DateUtils.DAY_IN_MILLIS;
			if (normalizedEndTimeMillis < mModel.mStart) {
				// mEnd should be midnight of the next day of mStart.
				mModel.mEnd = mModel.mStart + DateUtils.DAY_IN_MILLIS;
			} else {
				mModel.mEnd = normalizedEndTimeMillis;
			}
		} else {
			mStartTime.timezone = mTimezone;
			mEndTime.timezone = mTimezone;
			mModel.mStart = mStartTime.toMillis(true);
			mModel.mEnd = mEndTime.toMillis(true);
		}
		mModel.mTimezone = mTimezone;
		// TODO set correct availability value

		int selection;
		// If we're making an exception we don't want it to be a repeating
		// event.
		if (mModification == EditEventHelper.MODIFY_SELECTED) {
			selection = EditEventHelper.DOES_NOT_REPEAT;
		} else {
			int position = mRepeatsSpinner.getSelectedItemPosition();
			selection = mRecurrenceIndexes.get(position);
		}

		EditEventHelper.updateRecurrenceRule(selection, mModel,
				Utils.getFirstDayOfWeek(mActivity) + 1);

		// Save the timezone so we can display it as a standard option next time
		if (!mModel.mAllDay) {
			// /M: @{
			LogUtil.v(TAG, mTimezone + " is set to recent time zone");
			// /@}

		}
		return true;
	}

	// Gionee <jiangxiao> <2013-06-22> add for CR00829027 begin
	private boolean mFirstTimeSizeChanged = true;

	// Gionee <jiangxiao> <2013-06-22> add for CR00829027 end

	public AuroraEditEventView(Activity activity, View view,
			EditDoneRunnable done) {
		// /M: #extension# @{
		mExt = ExtensionFactory.getEditEventViewExt(activity, this);
		// /@}

		mActivity = activity;
		mView = view;
		mDone = done;
//		mIsClickMoreButton = false;
		Typeface editDateTypeface = Typeface
				.createFromFile("system/fonts/Roboto-Regular.ttf");
		Typeface editTimeTypeface = Typeface
				.createFromFile("system/fonts/RobotoCondensed-Bold.ttf");
		// cache top level view elements
		mLoadingMessage = (TextView) view.findViewById(R.id.loading_message);
		mScrollView = (ScrollView) view.findViewById(R.id.scroll_view);

		// cache all the widgets
		mCalendarsSpinner = (AuroraSpinner) view
				.findViewById(R.id.calendars_spinner);
		mCalendarsSpinner.setPrompt(activity.getResources().getString(
				R.string.edit_event_calendar_label));
		mCalendarsSpinner.setPadding(0, 0, 0, 0);
		mCalendarsSpinner.setBackground(R.drawable.aurora_edit_item_bg);
		mCalendarsSpinner.changeDirection(false);
		mTitleTextView = (AuroraEditText) view.findViewById(R.id.title);
		mTitleTextView.setIsNeedDeleteAll(true);

		descriptionEditText = (AuroraEditText) view
				.findViewById(R.id.description);
		descriptionEditText.setIsNeedDeleteAll(true);
		// mDescriptionTextView = (TextView)
		// view.findViewById(R.id.description);
		// Gionee <jiating><2013-07-03> modify for CR00830388 begin
		// mStartDateButton = (Button) view.findViewById(R.id.start_date);
		// mStartDateButton.setPadding(0,0,0,0);
		// mEndDateButton = (Button) view.findViewById(R.id.end_date);
		// mEndDateButton.setPadding(0,0,0,0);
		mStartDateText = (TextView) view.findViewById(R.id.start_date);
		mStartTimeText = (TextView) view.findViewById(R.id.start_time);
		mStartDataView = view.findViewById(R.id.from_row);
		mEndDateText = (TextView) view.findViewById(R.id.end_date);
		mEndTimeText = (TextView) view.findViewById(R.id.end_time);
		mEndDataView = view.findViewById(R.id.to_row);
		mReminderMe = (ImageView) view.findViewById(R.id.reminder_switch);
		mStartDateText.setTypeface(editDateTypeface);
		mStartTimeText.setTypeface(editTimeTypeface);
		mEndDateText.setTypeface(editDateTypeface);
		mEndTimeText.setTypeface(editTimeTypeface);
		if (isOpen) {
			mReminderMe.setImageResource(R.drawable.aurora_btn_clock_open);
		} else {
			mReminderMe.setImageResource(R.drawable.aurora_btn_clock_close);
		}

		// Gionee <jiating><2013-07-03> modify for CR00830388 end
		mRepeatView = view.findViewById(R.id.repeats_row);
		mRepeatsSpinner = (AuroraSpinner) view.findViewById(R.id.repeats);
		mRruleButton = (TextView) view.findViewById(R.id.repeat_content);
		mRepeatsSpinner.setBackground(null);
		mRepeatsSpinner.setClickable(false);

		mCalendarSelectorGroup = view
				.findViewById(R.id.calendar_selector_group);
		mCalendarStaticGroup = view.findViewById(R.id.calendar_group);
		// mRemindersGroup = view.findViewById(R.id.reminders_row);
		mAttendeesGroup = view.findViewById(R.id.add_attendees_row);
		mDescriptionGroup = view.findViewById(R.id.description_row);
		mAttendeesList = (MultiAutoCompleteTextView) view
				.findViewById(R.id.attendees);
		editEventView2 = (LinearLayout) view
				.findViewById(R.id.gn_edit_event_view);
		// Gionee <jiating><2013-07-05> modify for CR00825949 begin
		mCalendarAccountAll = (LinearLayout) view
				.findViewById(R.id.calendar_select_account_all);
		mCalendarAccountName = (TextView) view
				.findViewById(R.id.calendar_select_account_name);
		// Gionee <jiating><2013-07-05> modify for CR00825949 end
		mTitleTextView.addTextChangedListener(titleTextWatcher);
		descriptionEditText.addTextChangedListener(descriptionTextWatcher);
		descriptionEditText.setBackground(null);
		mTitleTextView.setBackground(null);
		mTitleTextView.setTag(mTitleTextView.getBackground());
		// mTitleTextView.setAdapter(new TitleAdapter(activity));
		// mTitleTextView.setOnEditorActionListener(new OnEditorActionListener()
		// {
		// @Override
		// public boolean onEditorAction(TextView v, int actionId,
		// KeyEvent event) {
		// if (actionId == EditorInfo.IME_ACTION_DONE) {
		// // Dismiss the suggestions dropdown. Return false so the
		// // other
		// // side effects still occur (soft keyboard going away,
		// // etc.).
		// mTitleTextView.dismissDropDown();
		// }
		// return false;
		// }
		// });

		// descriptionEditText.setTag(mDescriptionTextView.getBackground());
		mRepeatsSpinner.setTag(mRepeatsSpinner.getBackground());
//		mAttendeesList.setTag(mAttendeesList.getBackground());
		mOriginalSpinnerPadding[0] = mRepeatsSpinner.getPaddingLeft();
		mOriginalSpinnerPadding[1] = mRepeatsSpinner.getPaddingTop();
		mOriginalSpinnerPadding[2] = mRepeatsSpinner.getPaddingRight();
		mOriginalSpinnerPadding[3] = mRepeatsSpinner.getPaddingBottom();
		mEditViewList.add(mTitleTextView);
		mEditViewList.add(descriptionEditText);
//		mEditViewList.add(mAttendeesList);

		mEditOnlyList.add(view.findViewById(R.id.from_row));
		mEditOnlyList.add(view.findViewById(R.id.to_row));

		// mRemindersContainer = (LinearLayout) view
		// .findViewById(R.id.reminder_items_container);

		mTimezone = Utils.getTimeZone(activity, null);
		mIsMultipane = activity.getResources().getBoolean(R.bool.tablet_config);
		mStartTime = new Time(mTimezone);
		mEndTime = new Time(mTimezone);
		mEmailValidator = new Rfc822Validator(null);
//		initMultiAutoCompleteTextView((RecipientEditTextView) mAttendeesList);

		// /M:add input filter @{
		IEditTextExt extension = ExtensionFactory.getEditTextExt();
		extension.setLengthInputFilter((AuroraEditText) mTitleTextView,
				mActivity, TITLE_MAX_LENGTH);
		extension.setLengthInputFilter((AuroraEditText) descriptionEditText,
				mActivity, DESCRIPTION_MAX_LENGTH);
		// /@}

		// Display loading screen
		setModel(null);

		mReminderMe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (isOpen) {
					mReminderMe.setImageResource(R.drawable.aurora_btn_clock_close);
					// CalendarEventModel model = mModel;
					mModel.mReminders.clear();
					mReminderItems.clear();
					isOpen = false;
					updateRemindersVisibility(mReminderItems.size());
				} else {

					Statistics.onEvent(mActivity,
							Statistics.EDIT_EVENT_CLICK_ADD_REMINDER);
					addReminder();
					isOpen = true;
					mReminderMe.setImageResource(R.drawable.aurora_btn_clock_open);
				}

			}
		});

		if (mRepeatView != null) {
			mRepeatView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Statistics.onEvent(mActivity, Statistics.EDIT_EVENT_REPITE);
					Intent intent = new Intent(mActivity,
							AuroraEventRepeatActivity.class);
					intent.putExtra("start_time", mStartTime.toMillis(false));
					intent.putExtra("position", mPosition);
					intent.putExtra("is_weekday", isWeekdayEvent());
					intent.putExtra("is_custom", isCustomRecurrence());
					intent.putExtra("is_empty",
							TextUtils.isEmpty(mModel.mRrule));
					// mModel.
					Log.e("liumxxx", "isweekday----------------------------"
							+ isWeekdayEvent());
					Log.e("liumxxx", "iscustom----------------------------"
							+ isCustomRecurrence());
					Log.e("liumxxx", "startTime----------------------------"
							+ mStartTime.toMillis(false));
					// intent.setClass(mActivity,
					// AuroraEventRepeatActivity.class);
					int requestcode = 120;
					mActivity.startActivityForResult(intent, requestcode);
					// }
					// return false;
					// }
					// });
					// TODO Auto-generated method stub
					// Intent intent = new
					// Intent(AuroraEditEventFragment.REPEAT_CHOOSE_ACTION);
					// // intent.setClassName(packageName, className)
					// intent.putExtra("scene", "schedule_create");
					// intent.putExtra("appid", "com.android.calendar");
					// mActivity.startActivityForResult(intent,
					// AuroraEditEventFragment.REPEAT_CHOOSE_ACTION_CODE);
				}
			});
		}
	}

	/**
	 * Loads an integer array asset into a list.
	 */
	private static ArrayList<Integer> loadIntegerArray(Resources r, int resNum) {
		int[] vals = r.getIntArray(resNum);
		int size = vals.length;
		ArrayList<Integer> list = new ArrayList<Integer>(size);

		for (int i = 0; i < size; i++) {
			list.add(vals[i]);
		}

		return list;
	}

	/**
	 * Loads a String array asset into a list.
	 */
	private static ArrayList<String> loadStringArray(Resources r, int resNum) {
		String[] labels = r.getStringArray(resNum);
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(labels));
		return list;
	}

	private void updateSwitchState() {

	}

	/**
	 * Prepares the reminder UI elements.
	 * <p>
	 * (Re-)loads the minutes / methods lists from the XML assets, adds/removes
	 * items as needed for the current set of reminders and calendar properties,
	 * and then creates UI elements.
	 */
	private void prepareReminders() {
		CalendarEventModel model = mModel;
		Resources r = mActivity.getResources();

		// Load the labels and corresponding numeric values for the minutes and
		// methods lists
		// from the assets. If we're switching calendars, we need to clear and
		// re-populate the
		// lists (which may have elements added and removed based on calendar
		// properties). This
		// is mostly relevant for "methods", since we shouldn't have any
		// "minutes" values in a
		// new event that aren't in the default set.
		mReminderMinuteValues = loadIntegerArray(r,
				R.array.reminder_minutes_values);
		mReminderMinuteLabels = loadStringArray(r,
				R.array.reminder_minutes_labels);
		mReminderMethodValues = loadIntegerArray(r,
				R.array.reminder_methods_values);
		mReminderMethodLabels = loadStringArray(r,
				R.array.reminder_methods_labels);

		// Remove any reminder methods that aren't allowed for this calendar. If
		// this is
		// a new event, mCalendarAllowedReminders may not be set the first time
		// we're called.
		if (mModel.mCalendarAllowedReminders != null) {
			EventViewUtils.reduceMethodList(mReminderMethodValues,
					mReminderMethodLabels, mModel.mCalendarAllowedReminders);
		}

		int numReminders = 0;
		Log.i("GNEditEvent", "GNEditEvent-mHasAlarm == " + model.mHasAlarm);
		if (model.mHasAlarm) {
			ArrayList<ReminderEntry> reminders = model.mReminders;
			numReminders = reminders.size();
			// Insert any minute values that aren't represented in the minutes
			// list.
			for (ReminderEntry re : reminders) {
				if (mReminderMethodValues.contains(re.getMethod())) {
					EventViewUtils.addMinutesToList(mActivity,
							mReminderMinuteValues, mReminderMinuteLabels,
							re.getMinutes());
				}
			}

			// Create a UI element for each reminder. We display all of the
			// reminders we get
			// from the provider, even if the count exceeds the calendar
			// maximum. (Also, for
			// a new event, we won't have a maxReminders value available.)
			mUnsupportedReminders.clear();
			for (ReminderEntry re : reminders) {
				if (mReminderMethodValues.contains(re.getMethod())
						|| re.getMethod() == Reminders.METHOD_DEFAULT) {
					Log.i("GNEditEvent", "GNEditEvent-mHasAlarm == " + 1);
					EventViewUtils.addReminder(mActivity, mScrollView, this,
							mReminderItems, mReminderMinuteValues,
							mReminderMinuteLabels, mReminderMethodValues,
							mReminderMethodLabels, re, Integer.MAX_VALUE, null);
				} else {
					// TODO figure out a way to display unsupported reminders
					Log.i("GNEditEvent", "GNEditEvent-mHasAlarm == " + 2);
					mUnsupportedReminders.add(re);
				}
			}
		}

		updateRemindersVisibility(numReminders);
		EventViewUtils.updateAddReminderButton(mActivity, mView,
				mReminderItems, mModel.mCalendarMaxReminders);
	}

	/**
	 * Fill in the view with the contents of the given event model. This allows
	 * an edit view to be initialized before the event has been loaded. Passing
	 * in null for the model will display a loading screen. A non-null model
	 * will fill in the view's fields with the data contained in the model.
	 * 
	 * @param model
	 *            The event model to pull the data from
	 */
	public void setModel(CalendarEventModel model) {
		mModel = model;

		// Need to close the autocomplete adapter to prevent leaking cursors.
		if (mAddressAdapter != null
				&& mAddressAdapter instanceof EmailAddressAdapter) {
			((EmailAddressAdapter) mAddressAdapter).close();
			mAddressAdapter = null;
		}

		if (model == null) {
			// Display loading screen
			mLoadingMessage.setVisibility(View.VISIBLE);
			mScrollView.setVisibility(View.GONE);
			return;
		}

		boolean canRespond = EditEventHelper.canRespond(model);

		long begin = model.mStart;
		long end = model.mEnd;
		mTimezone = model.mTimezone; // this will be UTC for all day events

		// /M: If the begim & end Millis <= 0,also set it to mStartTime &
		// mEndTime
		// because if the timezone if bore utc,the millis may be less than 0 @{
		// Set up the starting times
		/*
		 * (if (begin > 0) { mStartTime.timezone = mTimezone;
		 * mStartTime.set(begin); mStartTime.normalize(true); } if (end > 0) {
		 * mEndTime.timezone = mTimezone; mEndTime.set(end);
		 * mEndTime.normalize(true); }
		 */
		Log.i("jiating", "setModel.......begin=" + model.mStart);
		mStartTime.timezone = mTimezone;
		mStartTime.set(begin);
		mStartTime.normalize(true);
		Log.i("jiating", "setModel.......begin=" + mStartTime.toString());
		mEndTime.timezone = mTimezone;
		mEndTime.set(end);
		mEndTime.normalize(true);
		// /@}

		String rrule = model.mRrule;
		if (!TextUtils.isEmpty(rrule)) {
			mEventRecurrence.parse(rrule);
		}

		// /M:remove the "Guest" when it is local account. @{
		setAttendeesGroupVisibility(model.mAccountType);
		// /@}
		// If the user is allowed to change the attendees set up the view and
		// validator
		if (!model.mHasAttendeeData) {
			mAttendeesGroup.setVisibility(View.GONE);
		}

		SharedPreferences prefs = GeneralPreferences
				.getSharedPreferences(mActivity);
		String defaultReminderString = prefs.getString(
				GeneralPreferences.KEY_DEFAULT_REMINDER,
				GeneralPreferences.NO_REMINDER_STRING);
		mDefaultReminderMinutes = Integer.parseInt(defaultReminderString);

		prepareReminders();

		if (model.mTitle != null) {
			mTitleTextView.setTextKeepState(model.mTitle);
		}

		if (model.mIsOrganizer || TextUtils.isEmpty(model.mOrganizer)
				|| model.mOrganizer.endsWith(GOOGLE_SECONDARY_CALENDAR)) {

		} else {
			((TextView) mView.findViewById(R.id.organizer))
					.setText(model.mOrganizerDisplayName);
		}

		if (model.mDescription != null) {
			descriptionEditText.setTextKeepState(model.mDescription);
		}

		if (canRespond) {
			int buttonToCheck = EventInfoFragment
					.findButtonIdForResponse(model.mSelfAttendeeStatus);

		}
		if (model.mTitle != null || model.mDescription != null) {

		} else {

		}
		int displayColor = Utils.getDisplayColorFromColor(model.mCalendarColor);
		if (model.mUri != null) {
			// This is an existing event so hide the calendar spinner
			// since we can't change the calendar.
			View calendarGroup = mView
					.findViewById(R.id.calendar_selector_group);
			calendarGroup.setVisibility(View.GONE);
			TextView tv = (TextView) mView.findViewById(R.id.calendar_textview);
			tv.setText(model.mCalendarDisplayName);
			tv = (TextView) mView
					.findViewById(R.id.calendar_textview_secondary);
			if (tv != null) {
				tv.setText(model.mOwnerAccount);
			}
			if (mIsMultipane) {
				mView.findViewById(R.id.calendar_textview).setBackgroundColor(
						displayColor);
			} else {
				// mView.findViewById(R.id.calendar_group).setBackgroundColor(displayColor);
			}
		} else {
			View calendarGroup = mView.findViewById(R.id.calendar_group);
			calendarGroup.setVisibility(View.GONE);
		}

		populateWhen();
		populateRepeats();
//		updateAttendees(model.mAttendeesList);

		updateView();
		mScrollView.setVisibility(View.VISIBLE);
		mLoadingMessage.setVisibility(View.GONE);
		sendAccessibilityEvent();

		// /M: extend setModel @{
		mExt.setExtUI(model);
		// /@}

	}

	private void sendAccessibilityEvent() {
		AccessibilityManager am = (AccessibilityManager) mActivity
				.getSystemService(Service.ACCESSIBILITY_SERVICE);
		if (!am.isEnabled() || mModel == null) {
			return;
		}
		StringBuilder b = new StringBuilder();
		addFieldsRecursive(b, mView);
		CharSequence msg = b.toString();

		AccessibilityEvent event = AccessibilityEvent
				.obtain(AccessibilityEvent.TYPE_VIEW_FOCUSED);
		event.setClassName(getClass().getName());
		event.setPackageName(mActivity.getPackageName());
		event.getText().add(msg);
		event.setAddedCount(msg.length());

		am.sendAccessibilityEvent(event);
	}

	private void addFieldsRecursive(StringBuilder b, View v) {
		if (v == null || v.getVisibility() != View.VISIBLE) {
			return;
		}
		if (v instanceof TextView) {
			CharSequence tv = ((TextView) v).getText();
			if (!TextUtils.isEmpty(tv.toString().trim())) {
				b.append(tv + PERIOD_SPACE);
			}
		} else if (v instanceof RadioGroup) {
			RadioGroup rg = (RadioGroup) v;
			int id = rg.getCheckedRadioButtonId();
			if (id != View.NO_ID) {
				b.append(((RadioButton) (v.findViewById(id))).getText()
						+ PERIOD_SPACE);
			}
		} else if (v instanceof AuroraSpinner) {
			AuroraSpinner s = (AuroraSpinner) v;
			if (s.getSelectedItem() instanceof String) {
				String str = ((String) (s.getSelectedItem())).trim();
				if (!TextUtils.isEmpty(str)) {
					b.append(str + PERIOD_SPACE);
				}
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int children = vg.getChildCount();
			for (int i = 0; i < children; i++) {
				addFieldsRecursive(b, vg.getChildAt(i));
			}
		}
	}

	/**
	 * Creates a single line string for the time/duration
	 */
	protected void setWhenString() {
		String when;
		int flags = DateUtils.FORMAT_SHOW_DATE;
		String tz = mTimezone;
		if (mModel.mAllDay) {
			flags |= DateUtils.FORMAT_SHOW_WEEKDAY;
			tz = Time.TIMEZONE_UTC;
		} else {
			flags |= DateUtils.FORMAT_SHOW_TIME;
			if (DateFormat.is24HourFormat(mActivity)) {
				flags |= DateUtils.FORMAT_24HOUR;
			}
		}
		long startMillis = mStartTime.normalize(true);
		long endMillis = mEndTime.normalize(true);
		mSB.setLength(0);

	}

	/**
	 * Configures the Calendars spinner. This is only done for new events,
	 * because only new events allow you to select a calendar while editing an
	 * event.
	 * <p>
	 * We tuck a reference to a Cursor with calendar database data into the
	 * spinner, so that we can easily extract calendar-specific values when the
	 * value changes (the spinner's onItemSelected callback is configured).
	 */
	public void setCalendarsCursor(Cursor cursor, boolean userVisible) {
		// If there are no syncable calendars, then we cannot allow
		// creating a new event.
		mCalendarsCursor = cursor;
		if (cursor == null || cursor.getCount() == 0) {
			// Cancel the "loading calendars" dialog if it exists
			if (mSaveAfterQueryComplete) {
				mLoadingCalendarsDialog.cancel();
			}
			if (!userVisible) {
				return;
			}
			// Create an error message for the user that, when clicked,
			// will exit this activity without saving the event.
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(
					mActivity);
			builder.setTitle(R.string.no_syncable_calendars)
					.setIconAttribute(android.R.attr.alertDialogIcon)
					.setMessage(R.string.no_calendars_found)
					.setPositiveButton(R.string.add_account, this)
					.setNegativeButton(android.R.string.no, this)
					.setOnCancelListener(this);
			mNoCalendarsDialog = builder.show();
			return;
		}

		int defaultCalendarPosition = findDefaultCalendarPosition(cursor);

		// populate the calendars spinner
		CalendarsAdapter adapter = new CalendarsAdapter(mActivity, cursor);
		mCalendarsSpinner.setAdapter(adapter);
		mCalendarsSpinner.setSelection(defaultCalendarPosition);
		mCalendarsSpinner.setOnItemSelectedListener(this);

		if (mSaveAfterQueryComplete) {
			mLoadingCalendarsDialog.cancel();
			if (prepareForSave() && fillModelFromUI()) {
				int exit = userVisible ? Utils.DONE_EXIT : 0;
				mDone.setDoneCode(Utils.DONE_SAVE | exit);
				mDone.run();
			} else if (userVisible) {
				mDone.setDoneCode(Utils.DONE_EXIT);
				mDone.run();
			} else if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG,
						"SetCalendarsCursor:Save failed and unable to exit view");
			}
			return;
		}
	}

	/**
	 * Updates the view based on {@link #mModification} and {@link #mModel}
	 */
	public void updateView() {
		if (mModel == null) {
			return;
		}
		if (EditEventHelper.canModifyEvent(mModel)) {
			setViewStates(mModification);
		} else {
			setViewStates(Utils.MODIFY_UNINITIALIZED);
		}
	}

	private void setViewStates(int mode) {
		// Extra canModify check just in case
		if (mode == Utils.MODIFY_UNINITIALIZED
				|| !EditEventHelper.canModifyEvent(mModel)) {
			setWhenString();

			for (View v : mViewOnlyList) {
				v.setVisibility(View.VISIBLE);
			}
			for (View v : mEditOnlyList) {
				v.setVisibility(View.GONE);
			}
			for (View v : mEditViewList) {
				v.setEnabled(false);
				v.setBackgroundDrawable(null);
			}
			Log.i("jiating", "setViewStates.....if");
			mCalendarSelectorGroup.setVisibility(View.GONE);
			mCalendarStaticGroup.setVisibility(View.GONE);
//			mCalendarStaticGroup.setVisibility(View.VISIBLE);
			mRepeatsSpinner.setEnabled(false);
			mRepeatsSpinner.setBackgroundDrawable(null);
			if (EditEventHelper.canAddReminders(mModel)) {
				// mRemindersGroup.setVisibility(View.VISIBLE);
			} else {
				// mRemindersGroup.setVisibility(View.GONE);
			}

			if (TextUtils.isEmpty(descriptionEditText.getText())) {
				mDescriptionGroup.setVisibility(View.GONE);
			}
		} else {
			for (View v : mViewOnlyList) {
				v.setVisibility(View.GONE);
			}
			for (View v : mEditOnlyList) {
				v.setVisibility(View.VISIBLE);
			}
			for (View v : mEditViewList) {
				v.setEnabled(true);
				if (v.getTag() != null) {
					v.setBackgroundDrawable((Drawable) v.getTag());
					// v.setPadding(mOriginalPadding[0], mOriginalPadding[1],
					// mOriginalPadding[2], mOriginalPadding[3]);
				}
			}
			if (mModel.mUri == null) {
				Log.i("jiating", "setViewStates.....else if");
				// mCalendarSelectorGroup.setVisibility(View.VISIBLE);
				mCalendarSelectorGroup.setVisibility(View.GONE);
				mCalendarStaticGroup.setVisibility(View.GONE);
			} else {
				Log.i("jiating", "setViewStates.....else else");
				mCalendarSelectorGroup.setVisibility(View.GONE);
				mCalendarStaticGroup.setVisibility(View.GONE);
//				mCalendarStaticGroup.setVisibility(View.VISIBLE);
			}
			mRepeatsSpinner.setBackgroundDrawable((Drawable) mRepeatsSpinner
					.getTag());
			// mRepeatsSpinner.setPadding(mOriginalSpinnerPadding[0],
			// mOriginalSpinnerPadding[1], mOriginalSpinnerPadding[2],
			// mOriginalSpinnerPadding[3]);
			mRepeatsSpinner.setPadding(0, 0, 0, 0);
			// / M:Check the status of repeat spinner, don't allow the user to
			// make exceptions
			// recurring events, and don't enable it when time is not in range.
			// @{
			updateRepeatsSpinnerStatus(mModel.mStart, mModel.mEnd);
			// / @}
			// mRemindersGroup.setVisibility(View.VISIBLE);

			mDescriptionGroup.setVisibility(View.VISIBLE);
		}
		// /M: After querying database ended, all the mode should
		// / judge whether the Timezone view should be shown or not.@{

		// / @}
	}

	public void setModification(int modifyWhich) {
		mModification = modifyWhich;
		updateView();
		updateHomeTime();
	}

	// Find the calendar position in the cursor that matches calendar in
	// preference
	private int findDefaultCalendarPosition(Cursor calendarsCursor) {
		if (calendarsCursor.getCount() <= 0) {
			return -1;
		}

		String defaultCalendar = Utils.getSharedPreference(mActivity,
				GeneralPreferences.KEY_DEFAULT_CALENDAR, (String) null);

		int calendarsOwnerColumn = calendarsCursor
				.getColumnIndexOrThrow(Calendars.OWNER_ACCOUNT);

		// /M: error handling @{
		if (calendarsOwnerColumn < 0) {
			LogUtil.w(TAG,
					"getColumnIndexOrThrow(Calendar.OWNER_ACCOUNT) failed, return 0");
			return 0;
		}
		// / @}

		int accountNameIndex = calendarsCursor
				.getColumnIndexOrThrow(Calendars.ACCOUNT_NAME);
		int accountTypeIndex = calendarsCursor
				.getColumnIndexOrThrow(Calendars.ACCOUNT_TYPE);
		int position = 0;
		calendarsCursor.moveToPosition(-1);
		while (calendarsCursor.moveToNext()) {
			String calendarOwner = calendarsCursor
					.getString(calendarsOwnerColumn);
			if (defaultCalendar == null) {
				// There is no stored default upon the first time running. Use a
				// primary
				// calendar in this case.
				if (calendarOwner != null
						&& calendarOwner.equals(calendarsCursor
								.getString(accountNameIndex))
						&& !CalendarContract.ACCOUNT_TYPE_LOCAL
								.equals(calendarsCursor
										.getString(accountTypeIndex))) {
					return position;
				}
			} else if (defaultCalendar.equals(calendarOwner)) {
				// Found the default calendar.
				return position;
			}
			position++;
		}
		return 0;
	}

	private void updateAttendees(HashMap<String, Attendee> attendeesList) {
//		Log.i("jiating", "AuroraEditEventView...updateAttendees");
//		if (attendeesList == null || attendeesList.isEmpty()) {
//			return;
//		}
//		mAttendeesList.setText(null);
//		for (Attendee attendee : attendeesList.values()) {
//			mAttendeesList.append(attendee.mEmail);
//		}
	}

	private void updateRemindersVisibility(int numReminders) {
		Log.i("jiating", "updateRemindersVisibility.....numReminders="
				+ numReminders + "maxReminders...="
				+ mModel.mCalendarMaxReminders);
		Log.i("GNEditEvent", "GNEditEvent-numReminders == " + numReminders);
		if (numReminders == 0) {
			Log.i("jiating", "updateRemindersVisibility.......if11");
			// mRemindersContainer.setVisibility(View.GONE);
			isOpen = false;
			mReminderMe.setImageResource(R.drawable.aurora_btn_clock_close);

		} else if (numReminders >= mModel.mCalendarMaxReminders) {
			Log.i("jiating", "updateRemindersVisibility.......else if11");
			// mRemindersContainer.setVisibility(View.GONE);
			// mRemindersContainer.setBackgroundResource(R.drawable.gn_edit_event_content_single_bg);
		} else {
			Log.i("jiating", "updateRemindersVisibility.......else11");
			// mRemindersContainer.setVisibility(View.GONE);
			// mRemindersContainer.setBackgroundResource(R.drawable.gn_all_in_one_sliding_content_bg_top);
		}

		// EventViewUtils.updateReminderDiver(mView,numReminders,mModel.mCalendarMaxReminders);
		// / @}
	}

	/**
	 * Add a new reminder when the user hits the "add reminder" button. We use
	 * the default reminder time and method.
	 */
	private void addReminder() {
		// TODO: when adding a new reminder, make it different from the
		// last one in the list (if any).
		if (mDefaultReminderMinutes == GeneralPreferences.NO_REMINDER) {
			EventViewUtils.addReminder(mActivity, mScrollView, this,
					mReminderItems, mReminderMinuteValues,
					mReminderMinuteLabels, mReminderMethodValues,
					mReminderMethodLabels, ReminderEntry
							.valueOf(GeneralPreferences.REMINDER_DEFAULT_TIME),
					mModel.mCalendarMaxReminders, null);
			isOpen = false;
			mReminderMe.setImageResource(R.drawable.aurora_btn_clock_close);
		} else {
			EventViewUtils.addReminder(mActivity, mScrollView, this,
					mReminderItems, mReminderMinuteValues,
					mReminderMinuteLabels, mReminderMethodValues,
					mReminderMethodLabels,
					ReminderEntry.valueOf(mDefaultReminderMinutes),
					mModel.mCalendarMaxReminders, null);
			isOpen = true;
			mReminderMe.setImageResource(R.drawable.aurora_btn_clock_open);
		}
		updateRemindersVisibility(mReminderItems.size());
		EventViewUtils.updateAddReminderButton(mActivity, mView,
				mReminderItems, mModel.mCalendarMaxReminders);
	}

	// From com.google.android.gm.ComposeActivity
//	private MultiAutoCompleteTextView initMultiAutoCompleteTextView(
//			RecipientEditTextView list) {
//		if (ChipsUtil.supportsChipsUi()) {
//			mAddressAdapter = new RecipientAdapter(mActivity);
//			list.setAdapter((BaseRecipientAdapter) mAddressAdapter);
//			list.setOnFocusListShrinkRecipients(false);
//			Resources r = mActivity.getResources();
//			Bitmap def = BitmapFactory.decodeResource(r,
//					R.drawable.ic_contact_picture);
//			/*list.setChipDimensions(r.getDrawable(R.drawable.chip_background),
//					r.getDrawable(R.drawable.chip_background_selected),
//					r.getDrawable(R.drawable.chip_background_invalid),
//					r.getDrawable(R.drawable.chip_delete), def,
//					R.layout.more_item, R.layout.chips_alternate_item,
//					r.getDimension(R.dimen.chip_height),
//					r.getDimension(R.dimen.chip_padding),
//					r.getDimension(R.dimen.chip_text_size),
//					R.layout.copy_chip_dialog_layout);*/
//		} else {
//			mAddressAdapter = new EmailAddressAdapter(mActivity);
//			list.setAdapter((EmailAddressAdapter) mAddressAdapter);
//		}
//		list.setTokenizer(new Rfc822Tokenizer());
//		list.setValidator(mEmailValidator);
//
//		// NOTE: assumes no other filters are set
//		list.setFilters(sRecipientFilters);
//
//		return list;
//	}

	/**
	 * From com.google.android.gm.ComposeActivity Implements special address
	 * cleanup rules: The first space key entry following an "@" symbol that is
	 * followed by any combination of letters and symbols, including one+ dots
	 * and zero commas, should insert an extra comma (followed by the space).
	 */
	private static InputFilter[] sRecipientFilters = new InputFilter[] { new Rfc822InputFilter() };

	private void setDate(TextView view, TextView timeTextView, long millis) {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
			    | DateUtils.FORMAT_ABBREV_MONTH;

		// Unfortunately, DateUtils doesn't support a timezone other than the
		// default timezone provided by the system, so we have this ugly hack
		// here to trick it into formatting our time correctly. In order to
		// prevent all sorts of craziness, we synchronize on the TimeZone class
		// to prevent other threads from reading an incorrect timezone from
		// calls to TimeZone#getDefault()
		// TODO fix this if/when DateUtils allows for passing in a timezone
		String dateString = null;
		String timeString = null;
		synchronized (TimeZone.class) {
			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));

			// /@}
			Date date = new Date(millis);
			SimpleDateFormat formatter, formatter2;
			formatter = new SimpleDateFormat(" EEEE");
			formatter2 = new SimpleDateFormat("HH:mm");
			dateString = formatter.format(date);
			timeString = formatter2.format(date);
//			if (TextUtils.isEmpty(dateString)) {
				dateString = DateUtils.formatDateTime(mActivity, millis, flags)+" "+DateUtils.formatDateTime(mActivity, millis, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY);
//			}
			// setting the default back to null restores the correct behavior
			TimeZone.setDefault(null);
		}
		// Gionee <jiating><2013-07-01> modify for CR00819533 begin
		Log.i("jiating", "AuroraEditEventView......setDate...dateString="
				+ dateString);
		if (dateString.contains(mActivity
				.getString(R.string.gn_agenda_time_noon))) {

			dateString = dateString.replace(
					mActivity.getString(R.string.gn_agenda_time_noon),
					mActivity.getString(R.string.gn_agenda_time_noon_replace));
			Log.i("jiating", "AuroraEditEventView..contains..dateString="
					+ dateString);
		}

		// Gionee <jiating><2013-07-01> modify for CR00819533 end
		view.setText(dateString);
		timeTextView.setText(timeString);
	}

	private void setTime(TextView view, long millis) {
		int flags = DateUtils.FORMAT_SHOW_TIME;
		if (DateFormat.is24HourFormat(mActivity)) {
			flags |= DateUtils.FORMAT_24HOUR;
		}

		// Unfortunately, DateUtils doesn't support a timezone other than the
		// default timezone provided by the system, so we have this ugly hack
		// here to trick it into formatting our time correctly. In order to
		// prevent all sorts of craziness, we synchronize on the TimeZone class
		// to prevent other threads from reading an incorrect timezone from
		// calls to TimeZone#getDefault()
		// TODO fix this if/when DateUtils allows for passing in a timezone
		String timeString;
		synchronized (TimeZone.class) {
			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
			timeString = DateUtils.formatDateTime(mActivity, millis, flags);
			TimeZone.setDefault(null);
		}
		view.setText(timeString);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// This is only used for the Calendar spinner in new events, and only
		// fires when the
		// calendar selection changes or on screen rotation
		Log.i("jiating", "CalendarsAdapter......onItemSelected");
		Cursor c = (Cursor) parent.getItemAtPosition(position);
		if (c == null) {
			// TODO: can this happen? should we drop this check?
			Log.w(TAG, "Cursor not set on calendar item");
			return;
		}
		Statistics.onEvent(mActivity, Statistics.EDIT_EVENT_ACOUNT);
		int colorColumn = c.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR);
		int color = c.getInt(colorColumn);
		int displayColor = Utils.getDisplayColorFromColor(color);

		if (mIsMultipane) {

		} else {
			// mCalendarSelectorGroup.setBackgroundColor(displayColor);
		}

		// Do nothing if the selection didn't change so that reminders will not
		// get lost
		int idColumn = c.getColumnIndexOrThrow(Calendars._ID);
		long calendarId = c.getLong(idColumn);
		if (calendarId == mModel.mCalendarId) {
			return;
		}
		mModel.mCalendarId = calendarId;
		mModel.mCalendarColor = color;
		// Update the max/allowed reminders with the new calendar properties.
		int maxRemindersColumn = c
				.getColumnIndexOrThrow(Calendars.MAX_REMINDERS);
		Log.i("GNEditEvent", "GNEditEvent-maxRemindersColumn == "
				+ maxRemindersColumn);
		mModel.mCalendarMaxReminders = c.getInt(maxRemindersColumn);
		int allowedRemindersColumn = c
				.getColumnIndexOrThrow(Calendars.ALLOWED_REMINDERS);
		mModel.mCalendarAllowedReminders = c.getString(allowedRemindersColumn);
		int allowedAttendeeTypesColumn = c
				.getColumnIndexOrThrow(Calendars.ALLOWED_ATTENDEE_TYPES);
		mModel.mCalendarAllowedAttendeeTypes = c
				.getString(allowedAttendeeTypesColumn);
		int allowedAvailabilityColumn = c
				.getColumnIndexOrThrow(Calendars.ALLOWED_AVAILABILITY);
		mModel.mCalendarAllowedAvailability = c
				.getString(allowedAvailabilityColumn);

		// Discard the current reminders and replace them with the model's
		// default reminder set.
		// We could attempt to save & restore the reminders that have been
		// added, but that's
		// probably more trouble than it's worth.
		mModel.mReminders.clear();
		mModel.mReminders.addAll(mModel.mDefaultReminders);
		mModel.mHasAlarm = mModel.mReminders.size() != 0;

		// Update the UI elements.
		mReminderItems.clear();
		// LinearLayout reminderLayout = (LinearLayout) mScrollView
		// .findViewById(R.id.reminder_items_container);
		// reminderLayout.removeAllViews();
		prepareReminders();
		// /M: #extension# #PC Sync# @{
		mExt.onAccountItemSelected(c);
		// /@}

	}

	/**
	 * Checks if the start and end times for this event should be displayed in
	 * the Calendar app's time zone as well and formats and displays them.
	 */
	private void updateHomeTime() {
		String tz = Utils.getTimeZone(mActivity, null);
		if (!TextUtils.equals(tz, mTimezone)
				&& mModification != EditEventHelper.MODIFY_UNINITIALIZED) {
			int flags = DateUtils.FORMAT_SHOW_TIME;
			boolean is24Format = DateFormat.is24HourFormat(mActivity);
			if (is24Format) {
				flags |= DateUtils.FORMAT_24HOUR;
			}
			long millisStart = mStartTime.toMillis(false);
			long millisEnd = mEndTime.toMillis(false);

			boolean isDSTStart = mStartTime.isDst != 0;
			boolean isDSTEnd = mEndTime.isDst != 0;

			// First update the start date and times
			String tzDisplay = TimeZone.getTimeZone(tz).getDisplayName(
					isDSTStart, TimeZone.LONG, Locale.getDefault());
			StringBuilder time = new StringBuilder();

			mSB.setLength(0);
			time.append(
					DateUtils.formatDateRange(mActivity, mF, millisStart,
							millisStart, flags, tz)).append(" ")
					.append(tzDisplay);

			flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_YEAR
					| DateUtils.FORMAT_SHOW_WEEKDAY;
			mSB.setLength(0);
			// Make any adjustments needed for the end times
			if (isDSTEnd != isDSTStart) {
				tzDisplay = TimeZone.getTimeZone(tz).getDisplayName(isDSTEnd,
						TimeZone.LONG, Locale.getDefault());
			}
			flags = DateUtils.FORMAT_SHOW_TIME;
			if (is24Format) {
				flags |= DateUtils.FORMAT_24HOUR;
			}

			// Then update the end times
			time.setLength(0);
			mSB.setLength(0);
			time.append(
					DateUtils.formatDateRange(mActivity, mF, millisEnd,
							millisEnd, flags, tz)).append(" ")
					.append(tzDisplay);

			flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_YEAR
					| DateUtils.FORMAT_SHOW_WEEKDAY;
			mSB.setLength(0);

		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	// ////////////////////////////////////////////////////////////////////////
	// /M: MTK code below

	// /M: the extension of this view @{
	private IEditEventViewExt mExt;

	// /@}

	@Override
	public void resetDateButton() {
		// Gionee <jiating><2013-07-03> modify for CR00830388 begin
		// setDate(mStartDateButton, mStartTime.toMillis(false /* use isDst
		// */));
		// setDate(mEndDateButton, mEndTime.toMillis(false /* use isDst */));
		setDate(mStartDateText, mStartTimeText, mStartTime.toMillis(false /*
																		 * use
																		 * isDst
																		 */));
		setDate(mEndDateText, mEndTimeText,
				mEndTime.toMillis(false /* use isDst */));
		// Gionee <jiating><2013-07-03> modify for CR00830388 end
	}

	@Override
	public CalendarEventModel getModel() {
		return mModel;
	}

	@Override
	public void setAttendeesGroupVisibility(int visible) {
		mAttendeesGroup.setVisibility(visible);
	}

	@Override
	public OnDateSetListener getOnDateSetListener(View view) {
		return null;
	}

	/**
	 * M: View is not under the control of Fragment's life cycle. so if we want
	 * to do some thing in the life cycle, we have to define a method like this
	 * to handle the requirement. for example, the language changed
	 * unexpectedly.
	 */
	public void doOnResume() {

		if (mModel != null) {
			updateRepeatsSpinnerStatus(mModel.mStart, mModel.mEnd);
		}
	}

	/**
	 * M: when the time is illegal, the button should display a hint
	 */
	private void setTimeZoneHint() {
		LogUtil.w(TAG,
				"timezone is not permitted, Default string is set to TimeZone button");
		mTimezoneTextView.setHint(R.string.timezone_label);

	}

	// /M:Repeate evnet max cross hour @{
	private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	private static final long REPEAT_EVENT_MAX_DURATION = DAY_IN_MILLIS;
	private static final int EVENT_ONLY_ONCE_INDEX = 0;

	// /@}

	/**
	 * M:If the time cross ,it cann't set to be repeate event
	 * 
	 * @param startMillis
	 * @param endMillis
	 * @return if the event if repeat event,will return the right endMillis that
	 *         greater 24 hour than startMillis
	 */
	public long updateRepeatsSpinnerStatus(long startMillis, long endMillis) {
		boolean isInRange = endMillis <= startMillis
				+ REPEAT_EVENT_MAX_DURATION ? true : false;
		if (!isInRange) {
			Log.i(TAG, "endMillis is greater 24 hour than startMillis.");
			int selectionIndex = mRepeatsSpinner.getSelectedItemPosition();
			if (EVENT_ONLY_ONCE_INDEX == selectionIndex) {
				enableRepeatsSpinner(false);
			} else {
				endMillis = startMillis + REPEAT_EVENT_MAX_DURATION;
			}
		} else if (mModel.mOriginalSyncId == null) {
			enableRepeatsSpinner(true);
		}
		return endMillis;
	}

	/**
	 * M:Set the view only can be selected once event set the spinner enable
	 * 
	 * @param enable
	 *            true the enable,or false.
	 */
	private void enableRepeatsSpinner(boolean enable) {
		mRepeatsSpinner.setEnabled(enable);
		mRepeatsSpinner.setClickable(enable);
		mRepeatsSpinner.setFocusable(enable);
	}

	// /M:Max Input length @{
	private static final int TITLE_MAX_LENGTH = 40;
	private static final int LOCATION_MAX_LENGTH = 2000;
	private static final int DESCRIPTION_MAX_LENGTH = 60;

	// /@}

	// /M: @{
	/**
	 * DialogManager is used to manage all potential dialogs of this
	 * EditEventView It remembered whether there exists a dialog. Use it to
	 * determine whether a dialog can be shown.
	 */
	private class DialogManager implements DialogInterface.OnDismissListener,
			DialogInterface.OnShowListener {

		private boolean mIsAnyDialogShown = false;

		public boolean isAnyDialogShown() {
			return mIsAnyDialogShown;
		}

		public void dialogShown() {
			if (isAnyDialogShown()) {
				LogUtil.w(TAG,
						"There is already a dialog shown, but another dialog is "
								+ "going to show.");
			}
			mIsAnyDialogShown = true;
		}

		private void dialogDismissed() {
			if (!isAnyDialogShown()) {
				LogUtil.w(TAG,
						"There is no dialog shown, but some dialog dismissed.");
			}
			mIsAnyDialogShown = false;

			// /M: keep AuroraDatePickerDialog and AuroraTimePickerDialog when
			// rotate device
			// @{
			LogUtil.d(DATE_TIME_TAG, "dialog dismissed.");
			setDateTimeViewId(ID_INVALID);
			// /@}
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			dialogDismissed();
		}

		@Override
		public void onShow(DialogInterface dialog) {
			dialogShown();
		}
	}

	// / @}

	// /M: give a tip when contain valid attendees.@{
	private void ToastMsg(int resId) {
		if (mToast == null) {
			mToast = Toast.makeText(mActivity, resId, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(resId);
		}
		mToast.show();
	}

	/**
	 * Checks whether a string attendee address has invalid address. M: change
	 * from EditEventHeper.getAddressesFromList()
	 */
	private boolean isHasInvalidAddress(String list, Rfc822Validator validator) {
		LinkedHashSet<Rfc822Token> addresses = new LinkedHashSet<Rfc822Token>();
		Rfc822Tokenizer.tokenize(list, addresses);
		if (validator == null) {
			return false;
		}

		boolean isHasInvalidAttendee = false;
		Iterator<Rfc822Token> addressIterator = addresses.iterator();
		while (addressIterator.hasNext()) {
			Rfc822Token address = addressIterator.next();
			if (!validator.isValid(address.getAddress())) {
				isHasInvalidAttendee = true;
			}
		}
		return isHasInvalidAttendee;
	}

	// /M:#Guest#@{
	private void setAttendeesGroupVisibility(String accountType) {
		if (ACCOUNT_TYPE_LOCAL.equals(accountType)) {
			mAttendeesGroup.setVisibility(View.GONE);
		} else {
			mAttendeesGroup.setVisibility(View.VISIBLE);
		}
	}

	// /@}
	private static final String ACCOUNT_TYPE_LOCAL = "local";

	// /M: keep AuroraDatePickerDialog and AuroraTimePickerDialog when rotate
	// device @{
	private static final String DATE_TIME_TAG = TAG + "::date_time_debug_tag";

	// ids of the date & time buttons
	private static final int ID_START_DATE = 1;
	private static final int ID_END_DATE = 2;
	private static final int ID_START_TIME = 3;
	private static final int ID_END_TIME = 4;
	public static final int ID_INVALID = 0;

	// //////////////////////////////////////////////////////////////////////////
	// these three methods expose dialog manager functionalities
	public boolean isAnyDialogShown() {
		LogUtil.d(DATE_TIME_TAG, "isAnyDialogShown()");

		return mDialogManager.isAnyDialogShown();
	}

	public void setDialogShown() {
		LogUtil.d(DATE_TIME_TAG, "getDateTimeOnDismissListener()");

		mDialogManager.onShow(null);
	}

	// when a dialog is dismissed, this MUST be called to reset the
	// dialog manager to correct state
	public OnDismissListener getDateTimeOnDismissListener() {
		LogUtil.d(DATE_TIME_TAG, "getDateTimeOnDismissListener()");

		return mDialogManager;
	}

	// //////////////////////////////////////////////////////////////////////////

	/*
	 * Set identifier in EditEventActivity to hold which button is clicked. Then
	 * we can restore the dialog correctly.
	 */
	private void setDateTimeViewId(int id) {
		if (mActivity instanceof EditEventActivity) {
			LogUtil.d(DATE_TIME_TAG, "setDateTimeViewId(), id: " + id);

			EditEventActivity eea = (EditEventActivity) mActivity;
			eea.setDateTimeViewId(id);
		} else if (mActivity instanceof AuroraEditEventActivity) {
			LogUtil.d(DATE_TIME_TAG, "setDateTimeViewId(), id: " + id);

			AuroraEditEventActivity eea = (AuroraEditEventActivity) mActivity;
			eea.setDateTimeViewId(id);
		}
	}

	/**
	 * Get the View identified by id.
	 * 
	 * @param id
	 *            identifier of the view
	 * @return a View if exists, null otherwise
	 */
	public View getDateTimeView(int id) {
		LogUtil.d(DATE_TIME_TAG, "getDateTimeView(), id: " + id);
		// Gionee <jiating><2013-07-03> modify for CR00830388 begin
		if (id == ID_START_DATE) {
			// return mStartDateButton;
			return mStartDataView;
		}

		if (id == ID_END_DATE) {
			// return mEndDateButton;
			return mEndDataView;
		}
		// Gionee <jiating><2013-07-03> modify for CR00830388 end

		return null;
	}

	// /@}
	
	private class DateTimeListener implements
			AuroraWeekPickerDialog.onWeekSetListener {

		private View mView;

		public DateTimeListener(View view) {
			mView = view;
		}

		@Override
		public void onWeekSet(Calendar calendar) {
			Time startTime = mStartTime;
			Time endTime = mEndTime;

			// Cache the start and end millis so that we limit the number
			// of calls to normalize() and toMillis(), which are fairly
			// expensive.
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			long startMillis = mStartTime.normalize(true);
			long endMillis = endTime.normalize(true);
			// Gionee <jiating><2013-07-03> modify for CR00830388 begin
			if (mView == mStartDataView) {
				// The start time was changed.
				long dateDuration = endMillis - startMillis;

				startTime.set(calendar.getTimeInMillis());

				startMillis = startTime.normalize(true);

				// Also update the end time to keep the duration constant.
				endTime.set(startMillis + dateDuration);
				endMillis = endTime.normalize(true);

				// If the start date has changed then update the repeats.
				populateRepeats();

			} else if (mView == mEndDataView) {
				// Gionee <jiating><2013-07-03> modify for CR00830388 end
				// The end time was changed.
				startMillis = startTime.toMillis(true);
				endTime.set(calendar.getTimeInMillis());

				// Move to the start time if the end time is before the start
				// time.
				if (endTime.before(startTime)) {
					endTime.monthDay = startTime.monthDay + 1;
				}
				// Call populateTimezone if we support end time zone as well

				endMillis = endTime.normalize(true);

				// Do not allow an event to have an end time before the start
				// time.
				if (endTime.before(startTime)) {
					endTime.set(startTime);
					endMillis = startMillis;
				}
			}

			endMillis = endTime.normalize(true);
			// /M:Set the event type wether activate by the start and end
			// Millis@{
			endMillis = updateRepeatsSpinnerStatus(startMillis, endMillis);
			endTime.set(endMillis);
			// /@}
			// Gionee <jiating><2013-07-03> modify for CR00830388 begin
			// setDate(mEndDateButton, endMillis);
			// setDate(mStartDateButton, startMillis);
			setDate(mStartDateText, mStartTimeText, startMillis);
			setDate(mEndDateText, mEndTimeText, endMillis);
			// Gionee <jiating><2013-07-03> modify for CR00830388 end
			// reset
			// updateHomeTime();
		}

		@Override
		public void onWeekSet(AuroraNumberPicker arg0, AuroraTimePicker arg1,
				String arg2, int arg3, int arg4) {
			// TODO Auto-generated method stub

		}

	}

	public void setEditTime(final Context context, long timeMillis,
			long minTimeMillis, View view) {

		Log.d("jiating", "AlarmUtils------set alarm  info: timeMillis"
				+ timeMillis);
		Calendar cal;

		cal = Calendar.getInstance();
		cal.setTimeInMillis(timeMillis);

		AuroraWeekPickerDialog pickerDialog;
		DateTimeListener dateTimeListener = new DateTimeListener(view);
		pickerDialog = new AuroraWeekPickerDialog(mActivity, dateTimeListener, cal, minTimeMillis);
		if (pickerDialog != null) {
			hideInputMethod();
			pickerDialog.show();
		}

	}

	public void setTitleAndTime(String title, long time) {
		Time startTime = mStartTime;
		Time endTime = mEndTime;
		mTitleTextView.setText(title);
		if (time != 0) {
			long startMillis = mStartTime.normalize(true);
			long endMillis = mEndTime.normalize(true);

			// The start time was changed.
			long dateDuration = endMillis - startMillis;

			startMillis = time;

			endMillis = startMillis + dateDuration;

			// If the start date has changed then update the repeats.
			populateRepeats();

			// /M:Set the event type wether activate by the start and end
			// Millis@{
			endMillis = updateRepeatsSpinnerStatus(startMillis, endMillis);

			// /@}
			startTime.set(startMillis);
			endTime.set(endMillis);
			// Gionee <jiating><2013-07-03> modify for CR00830388 begin
			// setDate(mEndDateButton, endMillis);
			// setDate(mStartDateButton, startMillis);
			setDate(mStartDateText, mStartTimeText, startMillis);
			setDate(mEndDateText, mEndTimeText, endMillis);
			// Gionee <jiating><2013-07-03> modify for CR00830388 end
		}
	}

	private TextWatcher titleTextWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			Log.d("jiating", "beforeTextChanged--------------->");
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			String temp1 = s.toString().trim().replace("\n", "")
					.replace("\\s", "");

			Log.d("jiating", "onTextChanged--------------->" + s
					+ "....s.size=" + temp1.length() + ">>>>count=" + count
					+ ">>>>start=" + start + ">>>>>before=" + before);
			if (temp1.length() > 0) {
				
			} else if (temp1.length() <= 0
					&& descriptionEditText.getText().toString().trim()
							.replace("\n", "").replace("\\s", "").length() <= 0) {
				
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			Log.d("jiating", "afterTextChanged--------------->");
		}
	};

	private TextWatcher descriptionTextWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			Log.d("jiating", "beforeTextChanged--------------->");
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			String temp1 = s.toString().trim().replace("\n", "")
					.replace("\\s", "");

			Log.d("jiating", "onTextChanged--------------->" + s
					+ "....s.size=" + temp1.length() + ">>>>count=" + count
					+ ">>>>start=" + start + ">>>>>before=" + before);
			if (temp1.length() > 0) {
				
			} else if (temp1.length() <= 0
					&& mTitleTextView.getText().toString().trim()
							.replace("\n", "").replace("\\s", "").length() <= 0) {
				
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			Log.d("jiating", "afterTextChanged--------------->");
		}
	};

	public void setRepeat(int position) {
		mPosition = position;
		TextView tView;
		tView = (TextView) mRepeatView.findViewById(R.id.repeat_content);
		if (tView == null || repeatArray == null) return;
		tView.setText(repeatArray.get(position));
		mRepeatsSpinner.setSelection(position);
	}

	int mPosition;
	
	private void showInputMethod() {
        final InputMethodManager imm = (InputMethodManager)
                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
        	 (new Handler()).postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
        		 
        	 },100);
            
        }
    }
	
	private void hideInputMethod() {
		 final InputMethodManager imm = (InputMethodManager)
	                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null&&mActivity != null) {
	        	final View focusedView = mActivity.getCurrentFocus();
	            if (focusedView != null) {
	            	
	                imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
	                focusedView.clearFocus();
	            }
	        }
	}
	
	public AuroraEditText getTitleEditText() {
		return mTitleTextView;
	}
	
	public boolean isTitleEmpty() {
        if (mTitleTextView != null && mTitleTextView.getText() != null && mTitleTextView.getText().length() > 0) {
            return false;
        }

        return true;
    }
	
	public boolean isDescriptionEmpty() {
        if (descriptionEditText != null && descriptionEditText.getText() != null && descriptionEditText.getText().length() > 0) {
            return false;
        }

        return true;
    }
	
	public String getEventTitle() {
		return mTitleTextView.getText() == null? null : mTitleTextView.getText().toString().trim();
	}
	
	public String getEventDescription() {
		return descriptionEditText.getText() == null? null : descriptionEditText.getText().toString().trim();
	}

}
