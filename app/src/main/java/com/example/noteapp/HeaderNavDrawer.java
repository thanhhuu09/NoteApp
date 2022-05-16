package com.example.noteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HeaderNavDrawer extends AppCompatActivity {

    TextView navDrawName, navDramEmail;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_nav_drawer);

        initUI();
        initListener();
    }

    private void initListener() {
        getDataFromFStore();
        setData();

    }

    private void getDataFromFStore() {
        String userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    navDrawName.setText("Hi "+ documentSnapshot.getString("name"));
                }
            }
        });
    }

    private void setData() {
        navDramEmail.setText(userEmail);
    }

    private void initUI() {
        navDrawName = findViewById(R.id.navDrawName);
        navDramEmail = findViewById(R.id.navDrawEmail);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userEmail = fAuth.getCurrentUser().getEmail();
    }
}