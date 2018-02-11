package com.aurora.store.pay;

import android.app.Activity;
import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.alipay.sdk.app.PayTask;
import com.aurora.store.interf.Pay;

/**
 * Created by joy on 2/12/15.
 */
public class AliPay implements Pay {
    Activity mContext;
    Handler mHandler;

    public AliPay(Activity activity, Handler handler) {
        mContext = activity;
        mHandler = handler;
    }

    // 如果target 大于等于API 17，则需要加上如下注解
    @Override
    @JavascriptInterface
    public String pay(String payInfo) {
        PayTask alipay = new PayTask(mContext);
        //调用支付接口，获取支付结果
        String result = alipay.pay(payInfo);
        return result;

        //for test
        //TestPay.pay(mContext);
        //return null;
    }
}
