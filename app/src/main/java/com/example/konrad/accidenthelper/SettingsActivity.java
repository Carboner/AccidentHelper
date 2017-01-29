package com.example.konrad.accidenthelper;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    private EditText phone1View;
    private EditText phone2View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        phone1View = (EditText) findViewById(R.id.phone1);
        phone2View = (EditText) findViewById(R.id.phone2);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("phone_1", phone1View.getText().toString());
//        editor.putString("phone_2", phone2View.getText().toString());
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
