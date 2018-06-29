package com.hajaulee.sisorshit;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Timer;
import java.util.TimerTask;

import static com.hajaulee.sisorshit.PostTaskAction.UPDATE_MARK_LIST;

public class UpdateMarkService extends Service {
    public static final String TAG = "UpdateMarkService";
    static private boolean isRunning = false;
    static private Timer timer = new Timer();
    static final int DELAY = 1000;
    static final int PERIOD = 600000;
    static private String ACC;
    static private String PASSWORD;

    public static boolean allowAlwaysShow(Context context){
        try{
            FileInputStream config = new FileInputStream(context.getApplicationInfo().dataDir + "/AlwaysShow.tmp");
            int allow = config.read();
            return allow == 1;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            return false;
        }
        return false;
    }

    public static boolean allowAlwaysShow(Context context, boolean allow){
        try{
            FileOutputStream config = new FileOutputStream(context.getApplicationInfo().dataDir + "/AlwaysShow.tmp");
            config.write(allow?1:0);
            return allow;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            return false;
        }
        return false;
    }



    @Override
    public void onCreate() {


        try{
            FileInputStream loginData = new FileInputStream(getApplicationInfo().dataDir + "/tokuda.jav.tmp");
            ObjectInputStream ios = new ObjectInputStream(loginData);
            String acc_pass = (String) ios.readObject();
            String[] ahihi = acc_pass.split("_jav_number_one_");
            ACC = ahihi[0];
            PASSWORD = ahihi[1];
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.e(TAG, "Run:" + "onStartCommand");
                    PostTask x = new PostTask(getApplicationContext(), UPDATE_MARK_LIST);
                    x.execute("ctl00$cLogIn1$bt_cLogIn", ACC, PASSWORD);
                }
            }, DELAY, PERIOD);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        isRunning = false;
        WidgetUtils.sendNotification(this, "Service đã dừng", "Destroy");
        startService(new Intent(this, UpdateMarkService.class));
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
