package com.nullparams.hive.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ChatUserDAO {

    @Insert
    void insert(ChatUserEntity chatUserEntity);

    @Update
    void update(ChatUserEntity chatUserEntity);

    @Delete
    void delete(ChatUserEntity chatUserEntity);

    @Query("SELECT * FROM chat_user_table")
    List<ChatUserEntity> getAll();

    @Query("DELETE FROM chat_user_table")
    void deleteAll();
}
