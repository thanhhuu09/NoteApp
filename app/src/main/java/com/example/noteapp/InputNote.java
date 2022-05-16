package com.example.noteapp;

import static com.example.noteapp.R.drawable.ic_unlock;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputNote extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText mTitle, mContent;
    private ImageButton mAddNote, mPinNote, mInputLockNote, mMediaMenu;
    private String userID;
    private FirebaseFirestore fStore;
    private static int mPriority = 0;
    private static boolean mLock;
    private String mPassword;
    private Uri mImageUri;
    private ImageView mImageView;
    static FirebaseStorage fStorage = FirebaseStorage.getInstance();
    static StorageReference fStorageRef = fStorage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_note);

        initUI();
        initListener();
    }

    private void initUI() {
        mTitle = findViewById(R.id.edtTitle);
        mContent = findViewById(R.id.edtContent);
        mAddNote = findViewById(R.id.addNote_save);
        mPinNote = findViewById(R.id.addNote_pin);
        mMediaMenu = findViewById(R.id.inputAddMedia);
        mInputLockNote = findViewById(R.id.addNote_lock);
        mImageView = findViewById(R.id.imageView);
        fStore = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fStorageRef = fStorage.getReference();
        mLock = false;
        mPassword = "";
    }

    private void initListener() {
        mMediaMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMediaMenu();
            }
        });

        mAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString().trim();
                String content = mContent.getText().toString().trim();

                if(title.isEmpty() && content.isEmpty()){
                    finish();
                }else{
                    String noteID = createNoteID();
                    pushNote(noteID, mPriority, title,content, mPassword,mLock,null, false);
                    mPriority = 0;
                    finish();
                }
            }
        });

        mPinNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notePin();
            }
        });
        
        mInputLockNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockNote();
            }
        });
    }


    private void requestPermission(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                openImagePicker();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };


        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                .check();

    }


    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(mImageView);

        }
    }

    private void lockNote() {
        if(!mLock){
            mInputLockNote.setBackgroundResource(R.drawable.ic_password);
            if(mTitle.getText().toString().isEmpty() && mContent.getText().toString().isEmpty()){
                Toast.makeText(this,"Cannot set password for empty note",Toast.LENGTH_SHORT).show();
                return;
            }
            DialogSetNotePass();
        }else{
            unlockNoteDialog();
        }
    }

    private void unlockNoteDialog() {
        AlertDialog.Builder unlockNoteDialog = new AlertDialog.Builder(this);
        final AlertDialog show = unlockNoteDialog.show();
        unlockNoteDialog.setCancelable(true);
        unlockNoteDialog.setIcon(ic_unlock);
        unlockNoteDialog.setTitle("Unlock note");
        unlockNoteDialog.setMessage("Are you sure want to unlock this note ?");

        unlockNoteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLock = false;
                mPassword = "";
                Toast.makeText(getApplicationContext(), "Unlocked note", Toast.LENGTH_SHORT).show();
                mInputLockNote.setBackgroundResource(R.drawable.ic_password);
                show.dismiss();

            }
        });

        unlockNoteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                show.dismiss();
            }
        });

        unlockNoteDialog.show();
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

                mLock = true;
                mPassword = notePass;

                dialogSetNotePass.dismiss();
                mInputLockNote.setBackgroundResource(R.drawable.ic_unlock);
                Toast.makeText(getApplicationContext(),"Successfully set password for note",Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSetNotePass.dismiss();
            }
        });
    }

    private void pushNote(String noteID, int priority, String title, String content, String password,boolean lock, String imgUri, Boolean deleted) {
        Note note = new Note(noteID, priority, title,content, password,lock, null, deleted);

        DocumentReference documentReference = fStore.collection("users").document(userID).collection("notes").document(noteID);
        Map<String, Object> noteAdd = note.toMap();
        documentReference.set(noteAdd);

        if(mImageUri != null){
            uploadToFStorage(noteID, documentReference);
        }

    }

    private String getImgExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadToFStorage(String noteID, DocumentReference documentReference) {
        StorageReference imageRef = fStorageRef.child(userID).child("images").child(noteID).child(System.currentTimeMillis() + "." + getImgExtension(mImageUri));

        imageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, Object> noteUdtMap = new HashMap<>();
                        noteUdtMap.put("imgUri",uri.toString());
                        documentReference.update(noteUdtMap);
                    }
                });

            }
        });
    }

    private void notePin() {
        if(mPriority == 0){
            mPinNote.setBackgroundResource(R.drawable.ic_pined);
            mPriority = 1;
        }else if(mPriority == 1){
            mPinNote.setBackgroundResource(R.drawable.ic_pin);
            mPriority = 0;
        }
    }

    private void showMediaMenu(){
        PopupMenu mediaMenu = new PopupMenu(getApplicationContext(), mMediaMenu);
        mediaMenu.getMenuInflater().inflate(R.menu.note_media, mediaMenu.getMenu());
        mediaMenu.setForceShowIcon(true);
        mediaMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.media_image:
                        requestPermission();
                        break;
                }
                return false;
            }

        });
        mediaMenu.show();
    }

    private String createNoteID(){
        DocumentReference docRef = fStore.collection("users").document(userID).collection("notes").document();

        return docRef.getId();
    }
}