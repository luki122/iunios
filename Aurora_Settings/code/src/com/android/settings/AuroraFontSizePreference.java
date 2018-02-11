package com.android.settings;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import aurora.preference.AuroraPreference;

public class AuroraFontSizePreference extends AuroraPreference {

    private static final String TAG = "AuroraFontSize";

	private Context mContext;
	private final Configuration mCurConfig = new Configuration();

	private static final int PROGRESS_ONE = 0;
    private static final int PROGRESS_TWO = 17;
    private static final int PROGRESS_THREE = 34;
    private static final int PROGRESS_FOUR = 51;
    private static final int PROGRESS_FIVE = 68;
    private static final int PROGRESS_SIX = 84;
    private static final int PROGRESS_SEVEN = 100;

    private static final int FONT_SIZE_SMALL = 0;
    private static final int FONT_SIZE_NOMAL = 1;
    private static final int FONT_SIZE_MEDIUM = 2;
    private static final int FONT_SIZE_LARGE = 3;

    private String[] mFontSizeEntries;
    private String[] mFontSizeValues;

    private SeekBar mSeekBar;

	public AuroraFontSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mFontSizeEntries = context.getResources().getStringArray(R.array.aurora_font_size_entries);
        mFontSizeValues = context.getResources().getStringArray(R.array.aurora_font_size_values);
	}

	@Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setMax(PROGRESS_SEVEN);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        readFontSize();
    }

    OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

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
        } else {
            progress = PROGRESS_SEVEN;
        }
        seekBar.setProgress(progress);
        writeFontSize(progress);
    }

    private void readFontSize() {;
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "configuration = " + "configuration.toString()" + ", mCurConfig.fontScale = " + mCurConfig.fontScale);

        int progress = 0;
        int fontScale = floatToIndex(mCurConfig.fontScale);
        if (fontScale == FONT_SIZE_SMALL) {
            progress = PROGRESS_ONE;
        } else if (fontScale == FONT_SIZE_NOMAL) {
            progress = PROGRESS_THREE;
        } else if (fontScale == FONT_SIZE_MEDIUM) {
            progress = PROGRESS_FIVE;
        } else {
            progress = PROGRESS_SEVEN;
       }

        mSeekBar.setProgress(progress);
    }

    private void writeFontSize(int progress) {
        float fontScale = 0f;
        switch (progress) {
            case PROGRESS_ONE:
                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_SMALL]);
                break;
            case PROGRESS_THREE:
                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_NOMAL]);
                break;
            case PROGRESS_FIVE:
                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_MEDIUM]);
                break;
            case PROGRESS_SEVEN:
                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_LARGE]);;
                break;
            default:
                break;
        }

        try {
            mCurConfig.fontScale = fontScale;
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    int floatToIndex(float val) {
        String[] indices = mContext.getResources().getStringArray(R.array.aurora_font_size_values);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal- lastVal) * .5f)) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }
}
