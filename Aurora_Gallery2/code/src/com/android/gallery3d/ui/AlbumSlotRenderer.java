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

package com.android.gallery3d.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.android.gallery3d.R;
import android.R.integer;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.AlbumDataLoader.Position;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import android.graphics.Bitmap;//Aurora <paul> <2014-02-27> for NEW_UI
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;


public class AlbumSlotRenderer extends AbstractSlotRenderer {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumSlotRenderer";

    public interface SlotFilter {
        public boolean acceptSlot(int index);
    }

    private final int mPlaceholderColor;
    private static final int CACHE_SIZE = AlbumDataLoader.MAX_LOAD_COUNT; //SQF modified 106 to AlbumDataLoader.MAX_LOAD_COUNT //106;//96,Iuni <lory><2014-01-16> add for test

    private AlbumSlidingWindow mDataWindow;
    private final AbstractGalleryActivity mActivity;
    private final ColorTexture mWaitLoadingTexture;
    private final SlotView mSlotView;
    private final SelectionManager mSelectionManager;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;

    private SlotFilter mSlotFilter;
    
    private int m_TotalNum = 0;
    private AlbumDataLoader mAlbumDataLoader = null;
    
    //Aurora <SQF> <2014-05-17>  for NEW_UI begin
    private int mEnteringOrLeavingEffectState = AlbumPage.state.IDLE;
    //Aurora <SQF> <2014-05-17>  for NEW_UI end

    public AlbumSlotRenderer(AbstractGalleryActivity activity, SlotView slotView,
            SelectionManager selectionManager, int placeholderColor) {
        super(activity);
        mActivity = activity;
        mSlotView = slotView;
        mSelectionManager = selectionManager;
        mPlaceholderColor = placeholderColor;

        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
        
        //Iuni <lory><2014-01-20> add begin start
        m_TotalNum = 0;
        mAlbumDataLoader = null;
        /*mContainers = null;
        mContainers = new ArrayList<Container>();
        mList = null;
        mAlbumDataLoader = null;*/
        //mList = new ArrayList<MediaItem>();
      //Iuni <lory><2014-01-20> add begin end
    }

    public void setPressedIndex(int index) {
    	if (false) {//MySelfBuildConfig.USEGALLERY3D_FLAG
    		int tindex1 = index;
    		int tindex2 = tindex1+1;
    		tindex1 += index2SlotViewPosition(tindex2);
    		
    		if (mPressedIndex == tindex1) return;
    		mPressedIndex = tindex1;
		} else {
			//Log.i(TAG, "zll ---- AlbumSlotRenderer mPressedIndex:"+mPressedIndex);
			if (mPressedIndex == index) return;
			mPressedIndex = index;
		}
        
		//mSlotView.setPressedSlot(mPressedIndex);//Aurora <paul> <2014-02-27> for NEW_UI
        mSlotView.invalidate();
    }

    public void setPressedUp() {
    	//Log.i(TAG, "zll ---- AlbumSlotRenderer setPressedUp mPressedIndex:"+mPressedIndex);
        if (mPressedIndex == -1) return;
        mAnimatePressedUp = true;
        //Log.i(TAG, "zll ---- AlbumSlotRenderer setPressedUp mPressedIndex:"+mPressedIndex);
        mSlotView.invalidate();
    }

    public void setHighlightItemPath(Path path) {
        if (mHighlightItemPath == path) return;
        mHighlightItemPath = path;
        mSlotView.invalidate();
    }

    public void setModel(AlbumDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mSlotView.setSlotCount(0);
            mDataWindow = null;
        }
        if (model != null) {
        	//Aurora <SQF> <2014-05-13>  for NEW_UI begin
        	//ORIGINALLY:
        	//mDataWindow = new AlbumSlidingWindow(mActivity, model, CACHE_SIZE);
        	//SQF MODIFIED TO:
        	mDataWindow = new AlbumSlidingWindow(mActivity, this, model, CACHE_SIZE);
        	//Aurora <SQF> <2014-05-13>  for NEW_UI end
            
            mDataWindow.setListener(new MyDataModelListener());
            
            if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            	mAlbumDataLoader = model;
				mSlotView.setAlbumDataLoader(model);
			}
            m_TotalNum = model.size();//Iuni <lory><2014-01-20> add begin
            mSlotView.setSlotCount(m_TotalNum);
            //Log.i(TAG, "zll ---- setModel xxxx m_TotalNum:"+m_TotalNum);
        }
    }

    private static Texture checkTexture(Texture texture) {
        return (texture instanceof TiledTexture)
                && !((TiledTexture) texture).isReady()
                ? null
                : texture;
    }

	//Aurora <paul> <2014-02-27> for NEW_UI begin
	public int getPressedIndex(){
		return mPressedIndex;
	}
	public Bitmap getBitmapByIndex(int slotIndex){
		//Log.i("SQF_LOG", "AlbumSlotRenderer::getBitmapByIndex --> slotIndex:" + slotIndex);
		AlbumSlidingWindow.AlbumEntry entry = mDataWindow.get(slotIndex);
		if(null == entry) return null;
		return mDataWindow.get(slotIndex).bitmap;
	}
	//Aurora <paul> <2014-02-27> for NEW_UI end	

   
    //Iuni <lory><2014-02-22> add begin
	public void hideFirstHeaderView(int header) {
		
		
		return;
	}
	
	public void showFirstHeaderView() {
		return;
	}
	
	@Override
	public int renderHeaderAnimation(GLCanvas canvas, int firstVisibleItem, int bottom, boolean bheader, boolean bDay) {
		if (mAlbumDataLoader == null) {
			return 0;
		}
		
		if (firstVisibleItem == -100) {
			drawHeaderAnimationContent(canvas, "", bheader, bottom, bDay);
			return 0;
		}
		
		String tmpstr = mAlbumDataLoader.getMyHeaderTitle(firstVisibleItem);
		//Log.i(TAG, "zll ---- rrrr tmpstr:"+tmpstr+",firstVisibleItem:"+firstVisibleItem);
		if (tmpstr.length() == 0 || tmpstr.equals("")) {
			return 0;
		}
		
		//drawHeaderContent(canvas, "", 0, 0);
		drawHeaderAnimationContent(canvas, tmpstr, bheader, bottom, bDay);
		canvas.setAlpha(1f);
		return 0;
	}
	
    @Override
	public int renderHeaderSlot(GLCanvas canvas, int index, String tile,int width, int height, boolean bDay) {    	
    	String tiletime = "";
    	if (index != -2 && mAlbumDataLoader != null) {
    		tiletime = mAlbumDataLoader.getMyDateString(index);
		}
    	//Log.i("SQF_LOG", "zll ----- renderHeaderSlot tiletime:"+tiletime+",index:"+index);
    	//long time2 = System.currentTimeMillis();
    	drawHeaderContent(canvas, tiletime, width, height, bDay);
    	//Log.i(TAG, "zll --- gggggggg 1.3 time is :"+(System.currentTimeMillis() - time2));
    	
		return 0;
	}
    
    //private boolean gggg = false;
    @Override
    public int renderSlot(GLCanvas canvas, int index, int pass, int width, int height) {
        if (mSlotFilter != null && !mSlotFilter.acceptSlot(index)) return 0;
        
        int t_index = 0;
        if (mAlbumDataLoader != null) {
        	Position pos = mAlbumDataLoader.translatePosition(index);
        	t_index = pos.mPosition;
		}
        //Log.i(TAG, "zll ---- renderSlot 1.1 index:"+index+",width:"+width+",height:"+height);
        
        AlbumSlidingWindow.AlbumEntry entry = mDataWindow.get(t_index);
        //AlbumSlidingWindow.AlbumEntry entry = mDataWindow.get(index);
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	if (entry == null) {
        		//Log.i(TAG, "zll ---- renderSlot 1.2 index:"+index);
    			return 0;
    		}
		}
        
        //InitItems(entry);//Iuni <lory><2014-01-20> add begin
        
        int renderRequestFlags = 0;
        //Aurora <SQF> <2014-05-13>  for NEW_UI begin
        Texture content = null;
        //Aurora <SQF> <2014-05-13>  for NEW_UI end
        	
        	//Aurora <SQF> <2014-05-13>  for NEW_UI begin
        	//ORIGINALLY:
        	/*
        	content = checkTexture(entry.content);
	        if (content == null) {
	            content = mWaitLoadingTexture;
	            entry.isWaitDisplayed = true;
	            //Log.i(TAG, "zll ---- renderSlot 1 index:"+index);
	        } else if (entry.isWaitDisplayed) {
	            entry.isWaitDisplayed = false;
	            content = new FadeInTexture(mPlaceholderColor, entry.bitmapTexture);
	            entry.content = content;
	            //Log.i(TAG, "zll ---- renderSlot 2 index:"+index);
	        }
	        */
        	//SQF MODIFIED TO:
        	content = checkTexture(entry.cropCenterBitmapTexture);
        	if(content == null) {
	        	content = checkTexture(entry.content);
	    	    if (content == null) {
	    	        content = mWaitLoadingTexture;
	    	        entry.isWaitDisplayed = true;
	    	        //Log.i(TAG, "zll ---- renderSlot 1 index:"+index);
	    	    } else if (entry.isWaitDisplayed) {
	    	        entry.isWaitDisplayed = false;
	    	        content = new FadeInTexture(mPlaceholderColor, entry.bitmapTexture);
	    	        entry.content = content;
	    	        //Log.i(TAG, "zll ---- renderSlot 2 index:"+index);
	    	    }
        	}
        	//Aurora <SQF> <2014-05-13>  for NEW_UI end
        //Log.i("SQF_LOG", "zll ---- renderSlot index:"+index+",rotation:"+entry.rotation+",width:"+width+",height:"+height+",mInSelectionMode:"+mInSelectionMode);
        drawContent(canvas, content, width, height, entry.rotation);
        if ((content instanceof FadeInTexture) &&
                ((FadeInTexture) content).isAnimating()) {
            renderRequestFlags |= SlotView.RENDER_MORE_FRAME;
        }

        if (entry.mediaType == MediaObject.MEDIA_TYPE_VIDEO) {
            drawVideoOverlay(canvas, width, height);
        }

        if (entry.isPanorama) {
        	//Log.i(TAG, "zll ---- renderSlot cccc entry.isPanorama");
            drawPanoramaIcon(canvas, width, height);
        }
		
		//paul del
        /*
        if (MySelfBuildConfig.USEGALLERY3D_FLAG && mInSelectionMode) {
        	drawImgUnCheckIcon(canvas, width, height);
		}
		*/
		
        renderRequestFlags |= renderOverlay(canvas, index, entry, width, height);

        return renderRequestFlags;
    }
    
    private int index2SlotViewPosition(int index){
    	//int pos = 0;
    	if (index == 0) {
			return 1;
		}
    	
    	if (index < 0 || mAlbumDataLoader == null) {
			return 0;
		}
    	
    	return mAlbumDataLoader.getHeaderNumByIndex(index);
    }

    private int renderOverlay(GLCanvas canvas, int index,
            AlbumSlidingWindow.AlbumEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        
	
		//paul modify start
		/*
        if (mPressedIndex == index) {
			//Aurora <paul> <2014-02-27> for NEW_UI
            if (mAnimatePressedUp) {
            	//Log.i(TAG, "zll ---- renderOverlay 0 index:"+index);
                drawPressedUpFrame(canvas, width, height);
                renderRequestFlags |= SlotView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
            } else {
            	//Log.i(TAG, "zll ---- renderOverlay 1 index:"+index);
            	if (!MySelfBuildConfig.USEGALLERY3D_FLAG) {
            		drawPressedFrame(canvas, width, height);
				} 
            }
        } else if ((entry.path != null) && (mHighlightItemPath == entry.path)) {
        	//Log.i(TAG, "zll ---- renderOverlay 2 index:"+index+",mPressedIndex:"+mPressedIndex);
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        		drawImgCheckIcon(canvas, width, height);
			} else {
				drawSelectedFrame(canvas, width, height);
			}
        } else if (mInSelectionMode && mSelectionManager.isItemSelected(entry.path)) {
        	//Log.i(TAG, "zll ---- renderOverlay 3 index:"+index+",mPressedIndex:"+mPressedIndex);
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        		drawImgCheckIcon(canvas, width, height);
			} else {
				drawSelectedFrame(canvas, width, height);
			}
        }		
		*/
		if (mInSelectionMode) {
        	if(playCheckAnimation(canvas, width, height, index)){
				renderRequestFlags |= SlotView.RENDER_MORE_FRAME;
			} else {
	        	if(mSelectionManager.isItemSelected(entry.path)){
		    		drawImgCheckIcon(canvas, width, height);
	        	} else {
		    		drawImgUnCheckIcon(canvas, width, height);
				}
			}
        }
        //paul modify end
        return renderRequestFlags;
    }

    private class MyDataModelListener implements AlbumSlidingWindow.Listener {
        @Override
        public void onContentChanged() {
            mSlotView.invalidate();
        }

        @Override
        public void onSizeChanged(int size) {
        	m_TotalNum = size;
            mSlotView.setSlotCount(size);
			mSlotView.invalidate();	//paul add
        }
    }

    public void resume() {
        mDataWindow.resume();
    }

    public void pause() {
        mDataWindow.pause();
    }

    @Override
    public void prepareDrawing() {
        mInSelectionMode = mSelectionManager.inSelectionMode();
    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public void onSlotSizeChanged(int width, int height) {
        // Do nothing
    	//Aurora <SQF> <2014-05-13>  for NEW_UI begin
    	if(width < 200) {
    		if(null != mDataWindow) mDataWindow.setThumbnailLoadInfo(true);
    	} else {
    		if(null != mDataWindow) mDataWindow.setThumbnailLoadInfo(false);
    	}
    	//Aurora <SQF> <2014-05-13>  for NEW_UI end
    }
 
    //Aurora <SQF> <2014-05-13>  for NEW_UI begin
    public boolean isLongScale() {
    	return mSlotView.isLongScale();
    }
    
    public boolean isSlotViewUserActionNotFinished(){
    	if(mSlotView == null) return false;
		return mSlotView.isSlotViewUserActionNotFinished();
	}
    
    public void notifyEnteringLeavingEffectState(int state) {
    	//Log.i("SQF_LOG", "notifyEnteringLeavingEffectState: " + state);
    	mEnteringOrLeavingEffectState = state;
    }
    
    public boolean isDrawingEnteringOrLeavingEffect() {
    	return mEnteringOrLeavingEffectState != AlbumPage.state.IDLE;
    	
    }
    //Aurora <SQF> <2014-05-13>  for NEW_UI end

    public void setSlotFilter(SlotFilter slotFilter) {
        mSlotFilter = slotFilter;
    }
}
