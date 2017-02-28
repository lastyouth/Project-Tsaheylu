package com.clover_studio.spikachatmodule;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clover_studio.spikachatmodule.adapters.MessageRecyclerViewAdapter;
import com.clover_studio.spikachatmodule.adapters.SettingsAdapter;
import com.clover_studio.spikachatmodule.api.retrofit.CustomResponse;
import com.clover_studio.spikachatmodule.api.retrofit.SpikaOSRetroApiInterface;
import com.clover_studio.spikachatmodule.base.BaseActivity;
import com.clover_studio.spikachatmodule.base.SingletonLikeApp;
import com.clover_studio.spikachatmodule.dialogs.InfoMessageDialog;
import com.clover_studio.spikachatmodule.dialogs.NotifyDialog;
import com.clover_studio.spikachatmodule.dialogs.PreviewMessageDialog;
import com.clover_studio.spikachatmodule.dialogs.ExpresserClassificationDialog;
import com.clover_studio.spikachatmodule.emotion.EffectManager;
import com.clover_studio.spikachatmodule.emotion.ExpresserCandidateManager;
import com.clover_studio.spikachatmodule.emotion.FacialEmotionManager;
import com.clover_studio.spikachatmodule.emotion.FacialEmotionManagerListener;
import com.clover_studio.spikachatmodule.emotion.HeartSensorFromWearManager;
import com.clover_studio.spikachatmodule.emotion.HeartSensorManager;
import com.clover_studio.spikachatmodule.emotion.PerformanceCheckManager;
import com.clover_studio.spikachatmodule.emotion.PerformanceCheckManagerListener;
import com.clover_studio.spikachatmodule.emotion.VibrationManager;
import com.clover_studio.spikachatmodule.managers.socket.SocketManager;
import com.clover_studio.spikachatmodule.managers.socket.SocketManagerListener;
import com.clover_studio.spikachatmodule.models.Attributes;
import com.clover_studio.spikachatmodule.models.Config;
import com.clover_studio.spikachatmodule.models.EstimatedEmotionModel;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.models.ExpresserCategory;
import com.clover_studio.spikachatmodule.models.FacialEmotionModel;
import com.clover_studio.spikachatmodule.models.GetMessagesModel;
import com.clover_studio.spikachatmodule.models.GetExpressersData;
import com.clover_studio.spikachatmodule.models.Login;
import com.clover_studio.spikachatmodule.models.Message;
import com.clover_studio.spikachatmodule.models.ParsedUrlData;
import com.clover_studio.spikachatmodule.models.SendTyping;
import com.clover_studio.spikachatmodule.models.User;
import com.clover_studio.spikachatmodule.utils.AnimUtils;
import com.clover_studio.spikachatmodule.utils.ApplicationStateManager;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.CustomImageDownloader;
import com.clover_studio.spikachatmodule.utils.EmitJsonCreator;
import com.clover_studio.spikachatmodule.utils.ErrorHandle;
import com.clover_studio.spikachatmodule.utils.LogCS;
import com.clover_studio.spikachatmodule.utils.LogManager;
import com.clover_studio.spikachatmodule.utils.ParseUrlLinkMetadata;
import com.clover_studio.spikachatmodule.utils.SeenByUtils;
import com.clover_studio.spikachatmodule.utils.Tools;
import com.clover_studio.spikachatmodule.utils.UtilsImage;
import com.clover_studio.spikachatmodule.view.menu.MenuManager;
import com.clover_studio.spikachatmodule.view.menu.OnMenuManageListener;
import com.clover_studio.spikachatmodule.view.stickers.OnExpressersManageListener;
import com.clover_studio.spikachatmodule.view.stickers.ExpressersManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;


public class ChatActivity extends BaseActivity {

    private User activeUser;

    private ListView settingsListView;
    protected RecyclerView rvMessages;
    protected TextView tvTyping;

    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnExpressers;
    private ImageButton btnEmotion;
    private ProgressBar pbAboveSend;
    private ButtonType buttonType = ButtonType.MENU;
    private ExpressersType expressersType = ExpressersType.CLOSED;
    private TypingType typingType = TypingType.BLANK;
    private TextView newMessagesButton;
    private VibrationManager mVibrationManager;

    // mckang


    ////////////////////////// mckang ///////////////////////////////
    private boolean isExperiment = false;
    private HeartSensorFromWearManager msr;
    private LogManager mLogManager;
    ////////////////////////////////////////////////////////////////////////////////

    // for candidate
    private LinearLayout mEmotionTransferInterface;
    private View.OnClickListener mExpressersCandidateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            int emotionType = (Integer)v.getTag();

            ArrayList<Expresser> candidates = ExpresserCandidateManager.getInstance().getExpresserList(emotionType);

            if(candidates == null)
            {
                // not initialized or wrong emotionType
                Toast.makeText(getActivity(),"Online expressers are not initialized! Try again",Toast.LENGTH_SHORT).show();
                return;
            }

            Expresser expresser = candidates.get(id);

            if(expresser.there_is_no_cow_level())
            {
                Toast.makeText(getActivity(),"Empty Slot!",Toast.LENGTH_SHORT).show();
            }
            else {
                sendExpresser(expresser);
            }

            //Toast.makeText(getActivity(),"imageView : "+id,Toast.LENGTH_SHORT).show();

            if(mEmotionTransferInterface.getVisibility() == View.VISIBLE)
            {
                mEmotionTransferInterface.setVisibility(View.GONE);


                mEmotionTransferInterface.removeAllViews();

            }
        }
    };


    protected MenuManager menuManager;
    protected ExpressersManager expressersManager;
    protected List<String> sentMessages = new ArrayList<>();
    protected List<User> typingUsers = new ArrayList<>();


    // sbh : check bluetooth
    private HeartSensorManager mHSManager;

    // sbh : recent emotion
    static private EstimatedEmotionModel mRecentEmotion = new EstimatedEmotionModel();

    // jsk : additional variables
    static private long mEmotionDurationCounter = 0;
    //static private final long DEFAULT_EMOTION_DURATION_DELAY = 2; // Delay twice
    static private final long DEFAULT_EMOTION_DURATION_DELAY = 0; // Delay twice
    private int mPreviousEmotion;

    //Jesung Kim_20170214_START
    private String mPrevRelativeEmotion = "neutral";
    //Jesung Kim_20170214_END

    private String preEmotionResult = "pre";

    //mckang
    private int happinessCount = 0;
    private int angerCount = 0;
    private int surpriseCount = 0 ;
    private int sadnessCount = 0;
    private int neutralCount = 0;

    //mckang
    private int positiveHRCount = 0;
    private int neutralHRCount = 0;
    private int negativeHRCount = 0;


    // sbh : prev Facial emotion

    private FacialEmotionModel mPrevFacialEmotion = null;

    // for loaded expressers
    private GetExpressersData mLoadedExpressersData;

    //data from last paging
    protected List<Message> lastDataFromServer = new ArrayList<>();

    //for scroll when keyboard opens
    protected int lastVisibleItem = 0;

    // is socket closed on pause
    private boolean pausedForSocket = false;
    // don't close socket when open camera or location or audio activity
    private boolean forceStaySocket = false;
    // first time resume called
    private boolean firstTime = true;

    //message queue for unsent message when socket is not connected
    private List<Message> unSentMessageList = new ArrayList<>();


    //message queue for new message from latest api when listView is not at bottom
    private List<Message> unReadMessage = new ArrayList<>();

    public enum ButtonType {
        MENU, SEND, MENU_OPENED, IN_ANIMATION;
    }

    public enum ExpressersType {
        CLOSED, OPENED, IN_ANIMATION;
    }

    public enum TypingType {
        TYPING, BLANK;
    }

    // callback for FacialEmotionManager
    private FacialEmotionManagerListener mFEMListener = new FacialEmotionManagerListener() {
        @Override
        public void facialEmotionRecognitionFinished(final FacialEmotionModel m, boolean success) {

            if(success) {
                Log.d(Const.TAG,"ChatActivity : Succeeded to receive result");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Pair<Double, String> baseResult;
                        //JesungKim 20161130
                        Pair<Double, String> AbsoluteResult;
                        Pair<Double, String> RelativeResult;

                        String preEmotion = "pre";
                        String newEmotion =  "new";

                        if(mPrevFacialEmotion != null) {
                            //JesungKim 20161130
                            RelativeResult = m.getScores().getBestIncrementScoredEmotionComparedWithPrevEmotion(mPrevFacialEmotion);
                            AbsoluteResult = m.getScores().getBestScoredEmotion();
                            mPreviousEmotion = Const.Emotion.EMOTION_NEUTRAL; //Reset?
                        }
                        else
                        {
                            //JesungKim 20161130
                            RelativeResult = m.getScores().getBestScoredEmotion();
                            AbsoluteResult = m.getScores().getBestScoredEmotion();
                        }

                        mPrevFacialEmotion = m;
                        //initialize problem mckang?
                        int classifiedemotion = Const.Emotion.CURRENT_EMOTION;
                        //JesungKim 20161130
                        String RelativeEmotion = RelativeResult.second;
                        String AbsoluteEmotion = AbsoluteResult.second;
                        String finalResult = "final";



                        //JesungKim 20161130
                        //if(mEmotionDurationCounter > 0) {
                        //    mEmotionDurationCounter--;
                        //}

                        //JesungKim 20170203 - "Discarded Internal Function"
                        /*
                        if (RelativeEmotion.equals("happiness") || AbsoluteEmotion.equals("happiness")) {
                            if(preEmotionResult.equals("angry")){
                                btnEmotion.setImageResource(R.drawable.ic_shy);
                                classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
                                finalResult = "neutral";
                            }else{
                                btnEmotion.setImageResource(R.drawable.ic_happy);
                                classifiedemotion = Const.Emotion.EMOTION_HAPPINESS;
                                finalResult = "happiness";
                            }
                        } else if (RelativeEmotion.equals("surprise") || AbsoluteEmotion.equals("surprise")) {
                            if(preEmotionResult.equals("angry")){
                                btnEmotion.setImageResource(R.drawable.ic_shy);
                                classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
                                finalResult = "neutral";
                            }else{
                                btnEmotion.setImageResource(R.drawable.ic_surprise);
                                classifiedemotion = Const.Emotion.EMOTION_SURPRISE;
                                finalResult = "surprise";
                            }
                        } else if (RelativeEmotion.equals("angry") || AbsoluteEmotion.equals("angry")) {
                            btnEmotion.setImageResource(R.drawable.ic_angry);
                            classifiedemotion = Const.Emotion.EMOTION_ANGRY;
                            finalResult = "angry";
                        } else if (AbsoluteEmotion.equals("sadness") || AbsoluteEmotion.equals("sadness")) {
                            if(preEmotionResult.equals("angry")){
                                btnEmotion.setImageResource(R.drawable.ic_shy);
                                classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
                                finalResult = "neutral";
                            }else{
                                btnEmotion.setImageResource(R.drawable.ic_sad);
                                classifiedemotion = Const.Emotion.EMOTION_SADNESS;
                                finalResult = "sadness";
                            }
                        } else if (AbsoluteEmotion.equals("neutral") || AbsoluteEmotion.equals("neutral")) {
                            //Using Current and Pre heart rate data
                            //Compare with Average (mean) variation
                            //Compare with Max variation
                            //Compare with Min variation



                        } else {
                            btnEmotion.setImageResource(R.drawable.ic_shy);
                            classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
                                                   finalResult = "neutral";
                        }

                        //JesungKim 20161130
                        if (mRecentEmotion.mFinalEstimatedEmotion != classifiedemotion) {
                            mEmotionDurationCounter = DEFAULT_EMOTION_DURATION_DELAY;
                        }
                        */

                        //JesungKim 20170223 - Revised Recognition Mechanism
//                        String currentState = msr.getCurrentState();    //Get HR Result First

                        String currentState = mHSManager.getCurrentState();    //Get HR Result First
                        Log.i(Const.TAG,"Current State : " + currentState);

                        Log.e(Const.TAG,"JesungKim -- HR QUEUE : "+ mHSManager.getHeartDate().getHR());

                        if (currentState.equals("positive")) {
                            Log.e(Const.TAG,"JesungKim -- HR: (" + currentState + ")");

                            if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                //finalResult = "neutral";
                                if ( RelativeEmotion.equals(mPrevRelativeEmotion) ){
                                    finalResult = "sadness";
                                }
                                else {
                                    finalResult = "neutral";
                                }
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }

                            else {
                                Log.e(Const.TAG,"JesungKim -- Unexpected Facial Emotion Result [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                        }

                        else if (currentState.equals("neutral") || currentState.equals("unavailable")) {
                            Log.e(Const.TAG,"JesungKim -- HR: (" + currentState + ")");

                            if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                //finalResult = "sadness";
                                if ( RelativeEmotion.equals(mPrevRelativeEmotion) ){
                                    finalResult = "sadness";
                                }
                                else {
                                    finalResult = "surprise";
                                }
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                //finalResult = "sadness";
                                if ( RelativeEmotion.equals(mPrevRelativeEmotion) ){
                                    finalResult = "sadness";
                                }
                                else {
                                    finalResult = "angry";
                                }
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                //finalResult = "sadness";
                                if ( RelativeEmotion.equals(mPrevRelativeEmotion) ){
                                    finalResult = "sadness";
                                }
                                else {
                                    finalResult = "neutral";
                                }
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }

                            else {
                                Log.e(Const.TAG,"JesungKim -- Unexpected Facial Emotion Result [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                        }



                        else if (currentState.equals("negative")) {
                            Log.e(Const.TAG,"JesungKim -- HR: (" + currentState + ")");

                            if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "happiness";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                //finalResult = "angry";
                                if ( RelativeEmotion.equals(mPrevRelativeEmotion) ){
                                    finalResult = "sadness";
                                }
                                else {
                                    finalResult = "angry";
                                }
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("happiness") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "surprise";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                                else if ( AbsoluteEmotion.equals("surprise") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }
                            else if ( AbsoluteEmotion.equals("sadness") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "sadness";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                //finalResult = "angry";
                                if ( RelativeEmotion.equals(mPrevRelativeEmotion) ){
                                    finalResult = "sadness";
                                }
                                else {
                                    finalResult = "angry";
                                }
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("anger") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }

                            /****************************************/
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("happiness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("surprise") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("sadness") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                //finalResult = "sadness";
                                if ( RelativeEmotion.equals(mPrevRelativeEmotion) ){
                                    finalResult = "sadness";
                                }
                                else {
                                    finalResult = "angry";
                                }
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("anger") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }
                            else if ( AbsoluteEmotion.equals("neutral") && RelativeEmotion.equals("neutral") ) {
                                Log.e(Const.TAG,"JesungKim -- [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "angry";
                            }

                            else {
                                Log.e(Const.TAG,"JesungKim -- Unexpected Facial Emotion Result [" + AbsoluteEmotion + "|" + RelativeEmotion + "]");
                                finalResult = "neutral";
                            }
                        }
                        else {
                            Log.e(Const.TAG,"JesungKim -- Case[0]: Unexpected HR Result (" + currentState + ")");
                        }

                        //Jesung Kim 20170203
                        //Determine Final Emotion State
                        if (finalResult.equals("happiness")) {
                            Log.e(Const.TAG,"JesungKim -- finalResult: [" + finalResult + "]");
                            btnEmotion.setImageResource(R.drawable.ic_happy);
                            classifiedemotion = Const.Emotion.EMOTION_HAPPINESS;
                        }
                        else if (finalResult.equals("surprise")) {
                            Log.e(Const.TAG,"JesungKim -- finalResult: [" + finalResult + "]");
                            btnEmotion.setImageResource(R.drawable.ic_surprise);
                            classifiedemotion = Const.Emotion.EMOTION_SURPRISE;
                        }
                        else if (finalResult.equals("sadness")) {
                            Log.e(Const.TAG,"JesungKim -- finalResult: [" + finalResult + "]");
                            btnEmotion.setImageResource(R.drawable.ic_sad);
                            classifiedemotion = Const.Emotion.EMOTION_SADNESS;
                        }
                        else if (finalResult.equals("angry")) {
                            Log.e(Const.TAG,"JesungKim -- finalResult: [" + finalResult + "]");
                            btnEmotion.setImageResource(R.drawable.ic_angry);
                            classifiedemotion = Const.Emotion.EMOTION_ANGRY;
                        }
                        else if (finalResult.equals("neutral")) {
                            Log.e(Const.TAG,"JesungKim -- finalResult: [" + finalResult + "]");
                            btnEmotion.setImageResource(R.drawable.ic_shy);
                            classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
                        }
                        else {
                            Log.e(Const.TAG,"JesungKim -- Unexpected finalResult: [" + finalResult + "]");
                            btnEmotion.setImageResource(R.drawable.ic_shy);
                            classifiedemotion = Const.Emotion.EMOTION_NEUTRAL;
                        }

                        //JesungKim_20170214_START
                        mPrevRelativeEmotion = RelativeResult.second;
                        //JesungKim_20170214_END


                        mRecentEmotion.mFinalEstimatedEmotion = classifiedemotion;
                        mRecentEmotion.mWeight = RelativeResult.first;


                        //mckang
                        //For Log

                        if(isExperiment==true) {

                            //For Log data log
//                            mLogManager.writeRawData(finalResult,preEmotion, RelativeEmotion, AbsoluteEmotion, m, msr);
                            mLogManager.writeRawData(finalResult,preEmotion, RelativeEmotion, AbsoluteEmotion, m, mHSManager);
                            if(currentState.equals("positive")){
                                positiveHRCount++;
                            }else if(currentState.equals("negative")){
                                negativeHRCount++;
                            }else if(currentState.equals("neutral")){
                                neutralHRCount++;
                            }
                        }


                        //JesungKim 20170223 - Move to Upper Region

                        /*
                        //NewCodeMCkang
                        //mckang test average and state
                        String currentState = msr.getCurrentState();
                        Log.i(Const.TAG,"Current State : " + currentState);
                        */
                    }
                });
            }
            else
            {
                Log.e(Const.TAG,"ChatActivity : Something wrong! discard this data");
            }
        }
    };

    // for checking performance
    private PerformanceCheckManager mPCManager = null;
    private PerformanceCheckManagerListener mPCMListener = new PerformanceCheckManagerListener() {
        @Override
        public void checkStart(String emotion) {
            final Message message = new Message();
            String data = new String();

            data = String.format("다음 감정을 떠올리고 표현해주세요 : %s",emotion);
            message.fillMessageForSend(activeUser, data, Const.MessageType.TYPE_TEXT,null);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(SocketManager.getInstance().isSocketConnect()){
                        JSONObject emitMessage = EmitJsonCreator.createEmitSendMessage(message);
                        SocketManager.getInstance().emitMessage(Const.EmitKeyWord.SEND_MESSAGE, emitMessage);
                        onMessageSent(message);
                    }
                }
            });
        }

        @Override
        public void checkEnd(String time, String emotion) {
            final Message message = new Message();
            String data = new String();

            data = String.format("%s 님이 '%s' 감정을 표현하기까지 걸린 시간 %s",activeUser.name,emotion,time);
            message.fillMessageForSend(activeUser, data, Const.MessageType.TYPE_TEXT,null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(SocketManager.getInstance().isSocketConnect()){
                        JSONObject emitMessage = EmitJsonCreator.createEmitSendMessage(message);
                        SocketManager.getInstance().emitMessage(Const.EmitKeyWord.SEND_MESSAGE, emitMessage);
                        onMessageSent(message);
                    }
                }
            });

        }
    };

    /**
     * start chat activity with user data
     *
     * @param context
     * @param user    user to login
     */
    public static void startChatActivity(Context context, User user) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Const.Extras.USER, user);
        context.startActivity(intent);
    }

    public static void startChatActivityWithConfig(Context context, User user, Config config) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Const.Extras.USER, user);
        intent.putExtra(Const.Extras.CONFIG, config);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        //move this to application class of application
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", SingletonLikeApp.getInstance().getConfig(getActivity()).apiBaseUrl);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .build();

        ImageLoaderConfiguration configImageLoader = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPoolSize(3)
                .defaultDisplayImageOptions(defaultOptions)
                .imageDownloader(new CustomImageDownloader(getActivity()))
                .build();
        ImageLoader.getInstance().init(configImageLoader);
        //***************************************************

        SingletonLikeApp.getInstance().setApplicationState(getActivity());


        if (getIntent().hasExtra(Const.Extras.CONFIG)) {
            Config config = getIntent().getParcelableExtra(Const.Extras.CONFIG);
            SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).setConfig(config);
            SingletonLikeApp.getInstance().setConfig(config);
        } else {
            Config config = new Config("", "");
            SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).setConfig(config);
            SingletonLikeApp.getInstance().setConfig(null);
        }

        setToolbar(R.id.tToolbar, R.layout.custom_chat_toolbar);
        setMenuLikeBack();
        onSettingsButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingsClicked();
            }
        });

        rvMessages = (RecyclerView) findViewById(R.id.rvMain);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        rvMessages.setLayoutManager(llm);

        settingsListView = (ListView) findViewById(R.id.settings_list_view);
        SettingsAdapter adapter = new SettingsAdapter(this);
        settingsListView.setAdapter(adapter);
        adapter.setSettings();
        settingsListView.setOnItemClickListener(onSettingItemClick);

        btnSend = (ImageButton) findViewById(R.id.btnSend);
        pbAboveSend = (ProgressBar) findViewById(R.id.loadingAboveSendButton);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMenuButtonClicked();
            }
        });
        // always true
        animateSendButton(true);

        btnExpressers = (ImageButton) findViewById(R.id.btnStickers);
        btnExpressers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExpressersButtonClicked();
            }
        });



        //sbh
        mEmotionTransferInterface = (LinearLayout)findViewById(R.id.candidates);
        btnEmotion = (ImageButton)findViewById(R.id.btnEmotion);

        btnEmotion.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                onEmotionButtonClicked();
            }
        });



        etMessage = (EditText) findViewById(R.id.etMessage);
        etMessage.addTextChangedListener(etMessageTextWatcher);

        tvTyping = (TextView) findViewById(R.id.toolbarSubtitle);

        menuManager = new MenuManager();
        menuManager.setMenuLayout(this, R.id.menuMain, onMenuManagerListener);

        expressersManager = new ExpressersManager();
        expressersManager.setStickersLayout(this, R.id.stickersMain, onExpressersManageListener);

        //check for user
        if (!getIntent().hasExtra(Const.Extras.USER)) {
            noUserDialog();
            return;
        } else {
            activeUser = getIntent().getParcelableExtra(Const.Extras.USER);
            if (activeUser == null) {
                noUserDialog();
                return;
            }
        }

        tvTyping.setText(activeUser.userID);

        rvMessages.setAdapter(new MessageRecyclerViewAdapter(new ArrayList<Message>(), activeUser));
        ((MessageRecyclerViewAdapter) rvMessages.getAdapter()).setLastItemListener(onLastItemAndClickItemListener);

        setToolbarTitle(activeUser.roomID);

        findViewById(R.id.viewForSettingBehind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSettings();
            }
        });

        findViewById(R.id.viewForMenuBehind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expressersType == ExpressersType.OPENED) {
                    onExpressersButtonClicked();
                } else if (buttonType == ButtonType.MENU_OPENED) {
                    onButtonMenuOpenedClicked();
                }
            }
        });

        login(activeUser);

        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                lastVisibleItem = ((LinearLayoutManager) rvMessages.getLayoutManager()).findLastVisibleItemPosition();
                if (newMessagesButton.getVisibility() == View.VISIBLE) {
                    AnimUtils.fadeThenGoneOrVisible(newMessagesButton, 1, 0, 250);
                }
            }
        });

        newMessagesButton = (TextView) findViewById(R.id.newMessagesButton);
        newMessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollRecyclerToBottomWithAnimation();
            }
        });

        //for background state
        IntentFilter intentFilter = new IntentFilter(ApplicationStateManager.APPLICATION_PAUSED);
        intentFilter.addAction(ApplicationStateManager.APPLICATION_RESUMED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverImplementation, intentFilter);

        // initialize FacialManager
        FacialEmotionManager.getInstance().initializeFacialManager(mFEMListener);

        // initialize HeartSensorManager
        //mckang modify
        mHSManager = new HeartSensorManager(this,Const.Emotion.MAX_QUEUED_DATA_FOR_HRV);
        mHSManager.checkBluetooth();

        // initialize HeartSensorFromWearManager
        //mckang modify
//        msr = HeartSensorFromWearManager.getInstance(this);
//        msr.startMeasurement();

        // initialize EffectManager
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        EffectManager.getInstance().initialize(this,displaySize.x,displaySize.y);

        // initialize VibrationManager
        mVibrationManager = new VibrationManager(this);

        activeUser.toString();
        System.out.println("User ID Test : " + activeUser.getUserID());
        System.out.println("Room ID Test : "+ activeUser.getRoomID());

        ///////////////////// mckang /////////////////////////////////

        mLogManager = new LogManager(activeUser.getRoomID());

        ////////////////////////////////////////////////////////////////


    }

    @Override
    protected void onResume() {
        super.onResume();

        /*if(firstTime){
            //progress is visible, and it is showed in login method
//            doNotShowProgressNow = true;
//            boolean isInit = true;
//            String lastMessageId = null;
//            getMessages(isInit, lastMessageId);
//
//            firstTime = false;

            //load data after login to socket (after get access token)
        }else{
            if(pausedForSocket){
                MessageRecyclerViewAdapter adapter = (MessageRecyclerViewAdapter) rvMessages.getAdapter();
                String lastMessageId = adapter.getNewestMessageId();
                getLatestMessages(lastMessageId);
            }
        }

        if (pausedForSocket) {
            SocketManager.getInstance().setListener(socketListener);
            SocketManager.getInstance().tryToReconnect(getActivity());

            pausedForSocket = false;
        }
        forceStaySocket = false;
        FacialEmotionManager.getInstance().initializeTimer(FacialEmotionManager.DEFAULT_INTERVAL);*/
    }

    @Override
    protected void onPause() {
        // don't pause the socket when app's state changes to 'BACKGROUND'
        /*if (!forceStaySocket) {
            SocketManager.getInstance().closeAndDisconnectSocket();
            pausedForSocket = true;
        }
        FacialEmotionManager.getInstance().releaseTimer();*/
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        SocketManager.getInstance().closeAndDisconnectSocket();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverImplementation);
        if(FacialEmotionManager.getInstance().isInitialized())
        {
            FacialEmotionManager.getInstance().releaseFacialManager();
        }
        //mckang modify
        mHSManager.disconnect();
        // stop service
//        HeartSensorFromWearManager msr = HeartSensorFromWearManager.getInstance(this);
//
//        msr.stopMeasurement();

        super.onDestroy();
    }

    protected OnMenuManageListener onMenuManagerListener = new OnMenuManageListener() {
        @Override
        public void onMenuOpened() {
            buttonType = ButtonType.MENU_OPENED;
        }

        @Override
        public void onMenuClosed() {
            buttonType = ButtonType.MENU;
            etMessage.setEnabled(true);
            findViewById(R.id.viewForMenuBehind).setVisibility(View.GONE);
        }
    };

    protected OnExpressersManageListener onExpressersManageListener = new OnExpressersManageListener() {
        @Override
        public void onExpressersOpened() {
            expressersType = ExpressersType.OPENED;
        }

        @Override
        public void onExpressersClosed() {
            expressersType = ExpressersType.CLOSED;
            etMessage.setEnabled(true);
            findViewById(R.id.viewForMenuBehind).setVisibility(View.GONE);
        }
    };

    private AdapterView.OnItemClickListener onSettingItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//            data.add("Happiness");
//            data.add("Anger");
//            data.add("Surprise");
//            data.add("Sadness");
//            data.add("Neutral");

            if (position == 0) {
                forceStaySocket = true;
                UsersInChatActivity.starUsersInChatActivity(getActivity(), activeUser.roomID);
            }
            // sbh added
            else if(position == 1)
            {

                if(isExperiment == false){
                    Toast.makeText(getApplicationContext(),"Experiment (Happiness) Start",Toast.LENGTH_SHORT).show();
                    isExperiment = true;

                    mLogManager.writeExperimentLog("Happiness","Start", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);

                    happinessCount = 0;
                    angerCount = 0;
                    surpriseCount = 0;
                    sadnessCount = 0;
                    neutralCount = 0;

                   mHSManager.clearQueue();

                }else{
                    Toast.makeText(getApplicationContext(),"Experiment (Happiness) End",Toast.LENGTH_SHORT).show();
                    isExperiment = false;

                    mLogManager.writeExperimentLog("Happiness","End", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);
                    //mckang for log
                    String content = "HR results --- Positive : "+ positiveHRCount + " Negative : "+ negativeHRCount+ " Neutral : " + neutralHRCount+"\n";
                    mLogManager.writeLog(content);
                    positiveHRCount = 0;
                    negativeHRCount = 0;
                    neutralHRCount = 0;

                }
            }else if(position == 2){

                if(isExperiment == false){
                    Toast.makeText(getApplicationContext(),"Experiment (Anger) Start",Toast.LENGTH_SHORT).show();
                    isExperiment = true;

                    mLogManager.writeExperimentLog("Anger","Start", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);


                    happinessCount = 0;
                    angerCount = 0;
                    surpriseCount = 0;
                    sadnessCount = 0;
                    neutralCount = 0;

                    mHSManager.clearQueue();

                }else{
                    Toast.makeText(getApplicationContext(),"Experiment (Anger) End",Toast.LENGTH_SHORT).show();
                    isExperiment = false;

                    mLogManager.writeExperimentLog("Anger","End", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);

                    //mckang for log
                    String content = "HR results --- Positive : "+ positiveHRCount + " Negative : "+ negativeHRCount+ " Neutral : " + neutralHRCount+"\n";
                    mLogManager.writeLog(content);
                    positiveHRCount = 0;
                    negativeHRCount = 0;
                    neutralHRCount = 0;

                }


            }else if(position == 3){

                if(isExperiment == false){
                    Toast.makeText(getApplicationContext(),"Experiment (Surprise) Start",Toast.LENGTH_SHORT).show();
                    isExperiment = true;

                    mLogManager.writeExperimentLog("Surprise","Start", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);

                    happinessCount = 0;
                    angerCount = 0;
                    surpriseCount = 0;
                    sadnessCount = 0;
                    neutralCount = 0;

                    mHSManager.clearQueue();

                }else{
                    Toast.makeText(getApplicationContext(),"Experiment (Surprise) End",Toast.LENGTH_SHORT).show();
                    isExperiment = false;

                    mLogManager.writeExperimentLog("Surprise","End", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);

                    //mckang for log
                    String content = "HR results --- Positive : "+ positiveHRCount + " Negative : "+ negativeHRCount+ " Neutral : " + neutralHRCount+"\n";
                    mLogManager.writeLog(content);
                    positiveHRCount = 0;
                    negativeHRCount = 0;
                    neutralHRCount = 0;
                }

            }else if(position ==4){

                if(isExperiment == false){
                    Toast.makeText(getApplicationContext(),"Experiment (Sadness) Start",Toast.LENGTH_SHORT).show();
                    isExperiment = true;

                    mLogManager.writeExperimentLog("Sadness","Start", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);

                    happinessCount = 0;
                    angerCount = 0;
                    surpriseCount = 0;
                    sadnessCount = 0;
                    neutralCount = 0;

                    mHSManager.clearQueue();

                }else{
                    Toast.makeText(getApplicationContext(),"Experiment (Sadness) End",Toast.LENGTH_SHORT).show();
                    isExperiment = false;

                    mLogManager.writeExperimentLog("Sadness","End", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);

                    //mckang for log
                    String content = "HR results --- Positive : "+ positiveHRCount + " Negative : "+ negativeHRCount+ " Neutral : " + neutralHRCount+"\n";
                    mLogManager.writeLog(content);
                    positiveHRCount = 0;
                    negativeHRCount = 0;
                    neutralHRCount = 0;
                }

            }else if(position == 5){
                if(isExperiment == false){
                    Toast.makeText(getApplicationContext(),"Experiment (Neutral) Start",Toast.LENGTH_SHORT).show();
                    isExperiment = true;

                    mLogManager.writeExperimentLog("Neutral","Start", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);


                    happinessCount = 0;
                    angerCount = 0;
                    surpriseCount = 0;
                    sadnessCount = 0;
                    neutralCount = 0;

                    mHSManager.clearQueue();

                }else{
                    Toast.makeText(getApplicationContext(),"Experiment (Neutral) End",Toast.LENGTH_SHORT).show();
                    isExperiment = false;

                    mLogManager.writeExperimentLog("Neutral","End", happinessCount, angerCount, surpriseCount,sadnessCount,neutralCount);


                    //mckang for log
                    String content = "HR results --- Positive : "+ positiveHRCount + " Negative : "+ negativeHRCount+ " Neutral : " + neutralHRCount+"\n";
                    mLogManager.writeLog(content);
                    positiveHRCount = 0;
                    negativeHRCount = 0;
                    neutralHRCount = 0;
                }
            }else if(position == 6) {
                Toast.makeText(getApplicationContext(),"잠시만 기다리세요. 곧 시작합니다.",Toast.LENGTH_SHORT).show();
                mPCManager = new PerformanceCheckManager(mPCMListener,-1);
            }
            hideSettings();
        }
    };

    protected TextWatcher etMessageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            /*if (s.length() == 0) {
                animateSendButton(false);
            } else {
                animateSendButton(true);
            }*/
            sendTypingType(s.length());
        }
    };

    /**
     * login user api
     *
     * @param user user to login
     */
    private void login(User user) {

        handleProgress(true);
        SpikaOSRetroApiInterface retroApiInterface = getRetrofit().create(SpikaOSRetroApiInterface.class);
        Call<Login> call = retroApiInterface.login(user);
        call.enqueue(new CustomResponse<Login>(getActivity(), true, true) {

            @Override
            public void onCustomSuccess(Call<Login> call, Response<Login> response) {
                doNotHideProgressNow = true;
                super.onCustomSuccess(call, response);
                SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).setToken(response.body().data.token);
                SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).setUserId(response.body().data.token);

                if (TextUtils.isEmpty(activeUser.avatarURL)) {
                    activeUser.avatarURL = response.body().data.user.avatarURL;
                }
                connectToSocket();

                //progress is visible, and it is showed in login method
                if (firstTime) {
                    doNotShowProgressNow = true;
                    boolean isInit = true;
                    String lastMessageId = null;
                    getMessages(isInit, lastMessageId);

                    firstTime = false;
                }
                loadExpressers();
            }

        });

    }

    /**
     * get messages from server
     *
     * @param isInit        true - initial call, false call on paging or on resume
     * @param lastMessageId id of last message (for paging can be null)
     */
    private void getMessages(final boolean isInit, String lastMessageId) {
        handleProgress(true);

        if (TextUtils.isEmpty(lastMessageId)) {
            lastMessageId = "0";
        }
        SpikaOSRetroApiInterface retroApiInterface = getRetrofit().create(SpikaOSRetroApiInterface.class);
        Call<GetMessagesModel> call = retroApiInterface.getMessages(activeUser.roomID, lastMessageId, SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).getToken());
        call.enqueue(new CustomResponse<GetMessagesModel>(getActivity(), true, true) {

            @Override
            public void onCustomSuccess(Call<GetMessagesModel> call, Response<GetMessagesModel> response) {
                super.onCustomSuccess(call, response);
                lastDataFromServer.clear();
                lastDataFromServer.addAll(response.body().data.messages);
                MessageRecyclerViewAdapter adapter = (MessageRecyclerViewAdapter) rvMessages.getAdapter();
                if (isInit) {
                    adapter.clearMessages();
                    lastVisibleItem = response.body().data.messages.size();
                }
                boolean isPaging = !isInit;
                adapter.addMessages(response.body().data.messages, isPaging);
//                    if (isInit) {
//                        scrollRecyclerToBottom();
//                    } else {
//                        int scrollToPosition = lastDataFromServer.size();
//                        scrollRecyclerToPosition(scrollToPosition);
//                    }

                List<String> unReadMessages = SeenByUtils.getUnSeenMessages(response.body().data.messages, activeUser);
                sendOpenMessage(unReadMessages);
            }

        });

    }

    /**
     * get new messages
     *
     * @param lastMessageId id of newest message
     */
    private void getLatestMessages(String lastMessageId) {

        if (TextUtils.isEmpty(lastMessageId)) {
            lastMessageId = "0";
        }
        SpikaOSRetroApiInterface retroApiInterface = getRetrofit().create(SpikaOSRetroApiInterface.class);
        Call<GetMessagesModel> call = retroApiInterface.getLatestMessages(activeUser.roomID, lastMessageId, SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).getToken());
        call.enqueue(new CustomResponse<GetMessagesModel>(getActivity(), false, false) {

            @Override
            public void onCustomSuccess(Call<GetMessagesModel> call, Response<GetMessagesModel> response) {
                super.onCustomSuccess(call, response);
                if (response.body().data.messages.size() == 0) {
                    return;
                }

                boolean toScrollBottom = false;
                LinearLayoutManager llManager = (LinearLayoutManager) rvMessages.getLayoutManager();
                if (llManager.findLastVisibleItemPosition() == rvMessages.getAdapter().getItemCount() - 1) {
                    toScrollBottom = true;
                }

                MessageRecyclerViewAdapter adapter = (MessageRecyclerViewAdapter) rvMessages.getAdapter();
                adapter.addLatestMessages(response.body().data.messages);
                lastVisibleItem = adapter.getItemCount();

                List<String> unReadMessages = SeenByUtils.getUnSeenMessages(response.body().data.messages, activeUser);
                sendOpenMessage(unReadMessages);

                if (toScrollBottom) {
                    scrollRecyclerToBottom();
                } else {
                    if (newMessagesButton.getVisibility() == View.GONE) {
                        AnimUtils.fadeThenGoneOrVisible(newMessagesButton, 0, 1, 250);
                    }
                }
            }
        });

    }

    protected MessageRecyclerViewAdapter.OnLastItemAndOnClickListener onLastItemAndClickItemListener = new MessageRecyclerViewAdapter.OnLastItemAndOnClickListener() {
        @Override
        public void onLastItem() {
            LogCS.e("LOG", "LAST ITEM");
            if (lastDataFromServer.size() < 50) {
                //no more paging
                LogCS.e("LOG", "NO MORE MESSAGES");
            } else {
                if (lastDataFromServer.size() > 0) {
                    String lastMessageId = lastDataFromServer.get(lastDataFromServer.size() - 1)._id;
                    boolean isInit = false;
                    getMessages(isInit, lastMessageId);
                }
            }
        }

        @Override
        public void onClickItem(final Message item) {
            if (item.deleted != -1 && item.deleted != 0) {
                return;
            }

            if(item.attributes != null && item.attributes.linkData != null){
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.attributes.linkData.url));
                startActivity(browserIntent);
            }else{
                // do nothing for now
            }

        }

        @Override
        public void onLongClick(Message item) {
            boolean showDelete = true;
            if(!activeUser.userID.equals(item.user.userID) ||
                    item.type == Const.MessageType.TYPE_NEW_USER ||
                    item.type == Const.MessageType.TYPE_USER_LEAVE){

                showDelete = false;
            }

            boolean showCopy = true;
            if(item.type != Const.MessageType.TYPE_TEXT){
                showCopy = false;
            }

            boolean showShare = false;

            boolean showRepeat = false;

            if(item.expresser != null && (item.expresser.expresserType == Const.ExpresserType.EXPRESSER_VIBRATION || item.expresser.expresserType == Const.ExpresserType.EXPRESSER_EFFECT))
            {
                showRepeat = true;
            }

            InfoMessageDialog.startDialogWithOptions(getActivity(), item, activeUser, showCopy, showDelete, showShare,showRepeat, new InfoMessageDialog.OnInfoListener() {
                @Override
                public void onDeleteMessage(Message message, Dialog dialog) {
                    confirmDeleteMessage(message);
                }

                @Override
                public void onDetailsClicked(Message message, Dialog dialog) {
                    openMessageInfoDialog(message);
                }

                @Override
                public void onRepeatClicked(Message message, Dialog dialog) {
                    if(message.expresser != null) {
                        if(message.expresser.expresserType == Const.ExpresserType.EXPRESSER_VIBRATION)
                        {
                            mVibrationManager.performVibration(message.expresser);
                        }
                        else if(message.expresser.expresserType == Const.ExpresserType.EXPRESSER_EFFECT) {
                            EffectManager.getInstance().performEffect(message.expresser);
                        }
                    }
                }
            });

        }
    };

    private void openMessageInfoDialog(Message message) {
        PreviewMessageDialog.startDialog(getActivity(), message, activeUser);
    }

    private void confirmDeleteMessage(final Message message) {
        NotifyDialog dialog = NotifyDialog.startConfirm(getActivity(), getString(R.string.delete_message_title), getString(R.string.delete_message_text));
        dialog.setTwoButtonListener(new NotifyDialog.TwoButtonDialogListener() {
            @Override
            public void onOkClicked(NotifyDialog dialog) {
                dialog.dismiss();
                sendDeleteMessage(message._id);
            }

            @Override
            public void onCancelClicked(NotifyDialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.setButtonsText(getString(R.string.NO_CAPITAL), getString(R.string.YES_CAPITAL));
    }

    private void animateSendButton(final boolean toSend) {
        if (toSend && buttonType == ButtonType.SEND) {
            return;
        }
        if (toSend) {
            buttonType = ButtonType.SEND;
        } else {
            buttonType = ButtonType.MENU;
        }
        AnimUtils.fade(btnSend, 1, 0, 100, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (toSend) {
                    btnSend.setImageResource(R.drawable.send);
                } else {
                    btnSend.setImageResource(R.drawable.attach);
                }
                AnimUtils.fade(btnSend, 0, 1, 100, null);
            }
        });
    }

    protected void onSendMenuButtonClicked() {
        if (buttonType == ButtonType.MENU) {
            onButtonMenuClicked();
        } else if (buttonType == ButtonType.MENU_OPENED) {
            onButtonMenuOpenedClicked();
        } else if (buttonType == ButtonType.SEND) {
            onButtonSendClicked();
        }
    }

    protected void onExpressersButtonClicked() {
        if (expressersType == ExpressersType.CLOSED) {
            if(expressersType == ExpressersType.IN_ANIMATION){
                return;
            }
            etMessage.setEnabled(false);
            expressersType = ExpressersType.IN_ANIMATION;
            expressersManager.openMenu(btnExpressers);
            findViewById(R.id.viewForMenuBehind).setVisibility(View.VISIBLE);
        } else if (expressersType == ExpressersType.OPENED) {
            if(expressersType == ExpressersType.IN_ANIMATION){
                return;
            }
            expressersType = ExpressersType.IN_ANIMATION;
            expressersManager.closeMenu();
        }
    }
    protected void onEmotionButtonClicked()
    {
        // get final estimatedEmotion
        int emotionType = mRecentEmotion.mFinalEstimatedEmotion;

        ArrayList<Expresser> mCurrentEmtionExpressers = ExpresserCandidateManager.getInstance().getExpresserList(emotionType);

        if(mCurrentEmtionExpressers == null)
        {
            Toast.makeText(this,"Expressers are not ready now",Toast.LENGTH_SHORT).show();

            return;
        }
        if(mEmotionTransferInterface.getVisibility() == View.GONE) {
            ImageView[] mImageView = new ImageView[Const.Emotion.MAX_EXPRESSER_CANDIDATE];
            float size = 0;
            for(int i=0;i<Const.Emotion.MAX_EXPRESSER_CANDIDATE;i++)
            {
                Expresser expresser = mCurrentEmtionExpressers.get(i);

                mImageView[i] = new ImageView(mEmotionTransferInterface.getContext());
                mEmotionTransferInterface.addView(mImageView[i],i);

                mImageView[i].setId(i);
                mImageView[i].setTag(emotionType);
                mImageView[i].setOnClickListener(mExpressersCandidateClickListener);

                size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mEmotionTransferInterface.getContext().getResources().getDisplayMetrics());

                mImageView[i].getLayoutParams().width = (int)size;
                mImageView[i].getLayoutParams().height = (int)size;

                if(expresser.there_is_no_cow_level())
                {
                    mImageView[i].setImageResource(R.drawable.ic_nosticker);
                }
                else
                {
                    if(expresser.isOnline)
                    {
                        UtilsImage.setImageWithLoader(mImageView[i], -1, null, expresser.smallPic);
                    }
                    else
                    {
                        mImageView[i].setImageResource(expresser.targetResource);
                    }

                }
            }
            mEmotionTransferInterface.getLayoutParams().width = (int)(size)*(Const.Emotion.MAX_EXPRESSER_CANDIDATE);
            mEmotionTransferInterface.setVisibility(View.VISIBLE);
        }
        else
        {
            mEmotionTransferInterface.setVisibility(View.GONE);
        }
    }

    private void onButtonMenuClicked() {
        if (buttonType == ButtonType.IN_ANIMATION) {
            return;
        }
        etMessage.setEnabled(false);
        buttonType = ButtonType.IN_ANIMATION;

        menuManager.openMenu(btnSend);
        findViewById(R.id.viewForMenuBehind).setVisibility(View.VISIBLE);
    }

    private void onButtonMenuOpenedClicked() {
        if (buttonType == ButtonType.IN_ANIMATION) {
            return;
        }
        buttonType = ButtonType.IN_ANIMATION;

        menuManager.closeMenu();
    }

    protected void onButtonSendClicked() {
        String text = etMessage.getText().toString();
        if(text != null && !text.equals("")) {
            sendMessage();
        }
    }

    private void showSettings() {
        settingsListView.setVisibility(View.VISIBLE);
        AnimUtils.fade(settingsListView, 0, 1, 300, null);
        findViewById(R.id.viewForSettingBehind).setVisibility(View.VISIBLE);
        AnimUtils.fade(findViewById(R.id.viewForSettingBehind), 0, 1, 300, null);
    }

    private void hideSettings() {
        AnimUtils.fade(settingsListView, 1, 0, 300, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                settingsListView.setVisibility(View.INVISIBLE);
                findViewById(R.id.viewForSettingBehind).setVisibility(View.GONE);
            }
        });
        AnimUtils.fade(findViewById(R.id.viewForSettingBehind), 1, 0, 300, null);
    }

    protected void onSettingsClicked() {
        if (settingsListView.getVisibility() == View.VISIBLE) {
            hideSettings();
        } else {
            showSettings();
        }
    }

    private void scrollRecyclerToBottom() {
        rvMessages.scrollToPosition(rvMessages.getAdapter().getItemCount() - 1);
    }

    private void scrollRecyclerToBottomWithAnimation() {
        rvMessages.smoothScrollToPosition(rvMessages.getAdapter().getItemCount() - 1);
    }

    private void scrollRecyclerToPosition(int pos) {
        int offset = getResources().getDisplayMetrics().heightPixels;
        ((LinearLayoutManager) rvMessages.getLayoutManager()).scrollToPositionWithOffset(pos, 0);
    }

    //*********** send message to socket method

    /**
     * send message type text
     */
    protected void sendMessage() {

        //***************************parse link*******************//
        boolean hasLink = false;
        String textMessage = etMessage.getText().toString();
        String checkForLink = Tools.checkForLink(textMessage);
        if(checkForLink != null){
            hasLink = true;
            //set hasLink to true when attributes implements on api
        }

        final Message message = new Message();
        message.fillMessageForSend(activeUser, etMessage.getText().toString(), Const.MessageType.TYPE_TEXT,null);
        if(hasLink){
            btnSend.setVisibility(View.INVISIBLE);
            pbAboveSend.setVisibility(View.VISIBLE);
            new ParseUrlLinkMetadata(checkForLink, new ParseUrlLinkMetadata.OnUrlParsed() {
                @Override
                public void onUrlParsed(ParsedUrlData data) {

                    btnSend.setVisibility(View.VISIBLE);
                    pbAboveSend.setVisibility(View.GONE);

                    etMessage.setText("");

                    Attributes att = new Attributes();
                    att.linkData = data;
                    message.attributes = att;

                    if(SocketManager.getInstance().isSocketConnect()){
                        JSONObject emitMessage = EmitJsonCreator.createEmitSendMessage(message);
                        SocketManager.getInstance().emitMessage(Const.EmitKeyWord.SEND_MESSAGE, emitMessage);
                    }else{
                        unSentMessageList.add(message);
                    }

                    onMessageSent(message);
                }
            }).execute();
        }else{
            etMessage.setText("");

            if(SocketManager.getInstance().isSocketConnect()){
                JSONObject emitMessage = EmitJsonCreator.createEmitSendMessage(message);
                SocketManager.getInstance().emitMessage(Const.EmitKeyWord.SEND_MESSAGE, emitMessage);
            }else{
                unSentMessageList.add(message);
            }

            onMessageSent(message);
        }

    }


    /**
     * send expressers
     *
     * @param expresser   expresser to send
     */
    protected void sendExpresser(Expresser expresser) {
        Message message = new Message();
        message.fillMessageForSend(activeUser, expresser.smallPic, Const.MessageType.TYPE_EXPRESSER,expresser);

        if(SocketManager.getInstance().isSocketConnect()){
            JSONObject emitMessage = EmitJsonCreator.createEmitSendMessage(message);
            SocketManager.getInstance().emitMessage(Const.EmitKeyWord.SEND_MESSAGE, emitMessage);
        }else{
            unSentMessageList.add(message);
        }

        onMessageSent(message);

    }

    private void loginWithSocket() {
        JSONObject emitLogin = EmitJsonCreator.createEmitLoginMessage(activeUser);
        SocketManager.getInstance().emitMessage(Const.EmitKeyWord.LOGIN, emitLogin);

        ChatActivity.this.sendUnSentMessages();
    }

    private void sendTypingType(int length) {
        if (length > 0 && typingType == TypingType.BLANK) {
            setTyping(Const.TypingStatus.TYPING_ON);
            typingType = TypingType.TYPING;
        } else if (length == 0 && typingType == TypingType.TYPING) {
            setTyping(Const.TypingStatus.TYPING_OFF);
            typingType = TypingType.BLANK;
        }
    }

    private void setTyping(int type) {
        JSONObject emitSendTyping = EmitJsonCreator.createEmitSendTypingMessage(activeUser, type);
        SocketManager.getInstance().emitMessage(Const.EmitKeyWord.SEND_TYPING, emitSendTyping);
    }

    private void sendOpenMessage(String messageId) {
        List<String> messagesIds = new ArrayList<>();
        messagesIds.add(messageId);
        sendOpenMessage(messagesIds);
    }

    private void sendOpenMessage(List<String> messagesIds) {
        JSONObject emitOpenMessage = EmitJsonCreator.createEmitOpenMessage(messagesIds, activeUser.userID);
        SocketManager.getInstance().emitMessage(Const.EmitKeyWord.OPEN_MESSAGE, emitOpenMessage);
    }

    protected void sendDeleteMessage(String messageId) {
        JSONObject emitDeleteMessage = EmitJsonCreator.createEmitDeleteMessage(activeUser.userID, messageId);
        SocketManager.getInstance().emitMessage(Const.EmitKeyWord.DELETE_MESSAGE, emitDeleteMessage);
    }
    //****************************************************

    //on received message from socket

    private void onUserLeft(User user) {
        if (typingUsers.contains(user)) {
            typingUsers.remove(user);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (typingUsers.size() < 1) {
                        tvTyping.setText(activeUser.userID);
                    } else {
                        generateTypingString();
                    }
                }
            });
        }
    }

    private void onMessageSent(Message sendMessage) {
        MessageRecyclerViewAdapter adapter = (MessageRecyclerViewAdapter) rvMessages.getAdapter();
        adapter.addSentMessage(sendMessage);
        if(sendMessage.type == Const.MessageType.TYPE_EXPRESSER)
        {
            //This process must be logged
            //KMC mckang
            mLogManager.writeTouchEvent();
            //KMC mckang


            Expresser expresser = sendMessage.expresser;

            if(!expresser.there_is_no_cow_level() && !expresser.isOnline)
            {
                if(expresser.expresserType == Const.ExpresserType.EXPRESSER_EFFECT)
                {
                    EffectManager.getInstance().performEffect(expresser);
                }
                else if(expresser.expresserType == Const.ExpresserType.EXPRESSER_VIBRATION)
                {
                    mVibrationManager.performVibration(expresser);
                }
            }
            else
            {

            }
            if(mPCManager != null)
            {
                mPCManager.checkEndInternal();
                mPCManager = null;
            }
        }

        sentMessages.add(sendMessage.localID);
        lastVisibleItem = adapter.getItemCount();
        scrollRecyclerToBottom();
    }

    private void onMessageReceived(final Message message) {
        final MessageRecyclerViewAdapter adapter = (MessageRecyclerViewAdapter) rvMessages.getAdapter();
        if (sentMessages.contains(message.localID)) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.setDeliveredMessage(message);
                }
            });
            sentMessages.remove(message.localID);
        } else {
            message.status = Const.MessageStatus.RECEIVED;

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(message.message != null) {
                        if (message.message.contains("0x00start")) {
                            Toast.makeText(getApplicationContext(), "잠시만 기다리세요. 곧 시작합니다.", Toast.LENGTH_SHORT).show();
                            mPCManager = new PerformanceCheckManager(mPCMListener, 0);
                        } else if (message.message.equals("0x01start")) {
                            Toast.makeText(getApplicationContext(), "잠시만 기다리세요. 곧 시작합니다.", Toast.LENGTH_SHORT).show();
                            mPCManager = new PerformanceCheckManager(mPCMListener, 1);
                        } else if (message.message.equals("0x02start")) {
                            Toast.makeText(getApplicationContext(), "잠시만 기다리세요. 곧 시작합니다.", Toast.LENGTH_SHORT).show();
                            mPCManager = new PerformanceCheckManager(mPCMListener, 2);
                        } else if (message.message.equals("0x03start")) {
                            Toast.makeText(getApplicationContext(), "잠시만 기다리세요. 곧 시작합니다.", Toast.LENGTH_SHORT).show();
                            mPCManager = new PerformanceCheckManager(mPCMListener, 3);
                        }
                    }
                    boolean toScrollBottom = false;
                    LinearLayoutManager llManager = (LinearLayoutManager) rvMessages.getLayoutManager();
                    if(llManager.findLastVisibleItemPosition() == rvMessages.getAdapter().getItemCount() - 1){
                        toScrollBottom = true;
                    }
                    adapter.addReceivedMessage(message);
                    // check? sbh
                    if(message.type == Const.MessageType.TYPE_EXPRESSER)
                    {
                        Expresser expresser = message.expresser;

                        if(!expresser.there_is_no_cow_level() && !expresser.isOnline)
                        {
                            if(expresser.expresserType == Const.ExpresserType.EXPRESSER_EFFECT)
                            {
                                EffectManager.getInstance().performEffect(expresser);
                            }
                            else if(expresser.expresserType == Const.ExpresserType.EXPRESSER_VIBRATION)
                            {
                                mVibrationManager.performVibration(expresser);
                            }
                        }
                        else
                        {

                        }
                    }
                    else
                    {
                        //adapter.addReceivedMessage(message);
                    }


                    if(toScrollBottom) {
                        scrollRecyclerToBottom();
                    }else{
                        if(newMessagesButton.getVisibility() == View.GONE){
                            AnimUtils.fadeThenGoneOrVisible(newMessagesButton, 0, 1, 250);
                        }
                    }
                }
            });
            if (!message.user.userID.equals(activeUser.userID)) {
                sendOpenMessage(message._id);
            }
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lastVisibleItem = rvMessages.getAdapter().getItemCount();
            }
        });
    }

    private void onMessagesUpdated(final List<Message> messages) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageRecyclerViewAdapter adapter = (MessageRecyclerViewAdapter) rvMessages.getAdapter();
                adapter.updateMessages(messages);
            }
        });

    }

    private void onSocketError(final int code) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NotifyDialog dialog = NotifyDialog.startInfo(getActivity(), getString(R.string.error), ErrorHandle.getMessageForCode(code, getResources()));
            }
        });

    }

    private void onTyping(final SendTyping typing) {
        if (typing.user.userID.equals(activeUser.userID)) {
            return;
        }

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (typing.type == Const.TypingStatus.TYPING_OFF) {

                    if (typingUsers.contains(typing.user)) {
                        typingUsers.remove(typing.user);
                    }

                    if (typingUsers.size() < 1) {
                        tvTyping.setText(activeUser.userID);
                    } else {
                        generateTypingString();
                    }
                } else {

                    if (typingUsers.contains(typing.user)) {
                        typingUsers.remove(typing.user);
                    }

                    typingUsers.add(typing.user);
                    generateTypingString();
                }
            }
        });
    }

    private void checkToRemoveUser(User user) {
        for (User item : typingUsers) {
            if (item.userID.equals(user.userID)) {
                typingUsers.remove(item);
                return;
            }
        }
    }

    //******************************************

    /**
     * connect to socket
     */
    private void connectToSocket() {

        SocketManager.getInstance().setListener(socketListener);
        SocketManager.getInstance().connectToSocket(getActivity());

    }

    private SocketManagerListener socketListener = new SocketManagerListener() {
        @Override
        public void onConnect() {
            LogCS.w("LOG", "CONNECTED TO SOCKET");
        }

        @Override
        public void onSocketFailed() {
            ChatActivity.this.socketFailedDialog();
        }

        @Override
        public void onNewUser(Object... args) {
            Log.w("LOG", "new user, args" + args[0].toString());
        }

        @Override
        public void onLoginWithSocket() {
            ChatActivity.this.loginWithSocket();
        }

        @Override
        public void onUserLeft(User user) {
            ChatActivity.this.onUserLeft(user);
        }

        @Override
        public void onTyping(SendTyping typing) {
            ChatActivity.this.onTyping(typing);
        }

        @Override
        public void onMessageReceived(Message message) {
            ChatActivity.this.onMessageReceived(message);
        }

        @Override
        public void onMessagesUpdated(List<Message> messages) {
            ChatActivity.this.onMessagesUpdated(messages);
        }

        @Override
        public void onSocketError(int code) {
            ChatActivity.this.onSocketError(code);
        }
    };

    private void generateTypingString() {
        String typingText = "";
        for (User item : typingUsers) {
            typingText = typingText + item.name + ", ";
        }
        typingText = typingText.substring(0, typingText.length() - 2);

        if (typingUsers.size() > 1) {
            String typingTextSetText = typingText + " " + getString(R.string.are_typing);
            tvTyping.setText(typingTextSetText);
        } else {
            String typingTextSetText = typingText + " " + getString(R.string.is_typing);

            tvTyping.setText(typingTextSetText);
        }
    }

    protected void noUserDialog() {
        NotifyDialog dialog = NotifyDialog.startInfo(this, getString(R.string.user_error_title), getString(R.string.user_error_not_sent));
        dialog.setOneButtonListener(new NotifyDialog.OneButtonDialogListener() {
            @Override
            public void onOkClicked(NotifyDialog dialog) {
                dialog.dismiss();
                finish();
            }
        });
    }

    protected void socketFailedDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NotifyDialog dialog = NotifyDialog.startInfo(getActivity(), getString(R.string.socket_error_title), getString(R.string.socket_error_connect_failed));
                dialog.setOneButtonListener(new NotifyDialog.OneButtonDialogListener() {
                    @Override
                    public void onOkClicked(NotifyDialog dialog) {
                        dialog.dismiss();
                        finish();
                    }
                });
            }
        });
    }

    public void sendUnSentMessages(){
        for(Message item : unSentMessageList){
            JSONObject emitMessage = EmitJsonCreator.createEmitSendMessage(item);
            SocketManager.getInstance().emitMessage(Const.EmitKeyWord.SEND_MESSAGE, emitMessage);
        }
        unSentMessageList.clear();
    }

    @Override
    public void onBackPressed() {
        if (settingsListView.getVisibility() == View.VISIBLE) {
            hideSettings();
            return;
        }
        if (buttonType == ButtonType.MENU_OPENED) {
            onButtonMenuOpenedClicked();
            return;
        }
        if (expressersType == ExpressersType.OPENED) {
            onExpressersButtonClicked();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    //************** ui customization methods
    protected void changeToolbarColor(String color) {
        super.changeToolbarColor(color);
    }
    //******************************************

    //************** download and upload file

    //************************************************

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Const.PermissionCode.CHAT_STORAGE: {
                if (grantResults.length > 0 && Tools.checkGrantResults(grantResults)) {
                } else {
                    finish();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //************************************************

    public void requestContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        forceStaySocket = true;
        startActivityForResult(intent, Const.RequestCode.CONTACT_CHOOSE);
    }

    //******stuff for check if application is enter background
    private BroadcastReceiverImplementation broadcastReceiverImplementation = new BroadcastReceiverImplementation();
    private class BroadcastReceiverImplementation extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ApplicationStateManager.APPLICATION_PAUSED)) {
                LogCS.e("******* PAUSE *******");
                SocketManager.getInstance().closeAndDisconnectSocket();
                pausedForSocket = true;
            } else if (intent.getAction().equals(ApplicationStateManager.APPLICATION_RESUMED)) {
                LogCS.e("******* RESUMED *******" + getActivity().getClass().getName());
                if (pausedForSocket) {
                    SocketManager.getInstance().setListener(socketListener);
                    SocketManager.getInstance().tryToReconnect(getActivity());
                    pausedForSocket = false;
                }
            }
        }
    }

    //STICKERS
    private void loadExpressers() {

        SpikaOSRetroApiInterface retroApiInterface = getRetrofit().create(SpikaOSRetroApiInterface.class);
        Call<GetExpressersData> call = retroApiInterface.getStickers(SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).getToken());
        call.enqueue(new CustomResponse<GetExpressersData>(getActivity(), false, false) {

            @Override
            public void onCustomSuccess(Call<GetExpressersData> call, Response<GetExpressersData> response) {
                super.onCustomSuccess(call, response);
                GetExpressersData tmpData = response.body();
                mLoadedExpressersData = new GetExpressersData();
                mLoadedExpressersData.data = new GetExpressersData().new GetExpressersInsideData();
                mLoadedExpressersData.data.expressers = new ArrayList<ExpresserCategory>();


                // re-organizing all expressers.
                for(int i = 0; i<tmpData.data.expressers.size(); i++)
                {
                    if(i>3)
                    {
                        break;
                    }
                    mLoadedExpressersData.data.expressers.add(tmpData.data.expressers.get(i));
                }
                // effecter
                ExpresserCategory mEffect = new ExpresserCategory();

                mEffect.expresserType = Const.ExpresserType.EXPRESSER_EFFECT;
                mEffect.isOnline = false;
                mEffect.targetResource = R.drawable.ic_fireworks;
                mEffect.list = new ArrayList<Expresser>();

                // sample effecter

                Expresser effectGreenStar = new Expresser();

                effectGreenStar.isOnline = false;
                effectGreenStar.targetResource = R.drawable.ic_green_star;
                effectGreenStar.expresserType = Const.ExpresserType.EXPRESSER_EFFECT;

                mEffect.list.add(effectGreenStar);

                Expresser effectRedHeart = new Expresser();

                effectRedHeart.isOnline = false;
                effectRedHeart.targetResource = R.drawable.ic_red_heart;
                effectRedHeart.expresserType = Const.ExpresserType.EXPRESSER_EFFECT;

                mEffect.list.add(effectRedHeart);

                Expresser effectNeutral = new Expresser();

                effectNeutral.isOnline = false;
                effectNeutral.targetResource = R.drawable.ic_pokemon_neutral;
                effectNeutral.expresserType = Const.ExpresserType.EXPRESSER_EFFECT;

                mEffect.list.add(effectNeutral);

                Expresser effectSmile = new Expresser();

                effectSmile.isOnline = false;
                effectSmile.targetResource = R.drawable.ic_pokemon_smile;
                effectSmile.expresserType = Const.ExpresserType.EXPRESSER_EFFECT;

                mEffect.list.add(effectSmile);

                Expresser effectFlower = new Expresser();

                effectFlower.isOnline = false;
                effectFlower.targetResource = R.drawable.ic_colorful_flower;
                effectFlower.expresserType = Const.ExpresserType.EXPRESSER_EFFECT;

                mEffect.list.add(effectFlower);

                Expresser effectBomb = new Expresser();

                effectBomb.isOnline = false;
                effectBomb.targetResource = R.drawable.ic_bomb;
                effectBomb.expresserType = Const.ExpresserType.EXPRESSER_EFFECT;

                mEffect.list.add(effectBomb);



                // vibration

                ExpresserCategory mVibration = new ExpresserCategory();

                mVibration.expresserType = Const.ExpresserType.EXPRESSER_VIBRATION;
                mVibration.isOnline = false;
                mVibration.targetResource = R.drawable.ic_vibrate;
                mVibration.list = new ArrayList<Expresser>();

                Expresser effectVibration = new Expresser();

                effectVibration.isOnline = false;
                effectVibration.targetResource = R.drawable.ic_nuclear;
                effectVibration.expresserType = Const.ExpresserType.EXPRESSER_VIBRATION;

                mVibration.list.add(effectVibration);

                mLoadedExpressersData.data.expressers.add(mEffect);
                mLoadedExpressersData.data.expressers.add(mVibration);

                ExpresserCandidateManager.getInstance().initialize(getActivity());

                expressersManager.setExpressers(mLoadedExpressersData, getSupportFragmentManager());
            }

        });

    }

    public void selectExpresser(Expresser expresser){
        onExpressersButtonClicked();
        sendExpresser(expresser);
        //save to shared
        SingletonLikeApp.getInstance().getSharedPreferences(getActivity()).increaseClickSticker(expresser);
        expressersManager.refreshRecent();
    }

    public void selectExpresserEmotion(Expresser expresser)
    {
        ExpresserClassificationDialog.start(this, expresser);
    }
}

