package com.nullparams.hive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nullparams.hive.database.RecentSearchesEntity;
import com.nullparams.hive.database.UserEntity;
import com.nullparams.hive.fragments.ChatsFragment;
import com.nullparams.hive.fragments.GroupsFragment;
import com.nullparams.hive.fragments.ProfileFragment;
import com.nullparams.hive.fragments.UsersFragment;
import com.nullparams.hive.models.User;
import com.nullparams.hive.repository.Repository;
import com.sjl.foreground.Foreground;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity implements Foreground.Listener {

    private Context context = this;
    private Repository repository;
    private Window window;
    private View container;
    private TextView textViewFragmentTitle;
    private ImageView imageViewProfilePic;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private Foreground.Binding listenerBinding;
    private BottomNavigationView bottomNav;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST = 13;
    private Toolbar toolbar;
    private AutoCompleteTextView searchField;
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private List<RecentSearchesEntity> recentSearchesList = new ArrayList<>();
    private ArrayList<String> recentSearchesStringArrayList = new ArrayList<>();

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.navigation_chats:
                            selectedFragment = new ChatsFragment();
                            textViewFragmentTitle.setText("Chats ");
                            break;
                        case R.id.navigation_groups:
                            selectedFragment = new GroupsFragment();
                            textViewFragmentTitle.setText("Groups");
                            break;
                        case R.id.navigation_users:
                            selectedFragment = new UsersFragment();
                            textViewFragmentTitle.setText("Users");
                            break;
                        case R.id.navigation_profile:
                            selectedFragment = new ProfileFragment();
                            textViewFragmentTitle.setText("Profile");
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, selectedFragment).commit();
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppRate.with(this)
                .setInstallDays(7)
                .setLaunchTimes(5)
                .setRemindInterval(2)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToWriteStorage();
        }

        repository = new Repository(getApplication());

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        imageViewProfilePic = findViewById(R.id.image_view_profile_pic);

        window = this.getWindow();
        container = findViewById(R.id.container);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewFragmentTitle = findViewById(R.id.text_view_fragment_title);
        textViewFragmentTitle.setText("Chats");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame,
                    new ChatsFragment()).commit();
        }

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);

        searchField = findViewById(R.id.searchField);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, searchSuggestions);
        searchField.setAdapter(adapter);

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        profilePicOperations();
        unreadMessages();
        registerToken();
        searchSetup();
        setupSearchSuggestions();

        listenerBinding = Foreground.get(getApplication()).addListener(this);
    }

    private void lightMode() {

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector));
    }

    private void darkMode() {

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector_light));
    }

    public void getPermissionToWriteStorage() {

        new AlertDialog.Builder(context)
                .setTitle("Permission needed to Write to External Storage")
                .setMessage("This permission is needed in order save images taken with the camera when accessed by the App. Manually enable in Settings > Apps & notifications > Hive > Permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "External storage permission granted", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(context, "External storage permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void profilePicOperations() {

        DocumentReference currentUserRef = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        currentUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    User user = snapshot.toObject(User.class);

                    SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                    prefsEditor.putString("username", user.getUsername());
                    prefsEditor.apply();

                    if (user.getProfilePicUrl() != null) {
                        Picasso.get().load(user.getProfilePicUrl()).into(imageViewProfilePic);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listenerBinding.unbind();
    }

    @Override
    public void onBecameForeground() {
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        chatPath.update("onlineOffline", "online");
    }

    @Override
    public void onBecameBackground() {
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        chatPath.update("onlineOffline", "offline");
    }

    private void registerToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();

                Map<String, Object> userToken = new HashMap<>();
                userToken.put("User_Token_ID", deviceToken);

                DocumentReference userTokenPath = mFireBaseFireStore.collection("User").document(mCurrentUserId).collection("Tokens").document("User_Token");
                userTokenPath.set(userToken);
            }
        });
    }

    private void unreadMessages() {

        CollectionReference unreadPath = mFireBaseFireStore.collection("Chats").document(mCurrentUserId).collection("Single");
        unreadPath.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {

                    CollectionReference messagesPath = mFireBaseFireStore.collection("Chats").document(mCurrentUserId).collection("Single").document(doc.getId()).collection("Messages");
                    messagesPath.whereEqualTo("isRead", false).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }

                            for (QueryDocumentSnapshot doc : value) {

                                if (doc.exists()) {
                                    bottomNav.getMenu().findItem(R.id.navigation_chats).setIcon(R.drawable.ic_new);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void searchSetup() {

        CollectionReference usersPath = mFireBaseFireStore.collection("User");
        usersPath.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            repository.deleteAllUsers();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                User user = document.toObject(User.class);

                                if (!user.getId().equals(mCurrentUserId)) {

                                    UserEntity userEntity = new UserEntity(user.getId(), user.getUsername(), user.getProfilePicUrl(), user.getOnlineOffline(), user.getStatus(), user.getEmailAddress());
                                    repository.insert(userEntity);
                                }
                            }
                        }
                    }
                });
    }

    private void setupSearchSuggestions() {

        searchSuggestions.clear();

        CollectionReference userListReference = mFireBaseFireStore.collection("User");
        userListReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                User user = document.toObject(User.class);

                                if (!user.getId().equals(mCurrentUserId)) {
                                    searchSuggestions.add(user.getUsername());
                                }
                            }
                        }
                    }
                });
    }

    private void search() {

        String searchTerm = searchField.getText().toString().trim().toLowerCase();

        recentSearchesList.clear();
        recentSearchesStringArrayList.clear();

        Repository repository = new Repository(getApplication());
        recentSearchesList = repository.getRecentSearches();

        for (RecentSearchesEntity recentSearches : recentSearchesList) {
            String recentSearchesListString = recentSearches.getSearchTerm();
            recentSearchesStringArrayList.add(recentSearchesListString);
        }

        if (!recentSearchesStringArrayList.contains(searchTerm) && !searchTerm.equals("")) {
            long timeStamp = System.currentTimeMillis();
            RecentSearchesEntity recentSearches = new RecentSearchesEntity(timeStamp, searchTerm);
            repository.insert(recentSearches);

        } else if (recentSearchesStringArrayList.contains(searchTerm)) {
            long timeStampQuery = repository.getTimeStamp(searchTerm);
            RecentSearchesEntity recentSearchesOld = new RecentSearchesEntity(timeStampQuery, searchTerm);
            repository.delete(recentSearchesOld);

            long timeStamp = System.currentTimeMillis();
            RecentSearchesEntity recentSearchesNew = new RecentSearchesEntity(timeStamp, searchTerm);
            repository.insert(recentSearchesNew);
        }

        Intent i = new Intent(context, SearchActivity.class);
        i.putExtra("searchTerm", searchTerm);
        i.putExtra("searchSuggestions", searchSuggestions);
        startActivity(i);
    }
}
