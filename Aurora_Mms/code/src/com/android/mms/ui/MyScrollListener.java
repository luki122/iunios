package com.android.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.CursorAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.HeaderViewListAdapter;

import com.aurora.mms.util.Utils;

public class MyScrollListener implements OnScrollListener {
    private static String TAG = "Mms/ScrollListener";
    private int HANDLE_FLING_THREAD_WAIT_TIME = 200;
    private String mThreadName = "ConversationList_Scroll_Tread";
    private int mMinCursorCount = 100;
    private Thread myThread = null;
    private boolean mNeedDestroy = false;
    private MyRunnable runnable = new MyRunnable(true);
     // Aurora liugj 2013-10-19 added for aurora's new feature start
    private Activity mActivity;
     // Aurora liugj 2013-10-19 added for aurora's new feature end

    public MyScrollListener (int minCursorCount, String threadName) {
         // Aurora liugj 2013-10-19 added for aurora's new feature start
         mActivity = null;
         // Aurora liugj 2013-10-19 added for aurora's new feature end
        mMinCursorCount = minCursorCount;
        mThreadName = threadName;
    }

     // Aurora liugj 2013-10-19 modified for aurora's new feature start    
    public MyScrollListener (Activity activity, int minCursorCount, String threadName) {
        mActivity = activity;
        mMinCursorCount = minCursorCount;
        mThreadName = threadName;
    }
    // Aurora liugj 2013-10-19 modified for aurora's new feature end

    public void destroyThread() {
        synchronized (runnable) {
            Log.d(TAG, "destroy thread.");
            mNeedDestroy = true;
            runnable.setNeedRun(false);
            runnable.notifyAll();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Aurora liugj 2013-10-19 added for aurora's new feature start
        if (mActivity != null) {
            Utils.hideInputMethod(mActivity);
        }
        // Aurora liugj 2013-10-19 added for aurora's new feature end
        MessageCursorAdapter mlistAdapter = null;
          // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview start
        if (mThreadName.equals("ConversationList_Scroll_Tread")) {
          // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview end
            HeaderViewListAdapter headlistadapter = (HeaderViewListAdapter) view.getAdapter();
            mlistAdapter = (MessageCursorAdapter) (headlistadapter.getWrappedAdapter());
        } else {
            mlistAdapter = (MessageCursorAdapter) view.getAdapter();
        }
     // Aurora liugj 2014-01-16 modified for allcheck animation start
        if (mlistAdapter != null) {
            if (mlistAdapter.mNeedAnim) {
                mlistAdapter.setCheckBoxAnim(false);
            }
            if (mlistAdapter.mAllShowCheckBox != 0) {
                mlistAdapter.updateAllCheckBox(0);
            }
        }
        // Aurora liugj 2014-01-16 modified for allcheck animation end
        if (mlistAdapter != null && mlistAdapter.getCount() >= mMinCursorCount) {// run below code when threads' count more than 100.
            if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {// on touch
                Log.d(TAG, "OnScrollListener.onScrollStateChanged(): on touch state.");
                mlistAdapter.setIsScrolling(false);
            } else if (scrollState == OnScrollListener.SCROLL_STATE_FLING) { // scrolling
                Log.d(TAG, "OnScrollListener.onScrollStateChanged(): scrolling...");
                mlistAdapter.setIsScrolling(true);
                synchronized (runnable) {
                    runnable.notifyAll();
                    runnable.setConversationListAdapter(mlistAdapter);
                    runnable.setNeedRun(true);
                }
                if (myThread == null) {
                    myThread = new Thread(runnable, mThreadName);
                    myThread.start();
                }
            } else if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {// not in scrolling
                Log.d(TAG, "OnScrollListener.onScrollStateChanged(): stop scrolling!");
                if (myThread != null) {
                    synchronized (runnable) {
                        runnable.setNeedRun(false);
                    }
                }
                mlistAdapter.setIsScrolling(false);
                mlistAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    private class MyRunnable implements Runnable {
        private boolean mNeedRun = true;
        MessageCursorAdapter mListAdapter = null;
        private int count = 0;
        private int bindViewTimes = 3;
        private int bindDefaultViewTimes = bindViewTimes - 1;

        public MyRunnable(boolean needRun) {
            mNeedRun = needRun;
        }

        public void setNeedRun(boolean needRun) {
            mNeedRun = needRun;
            if (!mNeedRun) {
                count = 0;
            }
        }
        public void setConversationListAdapter(MessageCursorAdapter listAdapter) {
            mListAdapter = listAdapter;
        }

        @Override
        public void run() {
            Object obj = new Object();
            while (!mNeedDestroy) {
                while(mNeedRun) {
                    Log.d(TAG, "OnScrollListener.run(): count=" + count);
                    /*
                     * percent of true: (bindViewTimes - 1) / bindViewTimes;
                     * percent of false: 1 / bindViewTimes.
                     */
                    if (count % bindViewTimes != bindDefaultViewTimes) {
                        mListAdapter.setIsScrolling(true);
                    } else {
                        mListAdapter.setIsScrolling(false);
                    }
                    count++;
                    if (mNeedDestroy) {
                        return;
                    }
                    synchronized (obj) {
                        try {
                            obj.wait(HANDLE_FLING_THREAD_WAIT_TIME);
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                    }
                }
                if (mNeedDestroy) {
                    return;
                }
                Log.d(TAG, "OnScrollListener.run(): listener is wait.");
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                }
            }
        }
    }
}
