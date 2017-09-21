package com.pxworlds;

import com.google.gson.Gson;
import com.pxworlds.configuration.ConfigurationStorage;
import com.pxworlds.configuration.JsonConfig;
import com.pxworlds.screens.PxWorlds;

import java.awt.*;

import static com.pxworlds.Constants.TITLE;

public class Bootstrap {

    private static Bootstrap instance;

    private Gson                 gson;
    private JsonConfig           jsonConfig;
    private ConfigurationStorage configurationStorage;

    /**
     * The method which is the entrance point after psvm
     */
    public void onEnable() {
        instance = this;

        initGson();
        initJsonConfig();
        initConfigurationStorage();

        getConfigurationStorage().init();

        System.out.println(getConfigurationStorage().getScreenConfiguration().getLastHeight());

        new PxWorlds(TITLE, (int) getDimension().getWidth(), (int) getDimension().getHeight(), true);

    }

    public Dimension getDimension() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static Bootstrap getInstance() {
        return instance;
    }

    public void initGson() {
        gson = new Gson();
    }

    public void initJsonConfig() {
        jsonConfig = new JsonConfig();
    }

    public void initConfigurationStorage() {
        configurationStorage = new ConfigurationStorage();
    }

    public Gson getGson() {
        return gson;
    }

    public JsonConfig getJsonConfig() {
        return jsonConfig;
    }

    public ConfigurationStorage getConfigurationStorage() {
        return configurationStorage;
    }
}
