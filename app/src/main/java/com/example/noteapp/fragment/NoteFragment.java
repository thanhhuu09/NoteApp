package com.example.noteapp.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp.InputNote;
import com.example.noteapp.MainActivity;
import com.example.noteapp.Note;
import com.example.noteapp.NoteAdapter;
import com.example.noteapp.R;
import com.example.noteapp.UpdateNoteActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Locale;

import data_local.DataLocalManager;

public class NoteFragment extends Fragment {
    private NoteAdapter noteAdapter;
    private RecyclerView recyclerView;
    public static ArrayList<Note> noteList;
    public FloatingActionButton mFloatingAddNote;
    private GridLayoutManager gridLayoutManager;
    FirebaseFirestore fStore;
    CollectionReference collectionReference ;
    private String userID ;
    private View mView;
    private EditText noteSearch;
    private FirestoreRecyclerOptions<Note> notes;
    private Intent intentToUdtNote;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);

        if(gridLayoutManager == null){
            gridLayoutManager = new GridLayoutManager(view.getContext(),2);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }

        MainActivity activity = (MainActivity) getActivity();


        activity.mButtonDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gridLayoutManager.getSpanCount() == 2){
                    gridLayoutManager.setSpanCount(1);
                    recyclerView.setLayoutManager(gridLayoutManager);
                }else{
                    gridLayoutManager.setSpanCount(2);
                    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_note, container, false);

        getView();
        initUI();
        setUpRecyclerView();
        mFloatingAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNoteNum();

            }
        });


        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Note note = documentSnapshot.toObject(Note.class);
                String id = documentSnapshot.getId();

                intentToUdtNote.putExtra("noteClickedID",id);
                if(note.getLock() == true){
                    DialogLockNote(note);
                }else{
                    startActivity(intentToUdtNote);
                }
            }
        });

        noteSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        return mView;
    }



    private void DialogLockNote(Note note) {
        Dialog dialogLockNote = new Dialog(this.getContext());
        dialogLockNote.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLockNote.setContentView(R.layout.dialog_lock_note);
        dialogLockNote.setCanceledOnTouchOutside(true);
        dialogLockNote.show();

        Window window = dialogLockNote.getWindow();
        if(window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        Button diaLockNotePositiveBtn = dialogLockNote.findViewById(R.id.btnLockNotePositive);
        Button diaLockNoteNegativeBtn = dialogLockNote.findViewById(R.id.btnLockNoteNegative);

        diaLockNotePositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText diaLockNotePass = dialogLockNote.findViewById(R.id.edtLockNotePass);

                String diaNotePass = diaLockNotePass.getText().toString().trim();

                if(diaNotePass.isEmpty()){
                    diaLockNotePass.setError("Note password is empty");
                    return;
                }
                if(!diaNotePass.equals(note.getPassword())){
                    diaLockNotePass.setError("Note password is invalid");
                    return;
                }

                if(diaNotePass.equals(note.getPassword())){
                    dialogLockNote.dismiss();
                    startActivity(intentToUdtNote);
                }
            }
        });

        diaLockNoteNegativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogLockNote.dismiss();
            }
        });
    }

    private void filter(String text) {
        Query query = collectionReference.whereEqualTo("deleted",false).orderBy("title").startAt(text).endAt(text+"\uf8ff");

        FirestoreRecyclerOptions<Note> filteredNotes = new FirestoreRecyclerOptions.Builder<Note>().
                setQuery(query, Note.class)
                .build();

        noteAdapter = new NoteAdapter(filteredNotes);
//        recyclerView.setHasFixedSize(false);
        noteAdapter.startListening();
        recyclerView.setAdapter(noteAdapter);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Note note = documentSnapshot.toObject(Note.class);
                String id = documentSnapshot.getId();

                intentToUdtNote.putExtra("noteClickedID",id);
                if(note.getLock() == true){
                    DialogLockNote(note);
                }else{
                    startActivity(intentToUdtNote);
                }
            }
        });

    }

    private void setUpRecyclerView() {
        Query query = collectionReference.whereEqualTo("deleted",false).orderBy("priority",Query.Direction.DESCENDING);

        notes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        noteAdapter = new NoteAdapter(notes);
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        noteAdapter.startListening();
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    private void initUI() {

        recyclerView = mView.findViewById(R.id.recyclerView);
        mFloatingAddNote = mView.findViewById(R.id.floatingAddNote);
        fStore = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference = fStore.collection("users").document(userID).collection("notes");
        noteSearch = getActivity().findViewById(R.id.noteSearch);

        intentToUdtNote = new Intent(getContext(), UpdateNoteActivity.class);

        noteList = new ArrayList<>();

        recyclerView.setAdapter(noteAdapter);
    }

    private void checkNoteNum() {
        fStore.collection("users").document(userID).collection("notes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int noteNum = task.getResult().size();
                if (task.isSuccessful()) {
                    if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                        if (noteNum == 5) {
                            Toast.makeText(getContext(), "Verify your account to add more note", Toast.LENGTH_LONG).show();
                        }else if(noteNum < 5){
                            startActivity(new Intent(getActivity(), InputNote.class));
                        }
                    }else{
                        startActivity(new Intent(getActivity(), InputNote.class));
                    }
                } else {
                    Log.d("error", "checkNoteNum failed");
                }
            }
        });
    }
}





