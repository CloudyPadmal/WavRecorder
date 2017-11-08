package lk.padmal.audiorecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String mFileName = null;

    private MediaPlayer mPlayer = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private List<String> samplingRates;

    private int samplingRate;

    private FloatingActionButton recordButton;
    private EditText customFileName;
    private Spinner samplingRatesSpinner;
    private RecyclerView recordedFilesList;
    private RecordedAudioListAdapter recordedAudioListAdapter;
    private List<String> fileList, uniqueList;

    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        initiateEditText();
        initiateRecordButton();
        initiateSamplingRateSpinner();
        initiateRecyclerView();
        updateRecordedFilesList();
    }

    private void initiateEditText() {
        customFileName = (EditText) findViewById(R.id.file_name_edit_text);
    }

    private void initiateRecordButton() {
        recordButton = (FloatingActionButton) findViewById(R.id.fab);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = !recording;
                customFileName.setEnabled(!recording);
                recordButton.setImageResource(recording ? R.drawable.ic_stop_recording : R.drawable.ic_start_recording);
                // Record to the external cache directory for visibility
                mFileName = getExternalCacheDir().getAbsolutePath();
                String customName = customFileName.getText().toString();
                mFileName +=  "/" + (customName.isEmpty() ? String.valueOf(System.currentTimeMillis()) : customName);
                mFileName += "_" + samplingRate + ".3gp";
                // Handle service
                Intent recorder = new Intent(MainActivity.this, RecorderService.class);
                recorder.putExtra("SAMPLE_RATE", samplingRate);
                recorder.putExtra("FILE_NAME", mFileName);
                recorder.putExtra("RECORD", recording);
                if (recording) {
                    recorder.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(recorder);
                } else {
                    stopService(recorder);
                    fileList.clear();
                    uniqueList.clear();
                    updateRecordedFilesList();
                }
            }
        });
    }

    private void initiateSamplingRateSpinner() {
        // Initiate view
        samplingRatesSpinner = (Spinner) findViewById(R.id.spinner_sampling_rate);
        // List of known sample rates; Add more to this if necessary..
        samplingRates = new ArrayList<>();
        samplingRates.add("44100");
        samplingRates.add("48000");
        samplingRates.add("8000");
        samplingRate = 44100;
        // Creates adapter to set to the spinner
        ArrayAdapter<String> samplingRatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, samplingRates);
        samplingRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        samplingRatesSpinner.setAdapter(samplingRatesAdapter);
        // Assign sampling rate according to the selected value
        samplingRatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                samplingRate = Integer.parseInt(samplingRates.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {/**/}
        });
    }

    private void initiateRecyclerView() {
        fileList = new ArrayList<>();
        uniqueList = new ArrayList<>();
        recordedFilesList = (RecyclerView) findViewById(R.id.recordings_recycler_list);
        recordedAudioListAdapter = new RecordedAudioListAdapter(fileList, uniqueList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recordedFilesList.setLayoutManager(mLayoutManager);
        recordedFilesList.setItemAnimator(new DefaultItemAnimator());
        recordedFilesList.setAdapter(recordedAudioListAdapter);
    }

    private void updateRecordedFilesList() {
        File mainDirectory = new File(getExternalCacheDir().getAbsolutePath());
        if (mainDirectory != null) {
            File[] recordingFileList = mainDirectory.listFiles();
            if (recordingFileList != null) {
                for (File file : recordingFileList) {
                    uniqueList.add(file.getAbsolutePath());
                    fileList.add(file.getName() + "_" + file.length() / 1024 + "kB");
                }
                recordedAudioListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
