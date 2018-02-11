package aurora.lib.widget;

import com.aurora.lib.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * @author leftaven
 * @2013年9月12日 初始化界面
 */
public class AuroraActionBarHost extends LinearLayout {

	private AuroraActionBar mActionBar;
	private FrameLayout mContentView;

	public AuroraActionBarHost(Context context) {
		this(context, null);
	}

	public AuroraActionBarHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mActionBar = (AuroraActionBar) findViewById(R.id.aurora_action_bar);
		if (mActionBar == null || !(mActionBar instanceof AuroraActionBar)) {
			throw new IllegalArgumentException(
					"No ActionBar with the id R.id.aurora_action_bar found in the layout.");
		}

		mContentView = (FrameLayout) findViewById(R.id.aurora_action_bar_content_view);
		if (mContentView == null || !(mContentView instanceof FrameLayout)) {
			throw new IllegalArgumentException(
					"No FrameLayout with the id R.id.aurora_action_bar_content_view found in the layout.");
		}
	}

	public AuroraActionBar getActionBar() {
		return mActionBar;
	}

	public FrameLayout getContentView() {
		return mContentView;
	}

}
