package com.pxworlds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pxworlds.configuration.ConfigurationStorage;
import com.pxworlds.configuration.JsonConfig;
import com.pxworlds.game.PxWorlds;
import com.pxworlds.worlds.WorldManager;

import java.awt.*;

import static com.pxworlds.Constants.TITLE;

public class Bootstrap {

    private static Bootstrap instance;

    private Gson                 gson;
    private JsonConfig           jsonConfig;
    private ConfigurationStorage configurationStorage;
    private WorldManager         worldManager;
    private String               apiKey;

    /**
     * The method which is the entrance point after psvm
     */
    public void onEnable() {
        instance = this;

        this.apiKey = "abc";

        initGson();
        initJsonConfig();
        initConfigurationStorage();
        initWorldManager();

        getConfigurationStorage().init();
        getWorldManager().init();

        int width = getConfigurationStorage().getScreenConfiguration().getLastWidth();
        int height = getConfigurationStorage().getScreenConfiguration().getLastHeight();
        int frameRate = getConfigurationStorage().getScreenConfiguration().getFrameRate();
        boolean fullscreen = getConfigurationStorage().getScreenConfiguration().isFullscreen();

        new PxWorlds(TITLE, width, height, fullscreen, frameRate).run();

    }

    public Dimension getDimension() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static Bootstrap getInstance() {
        return instance;
    }

    public void initGson() {
        gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
    }

    public void initJsonConfig() {
        jsonConfig = new JsonConfig();
    }

    public void initConfigurationStorage() {
        configurationStorage = new ConfigurationStorage();
    }

    public void initWorldManager() {
        worldManager = new WorldManager();
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

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public String getApiKey() {
        return apiKey;
    }
}