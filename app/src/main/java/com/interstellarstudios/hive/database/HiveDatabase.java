package com.interstellarstudios.hive.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserEntity.class, ChatUserEntity.class, RecentSearchesEntity.class, MessageEntity.class}, version = 9, exportSchema = false)

public abstract class HiveDatabase extends RoomDatabase {

    private static  HiveDatabase instance;
    public abstract UsersDAO usersDAO();
    public abstract ChatUserDAO chatUserDAO();
    public abstract RecentSearchesDAO recentSearchesDAO();
    public abstract MessageDAO messageDAO();

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
