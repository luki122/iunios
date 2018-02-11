

package com.android.gallery3d.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.android.gallery3d.util.GalleryUtils;
//Aurora <SQF> <2014-04-15>  for NEW_UI begin
import android.content.Context;
import android.content.res.Configuration;
//Aurora <SQF> <2014-04-15>  for NEW_UI end
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;

public class AuroraStringTexture extends CanvasTexture {
	private static final int CACHE_SIZE = 500;
	private static final int LEFT_SIZE = CACHE_SIZE / 5;
	
    private String mText;
    private final TextPaint mPaint;

	private String mText2;
	private String mText3;
	private TextPaint mPaint2;
	private TextPaint mPaint3;
	private boolean mbDay = false;

	private static final int sWidth = GalleryUtils.dpToPixel(64);
	public static final int sHeight = GalleryUtils.dpToPixel(36);
	private static final int rightMargin = GalleryUtils.dpToPixel(5);
	private static final int spacingBetweenTextAndText2 = GalleryUtils.dpToPixel(2);
	
	public static int mX1 = GalleryUtils.dpToPixel(21);
	private static int mX2 = GalleryUtils.dpToPixel(45);
	private static int mX3 = GalleryUtils.dpToPixel(12);
	private static int mY1 = (int)Math.round(GalleryUtils.dpToPixel(-0.6f));//-0.6
	private static int mY2 = (int)Math.round(GalleryUtils.dpToPixel(16f));//16
	//private static final int mX4 = GalleryUtils.dpToPixel(27);
	//Aurora <SQF> <2014-04-15>  for NEW_UI begin
	private Context mContext;
	//Aurora <SQF> <2014-04-15>  for NEW_UI end

	public void setText(String text,String text2,String text3){
		mText = text;
		mText2 = text2;
		mText3 = text3;
	}

	public AuroraStringTexture(Context context, String text,String text2,String text3, 
			TextPaint paint,TextPaint paint2,TextPaint paint3, boolean bDay) {
        super(sWidth, sHeight);
        //Log.i("SQF_LOG", "AuroraStringTexture text:" + text + " text2:" + text2 + " text3:" + text3);
        mContext = context;
        
        //in Chinese mode:
        	//in Month mode , mText, mText2, mText3 is 06, "月", 2014
        	//in Date mode, mText, mText2, mText3 is 22, "日", 2014.06
        //in English mode:
        	//in Month mode , mText, mText2, mText3 is "Aug.", "", 2014
    		//in Date mode, mText, mText2, mText3 is 22, "nd", 2014.06
        mText = text;
		mText2 = text2;
		mText3 = text3;

        mPaint = paint;
		mPaint2 = paint2;
		mPaint3 = paint3;
		mbDay = bDay;
		
		//Aurora <SQF> <2014-6-24>  for NEW_UI begin
		mapText2ToOrdinalNumberOrMonthAbbreviation();
		//Aurora <SQF> <2014-6-24>  for NEW_UI end
    }
	
	//Aurora <SQF> <2014-6-24>  for NEW_UI begin
	private void mapText2ToOrdinalNumberOrMonthAbbreviation() {
		if(TextUtils.isEmpty(mText) || TextUtils.isEmpty(mText2)) return;
		if(GalleryUtils.isInEnglish()) {
			if(! mbDay) {
				if(mText.endsWith("1")) {
					mText2 = "st";
				} else if(mText.endsWith("2")) {
					mText2 = "nd";
				} else if(mText.endsWith("3")) {
					mText2 = "rd";
				} else {
					mText2 = "th";
				} 
			} else {
				String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
				int monthIndex = Integer.valueOf(mText) - 1;
				int index = monthIndex < 0 ? 0 : monthIndex;
				mText = months[index];
				mText2 = "";
				
				float width = mPaint.measureText(mText);
    			mX1 = sWidth - (int)width - rightMargin;
			}
		}
	}
    //Aurora <SQF> <2014-6-24>  for NEW_UI end

    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
    	//Log.i("zll", "zll ---- mText:"+mText+",mText2:"+mText2+",mText3:"+mText3);
        canvas.translate(0,sHeight>>1);
        //Aurora <SQF> <2014-04-15>  for VisualDesigner's request begin
        if(null != mContext) {
        	if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        		mY1 = (int)Math.round(GalleryUtils.dpToPixel(-0.6f));
        		mY2 = (int)Math.round(GalleryUtils.dpToPixel(16f));
        	} else {
        		mY1 = (int)Math.round(GalleryUtils.dpToPixel(-1.3f));
        		mY2 = (int)Math.round(GalleryUtils.dpToPixel(15.33f));
        	}
        }
        //Log.i("SQF_LOG", "mX1:" + mX1 + " mX2:" + mX2 + " mY1:" + mY1 + " mY2:" + mY2);
        //Aurora <SQF> <2014-04-15>  for VisualDesigner's request end
        
        mX2 = sWidth - (int)mPaint2.measureText(mText2) - rightMargin;
        mX1 = sWidth - (int)mPaint2.measureText(mText2) - rightMargin - (int)mPaint.measureText(mText) - spacingBetweenTextAndText2;
        canvas.drawText(mText, mX1, mY1 , mPaint);
		canvas.drawText(mText2, mX2, mY1 , mPaint2);
		
		
		
	    //Aurora <SQF> <2014-08-13>  for NEW_UI begin
	    //ORIGINALLY:
		/*
		if (mbDay) {
			canvas.drawText(mText3, mX4, mY2 , mPaint3);
		} else {
			canvas.drawText(mText3, mX3, mY2 , mPaint3);
		}
		*/
	    //SQF MODIFIED TO:
		mX3 = sWidth - (int)mPaint3.measureText(mText3) - rightMargin;
		canvas.drawText(mText3, mX3, mY2 , mPaint3);
	    //Aurora <SQF> <2014-08-13>  for NEW_UI end
		
		
    }
    

    public static class StringCache {
        private static HashMap<String, WeakReference<AuroraStringTexture>> mCache = new HashMap<String, WeakReference<AuroraStringTexture>>();
      
        private static void delNullItem(){
        	ArrayList<String> keys = new ArrayList<String>();
            for (Map.Entry<String, WeakReference<AuroraStringTexture>> entry : mCache.entrySet()) {
            	if(null == entry.getValue().get()){
            		keys.add(entry.getKey());
            	}
            }
            
            if(keys.size() <= LEFT_SIZE || keys.size() == mCache.size()){
            	mCache.clear();
            	return;
            }
            
            for (String key : keys) {
            	mCache.remove(key);
            }
        }
        
        public static void clear(){
        	mCache.clear();
        }
        
        public static AuroraStringTexture getStringTexture(Context context, String text1,String text2,String text3, 
    			TextPaint paint,TextPaint paint2,TextPaint paint3, boolean bDay) {
        	StringBuffer tb = new StringBuffer(text1);
        	tb.append(text2);
        	tb.append(text3);
        	String text = tb.toString();
			
        	WeakReference<AuroraStringTexture> t = mCache.get(text);
        	AuroraStringTexture nt = null;
        	if(t != null){
        		nt = t.get();
        		if(nt != null) return nt;
        	}
        	
        	if(CACHE_SIZE <= mCache.size()){
        		delNullItem();
        	}
        	
    		nt = new AuroraStringTexture(context, text1, text2, text3, 
        			 paint, paint2, paint3, bDay);
    		WeakReference<AuroraStringTexture> wt = new WeakReference<AuroraStringTexture>(nt);
    		mCache.put(text, wt);
        	return nt;
        }
    }
}
