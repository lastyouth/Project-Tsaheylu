package com.clover_studio.spikachatmodule.models;

import android.util.Pair;

import com.clover_studio.spikachatmodule.utils.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sbh on 2016-10-26.
 */
public class FacialEmotionModel {
    /**
     * height : 888
     * left : 402
     * top : 1344
     * width : 888
     *
     */
    public String capturedTimestamp;

    public FacialEmotionModel()
    {
        capturedTimestamp = Tools.generateDate("yyyy/MM/dd - HH:mm:ss",System.currentTimeMillis());
    }


    private FaceRectangleBean faceRectangle;
    /**
     * anger : 1.04674189E-4
     * contempt : 0.002808268
     * disgust : 3.03376673E-5
     * fear : 9.065066E-6
     * happiness : 8.2738894E-5
     * neutral : 0.9863173
     * sadness : 0.0101008937
     * surprise : 5.4674584E-4
     */

    private ScoresBean scores;

    public FaceRectangleBean getFaceRectangle() {
        return faceRectangle;
    }

    public void setFaceRectangle(FaceRectangleBean faceRectangle) {
        this.faceRectangle = faceRectangle;
    }

    public ScoresBean getScores() {
        return scores;
    }

    public void setScores(ScoresBean scores) {
        this.scores = scores;
    }

    public static class FaceRectangleBean {
        private int height;
        private int left;
        private int top;
        private int width;

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }
    }

    public static class ScoresBean {
        private double anger;
        private double contempt;
        private double disgust;
        private double fear;
        private double happiness;
        private double neutral;
        private double sadness;
        private double surprise;

        public double getAnger() {
            return anger;
        }

        public void setAnger(double anger) {
            this.anger = anger;
        }

        public double getContempt() {
            return contempt;
        }

        public void setContempt(double contempt) {
            this.contempt = contempt;
        }

        public double getDisgust() {
            return disgust;
        }

        public void setDisgust(double disgust) {
            this.disgust = disgust;
        }

        public double getFear() {
            return fear;
        }

        public void setFear(double fear) {
            this.fear = fear;
        }

        public double getHappiness() {
            return happiness;
        }

        public void setHappiness(double happiness) {
            this.happiness = happiness;
        }

        public double getNeutral() {
            return neutral;
        }

        public void setNeutral(double neutral) {
            this.neutral = neutral;
        }

        public double getSadness() {
            return sadness;
        }

        public void setSadness(double sadness) {
            this.sadness = sadness;
        }

        public double getSurprise() {
            return surprise;
        }

        public void setSurprise(double surprise) {
            this.surprise = surprise;
        }

        public Pair<Double,String> getBestScoredEmotion()
        {
            List<Pair<Double,String>> forsort = new ArrayList<Pair<Double,String>>();
            forsort.add(new Pair(anger,"anger"));
            //forsort.add(new Pair(contempt,"contempt"));
            //forsort.add(new Pair(disgust,"disgust"));
            //forsort.add(new Pair(fear,"fear"));
            forsort.add(new Pair(happiness,"happiness"));
            forsort.add(new Pair(neutral,"neutral"));
            forsort.add(new Pair(sadness, "sadness"));
            forsort.add(new Pair(surprise, "surprise"));

            Collections.sort(forsort, new Comparator<Pair<Double, String>>() {

                @Override
                public int compare(Pair<Double, String> t1, Pair<Double, String> t2) {
                    return Double.compare(t2.first, t1.first);
                }
            });
            return forsort.get(0);
        }

        public Pair<Double,String> getBestIncrementScoredEmotionComparedWithPrevEmotion(FacialEmotionModel prev)
        {
            List<Pair<Double,String>> forsort = new ArrayList<Pair<Double,String>>();
            forsort.add(new Pair(anger - prev.getScores().anger,"anger"));
            //forsort.add(new Pair(contempt,"contempt"));
            //forsort.add(new Pair(disgust,"disgust"));
            //forsort.add(new Pair(fear,"fear"));
            forsort.add(new Pair(happiness - prev.getScores().happiness,"happiness"));
            forsort.add(new Pair(neutral - prev.getScores().neutral,"neutral"));
            forsort.add(new Pair(sadness - prev.getScores().sadness, "sadness"));
            forsort.add(new Pair(surprise - prev.getScores().surprise, "surprise"));

            Collections.sort(forsort, new Comparator<Pair<Double, String>>() {

                @Override
                public int compare(Pair<Double, String> t1, Pair<Double, String> t2) {
                    return Double.compare(t2.first, t1.first);
                }
            });
            return forsort.get(0);
        }
    }
}
