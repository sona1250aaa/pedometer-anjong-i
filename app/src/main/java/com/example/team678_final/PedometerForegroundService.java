package com.example.team678_final;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.hardware.Sensor.TYPE_STEP_COUNTER;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PedometerForegroundService extends Service implements SensorEventListener {
    SensorManager sensorManager;
    Sensor stepCountSensor;
    private NotificationManager notificationManager;
    private Intent notificationIntent;
    private PendingIntent pendingIntent;
    private SQLiteDatabase db;
    int startSteps;
    int currentSteps;
    BroadcastReceiver reciever;

    //onCreate에서 센서와 센서매니저 초기화
    @Override
    public void onCreate() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(TYPE_STEP_COUNTER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //최종 발표 전까지 알림창 커스텀마이징에 대해서...
        //알림창을 누르면 만보기 화면으로 갈 수 있도록 intent를 설정
        notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //foregroundservice 실행 시 핸드폰 상단 알림바에서 동작하는 알림 채널을 만드는 코드
            NotificationChannel channel = new NotificationChannel(
                    "678", "만보기 알림 채널", NotificationManager.IMPORTANCE_LOW);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        //foregroundservice 실행을 정의
        startForeground(678, new Notification.Builder(
                this, "678")
                .setContentTitle("만보기 알림 채널")
                //금일 전체걸음수: ["current_steps"에 저장된 걸음수값]의 양식으로 출력하며, 앱 최초 실행시 "current_steps"에 -1이 저장되어 있으므로 이를 0으로 표기
                .setContentText("금일 누적걸음수: " + (PreferenceManager.getInt(this, "current_steps") == -1 ? 0 :
                        PreferenceManager.getInt(this, "current_steps")))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker("")
                .build());

        String today = new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis()));
        if(!today.equals(PreferenceManager.getString(this, "today_date"))) {
            PreferenceManager.setString(this, "today_date", today);
            PreferenceManager.setBoolean(this, "start_steps_reset", true);
            PreferenceManager.setInt(this, "current_steps", 0);
            new PedometerDB(this).getWritableDatabase().execSQL("insert into pedometerTable values('" +
                    today + "', 0);");
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("ForegroundServiceFilter"));
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new PedometerFragment().checkDateChange(context);
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("ForegroundServiceFilter"));
            }
        };
        registerReceiver(reciever, filter);

        //센서에서 보내는 이벤트의 리스너를 해당 클래스로 설정
        sensorManager.registerListener(this, stepCountSensor, SENSOR_DELAY_FASTEST);

        Toast.makeText(this, "기록을 시작합니다", Toast.LENGTH_SHORT).show();
        Log.e("boolean", PreferenceManager.getBoolean(this, "start_steps_reset") + "");
        Log.e("boolean", !PreferenceManager.getBoolean(this, "first_start_foreground") + "");
        Log.e("boolean", PreferenceManager.getInt(this, "start_steps") + "");

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == TYPE_STEP_COUNTER) {
            if(PreferenceManager.getBoolean(this, "first_start_foreground") && PreferenceManager.getInt(this, "start_steps") != -1
            && !PreferenceManager.getBoolean(this, "start_steps_reset")) {
                Log.e("first", "start");
                PreferenceManager.setInt(this, "start_steps",
                        PreferenceManager.getInt(this, "start_steps") + (int) event.values[0] - PreferenceManager.getInt(this, "event_values"));
                PreferenceManager.setBoolean(this, "first_start_foreground", false);
            }
            //핸드폰을 껐다 켰을 때 기준값인 "start_steps"를 -1로 초기화하는 부분을 여기에 작성
            Log.e("test", "" + (int) event.values[0]);

            //"start_steps"가 초기값일 때 현재 센서값을 "start_steps"에 저장해 기준값으로 설정
            if (PreferenceManager.getInt(this, "start_steps") == -1 ||
                PreferenceManager.getBoolean(this, "start_steps_reset")) {
                PreferenceManager.setInt(this, "start_steps", (int) event.values[0]);
                PreferenceManager.setBoolean(this, "start_steps_reset", false);
            }
            //현재 걸음수인 "current_steps"는 이벤트 발생 시 센서값 - 기준 걸음수값("start_steps")
            startSteps = PreferenceManager.getInt(this, "start_steps");
            currentSteps = (int) event.values[0] - startSteps;

            //DB를 읽기모드로 열고, yy-MM-dd 양식의 오늘 날짜를 키값으로, 현재 걸음수값을 value로 삼는 레코드를 저장
            db = new PedometerDB(this).getWritableDatabase();
            String date = new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis()));
            String str = "insert or replace into pedometerTable values ('" + date + "', " + currentSteps + ");";

            db.execSQL(str);

            //"current_steps" 키값에 현재 걸음수값을 저장, Pedometer.java 파일에 설정된 리시버(프로그레스바와 텍스트뷰를 갱신)에 브로드캐스트를 전송
            PreferenceManager.setInt(this, "current_steps", currentSteps);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("ForegroundServiceFilter"));

            //알림 채널에 표기되는 걸음수 정보를 현재 걸음수로 수정
            notificationManager.notify(678, new Notification.Builder(
                    this, "678")
                    .setContentTitle("만보기 알림 채널")
                    .setContentText("금일 전체걸음수: " + PreferenceManager.getInt(this, "current_steps"))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setTicker("")
                    .build());
            PreferenceManager.setInt(this, "event_values", (int) event.values[0]);
        }
    }

    @Override
    public void onDestroy() {
        //기록 종료시에만 토스트 메시지를 보내도록, 센서매니저에서 해당 클래스 리스너를 제거
        sensorManager.unregisterListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reciever);
        PreferenceManager.setBoolean(this, "first_start_foreground", true);
        Toast.makeText(this, "기록을 종료합니다", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}