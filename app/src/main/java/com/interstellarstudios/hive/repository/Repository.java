package com.interstellarstudios.hive.repository;

import android.app.Application;

import com.interstellarstudios.hive.database.CurrentUserDAO;
import com.interstellarstudios.hive.database.CurrentUserEntity;
import com.interstellarstudios.hive.database.HiveDatabase;
import com.interstellarstudios.hive.database.UserEntity;
import com.interstellarstudios.hive.database.UsersDAO;

import java.util.List;

public class Repository {

    private UsersDAO usersDAO;
    private CurrentUserDAO currentUserDAO;

    public Repository(Application application) {

        HiveDatabase hiveDatabase = HiveDatabase.getInstance(application);
        usersDAO = hiveDatabase.usersDAO();
        currentUserDAO = hiveDatabase.currentUserDAO();
    }

    public void insert(UserEntity userEntity) {
        usersDAO.insert(userEntity);
    }

    public void update(UserEntity userEntity) {
        usersDAO.update(userEntity);
    }

    public void delete(UserEntity userEntity) {
        usersDAO.delete(userEntity);
    }

    public void deleteAllUsers() {
        usersDAO.deleteAll();
    }

    public List<UserEntity> getAllUsers() {
        return usersDAO.getAll();
    }

    public List<UserEntity> searchAllUsers(String term) {
        return usersDAO.search(term);
    }

    public void insert(CurrentUserEntity currentUserEntity) {
        currentUserDAO.insert(currentUserEntity);
    }

    public void update(CurrentUserEntity currentUserEntity) {
        currentUserDAO.update(currentUserEntity);
    }

    public void delete(CurrentUserEntity currentUserEntity) {
        currentUserDAO.delete(currentUserEntity);
    }

    public void deleteCurrentUser() {
        currentUserDAO.deleteAll();
    }

    public List<CurrentUserEntity> getCurrentUser() {
        return currentUserDAO.getAll();
    }
}
