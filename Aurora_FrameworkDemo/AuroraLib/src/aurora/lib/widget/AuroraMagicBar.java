/************************************************************
 *
 * FileName       : AuroraMagicBar.java
 * Version Number : 1.0
 * Description    : a widget of buttons.
 * Author         : Daizhimin
 * Date           : 2013-6-28
 * History        :( ID,     Date,      Author, Description)
 ************************************************************/

package aurora.lib.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.aurora.lib.R;

public class AuroraMagicBar extends RelativeLayout implements OnClickListener,OnItemClickListener,
        OnTouchListener ,OnLongClickListener{
    private String TAG = "AuroraMagicBar" ;
    private Context mContext;
    private TextView[] mMagicItemTitle;
    private ImageView[] mMagicItemIcon;
    private String[] mMenuTitle,mListItems;
    private Drawable[] mMenuIcon;
    private Drawable mBackground ;
    private LinearLayout mTransparentLayout ;
    private ListView mMagicListView ;
    private onOptionsItemSelectedListener mListener;
    private onMoreItemSelectedListener mMoreListener;
    private onOptionsItemLongClickListener mLongClickListener ;
    private OnTransparentTouchListener mTouchListener ;
    private List<MenuItem>mMenuItems = new ArrayList(0),mMenuTitleItemList = new ArrayList(0),
                           mListItemList = new ArrayList(0);
    private String mWidgetArray[] ;
    private boolean mHaveData = false;
    private int mItemCount;
    private int mListItemCount;
    public final int mMoreClick = -100;
    private int mItemHeight ;
    private int mListItemHeight ;
    private int mTitleModeHeight ;
    private int mMaxListViewheight;
    private int mListViewHorizontalMargin , mListViewBottomMargin ;
    private boolean mExpand = false ;
    private List<Map<String, Object>> myData = new ArrayList<Map<String, Object>>();
    // Gionee <lihq> <2013-8-21> modify for CR00864938 begin
    private static final int MAX_ICON_SIZE = 60; //dp
    // Gionee <lihq> <2013-8-21> modify for CR00864938 end
    private int mMaxIconSize;

    /**
    * Interface definition for a callback to be invoked when click Item
    */
    public interface onOptionsItemSelectedListener {
        boolean onOptionsItemSelected(MenuItem menuItem);
    }

    public interface onOptionsItemLongClickListener {
        boolean onOptionsItemLongClick(MenuItem menuItem);   
    }

    public interface OnTransparentTouchListener {
        boolean OnTransparentTouch(View v, MotionEvent event);
    }

    public interface onMoreItemSelectedListener {
        boolean onMoreItemSelected(View view);
    }


    public AuroraMagicBar(Context context) {
        super(context);
        init(context,null);
        Log.e("Dazme","AuroraMagicBar1");
       
    }

    public AuroraMagicBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
        Log.e("Dazme","AuroraMagicBar");

    }

    private void init(Context context, AttributeSet attrs){
        mContext = context ;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AuroraActionBar);
        mBackground = a.getDrawable(R.styleable.AuroraActionBar_aurorabackgroundSplit);
        a.recycle();
        if(mBackground != null) {
            Log.e("Dazme","setBackground");
            setBackground(mBackground);           
        } 

        Resources resources = context.getResources();
        mItemHeight = (int)resources.getDimension(R.dimen.aurora_magicbar_item_height);
        mListItemHeight = (int)resources.getDimension(R.dimen.aurora_magicbar_list_item_height);
        mTitleModeHeight = (int)resources.getDimension(R.dimen.aurora_magicbar_title_mode_height);
        mMaxListViewheight = (int)resources.getDimension(R.dimen.aurora_magicbar_max_listview_height);
        mListViewHorizontalMargin = (int)resources.getDimension(R.dimen.aurora_magicbar_listview_left_right_margin);
        mListViewBottomMargin = (int)resources.getDimension(R.dimen.aurora_magicbar_listview_bottom_margin); 
        final float density = resources.getDisplayMetrics().density;
        // Gionee <lihq> <2013-8-20> modify for CR00850605 begin
        //mMaxIconSize = (int) (MAX_ICON_SIZE * density + 0.5f);  
        mMaxIconSize = (int) (MAX_ICON_SIZE * density + 8.5f); 
        // Gionee <lihq> <2013-8-20> modify for CR00850605 end
    }

    public int getItemHeight() {
        return mItemHeight ;
    }

    public int getListItemHeight(){
        return mListItemHeight ;
    }

    public int getTitleModeHeight(){
        return mTitleModeHeight;
    }

    /**
     * set item to the widget
     * @param titleId     the id of the title
     * @param drawbaleId  the id of the menu icon
     * @param listitem    the items in the listview
     * @param mListItemId
     * @param mMenuItemId
     */
    public void setMagicItem(String[] title,Drawable[] drawbale,String[] listitem, List mMenuTitleItem, List mListItem,List menuItems) {
        mMenuTitle = title ;
        mMenuIcon = drawbale;
        mListItems = listitem ;
        mMenuItems = menuItems;
        mMenuTitleItemList = mMenuTitleItem ;
        mListItemList = mListItem ;
        if (drawbale == null || drawbale.length == 0 ) {

            mItemCount = 0 ;
            //Log.e("Dazme","drawbale mItemCount1:"+mItemCount);
        } else if ( mMenuTitleItem.size() == 0) { //drawbale.length == 1  &&
            mItemCount = 0 ;
            //Log.e("Dazme","drawbale mItemCount2:"+mItemCount);
        } else {
            mItemCount = drawbale.length ;
            //Log.e("Dazme","drawbale mItemCount3:"+mItemCount);
        }

        final Context context = getContext();
        LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.e("Dazme","drawbale mItemCount:"+mItemCount);
        switch (mItemCount) {
        case 0:
            RelativeLayout aurora_magicbar0 = (RelativeLayout) inflater.inflate(R.layout.aurora_magicbar_one_menuitem, this, false);
            findView0(aurora_magicbar0);
            handleMagicItem(title,drawbale,listitem);
            addView(aurora_magicbar0);
            break;
        case 1:
            RelativeLayout aurora_magicbar1 = (RelativeLayout) inflater.inflate(R.layout.aurora_magicbar_one_menuitem, this, false);
            findView1(aurora_magicbar1);
            handleMagicItem(title,drawbale,listitem);
            addView(aurora_magicbar1);
            break;
        case 2:
            RelativeLayout aurora_magicbar2 = (RelativeLayout) inflater.inflate(R.layout.aurora_magicbar_two_menuitem, this, false);
            findView2(aurora_magicbar2);
            handleMagicItem(title,drawbale,listitem);
            addView(aurora_magicbar2);
            break;
        case 3:
            RelativeLayout aurora_magicbar3 = (RelativeLayout) inflater.inflate(R.layout.aurora_magicbar_three_menuitem, this, false);
            findView3(aurora_magicbar3);
            handleMagicItem(title,drawbale,listitem);
            addView(aurora_magicbar3);
            break;
        case 4:
            RelativeLayout aurora_magicbar4 = (RelativeLayout) inflater.inflate(R.layout.aurora_magicbar_four_menuitem , this, false);
            findView4(aurora_magicbar4);
            handleMagicItem(title,drawbale,listitem);
            addView(aurora_magicbar4);
            break;

        default:
            break;
        }

    }

    private void findView0(RelativeLayout aurora_magicbar) {
        mTransparentLayout = (LinearLayout)aurora_magicbar.findViewById(R.id.aurora_transparent_bar);
        mMagicItemIcon = new ImageView[2];
        mMagicItemIcon[0] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item1_icon5);

        mMagicItemIcon[0].setScaleType(ScaleType.CENTER);

        mMagicListView = (ListView)aurora_magicbar.findViewById(R.id.aurora_item1_listview);
    }

    private void findView1(RelativeLayout aurora_magicbar) {
        mTransparentLayout = (LinearLayout)aurora_magicbar.findViewById(R.id.aurora_transparent_bar);
        mMagicItemTitle = new TextView[1];
        mMagicItemTitle[0] = (TextView)aurora_magicbar.findViewById(R.id.aurora_item1_title1);

        mMagicItemIcon = new ImageView[2];
        mMagicItemIcon[0] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item1_icon1);
        mMagicItemIcon[1] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item1_icon5);

        mMagicItemIcon[1].setScaleType(ScaleType.CENTER);

        mMagicListView = (ListView)aurora_magicbar.findViewById(R.id.aurora_item1_listview);
    }

    private void findView2(RelativeLayout aurora_magicbar) {
        mTransparentLayout = (LinearLayout)aurora_magicbar.findViewById(R.id.aurora_transparent_bar);
        mMagicItemTitle = new TextView[2];
        mMagicItemTitle[0] = (TextView)aurora_magicbar.findViewById(R.id.aurora_item2_title1);
        mMagicItemTitle[1] = (TextView)aurora_magicbar.findViewById(R.id.aurora_item2_title2);

        mMagicItemIcon = new ImageView[3];
        mMagicItemIcon[0] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item2_icon1);
        mMagicItemIcon[1] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item2_icon2);
        mMagicItemIcon[2] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item2_icon5);

        mMagicItemIcon[2].setScaleType(ScaleType.CENTER);

        mMagicListView = (ListView)aurora_magicbar.findViewById(R.id.aurora_item2_listview);
    }

    private void findView3(RelativeLayout aurora_magicbar) {
        mTransparentLayout = (LinearLayout)aurora_magicbar.findViewById(R.id.aurora_transparent_bar);
        mMagicItemTitle = new TextView[3];
        mMagicItemTitle[0] = (TextView)aurora_magicbar.findViewById(R.id.aurora_item3_title1);
        mMagicItemTitle[1] = (TextView)aurora_magicbar.findViewById(R.id.aurora_item3_title2);
        mMagicItemTitle[2] = (TextView)aurora_magicbar.findViewById(R.id.aurora_item3_title3);


        mMagicItemIcon = new ImageView[4];
        mMagicItemIcon[0] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item3_icon1);
        mMagicItemIcon[1] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item3_icon2);
        mMagicItemIcon[2] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item3_icon3);
        mMagicItemIcon[3] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_item3_icon5);

        mMagicItemIcon[3].setScaleType(ScaleType.CENTER);

        mMagicListView = (ListView)aurora_magicbar.findViewById(R.id.aurora_item3_listview);
    }

    private void findView4(RelativeLayout aurora_magicbar) {
        mTransparentLayout = (LinearLayout)aurora_magicbar.findViewById(R.id.aurora_transparent_bar);
        mMagicItemTitle = new TextView[4];
        mMagicItemTitle[0] = (TextView)aurora_magicbar.findViewById(R.id.aurora_title1);
        mMagicItemTitle[1] = (TextView)aurora_magicbar.findViewById(R.id.aurora_title2);
        mMagicItemTitle[2] = (TextView)aurora_magicbar.findViewById(R.id.aurora_title3);
        mMagicItemTitle[3] = (TextView)aurora_magicbar.findViewById(R.id.aurora_title4);

        mMagicItemIcon = new ImageView[5];
        mMagicItemIcon[0] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_icon1);
        mMagicItemIcon[1] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_icon2);
        mMagicItemIcon[2] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_icon3);
        mMagicItemIcon[3] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_icon4);
        mMagicItemIcon[4] = (ImageView)aurora_magicbar.findViewById(R.id.aurora_icon5);
        mMagicItemIcon[4].setScaleType(ScaleType.CENTER);

        mMagicListView = (ListView)aurora_magicbar.findViewById(R.id.aurora_listview);
    }

    private void handleMagicItem(String[] title,Drawable[] drawbale,String[] listitem) {
        for (int i=0 ; i< mItemCount ; i++) {
           mMagicItemIcon[i].setImageDrawable(zoomIcon(drawbale[i]));
            //mMagicItemIcon[i].setImageDrawable(drawbale[i]);
            mMagicItemIcon[i].setVisibility(View.VISIBLE);
            mMagicItemIcon[i].setScaleType(ScaleType.CENTER);
            mMagicItemIcon[i].setEnabled(true);
            mMagicItemTitle[i].setText(title[i]);
            mMagicItemTitle[i].setAlpha(1.0f);
            if (!mMenuTitleItemList.get(i).isEnabled()) {
                mMagicItemIcon[i].setEnabled(false);
                mMagicItemTitle[i].setAlpha(0.3f);
            } 
            mMagicItemIcon[i].setTag(mMenuTitleItemList.get(i).getItemId());
            mMagicItemIcon[i].setOnClickListener(this);
            mMagicItemIcon[i].setOnLongClickListener(this);


        }
        mMagicItemIcon[mItemCount].setTag(mMoreClick);
        mMagicItemIcon[mItemCount].setOnClickListener(this);
        mMagicListView.setOnItemClickListener(this);
        mTransparentLayout.setOnTouchListener(this);
        if (listitem == null) {
            mHaveData = false ;
        } else {
            mListItemCount = listitem.length;
            myData.clear();
            setAdapter(listitem);
        }
        setListViewMaxHeight();
     
    }

    private Drawable zoomIcon(Drawable icon) {
        if (icon != null) {
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if(width > mMaxIconSize || height > mMaxIconSize ) {
                Bitmap bm = drawableToBitmap(icon);
                /*Rect rect = new Rect(0,0,width,height); 
                
                if (width > mMaxIconSize) {
                    final float scale = (float) mMaxIconSize / width;
                    width = mMaxIconSize;
                    height *= scale;
                }
                if (height > mMaxIconSize) {
                    final float scale = (float) mMaxIconSize / height;
                    height = mMaxIconSize;
                    width *= scale;
                }
                
                RectF rectf = new RectF(0,0,288,288); 
                Bitmap.Config config = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
                : Bitmap.Config.RGB_565;  
                Bitmap newBitmap = Bitmap.createBitmap(288, 288, config); 
                Canvas canvas = new Canvas(newBitmap);
                canvas.drawBitmap(bm,rect,rectf,null); */
                Drawable drawable = new BitmapDrawable(bm);    
                return drawable ;       
            }
            return icon ;
     
        }
        return null ;
    }

    private Bitmap drawableToBitmap(Drawable drawable){
        int w = drawable.getIntrinsicWidth();  
        int h = drawable.getIntrinsicHeight();  
  
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
                : Bitmap.Config.RGB_565;  
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);  
        Canvas canvas = new Canvas(bitmap);  
        drawable.setBounds(0, 0, w, h);  
        drawable.draw(canvas);  
        return bitmap; 
    }

    private void setListViewMaxHeight(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(mListItemCount > 3) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mMaxListViewheight);
                params.leftMargin = mListViewHorizontalMargin;
                params.rightMargin = mListViewHorizontalMargin;
                params.bottomMargin = mListViewBottomMargin;
                params.topMargin = mListViewBottomMargin;
                mMagicListView.setLayoutParams(params);  
            }

        }else {
            if(mListItemCount >= 5) {
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mMaxListViewheight);
                params1.leftMargin = mListViewHorizontalMargin;
                params1.rightMargin = mListViewHorizontalMargin;
                params1.bottomMargin = mListViewBottomMargin;
                params1.topMargin = mListViewBottomMargin;
                mMagicListView.setLayoutParams(params1);        
            }
        }       
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean visible = false ;
        switch (event.getAction()) {

        case MotionEvent.ACTION_DOWN:
            if ((mHaveData && mMagicListView.getVisibility() == View.VISIBLE) || (!mHaveData && mMagicItemTitle[0] !=null
                    && mMagicItemTitle[0] .getVisibility() == View.VISIBLE)) {
                if (mHaveData) {
                    visible = (mMagicListView.getVisibility() == View.VISIBLE) ;
                } else {
                    visible = (mMagicItemTitle[0] .getVisibility() == View.VISIBLE);
                }
                mTouchListener.OnTransparentTouch( v, event);
                return visible ;
            }
            break;


        default:

            break;
        }
        return false ;


    }


    /**
     * Register a callback to be invoked when click one Item
     * @param l
     * The callback that will run
     */
    public void setonTransparentTouchListener(OnTransparentTouchListener l) {
        mTouchListener = l;
    }

    public void setonOptionsItemSelectedListener(onOptionsItemSelectedListener l) {
        mListener = l;
    }

    public void setonMoreItemSelectedListener(onMoreItemSelectedListener l) {
        mMoreListener = l;
    }

    public void setonOptionsItemLongClickListener(onOptionsItemLongClickListener l ){
        mLongClickListener = l ;
    }


    @Override
    public void onClick(View v) {
        if (mListener != null && (Integer)v.getTag() != mMoreClick) {
            for (int i=0; i<mMenuTitleItemList.size(); i++) {
                if (mMenuTitleItemList.get(i).getItemId()== (Integer)v.getTag()) {
                    if (!mMenuTitleItemList.get(i).isEnabled()) {  
                        break ;
                    }
                    mListener.onOptionsItemSelected(mMenuTitleItemList.get(i));
                    break ;
                }
            }
        }

        if (mMoreListener != null) {
            mMoreListener.onMoreItemSelected(v);
        }

    }

    @Override
    public boolean onLongClick(View v) {
        if (mLongClickListener != null ) {
            for (int i=0; i<mMenuTitleItemList.size(); i++) {
                if (mMenuTitleItemList.get(i).getItemId()== (Integer)v.getTag()) {
                    if (!mMenuTitleItemList.get(i).isEnabled()) {
                        break ;
                    }
                    mLongClickListener.onOptionsItemLongClick(mMenuTitleItemList.get(i));
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mListener != null) {
            mListener.onOptionsItemSelected(mListItemList.get(arg2));

        }

    }

    public boolean isHaveData() {
        // TODO Auto-generated method stub
        return mHaveData;
    }

    public void setAdapter(String[] string) {
        mWidgetArray = string ;
        //initData();
        setAdapter();

    }

    private void initData() {
        for (int i =0 ; i<mWidgetArray.length; i++) {
            Map<String, Object> temp = new HashMap<String, Object>();
            temp.put("title", mWidgetArray[i]);
            myData.add(temp);
        }

    }

    private void setAdapter() {
        BaseAdapter baseAdapter = new myBaseAdapter();
        mMagicListView.setAdapter(baseAdapter);
        mHaveData = true;


    }

    public void setOnlyMenuItemTitleVisibility(int visibility) {
        for (int i=0 ; i< mItemCount ; i++) {
            mMagicItemTitle[i].setVisibility(visibility);
        }
        if(View.VISIBLE == visibility) {
            mExpand = true ;
        }else {
            mExpand = false ;
        }

    }

    public void setMenuListVisibility(int visibility) {
        for (int i=0 ; i< mItemCount ; i++) {
            mMagicItemTitle[i].setVisibility(visibility);
        }
        if (mMagicListView == null) {
            return ;
        }
        mMagicListView.setVisibility(visibility);
        if(View.VISIBLE == visibility) {
            mExpand = true ;
        }else {
            mExpand = false ;
        }
        //mTransparentLayout.setVisibility(visibility);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        mMaxListViewheight = (int)mContext.getResources().getDimension(R.dimen.aurora_magicbar_max_listview_height);
        if(mMenuItems != null && mMenuItems.size() > 0 ) {
            removeAllViews();
            setMagicItem(mMenuTitle,mMenuIcon,mListItems,mMenuTitleItemList,mListItemList,mMenuItems);
            if(mHaveData) {
                if(mExpand){
                    setMenuListVisibility(View.VISIBLE);    
                }

            }else {
                if(mExpand){
                    setOnlyMenuItemTitleVisibility(View.VISIBLE) ;   
                }    
            }
        }

    }

    class myBaseAdapter extends BaseAdapter {

        public myBaseAdapter() {
            //super(context, data, resource, from, to);
            // TODO Auto-generated constructor stub
        }
        @Override    
        public int getCount() {    
            return mWidgetArray.length;    
        }    
     
        @Override    
        public Object getItem(int position) {    
            return null;    
        }    
     
        @Override    
        public long getItemId(int position) {    
            return 0;    
        }

        @Override
        public boolean isEnabled(int position) {
            if (!mListItemList.get(position).isEnabled()) {
                return false ;
            }

            return super.isEnabled(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            Holder holder;
            if (null==convertView) {
                holder=new Holder();
                convertView=LayoutInflater.from(mContext).inflate(R.layout.aurora_magicbar_listview_item, null);
                holder.textView=(TextView)convertView.findViewById(R.id.aurora_magic_listitem_textview);
                holder.checkBox=(CheckBox)convertView.findViewById(R.id.aurora_magic_listitem_checkbox);
                convertView.setTag(holder);
            }
            else {
                holder=(Holder)convertView.getTag();
            }
            holder.textView.setText(mWidgetArray[position]);
            holder.textView.setAlpha(1.0f);
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false); 
            if (!mListItemList.get(position).isEnabled()) {
                holder.textView.setAlpha(0.3f);
            }
            if(mListItemList.get(position).isCheckable()) {
                holder.checkBox.setVisibility(View.VISIBLE);
                if(mListItemList.get(position).isChecked()) {
                    holder.checkBox.setChecked(true);
                } else {
                    holder.checkBox.setChecked(false);   
                }
            }

            return convertView;
        }
        class Holder {
            public TextView textView;
            public CheckBox checkBox;

        }

    }

}
