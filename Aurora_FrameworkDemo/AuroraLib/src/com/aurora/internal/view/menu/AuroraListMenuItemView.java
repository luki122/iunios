package com.aurora.internal.view.menu;

import com.aurora.lib.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class AuroraListMenuItemView extends LinearLayout implements AuroraMenuView.ItemView {
	private static final String TAG = "ListMenuItemView";
	private AuroraMenuItemImpl mItemData;

	private ImageView mIconView;
	private RadioButton mRadioButton;
	private TextView mTitleView;
	private CheckBox mCheckBox;
	private TextView mShortcutView;

	private Drawable mBackground;
	private int mTextAppearance=-1;
	private Context mTextAppearanceContext;
	private boolean mPreserveIconSpacing;

	private int mMenuType;

	private LayoutInflater mInflater;

	private boolean mForceShowIcon;
	
	private Context mContext;

	public AuroraListMenuItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		mContext = context;
//		TypedArray a = context.obtainStyledAttributes(attrs, com.aurora.internal.R.styleable.AuroraMenuView,
//				defStyle, 0);

//		mBackground = a.getDrawable(com.aurora.internal.R.styleable.AuroraMenuView_auroraitemBackground);
//		mTextAppearance = a.getResourceId(com.aurora.internal.R.styleable.AuroraMenuView_auroraitemTextAppearance, -1);
//		mPreserveIconSpacing = a.getBoolean(com.aurora.internal.R.styleable.AuroraMenuView_aurorapreserveIconSpacing,
//				false);
//		mTextAppearanceContext = context;

//		a.recycle();
	}

	public AuroraListMenuItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

//		setBackgroundDrawable(mBackground);

		mTitleView = (TextView) findViewById(R.id.aurora_title);
		if (mTextAppearance != -1) {
			mTitleView.setTextAppearance(mTextAppearanceContext, mTextAppearance);
		}

		mShortcutView = (TextView) findViewById(R.id.aurora_shortcut);
	}

	public void initialize(AuroraMenuItemImpl itemData, int menuType) {
		mItemData = itemData;
		mMenuType = menuType;

		setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);

		setTitle(itemData.getTitleForItemView(this));
		setCheckable(itemData.isCheckable());
		setShortcut(itemData.shouldShowShortcut(), itemData.getShortcut());
		setIcon(itemData.getIcon());
		setEnabled(itemData.isEnabled());
	}

	public void setForceShowIcon(boolean forceShow) {
		mPreserveIconSpacing = mForceShowIcon = forceShow;
	}

	public void setTitle(CharSequence title) {
		if (title != null) {
			mTitleView.setText(title);

			if (mTitleView.getVisibility() != VISIBLE)
				mTitleView.setVisibility(VISIBLE);
		} else {
			if (mTitleView.getVisibility() != GONE)
				mTitleView.setVisibility(GONE);
		}
	}

	public AuroraMenuItemImpl getItemData() {
		return mItemData;
	}

	public void setCheckable(boolean checkable) {
		if (!checkable && mRadioButton == null && mCheckBox == null) {
			return;
		}

		// Depending on whether its exclusive check or not, the checkbox or
		// radio button will be the one in use (and the other will be otherCompoundButton)
		final CompoundButton compoundButton;
		final CompoundButton otherCompoundButton;

		if (mItemData.isExclusiveCheckable()) {
			if (mRadioButton == null) {
				insertRadioButton();
			}
			compoundButton = mRadioButton;
			otherCompoundButton = mCheckBox;
		} else {
			if (mCheckBox == null) {
				insertCheckBox();
			}
			compoundButton = mCheckBox;
			otherCompoundButton = mRadioButton;
		}

		if (checkable) {
			compoundButton.setChecked(mItemData.isChecked());

			final int newVisibility = checkable ? VISIBLE : GONE;
			if (compoundButton.getVisibility() != newVisibility) {
				compoundButton.setVisibility(newVisibility);
			}

			// Make sure the other compound button isn't visible
			if (otherCompoundButton != null && otherCompoundButton.getVisibility() != GONE) {
				otherCompoundButton.setVisibility(GONE);
			}
		} else {
			if (mCheckBox != null)
				mCheckBox.setVisibility(GONE);
			if (mRadioButton != null)
				mRadioButton.setVisibility(GONE);
		}
	}

	public void setChecked(boolean checked) {
		CompoundButton compoundButton;

		if (mItemData.isExclusiveCheckable()) {
			if (mRadioButton == null) {
				insertRadioButton();
			}
			compoundButton = mRadioButton;
		} else {
			if (mCheckBox == null) {
				insertCheckBox();
			}
			compoundButton = mCheckBox;
		}

		compoundButton.setChecked(checked);
	}

	public void setShortcut(boolean showShortcut, char shortcutKey) {
		final int newVisibility = (showShortcut && mItemData.shouldShowShortcut()) ? VISIBLE : GONE;

		if (newVisibility == VISIBLE) {
			mShortcutView.setText(mItemData.getShortcutLabel());
		}

		if (mShortcutView.getVisibility() != newVisibility) {
			mShortcutView.setVisibility(newVisibility);
		}
	}

	public void setIcon(Drawable icon) {
		final boolean showIcon = mItemData.shouldShowIcon() || mForceShowIcon;
		if (!showIcon && !mPreserveIconSpacing) {
			return;
		}

		if (mIconView == null && icon == null && !mPreserveIconSpacing) {
			return;
		}

		if (mIconView == null) {
			insertIconView();
		}

		if (icon != null || mPreserveIconSpacing) {
			mIconView.setImageDrawable(showIcon ? icon : null);

			if (mIconView.getVisibility() != VISIBLE) {
				mIconView.setVisibility(VISIBLE);
			}
		} else {
			mIconView.setVisibility(GONE);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mIconView != null && mPreserveIconSpacing) {
			// Enforce minimum icon spacing
			ViewGroup.LayoutParams lp = getLayoutParams();
			LayoutParams iconLp = (LayoutParams) mIconView.getLayoutParams();
			if (lp.height > 0 && iconLp.width <= 0) {
				iconLp.width = lp.height;
			}
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void insertIconView() {
		LayoutInflater inflater = getInflater();
		mIconView = (ImageView) inflater.inflate(R.layout.aurora_list_menu_item_icon, this,
				false);
		addView(mIconView, 0);
	}

	private void insertRadioButton() {
		LayoutInflater inflater = getInflater();
		mRadioButton = (RadioButton) inflater.inflate(R.layout.aurora_list_menu_item_radio,
				this, false);
		addView(mRadioButton);
	}

	private void insertCheckBox() {
		LayoutInflater inflater = getInflater();
		mCheckBox = (CheckBox) inflater.inflate(R.layout.aurora_list_menu_item_checkbox, this,
				false);
		addView(mCheckBox);
	}

	public boolean prefersCondensedTitle() {
		return false;
	}

	public boolean showsIcon() {
		return mForceShowIcon;
	}

	private LayoutInflater getInflater() {
		if (mInflater == null) {
			mInflater = LayoutInflater.from(mContext);
		}
		return mInflater;
	}
}