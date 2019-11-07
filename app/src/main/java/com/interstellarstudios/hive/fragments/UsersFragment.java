package com.interstellarstudios.hive.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.hive.R;
import com.interstellarstudios.hive.SearchActivity;
import com.interstellarstudios.hive.adapters.UserAdapter;
import com.interstellarstudios.hive.database.RecentSearchesEntity;
import com.interstellarstudios.hive.models.User;
import com.interstellarstudios.hive.repository.Repository;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class UsersFragment extends Fragment {

    private Context context;
    private RecyclerView recyclerView;
    private List<User> mUsers = new ArrayList<>();
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private AutoCompleteTextView searchField;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private List<RecentSearchesEntity> recentSearchesList = new ArrayList<>();
    private ArrayList<String> recentSearchesStringArrayList = new ArrayList<>();
    private ConstraintLayout layout;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        context = getActivity();

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        searchField = view.findViewById(R.id.searchField);
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

        layout = view.findViewById(R.id.container);

        sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        readUsers();
        setupSearchSuggestions();

        return view;
    }

    private void lightMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    private void darkMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
    }

    private void readUsers() {

        CollectionReference usersPath = mFireBaseFireStore.collection("User");
        usersPath.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                mUsers.clear();

                for (QueryDocumentSnapshot doc : value) {

                    User user = doc.toObject(User.class);

                    if (!user.getId().equals(mCurrentUserId)) {
                        mUsers.add(user);
                    }
                }
                UserAdapter userAdapter = new UserAdapter(context, mUsers, false, sharedPreferences);
                recyclerView.setAdapter(userAdapter);
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

        Repository repository = new Repository(getActivity().getApplication());
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
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
