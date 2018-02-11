package com.netmanage.data;

public class WaveData{
	public final static int WAVE_WHERE_OF_FRONT = 0;
	public final static int WAVE_WHERE_OF_BACK = 1;
	public final static int MOVE_TO_RIGHT = 0;
	public final static int MOVE_TO_LEFT = 1;	
	private int moveTo;	
	private int moveMaxDistance;
	private float oldLeftMargin;
	private float curLeftMargin;
	
	public WaveData(int waveWhere){
		oldLeftMargin = 0;
	}
	
	public void initOrReset(int moveTo,int moveMaxDistance,float oldLeftMargin){
		this.moveTo = moveTo;
		this.moveMaxDistance = moveMaxDistance;
		this.oldLeftMargin = oldLeftMargin;
		this.curLeftMargin = oldLeftMargin;
	}
	
	public void computeCurLeftMargin(float interpolatedTime){
		curLeftMargin = getCurLeftMarginForFront(interpolatedTime);
	}
	
	public float getCurLeftMargin(){
		return this.curLeftMargin;
	}
	
	public int getMoveTo(){
		return this.moveTo;
	}
	
	private float getCurLeftMarginForFront(float interpolatedTime) {
		float leftMargin = 0;
		if(moveTo == MOVE_TO_RIGHT) {
			leftMargin = oldLeftMargin+(int)(moveMaxDistance*interpolatedTime);
		} else {
			leftMargin = oldLeftMargin-(int)(moveMaxDistance*interpolatedTime);
		}
		return leftMargin;
	}	
}
