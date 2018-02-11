package com.android.gallery3d.xcloudalbum.widget;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import android.R.string;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.test.UiThreadTest;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraTextView;

import com.android.gallery3d.R;

public class TextInputDialog extends aurora.app.AuroraAlertDialog {
	private static final String TAG = "TextInputDialog";
	private AuroraEditText mAlbumName;
	private AuroraTextView errorText;
	private String mTitle;
	private String mInputText;
	private View mView;
	private CommonFileInfo fileInfo;
	private Button mDlgBtnDone;
	private int FILENAME_MAX_LENGTH = 30;
	private InputMethodManager inputMethodManager;
	private List<CommonFileInfo> fileInfos;
	private OnFinishListener mListener;
	private boolean isRename=false;

	public interface OnFinishListener {
		boolean onFinish(boolean isRename,String text,boolean isCancel);
	}

	protected TextInputDialog(Context context) {
		super(context);
	}

	/**
	 * rename
	 * 
	 * @param context
	 * @param mTitle
	 * @param fileInfo
	 * @param inputMethodManager
	 */
	public TextInputDialog(Context context, String mTitle,
			CommonFileInfo fileInfo, List<CommonFileInfo> fileInfos,
			InputMethodManager inputMethodManager, OnFinishListener mListener) {
		super(context);
		isRename =true;
		this.mTitle = mTitle;
		this.fileInfo = fileInfo;
		this.fileInfos = fileInfos;
		this.inputMethodManager = inputMethodManager;
		this.mListener = mListener;
	}
	
	/**
	 * rename
	 * 
	 * @param context
	 * @param mTitle
	 * @param fileInfo
	 * @param inputMethodManager
	 */
	public TextInputDialog(Context context, int mTitleId,
			CommonFileInfo fileInfo, List<CommonFileInfo> fileInfos,
			InputMethodManager inputMethodManager, OnFinishListener mListener) {
		super(context);
		isRename =true;
		this.mTitle = context.getString(mTitleId);
		this.fileInfo = fileInfo;
		this.fileInfos = fileInfos;
		this.inputMethodManager = inputMethodManager;
		this.mListener = mListener;
	}


	/**
	 * create album
	 * 
	 * @param context
	 * @param mTitle
	 * @param inputMethodManager
	 */
	public TextInputDialog(Context context, String mTitle,
			List<CommonFileInfo> fileInfos,
			InputMethodManager inputMethodManager, OnFinishListener mListener) {
		super(context);
		this.mTitle = mTitle;
		this.fileInfos = fileInfos;
		this.inputMethodManager = inputMethodManager;
		this.mListener = mListener;
	}
	/**
	 * create album
	 * 
	 * @param context
	 * @param mTitle
	 * @param inputMethodManager
	 */
	public TextInputDialog(Context context, int mTitleId,
			List<CommonFileInfo> fileInfos,
			InputMethodManager inputMethodManager, OnFinishListener mListener) {
		super(context);
		this.mTitle = context.getString(mTitleId);;
		this.fileInfos = fileInfos;
		this.inputMethodManager = inputMethodManager;
		this.mListener = mListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mView = getLayoutInflater().inflate(R.layout.textinput_dialog, null);
		setTitle(mTitle);
		errorText = (AuroraTextView) mView.findViewById(R.id.input_error);
		mAlbumName = (AuroraEditText) mView.findViewById(R.id.input_album_name);
		mAlbumName.setHint(R.string.aurora_max_album_name);
		if(isRename){
			mAlbumName.setText(Utils.getPathNameFromPath(fileInfo.path));
			Editable editable = mAlbumName.getText();
			Selection.setSelection(editable, 0, editable.length());
		}
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		setCanceledOnTouchOutside(false);
		initOnClickListener();
		setView(mView);
		super.onCreate(savedInstanceState);
		this.setTextChangedCallbackForInputDlg();
	}

	/**
	 * 字符串转换unicode
	 * wenyongzhe 2015.11.18
	 */
	public static String string2Unicode(String string) {
	    StringBuffer unicode = new StringBuffer();
	    for (int i = 0; i < string.length(); i++) {
	        // 取出每一个字符
	        char c = string.charAt(i);
	        // 转换为unicode
	        unicode.append("\\u" + Integer.toHexString(c));
	    }
	    return unicode.toString();
	}
	
	private boolean isWrongText(String name) {
		//wenyongzhe 2015.11.18 start
		//Log.e(TAG, "------"+string2Unicode(name));
		String pstr = "[\u2600-\u27bf]+";  //杂项符号  印刷符号
	    Pattern p = Pattern.compile(pstr);  
	    Matcher m = p.matcher(name);  
	    if(m.find()){
	    	return true;
	    }
	  //wenyongzhe 2015.11.18 end
		if(name.contains("\\")||name.contains("/")||name.contains("?.:")||name.contains("*")||name.contains(".")){//wenyongzhe 2015.9.30  "*"
			return true;
		}
		return false;
	}
	
	//wenyongzhe 2015.11.3 disable "截图" start
	private boolean isRepeatedText(String name) {
		if(name.contains(getContext().getString(R.string.aurora_system_screenshot))){
			return true;
		}
		return false;
	}
	//wenyongzhe 2015.11.3 disable "截图" end
	
	private boolean isRepeatName(String name){
		Log.i("SQF_LOG", "info." + (fileInfos != null) + " name:" + name);
		if (fileInfos != null) {
			for (CommonFileInfo info : fileInfos) {
				Log.i("SQF_LOG", "info." + info.toString() + " name:" + name);
				if (name.equals(Utils.getPathNameFromPath(info.path))) {
					return true;
				}
			}
		}
		return false;
	}

	private void initOnClickListener() {
		String ok=getContext().getString(android.R.string.ok);
		if(!isRename){
			ok=getContext().getString(R.string.aurora_create);
		}
		setButton(BUTTON_POSITIVE, ok,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							inputMethodManager.hideSoftInputFromWindow(
									TextInputDialog.this.getCurrentFocus()
											.getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
						} catch (Exception e) {
							Log.e(TAG, "error::" + e.getMessage());
							e.printStackTrace();

						}
						if (which == BUTTON_POSITIVE) {
							 mInputText = mAlbumName.getText().toString();
							// if (TextUtils.isEmpty(mInputText)
							// || isWrongText(mInputText)) {
							// Toast.makeText(mContext,
							// R.string.aurora_file_name_tint,
							// Toast.LENGTH_SHORT).show();
							//
							// return;
							// }
							if (mListener.onFinish(isRename,mInputText,false)) {
								dismiss();
							}
						}
					}
				});
		setButton(BUTTON_NEGATIVE,
				getContext().getString(android.R.string.cancel),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == BUTTON_NEGATIVE) {
							try {
								inputMethodManager.hideSoftInputFromWindow(
										TextInputDialog.this.getCurrentFocus()
												.getWindowToken(),
										InputMethodManager.HIDE_NOT_ALWAYS);
							} catch (Exception e) {
								Log.e(TAG, " error::" + e.getMessage());
								e.printStackTrace();
							} finally {
								mListener.onFinish(isRename,null,true);
								dialog.dismiss();
							}
						}
					}
				});

	}

	private void setTextChangedCallbackForInputDlg() {
		MyLog.i2("SQF_LOG", "TextInputDialog::setTextChangedCallbackForInputDlg");
		mDlgBtnDone = this.getButton(DialogInterface.BUTTON_POSITIVE);
		mDlgBtnDone.setEnabled(false);
		mAlbumName.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.i("SQF_LOG", "onTextChanged s:" + s + " start:" + start + " before:" + before + " count:" + count);
				if(!Utils.isConnect(getContext())){
					errorText.setText(getContext().getString(
							R.string.aurora_album_network_fail));
					mDlgBtnDone.setEnabled(false);
					return;
				}
				if (s.toString().trim().length() == 0
						|| s.toString().trim().length() > FILENAME_MAX_LENGTH) {
					mDlgBtnDone.setEnabled(false);
				} else if (isRepeatName(s.toString())) {
					errorText.setText(getContext().getString(
							R.string.aurora_album_name_repeat));
//					 ToastUtils.showTast(getContext(),
//					 R.string.aurora_album_name_repeat);
					mDlgBtnDone.setEnabled(false);
				}else if (isWrongText(s.toString())) {
					errorText.setText(getContext().getString(
							R.string.aurora_album_error_char));
//					 ToastUtils.showTast(getContext(),
//					 R.string.aurora_album_error_char);
					mDlgBtnDone.setEnabled(false);
				} else if (isRepeatedText(s.toString())) {//wenyongzhe 2015.11.3 disable "截图" start
					errorText.setText(getContext().getString(
							R.string.aurora_album_cannot_char));
					mDlgBtnDone.setEnabled(false);
				} //wenyongzhe 2015.11.3 disable "截图" end
				else {
					mDlgBtnDone.setEnabled(true);
					if (s.toString().trim().length() == FILENAME_MAX_LENGTH) {
						errorText.setText(getContext().getString(
								R.string.aurora_max_album_name));
					}else {
						errorText.setText("");
					}
				}
			}
		});
	}

}
