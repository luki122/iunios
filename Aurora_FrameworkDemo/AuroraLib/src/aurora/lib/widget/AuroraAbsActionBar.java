package aurora.lib.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public abstract class AuroraAbsActionBar extends LinearLayout {

    
    private static final int SAME_ITEM = 0;
    
    private static final int NOT_SAME_ITEM = 1;
    
    private static final int MAX_ITEM = 4;
    
    private int mActionBarLayoutRes;
    
    
    private int mHomeIconRes;
    
    private int mMiddleLayoutRes;
    
    private int mOptionLayoutRes;
    
    private int mCustomTitleLayoutRes;
    
    
    
    private CharSequence mTitleStr;
    
    private CharSequence mSubTitleStr;
    
    
    protected LayoutInflater mInflater;
    
    private Context mContext;
    
    
    
    
    protected TextView mTitleView;
    
    protected TextView mSubTitleView;
    
    protected ImageButton mHomeIcon;
    
    
    protected LinearLayout mMiddleLayout;
    
    protected LinearLayout mOptionLayout;
    
    protected LinearLayout mCusomTitleLayout;
    
    protected FrameLayout mBottomLayout;
    
    
    
    private ArrayList<Item> mItems;
   

    public AuroraAbsActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mItems = new ArrayList<AuroraAbsActionBar.Item>();
        // TODO Auto-generated constructor stub
    }
    
    
    
    public int getCustomlayoutRes(){
        return mActionBarLayoutRes;
    }
    
    
    public void setMiddleLayout(int resLayout){
        this.mMiddleLayoutRes = resLayout;
    }

    public int getMiddleLayout(){
        return mMiddleLayoutRes;
    }
    
    public void setOptionLayout(int resLayout){
        this.mOptionLayoutRes = resLayout;
    }
    
    
    public void setCustomLayout(int resLayout){
        this.mActionBarLayoutRes = resLayout;
    }
    
    public int getOptionLayout(){
        return mOptionLayoutRes;
    }
    
    public void setCustomTitleLayout(int resLayout){
        mCustomTitleLayoutRes = resLayout;
    }
    
    public int getCustomLayout(){
        return mCustomTitleLayoutRes;
    }
    
    
    public void setTitle(CharSequence title){
        this.mTitleStr = title;
    }
    public CharSequence getTitle(){
        return mTitleStr;
    }
    
    public void setSubTitle(CharSequence subTitle){
        this.mSubTitleStr = subTitle;
    }
    
    public CharSequence getSubTitle(){
        return mSubTitleStr;
    }
    
    /**
     * add item for action bar,max number of item is 4,but it did not working
     * @param item
     */
    void addItem(Item item){
        if(item == null){
            return;
        }
        if(mItems.size() == 0){
            mItems.add(item);
        }else if(mItems.size()>MAX_ITEM){
            return;
        }else{
            int id = item.getItemId();
            for(int i = 0;i<mItems.size();i++){
                Item tempItem = mItems.get(i);
                if(tempItem.compareTo(item) == SAME_ITEM){
                   return;
                }
            }
            mItems.add(item);
        }
    }
    
    /**
     * get item by id
     * @param id
     * @return Item
     */
    public Item getItem(int id){
        Item item = null;
        if(id < 0){
            throw new IllegalArgumentException(
                    "item's id must be positive number");
        }
        if(mItems.size() >0){
            for(int i = 0;i < mItems.size();i++){
                if(mItems.get(i).getItemId() == id){
                    item = mItems.get(i);
                    break;
                }
            }
        }
        
        return item;
    }
    
    protected List<Item> getItems(){
        return mItems;
    }
    /**
     * action bar option Item
     * @author luofu
     *
     */
    public  class Item implements Comparable<Item>{
        private int mId;
        private Drawable mIcon;
        
        public  void setIcon(Drawable icon){
            mIcon = icon;
            
        }
        
        public  void setId(int id){
            mId = id;
        }
        
        
        public int getItemId(){
            return mId;
        }

        @Override
        public int compareTo(Item item) {
            // TODO Auto-generated method stub
            if(item.getItemId() == mId){
                return SAME_ITEM;
            }
            return NOT_SAME_ITEM;
        }
        
    }
    
    /**
     * action bar item click listener
     * @author luofu
     *
     */
    public interface OnItemClickListener{
        
        public void itemClick(Item item,int id);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
