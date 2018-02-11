package com.aurora.change.activities;

import java.io.File;
import java.util.List;

import com.aurora.change.R;
import com.aurora.change.adapters.DesktopWallpaperSourceSelectAdapter;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.filemanager.FileInfo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

//2014-07-17 废弃使用for文管提供图片选择接口  modified by liugj
public class DesktopWallpaperSourceSelectActivity extends AuroraActivity {

    private Context mContext;
    private String mType;

    private static final String TAG = "WallpaperSourceSelectActivity";
    private static final String ACTION_CROP = "com.android.camera.action.CROP";
    private static final String IMAGE_TYPE = "image/*";
    private static final String KEY_ASPECT_X = "aspectX";
    private static final String KEY_ASPECT_Y = "aspectY";
    private static final String KEY_SPOTLIGHT_X = "spotlightX";
    private static final String KEY_SPOTLIGHT_Y = "spotlightY";
    private static final String KEY_OUTPUT_X = "outputX";
    private static final String KEY_OUTPUT_Y = "outputY";
    private static final String KEY_SCALE = "scale";
    private static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    private static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
    private static final String KEY_NO_FACE_DETECTION = "noFaceDetection";
    
    private static CommonLog log = LogFactory.createLog(TAG);
    private String AURORA_WALLPAPER_MANAGER = "com.aurora.pic.wallpaper.manager.action";

    private Intent fileIntent;
    private String folderPath;
    private List<FileInfo> mFileNameList;

    private GridView mDesktopSourceGrid;
    private DesktopWallpaperSourceSelectAdapter mSourceSelectAdapter;
//    private ActionBar mActionBar;
    private AuroraActionBar mAuroraActionBar;
    private ImageResizer mImageResizer;
    private static final String IMAGE_CACHE_DIR = "desktop_file_thumbs";

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
                Log.i(TAG, folderPath);
                Log.i(TAG, mFileNameList.size() + "");
                if (folderPath != null) {
                    folderPath = folderPath.substring(folderPath.lastIndexOf("/") + 1, folderPath.length());
                    setupViews();
                    return;
                }
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if (folderPath != null) {
                startFileManagerActivity();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {

        if (mSourceSelectAdapter != null) {
            mSourceSelectAdapter.clearData();
        }
        super.onDestroy();
        if (mFileNameList != null) {
			mFileNameList.clear();
			mFileNameList = null;
		}
    }

    private void setupViews() {
//         setContentView(R.layout.activity_wallpaper_source_select);
        setAuroraContentView(R.layout.activity_desktop_source_select);
        mAuroraActionBar = getAuroraActionBar();
        mAuroraActionBar.setTitle(folderPath);
        mAuroraActionBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {

            @Override
            public void onAuroraActionBarBackItemClicked(int arg0) {
                Log.d(TAG, "backpressed");
                if (folderPath != null) {
                    startFileManagerActivity();
                }
            }
        });

        mDesktopSourceGrid = ( GridView ) findViewById(R.id.desktop_source_select_grid);

        mSourceSelectAdapter = new DesktopWallpaperSourceSelectAdapter(this, mFileNameList);
        mSourceSelectAdapter.setImageResizer(mImageResizer);

        mDesktopSourceGrid.setAdapter(mSourceSelectAdapter);

        mDesktopSourceGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            	Uri uri = Uri.fromFile(new File(mFileNameList.get(position).filePath));
            	/*int width = getWallpaperDesiredMinimumWidth();
                int height = getWallpaperDesiredMinimumHeight();
                Point size = getDefaultDisplaySize(new Point());
                float spotlightX = (float) size.x / width;
                float spotlightY = (float) size.y / height;*/
            	//Intent request = new Intent(ACTION_CROP)
                Intent request = new Intent(DesktopWallpaperSourceSelectActivity.this, SetWallpaperCropActivity.class);
                request.setDataAndType(uri, IMAGE_TYPE)
                .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                .putExtra(KEY_SET_AS_WALLPAPER, true);
            	/*.putExtra(KEY_OUTPUT_X, width)
                .putExtra(KEY_OUTPUT_Y, height)
                .putExtra(KEY_ASPECT_X, width)
                .putExtra(KEY_ASPECT_Y, height)
                .putExtra(KEY_SPOTLIGHT_X, spotlightX)
                .putExtra(KEY_SPOTLIGHT_Y, spotlightY)
                .putExtra(KEY_SCALE, true)
                .putExtra(KEY_SCALE_UP_IF_NEEDED, true)
                .putExtra(KEY_NO_FACE_DETECTION, true)
                .putExtra(KEY_SET_AS_WALLPAPER, true);*/
            	startActivity(request);
            	finish();
            }
        });

    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private Point getDefaultDisplaySize(Point size) {
        Display d = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            d.getSize(size);
        } else {
            size.set(d.getWidth(), d.getHeight());
        }
        return size;
    }
    
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
            bundle.putString("file_type", "desktop");
            intent.putExtras(bundle);
            startActivity(intent);
            overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
            finish();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
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
}
