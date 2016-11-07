package com.clover_studio.spikachatmodule.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.models.Sticker;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.UtilsImage;
import com.clover_studio.spikachatmodule.view.roundimage.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbh on 11/6/16.
 */

public class StickerClassificationRecyclerViewAdapter extends RecyclerView.Adapter<StickerClassificationRecyclerViewAdapter.ViewHolder>{
    // Locally used constant
    static public final int POSITION_ACQUIRED_CONSTANT = 0x12345678;
    // varialbles
    private List<Sticker> mData;
    private Context mContext;

    public StickerClassificationRecyclerViewAdapter(Context context)
    {
        mData = new ArrayList<Sticker>();
        mContext = context;
    }

    private RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            int pos = (Integer)radioGroup.getTag(POSITION_ACQUIRED_CONSTANT);

            Log.d(Const.TAG,"Inquired position : "+pos);
        }
    };

    public void addData(List<Sticker> stickers)
    {
        mData.addAll(stickers);
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickerClassificationRecyclerViewAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_classification, parent, false));
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Sticker i = mData.get(position);
        holder.emotionRadioGroup.setTag(POSITION_ACQUIRED_CONSTANT,position);
        //holder.imageForSmallpic =
        UtilsImage.setImageWithLoader(holder.imageForSmallpic, -1, null, i.smallPic);

        holder.emotionRadioGroup.setOnCheckedChangeListener(mCheckedChangeListener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView imageForSmallpic;
        RadioGroup emotionRadioGroup;

        ViewHolder(View v) {
            super(v);
            imageForSmallpic = (RoundedImageView)v.findViewById(R.id.stickerforclassification);
            emotionRadioGroup = (RadioGroup)v.findViewById(R.id.emotionradios);


        }

    }
}
