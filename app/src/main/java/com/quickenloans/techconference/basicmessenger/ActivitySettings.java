package com.quickenloans.techconference.basicmessenger;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.content.Context;

public class ActivitySettings extends AppCompatActivity {

    RadioButton lightThemeButton, darkThemeButton;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.application_settings);

        lightThemeButton = (RadioButton) findViewById(R.id.lightTheme);
        darkThemeButton = (RadioButton) findViewById(R.id.darkTheme);

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Constants.PREF_DARK_THEME, false)) {
            setTheme(R.style.AppTheme_Dark);
            darkThemeButton.toggle();
        } else {
            lightThemeButton.toggle();
        }



        context = this;





        darkThemeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.PREF_DARK_THEME, true).apply();
                    getApplication().setTheme(R.style.AppTheme_Dark);
                } else {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.PREF_DARK_THEME, false).apply();
                    getApplication().setTheme(R.style.AppTheme);
                }
            }
        });
    }
}
