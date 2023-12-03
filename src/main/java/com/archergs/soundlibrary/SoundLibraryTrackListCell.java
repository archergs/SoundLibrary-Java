package com.archergs.soundlibrary;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SoundLibraryTrackListCell extends ListCell<SoundLibraryTrack> {

    private final ImageView image = new ImageView();
    private final Label title = new Label();
    private final Label detail = new Label();
    private final VBox info = new VBox(title, detail);
    private final HBox layout = new HBox(image, info);

    @Override protected void updateItem(SoundLibraryTrack track, boolean empty){
        super.updateItem(track, empty);

        setText(null);

        if (empty || track == null || track.title == null) {
            title.setText(null);
            detail.setText(null);
            setGraphic(null);
        } else {
            /*if (!track.albumArtURLString.isEmpty()) {
                String albumArt = track.albumArtURLString.replace("-t500x500", "-large");
                Image img = new Image(albumArt, 32, 32, false, false);
                image.setImage(img);
            }*/

            title.setText(track.title);
            detail.setText(track.artist);

            info.setPadding(new Insets(0,0,0,4));
            layout.setPadding(new Insets(2,0,2,0));

            setGraphic(layout);
        }
    }
}
