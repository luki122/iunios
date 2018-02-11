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

import com.android.contacts.R;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Displays a list of call log entries.
 */
public class CallLogMultipleChoiceActivity extends CallLogMultipleDeleteActivity {
    private static final String TAG = "CallLogMultipleChoiceActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_log_choice_multiple_actions, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_add: {
                Intent intent = new Intent();
                String ids = mFragment.getSelections();
                intent.putExtra("calllogids", ids);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
            // All the options menu items are handled by onMenu... methods.
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }
    
    private void log(final String log) {
        Log.i(TAG, log);
    }
}
