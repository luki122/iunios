/**
 * 
 */
package com.aurora.plugin;
import java.util.ArrayList;
import com.aurora.launcher.BubbleTextView;
import com.aurora.launcher.FolderIcon;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Vulcan added these code in 下午2:34:09
 * 
 * Dynamic folder creating flow:
 * 1.FolderIcon.fromXml, we create a new Object DynIconPlg
 * 2.add view to celllayout
 * 3.onAttachedToWindow is called
 * 4.DynIconPlg.addDynIcon is called
 * 5.At this moment, we have a folder in our list
 * 6.If timer is on, now we can see dynamic folder
 * 7.In some time, item in the folder changes.
 * 8.It leads to changes of FolderIcon.mDynIconPlg to null.
 * 9.however, it is still in our draw list, because onDetachedFromWindow doesn't happen
 *
 */
public class FolderMovingIcon extends DynIconPlg {

	/**
	 * @param v
	 */
	public FolderMovingIcon(View v) {
		super(v);
		mIsDynamic = true;
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#getCurDrawContent()
	 */
	@Override
	public Drawable getCurDrawContent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#getRedrawMultipleFreq()
	 */
	@Override
	public int getRedrawMultipleFreq() {
		// TODO Auto-generated method stub
		return 20;
	}
	
	@Override
	public boolean isDirty() {
/*
		FolderIcon fiView = (FolderIcon)hostView;
		ArrayList<View> previewList = fiView.getPreviewItems();//it must be not null
		int prvCnt = previewList.size();
		BubbleTextView subIcon = null;
		//Drawable drawable = null;
		for(int i=0;i < prvCnt; i ++) {
			subIcon = (BubbleTextView)previewList.get(i);
			if(subIcon.mDynIconPlg != null) {
				if(subIcon.mDynIconPlg.isDirty()) {
					return true;
				}
			}
		}
		return false;
*/
		return true;
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#refreshDynIcon()
	 */
	@Override
	public boolean refreshDynIcon() {
		// TODO Auto-generated method stub
		FolderIcon fiView = (FolderIcon)hostView;
		ArrayList<View> previewList = fiView.getDynPreviewItems();//it must be not null
		int prvCnt = previewList.size();
		if (prvCnt > 0) {
			BubbleTextView subIcon = null;
			Drawable drawable = null;
			for (int i = 0; i < prvCnt; i++) {
				subIcon = (BubbleTextView) previewList.get(i);
				if (subIcon.mDynIconPlg != null) {
					drawable = subIcon.mDynIconPlg.getCurDrawContent();
					if (drawable != null) {
						subIcon.setCompoundDrawablesWithIntrinsicBounds(null,
								drawable, null, null);
						// Log.d("vulcan-iconop", "refreshDynIcon: subIcon = " +
						// subIcon.getText());
					}
				}
			}
			fiView.invalidate();
			return true;
		}
		return false;
	}
	
	@Override
	public void resetDirty() {
		return;
	}
	
	@Override
	public void dump() {
		return;
	}

}
