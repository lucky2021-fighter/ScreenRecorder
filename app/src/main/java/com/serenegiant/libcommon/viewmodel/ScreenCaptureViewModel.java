package com.serenegiant.libcommon.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.serenegiant.service.ScreenRecorderService;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ScreenCaptureViewModel extends AndroidViewModel {
    private static final boolean DEBUG = true;
    private static final String TAG = ScreenCaptureViewModel.class.getSimpleName();
    public boolean isRecording = false;
    //val isChecked by lazy { MutableLiveData(isRecording) }
    public MutableLiveData<Boolean> isChecked = new MutableLiveData<Boolean>();
    public boolean isReceived = false;

    public ScreenCaptureViewModel(@NonNull @NotNull Application application) {
        super(application);
        init();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (DEBUG) Log.v(TAG, "onCleared:");
        Context context = getApplication();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
    }

    private void init() {
        if (DEBUG) Log.v(TAG, "init()");
        Context context = getApplication();
        IntentFilter filter = new IntentFilter(ScreenRecorderService.ACTION_QUERY_STATUS_RESULT);
        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, filter);
        queryRecordingStatus();
    }

    public void startScreenCapture(Intent data) {
        if (DEBUG) Log.v(TAG, "startScreenCapture: isRecording=" + isRecording + ", data=" + data);
        if (!isRecording) {
            isRecording = true;
            Context context = getApplication();
            Intent intent = new Intent(context, ScreenRecorderService.class);
            intent.setAction(ScreenRecorderService.ACTION_START);
            intent.putExtras(data);
            context.startService(intent);
        }
    }

    public void stopScreenCapture() {
        if (isReceived && isRecording) {
            if (DEBUG) Log.v(TAG, "stopScreenCapture:");
            Context context = getApplication();
            Intent intent = new Intent(context, ScreenRecorderService.class);
            intent.setAction(ScreenRecorderService.ACTION_STOP);
            context.startService(intent);
        }
    }

    public String getRecordingLabel() {
        LiveData<String> result = Transformations.map(isChecked, value -> {
            if (value) {
                return "stop";
            } else {
                return "start";
            }
        });
        return result.getValue();
    }

    public void queryRecordingStatus() {
        if (DEBUG) Log.v(TAG, "queryRecording:");
        Context context = getApplication();
        Intent intent = new Intent(context, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_QUERY_STATUS);
        context.startService(intent);
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DEBUG) Log.v(TAG, "onReceive: action=" + action);
            if (ScreenRecorderService.ACTION_QUERY_STATUS_RESULT.equals(action)) {
                boolean recording = intent.getBooleanExtra(
                        ScreenRecorderService.EXTRA_QUERY_RESULT_RECORDING, false);
                isReceived = true;
                if (isRecording != recording) {
                    isRecording = recording;
                    isChecked.setValue(recording);
                }
            }
        }
    };
}
