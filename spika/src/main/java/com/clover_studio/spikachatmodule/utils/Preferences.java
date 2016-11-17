package com.clover_studio.spikachatmodule.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.clover_studio.spikachatmodule.models.Config;
import com.clover_studio.spikachatmodule.models.Expresser;
import com.clover_studio.spikachatmodule.models.ExpresserCategory;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by ubuntu_ivo on 22.07.15..
 */
public class Preferences {

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.Preferences.TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(Const.Preferences.TOKEN, "");
    }

    //sbh inserted
    public String getCachedDeviceId() {
        return sharedPreferences.getString(Const.Preferences.CACHED_DEVICE_ID, null);
    }

    public void setDeviceId(String v) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.Preferences.CACHED_DEVICE_ID, v);
        editor.apply();
    }


    public void setUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.Preferences.USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(Const.Preferences.USER_ID, "");
    }

    public void setConfig(Config config) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.Preferences.SOCKET_URL, config.socketUrl);
        editor.putString(Const.Preferences.BASE_URL, config.apiBaseUrl);
        editor.apply();
    }

    public Config getConfig() {
        Config config = new Config();
        config.apiBaseUrl = sharedPreferences.getString(Const.Preferences.BASE_URL, "");
        config.socketUrl = sharedPreferences.getString(Const.Preferences.SOCKET_URL, "");
        config.showSidebar = false;
        config.showTitlebar = false;
        return config;
    }

    public void increaseClickSticker(Expresser expresser) {
        String json = getStickersString();
        if (!TextUtils.isEmpty(json)) {
            Gson gson = new Gson();
            try {
                ExpresserCategory responseModel = gson.fromJson(json, ExpresserCategory.class);
                if (responseModel.list.contains(expresser)) {
                    int position = responseModel.list.indexOf(expresser);
                    responseModel.list.get(position).timesClicked++;
                } else {
                    expresser.timesClicked = 1;
                    responseModel.list.add(expresser);
                }

                String jsonNew = gson.toJson(responseModel);
                setRecentStickers(jsonNew);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ExpresserCategory category = new ExpresserCategory();
            category.list = new ArrayList<>();
            expresser.timesClicked = 1;
            category.list.add(expresser);
            Gson gson = new Gson();
            try {
                String jsonNew = gson.toJson(category);
                setRecentStickers(jsonNew);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getStickersString() {
        return sharedPreferences.getString(Const.Preferences.STICKERS_COUNT, "");
    }

    public ExpresserCategory getStickersLikeObject() {
        String json = getStickersString();
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        Gson gson = new Gson();
        try {
            ExpresserCategory responseModel = gson.fromJson(json, ExpresserCategory.class);

            Collections.sort(responseModel.list, new Comparator<Expresser>() {
                @Override
                public int compare(Expresser lhs, Expresser rhs) {
                    return rhs.timesClicked - lhs.timesClicked;
                }
            });

            return responseModel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //sbh
    public void setStickersClassification(String json) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.Preferences.STICKERS_CLASSIFICATION, json);
        editor.apply();
    }

    public String getStickersClassification() {
        return sharedPreferences.getString(Const.Preferences.STICKERS_CLASSIFICATION,"");
    }

    public void setRecentStickers(String json){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.Preferences.STICKERS_COUNT, json);
        editor.apply();
    }

}
