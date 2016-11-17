package com.clover_studio.spikachatmodule.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clover_studio.spikachatmodule.ChatActivity;
import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.adapters.RecyclerExpressersAdapter;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.models.ExpresserCategory;
import com.clover_studio.spikachatmodule.utils.Const;

/**
 * Created by ubuntu_ivo on 23.03.16..
 */
public class CategoryExpressersFragment extends Fragment{

    private ExpresserCategory category;
    private RecyclerView rvStickers;
    private RecyclerExpressersAdapter adapter;

    public static CategoryExpressersFragment newInstance(ExpresserCategory category) {
        CategoryExpressersFragment fragment = new CategoryExpressersFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Const.Extras.STICKERS, category);
        fragment.setArguments(bundle);
        return fragment;
    }

    public CategoryExpressersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        category = (ExpresserCategory) getArguments().getSerializable(Const.Extras.STICKERS);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_category_stickers, container, false);

        //20 is sum of left and right margin of layout expressers menu
        int width = (int) (getResources().getDisplayMetrics().widthPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));

        //80 is sum of item width and left and right padding
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        int numColumns = width / size;

        rvStickers = (RecyclerView) rootView.findViewById(R.id.rvStickers);
        rvStickers.setLayoutManager(new GridLayoutManager(getActivity(), numColumns));
        adapter = new RecyclerExpressersAdapter(category.list);
        rvStickers.setAdapter(adapter);

        adapter.setListener(new RecyclerExpressersAdapter.OnItemClickedListener() {
            @Override
            public void onItemClicked(Expresser expresser) {
                if(getActivity() instanceof ChatActivity){
                    ((ChatActivity)getActivity()).selectExpresser(expresser);
                }
            }
        });
        adapter.setLongClickListener(new RecyclerExpressersAdapter.OnItemLongClickListener(){

            @Override
            public void onItemLongClicked(Expresser expresser) {
                if(getActivity() instanceof ChatActivity){
                    ((ChatActivity)getActivity()).selectExpresserEmotion(expresser);
                }
            }
        });


        return rootView;

    }

    public void refreshData(ExpresserCategory category){
        this.category.list.clear();
        this.category.list.addAll(category.list);
        if(rvStickers != null && adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

}
