package com.aurora.note.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

import aurora.widget.AuroraEditText;

import com.aurora.note.R;
import com.aurora.note.util.Globals;
import com.aurora.note.widget.NoteImageGroupSpan;
import com.aurora.note.widget.NoteImageSpan;

@SuppressWarnings("deprecation")
public class CopyNoSpaceEditText extends AuroraEditText {
	private static final String TAG = "CopyNoSpaceEditText";

	private static final int NOTE_IMAGE_SPAN_RIGHT_HOT_AREA = 30; // NoteImageSpan右边的热点区域定义
	
	private static final int DEFAULT_LINE_OFFSET = 25;
	private static int LINE_OFFSET; // 画线的偏移

	private static int LINE_RIGHT_OFFSET;

	private InputMethodManager mImm;
	private int mScaleTouchSlop;
	private final Paint mLinePaint = new Paint();
//	private final Rect mLineBounds = new Rect();
	
	private OnCursorLineChangedListener mOnCursorLineChangedListener;
	private OnNoteImageSpanLongClickListener mOnNoteImageSpanLongClickListener;
	private OnNoteImageSpanClickListener mOnNoteImageSpanClickListener;
	
	private int mLastSelectionStart = -1; // 上一次光标的位置
	private boolean mSelectionChangedByCode = false;
	
	private boolean mIsHandleEvent = false; // 是否正在处理事件

	public CopyNoSpaceEditText(Context context) {
		super(context);
		init(context);
	}

	public CopyNoSpaceEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CopyNoSpaceEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
	    mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	    mLinePaint.setColor(getResources().getColor(R.color.new_note_content_line_color));
	    mLinePaint.setAntiAlias(false); // 这里必须设置为false，不然画出来的线是2pixel宽
	    mLinePaint.setStyle(Style.STROKE);
	    mLinePaint.setStrokeWidth(0);

	    LINE_OFFSET = getResources().getDimensionPixelSize(R.dimen.new_note_content_line_offset);
	    if (LINE_OFFSET <= 0) {
	        LINE_OFFSET = DEFAULT_LINE_OFFSET;
	    }

	    LINE_RIGHT_OFFSET = getResources().getDimensionPixelSize(R.dimen.new_note_line_padding_right);

	    setDoubleClickable(false); // 屏蔽掉双击
	}
	
	protected String getCursorLine(int selStart, int selEnd) {
	    if (selStart >= 0 && selStart == selEnd) {
            final String text = getText().toString();
            int index = text.indexOf(Globals.NEW_LINE);
            if (index == -1) {
                return text;
            } else if (selStart <= index) {
                String line = text.substring(0, index);
                return line;
            } else {
                int rowStartIndex = ChatEmotion.getRowStartIndex(text, selStart);
                int rowEndIndex = ChatEmotion.getRowEndIndex(text, selStart);
                String line = null;
                if (rowStartIndex >= 0 && rowEndIndex >= 0 && rowStartIndex <= rowEndIndex) {
                    line = text.substring(rowStartIndex, rowEndIndex);
                }
                
                return line;
            }
        }
        
        return null;
	}
	
	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
	    Log.d(TAG, "Jim, onSelectionChanged selStart: " + selStart + ", selEnd: " + selEnd);
	    if (mSelectionChangedByCode) {
	        mSelectionChangedByCode = false;
	        return;
	    }
	    clearSelectedNoteImageSpan();
	    
	    String line = null;
	    int rowStartIndex = -1;
	    int rowEndIndex = -1;
	    if (selStart >= 0 && selStart == selEnd) {
            final String text = getText().toString();
            int index = text.indexOf(Globals.NEW_LINE);
            if (index == -1) {
                // 只有一行
                line = text;
                rowStartIndex = 0;
                rowEndIndex = text.length();
            } else if (selStart <= index) {
                line = text.substring(0, index);
                rowStartIndex = 0;
                rowEndIndex = index;
            } else {
                rowStartIndex = ChatEmotion.getRowStartIndex(text, selStart);
                rowEndIndex = ChatEmotion.getRowEndIndex(text, selStart);
                if (rowStartIndex >= 0 && rowEndIndex >= 0 && rowStartIndex <= rowEndIndex) {
                    line = text.substring(rowStartIndex, rowEndIndex);
                }
            }
            
            if (!TextUtils.isEmpty(line) && line.startsWith(Globals.ATTACHMENT_START) && selStart == rowStartIndex) {
                if (mLastSelectionStart == -1 || mLastSelectionStart < rowStartIndex) {
                    mLastSelectionStart = rowEndIndex;
                } else if (mLastSelectionStart > text.length()) {
                    mLastSelectionStart = text.length();
                }
                mSelectionChangedByCode = true;
                setSelection(mLastSelectionStart);
            } else {
//                notifyCursorLineChange(line);
                mLastSelectionStart = selStart;
            }
            notifyCursorLineChange(line);
        }
    }
	
	private void notifyCursorLineChange(String lineText) {
	    if (mOnCursorLineChangedListener != null) {
	        mOnCursorLineChangedListener.onCursorLineChanged(lineText);
	    }
	}
	
	public void setIsHandleEvent(boolean isHandleEvent) {
	    mIsHandleEvent = isHandleEvent;
	}
	
	public void setOnCursorLineChangedListener(OnCursorLineChangedListener listener) {
	    mOnCursorLineChangedListener = listener;
	}
	
	/**
	 * 光标行改变事件处理
	 * @author JimXia
	 * 2014-5-7 下午2:33:43
	 */
	public static interface OnCursorLineChangedListener {
	    void onCursorLineChanged(String currentLineText);
	}	

	@Override
    protected void onContextItemClicked(int itemId) {
	    switch (itemId) {
	        case android.R.id.cut:
	        case android.R.id.copy:
	            ClipboardManager clip = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
	            String copiedText = clip.getText().toString();
	            if (!TextUtils.isEmpty(copiedText)) {
	                copiedText = copiedText.replaceAll(Globals.ATTACHMENT_ALL_PATTERN, "");
	                clip.setText(copiedText);
	            }
	            break;
	        default:
	            super.onContextItemClicked(itemId);
	            break;
	    }
    }
	
	private void closeSoftInputWindow() {
	    if (mImm == null) {
	        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	    }
        if (mImm.isActive(this)) {
            mImm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

	@Override
    protected void onDetachedFromWindow() {
	    getText().clearSpans();
        super.onDetachedFromWindow();
    }
	
	private <T> T[] getSpans(int off, Class<T> type) {
	    return getText().getSpans(off, off, type);
	}
	
	private void setNoteImageSpanSelected(int off, MotionEvent event, boolean selected) {
        NoteImageSpan[] spans = getSpans(off, NoteImageSpan.class);
        if (spans != null && spans.length > 0) {
            NoteImageSpan span = spans[0];
            if (span.getType() != NoteImageSpan.Type.Type_Sign) {
                final int x = adjustX(span, (int) event.getX());
                final int y = adjustY(span, (int) event.getY());
                if (span.contains(x, y)) {
                    if (span.getType().equals(NoteImageSpan.Type.Type_Sound) ||
                            span.getType().equals(NoteImageSpan.Type.Type_Video)) {
                        span.setSelected(selected);
                        refreshNoteImageSpan(span);
                    }
                    mLastSelectedParentNoteImageSpan = span;
                    mLastSelectedNoteImageSpan = span.getSpan(x, y);
                }
            }
        }
	}
	
	public void refreshNoteImageSpan(NoteImageSpan span) {
	    final Editable editable = getText();
        editable.setSpan(span, editable.getSpanStart(span),
                editable.getSpanEnd(span), editable.getSpanFlags(span));
	}
	
	public void setNoteImageSpanSelected(NoteImageSpan parentNoteImageSpan, NoteImageSpan subSpan) {
	    if (parentNoteImageSpan != null && subSpan != null) {
	        mLastSelectedParentNoteImageSpan = parentNoteImageSpan;
	        mLastSelectedNoteImageSpan = subSpan;
	        subSpan.setSelected(true);
	        refreshNoteImageSpan(parentNoteImageSpan);
	    } else {
	        mLastSelectedParentNoteImageSpan = null;
	        mLastSelectedNoteImageSpan = null;
	    }
    }
	
	public void emptySelectedNoteImagespan() {
	    mLastSelectedParentNoteImageSpan = null;
        mLastSelectedNoteImageSpan = null;
	}
	
	public void clearSelectedNoteImageSpan() {
	    if (mLastSelectedNoteImageSpan != null && mLastSelectedParentNoteImageSpan != null) {
	        NoteImageSpan span = mLastSelectedNoteImageSpan;
	        NoteImageSpan parentSpan = mLastSelectedParentNoteImageSpan;
	        if (span.isSelected()) {
	            final Editable editable = getText();
	            span.setSelected(false);
	            editable.setSpan(parentSpan, editable.getSpanStart(parentSpan),
	                    editable.getSpanEnd(parentSpan), editable.getSpanFlags(parentSpan));
	        }
            mLastSelectedNoteImageSpan = null;
            mLastSelectedParentNoteImageSpan = null;
	    }
	}

	private NoteImageSpan mLastSelectedNoteImageSpan = null;
	private NoteImageSpan mLastSelectedParentNoteImageSpan = null;
    private float mDownY = 0;

    private boolean mHasPerformedLongPress = false;
    private CheckForLongPress mPendingCheckForLongPress;

    private boolean mShouldInterceptTouchUp = false; // 是否应该拦截触摸事件的ACTION_UP事件

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (mIsHandleEvent) {
	        return super.onTouchEvent(event);
	    }

	    int off = -1;
	    try {
	        off = getoff(event);
	    } catch (Exception e) {
	    }

        if (((int) event.getX() - getTotalPaddingLeft()) <= 0) return true;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    Log.d(TAG, "Jim, ACTION_DOWN enter");
			mDownY = event.getY();

			clearSelectedNoteImageSpan();
			setNoteImageSpanSelected(off, event, true);
			mHasPerformedLongPress = false;
			if (mLastSelectedNoteImageSpan != null) {
			    checkForLongClick(0);
			    return true; // 点的是图片，拦截触摸事件
			}
			break;

		case MotionEvent.ACTION_MOVE:
//		    Log.d(TAG, "Jim, ACTION_MOVE enter, distance: " + Math.abs(event.getY() - mDownY)
//                  + ", mScaleTouchSlop: " + mScaleTouchSlop);
		    if (mLastSelectedNoteImageSpan != null) {
		        NoteImageSpan[] spans = getSpans(off, NoteImageSpan.class);
		        if (spans == null || spans.length == 0) {
		            clearSelectedNoteImageSpan();
		            removeLongPressCallback();
	            } else {
	                NoteImageSpan span = spans[0];
	                final int x = adjustX(span, (int) event.getX());
                    final int y = adjustY(span, (int) event.getY());
                    if (!mLastSelectedNoteImageSpan.equals(span.getSpan(x, y))) {
                        clearSelectedNoteImageSpan();
                        removeLongPressCallback();
                    } else if (Math.abs(event.getY() - mDownY) > mScaleTouchSlop) {
                        clearSelectedNoteImageSpan();
    		            removeLongPressCallback();
                    }
	            }
		    }

		    if (getLayout() != null && getLayout().getHeight() < getHeight() && (Math.abs(event.getY() - mDownY) > mScaleTouchSlop)) {
		        // 文本框的内容不足以滚动时，滑动屏幕不要弹出输入法
		        mShouldInterceptTouchUp = true;
		    }
			break;

		case MotionEvent.ACTION_UP:
		    Log.d(TAG, "Jim, ACTION_UP enter");

		    if (mShouldInterceptTouchUp) {
		        // 文本框内容不足以滚动时，拦截滑动事件的UP事件，阻止输入法弹出
		        mShouldInterceptTouchUp = false;
		        event.setAction(MotionEvent.ACTION_CANCEL);
		    } else {
		        clearSelectedNoteImageSpan();
	            if (!mHasPerformedLongPress) {
	                removeLongPressCallback();

	                if (Math.abs(event.getY() - mDownY) < mScaleTouchSlop) {
	                    if (!hasSelection()) {
	                        if (handleImageSpanClick(event, off)) {
	                            return true;
	                        }
	                    }

//	                  int index = 0;
//	                  String str = getText().toString();
//	                  if (off > index) {
//	                      str = str.substring(index, off);
//	                      index = str.lastIndexOf(Globals.ATTACHMENT_START);
//	                      if(index != -1) {
//	                          index += Globals.ATTACHMENT_START_LENGTH;
//	                          String tmp = str.substring(index, index + 1);
//	                          if(tmp.equals(String.valueOf(Globals.ATTACHMENT_TYPE_SIGN))) {
//	                              // 项目符号，不拦截触摸事件
//	                              return super.onTouchEvent(event);
//	                          }
//	                      }
//	                      
//	                      index = str.lastIndexOf(Globals.ATTACHMENT_END);
//	                      if (index != -1) {
//	                          str = str.substring(index + Globals.ATTACHMENT_END_LENGTH, off);
//	                          index = str.indexOf(Globals.NEW_LINE);
//	                          if (index == -1) {
//	                              // 图片后面没有换行，拦截触摸事件，防止长按出现粘帖
//	                              return false;
//	                          }
//	                      }
//	                  }
	                }
	            } else {
	                return true;
	            }
		    }
            break;

		case MotionEvent.ACTION_CANCEL:
		    Log.d(TAG, "Jim, ACTION_CANCEL enter, distance: " + Math.abs(event.getY() - mDownY)
		            + ", mScaleTouchSlop: " + mScaleTouchSlop);
		    clearSelectedNoteImageSpan();
		    removeLongPressCallback();
            if (Math.abs(event.getY() - mDownY) >= mScaleTouchSlop) {
                closeSoftInputWindow();
            }
		    break;
		}

		return super.onTouchEvent(event);
	}
	
	private void checkForLongClick(int delayOffset) {
        mHasPerformedLongPress = false;
        
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(mPendingCheckForLongPress,
                ViewConfiguration.getLongPressTimeout() - delayOffset);
    }
	
	private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }
	
	private class CheckForLongPress implements Runnable {

        private int mOriginalWindowAttachCount;

        public void run() {
            if (mOriginalWindowAttachCount == getWindowAttachCount()) {
                if (handleLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = getWindowAttachCount();
        }
    }
	
	private boolean handleLongClick() {
	    if (mOnNoteImageSpanLongClickListener != null) {
	        int index = -1;
	        if (mLastSelectedParentNoteImageSpan instanceof NoteImageGroupSpan) {
	            NoteImageGroupSpan groupSpan = (NoteImageGroupSpan) mLastSelectedParentNoteImageSpan;
	            index = groupSpan.indexOf(mLastSelectedNoteImageSpan);
	        }
	        if (mOnNoteImageSpanLongClickListener.onNoteImageSpanLongClick(getText(),
	                mLastSelectedParentNoteImageSpan, mLastSelectedNoteImageSpan, index)) {
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	public void setOnNoteImageSpanLongClickListener(OnNoteImageSpanLongClickListener listener) {
	    mOnNoteImageSpanLongClickListener = listener;
	}
	
	public static interface OnNoteImageSpanLongClickListener {
	    boolean onNoteImageSpanLongClick(Editable editable, NoteImageSpan parentSpan, NoteImageSpan span, int index);
	}

	private int getoff(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		x -= getTotalPaddingLeft();
		y -= getTotalPaddingTop();

		x += getScrollX();
		y += getScrollY();

		Layout layout = getLayout();

		int line = layout.getLineForVertical(y);
		int off = layout.getOffsetForHorizontal(line, x);
		return off;
	}

	// 设置点击链接操作
	private boolean handleImageSpanClick(MotionEvent event, int off) {
		NoteImageSpan[] spans = getSpans(off, NoteImageSpan.class);
		if (spans != null && spans.length > 0) {
			if (event.getActionIndex() == 0) {
			    NoteImageSpan span = spans[0];
			    int x = adjustX(span, (int) event.getX());
			    int y = adjustY(span, (int) event.getY());
			    Log.d(TAG, "Jim, x: " + x);
			    NoteImageSpan.Type type = span.getType(x, y);
			    // if (x <= span.getRight() - NOTE_IMAGE_SPAN_RIGHT_HOT_AREA) {
			    if ((type != NoteImageSpan.Type.Type_Sign && x <= span.getRight() - NOTE_IMAGE_SPAN_RIGHT_HOT_AREA)
			            || (type == NoteImageSpan.Type.Type_Sign && x <= span.getRight())) {
			        // 在热点区域内点击才处理点击事件
			        String source = span.getSource(x, y);
	                // NoteImageSpan.Type type = span.getType(x, y);
	                if (source != null && type != null && (type != NoteImageSpan.Type.Type_Sign
	                        || Globals.SIGN_UNCHECKED_ID.equals(source) || Globals.SIGN_CHECKED_ID.equals(source))) {

	                	NoteImageSpan subSpan = span.getSpan(x, y);
	                	int index = -1;
	                	int count = 1;
	                	if (span instanceof NoteImageGroupSpan) {
	                		index = ((NoteImageGroupSpan) span).indexOf(subSpan);
	                		count = ((NoteImageGroupSpan) span).getSubSpanCount();
	                	}

	                    if (mOnNoteImageSpanClickListener != null) {
	                        return mOnNoteImageSpanClickListener.onNoteImageSpanClicked(getText(), span, count, index,
	                        		source, type, off);
	                    }
	                }
			    }
			}
		}
		
		return false;
	}
	
	public static interface OnNoteImageSpanClickListener {
	    boolean onNoteImageSpanClicked(Editable editable, NoteImageSpan span, int count, int index,
	    		String source, NoteImageSpan.Type type, int selection);
	}
	
	public void setOnNoteImageSpanClickListener(OnNoteImageSpanClickListener listener) {
        mOnNoteImageSpanClickListener = listener;
    }
	
	private int adjustX(NoteImageSpan span, int x) {
//	    if (x < span.getLeft()) {
//            x = span.getLeft();
//        }
//        if (x > span.getRight()) {
//            x = span.getRight();
//        }
        
        return x;
	}
	
	private int adjustY(NoteImageSpan span, int y) {
//	    if (y < span.getTop()) {
//            y = span.getTop();
//        }
//        if (y > span.getBottom()) {
//            y = span.getBottom();
//        }
        
        return y;
	}

	/**
	 * 取消默认编辑
	 */
	protected boolean getDefaultEditable() {
		return true;
	}

    @Override
    protected void onDraw(Canvas canvas) {
        drawLines(canvas);
        super.onDraw(canvas);
//        drawHotArea(canvas);
    }
    
    private void drawLines(Canvas canvas) {
        final int height = getHeight();
        final int lineCount = getLineCount();
        // final int left = getCompoundPaddingLeft();
        final int left = 0;
        // final int right = getWidth() - getCompoundPaddingRight();
        final int right = getWidth() - LINE_RIGHT_OFFSET;
        final Layout layout = getLayout();
        
        final int lineHeight = getLineHeight();
//        if (lineCount == 0) {
//            lineHeight = getLineHeight();
//        } else {
//            lineHeight = layout.getLineBottom(0) - layout.getLineTop(0);
//        }
        int bottom = lineHeight;
        canvas.save();
        canvas.translate(0/*getScaleX()*/, getCompoundPaddingTop()/* + getScaleY()*/);
        canvas.drawLine(left, -getCompoundPaddingTop() + 1, right, -getCompoundPaddingTop() + 1, mLinePaint);
//        Log.d(TAG, "Jim, lineCount: " + lineCount);
        for (int i = 0; i < lineCount; i ++) {
            bottom = layout.getLineBottom(i);
//            lineHeight = bottom - layout.getLineTop(i);
//            Log.d(TAG, "Jim, bottom: " + bottom + ", top: " + layout.getLineTop(i) + ", line: " + (i + 1));
            bottom -= LINE_OFFSET;
            canvas.drawLine(left, bottom, right, bottom, mLinePaint);
        }
        while (bottom < height) {
            bottom += lineHeight;
            canvas.drawLine(left, bottom, right, bottom, mLinePaint);
        }
        canvas.restore();
    }
    
    protected void drawHotArea(Canvas canvas) {
        final int height = getHeight();
        final int right = getWidth() - getCompoundPaddingRight();
        canvas.save();
        canvas.translate(getScaleX(), getCompoundPaddingTop() + getScaleY());
        canvas.drawLine(right - NOTE_IMAGE_SPAN_RIGHT_HOT_AREA, 0,
                right - NOTE_IMAGE_SPAN_RIGHT_HOT_AREA, height, mLinePaint);
        canvas.restore();
    }
}