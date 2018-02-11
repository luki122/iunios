package aurora.lib.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.aurora.lib.R;

/**
 * @author leftaven
 * @2013年9月12日 aurora menu
 */
public class AuroraSystemMenu extends AuroraMenuBase {
	public ListView menuListView;
	public LinearLayout layout;

	private OnAuroraMenuItemClickListener auroraMenuCallBack;
	
	public void setMenuItemCLickListener(OnAuroraMenuItemClickListener auroraMenuCallBack){
	    this.auroraMenuCallBack = auroraMenuCallBack;
	}
	
	private Handler mMenuActionHandler;
	private Runnable mMenuAciton = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            
        }
    };
	/**
	 * 计算listview高度
	 * 
	 * @param pull
	 */
	public void setPullLvHeight(ListView pull) {
		int totalHeight = 0;
		ListAdapter adapter = pull.getAdapter();
		for (int i = 0, len = adapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
			View listItem = adapter.getView(i, null, pull);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}

		ViewGroup.LayoutParams params = pull.getLayoutParams();
		params.height = totalHeight
				+ (pull.getDividerHeight() * (pull.getCount() - 1));
		pull.setLayoutParams(params);
	}
	private void setCallBack(OnAuroraMenuItemClickListener auroraMenuCallBack,int position){
	    auroraMenuItem = (AuroraMenuItem) menuAdapter
                .getItem(position);
        auroraMenuCallBack.auroraMenuItemClick(auroraMenuItem
                .getId());
	}

	public AuroraSystemMenu(final Context context,
			final OnAuroraMenuItemClickListener auroraMenuCallBack,
			final AuroraMenuAdapterBase auroraMenuAdapter, int aniTabMenu,
			int resId) {
		super(context);
		mMenuActionHandler = new Handler();
		menuAdapter = auroraMenuAdapter;
		mContext = context;
		layout = (LinearLayout) LayoutInflater.from(context).inflate(resId,
				null);
		layout.setFocusableInTouchMode(true);// 重点，再次点击menu键，窗口消失

		menuListView = (ListView) layout.findViewById(R.id.auroraMenuContentLv);
		menuListView.setAdapter(menuAdapter);
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if (auroraMenuCallBack != null) {
//					auroraMenuItem = (AuroraMenuItem) menuAdapter
//							.getItem(position);
//					auroraMenuCallBack.auroraMenuItemClick(auroraMenuItem
//							.getId());
//				    Constants.SHOW_DIALOAG_FROM_MENU = true;
					if (isShowing()) {// 菜单项目触发，隐藏menu
						dismissMenu();
					}
					if(Looper.myLooper() == mMenuActionHandler.getLooper()){
					    setCallBack(auroraMenuCallBack,position);
					}else {
					    mMenuActionHandler.postDelayed(mMenuAciton, 50);
                    }
				}
			}
		});
		layout.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// 单击menu菜单时拦截事件
				if (keyCode == KeyEvent.KEYCODE_MENU && isShowing()) {
					dismissMenu();
				} else if (keyCode == KeyEvent.KEYCODE_BACK && isShowing()) {
					dismissMenu();
					// dismiss();
				}
				return false;
			}
		});

		layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissMenu();
			}
		});

		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				dismissMenu();
			}
		});

		defaultMenu(layout, aniTabMenu);
	}

	/**
	 * 增加菜单项
	 * 
	 * @param itemId
	 */
	public void addMenuItemById(int itemId) {
		menuAdapter.addMenuItemById(itemId);
		setPullLvHeight(menuListView);
		menuAdapter.notifyDataSetChanged();
	}

	/**
	 * 删除菜单项
	 * 
	 * @param itemId
	 */
	public void removeMenuItemById(int itemId) {
		menuAdapter.removeMenuItemById(itemId);
		setPullLvHeight(menuListView);
		menuAdapter.notifyDataSetChanged();
	}

	// Aurora <leftaven> <2013年9月13日> modify for OTA begin
	/**
	 * 单个menu项设置是否可用
	 * 
	 * @param itemId
	 * @param isEnable
	 */
	public void setMenuItemEnable(int itemId, boolean isEnable) {
		menuAdapter.setMenuItemEnable(itemId, isEnable);
	}
	
	//Aurora add by tangjun 2014.7.7 start
	public void addMenu(int itemId,int titleRes,int iconRes) {
		menuAdapter.addMenu(itemId, titleRes, iconRes);
		menuAdapter.notifyDataSetChanged();
		setPullLvHeight(menuListView);
		update();
	}

	public void addMenu(int itemId,int titleRes,int iconRes, int position) {
		menuAdapter.addMenu(itemId, titleRes, iconRes, position);
		menuAdapter.notifyDataSetChanged();
		setPullLvHeight(menuListView);
		update();
	}
	
	public void addMenu(int itemId,CharSequence menuText,int iconRes) {
		menuAdapter.addMenu(itemId, menuText, iconRes);
		menuAdapter.notifyDataSetChanged();
		setPullLvHeight(menuListView);
		update();
	}

	public void addMenu(int itemId,CharSequence menuText,int iconRes, int position) {
		menuAdapter.addMenu(itemId, menuText, iconRes, position);
		menuAdapter.notifyDataSetChanged();
		setPullLvHeight(menuListView);
		update();
	}
	
	public void removeMenuByItemId(int itemId) {
		menuAdapter.removeMenuByItemId(itemId);
		menuAdapter.notifyDataSetChanged();
		setPullLvHeight(menuListView);
		update();
	}
	
	public void removeMenuByPosition(int position) {
		menuAdapter.removeMenuByPosition(position);
		menuAdapter.notifyDataSetChanged();
		setPullLvHeight(menuListView);
		update();
	}
	
	public void setMenuTextByItemId(int menuTextResId, int itemId) {
		menuAdapter.setMenuTextByItemId(menuTextResId, itemId);
		menuAdapter.notifyDataSetChanged();
		update();
	}
	
	public void setMenuTextByItemId(CharSequence menuText, int itemId) {
		menuAdapter.setMenuTextByItemId(menuText, itemId);
		menuAdapter.notifyDataSetChanged();
		update();
	}
	
	public void setMenuTextByPosition(int menuTextResId, int position) {
		menuAdapter.setMenuTextByPosition(menuTextResId, position);
		menuAdapter.notifyDataSetChanged();
		update();
	}
	
	public void setMenuTextByPosition(CharSequence menuText, int position) {
		menuAdapter.setMenuTextByPosition(menuText, position);
		menuAdapter.notifyDataSetChanged();
		update();
	}
	//Aurora add by tangjun 2014.7.7 end
}
