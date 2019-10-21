package com.interstellarstudios.hive.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CurrentUserDAO {

    @Insert
    void insert(CurrentUserEntity currentUserEntity);

    @Update
    void update(CurrentUserEntity currentUserEntity);

    @Delete
    void delete(CurrentUserEntity currentUserEntity);

    @Query("SELECT * FROM current_user_table")
    List<CurrentUserEntity> getAll();

    @Query("DELETE FROM current_user_table")
    void deleteAll();
}
