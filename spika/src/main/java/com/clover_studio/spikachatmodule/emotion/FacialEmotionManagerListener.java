package com.clover_studio.spikachatmodule.emotion;

import com.clover_studio.spikachatmodule.models.FacialEmotionModel;

/**
 * Created by sbh on 2016-10-26.
 */
public interface FacialEmotionManagerListener {
    void facialEmotionRecognitionFinished(FacialEmotionModel m, boolean success);
}
