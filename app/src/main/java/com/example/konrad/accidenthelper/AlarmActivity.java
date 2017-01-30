package com.example.konrad.accidenthelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AlarmActivity extends AppCompatActivity {

    private TextView timeView;
    private Button cancelButtonView;

    private CountDownTimer countDownTimer;
    private static final int TIME_TO_CANCEL = 5;

    private String longitudeIntent;
    private String latitudeIntent;
    private String volumeIntent;
    private String max_acceleration_magnitude_Intent;
    private String speed;

    private String phone1;
    private String phone2;
    private Boolean sendSMSBool;

    private static final String SERVER_URL = "http://104.131.161.226:8000/api/incidents/";
    private static final String KEY_TOKEN = "Authorization";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        timeView = (TextView) findViewById(R.id.time_text_view);
        cancelButtonView = (Button) findViewById(R.id.cancel_button);

        Intent intent = getIntent();
        longitudeIntent = intent.getStringExtra(MainActivity.KEY_LONGITUDE);
        latitudeIntent = intent.getStringExtra(MainActivity.KEY_LATITUDE);
        volumeIntent = intent.getStringExtra(MainActivity.KEY_VOLUME);
        max_acceleration_magnitude_Intent = intent.getStringExtra(MainActivity.KEY_MAX_ACCELERATION_MAGNITUDE);
        speed = intent.getStringExtra(MainActivity.SPEED);

        SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
        phone1 = sharedPreferences.getString("phone_1", null);
        phone2 = sharedPreferences.getString("phone_2", null);
        sendSMSBool = sharedPreferences.getBoolean("sendSMSWarning", false);

        Log.d("AlarmActivity", phone1);
        Log.d("AlarmActivity", phone2);
        Log.d("AlarmActivity", String.valueOf(sendSMSBool));

        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(TIME_TO_CANCEL * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeView.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                timeView.setTextSize(35);
                timeView.setText("Message sent");

                sendData();

                if (sendSMSBool) {
                    sendSMS();
                }

            }
        };
        countDownTimer.start();
    }

    public void onCancelAlarm(View view) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            finish();
        }

    }

    protected void sendSMS() {
        final String longitude = longitudeIntent;
        final String latitude = latitudeIntent;

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone1, null, "wypadek w " + longitude + " " + latitude + " z predkością " + speed, null, null);

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);

        sendIntent.setType("vnd.android-dir/mms-sms");
        startActivity(sendIntent);
    }

    private void sendData() {
        final String token = "Token 8f12919ffe3caa4eb20a594dc4bc460765fc9423";
        final String longitude = longitudeIntent;
        final String latitude = latitudeIntent;
        final String volume = volumeIntent;
        final String max_acceleration_magnitude = max_acceleration_magnitude_Intent;


        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
//                        System.out.println(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(AlarmActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(MainActivity.KEY_LONGITUDE, longitude);
                params.put(MainActivity.KEY_LATITUDE, latitude);
                params.put(MainActivity.KEY_VOLUME, volume);
                params.put(MainActivity.KEY_MAX_ACCELERATION_MAGNITUDE, max_acceleration_magnitude);
                return params;
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(KEY_TOKEN, token);
                headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                return headers;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
