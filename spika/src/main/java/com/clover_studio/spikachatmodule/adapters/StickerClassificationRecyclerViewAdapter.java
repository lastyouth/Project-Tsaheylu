package com.clover_studio.spikachatmodule.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.base.SingletonLikeApp;
import com.clover_studio.spikachatmodule.emotion.EmoticonSuggestionManager;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.UtilsImage;
import com.clover_studio.spikachatmodule.view.roundimage.RoundedImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_ANGRY;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_NEUTRAL;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_SADNESS;
import static com.clover_studio.spikachatmodule.utils.Const.Emotion.EMOTION_SURPRISE;

/**
 * Created by sbh on 11/6/16.
 */

public class StickerClassificationRecyclerViewAdapter extends RecyclerView.Adapter<StickerClassificationRecyclerViewAdapter.ViewHolder>{
    //


    // map
    private Map<String,Integer> mStickerType = new HashMap<String,Integer>();


    // Locally used constant
    static public final int POSITION_ACQUIRED_CONSTANT = 0x12345678;
    // varialbles
    private List<Expresser> mData;
    private Context mContext;



    public StickerClassificationRecyclerViewAdapter(Context context)
    {
        mData = new ArrayList<Expresser>();
        mContext = context;
        String json;
        Gson gson = new Gson();
        Type type =  new TypeToken<Map<String,Integer>>(){}.getType();

        json = SingletonLikeApp.getInstance().getSharedPreferences(mContext).getStickersClassification();

        if(!json.equals(""))
        {
            mStickerType = gson.fromJson(json,type);
        }
    }



    public void addData(List<Expresser> expressers)
    {
        mData.addAll(expressers);
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickerClassificationRecyclerViewAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_classification, parent, false));
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(Const.TAG,"Position : "+position);
        Expresser i = mData.get(position);
        holder.emotionRadioGroup.setTag(POSITION_ACQUIRED_CONSTANT,position);
        //holder.imageForSmallpic =
        UtilsImage.setImageWithLoader(holder.imageForSmallpic, -1, null, i.smallPic);


        if(mStickerType.containsKey(i.smallPic))
        {
            int childnum = mStickerType.get(i.smallPic);

            Log.d(Const.TAG,"onBindViewHolder : pos - "+position+" childnum - "+childnum+" key - "+i.smallPic);

            try {
                if (holder.radioButtons[childnum] == null) {
                    Log.e(Const.TAG, "onBindViewHolder : why null");
                } else {
                    holder.radioButtons[childnum].setChecked(true);
                }
            }catch(ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            for(int p=0;p<5;p++)
            {
                holder.radioButtons[p].setChecked(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView imageForSmallpic;
        RadioGroup emotionRadioGroup;
        RadioButton[] radioButtons = new RadioButton[5];


        private View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioGroup m = (RadioGroup)v.getParent();
                int pos = (Integer)m.getTag(POSITION_ACQUIRED_CONSTANT);
                Log.d(Const.TAG,"Inquired position : "+pos);

                // retrieving smallpic key
                String key = mData.get(pos).smallPic;
                int i = v.getId();

                if(i == R.id.happiness) {
                    mStickerType.put(key,Const.Emotion.EMOTION_HAPPINESS);
                }
                else if(i == R.id.sadness) {
                    mStickerType.put(key,EMOTION_SADNESS);
                }else if(i == R.id.angry)
                {
                    mStickerType.put(key,EMOTION_ANGRY);
                }else if(i == R.id.surprise)
                {
                    mStickerType.put(key,EMOTION_SURPRISE);
                }else if(i == R.id.neutral)
                {
                    mStickerType.put(key,EMOTION_NEUTRAL);
                }
                // update it to SharedPreference
                Gson gson = new Gson();
                String json = gson.toJson(mStickerType);
                SingletonLikeApp.getInstance().getSharedPreferences(mContext).setStickersClassification(json);
                EmoticonSuggestionManager.getInstance().updateStickerClassification();
            }
        };

        ViewHolder(View v) {
            super(v);
            imageForSmallpic = (RoundedImageView)v.findViewById(R.id.stickerforclassification);
            emotionRadioGroup = (RadioGroup)v.findViewById(R.id.emotionradios);

            radioButtons[0] = (RadioButton)v.findViewById(R.id.happiness);
            radioButtons[1] = (RadioButton)v.findViewById(R.id.sadness);
            radioButtons[2] = (RadioButton)v.findViewById(R.id.angry);
            radioButtons[3] = (RadioButton)v.findViewById(R.id.surprise);
            radioButtons[4] = (RadioButton)v.findViewById(R.id.neutral);
            for(int i=0;i<5;i++)
            {
                radioButtons[i].setOnClickListener(mClickListener);
            }
            //emotionRadioGroup.setOnClickListener(mClickListener);
        }

    }
}
