package com.clover_studio.spikachatmodule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.clover_studio.spikachatmodule.adapters.StickerClassificationRecyclerViewAdapter;
import com.clover_studio.spikachatmodule.base.BaseActivity;
import com.clover_studio.spikachatmodule.models.Sticker;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.view.CustomTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 11/6/16.
 */

public class StickerClassificationActivity extends BaseActivity {
    private RecyclerView mStickerClassificationMain;

    public static void startStickerClassificationActivity(Context context, ArrayList<Sticker> stickers){
        Bundle bundle = new Bundle();
        bundle.putSerializable("stickers",stickers);
        Intent intent = new Intent(context, StickerClassificationActivity.class);
        intent.putExtra("stickersbundle",bundle);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sticker_classification);

        setToolbar(R.id.tToolbar, R.layout.custom_users_in_chat_toolbar);
        setMenuLikeBack();

        CustomTextView ctv = (CustomTextView)findViewById(R.id.toolbarTitle);
        ctv.setText("Sticker Classification");

        mStickerClassificationMain = (RecyclerView)findViewById(R.id.stickersInfoMain);

        Bundle fromCallerBundle = getIntent().getBundleExtra("stickersbundle");

        if(fromCallerBundle == null)
        {
            Log.e(Const.TAG,"Mandatory informations are missing");
            finish();
        }
        List<Sticker> data = (ArrayList<Sticker>)fromCallerBundle.getSerializable("stickers");
        mStickerClassificationMain.setLayoutManager(new LinearLayoutManager(this));
        mStickerClassificationMain.setAdapter(new StickerClassificationRecyclerViewAdapter(this));

        ((StickerClassificationRecyclerViewAdapter)mStickerClassificationMain.getAdapter()).addData(data);

        super.onCreate(savedInstanceState);
    }
}
