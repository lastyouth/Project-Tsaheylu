package com.clover_studio.spikachatmodule.emotion;

import android.content.Context;

import com.clover_studio.spikachatmodule.adapters.RecyclerStickersAdapter;
import com.clover_studio.spikachatmodule.adapters.StickerClassificationRecyclerViewAdapter;
import com.clover_studio.spikachatmodule.base.SingletonLikeApp;
import com.clover_studio.spikachatmodule.models.EstimatedEmotionModel;
import com.clover_studio.spikachatmodule.models.Sticker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_ANGRY;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_HAPPINESS;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_NEUTRAL;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_SADNESS;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_SURPRISE;

/**
 * Created by sbh on 2016-10-27.
 */
public class EmoticonSuggestionManager {
    static private EmoticonSuggestionManager mInstance = null;

    /*
    * static public final int EMOTION_HAPPINESS = 0x00;
    static public final int EMOTION_SADNESS = 0x01;
    static public final int EMOTION_ANGRY = 0x02;
    static public final int EMOTION_SURPRISE = 0x03;
    static public final int EMOTION_NEUTRAL = 0x04;*/
    private Map<String, Integer> mStickerClassification = null;
    private Context mContext = null;
    private List<Sticker> mStickers = null;
    private boolean mInitialized = false;
    private boolean mEmoticonReady = false;
    private List<Sticker> mHappinessEmoticons = new ArrayList<Sticker>();
    private List<Sticker> mSadnessEmoticons = new ArrayList<Sticker>();
    private List<Sticker> mAngryEmoticons = new ArrayList<Sticker>();
    private List<Sticker> mSurpriseEmoticons = new ArrayList<Sticker>();
    private List<Sticker> mNeutralEmoticons = new ArrayList<Sticker>();

    public void updateStickerClassification() {
        String json;
        Gson gson = new Gson();
        if(mInitialized) {
            Type type =  new TypeToken<Map<String,Integer>>(){}.getType();

            json = SingletonLikeApp.getInstance().getSharedPreferences(mContext).getStickersClassification();
            if(json.equals(""))
            {
                return;
            }
            mStickerClassification = gson.fromJson(json,type);
            mEmoticonReady = false;
            mHappinessEmoticons.clear();
            mSadnessEmoticons.clear();
            mAngryEmoticons.clear();
            mNeutralEmoticons.clear();
            mSurpriseEmoticons.clear();

            for (Sticker s: mStickers) {
                String key = s.smallPic;

                if(mStickerClassification.containsKey(key))
                {
                    int emotionType = mStickerClassification.get(key);

                    if(emotionType == EMOTION_HAPPINESS)
                    {
                        mHappinessEmoticons.add(s);
                    }else if(emotionType == EMOTION_SADNESS)
                    {
                        mSadnessEmoticons.add(s);
                    }else if(emotionType == EMOTION_ANGRY)
                    {
                        mAngryEmoticons.add(s);
                    }else if(emotionType == EMOTION_NEUTRAL)
                    {
                        mNeutralEmoticons.add(s);
                    }else if(emotionType == EMOTION_SURPRISE)
                    {
                        mSurpriseEmoticons.add(s);
                    }
                }
            }
            mEmoticonReady = true;
        }
    }

    private EmoticonSuggestionManager(){
    }

    public static EmoticonSuggestionManager getInstance() {
        if(mInstance == null)
        {
            mInstance = new EmoticonSuggestionManager();
        }
        return mInstance;
    }
    public void initialize(Context c, List<Sticker> s)
    {
        mContext = c;
        mStickers = s;
        mInitialized = true;

        updateStickerClassification();
    }

    public Sticker getCurrentEmotionEmoticon(EstimatedEmotionModel e)
    {
        Sticker ret = null;
        Random rn = new Random();
        int emotionType = e.mFinalEstimatedEmotion;

        if(emotionType == EMOTION_HAPPINESS)
        {
            int size = mHappinessEmoticons.size();
            int next = rn.nextInt(Integer.SIZE - 1)%size;

            ret = mHappinessEmoticons.get(next);

        }else if(emotionType == EMOTION_SADNESS)
        {
            int size = mSadnessEmoticons.size();
            int next = rn.nextInt(Integer.SIZE - 1)%size;

            ret = mSadnessEmoticons.get(next);
        }else if(emotionType == EMOTION_ANGRY)
        {
            int size = mAngryEmoticons.size();
            int next = rn.nextInt(Integer.SIZE - 1)%size;

            ret = mAngryEmoticons.get(next);
        }else if(emotionType == EMOTION_NEUTRAL)
        {
            int size = mNeutralEmoticons.size();
            int next = rn.nextInt(Integer.SIZE - 1)%size;

            ret = mNeutralEmoticons.get(next);
        }else if(emotionType == EMOTION_SURPRISE)
        {
            int size = mSurpriseEmoticons.size();
            int next = rn.nextInt(Integer.SIZE - 1)%size;

            ret = mSurpriseEmoticons.get(next);
        }
        return ret;
    }

}
