package lk.padmal.audiorecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Padmal on 11/1/17.
 */

public class RecorderService extends Service {

    private MediaRecorder mRecorder = null;

    // Constants
    private int SAMPLE_RATE;
    private String FILE_NAME;
    private boolean RECORD;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            SAMPLE_RATE = intent.getIntExtra("SAMPLE_RATE", 44100);
            FILE_NAME = intent.getStringExtra("FILE_NAME");
            RECORD = intent.getBooleanExtra("RECORD", true);
            // Start/Stop recording
            if (RECORD) {
                startRecording();
            } else {
                stopRecording();
            }
        } catch (NullPointerException e) {
            Log.d("ABCD", "Data missing");
        }
        return Service.START_STICKY;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setAudioSamplingRate(SAMPLE_RATE);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(FILE_NAME);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e("ABCD", "prepare() failed --> " + e.getMessage());
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
        }
        mRecorder = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // Release recorder from main thread
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }
}
