package com.aurora.community.activity;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aurora.community.R;
import com.aurora.community.activity.PersonalCenter.RefreshMode;
import com.aurora.community.adapter.MessageBoxAdapter;
import com.aurora.community.utils.DialogUtil;
import com.aurora.community.utils.DialogUtil.IAlertDialogClick;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.ToastUtil;
import com.aurora.community.view.LeftScollDeleteRelativeLayout;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.ClearAllMessageHolder;
import com.aurora.datauiapi.data.bean.DeleteMessageHolder;
import com.aurora.datauiapi.data.bean.MessageBoxHolder;
import com.aurora.datauiapi.data.bean.MessageBoxObj;
import com.aurora.datauiapi.data.bean.MessageReadAllHolder;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableManager;


public class MessageBoxActivity extends BaseActivity {

	private static final int MESSAGE_CLEAR_ITEM_ID = 0x4512;
	
	private PullToRefreshListView lv_messages;
	private ImageView iv_no_msg_tip;
	private ArrayList<MessageBoxObj> datas = new ArrayList<MessageBoxObj>();
	
	private final int PAGE_REQUEST_COUNT = 20;
	private int currentPage =1;
	private Dialog clearTipDialog;
	private MessageBoxAdapter adapter;
	
	public static final String HAS_NEW_MSG_KEY = "has_new_msg_key";
	private boolean isHasNewMsg =false;
	
	private RefreshMode refreshMode = RefreshMode.REFRESH_MODE;
	
	private RelativeLayout loading_layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mComanager = new CommunityManager(this);
		setContentView(R.layout.activity_message_box);
		setupViews();
		if(SystemUtils.isNetworkConnected())
		{
			allMessageRead();
			requestMsgs();
			loading_layout.setVisibility(View.VISIBLE);
		}else{
			lv_messages.setVisibility(View.GONE);
			findViewById(R.id.network_layout).setVisibility(View.VISIBLE);
			loading_layout.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void setupViews() {
		isHasNewMsg = getIntent().getBooleanExtra(HAS_NEW_MSG_KEY, false);
		loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
		// TODO Auto-generated method stub
		lv_messages = (PullToRefreshListView) findViewById(R.id.lv_messages);
		iv_no_msg_tip = (ImageView) findViewById(R.id.iv_no_msg_tip);
		lv_messages.setEnableDelete(true);
		lv_messages.setPullLoadEnable(true);
		findViewById(R.id.bt_retry_network).setOnClickListener(listener);
		lv_messages.setXListViewListener(new IXListViewListener() {
			
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				if(refreshMode != RefreshMode.NONE)
					return;
				currentPage = 1;
				refreshMode = RefreshMode.REFRESH_MODE;
				requestMsgs();
			}
			
			@Override
			public void onLoadMore() {
				if(refreshMode != RefreshMode.NONE)
					return;
				// TODO Auto-generated method stub
				currentPage++;
				refreshMode = RefreshMode.LOAD_MORE_MODE;
				requestMsgs();
			}
		});
		adapter = new MessageBoxAdapter(datas, this);
		lv_messages.setAdapter(adapter);
		lv_messages.setOnItemClickListener(itemClickListener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			refreshMode = RefreshMode.REFRESH_MODE;
			findViewById(R.id.network_layout).setVisibility(View.GONE);
			loading_layout.setVisibility(View.VISIBLE);
			lv_messages.setVisibility(View.VISIBLE);
			allMessageRead();
			requestMsgs();
		}
	};
	
	private final static int REQUEST_CODE = 0x4512;
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if (id == -1 ||((LeftScollDeleteRelativeLayout)view).isRubbishShow()) {
				return;
			}
			Intent intent = new Intent(MessageBoxActivity.this,PostDetailActivity.class);
			intent.putExtra(PostDetailActivity.USER_ID_KEY, datas.get((int)id).getRuid());
			intent.putExtra(PostDetailActivity.PAGE_ID_KEY,datas.get((int)id).getItemid());
			intent.putExtra(PostDetailActivity.COME_FROM_KEY,MessageBoxActivity.class.getName());
			startActivityForResult(intent, REQUEST_CODE);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQUEST_CODE && resultCode == PostDetailActivity.RESULT_BACK)
		{
			finish();
		}
		
	}
	
	public void showClearItem(boolean isShow){
		if(isShow)
		{
			if(getActionBarItem(MESSAGE_CLEAR_ITEM_ID) == null)
			{
			   addActionBarItem(getString(R.string.message_box_clear), MESSAGE_CLEAR_ITEM_ID);
			   iv_no_msg_tip.setVisibility(View.GONE);
			   lv_messages.setVisibility(View.VISIBLE);
			}
		}else{
			removeActionBarItem(MESSAGE_CLEAR_ITEM_ID);
			iv_no_msg_tip.setVisibility(View.VISIBLE);
			lv_messages.setVisibility(View.GONE);
		}
	}
	
	private void requestMsgs() {
		mComanager.messageBox(new DataResponse<MessageBoxHolder>() {
					@Override
					public void run() {
						super.run();
						if (value != null) {
							if (value.getReturnCode() == Globals.CODE_SUCCESS) {
								if (refreshMode == RefreshMode.REFRESH_MODE) {
									datas.clear();
								}
								datas.addAll(value.getData().getDataContext());
								if(datas.size() == 0)
								{
									iv_no_msg_tip.setVisibility(View.VISIBLE);
									lv_messages.setVisibility(View.GONE);
								}else if(lv_messages.getVisibility() == View.GONE)
								{
									iv_no_msg_tip.setVisibility(View.GONE);
									lv_messages.setVisibility(View.VISIBLE);
								}
								if(value.getCountRow() <= PAGE_REQUEST_COUNT)
								{
									lv_messages.setPullLoadEnable(false);
								}else{
									lv_messages.setPullLoadEnable(true);
								}
							}
						}
						adapter.notifyDataSetChanged();
						refreshMode = RefreshMode.NONE;
						lv_messages.stopLoadMore();
						lv_messages.stopRefresh();
						loading_layout.setVisibility(View.GONE);
					}
				}, PAGE_REQUEST_COUNT, "all", datas.size() == 0 || refreshMode == RefreshMode.REFRESH_MODE ? null : datas.get(datas.size() - 1).getNid(), currentPage + "");
	}
	
	private void allMessageRead(){
		mComanager.messageReadAll(new DataResponse<MessageReadAllHolder>(){
			@Override
			public void run() {
				super.run();
			}
		});
	}
	
	private Dialog deleteDialog;
	private int deleteCurrentPosition = -1;
	public void delete(int position){
		if(deleteDialog == null)
		{
			deleteDialog = DialogUtil.getAlertDialog(this, getString(R.string.sure_delete_msg), R.string.delete_tip_cancel, R.string.delete_tip_delete, deleteClick);
		}
		deleteCurrentPosition = position;
		deleteDialog.show();
	}
	
	private IAlertDialogClick deleteClick = new IAlertDialogClick() {
		
		
		@Override
		public void sureClick() {
			// TODO Auto-generated method stub
			deleteDialog.dismiss();
			deleteNotification(deleteCurrentPosition);
		}
		
		@Override
		public void cancelClick() {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void deleteNotification(final int position){
		if(position > datas.size() -1)
			return;
		mComanager.deleteMessage(new DataResponse<DeleteMessageHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						datas.remove(position);
						adapter.notifyDataSetChanged();
					}
					ToastUtil.shortToast(value.getMsg());
					deleteCurrentPosition = -1;
				}
			}
		}, datas.get(position).getNid());
	}
	
	private static final int ERROR = 0x4511;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ERROR:
				refreshMode = RefreshMode.NONE;
				lv_messages.stopLoadMore();
				lv_messages.stopRefresh();
				loading_layout.setVisibility(View.GONE);
				if(datas.size() == 0)
				{
					lv_messages.setVisibility(View.GONE);
					findViewById(R.id.network_layout).setVisibility(View.VISIBLE);
				}
				break;

			default:
				break;
			}
			
			
		}
	};
	
	
	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		// TODO Auto-generated method stub
		super.onError(code, message, manager, e);
		handler.sendEmptyMessage(ERROR);
	}
	
	@Override
	public void setupAuroraActionBar() {
		// TODO Auto-generated method stub
		super.setupAuroraActionBar();
		setTitleRes(R.string.message_box_title);
	}

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		// TODO Auto-generated method stub
		super.onActionBarItemClick(view, itemId);
		
		if(itemId == MESSAGE_CLEAR_ITEM_ID)
		{
			if(clearTipDialog == null)
			{
				clearTipDialog = DialogUtil.getAlertDialog(this, getString(R.string.sure_clear_msg), R.string.delete_tip_cancel, R.string.delete_tip_delete, alertDialogClick);
			}
			clearTipDialog.show();
		}else if(itemId == BACK_ITEM_ID)
		{
			finish();
		}
		
	}
	
	private IAlertDialogClick alertDialogClick = new IAlertDialogClick() {
		
		@Override
		public void sureClick() {
			deleteAllMessage();
			clearTipDialog.dismiss();
		}
		
		@Override
		public void cancelClick() {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void deleteAllMessage(){
		mComanager.clearAllMessage(new DataResponse<ClearAllMessageHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						datas.clear();
						adapter.notifyDataSetChanged();
					}
					ToastUtil.longToast(value.getMsg());
				}
				
			}
		});
	}
}
