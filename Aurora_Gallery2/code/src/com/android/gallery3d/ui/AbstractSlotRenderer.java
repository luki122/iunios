/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.FloatMath;
import android.widget.TextView;
import com.android.gallery3d.R;

import android.graphics.Bitmap;//Aurora <paul> <2014-02-27> for NEW_UI

import com.android.gallery3d.fragmentapp.GridViewUtil;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import com.android.gallery3d.ui.AuroraStringTexture.StringCache;
import com.android.gallery3d.anim.Animation;
import android.view.animation.LinearInterpolator;
import com.android.gallery3d.util.GalleryUtils;


public abstract class AbstractSlotRenderer implements SlotView.SlotRenderer {

    private final ResourceTexture mVideoOverlay;
    private final ResourceTexture mVideoPlayIcon;
    private final ResourceTexture mPanoramaIcon;
    private final NinePatchTexture mFramePressed;
    private final NinePatchTexture mFrameSelected;
    private FadeOutTexture mFramePressedUp;
	//paul add for UI_20 start
	private final ResourceTexture mFavoriteIcon;
	private StringCache mStringCache;
	private static final int EAGE_MARGIN = GalleryUtils.dpToPixel(1);
	private static final int EAGE_MARGIN_CHECK = GalleryUtils.dpToPixel(4);
	private static final int EAGE_MARGIN_VIDEO = GalleryUtils.dpToPixel(2);
	//paul add for UI_20 end
    //Iuni <lory><2014-02-19> add begin
	//paul add start
    private static final int CHECK_ICONS_MAX_INDEX = 14;
	private final ResourceTexture mCheckIcons[];
	//paul add end
    private final ResourceTexture mCheckIcon;
    private final ResourceTexture mUnCheckIcon;
    private static final float MY_DEFAULT_TEXT_SIZE = 40;
    private String mMonth;
    private String mDay;
	private String mYear;
    private TextPaint mPaint;

    private int mWidth = 0;
    private int mHeight = 0;

//    private static final String AURORA_DAY_FONT_PATH = "system/fonts/DroidSansFallback.ttf";//"system/fonts/Roboto-Light.ttf";
//    private static final String AURORA_YEAR_AND_MONTH_FONT_PATH = "system/fonts/Roboto-Light.ttf";//"system/fonts/Roboto-Light.ttf";
    
    
    //private static final String AURORA_DAY_FONT_PATH = "system/fonts/SourceHanSansCN-Normal.ttf";//"system/fonts/Roboto-Light.ttf";
    //private static final String AURORA_DAY_FONT_PATH = "system/fonts/DroidSansFallback.ttf";
    
    //private static final String AURORA_YEAR_AND_MONTH_FONT_PATH = "system/fonts/Roboto-Regular.ttf";//"system/fonts/Roboto-Light.ttf";
    //private static final String AURORA_YEAR_AND_MONTH_FONT_PATH = "system/fonts/DroidSansFallback.ttf";

    /*
    private Typeface m_auroraDayNumberTf;
    private Typeface m_auroraYearAndMonthNumberTf;
    */
    
    private static final float MY_DAY_TEXT_SIZE = 20;
    private static final float MY_DAYWENZI_TEXT_SIZE = 10;
    private static final float MY_YEAR_TEXT_SIZE = 12;
    private static float m_DaySize = 0;
    //private static float m_WenziSize = 0;
    //private static float m_YearSize = 0;
    private TextPaint mDayPaint;
    //private TextPaint mWenziPaint;
    //private TextPaint mYearPaint;
    private Typeface m_auroraFallbackTf;
    
    private static int m_DayXPos = 0;
    private static int m_WenziXPos = 0;
    private static int m_YearXPos = 0;
    //private AuroraStringTexture mDataAnimation;
    //Iuni <lory><2014-02-19> add end
    
    //Aurora <SQF> <2014-04-15>  for NEW_UI begin
    //used to determine screen orientation
    private Context mContext;
    //Aurora <SQF> <2014-04-15>  for NEW_UI end
    private StringTexture mNoPicText;
	private ResourceTexture mNoPicIcon;
	private int mNoPicTextXOffset = 0;
	private int mNoPicIconXOffset = 0;
	private int mNoPicIconPaddingTop;
	private int mNoPicTextPaddingTop;
	
    protected AbstractSlotRenderer(Context context) {
    	//Aurora <SQF> <2014-04-15>  for NEW_UI begin
    	mContext = context;
    	//Aurora <SQF> <2014-04-15>  for NEW_UI end
        mVideoOverlay = new ResourceTexture(context, R.drawable.ic_video_thumb);
        mVideoPlayIcon = new ResourceTexture(context, R.drawable.ic_gallery_play);
		mFavoriteIcon = new ResourceTexture(context, R.drawable.ic_gallery_fav);//paul add for UI_20
        mPanoramaIcon = new ResourceTexture(context, R.drawable.ic_360pano_holo_light);
        mFramePressed = new NinePatchTexture(context, R.drawable.grid_pressed);
        mFrameSelected = new NinePatchTexture(context, R.drawable.grid_selected);
		
		//paul modify start	
		/*
        mCheckIcon = new ResourceTexture(context, R.drawable.check);
    	mUnCheckIcon = new ResourceTexture(context, R.drawable.uncheck);
		*/	
		mStringCache = new StringCache();
        mCheckIcons = new ResourceTexture[CHECK_ICONS_MAX_INDEX + 1];
		mCheckIcons[0] = new ResourceTexture(context, R.drawable.aurora_check00);
		mCheckIcons[1] = new ResourceTexture(context, R.drawable.aurora_check01);
		mCheckIcons[2] = new ResourceTexture(context, R.drawable.aurora_check02);
		mCheckIcons[3] = new ResourceTexture(context, R.drawable.aurora_check03);
		
		mCheckIcons[4] = new ResourceTexture(context, R.drawable.aurora_check04);
		mCheckIcons[5] = new ResourceTexture(context, R.drawable.aurora_check05);
		mCheckIcons[6] = new ResourceTexture(context, R.drawable.aurora_check06);
		mCheckIcons[7] = new ResourceTexture(context, R.drawable.aurora_check07);
		
		mCheckIcons[8] = new ResourceTexture(context, R.drawable.aurora_check08);
		mCheckIcons[9] = new ResourceTexture(context, R.drawable.aurora_check09);
		mCheckIcons[10] = new ResourceTexture(context, R.drawable.aurora_check10);
		mCheckIcons[11] = new ResourceTexture(context, R.drawable.aurora_check11);
		
		mCheckIcons[12] = new ResourceTexture(context, R.drawable.aurora_check12);
		mCheckIcons[13] = new ResourceTexture(context, R.drawable.aurora_check13);
		mCheckIcons[14] = new ResourceTexture(context, R.drawable.aurora_check14);

        mCheckIcon = mCheckIcons[CHECK_ICONS_MAX_INDEX];
    	mUnCheckIcon = mCheckIcons[0];

		TextPaint paint = new TextPaint();
		paint.setTextSize(context.getResources().getDimension(R.dimen.aurora_no_pic_textsize));
		paint.setAntiAlias(true);
		paint.setColor(context.getResources().getColor(R.color.aurora_no_pic_txt_color));
		String str = context.getString(R.string.aurora_empty_text);
        mNoPicText = StringTexture.newInstance(str, paint);
		mNoPicIcon = new ResourceTexture(context, R.drawable.aurora_no_pic_icon);
		mNoPicTextXOffset = - (int)(paint.measureText(str) / 2f + 0.5f);
		mNoPicIconXOffset = -(mNoPicIcon.getWidth()>>1);
		mNoPicIconPaddingTop = context.getResources().getDimensionPixelSize(R.dimen.aurora_no_pic_paddingtop);
		mNoPicTextPaddingTop = mNoPicIcon.getHeight() + mNoPicIconPaddingTop + context.getResources().getDimensionPixelSize(R.dimen.aurora_no_pic_paddingtop_offset);
		
        mMonth = context.getString(R.string.date_month);
        mDay = context.getString(R.string.date_day);
        mYear = context.getString(R.string.date_year);
        m_DaySize = context.getResources().getDimension(R.dimen.date_textsize);
        //m_WenziSize = context.getResources().getDimension(R.dimen.wenzi_textsize);
        //m_YearSize = context.getResources().getDimension(R.dimen.year_textsize);
		int color = context.getResources().getColor(R.color.aurora_header_txt_color);
		setDefaultDayPaint(m_DaySize, color);
        //setDefaultWenziPaint(m_WenziSize, color);
        //setDefaultYearPaint(m_YearSize, color);
		//paul modify end
    }

	//Aurora <paul> <2014-02-27> for NEW_UI begin
	public int getPressedIndex(){
		return -1;
	}
	public Bitmap getBitmapByIndex(int slotIndex){
		return null;
	}
	//Aurora <paul> <2014-02-27> for NEW_UI end
	


	//Iuni <lory><2014-02-22> add begin
    private TextPaint setDefaultDayPaint(float textSize, int color) {
    	mDayPaint = new TextPaint();
    	mDayPaint.setTextSize(textSize);
    	mDayPaint.setAntiAlias(true);
    	mDayPaint.setColor(color);
    	
    	/*
    	try {
//    		mDayPaint.setTypeface(m_auroraFallbackTf);
    		mDayPaint.setTypeface(m_auroraYearAndMonthNumberTf);//mDayPaint.setTypeface(m_auroraDayNumberTf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
    	
        return mDayPaint;
    }
	
    //paul add start
    protected void drawEmptyContent(GLCanvas canvas, int width, int height){
    	mNoPicIcon.draw(canvas, mNoPicIconXOffset + (width>>1), mNoPicIconPaddingTop);
		mNoPicText.draw(canvas, mNoPicTextXOffset + (width>>1), mNoPicTextPaddingTop);
	}
	
    private String mDateStr[] = new String[6];
    protected void drawHeaderContent(GLCanvas canvas, String title, int width, int height, boolean isMonth, boolean needYear, String address) {
		//SimpleDateFormat("yyyyMMdd");
		if (!isMonth) {
			mDateStr[0] = title.substring(6, 8);//dd
		} else {
			mDateStr[0] = null;
		}
		mDateStr[1] = mDay;
		mDateStr[2] = title.substring(4, 6);//MM
		mDateStr[3] = mMonth;
		mDateStr[4] = title.substring(0, 4);//yyyy
		mDateStr[5] = mYear;
		AuroraStringTexture dateTextView =  mStringCache.getStringTexture(mDateStr,
				mDayPaint, null, null, isMonth, needYear, address, width, height);
		dateTextView.draw(canvas, 0, 0);
    }
     //paul add end
    protected void drawContent(GLCanvas canvas,
            Texture content, int width, int height, int rotation) {
        canvas.save(GLCanvas.SAVE_FLAG_MATRIX);

        // The content is always rendered in to the largest square that fits
        // inside the slot, aligned to the top of the slot.
        width = height = Math.min(width, height);
        if (rotation != 0) {
            canvas.translate(width / 2, height / 2);
            canvas.rotate(rotation, 0, 0, 1);
            canvas.translate(-width / 2, -height / 2);
        }

        // Fit the content into the box
        float scale = Math.min(
                (float) width / content.getWidth(),
                (float) height / content.getHeight());
        canvas.scale(scale, scale, 1);
        content.draw(canvas, 0, 0);

        canvas.restore();
    }

	//paul add start
    protected void drawImgCheckIcon(GLCanvas canvas, int width, int height) {
    	
    	int w = mCheckIcon.getWidth();
    	int h = mCheckIcon.getHeight();
		canvas.fillRect(0.0f,0.0f,width,height,0x80000000);
    	mCheckIcon.draw(canvas, width - w - EAGE_MARGIN_CHECK, height - h - EAGE_MARGIN_CHECK, w, h);
		return;
	}
    

    protected void drawImgUnCheckIcon(GLCanvas canvas, int width, int height) {
    	int w = mUnCheckIcon.getWidth();
    	int h = mUnCheckIcon.getHeight();
    	mUnCheckIcon.draw(canvas, width- w - EAGE_MARGIN_CHECK, height - h - EAGE_MARGIN_CHECK, w, h);
		return;
	}
	
	private CheckAnimation mToCheckAnimation = new CheckAnimation(true);
	private CheckAnimation mUnCheckAnimation = new CheckAnimation(false);
	private int mPlayingIndex = -1;
	
	protected boolean playCheckAnimation(GLCanvas canvas, int width, int height, int index){
		if(mPlayingIndex != index || index < 0){
			return false;
		}
		boolean check = mToCheckAnimation.isActive();
		boolean unCheck = mUnCheckAnimation.isActive();
		if(!check && !unCheck) return false;
		if(check){
			mToCheckAnimation.calculate(AnimationTime.get());
			mToCheckAnimation.apply(canvas, width, height);
		}else{
			mUnCheckAnimation.calculate(AnimationTime.get());
			mUnCheckAnimation.apply(canvas, width, height);
		}
		return true;
	}
	
	private void cancelCheckAnimation(){
		mToCheckAnimation.forceStop();
		mUnCheckAnimation.forceStop();
	}	
	
	public void startCheckAnimation(int index){
		if(index < 0) return;
		cancelCheckAnimation();
		mPlayingIndex = index;
		mToCheckAnimation.reStart();
	}
	
	public void startUnCheckAnimation(int index){
		if(index < 0) return;
		cancelCheckAnimation();
		mPlayingIndex = index;
		mUnCheckAnimation.reStart();
	}	

    public class CheckAnimation extends Animation {
        protected ResourceTexture mIcon;
		private boolean mToCheck = false;
		private float mProgress = -1f;
		private LinearInterpolator mInterpolator = new LinearInterpolator();
        public CheckAnimation(boolean toCheck) {
			mToCheck = toCheck;
            setInterpolator(mInterpolator);
            setDuration(250);
        }

        @Override
        protected void onCalculate(float progress) {
        	if(mToCheck){
				mIcon = mCheckIcons[(int)(CHECK_ICONS_MAX_INDEX * progress)];
        	} else {
				mIcon = mCheckIcons[(int)(CHECK_ICONS_MAX_INDEX * (1 - progress))];
			}
			mProgress = progress;
        }

		public void reStart(){
			if(mToCheck){
				mIcon = mCheckIcons[0];
			} else {
				mIcon = mCheckIcons[CHECK_ICONS_MAX_INDEX];
			}
			mProgress = 0f;
			start();
		}
	
        public void apply(GLCanvas canvas, int width, int height){
			int w = mIcon.getWidth();
			int h = mIcon.getHeight();
			mIcon.draw(canvas, width-w, height - h - EAGE_MARGIN_CHECK, w, h);
		}
    }
	

    protected void drawVideoOverlay(GLCanvas canvas, int width, int height) {
        mVideoPlayIcon.draw(canvas, 
			width - mVideoPlayIcon.getWidth() - EAGE_MARGIN_VIDEO,
			0, 
			mVideoPlayIcon.getWidth(), 
			mVideoPlayIcon.getHeight());
    }

    protected void drawFavoriteOverlay(GLCanvas canvas, int width, int height) {
        mFavoriteIcon.draw(canvas, 
			EAGE_MARGIN,
			height - mFavoriteIcon.getHeight() - EAGE_MARGIN, 
			mFavoriteIcon.getWidth(), 
			mFavoriteIcon.getHeight());
    }
	//paul add end

    protected void drawPanoramaIcon(GLCanvas canvas, int width, int height) {
        int iconSize = Math.min(width, height) / 6;
        mPanoramaIcon.draw(canvas, (width - iconSize) / 2, (height - iconSize) / 2,
                iconSize, iconSize);
    }

    protected boolean isPressedUpFrameFinished() {
        if (mFramePressedUp != null) {
            if (mFramePressedUp.isAnimating()) {
                return false;
            } else {
                mFramePressedUp = null;
            }
        }
        return true;
    }

    protected void drawPressedUpFrame(GLCanvas canvas, int width, int height) {
        if (mFramePressedUp == null) {
            mFramePressedUp = new FadeOutTexture(mFramePressed);
        }
        drawFrame(canvas, mFramePressed.getPaddings(), mFramePressedUp, 0, 0, width, height);
    }

    protected void drawPressedFrame(GLCanvas canvas, int width, int height) {
        drawFrame(canvas, mFramePressed.getPaddings(), mFramePressed, 0, 0, width, height);
    }

    protected void drawSelectedFrame(GLCanvas canvas, int width, int height) {
        drawFrame(canvas, mFrameSelected.getPaddings(), mFrameSelected, 0, 0, width, height);
    }

    protected static void drawFrame(GLCanvas canvas, Rect padding, Texture frame,
            int x, int y, int width, int height) {
        frame.draw(canvas, x - padding.left, y - padding.top, width + padding.left + padding.right,
                 height + padding.top + padding.bottom);
    }
}
