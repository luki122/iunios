package aurora.lib.widget;

import android.widget.FrameLayout;

/**
 * @author leftaven
 * @2013年9月12日 
 * actionbar对外提供接口
 */
public interface AuroraActionBarBase {

	public FrameLayout getContentView();

	public AuroraActionBarItem addAuroraActionBarItem(AuroraActionBarItem item);

	public AuroraActionBarItem addAuroraActionBarItem(AuroraActionBarItem item,
			int itemId);

	public AuroraActionBarItem addAuroraActionBarItem(
			AuroraActionBarItem.Type actionBarItemType);

	public AuroraActionBarItem addAuroraActionBarItem(
			AuroraActionBarItem.Type actionBarItemType, int itemId);

	public int createLayout();

	public void onPreContentChanged();

}
