package com.advanced_android.musicplayersample;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Intent intent = new Intent(this, BackgroundMusicService.class);
        intent.putExtra("PendingIntent", 1);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Title")
                .setContentText("Text")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(notifiId, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        int flag = intent.getIntExtra("PendingIntent", 0);

        if (flag == 1) {
            searchRunningAppProcesses();
        }

        return START_STICKY;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved()");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
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

    private void searchRunningAppProcesses() {
        manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        final List<ActivityManager.RunningAppProcessInfo> apps = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo rapi : apps) {
            if (rapi.processName.contains("musicplayersample")) {

                Log.d("RunningAppProcessInfo", "processName: " + rapi.processName);
                Log.d("RunningAppProcessInfo", "importance: " + importance.get(rapi.importance));
                Log.d("RunningAppProcessInfo", "importanceResonCode: " + rapi.importanceReasonCode);

                break;
            }
        }
    }
}
