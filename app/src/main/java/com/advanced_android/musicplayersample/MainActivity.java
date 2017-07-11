package com.advanced_android.musicplayersample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    final public static int PLAYER_PLAY = 1;
    final public static int PLAYER_STOP = 2;
    Intent intent;

    private Boolean mIsPlaying;
    private View mBtnPlay;
    private View mBtnStop;
    private BackgroundMusicService mServiceBinder;
    // サービスとの接続のコールバック
    private ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            mServiceBinder = ((BackgroundMusicService.MyBinder) binder).getService();
            Log.d("ServiceConnection","connected");
            
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection", "disconnected");
            mServiceBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, BackgroundMusicService.class);

        mBtnPlay = findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceBinder != null) {
                    mServiceBinder.play();
                }

                // startServiceをコメントアウトしたりして、startServiceの挙動を確かめる
                intent.putExtra("PLAYER_CONTROL", PLAYER_PLAY);
                //startService(intent);
            }
        });
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceBinder != null) {
                    mServiceBinder.stop();
                }

                // startServiceをコメントアウトしたりして、startServiceの挙動を確かめる
                intent.putExtra("PLAYER_CONTROL", PLAYER_STOP);
                //startService(intent);
            }
        });
    }

    public void doBindService() {
        Intent intent = null;
        intent = new Intent(this, BackgroundMusicService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        Log.d("activity", "onResume");
        super.onResume();
        if (mServiceBinder == null) {
            // ここで、bindするかを切り変える
            doBindService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(myConnection);
    }

    @Override
    protected void onPause() {
        Log.d("activity", "onPause");
        super.onPause();
        if (mServiceBinder != null) {
            mIsPlaying = mServiceBinder.isPlaying();
            if (!mIsPlaying) {
                mServiceBinder.stopSelf();
            }
            mServiceBinder = null;
        }
    }


}

