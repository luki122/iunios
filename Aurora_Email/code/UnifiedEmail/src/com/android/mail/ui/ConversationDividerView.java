package com.android.mail.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.mail.R;
import com.android.mail.analytics.Analytics;
import com.android.mail.browse.ConversationCursor;
import com.android.mail.preferences.MailPrefs;
import com.android.mail.providers.Folder;
import com.android.mail.utils.Utils;
/**
 * listView Divider class
 * @author JXH
 *
 */
public class ConversationDividerView extends FrameLayout implements
		ConversationSpecialItemView, SwipeableItemView {

	private AnimatedAdapter mAnimatedAdapter;

	private static int sScrollSlop = 0;
	private static int sShrinkAnimationDuration;
	
	private final MailPrefs mMailPrefs;
	
	/** Whether we are on a tablet device or not */
    private final boolean mTabletDevice;
    /** When in conversation mode, true if the list is hidden */
    private final boolean mListCollapsible;
    
    //private static final String AURORA_FONT_PATH = "system/fonts/DroidSansFallback.ttf";
    //private static final Typeface AURORA_FONT = Typeface.createFromFile(AURORA_FONT_PATH);

	private View mSwipeableContent;

	public ConversationDividerView(final Context context) {
		this(context, null);
	}

	public ConversationDividerView(final Context context,
			final AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public ConversationDividerView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		final Resources resources = context.getResources();

		synchronized (ConversationDividerView.class) {
			if (sScrollSlop == 0) {
				sScrollSlop = resources.getInteger(R.integer.swipeScrollSlop);
				sShrinkAnimationDuration = resources
						.getInteger(R.integer.shrink_animation_duration);
			}
		}

		mMailPrefs = MailPrefs.get(context);
		mTabletDevice = Utils.useTabletUI(resources);
		mListCollapsible = resources.getBoolean(R.bool.list_collapsible);
	}

	@Override
	public SwipeableView getSwipeableView() {
		return SwipeableView.from(mSwipeableContent);
	}

	@Override
	public boolean canChildBeDismissed() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void dismiss() {

	}

	@Override
	public float getMinAllowScrollDistance() {
		return sScrollSlop;
	}

	@Override
	public void onGetView() {
		// do nothing
	}

	@Override
	public boolean getShouldDisplayInList() {
		
		return position>0;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mSwipeableContent = findViewById(R.id.swipeable_content);
		//((TextView)findViewById(R.id.earlier_txt)).setTypeface(AURORA_FONT);
		
		((TextView)findViewById(R.id.earlier_txt)).setTypeface(Typeface.DEFAULT);   //cjs modify
	}
	
	private int position=0;
	
	public void setPosition(int position){
		this.position = position;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public void setAdapter(AnimatedAdapter adapter) {
		mAnimatedAdapter = adapter;
	}

	@Override
	public void bindFragment(LoaderManager loaderManager,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConversationSelected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCabModeEntered() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCabModeExited() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean acceptsUserTaps() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onConversationListVisibilityChanged(boolean visible) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdate(Folder folder, ConversationCursor cursor) {
		// Do nothing

	}

}
