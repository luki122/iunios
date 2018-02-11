package com.secure.stickylistheaders;

import android.content.Context;
import android.widget.SectionIndexer;

/**
 * ���adapter�����⹦�ܣ����϶�ListView�Ҳ�Ŀ����϶���ʱ������ʾ��ǩ��ĸ
 * @author bin.huang
 *
 */
class SectionIndexerAdapterWrapper extends
		AdapterWrapper implements SectionIndexer {
	
	final SectionIndexer mSectionIndexerDelegate;

	SectionIndexerAdapterWrapper(Context context,
			StickyListHeadersAdapter delegate) {
		super(context, delegate);
		mSectionIndexerDelegate = (SectionIndexer) delegate;
	}

	@Override
	public int getPositionForSection(int section) {
		//listView����ʱ��ϵͳ�ص��������
		return mSectionIndexerDelegate.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		//listView����ʱ��ϵͳ�ص��������
		return mSectionIndexerDelegate.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		//��ʼ��ʱ��ϵͳ�ص��������
		return mSectionIndexerDelegate.getSections();
	}

}
