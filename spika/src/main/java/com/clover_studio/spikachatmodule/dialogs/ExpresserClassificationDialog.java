package com.clover_studio.spikachatmodule.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.utils.UtilsImage;

/**
 * Created by sbh on 2016-11-16.
 */

public class ExpresserClassificationDialog extends Dialog {
    private Spinner mEmotionSpinner;
    private ArrayAdapter<CharSequence> mAdapter;
    private Context mContext;
    private Expresser mExpresser;
    private ImageView mCurrentSticker;

    public static ExpresserClassificationDialog start(Context context, Expresser expresser){
        ExpresserClassificationDialog dialog = new ExpresserClassificationDialog(context, expresser);
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sticker_classification);
        mEmotionSpinner = (Spinner)findViewById(R.id.classification_currentEmotion);
        mCurrentSticker = (ImageView)findViewById(R.id.classification_currentSticker);
        mAdapter = ArrayAdapter.createFromResource(mContext,
                R.array.emotions, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mEmotionSpinner.setAdapter(mAdapter);

        UtilsImage.setImageWithLoader(mCurrentSticker, -1, null, mExpresser.smallPic);
    }

    public ExpresserClassificationDialog(Context context, Expresser expresser)
    {
        super(context, R.style.Theme_Dialog);

        mContext = context;
        mExpresser = expresser;

        show();
    }
}
