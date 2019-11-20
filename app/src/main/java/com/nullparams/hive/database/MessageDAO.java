package com.nullparams.hive.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface MessageDAO {

    @Insert
    void insert(MessageEntity messageEntity);

    @Update
    void update(MessageEntity messageEntity);

    @Delete
    void delete(MessageEntity messageEntity);

    @Query("SELECT * FROM messages_table")
    List<MessageEntity> getAll();

    @Query("DELETE FROM messages_table")
    void deleteAll();

    @Query("SELECT * FROM messages_table WHERE messages_table.message LIKE '%' || :term || '%' COLLATE NOCASE")
    List<MessageEntity> search(String term);
}
