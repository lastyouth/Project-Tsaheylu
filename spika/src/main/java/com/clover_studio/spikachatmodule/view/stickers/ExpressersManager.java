package com.clover_studio.spikachatmodule.view.stickers;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.clover_studio.spikachatmodule.R;
import com.clover_studio.spikachatmodule.adapters.ExpressersPagerAdapter;
import com.clover_studio.spikachatmodule.base.SingletonLikeApp;
import com.clover_studio.spikachatmodule.fragments.CategoryExpressersFragment;
import com.clover_studio.spikachatmodule.models.GetExpressersData;
import com.clover_studio.spikachatmodule.models.ExpresserCategory;
import com.clover_studio.spikachatmodule.utils.AnimUtils;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.UtilsImage;
import com.clover_studio.spikachatmodule.view.circularview.animation.SupportAnimator;
import com.clover_studio.spikachatmodule.view.circularview.animation.ViewAnimationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu_ivo on 24.07.15..
 */
public class ExpressersManager {

    private RelativeLayout rlExpressersMain;
    private LinearLayout llForExpressersCategory;
    private ViewPager vpExpressers;
    private HorizontalScrollView hsvExpressers;
    private SupportAnimator expressersAnimator;
    private boolean firstShowRecent = false;

    private OnExpressersManageListener listener;

    public void setStickersLayout(Activity activity, int stickersLayoutId, OnExpressersManageListener listener){

        rlExpressersMain = (RelativeLayout) activity.findViewById(stickersLayoutId);
        llForExpressersCategory = (LinearLayout) rlExpressersMain.findViewById(R.id.llStickersCategory);
        vpExpressers = (ViewPager) rlExpressersMain.findViewById(R.id.stickersViewPager);
        hsvExpressers = (HorizontalScrollView) rlExpressersMain.findViewById(R.id.hvStickersCategory);
        this.listener = listener;

    }

    public void openMenu(ImageButton btnExpressers){

        ((View) rlExpressersMain.getParent().getParent()).setVisibility(View.VISIBLE);

        // get the center for the clipping circle
        int cx = btnExpressers.getRight();
        int cy = rlExpressersMain.getBottom();

        // get the final radius for the clipping circle
        int finalRadius = Math.max(rlExpressersMain.getWidth(), rlExpressersMain.getHeight());

        expressersAnimator = ViewAnimationUtils.createCircularReveal(rlExpressersMain, cx, cy, 0, finalRadius + 300);
        expressersAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        expressersAnimator.setDuration(Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);
        expressersAnimator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationEnd() {
                listener.onExpressersOpened();
            }

            @Override
            public void onAnimationCancel() {
            }

            @Override
            public void onAnimationRepeat() {
            }
        });
        expressersAnimator.start();

        handleButtonsOnOpen();

    }

    protected void handleButtonsOnOpen(){

        int start = 150;
        int offset = 50;

//        singleButtonAnimationOn(audio, start + 0 * offset);
//        singleButtonAnimationOn(contact, start + 1 * offset);
//        singleButtonAnimationOn(location, start + 2 * offset);
//        singleButtonAnimationOn(video, start + 3 * offset);
//        singleButtonAnimationOn(gallery, start + 4 * offset);
//        singleButtonAnimationOn(file, start + 5 * offset);
//        singleButtonAnimationOn(camera, start + 6 * offset);

    }

    protected void singleButtonAnimationOn(final View view, int offset){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
                AnimUtils.scale(view, 0.6f, 1, Const.AnimationDuration.MENU_BUTTON_ANIMATION_DURATION, null);
            }
        }, offset);
    }

    public void closeMenu(){

        if(expressersAnimator == null) {
            return;
        }

        expressersAnimator = expressersAnimator.reverse();
        if(expressersAnimator != null){
            expressersAnimator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {}

                @Override
                public void onAnimationEnd() {
                    listener.onExpressersClosed();
                    ((View) rlExpressersMain.getParent().getParent()).setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel() {}

                @Override
                public void onAnimationRepeat() {}
            });

            expressersAnimator.start();
        }

        handleButtonsOnClose();

    }

    protected void handleButtonsOnClose(){

//        singleButtonAnimationOff(audio, Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);
//        singleButtonAnimationOff(location, Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);
//        singleButtonAnimationOff(video, Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);
//        singleButtonAnimationOff(gallery, Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);
//        singleButtonAnimationOff(contact, Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);
//        singleButtonAnimationOff(camera, Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);
//        singleButtonAnimationOff(file, Const.AnimationDuration.MENU_LAYOUT_ANIMATION_DURATION);

    }

    protected void singleButtonAnimationOff(final View view, int offset){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        }, offset);
    }

    public void setExpressers(GetExpressersData data, FragmentManager fm){
        llForExpressersCategory.getChildAt(0).setSelected(true);
        llForExpressersCategory.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOfCategory(-1);
            }
        });
        int position = 0;

        firstShowRecent = true;
        List<Fragment> expressersFragmentList = new ArrayList<>();
        ExpresserCategory recentCategory = SingletonLikeApp.getInstance().getSharedPreferences(llForExpressersCategory.getContext()).getStickersLikeObject();
        if(recentCategory == null){
            recentCategory = new ExpresserCategory();
            recentCategory.list = new ArrayList<>();
            firstShowRecent = false;
        }
        expressersFragmentList.add(CategoryExpressersFragment.newInstance(recentCategory));

        for(final ExpresserCategory category : data.data.expressers){
            ImageView ivExpresserCategory = new ImageView(llForExpressersCategory.getContext());
            llForExpressersCategory.addView(ivExpresserCategory);
            float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, llForExpressersCategory.getContext().getResources().getDisplayMetrics());
            float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, llForExpressersCategory.getContext().getResources().getDisplayMetrics());
            ivExpresserCategory.getLayoutParams().height = (int) size;
            ivExpresserCategory.getLayoutParams().width = (int) size;
            ivExpresserCategory.setPadding((int) padding, (int) padding, (int) padding, (int) padding);
            ivExpresserCategory.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivExpresserCategory.setBackgroundResource(R.drawable.selector_stickers_category);
            if(category.isOnline) {
                UtilsImage.setImageWithLoader(ivExpresserCategory, -1, null, category.mainPic);
            }
            else
            {
                ivExpresserCategory.setImageResource(category.targetResource);
            }
            //LogCS.e(Const.TAG, "mainPic : " + category.mainPic);
            ivExpresserCategory.setTag(position);
            position++;
            ivExpresserCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickOfCategory((int) v.getTag());
                }
            });

            expressersFragmentList.add(CategoryExpressersFragment.newInstance(category));
            /*if(position == 1)
            {
                break;
            }*/
        }

        ExpressersPagerAdapter pagerAdapter = new ExpressersPagerAdapter(fm, llForExpressersCategory.getContext(), expressersFragmentList);
        vpExpressers.setAdapter(pagerAdapter);
        vpExpressers.addOnPageChangeListener(onPageChanged);

        if(!firstShowRecent && vpExpressers.getChildCount() > 0){
            vpExpressers.setCurrentItem(1);
        }
    }

    private ViewPager.OnPageChangeListener onPageChanged = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            selectExpressersPack(position - 1);
            float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, llForExpressersCategory.getContext().getResources().getDisplayMetrics());
            if(position >= 4){
                hsvExpressers.smoothScrollTo((int) (size * position), 0);
            }else{
                hsvExpressers.smoothScrollTo(0, 0);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    public void clickOfCategory(int position){
        vpExpressers.setCurrentItem(position + 1);
        selectExpressersPack(position);
    }

    public void selectExpressersPack(int position){
        for(int i = 0; i < llForExpressersCategory.getChildCount(); i++){
            View view = llForExpressersCategory.getChildAt(i);
            view.setSelected(false);
        }
        llForExpressersCategory.getChildAt(position + 1).setSelected(true);
    }

    public void refreshRecent(){
        ExpresserCategory recentCategory = SingletonLikeApp.getInstance().getSharedPreferences(llForExpressersCategory.getContext()).getStickersLikeObject();
        ((CategoryExpressersFragment) ((ExpressersPagerAdapter) vpExpressers.getAdapter()).getItem(0)).refreshData(recentCategory);
    }
}
