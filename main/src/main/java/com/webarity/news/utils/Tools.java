package com.webarity.news.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.webarity.entities.helpers.JSONElements;

public interface Tools {

    static final ExecutorService ex = Executors.newFixedThreadPool(20);
    static final ForkJoinPool commentExecutor = new ForkJoinPool(50);
    static final ExecutorService  entityExecutor = Executors.newSingleThreadExecutor();

    public static JSONElements httpMessageRQ(long id) {
        HttpURLConnection conn = null;
        try {
            URL resource = new URL("https", "hacker-news.firebaseio.com", -1, String.format("/v0/item/%d.json", id));
            resource.openConnection();
            conn = (HttpURLConnection)resource.openConnection();
            conn.setRequestMethod("GET");
            JSONElements jse = Tools.processArticleEntry(conn.getInputStream());
            conn.disconnect();
            return jse;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static Set<Integer> httpTopStoriesRQ() {
        HttpURLConnection conn = null;
        Set<Integer> storyIDs = Collections.emptySet();
        try {
            URL resource = new URL("https", "hacker-news.firebaseio.com", -1, "/v0/topstories.json");
            resource.openConnection();
            conn = (HttpURLConnection)resource.openConnection();
            conn.setRequestMethod("GET");
            storyIDs = Tools.processStoriesList(conn.getInputStream());
            return storyIDs;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static Set<Integer> processStoriesList(InputStream is) {
        JsonParser par = Json.createParser(is);
        while (par.hasNext()) {
            Event e = par.next();
            switch (e) {
                case START_ARRAY: 
                    return par.getArray().stream().map(qwe -> Integer.valueOf(qwe.toString())).collect(Collectors.toSet());
                case END_ARRAY:
                case START_OBJECT:
                case END_OBJECT:
                case KEY_NAME:
                case VALUE_FALSE:
                case VALUE_TRUE:
                case VALUE_STRING:
                case VALUE_NUMBER:
                case VALUE_NULL:
                break;
            }
        }
        return null;
    }
    public static JSONElements processArticleEntry(InputStream is) {
        JsonParser par = Json.createParser(is);

        JSONElements data = new JSONElements();
        
        String key = null;
        Long num = null;
        String string = null;
        boolean val = false;
        boolean deleted = false;

        boolean isValid = false;

        while (par.hasNext()) {
            Event e = par.next();
            switch (e) {
                case START_ARRAY:
                data.comments = par.getArray().getValuesAs(JsonNumber.class).stream().map(a -> a.longValue()).collect(Collectors.toList());
                case END_ARRAY:
                case START_OBJECT:
                case END_OBJECT:
                break;
                case KEY_NAME:
                    key = par.getString();
                    break;
                case VALUE_FALSE:
                    deleted = false;
                    val = true;
                break;
                case VALUE_TRUE:
                    deleted = true;
                    val = true;
                break;
                case VALUE_STRING:
                    string = par.getString();
                    val = true;
                    break;
                case VALUE_NUMBER:
                    num = par.getLong();
                    val = true;
                    break;
                case VALUE_NULL:
                break;
            }
            if (key != null && val) {
                switch (key) {
                    case "deleted" :
                        if (deleted) {
                            data.by = "deleted";
                            data.commentText = "deleted";
                        }
                    break;
                    case "title" : data.article = string;
                    break;
                    case "score" : data.score = num.intValue();
                    break;
                    case "by" : data.by = ((string == null || string.length() < 1) ? "_null" : string);
                    break;
                    case "time" : data.time = num * 1000; //num is in seconds; convert it to millis
                    break;
                    case "type" : data.type = string;
                    break;
                    case "dead" : if (deleted) return null;
                    break;
                    case "id" : data.id = num;
                    break;
                    case "text" : data.commentText = ((string == null || string.length() < 1) ? "_null" : string);
                    break;
                    case "descendants": data.descendants = num;
                    break;
                    case "parent" : data.commentParent = num;
                    break;
                    case "url":
                        try {
                            data.url = new URL(string);
                        } catch (MalformedURLException e1) {
                            data.url = null;
                        }
                        break;
                }
                key = null;
                val = false;
                string = null;

                isValid = true;
            }
        }
        if (isValid) return data;
        else return null;
    }

    public static void log(String s, Object ...qwe) {
        System.out.println(String.format(s, qwe));
    }
}