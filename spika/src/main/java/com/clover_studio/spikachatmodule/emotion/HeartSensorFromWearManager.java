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
    //mckang
    private int dataCount;
    //mckang
    public float allAverage;
    public float allSum;
    public int allCount;
    public float allMax;
    public float allMin;

    private String preState;

//    public float upperMax;
//    public float upperAverage;
//    public float upperMin;
//    public float middleMax;
//    public float middleAverage;
//    public float middleMin;
//    public float lowerMax;
//    public float lowerAverage;
//    public float lowerMin;

    public LinkedList<Float> negativeValues;
    public LinkedList<Float> neutralValues;
    public LinkedList<Float> positiveValues;

    public float maxNegative;
    public float minNegative;
    public float maxNeutral;
    public float minNeutral;
    public float maxPositive;
    public float minPositive;

    public float maxNeutralSum;
    public float minNeutralSum;
    private float maxNeutralCount;
    private float minNeutralCount;

    public float maxNeutralAverage;
    public float minNeutralAverage;

    //JesungKim 20170223
    public static float avgHRValue = -1;
    public static float sumHRValue = -1;
    public static float prevHRValue = -1;
    public static float avgHRCnt = -1;
    public static final float STATE_CHANGE_VALUE = 3;
    public static int incCnt = -1;
    public static int decCnt = -1;
    public static String emotionState = "neutral";
    public static final int STATE_CHANGE_COUNTER = 3;



    public void addSensorDataToQueue(float[] hr, long timestamp, int accuracy)
    {
        HeartSensorDataFromWear rawdata = new HeartSensorDataFromWear();


        rawdata.heartrate = hr[0];
        rawdata.timestamp = timestamp;
        rawdata.accuracy = accuracy;

        if(mHRList != null)
        {
            //For Logging
            allCount++;
            allSum += rawdata.heartrate;
            allAverage = allSum / allCount;
            if( allMax < rawdata.heartrate){
                allMax = rawdata.heartrate;
            }
            if(allMin > rawdata.heartrate){
                allMin = rawdata.heartrate;
            }
            ////////////////////

            if(mHRList.size() != queueSize){
                mHRList.add(rawdata);
                HRList.add(rawdata.heartrate);
                Log.e(TAG,"New heartrate data is added");
            }else{
                if(dataCount == queueSize) {
                    setCurrentAverage();
                    dataCount = 0;
                }
                HRList.removeFirst();
                HRList.add(rawdata.heartrate);
                mHRList.removeFirst();
                mHRList.add(rawdata);
                Log.e(TAG,"New heartrate data is added");
            }

            dataCount++;

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
        dataCount = 0;
        HRList = new LinkedList<Float>();
        averageHRList = new LinkedList<Float>();
        maxOfAverageHRList = new LinkedList<Float>();
        minOfAverageHRList = new LinkedList<Float>();
        availableFlag = "NO";

        allSum = 0;
        allMax = 0;
        allCount = 0;
        allMin = 1000;

        preState = new String("unavailable");

//        upperMax = 0;
//        upperMin = 1000;
//        middleMax = 0;
//        middleMin = 1000;
//        lowerMax = 0;
//        lowerMin = 1000;
//
//        upperAverage = 0;
//        middleAverage = 0;
//        lowerAverage = 0;
        negativeValues = new LinkedList<>();
        neutralValues = new LinkedList<>();
        positiveValues = new LinkedList<>();

        maxNegative = 0;
        minNegative = 10000;
        maxNeutral = 0;
        minNeutral = 10000;
        maxPositive = 0;
        minPositive = 10000;


        maxNeutralSum = 0;
        minNeutralSum = 0;
        maxNeutralCount = 0;
        minNeutralCount = 0;

        maxNeutralAverage = 0;
        minNeutralAverage = 0;

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
    //new max and min
    public void setMaxAndMinOfAverageHRList(){
        float maxHR = 0;
        float minHR = 100000;
        if(mHRList.size() != 0 && mHRList.size() == queueSize){

            for(int i = 0; i < mHRList.size()  ; i++){
                //Find Max
                if(maxHR < mHRList.get(i).heartrate){
                    maxHR = mHRList.get(i).heartrate;
                }
                //Find Min
                if(minHR > mHRList.get(i).heartrate){
                    minHR = mHRList.get(i).heartrate;
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

//    //mckang
//    public void setMaxAndMinOfAverageHRList(){
//        float maxHR = 0;
//        float minHR = 100000;
//        if(averageHRList.size() != 0 && averageHRList.size() == 10){
//
//            for(int i = 0; i < averageHRList.size(); i++){
//                //Find Max
//                if(maxHR < averageHRList.get(i)){
//                    maxHR = averageHRList.get(i);
//                }
//                //Find Min
//                if(minHR > averageHRList.get(i)){
//                    minHR = averageHRList.get(i);
//                }
//            }
//            //Add Max data
//                if(maxOfAverageHRList != null){
//                if(maxHR != 0) {
//                    if (maxOfAverageHRList.size() != queueSize) {
//                        maxOfAverageHRList.add(maxHR);
//                    } else {
//                        maxOfAverageHRList.removeFirst();
//                        maxOfAverageHRList.add(maxHR);
//                    }
//                    Log.i(Const.TAG, "New maxOfAverageHR data is added");
//                }else{
//                    Log.i(Const.TAG, "Unavailable Max HR data");
//                }
//            }else{
//                Log.i(Const.TAG, "maxOfAverageHRList is null");
//            }
//            //Add Min Data
//            if(minOfAverageHRList != null){
//                if(minHR != 100000) {
//                    if (minOfAverageHRList.size() != queueSize) {
//                        minOfAverageHRList.add(minHR);
//                    } else {
//                        minOfAverageHRList.removeFirst();
//                        minOfAverageHRList.add(minHR);
//                    }
//                    Log.i(Const.TAG, "New minOfAverageHR data is added");
//                }else{
//                    Log.i(Const.TAG, "Unavailable Min HR data");
//                }
//            }else{
//                Log.i(Const.TAG, "minOfAverageHRList is null");
//            }
//
//        }
//    }

    public void setCurrentAverage(){
        float averageHR = 0;
        float sumOfHR = 0;
        if(mHRList.size() != 0 && mHRList.size() == queueSize){

            for (int i = 0; i < mHRList.size() ; i++) {
                sumOfHR += mHRList.get(i).heartrate;
            }
            averageHR = sumOfHR / (mHRList.size());

            if(averageHRList != null){
                if(averageHRList.size() != queueSize){
                    //set Max Average HR
                    setMaxAndMinOfAverageHRList();
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
    //mckang for detecting lower, middle, and upper state
    private String getChangeState( LinkedList<Float> data){
        String state = new String("middle");

        LinkedList<Float> tempData = new LinkedList<>(data);

        if(tempData.getFirst() < tempData.getLast()){
            if((tempData.getLast() - tempData.getFirst()) > 1 ){
                for(int i = 1; i < tempData.size()-1; i++){
                    if(tempData.get(i) > tempData.getLast()){
                        state = "middle";
                        break;
                    }else{
                        state = "upper";
                        break;
                    }
                }
            }else{
                state = "middle";
            }
        }
        else if(tempData.getFirst() > tempData.getLast()){
            if((tempData.getFirst() - tempData.getLast()) > 1){
                for(int i = 1 ; i< tempData.size()-1; i++) {
                    if (tempData.get(i) < tempData.getLast()) {
                        state = "middle";
                        break;
                    } else {
                        state = "lower";
                        break;
                    }
                }
            }else{
                state = "middle";
            }
        }
        else if(tempData.getFirst() == tempData.getLast()){
            state = "middle";
        }

        return state;
    }

    //public static float prevHRValue = -1;
    //public static int incCnt = -1;
    //public static int decCnt = -1;
    //public static String emotionState = "neutral";
    //public static final int STATE_CHANGE_COUNTER = 3;


    // mckang
    // State is four (unavailable, positive, neutral, and negative),
    public String getCurrentState(){
        String state = new String("unavailable");

        float maxHR_F = 0;
        float minHR_F = 0;
        float maxHR_L = 0;
        float minHR_L = 0;
        float lastHR = 0;

        float averageHR_F = 0;
        float averageHR_L = 0;

        String changeState = new String();

        String initialState = new String();

        lastHR = mHRList.getLast().heartrate;

        //JesungKim 20170222
        Log.e(Const.TAG,"JesungKim --  HR Score: (" + lastHR + ")");
        if (prevHRValue == -1) {
            prevHRValue = lastHR;
            incCnt = 0;
            decCnt = 0;
            avgHRCnt = 0;
            avgHRValue = 0;
            sumHRValue = 0;
            state = emotionState;
        }
        else {

            avgHRCnt++;
            sumHRValue = sumHRValue + lastHR;
            avgHRValue = sumHRValue/avgHRCnt;
            Log.e(Const.TAG,"JesungKim --  AVG HR Score: (" + avgHRValue + ")");

            if ( (lastHR-avgHRValue) >= STATE_CHANGE_VALUE) {
                state = "negative";
            }

            else {
                state = "neutral";
            }

            /*
            if (prevHRValue < lastHR) {
                incCnt++;
                decCnt = 0;
            }
            else if (prevHRValue > lastHR) {
                decCnt++;
                incCnt = 0;
            }

            if (incCnt >= STATE_CHANGE_COUNTER){
                state = "negative";
                emotionState = "negative";
            }
            else if (decCnt >= STATE_CHANGE_COUNTER){
                state = "neutral";
                emotionState = "neutral";
            }
            else {
                state = emotionState;
            }
            */

        }

        //state = "negative";
        //state = "positive";
        //state = "neutral";

        /*
//      if(maxOfAverageHRList.size() != 0 && minOfAverageHRList.size() != 0){
        if(maxOfAverageHRList.size() != 0 && minOfAverageHRList.size() != 0
                && maxOfAverageHRList.size() == queueSize && minOfAverageHRList.size() == queueSize){

            maxHR_F = maxOfAverageHRList.getFirst();
            minHR_F = minOfAverageHRList.getFirst();
            maxHR_L = maxOfAverageHRList.getLast();
            minHR_L = maxOfAverageHRList.getLast();
            lastHR = mHRList.getLast().heartrate;
            averageHR_F = averageHRList.getFirst();
            averageHR_L = averageHRList.getLast();

            Log.i(Const.TAG, "Average (last) " + averageHRList.getLast() + " Max HR F : "+ maxHR_F + " Min HR F : "+ minHR_F
                    + " Max HR L : " + maxHR_L + " Min HR L : " + minHR_L + " Last HR : "+lastHR);


            if(negativeValues.size() != queueSize || neutralValues.size() != queueSize || positiveValues.size() != queueSize){

                if(maxHR_F < lastHR){
                    initialState = "negative";
                }else if (minHR_F > lastHR){
                    initialState = "positive";
                }else{
                    initialState = "neutral";
                }

                if(initialState.equals("negative")){
                    if (negativeValues.size() != queueSize) {
                        negativeValues.add(lastHR);
                    } else {
                        negativeValues.removeFirst();
                        negativeValues.add(lastHR);
                    }

                    //Consider neutral !
                    if(neutralValues.size()!= 0
                            && maxNeutral!=0){
                        if(lastHR < maxNeutral){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }

                }else if(initialState.equals("neutral")){

                    if(negativeValues.size()!=0
                            && maxNegative != 0){
                        if(lastHR < maxNegative){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }
                    if(positiveValues.size()!=0
                        && minPositive !=10000){
                        if(lastHR > minPositive){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }


                }else if(initialState.equals("positive")){
                    if (positiveValues.size() != queueSize) {
                        positiveValues.add(lastHR);
                    } else {
                        positiveValues.removeFirst();
                        positiveValues.add(lastHR);
                    }

                    //Consider neutral !
                    if(neutralValues.size()!= 0
                            && minNeutral!=10000){
                        if(lastHR > minNeutral){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }

                }


                if(negativeValues.size()!= 0){
                    maxNegative = getMaxValueFromArray(negativeValues);
                    minNegative = getMinValueFromArray(negativeValues);
                }
                if(neutralValues.size() != 0){
                    maxNeutral = getMaxValueFromArray(neutralValues);
                    minNeutral = getMinValueFromArray(neutralValues);

                    //Make Average
                    maxNeutralSum += maxNeutral;
                    minNeutralSum += minNeutral;

                    maxNeutralCount++;
                    minNeutralCount++;

                    maxNeutralAverage = maxNeutralSum / maxNeutralCount;
                    minNeutralAverage = minNeutralSum / minNeutralCount;


                }
                if(positiveValues.size() !=0){
                    maxPositive = getMaxValueFromArray(positiveValues);
                    minPositive = getMinValueFromArray(positiveValues);
                }

                //if initial data finish
            }else{

                if(lastHR > maxNeutral
                        && lastHR > minNegative){
                    state = "negative";
                }else if( lastHR < minNeutral
                        && lastHR < maxPositive){
                    state = "positive";
                }else{
                    state = "neutral";
                }

                if(state.equals("negative")) {
                    if (negativeValues.size() != queueSize) {
                        negativeValues.add(lastHR);
                    } else {
                        negativeValues.removeFirst();
                        negativeValues.add(lastHR);
                    }

                    //Consider neutral !
                    if(neutralValues.size()!= 0
                            && maxNeutral!=0){
                        if(lastHR < maxNeutral){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }


                }else if(state.equals("neutral")){

                    if(negativeValues.size()!=0
                            && maxNegative != 0){
                        if(lastHR < maxNegative){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }
                    if(positiveValues.size()!=0
                            && minPositive !=10000){
                        if(lastHR > minPositive){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }

                }else if(state.equals("positive")){
                    if (positiveValues.size() != queueSize) {
                        positiveValues.add(lastHR);
                    } else {
                        positiveValues.removeFirst();
                        positiveValues.add(lastHR);
                    }

                    //Consider neutral !
                    if(neutralValues.size()!= 0
                            && minNeutral!=10000){
                        if(lastHR > minNeutral){
                            if (neutralValues.size() != queueSize) {
                                neutralValues.add(lastHR);
                            } else {
                                neutralValues.removeFirst();
                                neutralValues.add(lastHR);
                            }
                        }
                    }
                }

                if(negativeValues.size()!= 0){
                    maxNegative = getMaxValueFromArray(negativeValues);
                    minNegative = getMinValueFromArray(negativeValues);
                }

                if(neutralValues.size() != 0){
                    maxNeutral = getMaxValueFromArray(neutralValues);
                    minNeutral = getMinValueFromArray(neutralValues);

                    //Make Average
                    maxNeutralSum += maxNeutral;
                    minNeutralSum += minNeutral;

                    maxNeutralCount++;
                    minNeutralCount++;

                    maxNeutralAverage = maxNeutralSum / maxNeutralCount;
                    minNeutralAverage = minNeutralSum / minNeutralCount;

                }

                if(positiveValues.size() !=0){
                    maxPositive = getMaxValueFromArray(positiveValues);
                    minPositive = getMinValueFromArray(positiveValues);
                }



            }







//            //Set preState
//            if(preState.equals("unavailable")){//
//                state = "neutral";
//                preState = "neutral";
//            //Use preState
//            }else{
//                changeState = getChangeState(averageHRList);
//
//                //Pre is neutral
//                if(preState.equals("neutral")){
//
//                    if(changeState.equals("upper")){
//                        if(lastHR > maxHR_F){
//                            state = "negative";
//                            preState = "neutral";
//                        }else{
//                            if(lastHR > averageHR_F){
//                                state = "negative";
//                                preState = "neutral";
//                            }else{
//                                state = "neutral";
//                                preState = "neutral";
//                            }
//                        }
//                    }else if(changeState.equals("middle")){
//                        if(lastHR < maxHR_F
//                                && lastHR > minHR_F){
//                            state ="neutral";
//                            preState = "neutral";
//                        }else{
//                            if(lastHR > maxHR_F){
//                                state = "negative";
//                                preState = "neutral";
//                            }else if(lastHR > averageHR_F){
//                                state = "negative";
//                                preState = "neutral";
//                            }else if(lastHR < minHR_F){
//                                state = "positive";
//                                preState = "neutral";
//                            }else if(lastHR < averageHR_F){
//                                state = "positive";
//                                preState = "neutral";
//                            }else{
//                                state = "neutral";
//                                preState = "neutral";
//                            }
//                        }
//                    }else if(changeState.equals("lower")){
//                        if(lastHR < minHR_F){
//                            state = "positive";
//                            preState = "neutral";
//                        }else{
//                            if(lastHR < averageHR_F){
//                                state = "positive";
//                                preState = "neutral";
//                            }else{
//                                state = "neutral";
//                                preState = "neutral";
//                            }
//                        }
//                    }else{
//                        Log.i(Const.TAG, "Error state");
//                    }
//                //pre is negative
//                }else if(preState.equals("negative")){
//
//                    if(changeState.equals("upper")){
//                        if(lastHR < minHR_F){
//                            if(lastHR >= averageHR_F){
//                                state = "negative";
//                                preState = "negative";
//                            }else{
//                                state = "neutral";
//                                preState = "negative";
//                            }
//                        }else{
//                            state = "negative";
//                            preState = "negative";
//                        }
//                    }else if(changeState.equals("middle")){
//                        if(lastHR < minHR_F){
//                            if(lastHR > averageHR_F){
//                                state = "negative";
//                                preState = "negative";
//                            }else{
//                                state = "neutral";
//                                preState = "negative";
//                            }
//                        }else{
//                            state = "negative";
//                            preState = "negative";
//                        }
//                    }else if(changeState.equals("lower")){
//                        if(lastHR < minHR_F){
//                            if(lastHR > averageHR_F){
//                                state = "negative";
//                                preState = "negative";
//                            }else{
//                                state = "neutral";
//                                preState = "negative";
//                            }
//                        }else{
//                            state = "negative";
//                            preState = "negative";
//                        }
//                    }else{
//                        Log.i(Const.TAG, "Error state");
//                    }
//                //pre is positive
//                }else if(preState.equals("positive")){
//
//                    if(changeState.equals("upper")){
//                        if(lastHR > maxHR_F){
//                            if(lastHR < averageHR_F){
//                                state = "positive";
//                                preState = "positive";
//                            }else{
//                                state = "neutral";
//                                preState = "positive";
//                            }
//                        }else{
//                            state = "positive";
//                            preState = "positive";
//                        }
//                    }else if(changeState.equals("middle")){
//                        if(lastHR > maxHR_F){
//                            if(lastHR < averageHR_F){
//                                state = "positive";
//                                preState = "neutral";
//                            }else{
//                                state = "neutral";
//                                preState = "positive";
//                            }
//                        }else{
//                            state = "positive";
//                            preState = "positive";
//                        }
//                    }else if(changeState.equals("lower")){
//                        if(lastHR > maxHR_F){
//                            if(lastHR < averageHR_F){
//                                state = "positive";
//                                preState = "positive";
//                            }else{
//                                state = "neutral";
//                                preState = "positive";
//                            }
//                        }else{
//                            state = "positive";
//                            preState = "positive";
//                        }
//                    }else{
//                        Log.i(Const.TAG, "Error state");
//                    }
//
//                }else{
//                    Log.i(Const.TAG, "Error state");
//                }
//
//            }
        }

        //            if(lastHR > )


//            //upper state
//            if(maxHR_F < maxHR_L
//                && maxHR_F < lastHR) {
//                state = "negative";
//            //down state
//            }else if(minHR_F > minHR_L
//                    && minHR_F > lastHR){
//                state = "positive";
//            }else{
//                state= "neutral";
//            }

//            if(maxHR_1 < lastHR && maxHR_2 < lastHR){
//                state = "negative";
//            }else if( minHR_1 > lastHR && minHR_2 > lastHR){
//                state = "positive";
//            }else{
//                state = "neutral";
//            }

*/



        return state;

    }

    public float getMaxValueFromArray(LinkedList<Float> data){
        float max = 0;
        for(int i = 0; i < data.size(); i++){
            if(max < data.get(i)){
                max = data.get(i);
            }
        }
        return max;
    }
    public float getMinValueFromArray(LinkedList<Float> data) {
        float min = 10000;
        for(int i = 0; i< data.size(); i++){
            if(min > data.get(i)){
                min = data.get(i);
            }
        }
        return min;
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
