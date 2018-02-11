/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import com.android.settings.R;
import com.google.common.collect.Lists;

import android.content.Context;
import aurora.preference.AuroraPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Creates a percentage bar chart inside a preference.
 */
public class UsageBarPreference extends AuroraPreference {
    /*private PercentageBarChart mChart = null;

    private final List<PercentageBarChart.Entry> mEntries = Lists.newArrayList();*/
	
	private TextView mText1;
	private ProgressBar mProgressBar;
	private  int mMax;
	private  int mProgress;
	private CharSequence mSummary;
	

    public UsageBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setLayoutResource(R.layout.preference_memoryusage);
        setLayoutResource(R.layout.aurora_storage_volume_prefs_layout);
    }

    public UsageBarPreference(Context context) {
        super(context);
        // qy modify
//        setLayoutResource(R.layout.preference_memoryusage);
        setLayoutResource(R.layout.aurora_storage_volume_prefs_layout);
    }

    public UsageBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setLayoutResource(R.layout.preference_memoryusage);
        setLayoutResource(R.layout.aurora_storage_volume_prefs_layout);
    }

    /*public void addEntry(int order, float percentage, int color) {
        mEntries.add(PercentageBarChart.createEntry(order, percentage, color));
        Collections.sort(mEntries);
    }*/

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        /*mChart = (PercentageBarChart) view.findViewById(R.id.percentage_bar_chart);
        mChart.setEntries(mEntries);*/
        setSelectable(false);
        mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
        mProgressBar.setProgress(mProgress);
        mProgressBar.setMax(mMax);

        mText1 = (TextView) view.findViewById(android.R.id.text1);
        mText1.setText(mSummary);
    }
    
    public void setProgressAndMax(int progress,int max){
    	mProgress = progress;
    	mMax = max;    
    	notifyChanged();
    }
    
    public void setSummary(CharSequence summary){
    	mSummary = summary;
    	notifyChanged();
    }

    /*public void commit() {
        if (mChart != null) {
            mChart.invalidate();
        }
    }

    public void clear() {
        mEntries.clear();
    }*/
}
