package com.example.noteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.noteapp.fragment.NoteFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int DISPLAY_LIST = 1;
    private static final int DISPLAY_GRID = 2;

    public int display  = DISPLAY_GRID;

    private static final int FRAGMENT_NOTE = 0;

    private int mCurrentFragment = FRAGMENT_NOTE;

    private CircleImageView profileIcon;
    FirebaseAuth fAuth;
    FirebaseUser user;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    private NavigationView mNavigationView ;

    public ImageButton mButtonDisplay;
    private ImageButton mAddNote;
    private NoteFragment noteFragment;
    private Intent intent;
    private MenuItem verifyEmailItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initListener();
    }


    private void initUI() {
        profileIcon = findViewById(R.id.profileIcon);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToolBar = findViewById(R.id.toolbar);
        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mButtonDisplay = findViewById(R.id.buttonDisplay);
        mAddNote = findViewById(R.id.addNote_save);
        noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.nav_notes);

        intent = this.getIntent();
    }

    private void initListener(){

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        replaceFragment(new NoteFragment());

        mNavigationView.setCheckedItem(R.id.nav_notes);

        mNavigationView.getMenu().findItem(R.id.nav_notes).setChecked(true);

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        mButtonDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display= (display == DISPLAY_GRID) ? DISPLAY_LIST : DISPLAY_GRID;

                if (display == DISPLAY_GRID) {
                    mButtonDisplay.setBackgroundResource(R.drawable.ic_grid);
                } else {
                    mButtonDisplay.setBackgroundResource(R.drawable.ic_list);
                }
            }
        });
    }

    private void showMenu(){
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), profileIcon);
        popupMenu.setForceShowIcon(true);
        popupMenu.getMenuInflater().inflate(R.menu.profile_popup, popupMenu.getMenu());
        verifyEmailItem = popupMenu.getMenu().findItem(R.id.profile_verifyEmail);

        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            verifyEmailItem.setVisible(false);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.profile_verifyEmail:
                        startActivity(new Intent(getApplicationContext(), VerifyEmail.class));
                        finish();
                        break;
                    case R.id.profile_changePass:
                        startActivity(new Intent(getApplicationContext(), ChangePassword.class));
                        finish();
                        break;
                    case R.id.profile_signOut:
                        fAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.nav_notes:
                if(mCurrentFragment != FRAGMENT_NOTE){
                    replaceFragment(new NoteFragment());
                    mCurrentFragment = FRAGMENT_NOTE;
                }
                break;
            case R.id.nav_trash:
                startActivity(new Intent(MainActivity.this, TrashActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.nav_feedback:
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_help:
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    public NoteFragment getNoteFragment() {
        return noteFragment;
    }

    //    private void sendDataToFragment(int display){
//        NoteFragment noteFragment = new NoteFragment();
//        Bundle bundle = new Bundle();
//
//        bundle.putInt("display_mode",display);
//        noteFragment.setArguments(bundle);
//
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.content_frame, noteFragment);
//
//        fragmentTransaction.commit();
//    }


}

