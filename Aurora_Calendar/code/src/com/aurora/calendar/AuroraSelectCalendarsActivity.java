package com.aurora.calendar;

import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.Settings;
import android.util.Log;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;

import com.android.calendar.R;
import com.android.calendar.Utils;

public class AuroraSelectCalendarsActivity extends AuroraPreferenceActivity implements OnPreferenceChangeListener {

    private static final String TAG = "SelectCalendars";
    private static final int KEY_ADD_ACCOUNT = 9;
    private static final String KEY_LOCAL_CALENDAR = "local_calendar";

    private static final String ACCOUNT_UNIQUE_KEY = "ACCOUNT_KEY";
    private static final String ACCOUNT_SELECTION = Calendars.ACCOUNT_TYPE + "!=?";
    private static final String ACCOUNT_ORDERBY = Calendars.ACCOUNT_TYPE + " DESC," + Calendars.ACCOUNT_NAME + " ASC";
    private static final String[] ACCOUNT_PROJECTION = new String[] {
        Calendars._ID,
        Calendars.ACCOUNT_TYPE,
        Calendars.ACCOUNT_NAME,
        Calendars.ACCOUNT_TYPE + " || " + Calendars.ACCOUNT_NAME + " AS " + ACCOUNT_UNIQUE_KEY,
    };

    private static final String IS_PRIMARY = "\"primary\"";
    private static final String CALENDARS_SELECTION = Calendars.ACCOUNT_NAME + "=?"
            + " AND " + Calendars.ACCOUNT_TYPE + "=?";
    private static final String CALENDARS_ORDERBY = IS_PRIMARY + " DESC,"
            + Calendars.CALENDAR_DISPLAY_NAME + " COLLATE NOCASE";
    private static final String[] CALENDARS_PROJECTION = new String[] {
        Calendars._ID,
        Calendars.ACCOUNT_NAME,
        Calendars.OWNER_ACCOUNT,
        Calendars.CALENDAR_DISPLAY_NAME,
        Calendars.CALENDAR_COLOR,
        Calendars.VISIBLE,
        Calendars.SYNC_EVENTS,
        "(" + Calendars.ACCOUNT_NAME + "=" + Calendars.OWNER_ACCOUNT + ") AS " + IS_PRIMARY,
    };

    private static final int ID_COLUMN = 0;
    private static final int ACCOUNT_COLUMN = 1;
    private static final int OWNER_COLUMN = 2;
    private static final int NAME_COLUMN = 3;
    private static final int COLOR_COLUMN = 4;
    private static final int SELECTED_COLUMN = 5;
    private static final int SYNCED_COLUMN = 6;
    private static final int PRIMARY_COLUMN = 7;

    private Context mContext;
    private ContentResolver mResolver;
    private AuroraSwitchPreference mLocalCalendarSwitch;

    private Bitmap mColorBitmap;
    private Canvas mColorCanvas;
    private Paint mColorPaint;

    private Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap<String, AuthenticatorDescription>();
    private AuthenticatorDescription[] mAuthDescs;

    private HashMap<String, Boolean> mTypeIsExist = new HashMap<String, Boolean>();
    private HashMap<String, Long> mCalendarIds = new HashMap<String, Long>();

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        addPreferencesFromResource(R.xml.aurora_select_calendars_preferences);
        initActionBar();

        mContext = this;
        mResolver = mContext.getContentResolver();

        mColorPaint = new Paint();
        mColorPaint.setAntiAlias(true);

        mAuthDescs = AccountManager.get(mContext).getAuthenticatorTypes();
        for (int i = 0; i < mAuthDescs.length; i++) {
            mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
        }

        mLocalCalendarSwitch = (AuroraSwitchPreference) findPreference(KEY_LOCAL_CALENDAR);

        Cursor localCalendarCursor = mResolver.query(
                Calendars.CONTENT_URI,
                new String[] { Calendars._ID, Calendars.VISIBLE },
                Calendars.ACCOUNT_TYPE + "=?",
                new String[] { CalendarContract.ACCOUNT_TYPE_LOCAL },
                null);
        if (localCalendarCursor != null && localCalendarCursor.moveToFirst()) {
            int visibleColumn = localCalendarCursor.getColumnIndexOrThrow(Calendars.VISIBLE);
            boolean selected = localCalendarCursor.getInt(visibleColumn) == 1;
            mLocalCalendarSwitch.setChecked(selected);
            mLocalCalendarSwitch.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mLocalCalendarSwitch);
        }

        if (localCalendarCursor != null) {
        	localCalendarCursor.close();
        }

        Cursor cursor = mResolver.query(
                Calendars.CONTENT_URI,
                ACCOUNT_PROJECTION,
                ACCOUNT_SELECTION + "1) GROUP BY (" + ACCOUNT_UNIQUE_KEY,
                new String[] { CalendarContract.ACCOUNT_TYPE_LOCAL },
                ACCOUNT_ORDERBY);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                int accountColumn = cursor.getColumnIndexOrThrow(Calendars.ACCOUNT_NAME);
                int accountTypeColumn = cursor.getColumnIndexOrThrow(Calendars.ACCOUNT_TYPE);
                String account = cursor.getString(accountColumn);
                String accountType = cursor.getString(accountTypeColumn);

                if (mTypeIsExist.containsKey(accountType) && mTypeIsExist.get(accountType)) {
                	getPreferenceScreen().addPreference(new AuroraPreferenceCategory(mContext));
                } else {
                    mTypeIsExist.put(accountType, true);
                    CharSequence accountLabel = getLabelForType(accountType);

                    AuroraPreferenceCategory accountTypeCategory = new AuroraPreferenceCategory(mContext);
                    if (accountLabel != null) {
                        accountTypeCategory.setTitle(accountLabel);
                    } else {
                        accountTypeCategory.setTitle(accountType);
                    }
                    getPreferenceScreen().addPreference(accountTypeCategory);
                }

                AuroraPreference accountPrerence = new AuroraPreference(mContext);
                accountPrerence.setTitle(account);
                accountPrerence.setSelectable(false);
                getPreferenceScreen().addPreference(accountPrerence);

                Cursor childCursor = mResolver.query(
                        Calendars.CONTENT_URI,
                        CALENDARS_PROJECTION,
                        CALENDARS_SELECTION,
                        new String[] { account, accountType},
                        CALENDARS_ORDERBY);
                if (childCursor != null && childCursor.getCount() > 0) {
                    childCursor.moveToPosition(-1);
                    while (childCursor.moveToNext()) {
                        long id = childCursor.getLong(ID_COLUMN);
                        String name = childCursor.getString(NAME_COLUMN);
                        String owner = childCursor.getString(OWNER_COLUMN);
                        int color = childCursor.getInt(COLOR_COLUMN);
                        boolean selected = childCursor.getInt(SELECTED_COLUMN) == 1;

                        mCalendarIds.put(name + "#" + owner, id);

                        /*mColorPaint.setColor(color);
                        mColorBitmap = Bitmap.createBitmap(42, 42, Config.ARGB_8888);
                        mColorCanvas = new Canvas(mColorBitmap);
                        mColorCanvas.drawCircle(21, 21, 21, mColorPaint);
                        BitmapDrawable colorIcon = new BitmapDrawable(mColorBitmap);*/
                        CycleColorDrawable colorIcon = new CycleColorDrawable(color);
                        colorIcon.setCenter(8, 8);
                        colorIcon.setRadius(7);

                        AuroraSelectCalendarPreference calendarPreference = new AuroraSelectCalendarPreference(mContext);
                        calendarPreference.setKey(name + "#" + owner);
                        calendarPreference.setIcon(colorIcon);
                        calendarPreference.setTitle(name);
                        calendarPreference.setChecked(selected);
                        calendarPreference.setOnPreferenceChangeListener(this);
                        getPreferenceScreen().addPreference(calendarPreference);
                    }
                }

                if (childCursor != null) {
                    childCursor.close();
                }
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void initActionBar() {
        addAuroraActionBarItem(AuroraActionBarItem.Type.Add, KEY_ADD_ACCOUNT);
        getAuroraActionBar().setTitle(R.string.aurora_account_management);
        getAuroraActionBar().setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {
            public void onAuroraActionBarItemClicked(int itemId) {
                switch (itemId) {
                case KEY_ADD_ACCOUNT:
                    doAddAccount();
                    break;
                default:
                    break;
                }
            }
        });
    }

    private void doAddAccount() {
        Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        final String[] array = { "com.android.calendar" };
        nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, array);
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(nextIntent);
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference mPreference, Object newValue) {
        Boolean selected = (Boolean) newValue;
        ContentValues values = new ContentValues();
        values.put(Calendars.VISIBLE, selected ? 1 : 0);

        String selection = null;
        if (KEY_LOCAL_CALENDAR.equals(mPreference.getKey())) {
            selection = Calendars.ACCOUNT_TYPE + "='" + CalendarContract.ACCOUNT_TYPE_LOCAL + "' AND " +
                    Calendars.ACCOUNT_NAME + "!='" + Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME + "'";
        } else {
            selection = Calendars._ID + "=" + mCalendarIds.get(mPreference.getKey());
        }

        mResolver.update(Calendars.CONTENT_URI, values, selection, null);

        return true;
    }

    /**
     * Gets the label associated with a particular account type. If none found, return null.
     * @param accountType the type of account
     * @return a CharSequence for the label or null if one cannot be found.
     */
    protected CharSequence getLabelForType(final String accountType) {
        CharSequence label = null;
        if (mTypeToAuthDescription.containsKey(accountType)) {
             try {
                 AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                 Context authContext = this.createPackageContext(desc.packageName, 0);
                 label = authContext.getResources().getText(desc.labelId);
             } catch (PackageManager.NameNotFoundException e) {
                 Log.w(TAG, "No label for account type " + ", type " + accountType);
             }
        }
        return label;
    }
}