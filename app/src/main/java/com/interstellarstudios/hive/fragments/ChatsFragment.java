package com.interstellarstudios.hive.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.hive.R;
import com.interstellarstudios.hive.models.User;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private Context context;
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private AutoCompleteTextView searchField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        context = getActivity();

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

        setupSearchSuggestions();

        return view;
    }

    private void setupSearchSuggestions() {

        searchSuggestions.clear();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String mCurrentUserId = firebaseUser.getUid();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference userListReference = firebaseFirestore.collection("User");
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

        /*repository.deleteAllUsers();

        CollectionReference userListReference = mFireBaseFireStore.collection("User");
        userListReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                User user = document.toObject(User.class);

                                if (!user.getId().equals(mCurrentUserId)) {

                                    UserEntity userEntity = new UserEntity(user.getId(), user.getUsername(), user.getProfilePicUrl(), user.getOnlineOffline(), user.getStatus(), user.getEmailAddress());
                                    repository.insert(userEntity);
                                }
                            }
                        }
                    }
                });*/
    }
}
