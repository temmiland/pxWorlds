package com.pxworlds.configuration;

import com.pxworlds.Bootstrap;
import com.pxworlds.Constants;

import java.io.*;
import java.lang.reflect.Type;

public class JsonConfig {

    public String generateConfigName(String configName) {
        return Constants.CONFIG_DIRECTORY_PATH + configName + ".json";
    }

    public void saveConfig(Configuration configuration, String fileName) {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(fileName));
            writer.write(configuration.serializeToJson());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T extends Configuration> T readConfiguration(String fileName, Type type) {
        StringBuilder stringBuilder = new StringBuilder();
        File file          = new File(fileName);
        try {
            FileReader     fileReader     = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String         currentLine    = "";
            while ((currentLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(currentLine);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return Bootstrap.getInstance().getGson().fromJson(stringBuilder.toString(), type);
        }catch(Exception e){
            saveConfig(new Configuration(), fileName);
            return Bootstrap.getInstance().getGson().fromJson("{}", type);
        }
    }

}
