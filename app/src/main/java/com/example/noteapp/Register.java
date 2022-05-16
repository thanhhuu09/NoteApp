package com.example.noteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {

    public static boolean checkEnterOTP;
    EditText registerName, registerEmail, registerPassword, registerConfirmPassword, registerPhoneNumber;
    Button btnRegister, btnLogin;
    FirebaseAuth fireAuth;
    ConstraintLayout mLayoutRegister;
    String userID, userName, userEmail, userPhoneNumber;
    FirebaseFirestore fStore;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initUi();
        initListener();
    }

    private void initUi(){
        mLayoutRegister = findViewById(R.id.layoutRegister);
        registerName = findViewById(R.id.registerName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfirmPassword = findViewById(R.id.registerPassword2);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        btnRegister = findViewById(R.id.registerBtnRegister);
        btnLogin = findViewById(R.id.btnRegisterLogin);
        fireAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    private void initListener() {
        //        Switch to Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                  Extract the data from the form
                userName = registerName.getText().toString().trim();
                userEmail = registerEmail.getText().toString().trim();
                userPhoneNumber = registerPhoneNumber.getText().toString().trim();
                checkEnterOTP = false;

                String password = registerPassword.getText().toString().trim();
                String confPass = registerConfirmPassword.getText().toString().trim();
                if(userName.isEmpty()){
                    setErr(registerName,"Name is required");
                    return;
                }

                if(userEmail.isEmpty()){
                    setErr(registerEmail,"Email is required");
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                    setErr(registerEmail, "Email is invalid");
                    return;
                }
                if(userPhoneNumber.isEmpty()){
                    setErr(registerPhoneNumber, "Phone number is required");
                    return;
                }
                if(password.isEmpty()){
                    setErr(registerPassword, "Password is required");
                    return;
                }

                if(confPass.isEmpty()){
                    setErr(registerPassword, "Confirm password is required");
                    return;
                }

                if(!password.equals(confPass)){
                    setErr(registerConfirmPassword, "Password is invalid");
                    return;
                }
                //xac thuc otp
//              Data is validated
                fireAuth.createUserWithEmailAndPassword(userEmail,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        addUserToData();
                        onClickVerifyPhoneNumber(userPhoneNumber);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
        });
    }

    public void onClickVerifyPhoneNumber(String phoneNumber){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
            }
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Intent intent = new Intent(getApplicationContext(), VerifyPhoneNumber.class);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("verificationId", verificationId);
                intent.putExtra("userID",FirebaseAuth.getInstance().getCurrentUser().getUid());

                Log.d("onCodeSent", "onCodeSent:" + verificationId);
                Log.d("userID", "userID " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+84" + phoneNumber)
                .setTimeout(120L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void setErr(EditText edt, String warn){
        edt.setError(warn);
    }

    private void addUserToData(){
        User user = new User(userName, userEmail, userPhoneNumber, checkEnterOTP);
        userID = fireAuth.getCurrentUser().getUid();

        fStore.collection("users")
                .document(userID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("add", "DocumentSnapshot written with ID: " + userID);
                    }
                });
    }


}