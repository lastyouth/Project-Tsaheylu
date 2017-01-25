package com.clover_studio.spikachatmodule.emotion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import com.clover_studio.spikachatmodule.api.retrofit.MSEmotionAPISupporter;
import com.clover_studio.spikachatmodule.models.FacialEmotionModel;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.Tools;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sbh on 2016-10-26.
 */
public class FacialEmotionManager {
    // for singleton
    private static FacialEmotionManager mInstance = null;

    // for retrofit lib,
    private Retrofit mRetrofitForMSEApi;
    private MSEmotionAPISupporter mAPISupporter;
    private static final String BASE_URL_FOR_MSE = "https://api.projectoxford.ai/";
    private static final String KEY = "1a39795f7c1f4a4fb1428371bc710f52"; //mckang key 2016-12-13
    private static final String CONTENT_TYPE = "application/octet-stream";

    // TimerTask
    private class iterateRequest extends TimerTask{

        @Override
        public void run() {
            // request
            mFrontCamera.setOneShotPreviewCallback(mPreviewCallback);
        }
    };
    private TimerTask mTaskForEmotionRecognizion = null;
    private Timer mTimer = null;
    public static final long DEFAULT_INTERVAL = 3000; // 1sec

    // object state
    private boolean mInitialized = false;

    // callback for ChatActivity
    private FacialEmotionManagerListener mCallback;

    // cameras
    private Camera mFrontCamera;
    private Camera.Parameters mFrontCameraParameters;
    private SurfaceTexture mFakePreview;
    private int mPreviewWidth;
    private int mPreviewHeight;
    @SuppressWarnings("deprecated")
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(Const.TAG, "Front Camera has been found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    @SuppressWarnings("deprecated")
    private Camera getCameraInstance(){
        Camera c = null;
        try {
            int cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Log.e(Const.TAG,"No Front Camera has been found");
            } else {
                c = Camera.open(cameraId);
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }
    private Camera.Size findProperPreviewSize()
    {
        if(mFrontCamera == null) {
            return null;
        }
        List<Camera.Size> availableSize = mFrontCamera.getParameters().getSupportedPreviewSizes();

        int length = availableSize.size();
        int targetLength = length / 2; // get the middle size of picture

        Camera.Size properSize = availableSize.get(targetLength);

        return properSize;
    }
    // preview callbacks
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback(){

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            //needed image preprocessing : YCbCr_420_SP -> JPG -> BMP -> ROTATE -> JPG
            YuvImage im = new YuvImage(data, ImageFormat.NV21, mPreviewWidth,
                    mPreviewHeight, null);
            Rect r = new Rect(0,0,mPreviewWidth,mPreviewHeight);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            im.compressToJpeg(r, mFrontCamera.getParameters().getJpegQuality(), baos);
            byte[] rawImage = baos.toByteArray();
            // jpg to bmp
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
            ByteArrayOutputStream rotatedStream = new ByteArrayOutputStream();
            // rotate
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, mPreviewWidth, mPreviewHeight, matrix, false);

            // BMP to JPG
            bitmap.compress(Bitmap.CompressFormat.JPEG, mFrontCamera.getParameters().getJpegQuality(), rotatedStream);

            byte[] rotatedJPGImage = rotatedStream.toByteArray();
            // image preprocessing done.

            Log.d(Const.TAG, "FacialEmotionManager - PreviewFrameTaken: timestamp - " + Tools.generateDate("yyyy/MM/dd - HH:mm:ss", System.currentTimeMillis()));


            Call<ResponseBody> newRequest = mAPISupporter.getFacialEmotion(KEY, CONTENT_TYPE, RequestBody.create(MediaType.parse(CONTENT_TYPE), rotatedJPGImage));

            // async request
            newRequest.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // may have result failure or success
                    int responseCode = response.code();
                    if(responseCode == 200)
                    {
                        // success
                        Log.e(Const.TAG,"FacialEmotionManager : onResponse invoked, rescode is "+responseCode+" job finished successfully");

                        ResponseBody resultbody = response.body();
                        String resultString = null;
                        try {
                            resultString = resultbody.string();
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                        if(resultString == null)
                        {
                            mCallback.facialEmotionRecognitionFinished(null,false);
                            return;
                        }
                        else {
                            try {
                                JSONArray arrayresult = new JSONArray(resultString);

                                if(arrayresult.length() != 1)
                                {
                                    // multiple faces detected
                                    Log.d(Const.TAG,"FacialEmotionManater : multiple faces detected, discard this emotion info");
                                    mCallback.facialEmotionRecognitionFinished(null,false);
                                    return;
                                }
                                // guarantee that JSONArray has one element
                                resultString = resultString.substring(1,resultString.length()-1);
                                Gson gson = new Gson();
                                FacialEmotionModel t = gson.fromJson(resultString,FacialEmotionModel.class);

                                // now FacialEmotionModel has been produced.
                                Log.d(Const.TAG,"FacialEmotionManater : FacialEmotionModel has been produced, rawjson : "+resultString);
                                mCallback.facialEmotionRecognitionFinished(t,true);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                mCallback.facialEmotionRecognitionFinished(null,false);
                                return;
                            }
                        }
                    }
                    else
                    {
                        // something wrong
                        Log.e(Const.TAG,"FacialEmotionManager : onResponse invoked, but rescode is "+responseCode);
                        mCallback.facialEmotionRecognitionFinished(null,false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // internal processing failure
                    Log.e(Const.TAG,"FacialEmotionManager : onFailure Invoked");
                    mCallback.facialEmotionRecognitionFinished(null,false);
                }
            });
        }
    };
    private FacialEmotionManager()
    {
        mRetrofitForMSEApi = new Retrofit.Builder()
                .baseUrl(BASE_URL_FOR_MSE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mAPISupporter = mRetrofitForMSEApi.create(MSEmotionAPISupporter.class);
    }
    public static FacialEmotionManager getInstance()
    {
        if(mInstance == null)
        {
            mInstance = new FacialEmotionManager();
        }
        return mInstance;
    }

    public boolean initializeFacialManager(FacialEmotionManagerListener m)
    {
        if(!mInitialized)
        {
            // callback check
            if(m == null)
            {
                return false;
            }
            mCallback = m;
            // Camera
            mFrontCamera = getCameraInstance();
            if(mFrontCamera == null)
            {
                return false;
            }
            mFrontCameraParameters = mFrontCamera.getParameters();
            mFakePreview = new SurfaceTexture(10);
            try {
                mFrontCamera.setPreviewTexture(mFakePreview);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            Camera.Size properSize = findProperPreviewSize();

            mPreviewHeight = properSize.height;
            mPreviewWidth = properSize.width;

            // set proper picture size
            mFrontCameraParameters.setPreviewSize(properSize.width,properSize.height);
            mFrontCameraParameters.setRotation(270);
            mFrontCamera.setParameters(mFrontCameraParameters);
            mFrontCamera.setDisplayOrientation(90);
            mFrontCamera.startPreview();
            // Timer
            initializeTimer(DEFAULT_INTERVAL);
            mInitialized = true;
        }
        else
        {
            return false; // already initialized
        }
        return true;
    }

    public void releaseFacialManager()
    {
        mFrontCamera.stopPreview();
        mFrontCamera.release();
        mFrontCamera = null;
        releaseTimer();
        mInitialized = false;
    }

    public void initializeTimer(long interval)
    {
        if(mTimer == null && mTaskForEmotionRecognizion == null) {
            mTimer = new Timer();
            mTaskForEmotionRecognizion = new iterateRequest();

            mTimer.schedule(mTaskForEmotionRecognizion, 0, interval);
        }
    }

    public void releaseTimer()
    {
        if(mTaskForEmotionRecognizion != null)
        {
            mTaskForEmotionRecognizion.cancel();
            mTaskForEmotionRecognizion = null;
        }
        if(mTimer != null)
        {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }
    public boolean isInitialized()
    {
        return mInitialized;
    }
}
