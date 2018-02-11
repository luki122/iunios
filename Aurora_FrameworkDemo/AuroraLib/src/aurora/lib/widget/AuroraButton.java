package aurora.lib.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;

import com.aurora.lib.R;


/**
 * Represents a push-button widget. Push-buttons can be
 * pressed, or clicked, by the user to perform an action.

 * <p>A typical use of a push-button in an activity would be the following:
 * </p>
 *
 * <pre>
 * public class MyActivity extends Activity {
 *     protected void onCreate(Bundle icicle) {
 *         super.onCreate(icicle);
 *
 *         setContentView(R.layout.content_layout_id);
 *
 *         final AuroraButton button = (AuroraButton) findViewById(R.id.button_id);
 *         button.setOnClickListener(new View.OnClickListener() {
 *             public void onClick(View v) {
 *                 // Perform action on click
 *             }
 *         });
 *     }
 * }</pre>
 *
 * <p>However, instead of applying an {@link android.view.View.OnClickListener OnClickListener} to
 * the button in your activity, you can assign a method to your button in the XML layout,
 * using the {@link android.R.attr#onClick android:onClick} attribute. For example:</p>
 *
 * <pre>
 * &lt;AuroraButton
 *     android:layout_height="wrap_content"
 *     android:layout_width="wrap_content"
 *     android:text="@string/self_destruct"
 *     android:onClick="selfDestruct" /&gt;</pre>
 *
 * <p>Now, when a user clicks the button, the Android system calls the activity's {@code
 * selfDestruct(View)} method. In order for this to work, the method must be public and accept
 * a {@link android.view.View} as its only parameter. For example:</p>
 *
 * <pre>
 * public void selfDestruct(View view) {
 *     // Kabloey
 * }</pre>
 *
 * <p>The {@link android.view.View} passed into the method is a reference to the widget
 * that was clicked.</p>
 *
 * <h3>AuroraButton style</h3>
 *
 * <p>Every AuroraButton is styled using the system's default button background, which is often different
 * from one device to another and from one version of the platform to another. If you're not
 * satisfied with the default button style and want to customize it to match the design of your
 * application, then you can replace the button's background image with a <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html#StateList">state list drawable</a>.
 * A state list drawable is a drawable resource defined in XML that changes its image based on
 * the current state of the button. Once you've defined a state list drawable in XML, you can apply
 * it to your AuroraButton with the {@link android.R.attr#background android:background}
 * attribute. For more information and an example, see <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html#StateList">State List
 * Drawable</a>.</p>
 *
 * <p>See the <a href="{@docRoot}guide/topics/ui/controls/button.html">Buttons</a>
 * guide.</p>
 *
 * <p><strong>XML attributes</strong></p>
 * <p>
 * See {@link android.R.styleable#AuroraButton AuroraButton Attributes},
 * {@link android.R.styleable#TextView TextView Attributes},
 * {@link android.R.styleable#View View Attributes}
 * </p>
 */
@RemoteView
public class AuroraButton extends TextView {
    private float mSmallFontSize;
    private int mOldBtnWidth;
    private int mOldBtnHeight;

    public AuroraButton(Context context) {
        this(context, null);
    }

    public AuroraButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public AuroraButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSmallFontSize = getResources().getDimension(R.dimen.aurora_loading_button_small_size);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AuroraButton.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AuroraButton.class.getName());
    }

    // Gionee <zhangxx> <2013-05-31> add for CR00811583 begin
    public static final int BUTTON_NORMAL_STYLE = 0;
    public static final int BUTTON_RECOM_STYLE = 1;
    public static final int BUTTON_CONTRA_STYLE = 2;
    public static final int BUTTON_LOADING_INFINITY_STYLE = 4;
    public static final int BUTTON_LOADING_STYLE = 5;
    private int mButtonStyle;
//    private Drawable mOldBackgrounddraDrawable;
    private CharSequence mOldText;
    private ColorStateList mOldTextColorStateList;
    private AnimationDrawable mAnimationDrawable;

    public void setButtonStyle(int style) {
        mButtonStyle = style;

        switch (style) {
            case BUTTON_NORMAL_STYLE:
                break;
            case BUTTON_RECOM_STYLE:
                setBackgroundResource(R.drawable.aurora_btn_recom);
                break;
            case BUTTON_CONTRA_STYLE:
                break;
            case BUTTON_LOADING_INFINITY_STYLE:
            case BUTTON_LOADING_STYLE:
                setBackgroundResource(R.drawable.aurora_btn_loading);
                break;
            default:
                break;
        }
    }

    // Gionee <zhangxx> <2013-05-31> add for CR00811583 end

    // Gionee <fenglp> <2013-08-02> modify for CR00812456 begin
    @Override
    public void setOnClickListener(OnClickListener l) {
        if (mButtonStyle == BUTTON_LOADING_STYLE || mButtonStyle == BUTTON_LOADING_INFINITY_STYLE) {
            final OnClickListener oriListener = l;
            OnClickListener wrapOnClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOldBtnWidth == 0 || mOldBtnHeight == 0) {
                        mOldBtnWidth = getWidth();
                        mOldBtnHeight = getHeight();
                    }
                    if (mOldText == null) {
                        mOldText = getText();
                    }
                    if (mOldTextColorStateList == null) {
                        mOldTextColorStateList = getTextColors();
                    }
                    if (mButtonStyle == BUTTON_LOADING_INFINITY_STYLE) {
//                    setTextColor(android.R.color.transparent);
                        setText("");
                        setBackgroundResource(R.drawable.aurora_btn_loading_bg);
                        mAnimationDrawable = (AnimationDrawable) getBackground();
                        mAnimationDrawable.start();
                    }
                    oriListener.onClick(v);
                }
            };
            super.setOnClickListener(wrapOnClickListener);
        } else {
            super.setOnClickListener(l);
        }
    }

    public void setUpdate(int val) {
        setTextColor(getResources().getColor(R.color.aurora_loading_button_text_color));
        String valStr = val + "%";
        Spannable text = new SpannableString(valStr);
        int index = valStr.indexOf("%");
        text.setSpan(new AbsoluteSizeSpan((int) mSmallFontSize), index, valStr.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setText(text);
    }

    public void reset() {
        if (mOldText != null) {
            setText(mOldText);
        }
        if (mOldTextColorStateList != null) {
            setTextColor(mOldTextColorStateList);
        }
        setBackgroundResource(R.drawable.aurora_btn_loading);
        if (mAnimationDrawable != null) {
            mAnimationDrawable.stop();
        }
    }
    // Gionee <fenglp> <2013-08-02> modify for CR00812456 end
}
