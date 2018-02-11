package com.aurora.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.mms.R;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.TextView;

// Aurora liugj 2013-09-20 created for aurora's new feature 
public class AuroraTextViewSnippet extends TextView{
    
    private static String sEllipsis = "\u2026";
    
    private String mFullText;
    private String mTargetString;
    private Pattern mPattern;
    
    public AuroraTextViewSnippet(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public AuroraTextViewSnippet(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * We have to know our width before we can compute the snippet string.  Do that
     * here and then defer to super for whatever work is normally done.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Aurora liugj 2013-09-29 modified for aurora's new feature start
        if (TextUtils.isEmpty(mTargetString)) {
            setText(mFullText);
        } else {

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
            float textFieldWidth = getWidth();

            String snippetString = null;

            if (searchStringWidth > textFieldWidth) {
                // Aurora liugj 2013-11-15 modified for bug-764 start
                if (mFullText.length() >=  (startPos + searchStringLength)) {
                    snippetString = mFullText.substring(startPos, startPos
                            + searchStringLength);
                }
                // Aurora liugj 2013-11-15 modified for bug-764 end
            } else {
                float ellipsisWidth = tp.measureText(sEllipsis);
                textFieldWidth -= (2F * ellipsisWidth); // assume we'll need one
                                                        // on both ends

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
                    int newend = Math.min(bodyLength, startPos
                            + searchStringLength + offset);

                    if (newstart == start && newend == end) {
                        // if we couldn't expand out any further then we're done
                        break;
                    }
                    start = newstart;
                    end = newend;

                    // pull the candidate string out of the full text rather
                    // than body
                    // because body has been toLower()'ed
                    String candidate = mFullText.substring(start, end);
                    if (tp.measureText(candidate) > textFieldWidth) {
                        // if the newly computed width would exceed our bounds
                        // then we're done
                        // do not use this "candidate"
                        break;
                    }

                    snippetString = String.format("%s%s%s", start == 0 ? ""
                            : sEllipsis, candidate, end == bodyLength ? ""
                            : sEllipsis);
                }
            }
          // Aurora xuyong 2014-04-09 added for bug #4001 start
            if (snippetString == null) {
                return;
            }
            // Aurora xuyong 2014-04-09 added for bug #4001 end
            SpannableString spannable = new SpannableString(snippetString);
            int start = 0;

            m = mPattern.matcher(snippetString);
            while (m.find(start)) {
                // Aurora liugj 2013-10-10 modified for aurora's new feature start
                spannable
                        .setSpan(
                                new ForegroundColorSpan(
                                        getContext()
                                                .getResources()
                                                .getColor(
                                                        com.aurora.R.color.aurora_highlighted_color)),
                                m.start(), m.end(), 0);
                // Aurora liugj 2013-10-10 modified for aurora's new feature end
                start = m.end();
            }
            setText(spannable);

        }
        // Aurora liugj 2013-09-29 modified for aurora's new feature end
        // do this after the call to setText() above
        super.onLayout(changed, left, top, right, bottom);
    }
    
    public void setText(String fullText, String target) { //java.lang.StringIndexOutOfBoundsException
        // Use a regular expression to locate the target string
        // within the full text.  The target string must be
        // found as a word start so we use \b which matches
        // word boundaries.
       // Aurora xuyong 2014-04-10 added for bug #4061 start
        if (target != null) {
            if (target.contains("/%")) {
                target = target.replaceAll("/%", "%");
            }
            if (target.contains("//")) {
                target = target.replaceAll("//", "/");
            }
        }
       // Aurora xuyong 2014-04-10 added for bug #4061 end
        String patternString = /*"\\b" + */Pattern.quote(target);
        mPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);

        mFullText = fullText;
        mTargetString = target;
        requestLayout();
    }
    
}
