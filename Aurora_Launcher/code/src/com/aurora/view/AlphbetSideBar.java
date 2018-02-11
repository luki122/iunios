package com.aurora.view;


import com.aurora.launcher.Launcher;
import com.aurora.launcher.R;
import com.aurora.launcher.Utilities;
import com.aurora.stickylistheaders.SectionIndexerAdapterWrapper;
import com.aurora.stickylistheaders.StickyListHeadersListView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;


public class AlphbetSideBar extends View {
	private static final int INVALID_INDEX = -1;
	private static final int PROMPT_ANIM_DURATION = 200;
	public final static String[] mAlphbet = { "#", "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
				"V", "W", "X", "Y", "Z" };
	private final static int ALPHEBT_LEN = mAlphbet.length;
	private int MAX_TOUCHBLE_WIDTH = 120;
	private final int MAX_REDRAW_Y_RANGE = 100;

	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;	
	private int choose = -1;
	private Paint mPaint = new Paint();
	private float scale = 1.0f;
	private final int DEF_TEXT_SIZE = 39;
	
	Typeface mFace;
	private int mLetterTextSize;
	private float mPromptTextSize;
    private int mPromptTextColor;
	
	private final float MIN_SUGGETS_LETTER_HEIGHT = 7;
	private final String LETTER_DISPLAY_AREA_REPRESENTER = "M";
	private int mLetterHeight;
	private int mLetterTranslate;
	private int ORIG_LEFT = 0;
	private int ORIG_WIDTH = 0;
	
	private Handler mHandler;
    private Animation mAuroraEnterAnim;
    private Animation mAuroraExitAnim;
    
    //======================List Section======================
    private StickyListHeadersListView mList;
    private SectionIndexer mSectionIndexer;
    private String[] mSectionStrings = new String[] {null};
    private int mListOffset;
	
    // index
    private int mShowingLetterIndex = INVALID_INDEX;
    private int mTouchingAlphbetIndex = INVALID_INDEX;
   	private int mPreTouchingLetterIndex = INVALID_INDEX;
   	
   	private boolean mIsTouching = false;

   	private PopupWindow mPromptWindow;
	private TextView mPromptView;
	private Drawable mPromptViewBg;
	private int mPromptBgWidth;
	private int mPromptBgHight;
	
    private int mEnableLetterColor;
    private int mDisableLetterColor;
    private int mShowingLetterColor;

    private Rect mRectRedraw = new Rect();
	
    private Launcher mLauncher;
    
	public AlphbetSideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public AlphbetSideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public AlphbetSideBar(Context context) {
		super(context);
		initView(context);
	}
	
	private void initView(Context context){
		DisplayMetrics dm = new DisplayMetrics();   
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);//this指当前activity  
		
		if(dm != null){
			scale = dm.densityDpi/160.0f;
		}
		mFace = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
		mLetterTextSize = context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_index_font_size);
		mPromptTextSize = context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_prompt_text_font_size);
		
		Drawable promptViewBgRes = context.getResources().getDrawable(R.drawable.show_head_toast_bg_left);
	    if (promptViewBgRes != null) {
	        setPromptViewBg(promptViewBgRes);
	    }
	    		
		mEnableLetterColor = mDisableLetterColor = Color.WHITE;
		mShowingLetterColor = Color.WHITE;
		
		mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        
		Rect bounds = new Rect();
		
		mPaint.setTextSize(mLetterTextSize);
		mPaint.setTypeface(mFace);
        mPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
        mLetterHeight = bounds.height();
        mLetterTranslate = mLetterHeight/2;
        
        if (0 == mPromptBgWidth) {
        	mPaint.setTextSize(mPromptTextSize);
        	mPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
        	mPromptBgWidth = bounds.width();
        }
        
        mHandler = new Handler();
        mAuroraEnterAnim = AnimationUtils.loadAnimation(context, R.anim.quick_index_prompt_enter);
        mAuroraExitAnim = AnimationUtils.loadAnimation(context, R.anim.quick_index_prompt_exit);
        mLauncher = (Launcher)context;
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		//setBackground(null);
		//getBackground().setAlpha(0);
		hideBackground();
	}
	
    protected void setPromptViewBg(Drawable bg) {
    	if (null == bg) {
    		return;
    	}
    	mPromptViewBg = bg;
    	mPromptBgWidth = bg.getIntrinsicWidth();
    	mPromptBgHight = bg.getIntrinsicHeight();
	}
    
    @Override
    protected void onDetachedFromWindow() {
    	dismissPromptWindowImediately();
		mPromptWindow = null;
    	super.onDetachedFromWindow();
    }
    
	/**
     * Start the window exit animation, and then dismiss the pop up window
     * after the animation has been played.
     */
    private void dismissPromptWindowImediately() {
    	mHandler.removeCallbacks(mDismissPromptRunnable);
    	if (null != mPromptWindow) {
    		mPromptWindow.dismiss();
    	}    	
    }
    
    private void dismissPromptWindow() {
    	if (null != mPromptWindow) {
    	    mPromptView.startAnimation(mAuroraExitAnim);
            mHandler.postDelayed(mDismissPromptRunnable, PROMPT_ANIM_DURATION);
    	}
    }
    
    private Runnable mDismissPromptRunnable = new Runnable() {
		@Override
		public void run() {			
			if (null != mPromptWindow) {
        		mPromptWindow.dismiss();
        	}
		}
	};
    
	@Override
	public void layout(int l, int t, int r, int b) {
		super.layout(l, t, r, b);
		
		if (getHeight() > 0) {
			initLetterHolders(getHeight());
		}
	}
	
    private void initSections(StickyListHeadersListView listView) {
    	SectionIndexerAdapterWrapper adapter = listView.getSectionIndexerAdapter();
        mSectionIndexer = null;
        // Log.w("HJJ", "try to test initing quickIndex ==> initSections, adapter == " + adapter );
        if (adapter != null) {
        	//Log.e("HJJ", "SectionIndexerAdapterWrapper....");
            mSectionIndexer = (SectionIndexer) adapter;
            Object[] sections = mSectionIndexer.getSections();
            // Log.w("HJJ", "try to test initing quickIndex ==> sections.length:" + sections.length);
            if (null != sections && 0 < sections.length) {
            	mSectionStrings = new String[sections.length];
            	boolean setNull = true;
            	for (int i = 0; i < mSectionStrings.length; ++i) {
            		mSectionStrings[i] = sections[i].toString();
            		//Log.w("HJJ", "try to test initing quickIndex ==>i:" + i + ",value:" + mSectionStrings[i]);
            		setNull = true;
            		for (int j = 0; j < mAlphbet.length; ++j) {
            			if (mAlphbet[j].equalsIgnoreCase(mSectionStrings[i])) {
            				setNull = false;
            				break;
            			}            			
            		}
            		if (setNull) {
            			mSectionStrings[i] = null;
            		}
            	}
            }
        	//Gionee:huangzy 20130118 add for CR00762066 start
            else {
            	mSectionStrings = new String[] {null};;
            }
        	//Gionee:huangzy 20130118 add for CR00762066 end
        	
        }
        
        pickDisableSection();
    }
	
    private int toAlphbetIndex(int sectionIndex) {
    	if (sectionIndex >= 0 && sectionIndex < mSectionStrings.length) {
    		String section = mSectionStrings[sectionIndex];
    		
        	for (int i = 0; i < mAlphbet.length; i++) {
                if (mAlphbet[i].equalsIgnoreCase(section)) {
                	//Log.e("HJJ", "toAlphbetIndex, section:" + section + ", i:" + i);
                    return i;
                }
            }
    	}
    	
        return INVALID_INDEX;
    }
    
	private boolean countShowingLetterIndex() {
		if (null == mSectionIndexer || null == mList) {
			return false;
		}
		
		int position = mList.getFirstVisiblePosition() - mListOffset;
		position = position < 0 ? 0 : position;
		int sectionIndex = mSectionIndexer.getSectionForPosition(position);
		int shallShowing = toAlphbetIndex(sectionIndex);
		
		if (mShowingLetterIndex == shallShowing) {
			return false;
		}
		
		mShowingLetterIndex = shallShowing;
		return true;
	}
	
	public void setList(StickyListHeadersListView listView){
		if (listView != null) {
            mList = listView;
            // Log.w("HJJ", "try to test initing quickIndex ==> set listview:" + mList);
            updateSections();
        } else {
            throw new IllegalArgumentException("Can not set a null list!");
        }
	}
	
	public void updateSections(){
		initSections(mList);
        countShowingLetterIndex();
        invalidate();
	}
	
    class LetterHolder {
    	Rect mDrawRect;
    	Rect mOrigRect;
    	String mLetter;
    	boolean mIsEnable = false;
    	
    	public LetterHolder(int left, int top, int right, int bottom, String letter) {
    		this(new Rect(left, top, right, bottom), letter);
    	}
    	
    	public LetterHolder(Rect origRect, String letter) {
    		mDrawRect = new Rect(origRect);
    		mOrigRect = new Rect(origRect);
    		
    		mLetter = letter;
    	}
    	
    	public int getTextTop() {
    		return mDrawRect.top/2 + mDrawRect.bottom/2;
    	}
    	
    	public int getTextLeft() {
    		return mDrawRect.left + mDrawRect.width()/2;
    	}
    	
    	public void reduceWave() {
    		mDrawRect.left = mOrigRect.left;
    		mDrawRect.right = mOrigRect.right;
    	}
    	
    	public void offsetDrawLeft(int leftOffset) {
    		mDrawRect.left = mOrigRect.left - leftOffset;
    		mDrawRect.right = mOrigRect.right - leftOffset;
    	}
    	
    	public void setEnable(boolean isEnable) {
    		mIsEnable = isEnable;
    	}
    	
    	public boolean isEnable() {
    		return mIsEnable;
    	}
   
    }
    
    LetterHolder[] mLetterHolders;
    
    private void initLetterHolders(int viewHeight) {    	
    	final int alphbetLen = mAlphbet.length;
    	if (alphbetLen <= 0)
    		return;
    	
    	int sectionTopOffset = getPaddingTop();
    	int realHeight = viewHeight - sectionTopOffset - getPaddingBottom();
    	int sectionHeight = realHeight/alphbetLen;
    	sectionTopOffset += ((realHeight%alphbetLen)/2);
    	ORIG_LEFT = getWidth() - sectionHeight - getPaddingRight();
    	ORIG_WIDTH = sectionHeight;
    	mLetterHolders = new LetterHolder[alphbetLen];
    	
    	int right = ORIG_LEFT + sectionHeight;
    	int top = sectionTopOffset;
    	int bottom = top + sectionHeight;
    	
    	//Gionee:huangzy 20120903 add for CR00682623 start
    	//adjustLetterTextSize(sectionHeight);
    	//Gionee:huangzy 20120903 add for CR00682623 end
    	
    	for (int i = 0; i < alphbetLen; ++i) {
    		mLetterHolders[i] = new LetterHolder(ORIG_LEFT, top, 
    				right, bottom, mAlphbet[i]);
    		
    		top += sectionHeight;
    		bottom += sectionHeight;
    	}
    	pickDisableSection();
    }
    
    private boolean pickDisableSection() {
    	if (null != mLetterHolders) {
    		for (int i = 0; i < mLetterHolders.length; ++i) {
        		mLetterHolders[i].setEnable(false);
        		
        		if (null != mSectionStrings && mSectionStrings.length > 0) {
        			for (int j = 0; j < mSectionStrings.length; j++) {        				
        				if (mLetterHolders[i].mLetter.equalsIgnoreCase(mSectionStrings[j])) {
        					mLetterHolders[i].setEnable(true);
        					break;
        				}            			
            		}    				
        		} else {
        			mLetterHolders[i].setEnable(false);
        		}
        	}
    		
        	return true;
        }
    	//Gionee:huangzy 20130118 modify for CR00762066 end
    	
    	return false;
    }
    
	//Gionee:huangzy 20120903 add for CR00682623 start
    private void adjustLetterTextSize(int holderHeight) {
    	final int letterMaxHeight = holderHeight - 2;
    	Rect bounds = new Rect();
    	//Log.e("linp", "letterMaxHeight="+letterMaxHeight+";"+"mLetterHeight="+mLetterHeight+";");
    	while (letterMaxHeight <= mLetterHeight && mLetterHeight > MIN_SUGGETS_LETTER_HEIGHT) {
    		Log.e("linp", "scale Letter size!");
    		--mLetterTextSize;
    		mPaint.setTextSize(mLetterTextSize);
            mPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
            mLetterHeight = bounds.height();
            mLetterTranslate = mLetterHeight/2;
    	}
    }
	//Gionee:huangzy 20120903 add for CR00682623 end
	
	/**
	 * 重写这个方法
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null == mLetterHolders)
			return;
				
		LetterHolder hodler = null;
		int paintColor;
		int left;
		for (int i = 0; i < mLetterHolders.length; ++i) {
			if (i == mTouchingAlphbetIndex && mIsTouching) {
				showPromptWindow();
			} 
			
			hodler = mLetterHolders[i];
			// 选中顔色的
			/*if (!mIsTouching && mShowingLetterIndex == i) {
				paintColor = mShowingLetterColor;
			} else {
				if (hodler.isEnable()) {
					paintColor = mEnableLetterColor;
				} else {
					paintColor = mDisableLetterColor;
				}
			}*/
			paintColor = mEnableLetterColor;

			mPaint.setColor(paintColor);
			mPaint.setTextSize(mLetterTextSize);
			left = hodler.getTextLeft();
			canvas.drawText(hodler.mLetter, left, hodler.getTextTop()
					+ mLetterTranslate, mPaint);
		}	
	}
    
    private TextView createPromptView() {
    	TextView tv = new TextView(getContext());
    	tv.setTextSize(mPromptTextSize);
    	Rect bounds = new Rect();
    	
    	tv.getPaint().getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
    	tv.setFocusable(false);
    	tv.setMinWidth(bounds.width() + 4);
    	tv.setGravity(Gravity.CENTER);
    	tv.setTextColor(Color.BLACK);
    	tv.setBackground(mPromptViewBg);
    	return tv;
    }
    
    private void createPromptWindow() {
        if (mPromptWindow == null) {
            Context c = getContext();
            PopupWindow p = new PopupWindow(c);
                        
            p.setWidth(mPromptBgWidth);
            p.setHeight(mPromptBgHight);
         
            /* Set background of the prompt window to transparent. */
            ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
            p.setBackgroundDrawable(dw);
          
            mPromptWindow = p;
            
            mPromptView = createPromptView();
            mPromptWindow.setContentView(mPromptView);
        }
    }
    
    private void setPromptText() {
    	if (null != mPromptView) {
    		mPromptView.setText(mAlphbet[mTouchingAlphbetIndex]);
    		mPromptView.setTextSize(mPromptTextSize);
    		mPromptView.setPadding(0, 0, 0, 0);
    	}
    }        
    
    /**
     * Create the prompt window and then show it at appropriate position.
     */
    private void showPromptWindow() {
        /* Make sure we have a window before showing the pop up window. */
        if (getWindowVisibility() == View.VISIBLE) {
            createPromptWindow();
            setPromptText();
            positionPromptWindow();
        }
    }
    
    private void positionPromptWindow() {
    	if (null == mPromptView) {
    		return;
    	}
    	
    	Rect visibleRect = new Rect(); 
    	getGlobalVisibleRect(visibleRect);
    	int offset = Utilities.getIntegerValueFromResourcesDimens(mLauncher.getResources(),R.dimen.quick_index_alpha_preview_start_offset);
       int x = visibleRect.left - offset ;
        int nActualY = mLetterHolders[mTouchingAlphbetIndex].mDrawRect.centerY() +
        	visibleRect.top - mPromptView.getHeight()/2;
        
        if (nActualY < visibleRect.top) {
        	nActualY = visibleRect.top;
        } else if (nActualY > visibleRect.bottom) {
        	nActualY = visibleRect.bottom;
        }

        /* Show the prompt window or update its position if it is already visible. */
        if (!mPromptWindow.isShowing()) {
            mPromptWindow.showAtLocation(this, 
            		Gravity.NO_GRAVITY, x, nActualY);
        } else {
            mPromptWindow.update(x, nActualY, -1, -1);
        }
    }

    private Drawable mBackground;
    private void showBackground(){
    	if(mBackground == null) {
    		mBackground = getBackground();
    	}
    	if(mBackground != null) {
    		mBackground.setAlpha(255);
    	}
    }
    
    private void hideBackground(){
    	if(mBackground == null) {
    		mBackground = getBackground();
    	}
    	if(mBackground != null) {
    		mBackground.setAlpha(0);
    	}
    }
    
    
    // 日志显示刚开机时会调用constructAppsIndex和clearAppsIndex，但两个是在不同的线程中运行，结果clearAppsIndex执行
 	// 因此没有section，最终导致滑动右侧字母表时没有效果
    private boolean isForceUpdateSection(){
    	//Log.w("HJJ", "try to test initing quickIndex ==> 1mSectionIndexer:" + mSectionIndexer + ",mLetterHolders:" + mLetterHolders);
    	if(mList == null) return false;
    	if(mSectionIndexer == null || mLetterHolders == null 
    			|| (mSectionStrings != null && mSectionStrings.length == 0)){
    		return true;
    	}
    	return false;
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		//Log.e("HJJ", "onTouchEvent:(" + x + "," + y + ")" + ", RawXY(" + event.getRawX() + "," + event.getRawY() + ")");
		if(isForceUpdateSection()) {
			Log.w("HJJ", "try to test initing quickIndex ==> onTouchEvent, isForceUpdateSection is true");
			updateSections();
		}
		mTouchingAlphbetIndex = getTouchingIndex(y);
		
		if (!mIsTouching && getWidth() - x > MAX_TOUCHBLE_WIDTH) {
			if (mIsTouching) {
				mIsTouching = false;
			}
		} else {
			createPromptWindow();
			
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				cancelFling();
				mPromptView.setAnimation(mAuroraEnterAnim);	
				showBackground();
			case MotionEvent.ACTION_MOVE:
				mPromptView.setAnimation(null);
				mIsTouching = true;
				showBackground();
				break;
			default:
				mIsTouching = false;
				dismissPromptWindow();
				countShowingLetterIndex();
				hideBackground();
			}
		}
		
		if (mIsTouching) {
			if (mPreTouchingLetterIndex != mTouchingAlphbetIndex ||
					mShowingLetterIndex  != mTouchingAlphbetIndex) {
				mPreTouchingLetterIndex = mTouchingAlphbetIndex;
				moveListToSection(toSectionIndex(mTouchingAlphbetIndex));
			}
		} else {
			for (int i = 0; i < mLetterHolders.length; i++) {
				mLetterHolders[i].reduceWave();				
			}
		}
		
		mRectRedraw = getRedrawRect(y);
		invalidate(mRectRedraw);
		
		return mIsTouching;
	}

	protected Rect getRedrawRect(int touchingY) {
		mRectRedraw.set(0, touchingY - MAX_REDRAW_Y_RANGE,
				getWidth(), touchingY + MAX_REDRAW_Y_RANGE);
		
		return mRectRedraw;
	}
	
	private void moveListToSection(int sectionIndex) {
    	if (null == mList || null == mSectionIndexer || INVALID_INDEX == sectionIndex) {
    		return;
    	}

        int position = mSectionIndexer.getPositionForSection(sectionIndex);
        /* Add mListOffset for all list view, because it will be 0 if it has no header. */
        mList.setSelectionFromTop(position + mListOffset, 0);        
    }
	
    private int toSectionIndex(int alphbetIndex) {
    	if (alphbetIndex >= 0 && alphbetIndex < mAlphbet.length) {
	    	String letter = mAlphbet[alphbetIndex];
	    	for (int i = 0; i < mSectionStrings.length; i++) {
	    		if (null == mSectionStrings[i]) {
	    			continue;
	    		}
	    		
	    		if (letter.compareToIgnoreCase(mSectionStrings[i]) <= 0) {
	    			//Log.e("HJJ", "letter:" + letter + ",i:" + i);
	                return i;
	            }
	        }
    	}
    	
    	return INVALID_INDEX;
    }
    
    private void cancelFling() {
    	if (null == mList)
    		return;
    	
        MotionEvent cancelFling = MotionEvent.obtain(
                0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }
    
	protected int getTouchingIndex(int touchingY) {
		if (touchingY < mLetterHolders[0].mOrigRect.top) {
			return 0;
		} else if (touchingY > mLetterHolders[ALPHEBT_LEN - 1].mOrigRect.bottom) {
			return ALPHEBT_LEN - 1;
		}
		
		int start = 0;
		int end = ALPHEBT_LEN - 1;
		int mid = (start + end)>>1;
		
		Rect rect = new Rect(mLetterHolders[mid].mOrigRect);
		
		while (!rect.contains(rect.left, touchingY) && end > start) {
			if (touchingY < rect.top) {
				end = mid - 1;
			} else {
				start = mid + 1;
			}
			
			mid = (start + end)/2;
			rect = mLetterHolders[mid].mOrigRect;
		}
		
		return mid;
	}
	
	public void invalidateShowingLetterIndex() {
		if (countShowingLetterIndex()) {
			invalidate();	
		}
	}
	
	/**
	 * 向外公开的方法
	 * 
	 * @param onTouchingLetterChangedListener
	 */
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	/**
	 * 接口
	 * @author coder
	 */
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s,float positionOfY);
	}

}
