/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.android.contacts;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.util.ArrayList;

/**
 * Highlights the text in a text field.
 */
public class AuroraPrefixHighlighter {

    private final int mHighlightColor;

    private ForegroundColorSpan mColorSpan;

    public AuroraPrefixHighlighter(int highlightColor) {
        mHighlightColor = highlightColor;
    }

    /**
     * Returns a CharSequence which highlights the given prefix if found in the given text.
     *
     * @param text the text to which to apply the highlight
     * @param prefix the prefix to look for
     */
    public CharSequence applyName(CharSequence text, char[] textForHighlight) {
        if (null == textForHighlight) {
            return text;
        }
        return applyNameText(text, textForHighlight);
    }

    /**
     * Returns a CharSequence which highlights the given prefix if found in the given text.
     *
     * @param text the text to which to apply the highlight
     * @param prefix the prefix to look for
     */
    public CharSequence applyNumber(CharSequence text, char[] textForHighlight) {
        if (null == textForHighlight) {
            return text;
        }
        if (String.valueOf(text).contains("@")) {
            return applyInternationalCallText(text, textForHighlight);
        } else {
            return applyNumberText(text, textForHighlight);
        }
    }

    private CharSequence applyNumberText(CharSequence text, char[] textForHighlight) {
        ArrayList<Integer> ignore = new ArrayList<Integer>();
        if (null == textForHighlight) {
            return text;
        }
        char[] handledText = lettersAndDigitsOnly(textForHighlight);
        int index = indexOfWordForLetterOrDigit(text, handledText, ignore);

        if (index != -1) {
            SpannableString stringBuilder = new SpannableString(text);
            for (int i = 0; i <= ignore.size(); ++ i) {
                int start = (0 == i) ? index : (ignore.get(i - 1) + 1);
                int end = (i == ignore.size()) ? (index + handledText.length + ignore.size()) : ignore.get(i);
                if (start <= end) {
                    stringBuilder.setSpan(new ForegroundColorSpan(mHighlightColor), start, end, 0);
                }
            }
            return stringBuilder;
        } else {
            return text;
        }
    }

    private CharSequence applyNameText(CharSequence text, char[] textForHighlight) {
        int index = indexOfWordForLetterOrDigit(text, textForHighlight);
        if (index != -1) {
            if (mColorSpan == null) {
                mColorSpan = new ForegroundColorSpan(mHighlightColor);
            }

            SpannableString result = new SpannableString(text);
            result.setSpan(mColorSpan, index, index + textForHighlight.length, 0 /* flags */);
            return result;
        } else {
            return text;
        }
    }

    private CharSequence applyInternationalCallText(CharSequence text, char[] textForHighlight) {
        int index = indexOfWordForInternationalCall(text, textForHighlight);
        if (index != -1) {
            if (mColorSpan == null) {
                mColorSpan = new ForegroundColorSpan(mHighlightColor);
            }

            SpannableString result = new SpannableString(text);
            result.setSpan(mColorSpan, index, index + textForHighlight.length, 0 /* flags */);
            return result;
        } else {
            return text;
        }
    }

    private static char[] lettersAndDigitsOnly(char[] lettersOriginal) {
        char[] letters = lettersOriginal.clone();
        int length = 0;
        for (int i = 0; i < letters.length; i++) {
            final char c = letters[i];
            if (Character.isLetterOrDigit(c)) {
                letters[length++] = c;
            }
        }

        if (length != letters.length) {
            return new String(letters, 0, length).toCharArray();
        }

        return letters;
    }
    
  
    private static int indexOfWordForLetterOrDigit(CharSequence text, char[] prefix) {
        if (prefix == null || text == null) {
            return -1;
        }

        int textLength = text.length();
        int prefixLength = prefix.length;

        if (prefixLength == 0 || textLength < prefixLength) {
            return -1;
        }

        int i = 0;
        while (i < textLength) {
            while (i < textLength && !Character.isLetterOrDigit(text.charAt(i))) {
                i++;
            }

            if (i + prefixLength > textLength) {
                return -1;
            }

            int j;
            for (j = 0; j < prefixLength; j++) {
                if (Character.toUpperCase(text.charAt(i + j)) != prefix[j]) {
                    break;
                }
            }
            if (j == prefixLength) {
                return i;
            }

            i ++;
        }

        return -1;
    }

    private static int indexOfWordForInternationalCall(CharSequence text, char[] prefix) {
        if (prefix == null || text == null) {
            return -1;
        }

        int textLength = text.length();
        int prefixLength = prefix.length;

        if (prefixLength == 0 || textLength < prefixLength) {
            return -1;
        }

        int i = 0;
        while (i < textLength) {
            // Skip non-word characters
            while (i < textLength && !Character.isLetterOrDigit(text.charAt(i))
                    && text.charAt(i) != '@' && text.charAt(i) != '.') {
                i++;
            }

            if (i + prefixLength > textLength) {
                return -1;
            }

            // Compare the prefixes
            int j;
            for (j = 0; j < prefixLength; j++) {
                if (Character.toUpperCase(text.charAt(i + j)) != prefix[j]) {
                    break;
                }
            }
            if (j == prefixLength) {
                return i;
            }

            i ++;
        }

        return -1;
    }

    private static int indexOfWordForLetterOrDigit(CharSequence text, char[] prefix, ArrayList<Integer> ignore) {
        if (prefix == null || text == null) {
            return -1;
        }

        int textLength = text.length();
        int prefixLength = prefix.length;

        if (prefixLength == 0 || textLength < prefixLength) {
            return -1;
        }

        int i = 0;
        while (i < textLength) {
            while (i < textLength && !Character.isLetterOrDigit(text.charAt(i))) {
                i++;
            }

            if (i + prefixLength > textLength) {
                return -1;
            }

            int j;
            ignore.clear();
            for (j = 0; j < prefixLength; j++) {
                while (i + ignore.size() + j < textLength &&
                        !Character.isLetterOrDigit(text.charAt(i + ignore.size() + j))) {
                    ignore.add(Integer.valueOf(i + ignore.size() + j));
                }
                if (i + ignore.size() + j >= textLength) {
                    return -1;
                }
                if (Character.toUpperCase(text.charAt(i + ignore.size() + j)) != prefix[j]) {
                    break;
                }
            }
            if (j == prefixLength) {
                return i;
            }

            i ++;
        }

        return -1;
    }
    
}
