package com.android.settings;

import aurora.app.AuroraActivity;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import aurora.provider.AuroraSettings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import aurora.widget.AuroraActionBar;
/**
 * 
 * @author zhangxin
 * @date 2013-03-28
 *
 */
public class GnFontSizeActivity extends AuroraActivity {

    private static final String TAG = "GnFontSizeActivity";

    private ImageSwitcher mFontImageSwitcher;
    private ImageView mSeekBarBg;
    private SeekBar mFontSeekBar;
    private Button mOkButton;
    private Button mCancelButton;
    private ImageView mFontSizeText;

    private Context mContext;
    private final Configuration mCurConfig = new Configuration();

    private static final int HIGH_PIXEL = 1200;
    private static final int HIGH_PIXEL_PROGRESS_MAX = 100;
    private static final int LOW_PIXEL_PROGRESS_MAX = 75;
    private static final int ENGLISH_PROGRESS_MAX = 50;
    private static final int DIV_PROGRESS = 25;
    private static final int HALF_DIV_PROGRESS = DIV_PROGRESS / 2;
    private static final int PROGRESS_ONE = 0;
    private static final int PROGRESS_TWO = DIV_PROGRESS;
    private static final int PROGRESS_THREE = 2 * DIV_PROGRESS;
    private static final int PROGRESS_FOUR = 3 * DIV_PROGRESS;
    private static final int PROGRESS_FIVE = HIGH_PIXEL_PROGRESS_MAX;

    private static final int FONT_SIZE_SMALL = 0;
    private static final int FONT_SIZE_NOMAL = 1;
    private static final int FONT_SIZE_MEDIUM = 2;
    
    private float mFontSizeLarge;
    private float mFontSizeExtraLarge;
    
    private String[] mFontSizeValues;

    private static final int FONT_SIZE_STATE_SYSTEM = AuroraSettings.FONT_SIZE_SYSTEM;
    private static final int FONT_SIZE_STATE_LARGE = AuroraSettings.FONT_SIZE_LARGE;
    private static final int FONT_SIZE__STATE_EXTRA_LARGE = AuroraSettings.FONT_SIZE_EXTRA_LARGE;

    private static final int FONT_PREVIEW_IMG[] = {R.drawable.gn_font_size_small,
            R.drawable.gn_font_size_normal, R.drawable.gn_font_size_medium, R.drawable.gn_font_size_large,
            R.drawable.gn_font_size_ext_large};

    private static final int FONT_PREVIEW_IMG_US[] = {R.drawable.gn_font_size_small_us,
            R.drawable.gn_font_size_normal_us, R.drawable.gn_font_size_large_us};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (GnSettingsUtils.sGnSettingSupport) {
            if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(
                    GnSettingsUtils.TYPE_LIGHT_THEME)) {
                setTheme(R.style.GnSettingsLightTheme);
            } else {
//                setTheme(R.style.GnSettingsDarkTheme);
                setTheme(R.style.GnSettingsLightTheme);
            }
        }
        super.onCreate(savedInstanceState);
        //AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
        //setContentView(R.layout.gn_font_size);
        setAuroraContentView(R.layout.gn_font_size,AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.title_font_size);
	//AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        buildView();

        if (GnSettingsUtils.sGnSettingSupport) {
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
            getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
            getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        readFontSize(mContext);
        super.onResume();
    }

    private void buildView() {
        mFontImageSwitcher = ( ImageSwitcher ) findViewById(R.id.gn_font_size_imageswitcher);
        mSeekBarBg = ( ImageView ) findViewById(R.id.gn_font_size_seekbar_bg);
        mFontSeekBar = ( SeekBar ) findViewById(R.id.gn_font_size_seekbar);
        mOkButton = ( Button ) findViewById(R.id.gn_font_size_btn_ok);
        mCancelButton = ( Button ) findViewById(R.id.gn_font_size_btn_cancel);
        mFontSizeText = ( ImageView ) findViewById(R.id.gn_font_size_text);
        mContext = GnFontSizeActivity.this;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Log.d(TAG, "displayMetrics=" + displayMetrics.toString());

        mFontImageSwitcher.setFactory(new ViewFactory() {
            @Override
            public View makeView() {
                ImageView i = new ImageView(mContext);
                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
                i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
                return i;
            }
        });
        setPreviewImg(mContext, 0);

        mFontSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mOkButton.setOnClickListener(clickListener);
        mCancelButton.setOnClickListener(clickListener);

        if (isCNLanguage(mContext)) {
            mFontSizeText.setImageResource(R.drawable.gn_font_size_text);
            mSeekBarBg.setImageResource(R.drawable.gn_font_seekbar_bg);
        } else {
            mFontSizeText.setImageResource(R.drawable.gn_font_size_text_us);
            mSeekBarBg.setImageResource(R.drawable.gn_font_seekbar_bg_us);
        }

        if (isCNLanguage(mContext)) {
            if (displayMetrics.heightPixels > HIGH_PIXEL) {
                mFontSeekBar.setMax(HIGH_PIXEL_PROGRESS_MAX);
            } else {
                mFontSeekBar.setMax(LOW_PIXEL_PROGRESS_MAX);
            }
        } else {
            mFontSeekBar.setMax(ENGLISH_PROGRESS_MAX);
        }
        
        mFontSizeValues = getResources().getStringArray(R.array.gn_entryvalues_font_size);
        mFontSizeLarge = Float.parseFloat(mFontSizeValues[mFontSizeValues.length - 1]) + 0.00001f;
        mFontSizeExtraLarge = Float.parseFloat(mFontSizeValues[mFontSizeValues.length - 1]) + 0.00002f;
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

    OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.gn_font_size_btn_cancel) {
                GnFontSizeActivity.this.finish();
            } else if (v.getId() == R.id.gn_font_size_btn_ok) {
                int progress = mFontSeekBar.getProgress();
                writeFontSize(mContext, progress);
                GnFontSizeActivity.this.finish();
            }
        }
    };

    private void refreshSeekBarState(SeekBar seekBar) {
        int res = 0;
        int currentProgress = seekBar.getProgress();
        int progress = 0;
        if (currentProgress < HALF_DIV_PROGRESS) {
            progress = PROGRESS_ONE;
            res = 0;
        } else if (currentProgress >= HALF_DIV_PROGRESS
                && currentProgress < (DIV_PROGRESS + HALF_DIV_PROGRESS)) {
            progress = PROGRESS_TWO;
            res = 1;
        } else if (currentProgress >= (DIV_PROGRESS + HALF_DIV_PROGRESS)
                && currentProgress < (2 * DIV_PROGRESS + HALF_DIV_PROGRESS)) {
            progress = PROGRESS_THREE;
            res = 2;
        } else if (currentProgress >= (2 * DIV_PROGRESS + HALF_DIV_PROGRESS)
                && currentProgress < (3 * DIV_PROGRESS + HALF_DIV_PROGRESS)) {
            progress = PROGRESS_FOUR;
            res = 3;
        } else {
            progress = seekBar.getMax();
            res = 4;
        }
        seekBar.setProgress(progress);
        setPreviewImg(mContext, res);
    }

    private void readFontSize(Context context) {
        int fontState = 0;
        int res = 0;
        int progress = 0;
//        Configuration configuration = context.getResources().getConfiguration();
        try {
//            mCurConfig.updateFrom(configuration);
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            fontState = AuroraSettings.getInt(getContentResolver(), AuroraSettings.FONT_SIZE, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "configuration=" + "configuration.toString()" + ",mCurConfig.fontScale=" + mCurConfig.fontScale);

        if (fontState != FONT_SIZE_STATE_SYSTEM) {
            if (fontState == AuroraSettings.FONT_SIZE_EXTRA_LARGE) {
                res = 4;
                progress = PROGRESS_FIVE;
            } else {
                res = 3;
                progress = PROGRESS_FOUR;
            }
        } else {
            int fontScale = floatToIndex(context, mCurConfig.fontScale);
            if (fontScale == FONT_SIZE_SMALL) {
                res = 0;
                progress = PROGRESS_ONE;
            } else if (fontScale == FONT_SIZE_MEDIUM) {
                res = 2;
                progress = PROGRESS_THREE;
            } else {
                res = 1;
                progress = PROGRESS_TWO;
            }
        }

        mFontSeekBar.setProgress(progress);
        setPreviewImg(context, res);
    }

    private void writeFontSize(Context context, int progress) {
        float fontScale = 0f;
        int fontState = 0;
        switch (progress) {
            case PROGRESS_ONE:
                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_SMALL]);
                fontState = FONT_SIZE_STATE_SYSTEM;
                break;
            case PROGRESS_TWO:
                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_NOMAL]);
                fontState = FONT_SIZE_STATE_SYSTEM;
                break;
            case PROGRESS_THREE:
                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_MEDIUM]);
                fontState = FONT_SIZE_STATE_SYSTEM;
                break;
            case PROGRESS_FOUR:
//                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_MEDIUM]);
                fontScale = mFontSizeLarge;
                fontState = FONT_SIZE_STATE_LARGE;
                break;
            case PROGRESS_FIVE:
//                fontScale = Float.parseFloat(mFontSizeValues[FONT_SIZE_MEDIUM]);
                fontScale = mFontSizeExtraLarge;
                fontState = FONT_SIZE__STATE_EXTRA_LARGE;
                break;

            default:
                break;
        }

        try {
            mCurConfig.fontScale = fontScale;
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);

            AuroraSettings.putInt(getContentResolver(), AuroraSettings.FONT_SIZE, fontState);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void setPreviewImg(Context context, int res) {
        if (isCNLanguage(context)) {
            mFontImageSwitcher.setImageResource(FONT_PREVIEW_IMG[res]);
        } else {
            mFontImageSwitcher.setImageResource(FONT_PREVIEW_IMG_US[res]);
        }
    }

    private boolean isCNLanguage(Context context) {
        String countryCode = context.getResources().getConfiguration().locale.getCountry();
        if (countryCode.equals("CN")) {
            return true;
        } else {
            return false;
        }
    }
    
    public static int floatToIndex(Context context, float val){
        String[] indices = context.getResources().getStringArray(R.array.gn_entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal - lastVal) * .5f)) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
