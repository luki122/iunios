package com.aurora.change.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.aurora.thememanager.R;
import com.aurora.change.adapters.WallpaperSourceSelectAdapter;
import com.aurora.change.cache.ImageLoader;
import com.aurora.change.data.DbControl;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.MimeUtils;
import com.aurora.filemanager.FileInfo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

//2014-07-17 废弃使用for文管提供图片选择接口  modified by liugj
public class WallpaperSourceSelectActivity extends AuroraActivity {

    private Context mContext;
    private String mType;

    private static final String TAG = "WallpaperSourceSelectActivity";
    private static CommonLog log = LogFactory.createLog(TAG);
    private String AURORA_WALLPAPER_MANAGER = "com.aurora.pic.wallpaper.manager.action";
    private TextView setPaper;
    private TextView folderTextView;
    private TextView countTextView;

    private Intent fileIntent;
    private String folderPath;
    private List<FileInfo> mFileNameList;
    private static final int ITEM_NEXT = 0;

    private GridView picBrowerGridView;
    private WallpaperSourceSelectAdapter mSourceSelectAdapter;
//    private ActionBar mActionBar;
    private AuroraActionBar mAuroraActionBar;
    private ImageResizer mImageResizer;
    private static final String IMAGE_CACHE_DIR = "file_thumbs";
//    private static final String AURORA_TITLE_FONT_FILE = "/system/fonts/title.ttf";
    private SdMountRecevier mSdMountRecevier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileIntent = getIntent();

        initImageCache();
        if (fileIntent != null) {
            Bundle bundle = fileIntent.getExtras();
            if (bundle != null) {
                folderPath = ( String ) bundle.get("folderPath");
                mFileNameList = ( List<FileInfo> ) bundle.getSerializable("files");
//                List<FileInfo> tempFileList = ( List<FileInfo> ) bundle.getSerializable("files");
//                mFileNameList = new ArrayList<FileInfo>();
//                for (int i = 0; i < tempFileList.size(); i++) {
//                    Log.d(TAG, tempFileList.get(i).fileName + ",path=" + tempFileList.get(i).filePath);
//                    if (getMimeType(tempFileList.get(i).filePath).contains("image")) {
//                        mFileNameList.add(tempFileList.get(i));
//                    }
//                }
                Log.i(TAG, folderPath);
                Log.i(TAG, mFileNameList.size() + "");
                if (folderPath != null) {
                    folderPath = folderPath.substring(folderPath.lastIndexOf("/") + 1, folderPath.length());
                    setupViews();
                    return;
                }
            }
        }

//        setContentView(R.layout.activity_main);
//        setPaper = (TextView) findViewById(R.id.setPaper);
//        setPaper.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                start();
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        /*if (folderPath != null) {
            Log.i(TAG, folderPath);
            startFileManagerActivity();
        } else {
            Log.i(TAG, "onBackPressed");
//            startLocalActivity();
            finish();
        }*/
        if (folderPath != null) {
            startFileManagerActivity();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if (folderPath != null) {
//                startLocalActivity();
                startFileManagerActivity();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSdMountRecevier == null) {
            registSdMountRecevier();
        }
    }

    @Override
    protected void onDestroy() {

//        if (mSourceSelectAdapter != null) {
//            ImageLoader imageLoader = mSourceSelectAdapter.getImageLoader();
//            if (imageLoader != null) {
//                imageLoader.clearCache();
//            }
//        }
        if (mSourceSelectAdapter != null) {
            mSourceSelectAdapter.clearData();
        }
        if (mSdMountRecevier != null) {
            unRegistSdMountRecevier();
        }
        super.onDestroy();
    }

    private void setupViews() {
//         setContentView(R.layout.activity_wallpaper_source_select);
        setAuroraContentView(R.layout.activity_wallpaper_source_select);
        mAuroraActionBar = getAuroraActionBar();
        mAuroraActionBar.setTitle(folderPath);
        mAuroraActionBar.getTitleView().setMaxEms(6);
        mAuroraActionBar.addItem(R.layout.source_select_show_next, ITEM_NEXT);
        mAuroraActionBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {

            @Override
            public void onAuroraActionBarBackItemClicked(int arg0) {
                Log.d(TAG, "backpressed");
                if (folderPath != null) {
                    startFileManagerActivity();
                }
            }
        });
        countTextView = ( TextView ) mAuroraActionBar.findViewById(R.id.source_select_show_next);
        countTextView.setEnabled(false);
//        try {
//            Typeface typeface = Typeface.createFromFile(AURORA_TITLE_FONT_FILE);
//            countTextView.setTypeface(typeface);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//         folderTextView = (TextView) findViewById(R.id.folderTextView);
//        folderTextView = getAuroraActionBar().getCancelButton();
//        folderTextView.setText(folderPath);
//
//        folderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.aurora_action_bar_back_normal, 0,
//                0, 0);
//        folderTextView.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        picBrowerGridView = ( GridView ) findViewById(R.id.picBrowerGridView);

        mSourceSelectAdapter = new WallpaperSourceSelectAdapter(this, mFileNameList);
        mSourceSelectAdapter.setImageResizer(mImageResizer);

        picBrowerGridView.setAdapter(mSourceSelectAdapter);
//        picBrowerGridView.setOnScrollListener(mScrollListener);

//         countTextView = (TextView) findViewById(R.id.countTextView);
//        countTextView = getAuroraActionBar().getOkButton();
        /*countTextView.setText(getResources().getString(R.string.menu_next) + "("
                + mSourceSelectAdapter.getChooseCount() + ")");*/
        countTextView.setText(getResources().getString(R.string.menu_next));
        countTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("wallpaper", "files=" + mSourceSelectAdapter.getChooseFiles());
                try {
                    Intent intent = new Intent(WallpaperSourceSelectActivity.this,
                            WallpaperCropActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Consts.WALLPAPER_CROP_KEY,
                            ( Serializable ) mSourceSelectAdapter.getChooseFiles());
//                    DbControl control = new DbControl(mContext);
//                    List<PictureGroupInfo> groupInfos = control.queryAllGroupInfos();
//                    String group_name = getString(R.string.wallpaper_crop_custom_name);
//                    int id = 1;
//                    if (groupInfos != null) {
//                        id = groupInfos.get(groupInfos.size() - 1).getId() + 1;
//                    }
//                    if (id < 10) {
//                        group_name = group_name + "0" + id;
//                    } else {
//                        group_name += id;
//                    }
//                    Log.d("crop", "group_name=" + group_name + ",groupInfos=" + groupInfos.toString());
//                    bundle.putString(Consts.LOCKSCREEN_WALLPAPER_GROUP_NAME_KEY, group_name);
//                    control.close();
                    bundle.putString(Consts.LOCKSCREEN_WALLPAPER_CROP_TYPE, "mult");
                    bundle.putBoolean(Consts.LOCKSCREEN_WALLPAPER_CROP_SOURCE, true);
                    intent.putExtras(bundle);
                    if (mSourceSelectAdapter.getChooseCount() > 0) {
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        picBrowerGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (mSourceSelectAdapter.getChooseCount() < 12
                        || mSourceSelectAdapter.getCheckState(position)) {
                    mSourceSelectAdapter.checkBoxState(position);
                    countTextView.setEnabled(true);
                    if (mSourceSelectAdapter.getChooseCount() <= 1) {
                        countTextView.setText(getResources().getString(R.string.menu_next));
                        if (mSourceSelectAdapter.getChooseCount() == 0) {
                            countTextView.setEnabled(false);
                        }
                    } else {
                        countTextView.setText(getResources().getString(R.string.menu_next) + "("
                                + mSourceSelectAdapter.getChooseCount() + ")");
                    }
                    mSourceSelectAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(mContext, getString(R.string.wallpaper_select_max_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*mActionBar = getActionBar();
        int options = ActionBar.DISPLAY_HOME_AS_UP ^ ActionBar.DISPLAY_SHOW_TITLE ^ ActionBar.DISPLAY_SHOW_CUSTOM;
        mActionBar.setDisplayOptions(options);
        mActionBar.setTitle(folderPath);
        mActionBar.setCustomView(R.layout.source_select_show_next);
        countTextView = (TextView) mActionBar.getCustomView().findViewById(R.id.source_select_show_next);
        countTextView.setText(getResources().getString(R.string.menu_next) + "(" + mSourceSelectAdapter.getChooseCount()
                + ")");
        countTextView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.d("wallpaper", "files=" + mSourceSelectAdapter.getChooseFiles());
                try {
                    Intent intent = new Intent(WallpaperSourceSelectActivity.this, WallpaperCropActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Consts.WALLPAPER_CROP_KEY, (Serializable) mSourceSelectAdapter.getChooseFiles());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    /*OnScrollListener mScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_FLING:
                    mSourceSelectAdapter.setFlagBusy(true);
                    break;
                case OnScrollListener.SCROLL_STATE_IDLE:
                    mSourceSelectAdapter.setFlagBusy(false);
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    mSourceSelectAdapter.setFlagBusy(false);
                    break;
                default:
                    break;
            }
            mSourceSelectAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    };*/

    protected void startLocalActivity() {
        Intent intent = new Intent();
        try {
            intent.setClass(getApplicationContext(), WallpaperLocalActivity.class);
            intent.putExtra(Consts.WALLPAPER_TYPE_KEY, Consts.WALLPAPER_LOCKSCREEN_TYPE);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            log.i(e.toString());
        }
    }

    private void startFileManagerActivity() {
        PackageManager packageManager = this.getPackageManager();
        Log.d(TAG, "startFileManagerActivity");
        Intent intent = new Intent();
        try {
            intent = packageManager.getLaunchIntentForPackage("com.aurora.filemanager");
            Bundle bundle = new Bundle();
            bundle.putBoolean(Consts.FILEMANAGER_WALLPAPER, true);
            bundle.putBoolean("com.aurora.change", true);
            bundle.putString("file_type", "lockscreen");
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
            finish();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_next, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_next:
                Toast.makeText(mContext, "next", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initImageCache() {
        mContext = this;
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int longest = ( int ) (119 * displayMetrics.scaledDensity);
        mImageResizer = new ImageResizer(mContext, longest, true);
        mImageResizer.setLoadingImage(R.drawable.preview_loading);
        mImageResizer.addImageCache(this, IMAGE_CACHE_DIR);
    }

    private void registSdMountRecevier() {
        mSdMountRecevier = new SdMountRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mSdMountRecevier, filter);
    }

    private void unRegistSdMountRecevier() {
        unregisterReceiver(mSdMountRecevier);
        mSdMountRecevier = null;
    }

    class SdMountRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();

            if (Intent.ACTION_MEDIA_SHARED.equals(action) || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)
                    || Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_REMOVED.equals(action)
                    || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                Toast.makeText(mContext, getString(R.string.wallpaper_crop_sdcard_error), Toast.LENGTH_SHORT)
                        .show();
                WallpaperSourceSelectActivity.this.finish();
            }
        }
    }

    public static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "*/*";

        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }
}
