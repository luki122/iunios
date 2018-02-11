
package com.aurora.note.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * @author JimXia 2014年7月11日 上午9:11:47
 */
public class WaveformViewForPlay extends WaveformView {
//    private static final String TAG = "WaveformViewForPlay";
    
//    protected static final int RATE_X = 64;
//    
//    private ArrayList<Byte> mWaveDataBuffer = new ArrayList<Byte>();
//    private ArrayList<Byte> mWaveDataFFTBuffer = new ArrayList<Byte>();
//    private Visualizer mVisualizer;
//    private static final int SCALE_Y = 4;
//    private boolean mStopped = false;
    
    private Drawable mWaveDrawable;
    
    public WaveformViewForPlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setWaveDrawable(int waveDrawableResId) {
        mWaveDrawable = getResources().getDrawable(waveDrawableResId);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mWaveDrawable != null) {
            mWaveDrawable.setBounds(0, 0, getWidth(), getHeight());
            mWaveDrawable.draw(canvas);
        } else {
            super.onDraw(canvas);
        }
        
        /*if (mWaveDataBuffer.isEmpty() && mWaveDataFFTBuffer.isEmpty()) {
            super.onDraw(canvas);
            return;
        }

        canvas.drawColor(BG_COLOR);

        boolean needScale = true;
        ArrayList<Byte> buf = mWaveDataFFTBuffer;
        if (buf.isEmpty()) {
            buf = mWaveDataBuffer;
            needScale = false;
        }
        
        int start = getWidth() - buf.size() * WaveformHelper.DIVIDER;
        canvas.drawLine(0, mBaseLine, start - WaveformHelper.DIVIDER, mBaseLine, mWaveFormDrawPaint);
        for (int i = 0; i < buf.size(); i++) {
            int y = Math.abs(buf.get(i));
            if (needScale) {
                y *= SCALE_Y;
            }
            canvas.drawLine(start + (i - 1) * WaveformHelper.DIVIDER, mBaseLine - y, start + i * WaveformHelper.DIVIDER,
                    mBaseLine + y, mWaveFormDrawPaint);
        }*/
    }

    /**
     * Links the visualizer to a player
     * 
     * @param player - MediaPlayer instance to link to
     */
    /*public void link(MediaPlayer player) {
        if (player == null) {
            throw new NullPointerException("Cannot link to null MediaPlayer");
        }

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(player.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        // Pass through Visualizer data to VisualizerView
        Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                updateVisualizer(bytes);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                updateVisualizerFFT(bytes);
            }
        };

        mVisualizer.setDataCaptureListener(captureListener, Visualizer.getMaxCaptureRate(),
                true, true);

        // Enabled Visualizer and disable when we're done with the stream
        mVisualizer.setEnabled(true);
    }*/

    /**
     * Call to release the resources used by VisualizerView. Like with the
     * MediaPlayer it is good practice to call this method
     */
    /*public void release() {
        mVisualizer.release();
    }
    
    public void pause() {
        mVisualizer.setEnabled(false);
    }
    
    public void stop() {
        mStopped = true;
        mVisualizer.setEnabled(false);
    }
    
    public void resume() {
        if (mStopped) {
            mStopped = false;
            mWaveDataBuffer.clear();
            mWaveDataFFTBuffer.clear();
        }
        mVisualizer.setEnabled(true);
    }*/

    /**
     * Pass data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
     * 
     * @param bytes
     */
    /*public void updateVisualizer(byte[] bytes) {
        byte[] data = WaveformHelper.convertData(bytes, RATE_X);
        final int length = data.length;
        final ArrayList<Byte> waveDataBuffer = mWaveDataBuffer;
        for (int i = 0; i < length; i ++) {
            waveDataBuffer.add(data[i]);
        }
        
        final int expectedDataNum = getWidth() / WaveformHelper.DIVIDER;
        final int unwanted = waveDataBuffer.size() - expectedDataNum;
        for (int i = 0; i < unwanted; i ++) {
            waveDataBuffer.remove(0);
        }
        invalidate();
    }*/

    /**
     * Pass FFT data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
     * 
     * @param bytes
     */
    /*public void updateVisualizerFFT(byte[] bytes) {
        byte[] data = WaveformHelper.convertData(bytes, RATE_X);
        final int length = data.length;
        final ArrayList<Byte> waveDataFFTBuffer = mWaveDataFFTBuffer;
        for (int i = 0; i < length; i ++) {
            waveDataFFTBuffer.add(data[i]);
        }
        
        final int expectedDataNum = getWidth() / WaveformHelper.DIVIDER;
        final int unwanted = waveDataFFTBuffer.size() - expectedDataNum;
        for (int i = 0; i < unwanted; i ++) {
            waveDataFFTBuffer.remove(0);
        }
        invalidate();
    }*/
}
