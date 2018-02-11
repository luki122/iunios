package aurora.lib.widget;

import android.view.View;

/**
 * @author leftaven
 * @2013年9月12日
 * menu item
 */
public class AuroraMenuItem {
	private int id = 0;
	private int title = 0;
	private int icon = 0;
	private CharSequence titleText = null;


	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}
	
	public void setTitleText( CharSequence titleText ) {
		this.titleText = titleText;
	}
	
	public CharSequence getTitleText( ) {
		return titleText;
	}

}
