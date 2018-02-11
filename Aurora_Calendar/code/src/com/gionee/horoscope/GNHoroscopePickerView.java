// Gionee <jiangxiao> <2013-07-01> add for CR00832082  begin

package com.gionee.horoscope;

import static com.gionee.astro.GNAstroUtils.COUNT_ASTRO;
import static com.gionee.astro.GNAstroUtils.ICON_TYPE_BIG;
import static com.gionee.astro.GNAstroUtils.INDEX_AQUARIUS;
import static com.gionee.astro.GNAstroUtils.INDEX_ARIES;
import static com.gionee.astro.GNAstroUtils.INDEX_CANCER;
import static com.gionee.astro.GNAstroUtils.INDEX_CAPRICORN;
import static com.gionee.astro.GNAstroUtils.INDEX_GEMINI;
import static com.gionee.astro.GNAstroUtils.INDEX_LEO;
import static com.gionee.astro.GNAstroUtils.INDEX_LIBRA;
import static com.gionee.astro.GNAstroUtils.INDEX_PISCES;
import static com.gionee.astro.GNAstroUtils.INDEX_SAGITTARIUS;
import static com.gionee.astro.GNAstroUtils.INDEX_SCORPIO;
import static com.gionee.astro.GNAstroUtils.INDEX_TAURUS;
import static com.gionee.astro.GNAstroUtils.INDEX_VIRGO;
import static com.gionee.astro.GNAstroUtils.KEY_ASTRO_INDEX;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.calendar.R;
import com.gionee.astro.GNAstroPickerActivity;
import com.gionee.astro.GNAstroUtils;

public class GNHoroscopePickerView extends ViewGroup {
	
	public GNHoroscopePickerView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public GNHoroscopePickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public GNHoroscopePickerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(context);
	}
	

	private Context mContext = null;
	private GNHoroscopeActivity mAttachedActivity = null;

	private ViewGroup mPickerView = null;
	private ScrollView mAstroItemList = null;
	private View mLeftMargin = null;
	
	private ImageView mCurrentAstroIcon = null;
	private ImageView mSelectedAstroIcon = null;
	
	private ViewGroup[] mAstroItems = null;
	private Drawable[] mAstroImages = null;
	private String[] mAstroNames = null;
	private String[] mAstroRanges = null;
	
	private Drawable mPrevPickedDrawable = null;
	private Drawable mPickedDrawable = null;
	private int mPrevPickedIndex = -1;
	private int mPickedIndex = -1;
	
	// members for animation
	
	private static final long DURATION_ROTATE = 400;
	private static final long BASE_DURATION_FOLD = 400;
	private static final long BASE_DURATION_UNFOLD = 400;
	
	private static final int TEXT_HEIGHT_ASTRO_NAME = 12;
	private static final int TEXT_HEIGHT_ASTRO_RANGE = 8;
	private static final int ASTRO_TEXT_GAPPING = 4;
	private static final int ASTRO_ITEM_GAPPING = 18;
	
	private int mSmallIconHeight;
	private int mNameTextHeight;
	private int mTextGapping;
	private int mRangeTextHeight;
	private int mAstroItemGapping;
	private int mUnfoldDistance;
	
	private ArrayList<Animation> mUnfoldAnimations = null;
	private ArrayList<Animation> mFoldAnimations = null;
	
	private boolean isUnfoldAnimRunning = false;
	private boolean isFoldAnimRunning = false;
	
	private boolean mFlipIcon = true;
	
	private class AstroViewHolder {
		public ImageView mIconView = null;
		public TextView mNameView = null;
		public TextView mRangeView = null;
	}
	
	private float mDensity = 1.0f;
	private float mScaledDensity = 1.0f;
	
	private int dp2px(int dp) {
		return (int) (dp * mDensity + 0.5f);
	}
	
	private int sp2px(int sp) {
		return (int) (sp * mScaledDensity + 0.5f);
	}
	
	private AstroViewHolder[] mAstroViewHolders = null;
	
	private void initView(Context context) {
		mContext = context;
		mAttachedActivity = (GNHoroscopeActivity) context;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPickerView = (ViewGroup) inflater.inflate(R.layout.gn_astro_picker_menu, null);
		
		initRes(context);
		initAstroItems();
		initCurrentAstroIcon();
		initLeftMargin();
		
		initUnfoldAnimation();
		initFoldAnimation();
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(mPickerView, params);
		// this.bringChildToFront(mCurrentAstroIcon);
	}
	
	private void initRes(Context context) {
		Resources res = context.getResources();
		
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
		
		mDensity = res.getDisplayMetrics().density;
		mScaledDensity = res.getDisplayMetrics().scaledDensity;
	}
	
	private void initAstroItems() {
		if(mPickerView == null) return;
		
		mAstroItemList = (ScrollView) mPickerView.findViewById(R.id.astro_item_list);
		
		mAstroItems = new ViewGroup[COUNT_ASTRO];
		
		mAstroItems[0] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_1);
		mAstroItems[1] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_2);
		mAstroItems[2] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_3);
		mAstroItems[3] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_4);
		mAstroItems[4] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_5);
		mAstroItems[5] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_6);
		mAstroItems[6] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_7);
		mAstroItems[7] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_8);
		mAstroItems[8] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_9);
		mAstroItems[9] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_10);
		mAstroItems[10] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_11);
		mAstroItems[11] = (ViewGroup) mPickerView.findViewById(R.id.astro_list_item_12);
		
		mAstroViewHolders = new AstroViewHolder[COUNT_ASTRO];
		for(int i = 0; i < mAstroItems.length; ++i) {
			initAstroItemByIndex(i);
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
	
	private int getAstroItemIndex(View view) {
		for(int i = 0; i < mAstroItems.length; ++i) {
			if(view == mAstroItems[i].getChildAt(0)) {
				return i;
			}
		}
		
		return -1;
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
	
	private View.OnClickListener mAstroItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if(isUnfoldAnimRunning || isFoldAnimRunning) {
				return;
			}
			
			mPickedIndex = getAstroItemIndex(view);
			if(mPickedIndex != -1) {
				GNAstroUtils.saveAstroIndexToPref(mContext, mPickedIndex);
			}
			
			// foldAstroItems(mPickedIndex, true);
			startFoldAnimation();
		}
	};
	
	private void initCurrentAstroIcon() {
		mPrevPickedIndex = GNAstroUtils.getAstroIndexFromPref(mContext);
		
		int iconId = GNAstroUtils.findItemIconResByIndex(mPrevPickedIndex, ICON_TYPE_BIG);
		this.mPrevPickedDrawable = mContext.getResources().getDrawable(iconId);
		
		mCurrentAstroIcon = (ImageView) mPickerView.findViewById(R.id.current_astro);
		mCurrentAstroIcon.setImageDrawable(mPrevPickedDrawable);
		mSelectedAstroIcon = (ImageView) mPickerView.findViewById(R.id.selected_astro);
		
		mPickerView.bringChildToFront(mCurrentAstroIcon);
	}

	private void initLeftMargin() {
		mLeftMargin = (View) mPickerView.findViewById(R.id.gn_astro_list_left_margin);
		mLeftMargin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// fold astro items
				Log.d("picker_view", "0 - click left margin");
				if(isUnfoldAnimRunning || isFoldAnimRunning) {
					return;
				}
				Log.d("picker_view", "1 - click left margin");
				mFlipIcon = false;
				Log.d("picker_view", "0 - mFlipIcon = " + mFlipIcon);
				startFoldAnimation();
			}
		});
	}
	
	private void initUnfoldAnimation() {
		mSmallIconHeight = mPrevPickedDrawable.getIntrinsicHeight();
		mNameTextHeight = this.sp2px(TEXT_HEIGHT_ASTRO_NAME);
		mTextGapping = this.dp2px(ASTRO_TEXT_GAPPING);
		mRangeTextHeight = this.sp2px(TEXT_HEIGHT_ASTRO_RANGE);
		mUnfoldDistance = (mSmallIconHeight + mNameTextHeight + mTextGapping + mRangeTextHeight);
		
		mUnfoldAnimations = new ArrayList<Animation>(COUNT_ASTRO);
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
			anim.setAnimationListener(new UnfoldAnimationListener(i));
			
			mUnfoldAnimations.add(anim);
			// TODO: remove // will trigger the unfolding animation
			// mAstroItems[i].setAnimation(anim);
		} // end for...
	}
	
	private void initFoldAnimation() {
		mSmallIconHeight = mPrevPickedDrawable.getIntrinsicHeight();
		mNameTextHeight = this.sp2px(TEXT_HEIGHT_ASTRO_NAME);
		mTextGapping = this.dp2px(ASTRO_TEXT_GAPPING);
		mRangeTextHeight = this.sp2px(TEXT_HEIGHT_ASTRO_RANGE);
		mAstroItemGapping = this.dp2px(ASTRO_ITEM_GAPPING);
		mUnfoldDistance = (mSmallIconHeight + mNameTextHeight + mTextGapping
				+ mRangeTextHeight + mAstroItemGapping);
		
		mFoldAnimations = new ArrayList<Animation>(COUNT_ASTRO);
		Interpolator interpolator = new AccelerateDecelerateInterpolator();
		for(int i = 0; i < COUNT_ASTRO; ++i) {
			float startX = 0;
			float endX = 0;
			float startY = 0;
			float endY = startY - mUnfoldDistance * (i + 1);
			
			TranslateAnimation anim = new TranslateAnimation(
					Animation.ABSOLUTE, startX, 
					Animation.ABSOLUTE, endX, 
					Animation.ABSOLUTE, startY, 
					Animation.ABSOLUTE, endY);
			anim.setInterpolator(interpolator);
			long duration = (long) (BASE_DURATION_FOLD * Math.sqrt(Math.sqrt(i + 1)));
			anim.setDuration(duration);
			anim.setAnimationListener(new FoldAnimationListener(i));
			
			mFoldAnimations.add(anim);
			// mAstroItems[i].setAnimation(anim);
		} // end for...
	}
	
	private class UnfoldAnimationListener implements AnimationListener {
		private int mAstroItemIndex;
		
		UnfoldAnimationListener(int itemId) {
			mAstroItemIndex = itemId;
		}
		
		@Override
		public void onAnimationEnd(Animation arg0) {
			showAstroTextByIndex(mAstroItemIndex);
			
			if(mAstroItemIndex == COUNT_ASTRO - 1) {
				isUnfoldAnimRunning = false;
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			hideAstroTextByIndex(mAstroItemIndex);
			mAstroViewHolders[mAstroItemIndex].mIconView.setVisibility(View.VISIBLE);
		}
	}
	
	private class FoldAnimationListener implements AnimationListener {
		private int mAstroItemIndex;
		
		FoldAnimationListener(int itemId) {
			mAstroItemIndex = itemId;
		}
		
		@Override
		public void onAnimationEnd(Animation arg0) {
			mAstroViewHolders[mAstroItemIndex].mIconView.setVisibility(View.INVISIBLE);
			Log.d("picker_view", "1 - mFlipIcon = " + mFlipIcon);
			if(mFlipIcon && mAstroItemIndex == COUNT_ASTRO - 10) {
				Log.d("picker_view", "2 - mFlipIcon = " + mFlipIcon);
				flipBigIcon();
			} else {
				mFlipIcon = true;
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			hideAstroTextByIndex(mAstroItemIndex);
			if(mAstroItemIndex == 0) {
				
			}
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
	
	private void hideAstroItemsText() {
		for(int i = 0; i < this.mAstroItems.length; ++i) {
			mAstroViewHolders[i].mNameView.setVisibility(View.INVISIBLE);
			mAstroViewHolders[i].mRangeView.setVisibility(View.INVISIBLE);
		}
	}
	
	private boolean isUnfoldAnimationRunning() {
		Animation lastOne = mUnfoldAnimations.get(mUnfoldAnimations.size() - 1);
		
		return !lastOne.hasEnded();
	}
	
	public void startUnfoldAnimation() {
		if(mUnfoldAnimations == null || mUnfoldAnimations.size() == 0) {
			return;
		}
		
		isUnfoldAnimRunning = true;
		
		initCurrentAstroIcon();
		
		mCurrentAstroIcon.setRotationY(0);
		mCurrentAstroIcon.setVisibility(View.VISIBLE);
		
		for(int i = 0, sz = mUnfoldAnimations.size(); i < sz; ++i) {
			Animation anim = mUnfoldAnimations.get(i);
			mAstroItems[i].startAnimation(anim);
		}
	}
	
	public void startFoldAnimation() {
		if(mFoldAnimations == null || mFoldAnimations.size() == 0) {
			return;
		}
		
		isFoldAnimRunning = true;
		
		for(int i = 0, sz = mFoldAnimations.size(); i < sz; ++i) {
			mAstroItems[i].startAnimation(mFoldAnimations.get(i));
		}
	}
	
	private void flipBigIcon() {
		ObjectAnimator outAnim = ObjectAnimator.ofFloat(mCurrentAstroIcon, "RotationY", 0, 90.0f);
		outAnim.setInterpolator(new AccelerateInterpolator());
		outAnim.setDuration(DURATION_ROTATE);
		outAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
            	mCurrentAstroIcon.setVisibility(View.INVISIBLE);
            	GNHoroscopePickerView.this.setVisibility(View.GONE);
            	
            	isFoldAnimRunning = false;
            	
            	// flip out the selected icon in content view
            	mAttachedActivity.updateHoroscopeIcon();
            }
		});
		outAnim.start();
	}
	
	@Override
	protected void onMeasure(int measuredWidth, int measuredHeight) {
		mPickerView.measure(measuredWidth, measuredHeight);
		
		this.setMeasuredDimension(measuredWidth, measuredHeight);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// impl
		mPickerView.layout(l, t, r, b);
		
		Log.d("picker_view", "mPickerView dimension: " + mPickerView.getWidth() + ", " + mPickerView.getHeight());
	}
	
	private static class SimpleAnimationListener implements AnimationListener {
		@Override
		public void onAnimationEnd(Animation arg0) {
			// do noting
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// do noting
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// do noting
		}
	}
}

//Gionee <jiangxiao> <2013-07-01> add for CR00832082  end
