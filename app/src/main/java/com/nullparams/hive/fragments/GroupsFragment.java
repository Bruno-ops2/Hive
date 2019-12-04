package com.nullparams.hive.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.hive.CreateGroupActivity;
import com.nullparams.hive.GroupMessageActivity;
import com.nullparams.hive.R;
import com.nullparams.hive.adapters.GroupAdapter;
import com.nullparams.hive.models.Group;

import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class GroupsFragment extends Fragment {

    private Context context;
    private RecyclerView recyclerView;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private GroupAdapter adapter;
    private SharedPreferences sharedPreferences;
    private ConstraintLayout layout;
    private TextView textViewNewGroup;
    private ImageView imageViewNewGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        context = getActivity();

        AutoCompleteTextView searchField = getActivity().findViewById(R.id.searchField);
        searchField.setVisibility(View.VISIBLE);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        textViewNewGroup = view.findViewById(R.id.text_view_new_group);
        textViewNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uniqueId = UUID.randomUUID().toString();
                Intent i = new Intent(context, CreateGroupActivity.class);
                i.putExtra("uniqueId", uniqueId);
                startActivity(i);
            }
        });

        imageViewNewGroup = view.findViewById(R.id.image_view_new_group);
        imageViewNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uniqueId = UUID.randomUUID().toString();
                Intent i = new Intent(context, CreateGroupActivity.class);
                i.putExtra("uniqueId", uniqueId);
                startActivity(i);
            }
        });

        layout = view.findViewById(R.id.container);

        sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        setUpRecyclerView();

        return view;
    }

    private void lightMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewNewGroup.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(imageViewNewGroup, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
    }

    private void darkMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewNewGroup.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewNewGroup, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
    }

    private void setUpRecyclerView() {

        final CollectionReference listRef = mFireBaseFireStore.collection("Chats").document(mCurrentUserId).collection("Groups");
        Query query = listRef.orderBy("groupName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>()
                .setQuery(query, Group.class)
                .build();

        adapter = new GroupAdapter(options, sharedPreferences, context);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                Group group = documentSnapshot.toObject(Group.class);

                Intent i = new Intent(context, GroupMessageActivity.class);
                i.putExtra("groupName", group.getGroupName());
                i.putExtra("uniqueId", documentSnapshot.getId());
                startActivity(i);
            }
        });

        adapter.setOnItemLongClickListener(new GroupAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position) {

                new AlertDialog.Builder(context)
                        .setTitle("Delete Group")
                        .setMessage("Are you sure you want to delete and remove yourself from this group?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String uniqueId = documentSnapshot.getId();

                                CollectionReference seenPath = mFireBaseFireStore.collection("Chats").document(mCurrentUserId).collection("Groups").document(uniqueId).collection("Participants");
                                seenPath.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                        String participantUserId = document.getId();

                                                        DocumentReference deleteParticipantRef = mFireBaseFireStore.collection("Chats").document(participantUserId).collection("Groups").document(uniqueId).collection("Participants").document(mCurrentUserId);
                                                        deleteParticipantRef.delete();
                                                    }
                                                }
                                            }
                                        });

                                DocumentReference removedUserGroupPath = mFireBaseFireStore.collection("Chats").document(mCurrentUserId).collection("Groups").document(uniqueId);
                                removedUserGroupPath.delete();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
