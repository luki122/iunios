package aurora.lib.widget;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.aurora.lib.R;
import com.aurora.lib.utils.AuroraLog;

public class AuroraCustomMenu extends PopupWindow {
    private LinearLayout mLayout;
    private View mContentView;
    private Context mContext;
    private int mMenuAnim;
    private LayoutInflater mInflater;
    
    private Map<Integer,MenuItem> mMenuMap = new HashMap<Integer,MenuItem>();
    private int position;
    
    private Animation coverAnimation;
    private OnMenuItemClickLisener mOnMenuItemClickListener;
    
    private boolean mShowFromMenuKey;
    private boolean showing;
    private int mPressedMenu;
    
    private CallBack mCallback;
    
    public interface CallBack{
        
        public void callBack(boolean flag);
    }
    public void setCallBack(CallBack c){
        this.mCallback = c;
    }
    public interface OnMenuItemClickLisener{
        public void onItemClick(View menu);
    }
    public void setOnMenuItemClickListener(OnMenuItemClickLisener listener){
        this.mOnMenuItemClickListener = listener;
    }
    public AuroraCustomMenu(Context context,int menuAnim,int contentView){
        this.mContext = context;
        this.mMenuAnim = menuAnim;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = mInflater.inflate(contentView, null);
        mLayout = (LinearLayout) mContentView.findViewById(R.id.aurora_custom_menu_layout);
        mContentView.setFocusableInTouchMode(true);
        mContentView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_MENU){
                    if(mCallback != null){
                        mCallback.callBack(true);
                    }
                    return true;
                }
                return false;
            }
        });
        this.setContentView(mContentView);
        this.setWidth(LayoutParams.FILL_PARENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new BitmapDrawable());// 设置Menu菜单背景
        if(menuAnim!=-1)
        this.setAnimationStyle(menuAnim);
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        
        
        
    }
    
    public void addMenu(int menuId,CharSequence menuTitle,final OnMenuItemClickLisener listener){
        View menu = getView(com.aurora.lib.R.layout.aurora_menu_item, menuId, menuTitle);
        addMenu(menuId, menu, listener);
    }
    
    public void addMenu(int menuId,View menuView,final OnMenuItemClickLisener listener){
        position +=position;
        MenuItem menuItem = new MenuItem();
        menuItem.setMenuItem(menuView);
        menuItem.setMenuId(menuId);
        menuItem.setPosition(position);
        menuItem.setOnClickListener(listener);
        if(!existMenu(menuItem)){
            mMenuMap.put(menuId, menuItem);
        }
        createMenu();
    }
    
    public void addMenu(MenuItem menu){
        if(menu == null){
            return;
        }
        View menuView = menu.getMenuItem();
        mLayout.addView(menuView);
    }
    
    public void setCustomMenuText(CharSequence menuText, int menuId) {
    	MenuItem menu = null;
    	if(mMenuMap.containsKey(menuId)){
    		menu = mMenuMap.get(menuId);
    		View menuView = menu.getMenuItem();
    		if(menuView != null){
    			TextView title = (TextView)menuView.findViewById(R.id.aurora_menu_item_text);
    			if(!TextUtils.isEmpty(menuText)){
    				if(title != null){
    					title.setText(menuText);
    				}
    			}
    			createMenu();
    		}
    	}
    }
    
    public void removeMenu(MenuItem menu){
        if(menu == null){
            return;
        }
        removeMenuInternal(menu);
    }
    
    public void removeMenuById(int menuId){
        MenuItem menu = null;
        if(mMenuMap.containsKey(menuId)){
            menu = mMenuMap.get(menuId);
            mMenuMap.remove(menuId);
            removeMenuInternal(menu);
        }
        
        
    }
    public void removeMenuByTitle(CharSequence title){
//        MenuItem menu = null;
//        if(mMenuMap.containsKey(menuId)){
//            menu = mMenuMap.get(menuId);
//            mMenuMap.remove(menuId);
//            removeMenu(menu);
//        }
        
    }
    
    public boolean existMenu(MenuItem item){
//        if(mMenuMap.containsKey(item.getMenuId())){
//            return true;
//        }
        TextView title = (TextView)item.getMenuItem().findViewById(R.id.aurora_menu_item_text);
        for(Integer key:mMenuMap.keySet()){
            MenuItem menu = mMenuMap.get(key);
            
            TextView itemText =(TextView) menu.getMenuItem().findViewById(R.id.aurora_menu_item_text);
            if(itemText != null && title != null){
                if(itemText.getText().toString().equals(title.getText().toString())){
                    return true;
                }
            }
        }
        return false;
    }
    
   @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        // TODO Auto-generated method stub
        showing = true;
        super.showAtLocation(parent, gravity, x, y);
    }
    private void createMenu(){
        mLayout.removeAllViews();
        for(Integer key:mMenuMap.keySet()){
            MenuItem menu = mMenuMap.get(key);
            
            mLayout.addView(menu.getMenuItem(), menu.getPosition());
        }
    }
    
    private void removeMenuInternal(MenuItem menu){
        View menuView = menu.getMenuItem();
        if(menuView != null){
            mLayout.removeView(menuView);
        }
    }
    public View getView(int layoutRes,int menuId,CharSequence menuTitle){
        View view = mInflater.inflate(layoutRes, null);
        TextView title = (TextView)view.findViewById(R.id.aurora_menu_item_text);
       // ImageView icon = (ImageView)view.findViewById(R.id.aurora_menu_item_icon);
        if(!TextUtils.isEmpty(menuTitle)){
            if(title != null){
                title.setText(menuTitle);
            }
        }
        
        return view;
    }
    
    
   
    
   public class MenuItem{
        private int menuId;
        private int position;
        private View menuItem;
        
        public int getMenuId() {
            return menuId;
        }
        public void setMenuId(int menuId) {
            this.menuId = menuId;
        }
        public View getMenuItem() {
            return menuItem;
        }
        public void setMenuItem(View menuItem) {
            this.menuItem = menuItem;
        }
        public int getPosition() {
            return position;
        }
        public void setPosition(int position) {
            this.position = position;
        }
        public void setOnClickListener(final OnMenuItemClickLisener listener){
            menuItem.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if(listener != null){
                        listener.onItemClick(v);
                    }
                    dismiss();
                }
            });
        }
        
    }
}