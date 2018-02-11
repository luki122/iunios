package com.android.auroramusic.widget;

import android.content.Context;
import android.widget.SectionIndexer;

class SectionIndexerAdapterWrapper implements SectionIndexer {
	
	final SectionIndexer mSectionIndexerDelegate;

	SectionIndexerAdapterWrapper(Context context,
			StickyListHeadersAdapter delegate) {
		mSectionIndexerDelegate = (SectionIndexer) delegate;
	}

	@Override
	public int getPositionForSection(int section) {
		return mSectionIndexerDelegate.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return mSectionIndexerDelegate.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return mSectionIndexerDelegate.getSections();
	}

}
