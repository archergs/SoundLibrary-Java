package com.archergs.soundlibrary;

import java.io.File;

public class Launcher {
    public static void main(String[] args) {
        new File(System.getProperty("user.home") + "/SoundLibrary/").mkdirs();

        SoundLibraryApplication.launch(SoundLibraryApplication.class, args);
    }
}
