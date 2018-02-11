package com.gionee.calendar.view;

import aurora.app.AuroraDatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import aurora.widget.AuroraDatePicker;
//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
public class GNCustomTimeDialog extends AuroraDatePickerDialog{

//	public CustomTimeDialog(Context context, int theme,
//			OnDateSetListener callBack, int year, int monthOfYear,
//			int dayOfMonth) {
//		super(context, theme, callBack, year, monthOfYear, dayOfMonth);
//		// TODO Auto-generated constructor stub
//	}

	public GNCustomTimeDialog(Context context, AuroraDatePickerDialog.OnDateSetListener callBack,
			int year, int monthOfYear, int dayOfMonth) {
		super(context, callBack, year, monthOfYear, dayOfMonth);
		// TODO Auto-generated constructor stub
	}
	
	
	
	public GNCustomTimeDialog(Context context, int theme,
			AuroraDatePickerDialog.OnDateSetListener callBack, int year, int monthOfYear,
			int dayOfMonth) {
		super(context, callBack, year, monthOfYear, dayOfMonth);
		// TODO Auto-generated constructor stub
	}



	@Override
	public void onDateChanged(AuroraDatePicker view, int year, int month, int day) {
		// TODO Auto-generated method stub
		super.onDateChanged(view, year, month, day);
		
	}
	//Gionee <jiating><2013-07-03> modify for CR00823772 begin
	@Override
	protected void onStop() {
			// TODO Auto-generated method stub
//		super.onStop();
	}
	//Gionee <jiating><2013-07-03> modify for CR00823772 begin
}
//Gionee <jiating> <2013-04-24> modify for CR00000000  end