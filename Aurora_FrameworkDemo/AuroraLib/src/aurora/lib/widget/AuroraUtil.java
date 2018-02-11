package aurora.lib.widget;

/**
 * @author leftaven
 * @2013年9月12日
 * @TODO AuroraUtil for menu actionbar
 */
public class AuroraUtil {

	// MENU TYPE 0 表示菜单消失，1表示显示菜单
	public static int MENU_SHOW = 1;
	public static int MENU_DISMISS = 0;
	public static String MENU_ITEM = "item";
	public static String MENU_TITLE = "title";
	public static String MENU_ICON = "icon";
	public static String MENU_ID = "id";

	// for font
	public static String ACTION_BAR_TITLE_FONT = "/system/fonts/title.ttf";
	public static String ACTION_BAR_TITLE_FONT_FORENGLISH = "/system/fonts/title.ttf";

	// android res xml
	public static String ANDROID_XMLNS = "http://schemas.android.com/apk/res/android";

	// aurora action bar handler number
	public static int ACTION_BAR_MENU_DISMISS = 0x0001;
	public static int ACTION_BAR_MENU_SHOW=0x0002;
	public static int ACTION_BAR_DASHBOARD_SHOW = 0x0003;
	public static int ACTION_BAR_DASHBOARD_MISS=0x0004;
	
	public static final int ACTION_BAR_HEIGHT_PX = 165;
}
