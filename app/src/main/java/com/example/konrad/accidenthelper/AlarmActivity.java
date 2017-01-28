package com.example.konrad.accidenthelper;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AlarmActivity extends AppCompatActivity {

    private TextView timeView;
    private Button cancelButtonView;

    private CountDownTimer countDownTimer;
    private static final int TIME_TO_CANCEL = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        timeView = (TextView) findViewById(R.id.time_text_view);
        cancelButtonView = (Button) findViewById(R.id.cancel_button);

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
                timeView.setText("Message was sent");

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
}
