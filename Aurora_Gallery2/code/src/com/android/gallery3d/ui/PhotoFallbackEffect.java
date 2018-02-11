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

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.animation.OvershootInterpolator;//Aurora <paul> <2014-05-09>
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AlbumSlotRenderer.SlotFilter;

import java.util.ArrayList;

public class PhotoFallbackEffect extends Animation implements SlotFilter {

    private static final int ANIM_DURATION = 250;//Aurora <paul> <2014-02-27> for NEW_UI    300
    private static final Interpolator ANIM_INTERPOLATE = new DecelerateInterpolator(1.5f);
    private static final int ANIM_OPEN_DURATION = 350;//Aurora <paul> <2014-05-09>
	private static final Interpolator ANIM_OPEN_INTERPOLATE = new OvershootInterpolator(1.2f);//Aurora <paul> <2014-05-09>

    public static class Entry {
        public int index;
        public Path path;
        public Rect source;
        public Rect dest;
        public RawTexture texture;

        public Entry(Path path, Rect source, RawTexture texture) {
            this.path = path;
            this.source = source;
            this.texture = texture;
        }
    }

    public interface PositionProvider {
        public Rect getPosition(int index);
        public int getItemIndex(Path path);
    }

    private RectF mSource = new RectF();
    private RectF mTarget = new RectF();
    private float mProgress;
    private PositionProvider mPositionProvider;

    private ArrayList<Entry> mList = new ArrayList<Entry>();
	//Aurora <paul> <2014-02-28> for NEW_UI begin
	/*
    public PhotoFallbackEffect() {
        setDuration(ANIM_DURATION);
        setInterpolator(ANIM_INTERPOLATE);
    }
	*/
    public PhotoFallbackEffect(int width,int height) {
        setDuration(ANIM_DURATION);
        setInterpolator(ANIM_INTERPOLATE);
		mViewWidth = width;
		mViewHeight = height;
    }
	//Aurora <paul> <2014-02-28> for NEW_UI end
    public void addEntry(Path path, Rect rect, RawTexture texture) {
        mList.add(new Entry(path, rect, texture));
    }

    public Entry getEntry(Path path) {
        for (int i = 0, n = mList.size(); i < n; ++i) {
            Entry entry = mList.get(i);
            if (entry.path == path) return entry;
        }
        return null;
    }

    public boolean draw(GLCanvas canvas) {
        boolean more = calculate(AnimationTime.get());
        for (int i = 0, n = mList.size(); i < n; ++i) {
            Entry entry = mList.get(i);
            if (entry.index < 0) continue;
            entry.dest = mPositionProvider.getPosition(entry.index);
            drawEntry(canvas, entry);
        }
        return more;
    }

    private void drawEntry(GLCanvas canvas, Entry entry) {
        if (!entry.texture.isLoaded()) return;

        int w = entry.texture.getWidth();
        int h = entry.texture.getHeight();

        Rect s = entry.source;
        Rect d = entry.dest;

        // the following calculation is based on d.width() == d.height()

        float p = mProgress;

        float fullScale = (float) d.height() / Math.min(s.width(), s.height());
        float scale = fullScale * p + 1 * (1 - p);

        float cx = d.centerX() * p + s.centerX() * (1 - p);
        float cy = d.centerY() * p + s.centerY() * (1 - p);

        float ch = s.height() * scale;
        float cw = s.width() * scale;

		//Aurora <paul> <2014-02-27> for NEW_UI begin	
        if (w > h) {

			
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
			float al = 1 - p;
            canvas.multiplyAlpha(al < 0 ? 0 : (al > 1 ? 1 : al));

			canvas.fillRect(0.0f,0.0f,mViewWidth,mViewHeight,0xff0F0F0F);//original 0xff1A1A1A, SQF Modified to Black

			canvas.restore();
            // draw the left part
            //mTarget.set(cx - cw / 2, cy - ch / 2, cx - ch / 2, cy + ch / 2);
            //mSource.set(0, 0, (w - h) / 2, h);
            //p    1 -> 0
            float tWidth = (cw - ch) / 2;
			float sWidth = (w - h) / 2;
			
            mTarget.set(cx - cw / 2 + tWidth * p, cy - ch / 2, cx - ch / 2, cy + ch / 2);
            mSource.set(0 + sWidth * p, 0, (w - h) / 2, h);

            canvas.drawTexture(entry.texture, mSource, mTarget);

            // draw the right part
            mTarget.set(cx + ch / 2, cy - ch / 2, cx + ch / 2 + tWidth * (1 - p), cy + ch / 2);
            mSource.set((w + h) / 2, 0, (w + h) / 2 + sWidth * (1 - p), h);
            canvas.drawTexture(entry.texture, mSource, mTarget);

            

			// draw the center part
			mTarget.set(cx - ch / 2, cy - ch / 2, cx + ch / 2, cy + ch / 2);
			mSource.set((w - h) / 2, 0, (w + h) / 2, h);
			canvas.drawTexture(entry.texture, mSource, mTarget);

        } else {


            canvas.save(GLCanvas.SAVE_FLAG_ALPHA);

			float al = 1 - p;
            canvas.multiplyAlpha(al < 0 ? 0 : (al > 1 ? 1 : al));

			canvas.fillRect(0.0f,0.0f,mViewWidth,mViewHeight,0xff0F0F0F);//original 0xff1A1A1A, SQF Modified to Black);
			canvas.restore();
			
            // draw the upper part
            //mTarget.set(cx - cw / 2, cy - ch / 2, cx + cw / 2, cy - cw / 2);
            //mSource.set(0, 0, w, (h - w) / 2);
            //p    1 -> 0
            //float tTH = (cy - cw / 2  - (cy - ch / 2));
			//float sTH = ((h - w) / 2 - 0);
			float tHeight = (ch - cw) / 2;
			float sHeight = (h - w) / 2;
            mTarget.set(cx - cw / 2, cy - ch / 2 + tHeight * p, cx + cw / 2, cy - cw / 2);
            mSource.set(0, 0 + sHeight * p, w, (h - w) / 2);

            canvas.drawTexture(entry.texture, mSource, mTarget);

            // draw the bottom part
            //mTarget.set(cx - cw / 2, cy + cw / 2, cx + cw / 2, cy + ch / 2);
            //mSource.set(0, (w + h) / 2, w, h);
            //float tBH = (cy + ch / 2  - (cy + cw / 2));
			//float sBH = (h - (w + h) / 2);

            //mTarget.set(cx - cw / 2, cy + cw / 2, cx + cw / 2, cy + ch / 2);
            //mSource.set(0, (w + h) / 2, w, h);
            mTarget.set(cx - cw / 2, cy + cw / 2, cx + cw / 2, cy + cw / 2 + tHeight * (1 - p));
            mSource.set(0, (w + h) / 2, w, (w + h) / 2 + sHeight * (1 - p));

            canvas.drawTexture(entry.texture, mSource, mTarget);

            

			// draw the center part
			mTarget.set(cx - cw / 2, cy - cw / 2, cx + cw / 2, cy + cw / 2);
			mSource.set(0, (h - w) / 2, w, (h + w) / 2);
			canvas.drawTexture(entry.texture, mSource, mTarget);

        }
		/*
      if (w > h) {
            // draw the center part
            mTarget.set(cx - ch / 2, cy - ch / 2, cx + ch / 2, cy + ch / 2);
            mSource.set((w - h) / 2, 0, (w + h) / 2, h);
            canvas.drawTexture(entry.texture, mSource, mTarget);

            canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
            canvas.multiplyAlpha(1 - p);

            // draw the left part
            mTarget.set(cx - cw / 2, cy - ch / 2, cx - ch / 2, cy + ch / 2);
            mSource.set(0, 0, (w - h) / 2, h);
            canvas.drawTexture(entry.texture, mSource, mTarget);

            // draw the right part
            mTarget.set(cx + ch / 2, cy - ch / 2, cx + cw / 2, cy + ch / 2);
            mSource.set((w + h) / 2, 0, w, h);
            canvas.drawTexture(entry.texture, mSource, mTarget);

            canvas.restore();
        } else {
            // draw the center part
            mTarget.set(cx - cw / 2, cy - cw / 2, cx + cw / 2, cy + cw / 2);
            mSource.set(0, (h - w) / 2, w, (h + w) / 2);
            canvas.drawTexture(entry.texture, mSource, mTarget);

            canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
            canvas.multiplyAlpha(1 - p);

            // draw the upper part
            mTarget.set(cx - cw / 2, cy - ch / 2, cx + cw / 2, cy - cw / 2);
            mSource.set(0, 0, w, (h - w) / 2);
            canvas.drawTexture(entry.texture, mSource, mTarget);

            // draw the bottom part
            mTarget.set(cx - cw / 2, cy + cw / 2, cx + cw / 2, cy + ch / 2);
            mSource.set(0, (w + h) / 2, w, h);
            canvas.drawTexture(entry.texture, mSource, mTarget);

            canvas.restore();
        }
		*/
		//Aurora <paul> <2014-02-27> for NEW_UI end
    }

    @Override
    protected void onCalculate(float progress) {
    	//Aurora <paul> <2014-02-27> for NEW_UI begin
        if(mIsOpenningAnimation)
        	mProgress = 1 - progress;
		else
		//Aurora <paul> <2014-02-27> for NEW_UI end
        mProgress = progress;
    }

    public void setPositionProvider(PositionProvider provider) {
        mPositionProvider = provider;
        if (mPositionProvider != null) {
            for (int i = 0, n = mList.size(); i < n; ++i) {
                Entry entry = mList.get(i);
                entry.index = mPositionProvider.getItemIndex(entry.path);
            }
        }
    }

    @Override
    public boolean acceptSlot(int index) {
        for (int i = 0, n = mList.size(); i < n; ++i) {
            Entry entry = mList.get(i);
            if (entry.index == index) return false;
        }
        return true;
    }
	
	//Aurora <paul> <2014-02-27> for NEW_UI begin
	private boolean mIsOpenningAnimation = false;
	
	private int mViewWidth = 1080;
	private int mViewHeight = 1920;
	public void setToOpenningAnimation(){
		mIsOpenningAnimation = true;
		setDuration(ANIM_OPEN_DURATION);
		setInterpolator(ANIM_OPEN_INTERPOLATE);
	}

    public void setPositionProvider(PositionProvider provider,int index) {
        mPositionProvider = provider;
        if (mPositionProvider != null) {
            for (int i = 0, n = mList.size(); i < n; ++i) {
                Entry entry = mList.get(i);
                entry.index = index;
            }
        }
    }

	//Aurora <paul> <2014-02-27> for NEW_UI end
}
