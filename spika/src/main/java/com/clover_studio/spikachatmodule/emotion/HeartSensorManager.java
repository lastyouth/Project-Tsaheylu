package com.clover_studio.spikachatmodule.emotion;

/**
 * Created by ECLmcKang on 2016-10-28.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.assist.deque.LIFOLinkedBlockingDeque;

/**
 블루트스를 연결하고 HR 또는 HRV 의 데이터를 Queue 의 데이터저장하는 클래스.
 프로그램 흐름
 checkBluetooth() -> selectDevice() -> connect() -> ConnectThread().run() ->
 connected() -> ConnectedThread().run()
 method 설명
 checkBluetooth() - 블루투스가 활성화 되어있는지를 검사하고 활성화가 되어있다면 selectDevice()를 호출
 selectDevice() - 블루투스를 사용 할 수 있는 디바이스를 검색하고 선택하여 연결 시도 (connect())
 connect() - thread 를 reset 하고, 연결을 시도함
 ConnectThread().run() - 소켓을 열어 연결을 시도함. 연결이 활상화되면 connected() 를 호출함
 connected() - thread 를 reset 하고. 데이터를 받아올 준비를 함
 ConnectedThread().run() - inputStream, outputStream 을 통해서 데이터를 받고, queue 에 데이터를 저장함

 HR 또는 HRV 데이터 관련
 getHR(), getHRV (Queue 클래스로 return함)



 */
public class HeartSensorManager {
    // Debugging
    private static final String TAG = "BluetoothManager";

    private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private boolean mAllowInsecureConnections;
    private BluetoothAdapter mAdapter;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private Context mContext;

    private int mPariedDeviceCount = 0;
    private Set<BluetoothDevice> mDevices;

    public List<String> receivedString;


    //Queue 데이터 선언 및 크기
    private HeartData heartDate;
    private HeartData preHeartDate;
    private HeartData prepreHeartData;

//    private LinkedList<HeartData> test;

    private int queueSize;

    private long overallSumHR = 0;
    private long overallSumHRV = 0;

    private long preOverallSumHR = 0;
    private long preOverallSumHRV = 0;

    private int overallCountHR = 0;
    private int overallCountHRV = 0;

    private float overallAverageHR = 0;
    private float overallAverageHRV = 0;

    public boolean availableFlag = false;


    public class HeartData
    {
        private Queue<Integer> HR;
        private Queue<Integer> HRV;

        private Queue<Integer> minHR;
        private Queue<Integer> maxHR;

        private Queue<Integer> minHRV;
        private Queue<Integer> maxHRV;

        private Queue<Float> averageHR;
        private Queue<Float> averageHRV;

        private Queue<Float> minOfAverageHR;
        private Queue<Float> minOfAverageHRV;

        private Queue<Float> maxOfAverageHR;
        private Queue<Float> maxOfAverageHRV;

        private Queue<Float> averageOfAverageHR;
        private Queue<Float> averageOfAverageHRV;



        public HeartData(){
            HR = new LinkedList<Integer>();
            HRV = new LinkedList<Integer>();

            minHR = new LinkedList<Integer>();
            minHRV = new LinkedList<Integer>();

            maxHR = new LinkedList<Integer>();
            maxHRV = new LinkedList<Integer>();

            averageHR = new LinkedList<Float>();
            averageHRV = new LinkedList<Float>();

            averageOfAverageHR = new LinkedList<Float>();
            averageOfAverageHRV = new LinkedList<Float>();

            maxOfAverageHR = new LinkedList<Float>();
            maxOfAverageHRV = new LinkedList<Float>();

            minOfAverageHR = new LinkedList<Float>();
            minOfAverageHRV = new LinkedList<Float>();

        }


//        private Queue<Integer> HR = new LinkedList<Integer>();
//        private Queue<Integer> HRV = new LinkedList<Integer>();
//
//        private Queue<Float> averageHR = new LinkedList<Float>();
//        private Queue<Float> averageHRV = new LinkedList<Float>();
//
//        private Queue<Float> meanHR = new LinkedList<Float>();
//        private Queue<Float> meanHRV = new LinkedList<Float>();
//
//        private Queue<Float> maxHR = new LinkedList<Float>();
//        private Queue<Float> maxHRV = new LinkedList<Float>();
//
//        private Queue<Float> minHR = new LinkedList<Float>();
//        private Queue<Float> minHRV = new LinkedList<Float>();


//        heartDate.HR = new LinkedList<Integer>();
//        heartDate.HRV = new LinkedList<Integer>();
//
//        heartDate.averageHR = new LinkedList<Float>();
//        heartDate.averageHRV = new LinkedList<Float>();
//
//        heartDate.meanHR = new LinkedList<Float>();
//        heartDate.meanHRV = new LinkedList<Float>();
//
//        heartDate.minHR = new LinkedList<Float>();
//        heartDate.minHRV = new LinkedList<Float>();
//
//        heartDate.maxHR = new LinkedList<Float>();
//        heartDate.maxHRV = new LinkedList<Float>();

        //데이터를 Get method
        public Queue<Integer> getHR(){
            return HR;
        }

        public Queue<Integer> getHRV(){
            return HRV;
        }

        public Queue<Integer> getMinHR() { return minHR; }

        public Queue<Integer> getMinHRV() { return minHRV; }

        public Queue<Integer> getMaxHR() { return maxHR; }

        public Queue<Integer> getMaxHRV() { return  maxHRV; }

        public Queue<Float> getAverageHR(){ return averageHR; }

        public Queue<Float> getAverageHRV(){ return averageHRV; }

        public Queue<Float> getAverageOfAverageHR() { return averageOfAverageHR; }

        public Queue<Float> getAverageOfAverageHRV() { return  averageOfAverageHRV; }

        public Queue<Float> getMinOfAverageHR() { return minOfAverageHR; }

        public Queue<Float> getMinOfAverageHRV() { return  minOfAverageHRV; }

        public Queue<Float> getMaxOfAverageHR() { return maxOfAverageHR; }

        public Queue<Float> getMaxOfAverageHRV() { return  maxOfAverageHRV; }

    }



    //mcKang
//    private Queue<Integer> HR;
//    private Queue<Integer> HRV;
//    //Average
//    private Queue<Float> averageHR ;
//    private Queue<Float> averageHRV;
//
//    private Queue<Float> meanHR;
//    private Queue<Float> meanHRV;
//
//    private Queue<Float> maxHR;
//    private Queue<Float> maxHRV;
//
//    private Queue<Float> minHR;
//    private Queue<Float> minHRV;




    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    /**
     * 생성자
     * @param context The UI Activity Context
     * @param queueSize Queue 크기, 가장 최근 몇개의 데이터를 사용할 지 결정을 함
     */
    public HeartSensorManager(Context context, int queueSize) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mContext = context;
        mAllowInsecureConnections = true;
        receivedString= new ArrayList<String>();


        this.queueSize = queueSize;

        heartDate = new HeartData();

//        test = new LinkedList<HeartData>();



//        HR = new LinkedList<Integer>();
//        HRV = new LinkedList<Integer>();
//
//        averageHR = new LinkedList<Float>();
//        averageHRV = new LinkedList<Float>();
//
//        meanHR = new LinkedList<Float>();
//        meanHRV = new LinkedList<Float>();
//
//        minHR = new LinkedList<Float>();
//        minHRV = new LinkedList<Float>();
//
//        maxHR = new LinkedList<Float>();
//        maxHRV = new LinkedList<Float>();

    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /*
      블루투스연결이 되어있는지 검사를 하고 연결이 되었다면, 디바이스를 선택하는 부분으로 넘어간다.
     */
    public void checkBluetooth() {
        /*
         * getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
         이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.
         */
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {  // 블루투스 미지원
            Toast.makeText(mContext, "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();

        } else { // 블루투스 지원
            /* isEnable() : 블루투스 모듈이 활성화 되었는지 확인.
             *  true : 지원 ,  false : 미지원
             */
            if (!mAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
                Toast.makeText(mContext, "현재 블루투스가 비활성 상태입니다. 블루투스를 활성화 하십시오.", Toast.LENGTH_LONG).show();

            } else {
                // 블루투스 지원하며 활성 상태인 경우.
                selectDevice();
            }
        }
    }

    /*
    블루투스 활성화 후 연결 할 수 있는 장비를 선택을 함.
     */
    // 블루투스 지원하며 활성 상태인 경우.
    private void selectDevice() {
        // 블루투스 디바이스는 연결해서 사용하기 전에 먼저 페어링 되어야만 한다
        // getBondedDevices() : 페어링된 장치 목록 얻어오는 함수.
        mDevices = mAdapter.getBondedDevices();
        mPariedDeviceCount = mDevices.size();

        if (mPariedDeviceCount == 0) { // 페어링된 장치가 없는 경우.
            Toast.makeText(mContext, "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
        }

        System.out.println("In Select Device method");
        // 페어링된 장치가 있는 경우.
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("블루투스 장치 선택");

        // 각 디바이스는 이름과(서로 다른) 주소를 가진다. 페어링 된 디바이스들을 표시한다.
        List<String> listItems = new ArrayList<String>();
        List<String> listAddress = new ArrayList<String>();

        for (BluetoothDevice device : mDevices) {
            // device.getName() : 단말기의 Bluetooth Adapter 이름을 반환.
            listItems.add(device.getName());
            listAddress.add(device.getAddress());
        }
        listItems.add("취소");  // 취소 항목 추가.


        // CharSequence : 변경 가능한 문자열.
        // toArray : List형태로 넘어온것 배열로 바꿔서 처리하기 위한 toArray() 함수.
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        // toArray 함수를 이용해서 size만큼 배열이 생성 되었다.
        listItems.toArray(new CharSequence[listItems.size()]);

        final CharSequence[] itemsForAddress = listAddress.toArray(new CharSequence[listAddress.size()]);
        listAddress.toArray(new CharSequence[listAddress.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                // TODO Auto-generated method stub
                if (item == mPariedDeviceCount) { // 연결할 장치를 선택하지 않고 '취소' 를 누른 경우.
                    Toast.makeText(mContext, "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
//                    finish();
                } else { // 연결할 장치를 선택한 경우, 선택한 장치와 연결을 시도함.

                    System.out.println(items[item].toString());

                    BluetoothDevice device = mAdapter.getRemoteDevice(itemsForAddress[item].toString());

                    connect(device);

                }
            }

        });

        builder.setCancelable(false);  // 뒤로 가기 버튼 사용 금지.
        AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * Thread 를 reset 하고, 블루투스와 연결된 장비와 소켓을 위한 전처리 작업을 수행함
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    /**
     * Socket 을 연결 후 데이터를 주고 받을 준비를 함. Thread 가 돌아가고 있다면 reset
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * 블루투스 디바이스와 연결을 시도 socket 을 통한 연결을 시도함
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (mAllowInsecureConnections) {
                    Method method;

                    method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                } else {
                    tmp = device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
                }
            } catch (Exception e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (HeartSensorManager.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
    블루투스와 연결을 한 후 HR 또는 HRV 를 데이터를 가져 옴
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mEleganceEndup=false;

        public void sendExitSignal()
        {
            mEleganceEndup = true;
            try {
                if(mmSocket.isConnected()) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (!mEleganceEndup) {
                try {
                    bytes = mmInStream.read(buffer);

                    String value = new String(buffer,0,bytes);

                    String subValue = new String();

                    for(int i= 0; i < bytes; i++){
                        if(value.charAt(i) == 'B'){
                            if(bytes >= i+4){
                                subValue = value.substring(i,i+4);
//                              System.out.print("Value : "+ subValue + " byte : "+ bytes);
                                String tempHRToString = value.substring(i+1,i+4);
                                tempHRToString = tempHRToString.trim();
                                int tempHR  = Integer.parseInt(tempHRToString);

//                                 System.out.println("(Test) HR : "+tempHR );
                                if(tempHR <= 150 && tempHR >= 50){

//                                    preOverallSumHR = overallSumHR;

                                    overallSumHR += tempHR;
                                    overallCountHR++;

                                    if(heartDate.getHR().size() >= queueSize){
                                        heartDate.getHR().remove();
                                        heartDate.getHR().offer(tempHR);
                                    }else{
                                        heartDate.getHR().offer(tempHR);
                                    }

                                }

                            }
//                            else{
//                                System.out.println("(Test) Failed Value B: "+ value + " byte : "+ bytes);
//                            }

                        }
                        if(value.charAt(i) == 'Q'){
                            if(bytes >=i+4){
                                subValue = value.substring(i,i+4);
//                              System.out.print("Value : "+ subValue + " byte : "+ bytes);
                                String tempHRVToString = value.substring(i+1,i+4);
                                tempHRVToString = tempHRVToString.trim();
                                int tempHRV = Integer.parseInt(tempHRVToString);
//                               System.out.println("(Test) HRV : "+ tempHRV );
                                if(tempHRV >= 500) {

//                                    preOverallSumHRV = overallSumHRV;

                                    overallSumHRV += tempHRV;
                                    overallCountHRV++;

                                    if (heartDate.getHRV().size() >= queueSize) {
                                        heartDate.getHRV().remove();
                                        heartDate.getHRV().offer(tempHRV);
                                    } else {
                                        heartDate.getHRV().offer(tempHRV);
                                    }
                                }

                            }
//                            else{
//                                System.out.println("(Test) Failed Value Q:"+ value + " byte : "+ bytes);
//                            }
                        }
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * 연결 실패 후 상태 전환
     */
    private void connectionFailed() {
        setState(STATE_NONE);
     }

    /**
     * 연결 실패 후 상태 전환
     */
    private void connectionLost() {
        setState(STATE_NONE);
    }
    //For debug
    public void printReceivedString(){
        for(int i = 0; i< receivedString.size(); i++){
            System.out.println(receivedString.get(i));
        }
    }

    public void disconnect()
    {
        if(mConnectedThread != null) {
            mConnectedThread.sendExitSignal();
        }
    }


    public void calucateOverallAverage(){

        if(overallCountHR!=0){
            overallAverageHR = (float)overallSumHR / (float)overallCountHR;
        }
        if(overallCountHRV !=0){
            overallAverageHRV = (float)overallSumHRV / (float)overallCountHRV;
        }
    }



    public void calculateAverage(){
        int sumOfHR = 0;
        int sumOfHRV = 0;
        float tempAverageHR = 0;
        float tempAverageHRV = 0;

        int tempMinHR = 1000;
        int tempMaxHR = 0;
        int tempMinHRV = 1000;
        int tempMaxHRV = 0;



        if(heartDate.getHR().size() == 0 || heartDate.getHRV().size() == 0){
            return;
        }
        if (preOverallSumHR == overallSumHR || preOverallSumHRV == overallSumHRV) {
            return;
        }

        ArrayList<Integer> tempHR = new ArrayList<Integer>(heartDate.getHR());
        ArrayList<Integer> tempHRV = new ArrayList<Integer>(heartDate.getHRV());

        for(int i= 0; i< heartDate.getHR().size();i++){
            sumOfHR  += tempHR.get(i);
            //Find max
            if(tempMaxHR < tempHR.get(i)){
                tempMaxHR = tempHR.get(i);
            }

            //Find Min
            if(tempMinHR > tempHR.get(i)){
                tempMinHR = tempHR.get(i);
            }

        }

        for(int i= 0; i< heartDate.getHRV().size();i++){
            sumOfHRV += tempHRV.get(i);

            if(tempMaxHRV < tempHRV.get(i)){
                tempMaxHRV = tempHRV.get(i);
            }

            if(tempMinHRV > tempHRV.get(i)){
                tempMinHRV= tempHRV.get(i);
            }

        }

        tempAverageHR = (float) sumOfHR / (float)heartDate.getHR().size();
        tempAverageHRV = (float) sumOfHRV / (float)heartDate.getHRV().size();

        if(heartDate.getAverageHR().size() >= queueSize){
            heartDate.getAverageHR().remove();
            heartDate.getAverageHR().offer(tempAverageHR);
        }else{
            heartDate.getAverageHR().offer(tempAverageHR);
        }

        if(heartDate.getMinHR().size() >= queueSize){
            heartDate.getMinHR().remove();
            heartDate.getMinHR().offer(tempMinHR);
        }else{
            heartDate.getMinHR().offer(tempMinHR);
        }

        if(heartDate.getMaxHR().size() >= queueSize){
            heartDate.getMaxHR().remove();
            heartDate.getMaxHR().offer(tempMaxHR);
        }else{
            heartDate.getMaxHR().offer(tempMaxHR);
        }


        if(heartDate.getAverageHRV().size() >= queueSize){
            heartDate.getAverageHRV().remove();
            heartDate.getAverageHRV().offer(tempAverageHRV);
        }else{
            heartDate.getAverageHRV().offer(tempAverageHRV);
        }

        if(heartDate.getMinHRV().size() >= queueSize){
            heartDate.getMinHRV().remove();
            heartDate.getMinHRV().offer(tempMinHRV);
        }else{
            heartDate.getMinHRV().offer(tempMinHRV);
        }

        if(heartDate.getMaxHRV().size() >= queueSize){
            heartDate.getMaxHRV().remove();
            heartDate.getMaxHRV().offer(tempMaxHRV);
        }else{
            heartDate.getMaxHRV().offer(tempMaxHRV);
        }

        preOverallSumHR = overallSumHR;
        preOverallSumHRV = overallSumHRV;


    }

    public void calculateAverageOfAverageAndMinMax(){

        if(heartDate.getAverageHR().size() == queueSize){


            float tempSumHR = 0;
            float tempAverageHR = 0;
            float tempMinHR = 1000;
            float tempMaxHR = 0;
            ArrayList<Float> tempAverageListHR = new ArrayList<Float>(heartDate.getAverageHR());

            float tempSumHRV = 0;
            float tempAverageHRV = 0;
            float tempMinHRV = 1000;
            float tempMaxHRV = 0;
            ArrayList<Float> tempAverageListHRV = new ArrayList<Float>(heartDate.getAverageHRV());

            for(int i = 0; i < queueSize; i++){
                tempSumHR += tempAverageListHR.get(i);
                tempSumHRV += tempAverageListHRV.get(i);

                //Find max
                if(tempMaxHR < tempAverageListHR.get(i)){
                    tempMaxHR = tempAverageListHR.get(i);
                }
                if(tempMaxHRV < tempAverageListHR.get(i)){
                    tempMaxHRV = tempAverageListHRV.get(i);
                }

                //Find Min
                if(tempMinHR > tempAverageListHR.get(i)){
                    tempMinHR = tempAverageListHR.get(i);
                }
                if(tempMinHRV > tempAverageListHR.get(i)){
                    tempMinHRV= tempAverageListHRV.get(i);
                }
            }

            //calculate average
            tempAverageHR = tempSumHR / (float) queueSize;
            tempAverageHRV = tempSumHRV / (float) queueSize;

            //Insert average to Queue
            if(heartDate.getAverageOfAverageHR().size() >= queueSize){
                heartDate.getAverageOfAverageHR().remove();
                heartDate.getAverageOfAverageHR().offer(tempAverageHR);
            }else{
                heartDate.getAverageOfAverageHR().offer(tempAverageHR);
            }

            if(heartDate.getAverageOfAverageHRV().size() >= queueSize){
                heartDate.getAverageOfAverageHRV().remove();
                heartDate.getAverageOfAverageHRV().offer(tempAverageHRV);
            }else{
                heartDate.getAverageOfAverageHRV().offer(tempAverageHRV);
            }
            //Insert min to queue
            if(heartDate.getMinOfAverageHR().size() >= queueSize){
                heartDate.getMinOfAverageHR().remove();
                heartDate.getMinOfAverageHR().offer(tempMinHR);
            }else{
                heartDate.getMinOfAverageHR().offer(tempMinHR);
            }

            if(heartDate.getMinOfAverageHRV().size() >= queueSize){
                heartDate.getMinOfAverageHRV().remove();
                heartDate.getMinOfAverageHRV().offer(tempMinHRV);
            }else{
                heartDate.getMinOfAverageHRV().offer(tempMinHRV);
            }
            //Insert max to queue
            if(heartDate.getMaxOfAverageHR().size() >= queueSize){
                heartDate.getMaxOfAverageHR().remove();
                heartDate.getMaxOfAverageHR().offer(tempMaxHR);
            }else{
                heartDate.getMaxOfAverageHR().offer(tempMaxHR);
            }

            if(heartDate.getMaxOfAverageHRV().size() >= queueSize){
                heartDate.getMaxOfAverageHRV().remove();
                heartDate.getMaxOfAverageHRV().offer(tempMaxHRV);
            }else{
                heartDate.getMaxOfAverageHRV().offer(tempMaxHRV);
            }

            if(availableFlag == false){
                Toast.makeText(mContext.getApplicationContext(),"The HR data is available !!!!",Toast.LENGTH_SHORT).show();
                availableFlag = true;
            }


        }else{
            return;
        }

    }



    public ArrayList<String> getStringFromData( Queue<Objects> data ){
        ArrayList<String> tempString = new ArrayList<String>();
        ArrayList<Objects> tempData =  new ArrayList<Objects>(data);

        for(int i = 0; i < data.size(); i++){
            tempString.add(tempData.get(i).toString());
        }

        return tempString;
    }

    public String getCurrentState(){
        String currentState = "unavailable";

        calculateAverage();
        calculateAverageOfAverageAndMinMax();
        calucateOverallAverage();

        LinkedList<Float> tempAverageHR = new LinkedList<Float>(getHeartDate().getAverageHR());
        LinkedList<Integer> tempMinHR = new LinkedList<Integer>(getHeartDate().getMinHR());
        LinkedList<Integer> tempMaxHR = new LinkedList<Integer>(getHeartDate().getMaxHR());

        LinkedList<Float> tempAverageOfAverageHR = new LinkedList<Float>(getHeartDate().getAverageOfAverageHR());

        LinkedList<Float> tempMaxOfAverageHR = new LinkedList<Float>(getHeartDate().getMaxOfAverageHR());
        LinkedList<Integer> tempHR = new LinkedList<Integer>(getHeartDate().getHR());

//        if (tempMaxOfAverageHR.size() != 0) {
//            if (preEmotionResult.equals("happiness") || preEmotionResult.equals("surprise")) {
//                btnEmotion.setImageResource(R.drawable.ic_shy);
//                classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
//                finalResult = "neutral";
//            } else if (tempHR.getLast() > tempMaxOfAverageHR.getFirst()) {
//                btnEmotion.setImageResource(R.drawable.ic_angry);
//                classifiedemotion = Const.Emotion.EMOTION_ANGRY;
//                finalResult = "angry";
//            } else {
//                btnEmotion.setImageResource(R.drawable.ic_shy);
//                classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
//                finalResult = "neutral";
//            }
//        } else {
//            btnEmotion.setImageResource(R.drawable.ic_shy);
//            classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
//            finalResult = "neutral";
//        }

        if (tempMaxOfAverageHR.size() != 0) {
          if (tempHR.getLast() > tempMaxOfAverageHR.getFirst()) {
                currentState = "negative";
            } else {
                currentState = "neutral";
            }
        } else {
            currentState = "unavailable";
        }


        return currentState;
    }




    public float getOverallAverageHR(){ return overallAverageHR;}

    public float getOverallAverageHRV(){ return overallAverageHRV; }

    public int getQueueSize(){ return queueSize; }

    public HeartData getHeartDate(){ return heartDate;}

    public void setHeartDate(HeartData heartDate){ this.heartDate = heartDate;}

    public HeartData getPreHeartDate(){ return preHeartDate;}

    public void getPreHeartDate(HeartData heartDate){ this.preHeartDate= heartDate;}

    public HeartData getPrePreHeartDate(){ return prepreHeartData;}

    public void setPrePreHeartDate(HeartData heartDate){ this.prepreHeartData= heartDate;}

    public void clearQueue(){

        this.heartDate.getHR().clear();
        this.heartDate.getHRV().clear();
        this.heartDate.getAverageHR().clear();
        this.heartDate.getAverageHRV().clear();
        this.heartDate.getMinHR().clear();
        this.heartDate.getMinHRV().clear();
        this.heartDate.getMaxHR().clear();
        this.heartDate.getMaxHRV().clear();

        this.heartDate.getAverageOfAverageHR().clear();
        this.heartDate.getAverageOfAverageHRV().clear();
        this.heartDate.getMaxOfAverageHR().clear();
        this.heartDate.getMaxOfAverageHRV().clear();
        this.heartDate.getMinOfAverageHR().clear();
        this.heartDate.getMinOfAverageHRV().clear();

        availableFlag = false;

        overallCountHR = 0;
        overallCountHRV = 0;
        overallSumHR = 0;
        overallSumHRV = 0;
        preOverallSumHR = 0;
        preOverallSumHRV = 0;

    }






}