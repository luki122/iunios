package com.aurora.thememanager.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraWeekPickerDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.OnAuroraItemDismissCallback;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.internet.toolbox.FileDownloader.DownloadController;
import com.aurora.thememanager.R;
import com.aurora.thememanager.adapter.AbsThemeAdapter;
import com.aurora.thememanager.adapter.ThemeLocalAdapter;
import com.aurora.thememanager.db.LoadedThemeDao;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.preference.PreferenceManager;
import com.aurora.thememanager.utils.FileUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DatabaseController;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.utils.themeloader.Loader;
import com.aurora.thememanager.utils.themeloader.ThemeLoadListener;
import com.aurora.thememanager.utils.themeloader.ThemePackageLoader;

public class DownloadedThemeActivity extends BaseActivity implements
		ThemeLoadListener,OnClickListener,ThemeOperationCallBack {
	private static final int MSG_START_LOAD = 0;

	private static final int MSG_END_LOAD = 1;

	private static final int MSG_LOAD_THEME_OK = 2;

	private static final int MSG_LOAD_THEME_FAILURE = 3;
	
	private static final int MSG_SHOW_EDIT_PANEL = 4;
	
	private static final int MSG_HIDE_EDIT_PANEL = 5;

	private static final int DIALOG_PROGRESS = 0;
	
	private static final int DIALOG_THEME_FILE_NOT_EXITSTS = 1;
	
	private static final int DIALOG_DELETE_THEME = 2;
	
	private static final int FILE_SELECT_CODE = 100;
	
	private static final int THEME_ID_INVALID = -4;
	

	private GridView mThemeGrid;

	private ThemeLocalAdapter mAdapter;

	private Loader mThemeLoader;
	
	private Loader mImportThemeLoader;

	private Dialog mDialog;
	
	private boolean mFromShop = false;
	
	private TextView mGoToShop;
	
	private TextView mCancelBtn;
	
	private TextView mSelectAllBtn;
	
	private boolean mSelectMode = false;
	
	private boolean mSelectAll = false;
	
	private OnAuroraMenuItemClickListener mMenuItemClickListnener;
	
	private AuroraMenu mAuroraMenu;
	
	private ThemeManager mThemeManager;
	
	private Theme mDefaultTheme;

	private PreferenceManager mPrefManager;
	
	private LoadedThemeDao mLoadedThemeDao;

	private boolean mLoadFromLocal = false;
	
	private String mImportThemePath;
	
	private boolean mLoadFromLocalDb  = false;
	
	private int mUsedThemeId;
	
	private HashMap<String,Integer> mDownloadedThemeMap = new HashMap<String,Integer>();
	private boolean mLoadFromDownloaded = false;
	
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case MSG_START_LOAD:
				showDialog(DIALOG_PROGRESS);
				break;
			case MSG_END_LOAD:
				dismissDialog(DIALOG_PROGRESS);
				break;
			case MSG_LOAD_THEME_OK:
				Theme theme = (Theme) msg.obj;
				List<Theme> themes = mLoadedThemeDao.getLoadedThemes();
				if(mLoadFromLocal){
					if(themes != null && themes.size() > 0){
						Theme lastTheme = themes.get(themes.size() - 1);
						if(themes.contains(theme)){
							theme.themeId = lastTheme.themeId;
						}else{
							theme.themeId = lastTheme.themeId  +1;
						}
					}else{
						theme.themeId |= Theme.TYPE_THEME_IMPORT; 
					}
					
					Theme loadedTheme = mLoadedThemeDao.getTheme(theme.themeId);
					if(loadedTheme != null){
						if( loadedTheme.equals(theme)){
							return;
						}else if(loadedTheme.needUpdate(theme)){
							mLoadedThemeDao.updateLoadedTheme(theme);
						}
					}else{
						mLoadedThemeDao.insert(theme);
					}
				
				}
				if(mLoadFromLocalDb){
					if(themes.contains(theme)){
						int tmpThemeIndex = themes.indexOf(theme);
						Theme tmpTheme = themes.get(tmpThemeIndex);
						theme.themeId = tmpTheme.themeId;
					}
				}
				mAdapter.addData(theme);
				break;
			case MSG_LOAD_THEME_FAILURE:
				showDialog(DIALOG_THEME_FILE_NOT_EXITSTS);
				break;
			case MSG_SHOW_EDIT_PANEL:
				showEditPanel();
				break;
			case MSG_HIDE_EDIT_PANEL:
				mActionBar.setShowBottomBarMenu(false);
				mActionBar.showActionBarDashBoard();
				mAdapter.exitSelectMode();
				break;
				

			default:
				break;
			}
		};
		
	};
	
	private void createDefaultTheme(){
		mDefaultTheme = new Theme();
		mDefaultTheme.themeId = ThemeConfig.THEME_DEFAULT_ID;
		mDefaultTheme.name="Default";
		mDefaultTheme.author="IUNI";
		mAdapter.addData(mDefaultTheme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mApp.registerActivity(this);
		mPrefManager = PreferenceManager.getInstance(this);
		mLoadedThemeDao =new  LoadedThemeDao(this);
		 Intent intent = getIntent();
	        if(intent != null){
	        	mFromShop = intent.getBooleanExtra(Action.KEY_GOTO_LOCAL_TIME_WALLPAPER_LOCATION, false);
	        }
	        mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_ALL);
	        mThemeManager.setCallBack(this);
		setAuroraContentView(R.layout.downloaded_theme_activity,AuroraActionBar.Type.Normal);
		mThemeGrid = (GridView) findViewById(R.id.theme_gridview);
		mThemeLoader = new ThemePackageLoader(this,DownloadedThemeActivity.this);
		mImportThemeLoader =   new ThemePackageLoader(this,DownloadedThemeActivity.this);
		mUsedThemeId = mThemeManager.getAppliedThemeId(this);
		mAdapter = new ThemeLocalAdapter(this,mUsedThemeId);
		
		mThemeGrid.setAdapter(mAdapter);
		

		loadImportTheme();
		
		loadDownloadedThemes();
		
		mActionBar = getAuroraActionBar();
		mGoToShop = (TextView)findViewById(R.id.goto_shop_item);
		mActionBar.addItem(AuroraActionBarItem.Type.More,android.R.id.edit);
		mActionBar.setTitle(R.string.local_theme_title);
		setAuroraMenuItems(R.menu.menu_import_theme);
		getAuroraMenu().setAnimationStyle(com.aurora.R.style.AuroraMenuRightTopAnimation);
		setAuroraSystemMenuCallBack(new OnAuroraMenuItemClickListener() {
			
			@Override
			public void auroraMenuItemClick(int menuId) {
				// TODO Auto-generated method stub
				if(menuId == R.id.menu_import_theme){
					showFileChooser();
				}
			}
		});
		mActionBar.setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {

					@Override
					public void onAuroraActionBarItemClicked(int itemId) {
						// TODO Auto-generated method stub
						if (itemId == android.R.id.edit) {
							showAuroraMenu(mActionBar, Gravity.TOP|Gravity.RIGHT, 0, 0);
							
						}
					}
				});

		if(mFromShop){
			mGoToShop.setVisibility(View.GONE);
		}else{
			mGoToShop.setText(getResources().getString(R.string.network_theme));
		}
		mGoToShop.setOnClickListener(this);
		createDefaultTheme();
	}
	
	private void loadDownloadedThemes(){
		mLoadFromDownloaded = true;
		DatabaseController dbController = DatabaseController.getController(this, DatabaseController.TYPE_DOWNLOAD);
		ArrayList<File> files = new ArrayList<File>();
		if(dbController != null){
			dbController.openDatabase();
			List<DownloadData> datas = dbController.getDownloadedDatas();
			if(datas != null && datas.size() > 0){
				int size = datas.size();
				
				for(int i = 0;i< size;i++){
					Log.d("id", "currentId000:"+datas.get(i).downloadId+"  appliedId:"+mUsedThemeId);
					File file = new File(datas.get(i).fileDir+File.separatorChar+datas.get(i).fileName);
					if(file.exists()){
						mDownloadedThemeMap.put(file.getAbsolutePath(), Integer.valueOf(datas.get(i).downloadId));
						files.add(file) ;
					}
				}
			}
		}
		if(files.size() > 0){
			mThemeLoader.loadTheme(files.toArray());
		}
		
	}
	
	private void loadImportTheme(){
		mLoadFromDownloaded = false;
		List<Theme> themes = mLoadedThemeDao.getLoadedThemes();
		if(themes != null && themes.size() > 0){
			for(Theme theme:themes){
				if(TextUtils.isEmpty(theme.importPathName)){
					continue;
				}
				File file = new File(theme.importPathName);
				if(file.exists()){
					mLoadFromLocal = false;
					mLoadFromLocalDb = true;
					ThemePackageLoader loader = new ThemePackageLoader(this,DownloadedThemeActivity.this);
					loader.loadTheme(file);
					
				}
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_PROGRESS:
			mDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
			break;
		case DIALOG_THEME_FILE_NOT_EXITSTS:
			mDialog = new AuroraAlertDialog.Builder(this).setTitle(R.string.load_theme)
			.setNegativeButton(R.string.ok, null)
			.setMessage(R.string.msg_load_theme_failed)
			.create();
			break;
		case DIALOG_DELETE_THEME:
			
			mDialog = new AuroraAlertDialog.Builder(this).setTitle(R.string.delete_theme_title)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					deleteTheme();
					dialog.dismiss();
				}
			})
			.setMessage(R.string.delete_theme_msg)
			.create();
			
			break;
		default:
			break;
		}
		return mDialog;
	}
	
	private void deleteTheme(){
		ArrayList<Theme> selectedTheme = mAdapter.getSelectedTheme();
		if(selectedTheme != null && selectedTheme.size() > 0){
			Object[] objs =  selectedTheme.toArray();
			ArrayList<Theme> themes = new ArrayList<Theme>();
			for(int i  = 0 ;i < objs.length;i++){
				 Theme t1 = (Theme)objs[i];
				 /*
				  * 已经应用的、默认的主题不能删除
				  */
				 if(t1.themeId == ThemeConfig.THEME_DEFAULT_ID|| t1.themeId == mUsedThemeId){
					 continue;
				 }
				 themes.add((Theme)objs[i]);
			}
			mThemeManager.deleteTheme(themes);
			for(Theme t:themes){
				 /*
				  * 已经应用的、默认的主题不能删除
				  */
				 if(t.themeId == ThemeConfig.THEME_DEFAULT_ID|| t.themeId == mUsedThemeId){
					 continue;
				 }
					mAdapter.deleteData(t);
			}
		}
		mHandler.sendEmptyMessage(MSG_HIDE_EDIT_PANEL);
	}

	private void showEditPanel() {
		mMenuItemClickListnener = new OnAuroraMenuItemClickListener() {

			@Override
			public void auroraMenuItemClick(int id) {
				// TODO Auto-generated method stub
				if(id == R.id.aurora_menu_del){
					showDialog(DIALOG_DELETE_THEME);
					
				}
				

			}
		};
		setAuroraBottomBarMenuCallBack(mMenuItemClickListnener); // 必须写在布局前
		mActionBar.initActionBottomBarMenu(R.menu.aurora_menu_delete, 1);
		mActionBar.setShowBottomBarMenu(true);
		mActionBar.showActionBarDashBoard();
		mAuroraMenu = mActionBar.getAuroraActionBottomBarMenu();
		mAuroraMenu.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				mSelectAll = false;
				mAdapter.exitSelectMode();
			}
		});

		mCancelBtn = (TextView) mActionBar.getSelectLeftButton();
		mSelectAllBtn = (TextView) mActionBar.getSelectRightButton();
		mSelectAllBtn.setText(mSelectAll?R.string.cancel_select_all:R.string.select_all);
		mCancelBtn.setOnClickListener(this);
		mSelectAllBtn.setOnClickListener(this);
	}
	
	public void showEditView() {
		mHandler.sendEmptyMessage(MSG_SHOW_EDIT_PANEL);
	}
	
	

	private void showFileChooser() {
		Intent intent = new Intent(Action.ACTION_PICK_THEME);
		try {
			startActivityForResult(intent,FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				String themePath = data.getStringExtra(ThemeConfig.KEY_PICK_THEME_FILE_PATH);
				File themeFile = new File(themePath);
				if(themeFile.exists()){
					mImportThemePath = themePath;
					mThemeLoader = new ThemePackageLoader(this,DownloadedThemeActivity.this,true);
					mLoadFromLocal = true;
					mThemeLoader.loadTheme(themeFile);
					
				}else{
					mHandler.sendEmptyMessage(MSG_LOAD_THEME_FAILURE);
				}
				
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStartLoad() {

	}

	@Override
	public void onProgress(Integer... progress) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void onSuccess(boolean success, int status, Theme theme) {
		// TODO Auto-generated method stub
		if (success && theme != null) {
			Log.d("id", "mLoadFromDownloaded:"+mLoadFromDownloaded);
			if(mLoadFromDownloaded){
				String key = theme.fileDir+File.separatorChar+theme.fileName;
				if(!TextUtils.isEmpty(key)){
					Integer id = mDownloadedThemeMap.get(key);
					if(id != null){
						theme.themeId = id;
					}
				}
			}
			Message msg = msg = new Message();
			msg.what = MSG_LOAD_THEME_OK;
			msg.obj = theme;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.stopQueue();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == mCancelBtn){
			mHandler.sendEmptyMessage(MSG_HIDE_EDIT_PANEL);
		}else if(v == mSelectAllBtn){
			mSelectAll = !mSelectAll;
			mSelectAllBtn.setText(mSelectAll?R.string.cancel_select_all:R.string.select_all);
			mAdapter.selectAll(mSelectAll);
		}else if(v == mGoToShop){
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
		}
		
	}

	@Override
	public void onCompleted(boolean success, int statusCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgressUpdate(int progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return this;
	}

	

}
