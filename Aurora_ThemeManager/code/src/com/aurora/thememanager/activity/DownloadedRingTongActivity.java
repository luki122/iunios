package com.aurora.thememanager.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.aurora.thememanager.R;
import com.aurora.thememanager.adapter.AbsThemeAdapter;
import com.aurora.thememanager.adapter.LocalRingtoneListAdapter;
import com.aurora.thememanager.adapter.RingtoneListAdapter;
import com.aurora.thememanager.adapter.ThemeLocalAdapter;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.FileUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themeloader.Loader;
import com.aurora.thememanager.utils.themeloader.ThemeLoadListener;
import com.aurora.thememanager.utils.themeloader.ThemePackageLoader;

public class DownloadedRingTongActivity extends BaseActivity {

	private LocalRingtoneListAdapter mLocalRingtoneListAdapter;

	private RelativeLayout mLocalRingtoneRela;
	private AuroraListView mLocalRingtoneList;
	private AuroraActionBar mAuroraActionBar;
	private TextView mDownLoadedText;
	private TextView mNoDownloadText;
	private ArrayList<String> audioFile = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.downloaded_ringtone_activity, AuroraActionBar.Type.Normal);
		getVideoFileName(ThemeConfig.THEME_RINGTONG_DOWNLLOAD_PATH);
		initViews();
	}
	
	private void initViews( ) {
		mAuroraActionBar = getAuroraActionBar();
		mAuroraActionBar.setTitle(R.string.ringtone);
		mDownLoadedText = (TextView)findViewById(R.id.local_ringtone_text);
		mNoDownloadText = (TextView)findViewById(R.id.no_downloadringtone_text);
		mLocalRingtoneList = (AuroraListView)findViewById(R.id.local_ringtone_list);
		if(audioFile.size() != 0) {
			mLocalRingtoneListAdapter = new LocalRingtoneListAdapter(this, audioFile);
			mLocalRingtoneList.setAdapter(mLocalRingtoneListAdapter);
			mNoDownloadText.setVisibility(View.GONE);
		} else {
			mDownLoadedText.setVisibility(View.GONE);
			mLocalRingtoneList.setVisibility(View.GONE);
			mNoDownloadText.setVisibility(View.VISIBLE);
		}
		
	}

	public void getVideoFileName(String fileAbsolutePath) {
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();
 
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                int index = filename.lastIndexOf(".");
                filename = filename.substring(0, index);
                audioFile.add(filename);
            }
        }
    }

}
