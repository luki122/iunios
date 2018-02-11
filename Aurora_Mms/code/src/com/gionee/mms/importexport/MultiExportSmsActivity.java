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

import java.util.ArrayList;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import aurora.widget.AuroraListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.mms.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.android.mms.MmsApp;
import com.gionee.mms.ui.ConvFragment;
import com.gionee.mms.ui.MsgChooseLockPassword;
// gionee zhouyj 2012-05-28 add for CR00607938 start
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.aurora.featureoption.FeatureOption;
// gionee zhouyj 2012-05-28 add for CR00607938 end
//Gionee <zhouyj> <2012-12-17> add for CR00802407 begin
import java.lang.reflect.Field;
//Gionee <zhouyj> <2012-12-17> add for CR00802407 end

public class MultiExportSmsActivity extends AuroraActivity implements View.OnClickListener, ServiceCallBack {
    private static final String TAG = "MultiExportSmsActivity";
    private static final int SMS_LIST_QUERY_TOKEN = 1;
    private AuroraProgressDialog mDialog = null;
    private ImportExportInterface mService = null;
    private ImportExportSms mComponent = null;

    private SmsListQueryHandler mQueryHandler;
    private MultiExportSmsAdapter mListAdapter;
    private AuroraListView mMultiExportList;
    private boolean mIsSelectedAll = false;
    private ContentResolver mContentResolver;
    private ArrayList<String> mThreadIds = new ArrayList<String>();
    private boolean mIsExporting = false;
    private String mExportFilePath = null;
    private boolean mIsExternalCall = false;
    private static final int MENU_SELECT_CANCEL = 0;
    private static final int MENU_SELECT_DONE   = 1;
    private static final int MENU_SELECT_ALL    = 2;
    // gionee zhouyj 2012-09-07 add for CR00687783 start
    private boolean mIsShowingNoSpaceDialog = false;
    // gionee zhouyj 2012-09-07 add for CR00687783 end

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
        mContentResolver = getContentResolver();
        mQueryHandler = new SmsListQueryHandler(mContentResolver);
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
        mOneMenuItem.setTitle(getResources().getString(R.string.gn_export_text));
        if (MmsApp.mLightTheme) {
            mOneMenuItem.setIcon(R.drawable.gn_export_normal_dis);
        } else {
            mOneMenuItem.setIcon(R.drawable.gn_export_normal_light_dis);
        }*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        mOneMenuItem = menu.findItem(R.id.selection);
        if (mListAdapter.getSelectedNumber() > 0) {
            /*if (MmsApp.mLightTheme) {
                mOneMenuItem.setIcon(R.drawable.gn_export_normal);
            } else {
                mOneMenuItem.setIcon(R.drawable.gn_export_normal_light);
            }*/
            mOneMenuItem.setEnabled(true);
        } else {
            /*if (MmsApp.mLightTheme) {
                mOneMenuItem.setIcon(R.drawable.gn_export_normal_dis);
            } else {
                mOneMenuItem.setIcon(R.drawable.gn_export_normal_light_dis);
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
            if (MmsApp.mEncryption) {
                if (mListAdapter.checkEncryption()) {
                    final Intent intent = new Intent();
                    intent.setClass(this, MsgChooseLockPassword.class);
                    intent.putExtra("isdecryption", true);
                    startActivityForResult(intent,
                            ConvFragment.UPDATE_PASSWORD_REQUEST);
                    } else {
                        startExport();
                    }
                } else {
                    startExport();
             }
            break;
        case android.R.id.home:
            finish();
            break;
        default:
            break;
        }
        return true;
    }

    private void initViews(Bundle savedInstanceState) {
        mTitleTextView.setText(getResources().getString(R.string.gn_select_conversation_more, 0));
        mDialog = new AuroraProgressDialog(this);
        mMultiExportList = (AuroraListView) findViewById(R.id.item_list);
        initListAdapter();
    }

    //Gionee <zhouyj> <2013-06-25> modify for CR00825790 begin
    private void setListeners() {
        mDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int arg1, KeyEvent arg2) {
                // TODO Auto-generated method stub
                if (arg1 == KeyEvent.KEYCODE_BACK && mIsExporting) {
                    cancelExportDialog(dialog);
                } else if (arg1 == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        });
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                cancelExportDialog(dialog);
            }
        });
    }
    
    private void cancelExportDialog(DialogInterface dialog) {
        dialog.dismiss();
        if (null != mComponent) {
            mComponent.setInterrupt(true);
        }
        ConfigConstantUtils.deleteSmsFile(mExportFilePath);
        mExportFilePath = null;
        Toast.makeText(MultiExportSmsActivity.this, getString(R.string.gn_export_failed), Toast.LENGTH_SHORT).show();
        if (mIsExternalCall) {
            setResult(ConfigConstantUtils.EXPORT_SMS_CANCEL);
        }
    }
    //Gionee <zhouyj> <2013-06-25> modify for CR00825790 end

    private final MultiExportSmsAdapter.OnContentChangedListener mContentChangedListener = new MultiExportSmsAdapter.OnContentChangedListener() {
        @Override
        public void onContentChanged(MultiExportSmsAdapter adapter) {
            startAsyncQuery(SMS_LIST_QUERY_TOKEN);
        }
    };

    private void initListAdapter() {
        mListAdapter = new MultiExportSmsAdapter(this, null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        mListAdapter.setExportSmsAdapterCallBack(new MultiExportSmsAdapter.ExportSmsAdapterCallBack() {

            @Override
            public void onExportSmsAdapterCallBack(ArrayList<String> threadIds) {
                // TODO Auto-generated method stub
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
                    mTitleCheckBox.setChecked(false);
                    mTitleTextView.setText(getResources().getString(R.string.gn_select_conversation_more, 0));
//                    mOneMenuItem.setEnabled(false);
                }
                invalidateOptionsMenu();
                mThreadIds = threadIds;
            }
        });
        mMultiExportList.setAdapter(mListAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkAction();
        startAsyncQuery(SMS_LIST_QUERY_TOKEN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        startAsyncQuery(SMS_LIST_QUERY_TOKEN);
        //Gionee <zhouyj> <2013-04-23> modify for CR00801232 start
        // gionee zhouyj 2012-12-19 add for CR00746519 start 
        if (ImportExportSmsActivity.getSdcardCount() < 1 || MmsApp.mIsSafeModeSupport) {
            finish();
        }
        // gionee zhouyj 2012-12-19 add for CR00746519 end 
        //Gionee <zhouyj> <2013-04-23> modify for CR00801232 end
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mListAdapter.changeCursor(null);
    }

    private static final String[] PROJECTION = new String[] {Sms._ID, Sms.THREAD_ID, Sms.ADDRESS, "count(" + Sms.THREAD_ID + ")"};
    private void startAsyncQuery(int token) {
        setProgressBarIndeterminateVisibility(true);
        switch (token) {
            case SMS_LIST_QUERY_TOKEN:
                mQueryHandler.startQuery(SMS_LIST_QUERY_TOKEN, null, ConfigConstantUtils.IMPORT_EXPORT_SMS_QUERY_URI, PROJECTION, Sms.TYPE + " != " + Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT + ") GROUP BY (" + Sms.THREAD_ID, null, Sms.DATE + " DESC");
                break;

            default:
                Log.e(TAG, "invalid token");
        }
    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    private final class SmsListQueryHandler extends AsyncQueryHandler {
        public SmsListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            setProgressBarIndeterminateVisibility(false);
            switch (token) {
                case SMS_LIST_QUERY_TOKEN:
                    mListAdapter.setmData(cursor);
                    mListAdapter.changeCursor(cursor);
                    if (mListAdapter.isAllSelected()) {
                        mIsSelectedAll = true;
                        mTitleCheckBox.setChecked(true);
                    } else {
                        mIsSelectedAll = false;
                        mTitleCheckBox.setChecked(false);
                    }
                    return;

                default:
                    Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }
    }

    private void markCheckedState(boolean checkedState) {
        int count = mMultiExportList.getChildCount();
        RelativeLayout layout = null;
        int childCount = 0;
        View view = null;
        for (int i = 0; i < count; i++) {
            layout = (RelativeLayout) mMultiExportList.getChildAt(i);
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
//            mDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
            mDialog.setTitle(getString(R.string.gn_sms_exporting));
            mDialog.setCanceledOnTouchOutside(false);
            if (null != msg && null != mExportFilePath && mIsExporting) {
                String replace = pathToDescription(mExportFilePath);
                mDialog.setMessage(getString(R.string.gn_exporting_front) + "\"" + replace + "\""
                                + getString(R.string.gn_exporting_back) + "\n\n" + getString(R.string.gn_export_current_session)
                                + msg.what + getString(R.string.gn_export_item) + getString(R.string.gn_import_export_sum)
                                + mThreadIds.size() + getString(R.string.gn_import_export_piece));
            }
            if (!mDialog.isShowing()) {
                //Gionee <zhouyj> <2012-12-17> modify for CR00802407 begin
                if (isResume() && !mIsShowingNoSpaceDialog && !mComponent.isInterrupt()) {
                    mDialog.show();
                }
                //Gionee <zhouyj> <2012-12-17> modify for CR00802407 end
            }
        };
    };
    
    //Gionee <zhouyj> <2012-12-17> add for CR00802407 begin
    private boolean isResume() {
        try {
            Field field = MultiExportSmsActivity.this.getClass().getSuperclass().getSuperclass().getDeclaredField("mResumed");
            if (field != null) {
                field.setAccessible(true);
                return field.getBoolean(MultiExportSmsActivity.this);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    //Gionee <zhouyj> <2012-12-17> add for CR00802407 end

    @Override
    public void callBack(int responseCode, final Object obj) {
        // TODO Auto-generated method stub
        Log.d(TAG, "MultiExportSmsActivity----------------callBack");
        if (mDialog.isShowing()) {
            Log.d(TAG, "MultiExportSmsActivity----------------dialog dismiss");
            mDialog.dismiss();
        }
        mIsExporting = false;
        switch (responseCode) {
            case ConfigConstantUtils.EXPORT_COMPLETE_CODE:
                SharedPreferences preferences = getSharedPreferences(ConfigConstantUtils.IMPORT_EXPORT_SMS_PRE, Context.MODE_PRIVATE);
                Editor editor = preferences.edit();
                editor.putString("action", "export");
                editor.putString("lastDate", ConfigConstantUtils.formatDate2String(ConfigConstantUtils.DATE_THIRD_TYPE, System.currentTimeMillis()));
                editor.commit();
                Toast.makeText(this, getString(R.string.gn_export_success), Toast.LENGTH_SHORT).show();
                if (mIsExternalCall) {
                    setResult(ConfigConstantUtils.EXPORT_COMPLETE_CODE);
                }
                setResult(RESULT_OK);
                MultiExportSmsActivity.this.finish();
                break;
            case ConfigConstantUtils.EXPORT_FILE_READY:
                String msg = "";
                if (null != obj) {
                    mExportFilePath = obj.toString();
                    String replace = pathToDescription(mExportFilePath);
                    msg = getString(R.string.gn_sure_export_front) + "\"" + replace + "\"" + getString(R.string.gn_sure_export_back);
                }
                new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
                                .setTitle(" " + getString(R.string.gn_sure_export))
                                .setMessage(msg)
                                .setPositiveButton(getString(R.string.gn_cancel_all_favorite_ok), new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        // TODO Auto-generated method stub
                                        arg0.dismiss();
                                        if (null != mService) {
                                            mIsExporting = true;
                                            mService.exportComponent();
                                        }
                                    }
                                })
                                .setNeutralButton(getString(R.string.gn_cancel_all_favorite_cancel_btn), new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        ConfigConstantUtils.deleteSmsFile(mExportFilePath);
                                        mExportFilePath = null;
                                        arg0.dismiss();
                                    }
                                })
                                .setOnKeyListener(new OnKeyListener() {
                                    @Override
                                    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                                        // TODO Auto-generated method stub
                                        if (arg1 == KeyEvent.KEYCODE_BACK) {
                                            ConfigConstantUtils.deleteSmsFile(mExportFilePath);
                                            mExportFilePath = null;
                                        } else if (arg1 == KeyEvent.KEYCODE_SEARCH) {
                                            return true;
                                        }
                                        return false;
                                    }
                                })
                                .show()
                                .setCanceledOnTouchOutside(false);
                break;

            case ConfigConstantUtils.NO_SDCARD:
                Toast.makeText(this, getString(R.string.gn_no_sdcard), Toast.LENGTH_SHORT).show();
                break;
            case ConfigConstantUtils.SDCARD_NO_SPACE:
                // gionee zhouyj 2012-09-07 add for CR00687783 start
                mIsShowingNoSpaceDialog = true;
                // gionee zhouyj 2012-09-07 add for CR00687783 end
                ConfigConstantUtils.deleteSmsFile(mExportFilePath);
                mExportFilePath = null;
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                // gionee zhouyj 2012-08-07 add for CR00667794 start 
                boolean isSdcard = ImportExportSmsActivity.mDefaultSDCardPath == ImportExportSmsActivity.mSDCardPath;
                // gionee zhouyj 2012-08-07 add for CR00667794 end 
                new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
//                                .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                                .setTitle(getString(R.string.gn_export_failed))
                                 // gionee zhouyj 2012-08-07 modify for CR00667794 start 
                                .setMessage(getString(isSdcard ? R.string.gn_sdcard_no_space : R.string.gn_internal_sdcard_no_space))
                                 // gionee zhouyj 2012-08-07 modify for CR00667794 end 
                                .setPositiveButton(getString(R.string.OK), new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        // TODO Auto-generated method stub
                                        arg0.dismiss();
                                        if (mIsExternalCall) {
                                            setResult(ConfigConstantUtils.EXPORT_SMS_FAILED);
                                        }
                                        MultiExportSmsActivity.this.finish();
                                    }
                                })
                                .setOnKeyListener(new OnKeyListener() {

                                    @Override
                                    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                                        // TODO Auto-generated method stub
                                        if (arg1 == KeyEvent.KEYCODE_BACK) {
                                            arg0.dismiss();
                                            if (mIsExternalCall) {
                                                setResult(ConfigConstantUtils.EXPORT_SMS_FAILED);
                                            }
                                            MultiExportSmsActivity.this.finish();
                                        } else if (arg1 == KeyEvent.KEYCODE_SEARCH) {
                                            return true;
                                        }
                                        return false;
                                    }
                                })
                                .show()
                                .setCanceledOnTouchOutside(false);
                break;
            default:
                break;
        }
    }

    ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
            Log.d(TAG, "MultiExportSmsActivity----------------onServiceDisconnected");
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            // TODO Auto-generated method stub
            Log.d(TAG, "MultiExportSmsActivity----------------onServiceConnected");
            mService = (ImportExportInterface) service;
        }
    };

    private void bindService() {
        Intent mIntent = new Intent(ConfigConstantUtils.SERVICE_ACTION);
        bindService(mIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private void startExport() {
        mComponent = (ImportExportSms) ImportExportFactory.getImportExportComponentInstance(this, ImportExportFactory.SMS_COMPONENT);
        mComponent.setmThreadIds(mThreadIds);
        mComponent.setHandler(mHandler);
        mService.setImportExportComponent(mComponent);
        mService.setCallBack(this);
        mService.exportComponent();
    }
    
    // gionee zhouyj 2012-10-11 add for CR00710370 start 
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if(mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onPause();
    }
    // gionee zhouyj 2012-10-11 add for CR00710370 end 

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(connection);
        // gionee zhouyj 2012-09-07 add for CR00687783 start
        mIsShowingNoSpaceDialog = false;
        // gionee zhouyj 2012-09-07 add for CR00687783 end
        mService = null;
        ConfigConstantUtils.deleteEmptySmsFile(mExportFilePath);
        mExportFilePath = null;
        if (mDialog.isShowing()) {
            mDialog.dismiss();
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
            setResult(ConfigConstantUtils.EXPORT_SMS_CANCEL);
        }
        // gionee zhouyj 2012-10-11 add for CR00710370 start 
        if(mDialog.isShowing()) {
            mDialog.dismiss();
        }
        // gionee zhouyj 2012-10-11 add for CR00710370 end 
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case ConvFragment.UPDATE_PASSWORD_REQUEST:
                if (data != null && data.getAction().equals("confirm")) {
                    startExport();
                }
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
