
package aurora.lib.widget;

import android.content.Context;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.view.View.OnTouchListener;

import com.aurora.lib.R;
import com.aurora.lib.utils.AuroraLog;
import com.aurora.lib.utils.DensityUtil;
import com.aurora.lib.utils.EventUtils;
import com.aurora.lib.utils.FontUtils;
public class AuroraCustomActionBar extends AuroraAbsActionBar implements OnClickListener,OnTouchListener{

    private static final int AURORA_FULL_TRANSPARENT = 0x00000000;
    
    private static final int AURORA_SEARCH_ICON_MAX_LEVELS = 13;
    
    
    private int mTitleStyle;

    private int mSubTitlestyle;

    private Drawable mBackground;

    private Drawable mIconDrawable;

    private View mParent;

    private Activity mParentActivity;

    private int mTempTouchDownY, mTouchDownY;

    private int mTouchSlop;

    private FrameLayout mBottomSearchBarLayout;

    private LinearLayout mBottomSearchViewParent;

    private LinearLayout mBottomSearchIconPanle;

    private ImageButton mBottomSearchShowButton;

    private int mSearchIconX, mSearchIconY;

    private int mMinTouchSpeedY, mMaxTouchSpeedY;

    private int mSearchViewWidth, mSearchViewHeight;

    private int mSearchIconPaddingTop, mSearchIconPaddingRight;

    private ImageButton mSearchView;

    private View mSearchHintTextView;

    private FrameLayout.LayoutParams mBottomSearchViewParentParams;

    private FrameLayout.LayoutParams mSearchIconParams, mHintTextParams;

    private LinearLayout.LayoutParams mSearchViewParams;

    private float mSearchViewDefaultLeftMargin;

    private int mActionBarHeight;

    private int mMoveDetalY;

    private int mScreenWidth, mSearchViewMarginLeft, mSearchViewMarginRight;

    private float mSearchIconDefaultMarginLeft, mSearchHintTextDefalutMarginLeft;

    private float mActionBarIconMarginTop;

    private onSearchViewClickedListener mSearchViewClickedListener;

    private onOptionItemClickListener mOptionItemClickListener;

    private VelocityTracker mVelocityTracker;
    private int mSpeed;

    private ImageButton mDefaultOptionButton;

    private Drawable mBigSearchIcon, mSmallSearchIcon;

    ValueAnimator mSearchIconScaleAnimation;
    
    private boolean mSeachIconClicked;
    private boolean mSearchIconInTop;
    private boolean mDealTitleEvent = true;
    
    private Context mContext;
	
    private int mHeight;
    
    private View mTitlePanelLayout;
	
    private OnItemClickListener mItemClickListener;
    
    /*
     * 更新底部搜索条动画的Handler对象
     */
    private Handler mUpdateLayoutHandler;
    
    private float mUpdateLayoutProgress;
    /**
     * 更新和处理底部搜索条动画的线程，通过Handler机制来实现避免主线程阻塞
     */
    private Runnable mUpdateLayoutThread = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            auroraUpdateHintTextAlpha(mUpdateLayoutProgress);
            
            auroraUpdateSearchViewBgAlpha(mUpdateLayoutProgress);
            
            auroraUpdateTopMargins(mUpdateLayoutProgress);
            
            auroraUpdateSearchViewBgLeft(mUpdateLayoutProgress);
            
            auroraUpdateSearchIconLeft(mUpdateLayoutProgress);
        }
    };
    private static Typeface mTitleFace;
	
    /**
     * 加载actionbar字体文件
     */
    static {

        mTitleFace = FontUtils.auroraCreateTitleFont(AuroraUtil.ACTION_BAR_TITLE_FONT);

    }

    /**
     * actionbar上搜索条的点击事件回调接口
     * @author luofu
     *
     */
    public interface onSearchViewClickedListener {
        public void click(View searchView);
    }

    /**
     * 右边默认操作组件的点击回调接口
     * @author luofu
     *
     */
    public interface onOptionItemClickListener {
        public void click(View item);

    }

    public void setOnOptionItemClickListener(onOptionItemClickListener listener) {
        mOptionItemClickListener = listener;
    }

    public void setOnSearchViewClickListener(onSearchViewClickedListener listener) {
        mSearchViewClickedListener = listener;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public AuroraCustomActionBar(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public AuroraCustomActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public AuroraCustomActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mContext = context;
        mInflater.inflate(com.aurora.lib.R.layout.aurora_custom_action_bar, this, true);
        mHeight = getContext().getResources()
                .getDimensionPixelSize(com.aurora.lib.R.dimen.aurora_action_bar_height);
        mUpdateLayoutHandler = new Handler();
        initView();
        
    }

    /**
     * show or hide searchview on action bar
     * @param show  flag to show or hide searchview
     * @param position searchview position on action bar
     */
    public void showSearchView(boolean show,int position){
        if(show){
            mBottomSearchViewParent.setVisibility(View.VISIBLE);
            mBottomSearchBarLayout.setVisibility(View.VISIBLE);
            mBottomSearchIconPanle.setVisibility(View.VISIBLE);
        }else{
            mBottomSearchViewParent.setVisibility(View.GONE);
            mBottomSearchBarLayout.setVisibility(View.GONE);
            mBottomSearchIconPanle.setVisibility(View.GONE);
        }
        calculateContentPosition(show);
    }
    
    private void calculateContentPosition(boolean showSearchView){

    }
    
    /**
     * 以后所有的activity的contentView都会被加载到这里来，这样减少了调用的复杂程度，便于维护，
     * 在AuroraActivity中直接调用该接口来加载内容布局视图
     * @param content
     */
    public void addContentView(View content){
        if(mBottomLayout != null){
            mBottomLayout.addView(content, 0);
        }
    }
    
    /**
     * @see addContentView(View content)
     * @param layoutRes
     */
    public void addContentView(int layoutRes){
        View view = mInflater.inflate(layoutRes, null);
        if(view != null){
            addContentView(view);
        }
    }
    /**
     * bind action bar to activity
     * 
     * @param activity activity need to show action bar
     */
    public void bindToActivity(Activity activity) {
        this.mParentActivity = activity;
    }

    private void initView() {
        mTitleView = (TextView) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_title);
        mSubTitleView = (TextView) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_subtitle);

        mTitlePanelLayout = findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_title_panel);
        
        mMiddleLayout = (LinearLayout) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_middle_panel);
        mOptionLayout = (LinearLayout) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_option_panel);
        mBottomLayout = (FrameLayout) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_bottom_widget);
        mCusomTitleLayout = (LinearLayout) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_title_custom);

        mParent = findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_body);

        mBottomSearchIconPanle = (LinearLayout) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_bottom_search_icon_panel);

        mDefaultOptionButton = (ImageButton) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_default_option_items);

        mHomeIcon = (ImageButton) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_title_icon);

        mBottomSearchBarLayout = (FrameLayout) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_bottom_search_widget);

        mBottomSearchViewParent = (LinearLayout) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_search_view_parent);

        mBottomSearchShowButton = (ImageButton) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_search_view_icon);

        mSearchView = (ImageButton) findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_search_view);

        mSearchHintTextView = findViewById(com.aurora.lib.R.id.aurora_custom_action_bar_search_hint_text);

        mSearchViewWidth = mSearchView.getRight() - getLeft();

        mSearchIconX = (int) mBottomSearchShowButton.getX();

        mBottomSearchShowButton.setAlpha(0.5f);
        mSearchHintTextView.setAlpha(0.5f);
        
        mSearchViewParams = (LayoutParams) mSearchView.getLayoutParams();
        mBottomSearchViewParentParams = (FrameLayout.LayoutParams) mBottomSearchViewParent
                .getLayoutParams();

        if (mBottomSearchViewParent != null) {
            mBottomSearchViewParentParams = (android.widget.FrameLayout.LayoutParams) mBottomSearchViewParent
                    .getLayoutParams();
        }

        mSearchIconParams = (FrameLayout.LayoutParams) mBottomSearchIconPanle.getLayoutParams();

        // mHintTextParams =
        // (FrameLayout.LayoutParams)mSearchHintTextView.getLayoutParams();

        mActionBarHeight = getContext().getResources().getDimensionPixelSize(
                com.aurora.lib.R.dimen.aurora_action_bar_height);

        mTouchSlop = ViewConfiguration.getTapTimeout();
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinTouchSpeedY = configuration.getScaledMinimumFlingVelocity();
        mMaxTouchSpeedY = configuration.getScaledMaximumFlingVelocity();

        mScreenWidth = DensityUtil.getDisplayHeight(getContext())[1];
        mScreenWidth = mScreenWidth - mBottomSearchViewParentParams.leftMargin
                - mBottomSearchViewParentParams.rightMargin;
        mSearchViewMarginLeft = mBottomSearchViewParentParams.leftMargin;

        mTitleView.setOnTouchListener(this);
        
        setOnClickListener(mTitleView);
        setOnClickListener(mSubTitleView);
        setOnClickListener(mHomeIcon);

        mBigSearchIcon = getResources().getDrawable(
                R.drawable.header_search_activation);
        mSmallSearchIcon = getResources().getDrawable(
                R.drawable.header_search_icon_small);

        mSearchView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mSearchViewClickedListener != null) {
                    mSearchViewClickedListener.click(v);
                }
            }
        });
        mBottomSearchShowButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mSearchViewClickedListener != null) {
                    mSearchViewClickedListener.click(v);
                }
            }
        });
        mDefaultOptionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mOptionItemClickListener != null) {
                    mOptionItemClickListener.click(v);
                }
                if(mSearchIconInTop){
                   // Log.e("f", "mSearchIconInTop");
                    postInvalidate();
                    requestLayout();
                }
            }
        });
        mTitleView.setTypeface(mTitleFace);

        initMargins();
        showHomeIcon(true);
    }
    
    private void initAnimation(){
        
    }
    
    private void readAttr(){
        
    }

    private void initMargins() {
        mSearchViewDefaultLeftMargin = getContext().getResources().getDimension(
                com.aurora.lib.R.dimen.aurora_custom_action_bar_search_view_margin_left);
        mSearchIconDefaultMarginLeft = getContext().getResources().getDimension(
                com.aurora.lib.R.dimen.aurora_custom_action_bar_search_icon_margin_left);

        mActionBarIconMarginTop = getContext().getResources().getDimension(
                com.aurora.lib.R.dimen.aurora_custom_action_bar_icon_margin_top);
        mSearchHintTextDefalutMarginLeft = getContext().getResources().getDimension(
                com.aurora.lib.R.dimen.aurora_custom_action_bar_search_hint_text_margin_left);

        mSearchIconPaddingTop = (int) (getResources()
                .getDimension(com.aurora.lib.R.dimen.aurora_custom_action_bar_search_icon_padding_top));
        mSearchIconPaddingRight = (int) (getResources()
                .getDimension(com.aurora.lib.R.dimen.aurora_custom_action_bar_search_icon_padding_right));

    }
    
    /**
     * show the default option item on right,it will show by default.
     * @param show
     */
    public void showDefualtItem(boolean show){
        if(mDefaultOptionButton != null){
            mDefaultOptionButton.setVisibility(show?View.VISIBLE:View.GONE);
        }
    }
    
    /**
     * add view to option layout
     * @param itemView
     */
    public void addItemView(View itemView){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        if(mOptionLayout != null){
            mOptionLayout.addView(itemView, 0,params);
            
        }
    }
    
    /**
     * add view to option layout
     * @param layoutRes
     */
    public void addItemView(int layoutRes){
        View view = mInflater.inflate(layoutRes, null);
        if(view != null){
            addItemView(view);
        }
    }

    private void setOnClickListener(View view) {
        view.setOnClickListener(this);
    }

    /**
     * set background for action bar
     */
    public void setBackground(Drawable bg) {
        if (mParent != null && bg != null) {
            mParent.setBackground(bg);
        }
    }

    /**
     * set background for action bar
     */
    public void setBackground(int bg) {
        mParent.setBackgroundResource(bg);
    }

    /*
     * deal with title
     */
    /**
     * set text for title,
     * 
     * @param res text res for title
     */
    public void setTitle(int res) {
       /* if (mTitleView != null) {
            if (!TextUtils.isEmpty(getContext().getText(res))) {
                mTitleView.setText(res);
                mTitleView.setVisibility(View.VISIBLE);
            } else {
                mTitleView.setVisibility(View.GONE);
            }
        }
        showOrHideTitle(mTitleView, mSubTitleView);*/
    	setTitle(getResources().getString(res));
    	
    }

    /**
     * set text for title,
     */
    public void setTitle(CharSequence title) {
        /*if (mTitleView != null) {
            if (!TextUtils.isEmpty(title)) {
                mTitleView.setText(title);
                mTitleView.setVisibility(View.VISIBLE);
            } else {
                mTitleView.setVisibility(View.GONE);
            }
        }*/
    	
        setTitleInternal(title);
        showOrHideTitle(mTitleView, mSubTitleView);

    }

    private void setTitleInternal(CharSequence title){
    	int maxTitleLength = (int)getResources().getDimension(com.aurora.lib.R.dimen.aurora_action_bar_title_maxwidth);
	    String titleSuffix = "...";
	    int titleWidth = 0;
	    int index = 0;
		if (mTitleView != null) {
	    if(!TextUtils.isEmpty(title)) {
		    	title = TextUtils.ellipsize(title, mTitleView.getPaint(), maxTitleLength,
	    				TextUtils.TruncateAt.END);
		    }
    	    
    		mTitleView.setText(title);
		}
    }
    
    /**
     * set title textappearance ,such as text size,text color
     * 
     * @param style
     */
    public void setTitleStyle(int style) {
        if (mTitleView != null) {
            mTitleView.setTextAppearance(getContext(), style);
        }
    }

    /*
     * deal with sub title
     */

    /**
     * set text for sub title
     * 
     * @param res text res id for title
     */
    public void setSubTitle(int res) {
        if (mSubTitleView != null) {
            if (!TextUtils.isEmpty(getContext().getText(res))) {
                mSubTitleView.setText(res);
                mSubTitleView.setVisibility(View.VISIBLE);
            } else {
                mSubTitleView.setVisibility(View.GONE);
            }
        }
        showOrHideTitle(mTitleView, mSubTitleView);
    }

    /**
     * set text for sub title
     */
    public void setSubTitle(CharSequence title) {
        if (mSubTitleView != null) {
            if (!TextUtils.isEmpty(title)) {
                mSubTitleView.setText(title);
                mSubTitleView.setVisibility(View.VISIBLE);
            } else {
                mSubTitleView.setVisibility(View.GONE);
            }
        }
        showOrHideTitle(mTitleView, mSubTitleView);

    }

    /**
     * actionbar默认会在右边添加一个操作组件，通过该接口去设置默认组件的图片
     * @param d
     */
    public void setDefaultOptionItemDrawable(Drawable d) {
        if (mDefaultOptionButton != null) {
            mDefaultOptionButton.setImageDrawable(d);
        }
    }
    
    /**
     * 调用该方法来控制是否显示actionbar标题的图标
     * @param show
     */
    public void showHomeIcon(boolean show){
        if(mHomeIcon != null){
            mHomeIcon.setVisibility(show?View.VISIBLE:View.GONE);
        }
        resetTitlePadding(show);
    }
    
    /**
     * reset padding of title when show or hide home icon
     * @param showHomeIcon
     */
    private void resetTitlePadding(boolean showHomeIcon){
        int paddingLeft  = 0;
        if(mTitlePanelLayout != null){
        if(showHomeIcon){
            paddingLeft = 0;
            }else{
                paddingLeft = DensityUtil.dip2px(getContext(), 10);
            }
        mTitlePanelLayout.setPadding(paddingLeft, mTitlePanelLayout.getPaddingTop(),
                mTitlePanelLayout.getPaddingRight(), mTitlePanelLayout.getPaddingBottom());
        }
    }

    /**
     * @see setDefaultOptionItemDrawable(Drawable d)
     * @param res
     */
    public void setDefaultOptionItemDrawable(int res) {
        if (mDefaultOptionButton != null) {
            mDefaultOptionButton.setImageResource(res);
        }
    }

    /**
     * set subTitle textappearance ,such as text size,text color
     * 
     * @param style
     */
    public void setSubTitleStyle(int style) {
        if (mSubTitleView != null) {
            mSubTitleView.setTextAppearance(getContext(), style);
        }
    }

    /**
     * set drawable for home icon view,not background
     * 
     * @param icon
     */
    public void setIcon(Drawable icon) {
        if (mHomeIcon != null) {
            mHomeIcon.setImageDrawable(icon);
        }
    }

    /**
     * set drawable for home icon view,not background
     * 
     * @param res
     */
    public void setIcon(int res) {
        if (mHomeIcon != null) {
            mHomeIcon.setImageResource(res);
        }
    }

    /**
     * 通过该接口来隐藏或者显示actionbar的标题
     * @param title
     * @param subTitle
     */
    private void showOrHideTitle(TextView title, TextView subTitle) {
        if (title != null && subTitle != null) {
            CharSequence t = title.getText();
            CharSequence st = subTitle.getText();
            if (TextUtils.isEmpty(t)) {
                title.setVisibility(View.GONE);
            } else {
                title.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(st)) {
                subTitle.setVisibility(View.GONE);
            } else {
                subTitle.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Deal with title click event or not
     * @param flag true to deal with event but false not
     */
    public void dealTitleClickEvent(boolean flag){
        mDealTitleEvent = flag;
    }

    public void dealFocus(){
        postInvalidate();
        requestFocus();
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    	int title = R.id.aurora_custom_action_bar_title;
    	int subtitle = R.id.aurora_custom_action_bar_subtitle;
    	int titleIcon = R.id.aurora_custom_action_bar_title_icon;
    	int id = v.getId();
    	if(id == title || id == subtitle || id == titleIcon){
    		if(mDealTitleEvent){
                if(mHomeIcon != null){
                    mHomeIcon.setPressed(true);
                }
                if (mParentActivity != null) {
                    mParentActivity.onBackPressed();
                }
            }
    	}
    }

    /**
     * 调用该接口来播放actionbar底部searchview的动画，需要调用方自己计算动画播放系数（progress）
     * @param progress 范围在0.0~1.0之间的动画播放系数
     */
    public void playSearchPanelAnimation(float progress) {
        mUpdateLayoutProgress = progress;
        mUpdateLayoutHandler.post(mUpdateLayoutThread);
        
         
    }

    /**
     * 设置底部搜索条提示文字的透明度
     * @param progress
     */
    private void auroraUpdateHintTextAlpha(float progress)
    {
		
		float hintTextAlpha = 0.5f - progress * 2;
		
		mSearchHintTextView.setAlpha(hintTextAlpha);
        
		mSearchHintTextView.setVisibility(hintTextAlpha == 0 ? View.GONE : View.VISIBLE);
        
	}
    
   /**
    * 设置底部搜索条透明度
    * @param progress
    */
    private void auroraUpdateSearchViewBgAlpha(float progress)
    {
		
		float searchViewAlpha = 1-progress*2;
		
		mBottomSearchViewParent.setAlpha(searchViewAlpha);
        
        mSearchView.setVisibility(searchViewAlpha == 0 ? View.GONE : View.VISIBLE);
       
	}
    
    /**
     * 更新底部搜索条的marginTop值，根据这个值来实现搜索条上下移动的动画
     * @param progress
     */
    private void auroraUpdateTopMargins(float progress)
    {
		int topMargin = (int) (mActionBarHeight * (1 - progress * progress));
		
		float alpha = 0.5f*(1+progress);
		
        mBottomSearchViewParentParams.topMargin = topMargin;
        
        mBottomSearchViewParent.setLayoutParams(mBottomSearchViewParentParams);
		
		mBottomSearchShowButton.setAlpha(alpha);
		
        if (topMargin == 0) {
			
            mSearchIconInTop = true;
            /*
             * 通过在drawable目录下配置一个level-list的xml文件，并调用setImageLevel(int level)
             * 接口来实现不同情况下不同图片的切换
             */
            mBottomSearchShowButton.setImageLevel(AURORA_SEARCH_ICON_MAX_LEVELS);
            
            mBottomSearchShowButton.setBackgroundResource(com.aurora.lib.R.drawable.aurora_green_action_bar_normal_item_pressed_selector);
            
            mSearchView.setVisibility(View.INVISIBLE);
            
        } else {
			
            mSearchIconInTop = false;
            
            mBottomSearchShowButton.setImageLevel((int)(AURORA_SEARCH_ICON_MAX_LEVELS*progress));
            
            mBottomSearchShowButton.setBackgroundColor(AURORA_FULL_TRANSPARENT);        
            
            mSearchView.setVisibility(View.VISIBLE);
           
        }
	}
    
    /**
     * 更新搜索条的marginLeft值，根据这个值来实现搜索条左右移动的动画
     * @param progress
     */
    private void auroraUpdateSearchViewBgLeft(float progress)
    {
		int marginLeft = (int) ((mScreenWidth / 2) * progress);
		 
		marginLeft = (int) Math.min(Math.max(mSearchViewDefaultLeftMargin, marginLeft),
                mScreenWidth / 2 + mScreenWidth / 5);
                
		mSearchViewParams.leftMargin = marginLeft;
		
        mSearchView.setLayoutParams(mSearchViewParams);
	}
    
    /**
     * 更新搜索条放大镜按钮的marginLeft值，根据这个值来实现搜索条放大镜按钮左右移动的动画
     * @param progress
     */
    private void auroraUpdateSearchIconLeft(float progress)
    {
        int iconMarginLeft = (int)(auroraGetSearchIconFinalLeftLocation() * progress);
         
        iconMarginLeft = (int) Math.max(mSearchIconDefaultMarginLeft,iconMarginLeft);
        
        mSearchIconParams.leftMargin = iconMarginLeft;
        
        mBottomSearchIconPanle.setLayoutParams(mSearchIconParams);
	}
    
    private int auroraGetSearchIconFinalLeftLocation()
    {
		return (mDefaultOptionButton != null)?(mScreenWidth - mDefaultOptionButton.getWidth()*2) : 0;
	}

    
    
   /*
    * (non-Javadoc)
    * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
    */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                 if(mHomeIcon != null){
                     mHomeIcon.setPressed(true);
                 }
                break;
            case MotionEvent.ACTION_UP:
                if(mHomeIcon != null){
                    mHomeIcon.setPressed(false);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mHomeIcon != null){
                    if(EventUtils.isOutOfBounds(getContext(), event, mTitleView)){
                        mHomeIcon.setPressed(false);
                    }else{
                        mHomeIcon.setPressed(true);
                    }
                    
                }
                
                break;

            default:
                break;
        }
        return false;
    }
}
