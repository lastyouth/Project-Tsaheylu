package com.clover_studio.spikachatmodule.emotion;

import android.content.Context;

import com.clover_studio.spikachatmodule.base.SingletonLikeApp;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.utils.Const;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sbh on 2016-10-27.
 */
public class ExpresserCandidateManager {
    static private ExpresserCandidateManager mInstance = null;

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
        saveExpressers();
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
        loadExpressers();
        mInitialized = true;
    }

    private boolean loadExpressers()
    {
        if(mContext == null)
        {
            return false;
        }
        Type type = new TypeToken<Map<Integer,ArrayList<Expresser>>>(){}.getType();

        Gson gson = new Gson();
        String json = SingletonLikeApp.getInstance().getSharedPreferences(mContext).getExpressersClassification();

        if(json.equals(""))
        {
            return false;
        }

        Map<Integer,ArrayList<Expresser>> savedData = new HashMap<Integer, ArrayList<Expresser>>();

        savedData = gson.fromJson(json,type);

        mHappinessExpressers = savedData.get(Const.Emotion.EMOTION_HAPPINESS);
        mSurpriseExpressers = savedData.get(Const.Emotion.EMOTION_SURPRISE);
        mAngryExpressers = savedData.get(Const.Emotion.EMOTION_ANGRY);
        mNeutralExpressers = savedData.get(Const.Emotion.EMOTION_NEUTRAL);
        mSadnessExpressers = savedData.get(Const.Emotion.EMOTION_SADNESS);

        return true;
    }
    private boolean saveExpressers()
    {
        if(mContext == null)
        {
            return false;
        }
        Gson gson = new Gson();

        Map<Integer,ArrayList<Expresser>> forSaving = new HashMap<Integer, ArrayList<Expresser>>();

        forSaving.put(Const.Emotion.EMOTION_HAPPINESS,mHappinessExpressers);
        forSaving.put(Const.Emotion.EMOTION_ANGRY,mAngryExpressers);
        forSaving.put(Const.Emotion.EMOTION_NEUTRAL,mNeutralExpressers);
        forSaving.put(Const.Emotion.EMOTION_SADNESS,mSadnessExpressers);
        forSaving.put(Const.Emotion.EMOTION_SURPRISE,mSurpriseExpressers);

        String json = gson.toJson(forSaving);

        SingletonLikeApp.getInstance().getSharedPreferences(mContext).setExpressersClassification(json);

        return true;
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
