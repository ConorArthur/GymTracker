package com.example.gymtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.app.NotificationCompat;

public class StepTrackingService extends Service {
    private SensorManager sensorManager;
    private SensorEventListener stepCounterListener;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "StepCounterPrefs";
    private static final String STEP_COUNT_KEY = "stepCount";
    private static final String LAST_RESET_KEY = "lastReset";

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);


        if (!sharedPreferences.contains(STEP_COUNT_KEY)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(STEP_COUNT_KEY, 0);
            editor.putLong(LAST_RESET_KEY, 0);
            editor.apply();
        }


        stepCounterListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    int stepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);
                    stepCount++;

                    updateStepCount(stepCount);
                    resetStepCountIfNeeded(stepCount);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    private void updateStepCount(int stepCount) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, stepCount);
        editor.apply();


        Intent intent = new Intent("com.example.gymtracker.STEP_COUNT_UPDATED");
        intent.putExtra("stepCount", stepCount);
        sendBroadcast(intent);
    }

    private void resetStepCountIfNeeded(int currentStepCount) {

        long lastResetMillis = sharedPreferences.getLong(LAST_RESET_KEY, 0);
        Date lastResetDate = new Date(lastResetMillis);


        Date currentDate = new Date();


        if (!isSameDay(currentDate, lastResetDate)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(STEP_COUNT_KEY, currentStepCount);
            editor.putLong(LAST_RESET_KEY, currentDate.getTime());
            editor.apply();
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString1 = dateFormat.format(date1);
        String dateString2 = dateFormat.format(date2);
        return dateString1.equals(dateString2);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(stepCounterListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }


        int stepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);
        if (stepCount >= 10000) {
            createNotification();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(stepCounterListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {

        String channelId = "step_tracking_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Step Tracking Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Step Tracking")
                .setContentText("You've reached 10,000 steps!")
                .setSmallIcon(R.drawable.steps)
                .setOngoing(false)
                .addAction(0, "Stop Service", getStopServicePendingIntent())
                .build();


        startForeground(1, notification);
    }

    private PendingIntent getStopServicePendingIntent() {
        Intent intent = new Intent(this, StepTrackingService.class);
        intent.setAction("STOP_SERVICE_ACTION");
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}