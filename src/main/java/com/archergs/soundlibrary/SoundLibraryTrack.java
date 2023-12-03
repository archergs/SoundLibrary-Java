package com.archergs.soundlibrary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class SoundLibraryTrack {

    private static String clientID = "Hd4akujkPoaPv8SOUw6sqAySNno8EM7b";

    public String title;
    public String artist;
    public String downloadURLString;
    public String streamURLString;
    public String albumArtURLString;
    public int duration;
    public String genre;
    public String license;
    public String creationDate;

    SoundLibraryTrack(JSONObject json){
        System.out.println(json);
        this.title = json.getString("title");
        this.artist = json.getJSONObject("user").getString("username");

        JSONObject transcoding = getStream(json.getJSONObject("media").getJSONArray("transcodings"));
        this.streamURLString = transcoding.getString("url");
        this.duration = transcoding.getInt("duration");

        this.downloadURLString = getDownloadURLString();
        System.out.println(this.downloadURLString);

        if (!json.isNull("artwork_url")){
            this.albumArtURLString = json.getString("artwork_url").replace("-large", "-t500x500");
        } else {
            this.albumArtURLString = "";
        }

        if (!json.isNull("genre")){
            this.genre = json.getString("genre");
        } else {
            this.genre = "";
        }

        if (!json.isNull("license")){
            this.license = json.getString("license");
        } else {
            this.license = "";
        }

        if (!json.isNull("created_at")){
            this.creationDate = json.getString("created_at");
        } else {
            this.creationDate = "";
        }
    }

    private String getDownloadURLString(){
        try {
            // create request connection
            URL url = new URI(streamURLString + "?client_id="+clientID).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // set headers
            connection.setRequestProperty("Content-Type", "application/json");

            // set connection timeout
            connection.setConnectTimeout(5000);

            String response = SoundLibraryManager.FullResponseBuilder.getFullResponse(connection);

            connection.disconnect();

            return new JSONObject(response).getString("url");
        } catch (Exception e) {
            System.out.println("An error occurred loading the URL!");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    private JSONObject getStream(JSONArray transcodings) {
        JSONObject hqFileProgressive = null;
        for (Object transcodingObj : transcodings) {
            JSONObject transcoding = (JSONObject) transcodingObj;
            if ((transcoding.getString("quality")).equals("hq") && isProgressiveFormat(transcoding)) {
                hqFileProgressive = transcoding;
                break;
            }
        }

        JSONObject sqFileProgressive = null;
        for (Object transcodingObj : transcodings) {
            JSONObject transcoding = (JSONObject) transcodingObj;
            if ((transcoding.getString("quality")).equals("sq") && isProgressiveFormat(transcoding)) {
                sqFileProgressive = transcoding;
                break;
            }
        }

        JSONObject hqFile = null;
        for (Object transcodingObj : transcodings) {
            JSONObject transcoding = (JSONObject) transcodingObj;
            if ((transcoding.getString("quality")).equals("hq")) {
                hqFile = transcoding;
                break;
            }
        }

        JSONObject sqFile = null;
        for (Object transcodingObj : transcodings) {
            JSONObject transcoding = (JSONObject) transcodingObj;
            if ((transcoding.getString("quality")).equals("sq")) {
                sqFile = transcoding;
                break;
            }
        }

        return hqFileProgressive != null ? hqFileProgressive
                : sqFileProgressive != null ? sqFileProgressive
                : hqFile != null ? hqFile
                : sqFile;
    }

    private boolean isProgressiveFormat(JSONObject transcoding){
        JSONObject format = transcoding.getJSONObject("format");
        return format.getString("protocol").equals("progressive");
    }
}
