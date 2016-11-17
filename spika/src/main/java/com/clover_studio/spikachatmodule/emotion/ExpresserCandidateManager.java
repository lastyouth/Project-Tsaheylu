package com.clover_studio.spikachatmodule.emotion;

import android.content.Context;
import android.widget.LinearLayout;

import com.clover_studio.spikachatmodule.models.EstimatedEmotionModel;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.utils.Const;

import java.util.ArrayList;
import java.util.Random;

import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_ANGRY;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_HAPPINESS;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_NEUTRAL;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_SADNESS;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_SURPRISE;

/**
 * Created by sbh on 2016-10-27.
 */
public class ExpresserCandidateManager {
    static private ExpresserCandidateManager mInstance = null;

    /*
    * static public final int EMOTION_HAPPINESS = 0x00;
    static public final int EMOTION_SADNESS = 0x01;
    static public final int EMOTION_ANGRY = 0x02;
    static public final int EMOTION_SURPRISE = 0x03;
    static public final int EMOTION_NEUTRAL = 0x04;*/
    private Context mContext = null;
    private boolean mInitialized = false;
    private boolean mEmoticonReady = false;

    private ArrayList<Expresser> mHappinessExpressers = new ArrayList<Expresser>();
    private ArrayList<Expresser> mSadnessExpressers = new ArrayList<Expresser>();
    private ArrayList<Expresser> mAngryExpressers = new ArrayList<Expresser>();
    private ArrayList<Expresser> mSurpriseExpressers = new ArrayList<Expresser>();
    private ArrayList<Expresser> mNeutralExpressers = new ArrayList<Expresser>();

    public boolean updateStickerClassification(Expresser targetExpresser, int emotionType, int pos) {
        if(pos >= Const.Emotion.MAX_EXPRESSER_CANDIDATE)
        {
            return false;
        }

        if(emotionType == Const.Emotion.EMOTION_ANGRY)
        {
            mAngryExpressers.set(pos,targetExpresser);
        }else if(emotionType == Const.Emotion.EMOTION_HAPPINESS)
        {
            mHappinessExpressers.set(pos,targetExpresser);
        }else if(emotionType == Const.Emotion.EMOTION_NEUTRAL)
        {
            mNeutralExpressers.set(pos,targetExpresser);
        }else if(emotionType == Const.Emotion.EMOTION_SADNESS)
        {
            mSadnessExpressers.set(pos,targetExpresser);
        }else if(emotionType == Const.Emotion.EMOTION_SURPRISE)
        {
            mSurpriseExpressers.set(pos,targetExpresser);
        }else
        {
            return false;
        }

        return true;
    }

    private ExpresserCandidateManager(){
    }

    public static ExpresserCandidateManager getInstance() {
        if(mInstance == null)
        {
            mInstance = new ExpresserCandidateManager();
        }
        return mInstance;
    }
    public void initialize(Context c)
    {
        mContext = c;
        for(int i=0;i<Const.Emotion.MAX_EXPRESSER_CANDIDATE;i++)
        {
            mNeutralExpressers.add(new Expresser());
            mSadnessExpressers.add(new Expresser());
            mAngryExpressers.add(new Expresser());
            mSurpriseExpressers.add(new Expresser());
            mHappinessExpressers.add(new Expresser());
        }
        mInitialized = true;
    }

    public Expresser getCurrentEmotionEmoticon(EstimatedEmotionModel e)
    {
        Expresser ret = null;

        return ret;
    }

    public ArrayList<Expresser> getExpresserList(int emotionType)
    {
        if(!mInitialized)
        {
            return null;
        }
        if(emotionType == Const.Emotion.EMOTION_HAPPINESS)
        {
            return mHappinessExpressers;
        }else if(emotionType == Const.Emotion.EMOTION_SURPRISE)
        {
            return mSurpriseExpressers;
        }else if(emotionType == Const.Emotion.EMOTION_NEUTRAL)
        {
            return mNeutralExpressers;
        }else if(emotionType == Const.Emotion.EMOTION_ANGRY)
        {
            return mAngryExpressers;
        }else if(emotionType == Const.Emotion.EMOTION_SADNESS)
        {
            return mSadnessExpressers;
        }
        return null;
    }

}
