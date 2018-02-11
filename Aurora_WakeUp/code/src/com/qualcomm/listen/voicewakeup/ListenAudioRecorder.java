/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;


public class ListenAudioRecorder
{
    private static final String TAG = "ListenLog.ListenAudioRecorder";

    public static ListenAudioRecorder getInstance() {
        return new ListenAudioRecorder(
                AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    /**
     * INITIALIZING : recorder is in the process of initializing
     * READY : recorder has been initialized, but not yet started
     * RECORDING : recorder is in the process of recording
     * ERROR : recorder needs to be reinitialized
     * STOPPED: recorder needs to be reset
     */
    public enum State {INITIALIZING, READY, RECORDING, ERROR, STOPPED};

    // The interval for outputting the recorded samples to the file
    private static final int TIMER_INTERVAL = 120;

    // The recorder
    private AudioRecord     audioRecorder = null;

    // Output file path
    //private String          filePath = Global.RECORDINGS_FILE_PATH;

    // Temp output file path
    private String          tempFilePath = Global.RECORDINGS_TEMP_FILE_PATH;

    // Recorder state. See State enum.
    private State           state;

    private final short              numChannels = 1; // mono
    private int                      sampleRate;
    private final int                bitsPerSample = 16; // in bits
    private int                      bufferSize;

    // Number of frames written to file on each output
    private int                      framePeriod;

    private Thread recordingThread = null;

    /**
     *
     * Instantiates a new recorder.
     * If there are errors, no exception is thrown, but the state is set to ERROR.
     *
     */
    public ListenAudioRecorder(int inAudioSource, int inSampleRate, int inChannelConfig, int inAudioFormat)
    {
        Log.v(TAG, "ListenAudioRecorder constructor");
        try
        {
            sampleRate   = inSampleRate;
            framePeriod = inSampleRate * TIMER_INTERVAL / 1000;
            bufferSize = framePeriod * 2 * bitsPerSample * numChannels / 8;
            if (bufferSize < AudioRecord.getMinBufferSize(inSampleRate, inChannelConfig, inAudioFormat))
            {
                // Check to make sure buffer size is not smaller than the smallest allowed
                bufferSize = AudioRecord.getMinBufferSize(inSampleRate, inChannelConfig, inAudioFormat);
                // Set frame period and timer interval accordingly
                framePeriod = bufferSize / ( 2 * bitsPerSample * numChannels / 8 );
                Log.v(TAG, "Increasing buffer size to " + Integer.toString(bufferSize));
            }

            audioRecorder = new AudioRecord(inAudioSource, inSampleRate, inChannelConfig, inAudioFormat, bufferSize);
            Log.v(TAG, "ListenAudioRecorder constructor: constructed");

            if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new Exception("AudioRecord initialization failed");
            }
            state = State.INITIALIZING;
            Log.v(TAG, "ListenAudioRecorder constructor: finished");
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(TAG, e.getMessage());
            } else {
                Log.e(TAG, "Unknown error occured while initializing recording");
            }
            state = State.ERROR;
        }
    }


    /**
     *
     * Returns the state of the recorder.
     *
     * @return recorder state
     *
     */
    public State getState()
    {
        return state;
    }

    /**
     *
     * Starts recording and sets the state of the recorder to RECORDING.
     *
     */
    public void start()
    {
        Log.v(TAG, "start");
        Log.v(TAG, "start: in state = READY");
        audioRecorder.startRecording();
        Log.v(TAG, "start: started recording, audioRecorder state= " + audioRecorder.getRecordingState());
        state = State.RECORDING;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }


    // Wav output is used for ease of debugging recordings
    private void writeAudioDataToFile(){
        Log.v(TAG, "writeAudioDataToFile");
        String filename = tempFilePath;
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;

        if(os != null){
            while(state == State.RECORDING){
                byte data[] = new byte[bufferSize];
                read = audioRecorder.read(data, 0, bufferSize);
                Log.v(TAG, "writeAudioDataToFile: read = " + read);
                if(AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * Stops recording, releases recorder, and sets the state of the recorder to STOPPED.
     * Finalizes the wave file.
     *
     */
    public void stop()
    {
        Log.v(TAG, "stop");
        if (state == State.RECORDING)
        {
            state = State.STOPPED;
            audioRecorder.stop();
            audioRecorder.release();
            Log.v(TAG, "release() returned");
            String filePath = Global.getInstance().getLastUserRecordingFilePath();
            copyWaveFile(tempFilePath, filePath);
            deleteTempFile();
        } else {
            Log.v(TAG, "stop() called but not recording");
            state = State.ERROR;
        }
    }

    private void copyWaveFile(String inFilename, String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 44;
        long longSampleRate = sampleRate;
        long byteRate = bitsPerSample * sampleRate * numChannels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 44;

            createAndWriteWavFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, numChannels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAndWriteWavFileHeader(
            FileOutputStream fileOutputStream, long dataLength,
            long totalDataLength, long blocksPerSecond, int numChannels,
            long dataRate) throws IOException {

        byte[] waveFileFirst44Bytes = new byte[44];

        waveFileFirst44Bytes[0] = 'R';  // chunk ID = "RIFF"
        waveFileFirst44Bytes[1] = 'I';
        waveFileFirst44Bytes[2] = 'F';
        waveFileFirst44Bytes[3] = 'F';
        waveFileFirst44Bytes[4] = (byte) (totalDataLength & 0xff); // chunk size
        waveFileFirst44Bytes[5] = (byte) ((totalDataLength >> 8) & 0xff);
        waveFileFirst44Bytes[6] = (byte) ((totalDataLength >> 16) & 0xff);
        waveFileFirst44Bytes[7] = (byte) ((totalDataLength >> 24) & 0xff);
        waveFileFirst44Bytes[8] = 'W'; // wave ID = "WAVE"
        waveFileFirst44Bytes[9] = 'A';
        waveFileFirst44Bytes[10] = 'V';
        waveFileFirst44Bytes[11] = 'E';
        waveFileFirst44Bytes[12] = 'f'; // chunk ID = "fmt "
        waveFileFirst44Bytes[13] = 'm';
        waveFileFirst44Bytes[14] = 't';
        waveFileFirst44Bytes[15] = ' ';
        waveFileFirst44Bytes[16] = 16;  // chunk size = 16
        waveFileFirst44Bytes[17] = 0;
        waveFileFirst44Bytes[18] = 0;
        waveFileFirst44Bytes[19] = 0;
        waveFileFirst44Bytes[20] = 1; // format code
        waveFileFirst44Bytes[21] = 0;
        waveFileFirst44Bytes[22] = (byte) numChannels;  // number of interleaved channels
        waveFileFirst44Bytes[23] = 0;
        waveFileFirst44Bytes[24] = (byte) (blocksPerSecond & 0xff); // Sampling rate (blocks/sec)
        waveFileFirst44Bytes[25] = (byte) ((blocksPerSecond >> 8) & 0xff);
        waveFileFirst44Bytes[26] = (byte) ((blocksPerSecond >> 16) & 0xff);
        waveFileFirst44Bytes[27] = (byte) ((blocksPerSecond >> 24) & 0xff);
        waveFileFirst44Bytes[28] = (byte) (dataRate & 0xff); // Data rate
        waveFileFirst44Bytes[29] = (byte) ((dataRate >> 8) & 0xff);
        waveFileFirst44Bytes[30] = (byte) ((dataRate >> 16) & 0xff);
        waveFileFirst44Bytes[31] = (byte) ((dataRate >> 24) & 0xff);
        waveFileFirst44Bytes[32] = (byte) (numChannels * bitsPerSample / 8); // Data block size 8 b/c word-aligned
        waveFileFirst44Bytes[33] = 0;
        waveFileFirst44Bytes[34] = bitsPerSample;
        waveFileFirst44Bytes[35] = 0;
        waveFileFirst44Bytes[36] = 'd'; // chunk ID = "data"
        waveFileFirst44Bytes[37] = 'a';
        waveFileFirst44Bytes[38] = 't';
        waveFileFirst44Bytes[39] = 'a';
        waveFileFirst44Bytes[40] = (byte) (dataLength & 0xff);
        waveFileFirst44Bytes[41] = (byte) ((dataLength >> 8) & 0xff);
        waveFileFirst44Bytes[42] = (byte) ((dataLength >> 16) & 0xff);
        waveFileFirst44Bytes[43] = (byte) ((dataLength >> 24) & 0xff);

        fileOutputStream.write(waveFileFirst44Bytes, 0, 44);
    }

    private void deleteTempFile() {
        File file = new File(tempFilePath);
        file.delete();
    }
}
