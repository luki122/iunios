package aurora.lib.widget;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


import aurora.lib.app.AuroraActivity;
import aurora.lib.widget.AuroraActionBar;
import aurora.lib.widget.AuroraActionBarItem;
import aurora.lib.widget.AuroraMenu;
import aurora.lib.widget.NormalAuroraActionBarItem;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import com.aurora.lib.R;
import com.aurora.lib.utils.DensityUtil;
//自定义actionbar，继承LinearLayout
/**
 * @author leftaven
 * @2013年9月12日 自定义actionbar
 */
public class AuroraActionBar extends LinearLayout {

	public static final int NONE = 0;

	private AuroraActivity activity;
	private Context context;
	private View view;// 当前窗口对象视图
	private Rect rect;
	private int statusBarHeight;
	private Animation menuAnimation;
	
	private TextView mHomeTextView;
	private TextView mCancelView;
	private TextView mOkView;
	private TextView mTitleView;
	private ImageButton mHomeButton;
	private FrameLayout mCustomView;
	private AuroraMenu auroraMenu;
	private boolean isActionBottomBarMenu;
	private AuroraMenu auroraActionBottomBarMenu;
	private boolean isShowBottomBarMenu;
	private AuroraMenu.Type menuType;
	private boolean mMerging = false;// actionbar视图是否完成合并，各个视图组合成一个actionbar

	private CharSequence mTitle;
	private AuroraActionBar.Type mType;// 不同类型到actionbar，自定义有3种
	private OnAuroraActionBarItemClickListener mOnActionBarListener;// 监听actionbar上的事件
	private LinkedList<AuroraActionBarItem> mItems;// actionbar上的所有item
	private OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener;// 监听actionbar上home的事件

	private int mMaxItemsCount;// actionbar允许到最多item个数
	
	//add by tangjun 2013.12.3 start
	private Boolean firstCreateAllOperation=true;
	private List<View> views;
	private TextView mLeftView;
	private TextView mRightView;
	private View dashboardLayout;
	private boolean isNeedShowAuroraActionBottomBarMenu = true;
	private boolean isDashBoardAnimRun = false;
	//add by tangjun 2013.12.3 end
	
	private Drawable mTitleBackground;
	
	
	// Aurora <Luofu> <2014-3-19> modify for title animation begin
	
	private View mTitleLayout;
	private int mDefaultTitleBackgroundColor = 0x00000000;
	
	
	// Aurora <Luofu> <2014-3-19> modify for title animation end
	
	private LinearLayout mLinear;
	private LinearLayout mLinearNormal;
	
	private LinearLayout mHomeLayout;
	
	private Drawable mBackground;
	
	private View actionbarLayout;

	public TextView getCancelButton() {
		return mCancelView;
	}

	public TextView getOkButton() {
		return mOkView;
	}
	
	public TextView getTitleView() {
		return mTitleView;
	}

	/*
	 * Normal：textview，homebutton有效 Dashboard: textview，homebutton无效
	 * Empty:只有textview，无homebutton
	 */
	public enum Type {
		Normal, Dashboard, Empty, Custom,NEW_COSTOM
	}

	public interface OnAuroraActionBarItemClickListener {
		int HOME_ITEM = -1;

		void onAuroraActionBarItemClicked(int itemId);// 定义actionbar点击事件
	}

	public interface OnAuroraActionBarBackItemClickListener {
		int HOME_ITEM = -1;

		void onAuroraActionBarBackItemClicked(int itemId);// 定义actionbar点击事件
	}

	public void setOnAuroraActionBarListener(
			OnAuroraActionBarItemClickListener listener) {
		mOnActionBarListener = listener;
	}

	public void setmOnActionBarBackItemListener(
			OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener) {
		this.mOnActionBarBackItemListener = mOnActionBarBackItemListener;
	}

	public AuroraActionBar(Context context) {
		this(context, null);
	}

	public AuroraActionBar(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.auroraActionBarStyle);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 *            完成actionbar的初始化工作
	 */
	public AuroraActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		//long aa = System.currentTimeMillis();
		
		this.context = context;
		activity = (AuroraActivity) context;
		initActionBar();
		initActionBarMenu(R.layout.aurora_action_bar_all_delete);

		//long dd = System.currentTimeMillis();
		//Log.e("222222", "AuroraActionBar--1 = " + String.valueOf(dd - aa) );
		
		// 读取控件属性值，存入到typedarray中,对应属性存放在R.styleable.ActionBar里面
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AuroraActionBar, defStyle, 0);

		// 获取各个属性的值
		mTitle = a.getString(R.styleable.AuroraActionBar_title);
		mMaxItemsCount = a.getInt(
				R.styleable.AuroraActionBar_maxItems, 3);
		int layoutID;
		int type = a
				.getInteger(R.styleable.AuroraActionBar_type, -1);

		mTitleBackground = a.getDrawable(R.styleable.AuroraActionBar_titleBackground);
		
		// 根据选择的类型不同，选择不同的actionbar布局
		switch (type) {
		case 2:
			mType = Type.Empty;
			layoutID = R.layout.aurora_action_bar_empty;
			break;
		case 1:
			mType = Type.Dashboard;
			layoutID = R.layout.aurora_action_bar_dashboard;
			break;
		case 3:
			mType = Type.Custom;
			layoutID = R.layout.aurora_action_bar_custom;
			break;
		case 0:
		default:
			mType = Type.Normal;
			layoutID = R.layout.aurora_action_bar_normal;
			break;
		}
		//long bb = System.currentTimeMillis();
		//Log.e("222222", "AuroraActionBar--2 = " + String.valueOf(bb - dd) );
		
		mMerging = true;
		// actionbar视图渲染
		LayoutInflater.from(context).inflate(layoutID, this, true);
		mMerging = false;
		a.recycle();
		
		initTempDashBoardActionBar( );
		
		//flash add 
		auroraInitEditModeAnimObjects();
		//end
		readBackgroundDrawable();
	
		// auroraGetTitleFontTypeFace();
	}
	
	public void changeAuroraActionbarType(AuroraActionBar.Type actionBarType) {
		
		int actionbarTypeInt;
		
		mType = actionBarType;
		
		if(actionBarType == null){
			actionbarTypeInt = R.layout.aurora_action_bar_normal;
			mType = Type.Normal;
	    }
		switch (actionBarType) {
		case Dashboard:
			actionbarTypeInt = R.layout.aurora_action_bar_dashboard;
			break;
		case Empty:
			actionbarTypeInt = R.layout.aurora_action_bar_empty;
			break;
		case Custom:
			actionbarTypeInt = R.layout.aurora_action_bar_custom;
			break;
		case Normal:
		default:
			actionbarTypeInt = R.layout.aurora_action_bar_normal;
			break;
		}
		
		actionbarLayout = LayoutInflater.from(context).inflate(actionbarTypeInt, null);
		if ( actionbarLayout != null ) {
			removeAllViews();
			addView(actionbarLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
			bindViewsEvent( );
		}
	}
	
	private void readBackgroundDrawable(){
       TypedValue outValue = new TypedValue();
        
        context.getTheme().resolveAttribute(R.attr.auroraActionBarBackground,
                outValue, true);
        int backgroundResId = outValue.resourceId;
        setBackgroundResource(backgroundResId);
	}
	
	private void initTempDashBoardActionBar( ) {
		dashboardLayout = LayoutInflater.from(context).inflate(R.layout.aurora_action_bar_all_delete, null);
		mLeftView = (TextView) dashboardLayout.findViewById(R.id.aurora_action_bar_btn_left);
		mRightView = (TextView) dashboardLayout.findViewById(R.id.aurora_action_bar_btn_right);
	}

	/**
	 * Aurora <aven> <2013年9月16日> modify for 自定义view
	 * 
	 * @param resId
	 */
	public void setCustomView(int resId) {
		View view = LayoutInflater.from(context).inflate(resId, this, false);
		if (mCustomView.getChildCount() != 0) {
			mCustomView.removeAllViews();
		}
		mCustomView.addView(view);
	}

	public ViewGroup getCustomView(int viewId) {
		ViewGroup viewGroup=(ViewGroup) mCustomView.findViewById(viewId);
		viewGroup.setOnTouchListener(backButtonChangeListener_custom);
		return viewGroup;
	}
	
	public View getHomeTextView( ) {
		return mHomeTextView;
	}
	
	private void bindViewsEvent( ) {

			switch (mType) {
			case Custom:
				mCustomView = (FrameLayout) findViewById(R.id.aurora_action_bar_custom_view);
				setHomeButton();
				mHomeLayout = (LinearLayout)findViewById(R.id.aurora_action_bar_home_layout);
				mHomeTextView = (TextView) findViewById(R.id.aurora_action_bar_home_item_back);
				mHomeTextView.setOnTouchListener(backButtonChangeListener_custom);
				mHomeTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						setBackButtonAction();
						return;
					}
				});
				break;
			case Dashboard:
				mCancelView = (TextView) findViewById(R.id.aurora_action_bar_btn_cancel);
				mOkView = (TextView) findViewById(R.id.aurora_action_bar_btn_right);
				mCancelView.setText(getResources().getString(
						R.string.aurora_action_bar_cancel_btn));
				mOkView.setText(getResources().getString(
						R.string.aurora_action_bar_ok_btn));
				break;

			case Empty:
				mTitleView = (TextView) findViewById(R.id.aurora_action_bar_title);
				if(mTitleView != null){
				    //mTitleView.playAnim(false);
	                setTitle(mTitle); 
				}

				break;

			case Normal:
			default:
				setHomeButton();
				mHomeLayout = (LinearLayout)findViewById(R.id.aurora_action_bar_home_layout);
				mTitleLayout = findViewById(R.id.aurora_action_bar_title_layout);
				if(mTitleBackground != null){
					//该LinearLayout暂时不需要点击出阴影了
				    //mTitleLayout.setBackground(mTitleBackground);
				    mTitleLayout.setBackgroundColor(mDefaultTitleBackgroundColor);
				}else{
				    mTitleLayout.setBackgroundColor(mDefaultTitleBackgroundColor);
				}
				mTitleLayout.setPadding(mTitleLayout.getPaddingLeft(),
                        mTitleLayout.getPaddingTop(), 20,mTitleLayout.getPaddingBottom());
				mTitleView = (TextView) findViewById(R.id.aurora_action_bar_title);
				setTitle(mTitle);
				mHomeTextView = (TextView) findViewById(R.id.aurora_action_bar_home_item_back);
				mHomeTextView.setOnTouchListener(backButtonChangeListener);
				mHomeTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
				    Log.e("action", "onClick");
					// mHomeButton
					// .setBackgroundDrawable(getContext()
					// .getResources()
					// .getDrawable(
					// R.drawable.aurora_action_bar_back_pressed));
					// mHomeButton
					// .setImageResource(R.drawable.aurora_action_bar_back_pressed);
					// ((Activity) context).finish();
					setBackButtonAction();
					return;
				}
			});
			break;
		}
	}

	/*
	 * 在渲染后，完成事件绑定工作
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (!mMerging) {

			bindViewsEvent( );
		}
		
		views = new ArrayList<View>();
		for (int i = 0; i < getChildCount(); i++) {
			View view2 = getChildAt(i);
			views.add(view2);
		}
	}

	private void setHomeButton() {
		mHomeButton = (ImageButton) findViewById(R.id.aurora_action_bar_home_item);
		if(mHomeButton == null){
		    return;
		}
		mHomeButton.setOnClickListener(mClickHandler);
		if(!(mType == Type.Custom)){
		    mHomeButton.setOnTouchListener(backButtonChangeListener);
		} else {
			mHomeButton.setOnTouchListener(backButtonChangeListener_custom);
		}
		
		// mHomeButton.setBackgroundDrawable(getContext().getResources()
		// .getDrawable(R.drawable.aurora_action_bar_back));
//		if(context.getTheme())
//		context.getTheme()
		mHomeButton.setVisibility(View.VISIBLE);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, (int) getResources().getDimension(
				R.dimen.aurora_action_bar_height));
		if (mType == AuroraActionBar.Type.Custom) {
		   
			lp.gravity = Gravity.TOP;
			//lp.topMargin = DensityUtil.dip2px(getContext(), 17.0f);
			//mHomeButton.setBackgroundColor(0x00000000);
		} else {
			lp.gravity = Gravity.CENTER;
		}
//		lp.leftMargin = (int) getResources().getDimension(
//				R.dimen.aurora_action_bar_margin_left);
		
		mHomeButton.setLayoutParams(lp);
		mHomeButton.setContentDescription(getContext().getString(
				R.string.aurora_go_home));
	}

	public View getHomeButton() {
		return mHomeButton;
	}

	
	//flash add 
	private static Typeface auroraTitleFace;
	private static String localeLanguage;
	
	static {
        
		 //auroraCreateTitleFont( );
		
    }
    
    /*private Typeface auroraCreateTitleFont( ) {
    	try {
    		Typeface tf = Typeface.createFromAsset(context.getAssets(),  
		              "fonts/title.ttf");  
			if ( Locale.getDefault().getLanguage().equals("zh") ) {
				auroraTitleFace = tf;//Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
			} else {
				auroraTitleFace = tf;//Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT_FORENGLISH);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.getCause();
			e.printStackTrace();
			auroraTitleFace = null;
		}
    	localeLanguage = Locale.getDefault().getLanguage();
		return auroraTitleFace;
    }
    
    private Typeface auroraGetTitleFontTypeFace()
    {
		if(auroraTitleFace == null || localeLanguage == null || !Locale.getDefault().getLanguage().equals(localeLanguage)) {
			auroraTitleFace = auroraCreateTitleFont();
		}
		return 	auroraTitleFace;
	}*/
	//flash add end
	
	
	public void setTitle(CharSequence title) {
	    int maxTitleLength = (int)getResources().getDimension(R.dimen.aurora_action_bar_title_maxwidth);
	    String titleSuffix = "...";
	    int titleWidth = 0;
	    int index = 0;
		if (mTitleView != null) {
			/*
		//	Typeface titleFace = Typeface
		//			.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		    Paint paint = mTitleView.getPaint();
	        
		    if(paint != null && !TextUtils.isEmpty(title)){
	            for(int i = 0;i< title.length();i++){
	                String sub = title.subSequence(i, i+1).toString();
	                titleWidth += paint.measureText(sub);
	                index = i;
	                if(titleWidth > maxTitleLength){
	                    break;
	                }
	                
	            }
//	            titleWidth = (int)paint.measureText(title.toString());
	        
	        }
	        
			mTitleView.setTypeface(auroraGetTitleFontTypeFace());
			Log.e("width", "titleWidth:"+titleWidth);
			Log.e("width", "maxTitleLength:"+maxTitleLength);
			if(titleWidth > maxTitleLength){
			    title = title.subSequence(0, index-2)+titleSuffix;
            }
            */
		    
		    // mTitleView.setTypeface(auroraGetTitleFontTypeFace());
		    if(!TextUtils.isEmpty(title)) {
		    	title = TextUtils.ellipsize(title, mTitleView.getPaint(), maxTitleLength,
	    				TextUtils.TruncateAt.END);
		    }
    	    
    		mTitleView.setText(title);
    		mTitle = title;
    		setTitleClickable();
		}
		
	}
	
	

	// Aurora <aven> <2013年9月14日> modify for 重载setTitle（），允许设置资源id
	public void setTitle(int title) {
		/*
		if (mTitleView != null) {
			mTitleView.setTypeface(auroraGetTitleFontTypeFace());
			mTitleView.setText(context.getResources().getString(title));
			setTitleClickable();
		}
		*/
		setTitle(context.getResources().getString(title));
	}

	// Aurora <aven> <2013年9月14日> modify for 重载setTitle（），允许设置资源id
	/**
	 * 设置actionbar标题是否可点击
	 */
	private void setTitleClickable() {
		if (!(AuroraActionBar.Type.Empty.toString()).equals(mType.toString())) {
			mTitleView.setOnTouchListener(backButtonChangeListener);
			mTitleView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// mHomeButton
					// .setImageResource(R.drawable.aurora_action_bar_back_pressed);
					// ((Activity) context).finish();
					setBackButtonAction();
					return;
				}
			});
		}
	}
	
	private OnTouchListener backButtonChangeListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
		    
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mHomeButton.onTouchEvent(event);
				if ( mHomeTextView != null ) {
					mHomeTextView.onTouchEvent(event);
				}
				if(mTitleLayout != null){
				    mTitleLayout.onTouchEvent(event);
				}
				if ( mHomeLayout != null ) {
					mHomeLayout.onTouchEvent(event);
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				mHomeButton.setPressed(false);
				if ( mHomeTextView != null ) {
					mHomeTextView.setPressed(false);
				}
				if ( mHomeLayout != null ) {
					mHomeLayout.setPressed(false);
				}
				if(mTitleLayout != null){
				    mTitleLayout.setPressed(false);
				    if(!isOutOfBounds(getContext(), event, mTitleLayout)){
	                    setBackButtonAction();
	                }
				}
				
				
				
			}
			
			if(event.getAction() == MotionEvent.ACTION_MOVE){
			    if(mTitleLayout!=null){
			        if(isOutOfBounds(getContext(), event, mTitleLayout)){
	                    if(mHomeButton != null){
	                        mHomeButton.setPressed(false);
	                    }
	                    if(mHomeTextView != null){
	                    	mHomeTextView.setPressed(false);
	                    }
	                    if(mTitleLayout != null){
	                        mTitleLayout.setPressed(false);
	                    }
	    				if ( mHomeLayout != null ) {
	    					mHomeLayout.setPressed(false);
	    				}
	                } else {
	                	if(mHomeButton != null){
	                		mHomeButton.setPressed(true);
	                	}
	                	if(mHomeTextView != null){
	                    	mHomeTextView.setPressed(true);
	                    }
	                	if(mTitleLayout != null){
	                		mTitleLayout.setPressed(true);
	                	}
	    				if ( mHomeLayout != null ) {
	    					mHomeLayout.setPressed(true);
	    				}
	                }
			    }
			    
			}
			return false;
		}
	};
	
	//aurora add by tangjun start 2014.5.12 专门为Custom布局设置的touch监听，因为Custom没有mTitleLayout了
	private OnTouchListener backButtonChangeListener_custom = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
		    
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.e("222222", "--backButtonChangeListener_custom--ACTION_DOWN--");
				mHomeButton.onTouchEvent(event);
				if ( mHomeTextView != null ) {
					mHomeTextView.onTouchEvent(event);
				}
				if ( mHomeLayout != null ) {
					mHomeLayout.onTouchEvent(event);
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				Log.e("222222", "--backButtonChangeListener_custom--ACTION_UP--");
				mHomeButton.setPressed(false);
				if ( mHomeTextView != null ) {
					mHomeTextView.setPressed(false);
				}
				if ( mHomeLayout != null ) {
					mHomeLayout.setPressed(false);
				}
			    if(!isOutOfBounds(getContext(), event, mHomeButton) || !isOutOfBounds(getContext(), event, mHomeTextView)){
                    setBackButtonAction();
                }
			}
			
			if(event.getAction() == MotionEvent.ACTION_MOVE){
				if(isOutOfBounds(getContext(), event, mHomeButton) && isOutOfBounds(getContext(), event, mHomeTextView)){
					if(mHomeButton != null){
						mHomeButton.setPressed(false);
					}
					if(mHomeTextView != null){
						mHomeTextView.setPressed(false);
					}
					if ( mHomeLayout != null ) {
    					mHomeLayout.setPressed(false);
    				}
				} else {
					if(mHomeButton != null){
						mHomeButton.setPressed(true);
					}
					if(mHomeTextView != null){
						mHomeTextView.setPressed(true);
					}
					if ( mHomeLayout != null ) {
    					mHomeLayout.setPressed(true);
    				}
				} 
			}
			return false;
		}
	};
	
   private boolean isOutOfBounds(Context context, MotionEvent event,View target) {
	        final int x = (int) event.getX();
	        final int y = (int) event.getY();
	        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
	        return (x < -slop) || (y < -slop)
	                || (x > (target.getWidth()+slop))
	                || (y > (target.getHeight()+slop));
    }
	

	private void initActionBar() {
		mItems = new LinkedList<AuroraActionBarItem>();
	}

	public AuroraActionBarItem getItem(int position) {
		if (position < 0 || position >= mItems.size()) {
			return null;
		}
		return mItems.get(position);
	}

	private OnClickListener mClickHandler = new OnClickListener() {

		public void onClick(View v) {
			if (v == mHomeButton) {
				// ((Activity) context).finish();
				setBackButtonAction();
				return;
			}
			
			final int itemCount = mItems.size();
			View itemButton  = null;
			boolean finded = false;
			AuroraActionBarItem item = null;
            for (int i = 0; i < itemCount; i++) {
                item = mItems.get(i);
                itemButton = item.getItemView().findViewById(
                        R.id.aurora_action_bar_item);
                if (v == itemButton) {
                    finded = true;
                    break;
                }
            }
            
			if (mOnActionBarListener != null) {
					    if(finded && item != null && itemButton != null){
					    
						mOnActionBarListener.onAuroraActionBarItemClicked(item
								.getItemId());
//						itemButton.setBackgroundResource(R.drawable.aurora_action_item_base_selector);
						//itemButton.setBackgroundColor(0x00000000);
                        }
			}else{
			    if(finded && item != null  && itemButton != null){
                    //itemButton.setBackgroundColor(0x00000000);
                    
                    }
                }
			}
		
	};

	
	private void setBackButtonAction() {
		Log.e("222222", "-----setBackButtonAction--------");
		if (mOnActionBarBackItemListener != null) {
			mOnActionBarBackItemListener
					.onAuroraActionBarBackItemClicked(OnAuroraActionBarBackItemClickListener.HOME_ITEM);
//			mHomeButton
//					.setImageResource(R.drawable.aurora_action_bar_back_pressed);
//			Message msgMessage = new Message();
//			msgMessage.what = 1;
//			handler.sendMessageDelayed(msgMessage, 100);
//			 Log.e("action", "mOnActionBarBackItemListener");
		} else {
//		    Log.e("action", "finish");
			((Activity) context).onBackPressed();
		}
	}

	public AuroraActionBarItem newActionBarItem(
			Class<? extends AuroraActionBarItem> klass) {
		try {
			AuroraActionBarItem item = klass.newInstance();
			item.setActionBar(this);
			return item;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"The given klass must have a default constructor");
		}
	}

	public AuroraActionBarItem addItem(int drawableId, int itemId, String title) {
		return addItem(newActionBarItem(NormalAuroraActionBarItem.class)
				.getDrawableId(drawableId).setContentDescription(title), itemId);
	}

	public AuroraActionBarItem addItem(
			AuroraActionBarItem.Type actionBarItemType) {
		return addItem(
				AuroraActionBarItem.createWithType(this, actionBarItemType),
				NONE);
	}

	public AuroraActionBarItem addItem(
			AuroraActionBarItem.Type actionBarItemType, int itemId) {
		return addItem(
				AuroraActionBarItem.createWithType(this, actionBarItemType),
				itemId);
	}

	public AuroraActionBarItem addItem(AuroraActionBarItem item) {
		return addItem(item, NONE);
	}
	
	public AuroraActionBarItem addItem(int resId, int itemId) {
		return addItem(newActionBarItem(CustomAuroraActionBarItem.class)
				.setResId(resId), itemId);
	}

	public AuroraActionBarItem addItem(AuroraActionBarItem item, int itemId) {
		if (mItems.size() >= mMaxItemsCount) {
			return null;
		}
		if (item != null) {
			item.setItemId(itemId);
			final View itemView = item.getItemView();
			if (!(item instanceof CustomAuroraActionBarItem)) {
			    View v = itemView.findViewById(R.id.aurora_action_bar_item);
			    v.setOnClickListener(mClickHandler);
						
			}
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, (int) getResources().getDimension(
					R.dimen.aurora_action_bar_height));
			if (mType == AuroraActionBar.Type.Custom) {
				lp.gravity = Gravity.TOP;
			} else {
				lp.gravity = Gravity.CENTER;
			}
//			lp.rightMargin = (int) getResources().getDimension(
//					R.dimen.aurora_action_bar_margin_right);
			
			//对normal布局外面加个整体布局
			if ( mType == Type.Normal ) {
				mLinearNormal = (LinearLayout) findViewById(R.id.aurora_action_bar_normal_linear);
				if ( mLinearNormal != null ) {
					mLinearNormal.addView(itemView, lp);
				}
				
			} else {
				mLinear = (LinearLayout) findViewById(R.id.auroraActionbarLayout);
				if ( mLinear != null ) {
					mLinear.addView(itemView, lp);
				}
			}
			
			mItems.add(item);
		}

		return item;
	}
	
	/**
	 * match public AuroraActionBarItem addItem(int drawableId, int itemId, String title);
	 */
	public void changeItemDrawable( int drawableId, int itemId, String title ) {
		removeItemByItemId(itemId);
		addItem(drawableId, itemId, title);
	}
	
	/**
	 * match public AuroraActionBarItem addItem(AuroraActionBarItem.Type actionBarItemType, int itemId);
	 */
	public void changeItemType( AuroraActionBarItem.Type actionBarItemType, int itemId ) {
		removeItemByItemId(itemId);
		addItem(actionBarItemType, itemId);
	}
	
	/**
	 * match public AuroraActionBarItem addItem(int resId, int itemId);
	 */
	public void changeItemLayout( int resId, int itemId ) {
		removeItemByItemId(itemId);
		addItem(resId, itemId);
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * Aurora <aven> <2013年9月14日> modify for 返回键是否可用
	 * 
	 * @param showBackButton
	 */
	public void setDisplayHomeAsUpEnabled(boolean showBackButton) {
		mHomeButton.setVisibility(showBackButton == true ? View.VISIBLE
				: View.GONE);
		mHomeTextView.setVisibility(showBackButton == true ? View.VISIBLE
				: View.GONE);
		int visibility = mHomeTextView.getVisibility();
//		mTitleView.playAnim((visibility == View.VISIBLE)?false:true);
		if (!showBackButton) {
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT);
			lp.leftMargin = (int) getResources().getDimension(
					R.dimen.aurora_action_bar_margin_right);
			mTitleView.setLayoutParams(lp);
			mTitleView.setEnabled(false);
			mHomeButton.setEnabled(false);
			mHomeTextView.setEnabled(false);
		} else {
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT);
			lp.leftMargin = 0;
			mTitleView.setLayoutParams(lp);
			mTitleView.setEnabled(true);
			mHomeButton.setEnabled(true);
			mHomeTextView.setEnabled(true);
		}
		// mHomeButton.setVisibility(View.GONE);
	}

	/**
	 * Aurora <aven> <2013年9月14日> modify for 根据itemId设置按钮是否可用
	 * 
	 * @param itemId
	 * @param isVisiable
	 */
	public void setDisplayOptions(int itemId, boolean isVisiable) {
		for (int i = 0; i < mItems.size(); i++) {
			AuroraActionBarItem item = mItems.get(i);
			if (item.getItemId() == itemId) {
				item.getItemView()
						.findViewById(
								R.id.aurora_action_bar_item)
						.setVisibility(
								isVisiable == true ? View.VISIBLE : View.GONE);
				break;
			}
		}
	}

	/**
	 * Aurora <aven> <2013年9月14日> modify for actionbar 标题是否可触发返回
	 * 
	 * @param titleClickable
	 */
	public void setHomeButtonEnabled(boolean titleClickable) {
		mTitleView.setEnabled(titleClickable == true ? true : false);
	}

	// Aurora <aven> <2013年9月16日> modify for sms menu begin
	public void initActionBarMenu(AuroraActionBar auroraActionBar, int resId,
			int animMenu) {
		if (auroraMenu == null) {
			auroraMenu = new AuroraMenu(auroraActionBar, context, resId,
					animMenu);
			view = ((Activity) context).getWindow().getDecorView();
			rect = new Rect();
		}
	}

	public void initActionBarMenu(int resId) {
//		initActionBarMenu(this, resId,
//				R.style.ActionBarPopupAnimation);
		initActionBarMenu(this, resId, -1);
	}

	public AuroraMenu getActionBarMenu() {
		return auroraMenu;
	}
	
	//其他actionbar类型跳转到dashboard类型 add by tangjun 2013.12.03 start
	public void goToActionBarSelectView() {
		if (firstCreateAllOperation) {
			views = new ArrayList<View>();
			for (int i = 0; i < getChildCount(); i++) {
				View view2 = getChildAt(i);
				views.add(view2);
			}
			removeAllViews();
			
			addView(dashboardLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

			firstCreateAllOperation=false;
		}
	}
	
	public void goToActionBarSelectViewEx() {
		removeAllViews();
		if ( dashboardLayout != null ) {
			addView(dashboardLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		}
	}

	public View getSelectLeftButton() {
		return mLeftView;
	}

	public View getSelectRightButton() {
		return mRightView;
	}

	/**
	 * 返回到初始布局
	 */
	public void backToActionBarFrontViewEx() {
		removeAllViews();
		
		if(auroraCurActionBarContent != null) {
			addView(auroraCurActionBarContent);
		}
	}
	
	public void backToActionBarFrontView() {
		removeAllViews();
		for (int i = 0; i < views.size(); i++) {
			View view2 = views.get(i);
			addView(view2);
		}
		firstCreateAllOperation=true;
	}
	
	/**
	 * 批量删除显示 Dashboard类型
	 */
	public void showActionBarDashBoard() {
		
//		if ( isDashBoardAnimRun ) {
//			//dashboardhandler.removeMessages(AuroraUtil.ACTION_BAR_DASHBOARD_MISS);
//			dashboardhandler.removeMessages(AuroraUtil.ACTION_BAR_DASHBOARD_SHOW);
//			loadDashBoardAnimation(R.anim.aurora_action_bar_dashboard_exit);
//			Message msgMessage = new Message();
//			msgMessage.what = AuroraUtil.ACTION_BAR_DASHBOARD_MISS;
//			dashboardhandler.sendMessageDelayed(msgMessage, 300);
//			showActionBottomeBarMenu();
//			return;
//		}
//		
//		view = ((Activity) context).getWindow().getDecorView();
//		menuType = AuroraMenu.Type.BottomBar;
//		if ( firstCreateAllOperation ) {
//			isDashBoardAnimRun = true;
//			loadNotDashBoardAnimation(R.anim.aurora_action_bar_dashboard_exit);
//			Message msgMessage = new Message();
//			msgMessage.what = AuroraUtil.ACTION_BAR_DASHBOARD_SHOW;
//			dashboardhandler.sendMessageDelayed(msgMessage, 300);
//		} else {
//			isDashBoardAnimRun = true;
//			loadDashBoardAnimation(R.anim.aurora_action_bar_dashboard_exit);
//			Message msgMessage = new Message();
//			msgMessage.what = AuroraUtil.ACTION_BAR_DASHBOARD_MISS;
//			dashboardhandler.sendMessageDelayed(msgMessage, 300);
//		}
//		showActionBottomeBarMenu();
		
		auroraPlayEditModeAnim();
	}
	
	private Handler dashboardhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == AuroraUtil.ACTION_BAR_DASHBOARD_SHOW) {
				goToActionBarSelectView();
				loadDashBoardAnimation(R.anim.aurora_action_bar_dashboard_enter);
			} else if (msg.what == AuroraUtil.ACTION_BAR_DASHBOARD_MISS) {
				backToActionBarFrontView();
				loadNotDashBoardAnimation(R.anim.aurora_action_bar_dashboard_enter);
			}
		}
	};
	
	public void loadNotDashBoardAnimation(int animId) {
		menuAnimation = AnimationUtils.loadAnimation(context, animId);
		//对normal布局整体做动画，防止箭头和文本动画不同步了
		if ( mType == Type.Normal ) {
			LinearLayout linear = (LinearLayout) findViewById(R.id.aurora_action_bar_normal_linear);
			linear.startAnimation(menuAnimation);
		} else {
			if (mTitleView != null)
				mTitleView.startAnimation(menuAnimation);
			if (mHomeButton != null)
				mHomeButton.startAnimation(menuAnimation);
			for (int i = 0; i < mItems.size(); i++) {
				View view = mItems.get(i).getItemView();
				if (view.getVisibility() == View.VISIBLE ) {
					view.startAnimation(menuAnimation);
				}
			}
			if(mCustomView!=null){
				mCustomView.startAnimation(menuAnimation);
			}
		}
		
		setDashBoardAnimationListener(animId);
	}
	
	public void loadDashBoardAnimation(int animId) {
		menuAnimation = AnimationUtils.loadAnimation(context, animId);
		
		dashboardLayout.startAnimation(menuAnimation);
		
		/*
		mLeftView.startAnimation(menuAnimation);
		mRightView.startAnimation(menuAnimation);
		*/
		
		setDashBoardAnimationListener(animId);
	}
	
	private void setDashBoardAnimationListener( int animId ) {
		if ( animId == R.anim.aurora_action_bar_dashboard_enter ) {
			menuAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					isDashBoardAnimRun = false;
				}
			});
		}
	}
	
	/**
	 * @return    add by tangjun 2014.1.15
	 */
	public boolean getIsDashBoardAnimRun( ) {
		return isDashBoardAnimRun;
	}
	
	public void setIsNeedShowAuroraActionBottomBarMenu( boolean isNeedShowAuroraActionBottomBarMenu ) {
		this.isNeedShowAuroraActionBottomBarMenu = isNeedShowAuroraActionBottomBarMenu;
	}
	//其他actionbar类型跳转到dashboard类型 add by tangjun 2013.12.03 end

	/**
	 * 批量删除显示菜单
	 */
	public void showActionBarMenu() {
		view = ((Activity) context).getWindow().getDecorView();
		menuType = AuroraMenu.Type.BottomBar;
		if (auroraMenu.isShowing()) {
			loadSelectMenuAnimation(R.anim.aurora_action_bar_menu_exit);
			Message msgMessage = new Message();
			msgMessage.what = AuroraUtil.ACTION_BAR_MENU_DISMISS;
			handler.sendMessageDelayed(msgMessage, 300);
			// loadMenuAnimation(R.anim.aurora_action_bar_menu_enter);
		} else {
			view.getWindowVisibleDisplayFrame(rect);
			statusBarHeight = rect.top;
			loadMenuAnimation(R.anim.aurora_action_bar_menu_exit);
			Message msgMessage = new Message();
			msgMessage.what = AuroraUtil.ACTION_BAR_MENU_SHOW;
			handler.sendMessageDelayed(msgMessage, 300);
			// auroraMenu.showAtLocation(view, Gravity.TOP, 0, statusBarHeight);
			// loadSelectMenuAnimation(R.anim.aurora_action_bar_menu_enter);
			// this.startAnimation(animation);
			// loadMenuAnimation(R.anim.aurora_action_bar_menu_exit);
		}
		showActionBottomeBarMenu();
		
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == AuroraUtil.ACTION_BAR_MENU_DISMISS) {
				auroraMenu.dismiss();
				loadMenuAnimation(R.anim.aurora_action_bar_menu_enter);
			} else if (msg.what == AuroraUtil.ACTION_BAR_MENU_SHOW) {
				auroraMenu.showAtLocation(view, Gravity.TOP, 0, statusBarHeight);
				loadSelectMenuAnimation(R.anim.aurora_action_bar_menu_enter);
			}
		}
	};
	
	public void loadSelectMenuAnimation(int animId) {
		menuAnimation = AnimationUtils.loadAnimation(context, animId);
		auroraMenu.getActionMenuLeftView().startAnimation(menuAnimation);
		auroraMenu.getActionMenuRightView().startAnimation(menuAnimation);
	}
	
	public void loadMenuAnimation(int animId) {
		menuAnimation = AnimationUtils.loadAnimation(context, animId);
		if (mTitleView != null)
			mTitleView.startAnimation(menuAnimation);
		if (mHomeButton != null)
			mHomeButton.startAnimation(menuAnimation);
		for (int i = 0; i < mItems.size(); i++) {
			View view = mItems.get(i).getItemView();
			if (view.getVisibility() == View.VISIBLE ) {
				view.startAnimation(menuAnimation);
			}
		}
		if(mCustomView!=null){
			mCustomView.startAnimation(menuAnimation);
		}
	}
	
	public void loadMenuAnimationEx(boolean outAnim) {
		if ( outAnim ) {
			menuAnimation = AnimationUtils.loadAnimation(context, R.anim.aurora_action_bar_up_exit);
		} else {
			menuAnimation = AnimationUtils.loadAnimation(context, R.anim.aurora_action_bar_up_enter);
		}

		if (mTitleView != null)
			mTitleView.startAnimation(menuAnimation);
		if (mHomeButton != null)
			mHomeButton.startAnimation(menuAnimation);
		for (int i = 0; i < mItems.size(); i++) {
			View view = mItems.get(i).getItemView();
			view.startAnimation(menuAnimation);
		}
		if(mCustomView!=null){
			mCustomView.startAnimation(menuAnimation);
		}
	}

	public void contentViewFloatUp() {
		if ( activity.getContentView().getParent() instanceof LinearLayout ) {
			Log.e("222222", "contentViewFloatUp---LinearLayout");
			LayoutParams lp = (LayoutParams) activity.getContentView()
					.getLayoutParams();
			lp.bottomMargin = (int) context.getResources().getDimension(
					R.dimen.aurora_action_bottom_bar_height);
			activity.getContentView().setLayoutParams(lp);
		}
	}

	public void contentViewFloatDown() {
		if ( activity.getContentView().getParent() instanceof LinearLayout ) {
			Log.e("222222", "contentViewFloatDown---LinearLayout");
			LayoutParams lp = (LayoutParams) activity.getContentView()
					.getLayoutParams();
			lp.bottomMargin = 0;
			activity.getContentView().setLayoutParams(lp);
		}
	}

	// Aurora <aven> <2013年9月16日> modify for sms menu end

	// Aurora <aven> <2013年9月18日> modify for action bottom bar menu begin

	public void initActionBottomBarMenu(int menuId, int count) {
		view = ((Activity) context).getWindow().getDecorView();
		setActionBottomBarMenu(true);
		isShowBottomBarMenu = true;
		// activity.setAuroraMenuItems(menuId,
		// R.layout.aurora_action_bottom_bar);
		choseBottomBarStyle(menuId, count);
	}

	/**
	 * 选择底部导航类型
	 * 
	 * @param menuId
	 * @param count
	 */
	private void choseBottomBarStyle(int menuId, int count) {
		try {
			switch (count) {
			case 1:
				activity.setAuroraMenuItems(menuId,
						R.layout.aurora_action_bottom_bar_style_1);
				break;
			case 2:
				activity.setAuroraMenuItems(menuId,
						R.layout.aurora_action_bottom_bar_style_2);
				break;
			case 3:
				activity.setAuroraMenuItems(menuId,
						R.layout.aurora_action_bottom_bar_style_3);
				break;
			case 4:
				activity.setAuroraMenuItems(menuId,
						R.layout.aurora_action_bottom_bar_style_4);
				break;
			case 5:
				activity.setAuroraMenuItems(menuId,
						R.layout.aurora_action_bottom_bar_style_5);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isActionBottomBarMenu() {
		return isActionBottomBarMenu;
	}

	public void setActionBottomBarMenu(boolean opt) {
		isActionBottomBarMenu = opt;
	}

	public AuroraMenu getAuroraActionBottomBarMenu() {
		return auroraActionBottomBarMenu;
	}

	public void setAuroraActionBottomBarMenu(
			AuroraMenu auroraActionBottomBarMenu) {
		this.auroraActionBottomBarMenu = auroraActionBottomBarMenu;
	}

	/**
	 * 显示actionbar底部菜单
	 */
	public void showActionBottomeBarMenu() {
		if ( isNeedShowAuroraActionBottomBarMenu ) {
			if (isShowBottomBarMenu) {
				auroraActionBottomBarMenu
						.showAtLocation(view, Gravity.BOTTOM, 0, 0);
				contentViewFloatUp();
			} else {
				if(auroraActionBottomBarMenu.isShowing()){
					auroraActionBottomBarMenu.dismiss();
					contentViewFloatDown();
				}
			}
		}
	}

	public boolean isShowBottomBarMenu() {
		return isShowBottomBarMenu;
	}

	public void setShowBottomBarMenu(boolean isShowBottomBarMenu) {
		this.isShowBottomBarMenu = isShowBottomBarMenu;
	}

	public AuroraMenu.Type getMenuType() {
		return menuType;
	}

	public void setMenuType(AuroraMenu.Type menuType) {
		this.menuType = menuType;
	}

	// Aurora <aven> <2013年9月18日> modify for action bottom bar menu end

	/**
	 * @param item
	 */
	public void removeItem(AuroraActionBarItem item) {
		removeItem(mItems.indexOf(item));
	}

	/**
	 * @param position
	 */
	public void removeItem(int position) {

		if (position < 0 || position >= mItems.size()) {
			return;
		}
		
		if ( mType == Type.Normal ) {
			if ( mLinearNormal != null ) {
				final int viewIndex = mLinearNormal.indexOfChild(mItems.get(position).getItemView());
				mLinearNormal.removeViews(viewIndex, 1);
			}
		} else {
			if ( mLinear != null ) {
				final int viewIndex = mLinear.indexOfChild(mItems.get(position).getItemView());
				Log.e("111111", "-viewIndex--- = " + viewIndex);
				mLinear.removeViews(viewIndex, 1);
			}
		}
		mItems.remove(position);
	}
	
	/**
	 * @param by itemId
	 */
	public void removeItemByItemId(int itemId) {
		for( int i = 0; i < mItems.size(); i++ ) {
			AuroraActionBarItem item = mItems.get(i);
			if ( item.getItemId() == itemId ) {
				removeItem( i );
			}
		}
	}
	
	public void setHomeLayoutBackground( Drawable background ) {
		if ( mHomeLayout != null ) {
			mHomeLayout.setBackground(background);
		}
	}
	public void setHomeLayoutBackgroundResource( int resid ) {
		if ( mHomeLayout != null ) {
			mHomeLayout.setBackgroundResource(resid);
		}
	}
	public void setHomeLayoutBackgroundColor( int color ) {
		if ( mHomeLayout != null ) {
			mHomeLayout.setBackgroundColor(color);
		}
	}
	
	/****************************************************************************************************
	 * 
	 * 							flash add 
	 * 
	 * 							reset entry editMode anim
	 * 								
	 * 							2014.3.16
	 * 
	 * 							start
	 * 
	 * ***************************************************************************************************/
	 
	 private AnimatorSet auroraEntryEditModeAnimator;
	 
	 private AnimatorSet auroraExitEditModeAnimator;
	 
	 private View auroraCurActionBarContent;
	 
	 private boolean auroraEditMode = false;
	 
	 private static final int AURORA_EDIT_MODE_ANIM_TIME = 300;
	 
	 private static final float AURORA_EDIT_MODE_ANIM_TRANSITION_HEIGHT = -100f;
	 
	 private void auroraInitEditModeAnimObjects()
	 {
		 if(auroraEntryEditModeAnimator == null)
			 auroraEntryEditModeAnimator = new AnimatorSet();
		 
		 if(auroraExitEditModeAnimator == null)
			 auroraExitEditModeAnimator = new AnimatorSet();
		 
		 auroraSetEditMode(false);
	 }
	 
	private void auroraPlayEditModeAnim()
	{
		Log.e("liuwei","auroraIsEditMode() = " + auroraIsEditMode());
		if (!auroraIsEditMode()) 
		{
			auroraPlayEntryEditModeAnim();
			
		} else 
		{
			auroraPlayExitEditModeAnim();
		}
		showActionBottomeBarMenu();
	}
	 
	 private void auroraSetEditMode(boolean mode)
	 {
		 auroraEditMode = mode;
	 }
	 
	 public boolean auroraIsEditMode()
	 {
		 return auroraEditMode;
	 }
	 
	 private void auroraPlayEntryEditModeAnim()
	 {
		 if(auroraIsExitEditModeAnimRunning() || auroraIsEntryEditModeAnimRunning())return;
		 
		 auroraCurActionBarContent = this.findViewById(R.id.auroraActionbarLayout);
		 
		 ObjectAnimator translateOut = ObjectAnimator.ofFloat(auroraCurActionBarContent, "TranslationY", 0f,AURORA_EDIT_MODE_ANIM_TRANSITION_HEIGHT);
		 
		 translateOut.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 ObjectAnimator alphaOut = ObjectAnimator.ofFloat(auroraCurActionBarContent, "Alpha", 1.0f,0.0f);
		 
		 alphaOut.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 ObjectAnimator translateIn = ObjectAnimator.ofFloat(dashboardLayout, "TranslationY", AURORA_EDIT_MODE_ANIM_TRANSITION_HEIGHT,0f);
		 
		 translateIn.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 ObjectAnimator alphaIn = ObjectAnimator.ofFloat(dashboardLayout, "Alpha", 0.0f,1.0f);
		 
		 alphaIn.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 translateOut.addListener(auroraEntryEditModeOutAnimListener);
		 
		 if(auroraEntryEditModeAnimator != null)
		 {
		 
			 auroraEntryEditModeAnimator.play(translateOut).with(alphaOut);
		 
			 auroraEntryEditModeAnimator.play(translateIn).with(alphaIn).after(translateOut);
			 
			 auroraSetEditMode(true);
			 
			 auroraEntryEditModeAnimator.start();
		 
		 }
		 
	 }
	
	 
	 private void auroraPlayExitEditModeAnim()
	 {
		 if(auroraIsExitEditModeAnimRunning() || auroraIsEntryEditModeAnimRunning())return;
		 
		 ObjectAnimator translateOut = ObjectAnimator.ofFloat(dashboardLayout, "TranslationY", 0f,AURORA_EDIT_MODE_ANIM_TRANSITION_HEIGHT);
		 
		 translateOut.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 ObjectAnimator alphaOut = ObjectAnimator.ofFloat(dashboardLayout, "Alpha", 1.0f,0.0f);
		 
		 alphaOut.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 ObjectAnimator translateIn = ObjectAnimator.ofFloat(auroraCurActionBarContent, "TranslationY", AURORA_EDIT_MODE_ANIM_TRANSITION_HEIGHT,0f);
		 
		 translateIn.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 ObjectAnimator alphaIn = ObjectAnimator.ofFloat(auroraCurActionBarContent, "Alpha", 0.0f,1.0f);
		 
		 alphaIn.setDuration(AURORA_EDIT_MODE_ANIM_TIME);
		 
		 translateOut.addListener(auroraExitEditModeOutAnimListener);
		 
		 if(auroraExitEditModeAnimator != null) 
		 {
			 auroraExitEditModeAnimator.play(translateOut).with(alphaOut);
		 
		 	auroraExitEditModeAnimator.play(translateIn).with(alphaIn).after(translateOut);
		 	
		 	auroraSetEditMode(false);
		 	
		 	auroraExitEditModeAnimator.start();
		 }

	 }
	
	private android.animation.Animator.AnimatorListener auroraEntryEditModeOutAnimListener = new android.animation.Animator.AnimatorListener()
	{

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			removeAllViews();
			
			if(dashboardLayout != null)
				addView(dashboardLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub
			
		}
				 
	}; 
	
	private android.animation.Animator.AnimatorListener auroraExitEditModeOutAnimListener = new android.animation.Animator.AnimatorListener()
	{

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			removeAllViews();
			
			if(auroraCurActionBarContent != null)
				addView(auroraCurActionBarContent);
			
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub
			
		}
				 
	}; 
	
	public boolean auroraIsExitEditModeAnimRunning()
	{
		if(auroraExitEditModeAnimator == null)return false;
		
		return auroraExitEditModeAnimator.isRunning();
	}
	
	public boolean auroraIsEntryEditModeAnimRunning()
	{
		if(auroraEntryEditModeAnimator == null)return false;
		
		return auroraEntryEditModeAnimator.isRunning();
	}
	
	
	Handler auroraSynchronousClickableHanlder = new Handler();
	
	private boolean auroraActionBarClickable = true;
	
	private static final int auroraActionBarNotClickableTimeOut = 100;
	
	private Runnable auroraSynchronousClickableRunnable = new Runnable()
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			auroraActionBarClickable = true;
		}
		
	};
	
	public boolean auroraActionBarAllowClickable()
	{
		if(auroraActionBarClickable)
		{
			auroraActionBarClickable = false;
			
			auroraSynchronousClickableHanlder.postDelayed(auroraSynchronousClickableRunnable, auroraActionBarNotClickableTimeOut);
			
			return true;
		}
		
		return false;
	}
	/****************************************************************************************************
	 * 
	 * 							flash add 
	 * 
	 * 							reset entry editMode anim
	 * 								
	 * 							2014.3.16
	 * 
	 * 							end
	 * 
	 * ***************************************************************************************************/
}
