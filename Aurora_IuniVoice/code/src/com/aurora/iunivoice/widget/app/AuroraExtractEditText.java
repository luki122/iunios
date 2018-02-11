package com.aurora.iunivoice.widget.app;


import android.annotation.SuppressLint;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.AttributeSet;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputMethodManager;

import com.aurora.iunivoice.widget.AuroraEditText;


/***
 * Specialization of {@link android.widget.EditText} for showing and interacting with the
 * extracted text in a full-screen input method.
 */
public class AuroraExtractEditText extends AuroraEditText {
    private InputMethodService mIME;
    private int mSettingExtractedText;

    public AuroraExtractEditText(Context context) {
        super(context, null);
    }

    public AuroraExtractEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }

    public AuroraExtractEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setIME(InputMethodService ime) {
        mIME = ime;
    }

    /**
     * Start making changes that will not be reported to the client.  That
     * is, {@link #onSelectionChanged(int, int)} will not result in sending
     * the new selection to the client
     */
    public void startInternalChanges() {
        mSettingExtractedText += 1;
    }

    /**
     * Finish making changes that will not be reported to the client.  That
     * is, {@link #onSelectionChanged(int, int)} will not result in sending
     * the new selection to the client
     */
    public void finishInternalChanges() {
        mSettingExtractedText -= 1;
    }

    /**
     * Implement just to keep track of when we are setting text from the
     * client (vs. seeing changes in ourself from the user).
     */
    @Override
    public void setExtractedText(ExtractedText text) {
        try {
            mSettingExtractedText++;
            super.setExtractedText(text);
        } finally {
            mSettingExtractedText--;
        }
    }

    /**
     * Report to the underlying text editor about selection changes.
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if ((mSettingExtractedText == 0) && (mIME != null) && (selStart >= 0) &&
                (selEnd >= 0)) {
            mIME.onExtractedSelectionChanged(selStart, selEnd);
        }
    }

    /**
     * Redirect clicks to the IME for handling there.  First allows any
     * on click handler to run, though.
     */
    @Override
    public boolean performClick() {
        if (!super.performClick() && (mIME != null)) {
            mIME.onExtractedTextClicked();

            return true;
        }

        return false;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if ((mIME != null) && mIME.onExtractTextContextMenuItem(id)) {
            return true;
        }

        return super.onTextContextMenuItem(id);
    }

    /**
     * We are always considered to be an input method target.
     */
    @Override
    public boolean isInputMethodTarget() {
        return true;
    }

    /**
     * Return true if the edit text is currently showing a scroll bar.
     */
    public boolean hasVerticalScrollBar() {
        return computeVerticalScrollRange() > computeVerticalScrollExtent();
    }

    /**
     * Pretend like the window this view is in always has focus, so its
     * highlight and cursor will be displayed.
     */
    @Override
    public boolean hasWindowFocus() {
        return this.isEnabled();
    }

    /**
     * Pretend like this view always has focus, so its
     * highlight and cursor will be displayed.
     */
    @Override
    public boolean isFocused() {
        return this.isEnabled();
    }

    /**
     * Pretend like this view always has focus, so its
     * highlight and cursor will be displayed.
     */
    @Override
    public boolean hasFocus() {
        return this.isEnabled();
    }

    /**
     * @hide
     */
    //@Override
    //TODO Alan.Xu
    @SuppressLint("NewApi")
	protected void viewClicked(InputMethodManager imm) {
        // As an instance of this class is supposed to be owned by IMS,
        // and it has a reference to the IMS (the current IME),
        // we just need to call back its onViewClicked() here.
        // It should be good to avoid unnecessary IPCs by doing this as well.
        if (mIME != null) {
            mIME.onViewClicked(false);
        }
    }

    /**
     * {@inheritDoc}
     * @hide
     */
    //@Override
    //TODO Alan.Xu
    protected void deleteText_internal(int start, int end) {
        // Do not call the super method.
        // This will change the source TextView instead, which will update the ExtractTextView.
      //  mIME.onExtractedDeleteText(start, end);
    }

    /**
     * {@inheritDoc}
     * @hide
     */
    //@Override
    //TODO Alan.Xu
    protected void replaceText_internal(int start, int end, CharSequence text) {
        // Do not call the super method.
        // This will change the source TextView instead, which will update the ExtractTextView.
      //  mIME.onExtractedReplaceText(start, end, text);
    }

    /**
     * {@inheritDoc}
     * @hide
     */
    //@Override
    //TODO Alan.Xu
    protected void setSpan_internal(Object span, int start, int end, int flags) {
        // Do not call the super method.
        // This will change the source TextView instead, which will update the ExtractTextView.
    //    mIME.onExtractedSetSpan(span, start, end, flags);
    }

    /**
     * {@inheritDoc}
     * @hide
     */
    //@Override
    //TODO Alan.Xu
    protected void setCursorPosition_internal(int start, int end) {
        // Do not call the super method.
        // This will change the source TextView instead, which will update the ExtractTextView.
        mIME.onExtractedSelectionChanged(start, end);
    }
}
