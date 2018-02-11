package views;

import com.aurora.weatherforecast.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CityOptionView extends AbstractWeatherView {
	
	private OptionView mOptionView;
	private CityDateView mCityDateView;
	private Context mContext;
	private RelativeLayout mRefreshRela;
	private ImageView mRefreshImage;
	private TextView mRefreshText;
	
	public CityOptionView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public CityOptionView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public CityOptionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public OptionView getOptionView(){
		return mOptionView;
	}
	
	private void init(Context context) {
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.cityoptionview, this, true);
		
		mOptionView = (OptionView)findViewById(R.id.optionview);
		mCityDateView = (CityDateView)findViewById(R.id.citydateview);
		mRefreshRela = (RelativeLayout)findViewById(R.id.refreshrela);
		mRefreshImage = (ImageView)findViewById(R.id.refreshimage);
		mRefreshText = (TextView)findViewById(R.id.refresh_text);
	}
	
	public CityDateView getCityDateView( ) {
		return mCityDateView;
	}
	
	public RelativeLayout getRefreshRela( ) {
		return mRefreshRela;
	}
	
	public ImageView getRefreshImage( ) {
		return mRefreshImage;
	}
	
	public TextView getRefreshText( ) {
		return mRefreshText;
	}
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onUpdating() {
		// TODO Auto-generated method stub
		
	}
}
