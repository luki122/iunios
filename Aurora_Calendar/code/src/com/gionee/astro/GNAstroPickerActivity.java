// gionee <jiangxiao> <2013-05-31> add for CR000000 begin
package com.gionee.astro;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.android.calendar.R;

import static com.gionee.astro.GNAstroUtils.KEY_ASTRO_INDEX;
import static com.gionee.astro.GNAstroUtils.INDEX_ARIES;
import static com.gionee.astro.GNAstroUtils.INDEX_TAURUS;
import static com.gionee.astro.GNAstroUtils.INDEX_GEMINI;
import static com.gionee.astro.GNAstroUtils.INDEX_CANCER;
import static com.gionee.astro.GNAstroUtils.INDEX_LEO;
import static com.gionee.astro.GNAstroUtils.INDEX_VIRGO;
import static com.gionee.astro.GNAstroUtils.INDEX_LIBRA;
import static com.gionee.astro.GNAstroUtils.INDEX_SCORPIO;
import static com.gionee.astro.GNAstroUtils.INDEX_SAGITTARIUS;
import static com.gionee.astro.GNAstroUtils.INDEX_CAPRICORN;
import static com.gionee.astro.GNAstroUtils.INDEX_AQUARIUS;
import static com.gionee.astro.GNAstroUtils.INDEX_PISCES;
import static com.gionee.astro.GNAstroUtils.COUNT_ASTRO;
import static com.gionee.astro.GNAstroUtils.ICON_TYPE_BIG;
import static com.gionee.astro.GNAstroUtils.ICON_TYPE_SMALL;

public class GNAstroPickerActivity extends AuroraActivity {
	private Drawable[] mAstroImages = null;
	private String[] mAstroNames = null;
	private String[] mAstroRanges = null;
	
	private Drawable mPrevPickedDrawable = null;
	private Drawable mPickedDrawable = null;
	private int mPrevPickedIndex = -1;
	private int mPickedIndex = -1;
	
	private ImageView mCurrentAstroIcon = null;
	private ImageView mSelectedAstroIcon = null;
	
	private static final long DURATION_ROTATE = 300;
	private static final long BASE_DURATION_FOLD = 800;
	private static final long BASE_DURATION_UNFOLD = 800;
	
	// public static final String KEY_ASTRO_INDEX = "astro_index";
	
	private int getAstroIndexFromPref() {
		SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);
		int index = pref.getInt(KEY_ASTRO_INDEX, INDEX_ARIES);
		
		return index;
	}
	
	private boolean setAstroIndexToPref(int astroIndex) {
		if(astroIndex >= 0 && astroIndex < COUNT_ASTRO) {
			SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt(KEY_ASTRO_INDEX, astroIndex);
			
			return editor.commit();
		}
		
		return false;
	}
	
	private int getAstroItemIndex(View view) {
		for(int i = 0; i < mAstroItems.length; ++i) {
			if(view == mAstroItems[i].getChildAt(0)) {
				return i;
			}
		}
		
		return -1;
	}
	
	private ScrollView mAstroItemList = null;

	@Override
	public void onCreate(Bundle args) {
		super.onCreate(args);
		this.setContentView(R.layout.gn_astro_picker_menu);
		
		mDensity = this.getResources().getDisplayMetrics().density;
		mScaledDensity = this.getResources().getDisplayMetrics().scaledDensity;
		
		initRes();
		initAstroItems();
		
		initCurrentAstroIcon();
		
		initLeftMargin();
		
		initUnfold();
		

		ViewGroup root = (ViewGroup) this.findViewById(R.id.astro_picker_menu);
		root.bringChildToFront(mCurrentAstroIcon);
	}
	
	private void initCurrentAstroIcon() {
		// mPrevPickedIndex = getAstroIndexFromPref();
		mPrevPickedIndex = GNAstroUtils.getAstroIndexFromPref(this);
		
		int iconId = GNAstroUtils.findItemIconResByIndex(mPrevPickedIndex, ICON_TYPE_BIG);
		this.mPrevPickedDrawable = this.getResources().getDrawable(iconId);
		
		mCurrentAstroIcon = (ImageView) this.findViewById(R.id.current_astro);
		mCurrentAstroIcon.setImageDrawable(mPrevPickedDrawable);
		mSelectedAstroIcon = (ImageView) this.findViewById(R.id.selected_astro);
	}
	
	private ViewGroup[] mAstroItems = null;
	
	private class AstroViewHolder {
		public ImageView mIconView = null;
		public TextView mNameView = null;
		public TextView mRangeView = null;
	}
	
	private AstroViewHolder[] mAstroViewHolders = null;
	
	private void initAstroItems() {
		mAstroItemList = (ScrollView) this.findViewById(R.id.astro_item_list);
//		for(int i = COUNT_ASTRO - 1; i >= 0; --i) {
//			mAstroItemList.bringChildToFront(mAstroItemList.getChildAt(i));
//			mAstroItemList.invalidate();
//		}
		
		mAstroItems = new ViewGroup[COUNT_ASTRO];
		
		mAstroItems[0] = (ViewGroup) this.findViewById(R.id.astro_list_item_1);
		mAstroItems[1] = (ViewGroup) this.findViewById(R.id.astro_list_item_2);
		mAstroItems[2] = (ViewGroup) this.findViewById(R.id.astro_list_item_3);
		mAstroItems[3] = (ViewGroup) this.findViewById(R.id.astro_list_item_4);
		mAstroItems[4] = (ViewGroup) this.findViewById(R.id.astro_list_item_5);
		mAstroItems[5] = (ViewGroup) this.findViewById(R.id.astro_list_item_6);
		mAstroItems[6] = (ViewGroup) this.findViewById(R.id.astro_list_item_7);
		mAstroItems[7] = (ViewGroup) this.findViewById(R.id.astro_list_item_8);
		mAstroItems[8] = (ViewGroup) this.findViewById(R.id.astro_list_item_9);
		mAstroItems[9] = (ViewGroup) this.findViewById(R.id.astro_list_item_10);
		mAstroItems[10] = (ViewGroup) this.findViewById(R.id.astro_list_item_11);
		mAstroItems[11] = (ViewGroup) this.findViewById(R.id.astro_list_item_12);
		
		mAstroViewHolders = new AstroViewHolder[COUNT_ASTRO];
		for(int i = 0; i < mAstroItems.length; ++i) {
			initAstroItemByIndex(i);
			// mAstroItems[i].setVisibility(View.INVISIBLE);
		}
	}
	
	private int getSelectorIdByIndex(int index) {
		switch(index) {
			case INDEX_ARIES:
				return R.drawable.gn_astro_picker_selector_aries;
			case INDEX_TAURUS:
				return R.drawable.gn_astro_picker_selector_taurus;
			case INDEX_GEMINI:
				return R.drawable.gn_astro_picker_selector_gemini;
			case INDEX_CANCER:
				return R.drawable.gn_astro_picker_selector_cancer;
			case INDEX_LEO:
				return R.drawable.gn_astro_picker_selector_leo;
			case INDEX_VIRGO:
				return R.drawable.gn_astro_picker_selector_virgo;
			case INDEX_LIBRA:
				return R.drawable.gn_astro_picker_selector_libra;
			case INDEX_SCORPIO:
				return R.drawable.gn_astro_picker_selector_scorpio;
			case INDEX_SAGITTARIUS:
				return R.drawable.gn_astro_picker_selector_sagittarius;
			case INDEX_CAPRICORN:
				return R.drawable.gn_astro_picker_selector_capricorn;
			case INDEX_AQUARIUS:
				return R.drawable.gn_astro_picker_selector_aquarius;
			case INDEX_PISCES:
				return R.drawable.gn_astro_picker_selector_pisces;
			default:
				return R.drawable.gn_astro_picker_selector_aries;
		}
	}
	
	private void initAstroItemByIndex(int index) {
		if(index < 0 || index >= COUNT_ASTRO) {
			return;
		}

		ImageView iconView = (ImageView) mAstroItems[index].findViewById(R.id.astro_list_item_icon);
		// iconView.setImageDrawable(mAstroImages[index]);
		int selectorId = getSelectorIdByIndex(index);
		iconView.setBackgroundResource(selectorId);
		iconView.setOnClickListener(mAstroItemOnClickListener);
		
		TextView nameView = (TextView) mAstroItems[index].findViewById(R.id.astro_list_item_name);
		nameView.setText(mAstroNames[index]);
		
		TextView rangeView = (TextView) mAstroItems[index].findViewById(R.id.astro_list_item_range);
		rangeView.setText(mAstroRanges[index]);
		
		mAstroViewHolders[index] = new AstroViewHolder();
		mAstroViewHolders[index].mIconView = iconView;
		mAstroViewHolders[index].mNameView = nameView;
		mAstroViewHolders[index].mRangeView = rangeView;
	}
	
	private void initRes() {
		Resources res = this.getResources();
		
		mAstroImages = new Drawable[COUNT_ASTRO];
		mAstroImages[0] = res.getDrawable(R.drawable.icon_small_aries);
		mAstroImages[1] = res.getDrawable(R.drawable.icon_small_taurus);
		mAstroImages[2] = res.getDrawable(R.drawable.icon_small_gemini);
		mAstroImages[3] = res.getDrawable(R.drawable.icon_small_cancer);
		mAstroImages[4] = res.getDrawable(R.drawable.icon_small_leo);
		mAstroImages[5] = res.getDrawable(R.drawable.icon_small_virgo);
		mAstroImages[6] = res.getDrawable(R.drawable.icon_small_libra);
		mAstroImages[7] = res.getDrawable(R.drawable.icon_small_scorpio);
		mAstroImages[8] = res.getDrawable(R.drawable.icon_small_sagittarius);
		mAstroImages[9] = res.getDrawable(R.drawable.icon_small_capricorn);
		mAstroImages[10] = res.getDrawable(R.drawable.icon_small_aquarius);
		mAstroImages[11] = res.getDrawable(R.drawable.icon_small_pisces);
		
		mAstroNames = new String[COUNT_ASTRO];
		mAstroNames[0] = res.getString(R.string.gn_astro_name_aries);
		mAstroNames[1] = res.getString(R.string.gn_astro_name_taurus);
		mAstroNames[2] = res.getString(R.string.gn_astro_name_gemini);
		mAstroNames[3] = res.getString(R.string.gn_astro_name_cancer);
		mAstroNames[4] = res.getString(R.string.gn_astro_name_leo);
		mAstroNames[5] = res.getString(R.string.gn_astro_name_virgo);
		mAstroNames[6] = res.getString(R.string.gn_astro_name_libra);
		mAstroNames[7] = res.getString(R.string.gn_astro_name_scorpio);
		mAstroNames[8] = res.getString(R.string.gn_astro_name_sagittarius);
		mAstroNames[9] = res.getString(R.string.gn_astro_name_capricorn);
		mAstroNames[10] = res.getString(R.string.gn_astro_name_aquarius);
		mAstroNames[11] = res.getString(R.string.gn_astro_name_pisces);
		
		mAstroRanges = new String[COUNT_ASTRO];
		mAstroRanges[0] = res.getString(R.string.gn_astro_range_aries);
		mAstroRanges[1] = res.getString(R.string.gn_astro_range_taurus);
		mAstroRanges[2] = res.getString(R.string.gn_astro_range_gemini);
		mAstroRanges[3] = res.getString(R.string.gn_astro_range_cancer);
		mAstroRanges[4] = res.getString(R.string.gn_astro_range_leo);
		mAstroRanges[5] = res.getString(R.string.gn_astro_range_virgo);
		mAstroRanges[6] = res.getString(R.string.gn_astro_range_libra);
		mAstroRanges[7] = res.getString(R.string.gn_astro_range_scorpio);
		mAstroRanges[8] = res.getString(R.string.gn_astro_range_sagittarius);
		mAstroRanges[9] = res.getString(R.string.gn_astro_range_capricorn);
		mAstroRanges[10] = res.getString(R.string.gn_astro_range_aquarius);
		mAstroRanges[11] = res.getString(R.string.gn_astro_range_pisces);
	}
	
	private View.OnClickListener mAstroItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			// this statement will cause list can not be folded after we press power key
			// if(isUnfoldAnimationRunning()) return;
			
			if(mFoldingAnimSet != null && !mFoldingAnimSet.isRunning() || mFoldingAnimSet == null) {
				// magnifyAstroView(view);
				mPickedIndex = getAstroItemIndex(view);
				if(mPickedIndex != -1) {
					Log.d("debug", "picked view " + mAstroNames[mPickedIndex]);
					// setAstroIndexToPref(mPickedIndex);
					GNAstroUtils.saveAstroIndexToPref(GNAstroPickerActivity.this, mPickedIndex);
					
					// mAstroItems[mPickedIndex].bringToFront();
					// mAstroItemList.postInvalidate();
					
					int iconId = GNAstroUtils.findItemIconResByIndex(mPickedIndex, ICON_TYPE_BIG);
					mPickedDrawable = getResources().getDrawable(iconId);
					mSelectedAstroIcon.setImageDrawable(mPickedDrawable);
				}
				
				// sendResultToCaller();
				// foldAstroItems();
				foldAstroItemsWithResult(mPickedIndex, true);
			}
		}
	};
	
	private void sendResultToCaller() {
		if(mPickedIndex != -1) {
			Intent result = new Intent();
			result.putExtra(KEY_ASTRO_INDEX, mPickedIndex);
			
			Log.d("debug2", "send result " + mPickedIndex);
			this.setResult(AuroraActivity.RESULT_OK, result);
		} else {
			this.setResult(AuroraActivity.RESULT_CANCELED);
		}
		
		this.finish();
	}
	
	private void sendResultToCaller(int value) {
		if(value >= 0 && value < COUNT_ASTRO) {
			Intent result = new Intent();
			result.putExtra(KEY_ASTRO_INDEX, value);
			
			this.setResult(AuroraActivity.RESULT_OK, result);
		} else {
			this.setResult(AuroraActivity.RESULT_CANCELED);
		}
		
		this.finish();
	}
	
	AnimatorSet mFoldingAnimSet = new AnimatorSet();
	
	private void magnifyAstroView(final View view) {
		final float savedScaleX = view.getScaleX();
		final float savedScaleY = view.getScaleY();
		Log.d("debug", "savedScaleX = " + savedScaleX + ", savedScaleY = " + savedScaleY);
		ObjectAnimator animScaleX = ObjectAnimator.ofFloat(view, "ScaleX", savedScaleX, savedScaleX * 2);
		ObjectAnimator animScaleY = ObjectAnimator.ofFloat(view, "ScaleY", savedScaleY, savedScaleY * 2);
		
		final float savedAlpha = view.getAlpha();
		ObjectAnimator animAlpha = ObjectAnimator.ofFloat(view, "Alpha", savedAlpha, 0);
		
		if(mFoldingAnimSet == null) {
			mFoldingAnimSet = new AnimatorSet();
		}
		mFoldingAnimSet.playTogether(animScaleX, animScaleY, animAlpha);
		mFoldingAnimSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				mFoldingAnimSet = null;
				
				view.setScaleX(savedScaleX);
				view.setScaleY(savedScaleY);
				view.setAlpha(savedAlpha);
            }
		});
		
		mFoldingAnimSet.start();
	}
	
//	private static final int DURATION_UNFOLD_BASE = 1000;
//	
//	private void unfoldAstroItems() {
//		ObjectAnimator animPisces = ObjectAnimator.ofFloat(mAstroItems[INDEX_PISCES], "Y", 0, 100 * INDEX_PISCES);
//		animPisces.setDuration(DURATION_UNFOLD_BASE * INDEX_PISCES);
//		
//		if(mFoldingAnimSet == null) {
//			mFoldingAnimSet = new AnimatorSet();
//		}
//		mFoldingAnimSet.playTogether(animPisces);
//		mFoldingAnimSet.start();
//	}
	
//	private void foldAstroItems() {
//		hideAstroText();
//		
//		ArrayList<Animator> animList = new ArrayList<Animator>(COUNT_ASTRO);
//		for(int i = 1; i < COUNT_ASTRO; ++i) {
//			animList.add(createFoldAnimatorByIndex(i));
//		}
//		
//		if(mAnimSet == null) {
//			mAnimSet = new AnimatorSet();
//		}
//		mAnimSet.playTogether(animList);
//		mAnimSet.addListener(new AnimatorListenerAdapter() {
//			@Override
//			public void onAnimationEnd(Animator anim) {
//				mAnimSet = null;
//				sendResultToCaller();
//			}
//		});
//		
//		mAnimSet.start();
//	}
	
	private void foldAstroItemsWithResult(final int result, final boolean flip) {
		hideAstroText();
//		for(int i = COUNT_ASTRO - 1; i >= 0; --i) {
//			mAstroItemList.bringChildToFront(mAstroItemList.getChildAt(i));
//			mAstroItemList.invalidate();
//		}
		
		ArrayList<Animator> animList = new ArrayList<Animator>(COUNT_ASTRO);
		for(int i = 0; i < COUNT_ASTRO; ++i) {
			animList.add(createFoldAnimatorByIndex(i));
		}
		
		if(mFoldingAnimSet == null) {
			mFoldingAnimSet = new AnimatorSet();
		}
		mFoldingAnimSet.setDuration(BASE_DURATION_FOLD);
		mFoldingAnimSet.playTogether(animList);
		mFoldingAnimSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				mFoldingAnimSet = null;
				
				if(flip) {
					flipBigIcon(result);
				} else {
					sendResultToCaller(result);
				}
			}
			
			@Override
			public void onAnimationStart(Animator anim) {
//				if(flip) {
//					flipBigIcon();
//				}
			}
		});
		
		mFoldingAnimSet.start();
	}
	
	private void flipBigIcon(final int result) {
		ObjectAnimator outAnim = ObjectAnimator.ofFloat(mCurrentAstroIcon, "RotationY", 0, 90.0f);
		outAnim.setInterpolator(new AccelerateInterpolator());
		outAnim.setDuration(DURATION_ROTATE);
		
		final ObjectAnimator inAnim = ObjectAnimator.ofFloat(mSelectedAstroIcon, "RotationY", -90.0f, 0);
		inAnim.setInterpolator(new DecelerateInterpolator());
		inAnim.setDuration(DURATION_ROTATE);
		inAnim.addListener(new AnimatorListenerAdapter() {
			@Override
            public void onAnimationEnd(Animator anim) {
            	sendResultToCaller(result);
			}
		});
		
		outAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
            	mCurrentAstroIcon.setVisibility(View.INVISIBLE);
            	inAnim.start();
            	mSelectedAstroIcon.setVisibility(View.VISIBLE);
            }
		});
		outAnim.start();
	}
	
	private ObjectAnimator createFoldAnimatorByIndex(int index) {
		int startY = this.mAstroItems[index].getTop();
		int endY = -this.mCurrentAstroIcon.getHeight();
		// Log.d("debug", "startY = " + startY + ", endY = " + endY);
		// Log.d("debug", "distance = " + (startY - endY));
		// Log.d("debug", "item height = " + this.mAstroItems[0].getHeight());
		
		ObjectAnimator anim = ObjectAnimator.ofFloat(
				this.mAstroItems[index], "Y", 
				startY, endY);
		anim.setDuration(BASE_DURATION_FOLD);
		
		return anim;
	}
	
	private void hideAstroText() {
		for(int i = 0; i < this.mAstroItems.length; ++i) {
			mAstroViewHolders[i].mNameView.setVisibility(View.INVISIBLE);
			mAstroViewHolders[i].mRangeView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void hideAstroTextByIndex(int index) {
		mAstroViewHolders[index].mNameView.setVisibility(View.INVISIBLE);
		mAstroViewHolders[index].mRangeView.setVisibility(View.INVISIBLE);
	}
	
	private void showAstroTextByIndex(int index) {
		mAstroViewHolders[index].mNameView.setVisibility(View.VISIBLE);
		mAstroViewHolders[index].mRangeView.setVisibility(View.VISIBLE);
	}
	
	private int mAstroItemHeight = -1;
	
	private float mDensity = 0;
	private float mScaledDensity = 0;
	
	private int dp2px(int dp) {
		return (int) (dp * mDensity + 0.5f);
	}
	
	private int sp2px(int sp) {
		return (int) (sp * mScaledDensity + 0.5f);
	}
	
	private int mSmallIconHeight;
	private int mNameTextHeight;
	private int mTextGapping;
	private int mRangeTextHeight;
	private int mUnfoldDistance = 0;
	
	private ArrayList<Animation> mUnfoldAnimations = null;
	// private AnimationSet mUnfoldAnimations = null;
	
	private boolean isUnfoldAnimationRunning() {
		Animation lastOne = mUnfoldAnimations.get(mUnfoldAnimations.size() - 1);
		
		return !lastOne.hasEnded();
	}
	
	private class AstroAnimationListener implements AnimationListener {
		private int mAstroItemIndex;
		
		public AstroAnimationListener(int index) {
			mAstroItemIndex = index;
		}
		
		@Override
		public void onAnimationEnd(Animation arg0) {
			showAstroTextByIndex(mAstroItemIndex);
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			hideAstroTextByIndex(mAstroItemIndex);
		}
	}
	
	private void initUnfold() {
		mSmallIconHeight = mPrevPickedDrawable.getIntrinsicHeight();
		mNameTextHeight = this.sp2px(12);
		mTextGapping = this.dp2px(4);
		mRangeTextHeight = this.sp2px(8);
		
		mUnfoldDistance = (mSmallIconHeight + mNameTextHeight + mTextGapping + mRangeTextHeight);
		Log.d("debug2", "mUnfoldDistance = " + mUnfoldDistance);
		
		mUnfoldAnimations = new ArrayList<Animation>(COUNT_ASTRO - 1);
//		mUnfoldAnimations = new AnimationSet(true);
//		mUnfoldAnimations.setInterpolator(new DecelerateInterpolator());
//		mUnfoldAnimations.setDuration(1000);
		
		// Interpolator interpolator = new DecelerateInterpolator();
		Interpolator interpolator = new AccelerateDecelerateInterpolator();
		for(int i = 0; i < COUNT_ASTRO; ++i) {
			float startX = 0;
			float endX = 0;
			float endY = 0;
			float startY = endY - mUnfoldDistance * (i + 1);
			
			TranslateAnimation anim = new TranslateAnimation(
					Animation.ABSOLUTE, startX, 
					Animation.ABSOLUTE, endX, 
					Animation.ABSOLUTE, startY, 
					Animation.ABSOLUTE, endY);
			anim.setInterpolator(interpolator);
			long duration = (long) (BASE_DURATION_UNFOLD * Math.sqrt(Math.sqrt(i + 1)));
			Log.d("debug2", "duration = " + duration);
			anim.setDuration(duration);
			anim.setAnimationListener(new AstroAnimationListener(i));
			
			mUnfoldAnimations.add(anim);
			// mUnfoldAnimations.addAnimation(anim);
			mAstroItems[i].setAnimation(anim);
		}
	} // end of initUnfold()
	
	@Override
	public void onResume() {
		super.onResume();
		
		for(int i = 0; i < mUnfoldAnimations.size(); ++i) {
			mUnfoldAnimations.get(i).startNow();
		}
//		new java.util.Timer().schedule(new java.util.TimerTask() {
//			@Override
//			public void run() {
//				for(int i = 0; i < mUnfoldAnimations.size(); ++i) {
//					mUnfoldAnimations.get(i).startNow();
//				}
//			}
//		}, 5000);
		
		// mUnfoldAnimations.startNow();
	}
	
	private View mLeftMargin = null;
	
	private void initLeftMargin() {
		mLeftMargin = (View) this.findViewById(R.id.gn_astro_list_left_margin);
		mLeftMargin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isUnfoldAnimationRunning()) return;
				
				if(mFoldingAnimSet != null && !mFoldingAnimSet.isRunning() || mFoldingAnimSet == null) {
					foldAstroItemsWithResult(mPrevPickedIndex, false);
				}
			}
		});
	}
}
//gionee <jiangxiao> <2013-05-31> add for CR000000 end
