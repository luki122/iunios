package com.aurora.thememanager.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraListView;

import com.aurora.change.model.PictureGroupInfo;
import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.cache.DiskCache;
import com.aurora.internet.request.ImageRequest;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.download.RingtongDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.view.ListViewLoadView;
import com.aurora.thememanager.view.ListViewDelegate.OnListScrollChange;
import com.aurora.thememanager.view.ListViewLoadView.OnLoadViewClick;
import com.aurora.thememanager.widget.DownloadButton;
import com.aurora.thememanager.widget.NetworkRoundedImageView;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;
import com.nostra13.universalimageloader.core.assist.FailReason;

public class LocalRingtoneListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> mAudioFile;

	public LocalRingtoneListAdapter(Context context, ArrayList<String> audioFile) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		mAudioFile = audioFile;
	}

	@Override
	public View getView(int position, View coverview, ViewGroup parent) {
		// TODO Auto-generated method stub
		final Holder holder;
        if (coverview != null) {
    		holder = (Holder) coverview.getTag();
		} else {
			coverview = LayoutInflater.from(mContext).inflate(R.layout.ringtone_local_list_item, null);
			holder = new Holder();
			holder.mAudioName = (TextView)coverview.findViewById(R.id.ringtone_name);
			holder.mAudioName.setText(mAudioFile.get(position));
			coverview.setTag(holder);
		}
		return coverview;
	}
	
	@Override
	public Theme getItem(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAudioFile.size();
	}

	private final class Holder {
		TextView mAudioName;
		TextView mDuration;
	}

	@Override
	public long getItemId(int paramInt) {
		// TODO Auto-generated method stub
		return 0;
	}
}
