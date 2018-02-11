package com.aurora.widget;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.aurora.filemanager.R;

/**
 * 显示空数据view和loading view
 * 
 * @author jiangxh
 * @CreateTime 2014年4月28日 下午5:33:23
 * @Description com.aurora.widget AuroraLoadAndEmptyView.java
 */
public class AuroraLoadAndEmptyView {

	private LinearLayout loading_view;
	private ProgressBar loading_progressbar;

	private LinearLayout empty_view;
	private TextView empty_tv;

	private View baseView;

	public AuroraLoadAndEmptyView(View baseView) {
		super();
		this.baseView = baseView;
	}

	/**
	 * 获取loading view
	 * 
	 * @return
	 */
	public LinearLayout getLoadingView() {
		if (loading_view == null) {
			loading_view = (LinearLayout) baseView
					.findViewById(R.id.loading_view);
		}
		return loading_view;
	}

	public ProgressBar getLoadingProgressbar() {
		if (loading_progressbar == null) {
			loading_progressbar = (ProgressBar) baseView
					.findViewById(R.id.loading_progressbar);
		}
		return loading_progressbar;
	}

	public TextView getEmptyTextView() {
		if (empty_tv == null) {
			empty_tv = (TextView) baseView.findViewById(R.id.empty_tv);
		}
		return empty_tv;
	}

	public LinearLayout getEmptyView() {
		if (empty_view == null) {
			empty_view = (LinearLayout) baseView.findViewById(R.id.empty_view);
		}
		return empty_view;
	}

}
