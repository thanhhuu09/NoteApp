package com.example.noteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class TrashReaderActivity extends AppCompatActivity {
    TextView txtNoteTrashTitle, txtNoteTrashContent;
    ImageButton btnNoteTrashBack, btnNoteTrashRestore, btnNoteTrashDelF;
    String noteID;
    DocumentReference documentReference;
    FirebaseFirestore fStore;
    ImageView trashReaderImg;
    private static FirebaseStorage fStorage = FirebaseStorage.getInstance();
    String imgUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_reader);

        initUI();

        noteID = getIntent().getStringExtra("noteID");
        documentReference = fStore.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("notes").document(noteID);

        initDataFromTrashActivity();
        initListener();
    }

    private void initDataFromTrashActivity() {

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Note note = documentSnapshot.toObject(Note.class);

                txtNoteTrashTitle.setText(note.getTitle());
                txtNoteTrashContent.setText(note.getContent());

                String uri = note.getImgUri();
                if(uri != null){
                    Picasso.get().load(uri).into(trashReaderImg);
                }
            }
        });
    }

    private void initUI() {
        txtNoteTrashTitle = findViewById(R.id.trashNoteTxtTitle);
        txtNoteTrashContent = findViewById(R.id.trashNoteTxtContent);
        btnNoteTrashBack = findViewById(R.id.btnBackNoteTrash);
        btnNoteTrashRestore = findViewById(R.id.btnNoteTrashRestore);
        fStore = FirebaseFirestore.getInstance();
        trashReaderImg = findViewById(R.id.trashReaderImg);
        btnNoteTrashDelF = findViewById(R.id.btnNoteTrashDelF);

    }

    private void initListener() {
        btnNoteTrashDelF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogdDelNoteForever();
            }
        });

        btnNoteTrashRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> noteUdtMap = new HashMap<>();
                noteUdtMap.put("deleted",false);
                noteUdtMap.put("deleteF_date", FieldValue.delete());

                documentReference.update(noteUdtMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        btnNoteTrashBack.callOnClick();
                    }
                });
            }
        });


        txtNoteTrashTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TrashReaderActivity.this, "Cannot edit in trash", Toast.LENGTH_SHORT).show();
            }
        });

        txtNoteTrashContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TrashReaderActivity.this, "Cannot edit in trash", Toast.LENGTH_SHORT).show();
            }
        });

        btnNoteTrashBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    private void dialogdDelNoteForever() {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document != null){
                        imgUri = document.getString("imgUri");
                    }
                }
            }
        });
        AlertDialog.Builder diaNoteDelF = new AlertDialog.Builder(this);
        AlertDialog show = diaNoteDelF.show();
        diaNoteDelF.setIcon(R.drawable.ic_del_f);
        diaNoteDelF.setTitle("Are you sure want to delete this note forever ?");
        diaNoteDelF.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                documentReference.delete();

                if(imgUri != null){
                    StorageReference imageRef = fStorage.getReferenceFromUrl(imgUri);
                    imageRef.delete();
                }
                finish();
            }
        });
        diaNoteDelF.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                show.dismiss();
            }
        });
        diaNoteDelF.show();
    }
}