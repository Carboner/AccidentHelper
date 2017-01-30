package com.example.konrad.accidenthelper;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private EditText phone1View;
    private EditText phone2View;
    private Button buttonSaveNumbersView;
    private Switch switchSms;

    private String phone1;
    private String phone2;
    private boolean sendSMSWarning;

    public static final String PREFERENCES_FILE = "AccidentHelperPrefernces";
    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        phone1View = (EditText) findViewById(R.id.phone1);
        phone2View = (EditText) findViewById(R.id.phone2);
        buttonSaveNumbersView = (Button) findViewById(R.id.button_save_numbers);
        switchSms = (Switch) findViewById(R.id.switch_sms);

        sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);

        phone1 = sharedPreferences.getString("phone_1", "");
        phone2 = sharedPreferences.getString("phone_2", "");
        sendSMSWarning = sharedPreferences.getBoolean("sendSMSWarning", false);

        Log.i("SettingsAct onCreate", " " + "phone1: " + phone1);
        Log.i("SettingsAct onCreate", " " + "phone1: " + phone2);
        Log.i("SettingsAct onCreate", " " + sendSMSWarning);

        if (!phone1.equalsIgnoreCase("")) {
            phone1View.setText(phone1);
        }
        if (!phone2.equalsIgnoreCase("")) {
            phone2View.setText(phone2);
        }
        switchSms.setChecked(sendSMSWarning);

        if (!sendSMSWarning) {
            phone1View.setEnabled(false);
            phone2View.setEnabled(false);
            buttonSaveNumbersView.setEnabled(false);
        }

    }

    public void onClickSwitch(View view) {

        boolean switchOn = ((Switch) view).isChecked();

        if (switchOn) {
            sendSMSWarning = true;

            phone1View.setEnabled(true);
            phone2View.setEnabled(true);
            buttonSaveNumbersView.setEnabled(true);
        } else {
            sendSMSWarning = false;

            phone1View.setEnabled(false);
            phone2View.setEnabled(false);
            buttonSaveNumbersView.setEnabled(false);
        }

    }

    public void onClickSaveNumbers(View view) {

        phone1 = phone1View.getText().toString();
        phone2 = phone2View.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("phone_1", phone1);
        editor.putString("phone_2", phone2);
        editor.putBoolean("sendSMSWarning", sendSMSWarning);

        editor.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        phone1 = phone1View.getText().toString();
        phone2 = phone2View.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("phone_1", phone1);
        editor.putString("phone_2", phone2);
        editor.putBoolean("sendSMSWarning", sendSMSWarning);

        editor.commit();

    }

    public void onClickAboutApp(View view) {
        showInfoDialog();
    }

    private void showInfoDialog() {
        Dialog infoDialog = new Dialog(this);
        infoDialog.setCancelable(true);
        infoDialog.setCanceledOnTouchOutside(true);

        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        infoDialog.setContentView(getLayoutInflater().inflate(R.layout.info_dialog, null));

        infoDialog.show();
    }
}
