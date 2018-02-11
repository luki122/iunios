package aurora.lib.widget;

import android.view.LayoutInflater;
import android.view.View;

public class CustomAuroraActionBarItem extends NormalAuroraActionBarItem {

	private View mCustomView;

	@Override
	protected View createItemView(int resId) {
		mCustomView = LayoutInflater.from(mContext).inflate(resId, mActionBar,
				false);
		return mCustomView;
	}

	@Override
	protected void prepareItemView() {
	}

}
