package com.aurora.iunivoice.widget;

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

import com.aurora.iunivoice.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
//import android.text.method.AllCapsTransformationMethod;
//import android.text.method.TransformationMethod2;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;

//flash add for click sound
import android.media.AudioManager;
import android.media.SoundPool;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

//end

public class AuroraSwitch extends CompoundButton {

	private final boolean DEBUG = false;
	private final String TAG = "AuroraSwitch";

	private static final int TOUCH_MODE_IDLE = 0;
	private static final int TOUCH_MODE_DOWN = 1;
	private static final int TOUCH_MODE_DRAGGING = 2;

	// Enum for the "typeface" XML parameter.
	private static final int SANS = 1;
	private static final int SERIF = 2;
	private static final int MONOSPACE = 3;

	private Drawable mTrackDrawable;
	private int mSwitchMinWidth;
	private int mSwitchPadding;

	private int mTouchMode = TOUCH_MODE_IDLE;
	private int mTouchSlop;
	private float mTouchX;
	private float mTouchY;
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	private int mMinFlingVelocity;

	private float mThumbPosition;
	private int mSwitchWidth;
	private int mSwitchHeight;
	private int mThumbWidth; // Does not include padding

	private int mSwitchLeft;
	private int mSwitchTop;
	private int mSwitchRight;
	private int mSwitchBottom;

	private TextPaint mTextPaint;
	private ColorStateList mTextColors;

	@SuppressWarnings("hiding")
	private final Rect mTempRect = new Rect();

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	// chengrongqiang
	private float mVelocity;
	private final float VELOCITY = 350;
	private boolean mAnimating;
	private float mAnimatedVelocity;
	private float mAnimationPosition;

	private ValueAnimator vaon;
	private ValueAnimator vaoff;
	private ValueAnimator auroraDragAnim;
	private final int animation_time = 400;
	private int aurora_anim_curFrame = 0;

	private int[] auroraDrawableIds = { R.drawable.switch0001,
			R.drawable.switch0002, R.drawable.switch0003,
			R.drawable.switch0004, R.drawable.switch0005,
			R.drawable.switch0006, R.drawable.switch0007,
			R.drawable.switch0008, R.drawable.switch0009,
			R.drawable.switch0010, R.drawable.switch0011,
			R.drawable.switch0012, R.drawable.switch0013,
			R.drawable.switch0014, R.drawable.switch0015,
			R.drawable.switch0016, R.drawable.switch0017,
			R.drawable.switch0018, R.drawable.switch0019,
			R.drawable.switch0020, R.drawable.switch0021,
			R.drawable.switch0022, R.drawable.switch0023,
			R.drawable.switch0024, R.drawable.switch0025,
			R.drawable.switch0026, R.drawable.switch0027,
			R.drawable.switch0028, R.drawable.switch0029, R.drawable.switch0030 };

	private int[] auroraDrawableOffIds = { R.drawable.switch0070,
			R.drawable.switch0071, R.drawable.switch0072,
			R.drawable.switch0073, R.drawable.switch0074,
			R.drawable.switch0075, R.drawable.switch0076,
			R.drawable.switch0077, R.drawable.switch0078,
			R.drawable.switch0079, R.drawable.switch0080,
			R.drawable.switch0081, R.drawable.switch0082,
			R.drawable.switch0083, R.drawable.switch0084,
			R.drawable.switch0085, R.drawable.switch0086,
			R.drawable.switch0087, R.drawable.switch0088,
			R.drawable.switch0089, R.drawable.switch0090,
			R.drawable.switch0091, R.drawable.switch0092,
			R.drawable.switch0093, R.drawable.switch0094,
			R.drawable.switch0095, R.drawable.switch0096,
			R.drawable.switch0097, R.drawable.switch0098,
			R.drawable.switch0099, R.drawable.switch0100 };

	private int auroraPerDistance;

	/**
	 * Construct a new Switch with default styling.
	 * 
	 * @param context
	 *            The Context that will determine this widget's theming.
	 */
	public AuroraSwitch(Context context) {
		this(context, null);
	}

	/**
	 * Construct a new Switch with default styling, overriding specific style
	 * attributes as requested.
	 * 
	 * @param context
	 *            The Context that will determine this widget's theming.
	 * @param attrs
	 *            Specification of attributes that should deviate from default
	 *            styling.
	 */
	public AuroraSwitch(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.auroraswitchStyle);
	}

	/**
	 * Construct a new Switch with a default style determined by the given theme
	 * attribute, overriding specific style attributes as requested.
	 * 
	 * @param context
	 *            The Context that will determine this widget's theming.
	 * @param attrs
	 *            Specification of attributes that should deviate from the
	 *            default styling.
	 * @param defStyle
	 *            An attribute ID within the active theme containing a reference
	 *            to the default style for this widget. e.g.
	 *            android.R.attr.switchStyle.
	 */
	public AuroraSwitch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		Resources res = getResources();
		mTextPaint.density = res.getDisplayMetrics().density;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AuroraSwitch, defStyle, 0);

		mTrackDrawable = a.getDrawable(R.styleable.AuroraSwitch_track);
		mSwitchMinWidth = a.getDimensionPixelSize(
				R.styleable.AuroraSwitch_switchMinWidth, 0);
		mSwitchPadding = a.getDimensionPixelSize(
				R.styleable.AuroraSwitch_switchPadding, 0);

		int appearance = a.getResourceId(
				R.styleable.AuroraSwitch_switchTextAppearance, 0);
		if (appearance != 0) {
			setSwitchTextAppearance(context, appearance);
		}
		a.recycle();

		ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
		mMinFlingVelocity = config.getScaledMinimumFlingVelocity();

		// Refresh display with current params
		refreshDrawableState();
		log("liuwei", "init setChecked() !!!");
		setChecked(isChecked());

		// auroraSetTrackDrawable();
		// chengrongqiang
		final float density = getResources().getDisplayMetrics().density;

		mVelocity = (int) (VELOCITY * density + 0.5f);

		vaoff = ValueAnimator.ofInt(0, auroraDrawableOffIds.length - 1);

		vaoff.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				log("vaoff onAnimationUpdate  value = "
						+ (Integer) animation.getAnimatedValue());
				setTrackResource(auroraDrawableOffIds[(Integer) animation
						.getAnimatedValue()]);
				invalidate();
			}

		});

		vaoff.setDuration(animation_time);

		vaoff.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				vaoff.cancel();
				setChecked(false);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub

			}

		});

		/*
		 * vaon =
		 * ValueAnimator.ofInt(R.drawable.switch0001,R.drawable.switch0002
		 * ,R.drawable.switch0003
		 * ,R.drawable.switch0004,R.drawable.switch0005,R.drawable.switch0006
		 * ,R.drawable.switch0007,R.drawable.switch0008,R.drawable.switch0009
		 * ,R.drawable.switch0010,R.drawable.switch0011,R.drawable.switch0012
		 * ,R.drawable.switch0013,R.drawable.switch0014,R.drawable.switch0015
		 * ,R.drawable.switch0016,R.drawable.switch0017,R.drawable.switch0018
		 * ,R.drawable.switch0019,R.drawable.switch0020,R.drawable.switch0021
		 * ,R.drawable.switch0022,R.drawable.switch0023,R.drawable.switch0024
		 * ,R.drawable.switch0025,R.drawable.switch0026,R.drawable.switch0027
		 * ,R.drawable.switch0028,R.drawable.switch0029,R.drawable.switch0030);
		 */
		vaon = ValueAnimator.ofInt(0, auroraDrawableIds.length - 1);
		vaon.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				log("vaon onAnimationUpdate  value = "
						+ (Integer) animation.getAnimatedValue());

				setTrackResource(auroraDrawableIds[(Integer) animation
						.getAnimatedValue()]);

				invalidate();
			}

		});
		vaon.setDuration(animation_time);

		vaon.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				vaon.cancel();
				setChecked(true);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub

			}

		});

		// flash add init Click Sound
		// auroraInitSoundPool();

	}

	/**
	 * Sets the switch text color, size, style, hint color, and highlight color
	 * from the specified TextAppearance resource.
	 * 
	 * @attr ref android.R.styleable#Switch_switchTextAppearance
	 */
	public void setSwitchTextAppearance(Context context, int resid) {
		// TypedArray appearance =
		// context.obtainStyledAttributes(resid,
		// android.R.styleable.TextAppearance);
		//
		// ColorStateList colors;
		// int ts;
		//
		// colors =
		// appearance.getColorStateList(android.R.styleable.TextAppearance_textColor);
		// if (colors != null) {
		// mTextColors = colors;
		// } else {
		// // If no color set in TextAppearance, default to the view's textColor
		// mTextColors = getTextColors();
		// }
		//
		// ts =
		// appearance.getDimensionPixelSize(android.R.styleable.TextAppearance_textSize,
		// 0);
		// if (ts != 0) {
		// if (ts != mTextPaint.getTextSize()) {
		// mTextPaint.setTextSize(ts);
		// requestLayout();
		// }
		// }
		//
		// int typefaceIndex, styleIndex;
		//
		// typefaceIndex = appearance.getInt(android.R.styleable.
		// TextAppearance_typeface, -1);
		// styleIndex = appearance.getInt(android.R.styleable.
		// TextAppearance_textStyle, -1);
		//
		// setSwitchTypefaceByIndex(typefaceIndex, styleIndex);
		//
		// boolean allCaps = appearance.getBoolean(android.R.styleable.
		// TextAppearance_textAllCaps, false);
		// if (allCaps) {
		// // mSwitchTransformationMethod = new
		// AllCapsTransformationMethod(getContext());
		// //mSwitchTransformationMethod.setLengthChangesAllowed(true);
		// } else {
		// //mSwitchTransformationMethod = null;
		// }
		//
		// appearance.recycle();
	}

	private void setSwitchTypefaceByIndex(int typefaceIndex, int styleIndex) {
		Typeface tf = null;
		switch (typefaceIndex) {
		case SANS:
			tf = Typeface.SANS_SERIF;
			break;

		case SERIF:
			tf = Typeface.SERIF;
			break;

		case MONOSPACE:
			tf = Typeface.MONOSPACE;
			break;
		}

		setSwitchTypeface(tf, styleIndex);
	}

	/**
	 * Sets the typeface and style in which the text should be displayed on the
	 * switch, and turns on the fake bold and italic bits in the Paint if the
	 * Typeface that you provided does not have all the bits in the style that
	 * you specified.
	 */
	public void setSwitchTypeface(Typeface tf, int style) {
		if (style > 0) {
			if (tf == null) {
				tf = Typeface.defaultFromStyle(style);
			} else {
				tf = Typeface.create(tf, style);
			}

			setSwitchTypeface(tf);
			// now compute what (if any) algorithmic styling is needed
			int typefaceStyle = tf != null ? tf.getStyle() : 0;
			int need = style & ~typefaceStyle;
			mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
			mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
		} else {
			mTextPaint.setFakeBoldText(false);
			mTextPaint.setTextSkewX(0);
			setSwitchTypeface(tf);
		}
	}

	/**
	 * Sets the typeface in which the text should be displayed on the switch.
	 * Note that not all Typeface families actually have bold and italic
	 * variants, so you may need to use
	 * {@link #setSwitchTypeface(Typeface, int)} to get the appearance that you
	 * actually want.
	 * 
	 * @attr ref android.R.styleable#TextView_typeface
	 * @attr ref android.R.styleable#TextView_textStyle
	 */
	public void setSwitchTypeface(Typeface tf) {
		if (mTextPaint.getTypeface() != tf) {
			mTextPaint.setTypeface(tf);

			requestLayout();
			invalidate();
		}
	}

	/**
	 * Set the amount of horizontal padding between the switch and the
	 * associated text.
	 * 
	 * @param pixels
	 *            Amount of padding in pixels
	 * 
	 * @attr ref android.R.styleable#Switch_switchPadding
	 */
	public void setSwitchPadding(int pixels) {
		mSwitchPadding = pixels;
		requestLayout();
	}

	/**
	 * Get the amount of horizontal padding between the switch and the
	 * associated text.
	 * 
	 * @return Amount of padding in pixels
	 * 
	 * @attr ref android.R.styleable#Switch_switchPadding
	 */
	public int getSwitchPadding() {
		return mSwitchPadding;
	}

	/**
	 * Set the minimum width of the switch in pixels. The switch's width will be
	 * the maximum of this value and its measured width as determined by the
	 * switch drawables and text used.
	 * 
	 * @param pixels
	 *            Minimum width of the switch in pixels
	 * 
	 * @attr ref android.R.styleable#Switch_switchMinWidth
	 */
	public void setSwitchMinWidth(int pixels) {
		mSwitchMinWidth = pixels;
		requestLayout();
	}

	/**
	 * Get the minimum width of the switch in pixels. The switch's width will be
	 * the maximum of this value and its measured width as determined by the
	 * switch drawables and text used.
	 * 
	 * @return Minimum width of the switch in pixels
	 * 
	 * @attr ref android.R.styleable#Switch_switchMinWidth
	 */
	public int getSwitchMinWidth() {
		return mSwitchMinWidth;
	}

	/**
	 * Set the horizontal padding around the text drawn on the switch itself.
	 * 
	 * @param pixels
	 *            Horizontal padding for switch thumb text in pixels
	 * 
	 * @attr ref android.R.styleable#Switch_thumbTextPadding
	 */
	// public void setThumbTextPadding(int pixels) {
	// mThumbTextPadding = pixels;
	// requestLayout();
	// }

	/**
	 * Get the horizontal padding around the text drawn on the switch itself.
	 * 
	 * @return Horizontal padding for switch thumb text in pixels
	 * 
	 * @attr ref android.R.styleable#Switch_thumbTextPadding
	 */
	// public int getThumbTextPadding() {
	// return mThumbTextPadding;
	// }

	/**
	 * Set the drawable used for the track that the switch slides within.
	 * 
	 * @param track
	 *            Track drawable
	 * 
	 * @attr ref android.R.styleable#Switch_track
	 */
	public void setTrackDrawable(Drawable track) {
		mTrackDrawable = track;
		requestLayout();
	}

	/**
	 * Set the drawable used for the track that the switch slides within.
	 * 
	 * @param resId
	 *            Resource ID of a track drawable
	 * 
	 * @attr ref android.R.styleable#Switch_track
	 */
	public void setTrackResource(int resId) {
		setTrackDrawable(getContext().getResources().getDrawable(resId));
	}

	/**
	 * Get the drawable used for the track that the switch slides within.
	 * 
	 * @return Track drawable
	 * 
	 * @attr ref android.R.styleable#Switch_track
	 */
	public Drawable getTrackDrawable() {
		return mTrackDrawable;
	}

	/**
	 * Set the drawable used for the switch "thumb" - the piece that the user
	 * can physically touch and drag along the track.
	 * 
	 * @param thumb
	 *            Thumb drawable
	 * 
	 * @attr ref android.R.styleable#Switch_thumb
	 */
	// public void setThumbDrawable(Drawable thumb) {
	// mThumbDrawable = thumb;
	// requestLayout();
	// }

	/**
	 * Set the drawable used for the switch "thumb" - the piece that the user
	 * can physically touch and drag along the track.
	 * 
	 * @param resId
	 *            Resource ID of a thumb drawable
	 * 
	 * @attr ref android.R.styleable#Switch_thumb
	 */
	// public void setThumbResource(int resId) {
	// setThumbDrawable(getContext().getResources().getDrawable(resId));
	// }

	/**
	 * Get the drawable used for the switch "thumb" - the piece that the user
	 * can physically touch and drag along the track.
	 * 
	 * @return Thumb drawable
	 * 
	 * @attr ref android.R.styleable#Switch_thumb
	 */
	// public Drawable getThumbDrawable() {
	// return mThumbDrawable;
	// }

	/**
	 * Returns the text displayed when the button is in the checked state.
	 * 
	 * @attr ref android.R.styleable#Switch_textOn
	 */
	public CharSequence getTextOn() {
		return "";
	}

	/**
	 * Sets the text displayed when the button is in the checked state.
	 * 
	 * @attr ref android.R.styleable#Switch_textOn
	 */
	public void setTextOn(CharSequence textOn) {

	}

	/**
	 * Returns the text displayed when the button is not in the checked state.
	 * 
	 * @attr ref android.R.styleable#Switch_textOff
	 */
	public CharSequence getTextOff() {
		return "";
	}

	/**
	 * Sets the text displayed when the button is not in the checked state.
	 * 
	 * @attr ref android.R.styleable#Switch_textOff
	 */
	public void setTextOff(CharSequence textOff) {

	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		log("liuwei", "widthMeasureSpec = " + widthMeasureSpec);
		log("liuwei", "heightMeasureSpec = " + heightMeasureSpec);

		// if (mOnLayout == null) {
		// mOnLayout = makeLayout(mTextOn);
		// }
		// if (mOffLayout == null) {
		// mOffLayout = makeLayout(mTextOff);
		// }

		mTrackDrawable.getPadding(mTempRect);
		// final int maxTextWidth = Math.max(mOnLayout.getWidth(),
		// mOffLayout.getWidth());

		final int switchWidth = Math.max(mSwitchMinWidth,
				mTrackDrawable.getIntrinsicWidth());
		final int switchHeight = mTrackDrawable.getIntrinsicHeight();

		log("liuwei", "switchWidth = " + switchWidth);
		log("liuwei", "switchHeight = " + switchHeight);

		mThumbWidth = 2 + 12 * 2;

		switch (widthMode) {
		case MeasureSpec.AT_MOST:
			widthSize = Math.min(widthSize, switchWidth);
			break;

		case MeasureSpec.UNSPECIFIED:
			widthSize = switchWidth;
			break;

		case MeasureSpec.EXACTLY:
			// Just use what we were given
			break;
		}

		switch (heightMode) {
		case MeasureSpec.AT_MOST:
			heightSize = Math.min(heightSize, switchHeight);
			break;

		case MeasureSpec.UNSPECIFIED:
			heightSize = switchHeight;
			break;

		case MeasureSpec.EXACTLY:
			// Just use what we were given
			break;
		}

		mSwitchWidth = switchWidth;
		mSwitchHeight = switchHeight;

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int measuredHeight = getMeasuredHeight();

		log("liuwei", "measuredHeight = " + measuredHeight);
		log("liuwei", "switchHeight = " + switchHeight);
		if (measuredHeight < switchHeight) {
			setMeasuredDimension(getMeasuredWidthAndState(), switchHeight);
		}
	}

	@Override
	public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
		super.onPopulateAccessibilityEvent(event);
		// CharSequence text = isChecked() ? mOnLayout.getText() :
		// mOffLayout.getText();
		// if (!TextUtils.isEmpty(text)) {
		// event.getText().add(text);
		// }
	}

	private Layout makeLayout(CharSequence text) {
		// final CharSequence transformed = (mSwitchTransformationMethod !=
		// null)
		// ? mSwitchTransformationMethod.getTransformation(text, this)
		// : text;

		return new StaticLayout(text, mTextPaint, (int) Math.ceil(Layout
				.getDesiredWidth(text, mTextPaint)),
				Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
	}

	/**
	 * @return true if (x, y) is within the target area of the switch thumb
	 */
	private boolean hitThumb(float x, float y) {
		// mThumbDrawable.getPadding(mTempRect);
		final int thumbTop = mSwitchTop - mTouchSlop;
		// final int thumbLeft = mSwitchLeft + (int) (mThumbPosition + 0.5f) -
		// mTouchSlop;
		final int thumbLeft = mSwitchLeft - mTouchSlop;
		// final int thumbRight = thumbLeft + mThumbWidth +
		// mTempRect.left + mTempRect.right + mTouchSlop;
		final int thumbRight = mSwitchRight + mTouchSlop;
		final int thumbBottom = mSwitchBottom + mTouchSlop;
		return x > thumbLeft && x < thumbRight && y > thumbTop
				&& y < thumbBottom;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// mVelocityTracker.addMovement(ev);
		if (!isEnabled())
			return false;

		final int action = ev.getActionMasked();
		log("chengrq", "action:" + action);
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			// setThumbResource(R.drawable.aurora_seekbar_press);
			// setTrackResource(R.drawable.frame);
			final float x = ev.getX();
			final float y = ev.getY();
			if (isEnabled() && hitThumb(x, y)) {
				mTouchMode = TOUCH_MODE_DOWN;
				mTouchX = x;
				mTouchY = y;
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {

			switch (mTouchMode) {
			case TOUCH_MODE_IDLE:
				// Didn't target the thumb, treat normally.
				break;

			case TOUCH_MODE_DOWN: {
				final float x = ev.getX();
				final float y = ev.getY();
				if (Math.abs(x - mTouchX) > mTouchSlop) {
					mTouchMode = TOUCH_MODE_DRAGGING;
					getParent().requestDisallowInterceptTouchEvent(true);
					mTouchX = x;
					mTouchY = y;
					return true;
				}
				break;
			}

			case TOUCH_MODE_DRAGGING: {
				final float x = ev.getX();
				float dx = x - mTouchX;
				int[] ids = isChecked() ? auroraDrawableOffIds
						: auroraDrawableIds;

				setTrackResource(ids[getCurShowDrawableIndexWhenDragging((int) dx)]);
				// float newPos = Math.max(0,
				// Math.min(mThumbPosition + dx, getThumbScrollRange()));

				return true;
			}
			}
			break;
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			if (mTouchMode == TOUCH_MODE_DRAGGING) {
				// stopDrag(ev);
				final float x = ev.getX();
				float dx = x - mTouchX;
				auroraStopDragging((int) dx);
				return true;
			}
			mTouchMode = TOUCH_MODE_IDLE;
			// mVelocityTracker.clear();
			break;
		}
		}
		return super.onTouchEvent(ev);
	}

	private int getCurShowDrawableIndexWhenDragging(int distance) {
		int index;
		int[] ids = isChecked() ? auroraDrawableOffIds : auroraDrawableIds;
		if (!isChecked()) {
			index = distance < 0 ? 0 : distance / auroraPerDistance;
		} else {
			index = distance < 0 ? (-distance / auroraPerDistance) : 0;
		}

		if (index < 0)
			index = 0;
		if (index > ids.length - 1)
			index = ids.length - 1;

		log("index = " + index);
		log("distance = " + distance);
		log("mSwitchRight = " + mSwitchRight);
		log("isChecked() = " + isChecked());
		log("auroraDrawableIds.length = " + ids.length);
		return index;
	}

	private void auroraStopDragging(int distance) {
		int absDistance = Math.abs(distance);

		int index = getCurShowDrawableIndexWhenDragging(distance);

		int[] ids = isChecked() ? auroraDrawableOffIds : auroraDrawableIds;

		// the seventh drawable has in the middle!!! 30/5 = 6
		boolean valueChanged = index > ids.length / 5;

		log("valueChanged = " + valueChanged);
		log("index = " + index);
		log("auroraDrawableIds.length = " + auroraDrawableIds.length);

		int startValue = index;

		int endValue = valueChanged ? (ids.length - 1) : 0;

		log("startValue = " + startValue);
		log("endValue = " + endValue);
		auroraStartDraggingRemainderAnim(startValue, endValue, valueChanged);

	}

	private void auroraStartDraggingRemainderAnim(int start, int end,
			boolean changeValue) {
		final int[] ids = isChecked() ? auroraDrawableOffIds
				: auroraDrawableIds;

		int duration = Math.abs(start - end) * animation_time / ids.length;
		log("duration = " + duration);
		final boolean changedVaule = changeValue;

		auroraDragAnim = ValueAnimator.ofInt(start, end);

		auroraDragAnim.setDuration(duration);

		auroraDragAnim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				log("auroraDragAnim onAnimationUpdate  value = "
						+ (Integer) animation.getAnimatedValue());

				setTrackResource(ids[(Integer) animation.getAnimatedValue()]);

				invalidate();
			}

		});

		auroraDragAnim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				auroraDragAnim.cancel();
				if (changedVaule) {
					log("auroraDragAnim onAnimationEnd  changedVaule = "
							+ changedVaule);
					setCheckedDelayed(!isChecked());
				}
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub

			}

		});

		auroraDragAnim.start();
	}

	private void cancelSuperTouch(MotionEvent ev) {
		MotionEvent cancel = MotionEvent.obtain(ev);
		cancel.setAction(MotionEvent.ACTION_CANCEL);
		super.onTouchEvent(cancel);
		cancel.recycle();
	}

	/**
	 * Called from onTouchEvent to end a drag operation.
	 * 
	 * @param ev
	 *            Event that triggered the end of drag mode - ACTION_UP or
	 *            ACTION_CANCEL
	 */
	private void stopDrag(MotionEvent ev) {
		mTouchMode = TOUCH_MODE_IDLE;
		// Up and not canceled, also checks the switch has not been disabled
		// during the drag
		boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP
				&& isEnabled();
		cancelSuperTouch(ev);

		if (commitChange) {
			boolean newState;
			mVelocityTracker.computeCurrentVelocity(1000);
			float xvel = mVelocityTracker.getXVelocity();
			if (Math.abs(xvel) > mMinFlingVelocity) {
				newState = xvel > 0;
			} else {
				newState = getTargetCheckedState();
			}
			animateThumbToCheckedState(newState);
		} else {
			animateThumbToCheckedState(isChecked());
		}
	}

	// added by flash 2013.11.19
	private boolean auroraAnimIsPlaying() {
		return (auroraAnimIsPlayingOpenAnim() || auroraAnimIsPlayingCloseAnim());
	}

	private boolean auroraAnimIsPlayingOpenAnim() {
		return (vaon != null && vaon.isRunning());
	}

	private boolean auroraAnimIsPlayingCloseAnim() {
		return (vaoff != null && vaoff.isRunning());
	}

	@Override
	public boolean performClick() {
		// not clickable when anim is playing !!!
		if (auroraAnimIsPlaying())
			return true;

		// auroraPlayClickSound();

		startAnimation(!isChecked());
		return false;

	}

	private void startAnimation(boolean turnOn) {
		mAnimating = true;
		log("liuwei", "startAnimation() mAnimating = " + mAnimating);
		mAnimatedVelocity = turnOn ? mVelocity : -mVelocity;
		mAnimationPosition = mThumbPosition;
		// new SwitchAnimation().run();

		log("error", "turnOn:" + turnOn);
		if (turnOn) {
			vaon.start();
		} else {
			vaoff.start();
		}
	}

	private void stopAnimation() {
		mAnimating = false;
		if (vaoff.isRunning())
			vaoff.cancel();
		if (vaon.isRunning())
			vaon.cancel();
		log("liuwei", "stopAnimation() mAnimating = " + mAnimating);
	}

//	private final class SwitchAnimation implements Runnable {
//
//		@Override
//		public void run() {
//			if (!mAnimating) {
//				return;
//			}
//			doAnimation();
//			FrameAnimationControllerNew.requestAnimationFrame(this);
//		}
//	}

//	private void doAnimation() {
//
//		// Log.i("chengrq","mAnimationPosition:"+mAnimationPosition);
//		mAnimationPosition += mAnimatedVelocity
//				* FrameAnimationControllerNew.ANIMATION_FRAME_DURATION / 1000;
//		mThumbPosition = isChecked() ? 0 : getThumbScrollRange();
//
//		log("liuwei", "mAnimationPosition = " + mAnimationPosition
//				+ "mAnimatedVelocity = " + mAnimatedVelocity
//				+ "getThumbScrollRange() = " + getThumbScrollRange());
//		log("liuwei", "doAnimation 0 .....");
//
//		if (mAnimationPosition >= mThumbPosition && !isChecked()) {
//			stopAnimation();
//			mAnimationPosition = mThumbPosition;
//			log("liuwei", "doAnimation 1 .....");
//			setCheckedDelayed(true);
//		}
//
//		if (mAnimationPosition <= mThumbPosition && isChecked()) {
//			stopAnimation();
//			mAnimationPosition = mThumbPosition;
//			log("liuwei", "doAnimation 2 .....");
//			setCheckedDelayed(false);
//		}
//		moveView(mAnimationPosition);
//	}

//	private void moveView(float position) {
//		// mBtnPos = position;
//		// mRealPos = getRealPos(mBtnPos);
//		// float targetPos = isChecked() ? 0 : getThumbScrollRange();
//		mThumbPosition = position;
//		invalidate();
//	}

	private void animateThumbToCheckedState(boolean newCheckedState) {
		// TODO animate!
		// float targetPos = newCheckedState ? 0 : getThumbScrollRange();
		// mThumbPosition = targetPos;
		// Log.i("chengrq","newCheckedState:"+newCheckedState);
		// setChecked(newCheckedState);
		performClick();
	}

	private boolean getTargetCheckedState() {
		return mThumbPosition >= getThumbScrollRange() / 2;
	}

	// int i = 1;
	// private Drawable mCurrentDrawable;

	//
	private void auroraSetTrackDrawable() {
		if (isChecked()) {
			setTrackResource(R.drawable.switch0030);
		} else {
			setTrackResource(R.drawable.switch0100);
		}
		invalidate();
	}

	@Override
	public void setChecked(boolean checked) {

		super.setChecked(checked);

		mThumbPosition = checked ? getThumbScrollRange() : 0;

		if (auroraAnimIsPlayingOpenAnim())
			vaon.cancel();

		if (auroraAnimIsPlayingCloseAnim())
			vaoff.cancel();

		auroraSetTrackDrawable();

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		mThumbPosition = isChecked() ? getThumbScrollRange() : 0;

		int switchRight = getWidth() - getPaddingRight();
		int switchLeft = switchRight - mSwitchWidth;
		int switchTop = 0;
		int switchBottom = 0;
		switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
		default:
		case Gravity.TOP:
			switchTop = getPaddingTop();
			switchBottom = switchTop + mSwitchHeight;
			break;

		case Gravity.CENTER_VERTICAL:
			switchTop = (getPaddingTop() + getHeight() - getPaddingBottom())
					/ 2 - mSwitchHeight / 2;
			switchBottom = switchTop + mSwitchHeight;
			break;

		case Gravity.BOTTOM:
			switchBottom = getHeight() - getPaddingBottom();
			switchTop = switchBottom - mSwitchHeight;
			break;
		}

		mSwitchLeft = switchLeft;
		mSwitchTop = switchTop;
		mSwitchBottom = switchBottom;
		mSwitchRight = switchRight;
		auroraPerDistance = mSwitchRight / auroraDrawableIds.length;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw the switch
		int switchLeft = mSwitchLeft;
		int switchTop = mSwitchTop;
		int switchRight = mSwitchRight;
		int switchBottom = mSwitchBottom;

		// end

		mTrackDrawable.setBounds(switchLeft, switchTop, switchRight,
				switchBottom);
		mTrackDrawable.draw(canvas);

		canvas.restore();
	}

	@Override
	public int getCompoundPaddingRight() {
		int padding = super.getCompoundPaddingRight() + mSwitchWidth;
		if (!TextUtils.isEmpty(getText())) {
			padding += mSwitchPadding;
		}
		return padding;
	}

	private int getThumbScrollRange() {
		if (mTrackDrawable == null) {
			return 0;
		}
		mTrackDrawable.getPadding(mTempRect);
		return mSwitchWidth - mThumbWidth - mTempRect.left - mTempRect.right;
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		int[] myDrawableState = getDrawableState();

		// Set the state of the Drawable
		// Drawable may be null when checked state is set from XML, from super
		// constructor
		// if (mThumbDrawable != null) mThumbDrawable.setState(myDrawableState);
		if (mTrackDrawable != null)
			mTrackDrawable.setState(myDrawableState);

		invalidate();
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) /* || who == mThumbDrawable */
				|| who == mTrackDrawable;
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		// mThumbDrawable.jumpToCurrentState();
		mTrackDrawable.jumpToCurrentState();
	}

	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(AuroraSwitch.class.getName());
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(AuroraSwitch.class.getName());
		// CharSequence switchText = isChecked() ? mTextOn : mTextOff;
		// if (!TextUtils.isEmpty(switchText)) {
		// CharSequence oldText = info.getText();
		// if (TextUtils.isEmpty(oldText)) {
		// info.setText(switchText);
		// } else {
		// StringBuilder newText = new StringBuilder();
		// newText.append(oldText).append(' ').append(switchText);
		// info.setText(newText);
		// }
		// }
	}

//	public static class FrameAnimationControllerNew {
//		private static final int MSG_ANIMATE = 1000;
//
//		public static final int ANIMATION_FRAME_DURATION = 1000 / 120;
//
//		private static final Handler mHandler = new AnimationHandler();
//
//		private FrameAnimationControllerNew() {
//			throw new UnsupportedOperationException();
//		}
//
//		public static void requestAnimationFrame(Runnable runnable) {
//			Message message = new Message();
//			message.what = MSG_ANIMATE;
//			message.obj = runnable;
//			mHandler.sendMessageDelayed(message, ANIMATION_FRAME_DURATION);
//		}
//
//		public static void requestFrameDelay(Runnable runnable, long delay) {
//			Message message = new Message();
//			message.what = MSG_ANIMATE;
//			message.obj = runnable;
//			mHandler.sendMessageDelayed(message, delay);
//		}
//
//		private static class AnimationHandler extends Handler {
//			public void handleMessage(Message m) {
//				switch (m.what) {
//				case MSG_ANIMATE:
//					if (m.obj != null) {
//						((Runnable) m.obj).run();
//					}
//					break;
//				}
//			}
//		}
//	}

	private void setCheckedDelayed(final boolean checked) {

		this.postDelayed(new Runnable() {

			@Override
			public void run() {
				setChecked(checked);
			}
		}, 10);
	}

	private void log(String tag, String string) {
		if (DEBUG)
			Log.e(tag, string);
	}

	private void log(String string) {
		if (DEBUG)
			Log.e(TAG, string);
	}

	/*******************************************************************************
	 * 
	 * add click sound
	 * 
	 * 2014.3.3
	 * 
	 * *****************************************************************************/
	// private SoundPool auroraSoundPool;
	//
	// private int auroraSoundId;
	//
	// private static final String auroraSoundPath =
	// "system/media/audio/ui/Switch.ogg";
	//
	// private void auroraInitSoundPool()
	// {
	// auroraSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
	//
	// auroraSoundId = auroraSoundPool.load(auroraSoundPath, 1);
	// }
	//
	// private void auroraPlayClickSound()
	// {
	// float leftVolume = 1.0f;
	//
	// float rightVolume = 1.0f;
	//
	// int priority = 1;
	//
	// int root = 0;
	//
	// float rate = 1.0f;
	//
	// if(auroraIsSoundEffectEnable())
	// {
	// auroraSoundPool.play(auroraSoundId, leftVolume, rightVolume, priority,
	// root, rate);
	// }
	// }
	//
	// private boolean auroraIsSoundEffectEnable()
	// {
	// int enable = 0;
	//
	// try {
	//
	// enable = Settings.System.getInt(getContext().getContentResolver(),
	// Settings.System.SOUND_EFFECTS_ENABLED);
	//
	// } catch (SettingNotFoundException e) {
	//
	// e.printStackTrace();
	//
	// }
	//
	// return enable == 1;
	// }

}
