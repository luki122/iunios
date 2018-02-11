package com.aurora.mms.ui;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
//Aurora xuyong 2014-07-04 added for reject feature start
import android.os.Handler;
import android.os.Message;
//Aurora xuyong 2014-07-04 added for reject feature end
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.mms.util.GnSelectionManager;
import com.android.mms.util.GnSelectionManager.SelectionListener;
import aurora.widget.AuroraMenu;
// Aurora xuyong 2014-03-21 added for aurora's new feature start
import aurora.widget.AuroraActionBar;
// Aurora xuyong 2014-03-21 added for aurora's new feature end
import com.android.mms.R;

// Aurora liugj 2013-09-24 created for aurora's new feature
public abstract class AuroraActionBatchHandler<T> implements SelectionListener {
    // Aurora xuyong 2014-03-21 modified for aurora's new feature start
    private AuroraActionBar mActionBar;
    // Aurora xuyong 2014-03-21 modified for aurora's new feature end
    private Activity mActivity;
    private GnSelectionManager<T> mSelectionManager;
    private TextView mSelectAllBtn;
    private TextView mCancelBtn;
    
    private boolean mInSelectionMode;
    private boolean mAutoLeave = true;
    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    // Aurora xuyong 2014-07-16 added for reject feature start
    private boolean mIsFromReject = false;
    public void setIsFromReject (boolean isFromReject) {
        mIsFromReject = isFromReject;
    }
    // Aurora xuyong 2014-07-16 added for reject feature end
    // Aurora xuyong 2014-03-21 modified for aurora's new feature start
    public AuroraActionBatchHandler(Activity activity, AuroraActionBar actionBar) {
        mActivity = activity;
        mActionBar = actionBar;
    }
    // Aurora xuyong 2014-03-21 modified for aurora's new feature end
    
    public void enterSelectionMode(boolean autoLeave, T itemPressing) {
        if (null == mSelectionManager) {
            mSelectionManager = new GnSelectionManager<T>(mActivity);
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            mSelectionManager.setActionBar(mActionBar);
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
            bindToAdapter(mSelectionManager);
            mSelectionManager.setSelectionListener(this);
            mAutoLeave = autoLeave;
            refreshDataSet();
        }
        
        mInSelectionMode = true;
        initActionBarListener();
        if (null != mSelectionManager) {
            // Aurora liugj 2013-09-30 modified for aurora's new feature start
            if (null == itemPressing) {
                onSelectionModeChange(GnSelectionManager.DESELECT_ALL_MODE);
            }else {
                mSelectionManager.toggle(itemPressing);
            }
            // Aurora liugj 2013-09-30 modified for aurora's new feature end
        }
        
//        updateTitle();
    }
    // Aurora xuyong 2014-07-04 added for bug #6373 start
    private static boolean needHiddenAllBtn = false;
    public static void setHiddenAllBtn(boolean need) {
        needHiddenAllBtn = need;
    }
    // Aurora xuyong 2014-07-04 added for bug #6373 end
    // Aurora xuyong 2014-07-18 added for bug #6626 start
    private boolean isFromRejectActivity = false;
    public void setIsFromRejectActivity(boolean isFromReject) {
        isFromRejectActivity = isFromReject;
    }
    // Aurora xuyong 2014-07-18 added for bug #6626 end
   // Aurora xuyong 2014-07-22 added for bug #6735 start
    public void initActionBarOnlyCancelListener() {
        if (isFromRejectActivity) {
            mCancelBtn = (TextView) mActionBar.getCancelButton();
            mSelectAllBtn = (TextView) mActionBar.getOkButton();
            mSelectAllBtn.setVisibility(View.GONE);
        } else {
            mCancelBtn = (TextView) mActionBar.getSelectLeftButton();
            mSelectAllBtn = (TextView) mActionBar.getSelectRightButton();
        }
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFromReject) {
                    leaveSelectionModeWithActivityFinish(mActivity);
                } else {
                    leaveSelectionMode();
                }
            }
        });
    }
   // Aurora xuyong 2014-07-22 added for bug #6735 end
    private void initActionBarListener() {
        // Aurora xuyong 2014-03-21 modified for aurora's new feature start
       // Aurora xuyong 2014-07-18 modified for bug #6626 start
        if (isFromRejectActivity) {
            mCancelBtn = (TextView) mActionBar.getCancelButton();
            mSelectAllBtn = (TextView) mActionBar.getOkButton();
            mSelectAllBtn.setText(R.string.select_all);
        } else {
            mCancelBtn = (TextView) mActionBar.getSelectLeftButton();
            mSelectAllBtn = (TextView) mActionBar.getSelectRightButton();
        }
        // Aurora xuyong 2014-07-18 modified for bug #6626 end
        // Aurora xuyong 2014-03-21 modified for aurora's new feature end
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
             // Aurora xuyong 2014-07-16 added for reject feature start
                if (mIsFromReject) {
                    leaveSelectionModeWithActivityFinish(mActivity);
                } else {
                    leaveSelectionMode();
                }
             // Aurora xuyong 2014-07-16 added for reject feature end
            }
        });
       // Aurora xuyong 2014-07-04 added for bug #6373 start
        if (needHiddenAllBtn) {
            mSelectAllBtn.setVisibility(View.INVISIBLE);
        } else {
            mSelectAllBtn.setVisibility(View.VISIBLE);
        }
       // Aurora xuyong 2014-07-04 added for bug #6373 end
        mSelectAllBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isAllSelected = isAllSelected();

                // mSelectAllCheckBox.setChecked(isAllSelected);
                if (isAllSelected) {
                    mSelectAllBtn.setText(R.string.select_all);
                    deselectAll();
                //Aurora xuyong 2014-07-04 added for reject feature start
                    deSelectAllItems();
                //Aurora xuyong 2014-07-04 added for reject feature end
                    // Aurora liugj 2014-01-07 modified for allcheck animation start
                    updateListView(2);
                    // Aurora liugj 2014-01-07 modified for allcheck animation end
                } else {
                    mSelectAllBtn.setText(R.string.unselect_all);
                    selectAll();
                //Aurora xuyong 2014-07-04 added for reject feature start
                    selectAllItems();
                //Aurora xuyong 2014-07-04 added for reject feature end
                    // Aurora liugj 2014-01-07 modified for allcheck animation start
                    updateListView(1);
                    // Aurora liugj 2014-01-07 modified for allcheck animation end
                }
            }
        });
    }
    //Aurora xuyong 2014-07-04 added for reject feature start
    private Handler mHandler;
    
    public void setHandler(Handler handler) {
        mHandler = handler;
    }
    
    public void deSelectAllItems() {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(ConvFragment.DIS_SELECT_ALL);
            msg.sendToTarget();
        }
    }
    
    public void selectAllItems() {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(ConvFragment.SELECT_ALL);
            msg.sendToTarget();
        }
    }
    //Aurora xuyong 2014-07-04 added for reject feature end
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
    
    public GnSelectionManager<T> getSelectionManger() {
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
                    mSelectAllBtn.setText(R.string.unselect_all);
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
    
    public void setAutoLeave(boolean enable) {
        mAutoLeave = enable;
    }
    
    public boolean isAutoLeave() {
        return mAutoLeave;
    }
    
    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
        case GnSelectionManager.SELECT_ALL_MODE:
            if (null != mSelectAllBtn) {
                mSelectAllBtn.setText(R.string.unselect_all);
            }
            break;
        case GnSelectionManager.DESELECT_ALL_MODE:
            if (null != mSelectAllBtn) {
                mSelectAllBtn.setText(R.string.select_all);
            }
            /*if (mAutoLeave) {
                leaveSelectionMode();
                return;
            }*/
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
            if (mSelectAllBtn.getText().equals(mActivity.getString(R.string.unselect_all)) && !isAllSelected()) {
                mSelectAllBtn.setText(R.string.select_all);
            }
        }
    }
    
    public void destroyAction() {
        mInSelectionMode = false;
        bindToAdapter(null);
        mSelectionManager = null;
    }
    // Aurora xuyong 2014-07-16 added for reject feature start
    public void leaveSelectionModeWithActivityFinish(Activity activity) {
        leaveSelectionMode();
    }
    // Aurora xuyong 2014-07-16 added for reject feature end
    public abstract void updateUi();
    
    // Aurora liugj 2014-01-07 modified for allcheck animation start
    public abstract void updateListView(int allShow);
    // Aurora liugj 2014-01-07 modified for allcheck animation end

    public abstract void leaveSelectionMode();
    
    public abstract void bindToAdapter(GnSelectionManager<T> selectionManager);   
}
