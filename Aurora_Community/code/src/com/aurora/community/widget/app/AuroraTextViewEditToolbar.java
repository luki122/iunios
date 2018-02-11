package com.aurora.community.widget.app;


import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.widget.AuroraEditText;
//gionee 20121210 guoyx modified for CR00734816 begin
// import android.widget.Editor.CursorController;
//gionee 20121210 guoyx modified for CR00734816 end

@SuppressLint("NewApi")
public class AuroraTextViewEditToolbar extends AuroraTextViewToolbar {
    static final String LOG_TAG = "GN_FW_GNTextViewEditToolbar";

    private static final int ID_SELECT_ALL = android.R.id.selectAll;
    private static final int ID_START_SELECTING_TEXT = android.R.id.startSelectingText;
    private static final int ID_CUT = android.R.id.cut;
    private static final int ID_COPY = android.R.id.copy;
    private static final int ID_SWITCH_INPUT_METHOD = android.R.id.switchInputMethod;

    private static final int ID_SELECT_ALL_STR = R.string.aurora_selectAll;
    private static final int ID_START_SELECTING_TEXT_STR = R.string.aurora_select;
    private static final int ID_CUT_STR = R.string.aurora_cut;
    private static final int ID_COPY_STR = R.string.aurora_copy;
    private static final int ID_SWITCH_INPUT_METHOD_STR = R.string.aurora_inputMethod;

    private TextView mItemSelectAll;
    private TextView mItemStartSelect;
    private TextView mItemCut;
    private TextView mItemCopy;
    private TextView mItemInputMethod;
  //gionee 20121210 guoyx modified for CR00734816 begin
    // private Editor mEditor = mEditText.mEditor;
  //gionee 20121210 guoyx modified for CR00734816 end

    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (isShowing()) {
                /*if (mEditText instanceof GnExtractEditText) {
                    mEditText.onTextContextMenuItem(v.getId());
                }*/
                onItemAction(v.getId());
                switch (v.getId()) {
                case ID_SELECT_ALL:
                case ID_START_SELECTING_TEXT:
                    hide();
                    show();
                    break;
                default:
                    hide();
                    break;
                }
            }
        }
    };

    public AuroraTextViewEditToolbar(AuroraEditText hostView) {
        super(hostView);
        initToolbarItem();
    }

    protected void initToolbarItem() {
        super.initToolbarItem();
		//gionee 20121210 guoyx modified for CR00734816 begin
        /*if(mEditor == null) {
		    Log.d("LOG_TAG", "mEditor == null and create new one.");
            mEditText.createEditorIfNeeded();
            mEditor = mEditText.mEditor;
        }*/
		//gionee 20121210 guoyx modified for CR00734816 end
        mItemSelectAll = initToolbarItem(ID_SELECT_ALL, ID_SELECT_ALL_STR);
        mItemStartSelect = initToolbarItem(ID_START_SELECTING_TEXT, ID_START_SELECTING_TEXT_STR);
        mItemCopy = initToolbarItem(ID_COPY, ID_COPY_STR);
        mItemCut = initToolbarItem(ID_CUT, ID_CUT_STR);
        mItemInputMethod = initToolbarItem(ID_SWITCH_INPUT_METHOD, ID_SWITCH_INPUT_METHOD_STR);
    }

    protected OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    protected void updateToolbarItemsEx() {/*
        mToolbarGroup.removeAllViews();
        // construct toolbar.
        if (mEditText.isInTextSelectionMode()) {
            Log.d("LOG_TAG", "updateToolbarItems()----mEditText.isInTextSelectionMode()");
            if (mEditText.canCut()) {
                Log.d("LOG_TAG", "updateToolbarItems()----mEditText.canCut()");
                mToolbarGroup.addView(mItemCut);
            }
            if (mEditText.canCopy()) {
                Log.d("LOG_TAG", "updateToolbarItems()----mEditText.canCopy()");
                mToolbarGroup.addView(mItemCopy);
            }
            if (mEditText.canPaste()) {
                Log.d("LOG_TAG", "updateToolbarItems()----mEditText.canPaste()");
                mToolbarGroup.addView(mItemPaste);
            }
        } else {
            if (mEditText.canSelectText()) {
                Log.d("LOG_TAG", "updateToolbarItems()----mEditText.canSelectText()");
                if (!mEditText.hasPasswordTransformationMethod()) {
                    mToolbarGroup.addView(mItemStartSelect);
                }
                mToolbarGroup.addView(mItemSelectAll);
            }
            if (mEditText.canPaste()) {
                Log.d("LOG_TAG", "updateToolbarItems()----mEditText.canPaste()");
                mToolbarGroup.addView(mItemPaste);
            }
            if (mEditText.isInputMethodTarget()) {
                Log.d("LOG_TAG", "updateToolbarItems()----mEditText.isInputMethodTarget()");
                mToolbarGroup.addView(mItemInputMethod);
            }
        }
    */}
    
    protected void updateToolbarItems() {
        mToolbarGroup.removeAllViews();

        boolean passwordTransformed = mEditText.getTransformationMethod() instanceof PasswordTransformationMethod;
        CharSequence text = mEditText.getText();

        boolean hasClip = ((ClipboardManager) mEditText.getContext()
                           .getSystemService(Context.CLIPBOARD_SERVICE)).hasPrimaryClip();

        if (mEditText.hasSelection()) {
            if (!passwordTransformed && (text.length() > 0) &&
                    text instanceof Editable &&
                    (mEditText.getKeyListener() != null)) {
                mToolbarGroup.addView(mItemCut);
            }

            if (!passwordTransformed && (text.length() > 0)) {
                mToolbarGroup.addView(mItemCopy);
            }

            if (text instanceof Editable &&
                    (mEditText.getKeyListener() != null) &&
                    (mEditText.getSelectionStart() >= 0) &&
                    (mEditText.getSelectionEnd() >= 0) && hasClip) {
                mToolbarGroup.addView(mItemPaste);
            }
        } else {
            if (text.length() > 0) {
                if (mEditText.isSelectionToolEnabled()) {
                    if (!passwordTransformed) {
                        mToolbarGroup.addView(mItemStartSelect);
                    }

                    mToolbarGroup.addView(mItemSelectAll);
                }
            }

            if (text instanceof Editable &&
                    (mEditText.getKeyListener() != null) &&
                    (mEditText.getSelectionStart() >= 0) &&
                    (mEditText.getSelectionEnd() >= 0) && hasClip) {
                mToolbarGroup.addView(mItemPaste);
            }

            if (mEditText.isImSwitcherEnabled() &&
                    mEditText.isInputMethodTarget()) {
                    //TODO Alan.Xu
                    //!(mEditText instanceof OppoExtractEditText)) {
                mToolbarGroup.addView(mItemInputMethod);
            }
        }
    }

    private boolean onItemAction(int id) {
        CharSequence text = mEditText.getText();
        CharSequence transformed = text;//mEditText.getTransformed();

        int min = 0;
        int max = text.length();
        if (mEditText.isFocused()) {
            final int selStart = mEditText.getSelectionStart();
            final int selEnd = mEditText.getSelectionEnd();
            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
        }

        ClipboardManager clip = (ClipboardManager) mEditText.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
      //gionee 20121210 guoyx modified for CR00734816 begin
        // CursorController selectionController = mEditor.getSelectionController();
      //gionee 20121210 guoyx modified for CR00734816 end
        switch (id) {
        case ID_SELECT_ALL:
            Selection.setSelection((Spannable) text, 0, text.length());
            mEditText.mStart=0;
            mEditText.mEnd=text.length();
            mEditText.mIsSelectedAll = true;
            mEditText.onContextItemClicked(ID_SELECT_ALL);
            mEditText.startTextSelectionMode();
            return true;
        case ID_START_SELECTING_TEXT:
            mEditText.startTextSelectionMode();
            return true;
        case ID_CUT:
            int end = mEditText.getSelectionStart();
            clip.setText(transformed.subSequence(min, max));
            // if (!(mEditText instanceof GnExtractEditText)) {
                ((Editable) text).delete(min, max);
            // }
            mEditText.stopTextSelectionMode();
            // if (mEditText instanceof GnExtractEditText) {
                Selection.setSelection((Spannable) mEditText.getText(), end);
            // }
                mEditText.onContextItemClicked(ID_CUT);
            return true;
        case ID_COPY:
            clip.setText(transformed.subSequence(min, max));
            mEditText.stopTextSelectionMode();
            mEditText.onContextItemClicked(ID_COPY);
            return true;
        case ID_PASTE:
            CharSequence paste = clip.getText();
            if (paste != null && paste.length() > 0) {
                Selection.setSelection((Spannable) text, max);
                // if (!(mEditText instanceof GnExtractEditText)) {
                    ((Editable) text).replace(min, max, paste);
                //}
                mEditText.stopTextSelectionMode();
                mEditText.onContextItemClicked(ID_PASTE);
            }
            
            return true;
        case ID_SWITCH_INPUT_METHOD:
            if (!(mEditText instanceof AuroraExtractEditText)) {
                InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                }
            }
            return true;
        }
        return false;
    }

}

