package cn.com.xy.sms.sdk.ui.popu.popupview;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;

/**
 * 
 * @author Administrator
 * 
 */
public class BubblePopupView extends BasePopupView implements IBubbleView {

    private Integer mDuoquBubbleViewWidth = null;
    
    public BubblePopupView(Context context) {
        super(context);
    }

    public BubblePopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void initExtendParamData(boolean isRebind) {
        mDuoquBubbleViewWidth = (Integer) mBusinessSmsMessage.getExtendParamValue("duoqu_bubble_view_width");
        if(!isRebind){
            Integer bgResId = (Integer) mBusinessSmsMessage.getExtendParamValue("duoqu_bg_resid");
            if (bgResId != null) {
                mView.setBackgroundResource(bgResId);
            } else {
                Drawable bg = (Drawable) mBusinessSmsMessage.getExtendParamValue("duoqu_bg_drawable");
                if (bg != null) {
                    ViewUtil.setBackground(mView, bg);
                }
            }
        }
    }

    @Override
    public void initUIAfter() {
        initExtendParamData(false);
        // Set the left and top padding
        Integer leftPadding = (Integer) mBusinessSmsMessage
                .getExtendParamValue("duoqu_leftPadding");
        Integer topPadding = (Integer) mBusinessSmsMessage
                .getExtendParamValue("duoqu_topPadding");
        this.setPadding(leftPadding == null ? this.getPaddingLeft()
                : leftPadding, topPadding == null ? this.getPaddingTop()
                : topPadding, this.getPaddingTop(), this.getPaddingBottom());
        setLayoutParam();
    }
    public void initUIPartBefore(Activity mContext,
            BusinessSmsMessage businessSmsMessage) {
        this.mView = this;
    }

    void setLayoutParam() {
        int width = LayoutParams.WRAP_CONTENT;
        if (mDuoquBubbleViewWidth != null) {
            width = mDuoquBubbleViewWidth;
        }
        ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
        if(lp == null){
            lp = new RelativeLayout.LayoutParams(width,LayoutParams.WRAP_CONTENT);
        }else{
            lp.width=width;
        }
        mView.setId(cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager.DUOQU_BUBBLE_VIEW_ID);
        mView.setLayoutParams(lp);
//        this.addView(mView, lp);
    }

    @Override
    public void reBindData(Activity context, BusinessSmsMessage businessSmsMessage)
            throws Exception {
        if(mBusinessSmsMessage.messageBody == null){
            LogManager.w("duoqu_xiaoyuan","mBusinessSmsMessage.messageBody is null reBindData false.");
            //befroe init Bubble BusinessSmsMessage data is empty. need reinit all data
            initData(businessSmsMessage);
            bindData(context, false);
            return;
        }
        super.reBindData(context, businessSmsMessage);
        initExtendParamData(true);
//        LogManager.e("duoqu_xiaoyuan", "w1: "+ this.mView.getLayoutParams().width + " w2: "+ mDuoquBubbleViewWidth+"  hascode: "+businessSmsMessage.hashCode());
        if (mDuoquBubbleViewWidth != null
                && this.mView.getLayoutParams().width != mDuoquBubbleViewWidth) {
            ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
            lp.width = mDuoquBubbleViewWidth;
            this.mView.setLayoutParams(lp);
        }
    }

    @Override
    public void addExtendView(View view, int place) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAllExtendView() throws Exception {
        // TODO Auto-generated method stub
        
    }
    
//  private View mTop;
//  private View mLeft;
//  private View mRight;
//  private View mBottom;
//
//  public void addExtendView(View view, int place) throws Exception {
//
//      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//              LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//      int verp = -1;
//      int verp1 = -1;
//      int verp2 = -1;
//      if (view.getId() == -1) {
//          view.setId(place);
//      }
//      RelativeLayout.LayoutParams viewLp = (RelativeLayout.LayoutParams) this.mView
//              .getLayoutParams();
//      switch (place) {
//      case 1:
//          mTop = view;
//          verp = RelativeLayout.ALIGN_PARENT_TOP;
//          verp2 = RelativeLayout.BELOW;
//          lp.width = LayoutParams.MATCH_PARENT;
//          break;
//      case 2:
//          mBottom = view;
//          lp.width = LayoutParams.MATCH_PARENT;
//          verp = RelativeLayout.ALIGN_PARENT_BOTTOM;
//          verp1 = RelativeLayout.BELOW;
//          if (mLeft != null) {
//              lp.addRule(verp1, mLeft.getId());
//          }
//          if (mRight != null) {
//              lp.addRule(verp1, mRight.getId());
//          }
//          break;
//      case 3:
//          lp.height = LayoutParams.MATCH_PARENT;
//          mLeft = view;
//          verp = RelativeLayout.ALIGN_PARENT_LEFT
//                  | RelativeLayout.CENTER_VERTICAL;
//          verp2 = RelativeLayout.RIGHT_OF;
//          if (mTop != null) {
//              lp.addRule(RelativeLayout.BELOW, mTop.getId());
//          }
//          break;
//      case 4:
//          lp.height = LayoutParams.MATCH_PARENT;
//          mRight = view;
//          lp.width = LayoutParams.MATCH_PARENT;
//          verp = RelativeLayout.CENTER_VERTICAL;
//          verp1 = RelativeLayout.RIGHT_OF;
//          if (mTop != null) {
//              lp.addRule(RelativeLayout.BELOW, mTop.getId());
//          }
//          break;
//      default:
//          throw new Exception("please check place value in [1-4]");
//      }
//      if (verp != -1) {
//          lp.addRule(verp);
//      }
//      if (verp1 != -1) {
//          lp.addRule(verp1, this.mView.getId());
//      }
//
//      this.addView(view, lp);
//      if (verp2 != -1) {
//          viewLp.addRule(verp2, view.getId());
//      }
//
//  }
//
//  @Override
//  public void removeAllExtendView() throws Exception {
//      this.removeAllViews();
//      mTop = null;
//      mLeft = null;
//      mRight = null;
//      mBottom = null;
//      setLayoutParam();
//  }

}
