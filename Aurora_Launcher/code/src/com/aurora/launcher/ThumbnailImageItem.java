package com.aurora.launcher;

import com.aurora.launcher.Launcher.EditMode;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ThumbnailImageItem extends TextView implements DropTarget {
	
	public static final String TANGJUN = "tangjun";
	
	public static final long DELAYTIME = 150;
	
	public static final long SHOWPAGEDELAYTIME = 500;
	
	private WorkspaceScrollRunnable mWorkspaceScrollRunnable = new WorkspaceScrollRunnable();
	
	private Launcher mLauncher;

	public Launcher getLauncher() {
		return mLauncher;
	}

	public void setLauncher(Launcher mLauncher) {
		this.mLauncher = mLauncher;
	}

	public ThumbnailImageItem(Context context) {
		super(context);
	}

	public ThumbnailImageItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    private class WorkspaceScrollRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//ViewGroup parent = (ViewGroup) getParent();
			//int position = parent.indexOfChild(ThumbnailImageItem.this);
			int position = mLauncher.getPreviewContent().indexOfChild(ThumbnailImageItem.this);
			//Log.e(TANGJUN, "position = " + position);
			//mLauncher.getWorkspace().snapToPage(position, 1000);
			mLauncher.getWorkspace().snapToPage(position % mLauncher.getWorkspace().getChildCount());
//AURORA-START::Fix bug #985::Shi guiqiang::20131122
			if (mLauncher.getEditMode() != EditMode.APPWIDGET_ADD) {
				mLauncher.setPageIndicatotorTextViewAlpha(1f);
			}
//AURORA-END::Fix bug #985::Shi guiqiang::20131122
		}

    }

	@Override
	public boolean isDropEnabled() {
		// TODO Auto-generated method stub
		//Log.e(TANGJUN, "isDropEnabled----");
		if ( mLauncher.getHorizontalScrollView().getVisibility() != View.VISIBLE ) {
			return false;
		}
		ViewGroup parent = (ViewGroup) getParent();
		int position = parent.indexOfChild(ThumbnailImageItem.this);
		if ( mLauncher.getHorizontalScrollView().getLeftpage() > position ||
				mLauncher.getHorizontalScrollView().getLeftpage() + ThumbnailImage.COUNTINONEPAGE <= position ) {
			//Log.e(TANGJUN, "isDropEnabled----false position = " + position);
			return false;
		} else {
			//Log.e(TANGJUN, "isDropEnabled----true position = " + position);
			return true;
		}
	}

	// Aurora <haojj> <2013-10-22> add for 从folder出来新增一些时延 begin
	private boolean mDelayWorkspaceScroll = false;
	public static final long FORCE_DELAY_TIME = 1100;
	public void forceDelayWorkspaceScroll(){
		mDelayWorkspaceScroll = true;
	}
	// Aurora <haojj> <2013-10-22> end
	
	@Override
	public void onDragEnter(DragObject dragObject) {
		//Log.e(TANGJUN, "onDragEnter----" + this.toString());
		// Aurora <haojj> <2013-10-22> add for 从folder出来新增一些时延 begin
		// postDelayed(mWorkspaceScrollRunnable, DELAYTIME);
		if(mDelayWorkspaceScroll){
			mDelayWorkspaceScroll = false;
			postDelayed(mWorkspaceScrollRunnable, FORCE_DELAY_TIME);
		} else {
			postDelayed(mWorkspaceScrollRunnable, DELAYTIME);
		}
		// Aurora <haojj> <2013-10-22> end
	}

	@Override
	public void onDragOver(DragObject dragObject) {
		// TODO Auto-generated method stub
		//Log.e(TANGJUN, "onDragOver----");
	}

	@Override
	public void onDragExit(DragObject dragObject) {
		// TODO Auto-generated method stub
		//Log.e(TANGJUN, "onDragExit----" + this.toString());
		removeCallbacks(mWorkspaceScrollRunnable);
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
//AURORA-START::Fix bug #937::Shi guiqiang::20131120
		if (mLauncher.getEditMode() == EditMode.APPWIDGET_ADD) {
			/*Toast mWidgetInstructionToast = null;
			if (mWidgetInstructionToast != null) {
                mWidgetInstructionToast.cancel();
            }
            mWidgetInstructionToast = Toast.makeText(getContext(),R.string.no_space,
                Toast.LENGTH_LONG);
            mWidgetInstructionToast.show();*/
            dragObject.cancelled = true;
            dragObject.deferDragViewCleanupPostAnimation =false;
			return false;
		}
//AURORA-END::Fix bug #937::Shi guiqiang::20131120
		Log.e(TANGJUN, "acceptDrop----");
		dragObject.deferDragViewCleanupPostAnimation = false;
		
		int page = mLauncher.getWorkspace().getCurrentPage();
		CellLayout cellLayout = (CellLayout) mLauncher.getWorkspace().getChildAt(page);
		ItemInfo info = (ItemInfo) dragObject.dragInfo;
		final View cell = mLauncher.getWorkspace().getDragInfo().cell;
		int spanX = info.spanX;
		int spanY = info.spanY;
		int[] empty = { -1, -1 };
		
		Log.e(TANGJUN, "---spanX = " + spanX + ", spanY = " + spanY);
		
		cellLayout.findEmpytCell(empty,spanX,spanY);
		
		Workspace space = mLauncher.getWorkspace();
		if(empty[0] == -1 || empty[1] == -1){
			if (cell.getParent() != null) {
				if (space.ismAuroraSwapTag()
						|| (!space.ismAuroraSwapTag() && info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP)) {
					CellLayout layout = (CellLayout) cell.getParent().getParent();
					layout.markCellsAsOccupiedForView(cell);
					cell.setVisibility(View.VISIBLE);
				} else if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
					CellLayout parentLayout = mLauncher.getHotseat().getLayout();
					ShortcutAndWidgetContainer sac = parentLayout.getShortcutsAndWidgets();
					space.updateAuroraMemoryParams(info, parentLayout, sac);
					space.updateHotseatData();
					space.resetAuroraHotseatData();
					
					ItemInfo in = (ItemInfo) cell.getTag();
					int[] dragInfo = mLauncher.getWorkspace().getDragItemInfo();
					in.screen = dragInfo[0];
					in.cellX = dragInfo[1];
					in.cellY = dragInfo[2];;
					in.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
					cell.setTag(in);
					mLauncher.getWorkspace().addInScreen(cell, in.container, in.screen, in.cellX, in.cellY, spanX, spanY);
					cell.setVisibility(View.VISIBLE);
					
			        // update the item's position after drop
			        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
			        lp.cellX = lp.tmpCellX = dragInfo[1];
			        lp.cellY = lp.tmpCellY = dragInfo[2];
			        lp.cellHSpan = info.spanX;
			        lp.cellVSpan = info.spanY;
			        lp.isLockedToGrid = true;
					LauncherModel.addOrMoveItemInDatabase(mLauncher, info, in.container, in.screen,
			                lp.cellX, lp.cellY);
				}
			} else {
				if (space.ismAuroraSwapTag()) {
					space.resetHotseatChildToScreen(info, cell);
					space.updateHotseatData();
					space.setAuroraDragInfoVisible();
				} else if (dragObject.dragSource instanceof Workspace) {
					ItemInfo in = (ItemInfo)cell.getTag();
					int[] dragInfo = mLauncher.getWorkspace().getDragItemInfo();
					in.screen = dragInfo[0];
					in.cellX = dragInfo[1];
					in.cellY = dragInfo[2];;
					in.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
					cell.setTag(in);
					mLauncher.getWorkspace().addInScreen(cell, in.container, in.screen, in.cellX, in.cellY, spanX, spanY);
					cell.setVisibility(View.VISIBLE);
					
			        // update the item's position after drop
			        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
			        lp.cellX = lp.tmpCellX = dragInfo[1];
			        lp.cellY = lp.tmpCellY = dragInfo[2];
			        lp.cellHSpan = info.spanX;
			        lp.cellVSpan = info.spanY;
			        lp.isLockedToGrid = true;
			        
					LauncherModel.addOrMoveItemInDatabase(mLauncher, info, in.container, in.screen,
			                lp.cellX, lp.cellY);
				}
			}
			mLauncher.setPageIndicatotorTextViewAlpha(0f);
			return false;
		}
		
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
		//Log.e(TANGJUN, "getLocationInDragLayer----");
		getLocationOnScreen(loc);
		//mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
		//Log.e(TANGJUN, "loc = " + loc[0] + ", " + loc [1]);

	}

	@Override
	public void onDrop(DragObject dragObject) {
		Log.e(TANGJUN, "onDrop----");
		
		removeCallbacks(mWorkspaceScrollRunnable);
		
		mLauncher.setPageIndicatotorTextViewAlpha(0f);
        /*
        ViewGroup.LayoutParams params = getLayoutParams();
    	//we need to increase the statusbar height  when  exiting fullsreen
    	if (params instanceof FrameLayout.LayoutParams) {
    		((FrameLayout.LayoutParams) params).topMargin = 0;
    	}
		mLauncher.onExitFullScreen();
		mLauncher.exitEditMode(true);
		*/
		ItemInfo info = (ItemInfo) dragObject.dragInfo;
		/*
		if (info.container != LauncherSettings.Favorites.CONTAINER_DESKTOP
				&& info.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mLauncher.exitEditMode(true);
				}
			}, 500);
		}
		*/
        
		int page = mLauncher.getWorkspace().getCurrentPage();
		CellLayout cellLayout = (CellLayout) mLauncher.getWorkspace().getChildAt(page);
//		final View cell = mLauncher.createShortcut(R.layout.application, cellLayout,
//                (ShortcutInfo) info);
		final View cell = mLauncher.getWorkspace().getDragInfo().cell;
		long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		int spanX = info.spanX;
		int spanY = info.spanY;
		int[] empty = { -1, -1 };
		
		Log.e(TANGJUN, "---spanX = " + spanX + ", spanY = " + spanY);
		
		cellLayout.findEmpytCell(empty,spanX,spanY);
		
		// Aurora <jialf> <2013-10-08> modify for Dock data begin
		Workspace space = mLauncher.getWorkspace();
		if(empty[0] == -1 || empty[1] == -1){
			//Toast.makeText(getContext(), "该页已没空间了", Toast.LENGTH_SHORT).show();
			if (cell.getParent() != null) {
				CellLayout layout = (CellLayout) cell.getParent().getParent();
				layout.markCellsAsOccupiedForView(cell);
				cell.setVisibility(View.VISIBLE);
			}
		}else{
			Log.e(TANGJUN, "---empty[0] = " + empty[0] + ", empty[1] = " + empty[1]);
			// Reparent the view
    		// Aurora <jialf> <2013-10-22> modify for fix bug #41 begin	
			final CellLayout parentLayout = space.getParentCellLayoutForView(cell);
			// Aurora <jialf> <2013-10-31> modify for fix bug #313 begin
			if (space.ismAuroraSwapTag()
					|| info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			// Aurora <jialf> <2013-10-31> modify for fix bug #313 end
				if (parentLayout != null) {
					CellLayout layout = mLauncher.getHotseat().getLayout();
					ShortcutAndWidgetContainer sac = layout.getShortcutsAndWidgets();
					space.updateAuroraMemoryParams(info, sac);
					parentLayout.removeView(cell);
					layout.setAuroraCellX(false);
					layout.setAuroraGridSize(-1);
				}
				space.updateHotseatData();
				space.resetAuroraHotseatData();
			} else {
				if (parentLayout != null)
					parentLayout.removeView(cell);
			}  
    		// Aurora <jialf> <2013-10-22> modify for fix bug #41 end
			ItemInfo in = (ItemInfo)cell.getTag();
			in.cellX = empty[0];
			in.cellY = empty[1];
			in.screen = page;
			in.container = container;
			cell.setTag(in);
			// page = mLauncher.getPreviewContent().indexOfChild(ThumbnailImageItem.this);
			/*begin add by xiangzx for bug 16470 in 2015/09/22 */
			cell.setOnClickListener(mLauncher);
			/*end add by xiangzx for bug 16470 in 2015/09/22 */
			mLauncher.getWorkspace().addInScreen(cell, container, page, empty[0], empty[1], spanX, spanY);
			cell.setVisibility(View.VISIBLE);
			
	        // update the item's position after drop
	        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
	        lp.cellX = lp.tmpCellX = empty[0];
	        lp.cellY = lp.tmpCellY = empty[1];
	        lp.cellHSpan = info.spanX;
	        lp.cellVSpan = info.spanY;
	        lp.isLockedToGrid = true;
	        
			LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, page,
	                lp.cellX, lp.cellY);
		}
		// Aurora <jialf> <2013-10-08> modify for Dock data end
		
//		final CellLayout parent = (CellLayout) cell.getParent().getParent();
//		parent.onDropChild(cell);
		
	}

}