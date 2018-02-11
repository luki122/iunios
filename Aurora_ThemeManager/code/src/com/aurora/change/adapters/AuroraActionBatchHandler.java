package com.aurora.change.adapters;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.aurora.thememanager.R;
import aurora.widget.AuroraActionBar;
import com.aurora.change.adapters.SelectionManager.SelectionListener;

// Aurora liugj 2014-03-06 created for wallpaper batch delete
public abstract class AuroraActionBatchHandler<T> implements SelectionListener {
	
	private AuroraActionBar mActionBar;
	private Activity mActivity;
	private SelectionManager<T> mSelectionManager;
	private TextView mSelectAllBtn;
    private TextView mCancelBtn;
	
	private boolean mInSelectionMode;
    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
	
	public AuroraActionBatchHandler(Activity activity, AuroraActionBar actionBar) {
		mActivity = activity;
		mActionBar = actionBar;
	}
	
	public void enterSelectionMode(T itemPressing) {
        if (null == mSelectionManager) {
            mSelectionManager = new SelectionManager<T>(mActivity);
            bindToAdapter(mSelectionManager);
            mSelectionManager.setSelectionListener(this);
            refreshDataSet();
        }
        
        mInSelectionMode = true;
		initActionBarListener();
        if (null != mSelectionManager) {
        	if (null == itemPressing) {
        		onSelectionModeChange(SelectionManager.DESELECT_ALL_MODE);
			}else {
				mSelectionManager.toggle(itemPressing);
			}
		}
		
//        updateTitle();
    }
	
	private void initActionBarListener() {
		mCancelBtn = (TextView) mActionBar.getSelectLeftButton();
		mCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mActionBar.auroraIsEntryEditModeAnimRunning()) {
					leaveSelectionMode();
				}
			}
		});
		mSelectAllBtn = (TextView) mActionBar.getSelectRightButton();
		mSelectAllBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isAllSelected = isAllSelected();

				// mSelectAllCheckBox.setChecked(isAllSelected);
				if (isAllSelected) {
					mSelectAllBtn.setText(R.string.select_all);
					deselectAll();
				} else {
					mSelectAllBtn.setText(R.string.no_select_all);
					selectAll();
				}
			}
		});
	}
	
	public void toggle(T item) {
    	if (null != mSelectionManager) {
    		mSelectionManager.toggle(item);
    	}
    }
	
	public void earseDataSet() {
    	if (null != mSelectionManager) {
            mSelectionManager.setDataSet(null);
        }
    }
	
	public SelectionManager<T> getSelectionManger() {
        return mSelectionManager;
    }
    
    public ArrayList<T> getSelected() {
        if (null == mSelectionManager) {
            return null;
        }
        return mSelectionManager.getSelected();
    }
    
	public void selectAll() {
    	refreshDataSetIfEmpty();
    	if (null != mSelectionManager) {
    		mSelectionManager.selectAll();
    	}
    }
    
    public void deselectAll() {
    	if (null != mSelectionManager) {
    		mSelectionManager.deseletcAll();
    	}
    }
    
    public boolean isAllSelected() {
    	if (null != mSelectionManager) {
    		return mSelectionManager.isAllSelected();
    	}
    	
    	return false;
    }
    
    public boolean refreshDataSet() {
        if (null != mSelectionManager) {
            mSelectionManager.setDataSet(getDataSet());
            return true;
        }
        
        return false;
    }
    
    public boolean refreshDataSetIfEmpty() {
        if (null != mSelectionManager && !mSelectionManager.isDataSetReady()) {
            mSelectionManager.setDataSet(getDataSet());
            
            if (null != mSelectAllBtn) {
				if (isAllSelected()) {
					mSelectAllBtn.setText(R.string.no_select_all);
				} else {
					mSelectAllBtn.setText(R.string.select_all);
				}
			}
            return true;
        }
        
        return false;
    }
    
    public int getSelectedCount() {
    	int count = 0;
    	if (null != mSelectionManager) {
    		count = mSelectionManager.getSelectedCount();
    	}
    	
    	return count;
	}
    
    public boolean isInSelectionMode() {
        return mInSelectionMode;
    }
    
	@Override
	public void onSelectionModeChange(int mode) {
		switch (mode) {
        case SelectionManager.SELECT_ALL_MODE:
        	if (null != mSelectAllBtn) {
        		mSelectAllBtn.setText(R.string.no_select_all);
			}
            break;
        case SelectionManager.DESELECT_ALL_MODE:
        	if (null != mSelectAllBtn) {
        		mSelectAllBtn.setText(R.string.select_all);
			}
            break;

        default:
            break;
        }
        
		updateUi();
	}

	@Override
	public void onSelectionChange(Object item, boolean selected) {
		updateUi();
    	
    	if (null != mSelectAllBtn) {
    		if (mSelectAllBtn.getText().equals(mActivity.getString(R.string.no_select_all)) && !isAllSelected()) {
    			mSelectAllBtn.setText(R.string.select_all);
    		}
		}
	}
	
	public void destroyAction() {
		mInSelectionMode = false;
    	bindToAdapter(null);
    	mSelectionManager = null;
	}
	
	public abstract void updateUi();
	
	public abstract void leaveSelectionMode();
	
	public abstract void bindToAdapter(SelectionManager<T> selectionManager);   
}
