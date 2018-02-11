package com.aurora.stickylistheaders;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

public class ContextWrapperEdgeEffect extends ContextWrapper {
	private ResourcesEdgeEffect RES_EDGE_EFFECT;

	public ContextWrapperEdgeEffect(Context context) {
		super(context);
		Resources resources = context.getResources();
		RES_EDGE_EFFECT = new ResourcesEdgeEffect(context,
				resources.getAssets(), resources.getDisplayMetrics(),
				resources.getConfiguration());
	}

	// 返回自定义的Resources
	public Resources getResources() {
		return RES_EDGE_EFFECT;
	}
}
