package com.aurora.market.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class GroupWordTextView extends TextView {
	
	private float mHorizontalSpace = 30;
	
	private String horizontalSpaceStr;
	private float horizontalSpaceWidth;
	
	private float width;

	public GroupWordTextView(Context context) {
		super(context);
	}
	
	public GroupWordTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public GroupWordTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setGroupText(String[] groupWord) {
		StringBuilder sb = new StringBuilder();
		float lastWidth = 0;
		for (int i = 0; i < groupWord.length; i++) {
			String word = groupWord[i];
			if (i == 0) {	// 第一个
				if (longerThanLineWidth(word)) {
					sb.append(word);
					lastWidth = getWidthToLastLine(word);
				} else {
					sb.append(word);
					lastWidth = getWordWidth(word);
				}
			} else {
				if (lastWidth + getHorizontalSpaceWidth() + getWordWidth(word) < getLineWidth()) {
					sb.append(getHorizontalSpaceStr());
					sb.append(word);
					lastWidth += (getHorizontalSpaceWidth() + getWordWidth(word));
				} else {
					sb.append("\n");
					if (longerThanLineWidth(word)) {
						sb.append(word);
						lastWidth = getWidthToLastLine(word);
					} else {
						sb.append(word);
						lastWidth = getWordWidth(word);
					}
				}
			}
		}
		setText(sb.toString());
	}
	
	private String getHorizontalSpaceStr() {
		if (!TextUtils.isEmpty(horizontalSpaceStr)) {
			return horizontalSpaceStr;
		}
		StringBuilder sb = new StringBuilder();
		while (true) {
			sb.append(" ");
			if (getPaint().measureText(sb.toString()) >= mHorizontalSpace) {
				break;
			}
		}
		return sb.toString();
	}
	
	private float getHorizontalSpaceWidth() {
		if (horizontalSpaceWidth == 0) {
			horizontalSpaceWidth = getPaint().measureText(getHorizontalSpaceStr());
		}
		return horizontalSpaceWidth;
	}
	
	private float getLineWidth() {
		return width - getPaddingLeft() - getPaddingBottom();
	}
	
	private float getWordWidth(String word) {
		return getPaint().measureText(word);
	}
	
	/**
	* @Title: longerThanLineWidth
	* @Description: 是否单个词大于一行
	* @param @param word
	* @param @return
	* @return boolean
	* @throws
	 */
	private boolean longerThanLineWidth(String word) {
		if (getPaint().measureText(word) > getLineWidth()) {
			return true;
		}
		return false;
	}
	
	private float getWidthToLastLine(String word) {
		int start = 0;
		for (int i = 0; i < word.length(); i++) {
			if (getPaint().measureText(word.substring(start, i)) > getLineWidth()) {
				start = i;
			}
		}
		return getPaint().measureText(word.substring(start, word.length()));
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public void setmHorizontalSpace(float mHorizontalSpace) {
		this.mHorizontalSpace = mHorizontalSpace;
	}

}
