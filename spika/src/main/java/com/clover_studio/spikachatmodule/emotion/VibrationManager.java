package com.clover_studio.spikachatmodule.emotion;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.view.animation.AccelerateInterpolator;

import com.clover_studio.spikachatmodule.ChatActivity;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.plattysoft.leonids.ParticleSystem;

/**
 * Created by sbh on 2016-11-21.
 */

public class VibrationManager {
    private Vibrator mVibrator;
    private VibrationManager mManager = null;
    private Context mContext;
    private static final int MAX_PATTERN = 10;

    public VibrationManager(Context m)
    {
        mContext = m;

        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void performVibration(Expresser expresser)
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
            long[] pattern = {500,250,750,350,850,450,500,1000,250,1000,250};

            mVibrator.vibrate(pattern,-1);
        }
    }
}
