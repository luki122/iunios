package cn.com.xy.sms.sdk.ui.popu.web;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;

public class MenuWindow extends PopupWindow {

    private View mMenuView;
//    private TextView mTextView_Reset;
    private TextView mTextView_Reload;// 刷新
    private TextView mTextView_Feedback;// 反馈
    private TextView mTextView_Help;// 帮助
    private TextView mTextView_AboutUs;// 服务说明
//    private View mSplitline;
//    private String mContent;

//    public View getmSplitline() {
//        return mSplitline;
//    }
//
//    public void setmSplitline(View mSplitline) {
//        this.mSplitline = mSplitline;
//    }

//    public View getmTextView_Reset() {
//        return mTextView_Reset;
//    }
//
//    public void setmTextView_Reset(View mTextView_Reset) {
//        this.mTextView_Reset = (TextView) mTextView_Reset;
//    }

    public MenuWindow(final Activity context, final WebView webView, OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.duoqu_popup_menu, null);
        mTextView_Reload = (TextView) mMenuView
        		.findViewById(R.id.duoqu_menu_reload);
        mTextView_Feedback = (TextView) mMenuView
        		.findViewById(R.id.duoqu_menu_feedback);
        mTextView_Help = (TextView) mMenuView
        		.findViewById(R.id.duoqu_menu_help);
        mTextView_AboutUs = (TextView) mMenuView
                .findViewById(R.id.duoqu_menu_abourtus);
//        mTextView_Reset = (TextView) mMenuView
//                .findViewById(R.id.duoqu_menu_reset);
//        mSplitline = mMenuView.findViewById(R.id.duoqu_menu_splitline);
//        mContent = context.getResources().getString(R.string.duoqu_popmenu_content);
//        mTextView_Reset.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                final CustomDialog dialog2 = new CustomDialog(context,
//                        R.layout.duoqu_popup_alertdialog, R.style.Theme_dialog);
//                TextView mTextViewContent = (TextView) dialog2
//                        .findViewById(R.id.duoqu_alertdialog_content);
//                mTextViewContent.setText(mContent);
//                Button mButtonOk = (Button) dialog2
//                        .findViewById(R.id.duoqu_alertdialog_ok);
//                mButtonOk.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // TODO Auto-generated method stub
//                        long result = ParseManager.setDefServiceSwitch(context,
//                                "0");
////                        LogManager.i("DefServiceSwitch", "result: " + result);
//                        mTextView_Reset.setVisibility(View.GONE);
//                        mSplitline.setVisibility(View.GONE);
//                        dialog2.cancel();
//
//                        MenuWindow.this.dismiss();
//                    }
//                });
//                Button mButton_Cancel = (Button) dialog2
//                        .findViewById(R.id.duoqu_alertdialog_cancel);
//                mButton_Cancel.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // TODO Auto-generated method stub
//                        dialog2.cancel();
//                    }
//                });
//                dialog2.show();
//            }
//        });
        
        mTextView_Reload.setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
                try {
                    RelativeLayout webViewLy = (RelativeLayout) context
                            .findViewById(R.id.duoqu_webview);
                    RelativeLayout errViewLy = (RelativeLayout) context
                            .findViewById(R.id.duoqu_error_page);
                    TextView titleName = (TextView) context
                            .findViewById(R.id.duoqu_title_name);
                    if (webViewLy != null && errViewLy != null) {
                        WebView webView = (WebView) webViewLy.getChildAt(0);
                        int isNetWork = XyUtil.checkNetWork(context,2);
                        if (isNetWork == -1 || isNetWork == 1) {
                            errViewLy.setVisibility(View.VISIBLE);
                            titleName.setText(R.string.duoqu_web_not_find_page);
                            webView.setVisibility(View.GONE);
                        } else {
//                          webView.reload();
//                          errViewLy.setVisibility(View.GONE);
//                          webView.setVisibility(View.VISIBLE);
                            if(!StringUtils.isNull(SdkWebActivity.endUrl)){
                                webView.loadUrl(SdkWebActivity.endUrl);
                                SdkWebActivity. endUrl = "";
                            }else{
                                webView.reload();
                            }
                            errViewLy.setVisibility(View.GONE);
                            webView.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MenuWindow.this.dismiss();
            }
        });
        
        mTextView_Feedback.setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		try {
        			JSONObject jsobj = new JSONObject();
        			jsobj.put("type", "WEB_FEEDBACK");
        			PopupUtil.startWebActivity(context, jsobj, "WEB_FEEDBACK", "");
        			MenuWindow.this.dismiss();
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}
        	}
        });
        
        mTextView_Help.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					JSONObject jsobj = new JSONObject();
					jsobj.put("type", "WEB_HELP");
					PopupUtil.startWebActivity(context, jsobj, "WEB_HELP", "");
					MenuWindow.this.dismiss();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

        mTextView_AboutUs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    // long result =
                    // ParseManager.setDefServiceSwitch(context,"1");
                    // LogManager.i("DefServiceSwitch", "result: "+ result);

					// mTextView_Reset.setVisibility(View.VISIBLE);
					// mSplitline.setVisibility(View.VISIBLE);
                    JSONObject jsobj = new JSONObject();
                    jsobj.put("type", "WEB_ABOUT");
                    PopupUtil.startWebActivity(context,jsobj, "WEB_ABOUT", "");
                    MenuWindow.this.dismiss();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
        // int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        this.setContentView(mMenuView);
        this.setWidth(w / 3);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(android.R.style.Animation_Dialog);
        ColorDrawable dw = new ColorDrawable(0000000000);
        this.setBackgroundDrawable(dw);
        mMenuView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

}