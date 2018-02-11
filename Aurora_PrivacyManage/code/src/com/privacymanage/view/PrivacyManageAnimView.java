package com.privacymanage.view;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import com.privacymanage.interfaces.AnimationEndListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Process;
import android.util.AttributeSet;
import android.view.View;
import com.aurora.privacymanage.R;

public class PrivacyManageAnimView extends View{
	private final int[] aniRes = {R.drawable.privay_manage_ani1,R.drawable.privay_manage_ani2,
			R.drawable.privay_manage_ani3,R.drawable.privay_manage_ani4,
			R.drawable.privay_manage_ani5,R.drawable.privay_manage_ani6,
			R.drawable.privay_manage_ani7,R.drawable.privay_manage_ani8,
			R.drawable.privay_manage_ani9,R.drawable.privay_manage_ani10,
			R.drawable.privay_manage_ani11,R.drawable.privay_manage_ani12,
			R.drawable.privay_manage_ani13,R.drawable.privay_manage_ani14,
			R.drawable.privay_manage_ani15,R.drawable.privay_manage_ani16,
			R.drawable.privay_manage_ani17,R.drawable.privay_manage_ani18,
			R.drawable.privay_manage_ani19,R.drawable.privay_manage_ani20,
			R.drawable.privay_manage_ani21,R.drawable.privay_manage_ani22,
			R.drawable.privay_manage_ani23,R.drawable.privay_manage_ani24,
			R.drawable.privay_manage_ani25	};
	
	private final int AniSpaceTime = 50;
	private final int MAX_BITMAP_NUM_IN_POOL = 3;
	private AnimationEndListener animationImageListener;
	private final List<Bitmap> bitmapList = new ArrayList<Bitmap>();
    private boolean isRun;
    private LoadBitmapThread loadBitmapThread = null;
    private int curIndexOfBitmap;
    private Paint mPaint;
    private Bitmap curBitmap = null;
    
	
	public PrivacyManageAnimView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PrivacyManageAnimView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PrivacyManageAnimView(Context context) {
		super(context);
		init();
	}
		
    @Override
	protected void onDraw(Canvas canvas) {
    	if(curBitmap != null && !curBitmap.isRecycled()){
    		canvas.drawBitmap(curBitmap, 0, 0, mPaint);
    	}  	
		super.onDraw(canvas);
	}

	private void init() {
        this.isRun = false;
        mPaint = new Paint();
    }
    
    public void start() {
        if(isRun) {
            return;
        }      
        this.isRun = true;
        curIndexOfBitmap = 0;
        bitmapList.clear();	
        curBitmap = null;
        startGetBitmap();        
        nextFrame();
    }
    
    public void stop() {
        this.isRun = false;
    }
    
    public void setAnimationImageListener(AnimationEndListener animationImageListener) {
        this.animationImageListener = animationImageListener;
    }
    
    private void end() {
        if(animationImageListener != null) {
            animationImageListener.onAnimationEnd();
        }
        this.isRun = false;
        System.gc();
    }
    
    private void nextFrame() {
    	Bitmap tmpbm= getBitmap();
    	if(tmpbm == null){
    		end();
    		return ;
     	}
    	curBitmap = tmpbm;
    	invalidate();
        postDelayed(nextFrameRun,AniSpaceTime);
    }
    
    private Runnable nextFrameRun = new Runnable() {
        public void run() {
            if(!isRun) {
                end();
                return;
            }
            nextFrame();
        }
    };
    
    private void startGetBitmap(){
    	if(loadBitmapThread == null){
   		  loadBitmapThread = new LoadBitmapThread();
   	    }
    	loadBitmapThread.start();
    }
       
    private final class LoadBitmapThread extends Thread {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);          
            Resources curRes = getResources();
            curIndexOfBitmap = 0;
            bitmapList.clear();
            while(isRun){
            	if(curIndexOfBitmap>=aniRes.length){
                	break;
                }          	
                synchronized (bitmapList) {
                    if (bitmapList.size() > MAX_BITMAP_NUM_IN_POOL) {
                    	try{
                    		bitmapList.wait();             		
                    	}catch(Exception e){
                    		e.printStackTrace();
                    	}               	
                    }
                }
                             
                Bitmap bitmap = ((BitmapDrawable)curRes.
                		getDrawable(aniRes[curIndexOfBitmap])).getBitmap();
                synchronized (bitmapList) {
                	bitmapList.add(bitmap); 
                	 curIndexOfBitmap++;
                	 bitmapList.notify();
                }                          
            }        
        }
    }
        
    private synchronized Bitmap getBitmap(){	    	   
 	   Bitmap bitmap;
 	   synchronized (bitmapList) {		
          if (bitmapList.size() == 0) {
        	  if(curIndexOfBitmap >= aniRes.length-1){
        		  return null; 
        	  }else{
        		 try{
             		 bitmapList.wait();
             	 }catch(Exception e){
             		 e.printStackTrace();
             	 }
        	  }	        	 
          }
          bitmap = bitmapList.get(0);
     	  bitmapList.remove(0);
     	  bitmapList.notify();
       }
 	   return bitmap; 	   
    }
}
