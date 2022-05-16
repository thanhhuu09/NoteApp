package com.example.noteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    EditText edtCurrentPass, edtNewPass, edtPassConf;
    Button btnChangePass;
    FirebaseAuth fAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        bindVariables();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();


        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              Extract data
                String currentPass = edtCurrentPass.getText().toString();
                String newPass = edtNewPass.getText().toString();
                String confPass = edtPassConf.getText().toString();

//              Verify data
                if(currentPass.isEmpty()){
                    edtCurrentPass.setError("Current pass is required");
                    return;
                }

                if(newPass.isEmpty()){
                    edtNewPass.setError("New password is required");
                    return;
                }

                if(confPass.isEmpty()){
                    edtPassConf.setError("Confirm password is required");
                    return;
                }

                if(!confPass.equals(newPass)){
                    edtPassConf.setError("Confirm password does not match the new password");
                    return;
                }

                changePassHandler(currentPass,newPass);
            }
        });
    }

    private void changePassHandler(String curPass, String newPass){
        String email = user.getEmail();
        if(email != null){
            try{
//          Check current password and change password in on success
                fAuth.signInWithEmailAndPassword(email, curPass).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        edtCurrentPass.setError("Your current pass is invalid");
                    }
                }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d("check","in");
                        user.updatePassword(newPass);
                        Toast.makeText(ChangePassword.this, "Successfully changed password", Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void bindVariables(){
        edtCurrentPass = findViewById(R.id.edtChangePassCurrent);
        edtNewPass = findViewById(R.id.edtchangePassNew);
        edtPassConf = findViewById(R.id.edtChangePassConf);

        btnChangePass = findViewById(R.id.btnChangePass);
    }
}