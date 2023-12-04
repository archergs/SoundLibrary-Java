./mvnw clean package

jpackage --input target/ \
  --name SoundLibrary \
  --main-jar SoundLibrary-1.0-SNAPSHOT.jar \
  --main-class com.archergs.soundlibrary.Launcher \
  --type deb \
  --java-options '--enable-preview'
