package com.archergs.soundlibrary;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.Desktop;
import com.mpatric.mp3agic.*;
import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.MediaSegment;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import io.lindstrom.m3u8.parser.ParsingMode;

public class SoundLibraryManager {

    public static String clientID = "Hd4akujkPoaPv8SOUw6sqAySNno8EM7b";
    public static File downloadLocation = new File(System.getProperty("user.home") + "/SoundLibrary/");

    public String resolveSoundcloudURL(String urlString){
        String apiURLString = "https://api-v2.soundcloud.com/resolve?client_id="+clientID+"&url=" + urlString;
        try {
            // create request connection
            URL url = new URI(apiURLString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // set headers
            connection.setRequestProperty("Content-Type", "application/json");

            // set connection timeout
            connection.setConnectTimeout(5000);

            String response = FullResponseBuilder.getFullResponse(connection);

            connection.disconnect();

            return response;
        } catch (Exception e) {
            System.out.println("An error occurred loading the URL!");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    public String loadPlaylistTracks(List<String> ids){
        String idList = String.join(",", ids);
        String apiURLString = "https://api-v2.soundcloud.com/tracks?client_id="+clientID+"&ids=" + idList;
        System.out.println(apiURLString);
        try {
            // create request connection
            URL url = new URI(apiURLString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // set headers
            connection.setRequestProperty("Content-Type", "application/json");

            // set connection timeout
            connection.setConnectTimeout(5000);

            String response = FullResponseBuilder.getFullResponse(connection);

            connection.disconnect();

            return response;
        } catch (Exception e) {
            System.out.println("An error occurred loading the URL!");
            e.printStackTrace();
        }

        return "";
    }

    public void downloadFile(SoundLibraryTrack track){
        String filename = removeIllegalCharacters(track.title, true);
        File outputFile = null;

        if (track.isPlaylist){
            outputFile = new File(downloadLocation + "/" + filename + ".m3u8");
        } else {
            outputFile = new File(downloadLocation + "/" + filename + ".mp3.old");
        }

        System.out.println("Downloading: " + outputFile);
        System.out.println(track.downloadURLString);

        if (outputFile.exists()){
            System.out.println("file exists!");
            return;
        }

        try {
            // create file
            outputFile.createNewFile();

            // write data to file
            BufferedInputStream in = new BufferedInputStream(new URI(track.downloadURLString).toURL().openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            fileOutputStream.close();
            in.close();

            if (track.isPlaylist){
                parsePlaylist(outputFile, track);
            } else {
                createFileMetadata(track, outputFile);
            }
        } catch (Exception e) {
            // handle exception
            System.out.println(outputFile);
            e.printStackTrace();
        }
    }

    private void parsePlaylist(File playlistFile, SoundLibraryTrack track){
        MediaPlaylistParser parser = new MediaPlaylistParser(ParsingMode.LENIENT);
        try {
            MediaPlaylist playlist = parser.readPlaylist(Path.of(playlistFile.getAbsolutePath()));

            byte[] songSegments = new byte[0];

            // download each song segment and stitch it together
            for (MediaSegment segment : playlist.mediaSegments()) {
                byte[] dataSegment = downloadPlaylistComponent(segment.uri());
                if (dataSegment != null) {
                    byte[] combined = new byte[songSegments.length + dataSegment.length];

                    System.arraycopy(songSegments, 0, combined, 0, songSegments.length);
                    System.arraycopy(dataSegment, 0, combined, songSegments.length, dataSegment.length);

                    songSegments = combined;
                }
            }

            // save stitched data to file
            String filename = removeIllegalCharacters(track.title, true);
            File outputFile = new File(downloadLocation + "/" + filename + ".mp3.old");
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            fileOutputStream.write(songSegments);
            fileOutputStream.close();

            // delete the playlist file
            playlistFile.delete();

            // add metadata
            createFileMetadata(track, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] downloadPlaylistComponent(String url){
        try {
            BufferedInputStream in = new BufferedInputStream(new URI(url).toURL().openStream());
            byte[] data = in.readAllBytes();
            in.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void createFileMetadata(SoundLibraryTrack track, File file) {
        try {
            Mp3File mp3File = new Mp3File(file);

            ID3v2 id3v2Tag;
            if (mp3File.hasId3v2Tag()) {
                id3v2Tag = mp3File.getId3v2Tag();
            } else {
                // mp3 does not have an ID3v2 tag, let's create one..
                id3v2Tag = new ID3v24Tag();
                mp3File.setId3v2Tag(id3v2Tag);
            }

            id3v2Tag.setTrack("1");
            id3v2Tag.setArtist(track.artist);
            id3v2Tag.setTitle(track.title);
            id3v2Tag.setAlbum(track.title);
            id3v2Tag.setGenreDescription(track.genre);
            id3v2Tag.setCopyright(track.license);
            id3v2Tag.setDate(track.creationDate);

            // get album art
            if (track.albumArtURLString.isEmpty()){
                System.out.println("no album art");
            } else {
                BufferedInputStream in = new BufferedInputStream(new URI(track.albumArtURLString).toURL().openStream());
                byte[] data = in.readAllBytes();

                System.out.println(data.length);

                id3v2Tag.setAlbumImage(data, "jpeg");
            }

            mp3File.save(file.getAbsolutePath().replace(".old", ""));
            file.delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void openTrackFileLocation(){
        try {
            Desktop.getDesktop().open(downloadLocation);
        } catch (IOException exception) {
            System.out.println("Couldn't open download location!");
        }
    }

    public static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return !resultString.isEmpty()
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }

    public static class FullResponseBuilder {
        public static String getFullResponse(HttpURLConnection connection) throws IOException {
            StringBuilder fullResponseBuilder = new StringBuilder();

            int status = connection.getResponseCode();

            // read response content
            Reader streamReader = null;

            if (status > 299) {
                streamReader = new InputStreamReader(connection.getErrorStream());
            } else {
                streamReader = new InputStreamReader(connection.getInputStream());
            }

            BufferedReader in = new BufferedReader(streamReader);
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            fullResponseBuilder.append(content);

            return fullResponseBuilder.toString();
        }
    }

    // SRC: http://www.java2s.com/example/java/java.io/removes-all-illegal-filename-characters-from-a-given-string.html
    public static final String removeIllegalCharacters(String name, final boolean singleSpaces) {
        // remove illegal characters and replace with a more friendly char ;)
        String safe = name.trim();

        // remove illegal characters
        safe = safe.replaceAll(
                "[\\/|\\\\|\\*|\\:|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]",
                "");

        // replace . dots with _ and remove the _ if at the end
        safe = safe.replaceAll("\\.", "_");
        if (safe.endsWith("_")) {
            safe = safe.substring(0, safe.length() - 1);
        }

        // replace whitespace characters with _
        safe = safe.replaceAll("\\s+", "_");

        // replace double or more spaces with a single one
        if (singleSpaces) {
            safe = safe.replaceAll("_{2,}", "_");
        }

        return safe;
    }
}
