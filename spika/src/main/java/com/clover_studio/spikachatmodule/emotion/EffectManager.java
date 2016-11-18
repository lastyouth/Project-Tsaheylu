package com.clover_studio.spikachatmodule.emotion;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.animation.AccelerateInterpolator;

import com.clover_studio.spikachatmodule.ChatActivity;
import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.plattysoft.leonids.ParticleSystem;

import java.util.ArrayList;

/**
 * Created by sbh on 2016-11-18.
 */

public class EffectManager {

    private static EffectManager mManager = null;
    private Context mContext = null;
    private ParticleSystem ps;
    private int mMaxDisplayX;
    private int mMaxDisplayY;

    private static final int NEXT_X_STEP_THRESHOLD = 15;
    private static final int NEXT_Y_STEP_THRESHOLD = 20;

    private int sinFunction(int Y)
    {
        final double period = mMaxDisplayX / 16.0;
        final double y_axis_parallel = mMaxDisplayX / 2.0;
        final double amplitude = mMaxDisplayX / 3.0;
        double result = amplitude*Math.cos(period*Y) + y_axis_parallel;

        int res = (int)result;

        if(res > mMaxDisplayX)
        {
            res = mMaxDisplayX - NEXT_X_STEP_THRESHOLD;
        }
        return res;
    }


    private EffectManager()
    {

    }
    public static EffectManager getInstance()
    {
        if(mManager == null)
        {
            mManager = new EffectManager();
        }
        return mManager;
    }
    public void initialize(Context c, int displayMaxX,int displayMaxY)
    {
        mContext = c;

        mMaxDisplayX = displayMaxX;
        mMaxDisplayY = displayMaxY;
    }
    public class UpdateEmitThread extends Thread
    {
        private int startX;
        private int startY;
        private int maxSpreadCount;

        public UpdateEmitThread(int initX,int initY,int cnt)
        {
            startX = initX;
            startY = initY;
            maxSpreadCount = cnt;
        }
        private Point nextStep(int currentX,int currentY)
        {
            Point ret = new Point();

            ret.y = currentY+NEXT_Y_STEP_THRESHOLD;

            if(ret.y > mMaxDisplayY)
            {
                ret.y = mMaxDisplayY - NEXT_Y_STEP_THRESHOLD;
            }

            ret.x = sinFunction(ret.y);

            return ret;
        }
        @Override
        public void run() {
            super.run();
            Point next = new Point(startX,startY);
            for(int i=0;i<maxSpreadCount;i++)
            {
                ps.updateEmitPoint(next.x,next.y);

                next = nextStep(next.x,next.y);
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ps.stopEmitting();
        }
    }
    public void performEffect(Expresser expresser)
    {
        if(expresser.there_is_no_cow_level())
        {
            return;
        }
        if(expresser.isOnline)
        {
            return;
        }
        if(mContext instanceof ChatActivity)
        {
            ps = new ParticleSystem((Activity)mContext, 100, expresser.targetResource, 800);
            ps.setScaleRange(0.7f, 1.3f);
            ps.setSpeedRange(0.05f, 0.1f);
            ps.setRotationSpeedRange(90, 180);
            ps.setFadeOut(200, new AccelerateInterpolator());
            ps.emit(mMaxDisplayX/2, NEXT_Y_STEP_THRESHOLD, 40);

            new UpdateEmitThread(mMaxDisplayX/2,NEXT_Y_STEP_THRESHOLD,100).start();
        }
    }
}
