package com.example.noteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TrashActivity extends AppCompatActivity {
    TrashAdapter trashAdapter;
    RecyclerView trashRecyclerView;
    CollectionReference collectionReference;
    FirebaseFirestore fStore;
    private GridLayoutManager gridLayoutManager;
    private FirestoreRecyclerOptions<Note> notes_del;
    private TextView txtBtnEmptyTrash;
    private ImageButton btnTrashBack;
    static FirebaseStorage fStorage = FirebaseStorage.getInstance();
    static StorageReference fStorageRef = fStorage.getReference();
    static final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        initUI();
        setLayoutManager();
        setUpTrashRecyclerView();
        initListener();


    }

    private void showEmptyTrashAlert(){
        AlertDialog.Builder emptyAlert = new AlertDialog.Builder(this);
        final AlertDialog show = emptyAlert.show();
        emptyAlert.setIcon(R.drawable.ic_warning);
        emptyAlert.setTitle("WARNING !");
        emptyAlert.setMessage("Are you sure want to delete all the note in trash forever ?");
        emptyAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                emptyTrash();
                show.dismiss();
            }
        });

        emptyAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                show.dismiss();
            }
        });

        emptyAlert.setCancelable(true);
        emptyAlert.show();
    }

    private void setLayoutManager() {
        if(gridLayoutManager == null){
            gridLayoutManager = new GridLayoutManager(TrashActivity.this,2);
            trashRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
    }

    private void setUpTrashRecyclerView() {
        Query query = collectionReference.whereEqualTo("deleted",true);

        notes_del = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        trashAdapter = new TrashAdapter(notes_del);
        trashRecyclerView.setAdapter(trashAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        clearNoteExpired();
        trashAdapter.startListening();
        trashAdapter.notifyDataSetChanged();
    }

    private void clearNoteExpired() {
        Query query = collectionReference.whereEqualTo("deleted",true);

        notes_del = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        collectionReference.whereEqualTo("deleted",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        String imgUri = document.getString("imgUri");
                        String deleteF_date = document.get("deleteF_date").toString();
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                        try {
                            Date date = sdf.parse(deleteF_date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Calendar cal_del = sdf.getCalendar();
                        Calendar cal_cur = Calendar.getInstance();

                        if(cal_del.compareTo(cal_cur) <= 0){
                            if(imgUri != null){
                                StorageReference imageRef = fStorage.getReferenceFromUrl(imgUri);
                                imageRef.delete();
                            }
                            collectionReference.document(documentId).delete();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        trashAdapter.stopListening();
    }


    private void initUI() {
        btnTrashBack = findViewById(R.id.btnTrashBack);
        txtBtnEmptyTrash = findViewById(R.id.txtBtnEmptyTrash);
        trashRecyclerView = findViewById(R.id.trashRecyclerView);
        fStore = FirebaseFirestore.getInstance();
        collectionReference = fStore.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("notes");
    }

    private void emptyTrash(){
        Query query = collectionReference.whereEqualTo("deleted",true);

        notes_del = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        collectionReference.whereEqualTo("deleted",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        String imgUri = document.getString("imgUri");

                        if(imgUri != null){
                            StorageReference imageRef = fStorage.getReferenceFromUrl(imgUri);
                            imageRef.delete();
                        }

                        collectionReference.document(documentId).delete();
                    }
                }
            }
        });
    }

    private void initListener() {
        txtBtnEmptyTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmptyTrashAlert();
            }
        });

        btnTrashBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TrashActivity.this, MainActivity.class));
            }
        });


        trashAdapter.setOnTrashClickListener(new TrashAdapter.OnTrashClickListener() {
            @Override
            public void onTrashClick(DocumentSnapshot documentSnapshot, int position) {
                Intent intentToTrashReader = new Intent(getApplicationContext(),TrashReaderActivity.class);
//                Note note = documentSnapshot.toObject(Note.class);
                String id = documentSnapshot.getId();
                intentToTrashReader.putExtra("noteID",id);
                startActivity(intentToTrashReader);
            }
        });
    }
}