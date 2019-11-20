package com.nullparams.hive.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UsersDAO {

    @Insert
    void insert(UserEntity userEntity);

    @Update
    void update(UserEntity userEntity);

    @Delete
    void delete(UserEntity userEntity);

    @Query("SELECT * FROM users_table")
    List<UserEntity> getAll();

    @Query("DELETE FROM users_table")
    void deleteAll();

    @Query("SELECT * FROM users_table WHERE users_table.username LIKE '%' || :term || '%' OR users_table.emailAddress LIKE '%' || :term || '%' COLLATE NOCASE")
    List<UserEntity> search(String term);
}
