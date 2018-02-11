package com.aurora.stickylistheaders;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ListAdapter;

class AdapterWrapper extends BaseAdapter implements StickyListHeadersAdapter {


	public interface OnHeaderClickListener{
		public void onHeaderClick(View header, int itemPosition, long headerId);
	}

	final StickyListHeadersAdapter mDelegate;
	private final List<View> mHeaderCache = new LinkedList<View>();
	private final Context mContext;
	private Drawable mDivider;
	private int mDividerHeight;
	private OnHeaderClickListener mOnHeaderClickListener;
	private DataSetObserver mDataSetObserver = new DataSetObserver() {

		@Override
		public void onInvalidated() {
			mHeaderCache.clear();
			AdapterWrapper.super.notifyDataSetInvalidated();
		}
		
		@Override
		public void onChanged() {
			AdapterWrapper.super.notifyDataSetChanged();
		}
	};

	AdapterWrapper(Context context,
			StickyListHeadersAdapter delegate) {
		this.mContext = context;
		this.mDelegate = delegate;
		delegate.registerDataSetObserver(mDataSetObserver);
	}

	void setDivider(Drawable divider) {
		this.mDivider = divider;
	}

	void setDividerHeight(int dividerHeight) {
		this.mDividerHeight = dividerHeight;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return mDelegate.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(int position) {
		return mDelegate.isEnabled(position);
	}

	@Override
	public int getCount() {
		return mDelegate.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mDelegate.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mDelegate.getItemId(position);
	}

	@Override
	public boolean hasStableIds() {
		return mDelegate.hasStableIds();
	}

	@Override
	public int getItemViewType(int position) {
		return mDelegate.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mDelegate.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		return mDelegate.isEmpty();
	}

	private void recycleHeaderIfExists(WrapperView wv) {
		View header = wv.mHeader;
		if (header != null) {
			mHeaderCache.add(header);
		}
	}

	private View configureHeader(WrapperView wv, final int position) {
		View header = wv.mHeader == null ? popHeader() : wv.mHeader;
		header = mDelegate.getHeaderView(position, header, wv);
		if (header == null) {
			throw new NullPointerException("Header view must not be null.");
		}
	
		header.setClickable(true);
		header.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mOnHeaderClickListener != null){
					long headerId = mDelegate.getHeaderId(position);
					mOnHeaderClickListener.onHeaderClick(v, position, headerId);
				}
			}
		});
		return header;
	}

	private View popHeader() {
		if(mHeaderCache.size() > 0) {
			return mHeaderCache.remove(0);
		}
		return null;
	}

	
	private boolean previousPositionHasSameHeader(int position) {
		return position != 0
				&& mDelegate.getHeaderId(position) == mDelegate
						.getHeaderId(position - 1);
	}

	/**
	 * @hideHeader 某些情况下我们不需要显示header
	 */
	private boolean hideHeader = false;
	protected void setAdapterHeaderVisiblity(boolean flag){
		hideHeader = flag;
	}
	
	@Override
	public WrapperView getView(int position, View convertView, ViewGroup parent) {
		WrapperView wv = (convertView == null) ? new WrapperView(mContext) : (WrapperView) convertView;
		View item = mDelegate.getView(position, wv.mItem, wv);
		View header = null;
		if (previousPositionHasSameHeader(position)) {
			recycleHeaderIfExists(wv);
		} else {
			header = configureHeader(wv, position);
		}
		if((item instanceof Checkable) && !(wv instanceof CheckableWrapperView)) {
			wv = new CheckableWrapperView(mContext);
		} else if(!(item instanceof Checkable) && (wv instanceof CheckableWrapperView)) {
			wv = new WrapperView(mContext);
		}
		if(hideHeader){
			wv.update(item, null, null, mDividerHeight);
		} else {
			wv.update(item, header, mDivider, mDividerHeight);
		}
		return wv;
	}

	public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener){
		this.mOnHeaderClickListener = onHeaderClickListener;
	}

	@Override
	public boolean equals(Object o) {
		return mDelegate.equals(o); 
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return ((BaseAdapter) mDelegate).getDropDownView(position, convertView, parent);
	}

	@Override
	public int hashCode() {
		return mDelegate.hashCode();
	}

	@Override
	public void notifyDataSetChanged() {
		((BaseAdapter) mDelegate).notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		((BaseAdapter) mDelegate).notifyDataSetInvalidated();
	}

	@Override
	public String toString() {
		return mDelegate.toString();
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		return mDelegate.getHeaderView(position, convertView, parent);
	}

	@Override
	public long getHeaderId(int position) {
		return mDelegate.getHeaderId(position);
	}

}
