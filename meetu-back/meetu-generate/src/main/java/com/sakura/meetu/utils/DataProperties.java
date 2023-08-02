package com.sakura.meetu.utils;

/**
 * @author sakura
 * @date 2023/7/28 15:15:19 周五
 */
public class DataProperties {
    String url;
    String username;
    String password;
    String database;

    public DataProperties(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public DataProperties(String url, String username, String password, String database) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.database = database;
    }
}
