/*
 *
 * Copyright (C) 2011 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.importexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import aurora.widget.AuroraListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.mms.MmsApp;
import com.android.mms.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//gionee gaoj 2012 6-12 added for CR00624001 start
import android.graphics.Color;
//gionee gaoj 2012 6-12 added for CR00624001 end

import java.lang.reflect.Field;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
// gionee zhouyj 2012-05-28 add for CR00607938 start
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import java.io.FileFilter;
import com.aurora.featureoption.FeatureOption;
// gionee zhouyj 2012-05-28 add for CR00607938 end

public class MultiImportSmsActivity extends AuroraActivity implements View.OnClickListener, ServiceCallBack {
    private static final String TAG = "MultiImportSmsActivity";
    private AuroraAlertDialog mAlertDialog = null;
    private ImportExportInterface mService = null;
    private ImportExportSms mComponent = null;

    private MultiImportSmsAdapter mListAdapter;
    private AuroraListView mMultiImportList;
    private boolean mIsSelectedAll = false;
    private View mBottomView;
    private ArrayList<String> mImportFilePaths = null;
    private Toast mToast = null;
    private boolean mIsExternalCall = false;
    private ImportProAlertDialog mProAlertDialog = null;
    private AuroraAlertDialog mDialog = null;
    private AuroraAlertDialog mCancelAlertDialog = null;
    private static final int SHOW_DIALOG = 0;
    private static final int MENU_SELECT_CANCEL = 0;
    private static final int MENU_SELECT_DONE   = 1;
    private static final int MENU_SELECT_ALL    = 2;

    //gionee gaoj 2012-12-15 added for CR00725602 start
    private View mTitleView;
    private ImageButton mTitleBack;
    private TextView mTitleTextView;
    private CheckBox mTitleCheckBox;
    //gionee gaoj 2012-12-15 added for CR00725602 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //gionee gaoj 2012-5-30 added for CR00555790 start
        if (MmsApp.mTransparent) {
            setTheme(R.style.TabActivityTheme);
        } else if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkTheme) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-5-30 added for CR00555790 end
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //gionee gaoj 2012-12-15 added for CR00725602 start
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setCustomView(R.layout.gn_multi_action_view);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        mTitleView = getActionBar().getCustomView();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        mTitleBack = (ImageButton) mTitleView.findViewById(R.id.gn_action_view_back);
        mTitleBack.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        mTitleTextView = (TextView) mTitleView.findViewById(R.id.gn_select_count_text);
        mTitleCheckBox = (CheckBox) mTitleView.findViewById(R.id.gn_select_check_box);
        mTitleCheckBox.setOnClickListener(this);
        //gionee gaoj 2012-12-15 added for CR00725602 end
        checkAction();
        bindService();
        setContentView(R.layout.gn_multi_import_export);
        initViews(savedInstanceState);
        setListeners();
    }
    //gionee gaoj 2012-12-15 added for CR00725602 start
    private MenuItem mOneMenuItem;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.gn_one_menu, menu);
        /*mOneMenuItem.setEnabled(false);
        mOneMenuItem.setTitle(getResources().getString(R.string.gn_import_text));
        if (MmsApp.mLightTheme) {
            mOneMenuItem.setIcon(R.drawable.gn_import_normal_dis);
        } else {
            mOneMenuItem.setIcon(R.drawable.gn_import_normal_light_dis);
        }*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        mOneMenuItem = menu.findItem(R.id.selection);
        if (mListAdapter.getSelectedNumber() > 0) {
            /*if (MmsApp.mLightTheme) {
                mOneMenuItem.setIcon(R.drawable.gn_import_normal);
            } else {
                mOneMenuItem.setIcon(R.drawable.gn_import_normal_light);
            }*/
            mOneMenuItem.setEnabled(true);
        } else {
            /*if (MmsApp.mLightTheme) {
                mOneMenuItem.setIcon(R.drawable.gn_import_normal_dis);
            } else {
                mOneMenuItem.setIcon(R.drawable.gn_import_normal_light_dis);
            }*/
            mOneMenuItem.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    //gionee gaoj 2012-12-15 added for CR00725602 end

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.selection:
            startImport();
            break;
        case android.R.id.home:
            finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews(Bundle savedInstanceState) {
        mTitleTextView.setText(getResources().getString(R.string.gn_select_conversation_more, 0));
        mAlertDialog = new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
                                   .setTitle(getString(R.string.gn_import_failed))
//                                   .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                                   .setNegativeButton(getString(R.string.OK), new OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            // TODO Auto-generated method stub
                                            arg0.dismiss();
                                            if (mIsExternalCall) {
                                                setResult(ConfigConstantUtils.IMPORT_SMS_FAILED);
                                            }
                                            MultiImportSmsActivity.this.finish();
                                        }
                                    })
                                    .setOnKeyListener(new OnKeyListener() {
                                        @Override
                                        public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                                            if (arg1 == KeyEvent.KEYCODE_SEARCH) {
                                                return true;
                                            }
                                            return false;
                                        }
                                    })
                                    .create();
        mMultiImportList = (AuroraListView) findViewById(R.id.item_list);
        initListAdapter();
        mCancelAlertDialog =  new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
        .setTitle(getString(R.string.gn_import_cancel_title))
//        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
        .setMessage(getString(R.string.gn_import_cancel_msg))
        .setPositiveButton(getString(R.string.OK), new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                arg0.dismiss();
                if (null != mComponent) {
                    mComponent.setInterrupt(true);
                    mComponent.cancelImport();
                }
                try {
                    Field field = arg0.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(mDialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                if (mIsExternalCall) {
                    setResult(ConfigConstantUtils.IMPORT_SMS_FAILED);
                }
            }
        })
        .setNegativeButton(getString(R.string.Cancel), new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                arg0.dismiss();
                if (!mDialog.isShowing()) {
                    try {
                        Field field = mDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(mDialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mDialog.show();
                }
            }
        })
        .setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        })
        .create();
        mDialog = new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
        .setPositiveButton(getString(R.string.Cancel), new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                try {
                    Field field = arg0.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(arg0, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!mCancelAlertDialog.isShowing()) {
                    mCancelAlertDialog.show();
                }
            }
        })
        .create();
        mProAlertDialog = new ImportProAlertDialog(this);
        mDialog.setView(mProAlertDialog);
        // gionee zhouyj 2012-06-30 add for CR00632615 start 
        mDialog.setCanceledOnTouchOutside(false);
        // gionee zhouyj 2012-06-30 add for CR00632615 end 
        mToast = Toast.makeText(MultiImportSmsActivity.this, getString(R.string.gn_importing), 2000);
    }
    
    // gionee zhouyj 2012-11-22 add for CR00735432 start 
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        //Gionee <zhouyj> <2013-04-23> modify for CR00801232 start
        if ((mMultiImportList != null && mMultiImportList.getCount() < 1) || MmsApp.mIsSafeModeSupport) {
            finish();
        }
        //Gionee <zhouyj> <2013-04-23> modify for CR00801232 end
        // gionee zhouyj 2013-03-12 add for CR00783513 start
        mTitleCheckBox.setChecked(mIsSelectedAll);
        // gionee zhouyj 2013-03-12 add for CR00783513 end
        super.onResume();
    }
    // gionee zhouyj 2012-11-22 add for CR00735432 end 

    private void setListeners() {
        mDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int code, KeyEvent event) {
                // TODO Auto-generated method stub
                if (code == KeyEvent.KEYCODE_BACK) {
                    if (!mCancelAlertDialog.isShowing()) {
                        mCancelAlertDialog.show();
                    }
                } else if (code == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        });
    }

    private void initListAdapter() {
        mListAdapter = new MultiImportSmsAdapter(this);
        mListAdapter.setmData(getImportFilesPath());
        mListAdapter.setImportSmsAdapterCallBack(new MultiImportSmsAdapter.ImportSmsAdapterCallBack() {

            @Override
            public void onImportSmsAdapterCallBack(ArrayList<String> selectedPaths) {
                if (mListAdapter.getSelectedNumber() > 0) {
                    if (mListAdapter.isAllSelected()) {
                        mIsSelectedAll = true;
                        mTitleCheckBox.setChecked(true);
                    } else {
                        mIsSelectedAll = false;
                        mTitleCheckBox.setChecked(false);
                    }
                    mTitleTextView.setText(getResources().getString(R.string.gn_select_conversation_more, mListAdapter.getSelectedNumber()));
//                    mOneMenuItem.setEnabled(true);
                } else {
                    mIsSelectedAll = false;
//                    mOneMenuItem.setEnabled(false);
                    mTitleCheckBox.setChecked(false);
                    mTitleTextView.setText(getResources().getString(R.string.gn_select_conversation_more, 0));
                }
                // gionee zhouyj 2012-05-28 add for CR00607938 start
                invalidateOptionsMenu();
                // gionee zhouyj 2012-05-28 add for CR00607938 end
                mImportFilePaths = selectedPaths;
            }
        });
        mMultiImportList.setAdapter(mListAdapter);
    }

    private ArrayList<String> getImportFilesPath() {
        ArrayList<String> filePaths = new ArrayList<String>();
        /** move for CR00607938
        File fileDir = new File(ConfigConstantUtils.SDCARD_ROOT_PATH + getString(R.string.gn_import_export_file_path));
        File[] files = fileDir.listFiles();
        if (null != files) {
            for (int i=0;i<files.length;i++) {
                if (files[i].getName().endsWith(ConfigConstantUtils.IMPORT_EXPORT_SMS_FILE_EXTENSION)) {
                    filePaths.add(files[i].getAbsolutePath());
                }
            }
            Collections.sort(filePaths);
            Collections.reverse(filePaths);
        } else {
            if (mIsExternalCall) {
                setResult(ConfigConstantUtils.OTHER_ERROR);
            }
            this.finish();
        }*/
        // gionee zhouyj 2012-05-28 add for CR00607938 start
        filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCardPath + getString(R.string.gn_import_export_file_path), ".txt"));
        filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCardPath + "/message",".txt"));
        filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCardPath + getString(R.string.gn_import_export_file_path), ".db"));
        filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCardPath + "/message",".db"));
        // gionee zhouyj 2012-11-22 modify for CR00735432 start 
        if(ImportExportSmsActivity.mSDCard2Path != null && new File(ImportExportSmsActivity.mSDCard2Path).exists()) {
            filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCard2Path + getString(R.string.gn_import_export_file_path), ".txt"));
            filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCard2Path + "/message",".txt"));
            filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCard2Path + getString(R.string.gn_import_export_file_path), ".db"));
            filePaths.addAll(getFiles(ImportExportSmsActivity.mSDCard2Path + "/message",".db"));
        }
        // gionee zhouyj 2012-11-22 modify for CR00735432 end 
        Collections.sort(filePaths);
        // gionee zhouyj 2012-05-28 add for CR00607938 end
        return filePaths;
    }
    
    private ArrayList<String> getFiles(String path, String atrr) {
        ArrayList<String> filePaths = new ArrayList<String>();
        final String atr = atrr;
        File fileDir = new File(path);
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                if (!f.isDirectory() && f.getName().toLowerCase().endsWith(atr)
                        && f.canRead()) {
                    return true;
                }
                return false;
            }
        };
        File[] dbFiles = fileDir.listFiles(ff);
        if(dbFiles != null) {
            for(int i = 0;i < dbFiles.length; i++) {
                filePaths.add(dbFiles[i].getAbsolutePath());
            }
        }
        return filePaths;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkAction();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    private void markCheckedState(boolean checkedState) {
        int count = mMultiImportList.getChildCount();
        RelativeLayout layout = null;
        int childCount = 0;
        View view = null;
        for (int i = 0; i < count; i++) {
            layout = (RelativeLayout) mMultiImportList.getChildAt(i);
            childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);
                if (view instanceof CheckBox) {
                    ((CheckBox) view).setChecked(checkedState);
                    break;
                }
            }
        }
    }

    public void onClick(View v) {
        if (v == mTitleCheckBox) {
            mIsSelectedAll = !mIsSelectedAll;
            markCheckedState(mIsSelectedAll);
            if (mIsSelectedAll) {
                mListAdapter.setSelectedAll();
            } else {
                mListAdapter.cancelSelectedAll();
            }
            mOneMenuItem.setEnabled(mIsSelectedAll);
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (null != msg && null != msg.obj) {
                String path = msg.obj.toString();
                int index = mImportFilePaths.indexOf(path);
                if (-1 != index) {
                    if (index < mImportFilePaths.size()) {
                        if (msg.what != ConfigConstantUtils.IMPORT_FILE_READY) {
                            mDialog.dismiss();
                            if (mCancelAlertDialog.isShowing()) {
                                mCancelAlertDialog.dismiss();
                            }
                        }
                        String replace;
                        switch (msg.what) {
                            case ConfigConstantUtils.IMPORT_FILE_READY:
                                replace = pathToDescription(mImportFilePaths.get(index));
                                //Gionee <zhouyj> <2013-07-25> modify for CR00839119 begin
                                mProAlertDialog.setMessage(getString(R.string.gn_sms_importing) + "\"" + replace
                                        + "\"\n\n"+ getString(R.string.gn_import_current_file) + (index+1) + getString(R.string.gn_import_item) + getString(R.string.gn_import_export_sum)
                                        + mImportFilePaths.size() + getString(R.string.gn_import_export_piece));
                                //Gionee <zhouyj> <2013-07-25> modify for CR00839119 end
                                mHandler.sendEmptyMessage(SHOW_DIALOG);
                                break;

                            case ConfigConstantUtils.IMPORT_FILE_PATH_ERROR:
                                replace = pathToDescription(msg.obj.toString());
                                mAlertDialog.setMessage(getString(R.string.gn_import_file)
                                                        + replace + getString(R.string.gn_import_file_path_error)
                                                        + getImportedFileNames(index));
                                mAlertDialog.show();
                                break;

                            case ConfigConstantUtils.IMPORT_FILE_EMPTY:
                                replace = pathToDescription(msg.obj.toString());
                                mAlertDialog.setMessage(getString(R.string.gn_import_file)
                                                        + replace + getString(R.string.gn_import_file_empty)
                                                        + getImportedFileNames(index));
                                mAlertDialog.show();
                                break;

                            case ConfigConstantUtils.IMPORT_FILE_ERROR:
                                replace = pathToDescription(msg.obj.toString());
                                mAlertDialog.setMessage(getString(R.string.gn_import_file)
                                                        + replace + getString(R.string.gn_import_file_content_error)
                                                        + getImportedFileNames(index));
                                mAlertDialog.show();
                                break;

                            case ConfigConstantUtils.IMPORT_FILE_NOT_EXISTS:
                                replace = pathToDescription(msg.obj.toString());
                                mAlertDialog.setMessage(getString(R.string.gn_import_file)
                                        + replace + getString(R.string.gn_import_file_no_exists)
                                        + getImportedFileNames(index));
                                mAlertDialog.show();
                                break;
                            // gionee zhouyj 2012-05-28 add for CR00607938 start
                            case ConfigConstantUtils.IMPORT_COMPLETE_CODE:
                                mDialog.dismiss();
                                Toast.makeText(MultiImportSmsActivity.this, getString(R.string.gn_import_success), Toast.LENGTH_SHORT).show();
                                MultiImportSmsActivity.this.finish();
                                break;
                            // gionee zhouyj 2012-05-28 add for CR00607938 end
                            default:
                                break;
                        }
                    }
                }
            } else {
                if (msg.what == SHOW_DIALOG) {
                    try {
                        // gionee zhouyj 2012-07-18 modify for CR00651114 start 
                        if (!mDialog.isShowing() && !isFinishing() && !mAlertDialog.isShowing()) {
                            mDialog.show();
                        }
                        // gionee zhouyj 2012-07-18 modify for CR00651114 end 
                    } catch (Exception e){
                        Log.e(TAG, "SHOW DIALOG ERROR!!");
                        e.printStackTrace();
                    }
                }
            }
        };
    };

    private String getImportedFileNames(int index) {
        String fileNames = "";
        if (0 != index) {
            fileNames = "\n\n" + getString(R.string.gn_imported_file_names);
            String replace;
            for (int i=0;i<index;i++) {
                //gionee <gaoj> <2013-06-24> modify for CR00812909 begin
                /*replace = pathToDescription(mImportFilePaths.get(index));*/
                replace = pathToDescription(mImportFilePaths.get(i));
                //gionee <gaoj> <2013-06-24> modify for CR00812909 end
                fileNames = fileNames + "\n" + replace;
            }
        }
        return fileNames;
    }

    @Override
    public void callBack(int responseCode, Object obj) {
        Log.d(TAG, "MultiImportSmsActivity----------------callBack");
        if (null == obj) {
            obj = "";
        }
        if (responseCode == ConfigConstantUtils.IMPORT_COMPLETE_CODE) {
            //obj = obj.toString().substring(obj.toString().lastIndexOf("/") + 1);
            int index = mImportFilePaths.indexOf(obj);
            if (-1 != index) {
                if (index < mImportFilePaths.size() - 1) {
                    if (null != mService) {
                        mHandler.sendEmptyMessage(SHOW_DIALOG);
                        mService.importComponent(mImportFilePaths.get(index+1));
                    }
                } else {
                    SharedPreferences preferences = getSharedPreferences(ConfigConstantUtils.IMPORT_EXPORT_SMS_PRE, Context.MODE_PRIVATE);
                    Editor editor = preferences.edit();
                    editor.putString("action", "import");
                    editor.putString("lastDate", ConfigConstantUtils.formatDate2String(ConfigConstantUtils.DATE_THIRD_TYPE, System.currentTimeMillis()));
                    editor.commit();
                    mDialog.dismiss();
                    Toast.makeText(this, getString(R.string.gn_import_success), Toast.LENGTH_SHORT).show();
                    if (mIsExternalCall) {
                        setResult(ConfigConstantUtils.IMPORT_COMPLETE_CODE);
                    }
                    setResult(RESULT_OK);
                    MultiImportSmsActivity.this.finish();
                }
            } else {
                mDialog.dismiss();
            }
            return;
        } else if (mDialog.isShowing()) {
            Log.d(TAG, "MultiImportSmsActivity----------------dialog dismiss");
            mDialog.dismiss();
            if (mCancelAlertDialog.isShowing()) {
                mCancelAlertDialog.dismiss();
            }
        }
        switch (responseCode) {
            case ConfigConstantUtils.NO_SDCARD:
                Toast.makeText(this, getString(R.string.gn_no_sdcard), Toast.LENGTH_SHORT).show();
                break;

            case ConfigConstantUtils.OTHER_ERROR:
                Toast.makeText(this, getString(R.string.gn_unkonw_error), Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "MultiImportSmsActivity----------------onServiceDisconnected");
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            Log.d(TAG, "MultiImportSmsActivity----------------onServiceConnected");
            mService = (ImportExportInterface) service;
        }
    };

    private void bindService() {
        Intent mIntent = new Intent(ConfigConstantUtils.SERVICE_ACTION);
        bindService(mIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private void startImport() {
        if (null != mImportFilePaths && mImportFilePaths.size() > 0) {
            Collections.sort(mImportFilePaths);
            Collections.reverse(mImportFilePaths);
            if (!mDialog.isShowing()) {
                Log.d(TAG, "MultiImportSmsActivity----------------startExport..........show dialog");
//                mDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                mDialog.setTitle(getString(R.string.gn_sms_importing));
            }
            mComponent = (ImportExportSms) ImportExportFactory.getImportExportComponentInstance(this, ImportExportFactory.SMS_COMPONENT);
            mComponent.setHandler(mHandler);
            mService.setImportExportComponent(mComponent);
            mService.setCallBack(this);
            mService.importComponent(mImportFilePaths.get(0));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        mService = null;
        if (null != mComponent) {
            mComponent.setInterrupt(true);
            mComponent.cancelImport();
        }
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        if (mCancelAlertDialog.isShowing()) {
            mCancelAlertDialog.dismiss();
        }
    }

    private void checkAction() {
        if (null != getIntent().getAction() && getIntent().getAction().equals(ConfigConstantUtils.EXPORT_SMS_ACTION)) {
            mIsExternalCall = false;
        } else {
            mIsExternalCall = true;
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        if (mIsExternalCall) {
            setResult(ConfigConstantUtils.IMPORT_SMS_CANCEL);
        }
    }

    private String pathToDescription(String path) {
        if(ImportExportSmsActivity.getSdcardCount() < 2) {
            if(path.startsWith(ImportExportSmsActivity.mSDCardPath)) {
                path = path.replaceFirst(ImportExportSmsActivity.mSDCardPath, getString(R.string.gn_chooser_internal_sdcard));
            }
        } else {
            if(FeatureOption.MTK_2SDCARD_SWAP) {
                if(path.startsWith(ImportExportSmsActivity.mSDCard2Path)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCard2Path, getString(R.string.gn_chooser_internal_sdcard));
                } else if(path.startsWith(ImportExportSmsActivity.mSDCardPath)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCardPath, getString(R.string.gn_chooser_external_sdcard));
                }
            } else {
                if(path.startsWith(ImportExportSmsActivity.mSDCard2Path)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCard2Path, getString(R.string.gn_chooser_external_sdcard));
                } else if(path.startsWith(ImportExportSmsActivity.mSDCardPath)) {
                    path = path.replaceFirst(ImportExportSmsActivity.mSDCardPath, getString(R.string.gn_chooser_internal_sdcard));
                }
            }
        }
        return path;
    }
}

class ImportProAlertDialog extends LinearLayout {
    public ImportProAlertDialog(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.gn_import_dialog, this);
    }

    public void setMessage(String text) {
        ((TextView) findViewById(R.id.text)).setText(text);
        //gionee gaoj 2012 6-12 added for CR00624001 start
//        if (MmsApp.mTransparent) {
            TextView textView = (TextView) findViewById(R.id.text);
            if (textView != null) {
                textView.setTextColor(Color.BLACK);
            }
//        }
        //gionee gaoj 2012 6-12 added for CR00624001 end
    }
}