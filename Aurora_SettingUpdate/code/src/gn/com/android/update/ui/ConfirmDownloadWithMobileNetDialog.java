
package gn.com.android.update.ui;

import gn.com.android.update.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ConfirmDownloadWithMobileNetDialog extends Activity implements OnClickListener {

    public static final int POSITIVE_RESULT = 0x001;
    
    public static final int NEGATIVE_RESULT = 0x002;
    
    private Button mPositiveButton;
    private Button mNegativeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.aurora_confirmm_dialog_layout);
        initView();
    }

    private void initView() {
        mPositiveButton = (Button) findViewById(R.id.dialog_positive_button);
        mNegativeButton = (Button) findViewById(R.id.dialog_negative_button);
        
        mPositiveButton.setOnClickListener(this);
        
        mNegativeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.dialog_positive_button:
                setResult(POSITIVE_RESULT);
                break;
            case R.id.dialog_negative_button:
                    setResult(NEGATIVE_RESULT);
                break;

            default:
                break;
        }
        finish();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
