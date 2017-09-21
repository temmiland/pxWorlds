package com.pxworlds.configuration;

import com.google.gson.reflect.TypeToken;
import com.pxworlds.Bootstrap;
import com.pxworlds.Constants;

import java.io.*;
import java.lang.reflect.Type;

public class JsonConfig {

    public boolean isConfigurationExisting(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }

    public String generateConfigName(String configName) {
        return Constants.CONFIG_DIRECTORY_PATH + configName + ".json";
    }

    public void saveConfig(Configuration configuration, String fileName) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
            writer.write(Bootstrap.getInstance().getGson().toJson(configuration));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T extends Configuration> T readConfiguration(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        File          file          = new File(fileName);
        try {
            FileReader     fileReader     = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String         currentLine    = "";
            while ((currentLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(currentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Type typeToken = new TypeToken<T>() {
        }.getType();
        return Bootstrap.getInstance().getGson().<T>fromJson(stringBuilder.toString(), typeToken);
    }

}
