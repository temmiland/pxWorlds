package com.pxworlds.configuration;

import com.google.gson.Gson;
import com.pxworlds.Constants;

import java.io.*;
import java.lang.reflect.Type;

public class JsonConfig {

    /** Gson-Instanz für Serialisierung/Deserialisierung. */
    private final Gson gson;

    public JsonConfig(Gson gson) {
        this.gson = gson;
    }

    public String generateConfigName(String configName) {
        return Constants.CONFIG_DIRECTORY_PATH + configName + ".json";
    }

    public void saveConfig(Configuration configuration, String fileName) {
        try {
            final Writer writer = new OutputStreamWriter(new FileOutputStream(fileName));
            writer.write(gson.toJson(configuration, configuration.getClass()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T extends Configuration> T readConfiguration(String fileName, Type type) {
        final StringBuilder stringBuilder = new StringBuilder();
        final File file = new File(fileName);
        try {
            final FileReader     fileReader     = new FileReader(file);
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(currentLine);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return gson.fromJson(stringBuilder.toString(), type);
        } catch (Exception e) {
            saveConfig(new Configuration(), fileName);
            return gson.fromJson("{}", type);
        }
    }

}
