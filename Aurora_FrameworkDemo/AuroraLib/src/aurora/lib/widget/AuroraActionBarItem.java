package aurora.lib.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.aurora.lib.R;
/**
 * @author leftaven
 * @2013年9月12日
 * actionbar item
 */
public abstract class AuroraActionBarItem {
	// 定义图片类型
	public enum Type {
		GoHome, Info, Back, More,Add,Edit
	}
	protected int mDrawableId;
	protected Drawable mDrawable;

	protected CharSequence mContentDescription;
	protected View mItemView;

	protected Context mContext;
	protected AuroraActionBar mActionBar;// actionbarItem所依赖到actionbar

	private int mItemId;
	
	private int resId;

	public AuroraActionBarItem setResId(int resId) {
		this.resId = resId;
		return this;
	}

	public void setActionBar(AuroraActionBar actionBar) {
		mContext = actionBar.getContext();
		mActionBar = actionBar;
	}

	public Drawable getDrawable() {
		return mDrawable;
	}
	
	public AuroraActionBarItem getDrawableId(int drawableId){
		mDrawableId=drawableId;
		return this;
	}

	public AuroraActionBarItem setDrawable(int drawableId) {
		return setDrawable(mContext.getResources().getDrawable(drawableId));
	}

	public AuroraActionBarItem setDrawable(Drawable drawable) {
		if (drawable != mDrawable) {
			mDrawable = drawable;
			if (mItemView != null) {
				onDrawableChanged();
			}
		}
		return this;
	}

	public AuroraActionBarItem setContentDescription(
			CharSequence contentDescription) {
		if (contentDescription != mContentDescription) {
			mContentDescription = contentDescription;
			if (mItemView != null) {
				onContentDescriptionChanged();
			}
		}
		return this;
	}

	public AuroraActionBarItem setContentDescription(int contentDescriptionId) {
		return setContentDescription(mContext.getString(contentDescriptionId));
	}

	public CharSequence getContentDescription() {
		return mContentDescription;
	}
	
	public void setItemView() {
		mItemView = createItemView(this.resId);
		prepareItemView();
	}

	public View getItemView() {
		if (mItemView == null) {
			mItemView = createItemView(this.resId);
			prepareItemView();
		}
		return mItemView;
	}

	protected abstract View createItemView(int resId);

	protected void prepareItemView() {
	}

	protected void onDrawableChanged() {
	}

	protected void onContentDescriptionChanged() {
	}

	protected void onItemClicked() {
	}

	public void setItemId(int itemId) {
		mItemId = itemId;
	}

	public int getItemId() {
		return mItemId;
	}

	public static AuroraActionBarItem createWithType(AuroraActionBar actionBar,
			AuroraActionBarItem.Type type) {

		int drawableId = 0;
		int descriptionId = 0;

		switch (type) {
		case GoHome:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_home;
			descriptionId = com.aurora.lib.R.string.aurora_go_home;
			break;

		case Info:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_info;
			descriptionId = com.aurora.lib.R.string.aurora_info;
			break;

		case Back:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_back;
			descriptionId = com.aurora.lib.R.string.aurora_back;
			break;

		case More:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_more;
			descriptionId = com.aurora.lib.R.string.aurora_more;
			break;
			
		case Add:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_add;
			descriptionId = com.aurora.lib.R.string.aurora_add;
			break;
			
		case Edit:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_edit;
			descriptionId =com.aurora.lib.R.string.aurora_edit;
			break;

		default:
			return null;
		}
		final Drawable d = actionBar.getContext().getResources()
				.getDrawable(drawableId);

		return actionBar.newActionBarItem(NormalAuroraActionBarItem.class)
				.setDrawable(d).setContentDescription(descriptionId).getDrawableId(drawableId);
	}
	
	public static int getDrawableIdWithType(
			AuroraActionBarItem.Type type) {

		int drawableId = 0;
		int descriptionId = 0;

		switch (type) {
		case GoHome:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_home;
			descriptionId = com.aurora.lib.R.string.aurora_go_home;
			break;

		case Info:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_info;
			descriptionId = com.aurora.lib.R.string.aurora_info;
			break;

		case Back:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_back;
			descriptionId = com.aurora.lib.R.string.aurora_back;
			break;

		case More:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_more;
			descriptionId = com.aurora.lib.R.string.aurora_more;
			break;
			
		case Add:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_add;
			descriptionId = com.aurora.lib.R.string.aurora_add;
			break;
			
		case Edit:
			drawableId = com.aurora.lib.R.drawable.aurora_action_bar_edit;
			descriptionId =com.aurora.lib.R.string.aurora_edit;
			break;

		default:
			return -1;
		}

		return drawableId;
	}
}
