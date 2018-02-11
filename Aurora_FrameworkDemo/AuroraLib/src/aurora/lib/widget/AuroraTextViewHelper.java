package aurora.lib.widget;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.widget.TextView;

class AuroraTextViewHelper {

    // select paragraph of EditText at (x, y)
    public static void selectParagraph(TextView widget, float wx, float wy) {
        String text = widget.getText().toString();
        int offset =  getOffset(widget, wx, wy);
        int offset1 = offset;
        int offset2 = offset;
        if (offset < text.length()) {
            char c = text.charAt(offset);
            if (c == '\n') {
                offset1--;
            }
        }
        int index = text.lastIndexOf('\n', offset1);
        int start = index == -1 ? 0 : index + 1;
        index = text.indexOf('\n', offset2);
        int stop = index == -1 ? text.length() : index;
        Selection.setSelection((Spannable) widget.getText(), start, stop);
    }

    // return line number of view coordinator (x,y)
    public static int getLineNumber(TextView widget, float wy) {
        Layout layout = widget.getLayout();
        return layout.getLineForVertical(Math.round(getVertical(widget, wy)));
    }

    // return text offset of view coordinator (x,y)
    public static int getOffsetByLine(TextView widget, int line, float wx) {
        Layout layout = widget.getLayout();
        return layout.getOffsetForHorizontal(line, getHorizontal(widget, wx));
    }

    // return text offset of view coordinator (x,y)
    public static int getOffset(TextView widget, float wx, float wy) {
        int line = getLineNumber(widget, wy);
        return getOffsetByLine(widget, line, wx);
    }

    // return line text context of view coordinator (x,y)
    public static CharSequence getLineText(TextView widget, float wy) {
        int line = getLineNumber(widget, wy);
        Layout layout = widget.getLayout();
        int start = layout.getLineStart(line);
        int end = layout.getLineEnd(line);
        return layout.getText().subSequence(start, end);
    }

    private static float getHorizontal(TextView widget, float wx) {
        // Converts the absolute X,Y coordinates to the character offset for the
        // character whose position is closest to the specified
        // horizontal position.
        float x = wx - widget.getTotalPaddingLeft();
        // Clamp the position to inside of the view.
        if (x < 0) {
            x = 0;
        } else if (x >= (widget.getWidth() - widget.getTotalPaddingRight())) {
            x = widget.getWidth() - widget.getTotalPaddingRight() - 1;
        }
        //
        x += widget.getScrollX();
        return x;
    }

    private static float getVertical(TextView widget, float wy) {
        // Converts the absolute X,Y coordinates to the character offset for the
        // character whose position is closest to the specified
        // horizontal position.
        float y = wy - widget.getTotalPaddingTop();
        // Clamp the position to inside of the view.
        if (y < 0) {
            y = 0;
        } else if (y >= (widget.getHeight() - widget.getTotalPaddingBottom())) {
            y = widget.getHeight() - widget.getTotalPaddingBottom() - 1;
        }
        //
        y += widget.getScrollY();
        return y;
    }

}
