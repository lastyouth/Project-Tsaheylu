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
import android.widget.ImageButton;
import android.widget.Toast;

import com.clover_studio.spikachatmodule.ChatActivity;
import com.clover_studio.spikachatmodule.base.SingletonLikeApp;
import com.clover_studio.spikachatmodule.models.Config;
import com.clover_studio.spikachatmodule.models.User;
import com.clover_studio.spikachatmodule.utils.Const;
import com.clover_studio.spikachatmodule.utils.Tools;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mUserName;
    private EditText mRoomName;
    private ImageButton mEnterToChatRoom;
    private boolean mNecessaryPermissionsGranted = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Const.PermissionCode.NECESSARY_PERMISSIONS: {
                if (grantResults.length > 0 && Tools.checkGrantResults(grantResults)) {
                    mNecessaryPermissionsGranted = true;
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

        mEnterToChatRoom = (ImageButton)findViewById(R.id.enter);

        mEnterToChatRoom.setOnClickListener(this);




        requestNecessaryPermission();
    }
    private void requestNecessaryPermission()
    {
        // write permission
        String[] reqpermissions;
        //int pointer = 0;
        List<String> rawperms = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            rawperms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // for device id
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            rawperms.add(Manifest.permission.READ_PHONE_STATE);
        }
        // for internet
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            rawperms.add(Manifest.permission.INTERNET);
        }
        // for camera
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            rawperms.add(Manifest.permission.CAMERA);
        }
        // for bluetooth
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            rawperms.add(Manifest.permission.BLUETOOTH);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            rawperms.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            rawperms.add(Manifest.permission.VIBRATE);
        }
        if(rawperms.size() > 0)
        {
            reqpermissions = rawperms.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, reqpermissions, Const.PermissionCode.NECESSARY_PERMISSIONS);
        }
        else
        {
            mNecessaryPermissionsGranted = true;
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
            if(mNecessaryPermissionsGranted)
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
