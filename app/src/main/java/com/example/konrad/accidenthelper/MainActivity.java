package com.example.konrad.accidenthelper;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float magnitudeAcc;
    private int countAccidentFlag;

    LocationManager lm;
    Criteria kr;
    Location loc;
    String najlepszyDostawca;
    double difference, l1, l2, l3, l4;
    float speed;

    public static final float G = SensorManager.GRAVITY_EARTH;
    public static final double REFRENCE_AMPLITUDE = Math.pow(10, Math.exp(-7));
    public static final float MAGNITUDE_ACC_THRESHOLD = 1 * SensorManager.GRAVITY_EARTH;
    public static final int dB_VALUE_THRESHOLD = 60;
    public static final int SPEED_THRESHOLD = -1;

    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_MAX_ACCELERATION_MAGNITUDE = "max_acceleration_magnitude";
    public static final String SPEED = "speed";

    private double dBValue;

    private MediaRecorder mRecorder;
    private Thread runner;

    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;

    final Runnable updater = new Runnable() {
        public void run() {
            updateTv();
        }
    };
    final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            //  we dont have an accelerometer!
        }

        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
        }
        kr = new Criteria();
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        refresh();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            ;

        lm.requestLocationUpdates(najlepszyDostawca, 1000, 1, this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        startRecorder();
        countAccidentFlag = 0;
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stopRecorder();
    }

    public void onClickSettings(View view) {
        Intent intentSettingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(intentSettingsActivity);
    }

    public void onClickSensorDetails(View view) {
        Intent intentSensorDetailActivity = new Intent(this, SensorDetailActivity.class);
        startActivity(intentSensorDetailActivity);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        magnitudeAcc = ((float) Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])) - G;

        if (magnitudeAcc > MAGNITUDE_ACC_THRESHOLD && dBValue > dB_VALUE_THRESHOLD && speed > SPEED_THRESHOLD) {
            countAccidentFlag++;
            if (countAccidentFlag == 1) {
                startAlarmActivity();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void startAlarmActivity() {
        Intent intentAlarmActivity = new Intent(this, AlarmActivity.class);
        intentAlarmActivity.putExtra(KEY_LONGITUDE, String.valueOf(l1));
        intentAlarmActivity.putExtra(KEY_LATITUDE, String.valueOf(l2));
        intentAlarmActivity.putExtra(KEY_VOLUME, String.valueOf(dBValue));
        intentAlarmActivity.putExtra(KEY_MAX_ACCELERATION_MAGNITUDE, String.valueOf(magnitudeAcc));
        intentAlarmActivity.putExtra(SPEED, String.valueOf(speed));
        startActivity(intentAlarmActivity);
    }


    public void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
        }

    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void updateTv() {
        dBValue = soundDb(REFRENCE_AMPLITUDE);
    }

    public double soundDb(double ampl) {
        return 20 * Math.log10(getAmplitudeEMA() / ampl);
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 0;
    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    private void refresh() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            ;

        najlepszyDostawca = lm.getBestProvider(kr, true);
        loc = lm.getLastKnownLocation(najlepszyDostawca);

    }


    @Override
    public void onLocationChanged(Location location) {
        refresh();
        //predkosc
        locationSpeed();
        if (loc != null) {
            l1 = loc.getLongitude();
            l2 = loc.getLatitude();
        } else {
            l1 = 0;
            l2 = 0;
        }
    }

    //predkosc
    public float locationSpeed() {
        if (loc.hasSpeed()) {
            speed = loc.getSpeed() * 3.6f;
        }
        return speed;
    }


    //LocationListener
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
