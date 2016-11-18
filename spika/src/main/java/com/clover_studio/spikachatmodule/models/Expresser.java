package com.clover_studio.spikachatmodule.models;

import com.clover_studio.spikachatmodule.base.BaseModel;
import com.clover_studio.spikachatmodule.utils.Const;

import java.io.Serializable;

/**
 * Created by ubuntu_ivo on 17.07.15..
 */
public class Expresser extends BaseModel implements Serializable{

    public String fullPic = null;
    public String smallPic = null;
    // sbh
    public int expresserType = Const.ExpresserType.EXPRESSER_STICKER;
    public boolean isOnline = true;
    public int targetResource = -1;

    //for saving
    public int timesClicked = 0;

    @Override
    public String toString() {
        return "Expresser{" +
                "fullPic='" + fullPic + '\'' +
                ", smallPic='" + smallPic + '\'' +
                ", timesClicked=" + timesClicked +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Expresser expresser = (Expresser) o;

        if (fullPic != null ? !fullPic.equals(expresser.fullPic) : expresser.fullPic != null) return false;
        return !(smallPic != null ? !smallPic.equals(expresser.smallPic) : expresser.smallPic != null);

    }
    public boolean there_is_no_cow_level()
    {
        if(smallPic == null && fullPic == null && targetResource == -1)
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = fullPic != null ? fullPic.hashCode() : 0;
        result = 31 * result + (smallPic != null ? smallPic.hashCode() : 0);
        result = 31 * result + timesClicked;
        return result;
    }
}
