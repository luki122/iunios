package views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.view.View;

public class TemperatureView extends View {

	private float width,height;
	private float density;
	public TemperatureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public TemperatureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public TemperatureView(Context context) {
		super(context);
		initView();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension((int)width, (int)height);
	}
	
	
	public List<Data> getTData(){
		return tempDate;
	}
	
	private Paint paint=new Paint();
	private Path path=new Path();
	private List<Data> tempDate=new ArrayList<Data>();
	private float[] temperatures;
	private void initView(){
		density=getResources().getDisplayMetrics().density;
		width=getResources().getDisplayMetrics().widthPixels;
		height=156*density;
	}
	
	public void setTemperatures(float[] temperatures){
		this.temperatures=temperatures;
		initTempDate();
	}
	
	private void initTempDate(){
		tempDate.clear();
		if(temperatures!=null)
		{
			for(int i=0;i<temperatures.length;i++)
			{
				Data d=new Data();
				d.px=i*width/(temperatures.length-1);
				d.py=temperatures[i]/30*(height-20*density);
				d.pd=temperatures[i];
				d.endPy=height;
				tempDate.add(d);
			}
		}
		invalidate();
	}
	
	public Data getHighestData(){
		Data data=tempDate.get(0);
		for(int i=1;i<tempDate.size();i++)
		{
		    if(tempDate.get(i).py<data.py)
		    {
		    	data=tempDate.get(i);
		    }
		}
		return data;
	}
	
	public Data getLowestData(){
		Data data=tempDate.get(0);
		for(int i=1;i<tempDate.size();i++)
		{
		    if(tempDate.get(i).py>data.py)
		    {
		    	data=tempDate.get(i);
		    }
		}
		return data;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.canvas=canvas;
		drawDate(canvas);
	}
	
	private float[] getControlPoint(Data d1,Data d2){
		float[] ps=new float[2];
		ps[0]=d1.px+(d2.px-d1.px)/2;
		if(d1.py>d2.py)
		{
			ps[1]=d1.py-20*density;
		}else{
			ps[1]=d1.py+20*density;
		}
		return ps;
	}
	
	public float getPointX(int i){
		return tempDate.get(i).px;
	}
	
	private int lineIndex=1;
	private float lineX=0f;
	private Canvas canvas;
	
	private void drawDate(Canvas canvas){
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2);
		Paint linePaint=new Paint();
		linePaint.setColor(Color.parseColor("#55ffffff"));
		linePaint.setStyle(Style.STROKE);
		linePaint.setAntiAlias(true);
		linePaint.setStrokeWidth(1);
		if(tempDate.size()!=0)
		{
			path.reset();
			path.moveTo(tempDate.get(0).px, tempDate.get(0).py);
			float[] cp;
			for(int i=1;i<tempDate.size();i++)
			{
				cp=getControlPoint(tempDate.get(i-1), tempDate.get(i));
				path.quadTo(cp[0],cp[1], tempDate.get(i).px, tempDate.get(i).py);
				canvas.drawPath(path, paint);
			}
			path.lineTo(width, height);
			path.lineTo(0, height);
			path.close();
			canvas.clipPath(path,Op.INTERSECT);
			canvas.drawColor(Color.parseColor("#33ffffff"));
		}
	}
	
	public int getTWidth() {
		return (int)width;
	}
	
	public int getTHeight(){
		return (int)height;
	}
	
	public static class Data{
		float px,py,pd,endPy;
	}
}
