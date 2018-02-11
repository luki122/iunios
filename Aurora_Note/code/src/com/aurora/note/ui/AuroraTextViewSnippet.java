package com.aurora.note.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

public class AuroraTextViewSnippet extends TextView {

    private static String sEllipsis = "\u2026";

    private String mFullText;
    private String mTargetString;
    private Pattern mPattern;

    public AuroraTextViewSnippet(Context context) {
        super(context);
    }

    public AuroraTextViewSnippet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * We have to know our width before we can compute the snippet string.
     * Do that here and then defer to super for whatever work is normally done.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!TextUtils.isEmpty(mTargetString)) {
            String fullTextLower = mFullText.toLowerCase();
            String targetStringLower = mTargetString.toLowerCase();

            int startPos = 0;
            int searchStringLength = targetStringLower.length();
            int bodyLength = fullTextLower.length();
            Matcher m = mPattern.matcher(mFullText);
            if (m.find(0)) {
                startPos = m.start();
            }

            TextPaint tp = getPaint();

            float searchStringWidth = tp.measureText(mTargetString);
            float textFieldWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) * 2;

            String snippetString = null;

            if (searchStringWidth > textFieldWidth) {
                if (mFullText.length() >=  (startPos + searchStringLength)) {
                    snippetString = mFullText.substring(startPos, startPos + searchStringLength);
                }
            } else {
                float ellipsisWidth = tp.measureText(sEllipsis);
                textFieldWidth -= (2F * ellipsisWidth); // assume we'll need one on both ends

                int offset = -1;
                int start = -1;
                int end = -1;
                /*
                 * TODO: this code could be made more efficient by only
                 * measuring the additional characters as we widen the string
                 * rather than measuring the whole new string each time.
                 */
                 while (true) {
                    offset += 1;

                    int newstart = Math.max(0, startPos - offset);
                    int newend = Math.min(bodyLength, startPos + searchStringLength + offset);

                    if (newstart == start && newend == end) {
                        // if we couldn't expand out any further then we're done
                        break;
                    }
                    start = newstart;
                    end = newend;

                    // pull the candidate string out of the full text rather than body
                    // because body has been toLower()'ed
                    String candidate = mFullText.substring(start, end);
                    if (tp.measureText(candidate) > textFieldWidth) {
                        // if the newly computed width would exceed our bounds
                        // then we're done
                        // do not use this "candidate"
                        break;
                    }

                    snippetString = String.format(
                            "%s%s%s",
                            start == 0 ? "" : sEllipsis,
                            candidate,
                            end == bodyLength ? "" : sEllipsis);
                }
            }

            if (snippetString == null) {
                return;
            }

            SpannableString spannable = new SpannableString(snippetString);
            int start = 0;

            m = mPattern.matcher(snippetString);
            while (m.find(start)) {
                spannable.setSpan(
                        new ForegroundColorSpan(
                                getContext().getResources().getColor(com.aurora.R.color.aurora_highlighted_color)),
                        m.start(), m.end(), 0);
                start = m.end();
            }

            setText(spannable);
		}

        // do this after the call to setText() above
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setText(String fullText, String target) { //java.lang.StringIndexOutOfBoundsException
        // Use a regular expression to locate the target string
        // within the full text.  The target string must be
        // found as a word start so we use \b which matches
        // word boundaries.

        if (target != null) {
            if (target.contains("/%")) {
                target = target.replaceAll("/%", "%");
            }
            if (target.contains("//")) {
                target = target.replaceAll("//", "/");
            }
        }

        String patternString = Pattern.quote(target);
        mPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);

        mFullText = fullText;
        mTargetString = target;

        setText(fullText);
        requestLayout();
    }
}