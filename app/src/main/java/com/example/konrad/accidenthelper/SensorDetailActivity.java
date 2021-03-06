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
import android.widget.TextView;

import java.io.IOException;

public class SensorDetailActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private TextView maxX, maxY, maxZ, maxMagnitudeAccView, dBValueView, changeL, changeW, speedV;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastX, lastY, lastZ;
    private float deltaX;
    private float deltaY;
    private float deltaZ;
    private float deltaXMax;
    private float deltaYMax;
    private float deltaZMax;
    private float magnitudeAccMax;
    private float magnitudeAcc;
    private int countAccidentFlag;

    LocationManager lm;
    Criteria kr;
    Location loc;
    String najlepszyDostawca;
    double difference, l1, l2, l3, l4;
    float speed;

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
        setContentView(R.layout.activity_sensor_detail);
        initializeViews();

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

    public void initializeViews() {
        changeL = (TextView) findViewById(R.id.changeL);
        changeW = (TextView) findViewById(R.id.changeW);

        speedV = (TextView) findViewById(R.id.speedV);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);

        maxMagnitudeAccView = (TextView) findViewById(R.id.maxMagnitudeAcc);
        dBValueView = (TextView) findViewById(R.id.statusDB);
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {

        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(String.format("%.3f", deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(String.format("%.3f", deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(String.format("%.3f", deltaZMax));
        }

//        if (magnitudeAcc > magnitudeAccMax) {
//            magnitudeAccMax = magnitudeAcc;
        maxMagnitudeAccView.setText(Float.toString(magnitudeAcc));
//        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        displayMaxValues();

        magnitudeAcc = ((float) Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])) - MainActivity.G;

        if (magnitudeAcc > MainActivity.MAGNITUDE_ACC_THRESHOLD && dBValue > MainActivity.dB_VALUE_THRESHOLD && speed > MainActivity.SPEED_THRESHOLD) {
            countAccidentFlag++;
            if (countAccidentFlag == 1) {
                startAlarmActivity();
            }
        }

        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);


        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if (deltaZ < 2)
            deltaZ = 0;

        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void startAlarmActivity() {
        Intent intentAlarmActivity = new Intent(this, AlarmActivity.class);
        intentAlarmActivity.putExtra(MainActivity.KEY_LONGITUDE, String.valueOf(l1));
        intentAlarmActivity.putExtra(MainActivity.KEY_LATITUDE, String.valueOf(l2));
        intentAlarmActivity.putExtra(MainActivity.KEY_VOLUME, String.valueOf(dBValue));
        intentAlarmActivity.putExtra(MainActivity.KEY_MAX_ACCELERATION_MAGNITUDE, String.valueOf(magnitudeAcc));
        intentAlarmActivity.putExtra(MainActivity.SPEED, String.valueOf(speed));
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
        dBValue = soundDb(MainActivity.REFRENCE_AMPLITUDE);
        dBValueView.setText(String.format("%.2f dB", dBValue));
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

    public void displayLocalization() {
//        changeL.setText(Double.toString(loc.getLongitude()));
        changeL.setText(String.format("%.6f", loc.getLongitude()));

        changeW.setText(String.format("%.6f", loc.getLatitude()));
    }


    @Override
    public void onLocationChanged(Location location) {
        refresh();
        displayLocalization();
        //predkosc
        locationSpeed();
        displaySpeed();


        if (loc != null) {
            l1 = loc.getLongitude();
            l2 = loc.getLatitude();
        } else {
            l1 = 0;
            l2 = 0;
        }

        //t4.setText(t4.getText()+""+loc.getLongitude()+"/"+loc.getLatitude()+"\n");

    }

    //predkosc
    public float locationSpeed() {
        if (loc.hasSpeed()) {
            speed = loc.getSpeed() * 3.6f;

        }
        return speed;
    }

    public void displaySpeed() {
        speedV.setText(Float.toString(speed));

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
