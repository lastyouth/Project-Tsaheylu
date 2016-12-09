package com.clover_studio.spikachatmodule.emotion;

import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.Tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sbh on 2016-12-09.
 */

public class PerformanceCheckManager {

    private Timer mTimer;
    private long mStartTime;
    private String mSelectedEmotion;
    public final int WAIT_TIME = 3000;
    private PerformanceCheckManagerListener mCallback;
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            checkStartInternal();
        }
    };

    public PerformanceCheckManager(PerformanceCheckManagerListener m)
    {
        mCallback = m;
        mTimer = new Timer();
        mTimer.schedule(mTimerTask,WAIT_TIME);
    }
    private void checkStartInternal()
    {
        Random random = new Random();

        int n = random.nextInt();
        int emotion;
        if(n < 0)
        {
            n*=-1;
        }
        emotion = n%4;

        if(emotion == 0)
        {
            mSelectedEmotion = "화남";
        }else if(emotion == 1)
        {
            mSelectedEmotion = "슬픔";
        }else if(emotion == 2)
        {
            mSelectedEmotion = "기쁨";
        }else if(emotion == 3)
        {
            mSelectedEmotion = "놀람";
        }
        mStartTime = System.currentTimeMillis();
        mCallback.checkStart(mSelectedEmotion);
    }
    public void checkEndInternal()
    {
        long endtime = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String date = sdf.format(new Date(endtime-mStartTime));

        mCallback.checkEnd(date,mSelectedEmotion);
    }
}
