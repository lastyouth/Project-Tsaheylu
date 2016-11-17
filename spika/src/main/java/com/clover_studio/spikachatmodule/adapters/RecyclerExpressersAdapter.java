package com.clover_studio.spikachatmodule.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.utils.UtilsImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu_ivo on 23.03.16..
 */
public class RecyclerExpressersAdapter extends RecyclerView.Adapter<RecyclerExpressersAdapter.ViewHolder>{

    private List<Expresser> data;
    private OnItemClickedListener onItemClickedListener;
    private OnItemLongClickListener onItemLongClickListener;

    public RecyclerExpressersAdapter(List<Expresser> data){
        this.data = data;
    }

    public void setListener(OnItemClickedListener listener){
        onItemClickedListener = listener;
    }

    public void setLongClickListener(OnItemLongClickListener listener)
    {
        onItemLongClickListener = listener;
    }

    public void setData (List<Expresser> data){
        if(this.data == null){
            this.data = new ArrayList<>();
        }
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void addData (List<Expresser> data){
        if(this.data == null){
            this.data = new ArrayList<>();
        }
        int preSize = this.data.size();
        this.data.addAll(data);
        notifyItemRangeInserted(preSize, preSize + data.size());
    }

    public void removeData() {
        if(this.data == null){
            this.data = new ArrayList<>();
        }
        this.data.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView stickerIv;

        ViewHolder(View v) {
            super(v);
            stickerIv = (ImageView) v.findViewById(R.id.stickerIv);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.stickerIv.setImageDrawable(null);

        final Expresser expresser = data.get(position);
        UtilsImage.setImageWithLoader(holder.stickerIv, -1, null, expresser.smallPic);
        holder.stickerIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickedListener != null){
                    onItemClickedListener.onItemClicked(expresser);
                }
            }
        });
        // sbh
        holder.stickerIv.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                onItemLongClickListener.onItemLongClicked(expresser);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickedListener{
        void onItemClicked(Expresser expresser);
    }
    public interface OnItemLongClickListener{
        void onItemLongClicked(Expresser expresser);
    }

}
