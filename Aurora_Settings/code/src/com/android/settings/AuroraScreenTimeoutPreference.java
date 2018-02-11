package com.android.settings;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import java.util.ArrayList;
import java.util.HashMap;
import com.android.settings.R;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import aurora.preference.AuroraPreference;

public class AuroraScreenTimeoutPreference extends AuroraPreference {

    private static final String TAG = "AuroraScreenTimeout";
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 15000;

	private Context mContext;
	private ContentResolver mResolver;

	private static final int PROGRESS_ONE = 0;
    private static final int PROGRESS_TWO = 8;
    private static final int PROGRESS_THREE = 17;
    private static final int PROGRESS_FOUR = 25;
    private static final int PROGRESS_FIVE = 34;
    private static final int PROGRESS_SIX = 42;
    private static final int PROGRESS_SEVEN = 50;
    private static final int PROGRESS_EIGHT = 58;
    private static final int PROGRESS_NINE = 66;
    private static final int PROGRESS_TEN = 75;
    private static final int PROGRESS_ELEVEN = 83;
    private static final int PROGRESS_TWELVE = 92;
    private static final int PROGRESS_THRITEEN = 100;

    private String[] mScreenTimeoutEntries;
    private String[] mScreenTimeoutValues;

//    private SeekBar mSeekBar;
    private ArrayList<HashMap<String, Object>> listTimeouts;
    private GridView mGridview;

	public AuroraScreenTimeoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mResolver = context.getContentResolver();
        mScreenTimeoutEntries = context.getResources().getStringArray(R.array.aurora_screen_timeout_entries);
        mScreenTimeoutValues = context.getResources().getStringArray(R.array.aurora_screen_timeout_values);
        
     
	}

	@Override
    protected void onBindView(View view) {
        super.onBindView(view);        
        
        

        /*mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setMax(PROGRESS_THRITEEN);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        readScreenTimeout();*/
    }

   /* OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            refreshSeekBarState(seekBar);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
    };

    private void refreshSeekBarState(SeekBar seekBar) {
        int currentProgress = seekBar.getProgress();
        int progress = 0;
        if (currentProgress < PROGRESS_TWO) {
            progress = PROGRESS_ONE;
        } else if (currentProgress >= PROGRESS_TWO && currentProgress < PROGRESS_FOUR) {
            progress = PROGRESS_THREE;
        } else if (currentProgress >= PROGRESS_FOUR && currentProgress < PROGRESS_SIX) {
            progress = PROGRESS_FIVE;
        } else if (currentProgress >= PROGRESS_SIX && currentProgress < PROGRESS_EIGHT) {
            progress = PROGRESS_SEVEN;
        } else if (currentProgress >= PROGRESS_EIGHT && currentProgress < PROGRESS_TEN) {
            progress = PROGRESS_NINE;
        } else if (currentProgress >= PROGRESS_TEN && currentProgress < PROGRESS_TWELVE) {
            progress = PROGRESS_ELEVEN;
        } else {
            progress = PROGRESS_THRITEEN;
        }
        seekBar.setProgress(progress);
        writeScreenTimeout(progress);
    }

    private void readScreenTimeout() {;
        final int currentTimeout = Settings.System.getInt(mResolver, SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);

        int index = 0;
        int progress = 0;
        if (currentTimeout == Integer.parseInt(mScreenTimeoutValues[0])) {
            index = 0;
            progress = PROGRESS_ONE;
        } else if (currentTimeout ==  Integer.parseInt(mScreenTimeoutValues[1])) {
        	index = 1;
            progress = PROGRESS_THREE;
        } else if (currentTimeout ==  Integer.parseInt(mScreenTimeoutValues[2])) {
        	index = 2;
            progress = PROGRESS_FIVE;
        } else if (currentTimeout ==  Integer.parseInt(mScreenTimeoutValues[3])) {
        	index = 3;
            progress = PROGRESS_SEVEN;
        } else if (currentTimeout ==  Integer.parseInt(mScreenTimeoutValues[4])) {
        	index = 4;
            progress = PROGRESS_NINE;
        } else if (currentTimeout ==  Integer.parseInt(mScreenTimeoutValues[5])) {
        	index = 5;
            progress = PROGRESS_ELEVEN;
        } else {
        	index = 6;
            progress = PROGRESS_THRITEEN;
        }

        mSeekBar.setProgress(progress);
        updateSummary(index);
    }

    private void writeScreenTimeout(int progress) {
        int index = 0;
        int value = 0;
        switch (progress) {
            case PROGRESS_ONE:
                index = 0;
                value = Integer.parseInt(mScreenTimeoutValues[0]);
                break;
            case PROGRESS_THREE:
                index = 1;
            	value = Integer.parseInt(mScreenTimeoutValues[1]);
                break;
            case PROGRESS_FIVE:
            	index = 2;
            	value = Integer.parseInt(mScreenTimeoutValues[2]);
                break;
            case PROGRESS_SEVEN:
            	index = 3;
            	value = Integer.parseInt(mScreenTimeoutValues[3]);
                break;
            case PROGRESS_NINE:
            	index = 4;
            	value = Integer.parseInt(mScreenTimeoutValues[4]);
                break;
            case PROGRESS_ELEVEN:
            	index = 5;
            	value = Integer.parseInt(mScreenTimeoutValues[5]);
                break;
            case PROGRESS_THRITEEN:
            	index = 6;
            	value = Integer.parseInt(mScreenTimeoutValues[6]);
                break;
            default:
                break;
        }

        try {
            Settings.System.putInt(mResolver, SCREEN_OFF_TIMEOUT, value);
            updateSummary(index);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist screen timeout setting", e);
        }
    }*/

    /*private void updateSummary(int index) {
    	if (index < mScreenTimeoutEntries.length) {
        	String summary = mContext.getString(R.string.aurora_display_screen_timeout_summary, mScreenTimeoutEntries[index]);
        	// Aurora <likai> <2013-10-29> modify begin
        	//setSummary(summary);
        	auroraSetArrowText(summary);
        	// Aurora <likai> <2013-10-29> modify end
        }
    }*/
}
