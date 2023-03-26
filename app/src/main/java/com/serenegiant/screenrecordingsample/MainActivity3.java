package com.serenegiant.screenrecordingsample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.serenegiant.service.ScreenRecorderService;

import java.lang.ref.WeakReference;

import androidx.annotation.RequiresApi;

public final class MainActivity3 extends Activity {
    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;

    private ToggleButton mRecordButton;
    private ToggleButton mPauseButton;
    private MyBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate:");
        setContentView(R.layout.activity_main3);
        mRecordButton = (ToggleButton) findViewById(R.id.record_button);
        mPauseButton = (ToggleButton) findViewById(R.id.pause_button);
        updateRecording(false, false);
        if (mReceiver == null)
            mReceiver = new MyBroadcastReceiver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume:");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScreenRecorderService.ACTION_QUERY_STATUS_RESULT);
        registerReceiver(mReceiver, intentFilter);
        queryRecordingStatus();
    }

    @Override
    protected void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:");
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (DEBUG) Log.v(TAG, "onActivityResult:resultCode=" + resultCode + ",data=" + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SCREEN_CAPTURE == requestCode) {
            if (resultCode != Activity.RESULT_OK) {
                // when no permission
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                return;
            }
            startScreenRecorder(resultCode, data);
        }
    }

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.record_button:
                    if (isChecked) {
                        final MediaProjectionManager manager
                                = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                        final Intent permissionIntent = manager.createScreenCaptureIntent();
                        startActivityForResult(permissionIntent, REQUEST_CODE_SCREEN_CAPTURE);
                    } else {
                        final Intent intent = new Intent(MainActivity3.this, ScreenRecorderService.class);
                        intent.setAction(ScreenRecorderService.ACTION_STOP);
                        startService(intent);
                    }
                    break;
                case R.id.pause_button:
                    if (isChecked) {
                        final Intent intent = new Intent(MainActivity3.this, ScreenRecorderService.class);
                        intent.setAction(ScreenRecorderService.ACTION_PAUSE);
                        startService(intent);
                    } else {
                        final Intent intent = new Intent(MainActivity3.this, ScreenRecorderService.class);
                        intent.setAction(ScreenRecorderService.ACTION_RESUME);
                        startService(intent);
                    }
                    break;
            }
        }
    };

    private void queryRecordingStatus() {
        if (DEBUG) Log.v(TAG, "queryRecording:");
        final Intent intent = new Intent(this, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_QUERY_STATUS);
        startService(intent);
    }

    private void startScreenRecorder(final int resultCode, final Intent data) {
        final Intent intent = new Intent(this, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_START);
        intent.putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, resultCode);
        intent.putExtras(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        //moveTaskToBack(true);
    }

    public void startScreenCapture(Intent data) {
        if (DEBUG) Log.v(TAG, "startScreenCapture: data=" + data);
        Context context = getApplication();
        Intent intent = new Intent(context, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_START);
        intent.putExtras(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void stopScreenCapture() {
        if (DEBUG) Log.v(TAG, "stopScreenCapture:");
        Context context = getApplication();
        Intent intent = new Intent(context, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_STOP);
        context.startService(intent);
    }

    private void updateRecording(final boolean isRecording, final boolean isPausing) {
        if (DEBUG)
            Log.v(TAG, "updateRecording:isRecording=" + isRecording + ",isPausing=" + isPausing);
        mRecordButton.setOnCheckedChangeListener(null);
        mPauseButton.setOnCheckedChangeListener(null);
        try {
            mRecordButton.setChecked(isRecording);
            mPauseButton.setEnabled(isRecording);
            mPauseButton.setChecked(isPausing);
        } finally {
            mRecordButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
            mPauseButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
        }
    }

    private static final class MyBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<MainActivity3> mWeakParent;

        public MyBroadcastReceiver(final MainActivity3 parent) {
            mWeakParent = new WeakReference<>(parent);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (DEBUG) Log.v(TAG, "onReceive:" + intent);
            final String action = intent.getAction();
            if (ScreenRecorderService.ACTION_QUERY_STATUS_RESULT.equals(action)) {
                final boolean isRecording = intent.getBooleanExtra(ScreenRecorderService.EXTRA_QUERY_RESULT_RECORDING, false);
                final boolean isPausing = intent.getBooleanExtra(ScreenRecorderService.EXTRA_QUERY_RESULT_PAUSING, false);
                final MainActivity3 parent = mWeakParent.get();
                if (parent != null) {
                    parent.updateRecording(isRecording, isPausing);
                }
            }
        }
    }
}
