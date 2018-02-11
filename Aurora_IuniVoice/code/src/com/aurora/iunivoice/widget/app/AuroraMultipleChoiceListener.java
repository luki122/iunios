package com.aurora.iunivoice.widget.app;

import android.content.DialogInterface;
import android.widget.Button;
import android.widget.EditText;

/**
 * 该接口主要用于在多选Dialog中需要动态添加数据的情况
 * <p>在这种情况下的多选Dialog会在最后一项中显示一个Editext和一个Button，该
 * 接口主要是为了响应Button的事件，并将添加的数据回传给调用方，以此来实现数
 * 据的保存。
 *@author IUNI
 *@since 2014-8-26
 */
public interface AuroraMultipleChoiceListener  extends DialogInterface.OnMultiChoiceClickListener{
	
	
	/**
	 * call this method to save your data witch added into list automatic
	 * @param dialog  current dialog 
	 * @param which  selected item position
	 * @param isChecked  item is checked or not 
	 * @param itemText    new text 
	 */
	public void onClick(DialogInterface dialog, int which, boolean isEqual,CharSequence itemText);
	
	public void onInput(EditText edit,Button addBtn);

}
