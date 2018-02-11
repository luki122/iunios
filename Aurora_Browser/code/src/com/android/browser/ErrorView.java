package com.android.browser;



import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ErrorView extends LinearLayout {
    public ImageView mImageView;
    private TextView mTextView;
    boolean isNetwork;
    Context mContext;
    WebView webView;
	public ErrorView(Context context) {
		super(context);
		mContext=context;
		// TODO Auto-generated constructor stub
	}
	public ErrorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		// TODO Auto-generated constructor stub
	}
	public ErrorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
		// TODO Auto-generated constructor stub
	}
	 private void commonSetupIfNeeded() {
	       

	        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
	                Context.LAYOUT_INFLATER_SERVICE);
	        inflater.inflate(R.layout.error_view, this);
	        mImageView=(ImageView) findViewById(R.id.erro_imageView);

	        mTextView=(TextView) findViewById(R.id.erro_textView);
	        // Get references to each ui element.
	 }
	public void setErrorMessage(boolean b,WebView view) {
		isNetwork=b;
		webView=view;
		Log.i("xiexiujie11", "_______________"+b);
	}
	
   public void changeStatueOfErrorView(int errorCode) {
	   Log.i("browser", "errorCode-----------------------------:" + errorCode);
	   commonSetupIfNeeded();
	    if((errorCode==WebViewClient.ERROR_HOST_LOOKUP || errorCode==WebViewClient.ERROR_TIMEOUT || errorCode==0)&&!isNetwork){//没有连网
	    	mImageView.setImageResource(R.drawable.net_not_open);
	    	mTextView.setVisibility(View.VISIBLE);
	    	mTextView.setText(R.string.net_set_con);
          mTextView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent("/");
					ComponentName cm = new ComponentName("com.android.settings","com.android.settings.Settings");
				   intent.setComponent(cm);
				   intent.setAction("android.intent.action.VIEW");
				   mContext.startActivity( intent );

					
				}
			});
		   
	     }
	    else if(errorCode==WebViewClient.ERROR_HOST_LOOKUP&&isNetwork){//not find
			mImageView.setImageResource(R.drawable.net_notfind);
			
			mTextView.setVisibility(View.GONE);
			
		}
	    else if(errorCode!=WebViewClient.ERROR_HOST_LOOKUP&&isNetwork){
			mImageView.setImageResource(R.drawable.net_error);
			mTextView.setText(R.string.net_reload);
			mTextView.setVisibility(View.VISIBLE);
           mTextView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					webView.reload();

					
				}
			});
			
		}
		
	}

}
