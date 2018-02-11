package com.aurora.internal.view.menu;

import com.aurora.internal.view.menu.AuroraMenuBuilder.ItemInvoker;
import com.aurora.lib.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class AuroraExpandedMenuView extends ListView implements ItemInvoker, AuroraMenuView, OnItemClickListener {
	private AuroraMenuBuilder mMenu;

	/** Default animations for this menu */
	private int mAnimations;

	/**
	 * Instantiates the AuroraExpandedMenuView that is linked with the provided AuroraMenuBuilder.
	 * 
	 * @param menu
	 *            The model for the menu which this MenuView will display
	 */
	public AuroraExpandedMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);

//		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AuroraMenuView, 0, 0);
//		mAnimations = a.getResourceId(R.styleable.AuroraMenuView_auroraWindowAnimationStyle, 0);
//		a.recycle();

		setOnItemClickListener(this);
	}

	public void initialize(AuroraMenuBuilder menu) {
		mMenu = menu;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		// Clear the cached bitmaps of children
		setChildrenDrawingCacheEnabled(false);
	}

	public boolean invokeItem(AuroraMenuItemImpl item) {
		return mMenu.performItemAction(item, 0);
	}

	public void onItemClick(AdapterView parent, View v, int position, long id) {
		invokeItem((AuroraMenuItemImpl) getAdapter().getItem(position));
	}

	public int getWindowAnimations() {
		return mAnimations;
	}

}
