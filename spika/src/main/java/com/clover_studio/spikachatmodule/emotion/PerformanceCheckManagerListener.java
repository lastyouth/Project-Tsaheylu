package com.clover_studio.spikachatmodule.emotion;

/**
 * Created by sbh on 2016-12-09.
 */

public interface PerformanceCheckManagerListener {
    void checkStart(String emotion);
    void checkEnd(String time, String emotion);
}
