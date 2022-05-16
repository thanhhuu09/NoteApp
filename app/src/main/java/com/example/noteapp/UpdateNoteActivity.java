package com.example.noteapp;

import static com.example.noteapp.R.drawable.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.noteapp.fragment.NoteFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.StructuredQuery;
import com.google.firestore.v1.WriteResult;
import com.google.type.DateTime;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateNoteActivity extends AppCompatActivity   {
    ImageButton btnUpdateNoteSave, btnUpdatePin, btnUpdateNoteAction, btnUdtNoteLock, btnUdtNoteRemind;
    TextView txtUdtTitle, txtUdtContent;
    private String userID;
    private FirebaseFirestore fStore;
    private DocumentReference noteDocReference;
    private int mPriority;
    private boolean mLock;
    private String mPassword;
    private ImageView udtPrevImg;
    static FirebaseStorage fStorage = FirebaseStorage.getInstance();
    static StorageReference fStorageRef = fStorage.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_note);

        initUI();
        initListener();
    }

    private void initUI() {
        btnUpdateNoteSave = findViewById(R.id.updateNoteUpdate);
        btnUpdatePin = findViewById(R.id.updateNotePin);
        btnUpdateNoteAction = findViewById(R.id.updateNoteAction);
        btnUdtNoteLock = findViewById(R.id.updateNoteLock);
        btnUdtNoteRemind = findViewById(R.id.updateNoteRemind);
        txtUdtTitle = findViewById(R.id.updateEdtTitle);
        txtUdtContent = findViewById(R.id.updateEdtContent);
        udtPrevImg = findViewById(R.id.udtNotePrevImg);


        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        noteDocReference = fStore.collection("users").document(userID).collection("notes").document(getNoteClickedID());

    }

    private void initListener() {
        initPriority();
        initNoteLock();
        getNoteClickedID();
        getNoteData();

        btnUpdateNoteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionMenu();
            }
        });

        btnUpdateNoteSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String udtNoteTitle = txtUdtTitle.getText().toString().trim();
                String udtNoteContent = txtUdtContent.getText().toString().trim();

                saveUpdateNote(udtNoteTitle,udtNoteContent);
            }
        });

        btnUpdatePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePin();
            }
        });

        btnUdtNoteLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockDialog();
            }
        });

    }

    private void unlockDialog() {
        AlertDialog.Builder unlockDialog = new AlertDialog.Builder(this);
        final AlertDialog show = unlockDialog.show();
        unlockDialog.setCancelable(true);
        unlockDialog.setIcon(ic_unlock);
        unlockDialog.setTitle("Unlock note");
        unlockDialog.setMessage("Are you sure want to unlock this note ?");

        unlockDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Object> unlock = new HashMap<>();
                unlock.put("lock",false);
                noteDocReference.update(unlock).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        btnUdtNoteLock.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Unlocked note", Toast.LENGTH_SHORT).show();
                        show.dismiss();
                    }
                });
            }
        });

        unlockDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                show.dismiss();
            }
        });

        unlockDialog.show();

    }

    private void initNoteLock() {
        noteDocReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Note note = documentSnapshot.toObject(Note.class);
                Boolean lock = note.getLock();
                if(lock){
                    mLock = true;
                    String notePassword = note.getPassword();
                    mPassword = notePassword;
                    btnUdtNoteLock.setVisibility(View.VISIBLE);
                    btnUdtNoteLock.setBackgroundResource(ic_unlock);
                }else{
                    mLock = false;
                    btnUdtNoteLock.setVisibility(View.GONE);
                }
            }
        });
    }


    private void initPriority() {
        noteDocReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Note note = documentSnapshot.toObject(Note.class);
                int Priority = note.getPriority();
                Log.d("priority",""+Priority);
                if(Priority == 0){
                    mPriority = 0;
                    btnUpdatePin.setBackgroundResource(ic_pin);
                }else if(Priority == 1){
                    mPriority = 1;
                    btnUpdatePin.setBackgroundResource(ic_pined);
                }
            }
        });
    }

    private void updatePin() {
        if(mPriority == 0){
            btnUpdatePin.setBackgroundResource(ic_pined);
            mPriority = 1;
        }else if(mPriority == 1){
            btnUpdatePin.setBackgroundResource(ic_pin);
            mPriority = 0;
        }
    }

    private void saveUpdateNote(String udtNoteTitle, String udtNoteContent) {
        Map<String, Object> noteUdtMap = new HashMap<>();
        noteUdtMap.put("title",udtNoteTitle);
        noteUdtMap.put("content",udtNoteContent);
        noteUdtMap.put("priority",mPriority);

        noteDocReference.update(noteUdtMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
            }
        });

    }

    private void getNoteData() {
        noteDocReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Note note = documentSnapshot.toObject(Note.class);

                txtUdtTitle.setText(note.getTitle());
                txtUdtContent.setText(note.getContent());

                if(note.getImgUri() !=  null){
                    Picasso.get().load(note.getImgUri()).into(udtPrevImg);
                }
            }
        });
    }

    private String getNoteClickedID() {
        return getIntent().getStringExtra("noteClickedID");
    }

    private void showActionMenu(){
        PopupMenu actionNoteMenu = new PopupMenu(getApplicationContext(), btnUpdateNoteAction);
        actionNoteMenu.getMenuInflater().inflate(R.menu.note_actions, actionNoteMenu.getMenu());
        actionNoteMenu.setForceShowIcon(true);
        actionNoteMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.act_note_delete:
                        DeleteNote();
                        break;
                    case R.id.act_note_lock:
                        DialogSetNotePass();
                        break;
                }
                return false;
            }
        });
        actionNoteMenu.show();
    }

    private void DeleteNote() {
        Map<String, Object> noteUdtMap = new HashMap<>();
        noteUdtMap.put("priority",0);
        noteUdtMap.put("deleted",true);
        noteUdtMap.put("deleteF_date",getDeleteForeverDateTime());
        noteDocReference.update(noteUdtMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onBackPressed();
            }
        });
    }

    private String getDeleteForeverDateTime(){
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.MINUTE, 15);
        dt = c.getTime();
        return dt.toString();
    }


    private void DialogSetNotePass() {
        Dialog dialogSetNotePass = new Dialog(this);
        dialogSetNotePass.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogSetNotePass.setContentView(R.layout.dialog_note_set_pass);
        dialogSetNotePass.setCanceledOnTouchOutside(true);
        dialogSetNotePass.show();

        Window window = dialogSetNotePass.getWindow();
        if(window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        EditText edtNotePass = dialogSetNotePass.findViewById(R.id.edtUdtNotePass);
        EditText edtNotePassConf = dialogSetNotePass.findViewById(R.id.edtUdtNoteConf);
        Button btnCancel = dialogSetNotePass.findViewById(R.id.edtUdtNotePassNegative);
        Button btnOK = dialogSetNotePass.findViewById(R.id.edtNotePassPositive);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String notePass = edtNotePass.getText().toString().trim();
                String notePassConf = edtNotePassConf.getText().toString().trim();

                if(notePass.isEmpty()){
                    Log.d("pass","pass: "+notePass);
                    edtNotePass.setError("Please enter note password");
                    return;
                }

                if(notePassConf.isEmpty()){
                    edtNotePassConf.setError("Please enter note password verification");
                    return;
                }

                if(!notePassConf.equals(notePass)){
                    edtNotePassConf.setError("Note password verification and password are not match");
                    return;
                }

                Map<String, Object> noteUdtMap = new HashMap<>();
                noteUdtMap.put("password",notePass);
                noteUdtMap.put("lock",true);
                noteDocReference.update(noteUdtMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Udt","OK");
                    }
                });
                dialogSetNotePass.dismiss();
                Toast.makeText(getApplicationContext(),"Sucessfully set password for note",Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSetNotePass.dismiss();
            }
        });
    }
}