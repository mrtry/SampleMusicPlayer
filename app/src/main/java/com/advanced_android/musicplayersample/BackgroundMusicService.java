package com.advanced_android.musicplayersample;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundMusicService extends Service {

    private static final String TAG = BackgroundMusicService.class.getSimpleName();

    private final IBinder mBinder = new MyBinder();
    private MediaPlayer mPlayer;
    private int notifiId = 10;

    public BackgroundMusicService() {
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.d("service", "onBind");
        return mBinder;
    }

    public class MyBinder extends Binder {
        BackgroundMusicService getService() {
            return BackgroundMusicService.this;
        }
    }

    /**
     * 音楽再生中かどうか返す
     *
     * @return 音楽再生中の場合はtrue。それ以外はfalse。
     */
    public boolean isPlaying() {
        boolean isPlaying = false;
        if (mPlayer != null) {
            isPlaying = mPlayer.isPlaying();
        }
        return isPlaying;
    }

    /**
     * 音楽を再生する
     */
    public void play() {
        Log.d(TAG, "play");
        mPlayer = MediaPlayer.create(this, R.raw.bensound_clearday);
        mPlayer.setLooping(true); // Set looping
        mPlayer.setVolume(100, 100);
        mPlayer.start();
    }

    /**
     * 音楽を停止する。すでに停止中の場合は何もしない
     */
    public void stop() {
        Log.d(TAG, "stop");
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // 3秒ごとにプロセス優先順位度をログに出す
        getProcessInfo();

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Title")
                .setContentText("Text")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        // startForeground()試したかったら、ここをコメントアウトする
        //startForeground(notifiId, notification);
    }

    @Override
    public void onDestroy() {
        //stopForeground(true);
        stop();

        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null) {
            int val = intent.getIntExtra("PLAYER_CONTROL", 0);

            switch (val) {
                case MainActivity.PLAYER_PLAY:
                    play();
                    break;

                case MainActivity.PLAYER_STOP:
                    stopSelf();
                    break;

                default:
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved()");
    }


    private ActivityManager manager;
    private static Map<Integer, String> importance = new HashMap<Integer, String>();
    private static Map<Integer, String> reason = new HashMap<Integer, String>();

    static {
        importance.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND, "IMPORTANCE_FOREGROUND");
        importance.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE, "IMPORTANCE_PERCEPTIBLE");
        importance.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE, "IMPORTANCE_VISIBLE");
        importance.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE, "IMPORTANCE_SERVICE");
        importance.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE, "IMPORTANCE_FOREGROUND_SERVICE");
        importance.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND, "IMPORTANCE_BACKGROUND");
        importance.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY, "IMPORTANCE_EMPTY");
        reason.put(ActivityManager.RunningAppProcessInfo.REASON_PROVIDER_IN_USE, "REASON_PROVIDER_IN_USE");
        reason.put(ActivityManager.RunningAppProcessInfo.REASON_SERVICE_IN_USE, "REASON_SERVICE_IN_USE");
        reason.put(ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN, "UNKNOWN");
    }

    private void getProcessInfo() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

                final List<ActivityManager.RunningAppProcessInfo> apps = manager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo rapi : apps) {
                    if (rapi.processName.contains(getPackageName())) {

                        Log.d(getPackageName() + " ProcessInfo", "processName: " + rapi.processName);
                        Log.d(getPackageName() + " ProcessInfo", "importance: " + importance.get(rapi.importance));
                        Log.d(getPackageName() + " ProcessInfo", "importanceResonCode: " + rapi.importanceReasonCode);

                        break;
                    }
                }
            }
        }, 0, 3000);
    }
}
