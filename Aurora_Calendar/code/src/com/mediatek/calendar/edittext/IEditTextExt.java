package com.mediatek.calendar.edittext;

import android.content.Context;
import android.widget.EditText;
import aurora.widget.AuroraEditText;

/**
 * M:This class is used to extension the AuroraEditText
 *
 */
public interface IEditTextExt {
    
    /**
     * the extension feature that set the inputText length input filter
     * @param inputText the AuroraEditText to set.
     * @param context
     * @param maxLength
     */
    void setLengthInputFilter(AuroraEditText inputText, final Context context,final int maxLength);
}
