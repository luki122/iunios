package com.aurora.thememanager.activity;

import java.io.File;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.Type;

import com.aurora.thememanager.R;
import com.aurora.thememanager.fragments.DirectoryFragment;
import com.aurora.thememanager.utils.FileUtils;
import com.aurora.thememanager.utils.ThemeConfig;
public class ThemePickerActivity extends AuroraActivity  implements DirectoryFragment.FileClickListener{
	private static final String ARG_CURRENT_PATH = "arg_title_state";
	private static final String START_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final int HANDLE_CLICK_DELAY = 150;
	private String mCurrentPath = START_PATH;
	private AuroraActionBar mActionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.activity_file_picker,Type.Normal);

        if (savedInstanceState != null) {
            mCurrentPath = savedInstanceState.getString(ARG_CURRENT_PATH);
        } else {
            initFragment();
        }

        mActionBar = getAuroraActionBar();
        mActionBar.setTitle("Sdcard");
	}
	
	private void updateTitle(){
		mActionBar.setTitle(mCurrentPath);
	}
	

	 private void initFragment() {
	        getFragmentManager().beginTransaction()
	                .add(R.id.container, DirectoryFragment.getInstance(START_PATH))
	                .commit();
	    }
	 
	 
	 private void addFragmentToBackStack(String path) {
	        getFragmentManager().beginTransaction()
	                .replace(R.id.container, DirectoryFragment.getInstance(path))
	                .addToBackStack(null)
	                .commit();
	    }

	 @Override
	    public void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        outState.putString(ARG_CURRENT_PATH, mCurrentPath);
	    }

	    @Override
	    public void onFileClicked(final File clickedFile) {
	        new Handler().postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                handleFileClicked(clickedFile);
	            }
	        }, HANDLE_CLICK_DELAY);
	    }

	    private void handleFileClicked(final File clickedFile) {
	        if (clickedFile.isDirectory()) {
	            addFragmentToBackStack(clickedFile.getPath());
	            mCurrentPath = clickedFile.getPath();
	            updateTitle();
	        } else {
	            setResultAndFinish(clickedFile.getPath());
	        }
	    }

	    private void setResultAndFinish(String filePath) {
	        Intent data = new Intent();
	        data.putExtra(ThemeConfig.KEY_PICK_THEME_FILE_PATH, filePath);
	        setResult(RESULT_OK, data);
	        finish();
	    }
	 
    @Override
    public void onBackPressed(){
        FragmentManager fm = getFragmentManager();

        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            mCurrentPath = FileUtils.cutLastSegmentOfPath(mCurrentPath);
            updateTitle();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }
}
