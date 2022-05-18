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
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import data_local.DataLocalManager;
import data_local.MySharedPreferences;

public class SettingsActivity extends AppCompatActivity {
    ImageButton btnBack;
    SwitchCompat swtDisMode;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferencesFont;
    RadioGroup fontSizeRadiodGrp;
    RadioButton rBFontSmall, rbFontNormal, rbFontLarge;
    TextView setTxtNoteHeading, setTxtNoteContent, txtNotePrevHeading, txtNotePrevContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        

//        retrieveFontSize();

        initUI();
        initListener();
    }

//    private void retrieveFontSize() {
//        sharedPreferences= getSharedPreferences(font_size_choicce,MODE_PRIVATE);
//        int i = sharedPref.getInt(my_choice_key,-1);
//        if( i >= 0){
//            ((RadioButton) ((RadioGroup)findViewById(R.id.myChoiceViewRadio)).getChildAt(i)).setChecked(true);
//        }
//    }

    private void initUI() {
        swtDisMode = findViewById(R.id.swtDisplayMode);
        btnBack = findViewById(R.id.btnSettingBack);
        fontSizeRadiodGrp = findViewById(R.id.fontSizeRadioGrp);
        rBFontSmall = findViewById(R.id.radFontSmall);
        rbFontNormal = findViewById(R.id.radFontNormal);
        rbFontLarge = findViewById(R.id.radFontLarge);
        setTxtNoteHeading = findViewById(R.id.setTxtNoteHeading);
        setTxtNoteContent = findViewById(R.id.setTxtNoteContent);

        txtNotePrevHeading = findViewById(R.id.note_title);
        txtNotePrevContent = findViewById(R.id.note_content);


        sharedPreferences = getSharedPreferences("night",0);
        Boolean displayNight = sharedPreferences.getBoolean("night_mode", false);

        int fontSizeChoice  = DataLocalManager.getIntFontSizeValue("font_size");

        switch (fontSizeChoice){
            case 0:
                rBFontSmall.setChecked(true);
//                setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_title_small));
//                setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_content_small));
                setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                break;
            case 1:
                rbFontNormal.setChecked(true);
//                setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_title_nor));
//                setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_content_nor));

                setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                break;
            case 2:
                rbFontLarge.setChecked(true);
//                setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_title_large));
//                setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_content_large));

                setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                break;
        }

        if(displayNight){
            swtDisMode.setChecked(true);
        }else{
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


//        txtNotePrevHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_title_small));
//        txtNotePrevContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.note_content_small));

        fontSizeRadiodGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radFontSmall:
                        DataLocalManager.setIntFontSizeValue("font_size",0);
                        setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                        setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                        break;
                    case R.id.radFontNormal:
                        DataLocalManager.setIntFontSizeValue("font_size",1);
                        setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                        setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                        break;
                    case R.id.radFontLarge:
                        DataLocalManager.setIntFontSizeValue("font_size",2);
                        setTxtNoteHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                        setTxtNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                        break;
                }
            }
        });


    }

//    private void saveFontSize(RadioGroup group) {
//        DataLocalManager.setIntValue("font_size",group.indexOfChild(findViewById(group.getCheckedRadioButtonId())));
//    }
}

