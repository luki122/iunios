package cn.com.xy.sms.sdk.ui.popu.web;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CommonWebViewClientEx extends WebViewClient {

	@Override
	public void onLoadResource(WebView view, String url) {
		try {
			if (view instanceof CommonWebView) {
			    CommonWebView webview = (CommonWebView) view;
				webview.injectJavascriptInterfaces(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onLoadResource(view, url);
	}

	@Override
	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
		try {
			if (view instanceof CommonWebView) {
			    CommonWebView webview = (CommonWebView) view;
				webview.injectJavascriptInterfaces(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.doUpdateVisitedHistory(view, url, isReload);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		try {
			if (view instanceof CommonWebView) {
			    CommonWebView webview = (CommonWebView) view;
				webview.injectJavascriptInterfaces(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		try {
			if (view instanceof CommonWebView) {
			    CommonWebView webview = (CommonWebView) view;
				webview.injectJavascriptInterfaces(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPageFinished(view, url);
	}

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.shouldOverrideUrlLoading(view, url);
    }
	
}
