package com.aurora.stickylistheaders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public interface StickyListHeadersAdapter extends ListAdapter {

	View getHeaderView(int position, View convertView, ViewGroup parent);

	long getHeaderId(int position);
}
