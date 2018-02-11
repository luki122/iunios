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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.state.StatePanel;
import com.android.gallery3d.util.MyLog;

import android.util.Log;

public class MainPanel extends Fragment {

    private static final String LOGTAG = "MainPanel";

    private LinearLayout mMainView;
	
    private ImageButton looksButton;
    private ImageButton bordersButton;
    private ImageButton geometryButton;
    private ImageButton filtersButton;
    private ImageButton beautyShotButton;
	
    public static final String FRAGMENT_TAG = "MainPanel";
    
    //Aurora <SQF> <2014-09-01>  for NEW_UI begin
    public static final int MAIN_MENU = 999;
    private boolean mIsShowingFilterPanel = false;
    //Aurora <SQF> <2014-09-01>  for NEW_UI end
    
    public static final int LOOKS = 0;
    public static final int BORDERS = 1;
    public static final int GEOMETRY = 2;
    public static final int FILTERS = 3;
    public static final int BEAUTY_SHOT = 4;
    public static final int VERSIONS = 5;

    private int mCurrentSelected = FilterShowActivity.DEFAULT_PANEL;
    private int mPreviousToggleVersions = -1;

    private void selection(int position, boolean value) {
        if (value) {
            FilterShowActivity activity = (FilterShowActivity) getActivity();
            activity.setCurrentPanel(position);
        }
        switch (position) {
            case LOOKS: {
                looksButton.setSelected(value);
                break;
            }
            case BORDERS: {
                bordersButton.setSelected(value);
                break;
            }
            case GEOMETRY: {
                geometryButton.setSelected(value);
                break;
            }
            case FILTERS: {
                filtersButton.setSelected(value);
                break;
            }
            case BEAUTY_SHOT: {
            	beautyShotButton.setSelected(value);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMainView != null) {
            if (mMainView.getParent() != null) {
                ViewGroup parent = (ViewGroup) mMainView.getParent();
                parent.removeView(mMainView);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = (LinearLayout) inflater.inflate(
                R.layout.filtershow_main_panel, null, false);
		
        //Aurora <SQF> <2014-08-30>  for NEW_UI begin
        //SQF ANNOTATED ON 2014-08-30

        /*
        looksButton = (ImageButton) mMainView.findViewById(R.id.fxButton);
        bordersButton = (ImageButton) mMainView.findViewById(R.id.borderButton);
        geometryButton = (ImageButton) mMainView.findViewById(R.id.geometryButton);
        filtersButton = (ImageButton) mMainView.findViewById(R.id.colorsButton);
        
        looksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(LOOKS);
            }
        });
        bordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(BORDERS);
            }
        });
        geometryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(GEOMETRY);
            }
        });
        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(FILTERS);
            }
        });
        */
        //Aurora <SQF> <2014-08-30>  for NEW_UI end
        
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        //showImageStatePanel(activity.isShowingImageStatePanel());//SQF ANNOTATED ON 2015.3.18
        showPanel(activity.getCurrentPanel());
        return mMainView;
    }

    private boolean isRightAnimation(int newPos) {
        if (newPos < mCurrentSelected) {
            return false;
        }
        return true;
    }
    
    
    //Aurora <SQF> <2014-05-29>  for NEW_UI begin
    /*
    private void startBottomViewAnimation(boolean isWalkingIn) {
    	if(mMainView == null) return;
        View bottom = mMainView.findViewById(R.id.bottom_panel);
        int animId = isWalkingIn ? R.anim.float_up_in : R.anim.float_down_out;
        bottom.startAnimation(AnimationUtils.loadAnimation(this.getActivity(), animId));
    }
    */
    //Aurora <SQF> <2014-05-29>  for NEW_UI end
    

    private void setCategoryFragment(CategoryPanel category, boolean fromRight) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        //Aurora <SQF> <2014-6-3>  for NEW_UI begin
        //ORIGINALLY:
        /*
        if (fromRight) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        }
        */
        //SQF MODIFIED TO:
        //transaction.setCustomAnimations(R.anim.float_up_in, R.anim.float_down_out);
        //Aurora <SQF> <2014-6-3>  for NEW_UI end
        transaction.replace(R.id.category_panel_container, category, CategoryPanel.FRAGMENT_TAG);
        transaction.commitAllowingStateLoss();
        
    }

    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-30
    /*
    public void loadCategoryLookPanel(boolean force) {
    	//Log.i("SQF_LOG", "MainPanel::loadCategoryLookPanel"); 
        if (!force && mCurrentSelected == LOOKS) {
            return;
        }
        boolean fromRight = isRightAnimation(LOOKS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(LOOKS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = LOOKS;
        selection(mCurrentSelected, true);
    }

    public void loadCategoryBorderPanel() {
    	//Log.i("SQF_LOG", "MainPanel::loadCategoryBorderPanel"); 
        if (mCurrentSelected == BORDERS) {
            return;
        }
        boolean fromRight = isRightAnimation(BORDERS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(BORDERS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = BORDERS;
        selection(mCurrentSelected, true);
    }

    public void loadCategoryGeometryPanel() {
    	//Log.i("SQF_LOG", "MainPanel::loadCategoryGeometryPanel"); 
        if (mCurrentSelected == GEOMETRY) {
            return;
        }

        //if (MasterImage.getImage().hasTinyPlanet()) {
        //    return;
        //}
        
        boolean fromRight = isRightAnimation(GEOMETRY);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(GEOMETRY);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = GEOMETRY;
        selection(mCurrentSelected, true);
    }


    
    public void loadCategoryFiltersPanel() {
    	//Log.i("SQF_LOG", "MainPanel::loadCategoryFiltersPanel"); 
        if (mCurrentSelected == FILTERS) {
            return;
        }
        boolean fromRight = isRightAnimation(FILTERS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(FILTERS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = FILTERS;
        selection(mCurrentSelected, true);
    }
    */
    //Aurora <SQF> <2014-08-30>  for NEW_UI end
    
    
    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    public void loadCategoryPanel() {
    	//Log.i("SQF_LOG", "MainPanel::loadCategoryFiltersPanel"); 
//        if (mCurrentSelected == FILTERS) {
//            return;
//        }
//        boolean fromRight = isRightAnimation(FILTERS);
//        selection(mCurrentSelected, false);
    	//MyLog.i2("SQF_LOG", "MainPanel::loadCategoryPanel-------------------");
        CategoryPanel categoryPanel = new CategoryPanel();
        //categoryPanel.setMainPanel(this);
//        categoryPanel.setAdapter(FILTERS);
        setCategoryFragment(categoryPanel, false);
//        mCurrentSelected = FILTERS;
//        selection(mCurrentSelected, true);
    }
    //Aurora <SQF> <2014-08-30>  for NEW_UI end

    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-30
    /*
    public void loadCategoryVersionsPanel() {
    	
    	//Log.i("SQF_LOG", "MainPanel::loadCategoryVersionsPanel"); 
        if (mCurrentSelected == VERSIONS) {
            return;
        }
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        activity.updateVersions();
        boolean fromRight = isRightAnimation(VERSIONS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(VERSIONS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = VERSIONS;
        selection(mCurrentSelected, true);
    }
    */
    //Aurora <SQF> <2014-08-30>  for NEW_UI end
    
    public void showPanel(int currentPanel) {
        //Aurora <SQF> <2014-08-30>  for NEW_UI begin
        //ORIGINALLY:
    	/*
        switch (currentPanel) {
            case LOOKS: {
                loadCategoryLookPanel(false);
                break;
            }
            case BORDERS: {
                loadCategoryBorderPanel();
                break;
            }
            case GEOMETRY: {
                loadCategoryGeometryPanel();
                break;
            }
            case FILTERS: {
                loadCategoryFiltersPanel();
                break;
            }
            case VERSIONS: {
                loadCategoryVersionsPanel();
                break;
            }
        }
        */
    	//SQF MODIFIED TO:
    	//Log.i("SQF_LOG", "MainPanel::showPanel-------------------");
    	loadCategoryPanel();
    	//Aurora <SQF> <2014-08-30>  for NEW_UI end
    }

    public void setToggleVersionsPanelButton(ImageButton button) {
        if (button == null) {
            return;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentSelected == VERSIONS) {
                    showPanel(mPreviousToggleVersions);
                } else {
                    mPreviousToggleVersions = mCurrentSelected;
                    showPanel(VERSIONS);
                }
            }
        });
    }
    
    //Aurora <SQF> <2014-09-01>  for NEW_UI begin
    /*
     * CategoryPanel's animation
     */
    public void showMainMenuCategoryPanelAnimation(boolean show, AnimationListener listener) {
    	FilterShowActivity activity = (FilterShowActivity) getActivity();
    	final View v = mMainView.findViewById(R.id.category_panel_container);
    	if(v == null) return;
    	if(show) {
    		v.setVisibility(View.VISIBLE);// this line doesn't work...?
    		Animation animation = AnimationUtils.loadAnimation(activity, R.anim.float_up_in);
    		v.startAnimation(animation);
    	} else {
    		Animation animation = AnimationUtils.loadAnimation(activity, R.anim.float_down_out);
    		animation.setAnimationListener(listener);
    		v.startAnimation(animation);
    	}
    }
    //Aurora <SQF> <2014-09-12>  for NEW_UI begin
    
    
    public boolean isShowingFilterPanel() {
    	return mIsShowingFilterPanel;
    }
    
    public void setIsShowingFilterPanel(boolean isShowingFilterPanel) {
    	mIsShowingFilterPanel = isShowingFilterPanel;
    }
    //Aurora <SQF> <2014-09-12>  for NEW_UI end
    

    public void showSubFilterCategoryPanelAnimation(boolean show, AnimationListener listener) {
    	FilterShowActivity activity = (FilterShowActivity) getActivity();
    	final View v = mMainView.findViewById(R.id.sub_category_panel_container);
    	if(v == null) return;
    	if(show) {
    		v.setVisibility(View.VISIBLE);
    		//resetMainPanelBackgroundColor();
    		Animation animation = AnimationUtils.loadAnimation(activity, R.anim.float_up_in);
    		v.startAnimation(animation);
    	} else {
    		Animation animation = AnimationUtils.loadAnimation(activity, R.anim.float_down_out);
    		/*
    		animation.setAnimationListener(new AnimationListener() {
    			public void onAnimationEnd(Animation animation) {
    	            v.setVisibility(View.GONE);
    	            //if(listener != null) listener.hideEnd();
    	        }

    	        public void onAnimationRepeat(Animation animation) {
    	        }

    	        public void onAnimationStart(Animation animation) {
    	        }
    		});
    		*/
    		animation.setAnimationListener(listener);
    		v.startAnimation(animation);
    	}
    }
    //Aurora <SQF> <2014-09-01>  for NEW_UI end
    
    /*
    public void replaceSubCategoryFiltersPanel(final boolean show) {
    	FilterShowActivity activity = (FilterShowActivity) getActivity();
    	View container = mMainView.findViewById(R.id.sub_category_panel_container);
    	FragmentTransaction transaction = null;
        if (container == null) {
            //FilterShowActivity activity = (FilterShowActivity) getActivity();
            container = activity.getMainStatePanelContainer(R.id.sub_category_panel_container);
        } else {
            transaction = getChildFragmentManager().beginTransaction();
        }
        if (container == null) {
            return;
        } else {
            transaction = getFragmentManager().beginTransaction();
        }
        //int currentPanel = mCurrentSelected;
        transaction.setCustomAnimations(R.anim.float_up_in, R.anim.float_down_out);
        if (show) {
            container.setVisibility(View.VISIBLE);
            SubCategoryFiltersPanel filtersCategoryPanel = new SubCategoryFiltersPanel();
            //filtersCategoryPanel.setMainPanel(this);
            //FilterShowActivity activity = (FilterShowActivity) getActivity();
            transaction.replace(R.id.sub_category_panel_container, filtersCategoryPanel, SubCategoryFiltersPanel.FRAGMENT_TAG);
        } else {
        	Log.i("SQF_LOG", "==================================");
            container.setVisibility(View.GONE);
            Fragment filtersCategoryPanel = getChildFragmentManager().findFragmentByTag(SubCategoryFiltersPanel.FRAGMENT_TAG);
            if (filtersCategoryPanel != null) {
                transaction.remove(filtersCategoryPanel);
            }
            //currentPanel = MAIN_MENU;
        }
        mCurrentSelected = -1;
        //showPanel(currentPanel);
        transaction.commit();
    }
    */
    //

    public void showImageStatePanel(boolean show) {
        View container = mMainView.findViewById(R.id.state_panel_container);
        FragmentTransaction transaction = null;
        if (container == null) {
            FilterShowActivity activity = (FilterShowActivity) getActivity();
            container = activity.getMainStatePanelContainer(R.id.state_panel_container);
        } else {
            transaction = getChildFragmentManager().beginTransaction();
        }
        if (container == null) {
            return;
        } else {
            transaction = getFragmentManager().beginTransaction();
        }
        int currentPanel = mCurrentSelected;
        if (show) {
            container.setVisibility(View.VISIBLE);
            StatePanel statePanel = new StatePanel();
            statePanel.setMainPanel(this);
            FilterShowActivity activity = (FilterShowActivity) getActivity();
            //activity.updateVersions();//SQF ANNOTATED ON 2014-08-30
            transaction.replace(R.id.state_panel_container, statePanel, StatePanel.FRAGMENT_TAG);
        } else {
            container.setVisibility(View.GONE);
            Fragment statePanel = getChildFragmentManager().findFragmentByTag(StatePanel.FRAGMENT_TAG);
            if (statePanel != null) {
                transaction.remove(statePanel);
            }
            if (currentPanel == VERSIONS) {
                currentPanel = LOOKS;
            }
        }
        mCurrentSelected = -1;
        showPanel(currentPanel);
        transaction.commit();
    }
}
