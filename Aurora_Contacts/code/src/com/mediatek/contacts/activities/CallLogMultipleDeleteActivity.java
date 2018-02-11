/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.contacts.activities;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.ActionBar;
import android.app.ProgressDialog;

import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.mediatek.contacts.calllog.CallLogMultipleDeleteFragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.StatusBarManager;
import android.os.SystemProperties;
import android.provider.Settings;

import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import android.content.Context;
import android.widget.Toast;
import android.app.DialogFragment;
import android.app.Dialog;
import android.content.DialogInterface;
import gionee.app.GnStatusBarManager;
// import gionee.provider.GnSettings;

import aurora.app.AuroraActivity;
import aurora.app.AuroraProgressDialog;


/**
 * Displays a list of call log entries.
 */
public class CallLogMultipleDeleteActivity extends AuroraActivity {
    private static final String TAG = "CallLogMultipleDeleteActivity";

    protected CallLogMultipleDeleteFragment mFragment;
    
    public StatusBarManager mStatusBarMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.call_log_multiple_delete_activity);

        // Typing here goes to the dialer
        //setDefaultKeyMode(DEFAULT_KEYS_DIALER);

        mFragment = (CallLogMultipleDeleteFragment) getFragmentManager().findFragmentById(
                R.id.call_log_fragment);
        configureActionBar();
        updateSelectedItemsView(0);
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_log_delete_multiple_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // This action deletes all elements in the group from the call log.
        // We don't have this action for voicemails, because you can just use the trash button.
//        menu.findItem(R.id.menu_remove_from_call_log).setVisible(!hasVoicemail());
//        menu.findItem(R.id.menu_edit_number_before_call).setVisible(mHasEditNumberBeforeCall);
//        menu.findItem(R.id.menu_trash).setVisible(hasVoicemail());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_select_all: {
                updateSelectedItemsView(mFragment.selectAllItems());
                return true;
            }

            case R.id.menu_unselect_all: {
                mFragment.unSelectAllItems();
                updateSelectedItemsView(0);
                return true;
            }

            case R.id.menu_delete: {
                if (mFragment.getSelectedItemCount() == 0) {
                    Toast.makeText(this, R.string.multichoice_no_select_alert,
                                   Toast.LENGTH_SHORT).show();
                    return true;
                }
                ConfirmDialog cDialog = new ConfirmDialog();
                cDialog.setTargetFragment(mFragment, 0);
                cDialog.setArguments(mFragment.getArguments());
                cDialog.show(mFragment.getFragmentManager(), "cDialog");
                //mFragment.deleteSelectedCallItems();
                //updateSelectedItemsView(0);
                return true;
            }
            // All the options menu items are handled by onMenu... methods.
            default:
                throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
            setSimIndicatorVisibility(true);
        }
    }

    @Override
    protected void onPause() {
    	
        if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
            setSimIndicatorVisibility(false);
        }
        
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        this.finish();
    }
    private void configureActionBar() {
        log("configureActionBar()");
        // Inflate a custom action bar that contains the "done" button for
        // multi-choice
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.call_log_multiple_delete_custom_action_bar, null);
        ImageButton doneMenuItem = (ImageButton) customActionBarView.findViewById(R.id.done_menu_item);
        doneMenuItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                                        ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                                        | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
        }
    }

    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            GnStatusBarManager.showSIMIndicator(mStatusBarMgr, getComponentName(), ContactsFeatureConstants.VOICE_CALL_SIM_SETTING);
        else
            GnStatusBarManager.hideSIMIndicator(mStatusBarMgr, getComponentName());
    }

    public void updateSelectedItemsView(final int checkedItemsCount) {
        TextView selectedItemsView = (TextView) getActionBar().getCustomView().findViewById(R.id.select_items);
        if (selectedItemsView == null) {
            log("Load view resource error!");
            return;
        }
        selectedItemsView.setText(getString(R.string.selected_item_count, checkedItemsCount));
    }

    private void log(final String log) {
        Log.i(TAG, log);
    }
    
    public class ConfirmDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this.getActivity())
                    .setTitle(R.string.deleteCallLogConfirmation_title)
                    .setIcon(R.drawable.ic_dialog_alert_holo_light)
                    .setMessage(R.string.deleteCallLogConfirmation_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mFragment.deleteSelectedCallItems();
                                updateSelectedItemsView(0);
                            }
                        });
            return builder.create();
        }
    }
}
