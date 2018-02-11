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

package com.android.mms.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
// Aurora xuyong 2014-06-11 added for bug #5592 start
// Aurora xuyogn 2016-01-04 added for aurora's new feature start
import com.android.mms.R;
// Aurora xuyogn 2016-01-04 added for aurora's new feature end
import com.android.mms.ui.MessageCursorAdapter;
// Aurora xuyong 2014-06-11 added for bug #5592 end
import android.content.Context;
// Aurora xuyong 2016-01-19 added for aurora 2.0 new feature start
import android.widget.TextView;
// Aurora xuyong 2016-01-19 added for aurora 2.0 new feature end
// Aurora xuyogn 2016-01-04 added for aurora's new feature start
import aurora.widget.AuroraActionBar;
// Aurora xuyogn 2016-01-04 added for aurora's new feature end
public class GnSelectionManager<T> {

    private Set<T> mSelectedSet;
    // Aurora xuyogn 2016-01-04 added for aurora's new feature start
    private Context mContext;
    private AuroraActionBar mActionBar;
    // Aurora xuyogn 2016-01-04 added for aurora's new feature end
    private Set<T> mDataSet;
    private SelectionListener mListener;
    private Integer mDataSetSize;
    // Aurora xuyong 2014-06-11 added for bug #5592 start
    private MessageCursorAdapter mAdapter;
    // Aurora xuyong 2014-06-11 added for bug #5592 end
    public static final int SELECT_ALL_MODE = 1;
    public static final int DESELECT_ALL_MODE = 2;

    public interface SelectionListener {
        public void onSelectionModeChange(int mode);
        public void onSelectionChange(Object item, boolean selected);
        public Set getDataSet();
    }

    public GnSelectionManager(Context context) {
        // Aurora xuyogn 2016-01-04 added for aurora's new feature start
        mContext = context;
        // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        mSelectedSet = new HashSet<T>();
    }
    // Aurora xuyogn 2016-01-04 added for aurora's new feature start
    public void setActionBar(AuroraActionBar actionBar) {
        mActionBar = actionBar;
    }
    // Aurora xuyogn 2016-01-04 added for aurora's new feature end
    // Aurora xuyong 2014-06-11 added for bug #5592 start
    public void setAdapter(MessageCursorAdapter adpter) {
        mAdapter = adpter;
    }
    // Aurora xuyong 2014-06-11 added for bug #5592 end
    private void checkDataSetReady() {
        if (null == mDataSet) {
            throw new IllegalAccessError("setDataSet first");
        }    
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    public void setDataSet(Set<T> dataAll) {
        mDataSet = dataAll;
        mDataSetSize = (null == mDataSet ? null : mDataSet.size()); 
    }
    
    public Integer getSelectableSize() {
        return mDataSetSize;
    }
    
    public void setDataSetIfEmpty(Set<T> dataAll) {
        if (null == mDataSet) {
            setDataSet(dataAll);
        }
    }
    // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature start
    private void updateMiddleText(int selectedNum) {
        TextView middleTextView = mActionBar.getMiddleTextView();
        if (selectedNum == 0) {
            middleTextView.setText(mContext.getResources().getString(R.string.noItemSelected));
        } else {
            middleTextView.setText(mContext.getResources().getQuantityString(R.plurals.numberOfItemSelected, selectedNum, selectedNum));
        }
    }
    // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature end
    public void selectAll() {
        checkDataSetReady();
        
        mSelectedSet.addAll(mDataSet);
        // Aurora xuyogn 2016-01-04 added for aurora's new feature start
        // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
        updateMiddleText(mSelectedSet.size());
        // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
        // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        if (mListener != null) {
            mListener.onSelectionModeChange(SELECT_ALL_MODE);
        }
    }

    public void deseletcAll() {
        mSelectedSet.clear();
        // Aurora xuyogn 2016-01-04 added for aurora's new feature start
        // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
        updateMiddleText(mSelectedSet.size());
        // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
        // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        if (mListener != null) {
            mListener.onSelectionModeChange(DESELECT_ALL_MODE);
        }
    }
    
    public void toggle(T item) {
        if (null == item) {
            return;
        }
        
        if (mSelectedSet.contains(item)) {
            mSelectedSet.remove(item);
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
            updateMiddleText(mSelectedSet.size());
            // Aurora xuyong 2016-01-19 modfieid for aurora 2.0 new feature end
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        } else {            
            mSelectedSet.add(item);
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
            updateMiddleText(mSelectedSet.size());
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        }
        // Aurora xuyong 2014-06-11 added for bug #5592 start
        if (mAdapter != null) {
            if (mSelectedSet.size() == 0) {
                mAdapter.updateAllCheckBox(2);
            } else if (mSelectedSet.size() == mAdapter.getCount()) {
                mAdapter.updateAllCheckBox(1);
            } else {
                mAdapter.updateAllCheckBox(0);
            }
        }
        // Aurora xuyong 2014-06-11 added for bug #5592 end
        notifySelectionChange(item);
    }
    
    private void notifySelectionChange(T item) {
        if (mListener != null) {            
            mListener.onSelectionChange(item, isSelected(item));
            
            if (mSelectedSet.isEmpty()) {
                mListener.onSelectionModeChange(DESELECT_ALL_MODE);
            } else if (isAllSelected()) {
                mListener.onSelectionModeChange(SELECT_ALL_MODE);
            }
        }
    }

    public boolean isSelected(T item) {
        return mSelectedSet.contains(item);
    }

    public int getSelectedCount() {
        return mSelectedSet.size();        
    }

    public ArrayList<T> getSelected() {
        ArrayList<T> selected = new ArrayList<T>();
        for (T id : mSelectedSet) {
            selected.add(id);
        }
        return selected;
    }
    
    public boolean isDataSetReady() {
        return null != mDataSet;
    }
    
    public boolean isAllSelected() {
        checkDataSetReady();
        
        if (null != mDataSet && null != mSelectedSet) {
            return mDataSet.size() == mSelectedSet.size();
        }
        
        return false;
    }
}
