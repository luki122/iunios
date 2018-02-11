/**
 * @name:GNLayout.java
 * @author:wangpf
 * @see:This class in order to achieve upward pop-up animation
 * @createdate：2013-04-11
 */
package gn.com.android.update.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import gn.com.android.update.R;
import gn.com.android.update.utils.LogUtils;

public class GNLayout extends RelativeLayout {

    private final static int MOVE_HEIGHT = 18;
    private final static int MOVE_ID = 0x001;
    private final static String TAG = "GNLayout";
    private LinearLayout mCheckLayout;
    private LinearLayout mDownLoadLayout;
    private android.widget.RelativeLayout.LayoutParams mCheckParams;
    private android.widget.RelativeLayout.LayoutParams mDownLoadParams;
    private int mHeight;
    private boolean mIsInit = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MOVE_ID:
                    // Gionee <wangpf> <2013-04-27> modify for CR00803212 begin
                    int speed = getResources().getInteger(R.integer.new_version_popup_speed);
                    moveView(speed);
                    // Gionee <wangpf> <2013-04-27> modify for CR00803212 end
                    break;
                default:
                    break;
            }
        };
    };

    public GNLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCheckLayout = (LinearLayout) getRootView().findViewById(R.id.check_info_layout);
        mDownLoadLayout = (LinearLayout) getRootView().findViewById(R.id.download_info_layout);
    }

    public synchronized void initView() {
        if (mIsInit) {
            return;
        }
        mCheckParams = (android.widget.RelativeLayout.LayoutParams) mCheckLayout.getLayoutParams();
        mDownLoadParams = (android.widget.RelativeLayout.LayoutParams) mDownLoadLayout.getLayoutParams();
        mHeight = getResources().getDrawable(R.drawable.check_update_background).getIntrinsicHeight();
        LogUtils.logd(TAG, "   height = " + mHeight);
        mCheckParams.height = mHeight;
        mDownLoadParams.height = mHeight;
        mCheckLayout.setLayoutParams(mCheckParams);
        mDownLoadLayout.setLayoutParams(mDownLoadParams);
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) getLayoutParams();
        params.height = mHeight;
        setLayoutParams(params);
        mIsInit = true;
    }

    public void showCheckLayout() {
        mCheckLayout.setVisibility(View.VISIBLE);
        mDownLoadLayout.setVisibility(View.INVISIBLE);
    }

    public void showDownLoadLayout() {
        mCheckLayout.setVisibility(View.VISIBLE);
        mDownLoadLayout.setVisibility(View.VISIBLE);
        LogUtils.logd(TAG, "show Height = " + mHeight);
        mDownLoadParams.topMargin = mHeight;
        mDownLoadLayout.setLayoutParams(mDownLoadParams);
        mHandler.sendEmptyMessage(MOVE_ID);
    }

    public void showDownLoadLayoutWithoutAnim() {
        mCheckLayout.setVisibility(View.INVISIBLE);
        mDownLoadLayout.setVisibility(View.VISIBLE);
    }
    
    public boolean isInitFinish() {
        return mIsInit;
    }

    private synchronized void moveView(int dir) {
        mCheckParams.topMargin -= dir;
        mDownLoadParams.topMargin -= dir;
        if (mCheckParams.topMargin <= -mHeight) {
            mCheckParams.topMargin = 0;
            mCheckLayout.setVisibility(View.INVISIBLE);
        }
        if (mDownLoadParams.topMargin <= 0) {
            mDownLoadParams.topMargin = 0;
            if (mHandler != null) {
                mHandler.removeMessages(MOVE_ID);
            }
        } else {
            mHandler.sendEmptyMessage(MOVE_ID);
        }
        mCheckLayout.setLayoutParams(mCheckParams);
        mDownLoadLayout.setLayoutParams(mDownLoadParams);
    }
}
