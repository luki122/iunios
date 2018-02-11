package com.aurora.voiceassistant.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.aurora.voiceassistant.R;

public class WebviewActivity extends Activity {
	private WebView mWebViewview = null;
	private String mUrl = null;
	private ProgressDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vs_webview_layout);
		mUrl = getIntent().getStringExtra("url");
		mWebViewview = (WebView) findViewById(R.id.wview_webview);
		mWebViewview.getSettings().setJavaScriptEnabled(true);
		mWebViewview.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		dialog = new ProgressDialog(this);
		if (dialog != null) {
			// dialog = ProgressDialog.show(WebviewActivity.this, "", "加载中……");
			dialog.setMessage("加载中……");
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();
		}
		mWebViewview.loadUrl(mUrl);
		mWebViewview.setWebViewClient(new CallWebViewClient());
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("xiejun", mUrl);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	private class CallWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d("pgm", "url==" + url);
			if(url.startsWith("http://")){
			view.loadUrl(url);
			}
		else{
				try{
				  	Uri uri = Uri.parse(url); 
		            Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
		            startActivity(intent);
		            } catch(Exception e){
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			Log.i("test", "onPageStarted");
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			Log.i("test", "onPageFinished");
			if (dialog != null)
				dialog.dismiss();
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.i("test","onReceivedError");
			Log.i("test","errorCode=="+errorCode+"     description=="+description+"     failingUrl=="+failingUrl);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		dismissWebLoadingDialog();
	}

	public void dismissWebLoadingDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}
}
