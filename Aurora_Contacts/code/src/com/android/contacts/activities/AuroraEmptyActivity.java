package com.android.contacts.activities;

import com.android.contacts.dialpad.AuroraDialpadFragmentV2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.contacts.R;

public class AuroraEmptyActivity extends Activity{
	AuroraDialpadFragmentV2 mGnDialpadFragment;
	ImageView iv1,iv2,iv3,iv4,iv5,iv6,iv7,iv8,iv9,iv0,ivstar,ivpound,ivadd,ivdial,ivdel;
	Handler handler=new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			/*iv1=(ImageView)findViewById(R.id.aurora_one);
			iv1.setBackgroundResource(R.drawable.aurora_dial_num_1x);
			iv2=(ImageView)findViewById(R.id.aurora_two);
			iv2.setBackgroundResource(R.drawable.aurora_dial_num_2x);
			iv3=(ImageView)findViewById(R.id.aurora_three);
			iv3.setBackgroundResource(R.drawable.aurora_dial_num_3x);
			iv4=(ImageView)findViewById(R.id.aurora_four);
			iv4.setBackgroundResource(R.drawable.aurora_dial_num_4x);
			iv5=(ImageView)findViewById(R.id.aurora_five);
			iv5.setBackgroundResource(R.drawable.aurora_dial_num_5x);
			iv6=(ImageView)findViewById(R.id.aurora_six);
			iv6.setBackgroundResource(R.drawable.aurora_dial_num_6x);
			iv7=(ImageView)findViewById(R.id.aurora_seven);
			iv7.setBackgroundResource(R.drawable.aurora_dial_num_7x);
			iv8=(ImageView)findViewById(R.id.aurora_eight);
			iv8.setBackgroundResource(R.drawable.aurora_dial_num_8x);
			iv9=(ImageView)findViewById(R.id.aurora_nine);
			iv9.setBackgroundResource(R.drawable.aurora_dial_num_9x);
			iv0=(ImageView)findViewById(R.id.aurora_zero);
			iv0.setBackgroundResource(R.drawable.aurora_dial_num_0x);
			ivstar=(ImageView)findViewById(R.id.aurora_star);
			ivstar.setBackgroundResource(R.drawable.aurora_dial_num_starx);
			ivpound=(ImageView)findViewById(R.id.aurora_pound);
			ivpound.setBackgroundResource(R.drawable.aurora_dial_num_poundx);
			ivadd=(ImageView)findViewById(R.id.aurora_addcontacts);
			ivadd.setBackgroundResource(R.drawable.aurora_dial_addcontactx);
			ivdial=(ImageView)findViewById(R.id.aurora_dialButton);
			ivdial.setBackgroundResource(R.drawable.aurora_dial_callx);
			ivdel=(ImageView)findViewById(R.id.aurora_deleteButton);
			ivdel.setBackgroundResource(R.drawable.aurora_dial_delx);*/
		}
		
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(new FrameLayout(this), new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		setContentView(R.layout.aurora_dialpad_fragment_v2);
		Log.v("SHIJIAN", "AuroraEmptyActivity onCreate1 time="+System.currentTimeMillis());
		handler.sendMessageDelayed(handler.obtainMessage(), 100);
	}

	
	
	
}
