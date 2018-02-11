package com.aurora.community.widget.app;


import java.text.BreakIterator;
import java.util.Locale;

/**
 * Walks through cursor positions at word boundaries. Internally uses
 * {@link BreakIterator#getWordInstance()}, and caches {@link CharSequence}
 * for performance reasons.
 *
 * Also provides methods to determine word boundaries.
 * {@hide}
 */
public class AuroraWordIterator{ // implements //Selection.PositionIterator {
    // Size of the window for the word iterator, should be greater than the longest word's length
    private static final int WINDOW_WIDTH = 50;

    private String mString;
    private int mOffsetShift;

    private BreakIterator mIterator;

    /**
     * Constructs a WordIterator using the default locale.
     */
    public AuroraWordIterator() {
        this(Locale.getDefault());
    }

    /**
     * Constructs a new WordIterator for the specified locale.
     * @param locale The locale to be used when analysing the text.
     */
    public AuroraWordIterator(Locale locale) {
        mIterator = BreakIterator.getWordInstance(locale);
    }

    public void setCharSequence(CharSequence charSequence, int start, int end) {
        mOffsetShift = Math.max(0, start - WINDOW_WIDTH);
        final int windowEnd = Math.min(charSequence.length(), end + WINDOW_WIDTH);

        //if (charSequence instanceof SpannableStringBuilder) {
         //   mString = ((SpannableStringBuilder) charSequence).substring(mOffsetShift, windowEnd);
       // } else {
            mString = charSequence.subSequence(mOffsetShift, windowEnd).toString();
       // }
        mIterator.setText(mString);
    }

    /** {@inheritDoc} */
    public int preceding(int offset) {
        int shiftedOffset = offset - mOffsetShift;
        do {
            shiftedOffset = mIterator.preceding(shiftedOffset);
            if (shiftedOffset == BreakIterator.DONE) {
                return BreakIterator.DONE;
            }
            if (isOnLetterOrDigit(shiftedOffset)) {
                return shiftedOffset + mOffsetShift;
            }
        } while (true);
    }

    /** {@inheritDoc} */
    public int following(int offset) {
        int shiftedOffset = offset - mOffsetShift;
        do {
            shiftedOffset = mIterator.following(shiftedOffset);
            if (shiftedOffset == BreakIterator.DONE) {
                return BreakIterator.DONE;
            }
            if (isAfterLetterOrDigit(shiftedOffset)) {
                return shiftedOffset + mOffsetShift;
            }
        } while (true);
    }

    /** If <code>offset</code> is within a word, returns the index of the first character of that
     * word, otherwise returns BreakIterator.DONE.
     *
     * The offsets that are considered to be part of a word are the indexes of its characters,
     * <i>as well as</i> the index of its last character plus one.
     * If offset is the index of a low surrogate character, BreakIterator.DONE will be returned.
     *
     * Valid range for offset is [0..textLength] (note the inclusive upper bound).
     * The returned value is within [0..offset] or BreakIterator.DONE.
     *
     * @throws IllegalArgumentException is offset is not valid.
     */
    public int getBeginning(int offset) {
        final int shiftedOffset = offset - mOffsetShift;
        checkOffsetIsValid(shiftedOffset);

        if (isOnLetterOrDigit(shiftedOffset)) {
            if (mIterator.isBoundary(shiftedOffset)) {
                return shiftedOffset + mOffsetShift;
            } else {
                return mIterator.preceding(shiftedOffset) + mOffsetShift;
            }
        } else {
            if (isAfterLetterOrDigit(shiftedOffset)) {
                return mIterator.preceding(shiftedOffset) + mOffsetShift;
            }
        }
        return BreakIterator.DONE;
    }

    /** If <code>offset</code> is within a word, returns the index of the last character of that
     * word plus one, otherwise returns BreakIterator.DONE.
     *
     * The offsets that are considered to be part of a word are the indexes of its characters,
     * <i>as well as</i> the index of its last character plus one.
     * If offset is the index of a low surrogate character, BreakIterator.DONE will be returned.
     *
     * Valid range for offset is [0..textLength] (note the inclusive upper bound).
     * The returned value is within [offset..textLength] or BreakIterator.DONE.
     *
     * @throws IllegalArgumentException is offset is not valid.
     */
    public int getEnd(int offset) {
        final int shiftedOffset = offset - mOffsetShift;
        checkOffsetIsValid(shiftedOffset);

        if (isAfterLetterOrDigit(shiftedOffset)) {
            if (mIterator.isBoundary(shiftedOffset)) {
                return shiftedOffset + mOffsetShift;
            } else {
                return mIterator.following(shiftedOffset) + mOffsetShift;
            }
        } else {
            if (isOnLetterOrDigit(shiftedOffset)) {
                return mIterator.following(shiftedOffset) + mOffsetShift;
            }
        }
        return BreakIterator.DONE;
    }

    private boolean isAfterLetterOrDigit(int shiftedOffset) {
        if (shiftedOffset >= 1 && shiftedOffset <= mString.length()) {
            final int codePoint = mString.codePointBefore(shiftedOffset);
            if (Character.isLetterOrDigit(codePoint)) return true;
        }
        return false;
    }

    private boolean isOnLetterOrDigit(int shiftedOffset) {
        if (shiftedOffset >= 0 && shiftedOffset < mString.length()) {
            final int codePoint = mString.codePointAt(shiftedOffset);
            if (Character.isLetterOrDigit(codePoint)) return true;
        }
        return false;
    }

    private void checkOffsetIsValid(int shiftedOffset) {
        if (shiftedOffset < 0 || shiftedOffset > mString.length()) {
            throw new IllegalArgumentException("Invalid offset: " + (shiftedOffset + mOffsetShift) +
                    ". Valid range is [" + mOffsetShift + ", " + (mString.length() + mOffsetShift) +
                    "]");
        }
    }
}
