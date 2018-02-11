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
import com.android.gallery3d.filtershow.ttpic.AuroraTTPicUtils;
import com.android.gallery3d.util.InstallUtils;
import com.android.gallery3d.util.StatisticsUtils;
import com.tencent.ttpic.sdk.util.Pitu;

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
    public static final String PACKAGE_NAME_TianTianPiTu = Pitu.PACKAGE_NAME;//"com.tencent.ttpic111";

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
    private ViewGroup mPanelView;//ADDED ON 2015.01.06
    private AuroraAlertDialog mInstallingDialog;//ADDED ON 2015.01.06
    
    public void setAdapter(int value) {
        mCurrentAdapter = value;
    }
    
	private PackageInstallObserver mObserver = new PackageInstallObserver();
	private Handler mRefreshButtonsHandler = new Handler();
	
	public class PackageInstallObserver extends IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {
			if(! packageName.equals(PACKAGE_NAME_TianTianPiTu)) return;
			if (returnCode == PackageManager.INSTALL_SUCCEEDED) {
				hidePituInstallingAlertDialog();
				mRefreshButtonsHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//refreshButtons();
					}
				}, 200);
			} else {
				hidePituInstallingAlertDialog();
			}
		}
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
    
/*    private void refreshButtons() {
        if(Pitu.isOpenSdkSupport(getActivity().getBaseContext())) {
        	((FilterShowActivity)getActivity()).getActionBarManager().setType(ActionBarType.ABT_PITU_RETURN);
        	showTTPicButtons();
        } else {
        	((FilterShowActivity)getActivity()).getActionBarManager().setType(ActionBarType.ABT_SAVE);
        	showOriginalButtons(true);
        }
    }*/

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
        mPanelView = (ViewGroup)panelView;
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
	     	view = panelView.findViewById(R.id.main_menu_beauty_shot_btn);
			//modify by JXH for N1 not need ttpitu
	     	if(!Globals.SUPPORT_TTPT){
	     		view.setVisibility(View.GONE);
	     	} else {
				view.setOnClickListener(this);
			}
        } 
        /*else if(mCurrentAdapter == MainPanel.FILTERS) {
        	//TODO: 
        }
        */
        //Aurora <SQF> <2014-09-01>  for NEW_UI end
        
        
        //Aurora <SQF> <2015-12-06>  for NEW_UI begin
        //refreshButtons();
        //Aurora <SQF> <2015-12-06>  for NEW_UI end
        return main;
    }
    
    
    //add by JXH N1 not need ttpitu 
    private boolean is7503Model(){
    	String prop = SystemProperties.get("ro.product.model");
    	if(!TextUtils.isEmpty(prop)&&(prop.equalsIgnoreCase("IUNI N1")||prop.equalsIgnoreCase("IUNI U5"))){
    		return true;
    	}
    	return false;
    }
    
    private boolean is8905ForHer() {
    	FilterShowActivity activity = (FilterShowActivity) getActivity();
    	Context context = ((Activity)activity).getBaseContext();
    	Resources res = context.getResources();
    	int vvv = res.getColor(R.color.custom_action_bar_btn_pressed_color);
    	if(res.getColor(R.color.custom_action_bar_btn_pressed_color) == Color.parseColor("#ff9999")) {
    		return true;
    	}
    	return false;
    }
    
    private int [] originalButtons = {R.id.main_menu_crop_btn, 
    									R.id.main_menu_rotate_btn, 
    									R.id.main_menu_filters_btn, 
    									R.id.main_menu_beauty_shot_btn};
    
    private int [] ttpicButtons = {R.id.main_menu_ttpic_beautify_photo_btn,
    									R.id.main_menu_ttpic_beautify_portrait_btn,
    									R.id.main_menu_ttpic_natural_makeup_btn,
    									R.id.main_menu_ttpic_jigsaw_btn};
    //Aurora <SQF> <2015-12-06>  for NEW_UI begin
    
    /*
    private void showOriginalButtons(boolean showOriginalButtons) {
    	for(int i = 0; i < mPanelView.getChildCount(); i ++) {
    		View view = mPanelView.getChildAt(i);
    		view.setOnClickListener(this);
    		for(int j = 0; j < originalButtons.length; j ++) {
    			if(view.getId() == originalButtons[j]) {
    				view.setVisibility(showOriginalButtons ? View.VISIBLE : View.GONE);
    			}
    			if(! is8905ForHer() && j == 3 && view.getId() == originalButtons[j]) {
    				view.setVisibility(View.GONE);
    			}
    		}
    		
    		for(int j = 0; j < ttpicButtons.length; j ++) {
    			if(view.getId() == ttpicButtons[j]) {
    				view.setVisibility(showOriginalButtons ? View.GONE : View.VISIBLE);
    			}
    		}
    		
    	}

    }
    */
    
/*    private void showTTPicButtons() {
    	showOriginalButtons(false);
    }*/
    //Aurora <SQF> <2015-12-06>  for NEW_UI end
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
        case R.id.main_menu_beauty_shot_btn:
			if(setEditLoading()) return;//paul add <2015-10-27>
        	Context context = activity.getBaseContext();
        	if( Pitu.isOpenSdkSupport(context)) {
        		StatisticsUtils.addEnterTTPicStatistics(context);
        		action = mAdapter.getItem(3);
            	activity.loadAuroraBeautyShotPanel(true);
        	} else {
        		showPituNotInstalledAlertDialog(); 
        	}
        	break;
        //Aurora <SQF> <2014-09-01>  for NEW_UI end
        
        	/*
        //Aurora <SQF> <2014-09-01>  for NEW_UI begin
        case R.id.main_menu_ttpic_beautify_photo_btn:
        	AuroraTTPicUtils.startBeautifyPhoto(activity, Uri.parse(activity.getSelectedFilePath()));
        	break;
        case R.id.main_menu_ttpic_beautify_portrait_btn:
        	AuroraTTPicUtils.startBeautifyPortrait(activity, Uri.parse(activity.getSelectedFilePath()));
        	break;
        case R.id.main_menu_ttpic_natural_makeup_btn:
        	AuroraTTPicUtils.startNaturalMakeUp(activity, Uri.parse(activity.getSelectedFilePath()));
        	break;
        case R.id.main_menu_ttpic_jigsaw_btn:
        	AuroraTTPicUtils.startJigsaw(activity, Uri.parse(activity.getSelectedFilePath()));
        	break;
        //Aurora <SQF> <2014-09-01>  for NEW_UI end
         * */

        } 
    }
    
    
    private void showPituNotInstalledAlertDialog() {
    	AuroraAlertDialog builder = new AuroraAlertDialog.Builder(getActivity())
        .setTitle(R.string.aurora_pitu_not_installed_tip_title)
		.setMessage(R.string.aurora_pitu_not_installed_tip_message).setPositiveButton(R.string.aurora_pitu_install, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				/*
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("http://service.mail.qq.com/cgi-bin/help?subtype=1&&id=28&&no=166"));//设置一个URI地址
				startActivity(intent);
				*/
				dialog.dismiss();
				showPituInstallingAlertDialog();
				((FilterShowActivity)getActivity()).toInstallTTPT(mObserver);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).create();
    	builder.show();
    }
    
    private void showPituInstallingAlertDialog() {
    	if(mInstallingDialog == null) {
	    	mInstallingDialog = new AuroraAlertDialog.Builder(getActivity())
	        .setTitle(R.string.aurora_pitu_installing_tip_title)
			.setMessage(R.string.aurora_pitu_installing_tip_message).setCancelable(false).create();
    	}
    	mInstallingDialog.show();
    }
    
    private void hidePituInstallingAlertDialog() {
    	if(mInstallingDialog != null) {
    		mInstallingDialog.dismiss();
    		mInstallingDialog = null;
    	}
    }
    
	
	
	

    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-30
    /*
    public void updateAddButtonVisibility() {
        if (mAddButton == null) {
            return;
        }
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        if (activity.isShowingImageStatePanel() && mAdapter.showAddButton()) {
            mAddButton.setVisibility(View.VISIBLE);
            if (mAdapter != null) {
                mAddButton.setText(mAdapter.getAddButtonText());
            }
        } else {
            mAddButton.setVisibility(View.GONE);
        }
    }
    */
    //Aurora <SQF> <2014-08-30>  for NEW_UI end
}
