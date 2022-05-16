package com.example.noteapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

import data_local.DataLocalManager;
import data_local.MySharedPreferences;

public class SettingsActivity extends AppCompatActivity {
    ImageButton btnBack;
    SwitchCompat swtDisMode;
    SharedPreferences sharedPreferences;
    AutoCompleteTextView autoCompleteLang;
    ArrayAdapter<String> adapterLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initUI();
        initListener();
    }

    private void initUI() {
        swtDisMode = findViewById(R.id.swtDisplayMode);
        btnBack = findViewById(R.id.btnSettingBack);

        sharedPreferences = getSharedPreferences("night",0);
        Boolean displayNight = sharedPreferences.getBoolean("night_mode", false);


        if(displayNight){
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            swtDisMode.setChecked(true);
        }else{
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            swtDisMode.setChecked(false);
        }
    }

    private void initListener() {

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        swtDisMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    swtDisMode.setChecked(true);
                    DataLocalManager.setBooleanValue("night_mode",true);

                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    swtDisMode.setChecked(false);
                    DataLocalManager.setBooleanValue("night_mode",false);

                }
            }
        });
    }
}