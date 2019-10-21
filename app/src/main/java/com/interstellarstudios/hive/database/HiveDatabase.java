package com.interstellarstudios.hive.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserEntity.class, CurrentUserEntity.class}, version = 4, exportSchema = false)

public abstract class HiveDatabase extends RoomDatabase {

    private static  HiveDatabase instance;
    public abstract UsersDAO usersDAO();
    public abstract CurrentUserDAO currentUserDAO();

    public static synchronized HiveDatabase getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), HiveDatabase.class, "hive_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
