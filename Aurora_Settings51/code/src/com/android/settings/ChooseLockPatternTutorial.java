package com.android.settings;

import java.util.ArrayList;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.aurora.lockscreen.AuroraLockPatternView;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class ChooseLockPatternTutorial extends AuroraPreferenceActivity {

	 private static final String TAG = "ChooseLockPatternTutorial";
    public ChooseLockPatternTutorial() {

    }

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, ChooseLockPatternTutorialFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }

        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        
        CharSequence msg = getText(R.string.lockpassword_choose_your_pattern_header);
        showBreadCrumbs(msg, msg);
    }

    public static class ChooseLockPatternTutorialFragment extends Fragment
            implements View.OnClickListener {
        private View mNextButton;
        private View mSkipButton;
        private AuroraLockPatternView mPatternView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "---ChooseLockPatternTutorialFragment ---onCreate---");
            
            LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
            if (savedInstanceState == null && lockPatternUtils.isPatternEverChosen()) {
            	Log.d(TAG, "---loggg onCreate---");
                Intent intent = new Intent(getActivity(), ChooseLockPattern.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                intent.putExtra("confirm_credentials", false);
                final boolean isFallback = getActivity().getIntent()
                    .getBooleanExtra(LockPatternUtils.LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK, false);
                intent.putExtra(LockPatternUtils.LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK,
                                isFallback);
                startActivity(intent);
                getActivity().finish();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	 Log.d(TAG, "---ChooseLockPatternTutorialFragment ---onCreateView---");
            View view = inflater.inflate(R.layout.choose_lock_pattern_tutorial, null);
            mNextButton = view.findViewById(R.id.next_button);
            mNextButton.setOnClickListener(this);
            mSkipButton = view.findViewById(R.id.skip_button);
            mSkipButton.setOnClickListener(this);

            mPatternView = (AuroraLockPatternView) view.findViewById(R.id.lockPattern);
            ArrayList<AuroraLockPatternView.Cell> demoPattern = new ArrayList<AuroraLockPatternView.Cell>();
            demoPattern.add(AuroraLockPatternView.Cell.of(0,0));
            demoPattern.add(AuroraLockPatternView.Cell.of(0,1));
            demoPattern.add(AuroraLockPatternView.Cell.of(1,1));
            demoPattern.add(AuroraLockPatternView.Cell.of(2,1));
            mPatternView.setPattern(AuroraLockPatternView.DisplayMode.Animate, demoPattern);
            mPatternView.disableInput();
           

            return view;
        }

        public void onClick(View v) {
            if (v == mSkipButton) {
            	Log.d(TAG, "cancel");
                // Canceling, so finish all
                getActivity().setResult(ChooseLockPattern.RESULT_FINISHED);
                getActivity().finish();
            } else if (v == mNextButton) {
            	Log.d(TAG, "continue");
                final boolean isFallback = getActivity().getIntent()
                    .getBooleanExtra(LockPatternUtils.LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK, false);
                Intent intent = new Intent(getActivity(), ChooseLockPattern.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                intent.putExtra(LockPatternUtils.LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK,
                                isFallback);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0); // no animation
                getActivity().finish();
            }
        }
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
