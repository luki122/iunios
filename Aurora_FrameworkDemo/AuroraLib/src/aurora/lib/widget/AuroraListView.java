package aurora.lib.widget;

import javax.crypto.Mac;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.aurora.internal.view.menu.AuroraContextMenuBuilder;

import android.view.MotionEvent;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.widget.AbsListView;
import android.util.Log;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.ViewConfiguration;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.widget.CheckBox;

import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.LayoutInflater;
import android.graphics.PixelFormat;
import android.view.animation.DecelerateInterpolator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.view.animation.DecelerateInterpolator;

import com.aurora.lib.R;
import com.aurora.lib.utils.DensityUtil;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.widget.ListAdapter;
import android.os.SystemClock;
import com.aurora.*;
//import aurora.widget.AuroraOverScrollFling;

public class AuroraListView extends ListView {

	private Context mContext;
	private OnCreateContextMenuListener mContextMenuListener;
    private Fragment mFragment;
    
	private int mTouchSlop;
	
	
	private static void log(String s)
	{
		if(DEBUG)
			Log.e(TAG,s);
	}
	
	public AuroraListView(Context context){
		this(context, null);
	}

	public AuroraListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	    init(context);
	    initAttrs(context, attrs);
	}
	
	public AuroraListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	    init(context);
	    initAttrs(context, attrs);
	}
	
	private void init(Context context)
	{
		//set a transparent selector
		auroraTansparentDrawable = new ColorDrawable(Color.TRANSPARENT);
		
		setOnScrollListener(null);
		
		auroraSetTouchMode(AURORA_TOUCH_MODE_IDLE);
		
		auroraSelectorDrawable = getResources().getDrawable(R.drawable.aurora_list_selector_light);
		
		setSelector(auroraSelectorDrawable);
		
		//contacts used this as background
		auroraPressedDrawable = getResources().getDrawable(R.drawable.aurora_list_pressed_light);
		
		AURORA_LIST_CHECKBOX_WIDTH =  getResources().getDimensionPixelSize(R.dimen.aurora_list_cb_width);
		
		AURORA_DRAG_START_WIDTH = DensityUtil.dip2px(context, 10.0f);
		
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		
        mTouchSlop = configuration.getScaledTouchSlop();
		
        //init sort item informations
        initSortItem();
	}
	
	private void initAttrs(Context context,AttributeSet attrs)
	{
		boolean enableOverScroll = false;
		
		TypedArray a = context.obtainStyledAttributes(attrs,
               R.styleable.AuroraListView, 0, R.style.AuroraListViewStyle);
        
        int n = a.getIndexCount();
	
        for(int i = 0; i < n; i++)
        {
			int attr = a.getIndex(i);    
			    
			switch(attr)
			{
				case R.styleable.AuroraListView_auroraListViewheaderShadowOffsetX:
				
					auroraHeaderViewXOffset = a.getInteger(attr,0);
					
					break;
				
				case R.styleable.AuroraListView_auroraListViewheaderShadowOffsetY:
				
					auroraHeaderViewYOffset = a.getInteger(attr,0); 
					
					break;
				
				case R.styleable.AuroraListView_auroraShowRubbishIcon:
					
					auroraShowRubbishIcon = a.getBoolean(attr,true);
					
					break;
				
				case R.styleable.AuroraListView_auroraListViewOverScroll:
					
					enableOverScroll = a.getBoolean(attr,false);
					
					break;	
				case R.styleable.AuroraListView_auroraListViewSlideRight:
					
					mSlideRight = a.getBoolean(R.styleable.AuroraListView_auroraListViewSlideRight,false);
					
					break;		
				case R.styleable.AuroraListView_auroraListViewSortItem:
					
					mSortEnable = a.getBoolean(R.styleable.AuroraListView_auroraListViewSortItem,false);
					
					break;			
				default:
					break;
			}
		}
  
        a.recycle();
       
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        
        auroraEnableOverScroll(enableOverScroll);
        
	}
	
	@Override
	public void setOnItemLongClickListener(final android.widget.AdapterView.OnItemLongClickListener listener) {
	
		
			android.widget.AdapterView.OnItemLongClickListener wrapListener = new android.widget.AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				if(auroraOverScrolling())return true;
				
				if(auroraGetTouchMode() != AURORA_TOUCH_MODE_DRAG && auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL){
					if (listener == null || !listener.onItemLongClick(parent, view, position, id)) {
							showContextMenuDialog(view, position, id);
					}
				}
				return true;
			}
				
			};
			super.setOnItemLongClickListener(wrapListener);
			
		}


    private void showContextMenuDialog(View view, int position, long id) {
        ContextMenuInfo menuInfo = createContextMenuInfo(view, position, id);
        final AuroraContextMenuBuilder menu = new AuroraContextMenuBuilder(mContext);
        menu.setCurrentMenuInfo(menuInfo);
        menu.setFragment(mFragment);
        if (mContextMenuListener != null) {
            mContextMenuListener.onCreateContextMenu(menu, this, menuInfo);
        }

        int size = menu.size();
        if (size > 0) {
            menu.show(this, null);
        }
    }

    // Gionee <fenglp> <2013-07-24> add for CR00812456 begin
	@Override
	public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
	    mContextMenuListener = l;
	    setOnItemLongClickListener(getOnItemLongClickListener());
	}
	
    ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
    }
	// Gionee <fenglp> <2013-07-24> add for CR00812456 end

    // Gionee <fenglp> <2013-07-31> add for CR00812456 begin
    public void registerFragmentForContextMenu(Fragment fragment){
        mFragment = fragment;
    }
    // Gionee <fenglp> <2013-07-31> add for CR00812456 end
	


    /**************************************************************************************************
     * 
     * 
     * 				slide delete fuction start
     * 				flash add
     * 
     * 
     *************************************************************************************************/
    private static final String TAG = "AuroraListView";
	
	private static final boolean DEBUG = false;


	private final int AURORA_INVALIDATE = -1;
	
	
	private int AURORA_DRAG_START_WIDTH;
	
	private final int AURORA_TOUCH_MODE_IDLE = 0;
	private final int AURORA_TOUCH_MODE_DOWN = 1;
	private final int AURORA_TOUCH_MODE_DRAG = 2;
	private final int AURORA_TOUCH_MODE_TAP = 3;
	private final int AURORA_TOUCH_MODE_LONGPRESS = 4;
	private final int AURORA_TOUCH_MODE_SCROLLY = 5;
	//record touch mode 
	private int auroraTouchMode;
	
	/**
	 * no slide
	 */
	private final int AURORA_ITEM_SHOW_NORMAL = 0;
	
	/**
	 * slided to left 
	 */
	private final int AURORA_ITEM_SHOW_DELETE = 1; 
	
	/**
	 * slided to right
	 */
	private final int AURORA_ITEM_SHOW_LEFT_REGION = 2;
	
	private int auroraItemState = AURORA_ITEM_SHOW_NORMAL;
	
	private boolean auroraScrolling = false;
	
	private boolean auroraIsNeedOpenSlideItemFuction = false;
	
	private Point auroraDowmPoint = new Point();
	
	private int mPointer_id = AURORA_INVALIDATE;
	
	private int auroraMotionPosition = AURORA_INVALIDATE;
	
	//the selected view 
	private View auroraChild = null;
	 
	//content layout 
	private static LinearLayout auroraChildContent = null;
	
	//second layout 
	private LinearLayout auroraChildRubbish = null;
	
	private FrameLayout auroraCustomFront = null;
	
	//item content layout
	private LinearLayout auroraContent = null;
	
	//draged item distance
	private int auroraDistance = AURORA_INVALIDATE;

	// create a handle
	private Handler auroraHandler = new Handler();

	private ImageView auroraRubbish = null;
	
	private AnimationDrawable auroraRubbishAnimDrawable = null;
	
	private boolean auroraClickRubbish= false;
	
	private boolean auroraOnPaused = false;
	
	private final int AURORA_RUBBISH_DRAWABLE_NUMBERS = 17;
	
	//2014.2.17 add, listen if touched headerView
	private boolean auroraTouchedHeaderView;
	
	private int auroraRubbishLayoutWidth = -1;
	
	private static boolean auroraEnableSelector = true;
	
	//255 mean opaque
	private static final int AURORA_OPAQUE_ALPHA = 255;
	
	//0 means all fully tansparent
	private static final int AURORA_TANSPARENT_ALPHA = 0;
	
	// tansparent drawable instance
	private static Drawable auroraTansparentDrawable;
	
	private ValueAnimator auroraSlidingAnim;
	
	private boolean auroraShowRubbishIcon;
	
	private boolean auroraEnableOverScroll;
	
	private int mX, mY, mLastX, mLastY;
	
	/**
	 * the switch of slide right
	 */
	private boolean mSlideRight = false;
	
	/**
	 * no sliding
	 */
	private final int NO_SLIDING = 0;
	
	/**
	 * start sliding left fuction
	 */
	private final int SLIDING_LEFT = 1;
	
	/**
	 * start sliding right fuction
	 */
	private final int SLIDING_RIGHT = 2;
	
	/**
	 * sliding down
	 */
	private final int SLIDING_DOWN = 3;
	
	/**
	 * sliding up
	 */
	private final int SLIDING_UP = 4;
	
	/**
	 * sorting 
	 */
	private final int SLIDING_SORT = 5;
	
	/**
	 * different sliding means different Branch
	 * whenever just one state !!!
	 */
	private int mSlidingState = NO_SLIDING;
	
	public  interface AuroraBackOnClickListener{
		void auroraOnClick(int position);
		
		// give a fuction to user , do something before start draging
		void auroraPrepareDraged(int position);

		// successed draged out delete layout 
		void auroraDragedSuccess(int position);
		
		// draged delete layout but not successed
		void auroraDragedUnSuccess(int position);
	}
	
	public  interface AuroraOnSizeChangedListener{
		
		void auroraOnSizeChanged(int newWidth,int newHeight,int oldWidth,int oldHeight);

	}
	
	private AuroraBackOnClickListener auroraBackOnClickListener = null;
	
	private AuroraOnSizeChangedListener auroraOnSizeChangedListener = null;
	
	public void auroraSetAuroraBackOnClickListener(AuroraBackOnClickListener l)
	{
		auroraBackOnClickListener = l;
	}
	
	public void auroraSetAuroraOnSizeChangedListener(AuroraOnSizeChangedListener l)
	{
		auroraOnSizeChangedListener = l;
	}
	
	//set touch mode
	private void auroraSetTouchMode(int mode)
	{
		auroraTouchMode = mode;
	}
	
	//get touch mode
	private int auroraGetTouchMode()
	{
		return auroraTouchMode;
	}
	
	private OnScrollListener auroraWrapListener = null;
	
	@Override
	public void setOnScrollListener(final OnScrollListener l)
	{
		auroraWrapListener = new OnScrollListener(){

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
			if(l != null)
				l.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				
			auroraUpdateAuroraHeaderView();
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
			if(l != null)
				l.onScrollStateChanged(view, scrollState);
			
			if(scrollState == OnScrollListener.SCROLL_STATE_IDLE)
			{
				
				if(auroraIsFirstItemOnTopContainHeaderView())
					auroraHideHeaderView();

				auroraScrolling = false;
			}
			else
			{
				auroraScrolling = true;
				
				auroraSetRubbishBack();
				
				auroraSetTouchMode(AURORA_TOUCH_MODE_IDLE);
			}
			
		}
			
		};
		
		super.setOnScrollListener(auroraWrapListener);
	}
	
	private boolean onIntercept = false;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		boolean result = false;
		
		onIntercept = true;
		
		onTouchEvent(ev);
		
		onIntercept = false;
		
		//super first need intercept !!! 
		result = super.onInterceptTouchEvent(ev);
	
		//moving
		boolean moved = (Math.abs(ev.getX() - auroraDowmPoint.x) > mTouchSlop)
					|| (Math.abs(ev.getY() - auroraDowmPoint.y) > mTouchSlop);
		
	
		//if has slide fuction
		moved = moved && (isSlideRightEnable() || auroraIsNeedSlideDelete());
		
		//if sliding 
		moved = moved || (mSlidingState != NO_SLIDING);		
	
		result = result || moved ;
		Log.e("AuroraListView", "test for slide");
		return result;
	}
	

	
	@Override
	public boolean onTouchEvent( MotionEvent event) {
		
		boolean result = true;
		
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		
		auroraSaveTouchedCoords(event);
		
		auroraDoCommomThings(event);
		
		//first jarge user whether use this event
		//2014.1.15 flash add here for canlender 
		if(auroraUserHandleOnTouchEvent(event))
			return true;
			
		//slide to left
		if(auroraIsNeedSlideDelete())
		{
			AuroraOnTouchEvent(event);
		}
		
		//slide to right 
		handleSlideRight(event);
		
		//sort Item fuction
		if(mSortEnable)
        {
			auroraHanldeSortAction(event);
        }
				
		//overscroll animation
		auroraHanldeOverScrollEvent(event);
	
		//cancel event
	
		if(auroraShouldCancelEvent())
		{
			cancelEvent(event);
		}
		
		if(!onIntercept)
			result = super.onTouchEvent(event);
		
		//resume state
		if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
		{
			mSlidingState = NO_SLIDING;
		}
		
		return result;
		
	}
	
	private void cancelEvent(MotionEvent event)
    {
    	event.setAction(MotionEvent.ACTION_CANCEL);
    }
	
	/**
	 * save move point
	 */
	private void auroraSaveTouchedCoords(MotionEvent ev)
	{
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		
        if (action != MotionEvent.ACTION_DOWN) {
            mLastX = mX;
            mLastY = mY;
        }
        
        mX = (int) ev.getX();
        mY = (int) ev.getY();
        
        if (action == MotionEvent.ACTION_DOWN) {
            mLastX = mX;
            mLastY = mY;
            auroraDowmPoint.set(mX, mY);
        }        
	}
	
	private void auroraDoCommomThings(MotionEvent event) 
	{
		// TODO Auto-generated method stub
		
		int x = (int)event.getX();
		
		int y = (int)event.getY();
		
		//init headerShadow
		auroraGetAuroraListHeader();
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				//charge is touched headerView 
				auroraTouchedHeaderView = auroraSelectedItemIsHeaderView(this.pointToPosition(x,y));
				
				break;
			default:
				break;	
		}
	
	}
	
	private boolean auroraShouldCancelEvent()
	{
		return (auroraIsPlayingDeleteAnim() 
				|| auroraIsPlayingSlidingAmim() 
				|| auroraClickRubbish 
				|| auroraIsRubbishOut()
				|| auroraGetTouchMode() == AURORA_TOUCH_MODE_DRAG
				|| auroraGetAuroraItemState() != AURORA_ITEM_SHOW_NORMAL
				|| slideRightAnimationRunning()
				|| (mSlidingState == SLIDING_SORT)
			);
	}

	private boolean AuroraOnTouchEvent(MotionEvent event) 
	{
	
		int x,y;
		
		if(!(mSlidingState == SLIDING_LEFT || mSlidingState == NO_SLIDING) 
				|| auroraGetAuroraItemState() == AURORA_ITEM_SHOW_LEFT_REGION)return false;
		
		if(!(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_DELETE 
    			|| auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL))return false;
		
		//sliding right animation is running 
		if(slideRightAnimationRunning())return false;
		
		if(auroraOnPaused)return false;
		
		//if is scrolling ,not handle anything!!!
		
		log("auroraScrolling = " + auroraScrolling + ", getScrollY() = " + getScrollY());
		if(auroraScrolling || this.getScrollY() != 0)
		{
			return false;
		}
		
		//touched the headerView , headerView not allowed do AuroraOnTouchEvent
		if(auroraTouchedHeaderView)
		{
			return false;
		}	
		
		//if is playing sliding anim ,do nothing!
		if(auroraIsPlayingSlidingAmim())
		{
			//not allow Scrolling
			smoothScrollBy(0,0);
			return true;
		}
		
		if(auroraIsPlayingDeleteAnim())
		{
			return true;
		}

		/* get action */
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
	
			auroraHandleActionDown(event);
			
			break;
		
		case MotionEvent.ACTION_MOVE:
		
			auroraHandleActionMove(event);
		
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		
			auroraHandleActionUp(event);
			
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			log("MotionEvent.ACTION_POINTER_DOWN");
			
			//update auroraChildContent background
			if(auroraChildContent != null)
				auroraChildContent.setPressed(false);
	
			return true;
		case MotionEvent.ACTION_POINTER_UP:
			//solve multi finger up not update UI 
			if(AuroraIsSameFinger(event))
			{
				
				MotionEvent up = MotionEvent.obtain(event);
				up.setAction(MotionEvent.ACTION_UP);
				AuroraOnTouchEvent(up);
				up.recycle();
			}
			return true;
			default:
				break;
		}
		
		return true;
	}
	
	
	private void auroraHandleActionDown(MotionEvent event)
	{
		int x = (int) event.getX();
            
		int y = (int) event.getY();
		
		mPointer_id = event.getPointerId(0);
		
		auroraClickRubbish = false;
			
		if(auroraIsRubbishOut())
		{
			if(auroraHandleActionDownInDeleteState(x,y))
				return;
		}
		
		AuroraInitActionDownInfo(x,y);
	}
	
	public boolean auroraIsRubbishOut()
	{
		return auroraGetAuroraItemState() == AURORA_ITEM_SHOW_DELETE;
	}
	
	private boolean auroraHandleActionDownInDeleteState(int x, int y)
	{
		if(!auroraPointIsInChildRect(x,y))
		{
			//click other item
			auroraSetRubbishBack();
			return true;
		}
		else if(auroraPointIsInChildRubbishRect(x,y))
		{
			//click rubbish
			auroraClickRubbish = true;
		
			if(auroraBackOnClickListener != null && !onIntercept)
				auroraBackOnClickListener.auroraOnClick(auroraMotionPosition);
					
			return true;
		}
		else
		{
			log("pressed orignal item ");
			
			AuroraInitActionDownInfo(x,y);
			
			if(auroraChildRubbish != null)
				auroraDistance = auroraChildRubbish.getWidth();
				
			return true;
		}
	}
	

	private void AuroraInitActionDownInfo(int x, int y) {
		// TODO Auto-generated method stub

		auroraMotionPosition = pointToPosition(x, y);
		
		log("AuroraInitActionDownInfo auroraMotionPosition = " + pointToPosition(x, y));
		
		auroraSetTouchMode(AURORA_TOUCH_MODE_DOWN);
		
		/*get the selected view*/
		auroraChild = this.getChildAt(auroraMotionPosition - this.getFirstVisiblePosition());
		
		if(auroraChild != null)
		{
			/* get auroraChildContent */
			auroraChildContent = (LinearLayout)auroraChild.findViewById(R.id.content);
			
			/* get fuc layout */
			auroraChildRubbish = (LinearLayout)auroraChild.findViewById(R.id.aurora_listview_back);
			
			auroraCustomFront = (FrameLayout)auroraChild.findViewById(R.id.aurora_listview_custom_front);
			
			auroraItemSlideSwitch = (View)auroraChild.findViewById(R.id.aurora_item_sliding_switch);
		}
		
	}
	
	
	private void auroraHandleActionMove(MotionEvent event)
	{
		/* when listview is sliding ,down action maybe can not get this Item */
		if(auroraChild == null)return;
			
		log("MotionEvent.ACTION_MOVE 0 !");
		
		if(!AuroraIsSameFinger(event))return;
			
		log("MotionEvent.ACTION_MOVE 1 !");
			
		//not allow up down fling when this state
		if(auroraIsRubbishOut())smoothScrollBy(0,0);
			
		int	x = (int)event.getX(event.findPointerIndex(mPointer_id));
			
		int	y = (int)event.getY(event.findPointerIndex(mPointer_id));
			
		log("MotionEvent.ACTION_MOVE 5 !");
		
		auroraHandleSlideLogicalWhenActionMove(x,y);
	}
	
	private boolean AuroraIsSameFinger(MotionEvent event) {
		// TODO Auto-generated method stub
		
		int pointerIndex = event.findPointerIndex(mPointer_id);
		
		return (pointerIndex == -1)?false:true;
	}
	
	private void auroraHandleSlideLogicalWhenActionMove(int x,int y)
	{
		if(auroraIsItemSlideSwitchOn())
		{
			switch(auroraGetTouchMode())
			{
			case AURORA_TOUCH_MODE_DOWN:
				log("AURORA_TOUCH_MODE_DOWN 0 !");
			
				if(auroraCheckSrollX(x,y))
				{
					mSlidingState = SLIDING_LEFT;
					auroraPrepareSliding(x,y);	
				}
			
				break;
			case AURORA_TOUCH_MODE_DRAG:
		
				auroraUpdateSlidingItem(x,y);
			
				break;
			}
		}
	}
	
	private boolean auroraCheckSrollX(int x, int y) 
	{
		// AURORA_ITEM_SHOW_NORMAL  state just allow move left !
		if(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL && (x - auroraDowmPoint.x) < 0 && Math.abs(y - auroraDowmPoint.y) < Math.abs(x - auroraDowmPoint.x))
		{
			if(Math.abs(x-auroraDowmPoint.x) > AURORA_DRAG_START_WIDTH )
				return true;
		}
		else if(auroraIsRubbishOut() && Math.abs(y - auroraDowmPoint.y) < Math.abs(x - auroraDowmPoint.x))
		{
			if(Math.abs(x-auroraDowmPoint.x) > AURORA_DRAG_START_WIDTH)
				return true;
		}
		return false;
	}
	
	
	private boolean auroraCheckSrollY(int x,int y)
	{
		return Math.abs(y-auroraDowmPoint.y) > AURORA_DRAG_START_WIDTH;
	}
	
	private void auroraPrepareSliding(int x,int y)
	{
		
		auroraSetTouchMode(AURORA_TOUCH_MODE_DRAG);
		
		//client prepare drag listener
		if(auroraBackOnClickListener != null)
			auroraBackOnClickListener.auroraPrepareDraged(auroraMotionPosition);
		
		smoothScrollBy(0,0);
		
		auroraInitRubbish();
		
		auroraDowmPoint.set(x, y);
		
	}
	
	
	private void auroraInitRubbish()
	{
		if(auroraChildRubbish != null)
		{
			log("AURORA_TOUCH_MODE_DOWN 4 !");
			
			if(auroraChildRubbish.getVisibility() != View.VISIBLE)
			{
				
				log("AURORA_TOUCH_MODE_DOWN 5 !");
				
				auroraChildRubbish.setAlpha(0);
				
				auroraChildRubbish.setVisibility(View.VISIBLE);
			}
		}
		
		log("AURORA_TOUCH_MODE_DOWN 6 !");
			
		auroraInitRubbishAnim();	
		
		//get rubbish width
		auroraGetRubbishLayoutWidth();
	}
	
	private void auroraInitRubbishAnim()
	{
		log("auroraInitRubbishAnim 0 !");
		
		log("auroraChildRubbish = " + auroraChildRubbish);
		
		log("auroraGetAuroraItemState() = " + auroraGetAuroraItemState());
		
		if(auroraShowRubbishIcon && auroraChildRubbish != null && auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL)
		{
			log("auroraInitRubbishAnim 1 !");
			
			
			auroraRubbish = (ImageView)auroraChildRubbish.findViewById(R.id.aurora_rubbish);
			
			auroraRubbish.setScaleX(0.9f);
			
			auroraRubbish.setScaleY(0.9f);
			
			auroraRubbish.setImageResource(R.drawable.aurora_listview_rubbish_anim);
			
			auroraRubbishAnimDrawable = (AnimationDrawable)auroraRubbish.getDrawable();
		}
	}
	
	private int auroraGetRubbishLayoutWidth()
	{
		log("auroraGetRubbishLayoutWidth 0 !");	
		
		log("auroraRubbishLayoutWidth = " + auroraRubbishLayoutWidth);
		
		if(auroraRubbishLayoutWidth <= 0)
		{
			if(auroraChildRubbish != null)
				auroraRubbishLayoutWidth = auroraChildRubbish.getWidth();
				
		}
		
		return auroraRubbishLayoutWidth;
	}
	
	private void auroraUpdateSlidingItem(int x,int y) 
	{
		// TODO Auto-generated method stub
		
		int distance = auroraDistance = x - auroraDowmPoint.x;
		
		log("auroraUpdateItem 0");
		
		log("auroraUpdateItem auroraGetAuroraItemState() = " + auroraGetAuroraItemState());
		
		auroraUpdateItemInNormalStateDraged(distance);
		
		auroraUpdateItemInDeleteStateDraged(distance);
	}
	
	private void auroraHandleActionUp(MotionEvent event)
	{
		log("MotionEvent.ACTION_UP");
			
		if(auroraChild == null)return;
		
		if(!AuroraIsSameFinger(event))return;
		
		//does not have down message
		if(AURORA_TOUCH_MODE_IDLE == auroraGetTouchMode())return;
		
		int x = (int)event.getX(event.findPointerIndex(mPointer_id));
		
		int y = (int)event.getY(event.findPointerIndex(mPointer_id));
		
		log("MotionEvent.ACTION_UP auroraMotionPosition = " + pointToPosition(x, y));
		
		log("MotionEvent.ACTION_UP    auroraGetTouchMode() = " + auroraGetTouchMode());
		
		if(auroraUpdateRubbishSlideEnd( x, y))
			return;
			
		auroraClickRubbishItem(x,y);
	}
	
	private boolean auroraUpdateRubbishSlideEnd(int x,int y)
	{
		switch(auroraGetTouchMode())
		{
			case AURORA_TOUCH_MODE_DOWN:
				break;
			case AURORA_TOUCH_MODE_TAP:
				break;
			case AURORA_TOUCH_MODE_DRAG:
				{
					auroraHandleDragedUpResult( x, y);
					auroraSetTouchMode(AURORA_TOUCH_MODE_IDLE);
					return true;
				}
			default:
				break;
		}
		return false;
	}
	
	// sliding self anim logical 
	private void auroraHandleDragedUpResult(int x,int y)
	{
		int startValue = 0;
		int endValue = 0;
		int distance = auroraGetAuroraDistance();
		int rubbishLayoutWidth = auroraGetRubbishLayoutWidth();
		switch(auroraGetAuroraItemState())
		{
		case AURORA_ITEM_SHOW_NORMAL:
			if(Math.abs(distance) > rubbishLayoutWidth/2 && distance < 0)
			{
				startValue = distance;
				endValue = -rubbishLayoutWidth;
			}
			else if(Math.abs(distance) < rubbishLayoutWidth/2 && distance < 0)
			{
				startValue = distance;
				endValue = 0;
			}
			
			auroraStartSlideSelfAnimInNormalState( startValue, endValue);
			break;
		case AURORA_ITEM_SHOW_DELETE:

			if(distance > 0 && distance < rubbishLayoutWidth/2)
			{
				startValue = distance;
				endValue = 0;
			}
			else if(distance > 0 && distance >= rubbishLayoutWidth/2 )
			{
				startValue = distance;
				if(distance > rubbishLayoutWidth)
					startValue = rubbishLayoutWidth;
				endValue = rubbishLayoutWidth;
			}
			else if(distance < 0)
			{
				startValue = distance;
				endValue = 0;
			}
			
			auroraStartSlideSelfAnimInDeleteState( startValue, endValue);
			break;
			default:
				break;
		}
	
	}
	
	private void auroraClickRubbishItem(int x, int y)
	{
		if(auroraIsRubbishOut())
		{
			if(auroraPointIsInChildRubbishRect(x,y))
			{
				auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
				return;
			}
			else if(auroraPointIsInChildContentRect(x,y))
			{
				log("up ,pressed original item ");
				
				auroraStartSlideSelfAnimInDeleteState(0,auroraGetRubbishLayoutWidth());
				
				return;
			}
		}	
	}
	
	public void auroraSetNeedSlideDelete(boolean result)
	{
		auroraIsNeedOpenSlideItemFuction = result;
	}
	
	private boolean auroraIsNeedSlideDelete()
	{
		return auroraIsNeedOpenSlideItemFuction;
	}

	public int auroraGetAuroraDistance() {
		return auroraDistance;
	}

	public int auroraGetAuroraItemState() {
		return auroraItemState;
	}
	
	public void auroraSetAuroraItemState(int auroraItemState) {
		this.auroraItemState = auroraItemState;
	}
	
   	public void auroraOnPause()
	{
   		
		auroraListOnPause();
	}
	
	public void auroraOnResume()
	{
		auroraOnPaused = false;
		
		auroraUpdateAuroraHeaderView();
	
	}
	
	private void auroraListOnPause()
	{
		auroraOnPaused = true;
		
		auroraResetStateDragingWhenOnPause();
		
		if(!auroraIsRubbishOut())
		{
			// clear memory , auroraChildContent is static !!!
			// here has two state, one is rubbish out state ,one is normal state 
			// rubbish out state should play anim ,anim need use auroraChildContent , 
			// so I have to set null at two different place !!!
			
			//	auroraChildContent = null;
		}
		
		auroraSetRubbishBack();
		
		//hide headerView
		auroraHideHeaderView();
		
		slideRightOnPause();
	}
	
	private void auroraResetStateDragingWhenOnPause()
	{
		if(auroraGetTouchMode() == AURORA_TOUCH_MODE_DRAG)
		{
			auroraSetTouchMode(AURORA_TOUCH_MODE_IDLE);
			auroraSetAuroraItemState(AURORA_ITEM_SHOW_DELETE);
		}
	}
	
	@Override
    public boolean performItemClick(View view, int position, long id)
	{
		log("on performItemClick !!!");
		
		if(auroraIsRubbishOut())
		{
			auroraStartSlideSelfAnimInDeleteState(0,auroraGetRubbishLayoutWidth());
			return true;
		}
		
		if(auroraOverScrolling())
		{
			return true;
		}
		
		return super.performItemClick(view, position, id);

	}
	
	//not used,but maybe app used before ,so do not delete
	public void auroraSetWhetherUseSepcialLayout(boolean isSpecial)
	{
	}
	
	/****************************************************************************************
	 * 
	 * 
	 * 			checkbox anim start
	 * 
	 * 
	 ****************************************************************************************/
	public static final int AURORA_ANIM_DURATION = 300;
	
	public static int AURORA_LIST_CHECKBOX_WIDTH;
	
	private static DecelerateInterpolator auroraDecelerateInterpolator;
	
	public void auroraPreparePlayCheckboxAnim()
	{
		if(auroraGetAuroraItemState() != AURORA_ITEM_SHOW_NORMAL || auroraIsPlayingSlidingAmim())
		{
			auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
			
			if(auroraChildContent != null)
			{
			    auroraChildContent.setLeft(0);
			    
			    auroraChildContent.setRight(auroraChildContent.getRight());
			}
		}
	}
	
	public static void auroraStartCheckBoxAppearingAnim(RelativeLayout  front,CheckBox cb, boolean second)
	{
		auroraStartCheckBoxAppearingAnim(front,cb);
	}
	
	public static void auroraStartCheckBoxAppearingAnim(RelativeLayout  front,CheckBox cb)
	{
		
		final CheckBox box = cb;
		
		final RelativeLayout  mFront = front;
		
		if(box == null)return;
		
		box.setVisibility(View.VISIBLE);
		
		ObjectAnimator oa = ObjectAnimator.ofFloat(mFront, "TranslationX", 0,AURORA_LIST_CHECKBOX_WIDTH);
		
		ObjectAnimator ob = ObjectAnimator.ofFloat(box, "Alpha", 0,1);
		
		if(auroraDecelerateInterpolator == null)
		{
			auroraDecelerateInterpolator = new DecelerateInterpolator();
		}
		
		oa.setInterpolator(auroraDecelerateInterpolator);
		
		AnimatorSet set = new AnimatorSet();
		
		set.play(oa).with(ob);
		
		set.setDuration(AURORA_ANIM_DURATION);
		
		set.start();
	
	}
	
	public static void auroraStartCheckBoxDisappearingAnim(RelativeLayout  front,CheckBox cb)
	{
		final CheckBox box = cb;
		
		final RelativeLayout  mFront = front;
		
		if(box == null)return;
		
		ObjectAnimator oa = ObjectAnimator.ofFloat(mFront, "TranslationX", AURORA_LIST_CHECKBOX_WIDTH,0);
		
		ObjectAnimator ob = ObjectAnimator.ofFloat(box, "Alpha", 1,0);
		
		if(auroraDecelerateInterpolator == null)
		{
			auroraDecelerateInterpolator = new DecelerateInterpolator();
		}
		
		oa.setInterpolator(auroraDecelerateInterpolator);
		
		AnimatorSet set = new AnimatorSet();
		
		set.play(oa).with(ob);
		
		set.setDuration(AURORA_ANIM_DURATION);
		
		set.start();
		
	}
	
	public static void auroraStartCheckBoxAppearingAnim(View  front,CheckBox cb)
	{
		
		final CheckBox box = cb;
		
		final View  mFront = front;
		
		if(box == null)return;
		
		box.setVisibility(View.VISIBLE);
		
		ObjectAnimator oa = ObjectAnimator.ofFloat(mFront, "TranslationX", 0,AURORA_LIST_CHECKBOX_WIDTH);
		
		ObjectAnimator ob = ObjectAnimator.ofFloat(box, "Alpha", 0,1);
		
		if(auroraDecelerateInterpolator == null)
		{
			auroraDecelerateInterpolator = new DecelerateInterpolator();
		}
		
		oa.setInterpolator(auroraDecelerateInterpolator);
		
		AnimatorSet set = new AnimatorSet();
		
		set.play(oa).with(ob);
		
		set.setDuration(AURORA_ANIM_DURATION);
		
		set.start();
	
	}
	
	public static void auroraStartCheckBoxDisappearingAnim(View  front,CheckBox cb)
	{
		final CheckBox box = cb;
		
		final View  mFront = front;
		
		if(box == null)return;
		
		ObjectAnimator oa = ObjectAnimator.ofFloat(mFront, "TranslationX", AURORA_LIST_CHECKBOX_WIDTH,0);
		
		ObjectAnimator ob = ObjectAnimator.ofFloat(box, "Alpha", 1,0);
		
		if(auroraDecelerateInterpolator == null)
		{
			auroraDecelerateInterpolator = new DecelerateInterpolator();
		}
		
		oa.setInterpolator(auroraDecelerateInterpolator);
		
		AnimatorSet set = new AnimatorSet();
		
		set.play(oa).with(ob);
		
		set.setDuration(AURORA_ANIM_DURATION);
		
		set.start();
		
	}
	
	public static void auroraSetCheckBoxVisible(CheckBox cb, boolean visible)
	{
		if(cb == null)
			return;

		cb.setVisibility(visible?View.VISIBLE:View.GONE);
	}
	
	public static void auroraSetCheckBoxVisible(RelativeLayout  front,CheckBox cb, boolean visible)
	{
		if(cb == null || front == null)
			return;
	
		front.setTranslationX(visible?AURORA_LIST_CHECKBOX_WIDTH:0);
		
		cb.setAlpha(visible?1.0f:0.0f);
		
		cb.setVisibility(visible?View.VISIBLE:View.GONE);
		
	}
	
	public static void auroraSetCheckBoxVisible(View  front,CheckBox cb, boolean visible)
	{
		if(cb == null || front == null)
			return;
	
		front.setTranslationX(visible?AURORA_LIST_CHECKBOX_WIDTH:0);
		
		cb.setAlpha(visible?1.0f:0.0f);
		
		cb.setVisibility(visible?View.VISIBLE:View.GONE);
		
	}
	/****************************************************************************************
	 * 
	 * 
	 * 			checkbox anim end
	 * 
	 * 
	 ****************************************************************************************/
	
	
	
	
	/****************************************************************************************
	 * 
	 * 
	 * 						listheader fuction
	 * 
	 * 
	 ****************************************************************************************/

	private AuroraListHeader auroraListHeader = null;
	
	private int[] auroraLocation = new int[2]; 
	
	private Rect auroraTempRect = new Rect();
	
	private int auroraHeaderViewXOffset;
	
	private int auroraHeaderViewYOffset;
	
	private int auroraGetStatesBarHeight()
	{
		this.getWindowVisibleDisplayFrame(auroraTempRect);
		
		return auroraTempRect.top;
	}
	
	private int auroraGetListViewLeftOnScreen()
	{
		getLocationInWindow(auroraLocation);
		
		log("auroraLocation[0] = " + auroraLocation[0]);
		log("auroraLocation[1] = " + auroraLocation[1]);
		
	//	getLocationOnScreen(auroraLocation);
	
		return auroraLocation[0];
	}
	
	private int auroraGetListViewTopOnScreen()
	{
		getLocationInWindow(auroraLocation);
		
	//	getLocationOnScreen(auroraLocation);
	
		return auroraLocation[1]; 
	}
	
	public void auroraSetHeaderViewXOffset(int offsetX)
	{
		auroraHeaderViewXOffset = offsetX;
	}
	
	public void auroraSetHeaderViewYOffset(int offsetY)
	{
		auroraHeaderViewYOffset = offsetY;
	}
	
	private class AuroraListHeader 
	{
		 private WindowManager mWindowManager;
		 
		 private WindowManager.LayoutParams mLayoutParams = null;
		 
		 private Context mContext;
		 
		 private LayoutInflater mLayoutInflater;
		 
		 private LinearLayout auroraLayout;
		 
		 private boolean auroraViewAdded = false;
		 
		 private ObjectAnimator anim;
		 
		 private int auroraCheckboxWidth = 0;
		 
		 public AuroraListHeader(View host)
		 {
			 mContext = host.getContext();
			 
			 //init a imageView
			 mLayoutInflater = LayoutInflater.from(mContext);
			 
			 auroraLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.aurora_listview_header, null);

			 // initial window manager
		     mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		     
		     WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		     
		        lp.token = host.getWindowToken();//.getApplicationWindowToken();
		        
		        lp.x = 0;
		        
		        lp.y = 0;
		        
		        lp.width = LayoutParams.MATCH_PARENT;
		        
		        lp.height = 9;
		        
		        lp.gravity = Gravity.LEFT | Gravity.TOP;
		        
		        lp.format = PixelFormat.TRANSLUCENT;   
		        
		        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		        
		        lp.flags = 	WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		        		| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
		        		| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		        		
		        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
		        
				lp.packageName = mContext.getPackageName();
				
		        mLayoutParams = lp;
		        
		        if(auroraLayout != null)
		        {
					try
					{
						mWindowManager.addView(auroraLayout, lp);
					}
					catch(Exception ex)
					{
					}
				}
		 }
		 
		 public boolean auroraIsHeaderShowing()
		 {
			 return auroraViewAdded;
		 }
		 
		 public void auroraShowImage(int x,int y)
		 {
			 if(!auroraViewAdded)
			 {
				 auroraViewAdded = true;
				 
				 WindowManager.LayoutParams lp = mLayoutParams;
				 
				 log("lp.x = " + lp.x);
				 log("lp.y = " + lp.y);
				 lp.x = x;
				 
				 lp.y = y;
				   
				 if(auroraLayout != null && mWindowManager != null)
				 {
					auroraLayout.setAlpha(AURORA_OPAQUE_ALPHA);    
					
					mWindowManager.updateViewLayout(auroraLayout, lp);
				 }
		
			 }
		 }
		 
		 public void auroraHideImage()
		 {	 
			 if(auroraViewAdded)
			 {
				 auroraViewAdded = false;
				
				 if(auroraLayout != null && mWindowManager != null)
				 {
					auroraLayout.setAlpha(AURORA_TANSPARENT_ALPHA);    
				
					mWindowManager.updateViewLayout(auroraLayout, mLayoutParams);
				 }
			 }
		 }
		 
		 
		 public void auroraUpdateHeaderView(int x,int y)
		 {
			 if(auroraViewAdded)
			 {
				 WindowManager.LayoutParams lp = mLayoutParams;
				 
				 lp.x = x;
				 
				 lp.y = y;      
				 
				 mWindowManager.updateViewLayout(auroraLayout, lp);
			 }
		 }
		 
		 public void auroraRemoveImage()
		 {
			 if(mWindowManager != null)
				 mWindowManager.removeView(auroraLayout);
		 }
		 
		 private void auroraStartAnim(View v,boolean isAppearAnim)
		 {	
			 if(anim != null)
			 {
				 if(anim.isRunning())
					 anim.cancel();
			 }
			 if(isAppearAnim)
				 anim = ObjectAnimator.ofFloat(v, "alpha", 0f,1f);
			 else
				 anim = ObjectAnimator.ofFloat(v, "alpha", 1f,0f);
			 anim.setDuration(300);
			 
			 anim.start();
		 }
	}

	private synchronized AuroraListHeader auroraGetAuroraListHeader()
	{
		if(auroraListHeader == null)
		{
			auroraListHeader = new AuroraListHeader(this);
		}
		return auroraListHeader;
	}
	
	public void auroraShowHeaderView()
	{
		if(auroraListHeader != null && (getVisibility() == View.VISIBLE))
		{
			if(!auroraListHeader.auroraIsHeaderShowing())
			{
				auroraListHeader.auroraShowImage(auroraGetListViewLeftOnScreen() + auroraHeaderViewXOffset, auroraGetListViewTopOnScreen() + auroraHeaderViewYOffset);
			}
			else
			{
				auroraListHeader.auroraUpdateHeaderView(auroraGetListViewLeftOnScreen() + auroraHeaderViewXOffset, auroraGetListViewTopOnScreen() + auroraHeaderViewYOffset);
			}
		}
	}
	
	public void auroraHideHeaderView()
	{
		if(this.getVisibility() != View.VISIBLE)return;
		
		if(auroraListHeader != null)
		{
			if(auroraListHeader.auroraIsHeaderShowing())
			{
				auroraListHeader.auroraHideImage();
			}
		}
	}
	
	//note: this FirstItem not contain HeaderView
	private boolean auroraIsFirstItemOnTop()
	{
		int headerViewcount = this.getHeaderViewsCount();
		
		int position = this.getFirstVisiblePosition();
		
		View child = this.getChildAt(position);
		
		if(position < headerViewcount)return true;
		
		if(child == null)return false;
		
		if(headerViewcount == 0 && child != null && child.getTop() == 0)return true;
		
		return false;
	}
	
	//2014.03.09 add , signed to this
	private boolean auroraIsFirstItemOnTopContainHeaderView()
	{
		
		int position = this.getFirstVisiblePosition();
		
		View child = this.getChildAt(position);
		
		//not have any content
		if(child == null && position == 0)return true;
		
		if(child == null && position != 0)return false;
		
		if(child != null && child.getTop() == 0)return true;
		
		return false;
	}
	
	//update header view
	private void auroraUpdateAuroraHeaderView()
	{
		if(this.getVisibility() != View.VISIBLE)return;
		
		if(!auroraOnPaused)
		{
			if(auroraIsFirstItemOnTopContainHeaderView())
			{
				auroraHideHeaderView();
			}
			else
			{
				auroraShowHeaderView();
			}
		}
	}
	
	/****************************************************************************************
	 * 
	 * 
	 * 			listheader when scrolling end
	 * 
	 * 
	 ****************************************************************************************/
	
	@Override
	protected void onFocusChanged(boolean gainFocus,int direction,Rect previouslyFocusedRect)
	{
		log("onFocusChanged   gainFocus = " + gainFocus);
		if(!gainFocus)
		{
			auroraOnPause();
		}
		else
		{
			auroraOnPaused = false;
			
			auroraUpdateAuroraHeaderView();
		}
		
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}
	
	
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh)
	{
		auroraUpdateAuroraHeaderView();
		
		if(auroraIsRubbishOut())auroraSetRubbishBackNoAnim();
		
		if(auroraOnSizeChangedListener != null)
			auroraOnSizeChangedListener.auroraOnSizeChanged(w,h,oldw,oldh);
		
		super.onSizeChanged ( w,  h,  oldw,  oldh);
		
		//sort item fuction
		updateScrollStarts();
	}
	

	private boolean auroraFristVisiableItemIsHeaderView()
	{
		int itemIndex = this.getFirstVisiblePosition();
		
		int headerViewCounts = this.getHeaderViewsCount();
		
		return (headerViewCounts > 0 && itemIndex < headerViewCounts);
	}
	
	
	//2014.1.7 add
	//fuction: charge whether user touch the item is headerView
	//params: 
	//		ItemIndex: current ontouched item index
	private boolean auroraSelectedItemIsHeaderView(int ItemIndex)
	{
		int itemIndex = ItemIndex;
		
		int headerViewCounts = this.getHeaderViewsCount();
		
		return (headerViewCounts > 0 && itemIndex >= 0 && itemIndex < headerViewCounts);
	}
	
	/**********************************************************************************
	 * 
	 * 				remove selected item
	 * 
	 * 				2014.2.27
	 * 
	 * ********************************************************************************/
	
	private AuroraDeleteItemListener auroraDeleteItemListener;
	
	private static int auroraOriginalItemHeight;
	
	private AnimatorSet auroraDeleteUpMoveAnim;
	
	//2014.1.14 add
	//fuction:delete the selected item anim
	public void auroraDeleteSelectedItemAnim()
	{
		if(auroraClickRubbish)
		{
			auroraClickRubbish = false;
			
			auroraDeleteSelectedItemAnim(auroraChild);
		}
	}
	
	public void auroraRemoveItemAnim(View v)
	{	
	}
	
	public void auroraSetFrameNumbers(int frameNumbers)
	{
	}
	
	public void auroraDeleteSelectedItemAnim(View view)
    {	
    	final View v = view;
    	
    	if(v == null)return;
    	
    	auroraMotionPosition = getPositionForView(v);
    	
    	//record item height
    	auroraOriginalItemHeight = v.getHeight();
    	//log("auroraOriginalItemHeight = " + auroraOriginalItemHeight);
    	
		//set content width let rubbish appear
    	final View content =  (LinearLayout)v.findViewById(R.id.content);
    	ViewGroup.LayoutParams lp1 = content.getLayoutParams(); 
		lp1.width = auroraGetScreenWidth() - auroraGetRubbishLayoutWidth();
		content.setLayoutParams(lp1);
		  
		//set rubbish height,not match_parent and wrap_conent 
		final View rubbish =  (LinearLayout)v.findViewById(R.id.aurora_listview_back);
    	LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)rubbish.getLayoutParams(); 
		lp2.height = rubbish.getHeight();
		rubbish.setLayoutParams(lp2);
	

    	ObjectAnimator translationX = ObjectAnimator.ofFloat(content, "TranslationX", -(auroraGetRubbishLayoutWidth()),-(auroraGetScreenWidth()));
    	
    	translationX.setDuration(AURORA_ANIM_DURATION);
    	
    	ObjectAnimator translationY = ObjectAnimator.ofFloat(content, "TranslationY", 0.0f,0.0f);
    	
    	translationY.setDuration(AURORA_ANIM_DURATION);
		
		log("auroraDeleteSelectedItemAnim v.getHeight() = " + v.getHeight());
		log("auroraDeleteSelectedItemAnim auroraMotionPosition = " + auroraMotionPosition);
		log("auroraDeleteSelectedItemAnim auroraOriginalItemHeight = " + auroraOriginalItemHeight);
		log("auroraDeleteSelectedItemAnim content.getHeight() = " + content.getHeight());
		
		//if height == 0 , listView will auto invalidate !!! cause screen flash
		int end = Math.max(auroraOriginalItemHeight - content.getHeight() , 1);
		
		log("auroraDeleteSelectedItemAnim end = " + end);
		
    	ValueAnimator heightAnim = ValueAnimator.ofInt(auroraOriginalItemHeight,end);
    	
    	heightAnim.addUpdateListener(new AnimatorUpdateListener(){

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				
				int value = (Integer) animation.getAnimatedValue();
		
				ViewGroup.LayoutParams lp = v.getLayoutParams(); 
				
				lp.height = value;
				
				v.setLayoutParams(lp);
				
			}
    		
    	});
    	
    	heightAnim.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				
				auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
				
				auroraResetLeft(0,auroraGetScreenWidth());
				
				auroraDeleteSelectedItem(auroraChild,auroraMotionPosition);
				
				content.setTranslationX(0);
				
				content.setTranslationY(0);	
				
				//resume content width
				ViewGroup.LayoutParams lp1 = content.getLayoutParams(); 
				
				lp1.width = auroraGetScreenWidth();
				
				content.setLayoutParams(lp1);
				
				//set AURORA_OPAQUE_ALPHA in app bindView 
				content.setAlpha(AURORA_TANSPARENT_ALPHA);
				
			}
		
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	
    	heightAnim.setDuration(AURORA_ANIM_DURATION);
    	
    	//reset
    	auroraDeleteUpMoveAnim = null;
    	
    	AnimatorSet set = new AnimatorSet();
    	
    	if(set != null)
    	{
    		set.play(translationX).with(translationY);
    	
    		set.play(translationX).with(heightAnim);
    	
    		auroraDeleteUpMoveAnim = set;
    		
    		set.start();
		}
    }
    
	public interface AuroraDeleteItemListener
	{
    	void auroraDeleteItem(View v,int position);
    }
    
    public void auroraSetDeleteItemListener(AuroraDeleteItemListener listener)
    {
    	auroraDeleteItemListener = listener;
    }
    
    private void auroraDeleteSelectedItem(View v,int position)
    {
		
    	if(auroraDeleteItemListener != null)
    		auroraDeleteItemListener.auroraDeleteItem(v, position);
    		
    }
    
    private boolean auroraIsPlayingDeleteAnim()
    {
		if(auroraDeleteUpMoveAnim != null)
		{
			return auroraDeleteUpMoveAnim.isRunning();
				
		}
		return false;
		
	}
	
	//called in application adapter 
	public static int auroraGetDeleteItemHeight()
	{
		return auroraOriginalItemHeight;
	}
	/**********************************************************
	 * 
	 * 
	 * 				calender interface start 2014.1.15
	 * 				2014.1.15
	 * 
	 * 
	 * ********************************************************/
	
	//a mark to charge user whether use my event
	private boolean auroraUserHandleOnTouchEventSelf = false;
	
	private AuroraHandleOnTouchEvnetSelf auroraHandleOnTouchEvnetSelfListener = null;
	
	//time : 2014.1.15
	//fuction: mark user will get the OnTouch Event and do what they want to do 
	//params: self : false or true
	public void auroraSetOnTouchEventSelf(boolean self)
	{
		auroraUserHandleOnTouchEventSelf = self;
	}
	
	//time : 2014.1.15
	//fuction: the interface to let user get the event and do they want to do 
	public  interface AuroraHandleOnTouchEvnetSelf{
		
		//return:
		//true: not continue do my things,just do user things
		//false: continue do my things
		boolean auroraHandleOnTouchEvent(MotionEvent event);

	}
	
	//time : 2014.1.15
	//fuction: set a listener instance
	public void auroraSetHandleOnTouchEvnetSelfListener(AuroraHandleOnTouchEvnetSelf listener)
	{
		auroraHandleOnTouchEvnetSelfListener = listener;
	}
	
	
	//time : 2014.1.15
	//fuction: give the touch Event to user if user set auroraUserHandleOnTouchEventSelf = true;
	private boolean auroraUserHandleOnTouchEvent(MotionEvent event)
	{
		if(auroraUserHandleOnTouchEventSelf)
		{
			if(auroraHandleOnTouchEvnetSelfListener != null)
				return auroraHandleOnTouchEvnetSelfListener.auroraHandleOnTouchEvent(event);
		}
		
		return false;
	}
	/**********************************************************
	 * 
	 * 			
	 * 				calender interface end
	 * 
	 * 
	 * ********************************************************/


	/**********************************************************
	 * 
	 * 
	 * 				sliding delete new  logical start
	 * 				
	 * 				2014.1.23	
	 * 
	 *********************************************************/
	private int auroraScreenWidth = -1;
	
	private int auroraGetScreenWidth()
	{
		if(auroraScreenWidth == -1)
		{
			Rect rect = new Rect();
			
			this.getWindowVisibleDisplayFrame(rect);
			
			auroraScreenWidth = rect.right - rect.left;
			
		}
		return auroraScreenWidth;
	}
	
	private void auroraUpdateItemInNormalStateDraged(int distance)
	{
		log("auroraUpdateItemInNormalStateDraged distance = " + distance);
		
		if(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL)
		{
			int perDistance	= auroraGetRubbishLayoutWidth()/AURORA_RUBBISH_DRAWABLE_NUMBERS + 1;
			
			int index = Math.abs(distance)/perDistance;
			
			log("auroraUpdateItemInNormalStateDraged perDistance = " + perDistance);
			
			log("auroraUpdateItemInNormalStateDraged index = " + index);
			
			if(this.auroraChildContent != null)
			{
				auroraChildContent.setLeft(distance>0?0:distance);
				
				if(auroraChildRubbish != null && auroraChildRubbish.getVisibility() != View.VISIBLE)
				{
		
					auroraChildRubbish.setAlpha(0);
				
					auroraChildRubbish.setVisibility(View.VISIBLE);
					
				}
			}
			
			if(distance <= 0 && Math.abs(distance) <= auroraGetRubbishLayoutWidth())
			{	
				if(this.auroraChildRubbish != null)
				{
					if(auroraChildRubbish.getAlpha()==0.0f)
						auroraChildRubbish.setAlpha(1.0f);
						
					log("auroraUpdateItemInNormalStateDraged auroraGetScreenWidth() + distance = " + (auroraGetScreenWidth() + distance));
						
					auroraChildRubbish.setLeft(auroraGetScreenWidth() + distance);
					
					auroraUpdateRubbishDrawable(index);
				}
			}
			else if(distance <= 0 && Math.abs(distance) > auroraGetRubbishLayoutWidth())
			{
				log("auroraUpdateItemInNormalStateDraged 3 ");
				
				if(this.auroraChildRubbish != null)
				{
					log("auroraUpdateItemInNormalStateDraged 4 ");
					
					if(auroraChildRubbish.getAlpha()==0.0f)
						auroraChildRubbish.setAlpha(1.0f);
						
					auroraUpdateRubbishDrawable(AURORA_RUBBISH_DRAWABLE_NUMBERS - 1);
					
					auroraChildRubbish.setLeft(auroraGetScreenWidth() - auroraGetRubbishLayoutWidth());
				}
			}
			else if(distance > 0)
			{
				log("auroraUpdateItemInNormalStateDraged 5 ");
				
				if(this.auroraChildRubbish != null)
					auroraChildRubbish.setLeft(auroraGetScreenWidth());
				
			}
		}
	}
	
	private void auroraUpdateItemInDeleteStateDraged(int distance)
	{

		if(auroraIsRubbishOut())
		{
			int perDistance	= auroraGetRubbishLayoutWidth()/AURORA_RUBBISH_DRAWABLE_NUMBERS + 1;
			
			int rubbishWidth = auroraGetRubbishLayoutWidth();
			
			if(this.auroraChildContent != null)
			{
				auroraChildContent.setLeft((distance >= 0 && distance > rubbishWidth)?0:(distance - rubbishWidth));
				
				if(auroraChildRubbish != null && auroraChildRubbish.getVisibility() != View.VISIBLE)
				{
		
					auroraChildRubbish.setAlpha(0);
				
					auroraChildRubbish.setVisibility(View.VISIBLE);
					
				}
			}	
			if(distance >= 0 && distance <= rubbishWidth)
			{	
				if(this.auroraChildRubbish != null)
				{
					int index = Math.abs(distance - rubbishWidth)/perDistance;
					
					auroraChildRubbish.setLeft(auroraGetScreenWidth() + distance - rubbishWidth);
					
					auroraUpdateRubbishDrawable(index);
				}
			}
			else if(distance >= 0 && distance > rubbishWidth)
			{
				auroraChildRubbish.setLeft(auroraGetScreenWidth());
			}
			
		}
	}
	
	AuroraOverScrollFling.AuroraDecelerateInterpolator auroraSildeInterpolator = new AuroraOverScrollFling.AuroraDecelerateInterpolator(0.92f);
	
	private void auroraStartSlideSelfAnimInNormalState(final int startValue,final int endValue)
	{
		
		log("auroraStartSlideSelfAnim  startValue = " + startValue);
		
		log("auroraStartSlideSelfAnim  endValue = " + endValue);
		
		// if sliding speed most fast,not call auroraUpdateItem(), so must set Alpha = 1.0 here
		if(auroraChildRubbish != null)
		{
			if(auroraChildRubbish.getAlpha()==0.0f)
			{
				auroraChildRubbish.setAlpha(1.0f);
			}
		}
		
		ValueAnimator anim = ValueAnimator.ofInt( startValue,endValue);
		
		anim.setDuration(AURORA_ANIM_DURATION);
		
		auroraSildeInterpolator.auroraReset();
		
		anim.setInterpolator(auroraSildeInterpolator);
		
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				int left = (Integer)animation.getAnimatedValue();
				if(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL)
				{
					int perDistance	= auroraGetRubbishLayoutWidth()/AURORA_RUBBISH_DRAWABLE_NUMBERS + 1;
					
					int index = Math.abs(left)/perDistance;
					
					if(auroraChildContent != null)
					{
						auroraChildContent.setLeft(left);
					}
					if(auroraChildRubbish != null && Math.abs(left) <= auroraGetRubbishLayoutWidth())
					{
						auroraChildRubbish.setLeft(left + auroraGetScreenWidth());
						
						auroraUpdateRubbishDrawable(index);
					}
				}
				
			}
		});
		
		anim.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			//when anim is end,we should reset content bg.
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				if(auroraOnPaused)
				{
					auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
					
					if(auroraChildContent != null)
					{
						auroraChildContent.setLeft(0);
			    
						auroraChildContent.setRight(auroraChildContent.getRight());
						
						auroraChildRubbish.setLeft(auroraChildContent.getRight());
					}
					
					return;
				}
				
				if(endValue != 0)
				{
					
					log("auroraChildRubbish.setVisibility(View.VISIBLE)");
					
					auroraSetAuroraItemState(AURORA_ITEM_SHOW_DELETE);
					
					//draged succuss
					if(auroraBackOnClickListener != null)
						auroraBackOnClickListener.auroraDragedSuccess(auroraMotionPosition);
				}
				else
				{
					log("auroraChildRubbish.setVisibility(View.INVISIBLE)");
					
					auroraChildRubbish.setVisibility(View.INVISIBLE);
					
					if(auroraBackOnClickListener != null)
						auroraBackOnClickListener.auroraDragedUnSuccess(auroraMotionPosition);
				}
					
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		auroraSlidingAnim = anim;
		
		anim.start();	
	}
	
	
	private void auroraStartSlideSelfAnimInDeleteState(final int startValue,final int endValue)
	{
		ValueAnimator anim = ValueAnimator.ofInt( startValue,endValue);
		
		anim.setDuration(AURORA_ANIM_DURATION);
		
		auroraSildeInterpolator.auroraReset();
		
		anim.setInterpolator(auroraSildeInterpolator);
		
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				
				int left = (Integer)animation.getAnimatedValue();
				
				if(auroraIsRubbishOut())
				{
					int perDistance	= auroraGetRubbishLayoutWidth()/AURORA_RUBBISH_DRAWABLE_NUMBERS + 1;
					
					int index = Math.abs(left- auroraGetRubbishLayoutWidth())/perDistance;
					
					if(auroraChildContent != null)
					{
						auroraChildContent.setLeft(left - auroraGetRubbishLayoutWidth());
					}
					if(auroraChildRubbish != null && left > 0 && left < auroraGetRubbishLayoutWidth())
					{
						auroraChildRubbish.setLeft(left + auroraGetScreenWidth() - auroraGetRubbishLayoutWidth());
						
						auroraUpdateRubbishDrawable(index);
					}
				}
				
			}
		});
		
		anim.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			//when anim is end,we should reset content bg.
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				if(endValue != 0)
				{
					auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
					
					auroraChildRubbish.setVisibility(View.INVISIBLE);
					
					if(auroraBackOnClickListener != null)
						auroraBackOnClickListener.auroraDragedUnSuccess(auroraMotionPosition);
				}
				
				if(auroraOnPaused)
				{
					//clear memory ,it is static !!!
					auroraChildContent = null;
				}
					
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		auroraSlidingAnim = anim;
		
		anim.start();	
	}
	
	private void auroraUpdateRubbishDrawable(int index)
	{
		if(index < AURORA_RUBBISH_DRAWABLE_NUMBERS && index > 0)
		{		
			if(auroraRubbish != null && auroraRubbishAnimDrawable != null)
			{
				auroraRubbish.setImageDrawable(auroraRubbishAnimDrawable.getFrame(index));
				auroraRubbish.invalidate();
			}
		}
	}
	
	private boolean auroraIsPlayingSlidingAmim()
	{
		if(auroraSlidingAnim != null)
		{
			if(auroraSlidingAnim.isRunning())
				return true;
		}
		return false;
	}
	
	
	private boolean auroraPointIsInChildRect(int x, int y)
	{
	    if(auroraChild == null){
            return false;
        }
		int left = auroraChild.getLeft();
		
		int right = auroraChild.getRight();
		
		int top = auroraChild.getTop();
		
		int bottom = auroraChild.getBottom();
		
		log("auroraPointIsInChildRect left = " + left);
		
		log("auroraPointIsInChildRect right = " + right);
		
		log("auroraPointIsInChildRect top = " + top);
		
		log("auroraPointIsInChildRect bottom = " + bottom);
		
		log("auroraPointIsInChildRect x = " + x);
		
		log("auroraPointIsInChildRect y = " + y);
		
		return (x > left && x < right && y > top && y < bottom);
	}
	
	
	private boolean auroraPointIsInChildContentRect(int x, int y)
	{
	    if(auroraChildRubbish == null){
            return false;
        }
		int left = auroraChildContent.getLeft();
		
		int right = auroraChildContent.getRight();
		
		int top = auroraChildContent.getTop();
		
		int bottom = auroraChildContent.getBottom();
		
		return (x > left && x < right && auroraPointIsInChildRect(x,y));
	}
	
	
	private boolean auroraPointIsInChildRubbishRect(int x, int y)
	{
	    if(auroraChildRubbish == null){
	        return false;
	    }
		int left = auroraChildRubbish.getLeft();
		
		int right = auroraChildRubbish.getRight();
		
		int top = auroraChildRubbish.getTop();
		
		int bottom = auroraChildRubbish.getBottom();
		
		log("auroraPointIsInChildRubbishRect left = " + left);
		
		log("auroraPointIsInChildRubbishRect right = " + right);
		
		log("auroraPointIsInChildRubbishRect top = " + top);
		
		log("auroraPointIsInChildRubbishRect bottom = " + bottom);
		
		log("auroraPointIsInChildRubbishRect x = " + x);
		
		log("auroraPointIsInChildRubbishRect y = " + y);
		
		return (x > left && x < right && auroraPointIsInChildRect(x,y));
	}
	
	
	//client press back key call this fuction ,not call auroraOnPause
	//2014.2.18 add
	public void auroraSetRubbishBack()
	{
		if(auroraIsRubbishOut())
		{
			//incase play this anim more times
			if(!auroraIsPlayingSlidingAmim())
				auroraStartSlideSelfAnimInDeleteState(0,auroraGetRubbishLayoutWidth());
		}
	}
	
	public void auroraSetRubbishBackNoAnim()
	{
		if(auroraIsRubbishOut())
		{
			auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
		}
	}
	/**********************************************************
	 * 
	 * 
	 * 				sliding delete new  logical end
	 * 
	 * 
	 * ********************************************************/
	

	
	/****************************************************************************
	 * 
	 * 				
	 * 				selector final 
	 * 
	 * 				2014.2.24
	 * 
	 * 				start
	 * 
	 ****************************************************************************/
	//android system slector object
	private Drawable auroraSelectorDrawable;
	
	//a mark of return back selector disappear
	private boolean auroraNewSelectorLogical = true;
	
	//just contacts used
	private boolean auroraSetSelectorToContentBg = false;
	
	private static final int[] AURORA_STATE_PRESSED = new int[]{ 
			android.R.attr.state_enabled,
    		android.R.attr.state_pressed,        	
    };
	
	private static final int[] AURORA_STATE_FOCUSED = new int[]{
			android.R.attr.state_enabled,      
    };
	
	private static final int[] AURORA_STATE_UP = new int[]{
			0        	
    };
	
	private static Drawable auroraPressedDrawable;

	public void auroraSetUseNewSelectorLogical(boolean use)
	{
		auroraNewSelectorLogical = use;
	}

	private boolean auroraIsNewSelectorLogical()
	{
		return auroraNewSelectorLogical;
	}
	
	public static class AuroraStateListDrawable extends StateListDrawable
	{
		public AuroraStateListDrawable()
		{
			super();
		}
		
		protected boolean onStateChange(int[] stateSet) {
		
		if(DEBUG)
		{	
			
			for(int i =0 ; i < stateSet.length ; i++)
			{
				Log.e("liuwei","state["+i+"] = " + stateSet[i]);
			}
		
			Log.e("liuwei","android.R.attr.state_window_focused = "+ android.R.attr.state_window_focused);
			Log.e("liuwei","android.R.attr.state_selected = "+ android.R.attr.state_selected);
			Log.e("liuwei","android.R.attr.state_focused = "+ android.R.attr.state_focused);
			Log.e("liuwei","android.R.attr.state_enabled = "+ android.R.attr.state_enabled);
			Log.e("liuwei","android.R.attr.state_pressed = "+ android.R.attr.state_pressed);
			Log.e("liuwei","android.R.attr.state_activated = "+ android.R.attr.state_activated);
			Log.e("liuwei","android.R.attr.state_accelerated = "+ android.R.attr.state_accelerated);
			Log.e("liuwei","android.R.attr.state_hovered = "+ android.R.attr.state_hovered);
			Log.e("liuwei","android.R.attr.state_drag_can_accept = "+ android.R.attr.state_drag_can_accept);
			Log.e("liuwei","android.R.attr.state_drag_hovered = "+ android.R.attr.state_drag_hovered);
		}
			
	        return super.onStateChange(stateSet);
	    }
	}
	
	public static AuroraStateListDrawable auroraNewStateListDrawable()
	{
		AuroraStateListDrawable drawable= new AuroraStateListDrawable();

		auroraAddSelectorStates(drawable);
		
		return drawable;
	}
	
	private static void auroraAddSelectorStates(AuroraStateListDrawable drawable)
	{
		if(drawable == null)return;
		
	//	drawable.setEnterFadeDuration(50);
		
	//	drawable.setExitFadeDuration(50);
		
		drawable.addState(AURORA_STATE_PRESSED,auroraPressedDrawable);
		
	}
	
	public void auroraSetSelectorToContentBg(boolean set)
	{
		auroraSetSelectorToContentBg = set;
		
		if(set)
		{
			setSelector(auroraTansparentDrawable);
		}
	}
	
	public boolean auroraIsSetSelectorToContentBg()
	{
		return auroraSetSelectorToContentBg;
	}
	
	//must static type ,contacts adpter call this
	public static Drawable auroraGetAuroraStateListDrawableFromIndex(View v,int index)
	{
	
		if(index < 0 || v == null)return null;
		
		Drawable original = v.getBackground();	
		
		if(auroraIsSelectorEnable())
		{
			
			if(!(original instanceof AuroraStateListDrawable))
			{	
				v.setBackground(auroraNewStateListDrawable());
			}
			else
			{
				original.setAlpha(AURORA_OPAQUE_ALPHA);
			}
		}
		else
		{
			if(original instanceof AuroraStateListDrawable)
			{
				original.setAlpha(AURORA_TANSPARENT_ALPHA);
			}
		}
		
		return null;
	}
	
	// enable selector
	public void auroraEnableSelector(boolean enable)
	{
		auroraEnableSelector = enable;
		if(enable)
		{
			if(!auroraIsSetSelectorToContentBg())
				setSelector(auroraSelectorDrawable);
		}
		else
		{
			if(!auroraIsSetSelectorToContentBg())
				setSelector(auroraTansparentDrawable);
		}
	}
	
	//is enable selector or not 
	public static boolean auroraIsSelectorEnable()
	{
		return auroraEnableSelector;
	}
	
	/***************************************************************************************
	 * 
	 * 				check each item can slide rubbish out
	 * 
	 * 				2014.4.11
	 * 
	 * 				start
	 * 
	 * ************************************************************************************/
	
	private View auroraItemSlideSwitch;
	
	private boolean auroraIsItemSlideSwitchOn()
	{
		if(auroraIsNeedSlideDelete())
		{
			//auroraItemSlideSwitch.getVisibility() == View.GONE means switch on
			return (auroraItemSlideSwitch != null)?(auroraItemSlideSwitch.getVisibility() == View.GONE):true;
		}
		
		return false;
	}
	/***************************************************************************************
	 * 
	 * 				check each item can slide rubbish out
	 * 
	 * 				2014.4.11
	 * 
	 * 				end
	 * 
	 * ************************************************************************************/
	
	public void auroraSetShowRubbishIcon(boolean show)
	{
		auroraShowRubbishIcon = show;
	}
	
	AuroraOverScrollAnim auroraOverScrollObject = null;
	
	public class AuroraOverScrollAnim extends AuroraOverScrollFling
    {
    	ListView auroraListView;

		public AuroraOverScrollAnim(ListView v)
    	{
    		auroraListView = v;
    		
    	}
    	
    	@Override
    	public void onTouchEvent(MotionEvent event)
    	{
			if(!auroraIsFirstItemTopVisible() &&  !auroraIsLastItemBottomVisible())return;
			
			if(mSlidingState == NO_SLIDING || mSlidingState == SLIDING_UP || mSlidingState == SLIDING_DOWN)
			{
				super.onTouchEvent(event);
			}
		}
		
		protected void auroraHandleActionDown(int x, int y) {
			// TODO Auto-generated method stub
    	
		}
		
		protected void auroraHandleActionPointerDown(int x, int y) {
			// TODO Auto-generated method stub
			log("auroraHandleActionPointerDown ");
			
		}
		
		protected void auroraHandleActionMove(int x, int y) {
			// TODO Auto-generated method stub
			log("auroraHandleActionMove ");	
			
			auroraStopOverFingAnim();
			
    		auroraSetOverScrollY(y);

    		auroraListView.setScrollY(auroraOverScrollY);
    		
    		//auroraOverScrollY != 0 just real overScroll
    		if(auroraOverScrollY == 0)return;
    		
    		//must set state !!!
    		mSlidingState = (auroraOverScrollY > 0)?SLIDING_UP : SLIDING_DOWN;
    		
    		if(auroraWrapListener != null)
    		{
				auroraWrapListener.onScroll(auroraListView, auroraListView.getFirstVisiblePosition(),
					auroraListView.getChildCount(), auroraListView.getCount());
				
				auroraWrapListener.onScrollStateChanged(AuroraListView.this, OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			}
		}
		
		protected void auroraHandleActionUp(int x, int y) {
			// TODO Auto-generated method stub
    		log("auroraHandleActionUp ");	
    		
    		auroraPlayOverScrollAnim(auroraListView);
    		
    		if(auroraWrapListener != null)
    		{
				auroraWrapListener.onScrollStateChanged(AuroraListView.this, OnScrollListener.SCROLL_STATE_FLING);
			}
		}
		
    	protected void auroraHandleActionPointerUp(int x, int y) {
			// TODO Auto-generated method stub
			
			log("auroraHandleActionPointerUp ");		
			
		}
		
		protected void auroraHandleActionCancel(int motionX2, int motionY2) 
		{
			// TODO Auto-generated method stub
			log("auroraHandleActionCancel ");	
			
			auroraPlayOverScrollAnim(auroraListView);
		}
		
    	protected boolean auroraIsFirstItemTopVisible()
    	{

			View firstItem = null;
			
			if(auroraListView != null)firstItem = auroraListView.getChildAt(0);
			
			if(auroraListView != null && firstItem != null)
			{
				
				return (auroraListView.getFirstVisiblePosition() == 0 && firstItem.getTop() >= 0);
			}
			
			return false;
    	}
    	
    	protected boolean auroraIsLastItemBottomVisible()
    	{
    		int fistVisibleIndex = auroraListView.getFirstVisiblePosition();
    		
    		int lastVisibleIndex = auroraListView.getLastVisiblePosition();
    		
    		if(auroraListView.getAdapter() == null)return false;
    		
    		int totoalCounts = 0;
    		
    		if(auroraListView.getAdapter() != null)totoalCounts = auroraListView.getAdapter().getCount();
				
    		View lastItem = auroraListView.getChildAt(lastVisibleIndex - fistVisibleIndex);
    		
    		if(auroraListView != null && lastItem != null)
    		{
				
				return (lastVisibleIndex == (totoalCounts - 1) && (lastItem.getBottom() <= auroraListView.getBottom())); 
			}
			
			return false;
    	}
    	
    	protected void auroraOverFlingEndListener()
		{
			if(auroraWrapListener != null)
			{
				auroraWrapListener.onScrollStateChanged(AuroraListView.this, OnScrollListener.SCROLL_STATE_IDLE);
			}
		
			log("auroraOverFlingEndListener ");	
		}
		
		public boolean auroraOverScrolling()
		{
			return auroraOverScrollY != 0;
		}
		
    }
    
    private void auroraHanldeOverScrollEvent(MotionEvent event)
    {
   
		if(auroraOverScrollObject != null && auroraEnableOverScroll)
		{
			if(AURORA_TOUCH_MODE_DRAG != auroraGetTouchMode() && auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL)	
			{
				
				auroraOverScrollObject.onTouchEvent(event);
			
				if(auroraOverScrolling())	
				{
					
					if(event.getAction() == MotionEvent.ACTION_MOVE)
					{
						event.setAction(MotionEvent.ACTION_CANCEL);
					}
				}
				
			}
		}
	}
    
    public boolean auroraOverScrolling()
    {
		return (auroraOverScrollObject != null && auroraOverScrollObject.auroraOverScrolling());
	}
	
	public void auroraEnableOverScroll(boolean enable)
	{
		auroraEnableOverScroll = enable;
		
		if(enable && auroraOverScrollObject == null)
        {
			auroraOverScrollObject = new AuroraOverScrollAnim(this);
		}
		else if(!enable)
		{
			auroraOverScrollObject = null;
		}
		
	}
	
	private void auroraEndOverScroll()
	{
		if(auroraOverScrolling())
		{
			auroraOverScrollObject.auroraStopOverFingAnim();
			
			auroraOverScrollObject.auroraSetScrollY(0);
			
			setScrollY(0);
		}
	}
	
	@Override
    public void requestLayout() {
		//sorting item 
		if (mBlockLayoutRequests)
        {
            return;
        }
		
		auroraEndOverScroll();
		
        super.requestLayout();
        
    }
    
	
	public void enableSlideRight()
	{
		mSlideRight = true;
	}
	
	public void disableSlideRight()
	{
		mSlideRight = false;
	}
	
	public boolean isSlideRightEnable()
	{
		return mSlideRight;
	}
	
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) 
    {
		int contentLeft = 0;
		
		int rubbishLeft = auroraGetScreenWidth();
		
		if(auroraChildContent != null && auroraChildRubbish != null)
		{
			contentLeft = auroraChildContent.getLeft();
			
			rubbishLeft = auroraChildRubbish.getLeft();
		}
			
		super.onLayout(changed,  left,  top,  right,  bottom); 
		
		//incase rubbish invisible
		if(auroraIsRubbishOut())
		{
			auroraResetLeft(contentLeft,rubbishLeft);
		}
		//incase auroraChildContent auto back !!!
		else if(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_LEFT_REGION)
		{
			if(auroraChildContent != null && auroraCustomFront != null)
			{
				int width = auroraCustomFront.getMeasuredWidth();
				
	    		auroraChildContent.setLeft(width);
			}
		}
    }
    
    private void auroraResetLeft(int contentLeft , int rubbishLeft)
    {
		if(auroraChildContent != null && auroraChildRubbish != null)
		{
			auroraChildContent.setLeft(contentLeft);
		
			auroraChildRubbish.setLeft(rubbishLeft);
		}
	}
    
    private void handleSlideRight(MotionEvent event)
    {
    	//maybe is sliding left
    	if(!(mSlidingState == SLIDING_RIGHT || mSlidingState == NO_SLIDING))return;
    	
    	//maybe sliding left successed
    	if(!(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_LEFT_REGION 
    			|| auroraGetAuroraItemState() == AURORA_ITEM_SHOW_NORMAL))return;
    	
    	//action_up and animation is running 
    	if(slideRightAnimationRunning())return;
    	
    	if(auroraScrolling)return;
    	
    	int action = event.getAction() & MotionEvent.ACTION_MASK;
    	
    	switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			hanldeSlideRightActionDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			hanldeSlideRightActionMove(event);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_POINTER_DOWN:	
			hanldeSlideRightActionUp(event);
			break;

		}
    }
    
    /**
     * action_down
     */
    private void hanldeSlideRightActionDown(MotionEvent event)
    {
    	if(mSlideRight)
    	{
    		//left region has slide out
    		if(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_LEFT_REGION)
			{
    			int width = auroraCustomFront.getMeasuredWidth();
    			
    			int lastPosition = auroraMotionPosition;
    			
    			int x = (int)event.getX();
    			
    			int y = (int)event.getY();
    			
    			int position = pointToPosition(x , y);
    			
    			//click other item
    			if(position != lastPosition)
    			{
    				startBackWithAnim();
    			}
    			else if(x > width)
    			{
    				//click contentView ,let it continue slide right
    				mSlidingState = SLIDING_RIGHT;
    				
    				auroraSetTouchMode(AURORA_TOUCH_MODE_DRAG);
    				
    				auroraDowmPoint.set(auroraDowmPoint.x - width , auroraDowmPoint.y);
    				
    			}
				return;
			}
    		
    		//if opened slide left fuction , it will be init at slide left action_down
    		if(!auroraIsNeedOpenSlideItemFuction)
    		{
    			AuroraInitActionDownInfo(auroraDowmPoint.x , auroraDowmPoint.y);
    		}
    	}
    }
    
    /**
     * action_move
     */
    private void hanldeSlideRightActionMove(MotionEvent event)
    {
    	if(mSlideRight)
    	{
    		if(auroraGetTouchMode() == AURORA_TOUCH_MODE_DOWN && (mX - auroraDowmPoint.x > AURORA_DRAG_START_WIDTH))
    		{
    			auroraSetTouchMode(AURORA_TOUCH_MODE_DRAG);
    			
    			auroraDowmPoint.set(mX, mY);
    			
    			mSlidingState = SLIDING_RIGHT;
    			
    			prepareSlideRight();
    		}
    		else if(auroraGetTouchMode() == AURORA_TOUCH_MODE_DRAG)
    		{
    			int width = auroraCustomFront.getMeasuredWidth();
    			
    			int distance = Math.max(mX - auroraDowmPoint.x, 0);
    			
    			updateCustomFrontLocation(distance);
    			
    		}
    	}
    }
    
    /**
     * action_up
     */
    private void hanldeSlideRightActionUp(MotionEvent event) {
		// TODO Auto-generated method stub
		if(mSlideRight)
		{
			if(auroraGetTouchMode() == AURORA_TOUCH_MODE_DRAG)
			{
				int width = auroraCustomFront.getMeasuredWidth();
    			
    			int distance = Math.max(mX - auroraDowmPoint.x, 0);
    			
    			if(distance < width/2)
    				startBack(distance);
    			else
    				openLeftRegion(distance);
    			
    			auroraSetTouchMode(AURORA_TOUCH_MODE_IDLE);
			}
		}
	}
    
    /**
     * slide right Animator
     */
    private ValueAnimator mSlideRightAnimation;
    
    /**
     * slideRight success !!!
     */
    private void openLeftRegion(int distance) {
		// TODO Auto-generated method stub
		final int width = auroraCustomFront.getMeasuredWidth();
		
		ValueAnimator anim = ValueAnimator.ofInt(distance , width);
		
		anim.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				int vlaue = (Integer) animation.getAnimatedValue();
				
				updateCustomFrontLocation(vlaue);
				
			}
		});
		
		anim.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				
				auroraSetAuroraItemState(AURORA_ITEM_SHOW_LEFT_REGION);
				
				if(mSlideRightListener != null)
					mSlideRightListener.slideSucess(auroraMotionPosition);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
    			
				auroraSetAuroraItemState(AURORA_ITEM_SHOW_LEFT_REGION);
				
				if(mSlideRightListener != null)
					mSlideRightListener.slideSucess(auroraMotionPosition);
			}
		});
		
		auroraSildeInterpolator.auroraReset();
		
		anim.setInterpolator(auroraSildeInterpolator);
		
		anim.setDuration(AURORA_ANIM_DURATION);
		
		mSlideRightAnimation = anim;
		
		anim.start();
	}

	private void startBack(int distance) {
		// TODO Auto-generated method stub
		final int width = auroraCustomFront.getMeasuredWidth();
		
		ValueAnimator anim = ValueAnimator.ofInt(distance , 0);
		
		anim.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				int vlaue = (Integer) animation.getAnimatedValue();
				
				updateCustomFrontLocation(vlaue);
			
			}
		});
		
		anim.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				
				auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
				
				auroraCustomFront.setVisibility(View.GONE);
				
				if(mSlideRightListener != null)
					mSlideRightListener.slideBack(auroraMotionPosition);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
				auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
				
				auroraCustomFront.setVisibility(View.GONE);
				
				if(mSlideRightListener != null)
					mSlideRightListener.slideBack(auroraMotionPosition);
			}
		});
		
		auroraSildeInterpolator.auroraReset();
		
		anim.setInterpolator(auroraSildeInterpolator);
		
		anim.setDuration(AURORA_ANIM_DURATION);
		
		mSlideRightAnimation = anim;
		
		anim.start();
	}
	
	/**
	 * animation is runing 
	 */
	private boolean slideRightAnimationRunning()
	{	
		if(mSlideRightAnimation != null)
		{
			return mSlideRightAnimation.isRunning();
		}

		return false;
	}
	
	/**
	 * let contentView reback ,not has animation
	 */
	public void startBack() {
		// TODO Auto-generated method stub
	
		updateCustomFrontLocation(0);
		
		auroraCustomFront.setVisibility(View.GONE);
		
		auroraSetAuroraItemState(AURORA_ITEM_SHOW_NORMAL);
		
		if(mSlideRightListener != null)
			mSlideRightListener.slideBack(auroraMotionPosition);
	}
	
	/**
	 * let contentView reback with animation
	 */
	public void startBackWithAnim() {
		// TODO Auto-generated method stub
		final int width = auroraCustomFront.getMeasuredWidth();
		
		startBack(width);
		
		if(mSlideRightListener != null)
			mSlideRightListener.slideBack(auroraMotionPosition);
	}
	
	/**
	 * ready to slide right
	 */
    private void prepareSlideRight()
    {
    	if(auroraCustomFront.getVisibility() == View.GONE)
    	{
    		updateCustomFrontLocation(0);
    		
    		auroraCustomFront.setAlpha(0);
    		
    		auroraCustomFront.setVisibility(View.VISIBLE);
    		
    		if(mSlideRightListener != null)
    			mSlideRightListener.prepareDraged(auroraMotionPosition);
    	}
    }
    
    /**
     * on pause when operating slide right
     */
    private void slideRightOnPause()
    {
    	if(mSlideRight)
    	{
    		//cancel animation
    		if(slideRightAnimationRunning())
    			mSlideRightAnimation.cancel();
    		
    		if(auroraGetAuroraItemState() == AURORA_ITEM_SHOW_LEFT_REGION)
    		{
    			startBackWithAnim();
    		}
    		
    		//still sliding right 
    		if(mSlidingState == SLIDING_RIGHT)
    		{
    			startBack();
    		}
    	}
    }
    
    /**
     * update location
     */
    private void updateCustomFrontLocation(int distance)
    {
    	int width = auroraCustomFront.getMeasuredWidth();
    	
    	if(auroraChildContent != null)
    		auroraChildContent.setLeft(distance);
    	
    	if(distance > width)
    		distance = width;
    	
    	auroraCustomFront.setLeft(distance - width);
    	
    	auroraCustomFront.setRight(auroraCustomFront.getLeft() + width);
    	
    	
    	if(auroraCustomFront.getVisibility() == View.VISIBLE)
    		auroraCustomFront.setAlpha(1);
    }
    
    /**
     * slide right listener
     */
    public interface AuroraSlideRightListener{
    	
    	public void prepareDraged(int index);
    	
    	public void slideBack(int index);
    	
    	public void slideSucess(int index);
    };
    
    private AuroraSlideRightListener mSlideRightListener;
    
    /**
     * set slide right listener
     */
    public void setSlideRightListener(AuroraSlideRightListener listener)
    {
    	this.mSlideRightListener = listener;
    }
    
    /**
     * focus or not 
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        
    	super.onWindowFocusChanged(hasFocus);
    	
    	if(hasFocus)
    	{
    		auroraOnResume();
    	}
    	else
    	{
    		auroraOnPause();
    	}
    	
    }
    
    /**
     * window destoryed !!!
     */
    @Override
    protected void onDetachedFromWindow()
    {
    	super.onDetachedFromWindow();
    	
    	//static var must be null to release memory
		auroraChildContent = null;
    }
    
    /**
     * screen on or off
     */
    @Override
    public void onScreenStateChanged(int screenState) {
    	
    	super.onScreenStateChanged(screenState);
    	
    }
    
    /**
     * view's visibility changed
     */
    @Override
    protected void onVisibilityChanged(View changedView, int visibility)
    {
    	
    	if(visibility == View.GONE || visibility == View.INVISIBLE)
    	{
			auroraOnPause();
		}
		else if(visibility == View.VISIBLE)
		{
			auroraOnResume();
		}

    }
    
    
    
    /***                 item sort fuction                     ***/
    
    private boolean mSortEnable;
    
    private boolean mScrolling = false;
	
	public boolean mBlockLayoutRequests = false;
	
	private AuroraAdapterWrapper mAdapterWrapper;
	
	private DataSetObserver mObserver;
	
	private DropAnimator mDropAnimator;
	
	private ImageView mSuspendView; 
	
	private Bitmap mSuspendBitmap;
	
	private Point auroraSuspendLoc = new Point();
	
	private int mDeltaY, mSuspendViewWidth, mSuspendViewHeight,mSuspendViewMid;

	 /**
     * Height in pixels to which the originally dragged item
     * is collapsed during a drag-sort. Currently, this value
     * must be greater than zero.
     */
	private int mItemHeightCollapsed = 1;
	
	private int mSrcPos = -1, mCurPos = -1;
	
	private final int NORMAL = 0;
	
	private final int UP = 1;
	
	private final int DOWN = 2;
	
	private int[] mCurItemState;;
	
	private View mSrcView , mCurView;
	
	private int mCurItemTop,mCurItemBottom;
	
	private ValueAnimator mAnimator;
	
	private static final int DIRECTION_UP = 2;
	
	private static final int DIRECTION_DOWN = 1;
	
	private static final int DIRECTION_STOP = 0;
	
	private int curDirection = DIRECTION_STOP;
	
	private int lastDirection = DIRECTION_STOP;
	
	/**
     * The first expanded ListView position that helps represent
     * the drop slot tracking the floating View.
     */
    private int mFirstExpPos = -1;

    /**
     * The second expanded ListView position that helps represent
     * the drop slot tracking the floating View. This can equal
     * mFirstExpPos if there is no slide shuffle occurring; otherwise
     * it is equal to mFirstExpPos + 1.
     */
    private int mSecondExpPos = -1;
    
    /**
     * Save the given width spec for use in measuring children
     */
    private int mWidthMeasureSpec = 0;
    
    /**
     * Number between 0 and 1 indicating the relative location of
     * a sliding item (only used if drag-sort animations
     * are turned on). Nearly 1 means the item is 
     * at the top of the slide region (nearly full blank item
     * is directly below).
     */
    private float mSlideFrac = 0.0f;
    
    /**
     * Determines when a slide shuffle animation starts. That is,
     * defines how close to the edge of the drop slot the floating
     * View must be to initiate the slide.
     */
    private float mSlideRegionFrac = 0.25f;
    
    
    private DragScroller mDragScroller;
    
    private int mUpScrollStartY;
    
    private int mDownScrollStartY;
    
    private float mDownScrollStartYF;
    
    private float mUpScrollStartYF;
    
    private float mMaxScrollSpeed = 0.5f;
    
    private float mDragUpScrollStartFrac = 1.0f / 3.0f;
    
    private float mDragDownScrollStartFrac = 1.0f / 3.0f;
    
    private float mDragUpScrollHeight;
    
    private float mDragDownScrollHeight;
    
    private View[] mSampleViewTypes = new View[1];
    
    private int[] mHeightCash;
    
    /**
     * enable
     */
    public void enableSortItem()
    {
    	mSortEnable = true;
    }
    
    /**
     * disable
     */
    public void disableSortItem()
    {
    	mSortEnable = false;
    }
    
    private void initSortItem()
	{
		
		mDropAnimator = new DropAnimator(0.5f, AURORA_ANIM_DURATION);
		
		// construct the dataset observer
        mObserver = new DataSetObserver() {
        	
            private void cancel() {

            	if(mSortEnable)
            		cancelDrag();    
            }

            @Override
            public void onChanged() {
                cancel();
            }

            @Override
            public void onInvalidated() {
                cancel();
            }
        };
	}
    
    /**
     * 
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
		
        super.dispatchDraw(canvas);
        
        auroraDrawSuspendView(canvas);
	}
    
    private void auroraDrawSuspendView(Canvas canvas)
	{
		if(mSortEnable)
        {
        	drawSuspendView(canvas);
        }
	}
    
    private void updateScrollStarts() 
	{
        final int padTop = getPaddingTop();
        final int listHeight = getHeight() - padTop - getPaddingBottom();
        float heightF = (float) listHeight;

        mUpScrollStartYF = padTop + mDragUpScrollStartFrac * heightF;
        mDownScrollStartYF = padTop + (1.0f - mDragDownScrollStartFrac) * heightF;

        mUpScrollStartY = (int) mUpScrollStartYF;
        mDownScrollStartY = (int) mDownScrollStartYF;

        mDragUpScrollHeight = mUpScrollStartYF - padTop;
        
        mDragDownScrollHeight = padTop + listHeight - mDownScrollStartYF;
    }
    
    /**
     * handle sort item MotionEvent
     */
    private void auroraHanldeSortAction(MotionEvent ev)
	{
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		
		switch(action)
		{
			case MotionEvent.ACTION_DOWN:
//				onSortActionDown(ev);
				break;
			case MotionEvent.ACTION_MOVE:
				onSortActionMove(ev);
				break;
			case MotionEvent.ACTION_UP:
				onSortActionUp(ev);
				break;
			case MotionEvent.ACTION_CANCEL:
				onSortActionCancel(ev);
				break;
		}
	}
    
    /**
     * sort item action down 
     */
    public void onSortActionDown()
	{
	
		int x = (int) mX;
		
		int y = (int) mY;
		
		int position = pointToPosition(x, y);
		
		mFirstExpPos = mSecondExpPos = mCurPos = mSrcPos = position;
		
		View child = getChildAt(position - getFirstVisiblePosition());
		
		//stop drag
		if(child == null)
		{

			return;
		}
		
		mSrcView = child;
		
		createSuspendView(child);
		
		mSuspendViewHeight = child.getHeight() + getDividerHeight();
		
		mSuspendViewWidth = child.getWidth();
		
		mDeltaY = y - child.getTop();
		
		auroraSuspendLoc.x = getPaddingLeft();
		
		auroraSuspendLoc.y = y - mDeltaY;
		
		mSuspendViewMid = auroraSuspendLoc.y + mSuspendViewHeight/2;
		
		mFirstExpPos = mSecondExpPos = position;
		
		child.setVisibility(View.INVISIBLE);
		
		mDragScroller = new DragScroller();
		
		//real sorting flag !!!
		mSlidingState = SLIDING_SORT;
	}
    
    /**
     * create Suspend view
     */
    private void createSuspendView(View view)
	{
		if(view == null)return;
		
		if(mSuspendBitmap != null)
			mSuspendBitmap.recycle();
		
		if(mSuspendView == null)
			mSuspendView = new ImageView(getContext());
		
		view.setPressed(false);
		
		view.setDrawingCacheEnabled(true);
		
		view.buildDrawingCache();
		
		mSuspendBitmap = Bitmap.createBitmap(view.getDrawingCache());
	
		view.setDrawingCacheEnabled(false);
		
		mSuspendView.setImageBitmap(mSuspendBitmap);
		
		mSuspendView.setLayoutParams(new ViewGroup.LayoutParams(view.getWidth(), view.getHeight() + getDividerHeight()));
	
		mSuspendView.layout(0, 0, view.getWidth(), view.getHeight() + getDividerHeight());
		
	}
    
    /**
     * destory Suspend view
     */
    private void destroySuspendView()
	{
		if (mSuspendView != null)
		{
			mSuspendView.setVisibility(GONE);
			
			mSuspendView = null;
			
			if(mSuspendBitmap != null)
				mSuspendBitmap.recycle();
			
			mSuspendBitmap = null;
			
            invalidate();
        }
	}
    
    /**
     * sort item action move 
     */
    private void onSortActionMove(MotionEvent ev)
	{
    	if(mSlidingState != SLIDING_SORT)return;
    	
		auroraSuspendLoc.y = mY - mDeltaY;
		
		mSuspendViewMid = auroraSuspendLoc.y + mSuspendViewHeight/2;
		
		dragSuspendView(true);
		
	}
    
    /**
     * invalide suspend View when dragging !!!
     */
    private void dragSuspendView(boolean forceInvalidate)
	{
		int movePos = getFirstVisiblePosition() + getChildCount() / 2;
		
        View moveItem = getChildAt(getChildCount() / 2);
		
        dragSuspendView(movePos , moveItem , forceInvalidate);
		
        dragScrollIfNeed();
        
	}
    
    /**
     * draged to top or bottom ,we should scroll the listView to continue sort !!!
     */
    private void dragScrollIfNeed()
	{
		int minY = Math.min(mY, mSuspendViewMid + mSuspendViewHeight/2);
		
        int maxY = Math.max(mY, mSuspendViewMid - mSuspendViewHeight/2);
        
        int currentScrollDir = mDragScroller.getScrollDir();
        
        if (minY > mLastY && minY > mDownScrollStartY && currentScrollDir != DragScroller.DOWN) 
        {
            // dragged down, it is below the down scroll start and it is not
            // scrolling up
            if (currentScrollDir != DragScroller.STOP) {
                // moved directly from up scroll to down scroll
                mDragScroller.stopScrolling(true);
            }

            // start scrolling down
            mDragScroller.startScrolling(DragScroller.DOWN);
            
        }
        else if (maxY < mLastY && maxY < mUpScrollStartY && currentScrollDir != DragScroller.UP) 
        {
            // dragged up, it is above the up scroll start and it is not
            // scrolling up

            if (currentScrollDir != DragScroller.STOP) {
                // moved directly from down scroll to up scroll
                mDragScroller.stopScrolling(true);
            }

            // start scrolling up
            mDragScroller.startScrolling(DragScroller.UP);
            
        }
        else if (maxY >= mUpScrollStartY && minY <= mDownScrollStartY && mDragScroller.isScrolling()) 
        {	
            // not in the upper nor in the lower drag-scroll regions but it is
            // still scrolling
            mDragScroller.stopScrolling(true);
        }
	}
    
    /**
     * real drag The specified position item 
     */
    private void dragSuspendView(int movePos, View moveItem, boolean forceInvalidate)
	{
		mBlockLayoutRequests = true;
		
		updateSuspendView();
		
		int oldFirstExpPos = mFirstExpPos;
        int oldSecondExpPos = mSecondExpPos;
        
        boolean updated = updatePositions();
        
        if(true) 
        {
            adjustAllItems();
            
            int scroll = adjustScroll(movePos, moveItem, oldFirstExpPos, oldSecondExpPos);

            setSelectionFromTop(movePos, moveItem.getTop() + scroll - getPaddingTop());
            layoutChildren();
        }

        if (updated || forceInvalidate) {
            invalidate();
        }
        
        mBlockLayoutRequests = false;
	}
    
    /**
     * limit suspend view location !!!
     */
	private void updateSuspendView()
	{
		final int floatX = auroraSuspendLoc.x;
        final int floatY = auroraSuspendLoc.y;
        
        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();
        final int firstPos = getFirstVisiblePosition();
        final int lastPos = getLastVisiblePosition();
        
        int topLimit = getPaddingTop();
        
        if (firstPos < numHeaders) 
        {
            topLimit = getChildAt(numHeaders - firstPos - 1).getBottom();
        }
        
        int bottomLimit = getHeight() - getPaddingBottom();
        
        if (lastPos >= getCount() - numFooters - 1) 
        {
            bottomLimit = getChildAt(getCount() - numFooters - 1 - firstPos).getBottom();
        }
        
        if (floatY < topLimit) 
        {
        	auroraSuspendLoc.y = topLimit;
        } 
        else if (floatY + mSuspendViewHeight > bottomLimit) 
        {
        	auroraSuspendLoc.y = bottomLimit - mSuspendViewHeight;
        }
        
        mSuspendViewMid = auroraSuspendLoc.y + mSuspendViewHeight/2;
	}
	
	/**
	 * 
	 */
	private boolean updatePositions()
	{
		final int first = getFirstVisiblePosition();
		
		int startPos = mFirstExpPos;
		
		View startView = getChildAt(startPos - first);
		
		if (startView == null) {
            startPos = first + getChildCount() / 2;
            startView = getChildAt(startPos - first);
        }
		
		int startTop = startView.getTop();
		
		int itemHeight = startView.getHeight();
		
		int edge = getShuffleEdge(startPos, startTop);
		
		int lastEdge = edge;
		
		int divHeight = getDividerHeight();
		
		int itemPos = startPos;
		
		int itemTop = startTop;
		
		if (mSuspendViewMid < edge) {
            // scanning up for float position
            // Log.d("mobeta", "    edge="+edge);
            while (itemPos >= 0) {
                itemPos--;
                itemHeight = getItemHeight(itemPos);

                if (itemPos == 0) {
                    edge = itemTop - divHeight - itemHeight;
                    break;
                }

                itemTop -= itemHeight + divHeight;
                
                edge = getShuffleEdge(itemPos, itemTop);
                // Log.d("mobeta", "    edge="+edge);

                if (mSuspendViewMid >= edge) {
                    break;
                }

                lastEdge = edge;
            }
        } else {
            // scanning down for float position
            // Log.d("mobeta", "    edge="+edge);
            final int count = getCount();
            while (itemPos < count) {
                if (itemPos == count - 1) {
                    edge = itemTop + divHeight + itemHeight;
                    break;
                }

                itemTop += divHeight + itemHeight;
                itemHeight = getItemHeight(itemPos + 1);
                edge = getShuffleEdge(itemPos + 1, itemTop);
                // Log.d("mobeta", "    edge="+edge);

                // test for hit
                if (mSuspendViewMid < edge) {
                    break;
                }

                lastEdge = edge;
                itemPos++;
            }
        }

        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();

        boolean updated = false;
		
        int oldFirstExpPos = mFirstExpPos;
        int oldSecondExpPos = mSecondExpPos;
        float oldSlideFrac = mSlideFrac;

        if (true) {
        	
            int edgeToEdge = Math.abs(edge - lastEdge);
          
            int edgeTop, edgeBottom;
            if (mSuspendViewMid < edge) {
                edgeBottom = edge;
                edgeTop = lastEdge;
            } else {
                edgeTop = edge;
                edgeBottom = lastEdge;
            }
          
            int slideRgnHeight = (int) (0.5f * mSlideRegionFrac * edgeToEdge);
            float slideRgnHeightF = (float) slideRgnHeight;
            int slideEdgeTop = edgeTop + slideRgnHeight;
            int slideEdgeBottom = edgeBottom - slideRgnHeight;

            // Three regions
            if (mSuspendViewMid < slideEdgeTop) {
                mFirstExpPos = itemPos - 1;
                mSecondExpPos = itemPos;
                mSlideFrac = 0.5f * ((float) (slideEdgeTop - mSuspendViewMid)) / slideRgnHeightF;
                
            } else if (mSuspendViewMid < slideEdgeBottom) {
                mFirstExpPos = itemPos;
                mSecondExpPos = itemPos;
            } else {
                mFirstExpPos = itemPos;
                mSecondExpPos = itemPos + 1;
                mSlideFrac = 0.5f * (1.0f + ((float) (edgeBottom - mSuspendViewMid))
                        / slideRgnHeightF);
                
            }

        } else {
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        }
        
        // correct for headers and footers
        if (mFirstExpPos < numHeaders) {
            itemPos = numHeaders;
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        } else if (mSecondExpPos >= getCount() - numFooters) {
            itemPos = getCount() - numFooters - 1;
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        }

        if (mFirstExpPos != oldFirstExpPos || mSecondExpPos != oldSecondExpPos
                || mSlideFrac != oldSlideFrac) {
            updated = true;
        }

        if (itemPos != mCurPos) {
          
        	mCurPos = itemPos;
            updated = true;
        }

        return updated;
	}
	
	/**
	 * 
	 */
	private int getShuffleEdge(int position, int top)
	{
		final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();
        
        if (position <= numHeaders || (position >= getCount() - numFooters)) {
            return top;
        }
        
        int divHeight = getDividerHeight();
        
        int edge;
        
        int maxBlankHeight = mSuspendViewHeight - mItemHeightCollapsed;
        
        int childHeight = getChildHeight(position);
        
        int itemHeight = getItemHeight(position);
        
        // first calculate top of item given that floating View is
        // centered over src position
        int otop = top;
        
        if (mSecondExpPos <= mSrcPos)
        {
        	// items are expanded on and/or above the source position
        	if (position == mSecondExpPos && mFirstExpPos != mSecondExpPos)
        	{
        		if (position == mSrcPos) {
                    otop = top + itemHeight - mSuspendViewHeight;
                } else {
                    int blankHeight = itemHeight - childHeight;
                    otop = top + blankHeight - maxBlankHeight;
                }
        	}
        	else if (position > mSecondExpPos && position <= mSrcPos) {
                otop = top - maxBlankHeight;
            }
        }
        else
        {
        	// items are expanded on and/or below the source position
        	if (position > mSrcPos && position <= mFirstExpPos) {
                otop = top + maxBlankHeight;
            } else if (position == mSecondExpPos && mFirstExpPos != mSecondExpPos) {
                int blankHeight = itemHeight - childHeight;
                otop = top + blankHeight;
            }
        }
        
        // otop is set
        if (position <= mSrcPos) {
            edge = otop + (mSuspendViewHeight - divHeight - getChildHeight(position - 1)) / 2;
        } else {
            edge = otop + (childHeight - divHeight - mSuspendViewHeight) / 2;
        }

        return edge;
	}
	
	/**
	 * 
	 */
	private int getChildHeight(int position)
	{
		if (position == mSrcPos) 
		{
            return 0;
        }
		
		View v = getChildAt(position - getFirstVisiblePosition());
		
		if (v != null) {
            // item is onscreen, therefore child height is valid,
            // hence the "true"
            return getChildHeight(position, v, false);
        } 
		else
		{
			// item is offscreen
			final ListAdapter adapter = getAdapter();
			
			int type = adapter.getItemViewType(position);
			
			v = adapter.getView(position, null, AuroraListView.this);
			
			// current child height is invalid, hence "true" below
            int childHeight = getChildHeight(position, v, true);
            
            return childHeight;
		}
	}
	
	/**
	 * 
	 */
	private int getChildHeight(int position, View item, boolean invalidChildHeight) {
        if (position == mSrcPos) {
            return 0;
        }

        View child;
        if (position < getHeaderViewsCount() || position >= getCount() - getFooterViewsCount()) {
            child = item;
        } else {
            child = ((ViewGroup) item).getChildAt(0);
        }

        ViewGroup.LayoutParams lp = child.getLayoutParams();

        if (lp != null) {
            if (lp.height > 0) {
                return lp.height;
            }
        }

        int childHeight = child.getHeight();

        if (childHeight == 0 || invalidChildHeight) {
            measureItem(child);
            childHeight = child.getMeasuredHeight();
        }

        return childHeight;
    }
	
	/**
	 * 
	 */
	private int adjustScroll(int movePos, View moveItem, int oldFirstExpPos, int oldSecondExpPos) {
        int adjust = 0;

        final int childHeight = getChildHeight(movePos);

        int moveHeightBefore = moveItem.getHeight();
        int moveHeightAfter = calcItemHeight(movePos, childHeight);

        int moveBlankBefore = moveHeightBefore;
        int moveBlankAfter = moveHeightAfter;
        if (movePos != mSrcPos) {
            moveBlankBefore -= childHeight;
            moveBlankAfter -= childHeight;
        }

        int maxBlank = mSuspendViewHeight;
        if (mSrcPos != mFirstExpPos && mSrcPos != mSecondExpPos) {
            maxBlank -= mItemHeightCollapsed;
        }

        if (movePos <= oldFirstExpPos) {
            if (movePos > mFirstExpPos) {
                adjust += maxBlank - moveBlankAfter;
            }
        } else if (movePos == oldSecondExpPos) {
            if (movePos <= mFirstExpPos) {
                adjust += moveBlankBefore - maxBlank;
            } else if (movePos == mSecondExpPos) {
                adjust += moveHeightBefore - moveHeightAfter;
            } else {
                adjust += moveBlankBefore;
            }
        } else {
            if (movePos <= mFirstExpPos) {
                adjust -= maxBlank;
            } else if (movePos == mSecondExpPos) {
                adjust -= moveBlankAfter;
            }
        }

        return adjust;
    }
	
	/**
	 * measure item width and height
	 */
	private void measureItem(View item) 
	{	
        ViewGroup.LayoutParams lp = item.getLayoutParams();
        
        if (lp == null) {
            lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item.setLayoutParams(lp);
        }
        
        int wspec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, getListPaddingLeft()
                + getListPaddingRight(), lp.width);
        
        int hspec;
        
        if (lp.height > 0) {
            hspec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        } else {
            hspec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        
        item.measure(wspec, hspec);
    }
	
	/**
	 * get item height from position !!!
	 */
	private int getItemHeight(int position) 
	{
        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null) {
            // item is onscreen, just get the height of the View
            return v.getHeight();
        } else {
            // item is offscreen. get child height and calculate
            // item height based on current shuffle state
            return calcItemHeight(position, getChildHeight(position));
        }
    }
	
	/**
	 * 
	 */
	private int calcItemHeight(int position, View item, boolean invalidChildHeight) 
    {
        return calcItemHeight(position, getChildHeight(position, item, invalidChildHeight));
    }
	
	/**
	 * 
	 */
	private int calcItemHeight(int position, int childHeight) {

        int divHeight = getDividerHeight();

        boolean isSliding = mFirstExpPos != mSecondExpPos;
        int maxNonSrcBlankHeight = mSuspendViewHeight - mItemHeightCollapsed;
        int slideHeight = (int) (mSlideFrac * maxNonSrcBlankHeight);
        
       
        int height;

        if (position == mSrcPos) {
            if (mSrcPos == mFirstExpPos) {
                if (isSliding) {
                    height = slideHeight + mItemHeightCollapsed;
                } else {
                    height = mSuspendViewHeight;
                }
            } else if (mSrcPos == mSecondExpPos) {
                // if gets here, we know an item is sliding
                height = mSuspendViewHeight - slideHeight;
            } else {
                height = mItemHeightCollapsed;
            }
        } else if (position == mFirstExpPos) {
            if (isSliding) {
                height = childHeight + slideHeight;
            } else {
                height = childHeight + maxNonSrcBlankHeight;
            }
        } else if (position == mSecondExpPos) {
            // we know an item is sliding (b/c 2ndPos != 1stPos)
            height = childHeight + maxNonSrcBlankHeight - slideHeight;
        } else {
            height = childHeight;
        }
        
        return height;
    }
	
	/**
	 * 
	 */
	private void adjustAllItems()
	{
        final int first = getFirstVisiblePosition();
        final int last = getLastVisiblePosition();

        int begin = Math.max(0, getHeaderViewsCount() - first);
        int end = Math.min(last - first, getCount() - 1 - getFooterViewsCount() - first);

        for (int i = begin; i <= end; ++i) {
            View v = getChildAt(i);
            if (v != null) {
                adjustItem(first + i, v, false);
            }
        }
    }
	
	/**
	 * 
	 */
	private void adjustItem(int position) 
	{
        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null) {
            adjustItem(position, v, false);
        }
    }
	
	/**
     * Sets layout param height, gravity, and visibility  on
     * wrapped item.
     */
	private void adjustItem(int position, View v, boolean invalidChildHeight) {

        // Adjust item height
        ViewGroup.LayoutParams lp = v.getLayoutParams();
       
        int height;
        
        if (position != mSrcPos && position != mFirstExpPos && position != mSecondExpPos)
        {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } 
        else 
        {
            height = calcItemHeight(position, v, invalidChildHeight);    
        }
        
        if (height != lp.height) 
        {
        	
            lp.height = height;
            
            v.setLayoutParams(lp);
        }
        
        // Adjust item gravity
        if (position == mFirstExpPos || position == mSecondExpPos) {
            if (position < mSrcPos) {
                ((DragSortItemView) v).setGravity(Gravity.BOTTOM);
            } else if (position > mSrcPos) {
                ((DragSortItemView) v).setGravity(Gravity.TOP);
            }
        }
       
        // Finally adjust item visibility

        int oldVis = v.getVisibility();
        int vis = View.VISIBLE;

        if (position == mSrcPos && mSuspendView != null) {
            vis = View.INVISIBLE;
        }

        if (vis != oldVis) {
            v.setVisibility(vis);
        }
    }
	
	/**
	 * finally sort item action up !!!
	 */
	private void onSortActionUp(MotionEvent ev)
	{
		if(mSlidingState != SLIDING_SORT)return;
		
		stopDrag();
	}
	
	/**
	 * finally sort item action cancel !!!
	 */
	private void onSortActionCancel(MotionEvent ev)
	{
		if(mSlidingState != SLIDING_SORT)return;
		
		cancelDrag();	
	}
	
	/**
	 * show suspend view on 
	 */
	private void drawSuspendView(Canvas canvas)
	{
		if(mSuspendView != null)
		{
			int w = mSuspendViewWidth;
			
			int h = mSuspendViewHeight;
			
			ViewGroup.LayoutParams lp = mSuspendView.getLayoutParams();
			
			canvas.save();
			
			canvas.translate(auroraSuspendLoc.x, auroraSuspendLoc.y);
			
			canvas.clipRect(0, 0, w, h);
			
			canvas.saveLayerAlpha(0, 0, w, h, 100, Canvas.ALL_SAVE_FLAG);
			
			mSuspendView.draw(canvas);
			
			canvas.restore();
		}
	}
	
	/**
	 * the scroller when draged suspend view at most top or bottom location
	 */
	private class DragScroller implements Runnable
	{
		private boolean mAbort;
		
		private int scrollDir;

        public final static int STOP = -1;
        public final static int UP = 0;
        public final static int DOWN = 1;
	    
        private long tStart; 
        
        private long mPrevTime;
        
        private long mCurrTime;
        
        private float dt;
        
        private int dy;
        
        private float mScrollSpeed; // pixels per ms
        
		public boolean isScrolling() {
            return mScrolling;
        }
		
		public int getScrollDir() {
            return mScrolling ? scrollDir : STOP;
        }
		
		public void startScrolling(int dir) {
            if(!mScrolling) 
            {    
                mAbort = false;
                mScrolling = true;
                tStart = SystemClock.uptimeMillis();
                mPrevTime = tStart;
                scrollDir = dir;
                post(this);
            }
        }
		
		public void stopScrolling(boolean now) 
		{
            if(now)
            {
            	AuroraListView.this.removeCallbacks(this);
                mScrolling = false;
            }
            else
            {
                mAbort = true;
            }
        }
		
		@Override
		public void run() 
		{
			// TODO Auto-generated method stub
			if (mAbort) {
                mScrolling = false;
                return;
            }
			
			final int first = getFirstVisiblePosition();
            final int last = getLastVisiblePosition();
            final int count = getCount();
            final int padTop = getPaddingTop();
            final int listHeight = getHeight() - padTop - getPaddingBottom();
            
            int minY = Math.min(mY, mSuspendViewMid + mSuspendViewHeight/2);
	        int maxY = Math.max(mY, mSuspendViewMid - mSuspendViewHeight/2);
	        
	        if (scrollDir == UP) {
	        	
                View v = getChildAt(0);
                
                if (v == null) {
                    mScrolling = false;
                    return;
                } else {
                    if (first == 0 && v.getTop() == padTop) {
                        mScrolling = false;
                        return;
                    }
                }
                mScrollSpeed = 2.0f * (mUpScrollStartYF - minY)/mDragDownScrollHeight;
            } 
	        else
	        {
                View v = getChildAt(last - first);
                if (v == null) {
                    mScrolling = false;
                    return;
                } else {
                    if (last == count - 1 && v.getBottom() <= listHeight + padTop) {
                        mScrolling = false;
                        return;
                    }
                }
                mScrollSpeed = 2.0f * (mDownScrollStartYF - minY)/mDragDownScrollHeight ;
            }
	        
	        mCurrTime = SystemClock.uptimeMillis();
	        
	        dt = (float) (mCurrTime - mPrevTime);
	        
	        dy = (int) Math.round(mScrollSpeed * dt);
	        
	        int movePos;
	        
	        if(dy >= 0)
	        {
                dy = Math.min(listHeight, dy);
                movePos = first;
            }
	        else
	        {
                dy = Math.max(-listHeight, dy);
                movePos = last;
            }
	        
	        final View moveItem = getChildAt(movePos - first);
	        
	        int top = moveItem.getTop() + dy;
	        
	        if (movePos == 0 && top > padTop) {
                top = padTop;
            }
	        mBlockLayoutRequests = true;
	        setSelectionFromTop(movePos, top - padTop);
	        layoutChildren();
	        invalidate();
	        
	        dragSuspendView(movePos, moveItem, false);
	        mBlockLayoutRequests = false;
	        mPrevTime = mCurrTime;
	        
	        post(this);
		}//end run 
	}

	/**
	 * 
	 */
	@Override
	public void setAdapter(ListAdapter adapter) {
		//if sort item disable ,we can not set our adapter !!!
		if(!mSortEnable)
		{
			super.setAdapter(adapter);
			
			return;
		}
		
        if (adapter != null) 
        {
        	mHeightCash = new int[adapter.getCount()];
        	
            mAdapterWrapper = new AuroraAdapterWrapper(adapter);
            
            adapter.registerDataSetObserver(mObserver);
            
        } 
        else
        {
            mAdapterWrapper = null;
        }

        super.setAdapter(mAdapterWrapper);
    }
	
	/**
	 * sort fuction should use ourSelf Adater !!!
	 */
	public class AuroraAdapterWrapper extends BaseAdapter {
        private ListAdapter mAdapter;

        public AuroraAdapterWrapper(ListAdapter adapter) {
            super();
            mAdapter = adapter;
            
            mAdapter.registerDataSetObserver(new DataSetObserver() {
                public void onChanged() {
                    notifyDataSetChanged();
                }

                public void onInvalidated() {
                    notifyDataSetInvalidated();
                }
            });
        }

        public ListAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position) {
            return mAdapter.isEnabled(position);
        }
        
        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }
        
        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }
        
        @Override
        public boolean isEmpty() {
            return mAdapter.isEmpty();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DragSortItemView v;
            View child;
       
            if (convertView != null) {
                v = (DragSortItemView) convertView;
                View oldChild = v.getChildAt(0);

                child = mAdapter.getView(position, oldChild, AuroraListView.this);
                
                if (child != oldChild) 
                {
                    // shouldn't get here if user is reusing convertViews
                    // properly
                    if (oldChild != null) {
                        v.removeViewAt(0);
                    }
                    v.addView(child);
                }
            }
            else 
            {
                child = mAdapter.getView(position, null, AuroraListView.this);
                
                v = new DragSortItemView(getContext());

                v.addView(child);
            }
            
            // Set the correct item height given drag state; passed
            // View needs to be measured if measurement is required.
            adjustItem(position + getHeaderViewsCount(), v, true);
            
            return v;
        }
        
        @Override
	 	public void notifyDataSetChanged() {
	         super.notifyDataSetChanged();
	     }
    }
	
	/**
	 * 
	 */
	public void cancelDrag() 
	{
        if (mSortEnable) 
        {    	
        	if(mDragScroller != null)
        		mDragScroller.stopScrolling(true);
            
            destroySuspendView();
            
            clearPositions();
            
            adjustAllItems();
            
        }
    }
	
	/**
	 * 
	 */
	private void clearPositions()
	{
		mSrcPos = -1;
        mFirstExpPos = -1;
        mSecondExpPos = -1;
        mCurPos = -1;
	}
	
	/**
	 * 
	 */
	private void stopDrag()
	{
		if (mSuspendView != null)
		{
            mDragScroller.stopScrolling(true);
            
            if (mDropAnimator != null) {
                mDropAnimator.start();
            } else {
                dropFloatView();
            }
		}         
	}
	
	/**
	 * 
	 */
	private void dropFloatView()
	{
		if(auroraItemChangedListener != null)
        	auroraItemChangedListener.auroraExchangeItem(mSrcPos, mCurPos);
		
        destroySuspendView();
        
        adjustOnReorder();
        
        clearPositions();
        
        adjustAllItems();
        
	}
	
	/**
	 * 
	 */
	private void adjustOnReorder() 
	{
        final int firstPos = getFirstVisiblePosition();
      
        if (mSrcPos < firstPos) {
            // collapsed src item is off screen;
            // adjust the scroll after item heights have been fixed
            View v = getChildAt(0);
            int top = 0;
            if (v != null) {
                top = v.getTop();
            }
            setSelectionFromTop(firstPos - 1, top - getPaddingTop());
        }
    }
	
	/**
	 * parent animation!!!
	 */
	private class SmoothAnimator implements Runnable {
        protected long mStartTime;

        private float mDurationF;

        private float mAlpha;
        private float mA, mB, mC, mD;

        private boolean mCanceled;

        public SmoothAnimator(float smoothness, int duration) {
            mAlpha = smoothness;
            mDurationF = (float) duration;
            mA = mD = 1f / (2f * mAlpha * (1f - mAlpha));
            mB = mAlpha / (2f * (mAlpha - 1f));
            mC = 1f / (1f - mAlpha);
        }

        public float transform(float frac) {
            if (frac < mAlpha) {
                return mA * frac * frac;
            } else if (frac < 1f - mAlpha) {
                return mB + mC * frac;
            } else {
                return 1f - mD * (frac - 1f) * (frac - 1f);
            }
        }

        public void start() {
            mStartTime = SystemClock.uptimeMillis();
            mCanceled = false;
            onStart();
            post(this);
        }

        public void cancel() {
            mCanceled = true;
        }

        public void onStart() {
            // stub
        }

        public void onUpdate(float frac, float smoothFrac) {
            // stub
        }

        public void onStop() {
            // stub
        }

        @Override
        public void run() {
            if (mCanceled) {
                return;
            }

            float fraction = ((float) (SystemClock.uptimeMillis() - mStartTime)) / mDurationF;

            if (fraction >= 1f) {
                onUpdate(1f, 1f);
                onStop();
            } else {
                onUpdate(fraction, transform(fraction));
                post(this);
            }
        }
    }
	
	/**
	 * child animation .
	 */
	private class DropAnimator extends SmoothAnimator {

        private int mDropPos;
        private int srcPos;
        private float mInitDeltaY;
        private float mInitDeltaX;

        public DropAnimator(float smoothness, int duration) {
            super(smoothness, duration);
        }

        @Override
        public void onStart() {
            mDropPos = mCurPos;
            srcPos = mSrcPos;
           
            mInitDeltaY = auroraSuspendLoc.y - getTargetY();
            mInitDeltaX = auroraSuspendLoc.x - getPaddingLeft();
        }

        private int getTargetY() {
            final int first = getFirstVisiblePosition();
            final int otherAdjust = (mItemHeightCollapsed + getDividerHeight()) / 2;
            View v = getChildAt(mDropPos - first);
            int targetY = -1;
            if (v != null) {
                if (mDropPos == srcPos) {
                    targetY = v.getTop();
                } else if (mDropPos < srcPos) {
                    // expanded down
                    targetY = v.getTop() - otherAdjust;
                } else {
                    // expanded up
                    targetY = v.getBottom() + otherAdjust - mSuspendViewHeight;
                }
            } else {
                // drop position is not on screen?? no animation
                cancel();
            }

            return targetY;
        }

        @Override
        public void onUpdate(float frac, float smoothFrac) {
            final int targetY = getTargetY();
            final int targetX = getPaddingLeft();
            final float deltaY = auroraSuspendLoc.y - targetY;
            final float deltaX = auroraSuspendLoc.x - targetX;
            final float f = 1f - smoothFrac;
            if (f < Math.abs(deltaY / mInitDeltaY) || f < Math.abs(deltaX / mInitDeltaX)) {
            	auroraSuspendLoc.y = targetY + (int) (mInitDeltaY * f);
            	auroraSuspendLoc.x = getPaddingLeft() + (int) (mInitDeltaX * f);
                dragSuspendView(true);
            }
        }

        @Override
        public void onStop() {
            dropFloatView();
           
        }
    }
	
	/**
	 * client listener
	 */
	public interface AuroraItemChangedListener
	{
		public void auroraExchangeItem(int src, int dest);
	}
	
	/**
	 * 
	 */
	private AuroraItemChangedListener auroraItemChangedListener;
	
	/**
	 * 
	 */
	public void auroraSetItemChangedListener(AuroraItemChangedListener l)
	{
		auroraItemChangedListener = l;
	}
} //AuroraListView end













	


