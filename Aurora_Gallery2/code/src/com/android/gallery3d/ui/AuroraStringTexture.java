

package com.android.gallery3d.ui;

//import java.lang.ref.WeakReference;
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
import com.android.gallery3d.ui.SlotView;

public class AuroraStringTexture extends CanvasTexture {
	
	//private static final int LEFT_SIZE = CACHE_SIZE / 5;
	
    public String mTextDay;
	public String mTextDayStr;
    public String mTextMon;
	public String mTextMonStr;

	public String mTextYear;
	public boolean mNeedYear = false;
	public String mDrawingText;

	
	public String mAddress = null;
	
	//private int mAddressLimitX = -1;
	//private TextPaint mPaint2;
	//private TextPaint mPaint3;
	private TextPaint mAddressPaint;
	private final TextPaint mPaint;
	private boolean mIsMonth = false;


	private static final int rightMargin = GalleryUtils.dpToPixel(16);
	private static final int textHalfHeight = GalleryUtils.dpToPixel(6);
	//private static final int spacingBetweenTextAndText2 = GalleryUtils.dpToPixel(2);
	
	private static final int sTextX = rightMargin;//GalleryUtils.dpToPixel(16);
	private static final int sTextY = (int)(GalleryUtils.dpToPixel(13.6f) + 0.5f);
	
	//private static int mX2 = GalleryUtils.dpToPixel(45);
	//private static int mY2 = (int)(GalleryUtils.dpToPixel(16f) + 0.5f);//16

	private int mAddressX = 0;
	//private static final int mX4 = GalleryUtils.dpToPixel(27);

	//day, strDay, month,  strMonth,  year, yearStr
	public AuroraStringTexture(String str[],
			TextPaint paint,TextPaint paint2,TextPaint paint3, boolean isMonth, boolean needYear, String address, int w, int h) {

        super(w, h);
        //in Chinese mode:
        	//in Month mode , mText, mText2, mText3 is 06, "月", 2014
        	//in Date mode, mText, mText2, mText3 is 22, "日", 2014.06
        //in English mode:
        	//in Month mode , mText, mText2, mText3 is "Aug.", "", 2014
    		//in Date mode, mText, mText2, mText3 is 22, "nd", 2014.06
        mTextDay = str[0];
		mTextDayStr = str[1];
		mTextMon = str[2];
		mTextMonStr = str[3];
		mTextYear = str[4] + str[5];
		mAddress = address;
		
        mPaint = paint;
		//mPaint2 = paint2;
		//mPaint3 = paint3;
		mIsMonth = isMonth;
		mNeedYear = needYear;
		mAddressPaint = new TextPaint(paint);
		mAddressPaint.setAlpha(128);
		//mAddressLimitX = (mWidth>>1);

		//Aurora <SQF> <2014-6-24>  for NEW_UI begin
		mapText2ToOrdinalNumberOrMonthAbbreviation();
		//Aurora <SQF> <2014-6-24>  for NEW_UI end
		StringBuffer tb = null;
		if(isMonth){
			tb = new StringBuffer(mTextYear);//year
			tb.append(mTextMon);//month
			tb.append(mTextMonStr);
		} else if(needYear){
			tb = new StringBuffer(mTextYear);//year
			tb.append(mTextMon);//month
			tb.append(mTextMonStr);
			tb.append(mTextDay);//day
			tb.append(mTextDayStr);
		} else {
			tb = new StringBuffer(mTextDay);//day
			tb.append(mTextDayStr);
		}
		mDrawingText = tb.toString();

    }
	
	//Aurora <SQF> <2014-6-24>  for NEW_UI begin
	private void mapText2ToOrdinalNumberOrMonthAbbreviation() {
		if(GalleryUtils.isInEnglish()) {
			if(!mIsMonth) {
				if(mTextDay.endsWith("1")) {
					mTextDay = "st";
				} else if(mTextDay.endsWith("2")) {
					mTextDay = "nd";
				} else if(mTextDay.endsWith("3")) {
					mTextDay = "rd";
				} else {
					mTextDay = "th";
				} 
			} else {
				String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
				int monthIndex = Integer.valueOf(mTextMon) - 1;
				int index = monthIndex < 0 ? 0 : monthIndex;
				mTextMon = months[index];
				mTextDayStr = "";
				//float width = mPaint.measureText(mText);
    			//sTextX = mWidth - (int)width - rightMargin;
			}
		}
	}
    //Aurora <SQF> <2014-6-24>  for NEW_UI end

    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
		/*
        canvas.translate(0, mHeight>>1);
        if(null != mContext) {
        	if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        		mY1 = (int)(GalleryUtils.dpToPixel(-0.6f) + 0.5f);
        		mY2 = (int)(GalleryUtils.dpToPixel(16f) + 0.5f);
        	} else {
        		mY1 = (int)(GalleryUtils.dpToPixel(-1.3f) + 0.5f);
        		mY2 = (int)(GalleryUtils.dpToPixel(15.33f) + 0.5f);
        	}
        }
        
        mX2 = mWidth - (int)mPaint2.measureText(mText2) - rightMargin;
        mX1 = mWidth - (int)mPaint2.measureText(mText2) - rightMargin - (int)mPaint.measureText(mText) - spacingBetweenTextAndText2;
        canvas.drawText(mText, mX1, mY1 , mPaint);
		canvas.drawText(mText2, mX2, mY1 , mPaint2);
		

		mX3 = mWidth - (int)mPaint3.measureText(mText3) - rightMargin;
		canvas.drawText(mText3, mX3, mY2 , mPaint3);
		
		mX3 = mWidth - (int)mAddressPaint.measureText(mAddress) - rightMargin;
		canvas.drawText(mAddress, mX3, mY2 , mAddressPaint);
		*/
		canvas.translate(0, textHalfHeight);
		canvas.drawText(mDrawingText, sTextX, sTextY , mPaint);//draw day or month

		//draw address
		if(null != mAddress && mAddress.length() > 0){
			mAddressX = mWidth - (int)mAddressPaint.measureText(mAddress) - rightMargin;
			//if(mAddressX < mAddressLimitX) mAddressX = mAddressLimitX;
			canvas.drawText(mAddress, mAddressX, sTextY , mAddressPaint);
		}
    }
    

    public static class StringCache {
		private static final int CACHE_SIZE = 24;
        private HashMap<String, AuroraStringTexture> mCache = new HashMap<String, AuroraStringTexture>();
        public AuroraStringTexture getStringTexture(String str[],
    			TextPaint paint,TextPaint paint2,TextPaint paint3, boolean isMonth, boolean needYear, String address, int w, int h) {
        	StringBuffer tb = null;
			String text = str[0];//day
			if(isMonth){
				tb = new StringBuffer(str[4]);//year
				tb.append(str[2]);//month
				text = tb.toString();
			} else if(needYear) {
				tb = new StringBuffer(str[4]);//year
				tb.append(str[2]);//month
				tb.append(str[0]);//day
				text = tb.toString();
			}

        	AuroraStringTexture nt = mCache.get(text);
        	if(nt != null){
				
				if(nt.mAddress == address || (address != null && address.equals(nt.mAddress))){
					//same address
				} else {
					nt.mAddress = address;
					nt.resetCanvas();
				}

				return nt;
        	}
        	
        	if(CACHE_SIZE <= mCache.size()){
				Log.d("StringCache","==clear StringCache:" + mCache.size());
				for (Map.Entry<String, AuroraStringTexture> entry : mCache.entrySet()) {
					AuroraStringTexture t = entry.getValue();
					if(null != t) t.recycle();
				}
        		mCache.clear();
        	}
    		nt = new AuroraStringTexture(str, paint, paint2, paint3, isMonth, needYear, address, w, h);
    		mCache.put(text, nt);
        	return nt;
        }
    }
}
