package com.android.contacts.widget;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import aurora.widget.AuroraListView;
import android.widget.PopupWindow;
import android.widget.SectionIndexer;
import android.widget.TextView;
/**
 * 
 * @author HuangZhiYuan
 * @since  04/05/2012
 *
 */
public final class AlphbetIndexView extends AbsListIndexer {
	private static final int INVALID_INDEX = -1;
	private final int INVALID_OFFSET = -1;
	
	private final int MAX_WAVE_Y_RANGE;
	private final int MAX_REDRAW_Y_RANGE;

	private int MAX_TOUCHBLE_WIDTH = 120;
	private int MAX_WAVE_Y_HEIGHT = 0;
	private int ORIG_LEFT = 0;
	private int ORIG_WIDTH = 0;
	private float WAVE_FACTOR = 0;
	private float TEXT_SIZE_FACTOR_A = 0;
	private float TEXT_SIZE_FACTOR_B = 0;
	
	//Gionee:huangzy 20120903 add for CR00682623 start
	private final float MIN_SUGGETS_LETTER_HEIGHT = 7;
	private final String LETTER_DISPLAY_AREA_REPRESENTER = "M";
	//Gionee:huangzy 20120903 add for CR00682623 end
	
	private String[] mAlphbet;
	private final int ALPHEBT_LEN;

	private Paint mPaint;
	
	private int mLetterTextSizeOri;    
    private int mLetterHeightOri;
    private int mLetterTranslateOri;
    private int mLetterTextSize;
    private int mLetterHeight;
    private int mLetterTranslate;
    
    private int mEnableLetterColor;
    private int mDisableLetterColor;
    private int mShowingLetterColor;
    
    private Handler mHandler;
    
    private int mShowingLetterIndex = INVALID_INDEX;
    private int mTouchingAlphbetIndex = INVALID_INDEX;
	private int mPreTouchingLetterIndex = INVALID_INDEX;
	
	private boolean mIsTouching = false;
	private Rect mRectRedraw = new Rect();
    //aurora <wangth> <2013-9-2> add for auroro ui begin
    Typeface mFace;
    Typeface mStarFace;
    private int mStarLetterTextSize;
    private int mStarLetterTextPropSize;
    private Drawable mPromptViewStarBgRes;
    private Drawable mPromptViewBgRes;
    private int mPrompViewTop;
    private int mPrompViewLeft;
    //aurora <wangth> <2013-9-2> add for auroro ui end
   

	public AlphbetIndexView(Context context) {
		this(context, null, 0);
	}
	
	public AlphbetIndexView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public AlphbetIndexView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mFace = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
//		mStarFace = Typeface.createFromFile("system/fonts/Roboto-Thin.ttf");
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlphbetIndexView);
		Resources res = getResources();
		
		MAX_WAVE_Y_RANGE = a.getDimensionPixelSize(R.styleable.AlphbetIndexView_waveSideRange, 90);
		MAX_REDRAW_Y_RANGE = MAX_WAVE_Y_RANGE + 20;
		
		// aurora <wangth> <2013-9-3> modify for auroro ui begin
		/*
		mLetterTextSizeOri = a.getDimensionPixelSize(R.styleable.AlphbetIndexView_sectionFontSize, toRawTextSize(13));
		mLetterTextSize = mLetterTextSizeOri;
		mPromptTextSize = a.getDimensionPixelSize(R.styleable.AlphbetIndexView_promptSectionFontSize, 40);
		*/
		mLetterTextSizeOri = context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_index_font_size);
        mLetterTextSize = mLetterTextSizeOri;
        mPromptTextSize = context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_prompt_text_font_size);
        mStarLetterTextSize = context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_index_star_font_size);
        mStarLetterTextPropSize = 40;//context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_index_star_font_size);
        // aurora <wangth> <2013-9-3> modify for auroro ui begin
		
		//for CR00786443 begin
		/*mEnableLetterColor = a.getColor(R.styleable.AlphbetIndexView_enableSectionColor, Color.LTGRAY);
	    mDisableLetterColor = a.getColor(R.styleable.AlphbetIndexView_disableSectionColor, Color.DKGRAY);
	    mShowingLetterColor = a.getColor(R.styleable.AlphbetIndexView_showingLetterColor, Color.GREEN);
	    mPromptTextColor = a.getColor(R.styleable.AlphbetIndexView_promptSectionColor, Color.GREEN);*/	    
	    if (ContactsApplication.sIsGnDarkStyle) {
	    	mEnableLetterColor = res.getColor(R.color.alphbetIndexView_enable_section_dark);
	    	// aurora <wangth> <2013-9-2> modify for auroro ui begin
	    	/*
		    mDisableLetterColor = res.getColor(R.color.alphbetIndexView_disable_section_dark);
		    */
	    	mDisableLetterColor = mEnableLetterColor;
	    	// aurora <wangth> <2013-9-2> modify for auroro ui end
		    mShowingLetterColor = res.getColor(R.color.alphbetIndexView_showing_section_dark);
		    mPromptTextColor = res.getColor(R.color.alphbetIndexView_prompt_section_dark);
	    } else {
	    	mEnableLetterColor = res.getColor(R.color.aurora_contact_alphabet_letter_color);
	    	//aurora <wangth> <2013-9-2> modify for auroro ui begin
	    	/*
		    mDisableLetterColor = res.getColor(R.color.alphbetIndexView_disable_section_light);
		    */
	    	mDisableLetterColor = mEnableLetterColor;
	    	//aurora <wangth> <2013-9-2> modify for auroro ui end
		    mShowingLetterColor = res.getColor(R.color.alphbetIndexView_showing_section_light);
		    mPromptTextColor = res.getColor(R.color.alphbetIndexView_prompt_section_light);	    	
	    }
	    //for CR00786443 end
	    
	    mAnimationDuration = a.getInt(R.styleable.AlphbetIndexView_promptAnimationDuration, PROMPT_ANIM_DURATION);
	    
	    mPromptLeftOffset = a.getDimensionPixelOffset(R.styleable.AlphbetIndexView_promptLeftOffset, 0);
	    // aurora <wangth> <2013-9-2> modify for auroro ui begin
	    /*
	    int promptViewBgRes = a.getResourceId(R.styleable.AlphbetIndexView_promptBgRes, 0);
	    if (0 != promptViewBgRes) {
	    	setPromptViewBg(res.getDrawable(promptViewBgRes));
	    }
	    */
	    mPromptViewStarBgRes = a.getResources().getDrawable(R.drawable.aurora_alphbet_index_star_prompt_bg);
	    mPromptTextColor = Color.WHITE;
	    mPromptViewBgRes = a.getResources().getDrawable(R.drawable.aurora_alphbet_index_prompt_bg);
	    if (mPromptViewBgRes != null) {
	        setPromptViewBg(mPromptViewBgRes);
	    }
	    
	    mPrompViewTop = context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_index_prompt_view_top);
	    mPrompViewLeft = context.getResources().getDimensionPixelOffset(R.dimen.aurora_alphbet_index_prompt_view_left);
        // aurora <wangth> <2013-9-2> modify for auroro ui end
	    
	    a.recycle();
	    mAlphbet = context.getResources().getStringArray(R.array.gn_alphbet_index);
	    ALPHEBT_LEN = mAlphbet.length;
	    
		init(context);
	}
	
	protected void init(Context context) {
		mPaint = new Paint();
        mPaint.setColor(mEnableLetterColor);
        // aurora <wangth> <2013-9-2> remove for auroro ui begin
        //mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        // aurora <wangth> <2013-9-2> remove for auroro ui end
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        
		Rect bounds = new Rect();
		
		mPaint.setTextSize(mLetterTextSize);
		mPaint.setTypeface(mFace);
        mPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
        mLetterHeightOri = mLetterHeight = bounds.height();
        mLetterTranslateOri = mLetterTranslate = mLetterHeight/2;
        
        if (0 == mPromptBgWidth) {
        	mPaint.setTextSize(toRawTextSize(mPromptTextSize));
        	mPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
        	mPromptBgWidth = bounds.width();
        }
		
		mHandler = new Handler();
		
//		mEnterAnim = new PromptRotateAnimation(-PROMPT_HIDE_ANGLE, PROMPT_SHOW_ANGLE, mPromptBgWidth >> 1);
//        mEnterAnim.setDuration(mAnimationDuration);
//        mEnterAnim.setFillAfter(true);
//        mEnterAnim.setInterpolator(new LinearInterpolator());
//        
//        mExitAnim = new PromptRotateAnimation(PROMPT_SHOW_ANGLE, PROMPT_HIDE_ANGLE, mPromptBgWidth >> 1);
//        mExitAnim.setDuration(mAnimationDuration);
//        mExitAnim.setFillAfter(true);
//        mExitAnim.setInterpolator(new LinearInterpolator());  
		
        mAuroraEnterAnim = AnimationUtils.loadAnimation(context, R.anim.aurora_contact_prompt_enter);
        mAuroraExitAnim = AnimationUtils.loadAnimation(context, R.anim.aurora_contact_prompt_exit);
	}
	
	@Override
	public void layout(int l, int t, int r, int b) {
		super.layout(l, t, r, b);
		
		if (getHeight() > 0) {
			initLetterHolders(getHeight());
			
			MAX_WAVE_Y_HEIGHT = mLetterHolders[0].mOrigRect.left;
			WAVE_FACTOR = -(float)MAX_WAVE_Y_HEIGHT/(MAX_WAVE_Y_RANGE*MAX_WAVE_Y_RANGE);
			TEXT_SIZE_FACTOR_A = (4.0f/(MAX_WAVE_Y_HEIGHT*MAX_WAVE_Y_HEIGHT));
			TEXT_SIZE_FACTOR_B = (2.0f/(MAX_WAVE_Y_HEIGHT));
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (null == mLetterHolders)
			return;
				
		LetterHolder hodler = null;
		int paintColor;
		int left;
		int offset;
		for (int i = 0; i < mLetterHolders.length; ++i) {
			if (i == mTouchingAlphbetIndex && mIsTouching) {
				showPromptWindow();
			} /*else */{
				hodler = mLetterHolders[i];
				if (!mIsTouching && mShowingLetterIndex == i) {
					paintColor = mShowingLetterColor;
				} else {
					if (hodler.isEnable()) {
						paintColor = mEnableLetterColor;
					} else {
						paintColor = mDisableLetterColor;
					}
					
					/*if (i == mTouchingAlphbetIndex && mIsTouching) {
						paintColor = mShowingLetterColor;
					}*/
				}
				
				mPaint.setColor(paintColor);
//				//Gionee:huangzy 20120903 modify for CR00682623 start
//				if (i == 0) {
//				    mLetterTextSize = mStarLetterTextSize;
//				    mPaint.setTypeface(mStarFace);
//				} else {
				    mLetterTextSize = mLetterTextSizeOri;
				    mPaint.setTypeface(mFace);
//				}
				mPaint.setTextSize(mLetterTextSize);
				//Gionee:huangzy 20120903 modify for CR00682623 start
				left = hodler.getTextLeft();
				offset = ORIG_LEFT - left;
				if (offset > 0) {
					//mPaint.setTextSize(getTextSizeOffset(offset) + mLetterTextSize);
				}
				canvas.drawText(hodler.mLetter, left, 
						hodler.getTextTop() + mLetterTranslate, mPaint);
				
				//Gionee:huangzy 20120903 remove for CR00682623 start
				/*mPaint.setTextSize(mLetterTextSize);*/
				//Gionee:huangzy 20120903 remove for CR00682623 start
			}			
		}
	}
	
	public float getTextSizeOffset(int offsetLeft) {
		float size = (TEXT_SIZE_FACTOR_A * offsetLeft * offsetLeft + TEXT_SIZE_FACTOR_B * offsetLeft)
		+ (MAX_WAVE_Y_HEIGHT - offsetLeft - ORIG_WIDTH < 4 ? 10 : 0);
			
		return size;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		
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
			case MotionEvent.ACTION_MOVE:
				mPromptView.setAnimation(null);
				mIsTouching = true;
				break;
			default:
				mIsTouching = false;
				dismissPromptWindow();
				
				countShowingLetterIndex();
			}
		}
		
		if (mIsTouching) {
		    // aurora <wangth> <2013-9-2> remove for auroro ui begin
            /*
			LetterHolder holder;
			for (int i = 0, offset = 0; i < mLetterHolders.length; i++) {
				holder = mLetterHolders[i]; 
				offset = getWaveOffset(y, holder.mOrigRect.centerY());
				if (INVALID_OFFSET != offset) {
					holder.offsetDrawLeft(offset);
				} else {
					holder.reduceWave();
				}		
			}
			*/
			// aurora <wangth> <2013-9-2> remove for auroro ui end
		
			
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
	
	public void invalidateShowingLetterIndex() {
		if (countShowingLetterIndex()) {
			invalidate();	
		}
	}

	protected Rect getRedrawRect(int touchingY) {
		mRectRedraw.set(0, touchingY - MAX_REDRAW_Y_RANGE,
				getWidth(), touchingY + MAX_REDRAW_Y_RANGE);
		
		return mRectRedraw;
	}
	
	protected int getTouchingIndex(int touchingY) {
		/*for (int i = 0; i < mLetterHolders.length; i++) {
			if (touchingY <= mLetterHolders[i].mOrigRect.bottom)
				return i;
		}	
		
		return mLetterHolders.length - 1;*/
		
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
	
	protected int getWaveOffset(int touchingY, int waveY) {
		int distance = waveY - touchingY;
		if (Math.abs(distance) > MAX_WAVE_Y_RANGE) {
			return INVALID_OFFSET;
		}
		
		int offset = (int) (distance*distance*WAVE_FACTOR + MAX_WAVE_Y_HEIGHT);
		return offset;
	}
	
	protected void log(String msg) {
		Log.i("James", msg);
	}
    
	//======================List Section======================
    /* The related list view and adapter. */
    private AuroraListView mList;
    private SectionIndexer mSectionIndexer;
    
    /* The real sections got from list. */
    private String[] mSectionStrings = new String[] {null};
    
    /* When it is an header-footer list, represents the count of headers, 0 in normal list. */
    private int mListOffset;
    
    /**
     * Send an ACTION_CANCEL message to stop list fling.
     */
    private void cancelFling() {
    	if (null == mList)
    		return;
    	
        MotionEvent cancelFling = MotionEvent.obtain(
                0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }
    
    /**
     * Set the related list view and get indexer information from it, 
     * every time when you change the data of list, you should call it 
     * to reset the list.
     * 
     * @param listView The related list view.
     */
    //Gionee:huangzy 20120607 modify for CR00614801 start
    public void setList(AuroraListView listView, AbsListView.OnScrollListener scrollListener) {
    	if (listView != null) {
            mList = listView;
            
            /* Disable fast scroller and hide the vertical scroll bar. */
            mList.setFastScrollEnabled(false);
            mList.setVerticalScrollBarEnabled(false);
            
            mList.setOnScrollListener(scrollListener);
            
            /* Get section indexers information form list. */
            initSections(mList);
            countShowingLetterIndex();
            invalidate();
        } else {
            throw new IllegalArgumentException("Can not set a null list!");
        }
	}
    //Gionee:huangzy 20120607 modify for CR00614801 end
    
    /**
     * Get sections and section indexers, then initialize adapter with the list
     * adapter, in the end of the function, the absent array will be updated.
     */
    private void initSections(AuroraListView listView) {
        Adapter adapter = listView.getAdapter();
        mSectionIndexer = null;
        
        if (adapter instanceof HeaderViewListAdapter) {
            mListOffset = ((HeaderViewListAdapter) adapter).getHeadersCount();
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        
        if (adapter instanceof SectionIndexer) {
            mSectionIndexer = (SectionIndexer) adapter;
            Object[] sections = mSectionIndexer.getSections();
            
            if (null != sections && 0 < sections.length) {
            	mSectionStrings = new String[sections.length];
            	boolean setNull = true;
            	for (int i = 0; i < mSectionStrings.length; ++i) {
            		mSectionStrings[i] = sections[i].toString();
            		setNull = true;
            		for (int j = 0; j < mAlphbet.length; ++j) {
            			if (mAlphbet[j].equalsIgnoreCase(mSectionStrings[i])) {
            				setNull = false;
            				break;
            			}            			
            		}
            		if (setNull) {
            			mSectionStrings[i] = null;
            			// aurora <wangth> <2013-12-31> add for aurora begin
            			if (0 == i) {
            			    mSectionStrings[i] = "☆";
            			}
            			// aurora <wangth> <2013-12-31> add for aurora end
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
    
    private int toSectionIndex(int alphbetIndex) {
    	if (alphbetIndex >= 0 && alphbetIndex < mAlphbet.length) {
	    	String letter = mAlphbet[alphbetIndex];
            // aurora <wangth> <2014-1-4> add for aurora begin
	    	if (letter.equals("☆") && mSectionStrings[0] != null && mSectionStrings[0].equals("☆")) {
	    		return 0;
	    	} else if (letter.equals("☆") && mSectionStrings[0] != null && mSectionStrings[0].equals("#")) {
                return 0;
            } else if (letter.equals("#") && mSectionStrings[0] != null && mSectionStrings[0].equals("#")) {
                return 0;
            } else if (letter.equals("#") && mSectionStrings.length > 1 
                    && mSectionStrings[1] != null && mSectionStrings[1].equals("#")) {
                return 1;
            }
            // aurora <wangth> <2014-1-4> add for aurora end
            
	    	for (int i = 0; i < mSectionStrings.length; i++) {
	    		if (null == mSectionStrings[i]) {
	    			continue;
	    		}
	    		
	    		if (0 == i) {
	    			if (!mSectionStrings[0].equals("☆")&&!mSectionStrings[0].equals("#")&&(letter.equals("☆") ||letter.equals("#")||letter.equals("A"))) {
	    				
	    				return 0;
	    			}
	    		} else {
	    			if (letter.compareToIgnoreCase(mSectionStrings[i]) <= 0) {
	    				return i;
	    			}
	    		}
	        }
    	}
    	
    	return INVALID_INDEX;
    }
    
    private int toAlphbetIndex(int sectionIndex) {
    	if (sectionIndex >= 0 && sectionIndex < mSectionStrings.length) {
    		String section = mSectionStrings[sectionIndex];
    		
        	for (int i = 0; i < mAlphbet.length; i++) {
                if (mAlphbet[i].equalsIgnoreCase(section)) {
                    return i;
                }
            }
    	}
    	
        return INVALID_INDEX;
    }

    /**
     * Move the list to the start position of the current selected section.
     * 
     * @param fullSection The index in list sections.
     */
    private void moveListToSection(int sectionIndex) {
    	if (null == mList || null == mSectionIndexer || INVALID_INDEX == sectionIndex) {
    		return;
    	}

        int position = mSectionIndexer.getPositionForSection(sectionIndex);

        /* Add mListOffset for all list view, because it will be 0 if it has no header. */
        mList.setSelectionFromTop(position + mListOffset, 0);        
    }
    //*************************List Section*************************

 
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
    	adjustLetterTextSize(sectionHeight);
    	//Gionee:huangzy 20120903 add for CR00682623 end
    	
    	for (int i = 0; i < alphbetLen; ++i) {
    	    // aurora <wangth> <2013-9-16> add for aurora ui begin 
//    	    if (i == 1) {
//    	        top = top - 45;
////    	        bottom = bottom + 50;
//    	    }
    	    // aurora <wangth> <2013-9-16> add for aurora ui end
            
    		mLetterHolders[i] = new LetterHolder(ORIG_LEFT, top, 
    				right, bottom, mAlphbet[i]);
    		
    		top += sectionHeight;
    		bottom += sectionHeight;
    	}
    	
    	pickDisableSection();
    }
    
	//Gionee:huangzy 20120903 add for CR00682623 start
    private void adjustLetterTextSize(int holderHeight) {
    	final int letterMaxHeight = holderHeight - 2;
    	Rect bounds = new Rect();
    	mLetterTextSize = mLetterTextSizeOri;
    	mLetterHeight = mLetterHeightOri;
    	while (letterMaxHeight <= mLetterHeight && mLetterHeight > MIN_SUGGETS_LETTER_HEIGHT) {
    		--mLetterTextSize;
    		mPaint.setTextSize(mLetterTextSize);
            mPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
            mLetterHeight = bounds.height();
            mLetterTranslate = mLetterHeight/2;
    	}
    }
	//Gionee:huangzy 20120903 add for CR00682623 end
    
    private boolean pickDisableSection() {
    	//Gionee:huangzy 20130118 modify for CR00762066 start
    	/*if (null != mLetterHolders && null != mSectionStrings && mSectionStrings.length > 0) {
        	for (int i = 0; i < mLetterHolders.length; ++i) {
        		mLetterHolders[i].setEnable(false);
        		for (int j = 0; j < mSectionStrings.length; j++) {        				
    				if (mLetterHolders[i].mLetter.equalsIgnoreCase(mSectionStrings[j])) {
    					mLetterHolders[i].setEnable(true);
    					break;
    				}            			
        		}
        	}
        	return true;
        }*/
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
       
  //======================PromptWindow======================
	private static final int PROMPT_ANIM_DURATION = 200;
    private static final int PROMPT_HIDE_ANGLE = -90;
    private static final int PROMPT_SHOW_ANGLE = 0;
    
	/* The prompt window and the text view showing in the window. */
	private PopupWindow mPromptWindow;
	private TextView mPromptView;    
    /* The enter and exit rotate animation of the prompt window. */
//    private PromptRotateAnimation mEnterAnim;
//    private PromptRotateAnimation mExitAnim;
    private int mAnimationDuration = PROMPT_ANIM_DURATION;
    
    private Animation mAuroraEnterAnim;
    private Animation mAuroraExitAnim;
    
    private float mPromptTextSize;
    private int mPromptTextColor;
    
    private Drawable mPromptViewBg;
    private int mPromptBgWidth;
    private int mPromptBgHight;
    private int mPromptLeftOffset;
    
    /**
     * Create the prompt window and set its background to be transparent.
     */
    private TextView createPromptView() {
    	TextView tv = new TextView(getContext());
    	tv.setTextSize(mPromptTextSize);
    	
    	Rect bounds = new Rect();
    	tv.getPaint().getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
    	tv.setFocusable(false);
    	tv.setMinWidth(bounds.width() + 4);
    	tv.setGravity(Gravity.CENTER);
//    	tv.setPadding(0, 0, 35, 0);
    	tv.setTextColor(mPromptTextColor);
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
    	    // aurora <wangth> <2014-01-17> modify for aurora begin
    		//mPromptView.setText(mAlphbet[mTouchingAlphbetIndex]);
    	    if (mAlphbet[mTouchingAlphbetIndex].equals("*")) {
    	        mPromptView.setText("");
    	        if (mPromptViewStarBgRes != null) {
    	            mPromptView.setBackgroundDrawable(mPromptViewStarBgRes);
    	        }
    	    } else {
    	        mPromptView.setText(mAlphbet[mTouchingAlphbetIndex]);
    	        mPromptView.setTextSize(mPromptTextSize);
    	        if (mPromptViewBgRes != null) {
    	            mPromptView.setBackgroundDrawable(mPromptViewBgRes);
    	        }
    	    }
    	    // aurora <wangth> <2014-01-17> modify for aurora end
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
            mHandler.postDelayed(mDismissPromptRunnable, mAnimationDuration);
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

    /**
     * Calculate the pop up window offset and position it in the right place.
     */       
    private void positionPromptWindow() {
    	if (null == mPromptView) {
    		return;
    	}
    	
//    	Rect visibleRect = new Rect(); 
//    	getGlobalVisibleRect(visibleRect);
//        int x = visibleRect.left - mPromptBgWidth - mPromptLeftOffset;
//        int nActualY = mLetterHolders[mTouchingAlphbetIndex].mDrawRect.centerY() +
//        	visibleRect.top - mPromptView.getHeight()/2;
//        
//        if (nActualY < visibleRect.top) {
//        	nActualY = visibleRect.top;
//        } else if (nActualY > visibleRect.bottom) {
//        	nActualY = visibleRect.bottom;
//        }

        /* Show the prompt window or update its position if it is already visible. */
        if (!mPromptWindow.isShowing()) {
            mPromptWindow.showAtLocation(this, 
            		Gravity.NO_GRAVITY, mPrompViewLeft, mPrompViewTop);
        } else {
            mPromptWindow.update(mPrompViewLeft, mPrompViewTop, -1, -1);
        }
    }
	//*************************PromptWindow*************************
    
    
    /**
     * An animation which can rotate on Y axis, the start and end degree
     * can be specified, currently used in BladeView pop up window animation.
     */
    public class PromptRotateAnimation extends Animation {
        private static final String TAG = "PopupRotateAnimation";
        
        private float mFromDegree;
        private float mToDegree;
        private float mPivotX;
        private Camera mCamera;
        
        /**
         * Simple constructor, need to specify all parameters.
         * 
         * @param fromDegree The rotate start degree.
         * @param toDegree The rotate end degree.
         * @param pivotX The X axis pivot of the rotate.
         */
        public PromptRotateAnimation(float fromDegree, float toDegree, float pivotX) {
            mFromDegree = fromDegree;
            mToDegree = toDegree;
            mPivotX = pivotX;
            mCamera = new Camera();
        }
        
        /**
         * Set rotate interval.
         * 
         * @param fromDegree The rotate start degree.
         * @param toDegree The rotate end degree.
         */
        public void setDegreeInterval(final float fromDegree, final float toDegree) {
            mFromDegree = fromDegree;
            mToDegree = toDegree;
        }
        
        /**
         * Set X axis pivot.
         * 
         * @param pivot The X axis pivot of the rotate.
         */
        public void setPivot(final float pivot) {
            mPivotX = pivot;
        }
        
        @Override
        protected void applyTransformation(final float interpolatedTime, 
                final Transformation t) {
            final float fromDegree = mFromDegree;
            float degree = fromDegree + ((mToDegree - fromDegree) * interpolatedTime);
            
            final Camera camera = mCamera;
            final Matrix matrix = t.getMatrix();
            camera.save();
            
            /* Rotate subject on Y axis. */
            camera.rotateY(-degree);
            camera.getMatrix(matrix);
            camera.restore();

            /* Translate the pivot. */
            matrix.preTranslate(-mPivotX, 0);
            matrix.postTranslate(mPivotX, 0);
        }
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
    
    //Gionee:huangzy 20120607 add for CR00614801 start
    public boolean isPromptWindowShowing() {
        return (null != mPromptWindow) ? mPromptWindow.isShowing() : false;
    }
    //Gionee:huangzy 20120607 add for CR00614801 end
    
    public boolean isBusying() {
    	return mIsTouching || isPromptWindowShowing();
    }
}
