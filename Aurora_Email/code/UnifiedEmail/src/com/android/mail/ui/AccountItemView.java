/**
 * Copyright (c) 2013, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.mail.ui;

import com.android.mail.R;

import android.widget.ImageView;
import android.widget.TextView;

import com.android.mail.providers.Account;
import com.android.mail.utils.Utils;

import android.content.Context;

import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.ImageButton;
import android.content.Intent;
import com.android.mail.providers.MailAppProvider;
import com.android.mail.ui.ControllableActivity;
import android.util.Log;
/**
 * The view for each account in the folder list/drawer.
 */
public class AccountItemView extends RelativeLayout {
    private TextView mAccountTextView;
    private TextView mUnreadCountTextView;
    //Aurora <shihao> <2014-11-04> for new DrawLayout begin
//    private ImageView mSelectedButton;
    private RelativeLayout addAccountButton;
    private Context mContext;
    private ControllableActivity mActivity;
    private final static int ADD_NEW_ACCOUNT_FROM_DL = 1;
    private AccountController accountController;
    private ImageView accountListDivide;
    //Aurora <shihao> <2014-11-04> for new DrawLayout end

    public AccountItemView(Context context) {
        super(context);
    	mContext = context;
    }

    public AccountItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    	mContext = context;
    }

    public AccountItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    	mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAccountTextView = (TextView)findViewById(R.id.name);
        mUnreadCountTextView = (TextView)findViewById(R.id.unread);
        //Aurora <shihao> <2014-11-04> for new DrawLayout begin
//        mSelectedButton = (ImageView)findViewById(R.id.account_radio_button);
        addAccountButton = (RelativeLayout)findViewById(R.id.add_new_account);
        ImageButton settingImageButton = (ImageButton)addAccountButton.findViewById(R.id.aurora_draweritem_setting);
        settingImageButton.setOnClickListener(addAccountListener);
        ImageButton addAccountImageButton = (ImageButton)addAccountButton.findViewById(R.id.aurora_add_newaccount);
        addAccountImageButton.setOnClickListener(addAccountListener);
        //addAccountButton.setOnClickListener(addAccountListener);
        accountListDivide = (ImageView)findViewById(R.id.account_list_divide);
       //Aurora <shihao> <2014-11-04> for new DrawLayout end
    }
    //Aurora <shihao> <2014-11-04> for new DrawLayout begin
    OnClickListener addAccountListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			
			switch (arg0.getId()) {
			case R.id.aurora_draweritem_setting:{
            	Account account = mActivity.getAccountController().getAccount();
            	Utils.showSettings(mActivity.getActivityContext(), account);
				break;
			}
				
				
				
			case R.id.aurora_add_newaccount:{
	            final Intent noAccountIntent = MailAppProvider.getNoAccountIntent(mContext,false);
	            if (noAccountIntent != null) {  
	            	accountController.closeDrawer(false, null, null);
	        		new Handler().postDelayed(new Runnable() {
	        			
	        			@Override
	        			public void run() {
	        				// TODO Auto-generated method stub
	                    	mActivity.startActivity(noAccountIntent);
	        			}
	        		}, 150);
	            }
				break;
			}
				

			default:
				break;
			}
			// TODO Auto-generated method stub

		}
	}; 

    /**
     * Sets the account name and draws the unread count. Depending on the account state (current or
     * unused), the colors and drawables will change through the call to setSelected for each
     * necessary element.
     *
     * @param account account whose name will be displayed
     * @param isCurrentAccount true if the account is the one in use, false otherwise
     * @param count unread count
     */
    public void bind(final ControllableActivity activity,final Account account, final boolean isCurrentAccount, final int count) {
    	mActivity = activity;
    	accountController = mActivity.getAccountController();
        mAccountTextView.setText(account.name);
//        mUnreadCountTextView.setSelected(isCurrentAccount);
        mAccountTextView.setSelected(isCurrentAccount);
        //Aurora <shihao> <2014-11-04> for new DrawLayout begin
//        mSelectedButton.setSelected(isCurrentAccount);
//        Log.i("test","isCurrentAccount ="+isCurrentAccount  + "  account.name= "+account.name);
        if(isCurrentAccount){
        	mAccountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        	addAccountButton.setVisibility(View.VISIBLE);
        	mUnreadCountTextView.setVisibility(View.GONE);
        	RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)accountListDivide.getLayoutParams();
        	layoutParams.setMargins(10, 0, 10, 0);
        	accountListDivide.setLayoutParams(layoutParams);        	
        }else{
        	addAccountButton.setVisibility(View.GONE);
        	mAccountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }
        setUnreadCount(count,isCurrentAccount);
        //Aurora <shihao> <2014-11-04> for new DrawLayout end
    }

    /**
     * Sets the unread count, taking care to hide/show the textview if the count
     * is zero/non-zero.
     */
    private void setUnreadCount(final int count,boolean isCurrentAccount) {
        mUnreadCountTextView.setVisibility(((count > 0) && !isCurrentAccount)? View.VISIBLE : View.GONE);
        if (count > 0) {
            mUnreadCountTextView.setText(Utils.getUnreadCountString(getContext(), count));
        }
    }
}
