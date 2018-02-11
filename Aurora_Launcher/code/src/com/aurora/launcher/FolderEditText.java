package com.aurora.launcher;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class FolderEditText extends EditText implements DropTarget{

    private Folder mFolder;
    private Launcher mLauncher;

    public FolderEditText(Context context) {
        super(context);
    }

    public FolderEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        Drawable d = getResources().getDrawable(R.drawable.folder_name_edit_bound_icon); 
        setCompoundDrawablesWithIntrinsicBounds(d, null, d, null);
    }

    public FolderEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFolder(Folder folder) {
        mFolder = folder;
    }

    public void setLauncher(Launcher launcher) {
    	mLauncher = launcher;
    }
    
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // Catch the back button on the soft keyboard so that we can just close the activity
        if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
            mFolder.doneEditingFolderName(true);
        }
        return super.onKeyPreIme(keyCode, event);
    }

	@Override
	public boolean isDropEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onDrop(DragObject d) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDragEnter(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragOver(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragExit(DragObject dragObject) {
		// TODO Auto-generated method stub
		
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
	public boolean acceptDrop(DragObject d) {
		// TODO Auto-generated method stub
		if(d.dragSource instanceof Workspace){
			mLauncher.getDragController().cancelFolderDelayCloseRunnable();
			mLauncher.closeFolder();
			
			d.deferDragViewCleanupPostAnimation = false;
			final View cell = mLauncher.getWorkspace().getDragInfo().cell;
			ItemInfo info = (ItemInfo) d.dragInfo;
			Workspace space = mLauncher.getWorkspace();
			
			if (cell.getParent() != null) {
				CellLayout layout = (CellLayout) cell.getParent().getParent();
				layout.markCellsAsOccupiedForView(cell);
				cell.setVisibility(View.VISIBLE);
			} else {
				if (space.ismAuroraSwapTag()) {
					space.resetHotseatChildToScreen(info, cell);
					space.setAuroraDragInfoVisible();
				} else {
					ItemInfo in = (ItemInfo)cell.getTag();
					int[] dragInfo = mLauncher.getWorkspace().getDragItemInfo();
					in.screen = dragInfo[0];
					in.cellX = dragInfo[1];
					in.cellY = dragInfo[2];;
					in.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
					cell.setTag(in);
					mLauncher.getWorkspace().addInScreen(cell, in.container, in.screen, in.cellX, in.cellY, in.spanX, in.spanY);
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
		} else if(d.dragSource instanceof Folder){
			mLauncher.getDragController().cancelFolderDelayCloseRunnable();
			if(d.dragSource != mFolder){
				mLauncher.closeFolder(mFolder, true, true);
			}
			d.dragView.remove();
			return false;
		}
		return true;
	}

	@Override
	public void getLocationInDragLayer(int[] loc) {
		// TODO Auto-generated method stub
		getLocationOnScreen(loc);
//		mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
	}
	
	@Override
	public void getHitRect(Rect outRect) {
//		outRect.set(0, 215, 1080, 290);
	//	outRect.set(0, 120, 1080, 360);
		outRect.set(mRect);
		Log.i("hitRect"," outRect ="+outRect);
	}
	private Rect mRect = new Rect();;
	public void setHitRect(int left,int top,int right,int bottom){
		mRect.set(left, top, right, bottom);
	}
}
