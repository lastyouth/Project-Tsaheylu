package com.example.sbh.tsaheylu;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.clover_studio.spikachatmodule.ChatActivity;
import com.clover_studio.spikachatmodule.base.SingletonLikeApp;
import com.clover_studio.spikachatmodule.models.Config;
import com.clover_studio.spikachatmodule.models.User;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.Tools;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mUserName;
    private EditText mRoomName;
    private Button mEnterToChatRoom;
    private boolean mWritePermission = false;
    private boolean mPhoneStateReadPermission = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Const.PermissionCode.READ_PHONE_STATE_AND_WRITE_STORAGE: {
                if (grantResults.length > 0 && Tools.checkGrantResults(grantResults)) {
                    mPhoneStateReadPermission = true;
                    mWritePermission = true;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserName = (EditText)findViewById(R.id.username);

        mRoomName = (EditText)findViewById(R.id.roomname);

        mEnterToChatRoom = (Button)findViewById(R.id.enter);

        mEnterToChatRoom.setOnClickListener(this);




        requestNecessaryPermission();
    }
    private void requestNecessaryPermission()
    {
        // write permission
        String[] reqpermissions = new String[2];
        int pointer = 0;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            reqpermissions[pointer++] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
        else
        {
            mWritePermission = true;
        }
        // for device id
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            reqpermissions[pointer++] = Manifest.permission.READ_PHONE_STATE;
        } else {
            mPhoneStateReadPermission = true;
        }
        if(pointer > 0)
        {
            ActivityCompat.requestPermissions(this, reqpermissions, Const.PermissionCode.READ_PHONE_STATE_AND_WRITE_STORAGE);
        }
    }
    public static String getDeviceUUID(final Context context) {
        final String id = SingletonLikeApp.getInstance().getSharedPreferences(context).getCachedDeviceId();

        UUID uuid = null;
        if (id != null) {
            uuid = UUID.fromString(id);
        } else {
            final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            SingletonLikeApp.getInstance().getSharedPreferences(context).setDeviceId(uuid.toString());
        }

        return uuid.toString();
    }
    private void tryToLogin()
    {
        String username = mUserName.getText().toString();
        String roomname = mRoomName.getText().toString();

        Log.d(Const.TAG,"username : "+username+" roomname : "+roomname);

        if(username == null || username.equals("") || roomname == null || username.equals(""))
        {
            Toast.makeText(this,"Please enter the requirements",Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User();
        user.roomID = roomname;
        user.userID = getDeviceUUID(this);
        user.name = username;

        Config config = new Config();
        config.apiBaseUrl = "http://143.248.55.190:8080/spika/v1/";
        //config.apiBaseUrl = "";
        config.socketUrl = "http://143.248.55.190:8080/spika";

        ChatActivity.startChatActivityWithConfig(this,user,config);
    }
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.enter)
        {
            if(mWritePermission && mPhoneStateReadPermission)
            {
                tryToLogin();
            }
            else
            {
                Toast.makeText(this,"Necessary permissions should be granted to proceed.",Toast.LENGTH_SHORT).show();
                requestNecessaryPermission();
            }
        }

    }
}
