package com.android.gallery3d.filtershow.category;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Rect;
import android.net.Uri;
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
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.ttpic.AuroraTTPicUtils;
import aurora.widget.AuroraAnimationImageView;

public class SubCategoryBeautyShotPanel extends Fragment
  implements View.OnClickListener
{
  public static final String FRAGMENT_TAG = "SubCategoryBeautyShotPanel";
  private static final String PARAMETER_TAG = "currentPanel";
  private AuroraCategoryAdapter mAdapter;
  private ArrayList<AuroraAnimationImageView> mAuroraAnimationImageView = new ArrayList();
  private int mCurrentAdapter = 4;
  private View mPanelView;

  public void enumerateAuroraEffectView(View parentView) {
  	if(!(parentView instanceof ViewGroup)) return;
  	ViewGroup viewGroup = (ViewGroup)parentView;
  	int count = viewGroup.getChildCount();
  	for(int i=0; i<count; i++) {
  		View child = viewGroup.getChildAt(i);
  		if(child instanceof AuroraAnimationImageView) {
  			AuroraAnimationImageView effectView = (AuroraAnimationImageView)child;
  			mAuroraAnimationImageView.add(effectView);
  		}
  	}
  }

  public void loadAdapter(int adapter) {
      FilterShowActivity activity = (FilterShowActivity) getActivity();
      mAdapter = activity.getCategoryBeautyShotAdapter();
  }
  
  @Override
  public void onAttach(Activity activity) {
      super.onAttach(activity);
      //Log.i("SQF_LOG", "onAttach SubCategoryFiltersPanel----->");
      loadAdapter(mCurrentAdapter);
  }

  @Override
  public void onClick(View v) {    	
/*  	if(v instanceof AuroraEffectView) {
  		FilterShowActivity activity = (FilterShowActivity) getActivity();
  		AuroraEffectView effectView = (AuroraEffectView)v;
  		activity.showRepresentation(effectView.getAuroraAction().getFilterRepresentation());
  		setSelected(effectView);
  	}*/
	  Log.i("SQF_LOG", "SubCategoryBeautyShotPanel::onCreateView ----> ");
  		if(! MasterImage.getImage().isPreviewUpdateFinish() ) return;
	
  		FilterShowActivity activity = (FilterShowActivity) getActivity();
  		switch (v.getId()) {
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
  		}
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	  
	  Log.i("SQF_LOG", "SubCategoryBeautyShotPanel::onCreateView ----> ");
      LinearLayout main = (LinearLayout) inflater.inflate(R.layout.filtershow_sub_category_panel_beauty_shot, container, false);
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
      
      //enumerateAuroraEffectView(mPanelView);
      
      //setOnClickListeners();
      
      
      	View view = mPanelView.findViewById(R.id.main_menu_ttpic_beautify_photo_btn);
  		view.setOnClickListener(this);
  		view = mPanelView.findViewById(R.id.main_menu_ttpic_beautify_portrait_btn);
  		view.setOnClickListener(this);
  		view = mPanelView.findViewById(R.id.main_menu_ttpic_natural_makeup_btn);
  		view.setOnClickListener(this);
  		view = mPanelView.findViewById(R.id.main_menu_ttpic_jigsaw_btn);
  		view.setOnClickListener(this);
      
      
      //setAuroraActions();
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

  public void onSaveInstanceState(Bundle paramBundle)
  {
    super.onSaveInstanceState(paramBundle);
    paramBundle.putInt("currentPanel", this.mCurrentAdapter);
  }

  public void recycleAuroraEffectViewBitmaps() {
/*  	for(AuroraEffectView effectView : mAuroraEffectViews) {
  		effectView.recycleBitmaps();
  	}*/
  }

  public void setAdapter(int paramInt)
  {
    this.mCurrentAdapter = paramInt;
  }

  public void setAuroraActions() {
  	int i = 0;
/*  	for(AuroraEffectView effectView : mAuroraEffectViews) {
  		AuroraAction action = mAdapter.getItem(i);
  		action.setPosition(i);
			effectView.setAuroraAction(action);
			action.setAuroraEffectView(effectView);
			++ i;
  	}*/
  }

  public void setOnClickListeners() {
/*  	for(AuroraEffectView effectView : mAuroraEffectViews) {
  		effectView.setOnClickListener(this);
  	}*/

  }

  public void setSelected(AuroraEffectView selected) {    	
/*  	for(AuroraEffectView effectView : mAuroraEffectViews) {
  		if(selected == effectView) {
				effectView.setSelected(true);
			} else {
				effectView.setSelected(false);
			}
  	}*/
  }
}
