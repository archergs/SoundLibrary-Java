package com.archergs.soundlibrary;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.json.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SoundLibraryController {

    @FXML private TextField soundcloudURLTextBox;
    @FXML private Button loadURLBtn;

    @FXML private ListView<SoundLibraryTrack> trackListView;
    @FXML private Button downloadTracksBtn;
    @FXML private Label statusLabel;

    public static SoundLibraryManager manager = new SoundLibraryManager();

    public ObservableList<SoundLibraryTrack> trackList = FXCollections.observableArrayList();

    public void loadURL(ActionEvent event) throws InterruptedException {
        // empty list and (re)load
        trackList = FXCollections.observableArrayList();

        statusLabel.setText("Loading URL...");

        trackListView.setItems(trackList);
        trackListView.setCellFactory(track -> new SoundLibraryTrackListCell());

        String urlString = soundcloudURLTextBox.getText();

        AtomicReference<String> response = new AtomicReference<>("");
        Thread resolveThread = new Thread(() -> {
            response.set(manager.resolveSoundcloudURL(urlString));
        });

        resolveThread.start();
        resolveThread.join();

        JSONObject jsonObject = new JSONObject(response.get());

        String responseKind = jsonObject.getString("kind");

        switch (responseKind){
            case "track":
                addTrack(jsonObject);
                break;
            case "playlist":
                parsePlaylist(jsonObject);
                break;
            default:
                return;
        }

        statusLabel.setText("Ready to download!");
    }

    public void downloadFiles(ActionEvent event) throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        statusLabel.setText("Downloaded " + count + "/" + trackList.size());
        for (SoundLibraryTrack track: trackList) {
            Thread downloadTrackThread = new Thread(() -> {
                manager.downloadFile(track);

                count.getAndIncrement();

                if (count.get() == trackList.size()){
                    Platform.runLater(() -> statusLabel.setText("Download complete!"));
                } else {
                    Platform.runLater(() -> statusLabel.setText("Downloaded " + count + "/" + trackList.size()));
                }
            });

            downloadTrackThread.start();
        }
    }

    public void openTrackLocation(ActionEvent event){
        manager.openTrackFileLocation();
    }

    private void addTrack(JSONObject jsonTrack){
        SoundLibraryTrack track = new SoundLibraryTrack(jsonTrack);

        Platform.runLater(() -> trackList.add(track));
    }

    private void parsePlaylist(JSONObject jsonPlaylist){
        JSONArray playlistTracks = jsonPlaylist.getJSONArray("tracks");
        List<String> ids = new ArrayList<>();

        for (int index = 0; index < playlistTracks.length(); index++) {
            JSONObject track = playlistTracks.getJSONObject(index);

            int id = track.getInt("id");
            ids.add(Integer.toString(id));
        }

        String response = manager.loadPlaylistTracks(ids);
        JSONArray loadedTracks = new JSONArray(response);

        for (int index = 0; index < loadedTracks.length(); index++) {
            int finalIndex = index;
            new Thread(() -> {
                JSONObject track = loadedTracks.getJSONObject(finalIndex);
                System.out.println(track);
                addTrack(track);
            }).start();
        }
    }
}