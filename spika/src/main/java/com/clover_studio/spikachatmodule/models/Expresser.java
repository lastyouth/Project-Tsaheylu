package com.clover_studio.spikachatmodule.models;

import com.clover_studio.spikachatmodule.base.BaseModel;

import java.io.Serializable;

/**
 * Created by ubuntu_ivo on 17.07.15..
 */
public class Expresser extends BaseModel implements Serializable{

    public String fullPic;
    public String smallPic;
    // sbh
    public boolean isOnline = true;
    public int targetResource;

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

    @Override
    public int hashCode() {
        int result = fullPic != null ? fullPic.hashCode() : 0;
        result = 31 * result + (smallPic != null ? smallPic.hashCode() : 0);
        result = 31 * result + timesClicked;
        return result;
    }
}
