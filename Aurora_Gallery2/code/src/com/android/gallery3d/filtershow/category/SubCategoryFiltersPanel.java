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

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;

public class SubCategoryFiltersPanel extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "SubCategoryFiltersPanel";
    private static final String PARAMETER_TAG = "currentPanel";

    private AuroraCategoryAdapter mAdapter;
    private int mCurrentAdapter = MainPanel.FILTERS;
    
    private View mPanelView;
    private ArrayList<AuroraEffectView> mAuroraEffectViews = new ArrayList<AuroraEffectView>();
      
    public void setAdapter(int value) {
        mCurrentAdapter = value;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Log.i("SQF_LOG", "onAttach SubCategoryFiltersPanel----->");
        loadAdapter(mCurrentAdapter);
    }

    public void loadAdapter(int adapter) {
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        mAdapter = activity.getCategoryFiltersAdapter();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(PARAMETER_TAG, mCurrentAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout main = (LinearLayout) inflater.inflate(R.layout.filtershow_sub_category_panel_filters, container, false);
        if (savedInstanceState != null) {
            int selectedPanel = savedInstanceState.getInt(PARAMETER_TAG);
            loadAdapter(selectedPanel);
        }

        mPanelView = main.findViewById(R.id.sub_category_listItems);
        if (mPanelView instanceof CategoryTrack) {
            CategoryTrack panel = (CategoryTrack) mPanelView;
            if (mAdapter != null) {
                //mAdapter.setOrientation(CategoryView.HORIZONTAL);//SQF ANNOTATED ON 2014-08-30
                panel.setAdapter(mAdapter);
                mAdapter.setContainer(panel);
            }
        } else if (mAdapter != null) {
            ListView panel = (ListView) main.findViewById(R.id.sub_category_listItems);
            panel.setAdapter(mAdapter);
            mAdapter.setContainer(panel);
        }
        
        enumerateAuroraEffectView(mPanelView);
        
        setOnClickListeners();
        setAuroraActions();
        //Log.i("SQF_LOG", "adapter size:" + mAdapter.getCount());
        
        return main;
    }
    
    @Override  
    public void onDestroyView() {  
    	
    	if(mPanelView != null) {
    		recycleAuroraEffectViewBitmaps();
    	}
        super.onDestroyView();  
        
    }  
    
    public void enumerateAuroraEffectView(View parentView) {
    	if(!(parentView instanceof ViewGroup)) return;
    	ViewGroup viewGroup = (ViewGroup)parentView;
    	int count = viewGroup.getChildCount();
    	for(int i=0; i<count; i++) {
    		View child = viewGroup.getChildAt(i);
    		if(child instanceof AuroraEffectView) {
    			AuroraEffectView effectView = (AuroraEffectView)child;
    			mAuroraEffectViews.add(effectView);
    		}
    	}
    }
    
    public void recycleAuroraEffectViewBitmaps() {
    	for(AuroraEffectView effectView : mAuroraEffectViews) {
    		effectView.recycleBitmaps();
    	}
    }
    
    public void setOnClickListeners() {
    	for(AuroraEffectView effectView : mAuroraEffectViews) {
    		effectView.setOnClickListener(this);
    	}
    }
    
    public void setAuroraActions() {
    	//wenyongzhe 2015.10.26 BUG 16860 start
    	if(null==mAdapter){
    		 loadAdapter(mCurrentAdapter);
    		 if(mAdapter == null){
    			 return;
    		 }
    	}
    	//wenyongzhe 2015.10.26 BUG 16860 end
    	int i = 0;
    	for(AuroraEffectView effectView : mAuroraEffectViews) {
    		AuroraAction action = mAdapter.getItem(i);
    		action.setPosition(i);
			effectView.setAuroraAction(action);
			action.setAuroraEffectView(effectView);
			++ i;
    	}
    }
    
    public void setSelected(AuroraEffectView selected) {    	
    	for(AuroraEffectView effectView : mAuroraEffectViews) {
    		if(selected == effectView) {
				effectView.setSelected(true);
			} else {
				effectView.setSelected(false);
			}
    	}
    }
    
    @Override
    public void onClick(View v) {    	
    	if(v instanceof AuroraEffectView) {
    		FilterShowActivity activity = (FilterShowActivity) getActivity();
    		AuroraEffectView effectView = (AuroraEffectView)v;
    		activity.showRepresentation(effectView.getAuroraAction().getFilterRepresentation());
    		setSelected(effectView);
    	}
    }
}
