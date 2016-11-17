package com.clover_studio.spikachatmodule.models;

import com.clover_studio.spikachatmodule.base.BaseModel;
import com.clover_studio.spikachatmodule.utils.Const;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ubuntu_ivo on 17.07.15..
 */
public class ExpresserCategory extends BaseModel implements Serializable{

    public String mainPic;
    public List<Expresser> list;
    // sbh
    public int expresserType = Const.ExpresserType.EXPRESSER_STICKER;
    public boolean isOnline = true;
    public int targetResource;

    @Override
    public String toString() {
        return "ExpresserCategory{" +
                "mainPic='" + mainPic + '\'' +
                ", list=" + list +
                '}';
    }
}
