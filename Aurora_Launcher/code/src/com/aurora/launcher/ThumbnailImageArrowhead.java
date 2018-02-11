package com.aurora.launcher;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class ThumbnailImageArrowhead extends ImageView implements DropTarget{
	
	public static final String TANGJUN = "tangjun";
	
	private Launcher mLauncher;
	
	public static final long DELAYTIME = 1000;
	
	private ArrowHeadScrollRunnable mArrowHeadScrollRunnable = new ArrowHeadScrollRunnable();
	
	public Launcher getLauncher() {
		return mLauncher;
	}

	public void setLauncher(Launcher mLauncher) {
		this.mLauncher = mLauncher;
	}

	public ThumbnailImageArrowhead(Context context) {
		super(context);
	}

	public ThumbnailImageArrowhead(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    private class ArrowHeadScrollRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//Log.e(TANGJUN, "----setPreviewBarScrollTo");
			setPreviewBarScrollTo( );
		}
    }
    
    /**
     * setPreviewBarScrollTo
     */
    private void setPreviewBarScrollTo() {
		if ( ThumbnailImageArrowhead.this.getId() == R.id.leftarrowhead ) {
			//Toast.makeText(mLauncher, "click leftarrowhead", Toast.LENGTH_SHORT).show();
			mLauncher.smoothPreviewBarScroll(0);

		} else {
			//Toast.makeText(mLauncher, "click rightarrowhead", Toast.LENGTH_SHORT).show();
			mLauncher.smoothPreviewBarScroll(1);
		}
		
		mLauncher.setPageIndicatotorTextViewAlpha(1f);
		
		if ( this.getVisibility() == View.VISIBLE ) {
			postDelayed(mArrowHeadScrollRunnable, DELAYTIME);
		}
    }

	@Override
	public boolean isDropEnabled() {
		// TODO Auto-generated method stub
		if ( this.getVisibility() != View.VISIBLE ) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onDrop(DragObject dragObject) {
		// TODO Auto-generated method stub
		removeCallbacks(mArrowHeadScrollRunnable);
	}

	@Override
	public void onDragEnter(DragObject dragObject) {
		// TODO Auto-generated method stub
		setPreviewBarScrollTo ( );
	}

	@Override
	public void onDragOver(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragExit(DragObject dragObject) {
		// TODO Auto-generated method stub
		removeCallbacks(mArrowHeadScrollRunnable);
	}

	@Override
	public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DropTarget getDropTargetDelegate(DragObject dragObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean acceptDrop(DragObject dragObject) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void getHitRect(Rect outRect) {
		// TODO Auto-generated method stub
		//Log.e(TANGJUN, "getHitRect----");
		super.getHitRect(outRect);
	}

	@Override
	public void getLocationInDragLayer(int[] loc) {
		// TODO Auto-generated method stub
		getLocationOnScreen(loc);
		//mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
	}

}
