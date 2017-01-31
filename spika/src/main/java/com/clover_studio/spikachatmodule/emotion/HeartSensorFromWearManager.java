package com.clover_studio.spikachatmodule.emotion;

import android.content.Context;
import android.util.Log;

import com.clover_studio.spikachatmodule.utils.Const;
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

//    private Queue<HeartSensorDataFromWear> mHRQueue;
    //mckang
    private String availableFlag;
    private LinkedList<HeartSensorDataFromWear> mHRList;
    //Queue Size
    private int queueSize;
    private LinkedList<Float> HRList;
    private LinkedList<Float> averageHRList;
    private LinkedList<Float> maxOfAverageHRList;
    private LinkedList<Float> minOfAverageHRList;


    public void addSensorDataToQueue(float[] hr, long timestamp, int accuracy)
    {
        HeartSensorDataFromWear rawdata = new HeartSensorDataFromWear();


        rawdata.heartrate = hr[0];
        rawdata.timestamp = timestamp;
        rawdata.accuracy = accuracy;

        if(mHRList != null)
        {
            if(mHRList.size() != queueSize){
                mHRList.add(rawdata);
                HRList.add(rawdata.heartrate);
                Log.e(TAG,"New heartrate data is added");
            }else{
                setCurrentAverage();
                HRList.removeFirst();
                HRList.add(rawdata.heartrate);
                mHRList.removeFirst();
                mHRList.add(rawdata);
                Log.e(TAG,"New heartrate data is added");
            }

        }
        else
        {
            Log.e(TAG,"mHRList is NULL");
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

//        mHRQueue = new LinkedList<HeartSensorDataFromWear>();
        //mckang
        mHRList = new LinkedList<HeartSensorDataFromWear>();
        queueSize = Const.Emotion.MAX_QUEUED_DATA_FOR_HRV;
        HRList = new LinkedList<Float>();
        averageHRList = new LinkedList<Float>();
        maxOfAverageHRList = new LinkedList<Float>();
        minOfAverageHRList = new LinkedList<Float>();
        availableFlag = "NO";


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

    //mckang
    public void setMaxAndMinOfAverageHRList(){
        float maxHR = 0;
        float minHR = 100000;
        if(averageHRList.size() != 0 && averageHRList.size() == 10){

            for(int i = 0; i < averageHRList.size(); i++){
                //Find Max
                if(maxHR < averageHRList.get(i)){
                    maxHR = averageHRList.get(i);
                }
                //Find Min
                if(minHR > averageHRList.get(i)){
                    minHR = averageHRList.get(i);
                }
            }
            //Add Max data
            if(maxOfAverageHRList != null){
                if(maxHR != 0) {
                    if (maxOfAverageHRList.size() != queueSize) {
                        maxOfAverageHRList.add(maxHR);
                    } else {
                        maxOfAverageHRList.removeFirst();
                        maxOfAverageHRList.add(maxHR);
                    }
                    Log.i(Const.TAG, "New maxOfAverageHR data is added");
                }else{
                    Log.i(Const.TAG, "Unavailable Max HR data");
                }
            }else{
                Log.i(Const.TAG, "maxOfAverageHRList is null");
            }
            //Add Min Data
            if(minOfAverageHRList != null){
                if(minHR != 100000) {
                    if (minOfAverageHRList.size() != queueSize) {
                        minOfAverageHRList.add(minHR);
                    } else {
                        minOfAverageHRList.removeFirst();
                        minOfAverageHRList.add(minHR);
                    }
                    Log.i(Const.TAG, "New minOfAverageHR data is added");
                }else{
                    Log.i(Const.TAG, "Unavailable Min HR data");
                }
            }else{
                Log.i(Const.TAG, "minOfAverageHRList is null");
            }

        }
    }

    public void setCurrentAverage(){
        float averageHR = 0;
        float sumOfHR = 0;
        if(mHRList.size() != 0 && mHRList.size() == 10){

            for (int i = 0; i < mHRList.size(); i++) {
                sumOfHR += mHRList.get(i).heartrate;
            }
            averageHR = sumOfHR / mHRList.size();

            if(averageHRList != null){
                if(averageHRList.size() != queueSize){
                    averageHRList.add(averageHR);
                }else{
                    //set Max Average HR
                    setMaxAndMinOfAverageHRList();
                    averageHRList.removeFirst();
                    averageHRList.add(averageHR);
                }
                Log.i(Const.TAG, "New averageHR data is added");
            }else{
                Log.i(Const.TAG, "AverageHRList is null");
            }

        }
    }
    // mckang
    // State is four (unavailable, positive, neutral, and negative),
    public String getCurrentState(){
        String state = new String("unavailable");

        float maxHR = 0;
        float minHR = 0;
        float lastHR = 0;

//        Log.i("Average ")

        if(maxOfAverageHRList.size() != 0 && minOfAverageHRList.size() != 0){

            maxHR = maxOfAverageHRList.getFirst();
            minHR = minOfAverageHRList.getFirst();
            lastHR = mHRList.getLast().heartrate;

            Log.i(Const.TAG, "Average (last) " + averageHRList.getLast() + " Max HR : "+ maxHR + " Min HR : "+ minHR + " Last HR : "+lastHR);
            if(maxHR < lastHR){
                state = "negative";
            }else if (minHR > lastHR){
                state = "positive";
            }else{
                state = "neutral";
            }
        }

        return state;
    }

    public String getAvailableFlag(){
        return availableFlag;
    }
    //mckang
    public LinkedList<HeartSensorDataFromWear> getmHRList(){
        return mHRList;
    }

    public LinkedList<Float> getHRList(){
        return HRList;
    }


    public LinkedList<Float> getAverageHRList(){
        return averageHRList;
    }

    public LinkedList<Float> getMaxOfAverageHRList(){
        return maxOfAverageHRList;
    }

    public LinkedList<Float> getMinOfAverageHRList(){
        return minOfAverageHRList;
    }







}
