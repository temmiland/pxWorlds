package com.pxworlds.worlds;

import com.pxworlds.Constants;
import com.pxworlds.util.Hash;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.ArrayList;

public class WorldManager {

    public void init() {
        createWorldDirectoryIfNotExists();
        downloadCorruptWorldConfigurations(checkWorldConfigurations());
    }

    private ArrayList<String> checkWorldConfigurations() {
        File[] folder = new File(Constants.WORLDS_DIRECTORY_PATH).listFiles();
        ArrayList<String> succeededChecksumList = new ArrayList<>();

        for (File file : folder) {
            int failedChecksum = 0;
            for (Worlds worlds : Worlds.values()) {
                if (!toHex(Hash.SHA512.checksum(file)).equals(worlds.getWorldChecksum())) {
                    failedChecksum++;
                    if (Worlds.values().length == failedChecksum) {
                        if (!file.delete()) System.exit(1);
                        break;
                    }
                } else {
                    if (!file.getName().equals(worlds.getWorldFileName())) {
                        if (!file.delete()) System.exit(1);
                        break;
                    } else {
                        succeededChecksumList.add(worlds.getWorldChecksum());
                    }
                }
            }
        }

        return succeededChecksumList;
    }

    private void downloadCorruptWorldConfigurations(ArrayList<String> succeededChecksumList) {
        for(Worlds worlds : Worlds.values()) {
            for(String succeededChecksum : succeededChecksumList) {
                if(!worlds.getWorldChecksum().equals(succeededChecksum)) {

                    // Methode für den Rest-Zugriff auf den Webserver fehlt --> Muss geschrieben werden damit die Datei heruntergeladen werden kann
                    // Download TO DO
                }
            }
        }
    }

    private void createWorldDirectoryIfNotExists() {
        File file = new File(Constants.WORLDS_DIRECTORY_PATH);
        if (!file.exists()) file.mkdirs();
    }

    private static String toHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }


}
