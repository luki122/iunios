package com.aurora.feedback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.settings.R;
import com.aurora.feedback.utils.DfProgressDialog;
import com.aurora.feedback.utils.FeedBackUtil;
import com.aurora.feedback.utils.HttpConnection;
import com.aurora.utils.Utils2Icon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

public class AuroraAdvicesActivity extends AuroraActivity implements OnClickListener{
    
	private static final String TAG="gd";
	private Dialog adNoticeDialog;
	private EditText adContact_infor_detail;
    private EditText adProblem_description_detail;
    private GridView adBrief_list;
    private Button adCommit;
    private addFeedBackAdapter adAdapter;
    private AuroraActionBar mActionBar;
    private ArrayList<Bitmap> adPicName;
    private Bitmap defaultItemBitmap;
    private Map<Bitmap, String> picMap=new HashMap<Bitmap, String>();    
    private DfProgressDialog progressDialog;
    private int respondeCode=-1;
    private static final int MAXSiZE=3;
	
    private static final int REQUEST_CODE_ADVICE=3;
    private static final int MSG_UPDATE_DRAW_DISPLAY=1;
    private static final int MSG_NOTIFY_DETAIL_INFO=2;
    private static final int MSG_NOTIFY_DETAIL_CONTACTID=4;
    private static final int MSG_NOTIFY_RESPONDECODE=5;
    private static final int MSG_NOTIFY_NO_CONNECTED=6;
    private UIHandle uiHandle;
	class UIHandle extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_DRAW_DISPLAY:
				refresh();
				break;
			case MSG_NOTIFY_DETAIL_INFO:
				showNotifyToast(R.string.suggest_discrible);
				break;
			case MSG_NOTIFY_RESPONDECODE:
				 if(progressDialog!=null)
				 {
					 progressDialog.close();
				 }
				 if(respondeCode==200)
				 {
					 showNotifyToast(R.string.uploaded_server);
				 }else
				 {
					 showNotifyToast(R.string.unUpload_server);
				 }
				 finish();
				 break;
			case MSG_NOTIFY_NO_CONNECTED:
				showNotifyToast(R.string.network_unConnect);
				 break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	private void showNotifyToast(int id)
	{
		 Toast.makeText(getApplicationContext(), getString(id).toString(), Toast.LENGTH_SHORT).show();
	}
	
	public void refresh()
	{
		adAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_advice ,AuroraActionBar.Type.Normal);
		getAuroraActionBar().setTitle(R.string.privider_sup);
		init();
		registerActionBar();
	}
	
	private void registerActionBar()
	{
		getAuroraActionBar().setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {
			@Override
			public void onAuroraActionBarItemClicked(int arg0) {
				Log.d(TAG, " onclick");
			}
		});
	}
	
	private void init()
	{
		initView();
		initData();
	}
	
    private void initView()
    {
		adContact_infor_detail=(EditText)findViewById(R.id.contact_infor_detail);
		adProblem_description_detail=(EditText)findViewById(R.id.problem_description_detail);
		adBrief_list=(GridView)findViewById(R.id.brief_list);
		adProblem_description_detail.addTextChangedListener(adviceWatcher);
		adContact_infor_detail.addTextChangedListener(contactidWatcher);
		adBrief_list.setOnItemClickListener(mOnItemClickListener);
		adCommit=(Button)findViewById(R.id.commit);
		adCommit.setOnClickListener(this);
    }	
    
    private TextWatcher contactidWatcher = new TextWatcher() {  
      @Override  
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }  
      @Override  
      public void onTextChanged(CharSequence s, int start, int before, int count) {  
      }  
      @Override  
      public void afterTextChanged(Editable s) {
			if (adContact_infor_detail.length() == 50) {
				Toast.makeText(getBaseContext(),R.string.limit_contentid_number, Toast.LENGTH_SHORT).show();
			}
      	}
  };  
    
    private TextWatcher adviceWatcher = new TextWatcher() {  
//        private int editStart;  
//        private int editEnd;
//        private int maxLen= 200;// the max byte  (一个汉字两个字节)  
        @Override  
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }  
        @Override  
        public void onTextChanged(CharSequence s, int start, int before, int count) {  
        }  
        @Override  
        public void afterTextChanged(Editable s) {
        	Log.d(TAG, " size="+adProblem_description_detail.length());
			if (adProblem_description_detail.length()  == 100) {
				Toast.makeText(getBaseContext(),R.string.limit_chinese_number, Toast.LENGTH_SHORT).show();
			}
//                editStart = adProblem_description_detail.getSelectionStart();  
//                editEnd = adProblem_description_detail.getSelectionEnd();  
//                adProblem_description_detail.removeTextChangedListener(this);  
//                if (!TextUtils.isEmpty(adProblem_description_detail.getText())) {  
//                    String etstring = adProblem_description_detail.getText().toString().trim();
//                    boolean limitNumb=true;
//                    while (calculateLength(s.toString()) > maxLen) {  
//                        s.delete(editStart - 1, editEnd);  
//                        editStart--;  
//                        editEnd--;
//                        if(limitNumb)
//                        {
//                        	Toast.makeText(getBaseContext(), R.string.limit_chinese_number,   Toast.LENGTH_SHORT).show();
//                        	limitNumb=false;
//                        }
//                    }
//                }  
//                adProblem_description_detail.setText(s);  
//                adProblem_description_detail.setSelection(editStart);  
//                adProblem_description_detail.addTextChangedListener(this);  
        	}
    };  
    
    private int calculateLength(String etstring) {  
        char[] ch = etstring.toCharArray();  
  
        int varlength = 0;  
        for (int i = 0; i < ch.length; i++) {  
            if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) { // 中文字符范围0x4e00 0x9fbb  
                varlength = varlength + 2;  
            } else {  
                varlength++;  
            }  
        }  
        Log.d(TAG, "varlength = "  + varlength);  
        // 这里也可以使用getBytes,更准确嘛  
        // varlength = etstring.getBytes(CharSet.forName(GBK)).lenght;// 编码根据自己的需求，注意u8中文占3个字节...  
        return varlength;  
    }  

    
    private OnItemClickListener mOnItemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
		    if(adAdapter.getItem(position).equals(defaultItemBitmap))
		    {
		    	openGallery(position);
		    }
		}
	};
    
    @Override
	public void onBackPressed() {
    	if(adProblem_description_detail.getText().toString().equals("") || respondeCode==200)
    	{
    		finish();
    	}else
    	{
    		createNoticeDialog();
    	}
	//	super.onBackPressed();
	}

	private void initData()
    {
    	defaultItemBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.add_pic_bg);
    	adPicName=new ArrayList<Bitmap>();
    	adPicName.add(defaultItemBitmap);
    	if(adAdapter==null)
    	{
    		adAdapter=new addFeedBackAdapter<Bitmap>(this, adPicName,defaultItemBitmap);
    	}
    	adBrief_list.setAdapter(adAdapter);
    	uiHandle=new UIHandle();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commit:
			createSubmitDialog();
			break;
		default:
			break;
		}
        		
	}
	private void upload()
	{
		if (adProblem_description_detail.getText().toString().equals("")) 
		{
			uiHandle.sendEmptyMessage(MSG_NOTIFY_DETAIL_INFO);
			return;
		}
		
         if(progressDialog==null)
         {
        	 progressDialog=new DfProgressDialog();
         }
         
         progressDialog.show(this, getString(R.string.upload), getString(R.string.upload_info));
		new Thread(new Runnable() {
			@Override
			public void run() {
				uploadDate();
			}
		}).start();
	}
	
	private void createNoticeDialog()
	{
		adNoticeDialog = new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.feed_back)
				.setMessage(R.string.feed_back_summury)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				 })
				.setNegativeButton(android.R.string.cancel, new  DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).show();
	}
	
	private void createSubmitDialog()
	{
		if(!FeedBackUtil.isNetworkAvailable(this))
		{
			uiHandle.sendEmptyMessage(MSG_NOTIFY_NO_CONNECTED);
			return ;
		}
		Dialog adNoticeDialog = new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.feed_back)
				.setMessage(R.string.uploading_dialog)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						upload();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).show();
	}
	
	private void openGallery(int type) {
        /*Intent intent = new Intent();
        if (type == 0)
            intent.setType("image/*");
        else
            intent.setType("video/*");
        // intent.putExtra("crop", "true");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // 限制为只能用我们系统自带的Gallery来选择
        intent.setPackage("com.android.gallery3d");
        if (type == 0)
            startActivityForResult(intent, Globals.REQUEST_CODE_ALBUM);
        else
            startActivityForResult(intent, Globals.REQUEST_CODE_VIDEO);*/
        Intent intent = new Intent();
        //if (type == 0) {
            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT/*"com.aurora.filemanager.MORE_GET_CONTENT"*/);
            intent.setAction("com.aurora.filemanager.MORE_GET_CONTENT");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // intent.setPackage("com.android.gallery3d");
            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle = new Bundle();
            if(MAXSiZE >= type){
            	bundle.putInt("size", MAXSiZE-type);
            }
            // intent.setPackage("com.android.gallery3d");
            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(bundle);
            startActivityForResult(intent,REQUEST_CODE_ADVICE);
       // } 
    }

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "requestCode="+requestCode+"  resultCode="+resultCode);
		switch (requestCode) {
		case REQUEST_CODE_ADVICE:
			if(resultCode==Activity.RESULT_OK)
			{
                /*Uri uri = data.getData();
                try {
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                    // mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    String imagePath;
                    if (cursor != null) {
                        cursor.moveToFirst();
                        imagePath = cursor.getString(1);
                        insertPicToEdit(imagePath, Globals.ATTACHMENT_TYPE_IMAGE, false);
                    }
                    cursor.close();
                } catch (Exception e) {
                    FileLog.e(TAG, e.getMessage());
                }*/
                Bundle bundle = data.getExtras();
                ArrayList<String> paths = bundle.getStringArrayList("image");
                if(paths.size()==0)
                {
                	return ;
                }
                ArrayList<Bitmap> tempPicName=new ArrayList<Bitmap>();
                tempPicName.clear();
                if(adPicName.size()!=0)
                {
                	for(Bitmap bitmap : adPicName)
                	{
                		if(bitmap.equals(defaultItemBitmap))
                		{
                			continue;
                		}else
                		{
                			tempPicName.add(bitmap);
                		}
                	}
                }
                int tempSpace=MAXSiZE-tempPicName.size();
                for(int temp=0; temp<=paths.size()-1;temp++)
                {
                	if(temp>tempSpace-1)
                	{
                		break;
                	}
                	Log.d(TAG, "  path="+paths.get(temp));
                	Bitmap bitmap=FeedBackUtil.convertToBitmap(paths.get(temp),200	,200);
                	tempPicName.add(bitmap);
                	picMap.put(bitmap, paths.get(temp));
//                	picMap.put(bitmap, paths.get(temp));
                }
                adPicName.clear();
                for(Bitmap bitmap : tempPicName)
                {
                	adPicName.add(bitmap);
                }
                if(!(adPicName.size()>2))
                {
                	adPicName.add(defaultItemBitmap);
                }
                uiHandle.sendEmptyMessage(MSG_UPDATE_DRAW_DISPLAY);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
    private void uploadDate()
    {
         Map<String,File> files = new HashMap<String, File>();
         if(adPicName!=null && adPicName.size()>1)
         {
        	 for(int index=0;index<adPicName.size();index++)
        	 {
        		 if(adPicName.get(index).equals(defaultItemBitmap))
        		 {
        			 continue;
        		 }
        		 
        		 if(picMap.get(adPicName.get(index))!=null)
        		 {
//        			 File file=new File(picMap.get(adPicName.get(index)));
//        	    	 File file = FeedBackUtil.saveBitmapTofile(adPicName.get(index),adPicName.get(index).toString());
        			 File file=new File(FeedBackUtil.storePath(picMap.get(adPicName.get(index))));
        		     files.put(file.getName(),file);
        		 }
        	 }
         }
		try {
			HttpConnection.initialConn();
			respondeCode=HttpConnection.upLoad(this,files,adProblem_description_detail.getText().toString(),adContact_infor_detail.getText().toString(),HttpConnection.SUGGESTS);
		} catch (Exception e) {
			respondeCode=-1;
		}
		uiHandle.sendEmptyMessage(MSG_NOTIFY_RESPONDECODE);
    }
}
