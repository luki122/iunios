package com.aurora.puremanager.permission;

import java.lang.reflect.Method;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraSwitch;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.mConfig;

public class PermissionAppDetail extends AuroraActivity implements LoaderCallbacks<Object>{
    private String TAG = "PermissionAppDetail";
    public static final String PACKAGENAME= "packagename";
    public static final String EXTRA_NAME_TITLE = "title";
    public static final String FROM_MYSELF= "from_myself";
    public static final String POSITION = "";
    private static Context mContext ;
    private PackageManager mPm;
    private AuroraListView mListView;
    private static AuroraSwitch mSwitch;
    private View mLayout ;
    private LinearLayout mLoader;
    private RelativeLayout mEmptyLayout ;
    private PermissionAppDetailAdapter mAdapter;
    private static String mPackageName ;
    public static String mPosition;
    private static boolean mChangeFlag = false ;
    private String mDefaultGrpName="DefaultGrp";
    private boolean mSecurityPermission = true ;
    private boolean mFrommyself = true ,mOnCreate = true;
    private String mTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
//        setContentView(R.layout.softmanager_permissi_app_detail);
        mTitle = getIntent().getStringExtra(EXTRA_NAME_TITLE);
        mPackageName = getIntent().getStringExtra(PACKAGENAME);
        mPosition = getIntent().getStringExtra(POSITION);
        DebugUtil.d(TAG, " onCreate() : "+ "  mTitle = " + mTitle
                + " mPackageName =" + mPackageName);
        
        if(mConfig.isNative){
        	setContentView(R.layout.softmanager_permissi_app_detail);
        }else{
        	setAuroraContentView(R.layout.softmanager_permissi_app_detail,
            		AuroraActionBar.Type.Normal);
        	setAuroraActionbarSplitLineVisibility(View.GONE);
            getAuroraActionBar().setTitle(mTitle);
            getAuroraActionBar().setBackgroundResource(R.color.app_manager_layout_color);
        }
        initView(); 
        getLoaderManager().initLoader(SoftManagerLoader_per.ID_LOADER_DEFAULT, null, this);
    }

    private void initView() {
        mListView = (AuroraListView) findViewById(R.id.permission_app_expand_listview);
//        mListView.setGroupIndicator(null);
        mSwitch = (AuroraSwitch) findViewById(R.id.permission_trust_switch);
        mLayout = findViewById(R.id.trust);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.permission_trust__empty_view);  
        mLoader = ( LinearLayout ) findViewById(R.id.loader);
        mLoader.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mSwitch.setClickable(true);
        mSwitch.setFocusable(true);
        mLayout.setClickable(true);
        mLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check ;
                if(PermissionsInfo.mTrust == 0){
                    PermissionsInfo.mTrust = 1;
                    check = true ;
                }else {
                    PermissionsInfo.mTrust = 0;
                    check = false ;
                }
                Log.i(TAG, "onClick  state -- > "+check);
                mSwitch.setChecked(check);
            }
        });
        mOnCreate = true ;
    }
    
    public static void setchecked(int status) {
        mChangeFlag = true;
        boolean check = status== 0 ? false :true;
        mSwitch.setChecked(check);
        PermissionsInfo.getInstance().updateTrust(mContext, mPackageName, status);
    }
    
    private void queryTrustDB(){
        if(mPackageName == null){
        	return;
        }
        int count = 0;
        String[] projection = new String[]{"packagename","status"};
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Uri.parse(PermissionsInfo.TRUST_URI), projection,
                    "packagename = ?", new String[]{mPackageName}, null);
            while(cursor.moveToNext()) {
                count++;
                int state = cursor.getInt(1);
                PermissionsInfo.mTrust = state;
            }

            if(count == 0){
                insertTrustDB(mPackageName,0);
                PermissionsInfo.mTrust = 0;
            }
		}catch (Exception e) {
			// TODO: handle exception
			Log.i(TAG, "query DB exception");
		} finally{
			if (cursor != null)   cursor.close();
		}
    }
    
    private void insertTrustDB(String pkgName,int status){
        ContentResolver cr = getContentResolver(); 
        ContentValues cv = new ContentValues();
        cv.put("packagename", pkgName);
        cv.put("status", status); 
        cr.insert(Uri.parse(PermissionsInfo.TRUST_URI), cv);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        PrepareTustDbData();
        super.onResume();
        PermissionsInfo.sSavedPackageName = getIntent().getStringExtra(PACKAGENAME);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        Object  AmigoServerManager = (Object) mContext.getSystemService("amigoactivity");
        try{
                 Class cls = Class.forName("android.app.AmigoActivityManager");      
                 Method method = cls.getMethod("setPackagePermEnable", String.class);
                 method.invoke(AmigoServerManager,mPackageName);
        } catch (Exception ex) {
                  ex.printStackTrace();
        }
    };
    
    private void PrepareTustDbData(){
        queryTrustDB();
        boolean check = PermissionsInfo.mTrust== 0 ? false :true;
        mSwitch.setChecked(check);
//        mSwitch.setEnabled(false);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mChangeFlag){
                   mChangeFlag = false;
                }else {
                    if (mAdapter == null || mAdapter.isEmpty()) {
                        return;
                    }
                    Log.i(TAG, "onCheckedChanged state --> "+isChecked);
                    PermissionsInfo.mTrust = isChecked ? 1 : 0;
                    mAdapter.operatePermissionDB(isChecked);
                    PermissionsInfo.getInstance().updateTrust(mContext, mPackageName, PermissionsInfo.mTrust);
                }
            }
        });
    }
    
    private void PrepareData(){
        if(mAdapter == null){
            mAdapter = new PermissionAppDetailAdapter(this);   
        }
        mAdapter.changeItems(mPackageName);
        mSecurityPermission = mAdapter.haveSecurityPermissions();       
//        mSwitch.setEnabled(true);
    }
    
    private void refreshList(){
        if (mSecurityPermission) {
            mEmptyLayout.setVisibility(View.GONE);
            mLoader.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mListView.setAdapter(mAdapter);
            /*mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
                public void onGroupExpand(int groupPosition) {
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        if (groupPosition != i) {
                            mListView.collapseGroup(i);
                        }
                    }
                }
            });*/
        }else {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mLoader.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        }
    }
    
   /* @Override
    public void addPackage(String pkgName) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removePackage(String pkgName) {
        // TODO Auto-generated method stub
        if(mPackageName != null && mPackageName.equalsIgnoreCase(pkgName)){
            this.finish(); 
        }
    }*/
    
    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        // TODO Auto-generated method stub
        return new SoftManagerLoader_per(this);
    }

    @Override
    public void onLoadFinished(Loader<Object> arg0, Object arg1) {
        DebugUtil.d(TAG, " onLoadFinished PermissionsInfo.sSavedPackageName : "
                + PermissionsInfo.sSavedPackageName + "  mOnCreate : " + mOnCreate);
        if(PermissionsInfo.sSavedPackageName != null && !mOnCreate){
            mPackageName = PermissionsInfo.sSavedPackageName ;
            PermissionsInfo.sSavedPackageName = null ;    
        }
        PrepareData();
        refreshList();
        mOnCreate = false ; 
    }

    @Override
    public void onLoaderReset(Loader<Object> arg0) {
        // TODO Auto-generated method stub
    }
}
