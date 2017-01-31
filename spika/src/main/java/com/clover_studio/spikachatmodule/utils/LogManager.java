package com.clover_studio.spikachatmodule.utils;

import android.os.Environment;
import android.util.Log;

import com.clover_studio.spikachatmodule.emotion.HeartSensorFromWearManager;
import com.clover_studio.spikachatmodule.models.FacialEmotionModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by ECLmcKang on 2017-01-31.
 */

public class LogManager {
    //    private static final String TAG = "HSMFromWearManager";
    public static String TAG = "FileManagerForLogFile";
    public static String STRSAVEPATH = Environment.getExternalStorageDirectory()+"/LogFolder/";
    public static String SAVEFILEPATH = "FinalResult.txt";
    public static String SAVEFILEPATH2 = "RawResult.txt";

    File dir;
    File finalFile; //For final experiment Log
    File rawFile; //For analyzing raw data log

    public LogManager(String roomID){

        dir = makeDirectory(STRSAVEPATH);
        //파일 생성

//        activeUser.toString();
//        System.out.println("User ID Test : " + activeUser.getUserID());
//        System.out.println("Room ID Test : "+ activeUser.getRoomID());

        finalFile = makeFile(dir, (STRSAVEPATH+roomID+"_"+SAVEFILEPATH));
        rawFile = makeFile(dir, (STRSAVEPATH+roomID+"_"+SAVEFILEPATH2));
        //절대 경로

        Log.i(Const.TAG, ""+getAbsolutePath(dir));
        Log.i(Const.TAG, ""+getAbsolutePath(finalFile));
        Log.i(Const.TAG, ""+getAbsolutePath(dir));
        Log.i(Const.TAG, ""+getAbsolutePath(rawFile));
    }


    /**
     * 디렉토리 생성
     * @return dir
     */
    public File makeDirectory(String dir_path){
        File dir = new File(dir_path);
        if (!dir.exists())
        {
            dir.mkdirs();
            Log.i(TAG , "!dir.exists" );
        }else{
            Log.i(TAG , "dir.exists" );
        }

        return dir;
    }

    /**
     * 파일 생성
     * @param dir
     * @return file
     */
    public File makeFile(File dir , String file_path){
        File file = null;
        boolean isSuccess = false;
        if(dir.isDirectory()){
            file = new File(file_path);
            if(file!=null&&!file.exists()){
                Log.i(TAG , "!file.exists" );
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    Log.i(TAG, "FileCreate = " + isSuccess);
                }
            }else{
                Log.i(TAG , "file.exists" );
            }
        }
        return file;
    }

    /**
     * (dir/file) 절대 경로 얻어오기
     * @param file
     * @return String
     */
    public String getAbsolutePath(File file){
        return ""+file.getAbsolutePath();
    }

    /**
     * (dir/file) 삭제 하기
     * @param file
     */
    public boolean deleteFile(File file){
        boolean result;
        if(file!=null&&file.exists()){
            file.delete();
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    /**
     * 파일여부 체크 하기
     * @param file
     * @return
     */
    public boolean isFile(File file){
        boolean result;
        if(file!=null&&file.exists()&&file.isFile()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 디렉토리 여부 체크 하기
     * @param dir
     * @return
     */
    public boolean isDirectory(File dir){
        boolean result;
        if(dir!=null&&dir.isDirectory()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 파일 존재 여부 확인 하기
     * @param file
     * @return
     */
    public boolean isFileExist(File file){
        boolean result;
        if(file!=null&&file.exists()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 파일 이름 바꾸기
     * @param file
     */
    public boolean reNameFile(File file , File new_name){
        boolean result;
        if(file!=null&&file.exists()&&file.renameTo(new_name)){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 디렉토리에 안에 내용을 보여 준다.
     //     * @param file
     * @return
     */
    public String[] getList(File dir){
        if(dir!=null&&dir.exists())
            return dir.list();
        return null;
    }

    /**
     * 파일에 내용 쓰기
     * @param file
     * @param file_content
     * @return
     */
    public boolean writeFile(File file , byte[] file_content){
        boolean result;
        FileOutputStream fos;
        if(file!=null&&file.exists()&&file_content!=null){
            try {
                fos = new FileOutputStream(file, true);
                try {
                    fos.write(file_content);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    public void writeLog(String content){
        writeFile(rawFile, content.getBytes());
        writeFile(finalFile, content.getBytes());

    }


    ////////////////// For Experiment Log ////////////////////
    public void writeExperimentLog(String emotion, String flag, int happinessCount, int angerCount, int surpriseCount, int sadnessCount, int neutralCount){
        String content = "\n--------------------Experiment ("+ emotion+") "+ flag +"-------------------------\n";
        writeLog(content);
        content = "\nBefore Result --- Happiness : " + happinessCount + " Anger : " + angerCount + " Surprise : "+ surpriseCount + " Sadness : " + sadnessCount + " Neutral : " + neutralCount + "\n";
        writeLog(content);

    }

    public void writeRawData(String finalResult, String preEmotionResult, String RelativeEmotion, String AbsoluteEmotion,FacialEmotionModel m, HeartSensorFromWearManager msr){
        String content = new String();
        Calendar time = Calendar.getInstance();
        long now = time.getTimeInMillis();
        content = content = time.getTime().toString() + " available : "+ msr.getAvailableFlag()+ "\n";
        writeLog(content);
        content = "Final Result : " + finalResult + " Pre Result : " + preEmotionResult + " Relative Result : " + RelativeEmotion + " Absolute Result : " + AbsoluteEmotion + "\n";
        writeLog(content);
        content = "Happiness : " + m.getScores().getHappiness() + " Surprise : " + m.getScores().getSurprise() +
                " Angry : " + m.getScores().getAnger() + " Sadness " + m.getScores().getSadness() + " Neutral : " + m.getScores().getNeutral() + "\n";
        writeLog(content);
        content = "Queue HR : " + msr.getHRList() +"\n";
        writeLog(content);
        content = "Average HR : " + msr.getAverageHRList() +"\n";
        writeLog(content);
        content = "Max of average HR : " + msr.getMaxOfAverageHRList() + "\n";
        writeLog(content);
        content = "Min of average HR : " + msr.getMinOfAverageHRList() + "\n";
        writeLog(content);
    }


    public void writeTouchEvent(){
        String content = new String();
        Calendar time = Calendar.getInstance();
        long now = time.getTimeInMillis();
        content = "\nTouch Event occur --- "+time.getTime().toString() + " --- User touches the expressor function \n\n";
        writeLog(content);

        //KMC mckang
    }




}
