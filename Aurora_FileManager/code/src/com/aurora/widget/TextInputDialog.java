package com.aurora.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;
import com.aurora.tools.LogUtil;
import com.aurora.tools.ButtonUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.Util;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.text.Selection;
import aurora.widget.AuroraEditText;

public class TextInputDialog extends aurora.app.AuroraAlertDialog {
	protected static final String TAG = "TextInputDialog";
	private String mInputText;
	private String mTitle;
	private OnFinishListener mListener;
	private Context mContext;
	private View mView;
	private AuroraEditText mFolderName;
	private FileInfo fileInfo;
	private FileExplorerTabActivity activity;

	private android.widget.Button mDlgBtnDone = null;
	private int FILENAME_MAX_LENGTH = 60;
	private InputMethodManager inputMethodManager;

	public interface OnFinishListener {
		boolean onFinish(String text);
	}

	public TextInputDialog(Context context, String title, String text,
			OnFinishListener listener, InputMethodManager in) {
		super(context);
		mTitle = title;
		mListener = listener;
		mInputText = text;
		mContext = context;
		activity = (FileExplorerTabActivity) context;
		inputMethodManager = in;
	}

	public TextInputDialog(FileInfo fileInfo, Context context, String title,
			String text, OnFinishListener listener, InputMethodManager in) {
		this(context, title, text, listener, in);
		this.fileInfo = fileInfo;

	}

	public String getInputText() {
		return mInputText;
	}

	private static String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
	
	/**
	 * @param name
	 * @return 是否是不合法的文件名
	 */
	private static boolean isWrongText(String name) {
		if(name.trim().endsWith(".")||name.trim().startsWith(".")){
			return true;
		}
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(name);
		if (m.find()) {
			return true;
		}
		return false;
	}

	protected void onCreate(Bundle savedInstanceState) {
		mView = getLayoutInflater().inflate(R.layout.textinput_dialog, null);
		setTitle(mTitle);
		mFolderName = (AuroraEditText) mView.findViewById(R.id.text);
		mFolderName.setText(mInputText);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		setCanceledOnTouchOutside(false);// touchu消失
		Editable editable = mFolderName.getText();
		String FileExtension = Util.getFileExtension(mInputText);
		if (FileExtension != null && !fileInfo.IsDir) {// 如果为目录，则整个title高亮，不做处理
			Selection.setSelection(editable, 0, editable.length()
					- FileExtension.length() - 1);
		} else {
			Selection.setSelection(editable, 0, editable.length());
		}
		setView(mView);
		setButton(BUTTON_POSITIVE, mContext.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							inputMethodManager.hideSoftInputFromWindow(
									TextInputDialog.this.getCurrentFocus()
											.getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
						} catch (Exception e) {
							LogUtil.e(TAG,
									inputMethodManager + " "
											+ TextInputDialog.this + " error=="
											+ e.getMessage());
							e.printStackTrace();
						}
						if (which == BUTTON_POSITIVE) {
							mInputText = mFolderName.getText().toString();
							if (TextUtils.isEmpty(mInputText)
									|| isWrongText(mInputText)) {
								Toast.makeText(mContext,
										R.string.aurora_file_name_tint,
										Toast.LENGTH_SHORT).show();

								return;
							}
							if (mListener.onFinish(mInputText)) {
								dismiss();
							}
						}
					}
				});
		setButton(BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == BUTTON_NEGATIVE) {
							try {
								inputMethodManager.hideSoftInputFromWindow(
										TextInputDialog.this.getCurrentFocus()
												.getWindowToken(),
										InputMethodManager.HIDE_NOT_ALWAYS);
							} catch (Exception e) {
								LogUtil.e(TAG,
										inputMethodManager + " "
												+ TextInputDialog.this
												+ " error==" + e.getMessage());
								e.printStackTrace();
							}finally{
								dialog.dismiss();
							}
						}
					}

				});
		super.onCreate(savedInstanceState);
		this.setTextChangedCallbackForInputDlg();
	}

	/**
	 * This method register callback and set filter to Edit, in order to make
	 * sure that user input is legal. The input can't be illegal filename and
	 * can't be too long.
	 */
	public void setTextChangedCallbackForInputDlg() {

		mDlgBtnDone = this.getButton(DialogInterface.BUTTON_POSITIVE);
		// setEditTextFilter(mFolderName);
		mFolderName.addTextChangedListener(new TextWatcher() {

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
				if (s.toString().trim().length() == 0
						|| s.toString().trim().length() > FILENAME_MAX_LENGTH) {
					if (s.toString().trim().length() > FILENAME_MAX_LENGTH) {
						if (!ButtonUtil.isFastClick()) {
							Toast.makeText(mContext, R.string.max_filename,
									Toast.LENGTH_SHORT).show();
						}
					}
					mDlgBtnDone.setEnabled(false);
				} else if (isWrongText(s.toString())) {
					if (!ButtonUtil.isFastClick()) {
						Toast.makeText(mContext, R.string.input_error_char,
								Toast.LENGTH_SHORT).show();
					}
					mDlgBtnDone.setEnabled(false);
				} else {
					mDlgBtnDone.setEnabled(true);
				}
			}
		});
	}

	/**
	 * This method is used to set filter to EditText which is used for user
	 * entering filename. This filter will ensure that the inputed filename
	 * wouldn't be too long. If so, the inputed info would be rejected.
	 * 
	 * @param edit
	 *            The EditText for filter to be registered.
	 */
	private void setEditTextFilter(final AuroraEditText edit) {
		InputFilter filter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				int oldSize = 0;
				int sourceSize = 0;
				// original
				String name = edit.getText().toString();
				oldSize = name.getBytes().length;
				// new add sequences
				String seq = source.toString();
				sourceSize = seq.getBytes().length;
				if (sourceSize <= 0 || sourceSize > FILENAME_MAX_LENGTH) {
					mDlgBtnDone.setEnabled(false);
					return "";
				} else {
					mDlgBtnDone.setEnabled(true);
					return null;
				}
			}
		};
		edit.setFilters(new InputFilter[] { filter });
	}
}
