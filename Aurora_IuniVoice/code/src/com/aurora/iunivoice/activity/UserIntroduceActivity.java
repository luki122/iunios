package com.aurora.iunivoice.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.DensityUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.ToastUtil;

public class UserIntroduceActivity extends BaseActivity {

	private EditText et_introduce;
	private static final int ACTIONBAR_ID_DONE = 0x015745;
	private TextView left_word_num;
	
	private int FINAL_MARGIN ;
	
	private final int MAX_WORD = 30,SHOW_TIP_NUM = 9;
	
	private IuniVoiceManager iuniVoiceManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_introduce);
		FINAL_MARGIN = DensityUtil.dip2px(this, 5);
		iuniVoiceManager = new IuniVoiceManager(this);
		setupViews();
	}
	
	@Override
	public void setupViews() {
		setTitleRes(R.string.mine_introduce);
		// TODO Auto-generated method stub
		et_introduce = (EditText) findViewById(R.id.et_introduce);
		et_introduce.setText(getIntent().getStringExtra(AccountManageActivity.INTRODEUC_KEY));
		addActionBarItem(getResources().getString(R.string.finish), ACTIONBAR_ID_DONE);
		left_word_num = (TextView) findViewById(R.id.left_word_num);
		et_introduce.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				// TODO Auto-generated method stub
				int delBottom = bottom - oldBottom;
				if(delBottom > 0)
				{
					changeLeftWordNumLayout(0);
				}else{
					changeLeftWordNumLayout(Math.abs(delBottom));
				}
			}
		});
		et_introduce.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				int leftNum = MAX_WORD - arg0.length();
				if(leftNum <= SHOW_TIP_NUM)
				{
					left_word_num.setVisibility(View.VISIBLE);
					left_word_num.setText(leftNum+"");
				}else{
					left_word_num.setVisibility(View.INVISIBLE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void changeLeftWordNumLayout(int marginBottom){
		LinearLayout.LayoutParams rlp = (android.widget.LinearLayout.LayoutParams) left_word_num.getLayoutParams();
		if(rlp.bottomMargin <= FINAL_MARGIN)
		{
			rlp.bottomMargin = marginBottom + FINAL_MARGIN;
			left_word_num.setLayoutParams(rlp);
		}
		
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		// TODO Auto-generated method stub
		super.onActionBarItemClick(view, itemId);
		if(itemId == ACTIONBAR_ID_DONE)
		{
			changeIntroduce();			
		}
	}

	private String filterContent(String source){
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
	        Matcher m = p.matcher(source);
	       return m.replaceAll("");
	}
	
	private void changeIntroduce(){
		
		if(et_introduce.getText().length() == 0)
		{
			ToastUtil.longToast(R.string.content_not_empty);
			return;
		}
		
		iuniVoiceManager.chanageUserIntroduce(new DataResponse<BaseResponseObject>(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				ToastUtil.longToast(value.getMsg());
				if(value.getReturnCode() == Globals.CODE_SUCCESS)
				{
					AccountHelper.getInstance(UserIntroduceActivity.this).userInfoChange();
					Intent intent = new Intent();
					intent.putExtra(AccountManageActivity.INTRODEUC_KEY, filterContent(et_introduce.getText().toString()));
					setResult(RESULT_OK,intent);
					finish();
				}
			}
		}, filterContent(et_introduce.getText().toString()));
		
	}
	
	
}
