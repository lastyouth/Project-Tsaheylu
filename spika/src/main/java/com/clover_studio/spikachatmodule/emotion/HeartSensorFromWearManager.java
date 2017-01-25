package com.clover_studio.spikachatmodule.emotion;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by sbh on 2017-01-24.
 */

public class HeartSensorFromWearManager {
    private static final String TAG = "HSMFromWearManager";
    private Context context;
    private ExecutorService executorService;
    private GoogleApiClient googleApiClient;
    private static HeartSensorFromWearManager instance;
    private static final String START_MEASUREMENT = "/start";
    private static final String STOP_MEASUREMENT = "/stop";
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    public class HeartSensorDataFromWear
    {
        public int accuracy;
        public long timestamp;
        public float heartrate;
    }

    private Queue<HeartSensorDataFromWear> mHRQueue;

    public void addSensorDataToQueue(float[] hr, long timestamp, int accuracy)
    {
        HeartSensorDataFromWear rawdata = new HeartSensorDataFromWear();

        rawdata.heartrate = hr[0];
        rawdata.timestamp = timestamp;
        rawdata.accuracy = accuracy;

        if(mHRQueue != null)
        {
            mHRQueue.add(rawdata);

            Log.i(TAG,"Queue : "+mHRQueue);
        }
        else
        {
            Log.e(TAG,"mHRQueue is NULL");
        }
    }



    public static synchronized HeartSensorFromWearManager getInstance(Context context) {
        if (instance == null) {
            instance = new HeartSensorFromWearManager(context.getApplicationContext());
        }

        return instance;
    }

    private HeartSensorFromWearManager(Context context) {
        context = context;

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        executorService = Executors.newCachedThreadPool();

        mHRQueue = new LinkedList<HeartSensorDataFromWear>();
    }
    public void startMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(START_MEASUREMENT);
            }
        });
    }

    public void stopMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(STOP_MEASUREMENT);
            }
        });
    }
    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    private void controlMeasurementInBackground(final String path) {
        if (validateConnection()) {
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();

            Log.d(TAG, "Sending to nodes: " + nodes.size());

            for (Node node : nodes) {
                Log.i(TAG, "add node " + node.getDisplayName());
                Wearable.MessageApi.sendMessage(
                        googleApiClient, node.getId(), path, null
                ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG, "controlMeasurementInBackground(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                    }
                });
            }
        } else {
            Log.w(TAG, "No connection possible");
        }
    }
}
