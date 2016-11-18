package com.clover_studio.spikachatmodule.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.emotion.ExpresserCandidateManager;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.UtilsImage;

import java.util.ArrayList;

/**
 * Created by sbh on 2016-11-16.
 */

public class ExpresserClassificationDialog extends Dialog {
    private Spinner mEmotionSpinner;
    private ArrayAdapter<CharSequence> mAdapter;
    private Context mContext;
    private Expresser mExpresser;
    private ImageView mCurrentExpresser;
    private LinearLayout mCandidates;
    private ImageView[] mIvCandidates;
    private AdapterView.OnItemSelectedListener mEmotionItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String currentEmotion = parent.getSelectedItem().toString();
            int emotionType = getCurrentEmotionType(currentEmotion);

            updateExpresserCandidate(emotionType);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private View.OnClickListener mCandidateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String currentEmotion = mEmotionSpinner.getSelectedItem().toString();
            int id = v.getId();
            int emotionType = getCurrentEmotionType(currentEmotion);

            ExpresserCandidateManager.getInstance().updateStickerClassification(mExpresser,emotionType,id);

            Toast.makeText(mContext,"Done!",Toast.LENGTH_SHORT).show();

            dismiss();
        }
    };

    public int getCurrentEmotionType(String currentEmotion)
    {
        int ret = Const.Emotion.EMOTION_HAPPINESS;

        if(currentEmotion == null)
        {
            return -1;
        }
        if(currentEmotion.equals("Happiness"))
        {
            ret = Const.Emotion.EMOTION_HAPPINESS;
        }else if(currentEmotion.equals("Sadness"))
        {
            ret = Const.Emotion.EMOTION_SADNESS;
        }else if(currentEmotion.equals("Angry"))
        {
            ret = Const.Emotion.EMOTION_ANGRY;
        }else if(currentEmotion.equals("Surprise"))
        {
            ret = Const.Emotion.EMOTION_SURPRISE;
        }else if(currentEmotion.equals("Neutral"))
        {
            ret = Const.Emotion.EMOTION_NEUTRAL;
        }
        return ret;
    }

    public static ExpresserClassificationDialog start(Context context, Expresser expresser){
        ExpresserClassificationDialog dialog = new ExpresserClassificationDialog(context, expresser);
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sticker_classification);
        mEmotionSpinner = (Spinner)findViewById(R.id.classification_currentEmotion);
        mCurrentExpresser = (ImageView)findViewById(R.id.classification_currentSticker);
        mAdapter = ArrayAdapter.createFromResource(mContext,
                R.array.emotions, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mEmotionSpinner.setAdapter(mAdapter);
        mEmotionSpinner.setOnItemSelectedListener(mEmotionItemSelectedListener);
        mCandidates = (LinearLayout)findViewById(R.id.classification_candidates);


        if(mExpresser.isOnline) {
            UtilsImage.setImageWithLoader(mCurrentExpresser, -1, null, mExpresser.smallPic);
        }
        else
        {
            mCurrentExpresser.setImageResource(mExpresser.targetResource);
        }
        float size = 0;
        //float padding = 0;
        for(int i=0;i<Const.Emotion.MAX_EXPRESSER_CANDIDATE;i++)
        {
            mIvCandidates[i] = new ImageView(mCandidates.getContext());
            mIvCandidates[i].setId(i);
            mCandidates.addView(mIvCandidates[i],i);
            mIvCandidates[i].setOnClickListener(mCandidateClickListener);
            size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mCandidates.getContext().getResources().getDisplayMetrics());
            //padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mCandidates.getContext().getResources().getDisplayMetrics());
            mIvCandidates[i].getLayoutParams().height = (int) size;
            mIvCandidates[i].getLayoutParams().width = (int) size;
            //mIvCandidates[i].setPadding((int) padding, (int) padding, (int) padding, (int) padding);
            mIvCandidates[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        mCandidates.getLayoutParams().width = (int)(size) *(Const.Emotion.MAX_EXPRESSER_CANDIDATE);
        //mDialog.getLayoutParams().width = (int)(size) *(Const.Emotion.MAX_EXPRESSER_CANDIDATE);

        updateExpresserCandidate(Const.Emotion.EMOTION_HAPPINESS);
    }

    private void updateExpresserCandidate(int emotionType)
    {
        if(mCandidates == null)
        {
            dismiss();
        }
        ArrayList<Expresser> tmpExpressers = ExpresserCandidateManager.getInstance().getExpresserList(emotionType);

        if(tmpExpressers == null)
        {
            Toast.makeText(mContext,"Online expressers are not initialized! Try again",Toast.LENGTH_SHORT).show();
            dismiss();
        }


        for(int i=0;i<Const.Emotion.MAX_EXPRESSER_CANDIDATE;i++)
        {
            Expresser expresser = tmpExpressers.get(i);

            if(expresser.there_is_no_cow_level())
            {
                // not initialized
                mIvCandidates[i].setImageResource(R.drawable.ic_nosticker);
            }
            else
            {
                if(expresser.isOnline)
                {
                    UtilsImage.setImageWithLoader(mIvCandidates[i], -1, null, expresser.smallPic);
                }
                else
                {
                    mIvCandidates[i].setImageResource(expresser.targetResource);
                }
            }
        }
    }

    public ExpresserClassificationDialog(Context context, Expresser expresser)
    {
        super(context, R.style.Theme_Dialog);

        mContext = context;
        mExpresser = expresser;
        mIvCandidates = new ImageView[Const.Emotion.MAX_EXPRESSER_CANDIDATE];

        show();
    }
}
