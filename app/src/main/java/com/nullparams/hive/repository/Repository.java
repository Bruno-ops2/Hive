package com.nullparams.hive.repository;

import android.app.Application;

import com.nullparams.hive.database.ChatUserDAO;
import com.nullparams.hive.database.ChatUserEntity;
import com.nullparams.hive.database.HiveDatabase;
import com.nullparams.hive.database.MessageDAO;
import com.nullparams.hive.database.MessageEntity;
import com.nullparams.hive.database.RecentSearchesDAO;
import com.nullparams.hive.database.RecentSearchesEntity;
import com.nullparams.hive.database.UserEntity;
import com.nullparams.hive.database.UsersDAO;

import java.util.List;

public class Repository {

    private UsersDAO usersDAO;
    private ChatUserDAO chatUserDAO;
    private RecentSearchesDAO recentSearchesDAO;
    private MessageDAO messageDAO;

    public Repository(Application application) {

        HiveDatabase hiveDatabase = HiveDatabase.getInstance(application);
        usersDAO = hiveDatabase.usersDAO();
        chatUserDAO = hiveDatabase.chatUserDAO();
        recentSearchesDAO = hiveDatabase.recentSearchesDAO();
        messageDAO = hiveDatabase.messageDAO();
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

    public void insert(ChatUserEntity chatUserEntity) {
        chatUserDAO.insert(chatUserEntity);
    }

    public void update(ChatUserEntity chatUserEntity) {
        chatUserDAO.update(chatUserEntity);
    }

    public void delete(ChatUserEntity chatUserEntity) {
        chatUserDAO.delete(chatUserEntity);
    }

    public void deleteChatUser() {
        chatUserDAO.deleteAll();
    }

    public List<ChatUserEntity> getChatUser() {
        return chatUserDAO.getAll();
    }

    public void insert(RecentSearchesEntity recentSearchesEntity) {
        recentSearchesDAO.insert(recentSearchesEntity);
    }

    public void update(RecentSearchesEntity recentSearchesEntity) {
        recentSearchesDAO.update(recentSearchesEntity);
    }

    public void delete(RecentSearchesEntity recentSearchesEntity) {
        recentSearchesDAO.delete(recentSearchesEntity);
    }

    public void deleteAllRecentSearches() {
        recentSearchesDAO.deleteAll();
    }

    public List<RecentSearchesEntity> getRecentSearches() {
        return recentSearchesDAO.getAll();
    }

    public long getTimeStamp(String term) {
        return recentSearchesDAO.getTimeStamp(term);
    }

    public void insert(MessageEntity messageEntity) {
        messageDAO.insert(messageEntity);
    }

    public void update(MessageEntity messageEntity) {
        messageDAO.update(messageEntity);
    }

    public void delete(MessageEntity messageEntity) {
        messageDAO.delete(messageEntity);
    }

    public void deleteAllMessages() {
        messageDAO.deleteAll();
    }

    public List<MessageEntity> getAllMessages() {
        return messageDAO.getAll();
    }

    public List<MessageEntity> searchAllMessages(String term) {
        return messageDAO.search(term);
    }
}
