/**
 * 
 */
package com.aurora.note.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.note.R;

/**
 * 分享界面底部工具栏一个Item，上边图片，下边文字
 * @author JimXia
 *
 * @date 2015年3月28日 上午10:05:43
 */
public class BottomToolItem extends RelativeLayout {
    private ImageView mImgIv;
    private TextView mTextTv;
    
	public BottomToolItem(Context context) {
		super(context);
	}

	public BottomToolItem(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}

	public BottomToolItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater.from(context).inflate(R.layout.bottom_tool_item, this, true);
		mImgIv = (ImageView) findViewById(R.id.img_iv);
        mTextTv = (TextView) findViewById(R.id.text_tv);
        
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BottomToolItem, defStyle, 0);

        Drawable d = a.getDrawable(R.styleable.BottomToolItem_src);
        if (d != null) {
            setImageDrawable(d);
        }
        
        CharSequence text = a.getText(R.styleable.BottomToolItem_text);
        if (text == null) {
            text = "";
        }
        setText(text);
        
        a.recycle();
	}
    
    public void setImageResource(int resId) {
        mImgIv.setImageResource(resId);
    }
    
    public void setImageDrawable(Drawable drawable) {
        mImgIv.setImageDrawable(drawable);
    }
    
    public void setImageBitmap(Bitmap bm) {
        mImgIv.setImageBitmap(bm);
    }
    
    public final void setText(CharSequence text) {
        mTextTv.setText(text);
    }
}