package com.android.systemui.recent;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.util.Log;

import android.content.res.Configuration;
import com.android.systemui.R;
import android.view.Gravity;

public class DragLayer extends FrameLayout{

	private Context mContext;

	public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
		mContext = context;
		initSize();
	}

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public int x, y;
        public boolean customPosition = false;

        /**
         * {@inheritDoc}
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }
    }

	private boolean isOrientationPortrait(){
		int orientation = mContext.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		return false;
	}

// temp
	View noAppsView, recentAppsView;

	private void initView(){
		if(noAppsView == null || recentAppsView == null){
			noAppsView = findViewById(R.id.recents_no_apps);
			recentAppsView = findViewById(R.id.recents_container);
		}
	}

	int noapp_view_height;
	int noapp_view_width_land;
	int recent_container_height;
	int recent_container_width_land;

	// as screen orientation changed, we should reload size, it's contain land & portrait
	private void initSize(){
		noapp_view_height = getResources().getDimensionPixelSize(R.dimen.recents_view_height);
		noapp_view_width_land = getResources().getDimensionPixelSize(R.dimen.recents_view_width_land);

		recent_container_height = getResources().getDimensionPixelSize(R.dimen.recents_container_height);
		recent_container_width_land = getResources().getDimensionPixelSize(R.dimen.recents_container_width_land);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateChildLayoutParam();
	}

	private void updateChildLayoutParam(){
		initView();
		FrameLayout.LayoutParams noAppsParam = (FrameLayout.LayoutParams)noAppsView.getLayoutParams();
		FrameLayout.LayoutParams recentAppsParam = (FrameLayout.LayoutParams)recentAppsView.getLayoutParams();
		if(isOrientationPortrait()){
			noAppsParam.width = FrameLayout.LayoutParams.MATCH_PARENT;
			noAppsParam.height = noapp_view_height;
			noAppsParam.gravity = Gravity.BOTTOM | Gravity.LEFT;

			recentAppsParam.width = FrameLayout.LayoutParams.MATCH_PARENT;
			recentAppsParam.height = recent_container_height;
			recentAppsParam.gravity = Gravity.BOTTOM | Gravity.LEFT;

		} else {
			noAppsParam.width = noapp_view_width_land;
			noAppsParam.height = FrameLayout.LayoutParams.MATCH_PARENT;
			noAppsParam.gravity = Gravity.BOTTOM | Gravity.RIGHT;

			recentAppsParam.width = recent_container_width_land;
			recentAppsParam.height = FrameLayout.LayoutParams.MATCH_PARENT;
			recentAppsParam.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		}
		noAppsView.setLayoutParams(noAppsParam);
		recentAppsView.setLayoutParams(recentAppsParam);
	}
}
