/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.gallery3d.filtershow.category;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.CustomActionBarManager.ActionBarType;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.util.InstallUtils;
import com.android.gallery3d.util.StatisticsUtils;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;

import com.android.gallery3d.util.InstallUtils;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import com.android.gallery3d.util.Globals;

public class CategoryPanel extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "CategoryPanel";
    private static final String PARAMETER_TAG = "currentPanel";

    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //ORIGINALLY:
    //private CategoryAdapter mAdapter;
    //private int mCurrentAdapter = MainPanel.LOOKS;
    //SQF MODIFIED TO:
    //private MainPanel mMainPanel;
    private AuroraCategoryAdapter mAdapter;
    private int mCurrentAdapter = MainPanel.MAIN_MENU;
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    private IconView mAddButton;

    //Aurora <SQF> <2014-09-01>  for NEW_UI begin
    /*
    public void setMainPanel(MainPanel mainPanel) {
    	mMainPanel = mainPanel;
    }
    */
    //Aurora <SQF> <2014-09-01>  for NEW_UI end

    public void setAdapter(int value) {
        mCurrentAdapter = value;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		//refreshButtons();
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadAdapter(mCurrentAdapter);
    }

    public void loadAdapter(int adapter) {
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        
        //Aurora <SQF> <2014-08-29>  for NEW_UI begin
        //ORIGINALLY:
        /*
        switch (adapter) {
            case MainPanel.LOOKS: {
                mAdapter = activity.getCategoryLooksAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.LOOKS);
                }
                activity.updateCategories();
                break;
            }
            case MainPanel.BORDERS: {
                mAdapter = activity.getCategoryBordersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.BORDERS);
                }
                activity.updateCategories();
                break;
            }
            case MainPanel.GEOMETRY: {
                mAdapter = activity.getCategoryGeometryAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.GEOMETRY);
                }
                break;
            }
            case MainPanel.FILTERS: {
                mAdapter = activity.getCategoryFiltersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.FILTERS);
                }
                break;
            }
            case MainPanel.VERSIONS: {
                mAdapter = activity.getCategoryVersionsAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.VERSIONS);
                }
                break;
            }
        }
        updateAddButtonVisibility();
        */
        //SQF MODIFIED TO:
        switch(adapter) {
        case MainPanel.MAIN_MENU: 
        	mAdapter = activity.getCategoryAdapter();
        	break;
        //case MainPanel.FILTERS:
        //	mAdapter = activity.getCategoryFiltersAdapter();
        default:
        	break;
        }
        //Aurora <SQF> <2014-08-29>  for NEW_UI end
        
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(PARAMETER_TAG, mCurrentAdapter);
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	
    	//Log.i("SQF_LOG", "CategoryPanel::onCreateView-------------------");
    	
        LinearLayout main = (LinearLayout) inflater.inflate(R.layout.filtershow_category_panel_new, container, false);
        if (savedInstanceState != null) {
            int selectedPanel = savedInstanceState.getInt(PARAMETER_TAG);
            loadAdapter(selectedPanel);
        }

        View panelView = main.findViewById(R.id.listItems);
        if (panelView instanceof CategoryTrack) {
            CategoryTrack panel = (CategoryTrack) panelView;
            if (mAdapter != null) {
                //mAdapter.setOrientation(CategoryView.HORIZONTAL);//SQF ANNOTATED ON 2014-08-30
                panel.setAdapter(mAdapter);
                mAdapter.setContainer(panel);
            }
        } else if (mAdapter != null) {
            ListView panel = (ListView) main.findViewById(R.id.listItems);
            panel.setAdapter(mAdapter);
            mAdapter.setContainer(panel);
        }
        //Aurora <SQF> <2014-08-30>  for NEW_UI begin
        //SQF ANNOTATED ON 2014-08-30
        /*
        mAddButton = (IconView) main.findViewById(R.id.addButton);
        if (mAddButton != null) {
            mAddButton.setOnClickListener(this);
            updateAddButtonVisibility();
        }
        */
        //Aurora <SQF> <2014-08-30>  for NEW_UI end
        
        //Aurora <SQF> <2014-09-01>  for NEW_UI begin
        if(mCurrentAdapter == MainPanel.MAIN_MENU) {
	        View view = panelView.findViewById(R.id.main_menu_crop_btn);
	    	view.setOnClickListener(this);
	    	view = panelView.findViewById(R.id.main_menu_rotate_btn);
	     	view.setOnClickListener(this);
	     	view = panelView.findViewById(R.id.main_menu_filters_btn);
	     	view.setOnClickListener(this);

        } 
        return main;
    }
    
    //paul add <2015-10-27> start
    private boolean mIsEditLoading = false;
	private Handler mEditLoadingHandler = new Handler();
	private boolean setEditLoading(){
		if(mIsEditLoading){
			return true;
		}
		mIsEditLoading = true;
		mEditLoadingHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mIsEditLoading = false;
			}
		}, 500);
		return false;
	}
	//paul add <2015-10-27> end
    @Override
    public void onClick(View v) {
    	//Log.i("SQF_LOG", "CategoryPanel onClick ...");
    	//if(! MasterImage.getImage().getIsBitmapLoaded()) return;
    	if(! MasterImage.getImage().isPreviewUpdateFinish() ) return;
    	
    	FilterShowActivity activity = (FilterShowActivity) getActivity();
    	AuroraAction action = null;
    	FilterRepresentation representation = null;
        switch (v.getId()) {
        //Aurora <SQF> <2014-09-01>  for NEW_UI begin
        //SQF ANNOTATED ON 2014-09-01
        	/*
            case R.id.addButton:
                FilterShowActivity activity = (FilterShowActivity) getActivity();
                activity.addCurrentVersion();
                break;
                */
        //Aurora <SQF> <2014-09-01>  for NEW_UI end
        
        //Aurora <SQF> <2014-09-01>  for NEW_UI begin
        case R.id.main_menu_crop_btn:
			if(setEditLoading()) return;//paul add <2015-10-27>
        	action = mAdapter.getItem(0);
        	representation = action.getFilterRepresentation();
        	activity.showRepresentation(representation);
        	break;
        
        case R.id.main_menu_rotate_btn:
			if(setEditLoading()) return;//paul add <2015-10-27>
        	action = mAdapter.getItem(1);
        	representation = action.getFilterRepresentation();
        	activity.showRepresentation(representation);
        	break;
        case R.id.main_menu_filters_btn:
			if(setEditLoading()) return;//paul add <2015-10-27>
        	action = mAdapter.getItem(2);
        	activity.loadAuroraEffectsPanel(true);
        	break;
        //Aurora <SQF> <2014-09-01>  for NEW_UI end

        } 
    }
}
