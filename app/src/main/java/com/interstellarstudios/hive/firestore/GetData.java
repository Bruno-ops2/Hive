package com.interstellarstudios.hive.firestore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.interstellarstudios.hive.database.CurrentUserEntity;
import com.interstellarstudios.hive.models.User;
import com.interstellarstudios.hive.repository.Repository;

public class GetData {

    public static void currentUser(FirebaseFirestore mFireBaseFireStore, String mCurrentUserId, Repository repository) {

        repository.deleteCurrentUser();

        DocumentReference currentUserRef = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        User user = document.toObject(User.class);

                        CurrentUserEntity currentUserEntity = new CurrentUserEntity(user.getId(), user.getUsername(), user.getProfilePicUrl(), user.getStatus(), user.getEmailAddress());
                        repository.insert(currentUserEntity);
                    }
                }
            }
        });
    }
}
