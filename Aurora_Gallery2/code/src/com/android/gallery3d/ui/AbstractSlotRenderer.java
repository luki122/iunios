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


public abstract class AbstractSlotRenderer implements SlotView.SlotRenderer {

    private final ResourceTexture mVideoOverlay;
    private final ResourceTexture mVideoPlayIcon;
    private final ResourceTexture mPanoramaIcon;
    private final NinePatchTexture mFramePressed;
    private final NinePatchTexture mFrameSelected;
    private FadeOutTexture mFramePressedUp;
    
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
    private TextPaint mPaint;
    private FontMetricsInt metricsInt;
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
    private static float m_WenziSize = 0;
    private static float m_YearSize = 0;
    private TextPaint mDayPaint;
    //private TextPaint mWenziPaint;
    private TextPaint mYearPaint;
    private Typeface m_auroraFallbackTf;
    
    private static int m_DayXPos = 0;
    private static int m_WenziXPos = 0;
    private static int m_YearXPos = 0;
    private AuroraStringTexture mDataAnimation;
    private AuroraStringTexture mEmptyAnimation;
    //Iuni <lory><2014-02-19> add end
    
    //Aurora <SQF> <2014-04-15>  for NEW_UI begin
    //used to determine screen orientation
    private Context mContext;
    //Aurora <SQF> <2014-04-15>  for NEW_UI end
    

    protected AbstractSlotRenderer(Context context) {
    	//Aurora <SQF> <2014-04-15>  for NEW_UI begin
    	mContext = context;
    	//Aurora <SQF> <2014-04-15>  for NEW_UI end
        mVideoOverlay = new ResourceTexture(context, R.drawable.ic_video_thumb);
        mVideoPlayIcon = new ResourceTexture(context, R.drawable.ic_gallery_play);
        mPanoramaIcon = new ResourceTexture(context, R.drawable.ic_360pano_holo_light);
        mFramePressed = new NinePatchTexture(context, R.drawable.grid_pressed);
        mFrameSelected = new NinePatchTexture(context, R.drawable.grid_selected);
		
		//paul modify start	
		/*
        mCheckIcon = new ResourceTexture(context, R.drawable.check);
    	mUnCheckIcon = new ResourceTexture(context, R.drawable.uncheck);
		*/	
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
		//paul modify end
    	//Iuni <lory><2014-02-22> add begin
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	
        	/*
        	try {
        		m_auroraDayNumberTf = Typeface.createFromFile(AURORA_DAY_FONT_PATH);
        		m_auroraYearAndMonthNumberTf = Typeface.createFromFile(AURORA_YEAR_AND_MONTH_FONT_PATH);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		*/
        	
            mMonth = context.getString(R.string.date_month);//月
            mDay = context.getString(R.string.date_day);//日
            m_DaySize = context.getResources().getDimension(R.dimen.date_textsize);//GridViewUtil.dip2px(context, MY_DAY_TEXT_SIZE);
            m_WenziSize = context.getResources().getDimension(R.dimen.wenzi_textsize);//GridViewUtil.dip2px(context, MY_DAYWENZI_TEXT_SIZE);
            m_YearSize = context.getResources().getDimension(R.dimen.year_textsize);//GridViewUtil.dip2px(context, MY_YEAR_TEXT_SIZE);
            
            float fff = context.getResources().getDimension(R.dimen.date_textsize);
            
            /*m_DayXPos = context.getResources().getDimension(R.dimen.year_textsize);
            m_WenziXPos = context.getResources().getDimension(R.dimen.year_textsize);
            m_YearXPos = context.getResources().getDimension(R.dimen.year_textsize);*/
            //setDefaultPaint(MY_DEFAULT_TEXT_SIZE, Color.BLACK);
            
            //setDefaultDayPaint(m_DaySize, Color.parseColor("#000000"));
            //setDefaultWenziPaint(m_WenziSize, Color.parseColor("#000000"));
            
            setDefaultDayPaint(m_DaySize, Color.parseColor("#525252"));
            setDefaultWenziPaint(m_WenziSize, Color.parseColor("#525252"));
            
            setDefaultYearPaint(m_YearSize, Color.parseColor("#868686"));//5e5e5e
            
            setFontMetricsInt(mDayPaint);

            //Aurora <SQF> <2014-04-15>  for NEW_UI begin
            //ORIGINALLY:
            //mDataAnimation =  new AuroraStringTexture("", "","",mDayPaint, mPaint,mYearPaint);
            //mEmptyAnimation = new AuroraStringTexture("", "","",mDayPaint, mPaint,mYearPaint);
            //SQF MODIFIED TO:
            //paul del
            //mDataAnimation =  StringCache.getStringTexture(mContext, "", "","",mDayPaint, mPaint,mYearPaint, false);
            //mEmptyAnimation = StringCache.getStringTexture(mContext, "", "","",mDayPaint, mPaint,mYearPaint, false);
            //Aurora <SQF> <2014-04-15>  for NEW_UI end
		} 
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
    private TextPaint setDefaultWenziPaint(float textSize, int color) {
    	mPaint = new TextPaint();
    	mPaint.setTextSize(textSize);
    	mPaint.setAntiAlias(true);
    	mPaint.setColor(color);
    	
    	/*
    	try {
//    		mPaint.setTypeface(m_auroraFallbackTf);
    		mPaint.setTypeface(m_auroraDayNumberTf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
    	
        return mDayPaint;
    }
	
	//Iuni <lory><2014-02-22> add begin
    private TextPaint setDefaultYearPaint(float textSize, int color) {
    	mYearPaint = new TextPaint();
    	mYearPaint.setTextSize(textSize);
    	mYearPaint.setAntiAlias(true);
    	mYearPaint.setColor(color);
    	
    	/*
    	try {
    		mYearPaint.setTypeface(m_auroraYearAndMonthNumberTf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
    	
        return mYearPaint;
    }

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
    
    //Iuni <lory><2014-02-22> add begin
//    private TextPaint setDefaultPaint(float textSize, int color) {
//    	mPaint = new TextPaint();
//    	mPaint.setTextSize(textSize);
//    	mPaint.setAntiAlias(true);
//    	mPaint.setColor(color);
//    	
//    	try {
//    		mPaint.setTypeface(m_auroraYearAndMonthNumberTf);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    	
//        return mPaint;
//    }
    
    ////Iuni <lory><2014-02-22> add begin
    private void setFontMetricsInt(TextPaint paint){
    	if (paint == null) {
    		//setDefaultDayPaint(MY_DEFAULT_TEXT_SIZE, Color.BLACK);
    		setDefaultDayPaint(m_DaySize, Color.parseColor("#000000"));
		}
    	metricsInt = paint.getFontMetricsInt();
    	return;
    }
    
	//Iuni <lory><2014-02-22> add begin
    private String mLastStr = "";
    protected void drawHeaderAnimationContent(GLCanvas canvas, String title, boolean bheader, int bottom, boolean bDay) {
    	//long time1 = System.currentTimeMillis();
    	//Log.i("zll", "zll --- drawHeaderAnimationContent title:"+title+",mLastStr:"+mLastStr);
    	if (title == null) {
			return;
		} else if (title.equals("")) {
			mLastStr = "";
			if (mEmptyAnimation != null) {
				mEmptyAnimation.draw(canvas, 0, 0);
			}
			return;
		}
    	
    	String t1 = title.substring(title.length()-2, title.length());
    	if (mLastStr.equals(title) && mDataAnimation != null) {
    		mDataAnimation.draw(canvas, 0, 0);
		} else {
			String tmpStr = mDay;
			if (bDay) {
				tmpStr = mMonth;
			}
			AuroraStringTexture dateTextView =  StringCache.getStringTexture(mContext, t1,tmpStr,title.substring(0, title.length()-3),
					mDayPaint, mPaint,mYearPaint, bDay);
	    	if (dateTextView != null) {
	    		//Log.i("zll", "zll ----- drawHeaderAnimationContent t1:"+t1+",substring:"+title.substring(0, title.length()-3));
	    		dateTextView.draw(canvas, 0, 0);
			}
	    	mDataAnimation = dateTextView;
		}
    	
    	mLastStr = title;
    	
		return;
    }
	
    //Iuni <lory><2014-02-22> add begin
    protected void drawHeaderContent(GLCanvas canvas, String title, int width, int height, boolean bDay) {
    	//long time1 = System.currentTimeMillis();
    	//Log.i("zll", "zll --- eeee drawHeaderContent title:"+title+",bDay:"+bDay);
    	if (title.equals("")) {
    		/*AuroraStringTexture dateTextView =  new AuroraStringTexture("","","",
					mDayPaint, mPaint,mYearPaint);
			dateTextView.draw(canvas, 0, 0);*/
    		if (mEmptyAnimation != null) {
				mEmptyAnimation.draw(canvas, 0, 0);
			}
			return;
		} 
    	
    	String t1 = title.substring(title.length()-2, title.length());
    	if (true) {
    		String tmpStr = mDay;
    		//bDay = false;
    		if (bDay) {
    			tmpStr = mMonth;
			}
    		//Aurora <SQF> <2014-04-15>  for NEW_UI begin
    		//ORIGINALLY:
    		//AuroraStringTexture dateTextView =  new AuroraStringTexture(t1,tmpStr,title.substring(0, title.length()-3),
    		//mDayPaint, mPaint,mYearPaint);
    		//SQF MODIFIED TO:
    		//Log.i("SQF_LOG", "zll --- eeee drawHeaderContent " + " title:" + title + " tmpStr:"+tmpStr+",t1:"+t1);
    		AuroraStringTexture dateTextView =  StringCache.getStringTexture(mContext, t1,tmpStr,title.substring(0, title.length()-3),
					mDayPaint, mPaint,mYearPaint, bDay);
    		//Aurora <SQF> <2014-04-15>  for NEW_UI end
    		

			dateTextView.draw(canvas, 0, 0);
        	
		} else {
			StringTexture dateTextView = StringTexture.newInstance(t1, MY_DEFAULT_TEXT_SIZE, Color.BLACK);
	    	dateTextView.draw(canvas, 100, 2);
	    	
	    	StringTexture yearTextView = StringTexture.newInstance(title.substring(0, title.length()-3), MY_DEFAULT_TEXT_SIZE, Color.BLACK);
	    	yearTextView.draw(canvas, 40, (int)(2+MY_DEFAULT_TEXT_SIZE));
		}
    	
    }
    
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

    ////Iuni <lory><2014-02-22> add begin
    protected void drawImgCheckIcon(GLCanvas canvas, int width, int height) {
    	
    	int w = mCheckIcon.getWidth();
    	int h = mCheckIcon.getHeight();
    	mCheckIcon.draw(canvas, width-w, 0, w, h);
		return;
	}
    
    ////Iuni <lory><2014-02-22> add begin
    protected void drawImgUnCheckIcon(GLCanvas canvas, int width, int height) {
    	int w = mUnCheckIcon.getWidth();
    	int h = mUnCheckIcon.getHeight();
    	
    	mUnCheckIcon.draw(canvas, width-w, 0, w, h);
		return;
	}
	
	//paul add start
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
			mIcon.draw(canvas, width-w, 0, w, h);
		}
    }
	//paul add end

    protected void drawVideoOverlay(GLCanvas canvas, int width, int height) {
        // Scale the video overlay to the height of the thumbnail and put it
        // on the left side.
    	if (true) {
    		int s = Math.min(width, height)/3;
            mVideoPlayIcon.draw(canvas, (width - s) / 2, (height - s) / 2, s, s);
            
    		return;
		}
    	
        ResourceTexture v = mVideoOverlay;
        float scale = (float) height / v.getHeight();
        int w = Math.round(scale * v.getWidth());
        int h = Math.round(scale * v.getHeight());
        v.draw(canvas, 0, 0, w, h);

        int s = Math.min(width, height) / 6;
        mVideoPlayIcon.draw(canvas, (width - s) / 2, (height - s) / 2, s, s);
    }

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
